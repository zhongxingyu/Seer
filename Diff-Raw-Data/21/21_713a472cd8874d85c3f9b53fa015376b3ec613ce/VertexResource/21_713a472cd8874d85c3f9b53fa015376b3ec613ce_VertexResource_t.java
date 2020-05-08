 package org.triple_brain.service.resources.vertex;
 
 import com.google.inject.assistedinject.Assisted;
 import com.google.inject.assistedinject.AssistedInject;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 import org.triple_brain.module.model.UserUris;
 import org.triple_brain.module.model.graph.Edge;
 import org.triple_brain.module.model.graph.GraphTransactional;
 import org.triple_brain.module.model.graph.UserGraph;
 import org.triple_brain.module.model.graph.Vertex;
 import org.triple_brain.module.model.json.graph.EdgeJson;
 import org.triple_brain.module.model.json.graph.VertexJson;
 import org.triple_brain.module.search.GraphIndexer;
 import org.triple_brain.service.resources.GraphElementIdentificationResource;
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import java.net.URI;
 
 import static org.triple_brain.module.model.json.StatementJsonFields.*;
 
 /**
  * Copyright Mozilla Public License 1.1
  */
 @Produces(MediaType.APPLICATION_JSON)
 @Consumes(MediaType.APPLICATION_JSON)
 public class VertexResource {
 
     @Inject
     GraphIndexer graphIndexer;
 
     @Inject
     VertexSuggestionResourceFactory vertexSuggestionResourceFactory;
 
     @Inject
     VertexPublicAccessResourceFactory vertexPublicAccessResourceFactory;
 
     @Inject
     GraphElementIdentificationResourceFactory graphElementIdentificationResourceFactory;
 
     @Inject
     VertexSurroundGraphResourceFactory vertexSurroundGraphResourceFactory;
 
     @Inject
     VertexImageResourceFactory vertexImageResourceFactory;
 
     private UserGraph userGraph;
 
     @AssistedInject
     public VertexResource(
             @Assisted UserGraph userGraph
     ) {
         this.userGraph = userGraph;
     }
 
     @GET
     @GraphTransactional
     @Path("/{shortId}")
     public Response getVertex(@PathParam("shortId") String shortId){
         return Response.ok().entity(
                VertexJson.toJson(
                        vertexFromShortId(shortId)
                )
         ).build();
     }
 
     @POST
     @GraphTransactional
     @Path("/")
     public Response createVertex() {
         Vertex newVertex = userGraph.createVertex();
         return Response.ok()
                 .entity(VertexJson.toJson(newVertex))
                 .build();
     }
 
     @POST
     @GraphTransactional
     @Path("/{sourceVertexShortId}")
     public Response addVertexAndEdgeToSourceVertex(
             @Context HttpServletRequest request
     ) {
         Vertex sourceVertex = userGraph.vertexWithUri(URI.create(
                 request.getRequestURI()
         ));
         Edge createdEdge = sourceVertex.addVertexAndRelation();
         Vertex createdVertex = createdEdge.destinationVertex();
         graphIndexer.indexVertex(
                 createdVertex
         );
         JSONObject jsonCreatedStatement = new JSONObject();
         try {
             jsonCreatedStatement.put(
                     SOURCE_VERTEX, VertexJson.toJson(sourceVertex)
             );
             jsonCreatedStatement.put(
                     EDGE, EdgeJson.toJson(createdEdge)
             );
             jsonCreatedStatement.put(
                     END_VERTEX, VertexJson.toJson(createdVertex)
             );
         } catch (JSONException e) {
             throw new RuntimeException(e);
         }
         //TODO response should be of type created
         return Response.ok(jsonCreatedStatement).build();
     }
 
     @DELETE
     @GraphTransactional
     @Produces(MediaType.TEXT_PLAIN)
     @Path("/{vertexShortId}")
     public Response removeVertex(
             @Context HttpServletRequest request
     ) {
         Vertex vertex = userGraph.vertexWithUri(URI.create(
                 request.getRequestURI()
         ));
        graphIndexer.deleteGraphElement(
                vertex
         );
         for(Edge edge : vertex.connectedEdges()){
            graphIndexer.deleteGraphElement(
                    edge
             );
         }
         vertex.remove();
         return Response.ok().build();
     }
 
     @POST
     @GraphTransactional
     @Path("{shortId}/label")
     @Produces(MediaType.TEXT_PLAIN)
     @Consumes(MediaType.TEXT_PLAIN)
     public Response updateVertexLabel(
             @PathParam("shortId") String shortId,
             @QueryParam("label") String label
     ) {
         URI vertexId = uriFromShortId(shortId);
         Vertex vertex = userGraph.vertexWithUri(
                 vertexId
         );
         vertex.label(label);
 
         graphIndexer.indexVertex(
                 vertex
         );
         return Response.ok().build();
     }
 
     @POST
     @GraphTransactional
     @Path("{shortId}/comment")
     @Produces(MediaType.TEXT_PLAIN)
     @Consumes(MediaType.TEXT_PLAIN)
     public Response updateVertexComments(
             @PathParam("shortId") String shortId,
             String comment
     ) {
         URI vertexId = uriFromShortId(shortId);
         Vertex vertex = userGraph.vertexWithUri(
                 vertexId
         );
         vertex.comment(comment);
         graphIndexer.indexVertex(vertex);
         return Response.ok().build();
     }
 
 
     @Path("{shortId}/image")
     @GraphTransactional
     public VertexImageResource image(
             @PathParam("shortId") String shortId
     ) {
         return vertexImageResourceFactory.ofVertex(
                 vertexFromShortId(shortId)
         );
     }
 
     @Path("{shortId}/surround_graph/{depthOfSubVertices}")
     @GraphTransactional
     public VertexSurroundGraphResource getVertexSurroundGraphResource(
             @PathParam("shortId") String shortId,
             @PathParam("depthOfSubVertices") Integer depth
     ) {
         return vertexSurroundGraphResourceFactory.ofUserGraphCenterVertexAndDepth(
                 userGraph,
                 vertexFromShortId(shortId),
                 depth
         );
     }
 
     @Path("{shortId}/identification")
     @GraphTransactional
     public GraphElementIdentificationResource getVertexIdentificationResource(@PathParam("shortId") String
                                                                                       shortId) {
         return graphElementIdentificationResourceFactory.forGraphElement(
                 vertexFromShortId(shortId),
                 true
         );
     }
 
     @Path("{shortId}/suggestions")
     @GraphTransactional
     public VertexSuggestionResource getVertexSuggestionResource(
             @PathParam("shortId") String shortId
     ) {
         return vertexSuggestionResourceFactory.ofVertex(
                 vertexFromShortId(shortId)
         );
     }
 
     @Path("{shortId}/public_access")
     @GraphTransactional
     public VertexPublicAccessResource getPublicAccessResource(
             @PathParam("shortId") String shortId
     ) {
         return vertexPublicAccessResourceFactory.ofVertex(
                 vertexFromShortId(shortId)
         );
     }
 
     private URI uriFromShortId(String shortId) {
         return new UserUris(
                 userGraph.user()
         ).vertexUriFromShortId(
                 shortId
         );
     }
 
     private Vertex vertexFromShortId(String shortId) {
         URI vertexId = uriFromShortId(shortId);
         return userGraph.vertexWithUri(
                 vertexId
         );
     }
 }
