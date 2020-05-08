 // Copyright (c) 2011, Christopher Pavlina. All rights reserved.
 
 package me.pavlina.alco.ast;
 import me.pavlina.alco.compiler.Env;
 import me.pavlina.alco.compiler.errors.*;
 import me.pavlina.alco.lex.Token;
 import me.pavlina.alco.lex.TokenStream;
 import me.pavlina.alco.language.Resolver;
 import me.pavlina.alco.language.Type;
 import me.pavlina.alco.language.HasType;
 import me.pavlina.alco.llvm.*;
 import java.util.List;
 import java.util.Arrays;
 
 /**
  * Addition operator.
  */
 public class OpPlus extends Expression.Operator {
     Token token;
     Expression[] children;
     Expression pointer;
     Expression integer;
     boolean pointerAdd;
     Type type;
     String valueString;
 
     public static Expression.OperatorCreator CREATOR;
 
 
     public OpPlus (Env env, TokenStream stream) throws CError {
         token = stream.next ();
         children = new Expression[2];
     }
 
     public int getPrecedence () {
         return me.pavlina.alco.language.Precedence.ADD;
     }
 
     public Expression.Associativity getAssociativity () {
         return Expression.Associativity.LEFT;
     }
 
     public Expression.Arity getArity () {
         return Expression.Arity.BINARY;
     }
 
     public void setOperands (Expression left, Expression right) {
         children[0] = left;
         children[1] = right;
     }
 
     public void checkTypes (Env env, Resolver resolver) throws CError {
         children[0].checkTypes (env, resolver);
         children[1].checkTypes (env, resolver);
 
         // Try checking for addition of (pointer + int) first
         boolean foundPointer = false, foundInt = false;
         for (int i = 0; i < 2; ++i) {
             if (children[i].getType ().getEncoding () ==
                 Type.Encoding.POINTER) {
                 foundPointer = true;
                 pointer = children[i];
 
             } else if (children[i].getType ().getEncoding () ==
                      Type.Encoding.UINT) {
                 if (children[i].getType ().getSize () > (env.getBits () / 8))
                     throw CError.at
                         ("cannot add pointer to wider integer", token);
                 children[i] = (Expression) Type.coerce
                     (children[i], new Type (env, "size", null),
                      OpCast.CASTCREATOR, env);
                 integer = children[i];
                 foundInt = true;
 
             } else if (children[i].getType ().getEncoding () ==
                        Type.Encoding.SINT) {
                 if (children[i].getType ().getSize () > (env.getBits () / 8))
                     throw CError.at
                         ("cannot add pointer to wider integer", token);
                 children[i] = (Expression) Type.coerce
                     (children[i], new Type (env, "ssize", null),
                      OpCast.CASTCREATOR, env);
                 integer = children[i];
                 foundInt = true;
             }
         }
         pointerAdd = (foundPointer && foundInt);
         if (pointerAdd) return;
 
         // Coercion: rank types by this list (see
         // Standard:Types:Casting:Coercion):
         //    FP64, FP32, UI64, UI32, UI16, UI8, SI64, SI32, SI16, SI8
         // Then apply implicit cast rules (note that this means that although
         // the list says SI64+UI8 yields UI8, this type of extreme precision
         // loss is not allowed by implicit cast rules.
         int[] ranks = new int[2];
         for (int i = 0; i < 2; ++i) {
             Type t = children[i].getType ();
             Type.Encoding enc = t.getEncoding ();
             int size = t.getSize ();
             if (enc == Type.Encoding.FLOAT) {
                 if (size == 4)      ranks[i] = 1;
                 else                ranks[i] = 2;
             } else if (enc == Type.Encoding.UINT) {
                 if (size == 8)      ranks[i] = 3;
                 else if (size == 4) ranks[i] = 4;
                 else if (size == 2) ranks[i] = 5;
                 else                ranks[i] = 6;
             } else if (enc == Type.Encoding.SINT) {
                 if (size == 8)      ranks[i] = 7;
                 else if (size == 4) ranks[i] = 8;
                 else if (size == 2) ranks[i] = 9;
                 else                ranks[i] = 10;
             } else {
                 throw CError.at ("invalid type for addition",
                                  children[i].getToken ());
             }
         }
 
         if (ranks[0] < ranks[1]) {
             // Coerce rhs to lhs
             children[1] = (Expression) Type.coerce
                 (children[1], children[0].getType (),
                  OpCast.CASTCREATOR, env);
 
         } else if (ranks[1] < ranks[0]) {
             // Coerce lhs to rhs
             children[0] = (Expression) Type.coerce
                 (children[0], children[1].getType (),
                  OpCast.CASTCREATOR, env);
 
         }
         // else: no coercion required
     }
 
     public String getValueString () {
         return valueString;
     }
 
     public Type getType () {
         return children[0].getType ();
     }
 
     public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
         if (pointerAdd) genLLVM_pointerAdd (env, emitter, function);
         else genLLVM_normal (env, emitter, function);
     }
 
     private void genLLVM_pointerAdd (Env env, LLVMEmitter emitter,
                                      Function function)
     {
         // Multiply the integer by the pointer width, then add it
         pointer.genLLVM (env, emitter, function);
         integer.genLLVM (env, emitter, function);
         String ptrV = pointer.getValueString ();
         String intV = integer.getValueString ();
 
         String ptrAsInt = new Conversion (emitter, function)
             .operation (Conversion.ConvOp.PTRTOINT)
             .source (LLVMType.getLLVMName (pointer.getType ()), ptrV)
             .dest (LLVMType.getLLVMName (integer.getType ()))
             .build ();
         String intByWidth = new Binary (emitter, function)
             .operation (Binary.BinOp.MUL)
             .type (LLVMType.getLLVMName (integer.getType ()))
             .operands (intV,
                       Integer.toString (pointer.getType ().getSize ()))
             .build ();
         String newPtrInt = new Binary (emitter, function)
             .operation (Binary.BinOp.ADD)
             .type (LLVMType.getLLVMName (integer.getType ()))
             .operands (intByWidth, ptrAsInt)
             .build ();
         valueString = new Conversion (emitter, function)
             .operation (Conversion.ConvOp.INTTOPTR)
             .source (LLVMType.getLLVMName (integer.getType ()), newPtrInt)
             .dest (LLVMType.getLLVMName (pointer.getType ()))
             .build ();
     }
 
     private void genLLVM_normal (Env env, LLVMEmitter emitter,
                                  Function function)
     {
         Binary.BinOp operation;
         Type.Encoding enc = children[0].getType ().getEncoding ();
         if (enc == Type.Encoding.FLOAT)
             operation = Binary.BinOp.FADD;
         else if (enc == Type.Encoding.SINT || enc == Type.Encoding.UINT)
             operation = Binary.BinOp.ADD;
         else
             throw new RuntimeException ("Adding unsupported times");
 
         children[0].genLLVM (env, emitter, function);
         children[1].genLLVM (env, emitter, function);
         String lhs = children[0].getValueString ();
         String rhs = children[1].getValueString ();
 
         valueString = new Binary (emitter, function)
             .operation (operation)
             .type (LLVMType.getLLVMName (children[0].getType ()))
             .operands (lhs, rhs)
             .build ();
     }
 
     @SuppressWarnings("unchecked")
     public List<AST> getChildren () {
         if (children == null)
             return null;
         else
             return (List) Arrays.asList (children);
     }
 
     public Token getToken () {
         return token;
     }
 
     public void print (java.io.PrintStream out) {
         out.println ("Add");
         children[0].print (out, 2);
         children[1].print (out, 2);
     }
 
     public void checkPointer (boolean write, Token token) throws CError {
         throw CError.at ("cannot assign to addition", token);
     }
 
     public String getPointer (Env env, LLVMEmitter emitter, Function function) {
         return null;
     }
 
     static {
         CREATOR = new Expression.OperatorCreator () {
                 public Operator create (Env env, TokenStream stream)
                     throws CError {
                     return new OpPlus (env, stream);
                 }
             };
     }
 }
