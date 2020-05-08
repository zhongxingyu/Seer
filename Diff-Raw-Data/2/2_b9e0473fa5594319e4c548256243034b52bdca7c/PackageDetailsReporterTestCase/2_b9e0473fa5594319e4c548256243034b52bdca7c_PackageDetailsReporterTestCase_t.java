 /**
  *
  *   Copyright Retroduction.org - All rights reserved
  *
  *   This file is part of Carma. Carma is licensed under the GPL 3
  *   (http://www.opensource.org/licenses/gpl-3.0.html).
  *
  */
 package org.retroduction.carma.reportgenerator.reporter;
 
 import java.io.StringWriter;
 import java.util.HashMap;
 
 import junit.framework.TestCase;
 
 import com.retroduction.carma.xmlreport.om.ClassUnderTest;
 import com.retroduction.carma.xmlreport.om.MutationRatio;
 import com.retroduction.carma.xmlreport.om.MutationRun;
 
 public class PackageDetailsReporterTestCase extends TestCase {
 	
 	private final String EOF_CHAR = System.getProperty("line.separator");
 
 
 	public void test() {
 
 		HashMap<String, Object> context = new HashMap<String, Object>();
 		context.put("ingoreExternalTemplates", new Object());
 
 		PackageDetailReporter reporter = new PackageDetailReporter(context);
 
 		MutationRun report = new MutationRun();
 
 		{
 			ClassUnderTest clazz = new ClassUnderTest();
 			clazz.setPackageName("package1");
 
 			MutationRatio ratio = new MutationRatio();
 			ratio.setMutationCount(5);
 			ratio.setSurvivorCount(3);
 
 			clazz.setMutationRatio(ratio);
 
 			report.getClassUnderTest().add(clazz);
 		}
 
 		{
 			ClassUnderTest clazz = new ClassUnderTest();
 			clazz.setPackageName("anotherPackage");
 
 			MutationRatio ratio = new MutationRatio();
 			ratio.setMutationCount(7);
 			ratio.setSurvivorCount(7);
 
 			clazz.setMutationRatio(ratio);
 
 			report.getClassUnderTest().add(clazz);
 		}
 
 		{
 			ClassUnderTest clazz = new ClassUnderTest();
 			clazz.setPackageName("package1");
 
 			MutationRatio ratio = new MutationRatio();
 			ratio.setMutationCount(3);
 			ratio.setSurvivorCount(1);
 
 			clazz.setMutationRatio(ratio);
 
 			report.getClassUnderTest().add(clazz);
 		}
 
 		StringWriter outputWriter = new StringWriter();
 
 		reporter.generateReport(report, outputWriter);
 
 		StringBuffer expectedResult = new StringBuffer();
 
 		expectedResult.append("<html>").append(EOF_CHAR);
 		expectedResult.append("<body>").append(EOF_CHAR);
 		expectedResult.append("<table>").append(EOF_CHAR);
 		expectedResult.append("<thead>").append(EOF_CHAR);
 		expectedResult.append("<tr>").append(EOF_CHAR);
 		expectedResult.append("<td>Package</td>").append(EOF_CHAR);
 		expectedResult.append("<td>Class Count</td>").append(EOF_CHAR);
 		expectedResult.append("<td>Coverage Level</td>").append(EOF_CHAR);
 		expectedResult.append("<td>Mutation Count</td>").append(EOF_CHAR);
 		expectedResult.append("<td>Survived Mutations Count</td>").append(EOF_CHAR);
 		expectedResult.append("<td>Defeated Mutations Count</td>").append(EOF_CHAR);
 		expectedResult.append("</tr>").append(EOF_CHAR);
 		expectedResult.append("</thead>").append(EOF_CHAR);
 		expectedResult.append("<tbody>").append(EOF_CHAR);
 
 		expectedResult.append("<tr>").append(EOF_CHAR);
 		expectedResult.append("<td>All Packages</td>").append(EOF_CHAR);
 		expectedResult.append("<td>3</td>").append(EOF_CHAR);
		expectedResult.append("<td>27 %</td>").append(EOF_CHAR);
 		expectedResult.append("<td>15</td>").append(EOF_CHAR);
 		expectedResult.append("<td>11</td>").append(EOF_CHAR);
 		expectedResult.append("<td>4</td>").append(EOF_CHAR);
 		expectedResult.append("</tr>").append(EOF_CHAR);
 
 		expectedResult.append("<tr>").append(EOF_CHAR);
 		expectedResult.append("<td><a href=\"anotherPackage.html\">anotherPackage</a></td>").append(EOF_CHAR);
 		expectedResult.append("<td>1</td>").append(EOF_CHAR);
 		expectedResult.append("<td>0 %</td>").append(EOF_CHAR);
 		expectedResult.append("<td>7</td>").append(EOF_CHAR);
 		expectedResult.append("<td>7</td>").append(EOF_CHAR);
 		expectedResult.append("<td>0</td>").append(EOF_CHAR);
 		expectedResult.append("</tr>").append(EOF_CHAR);
 
 		expectedResult.append("<tr>").append(EOF_CHAR);
 		expectedResult.append("<td><a href=\"package1.html\">package1</a></td>").append(EOF_CHAR);
 		expectedResult.append("<td>2</td>").append(EOF_CHAR);
 		expectedResult.append("<td>50 %</td>").append(EOF_CHAR);
 		expectedResult.append("<td>8</td>").append(EOF_CHAR);
 		expectedResult.append("<td>4</td>").append(EOF_CHAR);
 		expectedResult.append("<td>4</td>").append(EOF_CHAR);
 		expectedResult.append("</tr>").append(EOF_CHAR);
 
 		expectedResult.append("</tbody>").append(EOF_CHAR);
 		expectedResult.append("</table>").append(EOF_CHAR);
 		expectedResult.append("</body>").append(EOF_CHAR);
 		expectedResult.append("</html>").append(EOF_CHAR);
 
 		assertEquals("Output mismatch", expectedResult.toString(), outputWriter.toString());
 		System.out.println(outputWriter.toString());
 	}
 
 }
