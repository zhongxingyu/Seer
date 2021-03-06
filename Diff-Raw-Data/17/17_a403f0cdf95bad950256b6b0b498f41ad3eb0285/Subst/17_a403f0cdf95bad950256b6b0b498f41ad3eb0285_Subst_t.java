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
 package x10.types.matcher;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import polyglot.types.LocalInstance;
 import polyglot.types.NullType;
 import polyglot.types.SemanticException;
 import polyglot.types.StructType;
 import polyglot.types.Type;
import polyglot.types.Types;
 import polyglot.types.UnknownType;
 import x10.constraint.XFailure;
 import x10.constraint.XLocal;
 import x10.constraint.XVar;
 import x10.constraint.XTerm;
 import x10.types.ConstrainedType;
 import x10.types.MacroType;
 import x10.types.ParameterType;
 import x10.types.TypeParamSubst;
 import x10.types.X10ClassType;
 import x10.types.X10ConstructorInstance;
 import x10.types.X10FieldInstance;
 import x10.types.X10LocalInstance;
 import x10.types.X10MethodInstance;
 import x10.types.X10ParsedClassType;
 import x10.types.X10TypeMixin;
 import polyglot.types.TypeSystem;
 import x10.types.constraints.CConstraint;
 import x10.types.constraints.TypeConstraint;
 
 public class Subst {
     public static Type subst(Type t, Type[] Y, ParameterType[] X) throws SemanticException {
         assert Y.length == X.length;
 
         for (int i = 0; i < X.length; i++) {
             if (Y[i] == null)
                 throw new SemanticException("Cannot infer type for parameter " + X[i] + ".");
             t = subst(t, Y[i], X[i]);
         }
         return t;
     }
         
     /**
      * Add in to the constraint for every component of the type t, recursively:
      * If t is T{c} ===> return addIn(T,in){c,in}
      * If t is A[T1,..., Tn] ===> return addIn(A,in)[addIn(T1,in),..., addIn(Tn,n)]
      * If t is X ==> return X{in}
      * @param t --- The type subjected to the change
      * @param in --- The constraint to be added
      * @return -- The type t, with c added to each component (recursively).
      * @throws XFailure -- If one of the component types is inconsistent.
      */
     public static Type addIn(Type t, CConstraint in) throws XFailure {
         if (t == null)
             return null;
 
         TypeSystem ts = (TypeSystem) t.typeSystem();
 
         t = ts.expandMacros(t);
 
         if (t instanceof NullType)
             return t;
         if (t instanceof UnknownType)
             return t;
         if (t instanceof ParameterType) {
             return X10TypeMixin.xclause(t, in);
         }
         if (ts.isVoid(t)) {
             return t;
         }
         if (t instanceof X10ParsedClassType) {
             X10ParsedClassType ct = (X10ParsedClassType) t;
             List<Type> newArgs = new ArrayList<Type>();
             if (ct.typeArguments() == null)
                 return ct;
             for (Type at : ct.typeArguments()) {
                 Type at2 = addIn(at, in);
                 newArgs.add(at2);
             }
             return ct.typeArguments(newArgs);
         }
         Type base = X10TypeMixin.baseType(t);
         CConstraint c = X10TypeMixin.xclause(t);
         if (t==base)
             assert t != base;
 
         base = addIn(base, in);
 
         if (c != null) {
             c = c.copy().addIn(in);
         }
 
         return X10TypeMixin.xclause(base, c);
     }
 
     public static Type project(Type t, XVar v) {
         if (t == null)
             return null;
 
 
         TypeSystem ts = (TypeSystem) t.typeSystem();
 
         t = ts.expandMacros(t);
 
         if (t instanceof NullType)
             return t;
         if (t instanceof UnknownType)
             return t;
         if (t instanceof ParameterType) {
             return t;
         }
         if (ts.isVoid(t)) {
             return t;
         }
         if (t instanceof X10ParsedClassType) {
             X10ParsedClassType ct = (X10ParsedClassType) t;
             List<Type> newArgs = new ArrayList<Type>();
             if (ct.typeArguments() == null)
                 return ct;
             for (Type at : ct.typeArguments()) {
                 Type at2 = project(at, v);
                 newArgs.add(at2);
             }
             return ct.typeArguments(newArgs);
         }
 
         Type base = X10TypeMixin.baseType(t);
         CConstraint c = X10TypeMixin.xclause(t);
         if (t == base) 
             assert t != base;
         base = project(base, v);
 
         if (c != null) {
             c = c.copy().project(v);
         }
 
         return X10TypeMixin.xclause(base, c);
     }
 
     /**
      * Returns a new type formed from t by substituting y for x. The old type is not modified.
      * @param t
      * @param y
      * @param x
      * @return
      * @throws SemanticException
      */
     public static List<Type> subst(List<Type> ts, XTerm[] y, XVar[] x) throws SemanticException {
         List<Type> result= new ArrayList<Type>(ts.size());
         for (Type t : ts) 
             result.add(subst(t, y, x));
         return result;
     }
 
     public static Type subst(Type t, XTerm[] y, XVar[] x) throws SemanticException {
         assert y.length == x.length;
 
         if (t == null)
             return null;
 
         TypeSystem ts = (TypeSystem) t.typeSystem();
 
         t = ts.expandMacros(t);
 
         Type base = X10TypeMixin.baseType(t);
         CConstraint c = X10TypeMixin.xclause(t);
 
 
         if (t instanceof X10ParsedClassType) {
             X10ParsedClassType ct = (X10ParsedClassType) t;
             if (ct.typeArguments() == null)
                 return ct;
             List<Type> newArgs = new ArrayList<Type>();
             for (Type at : ct.typeArguments()) {
                 Type at2 = subst(at, y, x);
                 newArgs.add(at2);
             }
             if (! newArgs.isEmpty())
                 return ct.typeArguments(newArgs);
         } else 
             if (c != null) {
                 c = c.copy();
                 base = subst(base, y, x);
 
                 try {
                     c = c.substitute(y, x);
                     //                  c = c.saturate();
                 }
                 catch (XFailure e) {
                     throw new SemanticException("Cannot instantiate formal parameters on actuals.");
                 }
 
                 return X10TypeMixin.xclause(base, c);
             }
 
 
         return t;
     }
 
     public static Type subst(Type t, XTerm[] y, XVar[] x, Type[] Y, ParameterType[] X) throws SemanticException {
         if (t instanceof ConstrainedType) {
            ConstrainedType ct = (ConstrainedType) t;
            Type base = Types.get(ct.baseType()); // do not call X10TypeMixin.baseType(ct); that will strip constraints in ct
            base = subst(base, y, x, Y, X);
            CConstraint c = Types.get(ct.constraint());
            c =  subst(c, y, x, Y, X);
           
            return X10TypeMixin.xclause(base, c);
         }
         if (t instanceof ParameterType) {
             for (int i = 0; i < X.length; i++) {
                 if (TypeParamSubst.isSameParameter((ParameterType) t, X[i]))
                     return Y[i];
             }
             return t;
         }
         if (t instanceof X10ClassType) {
             X10ClassType ct = (X10ClassType) t;
             if (ct.isIdentityInstantiation()) {
                 List<Type> args = new ArrayList<Type>();
                 boolean changed = false;
                 for (Type ti : ct.x10Def().typeParameters()) {
                     Type ti2 = subst(ti, y, x, Y, X);
                     if (ti2 != ti)
                         changed = true;
                     args.add(ti2);
                 }
                 if (changed)
                     return ct.typeArguments(args);
                 return ct;
             }
             else {
                 if (ct.typeArguments() == null)
                     return ct;
                 List<Type> args = new ArrayList<Type>();
                 boolean changed = false;
                 for (Type ti : ct.typeArguments()) {
                     Type ti2 = subst(ti, y, x, Y, X);
                     if (ti2 != ti)
                         changed = true;
                     args.add(ti2);
                 }
                 if (changed)
                     return ct.typeArguments(args);
                 return ct;
             }
         }
         if (t instanceof MacroType) {
             MacroType mt = (MacroType) t;
             return subst(mt.definedType(), y, x, Y, X);
         }
         return t;
     }
 
     public static Type subst(Type t, XTerm y, XVar x) throws SemanticException {
         return subst(t, new XTerm[] { y }, new XVar[] { x });
     }
 
     public static Type subst(Type t, Type Y, ParameterType X) throws SemanticException {
         return subst(t, new XTerm[] {}, new XVar[] { }, new Type[] { Y }, new ParameterType[] { X });
     }
 
     public static CConstraint subst(CConstraint t, XTerm y, XVar x) throws SemanticException {
         return subst(t, new XTerm[] { y }, new XVar[] { x }, new Type[0], new ParameterType[0]);
     }
 
     public static CConstraint subst(CConstraint t, Type Y, ParameterType X) throws SemanticException {
         return t;
     }
 
     public static TypeConstraint subst(TypeConstraint t, XTerm y, XVar x) throws SemanticException {
         if (t == null)
             return null;
         return t.subst(y, x);
     }
 
     public static TypeConstraint subst(TypeConstraint t, Type Y, ParameterType X) throws SemanticException {
         if (t == null)
             return null;
         TypeParamSubst subst = new TypeParamSubst((TypeSystem) Y.typeSystem(), Arrays.asList(Y), Arrays.asList(X));
         return subst.reinstantiate(t);
     }
 
     public static TypeConstraint subst(TypeConstraint t, XTerm[] y, XVar[] x) throws SemanticException {
         assert y.length == x.length;
 
         if (t == null)
             return null;
 
         for (int i = 0; i < x.length; i++)
             t = subst(t, y[i], x[i]);
 
         return t;
     }
 
     public static TypeConstraint subst(TypeConstraint t, Type[] Y, ParameterType[] X) throws SemanticException {
         assert Y.length == X.length;
 
         if (t == null)
             return null;
 
         for (int i = 0; i < X.length; i++) {
             if (Y[i] == null)
                 throw new SemanticException("Cannot infer type for parameter " + X[i] + ".");
             t = subst(t, Y[i], X[i]);
         }
 
         return t;
     }
 
     public static TypeConstraint subst(TypeConstraint t, XTerm[] y, XVar[] x, Type[] Y, ParameterType[] X) throws SemanticException {
         TypeConstraint t2 = subst(t, y, x);
         TypeConstraint t3 = subst(t2, Y, X);
         return t3;
     }
 
     public static CConstraint subst(CConstraint t, XTerm[] y, XVar[] x) throws SemanticException {
         if (t == null)
             return null;
 
         try {
             CConstraint c = t.substitute(y, x);
             return c;
         }
         catch (XFailure e) {
             throw new SemanticException("Cannot instantiate formal parameters on actuals.");
         }
     }
 
     public static CConstraint subst(CConstraint t, Type[] Y, ParameterType[] X) throws SemanticException {
         if (t == null)
             return null;
 
         return t;
     }
 
     public static CConstraint subst(CConstraint t, XTerm[] y, XVar[] x, Type[] Y, ParameterType[] X) throws SemanticException {
         CConstraint t2 = subst(t, y, x);
         CConstraint t3 = subst(t2, Y, X);
         return t3;
     }
 
     public static X10FieldInstance subst(X10FieldInstance fi, XTerm[] y, XVar[] x) throws SemanticException {
         Type ft = subst(fi.type(), y, x);
         Type rt = subst(fi.rightType(), y, x);
         StructType ct = (StructType) subst(fi.container(), y, x);
         return (X10FieldInstance) fi.type(ft, rt).container(ct);
     }
 
     public static X10FieldInstance subst(X10FieldInstance fi, XTerm y, XVar x) throws SemanticException {
         return subst(fi, new XTerm[] { y }, new XVar[] { x });
     }
 
     public static X10LocalInstance subst(X10LocalInstance li, XTerm[] y, XVar[] x) throws SemanticException {
         Type ft = subst(li.type(), y, x);
         return li.type(ft);
     }
 
     public static X10LocalInstance subst(X10LocalInstance li, XTerm y, XVar x) throws SemanticException {
         return subst(li, new XTerm[] { y }, new XVar[] { x });
     }
 
     /**
      * @param ci
      * @param y
      * @param x
      * @return
      * @throws SemanticException 
      */
     public static X10ConstructorInstance subst(X10ConstructorInstance ci, XTerm[] y, XVar[] x) throws SemanticException {
         Type returnType = ci.returnType();
         Type newReturnType = subst(returnType, y, x);
         if (newReturnType != returnType) {
             ci = ci.returnType(newReturnType);
         }
         List<Type> formalTypes = ci.formalTypes();
         List<Type> newFormalTypes = subst(formalTypes, y, x);
         if (newFormalTypes != formalTypes) {
             ci = ci.formalTypes(newFormalTypes);
         }
         StructType ct = (StructType) subst(ci.container(), y, x);
         if (ct != ci.container()) {
             ci =  (X10ConstructorInstance) ci.container(ct);
         }
         return ci;
     }
 
     public static X10ConstructorInstance subst(X10ConstructorInstance ci, XTerm y, XVar x) throws SemanticException {
         return subst(ci, new XTerm[] { y }, new XVar[] { x });
     }
 
     /**
      * @param ci
      * @param y
      * @param x
      * @return
      * @throws SemanticException 
      */
     public static X10MethodInstance subst(X10MethodInstance mi, XTerm[] y, XVar[] x) throws SemanticException {
         Type returnType = mi.returnType();
         Type newReturnType = subst(returnType, y, x);
         if (newReturnType != returnType) {
             mi = mi.returnType(newReturnType);
         }
         List<Type> formalTypes = mi.formalTypes();
         List<Type> newFormalTypes = subst(formalTypes, y, x);
         if (newFormalTypes != formalTypes) {
             mi = mi.formalTypes(newFormalTypes);
         }
         StructType ct = (StructType) subst(mi.container(), y, x);
         if (ct != mi.container()) {
             mi =  (X10MethodInstance) mi.container(ct);
         }
         return mi;
     }
 
     public static X10MethodInstance subst(X10MethodInstance mi, XTerm y, XVar x) throws SemanticException {
         return subst(mi, new XTerm[] { y }, new XVar[] { x });
     }
 
 }
