 package com.damnhandy.aspects.bean;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 /**
  * Marks a class as Observable so that it can be advised by the 
  * {@link JavaBeanAspect}. By default, all of the fields in the observed class
  * will fire property change change events unless marked with the
 * Silent annotation. Fields that require vetoable change support need
  * to be marked with the Constrained annotation.
  * 
  * @author Ryan J. McDonough
  * @since 1.0
  * @version $Revision: 1.1 $
 *
  * @see com.damnhandy.aspects.bean.Silent
  */
 @Target({ElementType.TYPE})
 @Retention(value=RetentionPolicy.RUNTIME)
 public @interface Observable {
 }
