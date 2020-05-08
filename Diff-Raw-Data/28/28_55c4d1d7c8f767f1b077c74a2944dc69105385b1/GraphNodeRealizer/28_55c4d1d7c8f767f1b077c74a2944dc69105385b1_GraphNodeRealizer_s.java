 import java.awt.Color;
 
 import y.view.GenericNodeRealizer;
 import y.view.NodeLabel;
 import y.view.NodeRealizer;
 
 
 public class GraphNodeRealizer extends GenericNodeRealizer {
 	private boolean debug = false;
 	private String nodetext = "";
 	private String prefix = "";
 	private String suffix = "";
 	private String nodetypetext = "";
 	private int reference = 0;
 	private int identifier = 0;
 	private boolean neighbour_selected = false;
 	private boolean reference_selected = false;
 	private boolean highlight = false;
 	private boolean optimization_queue = false;
 	private boolean different = false;
 	private NodeType type = NodeType.CONTROL;
 
	private static final Color diffColor = new Color(0xC6, 0xB3, 0xFF);
 	private static final Color highlightColor = new Color(0xCC, 0xFF, 0xCC);
 	private static final Color optimizationColor = Color.orange;
 	private static final Color baseColor = new Color(0xbb, 0xbf, 0xff);
 	private static final Color dataColor = new Color(0xff, 0xe9, 0xe9);
 	
 	private NodeLabel label;
 
 	public static enum NodeType { CONTROL, DATA, TYPE, TYPEVAR };
 	private static final Color[] NodeColors = { baseColor, dataColor };
 	
 	
 	public GraphNodeRealizer () {
 		label = createNodeLabel();
 		label.setFontSize(15);
 		setLabel(label);
 	}
 	
   	public NodeRealizer createCopy(NodeRealizer r) {
 	    return new GraphNodeRealizer();
 	}
 	  
 	private void updateText () {
 		if (debug) {
 			String ref = "";
 			if (reference != 0)
 				ref = " [" + Integer.toString(reference) + "]";
 			label.setText(Integer.toString(identifier) + ": " +  prefix + " " + nodetext + " " + suffix + ref);
 		} else
 			label.setText(prefix + " " + nodetext + " " + suffix);
 		setWidth(label.getWidth() + 2 * 20);
 		repaint();
 	}
 	
 	public void updateColor () {
 		if (! isSelected()) {
 			Color nc = null;
 			if (different)
 				nc = diffColor;
 			else if (highlight)
 				nc = highlightColor;
 			else if (optimization_queue)
 				nc = optimizationColor;
 			else 
 				nc = NodeColors[type.ordinal() % 2];
 			if (neighbour_selected | reference_selected)
 				nc = nc.darker().darker();
 			setFillColor(nc);
 			repaint();
 		}
 	}
 	
 	public String getComparableText () {
 		return prefix + nodetext + suffix;
 	}
 	
 	public String getSliderText () {
 		return suffix + " " + nodetext;
 	}
 	
 	public void setNodeText (String s) {
 		nodetext = s;
 		updateText();
 	}
 	
 	public void setNodeTypeText (String s) {
 		nodetypetext = s;
 	}
 	
 	public void setReference (int r) {
 		reference = r;
 		updateText();
 	}
 	
 	public void setIdentifier (int i) {
 		identifier = i;
 		updateText();
 	}
 	
 	public void setPrefix (String p) {
 		prefix = p;
 		updateText();
 	}
 	
 	public void setSuffix (String s) {
 		suffix = s;
 		updateText();
 	}
 	
 	public void setNodeType (NodeType t) {
 		type = t;
 		updateColor();
 	}
 	
 	public void setDifferent (boolean b) {
 		if (different != b) {
 			different = b;
 			updateColor();
 		}
 	}
 
 	public void setNeighbourSelected(boolean b) {
 		if (neighbour_selected != b) {
 			neighbour_selected = b;
 			updateColor();
 		}
 	}
 
 	public void setReferenceSelected(boolean b) {
 		if (reference_selected != b) {
 			reference_selected = b;
 			updateColor();
 		}
 	}
 
 	public void setHighlight(boolean b) {
 		if (highlight != b) {
 			highlight = b;
 			updateColor();
 		}
 	}
 
 	public void setOptimizationQueue(boolean b) {
 		if (optimization_queue != b) {
 			optimization_queue = b;
 			updateColor();
 		}
 	}
 	
 	public void setDebug (boolean b) {
 		if (debug != b) {
 			debug = b;
 			updateText();
 		}
 	}
 }
