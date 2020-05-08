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
 package org.eclipse.riena.internal.core.test.collect;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Modifier;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.osgi.framework.Bundle;
 
 import org.eclipse.riena.core.util.Iter;
 
 /**
  * A helper for collecting test classes.
  */
 public final class TestCollector {
 
 	private static final String PLUS = " + "; //$NON-NLS-1$
 
 	private TestCollector() {
 		// utility
 	}
 
 	/**
 	 * Create a {@code TestSuite} that contains all test case in the given
 	 * bundle.
 	 * 
 	 * @param bundle
 	 *            bundle to collect all {@code TestCase}s
 	 * @param withinPackage
 	 *            if not null, only {@code TestCase}s within this package are
 	 *            collected
 	 * @param annotationClasses
 	 *            only {@code TestCase}s that have one of these annotations are
 	 *            collected
 	 * @return
 	 */
 	public static TestSuite createTestSuiteWith(final Bundle bundle, final Package withinPackage,
 			final Class<? extends Annotation>... annotationClasses) {
 		return createTestSuiteWith(bundle, withinPackage, false, annotationClasses);
 	}
 
 	/**
 	 * Create a {@code TestSuite} that contains all test case in the given
 	 * bundle.
 	 * 
 	 * @param bundle
 	 *            bundle to collect all {@code TestCase}s
 	 * @param withinPackage
 	 *            if not null, only {@code TestCase}s within this package are
 	 *            collected
 	 * @param annotationClasses
 	 *            only {@code TestCase}s that have one of these annotations are
 	 *            collected
 	 * @param subPackages
 	 *            on true also collect sub-packages
 	 * @return
 	 */
 	public static TestSuite createTestSuiteWith(final Bundle bundle, final Package withinPackage,
 			final boolean subPackages, final Class<? extends Annotation>... annotationClasses) {
 		final StringBuilder bob = new StringBuilder("Tests within bundle '").append(bundle.getSymbolicName()).append( //$NON-NLS-1$
 				"' and package '"); //$NON-NLS-1$
 		bob.append(withinPackage == null ? "all" : withinPackage.getName()).append("'").append(" recursive '").append( //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				subPackages).append("'"); //$NON-NLS-1$
 		bob.append(" and restricted to '"); //$NON-NLS-1$
 		for (final Class<? extends Annotation> annotationClass : annotationClasses) {
 			bob.append(annotationClass.getSimpleName()).append(PLUS);
 		}
 		if (annotationClasses.length > 0) {
 			bob.setLength(bob.length() - PLUS.length());
 		} else {
 			bob.append("none"); //$NON-NLS-1$
 		}
 		bob.append("'."); //$NON-NLS-1$
 		final TestSuite suite = new TestSuite(bob.toString());
 		for (final Class<? extends TestCase> clazz : collectWith(bundle, withinPackage, subPackages, annotationClasses)) {
 			suite.addTestSuite(clazz);
 		}
 		return suite;
 	}
 
 	/**
 	 * Collect all {@code TestCase}e within the given bundle.
 	 * 
 	 * @param bundle
 	 *            bundle to collect all {@code TestCase}s
 	 * @param withinPackage
 	 *            if not null, only {@code TestCase}s within this package are
 	 *            collected
 	 * @param annotationClasses
 	 *            only {@code TestCase}s that have one of these annotations are
 	 *            collected
 	 * @param subPackages
 	 *            on true also collect sub-packages
 	 * @return
 	 */
 	public static List<Class<? extends TestCase>> collectWith(final Bundle bundle, final Package withinPackage,
 			final boolean subPackages, final Class<? extends Annotation>... annotationClasses) {
 		final List<Class<? extends TestCase>> testClasses = new ArrayList<Class<? extends TestCase>>();
 
 		for (final Class<? extends TestCase> testClass : collect(bundle, withinPackage, subPackages)) {
 			boolean collect = annotationClasses.length == 0 ? true : false;
 			for (final Class<? extends Annotation> annotationClass : annotationClasses) {
 				collect = collect || testClass.isAnnotationPresent(annotationClass);
 			}
 			if (collect) {
 				testClasses.add(testClass);
 			}
 		}
 
 		return testClasses;
 	}
 
 	/**
 	 * Collect all unmarked {@code TestCase}s within the given bundle.
 	 * 
 	 * @param bundle
 	 *            bundle to collect all {@code TestCase}s
 	 * @param withinPackage
 	 *            if not null, only {@code TestCase}s within this package are
 	 *            collected
 	 * @return
 	 */
 	public static List<Class<? extends TestCase>> collectUnmarked(final Bundle bundle, final Package withinPackage) {
 		final List<Class<? extends TestCase>> testClasses = new ArrayList<Class<? extends TestCase>>();
 
 		for (final Class<? extends TestCase> testClass : collect(bundle, withinPackage, true)) {
 			if (testClass.getAnnotations().length == 0) {
 				testClasses.add(testClass);
 			}
 		}
 
 		return testClasses;
 	}
 
 	/**
 	 * Collect all badly named {@code TestCase}e within the given bundle.
 	 * 
 	 * @param bundle
 	 *            bundle to collect all {@code TestCase}s
 	 * @param withinPackage
 	 *            if not null, only {@code TestCase}s within this package are
 	 *            collected
 	 * @return
 	 */
 	public static List<Class<? extends TestCase>> collectBadlyNamed(final Bundle bundle, final Package withinPackage) {
 		final List<Class<? extends TestCase>> testClasses = new ArrayList<Class<? extends TestCase>>();
 		final Pattern testClassPattern = Pattern.compile(".*Test\\d*$"); //$NON-NLS-1$
 		for (final Class<? extends TestCase> testClass : collect(bundle, withinPackage, true)) {
 			if (!testClassPattern.matcher(testClass.getName()).matches()) {
 				testClasses.add(testClass);
 			}
 		}
 
 		return testClasses;
 	}
 
 	/**
 	 * Collect all {@code TestCase}e within the given bundle.
 	 * 
 	 * @param bundle
 	 *            bundle to collect all {@code TestCase}s
 	 * @param withinPackage
 	 *            if not null, only {@code TestCase}s within this package are
 	 *            collected
 	 * @param subPackages
 	 *            on true also collect sub-packages
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static List<Class<? extends TestCase>> collect(final Bundle bundle, final Package withinPackage,
 			final boolean subPackages) {
 		final List<Class<? extends TestCase>> testClasses = new ArrayList<Class<? extends TestCase>>();
 		final Enumeration<URL> allClasses = bundle.findEntries("", "*.class", true); //$NON-NLS-1$ //$NON-NLS-2$
 		for (final URL entryURL : Iter.able(allClasses)) {
 			final String url = entryURL.toExternalForm();
 			if (url.contains("$")) { //$NON-NLS-1$
 				// Skip inner classes
 				continue;
 			}
 			final Class<?> clazz = getClass(bundle, entryURL);
 			if (clazz == null) {
				trace("Could not get class name from ", url); //$NON-NLS-1$
 				continue;
 			}
 			if (!TestCase.class.isAssignableFrom(clazz)) {
 				trace("Not a TestCase: ", clazz.getName()); //$NON-NLS-1$
 				continue;
 			}
 			final String className = clazz.getName();
 			if (withinPackage != null) {
 				if (subPackages) {
 					if (!(className.startsWith(withinPackage.getName()) && className.endsWith(clazz.getSimpleName()))) {
 						continue;
 					}
 				} else {
 					if (!className.equals(withinPackage.getName() + "." + clazz.getSimpleName())) { //$NON-NLS-1$
 						continue;
 					}
 				}
 			}
 			if (clazz.isAnnotationPresent(NonGatherableTestCase.class)) {
 				continue;
 			}
 			if (Modifier.isAbstract(clazz.getModifiers())) {
 				continue;
 			}
 			testClasses.add((Class<TestCase>) clazz);
 		}
 		return testClasses;
 	}
 
 	private static Class<?> getClass(final Bundle bundle, final URL entryURL) {
 		final String entry = entryURL.toExternalForm().replace(".class", "").replace('/', '.'); //$NON-NLS-1$ //$NON-NLS-2$
 		// Brute force detecting of how many chars we have to skip to find a class within the url
 		final String name = entry;
 		int dot = 0;
 		while ((dot = name.indexOf('.', dot)) != -1) {
 			final String className = name.substring(dot + 1);
 			try {
 				return bundle.loadClass(className);
 			} catch (final ClassNotFoundException e) {
 				dot++;
 			} catch (final NoClassDefFoundError e) {
 				dot++;
 			}
 		}
 		return null;
 	}
 
 	private static void trace(final Object... objects) {
 		final StringBuilder bob = new StringBuilder();
 		for (final Object object : objects) {
 			bob.append(object);
 		}
 		System.err.println(bob.toString());
 	}
 
 }
