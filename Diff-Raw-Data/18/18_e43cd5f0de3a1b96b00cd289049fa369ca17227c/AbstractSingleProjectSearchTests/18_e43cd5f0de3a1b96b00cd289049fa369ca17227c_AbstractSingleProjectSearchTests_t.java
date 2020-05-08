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
 package org.eclipse.dltk.core.tests.model;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.search.IDLTKSearchConstants;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.SearchEngine;
 import org.eclipse.dltk.core.search.SearchParticipant;
 import org.eclipse.dltk.core.search.SearchPattern;
 
 public class AbstractSingleProjectSearchTests extends AbstractModelTests
 		implements IDLTKSearchConstants {
 
 	protected static final int EXACT_RULE = SearchPattern.R_EXACT_MATCH
 			| SearchPattern.R_CASE_SENSITIVE;
 
 	private final String scriptProjectName;
 
 	protected AbstractSingleProjectSearchTests(String testPluginName,
 			String testName, String scriptProjectName) {
 		super(testPluginName, testName);
 		this.scriptProjectName = scriptProjectName;
 	}
 
 	public void setUpSuite() throws Exception {
 		super.setUpSuite();
 		setUpScriptProject(scriptProjectName);
 		waitUntilIndexesReady();
 	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (!getScriptProject().exists())
		{
			setUpSuite();
		}
	}
	
	protected IScriptProject getScriptProject()
	{
		return getScriptProject(scriptProjectName);
	}


 
 	public void tearDownSuite() throws Exception {
 		deleteProject(scriptProjectName);
 		super.tearDownSuite();
 	}
 
 	protected TestSearchResults search(String patternString, int searchFor,
 			int limitTo) throws CoreException {
 		return search(patternString, searchFor, limitTo, EXACT_RULE);
 	}
 
 	protected TestSearchResults search(String patternString, int searchFor,
 			int limitTo, int matchRule) throws CoreException {
 		if (patternString.indexOf('*') != -1
 				|| patternString.indexOf('?') != -1) {
 			matchRule |= SearchPattern.R_PATTERN_MATCH;
 		}
 		final IDLTKSearchScope scope = SearchEngine
 				.createSearchScope(getScriptProject(scriptProjectName));
 		final IDLTKLanguageToolkit toolkit = scope.getLanguageToolkit();
 		final SearchPattern pattern = SearchPattern.createPattern(
 				patternString, searchFor, limitTo, matchRule, toolkit);
 		assertNotNull("Pattern should not be null", pattern);
 		final TestSearchResults results = new TestSearchResults();
 		final SearchParticipant[] participants = new SearchParticipant[] { SearchEngine
 				.getDefaultSearchParticipant() };
 		new SearchEngine().search(pattern, participants, scope, results, null);
 		return results;
 	}
 
 }
