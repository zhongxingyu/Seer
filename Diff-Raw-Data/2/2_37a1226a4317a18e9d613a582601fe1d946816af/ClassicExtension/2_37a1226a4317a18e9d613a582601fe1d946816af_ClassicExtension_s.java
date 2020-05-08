 package net.mcforge.API;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 @Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
 public @interface ClassicExtension {
 	String extName();
 	int version() default 1;
 }
