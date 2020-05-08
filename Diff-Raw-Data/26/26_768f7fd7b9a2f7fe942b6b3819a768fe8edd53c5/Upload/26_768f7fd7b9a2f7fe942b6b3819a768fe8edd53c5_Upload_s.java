 package org.eweb4j.mvc.validator.annotation;
 
 import java.lang.annotation.Documented;
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Inherited;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 @Inherited
 @Documented
 @Retention(RetentionPolicy.RUNTIME)
 @Target({ElementType.FIELD, ElementType.METHOD})
 public @interface Upload {
 	String maxRequestSize() default "";//允许上传文件最大大小 default 4M
 	
 	String maxMemorySize() default "";//硬盘缓冲 default 4K
 	
 	String tmpDir() default "" ;//临时目录
 	
	Class<?> listener() default void.class;
 	
 	String[] suffix();
 }
