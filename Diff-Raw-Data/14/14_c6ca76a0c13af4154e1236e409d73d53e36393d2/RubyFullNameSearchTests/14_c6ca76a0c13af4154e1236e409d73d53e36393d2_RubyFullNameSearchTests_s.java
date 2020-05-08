 /*******************************************************************************
  * Copyright (c) 2008 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.ruby.core.tests.search;
 
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.tests.model.AbstractSingleProjectSearchTests;
 import org.eclipse.dltk.core.tests.model.TestSearchResults;
 import org.eclipse.dltk.ruby.core.tests.Activator;
 
 public class RubyFullNameSearchTests extends AbstractSingleProjectSearchTests {
 
 	public RubyFullNameSearchTests(String name) {
 		super(Activator.PLUGIN_ID, name, "search");
 	}
 
 	public static Suite suite() {
 		return new Suite(RubyFullNameSearchTests.class);
 	}
 
 	private final String shortTypeName = "Search002";
 	private final String fullTypeName = "Module001::" + shortTypeName;
 	private final String methodName = "method003";
 
 	public void testFullTypeNameSearch() throws Exception {
 		final TestSearchResults results = search(fullTypeName, TYPE,
 				DECLARATIONS);
 		assertEquals(1, results.size());
 		results.assertType(fullTypeName);
 	}
 
 	public void testShortTypeNameSearch() throws Exception {
 		final TestSearchResults results = search(shortTypeName, TYPE,
 				DECLARATIONS);
 		assertEquals(1, results.size());
 		results.assertType(fullTypeName);
 	}
 
 	public void testShortMethodNameSearch() throws Exception {
 		final TestSearchResults results = search(methodName, METHOD,
 				DECLARATIONS);
 		assertEquals(1, results.size());
 		results.assertMethod(methodName);
 		IMethod method = (IMethod) results.locate(IMethod.class, methodName);
 		assertNotNull(method);
 		assertNotNull(method.getParent());
 		assertEquals(fullTypeName, ((IType) method.getParent())
				.getFullyQualifiedName("::"));
 	}
 
 	public void testFullMethodNameSearch() throws Exception {
 		final TestSearchResults results = search(fullTypeName + "::"
 				+ methodName, METHOD, DECLARATIONS);
 		assertEquals(1, results.size());
 		results.assertMethod(methodName);
 		IMethod method = (IMethod) results.locate(IMethod.class, methodName);
 		assertNotNull(method);
 		assertNotNull(method.getParent());
 		assertEquals(fullTypeName, ((IType) method.getParent())
				.getFullyQualifiedName("::"));
 	}
 
 }
