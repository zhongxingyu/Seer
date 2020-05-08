 
 package it.wolfed.model;
 
 import com.mxgraph.analysis.mxDistanceCostFunction;
 import com.mxgraph.analysis.mxGraphAnalysis;
 import com.mxgraph.model.mxCell;
 import com.mxgraph.util.mxEvent;
 import com.mxgraph.util.mxEventObject;
 import com.mxgraph.util.mxEventSource;
 import com.mxgraph.view.mxGraph;
 import it.wolfed.swing.GraphComponent;
 import it.wolfed.util.Constants;
 import it.wolfed.util.IterableNodeList;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 /**
  * PetriNet implementation.
  * 
  * The class extends the visual base model mxGraph (Jgraphx library).
  * 
  * mxCell > Vertex  > (PlaceVertex | TransitionVertex | InterfaceVertex)
  * mxCell > Edge    > (ArcEdge)
  * 
  * Petri graph definition:
  * 
  * A Petri graph is a directed bipartite graph, in which the nodes represent 
  * transitions (i.e. events that may occur, signified by bars) and places 
  * (i.e. conditions, signified by circles). 
  * 
  * A Petri graph consists of places, transitions, arcsNodes and interfacesFound. 
  * Arcs run from a place to a transition or vice versa, never between places
  * or between transitions. 
  * 
  * The places from which an arc runs to a transition are called the input places
  * of the transition; the places to which arcsNodes run from a transition are called
  * the output places of the transition.
  * 
  * @see <a href="http://en.wikipedia.org/wiki/Petri_net">Wikipedia PetriNet</a>
  * @see <a href="https://github.com/jgraph/jgraphx">JGrapx Repository</a>
  */
 public class PetriNetGraph extends mxGraph
 {
     /**
      * Id of the graph.
      * 
      * Usually id from pnmlXML file or tab name.
      */
     private String id;
     
     /**
      * Type for the graph.
      */
     private String type;  
     
     /**
      * Index of the places of the graph.
      * 
      * Must be in sync with any new place creation.
      */
     private int indexPlaces;
     
     /**
      * Index of the transitions of the graph.
      * 
      * Must be in sync with any new transition creation.
      */
     private int indexTransitions;
     
     /**
      * Index of the arcsNodes of the graph.
      * 
      * Must be in sync with any new arc creation.
      */
     private int indexArcs;
     
     /**
      * Index of the interfacesFound of the graph.
      * 
      * Must be in sync with any new interface creation.
      */
     private int indexInterfaces;
     
     /**
      * Holds all the initialPlaces of the graph.
      * 
      * Refreshed on every change.
      * @see PetriNetGraph#getInitialPlaces()
      */
     private List<PlaceVertex> initialPlaces;
     
     /**
      * Holds all the initialPlaces of the graph.
      * 
      * Refreshed on every change.
      * @see PetriNetGraph#getFinalPlaces()
      */
     private List<PlaceVertex> finalPlaces;
     
     /**
      * Holds all the NOT "workflow connected" vertices of the graph.
      * 
      * Refreshed on every change.
      * @see PetriNetGraph#getNotConnectedVertices(it.wolfed.model.Vertex)
      */
     private Set<Vertex> notConnectedVertices;
 
     /**
      * {@link PetriNetGraph} Constructor.
      * 
      * @param id
      */
     public PetriNetGraph(String id)
     {
         this.id = id;
         
         setCellsResizable(false);
         setAllowDanglingEdges(false);
         setAllowLoops(false);
         setDropEnabled(false);
         setMultigraph(false);
         
         // Dirty Pattern: Force refreshing of analysis on change event
         // @todo filter this only on add and remove events
         getModel().addListener(mxEvent.CHANGE, new mxEventSource.mxIEventListener()
         {
             @Override
             public void invoke(Object sender, mxEventObject evt)
             {
                 initialPlaces = null;
                 finalPlaces = null;
                 notConnectedVertices = null;
             }
         });
     }
     
     /**
      * @inheritDoc
      */
     @Override
     public String toString()
     {
         return id;
     }
     
     /**
      * Imports a pnmlXML node in a new {@link PetriNetGraph}.
      * 
      * @param dom
      * @param defaultId
      * @return PetriNetGraph
      * @see <a href="http://www.pnmlXML.org/">http://www.pnmlXML.org/</a>
      */
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
         
         // Creates new Graph
         PetriNetGraph graph = new PetriNetGraph(id);
         graph.setType(type);
                 
         // Pmnl Import
         graph.getModel().beginUpdate();
         Object parent = graph.getDefaultParent();
 
         // Holds the arcsNodes founds
         Set<Node> arcsNodes = new HashSet<>();
         
         try
         {
             for (final Node elementNode : new IterableNodeList(dom.getChildNodes())) 
             {
                 if (elementNode.getNodeType() == Node.ELEMENT_NODE) 
                 {
                     switch (elementNode.getNodeName())
                     {
                         case Constants.PNML_PLACE:
                             graph.addCell(PlaceVertex.factory(parent, elementNode));
                             graph.getSetNextPlaceId();
                             break;
 
                         case Constants.PNML_TRANSITION:
                             graph.addCell(TransitionVertex.factory(parent, elementNode));
                             graph.getSetNextTransitionId();
                             break;
 
                         case Constants.PNML_ARC:
                             arcsNodes.add(elementNode);             
                             break;
                     }
                 }
             }
             
             /**
              * Arcs should always be processed AFTER all the vertex.
              */
             for(Node arcNode : arcsNodes)
             {
                 graph.addCell(ArcEdge.factory(parent, arcNode, graph));
                 graph.getSetNextArcId();
             }
         }
         finally
         {
             graph.getModel().endUpdate();
         }
         
         return graph;
     }
 
     /**
      * Returns graph id.
      * 
      * @return String
      */
     public String getId()
     {
         return id;
     }
     
     /**
      * Returns graph type.
      * 
      * @return String
      */
     public String getType()
     {
         return type;
     }
 
     /**
      * Sets graph type.
      * 
      * @param type 
      */
     public void setType(String type)
     {
         this.type = type;
     }
 
     /**
      * Increments and returns the current places id (with prefix).
      * 
      * @return String
      */
     public String getSetNextPlaceId()
     {
         return "p" + String.valueOf(++indexPlaces);
     }
 
     /**
      * Increments and returns the current transitions index (with prefix).
      * 
      * @return String
      */
     public String getSetNextTransitionId()
     {
         return "t" + String.valueOf(++indexTransitions);
     }
     
     /**
      * Increments and returns the current interface index (with prefix).
      * 
      * @return String
      */
     public String getSetNextInterfaceId()
     {
         return "i" + String.valueOf(++indexInterfaces);
     }
     
     /**
      * Increments and returns the current arcsNodes index (with prefix).
      * 
      * @return String
      */
     public String getSetNextArcId()
     {
         return "a" + String.valueOf(++indexArcs);
     }
     
     /**
      * Returns if the graph has only an initial place.
      * 
      * @return boolean
      */
     public boolean isSingleInitialPlace()
     {
         return (getInitialPlaces().size() == 1);
     }
     
     /**
      * Returns if the graph has only a final place.
      * 
      * @return boolean
      */
     public boolean isSingleFinalPlace()
     {
         return (getFinalPlaces().size() == 1);
     }
     
     /**
      * Returns if a workflow has a path from initial to final place for each vertex.
      * 
      * @return boolean
      */
     public boolean isWorkflowStronglyConnected()
     {
         if(isSingleInitialPlace() == false || isSingleFinalPlace() == false)
         {            
             return false;
         }
         
         return getNotConnectedVertices(getFinalPlaces().get(0)).isEmpty();
     }
     
     /**
      * Returns if the graph is a workflow net.
      * 
      * Properties:
      * 
      * Single initial place
      * Single final place
      * Exist, for each vertex, a path to the final place.
      * 
      * @return boolean
      */
     public boolean isWorkFlow()
     {
         //return isSingleInitialPlace() && isSingleFinalPlace() && isWorkflowStronglyConnected();
         return isWorkflowStronglyConnected();
     }
     
     /**
      * Returns all the Places and Transition without a path to target Vertex.
      * 
      * @param target
      * @return HashSet<Vertex>
      */
     public Set<Vertex> getNotConnectedVertices(Vertex target)
     {
         if(notConnectedVertices == null)
         {
             notConnectedVertices = new HashSet<>();
             
             for (Object cell : getChildVertices())
             {
                 /**
                  * Search a path to target.
                  * 
                  * @todo Change this with a simple path check (shortestpath now)
                  */
                 if(cell != target)
                 {
                     Object[] paths = mxGraphAnalysis
                         .getInstance()
                         .getShortestPath(
                             this,
                             cell,
                             target,
                             new mxDistanceCostFunction(),
                             getChildVertices().length,
                             true
                      );
 
                     // Path not exists
                     if(paths.length == 0)
                     {
                         // Ignore Interfaces
                         if(cell instanceof TransitionVertex || cell instanceof PlaceVertex)
                         {
                             notConnectedVertices.add((Vertex) cell);
                         }
                     }
                 }
 
                 // Case with a Transition "Initial" (only postset arc)
                 if(cell instanceof TransitionVertex 
                         && getIncomingEdges(cell).length == 0)
                 {
                     notConnectedVertices.add((Vertex) cell);
                 }
             }
         }
 
         return notConnectedVertices;
     }
     
     /**
      * Get a vertex by his id.
      * 
      * @param  id 
      * @return Vertex
      */
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
     
     /**
      * Get a vertex by his value.
      * 
      * @param  value
      * @return Vertex
      */
     public Vertex getVertexByValue(Object value)
     {
         for (Object objVertex : getChildVertices())
         {
             if (objVertex instanceof Vertex)
             {
                 Vertex vertex = (Vertex) objVertex;
 
                 if (vertex.getValue().equals(value))
                 {
                     return vertex;
                 }
             }
         }
         
         return null;
     }
     
     /**
      * Get the initial places of the graph.
      * 
      * An initial place is a place with:
      * 
      * #Preset arcsNodes : 0
      * #Postset arcsNodes: > 1
      * 
      * @return List<PlaceVertex>
      */
     public List<PlaceVertex> getInitialPlaces()
     {
         if(initialPlaces == null)
         {        
             initialPlaces = new ArrayList<>();
         
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
         }
 
         return initialPlaces;
     }
     
     /**
      * Get the final places of the graph.
      * 
      * A final place is a place with:
      * 
      * #Preset arcsNodes : > 1
      * #Postset arcsNodes: 0
      * 
      * @return List<PlaceVertex>
      */
     public List<PlaceVertex> getFinalPlaces()
     {
         if(finalPlaces == null)
         {
             finalPlaces = new ArrayList<>();
             
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
         }
         
         return finalPlaces;
     }
     
     /**
      * Get child cell of the default graph parent.
      * 
      * @return Object[]
      */
     public Object[] getChildCells()
     {
         return getChildCells(getDefaultParent());
     }
     
     /**
      * Get child vertices of the default graph parent.
      * 
      * @return Object[]
      */
     public Object[] getChildVertices()
     {
         return getChildVertices(getDefaultParent());
     }
     
     /**
      * Get child edge of the default graph parent.
      * 
      * @return Object[]
      */
     public Object[] getChildEdges()
     {
         return getChildEdges(getDefaultParent());
     }
     
     /**
      * Add a new place to the graph.
      * 
      * @todo    method overload for id
      * @param   id
      * @return  PlaceVertex
      */
     public PlaceVertex insertPlace(String id)
     {
         String nextId = getSetNextPlaceId();
         
         id = (id == null) ? nextId : id;
         PlaceVertex place = new PlaceVertex(getDefaultParent(), id, id, 0, 0);
         return (PlaceVertex) addCell(place);
     }
     
     /**
      * Add a new transition to the graph.
      * 
      * @todo    method overload for id
      * @param   id
      * @return  TransitionVertex
      */
     public TransitionVertex insertTransition(String id)
     {
         String nextId = getSetNextTransitionId();
        
         id = (id == null) ? nextId : id;
         TransitionVertex transition = new TransitionVertex(getDefaultParent(), id, id, 0, 0);
         return (TransitionVertex) addCell(transition);
     }
     
     /**
      * Add a new interface to the graph.
      * 
      * @todo    method overload for id
      * @param   id
      * @return  InterfaceVertex
      */
     public InterfaceVertex insertInterface(String id)
     {
         String nextId = getSetNextInterfaceId();
        
         id = (id == null) ? nextId : id;
         InterfaceVertex interf = new InterfaceVertex(getDefaultParent(), id, id);
         return (InterfaceVertex) addCell(interf);
     }
     
     /**
      * Add a new arc to the graph.
      * 
      * @todo    method overload for id
      * @param   id
      * @param   source 
      * @param   target 
      * @return  ArcEdge
      */
     public ArcEdge insertArc(String id, Vertex source, Vertex target)
     {
         String nextId = getSetNextArcId();
        
         id = (id == null) ? nextId : id;
         ArcEdge edge = new ArcEdge(getDefaultParent(), id, "", source, target);
         return (ArcEdge) addCell(edge);
     }
     
     /**
      * Force only-vertex selectable cells.
      * 
      * @todo  try to force this in {@link GraphComponent}
      * @param cell 
      * @return boolean
      */
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
     
     /**
      * Prints out some useful information about the cell in the tooltip.
      * 
      * @todo  try to force this in {@link GraphComponent}
      * @param   cell
      * @return  String
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
 
     /**
      * Export graph to XML String.
      * 
      * @param exportType
      * @return 
      */
     public String exportPNML() throws ParserConfigurationException, TransformerConfigurationException, TransformerException 
     {
         /** <?xml version="1.0" encoding="UTF-8"?> */
         Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
         Element toolSpecific = doc.createElement(Constants.PNML_TOOL_SPECIFIC);
 
         /** <pnml> */
         Element pnml = doc.createElement(Constants.PNML_TAG);
         doc.appendChild(pnml);
 
         /**     <net type="http://www.informatik.hu-berlin.de/top/pntd/ptNetb" id="noId"> */
         Element net = doc.createElement(Constants.PNML_NET);
         net.setAttribute(Constants.PNML_TYPE, getType());
         net.setAttribute(Constants.PNML_ID, getId());
         pnml.appendChild(net);
         
         for (Object cellObj : getChildCells())
         {
             /** <place id="p1" name="p1"> ... </place> */
             if(cellObj instanceof PlaceVertex)
             {
                 PlaceVertex place = (PlaceVertex) cellObj;
                 net.appendChild(place.exportPNML(doc));
             }
             /** <transition id="t1" name="t1"> ... </transition> */
             else if(cellObj instanceof TransitionVertex)
             {
                 TransitionVertex transition = (TransitionVertex) cellObj;
                 net.appendChild(transition.exportPNML(doc));
             }
            /**  <arc id="a17" source="t3" target="p5"> ... </arc> */
            else if(cellObj instanceof mxCell)
            {
                mxCell cell = (mxCell) cellObj;
                
                if(cell.isEdge())
                {
                    net.appendChild(ArcEdge.exportPNML(doc, cell));
                }
            }
             /**  <interface id="i1" name="i1"> ... </interface> */
             else if(cellObj instanceof InterfaceVertex)
             {
                 InterfaceVertex interf = (InterfaceVertex) cellObj;
                 toolSpecific.appendChild(interf.exportPNML(doc));
                 
                 /*
                  * Interfaces are not PNML complaint.
                  * A "Place" mirror of the interface will be added to
                  * the Net node, for compatibility propuose.
                  */
                  PlaceVertex mirrorInterf = new PlaceVertex(getDefaultParent(), interf.getId(), interf.getValue(), interf.getGeometry().getX(), interf.getGeometry().getY());
                  net.appendChild(mirrorInterf.exportPNML(doc));
             }
         }
 
         if(indexInterfaces > 0)
         {
             /**     <toolspecific tool="WoLFEd" version="currentVersion">
             *            <interface id="i1" />
             *       </toolspecific>
             */
             net.appendChild(toolSpecific);
             toolSpecific.setAttribute(Constants.PNML_TOOL, Constants.EDITOR_NAME);
             toolSpecific.setAttribute(Constants.PNML_TOOL_VERSION, Constants.EDITOR_VERSION);
         }
 
         /**     </net>
         * </pnml> 
         */
         
         // Output
         doc.getDocumentElement().normalize();
         DOMSource source = new DOMSource(doc);
         StringWriter sw = new StringWriter();
         StreamResult result = new StreamResult(sw);
         Transformer transformer = TransformerFactory.newInstance().newTransformer();
         transformer.transform(source, result);
 
         return sw.toString();
     }
     
     /**
      * Export graph to DOT String.
      * 
      * @return 
      */
     public String exportDOT() 
     {
         StringBuilder dot = new StringBuilder();
         dot.append("digraph WoLFEdGraph{ \n rankdir=LR;");
      
         // Vertex
         for (Object cellObj : this.getChildVertices())
         {
             if (cellObj instanceof InterfaceVertex)
             {
                 InterfaceVertex interfaceVertex = (InterfaceVertex) cellObj;
                 dot.append(interfaceVertex.exportDOT());
             }
             if (cellObj instanceof PlaceVertex)
             {
                 PlaceVertex placeVertex = (PlaceVertex) cellObj;
                 dot.append(placeVertex.exportDOT(getIncomingEdges(placeVertex).length, getOutgoingEdges(placeVertex).length));
             }
             if (cellObj instanceof TransitionVertex)
             {
                 TransitionVertex transitionVertex = (TransitionVertex) cellObj;
                 dot.append(transitionVertex.exportDOT());
             }
         }
 
         // Edges
         for (Object edgeObj : this.getChildEdges())
         {
             mxCell edge = (mxCell) edgeObj;
             
             if (edge.isEdge())
             {
                 dot.append(ArcEdge.exportDOT(edge));
             }
         }
 
         dot.append("\n }");
         
         return dot.toString();
 
     }
 }
