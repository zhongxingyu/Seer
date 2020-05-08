 /**
  * 
  */
 package com.github.podd.resources;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.openrdf.OpenRDFException;
 import org.openrdf.model.URI;
 import org.openrdf.model.impl.ValueFactoryImpl;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFHandlerException;
 import org.openrdf.rio.RDFWriter;
 import org.openrdf.rio.Rio;
 import org.restlet.data.MediaType;
 import org.restlet.data.Status;
 import org.restlet.representation.ByteArrayRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.representation.Variant;
 import org.restlet.resource.Get;
 import org.restlet.resource.Post;
 import org.restlet.resource.ResourceException;
 import org.restlet.security.User;
 import org.semanticweb.owlapi.model.OWLException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.podd.api.FileReferenceVerificationPolicy;
 import com.github.podd.exception.FileReferenceVerificationFailureException;
import com.github.podd.exception.OntologyNotInProfileException;
 import com.github.podd.exception.PoddException;
 import com.github.podd.restlet.PoddAction;
 import com.github.podd.restlet.RestletUtils;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.OntologyUtils;
 import com.github.podd.utils.PoddRdfConstants;
 import com.github.podd.utils.PoddWebConstants;
 
 /**
  * 
  * Attach a file reference to a PODD artifact
  * 
  * @author kutila
  * 
  */
 public class FileReferenceAttachResourceImpl extends AbstractPoddResourceImpl
 {
     
     private final Logger log = LoggerFactory.getLogger(this.getClass());
     
     @Post("rdf|rj|ttl")
     public Representation attachFileReferenceRdf(final Representation entity, final Variant variant)
         throws ResourceException
     {
         // check mandatory parameter: artifact IRI
         final String artifactUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER);
         if(artifactUri == null)
         {
             this.log.error("Artifact ID not submitted");
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact IRI not submitted");
         }
         
         this.checkAuthentication(PoddAction.ARTIFACT_EDIT,
                 Collections.singleton(PoddRdfConstants.VALUE_FACTORY.createURI(artifactUri)));
         
         // check mandatory parameter: artifact version IRI
         final String versionUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER);
         if(versionUri == null)
         {
             this.log.error("Artifact Version IRI not submitted");
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact Version IRI not submitted");
         }
         
         // check optional parameter: whether file references should be verified. Defaults to NO
         final String verifyFileRefs = this.getQuery().getFirstValue(PoddWebConstants.KEY_VERIFICATION_POLICY);
         FileReferenceVerificationPolicy verificationPolicy = FileReferenceVerificationPolicy.DO_NOT_VERIFY;
         if(verifyFileRefs != null && Boolean.valueOf(verifyFileRefs))
         {
             verificationPolicy = FileReferenceVerificationPolicy.VERIFY;
         }
         
         // get input stream containing RDF statements
         InputStream inputStream = null;
         try
         {
             inputStream = entity.getStream();
         }
         catch(final IOException e)
         {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "There was a problem with the input", e);
         }
         final RDFFormat inputFormat =
                 Rio.getParserFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML);
         
         this.log.info("@Post attachFileReference ({})", variant.getMediaType().getName());
         
         InferredOWLOntologyID artifactMap = null;
         try
         {
             artifactMap =
                     this.getPoddArtifactManager().attachFileReferences(
                             ValueFactoryImpl.getInstance().createURI(artifactUri),
                             ValueFactoryImpl.getInstance().createURI(versionUri), inputStream, inputFormat,
                             verificationPolicy);
         }
         catch(FileReferenceVerificationFailureException e)
         {
             log.error("File reference validation errors: {}", e.getValidationFailures());
             throw new ResourceException(Status.SERVER_ERROR_BAD_GATEWAY, "File reference(s) failed verification", e);
         }
        catch(OntologyNotInProfileException e)
        {
            log.error("The ontology was not suitable for our reasoner after the changes: {}", e.getProfileReport());
            throw new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "Ontology was not consistent after the changes. Was the parent object correct before the submission.",
                    e);
        }
         catch(OpenRDFException | PoddException | IOException | OWLException e)
         {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not attach file references", e);
         }
         
         this.log.info("Successfully attached file reference to artifact {}", artifactMap);
         
         // prepare output: Artifact ID, object URI, file reference URI
         final ByteArrayOutputStream output = new ByteArrayOutputStream(8096);
         
         final RDFWriter writer =
                 Rio.createWriter(Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML),
                         output);
         try
         {
             writer.startRDF();
             OntologyUtils.ontologyIDsToHandler(Arrays.asList(artifactMap), writer);
             writer.endRDF();
         }
         catch(final RDFHandlerException e)
         {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create response");
         }
         
         return new ByteArrayRepresentation(output.toByteArray(), MediaType.valueOf(writer.getRDFFormat()
                 .getDefaultMIMEType()));
     }
     
     @Get
     public Representation attachFileReferencePageHtml(final Representation entity) throws ResourceException
     {
         // TODO: set required object URIs
         final Collection<URI> objectUris = Collections.<URI> emptySet();
         this.checkAuthentication(PoddAction.ARTIFACT_CREATE, objectUris);
         
         this.log.info("attachFileRefHtml");
         final User user = this.getRequest().getClientInfo().getUser();
         
         this.log.info("authenticated user: {}", user);
         
         final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
         dataModel.put("contentTemplate", "index.html.ftl");
         dataModel.put("pageTitle", "TODO: Attach File Reference");
         
         final Map<String, Object> artifactDataMap = this.getRequestedArtifact();
         dataModel.put("requestedArtifact", artifactDataMap);
         
         // Output the base template, with contentTemplate from the dataModel defining the
         // template to use for the content in the body of the page
         return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                 MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
     }
     
     // FIXME: populating dummy info for test
     private Map<String, Object> getRequestedArtifact()
     {
         final Map<String, Object> testArtifactMap = new HashMap<String, Object>();
         testArtifactMap.put("TODO: ", "Implement FileReferenceAttachResourceImpl");
         
         final Map<String, String> roleMap = new HashMap<String, String>();
         roleMap.put("description", "A dummy user account for testing");
         testArtifactMap.put("repositoryRole", roleMap);
         
         return testArtifactMap;
     }
     
 }
