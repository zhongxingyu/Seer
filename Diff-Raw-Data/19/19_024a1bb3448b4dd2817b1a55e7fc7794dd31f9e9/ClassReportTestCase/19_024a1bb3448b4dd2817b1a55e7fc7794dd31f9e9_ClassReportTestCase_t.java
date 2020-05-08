 package com.retroduction.carma.report.generator.html;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import com.retroduction.carma.report.generator.html.IRenderer;
 import com.retroduction.carma.report.generator.html.RenderException;
 import com.retroduction.carma.report.generator.html.SingleClassReportCreator;
 import com.retroduction.carma.report.generator.html.coverage.ClassReport;
 import com.retroduction.carma.report.om.Project;
 import com.retroduction.carma.report.om.SourceFile;
 import com.retroduction.carma.xmlreport.om.ClassUnderTest;
 import com.retroduction.carma.xmlreport.om.MutationRun;
 
 public class ClassReportTestCase extends TestCase {
 
 	public void testGenerateReport() throws RenderException, IOException{
 		ClassReport report = new ClassReport();
 		MockReportCreator creatorMock = new MockReportCreator();
 		report.setReportCreator(creatorMock);
 		
 		ClassUnderTest cut = new ClassUnderTest();
 		cut.setClassName("myclass");
 		cut.setPackageName("mypackage");
 		
 		MutationRun mutationRun = new MutationRun();
 		mutationRun.getClassUnderTest().add(cut);
 		Project project = new Project();
 		SourceFile sourceInfo = new SourceFile();
 		sourceInfo.setClassName(cut.getClassName());
 		sourceInfo.setPackageName(cut.getPackageName());
 		project.addSourceFile(sourceInfo);
 		File outputDirectory = new File("sample");
 		IRenderer renderer = new MockRenderer();
 		report.generateReport(mutationRun, project, outputDirectory, renderer);
 		
 		assertEquals(renderer, creatorMock.renderer);
 		assertEquals(cut, creatorMock.clazz);
 		assertEquals(new File(outputDirectory, "mypackage.myclass.html"), creatorMock.reportFile);
 		assertEquals(sourceInfo, creatorMock.sourceFile);
		assertEquals("com/retroduction/carma/report/generator/html/coverage/class.html", creatorMock.templateName);
 		
 	}
 	
 	public void testGenerateReport_SeveralClasses() throws RenderException, IOException{
 		ClassReport report = new ClassReport();
 		MockReportCreator creatorMock = new MockReportCreator();
 		report.setReportCreator(creatorMock);
 		
 		ClassUnderTest cut = new ClassUnderTest();
 		cut.setClassName("myclass");
 		cut.setPackageName("mypackage");
 		
 		ClassUnderTest cut2 = new ClassUnderTest();
 		cut2.setClassName("myclass2");
 		cut2.setPackageName("mypackage");
 
 		MutationRun mutationRun = new MutationRun();
 		mutationRun.getClassUnderTest().add(cut);
 		mutationRun.getClassUnderTest().add(cut2);
 		Project project = new Project();
 
 		SourceFile sourceInfo = new SourceFile();
 		sourceInfo.setClassName(cut.getClassName());
 		sourceInfo.setPackageName(cut.getPackageName());
 		project.addSourceFile(sourceInfo);
 		SourceFile sourceInfo2 = new SourceFile();
 		sourceInfo2.setClassName(cut2.getClassName());
 		sourceInfo2.setPackageName(cut2.getPackageName());
 		project.addSourceFile(sourceInfo2);
 		File outputDirectory = new File("sample");
 		IRenderer renderer = new MockRenderer();
 		report.generateReport(mutationRun, project, outputDirectory, renderer);
 		
 		assertEquals(renderer, creatorMock.renderer);
 		assertEquals(cut2, creatorMock.clazz);
 		assertEquals(new File(outputDirectory, "mypackage.myclass2.html"), creatorMock.reportFile);
 		assertEquals(sourceInfo2, creatorMock.sourceFile);
		assertEquals("com/retroduction/carma/report/generator/html/coverage/class.html", creatorMock.templateName);
 		
 	}
 
 	public void testGenerateReport_NoSources() throws RenderException, IOException{
 		ClassReport report = new ClassReport();
 		MockReportCreator creatorMock = new MockReportCreator();
 		report.setReportCreator(creatorMock);
 		
 		ClassUnderTest cut = new ClassUnderTest();
 		cut.setClassName("myclass");
 		cut.setPackageName("mypackage");
 		
 		MutationRun mutationRun = new MutationRun();
 		mutationRun.getClassUnderTest().add(cut);
 		Project project = new Project();
 		File outputDirectory = new File("sample");
 		IRenderer renderer = new MockRenderer();
 		report.generateReport(mutationRun, project, outputDirectory, renderer);
 		
 		//never called
 		assertNull(creatorMock.renderer);
 		
 	}
 	
 	public void testGenerateReport_NoClasses() throws RenderException, IOException{
 		ClassReport report = new ClassReport();
 		MockReportCreator creatorMock = new MockReportCreator();
 		report.setReportCreator(creatorMock);
 		
 	
 		
 		MutationRunX mutationRun = new MutationRunX();
 		Project project = new Project();
 		File outputDirectory = new File("sample");
 		IRenderer renderer = new MockRenderer();
 		try {
 			report.generateReport(mutationRun, project, outputDirectory, renderer);
 			fail("Expected exception");
 			
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 		
 		//never called
 		assertNull(creatorMock.renderer);
 		
 	}	
 	
 	class MutationRunX extends MutationRun{
 
 		@Override
 		public List<ClassUnderTest> getClassUnderTest() {
 			return null;
 		}
 		
 	}
 	public void testGenerateReport_SorceFileNull() throws RenderException, IOException{
 		ClassReport report = new ClassReport();
 		MockReportCreator creatorMock = new MockReportCreator();
 		report.setReportCreator(creatorMock);
 		
 		ClassUnderTest cut = new ClassUnderTest();
 		cut.setClassName("myclass");
 		cut.setPackageName("mypackage");
 		
 		MutationRun mutationRun = new MutationRun();
 		Project project = new Project();
 		File outputDirectory = new File("sample");
 		IRenderer renderer = new MockRenderer();
 		report.generateReport(mutationRun, project, outputDirectory, renderer);
 		
 		//never called
 		assertNull(creatorMock.renderer);
 		
 	}	
 	class MockReportCreator extends SingleClassReportCreator{
 		ClassUnderTest clazz; SourceFile sourceFile; File reportFile; String templateName; IRenderer renderer;
 		@Override
 		public void createClassReport(ClassUnderTest clazz, SourceFile sourceFile, File reportFile, String templateName, IRenderer renderer) throws IOException {
 			this.clazz = clazz;
 			this.sourceFile = sourceFile;
 			this.reportFile = reportFile;
 			this.templateName = templateName;
 			this.renderer = renderer;
 			
 		}
 		
 	}
 }
