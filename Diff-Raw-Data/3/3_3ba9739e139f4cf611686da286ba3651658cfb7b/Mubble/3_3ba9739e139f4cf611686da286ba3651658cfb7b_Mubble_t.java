 package xtc.oop.helper;
 import java.util.ArrayList;
 
 public class Mubble{
      String header; //header for method
      String methName; //name of the method
      String name; //class method is in
      String code; //actual code of class, in Block() type node of AST
      String packageName;
      boolean mainMeth; //method is the main method
      boolean isConstructor;
      public Mubble(String iName, String iHeader, boolean construct)
      {
         this.name = iName;
         this.methName = extractMethodName(iHeader);
         if(construct){
             this.header = iHeader;
         }
         else
             this.header = formatMethodHeader(iHeader);
         this.code = "";
         this.isConstructor = construct;
      }
      
      public Mubble(String classname , String methodname, String header, boolean construct)
      {
         this.name = classname;
         this.methName = methodname;
         this.header = header;
         this.isConstructor = construct;
      }
 
      public boolean isConstructor()
      {
         return this.isConstructor;
      }
 
      //returns a String of the formatted method for .cc file
      public String prettyPrinter()
      {
         String ret = "";
         if(isConstructor())
             ret += this.header + " : __vptr(&__vtable) {\n";
         else
             ret += this.header + "{\n";
         ret += this.code + "\n";
         ret += "}\n";
 
         return ret;
      }
      
      
 
 
      public void setPackageName(String pack)
      {
         this.packageName = pack;
      }
 
      public String getPackageName(){
         return this.packageName;
      }
 
      public String extractMethodName(String in)
      {
	
	 String[] sploded = in.trim().split(" ");
         if (sploded[sploded.length - 1] == "main")
             mainMeth = true;
         else
             mainMeth = false;
 
         return sploded[sploded.length - 1];
      }
      public String formatMethodHeader(String in)
      {
      //====TODO===//
      //-Deal with isA methods
         if (mainMeth == true)
             return "int main(void)";
 
 
         //converts method header from .h format to .cc format
         //From: public String toString
         //To: String __String::toString(String __this) {
 	 if (in == null) { // in should not be null
 	     return null;
 	 }
 	 if (getName().equals("Object") ||
 	     getName().equals("String") ||
 	     getName().equals("Class")) return in;
 
 	 if (in.matches(".*[\\(\\)].*")) {
 	     return in;
 	 }
 
 	 String tab = "";
 
 	 for (int i = 0; i < in.length(); i++) {
 	     if (in.charAt(i) == '\t') tab = "\t";
 	 }
 
 	 int square = 0;
 	 for (int i = 0; i < in.length(); i++) {
 	     if (in.charAt(i) == '[') square++;
 	 }
 
 	 String[] temp2 = in.split("[ \t]");
 
 	 int count = 0;
 	 for (int j = 0; j < temp2.length; j++) {
 	     if (temp2[j].length() != 0) count++;
 	 }
 
 	 String[] temp = new String[count-square];
 	 int index = 0;
 	 for (int j = 0; j < temp2.length; j++) {
 	     if (temp2[j].length() != 0) {
 		 if (temp2[j].charAt(0) == '[') {
 		     //temp[index-1] += "[]";
 		     temp[index-1] = "__rt::Array<" + temp[index-1] + ">*";
 		 }
 		 else {
 		     temp[index++] = convertPrimitiveType(temp2[j]);
 		 }
 	     }
 	 }
 
 	 int num = 0;
 	 for (int j = 0; j < temp.length; j++) {
 	     if (temp[j].equals("public") ||
 		 temp[j].equals("private") ||
 		 temp[j].equals("protected") ||
 		 temp[j].equals("static")) {
 		 //do nothing
 	     }
 	     else {
 		 num++;
 	     }
 	 }
 
 	 String s = "";
 	 if (num % 2 == 0) { // there is a return type
 	     s += temp[temp.length-num] + " ";
 	     index = temp.length-num+1;
 	 }
 	 else { // void
 	     s += "void ";
 	     index = temp.length-num;
 	 }
 
 	 s += "_"  + getName() + "::" + temp[temp.length-1] + "(" +
 	     getName() + " __this";
 
 	 for (int j = index; j < temp.length - 1; j+=2) {
 	     s += ", " + temp[j] + " " + temp[j+1];
 	 }
 
 	 s += ")";
 	 return s + tab;
      }
 
     public String convertPrimitiveType(String s) {
 	if (s.equals("int"))
 	    return "int32_t";
 	if (s.equals("boolean"))
 	    return "bool";
 	return s;
     }
 
      public boolean isModifier(String s)
      {
 
         s = s.trim();
 
         if(s.equals("static") || s.equals("public") || s.equals("private") || s.equals("protected"))
             return true;
         else
             return false;
      }
 
     public static String getStringBetween(String src, String start, String end)
     {
         int lnStart;
         int lnEnd;
         String ret = "";
         lnStart = src.indexOf(start);
         lnEnd = src.indexOf(end);
         if(lnStart != -1 && lnEnd != -1)
             ret = src.substring(lnStart + start.length(), lnEnd);
 
             return ret;
     }
 
      public String getHeader(){
         return this.header;
      }
 
      public String getMethName(){
         return this.methName;
      }
 
      public String getName(){
         return this.name;
      }
 
      public String getCode(){
         return this.code;
      }
 
     public void setCode(String s) {
 	this.code = s;
     }
 }
