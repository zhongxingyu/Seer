 package xtc.oop.helper; //UPDATE PACKAGE
 import java.util.ArrayList;
 
 public class Pubble{
     String name; //The name of this package
     ArrayList<Pubble> children; //The package children
     ArrayList<Bubble> bubbles; //All the classes within this package
     Pubble parent; //The parent package
 
 //=========Constructors=======//
     public Pubble()
     {
         this.children = new ArrayList<Pubble>();
         this.bubbles = new ArrayList<Bubble>();
     }
 
     public Pubble(String name){
         this.name = name;
         this.children = new ArrayList<Pubble>();
         this.bubbles = new ArrayList<Bubble>();
     }
 
     public Pubble(String name, Pubble parent){
         this.children = new ArrayList<Pubble>();
         this.bubbles = new ArrayList<Bubble>();
         this.name = name;
         this.parent = parent;
     }
 
 
 
 //=========NON-GETTER/SETTER METHODS=======//
 
     //Adds a new package to this Pubble's packageChildren
     public void addChild(Pubble child){
         this.children.add(child);
     }
 
     public void addBubble(Bubble b){
         this.bubbles.add(b);
     }
 
 
     public String getC(){
         String ret = "";
         if(!(name.equals("default package")))
             ret += "namespace " + name + " {\n";
 
 
         for(Bubble b: bubbles){
             ret += b.getC();
         }
 
         //now do it for all children
         for(Pubble p : children){
             ret += p.getC();
         }
 
         if(!(name.equals("Default Package")))
             ret += "}";
         return ret;
     }
 
     //returns a string with the correct information for a .h file
     //lines will be delimited by \n but will not be correctly indented
     public String getH()
     {
        String ret = "";
         ret += getForwardDecl();
         ret += getVTables();
         return ret;
     }
 
     /*prints all forward declarations of data fields, vtables and typedefs for this pnode
     Ex.
     namespace lang {
         struct __Object;
         struct __Object_VT;
 
         typedef __Object* Object;
      }
     */
     public String getForwardDecl()
     {
         String ret = "";
        if(!(name.equals("default package")))
             ret += "namespace " + name + " {\n";
         for(Bubble b : bubbles){
             ret += b.getFDeclStruct();
         }
 
         for(Bubble b: bubbles){
             ret += b.getTypeDef();
         }
 
         //now do it for all children
         for(Pubble p : children){
             ret += p.getForwardDecl();
         }
         if(!(name.equals("Default Package")))
             ret += "}";
         return ret;
     }
 
 
     //returns actual
     public String getVTables()
     {
         String ret = "";
         if(!(name.equals("Default Package")))
             ret += "namespace " + name + " {\n";
         for(Bubble b : bubbles){
             ret += b.getStruct();
             ret += b.getStructVT();
         }
 
         //now do it for all children
         for(Pubble p : children){
             ret += p.getVTables();
         }
 
         if(!(name.equals("Default Package")))
             ret += "}";
         return ret;
     }
 
 //=========GETTER/SETTER METHODS=======//
 
     public String getName(){
         return this.name;
     }
     public void setName(String n){
         this.name = n;
     }
 
     public Pubble getParent(){
         return this.parent;
     }
     public void setParent(Pubble p){
         this.parent = p;
     }
 
     public ArrayList<Bubble> getBubbles(){
         return this.bubbles;
     }
     //No Setter
 
     public ArrayList<Pubble> getChildren(){
         return this.children;
     }
     //No Setter
 }
