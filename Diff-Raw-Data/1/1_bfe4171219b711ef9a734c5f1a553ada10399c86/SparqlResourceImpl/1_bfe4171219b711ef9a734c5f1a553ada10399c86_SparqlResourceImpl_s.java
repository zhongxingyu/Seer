 /**
  * PODD is an OWL ontology database used for scientific project management
  * 
  * Copyright (C) 2009-2013 The University Of Queensland
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU Affero General Public License as published by the Free Software Foundation, either version 3
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License along with this program.
  * If not, see <http://www.gnu.org/licenses/>.
  */
 package com.github.podd.resources;
 
 import java.io.ByteArrayOutputStream;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import org.openrdf.OpenRDFException;
 import org.openrdf.model.Model;
 import org.openrdf.model.URI;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.openrdf.model.vocabulary.SESAME;
 import org.openrdf.query.GraphQuery;
 import org.openrdf.query.QueryLanguage;
 import org.openrdf.query.impl.DatasetImpl;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.Rio;
 import org.openrdf.rio.helpers.StatementCollector;
 import org.restlet.data.MediaType;
 import org.restlet.data.Status;
 import org.restlet.representation.ByteArrayRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.representation.Variant;
 import org.restlet.resource.Get;
 import org.restlet.resource.ResourceException;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLOntologyID;
 
 import com.github.podd.exception.UnmanagedArtifactIRIException;
 import com.github.podd.exception.UnmanagedArtifactVersionException;
 import com.github.podd.restlet.PoddAction;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.PoddWebConstants;
 
 /**
  * Service for executing SPARQL queries over specified artifacts (and their Schema ontologies) that
  * users have access to.
  * 
  * @author Peter Ansell p_ansell@yahoo.com
  * 
  */
 public class SparqlResourceImpl extends AbstractPoddResourceImpl
 {
     @Get("rdf|rj|json|ttl")
     public Representation getSparqlRdf(final Variant variant) throws ResourceException
     {
         this.log.debug("getSparqlRdf");
         
         final ByteArrayOutputStream output = new ByteArrayOutputStream(8096);
         
         // sparql query - mandatory parameter
         final String sparqlQuery = this.getQuery().getFirstValue(PoddWebConstants.KEY_SPARQLQUERY, true);
         if(sparqlQuery == null)
         {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "SPARQL query not submitted");
         }
         
         // artifact ids to search across
         final String[] artifactUris = this.getQuery().getValuesArray(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER);
         
         if(artifactUris == null || artifactUris.length == 0)
         {
             // TODO: Support execution of sparql queries over all accessible artifacts if they did
             // not specify any artifacts
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No artifacts specified in request");
         }
         
         final String includeConcreteStatements =
                 this.getQuery().getFirstValue(PoddWebConstants.KEY_INCLUDE_CONCRETE, true);
         boolean includeConcrete = true;
         if(includeConcreteStatements != null)
         {
             includeConcrete = Boolean.valueOf(includeConcreteStatements);
         }
         final String includeInferredStatements =
                 this.getQuery().getFirstValue(PoddWebConstants.KEY_INCLUDE_INFERRED, true);
         boolean includeInferred = true;
         if(includeInferredStatements != null)
         {
             includeInferred = Boolean.valueOf(includeInferredStatements);
         }
         final String includeSchemaStatements = this.getQuery().getFirstValue(PoddWebConstants.KEY_INCLUDE_SCHEMA, true);
         boolean includeSchema = true;
         if(includeSchemaStatements != null)
         {
             includeSchema = Boolean.valueOf(includeSchemaStatements);
         }
         
         final Set<InferredOWLOntologyID> artifactIds = new LinkedHashSet<>();
         
         final Model results = new LinkedHashModel();
         for(final String nextArtifactUri : artifactUris)
         {
             try
             {
                 final InferredOWLOntologyID ontologyID =
                         this.getPoddArtifactManager().getArtifact(IRI.create(nextArtifactUri));
                 
                 if(this.getPoddArtifactManager().isPublished(ontologyID))
                 {
                     this.checkAuthentication(PoddAction.PUBLISHED_ARTIFACT_READ, ontologyID.getOntologyIRI()
                             .toOpenRDFURI());
                 }
                 else
                 {
                     this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_READ, ontologyID.getOntologyIRI()
                             .toOpenRDFURI());
                 }
             }
             catch(final UnmanagedArtifactIRIException e)
             {
                 throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not find a requested artifact", e);
             }
             catch(OpenRDFException e)
             {
                 throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Repository exception occurred", e);
             }
         }
         
         for(final InferredOWLOntologyID ontologyID : artifactIds)
         {
             RepositoryConnection conn = null;
             try
             {
                 final Collection<OWLOntologyID> schemaImports =
                         this.getPoddArtifactManager().getSchemaImports(ontologyID);
                 conn = this.getPoddRepositoryManager().getPermanentRepository(schemaImports).getConnection();
                 final Set<URI> contextSet = new HashSet<>();
                 if(includeConcrete)
                 {
                     contextSet.addAll(Arrays.asList(this.getPoddSesameManager().versionContexts(ontologyID)));
                 }
                 if(includeInferred)
                 {
                     contextSet.addAll(Arrays.asList(this.getPoddSesameManager().inferredContexts(ontologyID)));
                 }
                 if(includeSchema)
                 {
                     contextSet.addAll(Arrays.asList(this.getPoddSesameManager().schemaContexts(ontologyID, conn,
                             this.getPoddRepositoryManager().getSchemaManagementGraph())));
                 }
                 // TODO: Support cross-artifact queries if they all import the same schemas
                 final URI[] contexts = contextSet.toArray(new URI[0]);
                 // MUST not perform queries on all contexts
                 if(this.getPoddRepositoryManager().safeContexts(contexts))
                 {
                     final GraphQuery query = conn.prepareGraphQuery(QueryLanguage.SPARQL, sparqlQuery);
                     
                     final DatasetImpl dataset = new DatasetImpl();
                     
                     for(final URI nextUri : contexts)
                     {
                         dataset.addDefaultGraph(nextUri);
                         dataset.addNamedGraph(nextUri);
                     }
                     
                     query.setDataset(dataset);
                     
                     query.evaluate(new StatementCollector(results));
                 }
                 else
                 {
                     this.log.error(
                             "Could not determine contexts for artifact, or included an unsafe context: ontology=<{}> contexts=<{}>",
                             ontologyID, contextSet);
                 }
             }
             catch(final UnmanagedArtifactIRIException e)
             {
                 throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not find a requested artifact", e);
             }
             catch(final UnmanagedArtifactVersionException e)
             {
                 throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not find a requested artifact", e);
             }
             catch(final OpenRDFException e)
             {
                 // TODO: May want to ignore this for stability of queries in the long term
                 throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Repository exception occurred", e);
             }
             finally
             {
                 if(conn != null)
                 {
                     try
                     {
                         conn.close();
                     }
                     catch(RepositoryException e)
                     {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
                         this.log.error("Could not close repository connection: ", e);
                     }
                 }
             }
         }
         
         final RDFFormat resultFormat =
                 Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML);
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
 }
