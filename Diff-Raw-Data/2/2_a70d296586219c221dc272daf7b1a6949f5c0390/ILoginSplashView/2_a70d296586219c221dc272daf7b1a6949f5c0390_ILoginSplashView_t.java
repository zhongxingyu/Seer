 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.ui.swt.login;
 
 import org.eclipse.swt.widgets.Composite;
 
 /**
  * The interface which the login splash dialog view should implement.
  */
 public interface ILoginSplashView {
 
 	/**
 	 * Build and open the dialog using parent.
 	 */
	void build(Composite parent);
 
 	/**
 	 * Returns the result of the login operation. The following conventions have
 	 * to be considered:
 	 * <ol>
 	 * <li>IApplication.EXIT_OK indicates that the login was successful,</li>
 	 * <li>In case of some other result the login operation was aborted.</li>
 	 * </ol>
 	 * 
 	 * @return the result of the login.
 	 */
 	int getResult();
 }
