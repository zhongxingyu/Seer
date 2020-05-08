 /*
  * Copyright (C) 2008
  *
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  *
  * The interactive user interfaces in modified source and object code versions
  * of this program must display Appropriate Legal Notices, as required under
  * Section 5 of the GNU Affero General Public License version 3.
  *
  * In accordance with Section 7(b) of the GNU Affero General Public License
  * version 3, these Appropriate Legal Notices must retain the display of the
  * "Derived from Travian world" logo. If the display of the logo is not
  * reasonably feasible for technical reasons, the Appropriate Legal Notices must
  * display the words "Derived from Travian world".
  */
 
 package ste.travian.gui;
 
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.tree.*;
 
 import java.awt.*;
 import java.awt.datatransfer.*;
 import java.awt.dnd.*;
 import java.awt.event.*;
 import java.io.*;
 
 public class AllianceGroupsTree extends JTree
         implements TreeSelectionListener, DragGestureListener, DropTargetListener, DragSourceListener {
 
     /** Stores the selected node info */
     protected TreePath selectedTreePath = null;
     protected DefaultMutableTreeNode selectedNode = null;
     
     /** Variables needed for DnD */
     private DragSource dragSource = null;
 
     /** Constructor
      * @param root The root node of the tree
 
      */
     public AllianceGroupsTree() {
         super();
 
         setEditable(true);
         getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
         setShowsRootHandles(true);
        setPreferredSize(new Dimension(200, 300));
         
         setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
 
         addTreeSelectionListener(this);
 
         dragSource = DragSource.getDefaultDragSource();
 
         DragGestureRecognizer dgr =
             dragSource.createDefaultDragGestureRecognizer(
                 this, DnDConstants.ACTION_MOVE, this
             );
 
         //
         // Eliminates right mouse clicks as valid actions
         //
         dgr.setSourceActions(dgr.getSourceActions() & ~InputEvent.BUTTON3_MASK);
 
         DropTarget dropTarget = new DropTarget(this, this);
     }
 
     /** Returns The selected node */
     public DefaultMutableTreeNode getSelectedNode() {
         return selectedNode;
     }
 
     // ----------------------------------------------------- DragGestureListener
 
     /** DragGestureListener interface method */
     public void dragGestureRecognized(DragGestureEvent e) {
         //Get the selected node
         DefaultMutableTreeNode dragNode = getSelectedNode();
         if (dragNode != null) {
             Object o = dragNode.getUserObject();
 
             if (!(o instanceof Transferable)) {
                 return;
             }
 
             Transferable transferable = (Transferable)o;
 
             //Select the appropriate cursor;
             Cursor cursor = DragSource.DefaultMoveNoDrop;
 
             //
             // begin the drag
             //
             dragSource.startDrag(e, cursor, transferable, this);
         }
     }
 
     // ------------------------------------------------------ DragSourceListener
 
     /** DragSourceListener interface method */
     public void dragDropEnd(DragSourceDropEvent e) {
         //
         // we need to delete the node from the old parent...
         //
         if (e.getDropSuccess()) {
             DefaultMutableTreeNode oldParent =
                     (DefaultMutableTreeNode) getSelectedNode().getParent();
 
             oldParent.remove(getSelectedNode());
 
             ((DefaultTreeModel)getModel()).reload(oldParent);
         }
     }
 
     /** DragSourceListener interface method */
     public void dragEnter(DragSourceDragEvent e) {
     }
 
     /** DragSourceListener interface method */
     public void dragOver(DragSourceDragEvent e) {
     }
 
     /** DragSourceListener interface method */
     public void dropActionChanged(DragSourceDragEvent e) {
     }
 
     /** DragSourceListener interface method */
     public void dragExit(DragSourceEvent e) {
     }
 
     // ------------------------------------------------------ DropTargetListener
 
     /** 
      * DropTargetListener interface method - What we do when drag is released
      */
     public void drop(DropTargetDropEvent e) {
         try {
             Transferable tr = e.getTransferable();
 
             //flavor not supported, reject drop
             if (!tr.isDataFlavorSupported(AllianceDnDInfo.INFO_FLAVOR)) {
                 e.rejectDrop();
                 return;
             }
 
             DefaultTreeModel model = (DefaultTreeModel) getModel();
 
             //cast into appropriate data type
             AllianceDnDInfo info =
                 (AllianceDnDInfo)tr.getTransferData(AllianceDnDInfo.INFO_FLAVOR);
 
             //get new parent node
             Point loc = e.getLocation();
             TreePath destinationPath = getPathForLocation(loc.x, loc.y);
 
             final String msg = testDropTarget(destinationPath, selectedTreePath);
             if (msg != null) {
                 e.rejectDrop();
 
                 return;
             }
 
             DefaultMutableTreeNode newParent =
                 (DefaultMutableTreeNode) destinationPath.getLastPathComponent();
 
             DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(info);
             newChild.setAllowsChildren(false);
 
             //
             // We insert the node in its alphabetical position
             //
             int i = 0;
             String node = null;
             for (; i<newParent.getChildCount(); ++i) {
                 node = String.valueOf(newParent.getChildAt(i));
                 if (node.compareTo(info.getName()) > 0) {
                     break;
                 }
             }
             newParent.insert(newChild, i);
             
             e.getDropTargetContext().dropComplete(true);
 
             //expand nodes appropriately - this probably isnt the best way...
             
             model.reload(newParent);
             TreePath parentPath = new TreePath(newParent.getPath());
             expandPath(parentPath);
         } catch (IOException io) {
             e.rejectDrop();
         } catch (UnsupportedFlavorException ufe) {
             e.rejectDrop();
         }
     } //end of method
 
     /** DropTargetListener interface method */
     public void dragEnter(DropTargetDragEvent e) {
     }
 
     /** DropTargetListener interface method */
     public void dragExit(DropTargetEvent e) {
     }
 
     /** DropTargetListener interface method */
     public void dragOver(DropTargetDragEvent e) {
         //
         //set cursor location. Needed in setCursor method
         //
         Point cursorLocationBis = e.getLocation();
         TreePath destinationPath =
                 getPathForLocation(cursorLocationBis.x, cursorLocationBis.y);
 
         //
         // if destination path is okay accept drop...
         //
         if (testDropTarget(destinationPath, selectedTreePath) == null) {
             e.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
         } else {
             //
             // ...otherwise reject drop
             //
             e.rejectDrag();
         }
     }
 
     /** DropTargetListener interface method */
     public void dropActionChanged(DropTargetDragEvent e) {
     }
 
     // --------------------------------------------------- TreeSelectionListener
 
     /** TreeSelectionListener - sets selected node */
     public void valueChanged(TreeSelectionEvent evt) {
         selectedTreePath = evt.getNewLeadSelectionPath();
         if (selectedTreePath == null) {
             selectedNode = null;
             return;
         }
         selectedNode = (DefaultMutableTreeNode)selectedTreePath.getLastPathComponent();
     }
 
     /** 
      * Convenience method to test whether drop location is valid
      * 
      * @param destination The destination path
      * @param dropper The path for the node to be dropped
      *
      * @return null if no problems, otherwise an explanation
      */
     private String testDropTarget(TreePath destination, TreePath dropper) {
          boolean destinationPathIsNull = destination == null;
         if (destinationPathIsNull) {
             return "Invalid drop location.";
         }
 
         DefaultMutableTreeNode node =
             (DefaultMutableTreeNode) destination.getLastPathComponent();
         if (!node.getAllowsChildren()) {
             return "This node does not allow children";
         }
 
         if (destination.equals(dropper)) {
             return "Destination cannot be same as source";
         }
 
         //
         // If dropper is null, it is a DnD from the alliance list; if dropper
         // is not null, it is a DnD from the tree
         //
         if (dropper != null) {
             if (dropper.isDescendant(destination)) {
                 return "Destination node cannot be a descendant.";
             }
 
             if (dropper.getParentPath().equals(destination)) {
                 return "Destination node cannot be a parent.";
             }
         }
 
         return null;
     }
 }
