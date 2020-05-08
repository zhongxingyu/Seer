 // Copyright (c) 2011, Christopher Pavlina. All rights reserved.
 
 package me.pavlina.alco.ast;
 import me.pavlina.alco.compiler.Env;
 import me.pavlina.alco.compiler.errors.*;
 import me.pavlina.alco.lex.Token;
 import me.pavlina.alco.lex.TokenStream;
 import me.pavlina.alco.language.Resolver;
 import me.pavlina.alco.language.Type;
 import me.pavlina.alco.llvm.LLVMEmitter;
 import me.pavlina.alco.llvm.Function;
 import java.util.List;
 import java.util.Arrays;
 
 /**
  * Tuple operator. This joins expressions for use in calls and multiple
  * assigns. It should never be left at the genLLVM() stage.
  */
 public class OpComma extends Expression.Operator {
     private Token token;
     private Expression[] children;
 
     public static Expression.OperatorCreator CREATOR;
 
 
     public OpComma (Env env, TokenStream stream) throws CError {
         token = stream.next ();
         children = new Expression[2];
     }
 
     /**
      * Placeholder empty tuple (for void function calls) */
     public OpComma (Token token) {
         this.token = token;
     }
 
     public int getPrecedence () {
         return me.pavlina.alco.language.Precedence.COMMA;
     }
 
     public Expression.Associativity getAssociativity () {
         return Expression.Associativity.LEFT;
     }
 
     public Expression.Arity getArity () {
         return Expression.Arity.BINARY;
     }
 
     public void setOperands (Expression left, Expression right) {
         if (children == null) return;
         children[0] = left;
         children[1] = right;
     }
 
     public void checkTypes (Env env, Resolver resolver) throws CError {
        if (children == null) return;
        children[0].checkTypes (env, resolver);
        children[1].checkTypes (env, resolver);
     }
 
     public String getValueString () {
         throw new RuntimeException ("OpComma still exists at codegen");
     }
 
     public Type getType () {
         throw new RuntimeException ("OpComma still exists at codegen");
     }
 
     public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
         throw new RuntimeException ("OpComma still exists at codegen");
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
         if (children == null) {
             out.println ("Empty tuple");
         } else {
             out.println ("Tuple");
             children[0].print (out, 2);
             children[1].print (out, 2);
         }
     }
 
     public void unpack (List<Expression> into) {
         if (children == null) return;
         if (OpComma.class.isInstance (children[0]))
             ((OpComma) children[0]).unpack (into);
         else
             into.add (children[0]);
         into.add (children[1]);
     }
 
     public void checkPointer (boolean write, Token token) throws CError {
         throw new RuntimeException ("OpComma still exists at codegen");
     }
 
     public String getPointer (Env env, LLVMEmitter emitter, Function function) {
         return null;
     }
 
     static {
         CREATOR = new Expression.OperatorCreator () {
                 public Operator create (Env env, TokenStream stream)
                     throws CError {
                     return new OpComma (env, stream);
                 }
             };
     }
 }
