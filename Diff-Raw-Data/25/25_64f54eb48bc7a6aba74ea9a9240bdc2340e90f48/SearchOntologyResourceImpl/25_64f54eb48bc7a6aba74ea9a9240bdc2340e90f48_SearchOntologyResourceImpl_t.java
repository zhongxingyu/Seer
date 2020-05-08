 /**
  * 
  */
 package com.github.podd.resources;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.openrdf.OpenRDFException;
 import org.openrdf.model.Model;
 import org.openrdf.model.URI;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.Rio;
 import org.openrdf.rio.UnsupportedRDFormatException;
 import org.restlet.data.MediaType;
 import org.restlet.data.Status;
 import org.restlet.representation.ByteArrayRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.representation.Variant;
 import org.restlet.resource.Get;
 import org.restlet.resource.Post;
 import org.restlet.resource.ResourceException;
 import org.restlet.security.User;
 import org.semanticweb.owlapi.model.IRI;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.podd.exception.UnmanagedArtifactIRIException;
 import com.github.podd.restlet.PoddAction;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.PoddRdfConstants;
 import com.github.podd.utils.PoddWebConstants;
 
 /**
  * Search ontology service to search for matching results from an Ontology.
  * 
  * @author kutila
  * 
  */
 public class SearchOntologyResourceImpl extends AbstractPoddResourceImpl
 {
     
     private final Logger log = LoggerFactory.getLogger(this.getClass());
     
     @Get("rdf|rj|json|ttl")
     public Representation getRdf(final Variant variant) throws ResourceException
     {
         this.log.info("searchRdf");
         
         final ByteArrayOutputStream output = new ByteArrayOutputStream(8096);
         
         // search term - mandatory parameter
         final String searchTerm = this.getQuery().getFirstValue(PoddWebConstants.KEY_SEARCHTERM);
         if(searchTerm == null)
         {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Search term not submitted");
         }
         
         // artifact ID - optional parameter
         final String artifactUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER);
         InferredOWLOntologyID ontologyID = null;
         if(artifactUri != null)
         {
             try
             {
                 ontologyID = this.getPoddArtifactManager().getArtifact(IRI.create(artifactUri));
             }
             catch(final UnmanagedArtifactIRIException e)
             {
                 throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not find the given artifact", e);
             }
         }
         
         // search Types - optional parameter
         final String[] searchTypes = this.getQuery().getValuesArray(PoddWebConstants.KEY_SEARCH_TYPES);
         final Set<URI> set = new HashSet<URI>();
         if(searchTypes != null && searchTypes.length > 0)
         {
             for(final String searchType : searchTypes)
             {
                 set.add(PoddRdfConstants.VF.createURI(searchType));
             }
         }
         
         this.log.info("requesting search ({}): {}, {}, {}", variant.getMediaType().getName(), searchTerm, artifactUri,
                 searchTypes);
         
        if(ontologyID == null)
        {
            // only when a Project Admin is creating a new artifact 
            this.checkAuthentication(PoddAction.ARTIFACT_CREATE, Collections.<URI> emptySet());
        }
        else
        {
            this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_READ,
                    Collections.singleton(ontologyID.getOntologyIRI().toOpenRDFURI()));
        }
         
         final User user = this.getRequest().getClientInfo().getUser();
         this.log.info("authenticated user: {}", user);
         
         Model results = null;
         try
         {
             results =
                     this.getPoddArtifactManager().searchForOntologyLabels(ontologyID, searchTerm,
                             set.toArray(new URI[0]));
         }
         catch(final OpenRDFException e)
         {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed searching for Ontology Labels", e);
         }
         this.log.info("Found {} matches for this search term", results.size());
         
         RDFFormat resultFormat = Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML);
         // - prepare response
         try
         {
             
             Rio.write(results, output, resultFormat);
         }
         catch(final OpenRDFException e)
         {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Error while preparing response", e);
         }
         
         return new ByteArrayRepresentation(output.toByteArray(), MediaType.valueOf(resultFormat.getDefaultMIMEType()));
     }
     
     /**
      * Handle an HTTP POST requesting information about the content passed in.
      */
     @Post("rdf|rj|json|ttl")
     public Representation postRdf(final Representation entity, final Variant variant) throws ResourceException
     {
         // artifact ID - optional parameter
         final String artifactUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER);
         InferredOWLOntologyID ontologyID = null;
         if(artifactUri != null)
         {
             try
             {
                 ontologyID = this.getPoddArtifactManager().getArtifact(IRI.create(artifactUri));
             }
             catch(final UnmanagedArtifactIRIException e)
             {
                 throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not find the given artifact", e);
             }
         }
         
        if(ontologyID == null)
        {
            // only when a Project Admin is creating a new artifact 
            this.checkAuthentication(PoddAction.ARTIFACT_CREATE, Collections.<URI> emptySet());
        }
        else
        {
            this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_READ,
                    Collections.singleton(ontologyID.getOntologyIRI().toOpenRDFURI()));
        }
         
         // - get input stream with incoming content
         InputStream inputStream = null;
         try
         {
             inputStream = entity.getStream();
         }
         catch(final IOException e)
         {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "There was a problem with the input", e);
         }
         final RDFFormat inputFormat = Rio.getParserFormatForMIMEType(entity.getMediaType().getName(), RDFFormat.RDFXML);
         final RDFFormat outputFormat =
                 Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML);
         
         // - prepare response
         final ByteArrayOutputStream output = new ByteArrayOutputStream(8096);
         try
         {
             // read input content into a Model
             final Model inputModel = Rio.parse(inputStream, "", inputFormat);
             
             final Model resultModel = this.getPoddArtifactManager().fillMissingData(ontologyID, inputModel);
             
             // - write the result Model into response
             Rio.write(resultModel, output, outputFormat);
         }
         catch(OpenRDFException e)
         {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create response", e);
         }
         catch(UnsupportedRDFormatException e)
         {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not parse input format", e);
         }
         catch(IOException e)
         {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not parse input", e);
         }
         
         return new ByteArrayRepresentation(output.toByteArray(), MediaType.valueOf(outputFormat.getDefaultMIMEType()));
     }
     
 }
