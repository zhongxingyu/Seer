 package com.mutation.report.generator;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.bind.JAXBException;
 
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.Velocity;
 import org.apache.velocity.app.VelocityEngine;
 
 import com.mutation.report.loader.ReportModelLoader;
 import com.mutation.report.om.ClassUnderTest;
 import com.mutation.report.om.MutationRun;
 import com.mutation.report.source.om.Project;
 import com.mutation.report.source.om.SourceFile;
 
 public class SingleReportGenerator {
 
 	private static final String CLASS_PAGE = "class.vm";
 
 	private static final String REPORT_INDEX_TEMPLATE = "report.vm";
 
 	private static final String REPORT_INDEX_PAGE = "report.html";
 
 	private static final String CONTEXT_REPORT_KEY = "report";
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		if (args.length < 4) {
 			System.out
 					.println("Use arguments: reportInputFile, outputDirectory, templateDirectory, sourceFolder1, sourceFolder2, ...");
 			System.exit(-1);
 		}
 
 		try {
 
 			List<String> sourceFolders = new ArrayList<String>();
 			for (int i = 3; i < args.length; i++) {
 				sourceFolders.add(args[i]);
 			}
 			new SingleReportGenerator().perform(new ReportModelLoader().loadReportModel(args[0]), args[1], args[2],
 					sourceFolders);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (JAXBException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public void perform(MutationRun report, String outputDirectory, String templateFileDirectory,
 			List<String> sourceFolders) throws Exception {
 
 		VelocityEngine engine = new VelocityEngine();
 		engine.init();
 
 		ProjectBuilder builder = new ProjectBuilder();
 
 		Project project = builder.buildProject(sourceFolders);
 
 		if (!(new File(outputDirectory).exists()))
 			new File(outputDirectory).mkdir();
 
 		for (ClassUnderTest clazz : report.getClassUnderTest()) {
 			VelocityContext vcontext = new VelocityContext();
 			vcontext.put("class", clazz);
 
 			SourceFile sourceFile = project.getSourceFile(clazz.getPackageName(), clazz.getClassName());
 
 			vcontext.put("sourceFile", sourceFile);
 
 			StringWriter w = new StringWriter();
 
			Velocity.mergeTemplate(templateFileDirectory + "/" + CLASS_PAGE, "UTF-8", vcontext, w);
 
			FileWriter writer = new FileWriter(outputDirectory + "/" + clazz.getPackageName() + "."
					+ clazz.getClassName() + ".html");
 
 			writer.write(w.toString());
 			writer.close();
 		}
 
 		VelocityContext vcontext = new VelocityContext();
 		vcontext.put(CONTEXT_REPORT_KEY, report);
 		vcontext.put("project", project);
 
 		StringWriter w = new StringWriter();
 
		Velocity.mergeTemplate(templateFileDirectory + "/" + REPORT_INDEX_TEMPLATE, "UTF-8", vcontext, w);
 
		FileWriter writer = new FileWriter(outputDirectory + "/" + REPORT_INDEX_PAGE);
 		writer.write(w.toString());
 		writer.close();
 
 	}
 
 }
