 /**
  * Protobuf Module
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.module.protobuf;
 
 import org.mule.api.MuleContext;
 import org.mule.api.annotations.Module;
 import org.mule.api.annotations.Processor;
 import org.mule.api.annotations.Transformer;
 import org.mule.api.annotations.TransformerResolver;
 import org.mule.api.annotations.param.Payload;
 import org.mule.api.transformer.DataType;
 
 import com.google.protobuf.GeneratedMessage;
 import com.google.protobuf.Message;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.List;
 
 import org.apache.commons.beanutils.MethodUtils;
 import org.apache.commons.lang.StringUtils;
 
 /**
  * Protocol Buffer Module
  *
  * @author MuleSoft, Inc.
  */
 @Module(name="protobuf", schemaVersion="1.0-SNAPSHOT")
 public class ProtoBufModule {
 
     protected static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
 
     /**
      * Parses the payload to build the specified Protocol Buffer object. Accepted payload types are InputStream or
      * byte[].
      *
      * The provided Class needs to be a Protocol Buffer class in order to parse the input.
      *
      * <p/>
      * {@sample.xml ../../../doc/Protobuf-module.xml.sample protobuf:deserialize}
      *
      *
      * @param payload Payload to process
      * @param protobufClass Name of protocol buffer class
      * @return an instance of the specified class built from the incoming payload
      */
     @Processor
     public Object deserialize(@Payload Object payload, String protobufClass) {
         if(payload instanceof InputStream || payload instanceof byte[]) {
             try {
                 Class clazz = Class.forName(protobufClass);
                 Method parseFromMethod = clazz.getDeclaredMethod("parseFrom", payload.getClass());
                 Object protobufResult = parseFromMethod.invoke(null, payload);
                 return protobufResult;
             } catch (Exception e) {
                 throw new RuntimeException("Unable to parse protocol buffer class", e);
             }
         }
         return payload;
     }
 
     /**
      * Serializes a Protocol Buffer instance into an InputStream. The input must be an instance of GeneratedMessage.
      *
      * <p/>
      * {@sample.xml ../../../doc/Protobuf-module.xml.sample protobuf:serialize-to-input-stream}
      *
      * @param protobuf the Protocol Buffer object to transform
      * @return an InputStream representation of the given object
      */
     @Transformer(sourceTypes = Object.class)
     public static InputStream serializeToInputStream(Object protobuf) {
         if(protobuf != null && protobuf instanceof GeneratedMessage) {
             return new ByteArrayInputStream(((GeneratedMessage)protobuf).toByteArray());
         }
         return null;
     }
 
     /**
      * Serializes a Protocol Buffer instance into a Byte Array. The input must be an instance of GeneratedMessage.
      *
      * <p/>
      * {@sample.xml ../../../doc/Protobuf-module.xml.sample protobuf:serialize-to-byte-array}
      *
      * @param protobuf the Protocol Buffer object to transform
      * @return a byte array representation of the given object
      */
     @Transformer (sourceTypes = Object.class)
     public static byte[] serializeToByteArray(Object protobuf) {
         if(protobuf != null && protobuf instanceof GeneratedMessage) {
             return ((GeneratedMessage)protobuf).toByteArray();
         }
         return null;
     }
 
     /**
      *
      * Transformer Resolver to handle Protocol Buffer types
      *
      * @param source the source data type
      * @param result the result data type
      * @param muleContext the MuleContext
      * @return a Transformer or null
      * @throws Exception if an error occurs
      */
     @TransformerResolver
     public static org.mule.api.transformer.Transformer ProtobufTransformerResolver(DataType source, DataType result, MuleContext muleContext) throws Exception {
         org.mule.api.transformer.Transformer t = null;
 
         if(isProtobufClass(source.getType())) {
             if(result.getType().equals(InputStream.class)) {
                 t = new ProtobufToInputStream();
             } else if(result.getType().equals(byte[].class)) {
                 t = new ProtobufToByteArray();
             }
         } else if(isProtobufClass(result.getType())) {
             if(source.getType().equals(InputStream.class) || source.getType().equals(byte[].class)) {
                 t = new ObjectToProtobuf();
                 t.setReturnDataType(result);
             }
         }
 
         if(t != null) {
             muleContext.getRegistry().applyProcessorsAndLifecycle(t);
         }
 
         return t;
     }
 
     private static boolean isProtobufClass(Class clazz) {
         return clazz.getSuperclass() != null && clazz.getSuperclass().equals(GeneratedMessage.class);
     }
 
     /**
      * Builder to instantiate and populate the provided Class. The class must be a Google Protocol Buffer class.
      *
      * <p/>
      * {@sample.xml ../../../doc/Protobuf-module.xml.sample protobuf:builder}
      *
      * @param protobufClass the Protocol Buffer class name
      * @param properties the map of properties needed to populate the bean
      * @return the Protocol Buffer object populated
      * @throws Exception if an error occurs building the object
      */
     @Processor
     public Object builder(String protobufClass,List<Property> properties) throws Exception {
 
         Class clazz = Class.forName(protobufClass);
         Object builder = clazz.getDeclaredMethod("newBuilder").invoke(null);
 
         for(Property property : properties) {
             String prefix = getPrefix(property, clazz);
 
 
             MethodUtils.invokeMethod(builder, prefix + StringUtils.capitalize(property.getName()), new Object[] {property.getExpression()});
         }
 
         return ((Message.Builder) builder).build();
     }
 
     private String getPrefix(Property property, Class clazz) {
 
         String prefix = "set";
 
         try {
             Field field = clazz.getDeclaredField(property.getName() + "_");
             field.setAccessible(true);
             if(Iterable.class.isAssignableFrom(field.getType())) {
                 if(property.getExpression() instanceof Iterable) {
                     prefix = "addAll";
                 } else {
                     prefix = "add";
                 }
             }
 
         } catch (NoSuchFieldException e) {
             throw new IllegalArgumentException("There is no field named " + property.getName());
         }
 
         return prefix;
     }
 
 }
