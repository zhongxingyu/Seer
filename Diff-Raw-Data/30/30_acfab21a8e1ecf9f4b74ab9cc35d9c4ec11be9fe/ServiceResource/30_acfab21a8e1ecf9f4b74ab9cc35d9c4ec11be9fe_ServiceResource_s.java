 package pl.psnc.dl.wf4ever.evo;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriInfo;
 
 import org.apache.log4j.Logger;
 import org.openrdf.rio.RDFFormat;
 
import pl.psnc.dl.wf4ever.Constants;

 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.rdf.model.Literal;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.Resource;
 
 /**
  * REST API resource for the root of the service.
  * 
  * @author piotrhol
  * 
  */
 @Path("evo/")
 public class ServiceResource {
 
     /** logger. */
     @SuppressWarnings("unused")
     private static final Logger LOGGER = Logger.getLogger(ServiceResource.class);
 
     /** Context. */
     @Context
     private HttpServletRequest request;
 
     /** URI info. */
     @Context
     private UriInfo uriInfo;
 
 
     /**
      * Get a service description as an RDF graph.
      * 
      * @return RDF service description, format subject to content-negotiation
      */
     @GET
    public Response getServiceDescription() {
        RDFFormat format = RDFFormat.forMIMEType(request.getHeader(Constants.ACCEPT_HEADER));
        if (format == null) {
            return Response.status(Status.UNSUPPORTED_MEDIA_TYPE).build();
        }
 
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
         Resource service = model.createResource(uriInfo.getAbsolutePath().toString());
         Property copy = model.createProperty("http://purl.org/ro/service/evolution/copy");
         Property finalize = model.createProperty("http://purl.org/ro/service/evolution/finalize");
         Property info = model.createProperty("http://purl.org/ro/service/evolution/info");
        Literal copyTpl = model.createLiteral(uriInfo.getAbsolutePathBuilder().path("copy/").toString());
        Literal finalizeTpl = model.createLiteral(uriInfo.getAbsolutePathBuilder().path("finalize/").toString());
        Literal infoTpl = model.createLiteral(uriInfo.getAbsolutePathBuilder().path("info").toString() + "{?ro}");
        service.addProperty(copy, copyTpl);
        service.addProperty(finalize, finalizeTpl);
         service.addProperty(info, infoTpl);
 
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         model.write(out, format.getName().toUpperCase(), null);
         InputStream description = new ByteArrayInputStream(out.toByteArray());
 
         return Response.ok(description, format.getDefaultMIMEType()).build();
     }
 }
