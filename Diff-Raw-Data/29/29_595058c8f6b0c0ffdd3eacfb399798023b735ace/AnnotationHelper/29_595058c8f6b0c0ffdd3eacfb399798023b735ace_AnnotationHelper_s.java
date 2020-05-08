 /*
  *  Copyright 2011 Yannick LOTH.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 package com.googlecode.wicketelements.common.annotation;
 
 import java.lang.annotation.Annotation;
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Target;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import static com.googlecode.jbp.common.requirements.ParamRequirements.INSTANCE;
 
 /**
  * Provides utility methods process annotations.
  *
  * @author Yannick LOTH
  */
 public final class AnnotationHelper {
 
     private AnnotationHelper() {
     }
 
     /**
      * Checks if a class has specific qualified annotations (these annotations are, in turn, also annotated).
      *
     * @param classParam               The class that may have a qualified annotation.
     * @param annotationQualifierClass The annotation that may qualifie the annotations on the class parameter.
     * @return {@code true} if the class has a qualified annotation, {@code false} else.
      */
     public static boolean isQualifiedAnnotationPresent(final Class<?> classParam, final Class<? extends Annotation> annotationQualifierClass) {
         INSTANCE.requireNotNull(classParam);
         INSTANCE.requireNotNull(annotationQualifierClass);
         final Target targetAnnot = annotationQualifierClass.getAnnotation(Target.class);
         final ElementType[] elementTypes = targetAnnot.value();
        INSTANCE.requireTrue(Arrays.asList(elementTypes).contains(ElementType.ANNOTATION_TYPE), "The specified annotation qualifier class must have the target element type ANNOTATION_TYPE, but does not.");
        INSTANCE.requireTrue(annotationQualifierClass.isAnnotation(), "Parameter 'annotationQualifierClass' must be the Class of an annotation type, but is not.");
         final Annotation[] annotations = classParam.getAnnotations();
         for (final Annotation annotation : annotations) {
             if (isQualifiedAnnotation(annotation.getClass(), annotationQualifierClass)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
     * Checks if the annotation is qualified by another annotation.
      *
     * @param annotationClassParam          The annotation that may be qualified.
     * @param annotationQualifierClassParam The annotation that may qualify other annotations.
     * @return {@code true} if the annotation is qualified, {@code false} else.
      */
     public static boolean isQualifiedAnnotation(final Class<? extends Annotation> annotationClassParam, final Class<? extends Annotation> annotationQualifierClassParam) {
         final Class<?>[] interfaces = annotationClassParam.getInterfaces();
         for (final Class<?> current : interfaces) {
             if (current.isAnnotationPresent(annotationQualifierClassParam)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Returns all qualified annotations of the specified class.
      *
      * @param classParam               The class that may have qualified annotations.
     * @param annotationQualifierClass The class of the annotation that may qualify other annotations.
      * @return A {@code List<Annotation>} with all found qualified annotations on the class.  May be an empty list, but must not be {@code null}.
      */
     public static List<Annotation> getQualifiedAnnotations(final Class<?> classParam, final Class<? extends Annotation> annotationQualifierClass) {
         INSTANCE.requireNotNull(classParam);
         INSTANCE.requireNotNull(annotationQualifierClass);
         final Target targetAnnot = annotationQualifierClass.getAnnotation(Target.class);
         final ElementType[] elementTypes = targetAnnot.value();
        INSTANCE.requireTrue(Arrays.asList(elementTypes).contains(ElementType.ANNOTATION_TYPE), "The specified annotation qualifier class must have the target element type ANNOTATION_TYPE, but does not.");
        INSTANCE.requireTrue(annotationQualifierClass.isAnnotation(), "Parameter 'annotationQualifierClass' must be the Class of an annotation type, but is not.");
         final List<Annotation> qualifiedAnnotations = new ArrayList<Annotation>();
         final Annotation[] annotations = classParam.getAnnotations();
         for (final Annotation annotation : annotations) {
             if (isQualifiedAnnotation(annotation.getClass(),annotationQualifierClass)) {
                 qualifiedAnnotations.add(annotation);
             }
         }
         return Collections.unmodifiableList(qualifiedAnnotations);
     }
 }
