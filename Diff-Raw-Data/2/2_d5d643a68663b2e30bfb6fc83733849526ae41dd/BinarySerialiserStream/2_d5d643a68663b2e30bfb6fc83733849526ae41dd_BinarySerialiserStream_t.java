 /*
  * libxjava -- utility library for cross-Java-platform development
  *             ${project.name}
  *
  * Copyright (c) 2010 Marcel Patzlaff (marcel.patzlaff@gmail.com)
  *
  * This library is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.github.libxjava.io;
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import /*[CDCjava.util.HashMap/*CDC]*/com.github.libxjava.util.BasicHashMap/**/;
 
 
 /**
  * @author Marcel Patzlaff
  * @version ${project.artifactId} - ${project.version}
  */
 public class BinarySerialiserStream extends DataOutputStream implements ISerialiser {
     private /*[CDCHashMap/*CDC]*/BasicHashMap/**/ _references= new /*[CDCHashMap/*CDC]*/BasicHashMap/**/();
     protected int referenceCounter= 0;
     
     public BinarySerialiserStream(OutputStream out) {
         super(out);
     }
     
     public void flush() throws IOException {
         _references.clear();
         referenceCounter= 0;
         super.flush();
     }
 
     public final void writeObject(Object o) throws IOException {
         Short refNum= null;
         if(o == null) {
             writeByte(BinarySerialiserConstants.NULL);
             return;
         } else if((refNum= getReferenceNumber(o)) != null) {
             writeByte(BinarySerialiserConstants.REFERENCE);
            writeShort(refNum.shortValue() & 0xFFFF);
             return;
         } else if(o instanceof ISerialisable) {
             writeByte(BinarySerialiserConstants.SERIALISABLE);
             writeObject(o.getClass().getName());
             insertReference(o);
             ((ISerialisable) o).serialise(this);
             return;
         } else {
             int type= BinarySerialiserConstants.getType(o.getClass().getName());
             if(type < 0) {
                 throw new IOException("object of class + '" + o.getClass().getName() + "' cannot be serialised");
             }
             
             writeByte(type);
             
             switch (type) {
                 case BinarySerialiserConstants.STRING: {
                     writeUTF((String) o);
                     break;
                 }
                     
                 case BinarySerialiserConstants.BOOLEAN: {
                     writeBoolean(((Boolean)o).booleanValue());
                     break;
                 }
                     
                 case BinarySerialiserConstants.BYTE: {
                     writeByte(((Byte)o).byteValue());
                     break;
                 }
     
                 case BinarySerialiserConstants.CHAR: {
                     writeChar(((Character)o).charValue());
                     break;
                 }
                     
                 case BinarySerialiserConstants.DOUBLE: {
                     writeDouble(((Double)o).doubleValue());
                     break;
                 }
                     
                 case BinarySerialiserConstants.FLOAT: {
                     writeFloat(((Float)o).floatValue());
                     break;
                 }
                     
                 case BinarySerialiserConstants.INT: {
                     writeInt(((Integer)o).intValue());
                     break;
                 }
                     
                 case BinarySerialiserConstants.LONG: {
                     writeLong(((Long)o).longValue());
                     break;
                 }
                     
                 case BinarySerialiserConstants.SHORT: {
                     writeShort(((Short)o).shortValue());
                     break;
                 }
             }
         }
         
         insertReference(o);
     }
     
     protected Short getReferenceNumber(Object obj) {
         return (Short) _references.get(obj);
     }
     
     private void insertReference(Object obj) {
         if(_references.containsKey(obj)) {
             return;
         }
         
         Short value= /*[J5Short.valueOf/*J5]*/new Short/**/((short)(referenceCounter++));
         _references.put(obj, value);
     }
 }
