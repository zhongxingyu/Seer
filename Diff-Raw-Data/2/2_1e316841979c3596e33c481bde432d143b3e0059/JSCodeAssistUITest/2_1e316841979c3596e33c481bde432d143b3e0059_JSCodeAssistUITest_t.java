 /*******************************************************************************
  * Copyright (c) 2012 NumberFour AG
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     NumberFour AG - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.javascript.ui.tests.codeassist;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.tests.ProjectSetup;
 import org.eclipse.dltk.javascript.ast.Keywords;
 import org.eclipse.dltk.javascript.ast.MultiLineComment;
 import org.eclipse.dltk.javascript.typeinfo.ITypeNames;
 import org.eclipse.dltk.javascript.ui.tests.AllTests;
 import org.eclipse.dltk.ui.templates.ScriptTemplateProposal;
 import org.eclipse.dltk.ui.tests.UICompletionUtil;
 import org.eclipse.dltk.ui.tests.UITestUtils;
 import org.eclipse.dltk.ui.text.completion.IScriptCompletionProposal;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.hamcrest.CoreMatchers;
 import org.hamcrest.Matcher;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.ClassRule;
 import org.junit.Test;
 
 import com.google.common.base.Predicate;
 import com.google.common.base.Predicates;
 import com.google.common.collect.FluentIterable;
 import com.google.common.collect.Lists;
 
 public class JSCodeAssistUITest extends Assert {
 
 	@ClassRule
 	public static final ProjectSetup PROJECT = new ProjectSetup(
 			AllTests.WORKSPACE, "completion");
 
 	@After
 	public void closeAllEditors() {
 		UITestUtils.closeAllEditors();
 	}
 
 	private static <T extends ICompletionProposal> List<T> filter(
 			List<ICompletionProposal> proposals, Class<T> proposalClass,
 			Predicate<T> predicate) {
 		return FluentIterable.from(proposals).filter(proposalClass)
 				.filter(predicate).toImmutableList();
 	}
 
 	private List<ScriptTemplateProposal> filterProposalsWithJSDoc(
 			final List<ICompletionProposal> proposals) {
 		return filter(proposals, ScriptTemplateProposal.class,
 				new Predicate<ScriptTemplateProposal>() {
 					public boolean apply(ScriptTemplateProposal input) {
 						return input.getAdditionalProposalInfo().contains(
 								MultiLineComment.JSDOC_PREFIX);
 					}
 				});
 	}
 
 	@SuppressWarnings("unchecked")
 	private static Matcher<String> stringHasValue() {
		return CoreMatchers.allOf(CoreMatchers.<String> notNullValue(String.class),
 				CoreMatchers.not(""));
 	}
 
 	@Test
 	public void functionDeclaration() throws CoreException {
 		final ISourceModule module = checkNotNull(PROJECT.getSourceModule(
 				"src", "function-declaration.js"));
 
 		final UICompletionUtil util = UICompletionUtil.openEditor(module)
 				.after("fu");
 
 		final List<ICompletionProposal> proposals = util.invokeCompletion();
 		assertEquals(4, proposals.size());
 
 		// function keyword
 		final List<IScriptCompletionProposal> keywords = filter(proposals,
 				IScriptCompletionProposal.class,
 				new Predicate<IScriptCompletionProposal>() {
 					public boolean apply(IScriptCompletionProposal input) {
 						return Keywords.FUNCTION.equals(input
 								.getDisplayString());
 					}
 				});
 		assertEquals(1, keywords.size());
 
 		// 2 function declaration templates (with jsdoc and without it)
 		final List<ScriptTemplateProposal> templates = filter(proposals,
 				ScriptTemplateProposal.class,
 				Predicates.<ScriptTemplateProposal> alwaysTrue());
 		assertEquals(2, templates.size());
 
 		// Function global
 		final List<ICompletionProposal> globals = Lists.newArrayList(proposals);
 		globals.removeAll(keywords);
 		globals.removeAll(templates);
 		assertEquals(1, globals.size());
 		assertEquals(ITypeNames.FUNCTION, globals.get(0).getDisplayString());
 	}
 
 	@Test
 	public void functionKeywordProposal() throws CoreException {
 		final ISourceModule module = checkNotNull(PROJECT.getSourceModule(
 				"src", "file1.js"));
 
 		final UICompletionUtil util = UICompletionUtil.openEditor(module)
 				.after("fu");
 
 		final List<ICompletionProposal> proposals = util.invokeCompletion();
 		assertFalse(proposals.isEmpty());
 
 		final List<IScriptCompletionProposal> keywords = filter(proposals,
 				IScriptCompletionProposal.class,
 				new Predicate<IScriptCompletionProposal>() {
 					public boolean apply(IScriptCompletionProposal input) {
 						return Keywords.FUNCTION.equals(input
 								.getDisplayString());
 					}
 				});
 		assertEquals(1, keywords.size());
 		util.apply(keywords.get(0));
 		assertEquals(PROJECT.getFileContentsAsString("src/file1keyword.txt"),
 				util.getText());
 	}
 
 	@Test
 	public void functionWithJSDoc() throws CoreException {
 		final ISourceModule module = checkNotNull(PROJECT.getSourceModule(
 				"src", "file1.js"));
 		final UICompletionUtil util = UICompletionUtil.openEditor(module)
 				.after("fu");
 
 		final List<ScriptTemplateProposal> templates = filterProposalsWithJSDoc(util
 				.invokeCompletion());
 		assertEquals(1, templates.size());
 
 		util.apply(templates.get(0));
 		assertEquals(PROJECT.getFileContentsAsString("src/file1.txt"),
 				util.getText());
 	}
 
 	@Test
 	public void secondFunctionWithJSDoc() throws CoreException {
 		final ISourceModule module = checkNotNull(PROJECT.getSourceModule(
 				"src", "file2.js"));
 		final UICompletionUtil util = UICompletionUtil.openEditor(module)
 				.afterLast("fu");
 
 		final List<ScriptTemplateProposal> templates = filterProposalsWithJSDoc(util
 				.invokeCompletion());
 		assertEquals(1, templates.size());
 
 		util.apply(templates.get(0));
 		assertEquals(PROJECT.getFileContentsAsString("src/file2.txt"),
 				util.getText());
 	}
 
 	@Test
 	public void functionWithJSDocIfJSDocAlreadyExists() throws CoreException {
 		final ISourceModule module = checkNotNull(PROJECT.getSourceModule(
 				"src", "file3.js"));
 		final UICompletionUtil util = UICompletionUtil.openEditor(module)
 				.afterLast("fu");
 
 		final List<ScriptTemplateProposal> templates = filterProposalsWithJSDoc(util
 				.invokeCompletion());
 		assertEquals(1, templates.size());
 
 		util.apply(templates.get(0));
 		assertEquals(PROJECT.getFileContentsAsString("src/file3.txt"),
 				util.getText());
 	}
 
 	@Test
 	public void additionalProposalInfoLengthProperty() throws CoreException {
 		final ISourceModule module = checkNotNull(PROJECT.getSourceModule(
 				"src", "additionalInfo.js"));
 		final UICompletionUtil util = UICompletionUtil.openEditor(module)
 				.afterLast("s.");
 
 		final List<IScriptCompletionProposal> proposals = filter(
 				util.invokeCompletion(), IScriptCompletionProposal.class,
 				new Predicate<IScriptCompletionProposal>() {
 					public boolean apply(IScriptCompletionProposal input) {
 						return input.getDisplayString().startsWith("length");
 					}
 				});
 		assertEquals(1, proposals.size());
 		assertThat(proposals.get(0).getAdditionalProposalInfo(),
 				stringHasValue());
 	}
 
 	@Test
 	public void additionalProposalInfoToStringMethod() throws CoreException {
 		final ISourceModule module = checkNotNull(PROJECT.getSourceModule(
 				"src", "additionalInfo.js"));
 		final UICompletionUtil util = UICompletionUtil.openEditor(module)
 				.afterLast("s.");
 
 		final List<IScriptCompletionProposal> proposals = filter(
 				util.invokeCompletion(), IScriptCompletionProposal.class,
 				new Predicate<IScriptCompletionProposal>() {
 					public boolean apply(IScriptCompletionProposal input) {
 						return input.getDisplayString().startsWith("toString");
 					}
 				});
 		assertEquals(1, proposals.size());
 		assertThat(proposals.get(0).getAdditionalProposalInfo(),
 				stringHasValue());
 	}
 
 }
