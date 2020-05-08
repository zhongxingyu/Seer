 package xtc.oop.helper;
 import java.util.ArrayList;
 
 public class PNode{
     String name;
     PNode[] packageChildren;
     Mubble[] mubbleList;
     String[] structChildren;
     PNode parent;
     ArrayList<String> structs = new ArrayList<String>();
 
 
     public PNode(String name){
         this.name = name;
     }
     public PNode(String name, PNode parent){
         this.name = name;
         this.parent = parent;
     }
 
     public boolean hasStruct(String s)
     {
         return structs.contains(s);
     }
 
 
     public Mubble[] getMubblelist(){
         return this.mubbleList;
     }
 
     public void addMubble(Mubble child){
         if(child == null){
             return;
         }
         int len = mubbleList == null ? 1 : mubbleList.length + 1;
         Mubble[] temp = new Mubble[len];
         if(mubbleList == null){
             temp[0] = child;
             mubbleList = temp;
         }
         else{
             for (int i = 0; i < mubbleList.length ; i++ ){
                 temp[i] = mubbleList[i];
             }
             temp[len-1] = child;
             mubbleList = temp;
         }
     }
 
 
     public void addPNodeChild(PNode child){
         if(child == null){
             return;
         }
         int len = packageChildren == null ? 1 : packageChildren.length + 1;
         PNode[] temp = new PNode[len];
         if(packageChildren == null){
             temp[0] = child;
             packageChildren = temp;
         }
         else{
             for (int i = 0; i < packageChildren.length ; i++ ){
                 temp[i] = packageChildren[i];
             }
             temp[len-1] = child;
             packageChildren = temp;
         }
     }
 
 
     public void addFirstStruct(String struct){
         //In order to add the forward struct declarations and typedefs
         int len = structChildren.length;
         String[] temp = new String[len+1];
         for(int i = 0; i< len; i++){
             temp[i+1] = structChildren[i];
         }
        temp[1] = struct;
         structChildren = temp;
 
     }
 
     public static String indentLevel(int indent){
         String toReturn = "";
         for( int i=0; i<indent; i++){
             toReturn += "  ";
         }
         return toReturn;
     }
 
 
     public String getForwardDecl(){
         String toReturn = "";
         int indent = 0;
         String lastname = name.split(" ")[name.split(" ").length -1];
         if(!name.equals("DefaultPackage")){
             toReturn += indentLevel(indent) + "namespace "+ lastname +"{\n";
             indent++;
         }
 
         //adding forward Decls
         if(structChildren != null && structChildren[0] != null)
             toReturn += structChildren[0] + "\n";
 
         if(packageChildren != null)
             for(int i = 0; i <packageChildren.length; i++){
                 toReturn += packageChildren[i].getForwardDecl() + "\n";
             }
 
         if(!name.equals("DefaultPackage")){
             toReturn += "}\n";
         }
         return toReturn;
     }
 
 
     public String getOutput(){
         int indent= 0;
         String toReturn = "";
         //for printing the entire .h
         //print my structs, print my children struct
         String lastname = name.split(" ")[name.split(" ").length -1];
         if(!name.equals("DefaultPackage")){
             toReturn += indentLevel(indent) + "namespace "+ lastname +"{\n";
             indent++;
         }
         if(structChildren != null)
             for(int i = 1; i < structChildren.length; i ++){
                 toReturn += structChildren[i] + "\n";
             }
         if(packageChildren != null)
             for(int i = 0; i <packageChildren.length; i++){
                 toReturn += packageChildren[i].getOutput() + "\n";
             }
 
         if(!name.equals("DefaultPackage")){
             toReturn+= "}\n";
         }

         return toReturn;
     }
 
     public String getOutputCC(){
         int indent= 0;
         String toReturn = "";
         //for printing the entire .cc
         //print my CONSTRUCTORS, print my formatted Mubbles
         String lastname = name.split(" ")[name.split(" ").length -1];
         if(!name.equals("DefaultPackage")){
             toReturn += indentLevel(indent) + "namespace "+ lastname +"{\n";
             indent++;
         }
 
         //ADD CONSTUCTORS
 
         //ADD MUBBLES
         if(mubbleList != null)
         {
             for(int i=0; i < mubbleList.length; i++)
             {
                 toReturn += mubbleList[i].prettyPrinter() + "\n";
             }
         }
 
         if(packageChildren != null)
             for(int i = 0; i <packageChildren.length; i++){
                 toReturn += packageChildren[i].getOutputCC() + "\n";
             }
 
         if(!name.equals("DefaultPackage")){
             toReturn+= "}\n";
         }
 
         //System.out.println(toReturn);
         return toReturn;
     }
 
     public void addStructChild(String child){
         structs.add(child);
         if(child == null){
             return;
         }
         int len = structChildren == null ? 1 : structChildren.length + 1;
         String[] temp = new String[len];
         if(structChildren == null){
             temp[0] = child;
             structChildren = temp;
         }
         else{
             for (int i = 0; i < structChildren.length ; i++ ){
                 temp[i] = structChildren[i];
             }
             temp[len-1] = child;
             structChildren = temp;
         }
     }
 
     public void setParent(PNode p){
         this.parent = p;
     }
 
     public PNode[] getPackageChildren(){
         return this.packageChildren;
     }
 
     public String[] getStructChildren(){
         return this.structChildren;
     }
 
     public String getName(){
         return this.name;
     }
 
     public PNode getParent(){
         return this.parent;
     }
 
     public String toString(){
         String toReturn = "";
         toReturn += this.name +"\n";
 
         if(this.name != "DefaultPackage")
             toReturn += "Parent: " + this.parent.getName() +"\n";
 
         if(packageChildren != null){
             toReturn += "pChildren: " +"\n";
             for (int i = 0; i < packageChildren.length ; i++ ){
                 toReturn += packageChildren[i].getName() + "\n";
             }
         }
 
         if(structChildren != null){
             toReturn += "struct Children: " +"\n";
             for (int i = 0; i < structChildren.length ; i++ ){
                 toReturn += structChildren[i] + "\n";
             }
         }
 
         return toReturn;
     }
 
 }
