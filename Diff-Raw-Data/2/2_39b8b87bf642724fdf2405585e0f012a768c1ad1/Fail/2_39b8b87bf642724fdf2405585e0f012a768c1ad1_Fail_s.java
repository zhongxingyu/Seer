 package org.nutz.mvc.annotation;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 @Retention(RetentionPolicy.RUNTIME)
 @Target({ElementType.METHOD, ElementType.TYPE})
 public @interface Fail {
 
	String value() default "";
 
 }
