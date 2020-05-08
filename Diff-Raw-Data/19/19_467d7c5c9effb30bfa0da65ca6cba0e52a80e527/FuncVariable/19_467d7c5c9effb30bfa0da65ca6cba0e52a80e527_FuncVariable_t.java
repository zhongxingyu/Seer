 package org.wixpress.hoopoe.lambda;
 
 /**
  * @author Yoav
  * @since 10/3/11
  */
 public class FuncVariable {
     private Class<?> type;
     private boolean hasBoxing = false;
     private BoxedPair boxedPair;
 
     public FuncVariable(Class<?> type) {
         this.type = type;
         initBoxing();
     }
 
     private void initBoxing() {
         for (BoxedPair pair: BoxedPair.values())
             if (pair.boxed == type || pair.primitive == type) {
                 hasBoxing = true;
                 boxedPair = pair;
             }
     }
 
     public Class<?> boxedType() {
         return hasBoxing?boxedPair.boxed:type;
     }
 
     public Class<?> primitiveType() {
         return hasBoxing?boxedPair.primitive:type;
     }
 
     public String boxCode(String block) {
         return hasBoxing?String.format(boxedPair.boxCode, block):block;
     }
 
     public String unboxCode(String block) {
        return hasBoxing?String.format(boxedPair.unboxCode, block):String.format("(%s)%s", type.getName(), block);
     }
 
     @SuppressWarnings({"UnusedDeclaration"})
     private enum BoxedPair {
         Boolean(Boolean.class, boolean.class, "new Boolean(%s)", "((Boolean)%s).booleanValue()"),
         Character(Character.class, char.class, "new Character(%s)", "((Character)%s).charValue()"),
         Byte(Byte.class, byte.class, "new Byte(%s)", "((Byte)%s).byteValue()"),
         Short(Short.class, short.class, "new Short(%s)", "((Short)%s).shortValue()"),
         Integer(Integer.class, int.class, "new Integer(%s)", "((Integer)%s).intValue()"),
         Long(Long.class, long.class, "new Long(%s)", "((Long)%s).longValue()"),
         Float(Float.class, float.class, "new Float(%s)", "((Float)%s).floatValue()"),
         Double(Double.class, double.class, "new Double(%s)", "((Double)%s).doubleValue()") ;
 
         Class<?> boxed;
         Class<?> primitive;
         String boxCode;
         String unboxCode;
 
         BoxedPair(Class<?> boxed, Class<?> primitive, String boxCode, String unboxCode) {
             this.boxed = boxed;
             this.primitive = primitive;
             this.boxCode = boxCode;
             this.unboxCode = unboxCode;
         }
     }
 
 
 }
