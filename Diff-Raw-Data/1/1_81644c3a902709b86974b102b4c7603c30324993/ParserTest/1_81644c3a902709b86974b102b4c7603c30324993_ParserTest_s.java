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
 import org.jamwiki.DataAccessException;
 import org.jamwiki.Environment;
 import org.jamwiki.JAMWikiUnitTest;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiException;
 import org.jamwiki.TestFileUtil;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicType;
 import org.jamwiki.model.TopicVersion;
 import org.jamwiki.model.VirtualWiki;
 import org.jamwiki.parser.ParserException;
 import org.jamwiki.parser.ParserInput;
 import org.jamwiki.parser.ParserOutput;
 import org.jamwiki.parser.ParserUtil;
 import org.jamwiki.utils.ImageUtil;
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
 	 * Read and load default topics from the file system.
 	 */
 	private void setupTopics() throws DataAccessException, IOException, WikiException {
 		File topicDir = TestFileUtil.getClassLoaderFile(TestFileUtil.TEST_TOPICS_DIR);
 		File[] topicFiles = topicDir.listFiles();
 		List<VirtualWiki> virtualWikis = WikiBase.getDataHandler().getVirtualWikiList();
 		for (VirtualWiki virtualWiki : virtualWikis) {
 			for (File topicFile : topicFiles) {
 				String fileName = topicFile.getName();
 				String contents = TestFileUtil.retrieveFileContent(TestFileUtil.TEST_TOPICS_DIR, fileName);
 				String topicName = TestFileUtil.decodeTopicName(fileName);
 				Topic topic = new Topic(virtualWiki.getName(), topicName);
 				topic.setTopicContent(contents);
 				int charactersChanged = (contents == null) ? 0 : contents.length();
 				TopicVersion topicVersion = new TopicVersion(null, "127.0.0.1", null, contents, charactersChanged);
 				if (topicName.toLowerCase().startsWith("image:") && !virtualWiki.getName().equals("en")) {
 					continue;
 				}
 				if (topicName.toLowerCase().startsWith("image:")) {
 					topic.setTopicType(TopicType.IMAGE);
 					topicVersion.setEditType(TopicVersion.EDIT_UPLOAD);
 				}
 				WikiBase.getDataHandler().writeTopic(topic, topicVersion, null, null);
 				if (topicName.toLowerCase().startsWith("image:")) {
 					// hard-coding for now since there is only one test image
 					ImageUtil.writeWikiFile(topic, null, "127.0.0.1", "test_image.gif", "/test_image.gif", "image/gif", 61);
 				}
 			}
 		}
 	}
 
 	/**
 	 *
 	 */
 	private String parse(String topicName, String raw) throws ParserException {
 		// set dummy values for parser input
 		ParserInput parserInput = new ParserInput();
 		parserInput.setContext("/wiki");
 		parserInput.setLocale(LocaleUtils.toLocale("en_US"));
 		parserInput.setWikiUser(null);
 		parserInput.setTopicName(topicName);
 		parserInput.setUserDisplay("0.0.0.0");
 		parserInput.setVirtualWiki("en");
 		parserInput.setAllowSectionEdit(true);
 		ParserOutput parserOutput = new ParserOutput();
 		return ParserUtil.parse(parserInput, parserOutput, raw);
 	}
 
 	/**
 	 *
 	 */
 	private void executeParserTest(String fileName, String resultDirName) throws IOException, ParserException {
 		String parserResult = this.parserResult(fileName);
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
 		ArrayList<String> failures = new ArrayList<String>();
 		failures.add("Heading5");
 		failures.add("HtmlCommentTest2");
 		failures.add("HtmlMismatchTest3");
 		failures.add("HtmlXSS1");
 		failures.add("ImageLink2"); // bad paragraph parsing - no <p> around <div> tags
 		failures.add("NestedTable1");
 		failures.add("NestedTable2");
 		failures.add("Paragraph13");
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
 	private String parserResult(String fileName) throws IOException, ParserException {
 		String raw = TestFileUtil.retrieveFileContent(TestFileUtil.TEST_TOPICS_DIR, fileName);
 		String topicName = TestFileUtil.decodeTopicName(fileName);
 		return this.parse(topicName, raw);
 	}
 
 	/**
 	 *
 	 */
 	private String sanitize(String value) {
 		return StringUtils.remove(value, '\r').trim();
 	}
 }
