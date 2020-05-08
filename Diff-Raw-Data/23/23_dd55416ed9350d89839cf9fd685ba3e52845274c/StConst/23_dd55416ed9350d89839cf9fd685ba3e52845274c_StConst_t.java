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
  * Const assignment. Syntax:
  *  - CONST := const {CONSTANT} = {expression} [ , {CONSTANT} = {expression} ]*;
  *  - CONSTANT := {name} [ {type} ]?
  */
 public class StConst extends Statement
 {
     private Token token;
     private List<String> names;
     private List<String> realNames;
     private List<Type> types;
     private List<Expression> expressions;
 
     public StConst (Env env, TokenStream stream, Method method) throws CError {
         Token token;
 
         this.token = stream.next ();
         assert this.token.is (Token.WORD, "const");
 
         names = new ArrayList<String> ();
         realNames = new ArrayList<String> ();
         types = new ArrayList<Type> ();
         expressions = new ArrayList<Expression> ();
 
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
             types.add (type);
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
                 types.set (i, expressions.get (i).getType ().getConst ());
             } else {
                 Type.checkCoerce (expressions.get (i),
                                   types.get (i).getConst (),
                                   token);
             }
             realNames.set
                 (i, resolver.addGlobalLocal
                  (names.get (i), types.get (i), token).getName ());
         }
             
     }
 
     public void genLLVM (Env env, Emitter emitter, Function function) {
         for (int i = 0; i < names.size (); ++i) {
 
             // According to the standard, a constant must be globally
             // accessible. It is assignable just once. If an assignment is
             // necessary, we declare an LLVM global; otherwise, we declare
             // an LLVM constant.
 
             if (IntValue.class.isInstance (expressions.get (i))
                 || RealValue.class.isInstance (expressions.get (i))
                 || BoolValue.class.isInstance (expressions.get (i))) {
                 
                 emitter.add
                     (new Constant (realNames.get (i),
                                    LLVMType.getLLVMName (types.get (i)),
                                    expressions.get (i).getInstruction ()
                                    .getId (),
                                    "internal"));
 
             } else if (NullValue.class.isInstance (expressions.get (i))) {
 
                 emitter.add
                     (new Constant (realNames.get (i),
                                    LLVMType.getLLVMName (types.get (i)),
                                    "zeroinitializer",
                                    "internal"));
 
             } else {
 
                 emitter.add
                     (new Global (realNames.get (i),
                                  LLVMType.getLLVMName (types.get (i)),
                                  "zeroinitializer",
                                  "internal"));
             
                 String initialisedVar =
                     "@.INITIALISED." + realNames.get (i).substring (1);
 
                 emitter.add
                     (new Global (initialisedVar,
                                  "i32", "0", "internal"));
 
                 Block Lassign = new Block ();
                 Block Lassigned = new Block ();
 
                 Instruction assigned = new CALL ()
                     .type ("i32").fun ("@llvm.atomic.swap.i32.p0i32")
                     .arg (LLVMType.getLLVMName (types.get (i)) + "*",
                           initialisedVar)
                     .arg ("i32", "1");
                 function.add (assigned);
 
                 function.add (new SWITCH ()
                               .value (assigned).dest (Lassigned)
                               .addDest ("0", Lassign));
 
                 expressions.get (i).genLLVM (env, emitter, function);
                 Type.Encoding enc = types.get (i).getEncoding ();
                 if (enc == Type.Encoding.UINT ||
                     enc == Type.Encoding.SINT ||
                     enc == Type.Encoding.FLOAT ||
                     enc == Type.Encoding.BOOL ||
                     enc == Type.Encoding.POINTER) {
                     // Simple assign
                     Instruction val = expressions.get (i).getInstruction ();
                     Cast c = new Cast (token)
                         .value (val).type (expressions.get (i).getType ())
                         .dest (types.get (i));
                     c.genLLVM (env, emitter, function);
                     val = c.getInstruction ();
                     function.add (new STORE ()
                                   .pointer (realNames.get (i))
                                   .value (val));
                 } else {
                     // Nonprimitive assign
                     Instruction val = expressions.get (i).getInstruction ();
                     function.add (new STORE ()
                                   .pointer (realNames.get (i))
                                   .value (val));
                 }
                 function.add (new BRANCH ().dest (Lassigned));
                 function.add (Lassigned);
             }
         }
     }
 
     public void print (java.io.PrintStream out) {
         out.println ("(const");
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
                     return new StConst (env, stream, method);
                 }
             };
     }
 }
