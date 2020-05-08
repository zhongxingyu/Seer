/* 
  * Created on 18-03-2013 18:35:37 by Andrzej Ludwikowski
  */
 
 package info.ludwikowski.annotation;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 /**
  * Marker interface for classes that should have builder generated.
  * 
  * @author andrzej.ludwikowski
  * 
  * 
  */
 @Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
 public @interface GenerateBuilder {}
