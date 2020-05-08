 package it.wolfed.event;
 
 import com.mxgraph.util.mxEventObject;
 import com.mxgraph.util.mxEventSource.mxIEventListener;
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
     
     private PetriNetGraph getGraph()
     {
         return this.graph;
     }
     
     @Override
     public void invoke(Object o, mxEventObject eo)
     {
         updateArcsStyle();
         updatePlacesStyle();
         getGraph().refresh();
     }
     
     private void updateArcsStyle()
     {
         boolean graphIsWorkFlow = getGraph().isWorkFlow();
         
         // Reset Arc Style
        for(Object edge: getGraph().getChildEdges(getGraph().getDefaultParent()))
         {
             if(graphIsWorkFlow)
             {
                 getGraph().getModel().setStyle(edge, Constants.STYLE_ARC_FLOW_CONNECTED);
             }
             else
             {
                 getGraph().getModel().setStyle(edge, Constants.STYLE_ARC);
             }
         }
         
         // Highlight not strongly connected arc
         if( ! graphIsWorkFlow && getGraph().isSingleFinalPlace())
         {
             PlaceVertex finalplace = getGraph().getFinalPlaces().iterator().next();
             HashSet<Vertex> notConnectedVertices = getGraph().getNotConnectedVertices(finalplace);
             
             for(Vertex vertex : notConnectedVertices)
             {
                 for(Object edge : getGraph().getIncomingEdges(vertex))
                 {
                     getGraph().getModel().setStyle(edge, Constants.STYLE_ARC_FLOW_UNCONNECTED);
                 }
                 
                 // Caso limite di "Transition Initial"
                 if(vertex instanceof TransitionVertex &&
                         getGraph().getIncomingEdges(vertex).length == 0)
                 {
                     for(Object edge : getGraph().getOutgoingEdges(vertex))
                     {
                         getGraph().getModel().setStyle(edge, Constants.STYLE_ARC_FLOW_UNCONNECTED);
                     }
                 }
             }
         }
     }
 
     private void updatePlacesStyle()
     {
         String newStyle;
         List<PlaceVertex> initialPlaces = getGraph().getInitialPlaces();
         List<PlaceVertex> finalPlaces = getGraph().getFinalPlaces();
         boolean graphIsWorkFlow = getGraph().isWorkFlow();
 
         for (Object objVertex : getGraph().getChildVertices(getGraph().getDefaultParent()))
         {
             if(objVertex instanceof PlaceVertex)
             {
                 PlaceVertex place = (PlaceVertex) objVertex;
 
                 if(initialPlaces.contains(place))
                 {
                     newStyle = (initialPlaces.size() > 1)
                             ? Constants.STYLE_PLACE_SPECIAL_INVALID
                             : Constants.STYLE_PLACE_SPECIAL_VALID;
                 }
                 else if(finalPlaces.contains(place))
                 {
                     newStyle = (finalPlaces.size() > 1)
                             ? Constants.STYLE_PLACE_SPECIAL_INVALID
                             : Constants.STYLE_PLACE_SPECIAL_VALID;
                 }
                 else if(graphIsWorkFlow)
                 {
                     newStyle = Constants.STYLE_PLACE_VALID;
                 }
                 else
                 {
                     newStyle = Constants.STYLE_PLACE;
                 }
                 
                 getGraph().getModel().setStyle(place, newStyle);
             }
             
             if(objVertex instanceof TransitionVertex)
             {
                 if(graphIsWorkFlow)
                 {
                     newStyle = Constants.STYLE_TRANSITION_VALID;
                 }
                 else
                 {
                     newStyle = Constants.STYLE_TRANSITION;
                 }
                 
                 getGraph().getModel().setStyle(objVertex, newStyle);
             }
         }
     }
 }
