 /*
  * Kajona Language File Editor Gui
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the
  * Free Software Foundation; either version 2, or (at your option) any
  * later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA
  *
  * (c) MulchProductions, www.mulchprod.de, www.kajona.de
  *
  */
 package de.mulchprod.kajona.languageeditor.gui.tree;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 
 /**
  *
  * @author sidler
  */
 public class GuiTreeNode extends DefaultMutableTreeNode {
 
     private TreeNode referencingTreeNode;
 
     public GuiTreeNode(TreeNode referencingTreeNode) {
         this.referencingTreeNode = referencingTreeNode;
     }
 
     @Override
     public String toString() {
         String addon = "";
        if(referencingTreeNode.getReferencingObject() != null)
             addon = " ("+referencingTreeNode.getReferencingObject().getAllKeys().size()+")";
         return referencingTreeNode.getNodeName()+addon;
     }
 
     public TreeNode getReferencingTreeNode() {
         return referencingTreeNode;
     }
 
     
 
 }
