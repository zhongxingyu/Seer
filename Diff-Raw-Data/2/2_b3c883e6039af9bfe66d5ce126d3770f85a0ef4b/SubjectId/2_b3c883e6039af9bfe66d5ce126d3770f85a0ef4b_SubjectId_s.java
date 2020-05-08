 package fr.cg95.cvq.service.request;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 import net.sf.oval.configuration.annotation.Constraint;
 
 @Retention(RetentionPolicy.RUNTIME)
 @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
 @Constraint(checkWith = SubjectIdCheck.class)
 public @interface SubjectId {
 
     String message() default "subjectId";
 
    String[] profiles() default {""};
 
     String when() default "";
 }
