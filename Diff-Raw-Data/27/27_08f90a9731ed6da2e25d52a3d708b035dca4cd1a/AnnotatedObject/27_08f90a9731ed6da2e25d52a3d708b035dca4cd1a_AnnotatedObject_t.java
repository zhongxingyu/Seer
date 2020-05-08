 package com.redshape.utils;
 
 import java.lang.annotation.Annotation;
 
 /**
  * Entity to represents annotated object.
  *  
  * Holds directly annotation subject and collection of
  * annotated objects which decorated him. 
  * 
  * @author nikelin
  * @date 18/04/11
  * @package com.redshape.validators
  */
 public class AnnotatedObject {
 	private Object context;
 	private Annotation[] annotations;
 
 	public AnnotatedObject( Object context, Annotation... annotations ) {
 		this.context = context;
 		this.annotations = annotations;
 	}
 
 	@SuppressWarnings("unchecked")
 	public <V> V getContext() {
 		return (V) this.context;
 	}
 
 	@SuppressWarnings("unchecked")
 	public <V extends Annotation> V getAnnotation( Class<V> annotationClazz ) {
 		for ( Annotation annotation : this.getAnnotations() ) {
 			if ( annotation.annotationType().equals( annotationClazz ) ) {
 				return (V) annotation;
 			}
 		}
 
 		return null;
 	}
 
 	public Annotation[] getAnnotations() {
 		return this.annotations;
 	}
 
 }
