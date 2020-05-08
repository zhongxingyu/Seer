 package qimpp;
 import qimpp.Type;
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.PrintWriter;
 import java.util.Iterator;
 import java.util.ArrayList;
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
   private boolean implemented;
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
     implemented = false;
     //current_class = "";
     printer.register(this);  
   }
   
  // ===================
  //  VISITOR
  // ==================
 
   public void visitCompilationUnit(GNode n){
     writeDependencies(); 
     visit(n);
     printer.flush();
   }
 
   public void visitDeclarations(GNode n){
     visit(n);
   }
 
   public void visitDeclaration(GNode n){
     writeTypeDeclaration(n);
     writeAlias(n);
   }
 
   public void visitClasses(GNode n){
     visit(n);
   }
 
   public void visitClassDeclaration(GNode n){
     //current_class = name(n); 
     visit(n);
     
     writeStruct(n);
     writeVTStruct(n);
 
     inherited_methods.clear();
     implemented_methods.clear();
     fields.clear();
     //current_class = "";
   }
 
   public void visitFields(GNode n){
     visit(n);
   }
 
   public void visitFieldDeclaration(GNode n){
     fields.add(n); 
   }
 
   public void visitInheritedMethods(GNode n){
     inherited = true;
     visit(n);
     inherited = false;
   }
 
   public void visitImplementedMethods(GNode n){
     implemented = true;
     visit(n);
     implemented = false;
   }
 
   public void visitMethodDeclaration(GNode n){
     if (!name(n).equals("main")) {
       if (inherited)
         inherited_methods.add(n);
       else if (implemented)
         implemented_methods.add(n);
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
     printer.p("#include \"java_lang.h\"").pln(); 
     printer.p("#include <stdint.h>").pln().pln();
   }
   
   /** Write out the internal names of the structs and vtables for each class 
   *
   * @param index the index of the class we are writing
   */
   public void writeTypeDeclaration(GNode node){
     // ClassDeclaration field 1 is the name of the class
     
     printer.p("struct __").p(name(node)).p(";\n");
     printer.p("struct __").p(name(node)).p("_VT;\n").pln();
   }
   
   /** Write out the typedefs so pretty-printing class names is easier on the programmer and
   * the eyes 
   *
   * @param node the node being examined
   */
   private void writeAlias(GNode node){
       printer.p("typedef __").p(name(node)).p("* ").p(name(node));
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
     printer.p("struct __").p(name(n)).p(" {\n");
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
     printer.p("};\n").pln();
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
   
   private String getType(GNode n, boolean isPointer) {
     GNode type = n.getGeneric(1);
     if (name(type).equals("PrimitiveType")) {
       return type.getString(0);
     }
     else if (name(type).equals("QualifiedIdentifier")) {
       if (type.size() == 1 && isPointer == false)
         return "__" + type.getString(0);
       String ret = "";
       for (Object id : type) {
         if (type.indexOf(id) == 0) 
           ret += type.getString(type.indexOf(id));
         else if (type.indexOf(id) == type.size() - 1 && isPointer == false)
           ret += "::__" + type.getString(type.indexOf(id));
         else 
           ret += "::" + type.getString(type.indexOf(id));
       }
       return ret;
     }
    return "NOT A REAL TYPE";
   }
 
   private void writeField(GNode n) {
     String type = getType(n, true); 
     indentOut().p(type).p(" ").p(n.getString(0)).p(";\n"); 
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
     printer.p(n.getString(0)).p("(").p(current_class);
     if (n.getGeneric(2).size() != 0) 
       printer.p(", <formal params>");
     printer.p(");\n");
   }
   
   private void writeClass(){
     indentOut().pln("static Class __class();"); 
   }
   
   private void writeVTable(GNode n){
     indentOut().p("static ").p(name(n)).pln("_VT __vtable;");
   }
 
 
 
 // =======================
 // WRITE VTABLE STRUCT 
 // ======================
 
 
   /** Write out the struct definition of a class's VTable 
   * @param i the index of the class we are writing
   */
   private void writeVTStruct(GNode node) {
     printer.p("struct __").p(name(node)).p("_VT {\n");
     printer.pln(); 
     printer.incr();
       // initialize __isa
       indentOut().p("Class __isa;\n");  
       writeInheritedVTMethods(node);
       writeVTMethods(node);
       
       printer.pln();
       writeVTConstructor(node);
       indentOut().p(": __isa(__").p(name(node)).p("::__class()),\n");
         // writeObjectInheritedVTAddresses(node);
         printer.incr();
         writeInheritedVTAddresses(node);
         writeVTAddresses(node);
         printer.p("{\n");  
       printer.decr();
       indentOut().p("}\n");
     printer.decr();
     printer.p("};\n").pln();
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
       writeVTMethod(m, current_class);
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
     printer.p("(*").p(n.getString(0)).p(")(").p(current_class);
     if (n.getGeneric(2).size() != 0) 
       printer.p(", <formal params>");
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
     indentOut().p(n.getString(0)).p("((");
     printer.p(getType(n, false));
     printer.p("(*)(").p(current_class);
     if (n.getGeneric(2).size() != 0)
       printer.p(", <formal params>");
     // following line gets From field from method node
     printer.p("))&").p(n.getGeneric(3).getString(0)).p("::").p(n.getString(0))
       .p(")");
   }
 
   private void writeVTAddress(GNode n, String current_class) {
     indentOut().p(n.getString(0)).p("(&__").p(current_class).p("::")
       .p(n.getString(0)).p(")");
   }
 
 
 // =======================
 // UTILITY METHODS
 // =======================
 
   private String name(GNode n) {
     String name = n.getString(0);
     return name;
   }
   
   private Printer indentOut(){
     return printer.indent();
   }
 
 }
