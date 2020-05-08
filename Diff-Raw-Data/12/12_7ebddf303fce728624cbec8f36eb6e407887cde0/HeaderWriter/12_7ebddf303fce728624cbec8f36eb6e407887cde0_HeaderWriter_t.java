 package qimpp;
 import qimpp.Type;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.PrintWriter;
 import java.util.Iterator;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 import xtc.tree.Location;
 import xtc.tree.Printer;
 
 import xtc.util.Tool;
 
 /** A generator for C++ class header files from a Java syntax tree
 *  
 *   This class handles inheritance, function declarations, and vtable generation.
 *   @author QIMPP
 */
 public class HeaderWriter extends Visitor {
   
   private Printer printer;
   private ArrayList<GNode> inherited_methods;
   private ArrayList<GNode> implemented_methods;
   private ArrayList<GNode> fields;
   private boolean inherited;
   //private String current_class;
   /** Constructor. Opens a new file called defined_classes.h
   *
   * The GNode passed in should contain a modified tree created by InheritanceManager.
   * HeaderGenerator should just format and print the fields and methods it is given,
   * determining which methods, order of fields, inheritance of fields etc. should not be
   * done here.
   *
   * A single header for all implemented classes is desirable as there may be type
   * cross-references in the defined types (Point may contain a Color, and Color may contain
   * a method returning a Point, so they must know about each other through
   * forward-declarations in C++)
   *
   *
   * Useful Notes:
   *
   * ClassBody is contained in roots[index].getGeneric(2)
   * Use .indent() to indent when needed. The Printer doesn't do it automatically. 
   * Use .incr() to increase indentation, and .decr() to decrease intentation.
   * ATTN: must call .incr() before you can call .indent()! Otherwise .indent() will do nothing 
   * TODO: Packages
   * 
   *@param roots The QimppClassDeclaration nodes for the classes we want to create a header for
   */
   
   // TODO: Need to change the HeaderWriter to take a GNode, instead of an array of GNodes
   
   public HeaderWriter(Printer printer) {
     this.printer = printer;
     inherited_methods = new ArrayList<GNode>();
     implemented_methods = new ArrayList<GNode>();
     fields = new ArrayList<GNode>();
     inherited = false;
     //current_class = "";
     printer.register(this);  
   }
   
  // ===================
  //  VISITOR
  // ==================
 
   GNode compilationUnit;
   public void visitCompilationUnit(GNode n){
     compilationUnit = n;
     writeDependencies(); 
     visit(n);
     printer.flush();
   }
 
   public void visitDeclarations(GNode n){
     visit(n);
   }
 
   public void visitDeclaration(GNode n){
     String[] qualifiers = getNameQualifiedArray(n);
     // Declare the types in the correct namespaces
     for ( int i = 0; i < qualifiers.length - 1; i++ ) {
       indentOut().p("namespace ").p(qualifiers[i]).pln(" {");
       printer.incr();
     }
 
     writeTypeDeclaration(n);
     writeAlias(n);
 
     for ( int i = 0; i < qualifiers.length - 1; i++ ) {
       printer.decr();
       indentOut().pln("}");
     }
   }
 
   public void visitClasses(GNode n){
     System.out.println("===in classes , size: " +  n.size());
     for (int i = n.size() - 1 ; i >= 0; i--) {
       System.out.println("===node : " + i);
       dispatch(n.getGeneric(i));
     }
   }
   
   /** State variable to make sure we get ordering done right */
   HashMap<String, Boolean> doneClass;
 
   public void visitClassDeclaration(GNode n){
     //current_class = name(n);
     if (doneClass == null){
       doneClass = new HashMap<String, Boolean>();
     }
     String name = n.getString(0);
     // Print out classes in inheritance order
     if (doneClass.get(name) == null) {
       doneClass.put(name, true);
       if (n.getProperty("ParentClassNode") != null){
         visitClassDeclaration((GNode)n.getProperty("ParentClassNode"));
       }
       try{ 
 
         visit(n);
 
         // Write out the namespace of the class
         String[] qualifiedType = getNameQualifiedArray(n);
         for ( int i = 0; i < qualifiedType.length - 1; i++ ) {
           indentOut().pln("namespace " + qualifiedType[i] + " {");
           printer.incr();
         }
 
         
         writeStruct(n);
         writeVTStruct(n);
 
         inherited_methods.clear();
         implemented_methods.clear();
         fields.clear();
 
         // Write out the namespace of the class
         for ( int i = 0; i < qualifiedType.length - 1; i++ ) {
           printer.decr();
           indentOut().pln("}");
         }
 
         new ArrayTemplatePrinter(printer).dispatch(n);
 
       //current_class = "";
       } catch ( Exception e) { e.printStackTrace(); }
     }
   }
 
   public void visitFields(GNode n){
     visit(n);
   }
 
   public void visitFieldDeclaration(GNode n){
     fields.add(n); 
   }
 
   public void visitInheritedMethodContainer(GNode n){
     inherited = true;
     inherited_methods.add(n);
     inherited = false;
   }
 
   //TODO: Hack
   //Prints out main methods for other classes differently
   boolean didMain = false;
   public void visitImplementedMethodDeclaration(GNode n){
     if (!name(n).equals("main") || didMain) {
       if (inherited)
         inherited_methods.add(n);
       else
         implemented_methods.add(n);
     }
     else {
       didMain = true;
     }
   }
 
   public void visit(GNode n){
     System.out.println("Visiting " + n.getName());
     for (Object o : n) if (o instanceof Node) dispatch((Node)o);
   }
 
 // ================================
 // WRITE DEPENDENCIES + DECLARATIONS
 // ================================
 
   /** Write out the dependencies for the header */
   //TODO: this method should probably do more. Not sure ATM.
   private void writeDependencies() {
     printer.p("#pragma once").pln();
     printer.p("#include \"java_lang.h\"").pln() 
       .p("#include <stdint.h>").pln()
       .p("#include \"qimpp_utils.h\"").pln()
       .pln();
   }
   
   /** Write out the internal names of the structs and vtables for each class 
   *
   * @param index the index of the class we are writing
   */
   public void writeTypeDeclaration(GNode node){
     indentOut().p("struct ").p("__").p(name(node)).p(";\n");
     indentOut().p("struct ").p("__").p(name(node)).p("_VT;\n").pln();
 
     
   }
   
   /** Write out the typedefs so pretty-printing class names is easier on the programmer and
   * the eyes 
   *
   * @param node the node being examined
   */
   private void writeAlias(GNode node){
       printer.p("typedef __rt::Ptr<__").p(name(node)).p(" > ").p(name(node));
     printer.p(";\n").pln();
   }
  
 // ===================
 //  WRITE STRUCT
 // ===================
 
   /** Write out the struct definition for a given class, with all its newly defined methods 
   *  
   * @param node  the node being written
   */
   // Using java_lang.h as a basis, NOT skeleton.h
   private void writeStruct(GNode n){
     try{
     indentOut().p("struct __").p(name(n)).p(" {\n");
     printer.pln();
     printer.incr();
       writeVPtr(n);
       writeFields();
       printer.pln();
       writeConstructor(n);
       printer.pln();
       writeMethods(n);
       printer.pln();
       writeClass();
       printer.pln();
       writeVTable(n); 
     printer.decr();
     indentOut().p("};\n").pln();
     }catch(Exception e) { e.printStackTrace(); }
   }
   
   private void writeVPtr(GNode node){
     indentOut().p("__").p(name(node)).p("_VT* __vptr;\n");
   }
  
   /** 
    * The constructor.
    *
    * @param index the index of the class we are writing.
    */ 
   private void writeConstructor(GNode node){
     indentOut().p("__").p(name(node)).p("(");
 
     // Now for parameters:
     // Iterate through initializing types and fields. Use Type.
 
     // For now, assuming only Object, therefore no parameters in constructor.
     
     printer.p(");\n");
   }
  
 
   /**
    * Write the fields that belong each type of object. For example, String
    * has <code>std::string data</code>, and Class has <code>String name</code>
    * and <code>Class parent</code>.
    *
    * @param index The index of the class we are writing.
    */
   private void writeFields() {
     //Interate through the FieldDeclarations
     for (GNode f : fields) {
       writeField(f);
     }
     /** 
     for(Iterator<Object> iter = node.getGeneric(2).iterator(); iter.hasNext();){
       Object objCurrent = iter.next();
       if(objCurrent == null || objCurrent instanceof String) continue;
       GNode current = (GNode) objCurrent;
       if(current.hasName("FieldDeclaration"))
         //For now just get the first field declared
         indentOut().p(Type.translate(current)).p(" ").p(current.getGeneric(2).getGeneric(0).getString(0)).p(";").pln();
     } */
   }
   
   private String getTypeDirect(GNode n, boolean isPointer){
     GNode newNode = GNode.create("FakeNodeName", "Fake", n);
     return getType(newNode, isPointer);
   }
   
   private String getType(GNode n, boolean isPointer) {
     GNode type = n.getGeneric(1).getGeneric(0);
     String ret = "";
     if (type.getName().equals("PrimitiveType")) {
       ret = Type.primitiveType(type.getString(0));
     }
     else if (type.getName().equals("QualifiedIdentifier")) {
       if (type.size() == 1 && isPointer == false)
         ret = "__" + type.getString(0);
       for (Object id : type) {
         if (type.indexOf(id) == 0) 
           ret += type.getString(type.indexOf(id));
         else if (type.indexOf(id) == type.size() - 1 && isPointer == false)
           ret += "::__" + type.getString(type.indexOf(id));
         else 
           ret += "::" + type.getString(type.indexOf(id));
       }
     }
     
     // HACK
     else if (type.getName().equals("Type")){
       ret = getTypeDirect(type, isPointer);
     }
     
     else {
       System.err.println("getType on : " + n.toString());
     }
 
     //If the type has a dimension array and it's not null, then add in the dimensions. If it's the return type for a java.lang.Object method (constructed by hand) it won't have a null at 1 index, so we have to check for that.
     GNode dimensions = (n.getGeneric(1).size() > 1) ? n.getGeneric(1).getGeneric(1) : null;
     if(dimensions != null){
       ret = "__rt::Array<" + ret + ">*"; 
     }
 
     return (ret.length()!=0) ? ret : "NOT A VALID TYPE";
   }
 
   private void writeField(GNode n) {
     String type = getType(n, true); 
     indentOut().p(type).p(" ").p(getFieldPrefix(n)).p(";\n"); 
   }
 
   private void writeMethods(GNode n){
     String current_class = name(n);
     for (GNode m : implemented_methods) {
       writeMethod(m, current_class);
     } 
   }
 
   private void writeMethod(GNode n, String current_class){
     indentOut().p("static ");
     printer.p(getType(n, true)).p(" ");
     printer.p(Type.getCppMangledMethodName(n)).p("(").p(current_class);
     // visit params 
     new Visitor() {
   
       public void visitFormalParameter(GNode n) {
         printer.p(", ").p(getType(n, true));
       }
 
       public void visit(GNode n) {
         for (Object o : n) if (o instanceof Node) dispatch((Node)o);
       } 
 
     }.dispatch(n);
 
     printer.p(");\n");
   }
   
   private void writeClass(){
     indentOut().pln("static java::lang::Class __class();"); 
   }
   
   private void writeVTable(GNode n){
     indentOut().p("static __").p(name(n)).pln("_VT __vtable;");
   }
 
 
 
 // =======================
 // WRITE VTABLE STRUCT 
 // ======================
 
 
   /** Write out the struct definition of a class's VTable 
   * @param i the index of the class we are writing
   */
   private void writeVTStruct(GNode node) {
     
     indentOut().p("struct __").p(name(node)).p("_VT {\n");
     printer.pln(); 
     printer.incr();
       // initialize __isa
       indentOut().pln("java::lang::Class __isa;\n");
       indentOut().p("void (*__delete)(__").p(name(node)).p("*);").pln();  
       writeInheritedVTMethods(node);
       writeVTMethods(node);
       
       printer.pln();
       writeVTConstructor(node);
       indentOut().p(": __isa(__").p(name(node)).pln("::__class()),\n");
       indentOut().p("__delete(&__rt::__delete<__").p(name(node)).p(" >),").pln();
         // writeObjectInheritedVTAddresses(node);
         printer.incr();
         writeInheritedVTAddresses(node);
         writeVTAddresses(node);
         printer.p("{\n");  
       printer.decr();
       indentOut().p("}\n");
     printer.decr();
     indentOut().p("};\n").pln();
   }
 
   /** Write out all the inherited methods of Object, since every class extends Object
    *
    * @param i the index of the class we are writing */
   /**
   private void writeObjectInheritedVTMethods(GNode node) {
     indentOut().p("int32_t (*hashCode)(").p(name(node)).p(");\n");
     indentOut().p("bool (*equals)(").p(name(node)).p(", Object);\n");
     indentOut().p("Class (*getClass)(").p(name(node)).p(");\n");
     indentOut().p("String (*toString)(").p(name(node)).p(");\n");
   } */
 
   /** Write out all the inherited methods of its superclass(es)
    * @param i the index of the class we are writing */
   // TODO: this
   private void writeInheritedVTMethods(GNode n) {
     String current_class = name(n);
     for (GNode m : inherited_methods) {
       //Get the implementedMethodDec node of the inheritedMethodContainer and write the VT method from it.
       writeVTMethod(m.getGeneric(0), current_class);
     }
   }
 
   /** Write out all the classe's own methods
    * @param i the index of the class we are writing */
   // TODO: this
   private void writeVTMethods(GNode n) {
     String current_class = name(n);
     for (GNode m : implemented_methods) {
       writeVTMethod(m, current_class);
     } 
   }
 
   private void writeVTMethod(GNode n, String current_class){
     indentOut().p(getType(n, true)).p(" ");
     printer.p("(*").p(Type.getCppMangledMethodName(n)).p(")(").p(current_class);
     // if (n.getGeneric(2).size() != 0) 
      // printer.p(", <formal params>");
     
     new Visitor() {
   
       public void visitFormalParameter(GNode n) {
         printer.p(", ").p(getType(n, true));
       }
 
       public void visit(GNode n) {
         for (Object o : n) if (o instanceof Node) dispatch((Node)o);
       } 
 
     }.dispatch(n);
     
     printer.p(");\n");
   }
 
   /** Write out the VT Constructor 
    * @param i the index of the class we are writing */
   private void writeVTConstructor(GNode node) {
     indentOut().p("__").p(name(node)).p("_VT()\n");
   }
 
   /** Write out all the inherited Object VT method addresses
    * @param i the index of the class we are writing */
   // TODO: not sure if this is exactly what we want
   /**
   private void writeObjectInheritedVTAddresses(GNode node) {
     indentOut().p("hashCode((int32_t(*)(").p(name(node)).p("))&__Object::hashCode),\n");
     indentOut().p("equals((bool(*)(").p(name(node)).p(",Object))&__Object::equals),\n");
     indentOut().p("getClass((Class(*)(").p(name(node)).p("))&__Object::getClass),\n");
     indentOut().p("toString((String(*)(").p(name(node)).p("))&__Object::toString)\n");
   } */
 
   /** Write out all the inherited VT addresses of the class' superclass(es)' methods
    * @param i the index of the class we are writing */
   // TODO: this
   private void writeInheritedVTAddresses(GNode n) {
     String current_class = name(n);
     for (GNode m : inherited_methods) {
       if (inherited_methods.indexOf(m) != 0)
         printer.p(",\n");
       writeInheritedVTAddress(m, current_class);
     }   
   }
 
   /** Write out all the VT addresses of the class' own methods
    * @param i the index of the class we are writing */
   // TODO: this
   private void writeVTAddresses(GNode n) {
     String current_class = name(n);
     for (GNode m : implemented_methods) {
       printer.p(",\n");
       writeVTAddress(m, current_class);
     }
   }
 
   private void writeInheritedVTAddress(GNode n, String current_class) {
     GNode inheritedMethodContainer;
     inheritedMethodContainer = n;
     n = inheritedMethodContainer.getGeneric(0);
     indentOut().p(Type.getCppMangledMethodName(n)).p("((");
     printer.p(getType(n, true));
     printer.p("(*)(").p(current_class);
     //if (n.getGeneric(2).size() != 0)
       
     new Visitor() {
   
       public void visitFormalParameter(GNode n) {
         printer.p(", ").p(getType(n, true));
       }
 
       public void visit(GNode n) {
         for (Object o : n) if (o instanceof Node) dispatch((Node)o);
       } 
 
     }.dispatch(n);
   
       //printer.p(", <formal params>");
     // following line gets From field from method node
     printer.p("))&").p(getTypeDirect(inheritedMethodContainer.getGeneric(1).getGeneric(0), false))
       .p("::").p(Type.getCppMangledMethodName(n)).p(")");
   }
 
   private void writeVTAddress(GNode n, String current_class) {
     indentOut().p(Type.getCppMangledMethodName(n)).p("(&__").p(current_class).p("::")
       .p(Type.getCppMangledMethodName(n)).p(")");
   }
 
 
 // =======================
 // UTILITY METHODS
 // =======================
 
   private String name(GNode n) {
     System.out.println("Name of what? :" + n.toString());
     // Get the final identifier from the qualified name
     String[] qualifiedNameArray = n.getString(0).split("\\.");
     String name = qualifiedNameArray[qualifiedNameArray.length - 1];
     return name;
   }
   
   private Printer indentOut(){
     return printer.indent();
   }
 
   private String[] getNameQualifiedArray(GNode n) {
     String qualifiedName = n.getString(0);
     String[] qualifiedType = qualifiedName.split("\\.");
     return qualifiedType;
   }
 
   
 
   private String getFieldPrefix(GNode n){
     return n.getString(0).replace(".", "_");
   }
 
 }
