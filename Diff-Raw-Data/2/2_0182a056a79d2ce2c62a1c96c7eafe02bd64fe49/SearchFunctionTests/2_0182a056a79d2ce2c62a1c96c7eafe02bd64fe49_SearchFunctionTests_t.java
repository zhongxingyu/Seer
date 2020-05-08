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
 
 import static org.eclipse.dltk.javascript.core.tests.AllTests.PLUGIN_ID;
 import static org.eclipse.dltk.javascript.core.tests.contentassist.AbstractContentAssistTest.lastPositionInFile;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.compiler.env.IModuleSource;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.search.MethodDeclarationMatch;
 import org.eclipse.dltk.core.search.MethodReferenceMatch;
 import org.eclipse.dltk.core.tests.Skip;
 import org.eclipse.dltk.core.tests.model.TestSearchResults;
 
 public class SearchFunctionTests extends AbstractSearchTest {
 
 	public SearchFunctionTests(String testName) {
 		super(PLUGIN_ID, testName, "selection");
 	}
 
 	public static Suite suite() {
 		return new Suite(SearchFunctionTests.class);
 	}
 
 	public void testFunctionGlobalField() throws CoreException {
 		IModuleSource module = getModule("functions.js");
 		IModelElement[] elements = select(module,
 				lastPositionInFile("fun1", module, false));
 		assertEquals(1, elements.length);
 		final IMethod method = (IMethod) elements[0];
 		final TestSearchResults results = search(method, ALL_OCCURRENCES);
 		assertEquals(2, results.size());
 		assertTrue(results.getMatch(0) instanceof MethodDeclarationMatch);
 		assertTrue(results.getMatch(1) instanceof MethodReferenceMatch);
 	}
 
 	public void testFunctionLocalField() throws CoreException {
 		if (notYetImplemented())
 			return;
 		IModuleSource module = getModule("functions.js");
 		IModelElement[] elements = select(module,
 				lastPositionInFile("fun2", module, false));
 		assertEquals(1, elements.length);
 		final IMethod method = (IMethod) elements[0];
 		final TestSearchResults results = search(method, ALL_OCCURRENCES);
 		assertEquals(2, results.size());
 		assertTrue(results.getMatch(0) instanceof MethodDeclarationMatch);
 		assertTrue(results.getMatch(1) instanceof MethodReferenceMatch);
 	}
 
 	public void testFunctionLocalFieldWithDoubleName() throws CoreException {
 		IModuleSource module = getModule("functions.js");
 		IModelElement[] elements = select(module,
 				lastPositionInFile("fun4", module, false));
 		assertEquals(0, elements.length);
 	}
 
 	@Skip
 	public void testFunctionThisField() throws CoreException {
 		IModuleSource module = getModule("functions.js");
 		IModelElement[] elements = select(module,
 				lastPositionInFile("fun5", module, false));
 		assertEquals(1, elements.length);
 		final IMethod method = (IMethod) elements[0];
 		final TestSearchResults results = search(method, ALL_OCCURRENCES);
 		assertEquals(2, results.size());
 		assertTrue(results.getMatch(0) instanceof MethodDeclarationMatch);
 		assertTrue(results.getMatch(1) instanceof MethodReferenceMatch);
 	}
 
 	public void testFunctionThisFieldOuterCall() throws CoreException {
 		IModuleSource module = getModule("functions.js");
 		IModelElement[] elements = select(module,
 				lastPositionInFile("fun6", module, false));
 		assertEquals(1, elements.length);
 		final IMethod method = (IMethod) elements[0];
 		final TestSearchResults results = search(method, ALL_OCCURRENCES);
 		assertEquals(2, results.size());
 		assertTrue(results.getMatch(0) instanceof MethodDeclarationMatch);
 		assertTrue(results.getMatch(1) instanceof MethodReferenceMatch);
 	}
 
 	public void testFunctionThisFieldWithInnerFunction() throws CoreException {
 		IModuleSource module = getModule("functions.js");
 		IModelElement[] elements = select(module,
 				lastPositionInFile("fun8", module, false));
 		assertEquals(0, elements.length);
 	}
 
 	public void testFunctionThisFieldWithInnerFunctionCall()
 			throws CoreException {
 		IModuleSource module = getModule("functions.js");
 		IModelElement[] elements = select(module,
 				lastPositionInFile("funA", module, false));
 		assertEquals(0, elements.length);
 	}
 
 	public void testGlobalInitializerFunctionField() throws CoreException {
 		if (notYetImplemented())
 			return;
 		IModuleSource module = getModule("functions.js");
 		IModelElement[] elements = select(module,
 				lastPositionInFile("funB", module, false));
 		assertEquals(1, elements.length);
 		final IMethod method = (IMethod) elements[0];
 		final TestSearchResults results = search(method, ALL_OCCURRENCES);
 		assertEquals(results.toString(), 2, results.size());
 		assertTrue(results.getMatch(0) instanceof MethodDeclarationMatch);
 		assertTrue(results.getMatch(1) instanceof MethodReferenceMatch);
 	}
 
 	public void testLocalInitializerFunctionField() throws CoreException {
 		if (notYetImplemented())
 			return;
 		IModuleSource module = getModule("functions.js");
 		IModelElement[] elements = select(module,
 				lastPositionInFile("funC", module, false));
 		assertEquals(1, elements.length);
 		final IModelElement method = elements[0];
 		final TestSearchResults results = search(method, ALL_OCCURRENCES);
 		assertEquals(2, results.size());
 		assertTrue(results.getMatch(0) instanceof MethodDeclarationMatch);
 		assertTrue(results.getMatch(1) instanceof MethodReferenceMatch);
 	}
 
 }
