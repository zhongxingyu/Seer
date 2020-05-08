 /*
  Copyright (c) 2010, The Staccato-Commons Team   
  
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation; version 3 of the License.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.
  */
 package net.sf.staccatocommons.check.annotation;
 
 import java.lang.annotation.Documented;
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 import net.sf.staccatocommons.defs.restriction.Restriction;
 
 /**
 * A check annotation that signals that the annotated element must be positive.
  * 
  * This annotation should only be applied to {@link Number}s and primitive
  * numeric types.
  * 
  * @author flbulgarelli
  * @see Restriction
  */
 @Restriction
 @Documented
 @Retention(RetentionPolicy.CLASS)
 @Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD })
 public @interface Positive {
 	/**
 	 * @return The variable name of the constrained element, or the empty string
 	 *         if unspecified. This value may help tools that analyze this
 	 *         annotation without access to source code.
 	 */
 	String value() default "";
 }
