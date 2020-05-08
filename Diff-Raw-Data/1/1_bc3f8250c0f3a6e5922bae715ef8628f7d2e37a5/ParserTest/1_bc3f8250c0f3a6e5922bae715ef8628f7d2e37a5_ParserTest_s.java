 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.parser;
 
 import java.io.File;
 import java.util.ArrayList;
 import junit.framework.TestCase;
 import junit.framework.TestResult;
 import org.apache.commons.lang.LocaleUtils;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.TestFileUtil;
 import org.jamwiki.utils.WikiLogger;
 
 /**
  * This class will first get a list of all parser result files in the /data/results
  * directory and then retrieve the corresponding /data/topics file, parse it, and
  * compare the parser output to the results file.
  */
 public class ParserTest extends TestCase {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(ParserTest.class.getName());
 
 	/**
 	 *
 	 */
 	public ParserTest(String name) {
 		super(name);
 	}
 
 	/**
 	 *
 	 */
 	private String parse(String topicName, String raw) throws Exception {
 		// set dummy values for parser input
 		ParserInput parserInput = new ParserInput();
 		parserInput.setContext("/wiki");
 		parserInput.setLocale(LocaleUtils.toLocale("en_US"));
 		parserInput.setWikiUser(null);
 		parserInput.setTopicName(topicName);
 		parserInput.setUserIpAddress("0.0.0.0");
 		parserInput.setVirtualWiki("en");
 		parserInput.setAllowSectionEdit(true);
 		ParserOutput parserOutput = new ParserOutput();
 		return ParserUtil.parse(parserInput, parserOutput, raw);
 	}
 
 	/**
 	 *
 	 */
 	private void executeParserTest(String topicName) throws Exception {
 		String parserResult = this.parserResult(topicName);
 		String expectedResult = this.expectedResult(topicName);
 		assertEquals(parserResult, expectedResult);
 	}
 
 	/**
 	 *
 	 */
 	private String expectedResult(String topicName) throws Exception {
 		String raw = TestFileUtil.retrieveFileContent(TestFileUtil.TEST_TOPICS_DIR, topicName);
 		return this.parse(topicName, raw);
 	}
 
 	/**
 	 * Hard-code a list of files that are known to fail parsing.
 	 */
 	// TODO - handle failure cases better.
 	private boolean knownFailure(String fileName) {
 		ArrayList failures = new ArrayList();
 		failures.add("HtmlXSS1");
 		failures.add("NestedTable1");
 		failures.add("NestedTable2");
 		failures.add("HtmlMismatchTest3");
		failures.add("HtmlMismatchTest4");
 		failures.add("UnbalancedTag1");
 		failures.add("Paragraph6");
 		failures.add("PreformattedInvalid1");
 		failures.add("HtmlList1");
 		failures.add("Preformatted2");
 		return (failures.indexOf(fileName) != -1);
 	}
 
 	/**
 	 *
 	 */
 	private void parseAllResults(TestResult result) {
 		try {
 			File resultDir = TestFileUtil.getClassLoaderFile(TestFileUtil.TEST_RESULTS_DIR);
 			File[] resultFiles = resultDir.listFiles();
 			String fileName = null;
 			for (int i = 0; i < resultFiles.length; i++) {
 				fileName = resultFiles[i].getName();
 				try {
 					executeParserTest(fileName);
 				} catch (Throwable t) {
 					if (!knownFailure(fileName)) {
 						result.addError(new ParserTest(fileName), t);
 					}
 				}
 			}
 		} catch (Exception e) {
 			result.addError(this, e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private String parserResult(String topicName) throws Exception {
 		String result = TestFileUtil.retrieveFileContent(TestFileUtil.TEST_RESULTS_DIR, topicName);
 		return this.sanitize(result);
 	}
 
 	/**
 	 *
 	 */
 	public void run(TestResult result) {
 		this.parseAllResults(result);
 	}
 
 	/**
 	 *
 	 */
 	private String sanitize(String value) {
 		return StringUtils.remove(value, '\r').trim();
 	}
 
 	/**
 	 * Surefire requires at least one method whose name starts with 'test', so create
 	 * this dummy method and then overwrite the JUnit run() method to run the actual
 	 * unit tests.
 	 */
 	public void testSuiteSurefireHack() {
 		// empty method, causes run() to be called
 	}
 }
