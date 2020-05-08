 package eu.fusepool.datalifecycle;
 
 import java.security.AccessController;
 import java.security.AllPermission;
 
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import org.apache.clerezza.jaxrs.utils.RedirectUtil;
 import org.apache.clerezza.jaxrs.utils.TrailingSlash;
 import org.apache.clerezza.rdf.core.MGraph;
 import org.apache.clerezza.rdf.core.UriRef;
 import org.apache.clerezza.rdf.core.access.LockableMGraph;
 import org.apache.clerezza.rdf.core.access.TcManager;
 import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
 import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
 import org.apache.clerezza.rdf.core.serializedform.Serializer;
 import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
 import org.apache.clerezza.rdf.ontologies.RDFS;
 import org.apache.clerezza.rdf.utils.GraphNode;
 import org.apache.felix.scr.annotations.Activate;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Deactivate;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.Service;
 import org.apache.stanbol.commons.web.viewable.RdfViewable;
 import org.osgi.service.component.ComponentContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Component
 @Property(name = "javax.ws.rs", boolValue = true)
 @Service(Object.class)
 @Path("pipesadmin")
 public class PipesAdmin {
 	
 	/**
      * This service allows accessing and creating persistent triple collections
      */
     @Reference
     private TcManager tcManager;
     
     @Reference
     private Serializer serializer;
     
     /**
      * Using slf4j for normal logging
      */
     private static final Logger log = LoggerFactory.getLogger(PipesAdmin.class);
     
     private UriRef DATA_LIFECYCLE_GRAPH_REFERENCE = new UriRef("urn:x-localinstance:/dlc/meta.graph");
     
     /**
      * This method return an RdfViewable, this is an RDF serviceUri with
      * associated presentational information.
      */
     @GET
     public RdfViewable serviceEntry(@Context final UriInfo uriInfo,
             @QueryParam("url") final UriRef url,
             @HeaderParam("user-agent") String userAgent) throws Exception {
         //this maks sure we are nt invoked with a trailing slash which would affect
         //relative resolution of links (e.g. css)
         TrailingSlash.enforcePresent(uriInfo);
 
         final String resourcePath = uriInfo.getAbsolutePath().toString();
         if (url != null) {
             String query = url.toString();
             log.info(query);
         }
 
         //The URI at which this service was accessed, this will be the 
         //central serviceUri in the response
         final UriRef serviceUri = new UriRef(resourcePath);
         
         //This GraphNode represents the service within our result graph
         final GraphNode node = new GraphNode(DATA_LIFECYCLE_GRAPH_REFERENCE, getDlcGraph());
                 
         //What we return is the GraphNode to the template with the same path and name 
         return new RdfViewable("PipesAdmin", node, PipesAdmin.class);
     }
     
     @POST
     @Path("empty_graph")
     @Produces("text/plain")
     public String emptyGraphRequest(@Context final UriInfo uriInfo,  
     		@FormParam("graph") final String graphName) throws Exception {
         AccessController.checkPermission(new AllPermission());
         String message = "";
         
         UriRef graphRef = new UriRef(graphName);
         
         tcManager.getMGraph(graphRef).clear();
         
         message += " Graph: " + graphName + " empty";
         
         return message;
     }
     
     @GET
     @Path("get_graph")
     @Produces("text/plain")
     public Response getGraphRequest(@Context final UriInfo uriInfo,
     		@QueryParam("graph") final String graphName) throws Exception {
     	
    	String graphUrl = uriInfo.getBaseUri() + "graph?name=" + graphName;
     	
     	
     	return RedirectUtil.createSeeOtherResponse(graphUrl, uriInfo);
     	
     	
     }
     
     
     /**
      * Returns the data life cycle graph containing all the monitored graphs. It
      * creates it if doesn't exit yet.
      *
      * @return
      */
     private LockableMGraph getDlcGraph() {
         return tcManager.getMGraph(DATA_LIFECYCLE_GRAPH_REFERENCE);
     }
     
     
     @Activate
     protected void activate(ComponentContext context) {
 
         log.info("The Pipes Admin service is being activated");
 
     }
     
     @Deactivate
     protected void deactivate(ComponentContext context) {
         log.info("The Pipes Admin service is being deactivated");
     }
 }
