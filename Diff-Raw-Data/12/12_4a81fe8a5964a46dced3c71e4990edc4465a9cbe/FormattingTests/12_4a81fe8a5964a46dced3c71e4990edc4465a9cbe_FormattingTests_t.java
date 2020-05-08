 /*******************************************************************************
  * Copyright (c) 2010 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.vjet.eclipse.core.test.formatter;
 
 import junit.framework.Assert;
import junit.framework.TestCase;
 
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IDocumentExtension3;
 import org.eclipse.jface.text.TextUtilities;
 import org.eclipse.jface.text.formatter.FormattingContext;
 import org.eclipse.jface.text.formatter.FormattingContextProperties;
 import org.eclipse.jface.text.formatter.IContentFormatterExtension;
 import org.eclipse.vjet.eclipse.core.VjetPlugin;
 import org.eclipse.vjet.eclipse.internal.formatter.comments.SingleCommentLine;
 import org.eclipse.vjet.eclipse.internal.ui.text.JavascriptSourceViewerConfiguration;
 import org.eclipse.vjet.eclipse.internal.ui.text.JavascriptTextTools;
 import org.eclipse.vjet.eclipse.ui.VjetUIPlugin;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
public class FormattingTests extends TestCase {
 	public static final String DELIMITER = TextUtilities
 			.getDefaultLineDelimiter(new Document());
 	protected static final String PREFIX = SingleCommentLine.SINGLE_COMMENT_PREFIX;
 
 	/** tools used to set up document for formatting */
 	private static JavascriptTextTools fJavaScriptTextTools;
 
 	/** context used for formatting */
 	private static FormattingContext fFormattingContext;
 
 	/** formatter used for formatting */
 	private static IContentFormatterExtension fFormatter;
 
 	@Test
 	public void testLongMultipleComments() {
 		String beforeContents = "//this is a really long comment that will have to be wrapped into multiple lines because it is so very very long"
 				+ DELIMITER
 				+ "//this is a really long comment that will have to be wrapped into multiple lines because it is so very very long"
 				+ DELIMITER;
 		String afterContents = PREFIX
 				+ "this is a really long comment that will have to be wrapped into multiple"
 				+ DELIMITER
 				+ PREFIX
 				+ "lines because it is so very very long"
 				+ DELIMITER
 				+ PREFIX
 				+ "this is a really long comment that will have to be wrapped into multiple"
 				+ DELIMITER + PREFIX + "lines because it is so very very long"
 				+ DELIMITER;
 
 		runFormatTest(beforeContents, afterContents);
 	}
 
 	@Test
 	public void testVarsOnSameLine() {
 		String beforeContents = "var x = 10;var y = 20";
 		String afterContents = "var x = 10;" + DELIMITER + "var y = 20";
 
 		runFormatTest(beforeContents, afterContents);
 	}
 
 	@Test
 	public void testFunctionSameLine() {
 		String beforeContents = "function foo(a,b,c){}";
 		String afterContents = "function foo(a, b, c) {" + DELIMITER + "}";
 
 		runFormatTest(beforeContents, afterContents);
 	}
 
 	@Test
 	public void testNestedLongMultipleComments() {
 		String beforeContents = "dojo.declare(\"myDojo.Test\", [], {"
 				+ DELIMITER
 				+ "//this is a really long comment that will have to be wrapped into multiple lines because it is so very very long"
 				+ DELIMITER
 				+ "//this is a really long comment that will have to be wrapped into multiple lines because it is so very very long"
 				+ DELIMITER
 				+ DELIMITER
 				+ "//this is a shorter comment"
 				+ DELIMITER
 				+ "constructor : function() {"
 				+ DELIMITER
 				+ "}"
 				+ DELIMITER
 				+ "//this is a really long comment that will have to be wrapped into multiple lines because it is so very very long"
 				+ DELIMITER + "});";
 		String afterContents = "dojo.declare(\"myDojo.Test\", [], {"
 				+ DELIMITER
 				+ "\t"
 				+ PREFIX
 				+ "this is a really long comment that will have to be wrapped into multiple"
 				+ DELIMITER
 				+ "\t"
 				+ PREFIX
 				+ "lines because it is so very very long"
 				+ DELIMITER
 				+ "\t"
 				+ PREFIX
 				+ "this is a really long comment that will have to be wrapped into multiple"
 				+ DELIMITER
 				+ "\t"
 				+ PREFIX
 				+ "lines because it is so very very long"
 				+ DELIMITER
 				+ DELIMITER
 				+ "\t"
 				+ PREFIX
 				+ "this is a shorter comment"
 				+ DELIMITER
 				+ "\t"
 				+ "constructor : function() {"
 				+ DELIMITER
 				+ "\t"
 				+ "}"
 				+ DELIMITER
 				+ PREFIX
 				+ "this is a really long comment that will have to be wrapped into multiple"
 				+ DELIMITER + PREFIX + "lines because it is so very very long"
 				+ DELIMITER + "});";
 
 		runFormatTest(beforeContents, afterContents);
 	}
 
 	/**
 	 * <p>
 	 * Formats the given <code>beforeContents</code> and compares it to the
 	 * given <code>afterContents</code>
 	 * </p>
 	 * 
 	 * @param beforeContents
 	 *            format this contents and compare it to the given
 	 *            <code>afterContents</code>
 	 * @param afterContents
 	 *            compare this contents to the <code>beforeContents</code> after
 	 *            it has been formated
 	 */
 	private static void runFormatTest(String beforeContents,
 			String afterContents) {
 		IDocument toFormat = new Document(beforeContents);
 		fJavaScriptTextTools.setupDocumentPartitioner(toFormat);
 		fFormatter.format(toFormat, fFormattingContext);
 		Assert.assertEquals(
 				"The formatted document does not have the expected contents",
 				afterContents, toFormat.get());
 	}
 
 	private static final String WTP_AUTOTEST_NONINTERACTIVE = "wtp.autotest.noninteractive";
 	private static String previousWTPAutoTestNonInteractivePropValue = null;
 
 	/**
 	 * Default constructor
 	 * 
 	 * @param test
 	 *            do setup for the given test
 	 */
 
 	/**
 	 * <p>
 	 * This is run once before all of the tests
 	 * </p>
 	 * 
 	 * @see junit.extensions.TestSetup#setUp()
 	 */
 	@BeforeClass
	public  void setUp() throws Exception {
 		// set up formatting tools
 		fJavaScriptTextTools = VjetUIPlugin.getDefault().getTextTools();
 		JavascriptSourceViewerConfiguration config = new JavascriptSourceViewerConfiguration(
 				fJavaScriptTextTools.getColorManager(), VjetUIPlugin
 						.getDefault().getPreferenceStore(), null,
 				IDocumentExtension3.DEFAULT_PARTITIONING);
 
 		fFormatter = (IContentFormatterExtension) config
 				.getContentFormatter(null);
 
 		Assert.assertNotNull(fFormatter);
 		fFormattingContext = new FormattingContext();
 		fFormattingContext.setProperty(
 				FormattingContextProperties.CONTEXT_PREFERENCES,
 				VjetPlugin.getOptions());
 		fFormattingContext.setProperty(
 				FormattingContextProperties.CONTEXT_DOCUMENT,
 				Boolean.valueOf(true));
 
 		// set non-interactive
 		String noninteractive = System.getProperty(WTP_AUTOTEST_NONINTERACTIVE);
 		if (noninteractive != null) {
 			previousWTPAutoTestNonInteractivePropValue = noninteractive;
 		} else {
 			previousWTPAutoTestNonInteractivePropValue = "false";
 		}
 		System.setProperty(WTP_AUTOTEST_NONINTERACTIVE, "true");
 	}
 
 	/**
 	 * <p>
 	 * This is run once after all of the tests have been run
 	 * </p>
 	 * 
 	 * @see junit.extensions.TestSetup#tearDown()
 	 */

	public void tearDown() throws Exception {
 		// reset non-interactive
 		if (previousWTPAutoTestNonInteractivePropValue != null) {
 			System.setProperty(WTP_AUTOTEST_NONINTERACTIVE,
 					previousWTPAutoTestNonInteractivePropValue);
 		}
 	}
 
 }
