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
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Printer;
 import xtc.tree.Visitor;
 
 /**
  * A wrapper around the various types of Java expressions;
  * used to avoid repetitive visit methods.
  *
  * @author Nabil Hassein
  * @author Thomas Huston
  * @author Mike Morreale
  * @author Marta Wilgan
  *
  * @version 1.1
  */
 public class JavaExpression extends Visitor implements Translatable {
 
   private JavaExpression e;
   private JavaStatement s;
   private JavaType type;
   private GNode node;
 
 
   // =========================== Constructors =======================
 
   /**
    * Empty constructor for subclass use only.
    */
   protected JavaExpression() {}
      
   /**
    * Dispatches on the specified expression node.
    *
    * @param n The expression node.
    */
   public JavaExpression(GNode n, JavaStatement s) {
     this.s = s;
     this.node = n;
     dispatch(n);
   }
   
 
   // ============================ Get Methods =======================
 
   /**
    * Gets the statement the expression is in.
    *
    * @return The statement.
    */
   public JavaStatement getStatement() {
     return s;
   }
 
   /**
    * Gets the resulting type of the expression.
    *
    * @return The type.
    */
   public JavaType getType() {
     if (node.hasName("CallExpression"))
       ((CallExpression)e).determineType();
     return type;
   }
 
   /**
    * Checks if the expression has the specified name.
    *
    * @param name The name.
    *
    * @return <code>True</code> if it has the specified name;
    * <code>false</code> otherwise.
    */
   public boolean hasName(String name) {
     return node.hasName(name);
   }
 
 
   // ============================ Set Methods =======================
 
   /**
    * Sets the resulting type of the expression.
    *
    * @param type The type.
    */
   public void setType(JavaType type) {
     this.type = type;
   }
 
 
   // =========================== Visit Methods ======================
 
   /**
    * Creates a new additive expression.
    *
    * @param n The additive expression node.
    */
   public void visitAdditiveExpression(GNode n) {
     e = new AdditiveExpression(n, this);
   }
 
   /**
    * Creates a new array initializer.
    *
    * @param n The array initializer node.
    */
   public void visitArrayInitializer(GNode n) {
     e = new ArrayInitializer(n, this);
   }
 
   /**
    * Creates a new basic cast expression.
    *
    * @param n The basic cast expression node.
    */
   public void visitBasicCastExpression(GNode n) {
     e = new BasicCastExpression(n, this);
   }
   
   /**
    * Creates a new bitwise and expression.
    *
    * @param n The bitwise and expression node.
    */
   public void visitBitwiseAndExpression(GNode n) {
     e = new Expression(n, "&", this);
   }
 
   /**
    * Creates a new bitwise negation expression.
    *
    * @param n The bitwise negation expression node.
    */
   public void visitBitwiseNegationExpression(GNode n) {
     e = new Expression(n, "~", this);
   }
 
   /**
    * Creates a new bitwise or expression.
    *
    * @param n The bitwise or expression node.
    */
   public void visitBitwiseOrExpression(GNode n) {
     e = new Expression(n, "|", this);
   }
 
   /**
    * Creates a new bitwise xor expression.
    *
    * @param n The bitwise xor expression node.
    */
   public void visitBitwiseXorExpression(GNode n) {
     e = new Expression(n, "^", this);
   }
 
   /**
    * Creates a new boolean literal.
    * 
    * @param n The boolean literal node.
    */
   public void visitBooleanLiteral(GNode n) {
     e = new Literal(n, this);
   }
   
   /**
    * Creates a new call expression.
    *
    * @param n The call expression node.
    */
   public void visitCallExpression(GNode n) {
     e = new CallExpression(n, this);
   }
 
   /**
    * Creates a new cast expression.
    *
    * @param n The cast expression node.
    */
   public void visitCastExpression(GNode n) {
     e = new CastExpression(n, this);
   }
 
   /**
    * Creates a new character literal
    *
    * @param n The character literal node.
    */
   public void visitCharacterLiteral(GNode n) {
     e = new Literal(n, this);
   }
 
   /**
    * Creates a new class literal expression.
    *
    * @param n The class literal expression node.
    */
   public void visitClassLiteralExpression(GNode n) {
     e = new ClassLiteralExpression(n, this);
   }
 
   /**
    * Creates a new conditional expression.
    *
    * @param n The conditional expression node.
    */
   public void visitConditionalExpression(GNode n) {
     e = new ConditionalExpression(n, this);
   }
   
   /**
    * Creates a new equality expression.
    *
    * @param n The equality expression node.
    */
   public void visitEqualityExpression(GNode n) {
     e = new Expression(n, this);
   }
 
   /**
    * Creates a new expression.
    *
    * @param n The expression node.
    */
   public void visitExpression(GNode n) {
     e = new Expression(n, this);
   }
 
   /**
    * Creates a new floating point literal
    *
    * @param n The floating point literal node.
    */
   public void visitFloatingPointLiteral(GNode n) {
     e = new Literal(n, this);
   }
 
   /**
    * Creates a new instanceof expression.
    *
    * @param n The instance of expression node.
    */
   public void visitInstanceOfExpression(GNode n) {
     e = new InstanceOfExpression(n, this);
   }
 
   /**
    * Creates a new integer literal
    *
    * @param n The integer literal node.
    */
   public void visitIntegerLiteral(GNode n) {
     e = new Literal(n, this);
   }
 
   /**
    * Creates a new logical and expression.
    *
    * @param n The logical and expression node.
    */
   public void visitLogicalAndExpression(GNode n) {
     e = new Expression(n, "&&", this);
   }
 
   /**
    * Creates a new logical negation expression.
    *
    * @param n The logical negation expression node.
    */
   public void visitLogicalNegationExpression(GNode n) {
     e = new Expression(n, "!", this);
   }
 
   /**
    * Creates a new logical or expression.
    *
    * @param n The logical or expression node.
    */
   public void visitLogicalOrExpression(GNode n) {
     e = new Expression(n, "||", this);
   }
 
   /**
    * Creates a new multiplicative expression.
    *
    * @param n The multiplicative expression node.
    */
   public void visitMultiplicativeExpression(GNode n) {
     e = new MultiplicativeExpression(n, this);
   }
   
   /**
    * Creates a new new array expression.
    *
    * @param n The new array expression node.
    */
   public void visitNewArrayExpression(GNode n) {
     e = new NewArrayExpression(n, this);
   }
   
   /**
    * Creates a new new class expression.
    *
    * @param n The new class expression node.
    */
   public void visitNewClassExpression(GNode n) {
     e = new NewClassExpression(n, this);
   }
 
   /**
    * Creats a new null literal.
    *
    * @param n The new null literal node.
    */
   public void visitNullLiteral(GNode n) {
     e = new Literal(n, this);
   }
   
   /**
    * Creates a new postfix expression.
    *
    * @param n The postfix expression node.
    */
   public void visitPostfixExpression(GNode n) {
     e = new UnaryExpression(n, this);
   }
 
   /**
    * Creates a new primary identifier.
    *
    * @param n The primary identifier node.
    */
   public void visitPrimaryIdentifier(GNode n) {
     e = new PrimaryIdentifier(n, this);
   }
 
   /**
    * Creates a new relational expression.
    *
    * @param n The relational expression node.
    */
   public void visitRelationalExpression(GNode n) {
     e = new Expression(n, this);
   }
 
   /**
    * Creates a new selection expression.
    *
    * @param n The selection expression node.
    */
   public void visitSelectionExpression(GNode n) {
     e = new SelectionExpression(n, this);
   }
 
   /**
    * Creates a new shift expression.
    *
    * @param n The shift expression node.
    */
   public void visitShiftExpression(GNode n) {
     e = new Expression(n, this);
   }
 
   /**
    * Creates a new string literal.
    *
    * @param n The string literal node.
    */
   public void visitStringLiteral(GNode n) {
     e = new Literal(n, this);
   }
 
   /**
    * Creates a new subscript expression.
    *
    * @param n The subscript expression node.
    */
   public void visitSubscriptExpression(GNode n) {
     e = new SubscriptExpression(n, this);
   }
 
   /**
    * Creates a new this expression.
    *
    * @param n The this expression node.
    */
   public void visitThisExpression(GNode n) {
     // Nothing to do
   }
 
   /**
    * Creates a new unary expression.
    *
    * @param n The unary expression node.
    */
   public void visitUnaryExpression(GNode n) {
     e = new UnaryExpression(n, this);
   }
 
   /**
    * Creats a new variable initializer.
    *
    * @param n The variable initializer.
    */
   public void visitVariableInitializer(GNode n) {
     e = new VariableInitializer(n, this);
   }
 
 
   // ========================== Nested Classes ======================
 
   /**
    * An additive expression 
    * (for example, <code>"s" + "o"</code>
    * or <code>1 + 3</code>).
    */
   private class AdditiveExpression extends JavaExpression {
 
     private JavaExpression left, right;
     private String operator;
     private boolean isString;
 
     /**
      * Creates a new additive expression.
      *
      * @param n The additive or multiplicative expression node.
      */
     public AdditiveExpression(GNode n, JavaExpression parent) {
       left = new JavaExpression(n.getGeneric(0), parent.getStatement());
       operator = n.getString(1);
       right = new JavaExpression(n.getGeneric(2), parent.getStatement());
       if (left.getType().hasType("String") || right.getType().hasType("String")) {
         isString = true;
         parent.setType(new JavaType("String"));
       } else if (left.getType().hasType("double") || right.getType().hasType("double"))
         parent.setType(new JavaType("double"));
       else if (left.getType().hasType("float") || right.getType().hasType("float"))
         parent.setType(new JavaType("float"));
       else if (left.getType().hasType("long") || right.getType().hasType("long"))
         parent.setType(new JavaType("long"));
       else
         parent.setType(new JavaType("int"));
     }
 
     /**
      * Translates the expression and adds it 
      * to the output stream.
      *
      * @param out The output stream.
      *
      * @return The output stream.
      */
     public Printer translate(Printer out) {
       if (!isString) {
         left.translate(out);
         out.p(" ").p(operator).p(" ");
         right.translate(out);
         return out;
       } else {
         out.pln("({");
         out.incr().indent().pln("std::ostringsteam sout;");
         out.indent().p("sout << ");
         left.translate(out).p(" << ");
         right.translate(out).pln(";");
         out.indent().pln("new __String(sout.str());");
         out.decr().indent().p("})");
         return out;
       }
     }
 
   }
 
   /**
    * An array initializer
    * (for example, <code>{5, 3, 4, 6, 7}</code>).
    */
   private class ArrayInitializer extends JavaExpression {
 
     private List<JavaExpression> variables;
 
     /**
      * Creates a new array initializer.
      *
      * @param n The array initializer node.
      */
     public ArrayInitializer(GNode n, JavaExpression parent) {
       variables = new ArrayList<JavaExpression>();
       for (Object o : n) {
         variables.add(new JavaExpression((GNode)o, parent.getStatement()));
       }
       JavaType type = new JavaType(variables.get(0).getType().getJavaType());
       type.setDimensions(1); // doesn't work for multidimensional arrays
       parent.setType(type);
     }
 
     /**
      * Translates the expression and adds it 
      * to the output stream.
      *
      * @param out The output stream.
      *
      * @return The output stream.
      */
     public Printer translate(Printer out) {
       // TODO: translate array initializers to C++
       return out;
     }
 
   }
 
   /**
    * A basic cast expression
    * (for example, <code></code>).
    */
   private class BasicCastExpression extends JavaExpression {
 
     private JavaExpression e;
     private JavaType castType;
 
     /**
      * Creates a new basic cast expression.
      *
      * @param n The basic cast expression node.
      */
     public BasicCastExpression(GNode n, JavaExpression parent) {
       castType = new JavaType(n.getGeneric(0));
       if (null != n.get(1))
         castType.setDimensions(n.getNode(1).size());
       e = new JavaExpression(n.getGeneric(2), parent.getStatement());
       parent.setType(castType);
     }
 
     /**
      * Translates the expression and adds it 
      * to the output stream.
      *
      * @param out The output stream.
      *
      * @return The output stream.
      */
     public Printer translate(Printer out) {
       out.p("(");
       castType.translate(out).p(")");
       e.translate(out);
       return out;
     }
 
   }
 
   /**
    * A call expression
    * (for example, <code>System.out.println("test")</code>).
    */
   private class CallExpression extends JavaExpression {
 
     private List<JavaExpression> args;
     private List<Boolean> isObject;
     private boolean isConstructor, isPrint;
     private JavaMethod method;
     private String name;
     private List<String> ref;
     private JavaExpression parent;
 
     /**
      * Creates a new call expression.
      *
      * @param n The call expression node.
      */
     public CallExpression(GNode n, JavaExpression parent) {
       this.parent = parent;
 
       // Keep track of which arguments are object variables
       isObject = new ArrayList<Boolean>();
 
       // Check if this expression is in the constructor
       if (null != parent.getStatement().getMethod())
         isConstructor = parent.getStatement().getMethod().isConstructor();
 
       // Determine who is making the call
       if (null != n.get(0)) {
         ref = new ArrayList<String>();
         int depth = 0;
         GNode temp = n.getGeneric(0);
         if (temp.hasName("SelectionExpression")) {
           while (temp.hasName("SelectionExpression")) {
             depth++;
             temp = temp.getGeneric(0);
           }   
           for (int i = depth - 1; i >= 0; i--) {
             temp = n.getGeneric(0);
             int j = 0;
             while (j++ < i)
               temp = temp.getGeneric(0);
             if (temp.getNode(0).hasName("PrimaryIdentifier"))
               ref.add(temp.getNode(0).getString(0));
             ref.add(temp.getString(1));
           }
         } else {
           ref.add(n.getNode(0).getString(0));
         }
       }
 
       // Get the arguments passed to the method
       args = new ArrayList<JavaExpression>();
       for (Object o : n.getNode(3)) {     
         GNode node = (GNode)o;
         args.add(new JavaExpression(node, parent.getStatement()));
         if (node.hasName("PrimaryIdentifier")) {
           if (null != parent.getStatement().getMethod()) {
             if (parent.getStatement().getMethod().hasVariable(node.getString(0)) &&
               !parent.getStatement().getMethod().getVariableType(node.getString(0)).isPrimitive()) {
               isObject.add(true);
             } else if (parent.getStatement().getMethod().getClassFrom().hasVariable(node.getString(0)) &&
               !parent.getStatement().getMethod().getClassFrom().getVariableType(node.getString(0)).isPrimitive()) {
               isObject.add(false);
             }
           }
         }
         if (isObject.size() != args.size())
           isObject.add(false);
       }
 
       if (null != ref && 2 == ref.size() && ref.get(0).equals("System") && ref.get(1).equals("out")
           && (n.getString(2).equals("print") || n.getString(2).equals("println")))
         isPrint = true;
 
       // Name mangling for method overloading
       StringBuilder s = new StringBuilder();
       s.append(n.getString(2));
       if (!isPrint) {
         for (JavaExpression e : args)
           s.append("$" + e.getType().getMangledType());
       }
       name = s.toString();
     }
     
     /**
      * Determines the return type of the method call.
      */
     public void determineType() {
       Global.runtime.console().flush();
       if (null != ref) {
         if (1 == ref.size()) {
           // If it's a variable, figure out what type it is and find the method called
           if (null != parent.getStatement().getMethod() ||
               null != parent.getStatement().getClassFrom()) {
             String classType;
             JavaPackage pkg;
             Set<JavaPackage> imports;
             if (null != parent.getStatement().getMethod()) {
               if (parent.getStatement().getMethod().hasVariable(ref.get(0)))
                 classType = parent.getStatement().getMethod().getVariableType(ref.get(0)).getType();
               else
                 classType = ref.get(0);
               pkg = parent.getStatement().getMethod().getClassFrom().getFile().getPackage();
               imports = parent.getStatement().getMethod().getClassFrom().getFile().getImports();
             } else {
               if (parent.getStatement().getClassFrom().hasVariable(ref.get(0)))
                 classType = parent.getStatement().getClassFrom().getVariableType(ref.get(0)).getType();
               else
                 classType = ref.get(0);
               pkg = parent.getStatement().getClassFrom().getFile().getPackage();
               imports = parent.getStatement().getClassFrom().getFile().getImports();
             }
             // Check the current package for the class
             if (null != pkg) {
               List<JavaFile> files = pkg.getFiles();
               for (JavaFile f : files) {
                 if (f.getPublicClass().getName().equals(classType)) {
                   JavaMethod m = f.getPublicClass().getMethod(name);
                   parent.setType(m.getReturnType());
                   method = m;
                   return;
                 }
               }
             }
             // Then check the imported packages for the class
             for (JavaPackage imp : imports) {
               List<JavaFile> files = imp.getFiles();
               for (JavaFile f : files) {
                 if (f.getPublicClass().getName().equals(classType)) {
                   JavaMethod m = f.getPublicClass().getMethod(name);
                   parent.setType(m.getReturnType());
                   method = m;
                   return;
                 }
               }
             }
           } 
         } else if (2 == ref.size() && ref.get(0).equals("System") && ref.get(1).equals("out")
             && (name.equals("print") || name.equals("println"))) {
           parent.setType(new JavaType("void"));
         } else {
           // TODO: reference to some explicit package and class
           String path = "";
           int refsize = ref.size();
           for (int i = 0; i < refsize - 1; i++) {
             if (0 < i)
               path += "/";
             path += ref.get(i);
           }
           path += ".java";
           JavaFile f;
           if (Global.files.containsKey(path))
             f = Global.files.get(path);
         }
       } else {
         if (null != parent.getStatement().getMethod()) {
           JavaMethod m = parent.getStatement().getMethod().getClassFrom().getMethod(name);
           parent.setType(m.getReturnType());
           method = m;
         } else {
           JavaMethod m = parent.getStatement().getClassFrom().getMethod(name);
           parent.setType(m.getReturnType());
           method = m;
         }
       }
     }
 
     /**
      * Translates the expression and adds it 
      * to the output stream.
      *
      * @param out The output stream.
      *
      * @return The output stream.
      */
     public Printer translate(Printer out) {
       determineType();
       if (null != ref) {
         if ((name.equals("print") || name.equals("println"))) {
           if ((name.equals("print") && args.size() > 0) || name.equals("println"))
             out.p("std::cout");
           int argsize = args.size();
           for (int i = 0; i < argsize; i++) {
             out.p(" << ");
             args.get(i).translate(out);
             if (isObject.get(i)) {
               out.p("->__vptr->toString(");
               args.get(i).translate(out);
               out.p(")");
             }
           }
           if (name.equals("println"))
             out.p(" << std::endl");
         } else {
           String namespace = "";
           int refsize = ref.size();
           for (int i = 0; i < refsize; i++) {
             if (0 < i)
               namespace += "::";
             namespace += ref.get(i);
           }
           if (!method.isStatic()) {
             out.p(namespace).p("->");
             if (method.isVirtual())
               out.p("__vptr->");
           } else {
             out.p("__").p(method.getClassFrom().getName()).p("::");
           }
           out.p(name).p("(");
           if (!method.isStatic())
             out.p(namespace);
           int argsize = args.size();
           for (int i = 0; i < argsize; i++) {
             out.p(", ");
             args.get(i).translate(out);
             if (isObject.get(i)) {
               out.p("->__vptr->toString(");
               args.get(i).translate(out);
               out.p(")");
             }
           }
           out.p(")");
         }
       } else {
         if (null == ref && method.isStatic())
           out.p("__").p(method.getClassFrom().getName()).p("::");
         else if (!isConstructor)
           out.p("__this->");
         if (method.isVirtual())
           out.p("__vptr->");
         out.p(name).p("(");
         if (!method.isStatic()) {
           out.p("__this");
           if (0 < args.size());
             out.p(", ");
         }
         int size = args.size();
         for (int i = 0; i < size; i++) {
           args.get(i).translate(out);
           if (i < size - 1)
             out.p(", ");
         }
         out.p(")");
       }
       // TODO: calling methods from this class, static or instance
       return out;
     }
   }
 
   /**
    * A cast expression
    * (for example, <code>(Node)o</code>).
    */
   private class CastExpression extends JavaExpression {
 
     private JavaType type;
      
     /**
      * Creates a new cast expression.
      *
      * @param n The cast expression node.
      */
     public CastExpression(GNode n, JavaExpression parent) {
       type = new JavaType(n.getGeneric(0));
       parent.setType(type);
     }
 
     /**
      * Translates the expression and adds it 
      * to the output stream.
      *
      * @param out The output stream.
      *
      * @return The output stream.
      */
     public Printer translate(Printer out) {
       return out.p("(").p(type.getType()).p(")");
     }
 
   }
 
   /**
    * A class literal expression
    * (for example, <code></code>).
    */
   private class ClassLiteralExpression extends JavaExpression {
 
     private JavaType type;
 
     /**
      * Creates a new class literal expression.
      *
      * @param n The class literal expression node.
      */
     public ClassLiteralExpression(GNode n, JavaExpression parent) {
       type = new JavaType(n.getGeneric(0));
     }
 
     /**
      * Translates the expression and adds it 
      * to the output stream.
      *
      * @param out The output stream.
      *
      * @return The output stream.
      */
     public Printer translate(Printer out) {
       // TODO: figure out what a class literal expression is
       return out;
     }
 
   }
 
   /**
    * A conditional expression
    * (for example, <code>x &lt; 5 ? 1 : 2</code>).
    */
   private class ConditionalExpression extends JavaExpression {
 
     private JavaExpression test, ifTrue, ifFalse;
 
     /**
      * Creates a new conditional expression.
      *
      * @param n The conditional expression node.
      */
     public ConditionalExpression(GNode n, JavaExpression parent) {
       test = new JavaExpression(n.getGeneric(0), parent.getStatement());
       ifTrue = new JavaExpression(n.getGeneric(1), parent.getStatement());
       ifFalse = new JavaExpression(n.getGeneric(2), parent.getStatement());
       parent.setType(ifTrue.getType());
     }
 
     /**
      * Translates the expression and adds it 
      * to the output stream.
      *
      * @param out The output stream.
      *
      * @return The output stream.
      */
     public Printer translate(Printer out) {
       test.translate(out).p(" ? ");
       ifTrue.translate(out).p(" : ");
       return ifFalse.translate(out);
     }
 
   }
 
   /**
    * An expression 
    * (for example, <code>x = y</code>),
    * an equality expression
    * (for example, <code>x == y</code>),
    * a relational expression,
    * (for example, <code>x &lt;= y</code>),
    * a shift expression
    * (for example, <code>x &lt;&lt; 5</code>),
    * a bitwise expression
    * (for example, <code>5 &amp; 8</code>,
    * <code>~5</code>, <code>5 | 8</code>,
    * <code>5 ^ 8</code>)
    * or a logical expression
    * (for example, <code>x &amp;&amp; y</code>,
    * <code>!x</code>, <code>x || y</code>).
    */
   private class Expression extends JavaExpression {
 
     private JavaExpression left, right;
     private String operator;
 
     /**
      * Creates a new expression or equality expression.
      *
      * @param n The expression node.
      */
     public Expression(GNode n, JavaExpression parent) {
       left = new JavaExpression(n.getGeneric(0), parent.getStatement());
       operator = n.getString(1);
       right = new JavaExpression(n.getGeneric(2), parent.getStatement());
       if (n.hasName("Expression"))
         parent.setType(left.getType());
       else if (n.hasName("EqualityExpression") || n.hasName("RelationalExpression"))
         parent.setType(new JavaType("boolean"));
       else if (n.hasName("ShiftExpression"))
         parent.setType(left.getType());
     }
 
     /**
      * Creates a new bitwise or logical expression.
      *
      * @param n The expression node.
      * @param operator The operator.
      */
     public Expression(GNode n, String operator, JavaExpression parent) {
       left = new JavaExpression(n.getGeneric(0), parent.getStatement());
       this.operator = operator;
       if (!operator.equals("~") && !operator.equals("!"))
         right = new JavaExpression(n.getGeneric(1), parent.getStatement());
       if (n.getName().startsWith("Logical"))
         parent.setType(new JavaType("boolean"));
       else if (n.getName().startsWith("Bitwise"))
         if (left.getType().hasType("long") || (right != null && right.getType().hasType("long")))
           parent.setType(new JavaType("long"));
         else
           parent.setType(new JavaType("int"));
     }
 
     /**
      * Translates the expression and adds it 
      * to the output stream.
      *
      * @param out The output stream.
      *
      * @return The output stream.
      */
     public Printer translate(Printer out) {
       if (right != null) {
         left.translate(out).p(" ").p(operator).p(" ");
         return right.translate(out);
       } else {
         out.p(operator);
         return left.translate(out);
       }
     }
 
   }
 
   /**
    * An instance of expression
    * (for example, <code>x instanceof String</code>).
    */
   private class InstanceOfExpression extends JavaExpression {
 
     private JavaExpression object;
     private JavaType type;
      
     /**
      * Creates a new instance of expression.
      *
      * @param n The instance of expression node.
      */
     public InstanceOfExpression(GNode n, JavaExpression parent) {
       object = new JavaExpression(n.getGeneric(0), parent.getStatement());
       type = new JavaType(n.getGeneric(1));
       parent.setType(new JavaType("boolean"));
     }
 
     /**
      * Translates the expression and adds it 
      * to the output stream.
      *
      * @param out The output stream.
      *
      * @return The output stream.
      */
     public Printer translate(Printer out) {
       out.pln("({");
       out.incr().indent().p("Class k = __").p(type.getType()).pln("::__class();");
       out.indent().p("k->__vptr->isInstance(k, ");
       object.translate(out).pln(");");
       out.decr().indent().p("})");
       return out;
     }
 
   }
 
   /**
    * A boolean, character, floating point,
    * integer, or string literal
    * (for example, <code>true</code> or
    * <code>'a'</code> or <code>5.0</code>
    * or <code>5</code> or <code>"test"</code>).
    */
   private class Literal extends JavaExpression {
 
     private String value;
     private boolean isString;
     private boolean isNull;
 
     /**
      * Creates a new literal.
      *
      * @param n The literal node.
      */
     public Literal(GNode n, JavaExpression parent) {
       if (n.size() == 0)
         isNull = true;
       else {
         value = n.getString(0);
         if (n.hasName("BooleanLiteral"))
           parent.setType(new JavaType("boolean"));
         else if (n.hasName("CharacterLiteral"))
           parent.setType(new JavaType("char"));
         else if (n.hasName("FloatingPointLiteral"))
           if (value.charAt(value.length() - 1) == 'F' || value.charAt(value.length() - 1) == 'f')
             parent.setType(new JavaType("float"));
           else
             parent.setType(new JavaType("double"));
         else if (n.hasName("IntegerLiteral"))
           if (value.charAt(value.length() - 1) == 'L' || value.charAt(value.length() - 1) == 'l')
             parent.setType(new JavaType("long"));
           else
             parent.setType(new JavaType("int"));
         else if (n.hasName("StringLiteral")) {
           isString = true;
           parent.setType(new JavaType("String"));
         }
       }
     }
 
     /**
      * Translates the expression and adds it 
      * to the output stream.
      *
      * @param out The output stream.
      *
      * @return The output stream.
      */
     public Printer translate(Printer out) {
       if (isNull)
         return out.p("__rt::null()");
       else if (isString)
         return out.p("__rt::literal(").p(value).p(")");
       else
         return out.p(value);
     }
 
   }
 
   /**
    * A multiplicative expression
    * (for example, <code>2 * 5</code>).
    */
   private class MultiplicativeExpression extends JavaExpression {
 
     private JavaExpression left, right;
     private String operator;
 
     /**
      * Creates a new additive or multiplicative expression.
      *
      * @param n The additive or multiplicative expression node.
      */
     public MultiplicativeExpression(GNode n, JavaExpression parent) {
       left = new JavaExpression(n.getGeneric(0), parent.getStatement());
       right = new JavaExpression(n.getGeneric(2), parent.getStatement());
       operator = n.getString(1);
       if (left.getType().hasType("double") || right.getType().hasType("double"))
         parent.setType(new JavaType("double"));
       else if (left.getType().hasType("float") || right.getType().hasType("float"))
         parent.setType(new JavaType("float"));
       else if (left.getType().hasType("long") || right.getType().hasType("long"))
         parent.setType(new JavaType("long"));
       else
         parent.setType(new JavaType("int"));
     }
 
     /**
      * Translates the expression and adds it 
      * to the output stream.
      *
      * @param out The output stream.
      *
      * @return The output stream.
      */
     public Printer translate(Printer out) {
       left.translate(out);
       out.p(" ").p(operator).p(" ");
       right.translate(out);
       return out;
     }
 
   }
 
   /**
    * A new array expression
    * (for example, <code>new String[5]</code>).
    */
   private class NewArrayExpression extends JavaExpression {
 
     private List<JavaExpression> dimensions;
     private JavaType type;
     private JavaStatement parent;
 
     /**
      * Creates a new new array expression.
      *
      * @param n The new array expression node.
      */
     public NewArrayExpression(GNode n, JavaExpression parent) {
       this.parent = parent.getStatement();
       type = new JavaType(n.getGeneric(0));
       dimensions = new ArrayList<JavaExpression>();
       for (Object o : n.getNode(1)) {
         if (null == o)
           continue;
         dimensions.add(new JavaExpression((GNode)o, parent.getStatement()));
       }
       type.setDimensions(dimensions.size());
       parent.setType(type);
     }
 
     /**
      * Translates the expression and adds it 
      * to the output stream.
      *
      * @param out The output stream.
      *
      * @return The output stream.
      */
     public Printer translate(Printer out) {
       // TODO: multi-dimensional arrays don't quite work
       // we need the variable name 
       //Set<String> vars = 
       for (int i = 0; i < 1; i++) {
         out.p("new __rt::Array<");
         type.setDimensions(dimensions.size() - 1 - i);
         type.translate(out).p(">(");
         dimensions.get(i).translate(out).p(")");
         //for (int j = 0; j < )
         //if (dimensions.size() > 1 && i < dimensions.size() - 1) {
         //  out.pln(";").indent().p(variable).p(" ");
         //}
       }
       return out;
     }
 
   }
 
   /**
    * A new class expression
    * (for example, <code>new Object()</code>).
    */
   private class NewClassExpression extends JavaExpression {
 
     private List<JavaExpression> arguments;
     private JavaType type;
 
     /**
      * Creates a new new class expression.
      *
      * @param n The new class expression node.
      */
     public NewClassExpression(GNode n, JavaExpression parent) {
       type = new JavaType(n.getGeneric(2));
       arguments = new ArrayList<JavaExpression>();
       for (Object o : n.getNode(3)) {
         if (null == o)
           continue;
         arguments.add(new JavaExpression((GNode)o, parent.getStatement()));
       }
       parent.setType(type);
     }
 
     /**
      * Translates the expression and adds it 
      * to the output stream.
      *
      * @param out The output stream.
      *
      * @return The output stream.
      */
     public Printer translate(Printer out) {
       out.p("new __");
       type.translate(out);
       out.p("(");
       int size = arguments.size();
       for (int i = 0; i < size; i++) {
         arguments.get(i).translate(out);
         if (i < size - 1)
           out.p(", ");
       }
       return out.p(")");
     }
 
   }
 
   /**
    * A variable
    * (for example, <code>x</code>).
    */
   private class PrimaryIdentifier extends JavaExpression {
 
     private boolean isClassVar;
     private String name;
 
     public PrimaryIdentifier(GNode n, JavaExpression parent) {
       name = "$" + n.getString(0);
       if (null != parent.getStatement().getMethod()) {
         if (!parent.getStatement().getMethod().isConstructor() && !parent.getStatement().getMethod().hasVariable(name))
           isClassVar = true;
         if (parent.getStatement().getMethod().hasVariable(name))
           parent.setType(parent.getStatement().getMethod().getVariableType(name));
       }
       else
         if (null != parent.getStatement().getClassFrom() && parent.getStatement().getClassFrom().hasVariable(name))
           parent.setType(parent.getStatement().getClassFrom().getVariableType(name));
      Global.runtime.console().pln(n.toString()).flush();
     }
 
     /**
      * Translates the expression and adds it 
      * to the output stream.
      *
      * @param out The output stream.
      *
      * @return The output stream.
      */
     public Printer translate(Printer out) {
       if (isClassVar)
         out.p("__this->");
       return out.p(name);
     }
 
   }
 
   /**
    * A selection expression
    * (for example, <code>System.out</code>).
    */
   private class SelectionExpression extends JavaExpression {
 
     private JavaExpression identifier;
     private String selection;
 
     /**
      * Creates a new selection expression.
      *
      * @param n The selection expression node.
      */
     public SelectionExpression(GNode n, JavaExpression parent) {
       if (!n.getNode(0).hasName("ThisExpression"))
         identifier = new JavaExpression(n.getGeneric(0), parent.getStatement());
       selection = n.getString(1);
     }
 
     /**
      * Translates the expression and adds it 
      * to the output stream.
      *
      * @param out The output stream.
      *
      * @return The output stream.
      */
     public Printer translate(Printer out) {
       if (null != identifier)
         return out.p("__this->").p(selection);
       else
         return identifier.translate(out).p("::").p(selection);
     }
 
   }
 
   /**
    * A subscript expression
    * (for example, <code>a[1]</code>).
    */
   private class SubscriptExpression extends JavaExpression {
 
     private JavaExpression variable;
    private List<JavaExpression> indices;
 
     public SubscriptExpression(GNode n, JavaExpression parent) {
       variable = new JavaExpression(n.getGeneric(0), parent.getStatement());
      indices = new ArrayList<JavaExpression>();
      for (int i = 1; i < n.size(); i++)
        indices.add(new JavaExpression(n.getGeneric(i), parent.getStatement()));
       if (n.getNode(0).hasName("PrimaryIdentifier"))
         parent.getStatement().addObject(n.getNode(0).getString(0));
       parent.setType(variable.getType().getArrayType());
     }
 
     public Printer translate(Printer out) {
      for (JavaExpression e : indices)
         out.p("(*");
       variable.translate(out);
      for (JavaExpression e : indices) {
        out.p(")[");
        e.translate(out).p("]");
      }
       return out;
     }
 
   }
 
   /**
    * A postfix expression
    * (for example, <code>x++</code>),
    * or a unary expression
    * (for example, <code>++x</code>).
    */
   private class UnaryExpression extends JavaExpression {
 
     private JavaExpression identifier;
     private String pre, post;
 
     /**
      * Creates a new postfix or unary expression.
      *
      * @param n The postfix or unary expression node.
      */
     public UnaryExpression(GNode n, JavaExpression parent) {
       if (n.get(0) instanceof String) {
         pre = n.getString(0);
         identifier = new JavaExpression(n.getGeneric(1), parent.getStatement());
       } else {
         identifier = new JavaExpression(n.getGeneric(0), parent.getStatement());
         post = n.getString(1);
       }
       parent.setType(identifier.getType());
     }
 
     /**
      * Translates the expression and adds it 
      * to the output stream.
      *
      * @param out The output stream.
      *
      * @return The output stream.
      */
     public Printer translate(Printer out) {
       if (pre != null) {
         out.p(pre);
         return identifier.translate(out);
       } else {
         return identifier.translate(out).p(post);
       }
     }
 
   }
 
   /**
    * A variable initializer
    * (for example, <code></code>).
    */
   private class VariableInitializer extends JavaExpression {
 
     /**
     * Creats a new variable initializer.
     *
     * @param n The variable initializer.
     */
     public VariableInitializer(GNode n, JavaExpression parent) {
       
     }
 
     /**
      * Translates the expression and adds it 
      * to the output stream.
      *
      * @param out The output stream.
      *
      * @return The output stream.
      */
     public Printer translate(Printer out) {
       return out;
     }
 
   }
 
 
   // ======================== Translation Methods ===================
 
   /**
     * Translates the expression and adds it 
     * to the output stream.
     *
     * @param out The output stream.
     *
     * @return The output stream.
     */
   public Printer translate(Printer out) {
     return e.translate(out);
   }
 
 }
