 // -*- mode: java -*- 
 //
 // file: cool-tree.m4
 //
 // This file defines the AST
 //
 //////////////////////////////////////////////////////////
 
 import java.util.Enumeration;
 import java.io.PrintStream;
 import java.util.Vector;
 import java.util.*;
 import java.lang.*;
 
 /** Defines simple phylum Program */
 abstract class Program extends TreeNode {
     protected Program(int lineNumber) {
         super(lineNumber);
     }
     public abstract void dump_with_types(PrintStream out, int n);
     public abstract void semant();
 
 }
 
 
 /** Defines simple phylum Class_ */
 abstract class Class_ extends TreeNode {
     protected Class_(int lineNumber) {
         super(lineNumber);
     }
     public abstract void dump_with_types(PrintStream out, int n);
 
 }
 
 
 /** Defines list phylum Classes
     <p>
     See <a href="ListNode.html">ListNode</a> for full documentation. */
 class Classes extends ListNode {
     public final static Class elementClass = Class_.class;
     /** Returns class of this lists's elements */
     public Class getElementClass() {
         return elementClass;
     }
     protected Classes(int lineNumber, Vector elements) {
         super(lineNumber, elements);
     }
     /** Creates an empty "Classes" list */
     public Classes(int lineNumber) {
         super(lineNumber);
     }
     /** Appends "Class_" element to this list */
     public Classes appendElement(TreeNode elem) {
         addElement(elem);
         return this;
     }
     public TreeNode copy() {
         return new Classes(lineNumber, copyElements());
     }
 }
 
 
 /** Defines simple phylum Feature */
 abstract class Feature extends TreeNode {
     protected Feature(int lineNumber) {
         super(lineNumber);
     }
     public abstract void dump_with_types(PrintStream out, int n);
 
 }
 
 
 /** Defines list phylum Features
     <p>
     See <a href="ListNode.html">ListNode</a> for full documentation. */
 class Features extends ListNode {
     public final static Class elementClass = Feature.class;
     /** Returns class of this lists's elements */
     public Class getElementClass() {
         return elementClass;
     }
     protected Features(int lineNumber, Vector elements) {
         super(lineNumber, elements);
     }
     /** Creates an empty "Features" list */
     public Features(int lineNumber) {
         super(lineNumber);
     }
     /** Appends "Feature" element to this list */
     public Features appendElement(TreeNode elem) {
         addElement(elem);
         return this;
     }
     public TreeNode copy() {
         return new Features(lineNumber, copyElements());
     }
 }
 
 
 /** Defines simple phylum Formal */
 abstract class Formal extends TreeNode {
     protected Formal(int lineNumber) {
         super(lineNumber);
     }
     public abstract void dump_with_types(PrintStream out, int n);
 
 }
 
 
 /** Defines list phylum Formals
     <p>
     See <a href="ListNode.html">ListNode</a> for full documentation. */
 class Formals extends ListNode {
     public final static Class elementClass = Formal.class;
     /** Returns class of this lists's elements */
     public Class getElementClass() {
         return elementClass;
     }
     protected Formals(int lineNumber, Vector elements) {
         super(lineNumber, elements);
     }
     /** Creates an empty "Formals" list */
     public Formals(int lineNumber) {
         super(lineNumber);
     }
     /** Appends "Formal" element to this list */
     public Formals appendElement(TreeNode elem) {
         addElement(elem);
         return this;
     }
     public TreeNode copy() {
         return new Formals(lineNumber, copyElements());
     }
 }
 
 
 /** Defines simple phylum Expression */
 abstract class Expression extends TreeNode {
     protected Expression(int lineNumber) {
         super(lineNumber);
     }
     private AbstractSymbol type = null;                                 
     public AbstractSymbol get_type() { return type; }           
     public Expression set_type(AbstractSymbol s) { type = s; return this; } 
     public abstract void dump_with_types(PrintStream out, int n);
     public void dump_type(PrintStream out, int n) {
         if (type != null)
             { out.println(Utilities.pad(n) + ": " + type.getString()); }
         else
             { out.println(Utilities.pad(n) + ": _no_type"); }
     }
 
 }
 
 
 /** Defines list phylum Expressions
     <p>
     See <a href="ListNode.html">ListNode</a> for full documentation. */
 class Expressions extends ListNode {
     public final static Class elementClass = Expression.class;
     /** Returns class of this lists's elements */
     public Class getElementClass() {
         return elementClass;
     }
     protected Expressions(int lineNumber, Vector elements) {
         super(lineNumber, elements);
     }
     /** Creates an empty "Expressions" list */
     public Expressions(int lineNumber) {
         super(lineNumber);
     }
     /** Appends "Expression" element to this list */
     public Expressions appendElement(TreeNode elem) {
         addElement(elem);
         return this;
     }
     public TreeNode copy() {
         return new Expressions(lineNumber, copyElements());
     }
 }
 
 
 /** Defines simple phylum Case */
 abstract class Case extends TreeNode {
     protected Case(int lineNumber) {
         super(lineNumber);
     }
     public abstract void dump_with_types(PrintStream out, int n);
 
 }
 
 
 /** Defines list phylum Cases
     <p>
     See <a href="ListNode.html">ListNode</a> for full documentation. */
 class Cases extends ListNode {
     public final static Class elementClass = Case.class;
     /** Returns class of this lists's elements */
     public Class getElementClass() {
         return elementClass;
     }
     protected Cases(int lineNumber, Vector elements) {
         super(lineNumber, elements);
     }
     /** Creates an empty "Cases" list */
     public Cases(int lineNumber) {
         super(lineNumber);
     }
     /** Appends "Case" element to this list */
     public Cases appendElement(TreeNode elem) {
         addElement(elem);
         return this;
     }
     public TreeNode copy() {
         return new Cases(lineNumber, copyElements());
     }
 }
 
 
 /** Defines AST constructor 'programc'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class programc extends Program {
     /* Initialize two MySymbolTables - one for method and one for objects */
     HashMap<String, MySymbolTable> objectSymTabMap = new HashMap<String, MySymbolTable>(); 
     HashMap<String, MySymbolTable> methodSymTabMap = new HashMap<String, MySymbolTable>();
     HashMap< String, HashMap< String, ArrayList<AbstractSymbol>>> methodEnvironment;
     protected Classes classes;
     ClassTable classTable;
     /** Creates "programc" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for classes
       */
     public programc(int lineNumber, Classes a1) {
         super(lineNumber);
         classes = a1;
     }
     public TreeNode copy() {
         return new programc(lineNumber, (Classes)classes.copy());
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "programc\n");
         classes.dump(out, n+2);
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_program");
         for (Enumeration e = classes.getElements(); e.hasMoreElements(); ) {
             // sm: changed 'n + 1' to 'n + 2' to match changes elsewhere
 	    ((Class_)e.nextElement()).dump_with_types(out, n + 2);
         }
     }
     /** This method is the entry point to the semantic checker.  You will
         need to complete it in programming assignment 4.
 	<p>
         Your checker should do the following two things:
 	<ol>
 	<li>Check that the program is semantically correct
 	<li>Decorate the abstract syntax tree with type information
         by setting the type field in each Expression node.
         (see tree.h)
 	</ol>
 	<p>
 	You are free to first do (1) and make sure you catch all semantic
     	errors. Part (2) can be done in a second stage when you want
 	to test the complete compiler.
     */
     public void semant() {
 	/* ClassTable constructor may do some semantic analysis */
 	classTable = new ClassTable(classes);
 	
     // Abort if there are errors in the inheritanc graph
     if (classTable.errors()) {
         System.err.println("Compilation halted due to static semantic errors.");
         System.exit(1);
     }
 	/* some semantic analysis code may go here */
 
     /* Traverse the AST top-down and when the leaves are reached, fill in the type information
         as we're winding back up. This does Scoping/Naming 
     */
     /* Traverse basic classes first */
     Classes basicClasses = classTable.getBasicClassList();
     methodEnvironment = new HashMap< String, HashMap< String, ArrayList<AbstractSymbol>>>(); 
     traverseAST(basicClasses);
     traverseAST(classes);
     /* Perform type checking with another traversal */
     if (Flags.semant_debug) {
         System.out.println(methodEnvironment);
     }
     traverseASTWithTypecheck(classes);
 
     if (classTable.errors()) {
         System.err.println("Compilation halted due to static semantic errors.");
         System.exit(1);
     }
     }
 
     /** Traverses AST and does 1. Scoping 2. Type Checking **/
     private void traverseAST(Classes classes) {
 	/* Loop through each class */
 	for (Enumeration e = classes.getElements(); e.hasMoreElements(); ) {
         class_c currentClass = ((class_c)e.nextElement());
 	    if (Flags.semant_debug) {
                System.out.println("First Pass Class " + currentClass.getName().toString());
         }
 	    /* Inside each class, start new scope, traverse down the class AST */
 	    objectSymTabMap.put(currentClass.getName().toString(), new MySymbolTable());
         MySymbolTable objectSymTab = objectSymTabMap.get(currentClass.getName().toString());
         objectSymTab.enterScope();
 	    methodEnvironment.put(currentClass.getName().toString(), new HashMap<String,ArrayList<AbstractSymbol>>());
         HashMap<String, ArrayList<AbstractSymbol>> methodSymTab = methodEnvironment.get(currentClass.getName().toString());
 	    Features features = currentClass.getFeatures();
 	    for (Enumeration fe = features.getElements(); fe.hasMoreElements();) {
             Feature f = ((Feature)fe.nextElement());
 	        if ( f instanceof attr ) {
 		    //System.out.println("Attribute ");
  		    if (Flags.semant_debug) {
 		    	System.out.println("Traversing Attribute : " + ((attr)f).getName().toString());
 		    }
 		    // Add attribute to object Symbol Table,if already present, scope error
 		    if (objectSymTab.lookup(((attr)f).getName()) != null) {
 			classTable.semantError(currentClass.getFilename(), f).println("Attribute " + ((attr)f).getName().toString() + " is multiply defined");
 		    }
 		    
   	 	    //add attribute to symbol table, overwrite if already there
 		    objectSymTab.addId(((attr)f).getName(), ((attr)f).getType());
 		    traverseExpression(currentClass, ((attr)f).getExpression(), objectSymTab, null);
         } else {
 		    if (Flags.semant_debug) {
 		    	System.out.println("Traversing Method : " + ((method)f).getName().toString());
 		    }
 		    // Add method to method Symbol Table,if already present, scope error
 		    if (methodSymTab.containsKey(((method)f).getName().toString())) {
 			classTable.semantError(currentClass.getFilename(), f).println("Method " + ((method)f).getName().toString() + " is multiply defined");
 		    }
 		    traverseMethod(currentClass, ((method)f), objectSymTab);
 		}	
 	    }
         }
     }
 
     /** Traverse method. Check formal parameters, return type and expressions **/
     private void traverseMethod(class_c currentClass, method m, MySymbolTable objectSymTab) {
 	String className = currentClass.getName().toString();
     // Start a new scope
 	objectSymTab.enterScope();
 	// Traverse formal arguments, adding them to scope
 	Formals formals = m.getFormals();
 	String methodname = m.getName().toString();
 
     if (methodEnvironment.get(className).get(methodname) == null) {
             methodEnvironment.get(className).put(methodname, new ArrayList<AbstractSymbol>());
     }
 
 	for (Enumeration e = formals.getElements(); e.hasMoreElements();) {
         formalc formal = ((formalc)e.nextElement());
 		if (objectSymTab.probe(formal.getName()) != null) {
 			classTable.semantError(currentClass.getFilename(), formal).println("Formal parameter " + formal.getName().toString() + " is multiply defined");
 		}
 		// Recover from multiply defined formal parameter. Just overwrite it
 		objectSymTab.addId(formal.getName(), formal.getType());
 		methodEnvironment.get(className).get(methodname).add(formal.getType());
 	}
     methodEnvironment.get(className).get(methodname).add(m.getReturnType());
 	// Traverse expression
     traverseExpression(currentClass, m.getExpression(), objectSymTab, null);
 
 	objectSymTab.exitScope();
     }
 
     /** Depending on what kind of expression, traverse down and fill in types and do scoping **/
     private void traverseExpression(class_c currentClass, Expression expression, MySymbolTable objectSymTab, MySymbolTable methodSymTab) {
         if (expression instanceof object) {
             if ( ((object)expression).getName() == TreeConstants.self ) {
                 expression.set_type(TreeConstants.SELF_TYPE);
             } else if (objectSymTab.lookup(((object)expression).getName()) == null) {
                 classTable.semantError(currentClass.getFilename(),expression).println("Undeclared identifier " + ((object)expression).getName());
                 expression.set_type(TreeConstants.Object_);
             } else {
                 // Set the type of this object from the symbol table, if it exists
                 expression.set_type((AbstractSymbol)objectSymTab.lookup(((object)expression).getName()));
             }
         } else if (expression instanceof string_const) {
             expression.set_type(TreeConstants.Str);
         } else if (expression instanceof bool_const) {
             expression.set_type(TreeConstants.Bool);
         } else if (expression instanceof int_const) {
             expression.set_type(TreeConstants.Int);
         } else if (expression instanceof isvoid) {
             expression.set_type(TreeConstants.Bool);
             traverseExpression(currentClass, ((isvoid)expression).getExpression(), objectSymTab, methodSymTab);
         } else if (expression instanceof new_) {
             expression.set_type(((new_)expression).getTypeName());
         } else if (expression instanceof comp) {
             expression.set_type(TreeConstants.Bool);
             traverseExpression(currentClass, ((comp)expression).getExpression(), objectSymTab, methodSymTab);
         } else if (expression instanceof eq) {
             expression.set_type(TreeConstants.Bool);
             traverseExpression(currentClass, ((eq)expression).getLHS(), objectSymTab, methodSymTab);
             traverseExpression(currentClass, ((eq)expression).getRHS(), objectSymTab, methodSymTab);
         } else if (expression instanceof leq) {
             expression.set_type(TreeConstants.Bool);
             traverseExpression(currentClass, ((leq)expression).getLHS(), objectSymTab, methodSymTab);
             traverseExpression(currentClass, ((leq)expression).getRHS(), objectSymTab, methodSymTab);
         } else if (expression instanceof lt) {
             expression.set_type(TreeConstants.Bool);
             traverseExpression(currentClass, ((lt)expression).getLHS(), objectSymTab, methodSymTab);
             traverseExpression(currentClass, ((lt)expression).getRHS(), objectSymTab, methodSymTab);
         } else if (expression instanceof neg) {
             expression.set_type(TreeConstants.Int);
             traverseExpression(currentClass, ((neg)expression).getExpression(), objectSymTab, methodSymTab);
         } else if (expression instanceof divide) {
             expression.set_type(TreeConstants.Int);
             traverseExpression(currentClass, ((divide)expression).getLHS(), objectSymTab, methodSymTab);
             traverseExpression(currentClass, ((divide)expression).getRHS(), objectSymTab, methodSymTab);
         } else if (expression instanceof sub) {
             expression.set_type(TreeConstants.Int);
             traverseExpression(currentClass, ((sub)expression).getLHS(), objectSymTab, methodSymTab);
             traverseExpression(currentClass, ((sub)expression).getRHS(), objectSymTab, methodSymTab);
         } else if (expression instanceof mul) {
             expression.set_type(TreeConstants.Int);
             traverseExpression(currentClass, ((mul)expression).getLHS(), objectSymTab, methodSymTab);
             traverseExpression(currentClass, ((mul)expression).getRHS(), objectSymTab, methodSymTab);
         } else if (expression instanceof plus) {
             expression.set_type(TreeConstants.Int);
             traverseExpression(currentClass, ((plus)expression).getLHS(), objectSymTab, methodSymTab);
             traverseExpression(currentClass, ((plus)expression).getRHS(), objectSymTab, methodSymTab);
         } else if (expression instanceof loop) {
             expression.set_type(TreeConstants.Object_);
             traverseExpression(currentClass, ((loop)expression).getPredicate(), objectSymTab, methodSymTab);
             traverseExpression(currentClass, ((loop)expression).getBody(), objectSymTab, methodSymTab);
         } else if (expression instanceof let) {
             // Start a new scope, and add ID to Mysymboltable
             objectSymTab.enterScope();
             let letExpression = (let)expression;
             objectSymTab.addId(letExpression.getIdentifier(), letExpression.getType());
             traverseExpression(currentClass, letExpression.getInit(), objectSymTab, methodSymTab);
             traverseExpression(currentClass, letExpression.getBody(), objectSymTab, methodSymTab);
             // The type of let is the type if the last statement in the body
             letExpression.set_type(letExpression.getBody().get_type());
             objectSymTab.exitScope();
         } else if (expression instanceof block) {
             // Type of block is type of last expression
             Expressions body = ((block)expression).getBody();
             AbstractSymbol lastType = null;
             for (Enumeration e = body.getElements(); e.hasMoreElements();) {
                 Expression nextExpression = (Expression)e.nextElement();
                 traverseExpression(currentClass, nextExpression, objectSymTab, methodSymTab);
                 lastType = nextExpression.get_type();
             }
             expression.set_type(lastType);
         } else if (expression instanceof static_dispatch) {
             static_dispatch e = (static_dispatch)expression;
             traverseExpression( currentClass, e.getExpression(), objectSymTab, methodSymTab);
             for (Enumeration enumer = e.getActual().getElements(); enumer.hasMoreElements();) {
                 traverseExpression(currentClass, ((Expression)enumer.nextElement()), objectSymTab, methodSymTab);
             }
             // TODO: Fill in the type of dispatch here ?
         } else if (expression instanceof assign) {
             if (objectSymTab.lookup(((assign)expression).getName()) == null) {
                 classTable.semantError(currentClass.getFilename(),expression).println("Undeclared identifier " + ((assign)expression).getName());
             }
             Expression e = ((assign)expression).getExpression();
             traverseExpression( currentClass, e, objectSymTab, methodSymTab);
             expression.set_type(e.get_type());
         }
 
 	//dispatch
 	  else if (expression instanceof dispatch) {
             dispatch e = (dispatch)expression;
             traverseExpression(currentClass, e.getExpression(), objectSymTab, methodSymTab);
             for (Enumeration en = e.getActual().getElements(); en.hasMoreElements();) {
                 traverseExpression(currentClass, ((Expression)en.nextElement()), objectSymTab, methodSymTab);
             }
 
         } 
 	// if then else statment
 	else if (expression instanceof cond) {
             Expression predicate = ((cond)expression).getPredicate();
     	    Expression then_exp = ((cond)expression).getThen();
             Expression else_exp = ((cond)expression).getElse();
       
             //traverseExpression(currentClass, ((cond)expression).getPredicate(), objectSymTab, methodSymTab);
             traverseExpression(currentClass, predicate, objectSymTab, methodSymTab);
             traverseExpression(currentClass, then_exp, objectSymTab, methodSymTab);
             traverseExpression(currentClass, else_exp, objectSymTab, methodSymTab);
 
     	    //set final type of cond expression to the lowest common ancestor of then and else expressions
     	    ArrayList<AbstractSymbol> inputTypes = new ArrayList<AbstractSymbol>();
     	    inputTypes.add(then_exp.get_type());
             inputTypes.add(else_exp.get_type());
      	    AbstractSymbol finalType = LCA(inputTypes);
 
             if (finalType!= null)expression.set_type(finalType);
 
         } else if (expression instanceof typcase) {
 	       typcase caseExpression = (typcase)expression;
            traverseExpression(currentClass, caseExpression.getExpression(), objectSymTab, methodSymTab);
            
           
             ArrayList<AbstractSymbol> branchTypes = new ArrayList<AbstractSymbol>();
            Cases caseList = caseExpression.getCases();
 
            //add all types returned from branch expressions to arraylist
            for (Enumeration e = caseList.getElements(); e.hasMoreElements();){
                 objectSymTab.enterScope();
                 branch c = (branch)e.nextElement();
                 //recurse on branch expression, add type to list of expressions
                 Expression branchExp = c.getExpression();
                 objectSymTab.addId(c.getName(), c.getType());
                 traverseExpression(currentClass, branchExp, objectSymTab, methodSymTab);
                 AbstractSymbol branchType = branchExp.get_type();
                 branchTypes.add(branchType);
                  objectSymTab.exitScope();
            }
 
            //return the lca of all branch types
            AbstractSymbol finalType = LCA(branchTypes);
            if (finalType != null) expression.set_type(finalType);
           
         }
     }
 
  // find the lowest common ancestor in inheritance tree 
     private AbstractSymbol LCA (ArrayList<AbstractSymbol> inputTypes) {
 
         int smallestDepth = Integer.MAX_VALUE;
         HashMap<AbstractSymbol, Integer> nodeToDepths = new HashMap<AbstractSymbol, Integer>();
 	    for (AbstractSymbol type : inputTypes) {
             if (Flags.semant_debug){
             System.out.println("NODE TYPE IS :" + type);
           
              }
             if (type == null) return null;
             int depth = classTable.getDepthFromNode(type.toString());
             if (depth < smallestDepth) smallestDepth = depth;
             nodeToDepths.put(type, depth);
         }
 
         ArrayList<String> ancestors = new ArrayList<String>();
         //get parent to same depth
         for (AbstractSymbol node : nodeToDepths.keySet()) {
             int depth = nodeToDepths.get(node);
             int difference = depth - smallestDepth;
             String nodeName = node.toString();
             for (int i = 0; i < difference; i++){
                 nodeName = classTable.getParent(nodeName);
             }
             ancestors.add(nodeName);
         }
 
         boolean LCA_notfound = true;
         //getParent until all nodes are the same
         while (LCA_notfound){
             boolean LCAfound = true;
             //compare all nodes to first node, doesn't matter because we check 
             //that all nodes must be the same. 
             String firstNode = ancestors.get(0);
             for (String nodeName : ancestors){
                 if (!firstNode.equals(nodeName)) {
                     LCAfound = false;
                     break;
                 }
             }
             if (LCAfound == true){
 
                 LCA_notfound = false;
 
             } else{
                 //getparent to every node
 
                 //create temporary arraylist and copy over elements
                 ArrayList<String> temp = new ArrayList<String>();
                 for (String ancestor: ancestors){
                     temp.add(ancestor);
                 }
                 ancestors.clear();
                 for (String ancestor: temp){
                     String parent = classTable.getParent(ancestor);
                     ancestors.add(parent);
                 }
             }
             
         }
         //return type of LCA
          if (Flags.semant_debug){
             System.out.println("Input Nodes ARE :" + inputTypes);
             System.out.println("ORIGINAL NODES DEPTHS ARE:" + nodeToDepths);
             System.out.println("FINAL TYPE IS :" + (classTable.getClass(ancestors.get(0))).getName());
          }
         return (classTable.getClass(ancestors.get(0))).getName();
     }
 
     /** Second + Third pass through AST to perform inheritance   **/
     private void traverseASTWithTypecheck(Classes classes) {
         // Start type checking from root
         setupInheritedClass(TreeConstants.Object_.toString());
         // Traverse AST and do type checking now
         Boolean isMainCLass = false;
         Boolean isMainMethod = false;
         Boolean mainClassSeen = false;
         for (Enumeration e = classes.getElements(); e.hasMoreElements(); ) {
             class_c currentClass = ((class_c)e.nextElement());
             String className = currentClass.getName().toString();
             if (Flags.semant_debug) {
                System.out.println("Type checking Class " + className);
            }
 
            if (className.equals(TreeConstants.Main.toString())) {
             isMainCLass = true;
             mainClassSeen = true;
            }
 
            MySymbolTable objectSymTab = objectSymTabMap.get(className);
            MySymbolTable methodSymTab = methodSymTabMap.get(className);
            Features features = currentClass.getFeatures();
            for (Enumeration fe = features.getElements(); fe.hasMoreElements();) { 
                 Feature f = ((Feature)fe.nextElement());
                 if ( f instanceof attr ) {
                     typeCheckAttribute(currentClass, (attr) f, objectSymTab, methodSymTab);
                 } else {
                     typeCheckMethod(currentClass, (method)f, objectSymTab, methodSymTab);
                     if (Flags.semant_debug) {
                         System.out.println(objectSymTab);
                     }
 
                     if (((method)f).getName() == TreeConstants.main_meth) {
                         isMainMethod =true;
                         if (Flags.semant_debug) {
                         System.out.println("Main method seen");
                     }
                     } 
                 }
             }
 
             if (isMainCLass) {
                 if (!isMainMethod) {
                     classTable.semantError(currentClass.getFilename(), currentClass).println("No \'main\' method in class Main");
                 }
                 isMainCLass = false;
             }
         }
 
         if (!mainClassSeen) {
             classTable.semantError().println("Main class missing");
         }
     }
 
     // Traverse the class graph and inherit methods/attributes from parent to child
     private void setupInheritedClass(String currentClassName) {
 
         if (Flags.semant_debug) {
             System.out.println("Setting up inheritance for class:" + currentClassName);
         }
         HashMap<String, class_c> classMap = classTable.nameToClass;
         class_c currentClass = classMap.get(currentClassName);
         String parent = classTable.getParent(currentClassName);
         MySymbolTable parentsSymTab = objectSymTabMap.get(parent);
         HashMap<String, ArrayList<AbstractSymbol>> parentsMethodSymTab = methodEnvironment.get(parent);
         MySymbolTable myObjSymTab = objectSymTabMap.get(currentClassName);
         HashMap<String, ArrayList<AbstractSymbol>> myMethSymTab = methodEnvironment.get(currentClassName);
         // Check for properly overrideen attributes/methods
 
         if (Flags.semant_debug) {
             System.out.println("Parent : " + parent);
             System.out.println("Parent's object Symbol Table : " + parentsSymTab);
             System.out.println("Parent's method Symbol Table : " + parentsMethodSymTab);
         }
 
         if (parentsSymTab != null) {
             Features features = currentClass.getFeatures();
             for (Enumeration fe = features.getElements(); fe.hasMoreElements();) {
                 Feature f = ((Feature)fe.nextElement());
                 // An attribute cannot be overridden
                 if (f instanceof attr) {
                     if (parentsSymTab.lookup(((attr)f).getName()) != null) {
                         classTable.semantError(currentClass.getFilename(), f).println("Attribute " + ((attr)f).getName().toString() + " is an attribute of an inherited class");
                         // And remove the redefined attribute from this guy's symbol table
                         myObjSymTab.remove(((attr)f).getName());
                     }
                 } else {
                     // A method can, provided the signature is the same
                     method m = (method)f;
                     String methodname = m.getName().toString();
                     if (parentsMethodSymTab != null && parentsMethodSymTab.containsKey(methodname)) {
                         ArrayList<AbstractSymbol> subclassSignature = myMethSymTab.get(methodname);
                         ArrayList<AbstractSymbol> inheritclassSignature = parentsMethodSymTab.get(methodname);
                         // Check if the two lists are exactly same
                         if (subclassSignature.size() != inheritclassSignature.size()) {
                             classTable.semantError(currentClass.getFilename(), m).println("Incompatible number of formal parameters in redefined method" + methodname );
                         } else {
                             for ( int j = 0; j < subclassSignature.size(); j++) {
                                 AbstractSymbol subclassFormal = subclassSignature.get(j);
                                 AbstractSymbol inheritclassFormal = inheritclassSignature.get(j);
                                 if (subclassFormal != inheritclassFormal) {
                                     classTable.semantError(currentClass.getFilename(), m).println("In redefined method " + methodname + " type of formal parameter " + subclassFormal + " is different from original return type " + inheritclassFormal);
                                     myMethSymTab.remove(methodname);
                                     break;
                                 }
                             }
                         }
                     }
                 }
             }
             /* For each id added to the outer scope hastable of the parent's Mysymboltable,
                inherit the id if not already present, if present skip it, as this has been dealt with just above  */
             parentsSymTab.copy(myObjSymTab);
             for (String methodName : parentsMethodSymTab.keySet()) {
                 if (!myMethSymTab.containsKey(methodName)) {
                     myMethSymTab.put(methodName, parentsMethodSymTab.get(methodName));
                 }
             }
 
             if (Flags.semant_debug) {
                 System.out.println("Child : " + currentClassName);
                 System.out.println("Child's object Symbol Table : " + myObjSymTab);
                 System.out.println("Child's method Symbol Table : " + myMethSymTab);
             }
         }
         /* Recurse on the children of this class */
         if (classTable.getChildren(currentClassName) != null) {
             for (String child : classTable.getChildren(currentClassName)) {
                 setupInheritedClass(child);
             }
         }
     }
 
 
     private void typeCheckAttribute(class_c currentClass, attr a, MySymbolTable objectSymTab, MySymbolTable methodSymTab) {
         if (Flags.semant_debug) {
             System.out.println("Type checking attribute: " + a.getName());
         }
         if (!(a.getExpression() instanceof no_expr)) {
             typeCheckExpression(currentClass, a.getExpression(), objectSymTab, methodSymTab);
             AbstractSymbol T1 = a.getExpression().get_type();
             if (T1 == TreeConstants.SELF_TYPE) {
                 T1 = currentClass.getName();
             }
             AbstractSymbol T0 = a.getType();
             if (T0 == TreeConstants.SELF_TYPE) {
                 T0 = currentClass.getName();
             }
             if (!classTable.checkConformance(T1, T0)) {
                 classTable.semantError(currentClass.getFilename(), a).println("Inferred type " + T1 + " of initialization of attribute " + a.getName() + " does not conform to declared type " + T0);
             }
         }
     }
 
     /** Type check a method and all the expressions in it **/
     private void typeCheckMethod(class_c currentClass, method m, MySymbolTable objectSymTab, MySymbolTable methodSymTab) {
         String className = currentClass.getName().toString();
         // Start a new scope
         objectSymTab.enterScope();
         // Traverse formal arguments, adding them to scope
         Formals formals = m.getFormals();
         String methodname = m.getName().toString();
 
         if (Flags.semant_debug) {
             System.out.println("Type checking method: " + methodname);
             m.dump_with_types(System.out,1);
         }
 
         for (Enumeration e = formals.getElements(); e.hasMoreElements();) {
             formalc formal = ((formalc)e.nextElement());
             // Check if the type of this formal is valid
             if (formal.getType() == TreeConstants.SELF_TYPE) {
                 classTable.semantError(currentClass.getFilename(), formal).println("formal parameter " + formal.getName() + " cannot have type SELF_TYPE");
             } else if ( !classTable.isValidType(formal.getType())) {
                 classTable.semantError(currentClass.getFilename(), formal).println("Class " + formal.getType() + " of formal parameter " + formal.getName() + " is undefined");
             } else {
                 // Accept this formal as a valid formal only if it is valid and not a selftype
                 objectSymTab.addId(formal.getName(), formal.getType());
             }
         }
         // Check return type
         typeCheckExpression(currentClass, m.getExpression(), objectSymTab, methodSymTab);
         if (m.getExpression().get_type() == null) {
             if (Flags.semant_debug) {
                 System.out.println("ERROR: Expression has no type set!");
                 m.getExpression().dump_with_types(System.out, 1);
             }
         }
         AbstractSymbol observedReturnType = m.getExpression().get_type();
         ArrayList<AbstractSymbol> declaredFormalTypes = methodEnvironment.get(className).get(methodname);
         AbstractSymbol declaredReturnType = declaredFormalTypes.get(declaredFormalTypes.size() - 1);
         if (Flags.semant_debug) {
             System.out.println("Inferred Return type: " + observedReturnType + ". Declared :" + declaredReturnType);
         }
         if (declaredReturnType == TreeConstants.SELF_TYPE) {
             declaredReturnType = currentClass.getName();
         }
         if (observedReturnType == TreeConstants.SELF_TYPE) {
             observedReturnType = currentClass.getName();
         }
         if (!classTable.checkConformance(observedReturnType, declaredReturnType)) {
             classTable.semantError(currentClass.getFilename(), m).println("Inferred return type " + observedReturnType.toString() + " of method " + methodname + " does not conform to declared return type " + declaredReturnType.toString());
         }
         objectSymTab.exitScope();
     }
 
     private void typeCheckExpression(class_c currentClass, Expression expression, MySymbolTable objectSymTab, MySymbolTable methodSymTab) {
         String className = currentClass.getName().getString();
         if (expression instanceof typcase){
 
           
             //check first expression
            typcase caseExpression = (typcase)expression;
            typeCheckExpression(currentClass, caseExpression.getExpression(), objectSymTab, methodSymTab);
            
             //loop over branches and typecheck each branch
             ArrayList<AbstractSymbol> branchTypes = new ArrayList<AbstractSymbol>();
            Cases caseList = caseExpression.getCases();
 
            //add all types returned from branch expressions to arraylist
            for (Enumeration e = caseList.getElements(); e.hasMoreElements();){
                 objectSymTab.enterScope();
                 branch c = (branch)e.nextElement();
                 //recurse on branch expression, add type to list of expressions
                 Expression branchExp = c.getExpression();
                  objectSymTab.addId(c.getName(), c.getType());
                 typeCheckExpression(currentClass, branchExp, objectSymTab, methodSymTab);
                 AbstractSymbol branchType = branchExp.get_type();
                 branchTypes.add(branchType);
                 objectSymTab.exitScope();
            }
 
            //return the lca of all branch types
            AbstractSymbol finalType = LCA(branchTypes);
            expression.set_type(finalType);
         } 
        
         else if (expression instanceof object) {
             // This can change of attributes got pruned out because of inheritance checks
             if ( ((object)expression).getName() == TreeConstants.self ) {
                 expression.set_type(TreeConstants.SELF_TYPE);
             } else {
                 expression.set_type((AbstractSymbol)objectSymTab.lookup(((object)expression).getName()));
             }
         } else if (expression instanceof assign) {
             assign e = (assign)expression;
             typeCheckExpression(currentClass, e.getExpression(), objectSymTab, methodSymTab);
             e.set_type(e.getExpression().get_type());
             AbstractSymbol inferredType = e.getExpression().get_type();
             AbstractSymbol declaredType = (AbstractSymbol) objectSymTab.lookup(e.getName());
 
             if (Flags.semant_debug) {
                 System.out.println("Type checking assignment : ");
                 //e.dump_with_types(System.out, 1);
             }
 
             if (!classTable.checkConformance(inferredType, declaredType)) {
                 classTable.semantError(currentClass.getFilename(), e).println("Type " + inferredType + " of assigned expression does not conform to declared type " + declaredType + " of identifier " + e.getName());
             }
         } else if (expression instanceof cond) {
             cond e = (cond)expression;
             // Type check the expression for the predicate
             typeCheckExpression(currentClass, e.getPredicate(), objectSymTab, methodSymTab);
             typeCheckExpression(currentClass, e.getThen(), objectSymTab, methodSymTab);
             typeCheckExpression(currentClass, e.getElse(), objectSymTab, methodSymTab);
             // Set the type of the cond here
             AbstractSymbol typeOfPredicate = e.getPredicate().get_type();
             if (typeOfPredicate != TreeConstants.Bool) {
                 classTable.semantError(currentClass.getFilename(), e).println("Predicate of \"if\" does not have type Bool");
             }
 
              //set final type of cond expression to the lowest common ancestor of then and else expressions
             ArrayList<AbstractSymbol> inputTypes = new ArrayList<AbstractSymbol>();
             inputTypes.add(e.getThen().get_type());
             inputTypes.add(e.getElse().get_type());
             AbstractSymbol finalType = LCA(inputTypes);
 
             e.set_type(finalType);
 
         } else if (expression instanceof let) {
             let e = (let)expression;
             typeCheckExpression(currentClass, e.getInit(), objectSymTab, methodSymTab);
             AbstractSymbol T0Prime = e.getType();
             if (T0Prime == TreeConstants.SELF_TYPE) {
                 T0Prime = currentClass.getName();
             }
             if (!(e.getInit() instanceof no_expr)) {
                 AbstractSymbol T1 = e.getInit().get_type();
                 if (!classTable.checkConformance(T1, T0Prime)) {
                     classTable.semantError(currentClass.getFilename(), e).println("Inferred type " + T1 + " of initialization of " + e.getIdentifier() + " does not conform to identifier's declared type " + T0Prime);
                 }
             }
             objectSymTab.enterScope();
             objectSymTab.addId(e.getIdentifier(), e.getType());
             typeCheckExpression(currentClass, e.getBody(), objectSymTab, methodSymTab);
             e.set_type(e.getBody().get_type());
             objectSymTab.exitScope();
         } else if (expression instanceof block) {
             Expressions body = ((block)expression).getBody();
             AbstractSymbol lastType = null;
             for (Enumeration e = body.getElements(); e.hasMoreElements();) {
                 Expression nextExpression = (Expression)e.nextElement();
                 typeCheckExpression(currentClass, nextExpression, objectSymTab, methodSymTab);
                 lastType = nextExpression.get_type();
             }
             expression.set_type(lastType);
         } else if (expression instanceof dispatch) {
             dispatch e = (dispatch)expression;
             String methodname = e.getName().toString();
             typeCheckExpression(currentClass, e.getExpression(), objectSymTab, methodSymTab);
             AbstractSymbol T0 = e.getExpression().get_type();
 
             if (Flags.semant_debug) {
                 System.out.println("Type checking dispatch: " + methodname + " of class " + T0);
                 e.dump_with_types(System.out,1);
             }
             AbstractSymbol T0Prime = T0;
             if (T0Prime == TreeConstants.SELF_TYPE) {
                 T0Prime = currentClass.getName();
             }
             // Check if method actually exists in class T0Prime
             if (!methodEnvironment.get(T0Prime.toString()).containsKey(methodname)) {
                 classTable.semantError(currentClass.getFilename(), e).println("Dispatch to undefined method " + methodname);
                 e.set_type(TreeConstants.Object_);
             } else {
                 // Check if actual and formal conform
                 ArrayList<AbstractSymbol> inferredTypes = new ArrayList<AbstractSymbol>();
                 for (Enumeration en = e.getActual().getElements(); en.hasMoreElements();) {
                     Expression next = (Expression)en.nextElement();
                     typeCheckExpression(currentClass, next , objectSymTab, methodSymTab);
                     inferredTypes.add(next.get_type());
                 }
                 // Check if the number of arguments are the same
                 ArrayList<AbstractSymbol> declaredTypes = methodEnvironment.get(T0Prime.toString()).get(methodname);
                 if (declaredTypes.size() != inferredTypes.size()+1) {
                     classTable.semantError(currentClass.getFilename(), e).println("Method " + methodname + " called with wrong number of arguments");
                 } else {
                     // If the number are the same, check each one
                     for (int i = 0; i < declaredTypes.size() - 1; i++) {
                         AbstractSymbol inferred = inferredTypes.get(i);
                         AbstractSymbol declared = declaredTypes.get(i);
                         if (!classTable.checkConformance(inferred, declared)) {
                             classTable.semantError(currentClass.getFilename(), e).println("In call of method " + methodname + " type " + inferred + " of parameter number " + i + " does not conform to declared type " + declared);
                         }
                     }
                 }
                 // Check, well actually set, return type, regardless of whether there are errors in the actuals/formals
                 AbstractSymbol declaredReturnType = declaredTypes.get(declaredTypes.size() - 1);
                 if (declaredReturnType == TreeConstants.SELF_TYPE) {
                     declaredReturnType = T0;
                 }
                 e.set_type(declaredReturnType);
             }
         } else if (expression instanceof static_dispatch) {
             static_dispatch e = (static_dispatch)expression;
             String methodname = e.getName().toString();
             typeCheckExpression(currentClass, e.getExpression(), objectSymTab, methodSymTab);
             AbstractSymbol T0 = e.getExpression().get_type();
             AbstractSymbol T = e.getTypeName();
 
             if (Flags.semant_debug) {
                 System.out.println("Type checking static dispatch: " + methodname + " of class " + T0);
                 e.dump_with_types(System.out,1);
             }
 
             // Check of T0 conforms to T
             if (!classTable.checkConformance(T0, T)) {
                 classTable.semantError(currentClass.getFilename(), e).println("Expression type " + T0 + " does not conform to declared static dispatch type " + T);
                 e.set_type(TreeConstants.Object_);
             }
             // Check if method actually exists in class T
              else if (!methodEnvironment.get(T.toString()).containsKey(methodname)) {
                 classTable.semantError(currentClass.getFilename(), e).println("Static Dispatch to undefined method " + methodname);
                 e.set_type(TreeConstants.Object_);
             } else {
                 // Check if actual and formal conform
                 ArrayList<AbstractSymbol> inferredTypes = new ArrayList<AbstractSymbol>();
                 for (Enumeration en = e.getActual().getElements(); en.hasMoreElements();) {
                     Expression next = (Expression)en.nextElement();
                     typeCheckExpression(currentClass, next , objectSymTab, methodSymTab);
                     inferredTypes.add(next.get_type());
                 }
                 // Check if the number of arguments are the same
                 ArrayList<AbstractSymbol> declaredTypes = methodEnvironment.get(T.toString()).get(methodname);
                 if (declaredTypes.size() != inferredTypes.size()+1) {
                     classTable.semantError(currentClass.getFilename(), e).println("Method " + methodname + " called with wrong number of arguments");
                 } else {
                     // If the number are the same, check each one
                     for (int i = 0; i < declaredTypes.size() - 1; i++) {
                         AbstractSymbol inferred = inferredTypes.get(i);
                         AbstractSymbol declared = declaredTypes.get(i);
                         if (!classTable.checkConformance(inferred, declared)) {
                             classTable.semantError(currentClass.getFilename(), e).println("In call of method " + methodname + " type " + inferred + " of parameter number " + i + " does not conform to declared type " + declared);
                         }
                     }
                 }
                 // Check, well actually set, return type, regardless of whether there are errors in the actuals/formals
                 AbstractSymbol declaredReturnType = declaredTypes.get(declaredTypes.size() - 1);
                 if (declaredReturnType == TreeConstants.SELF_TYPE) {
                     declaredReturnType = T0;
                 }
                 e.set_type(declaredReturnType);
             }
         } else if (expression instanceof loop) {
             loop e = (loop)expression;
             typeCheckExpression(currentClass, e.getPredicate(), objectSymTab, methodSymTab);
             typeCheckExpression(currentClass, e.getBody(), objectSymTab, methodSymTab);
         } else if (expression instanceof isvoid) {
             isvoid e = (isvoid)expression;
             typeCheckExpression(currentClass, e.getExpression(), objectSymTab, methodSymTab);
         } else if (expression instanceof comp) {
             comp e = (comp)expression;
             typeCheckExpression(currentClass, e.getExpression(), objectSymTab, methodSymTab);
             if (e.getExpression().get_type() != TreeConstants.Bool) {
                 classTable.semantError(currentClass.getFilename(), e).println("Argument of not has type " + e.getExpression().get_type() );
             }
         } else if (expression instanceof lt) {
             lt e = (lt)expression;
             typeCheckExpression(currentClass, e.getLHS(), objectSymTab, methodSymTab);
             typeCheckExpression(currentClass, e.getRHS(), objectSymTab, methodSymTab);
 
             if (e.getLHS().get_type() != TreeConstants.Int ||  e.getRHS().get_type() != TreeConstants.Int ) {
                 classTable.semantError(currentClass.getFilename(), e).println("Non-Int arguments: " +  e.getLHS().get_type() + " < " + e.getRHS().get_type());
             }
         } else if (expression instanceof leq) {
             leq e = (leq)expression;
             typeCheckExpression(currentClass, e.getLHS(), objectSymTab, methodSymTab);
             typeCheckExpression(currentClass, e.getRHS(), objectSymTab, methodSymTab);
 
             if (e.getLHS().get_type() != TreeConstants.Int ||  e.getRHS().get_type() != TreeConstants.Int ) {
                 classTable.semantError(currentClass.getFilename(), e).println("Non-Int arguments: " +  e.getLHS().get_type() + " <= " + e.getRHS().get_type());
             }
         } else if (expression instanceof neg) {
             neg e = (neg)expression;
             typeCheckExpression(currentClass, e.getExpression(), objectSymTab, methodSymTab);
             if (e.getExpression().get_type() != TreeConstants.Int) {
                 classTable.semantError(currentClass.getFilename(), e).println("Argument of ~ has type " + e.getExpression().get_type() + " instead of Int" );
             }
         } else if (expression instanceof plus) {
             plus e = (plus) expression;
             typeCheckExpression(currentClass, e.getLHS(), objectSymTab, methodSymTab);
             typeCheckExpression(currentClass, e.getRHS(), objectSymTab, methodSymTab);
 
             AbstractSymbol LHSType = e.getLHS().get_type();
             AbstractSymbol RHSType = e.getRHS().get_type();
 
             if ( LHSType != TreeConstants.Int || RHSType != TreeConstants.Int) {
                 classTable.semantError(currentClass.getFilename(), e).println("Non-Int arguments " + LHSType + "  + " + RHSType);
             }
         } else if (expression instanceof sub) {
             sub e = (sub) expression;
             typeCheckExpression(currentClass, e.getLHS(), objectSymTab, methodSymTab);
             typeCheckExpression(currentClass, e.getRHS(), objectSymTab, methodSymTab);
 
             AbstractSymbol LHSType = e.getLHS().get_type();
             AbstractSymbol RHSType = e.getRHS().get_type();
 
             if ( LHSType != TreeConstants.Int || RHSType != TreeConstants.Int) {
                 classTable.semantError(currentClass.getFilename(), e).println("Non-Int arguments " + LHSType + "  - " + RHSType);
             }
         } else if (expression instanceof mul) {
             mul e = (mul) expression;
             typeCheckExpression(currentClass, e.getLHS(), objectSymTab, methodSymTab);
             typeCheckExpression(currentClass, e.getRHS(), objectSymTab, methodSymTab);
 
             AbstractSymbol LHSType = e.getLHS().get_type();
             AbstractSymbol RHSType = e.getRHS().get_type();
 
             if ( LHSType != TreeConstants.Int || RHSType != TreeConstants.Int) {
                 classTable.semantError(currentClass.getFilename(), e).println("Non-Int arguments " + LHSType + "  * " + RHSType);
             }
         } else if (expression instanceof divide) {
             divide e = (divide) expression;
             typeCheckExpression(currentClass, e.getLHS(), objectSymTab, methodSymTab);
             typeCheckExpression(currentClass, e.getRHS(), objectSymTab, methodSymTab);
 
             AbstractSymbol LHSType = e.getLHS().get_type();
             AbstractSymbol RHSType = e.getRHS().get_type();
 
             if ( LHSType != TreeConstants.Int || RHSType != TreeConstants.Int) {
                 classTable.semantError(currentClass.getFilename(), e).println("Non-Int arguments " + LHSType + "  / " + RHSType);
             }
         } else if (expression instanceof eq) {
             eq e = (eq) expression;
             typeCheckExpression(currentClass, e.getLHS(), objectSymTab, methodSymTab);
             typeCheckExpression(currentClass, e.getRHS(), objectSymTab, methodSymTab);
 
             AbstractSymbol LHSType = e.getLHS().get_type();
             AbstractSymbol RHSType = e.getRHS().get_type();
 
             if ( (LHSType == TreeConstants.Int && RHSType == TreeConstants.Int) ||
                 (LHSType == TreeConstants.Bool && RHSType == TreeConstants.Bool) ||
                 (LHSType == TreeConstants.Str && RHSType == TreeConstants.Str)  )
              {
              } else {
                 classTable.semantError(currentClass.getFilename(), e).println("Illegal comparison with a basic type");
             }
         }
 
     }
 }
 
 
 /** Defines AST constructor 'class_c'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class class_c extends Class_ {
     protected AbstractSymbol name;
     protected AbstractSymbol parent;
     protected Features features;
     protected AbstractSymbol filename;
     /** Creates "class_c" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for name
       * @param a1 initial value for parent
       * @param a2 initial value for features
       * @param a3 initial value for filename
       */
     public class_c(int lineNumber, AbstractSymbol a1, AbstractSymbol a2, Features a3, AbstractSymbol a4) {
         super(lineNumber);
         name = a1;
         parent = a2;
         features = a3;
         filename = a4;
     }
     public TreeNode copy() {
         return new class_c(lineNumber, copy_AbstractSymbol(name), copy_AbstractSymbol(parent), (Features)features.copy(), copy_AbstractSymbol(filename));
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "class_c\n");
         dump_AbstractSymbol(out, n+2, name);
         dump_AbstractSymbol(out, n+2, parent);
         features.dump(out, n+2);
         dump_AbstractSymbol(out, n+2, filename);
     }
 
     
     public AbstractSymbol getFilename() { return filename; }
     public AbstractSymbol getName()     { return name; }
     public AbstractSymbol getParent()   { return parent; }
     public Features getFeatures() { return features; }
 
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_class");
         dump_AbstractSymbol(out, n + 2, name);
         dump_AbstractSymbol(out, n + 2, parent);
         out.print(Utilities.pad(n + 2) + "\"");
         Utilities.printEscapedString(out, filename.getString());
         out.println("\"\n" + Utilities.pad(n + 2) + "(");
         for (Enumeration e = features.getElements(); e.hasMoreElements();) {
 	    ((Feature)e.nextElement()).dump_with_types(out, n + 2);
         }
         out.println(Utilities.pad(n + 2) + ")");
     }
 
 }
 
 
 /** Defines AST constructor 'method'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class method extends Feature {
     protected AbstractSymbol name;
     protected Formals formals;
     protected AbstractSymbol return_type;
     protected Expression expr;
     /** Creates "method" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for name
       * @param a1 initial value for formals
       * @param a2 initial value for return_type
       * @param a3 initial value for expr
       */
     public method(int lineNumber, AbstractSymbol a1, Formals a2, AbstractSymbol a3, Expression a4) {
         super(lineNumber);
         name = a1;
         formals = a2;
         return_type = a3;
         expr = a4;
     }
     public TreeNode copy() {
         return new method(lineNumber, copy_AbstractSymbol(name), (Formals)formals.copy(), copy_AbstractSymbol(return_type), (Expression)expr.copy());
     }
 
     public AbstractSymbol getName() { return name;	}
     public Formals getFormals()	{ return formals;	}
     public Expression getExpression()	{ return expr;	}
     public AbstractSymbol getReturnType()	{  return return_type;	}
 
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "method\n");
         dump_AbstractSymbol(out, n+2, name);
         formals.dump(out, n+2);
         dump_AbstractSymbol(out, n+2, return_type);
         expr.dump(out, n+2);
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_method");
         dump_AbstractSymbol(out, n + 2, name);
         for (Enumeration e = formals.getElements(); e.hasMoreElements();) {
 	    ((Formal)e.nextElement()).dump_with_types(out, n + 2);
         }
         dump_AbstractSymbol(out, n + 2, return_type);
 	expr.dump_with_types(out, n + 2);
     }
 
 }
 
 
 /** Defines AST constructor 'attr'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class attr extends Feature {
     protected AbstractSymbol name;
     protected AbstractSymbol type_decl;
     protected Expression init;
     /** Creates "attr" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for name
       * @param a1 initial value for type_decl
       * @param a2 initial value for init
       */
     public attr(int lineNumber, AbstractSymbol a1, AbstractSymbol a2, Expression a3) {
         super(lineNumber);
         name = a1;
         type_decl = a2;
         init = a3;
     }
     public TreeNode copy() {
         return new attr(lineNumber, copy_AbstractSymbol(name), copy_AbstractSymbol(type_decl), (Expression)init.copy());
     }
 
     //helper functions added in
     public AbstractSymbol getName() { return name;	}
     public Expression getExpression()	{ return init;	}
     public AbstractSymbol getType()	{  return type_decl;	}
 
 
 
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "attr\n");
         dump_AbstractSymbol(out, n+2, name);
         dump_AbstractSymbol(out, n+2, type_decl);
         init.dump(out, n+2);
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_attr");
         dump_AbstractSymbol(out, n + 2, name);
         dump_AbstractSymbol(out, n + 2, type_decl);
 	init.dump_with_types(out, n + 2);
     }
 
 }
 
 
 /** Defines AST constructor 'formalc'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class formalc extends Formal {
     protected AbstractSymbol name;
     protected AbstractSymbol type_decl;
     /** Creates "formalc" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for name
       * @param a1 initial value for type_decl
       */
     public formalc(int lineNumber, AbstractSymbol a1, AbstractSymbol a2) {
         super(lineNumber);
         name = a1;
         type_decl = a2;
     }
     public TreeNode copy() {
         return new formalc(lineNumber, copy_AbstractSymbol(name), copy_AbstractSymbol(type_decl));
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "formalc\n");
         dump_AbstractSymbol(out, n+2, name);
         dump_AbstractSymbol(out, n+2, type_decl);
     }
 
     public AbstractSymbol getName()	{ return name;	}
     public AbstractSymbol getType()	{ return type_decl;	}
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_formal");
         dump_AbstractSymbol(out, n + 2, name);
         dump_AbstractSymbol(out, n + 2, type_decl);
     }
 
 }
 
 
 /** Defines AST constructor 'branch'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class branch extends Case {
     protected AbstractSymbol name;
     protected AbstractSymbol type_decl;
     protected Expression expr;
     /** Creates "branch" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for name
       * @param a1 initial value for type_decl
       * @param a2 initial value for expr
       */
     public branch(int lineNumber, AbstractSymbol a1, AbstractSymbol a2, Expression a3) {
         super(lineNumber);
         name = a1;
         type_decl = a2;
         expr = a3;
     }
     public TreeNode copy() {
         return new branch(lineNumber, copy_AbstractSymbol(name), copy_AbstractSymbol(type_decl), (Expression)expr.copy());
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "branch\n");
         dump_AbstractSymbol(out, n+2, name);
         dump_AbstractSymbol(out, n+2, type_decl);
         expr.dump(out, n+2);
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_branch");
         dump_AbstractSymbol(out, n + 2, name);
         dump_AbstractSymbol(out, n + 2, type_decl);
 	expr.dump_with_types(out, n + 2);
     }
 
      public AbstractSymbol getName() {return name;}
     public AbstractSymbol getType() {return type_decl;}
     public Expression getExpression() {return expr;}
 
 }
 
 
 /** Defines AST constructor 'assign'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class assign extends Expression {
     protected AbstractSymbol name;
     protected Expression expr;
     /** Creates "assign" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for name
       * @param a1 initial value for expr
       */
     public assign(int lineNumber, AbstractSymbol a1, Expression a2) {
         super(lineNumber);
         name = a1;
         expr = a2;
     }
     public TreeNode copy() {
         return new assign(lineNumber, copy_AbstractSymbol(name), (Expression)expr.copy());
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "assign\n");
         dump_AbstractSymbol(out, n+2, name);
         expr.dump(out, n+2);
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_assign");
         dump_AbstractSymbol(out, n + 2, name);
 	expr.dump_with_types(out, n + 2);
 	dump_type(out, n);
     }
 
     public AbstractSymbol getName() { return name;  }
     public Expression getExpression()   { return expr;  }
 }
 
 
 /** Defines AST constructor 'static_dispatch'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class static_dispatch extends Expression {
     protected Expression expr;
     protected AbstractSymbol type_name;
     protected AbstractSymbol name;
     protected Expressions actual;
     /** Creates "static_dispatch" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for expr
       * @param a1 initial value for type_name
       * @param a2 initial value for name
       * @param a3 initial value for actual
       */
     public static_dispatch(int lineNumber, Expression a1, AbstractSymbol a2, AbstractSymbol a3, Expressions a4) {
         super(lineNumber);
         expr = a1;
         type_name = a2;
         name = a3;
         actual = a4;
     }
     public TreeNode copy() {
         return new static_dispatch(lineNumber, (Expression)expr.copy(), copy_AbstractSymbol(type_name), copy_AbstractSymbol(name), (Expressions)actual.copy());
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "static_dispatch\n");
         expr.dump(out, n+2);
         dump_AbstractSymbol(out, n+2, type_name);
         dump_AbstractSymbol(out, n+2, name);
         actual.dump(out, n+2);
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_static_dispatch");
 	expr.dump_with_types(out, n + 2);
         dump_AbstractSymbol(out, n + 2, type_name);
         dump_AbstractSymbol(out, n + 2, name);
         out.println(Utilities.pad(n + 2) + "(");
         for (Enumeration e = actual.getElements(); e.hasMoreElements();) {
 	    ((Expression)e.nextElement()).dump_with_types(out, n + 2);
         }
         out.println(Utilities.pad(n + 2) + ")");
 	dump_type(out, n);
     }
 
     public Expression getExpression()   { return expr;  }
     public AbstractSymbol getTypeName() { return type_name; }
     public AbstractSymbol getName() { return name;  }
     public Expressions getActual()  { return actual;    }
 }
 
 
 /** Defines AST constructor 'dispatch'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class dispatch extends Expression {
     protected Expression expr;
     protected AbstractSymbol name;
     protected Expressions actual;
     /** Creates "dispatch" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for expr
       * @param a1 initial value for name
       * @param a2 initial value for actual
       */
     public dispatch(int lineNumber, Expression a1, AbstractSymbol a2, Expressions a3) {
         super(lineNumber);
         expr = a1;
         name = a2;
         actual = a3;
     }
     public TreeNode copy() {
         return new dispatch(lineNumber, (Expression)expr.copy(), copy_AbstractSymbol(name), (Expressions)actual.copy());
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "dispatch\n");
         expr.dump(out, n+2);
         dump_AbstractSymbol(out, n+2, name);
         actual.dump(out, n+2);
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_dispatch");
 	expr.dump_with_types(out, n + 2);
         dump_AbstractSymbol(out, n + 2, name);
         out.println(Utilities.pad(n + 2) + "(");
         for (Enumeration e = actual.getElements(); e.hasMoreElements();) {
 	    ((Expression)e.nextElement()).dump_with_types(out, n + 2);
         }
         out.println(Utilities.pad(n + 2) + ")");
 	dump_type(out, n);
     }
  
     public Expression getExpression()   { return expr;  }
     public AbstractSymbol getName() { return name;  }
     public Expressions getActual()  { return actual;    }
 }
 
 
 /** Defines AST constructor 'cond'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class cond extends Expression {
     protected Expression pred;
     protected Expression then_exp;
     protected Expression else_exp;
     /** Creates "cond" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for pred
       * @param a1 initial value for then_exp
       * @param a2 initial value for else_exp
       */
     public cond(int lineNumber, Expression a1, Expression a2, Expression a3) {
         super(lineNumber);
         pred = a1;
         then_exp = a2;
         else_exp = a3;
     }
     public TreeNode copy() {
         return new cond(lineNumber, (Expression)pred.copy(), (Expression)then_exp.copy(), (Expression)else_exp.copy());
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "cond\n");
         pred.dump(out, n+2);
         then_exp.dump(out, n+2);
         else_exp.dump(out, n+2);
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_cond");
 	pred.dump_with_types(out, n + 2);
 	then_exp.dump_with_types(out, n + 2);
 	else_exp.dump_with_types(out, n + 2);
 	dump_type(out, n);
     }
 
     public Expression getPredicate()    { return pred;  }
     public Expression getThen() { return then_exp;  }
     public Expression getElse() { return else_exp;  }
 }
 
 
 /** Defines AST constructor 'loop'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class loop extends Expression {
     protected Expression pred;
     protected Expression body;
     /** Creates "loop" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for pred
       * @param a1 initial value for body
       */
     public loop(int lineNumber, Expression a1, Expression a2) {
         super(lineNumber);
         pred = a1;
         body = a2;
     }
     public TreeNode copy() {
         return new loop(lineNumber, (Expression)pred.copy(), (Expression)body.copy());
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "loop\n");
         pred.dump(out, n+2);
         body.dump(out, n+2);
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_loop");
 	pred.dump_with_types(out, n + 2);
 	body.dump_with_types(out, n + 2);
 	dump_type(out, n);
     }
 
     public Expression getPredicate()    { return pred;  }
     public Expression getBody() { return body;  }
 }
 
 
 /** Defines AST constructor 'typcase'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class typcase extends Expression {
     protected Expression expr;
     protected Cases cases;
     /** Creates "typcase" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for expr
       * @param a1 initial value for cases
       */
     public typcase(int lineNumber, Expression a1, Cases a2) {
         super(lineNumber);
         expr = a1;
         cases = a2;
     }
     public TreeNode copy() {
         return new typcase(lineNumber, (Expression)expr.copy(), (Cases)cases.copy());
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "typcase\n");
         expr.dump(out, n+2);
         cases.dump(out, n+2);
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_typcase");
 	expr.dump_with_types(out, n + 2);
         for (Enumeration e = cases.getElements(); e.hasMoreElements();) {
 	    ((Case)e.nextElement()).dump_with_types(out, n + 2);
         }
 	dump_type(out, n);
     }
 
     public Expression getExpression() {return expr; }
     public Cases getCases() {return cases; }
 }
 
 
 /** Defines AST constructor 'block'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class block extends Expression {
     protected Expressions body;
     /** Creates "block" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for body
       */
     public block(int lineNumber, Expressions a1) {
         super(lineNumber);
         body = a1;
     }
     public TreeNode copy() {
         return new block(lineNumber, (Expressions)body.copy());
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "block\n");
         body.dump(out, n+2);
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_block");
         for (Enumeration e = body.getElements(); e.hasMoreElements();) {
 	    ((Expression)e.nextElement()).dump_with_types(out, n + 2);
         }
 	dump_type(out, n);
     }
 
     public Expressions getBody()    { return body;  }
 }
 
 
 /** Defines AST constructor 'let'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class let extends Expression {
     protected AbstractSymbol identifier;
     protected AbstractSymbol type_decl;
     protected Expression init;
     protected Expression body;
     /** Creates "let" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for identifier
       * @param a1 initial value for type_decl
       * @param a2 initial value for init
       * @param a3 initial value for body
       */
     public let(int lineNumber, AbstractSymbol a1, AbstractSymbol a2, Expression a3, Expression a4) {
         super(lineNumber);
         identifier = a1;
         type_decl = a2;
         init = a3;
         body = a4;
     }
     public TreeNode copy() {
         return new let(lineNumber, copy_AbstractSymbol(identifier), copy_AbstractSymbol(type_decl), (Expression)init.copy(), (Expression)body.copy());
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "let\n");
         dump_AbstractSymbol(out, n+2, identifier);
         dump_AbstractSymbol(out, n+2, type_decl);
         init.dump(out, n+2);
         body.dump(out, n+2);
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_let");
 	dump_AbstractSymbol(out, n + 2, identifier);
 	dump_AbstractSymbol(out, n + 2, type_decl);
 	init.dump_with_types(out, n + 2);
 	body.dump_with_types(out, n + 2);
 	dump_type(out, n);
     }
 
     public AbstractSymbol getIdentifier()   { return identifier;    }
     public AbstractSymbol getType() { return type_decl; }
     public Expression getInit() { return init;  }
     public Expression getBody() { return body;  }
 }
 
 
 /** Defines AST constructor 'plus'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class plus extends Expression {
     protected Expression e1;
     protected Expression e2;
     /** Creates "plus" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for e1
       * @param a1 initial value for e2
       */
     public plus(int lineNumber, Expression a1, Expression a2) {
         super(lineNumber);
         e1 = a1;
         e2 = a2;
     }
     public TreeNode copy() {
         return new plus(lineNumber, (Expression)e1.copy(), (Expression)e2.copy());
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "plus\n");
         e1.dump(out, n+2);
         e2.dump(out, n+2);
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_plus");
 	e1.dump_with_types(out, n + 2);
 	e2.dump_with_types(out, n + 2);
 	dump_type(out, n);
     }
 
     public Expression getLHS()  { return e1;    }
     public Expression getRHS()   { return e2;    }
 }
 
 
 /** Defines AST constructor 'sub'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class sub extends Expression {
     protected Expression e1;
     protected Expression e2;
     /** Creates "sub" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for e1
       * @param a1 initial value for e2
       */
     public sub(int lineNumber, Expression a1, Expression a2) {
         super(lineNumber);
         e1 = a1;
         e2 = a2;
     }
     public TreeNode copy() {
         return new sub(lineNumber, (Expression)e1.copy(), (Expression)e2.copy());
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "sub\n");
         e1.dump(out, n+2);
         e2.dump(out, n+2);
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_sub");
 	e1.dump_with_types(out, n + 2);
 	e2.dump_with_types(out, n + 2);
 	dump_type(out, n);
     }
 
     public Expression getLHS()  { return e1;    }
     public Expression getRHS()   { return e2;    }
 }
 
 
 /** Defines AST constructor 'mul'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class mul extends Expression {
     protected Expression e1;
     protected Expression e2;
     /** Creates "mul" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for e1
       * @param a1 initial value for e2
       */
     public mul(int lineNumber, Expression a1, Expression a2) {
         super(lineNumber);
         e1 = a1;
         e2 = a2;
     }
     public TreeNode copy() {
         return new mul(lineNumber, (Expression)e1.copy(), (Expression)e2.copy());
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "mul\n");
         e1.dump(out, n+2);
         e2.dump(out, n+2);
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_mul");
 	e1.dump_with_types(out, n + 2);
 	e2.dump_with_types(out, n + 2);
 	dump_type(out, n);
     }
 
     public Expression getLHS()  { return e1;    }
     public Expression getRHS()   { return e2;    }    
 
 }
 
 
 /** Defines AST constructor 'divide'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class divide extends Expression {
     protected Expression e1;
     protected Expression e2;
     /** Creates "divide" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for e1
       * @param a1 initial value for e2
       */
     public divide(int lineNumber, Expression a1, Expression a2) {
         super(lineNumber);
         e1 = a1;
         e2 = a2;
     }
     public TreeNode copy() {
         return new divide(lineNumber, (Expression)e1.copy(), (Expression)e2.copy());
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "divide\n");
         e1.dump(out, n+2);
         e2.dump(out, n+2);
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_divide");
 	e1.dump_with_types(out, n + 2);
 	e2.dump_with_types(out, n + 2);
 	dump_type(out, n);
     }
 
     public Expression getLHS()  { return e1;    }
     public Expression getRHS()   { return e2;    }
 }
 
 
 /** Defines AST constructor 'neg'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class neg extends Expression {
     protected Expression e1;
     /** Creates "neg" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for e1
       */
     public neg(int lineNumber, Expression a1) {
         super(lineNumber);
         e1 = a1;
     }
     public TreeNode copy() {
         return new neg(lineNumber, (Expression)e1.copy());
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "neg\n");
         e1.dump(out, n+2);
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_neg");
 	e1.dump_with_types(out, n + 2);
 	dump_type(out, n);
     }
 
     public Expression getExpression()   { return e1;    }
 }
 
 
 /** Defines AST constructor 'lt'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class lt extends Expression {
     protected Expression e1;
     protected Expression e2;
     /** Creates "lt" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for e1
       * @param a1 initial value for e2
       */
     public lt(int lineNumber, Expression a1, Expression a2) {
         super(lineNumber);
         e1 = a1;
         e2 = a2;
     }
     public TreeNode copy() {
         return new lt(lineNumber, (Expression)e1.copy(), (Expression)e2.copy());
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "lt\n");
         e1.dump(out, n+2);
         e2.dump(out, n+2);
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_lt");
 	e1.dump_with_types(out, n + 2);
 	e2.dump_with_types(out, n + 2);
 	dump_type(out, n);
     }
 
     public Expression getLHS()  { return e1;    }
     public Expression getRHS()   { return e2;    }
 }
 
 
 /** Defines AST constructor 'eq'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class eq extends Expression {
     protected Expression e1;
     protected Expression e2;
     /** Creates "eq" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for e1
       * @param a1 initial value for e2
       */
     public eq(int lineNumber, Expression a1, Expression a2) {
         super(lineNumber);
         e1 = a1;
         e2 = a2;
     }
     public TreeNode copy() {
         return new eq(lineNumber, (Expression)e1.copy(), (Expression)e2.copy());
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "eq\n");
         e1.dump(out, n+2);
         e2.dump(out, n+2);
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_eq");
 	e1.dump_with_types(out, n + 2);
 	e2.dump_with_types(out, n + 2);
 	dump_type(out, n);
     }
 
     public Expression getLHS()  { return e1;    }
     public Expression getRHS()   { return e2;    }
 }
 
 
 /** Defines AST constructor 'leq'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class leq extends Expression {
     protected Expression e1;
     protected Expression e2;
     /** Creates "leq" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for e1
       * @param a1 initial value for e2
       */
     public leq(int lineNumber, Expression a1, Expression a2) {
         super(lineNumber);
         e1 = a1;
         e2 = a2;
     }
     public TreeNode copy() {
         return new leq(lineNumber, (Expression)e1.copy(), (Expression)e2.copy());
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "leq\n");
         e1.dump(out, n+2);
         e2.dump(out, n+2);
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_leq");
 	e1.dump_with_types(out, n + 2);
 	e2.dump_with_types(out, n + 2);
 	dump_type(out, n);
     }
 
     public Expression getLHS()  { return e1;    }
     public Expression getRHS()   { return e2;    }
 }
 
 
 /** Defines AST constructor 'comp'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class comp extends Expression {
     protected Expression e1;
     /** Creates "comp" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for e1
       */
     public comp(int lineNumber, Expression a1) {
         super(lineNumber);
         e1 = a1;
     }
     public TreeNode copy() {
         return new comp(lineNumber, (Expression)e1.copy());
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "comp\n");
         e1.dump(out, n+2);
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_comp");
 	e1.dump_with_types(out, n + 2);
 	dump_type(out, n);
     }
 
     public Expression getExpression() { return e1;  }
 }
 
 
 /** Defines AST constructor 'int_const'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class int_const extends Expression {
     protected AbstractSymbol token;
     /** Creates "int_const" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for token
       */
     public int_const(int lineNumber, AbstractSymbol a1) {
         super(lineNumber);
         token = a1;
     }
     public TreeNode copy() {
         return new int_const(lineNumber, copy_AbstractSymbol(token));
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "int_const\n");
         dump_AbstractSymbol(out, n+2, token);
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_int");
 	dump_AbstractSymbol(out, n + 2, token);
 	dump_type(out, n);
     }
 
 }
 
 
 /** Defines AST constructor 'bool_const'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class bool_const extends Expression {
     protected Boolean val;
     /** Creates "bool_const" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for val
       */
     public bool_const(int lineNumber, Boolean a1) {
         super(lineNumber);
         val = a1;
     }
     public TreeNode copy() {
         return new bool_const(lineNumber, copy_Boolean(val));
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "bool_const\n");
         dump_Boolean(out, n+2, val);
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_bool");
 	dump_Boolean(out, n + 2, val);
 	dump_type(out, n);
     }
 
 }
 
 
 /** Defines AST constructor 'string_const'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class string_const extends Expression {
     protected AbstractSymbol token;
     /** Creates "string_const" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for token
       */
     public string_const(int lineNumber, AbstractSymbol a1) {
         super(lineNumber);
         token = a1;
     }
     public TreeNode copy() {
         return new string_const(lineNumber, copy_AbstractSymbol(token));
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "string_const\n");
         dump_AbstractSymbol(out, n+2, token);
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_string");
 	out.print(Utilities.pad(n + 2) + "\"");
 	Utilities.printEscapedString(out, token.getString());
 	out.println("\"");
 	dump_type(out, n);
     }
 
 }
 
 
 /** Defines AST constructor 'new_'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class new_ extends Expression {
     protected AbstractSymbol type_name;
     /** Creates "new_" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for type_name
       */
     public new_(int lineNumber, AbstractSymbol a1) {
         super(lineNumber);
         type_name = a1;
     }
     public TreeNode copy() {
         return new new_(lineNumber, copy_AbstractSymbol(type_name));
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "new_\n");
         dump_AbstractSymbol(out, n+2, type_name);
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_new");
 	dump_AbstractSymbol(out, n + 2, type_name);
 	dump_type(out, n);
     }
 
     public AbstractSymbol getTypeName() { return type_name; }
 }
 
 
 /** Defines AST constructor 'isvoid'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class isvoid extends Expression {
     protected Expression e1;
     /** Creates "isvoid" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for e1
       */
     public isvoid(int lineNumber, Expression a1) {
         super(lineNumber);
         e1 = a1;
     }
     public TreeNode copy() {
         return new isvoid(lineNumber, (Expression)e1.copy());
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "isvoid\n");
         e1.dump(out, n+2);
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_isvoid");
 	e1.dump_with_types(out, n + 2);
 	dump_type(out, n);
     }
 
     public Expression getExpression() { return e1;  }
 }
 
 
 /** Defines AST constructor 'no_expr'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class no_expr extends Expression {
     /** Creates "no_expr" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       */
     public no_expr(int lineNumber) {
         super(lineNumber);
     }
     public TreeNode copy() {
         return new no_expr(lineNumber);
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "no_expr\n");
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_no_expr");
 	dump_type(out, n);
     }
 
 }
 
 
 /** Defines AST constructor 'object'.
     <p>
     See <a href="TreeNode.html">TreeNode</a> for full documentation. */
 class object extends Expression {
     protected AbstractSymbol name;
     /** Creates "object" AST node. 
       *
       * @param lineNumber the line in the source file from which this node came.
       * @param a0 initial value for name
       */
     public object(int lineNumber, AbstractSymbol a1) {
         super(lineNumber);
         name = a1;
     }
     public TreeNode copy() {
         return new object(lineNumber, copy_AbstractSymbol(name));
     }
     public void dump(PrintStream out, int n) {
         out.print(Utilities.pad(n) + "object\n");
         dump_AbstractSymbol(out, n+2, name);
     }
 
     
     public void dump_with_types(PrintStream out, int n) {
         dump_line(out, n);
         out.println(Utilities.pad(n) + "_object");
 	dump_AbstractSymbol(out, n + 2, name);
 	dump_type(out, n);
     }
 
     public AbstractSymbol getName()     { return name;  }
 }
 
 
