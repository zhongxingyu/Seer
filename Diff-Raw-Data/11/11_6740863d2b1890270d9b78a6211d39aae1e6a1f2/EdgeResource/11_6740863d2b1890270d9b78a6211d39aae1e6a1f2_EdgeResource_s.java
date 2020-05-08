 package org.triple_brain.service.resources;
 
 import com.google.inject.assistedinject.Assisted;
 import com.google.inject.assistedinject.AssistedInject;
 import org.triple_brain.module.common_utils.Uris;
 import org.triple_brain.module.model.UserUris;
 import org.triple_brain.module.model.graph.Edge;
 import org.triple_brain.module.model.graph.GraphFactory;
 import org.triple_brain.module.model.graph.UserGraph;
 import org.triple_brain.module.model.graph.Vertex;
 import org.triple_brain.module.search.GraphIndexer;
 import org.triple_brain.service.resources.vertex.GraphElementIdentificationResourceFactory;
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
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
 
     @Inject
     GraphIndexer graphIndexer;
 
     @Inject
     GraphElementIdentificationResourceFactory graphElementIdentificationResourceFactory;
 
     private UserGraph userGraph;
 
     @AssistedInject
     public EdgeResource(
         @Assisted UserGraph userGraph
     ){
         this.userGraph = userGraph;
     }
 
     @POST
     @Produces(MediaType.TEXT_PLAIN)
     @Path("/")
     public Response addRelation(
             @QueryParam("sourceVertexId") String sourceVertexId,
             @QueryParam("destinationVertexId") String destinationVertexId
         ){
         try{
             sourceVertexId = decodeURL(sourceVertexId);
             destinationVertexId = decodeURL(destinationVertexId);
         }catch (UnsupportedEncodingException e){
             return Response.status(Response.Status.BAD_REQUEST).build();
         }
         Vertex sourceVertex = userGraph.vertexWithUri(URI.create(
                 sourceVertexId
         ));
         Vertex destinationVertex = userGraph.vertexWithUri(URI.create(
                 destinationVertexId
         ));
         Edge createdEdge = sourceVertex.addRelationToVertex(destinationVertex);
         return Response.created(URI.create(
                 UserUris.edgeShortId(createdEdge.uri())
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
         Vertex sourceVertex = edge.sourceVertex();
         Vertex destinationVertex = edge.destinationVertex();
         graphIndexer.deleteGraphElementOfUser(
                 edge,
                 userGraph.user()
         );
         edge.remove();
         graphIndexer.indexVertex(
                 sourceVertex
         );
         graphIndexer.indexVertex(
                 destinationVertex
         );
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
         graphIndexer.indexVertex(
                 edge.sourceVertex()
         );
         graphIndexer.indexVertex(
                 edge.destinationVertex()
         );
         graphIndexer.indexRelationOfUser(
                 edge,
                 userGraph.user()
         );
         return Response.ok().build();
     }
 
     @Path("{shortId}/identification")
     public GraphElementIdentificationResource getVertexIdentificationResource(@PathParam("shortId") String shortId){
         return graphElementIdentificationResourceFactory.forGraphElement(
                edgeFromShortId(shortId)
         );
     }
 
     private Edge edgeFromShortId(String shortId){
         return userGraph.edgeWithUri(
                 edgeUriFromShortId(
                         shortId
                 )
         );
     }
 
     private URI edgeUriFromShortId(String shortId){
         return  new UserUris(
                 userGraph.user()
         ).edgeUriFromShortId(
                 shortId
         );
     }
 }
