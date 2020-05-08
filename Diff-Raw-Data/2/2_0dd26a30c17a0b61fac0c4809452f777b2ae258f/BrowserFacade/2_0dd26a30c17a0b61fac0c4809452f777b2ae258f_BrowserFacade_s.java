 /*******************************************************************************
  * Copyright (c) 2007, 2010 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.swt.facades;
 
 import org.eclipse.swt.browser.Browser;
 
 /**
  * Facade for the {@link Browser} class.
  * 
 * @since 2.0
  */
 public abstract class BrowserFacade {
 
 	private static final BrowserFacade INSTANCE = FacadeFactory.newFacade(BrowserFacade.class);
 
 	/**
 	 * The applicable implementation of this class.
 	 */
 	public static final BrowserFacade getDefault() {
 		return INSTANCE;
 	}
 
 	/**
 	 * Returns a string with HTML that represents the content given
 	 * {@link Browser} control.
 	 * 
 	 * @param browser
 	 *            a non-null {@link Browser} instance
 	 * @return a String; may be empty; never null
 	 */
 	public abstract String getText(Browser browser);
 }
