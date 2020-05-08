 // Copyright (c) 2011, Christopher Pavlina. All rights reserved.
 
 package me.pavlina.alco.codegen;
 import me.pavlina.alco.ast.*;
 import me.pavlina.alco.compiler.errors.*;
 import me.pavlina.alco.compiler.Env;
 import me.pavlina.alco.llvm.*;
 import me.pavlina.alco.language.Type;
 import me.pavlina.alco.language.Resolver;
 import me.pavlina.alco.lex.Token;
 import java.util.List;
 import java.util.ArrayList;
 
 /**
  * Assignment of call to multiple destinations. */
 public class AssignCall {
     Token token;
     List<Expression> dests;
     List<Type> types;
     List<Type> returns;
     OpCall call;
 
     public AssignCall (Token token) {
         this.token = token;
     }
 
     /**
      * Required: Set source
      * @param source OpCall. */
     public AssignCall source (OpCall source) {
         call = source;
         return this;
     }
 
     /**
      * Required: Set destinations
      * @param dests Expression. Can be one destination or an OpComma linking
      * them
      */
     public AssignCall dests (Expression dests) {
         this.dests = new ArrayList<Expression> ();
         if (OpComma.class.isInstance (dests))
             ((OpComma) dests).unpack (this.dests);
         else
             this.dests.add (dests);
         return this;
     }
 
     public void checkTypes (Env env, Resolver resolver) throws CError {
         // Check types
         types = new ArrayList<Type> (dests.size ());
         for (Expression i: dests) {
             i.checkTypes (env, resolver);
             if (!NullValue.class.isInstance (i))
                 i.checkPointer (true, token);
             types.add (i.getType ());
         }
 
         call.checkTypes_ (env, resolver);
 
         returns = call.getFunction ().getTypes ();
 
         // Make sure all returns are coercible, and allocate temporaries
         int temps = 1; // First is the ignore temp
         Type nullType = Type.getNull ();
         for (int i = 0; i < types.size (); ++i) {
             if (i == returns.size ()) break;
             if (returns.get (i).equals (types.get (i))) continue;
             if (types.get (i).equals (nullType)) {
                 // ignore
             } else {
                 Type.checkCoerce (returns.get (i), types.get (i), token);
                 if (i > 0)
                     ++temps;
             }
         }
 
         call.getMethod ().requireTemps (temps);
     }
 
     public void genLLVM (Env env, Emitter emitter, Function function) {
         List<Instruction> pointers = new ArrayList<Instruction> (dests.size ());
         for (Expression i: dests) {
             if (NullValue.class.isInstance (i))
                 pointers.add (null);
             else
                 pointers.add (i.getPointer (env, emitter, function));
         }
 
         // Process arguments
         List<Instruction> values = new ArrayList<Instruction> ();
         List<Type> destTypes = call.getFunction ().getArgTypes ();
         List<Expression> args = call.getArgs ();
         for (int i = 0; i < args.size (); ++i) {
             args.get (i).genLLVM (env, emitter, function);
             Instruction val = args.get (i).getInstruction ();
             Cast c = new Cast (token)
                 .value (val)
                 .type (args.get (i).getType ())
                 .dest (destTypes.get (i));
             c.genLLVM (env, emitter, function);
             values.add (c.getInstruction ());
         }
 
         // Prepare bitcasts of required temporaries (they're all i128*)
         List<Instruction> temps = new ArrayList<Instruction> ();
         Type nullType = Type.getNull ();
         // Offset temps to allow for the first (real) return
         temps.add (null);
         int tempsUsed = 0;
         for (int i = 1; i < returns.size (); ++i) {
             if (i >= types.size () || types.get (i).equals (nullType)) {
                 // Ignore
                 Instruction temp = new CONVERT ()
                     .op ("bitcast").stype ("i128*")
                     .dtype (LLVMType.getLLVMName (returns.get (i)) + "*")
                     .value ("%.T0");
                 function.add (temp);
                 temps.add (temp);
 
             } else if (returns.get (i).equalsNoQual (types.get (i))) {
                 temps.add (null);
 
             } else {
                 // Cast required
                 String dest = "%.T" + Integer.toString (tempsUsed + 1);
                 ++tempsUsed;
                 Instruction temp = new CONVERT ()
                     .op ("bitcast").stype ("i128*")
                     .dtype (LLVMType.getLLVMName (returns.get (i)) + "*")
                     .value (dest);
                 function.add (temp);
                 temps.add (temp);
             }
         }
 
         // Build the call
         CALL callbuilder = new CALL ()
             .type (LLVMType.getLLVMNameV (call.getType ()))
             .fun ("@" + call.getFunction ().getMangledName ());
         for (int i = 1; i < returns.size (); ++i) {
             if (temps.get (i) == null) {
                 // Return directly into pointer
                 callbuilder.arg (pointers.get (i));
 
             } else {
                 // Return into temp
                 callbuilder.arg (temps.get (i));
             }
         }
         for (int i = 0; i < args.size (); ++i) {
             callbuilder.arg (values.get (i));
         }
 
         // Cast and assign the first return value
         if (!types.get (0).equals (nullType) && returns.size () > 0) {
             Instruction val;
             if (types.get (0).equals (returns.get (0))) {
                 val = callbuilder;
             } else {
                 Cast c = new Cast (token)
                     .value (callbuilder).type (returns.get (0))
                     .dest (types.get (0));
                 c.genLLVM (env, emitter, function);
                 val = c.getInstruction ();
             }
             function.add (new STORE ()
                           .pointer (pointers.get (0))
                           .type (LLVMType.getLLVMName (types.get (0)))
                           .value (val)
                           ._volatile (returns.get (0).isVolatile ()));
         }
 
         // Cast and assign the subsequent return values
         for (int i = 1; i < returns.size (); ++i) {
             if (i == types.size ()) break;
            if (!temps.get (i).equals ("") &&
                 !types.get (i).equals (nullType)) {
                 Instruction tempVal = new LOAD ()
                     .type (LLVMType.getLLVMName (returns.get (i)))
                     .pointer (temps.get (i));
                 function.add (tempVal);
                 Cast c = new Cast (token)
                     .value (tempVal).type (returns.get (i))
                     .dest (types.get (i));
                 c.genLLVM (env, emitter, function);
                 function.add (new STORE ()
                               .pointer (pointers.get (i))
                               .type (LLVMType.getLLVMName (types.get (i)))
                               .value (c.getInstruction ())
                               ._volatile (returns.get (i).isVolatile ()));
             }
         }
     }
 
     public Instruction getInstruction () {
         return call.getInstruction ();
     }
 }
