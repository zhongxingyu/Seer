 package xtc.oop;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.FileReader;
 
 import java.util.*; //ut oh, is Grimm going to be mad?
 
 import xtc.parser.ParseException;
 import xtc.parser.Result;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 import xtc.tree.Location;
 import xtc.tree.Printer;
 
 import xtc.lang.JavaFiveParser;
 
 //OUR IMPORTS
 import java.io.FileWriter;
 import java.io.BufferedWriter;
 
 import xtc.oop.helper.Bubble;    //NEED TO UPDATE TO OUR NEW DATA STRUCTURES
 import xtc.oop.helper.Mubble;
 import xtc.oop.helper.Pubble;
 import xtc.oop.helper.Field;
 
 public class StructureParser extends xtc.tree.Visitor //aka Decl
 {
 
     public static ArrayList<Bubble> bubbleList;
     public static ArrayList<PNode> packageTree;
     public static ArrayList<Mubble> mubbleList;
     public static ArrayList<String> parsed; //keeps track of what ASTs have been parsed
     public Pubble curPub;
     public Bubble curBub;
     public Mubble curMub;
 
     public Field curField;
 
     public StructureParser(ArrayList<Pubble> packageTree, ArrayList<Mubble> mubbleList, ArrayList<Bubble> bubbleList, ArrayList<String> parsed)
     {
         this.packageTree = packageTree;
         this.mubbleList = mubbleList;
         this.bubbleList = bubbleList;
         this.parsed = parsed;
     }
 
     public void visit(Node n)
     {
         int counter = 1;
         if(n.hasProperty("parent0")) {
             Node temp = (Node)n.getProperty("parent0");
 
             while(temp != null) {
                 n.setProperty("parent"+(counter++), temp.getProperty("parent0"));
                 temp = (Node)temp.getProperty("parent0");
             }
         }
 
         for (Object o : n){
             if (o instanceof Node){
                 ((Node)o).setProperty("parent_name", n.getName() );
                 ((Node)o).setProperty("parent0", n );
                 dispatch((Node)o);
             }
         }
     }
 
 
     public void visitClassDeclaration(GNode n){
         //n.getString(0) is the Modifiers node
         //n.getString(1) is the name of the class
         String className = n.getString(1);
         boolean found = false;
         for(Bubble b : bubbleList){
             if(b.getName().equals(className)) {
                 curBub = b;
             }
         }
         if (!found){
             curBub = new Bubble(className);
         }
         curBub.setParentPubble(curPub); //curBub's package is curPub
 
         //find Object bubble and set curBub's parent to object (it will get overwritten if it needs to)
         Bubble ob = new Bubble();
         for(Bubble b : bubbleList){
             if(b.getName().equals("Object")) {
                 ob = b;
             }
         }
 
         curBub.setParentBubble(ob); 
 
         visit(n);
 
         //curBub should be complete here, all it's dataFields, methods, children bubbles, package..etc
         bubbleList.add(curBub);
         curPub.addBubble(curBub);
     }
 
 
 
     public void visitFieldDeclaration(GNode n){
         visit(n);
     }
 
     public void visitDimensions(GNode n) {
         //will set curfield as an array and with correct details if appropriate
         visit(n);
         Node parent0 = (Node)n.getProperty("parent0");
         Node parent1 = (Node)n.getProperty("parent1");
         Node parent2 = (Node)n.getProperty("parent2");
         Node parent3 = (Node)n.getProperty("parent3");
         if ((parent0.hasName("Type")) &&
                 (parent1.hasName("FieldDeclaration")))
         {
             curField.setIsArray(true);
 
             //count dimensions
             int count = 0;
            for(Object o : n){
                 if(o.instanceof(String) && ((String)o).equals("[")){
                     count++;
                 }
             }
             curField.setArrayDims(count);
         }
     }
 
     public void visitModifiers(GNode n){
         visit(n);
 
     }
 
     public void visitConstructorDeclaration(GNode n){
         String name = n.getString(2);
         freshMubble = new Mubble(name);
         freshMubble.setConstructor(true);
 
         curMub = freshMubble;
 
         visit(n);
 
         curMub.setClassName(curBub);
         curMub.setPackageName(curPub);
 
         mubbleList.add(curMub);
         curBub.addMubble(curMub);
 
 
     }
 
     public void visitMethodDeclaration(GNode n){
         visit(n);
 
         /*
          * get method name
          * Create a new mubble
          * Visit
          * Add this new mubble to curbub
          * Also add to mubble list
          */
 
         //Create a Mubble to add
         Mubble freshMubble;
         String name = n.getString(3);
         if (name == "static"){
             name = n.getString(4);
             freshMubble = new Mubble(name);
             freshMubble.setStatic(true);
         }
         else{
             freshMubble = new Mubble(name);
         }
         curMub = freshMubble;
 
         visit(n);
 
         curMub.setClassName(curBub);
         curMub.setPackageName(curPub);
 
         mubbleList.add(curMub);
 
         curBub.addMubble(curMub);
 
     }
 
     public void visitModifier(GNode n){
         visit(n);
 
         Node parent0 = (Node)n.getProperty("parent0");
         Node parent1 = (Node)n.getProperty("parent1");
         Node parent2 = (Node)n.getProperty("parent2");
         Node parent3 = (Node)n.getProperty("parent3");
 
         if ((parent1.hasName("MethodDeclaration")) &&
                 (parent2.hasName("ClassBody")))
         {
             String visibility = n.getString(0);
             curMub.setVisibility(visibility);
         }
 
         if ((parent1.hasName("FormalParameter")) &&
                 (parent3.hasName("MethodDeclaration")))
         {
             String modifier = n.getString(0);
             curField.addModifier(modifier);
         }
     }
 
      }
 
 
     public void visitDeclarators(GNode n) {
         visit(n);
     }
 
     public void visitDeclarator(GNode n) {
         //TODO fact check with an AST (see testAssignmt..a.java)
         visit(n);
         Node parent0 = (Node)n.getProperty("parent0");
         Node parent1 = (Node)n.getProperty("parent1");
         Node parent2 = (Node)n.getProperty("parent2");
 
         //if an assignment is being made within a class declaration
         if ((parent1.hasName("FieldDeclaration")) &&
         (parent2.hasName("ClassDeclaration"))){
             curField.setHasAssignment(true);
             curField.setAssignmentNode(n); //save the node so we can re-parse it later
         }
     }
 
     public void visitIntegerLiteral(GNode n) {
         visit(n);
         Node parent0 = (Node)n.getProperty("parent0");
         Node parent1 = (Node)n.getProperty("parent1");
         Node parent2 = (Node)n.getProperty("parent2");
 
         if ((parent0.hasName("ConcreteDimensions")) &&
         (parent2.hasName("ClassDeclaration"))){
             //TODO what do we do about full declarations? e.g.
             //String[] SA = new String[4];
             //int a = 5;
             //Bubble b = new Bubble(param1, param2, param3)
             //THIS IS A BIG TODO
 
         }
     }
 
     public void visitClassBody(GNode n){
         visit(n);
     }
 
     public void visitFormalParameters(GNode n){
         visit(n);
     }
 
     public void visitFormalParameter(GNode n) {
 
         Field tempField = new Field();
         curField = tempField;
 
         String name = n.getString(3);
         curField.setName(name);
 
         visit(n);
 
         curMub.addParameter(curField);
     }
 
     public void visitCompilationUnit(GNode n){
         curPub = new Pubble();
         visit(n);
         pubbleList.add(curPub);
     }
 
     public void visitQualifiedIdentifier(GNode n){
         visit(n);
         Node parent0 = (Node)n.getProperty("parent0");
         Node parent1 = (Node)n.getProperty("parent1");
         Node parent2 = (Node)n.getProperty("parent2");
 
         //finding inheritance
         if ((parent1.hasName("Extension")) &&
         (parent2.hasName("ClassDeclaration"))){
             String name = n.getString(0);
 
             boolean parentFound = false;
             Bubble parent;
             for(Bubble b : bubbleList){
                 if(b.hasName(parentName)){             //if the parent is already in bubbleList
                     parent = b;
                     parentFound = true;
                 }
             }
 
             if(!parentFound){
                 parent = new Bubble(parentName);      //if parent isn't found, create it
                 bubbleList.add(parent);
             }
 
             parent.addBubble(curBub);  //add myself as my parent's child
             curBub.addParentBubble(parent); //set my parent
         }
 
         //finding the package curBub belongs to
         if (parent0.getName().equals("PackageDeclaration")){
             //looping through something like...
             /*QualifiedIdentifier(
               "xtc",
               "oop",
               "helper"
             )*/
             String name, packageName;
             for(int i=0; i<n.size(); i++){
                 name = n.getString(i);
                 packageName += " " + name;
             }
 
             curPub.setName(packageName);
 
             /* ===============DON'T THINK WE NEED THIS=======================
             //check to see if this package is already in pubbleList
             Pubble packPub;
             Boolean inPubbleList = false;
             for(Pubble p : pubbleList)
             {
                 if(p.getName().equals(packageName))
                 {
                     inPubbleList = true;
                     packPub = p;
                 }
             }
 
             //if its not in the packageList, create a new Pubble, and add it to pubbleList
             if(!inPubbleList)
             {
                 packPub = new Pubble(packageName);
                 pubbleList.add(packPub);
             }
 
             packPub.addBubble(curBub);
             curBub.setParentPubble(packPub);
             */
         }
 
             //get return type for methods
             if ((parent0.hasName("Type")) &&
                     (parent1.hasName("MethodDeclaration")))
             {
                 String type = n.getString(0);
                 curMub.setReturnType(type);
             }
 
             //get parameter type for methods
             if ((parent0.hasName("Type")) &&
                     (parent1.hasName("FormalParameter")))
             {
                 String type = n.getString(0);
                 curField.setType(type);
             }
         }
 
     public void visitImportDeclaration(GNode n){
         visit(n);
     }
 
     public void visitForStatement(GNode n)
     {
         visit(n);
     }
 
     public void visitBasicForControl(GNode n)
     {
         visit(n);
     }
 
     public void visitPrimitiveType(GNode n) {
         visit(n);
         Node parent0 = (Node)n.getProperty("parent0");
         Node parent1 = (Node)n.getProperty("parent1");
         Node parent2 = (Node)n.getProperty("parent2");
 
         //finding inheritance
         if ((parent0.hasName("Type")) &&
         (parent1.hasName("FormalParameter"))){
             String type = n.getString(0);
             curField.setType(type);
         }
     }
 
     public void visitType(GNode n)
     {
         visit(n);
     }
 
     public void visitExpressionList(GNode n)
     {
         visit(n);
     }
 
     public void visitRelationalExpression(GNode n)
     {
         visit(n);
     }
 
 }
