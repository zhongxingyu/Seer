 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jgenfit.bussines.experiment;
 
 import jgenfit.utils.GenfitLogger;
 
 /**
  *
  * @author alex
  */
 public class Parameter {
 
     private String content;
     private String upper;
     private String name;
     private String starting;
     private String lower;
     private String grid;
     private String flag;
     private String kind;
     private String upperInt;
     private String lowerInt;
     private String linkFunction;
     private final GenfitModel model;
 
     Parameter(String content, GenfitModel model) {
         this.content = content;
         this.model = model;
         this.parse(content);
 
     }
 
     private String getSubstring(int start, int end) {
         if (this.getContent().length() >= end) {
             return this.getContent().substring(start, end);
         } else {
             if (this.getContent().length() >= start) {
                 return this.getContent().substring(start, this.getContent().length());
             }
 
         }
         return new String();
     }
 
     private void parse(String content) {
         this.name = (this.getSubstring(0, 40).replace(".", "").replace(":", ""));
         this.starting = (this.getSubstring(40, 50).trim());
         this.lower = (this.getSubstring(51, 61).trim());
         this.upper = (this.getSubstring(62, 72).trim());
         this.flag = (this.getSubstring(73, 77).trim());
         this.kind = (this.getSubstring(78, 82).trim());
         this.grid = (this.getSubstring(83, 93).trim());
         this.lowerInt = (this.getSubstring(94, 104).trim());
         this.upperInt = (this.getSubstring(105, 115).trim());
 
         if (this.getFlag().trim().equals("4") || this.getFlag().trim().equals("5")) {
             this.linkFunction = (this.getSubstring(78, 1279)).trim();
         } else {
             this.linkFunction = (new String());
         }
     }
 
     public void setParametersUpperLower(int index, String lower, String upper) {
         int startLower = 116 + (22 * (index - 1));
         int startUpper = startLower + 11;
 
         //System.out.println(startLower + "  -- " + startUpper + "  Values: " +  lower + " : " + upper + "   ;");
         this.setParameters(startLower, startUpper - 1, lower);
         this.setParameters(startUpper, startUpper + 11, upper);
     }
     
     public String getLowerParameter(int index){
        int startLower = 116 + (22 * (index - 1)); 
         if (startLower + 10 > this.content.length()){
             return "";
         }
         return this.content.substring(startLower, startLower + 10).trim();        
     }
     
     public String getUpperParameter(int index){
        int startUpper = 117 + (22 * (index - 1)) + 10;  
        if (startUpper + 10 > this.content.length()){
             return "";
         }
         return this.content.substring(startUpper, startUpper + 10).trim();        
     }
     
     public String getParameter(int start, int end){
         try{
             return this.content.substring(start, end).trim();
         }
         catch(Exception Exp){
             GenfitLogger.warn(Exp.getMessage() + ". Expected a length of " + end + " and found a length of " + this.content.length() + " at:" + this.content);
         }
         return this.content.substring(start).trim();
     }
 
     public void setParameters(int start, int end, String value) {
         
         int length = end - start;
         if (value.length() < length) {
             for (int i = value.length(); i < length; i++) {
                 value = value.concat(" ");
             }
         }
         
         if (this.content.length() < end){
             for (int i = this.content.length(); i <= end; i++) {
                 this.content = this.content.concat(" ");
             }
         }
 
         int count = 0;
         for (int i = start; i < end; i++) {
             this.content = replaceCharAt(this.content, i, value.charAt(count));
             count++;
         }
 
         this.parse(content);
 
 
     }
 
     public String toString() {
         return this.content;
     }
 
     private String replaceCharAt(String s, int pos, char c) {
         return s.substring(0, pos) + c + s.substring(pos + 1);
     }
 
     /**
      * @return the content
      */
     public String getContent() {
         return content;
     }
 
     /**
      * @param content the content to set
      */
     public void setContent(String content) {
         this.content = content;
     }
 
     /**
      * @return the upper
      */
     public String getUpper() {
         return upper;
     }
 
     /**
      * @param upper the upper to set
      */
     public void setUpper(String upper) {
         this.setParameters(62, 72, upper);
     }
 
     /**
      * @return the name
      */
     public String getName() {
         return name;
     }
 
     /**
      * @param name the name to set
      */
     public void setName(String name) {
         name = " " + name.trim();
         
         for(int i = name.length(); i < 40; i++){
             name = name + ".";
         }
         name = name + ":";
         
         
         this.setParameters(0, 40, name.trim());
     }
 
     /**
      * @return the starting
      */
     public String getStarting() {
         return starting;
     }
 
     /**
      * @param starting the starting to set
      */
     public void setStarting(String starting) {
         this.setParameters(40, 50, starting);
     }
 
     /**
      * @return the lower
      */
     public String getLower() {
         return lower;
     }
 
     /**
      * @param lower the lower to set
      */
     public void setLower(String lower) {
         this.setParameters(51, 61, lower);
     }
 
     /**
      * @return the grid
      */
     public String getGrid() {
         return grid;
     }
 
     /**
      * @param grid the grid to set
      */
     public void setGrid(String grid) {
         this.setParameters(83, 93, grid);
     }
 
     /**
      * @return the flag
      */
     public String getFlag() {
         return flag;
     }
 
     /**
      * @param flag the flag to set
      */
     public void setFlag(String flag) {
         this.setParameters(73, 77, flag);
     }
 
     /**
      * @return the kind
      */
     public String getKind() {
         return kind;
     }
 
     /**
      * @param kind the kind to set
      */
     public void setKind(String kind) {
         this.setParameters(78, 82, kind);
     }
 
     /**
      * @return the upperInt
      */
     public String getUpperInt() {
         return upperInt;
     }
 
     /**
      * @param upperInt the upperInt to set
      */
     public void setUpperInt(String upperInt) {
 
         this.setParameters(105, 115, upperInt);
     }
 
     /**
      * @return the lowerInt
      */
     public String getLowerInt() {
         return lowerInt;
     }
 
     /**
      * @param lowerInt the lowerInt to set
      */
     public void setLowerInt(String lowerInt) {
         this.setParameters(94, 104, lowerInt);
     }
 
     /**
      * @return the linkFunction
      */
     public String getLinkFunction() {
         return linkFunction;
     }
 
     /**
      * @param linkFunction the linkFunction to set
      */
     public void setLinkFunction(String linkFunction) {
         //this.setParameters(78, 378, linkFunction.trim());
          this.setParameters(78, 1278, linkFunction.trim());
     }
 
     /**
      * @return the model
      */
     public GenfitModel getModel() {
         return model;
     }
 }
