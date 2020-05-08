 /*
     Copyright (c) 2000-2012 Alessandro Coppo
     All rights reserved.
 
     Redistribution and use in source and binary forms, with or without
     modification, are permitted provided that the following conditions
     are met:
     1. Redistributions of source code must retain the above copyright
        notice, this list of conditions and the following disclaimer.
     2. Redistributions in binary form must reproduce the above copyright
        notice, this list of conditions and the following disclaimer in the
        documentation and/or other materials provided with the distribution.
     3. The name of the author may not be used to endorse or promote products
        derived from this software without specific prior written permission.
 
     THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
     IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
     OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
     IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
     INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
     NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
     DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
     THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
     (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
     THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 package net.sf.jautl.utility.reflection;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * This class implements some common methods used when performing reflection
  * tasks on fields.
  */
 public class FieldUtils {
     /**
      * Get the list of the fields of an object.
      * @param o the instance to analyze
      * @return an array of Field instances.
      */
 	public static Field[] getFields(Object o) {
 		return o.getClass().getFields();
 	}
 
     /**
      * Get the lost of the field names.
      * @param o the instance to analyze
      * @return a String array of Field names. 
      */
 	public static String[] getFieldNames(Object o) {
 		List<String> result = new ArrayList<String>();
 
 		Field[] fields = o.getClass().getFields();
 		
 		for (Field f : fields)
 			result.add(f.getName());
 
		return result.toArray(new String[result.size()]);
 	}
 
     /**
      * Return the Field object associated with a name.
      * @param o the instance to analyze
      * @param name the name of the field
      * @return the Field instance or null if the field is not present
      */
 	public static Field getField(Object o, String name) {
 		try {
 			return o.getClass().getField(name);
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
     /**
      * Return a list of Fields annotated with a given attribute.
      * @param o the instance to analyze
      * @param attributeClass the class of the marking attribute
      * @return a vector of fields
      */
    @SuppressWarnings(value = "unchecked")
 	public static Field[] getFieldsByAttribute(Object o, Class attributeClass) {
 		List<Field> result = new ArrayList<Field>();
 
 		Field[] fields = o.getClass().getFields();
 		
 		for (Field f : fields)
             if (f.getAnnotation(attributeClass) != null) {
                 result.add(f);
             }
 
		return result.toArray(new Field[result.size()]);
 	}
 
     /**
      * Return a list of the names of field annotated with a given attribute.
      * @param o the instance to analyze
      * @param attributeClass the class of the marking attribute
      * @return a vector of Strings
      */
    @SuppressWarnings(value = "unchecked")
 	public static String[] getFieldNamesByAttribute(Object o, Class attributeClass) {
 		List<String> result = new ArrayList<String>();
 
 		Field[] fields = o.getClass().getFields();
 		
 		for (Field f : fields)
             if (f.getAnnotation(attributeClass) != null) {
                 result.add(f.getName());
             }
 
        return result.toArray(new String[result.size()]);
 	}
 
     /**
      * Set the value of a field of given name.
      * @param o the object whose field set
      * @param name the name of the field
      * @param value the new value
      * @throws SecurityException
      * @throws NoSuchFieldException
      * @throws IllegalArgumentException
      * @throws IllegalAccessException 
      */
 	public static void setValue(Object o, String name, Object value) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
 		Field f = o.getClass().getField(name);
 
 		f.set(o, value);
 	}
     
     /**
      * Return the value of a field given its name.
      * @param o the object whose field get
      * @param name the name of the field
      * @return the value or IllegalArgumentException if the field is missing
      * @throws NoSuchFieldException
      * @throws IllegalArgumentException
      * @throws IllegalAccessException 
      */
     public static Object getValue(Object o, String name) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
 		Field f = o.getClass().getField(name);
 
         if (f == null) throw new IllegalArgumentException("Field '" + name + "' does not exists in instance");
         
         return f.get(o);
     }
 }
