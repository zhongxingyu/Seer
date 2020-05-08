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
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.retroduction.carma.reportgenerator.FreeMarkerRenderer;
 import org.retroduction.carma.reportgenerator.beans.PackageDetailBean;
 
 import freemarker.template.Configuration;
 
 public class PackageDetailsTemplateTestCase extends junit.framework.TestCase {
 
 	private final String EOF_CHAR = System.getProperty("line.separator");
 
 	public void test() {
 
 		Configuration cfg = new Configuration();
 
 		FreeMarkerRenderer renderer = new FreeMarkerRenderer(
 				"packageDetails.ftl", "/templates/");
 		renderer.setConfig(cfg);
 
 		Writer outputWriter = new StringWriter();
 
 		HashMap<String, Object> context = new HashMap<String, Object>();
 		context.put("ingoreExternalTemplates", new Object());
 
 		List<PackageDetailBean> packageDetailBeans = new ArrayList<PackageDetailBean>();
 
 		{
 			PackageDetailBean projectBean = new PackageDetailBean();
 			projectBean.setFqName("");
 			projectBean.setCoverageLevel(0.75);
 			projectBean.setNumberOfClasses(5);
 			projectBean.setNumberOfDefeatedMutations(3);
 			projectBean.setNumberOfSurvivedMutations(1);
 
 			context.put("projectDetailBean", projectBean);
 
 		}
 
 		{
 			PackageDetailBean bean = new PackageDetailBean();
 			bean.setFqName("pkg1.sub2");
 			bean.setCoverageLevel(0.4);
 			bean.setNumberOfClasses(12);
 			bean.setNumberOfDefeatedMutations(4);
 			bean.setNumberOfSurvivedMutations(5);
 			packageDetailBeans.add(bean);
 
 		}
 
 		{
 			PackageDetailBean bean = new PackageDetailBean();
 			bean.setFqName("apkg.sub2.sub3");
 			bean.setCoverageLevel(0.2);
 			bean.setNumberOfClasses(2);
 			bean.setNumberOfDefeatedMutations(6);
 			bean.setNumberOfSurvivedMutations(0);
 			packageDetailBeans.add(bean);
 
 		}
 
 		context.put("packageDetailBeans", packageDetailBeans);
 
 		renderer.render(context, outputWriter);
 
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
		expectedResult.append("<td>Survived Mutations Count</td>").append(
				EOF_CHAR);
		expectedResult.append("<td>Defeated Mutations Count</td>").append(
				EOF_CHAR);
 		expectedResult.append("</tr>").append(EOF_CHAR);
 		expectedResult.append("</thead>").append(EOF_CHAR);
 		expectedResult.append("<tbody>").append(EOF_CHAR);
 
 		expectedResult.append("<tr>").append(EOF_CHAR);
 		expectedResult.append("<td>All Packages</td>").append(EOF_CHAR);
 		expectedResult.append("<td>5</td>").append(EOF_CHAR);
 		expectedResult.append("<td>75 %</td>").append(EOF_CHAR);
 		expectedResult.append("<td>4</td>").append(EOF_CHAR);
 		expectedResult.append("<td>1</td>").append(EOF_CHAR);
 		expectedResult.append("<td>3</td>").append(EOF_CHAR);
 		expectedResult.append("</tr>").append(EOF_CHAR);
 
 		expectedResult.append("<tr>").append(EOF_CHAR);
 		expectedResult.append(
 				"<td><a href=\"pkg1.sub2.html\">pkg1.sub2</a></td>").append(
				EOF_CHAR);
 		expectedResult.append("<td>12</td>").append(EOF_CHAR);
 		expectedResult.append("<td>40 %</td>").append(EOF_CHAR);
 		expectedResult.append("<td>9</td>").append(EOF_CHAR);
 		expectedResult.append("<td>5</td>").append(EOF_CHAR);
 		expectedResult.append("<td>4</td>").append(EOF_CHAR);
 		expectedResult.append("</tr>").append(EOF_CHAR);
 
 		expectedResult.append("<tr>").append(EOF_CHAR);
 		expectedResult.append(
 				"<td><a href=\"apkg.sub2.sub3.html\">apkg.sub2.sub3</a></td>")
 				.append(EOF_CHAR);
 		expectedResult.append("<td>2</td>").append(EOF_CHAR);
 		expectedResult.append("<td>20 %</td>").append(EOF_CHAR);
 		expectedResult.append("<td>6</td>").append(EOF_CHAR);
 		expectedResult.append("<td>0</td>").append(EOF_CHAR);
 		expectedResult.append("<td>6</td>").append(EOF_CHAR);
 		expectedResult.append("</tr>").append(EOF_CHAR);
 
 		expectedResult.append("</tbody>").append(EOF_CHAR);
 		expectedResult.append("</table>").append(EOF_CHAR);
 		expectedResult.append("</body>").append(EOF_CHAR);
 		expectedResult.append("</html>").append(EOF_CHAR);
 
 		assertEquals("Output mismatch", expectedResult.toString(), outputWriter
 				.toString());
 		System.out.println(outputWriter.toString());
 
 	}
 
 }
