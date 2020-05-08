 package lu.sven.epcmodeler.mouse.actions;
 
 import java.awt.event.ActionEvent;
 
 import javax.swing.AbstractAction;
 
 import lu.sven.epcmodeler.EPCModeler;
 import lu.sven.epcmodeler.graph.Node;
 import lu.sven.epcmodeler.graph.NodeType;
 
 public class ChangeNodeType extends AbstractAction {
 	private static final long serialVersionUID = 764855208327279821L;
 	private Node n;
 	private NodeType t;
 	
 	public ChangeNodeType(Node _n, NodeType _t) {
 		this.n = _n;
 		this.t = _t;
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
		this.n.setNodeType(this.t);	
 		EPCModeler.pushToPeers(n.toGML(), "/updateNode");
 	}
 }
 
