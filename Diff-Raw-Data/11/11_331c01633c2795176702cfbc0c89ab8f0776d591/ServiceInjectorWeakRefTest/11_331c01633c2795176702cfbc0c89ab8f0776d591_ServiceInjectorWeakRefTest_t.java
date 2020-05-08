 /*******************************************************************************
  * Copyright (c) 2007, 2013 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.core.injector.service;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import junit.framework.AssertionFailedError;
 
 import org.osgi.framework.ServiceRegistration;
 
 import org.eclipse.riena.core.injector.Inject;
 import org.eclipse.riena.core.test.RienaTestCase;
 import org.eclipse.riena.core.test.collect.NonUITestCase;
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.core.util.WeakRef;
 
 /**
 * Tests the {@code ServiceInjector} with respect to the {@code WeakRef} to the target.
  */
 @NonUITestCase
 public class ServiceInjectorWeakRefTest extends RienaTestCase {
 
 	public void testWeakRefFail() throws IOException {
 		try {
 			testWeakRef(false);
 			fail();
 		} catch (final AssertionFailedError e) {
 			ok();
 		}
 	}
 
 	public void testWeakRefSucceed() throws IOException {
 		testWeakRef(true);
 	}
 
	@SuppressWarnings("nls")
 	private void testWeakRef(final boolean withNulling) throws IOException {
 		printTestName();
 		Target target = new Target();
 
 		final DepOne depOne = new DepOne();
		final ServiceRegistration<?> reg = getContext().registerService(DepOne.class.getName(), depOne, null);
 		ServiceInjector shot = null;
 		try {
 			shot = Inject.service(DepOne.class.getName()).into(target).andStart(getContext());
 			assertEquals(1, target.count("bind", DepOne.class));
 
 			if (withNulling) {
 				target = null;
 			}
 			runOutOfMemory();
 
 			final WeakRef<Object> targetRef = ReflectionUtils.getHidden(shot, "targetRef");
 			assertNull(targetRef.get());
 			final Object state = ReflectionUtils.getHidden(shot, "state");
			assertTrue(state.toString().equals("STOPPED") || state.toString().equals("STOPPING"));
 		} finally {
 			shot.stop();
 			reg.unregister();
 		}
 	}
 
 	private void runOutOfMemory() throws IOException {
 		try {
 			final OutputStream os = new ByteArrayOutputStream();
 			while (true) {
 				os.write(new byte[1024 * 1024]);
 			}
 		} catch (final OutOfMemoryError e) {
 			System.gc();
 		}
 
 	}
 }
