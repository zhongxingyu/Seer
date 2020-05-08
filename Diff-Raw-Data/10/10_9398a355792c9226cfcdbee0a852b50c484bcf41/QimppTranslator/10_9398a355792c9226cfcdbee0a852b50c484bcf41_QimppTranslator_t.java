 /*
  * xtc - The eXTensible Compiler
  * Copyright (C) 2012 Robert Grimm
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * version 2 as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
  * USA.
  */
 
 package qimpp;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.PrintWriter;
 import java.util.Arrays;
 import java.util.ArrayList;
 
 import xtc.lang.JavaFiveParser;
 
 import xtc.parser.ParseException;
 import xtc.parser.Result;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 import xtc.tree.Location;
 import xtc.tree.Printer;
 
 import xtc.util.Tool;
 
 /**
  * A translator from (a subset of) Java to (a subset of) C++.
  *
  * Uses visitor and the return Object of dispatch()
  *
  *
  *
  * @author QIMPP
  * @version 0.1
  */
 public class QimppTranslator extends Tool {
   
     
   GNode currentClass, currentMethod, currentConstructor, parentClassNode;
   String currentClassName;
   String parentName;
   CPPAST cppast;
   InheritanceTreeManager treeManager;
       
   /** Create a new translator. */
   public QimppTranslator() {
     cppast = new CPPAST();
   }
 
   public String getName() {
     return "Java to C++ Translator";
   }
 
   public String getCopy() {
     return "(C) 2012 qimpp";
   }
 
   public void init() {
     super.init();
   }
 
   public void prepare() {
     super.prepare();
 
     // Perform consistency checks on command line arguments.
   }
 
   public File locate(String name) throws IOException {
     File file = super.locate(name);
     if (Integer.MAX_VALUE < file.length()) {
       throw new IllegalArgumentException(file + ": file too large");
     }
     return file;
   }
 
   public Node parse(Reader in, File file) throws IOException, ParseException {
     JavaFiveParser parser =
       new JavaFiveParser(in, file.toString(), (int)file.length());
     Result result = parser.pCompilationUnit(0);
     return (Node)parser.value(result);
   }
   
   public void run(String[] args){
     treeManager = new InheritanceTreeManager(GNode.create("ObjectClassDeclaration")); 
     // This gets the class name from the command line of the root class. Fix this later, as it only supports one argument
     currentClassName = args[args.length - 1];
     
     super.run(args);
     //cppast.printAST();
   }
 
   public void process(Node node) {
     new Visitor() {
 
       public GNode visitBlock(GNode n) {
         //Visits a Block (a set of instructions in Java) and figures out how to translate it. Returns the GNode of the whole translated Block
         GNode block = GNode.create("Block");
         for(Object o : n){
           //dispatches each ExpressionStatement, ReturnStatement, etc.
           block.add(getValidGNode(dispatch(getValidGNode(o))));
         }
         return block;
       }
 
       public GNode visitCallExpression(GNode n) {
         // if this is a system call
         if (n.getGeneric(0).getGeneric(0).getString(0).equals("System")) {
           // if this is system.out
           if (n.getGeneric(0).getString(1).equals("out")) {
             // if this is system.out.print
             if (n.getString(2).equals("print")) {
               return cppast.addPrintExpression(null, n.getGeneric(3)); 
             }
           }
         }
        return n;
       }
 
       public void visitClassBody(GNode n){
         visit(n);
       }
         
       public void visitClassDeclaration(GNode n) {
         
         //Add the current class to the cppast, and set it as the current class global variable.
         currentClass = cppast.addClass(n.getString(1));
         parentClassNode = currentClass;
         
         //add the current class to the inheritance tree, but parent it to Object for now
         String[] qualified = n.getString(1).split("\\.");
         treeManager.insertClass(new ArrayList<String>(Arrays.asList(qualified)), null, currentClass);
         
         visit(n);
       }
       
       public void visitCompilationUnit(GNode n) {
         
         visit(n);
         //Print the AST after we're done for debugging
         cppast.printAST();
         try{
           new HeaderWriter(new Printer(new PrintWriter("out.h"))).dispatch(cppast.compilationUnit);
           new ImplementationPrinter(new Printer(new PrintWriter("out.cc"))).dispatch(cppast.compilationUnit);
         } catch (Exception e) {
           System.out.println("Uh oh... " + e);
         }
       }
       
       public void visitConstructorDeclaration(GNode n) {
         //Add a constructor to currentClass and get the associated GNode
         currentConstructor = cppast.addConstructor(currentClass);
         //If there are formal parameters for the constructor, visit them and add them to the currentConstructor
         if(n.getGeneric(4) != null) cppast.setConstructorParameters(getValidGNode(dispatch(n.getGeneric(4))), currentConstructor);
         //If there are instructions in the block, visit them and add them to the constructor
         if(n.getGeneric(5) != null) cppast.setConstructorInstructions(getValidGNode(dispatch(n.getGeneric(5))), currentConstructor);
       }
 
       public String visitDeclarator(GNode n) {
         //A declarator just needs to return the name of it right now
         return n.getString(0);
       }
       
       public GNode visitExtension(GNode n){
         
         // Assume the name of the parent is fully qualified
         visit(n);
         
         cppast.addAllInheritedMethods(parentClassNode.getGeneric(4), parentClassNode.getGeneric(5), currentClass);
         parentClassNode = currentClass;
         //add the current class to the inheritance tree, but parent it to Object for now
         ArrayList parentQualified = new ArrayList<String>(Arrays.asList(parentName.split("\\.")));
         ArrayList childQualified = new ArrayList<String>(Arrays.asList(currentClassName.split("\\.")));
         treeManager.reparent(childQualified, parentQualified);
         
         
         
         return null; 
       }
 
       public GNode visitExpressionStatement(GNode n) {
         //TODO: figure out what we need to do to the expressionstatements to make them parseable including fetching needed files
         return n;
       }
 
       public void visitExpression(GNode n){
         visit(n);
       }
                       
       public void visitFieldDeclaration(GNode n) {
         //Get the string by dispatching the Type GNode
         GNode type = (GNode)dispatch(n.getGeneric(1));
         //Create a new GNode to hold all of the declarators;
         GNode declarators = n.getGeneric(2);
         //Loop through all declarators in this field declaration, dispatch them, and add each returned string plus type as its own field to the currentClass
         //There may be multiple e.g. Java: double x,y,z; => C++: double x; double y; double z;
         for(int i = 0; i < declarators.size(); i++){
           String name = (String)dispatch(declarators.getGeneric(i));
           cppast.addField(name, type, currentClass);
         }
       }
       
       public GNode visitFormalParameter(GNode n){
         //Create a parameter by getting the name and dispatching the type
         GNode param = GNode.create("FormalParameter");
         param.add(n.getString(3));
         param.addNode((GNode)dispatch(n.getGeneric(1)));
         return param;
       }
  
       public GNode visitFormalParameters(GNode n){
         //Loop through all the params dispatching them and adding the result to a parameters GNode
         GNode parameters = GNode.create("FormalParameters");
         for(Object o : n){
           parameters.add(getValidGNode(dispatch(getValidGNode(o))));
         }
         return parameters;
       }
 
       public void visitNewClassExpression(GNode n) {       
         visit(n);
       }  
 
       public void visitMethodDeclaration(GNode n) {
       //TODO: math names and remove
       try{
         //Add a new method with name equiv to this method dec, dispatched type in the current class
         currentMethod = cppast.addMethod(n.getString(3), (GNode)dispatch(n.getGeneric(2)), currentClass);
         //Add the method params gotten by dispatching the formalParameters node
         cppast.setMethodParameters(getValidGNode(dispatch(n.getGeneric(4))), currentMethod);
         //Add the method block gotten by dispatching the block node
         cppast.setMethodInstructions(getValidGNode(dispatch(n.getGeneric(7))), currentMethod);
         } catch(Exception e) { e.printStackTrace(); }
       }
 
       public void visitStringLiteral(GNode n){
         visit(n);
       }
         
       public GNode visitVoidType(GNode n){
         GNode type = GNode.create("Type");
         type.addNode(GNode.create("PrimitiveIdentifier")).getGeneric(1).add("void");
         return type;
       }
         
       public GNode visitType(GNode n) {
         //Determine the type translated into C++ using Type.primitiveType(String) and Type.qualifiedIdentifier(String)
         GNode identifier = n.getGeneric(0);
         String typename = identifier.getString(0);
         
         if(identifier.hasName("PrimitiveIdentifier")){
           GNode type = GNode.create("Type");
           type.addNode(GNode.create("PrimitiveIdentifier")).getGeneric(1).add(Type.primitiveType(typename));
           return type;
           
           // Fix this later in treeManager
         } else if ( typename.equals("String") || typename.equals("Class") || typename.equals("Object") ) {
           GNode type = GNode.create("Type");
           type.add((new Disambiguator()).disambiguate(typename));
           return type;
         } else {
           
           System.err.println("Split: " + typename.split("\\.").length);
           System.err.println("Adding typename: " + typename);
           String[] qualified = typename.split("\\.");
           
           // Reset currentClassName when we come back
           String tempClassName = currentClassName;
           currentClassName = typename;
           
           GNode tempClass = currentClass;
           
           parentName = typename;
 
           // disambiguate() - figure out the fully qualified name
           // Later we'll keep track of already-imported types,
           // and we'll automatically skip those or expand them
           // as necessary
           // For now we'll support only explicitly qualified name: "qimpp.Foo" ["qimpp", "Foo"]
           GNode classTreeNode = treeManager.dereference(new ArrayList(Arrays.asList(qualified)));
           if (classTreeNode == null){
               
               try{
                 process(typename.replace(".", "/")+".java");
               }
 
               catch (Exception e){
                 System.err.println("Cannot parse " + typename + " " + e);
                 System.exit(1);
               }
               // Fail and crash with error if the file cannot be located
 
           }
           
           currentClassName = tempClassName;
           currentClass = tempClass;
           
           GNode type = GNode.create("Type");
           type.add((new Disambiguator()).disambiguate(typename));
           return type;
         }
       }
       
       public GNode visitReturnStatement(GNode n) {
         return n;
       }
  
       public void visit(Node n) {
         System.err.println("We are currently running " + currentClassName);
         for (Object o : n) if (o instanceof Node) dispatch((Node)o);
       }
       
       //Takes an object, if it's a GNode returns it otherwise puts the object on a container with the name "something went wrong"
       public GNode getValidGNode(Object o){
         if(o instanceof GNode)
           return (GNode)o;
         else{
           GNode container = GNode.create("Something went wrong parsing");
           container.add(o);
           return container;
         }
       }
 
     }.dispatch(node);
   }
 
   /**
    * Run the translator with the specified command line arguments.
    *
    * @param args The command line arguments.
    */
   public static void main(String[] args) {
     new QimppTranslator().run(args);   
   }
 
 }
