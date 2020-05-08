 // Copyright (c) 2011, Christopher Pavlina. All rights reserved.
 
 package me.pavlina.alco.ast;
 import me.pavlina.alco.compiler.Env;
 import me.pavlina.alco.compiler.errors.*;
 import me.pavlina.alco.lex.TokenStream;
 import me.pavlina.alco.lex.Token;
 import me.pavlina.alco.language.Keywords;
 import me.pavlina.alco.language.Type;
 import me.pavlina.alco.language.HasType;
 import me.pavlina.alco.language.Resolver;
 import me.pavlina.alco.parse.ExpressionParser;
 import me.pavlina.alco.parse.TypeParser;
 import me.pavlina.alco.llvm.*;
 import me.pavlina.alco.codegen.Cast;
 import java.util.List;
 import java.util.ArrayList;
 
 /**
  * Variable assignment. Syntax:
  *  - LET := let {VARIABLE} = {expression} [ , {VARIABLE} = {expression} ]* ;
  *  - VARIABLE := {name} [ {type} ]?
  */
 public class StLet extends Statement
 {
     Token token;
     List<String> names;
     List<String> realNames;
     List<Type> types;
     List<Expression> expressions;
     Method method;
     boolean _volatile;
     List<Cast> casts;
 
     public StLet (Env env, TokenStream stream, Method method) throws CError {
         Token token;
 
         this.token = stream.next ();
         assert this.token.is (Token.WORD, "let");
 
         if (stream.peek ().is (Token.WORD, "volatile")) {
             stream.next ();
             _volatile = true;
         }
 
         this.method = method;
         names = new ArrayList<String> ();
         realNames = new ArrayList<String> ();
         types = new ArrayList<Type> ();
         expressions = new ArrayList<Expression> ();
         casts = new ArrayList<Cast> ();
 
         while (true) {
             String name;
             Type type;
             Expression value;
 
             // Name
             token = stream.next ();
             if (token.is (Token.NO_MORE))
                 throw UnexpectedEOF.after ("name", stream.last ());
             else if (!token.is (Token.WORD) ||
                 Keywords.isKeyword (token.value, true))
                 throw Unexpected.at ("name", token);
            else if (token.value.startsWith ("@"))
                throw Unexpected.at ("name", token);
             name = token.value;
 
             // Type and colon
             token = stream.next ();
             if (token.is (Token.NO_MORE))
                 throw UnexpectedEOF.after ("= or type", stream.last ());
             else if (token.is (Token.OPER, "="))
                 type = null;
             else {
                 stream.putback (token);
                 type = TypeParser.parse (stream, env);
                 token = stream.next ();
                 if (token.is (Token.NO_MORE))
                     throw UnexpectedEOF.after ("=", stream.last ());
                 else if (!token.is (Token.OPER, "="))
                     throw Unexpected.after ("=", stream.last ());
             }
 
             // Value
             value = ExpressionParser.parse (env, stream, method, ";,");
             if (value == null)
                 throw Unexpected.after ("expression", token);
 
             names.add (name);
             realNames.add (null);
             types.add (type == null ? null : type.getNonLiteral ());
             expressions.add (value);
             value.setParent (this);
 
             token = stream.next ();
             if (token.is (Token.OPER, ";"))
                 break;
             else if (!token.is (Token.OPER, ","))
                 throw Unexpected.after (", or ;", stream.last ());
 
         }
     }
 
     public Token getToken () {
         return token;
     }
 
     @SuppressWarnings("unchecked") // :-( I'm sorry
     public List<AST> getChildren () {
         return (List) expressions;
     }
 
     public void checkTypes (Env env, Resolver resolver) throws CError {
         for (Expression i: expressions) {
             i.checkTypes (env, resolver);
         }
 
         for (int i = 0; i < names.size (); ++i) {
             if (types.get (i) == null) {
                 types.set (i, expressions.get (i).getType ());
             }
             if (_volatile)
                 types.set (i, types.get (i).getVolatile ());
             realNames.set
                 (i, resolver.addVariable
                  (names.get (i), types.get (i), token).getName ());
             casts.add (new Cast (token)
                        .type (expressions.get (i).getType ())
                        .dest (types.get (i)));
             casts.get (i).checkTypes (env, resolver);
                        
             method.addAlloca (LLVMType.getLLVMName (types.get (i)),
                               realNames.get (i));
         }
             
     }
 
     public void genLLVM (Env env, Emitter emitter, Function function) {
         for (int i = 0; i < names.size (); ++i) {
             expressions.get (i).genLLVM (env, emitter, function);
             Type.Encoding enc = types.get (i).getEncoding ();
 
             // Simple assign
             if (enc == Type.Encoding.UINT ||
                 enc == Type.Encoding.SINT ||
                 enc == Type.Encoding.FLOAT ||
                 enc == Type.Encoding.POINTER ||
                 enc == Type.Encoding.BOOL) {
                 
                 Instruction val = expressions.get (i).getInstruction ();
                 Cast c = casts.get (i).value (val);
                 c.genLLVM (env, emitter, function);
                 function.add
                     (new STORE ()
                      .pointer (realNames.get (i))
                      .value (c.getInstruction ())
                      ._volatile (_volatile || types.get (i).isVolatile ()));
             }
 
             // Null assign
             else if (NullValue.class.isInstance (expressions.get (i))) {
                 function.add
                     (new STORE ()
                      .type ("i8*").value ("null").pointer (realNames.get (i))
                      ._volatile (_volatile || types.get (i).isVolatile ()));
             }
 
             // Obj/arr assign
             else if (enc == Type.Encoding.ARRAY ||
                      enc == Type.Encoding.OBJECT) {
                 Instruction val = expressions.get (i).getInstruction ();
                 function.add
                     (new STORE ()
                      .type ("i8*").value (val).pointer (realNames.get (i))
                      ._volatile (_volatile || types.get (i).isVolatile ()));
             }
         }
     }
 
     public void print (java.io.PrintStream out) {
         out.println ("(let");
         for (int i = 0; i < names.size (); ++i) {
             out.print ("  (");
             out.print (names.get (i));
             out.print (" ");
             expressions.get (i).print (out);
             out.println (")");
         }
         out.print (" )");
     }
 
     public static final Statement.StatementCreator CREATOR;
     static {
         CREATOR = new Statement.StatementCreator () {
                 public Statement create (Env env, TokenStream stream,
                                          Method method)
                     throws CError
                 {
                     return new StLet (env, stream, method);
                 }
             };
     }
 }
