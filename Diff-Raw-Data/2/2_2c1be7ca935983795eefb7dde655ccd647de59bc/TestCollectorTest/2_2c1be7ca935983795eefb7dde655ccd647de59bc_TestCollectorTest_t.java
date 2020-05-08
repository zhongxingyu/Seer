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
 package org.eclipse.riena.internal.core.test.collect;
 
 import static org.junit.Assert.*;
 
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import org.osgi.framework.Bundle;
 
 import org.eclipse.riena.internal.core.test.collect.testpackage.JUnit3DummyBadlyNamed;
 import org.eclipse.riena.internal.core.test.collect.testpackage.JUnit3DummyTest;
 import org.eclipse.riena.internal.core.test.collect.testpackage.JUnit4DummyBadlyNamed;
 import org.eclipse.riena.internal.core.test.collect.testpackage.JUnit4DummyTest;
 import org.eclipse.riena.internal.tests.Activator;
 
 /**
  * the first Riena JUnit4 test case
  */
 @NonUITestCase
 public class TestCollectorTest {
 	private Bundle bundle;
 	private Package withinPackage;
 
 	@Before
	public void setUp() throws Exception {
 		bundle = Activator.getDefault().getBundle();
 		withinPackage = JUnit3DummyTest.class.getPackage();
 	}
 
 	@Test
 	public void testCollectWithJUnit3() throws Exception {
 		final List<Class<? extends TestCase>> found = TestCollector.collectWith(bundle, withinPackage, false, UITestCase.class);
 		assertEquals(1, found.size());
 		assertTrue(found.contains(JUnit3DummyBadlyNamed.class));
 	}
 
 	@Test
 	public void testCollectWithJUnit3And4() throws Exception {
 		final List<Class<?>> found = TestCollector.collectWithJUnit3And4(bundle, withinPackage, false, UITestCase.class);
 		assertEquals(2, found.size());
 		assertTrue(found.contains(JUnit3DummyBadlyNamed.class));
 		assertTrue(found.contains(JUnit4DummyBadlyNamed.class));
 	}
 
 	@Test
 	public void testCollectWithEmpty() throws Exception {
 		assertTrue(TestCollector.collectWith(bundle, withinPackage, false, NonUITestCase.class).isEmpty());
 		assertTrue(TestCollector.collectWithJUnit3And4(bundle, withinPackage, false, NonUITestCase.class).isEmpty());
 	}
 
 	@Test
 	public void testCollectUnmarkedJUnit3() throws Exception {
 		final List<Class<? extends TestCase>> found = TestCollector.collectUnmarked(bundle, withinPackage);
 		assertEquals(1, found.size());
 		assertTrue(found.contains(JUnit3DummyTest.class));
 	}
 
 	@Test
 	public void testCollectUnmarkedJUnit3And4() throws Exception {
 		final List<Class<?>> found = TestCollector.collectUnmarkedJUnit3And4(bundle, withinPackage);
 		assertEquals(2, found.size());
 		assertTrue(found.contains(JUnit3DummyTest.class));
 		assertTrue(found.contains(JUnit4DummyTest.class));
 	}
 
 	@Test
 	public void testCollectJunit3() throws Exception {
 		final List<Class<? extends TestCase>> found = TestCollector.collect(bundle, withinPackage, false);
 		assertEquals(2, found.size());
 		assertTrue(found.contains(JUnit3DummyTest.class));
 		assertTrue(found.contains(JUnit3DummyBadlyNamed.class));
 	}
 
 	@Test
 	public void testCollectJUnit3And4() throws Exception {
 		final List<Class<?>> found = TestCollector.collectJUnit3And4(bundle, withinPackage, false);
 		assertEquals(4, found.size());
 		assertTrue(found.contains(JUnit3DummyTest.class));
 		assertTrue(found.contains(JUnit3DummyBadlyNamed.class));
 		assertTrue(found.contains(JUnit4DummyTest.class));
 		assertTrue(found.contains(JUnit4DummyBadlyNamed.class));
 	}
 
 	@Test
 	public void testCollectBadlyNamedJUnit3() throws Exception {
 		final List<Class<? extends TestCase>> found = TestCollector.collectBadlyNamed(bundle, withinPackage);
 		assertEquals(1, found.size());
 		assertTrue(found.contains(JUnit3DummyBadlyNamed.class));
 	}
 
 	@Test
 	public void testCollectBadlyNamedJUnit3And4() throws Exception {
 		final List<Class<?>> found = TestCollector.collectBadlyNamedJUnit3And4(bundle, withinPackage);
 		assertEquals(2, found.size());
 		assertTrue(found.contains(JUnit3DummyBadlyNamed.class));
 		assertTrue(found.contains(JUnit4DummyBadlyNamed.class));
 	}
 }
