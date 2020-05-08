 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gui;
 
 import SCADASite.SCADASite;
 import java.awt.Color;
 import java.awt.Component;
 import java.util.ArrayList;
 import javax.swing.JLabel;
 import javax.swing.JTree;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.*;
 
 /**
  *
  * @author Shawn
  */
 public class SCADAJTree extends JTree
 {
     DefaultMutableTreeNode root;
     TreeModel siteModel;
     
     public SCADAJTree() {
         super();
         root = new DefaultMutableTreeNode("Sites");
         siteModel = new DefaultTreeModel(root);
         this.setModel(siteModel);
         this.setCellRenderer(new SCADACellRenderer());
         this.setFocusable(true);
         
     }
     
     public void setSCADASites(ArrayList<SCADASite> sites) {
         for(SCADASite site: sites) {
             root.add(new SCADANode(site));
         }
         
         for (int i = 0; i < this.getRowCount(); i++) 
         {
          this.expandRow(i);
         }
     }
 
     
     
     
     class SCADANode extends DefaultMutableTreeNode {
         
         private SCADASite site;
         
         public SCADANode(SCADASite site) {
             this.site = site;
         }
         
         public SCADASite getSite() {
             return site;
         }
     }
     
     private class SCADACellRenderer implements TreeCellRenderer {
 
         @Override
         public Component getTreeCellRendererComponent(JTree jtree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
             JLabel label = new JLabel();
             
             if(value instanceof SCADANode) {
                 SCADANode node = (SCADANode) value;
                 SCADASite site = node.getSite();
                 label.setText(site.getName());
                 
                 if(site.getAlarm()) {
                     label.setForeground(Color.red);
                 } else if(site.getWarning()) {
                     label.setForeground(Color.orange);
                 } else {
                     label.setForeground(Color.black);
                 }
                 
                 if(selected) {
                     if(!hasFocus) {
                         label.setBackground(Color.gray.brighter());
                     } else {
                         label.setBackground(Color.cyan);
                     }
                 }
                 
             } else if(value instanceof DefaultMutableTreeNode){
                 label.setText(((DefaultMutableTreeNode) value).toString());
             } else {
                 label.setText("Invalid argument passed");
             }
             
             return label;
         }
         
         
         
     }
     public SCADASite getSelected(TreeSelectionEvent tse)
     {
         DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                        getLastSelectedPathComponent();
         
         if(node instanceof SCADANode)
         {
             return ((SCADANode) node).getSite();
         }
         else
             return null;
     }
 }
