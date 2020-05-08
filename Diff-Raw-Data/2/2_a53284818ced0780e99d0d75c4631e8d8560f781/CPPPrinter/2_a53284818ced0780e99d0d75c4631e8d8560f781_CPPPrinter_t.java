 /**
  * xtc - The eXTensible Compiler
  * Copyright (C) 2004-2008 Robert Grimm
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
 package xtc.oop;
 
 import java.util.Iterator;
 
 import xtc.tree.LineMarker;
 import xtc.tree.Node;
 import xtc.tree.GNode;
 import xtc.tree.Pragma;
 import xtc.tree.Printer;
 import xtc.tree.SourceIdentity;
 import xtc.tree.Token;
 import xtc.tree.Visitor;
 
 // lol, finally!
 import java.util.ArrayList;
 
 
 
 /**
  * A pretty printer for C++ based heavily on xtc's Java and C Printers.
  *
  * <p />A note on operator precedence: This printer uses precedence
  * levels to control when to print parentheses around expressions.
  * The actual precedence values are the standard C precedence levels
  * multiplied by ten.
  *
  * @author Robert Grimm
  * @version $Revision: 1.75 $
  */
 public class CPPPrinter extends Visitor {
 
 
 
 	
 	
     /**
      * Class Layout Parser so we can use it's methods
      */
     ClassLayoutParser clp;
 	
     
     /**
      * The flag for printing additional parentheses to avoid gcc
      * warnings.
      */
     public static final boolean EXTRA_PARENTHESES = true;
 	
     /**
      * The base precedence level. This level corresponds to the
      * expression nonterminal.
      */
     public static final int PREC_BASE = 0;
 	
     /**
      * The list precedence level.  This level corresponds to the
      * assignment expression nonterminal.
      */
     public static final int PREC_LIST = 11;
 	
     /**
      * The constant precedence level.  This level corresponds to the
      * conditional expression nonterminal.
      */
     public static final int PREC_CONSTANT = 21;
 	
     /** The flag for any statement besides an if or if-else statement. */
     public static final int STMT_ANY = 0;
 	
     /** The flag for an if statement. */
     public static final int STMT_IF = 1;
 	
     /** The flag for an if-else statement. */
     public static final int STMT_IF_ELSE = 2;
 	
     /** The printer for this C printer. */
     protected final Printer printer;
 	
     /**
      * The flag for whether to line up declarations and statements with
      * their source locations.
      */
     protected final boolean lineUp;
 	
     /** The flag for whether to use GNU coding guidelines. */
     protected final boolean gnuify;
 	
     /** The flag for whether we just printed a declaration. */
     protected boolean isDeclaration;
 	
     /**
      * The flag for whether we just printed a declaration spanning
      * several lines.
      */
     protected boolean isLongDecl;
 	
     /** The flag for whether we just printed a statement. */
     protected boolean isStatement;
 	
     /** The flag for whether the last statement ended with an open line. */
     protected boolean isOpenLine;
 	
     /**
      * The flag for whether the current statement requires nesting or
      * for whether the current declaration is nested within a for
      * statement.
      */
     protected boolean isNested;
 	
     /**
      * The flag for whether this statement is the else clause of an
      * if-else statement.
      */
     protected boolean isIfElse;
 	
     /**
      * The flag for whether this compound statement is an expression (as
      * in the GCC extension).
      */
     protected boolean isStmtAsExpr;
 	
     /**
      * The flag for whether this declarator is a function definition.
      */
     protected boolean isFunctionDef;
 	
     /** The operator precedence level for the current expression. */
     protected int precedence;
 	
     /**
      * Create a new CPP printer.
      *
      * @param printer The printer.
      */
     public CPPPrinter(Printer printer) {
 		this(printer, false, false);
     }
 	
 	
     /**
      * Create a new CPP printer.
      *
      * @param printer The printer.
      * @param lineUp The flag for whether to line up declaratons and
      *   statements with their source locations.
      * @param gnuify The flag for whether to use GNU code formatting
      *   conventions.
      */
 	
 	
     public CPPPrinter(ClassLayoutParser clp, Printer printer) {
 		this(clp, printer, false, false);
 	}
 	
     public CPPPrinter(ClassLayoutParser clp, Printer printer, boolean lineUp, boolean gnuify) 
     {
 		this.clp = clp;
 		this.printer = printer;
 		this.lineUp  = lineUp;
 		this.gnuify  = gnuify;
 		printer.register(this);
     }
 	
     public CPPPrinter(Printer printer, boolean lineUp, boolean gnuify) 
     {
 		this.printer = printer;
 		this.lineUp  = lineUp;
 		this.gnuify  = gnuify;
 		printer.register(this);
     }
 	
     /**
      * Determine whether the specified generic node contains a long type
      * definition.  A long type definition requires multiple lines for
      * readability.  Examples include enumeration, structure, and union
      * type definitions. 
      *
      * @param specs The generic node, representing a list of specifiers.
      * @return <code>true</code> if the specifiers contain a long
      *   type specifier.
      */
     protected boolean containsLongType(GNode specs) {
 		for (Object o : specs) {
 			GNode node = GNode.cast(o);
 			if (node.hasName("EnumerationTypeDefinition") ||
 				node.hasName("StructureTypeDefinition") ||
 				node.hasName("UnionTypeDefinition")) {
 				return true;
 			}
 		}
 		
 		return false;
     }
 	
     /**
      * Determine whether the specified declaration is long.  A long
      * declaration requires multiple lines for readability.  Examples
      * include declarations containing enumeration, structure, and union
      * type definitions.
      *
      * @param decl The declaration.
      * @return <code>true</code> if the specified declaration is long.
      */
     protected boolean isLongDeclaration(GNode decl) {
 		// Make sure the specified generic node actually is a declaration.
 		if (! decl.hasName("Declaration")) return false;
 		
 		// Check the declaration specifiers for enumeration, structure,
 		// and union definitions (but not declarations).
 		if (containsLongType(decl.getGeneric(1))) return true;
 		
 		// Check the initialized declarator list for initializer lists.
 		GNode n = decl.getGeneric(2);
 		if (null != n) {
 			for (Object o : n) {
 				GNode elem = GNode.cast(o).getGeneric(3);
 				if ((null != elem) && elem.hasName("InitializerList")) {
 					return true;
 				}
 			}
 		}
 		
 		// Done.
 		return false;
     }
 	
     /**
      * Print an expression as a truth value.  This method prints the
      * specified node.  If that node represents an assignment expression
      * and {@link #EXTRA_PARENTHESES} is <code>true</code>, this method
      * adds an extra set of parentheses around the expression to avoid
      * gcc warnings.
      *
      * @param n The node to print.
      */
     protected void formatAsTruthValue(Node n) {
 		if (GNode.cast(n).hasName("AssignmentExpression")) {
 			printer.p('(').p(n).p(')');
 		} else {
 			printer.p(n);
 		}
     }
 	
     /**
      * Start a new statement.  This method and the corresponding {@link
      * #prepareNested()} and {@link #endStatement(boolean)} methods
      * provide a reasonable default for newlines and indentation when
      * printing statements.  They manage the {@link #isDeclaration},
      * {@link #isStatement}, {@link #isOpenLine}, {@link #isNested}, and
      * {@link #isIfElse} flags.
      *
      * @param kind The kind of statement, which must be one of the
      *   three statement flags defined by this class.
      * @param node The statement's node.
      * @return The flag for whether the current statement is nested.
      */
     protected boolean startStatement(int kind, Node node) {
 		if (isIfElse && ((STMT_IF == kind) || (STMT_IF_ELSE == kind))) {
 			isNested = false;
 		} else {
 			if (lineUp) {
 				if (isOpenLine) printer.pln();
 				printer.lineUp(node);
 			} else {
 				if (isDeclaration || isOpenLine) {
 					printer.pln();
 				}
 			}
 			if (isNested) {
 				printer.incr();
 			}
 		}
 		
 		isOpenLine     = false;
 		boolean nested = isNested;
 		isNested       = false;
 		
 		return nested;
     }
 	
     /**
      * Prepare for a nested statement.
      *
      * @see #startStatement
      */
     protected void prepareNested() {
 		isDeclaration = false;
 		isStatement   = false;
 		isOpenLine    = true;
 		isNested      = true;
     }
 	
     /**
      * End a statement.
      *
      * @see #startStatement
      *
      * @param nested The flag for whether the current statement is nested.
      */
     protected void endStatement(boolean nested) {
 		if (nested) {
 			printer.decr();
 		}
 		isDeclaration = false;
 		isStatement   = true;
     }
 	
     /**
      * Enter an expression context.  The new context has the specified
      * precedence level.
      *
      * @see #exitContext(int)
      *
      * @param prec The precedence level for the expression context.
      * @return The previous precedence level.
      */
     protected int enterContext(int prec) {
 		int old    = precedence;
 		precedence = prec;
 		return old;
     }
 	
     /**
      * Enter an expression context.  The new context is appropriate for
      * an operand opposite the associativity of the current operator.
      * For example, when printing an additive expression, this method
      * should be called before printing the second operand, as additive
      * operators associate left-to-right.
      *
      * @see #exitContext(int)
      *
      * @return The previous precedence level.
      */
     protected int enterContext() {
 		int old     = precedence;
 		precedence += 1;
 		return old;
     }
 	
     /**
      * Exit an expression context.
      *
      * @see #enterContext(int)
      * @see #enterContext()
      *
      * @param prec The previous precedence level.
      */
     protected void exitContext(int prec) {
 		precedence = prec;
     }
 	
     /**
      * Start printing an expression at the specified operator precedence
      * level.
      *
      * @see #endExpression(int)
      *
      * @param prec The expression's precedence level.
      * @return The previous precedence level.
      */
     protected int startExpression(int prec) {
 		if (prec < precedence) {
 			printer.p('(');
 		}
 		
 		int old    = precedence;
 		precedence = prec;
 		return old;
     }
 	
     /**
      * Stop printing an expression.
      *
      * @see #startExpression(int)
      *
      * @param prec The previous precedence level.
      */
     protected void endExpression(int prec) {
 		if (precedence < prec) {
 			printer.p(')');
 		}
 		precedence = prec;
     }
 	
 	
     /** Visit the specified translation unit node. */
     public void visitTranslationUnit(GNode n) {
 		// Reset the state.
 		isDeclaration  = false;
 		isLongDecl     = false;
 		isStatement    = false;
 		isOpenLine     = false;
 		isNested       = false;
 		isIfElse       = false;
 		isStmtAsExpr   = false;
 		isFunctionDef  = false;
 		precedence     = PREC_BASE;
 		
 		if (lineUp) printer.line(1);
 		
 		for (Object o : n) {
 			if(o instanceof Node) printer.p((Node)o);
 			else if(o instanceof String);
 		}
     }
 	
 	
     /** Visit the specified function definition node. */
     public void visitFunctionDefinition(GNode n) {
 		if (lineUp) {
 			if (isOpenLine) printer.pln();
 			printer.lineUp(n);
 		} else if (isStatement || isDeclaration || isLongDecl) {
 			if (isOpenLine) printer.pln();
 			printer.pln();
 		}
 		
 		isDeclaration = false;
 		isLongDecl    = false;
 		isStatement   = false;
 		isOpenLine    = false;
 		isNested      = false;
 		isIfElse      = false;
 		
 		// Print extension and return type.
 		printer.indent();
 		if ((null != n.get(0)) || (null != n.get(1))) {
 			if (null != n.get(0)) {
 				printer.p("__extension__ ");
 			}
 			printer.p(n.getNode(1));
 		}
 		
 		// Print function name and parameters.
 		Node declor = n.getNode(2);
 		if (gnuify) {
 			GNode g = GNode.cast(declor);
 			if (g.hasName("PointerDeclarator")) {
 				isFunctionDef = true;
 				printer.p(' ').p(declor);
 				declor = g.getNode(1);
 			}
 			printer.pln().indent();
 		} else if (null != n.get(1)) {
 			printer.p(' ');
 		}
 		printer.p(declor);
 		if (null != n.get(3)) {
 			printer.pln().incr().p(n.getNode(3)).decr();
 		} else if (gnuify) {
 			printer.pln();
 			isOpenLine = false;
 		} else {
 			isOpenLine = true;
 		}
 		
 		isDeclaration = false;
 		isLongDecl    = false;
 		isStatement   = false;
 		isNested      = false;
 		isIfElse      = false;
 		
 		printer.p(n.getNode(4)).pln();
 		
 		isDeclaration = true;
 		isLongDecl    = true;
 		isStatement   = false;
 		isOpenLine    = false;
 		isNested      = false;
 		isIfElse      = false;
     }
 	
     /** Visit the specified empty definition. */
     public void visitEmptyDefinition(GNode n) {
 		/* We are not printing anything. */
     }
 	
     /** Visit the specified declaration list node. */
     public void visitDeclarationList(GNode n) {
 
 		for (Object o : n) printer.p((Node)o);
     }
 	
     /** Visit the specified declaration node. */
     public void visitDeclaration(GNode n) {
 	if(!n.isEmpty())
 		printer.p(n.getNode(0));
 		/*
 		 boolean nested = isNested;
 		 if (! nested) {
 		 if (lineUp) {
 		 if (isOpenLine) printer.pln();
 		 printer.lineUp(n);
 		 } else if (isStatement || isLongDecl) {
 		 if (isOpenLine) printer.pln();
 		 printer.pln();
 		 } else if (isLongDeclaration(n)) {
 		 printer.pln();
 		 }
 		 printer.indent();
 		 }
 		 
 		 isDeclaration  = false;
 		 isLongDecl     = false;
 		 isStatement    = false;
 		 isOpenLine     = false;
 		 isNested       = false;
 		 isIfElse       = false;
 		 
 		 //		if (null != n.get(0)) {
 		 //			printer.p("__extension__ ");
 		 //		}
 		 
 		 //		if (null != n.get(2)) {
 		 //			printer.p(' ').p(n.getNode(2));
 		 //			nested = true;
 		 //		}
 		 //		if (! nested) {
 		 //			printer.p(';').fitMore().pln();
 		 //		}
 		 
 		 isDeclaration  = true;
 		 isStatement    = false;
 		 isOpenLine     = false;
 		 isNested       = false;
 		 isIfElse       = false;
 		 
 		 */
     }
 	
     /** Visit the specified declaration specifiers node. */
     public void visitDeclarationSpecifiers(GNode n) {
 		// Removed this node. not called
 	    
 		for(Object o : n ) {
 			if( o instanceof GNode ) {
 				GNode x = GNode.cast(o);
 				
 				if (x.hasName("TypedefSpecifier")) {}
 				else if (x.hasName("PrimaryIdentifier")){}
 				
 				else printer.p(x);
 			}
 		} // end for
 		
     }
 	
     /** Visit the specified auto storage class specifier node. */
     public void visitAutoSpecifier(GNode n) {
 		printer.p("auto");
     }
 	
     /** Visit the specified extern storage class specifier node. */
     public void visitExternSpecifier(GNode n) {
 		printer.p("extern");
     }
 	
     /** Visit the specified register storage class specifier node. */
     public void visitRegisterSpecifier(GNode n) {
 		printer.p("register");
     }
 	
     /** Visit the specified static storage class specifier node. */
     public void visitStaticSpecifier(GNode n) {
 		printer.p("static");
     }
 	
     /** Visit the specified thread storage class specifier node. */
     public void visitThreadSpecifier(GNode n) {
 		printer.p("__thread");
     }
 	
 	
     public void visitTypedefDeclaration(GNode n) {
 		printer.indent().p(n.getNode(0)).p(" ");
 		
 		// If Smart Pointers...
         printer.p("__rt::Ptr<__").p(n.getNode(1)).p("> ");
 		printer.p(n.getNode(1)).p(";").pln();
 		
 		//	printer.p("__").p(n.getNode(1)).p("* ").p(n.getNode(1)).p(";").pln();
 		printer.pln();
     }
 	
 	
     /** Visit the specified typedef storage class specifier node. */
     public void visitTypedefSpecifier(GNode n) {
 		
 		printer.p("typedef");
     }
     
     /** Visit the specified volatile qualifier node. */
     public void visitVolatileQualifier(GNode n) {
 		printer.p(n.getString(0));
     }
 	
     /** Visit the specified constant qualifier node. */
     public void visitConstantQualifier(GNode n) {
 		printer.p(n.getString(0));
     }
 	
     /** Visit the specified restrict qualifier node. */
     public void visitRestrictQualifier(GNode n) {
 		printer.p(n.getString(0));
     }
 	
     /** Visit the specified function specifier node. */
     public void visitFunctionSpecifier(GNode n) {
 		printer.p(n.getString(0));
     }
 	
     /** Visit the specified enumeration type definition. */
     public void visitEnumerationTypeDefinition(GNode n) {
 		printer.p("enum ");
 		if (null != n.get(0)) {
 			printer.p(n.getNode(0)).p(' ');
 		}
 		if (null != n.get(1)) {
 			printer.p(n.getString(1)).p(' ');
 		}
 		printer.pln('{').incr().p(n.getNode(2)).decr().indent().p('}');
 		if (null != n.get(3)) {
 			printer.p(' ').p(n.getNode(3));
 		}
 		isLongDecl = true;
     }
 	
     /** Visit the specified enumerator list node. */
     public void visitEnumeratorList(GNode n) {
 		for (Iterator<?> iter = n.iterator(); iter.hasNext(); ) {
 			printer.indent().p((Node)iter.next());
 			if (iter.hasNext()) {
 				printer.p(',');
 			}
 			printer.pln();
 		}
     }
 	
     /** Visit the specified enumerator node. */
     public void visitEnumerator(GNode n) {
 		printer.p(n.getString(0));
 		if (null != n.get(1)) {
 			int prec = enterContext(PREC_CONSTANT);
 			printer.p(" = ").p(n.getNode(1));
 			exitContext(prec);
 		}
     }
 	
     /** Visit the specified enumeration type reference node. */
     public void visitEnumerationTypeReference(GNode n) {
 		printer.p("enum ");
 		if (null != n.get(0)) {
 			printer.p(n.getNode(0)).p(' ');
 		}
 		printer.p(n.getString(1));
     }
 	
 	
     // GLOBAL VARIABLE className
     String className = "CLASSNAME";
 	
     /** Visit the specified structure type definition. */
     public void visitStructureTypeDefinition(GNode n) {
 		
 		if("DataLayout".equals(n.getString(0))) {
 			printer.indent().p("// The data layout for ");
 			printer.p(n.getString(1)).pln();
 			printer.indent().p("struct __").p(n.getString(1)).pln(" {");
 			printer.incr();
 		}
 		else if("VTable".equals(n.getString(0))) {
 			printer.indent().p("// The vtable layout for ");
 			printer.p(n.getString(1)).pln();
 			printer.indent().p("struct __").p(n.getString(1));
 			printer.p("_VT").pln(" {");
 			printer.incr();
 		}
 		
 		for(Object o : n ) {
 			if( o instanceof GNode ) printer.p((GNode)o);
 			else ; // do nothing
 		}
 		
 		// Hardcoding class
 		if("DataLayout".equals(n.getString(0)))  {
 			printer.pln();
 			printer.indent().p("static Class __class();").pln();
 			printer.pln();
 		}
 	    
 		isLongDecl = true;
 		
 		printer.decr();
 		printer.indent().pln("};").pln();
     }
     
 	
     // Used to print out custom __class()
     public void visitCustomClasses(GNode n) {
 		
 		for (Iterator<?> iter = n.iterator(); iter.hasNext(); ) 
 			printer.p(' ').p((Node)iter.next());
     }
 	
     // FIXME: Extra spaces around type
     public void visitCustomClass(GNode n) {
 		
 		// 0 = Parent, wrapped in Type node
 		// 1 = Component, wrapped in Type node
 		
 		printer.indent().p("// Internal accessor for java.lang.");
 		printer.p(n.getNode(1)).p("'s class.").pln();
 		
 		printer.indent().p("Class __").p(n.getNode(1)).p("::__class() {").pln();
 		printer.incr();
 		printer.indent().p("static Class k = ").pln();
 		printer.incr().indent().p("new __Class(__rt::literal(\"java.lang.");
 		printer.p(n.getNode(1)).p("\"), __").p(n.getNode(0)).p("::__class());").pln();
 		printer.decr();
 		printer.indent().p("return k;").pln();
 		printer.decr();
 		printer.indent().p("}").pln();
 		printer.pln();
     }
 
 
 
     // Used to print out the struct for primitive types that aren't int
     public void visitPrimitiveStruct(GNode n) {
 	
 	printer.indent().pln();
 	printer.p("// The completely incomplete data layout for java.lang.");
 	printer.p(n.getNode(0)).pln();
 	printer.p("struct __").p(n.getNode(0)).p(" {").pln();
 	printer.p("// The class instance representing the primitive type");
 	printer.p(n.getNode(0)).pln();
 	printer.p("static Class TYPE();").pln();
 	printer.indent().p("};").pln().pln();
 	printer.decr();
 
     }
 
     public void visitPrimitiveTYPES(GNode n) {
 	printer.indent().p("namespace java {").pln();
 	printer.incr();
 	printer.indent().p("namespace lang {").pln();
 	for (Object o : n) printer.p((Node)o);
 	printer.pln("}");
 	printer.decr();
 	printer.pln("}");
 	printer.decr();
     }
 
     public void visitPrimitiveTYPE(GNode n) {
 
 	
 	// CHeck if we've made it already
 	String type = n.getNode(0).getNode(0).getString(0);
 	for(int i = 0; i < done.size(); i++) {
 	    if(done.get(i).equals(type.toUpperCase())) {
 		return;
 	    }
 	}
 	
 	printer.indent().p("Class __").p(n.getNode(0));
 	printer.p("::TYPE() {").pln();
 	printer.incr();
 	printer.indent().p("static Class k = ").pln();
 	printer.incr().indent();
 	printer.p("new __Class(__rt::literal(\"").p(n.getNode(1));
 	printer.p("\"), __rt::null(), __rt::null(), true);").pln();
 	printer.pln("return k;");
 	printer.decr();
 	printer.p("}");
 
 	// Mark that we've already created specialization stuff for this type
 
 	done.add(type.toUpperCase());
 	    
 
     }
 
     // LOL HACK - Only need to print specialization etc. once
     // so keep an array of all types we've specialized. 
     ArrayList done = new ArrayList();
     ArrayList done2 = new ArrayList();
 	
     // Used to print template specialization of __class() for arrays
     public void visitArrayTemplates(GNode n) {
 		
 		printer.pln().indent().p("namespace __rt {").pln();
 		printer.incr();
 		
 		for (Iterator<?> iter = n.iterator(); iter.hasNext(); ) 
 			printer.p(' ').p((Node)iter.next());
 		
 		printer.decr().pln("}").pln();
 		
     }
     
     public void visitArrayTemplate(GNode n) {
 
 
 	// CHeck if we've made it already
 	if(n.size() == 1) {
 	    String type = n.getNode(0).getNode(0).getNode(0).getString(0);
 	    for(int i = 0; i < done2.size(); i++) {
 		if(done2.get(i).equals(type.toUpperCase())) {
 		    return;
 		}
 	    }
 	}
 
 
 
 	if(n.size() == 1) {
 	    // It's a primitive type, need to memset!
 	    GNode type = (GNode) n.getNode(0);
 
 	    printer.indent().p("// Template specialization for array of ");
 	    printer.p(type).pln();
 	    printer.indent().pln("template<>");
 
 	    printer.indent().p("Array <").p(type).p(">::Array(const ");
 	    printer.p("int length)").pln();
 	    printer.indent().p(": __vptr(&__vtable), length(length), ");
 	    printer.p("__data(new ").p(type).p("[length]) {").pln();
 	    printer.indent().p("std::memset(__data, 0, length * sizeof(");
 	    printer.p(type).p("));").pln();
 	    printer.decr().pln("}");
 	}
 
 	// Now do actual template specialzation for everyone
 	// Just, for primitives, set both to Object and get first letter to capitals
 
 	printer.indent().p("// Template specialization for array of").p(n.getNode(0)).pln();
 	printer.indent().pln("template<>");
 	
 	if(n.size() == 1) {
 	    printer.indent().p("java::lang::Class Array <");
 	}
 	else {
 	    printer.indent().p("java::lang::Class Array <java::lang::");
 	}
 
 	if(n.size() == 1) {
 	    printer.p(n.getNode(0)).p(">::__class() {").pln();
 	}
 	else {
 	    printer.p(n.getNode(1)).p(">::__class() {").pln();
 	}
 
 	printer.incr();
 	
 	printer.indent().p("static java::lang::Class k = ").pln();
 	printer.incr();
 	// FIXME: Need first letter of the type if its primitive
 	String letter = "L";
 	String type;
 	if(n.size() == 1) {
 	    type = n.getNode(0).getNode(0).getNode(0).getString(0);
 	    letter = Character.toString(type.charAt(0));
 	    letter = letter.toUpperCase();
 	    System.out.println("Primitive type = " + letter);
 	}
 
 	printer.indent().p("new java::lang::__Class(literal(\"[");
 	if(n.size() == 1 ) {
 	    printer.p(letter);
 	    printer.p("\"),").pln();
 	}
 	else {
 	    printer.p(letter).p("java.lang.");
 	    printer.p(n.getNode(1)).p(";\"),").pln();
 	}
 
 		
 	// This follow JVM specs.  parent is parent.
 	printer.incr();
 	if(n.size() == 1) {
 	    printer.indent().p("java::lang::__Object");
 	}
 	else {
 	    printer.indent().p("java::lang::__").p(n.getNode(0));
 	}
 
 	printer.p("::__class(),").pln();
 		
 	// if is primtive, need to do __TYPE::TYPE()
 	printer.indent().p("java::lang::__");
 	if(n.size() == 1) {
 	    // need to make it proper case
 	    type = n.getNode(0).getNode(0).getNode(0).getString(0);
 	    type = type.substring(0,1).toUpperCase() + 
 		type.substring(1).toLowerCase();
 	    printer.p(type).p("::TYPE());");
 	}
 	else {
 	    printer.p(n.getNode(1)).p("::__class());");
 	}
 	
 	printer.pln().decr();
 		
 	printer.decr();
 		
 	printer.indent().pln("return k;");
 		
 	printer.decr();
 	printer.indent().pln("}");
 
 
 	// Mark that we've already created specialization stuff for this type
 	if(n.size() == 1) {
 	    type = n.getNode(0).getNode(0).getNode(0).getString(0);
 	    done2.add(type.toUpperCase());
 	}
 	else {
 	    // right index?
 	    String tmp = n.getNode(0).getNode(0).getString(0);
 	    done2.add(tmp.toUpperCase());
 	}
 
 		
     }
 	
     
 	
     public void visitParentType(GNode n) {
 		// Just a wrapper for a Type node
 		for (Iterator<?> iter = n.iterator(); iter.hasNext(); ) 
 			printer.p((Node)iter.next());
     }
 	
     public void visitComponentType(GNode n) {
 		
 		// Just a wrapper for a Type node
 		for (Iterator<?> iter = n.iterator(); iter.hasNext(); ) 
 			printer.p((Node)iter.next());
     }
 	
 	
     // Not Used
     /** Visit the specified structure type reference. */
 	
     public void visitStructureTypeReference(GNode n) {
 		if (null != n.get(0)) {
 			printer.p(n.getNode(0)).p(' ');
 		}
 		printer.p(n.getString(1));
     }
 	
     
     // Not used
     /** Visit the specified union type definition. */
 	
     public void visitUnionTypeDefinition(GNode n) {
 		printer.p("union ");
 		if (null != n.get(0)) {
 			printer.p(n.getNode(0)).p(' ');
 		}
 		if (null != n.get(1)) {
 			printer.p(n.getString(1)).p(' ');
 		}
 		printer.pln('{').incr().p(n.getNode(2)).decr().indent().p('}');
 		if (null != n.get(3)) {
 			printer.p(' ').p(n.getNode(3));
 		}
 		isLongDecl = true;
     }
 	
 	
     // Not used
     /** Visit the specified union type reference. */
 	
     public void visitUnionTypeReference(GNode n) {
 		printer.p("union ");
 		if (null != n.get(0)) {
 			printer.p(n.getNode(0)).p(' ');
 		}
 		printer.p(n.getString(1));
     }
 	
     /** Visit the specified structure declaration list node. */
     public void visitStructureDeclarationList(GNode n) {
 		boolean wasLong = false;
 		
 		printer.incr();
 		
 		for( Object o : n ) {
 			printer.p((GNode)o);
 		}
 		printer.decr();
 		
 		
     }
 	
     // Never called
     /** Visit the specified structure declaration node. */
     /**
 	 public void visitStructureDeclaration(GNode n) {
 	 
 	 // never called!
 	 printer.indent();
 	 if (null != n.get(0)) {
 	 printer.p("__extension__ ");
 	 }
 	 printer.p(n.getNode(1));
 	 if (null != n.get(2)) {
 	 printer.p(' ').p(n.getNode(2));
 	 }
 	 printer.pln(';');
 	 }
 	 **/
 	
     /** Visit the specified specifier qualifier list node. */
     public void visitSpecifierQualifierList(GNode n) {
 		for (Iterator<?> iter = n.iterator(); iter.hasNext(); ) {
 			printer.p((Node)iter.next());
 			if (iter.hasNext()) printer.p(' ');
 		}
     }
 	
     /** Visit the specified bit field node. */
     public void visitBitField(GNode n) {
 		if (null != n.get(0)) {
 			printer.p(n.getNode(0)).p(' ');
 		}
 		printer.p(n.getNode(1)).p(':');
 		
 		int prec = enterContext(PREC_CONSTANT);
 		printer.p(n.getNode(2));
 		exitContext(prec);
 		
 		if (null != n.get(3)) {
 			printer.p(' ').p(n.getNode(3));
 		}
     }
 	
     /** Visit the specified attributed declarator node. */
     public void visitAttributedDeclarator(GNode n) {
 		if (null != n.get(0)) {
 			printer.p(n.getNode(0)).p(' ');
 		}
 		printer.p(n.getNode(1));
 		if (null != n.get(2)) {
 			printer.p(' ').p(n.getNode(2));
 		}
     }
 	
     /** Visit the specified pointer declarator node. */
     public void visitPointerDeclarator(GNode n) {
 		printer.p(n.getNode(0));
 		if (isFunctionDef) {
 			isFunctionDef = false;
 			return;
 		}
 		printer.p(n.getNode(1));
     }
 	
     /** Visit the specified pointer node. */
     public void visitPointer(GNode n) {
 		printer.p('*').p(n.getNode(0)).p(n.getNode(1));
     }
 	
     /** Visit the specified type qualifier list node. */
     public void visitTypeQualifierList(GNode n) {
 		for (Iterator<?> iter = n.iterator(); iter.hasNext(); ) {
 			printer.p(' ').p((Node)iter.next());
 			if (! iter.hasNext()) {
 				printer.p(' ');
 			}
 		}
     }
 	
     /** Visit the specified simple declarator node. */
     public void visitSimpleDeclarator(GNode n) {
 		printer.p(n.getString(0));
     }
 	
     /** Visit the specified function declarator node. */
     public void visitFunctionDeclarator(GNode n) {
 		if (n.getGeneric(0).hasName("SimpleDeclarator")) {
 			printer.p(n.getNode(0));
 		} else {
 			printer.p('(').p(n.getNode(0)).p(')');
 		}
 		printer.p('(').p(n.getNode(1)).p(')');
     }
 	
     /** Visit the specified parameter type list node. */
     public void visitParameterTypeList(GNode n) {
 		printer.p(n.getNode(0));
 		if (null != n.get(1)) {
 			printer.p(", ...");
 		}
     }
 	
     /** Visit the specified parameter list node. */
     public void visitParameterList(GNode n) {
 		for (Iterator<?> iter = n.iterator(); iter.hasNext(); ) {
 			printer.p((Node)iter.next());
 			if (iter.hasNext()) {
 				printer.p(", ");
 			}
 		}
     }
 	
     /** Visit the specified parameter declaration node. */
     public void visitParameterDeclaration(GNode n) {
 		printer.p(n.getNode(0));
 		if (null != n.get(1)) {
 			printer.p(' ').p(n.getNode(1));
 		}
 		if (null != n.get(2)) {
 			printer.p(' ').p(n.getNode(2));
 		}
     }
 	
     /** Visit the specified attributed abstract declarator node. */
     public void visitAttributedAbstractDeclarator(GNode n) {
 		if (null != n.get(0)) {
 			printer.p(n.getNode(0)).p(' ');
 		}
 		printer.p(n.getNode(1));
     }
 	
     /** Visit the specified abstract declarator node. */
     public void visitAbstractDeclarator(GNode n) {
 		printer.p(n.getNode(0)).p(n.getNode(1));
     }
 	
     /** Visit the specified direct abstract declarator node. */
     public void visitDirectAbstractDeclarator(GNode n) {
 		if (null != n.get(0)) {
 			printer.p('(').p(n.getNode(0)).p(')');
 		}
 		if ("[".equals(n.get(1))) {
 			printer.p('[');
 			
 			if (null != n.get(2)) {
 				int prec = enterContext(PREC_LIST);
 				printer.p(n.getNode(2));
 				exitContext(prec);
 			}
 			
 			printer.p(']');
 			
 		} else {
 			printer.p('(').p(n.getNode(2)).p(')');
 		}
     }
 	
     /** Visit the specified identifier list node. */
     public void visitIdentifierList(GNode n) {
 		for (Iterator<?> iter = n.iterator(); iter.hasNext(); ) {
 			printer.p(Token.cast(iter.next()));
 			if (iter.hasNext()) {
 				printer.p(", ");
 			}
 		}
     }
 	
     /** Visit the specified array declarator node. */
     public void visitArrayDeclarator(GNode n) {
 		if (n.getGeneric(0).hasName("SimpleDeclarator")) {
 			printer.p(n.getNode(0));
 		} else {
 			printer.p('(').p(n.getNode(0)).p(')');
 		}
 		
 		printer.p('[');
 		int column = printer.column();
 		printer.p(n.getNode(1));
 		
 		if (null != n.get(2)) {
 			if (printer.column() != column) printer.p(' ');
 			int prec = enterContext(PREC_LIST);
 			printer.p(n.getNode(2));
 			exitContext(prec);
 		}
 		printer.p(']');
     }
 	
     /** Visit the specified variable length node. */
     public void visitVariableLength(GNode n) {
 		printer.p('*');
     }
 	
     /** Visit the specified array qualifier list node. */
     public void visitArrayQualifierList(GNode n) {
 		for (Iterator<?> iter = n.iterator(); iter.hasNext(); ) {
 			printer.p((Node)iter.next());
 			if (iter.hasNext()) {
 				printer.p(' ');
 			}
 		}
     }
 	
     /** Visit the specified complex node. */
     public void visitComplex(GNode n) {
 		printer.p(n.getString(0));
     }
 	
     /** Visit the specified double node. */
     public void visitDouble(GNode n) {
 		printer.p("double");
     }
 	
     /** Visit the specified float node. */
     public void visitFloat(GNode n) {
 		printer.p("float");
     }
 	
     /** Visit the specified long node. */
     public void visitLong(GNode n) {
 		printer.p("long");
     }
 	
     /** Visit the specified int node. */
     public void visitInt(GNode n) {
 		printer.p("int");
     }
 	
     /** Visit the specified short node. */
     public void visitShort(GNode n) {
 		printer.p("short");
     }
 	
     /** Visit the specified char node. */
     public void visitChar(GNode n) {
 		printer.p("char");
     }
 	
     /** Visit the specified bool node. */
     public void visitBool(GNode n) {
 		printer.p("_Bool");
     }
 	
     /** Visit the specified unsigned node. */
     public void visitUnsigned(GNode n) {
 		printer.p("unsigned");
     }
 	
     /** Visit the specified signed node. */
     public void visitSigned(GNode n) {
 		printer.p(n.getString(0));
     }
 	
     /** Visit the specified typedef name node. */
     public void visitTypedefName(GNode n) {
 		printer.p(n.getString(0));
     }
 	
     /** Visit the specified typeof specifier node. */
     public void visitTypeofSpecifier(GNode n) {
 		int prec = enterContext(PREC_BASE);
 		printer.p("typeof(").p(n.getNode(0)).p(')');
 		exitContext(prec);
     }
 	
     /** Visit the specified void type specifier node. */
     public void visitVoidTypeSpecifier(GNode n) {
 		printer.p("void");
     }
 	
     /** Visit the specified variable argument list specifier node. */
     public void visitVarArgListSpecifier(GNode n) {
 		printer.p("__builtin_va_list");
     }
 	
 	
     public void visitForwardDeclaration(GNode n) {
 		// Holds the nodes for forward declaration of structs and typedefs
 		
 		for (Iterator<?> iter = n.iterator(); iter.hasNext(); ) {
 			printer.p((Node)iter.next());
 			if (iter.hasNext()) printer.pln();
 		}
 		
 		
     }
 	
 	
     /** Visit the specified initialized declarator list node. */
     public void visitInitializedDeclaratorList(GNode n) {
 		
 		boolean  first = true;	    
 		
 		if(null != n.getNode(0).getNode(1)) {
 			
 			className = 
 			n.getNode(0).getNode(1).getString(0);
 			
 			printer.pln();
 			printer.indent().pln("// Forward declaration of datalayout and vt");
 			// FIXME: Do I need templates?
 			printer.indent().p("struct __").p(n.getNode(0)).pln(";");
 			printer.indent().p("struct __").p(n.getNode(0)).pln("_VT;");
 			
        	}
 		
 		else for(Object o : n ) { // catch all
 			printer.pln("// Missed an IntializedDeclaratorList");
 			
 	    }
     }
     
     /** Visit the specified initialized declarator node. */
     public void visitInitializedDeclarator(GNode n) {
 		// TODO: 
 		for(Object o : n ) {
 			if( o instanceof GNode ) printer.p((GNode)o);
 			else if (o instanceof String) printer.p((String)o);
 		}
     }
 	
     /** Visit the specified initializer list node. */
     public void visitInitializerList(GNode n) {
 		if (! n.isEmpty()) {
 			printer.pln('{').incr().indent();
 			for (Iterator<?> iter = n.iterator(); iter.hasNext(); ) {
 				printer.buffer().p((Node)iter.next());
 				if (iter.hasNext()) {
 					printer.p(", ");
 				}
 				printer.fit();
 			}
 			printer.pln().decr().indent().p('}');
 			isLongDecl = true;
 		} else {
 			printer.p("{ }");
 		}
     }
 	
     /** Visit the specified initializer list entry node. */
     public void visitInitializerListEntry(GNode n) {
 		printer.p(n.getNode(0));
 		int prec = enterContext(PREC_LIST);
 		printer.p(n.getNode(1));
 		exitContext(prec);
     }
 	
     /** Visit the specified designation node. */
     public void visitDesignation(GNode n) {
 		for (Iterator<?> iter = n.iterator(); iter.hasNext(); ) {
 			printer.p((Node)iter.next());
 			if (iter.hasNext()) {
 				printer.p(' ');
 			}
 		}
 		printer.p(" = ");
     }
 	
     /** Visit the specified obsolete array designation node. */
     public void visitObsoleteArrayDesignation(GNode n) {
 		// Print the obsolete array designation as a standard array
 		// designation.
 		int prec = enterContext(PREC_CONSTANT);
 		printer.p('[').p(n.getNode(1));
 		if (3 == n.size() && null != n.getGeneric(2)) {
 			printer.p(" ... ").p(n.getNode(2));
 		}
 		printer.p(']');
 		exitContext(prec);
 		printer.p(" = ");
     }
 	
     /** Visit the specified obsolete field designation node. */
     public void visitObsoleteFieldDesignation(GNode n) {
 		// Print the obsolete field designation as a standard field
 		// designation.
 		printer.p('.').p(n.getString(0)).p(" = ");
     }
 	
     /** Visit the specified designator entry node. */
     public void visitDesignator(GNode n) {
 		if ("[".equals(n.get(0))) {
 			int prec = enterContext(PREC_CONSTANT);
 			printer.p('[').p(n.getNode(1));
 			if (3 == n.size()) {
 				printer.p(" ... ").p(n.getNode(2));
 			}
 			printer.p(']');
 			exitContext(prec);
 		} else {
 			printer.p('.').p(n.getNode(1));
 		}
     }
 	
     /** Visit the specified type name node. */
     public void visitTypeName(GNode n) {
 		printer.p(n.getNode(0));
 		if (null != n.get(1)) {
 			printer.p(' ').p(n.getNode(1));
 		}
     }
 	
     /** Visit the specified attribute specifier list node. */
     public void visitAttributeSpecifierList(GNode n) {
 		for (Iterator<?> iter = n.iterator(); iter.hasNext(); ) {
 			printer.p((Node)iter.next());
 			if (iter.hasNext()) {
 				printer.p(' ');
 			}
 		}
     }
 	
     /** Visit the specified attribute specifier node. */
     public void visitAttributeSpecifier(GNode n) {
 		printer.p("__attribute__((").p(n.getNode(0)).p("))");
     }
 	
     /** Visit the specified attribute list node. */
     public void visitAttributeList(GNode n) {
 		for (Iterator<?> iter = n.iterator(); iter.hasNext(); ) {
 			printer.p((Node)iter.next());
 			if (iter.hasNext()) {
 				printer.p(", ");
 			}
 		}
     }
 	
     /** Visit the specified attribute list entry node. */
     public void visitAttributeListEntry(GNode n) {
 		printer.p(n.getString(0));
 		if (null != n.get(1)) {
 			printer.p('(').p(n.getNode(1)).p(')');
 		}
     }
 	
     /** Visit the specified labeled statement node. */
     public void visitLabeledStatement(GNode n) {
 		boolean nested = startStatement(STMT_ANY, n);
 		printer.indentLess().p(n.getNode(0)).pln();
 		isDeclaration  = false;
 		isStatement    = true;
 		printer.p(n.getNode(1));
 		endStatement(nested);
     }
 	
     /** Visit the specified named label node. */
     public void visitNamedLabel(GNode n) {
 		printer.p(n.getString(0));
 		if (null == n.get(1)) {
 			printer.p(':');
 		} else {
 			printer.p(": ").p(n.getNode(1));
 		}
     }
 	
     /** Visit the specified case label node. */
     public void visitCaseLabel(GNode n) {
 		int prec = enterContext(PREC_CONSTANT);
 		printer.p("case ").p(n.getNode(0));
 		if (2 == n.size()) {
 			printer.p(" ... ").p(n.getNode(1));
 		}
 		printer.p(':');
 		exitContext(prec);
     }
 	
     /** Visit the specified default label node. */
     public void visitDefaultLabel(GNode n) {
 		printer.p("default:");
     }
 	
     /** Visit the specified compound statement node. */
     public void visitCompoundStatement(GNode n) {
 		boolean stmtAsExpr = isStmtAsExpr;
 		isStmtAsExpr       = false;
 		boolean nested     = isNested;
 		
 		if (stmtAsExpr) {
 			printer.pln(" ({").incr();
 		} else if (nested && gnuify) {
 			printer.pln().incr().indent().pln('{').incr();
 		} else if (isOpenLine) {
 			printer.pln(" {").incr();
 		} else {
 			printer.indent().pln('{').incr();
 		}
 		
 		isOpenLine    = false;
 		isNested      = false;
 		isIfElse      = false;
 		for (Object o : n) printer.p((Node)o);
 		
 		if (isOpenLine) {
 			printer.pln();
 		}
 		if (stmtAsExpr) {
 			printer.decr().indent().p("})");
 			isOpenLine = true;
 		} else if (nested && gnuify) {
 			printer.decr().indent().pln('}').decr();
 			isOpenLine = false;
 		} else {
 			printer.decr().indent().p('}');
 			isOpenLine = true;
 		}
 		isNested      = false;
 		isIfElse      = false;
 		isStmtAsExpr  = stmtAsExpr;
     }
 	
     /** Visit the specified label declaration node. */
     public void visitLocalLabelDeclaration(GNode n) {
 		if (! isNested) {
 			if (lineUp) {
 				if (isOpenLine) printer.pln();
 				printer.lineUp(n);
 			} else if (isStatement || isLongDecl) {
 				if (isOpenLine) printer.pln();
 				printer.pln();
 			} else if (isLongDeclaration(n)) {
 				printer.pln();
 			}
 			printer.indent();
 		}
 		
 		printer.p("__label__ ");
 		
 		for (Iterator<?> iter = n.iterator(); iter.hasNext(); ) {
 			printer.p(Token.cast(iter.next()));
 			if (iter.hasNext()) {
 				printer.p(", ");
 			}
 		}
 		
 		printer.pln(';');
 		
 		isDeclaration  = true;
 		isStatement    = false;
 		isOpenLine     = false;
 		isNested       = false;
 		isIfElse       = false;
     }
 	
     /** Visit the specified if else statement node. */
     public void visitIfElseStatement(GNode n) {
 		boolean nested = startStatement(STMT_IF_ELSE, n);
 		if (isIfElse) {
 			printer.p(' ');
 		} else {
 			printer.indent();
 		}
 		printer.p("if (");
 		formatAsTruthValue(n.getNode(0));
 		printer.p(')');
 		prepareNested();
 		printer.p(n.getNode(1));
 		if (isOpenLine) {
 			printer.p(" else");
 		} else {
 			printer.indent().p("else");
 		}
 		prepareNested();
 		boolean ifElse = isIfElse;
 		isIfElse       = true;
 		printer.p(n.getNode(2));
 		isIfElse       = ifElse;
 		endStatement(nested);
     }
 	
     /** Visit the specified if statement node. */
     public void visitIfStatement(GNode n) {
 		boolean nested = startStatement(STMT_IF, n);
 		if (isIfElse) {
 			printer.p(' ');
 		} else {
 			printer.indent();
 		}
 		printer.p("if (");
 		formatAsTruthValue(n.getNode(0));
 		printer.p(')');
 		prepareNested();
 		printer.p(n.getNode(1));
 		endStatement(nested);
     }
 	
     /** Visit the specified while statement node. */
     public void visitWhileStatement(GNode n) {
 		boolean nested = startStatement(STMT_ANY, n);
 		printer.indent().p("while (");
 		formatAsTruthValue(n.getNode(0));
 		printer.p(')');
 		prepareNested();
 		printer.p(n.getNode(1));
 		endStatement(nested);
     }
 	
     /** Visit the specified do statement node. */
     public void visitDoStatement(GNode n) {
 		boolean nested = startStatement(STMT_ANY, n);
 		printer.indent().p("do");
 		prepareNested();
 		printer.p(n.getNode(0));
 		if (isOpenLine) {
 			printer.p(' ');
 		} else {
 			printer.indent();
 		}
 		printer.p("while (");
 		formatAsTruthValue(n.getNode(1));
 		printer.pln(");");
 		endStatement(nested);
     }
 	
 	
 	
     public void visitForStatement(GNode n) {
 		final boolean nested = startStatement(STMT_ANY, n);
 		
 		printer.indent().p("for (").p(n.getNode(0)).p(')');
 		prepareNested();
 		// For safety, add {} around single line 
 		// ExpressionStatements
 		if( n.getNode(1).hasName("ExpressionStatement") ) {
 			printer.pln('{').incr();
 			printer.p(n.getNode(1));
 			printer.decr().indent().p('}').pln();
 		}
 		else printer.p(n.getNode(1)); // Block, {} automatic 
 		
 		endStatement(nested);
     }
 	
 	
 	
     // ----------------------------------
     // START: Modded  by Diana!
     // ----------------------------------
 	
     public void visitImportDeclarations(GNode n) {
 		printer.pln("// ------------ begin CC file --------------");
 		// has 0-n children ImportDeclaration
 		for(Object o : n ) if( o instanceof GNode ) printer.p((GNode)o);
 		
     }
 	
 	
     // FIXME: Use tokens
     // @param n always has 3 children
     public void visitImportDeclaration(GNode n) {
 		
 		if(n.get(0) != null) {
 			// What's at 0?
 			printer.p("child 0  = " + n.get(0));
 		}
 		
 		if(n.get(1) != null) { // QualifiedIdentifier
 			printer.p("#include ").p(fold((GNode)n.getNode(1), n.getNode(1).size())).p(";");
 			
 			
 		}
 		
 		if(n.get(2) != null) { // Star import
 			printer.p(".").p(n.getString(2)).p(";");
 		}
 		
 		printer.pln();
 		
     }
 	
 	
     /**
      * Fold the specified qualified identifier.
      *
      * @param qid The qualified identifier.
      * @param size Its size.
      */
     protected String fold(GNode qid, int size) {
 		StringBuilder buf = new StringBuilder();
 		for (int i=0; i<size; i++) {
 			buf.append(qid.getString(i));
 			if (i<size-1) buf.append('.');
 		}
 		return buf.toString();
     }
 	
     /** Visit the specified qualified identifier. */
     // Changed by us
     /**
      // TODO: Change to cpp types
      public void visitQualifiedIdentifier(GNode n) {
      printer.p("// --- START QUALIFIED IDENTIFIER ");
 	 
      final int prec = startExpression(160);
 	 
      if (1 == n.size()) {
      String s = n.getString(0);
 	 
      if("String".equals(s)) s = "__String";
      else if ("boolean".equals(s)) s = "bool";
      else if ("int".equals(s)) s = "int32_t";
 	 
      printer.p(s);
      } 
      else {
      for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
      printer.p(Token.cast(iter.next()));
      if (iter.hasNext()) printer.p('.');
      }
      }
 	 
      endExpression(prec);
      printer.p("// --- END QUALIFIED IDENTIFIER ");
      }
 	 **/ 
 	
 	
     // Hacking global variable to print once.  See below FIXME
     boolean visited = false;
     public void visitClassDeclaration(GNode n) {
 		// TODO: Should we print to separate .h and .cc files?
 		
 		// FIXME: Abstract Class causes multiple ClassDeclaration nodes
 		// FIXME Abstract class doesn't print!
 		if(!visited) {
 			// Commenting out, since .h and .cc are one file
 			printer.pln("// #include <iostream>");
 			printer.pln("// #include \"java_lang.h\"");
 			printer.pln("using namespace java::lang;").pln();
 			visited = true;
 		}
 		
 		// FIXME: Must visit the actual class declaration as well, see above
 		// Visit ClassBody
 		for(Object o : n ) if( o instanceof GNode && GNode.cast(o).hasName("ClassBody")) printer.p((GNode)o);
 		
     }
 	
     public void visitClassBody(GNode n) {
 		// Keep visiting unless it's a FieldDeclaration
 		for(Object o : n ) if( o instanceof GNode ) {
 			if(GNode.cast(o).hasName("FieldDeclaration")) {} // do nothing
 			else if(GNode.cast(o).hasName("ConstructorDeclaration")) {}
 			else printer.p((GNode)o);
 	    }
 		
     }
 	
     public void visitNewClassExpression(GNode n) {
 		
 		
 		for(Object o : n ) {
 			if( o instanceof GNode ) {
 				if(GNode.cast(o).hasName("QualifiedIdentifier")) 
 					printer.p("new __").p((GNode)o).p("()");
 			}
 		}
     }
 	
     public void visitExtension(GNode n) {
 		// TODO: 
 		for(Object o : n ) if( o instanceof GNode ) printer.p((GNode)o);
     }
 	
     // Running random xtc code through our Translator to see what I've missed
     // Below is what I've added, w/default behavior
     
     public void visitTypeArguments(GNode n) {
 		for(Object o : n ) if( o instanceof GNode ) printer.p((GNode)o);
     }
 	
     public void visitWildcard(GNode n) {
 		// Node has null value, print something based on Node exists	for(Object o : n ) if( o instanceof GNode ) printer.p((GNode)o);
 		
     }
 	
     
     
 	
 	
 	
     // ----------------------------------
     // END : Modded by Diana! (Note, not all inclusive)
     // ----------------------------------
 	
 	
 	
 	
     //COPIED FROM JAVA PRINTER:
     /** Visit the specified basic for control. */
     public void visitBasicForControl(GNode n) {
 		
 		printer.p(n.getNode(0));
 		if (null != n.get(1)) printer.p(n.getNode(1)).p(' ');
 	    
 		final int prec1 = enterContext(PREC_BASE);
 		printer.p(n.getNode(2)).p("; ");
 		exitContext(prec1);
 	    
 		if (null != n.get(3)) {
 			final int prec2 = enterContext(PREC_BASE);
 			formatAsTruthValue(n.getNode(3));
 			exitContext(prec2);
 		}
 		printer.p("; ");
 	    
 		final int prec3 = enterContext(PREC_BASE);
 		printer.p(n.getNode(4));
 		exitContext(prec3);
 		
     }
 	
     /** Visit the specified switch statement node. */
     public void visitSwitchStatement(GNode n) {
 		boolean nested = startStatement(STMT_ANY, n);
 		printer.indent().p("switch (").p(n.getNode(0)).p(')');
 		prepareNested();
 		printer.p(n.getNode(1));
 		endStatement(nested);
     }
 	
     /** Visit the specified break statement node. */
     public void visitBreakStatement(GNode n) {
 		boolean nested = startStatement(STMT_ANY, n);
 		printer.indent().pln("break;");
 		endStatement(nested);
     }
 	
     /** Visit the specified continue statement node. */
     public void visitContinueStatement(GNode n) {
 		boolean nested = startStatement(STMT_ANY, n);
 		printer.indent().pln("continue;");
 		endStatement(nested);
     }
 	
     /** Visit the specified return statement node. */
     public void visitReturnStatement(GNode n) {
 		boolean nested = startStatement(STMT_ANY, n);
 		printer.indent().p("return");
 		if (null != n.getNode(0)) {
 			printer.p(' ').p(n.getNode(0));
 		}
 		printer.pln(';');
 		endStatement(nested);
     }
 	
     /** Visit the specified goto statement node. */
     public void visitGotoStatement(GNode n) {
 		boolean nested = startStatement(STMT_ANY, n);
 		printer.indent().p("goto ");
 		if (null == n.get(0)) {
 			printer.p(n.getNode(1));
 		} else {
 			printer.p('*');
 			int prec = enterContext(150);
 			printer.p(n.getNode(1));
 			exitContext(prec);
 		}
 		printer.pln(';');
 		endStatement(nested);
     }
 	
     /** Visit the specified expression statement node. */
     public void visitExpressionStatement(GNode n) {
 		
 		// If primary identifier other than std:; must check not null
 		for( Object o : n) {
 			if (o instanceof Node) {
 				new Visitor() {
 					public void visitPrimaryIdentifier(GNode n) {
 						if( (null != n.get(0)) 
 						   && !n.getString(0).startsWith("std")
 						   && !isConstructor) {
 							printer.indent().p("// __rt::checkNotNull(");
 							printer.p(n).p(");").pln();
 						}
 					}
 					
 					public void visitSubscriptExpression(GNode n) {
 						//			    printer.indent().p(" __rt::checkIndex(").p(n.getNode(0)).p(", ");
 						//			    printer.p(n.getNode(1)).p(");").pln();
 						
 					}
 					
 					public void visit(GNode n) {
 						for( Object o : n) {
 							if (o instanceof Node) dispatch((GNode)o);
 						}
 					}
 				}.dispatch(GNode.cast(o));
 				
 			} // end if
 		} // end for loop
 		
 		// Continue like normal
 		boolean nested = startStatement(STMT_ANY, n);
 		printer.indent().p(n.getNode(0)).pln(';');
 		endStatement(nested);
 		
 		
     }
     
     /** Visit the specified empty statement node. */
     public void visitEmptyStatement(GNode n) {
 		boolean nested = startStatement(STMT_ANY, n);
 		printer.indent().pln(';');
 		endStatement(nested);
     }
 	
     /** Visit the specified comma expression node. */
     public void visitCommaExpression(GNode n) {
 		int prec1 = startExpression(10);
 		printer.p(n.getNode(0)).p(", ");
 		
 		int prec2 = enterContext();
 		printer.p(n.getNode(1));
 		exitContext(prec2);
 		
 		endExpression(prec1);
     }
 	
     /** Visit the specified assignment expression node. */
     public void visitAssignmentExpression(GNode n) {
 		int prec1 = startExpression(20);
 		int prec2 = enterContext();
 		printer.p(n.getNode(0));
 		exitContext(prec2);
 		
 		printer.p(' ').p(n.getString(1)).p(' ').p(n.getNode(2));
 		endExpression(prec1);
     }
 	
     /** Visit the specified conditional expression node. */
     public void visitConditionalExpression(GNode n) {
 		int prec1 = startExpression(30);
 		
 		int prec2 = enterContext();
 		printer.p(n.getNode(0)).p(" ? ");
 		exitContext(prec2);
 		
 		prec2 = enterContext();
 		if (null != n.get(1)) {
 			printer.p(n.getNode(1)).p(" : ");
 		} else {
 			printer.p(" /* Empty */ : ");
 		}
 		exitContext(prec2);
 		
 		printer.p(n.getNode(2));
 		endExpression(prec1);
     }
 	
     /** Visit the specified logical or expression node. */
     public void visitLogicalOrExpression(GNode n) {
 		int     prec1  = startExpression(40);
 		boolean paren1 = n.getGeneric(0).hasName("LogicalAndExpression");
 		boolean paren2 = n.getGeneric(1).hasName("LogicalAndExpression");
 		
 		if (EXTRA_PARENTHESES && paren1) {
 			// Force parentheses to make gcc happy.
 			printer.p('(').p(n.getNode(0)).p(')');
 		} else {
 			printer.p(n.getNode(0));
 		}
 		
 		printer.p(" || ");
 		
 		if (EXTRA_PARENTHESES && paren2) {
 			// Force parentheses to make gcc happy.
 			printer.p('(').p(n.getNode(1)).p(')');
 		} else {
 			int prec2 = enterContext();
 			printer.p(n.getNode(1));
 			exitContext(prec2);
 		}
 		
 		endExpression(prec1);
     }
 	
     /** Visit the specified logical and expression node. */
     public void visitLogicalAndExpression(GNode n) {
 		int prec1 = startExpression(50);
 		printer.p(n.getNode(0)).p(" && ");
 		
 		int prec2 = enterContext();
 		printer.p(n.getNode(1));
 		exitContext(prec2);
 		
 		endExpression(prec1);
     }
 	
     /** Visit the specified bitwise or expression node. */
     public void visitBitwiseOrExpression(GNode n) {
 		int     prec1  = startExpression(60);
 		GNode   op1    = n.getGeneric(0);
 		boolean paren1 = (op1.hasName("AdditiveExpression") ||
 						  op1.hasName("BitwiseAndExpression") ||
 						  op1.hasName("BitwiseXorExpression") ||
 						  op1.hasName("RelationalExpression") ||
 						  op1.hasName("EqualityExpression"));
 		GNode   op2    = n.getGeneric(1);
 		boolean paren2 = (op2.hasName("AdditiveExpression") ||
 						  op2.hasName("BitwiseAndExpression") ||
 						  op2.hasName("BitwiseXorExpression") ||
 						  op2.hasName("RelationalExpression") ||
 						  op2.hasName("EqualityExpression"));
 		
 		if (EXTRA_PARENTHESES && paren1) {
 			// Force parentheses to make gcc happy.
 			printer.p('(').p(n.getNode(0)).p(')');
 		} else {
 			printer.p(n.getNode(0));
 		}
 		
 		printer.p(" | ");
 		
 		if (EXTRA_PARENTHESES && paren2) {
 			// Force parentheses to make gcc happy.
 			printer.p('(').p(n.getNode(1)).p(')');
 		} else {
 			int prec2 = enterContext();
 			printer.p(n.getNode(1));
 			exitContext(prec2);
 		}
 		
 		endExpression(prec1);
     }
 	
     /** Visit the specified bitwise xor expression node. */
     public void visitBitwiseXorExpression(GNode n) {
 		int     prec1  = startExpression(70);
 		GNode   op1    = n.getGeneric(0);
 		boolean paren1 = (op1.hasName("AdditiveExpression") ||
 						  op1.hasName("BitwiseAndExpression") ||
 						  op1.hasName("RelationalExpression") ||
 						  op1.hasName("EqualityExpression"));
 		GNode   op2    = n.getGeneric(1);
 		boolean paren2 = (op2.hasName("AdditiveExpression") ||
 						  op2.hasName("BitwiseAndExpression") ||
 						  op2.hasName("RelationalExpression") ||
 						  op2.hasName("EqualityExpression"));
 		
 		if (EXTRA_PARENTHESES && paren1) {
 			// Force parentheses to make gcc happy.
 			printer.p('(').p(n.getNode(0)).p(')');
 		} else {
 			printer.p(n.getNode(0));
 		}
 		
 		printer.p(" ^ ");
 		
 		if (EXTRA_PARENTHESES && paren2) {
 			// Force parentheses to make gcc happy.
 			printer.p('(').p(n.getNode(1)).p(')');
 		} else {
 			int prec2 = enterContext();
 			printer.p(n.getNode(1));
 			exitContext(prec2);
 		}
 		
 		endExpression(prec1);
     }
 	
     /** Visit the specified bitwise and expression node. */
     public void visitBitwiseAndExpression(GNode n) {
 		int     prec1  = startExpression(80);
 		GNode   op1    = n.getGeneric(0);
 		boolean paren1 = (op1.hasName("AdditiveExpression") ||
 						  op1.hasName("RelationalExpression") ||
 						  op1.hasName("EqualityExpression"));
 		GNode   op2    = n.getGeneric(1);
 		boolean paren2 = (op2.hasName("AdditiveExpression") ||
 						  op2.hasName("RelationalExpression") ||
 						  op2.hasName("EqualityExpression"));
 		
 		if (EXTRA_PARENTHESES && paren1) {
 			// Force parentheses to make gcc happy.
 			printer.p('(').p(n.getNode(0)).p(')');
 		} else {
 			printer.p(n.getNode(0));
 		}
 		
 		printer.p(" & ");
 		
 		if (EXTRA_PARENTHESES && paren2) {
 			// Force parentheses to make gcc happy.
 			printer.p('(').p(n.getNode(1)).p(')');
 		} else {
 			int prec2 = enterContext();
 			printer.p(n.getNode(1));
 			exitContext(prec2);
 		}
 		
 		endExpression(prec1);
     }
 	
     /** Visit the specified equality expression node. */
     public void visitEqualityExpression(GNode n) {
 		int prec1 = startExpression(90);
 		GNode   op1    = n.getGeneric(0);
 		boolean paren1 = (op1.hasName("RelationalExpression") ||
 						  op1.hasName("EqualityExpression"));
 		GNode   op2    = n.getGeneric(2);
 		boolean paren2 = (op2.hasName("RelationalExpression") ||
 						  op2.hasName("EqualityExpression"));
 		
 		if (EXTRA_PARENTHESES && paren1) {
 			// Force parentheses to make gcc happy.
 			printer.p('(').p(n.getNode(0)).p(')');
 		} else {
 			printer.p(n.getNode(0));
 		}
 		
 		printer.p(' ').p(n.getString(1)).p(' ');
 		
 		if (EXTRA_PARENTHESES && paren2) {
 			// Force parentheses to make gcc happy.
 			printer.p('(').p(n.getNode(2)).p(')');
 		} else {
 			int prec2 = enterContext();
 			printer.p(n.getNode(2));
 			exitContext(prec2);
 		}
 		
 		endExpression(prec1);
     }
 	
     /** Visit the specified relational expression node. */
     public void visitRelationalExpression(GNode n) {
 		int     prec1  = startExpression(100);
 		GNode   op1    = n.getGeneric(0);
 		boolean paren1 = (op1.hasName("RelationalExpression") ||
 						  op1.hasName("EqualityExpression"));
 		GNode   op2    = n.getGeneric(2);
 		boolean paren2 = (op2.hasName("RelationalExpression") ||
 						  op2.hasName("EqualityExpression"));
 		
 		if (EXTRA_PARENTHESES && paren1) {
 			// Force parentheses to make gcc happy.
 			printer.p('(').p(n.getNode(0)).p(')');
 		} else {
 			printer.p(n.getNode(0));
 		}
 		
 		printer.p(' ').p(n.getString(1)).p(' ');
 		
 		if (EXTRA_PARENTHESES && paren2) {
 			// Force parentheses to make gcc happy.
 			printer.p('(').p(n.getNode(2)).p(')');
 		} else {
 			int prec2 = enterContext();
 			printer.p(n.getNode(2));
 			exitContext(prec2);
 		}
 		
 		endExpression(prec1);
     }
 	
     /** Visit the specified shift expression node. */
     public void visitShiftExpression(GNode n) {
 		int     prec1  = startExpression(110);
 		boolean paren1 = n.getGeneric(0).hasName("AdditiveExpression");
 		boolean paren2 = n.getGeneric(2).hasName("AdditiveExpression");
 		
 		if (EXTRA_PARENTHESES && paren1) {
 			// Force parentheses to make gcc happy.
 			printer.p('(').p(n.getNode(0)).p(')');
 		} else {
 			printer.p(n.getNode(0));
 		}
 		
 		printer.p(' ').p(n.getString(1)).p(' ');
 		
 		if (EXTRA_PARENTHESES && paren2) {
 			// Force parentheses to make gcc happy.
 			printer.p('(').p(n.getNode(2)).p(')');
 		} else {
 			int prec2 = enterContext();
 			printer.p(n.getNode(2));
 			exitContext(prec2);
 		}
 		
 		endExpression(prec1);
     }
 	
     /** Visit the specified additive expression node. */
     public void visitAdditiveExpression(GNode n) {
 		int prec1 = startExpression(120);
         
 		printer.p(n.getNode(0)).p(' ').p(n.getString(1)).p(' ');
 		
 		int prec2 = enterContext();
 		printer.p(n.getNode(2));
 		exitContext(prec2);
 		
 		endExpression(prec1);
     }
 	
     /** Visit the specified multiplicative expression node. */
     public void visitMultiplicativeExpression(GNode n) {
 		int prec1 = startExpression(130);
 		printer.p(n.getNode(0)).p(' ').p(n.getString(1)).p(' ');
 		
 		int prec2 = enterContext();
 		printer.p(n.getNode(2));
 		exitContext(prec2);
 		
 		endExpression(prec1);
     }
 	
     /** Visit the specified cast expression node. */
 	
     public void visitCastExpression(GNode n) {
 		int prec = startExpression(140);
 		printer.p('(').p(n.getNode(0)).p(')').p(n.getNode(1));
 		endExpression(prec);
     }
 	
 	
     /** Visit the specified sizeof expression node. */
     public void visitSizeofExpression(GNode n) {
 		int prec  = startExpression(150);
 		int prec2 = enterContext(PREC_BASE);
 		printer.p("sizeof(").p(n.getNode(0)).p(')');
 		exitContext(prec2);
 		endExpression(prec);
     }
 	
     /** Visit the specified alignof expression node. */
     public void visitAlignofExpression(GNode n) {
 		int prec  = startExpression(150);
 		int prec2 = enterContext(PREC_BASE);
 		printer.p("__alignof__(").p(n.getNode(0)).p(')');
 		exitContext(prec2);
 		endExpression(prec);
     }
 	
     /** Visit the specified offsetof expression node. */
     public void visitOffsetofExpression(GNode n) {
 		int prec = startExpression(160);
 		printer.p("__builtin_offsetof(").p(n.getNode(0)).p(", ").p(n.getNode(1)).
 	    p(')');
 		endExpression(prec);
     }
 	
     /** Visit the specified type compatability expression. */
     public void visitTypeCompatibilityExpression(GNode n) {
 		int prec  = startExpression(150);
 		printer.p("__builtin_types_compatible_p(");
 		int prec2 = enterContext(PREC_BASE);
 		printer.p(n.getNode(0));
 		exitContext(prec2);
 		prec2     = enterContext(PREC_BASE);
 		printer.p(", ").p(n.getNode(1)).p(')');
 		exitContext(prec2);
 		endExpression(prec);
     }
 	
     /** Visit the specified unary minus expression node. */
     public void visitUnaryMinusExpression(GNode n) {
 		int   prec = startExpression(150);
 		GNode e    = n.getGeneric(0);
 		if (e.hasName("UnaryMinusExpression") ||
 			e.hasName("PredecrementExpression")) {
 			printer.p("-(").p(n.getNode(0)).p(')');
 		} else {
 			printer.p('-').p(n.getNode(0));
 		}
 		endExpression(prec);
     }
 	
     /** Visit the specified unary plus expression node. */
     public void visitUnaryPlusExpression(GNode n) {
 		int   prec = startExpression(150);
 		GNode e    = n.getGeneric(0);
 		if (e.hasName("UnaryPlusExpression") ||
 			e.hasName("PreincrementExpression")) {
 			printer.p("+(").p(n.getNode(0)).p(')');
 		} else {
 			printer.p('+').p(n.getNode(0));
 		}
 		endExpression(prec);
     }
 	
     /** Visit the specified logical negation expression node. */
     public void visitLogicalNegationExpression(GNode n) {
 		int prec = startExpression(150);
 		printer.p('!').p(n.getNode(0));
 		endExpression(prec);
     }
 	
     /** Visit the specified bitwise negation expression node. */
     public void visitBitwiseNegationExpression(GNode n) {
 		int prec = startExpression(150);
 		printer.p('~').p(n.getNode(0));
 		endExpression(prec);
     }
 	
     /** Visit the specified address expression node. */
     public void visitAddressExpression(GNode n) {
 		int   prec = startExpression(150);
 		GNode e    = n.getGeneric(0);
 		if (e.hasName("AddressExpression") ||
 			e.hasName("LabelAddressExpression")) {
 			printer.p("&(").p(n.getNode(0)).p(')');
 		} else {
 			printer.p('&').p(n.getNode(0));
 		}
 		endExpression(prec);
     }
 	
     /** Visit the specified label address expression node. */
     public void visitLabelAddressExpression(GNode n) {
 		int prec = startExpression(150);
 		printer.p("&&").p(n.getString(0));
 		endExpression(prec);
     }
 	
     /** Visit the specified indirection expression node. */
     public void visitIndirectionExpression(GNode n) {
 		int prec = startExpression(150);
 		printer.p('*').p(n.getNode(0));
 		endExpression(prec);
     }
 	
     /** Visit the specified preincrement expression node. */
     public void visitPreincrementExpression(GNode n) {
 		int prec = startExpression(150);
 		printer.p("++").p(n.getNode(0));
 		endExpression(prec);
     }
 	
     /** Visit the specified predecrement expression node. */
     public void visitPredecrementExpression(GNode n) {
 		int prec = startExpression(150);
 		printer.p("--").p(n.getNode(0));
 		endExpression(prec);
     }
 	
     /** Visit the extension expression node. */
     public void visitExtensionExpression(GNode n) {
 		int prec = startExpression(150);
 		printer.p("__extension__ ").p(n.getNode(0));
 		endExpression(prec);
     }
 	
     /** Visit the specified subscript expression node. */
     public void visitSubscriptExpression(GNode n) {
 		int prec1  = startExpression(160);
 		int prec2  = enterContext(PREC_BASE);
 		
 		
 		printer.p("(*").p(n.getNode(0)).p(")");
 		printer.p('[').p(n.getNode(1)).p(']');
         
 		
 		exitContext(prec2);
 		endExpression(prec1);
     }
 	
     /** Visit the specified direct component selection node. */
     public void visitDirectComponentSelection(GNode n) {
 		int prec = startExpression(160);
 		printer.p(n.getNode(0)).p('.').p(n.getString(1));
 		endExpression(prec);
     }
 	
     /** Visit the specified indirect component selection node. */
     public void visitIndirectComponentSelection(GNode n) {
 		int prec = startExpression(160);
 		printer.p(n.getNode(0)).p("->").p(n.getString(1));
 		endExpression(prec);
     }
 	
     /** Visit the specified function call node. */
     public void visitFunctionCall(GNode n) {
 		int  prec = startExpression(160);
 		printer.p(n.getNode(0)).p('(').p(n.getNode(1)).p(')');
 		endExpression(prec);
     }
 	
     /** Visit the specified expression list node. */
     public void visitExpressionList(GNode n) {
 		for (Iterator<?> iter = n.iterator(); iter.hasNext(); ) {
 			int prec = enterContext(PREC_LIST);
 			printer.p((Node)iter.next());
 			exitContext(prec);
 			if (iter.hasNext()) {
 				printer.p(", ");
 			}
 		}
     }
 	
     /** Visit the specified postincrement expression node. */
     public void visitPostincrementExpression(GNode n) {
 		int prec = startExpression(160);
 		printer.p(n.getNode(0)).p("++");
 		endExpression(prec);
     }
 	
     /** Visit the specified postdecrement expression node. */
     public void visitPostdecrementExpression(GNode n) {
 		int prec = startExpression(160);
 		printer.p(n.getNode(0)).p("--");
 		endExpression(prec);
     }
 	
     /** Visit the specified compound literal node. */
     public void visitCompoundLiteral(GNode n) {
 		int prec = startExpression(160);
 		printer.p('(').p(n.getNode(0)).p(") ").p(n.getNode(1));
 		endExpression(prec);
     }
 	
     /** Visit the specified primary identifier node. */
     public void visitPrimaryIdentifier(GNode n) {
 		
 		// HACK: Paranoid checks because PrimaryIdentiier(null) 's are 
 		// getting added to tree and I don't have time to figure out where
 		if( null != n && null != n.get(0) ) {
 			
 			
 			GNode dl = clp.getDataLayout(className);
 			
 			
 			boolean isField = clp.findPrimID(dl,n.getString(0));
 			if(isField && !isConstructor) printer.p("__this->");
 			else if( n.getString(0).equals("R1") || n.getString(0).equals("R2") || n.getString(0).equals("R3") || n.getString(0).equals("R4") ) {
 				printer.p("__").p(className).p("::");
 			}
 			
 			// For all variables, check not null?
 			// NO - std::cout and std::end lis a primary identifier....
 			// Also, in TypedefDeclaration, but I think we overrode?
 			
 			
 			// do normal stuff
 			int prec = startExpression(160);
 			printer.p(n.getString(0));
 			endExpression(prec);
 			
 		}
 		
 		
 		
     }
 	
     /** Visit the specified statement as exprression node. */
     public void visitStatementAsExpression(GNode n) {
 		int     prec       = enterContext(PREC_BASE);
 		boolean stmtAsExpr = isStmtAsExpr;
 		isStmtAsExpr       = true;
 		prepareNested();
 		printer.p(n.getNode(0));
 		isStmtAsExpr       = stmtAsExpr;
 		exitContext(prec);
     }
 	
     /** Visit the specified variable argument access node. */
     public void visitVariableArgumentAccess(GNode n) {
 		int prec  = startExpression(160);
 		printer.p("__builtin_va_arg(");
 		int prec2 = enterContext(PREC_LIST);
 		printer.p(n.getNode(0));
 		exitContext(prec2);
 		printer.p(", ").p(n.getNode(1)).p(')');
 		endExpression(prec);
     }
 	
     /** Visit the specified floating constant node. */
     public void visitFloatingConstant(GNode n) {
 		int prec = startExpression(160);
 		printer.p(n.getString(0));
 		endExpression(prec);
     }
 	
     /** Visit the specified integer constant node. */
     public void visitIntegerConstant(GNode n) {
 		int prec = startExpression(160);
 		printer.p(n.getString(0));
 		endExpression(prec);
     }
 	
     /** Visit the specified character constant node. */
     public void visitCharacterConstant(GNode n) {
 		int prec = startExpression(160);
 		printer.p(n.getString(0));
 		endExpression(prec);
     }
 	
     /** Visit the specified string constant node. */
     public void visitStringConstant(GNode n) {
 		int prec = startExpression(160);
 		
 		if (1 == n.size()) {
 			printer.p(n.getString(0));
 			
 		} else {
 			int align = printer.column();
 			for (Iterator<?> iter = n.iterator(); iter.hasNext(); ) {
 				printer.p(Token.cast(iter.next()));
 				if (iter.hasNext()) {
 					printer.pln().align(align);
 				}
 			}
 		}
 		
 		endExpression(prec);
     }
 	
     public void visitHeaderDeclaration(GNode n) {
 		//	    printer.pln("#pragma once"); // once we separate .h file
 		printer.pln("#include <iostream>");
 		printer.pln("#include \"ptr.h\"");
 		printer.pln("#include \"java_lang.h\"").pln();
 		printer.pln("namespace java {");
 		printer.incr();
 		printer.indent().pln("namespace lang {");
 		printer.incr();
 		
 		for(Object o : n ) if( o instanceof GNode ) printer.p((GNode)o);
 		
 		String cn = n.getNode(0).getNode(0).getNode(0).getNode(0).getNode(1).getString(0);
 		
 		// Add destructor to namespace java::lang
 		printer.indent().pln("// The destructor");
 		printer.indent().p("void __").p(cn).p("::__delete(__").p(cn);
 		printer.p("* __this) {").pln();
 		printer.incr();
 		printer.indent().pln("delete __this;");
 		printer.decr();
 		printer.indent().pln("}");
 		
 		printer.pln();
 		
 		// Before exiting namespace java::lang, write constructors for
 		// Class and Class_VT
 		
 		// If ConstructorDeclarations node is empty.  
 		// Otherwise it has already been done.
 		if(n.getNode(4).hasName("ConstructorDeclarations") &&
 		   n.getNode(4).isEmpty()) {
 			
 			printer.indent().pln("// Empty constructors for class and vt");
 			
 			printer.incr().indent();
 			printer.p("__").p(cn).p("::__").p(cn);
 			printer.p("() :  __vptr(&__vtable) {").pln();
 			printer.indent().p("}").pln();
 			
 		}
 		
 		printer.pln();
 		
 		printer.indent().p("__").p(cn).p("_VT __").p(cn);
 		printer.p("::__vtable;").pln();
 		
 		printer.decr();
 		
 		printer.decr();
 		printer.indent().pln("}");
 		printer.decr();
 		printer.pln("}").pln();
 		
     }
 	
     public void visitImplementationDeclaration(GNode n) {
 		for(Object o : n ) if( o instanceof GNode ) printer.p((GNode)o);
     }
 	
     // FIXME: global variable to selectively ->data Strings.  
     // not needed in future versions
     public static boolean is_output = false;
 	
     public void visitStreamOutputList(GNode n) {
 		for(Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
 			GNode nxt = (GNode)iter.next();
 		    
 			printer.p(nxt);
 			
 			printer.incr();
 			if(iter.hasNext()) printer.pln().indent().p( " << " );
 			printer.decr();
 		}
 		// TODO: Add logic for concatenation vs addition
 		// This is operator method overloading!
 		// Currently some +'s are incorrect
 		
 		is_output = false;
     }
 	
 	
 	
     public void visitVirtualMethodDeclaration(GNode n) {
 		// TODO: Make it prettier!
 		
 		if( ! "main".equals(n.getString(1))) {
 			printer.indent();
 			// Return Type
 			if(null != n.getNode(0)) {
 				printer.p(n.getNode(0));
 			}
 			
 			// method name
 			printer.p(" (*").p(n.getString(1)).p(")");
 			
 			
 			// Formal Parameters
 			
 			// Case __delete, pass raw pointer
 			if("__delete".equals(n.getString(1))) {
 				// void (*__delete)(__CLASS*);
 				printer.p("(__").p(n.getNode(2)).p("*)");
 			}
 			else if(null != n.getNode(2)) {
 				GNode fps = n.cast(n.getNode(2));
 				printer.p("(");
 				
 				for(Iterator<Object> iter = fps.iterator(); iter.hasNext();)
 				{
 					// Only print Identifier, variable name as well?
 					printer.p( ((GNode)iter.next()).getNode(0) );
 					if(iter.hasNext() )printer.p( ", " );
 				}
 				printer.p(")");
 				
 			}
 			printer.pln(';');
 		}
     }
 	
 	
     // KEEP
     public void visitvtMethodPointersList( GNode n ) {
 		printer.incr().indent().pln();
 		for(Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
 			// ROB: Pls remove main method
 			// add delete for mem mgmt
 			
 			printer.indent().p((GNode)iter.next());
 			if(iter.hasNext())printer.p( ", " ).pln();
 			
 		}
 		printer.decr();
     }
 	
     public void visitvtMethodPointer( GNode n ) {
         
 		printer.p(n.getString(0)); // print methodName
 		
 		if("__delete".equals(n.getString(0))) {
 			// FIXME: Hacky, but working delete method
 			printer.p("((").p(n.getNode(3).getNode(0)).p("(*)");
 			// to get __Demo* not just Demo
 			printer.p("(__").p(n.getNode(3).getNode(1)).p("*))");
 			printer.p("&__Object::").p(n.getString(0)).p(")"); 
 		}
 		else {
 			printer.p("(");
 			if(n.size() == 4 ) printer.p(n.getNode(3));
 			printer.p("&").p(n.getNode(1)).p("::");
 			printer.p(n.getString(0)).p(")");
 		}
 		
     }
 	
     public void visitMethodPointersList( GNode n )  {
 		// FIXME: Now it's not used?
 		// oh well, leave well enough alone.  it works!
 		printer.incr().indent().pln();
 		for(Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
 			printer.indent().p((GNode)iter.next());
 			if(iter.hasNext())printer.p( ", " ).pln();
 			
 		}
 		printer.decr();
     }
 	
     public void visitMethodPointer( GNode n ) {
 		// FIXME: Not used now? SO CONFUSEd
 		// oh well, leave them named vt
 		
 		printer.p(n.getString(0)); // print methodName
 		
 	    
 		
     }
 	
     public void visitMethodHeaderList( GNode n ) {
 		printer.pln();
 		for(Object o : n ) if( o instanceof GNode ) printer.p((GNode)o);
 		
     }
 	
     public void visitStaticMethodHeader(GNode n) {
 		// FIXME: Only print current Class as parameter, if inherited
 		if(! "main".equals(n.getString(1))) {
 			printer.indent().p("static ").p(n.getNode(0));
 			printer.p(' ').p(n.getString(1));
 			printer.p('(').p(n.getNode(2)).p(");").pln();	
 		}
     }
 	
     public void visitConstructorHeaderList(GNode n) {
 		
 		printer.pln();
 		// Print default constructor if none was declared
 		if(n.size() == 1) {
 			printer.indent().p("// Constructor").pln();
 			printer.indent().p("__").p(n.getString(0)).p("();").pln();
 		}
 		else { 
 			for(Object o : n ) if( o instanceof GNode ) printer.p((GNode)o);
 		}
 		
 		printer.indent().pln("// Destructor");
 		printer.indent().p("static void __delete(__");
 		printer.p(n.getString(0)).p("*);");
 		
 		if(n.size() > 0) printer.pln();
 		
     }
 	
     public void visitConstructorHeader(GNode n) {
 		printer.indent().p("// Constructor").pln();
 		printer.indent().p("__").p(n.getString(0));
 		printer.p('(').p(n.getNode(1)).p(");").pln();	
     }
 	
     public void visitDataFieldList( GNode n ) {
 		if(n.size() > 0) printer.pln();
 		for(Object o : n ) if( o instanceof GNode ) printer.p((GNode)o);
     }
 	
 	
 	
     public void visitClassISAPointer( GNode n ) {
 		// FIXME: Should always call Object's class?
 		printer.p(" __isa(__Object::__class())");
 		
 		//		printer.p("__isa(").p(n.getString(0)).p("::__class())");
     }
 	
     public void visitVTConstructorDeclaration(GNode n) { 
 		printer.indent().p(n.getNode(0)).pln();
 		
 		if (null != n.get(1)) printer.indent().p(n.getNode(1));
 		
 		
 		printer.indent().p("__").p(className).p("_VT()");
 		//	    printer.indent().p(n.getString(2)).p("() ");
 		
 		if(null != n.get(4)) {
 			// Print out MethodPointerList
 			printer.p(": ");
 			printer.p(n.getNode(4));
 		}
 		
 		isOpenLine = true;
 		printer.p(n.getNode(5)).pln();
 		
     }
 	
 	public void visitStaticVarsList(GNode n ) {
	    for( Object o : n ) printer.indent().p((GNode)o).p(";").pln();
 	}
 	
     public void visitPointerCast(GNode n) {
 		if(n.size() > 0 ) {
 			printer.p('(').p(n.getNode(0)).p("(*)(").p(n.getNode(1)).p("))");
 		}
     }
     
     //----------------------------------------------------------
     //                   END NEW METHODS
     //----------------------------------------------------------
     
     // ------------------------------------------------------
     // ------ Begin via JavaPrinter.  Thanks Grimm! ---------
     // ------------------------------------------------------
 	
     
     // Hacking, don't want __this in the Constructor
     public void visitConstructorDeclarations(GNode n) {
 		if(n.size() > 0) {
 			for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
 				printer.p((Node)iter.next());
 			}
 		}
 		else {
 			// Print the default empty constructor
 		}
     }
 	
 	
     // Hacking it, don't want __this in Constructor
     boolean isConstructor = false;
     /** Visit the specified constructor declaration. */
     public void visitConstructorDeclaration(GNode n) { 
 		isConstructor = true;
 		
 		// Don't have to put initialization into header
 		
 		printer.pln();
 		printer.indent().pln("// Constructor");
 		printer.indent();
 		// Ignore modifiers
 		//	printer.indent().p(n.getNode(0));
 		//	if (null != n.get(1))  printer.p(n.getNode(1));
 		
 		printer.p("__").p(n.getString(2)).p("::").p("__").p(n.getString(2));
 		printer.p("(").p(n.getNode(3)).p(")");
 		printer.p(" : __vptr(&__vtable) ");
 		
 		// What's in node 4!!!
 		if(null != n.get(4)) {
 			printer.p(n.getNode(4));
 		}
 		isOpenLine = true;
 		printer.p(n.getNode(5));
 		
 		printer.pln();
 		
 		isConstructor = false;
     }
     
     /** Visit the specified instance of expression. */
     public void visitInstanceOfExpression(GNode n) {
 		final int prec1 = startExpression(90);
 		printer.p(n.getNode(0)).p(' ').p("instanceof").p(' ');
 		final int prec2 = enterContext();
 		printer.p(n.getNode(1));
 		exitContext(prec2);
 		endExpression(prec1);
     }
     
     /** Visit the specified basic cast expression. */
     
     public void visitBasicCastExpression(GNode n) {
 		
 		final int prec = startExpression(140);
 		printer.p('(').p(n.getNode(0));
 		if(null != n.get(1)) {
 			printer.p(n.getNode(1));
 		}
 		printer.p(')').p(n.getNode(2));  
 		
 		endExpression(prec);
 		
     }
     
     
     /** Visit the specified new array expression. */
     public void visitNewArrayExpression(GNode n) {
 		final int prec = startExpression(160);
 		
 		printer.p(n.getNode(1)).p(n.getNode(2));
 		if (null != n.get(3)) printer.p(' ').p(n.getNode(3));
 		endExpression(prec);
     }
     
     public void visitConcreteDimensions(GNode n) {
 		printer.p("(");
 		
 		for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
 			printer.p((Node)iter.next());
 			if (iter.hasNext()) printer.p(", ");
 			printer.fit();
 		}
 		
 		printer.p(")");
     }
 	
 	
     // from Java Printer
     /** Visit the specified array initlizer. */
     public void visitArrayInitializer(GNode n) {
 		
 		if (! n.isEmpty()) {
 			printer.p('{');
 			for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
 				printer.buffer().p((Node)iter.next());
 				if (iter.hasNext()) printer.p(", ");
 				printer.fit();
 			}
 			printer.p('}');
 		} else {
 			printer.p("{ }");
 		}
     }
 	
 	
     /** Visit the specified try catch finally statement. */
     public void visitTryCatchFinallyStatement(GNode n) {
 		final boolean nested = startStatement(STMT_ANY, n);
 		
 		printer.indent().p("try");
 		if (null != n.get(0)) printer.p(" (").p(n.getNode(0)).p(')');
 		
 		isOpenLine = true;
 		printer.p(n.getNode(1)).p(' ');
 		
 		final Iterator<Object> iter = n.iterator();
 		iter.next(); // Skip resource specification.
 		iter.next(); // Skip try block.
 		while (iter.hasNext()) {
 			final GNode clause = GNode.cast(iter.next());
 			
 			isOpenLine = true;
 			if (iter.hasNext()) {
 				printer.p(clause).p(' ');
 			} else if (null != clause) {
 				printer.p("finally").p(clause);
 			}
 		}
 		
 		endStatement(nested);
     }
 	
     /** Visit the specified throws clause. */
     public void visitThrowsClause(GNode n) {
 		printer.p("throw (");
 		for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
 			printer.p("__").p((Node)iter.next());
 			if (iter.hasNext()) printer.p(", ");
 			printer.p(")");
 		}
     }
     
 	
     // via Java Printer
     /** Visit the specified throw statement. */
     public void visitThrowStatement(GNode n) {
 		final boolean nested = startStatement(STMT_ANY, n);
 		
 		printer.indent().p("throw").p(' ').p(n.getNode(0)).p("()");
 		printer.pln(';');
 		endStatement(nested);
 		isOpenLine = false;
     }
 	
     // via Java Printer
     /** Visit the specified catch clause. */
     public void visitCatchClause(GNode n) {
 		printer.p("catch (").p(n.getNode(0)).p(")").p(n.getNode(1));
     }
 	
 	
     // via Java Printer
     /** Visit the specified do while statement. */
     public void visitDoWhileStatement(GNode n) {
 		final boolean nested = startStatement(STMT_ANY, n);
 		printer.indent().p("do");
 		prepareNested();
 		printer.p(n.getNode(0));
 		if (isOpenLine) {
 			printer.p(' ');
 		} else {
 			printer.indent();
 		}
 		printer.p("while (").p(n.getNode(1)).pln(");");
 		endStatement(nested);
 		isOpenLine = false;
     }
     
 	
 	
     public void visitMethodDeclaration(GNode n) {
 		
 		printer.pln();
 		
 		// Is main method?
 		if("main".equals(n.getString(3))) {
 			printer.p("int32_t main()");
 			// FIXME: For main, detect if any command line args
 		}
 		else {
 			//	    printer.indent().p(n.getNode(0)); // Modifiers
 			if (null != n.get(1)) printer.p(n.getNode(1)).p("1=");
 			
 			// Node 2 = return type
 			printer.p(n.getNode(2)).p(" ");
 			
 			if (! "<init>".equals(n.get(3))) {
 				// Node 3 = method Name
 				printer.p("__").p(className).p("::").p(n.getString(3)).p(' ');
 			}
 			
 			// Formal Parameters
 			printer.p("(").p(n.getNode(4)).p(")");
 		}
 		
 		if (null != n.get(5)) {
 			printer.p(" 5=").p(n.getNode(5));
 		}
 		if (null != n.get(6)) {
 			// TODO: Exceptions
 			printer.p(n.getNode(6));
 		}
 		if (null != n.get(7)) {
 			// Block
 			isOpenLine = true;
 			printer.p(n.getNode(7)).pln();
 		} else {
 			printer.pln(';');
 		}
 		isOpenLine = false;
     }
     
     public void visitModifiers(GNode n) {
 		if(n.size() > 0) for (Object o : n) printer.p((Node)o).p(' ');
     }
 	
     /** Visit the specified modifier. */
     public void visitModifier(GNode n) {
 		if( n.getString(0).equals("final") || n.getString(0).equals("public") ) return; //don't print final or public modifiers
 		printer.p(n.getString(0));
     }
 	
     public void visitDeclarator(GNode n) {
 		//---------------------------Handle Casts
 		//dynamic casting
 		if(null != n.get(2)) {
 			
 			// OMG, FIXME! and do a semi-colon test in Declarator
 			
 			if((n.getNode(2).hasName("CastExpression"))) {
 				printer.p(n.getString(0));
 				printer.p(" = __rt::java_cast<");
 				printer.p(n.getNode(2).getNode(0).getNode(0).getString(0));
 				printer.p( ">(");
 				printer.p(n.getNode(2).getNode(1).getString(0));
 				printer.p(");").pln();
 			}
 		}
 		
 		//--------------------------End Cast Handling
 		
 		if(null != n.get(2)) 
 	    {
 			if ( n.getNode(2).hasName("ArrayInitializer") ||
 				n.getNode(2).hasName("NewArrayExpression") ) {
 			    
 				// suppress var name
 			}
 			else if( n.getNode(2).hasName("CastExpression")) {
 				printer.p("//");
 			}
 			else printer.p(n.getString(0)); // var name
 	    }
 		else printer.p(n.getString(0)); // var name
 	    
 		if(null != n.get(1)) {
 			if (Token.test(n.get(1))) {
 				formatDimensions(n.getString(1).length());
 			} else {
 				printer.p(n.getNode(1));
 			}
 		}
 	    
 		if(null != n.get(2)) {
 			
 			if(n.getNode(2).hasName("ArrayInitializer") ||
 			   n.getNode(2).hasName("NewArrayExpression")) {
 				printer.p(n.getNode(2));
 			}
 			else if((n.getNode(2).hasName("CastExpression"))) {
 				//suppress assignment - handled by dynamic cast
 			}
 			else { printer.p(" = ").p(n.getNode(2)); }
 		}
 	    
     }
     
     protected void formatDimensions(final int n) {
 		for (int i=0; i<n; i++) printer.p("[]");
     }
     
     public void visitDeclarators(GNode n) {
 		for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
 			printer.p((Node)iter.next());
 			if (iter.hasNext()) printer.p(", ");
 		}
     }
 	
     public void visitFieldDeclaration(GNode n) {
 		
 		// Look for NewClassExpression to implement memroy mgmt
 		
 		// Look ahead for ArrayInitializer or NewArrayExpression translation
 		GNode declarationType = 
 	    GNode.cast(n.getNode(2).getNode(0).getNode(2));
 		
 		if(null != declarationType && (declarationType.hasName("ArrayInitializer") || declarationType.hasName("NewArrayExpression") ) )
 	    {
 			// Ugly, but there's no other way to get Type later on, as
 			// Nodes are all generic
 			// Looks ugly, but works for all dimensions!
 			
 			// Begin mem mgmt:
 			// ? Do you only mem mgmt the exterior array?
 			printer.indent().p("__rt::Ptr<");
 			
 			// Begin Array declaration, may be nested
 			int dim = n.getNode(1).getNode(1).size();
 			for(int i = 0; i < dim; i++) {
 				printer.p("__rt::Array<");
 			}
 			printer.p(n.getNode(1).getNode(0));
 			for(int i = 0; i < dim; i++) {
 				printer.p(" >");
 			}
 			// end Array declaration
 			
 			// end mem mgmt
 			printer.p(" >");
 			printer.p(" ").p(n.getNode(2).getNode(0).getString(0));
 			printer.p(" = ");
 			printer.p("new ");
 			
 			// Begin Array declaration, may be nested
 			for(int i = 0; i < dim; i++) {
 				printer.p("__rt::Array<");
 			}
 			// Print type again
 			printer.p(n.getNode(1).getNode(0));
 			for(int i = 0; i < dim; i++) {
 				printer.p(" >");
 			}
 			// end Array declaration
 	    }
 		
 		if(null != declarationType && (declarationType.hasName("ArrayInitializer") || declarationType.hasName("NewArrayExpression") ) ) { /** don't print type here*/	}
 		else printer.indent().p(n.getNode(0)).p(n.getNode(1)).p(" ");
 		
 		// var name suppression is done in visitDeclarator
 		printer.p(n.getNode(2)).p(';').pln();
 		
 		
 		isDeclaration = true;
 		isOpenLine    = false;
     }
 	
 
     public void visitUnaryExpression(GNode n) {
 	for( Object o : n) {
 	    if (o instanceof Node) printer.p(GNode.cast(o));
 	    else if(o instanceof String) printer.p(o.toString());
 	}
     }
 
 
     public void visitBlockDeclaration(GNode n) {
 		printer.indent();
 		if (null != n.get(0)) {
 			printer.p(n.getString(0));
 			isOpenLine = true;
 		} 
 		printer.p(n.getNode(1)).pln();
 		isOpenLine = false;
     }
 	
     public void visitBlock(GNode n) {
 		if (isOpenLine) {
 			printer.p(' ');
 		} else {
 			printer.indent();
 		}
 		printer.pln('{').incr();
 		
 		isOpenLine    = false;
 		isNested      = false;
 		isIfElse      = false;
 		isDeclaration = false;
 		isStatement   = false;
 		
 		printDeclsAndStmts(n);
 		
 		printer.decr().indent().p('}');
 		isOpenLine    = true;
 		isNested      = false;
 		isIfElse      = false;
     }
 	
 	
 	
     /** Visit the specified call expression. */
     public void visitCallExpression(GNode n) {
 	    
 		// TODO: Modify AST to include CallingClass node
 		final int prec = startExpression(160);
 	    
 		// Callling instance
 		if (null == n.getNode(0)) 
 			printer.p("__this");	    
 		else if (n.getNode(0).hasName("ThisExpression")) 
 			printer.p("__this");
 		else printer.p(n.getNode(0));
 		
 		// method name
 		printer.p("->__vptr->").p(n.getString(2));
 		
 		// arguments
 		// FIXME: Know when to pass self, etc. as arguments
 		if(n.getNode(3).size() > 0) printer.p(n.getNode(3));
 		else if ("toString".equals(n.getString(2)) ||
 				 "getClass".equals(n.getString(2))) 
 			printer.p("(").p(n.getNode(0)).p(")");
 		else printer.p("()");
 		
 		endExpression(prec);
     }
 	
     public void visitClassLiteralExpression(GNode n) {
 		// TODO: Does CPP use Class Literal Expressions?
 		// If so, we must implement here without visitng nodes to 
 		// eliminate the extra space. ex: Node .class
 		final int prec = startExpression(160);
 		printer.p(n.getNode(0)).p(".class");
 		endExpression(prec);
     }
 	
     /** Visit the specified this expression. */
     public void visitThisExpression(GNode n) {
 		
 		// TODO: How to get calling class?
 		final int prec = startExpression(160);
 		// WTF would be there?
 		if (null != n.get(0)) printer.p(n.getNode(0)).p('.');
 		printer.p("__this");
 		endExpression(prec);
     }
 	
     /** Visit the specified super expression. */
     public void visitSuperExpression(GNode n) {
 		// TODO: In LeafTransplant, replace SuperExpression node
 		// ( located at CallExpression.getNode(0) ) with a PrimaryIdentifier
 		// node using a call to the vtable isa
 		// This method should become defunct
 		final int prec = startExpression(160);
 		if (null != n.get(0)) printer.p(n.getNode(0)).p('.');
 		printer.p("super");
 		endExpression(prec);
     }
 	
     /** Visit the specified type. */
     public void visitType(GNode n) {
 		
 		printer.p(n.getNode(0));
 		if (null != n.get(1)) {
 			if (Token.test(n.get(1))) {
 				formatDimensions(n.getString(1).length());
 			} else {
 				printer.p(n.getNode(1));
 			}
 		}
 		//    printer.p(' ');
     }
     
     public void visitInstantiatedType(GNode n) {
 		boolean first = true;
 		for (Object o : n) {
 			if (first) first = false;
 			else printer.p('.');
 			printer.p((Node)o);
 		}
     }
 	
     /** Visit the specified type instantiation. */
     public void visitTypeInstantiation(GNode n) {
 		printer.p(n.getString(0)).p(n.getNode(1));
     }
 	
     /** Visit the specified type parameters. */
     public void visitTypeParameters(GNode n) {
 		printer.p('<');
 		for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
 			printer.p((Node)iter.next());
 			if (iter.hasNext()) printer.p(", ");
 		}
 		printer.p('>');
     }
 	
     /** Visit the specified type parameter. */
     public void visitTypeParameter(GNode n) {
 		printer.p(n.getString(0));
 		if (null != n.get(1)) printer.p(" extends ").p(n.getNode(1));
     }
 	
     protected boolean isLongDeclarationJava(GNode decl) {
 		return (decl.hasName("ConstructorDeclaration") ||
 				decl.hasName("ClassDeclaration") ||
 				decl.hasName("InterfaceDeclaration") ||
 				decl.hasName("AnnotationDeclaration") ||
 				decl.hasName("EnumDeclaration") ||
 				decl.hasName("BlockDeclaration") ||
 				(decl.hasName("MethodDeclaration") &&
 				 (null != decl.get(7))) ||
 				(decl.hasName("FieldDeclaration") &&
 				 containsLongExpression(decl)) ||
 				(decl.hasName("AnnotationMethod") &&
 				 containsLongExpression(decl)));
     }
 	
     /**
      * Print the specified node's children as declarations and/or
      * statements.
      *
      * @param n The node.
      */
     protected void printDeclsAndStmts(GNode n) {
 		isOpenLine     = false;
 		isNested       = false;
 		isIfElse       = false;
 		isDeclaration  = false;
 		isStatement    = false;
 		GNode previous = null;
 		
 		for (Object o : n) {
 			final Node  node    = (Node)o;
 			if (null == node) continue;
 			final GNode current = GNode.cast(node);
 			
 			// If there was a previous node and the previous node was a
 			// block or long declaration, the current node is a block or
 			// long declaration, or the previous node was a statement and
 			// the current node is a declaration, then print an extra
 			// newline.
 			if ((null != previous) &&
 				(previous.hasName("Block") ||
 				 (isLongDeclarationJava(previous) &&
 				  current.getName().endsWith("Declaration")) ||
 				 current.hasName("Block") ||
 				 isLongDeclarationJava(current) ||
 				 (! previous.getName().endsWith("Declaration") &&
 				  current.getName().endsWith("Declaration")))) {
 					 printer.pln();
 				 }
 			
 			printer.p(node);
 			
 			if (isOpenLine) printer.pln();
 			isOpenLine = false;
 			previous   = current;
 		}
     }
 	
     public void visitVoidType(GNode n) {
 		printer.p("void");
     }
 	
     public void visitFormalParameters(GNode n) {
 		// Where are the parentheses getting printed?
 		//	    if(n.size() == 0) printer.p("()");
 		
 		for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
 			printer.p((Node)iter.next());
 			if (iter.hasNext()) printer.p(", ");
 		}
 		
     }
 	
     public void visitFormalParameter(GNode n) {
 		
 		// WTF is this black magic?
 		final int size = n.size();
 		printer.p(n.getNode(0)).p(n.getNode(1));
 		for (int i=2; i<size-3; i++) { // Print multiple catch types.
 			printer.p(" | ").p(n.getNode(i));
 		}
 		if (null != n.get(size-3)) printer.p(n.getString(size-3));
 		printer.p(' ').p(n.getString(size-2)).p(n.getNode(size-1));
     }
 	
 	
     // Original Grimm method
     public void visitQualifiedIdentifier(GNode n) {
 		final int prec = startExpression(160);
 		
 		if (1 == n.size()) {
 			printer.p(n.getString(0));
 		} else {
 			for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
 				printer.p(Token.cast(iter.next()));
 				if (iter.hasNext()) printer.p('.');
 			}
 		}
 		
 		endExpression(prec);
     }
 	
 	
     public void visitDimensions(GNode n) {
 		if(n.size() <= 1) ; // One dimension array, do nothing
 		else if(n.size() > 1) {
 			// FIXME: 
 			for (int i=0; i<n.size(); i++) printer.p("[]");
 		}
     }
 	
     protected boolean containsLongExpression(GNode n) {
 		return (Boolean)containsLongExprVisitor.dispatch(n);
     }
 	
     public void visitSelectionExpression(GNode n) {
 		// Grim prints rt:: and std:: elsewhere? Can't find it in CPPPrinter
 		// FUCK - where is rt? Needs translate to __rt
 		// If it's a method, neeed ->__vptr
 		// If it's a data field, only instance->field
 		
 		// This is a data field, don't need to access __vptr
 		final int prec = startExpression(160);
 		printer.p(n.getNode(0)).p("->").p(n.getString(1));
 		
 		endExpression(prec);
     }
 	
     public void visitArguments(GNode n) {
 		printer.p('(');
 		for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
 			final int prec = enterContext(PREC_LIST);
 			printer.p((Node)iter.next());
 			exitContext(prec);
 			if (iter.hasNext()) printer.p(", ");
 		}
 		printer.p(')');
     }
 	
     public void visitConditionalStatement(GNode n) {
 		final int     flag   = null == n.get(2) ? STMT_IF : STMT_IF_ELSE;
 		final boolean nested = startStatement(flag, n);
 		
 		if (isIfElse) {
 			printer.p(' ');
 		} else {
 			printer.indent();
 		}
 		printer.p("if (").p(n.getNode(0)).p(')');
 		prepareNested();
 		printer.p(n.getNode(1));
 		if (null != n.get(2)) {
 			if (isOpenLine) {
 				printer.p(" else");
 			} else {
 				printer.indent().p("else");
 			}
 			prepareNested();
 			boolean ifElse = isIfElse;
 			isIfElse       = true;
 			printer.p(n.getNode(2));
 			isIfElse       = ifElse;
 		}
 		endStatement(nested);
     }
 	
     public void visitIntegerLiteral(GNode n) {
 		final int prec = startExpression(160);
 		printer.p(n.getString(0));
 		endExpression(prec);
     }
 	
     /** Visit the specified floating point literal. */
     public void visitFloatingPointLiteral(GNode n) {
 		final int prec = startExpression(160);
 		printer.p(n.getString(0));
 		endExpression(prec);
     }
 	
     /** Visit the specified character literal. */
     public void visitCharacterLiteral(GNode n) {
 		final int prec = startExpression(160);
 		printer.p(n.getString(0));
 		endExpression(prec);
     }
 	
     /** Visit the specified string literal. */
     public void visitStringLiteral(GNode n) {
 		final int prec = startExpression(160);
 		printer.p("__rt::literal(").p(n.getString(0)).p(")");
 		//		if(is_output) printer.p("->data");
 		endExpression(prec);
     }
 	
     /** Visit the specified boolean literal. */
     public void visitBooleanLiteral(GNode n) {
 		final int prec = startExpression(160);
 		printer.p(n.getString(0));
 		endExpression(prec);
     }
 	
     /** Visit the specified null literal. */
     public void visitNullLiteral(GNode n) {
 		// 
 		final int prec = startExpression(160);
 		printer.p("__rt::null()");
 		endExpression(prec);
     }
 	
     // TODO DIANA: change to cpp types
     public void visitPrimitiveType(GNode n) {
 		String primType = n.getString(0);
 		if("int".equals(primType))
 			primType = "int32_t";
 		else if("boolean".equals(primType))
 			primType = "bool";
 	    
 		printer.p(primType);
     } 
 	
     public void visitPostfixExpression(GNode n) {
 		final int prec = startExpression(160);
 		printer.p(n.getNode(0)).p(n.getString(1));
 		endExpression(prec);
     }
 	
     /** Visit the specified expression. */
     public void visitExpression(GNode n) {
 		
 		if(n.getNode(2).hasName("PrimaryIdentifier")) {
 			
 			for( Object o : n) {
 				if (o instanceof Node) {
 					new Visitor() {
 						public void visitSubscriptExpression(GNode n) {
 							printer.indent().p("// __rt::checkStore(");
 							printer.p(n.getNode(0)).p(",");
 							printer.p(n.getNode(1)).p(");").pln();
 							
 						}
 						
 						public void visit(GNode n) {
 							for( Object o : n) {
 								if (o instanceof Node) dispatch((GNode)o);
 							}
 						}
 						
 					}.dispatch(GNode.cast(o));
 					
 				} // end if
 			} // end for loop
 			// FIXME: Is this in the right place?
 			//		     printer.p(n.getNode(2)).p(");").pln();
 		}
 		
 		final int prec1 = startExpression(10);
 		final int prec2 = enterContext();
 		printer.p(n.getNode(0));
 		exitContext(prec2);
 		
 		printer.p(' ').p(n.getString(1)).p(' ').p(n.getNode(2));
 		endExpression(prec1);
     }
 	
     public Object unableToVisit(Node node) {
 		System.out.println( "Could not visit node of type: " + node.getName() );
 		for( Object o : node ) if ( o instanceof GNode ) printer.p((GNode)o);
 		GNode returned = GNode.create("EmptyStatement");
 		return returned;
     }
 	
     /** The actual implementation of {@link #containsLongExpression}. */
     @SuppressWarnings("unused")
     private static final Visitor containsLongExprVisitor = new Visitor() {
 	public Boolean visitBlock(GNode n) {
 	return Boolean.TRUE;
 }
 
 public Boolean visitArrayInitializer(GNode n) {
 return Boolean.TRUE;
 }
 
 public Boolean visit(GNode n) {
 for (Object o : n) {
 if ((o instanceof Node) && (Boolean)dispatch((Node)o)) {
 return Boolean.TRUE;
 }
 }
 return Boolean.FALSE;
 }
 };
 
 /** Visit the specified annotations. */
 public void visitAnnotations(GNode n) {
 for (Object o : n) printer.p((Node)o).p(' ');
 }
 
 /** Visit the specified annotation. */
 public void visitAnnotation(GNode n) {
 printer.p('@').p(n.getNode(0));
 if (null != n.get(1)) printer.p('(').p(n.getNode(1)).p(')');
 }
 
 /** Visit the specified bound. */
 public void visitBound(GNode n) {
 for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
 printer.p((Node)iter.next());
 if (iter.hasNext()) printer.p(" & ");
 }
 }
 
 
 
 
 // ---------------------------------------------------------------
 // ---------- End via JavaPrinter.  Thanks Grimm! ----------------
 // ----------------------------------------------------------------
 
 /** Visit the specified line marker. */
 public void visit(LineMarker mark) {
 if (isOpenLine) {
 isOpenLine = false;
 printer.pln();
 }
 if (lineUp) printer.lineUp(mark);
 
 printer.p("# ").p(mark.line).p(" \"").p(mark.file).p('\"');
 if (0 != (mark.flags & LineMarker.FLAG_START_FILE)) {
 printer.p(" 1");
 }
 if (0 != (mark.flags & LineMarker.FLAG_RETURN_TO_FILE)) {
 printer.p(" 2");
 }
 if (0 != (mark.flags & LineMarker.FLAG_SYSTEM_HEADER)) {
 printer.p(" 3");
 }
 if (0 != (mark.flags & LineMarker.FLAG_EXTERN_C)) {
 printer.p(" 4");
 }
 printer.pln().p(mark.getNode());
 }
 
 /** Visit the specified pragma. */
 public void visit(Pragma p) {
 if (isOpenLine) {
 isOpenLine = false;
 printer.pln();
 }
 if (lineUp) printer.lineUp(p);
 
 printer.p("#pragma ").pln(p.directive).p(p.getNode());
 }
 
 /** Visit the specified source identity marker. */
 public void visit(SourceIdentity ident) {
 if (isOpenLine) {
 isOpenLine = false;
 printer.pln();
 }
 if (lineUp) printer.lineUp(ident);
 
 printer.p("#ident \"").p(ident.ident).pln('"').p(ident.getNode());
 }
 
 }
