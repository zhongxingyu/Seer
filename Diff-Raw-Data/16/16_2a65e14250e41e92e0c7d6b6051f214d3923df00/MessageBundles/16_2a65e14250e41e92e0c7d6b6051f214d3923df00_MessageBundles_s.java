 package org.dejava.component.i18n.message.annotation;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 /**
  * Annotation to define message bundles to be used when getting messages from within a class. The annotations
  * are used to define the available bundles in for the message handler.
  */
 @Retention(value = RetentionPolicy.RUNTIME)
 @Target(value = { ElementType.TYPE })
 public @interface MessageBundles {
	
 	/**
	 * Default type for the bundle. Will be used by the default if no type is given during the message
	 * retrieve.
 	 */
 	String defaultType() default "";
	
 	/**
 	 * Message bundles for the class.
 	 */
 	MessageBundle[] messageBundles();
 }
