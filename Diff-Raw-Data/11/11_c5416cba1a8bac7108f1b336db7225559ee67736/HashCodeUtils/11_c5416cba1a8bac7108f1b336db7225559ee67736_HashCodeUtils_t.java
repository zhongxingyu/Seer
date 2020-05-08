 package org.jtheque.utils.bean;
 
 import org.jtheque.utils.Constants;
 
 import java.beans.PropertyDescriptor;
 import java.util.Arrays;
 
 /*
  * Copyright JTheque (Baptiste Wicht)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 /**
  * @author Baptiste Wicht
  */
 public final class HashCodeUtils {
     private static final int NUMBER_BIT_LENGTH = 32;
 
     /**
      * Utility class, not instanciable.
      */
     private HashCodeUtils() {
         throw new AssertionError();
     }
 
     /**
      * Generate a hash code for the bean.
      * <p/>
      * Note : The properties of the Object class are not retrieved.
      *
      * @param bean The bean.
      *
      * @return A hash code based on all the properties of the bean.
      */
     public static int hashCode(Object bean) {
         int result = Constants.HASH_CODE_START;
 
         for (PropertyDescriptor property : ReflectionUtils.getProperties(bean)) {
             if (property.getReadMethod() == null) {
                 continue;
             }
 
             Object value = ReflectionUtils.getProperty(bean, property);
 
             result = computeValue(result, value);
         }
 
         return result;
     }
 
     /**
      * Return the hash code of an object, using the properties in the given list.
      *
      * @param bean       The bean to generate the hash code from.
      * @param properties The properties to use to generate the hash code.
      *
      * @return The hash code of the bean . If there is no properties, the hash code will be 17.
      */
     public static int hashCode(Object bean, String... properties) {
         int result = Constants.HASH_CODE_START;
 
         for (String property : properties) {
             Object value = ReflectionUtils.getPropertyValue(bean, property);
 
             result = computeValue(result, value);
         }
 
         return result;
     }
 
     /**
      * Return the hash code of an object, using the properties in the given list.
      *
      * @param properties The properties to use to generate the hash code.
      *
      * @return The hash code of the bean . If there is no properties, the hash code will be 17.
      */
     public static int hashCodeDirect(Object... properties) {
         int result = Constants.HASH_CODE_START;
 
         for (Object property : properties) {
            result = computeValue(result, property);
         }
 
         return result;
     }
 
     /**
      * Compute the value with the current result.
      *
      * @param result The current hash code result.
      * @param value  The value to compute to the result.
      *
      * @return The result computed with the value.
      */
     private static int computeValue(int result, Object value) {
        if (value == null) {
            return result * Constants.HASH_CODE_PRIME;
        }

         if(value.getClass().isArray()){
             if (value instanceof long[]) {
                 return Constants.HASH_CODE_PRIME * result + Arrays.hashCode((long[]) value);
             } else if (value instanceof int[]) {
                 return Constants.HASH_CODE_PRIME * result + Arrays.hashCode((int[]) value);
             } else if (value instanceof short[]) {
                 return Constants.HASH_CODE_PRIME * result + Arrays.hashCode((short[]) value);
             } else if (value instanceof char[]) {
                 return Constants.HASH_CODE_PRIME * result + Arrays.hashCode((char[]) value);
             } else if (value instanceof byte[]) {
                 return Constants.HASH_CODE_PRIME * result + Arrays.hashCode((byte[]) value);
             } else if (value instanceof double[]) {
                 return Constants.HASH_CODE_PRIME * result + Arrays.hashCode((double[]) value);
             } else if (value instanceof float[]) {
                 return Constants.HASH_CODE_PRIME * result + Arrays.hashCode((float[]) value);
             } else if (value instanceof boolean[]) {
                 return Constants.HASH_CODE_PRIME * result + Arrays.hashCode((boolean[]) value);
             } else {
                 return Constants.HASH_CODE_PRIME * result + Arrays.hashCode((Object[]) value);
             }
         } else {
             if (value instanceof Double) {
                 Long temp = Double.doubleToLongBits((Double) value);
                 return Constants.HASH_CODE_PRIME * result + (int) (temp ^ temp >>> NUMBER_BIT_LENGTH);
             } else if (value instanceof Long) {
                 Long temp = (Long) value;
                 return Constants.HASH_CODE_PRIME * result + (int) (temp ^ temp >>> NUMBER_BIT_LENGTH);
             } else if (value instanceof Boolean) {
                 return Constants.HASH_CODE_PRIME * result + ((Boolean) value ? 0 : 1);
             } else if (value instanceof Float) {
                 return Constants.HASH_CODE_PRIME * result + Float.floatToIntBits((Float) value);
             } else if (value instanceof Number) {
                 return Constants.HASH_CODE_PRIME * result + ((Number) value).intValue();
             } else {
                 return Constants.HASH_CODE_PRIME * result + value.hashCode();
             }
         }
     }
 }
