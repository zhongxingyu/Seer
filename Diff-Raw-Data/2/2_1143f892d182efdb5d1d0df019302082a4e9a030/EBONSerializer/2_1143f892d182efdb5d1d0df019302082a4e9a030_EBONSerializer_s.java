 package com.xap4o;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.IdentityHashMap;
 import java.util.List;
 import java.util.Map;
 
 public class EBONSerializer {
     private ExtendableByteBuffer buf = new ExtendableByteBuffer();
     private Map<Object, Integer> refMap = new IdentityHashMap<Object, Integer>();
     private int nextRef;
 
     public byte[] serialize(Object doc) {
         writeValue(doc);
         buf.flip();
         byte[] result = new byte[buf.remaining()];
         buf.get(result, 0, result.length);
        buf.flip();
         return result;
     }
 
     private void writeValue(Object value) {
         if (value == null) {
             buf.put(EBON.C_NULL);
             return;
         }
         Integer ref = refMap.get(value);
         if (ref != null) {
             buf.put(EBON.C_REF);
             buf.putInt(ref);
             return;
         }
         Class<?> clazz = value.getClass();
         if (clazz == Boolean.class) {
             buf.put(EBON.C_BOOLEAN);
             buf.put((Boolean) value ? (byte) 1 : (byte) 0);
         } else if (clazz == Integer.class) {
             buf.put(EBON.C_INT);
             buf.putInt((Integer) value);
         } else if (clazz == Long.class) {
             buf.put(EBON.C_LONG);
             buf.putLong((Long) value);
         } else if (clazz == Double.class) {
             buf.put(EBON.C_DOUBLE);
             buf.putDouble((Double) value);
         } else if (clazz == String.class) {
             writeString((String) value);
         } else if (clazz.isArray()) {
             if (clazz.getComponentType() == byte.class) {
                 writeByteArray((byte[]) value);
             } else {
                 throw new EBONException("Cannot serialize array of " + clazz.getComponentType());
             }
         } else if (List.class.isAssignableFrom(clazz)) {
             writeList((List) value);
         } else if (Map.class.isAssignableFrom(clazz)) {
             writeMap((Map<Object, Object>) value);
         } else if (clazz.isEnum()) {
             writeEnum((Enum) value);
         } else {
             writeObject(value);
         }
     }
 
     private void writeString(String str) {
         Integer ref = refMap.get(str);
         if (ref != null) {
             buf.put(EBON.C_REF);
             buf.putInt(ref);
         } else {
             byte[] bytes;
             try {
                 bytes = str.getBytes("UTF-8");
             } catch (Exception e) {
                 throw new EBONException("", e);
             }
             buf.put(EBON.C_STRING);
             buf.putInt(saveRef(str));
             buf.putInt(bytes.length);
             buf.put(bytes);
         }
     }
 
     private void writeObject(Object obj) {
         buf.put(EBON.C_OBJECT);
         buf.putInt(saveRef(obj));
         writeString(obj.getClass().getName());
         Map<String, Field> fieldsMap = Reflector.getFields(obj.getClass());
         int pos = buf.position();
         buf.putInt(0);//to reserve space for actual fieldsCount value
         int fieldsCount = 0;
         for (Map.Entry<String, Field> e : fieldsMap.entrySet()) {
             if (e.getValue().getAnnotation(Skip.class) == null) {
                 writeString(e.getKey());
                 try {
                     writeValue(e.getValue().get(obj));
                 } catch (Exception ex) {
                     throw new EBONException("", ex);
                 }
                 fieldsCount++;
             }
         }
         Map<String, Method> gettersMap = Reflector.getGetters(obj.getClass());
         for (Map.Entry<String, Method> e : gettersMap.entrySet()) {
             if (e.getValue().getAnnotation(Skip.class) == null) {
                 writeString(e.getKey());
                 try {
                     writeValue(e.getValue().invoke(obj));
                 } catch (Exception ex) {
                     throw new EBONException("", ex);
                 }
                 fieldsCount++;
             }
         }
         buf.putInt(pos, fieldsCount);
     }
 
     private void writeEnum(Enum value) {
         buf.put(EBON.C_ENUM);
         writeString(value.getClass().getName());
         writeString(value.name());
     }
 
     private void writeMap(Map<Object, Object> value) {
         buf.put(EBON.C_MAP);
         buf.putInt(saveRef(value));
         buf.putInt(value.size());
         for (Map.Entry<Object, Object> e : value.entrySet()) {
             writeValue(e.getKey());
             writeValue(e.getValue());
         }
     }
 
     private void writeList(List value) {
         buf.put(EBON.C_LIST);
         buf.putInt(saveRef(value));
         buf.putInt(value.size());
         for (Object elem : value) {
             writeValue(elem);
         }
     }
 
     private void writeByteArray(byte[] value) {
         buf.put(EBON.C_BINARY);
         buf.putInt(saveRef(value));
         buf.putInt(value.length);
         buf.put(value);
     }
 
     private int saveRef(Object value) {
         int ref = nextRef;
         nextRef++;
         refMap.put(value, ref);
         return ref;
     }
 }
