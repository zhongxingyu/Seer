 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2001 Myles Chippendale
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
  *     Myles Chippendale <mylesc@paneris.org>
  */
 package org.melati.util;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 import java.util.Vector;
 import java.util.Enumeration;
import java.util.NoSuchElementException;
 
 /**
  * A tree of <code>DefaultMutableTreeNode</code>s, 
  * the <code>userObject</code>s of which
  * are {@link Treeable} objects which supply their own
  * <code>getChildren()</code> functions. 
  * This is used to build the tree of
  * <code>DefaultMutableTreeNode</code>s.
  * <p>
  * It also allows you to search the subtree for a particular
  * {@link Treeable} object and returns the corresponding
  * <code>DefaultMutableTreeNode</code> object if one exists.
  *
  * @see DefaultMutableTreeNode
  *
 * @version 1.00 04/10/2000
  * @author Myles Chippendale
  **/
 
 
 public class ChildrenDrivenMutableTree {
 
     /**
      * An enumeration that is always empty. This is used when an enumeration
      * of a leaf node's children is requested.
      */
    // FIXME Why not just = DefaultMutableTreeNode.EMPTY_ENUMERATION ?
    public static final Enumeration EMPTY_ENUMERATION
        = new Enumeration() {
          public boolean hasMoreElements() { return false; }
          public Object nextElement() {
             throw new NoSuchElementException("No more elements");
       }
    };
 
     /** root node */
     protected DefaultMutableTreeNode root;
 
     public ChildrenDrivenMutableTree() {
       this(null);
     }
 
     public ChildrenDrivenMutableTree(Treeable userObject) {
       root = new DefaultMutableTreeNode(userObject);
       buildTree();
     }
 
     public void buildTree() {
       buildTree(computeChildren(root));
     }
 
     public void buildTree(Enumeration nodes) {
       while (nodes.hasMoreElements())
         buildTree(computeChildren((DefaultMutableTreeNode)nodes.nextElement()));
     }
 
     private static Enumeration computeChildren(DefaultMutableTreeNode node) {
       if (node == null)
         return EMPTY_ENUMERATION;
       Treeable[] kids = ((Treeable)node.getUserObject()).getChildren();
       for(int i = 0; i<kids.length; i++) {
           node.add(new DefaultMutableTreeNode(kids[i]));
       }
       return node.children();
     }
 
 
     public DefaultMutableTreeNode getTreeNodeFor(Treeable search) {
 
         Vector agenda = new Vector();
         agenda.addElement(root);
 
         while (!agenda.isEmpty()) {
             DefaultMutableTreeNode current =
                 (DefaultMutableTreeNode)agenda.firstElement();
             if (current == null)
               return null;
             if (current.getUserObject() == search)
               return current;
 
             agenda.removeElementAt(0);
             Enumeration kids = current.children();
             while(kids.hasMoreElements()) {
               agenda.addElement(kids.nextElement());
             }
         }
         return null;
     }
 
     public DefaultMutableTreeNode getRoot() {
       return root;
     }
 
     /**
      * Return an enumeration of nodes in preorder, whatever
      * that means.
      * <p>
      * Root is first node. What is the difference
      * from breadth first?
      */
     public Enumeration preorderEnumeration() {
       return root.preorderEnumeration();
     }
 
     /**
      * Return an enumeration of nodes in postorder, whatever
      * that means.
      * <p>
      * Leftmost leaf is first. What is the difference
      * from depth first?
      */
     public Enumeration postorderEnumeration() {
       return root.postorderEnumeration();
     }
 
     /**
      * Return an enumeration of nodes in breadth first order.
      */
     public Enumeration breadthFirstEnumeration() {
       return root.breadthFirstEnumeration();
     }
 
     /**
      * Return an enumeration of nodes in depth first order.
      */
     public Enumeration depthFirstEnumeration() {
       return root.depthFirstEnumeration();
     }
 }
