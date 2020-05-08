 package semant;
 
 import syntaxtree.*;
 import java.util.Vector;
 
 public class TypeCheckVisitor extends visitor.TypeDepthFirstVisitor {
     // By extending TypeDepthFirstVisitor, we only have to override those
     // methods that differ from the generic visitor.
 
     private errormsg.ErrorMsg errorMsg;
     private SymbolTable classTable;
     private ClassInfo currClass;
     private MethodInfo currMethod;
 
     // Type constants
     final IntegerType INTTY = new IntegerType();
     final IntArrayType INTARRTY = new IntArrayType();
     final BooleanType BOOLTY = new BooleanType();
 
     public TypeCheckVisitor(errormsg.ErrorMsg e, SymbolTable s){
 	errorMsg = e;
 	classTable = s;
 	currClass = null;
 	currMethod = null;
     }
 
     // Identifier i1,i2;
     // Statement s;
     public Type visit(MainClass n)
     {
 	// Mostly you just need to typecheck the body of 'main' here.
 	// But as shown in Foo.java, you need care concerning 'this'.
 	currClass = classTable.get(n.i1.toString());
 	n.s.accept(this);
 	return null;
     }
     // Identifier i;
     // VarDeclList vl;
     // MethodDeclList ml;
     public Type visit(ClassDeclSimple n)
     {
 	String id = n.i.toString();
 	currClass = classTable.get(id);
 	if(currClass == null)
	    errorMsg.error("Cannot Find Symbol: " + id);
 	else
 	{
 	    for ( int i = 0; i < n.vl.size(); i++ )
 		n.vl.elementAt(i).accept(this);
 	    for ( int i = 0; i < n.ml.size(); i++ )
 		n.ml.elementAt(i).accept(this);
 	}
 	return null;
     }
 
     // Exp e1,e2;
     public Type visit(LessThan n) {
 	Type t1 = n.e1.accept(this);
 	Type t2 = n.e2.accept(this);
 
 	if (!equal(t1, t2, INTTY))
 	    errorMsg.error(n.pos, eIncompBiop("<", t1.toString(), t2.toString()));
 	return BOOLTY;
     }
     // Exp e1,e2;
     public Type visit(Plus n)
     {
 	Type t1 = n.e1.accept(this);
 	Type t2 = n.e2.accept(this);
 
 	if (!equal(t1, t2, INTTY))
 	    errorMsg.error(n.pos, eIncompBiop("+", t1.toString(), t2.toString()));
 	return INTTY;
     }
     // Exp e1,e2;
     public Type visit(Minus n) {
 	Type t1 = n.e1.accept(this);
 	Type t2 = n.e2.accept(this);
 
 	if (!equal(t1, t2, INTTY))
 	    errorMsg.error(n.pos, eIncompBiop("-", t1.toString(), t2.toString()));
 	return INTTY;
     }
     // Exp e1,e2;
     public Type visit(Times n) {
 	Type t1 = n.e1.accept(this);
 	Type t2 = n.e2.accept(this);
 
 	if (!equal(t1, t2, INTTY))
 	    errorMsg.error(n.pos, eIncompBiop("*", t1.toString(), t2.toString()));
 	return INTTY;
     }
     // Exp e;
     // Identifier i;
     // ExpList el;
     public Type visit(Call n)
     {
 	n.e.accept(this);
 	n.i.accept(this);
 	for ( int i = 0; i < n.el.size(); i++ )
 	{
 	    n.el.elementAt(i).accept(this);
 	}
 	return null;
     }
     // Identifier i;
     public Type visit(NewObject n)
     {
 	return new IdentifierType(n.i.toString());
     }
     // Exp e;
     public Type visit(NewArray n)
     {
 	n.e.accept(this);
 	return INTARRTY;
     }
 
     // int i;
     public Type visit(IntegerLiteral n)
     {
 	return INTTY;
     }
     public Type visit(IntArrayType n)
     {
 	return INTARRTY;
     }
 
     public Type visit(BooleanType n)
     {
 	return BOOLTY;
     }
 
     public Type visit(IntegerType n)
     {
 	return INTTY;
     }
 
     // String s;
     public Type visit(IdentifierType n)
     {
 	return n;
     }
     // Exp e1,e2;
     public Type visit(And n)
     {
 	Type t1 = n.e1.accept(this);
 	Type t2 = n.e2.accept(this);
 	if (!equal(t1, t2, BOOLTY))
 	    errorMsg.error(n.pos, eIncompBiop("&&", t1.toString(), t2.toString()));
 	return BOOLTY;
     }
     // Exp e;
     public Type visit(Not n)
     {
 	n.e.accept(this);
 	return BOOLTY;
     }
 
     public Type visit(True n)
     {
 	return BOOLTY;
     }
     public Type visit(False n)
     {
 	return BOOLTY;
     }
     public Type visit(This n)
     {
 	if(currClass != null)
 	    return new IdentifierType(currClass.getName());
 	return null;
     }
 
     // Exp e;
     public Type visit(ArrayLength n) {
 	n.e.accept(this);
 	return INTTY;
     }
 
     // Identifier i;
     // Exp e;
     public Type visit(Assign n)
     {
 	Type t1 = n.i.accept(this);
 	Type t2 = n.e.accept(this);
 
 	if (!t1.toString().equals(t2.toString()))
 	    errorMsg.error(n.pos, eIncompTypes(t2.toString(), t1.toString()));
 	return null;
     }
     // Identifier i;
     // Exp e1,e2;
     public Type visit(ArrayAssign n)
     {
 	Type arrType =n.i.accept(this);
 	String id = n.i.toString();
 	Type t1 = n.e1.accept(this);
 	Type t2 = n.e2.accept(this);
 
	if(t1 == null)
	    errorMsg.error("Cannot Find Symbol: " + e1.);
	if(t1 == null)
	    errorMsg.error("Cannot Find Symbol: " + e2.);
	else if(!(t1 instanceof IntegerType))
 	    errorMsg.error(n.e1.pos, eIncompTypes(t1.toString(), INTTY.toString()));
	else if(!(t2 instanceof IntegerType))
 	    errorMsg.error(n.e2.pos, eIncompTypes(t2.toString(), INTTY.toString()));
 	return null;
     }
     // Type t;
     // Identifier i;
     // FormalList fl;
     // VarDeclList vl;
     // StatementList sl;
     // Exp e;
     public Type visit(MethodDecl n)
     {
 	currMethod = currClass.getMethod(n.i.toString());
 	n.t.accept(this);
 	n.i.accept(this);
 	for ( int i = 0; i < n.fl.size(); i++ )
 	    n.fl.elementAt(i).accept(this);
 	for ( int i = 0; i < n.vl.size(); i++ )
 	    n.vl.elementAt(i).accept(this);
 	for ( int i = 0; i < n.sl.size(); i++ )
 	    n.sl.elementAt(i).accept(this);
 
 	n.e.accept(this);
 	currMethod = null;
 	return n.t;
     }
 
     // String s;
     public Type visit(Identifier n)
     {
 	VariableInfo v = null;
 	if(currMethod != null)
 	{
 	    if(currClass.getMethod(n.s) == null)
 	    {
 		v = currMethod.getVar(n.s);
 		if(v != null)
 		    return v.type;
 		else if((v =currClass.getField(n.s)) != null)
 		    return v.type;
 		else
 		    System.out.println("Cannot find Symbol " + n.s +
 				       " in class " + currClass.getName());
 	    }
 	}
 	else
 	{
 	    if(currClass.getMethod(n.s) == null)
 	    {
 		v = currClass.getField(n.s);
 		if(v != null)
 		    return v.type;
 		else
 		    System.out.println("Cannot find Symbol " + n.s +
 				       " in class " + currClass.getName());
 	    }
 	}
	System.out.println("Just visited identifier [" + n.s  + "]");
 	return null;
     }
 
     // Check whether t1 == t2 == target, but suppress error messages if
     // either t1 or t2 is null.
     private boolean equal(Type t1, Type t2, Type target) {
 	if ( t1 == null || t2 == null )
 	    return true;
 
 	if (target == null)
 	    throw new Error("target argument in method equal cannot be null");
 
 	if (target instanceof IdentifierType && t1 instanceof IdentifierType
 	    && t2 instanceof IdentifierType)
 	    return ((IdentifierType) t1).s.equals(((IdentifierType) t2).s );
 
 	if (!(target instanceof IdentifierType) &&
 	    t1.toString().equals(target.toString()) &&
 	    t2.toString().equals(target.toString()))
 	    return true;
 
 	return false;
     }
 
     // Methods for error reporting:
     private String eIncompTypes(String t1, String t2) {
 	return "incompatible types \nfound   : " + t1
 	    + "\nrequired: " + t2 ;
     }
 
     private String eIncompBiop(String op, String t1, String t2) {
 	return "operator " + op + " cannot be applied to " + t1 + "," + t2 ;
     }
 }
