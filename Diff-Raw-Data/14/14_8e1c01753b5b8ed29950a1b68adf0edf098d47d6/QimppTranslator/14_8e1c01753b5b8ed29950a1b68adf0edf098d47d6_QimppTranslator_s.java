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
 import java.util.HashMap;
 
 import xtc.lang.JavaFiveParser;
 
 import xtc.parser.ParseException;
 import xtc.parser.Result;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 import xtc.tree.Location;
 import xtc.tree.Printer;
 
 import xtc.util.Tool;
 
 import qimpp.SymbolTable;
 
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
   String currentPackageName;
   String parentName;
   CPPAST cppast;
   InheritanceTreeManager treeManager;
   GNode root;
 
   HashMap<String, String> currentNameMap;
 
   boolean inBlock;
 
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
     // Create a hashmap to hold maps of ambiguous names to unambiguous names
     currentNameMap = new HashMap<String, String>();
     currentPackageName = "";
 
     /** SYMBOL TABLE */
     SymbolTable table = new SymbolTable();
     table.incorporate(node);
     // Now we can call .getProperty("qimpp.Constants.SCOPE") on certain
     // scope-defining nodes and we'll get back a Scope object (look in
     // SymbolTable).
     
     // First, get contextual information with an initial visit of types and the package declaration
     
     Visitor initialVisitor = new Visitor () {
 
         public void visitClassDeclaration(GNode n) {
         
           //Add the current class to the cppast, and set it as the current class global variable.
           String qualifiedClassName = currentPackageName + "." + n.getString(1);
           currentClass = cppast.addClass(qualifiedClassName);
           currentClassName = qualifiedClassName;
           parentClassNode = currentClass;
 
           currentNameMap.put(n.getString(1), qualifiedClassName);
           
           //add the current class to the inheritance tree, but parent it to Object for now
           String[] qualifiedArray = qualifiedClassName.split("\\.");
           treeManager.insertClass(new ArrayList<String>(Arrays.asList(qualifiedArray)), null, currentClass);
          
           System.err.println("Inserted class in tree manager ");
           System.err.println(new ArrayList<String>(Arrays.asList(qualifiedArray))); 
           visit(n);
 
         }
 
         /** Set the current package name context */
         public void visitPackageDeclaration(GNode n){
 
           GNode qualifiedIdentifier = n.getGeneric(1);
           StringBuilder packageNameBuilder = new StringBuilder();
 
           for ( int i = 0; i < qualifiedIdentifier.size(); i++ ){
             packageNameBuilder.append(qualifiedIdentifier.getString(i));
             if ( i < qualifiedIdentifier.size() - 1 ) {
               packageNameBuilder.append(".");
             }
           }
 
           currentPackageName = packageNameBuilder.toString();
         }
 
         //TODO: * imports
         /** Handle explicit imports */
         public void visitImportDeclaration(GNode n){
           //  Assuming it's not a * import
           GNode qualifiedIdentifier = n.getGeneric(1);
           StringBuilder qualifiedNameBuilder = new StringBuilder();
           String unqualifiedName = "";
 
           for ( int i = 0; i < qualifiedIdentifier.size(); i++ ) {
             qualifiedNameBuilder.append(qualifiedIdentifier.getString(i));
 
             if ( i < qualifiedIdentifier.size() - 1 ) {
               qualifiedNameBuilder.append(".");
             }
             else {
               unqualifiedName = qualifiedIdentifier.getString(i);
             }
           }
 
           // Add the disambiguation to the hashmap
           currentNameMap.put(unqualifiedName, qualifiedNameBuilder.toString());
           System.err.println("MAPPED: " + unqualifiedName);
         }
 
         /** Adds the field to the CPPAST */
         public void visitFieldDeclaration(GNode n) {
           //Get the string by dispatching the Type GNode
           
           
           GNode type = (GNode)dispatch(n.getGeneric(1));
           //Create a new GNode to hold all of the declarators;
           GNode declarators = n.getGeneric(2);
 
           //Loop through all declarators in this field declaration, dispatch them, and add each returned string plus type as its own field to the currentClass
           //There may be multiple e.g. Java: double x,y,z; => C++: double x; double y; double z;
           for(int i = 0; i < declarators.size(); i++){
             String name = (String)dispatch(declarators.getGeneric(i));
             if (!inBlock) {
              cppast.addField(currentClass.getString(0) + "_" + name, type, currentClass);
             }
           }
         }
 
         public void visitMethodDeclaration(GNode n) {
           //TODO: math names and remove
           try{
             String methodName = n.getString(3);
             if (methodName.equals("main")) {
               n.set(2, GNode.create("Type", GNode.create("PrimitiveType", "int"), null));
 
             }
             GNode returnType = (GNode)dispatch(n.getGeneric(2));
             currentMethod = cppast.addMethod(methodName, returnType, currentClass);
             //Add the method params gotten by dispatching the formalParameters node
             cppast.setMethodParameters((GNode)dispatch(n.getGeneric(4)), currentMethod);
             //Add the method block gotten by dispatching the block node
 
           //System.out.println("getGeneric(7): " + n.getGeneric(7));
           //System.out.println(dispatch(n.getGeneric(7)));
           GNode block = n.getGeneric(7);
           cppast.setMethodInstructions(block, currentMethod);
            
           } catch(Exception e) { e.printStackTrace(); }
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
             parameters.add((GNode)dispatch((GNode)o));
           }
           return parameters;
         }
 
         /** Visit all types. If it is unknown, process the file for that type. If it is known, fully qualify it */
         public GNode visitType(GNode n) {
           //Determine the type translated into C++ using Type.primitiveType(String) and Type.qualifiedIdentifier(String)
           visit(n);
           GNode identifier = n.getGeneric(0);
           String typename = identifier.getString(0);
 
           if(identifier.hasName("PrimitiveType")){
             return n;
           }
           // Fix this later in treeManager
           else if ( typename.equals("String") || typename.equals("Class") || typename.equals("Object") ) {
             GNode type = GNode.create("Type");
             type.add(Disambiguator.disambiguate(typename));
             return type;
           } 
           // If the name is our name map, then replace it with the fully qualified name
           
           else {
             if ( currentNameMap.get(typename) != null ) {
                // Create a QualifiedIdentifier node for the typename using the now obviously ill-named Disambiguator
                //TODO: Give Disambiguator a name that reflects its purpose, which is just to construct a type node from a name.
                // Maybe stick that function in CPPAST
 
                String qualifiedName = currentNameMap.get(typename);
                GNode qualifiedIdentifierNode = Disambiguator.disambiguate(qualifiedName);
                n.set(0, qualifiedIdentifierNode);
                System.err.println("REPLACING REFERENCE: " + qualifiedName);
 
                typename = qualifiedName;
             }
             
 
             //System.err.println("Split: " + typename.split("\\.").length);
             //System.err.println("Adding typename: " + typename);
             String[] qualified = typename.split("\\.");
             
             // Reset currentClassName when we come back
             String tempClassName = currentClassName;
             currentClassName = typename;
             
             String tempPackageName = currentPackageName;
             GNode tempClass = currentClass;
 
             HashMap<String, String> tempNameMap = currentNameMap;
             
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
                   // If we can't find it in the source root, then it must be a reference to a file in the current package
                   try {
                      String currentPackageQualifiedTypename = currentPackageName + "." + typename;
                      currentNameMap.put(typename, currentPackageQualifiedTypename);
                      process(currentPackageQualifiedTypename.replace(".", "/")+".java");
                      
                      // Copy-paste yay...
                      String qualifiedName = currentNameMap.get(typename);
                      GNode qualifiedIdentifierNode = Disambiguator.disambiguate(qualifiedName);
                      n.set(0, qualifiedIdentifierNode);
                   }
                   catch (Exception f){
                     System.err.println("Cannot parse " + typename + " " + e);
                     cppast.printAST();
                     e.printStackTrace();
                     System.exit(1);
                   }
                 }
                 // Fail and crash with error if the file cannot be located
 
             }
             
             currentClassName = tempClassName;
             currentPackageName = tempPackageName;
             currentNameMap = tempNameMap;
             currentClass = tempClass;
 
             return n;
 
           }
 
         }
 
         public void visitPrimaryIdentifier(GNode n) {
           // Check if this is a ambiguous name, and if it is, replace it with the fully qualified name
           String name = n.getString(0);
           if (currentNameMap.get(name) != null){
             n.set(0, currentNameMap.get(name));
           }
         }
 
         public void visit(Node n) {
           
           //System.err.println("We are currently running " + currentClassName);
           //
           for (Object o : n) if (o instanceof Node) dispatch((Node)o);
 
         }
 
 
     };
 
     initialVisitor.dispatch(node);
 
     new Visitor() {
 
       public GNode visitBlock(GNode n) {
         //Visits a Block (a set of instructions in Java) and figures out how to translate it. Returns the GNode of the whole translated Block
         inBlock = true;
         GNode block = GNode.create("Block");
         
         /*
         for (Object o : n) {
           //dispatches each ExpressionStatement, ReturnStatement, etc.
         } */
         visit(n);
         //for (Object o : n) {
         //}
         //TODO: CHNANGE THIS BACK
         // return block;
         inBlock = false;
 
         return n;
       }
 
       /*public GNode visitCallExpression(GNode n) {
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
       }*/
 
       public void visitClassBody(GNode n){
         visit(n);
       }
         
       
       
       public void visitCompilationUnit(GNode n) {
         root = n;     
         System.out.println("In QinppTranslator:visitCompilationUnit before visit(n)");
         visit(n);
         System.out.println("In QinppTranslator:visitCompilationUnit after visit(n)");
         //Print the AST after we're done for debugging
         //cppast.printAST();
         try{
           new HeaderWriter(new Printer(new PrintWriter("out.h"))).dispatch(cppast.compilationUnit);
           cppast.printAST();
           new ImplementationPrinter(new Printer(new PrintWriter("out.cc"))).dispatch(cppast.compilationUnit);
         } catch (Exception e) {
           //System.out.println("Uh oh... " + e);
           e.printStackTrace();
         }
       }
       
       public void visitConstructorDeclaration(GNode n) {
         //Add a constructor to currentClass and get the associated GNode
         currentConstructor = cppast.addConstructor(currentClass);
         //If there are formal parameters for the constructor, visit them and add them to the currentConstructor
         if(n.getGeneric(4) != null) cppast.setConstructorParameters((GNode)dispatch(n.getGeneric(4)), currentConstructor);
         //If there are instructions in the block, visit them and add them to the constructor
         if(n.getGeneric(5) != null) cppast.setConstructorInstructions((GNode)dispatch(n.getGeneric(5)), currentConstructor);
       }
 
      public String visitDeclarator(GNode n) {
        //A declarator just needs to return the name of it right now
        return n.getString(0);
      }
       
       public GNode visitExtension(GNode n){
         
         // Assume the name of the parent is fully qualified
         visit(n);
 
         GNode parentNameQualifiedIdentifier = n.getGeneric(0).getGeneric(0);
         parentName = Disambiguator.getDotDelimitedName(parentNameQualifiedIdentifier);
         
         currentClass.getGeneric(1).getGeneric(0).remove(0);
         String parentNameQualified = parentClassNode.getString(0);
         
         // Add the parent's type to the current class's Parent Node
         currentClass.getGeneric(1).getGeneric(0).addNode( Disambiguator.disambiguate(parentNameQualified));
         
         // Add inherited mehthods and fields using the parent's class
         // TODO: Refactor here. Implemented and inherited methods should be interspersed, and there
         // should only be one argument to addAllInheritedMethods
         cppast.addAllInheritedMethods(parentClassNode.getGeneric(4), currentClass);
 
         cppast.addAllInheritedFields(parentClassNode, currentClass); 
         parentClassNode = currentClass;
         
         //add the current class to the inheritance tree, but parent it to Object for now
         ArrayList parentQualified = new ArrayList<String>(Arrays.asList(parentName.split("\\.")));
         ArrayList childQualified = new ArrayList<String>(Arrays.asList(currentClassName.split("\\.")));
         System.err.println(parentQualified);
         System.err.println(childQualified);
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
                       
       public void visitNewClassExpression(GNode n) {       
         visit(n);
       }  
 
       public void visitPackageDeclaration(GNode n){
         System.out.println("Package " + dispatch(n.getGeneric(1)));
       }
 
       public void visitStringLiteral(GNode n){
         visit(n);
       }
         
       public GNode visitVoidType(GNode n){
         GNode type = GNode.create("Type");
         type.addNode(GNode.create("PrimitiveType")).getGeneric(0).add("void");
         return type;
       }
         
       /** Change primitive type names from Java to C names */
       public void visitPrimitiveType(GNode n) {
         String javaType = n.getString(0);
         String cppType = Type.primitiveType(javaType);
         n.set(0, cppType);
       }
       
       public GNode visitType(GNode n) {
         //Determine the type translated into C++ using Type.primitiveType(String) and Type.qualifiedIdentifier(String)
         visit(n);
         GNode identifier = n.getGeneric(0);
         String typename = identifier.getString(0);
 
         if(identifier.hasName("PrimitiveType")){
           return n;
         }
 
         // Fix this later in treeManager
         else if ( typename.equals("String") || typename.equals("Class") || typename.equals("Object") ) {
           GNode type = GNode.create("Type");
           type.add( Disambiguator.disambiguate(typename));
           return type;
         } 
 
         else {
 
           GNode type = GNode.create("Type");
           type.add(Disambiguator.disambiguate(typename));
           return type;
 
         }
       }
       
       public GNode visitReturnStatement(GNode n) {
         return n;
       }
  
       public void visit(Node n) {
         //System.err.println("We are currently running " + currentClassName);
         //
         for (Object o : n) if (o instanceof Node) dispatch((Node)o);
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
