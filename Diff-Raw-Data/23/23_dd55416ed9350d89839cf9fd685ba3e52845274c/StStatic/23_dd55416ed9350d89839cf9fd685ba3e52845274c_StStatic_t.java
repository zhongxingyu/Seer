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
 import static me.pavlina.alco.language.IntLimits.*;
 import me.pavlina.alco.llvm.*;
 import java.util.List;
 import java.util.ArrayList;
 import java.math.BigInteger;
 
 /**
  * Static variable assignment. Syntax:
  *  - ST := static {VARIABLE} = {expression} [ , {VARIABLE} = {expression} ]* ;
  *  - VARIABLE := {name} [ {type} ]?
  */
 public class StStatic extends Statement
 {
     Token token;
     List<String> names;
     List<String> realNames;
     List<Type> types;
     List<Expression> expressions;
     List<String> initialisers;
     boolean threadlocal, _volatile;
 
     public StStatic (Env env, TokenStream stream, Method method) throws CError {
         Token token;
 
         this.token = stream.next ();
         assert this.token.is (Token.WORD, "static");
 
         while (true) {
             if (stream.peek ().is (Token.WORD, "threadlocal")) {
                 threadlocal = true;
                 stream.next ();
             } else if (stream.peek ().is (Token.WORD, "volatile")) {
                 _volatile = true;
                 stream.next ();
             } else
                 break;
         }
 
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
         for (Expression i: expressions)
             i.checkTypes (env, resolver);
         for (int i = 0; i < names.size (); ++i) {
             if (types.get (i) == null) {
                 types.set (i, expressions.get (i).getType ());
             } else {
                 Type.checkCoerce (expressions.get (i),
                                   types.get (i), token);
             }
             if (_volatile)
                 types.set (i, types.get (i).getVolatile ());
 
             realNames.set
                 (i, resolver.addGlobalLocal
                  (names.get (i), types.get (i), token).getName ());
         }
 
         for (Expression i: expressions) {
             if (!IntValue.class.isInstance (i) &&
                 !RealValue.class.isInstance (i) &&
                 !NullValue.class.isInstance (i) &&
                 !BoolValue.class.isInstance (i)) {
 
                 throw CError.at ("static variable default must be primitive "
                                  + "literal or null", token);
             }
             i.checkTypes (env, resolver);
         }
         
         initialisers = new ArrayList<String> (names.size ());
         for (int i = 0; i < names.size (); ++i) {
             // Try casts. We have to do this manually here.
             // IntValue -> i8, i16, i32, i64, u8, u16, u32, u64
             // RealValue -> float, double
             if (IntValue.class.isInstance (expressions.get (i))) {
                 if (types.get (i).getEncoding () != Type.Encoding.UINT &&
                     types.get (i).getEncoding () != Type.Encoding.SINT) {
                     throw CError.at ("integer initialiser for non-integer",
                                      token);
                 }
                 BigInteger val = ((IntValue) expressions.get (i)).getValue ();
                 BigInteger min, max;
                 if (types.get (i).getEncoding () == Type.Encoding.UINT) {
                     min = BigInteger.ZERO;
                     if (types.get (i).getSize () == 1)
                         max = U8_MAX;
                     else if (types.get (i).getSize () == 2)
                         max = U16_MAX;
                     else if (types.get (i).getSize () == 4)
                         max = U32_MAX;
                     else
                         max = U64_MAX;
                 } else {
                     if (types.get (i).getSize () == 1) {
                         min = U8_MIN;
                         max = U8_MAX;
                     } else if (types.get (i).getSize () == 2) {
                         min = U16_MIN;
                         max = U16_MAX;
                     } else if (types.get (i).getSize () == 4) {
                         min = U32_MIN;
                         max = U32_MAX;
                     } else {
                         min = U64_MIN;
                         max = U64_MAX;
                     }
                 }
                 if (min.compareTo (val) > 0 || max.compareTo (val) < 0) {
                     throw CError.at ("invalid integer value for type", token);
                 }
                 initialisers.add (val.toString ());
             } else if (RealValue.class.isInstance (expressions.get (i))) {
                 if (types.get (i).getEncoding () != Type.Encoding.FLOAT)
                     throw CError.at ("float initialiser for non-float",
                                      token);
                 double val = ((RealValue) expressions.get (i)).getValue ();
                 if (types.get (i).getSize () == 4) {
                     // Convert double>float>double because LLVM expects
                     // float constants to be doubles representable exactly
                     // as floats (WTF)
                     float fval = (float) val;
                     initialisers.add
                         (String.format
                          ("0x%016x", Double.doubleToRawLongBits
                           ((double) fval)));
                 } else {
                     initialisers.add
                         (String.format
                          ("0x%016x", Double.doubleToRawLongBits (val)));
                 }
             } else if (BoolValue.class.isInstance (expressions.get (i))) {
                 if (types.get (i).getEncoding () != Type.Encoding.BOOL)
                     throw CError.at ("bool initialiser for non-bool",
                                      token);
                 boolean val = ((BoolValue) expressions.get (i)).getValue ();
                 initialisers.add (val ? "-1" : "0");
             } else {
                 initialisers.add ("zeroinitializer");
             }
         }
     }
 
     public void genLLVM (Env env, Emitter emitter, Function function) {
         String linkage = threadlocal ? "thread_local" : "internal";
 
         for (int i = 0; i < names.size (); ++i) {
             emitter.add
                 (new Global (realNames.get (i),
                              LLVMType.getLLVMName (types.get (i)),
                              initialisers.get (i),
                              linkage));
         }
     }
 
     public void print (java.io.PrintStream out) {
         out.println ("(static");
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
                     return new StStatic (env, stream, method);
                 }
             };
     }
 }
