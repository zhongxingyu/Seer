 /*
  * Copyright 2010 Ning, Inc.
  *
  * Ning licenses this file to you under the Apache License, version 2.0
  * (the "License"); you may not use this file except in compliance with the
  * License.  You may obtain a copy of the License at:
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
  * License for the specific language governing permissions and limitations
  * under the License.
  */
 
 package com.ning.metrics.serialization.thrift;
 
 import com.ning.metrics.serialization.schema.SchemaFieldType;
 import com.ning.metrics.serialization.thrift.item.DataItem;
 import com.ning.metrics.serialization.thrift.item.DataItemFactory;
 import org.apache.thrift.TException;
 import org.apache.thrift.protocol.TField;
 import org.apache.thrift.protocol.TProtocol;
 import org.apache.thrift.protocol.TType;
 
 public abstract class ThriftField
 {
     public abstract short getId();
 
     public abstract DataItem getDataItem();
 
     public abstract void write(TProtocol protocol) throws TException;
 
     public byte[] toByteArray()
     {
         switch (getDataItem().getThriftType()) {
             case TType.BOOL:
                 return booleanToByteArray(getDataItem().getBoolean());
             case TType.BYTE:
                 return numberToByteArray(Byte.toString(getDataItem().getByte()));
             case TType.DOUBLE:
                 return numberToByteArray(Double.toString(getDataItem().getDouble()));
             case TType.I16:
                 return numberToByteArray(Short.toString(getDataItem().getShort()));
             case TType.I32:
                 return numberToByteArray(Integer.toString(getDataItem().getInteger()));
             case TType.I64:
                 return numberToByteArray(Long.toString(getDataItem().getLong()));
             case TType.STRING:
                 return getDataItem().getString().getBytes();
             default:
                 throw new IllegalStateException("Unsupported field type " + getDataItem().getThriftType());
         }
     }
 
     private static byte[] numberToByteArray(String string)
     {
         byte[] bytes = new byte[string.length()];
 
         for (int i = 0; i < bytes.length; i++) {
             bytes[i] = (byte) string.charAt(i);
         }
         return bytes;
     }
 
     private static byte[] booleanToByteArray(boolean b)
     {
        return numberToByteArray(b ? "0" : "1");
     }
 
     public static ThriftField createThriftField(Boolean value, short id)
     {
         return new ThriftFieldImpl(DataItemFactory.create(value), new TField(String.valueOf(id), SchemaFieldType.BOOLEAN.getThriftType(), id));
     }
 
     public static ThriftField createThriftField(Byte value, short id)
     {
         return new ThriftFieldImpl(DataItemFactory.create(value), new TField(String.valueOf(id), SchemaFieldType.BYTE.getThriftType(), id));
     }
 
     public static ThriftField createThriftField(Short value, short id)
     {
         return new ThriftFieldImpl(DataItemFactory.create(value), new TField(String.valueOf(id), SchemaFieldType.SHORT.getThriftType(), id));
     }
 
     public static ThriftField createThriftField(Integer value, short id)
     {
         return new ThriftFieldImpl(DataItemFactory.create(value), new TField(String.valueOf(id), SchemaFieldType.INTEGER.getThriftType(), id));
     }
 
     public static ThriftField createThriftField(Long value, short id)
     {
         return new ThriftFieldImpl(DataItemFactory.create(value), new TField(String.valueOf(id), SchemaFieldType.LONG.getThriftType(), id));
     }
 
     public static ThriftField createThriftField(Double value, short id)
     {
         return new ThriftFieldImpl(DataItemFactory.create(value), new TField(String.valueOf(id), SchemaFieldType.DOUBLE.getThriftType(), id));
     }
 
     public static ThriftField createThriftField(String value, short id)
     {
         return new ThriftFieldImpl(DataItemFactory.create(value), new TField(String.valueOf(id), SchemaFieldType.STRING.getThriftType(), id));
     }
 
     public static ThriftField createThriftField(Class<?> type, Object o, short id)
     {
         if (o == null) {
             return null;
         }
 
         if (type.isAssignableFrom(Boolean.class)) {
             return new ThriftFieldImpl(DataItemFactory.create((Boolean) o), new TField(String.valueOf(id), SchemaFieldType.BOOLEAN.getThriftType(), id));
         }
         else if (type.isAssignableFrom(boolean.class)) {
             return new ThriftFieldImpl(DataItemFactory.create((Boolean) o), new TField(String.valueOf(id), SchemaFieldType.BOOLEAN.getThriftType(), id));
         }
         else if (type.isAssignableFrom(Byte.class)) {
             return new ThriftFieldImpl(DataItemFactory.create((Byte) o), new TField(String.valueOf(id), SchemaFieldType.BYTE.getThriftType(), id));
         }
         else if (type.isAssignableFrom(byte.class)) {
             return new ThriftFieldImpl(DataItemFactory.create((Byte) o), new TField(String.valueOf(id), SchemaFieldType.BYTE.getThriftType(), id));
         }
         else if (type.isAssignableFrom(Short.class)) {
             return new ThriftFieldImpl(DataItemFactory.create((Short) o), new TField(String.valueOf(id), SchemaFieldType.SHORT.getThriftType(), id));
         }
         else if (type.isAssignableFrom(short.class)) {
             return new ThriftFieldImpl(DataItemFactory.create((Short) o), new TField(String.valueOf(id), SchemaFieldType.SHORT.getThriftType(), id));
         }
         else if (type.isAssignableFrom(Integer.class)) {
             return new ThriftFieldImpl(DataItemFactory.create((Integer) o), new TField(String.valueOf(id), SchemaFieldType.INTEGER.getThriftType(), id));
         }
         else if (type.isAssignableFrom(int.class)) {
             return new ThriftFieldImpl(DataItemFactory.create((Integer) o), new TField(String.valueOf(id), SchemaFieldType.INTEGER.getThriftType(), id));
         }
         else if (type.isAssignableFrom(Long.class)) {
             return new ThriftFieldImpl(DataItemFactory.create((Long) o), new TField(String.valueOf(id), SchemaFieldType.LONG.getThriftType(), id));
         }
         else if (type.isAssignableFrom(long.class)) {
             return new ThriftFieldImpl(DataItemFactory.create((Long) o), new TField(String.valueOf(id), SchemaFieldType.LONG.getThriftType(), id));
         }
         else if (type.isAssignableFrom(Double.class)) {
             return new ThriftFieldImpl(DataItemFactory.create((Double) o), new TField(String.valueOf(id), SchemaFieldType.DOUBLE.getThriftType(), id));
         }
         else if (type.isAssignableFrom(double.class)) {
             return new ThriftFieldImpl(DataItemFactory.create((Double) o), new TField(String.valueOf(id), SchemaFieldType.DOUBLE.getThriftType(), id));
         }
         else if (type.isAssignableFrom(String.class)) {
             return new ThriftFieldImpl(DataItemFactory.create((String) o), new TField(String.valueOf(id), SchemaFieldType.STRING.getThriftType(), id));
         }
         else {
             return null;
         }
     }
 }
