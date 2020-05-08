 package org.triple_brain.graphmanipulator.jena.graph;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.Resource;
 import org.triple_brain.module.graph_manipulator.VertexManipulator;
 import org.triple_brain.module.graph_manipulator.exceptions.NonExistingResourceException;
 import org.triple_brain.module.model.User;
 import org.triple_brain.module.model.graph.Edge;
 import org.triple_brain.module.model.graph.Vertex;
 
 import java.util.UUID;
 
 import static com.hp.hpl.jena.vocabulary.OWL2.sameAs;
 import static com.hp.hpl.jena.vocabulary.RDF.type;
 import static org.triple_brain.graphmanipulator.jena.JenaConnection.modelMaker;
 import static org.triple_brain.graphmanipulator.jena.TripleBrainModel.SITE_URI;
 /**
  * Copyright Mozilla Public License 1.1
  */
 public class JenaVertexManipulator implements VertexManipulator{
 
     private Model userModel;
     private User user;
     private JenaGraphElementManipulator jenaGraphElementManipulator;
     public static JenaVertexManipulator withUser(User user){
         return new JenaVertexManipulator(user);
     }
 
     protected JenaVertexManipulator(User user){
         this.user = user;
         userModel = modelMaker().openModel(user.mindMapURIFromSiteURI(SITE_URI));
         jenaGraphElementManipulator = JenaGraphElementManipulator.withUserModel(userModel);
     }
 
     public Edge addVertexAndRelation(String sourceVertexURI) throws NonExistingResourceException {
         Resource subjectResource = graph().getResource(sourceVertexURI);
         if (!graph().containsResource(subjectResource)) {
             throw new NonExistingResourceException(sourceVertexURI);
         }
         Vertex sourceVertex = JenaVertex.loadUsingResource(subjectResource);
         String newVertexURI = user.URIFromSiteURI(SITE_URI) + UUID.randomUUID().toString();
         JenaVertex newVertex = JenaVertex.createUsingModelAndURI(userModel, newVertexURI);
 
         String edgeURI = user.URIFromSiteURI(SITE_URI) + UUID.randomUUID().toString();
         JenaEdge edge = JenaEdge.withModelURIAndDestinationVertex(
                 userModel,
                 edgeURI,
                 newVertex
         );
 
         sourceVertex.addOutgoingEdge(edge);
 
         newVertex.addNeighbor(sourceVertex);
         sourceVertex.addNeighbor(newVertex);
 
         return edge;
     }
 
     public Vertex createDefaultVertex(){
         String newVertexURI = user.URIFromSiteURI(SITE_URI) + "default";
         return JenaVertex.createUsingModelAndURI(userModel, newVertexURI);
     }
 
     public Vertex defaultVertex(){
         return JenaVertex.loadUsingResource(
                 userModel.getResource(user.URIFromSiteURI(SITE_URI) + "default")
         );
     }
 
     public JenaVertexManipulator removeVertex(String vertexURI) {
         Resource vertexResource = userModel.getResource(vertexURI);
         if (!graph().containsResource(vertexResource)) {
             throw new NonExistingResourceException(vertexURI);
         }
         JenaVertex vertex = JenaVertex.loadUsingResource(vertexResource);
         JenaEdgeManipulator jenaEdgeManipulator = JenaEdgeManipulator.withUserAndItsModel(
             user,
             userModel
         );
         for(Edge edge : vertex.connectedEdges()){
             jenaEdgeManipulator.removeEdge(edge.id());
         }
         vertexResource.removeProperties();
         return this;
     }
 
     public JenaVertexManipulator updateLabel(String vertexURI, String newLabel) throws NonExistingResourceException{
         jenaGraphElementManipulator.updateLabel(vertexURI, newLabel);
         return this;
     }
 
     public JenaVertexManipulator semanticType(String vertexURI, String typeUri){
         Resource vertex = userModel.getResource(vertexURI);
         if (!graph().containsResource(vertex)) {
             throw new NonExistingResourceException(vertexURI);
         }
         Resource typeAsResource = graph().createResource(typeUri);
         vertex.addProperty(type, typeAsResource);
         return this;
     }
 
     public JenaVertexManipulator sameAsResourceWithUri(String vertexURI, String sameAsUri){
         Resource vertex = userModel.getResource(vertexURI);
         if (!graph().containsResource(vertex)) {
             throw new NonExistingResourceException(vertexURI);
         }
         Resource sameAsResource = graph().createResource(sameAsUri);
         vertex.addProperty(sameAs, sameAsResource);
         return this;
     }
 
     public Model graph(){
         return userModel;
     }
 }
