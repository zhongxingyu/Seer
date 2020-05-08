 /*
  *  Copyright the original author or authors.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package net.morph.position;
 
 import java.lang.reflect.Type;
 import java.lang.reflect.TypeVariable;
 import java.util.Map;
 
 import org.apache.commons.lang3.ObjectUtils;
 import org.apache.commons.lang3.Validate;
 import org.apache.commons.lang3.reflect.TypeUtils;
 
 /**
  * Reference to a given value.
  * 
  * @param <T>
  */
 public class Ref<T> implements Position.Readable<T> {
     private static final TypeVariable<?>[] TYPE_PARAMS = Ref.class.getTypeParameters();
 
     private final T value;
     private final Type type;
 
     /**
      * If {@code value} may be {@code null}, create an anonymous subclass e.g. <code>new Ref<String>(null) {}</code>.
      * 
      * @param value
      */
     protected Ref(T value) {
         this.value = value;
         final Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(getClass(), Ref.class);
         if (typeArguments.containsKey(TYPE_PARAMS[0])) {
             this.type = typeArguments.get(TYPE_PARAMS[0]);
         } else {
             this.type = Validate.notNull(value).getClass();
         }
     }
 
     public Type getType() {
         return type;
     }
 
     public T getValue() {
         return value;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == this) {
             return true;
         }
         if (obj instanceof Ref == false) {
             return false;
         }
         Ref<?> other = (Ref<?>) obj;
         return other.getType().equals(type) && ObjectUtils.equals(other.getValue(), value);
     }
 
     @Override
     public int hashCode() {
         int result = 37 << 4;
         result |= type.hashCode();
         result <<= 4;
         result |= ObjectUtils.hashCode(value);
         return result;
     }
 
     @Override
     public String toString() {
        return String.format("Ref<%s>(%s)", type, value);
     }
 
     /**
      * Valid for non-{@code null} values only, and then can only get raw type from value. Use an anonymous subclass for
      * greater flexibility WRT types.
      * 
      * @param value
      * @return Ref
      */
     public static <T> Ref<T> to(T value) {
         return new Ref<T>(value);
     }
 
 }
