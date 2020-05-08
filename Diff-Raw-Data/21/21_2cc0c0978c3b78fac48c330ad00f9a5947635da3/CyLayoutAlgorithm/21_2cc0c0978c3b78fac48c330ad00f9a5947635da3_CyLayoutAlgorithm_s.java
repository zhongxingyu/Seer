 /* vim: set ts=2:
   File: CyLayoutAlgorithm.java
 
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
 package cytoscape.layout;
 
 import cytoscape.task.TaskMonitor;
 
 import cytoscape.util.*;
 
 import cytoscape.view.CyNetworkView;
 
 import giny.view.NodeView;
 
 import java.util.List;
 
 import javax.swing.JPanel;
 
 
 /**
  *
   */
 public interface CyLayoutAlgorithm {
 	/**
 	 * This method performs the layout on the current network using the
 	 * current network view.
 	 *
 	 */
 	public void doLayout();
 
 	/**
 	 * This method performs the layout on the current network.
 	 *
 	 * @param networkView the CyNetworkView on which to perform the layout
 	 */
 	public void doLayout(CyNetworkView networkView);
 
 	/**
 	 * This method performs the layout on the current network, but assumes
 	 * that the layout is part of an existing monitored task
 	 *
 	 * @param networkView the CyNetworkView on which to perform the layout
 	 * @param monitor the task monitor to use
 	 */
 	public void doLayout(CyNetworkView networkView, TaskMonitor monitor);
 
 	/**
 	 * Tests to see if this layout supports doing a layout on a subset of the
 	 * nodes in this network view.
 	 *
 	 * @return true if layout supports layouts on a subset of the nodes
 	 */
 	public boolean supportsSelectedOnly();
 
 	/**
 	 * Sets the "selectedOnly" flag
 	 *
 	 * @param selectedOnly boolean value that tells the layout algorithm whether to
 	 * only layout the selected nodes
 	 */
 	public void setSelectedOnly(boolean selectedOnly);
 
 	/**
 	 * Tests to see if this layout supports doing a layout based on node attributes.
 	 *
 	 * @return byte array of allowable attribute types or "null" if not supported.  If the
 	 *              first type is "-1", all types are supported
 	 */
 	public byte[] supportsNodeAttributes();
 
 	/**
 	 * Tests to see if this layout supports doing a layout based on edge attributes.
 	 *
 	 * @return type array of allowable attribute types or "null" if not supported.  If the
 	 *              first type is "-1", all types are supported
 	 */
 	public byte[] supportsEdgeAttributes();
 
 	/**
 	 * Sets the attribute to use for node- or edge- based attribute layouts
 	 *
 	 * @param attributeName String with the name of the attribute to use
 	 */
 	public void setLayoutAttribute(String attributeName);
 
 	/**
 	 * This returns a (possibly empty) List of Strings that is used for
 	 * the attribute list in the menu for attribute-based layouts.  This
 	 * allows layout algorithms to provide "special" attributes.  For example,
 	 * a force directed layout might want to set the list to ["(unweighted)"]
 	 * to allow the user to perform an unweighted layout.  Note that this value
 	 * will be set using setLayoutAttribute() just like other attributes, so the
 	 * layout algorithm will need to check for it.
 	 *
 	 * @return List of Strings
 	 */
 	public List<String> getInitialAttributeList();
 
 	/**
 	 * This method should return a JPanel that implements the UI to set
 	 * the tuneable parameters for the algorithm.
 	 *
 	 * @return JPanel that will be used in the LayoutSettingsDialog.  If
 	 *                this algorithm does not support a UI for settings,
 	 *                it should return null
 	 */
 	public JPanel getSettingsPanel();
 
 	/**
 	 * This method is used to ask the algorithm to revert its settings
 	 * to some previous state.  It is called from the settings dialog
 	 * when the user presses the "Cancel" button.
 	 *
 	 * NOTE: AbstractLayout implements this on behalf of all its subclasses
 	 * by using Java Preferences.
 	 */
 	public void revertSettings();
 
 	/**
 	 * This method is used to ask the algorithm to get its settings
 	 * from the settings dialog.  It is called from the settings dialog
 	 * when the user presses the "Done" or the "Execute" buttons.
 	 *
 	 * NOTE: AbstractLayout implements this on behalf of all its subclasses
 	 * by using Java Preferences.
 	 */
 	public void updateSettings();
 
 	/**
 	 * This method is used to ask the algorithm to get all of its tunables
 	 * and return them to the caller.
 	 *
 	 * @return the layout properties for this algorithm
 	 *
 	 */
 	public LayoutProperties getSettings();
 
 	/**
 	 * Get the name of this layout.
 	 *
 	 * @return String representing the name of the layout.
 	 */
 	public String getName();
 
 	/**
 	 *  Lock this array of nodes.  Locking a node prevents the node
 	 * from being moved by layout algorithmes.
 	 *
 	 * @param nodes the array of nodes to lock
 	 */
 	public void lockNodes(NodeView[] nodes);
 
 	/**
 	 * Lock this node.  Locking a node prevents the node
 	 * from being moved by layout algorithmes.
 	 *
	 * @param node the node to lock
 	 */
 	public void lockNode(NodeView v);
 
 	/**
 	 * Unlock this node.  Unlocking a node allows the node
 	 * to be moved by a layout algorithmes.
 	 *
 	 * @param v the node to unlock
 	 */
 	public void unlockNode(NodeView v);
 
 	/**
 	 * Unlock all of the nodes.  Unlocking a node allows the node
 	 * to be moved by a layout algorithmes.
 	 *
 	 */
 	public void unlockAllNodes();
 
 	/**
 	 * Can be used to stop the layout from running. 
 	 */
 	public void halt();
 }
