 package jrask.fixtures;
 
 public class Compiler {
 
     private String filename;
 
     public void setSource(String filename) {
         this.filename = filename;
     }
 
     public boolean failed() {
         return true;
     }
    
     public String errors() {
         return filename + ":1:1: expected 'class'";
     }
 }
