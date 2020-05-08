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
 import java.util.Map;
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
  * @version 1.4
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
     if (null == type)
       e.determineType();
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
 
   /**
    * Checks if the type of the expression has been set.
    *
    * @return <code>True</code> if the type is set;
    * <code>false</code> otherwise.
    */
   public boolean hasType() {
     return null != type;
   }
 
 
   // ============================ Set Methods =======================
 
   /**
    * Adds the variables referenced in the expression to a list of 
    * variables to check for null.
    */
   public void checkNotNull() {
     if (null != e)
       e.checkNotNull();
   }
 
   /**
    * Determines the resulting type of the expression.
    */
   public void determineType() {}
 
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
     private JavaExpression parent;
 
     /**
      * Creates a new additive expression.
      *
      * @param n The additive or multiplicative expression node.
      * @param parent The wrapper expression.
      */
     public AdditiveExpression(GNode n, JavaExpression parent) {
       this.parent = parent;
       left = new JavaExpression(n.getGeneric(0), parent.getStatement());
       operator = n.getString(1);
       right = new JavaExpression(n.getGeneric(2), parent.getStatement());
     }
 
     /**
      * Makes sure to call checkNotNull on this variable.
      */
     public void checkNotNull() {
       if (left.hasName("CallExpression") || left.hasName("SelectionExpression") ||
           left.hasName("SubscriptExpression"))
         left.checkNotNull();
       if (right.hasName("CallExpression") || left.hasName("SelectionExpression") ||
           right.hasName("SubscriptExpression"))
         right.checkNotNull();
     }
 
     /**
      * Determines the resulting type of the expression.
      */
     public void determineType() {
       if (parent.hasType())
         return;
       if (!left.getType().isPrimitive() || !right.getType().isPrimitive()) {
         isString = true;
         parent.setType(new JavaType("String"));
       } else if (left.getType().hasType("double") || right.getType().hasType("double")) {
         parent.setType(new JavaType("double"));
       } else if (left.getType().hasType("float") || right.getType().hasType("float")) {
         parent.setType(new JavaType("float"));
       } else if (left.getType().hasType("long") || right.getType().hasType("long")) {
         parent.setType(new JavaType("long"));
       } else {
         parent.setType(new JavaType("int"));
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
       if (!isString) {
         left.translate(out);
         out.p(" ").p(operator).p(" ");
         right.translate(out);
         return out;
       } else {
         out.pln("({");
         out.incr().indent().pln("std::ostringstream sout;");
         out.indent().p("sout << ");
         left.translate(out);
         if ((0 == left.getType().getDimensions() && 
             !left.getType().isPrimitive() &&
             !left.getType().getClassType().equals("String")) ||
             left.getType().isArray()) {
           out.p("->__vptr->toString$void(");
           left.translate(out);
           out.p(")");
         }
         out.p(" << ");
         right.translate(out);
         out.flush();
         if ((0 == right.getType().getDimensions() && 
             !right.getType().isPrimitive() &&
             null != right.getType().getClassType() && 
             !right.getType().getClassType().equals("String")) ||
             right.getType().isArray()) {
           out.p("->__vptr->toString$void(");
           right.translate(out);
           out.p(")");
         }
         out.pln(";");
         out.indent().pln("String $s$ = new __String(sout.str());");
         out.indent().pln("$s$;");
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
     private JavaExpression parent;
 
     /**
      * Creates a new array initializer.
      *
      * @param n The array initializer node.
      * @param parent The wrapper expression.
      */
     public ArrayInitializer(GNode n, JavaExpression parent) {
       this.parent = parent;
       variables = new ArrayList<JavaExpression>();
       for (Object o : n) {
         variables.add(new JavaExpression((GNode)o, parent.getStatement()));
       }
     }
 
     /**
      * Makes sure to call checkNotNull on this variable.
      */
     public void checkNotNull() {
       for (JavaExpression e : variables) {
         if (e.hasName("CallExpression") || e.hasName("SelectionExpression"))
           e.checkNotNull();
       }
     }
 
     /**
      * Determines the resulting type of the expression.
      */
     public void determineType() {
       if (parent.hasType())
         return;
       JavaType type = new JavaType(variables.get(0).getType().getJavaType());
       type.setDimensions(1 + variables.get(0).getType().getDimensions());
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
       determineType();
       out.pln("({").incr();
       out.indent().p("__rt::Ptr<");
       parent.getType().translate(out).p(" > $a$ = new ");
       parent.getType().translate(out).p("(").p(variables.size()).pln(");");
       for (int i = 0; i < variables.size(); i++) {
         out.indent().p("(*$a$)[").p(i).p("] = ");
         variables.get(i).translate(out).pln(";");
       }
       out.indent().pln("$a$;");
       out.decr().indent().p("})");
       return out;
     }
 
   }
 
   /**
    * A basic cast expression
    * (for example, <code>(String)o</code>).
    */
   private class BasicCastExpression extends JavaExpression {
 
     private JavaExpression e;
     private JavaType castType;
     private JavaExpression parent;
 
     /**
      * Creates a new basic cast expression.
      *
      * @param n The basic cast expression node.
      * @param parent The wrapper expression.
      */
     public BasicCastExpression(GNode n, JavaExpression parent) {
       this.parent = parent;
       castType = new JavaType(n.getGeneric(0));
       if (null != n.get(1))
         castType.setDimensions(n.getNode(1).size());
       e = new JavaExpression(n.getGeneric(2), parent.getStatement());
     }
 
     /**
      * Makes sure to call checkNotNull on this variable.
      */
     public void checkNotNull() {
       if (e.hasName("CallExpression") || e.hasName("SelectionExpression"))
         e.checkNotNull();
     }
 
     /**
      * Determines the resulting type of the expression.
      */
     public void determineType() {
       if (parent.hasType())
         return;
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
       determineType();
       if (castType.isPrimitive()) {
         out.p("(");
         castType.translate(out).p(")(");
         return e.translate(out).p(")");
       } else {
         out.p("__rt::java_cast<");
         castType.translate(out).p(">(");
         return e.translate(out).p(")");
       }
     }
 
   }
 
   private class CallExpression extends JavaExpression {
 
     private List<JavaExpression> args;
    private boolean isConstructor, isPrint, isSuper, isSuperCall, isThis;
     private String name;
     private JavaExpression caller, parent;
     private JavaMethod method;
 
     /**
      * Creates a new call expression.
      *
      * @param n The basic cast expression node.
      * @param parent The wrapper expression.
      */
     public CallExpression(GNode n, JavaExpression parent) {
       // Save the expression wrapper
       this.parent = parent;
       
       // Check if this expression is in the constructor
       if (parent.getStatement().getScope().hasName("JavaClass"))
         isConstructor = true;
       else
         isConstructor = parent.getStatement().getScope().hasName("JavaConstructor");
 
       // The caller of the method
       if (null != n.get(0)) {
         if (n.getNode(0).hasName("SelectionExpression") &&
             n.getNode(0).getNode(0).hasName("PrimaryIdentifier") &&
             n.getNode(0).getNode(0).getString(0).equals("System") &&
             n.getNode(0).getString(1).equals("out") &&
             (n.getString(2).equals("println") || n.getString(2).equals("print")))
           isPrint = true;
         else if (n.getNode(0).hasName("SuperExpression"))
           isSuperCall = true;
         else
           caller = new JavaExpression(n.getGeneric(0), parent.getStatement());
       }
 
       // The raw name of the method
       name = n.getString(2);
       if (name.equals("this"))
         isThis = true;
       else if (name.equals("super"))
         isSuper = true;
 
       // The arguments of the method
       args = new ArrayList<JavaExpression>();
       for (Object o : n.getNode(3)) {     
         args.add(new JavaExpression((GNode)o, parent.getStatement()));
       }
     }
 
     /**
      * Makes sure to call checkNotNull on this variable.
      */
     public void checkNotNull() {
       determineType();
      if ((null != caller && null == method ||
           (null != method && !method.isStatic())) && !isSuperCall)
         caller.checkNotNull();
       for (JavaExpression e : args) {
         if (e.hasName("CallExpression") || e.hasName("SelectionExpression") ||
             e.hasName("SelectionExpression"))
           e.checkNotNull();
       }
     }
 
     /**
      * Determines the closest matching overloaded method.
      */
     public void determineMethod() {
       if (isPrint)
         return;
 
       // First, locate the class that the method is being called on
       JavaClass cls = null;
       // If there is no caller, then we're using the current class
       if (null == caller && !isPrint) {
         JavaScope temp = parent.getStatement().getScope();
         while (!temp.hasName("JavaClass"))
           temp = temp.getParentScope();
         cls = (JavaClass)temp;
         if (isThis) {
           name = "$__" + cls.getName();
         } else if (isSuper) {
           cls = cls.getParent();
           name = "$__" + cls.getName();
         } else if (isSuperCall) {
           cls = cls.getParent();
         }
       // Otherwise locate the class
      } else if (null != caller) {
         String callerClass = caller.getType().getType();
         Set<String> classes = JavaClass.getJavaClassList();
         for (String className : classes) {
           if (className.endsWith(callerClass)) {
             cls = JavaClass.getJavaClass(className);
             break;
           }
         }
       }
 
       // Used for performing a breadth-first search
       int level = 0, maxLevel = 0;
 
       // Keep track of the types of the arguments passed
       List<String> newArgTypes = new ArrayList<String>();
 
       // Get the class hierarchy
       Map<String, String> hierarchy = JavaType.getHierarchy();
 
       // Get the types of the arguments and the depth of the type furthest from Object
       for (JavaExpression e : args) {
         if (!e.getType().isPrimitive() && 0 == e.getType().getDimensions()) {
           String type = e.getType().getClassType();
           int depth = 0;
           while (null != hierarchy.get(type)) {
             depth++;
             type = hierarchy.get(type);
           }
           if (depth > maxLevel)
             maxLevel = depth;
           newArgTypes.add(e.getType().getClassType());
         } else {
           newArgTypes.add(null);
         }
       }
 
       // Loop until a match is found or we have tried every combination
       while (true) {
         // If there are no arguments, simply locate use the original method name
         if (0 == newArgTypes.size()) {
           name += "$void";
           if (null != cls && !isThis && !isSuper)
             method = cls.getMethod(name);
           return;
         }
 
         // This loop specifies the argument to climb higher in the hierarchy
         for (int i = 0; i < newArgTypes.size(); i++) {
           // This loop specifies how many levels further to climb for specified argument
           for (int j = level; j <= maxLevel; j++) {
             // Create the mangled name
             StringBuilder s = new StringBuilder();
             s.append(name);
             List<String> argTypes = new ArrayList<String>();
 
             // This loop adds the argument types to the name in order
             for (int k = 0; k < newArgTypes.size(); k++) {
               // If this is a primitive type, we can't climb up the hierarchy so simply append it
               if (null == newArgTypes.get(k)) {
                 String t = args.get(k).getType().getMangledType();
                 s.append("$" + t);
                 argTypes.add(t);
               // Otherwise climb the tree as specified by the level
               } else {
                 // Start out with the original type of the argument
                 String type = newArgTypes.get(k);
                 // Climb the tree to the current level 
                 for (int l = 1; l < level; l++) {
                   if (null == hierarchy.get(type))
                     break;
                   type = hierarchy.get(type);
                 }
                 // If this is the argument specified by the outer loop, climb extra levels
                 if (i == k) {
                   for (int m = level; m < j; m++) {
                     if (null == hierarchy.get(type))
                       break;
                     type = hierarchy.get(type);
                   }
                 }
                 s.append("$" + type);
                 argTypes.add(type);
               }
             }
             String temp = s.toString();
 
             // Special cases for methods defined in java_lang
             if (temp.equals("equals$Object") ||
                 temp.equals("charAt$int32_t")) {
               name = temp;
               return;
             }
             if ((isThis || isSuper) && null != cls.getConstructor(temp)) {
               name = temp;
               return;
             }
             if (isSuper) {
               return;
             }
             // Check if the specified method exists
             if (null != cls && null != cls.getMethod(temp)) {
               name = temp;
               method = cls.getMethod(temp);
               return;
             }
           }
         }
 
         // If the method was not found, continue up another level in the hierarchy
         level++;
         // Return if we have already reached the top of the hierarchy for all arguments
         if (level > maxLevel)
           return;
       }
     }
 
     /**
      * Determines the resulting type of the expression.
      */
     public void determineType() {
       if (parent.hasType())
         return;
       determineMethod();
 
       // Special cases for methods defined in java_lang
       if (null == method) {
         if (name.equals("hashCode$void") || name.equals("length$void")) {
           parent.setType(new JavaType("int"));
           return;
         } else if (name.equals("equals$Object") || name.equals("isPrimitive$void") ||
             name.equals("isArray")) {
           parent.setType(new JavaType("boolean"));
           return;
         } else if (name.equals("getClass$void") || name.equals("getComponentType$void") ||
             name.equals("getSuperclass$void")) {
           parent.setType(new JavaType("Class"));
           return;
         } else if (name.equals("toString$void") || name.equals("getName$void")) {
           parent.setType(new JavaType("String"));
           return;
         } else if (name.equals("charAt$int32_t")) {
           parent.setType(new JavaType("char"));
           return;
         }
       }
 
       // If found, simply use the return type of the method being called
       if (null != method) {
         parent.setType(method.getReturnType());
         return;
       }
 
       // Otherwise default to void
       parent.setType(new JavaType("void"));
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
       // Special case for print statements
       if (isPrint) {
         out.p("std::cout");
         // Inject each argument into the output stream
         int argsize = args.size();
         for (int i = 0; i < argsize; i++) {
           out.p(" << ");
           if (args.get(i).getType().getType().equals("float") ||
               args.get(i).getType().getType().equals("double")) {
             out.pln("({").incr();
             out.indent().pln("std::ostringstream sout;");
             out.indent().p("if (modf(");
             args.get(i).translate(out).pln(", new double) == 0)");
             out.indentMore().p("sout << ");
             args.get(i).translate(out).pln(" << \".0\";");
             out.indent().pln("else");
             out.indentMore().p("sout << ");
             args.get(i).translate(out).pln(";");
             out.indent().pln("String $s$ = new __String(sout.str());");
             out.indent().pln("$s$;");
             out.decr().indent().p("})");
           } else if (args.get(i).getType().getType().equals("bool")) {
             out.p("(");
             args.get(i).translate(out).p(" ? \"true\" : \"false\")");
           } else if (args.get(i).getType().getType().equals("unsigned char")) {
             out.p("(int16_t)");
             args.get(i).translate(out);
           } else {
             args.get(i).translate(out);
             // If the argument is an object, call toString()
             if ((0 == args.get(i).getType().getDimensions() && 
                 !args.get(i).getType().isPrimitive() &&
                 !args.get(i).getType().getClassType().equals("String")) ||
                 args.get(i).getType().isArray()) {
               out.p("->__vptr->toString$void(");
               args.get(i).translate(out);
               out.p(")");
             }
           }
         }
         // Append a newline if necessary
         if (name.equals("println"))
           out.p(" << std::endl");
         return out;
       } else if (isThis || isSuper) {
         JavaScope temp = parent.getStatement().getScope();
         while (!temp.hasName("JavaClass"))
           temp = temp.getParentScope();
         JavaClass cls = (JavaClass)temp;
         if (isSuper)
           cls = cls.getParent();
         if (null == cls) {
           out.p("java::lang::__Object::$__Object$void(");
           if (isSuper)
             out.p("$con$");
           return out.p(")");
         }
         if (!cls.getFile().getPackage().getNamespace().equals(""))
           out.p(cls.getFile().getPackage().getNamespace()).p("::");
         out.p("__").p(cls.getName()).p("::").p(name).p("(");
         int size = args.size();
         for (int i = 0; i < size; i++) {
           args.get(i).translate(out);
           if (i < size - 1)
             out.p(", ");
         }
         if (isSuper)
           out.p(", $con$");
         return out.p(")");
       } else if (null != caller && caller.hasName("CallExpression")) {
         out.pln("({").incr();
         out.indent();
         caller.getType().translate(out).p(" $c$ = ");
         caller.translate(out).pln(";");
         out.indent().pln("__rt::checkNotNull($c$);");
         out.indent().p("$c$");
         if (null != method && method.isStatic())
           out.p("::");
         else
           out.p("->");
         if (null == method || method.isVirtual())
           out.p("__vptr->");
         out.p(name).p("(");
         if (null == method || method.isVirtual()) {
           out.p("$c$");
           if (0 < args.size())
             out.p(", ");
         }
         int size = args.size();
         for (int i = 0; i < size; i++) {
           args.get(i).translate(out);
           if (i < size - 1)
             out.p(", ");
         }
         out.pln(");");
         out.decr().indent().p("})");
         return out;
       } else if (null != caller) {
         if (null != method && method.isStatic()) {
           if (!method.getClassFrom().getFile().getPackage().getNamespace().equals(""))
             out.p(method.getClassFrom().getFile().getPackage().getNamespace()).p("::");
           out.p("__").p(method.getClassFrom().getName()).p("::");
         } else {                                      
           caller.translate(out).p("->");
         }
       } else {
         if (null != method && method.isStatic())
           out.p("__").p(method.getClassFrom().getName()).p("::");
         else if (isConstructor && isSuperCall)
           out.p("$con$->__super->");
         else if (isConstructor)
           out.p("$con$->");
         else if (isSuperCall)
           out.p("__this->__super->");
         else 
           out.p("__this->");
       }
       if (null == method || method.isVirtual())
         out.p("__vptr->");
       out.p(name).p("(");
       if (null == caller && (null == method || !method.isStatic())) {
         if (isConstructor)
           out.p("$con$");
         else
           out.p("__this");
         if (0 < args.size())
           out.p(", ");
       } else if (null != caller && (null == method || !method.isStatic())) {
         caller.translate(out);
         if (0 < args.size())
           out.p(", ");
       }
       int size = args.size();
       for (int i = 0; i < size; i++) {
         args.get(i).translate(out);
         if (i < size - 1)
           out.p(", ");
       }
       out.p(")");
       return out;
     }
 
   }
 
   /**
    * A cast expression
    * (for example, <code>(Node)o</code>).
    */
   private class CastExpression extends JavaExpression {
 
     private JavaExpression e;
     private JavaType castType;
     private JavaExpression parent;
 
     /**
      * Creates a new cast expression.
      *
      * @param n The cast expression node.
      * @param parent The wrapper expression.
      */
     public CastExpression(GNode n, JavaExpression parent) {
       this.parent = parent;
       castType = new JavaType(n.getGeneric(0));
       e = new JavaExpression(n.getGeneric(1), parent.getStatement());
     }
 
     /**
      * Makes sure to call checkNotNull on this variable.
      */
     public void checkNotNull() {
       if (e.hasName("CallExpression") || e.hasName("SelectionExpression"))
         e.checkNotNull();
     }
 
     /**
      * Determines the resulting type of the expression.
      */
     public void determineType() {
       if (parent.hasType())
         return;
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
       determineType();
       if (castType.isPrimitive()) {
         out.p("(");
         castType.translate(out).p(")(");
         return e.translate(out).p(")");
       } else {
         out.p("__rt::java_cast<");
         castType.translate(out).p(">(");
         return e.translate(out).p(")");
       }
     }
 
   }
 
   /**
    * A class literal expression
    * (for example, <code></code>).
    */
   private class ClassLiteralExpression extends JavaExpression {
 
     private JavaType type;
     private JavaExpression parent;
 
     /**
      * Creates a new class literal expression.
      *
      * @param n The class literal expression node.
      * @param parent The wrapper expression.
      */
     public ClassLiteralExpression(GNode n, JavaExpression parent) {
       this.parent = parent;
       type = new JavaType(n.getGeneric(0));
     }
 
     /**
      * Determines the resulting type of the expression.
      */
     public void determineType() {
       if (parent.hasType())
         return;
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
       determineType();
       // TODO: figure out what a class literal expression is
       return out;
     }
 
   }
 
   /**
    * A conditional expression
    * (for example, <code>x &lt; 5 ? 1 : 2</code>).
    */
   private class ConditionalExpression extends JavaExpression {
 
     private JavaExpression test, ifTrue, ifFalse, parent;
 
     /**
      * Creates a new conditional expression.
      *
      * @param n The conditional expression node.
      * @param parent The wrapper expression.
      */
     public ConditionalExpression(GNode n, JavaExpression parent) {
       test = new JavaExpression(n.getGeneric(0), parent.getStatement());
       ifTrue = new JavaExpression(n.getGeneric(1), parent.getStatement());
       ifFalse = new JavaExpression(n.getGeneric(2), parent.getStatement());
     }
 
     /**
      * Makes sure to call checkNotNull on this variable.
      */
     public void checkNotNull() {
       if (test.hasName("CallExpression") || test.hasName("SelectionExpression"))
         test.checkNotNull();
       if (ifTrue.hasName("CallExpression") || ifTrue.hasName("SelectionExpression"))
         ifTrue.checkNotNull();
       if (ifFalse.hasName("CallExpression") || ifFalse.hasName("SelectionExpression"))
         ifFalse.checkNotNull();
     }
 
     /**
      * Determines the resulting type of the expression.
      */
     public void determineType() {
       if (parent.hasType())
         return;
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
       determineType();
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
 
     private GNode n;
     private JavaExpression left, right, parent;
     private String operator;
 
     /**
      * Creates a new expression or equality expression.
      *
      * @param n The expression node.
      * @param parent The wrapper expression.
      */
     public Expression(GNode n, JavaExpression parent) {
       this.n = n;
       this.parent = parent;
       left = new JavaExpression(n.getGeneric(0), parent.getStatement());
       operator = n.getString(1);
       right = new JavaExpression(n.getGeneric(2), parent.getStatement());
     }
 
     /**
      * Creates a new bitwise or logical expression.
      *
      * @param n The expression node.
      * @param operator The operator.
      */
     public Expression(GNode n, String operator, JavaExpression parent) {
       this.n = n;
       this.parent = parent;
       left = new JavaExpression(n.getGeneric(0), parent.getStatement());
       this.operator = operator;
       if (!operator.equals("~") && !operator.equals("!"))
         right = new JavaExpression(n.getGeneric(1), parent.getStatement()); 
     }
 
     /**
      * Makes sure to call checkNotNull on this variable.
      */
     public void checkNotNull() {
       if (!left.hasName("PrimaryIdentifier"))
         left.checkNotNull();
       if (null != right && !right.hasName("PrimaryIdentifier"))
         right.checkNotNull();
     }
 
     /**
      * Determines the resulting type of the expression.
      */
     public void determineType() {
       if (parent.hasType())
         return;
       if (n.hasName("Expression")) {
         parent.setType(left.getType());
       } else if (n.hasName("EqualityExpression") || n.hasName("RelationalExpression") ||
           n.getName().startsWith("Logical")) {
         parent.setType(new JavaType("boolean"));
       } else if (n.hasName("ShiftExpression")) {
         parent.setType(left.getType());
       } else if (n.getName().startsWith("Bitwise")) {
         if (left.getType().hasType("long") || (right != null && right.getType().hasType("long")))
           parent.setType(new JavaType("long"));
         else
           parent.setType(new JavaType("int"));
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
       if (right != null) {
         left.translate(out).p(" ").p(operator).p(" ");
         right.translate(out);
       } else {
         out.p(operator);
         left.translate(out);
       }
       return out;
     }
 
   }
 
   /**
    * An instance of expression
    * (for example, <code>x instanceof String</code>).
    */
   private class InstanceOfExpression extends JavaExpression {
 
     private JavaExpression object, parent;
     private JavaType type;
      
     /**
      * Creates a new instance of expression.
      *
      * @param n The instance of expression node.
      * @param parent The wrapper expression.
      */
     public InstanceOfExpression(GNode n, JavaExpression parent) {
       this.parent = parent;
       object = new JavaExpression(n.getGeneric(0), parent.getStatement());
       type = new JavaType(n.getGeneric(1));
     }
 
     /**
      * Makes sure to call checkNotNull on this variable.
      */
     public void checkNotNull() {
       if (object.hasName("CallExpression") || object.hasName("SelectionExpression"))
         object.checkNotNull();
     }
 
     /**
      * Determines the resulting type of the expression.
      */
     public void determineType() {
       if (parent.hasType())
         return;
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
       determineType();
       out.pln("({");
       out.incr().indent().p("Class k = __").p(type.getType()).pln("::__class();");
       out.indent().p("k->__vptr->isInstance$Object(k, ");
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
     private GNode n;
     private JavaExpression parent;
 
     /**
      * Creates a new literal.
      *
      * @param n The literal node.
      * @param parent The wrapper expression.
      */
     public Literal(GNode n, JavaExpression parent) {
       this.n = n;
       this.parent = parent;
       if (n.size() == 0)
         isNull = true;
       else {
         value = n.getString(0);
       }
     }
 
     /**
      * Determines the resulting type of the expression.
      */
     public void determineType() {
       if (parent.hasType())
         return;
       if (n.hasName("BooleanLiteral")) {
         parent.setType(new JavaType("boolean"));
       } else if (n.hasName("CharacterLiteral")) {
         parent.setType(new JavaType("char"));
       } else if (n.hasName("FloatingPointLiteral")) {
         if (value.charAt(value.length() - 1) == 'F' || value.charAt(value.length() - 1) == 'f')
           parent.setType(new JavaType("float"));
         else
           parent.setType(new JavaType("double"));
       } else if (n.hasName("IntegerLiteral")) {
         if (value.charAt(value.length() - 1) == 'L' || value.charAt(value.length() - 1) == 'l')
           parent.setType(new JavaType("long"));
         else
           parent.setType(new JavaType("int"));
       } else if (n.hasName("StringLiteral")) {
         isString = true;
         parent.setType(new JavaType("String"));
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
 
     private JavaExpression left, right, parent;
     private String operator;
 
     /**
      * Creates a new multiplicative expression.
      *
      * @param n The multiplicative expression node.
      * @param parent The wrapper expression.
      */
     public MultiplicativeExpression(GNode n, JavaExpression parent) {
       this.parent = parent;
       left = new JavaExpression(n.getGeneric(0), parent.getStatement());
       right = new JavaExpression(n.getGeneric(2), parent.getStatement());
       operator = n.getString(1);
     }
 
     /**
      * Makes sure to call checkNotNull on this variable.
      */
     public void checkNotNull() {
       if (left.hasName("CallExpression") || left.hasName("SelectionExpression"))
         left.checkNotNull();
       if (right.hasName("CallExpression") || right.hasName("SelectionExpression"))
         right.checkNotNull();
     }
 
     /**
      * Determines the resulting type of the expression.
      */
     public void determineType() {
       if (parent.hasType())
         return;
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
       determineType();
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
     private JavaStatement statement;
     private JavaExpression parent;
 
     /**
      * Creates a new new array expression.
      *
      * @param n The new array expression node.
      * @param parent The wrapper expression.
      */
     public NewArrayExpression(GNode n, JavaExpression parent) {
       this.parent = parent;
       statement = parent.getStatement();
       type = new JavaType(n.getGeneric(0));
       dimensions = new ArrayList<JavaExpression>();
       for (Object o : n.getNode(1)) {
         if (null == o)
           continue;
         dimensions.add(new JavaExpression((GNode)o, parent.getStatement()));
       }
       type.setDimensions(dimensions.size());
     }
 
     /**
      * Makes sure to call checkNotNull on this variable.
      */
     public void checkNotNull() {
       for (JavaExpression e : dimensions) {
         if (e.hasName("CallExpression") || e.hasName("SelectionExpression"))
           e.checkNotNull();
       }
     }
 
     /**
      * Determines the resulting type of the expression.
      */
     public void determineType() {
       if (parent.hasType())
         return;
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
       determineType();
       if (1 < dimensions.size()) {
         out.pln("({").incr();
         for (int i = 0; i < dimensions.size(); i++) {
           out.indent().p("__rt::Ptr<");
           type.setDimensions(dimensions.size() - i);
           type.translate(out).p(" > $a").p(i).p("$ = new ");
           type.translate(out).p("(");
           dimensions.get(i).translate(out).pln(");");
           if (i < dimensions.size() - 1) {
             out.indent().p("for (int32_t $i").p(i).p("$ = 0; $i").p(i).p("$ < ");
             dimensions.get(i).translate(out).p("; $i").p(i).pln("$++) {").incr();
           }
         }
         for (int i = dimensions.size() - 1; i > 0; i--) {
           out.indent().p("(*$a").p(i-1).p("$)[$i").p(i-1).p("$] = $a").p(i).pln("$;");
           out.decr().indent().pln("}");
         }
         out.indent().pln("$a0$;");
         out.decr().indent().p("})");
       } else {
         out.p("new ");
         type.translate(out).p("(");
         dimensions.get(0).translate(out).p(")");
       }
       return out;
     }
 
   }
 
   /**
    * A new class expression
    * (for example, <code>new Object()</code>).
    */
   private class NewClassExpression extends JavaExpression {
 
     private List<JavaExpression> args;
     private JavaClass cls;
     private String name;
     private JavaType type;
     private JavaExpression parent;
 
     /**
      * Creates a new new class expression.
      *
      * @param n The new class expression node.
      * @param parent The wrapper expression.
      */
     public NewClassExpression(GNode n, JavaExpression parent) {
       this.parent = parent;
       type = new JavaType(n.getGeneric(2));
       args = new ArrayList<JavaExpression>();
       for (Object o : n.getNode(3)) {
         if (null == o)
           continue;
         args.add(new JavaExpression((GNode)o, parent.getStatement()));
       }
     }
 
     /**
      * Makes sure to call checkNotNull on this variable.
      */
     public void checkNotNull() {
       for (JavaExpression e : args) {
         if (e.hasName("CallExpression") || e.hasName("SelectionExpression"))
           e.checkNotNull();
       }
     }
 
     /**
      * Determines the correct overloaded constructor.
      */
     public void determineConstructor() {
       // First, locate the class that we're constructing
       Set<String> classes = JavaClass.getJavaClassList();
       for (String className : classes) {
         if (className.endsWith(type.getClassType())) {
           cls = JavaClass.getJavaClass(className);
           break;
         }
       }
       name = "$__" + type.getClassType();
 
       // Used for performing a breadth-first search
       int level = 0, maxLevel = 0;
 
       // Keep track of the types of the arguments passed
       List<String> newArgTypes = new ArrayList<String>();
 
       // Get the class hierarchy
       Map<String, String> hierarchy = JavaType.getHierarchy();
 
       // Get the types of the arguments and the depth of the type furthest from Object
       for (JavaExpression e : args) {
         if (!e.getType().isPrimitive() && 0 == e.getType().getDimensions()) {
           String type = e.getType().getClassType();
           int depth = 0;
           while (null != hierarchy.get(type)) {
             depth++;
             type = hierarchy.get(type);
           }
           if (depth > maxLevel)
             maxLevel = depth;
           newArgTypes.add(e.getType().getClassType());
         } else {
           newArgTypes.add(null);
         }
       }
 
       // Loop until a match is found or we have tried every combination
       while (true) {
         // If there are no arguments, simply locate use the original method name
         if (0 == newArgTypes.size()) {
           name += "$void";
           return;
         }
 
         // This loop specifies the argument to climb higher in the hierarchy
         for (int i = 0; i < newArgTypes.size(); i++) {
           // This loop specifies how many levels further to climb for specified argument
           for (int j = level; j <= maxLevel; j++) {
             // Create the mangled name
             StringBuilder s = new StringBuilder();
             s.append(name);
             List<String> argTypes = new ArrayList<String>();
 
             // This loop adds the argument types to the name in order
             for (int k = 0; k < newArgTypes.size(); k++) {
               // If this is a primitive type, we can't climb up the hierarchy so simply append it
               if (null == newArgTypes.get(k)) {
                 String t = args.get(k).getType().getMangledType();
                 s.append("$" + t);
                 argTypes.add(t);
               // Otherwise climb the tree as specified by the level
               } else {
                 // Start out with the original type of the argument
                 String type = newArgTypes.get(k);
                 // Climb the tree to the current level 
                 for (int l = 1; l < level; l++) {
                   if (null == hierarchy.get(type))
                     break;
                   type = hierarchy.get(type);
                 }
                 // If this is the argument specified by the outer loop, climb extra levels
                 if (i == k) {
                   for (int m = level; m < j; m++) {
                     if (null == hierarchy.get(type))
                       break;
                     type = hierarchy.get(type);
                   }
                 }
                 s.append("$" + type);
                 argTypes.add(type);
               }
             }
             String temp = s.toString();
 
             if (null != cls.getConstructor(temp)) {
               name = temp;
               return;
             }
           }
         }
 
         // If the method was not found, continue up another level in the hierarchy
         level++;
         // Return if we have already reached the top of the hierarchy for all arguments
         if (level > maxLevel)
           return;
       }
     
     }
 
     /**
      * Determines the resulting type of the expression.
      */
     public void determineType() {
       if (parent.hasType())
         return;
       determineConstructor();
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
       determineType();
       if (null != cls && !cls.getFile().getPackage().getNamespace().equals(""))
         out.p(cls.getFile().getPackage().getNamespace()).p("::");
       out.p("__").p(type.getClassType()).p("::").p(name).p("(");
       int argsize = args.size();
       for (int i = 0; i < argsize; i++) {
         args.get(i).translate(out);
         if (i < argsize - 1)
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
 
     private boolean isClass, isClassVar;
     private String name;
     private JavaExpression parent;
     private JavaPackage pkg;
 
     /**
      * Creates a new primary identifier.
      *
      * @param n The primary identifier node.
      * @param parent The wrapper expression.
      */
     public PrimaryIdentifier(GNode n, JavaExpression parent) {
       // Save the wrapper expression
       this.parent = parent;
       // The name of the identifier
       name = n.getString(0);
     }
 
     /**
      * Makes sure to call checkNotNull on this variable.
      */
     public void checkNotNull() {
       determineType();
       StringBuilder check = new StringBuilder();
       if (parent.getStatement().getScope().isInScope("$" + name)) {
         if (isClassVar) {
           JavaClass scope = (JavaClass)parent.getStatement().getScope().getVariableScope("$" + name);
           if (!scope.getFile().getPackage().getNamespace().equals(""))
             check.append(scope.getFile().getPackage().getNamespace() + "::");
           check.append("__" + scope.getName() + "::");
         } else if (parent.getStatement().getScope().getVariableScope("$" + name).hasName("JavaClass")) {
           if (parent.getStatement().getScope().hasName("JavaConstructor"))
             check.append("$con$->");
           else
             check.append("__this->");
         }
         check.append("$" + name);
       } else {
         if (isClass) {
           return;
         } else {
           check.append("$" + name);
         }
       }
       parent.getStatement().addObject(check.toString());
     }
 
     /**
      * Determines the resulting type of the expression.
      */
     public void determineType() {
       if (parent.hasType())
         return;
       // Check if this is a variable currently in scope
       if (parent.getStatement().getScope().isInScope("$" + name)) {
         parent.setType(parent.getStatement().getScope().getVariableType("$" + name));
         // Check if it's a class variable
         if (parent.getStatement().getScope().getVariableScope("$" + name).hasName("JavaClass")
             && parent.getStatement().getScope().getVariableType("$" + name).isStatic()) {
           isClassVar = true;
         }
         return;
       // Check if this refers to a class
       } else {
         JavaScope temp = parent.getStatement().getScope();
         while (!temp.hasName("JavaClass"))
           temp = temp.getParentScope();
         JavaClass cls = (JavaClass)temp;
         // Look in the current package
         JavaPackage pkg = cls.getFile().getPackage();
         for (JavaFile f : pkg.getFiles()) {
           if (name.equals(f.getPublicClass().getName())) {
             isClass = true;
             this.pkg = pkg;
             JavaType type = new JavaType(name);
             type.setStatic();
             parent.setType(type);
             return;
           }
         }
         // Then look in the imported packages
         Set<JavaPackage> imports = cls.getFile().getImports();
         for (JavaPackage imp : imports) {
           for (JavaFile f : imp.getFiles()) {
             if (name.equals(f.getPublicClass().getName())) {
               isClass = true;
               this.pkg = imp;
               JavaType type = new JavaType(name);
               type.setStatic();
               parent.setType(type);
               return;
             }
           }
         }
         // If it still isn't found, set the type to void
         parent.setType(new JavaType("void"));
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
       // Now that we have all classes available, determine the type
       determineType();
       // Check if it's a variable currently in scope
       if (parent.getStatement().getScope().isInScope("$" + name)) {
         // If it's a static class variable, print out the class first
         if (isClassVar) {
           JavaClass scope = (JavaClass)parent.getStatement().getScope().getVariableScope("$" + name);
           if (!scope.getFile().getPackage().getNamespace().equals(""))
             out.p(scope.getFile().getPackage().getNamespace()).p("::");
           out.p("__").p(scope.getName()).p("::");
         // Check if it's an instance variable
         } else if (parent.getStatement().getScope().getVariableScope("$" + name).hasName("JavaClass")) {
           if (parent.getStatement().getScope().hasName("JavaConstructor"))
             out.p("$con$->");
           else
             out.p("__this->");
         }
         return out.p("$").p(name);
       } else {
         // If this is a class, print out the full namespace and class name
         if (isClass) {
           if (!pkg.getNamespace().equals(""))
             out.p(pkg.getNamespace()).p("::");
           return out.p("__").p(name);
         // Otherwise just print out the variable name
         } else {
           return out.p("$" + name);
         }
       }
     }
   }
 
   /**
    * A selection expression
    * (for example, <code>System.out</code>).
    */
   private class SelectionExpression extends JavaExpression {
 
     private boolean isClass, isSuper, isThis, isVariable;
     private JavaExpression identifier, parent;
     private String selection;
 
     /**
      * Creates a new selection expression.
      *
      * @param n The selection expression node.
      */
     public SelectionExpression(GNode n, JavaExpression parent) {
       this.parent = parent;
       if (n.getNode(0).hasName("ThisExpression"))
         isThis = true;
       else if (n.getNode(0).hasName("SuperExpression"))
         isSuper = true;
       else
         identifier = new JavaExpression(n.getGeneric(0), parent.getStatement());
       selection = n.getString(1);
     }
 
     /**
      * Makes sure to call checkNotNull on this variable.
      */
     public void checkNotNull() {
       determineType();
       if (null != identifier && (identifier.hasName("CallExpression") || 
           (!identifier.getType().isArray() && isClass)))
         identifier.checkNotNull();
     }
 
     /**
      * Determines the resulting type of the expression.
      */
     public void determineType() {
       if (parent.hasType())
         return;
       String name;
       if (null != identifier)
         name = identifier.getType().getType();
       else
         name = selection;
       JavaScope temp = parent.getStatement().getScope();
       while (!temp.hasName("JavaClass"))
         temp = temp.getParentScope();
       JavaClass cls = (JavaClass)temp;
       if (isThis || isSuper) {
         parent.setType(parent.getStatement().getScope().getVariableType("$" + selection));
         isVariable = true;
         return;
       }
       // Look in the current package
       JavaPackage pkg = cls.getFile().getPackage();
       for (JavaFile f : pkg.getFiles()) {
         if (name.equals(f.getPublicClass().getName())) {
           cls = f.getPublicClass();
           if (null != identifier && identifier.getType().isStatic() &&
             cls.isInScope("$" + selection)) {
             parent.setType(cls.getVariableType("$" + selection));
             isVariable = true;
           } else {
             JavaType type = new JavaType(selection);
             type.setStatic();
             parent.setType(type);
             isClass = true;
           }
           return;
         }
       }
       // Then look in the imported packages
       Set<JavaPackage> imports = cls.getFile().getImports();
       for (JavaPackage imp : imports) {
         for (JavaFile f : imp.getFiles()) {
           if (name.equals(f.getPublicClass().getName())) {
             cls = f.getPublicClass();
             if (null != identifier && identifier.getType().isStatic() &&
               cls.isInScope("$" + selection)) {
               parent.setType(cls.getVariableType("$" + selection));
               isVariable = true;
             } else {
               JavaType type = new JavaType(selection);
               type.setStatic();
               parent.setType(type);
               isClass = true;
             }
             return;
           }
         }
       }
       parent.setType(new JavaType("void"));
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
       if (isThis || isSuper) {
         if (parent.getStatement().getScope().hasName("JavaConstructor"))
           out.p("$con$->");
         else
           out.p("__this->");
         return out.p("$").p(selection);
       }
       /*if (isSuper) {
         if (parent.getStatement().getScope().hasName("JavaConstructor"))
           out.p("$con$->__super->");
         else
           out.p("__this->__super->");
         for (int i = 1; i < depth; i++) {
           out.p("__super->");
         }
         return out.p("$").p(selection);
       }*/
       if (null != identifier) {
         if (identifier.getType().isArray()) {
           return identifier.translate(out).p("->").p(selection);
         } else if (isClass) {
           return identifier.translate(out).p("->$").p(selection);
         } else {
           identifier.translate(out).p("::");
         }
       }
       if (isVariable)
         return out.p("$").p(selection);
       else
         return out.p("__").p(selection);
     }
 
   }
 
   /**
    * A subscript expression
    * (for example, <code>a[1]</code>).
    */
   private class SubscriptExpression extends JavaExpression {
 
     private GNode n;
     private JavaExpression variable, parent;
     private List<JavaExpression> indices;
 
     /**
      * Creates a new subscript expression.
      *
      * @param n The subscript expression node.
      * @param parent The wrapper expression.
      */
     public SubscriptExpression(GNode n, JavaExpression parent) {
       this.n = n;
       this.parent = parent;
       variable = new JavaExpression(n.getGeneric(0), parent.getStatement());
       indices = new ArrayList<JavaExpression>();
       for (int i = 1; i < n.size(); i++)
         indices.add(new JavaExpression(n.getGeneric(i), parent.getStatement()));
     }
 
     /**
      * Makes sure to call checkNotNull on this variable.
      */
     public void checkNotNull() {
       if (n.getNode(0).hasName("PrimaryIdentifier") &&
           parent.getStatement().getScope().isInScope("$" + n.getNode(0).getString(0))) {
         if (parent.getStatement().getScope().getVariableScope("$" + n.getNode(0).getString(0)).equals("JavaClass")) {
           if (parent.getStatement().getScope().hasName("JavaConstructor") ||
               parent.getStatement().getScope().hasName("JavaClass"))
             parent.getStatement().addObject("$con$->$" + n.getNode(0).getString(0));
           else
             parent.getStatement().addObject("__this->$" + n.getNode(0).getString(0));
         } else {
           parent.getStatement().addObject("$" + n.getNode(0).getString(0));
         }
       }
       if (variable.hasName("CallExpression") || variable.hasName("SelectionExpression") || 
           variable.hasName("SubscriptExpression"))
         variable.checkNotNull();
       for (JavaExpression e : indices) {
         if (e.hasName("CallExpression") || e.hasName("SelectionExpression") || e.hasName("SubscriptExpression"))
           e.checkNotNull();
       }
     }
 
     /**
      * Determines the resulting type of the expression.
      */
     public void determineType() {
       if (parent.hasType())
         return;
       parent.setType(variable.getType().getArrayType());
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
 
     private JavaExpression identifier, parent;
     private String pre, post;
 
     /**
      * Creates a new postfix or unary expression.
      *
      * @param n The postfix or unary expression node.
      * @param parent The wrapper expression.
      */
     public UnaryExpression(GNode n, JavaExpression parent) {
       this.parent = parent;
       if (n.get(0) instanceof String) {
         pre = n.getString(0);
         identifier = new JavaExpression(n.getGeneric(1), parent.getStatement());
       } else {
         identifier = new JavaExpression(n.getGeneric(0), parent.getStatement());
         post = n.getString(1);
       }
     }
 
     /**
      * Makes sure to call checkNotNull on this variable.
      */
     public void checkNotNull() {
       if (identifier.hasName("CallExpression") || identifier.hasName("SelectionExpression"))
         identifier.checkNotNull();
     }
 
     /**
      * Determines the resulting type of the expression.
      */
     public void determineType() {
       if (parent.hasType())
         return;
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
       determineType();
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
 
     private JavaExpression parent;
 
     /**
     * Creats a new variable initializer.
     *
     * @param n The variable initializer.
     */
     public VariableInitializer(GNode n, JavaExpression parent) {
       this.parent = parent;
     }
 
     /**
      * Determines the resulting type of the expression.
      */
     public void determineType() {
       if (parent.hasType())
         return;
       parent.setType(new JavaType("void"));// TODO: fix this
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
