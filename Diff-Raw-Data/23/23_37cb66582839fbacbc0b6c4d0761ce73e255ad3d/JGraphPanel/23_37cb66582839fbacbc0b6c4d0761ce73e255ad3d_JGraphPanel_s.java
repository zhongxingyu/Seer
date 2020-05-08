 package petrinet.gui;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.JPanel;
 
 import petrinet.logic.ArcEndType.EndType;
 
 import petrinet.logic.Arc;
 import petrinet.logic.Petrinet;
 import petrinet.logic.Place;
 import petrinet.logic.Transition;
 
 import com.mxgraph.layout.mxOrganicLayout;
 import com.mxgraph.swing.mxGraphComponent;
 import com.mxgraph.view.mxGraph;
 
 /**
  * First examples of JGraphX - Creating a simple frame that
  * contains a graph component with two vertices and an edge connecting them.
  * @author vainolo
  */
 public class JGraphPanel  extends JPanel {
  
   private static final long serialVersionUID = 196831535599934813L;
  
   public mxGraph graph;
   
   public mxGraphComponent graphComponent;
   
   final int PLACE_WIDTH = 80;
   final int PLACE_HEIGHT = 30;
   final int TRANSITION_WIDTH = 80;
   final int TRANSITION_HEIGHT = 30;
   final String PLACE_STYLE = "strokeColor=red;fillColor=green";
   final String TRANSITION_STYLE = "ROUNDED;strokeColor=green;fillColor=orange";
   
  private ArrayList<Arc> arcs = new ArrayList<Arc>();
  
   // A way to store places and transitions that are retrievable by name
   // There are 2 Maps since a transition and place may have the same name, perhaps.
   private Map<String, Object> placeVertices = new HashMap<String, Object>();
   private Map<String, Object> transVertices = new HashMap<String, Object>();
     
   public JGraphPanel(Petrinet pn) {
 	graph = new mxGraph();
 	
     Object defaultParent = graph.getDefaultParent();
     graph.getModel().beginUpdate();
 //    Object v1 = graph.insertVertex(defaultParent, null, "Hello", 0, 0, 80, 30);
 //    Object v2 = graph.insertVertex(defaultParent, null, "World", 0, 0, 80, 30);
 //    graph.insertEdge(defaultParent, null, "Edge", v1, v2);
 
    // These are just used for testing to see if i can create an edge by referencing
    // previously created vertices.
    //Object tempFrom = null;
    //Object tempTo = null;
    
     // For each place in the petrinet, add a vertex
     for (Place p : pn.getPlaces()) {
     	Object vertex = graph.insertVertex(defaultParent, null, p.getName(), 0, 0, 
     			PLACE_WIDTH, PLACE_HEIGHT, PLACE_STYLE);
     	placeVertices.put(p.getName(), vertex);
    	//tempFrom = vertex;
     }
     
     // For each transition in the petrinet, add a vertex
     for (Transition t : pn.getTransitions()) {
     	Object vertex = graph.insertVertex(defaultParent, null, t.getName(), 0, 0, 
     			TRANSITION_WIDTH, TRANSITION_HEIGHT, TRANSITION_STYLE);
     	transVertices.put(t.getName(), vertex);
    	//tempTo = vertex;
     }
    
    //graph.insertEdge(defaultParent, null, "Temp Edge", tempFrom, tempTo);
    
     // For each arc draw the edge to represent it
    for (Arc a : arcs) {
     	// Create objects to represent the 'from' and 'to' nodes
     	Object from = null;
     	Object to = null;
     	
     	// Retrieve the appropriate vertex for the from-node of the arc
     	if (a.from == EndType.PLACE) {
     		from = placeVertices.get(a.getPlace().getName());
     	}
     	else if (a.from == EndType.TRANSITION) {
     		from = transVertices.get(a.getTransition().getName());
     	}
     		
     	// Retrieve the appropriate vertex for the to-node of the arc
     	if (a.to == EndType.PLACE) {
     		to = placeVertices.get(a.getPlace().getName());
     	}
     	else if (a.to == EndType.TRANSITION) {
     		to = transVertices.get(a.getTransition().getName());
     	}
     	
     	graph.insertEdge(defaultParent, null, "Edge", from, to);
    	
    	System.out.println("Tried to make an arc from " + from.toString() + " to " + to.toString());
     }
     
     // For each arc, find the place it is going to and from, then make edges.
     
     graph.getModel().endUpdate();
     graphComponent = new mxGraphComponent(graph);
   }
 }
