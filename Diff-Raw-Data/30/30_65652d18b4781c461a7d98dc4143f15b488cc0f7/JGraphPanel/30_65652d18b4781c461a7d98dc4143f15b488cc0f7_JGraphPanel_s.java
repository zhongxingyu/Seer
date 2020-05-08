 package petrinet.gui;
 
 import javax.swing.JPanel;
 
 import com.mxgraph.layout.mxOrganicLayout;
 import com.mxgraph.swing.mxGraphComponent;
 import com.mxgraph.view.mxGraph;
 @SuppressWarnings("serial")
 
 /**
  * Creates an MXGraph which can then be used by a layout mechanism in the calling class
  */
 public class JGraphPanel extends JPanel {
	
	final int PLACE_WIDTH = 80;
	final int PLACE_HEIGHT = 30;
	final int TRANSITION_WIDTH = 80;
	final int TRANSITION_HEIGHT = 30;
	final String PLACE_STYLE = "strokeColor=black;fillColor=gray";
	final String TRANSITION_STYLE = "ROUNDED;strokeColor=green;fillColor=orange";
	final String CAN_FIRE_STYLE = "ROUNDED;strokeColor=green;fillColor=green";
	
 	public mxGraph graph;
 	public mxGraphComponent graphComponent;
 	      
 	private final Object defaultParent;
 	
 	public JGraphPanel(mxGraph graph) {
 		this.graph = graph;
 	    defaultParent = graph.getDefaultParent();
 	    
 	    DrawGraph();
 	}
 	
 	/**
 	 * Draws the graph from the petrinet
 	 */
 	private void DrawGraph() {
 	    graphComponent = new mxGraphComponent(graph);
 	    
 	    mxOrganicLayout organic = new mxOrganicLayout(graph);
 	    organic.execute(defaultParent);
 	}
 	
 	public void RefreshGraph() {
 		graphComponent.refresh();
 	}
 }
