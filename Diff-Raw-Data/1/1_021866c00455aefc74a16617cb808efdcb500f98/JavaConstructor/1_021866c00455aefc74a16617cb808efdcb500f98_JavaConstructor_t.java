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
  * A constructor.
  *
  * @author Nabil Hassein
  * @author Thomas Huston
  * @author Mike Morreale
  * @author Marta Wilgan
  *
  * @version 2.0
  */
 public class JavaConstructor extends JavaMethod implements Translatable {
 
   // The body of the constructor
   private JavaBlock body;
 
   // The class this is a constructor for
   private JavaClass cls;
 
   // The mangled name of the constructor
   private String name;
 
   // The parameters for the constructor
   private LinkedHashMap<String, JavaType> parameters;
 
   // this() and super() calls
   private JavaStatement superCall, thisCall;
 
   // The visibility of the constructor
   private JavaVisibility visibility;
 
 
   // =========================== Constructors =======================
   
   /**
    * Creates the constructor.
    *
    * @param n The constructor declaration node.
    */
   public JavaConstructor(GNode n, JavaClass cls) {
     // Set the class
     this.cls = cls;
 
     // Initialize the maps
     parameters = new LinkedHashMap<String, JavaType>();
     
     // Set the default visibility
     visibility = JavaVisibility.PACKAGE_PRIVATE;
 
     // Get the name
     name = n.getString(2);
 
     // Dispatch over the child nodes
     for (Object o : n) {
       if (o instanceof Node) {
         dispatch((Node)o);
       }
     }
 
     // Create the body of the constructor
     body = new JavaBlock(n.getGeneric(5), cls, this);
 
     // Check if the first line of the constructor is a this() or super() call
     if (0 < n.getNode(5).size() && n.getNode(5).getNode(0).hasName("ExpressionStatement") &&
         n.getNode(5).getNode(0).getNode(0).hasName("CallExpression"))
       if (n.getNode(5).getNode(0).getNode(0).getString(2).equals("this"))
         thisCall = new JavaStatement(n.getNode(5).getGeneric(0), body);
       else if (n.getNode(5).getNode(0).getNode(0).getString(2).equals("super"))
         superCall = new JavaStatement(n.getNode(5).getGeneric(0), body);
   }
 
 
   // ============================ Get Methods =======================
 
   /**
    * Gets the class constructor is for.
    *
    * @return The class.
    */
   public JavaClass getClassFrom() {
     return cls;
   }
 
   /**
    * Gets the mangled constructor name.
    *
    * @return The mangled name.
    */
   public String getMangledName() {
     StringBuilder mangled = new StringBuilder();
     mangled.append("$__" + name);
     Set<String> params = parameters.keySet();
     for (String param : params) {
       mangled.append("$" + parameters.get(param).getMangledType());
     }
     if (0 == params.size())
       mangled.append("$void");
     return mangled.toString();
   }
 
   /**
    * Gets the parameters of the constructor.
    *
    * @return The parameters.
    */
   public LinkedHashMap<String, JavaType> getParameters() {
     return parameters;
   }
 
   /**
    * Gets the visibility of the constructor.
    *
    * @return The visibility.
    */
   public JavaVisibility getVisibility() {
     return visibility;
   }
 
   /**
    * Returns <code>false</code> in all cases; exists 
    * so that we may quickly check whether the dynamic
    * type of a JavaMethod is a method or constructor.
    *
    * @return <code>False</code>.
    */
   public boolean isMethod() {
     return false;
   }
 
   /**
    * Checks if the specified variable is static.
    *
    * @return <code>True</code> if the variable is static;
    * <code>false</code> otherwise.
    */
   public boolean isVariableStatic(String name) {
     return cls.isVariableStatic(name);
   }
 
 
   // =========================== Visit Methods ======================
 
   /**
    * Parses the body of the constructor.
    *
    * @param n The block node.
    */
   public void visitBlock(GNode n) {
     // Already initialized, nothing to do
   }
 
   /**
    * Parses the dimensions of the constructor.
    *
    * @param n The dimensions node.
    */
   public void visitDimensions(GNode n) {
     // What are dimensions in a method declaration?  
   }
      
   /**
    * Parses the parameters of the constructor.
    *
    * @param n The formal parameters node.
    */
   public void visitFormalParameters(GNode n) {
     for (Object o : n) {
       Node param = (Node)o;
       int j;
       if (param.getNode(1).hasName("Type")) 
         j = 1;
       else
         j = 2;
       JavaType paramType = new JavaType(param.getGeneric(j));
       if (null != param.getNode(j).get(1))
         paramType.setDimensions(param.getNode(j).getNode(1).size());
       parameters.put("$" + param.getString(j + 2), paramType);
     }
   }
 
   /**
    * Parses the modifiers of the constructor.
    *
    * @param n The modifiers node.
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
     }
   }
 
   /**
    * Parses the exceptions thrown by the constructor.
    *
    * @param n The throws clause node.
    */
   public void visitThrowsClause(GNode n) {
     // C++ doesn't have throws, so there's nothing to do here
   }
 
 
   // ======================== Translation Methods ===================
 
   /**
    * Translates the constructor helper method into a
    * declaration for the C++ header struct and writes
    * it to the output stream.
    *
    * @param out The output stream.
    *
    * @return The output stream.
    */
   public Printer translateHeaderDeclaration(Printer out) {
     out.indent().p("static ").p(cls.getName()).p(" $__");
     out.p(cls.getName());
     Set<String> params = parameters.keySet();
     for (String param : params) {
       out.p("$").p(parameters.get(param).getMangledType());
     }
     if (0 == params.size())
       out.p("$void");
     out.p("(");
     int count = 0;
     for (String param : params) {
       if (parameters.get(param).isArray())
         out.p("__rt::Ptr<");
       parameters.get(param).translate(out);
       if (parameters.get(param).isArray())
         out.p(" >");
       out.p(" ").p(param).p(", ");
     }
     return out.p(name).pln(" __this = __rt::null());");
   }
 
   /**
    * Translates the constructor into C++ and writes
    * it to the output stream.
    *
    * @param out The output stream.
    *
    * @return The output stream.
    */
   public Printer translate(Printer out) {
     // Create the mangled name based on the parameters
     Set<String> params = parameters.keySet();
     out.indent().p(name).p(" __").p(name).p("::$__").p(name);
     for (String param : params) {
       out.p("$").p(parameters.get(param).getMangledType());
     }
     if (0 == params.size())
       out.p("$void");
     out.p("(");
     for (String param : params) {
       if (parameters.get(param).isArray())
         out.p("__rt::Ptr<");
       parameters.get(param).translate(out);
       if (parameters.get(param).isArray())
         out.p(" >");
       out.p(" ").p(param);
       out.p(", ");
     }
     out.p(name).pln(" __this) {").incr();
 
     // Call the C++ constructor
     if (null != thisCall) {
       out.indent().p("__this = ");
       thisCall.translate(out);
     } else {
       out.indent().pln("if (__rt::null() == __this)");
       out.indentMore().p("__this = new __").p(name).pln("();");
     }
 
     // Use the explicit super() call if written
     if (null != superCall) {
       out.indent();
       superCall.translate(out);
     } else if (null == thisCall) {
       JavaClass sup = cls.getParent();
       if (null != sup) {
        out.indent();
         if (!sup.getFile().getPackage().getNamespace().equals(""))
           out.p(sup.getFile().getPackage().getNamespace()).p("::");
         out.p("__").p(sup.getName()).p("::$__").p(sup.getName()).pln("$void(__this);");
       }
       
     }
 
     // Initialize class fields
     if (null == thisCall) {
       // Initialize instance variables inherited from superclasses
       if (null == superCall) {
         JavaClass temp = cls.getParent();
         while (null != temp) {
           List<JavaField> parentFields = temp.getFields();
           for (JavaField f : parentFields) {
             if (!f.isStatic())
               f.translateConstructor(out);
           }
           temp = temp.getParent();
         }
       }
       // Initialize any class instance variables
       for (JavaField f : cls.getFields()) {
         if (!f.isStatic()) {
           f.translateConstructor(out);
         }
       }
     }
 
     // Translate the body of the constructor
     body.translate(out);
 
     // Return the created instance
     out.indent().pln("return __this;");
     return out.decr().indent().pln("}");
   }
 
 }
