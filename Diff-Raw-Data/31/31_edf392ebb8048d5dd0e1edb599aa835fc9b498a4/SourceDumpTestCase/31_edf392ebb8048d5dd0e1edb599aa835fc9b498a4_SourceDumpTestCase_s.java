 package com.retroduction.carma.report.generator.html.coverage;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import com.retroduction.carma.report.generator.FreeMarkerRenderer;
 import com.retroduction.carma.report.generator.SourceLineInfo;
 import com.retroduction.carma.report.generator.html.ITransitionDescription;
 import com.retroduction.carma.xmlreport.om.Mutant;
 
 import freemarker.template.Configuration;
 import freemarker.template.DefaultObjectWrapper;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 
 public class SourceDumpTestCase extends TestCase {
 
 	private static final String TEMPLATE_FOLDER = "src/main/resources/com/retroduction/carma/report/generator/html/coverage/";
 	private static final String TEMPLATE_UNDER_TEST = "sourceDump.ftl";
 
 	public void testWithJCov() throws TemplateException, IOException {
 
 		Configuration cfg = new Configuration();
 		cfg.setDirectoryForTemplateLoading(new File(TEMPLATE_FOLDER));
 		cfg.setObjectWrapper(new DefaultObjectWrapper());
 
 		Template templ = cfg.getTemplate(TEMPLATE_UNDER_TEST);
 
 		FreeMarkerRenderer frenderer = new FreeMarkerRenderer();
 		frenderer.setConfig(cfg);
 		frenderer.setOutputBaseDir(new File("target/template_test/"));
 
 		Map<String, Object> root = new HashMap<String, Object>();
 
 		List<SourceLineInfo> sourceLineInfoList = new ArrayList<SourceLineInfo>();
 		root.put("sourceInfo", sourceLineInfoList);
 
 		List<Mutant> mutants = null;
 
 		{
 			mutants = new ArrayList<Mutant>();
 
 			{
 				Mutant mutant = new Mutant();
 				mutant.setName("OneLineMutant1");
 				mutant.setSurvived(false);
 				mutant.setTransition("SomeTransition");
 				mutant.setBaseSourceLineStart(0);
 				mutant.setBaseSourceLineEnd(0);
 				mutant.setBaseSourceColumnStart(2);
 				mutant.setBaseSourceColumnEnd(4);
 
 				mutants.add(mutant);
 			}
 
 			sourceLineInfoList.add(new SourceLineInfo(0, "ABCDEFG", mutants));
 		}
 
 		{
 			mutants = new ArrayList<Mutant>();
 
 			{
 				Mutant mutant = new Mutant();
 				mutant.setName("OneLineMutant2");
 				mutant.setSurvived(true);
 				mutant.setTransition("SomeTransition1");
 				mutant.setBaseSourceLineStart(1);
 				mutant.setBaseSourceLineEnd(1);
 				mutant.setBaseSourceColumnStart(1);
 				mutant.setBaseSourceColumnEnd(5);
 				mutants.add(mutant);
 			}
 
 			sourceLineInfoList.add(new SourceLineInfo(1, "HIJKLMNOPQ", mutants));
 		}
 
 		{
 			mutants = new ArrayList<Mutant>();
 
 			{
 				Mutant mutant = new Mutant();
 				mutant.setName("TwoLineMutant");
 				mutant.setSurvived(true);
 				mutant.setTransition("SomeTransition1");
 				mutant.setBaseSourceLineStart(2);
 				mutant.setBaseSourceLineEnd(3);
 				mutant.setBaseSourceColumnStart(4);
 				mutant.setBaseSourceColumnEnd(10);
 				mutants.add(mutant);
 			}
 
 			sourceLineInfoList.add(new SourceLineInfo(2, "RSTUVW", mutants));
 			sourceLineInfoList.add(new SourceLineInfo(3, "XYZXYZXYZXYZ", mutants));
 		}
 
 		{
			mutants = new ArrayList<Mutant>();
 
 			{
 				Mutant mutant = new Mutant();
 				mutant.setName("ThreeLineMutant");
 				mutant.setSurvived(true);
 				mutant.setTransition("SomeTransition1");
 				mutant.setBaseSourceLineStart(4);
 				mutant.setBaseSourceLineEnd(6);
 				mutant.setBaseSourceColumnStart(5);
 				mutant.setBaseSourceColumnEnd(2);
				mutants.add(mutant);
 			}
 
			sourceLineInfoList.add(new SourceLineInfo(4, "RSTUVW", mutants));
			sourceLineInfoList.add(new SourceLineInfo(5, "XYZ", mutants));
			sourceLineInfoList.add(new SourceLineInfo(6, "ABCDEFGH", mutants));
 		}
 
 		sourceLineInfoList.add(new SourceLineInfo(7, "..............", null));
 
 		class MockTransitionDescription implements ITransitionDescription {
 			public String getString(String key) {
 				return "Mutants Description";
 			}
 		}
 
 		root.put("transitionsDescription", new MockTransitionDescription());
 
 		Writer out = new StringWriter();
 		out.write("<html><body>\n<script type=\"text/javascript\" ");
 		out.write("src=\"src/main/resources/com/retroduction/carma/report/");
 		out.write("generator/html/coverage/js/tooltip/wz_tooltip.js\"></script>\n");
 		out.write("<style type=\"text/css\">\n");
 		out.write("body { font-family: courier; }\n");
 		out.write("span.srcCovered { background: #90ff90; font-weight: bold;}\n");
 		out.write("span.srcUncovered { background: #ff9090; font-weight: bold;}\n");
 		out.write("</style>\n");
 		templ.process(root, out);
 		out.write("</body></html>");
 		out.flush();
 
 		System.out.println(out.toString());
 
 	}
 
 	public void testWithoutJCov() throws TemplateException, IOException {
 
 		Configuration cfg = new Configuration();
 		cfg.setDirectoryForTemplateLoading(new File(TEMPLATE_FOLDER));
 		cfg.setObjectWrapper(new DefaultObjectWrapper());
 
 		Template templ = cfg.getTemplate(TEMPLATE_UNDER_TEST);
 
 		FreeMarkerRenderer frenderer = new FreeMarkerRenderer();
 		frenderer.setConfig(cfg);
 		frenderer.setOutputBaseDir(new File("target/template_test/"));
 
 		Map<String, Object> root = new HashMap<String, Object>();
 
 		List<SourceLineInfo> sourceLineInfoList = new ArrayList<SourceLineInfo>();
 		root.put("sourceInfo", sourceLineInfoList);
 
 		List<Mutant> mutants = null;
 
 		{
 			mutants = new ArrayList<Mutant>();
 
 			{
 				Mutant mutant = new Mutant();
 				mutant.setName("OneLineMutant1");
 				mutant.setSurvived(false);
 				mutant.setTransition("SomeTransition");
 				mutant.setBaseSourceLineStart(0);
 				mutant.setBaseSourceLineEnd(0);
 				mutant.setBaseSourceColumnStart(0);
 				mutant.setBaseSourceColumnEnd(0);
 
 				mutants.add(mutant);
 			}
 
 			sourceLineInfoList.add(new SourceLineInfo(0, "ABCDEFG", mutants));
 		}
 
 		{
 			mutants = new ArrayList<Mutant>();
 
 			{
 				Mutant mutant = new Mutant();
 				mutant.setName("OneLineMutant2");
 				mutant.setSurvived(true);
 				mutant.setTransition("SomeTransition1");
 				mutant.setBaseSourceLineStart(1);
 				mutant.setBaseSourceLineEnd(1);
 				mutant.setBaseSourceColumnStart(0);
 				mutant.setBaseSourceColumnEnd(0);
 				mutants.add(mutant);
 			}
 
 			sourceLineInfoList.add(new SourceLineInfo(1, "HIJKLMNOPQ", mutants));
 		}
 
 		{
 			mutants = new ArrayList<Mutant>();
 
 			{
 				Mutant mutant = new Mutant();
 				mutant.setName("TwoLineMutant");
 				mutant.setSurvived(true);
 				mutant.setTransition("SomeTransition1");
 				mutant.setBaseSourceLineStart(2);
 				mutant.setBaseSourceLineEnd(3);
 				mutant.setBaseSourceColumnStart(0);
 				mutant.setBaseSourceColumnEnd(0);
 				mutants.add(mutant);
 			}
 
 			sourceLineInfoList.add(new SourceLineInfo(2, "RSTUVW", mutants));
 			sourceLineInfoList.add(new SourceLineInfo(3, "XYZXYZXYZXYZ", mutants));
 		}
 
 		{
 			mutants = new ArrayList<Mutant>();
 
 			{
 				Mutant mutant = new Mutant();
 				mutant.setName("ThreeLineMutant");
 				mutant.setSurvived(true);
 				mutant.setTransition("SomeTransition1");
 				mutant.setBaseSourceLineStart(4);
 				mutant.setBaseSourceLineEnd(6);
 				mutant.setBaseSourceColumnStart(5);
 				mutant.setBaseSourceColumnEnd(2);
 				mutants.add(mutant);
 			}
 
 			sourceLineInfoList.add(new SourceLineInfo(4, "RSTUVW", mutants));
 			sourceLineInfoList.add(new SourceLineInfo(5, "XYZ", mutants));
 			sourceLineInfoList.add(new SourceLineInfo(6, "ABCDEFGH", mutants));
 		}
 
 		sourceLineInfoList.add(new SourceLineInfo(7, "..............", null));
 
 		class MockTransitionDescription implements ITransitionDescription {
 			public String getString(String key) {
 				return "Mutants Description";
 			}
 		}
 
 		root.put("transitionsDescription", new MockTransitionDescription());
 
 		Writer out = new StringWriter();
 		out.write("<html><body>\n<script type=\"text/javascript\" ");
 		out.write("src=\"src/main/resources/com/retroduction/carma/report/");
 		out.write("generator/html/coverage/js/tooltip/wz_tooltip.js\"></script>\n");
 		out.write("<style type=\"text/css\">\n");
 		out.write("body { font-family: courier; }\n");
 		out.write("span.srcCovered { background: #90ff90; font-weight: bold;}\n");
 		out.write("span.srcUncovered { background: #ff9090; font-weight: bold;}\n");
 		out.write("</style>\n");
 		templ.process(root, out);
 		out.write("</body></html>");
 		out.flush();
 
		//System.out.println(out.toString());
 
 	}
 
 }
