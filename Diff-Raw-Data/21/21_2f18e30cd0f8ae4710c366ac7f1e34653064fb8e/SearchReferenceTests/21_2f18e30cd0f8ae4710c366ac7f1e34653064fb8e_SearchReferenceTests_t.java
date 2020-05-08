 /*******************************************************************************
  * Copyright (c) 2010 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.javascript.core.tests.search;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.core.IField;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.search.IDLTKSearchConstants;
 import org.eclipse.dltk.core.tests.ProjectSetup;
 import org.eclipse.dltk.core.tests.model.TestSearchResults;
 import org.eclipse.dltk.javascript.core.tests.AllTests;
 import org.junit.Assert;
 import org.junit.ClassRule;
 import org.junit.Test;
 
 public class SearchReferenceTests extends Assert implements
 		IDLTKSearchConstants {
 
 	@ClassRule
 	public static final ProjectSetup PROJECT = new ProjectSetup(
 			AllTests.WORKSPACE, "search",
 			ProjectSetup.Option.WAIT_INDEXES_READY);
 
 	@Test
 	public void testFunctionHello1() throws CoreException {
 		final TestSearchResults results = PROJECT.search("hello", METHOD,
 				REFERENCES);
 		assertEquals(1, results.size());
 		final IModelElement method = results.locate(IField.class, "a");
 		final IModelElement parent = method.getParent();
 		assertEquals(IModelElement.SOURCE_MODULE, parent.getElementType());
 		assertEquals("a.js", parent.getElementName());
 	}
 
 	@Test
 	public void testVarA() throws CoreException {
 		final TestSearchResults results = PROJECT
 				.search("a", FIELD, REFERENCES);
 		assertEquals(1, results.size());
 		final IModelElement module = results.locate(IField.class, "b");
 		final IModelElement parent = module.getParent();
 		assertEquals(IModelElement.SOURCE_MODULE, parent.getElementType());
 		assertEquals("b.js", parent.getElementName());
 	}
 
 	@Test
 	public void testMethodRef() throws CoreException {
 		final TestSearchResults results = PROJECT.search("size", METHOD,
 				REFERENCES);
 		assertEquals(1, results.size());
 		final IModelElement element = results.get(0);
 		final IModelElement parent = element
 				.getAncestor(IModelElement.SOURCE_MODULE);
 		assertEquals("size.js", parent.getElementName());
 	}
 
 	@Test
 	public void testPropertyRef() throws CoreException {
 		final TestSearchResults results = PROJECT.search("length", FIELD,
 				REFERENCES);
 		assertEquals(1, results.size());
 		final IModelElement element = results.get(0);
 		final IModelElement parent = element
 				.getAncestor(IModelElement.SOURCE_MODULE);
 		assertEquals("length.js", parent.getElementName());
 	}
 	
 	@Test
 	public void testThisFunctionTest() throws CoreException {
 		final TestSearchResults results = PROJECT.search("myfunction", METHOD,
 				REFERENCES);
 		assertEquals(2, results.size());
 		final IModelElement element = results.get(0);
 		final IModelElement parent = element
 				.getAncestor(IModelElement.SOURCE_MODULE);
 		assertEquals("thisobject.js", parent.getElementName());
 	}
 	
 	@Test
 	public void testThisVariableTest() throws CoreException {
 		final TestSearchResults results = PROJECT.search("myvariable", FIELD,
 				REFERENCES);
 		assertEquals(2, results.size());
 		final IModelElement element = results.get(0);
 		final IModelElement parent = element
 				.getAncestor(IModelElement.SOURCE_MODULE);
 		assertEquals("thisobject.js", parent.getElementName());
 	}
 	
 	@Test
 	public void testMethodReferenceThatsReferencesInAnArgument() throws CoreException {
 		final TestSearchResults results = PROJECT.search("convertToExternalURL", METHOD,
 				REFERENCES);
 		assertEquals(1, results.size());
 		final IModelElement element = results.get(0);
 		final IModelElement parent = element
 				.getAncestor(IModelElement.SOURCE_MODULE);
 		assertEquals("globals.js", parent.getElementName());
 	}
	
	@Test
	public void testMethodReferenceThatsReferencesInAVariableFunctionInit() throws CoreException {
		final TestSearchResults results = PROJECT.search("SvyException", METHOD,
				REFERENCES);
		assertEquals(2, results.size());
		final IModelElement element = results.get(0);
		final IModelElement parent = element
				.getAncestor(IModelElement.SOURCE_MODULE);
		assertEquals("functioncallsinfunctionvariableinit.js", parent.getElementName());
	}
 }
