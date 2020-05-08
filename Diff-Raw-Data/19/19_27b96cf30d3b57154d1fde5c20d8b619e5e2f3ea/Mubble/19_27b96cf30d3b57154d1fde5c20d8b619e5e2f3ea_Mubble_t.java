 package xtc.oop.helper;
 import java.util.ArrayList;
 
 public class Mubble{
      String header; //header for method
      String name; //class method is in
      String code; //actual code of class, in Block() type node of AST
      
      public Mubble(String iName, String iHeader)
      {
         this.name = iName;
         this.header = iHeader;
        System.out.println("========HEADER: " + iHeader);
         this.code = "";
      }
      
      public String formatMethodHeader(String in)
      {
      //====TODO===//
      //-Deal with isA methods
      //-Go from METHOD FORMAT NOT VTABLE FORMAT
      
         //converts method header from .h format to .cc format
         //From: String (*getName)(Class);
         //To: String __Class::getName(Class __this) {
         
         String[] sploded = in.split(" ");
         String returnType = sploded[0];
         String methodName = getStringBetween(in, "(*", ")");
         String params = getStringBetween(in, ")(", ");");
         for (String s : sploded)
             System.out.println("   Sploded: " + params);
             
         return "";
      }
      
     public static String getStringBetween(String src, String start, String end)  
     {  
         int lnStart; 
         int lnEnd;  
         String ret = "";  
         lnStart = src.indexOf(start);  
         lnEnd = src.indexOf(end);  
         System.out.println("lnStart: " + lnStart + "\nlnEnd: " + lnEnd);
         if(lnStart != -1 && lnEnd != -1)  
             ret = src.substring(lnStart + start.length(), lnEnd);  
       
             return ret;  
     } 
      
      public String getHeader(){
         return this.header;
      }
      
      public String getName(){
         return this.name;
      }
      
      public String getCode(){
         return this.code;
      }
 }
