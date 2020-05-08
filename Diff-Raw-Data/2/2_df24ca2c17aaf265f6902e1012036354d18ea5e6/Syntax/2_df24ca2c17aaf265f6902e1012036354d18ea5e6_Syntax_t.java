 // Copyright 2008 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.syntax;
 
 import java.io.Serializable;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Type;
 
 import org.joe_e.Powerless;
 import org.joe_e.Struct;
 import org.joe_e.reflect.Reflection;
 import org.ref_send.Record;
 import org.ref_send.deserializer;
 import org.ref_send.name;
 
 /**
  * A serialization syntax.
  */
 public class
 Syntax extends Struct implements Powerless, Record, Serializable {
     static private final long serialVersionUID = 1L;
 
     /**
      * file extension
      */
     public final String ext;
     
     /**
      * serializer
      */
     public final Serializer serializer;
     
     /**
      * deserializer
      */
     public final Deserializer deserializer;
     
     /**
      * Constructs an instance.
      * @param ext           {@link #ext}
      * @param serializer    {@link #serializer}
      * @param deserializer  {@link #deserializer}
      */
     public @deserializer
     Syntax(@name("ext") final String ext,
            @name("serializer") final Serializer serializer,
            @name("deserializer") final Deserializer deserializer) {
         this.ext = ext;
         this.serializer = serializer;
         this.deserializer = deserializer;
     }
     
     /**
      * Finds a corresponding {@link deserializer}.
      * @param type  type to construct
      * @return constructor, or <code>null</code> if none
      */
     static public Constructor<?>
     deserializer(final Class<?> type) {
         for (Class<?> i = type; null != i; i = i.getSuperclass()) {
             // Check for an explicit deserializer constructor.
             for (final Constructor<?> c : Reflection.constructors(i)) {
                 if (c.isAnnotationPresent(deserializer.class)) { return c; }
             }
             // Check for a default constructor of a pass-by-copy type.
             if (Throwable.class.isAssignableFrom(i) ||
                Record.class.isAssignableFrom(i)) {
                 try { 
                     return Reflection.constructor(i);
                 } catch (final NoSuchMethodException e) {
                     if (Record.class.isAssignableFrom(i)) {
                         throw new MissingDeserializer(Reflection.getName(i));
                     }
                 }
             }
             // Look for one in the superclass.
         }
         return null;
     }
     
     /**
      * Gets the default value of a specified type.
      * @param required  required type
      * @return default value
      */
     static public Object
     defaultValue(final Type required) {
         return boolean.class == required ? Boolean.FALSE :
                char.class    == required ? Character.valueOf('\0') :
                byte.class    == required ? Byte.valueOf((byte)0) :
                short.class   == required ? Short.valueOf((short)0) :
                int.class     == required ? Integer.valueOf(0) :
                long.class    == required ? Long.valueOf(0) :
                float.class   == required ? Float.valueOf(0.0f) :
                double.class  == required ? Double.valueOf(0.0) :
                (Object)null;
     }
 }
