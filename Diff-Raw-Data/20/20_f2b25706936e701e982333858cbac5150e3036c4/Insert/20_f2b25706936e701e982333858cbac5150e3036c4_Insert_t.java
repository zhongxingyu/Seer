 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.rit.se.reichmafia.htmleditor.models;
 
 /**
  * Handles the insertion of new tags
  * 
  * @author Zach
  */
 public class Insert {
     
     public String insertFlat (String text, String name, int index) {
         String newString = text.substring(0,index) + "<" + name + "></" + name
                + ">" + text.substring(index);
         return newString;
     }
     
     public String insertLayered (String text, String name, String subName, 
             int index, int subTags) {
        String newString = text.substring(0,index) + "<" + name + ">\n";
         int i = 0;
         for (i = 0; i < subTags; i++) {
             newString = newString + insertFlat("", subName, 0) + "\n";
         }
        newString = newString + "</" + name + ">\n" + text.substring(index);
         return newString;
     }
     
     public String insertTable (String text, int index, int rows, int cols) {
         String newString = text.substring(0,index) + "\n<table>\n";
        if (rows > 0) {
            newString = newString + insertLayered("", "th", "td", 0, cols);
        }
        
         int i = 0;
         for (i = 0; i < rows-1; i++) {
             newString = newString + insertLayered("", "tr", "td", 0, cols);
         }
        
        newString = newString + "</table>\n" + text.substring(index);
         return newString;
     }
 }
