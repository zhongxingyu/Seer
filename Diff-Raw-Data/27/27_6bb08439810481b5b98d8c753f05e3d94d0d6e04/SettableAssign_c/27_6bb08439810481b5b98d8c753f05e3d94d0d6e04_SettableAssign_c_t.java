 /*
  *  This file is part of the X10 project (http://x10-lang.org).
  *
  *  This file is licensed to You under the Eclipse Public License (EPL);
  *  You may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *      http://www.opensource.org/licenses/eclipse-1.0.php
  *
  *  (C) Copyright IBM Corporation 2006-2010.
  */
 
 package x10.ast;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import polyglot.ast.Assign;
 import polyglot.ast.Assign_c;
 import polyglot.ast.Binary;
 import polyglot.ast.Call;
 import polyglot.ast.Expr;
 import polyglot.ast.Field;
 import polyglot.ast.Node;
 import polyglot.ast.NodeFactory;
 import polyglot.ast.Precedence;
 import polyglot.ast.Term;
 import polyglot.ast.TypeNode;
 import polyglot.ast.Assign.Operator;
 import polyglot.frontend.Globals;
 import polyglot.types.ClassDef;
 import polyglot.types.Context;
 import polyglot.types.ErrorRef_c;
 import polyglot.types.Flags;
 import polyglot.types.Matcher;
 import polyglot.types.MethodDef;
 import polyglot.types.MethodInstance;
 import polyglot.types.SemanticException;
 import polyglot.types.Name;
 import polyglot.types.Type;
 import polyglot.types.TypeSystem;
 import polyglot.types.Types;
 import polyglot.types.UnknownType;
 import polyglot.types.TypeSystem_c.MethodMatcher;
 import polyglot.util.CodeWriter;
 import polyglot.util.InternalCompilerError;
 import polyglot.util.Pair;
 import polyglot.util.Position;
 import polyglot.util.TypedList;
 import polyglot.visit.AscriptionVisitor;
 import polyglot.visit.CFGBuilder;
 import polyglot.visit.ContextVisitor;
 import polyglot.visit.NodeVisitor;
 import polyglot.visit.PrettyPrinter;
 import polyglot.visit.Translator;
 import polyglot.visit.TypeBuilder;
 import polyglot.visit.TypeChecker;
 import x10.errors.Errors;
 import x10.types.X10ClassDef;
 import x10.types.X10MethodInstance;
 import x10.types.X10TypeMixin;
 import x10.types.X10TypeSystem;
 import x10.types.X10TypeSystem_c;
 import x10.types.checker.Checker;
 import x10.types.checker.Converter;
 import x10.types.matcher.DumbMethodMatcher;
 import x10.visit.X10TypeChecker;
 
 /** An immutable representation of an X10 array access update: a[point] op= expr;
  * TODO
  * Typechecking rules:
  * (1) point must be of a type (region) that can be cast to the array index type.
  * (2) expr must be of a type that can be implicitly cast to the base type of the array.
  * (3) The operator, if any, must be permitted on the underlying type.
  * (4) No assignment is allowed on a value array.
  * @author vj Dec 9, 2004
  * 
  */
 public class SettableAssign_c extends Assign_c implements SettableAssign {
    	protected Expr array;
    	protected List<Expr> index;
    
 	/**
 	 * @param pos
 	 * @param left
 	 * @param op
 	 * @param right
 	 */
     public SettableAssign_c(X10NodeFactory nf, Position pos, Expr array, List<Expr> index, Operator op, Expr right) {
 		super(nf, pos, op, right);
 		if (index.size() < 1)
 		assert index.size() >= 1;
 		this.array = array;
 		this.index = index;
 	}
 	
 	public Type leftType() {
 	    if (mi == null) return null;
 	    return mi.formalTypes().get(0);
 	}
 
 	public Expr left() {
 		return left(nf, null);
 	}
 	//@Override
 	public Expr left(NodeFactory nf, ContextVisitor cv) {
 	    Call c = nf.Call(position(), array, nf.Id(position(), ClosureCall.APPLY), index);
 	    if (ami != null) {
 	        c = c.methodInstance(ami);
 	    }
 	    if (type != null && ami != null) c = (Call) c.type(ami.returnType());
 	    return c;
 	}
 	
    	/** Get the precedence of the expression. */
    	public Precedence precedence() {
    		return Precedence.LITERAL;
    	}
    	
    	/** Get the array of the expression. */
    	public Expr array() {
    		return this.array;
    	}
    	
    	/** Set the array of the expression. */
    	public SettableAssign array(Expr array) {
    	 SettableAssign_c n = (SettableAssign_c) copy();
    		n.array = array;
    		return n;
    	}
    	
    	/** Get the index of the expression. */
    	public List<Expr> index() {
    		return TypedList.copy(this.index, Expr.class, false);
    	}
    	
    	/** Set the index of the expression. */
    	public SettableAssign index(List<Expr> index) {
    	    SettableAssign_c n = (SettableAssign_c) copy();
    	    n.index = TypedList.copyAndCheck(index, Expr.class, true);
    	    return n;
    	}
    	
    	/** Reconstruct the expression. */
    	protected SettableAssign_c reconstruct( Expr array, List<Expr> index ) {
    		if (array != this.array || index != this.index) {
    		 SettableAssign_c n = (SettableAssign_c) copy();
    			n.array = array;
    			n.index = TypedList.copyAndCheck(index, Expr.class, true);
    			return n;
    		}
    		return this;
    	}
    	/** Return the access flags of the variable. */
    	public Flags flags() {
    		return Flags.NONE;
    	}
    	
    	
    	/** Visit the children of the expression. */
    	public Assign visitLeft(NodeVisitor v) {
    		Expr array = (Expr) visitChild(this.array, v);
    		List<Expr> index =  visitList(this.index, v);
    		return reconstruct(array, index);
    	}
    	
    	public Type childExpectedType(Expr child, AscriptionVisitor av) {
    		X10TypeSystem ts = (X10TypeSystem) av.typeSystem();
    		
    		if (child == array) {
    			return ts.Settable();
    		}
    		
    		return child.type();
    	}
 	
 	X10MethodInstance mi;
 	X10MethodInstance ami;
 	
 	public X10MethodInstance methodInstance() {
 	    return mi;
 	}
 	public SettableAssign methodInstance(X10MethodInstance mi) {
 	    SettableAssign_c n = (SettableAssign_c) copy();
 	    n.mi = mi;
 	    return n;
 	}
 	public X10MethodInstance applyMethodInstance() {
 	    return ami;
 	}
 	public SettableAssign applyMethodInstance(X10MethodInstance ami) {
 	    SettableAssign_c n = (SettableAssign_c) copy();
 	    n.ami = ami;
 	    return n;
 	}
 	
 	@Override
 	public Node buildTypes(TypeBuilder tb) throws SemanticException {
 	    SettableAssign_c n = (SettableAssign_c) super.buildTypes(tb);
 
 	    X10TypeSystem ts = (X10TypeSystem) tb.typeSystem();
 
 	    X10MethodInstance mi = ts.createMethodInstance(position(), new ErrorRef_c<MethodDef>(ts, position(), "Cannot get MethodDef before type-checking settable assign."));
 	    X10MethodInstance ami = ts.createMethodInstance(position(), new ErrorRef_c<MethodDef>(ts, position(), "Cannot get MethodDef before type-checking settable assign."));
 	    return n.methodInstance(mi).applyMethodInstance(ami);
 	}
 
 	static Pair<MethodInstance,List<Expr>> tryImplicitConversions(X10Call_c n, ContextVisitor tc,
 	        Type targetType, List<Type> typeArgs, List<Type> argTypes) throws SemanticException {
 	    final X10TypeSystem ts = (X10TypeSystem) tc.typeSystem();
 	    final Context context = tc.context();
 
 	    List<MethodInstance> methods = ts.findAcceptableMethods(targetType,
 	            new DumbMethodMatcher(targetType, Name.make("set"), typeArgs, argTypes, context));
 
 	    Pair<MethodInstance,List<Expr>> p = Converter.<MethodDef,MethodInstance>tryImplicitConversions(n, tc,
 	            targetType, methods, new X10New_c.MatcherMaker<MethodInstance>() {
 	        public Matcher<MethodInstance> matcher(Type ct, List<Type> typeArgs, List<Type> argTypes) {
 	            return ts.MethodMatcher(ct, Name.make("set"), typeArgs, argTypes, context);
 	        }
 	    });
 
 	    return p;
 	}
 
 	@Override
	public Assign typeCheckLeft(ContextVisitor tc) {
 		X10TypeSystem ts = (X10TypeSystem) tc.typeSystem();
 		X10NodeFactory nf = (X10NodeFactory) tc.nodeFactory();
 		X10TypeSystem xts = ts;
 
 		X10MethodInstance mi = null;
 
 		List<Type> typeArgs = Collections.<Type>emptyList();
 		List<Type> actualTypes = new ArrayList<Type>(index.size()+1);
 		actualTypes.add(right.type());
 		for (Expr ei : index) {
 		    actualTypes.add(ei.type());
 		}
 
 		List<Expr> args = new ArrayList<Expr>();
 		args.add(right);
 		args.addAll(index);
 
 		// First try to find the method without implicit conversions.
 		mi = Checker.findAppropriateMethod(tc, array.type(), SET, typeArgs, actualTypes);
 		if (mi.error() != null) {
 		    // Now, try to find the method with implicit conversions, making them explicit.
 		    try {
 		        X10Call_c n = (X10Call_c) nf.X10Call(position(), array, nf.Id(position(), SET), Collections.<TypeNode>emptyList(), args);
 		        Pair<MethodInstance,List<Expr>> p = tryImplicitConversions(n, tc, array.type(), typeArgs, actualTypes);
 		        mi = (X10MethodInstance) p.fst();
 		        args = p.snd();
 		    }
 		    catch (SemanticException e) {
 		        if (mi.error() instanceof Errors.CannotGenerateCast) {
 		            throw new InternalCompilerError("Unexpected cast error", mi.error());
 		        }
 		        Type bt = X10TypeMixin.baseType(array.type());
 		        boolean arrayP = xts.isX10Array(bt) || xts.isX10DistArray(bt);
 		        Errors.issue(tc.job(), new Errors.CannotAssignToElement(leftToString(), arrayP, right, X10TypeMixin.arrayElementType(array.type()), position(), mi.error()));
 		    }
 		}
 
 		X10MethodInstance ami = null;
 
 		actualTypes = new ArrayList<Type>(mi.formalTypes());
 		actualTypes.remove(0);
 
 		// First try to find the method without implicit conversions.
 		ami = Checker.findAppropriateMethod(tc, array.type(), ClosureCall.APPLY, typeArgs, actualTypes);
 		if (ami.error() != null) {
 		    Type bt = X10TypeMixin.baseType(array.type());
 		    boolean arrayP = xts.isX10Array(bt) || xts.isX10DistArray(bt);
 		    Errors.issue(tc.job(), new Errors.CannotAssignToElement(leftToString(), arrayP, right, X10TypeMixin.arrayElementType(array.type()), position(), ami.error()));
 		}
 
 		if (op != Assign.ASSIGN) {
 		    X10Call_c left = (X10Call_c) nf.X10Call(position(), array, nf.Id(position(),
 		            ClosureCall.APPLY), Collections.<TypeNode>emptyList(),
 		            index).methodInstance(ami).type(ami.returnType());
 		    X10Binary_c n = (X10Binary_c) nf.Binary(position(), left, op.binaryOperator(), right);
 		    X10Call c = X10Binary_c.desugarBinaryOp(n, tc);
 		    X10MethodInstance cmi = (X10MethodInstance) c.methodInstance();
 		    if (cmi.error() != null) {
 		        Type bt = X10TypeMixin.baseType(array.type());
 		        boolean arrayP = xts.isX10Array(bt) || xts.isX10DistArray(bt);
		        Errors.issue(tc.job(),
		                new Errors.CannotPerformAssignmentOperation(leftToString(), arrayP, op.toString(), right, X10TypeMixin.arrayElementType(array.type()), position(), cmi.error()));
 		    }
 		}
 
 		if (mi.flags().isStatic() ) {
 		    Errors.issue(tc.job(), new Errors.AssignSetMethodCantBeStatic(mi, array, position()));
 		}
 
 		SettableAssign_c a = this;
 		a = (SettableAssign_c) a.methodInstance(mi);
 		a = (SettableAssign_c) a.applyMethodInstance(ami);
 		a = (SettableAssign_c) a.right(args.get(0));
 		a = (SettableAssign_c) a.index(args.subList(1, args.size()));
 		return a;
 	}
 	
 	@Override
 	public Node typeCheck(ContextVisitor tc) {
 	    SettableAssign_c a = (SettableAssign_c) x10.types.checker.Checker.typeCheckAssign(this, tc);
 	    return a.type(a.mi.returnType());
 	}
 	
 	public Term firstChild() {
 	    return array;
 	}
 	
 	protected void acceptCFGAssign(CFGBuilder v) {
 		v.visitCFG(array, listChild(index, right()), ENTRY);
 		v.visitCFGList(index, right(), ENTRY);
 		v.visitCFG(right(), this, EXIT);
 	}
 	protected void acceptCFGOpAssign(CFGBuilder v) {
 	    v.visitCFG(array, listChild(index, right()), ENTRY);
 	    v.visitCFGList(index, right(), ENTRY);
 	    v.visitCFG(right(), this, EXIT);
 	}
 
 	public List<Type> throwTypes(TypeSystem ts) {
 		List<Type> l = new ArrayList<Type>(super.throwTypes(ts));
 		l.add(ts.NullPointerException());
 		l.add(ts.OutOfBoundsException());
 		return l;
 	}
 
 	public String toString() {
 	    StringBuilder sb = new StringBuilder();
 	    sb.append(array.toString());
 	    sb.append("(");
 	    String sep = "";
 	    for (Expr e : index) {
 	        sb.append(sep);
 	        sep = ", ";
 	        sb.append(e);
 	    }
 	    sb.append(") ");
 	    sb.append(op);
 	    sb.append(" ");
 	    sb.append(right.toString());
 	    return sb.toString();
 	}
 
 	/** Write the expression to an output file. */
 	public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
 	    Type at = array.type();
 
 	    printSubExpr(array, w, tr);
 	    w.write ("(");
 	    w.begin(0);
 
 	    for(Iterator<Expr> i = index.iterator(); i.hasNext();) {
 	        Expr e = i.next();
 	        print(e, w, tr);
 	        if (i.hasNext()) {
 	            w.write(",");
 	            w.allowBreak(0, " ");
 	        }
 	    }
 
 	    w.write (")");
 	    w.write(" ");
 	    w.write(op.toString());
 	    w.write(" ");
 
 	    print(right, w, tr);
 
 	    w.end();
 	}
 
 	public String leftToString() {
 	    String arg = index.toString();
 	    return array.toString() + "(" + arg.substring(1, arg.length()-1) + ")";
 	}
 
 }
