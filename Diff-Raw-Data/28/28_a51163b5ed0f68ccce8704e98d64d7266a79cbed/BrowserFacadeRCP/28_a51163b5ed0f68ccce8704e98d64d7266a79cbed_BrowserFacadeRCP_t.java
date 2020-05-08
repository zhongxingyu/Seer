 /*******************************************************************************
  * Copyright (c) 2007, 2014 compeople AG and others.
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
  * Implements {@link BrowserFacade} for RCP.
  */
 public class BrowserFacadeRCP extends BrowserFacade {
 
 	@Override
 	public String getText(final Browser browser) {
		// check https://bugs.eclipse.org/bugs/show_bug.cgi?id=433526
		// if you get a org.eclipse.swt.SWTException: Failed to change Variant type result = -2147352571
 		return browser.getText();
 	}
 
 }
