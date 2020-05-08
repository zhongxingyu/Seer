 package it.wolfed.event;
 
 import com.mxgraph.util.mxEventObject;
 import com.mxgraph.util.mxEventSource.mxIEventListener;
 import it.wolfed.model.InterfaceVertex;
 import it.wolfed.model.PetriNetGraph;
 import it.wolfed.model.PlaceVertex;
 import it.wolfed.model.TransitionVertex;
 import it.wolfed.model.Vertex;
 import it.wolfed.util.Constants;
import java.util.HashSet;
import java.util.List;
 
 public class AutoUpdateStyleListener implements mxIEventListener
 {
     private PetriNetGraph graph;
     
     public AutoUpdateStyleListener(PetriNetGraph graph)
     {
         this.graph = graph;
     }
     
     @Override
     public void invoke(Object o, mxEventObject eo)
     {
         updateEdgesStyle();
         updateVerticesStyle();
         graph.refresh();
     }
     
     private void updateEdgesStyle()
     {
         // Reset Arc Style
         for(Object edge: graph.getChildEdges())
         {
             if(graph.isWorkFlow())
             {
                 graph.getModel().setStyle(edge, Constants.STYLE_ARC_FLOW_CONNECTED);
             }
             else
             {
                 graph.getModel().setStyle(edge, Constants.STYLE_ARC);
             }
         }
         
         // Highlight not strongly connected arc
         if( ! graph.isWorkFlow() && graph.isSingleFinalPlace())
         {
             for(Vertex vertex : graph.getNotConnectedVertices(graph.getFinalPlaces().get(0)))
             {
                 for(Object edge : graph.getIncomingEdges(vertex))
                 {
                     graph.getModel().setStyle(edge, Constants.STYLE_ARC_FLOW_UNCONNECTED);
                 }
                 
                 // Caso limite di "Transition Initial"
                 if(vertex instanceof TransitionVertex &&
                         graph.getIncomingEdges(vertex).length == 0)
                 {
                     for(Object edge : graph.getOutgoingEdges(vertex))
                     {
                         graph.getModel().setStyle(edge, Constants.STYLE_ARC_FLOW_UNCONNECTED);
                     }
                 }
             }
         }
     }
 
     private void updateVerticesStyle()
     {
         String newStyle;
 
         for (Object objVertex : graph.getChildVertices())
         {
             if(objVertex instanceof PlaceVertex)
             {
                 PlaceVertex place = (PlaceVertex) objVertex;
 
                 if(graph.getInitialPlaces().contains(place))
                 {
                     newStyle = (graph.getInitialPlaces().size() > 1)
                             ? Constants.STYLE_PLACE_SPECIAL_INVALID
                             : Constants.STYLE_PLACE_SPECIAL_VALID;
                 }
                 else if(graph.getFinalPlaces().contains(place))
                 {
                     newStyle = (graph.getFinalPlaces().size() > 1)
                             ? Constants.STYLE_PLACE_SPECIAL_INVALID
                             : Constants.STYLE_PLACE_SPECIAL_VALID;
                 }
                 else if(graph.isWorkFlow())
                 {
                     newStyle = Constants.STYLE_PLACE_VALID;
                 }
                 else
                 {
                     newStyle = Constants.STYLE_PLACE;
                 }
                 
                 graph.getModel().setStyle(place, newStyle);
             }
             else if(objVertex instanceof TransitionVertex)
             {
                 if(graph.isWorkFlow())
                 {
                     newStyle = Constants.STYLE_TRANSITION_VALID;
                 }
                 else
                 {
                     newStyle = Constants.STYLE_TRANSITION;
                 }
                 
                 graph.getModel().setStyle(objVertex, newStyle);
             }
             else if(objVertex instanceof InterfaceVertex)
             {
                 for(Object edgeObj : graph.getEdges(objVertex))
                 {
                     graph.getModel().setStyle(edgeObj, Constants.STYLE_ARC_WITH_INTERFACE);
                 }
             }
         }
     }
 }
