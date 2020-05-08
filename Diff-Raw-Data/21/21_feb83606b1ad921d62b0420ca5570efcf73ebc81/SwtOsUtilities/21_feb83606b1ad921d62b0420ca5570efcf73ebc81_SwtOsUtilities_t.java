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
 package org.eclipse.riena.navigation.ui.swt.utils;
 
 import org.eclipse.riena.core.injector.Inject;
 import org.eclipse.riena.internal.navigation.ui.swt.Activator;
 import org.eclipse.riena.navigation.ui.swt.utils.extpoint.ISwtUtililityExtPoint;
 import org.eclipse.riena.navigation.ui.swt.utils.extpoint.ISwtUtility;
 import org.eclipse.swt.widgets.Control;
 
 /**
  * A collection of utility methods which are depending on the operation system.<br>
  * This is an implementation for Windows (win32).
  */
 public class SwtOsUtilities {
 
 	private ISwtUtility swtUtility;
 
 	public SwtOsUtilities() {
 		Inject
 				.extension("org.eclipse.riena.navigation.ui.swt.swtutility").useType(ISwtUtililityExtPoint.class).into(this) //$NON-NLS-1$
 				.bind("setSwtUtilExtension").andStart(Activator.getContext()); //$NON-NLS-1$
 	}
 
 	public void setSwtUtilExtension(ISwtUtililityExtPoint[] swtUtilArray) {
		if (swtUtilArray.length == 0) {
			swtUtility = null;
			return;
		}
		// Assert.isNotNull(swtUtilArray);
		// Assert.isTrue(swtUtilArray.length == 1);
 		swtUtility = swtUtilArray[0].createImplementation();
 	}
 
 	/**
 	 * Hiddes the vertical and horizontal scroll bar of the given control.
 	 */
 	public void hideSrollBars(Control control) {
 
 		if (swtUtility != null) {
			swtUtility.hideScrollBars(control);
 		}
 	}
 }
