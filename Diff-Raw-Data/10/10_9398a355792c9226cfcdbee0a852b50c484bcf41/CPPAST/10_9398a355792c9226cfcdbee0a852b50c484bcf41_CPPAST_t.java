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
 
 /**
  * A translator from (a subset of) Java to (a subset of) C++.
  *
  * @author QIMPP
  * @version $Revision$
  */
 public class CPPAST {
   public GNode compilationUnit, directives, declarations, classes;
   HashMap<String, GNode> classesMap; 
   
   public CPPAST(){
       compilationUnit = GNode.create("CompilationUnit");
       compilationUnit.addNode(createDefaultDirectives());
       declarations = GNode.create("Declarations");
       compilationUnit.addNode(declarations);
       classes = GNode.create("Classes");
       compilationUnit.addNode(classes);
       classesMap = new HashMap<String, GNode>();
       System.out.println("Created new CPPAST");
   }
   
   GNode createDefaultDirectives(){
     directives = GNode.create("Directives");
     directives.addNode(GNode.create("Pragma")).getGeneric(directives.size()-1).add("once");
    
     GNode includeDirectives = GNode.create("IncludeDirectives");
     includeDirectives.addNode(GNode.create("QuotedForm")).getGeneric(includeDirectives.size()-1).add("java_lang");
     includeDirectives.addNode(GNode.create("AngleBracketForm")).getGeneric(includeDirectives.size()-1).add("stdint");
     
     directives.addNode(includeDirectives);
     return directives;
   }
   
   /*Methods to add:
    *GNode addClass(String name) - adds a class and a struct to the tree returns a GNode to the class
    *GNode addField(String name, String type, GNode class) - adds a field to a class returns the GNode of the field
    *GNode addMethod(String name, String returnType, GNode class) - adds a method to a class returns the method GNode
    *GNode addMethodInstruction(GNode instruction, GNode method) - adds instruction to method
    *GNode addMethodParameter(String paramType, String param, GNode method) - adds a parameter to method
    *
    *GNode getClass(String name) - gets a GNode to a class by it's name
    *GNode getMethod(String name, GNode* class) - gets a GNode to a method by it's name
    *GNode getField(String name, GNode* class) - gets a GNode to a Field by it's name
    *
    *void removeMethod(String name, GNode* class) - removes a method from a class
    
    *void printAST() - prints the AST for debugging
   */
   
   GNode addClass(String name, GNode parent){
     System.out.println("Adding class " + name);
   
     //Add to Structs
     GNode declaration = GNode.create("Declaration");
     declaration.add(name);
     declaration.addNode(GNode.create("Struct"));
     declaration.add(2, null);
     declarations.addNode(declaration);
     
     //Add to Classes with Name node, Fields node, ImplementedMethods node, and InheritedMethods node
     GNode classNode = GNode.create("ClassDeclaration");
     classNode.add(name);
     if(parent != null){
       classNode.addNode(GNode.create("Parent")).getNode(classNode.size()-1).add(parent);
     } else {
       classNode.addNode(GNode.create("Parent")).getNode(classNode.size()-1).add(parent);
     }
     classNode.addNode(GNode.create("Constructors"));
     classNode.addNode(GNode.create("Fields"));
     classNode.addNode(GNode.create("ImplementedMethods"));
     classNode.addNode(GNode.create("InheritedMethods"));
     classes.addNode(classNode);
     return classNode;
   }
   
   GNode addClass(String name){
     return addClass(name, null);
   }
   
   //Adding, getting, and removing fields
   
   GNode addField(String name, GNode type, GNode classNode){
     //Get the fields node
     GNode fieldNode = GNode.create("FieldDeclaration");
     fieldNode.add(name);
     fieldNode.addNode(type);
     classNode.getGeneric(3).addNode(fieldNode);
     return fieldNode;
   }
   
   GNode addConstructor(GNode classNode){
     GNode constructorNode = GNode.create("ConstructorDeclaration");
     constructorNode.add(null);
     constructorNode.addNode(GNode.create("Block"));
     classNode.getGeneric(2).addNode(constructorNode);
     return constructorNode;
   }
 
   void addConstructorInstruction(GNode instruction, GNode constructor) {
     constructor.getGeneric(1).addNode(instruction);
   }
   
   GNode addConstructorParameter(GNode paramType, String param, GNode constructor) {
     if(constructor.getGeneric(0) == null) constructor.add(0, GNode.create("Parameters"));
     GNode formalParameter = GNode.create("FormalParameter");
     formalParameter.add(param);
     formalParameter.addNode(paramType);
     constructor.getGeneric(0).addNode(formalParameter);
     return formalParameter;
   }
   
   void setConstructorParameters(GNode parameters, GNode constructor){
     constructor.remove(2);
     constructor.add(2, parameters);
   }
   
   void setConstructorInstructions(GNode block, GNode constructor){
     constructor.remove(1);
     constructor.add(1, block);
   }
     
   GNode addMethod(String name, GNode returnType, GNode classNode) {
     if(classNode.getGeneric(5).size() == 0){
       addAllInheritedMethods(GNode.create("ImplementedMethods"), generateObjectMethods(), classNode);
     }
   
     GNode methodNode = GNode.create("MethodDeclaration");
     methodNode.add(name);
     methodNode.addNode(GNode.create("ReturnType"));
     System.out.println(returnType);
     methodNode.getGeneric(1).add(returnType.getGeneric(0));
     System.out.println(methodNode.getGeneric(1));
     methodNode.addNode(GNode.create("FormalParameters"));
     methodNode.addNode(GNode.create("Block"));
     classNode.getGeneric(4).addNode(methodNode);
     return methodNode;
   }
   
   GNode addMethod(String name, GNode returnType, GNode classNode, String from) {
     GNode methodNode = GNode.create("MethodDeclaration");
     methodNode.add(name);
     methodNode.addNode(GNode.create("ReturnType")).getGeneric(methodNode.size()-1).add(returnType.getGeneric(0));
     methodNode.add(GNode.create("FormalParameters"));
     methodNode.addNode(GNode.create("From")).getNode(methodNode.size()-1).add(from);
     classNode.getGeneric(5).addNode(methodNode);
     return methodNode;
   }
   
   void addMethodInstruction(GNode instruction, GNode method) {
     method.getGeneric(3).addNode(instruction);
   }
   
   void setMethodInstructions(GNode block, GNode method) {
     method.remove(3);
     method.add(3, block);
   }
   
   GNode addMethodParameter(GNode paramType, String param, GNode method) {
     GNode formalParameter = GNode.create("FormalParameter");
     formalParameter.add(param);
     formalParameter.addNode(paramType);
     method.getGeneric(2).addNode(formalParameter);
     return formalParameter;
   }
   
 
   void addAllInheritedMethods(GNode implementedMethods, GNode inheritedMethods, GNode currentClass){
     for(int i = 0; i < implementedMethods.size(); i++){
       implementedMethods.getGeneric(i).remove(3);
       implementedMethods.getGeneric(i).add(3,GNode.create("From")).getGeneric(3).addNode(currentClass.getGeneric(1));
       currentClass.getGeneric(5).addNode(implementedMethods.getGeneric(i));
     }
     for(int i = 0; i < inheritedMethods.size(); i++){
       currentClass.getGeneric(5).addNode(inheritedMethods.getGeneric(i));
     }
   }
   
  GNode addPrintExpression(String option, GNode args) {
     GNode print = GNode.create("PrintExpression");
     print.add("cout");
     print.add(GNode.create("Option", option));
     print.add(args);
     return print;
   }
   
   void setMethodParameters(GNode parameters, GNode method){
     method.remove(2);
     method.add(2, parameters);
   }
   
   GNode getClass(String name) {
       return classesMap.get(name);
   }
   
   //Returns the index of a field with name in the class given by classNode
   int getFieldIndex(String name, GNode classNode){
     GNode fieldsOfClass = classNode.getGeneric(1);
     for(int i = 0; i < fieldsOfClass.size(); i++){
       if(getGNodeName(fieldsOfClass.getGeneric(i)).equals(name))
         return i;
     }
     return -1;
   }
   
   void removeField(String name, GNode classNode){
     int fieldIndex = getFieldIndex(name, classNode);
     if(fieldIndex != -1) classNode.getGeneric(1).remove(fieldIndex);
   }
   
   
   //Adding, getting, and removing methods  
   int getInheritedMethodIndex(String name, GNode classNode) {
     GNode inheritedMethods = classNode.getGeneric(4);
     for(int i=0; i < inheritedMethods.size(); i++){
       if(getGNodeName(inheritedMethods.getGeneric(i)).equals(name))
         return i;
     }
     return -1;      
   }
   
   
   void removeInheritedMethod(String name, GNode classNode) {
     int methodIndex = getInheritedMethodIndex(name, classNode);
     if(methodIndex != -1) classNode.getGeneric(5).remove(methodIndex);
   }
   
   //Utility methods
   GNode generateObjectType(){
     GNode qi = GNode.create("QualifiedIdentifier");
     qi.add("java").add("lang").add("Object");
     return (GNode)((GNode.create("Type")).add(qi));
   }
   
   GNode generateObjectMethods(){
     GNode objectMethods = GNode.create("ObjectMethods");
     GNode objectType = generateObjectType();
     
     //Hashcode
     GNode objectMethod = GNode.create("MethodDeclaration");
     objectMethod.add("hashCode");
     objectMethod.add(GNode.create("ReturnType")).getGeneric(1).add(GNode.create("PrimitiveType")).getGeneric(0).add("int32_t");
     objectMethod.addNode(GNode.create("FormalParameters"));
     objectMethod.addNode(GNode.create("From")).getGeneric(3).addNode(objectType);
     objectMethods.add(objectMethod);
     
     //equals
     objectMethod = GNode.create("MethodDeclaration");
     objectMethod.add("equals");
     objectMethod.add(GNode.create("ReturnType")).getGeneric(1).add(GNode.create("PrimitiveType")).getGeneric(0).add("bool");
     objectMethod.addNode(GNode.create("FormalParameters")).getGeneric(2).addNode(GNode.create("FormalParameter")).getGeneric(0).add("obj").addNode(objectType);
     objectMethod.addNode(GNode.create("From")).getGeneric(3).addNode(objectType);
     objectMethods.add(objectMethod);
     
     //getClass
     objectMethod = GNode.create("MethodDeclaration");
     objectMethod.add("getClass");
     objectMethod.add(GNode.create("ReturnType")).getGeneric(1).add(GNode.create("QualifiedIdentifier")).getGeneric(0).add("java").add("lang").add("Class");
     objectMethod.addNode(GNode.create("FormalParameters"));
     objectMethod.addNode(GNode.create("From")).getGeneric(3).addNode(objectType);
     objectMethods.add(objectMethod);
     
     //toString
     objectMethod = GNode.create("MethodDeclaration");
     objectMethod.add("toString");
     objectMethod.add(GNode.create("ReturnType")).getGeneric(1).add(GNode.create("QualifiedIdentifier")).getGeneric(0).add("java").add("lang").add("String");
     objectMethod.addNode(GNode.create("FormalParameters"));
     objectMethod.addNode(GNode.create("From")).getGeneric(3).addNode(objectType);
     objectMethods.add(objectMethod);
     
     return objectMethods;
   }
   
   String getGNodeName(GNode n){
     return n.getString(0);
   }
   
   public void printAST(){
     Printer p = new Printer(System.out);
     p.format(compilationUnit).flush();
   }
 }
