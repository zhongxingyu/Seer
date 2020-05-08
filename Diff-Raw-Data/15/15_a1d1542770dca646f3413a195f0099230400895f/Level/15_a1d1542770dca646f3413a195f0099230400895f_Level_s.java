 package checkers.latticetainting.quals;
 
 import checkers.quals.DefaultQualifierInHierarchy;
 import checkers.quals.SubtypeOf;
 import checkers.quals.TypeQualifier;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 
 /**
  * @author John L. Singleton <jsinglet@gmail.com>
  */
 
 @Retention(RetentionPolicy.RUNTIME)
//@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
 @TypeQualifier
 @DefaultQualifierInHierarchy
 @SubtypeOf({})
 public @interface Level {
     String name() default "";
 
     String value() default "";
 
     String declassify_to() default "";
 
     String on() default "";
 
 }
