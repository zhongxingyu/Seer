 package org.happy.commands.annotation;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 /**
  * User: Paweł Wesołowski (wesoły)
  * Date: 3/12/12
  * Time: 12:10 AM
  */
 
 @Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.TYPE,
        ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR,
        ElementType.PACKAGE,
        ElementType.PARAMETER,
        ElementType.LOCAL_VARIABLE,
        ElementType.METHOD,
        ElementType.FIELD
})
 public @interface CmdArg {
     String value();
 }
