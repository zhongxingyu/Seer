 package org.libj.xquery.runtime;
 
 import org.libj.xquery.namespace.StandardStaticNamespace;
 import org.libj.xquery.xml.XML;
 
 import java.util.ArrayList;
 
 public class Op extends StandardStaticNamespace {
     public Op() {
         super(Op.class.getName());
         registerStaticFunction("list", 1);
         registerStaticFunction("add", 2);
         registerStaticFunction("subtract", 2);
         registerStaticFunction("multiply", 2);
         registerStaticFunction("div", 2);
         registerStaticFunction("negative", 1);
         registerStaticFunction("mod", 2);
         registerStaticFunction("eq", 2);
         registerStaticFunction("ne", 2);
         registerStaticFunction("and", 2);
         registerStaticFunction("or", 2);
         registerStaticFunction("to", 2);
         registerStaticFunction("at", 2);
         registerStaticFunction("xpath", 2);
     }
 
     //////////////////////////////////////////////////
     /// non-export APIs, for Java code
     //////////////////////////////////////////////////
     public static boolean isList(Object x) {
         return x instanceof Iterable;
     }
 
     public static Iterable<Object> asList(Object x) {
         if (x instanceof Iterable) {
             return (Iterable<Object>) x;
         }
         else {
             java.util.List<Object> list = new ArrayList<Object>(1);
            list.add(1);
             return list;
         }
     }
 
     public static boolean asBool(Object x) {
         if (x instanceof Boolean) {
             return (Boolean)x;
         }
         else if (x instanceof Integer) {
             return (Integer)x != 0;
         }
         else if (x instanceof String) {
             return ((String) x).isEmpty();
         }
         throw new RuntimeException("Not Implemented!");
     }
 
     //////////////////////////////////////////////////
     /// Below APIs are exported to namespace op:*
     //////////////////////////////////////////////////
 
     public static Object list(Object x) {
         return asList(x);
     }
 
     public static Object add(Object x, Object y) {
         if (x instanceof Integer && y instanceof Integer) {
             return (Integer) x + (Integer) y;
         }
         double dx = x instanceof Integer ? (Integer) x : (Double) x;
         double dy = y instanceof Integer ? (Integer) y : (Double) y;
         return dx + dy;
     }
 
     public static Object subtract(Object x, Object y) {
         if (x instanceof Integer && y instanceof Integer) {
             return (Integer) x - (Integer) y;
         }
         double dx = x instanceof Integer ? (Integer) x : (Double) x;
         double dy = y instanceof Integer ? (Integer) y : (Double) y;
         return dx - dy;
     }
 
     public static Object multiply(Object x, Object y) {
         if (x instanceof Integer && y instanceof Integer) {
             return (Integer) x * (Integer) y;
         }
         double dx = x instanceof Integer ? (Integer) x : (Double) x;
         double dy = y instanceof Integer ? (Integer) y : (Double) y;
         return dx * dy;
     }
 
     public static Object div(Object x, Object y) {
         if (x instanceof Integer && y instanceof Integer) {
             return (Integer) x / (Integer) y;
         }
         double dx = x instanceof Integer ? (Integer) x : (Double) x;
         double dy = y instanceof Integer ? (Integer) y : (Double) y;
         return dx / dy;
     }
 
     public static Object negative(Object x) {
         Number n = (Number) x;
         if (n instanceof Integer) {
             return -(Integer)n;
         }
         else if (n instanceof Double) {
             return -(Double)n;
         }
         else {
             throw new RuntimeException("Not Implemented!");
         }
     }
 
     public static Object mod(Object x, Object y) {
         if (x instanceof Integer && y instanceof Integer) {
             return (Integer) x % (Integer) y;
         }
         double dx = x instanceof Integer ? (Integer) x : (Double) x;
         double dy = y instanceof Integer ? (Integer) y : (Double) y;
         return dx % dy;
     }
 
     public static Object eq(Object x, Object y) {
         if (x instanceof List || y instanceof List) {
             throw new RuntimeException("Not Implemented!");
         }
         return x.equals(y);
     }
     public static Object ne(Object x, Object y) {
         return !(Boolean)eq(x, y);
     }
 
     public static Object and(Object x, Object y) {
         return asBool(x) && asBool(y);
     }
 
     public static Object or(Object x, Object y) {
         return asBool(x) || asBool(y);
     }
 
     public static Object to(Object x, Object y) {
         int start = (Integer)x;
         int end = (Integer)y;
         return new Range(start, end+1);
     }
 
     public static Object at(Object x, Object i) {
         int index = (Integer) i;
         if (x instanceof java.util.List) {
             java.util.List<Object> list = (java.util.List<Object>) x;
             if (1 <= index && index <= list.size()) {
                 return list.get(index - 1);
             }
             else {
                 return new Nil();
             }
         }
         else if (x instanceof List) {
             List list = (List) x;
             return list.nth(index - 1);
         }
         else {
             if (index == 1) {
                 return x;
             }
             else {
                 return new Nil();
             }
         }
     }
     public static Object xpath(Object x, Object y) {
         return ((XML)x).eval((String) y);
     }
 }
