 /*******************************************************************************
  * Copyright (c) 2004, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.server.ui.tests;
 
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jst.server.ui.internal.JavaServerUIPlugin;
 import junit.framework.TestCase;
 
 public class ExistenceTest extends TestCase {
 	public void testPluginExists() {
 		assertNotNull(JavaServerUIPlugin.getInstance());
 	}
 
	protected void setUp() throws Exception {
		super.setUp();
		ErrorDialog.AUTOMATED_MODE=true;
	}
 }
