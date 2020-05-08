 package com.feedly.cassandra.entity;
 
 import java.math.BigInteger;
 import java.nio.ByteBuffer;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.UUID;
 
 import me.prettyprint.cassandra.serializers.AbstractSerializer;
 import me.prettyprint.hector.api.Serializer;
 
 public class ByteIndicatorSerializer extends AbstractSerializer<Object>
 {
     public static final Map<Class<?>, Byte> INDICATOR_BYTES;
     public static final Map<Byte, Class<?>> INDICATOR_BYTES_REV;
     private static final ByteIndicatorSerializer _instance = new ByteIndicatorSerializer();
     
     static
     {
         //these values cannot change!
         byte val = 0;
         Map<Class<?>, Byte> bytes = new HashMap<Class<?>, Byte>();
         Map<Byte, Class<?>> bytesRev = new HashMap<Byte, Class<?>>();
         bytes.put(byte[].class, val++);
         bytes.put(BigInteger.class, val++);
         bytes.put(Boolean.class, val++);
         bytes.put(Byte.class, val++);
         bytes.put(ByteBuffer.class, val++);
         bytes.put(Character.class, val++);
         bytes.put(Date.class, val++);
         bytes.put(Double.class, val++);
         bytes.put(Float.class, val++);
         bytes.put(Integer.class, val++);
         bytes.put(Long.class, val++);
         bytes.put(Short.class, val++);
         bytes.put(String.class, val++);
         bytes.put(UUID.class, val++);
         
         for(Entry<Class<?>, Byte> e : bytes.entrySet())
             bytesRev.put(e.getValue(), e.getKey());
         
         INDICATOR_BYTES = Collections.unmodifiableMap(bytes);
         INDICATOR_BYTES_REV = Collections.unmodifiableMap(bytesRev);
     }
 
     public static final ByteIndicatorSerializer get() 
     {
         return _instance;
     }
 
     @SuppressWarnings({ "unchecked", "rawtypes" })
     @Override
     public ByteBuffer toByteBuffer(Object obj)
     {
         if(obj == null)
             return null;
         
         Byte indicator = INDICATOR_BYTES.get(obj.getClass()); //what type is the value?
 
         if(indicator == null)
             throw new IllegalArgumentException(obj.getClass() + " not serializable by this class");
         
         byte[] val = ((Serializer) EntityUtils.getSerializer(obj.getClass())).toBytes(obj);
         ByteBuffer buffer = ByteBuffer.allocate(val.length + 1);
         buffer.put(indicator);
         buffer.put(val);
         buffer.rewind();
         
         return buffer;
     }
 
     @Override
     public Object fromByteBuffer(ByteBuffer byteBuffer)
     {
         if(byteBuffer == null || !byteBuffer.hasRemaining())
             return null;
 
         Byte indicator = byteBuffer.get();
         Class<?> clazz = INDICATOR_BYTES_REV.get(indicator);
         
         if(clazz == null)
             throw new IllegalArgumentException("invalid indicator byte " + indicator);
         
         return EntityUtils.getSerializer(clazz).fromByteBuffer(byteBuffer);
     }
 
 }
