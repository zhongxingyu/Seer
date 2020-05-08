 package org.openxdata.modelutils;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.text.MessageFormat;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
 
 import junit.framework.TestCase;
 
 import org.fcitmuk.epihandy.DynamicOptionDef;
 import org.fcitmuk.epihandy.FormDef;
 import org.fcitmuk.epihandy.PageDef;
 import org.fcitmuk.epihandy.QuestionDef;
 import org.fcitmuk.epihandy.xform.EpihandyXform;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.InputSource;
 
 @SuppressWarnings("unchecked")
 public class ModelToXMLTest extends TestCase {
 
 	private static Logger log = LoggerFactory.getLogger(ModelToXMLTest.class);
 
 	FormDef sampleDef;
 	String sampleXml;
 	String convertedXml;
 	FormDef reparsedDef;
 	InputStream convertedStream;
 	InputSource convertedSource;
 	XPath xpath;
 	Map<Short, QuestionDef> dynOptDepMap;
 
 	{
 		InputStream sampleStream = ModelToXMLTest.class
 				.getResourceAsStream("/sample.xml");
 
 		BufferedReader sampleReader = new BufferedReader(new InputStreamReader(
 				sampleStream));
 		StringBuilder buf = new StringBuilder();
 		String line = null;
 		try {
 			while ((line = sampleReader.readLine()) != null)
 				buf.append(line);
 			buf.append('\n');
 			sampleXml = buf.toString();
 		} catch (IOException e) {
 			String msg = "Failed to read in sample.xml";
 			log.error(msg, e);
 			throw new RuntimeException(msg);
 		}
 
 		sampleDef = EpihandyXform
 				.fromXform2FormDef(new StringReader(sampleXml));
 
 		dynOptDepMap = new HashMap<Short, QuestionDef>();
 		for (Map.Entry<Short, DynamicOptionDef> dynOptEntry : (Set<Map.Entry<Short, DynamicOptionDef>>) sampleDef
 				.getDynamicOptions().entrySet()) {
 			dynOptDepMap.put(dynOptEntry.getValue().getQuestionId(),
 					sampleDef.getQuestion(dynOptEntry.getKey()));
 		}
 
 		convertedXml = ModelToXML.convert(sampleDef);
 
 		reparsedDef = EpihandyXform.fromXform2FormDef(new StringReader(
 				convertedXml));
 
 		convertedStream = new ByteArrayInputStream(convertedXml.getBytes());
 		convertedSource = new InputSource(convertedStream);
 
 		XPathFactory xpathFactory = javax.xml.xpath.XPathFactory.newInstance();
 		xpath = xpathFactory.newXPath();
 		xpath.setNamespaceContext(new NamespaceContext());
 	}
 
 	public void testNullConversion() {
 		try {
 			ModelToXML.convert(null);
 			fail("Should throw exception with null document.");
 		} catch (Exception e) {
 		}
 	}
 
 	public void testFormConversion() {
 		assertEquals(sampleDef.getVariableName(), reparsedDef.getVariableName());
 		assertEquals(sampleDef.getDescriptionTemplate(),
 				reparsedDef.getDescriptionTemplate());
 		assertEquals(sampleDef.getId(), reparsedDef.getId());
 		assertEquals(sampleDef.getName(), reparsedDef.getName());
 	}
 
 	public void testPageConversion() {
 		assertEquals("page counts didn't match", sampleDef.getPageCount(),
 				reparsedDef.getPageCount());
 		for (int i = 0; i < reparsedDef.getPageCount(); i++) {
 			PageDef samplePage = sampleDef.getPageAt(i);
 			PageDef reparsedPage = reparsedDef.getPageAt(i);
 			assertNotNull("original page should not be null", samplePage);
 			assertNotNull("reparsed page should not be null", reparsedPage);
 			assertEquals("page numbers should mach", samplePage.getPageNo(),
 					reparsedPage.getPageNo());
 			assertEquals("page names should match", samplePage.getName(),
 					reparsedPage.getName());
 		}
 	}
 
 	public void testBindingConversion() throws Exception {
 		String[] exprs = {
 				"count(//xf:bind[@id='patientid' and @nodeset='/patientreg/patientid' and @type='xsd:string'])",
 				"count(//xf:bind[@id='picture' and @nodeset='/patientreg/picture' and @type='xsd:base64Binary' and @format='image'])",
 				"count(//xf:bind[@id='coughsound' and @nodeset='/patientreg/coughsound' and @format='audio' and @type='xsd:base64Binary'])",
 				"count(//xf:bind[@id='recordvideo' and @nodeset='/patientreg/recordvideo' and @format='video' and @type='xsd:base64Binary'])",
 				"count(//xf:bind[@id='location' and @nodeset='/patientreg/location' and @format='gps' and @type='xsd:string'])",
 				"count(//xf:bind[@id='phone' and @nodeset='/patientreg/phone' and @format='phonenumber' and @type='xsd:string'])",
 				"count(//xf:bind[@id='weight' and @nodeset='/patientreg/weight' and @type='xsd:decimal'])",
 				"count(//xf:bind[@id='height' and @nodeset='/patientreg/height' and @type='xsd:int'])",
 				"count(//xf:bind[@id='birthdate' and @nodeset='/patientreg/birthdate' and @type='xsd:date'])",
 				"count(//xf:bind[@id='starttime' and @nodeset='/patientreg/starttime' and @type='xsd:time'])",
 				"count(//xf:bind[@id='visitdate' and @nodeset='/patientreg/visitdate' and @type='xsd:dateTime'])" };
 		for (String expr : exprs) {
 			convertedStream.reset(); // Restore stream state
 			XPathExpression compiledExpr = xpath.compile(expr);
 			Double matchCount = (Double) compiledExpr.evaluate(convertedSource,
 					XPathConstants.NUMBER);
 			assertEquals(expr + " unique binding not present ", 1,
 					matchCount.intValue());
 		}
 	}
 
 	public void testBoundInputConversion() throws Exception {
 
 		String matchPattern = "count(//xf:input[@bind=''{0}''])";
 		String[] matchQuestions = { "patientid", "location", "phone", "weight",
 				"height", "birthdate", "starttime", "visitdate" };
 
 		for (String matchQuestion : matchQuestions) {
 			convertedStream.reset(); // Restore stream state
 			String expr = MessageFormat.format(matchPattern, matchQuestion);
 			XPathExpression compiledExpr = xpath.compile(expr);
 			Double matchCount = (Double) compiledExpr.evaluate(convertedSource,
 					XPathConstants.NUMBER);
 			assertEquals(expr + " input not present ", 1, matchCount.intValue());
 		}
 
 		String[] noMatchQuestions = { "kid", "kidsex", "kidage", "title",
 				"sex", "arvs", "picture", "coughsound", "recordvideo",
 				"continent", "country", "district", "village" };
 
 		for (String noMatchQuestion : noMatchQuestions) {
 			convertedStream.reset(); // Restore stream state
 			String expr = MessageFormat.format(matchPattern, noMatchQuestion);
 			XPathExpression compiledExpr = xpath.compile(expr);
 			Double matchCount = (Double) compiledExpr.evaluate(convertedSource,
 					XPathConstants.NUMBER);
 			assertEquals(expr + " input should not be present ", 0,
 					matchCount.intValue());
 		}
 	}
 
 	public void testBoundUploadConversion() throws Exception {
 
 		String matchPattern = "count(//xf:upload[@bind=''{0}'' and @mediatype=''{1}''])";
 		String[][] matchParams = { { "picture", "image/*" },
 				{ "coughsound", "audio/*" }, { "recordvideo", "video/*" } };
 
 		for (String[] matchQuestion : matchParams) {
 			convertedStream.reset(); // Restore stream state
 			String expr = MessageFormat.format(matchPattern,
 					(Object[]) matchQuestion);
 			XPathExpression compiledExpr = xpath.compile(expr);
 			Double matchCount = (Double) compiledExpr.evaluate(convertedSource,
 					XPathConstants.NUMBER);
 			assertEquals(expr + " upload not present ", 1,
 					matchCount.intValue());
 		}
 	}
 
 	public void testExclusiveListConversion() throws Exception {
 
 		String matchPattern = "count(//xf:select1[@bind=''{0}'' and count(xf:item) = ''{1}''])";
 		String[] matchParams = { "title", "sex", "continent" };
 
 		for (String matchQuestion : matchParams) {
 			QuestionDef qDef = sampleDef.getQuestion("/patientreg/"
 					+ matchQuestion);
 			convertedStream.reset(); // Restore stream state
 			String expr = MessageFormat.format(matchPattern, matchQuestion,
 					Integer.toString(qDef.getOptions().size()));
 			XPathExpression compiledExpr = xpath.compile(expr);
 			Double matchCount = (Double) compiledExpr.evaluate(convertedSource,
 					XPathConstants.NUMBER);
 			assertEquals(expr + " select1 not present ", 1,
 					matchCount.intValue());
 		}
 	}
 
 	public void testMultiSelectListConversion() throws Exception {
 
 		String matchPattern = "count(//xf:select[@bind=''{0}'' and count(xf:item) = ''{1}''])";
 		String[] matchParams = { "arvs" };
 
 		for (String matchQuestion : matchParams) {
 			QuestionDef qDef = sampleDef.getQuestion("/patientreg/"
 					+ matchQuestion);
 			convertedStream.reset(); // Restore stream state
 			String expr = MessageFormat.format(matchPattern, matchQuestion,
 					Integer.toString(qDef.getOptions().size()));
 			XPathExpression compiledExpr = xpath.compile(expr);
 			Double matchCount = (Double) compiledExpr.evaluate(convertedSource,
 					XPathConstants.NUMBER);
 			assertEquals(expr + " select not present ", 1,
 					matchCount.intValue());
 		}
 	}
 
 	public void testDynamicListConversion() throws Exception {
 
 		String matchPattern = "count(//xf:select1[@bind=''{1}'']/xf:itemset[@nodeset=\"instance(''{1}'')/item[@parent=instance(''{0}'')/{2}]\"])";
 		String[][] matchParams = { { "country", "continent" },
 				{ "district", "country" }, { "village", "district" } };
 
 		for (String[] matchQuestion : matchParams) {
 			convertedStream.reset(); // Restore stream state
 			String expr = MessageFormat.format(matchPattern, "patientreg",
 					matchQuestion[0], matchQuestion[1]);
 			XPathExpression compiledExpr = xpath.compile(expr);
 			Double matchCount = (Double) compiledExpr.evaluate(convertedSource,
 					XPathConstants.NUMBER);
 			assertEquals(expr + " select1 not present ", 1,
 					matchCount.intValue());
 		}
 	}
 
 	public void testDynamicListItemConversion() throws Exception {
 
 		String matchPattern = "count(//xf:instance[@id=''{0}'']/dynamiclist[count(item) = ''{1}''])";
 		String[] matchParams = { "country", "district", "village" };
 
 		for (String matchQuestion : matchParams) {
 			QuestionDef qDef = sampleDef.getQuestion("/patientreg/"
 					+ matchQuestion);
 			QuestionDef parentQuestionDef = dynOptDepMap.get(qDef.getId());
 			int optCount = 0;
 			DynamicOptionDef dynOptDef = sampleDef
 					.getDynamicOptions(parentQuestionDef.getId());
 			for (Short key : (Set<Short>) dynOptDef.getParentToChildOptions()
 					.keySet())
 				optCount += dynOptDef.getOptionList(key).size();
 			convertedStream.reset(); // Restore stream state
 			String expr = MessageFormat.format(matchPattern, matchQuestion,
 					optCount);
 			XPathExpression compiledExpr = xpath.compile(expr);
 			Double matchCount = (Double) compiledExpr.evaluate(convertedSource,
 					XPathConstants.NUMBER);
 			assertEquals(expr + " instance not present ", 1,
 					matchCount.intValue());
 		}
 	}
 
 	public void testDefaultsConversion() throws Exception {
 
 		String matchPattern = "count(//xf:instance[@id=''{0}'']/{0}[{1} = \"{2}\"])";
 		String[][] matchParams = { { "patientreg", "nokids", "0" },
 				{ "patientreg", "starttime", "'today()'" } };
 
 		for (String[] matchParam : matchParams) {
 			convertedStream.reset(); // Restore stream state
 			String expr = MessageFormat.format(matchPattern,
 					(Object[]) matchParam);
 			XPathExpression compiledExpr = xpath.compile(expr);
 			Double matchCount = (Double) compiledExpr.evaluate(convertedSource,
 					XPathConstants.NUMBER);
 			assertEquals(expr + " default value not present ", 1,
 					matchCount.intValue());
 		}
 	}
 
 	public void testValidationConverstion() throws Exception {
 		String matchPattern = "count(//xf:bind[@id=''{0}'' and @constraint=''{1}'' and @message=''{2}''])";
 		String[][] matchParams = {
 				{ "birthdate", ". <= today()", "Cannot be greater than today" },
 				{ "weight", ". >= 1.1 and . <= 99.9",
 						"Should be between 0 and 200 inclusive" },
 				{ "height", ". >= 1 and . >= 20",
 						"Should be between 1 and 20 inclusive" },
 				{ "nokids", ". >= 0 and . < 100", "Should be between 0 and 100" } };
 
 		for (String[] matchParam : matchParams) {
 			convertedStream.reset(); // Restore stream state
 			String expr = MessageFormat.format(matchPattern,
 					(Object[]) matchParam);
 			XPathExpression compiledExpr = xpath.compile(expr);
 			Double matchCount = (Double) compiledExpr.evaluate(convertedSource,
 					XPathConstants.NUMBER);
 			assertEquals(expr + " validation rule not present ", 1,
 					matchCount.intValue());
 		}
 	}
 
 	public void testSkipLogicConversion() throws Exception {
 		String matchPattern = "count(//xf:bind[@id=''{0}'' and @relevant=\"/patientreg/sex = ''female''\" and @action=''enable''])";
 		String[][] matchParams = { { "pregnant" } };
 
 		for (String[] matchParam : matchParams) {
 			convertedStream.reset(); // Restore stream state
 			String expr = MessageFormat.format(matchPattern,
 					(Object[]) matchParam);
 			XPathExpression compiledExpr = xpath.compile(expr);
 			Double matchCount = (Double) compiledExpr.evaluate(convertedSource,
 					XPathConstants.NUMBER);
 			assertEquals(expr + " skip rule not present ", 1,
 					matchCount.intValue());
 		}
 	}
 
 	public void testRepeatInstanceConversion() throws Exception {
 		String matchPattern = "count(//xf:instance[@id=''{0}'']/{0}/kids/kid/{1})";
 		String[] matchParams = { "kidname", "kidsex", "kidage" };
 
 		String instanceId = "patientreg";
 
 		for (String matchParam : matchParams) {
 			convertedStream.reset(); // Restore stream state
 			String expr = MessageFormat.format(matchPattern, new Object[] {
 					instanceId, matchParam });
 			XPathExpression compiledExpr = xpath.compile(expr);
 			Double matchCount = (Double) compiledExpr.evaluate(convertedSource,
 					XPathConstants.NUMBER);
 			assertEquals(expr + " repeat instance element not present ", 1,
 					matchCount.intValue());
 		}
 	}
 }
