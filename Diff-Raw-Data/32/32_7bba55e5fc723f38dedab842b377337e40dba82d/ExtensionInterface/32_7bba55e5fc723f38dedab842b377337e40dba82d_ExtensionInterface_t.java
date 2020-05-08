 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.core.injector.extension;
 
import java.lang.annotation.Documented;
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 /**
  * This annotation marks an <i>interface bean</i>. It is <b>highly</b>
  * recommended to use this annotation to avoid ambiguities., e.g.
  * 
  * <pre>
  * 
 * &#064;ExtensionInterface()
  * public interface ISubElement {
 * 	&#064;MapName(&quot;validation-policy&quot;)
 * 	String getValidationPolicy();
 * }
 * </pre>
  * 
 * or with a provided extension point id
 * 
 * <pre>
 * &#064;ExtensionInterface(&quot;org.eclipse.core.someId&quot;)
 * public interface ISubElement {
 * 	&#064;MapName(&quot;validation-policy&quot;)
 * 	String getValidationPolicy();
 * }
 * </pre>
  */
@Documented
 @Retention(RetentionPolicy.RUNTIME)
 @Target(ElementType.TYPE)
 public @interface ExtensionInterface {
 
	/**
	 * The id of the extension point this {@code ExtensionInterface} is used
	 * for.
	 * 
	 * @return the extension point id
	 */
	String id() default "";
 }
