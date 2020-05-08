 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.rit.se.reichmafia.htmleditor.models;
 
 /**
  * Handles dealing with the functions such as indent and insert
  * 
  * @author Zach, Wayne
  */
 public class Editor {
     
     private Indent indentor = new Indent();
     private Insert inserter = new Insert();
     
     /**
      * Instantiates an editor construct.
      */
     public Editor () {
         
     }
     
     /**
      * Indents a given buffer with the given start and end indexes
      * @param toIndent
      * @param startIndex
      * @param endIndex 
      */
     public void indent (Buffer toIndent, int startIndex, int endIndex) {
         toIndent.setText(indentor.indentText(toIndent.getText(), startIndex, 
                 endIndex));
     }
     
     /**
      * Insert a given tag at the given index in the given buffer
      * @param toInsert
      * @param name
      * @param startIndex 
      */
     public void insert (Buffer toInsert, String name, int startIndex) {
         toInsert.setText(inserter.insertFlat(toInsert.getText(), name, 
                 startIndex));
     }
     
     /**
      * Inserts a given layered tag with a sub tag name at the given index for
      * n sub tags.
      * @param toInsert
      * @param name
      * @param subName
      * @param startIndex
      * @param subTags 
      */
     public void insert (Buffer toInsert, String name, String subName, 
             int startIndex, int subTags) {
         toInsert.setText(inserter.insertLayered(toInsert.getText(), name, 
                 subName, startIndex, subTags));
     }
     
     /**
     * Inserts a table of given rows and columns at the given index
      * @param toInsert
      * @param startIndex
      * @param rows
      * @param cols 
      */
     public void insert (Buffer toInsert, int startIndex, int rows, int cols) {
         toInsert.setText(inserter.insertTable(toInsert.getText(), startIndex, 
                 rows, cols));
     }
 }
