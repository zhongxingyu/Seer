 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.lightningrod.gui;
 
 import com.dropbox.client2.DropboxAPI;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Enumeration;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.MutableTreeNode;
 
 /**
  * A class that defines a Node in the DropBox File Structure
  * The node object is the DropBox Entry Object
  * @author William Etter
  */
 class DBNode extends DefaultMutableTreeNode {
     // <editor-fold>
     protected boolean isDirectory;  // Is the node a Directory?
     protected boolean isSelected;   // Is the node selected?    
     
     /*
      ********** CONSTRUCTORS ************
     */
     public DBNode() {
         // Should only go here when creating the root node
         super(null,true);
     }
 
     public DBNode(Object dbEntry) {
         super(dbEntry,((DropboxAPI.Entry)dbEntry).isDir);
         
         DropboxAPI.Entry entry = (DropboxAPI.Entry)dbEntry;
     }
     
     /*
      * ONLY USED FOR TESTING
      */
     public DBNode(Object dbEntry, boolean direct) {
         super(dbEntry,direct);
         DropboxAPI.Entry entry = (DropboxAPI.Entry)dbEntry;
     }
     
     /*
      * Override add method to allow for sorting of files/folders
      */
     @Override
     public void add(MutableTreeNode newChild) {
         super.add(newChild);
         Collections.sort(this.children, nodeComparator);
         
         // Determine if parent is selected
         if(this.isSelected()){
             // Set child as selected
             ((DBNode)newChild).setSelected(true);
         }      
     }
 
     /*
     *   Set the node selection.  If a directory, set all children as selected
     *   recursively.
     */
     public void setSelected(boolean isSelected) {
         this.isSelected = isSelected;
         if (children != null) {
             Enumeration e = children.elements();
             while (e.hasMoreElements()) {
                 DBNode node = (DBNode) e.nextElement();
                 node.setSelected(isSelected);
             }
         }
     }
     
     /*
     *   Returns whether the node is selected or not.
     */
     public boolean isSelected() {
         return isSelected;
     }
     
     /*
      *  Returns whether the node allows children or not.
      */
     public boolean childrenAllowed(){
         return this.allowsChildren;
     }
     
     
     /*
     *   Overrides the DefaultMutableTreeNode toString method for use when
     *   displaying file structure
     */
     @Override
     public String toString(){
         if((DropboxAPI.Entry)this.getUserObject() == null){
             return "Need to Sync with DropBox Server";
         }
         else{
             return ((DropboxAPI.Entry)this.getUserObject()).fileName();
         }
     }
     
     protected Comparator nodeComparator = new Comparator () {
         @Override
         public int compare(Object o1, Object o2) {
             // Check object values
             if(((DBNode)o1).childrenAllowed()){ 
             //if(((DropboxAPI.Entry)(((DBNode)o1).getUserObject())).isDir){ -PUT BACK IN AFTER TESTING
                 //if(((DropboxAPI.Entry)o1).isDir){
                 if(((DBNode)o2).childrenAllowed()){
                     // o1 directory and o2 directory
                     return o1.toString().compareToIgnoreCase(o2.toString());
                 }
                 else{
                     // o1 directory and o2 file -> file first
                     return -1;
                 }
             }else{
                 if(((DBNode)o2).childrenAllowed()){
                //if(((DBNode)o2).childrenAllowed()){
                 //if(((DropboxAPI.Entry)(((DBNode)o2).getUserObject())).isDir){ - PUT BACK IN AFTER TESTING
                     // o1 file and o2 directory -> file first
                     return 1;
                 }
                 else{
                     // o1 file and o2 file
                     return o1.toString().compareToIgnoreCase(o2.toString());
                 }
             }
         }
     };
     // </editor-fold>
 }   // END CLASS DBnode
