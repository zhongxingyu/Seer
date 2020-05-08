 
 package it.wolfed.model;
 
 import com.mxgraph.analysis.mxDistanceCostFunction;
 import com.mxgraph.analysis.mxGraphAnalysis;
 import com.mxgraph.model.mxCell;
 import com.mxgraph.view.mxGraph;
import it.wolfed.util.Constants;
import it.wolfed.util.IterableNodeList;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import org.w3c.dom.Node;
 
 public final class PetriNetGraph extends mxGraph
 {
     private String id;
     private String type;       
     private int indexPlaces;
     private int indexTransitions;
     private int indexArcs;
     private int indexInterfaces;
 
     public PetriNetGraph(String id)
     {
         this.id = id;
         
         setCellsResizable(false);
         setAllowDanglingEdges(false);
         setAllowLoops(false);
         setDropEnabled(false);
         setMultigraph(false);
     }
     
     @Override
     public String toString()
     {
         return id;
     }
     
     public static PetriNetGraph factory(Node dom, String defaultId)
     {
         // Xml Mapping 1:1
         String id = dom.getAttributes().getNamedItem(Constants.PNML_ID).getTextContent().trim();
         String type = dom.getAttributes().getNamedItem(Constants.PNML_TYPE).getTextContent().trim();
         
         // Fix Woped "noID" behaviour
         if(id.isEmpty() || id.equals("noID"))
         {
             id = defaultId;
         }
         
         PetriNetGraph net = new PetriNetGraph(id);
         net.setType(type);
                 
         // Pmnl Import
         net.getModel().beginUpdate();
         Object parent = net.getDefaultParent();
 
         try
         {
             for (final Node elementNode : new IterableNodeList(dom.getChildNodes())) 
             {
                 if (elementNode.getNodeType() == Node.ELEMENT_NODE) 
                 {
                     switch (elementNode.getNodeName())
                     {
                        case Constants.PNML_PLACE:
                             net.addCell(PlaceVertex.factory(parent, elementNode));
                             net.nextIndexPlaces();
                             break;
 
                         case Constants.PNML_TRANSITION:
                             net.addCell(TransitionVertex.factory(parent, elementNode));
                             net.nextIndexTransition();
                             break;
 
                         case Constants.PNML_ARC:
                             ArcEdge arc = ArcEdge.factory(parent, elementNode);
                             Vertex source = net.getVertexById(arc.getSourceId());
                             Vertex target = net.getVertexById(arc.getTargetId());
                             net.addEdge(arc, parent, source, target, null);
                             break;
                     }
                 }
             }
         }
         finally
         {
             net.getModel().endUpdate();
         }
         
         return net;
     }
 
     public String getId()
     {
         return id;
     }
     
     public String getType()
     {
         return type;
     }
 
     public void setType(String type)
     {
         this.type = type;
     }
 
     public String nextIndexPlaces()
     {
         return "p" + String.valueOf(++indexPlaces);
     }
 
     public String nextIndexTransition()
     {
         return "t" + String.valueOf(++indexTransitions);
     }
     
     public String nextIndexArcs()
     {
         return "a" + String.valueOf(++indexArcs);
     }
     
     public String nextIndexInterfaces()
     {
         return "i" + String.valueOf(++indexInterfaces);
     }
 
     @Override
     public boolean isCellSelectable(Object cell) 
     {
          if (cell != null) 
          {
             if (cell instanceof mxCell) 
             {
                mxCell myCell = (mxCell) cell;
                if (myCell.isEdge())
                 {
                     return false;
                 }
             }
          }
          
          return super.isCellSelectable(cell);
     }
     
     public boolean isSingleInitialPlace()
     {
         return (getInitialPlaces().size() == 1);
     }
     
     public boolean isSingleFinalPlace()
     {
         return (getFinalPlaces().size() == 1);
     }
     
     public boolean isWorkflowStronglyConnected()
     {
         if(isSingleInitialPlace() == false || isSingleFinalPlace() == false)
         {            
             return false;
         }
         
         PlaceVertex finalplace = getFinalPlaces().iterator().next();
         HashSet<Vertex> notConnectedVertex = getNotConnectedVertices(finalplace);
 
         return notConnectedVertex.isEmpty();
     }
     
     public boolean isWorkFlow()
     {
         //return isSingleInitialPlace() && isSingleFinalPlace() && isWorkflowStronglyConnected();
         return isWorkflowStronglyConnected();
     }
     
     public HashSet<Vertex> getNotConnectedVertices(Vertex target)
     {
         HashSet<Vertex> notConnectedVertices = new HashSet<>();
         
         for (Object cell : getChildVertices())
         {
             // Cammino esistente fino alla final place
             if(cell != target)
             {
                 Object[] paths = mxGraphAnalysis
                     .getInstance()
                     .getShortestPath(
                         this,
                         cell,
                         target,
                         new mxDistanceCostFunction(),
                         getChildVertices(getDefaultParent()).length,
                         true
                  );
 
                 // path not exists
                 if(paths.length == 0)
                 {
                     notConnectedVertices.add((Vertex) cell);
                 }
             }
             
             // Caso limite in cui una transizione è "Initial"
             // Può essere possibile creando le tran "a mano"
             if(cell instanceof TransitionVertex 
                     && getIncomingEdges(cell).length == 0)
             {
                 notConnectedVertices.add((Vertex) cell);
             }
         }
         
         return notConnectedVertices;
     }
 
     public Vertex getVertexById(String id)
     {
         for (Object objVertex : getChildVertices())
         {
             if (objVertex instanceof Vertex)
             {
                 Vertex vertex = (Vertex) objVertex;
 
                 if (vertex.getId().equals(id))
                 {
                     return vertex;
                 }
             }
         }
         
         return null;
     }
     
     public List<PlaceVertex> getInitialPlaces()
     {
         List<PlaceVertex> initialPlaces = new ArrayList<>();
         for (Object vertexObj : getChildVertices())
         {   
             if(vertexObj instanceof PlaceVertex)
             {
                 if(getIncomingEdges(vertexObj).length == 0
                     && getOutgoingEdges(vertexObj).length > 0)
                 {
                     initialPlaces.add((PlaceVertex) vertexObj);
                 }
             }
         }
         
         return initialPlaces;
     }
     
     public List<PlaceVertex> getFinalPlaces()
     {
         List<PlaceVertex> finalPlaces = new ArrayList<>();
         for (Object vertexObj : getChildVertices())
         {   
             if(vertexObj instanceof PlaceVertex)
             {
                 if(getOutgoingEdges(vertexObj).length == 0
                     && getIncomingEdges(vertexObj).length > 0)
                 {
                     finalPlaces.add((PlaceVertex) vertexObj);
                 }
             }    
         }
 
         return finalPlaces;
     }
     
     /*
      * getChildCells Wrapper with default parent
      */
     public Object[] getChildCells()
     {
         return getChildCells(getDefaultParent());
     }
     
     /*
      * getChildVertices Wrapper with default parent
      */
     public Object[] getChildVertices()
     {
         return getChildVertices(getDefaultParent());
     }
     
     /*
      * getChildEdges Wrapper with default parent
      */
     public Object[] getChildEdges()
     {
         return getChildEdges(getDefaultParent());
     }
     
     /**
      * Aggiunge una nuova piazza
      * 
      * @param id
      * @param value
      * @return 
      */
     public PlaceVertex insertPlace(String id)
     {
         if(id == null)
         {
            id = nextIndexPlaces();
         }
         
         PlaceVertex place = new PlaceVertex(getDefaultParent(), id, id);
         return (PlaceVertex) addCell(place);
 
     }
     
     /**
      * Aggiunge una nuova transizione
      * 
      * @param id
      * @param value
      * @return 
      */
     public TransitionVertex insertTransition(String id)
     {
         if(id == null)
         {
             id = nextIndexTransition();
         }
         
         TransitionVertex transition = new TransitionVertex(getDefaultParent(), id, id);
         return (TransitionVertex) addCell(transition);
     }
     
     /**
      * Aggiunge un nuovo Arco
      * 
      * @param id
      * @param value
      * @return 
      */
     public ArcEdge insertArc(String id, Vertex source, Vertex target)
     {
         if(id == null)
         {
             id = nextIndexArcs();
         }
         
         ArcEdge edge = new ArcEdge(getDefaultParent(), id, "");
         edge.setSourceId(source.getId());
         edge.setTargetId(target.getId());
         
         return (ArcEdge) addEdge(edge, getDefaultParent(), source, target, null);
     }
     
     /**
      * Prints out some useful information about the cell in the tooltip.
      */
     @Override
     public String getToolTipForCell(Object cell)
     {
         String tip = "";	
         mxCell mxcell = (mxCell) cell;
         
         tip += mxcell.getClass();
         tip += " | ID: " + mxcell.getId();
         tip += " | VALUE: " + mxcell.getValue();
         
         if(cell instanceof Vertex)
         {
             tip += " | PRESET: " + String.format("%d", getIncomingEdges(cell).length);
             tip += " | POSTSET: " + String.format("%d", getOutgoingEdges(cell).length);
             
             if(cell instanceof PlaceVertex)
             {
                 PlaceVertex p = (PlaceVertex) cell;
                 tip += " | TOKENS: " + String.format("%d", p.getTokens());
             }
         }
 
         return tip;
     }
 }
