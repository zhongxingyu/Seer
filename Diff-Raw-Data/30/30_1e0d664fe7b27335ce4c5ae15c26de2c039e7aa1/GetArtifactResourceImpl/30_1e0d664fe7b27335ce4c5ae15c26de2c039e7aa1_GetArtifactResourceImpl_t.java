 /**
  * 
  */
 package com.github.podd.resources;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.openrdf.OpenRDFException;
 import org.openrdf.model.URI;
 import org.openrdf.rio.RDFFormat;
 import org.restlet.data.MediaType;
 import org.restlet.data.Status;
 import org.restlet.representation.ByteArrayRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.representation.Variant;
 import org.restlet.resource.Get;
 import org.restlet.resource.ResourceException;
 import org.restlet.security.User;
 import org.semanticweb.owlapi.model.IRI;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.podd.exception.PoddException;
import com.github.podd.exception.UnmanagedArtifactIRIException;
 import com.github.podd.restlet.PoddAction;
 import com.github.podd.restlet.RestletUtils;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.PoddWebConstants;
 
 /**
  * 
  * TODO: Empty class with logic not implemented
  * 
  * Get an artifact from PODD. This resource handles requests for asserted statements as well as
  * inferred statements.
  * 
  * @author kutila
  * 
  */
 public class GetArtifactResourceImpl extends AbstractPoddResourceImpl
 {
     
     private final Logger log = LoggerFactory.getLogger(this.getClass());
     
     @Get("html")
     public Representation getArtifactPageHtml(final Representation entity) throws ResourceException
     {
         // TODO: set requested artifact URI
         Collection<URI> objectUris = Collections.<URI> emptySet();
         this.checkAuthentication(PoddAction.ARTIFACT_CREATE, objectUris);
         
         this.log.info("getArtifactHtml");
         final User user = this.getRequest().getClientInfo().getUser();
         
         this.log.info("authenticated user: {}", user);
         
         final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
         dataModel.put("contentTemplate", "index.html.ftl");
         dataModel.put("pageTitle", "TODO: Display Artifact");
         
         final Map<String, Object> artifactDataMap = this.getRequestedArtifact();
         dataModel.put("requestedArtifact", artifactDataMap);
         
         // Output the base template, with contentTemplate from the dataModel defining the
         // template to use for the content in the body of the page
         return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                 MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
     }
     
     @Get("rdf|rj")
     public Representation getArtifactRdf(final Representation entity, final Variant variant) throws ResourceException
     {
         final ByteArrayOutputStream stream = new ByteArrayOutputStream();
         
         try
         {
            // FIXME: Change the URI munging approach from the prototype to use a query parameter
            String artifactUri = this.getRequest().getResourceRef().getQueryAsForm().getFirstValue("artifact", true);
            
            InferredOWLOntologyID ontologyID =
                    this.getPoddApplication().getPoddArtifactManager().getArtifactByIRI(IRI.create(artifactUri));
            
            // TODO: support prototype method for this
            String includeInferredString =
                    this.getRequest().getResourceRef().getQueryAsForm().getFirstValue("includeInferred", true);
            boolean includeInferred = Boolean.valueOf(includeInferredString);
            
             this.getPoddApplication()
                     .getPoddArtifactManager()
                     .exportArtifact(ontologyID, stream,
                             RDFFormat.forMIMEType(variant.getMediaType().getName(), RDFFormat.TURTLE), includeInferred);
         }
        catch(UnmanagedArtifactIRIException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find the given artifact", e);
        }
         catch(OpenRDFException | PoddException | IOException e)
         {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to export artifact", e);
         }
         
         return new ByteArrayRepresentation(stream.toByteArray());
     }
     
     // FIXME: populating dummy info for test
     private Map<String, Object> getRequestedArtifact()
     {
         final Map<String, Object> testArtifactMap = new HashMap<String, Object>();
         testArtifactMap.put("TODO: ", "Implement GetArtifactResourceImpl");
         
         final Map<String, String> roleMap = new HashMap<String, String>();
         roleMap.put("description", "A dummy user account for testing");
         testArtifactMap.put("repositoryRole", roleMap);
         
         return testArtifactMap;
     }
     
 }
