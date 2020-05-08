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
 
 import static org.eclipse.dltk.core.index2.search.ISearchEngine.SearchFor.ALL_OCCURRENCES;
 import static org.eclipse.dltk.core.tests.CodeAssistUtil.on;
 import static org.hamcrest.CoreMatchers.instanceOf;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.core.ILocalVariable;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.search.LocalVariableDeclarationMatch;
 import org.eclipse.dltk.core.search.LocalVariableReferenceMatch;
 import org.eclipse.dltk.core.tests.ProjectSetup;
 import org.eclipse.dltk.core.tests.model.TestSearchResults;
 import org.eclipse.dltk.javascript.core.tests.AllTests;
 import org.junit.Assert;
 import org.junit.ClassRule;
 import org.junit.Test;
 
 public class SearchLocalsTests extends Assert {
 
 	@ClassRule
 	public static final ProjectSetup PROJECT = new ProjectSetup(
 			AllTests.WORKSPACE, "selection",
 			ProjectSetup.Option.WAIT_INDEXES_READY);
 
 	@Test
 	public void testLocalVariableReference() throws CoreException {
 		ISourceModule module = PROJECT.getSourceModule("src", "locals.js");
 		IModelElement[] elements = on(module).beforeLast("beta").codeSelect();
 		assertEquals(1, elements.length);
 		final ILocalVariable var = (ILocalVariable) elements[0];
 		final TestSearchResults results = PROJECT.search(var, ALL_OCCURRENCES);
 		assertEquals(3, results.size());
 		assertThat(results.getMatch(0),
 				instanceOf(LocalVariableDeclarationMatch.class));
 		assertThat(results.getMatch(1),
 				instanceOf(LocalVariableReferenceMatch.class));
 		assertThat(results.getMatch(2),
 				instanceOf(LocalVariableReferenceMatch.class));
 	}
 
 	@Test
 	public void testLocalVariableAsArgumentReference() throws CoreException {
 		ISourceModule module = PROJECT.getSourceModule("src", "locals.js");
 		IModelElement[] elements = on(module).beforeLast("alpha").codeSelect();
 		assertEquals(1, elements.length);
 		final ILocalVariable var = (ILocalVariable) elements[0];
 		final TestSearchResults results = PROJECT.search(var, ALL_OCCURRENCES);
 		assertEquals(results.toString(), 2, results.size());
 		assertThat(results.getMatch(0),
 				instanceOf(LocalVariableDeclarationMatch.class));
 		assertThat(results.getMatch(1),
 				instanceOf(LocalVariableReferenceMatch.class));
 	}
 
 	@Test
 	public void testArgumentJSDocMatch() throws CoreException {
 		ISourceModule module = PROJECT.getSourceModule("src", "locals.js");
 		IModelElement[] elements = on(module).beforeLast("foo").codeSelect();
 		assertEquals(1, elements.length);
 		final ILocalVariable var = (ILocalVariable) elements[0];
 		final TestSearchResults results = PROJECT.search(var, ALL_OCCURRENCES);
 		assertEquals(results.toString(), 3, results.size());
 		// workaround for task in JavaScriptMatchLocator.endVisit(IModelElement)
 		results.sortByOffset();
 		assertThat(results.getMatch(0),
 				instanceOf(LocalVariableReferenceMatch.class));
 		assertTrue(results.getMatch(0).isInsideDocComment());
 		assertThat(results.getMatch(1),
 				instanceOf(LocalVariableDeclarationMatch.class));
 		assertThat(results.getMatch(2),
 				instanceOf(LocalVariableReferenceMatch.class));
 		assertFalse(results.getMatch(2).isInsideDocComment());
 	}
 
 }
