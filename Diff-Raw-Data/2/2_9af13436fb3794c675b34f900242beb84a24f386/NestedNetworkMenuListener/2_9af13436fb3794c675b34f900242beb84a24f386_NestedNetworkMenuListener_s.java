 /*
  File: NestedNetworkMenuListener.java
 
  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
 
  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
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
 package cytoscape.visual.ui;
 
 import ding.view.NodeContextMenuListener;
 import giny.view.NodeView;
 import java.awt.event.ActionEvent;
 import javax.swing.AbstractAction;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import cytoscape.CyNetwork;
 import cytoscape.view.CyNetworkView;
 import cytoscape.Cytoscape;
 import cytoscape.dialogs.SetNestedNetworkDialog;
 import javax.swing.JOptionPane;
 
 /**
  * NestedNetworkMenuListener implements NodeContextMenuListener
  * When a node is selected it calls NestedNetwork and add
  */
 class NestedNetworkMenuListener implements NodeContextMenuListener {
 	NestedNetworkMenuListener() {}
 
 	/**
 	 * @param nodeView The clicked NodeView
 	 * @param menu popup menu to add the Bypass menu
 	 */
 	public void addNodeContextMenuItems(NodeView nodeView, JPopupMenu menu) {		
 		if (menu == null){
 			menu = new JPopupMenu();		
 		}
 		JMenu jm = new JMenu("Nested network");
 				
 		final JMenuItem jm1 = new JCheckBoxMenuItem(new SetNestedNetworkMenuItemAction(nodeView));
 		final JMenuItem jm2 = new JCheckBoxMenuItem(new DeleteNestedNetworkMenuItemAction(nodeView));
 		final JMenuItem jm3 = new JCheckBoxMenuItem(new GotoNestedNetworkMenuItemAction(nodeView));
 
 		if (nodeView.getNode().getNestedNetwork() == null){
 			jm2.setEnabled(false);
 			jm3.setEnabled(false);
 		}
 
 		jm.add(jm1);
 		jm.add(jm2);
 		jm.add(jm3);
 		
 		menu.add(jm);
 	}
 	
 
 	//
 	class SetNestedNetworkMenuItemAction extends AbstractAction {
 		NodeView nodeView;
 		public SetNestedNetworkMenuItemAction(NodeView nodeView){
 			super("Set Nested Network");
 			this.nodeView = nodeView;
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			SetNestedNetworkDialog dlg = new SetNestedNetworkDialog(Cytoscape.getDesktop(), true, this.nodeView);
 					//"Set Nested Network for " + nodeView.getNode().getIdentifier());
 			dlg.setLocationRelativeTo(Cytoscape.getDesktop());
 			dlg.setVisible(true);
 		}
 	}
 
 	//
 	class DeleteNestedNetworkMenuItemAction extends AbstractAction {
 		NodeView nodeView;
 		public DeleteNestedNetworkMenuItemAction(NodeView nodeView){
 			super("Delete Nested Network");
 			this.nodeView = nodeView;
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			if (this.nodeView.getNode().getNestedNetwork() == null){
 				return;
 			}
 			int user_says = JOptionPane.showConfirmDialog(Cytoscape.getDesktop(), 
 					"Are you sure you want to delete this nested network?","Confirm Delete Nested Network", JOptionPane.YES_NO_OPTION);
 			if (user_says == JOptionPane.NO_OPTION){
 				return;
 			}
 				
 			this.nodeView.getNode().setNestedNetwork(null);			
 		}
 	}
 
 	class GotoNestedNetworkMenuItemAction extends AbstractAction {
 		NodeView nodeView;
 		public GotoNestedNetworkMenuItemAction(NodeView nodeView){
 			super("Go to Nested Network");
 			this.nodeView = nodeView;
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			if (this.nodeView.getNode().getNestedNetwork() == null){
 				return;
 			}
 		
 			CyNetwork nestedNetwork = (CyNetwork) this.nodeView.getNode().getNestedNetwork();
 			
 			CyNetworkView theView = Cytoscape.getNetworkView(nestedNetwork.getIdentifier());
			if (theView == null){
 				theView = Cytoscape.createNetworkView(nestedNetwork);
 			}
 
 			Cytoscape.getDesktop().setFocus(nestedNetwork.getIdentifier());
 		}
 	}
 }
