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
 
 
 import org.mule.api.transformer.TransformerException;
 import org.mule.transformer.AbstractTransformer;
 import org.mule.transformer.types.DataTypeFactory;
 
 import com.google.protobuf.GeneratedMessage;
 
 public class ProtobufToByteArray extends AbstractTransformer {
 
     public ProtobufToByteArray() {
         this.registerSourceType(DataTypeFactory.create(GeneratedMessage.class));
         setReturnDataType(DataTypeFactory.create(byte[].class));
     }
 
     @Override
     protected Object doTransform(Object src, String enc) throws TransformerException
     {
         if(src instanceof GeneratedMessage) {
             return ((GeneratedMessage) src).toByteArray();
         }
         return null;
     }
 }
