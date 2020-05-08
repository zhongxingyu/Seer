 package org.triple_brain.module.graphviz_visualisation;
 
 import org.codehaus.jettison.json.JSONObject;
 import org.triple_brain.module.graphviz_visualisation.draw.DrawConstants;
 import org.triple_brain.module.model.graph.Edge;
 import org.triple_brain.module.model.graph.SubGraph;
 import org.triple_brain.module.model.graph.Vertex;
 import org.triple_brain.module.model.graph.VertexInSubGraph;
 import org.triple_brain.module.model.json.graph.EdgeJsonFields;
 import org.triple_brain.module.model.json.graph.VertexJsonFields;
 
 import java.util.Set;
 
 /**
  * Copyright Mozilla Public License 1.1
  */
 public class GraphToDrawnGraphConverter {
 
     private SubGraph graph;
 
     public static GraphToDrawnGraphConverter withGraph(SubGraph graph){
         return new GraphToDrawnGraphConverter(graph);
     }
 
     protected  GraphToDrawnGraphConverter(SubGraph graph){
         this.graph = graph;
     }
 
     public JSONObject convert(){
         String notDrawnGraphVizFormat = convertGraphToNotDrawnGraphViz();
         String graphVizDrawnGraph = new GraphvizService()
                 .convertToDotDrawnGraph(notDrawnGraphVizFormat);
         return GraphVizToJsonGraph.withOriginalGraphAndGraphvizString(
                 graph, graphVizDrawnGraph
                 ).convert();
     }
 
     private String convertGraphToNotDrawnGraphViz() {
         String notDrawnGraphVizDot = "digraph G {node [fontsize=\"" +
                 DrawConstants.VERTEX_FONT_SIZE +
                 "\",fontname=\"" +
                 DrawConstants.VERTEX_FONT_FAMILY +
                 "\"]; edge [fontsize=\"" +
                 DrawConstants.EDGE_FONT_SIZE +
                 "\",fontname=\"" +
                 DrawConstants.EDGE_FONT_FAMILY +
                "\"]; K=0.90 mode=\"ipsep\"; sep=\"+35\"; ";
         notDrawnGraphVizDot += verticesRepresentationInDot(graph.vertices());
         notDrawnGraphVizDot += edgesRepresentationInDot(graph.edges());
         notDrawnGraphVizDot += "}";
         return notDrawnGraphVizDot;
     }
 
     private String verticesRepresentationInDot(Set<VertexInSubGraph> vertices){
         String verticesInGraphVizDot = "";
         for(Vertex vertex : vertices){
             verticesInGraphVizDot += vertexRepresentationInDot(vertex);
         }
         return verticesInGraphVizDot;
     }
 
     private String edgesRepresentationInDot(Set<Edge> edges){
         String edgesInGraphVizDot = "";
         for(Edge edge : edges){
             edgesInGraphVizDot += edgeRepresentationInDot(edge);
         }
         return edgesInGraphVizDot;
     }
 
     private String vertexRepresentationInDot(Vertex vertex){
         return "\"" + vertex.id() + "\" [" + VertexJsonFields.ID + "=\"" + vertex.id() + "\" " + VertexJsonFields.LABEL + "=\"" + (vertex.label().trim().equals("") ? Vertex.EMPTY_LABEL : vertex.label()) + "\"]";
 
     }
 
     private String edgeRepresentationInDot(Edge edge){
         return "\"" + edge.sourceVertex().id() + "\"->\"" + edge.destinationVertex().id() + "\"["+ EdgeJsonFields.ID+"=\"" + edge.id() + "\" "+ EdgeJsonFields.LABEL+"=\"" + (edge.label().trim().equals("") ? Edge.EMPTY_LABEL : edge.label()) + "\"];";
 
     }
 }
