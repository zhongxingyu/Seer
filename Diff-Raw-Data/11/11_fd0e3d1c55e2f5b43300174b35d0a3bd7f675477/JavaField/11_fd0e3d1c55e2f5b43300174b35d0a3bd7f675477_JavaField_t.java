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
 import java.util.List;
 import java.util.Map;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Printer;
 import xtc.tree.Visitor;
 
 /**
  * A Java field.
  *
  * @author Nabil Hassein
  * @author Thomas Huston
  * @author Mike Morreale
  * @author Marta Wilgan
  *
  * @version 2.1.1
  */
 public class JavaField extends JavaStatement implements Translatable {
 
   // If this is a class variable, the class the field is declared in
   private JavaClass cls;
 
   // Whether the field is final or static
   private boolean isFinal, isStatic;
 
   // The names of the variables declared
   private List<String> names;
 
   // The statement wrapper
   private JavaStatement parent;
 
   // The type of the field
   private JavaType type;
 
   // The initial values of the variables
   private List<JavaExpression> values;
 
   // The visibility of the field
   private JavaVisibility visibility;
 
 
   // =========================== Constructors =======================
 
   /**
    * Creates the specified field and sets the wrapper
    * statement and the class the statement is in.
    *
    * @param n The field declaration node.
    * @param parent The wrapper statement.
    * @param scope The scope the field is in.
    */
   public JavaField(GNode n, JavaStatement parent, JavaScope scope) {
     // Set the parent class
     JavaScope temp = scope;
     while (!temp.hasName("JavaClass"))
       temp = temp.getParentScope();
     cls = (JavaClass)temp;
     
     // Set the statement wrapper
     this.parent = parent;
 
     // Set the default visibility
     visibility = JavaVisibility.PACKAGE_PRIVATE;
 
     // Determine the modifiers
     for (Object o : n.getNode(0)) {
       String m = ((GNode)o).getString(0);
       if (m.equals("public"))
         visibility = JavaVisibility.PUBLIC;
       else if (m.equals("private"))
         visibility = JavaVisibility.PRIVATE;
       else if (m.equals("protected"))
         visibility = JavaVisibility.PROTECTED;
       else if (m.equals("static"))
         isStatic = true;
       else if (m.equals("final"))
         isFinal = true;
     }
 
     // Get the type
     type = new JavaType(n.getGeneric(1));
     if (isStatic)
       type.setStatic();
 
     // Get the variable names and initialized values
     names = new ArrayList<String>();
     values = new ArrayList<JavaExpression>();
     for (Object o : n.getNode(2)) {
       Node declarator = (Node)o;
       names.add("$" + declarator.getString(0));
       if (null == this.parent)
         cls.addVariable("$" + declarator.getString(0), type);
       else
         this.parent.getScope().addVariable("$" + declarator.getString(0), type);
       if (null != declarator.get(2)) {
         if (null != parent)
           values.add(new JavaExpression(declarator.getGeneric(2), parent));
         else
           values.add(new JavaExpression(declarator.getGeneric(2), this));
       } else {
         values.add(null);
       }
     }
 
     // Get the dimensions if it's an array
     if (null != n.getNode(2).getNode(0).get(1))
       type.setDimensions(n.getNode(2).getNode(0).getNode(1).size());
     
   }
   
   /**
    * Creates the specified field and sets the
    * class the statement is in.
    *
    * @param n The field declaration node.
    * @param scope The scope the field is in.
    */
   public JavaField(GNode n, JavaScope scope) {
     this(n, null, scope);
   }
   
 
   // ============================ Get Methods =======================
 
   /**
    * Gets the names of variables declared.
    *
    * @return The variable names.
    */
   public List<String> getNames() {
     return names;
   }
 
   /**
    * Gets the scope the field is in.
    *
    * @return The variable scope.
    */
   public JavaScope getScope() {
     return cls;
   }
 
   /**
    * Gets the type of the field.
    *
    * @return The variable type.
    */
   public JavaType getType() {
     return type;
   }
 
   /**
    * Checks if the field is initialized.
    *
    * @return <code>True</code> if it is initialized;
    * false otherwise.
    */
   public boolean isInitialized() {
     for (JavaExpression e : values) {
       if (null != e)
         return true;
     }
     return false;
   }
 
   /**
    * Checks if the field is static.
    *
    * @return <code>True</code> if it is static;
    * <code>false</code> otherwise.
    */
   public boolean isStatic() {
     return isStatic;
   }
 
 
   // ============================ Set Methods =======================
 
   /**
    * Makes sure to compile a full list of variables to check
    * not null on.
    */
   public void checkNotNull() {
     for (JavaExpression e : values) {
       if (null != e && e.hasName("CallExpression"))
         e.checkNotNull();
     }
   }
 
 
   // ======================== Translation Methods ===================
 
   /**
    * Translates the field declaration and adds it 
    * to the output stream.
    *
    * @param out The output stream.
    *
    * @return The output stream.
    */
   public Printer translateDeclaration(Printer out) {
     int size = names.size();
     for (int i = 0; i < size; i++) {
       out.indent();
       if (isStatic) {
         out.p("static ");
         if (isFinal)
           out.p("const ");
       }
       if (type.isArray())
         out.p("__rt::Ptr<");
       type.translate(out);
       if (type.isArray())
         out.p(" >");
       out.p(" ").p(names.get(i)).pln(";");
     }
     return out;
   }
 
   /**
    * Translates the field instantiation in the constructor
    * and adds it to the output stream.
    *
    * @param out The output stream.
    *
    * @return The output stream.
    */
   public Printer translateConstructor(Printer out) {
     int size = names.size();
     for (int i = 0; i < size; i++) {
      if (isStatic || (!type.isPrimitive() && null == values.get(i)))
         continue;
       out.indent().p("__this->").p(names.get(i));
       if (null != values.get(i)) {
         out.p(" = ");
         // Use the specified value if provided
         values.get(i).translate(out).pln(";");
       } else if (null == parent && type.isPrimitive()) {
         // Initialize primitives to 0
         out.pln(" = 0;");
       } else {
         out.pln(";");
       }
     }
     return out;
   }
 
   /**
    * Translates the field instantiation and adds it 
    * to the output stream.
    *
    * @param out The output stream.
    *
    * @return The output stream.
    */
   public Printer translate(Printer out) {
     int size = names.size();
     for (int i = 0; i < size; i++) {
       out.indent();
       if (isFinal)
         out.p("const ");
       if (type.isArray())
         out.p("__rt::Ptr<");
       type.translate(out);
       if (type.isArray())
         out.p(" >");
       out.p(" ");
       if (isStatic && !cls.getFile().getPackage().getNamespace().equals(""))
         out.p(cls.getFile().getPackage().getNamespace()).p("::");
       if (isStatic)
         out.p("__").p(cls.getName()).p("::");
       out.p(names.get(i));
       if (null != values.get(i)) {
         out.p(" = ");
         values.get(i).translate(out).pln(";");
       } else if (null == parent && type.isPrimitive()) {
         out.pln(" = 0;");
       } else {
         out.pln(";");
       }
     }
     return out;
   }
 
 }
