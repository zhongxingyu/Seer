 /**
  * Copyright (c) 2012-2013 André Bargull
  * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
  *
  * <https://github.com/anba/es6draft>
  */
 package com.github.anba.es6draft.runtime.objects;
 
 import static com.github.anba.es6draft.runtime.AbstractOperations.ToNumber;
 import static com.github.anba.es6draft.runtime.AbstractOperations.ToUint32;
 import static com.github.anba.es6draft.runtime.internal.Properties.createProperties;
 
 import org.mozilla.javascript.MathImpl;
 
 import com.github.anba.es6draft.runtime.ExecutionContext;
 import com.github.anba.es6draft.runtime.Realm;
 import com.github.anba.es6draft.runtime.internal.Initialisable;
 import com.github.anba.es6draft.runtime.internal.Properties.Attributes;
 import com.github.anba.es6draft.runtime.internal.Properties.Function;
 import com.github.anba.es6draft.runtime.internal.Properties.Optional;
 import com.github.anba.es6draft.runtime.internal.Properties.Value;
 import com.github.anba.es6draft.runtime.types.Intrinsics;
 import com.github.anba.es6draft.runtime.types.builtins.OrdinaryObject;
 
 /**
  * <h1>15 Standard Built-in ECMAScript Objects</h1><br>
  * <h2>15.8 The Math Object</h2>
  * <ul>
  * <li>15.8.1 Value Properties of the Math Object
  * <li>15.8.2 Function Properties of the Math Object
  * </ul>
  */
 public class MathObject extends OrdinaryObject implements Initialisable {
     public MathObject(Realm realm) {
         super(realm);
     }
 
     @Override
     public void initialise(ExecutionContext cx) {
         setPrototype(cx.getIntrinsic(Intrinsics.ObjectPrototype));
 
         createProperties(this, cx, ValueProperties.class);
         createProperties(this, cx, FunctionProperties.class);
     }
 
     /**
      * 15.8.1 Value Properties of the Math Object
      */
     public enum ValueProperties {
         ;
 
         /**
          * 15.8.1.1 Math.E
          */
         @Value(name = "E", attributes = @Attributes(writable = false, enumerable = false,
                 configurable = false))
         public static final Double E = Math.E;
 
         /**
          * 15.8.1.2 Math.LN10
          */
         @Value(name = "LN10", attributes = @Attributes(writable = false, enumerable = false,
                 configurable = false))
         public static final Double LN10 = Math.log(10d);
 
         /**
          * 15.8.1.3 Math.LN2
          */
         @Value(name = "LN2", attributes = @Attributes(writable = false, enumerable = false,
                 configurable = false))
         public static final Double LN2 = Math.log(2d);
 
         /**
          * 15.8.1.4 Math.LOG2E
          */
         @Value(name = "LOG2E", attributes = @Attributes(writable = false, enumerable = false,
                 configurable = false))
         public static final Double LOG2E = 1d / Math.log(2d);
 
         /**
          * 15.8.1.5 Math.LOG10E
          */
         @Value(name = "LOG10E", attributes = @Attributes(writable = false, enumerable = false,
                 configurable = false))
         public static final Double LOG10E = Math.log10(Math.E);
 
         /**
          * 15.8.1.6 Math.PI
          */
         @Value(name = "PI", attributes = @Attributes(writable = false, enumerable = false,
                 configurable = false))
         public static final Double PI = Math.PI;
 
         /**
          * 15.8.1.7 Math.SQRT1_2
          */
         @Value(name = "SQRT1_2", attributes = @Attributes(writable = false, enumerable = false,
                 configurable = false))
         public static final Double SQRT1_2 = Math.sqrt(.5d);
 
         /**
          * 15.8.1.8 Math.SQRT2
          */
         @Value(name = "SQRT2", attributes = @Attributes(writable = false, enumerable = false,
                 configurable = false))
         public static final Double SQRT2 = Math.sqrt(2d);
     }
 
     /**
      * 15.8.2 Function Properties of the Math Object
      */
     public enum FunctionProperties {
         ;
 
         /**
          * 15.8.2.1 Math.abs (x)
          */
         @Function(name = "abs", arity = 1)
         public static Object abs(ExecutionContext cx, Object thisValue, Object x) {
             return Math.abs(ToNumber(cx, x));
         }
 
         /**
          * 15.8.2.2 Math.acos (x)
          */
         @Function(name = "acos", arity = 1)
         public static Object acos(ExecutionContext cx, Object thisValue, Object x) {
             return Math.acos(ToNumber(cx, x));
         }
 
         /**
          * 15.8.2.3 Math.asin (x)
          */
         @Function(name = "asin", arity = 1)
         public static Object asin(ExecutionContext cx, Object thisValue, Object x) {
             return Math.asin(ToNumber(cx, x));
         }
 
         /**
          * 15.8.2.4 Math.atan (x)
          */
         @Function(name = "atan", arity = 1)
         public static Object atan(ExecutionContext cx, Object thisValue, Object x) {
             return Math.atan(ToNumber(cx, x));
         }
 
         /**
          * 15.8.2.5 Math.atan2 (y, x)
          */
         @Function(name = "atan2", arity = 2)
         public static Object atan2(ExecutionContext cx, Object thisValue, Object y, Object x) {
             return Math.atan2(ToNumber(cx, y), ToNumber(cx, x));
         }
 
         /**
          * 15.8.2.6 Math.ceil (x)
          */
         @Function(name = "ceil", arity = 1)
         public static Object ceil(ExecutionContext cx, Object thisValue, Object x) {
             return Math.ceil(ToNumber(cx, x));
         }
 
         /**
          * 15.8.2.7 Math.cos (x)
          */
         @Function(name = "cos", arity = 1)
         public static Object cos(ExecutionContext cx, Object thisValue, Object x) {
             return Math.cos(ToNumber(cx, x));
         }
 
         /**
          * 15.8.2.8 Math.exp (x)
          */
         @Function(name = "exp", arity = 1)
         public static Object exp(ExecutionContext cx, Object thisValue, Object x) {
             return Math.exp(ToNumber(cx, x));
         }
 
         /**
          * 15.8.2.9 Math.floor (x)
          */
         @Function(name = "floor", arity = 1)
         public static Object floor(ExecutionContext cx, Object thisValue, Object x) {
             return Math.floor(ToNumber(cx, x));
         }
 
         /**
          * 15.8.2.10 Math.log (x)
          */
         @Function(name = "log", arity = 1)
         public static Object log(ExecutionContext cx, Object thisValue, Object x) {
             return Math.log(ToNumber(cx, x));
         }
 
         /**
          * 15.8.2.11 Math.max ( [ value1 [ , value2 [ , ... ] ] ] )
          */
         @Function(name = "max", arity = 2)
         public static Object max(ExecutionContext cx, Object thisValue, Object... values) {
             double max = Double.NEGATIVE_INFINITY;
             for (Object value : values) {
                 double v = ToNumber(cx, value);
                 // Do not call `Double.compare(v, max)` (parameter order!), to handle NaN properly
                 // N.B. Double.compare() includes all necessary checks to be compliant to Abstract
                 // Relational Comparison Algorithm (11.8.1)
                 if (Double.compare(max, v) != 1) {
                     max = v;
                 }
             }
             return max;
         }
 
         /**
          * 15.8.2.12 Math.min ( [ value1 [ , value2 [ , ... ] ] ] )
          */
         @Function(name = "min", arity = 2)
         public static Object min(ExecutionContext cx, Object thisValue, Object... values) {
             double min = Double.POSITIVE_INFINITY;
             for (Object value : values) {
                 double v = ToNumber(cx, value);
                 // Do not call `Double.compare(-v, -min)` (parameter order!), to handle NaN properly
                 // N.B. Double.compare() includes all necessary checks to be compliant to Abstract
                 // Relational Comparison Algorithm (11.8.1)
                 if (Double.compare(-min, -v) != 1) {
                     min = v;
                 }
             }
             return min;
         }
 
         /**
          * 15.8.2.13 Math.pow (x, y)
          */
         @Function(name = "pow", arity = 2)
         public static Object pow(ExecutionContext cx, Object thisValue, Object x, Object y) {
             return Math.pow(ToNumber(cx, x), ToNumber(cx, y));
         }
 
         /**
          * 15.8.2.14 Math.random ( )
          */
         @Function(name = "random", arity = 0)
         public static Object random(ExecutionContext cx, Object thisValue) {
             return Math.random();
         }
 
         /**
          * 15.8.2.15 Math.round (x)
          */
         @Function(name = "round", arity = 1)
         public static Object round(ExecutionContext cx, Object thisValue, Object x) {
             double d = ToNumber(cx, x);
             if (d != d || d == 0 || Double.isInfinite(d)) {
                 return d;
             }
             if (d < 0 && d >= -0.5) {
                 return -0.0d;
             }
             int exp = Math.getExponent(d);
             if (exp >= 52) {
                 return d;
             }
             return Math.floor(d + 0.5);
         }
 
         /**
          * 15.8.2.16 Math.sin (x)
          */
         @Function(name = "sin", arity = 1)
         public static Object sin(ExecutionContext cx, Object thisValue, Object x) {
             return Math.sin(ToNumber(cx, x));
         }
 
         /**
          * 15.8.2.17 Math.sqrt (x)
          */
         @Function(name = "sqrt", arity = 1)
         public static Object sqrt(ExecutionContext cx, Object thisValue, Object x) {
             return Math.sqrt(ToNumber(cx, x));
         }
 
         /**
          * 15.8.2.18 Math.tan (x)
          */
         @Function(name = "tan", arity = 1)
         public static Object tan(ExecutionContext cx, Object thisValue, Object x) {
             return Math.tan(ToNumber(cx, x));
         }
 
         /**
          * 15.8.2.19 Math.log10 (x)
          */
         @Function(name = "log10", arity = 1)
         public static Object log10(ExecutionContext cx, Object thisValue, Object x) {
             return Math.log10(ToNumber(cx, x));
         }
 
         /**
          * 15.8.2.20 Math.log2 (x)
          */
         @Function(name = "log2", arity = 1)
         public static Object log2(ExecutionContext cx, Object thisValue, Object x) {
             return Math.log(ToNumber(cx, x)) / Math.log(2d);
         }
 
         /**
          * 15.8.2.21 Math.log1p (x)
          */
         @Function(name = "log1p", arity = 1)
         public static Object log1p(ExecutionContext cx, Object thisValue, Object x) {
             return Math.log1p(ToNumber(cx, x));
         }
 
         /**
          * 15.8.2.22 Math.expm1 (x)
          */
         @Function(name = "expm1", arity = 1)
         public static Object expm1(ExecutionContext cx, Object thisValue, Object x) {
             return Math.expm1(ToNumber(cx, x));
         }
 
         /**
          * 15.8.2.23 Math.cosh(x)
          */
         @Function(name = "cosh", arity = 1)
         public static Object cosh(ExecutionContext cx, Object thisValue, Object x) {
             return Math.cosh(ToNumber(cx, x));
         }
 
         /**
          * 15.8.2.24 Math.sinh(x)
          */
         @Function(name = "sinh", arity = 1)
         public static Object sinh(ExecutionContext cx, Object thisValue, Object x) {
             return Math.sinh(ToNumber(cx, x));
         }
 
         /**
          * 15.8.2.25 Math.tanh(x)
          */
         @Function(name = "tanh", arity = 1)
         public static Object tanh(ExecutionContext cx, Object thisValue, Object x) {
             return Math.tanh(ToNumber(cx, x));
         }
 
         /**
          * 15.8.2.26 Math.acosh(x)
          */
         @Function(name = "acosh", arity = 1)
         public static Object acosh(ExecutionContext cx, Object thisValue, Object x) {
             double d = ToNumber(cx, x);
             if (Double.isNaN(d) || d < 1.0) {
                 return Double.NaN;
             }
             if (d == 1) {
                 return +0.0;
             }
             if (d == Double.POSITIVE_INFINITY) {
                 return Double.POSITIVE_INFINITY;
             }
             // return Math.log(d + Math.sqrt(d * d - 1.0));
            return Math.acos(d);
         }
 
         /**
          * 15.8.2.27 Math.asinh(x)
          */
         @Function(name = "asinh", arity = 1)
         public static Object asinh(ExecutionContext cx, Object thisValue, Object x) {
             double d = ToNumber(cx, x);
             if (Double.isNaN(d) || d == 0.0 || Double.isInfinite(d)) {
                 return d;
             }
             // return Math.log(d + Math.sqrt(d * d + 1.0));
             return MathImpl.asinh(d);
         }
 
         /**
          * 15.8.2.28 Math.atanh(x)
          */
         @Function(name = "atanh", arity = 1)
         public static Object atanh(ExecutionContext cx, Object thisValue, Object x) {
             double d = ToNumber(cx, x);
             if (Double.isNaN(d) || d < -1.0 || d > 1.0) {
                 return Double.NaN;
             }
             if (d == -1.0) {
                 return Double.NEGATIVE_INFINITY;
             }
             if (d == +1.0) {
                 return Double.POSITIVE_INFINITY;
             }
             if (d == 0.0) {
                 return d;
             }
             // return (Math.log(1.0 + d) - Math.log(1.0 - d)) / 2.0;
             return MathImpl.atanh(d);
         }
 
         /**
          * 15.8.2.29 Math.hypot( value1 , value2, value3 = 0 )
          */
         @Function(name = "hypot", arity = 2)
         public static Object hypot(ExecutionContext cx, Object thisValue, Object value1,
                 Object value2, @Optional(Optional.Default.NONE) Object value3) {
             if (value3 == null) {
                 return Math.hypot(ToNumber(cx, value1), ToNumber(cx, value2));
             }
             double v1 = ToNumber(cx, value1), v2 = ToNumber(cx, value2), v3 = ToNumber(cx, value3);
             if (Double.isInfinite(v1) || Double.isInfinite(v2) || Double.isInfinite(v3)) {
                 return Double.POSITIVE_INFINITY;
             }
             if (Double.isNaN(v1) || Double.isNaN(v2) || Double.isNaN(v3)) {
                 return Double.NaN;
             }
             if (v1 == 0.0 && v2 == 0.0 && v3 == 0.0) {
                 return +0.0;
             }
             return Math.sqrt(v1 * v1 + v2 * v2 + v3 * v3);
         }
 
         /**
          * 15.8.2.30 Math.trunc(x)
          */
         @Function(name = "trunc", arity = 1)
         public static Object trunc(ExecutionContext cx, Object thisValue, Object x) {
             double d = ToNumber(cx, x);
             return d < 0 ? Math.ceil(d) : Math.floor(d);
         }
 
         /**
          * 15.8.2.31 Math.sign(x)
          */
         @Function(name = "sign", arity = 1)
         public static Object sign(ExecutionContext cx, Object thisValue, Object x) {
             return Math.signum(ToNumber(cx, x));
         }
 
         /**
          * 15.8.2.32 Math.cbrt(x)
          */
         @Function(name = "cbrt", arity = 1)
         public static Object cbrt(ExecutionContext cx, Object thisValue, Object x) {
             return Math.cbrt(ToNumber(cx, x));
         }
 
         /**
          * 15.8.2.44 Math.imul(x, y)
          */
         @Function(name = "imul", arity = 2)
         public static Object imul(ExecutionContext cx, Object thisValue, Object x, Object y) {
             long a = ToUint32(cx, x);
             long b = ToUint32(cx, y);
             return (int) (a * b);
         }
     }
 }
