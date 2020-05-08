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
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.tests.RienaTestCase;
 import org.eclipse.riena.tests.collect.UITestCase;
 
 /**
  * Test the {@code MarkSupport} class.
  */
 @UITestCase
 public class MarkerSupportTest extends RienaTestCase {
 
 	private static final String HIDE_DISABLED_RIDGET_CONTENT = "HIDE_DISABLED_RIDGET_CONTENT";
 
 	public void testHideDiabledRidgetContentSystemProperty() throws IOException {
 		System.clearProperty(HIDE_DISABLED_RIDGET_CONTENT);
 		assertTrue(getHideDisabledRidgetContent());
 
 		System.setProperty(HIDE_DISABLED_RIDGET_CONTENT, Boolean.FALSE.toString());
 		assertFalse(getHideDisabledRidgetContent());
 
 		System.setProperty(HIDE_DISABLED_RIDGET_CONTENT, Boolean.TRUE.toString());
 		assertTrue(getHideDisabledRidgetContent());
 	}
 
 	private static boolean getHideDisabledRidgetContent() throws IOException {
 		MarkSupportClassLoader freshLoader = new MarkSupportClassLoader();
 		Class<?> markerSupportClass = freshLoader.getFreshMarkSupportClass();
 		return ReflectionUtils.getHidden(markerSupportClass, HIDE_DISABLED_RIDGET_CONTENT);
 	}
 
 	/**
 	 * This {@code ClassLoader}s method {@code getFreshMarkSupportClass()}
 	 * retrieves with each call a new, fresh {@code MarkSupport} class. This
 	 * allows testing of the static field which gets initialized on class load.
 	 */
 	private static class MarkSupportClassLoader extends ClassLoader {
 
 		public Class<?> getFreshMarkSupportClass() throws IOException {
 			String resource = MarkerSupport.class.getName().replace('.', '/') + ".class";
 			URL classURL = MarkerSupportTest.class.getClassLoader().getResource(resource);
 			InputStream is = classURL.openStream();
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			int ch = -1;
 			while ((ch = is.read()) != -1) {
 				baos.write(ch);
 			}
 			byte[] bytes = baos.toByteArray();
			Class<?> cl = super.defineClass(MarkerSupport.class.getName(), bytes, 0, bytes.length);
 			resolveClass(cl);
 			return cl;
 		}
 	}
 }
