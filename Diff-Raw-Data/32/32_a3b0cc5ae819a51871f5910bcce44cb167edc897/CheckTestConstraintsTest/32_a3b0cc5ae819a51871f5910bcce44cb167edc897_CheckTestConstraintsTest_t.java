 /*******************************************************************************
  * Copyright (c) 2007, 2012 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena;
 
 import java.util.List;
 
 import junit.framework.TestCase;
 
import org.osgi.framework.Bundle;

 import org.eclipse.riena.internal.core.test.collect.NonUITestCase;
 import org.eclipse.riena.internal.core.test.collect.TestCollector;
import org.eclipse.riena.internal.core.test.collect.TestCollectorTest;
import org.eclipse.riena.internal.core.test.collect.testpackage.JUnit3DummyBadlyNamed;
 import org.eclipse.riena.internal.tests.Activator;
 
 /**
  * Check all {@code TestCase}s for any of our test case constraint violations.
  */
 @NonUITestCase
 public class CheckTestConstraintsTest extends TestCase {
 
 	public void testUnmarkedTests() {
		final List<Class<?>> unmarked = TestCollector.collectUnmarkedJUnit3And4(getBundle(), null);
		removeTestCollectorTests(unmarked);
 		if (unmarked.size() > 0) {
 			System.err.println(">> Found unmarked tests:");
 			for (final Class<?> testCase : unmarked) {
 				System.err.println("  unmarked: " + testCase.getName());
 			}
 		}
 		assertEquals(unmarked.size() + " unmarked test(s) found: " + unmarked, 0, unmarked.size());
 	}
 
	/**
	 * @return
	 */
	private Bundle getBundle() {
		return Activator.getDefault().getBundle();
	}

 	public void testBadlyNamedTests() {
		final List<Class<?>> badlyNamed = TestCollector.collectBadlyNamedJUnit3And4(getBundle(), null);
		removeTestCollectorTests(badlyNamed);
 		if (badlyNamed.size() > 0) {
 			System.err.println(">> Found badly named tests:");
 			for (final Class<?> testCase : badlyNamed) {
 				System.err.println("  badly named: " + testCase.getName());
 			}
 		}
 		assertEquals(badlyNamed.size() + " badly named test(s) found: " + badlyNamed, 0, badlyNamed.size());
 	}
 
	/**
	 * Remove all dummy test classes used for the {@link TestCollectorTest}
	 * 
	 */
	private void removeTestCollectorTests(final List<Class<?>> tests) {
		final List<Class<?>> dummyTests = TestCollector.collectJUnit3And4(getBundle(), JUnit3DummyBadlyNamed.class.getPackage(), true);
		tests.removeAll(dummyTests);
	}
 }
