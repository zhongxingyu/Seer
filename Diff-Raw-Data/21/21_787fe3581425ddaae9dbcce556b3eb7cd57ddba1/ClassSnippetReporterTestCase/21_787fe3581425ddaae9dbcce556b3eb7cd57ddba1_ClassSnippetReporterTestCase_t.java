 /**
  *
  *   Copyright Retroduction.org - All rights reserved
  *
  *   This file is part of Carma. Carma is licensed under the GPL 3
  *   (http://www.opensource.org/licenses/gpl-3.0.html).
  *
  */
 package org.retroduction.carma.reportgenerator.reporter;
 
 import java.io.IOException;
 import java.io.StringBufferInputStream;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.PropertyResourceBundle;
 
 import junit.framework.TestCase;
 
 import org.retroduction.carma.reportgenerator.RendererException;
 
 import com.retroduction.carma.report.om.SourceFile;
 import com.retroduction.carma.xmlreport.om.ClassUnderTest;
 import com.retroduction.carma.xmlreport.om.Mutant;
 
 public class ClassSnippetReporterTestCase extends TestCase {
 
 	private final String EOF_CHAR = System.getProperty("line.separator");
 
 	public void test_NoJCov() throws RendererException, IOException {
 
 		ClassUnderTest classUnderTest = new ClassUnderTest();
 
 		{
 			Mutant mutant = new Mutant();
 			mutant.setBaseSourceLineStart(4);
 			mutant.setBaseSourceLineEnd(5);
 			mutant.setTransition("descriptionKey");
 			mutant.setSurvived(true);
 
 			classUnderTest.getMutant().add(mutant);
 		}
 
 		{
 			Mutant mutant = new Mutant();
 			mutant.setBaseSourceLineStart(7);
 			mutant.setBaseSourceLineEnd(7);
 			mutant.setTransition("descriptionKey");
 			mutant.setSurvived(false);
 
 			classUnderTest.getMutant().add(mutant);
 		}
 
 		SourceFile sourceFile = new SourceFile();
 
 		List<String> sourceCode = new ArrayList<String>();
 		sourceCode.add("1 xxxxxx");
 		sourceCode.add("2 xxxxxx");
 		sourceCode.add("3 xxxxxx");
 		sourceCode.add("4 xxxxxx");
 		sourceCode.add("5 xxxxxx");
 		sourceCode.add("6 xxxxxx");
 		sourceCode.add("7 xxxxxx");
 		sourceCode.add("8 xxxxxx");
 
 		sourceFile.setSourceText(sourceCode);
 
 		Writer outputWriter = new StringWriter();
 
 		String resource = "descriptionKey=Description";
 
 		PropertyResourceBundle bundle = new PropertyResourceBundle(
 				new StringBufferInputStream(resource));
 
 		HashMap<String, Object> context = new HashMap<String, Object>();
 		context.put("ingoreExternalTemplates", new Object());
 
 		ClassSnippetReporter snippetReporter = new ClassSnippetReporter(context);
 
 		snippetReporter.createReport(classUnderTest, sourceFile, outputWriter,
 				bundle);
 
 		StringBuffer expectedResult = new StringBuffer();
 		expectedResult.append("<html>").append(EOF_CHAR);
 		expectedResult.append("<head>").append(EOF_CHAR);
 		expectedResult.append("</head>").append(EOF_CHAR);
 		expectedResult.append("<body>").append(EOF_CHAR);
 		expectedResult.append("<table>").append(EOF_CHAR);
 		expectedResult.append("<tr>").append(EOF_CHAR);
 		expectedResult.append("<td>").append(EOF_CHAR);
 		expectedResult.append("2").append(EOF_CHAR);
 		expectedResult.append("</td>").append(EOF_CHAR);
 		expectedResult.append("<td>").append(EOF_CHAR);
 		expectedResult.append("<div class=\"uncovered\">").append(EOF_CHAR);
 		expectedResult.append("2 xxxxxx").append(EOF_CHAR);
 		expectedResult.append("</div>").append(EOF_CHAR);
 		expectedResult.append("</td>").append(EOF_CHAR);
 		expectedResult.append("</tr>").append(EOF_CHAR);
 		expectedResult.append("<tr>").append(EOF_CHAR);
 		expectedResult.append("<td>").append(EOF_CHAR);
 		expectedResult.append("3").append(EOF_CHAR);
 		expectedResult.append("</td>").append(EOF_CHAR);
 		expectedResult.append("<td>").append(EOF_CHAR);
 		expectedResult.append("<div class=\"uncovered\">").append(EOF_CHAR);
 		expectedResult.append("3 xxxxxx").append(EOF_CHAR);
 		expectedResult.append("</div>").append(EOF_CHAR);
 		expectedResult.append("</td>").append(EOF_CHAR);
 		expectedResult.append("</tr>").append(EOF_CHAR);
 		expectedResult.append("<tr>").append(EOF_CHAR);
 		expectedResult.append("<td>").append(EOF_CHAR);
 		expectedResult.append("4").append(EOF_CHAR);
 		expectedResult.append("</td>").append(EOF_CHAR);
 		expectedResult.append("<td>").append(EOF_CHAR);
 		expectedResult.append("<div class=\"covered_survived\">").append(
 				EOF_CHAR);
 		expectedResult.append("4 xxxxxx").append(EOF_CHAR);
 		expectedResult.append("</div>").append(EOF_CHAR);
 		expectedResult.append("</td>").append(EOF_CHAR);
 		expectedResult.append("</tr>").append(EOF_CHAR);
 		expectedResult.append("<tr>").append(EOF_CHAR);
 		expectedResult.append("<td>").append(EOF_CHAR);
 		expectedResult.append("5").append(EOF_CHAR);
 		expectedResult.append("</td>").append(EOF_CHAR);
 		expectedResult.append("<td>").append(EOF_CHAR);
 		expectedResult.append("<div class=\"covered_survived\">").append(
 				EOF_CHAR);
 		expectedResult.append("5 xxxxxx").append(EOF_CHAR);
 		expectedResult.append("</div>").append(EOF_CHAR);
 		expectedResult.append("</td>").append(EOF_CHAR);
 		expectedResult.append("</tr>").append(EOF_CHAR);
 		expectedResult.append("<tr>").append(EOF_CHAR);
 		expectedResult.append("<td>").append(EOF_CHAR);
 		expectedResult.append("6").append(EOF_CHAR);
 		expectedResult.append("</td>").append(EOF_CHAR);
 		expectedResult.append("<td>").append(EOF_CHAR);
 		expectedResult.append("<div class=\"uncovered\">").append(EOF_CHAR);
 		expectedResult.append("6 xxxxxx").append(EOF_CHAR);
 		expectedResult.append("</div>").append(EOF_CHAR);
 		expectedResult.append("</td>").append(EOF_CHAR);
 		expectedResult.append("</tr>").append(EOF_CHAR);
 		expectedResult.append("<tr>").append(EOF_CHAR);
 		expectedResult.append("<td>").append(EOF_CHAR);
 		expectedResult.append("7").append(EOF_CHAR);
 		expectedResult.append("</td>").append(EOF_CHAR);
 		expectedResult.append("<td>").append(EOF_CHAR);
 		expectedResult.append("<div class=\"uncovered\">").append(EOF_CHAR);
 		expectedResult.append("7 xxxxxx").append(EOF_CHAR);
 		expectedResult.append("</div>").append(EOF_CHAR);
 		expectedResult.append("</td>").append(EOF_CHAR);
 		expectedResult.append("</tr>").append(EOF_CHAR);
 		expectedResult.append("</table>").append(EOF_CHAR);
 		expectedResult.append("<table class=\"descriptionTable\">").append(
 				EOF_CHAR);
 		expectedResult.append("<tr><td>ID: NoName</td></tr>").append(EOF_CHAR);
 		expectedResult.append("<tr><td>Transition: descriptionKey</td></tr>")
 				.append(EOF_CHAR);
 		expectedResult.append("<tr><td>Transition: Description</td></tr>")
 				.append(EOF_CHAR);
		expectedResult.append("<tr><td>Defeating Tests: </td></tr>").append(EOF_CHAR);
 		expectedResult.append("</table>").append(EOF_CHAR);
 		expectedResult.append("<table>").append(EOF_CHAR);
 		expectedResult.append("<tr>").append(EOF_CHAR);
 		expectedResult.append("<td>").append(EOF_CHAR);
 		expectedResult.append("5").append(EOF_CHAR);
 		expectedResult.append("</td>").append(EOF_CHAR);
 		expectedResult.append("<td>").append(EOF_CHAR);
 		expectedResult.append("<div class=\"uncovered\">").append(EOF_CHAR);
 		expectedResult.append("5 xxxxxx").append(EOF_CHAR);
 		expectedResult.append("</div>").append(EOF_CHAR);
 		expectedResult.append("</td>").append(EOF_CHAR);
 		expectedResult.append("</tr>").append(EOF_CHAR);
 		expectedResult.append("<tr>").append(EOF_CHAR);
 		expectedResult.append("<td>").append(EOF_CHAR);
 		expectedResult.append("6").append(EOF_CHAR);
 		expectedResult.append("</td>").append(EOF_CHAR);
 		expectedResult.append("<td>").append(EOF_CHAR);
 		expectedResult.append("<div class=\"uncovered\">").append(EOF_CHAR);
 		expectedResult.append("6 xxxxxx").append(EOF_CHAR);
 		expectedResult.append("</div>").append(EOF_CHAR);
 		expectedResult.append("</td>").append(EOF_CHAR);
 		expectedResult.append("</tr>").append(EOF_CHAR);
 		expectedResult.append("<tr>").append(EOF_CHAR);
 		expectedResult.append("<td>").append(EOF_CHAR);
 		expectedResult.append("7").append(EOF_CHAR);
 		expectedResult.append("</td>").append(EOF_CHAR);
 		expectedResult.append("<td>").append(EOF_CHAR);
 		expectedResult.append("<div class=\"covered_killed\">")
 				.append(EOF_CHAR);
 		expectedResult.append("7 xxxxxx").append(EOF_CHAR);
 		expectedResult.append("</div>").append(EOF_CHAR);
 		expectedResult.append("</td>").append(EOF_CHAR);
 		expectedResult.append("</tr>").append(EOF_CHAR);
 		expectedResult.append("<tr>").append(EOF_CHAR);
 		expectedResult.append("<td>").append(EOF_CHAR);
 		expectedResult.append("8").append(EOF_CHAR);
 		expectedResult.append("</td>").append(EOF_CHAR);
 		expectedResult.append("<td>").append(EOF_CHAR);
 		expectedResult.append("<div class=\"uncovered\">").append(EOF_CHAR);
 		expectedResult.append("8 xxxxxx").append(EOF_CHAR);
 		expectedResult.append("</div>").append(EOF_CHAR);
 		expectedResult.append("</td>").append(EOF_CHAR);
 		expectedResult.append("</tr>").append(EOF_CHAR);
 		expectedResult.append("</table>").append(EOF_CHAR);
 		expectedResult.append("<table class=\"descriptionTable\">").append(
 				EOF_CHAR);
 		expectedResult.append("<tr><td>ID: NoName</td></tr>").append(EOF_CHAR);
 		expectedResult.append("<tr><td>Transition: descriptionKey</td></tr>")
 				.append(EOF_CHAR);
 		expectedResult.append("<tr><td>Transition: Description</td></tr>")
 				.append(EOF_CHAR);
		expectedResult.append("<tr><td>Defeating Tests: </td></tr>").append(EOF_CHAR);
 		expectedResult.append("</table>").append(EOF_CHAR);
 		expectedResult.append("</body>").append(EOF_CHAR);
 		expectedResult.append("</html>").append(EOF_CHAR);
 
 		assertEquals("Output mismatch", expectedResult.toString(), outputWriter
 				.toString());
 
 	}
 }
