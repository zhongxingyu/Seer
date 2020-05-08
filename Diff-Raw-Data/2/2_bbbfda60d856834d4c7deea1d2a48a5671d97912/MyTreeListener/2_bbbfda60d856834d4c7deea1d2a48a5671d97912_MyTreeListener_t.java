 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package test;
 
 import javax.swing.JTree;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 
 /**
  *
  * @author Matthew Shepherd <s0935850> and Robert Evans <s0949775>
  * 
  * Custom tree listener to react to different nodes being selected
  * 
  */
 public class MyTreeListener implements TreeSelectionListener {
     
     ObjectManager manager;
     JTree tree;
     ImagePanel panel;
     
     /**
     * Constructor, params are references to data structures and visual elements
      * @param mngr Used to determine which Polygon has been selected
      * @param _tree Used to fetch which node was clicked
      * @param p  Used to change user mode
      */
     public MyTreeListener(ObjectManager mngr, JTree _tree, ImagePanel p) {
         manager = mngr;
         tree = _tree;
         panel = p;
     }
     
     /**
      * Called when the selected value in the tree has been changed (ie user has clicked on it)
      * @param tse 
      */
     @Override
     public void valueChanged(TreeSelectionEvent tse) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
          
 
     /* if nothing is selected */ 
         if (node == null) return;
 
     /* retrieve the node that was selected */ 
         Object nodeInfo = node.getUserObject();
         
         manager.select(manager.get(nodeInfo.hashCode()));
         if(node.toString().equals("Objects")) {
             panel.mode = ImagePanel.Mode.Limbo2;
             return;
         }
         panel.mode = ImagePanel.Mode.EditPoly;
     }
     
 }
