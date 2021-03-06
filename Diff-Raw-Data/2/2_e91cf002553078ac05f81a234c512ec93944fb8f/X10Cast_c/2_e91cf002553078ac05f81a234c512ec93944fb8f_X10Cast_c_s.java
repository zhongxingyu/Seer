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
 import java.util.Collections;
 import java.util.List;
 
 import polyglot.ast.Cast;
 import polyglot.ast.Cast_c;
 import polyglot.ast.Expr;
 import polyglot.ast.Node;
 import polyglot.ast.Precedence;
 import polyglot.ast.Term;
 import polyglot.ast.TypeNode;
 import polyglot.types.ClassDef;
 import polyglot.types.ConstructorDef;
 import polyglot.types.ConstructorInstance;
 import polyglot.types.Context;
 import polyglot.types.ErrorRef_c;
 import polyglot.types.ObjectType;
 import polyglot.types.SemanticException;
 import polyglot.types.Type;
 import polyglot.types.TypeSystem;
 import polyglot.types.Types;
 import polyglot.util.CodeWriter;
 import polyglot.util.Position;
 import polyglot.visit.AscriptionVisitor;
 import polyglot.visit.CFGBuilder;
 import polyglot.visit.ContextVisitor;
 import polyglot.visit.NodeVisitor;
 import polyglot.visit.PrettyPrinter;
 import x10.errors.Errors;
 import x10.errors.Warnings;
 import x10.types.ParameterType;
 import x10.types.X10ClassType;
 import x10.types.X10ParsedClassType;
 import polyglot.types.TypeSystem;
 import x10.types.checker.Converter;
 import x10.types.checker.Converter.ConversionType;
 
 /**
  * Represent java cast operation.
  * (CastType) expression
  * This class is compliant with dependent type constraint.
  * If a dynamic cast is needed, then some code is generated to check 
  * instance's value, field, etc... are valid against declared type constraint.
  *
  * @author vcave
  *
  */
 public class X10Cast_c extends Cast_c implements X10Cast, X10CastInfo {
     protected Converter.ConversionType convert;
     protected TypeNode castType;
     protected Expr expr;
     
     public X10Cast_c(Position pos, TypeNode castType, Expr expr, Converter.ConversionType convert) {
         super(pos, castType, expr);
         assert(castType != null && expr != null);
     	this.castType = castType;
     	this.expr = expr;
         this.convert = convert;
     }
 
     public Converter.ConversionType conversionType() {
         return convert;
     }
 
     public X10Cast conversionType(ConversionType convert) {
         X10Cast_c n = (X10Cast_c) copy();
         n.convert = convert;
         return n;
     }
     public X10Cast exprAndConversionType(Expr expr, ConversionType convert) {
     	  X10Cast_c n = (X10Cast_c) copy();
           n.convert = convert;
           n.expr = expr;
           return n;
     }
 
     /** Get the cast type of the expression. */
     public TypeNode castType() {
 	return this.castType;
     }
 
     /** Set the cast type of the expression. */
     public X10Cast castType(TypeNode castType) {
 	X10Cast_c n = (X10Cast_c) copy();
 	n.castType = castType;
 	return n;
     }
 
     /** Get the expression being cast. */
     public Expr expr() {
 	return this.expr;
     }
 
     /** Set the expression being cast. */
     public X10Cast expr(Expr expr) {
 	X10Cast_c n = (X10Cast_c) copy();
 	n.expr = expr;
 	return n;
     }
 
     /** Reconstruct the expression. */
     protected X10Cast_c reconstruct(TypeNode castType, Expr expr) {
     	if (castType != this.castType || expr != this.expr) {
 	    	X10Cast_c n = (X10Cast_c) copy();
 	    	n.castType = castType;
 	    	n.expr = expr;
 	    	return n;
 		}
 
 		return this;
     }
     @Override
     public Precedence precedence() {
         switch (convert) {
         case PRIMITIVE:
         case SUBTYPE:
             return Precedence.CAST;
         default:
             return Precedence.UNKNOWN;
         }
     }
 
     public Node typeCheck(ContextVisitor tc) {
         if (castType() != null) {
             try {
                 Types.checkMissingParameters(castType());
             } catch (SemanticException e) {
                 Errors.issue(tc.job(), e, this);
             }
         }
         try {
             Expr e = Converter.converterChain(this, tc);
             final Type type = e.type();
             assert type != null;
             assert ! (e instanceof X10Cast_c) || ((X10Cast_c) e).conversionType() != Converter.ConversionType.UNKNOWN_CONVERSION;
             assert ! (e instanceof X10Cast_c) || ((X10Cast_c) e).conversionType() != Converter.ConversionType.UNKNOWN_IMPLICIT_CONVERSION;
 
             // todo hack: after constraints will be kept at runtime, and we will do constraint solving at runtime, then all casts will be sound!
             // X10 currently doesn't do constraint solving at runtime (and constraints are erased at runtime!),
             // so given o:Any, a cast:
             //  o as Array[Int]
             // is unsound if "o" had constraints (e.g., Array[Int{self!=0}])
             // obviously,   o as Array[Int{self!=0}]     is always unsound.
             // Note that any generic struct will this warning due to the auto-generated equals method:
             //struct A[T] {
             //  public def equals(o:Any) {
             //    if (o instanceof A[T]) {
             //      val x = o as A[T]; // Warning: unsound cast!
             //      ...
             // Therefore we do not produce warnings in compiler-generated code (too confusing for the programmer).
             // In addition, I also don't report the 3 warnings we have in XRX (or else every client of HashMap will have a warning)
             if (!position.isCompilerGenerated() &&
                     !position.file().contains("Array.x10")&&
                     !position.file().contains("Box.x10")&&
                     !position.file().contains("HashMap.x10")&&
                     !position.file().contains("FinishState.x10")&&
                     !position.file().contains("Runtime.x10")&& 
                     !position.file().contains("HashSet.x10")) {
                 Type base = Types.baseType(type);
                 if (base instanceof X10ParsedClassType) {
                     X10ParsedClassType classType = (X10ParsedClassType) base;
                     final List<Type> args = classType.typeArguments();
                     if (args!=null && args.size()>0) {
                         boolean isOk = false;
                         if (e instanceof X10Cast) {
                             // ok, e.g., x:Array[Int],   x as Array[Int](3)
                             final X10Cast cast = (X10Cast) e;
                             if (cast.conversionType()== ConversionType.SUBTYPE)
                                 isOk = true;
                             else if (tc.typeSystem().isSubtype(Types.baseType(cast.expr().type()),base, tc.context()))
                                 isOk = true;
                         }
                         if (!isOk) {
                             Warnings.issue(tc.job(), "This is an unsound cast because X10 currently does not perform constraint solving at runtime for generic parameters.", position);
                         }
                     }
                 }
             }
             return e;
         } catch (SemanticException e) {
             Errors.issue(tc.job(), e, this);
         }
         return this;
     }
 
     public TypeNode getTypeNode() {
         return (TypeNode) this.castType().copy();
     }
 
     public String toString() {
         return expr.toString() + " as " + castType.toString();
     }
 
     @Override
     public List<Type> throwTypes(TypeSystem ts) {
         // 'e as T' and 'e to T' can throw ClassCastException
         if (expr.type().isReference()) {
             return Collections.<Type>singletonList(ts.ClassCastException());
         }
 
         return Collections.<Type>emptyList();
     }
     
     /** Write the expression to an output file. */
     public void prettyPrint(CodeWriter w, PrettyPrinter tr)
     {
 	w.begin(0);
 	w.write("(");
 	print(castType, w, tr);
 	w.write(")");
 	w.allowBreak(2, " ");
 	printSubExpr(expr, w, tr);
 	w.end();
     }
 
     public Term firstChild() {
         return expr;
     }
     
     public <S> List<S> acceptCFG(CFGBuilder v, List<S> succs) {
         v.visitCFG(expr, castType, ENTRY);
         v.visitCFG(castType, this, EXIT);
         return succs;
     }
     
     public Type childExpectedType(Expr child, AscriptionVisitor av) {
         TypeSystem ts = av.typeSystem();
 
         if (child == expr) {
             if (castType.type().isReference()) {
                 return ts.Object();
             }
             else if (castType.type().isNumeric()) {
                 return ts.Double();
             }
             else if (castType.type().isBoolean()) {
                 return ts.Boolean();
             }
         }
 
         return child.type();
     }
     
     /** Visit the children of the expression. */
     public Node visitChildren(NodeVisitor v) {
     	TypeNode castType = (TypeNode) visitChild(this.castType, v);
     	Expr expr = (Expr) visitChild(this.expr, v);
     	return reconstruct(castType, expr);
     }
     
     public boolean isConstant() {
         if (!expr.isConstant()) return false;
         if (castType.type().isNumeric()) return true;
        if (castType.type().typeSystem().isAny(castType.type())) return false; // FIXME: because constantValue method below doesn't know how to correctly handle this case
         return expr.type().isSubtype(castType.type(), expr.type().typeSystem().emptyContext());
     }
         
     public Object constantValue() {
     	Object v = expr.constantValue();
 
     	if (v == null) {
     	    return null;
     	}
     	
     	if (v instanceof Boolean) {
     		if (castType.type().isBoolean()) return v;
     	}
     	
     	if (v instanceof String) {
     		TypeSystem ts = castType.type().typeSystem();
     		if (castType.type().typeEquals(ts.String(), ts.emptyContext())) return v;
     	}
     	
     	if (v instanceof Double) {
     		double vv = ((Double) v).doubleValue();
     		
     		if (castType.type().isDouble()) return Double.valueOf((double) vv);
     		if (castType.type().isFloat()) return Float.valueOf((float) vv);
     		if (castType.type().isLong()) return Long.valueOf((long) vv);
     		if (castType.type().isInt()) return Integer.valueOf((int) vv);
     		if (castType.type().isChar()) return Character.valueOf((char) vv);
     		if (castType.type().isShort()) return Short.valueOf((short) vv);
     		if (castType.type().isByte()) return Byte.valueOf((byte) vv);
     	}
     	
     	if (v instanceof Float) {
     		float vv = ((Float) v).floatValue();
     		
     		if (castType.type().isDouble()) return Double.valueOf((double) vv);
     		if (castType.type().isFloat()) return Float.valueOf((float) vv);
     		if (castType.type().isLong()) return Long.valueOf((long) vv);
     		if (castType.type().isInt()) return Integer.valueOf((int) vv);
     		if (castType.type().isChar()) return Character.valueOf((char) vv);
     		if (castType.type().isShort()) return Short.valueOf((short) vv);
     		if (castType.type().isByte()) return Byte.valueOf((byte) vv);
     	}
 
     	if (v instanceof Number) {
     		long vv = ((Number) v).longValue();
     		
     		if (castType.type().isDouble()) return Double.valueOf((double) vv);
     		if (castType.type().isFloat()) return Float.valueOf((float) vv);
     		if (castType.type().isLong()) return Long.valueOf((long) vv);
     		if (castType.type().isInt()) return Integer.valueOf((int) vv);
     		if (castType.type().isChar()) return Character.valueOf((char) vv);
     		if (castType.type().isShort()) return Short.valueOf((short) vv);
     		if (castType.type().isByte()) return Byte.valueOf((byte) vv);
     	}
     	
     	if (v instanceof Character) {
     		char vv = ((Character) v).charValue();
     		
     		if (castType.type().isDouble()) return Double.valueOf((double) vv);
     		if (castType.type().isFloat()) return Float.valueOf((float) vv);
     		if (castType.type().isLong()) return Long.valueOf((long) vv);
     		if (castType.type().isInt()) return Integer.valueOf((int) vv);
     		if (castType.type().isChar()) return Character.valueOf((char) vv);
     		if (castType.type().isShort()) return Short.valueOf((short) vv);
     		if (castType.type().isByte()) return Byte.valueOf((byte) vv);
     	}
 
     	// not a constant
     	return null;
     }
 }
