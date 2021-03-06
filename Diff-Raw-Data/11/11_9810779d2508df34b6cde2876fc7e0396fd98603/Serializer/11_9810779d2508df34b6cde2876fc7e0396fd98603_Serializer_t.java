 /* This class is part of the XP framework's EAS connectivity
  *
  * $Id$ 
  */
 
 package net.xp_framework.easc.protocol.standard;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Date;
 import java.util.Iterator;
 import net.xp_framework.easc.protocol.standard.Handler;
 import net.xp_framework.easc.protocol.standard.ArraySerializer;
 
 /**
  * Serializer / unserializer for PHP representationOfd data
  *
  * Usage example:
  * <code>
  *   Object o= Serializer.valueOf("s:11:\"Hello World\";");
  *   System.out.println(o);
  * </code>
  *
  * Usage example:
  * <code>
  *   String s= Serializer.representationOf("Hello");
  *   System.out.println(s);
  * </code>
  *
  * @see   http://php.net/unserialize
  * @see   http://php.net/serialize
  */
 public class Serializer {
     
     static interface Invokeable<Element> {
         public String invoke(Element e) throws Exception;
     }
     
     abstract static class InvokationTarget<Element> implements Invokeable<Element> {
         abstract public String invoke(Element e) throws Exception;
     }
     
     static class MethodTarget<Element> implements Invokeable<Element> {
         private Method method = null;
         
         MethodTarget(Method m) {
             this.method= m;    
         }
         
         public String invoke(Element e) throws Exception {
             return (String)this.method.invoke(null, new Object[] { e });
         }
     }
 
    private static HashMap<Class, Invokeable<Object>> typeMap= new HashMap<Class, Invokeable<Object>>();
     
     static {
         
         // Set up typeMap by inspecting all class methods with @Handler annotation
         for (Method m : Serializer.class.getDeclaredMethods()) {
             if (null == m.getAnnotation(Handler.class)) continue;
             
            registerMapping(m.getParameterTypes()[0], new MethodTarget<Object>(m));
         }
     }
     
    public static void registerMapping(Class c, Invokeable<Object> i) {
         typeMap.put(c, i);
     }
 
    protected static String representationOf(Object o, Invokeable<Object> i) throws Exception {
         if (i != null) return i.invoke(o);
 
         // Default object serialization
         StringBuffer buffer= new StringBuffer();
         Class c= o.getClass();
         long numFields = 0;
 
         for (Field f : classFields(c)) {
             buffer.append("s:");
             buffer.append(f.getName().length());
             buffer.append(":\"");
             buffer.append(f.getName());
             buffer.append("\";");
 
             f.setAccessible(true);
             buffer.append(representationOf(f.get(o), typeMap.get(f.getType())));
             numFields++;
         }
 
         buffer.append("}");        
         buffer.insert(0, "O:" + c.getName().length() + ":\"" + c.getName() + "\":" + numFields + ":{");
         return buffer.toString();
     }
 
     @Handler
     public static String representationOf(String s) {
         return "s:" + s.length() + ":\"" + s + "\";";
     } 
 
     @Handler
     public static String representationOf(char c) {
         return "s:1:\"" + c + "\";";
     }
     
     @Handler
     public static String representationOf(final char[] array) throws Exception {
         return new ArraySerializer() {
             public void yield(int i) {
                 this.buffer.append(representationOf(array[i]));
             }
         }.run(array.length);
     }
 
     @Handler
     public static String representationOf(Character c) {
         return "s:1:\"" + c + "\";";
     }
 
     @Handler
     public static String representationOf(byte b) {
         return "i:" + b + ";";
     }
     
     @Handler
     public static String representationOf(final byte[] array) throws Exception {
         return new ArraySerializer() {
             public void yield(int i) {
                 this.buffer.append(representationOf(array[i]));
             }
         }.run(array.length);
     }
 
     @Handler
     public static String representationOf(Byte b) {
         return "i:" + b + ";";
     }
 
     @Handler
     public static String representationOf(short s) {
         return "i:" + s + ";";
     }
     
     @Handler
     public static String representationOf(final short[] array) throws Exception {
         return new ArraySerializer() {
             public void yield(int i) {
                 this.buffer.append(representationOf(array[i]));
             }
         }.run(array.length);
     }
 
     @Handler
     public static String representationOf(Short s) {
         return "i:" + s + ";";
     }
 
     @Handler
     public static String representationOf(int i) {
         return "i:" + i + ";";
     }
 
     @Handler
     public static String representationOf(final int[] array) throws Exception {
         return new ArraySerializer() {
             public void yield(int i) {
                 this.buffer.append(representationOf(array[i]));
             }
         }.run(array.length);
     }
 
     @Handler
     public static String representationOf(Integer i) {
         return "i:" + i + ";";
     }
 
     @Handler
     public static String representationOf(long l) {
         return "i:" + l + ";";
     }
 
     @Handler
     public static String representationOf(final long[] array) throws Exception {
         return new ArraySerializer() {
             public void yield(int i) {
                 this.buffer.append(representationOf(array[i]));
             }
         }.run(array.length);
     }
 
     @Handler
     public static String representationOf(Long l) {
         return "i:" + l + ";";
     }
 
     @Handler
     public static String representationOf(double d) {
         return "d:" + d + ";";
     }
     
     @Handler
     public static String representationOf(final double[] array) throws Exception {
         return new ArraySerializer() {
             public void yield(int i) {
                 this.buffer.append(representationOf(array[i]));
             }
         }.run(array.length);
     }
 
     @Handler
     public static String representationOf(Double d) {
         return "d:" + d + ";";
     }
 
     @Handler
     public static String representationOf(float f) {
         return "d:" + f + ";";
     }
     
     @Handler
     public static String representationOf(final float[] array) throws Exception {
         return new ArraySerializer() {
             public void yield(int i) {
                 this.buffer.append(representationOf(array[i]));
             }
         }.run(array.length);
     }
 
     @Handler
     public static String representationOf(Float f) {
         return "d:" + f + ";";
     }
 
     @Handler
     public static String representationOf(boolean b) {
         return "b:" + (b ? 1 : 0) + ";";
     }
 
     @Handler
     public static String representationOf(final boolean[] array) throws Exception {
         return new ArraySerializer() {
             public void yield(int i) {
                 this.buffer.append(representationOf(array[i]));
             }
         }.run(array.length);
     }
 
     @Handler
     public static String representationOf(Boolean b) {
         return "b:" + (b ? 1 : 0) + ";";
     }
 
     @Handler
     public static String representationOf(HashMap h) throws Exception {
         StringBuffer buffer= new StringBuffer("a:" + h.size() + ":{");
         
         for (Iterator it= h.keySet().iterator(); it.hasNext(); ) {
             Object key= it.next();
             Object value= h.get(key);
 
             buffer.append(representationOf(key, typeMap.get(key.getClass())));
             buffer.append(representationOf(value, typeMap.get(value.getClass())));
         }
         
         buffer.append("}");
         return buffer.toString();
     }
     
     @Handler
     public static String representationOf(Object[] a) throws Exception {
         StringBuffer buffer= new StringBuffer("a:" + a.length + ":{");
 
         for (int i= 0; i < a.length; i++) {
             buffer.append("i:" + i + ";");
             buffer.append(representationOf(a[i], typeMap.get(a[i].getClass())));
         }
 
         buffer.append("}");
         return buffer.toString();
     }
     
     protected static ArrayList<Field> classFields(Class c) {
         ArrayList<Field> list= new ArrayList<Field>();
         
         for (Field f : c.getDeclaredFields()) {
             if (Modifier.isTransient(f.getModifiers())) continue;
             list.add(f);
         }
         
         return list;
     }
     
     @Handler
     public static String representationOf(Object o) throws Exception {
         return representationOf(o, typeMap.get(o.getClass()));
     }
    
     @Handler
     public static String representationOf(Date d) throws Exception {
         return "D:" + d.getTime() / 1000 + ";";   // getTime() returns *milliseconds*
     }
 }
