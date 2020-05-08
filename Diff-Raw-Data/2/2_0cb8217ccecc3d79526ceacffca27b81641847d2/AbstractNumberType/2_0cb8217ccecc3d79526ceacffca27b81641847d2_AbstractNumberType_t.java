 /*
  Copyright (c) 2011, The Staccato-Commons Team
 
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation; version 3 of the License.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.
  */
 package net.sf.staccatocommons.lang.number;
 
 import java.io.Serializable;
 
 import net.sf.staccatocommons.defs.function.Function;
 import net.sf.staccatocommons.defs.type.NumberType;
 import net.sf.staccatocommons.lang.function.AbstractFunction;
 import net.sf.staccatocommons.lang.function.AbstractFunction2;
 import net.sf.staccatocommons.restrictions.Constant;
 
 /**
  * @author flbulgarelli
  * 
  */
 public abstract class AbstractNumberType<A extends Number & Comparable> implements NumberType<A>, Serializable {
 
   private static final long serialVersionUID = -2727245678088637829L;
 
   public boolean isZero(A n) {
     return compare(n, zero()) == 0;
   }
 
   public boolean isNegative(A n) {
     return compare(n, zero()) < 0;
   }
 
   public boolean isPositive(A n) {
     return compare(n, zero()) > 0;
   }
 
   public A subtract(A n0, A n1) {
     return add(n0, negate(n1));
   }
 
   public int compare(A o1, A o2) {
     return o1.compareTo(o2);
   }
 
   public A increment(A n) {
     return add(n, one());
   }
 
   public A decrement(A n) {
     return subtract(n, one());
   }
 
   public A abs(A n) {
     return isNegative(n) ? negate(n) : n;
   }
 
   @Override
   @Constant
   public AbstractFunction2<A, A, A> add() {
     return new AbstractFunction2<A, A, A>() {
       public A apply(A arg0, A arg1) {
         return add(arg0, arg1);
       }
     };
   }
 
   @Override
   @Constant
   public AbstractFunction2<A, A, A> multiply() {
     return new Multiply<A>(this);
   }
 
   public Function<A, A> add(A n) {
     return add().apply(n);
   }
 
   public A inverse(A n) {
     return divide(one(), n);
   }
 
   @Constant
   public Function<A, A> negate() {
     return new AbstractFunction<A, A>() {
       public A apply(A arg) {
        return negate(arg);
       }
     };
   }
 
   @Constant
   public Function<A, A> abs() {
     return new AbstractFunction<A, A>() {
       public A apply(A arg) {
         return abs(arg);
       }
     };
   }
 
   @Constant
   public Function<A, A> inverse() {
     return new AbstractFunction<A, A>() {
       public A apply(A arg) {
         return inverse(arg);
       }
     };
   }
 
   private static final class Multiply<A> extends NumberTypeFunction2<A> {
 
     /**
      * Creates a new {@link Multiply}
      */
     public Multiply(NumberType<A> type) {
       super(type);
     }
 
     @Override
     public A apply(A arg0, A arg1) {
       return numberType().multiply(arg0, arg1);
     }
 
   }
 
   private abstract static class NumberTypeFunction2<A> extends AbstractFunction2<A, A, A> {
 
     private NumberType<A> numberType;
 
     /**
      * Creates a new {@link NumberTypeFunction2}
      */
     public NumberTypeFunction2(NumberType<A> type) {
       this.numberType = type;
     }
 
     public NumberType<A> numberType() {
       return numberType;
     }
   }
 }
