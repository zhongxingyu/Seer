 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.ui.tests.text.indenting;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.dltk.core.tests.model.SuiteOfTestCases;
 import org.eclipse.dltk.ruby.internal.ui.RubyPreferenceConstants;
 import org.eclipse.dltk.ruby.internal.ui.text.RubyAutoEditStrategy;
 import org.eclipse.dltk.ruby.internal.ui.text.RubyPartitions;
 import org.eclipse.dltk.ruby.ui.tests.internal.TestUtils;
 import org.eclipse.dltk.ui.CodeFormatterConstants;
 import org.eclipse.jface.preference.PreferenceStore;
 import org.eclipse.jface.text.DocCmd;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.TextUtilities;
 
 public class IndentingTest extends SuiteOfTestCases {
 
     public IndentingTest(String name) {
 		super(name);
 	}
 
 	private static final String PATH = "resources/indenting/";
 	private RubyAutoEditStrategy tabStrategy, spaceStrategy;
 
 	protected void setUp() throws Exception {
 		tabStrategy = createStrategy(true);
 		spaceStrategy = createStrategy(false);
         super.setUp();
     }
 
 	private RubyAutoEditStrategy createStrategy(boolean useTabs) {
 		PreferenceStore store = new PreferenceStore();
     	store.setValue(CodeFormatterConstants.FORMATTER_TAB_CHAR, 
     			(useTabs ? CodeFormatterConstants.TAB : CodeFormatterConstants.SPACE));
     	RubyPreferenceConstants.initializeDefaultValues(store);
 		String partitioning = RubyPartitions.RUBY_PARTITIONING;
     	RubyAutoEditStrategy result = new RubyAutoEditStrategy(store, partitioning);
 		return result;
 	}
 
 	public void doTest(String data, RubyAutoEditStrategy strategy)
 			throws Exception {
 		data = data.replaceAll("π", "≤\n≥");
 		
 		int startPos = data.indexOf("≤");
 		Assert.isLegal(startPos >= 0);
 		data = data.substring(0, startPos) + data.substring(startPos + 1);
 		
		int replacePos = data.indexOf("±");
 		int insertionStartPos = startPos;
 		if (replacePos >= 0) {
 			Assert.isLegal(replacePos >= startPos);
 			data = data.substring(0, replacePos) + data.substring(replacePos + 1);
 			insertionStartPos = replacePos;
 		}
 		
 		int endPos = data.indexOf("≥");
 		Assert.isLegal(endPos >= 0);
 		Assert.isLegal(replacePos < 0 || endPos >= replacePos);
 		String insertion = data.substring(insertionStartPos, endPos);
 		data = data.substring(0, insertionStartPos) + data.substring(endPos + 1);
 		
 		int expectedPos = data.indexOf("§§");
 		Assert.isLegal(expectedPos >= 0);
 		String expected = data.substring(expectedPos + 2);
 		data = data.substring(0, expectedPos);
 		
 		Document doc = new Document(data);
 		TestUtils.installStuff(doc);
 		
 		// remove the leading line break from expected
 		String[] legalLineDelimiters = doc.getLegalLineDelimiters();
 		int index = TextUtilities.startsWith(legalLineDelimiters, expected);
 		Assert.isLegal(index >= 0);
 		expected = expected.substring(legalLineDelimiters[index].length());
 		
 		int replaceLength = (replacePos < 0 ? 0 : replacePos - startPos);
 		DocCmd cmd = new DocCmd(startPos, replaceLength, insertion);
 		
 		strategy.customizeDocumentCommand(doc, cmd);
 		if (cmd.doit) {
 //			for (Iterator iter = cmd.getCommandIterator(); iter.hasNext(); ) {
 //				Object command = iter.next();
 //				Method method = command.getClass().getMethod("execute", new Class[] {IDocument.class});
 //				method.invoke(command, new Object[] {doc});
 //			}
 			doc.replace(cmd.offset, cmd.length, cmd.text);
 		}
 		
 		assertEquals(expected, doc.get());
 	}
 	
 	private void magic() throws Exception {
 		String name = getName();
 		String fileName = name.substring(4, 5).toLowerCase() + name.substring(5) + ".rb";
 		String data = TestUtils.getData(PATH + fileName);
 		String moreData = data.replaceAll("\t", "    ");
 		if (!moreData.equals(data))
 			doTest(moreData, spaceStrategy);
 		doTest(data, tabStrategy);
 	}
 	
 	public void testNewLineInDef() throws Exception {
 		magic();
 	}
 	
 	public void testEnterBeforeClass() throws Exception {
 		magic();
 	}
 	
 	public void testEnterOpensClass() throws Exception {
 		magic();
 	}
 	
 	public void testIfStatement() throws Exception {
 		magic();
 	}
 	
 	public void testIfModifier() throws Exception {
 		magic();
 	}
 	
 	public void testMovingEndToNewLine() throws Exception {
 		magic();
 	}
 	
 	public void testMovingEndWithWhitespaceToNewLine() throws Exception {
 		magic();
 	}
 	
 	public void testDeindentingEnd() throws Exception {
 		magic();
 	}
 	
 	public void testClassNotKeyword() throws Exception {
 		magic();
 	}
 	
 	public void testNewLineAfterEmptyIndentedLine() throws Exception {
 		magic();
 	}
 	
 	public void testNewLineInRegularFunction() throws Exception {
 		magic();
 	}
 	
 	public void testIndentAfterNewLineBeforeParen() throws Exception {
 		magic();
 	}
 	
 	public void testIndentOnUnclosedParen() throws Exception {
 		magic();
 	}
 	
 	public void testIndentOnFirstExplicitContinuation() throws Exception {
 		magic();
 	}
 	
 	public void testIndentOnFirstImplicitContinuation() throws Exception {
 		magic();
 	}
 	
 	public void testNoIndentOnSubsequentExplicitContinuation() throws Exception {
 		magic();
 	}
 	
 	public void testNoIndentOnSubsequentImplicitContinuationAfterExplicitOne() throws Exception {
 		magic();
 	}
 	
 	public void testNoIndentOnSubsequentImplicitContinuationAfterImplicitOne() throws Exception {
 		magic();
 	}
 
 }
