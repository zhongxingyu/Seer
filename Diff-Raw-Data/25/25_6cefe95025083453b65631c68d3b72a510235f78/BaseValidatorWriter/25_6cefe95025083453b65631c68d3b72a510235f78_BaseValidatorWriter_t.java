 package fi.vincit.jmobster.processor.defaults.validator;
 
 /*
  * Copyright 2012-2013 Juha Siponen
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import fi.vincit.jmobster.annotation.RequiresAnnotations;
 import fi.vincit.jmobster.processor.languages.LanguageContext;
 import fi.vincit.jmobster.processor.model.HasType;
 import fi.vincit.jmobster.util.combination.CombinationManager;
 import fi.vincit.jmobster.util.combination.OptionalTypes;
 import fi.vincit.jmobster.util.combination.RequiredTypes;
 import fi.vincit.jmobster.util.reflection.CastUtil;
 import fi.vincit.jmobster.util.writer.DataWriter;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 import java.util.*;
 
 public abstract class BaseValidatorWriter<C extends LanguageContext<? extends W>, W extends DataWriter> implements ValidatorWriter<C, W> {
 
     private C context;
     private Method writeMethod;
 
     private final CombinationManager<HasType> combinationManager;
 
     public BaseValidatorWriter() {
         RequiredTypes requiredTypes = findRequiredTypes(this.getClass());
         OptionalTypes optionalTypes = findOptionalTypes(this.getClass());
         this.combinationManager = new CombinationManager<HasType>(requiredTypes, optionalTypes);
         this.writeMethod = this.findWriteMethods(this.getClass());
     }
 
     private OptionalTypes findOptionalTypes(Class validatorClass) {
         return OptionalTypes.get(findTypes(validatorClass, false));
     }
 
     private RequiredTypes findRequiredTypes(Class validatorClass) {
         return RequiredTypes.get(findTypes(validatorClass, true));
     }
 
     Class[] findTypes(Class validatorClass, boolean required) {
         Method method = findWriteMethods(validatorClass);
         List<Class> types = new ArrayList<Class>();
         resolveParameters(required, method, types);
         resolveAnnotationParameters(validatorClass, types);
         return types.toArray(new Class[types.size()]);
     }
 
     private void resolveAnnotationParameters(Class validatorClass, List<Class> types) {
         RequiresAnnotations requiredAnnotation = (RequiresAnnotations)validatorClass.getAnnotation(RequiresAnnotations.class);
         if( requiredAnnotation != null ) {
             Collections.addAll(types, requiredAnnotation.value());
         }
     }
 
    private void resolveParameters(boolean required, Method method, List<Class> types) {
         Type[] parameters = method.getGenericParameterTypes();
         for( Type parameter : parameters ) {
             CastUtil.ParamType resolvedType = CastUtil.resolveParamType(parameter);
             Class type = resolvedType.getType();
             if( !resolvedType.isOptional() && required ) {
                 types.add(type);
             } else if( resolvedType.isOptional() && !required ) {
                 types.add(type);
             }
         }
     }
 
     private Method findWriteMethods(Class validatorClass) {
         Method writeMethod = null;
         int numberOfWriteMethods = 0;
         for( Method method : validatorClass.getDeclaredMethods() ) {
             if( method.getName().equals("write") ) {
                 ++numberOfWriteMethods;
                 writeMethod = method;
             }
         }
         if( numberOfWriteMethods == 0 ) {
             throw new RuntimeException("Validator writer must have exactly one method called write");
         }
         if( numberOfWriteMethods > 1 ) {
             throw new RuntimeException("Validator writer must have exactly one method called write");
         }
         return writeMethod;
     }
 
     @Override
     public void setContext(C context) {
         this.context = context;
     }
 
     protected C getContext() {
         return context;
     }
 
     protected W getWriter() {
         return context.getWriter();
     }
 
     @Override
     public void write(Collection<? extends Annotation> annotations) {
         try {
             Map<Class, Annotation> bag = constructAnnotationBag(annotations);
             callWriteMethod(bag, this.writeMethod);
         } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
         } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
         }
     }
 
     private Map<Class, Annotation> constructAnnotationBag( Collection<? extends Annotation> annotations ) {
         Map<Class, Annotation> annotationBag = new HashMap<Class, Annotation>();
         for( Annotation annotation : annotations ) {
             if( combinationManager.containsClass(annotation.annotationType()) ) {
                 annotationBag.put(annotation.annotationType(), annotation);
             }
         }
         return annotationBag;
     }
 
     private void callWriteMethod(Map<Class, Annotation> annotations, Method method) throws InvocationTargetException, IllegalAccessException {
         Type[] paramTypes = method.getGenericParameterTypes();
         Object[] params = new Object[paramTypes.length];
 
         if(collectParams(annotations, paramTypes, params)) {
             method.invoke(this, params);
         }
     }
 
     @Override
     public boolean supportsAnnotations(Collection<? extends Annotation> annotations) {
         Map<Class, Annotation> bag = constructAnnotationBag(annotations);
         return supportsAnnotation(bag, this.writeMethod);
     }
 
     private boolean supportsAnnotation(Map<Class, Annotation> annotations, Method method) {
         Type[] paramTypes = method.getGenericParameterTypes();
         Object[] params = new Object[paramTypes.length];
 
         return collectParams(annotations, paramTypes, params);
     }
 
     private boolean collectParams(Map<Class, Annotation> annotations, Type[] paramTypes, Object[] params) {
         int numberFound = 0;
         int numberOfOptionalParams = 0;
         int numberOfNullOptionalParams = 0;
         for( int i = 0; i < paramTypes.length; ++i ) {
             CastUtil.ParamType type = CastUtil.resolveParamType(paramTypes[i]);
             if( type.isOfType(Annotation.class) ) {
                 // Now we know that the paramType is an Annotation
                 // We can cast it to correct type of class type
                 Class<? extends Annotation> annotationType =
                         type.getType().asSubclass(Annotation.class);
 
                 // Only add as found, if the annotation is actually found.
                 // Ensures that init methods are always called with non null values.
                 Annotation annotation = annotations.get(annotationType);
                 if( type.isOptional() ) {
                     ++numberOfOptionalParams;
                     if( annotation == null ) {
                         ++numberOfNullOptionalParams;
                     }
                 }
                 if( annotation != null || type.isOptional() ) {
                     params[i] = type.toParameter(annotation);
                     ++numberFound;
                 }
             }
         }
         final boolean noParamsNeeded = paramTypes.length == 0;
         final boolean onlyEmptyOptionalParams =
                 numberFound == numberOfOptionalParams
                 && numberOfNullOptionalParams == numberOfOptionalParams;
         final boolean allParamsFound = numberFound == paramTypes.length;
         return noParamsNeeded || (allParamsFound && !onlyEmptyOptionalParams);
     }
 }
 
 
