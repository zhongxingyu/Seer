 /* vim :set ts=2: */
 /*
   File: CyGroupViewer.java
 
   Copyright (c) 2007, The Cytoscape Consortium (www.cytoscape.org)
 
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
 package cytoscape.groups;
 
 import cytoscape.CyNode;
 import cytoscape.view.CyNetworkView;
 
 /**
  * The CyGroupViewer interface provides a mechanism for group view implementations
  * to register themselves and get notified of group creation and deletion.  The
  * primary goals of this mechanism are to allow multiple group viewers to maintain
  * different groups at the same time, and to provide a mechanism to retain view
  * mechanism across session save and restore.
  */
 public interface CyGroupViewer {
 	/**
 	 * The change values
 	 */
 	public static enum ChangeType { NODE_ADDED, NODE_REMOVED, STATE_CHANGED }
 
 	/**
 	 * Provide the string name of this viewer.  This will be used to reassociate
 	 * this viewer with its groups upon session restoration.
 	 *
 	 * @return String name of the viewer
 	 */
 	public String getViewerName();
 
 	/**
 	 * Provide viewer-specific initialization after the creation of a group.
 	 * This method will be called when a group is created from outside of the
 	 * viewer mechanism.  For example, when a session is restored.  This allows
 	 * the appropriate viewer to "take ownership" of the group, setting appropriate
 	 * node attributes, or even adding the group node to the view, if appropriate.
 	 * Note that this is called *after* the group creation has happened.
 	 *
 	 * @param group the CyGroup that was just created.
 	 */
 	public void groupCreated(CyGroup group);
 	public void groupCreated(CyGroup group, CyNetworkView myView);
 
 	/**
 	 * Provide viewer-specific hooks to deletion of a group.  Most of the time
 	 * group deletion would happen completely within the context of the viewer,
 	 * but there might be occaisions where alternative mechanisms would be provided.
 	 * Note that this is called *before* the group deletion actually occurs.
 	 *
 	 * @param group the CyGroup that will be deleted.
 	 */
 	public void groupWillBeRemoved(CyGroup group);
 
 	/**
 	 * Provide viewer-specific hooks to the change of a group.  At this point,
 	 * group change will be either the addition or deletion of a node, but
 	 * it could be imagined in a future implementation where we might want to 
 	 * notify viewers of changes in group edges, state, or viewObject information.
 	 *
 	 * @param group the CyGroup that will be deleted.
	 * @param CyNode the node that triggered the change
 	 * @param change the change that was made (see CyGroup defines)
 	 */
 	public void groupChanged(CyGroup group, CyNode changedNode, ChangeType change);
 }
