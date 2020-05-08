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
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
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
  *
  * @version 2.1.1
  */
 public class JavaClass extends Visitor implements JavaScope, Translatable {
   
   // A map from class names to the corresponding class objects
   private static Map<String, JavaClass> classes = new HashMap<String, JavaClass>();
 
   // The constructors for the class
   private List<JavaConstructor> constructors;
 
   // The type of the superclass
   private JavaType extension;
 
   // The class variables
   private List<JavaField> fields;
 
   // The file in which this class exists
   private JavaFile file;
 
   // Keeps track of final/static status
   private boolean isFinal, isStatic;
 
   // The methods in the class
   private List<JavaMethod> methods;
 
   // The name of the class
   private String name;
 
   // The superclass
   private JavaClass parent;
 
   // The variables declared in this scope
   private Map<String, JavaType> variables;
 
   // The visibility of the class
   private JavaVisibility visibility;
 
   // The vtable for the class
   private LinkedHashMap<String, JavaMethod> vtable;
 
 
   // =========================== Constructors =======================
   
   /**
    * Constructs the class.
    * 
    * @param n The class declaration node.
    * @param f The source file.
    */
   public JavaClass(GNode n, JavaFile f) {
     // Set the source file
     file = f;
 
     // Initialize the variable table
     variables = new HashMap<String, JavaType>();
 
     // Get the class name
     name = n.getString(1);
 
     // Add the class to the hierarchy
     JavaType.addType(name, "Object");
 
     // Initialize the modifiers to default values
     visibility = JavaVisibility.PACKAGE_PRIVATE;
 
     // Instantiate the lists
     fields = new ArrayList<JavaField>();
     constructors = new ArrayList<JavaConstructor>();
     methods = new ArrayList<JavaMethod>();
 
     // Visit the nodes in the class
     for (Object o : n) {
       if (o instanceof Node) {
         dispatch((Node)o);
       }
     }
   }
 
 
   // ============================ Get Methods =======================
   
   /**
    * Gets the reference to the superclass; used by the translator to
    * locate the actual superclass.
    *
    * @return The reference.
    */
   public JavaType getExtension() {
     return extension;
   }
 
   /**
    * Gets the file the class is in.
    *
    * @return The file.
    */
   public JavaFile getFile() {
     return file;
   }
 
   /**
    * Gets a list of the class fields.
    *
    * @return The class fields.
    */
   public List<JavaField> getFields() {
     return fields;
   }
 
   /**
    * Gets the constructor with the specified name if it exists.
    *
    * @param name The name of the constructor.
    *
    * @return The constructor if it exists;
    * <code>null</code> otherwise.
    */
   public JavaConstructor getConstructor(String name) {
     return getConstructor(name, false);
   }
 
   /**
    * Gets the constructor with the specified name if it exists.
    *
    * @param name The name of the constructor.
    * @param current Whether the call is from the current class.
    *
    * @return The constructor if it exists;
    * <code>null</code> otherwise.
    */
   public JavaConstructor getConstructor(String name, boolean current) {
     for (JavaConstructor c : constructors) {
       if (c.getMangledName().equals(name) &&
           (current || (JavaVisibility.PUBLIC == c.getVisibility() ||
                        JavaVisibility.PROTECTED == c.getVisibility())))
         return c;
     }
     return null;
   }
 
   /**
    * Gets the method of the specified name if it exists.
    *
    * @param name The name of the method.
    *
    * @return The method if it is in the class;
    * <code>null</code> otherwise.
    */
   public JavaMethod getMethod(String name) {
     return getMethod(name, false);
   }
 
   /**
    * Gets the method of the specified name if it exists;
    * includes private methods in the search only if the
    * method is being called from within the current class.
    *
    * @param name The name of the method.
    * @param current Whether the call is from the current class.
    *
    * @return The method if it is in the class and is accessible;
    * <code>null</code> otherwise.
    */
   public JavaMethod getMethod(String name, boolean current) {
     if (null == vtable)
       initializeVTable();
     if (vtable.containsKey(name))
       return vtable.get(name);
     if (current) {
       for (JavaMethod m : methods) {
         if (m.getName().equals(name))
           return m;
       }
     }
     return null;
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
    * Gets the parent scope; returns the superclass scope
    * if it exists; otherwise <code>null</code> because
    * JavaClass is the highest level scope.
    *
    * @return The parent scope.
    */
   public JavaScope getParentScope() {
     if (null != parent)
       return (JavaScope)parent;
     return null;
   }
 
   /**
    * Gets the scope in which the specified variable is declared.
    *
    * @param name The name of the variable.
    *
    * @return The scope of the variable if it exists;
    * <code>null</code> otherwise.
    */
   public JavaScope getVariableScope(String name) {
     if (variables.containsKey(name))
       return this;
     if (null != parent)
       return parent.getVariableScope(name);
     return null;
   }
 
   /**
    * Gets the type of the specified variable.
    *
    * @param name The name of the variable.
    *
    * @return The type if the variable exists;
    * <code>null</code> otherwise.
    */
   public JavaType getVariableType(String name) {
     if (variables.containsKey(name))
       return variables.get(name);
     if (null != parent)
       return parent.getVariableType(name);
     return null;
   }
 
   /**
    * Gets the visibility of the class.
    *
    * @return The visibility.
    */
   public JavaVisibility getVisibility() {
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
    * Checks if the current scope is of the specified type.
    *
    * @param type The type of the scope.
    *
    * @return <code>True</code> if the scope is of the specified
    * type; <code>false</code> otherwise.
    */
   public boolean hasName(String type) {
     return type.equals("JavaClass");
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
    * Tests whether the class is final.
    *
    * @return <code>True</code> if the class is final;
    * <code>false</code> otherwise.
    */
   public boolean isFinal() {
     return isFinal;
   }
   
   /**
    * Checks if a variable is currently in scope.
    *
    * @return <code>True</code> if the variable is in scope;
    * <code>false</code> otherwise.
    */
   public boolean isInScope(String name) {
     if (variables.containsKey(name))
       return true;
     if (null != parent)
       return parent.isInScope(name);
     return false;
   }
 
   /**
    * Checks if the specified class variable is static.
    *
    * @return <code>True</code> if the variable is static;
    * <code>false</code> otherwise.
    */
   public boolean isVariableStatic(String name) {
     for (JavaField f : fields) {
       List<String> names = f.getNames();
       for (String fieldName : names) {
         if (name.equals(fieldName))
           return f.isStatic();
       }
     }
     if (null != parent)
       return parent.isVariableStatic(name);
     return false;
   }
 
 
   // ============================ Set Methods =======================
   
   /**
    * Adds a variable to the scope.
    *
    * @param name The name of the variable.
    * @param type The type of the variable.
    */
   public void addVariable(String name, JavaType type) {
     variables.put(name, type);
   }
 
   /**
    * Sets the superclass.
    *
    * @param parent The superclass.
    */
   public void setParent(JavaClass parent) {
     this.parent = parent;
     // Add class instance variables from the superclass
     JavaClass temp = parent;
     while (null != temp) {
       List<JavaField> fields = temp.getFields();
       for (JavaField f : fields) {
         if (!f.isStatic()) {
           List<String> names = f.getNames();
           for (String name : names) {
             variables.put(name, f.getType());
           }
         }
       }
       temp = temp.getParent();
     }
   }
   
 
   // =========================== Visit Methods ======================
 
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
     constructors.add(new JavaConstructor(n, this));
   }
   
   /**
    * Visits the extension.
    *
    * @param n The AST node to visit.
    */
   public void visitExtension(GNode n) {
     extension = new JavaType(n.getGeneric(0));
     JavaType.addType(name, extension.getClassType());
   }
 
   /**
    * Visits a field.
    *
    * @param n The AST node to visit.
    */
   public void visitFieldDeclaration(GNode n) {
     fields.add(new JavaField(n, this));
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
         visibility = JavaVisibility.PUBLIC;
       else if (m.equals("private"))
         visibility = JavaVisibility.PRIVATE;
       else if (m.equals("protected"))
         visibility = JavaVisibility.PROTECTED;
       else if (m.equals("final"))
         isFinal = true;
       else if (m.equals("static"))
         isStatic = true;
     }
   }
 
 
   // ======================== Translation Methods ===================
 
   /**
    * Initializes the vtable for the class.
    */
   public void initializeVTable() {
     // Don't do anything if the vtable has already been created
     if (null != vtable)
       return;
 
     // Inherit methods from the parent
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
       if (m.isVirtual())
         vtable.put(m.getName(), m);
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
     out.indent().p("static const ");
     if (null == parent) {
       out.p("Object");
     } else {               
       if (!parent.getFile().getPackage().getNamespace().equals(""))
         out.p(parent.getFile().getPackage().getNamespace()).p("::");
       out.p(parent.getName());
     }
     out.pln(" __super;");
     JavaClass temp = parent;
     while (null != temp) {
       List<JavaField> parentFields = temp.getFields();
       for (JavaField f : parentFields) {
         if (!f.isStatic())
           f.translateDeclaration(out);
       }
       temp = temp.getParent();
     }
     for (JavaField f : fields) {
       f.translateDeclaration(out);
     }
     out.pln();
 
     // Declare the constructor
     out.indent().p("__").p(name).pln("();").pln();
 
     if (0 != constructors.size()) {
       for (JavaConstructor con : constructors) {
         con.translateHeaderDeclaration(out);
       }
     } else {
       out.indent().p("static ").p(name).p(" ").p(name).pln("$void();");
     }
 
     // Destructor
     out.pln().indent().p("static void __delete(__").p(name).pln("*);");
 
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
     out.indent().p("void (*__delete)(__").p(name).pln("*);");
     out.indent().p("int32_t (*hashCode$void)(").p(name).pln(");");
     out.indent().p("bool (*equals$Object)(").p(name).pln(", Object);");
     out.indent().p("Class (*getClass$void)(").p(name).pln(");");
     out.indent().p("String (*toString$void)(").p(name).pln(");");
     Set<String> keys = vtable.keySet();
     for (String key : keys) {
       if (key.equals("hashCode$void") || key.equals("equals$Object") || key.equals("toString$void"))
         continue;
       vtable.get(key).translateVTableDeclaration(out, this);
     }
 
     // Construct the vtable with pointers to the methods
     out.pln().indent().p("__").p(name).pln("_VT()");
     out.indent().p(": __isa(__").p(name).pln("::__class()),");
     out.indent().p("__delete((void(*)(__").p(name).pln("*))&__Object::__delete),");
     if (vtable.containsKey("hashCode$void")) {
       vtable.get("hashCode$void").translateVTableReference(out, this);
       out.pln(",");
     } else {
       out.indent().p("hashCode$void((int32_t(*)(").p(name).pln("))&__Object::hashCode$void),");
     }   
     if (vtable.containsKey("equals$Object")) {
       vtable.get("equals$Object").translateVTableReference(out, this);
       out.pln(",");
     } else {
       out.indent().p("equals$Object((bool(*)(").p(name).pln(",Object))&__Object::equals$Object),");
     }
     out.indent().p("getClass$void((Class(*)(").p(name).pln("))&__Object::getClass$void),");
     if (vtable.containsKey("toString$void")) {
       vtable.get("toString$void").translateVTableReference(out, this);
     } else {
       out.indent().p("toString$void((String(*)(").p(name).p("))&__Object::toString$void)");
     }
     for (String key : keys) {
       if (key.equals("hashCode$void") || key.equals("equals$Object") || key.equals("toString$void"))
         continue;
       out.pln(",");
       vtable.get(key).translateVTableReference(out, this);
     }
     out.pln(" {}");
     out.decr().indent().pln("};");
 
     return out;
   }
 
   /**
    * Translates the array template for the class
    * and writes it to the output stream.
    *
    * @param out The output stream.
    *
    * @return The output stream.
    */
   public Printer translateArrayTemplate(Printer out) {
     // Create the array template specializations for this class if necessary
     Map<String, Integer> arrays = JavaType.getArrayDimensions();
     if (arrays.containsKey(name)) {
       int dim = arrays.get(name);
       for (int i = 1; i <= dim; i++) {
         JavaType t;
         if (getFile().getPackage().getNamespace().equals(""))
           t = new JavaType(name, i);
         else
           t = new JavaType(name, getFile().getPackage(), i);
         t.translateArraySpecialization(out);
       }
     }
     
     // If this is the main public class, also add the template specializations
     // for java.lang classes and primitive types
     if (getFile().isMain() && visibility == JavaVisibility.PUBLIC) {
       Set<String> keys = arrays.keySet();
       for (String key : keys) {
         if (key.equals("Object") || key.equals("Class") ||
             key.equals("String") || JavaType.isPrimitive(key)) {
           int dim = arrays.get(key);
           int i = key.equals("Object") ? 2 : 1;
           for (; i <= dim; i++) {
             JavaType t = new JavaType(key, i);
             t.translateArraySpecialization(out);
           }
         }
       }
     }
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
     // Initialize the static reference to super
     out.indent().p("const ");
     if (null == parent) {
       out.p("Object ");
     } else {
       if (!parent.getFile().getPackage().getNamespace().equals(""))
         out.p(parent.getFile().getPackage().getNamespace()).p("::");
       out.p(parent.getName()).p(" ");
     }
     out.p("__").p(name).p("::__super = new ");
     if (null == parent) {
       out.pln("__Object();");
     } else {
       if (!parent.getFile().getPackage().getNamespace().equals(""))
         out.p(parent.getFile().getPackage().getNamespace()).p("::");
       out.p("__").p(parent.getName()).pln("();");
     }
 
 
     // Initialize any static fields
     for (JavaField f : fields) {
       if (f.isStatic()) {
         f.translate(out).pln();
       }
     }
 
     // Print out the constructors
     out.indent().p("__").p(name).p("::__").p(name).pln("()");
     out.indent().pln(": __vptr(&__vtable) {}").pln();
     // Use the custom constructors if written
     if (0 != constructors.size()) {
       for (JavaConstructor c : constructors) {
         c.translate(out);
         out.pln();
       }
     // Otherwise create the default constructor
     } else {
       out.indent().p(name).p(" __").p(name).p("::").p(name).pln("$void() {").incr();
       out.indent().p(name).p(" __this = new __").p(name).pln("();");
       if (null != parent) {
         out.indent();
         if (!parent.getFile().getPackage().getNamespace().equals(""))
           out.p(parent.getFile().getPackage().getNamespace()).p("::");
         out.p("__").p(parent.getName()).p("::__").p(parent.getName()).pln("$void(__this);");
       }
       JavaClass temp = parent;
       while (null != temp) {
         List<JavaField> parentFields = temp.getFields();
         for (JavaField f : parentFields) {
           if (!f.isStatic())
             f.translateConstructor(out);
         }
         temp = temp.getParent();
       }
       for (JavaField f : fields) {
        if (!f.isStatic()) {
           f.translateConstructor(out);
        }
       }
       out.indent().pln("return __this;");
       out.decr().indent().pln("}").pln();
     }
 
     // Add a delete method
     out.indent().p("void __").p(name).p("::__delete(__").p(name).pln("* __this) {").incr();
     out.indent().pln("delete __this;");
     out.decr().indent().pln("}").pln();
 
     // Translate all the methods
     for (JavaMethod m : methods) {
       m.translate(out);
       out.pln();
     }
 
     // Create the __class method
     out.indent().p("Class __").p(name).pln("::__class() {").incr();
     out.indent().p("static Class k = new __Class(__rt::literal(\"");
     String packagename = getFile().getPackage().getPackagename();
     if (!packagename.equals(""))
       out.p(packagename).p(".");
     out.p(name).p("\"), ");
     if (null != parent) {
       String parentpackage = parent.getFile().getPackage().getNamespace();
       if (!parentpackage.equals(""))
         out.p(parentpackage).p("::");
       out.p("__").p(parent.getName()).pln("::__class());");
     } else {
       out.pln("__Object::__class());");
     }
     out.indent().pln("return k;");
     out.decr().indent().pln("}").pln();
 
     // Declare the vtable
     out.indent().p("__").p(name).p("_VT __").p(name).pln("::__vtable;");
     return out;
   }
 
 
   // ========================== Static Methods ======================
 
   /**
    * Adds a class to the class map.
    *
    * @param name The fully qualified name of the class.
    * @param cls The corresponding JavaClass object.
    */
   public static void addClass(String name, JavaClass cls) {
     classes.put(name, cls);
   }
 
   /**
    * Gets the JavaClass specified by the qualified class name.
    *
    * @param name The fully qualified name of the class.
    * 
    * @return The corresponding JavaClass object.
    */
   public static JavaClass getJavaClass(String name) {
     return classes.get(name);
   }
 
   /**
    * Gets the list of Java classes.
    *
    * @return The list of Java classes.
    */
   public static Set<String> getJavaClassList() {
     return classes.keySet();
   }
 
 }
