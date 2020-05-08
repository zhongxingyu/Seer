 package lu.sven.epcmodeler.mouse;
 
 import javax.swing.Action;
 import javax.swing.JMenuItem;
 import javax.swing.JRadioButtonMenuItem;
 
 import lu.sven.epcmodeler.graph.Edge;
 import lu.sven.epcmodeler.graph.Node;
 import lu.sven.epcmodeler.graph.NodeType;
 import lu.sven.epcmodeler.graph.NodeVisibility;
 import lu.sven.epcmodeler.mouse.actions.ChangeNodeType;
 import lu.sven.epcmodeler.mouse.actions.ChangeNodeVisibility;
 import lu.sven.epcmodeler.mouse.actions.DeleteNode;
 import edu.uci.ics.jung.visualization.VisualizationViewer;
 
 public class NodeMenuItems {
 	public static class IDDisplay extends JMenuItem implements NodeMenuListener<Node> {
 		private static final long serialVersionUID = 792389276684856523L;
 
 		@Override
 		public void setVertexAndView(Node n, VisualizationViewer<Node, Edge> vv) {
 			this.setText("ID: "+ n.getID());		
 		}
     }
 	
 	public static class setEventType extends JRadioButtonMenuItem implements NodeMenuListener<Node> {
 		private static final long serialVersionUID = -6663216929189379362L;
 
 		@Override
 		public void setVertexAndView(Node n, VisualizationViewer<Node, Edge> vv) {
 			Action changeNodeType = new ChangeNodeType(n, NodeType.EVENT);
 			//n.setLabel("event["+n.getID()+"]");
 			this.setAction(changeNodeType);
 			if (n.getNodeType() == NodeType.EVENT) this.setSelected(true);
 			this.setText("Event");
 			// BUG: Repaint seems not to work, they need to click first
			vv.getRenderContext().getScreenDevice().repaint();
 		}
 		
 	}
 	
 	public static class setFunctionType extends JRadioButtonMenuItem implements NodeMenuListener<Node> {
 		private static final long serialVersionUID = -6663216929179379362L;
 
 		@Override
 		public void setVertexAndView(Node n, VisualizationViewer<Node, Edge> vv) {
 			if (n.getNodeType() == NodeType.FUNCTION) this.setSelected(true);
 			Action changeNodeType = new ChangeNodeType(n, NodeType.FUNCTION);
 			//n.setLabel("function["+n.getID()+"]");
 			this.setAction(changeNodeType);
 			this.setText("Function");
 			// BUG: Repaint seems not to work, they need to click first
 			vv.repaint();
 		}
 		
 	}
 	
 	public static class setOpAndType extends JRadioButtonMenuItem implements NodeMenuListener<Node> {
 		private static final long serialVersionUID = -2446479210098158691L;
 
 		@Override
 		public void setVertexAndView(Node n, VisualizationViewer<Node, Edge> vv) {
 			if (n.getNodeType() == NodeType.OPERATOR_AND) this.setSelected(true);
 			Action changeNodeType = new ChangeNodeType(n, NodeType.OPERATOR_AND);
 			//n.setLabel("AND");
 			this.setAction(changeNodeType);
 			this.setText("AND operator");
 			// BUG: Repaint seems not to work, they need to click first
 			vv.repaint();
 		}
 		
 	}
 	
 	public static class setOpOrType extends JRadioButtonMenuItem implements NodeMenuListener<Node> {
 		private static final long serialVersionUID = 2147503163897807912L;
 
 		@Override
 		public void setVertexAndView(Node n, VisualizationViewer<Node, Edge> vv) {
 			if (n.getNodeType() == NodeType.OPERATOR_OR) this.setSelected(true);
 			Action changeNodeType = new ChangeNodeType(n, NodeType.OPERATOR_OR);
 			//n.setLabel("OR");
 			this.setAction(changeNodeType);
 			this.setText("OR operator");
 			// BUG: Repaint seems not to work, they need to click first
 			vv.repaint();
 		}
 		
 	}
 	
 	public static class setOpXorType extends JRadioButtonMenuItem implements NodeMenuListener<Node> {
 		private static final long serialVersionUID = 580601600832452745L;
 
 		@Override
 		public void setVertexAndView(Node n, VisualizationViewer<Node, Edge> vv) {
 			if (n.getNodeType() == NodeType.OPERATOR_XOR) this.setSelected(true);
 			Action changeNodeType = new ChangeNodeType(n, NodeType.OPERATOR_XOR);
 			//n.setLabel("XOR");
 			this.setAction(changeNodeType);
 			this.setText("XOR operator");
 			// BUG: Repaint seems not to work, they need to click first
 			vv.repaint();
 		}
 		
 	}
 	
 	public static class setVisible extends JRadioButtonMenuItem implements NodeMenuListener<Node> {
 		private static final long serialVersionUID = -4898314533316530813L;
 
 		@Override
 		public void setVertexAndView(Node n, VisualizationViewer<Node, Edge> vv) {
 			if (n.getState() == NodeVisibility.VISIBLE) this.setSelected(true);
 			Action changeNodeVisibility = new ChangeNodeVisibility(n, NodeVisibility.VISIBLE);
 			this.setAction(changeNodeVisibility);
 			this.setText("Visible");
 			// BUG: Repaint seems not to work, they need to click first
 			vv.repaint();
 		}
 		
 	}
 	
 	public static class setInvisible extends JRadioButtonMenuItem implements NodeMenuListener<Node> {
 		private static final long serialVersionUID = 1103659091454861376L;
 
 		@Override
 		public void setVertexAndView(Node n, VisualizationViewer<Node, Edge> vv) {
 			if (n.getState() == NodeVisibility.INVISIBLE) this.setSelected(true);
 			Action changeNodeVisibility = new ChangeNodeVisibility(n, NodeVisibility.INVISIBLE);
 			this.setAction(changeNodeVisibility);
 			this.setText("Invisible");
 			// BUG: Repaint seems not to work, they need to click first
 			vv.repaint();
 		}
 		
 	}
 	
 	public static class deleteNode extends JRadioButtonMenuItem implements NodeMenuListener<Node> {
 		private static final long serialVersionUID = -957795618296736912L;
 
 		@Override
 		public void setVertexAndView(Node n, VisualizationViewer<Node, Edge> vv) {
 			Action deleteNode = new DeleteNode(n, vv);
 			this.setAction(deleteNode);
 			this.setText("Delete Node");
 			
 			// BUG: Repaint seems not to work, they need to click first
 			vv.repaint();
 		}
 		
 	}
 }
