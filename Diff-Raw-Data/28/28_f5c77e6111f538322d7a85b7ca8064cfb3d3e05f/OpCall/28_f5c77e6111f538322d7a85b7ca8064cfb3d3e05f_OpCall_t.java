 // Copyright (c) 2011, Christopher Pavlina. All rights reserved.
 
 package me.pavlina.alco.ast;
 import me.pavlina.alco.language.Resolver;
 import me.pavlina.alco.language.Type;
 import me.pavlina.alco.language.HasType;
 import me.pavlina.alco.compiler.Env;
 import me.pavlina.alco.compiler.errors.*;
 import me.pavlina.alco.lex.Token;
 import me.pavlina.alco.llvm.LLVMEmitter;
 import me.pavlina.alco.llvm.LLVMType;
 import me.pavlina.alco.llvm.Function;
 import me.pavlina.alco.llvm.call;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 /**
  * Call operator. This does not parse, as it is always explicitly created from
  * a known name. */
 public class OpCall extends Expression.Operator {
     private Token token;
     private String valueString;
     private AST[] children;
     private List<Expression> args;
     private FunctionLike function;
 
     public OpCall (Token token, Expression expr) {
         this.token = token;
         children = new AST[] { expr, null };
     }
 
     public int getPrecedence () {
         return me.pavlina.alco.language.Precedence.CALL;
     }
 
     public Expression.Associativity getAssociativity () {
         return Expression.Associativity.LEFT;
     }
 
     public Expression.Arity getArity () {
         return Expression.Arity.UNARY;
     }
 
     public Type getType () {
         return function.getType ();
     }
 
     public void setOperands (Expression op, Expression ignore) {
         children[1] = op;
     }
 
     public void checkTypes (Env env, Resolver resolver) throws CError {
         if (!NameValue.class.isInstance (children[0])) {
             throw Unexpected.at ("name", children[0].getToken ());
         }
         args = new ArrayList<Expression> ();
         if (OpComma.class.isInstance (children[1])) {
             ((OpComma) children[1]).unpack (args);
         } else {
             args.add ((Expression) children[1]);
         }
        for (Expression i: args)
            i.checkTypes (env, resolver);
         List<Type> types = new ArrayList<Type> (args.size ());
         for (Expression i: args) {
             types.add (i.getType ());
         }
         function = resolver.getFunction
             (((NameValue) children[0]).getName (), types,
              children[0].getToken ());
 
         // Coerce all arguments to the proper types
         List<Type> destTypes = function.getArgTypes ();
         Type.CastCreator castcreator = new Type.CastCreator () {
                 public HasType cast (HasType value, Type type,
                                           Env env) {
                     return new OpCast (value, type, env);
                 }
             };
         for (int i = 0; i < args.size (); ++i) {
             args.set (i, (Expression) Type.coerce
                       (args.get (i), destTypes.get (i), castcreator, env));
         }
     }
 
     public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
         List<String> valueStrings = new ArrayList<String> ();
         for (Expression i: args) {
             i.genLLVM (env, emitter, function);
             valueStrings.add (i.getValueString ());
         }
         call callbuilder = new call
             (emitter, function)
             .type (LLVMType.getLLVMName (this.getType ()))
             .pointer ("@" + this.function.getMangledName ());
         for (int i = 0; i < args.size (); ++i) {
             callbuilder.arg (LLVMType.getLLVMName (args.get (i).getType ()),
                              valueStrings.get (i));
         }
         valueString = callbuilder.build ();
     }
         
 
     public String getValueString () {
         return valueString;
     }
 
     public void print (java.io.PrintStream out) {
         out.println ("Call");
         for (AST i: children) {
             i.print (out, 2);
         }
     }
 
     public List<AST> getChildren () {
         return Arrays.asList (children);
     }
 
     public Token getToken () {
         return token;
     }
 
     public void checkPointer (boolean write, Token token) throws CError {
         throw CError.at ("function call has no address", token);
     }
 
     public String getPointer (Env env, LLVMEmitter emitter, Function function) {
         return null;
     }
 
 }
