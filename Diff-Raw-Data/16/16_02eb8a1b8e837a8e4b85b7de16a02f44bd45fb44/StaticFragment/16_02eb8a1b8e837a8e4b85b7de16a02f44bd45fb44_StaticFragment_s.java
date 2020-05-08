 
 package com.zanoccio.jpacket.headers.annotations;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 /**
  * The {@link StaticFragment} annotation is used for automating construction of
  * simple packets.
  * 
  * @author wiktor
  */
 @Retention(RetentionPolicy.RUNTIME)
 @Target(ElementType.FIELD)
 public @interface StaticFragment {
 	/**
 	 * The position of this fragment; slots are sorted from smallest to largest
 	 */
 	int slot() default -1;
 
 
 	/**
 	 * The size of this fragment in bytes.
 	 */
 	int size() default -1;
 }
