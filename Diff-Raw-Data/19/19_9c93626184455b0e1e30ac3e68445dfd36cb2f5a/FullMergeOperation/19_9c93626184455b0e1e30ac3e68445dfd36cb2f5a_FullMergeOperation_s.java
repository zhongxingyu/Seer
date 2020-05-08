 
 package it.wolfed.operation;
 
 import com.mxgraph.model.mxCell;
 import it.wolfed.manipulation.GraphManipulation;
 import it.wolfed.model.ArcEdge;
 import it.wolfed.model.InterfaceVertex;
 import it.wolfed.model.PetriNetGraph;
 import it.wolfed.model.PlaceVertex;
 import it.wolfed.model.TransitionVertex;
 import it.wolfed.model.Vertex;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  */
 public class FullMergeOperation extends Operation
 {
     protected List<PetriNetGraph> inputGraphs;
     
     /**
      * Clone all the cells from inputGraphs into operationGraph.
      * 
      * @param operationGraph
      * @param inputGraphs
      * @throws Exception  
      */
     public FullMergeOperation(PetriNetGraph operationGraph, PetriNetGraph... inputGraphs) throws Exception
     {
         super(operationGraph);
         this.inputGraphs = Arrays.asList(inputGraphs);
         execute();
     }
     
     @Override
     void process()
     {
         mxCell clone = null;
         Object parent = operationGraph.getDefaultParent();
         
         Map<Object, List<mxCell>> interfacesFound = new HashMap<>();
         
         for (int i = 0; i < inputGraphs.size(); i++)
         {
             PetriNetGraph net = inputGraphs.get(i);
 
             for (Object cellObj : net.getChildCells(net.getDefaultParent()))
             {
                 mxCell cell = (mxCell) cellObj;
 
                 if(cell instanceof PlaceVertex)
                 {
                     PlaceVertex place = (PlaceVertex) cell;
                     clone = new PlaceVertex(parent, getPrefix(i + 1) + cell.getId(), cell.getValue(), 0, 0);
                     ((PlaceVertex)clone).setTokens(place.getTokens());
                 }
                 else if(cell instanceof TransitionVertex)
                 {
                     clone = new TransitionVertex(parent, getPrefix(i + 1) + cell.getId(), cell.getValue(), 0, 0);
                 }
                 else if(cell instanceof InterfaceVertex)
                 {
                     clone = new InterfaceVertex(parent, getPrefix(i + 1) + cell.getId(), cell.getValue());
                     
                     if(interfacesFound.containsKey(clone.getValue()))
                     {
                         interfacesFound.get(clone.getValue()).add(clone);
                     }
                     else
                     {
                         List<mxCell> list = new ArrayList<>();
                         list.add(clone);
                         interfacesFound.put(clone.getValue(), list);
                     }
                 }
                 // @todo check instanceof Arc when the mouserelease creation of arcs will be type-zed
                 // ArcEdge e mxCell.isEdge()
                 else if(cell.isEdge())
                 {
                     // Get
                     Vertex source = operationGraph.getVertexById(getPrefix(i + 1) + cell.getSource().getId());
                     Vertex target = operationGraph.getVertexById(getPrefix(i + 1) + cell.getTarget().getId());
 
                     // Clone
                     clone = new ArcEdge(parent, getPrefix(i + 1) + cell.getId(), cell.getValue(), source, target);
                 }
 
                 operationGraph.addCell(clone);
             }
         }
         
        
        
         // Merge Interfaces
         if(interfacesFound.size() > 0)
         {
             for(List<mxCell> interfList : interfacesFound.values())
             {
                 Vertex firstInterface = (Vertex) interfList.get(0);
 
                 for(int i = 1; i < interfList.size(); i++)
                 {

                     Vertex currentInterf =(Vertex) interfList.get(i);
                     GraphManipulation.cloneEdges(operationGraph, currentInterf, firstInterface);
                     GraphManipulation.removeVertexAndHisEdges(operationGraph, currentInterf);
                 }
 
                 if(operationGraph.getIncomingEdges(firstInterface).length > 0 
                         && operationGraph.getOutgoingEdges(firstInterface).length > 0 )
                 {
                     PlaceVertex placeMirror = new PlaceVertex(
                             operationGraph.getDefaultParent(),
                             firstInterface.getId(),
                             firstInterface.getValue(),
                             firstInterface.getGeometry().getX(),
                             firstInterface.getGeometry().getY()
                     );
 
                     GraphManipulation.cloneEdges(operationGraph, firstInterface, placeMirror);
                     GraphManipulation.removeVertexAndHisEdges(operationGraph, firstInterface);
                     operationGraph.addCell(placeMirror);
                 }
             }
         }
     }
 }
