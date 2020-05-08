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
 package org.jamwiki.parser.jflex;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.commons.lang.LocaleUtils;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.Environment;
 import org.jamwiki.JAMWikiUnitTest;
 import org.jamwiki.TestFileUtil;
 import org.jamwiki.parser.ParserException;
 import org.jamwiki.parser.ParserInput;
 import org.jamwiki.parser.ParserOutput;
 import org.jamwiki.parser.ParserUtil;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  * This class will first get a list of all parser result files in the /data/results
  * directory and then retrieve the corresponding /data/topics file, parse it, and
  * compare the parser output to the results file.
  */
 public class ParserTest extends JAMWikiUnitTest {
 
 	private static boolean INITIALIZED = false;
 
 	/**
 	 *
 	 */
 	@Before
 	public void setup() throws Exception {
 		super.setup();
 		if (!INITIALIZED) {
 			this.setupTopics();
 			INITIALIZED = true;
 		}
 	}
 
 	/**
 	 *
 	 */
 	@Test
 	public void testInterwiki1() throws Throwable {
 		// this topic has two interwiki links, but they both go to the same wikipedia page
 		ParserOutput parserOutput = new ParserOutput();
 		String parserResult = this.parserResult(parserOutput, "Interwiki1");
 		assertEquals("Interwiki1", 1, parserOutput.getInterwikiLinks().size());
 		assertEquals("Interwiki1", "<a class=\"interwiki\" title=\"Wikipedia\" href=\"http://en.wikipedia.org/wiki/Main_Page\">Wikipedia</a>", parserOutput.getInterwikiLinks().get(0));
 	}
 
 	/**
 	 *
 	 */
 	@Test
 	public void testVirtualWiki1() throws Throwable {
 		// this topic has one virtual wiki link
 		ParserOutput parserOutput = new ParserOutput();
 		String parserResult = this.parserResult(parserOutput, "WikiLink1");
 		assertEquals("Interwiki1", 1, parserOutput.getVirtualWikiLinks().size());
 		assertEquals("Interwiki1", "<a href=\"/wiki/test/WikiLink1\" title=\"WikiLink1\">test:WikiLink1</a>", parserOutput.getVirtualWikiLinks().get(0));
 	}
 
 	/**
 	 *
 	 */
 	@Test
 	public void testMagicWordDisplayTitleValid() throws Throwable {
 		String topicName = "Magic Words Display Title";
 		String displayTitle = "Magic_Words Display_Title";
 		String topicContent = "{{DISPLAYTITLE:" + displayTitle + "}}";
 		ParserInput parserInput = this.parserInput(topicName);
 		ParserOutput parserOutput = new ParserOutput();
 		ParserUtil.parse(parserInput, parserOutput, topicContent);
 		assertEquals("DISPLAYTITLE", displayTitle, parserOutput.getPageTitle());
 	}
 
 	/**
 	 *
 	 */
 	@Test
 	public void testMagicWordDisplayTitleInvalid() throws Throwable {
 		String topicName = "Magic Words Display Title";
 		String displayTitle = "Invalid Title";
 		String topicContent = "{{DISPLAYTITLE:" + displayTitle + "}}";
 		ParserInput parserInput = this.parserInput(topicName);
 		ParserOutput parserOutput = new ParserOutput();
 		ParserUtil.parse(parserInput, parserOutput, topicContent);
 		assertNull("DISPLAYTITLE", parserOutput.getPageTitle());
 	}
 
 	/**
 	 *
 	 */
 	@Test
 	public void testParserNoJavascript() throws IOException {
 		// test with JS disabled
 		Environment.setBooleanValue(Environment.PROP_PARSER_ALLOW_JAVASCRIPT, false);
 		this.parseAllResults(TestFileUtil.TEST_RESULTS_DIR);
 	}
 
 	/**
 	 *
 	 */
 	@Test
 	public void testParserWithJavascript() throws IOException {
 		// test with JS enabled
 		Environment.setBooleanValue(Environment.PROP_PARSER_ALLOW_JAVASCRIPT, true);
 		this.parseAllResults(TestFileUtil.TEST_JS_RESULTS_DIR);
 	}
 
 	/**
 	 * Generate a generic ParserInput object that can be used for testing.
 	 */
 	private ParserInput parserInput(String topicName) {
 		// set dummy values for parser input
 		ParserInput parserInput = new ParserInput();
 		parserInput.setContext("/wiki");
 		parserInput.setLocale(LocaleUtils.toLocale("en_US"));
 		parserInput.setWikiUser(null);
 		parserInput.setTopicName(topicName);
 		parserInput.setUserDisplay("0.0.0.0");
 		parserInput.setVirtualWiki("en");
 		parserInput.setAllowSectionEdit(true);
 		return parserInput;
 	}
 
 	/**
 	 *
 	 */
 	private void executeParserTest(String fileName, String resultDirName) throws IOException, ParserException {
 		ParserOutput parserOutput = new ParserOutput();
 		String parserResult = this.parserResult(parserOutput, fileName);
 		String expectedResult = this.expectedResult(fileName, resultDirName);
 		assertEquals("Testing file " + fileName, expectedResult, parserResult);
 	}
 
 	/**
 	 *
 	 */
 	private String expectedResult(String fileName, String resultDirName) throws IOException, ParserException {
 		String result = TestFileUtil.retrieveFileContent(resultDirName, fileName);
 		return this.sanitize(result);
 	}
 
 	/**
 	 * Hard-code a list of files that are known to fail parsing.
 	 */
 	// TODO - handle failure cases better.
 	private boolean knownFailure(String fileName) {
 		List<String> failures = new ArrayList<String>();
 		failures.add("Heading5");
 		failures.add("HtmlCommentTest2");
 		failures.add("HtmlMismatchTest3");
 		failures.add("HtmlXSS1");
 		failures.add("ImageLink2"); // bad paragraph parsing - no <p> around <div> tags
 		failures.add("Inclusion~test"); // template params not parsed in parser functions
		failures.add("NestedTable1"); // paragraphs in <td> tags
		failures.add("Paragraph13"); // paragraphs in <td> tags
 		failures.add("Paragraph15"); // failure parsing of empty paragraphs with <nowiki>
 		failures.add("PreformattedInvalid1");
 		failures.add("Template_-_Paramtest");
 		failures.add("UnbalancedTag1");
 		failures.add("UnbalancedTag3");
 		return (failures.indexOf(fileName) != -1);
 	}
 
 	/**
 	 *
 	 */
 	private void parseAllResults(String resultDirName) throws IOException {
 		File resultDir = TestFileUtil.getClassLoaderFile(resultDirName);
 		File[] resultFiles = resultDir.listFiles();
 		String fileName = null;
 		for (int i = 0; i < resultFiles.length; i++) {
 			fileName = resultFiles[i].getName();
 			if (!knownFailure(fileName)) {
 				executeParserTest(fileName, resultDirName);
 			}
 		}
 	}
 
 	/**
 	 *
 	 */
 	private String parserResult(ParserOutput parserOutput, String fileName) throws IOException, ParserException {
 		String raw = TestFileUtil.retrieveFileContent(TestFileUtil.TEST_TOPICS_DIR, fileName);
 		String topicName = TestFileUtil.decodeTopicName(fileName);
 		ParserInput parserInput = this.parserInput(topicName);
 		return ParserUtil.parse(parserInput, parserOutput, raw);
 	}
 
 	/**
 	 *
 	 */
 	private String sanitize(String value) {
 		return StringUtils.remove(value, '\r').trim();
 	}
 }
