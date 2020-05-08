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
 package therian.operator.convert;
 
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.lang.reflect.TypeVariable;
 import java.util.Map;
 
 import org.apache.commons.lang3.ObjectUtils;
 import org.apache.commons.lang3.reflect.TypeUtils;
 
 import therian.TherianContext;
 import therian.Typed;
 import therian.operation.Convert;
 import therian.util.Types;
 
 /**
  * Abstract Element {@link Converter}.
  *
  * @param <SOURCE>
  * @param <TARGET>
  */
 public abstract class ElementConverter<SOURCE, TARGET> extends Converter<SOURCE, TARGET> {
     private final TypeVariable<? extends Class<?>> sourceElementType;
     private final TypeVariable<? extends Class<?>> targetElementType;
 
     /**
      * Create an {@link ElementConverter}.
      *
      * @param sourceElementType
      * @param targetElementType
      */
     protected ElementConverter(TypeVariable<? extends Class<?>> sourceElementType,
         TypeVariable<? extends Class<?>> targetElementType) {
         super();
         this.sourceElementType = sourceElementType;
         this.targetElementType = targetElementType;
     }
 
     @Override
     public boolean supports(TherianContext context, Convert<? extends SOURCE, ? super TARGET> convert) {
         if (!super.supports(context, convert)) {
             return false;
         }
         final Type targetComponentType = targetComponentType(convert.getTargetPosition());
         final Type sourceComponentType = sourceComponentType(convert.getSourcePosition());
 
         final boolean result =
             sourceComponentType != null && TypeUtils.isAssignable(sourceComponentType, targetComponentType);
         if (!result) {
             log.debug("Source component type [{}] does not seem to be assignable to target component type [{}]",
                 sourceComponentType, targetComponentType);
         }
         return result;
     }
 
     protected Type sourceComponentType(Typed<? extends SOURCE> item) {
         final Type t = item.getType();
         final Class<?> varOwner = sourceElementType.getGenericDeclaration();
 
         final Map<TypeVariable<?>, Type> args = TypeUtils.getTypeArguments(t, varOwner);
         return args == null ? null : ObjectUtils.defaultIfNull(Types.unrollVariables(args, sourceElementType),
             Object.class);
     }
 
     protected Type targetComponentType(Typed<? super TARGET> item) {
         final Type t = item.getType();
         final Class<?> varOwner = targetElementType.getGenericDeclaration();
 
        if (TypeUtils.isAssignable(varOwner, t)) {
            if (t instanceof Class<?>) {
                // raw
                return Object.class;
            }
 
            if (t instanceof ParameterizedType) {
                final Map<TypeVariable<?>, Type> args =
                    TypeUtils.determineTypeArguments(varOwner, (ParameterizedType) t);
                return args.get(targetElementType);
            }
         }
         return null;
     }
 }
