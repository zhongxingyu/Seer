 // Copyright (c) 2011, Christopher Pavlina. All rights reserved.
 
 package me.pavlina.alco.ast;
 import me.pavlina.alco.compiler.errors.*;
 import me.pavlina.alco.compiler.Env;
 import me.pavlina.alco.lex.TokenStream;
 import me.pavlina.alco.lex.Token;
 import me.pavlina.alco.language.Resolver;
 import me.pavlina.alco.language.IntLimits;
 import me.pavlina.alco.language.Type;
 import me.pavlina.alco.language.Keywords;
 import me.pavlina.alco.llvm.*;
 import java.util.List;
 import java.io.PrintStream;
 
 /**
  * Variable */
 public class NameValue extends Expression
 {
     Token token;
     Type type;
     String name, realName;
     Instruction instruction;
 
     /**
      * Create a NameValue from the stream */
     public NameValue (Env env, TokenStream stream) throws CError {
         token = stream.next ();
         if (token.is (Token.EXTRA, "$$name")) {
             // Extrastandard id $$name allows using anything as a name
             Token nextToken = stream.next ();
             if (nextToken.is (Token.NO_MORE))
                 throw UnexpectedEOF.after ("name", token);
             name = nextToken.value;
 
         } else {
             assert token.is (Token.WORD);
             if (Keywords.isKeyword (token.value, true))
                 throw Unexpected.at ("name", token);
            if (token.value.startsWith ("@"))
                throw Unexpected.at ("name", token);
             name = token.value;
         }
     }
 
     /**
      * Create a NameValue from a name */
     public NameValue (String name) {
         this.name = name;
     }
 
     public Instruction getInstruction () {
         return instruction;
     }
 
     public Token getToken () {
         return token;
     }
 
     public List<AST> getChildren () {
         return null;
     }
 
     public void checkTypes (Env env, Resolver resolver) throws CError {
         Resolver.Variable var = resolver.getVariable (name, token);
         realName = var.getName ();
         type = var.getType ().getNonLiteral ();
     }
 
     public Type getType () {
         return type;
     }
 
     public void genLLVM (Env env, Emitter emitter, Function function) {
         instruction = new LOAD ()
             .type (LLVMType.getLLVMName (type))
             .pointer (realName)
             ._volatile (type.isVolatile ());
         function.add (instruction);
     }
     
     public void print (PrintStream out) {
         out.print (name);
     }
 
     public String getName () {
         return name;
     }
 
     public String getRealName () {
         return realName;
     }
 
     public void checkPointer (boolean write, Token token) throws CError {
         if (type.isConst () && write)
             throw CError.at ("cannot assign to constant", token);
     }
 
     public Instruction getPointer (Env env, Emitter emitter, Function function)
     {
         return new Placeholder (realName, LLVMType.getLLVMName (type) + "*");
     }
 }
