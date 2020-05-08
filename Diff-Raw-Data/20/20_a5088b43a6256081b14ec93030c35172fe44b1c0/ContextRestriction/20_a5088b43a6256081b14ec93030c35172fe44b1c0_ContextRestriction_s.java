 package com.redshape.servlet.core.restrictions;
 
import com.redshape.servlet.core.context.ContextId;

 import java.lang.annotation.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: cyril
  * Date: 7/2/12
  * Time: 4:28 PM
  * To change this template use File | Settings | File Templates.
  */
 @Target(ElementType.TYPE)
 @Retention(RetentionPolicy.RUNTIME)
 public @interface ContextRestriction {
 
     /**
      * @see com.redshape.servlet.core.context.ContextId
      * @return
      */
     public String[] value() default "ContextId.AJAX";
 
 }
