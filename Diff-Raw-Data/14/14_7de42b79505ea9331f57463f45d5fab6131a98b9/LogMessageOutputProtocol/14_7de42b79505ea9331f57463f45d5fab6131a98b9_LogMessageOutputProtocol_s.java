 /*******************************************************************************
  * Copyright (c) 2013, Minor Gordon
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in
  *       the documentation and/or other materials provided with the
  *       distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
  * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
  * OF SUCH DAMAGE.
  ******************************************************************************/
 
 package org.thryft.protocol;
 
 import java.io.OutputStream;
 import java.io.Writer;
 
 import com.fasterxml.jackson.core.JsonGenerator;
 import com.google.common.annotations.GwtIncompatible;
 
 @GwtIncompatible("")
 public class LogMessageOutputProtocol extends JacksonJsonOutputProtocol {
     private final class ArrayOutputProtocol extends
    JacksonJsonOutputProtocol.ArrayOutputProtocol {
         @Override
         public void writeBool(final boolean value)
                 throws OutputProtocolException {
             if (size < SIZE_MAX) {
                 super.writeBool(value);
                 size++;
             }
         }
 
         @Override
         public void writeByte(final byte value) throws OutputProtocolException {
             if (size < SIZE_MAX) {
                 super.writeByte(value);
                 size++;
             }
         }
 
         @Override
         public void writeDouble(final double value)
                 throws OutputProtocolException {
             if (size < SIZE_MAX) {
                 super.writeDouble(value);
                 size++;
             }
         }
 
         @Override
         public void writeFieldBegin(final String name, final Type type,
                 final short id) throws OutputProtocolException {
             if (size < SIZE_MAX) {
                 super.writeFieldBegin(name, type, id);
             }
         }
 
         @Override
         public void writeI16(final short value) throws OutputProtocolException {
             if (size < SIZE_MAX) {
                 super.writeI16(value);
                 size++;
             }
         }
 
         @Override
         public void writeI32(final int value) throws OutputProtocolException {
             if (size < SIZE_MAX) {
                 super.writeI32(value);
                 size++;
             }
         }
 
         @Override
         public void writeI64(final long value) throws OutputProtocolException {
             if (size < SIZE_MAX) {
                 super.writeI64(value);
                 size++;
             }
         }
 
         @Override
         public void writeListBegin(final Type elementType, final int size)
                 throws OutputProtocolException {
            if (size < SIZE_MAX) {
                 super.writeListBegin(elementType, size);
             } else {
                 _getOutputProtocolStack().push(new NopOutputProtocol());
             }
         }
 
         @Override
         public void writeListEnd() throws OutputProtocolException {
             if (size < SIZE_MAX) {
                 super.writeListEnd();
                 size++;
             }
         }
 
         @Override
         public void writeMapBegin(final Type keyType, final Type valueType,
                 final int size) throws OutputProtocolException {
            if (size < SIZE_MAX) {
                 super.writeMapBegin(keyType, valueType, size);
             } else {
                 _getOutputProtocolStack().push(new NopOutputProtocol());
             }
         }
 
         @Override
         public void writeMapEnd() throws OutputProtocolException {
             if (size < SIZE_MAX) {
                 super.writeMapEnd();
                 size++;
             }
         }
 
         @Override
         public void writeString(final String value)
                 throws OutputProtocolException {
             if (size < SIZE_MAX) {
                 super.writeString(__cropString(value));
                 size++;
             }
         }
 
         @Override
         public void writeStructBegin(final String name)
                 throws OutputProtocolException {
             if (size < SIZE_MAX) {
                 super.writeStructBegin(name);
             } else {
                 _getOutputProtocolStack().push(new NopOutputProtocol());
             }
         }
 
         @Override
         public void writeStructEnd() throws OutputProtocolException {
             if (size < SIZE_MAX) {
                 super.writeStructEnd();
                 size++;
             }
         }
 
         public final static int SIZE_MAX = 4;
 
         private int size = 0;
     }
 
     private final class MapObjectOutputProtocol extends
    JacksonJsonOutputProtocol.MapObjectOutputProtocol {
         @Override
         public void writeBool(final boolean value)
                 throws OutputProtocolException {
             if (size < SIZE_MAX) {
                 super.writeBool(value);
                 size++;
             }
         }
 
         @Override
         public void writeByte(final byte value) throws OutputProtocolException {
             if (size < SIZE_MAX) {
                 super.writeByte(value);
                 size++;
             }
         }
 
         @Override
         public void writeDouble(final double value)
                 throws OutputProtocolException {
             if (size < SIZE_MAX) {
                 super.writeDouble(value);
                 size++;
             }
         }
 
         @Override
         public void writeFieldBegin(final String name, final Type type,
                 final short id) throws OutputProtocolException {
             if (size < SIZE_MAX) {
                 super.writeFieldBegin(name, type, id);
             }
         }
 
         @Override
         public void writeI16(final short value) throws OutputProtocolException {
             if (size < SIZE_MAX) {
                 super.writeI16(value);
                 size++;
             }
         }
 
         @Override
         public void writeI32(final int value) throws OutputProtocolException {
             if (size < SIZE_MAX) {
                 super.writeI32(value);
                 size++;
             }
         }
 
         @Override
         public void writeI64(final long value) throws OutputProtocolException {
             if (size < SIZE_MAX) {
                 super.writeI64(value);
                 size++;
             }
         }
 
         @Override
         public void writeListBegin(final Type elementType, final int size)
                 throws OutputProtocolException {
             if (size < SIZE_MAX) {
                 super.writeListBegin(elementType, size);
             } else {
                 _getOutputProtocolStack().push(new NopOutputProtocol());
             }
         }
 
         @Override
         public void writeListEnd() throws OutputProtocolException {
             if (size < SIZE_MAX) {
                 super.writeListEnd();
                 size++;
             }
         }
 
         @Override
         public void writeMapBegin(final Type keyType, final Type valueType,
                 final int size) throws OutputProtocolException {
             if (size < SIZE_MAX) {
                 super.writeMapBegin(keyType, valueType, size);
             } else {
                 _getOutputProtocolStack().push(new NopOutputProtocol());
             }
         }
 
         @Override
         public void writeMapEnd() throws OutputProtocolException {
             if (size < SIZE_MAX) {
                 super.writeMapEnd();
                 size++;
             }
         }
 
         @Override
         public void writeString(final String value)
                 throws OutputProtocolException {
             if (size < SIZE_MAX) {
                 super.writeString(__cropString(value));
                 size++;
             }
         }
 
         @Override
         public void writeStructBegin(final String name)
                 throws OutputProtocolException {
             if (size < SIZE_MAX) {
                 super.writeStructBegin(name);
             } else {
                 _getOutputProtocolStack().push(new NopOutputProtocol());
             }
         }
 
         @Override
         public void writeStructEnd() throws OutputProtocolException {
             if (size < SIZE_MAX) {
                 super.writeStructEnd();
                 size++;
             }
         }
 
         public final static int SIZE_MAX = 8;
 
         private int size = 0;
     }
 
     private final class NopOutputProtocol extends NestedOutputProtocol {
         @Override
         public void writeBool(final boolean value)
                 throws OutputProtocolException {
         }
 
         @Override
         public void writeByte(final byte value) throws OutputProtocolException {
         }
 
         @Override
         public void writeDouble(final double value)
                 throws OutputProtocolException {
         }
 
         @Override
         public void writeFieldBegin(final String name, final Type type,
                 final short id) throws OutputProtocolException {
         }
 
         @Override
         public void writeFieldEnd() throws OutputProtocolException {
         }
 
         @Override
         public void writeFieldStop() throws OutputProtocolException {
         }
 
         @Override
         public void writeI16(final short value) throws OutputProtocolException {
         }
 
         @Override
         public void writeI32(final int value) throws OutputProtocolException {
         }
 
         @Override
         public void writeI64(final long value) throws OutputProtocolException {
         }
 
         @Override
         public void writeListBegin(final Type elementType, final int size)
                 throws OutputProtocolException {
             _getOutputProtocolStack().push(new NopOutputProtocol());
         }
 
         @Override
         public void writeListEnd() throws OutputProtocolException {
         }
 
         @Override
         public void writeMapBegin(final Type keyType, final Type valueType,
                 final int size) throws OutputProtocolException {
             _getOutputProtocolStack().push(new NopOutputProtocol());
         }
 
         @Override
         public void writeMapEnd() throws OutputProtocolException {
         }
 
         @Override
         public void writeString(final String value)
                 throws OutputProtocolException {
         }
 
         @Override
         public void writeStructBegin(final String name)
                 throws OutputProtocolException {
             _getOutputProtocolStack().push(new NopOutputProtocol());
         }
 
         @Override
         public void writeStructEnd() throws OutputProtocolException {
         }
     }
 
     private final class StructObjectOutputProtocol extends
    JacksonJsonOutputProtocol.StructObjectOutputProtocol {
         @Override
         public void writeString(final String value)
                 throws OutputProtocolException {
             super.writeString(__cropString(value));
         }
     }
 
     private final static String __cropString(final String value) {
         if (value.length() <= STRING_LENGTH_MAX) {
             return value;
         } else {
             return value.substring(0, STRING_LENGTH_MAX - 6) + "..."
                     + value.substring(value.length() - 3);
         }
     }
 
     public LogMessageOutputProtocol(final JsonGenerator generator) {
         super(generator);
     }
 
     public LogMessageOutputProtocol(final OutputStream outputStream)
             throws OutputProtocolException {
         super(outputStream);
     }
 
     public LogMessageOutputProtocol(final Writer writer)
             throws OutputProtocolException {
         super(writer);
     }
 
     @Override
     protected ArrayOutputProtocol _createArrayOutputProtocol() {
         return new ArrayOutputProtocol();
     }
 
     @Override
     protected MapObjectOutputProtocol _createMapObjectOutputProtocol() {
         return new MapObjectOutputProtocol();
     }
 
     @Override
     protected StructObjectOutputProtocol _createStructObjectOutputProtocol() {
         return new StructObjectOutputProtocol();
     }
 
     public final static int STRING_LENGTH_MAX = 16;
 }
