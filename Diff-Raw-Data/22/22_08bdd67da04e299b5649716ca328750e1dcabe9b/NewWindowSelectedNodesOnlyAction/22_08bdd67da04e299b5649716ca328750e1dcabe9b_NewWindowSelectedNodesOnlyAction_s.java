 /*
  File: NewWindowSelectedNodesOnlyAction.java 
  
  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
  
  The Cytoscape Consortium is: 
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Pasteur Institute
  - Agilent Technologies
  
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
  
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute 
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute 
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute 
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
  
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
  */
 
 //-------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //-------------------------------------------------------------------------
 package cytoscape.actions;
 
 //-------------------------------------------------------------------------
 import giny.model.Node;
 
 import java.awt.event.ActionEvent;
 import java.util.Iterator;
 
 import cytoscape.CyNetwork;
 import cytoscape.Cytoscape;
 import cytoscape.util.CyNetworkNaming;
 import cytoscape.util.CytoscapeAction;
 import cytoscape.view.CyNetworkView;
 
 public class NewWindowSelectedNodesOnlyAction extends CytoscapeAction {
 
 	public NewWindowSelectedNodesOnlyAction() {
 		super("From selected nodes, all edges");
 		setPreferredMenu("File.New.Network");
 		setAcceleratorCombo(java.awt.event.KeyEvent.VK_N, ActionEvent.CTRL_MASK);
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		// save the vizmapper catalog
 
 		// CyNetworkView current_network_view =
 		// Cytoscape.getCurrentNetworkView();
 		// CyNetwork current_network = current_network_view.getNetwork();
 		CyNetwork current_network = Cytoscape.getCurrentNetwork();
 		CyNetworkView current_network_view = null;
 		if (Cytoscape.viewExists(current_network.getIdentifier())) {
 			current_network_view = Cytoscape.getNetworkView(current_network
 					.getIdentifier());
 		} // end of if ()
 
 		int[] nodes = current_network.getFlaggedNodeIndicesArray();
 		// int [] nodes = current_network_view.getSelectedNodeIndices();
 
 		CyNetwork new_network = Cytoscape.createNetwork(nodes, current_network
 				.getConnectingEdgeIndicesArray(nodes), CyNetworkNaming
 				.getSuggestedSubnetworkTitle(current_network), current_network);
 		new_network.setExpressionData(current_network.getExpressionData());
 
 		CyNetworkView new_view = Cytoscape.getNetworkView(new_network
 				.getIdentifier());
 		if (new_view == null) {
 			return;
 		}
 
 		if (current_network_view != null) {
 
 			Iterator i = new_network.nodesIterator();
 			while (i.hasNext()) {
 				Node node = (Node) i.next();
 				new_view.getNodeView(node).setOffset(
 						current_network_view.getNodeDoubleProperty(node
 								.getRootGraphIndex(),
 								CyNetworkView.NODE_X_POSITION),
 						current_network_view.getNodeDoubleProperty(node
 								.getRootGraphIndex(),
 								CyNetworkView.NODE_Y_POSITION));
 			}
 			
 			// Set visual style
			new_view.setVisualStyle(current_network_view.getVisualStyle().getName());
 			
 		} else {
 			new_view.setVisualStyle("default");
 		}
 
 	}
 
 }
