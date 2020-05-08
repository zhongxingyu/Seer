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
 * @ExtensionInterface()
  * public interface ISubElement {
 * @MapName("validation-policy")
 * 		String getValidationPolicy(); }
  * 
 *                                            </pre>
  */
 @Retention(RetentionPolicy.RUNTIME)
 @Target(ElementType.TYPE)
 public @interface ExtensionInterface {
 
 }
