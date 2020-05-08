 /*
  * pcp - The Producer of C++ Programs
  * Copyright (C) 2011 Nabil Hassein, Thomas Huston, Mike Morreale, Marta Wilgan
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package pcp.translator;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Set;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Printer;
 import xtc.tree.Visitor;
 
 /**
  * A Java class.
  *
  * @author Nabil Hassein
  * @author Thomas Huston
  * @author Mike Morreale
  * @author Marta Wilgan
  * @version 1.0
  */
 public class JavaClass extends Visitor implements Translatable {
   
   private JavaMethod constructor;
   private JavaType extension;
   private List<JavaField> fields;
   private boolean isAbstract, isFinal, isStatic;
   private List<JavaMethod> methods;
   private String name;
   private JavaClass parent;
   private Visibility visibility;
   private LinkedHashMap<String, JavaMethod> vtable;
   
   /**
    * Constructs the class.
    * 
    * @param n The class declaration node.
    */
   public JavaClass(GNode n) {
     // Get the class name
     name = n.getString(1);
 
     // Initialize the modifiers to default values
     isAbstract = false;
     isFinal = false;
     isStatic = false;
     visibility = Visibility.PACKAGE_PRIVATE;
 
     // Instantiate the lists
     fields = new ArrayList<JavaField>();
     methods = new ArrayList<JavaMethod>();
 
     // Visit the nodes in the class
     for (Object o : n) {
       if (o instanceof Node) {
         dispatch((Node)o);
       }
     }
   }
   
   /**
    * Gets the reference to the superclass.
    *
    * @return The reference.
    */
   public JavaType getExtension() {
     return extension;
   }
   
   /**
    * Gets the name of the class.
    *
    * @return The name.
    */
   public String getName() {
     return name;
   }
   
   /**
    * Gets the superclass.
    *
    * @return The superclass.
    */
   public JavaClass getParent() {
     return parent;
   }
 
   /**
    * Gets the visibility of the class.
    *
    * @return The visibility.
    */
   public Visibility getVisibility() {
     return visibility;
   }
 
   /**
    * Gets the vtable for the class.
    *
    * @return The vtable.
    */
   public LinkedHashMap<String, JavaMethod> getVTable() {
     // Initialize the vtable if it hasn't been created yet
     if (null == vtable)
       initializeVTable();
     return vtable;
   }
   
   /**
    * Tests whether the class has a superclass.
    *
    * @return <code>True</code> if the class has a superclass;
    * <code>false</code> otherwise.
    */
   public boolean hasParent() {
     return extension != null;
   }
 
   /**
    * Initializes the vtable for the class.
    */
   public void initializeVTable() {
     // Don't do anything if the vtable has already been created
     if (null != vtable)
       return;
 
     // Inherit methods from parent
     vtable = new LinkedHashMap<String, JavaMethod>();
     if (null != parent) {
       LinkedHashMap<String, JavaMethod> parentVTable = parent.getVTable();
       Set<String> keys = parentVTable.keySet();
       for (String key : keys) {
         vtable.put(key, parentVTable.get(key));
       }
     }
 
     // Add/override methods
     for (JavaMethod m : methods) {
       if ((m.getVisibility() == Visibility.PUBLIC || m.getVisibility() == Visibility.PROTECTED) 
           && !m.isFinal() && !m.isStatic())
         vtable.put(m.getName() + "_" + m.getReturnType().getType(), m);
     }
   }
   
   /**
    * Tests whether the class is abstract.
    *
    * @return <code>True</code> if the class is abstract;
    * <code>false</code> otherwise.
    */
   public boolean isAbstract() {
     return isAbstract;
   }
   
   /**
    * Tests whether the class is final.
    *
    * @return <code>True</code> if the class is final;
    * <code>false</code> otherwise.
    */
   public boolean isFinal() {
     return isFinal;
   }
   
   /**
    * Sets the superclass.
    *
    * @param parent The superclass.
    */
   public void setParent(JavaClass parent) {
     this.parent = parent;
   }
   
   /**
    * Visits the class body.
    *
    * @param n The AST node to visit.
    */
   public void visitClassBody(GNode n) {
     for (Object o : n) {
       if (o instanceof Node) {
         dispatch((Node)o);
       }
     }
   }
 
   /**
    * Visits a constructor.
    *
    * @param n The AST node to visit.
    */
   public void visitConstructorDeclaration(GNode n) {
     constructor = new JavaMethod(n, this);
   }
   
   /**
    * Visits the extension.
    *
    * @param n The AST node to visit.
    */
   public void visitExtension(GNode n) {
     extension = new JavaType(n.getGeneric(0));
   }
 
   /**
    * Visits a field.
    *
    * @param n The AST node to visit.
    */
   public void visitFieldDeclaration(GNode n) {
     fields.add(new JavaField(n));
   }
 
   /**
    * Visits a method.
    *
    * @param n The AST node to visit.
    */
   public void visitMethodDeclaration(GNode n) {
     methods.add(new JavaMethod(n, this));
   }
   
   /**
    * Visits the modifiers.
    *
    * @param n The AST node to visit.
    */
   public void visitModifiers(GNode n) {
     for (Object o : n) {
       String m = ((GNode)o).getString(0);
       if (m.equals("public"))
         visibility = Visibility.PUBLIC;
       else if (m.equals("private"))
         visibility = Visibility.PRIVATE;
       else if (m.equals("protected"))
         visibility = Visibility.PROTECTED;
       else if (m.equals("final"))
         isFinal = true;
       else if (m.equals("abstract"))
         isAbstract = true;
       else if (m.equals("static"))
         isStatic = true;
     }
   }
 
   /**
    * Writes the C++ header for the class to
    * the specified output stream.
    *
    * @param out The output stream.
    *
    * @return The output stream.
    */
   public Printer translateHeader(Printer out) {
     // Create the vtable
     initializeVTable();
 
     // First, print the class struct
     out.indent().p("struct __").p(name).pln(" {").incr();
 
     // Declare all the fields
     out.indent().p("__").p(name).pln("_VT* __vptr;");
     for (JavaField f : fields) {
       f.translate(out);
     }
     out.pln();
 
     // Declare the constructor
     if (null != constructor) {
       constructor.translateHeaderDeclaration(out);
     } else {
       out.indent().p("__").p(name).pln("();");
     }
 
     // Declare all methods
     if (methods.size() > 0)
       out.pln();
     for (JavaMethod m : methods) {
       m.translateHeaderDeclaration(out);
     }
     out.pln().indent().pln("static Class __class();").pln();
 
     // Add the vtable
     out.indent().p("static __").p(name).pln("_VT __vtable;");
     out.decr().indent().pln("};").pln();
 
     // Create the vtable struct
     out.indent().p("struct __").p(name).pln("_VT {").incr();
     out.indent().pln("Class __isa;");
 
     // Declare all the methods in the vtable
     out.indent().p("int32_t (*hashCode)(").p(name).pln(");");
     out.indent().p("bool (*equals)(").p(name).pln(", Object);");
     out.indent().p("Class (*getClass)(").p(name).pln(");");
     out.indent().p("String (*toString)(").p(name).pln(");");
     Set<String> keys = vtable.keySet();
     for (String key : keys) {
       if (key.equals("hashCode_int32_t") || key.equals("equals_bool") || key.equals("toString_String"))
         continue;
       vtable.get(key).translateVTableDeclaration(out, this);
     }
 
     // Construct the vtable with pointers to the methods
     out.pln().indent().p("__").p(name).pln("_VT()");
     out.indent().p(": __isa(__").p(name).pln("::__class()),");
     if (vtable.containsKey("hashCode_int32_t")) {
       vtable.get("hashCode_int32_t").translateVTableReference(out, this);
       out.pln(",");
     } else {
       out.indent().p("hashCode((int32_t(*)(").p(name).pln("))&__Object::hashCode),");
     }   
     if (vtable.containsKey("equals_bool")) {
       vtable.get("equals_bool").translateVTableReference(out, this);
       out.pln(",");
     } else {
       out.indent().p("equals((bool(*)(").p(name).pln(",Object))&__Object::equals),");
     }
     out.indent().p("getClass((Class(*)(").p(name).pln("))&__Object::getClass),");
     if (vtable.containsKey("toString_String")) {
       vtable.get("toString_String").translateVTableReference(out, this);
     } else {
      out.indent().p("toString(String(*)(").p(name).p("))&__Object::toString)");
     }
     for (String key : keys) {
       if (key.equals("hashCode_int32_t") || key.equals("equals_bool") || key.equals("toString_String"))
         continue;
       out.pln(",");
       vtable.get(key).translateVTableReference(out, this);
     }
     out.pln(" {}");
     out.decr().indent().pln("};");
 
     return out;
   }
 
   /**
    * Translates the body of the class and
    * writes it to the output stream.
    *
    * @param out The output stream.
    *
    * @return The output stream.
    */
   public Printer translate(Printer out) {
     return out;
   }
 
 }
