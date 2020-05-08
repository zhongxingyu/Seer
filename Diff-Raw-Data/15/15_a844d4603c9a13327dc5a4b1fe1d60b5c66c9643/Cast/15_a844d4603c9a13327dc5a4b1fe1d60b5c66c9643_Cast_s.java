 // Copyright (c) 2011, Christopher Pavlina. All rights reserved.
 
 package me.pavlina.alco.codegen;
 import me.pavlina.alco.language.Resolver;
 import me.pavlina.alco.language.Type;
 import me.pavlina.alco.compiler.Env;
 import me.pavlina.alco.compiler.errors.*;
 import me.pavlina.alco.lex.Token;
 import me.pavlina.alco.ast.Expression;
 import me.pavlina.alco.llvm.*;
 import java.math.BigInteger;
 import static me.pavlina.alco.language.IntLimits.*;
 
 public class Cast {
 
     Type srcT, dstT;
     String valueString, val;
     Token token;
     Expression expr;
 
     public Cast (Token token) {
         this.token = token;
     }
 
     /**
      * Required: Set value to cast. This is an LLVM value string. */
     public Cast value (String value) {
         this.val = value;
         return this;
     }
 
     /**
      * Required: Set type of value. */
     public Cast type (Type type) {
         this.srcT = type;
         return this;
     }
 
     /**
      * Required: Set destination type. */
     public Cast dest (Type dest) {
         this.dstT = dest;
         return this;
     }
 
     /**
      * Check types. */
     public void checkTypes (Env env, Resolver resolver) throws CError {
         Type.Encoding srcE = srcT.getEncoding ();
         Type.Encoding dstE = dstT.getEncoding ();
 
         // See Standard:Types:Casting:AllowedCasts
 
         // Integer literal cast
        if ((srcE == Type.Encoding.SINT || srcE == Type.Encoding.UINT) &&
            (dstE == Type.Encoding.SINT || dstE == Type.Encoding.SINT) &&
            srcT.getValue () != null) {
 
             BigInteger intVal = srcT.getValue ();
             BigInteger min, max;
 
             int size = dstT.getSize ();
             if (dstE == Type.Encoding.SINT) {
                 if (size == 8) {
                     min = I64_MIN;
                     max = I64_MAX;
                 } else if (size == 4) {
                     min = I32_MIN;
                     max = I32_MAX;
                 } else if (size == 2) {
                     min = I16_MIN;
                     max = I16_MAX;
                 } else {
                     min = I8_MIN;
                     max = I8_MAX;
                 }
             } else {
                 min = BigInteger.ZERO;
                 if (size == 8)
                     max = U64_MAX;
                 else if (size == 4)
                     max = U32_MAX;
                 else if (size == 2)
                     max = U16_MAX;
                 else
                     max = U8_MAX;
             }
             if (min.compareTo (intVal) > 0 ||
                 max.compareTo (intVal) < 0) {
                 throw CError.at ("integer literal outside range for type",
                                  expr.getToken ());
             }
 
             return;
         }
 
        if (srcT.equalsNoConst (dstT)) {
             // T to T
             // OK
    
         } else if (((srcE == Type.Encoding.SINT && dstE == Type.Encoding.UINT)||
                     (srcE == Type.Encoding.UINT && dstE == Type.Encoding.SINT))
                    && (srcT.getSize () != dstT.getSize ())) {
             throw CError.at ("cannot cast integer in both sign and width;\n" +
                              "sign and width casts are not commutative",
                              token);
 
         } else if (srcE == Type.Encoding.SINT && dstE == Type.Encoding.UINT) {
             // SI to UI
             // OK
 
         } else if (srcE == Type.Encoding.UINT && dstE == Type.Encoding.SINT) {
             // UI to SI
             // OK
 
         } else if (srcE == Type.Encoding.SINT && dstE == Type.Encoding.SINT
                    && srcT.getSize () < dstT.getSize ()) {
             // SI to SI
             // OK
 
         } else if (srcE == Type.Encoding.SINT && dstE == Type.Encoding.SINT
                    && srcT.getSize () > dstT.getSize ()) {
             // SI to SI
             // OK
 
         } else if (srcE == Type.Encoding.UINT && dstE == Type.Encoding.UINT
                    && srcT.getSize () < dstT.getSize ()) {
             // UI to UI
             // OK
 
         } else if (srcE == Type.Encoding.UINT && dstE == Type.Encoding.UINT
                    && srcT.getSize () > dstT.getSize ()) {
             // UI to UI
             // OK
 
         } else if ((srcE == Type.Encoding.UINT || srcE == Type.Encoding.SINT) &&
                    dstE == Type.Encoding.BOOL) {
             // SI/UI to B
             // OK
 
         } else if ((dstE == Type.Encoding.UINT || dstE == Type.Encoding.SINT) &&
                    srcE == Type.Encoding.BOOL) {
             // B to SI/UI
             // OK
 
         } else if (srcE == Type.Encoding.SINT && dstE == Type.Encoding.FLOAT) {
             // SI to FP
             // OK
 
         } else if (srcE == Type.Encoding.FLOAT && dstE == Type.Encoding.SINT) {
             // FP to SI
             // OK
         
         } else if (srcE == Type.Encoding.UINT && dstE == Type.Encoding.FLOAT) {
             // UI to FP
             // OK
 
         } else if (srcE == Type.Encoding.FLOAT && dstE == Type.Encoding.UINT) {
             // FP to UI
             // OK
 
         } else if (srcE == Type.Encoding.FLOAT && dstE == Type.Encoding.FLOAT
                    && srcT.getSize () < dstT.getSize ()) {
             // FP to FP
             // OK
 
         } else if (srcE == Type.Encoding.FLOAT && dstE == Type.Encoding.FLOAT
                    && srcT.getSize () > dstT.getSize ()) {
             // FP to FP
             // OK
 
         } else if (srcE == Type.Encoding.POINTER &&
                    dstE == Type.Encoding.POINTER) {
             // T* to U*
             // OK
 
         } else if (srcE == Type.Encoding.POINTER && dstE == Type.Encoding.BOOL){
             // T* to B
             // OK
 
         } else if (srcE == Type.Encoding.POINTER &&
                    dstE == Type.Encoding.UINT &&
                    dstT.getSize () >= (env.getBits () / 8)) {
             // T* to UI
             // OK
 
         } else if (srcE == Type.Encoding.UINT &&
                    dstE == Type.Encoding.POINTER &&
                    srcT.getSize () >= (env.getBits () / 8)) {
             // UI to T*
             // OK
 
         } else if (srcE == Type.Encoding.UINT &&
                    dstE == Type.Encoding.POINTER) {
             throw CError.at ("invalid cast: narrow integer to pointer", token);
 
         } else if (srcE == Type.Encoding.SINT &&
                    dstE == Type.Encoding.POINTER) {
             throw CError.at ("invalid cast: signed integer to pointer", token);
 
         } else if (srcE == Type.Encoding.POINTER &&
                    dstE == Type.Encoding.UINT) {
             throw CError.at ("invalid cast: pointer to narrow integer", token);
 
         } else if (srcE == Type.Encoding.POINTER &&
                    dstE == Type.Encoding.SINT) {
             throw CError.at ("invalid cast: pointer to signed integer", token);
 
         } else if (srcE == Type.Encoding.ARRAY &&
                    dstE == Type.Encoding.POINTER &&
                    srcT.getSubtype ().equals (dstT.getSubtype ())) {
             // T[] to T*
             throw new RuntimeException ("NOT IMPLEMENTED YET");
 
         } else if (srcE == Type.Encoding.ARRAY &&
                    dstE == Type.Encoding.BOOL) {
             // T[] to B
             throw new RuntimeException ("NOT IMPLEMENTED YET");
 
         } else if (srcE == Type.Encoding.NULL &&
                    dstE == Type.Encoding.SINT) {
             // null to SI
             // OK
 
         } else if (srcE == Type.Encoding.NULL &&
                    dstE == Type.Encoding.UINT) {
             // null to UI
             // OK
 
         } else if (srcE == Type.Encoding.NULL &&
                    dstE == Type.Encoding.OBJECT) {
             // null to class
             // OK
 
         } else if (srcE == Type.Encoding.NULL &&
                    dstE == Type.Encoding.ARRAY) {
             // null to T[]
             // OK
 
         } else if (srcE == Type.Encoding.NULL &&
                    dstE == Type.Encoding.POINTER) {
             // null to T*
             // OK
 
         } else if (srcE == Type.Encoding.NULL &&
                    dstE == Type.Encoding.BOOL) {
             // null to B
             // OK
 
         } else {
             throw CError.at ("invalid cast: " + srcT.toString ()
                              + " to " + dstT.toString (), token);
         }
     }
 
     public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
         String sty = LLVMType.getLLVMName (srcT);
         String dty = LLVMType.getLLVMName (dstT);
         Type.Encoding srcE = srcT.getEncoding ();
         Type.Encoding dstE = dstT.getEncoding ();
 
         // See Standard:Types:Casting:AllowedCasts
 
         // Integer literal cast
         if ((srcE == Type.Encoding.SINT || srcE == Type.Encoding.UINT) &&
            (dstE == Type.Encoding.SINT || dstE == Type.Encoding.SINT) &&
             srcT.getValue () != null) {
             BigInteger intVal = srcT.getValue ();
             valueString = intVal.toString ();
             return;
         }
 
        if (srcT.equalsNoConst (dstT)) {
             // T to T
             valueString = val;
    
         } else if (srcE == Type.Encoding.SINT && dstE == Type.Encoding.UINT) {
             // SI to UI
             valueString = val;
 
         } else if (srcE == Type.Encoding.UINT && dstE == Type.Encoding.SINT) {
             // UI to SI
             valueString = val;
 
         } else if (srcE == Type.Encoding.SINT && dstE == Type.Encoding.SINT
                    && srcT.getSize () < dstT.getSize ()) {
             // SI to SI
             valueString = new Conversion (emitter, function)
                 .operation (Conversion.ConvOp.SEXT)
                 .source (sty, val).dest (dty).build ();
 
         } else if (srcE == Type.Encoding.SINT && dstE == Type.Encoding.SINT
                    && srcT.getSize () > dstT.getSize ()) {
             // SI to SI
             valueString = new Conversion (emitter, function)
                 .operation (Conversion.ConvOp.TRUNC)
                 .source (sty, val).dest (dty).build ();
 
         } else if (srcE == Type.Encoding.UINT && dstE == Type.Encoding.UINT
                    && srcT.getSize () < dstT.getSize ()) {
             // UI to UI
             valueString = new Conversion (emitter, function)
                 .operation (Conversion.ConvOp.ZEXT)
                 .source (sty, val).dest (dty).build ();
 
         } else if (srcE == Type.Encoding.UINT && dstE == Type.Encoding.UINT
                    && srcT.getSize () > dstT.getSize ()) {
             // UI to UI
             valueString = new Conversion (emitter, function)
                 .operation (Conversion.ConvOp.TRUNC)
                 .source (sty, val).dest (dty).build ();
 
         } else if ((srcE == Type.Encoding.UINT || srcE == Type.Encoding.SINT) &&
                    dstE == Type.Encoding.BOOL) {
             // SI/UI to B
             String isZero = new icmp (emitter, function)
                 .comparison (icmp.Icmp.EQ)
                 .type (sty).operands (val, "0").build ();
             valueString = new select (emitter, function)
                 .cond (isZero)
                 .type (dty)
                 .values ("0", "-1")
                 .build ();
 
         } else if ((dstE == Type.Encoding.UINT || dstE == Type.Encoding.SINT) &&
                    srcE == Type.Encoding.BOOL) {
             // B to SI/UI
             if (srcT.getSize () < dstT.getSize ()) {
                 valueString = new Conversion (emitter, function)
                     .operation (Conversion.ConvOp.SEXT)
                     .source (sty, val).dest (dty).build ();
             } else {
                 valueString = val;
             }
 
         } else if (srcE == Type.Encoding.SINT && dstE == Type.Encoding.FLOAT) {
             // SI to FP
             valueString = new Conversion (emitter, function)
                 .operation (Conversion.ConvOp.SITOFP)
                 .source (sty, val).dest (dty).build ();
 
         } else if (srcE == Type.Encoding.FLOAT && dstE == Type.Encoding.SINT) {
             // FP to SI
             valueString = new Conversion (emitter, function)
                 .operation (Conversion.ConvOp.FPTOSI)
                 .source (sty, val).dest (dty).build ();
         
         } else if (srcE == Type.Encoding.UINT && dstE == Type.Encoding.FLOAT) {
             // UI to FP
             valueString = new Conversion (emitter, function)
                 .operation (Conversion.ConvOp.UITOFP)
                 .source (sty, val).dest (dty).build ();
 
         } else if (srcE == Type.Encoding.FLOAT && dstE == Type.Encoding.UINT) {
             // FP to UI
             valueString = new Conversion (emitter, function)
                 .operation (Conversion.ConvOp.FPTOUI)
                 .source (sty, val).dest (dty).build ();
 
         } else if (srcE == Type.Encoding.FLOAT && dstE == Type.Encoding.FLOAT
                    && srcT.getSize () < dstT.getSize ()) {
             // FP to FP
             valueString = new Conversion (emitter, function)
                 .operation (Conversion.ConvOp.FPEXT)
                 .source (sty, val).dest (dty).build ();
 
         } else if (srcE == Type.Encoding.FLOAT && dstE == Type.Encoding.FLOAT
                    && srcT.getSize () > dstT.getSize ()) {
             // FP to FP
             if (val.startsWith ("0x")) {
                 // Raw double value, casting to float. We can convert this here,
                 // rather than emitting an fptrunc. I checked, and no, LLVM does
                 // not optimise it out into the assembly. (I haven't checked
                 // the different options for OPT)
 
 
                 // FUCK YOU JAVA
                 try {
                     String half1 = val.substring (2, 10);
                     String half2 = val.substring (10, 18);
                     long bits = (Long.parseLong (half1, 16) << 32) |
                         Long.parseLong (half2, 16);
                     double valD = Double.longBitsToDouble (bits);
                     bits = Double.doubleToRawLongBits ((float) valD);
                     valueString = String.format ("0x%016x", bits);
                     return;
                 } catch (NumberFormatException e) {}
                 catch (IndexOutOfBoundsException e) {}
             }
             valueString = new Conversion (emitter, function)
                 .operation (Conversion.ConvOp.FPTRUNC)
                 .source (sty, val).dest (dty).build ();
 
         } else if (srcE == Type.Encoding.POINTER &&
                    dstE == Type.Encoding.POINTER) {
             // T* to U*
             valueString = new Conversion (emitter, function)
                 .operation (Conversion.ConvOp.BITCAST)
                 .source (sty, val).dest (dty).build ();
 
         } else if (srcE == Type.Encoding.POINTER &&
                    dstE == Type.Encoding.BOOL) {
             // T* to B
             String intermedT = "i" + Integer.toString (env.getBits ());
             String ptrAsInt = new Conversion (emitter, function)
                 .operation (Conversion.ConvOp.PTRTOINT)
                 .source (sty, val).dest (intermedT).build ();
             String isZero = new icmp (emitter, function)
                 .comparison (icmp.Icmp.EQ)
                 .type (intermedT).operands (ptrAsInt, "0").build ();
             valueString = new select (emitter, function)
                 .cond (isZero)
                 .type (dty).values ("0", "-1").build ();
 
         } else if (srcE == Type.Encoding.POINTER &&
                    dstE == Type.Encoding.UINT &&
                    dstT.getSize () >= (env.getBits () / 8)) {
             // T* to UI
             valueString = new Conversion (emitter, function)
                 .operation (Conversion.ConvOp.PTRTOINT)
                 .source (sty, val).dest (dty).build ();
 
         } else if (srcE == Type.Encoding.UINT &&
                    dstE == Type.Encoding.POINTER &&
                    srcT.getSize () >= (env.getBits () / 8)) {
             // UI to T*
             valueString = new Conversion (emitter, function)
                 .operation (Conversion.ConvOp.INTTOPTR)
                 .source (sty, val).dest (dty).build ();
 
         } else if (srcE == Type.Encoding.ARRAY &&
                    dstE == Type.Encoding.POINTER &&
                    srcT.getSubtype ().equals (dstT.getSubtype ())) {
             // T[] to T*
             throw new RuntimeException ("NOT IMPLEMENTED YET");
 
         } else if (srcE == Type.Encoding.NULL &&
                    dstE == Type.Encoding.SINT) {
             // null to SI
             valueString = "0";
 
         } else if (srcE == Type.Encoding.NULL &&
                    dstE == Type.Encoding.UINT) {
             // null to UI
             valueString = "0";
 
         } else if (srcE == Type.Encoding.NULL &&
                    dstE == Type.Encoding.OBJECT) {
             // null to class
             valueString = val;
 
         } else if (srcE == Type.Encoding.NULL &&
                    dstE == Type.Encoding.ARRAY) {
             // null to T[]
             valueString = val;
 
         } else if (srcE == Type.Encoding.NULL &&
                    dstE == Type.Encoding.POINTER) {
             // null to T*
             valueString = "null";
 
         } else if (srcE == Type.Encoding.NULL &&
                    dstE == Type.Encoding.BOOL) {
             valueString = "0";
 
         } else {
             throw new RuntimeException ("Invalid cast in genLLVM");
         }
     }
 
     public String getValueString () {
         return valueString;
     }
 }
