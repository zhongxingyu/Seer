 /*
  * IntegralTypeToken.java
  * Copyright (C) 2012 Pre-Alpha Software
  * All rights reserved.
  */
 
 package com.prealpha.diamond.compiler;
 
 import com.prealpha.diamond.compiler.node.ABinaryIntegralLiteral;
 import com.prealpha.diamond.compiler.node.ADecimalIntegralLiteral;
 import com.prealpha.diamond.compiler.node.AHexIntegralLiteral;
 import com.prealpha.diamond.compiler.node.AOctalIntegralLiteral;
 import com.prealpha.diamond.compiler.node.PIntegralLiteral;
 
 import java.math.BigInteger;
 
 enum IntegralTypeToken implements TypeToken {
     /*
      * Several methods depend on the order, so don't change it.
      */
     SIGNED_SHORT(15), UNSIGNED_SHORT(16), SIGNED_INT(31), UNSIGNED_INT(32), SIGNED_LONG(63), UNSIGNED_LONG(64);
 
     private final int width;
 
     private IntegralTypeToken(int width) {
         this.width = width;
     }
 
     @Override
     public boolean isReference() {
         return false;
     }
 
     @Override
     public boolean isNumeric() {
         return true;
     }
 
     @Override
     public int getWidth() {
         if (width % 2 == 0) {
             return width / 16;
         } else {
             return (width + 1) / 16;
         }
     }
 
     public boolean isSigned() {
         return (width % 2 != 0);
     }
 
     @Override
     public boolean isAssignableTo(TypeToken targetType) {
         if (!(targetType instanceof IntegralTypeToken)) {
             return false;
         } else {
             IntegralTypeToken integralTarget = (IntegralTypeToken) targetType;
             if (isSigned()) {
                 // signed types should only widen to other signed types
                 return (integralTarget.width >= this.width) && (integralTarget.isSigned());
             } else {
                 // unsigned types can widen to signed ones
                 return (integralTarget.width >= this.width);
             }
         }
     }
 
     @Override
     public TypeToken performBinaryOperation(TypeToken otherType) throws SemanticException {
         // go through each type; if both this and other are assignable to that type, return it
         for (IntegralTypeToken candidate : values()) {
             if (isAssignableTo(candidate) && otherType.isAssignableTo(candidate)) {
                 return candidate;
             }
         }
         throw new SemanticException(String.format("no type is a valid promotion for both %s and %s", this, otherType));
     }
 
     public IntegralTypeToken promoteToSigned() throws SemanticException {
         if (isSigned()) {
             return this;
         } else if ((ordinal() + 1) < values().length) {
             return values()[ordinal() + 1];
         } else {
             throw new SemanticException("unsigned long cannot be promoted to a signed type");
         }
     }
 
     @Override
     public String toString() {
         return name().toLowerCase().replace('_', ' ');
     }
 
     public static IntegralTypeToken fromLiteral(PIntegralLiteral literal) throws SemanticException {
         BigInteger value = parseLiteral(literal);
         for (IntegralTypeToken type : values()) {
            if (value.bitLength() <= type.width) {
                 return type;
             }
         }
         throw new SemanticException(literal, "integral literal too big");
     }
 
     public static BigInteger parseLiteral(PIntegralLiteral literal) throws SemanticException {
         if (literal instanceof ADecimalIntegralLiteral) {
             return new BigInteger(((ADecimalIntegralLiteral) literal).getDecimalLiteral().getText());
         } else if (literal instanceof AHexIntegralLiteral) {
             return new BigInteger(((AHexIntegralLiteral) literal).getHexLiteral().getText().substring(2), 16);
         } else if (literal instanceof AOctalIntegralLiteral) {
             return new BigInteger(((AOctalIntegralLiteral) literal).getOctalLiteral().getText(), 8);
         } else if (literal instanceof ABinaryIntegralLiteral) {
             return new BigInteger(((ABinaryIntegralLiteral) literal).getBinaryLiteral().getText().substring(2), 2);
         } else {
             throw new SemanticException(literal, "unknown integral literal flavor");
         }
     }
 }
