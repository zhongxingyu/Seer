 package org.triple_brain.service.resources;
 
 import com.google.inject.assistedinject.Assisted;
 import com.google.inject.assistedinject.AssistedInject;
 import org.triple_brain.module.common_utils.Uris;
 import org.triple_brain.module.model.UserUris;
 import org.triple_brain.module.model.graph.Edge;
 import org.triple_brain.module.model.graph.GraphFactory;
 import org.triple_brain.module.model.graph.UserGraph;
 import org.triple_brain.module.model.graph.Vertex;
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 
 import static org.triple_brain.module.common_utils.Uris.decodeURL;
 
 
 /**
  * Copyright Mozilla Public License 1.1
  */
 @Produces(MediaType.APPLICATION_JSON)
 public class EdgeResource {
 
     @Inject
     GraphFactory graphFactory;
 
     private UserGraph userGraph;
 
     @AssistedInject
     public EdgeResource(
         @Assisted UserGraph userGraph
     ){
         this.userGraph = userGraph;
     }
 
     @POST
     @Path("/")
     public Response addRelation(
             @QueryParam("sourceVertexId") String sourceVertexId,
             @QueryParam("destinationVertexId") String destinationVertexId,
             @Context UriInfo uriInfo
         ){
         try{
             sourceVertexId = decodeURL(sourceVertexId);
             destinationVertexId = decodeURL(destinationVertexId);
         }catch (UnsupportedEncodingException e){
             return Response.status(Response.Status.BAD_REQUEST).build();
         }
         Vertex sourceVertex = userGraph.vertexWithURI(URI.create(
             sourceVertexId
         ));
         Vertex destinationVertex = userGraph.vertexWithURI(URI.create(
             destinationVertexId
         ));
         Edge createdEdge = sourceVertex.addRelationToVertex(destinationVertex);
         return Response.created(URI.create(
                 UserUris.edgeShortId(createdEdge.id())
         )).build();
     }
 
     @DELETE
     @Produces(MediaType.TEXT_PLAIN)
     @Path("/{edgeShortId}")
     public Response removeRelation(
        @Context HttpServletRequest request
     ){
         Edge edge = userGraph.edgeWithUri(Uris.get(
                 request.getRequestURI()
         ));
         edge.remove();
         return Response.ok().build();
     }
 
     @POST
     @Path("{edgeShortId}/label")
     @Produces(MediaType.TEXT_PLAIN)
     public Response modifyEdgeLabel(
             @PathParam("edgeShortId") String edgeShortId,
             @QueryParam("label") String label){
         URI edgeId = edgeUriFromShortId(edgeShortId);
         Edge edge = userGraph.edgeWithUri(
             edgeId
         );
         edge.label(label);
         return Response.ok().build();
     }
 
     private URI edgeUriFromShortId(String shortId){
         return  new UserUris(
                 userGraph.user()
         ).edgeUriFromShortId(
                 shortId
         );
     }
 }
