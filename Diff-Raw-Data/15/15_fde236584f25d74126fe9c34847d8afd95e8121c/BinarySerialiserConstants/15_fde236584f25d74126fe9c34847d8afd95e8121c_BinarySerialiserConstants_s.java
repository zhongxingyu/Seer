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
 
import com.github.libxjava.util.BasicHashMap;
 
 /**
  * A utility class with constants for the serialisation.
  * 
  * @author Marcel Patzlaff
  * @version ${project.artifactId} - ${project.version}
  */
 final class BinarySerialiserConstants {
     // special class types
     public static final byte NULL= 1;
     public static final byte REFERENCE= 2;
     public static final byte SERIALISABLE= 3;
     
     // primitive types
     public static final byte BASE_TYPE_START= 4;
 
     public static final byte BOOLEAN= BASE_TYPE_START;
     public static final byte BYTE= BASE_TYPE_START + 1;
     public static final byte CHAR= BASE_TYPE_START + 2;
     public static final byte DOUBLE= BASE_TYPE_START + 3;
     public static final byte FLOAT= BASE_TYPE_START + 4;
     public static final byte INT= BASE_TYPE_START + 5;
     public static final byte LONG= BASE_TYPE_START + 6;
     public static final byte SHORT= BASE_TYPE_START + 7;
     public static final byte STRING= BASE_TYPE_START + 8;
     
    private static final BasicHashMap ALLOWED= new BasicHashMap();
     
     static {
         ALLOWED.put("java.lang.Boolean", new Byte(BOOLEAN));
         ALLOWED.put("java.lang.Byte", new Byte(BYTE));
         ALLOWED.put("java.lang.Character", new Byte(CHAR));
         ALLOWED.put("java.lang.Double", new Byte(DOUBLE));
         ALLOWED.put("java.lang.Float", new Byte(FLOAT));
         ALLOWED.put("java.lang.Integer", new Byte(INT));
         ALLOWED.put("java.lang.Long", new Byte(LONG));
         ALLOWED.put("java.lang.Short", new Byte(SHORT));
         ALLOWED.put("java.lang.String", new Byte(STRING));
     }
     
     public static byte getType(String className) {
         if(containsType(className)) {
             return ((Byte)ALLOWED.get(className)).byteValue();
         }
         return -1;
     }
     
     public static boolean containsType(String className) {
         return ALLOWED.containsKey(className);
     }
     
     /**
      * Checks whether serialisation is supported for the given type.
      * <p>
      * The following types can be serialised:
      * <ul>
      *  <li>{@link java.lang.Boolean}</li>
      *  <li>{@link java.lang.Byte}</li>
      *  <li>{@link java.lang.Character}</li>
      *  <li>{@link java.lang.Double}</li>
      *  <li>{@link java.lang.Float}</li>
      *  <li>{@link java.lang.Integer}</li>
      *  <li>{@link java.lang.Long}</li>
      *  <li>{@link java.lang.Short}</li>
      *  <li>{@link java.lang.String}</li>
      *  <li>classes that implements {@link ISerialisable}</li>
      * </ul>
      * 
      * @param obj   the object to be checked
      * @return      <code>true</code> if the object can be serialised
      *              or <code>false</code> otherwise.
      * 
      * @see ISerialisable
      * @see ISerialiser
      * @see IDeserialiser
      */
     public static boolean isSerialisationSupported(Object obj) {
         if((obj == null ) || (obj instanceof ISerialisable)) {
             return true;
         }
         
         return containsType(obj.getClass().getName());
     }
     
     private BinarySerialiserConstants() {}
 }
