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
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import junit.framework.TestCase;
 
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Display;
 
 /**
 * Tests for resource sharing helper methods.
 * 
 * @see Activator#getSharedColor(Display, String)
  */
 public class SharedResourcesTest extends TestCase {
 
 	public void testSharedColors() {
 		Display display = Display.getDefault();
 
 		Color colorFlash1 = Activator.getSharedColor(display, SharedColors.COLOR_FLASH_ERROR);
 		Color colorFlash2 = Activator.getSharedColor(display, SharedColors.COLOR_FLASH_ERROR);
 
 		assertNotNull(colorFlash1);
 		assertNotNull(colorFlash2);
 		assertSame(colorFlash1, colorFlash2);
 
 		Color colorOutput = Activator.getSharedColor(display, SharedColors.COLOR_OUTPUT);
 
 		assertNotNull(colorOutput);
 		assertNotSame(colorFlash2, colorOutput);
 
		try {
			Activator.getSharedColor(null, SharedColors.COLOR_FLASH_ERROR);
			fail();
		} catch (RuntimeException rex) {
			// expected
		}
 
 		try {
			Activator.getSharedColor(display, "does_not_exist");
 			fail();
 		} catch (RuntimeException rex) {
 			// expected
 		}
 
 		try {
 			Activator.getSharedColor(display, null);
 			fail();
 		} catch (RuntimeException rex) {
 			// expected
 		}
 	}
 }
