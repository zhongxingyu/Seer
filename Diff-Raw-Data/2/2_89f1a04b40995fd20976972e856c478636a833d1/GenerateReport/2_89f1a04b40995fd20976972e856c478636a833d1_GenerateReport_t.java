 /**
  * report-phresco-plugin
  *
  * Copyright (C) 1999-2013 Photon Infotech Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.photon.phresco.plugins;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Type;
 import java.math.BigDecimal;
 import java.net.UnknownHostException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.UUID;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.Collection;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import net.sf.jasperreports.engine.JREmptyDataSource;
 import net.sf.jasperreports.engine.JRExporter;
 import net.sf.jasperreports.engine.JRExporterParameter;
 import net.sf.jasperreports.engine.JRPen;
 import net.sf.jasperreports.engine.JRPrintElement;
 import net.sf.jasperreports.engine.JRPrintPage;
 import net.sf.jasperreports.engine.JRPrintText;
 import net.sf.jasperreports.engine.JRStyle;
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.JasperReport;
 import net.sf.jasperreports.engine.data.JRBeanArrayDataSource;
 import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
 import net.sf.jasperreports.engine.util.JRLoader;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.MapUtils;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 import org.apache.pdfbox.util.PDFMergerUtility;
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
 import org.apache.poi.openxml4j.opc.OPCPackage;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.xssf.usermodel.XSSFSheet;
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 import org.htmlcleaner.CleanerProperties;
 import org.htmlcleaner.HtmlCleaner;
 import org.htmlcleaner.SimpleHtmlSerializer;
 import org.htmlcleaner.TagNode;
 import org.sonar.wsclient.Host;
 import org.sonar.wsclient.Sonar;
 import org.sonar.wsclient.connectors.HttpClient4Connector;
 import org.sonar.wsclient.services.Measure;
 import org.sonar.wsclient.services.Resource;
 import org.sonar.wsclient.services.ResourceQuery;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.PageSize;
 import com.itextpdf.text.Paragraph;
 import com.itextpdf.text.pdf.PdfWriter;
 import com.itextpdf.tool.xml.Pipeline;
 import com.itextpdf.tool.xml.XMLWorker;
 import com.itextpdf.tool.xml.XMLWorkerHelper;
 import com.itextpdf.tool.xml.exceptions.CssResolverException;
 import com.itextpdf.tool.xml.exceptions.RuntimeWorkerException;
 import com.itextpdf.tool.xml.html.Tags;
 import com.itextpdf.tool.xml.parser.XMLParser;
 import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
 import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
 import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
 import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
 import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.plugin.commons.MavenProjectInfo;
 import com.photon.phresco.plugin.commons.PluginConstants;
 import com.photon.phresco.plugin.commons.PluginUtils;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
 import com.photon.phresco.plugins.util.MojoUtil;
 import com.photon.phresco.plugins.util.PluginPackageUtil;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.model.Model;
 import com.phresco.pom.model.Plugin;
 import com.phresco.pom.model.Model.Modules;
 import com.phresco.pom.model.Model.Profiles;
 import com.phresco.pom.model.Profile;
 import com.phresco.pom.util.PomProcessor;
 
 public class GenerateReport implements PluginConstants {
 	private static final String MANUAL_TEST_REPORTS = "manualTestReports";
 	private static final String MULTI_MODULE_UNIT_TEST_REPORTS = "multiModuleUnitTestReports";
 	private static final String IS_MULTI_MODULE_PROJECT = "isMultiModuleProject";
 	private static final String CODE_VALIDATION_REPORT = "Code Validation Report : ";
 	private static final String SCRIPT = "script";
 	private static final String IS_CLASS_EMPTY = "isClassEmpty";
 	private static final String MODULE_NAME = "Module Name";
 	private static final String COPY_RIGHTS = "copyRights";
 	private static final String SONAR_REPORT = "sonarReport";
 	private static final String DEFAULT_COPYRIGHTS = "© 2013 Photon Interactive Pvt.Ltd";
 	private static final String PHRESCO_UNIT_TEST = "phresco.unitTest";
 	private static final String REPORTS_TYPE = "reportsDataType";
 	private static final String PROJECT_NAME = "projectName";
 	private static final String LOGO = "logo";
 	private static final String PDF_PROJECT_CODE = "projectCode";
 	private static final String MMM_DD_YYYY_HH_MMA = "ddMMMyyyy_hh.mma";
 	private MavenProject mavenProject;
 	private File baseDir;
 	private File rootDir;
 	private Log log;
 	
 	private File pomFile;
 	private String pomFileName = "";
 	
 	private static final String INDEX = "index";
 	private static final String HTML = "html";
 	
 	private String techName = "";
 	private String projectCode = null;
 	
 	private String testType = null;
 	private String rootTestType = null;
 	private String version = null;
 	private String projName = null;
 	private String appDir = "";
 	private String moduleName = "";
     private String reportType = null;
     private String sonarUrl = null;
     private String pdfReportName = "";
 	private boolean isClangReport;
 	private boolean showDeviceReport;
 	
 	// logo and theme objects
 	private String logo = null;
 	private Map<String, String> theme = null;
 	private String copyRights = "";
 	
     //test suite details
 	private int noOfTstSuiteTests = 0;
     private int noOfTstSuiteFailures = 0;
     private int noOfTstSuiteErrors = 0;
     
     private String fileName = null;
     
     String REPORTS_JASPER  = "";
     
     // custom theme color
     private static String titleColor = "#969696"; // TitleRectLogo
     private static String titleLabelColor = "#333333"; // TitleRectDetail
 	
     private static String headingForeColor = "#D0B48E"; // heading yellow color
     private static String headingBackColor = "#DE522F";
 
     private static String headingRowBackColor = "#A7A7A7"; //HeadingRow - light color
     private static String headingRowLabelBackColor = "#FFFFFF"; //Done
     private static String headingRowLabelForeColor = "#333333"; //Done - font color
     private static String headingRowTextBackColor = "#FFFFFF"; //Done
     private static String headingRowTextForeColor = "#333333"; //Done
 	
     private static String copyRightBackColor = "#333333";
     private static String copyRightForeColor = "#FFFFFF";
 	
     private static String copyRightPageNumberForeColor = "#FFFFFF";
     private static String copyRightPageNumberBackColor = "#DE522F";
 	
     public GenerateReport() {
     	final Date currentTime = Calendar.getInstance().getTime();
         final SimpleDateFormat yymmdd =new SimpleDateFormat(MMM_DD_YYYY_HH_MMA);
         this.fileName = yymmdd.format(currentTime);
     }
     
 	public void HtmlToPdfConverter(File targetHtmlIndexFile, String tempOutFileNameHTML, String tempOutFileNameIphoneSonarPDF)
 			throws IOException, DocumentException, CssResolverException {
 		try {
 			com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A4, 10f, 10f, 10f, 10f);
 			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(tempOutFileNameIphoneSonarPDF));
 			
 			document.open();
 			Paragraph paragraph = new Paragraph();
 			String reportnameName = targetHtmlIndexFile.getParentFile().getName();
 		    paragraph.add(CODE_VALIDATION_REPORT + reportnameName);
 		   	document.add(paragraph);
 		   	
 			HtmlPipelineContext htmlContext = new HtmlPipelineContext();
 			htmlContext.setTagFactory(Tags.getHtmlTagProcessorFactory());
 			
 			CleanerProperties props = new CleanerProperties();
 			props.setUseCdataForScriptAndStyle(true);
 			props.setPruneTags(SCRIPT);
 			props.setOmitComments(false);
 			
 			SimpleHtmlSerializer htmlSerializer = new SimpleHtmlSerializer(props);
 			TagNode tagNode = new HtmlCleaner(props).clean(targetHtmlIndexFile);
 			htmlSerializer.writeToFile(tagNode, tempOutFileNameHTML, UTF_8);
 			CSSResolver cssResolver = XMLWorkerHelper.getInstance().getDefaultCssResolver(false);
 			cssResolver.addCss("body {font-weight: bold;font-size: 6pt;}", true);
 			cssResolver.addCss("th {font-weight: bold;font-size: 6pt;}", true);
 			cssResolver.addCss("td {font-weight: bold;font-size: 6pt;}", true);
 			cssResolver.addCss("table {border:1;}", true);
 			cssResolver.addCss("h2,h1 {font-size: 10pt;margin: 0.5em 0em;" + "color:" + titleColor + "}", true);
 			cssResolver.addCss("a {text-decoration: underline;color:blue}", true);
 			
 			Pipeline<?> pipeline = new CssResolverPipeline(cssResolver, new HtmlPipeline(htmlContext,
 					new PdfWriterPipeline(document, writer)));
 			XMLWorker worker = new XMLWorker(pipeline, true);
 			XMLParser p = new XMLParser(worker);
 			File input = new File(tempOutFileNameHTML);
 			p.parse(new InputStreamReader(new FileInputStream(input), UTF_8));
 			document.close();
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		} catch (CssResolverException ce) {
 			ce.printStackTrace();
 		} catch (DocumentException de) {
 			de.printStackTrace();
 		} catch (RuntimeWorkerException re) {
 			re.printStackTrace();
 		}
 	}
     
 	public void generatePdfReport() throws PhrescoException {
 		ArrayList<JmeterTypeReport> jmeterTestResults = null;
 		try {
 			// Report generation for unit and functional
 			if (UNIT.equals(testType) || FUNCTIONAL.equals(testType) || COMPONENT.equals(testType) || MANUAL.equals(testType)) {
 				List<String> modules = PluginUtils.getProjectModules(mavenProject);
 				boolean isMultiModuleProject = false;
 				if (CollectionUtils.isNotEmpty(modules)) {
 					isMultiModuleProject = true;
 				}
 				
 				//crisp and detail view report generation
 				if (isMultiModuleProject) {
 					// multi module project....
 					List<ModuleSureFireReport> moduleWiseReports = new ArrayList<ModuleSureFireReport>();
 					for (String module : modules) {
 						ModuleSureFireReport msr = new ModuleSureFireReport();
 						SureFireReport sureFireReports = sureFireReports(module);
 						
 						List<TestSuite> testSuites = sureFireReports.getTestSuites();
 						if (CollectionUtils.isNotEmpty(testSuites)) {
 							msr.setModuleOrTechName(module);
 							msr.setModuleOrTechLabel(MODULE_NAME);
 							msr.setSureFireReport(Arrays.asList(sureFireReports));
 							moduleWiseReports.add(msr);
 						}
 					}
 					generateUnitAndFunctionalReport(moduleWiseReports);
 				} else {
 					// none module projects....
 					SureFireReport sureFireReports = sureFireReports(null);
 					
 					generateUnitAndFunctionalReport(sureFireReports);				
 				}
 			// Report generation for performance
 			} else if (PERFORMACE.equals(testType)) {
 				boolean deviceReportAvail = isDeviceReportAvail();
 				showDeviceReport = deviceReportAvail;
 				if(showDeviceReport) { //showDeviceReport
 					//android technology reports 
 					List<AndroidPerfReport> jmeterTestResultsForAndroid = getJmeterTestResultsForAndroid();
 					generateAndroidPerformanceReport(jmeterTestResultsForAndroid);
 				} else {
 					jmeterTestResults = getJmeterTestResults();
 					// Performance test report generation
 					generateJmeterPerformanceLoadReport(jmeterTestResults);
 				}
 			} else if(LOAD.equals(testType)) {
 				jmeterTestResults = getJmeterTestResults();
 				generateJmeterPerformanceLoadReport(jmeterTestResults);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new PhrescoException(e);
 		}
 	}
 	
 	//Consolidated report for all test
 	public void cumalitiveTestReport() throws Exception {
 		log.debug("Entering GenerateReport.cumalitiveTestReport()");
 		try {
 			Map<String, Object> cumulativeReportparams = new HashMap<String,Object>();
 			cumulativeReportparams.put(COPY_RIGHTS, copyRights);
 //			unit and functional details
 			testType = UNIT;
 			
 			boolean isMultiModuleProject = false;
 			List<String> modules = PluginUtils.getProjectModules(mavenProject);
 			if (CollectionUtils.isNotEmpty(modules)) {
 				isMultiModuleProject = true;
 			}
 			
 			//crisp and detail view report generation
 			ModuleSureFireReport msr = null;
 			if (isMultiModuleProject) {
 				// multi module project....
 				List<ModuleSureFireReport> moduleWiseReports = new ArrayList<ModuleSureFireReport>();
 				for (String module : modules) {
 					msr = new ModuleSureFireReport();
 					SureFireReport sureFireReports = sureFireReports(module);
 					
 					List<TestSuite> testSuites = sureFireReports.getTestSuites();
 					if (CollectionUtils.isNotEmpty(testSuites)) {
 						msr.setModuleOrTechName(module);
 						msr.setModuleOrTechLabel(MODULE_NAME);
 						msr.setSureFireReport(Arrays.asList(sureFireReports));
 						moduleWiseReports.add(msr);
 					}
 				}
 				cumulativeReportparams.put(IS_MULTI_MODULE_PROJECT, true);
 				cumulativeReportparams.put(MULTI_MODULE_UNIT_TEST_REPORTS, moduleWiseReports);
 			} else {
 				SureFireReport unitTestSureFireReports = null;
 				unitTestSureFireReports = sureFireReports(null);
 				
 				List<TestSuite> testSuitesUnit = unitTestSureFireReports.getTestSuites();
 				List<AllTestSuite> allTestSuitesUnit = unitTestSureFireReports.getAllTestSuites();
 				List<TestSuite> jsTestSuitesUnit = unitTestSureFireReports.getJsTestSuites();
 				List<AllTestSuite> jsAllTestSuitesUnit = unitTestSureFireReports.getJsAllTestSuites();
 				
 				cumulativeReportparams.put(IS_MULTI_MODULE_PROJECT, false);
 				if (CollectionUtils.isNotEmpty(allTestSuitesUnit) || CollectionUtils.isNotEmpty(testSuitesUnit) || CollectionUtils.isNotEmpty(jsTestSuitesUnit) || CollectionUtils.isNotEmpty(jsAllTestSuitesUnit)) {
 					cumulativeReportparams.put(UNIT_TEST_REPORTS, Arrays.asList(unitTestSureFireReports));
 				}
 			}
 			
 			testType = MANUAL;
 			SureFireReport manualSureFireReports = null;
 			manualSureFireReports = sureFireReports(null);
 			List<AllTestSuite> allTestSuitesManual = manualSureFireReports.getAllTestSuites();
 			List<TestSuite> testSuitesManual = manualSureFireReports.getTestSuites();
 			if (CollectionUtils.isNotEmpty(testSuitesManual) || CollectionUtils.isNotEmpty(allTestSuitesManual)) {
 				cumulativeReportparams.put(MANUAL_TEST_REPORTS, Arrays.asList(manualSureFireReports));
 			}
 			
 			testType = FUNCTIONAL;
 			boolean isClassEmpty = true;
 			List<TestSuite> testSuitesFunctional = null;
 			SureFireReport functionalSureFireReports = null;
 			functionalSureFireReports = sureFireReports(null);
 			List<AllTestSuite> allTestSuitesFunctional = functionalSureFireReports.getAllTestSuites();
 			testSuitesFunctional = functionalSureFireReports.getTestSuites();
 			if (CollectionUtils.isNotEmpty(testSuitesFunctional) || CollectionUtils.isNotEmpty(allTestSuitesFunctional)) {
 				for (TestSuite testSuite : testSuitesFunctional) {
 					List<TestCase> testcases = testSuite.getTestCases();
 					for (TestCase testCase : testcases) {
 						if (StringUtils.isNotEmpty(testCase.getTestClass())) {
 							isClassEmpty = false;
 						}
 					}
 				}
 				cumulativeReportparams.put(FUNCTIONAL_TEST_REPORTS, Arrays.asList(functionalSureFireReports));
 			}
 			
 			testType = COMPONENT;
 			List<TestSuite> testSuitesComponent = null;
 			SureFireReport componentSureFireReports = sureFireReports(null);
 			List<AllTestSuite> allTestSuitesComponent = null;
 			allTestSuitesComponent = componentSureFireReports.getAllTestSuites();
 			testSuitesComponent = componentSureFireReports.getTestSuites();
 			if (CollectionUtils.isNotEmpty(testSuitesComponent) || CollectionUtils.isNotEmpty(allTestSuitesComponent)) {
 				cumulativeReportparams.put(COMPONENT_TEST_REPORTS, Arrays.asList(componentSureFireReports));
 			}
 			
 			
 			testType = PERFORMACE;
 			//performance details
 			List<AndroidPerfReport> jmeterTestResultsForAndroid = null;
 			ArrayList<JmeterTypeReport> jmeterTestResults = null;
 			boolean deviceReportAvail = isDeviceReportAvail();
 			showDeviceReport = deviceReportAvail;
 			
 			if(showDeviceReport) { //showDeviceReport
 				jmeterTestResultsForAndroid = getJmeterTestResultsForAndroid();
 			} else {
 				jmeterTestResults = getJmeterTestResults();
 			}
 			
 			cumulativeReportparams.put(PDF_PROJECT_CODE, projectCode);
 			cumulativeReportparams.put(PROJECT_NAME, projName);
 			cumulativeReportparams.put(TECH_NAME, techName);
 			cumulativeReportparams.put(VERSION, version);
 			cumulativeReportparams.put(LOGO, logo);
 			
 			cumulativeReportparams.put(REPORTS_TYPE, reportType);
 			cumulativeReportparams.put(IS_CLASS_EMPTY, isClassEmpty);
 			
 			if(deviceReportAvail) {
 				cumulativeReportparams.put(PERFORMANCE_SPECIAL_HANDLE, true);
 				cumulativeReportparams.put(PERFORMANCE_TEST_REPORTS, jmeterTestResultsForAndroid);
 			} else {
 				cumulativeReportparams.put(PERFORMANCE_SPECIAL_HANDLE, false);
 				cumulativeReportparams.put(PERFORMANCE_TEST_REPORTS, jmeterTestResults);
 			}
 			
 			//load test details
 			testType = LOAD;
 			ArrayList<JmeterTypeReport> loadTestResults = null;
 			loadTestResults = getJmeterTestResults();
 			
 			if (loadTestResults != null) {
 				cumulativeReportparams.put(LOAD_TEST_REPORTS, loadTestResults);
 			}
 			
 			if (!isClangReport) {
 				//Sonar details
 				List<SonarReport> sonarReports = new ArrayList<SonarReport>();
 				String pomPath =  baseDir + File.separator + mavenProject.getFile().getName();
 				if (StringUtils.isNotEmpty(sonarUrl)) {
 					List<String> sonarTechReports = getSonarProfiles(pomPath);
 					if (sonarTechReports != null) {
 						if(CollectionUtils.isEmpty(sonarTechReports)) {
 							sonarTechReports.add(SONAR_SOURCE);
 						}
 						sonarTechReports.add(FUNCTIONAL);
 						for (String sonarTechReport : sonarTechReports) {
 							if (isMultiModuleProject) {
 								for (String module : modules) {
 									SonarReport srcSonarReport = generateSonarReport(sonarTechReport, module);
 									if(srcSonarReport != null) {
 										sonarReports.add(srcSonarReport);
 									}
 								}
 							} else {
 								SonarReport srcSonarReport = generateSonarReport(sonarTechReport, null);
 								if(srcSonarReport != null) {
 									sonarReports.add(srcSonarReport);
 								}
 							}
 						}
 					}
 					if (CollectionUtils.isNotEmpty(sonarReports)) {
 						cumulativeReportparams.put(SONAR_REPORT, sonarReports);
 					}
 				}
 			}
 			generateCumulativeTestReport(cumulativeReportparams);
 		} catch (Exception e) {
 			log.error("Report generation errorr ");
 			throw new PhrescoException(e);
 		}
 	}
 	
 	//cumulative test report generation
 	public void generateCumulativeTestReport(Map<String, Object> cumulativeReportparams) throws PhrescoException {
 		log.debug("Entering Method PhrescoReportGeneration.generateCumulativeTestReport()");
 		InputStream reportStream = null;
 		BufferedInputStream bufferedInputStream = null;
 		String uuid = UUID.randomUUID().toString();
 		String outFileNamePDF = "";
 		String semiPath = File.separator + baseDir.getName() + STR_UNDERSCORE + reportType + STR_UNDERSCORE + fileName + DOT + PDF;
 		try {
 			if (isClangReport) { // iphone
 				outFileNamePDF = Utility.getPhrescoTemp() + uuid + semiPath;
 			} else {
 				outFileNamePDF = baseDir + File.separator + "do_not_checkin" + File.separator + ARCHIVES + File.separator + CUMULATIVE + File.separator + fileName + DOT + PDF;
 			}
 			
 			new File(outFileNamePDF).getParentFile().mkdirs();
 			String jasperFile = "PhrescoCumulativeReport.jasper";
 			reportStream = this.getClass().getClassLoader().getResourceAsStream(REPORTS_JASPER + jasperFile);
 			bufferedInputStream = new BufferedInputStream(reportStream);
 			JREmptyDataSource  dataSource = new JREmptyDataSource();
 			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(bufferedInputStream);
 			
 			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, cumulativeReportparams, dataSource);
 			//applying theme
 			applyTheme(jasperPrint);
 			
 			// Move table of contents to first page only for detail report
 			if ("detail".equals(reportType)) {
 				jasperPrint = moveTableOfContents(jasperPrint);
 			}
 			
 			JRExporter exporter = new net.sf.jasperreports.engine.export.JRPdfExporter();
 			exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outFileNamePDF);
 			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
 			exporter.exportReport();
 			
 			if (isClangReport) {
 				String outFinalFileNamePDF = baseDir + File.separator + DO_NOT_CHECKIN_FOLDER + File.separator + ARCHIVES + File.separator + CUMULATIVE + File.separator + fileName + DOT + PDF;;
 				new File(outFinalFileNamePDF).getParentFile().mkdirs();
 				try {
 					//check static checker analysis folder contains html 
 					checkStaticAnalysisHtmlFile(uuid);
 					List<String> pdfs = new ArrayList<String>();
 					// get all pdf from that uuid location 
 					String codeValidationPdfs = Utility.getPhrescoTemp() + uuid;
 					File codeValidationsPdfDir = new File(codeValidationPdfs);
 					if (codeValidationsPdfDir.exists()) {
 						String[] extensions = new String[] { PDF };
 					    List<File> codeReportPdfs = (List<File>) FileUtils.listFiles(codeValidationsPdfDir, extensions, false);
 					    if (codeReportPdfs != null && codeReportPdfs.size() == 0) {
 					    	FileUtils.copyFile(new File(outFileNamePDF), new File(outFinalFileNamePDF));
 					    } else {
 					        for (File codeReportPdf : codeReportPdfs) {
 					            pdfs.add(codeReportPdf.getAbsolutePath());
 							}
 					        mergePdf(pdfs, outFinalFileNamePDF, uuid);
 					    }
 					}
 				} catch (Exception e) {
 					// just copy generated generated pdf to archive folder
 					FileUtils.copyFile(new File(outFileNamePDF), new File(outFinalFileNamePDF));
 				}
 			}
 			try {
 				FileUtils.deleteDirectory(new File(Utility.getPhrescoTemp() + uuid));
 			} catch (Exception e) {
 				// ignore the deletion failure
 			}
 		} catch(Exception e) {
 			log.error("Report generation error ");
 			throw new PhrescoException(e);
 		} finally {
 			if (reportStream != null) {
 				try {
 					reportStream.close();
 				} catch (IOException e) {
 					log.error("Report generation error ");
 				}
 			}
 			if (bufferedInputStream != null) {
 				try {
 					bufferedInputStream.close();
 				} catch (IOException e) {
 					log.error("Report generation error ");
 				}
 			}
 		}
 		
 	}
 	
 	
 	/**
 	 * This method moves TOC page to first page and sets page number to all the pages
 	 */
 	private static JasperPrint moveMultiModuleTableOfContents(JasperPrint jasperPrint)	{
 		JasperPrint moveIndexPage = moveIndexPage(jasperPrint);
 		
 		if (moveIndexPage != null) {
 			List<JRPrintPage> pages = moveIndexPage.getPages();
 			int minPageSizeForTOC = 2;
 			if (pages != null && pages.size() > minPageSizeForTOC) {
 				
 				List<JRPrintPage> tocPages = moveIndexPage.getPages();
 				
 				for (int i = 0; i < tocPages.size(); i++) {
 					
 					JRPrintPage jrPrintPage = (JRPrintPage) tocPages.get(i);
 					// set Page number as 1 for TOC
 					String pageNoKey = "pageNo";
 					String pageCountKey = "pageCount";
 					List<JRPrintElement> elements = jrPrintPage.getElements();
 					
 					if (CollectionUtils.isNotEmpty(elements)) {
 						
 						for (JRPrintElement jrPrintElement : elements) {
 							
 							// For the first page alone
 							if (jrPrintElement instanceof JRPrintText) {
 								JRPrintText jrPrintText = (JRPrintText)jrPrintElement;
 								String key = jrPrintText.getKey();
 								// For all pages
 								if (pageCountKey.equals(key)) {
 									String text = jrPrintText.getText().trim();
 									int pageCount = Integer.parseInt(text);
 									pageCount = pageCount - 1;
 									jrPrintText.setText(" " + pageCount);
 								}
 							}
 						}
 						
 					}
 					
 				}
 			}
 		}
 		return moveIndexPage;
 	}
 	
 	private static JasperPrint moveIndexPage(JasperPrint jasperPrint) {
 		if (jasperPrint != null) {
 			List pages = jasperPrint.getPages();
 			if (pages != null && pages.size() > 0) {
 				String key = "HIDDEN TEXT TO MARK THE BEGINNING OF THE TABLE OF CONTENTS";
 				JRPrintPage page = null;
 				Collection elements = null;
 				Iterator it = null;
 				JRPrintElement element = null;
 				int i = pages.size() - 1;
 				boolean isFound = false;
 				while(i >= 0 && !isFound) {
 					page = (JRPrintPage)pages.get(i);
 					elements = page.getElements();
 					if (elements != null && elements.size() > 0) {
 						it = elements.iterator();
 						while(it.hasNext() && !isFound) {
 							element = (JRPrintElement)it.next();
 							if (element instanceof JRPrintText) {
 								if ( key.equals( ((JRPrintText)element).getText() ) ) {
 									isFound = true;
 									break;
 								}
 							}
 						}
 					}
 					i--;
 				}
 				
 				if (isFound) {
 					for(int j = i + 1; j < pages.size(); j++) {
 						jasperPrint.addPage(j - i - 1, jasperPrint.removePage(j));
 					}
 				}
 			}
 		}
 		
 		return jasperPrint;
 	}
 	
 	/**
 	 * This method moves TOC page to first page and sets page number to all the pages
 	 */
 	private static JasperPrint moveTableOfContents(JasperPrint jasperPrint)	{
 		if (jasperPrint != null) {
 			List<JRPrintPage> pages = jasperPrint.getPages();
 			int minPageSizeForTOC = 2;
 			if (pages != null && pages.size() > minPageSizeForTOC) {
 				
 				// move the TOC to first page
 				int lastPage = pages.size() - 1;
 				int firstPage = 0;
 				JRPrintPage removePage = jasperPrint.removePage(lastPage);
 				jasperPrint.addPage(firstPage, removePage);
 				jasperPrint.removePage(firstPage + 1);
 
 				List<JRPrintPage> tocPages = jasperPrint.getPages();
 				
 				for (int i = 0; i < tocPages.size(); i++) {
 					
 					JRPrintPage jrPrintPage = (JRPrintPage) tocPages.get(i);
 					// set Page number as 1 for TOC
 					String pageNoKey = "pageNo";
 					String pageCountKey = "pageCount";
 					List<JRPrintElement> elements = jrPrintPage.getElements();
 					
 					if (CollectionUtils.isNotEmpty(elements)) {
 						
 						for (JRPrintElement jrPrintElement : elements) {
 							
 							// For the first page alone
 							if (jrPrintElement instanceof JRPrintText) {
 								JRPrintText jrPrintText = (JRPrintText)jrPrintElement;
 								String key = jrPrintText.getKey();
 								if (pageNoKey.equals(key) && i == 0) {
 									jrPrintText.setText("Page 1 of");
 								}
 								
 								// For all pages
 								if (pageCountKey.equals(key)) {
 									String text = jrPrintText.getText().trim();
 									int pageCount = Integer.parseInt(text);
 									pageCount = pageCount - 1;
 									jrPrintText.setText(" " + pageCount);
 								}
 							}
 						}
 						
 					}
 					
 				}
 			}
 		}
 		
 		return jasperPrint;
 	}
 	
 	private void checkStaticAnalysisHtmlFile(String uuid) {
 		try {
 			StringBuilder codeValidatePath = new StringBuilder(baseDir.getAbsolutePath());
 	    	codeValidatePath.append(mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_VALIDATE_REPORT));
 	    	
 	    	File codeValidationReportDir = new File(codeValidatePath.toString());
 	        
 	        if(!codeValidationReportDir.exists()) {
 	        	return;
 	        }
 	    	// if static analysis report dir is available 
 	    	List<File> targetFiles = null;
 	    	if (codeValidationReportDir.exists() && codeValidationReportDir.isDirectory()) {
 				targetFiles = new ArrayList<File>();
 				File[] listFiles = codeValidationReportDir.listFiles();
 				for (File targrtDir : listFiles) {
 					File targetIndexFile = new File(targrtDir, INDEX_HTML);
 					if (targrtDir.isDirectory() && targetIndexFile.exists()) {
 						targetFiles.add(targetIndexFile);
 					}
 				}
 	    	}
 	    	
 	    	for (int i = 0; i < targetFiles.size(); i++) {
 	    		String tempOutFileNameHTML = Utility.getPhrescoTemp() + uuid + File.separator + projectCode + STR_UNDERSCORE + INDEX + i + DOT + HTML;
 	    		String tempOutFileNameIphoneSonarPDF = Utility.getPhrescoTemp() + uuid + File.separator + projectCode + STR_UNDERSCORE + fileName + i + DOT + PDF;
 	    		
 	    		File targetHtmlIndexFile = targetFiles.get(i);
 	    		HtmlToPdfConverter(targetHtmlIndexFile, tempOutFileNameHTML, tempOutFileNameIphoneSonarPDF);
 	    	}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	// merge pdfs
 	public void mergePdf(List<String> PDFFiles,  String outputPDFFile, String uuid) throws PhrescoException {
 		try {
 			  // Get the byte streams from any source (maintain order)
 			  List<InputStream> sourcePDFs = new ArrayList<InputStream>();//ASA-iphonehybrid_detail_Apr 09 2013 19.04.pdf
 			  String outFinalFileNamePDF = Utility.getPhrescoTemp() + uuid + File.separator + baseDir.getName() + STR_UNDERSCORE + reportType + STR_UNDERSCORE + fileName + DOT + PDF;
 			  if (CollectionUtils.isNotEmpty(PDFFiles)) {
 				  sourcePDFs.add(new FileInputStream(new File(outFinalFileNamePDF)));
 				  for (String PDFFile : PDFFiles) {
 					  if(!PDFFile.equals(outFinalFileNamePDF)) {
 						  sourcePDFs.add(new FileInputStream(new File(PDFFile)));
 					  }
 				  }
 			  }
 			  // initialize the Merger utility and add pdfs to be merged
 			  PDFMergerUtility mergerUtility = new PDFMergerUtility();
 			  mergerUtility.addSources(sourcePDFs);
 			  // set the destination pdf name and merge input pdfs
 			  mergerUtility.setDestinationFileName(outputPDFFile);
 			  mergerUtility.mergeDocuments();
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	 public SonarReport generateSonarReport(String report, String module) throws PhrescoException {
 		log.debug("Entering Method PhrescoReportGeneration.generateSonarReport()");
 		SonarReport sonarReport = null;
 		try {
 			StringBuilder builder = new StringBuilder(baseDir.getAbsolutePath());
             if (StringUtils.isNotEmpty(report) && FUNCTIONALTEST.equals(report)) {
                 builder.append(mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_FUNCTEST_DIR));
             }
             
             if (!FUNCTIONALTEST.equals(report) && module != null) {
 				builder.append(File.separatorChar);
 				builder.append(module);
 			}
             
             builder.append(File.separatorChar);
         	File pomFile = new File(builder.toString() + mavenProject.getFile().getName());
             if(pomFile.exists()) {
             	builder.append(pomFile.getName());
             } else {
             	builder.append(POM_XML);
             }
         	log.debug("Sonar pom path " + builder.toString());
         	File pomPath = new File(builder.toString());
         	if (pomPath.exists()) {
         		PomProcessor processor = new PomProcessor(pomPath);
             	String groupId = processor.getModel().getGroupId();
             	String artifactId = processor.getModel().getArtifactId();
             	StringBuilder sbuild = new StringBuilder();
             	sbuild.append(groupId);
             	sbuild.append(COLON);
             	sbuild.append(artifactId);
             	if (StringUtils.isNotEmpty(report) && !SONAR_SOURCE.equals(report)) {
             		sbuild.append(COLON);
             		sbuild.append(report);
             	}
             	
             	String artifact = sbuild.toString();
     			Sonar sonar = new Sonar(new HttpClient4Connector(new Host(sonarUrl)));
     			
     			//metric key parameters for sonar 
     			String metrickey[] = {"ncloc", "lines", "files", "comment_lines_density" , "comment_lines", "duplicated_lines_density", "duplicated_lines", 
     					"duplicated_blocks", "duplicated_files", "function_complexity", "file_complexity", "violations_density", "blocker_violations", 
     					"critical_violations", "major_violations", "minor_violations", "info_violations", "violations",
     					"classes", "functions",
     					"statements","packages", "accessors", "public_documented_api_density", "public_undocumented_api","package_tangle_index","package_cycles", "package_feedback_edges", "package_tangles", "lcom4", "rfc",
     					"directories", "class_complexity", "comment_blank_lines", "coverage", "uncovered_lines"};
 
     			String methodkey[] = {"nonCommentLinesOfCode", "lines", "files", "commentLinesDensity" , "commentLines", "duplicatedLinesDensity", "duplicatedLines", 
     					"duplicatedBlocks", "duplicatedFiles", "functionComplexity", "fileComplexity", "violationsDensity", "blockerViolations", 
     					"criticalViolations", "majorViolations", "minorViolations", "infoViolations", "violations",
     					"classes", "functions",
     					"statements","packages", "accessors", "publicDocumentedApiDensity", "publicUndocumentedApi","packageTangleIndex","packageCycles", "packageFeedbackEdges", "packageTangles", "lackOfCohesionMethods", "responseForCode",
     					"directories", "classComplexity", "commentBlankLines", "coverage", "uncoveredLines"};
     			Resource resrc = sonar.find(ResourceQuery.createForMetrics(artifact, metrickey));
     			BeanUtils bu = new BeanUtils();
     			if (resrc != null) {
     				sonarReport = new SonarReport();
     				for (int i = 0; i < metrickey.length; i++) {
     					Measure measure = resrc.getMeasure(metrickey[i]);
     					if (measure != null) {
     						String formattedValue = resrc.getMeasure(metrickey[i]).getFormattedValue();
     						bu.setProperty(sonarReport, methodkey[i], formattedValue);
     					} 
     				}
     				sonarReport.setReportType(report);
     				if (module != null) {
     					sonarReport.setModuleName(module);
     				}
     			}
         	}
 			return sonarReport;
 		} catch (Exception e) {
 			e.getLocalizedMessage();
 		}
 		return sonarReport;
 	}
 	
 	// Unit and functional pdf report generation
 	public void generateUnitAndFunctionalReport(SureFireReport sureFireReports)  throws PhrescoException {
 		InputStream reportStream = null;
 		BufferedInputStream bufferedInputStream = null;
 		try {
 			boolean isClassEmpty = true;
 			if (FUNCTIONAL.equals(testType)) {
 				List<TestSuite> testSuites = sureFireReports.getTestSuites();
 				for (TestSuite testSuite : testSuites) {
 					List<TestCase> testcases = testSuite.getTestCases();
 					for (TestCase testCase : testcases) {
 						if (StringUtils.isNotEmpty(testCase.getTestClass())) {
 							isClassEmpty = false;
 						}
 					}
 				}
 			}
 			
 			String outFileNamePDF = baseDir.getAbsolutePath() + File.separator + DO_NOT_CHECKIN_FOLDER + File.separator + ARCHIVES + File.separator + testType + File.separator + fileName + DOT + PDF;
 			new File(outFileNamePDF).getParentFile().mkdirs();
 			String containerJasperFile = "PhrescoSureFireReport.jasper";
 			reportStream = this.getClass().getClassLoader().getResourceAsStream(REPORTS_JASPER + containerJasperFile);
 			bufferedInputStream = new BufferedInputStream(reportStream);
 			Map<String, Object> parameters = new HashMap<String,Object>();
 			parameters.put(COPY_RIGHTS, copyRights);
 			parameters.put(PDF_PROJECT_CODE, projectCode);
 			parameters.put(PROJECT_NAME, projName);
 			parameters.put(TECH_NAME, techName);
 			parameters.put(TEST_TYPE, testType);
 			parameters.put(REPORTS_TYPE, reportType);
 			parameters.put(VERSION, version);
 			parameters.put(LOGO, logo);
 			parameters.put(IS_CLASS_EMPTY, isClassEmpty);
 			
 			JRBeanArrayDataSource dataSource = new JRBeanArrayDataSource(new SureFireReport[]{sureFireReports});
 			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(bufferedInputStream);
 			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
 			// applying theme
 			applyTheme(jasperPrint);
 			JRExporter exporter = new net.sf.jasperreports.engine.export.JRPdfExporter(); 
 			exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outFileNamePDF);
 			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
 			exporter.exportReport();
 		} catch(Exception e) {
 			log.error("Unit and functional generation error");
 			e.printStackTrace();
 			throw new PhrescoException(e);
 		} finally {
 			if (reportStream != null) {
 				try {
 					reportStream.close();
 				} catch (IOException e) {
 					log.error("Report generation errorr ");
 				}
 			}
 			if (bufferedInputStream != null) {
 				try {
 					bufferedInputStream.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 					log.error("Report generation errorr ");
 				}
 			}
 		}
 	}
 	
 	// Unit and functional pdf report generation
 	public void generateUnitAndFunctionalReport(List<ModuleSureFireReport> moduleWiseReports)  throws PhrescoException {
 		InputStream reportStream = null;
 		BufferedInputStream bufferedInputStream = null;
 		try {
 			String outFileNamePDF = baseDir.getAbsolutePath() + File.separator + DO_NOT_CHECKIN_FOLDER + File.separator + ARCHIVES + File.separator + testType + File.separator + fileName + DOT + PDF;
 
 			new File(outFileNamePDF).getParentFile().mkdirs();
 			reportStream = this.getClass().getClassLoader().getResourceAsStream("PhrescoModuleSureFireReport.jasper");
 			bufferedInputStream = new BufferedInputStream(reportStream);
 			Map<String, Object> parameters = new HashMap<String,Object>();
 			parameters.put(COPY_RIGHTS, copyRights);
 			parameters.put(PDF_PROJECT_CODE, projectCode);
 			parameters.put(PROJECT_NAME, projName);
 			parameters.put(TECH_NAME, techName);
 			parameters.put(TEST_TYPE, testType);
 			parameters.put(REPORTS_TYPE, reportType);
 			parameters.put(VERSION, version);
 			parameters.put(LOGO, logo);
 			JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(moduleWiseReports);
 			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(bufferedInputStream);
 			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
 			// applying theme
 			applyTheme(jasperPrint);
 			JRExporter exporter = new net.sf.jasperreports.engine.export.JRPdfExporter(); 
 			exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outFileNamePDF);
 			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
 			exporter.exportReport();
 		} catch(Exception e) {
 			log.error("Unit and functional  generation error");
 			throw new PhrescoException(e);
 		} finally {
 			if (reportStream != null) {
 				try {
 					reportStream.close();
 				} catch (IOException e) {
 					log.error("Report generation errorr ");
 				}
 			}
 			if (bufferedInputStream != null) {
 				try {
 					bufferedInputStream.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 					log.error("Report generation errorr ");
 				}
 			}
 		}
 	}
 	
 	// performance test report
 	public void generateJmeterPerformanceLoadReport(ArrayList<JmeterTypeReport> jmeterTestResults)  throws PhrescoException {
 		try {
 			ArrayList<JmeterTypeReport> jmeterTstResults = jmeterTestResults;
 			String outFileNamePDF = baseDir.getAbsolutePath() + File.separator + DO_NOT_CHECKIN_FOLDER + File.separator + ARCHIVES + File.separator + testType + File.separator + fileName + DOT + PDF;
 
 			String jasperFile = "PhrescoPerfContain.jasper";
 			Map<String, Object> parameters = new HashMap<String,Object>();
 			parameters.put(COPY_RIGHTS, copyRights);
 			parameters.put(PDF_PROJECT_CODE, projectCode);
 			parameters.put(PROJECT_NAME, projName);
 			parameters.put(TECH_NAME, techName);
 			parameters.put(TEST_TYPE, testType.toUpperCase());
 			parameters.put(REPORTS_TYPE, reportType);
 			parameters.put(VERSION, version);
 			parameters.put(LOGO, logo);
 			JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(jmeterTstResults);
 			reportGenerate(outFileNamePDF, jasperFile, parameters, dataSource);
 		} catch (Exception e) {
 			log.error("Performance  generation error ");
 			throw new PhrescoException(e);
 		}
 	}
 	
 	// performance test report
 	public void generateAndroidPerformanceReport(List<AndroidPerfReport> androidPerReports)  throws PhrescoException {
 		try {
 			String outFileNamePDF = baseDir.getAbsolutePath() + File.separator + DO_NOT_CHECKIN_FOLDER + File.separator + ARCHIVES + File.separator + testType + File.separator + fileName + DOT + PDF;
 
 			String jasperFile = "PhrescoAndroidPerfContain.jasper";
 			Map<String, Object> parameters = new HashMap<String,Object>();
 			parameters.put(COPY_RIGHTS, copyRights);
 			parameters.put(PDF_PROJECT_CODE, projectCode);
 			parameters.put(PROJECT_NAME, projName);
 			parameters.put(TECH_NAME, techName);
 			parameters.put(TEST_TYPE, testType);
 			parameters.put(REPORTS_TYPE, reportType);
 			parameters.put(VERSION, version);
 			parameters.put(LOGO, logo);
 			JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(androidPerReports);
 			reportGenerate(outFileNamePDF, jasperFile, parameters, dataSource);
 		} catch (Exception e) {
 			log.error("Android Performance  generation error ");
 			throw new PhrescoException(e);
 		}
 	}
 	
 	// load test report
 //	public void generateLoadTestReport(List<LoadTestReport> loadTestResults)  throws PhrescoException {
 //		try {
 //			String outFileNamePDF = baseDir.getAbsolutePath() + File.separator + DO_NOT_CHECKIN_FOLDER + File.separator + ARCHIVES + File.separator + testType + File.separator + fileName + DOT + PDF;
 //
 //			String jasperFile = "PhrescoLoadTestContain.jasper";
 //			Map<String, Object> parameters = new HashMap<String,Object>();
 //			parameters.put(COPY_RIGHTS, copyRights);
 //			parameters.put(PDF_PROJECT_CODE, projectCode);
 //			parameters.put(PROJECT_NAME, projName);
 //			parameters.put(TECH_NAME, techName);
 //			parameters.put(TEST_TYPE, testType.toUpperCase());
 //			parameters.put(REPORTS_TYPE, reportType);
 //			parameters.put(VERSION, version);
 //			parameters.put(LOGO, logo);
 //			JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(loadTestResults);
 //			reportGenerate(outFileNamePDF, jasperFile, parameters, dataSource);
 //		} catch (Exception e) {
 //			log.error("Load report generation error");
 //			throw new PhrescoException(e);
 //		}
 //	}
 	
 	public void reportGenerate(String outFileNamePDF, String jasperFile, Map<String, Object> parameters, JRBeanCollectionDataSource dataSource) throws PhrescoException {
 		InputStream reportStream = null;
 		BufferedInputStream bufferedInputStream = null;
 		try {
 			new File(outFileNamePDF).getParentFile().mkdirs();
 			reportStream = this.getClass().getClassLoader().getResourceAsStream(REPORTS_JASPER + jasperFile);
 			bufferedInputStream = new BufferedInputStream(reportStream);
 			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(bufferedInputStream);
 			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
 			// applying theme
 			applyTheme(jasperPrint);
 			JRExporter exporter = new net.sf.jasperreports.engine.export.JRPdfExporter(); 
 			exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outFileNamePDF);
 			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
 			exporter.exportReport(); 
 		} catch (Exception e) {
 			log.error("Load report generation error");
 			throw new PhrescoException(e);
 		} finally {
 			if (reportStream != null) {
 				try {
 					reportStream.close();
 				} catch (IOException e) {
 					log.error("Report generation errorr ");
 				}
 			}
 			if (bufferedInputStream != null) {
 				try {
 					bufferedInputStream.close();
 				} catch (IOException e) {
 					log.error("Report generation errorr ");
 				}
 			}
 		}
 	}
 	
 //	public List<LoadTestReport> getLoadTestResults()  throws Exception {
 //		List<String> testResultsTypes = new ArrayList<String>();
 //        testResultsTypes.add("server");
 //        testResultsTypes.add("webservice");
 //        
 //		List<LoadTestReport> loadTestReports = new ArrayList<LoadTestReport>();
 //		String reportFilePath = baseDir + mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_LOADTEST_RPT_DIR);
 //		String testResulExtension = mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_LOADTEST_RESULT_EXTENSION);
 //		List<File> testResultFiles = new ArrayList<File>();
 //		List<File> resultFiles = null;
 //		// if the load is having dir_type key
 //		if (reportFilePath.contains(DIR_TYPE)) {
 //			for(String loadType: testResultsTypes) {
 //				Pattern p = Pattern.compile(DIR_TYPE);
 //                Matcher matcher = p.matcher(reportFilePath);
 //                String loadReportFilePath = matcher.replaceAll(loadType);
 //                if (StringUtils.isNotEmpty(testResulExtension)) {
 //                	resultFiles = getResultFileExtension(loadReportFilePath, testResulExtension);
 //                }
 //                
 //                if (CollectionUtils.isNotEmpty(resultFiles)) {
 //                	testResultFiles.addAll(resultFiles);
 //                }
 //			}
 //		} else {
 //			if (StringUtils.isNotEmpty(testResulExtension)) {
 //            	resultFiles = getResultFileExtension(reportFilePath, testResulExtension);
 //            }
 //			
 //            if (CollectionUtils.isNotEmpty(resultFiles)) {
 //            	testResultFiles.addAll(resultFiles);
 //            }
 //		}
 //		
 //		for (File resultFile : testResultFiles) {
 //			Document doc = getDocumentOfFile(resultFile);
 //			List<TestResult> loadTestResults = getLoadTestResult(doc);
 //			
 //			// Adding report data to bean object
 //			LoadTestReport loadTestReport = new LoadTestReport();
 //			loadTestReport.setFileName(resultFile.getName());
 //			loadTestReport.setTestResults(loadTestResults);
 //			loadTestReports.add(loadTestReport);
 //		}
 //		return loadTestReports;
 //	}
 	
 	public ArrayList<JmeterTypeReport> getJmeterTestResults() throws Exception {
         List<String> testResultsTypes = new ArrayList<String>();
         testResultsTypes.add("server");
         testResultsTypes.add("database");
         testResultsTypes.add("webservice");
         
         // List of performance test types
         ArrayList<JmeterTypeReport> jmeterTypeReports = new ArrayList<JmeterTypeReport>();
         ApplicationInfo applicationInfo = getApplicationInfo();
         for(String testResultsType : testResultsTypes) {
         	String reportDir = "";
         	String reportExtension = "";
         	if (PERFORMACE.equals(testType)) {
         		reportDir = baseDir + mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_PERFORMANCETEST_RPT_DIR);
                 reportExtension = mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_PERFORMANCETEST_RESULT_EXTENSION);
         	} else if (LOAD.equals(testType)) {
         		reportDir = baseDir + mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_LOADTEST_RPT_DIR);
         		reportExtension = mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_LOADTEST_RESULT_EXTENSION);
         	}
         	
             if (StringUtils.isNotEmpty(reportDir) && StringUtils.isNotEmpty(testResultsType)) {
                 Pattern p = Pattern.compile(DIR_TYPE);
                 Matcher matcher = p.matcher(reportDir);
                 reportDir = matcher.replaceAll(testResultsType);
             }
             
             // to get performance extension tag value from pom
             List<String> testResultFiles = null;
             if (StringUtils.isNotEmpty(reportExtension)) {
             	testResultFiles = getTestResultFiles(reportDir, reportExtension);
             }
             
 			String deviceId = null; // for android alone
 			
 			// List of performance test reports
 			List<JmeterReport> jmeterReports = new ArrayList<JmeterReport>();
 			if (CollectionUtils.isNotEmpty(testResultFiles)) {
 				for (String testResultFile : testResultFiles) {
 					List<Images> screenShots = new ArrayList<Images>();
 					Document document = getDocumentOfFile(reportDir, testResultFile);
 					JmeterReport jmeterReport = getPerformanceReport(document, testResultFile, deviceId); // need to pass tech id and tag name
 					// get images of the perforamce report file
 					screenShots = getScreenShot(testResultsType, testResultFile, applicationInfo.getAppDirName(), testType);
 					if(CollectionUtils.isNotEmpty(screenShots)) {
 						jmeterReport.setImages(screenShots); 
 					}
 					
 					jmeterReports.add(jmeterReport);
 				}
 			}
             // When data is not available dont show in i report
             if (!jmeterReports.isEmpty()) {
 	            JmeterTypeReport jmeterTypeReport = new JmeterTypeReport();
 	            jmeterTypeReport.setType(testResultsType);
 	            jmeterTypeReport.setFileReport(jmeterReports);
 	            // adding final data to jmeter type reports
 	            jmeterTypeReports.add(jmeterTypeReport);
             }
         }
         
         for (JmeterTypeReport jmeterTypeReport : jmeterTypeReports) {
         	String type = jmeterTypeReport.getType();
         	List<JmeterReport> fileReports = jmeterTypeReport.getFileReport();
 		}
         return jmeterTypeReports;
 	}
 	
 	private List<Images> getScreenShot(String testAgainst, String resultFile, String appDirName, String from) throws PhrescoException {
 		List<Images> imgSources = new ArrayList<Images>();
 		try {
 			String testDir = "";
 			StringBuilder sb = new StringBuilder(baseDir.toString());
 			StringBuilder pomDir = new StringBuilder(baseDir.toString());
 			pomDir.append(File.separator)
 			.append("test")
 			.append(File.separator)
 			.append(from)
 			.append(File.separator)
 			.append(testAgainst)
 			.append(File.separator);
 			
 			int lastDot = resultFile.lastIndexOf(".");
 			String resultName = resultFile.substring(0, lastDot);
 
 			File testPomFile = new File(pomDir.toString() + File.separator + POM_XML);
 			PomProcessor pomProcessor = new PomProcessor(testPomFile);
 			Plugin plugin = pomProcessor.getPlugin(COM_LAZERYCODE_JMETER, JMETER_MAVEN_PLUGIN);
 			com.phresco.pom.model.Plugin.Configuration jmeterConfiguration = plugin.getConfiguration();
 			List<Element> jmeterConfigs = jmeterConfiguration.getAny();
 			for (Element element : jmeterConfigs) {
 				if (PLUGIN_TYPES.equalsIgnoreCase(element.getTagName()) && element.hasChildNodes()) {
 					NodeList types = element.getChildNodes();
 					for (int i = 0; i < types.getLength(); i++) {
 						Node pluginType = types.item(i);
 						if (StringUtils.isNotEmpty(pluginType.getTextContent())) {
 							File imgFile = new File(pomDir.toString() + RESULTS_JMETER_GRAPHS + resultName + HYPHEN + pluginType.getTextContent() + PNG);
 							if (imgFile.exists()) {
 								Images imgSource = new Images();
 								InputStream imageStream = new FileInputStream(imgFile);
 								String imgSrc = new String(Base64.encodeBase64(IOUtils.toByteArray(imageStream)));
 								imgSource.setImage(imgSrc);
 								imgSource.setImageName(resultName + HYPHEN + pluginType.getTextContent());
 								imgSources.add(imgSource);
 							}
 						}
 					}
 				}
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new PhrescoException(e);
 		}
 		
 		return imgSources;
 	}
 	
 	public List<AndroidPerfReport> getJmeterTestResultsForAndroid() throws Exception {
         // List of performance test types
         String performanceReportDir = baseDir + mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_PERFORMANCETEST_RPT_DIR);
         String performanceReportExtension = mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_PERFORMANCETEST_RESULT_EXTENSION);
         List<String> testResultFiles = null;
         
         //check for performance result extension tag in pom
         if (StringUtils.isNotEmpty(performanceReportExtension)) {
         	testResultFiles = getTestResultFiles(performanceReportDir, performanceReportExtension);
         }
 		
 		// List of performance test reports
         List<AndroidPerfReport> androidPerfFilesWithDatas = new ArrayList<AndroidPerfReport>();
 		if (CollectionUtils.isNotEmpty(testResultFiles)) {
 			for (String testResultFile : testResultFiles) {
 				Document document = getDocumentOfFile(performanceReportDir, testResultFile);
 
 				Map<String, String> deviceNamesWithId = getDeviceNames(document);
 				
 				Set st = deviceNamesWithId.entrySet();
 				Iterator it = st.iterator();
 				List<JmeterReport> androidDeviceWithDatas = new ArrayList<JmeterReport>();
 				while (it.hasNext()) {
 					Map.Entry m = (Map.Entry) it.next();
 					String androidDeviceId = (String) m.getKey();
 					String androidDeviceName = (String) m.getValue();
 					JmeterReport jmeterReport = getPerformanceReport(document, testResultFile, androidDeviceId); // need to pass tech id and tag name
 					jmeterReport.setFileName(androidDeviceName);
 					androidDeviceWithDatas.add(jmeterReport);
 				}
 				AndroidPerfReport androidPerReport = new AndroidPerfReport();
 				androidPerReport.setFileName(testResultFile);
 				androidPerReport.setDeviceReport(androidDeviceWithDatas);
 				androidPerfFilesWithDatas.add(androidPerReport);
 			}
         }
         for (AndroidPerfReport androidPerfFilesWithData : androidPerfFilesWithDatas) {
         	List<JmeterReport> deviceReports = androidPerfFilesWithData.getDeviceReport();
         	for (JmeterReport jmeterReport : deviceReports) {
 //        		log.info("getTotalAvg .. " + jmeterReport.getTotalAvg());
 			}
 		}
         return androidPerfFilesWithDatas;
 	}
 	
 	public boolean isDeviceReportAvail() throws Exception {
         // List of performance test types
         String performanceReportDir = baseDir + mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_PERFORMANCETEST_RPT_DIR);
         List<String> testResultFiles = getTestResultFiles(performanceReportDir);
 		
 		// List of performance test reports
 		if (CollectionUtils.isNotEmpty(testResultFiles)) {
 			for (String testResultFile : testResultFiles) {
 				Document document = getDocumentOfFile(performanceReportDir, testResultFile);
 				Map<String, String> deviceNamesWithId = getDeviceNames(document);
 				if (MapUtils.isNotEmpty(deviceNamesWithId)) {
 					return true;
 				}
 			}
 		}
         return false;
 	}
 	
 	// unit and functional test report
 	public ArrayList<XmlReport> sureFireReport() throws Exception {
 		ArrayList<XmlReport> xmlReports = new ArrayList<XmlReport>();
 		String reportFilePath = "";
 		if (UNIT.equals(testType)) {
 			reportFilePath = baseDir + mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_UNITTEST_DIR);
 		} else {
 			reportFilePath = baseDir + mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_FUNCTEST_DIR);
 		}
 		List<String> testResultFiles = getTestResultFiles(reportFilePath);
 		for (String resultFile : testResultFiles) {
 			XmlReport xmlReport = new XmlReport();
 			xmlReport.setFileName(resultFile);
 			ArrayList<TestSuite> testSuiteWithTestCase = new ArrayList<TestSuite>();
 			Document doc = getDocumentOfFile(reportFilePath, resultFile);
 			List<TestSuite> testSuites = getTestSuite(doc);
 			for (TestSuite testSuite : testSuites) {	// test suite ll have graph details
 				List<TestCase> testCases = getTestCases(doc, testSuite.getName());
 				testSuite.setTestCases(testCases);
 				testSuiteWithTestCase.add(testSuite);
 			}
 			xmlReport.setTestSuites(testSuiteWithTestCase);
 			xmlReports.add(xmlReport);
 		}
 		return xmlReports;
 	}
 	
 	// unit and functional test report
 	public SureFireReport sureFireReports(String module) throws Exception {
 		
 		String type = "";
 		Map<String, String> reportDirWithTestSuitePath = new HashMap<String, String>(); // <file
 																						// -
 																						// testsuitePath,testcasePath>
 		if (UNIT.equals(testType)) {
 			String reportFilePath = baseDir.getAbsolutePath();
 			if (StringUtils.isNotEmpty(module)) {
 				reportFilePath = reportFilePath + File.separatorChar + module;
 			}
 			getUnitTestXmlFilesAndXpaths(reportFilePath, reportDirWithTestSuitePath);
 			
 		} else if (MANUAL.equals(testType)) {
 			String reportFilePath = baseDir.getAbsolutePath();
 			String manualTestReportDir = mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_MANUALTEST_RPT_DIR);
 			String reportPath = "";
 			if (StringUtils.isNotEmpty(manualTestReportDir)) {
 				reportPath = reportFilePath + manualTestReportDir;
 				SureFireReport sureFireReport = new SureFireReport();
 				List<AllTestSuite> allTestSuites = readAllTestSuites(reportPath);
 				
 				sureFireReport.setAllTestSuites(allTestSuites);
 				List<TestSuite> testSuites = readTestSuitesWithTestCases(reportPath);
 				sureFireReport.setTestSuites(testSuites);
 				return sureFireReport;
 			}
 		} else if (FUNCTIONAL.equals(testType)){
 			String reportFilePath = baseDir.getAbsolutePath();
 			String functionalTestDir = mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_FUNCTEST_RPT_DIR);
 			String unitTestSuitePath = mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_FUNCTEST_TESTSUITE_XPATH);
 			String unitTestCasePath = mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_FUNCTEST_TESTCASE_PATH);
 			String reportPath = "";
 			if (StringUtils.isNotEmpty(functionalTestDir)) {
 				reportPath = reportFilePath + functionalTestDir;
 			}
 			if (StringUtils.isNotEmpty(module)) {
 				reportFilePath = reportFilePath + File.separatorChar + module;
 			}
 			List<File> testResultFiles = getTestResultFilesAsList(reportPath);
 			for (File testResultFile : testResultFiles) {
 				reportDirWithTestSuitePath.put(testResultFile.getPath(), unitTestSuitePath + "," + unitTestCasePath);
 			}
 		} else if (COMPONENT.equals(testType)) {
 			String reportFilePath = baseDir.getAbsolutePath();
 			String componentTestDir = mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_COMPONENTTEST_RPT_DIR);
 			String componentTestSuitePath = mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_COMPONENTTEST_TESTSUITE_XPATH);
 			String componentTestCasePath = mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_COMPONENTTEST_TESTCASE_PATH);
 			String reportPath = "";
 			if (StringUtils.isNotEmpty(componentTestDir)) {
 				reportPath = reportFilePath + componentTestDir;
 			}
 			
 			List<File> testResultFiles = getTestResultFilesAsList(reportPath);
 			for (File testResultFile : testResultFiles) {
 				reportDirWithTestSuitePath.put(testResultFile.getPath(), componentTestSuitePath + "," + componentTestCasePath);
 			}
 		}
 		
 		SureFireReport sureFireReport = new SureFireReport();
 		
 		ArrayList<TestSuite> testSuiteWithTestCase = null;
 		ArrayList<AllTestSuite> allTestSuiteDetails = null;
 
 		ArrayList<TestSuite> jsTestSuiteWithTestCase = null;
 		ArrayList<AllTestSuite> allJsTestSuiteDetails = null;
 				
 		jsTestSuiteWithTestCase = new ArrayList<TestSuite>();
 		allJsTestSuiteDetails = new ArrayList<AllTestSuite>();
 		
 		// detailed information object
 		testSuiteWithTestCase = new ArrayList<TestSuite>();
 		// crisp information of the test
 		allTestSuiteDetails = new ArrayList<AllTestSuite>();
 
 		// Iterate over each file
 		// testsuite path and testcase path
 		for (Map.Entry entry : reportDirWithTestSuitePath.entrySet()) {
 			String mapKey = (String) entry.getKey(); // file
 			String mapValue = (String) entry.getValue(); // /testSuite,/testcase	
 			
 			if(mapValue.contains("#SEP#")) {
 				String[] split = mapValue.split("#SEP#");
 				type = split[1];
 				String[] split2 = mapValue.split("#");
 				mapValue = split2[0];
 			}
 			String[] testsuiteAndTestcasePath = mapValue.split(",");
 			File reportFile = new File(mapKey);
 			
 			String testSuitePath = testsuiteAndTestcasePath[0];
 			String testCasePath = testsuiteAndTestcasePath[1];
 			
 			Document doc = getDocumentOfFile(reportFile);
 			if (doc == null) {
 				// if doc is null, the file does not have any values (i.e) zero
 				// bytes.
 				continue;
 			}
 
 			List<TestSuite> testSuites = getTestSuite(doc, testSuitePath);
 			// crisp info
 			int totalTestSuites = 0;
 			int successTestSuites = 0;
 			int failureTestSuites = 0;
 			int errorTestSuites = 0;
 			
 			if(CollectionUtils.isNotEmpty(testSuites)) {
 				for (TestSuite testSuite : testSuites) { // test suite ll have graph details
 					List<TestCase> testCases = getTestCases(doc, testSuite.getName(), testSuitePath, testCasePath);
 					int tests = 0;
 					int failures = 0;
 					int errors = 0;
 					failures = getNoOfTstSuiteFailures();
 					errors = getNoOfTstSuiteErrors();
 					tests = getNoOfTstSuiteTests();
 					int success = 0;
 	
 					if (failures != 0 && errors == 0) {
 						if (failures > tests) {
 							success = failures - tests;
 						} else {
 							success = tests - failures;
 						}
 					} else if (failures == 0 && errors != 0) {
 						if (errors > tests) {
 							success = errors - tests;
 						} else {
 							success = tests - errors;
 						}
 					} else if (failures != 0 && errors != 0) {
 						int failTotal = failures + errors;
 						if (failTotal > tests) {
 							success = failTotal - tests;
 						} else {
 							success = tests - failTotal;
 						}
 					} else {
 						success = tests;
 					}
 	
 					totalTestSuites = totalTestSuites + tests;
 					failureTestSuites = failureTestSuites + failures;
 					errorTestSuites = errorTestSuites + errors;
 					successTestSuites = (int) (successTestSuites + success);
 					String rstValues = tests + "," + success + "," + failures + "," + errors;
 					
 					AllTestSuite allTestSuiteDetail = new AllTestSuite(testSuite.getName(), tests, success, failures, errors);
 					
 					
 					testSuite.setTestCases(testCases);
 					if(type.equalsIgnoreCase("js")) {
 						jsTestSuiteWithTestCase.add(testSuite);
 						allJsTestSuiteDetails.add(allTestSuiteDetail);
 					} else {
 						testSuiteWithTestCase.add(testSuite);
 						allTestSuiteDetails.add(allTestSuiteDetail);
 					}
 				}
 				
 				
 //				printDetailedObj(testSuiteWithTestCase);
 				// crisp info
 				if(StringUtils.isNotEmpty(type) && type.equalsIgnoreCase("js")) {
 					sureFireReport.setJsTestSuites(jsTestSuiteWithTestCase);
 					sureFireReport.setJsAllTestSuites(allJsTestSuiteDetails);
 				} else {
 					// detailed info
 					sureFireReport.setTestSuites(testSuiteWithTestCase);
 					sureFireReport.setAllTestSuites(allTestSuiteDetails);
 				}
 			}
 		}
 		
 		return sureFireReport;
 	}
 
 	private List<AllTestSuite> readAllTestSuites(String filePath)  {
 		List<AllTestSuite> excels = new ArrayList<AllTestSuite>();
 		try {
 			File testDir = new File(filePath);
 			StringBuilder sb = new StringBuilder(filePath);
 			if(testDir.isDirectory()) {
 				FilenameFilter filter = new PhrescoFileFilter("", XLSX);
 				File[] listFiles = testDir.listFiles(filter);
 				if (listFiles.length != 0) {
 					readXlsxData(excels, sb, listFiles);
 				} else {
 					readxlsFormatData(excels, testDir, sb);
 				}
 			}
 
 		} catch (Exception e) {
 			 e.printStackTrace();
 		}
 		return excels;
 	}
 
 	private void readxlsFormatData(List<AllTestSuite> excels, File testDir,
 			StringBuilder sb) throws FileNotFoundException, IOException,
 			UnknownHostException, PhrescoException {
 		Iterator<Row> rowIterator;
 		FilenameFilter filter = new PhrescoFileFilter("", XLS);
 		File[] listFiles = testDir.listFiles(filter);
 		for(File file : listFiles) {
 			if (file.isFile()) {
 				sb.append(File.separator);
 				sb.append(file.getName());
 			}
 		}
 		
 		FileInputStream myInput = new FileInputStream(sb.toString());
 		HSSFWorkbook myWorkBook = new HSSFWorkbook(myInput);
 		HSSFSheet mySheet = myWorkBook.getSheetAt(0);
 		rowIterator = mySheet.rowIterator();
 		
 		for (int i = 0; i <=2; i++) {
 			rowIterator.next();
 		}
 		while (rowIterator.hasNext()) {
 			Row next = rowIterator.next();
 			if (StringUtils.isNotEmpty(getValue(next.getCell(2))) && !getValue(next.getCell(2)).equalsIgnoreCase("Total")) {
 				AllTestSuite createObject = readTestSuite(next);
 				excels.add(createObject);
 			}
 		}
 	}
 
 	private void readXlsxData(List<AllTestSuite> excels, StringBuilder sb,
 			File[] listFiles) throws FileNotFoundException,
 			InvalidFormatException, IOException, UnknownHostException,
 			PhrescoException {
 		Iterator<Row> rowIterator;
 		for (File file : listFiles) {
 			if (file.isFile()) {
 				sb.append(File.separator);
 				sb.append(file.getName());
 			}
 		}
 		FileInputStream myInput = new FileInputStream(sb.toString());
 		OPCPackage opc=OPCPackage.open(myInput); 
 		XSSFWorkbook myWorkBook = new XSSFWorkbook(opc);
 		XSSFSheet mySheet = myWorkBook.getSheetAt(0);
 		rowIterator = mySheet.rowIterator();
 		
 		for (int i = 0; i <=2; i++) {
 			rowIterator.next();
 		}
 		while (rowIterator.hasNext()) {
 			Row next = rowIterator.next();
 			if (StringUtils.isNotEmpty(getValue(next.getCell(2))) && !getValue(next.getCell(2)).equalsIgnoreCase("Total")) {
 				AllTestSuite createObject = readTestSuite(next);
 				excels.add(createObject);
 			}
 		}
 	}
 
 	  private AllTestSuite readTestSuite(Row next) throws UnknownHostException, PhrescoException{
 		  AllTestSuite testSuite = new AllTestSuite();
 		  if(next.getCell(2) != null) {
 			  Cell cell = next.getCell(2);
 			  String value = getValue(cell);
 			  if(StringUtils.isNotEmpty(value)) {
 				  testSuite.setTestSuiteName(value);
 			  }
 		  }
 		  if(next.getCell(3)!=null){
 			  Cell cell = next.getCell(3);
 			  String value=getValue(cell);
 			  if(StringUtils.isNotEmpty(value)) {
 				  float pass=Float.parseFloat(value);
				  testSuite.setSuccess(pass);
 			  }
 		  }
 		  if(next.getCell(4)!=null){
 			  Cell cell = next.getCell(4);
 			  String value=getValue(cell);
 			  if(StringUtils.isNotEmpty(value)) {
 				  float fail=Float.parseFloat(value);
 				  testSuite.setFailure(fail);
 			  }
 		  }
 		  if(next.getCell(5)!=null){
 			  Cell cell = next.getCell(5);
 			  String value=getValue(cell);
 			  if(StringUtils.isNotEmpty(value)) {
 				  float notApp=Float.parseFloat(value);
 				  testSuite.setNotApplicable(notApp);
 			  }
 		  }
 		  if(next.getCell(6)!=null){
 			  Cell cell = next.getCell(6);
 			  String value=getValue(cell);
 			  if(StringUtils.isNotEmpty(value)) {
 				  float notExecuted=Float.parseFloat(value);
 				  testSuite.setNotExecuted(notExecuted);
 			  }
 		  }
 		  if(next.getCell(7)!=null){
 			  Cell cell = next.getCell(7);
 			  String value=getValue(cell);
 			  if(StringUtils.isNotEmpty(value)) {
 				  float blocked=Float.parseFloat(value);
 				  testSuite.setBlocked(blocked);
 			  }
 		  }
 		  if(next.getCell(8)!=null){
 			  Cell cell = next.getCell(8);
 			  String value=getValue(cell);
 			  if(StringUtils.isNotEmpty(value)) {
 				  float total=Float.parseFloat(value);
 				  testSuite.setTotal(total);
 			  }
 		  }
 		  if(next.getCell(9)!=null){
 			  Cell cell= next.getCell(9);
 			  String value= getValue(cell);
 			  if(StringUtils.isNotEmpty(value)) {
 				  float testCoverage=Float.parseFloat(value);
 				  testSuite.setTestCoverage(testCoverage);
 			  }
 		  }
 		  return testSuite;
 	  }
 
 	  private static String getValue(Cell cell) {
 		  if (Cell.CELL_TYPE_STRING == cell.getCellType()) {
 			  return cell.getStringCellValue();
 		  }
 
 		  if (Cell.CELL_TYPE_NUMERIC == cell.getCellType()) {
 			  return String.valueOf(cell.getNumericCellValue());
 		  }
 
 		  return null;
 	  }
 
 	  public List<TestSuite> readTestSuitesWithTestCases(String filePath)  {
 		List<TestSuite> excels = new ArrayList<TestSuite>();
 		Iterator<Row> rowIterator = null;
 		FilenameFilter filter = null;
 		File[] listFiles = null;
 		try {
 			File testDir = new File(filePath);
 			StringBuilder sb = new StringBuilder(filePath);
 			if(testDir.isDirectory()) {
 				filter = new PhrescoFileFilter("", XLSX);
 				listFiles = testDir.listFiles(filter);
 				if (listFiles.length != 0) {
 					for (File file : listFiles) {
 						if (file.isFile()) {
 							sb.append(File.separator);
 							sb.append(file.getName());
 						}
 					}
 					FileInputStream myInput = new FileInputStream(sb.toString());
 					OPCPackage opc=OPCPackage.open(myInput); 
 					XSSFWorkbook myWorkBook = new XSSFWorkbook(opc);
 					XSSFSheet mySheet = myWorkBook.getSheetAt(0);
 					
 					rowIterator = mySheet.rowIterator();
 					for (int i = 0; i <=2; i++) {
 						rowIterator.next();
 					}
 			        while (rowIterator.hasNext()) {
 			    		Row next = rowIterator.next();
 			    		if (StringUtils.isNotEmpty(getValue(next.getCell(2))) && !getValue(next.getCell(2)).equalsIgnoreCase("Total")) {
 			    			TestSuite createObject = createObject(next, filePath);
 			            	excels.add(createObject);
 			    		}
 			        }
 				} else {
 					filter = new PhrescoFileFilter("", XLS);
 				    listFiles = testDir.listFiles(filter);
 				    for(File file : listFiles) {
 				    	if (file.isFile()) {
 				    		sb.append(File.separator);
 				        	sb.append(file.getName());
 				    	}
 				    }
 				    FileInputStream myInput = new FileInputStream(sb.toString());
 				    HSSFWorkbook myWorkBook = new HSSFWorkbook(myInput);
 				    HSSFSheet mySheet = myWorkBook.getSheetAt(0);
 				    rowIterator = mySheet.rowIterator();
 				    for (int i = 0; i <=2; i++) {
 						rowIterator.next();
 					}
 				    while (rowIterator.hasNext()) {
 					Row next = rowIterator.next();
 					if (StringUtils.isNotEmpty(getValue(next.getCell(2))) && !getValue(next.getCell(2)).equalsIgnoreCase("Total")) {
 						TestSuite createObject = createObject(next, filePath);
 				    	excels.add(createObject);
 					}
 				}
 			}
 		}
 	} catch (Exception e) {
 	}
 	return excels;
   }
   
 	  private TestSuite createObject(Row next, String filePath) throws UnknownHostException, PhrescoException {
 		  TestSuite testSuite = new TestSuite();
 		  if(next.getCell(2) != null) {
 			  Cell cell = next.getCell(2);
 			  String value = getValue(cell);
 			  if(StringUtils.isNotEmpty(value)) {
 				  testSuite.setName(value);
 			  }
 		  }
 		  if(next.getCell(3)!=null){
 			  Cell cell = next.getCell(3);
 			  String value=getValue(cell);
 			  if(StringUtils.isNotEmpty(value)) {
 				  Float f= Float.parseFloat(value);
 				  int pass = Math.round(f);
 				  testSuite.setSuccess(pass);
 			  }
 		  }
 		  if(next.getCell(4)!=null){
 			  Cell cell = next.getCell(4);
 			  String value=getValue(cell);
 			  if(StringUtils.isNotEmpty(value)) {
 				  Float f= Float.parseFloat(value);
 				  int fail = Math.round(f);
 				  testSuite.setFailures(fail);
 			  }
 		  }
 		  if(next.getCell(5)!=null){
 			  Cell cell = next.getCell(5);
 			  String value=getValue(cell);
 			  if(StringUtils.isNotEmpty(value)) {
 				  Float f= Float.parseFloat(value);
 				  int notApp = Math.round(f);
 				  testSuite.setNotApplicable(notApp);
 			  }
 		  }
 		  if(next.getCell(6)!=null){
 			  Cell cell = next.getCell(6);
 			  String value=getValue(cell);
 			  if(StringUtils.isNotEmpty(value)) {
 				  Float f= Float.parseFloat(value);
 				  int notExecuted = Math.round(f);
 				  testSuite.setNotExecuted(notExecuted);
 			  }
 		  }
 		  if(next.getCell(7)!=null){
 			  Cell cell = next.getCell(7);
 			  String value=getValue(cell);
 			  if(StringUtils.isNotEmpty(value)) {
 				  Float f= Float.parseFloat(value);
 				  int blocked = Math.round(f);
 				  testSuite.setBlocked(blocked);
 			  }
 		  }
 		  if(next.getCell(8)!=null){
 			  Cell cell = next.getCell(8);
 			  String value=getValue(cell);
 			  if(StringUtils.isNotEmpty(value)) {
 				  Float f= Float.parseFloat(value);
 				  int total = Math.round(f);
 				  testSuite.setTotal(total);
 			  }
 		  }
 		  if(next.getCell(9)!=null){
 			  Cell cell=next.getCell(9);
 			  String value=getValue(cell);
 			  if(StringUtils.isNotEmpty(value)) {
 				  float testCoverage=Float.parseFloat(value);
 				  testSuite.setTestCoverage(testCoverage);
 			  }
 		  }
 		  List<TestCase> readTestCase = readTestCase(filePath, testSuite.getName());
 		  testSuite.setTestCases(readTestCase);
 		  return testSuite;
 	  }
 
 	  private List<TestCase> readTestCase(String filePath,String fileName) throws PhrescoException {
 		  List<TestCase> testCases = new ArrayList<TestCase>();
 		  try {
 			  File testDir = new File(filePath);
 			  StringBuilder sb = new StringBuilder(filePath);
 			  if(testDir.isDirectory()) {
 				  FilenameFilter filter = new PhrescoFileFilter("", XLSX);
 				  File[] listFiles = testDir.listFiles(filter);
 				  if (listFiles.length != 0) {
 					  for (File file : listFiles) {
 						  if (file.isFile()) {
 							  sb.append(File.separator);
 							  sb.append(file.getName());
 						  }
 					  }
 					  FileInputStream myInput = new FileInputStream(sb.toString());
 					  OPCPackage opc=OPCPackage.open(myInput); 
 
 					  XSSFWorkbook myWorkBook = new XSSFWorkbook(opc);
 					  int numberOfSheets = myWorkBook.getNumberOfSheets();
 					  for (int j = 0; j < numberOfSheets; j++) {
 						  XSSFSheet mySheet = myWorkBook.getSheetAt(j);
 						  if(mySheet.getSheetName().equals(fileName)) {
 							  Iterator<Row> rowIterator = mySheet.rowIterator();
 							  for (int i = 0; i <=23; i++) {
 								  rowIterator.next();
 							  }
 							  while (rowIterator.hasNext()) {
 								  Row next = rowIterator.next();
 								  if (StringUtils.isNotEmpty(getValue(next.getCell(1)))) {
 									  TestCase createObject = readTest(next);
 									  testCases.add(createObject);
 								  }
 
 							  }
 						  }
 					  }
 				  } else {
 					  FilenameFilter filter1 = new PhrescoFileFilter("", XLS);
 					  File[] listFiles1 = testDir.listFiles(filter1);
 					  for(File file2 : listFiles1) {
 						  if (file2.isFile()) {
 							  sb.append(File.separator);
 							  sb.append(file2.getName());
 						  }
 					  }
 					  FileInputStream myInput = new FileInputStream(sb.toString());
 					  HSSFWorkbook myWorkBook = new HSSFWorkbook(myInput);
 					  int numberOfSheets = myWorkBook.getNumberOfSheets();
 					  for (int j = 0; j < numberOfSheets; j++) {
 						  HSSFSheet mySheet = myWorkBook.getSheetAt(j);
 						  if(mySheet.getSheetName().equals(fileName)) {
 							  Iterator<Row> rowIterator = mySheet.rowIterator();
 							  for (int i = 0; i <=24; i++) {
 								  rowIterator.next();
 							  }
 							  while (rowIterator.hasNext()) {
 								  Row next = rowIterator.next();
 								  if (StringUtils.isNotEmpty(getValue(next.getCell(1)))) {
 									  TestCase createObject = readTest(next);
 									  testCases.add(createObject);
 								  }
 							  }
 
 						  }
 					  }
 				  }
 			  }
 		  } catch (Exception e) {
 		  }
 		  return testCases;
 	  }
 
 	  private TestCase readTest(Row next){
 		  TestCase testcase = new TestCase();
 		  if(next.getCell(1) != null) {
 			  Cell cell = next.getCell(1);
 			  String value = getValue(cell);
 			  if(StringUtils.isNotEmpty(value)) {
 				  testcase.setFeatureId(value);
 			  }
 		  }
 		  if(next.getCell(3)!=null){
 			  Cell cell = next.getCell(3);
 			  String value=getValue(cell);
 			  if(StringUtils.isNotEmpty(value)) {
 				  testcase.setTestCaseId(value);
 			  }
 		  }
 
 		  if(next.getCell(8)!=null){
 			  Cell cell=next.getCell(8);
 			  String value=getValue(cell);
 			  if(StringUtils.isNotEmpty(value)) {
 				  testcase.setExpectedResult(value);
 			  }
 		  }
 		  if(next.getCell(9)!=null){
 			  Cell cell=next.getCell(9);
 			  String value=getValue(cell);
 			  if(StringUtils.isNotEmpty(value)) {
 				  testcase.setActualResult(value);
 			  }
 		  }
 		  if(next.getCell(10)!=null){
 			  Cell cell=next.getCell(10);
 			  String value=getValue(cell);
 			  if(StringUtils.isNotEmpty(value)) {
 				  testcase.setStatus(value);
 			  }
 		  }
 
 		  if(next.getCell(13)!=null){
 			  Cell cell=next.getCell(13);
 			  String value=getValue(cell);
 			  if(StringUtils.isNotEmpty(value)) {
 				  testcase.setBugComment(value);
 			  }
 		  }
 
 		  return testcase;
 	  }
     
 	private void getUnitTestXmlFilesAndXpaths(String reportFilePath,
 			Map<String, String> reportDirWithTestSuitePath) {
 		String unitTestDir = mavenProject.getProperties().getProperty(PHRESCO_UNIT_TEST);
 		// when it is having multiple values
 		if (StringUtils.isEmpty(unitTestDir)) {
 			unitTestDir = mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_UNITTEST_RPT_DIR);
 			String unitTestSuitePath = mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_UNITTEST_TESTSUITE_XPATH);
 			String unitTestCasePath = mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_UNITTEST_TESTCASE_PATH);
 			String reportPath = reportFilePath + unitTestDir;
 			List<File> testResultFiles = getTestResultFilesAsList(reportPath);
 			for (File testResultFile : testResultFiles) {
 				reportDirWithTestSuitePath.put(testResultFile.getPath(), unitTestSuitePath + "," + unitTestCasePath);
 			}
 		} else {
 			List<String> unitTestTechs = Arrays.asList(unitTestDir.split(","));
 			for (String unitTestTech : unitTestTechs) {
 				unitTestDir = mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_UNITTEST_RPT_DIR_START + unitTestTech + Constants.POM_PROP_KEY_UNITTEST_RPT_DIR_END);
 				String unitTestSuitePath = mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_UNITTEST_RPT_DIR_START + unitTestTech + Constants.POM_PROP_KEY_UNITTEST_TESTSUITE_XPATH_END);
 				String unitTestCasePath = mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_UNITTEST_RPT_DIR_START + unitTestTech + Constants.POM_PROP_KEY_UNITTEST_TESTCASE_PATH_END);
 				if (StringUtils.isNotEmpty(unitTestDir)) {
 					String reportPath = reportFilePath + unitTestDir;
 					List<File> testResultFiles = getTestResultFilesAsList(reportPath);
 					for (File testResultFile : testResultFiles) {
 						reportDirWithTestSuitePath.put(testResultFile.getPath(), unitTestSuitePath + "," + unitTestCasePath + "#SEP#" + unitTestTech);
 					}
 				}
 			}
 		}
 	}
 	
     private List<TestSuite> getTestSuite(Document doc) throws TransformerException, PhrescoException {
         try {
             String testSuitePath = null;
     		if (UNIT.equals(testType)) {
     			testSuitePath = mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_UNITTEST_TESTSUITE_XPATH);
     		} else {
     			testSuitePath = mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_FUNCTEST_TESTSUITE_XPATH);
     		}
             NodeList nodelist = org.apache.xpath.XPathAPI.selectNodeList(doc, XPATH_MULTIPLE_TESTSUITE);
             if (nodelist.getLength() == 0) {
                 nodelist = org.apache.xpath.XPathAPI.selectNodeList(doc, testSuitePath);
             }
 
             List<TestSuite> testSuites = new ArrayList<TestSuite>(2);
             TestSuite testSuite = null;
 
             for (int i = 0; i < nodelist.getLength(); i++) {
                 testSuite =  new TestSuite();
                 Node node = nodelist.item(i);
                 NamedNodeMap nameNodeMap = node.getAttributes();
 
                 for (int k = 0; k < nameNodeMap.getLength(); k++){
                     Node attribute = nameNodeMap.item(k);
                     String attributeName = attribute.getNodeName();
                     String attributeValue = attribute.getNodeValue();
 
                     if (ATTR_ASSERTIONS.equals(attributeName)) {
                         testSuite.setAssertions(attributeValue);
                     } else if (ATTR_ERRORS.equals(attributeName)) {
                         testSuite.setErrors(Integer.parseInt(attributeValue));
                     } else if (ATTR_FAILURES.equals(attributeName)) {
                         testSuite.setFailures(Integer.parseInt(attributeValue));
                     } else if (ATTR_FILE.equals(attributeName)) {
                         testSuite.setFile(attributeValue);
                     } else if (ATTR_NAME.equals(attributeName)) {
                         testSuite.setName(attributeValue);
                     } else if (ATTR_TESTS.equals(attributeName)) {
                         testSuite.setTests(Float.parseFloat(attributeValue));
                     } else if (ATTR_TIME.equals(attributeName)) {
                         testSuite.setTime(attributeValue);
                     }
                 }
                 testSuites.add(testSuite);
             }
             return testSuites;
         } catch (Exception e) {
             throw new PhrescoException(e);
         }
     }
     
 	private List<TestSuite> getTestSuite(Document doc, String testSuitePath) throws TransformerException,
 			PhrescoException {
 		try {
 			NodeList nodelist = org.apache.xpath.XPathAPI.selectNodeList(doc, XPATH_MULTIPLE_TESTSUITE);
 			if (nodelist.getLength() == 0) {
 				nodelist = org.apache.xpath.XPathAPI.selectNodeList(doc, testSuitePath);
 			}
 
 			List<TestSuite> testSuites = new ArrayList<TestSuite>(2);
 			TestSuite testSuite = null;
 
 			for (int i = 0; i < nodelist.getLength(); i++) {
 				testSuite = new TestSuite();
 				Node node = nodelist.item(i);
 				NamedNodeMap nameNodeMap = node.getAttributes();
 
 				for (int k = 0; k < nameNodeMap.getLength(); k++) {
 					Node attribute = nameNodeMap.item(k);
 					String attributeName = attribute.getNodeName();
 					String attributeValue = attribute.getNodeValue();
 
 					if (ATTR_ASSERTIONS.equals(attributeName)) {
 						testSuite.setAssertions(attributeValue);
 					} else if (ATTR_ERRORS.equals(attributeName)) {
 						testSuite.setErrors(Integer.parseInt(attributeValue));
 					} else if (ATTR_FAILURES.equals(attributeName)) {
 						testSuite.setFailures(Integer.parseInt(attributeValue));
 					} else if (ATTR_FILE.equals(attributeName)) {
 						testSuite.setFile(attributeValue);
 					} else if (ATTR_NAME.equals(attributeName)) {
 						testSuite.setName(attributeValue);
 					} else if (ATTR_TESTS.equals(attributeName)) {
 						testSuite.setTests(Float.parseFloat(attributeValue));
 					} else if (ATTR_TIME.equals(attributeName)) {
 						testSuite.setTime(attributeValue);
 					}
 				}
 				testSuites.add(testSuite);
 			}
 			return testSuites;
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 
     private List<TestCase> getTestCases(Document doc, String testSuiteName) throws TransformerException, PhrescoException {
         try {
             String testCasePath = null;
             String testSuitePath = null;
     		if (UNIT.equals(testType)) {
                 testSuitePath = mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_UNITTEST_TESTSUITE_XPATH);
                 testCasePath = mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_UNITTEST_TESTCASE_PATH);
     		} else {
                 testSuitePath = mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_FUNCTEST_TESTSUITE_XPATH);
                 testCasePath = mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_FUNCTEST_TESTCASE_PATH);
     		}
     		
 //    		log.info("testSuitePath " + testSuitePath);
 //    		log.info("testCasePath " + testCasePath);
             StringBuilder sb = new StringBuilder(); //testsuites/testsuite[@name='yyy']/testcase
             //sb.append(XPATH_SINGLE_TESTSUITE);
             sb.append(testSuitePath);
             sb.append(NAME_FILTER_PREFIX);
             sb.append(testSuiteName);
             sb.append(NAME_FILTER_SUFIX);
             sb.append(testCasePath);
             //sb.append(XPATH_TESTCASE);
 
             NodeList nodelist = org.apache.xpath.XPathAPI.selectNodeList(doc, sb.toString());
             List<TestCase> testCases = new ArrayList<TestCase>();
 
             int failureTestCases = 0;
             int errorTestCases = 0;
 
             for (int i = 0; i < nodelist.getLength(); i++) {
                 Node node = nodelist.item(i);
                 NodeList childNodes = node.getChildNodes();
                 NamedNodeMap nameNodeMap = node.getAttributes();
                 TestCase testCase = new TestCase();
 
                 if (childNodes != null && childNodes.getLength() > 0) {
 
                     for (int j = 0; j < childNodes.getLength(); j++) {
                         Node childNode = childNodes.item(j);
 
                         if (ELEMENT_FAILURE.equals(childNode.getNodeName())) {
                         	failureTestCases++;
                             TestCaseFailure failure = getFailure(childNode);
                             if (failure != null) {
                                 testCase.setTestCaseFailure(failure);
                             } 
                         }
 
                         if (ELEMENT_ERROR.equals(childNode.getNodeName())) {
                         	errorTestCases++;
                             TestCaseError error = getError(childNode);
                             if (error != null) {
                                 testCase.setTestCaseError(error);
                             }
                         }
                     }
                 }
 
                 for (int k = 0; k < nameNodeMap.getLength(); k++){
                     Node attribute = nameNodeMap.item(k);
                     String attributeName = attribute.getNodeName();
                     String attributeValue = attribute.getNodeValue();
                     if (ATTR_NAME.equals(attributeName)) {
                         testCase.setName(attributeValue);
                     } else if (ATTR_CLASS.equals(attributeName) || ATTR_CLASSNAME.equals(attributeName)) {
                         testCase.setTestClass(attributeValue);
                     } else if (ATTR_FILE.equals(attributeName)) {
                         testCase.setFile(attributeValue);
                     } else if (ATTR_LINE.equals(attributeName)) {
                         testCase.setLine(Float.parseFloat(attributeValue));
                     } else if (ATTR_ASSERTIONS.equals(attributeName)) {
                         testCase.setAssertions(Float.parseFloat(attributeValue));
                     } else if (ATTR_TIME.equals(attributeName)) {
                         testCase.setTime(attributeValue);
                     } 
                 }
                 testCases.add(testCase);
             }
             
             setNoOfTstSuiteFailures(failureTestCases);
             setNoOfTstSuiteErrors(errorTestCases);
             setNoOfTstSuiteTests(nodelist.getLength());
             return testCases;
         } catch (Exception e) {
             throw new PhrescoException(e);
         }
     }
     
 	private List<TestCase> getTestCases(Document doc, String testSuiteName, String testSuitePath, String testCasePath)
 			throws TransformerException, PhrescoException {
 		try {
 //			log.info("testSuitePath " + testSuitePath);
 //			log.info("testCasePath " + testCasePath);
 			StringBuilder sb = new StringBuilder(); // testsuites/testsuite[@name='yyy']/testcase
 			// sb.append(XPATH_SINGLE_TESTSUITE);
 			sb.append(testSuitePath);
 			sb.append(NAME_FILTER_PREFIX);
 			sb.append(testSuiteName);
 			sb.append(NAME_FILTER_SUFIX);
 			sb.append(testCasePath);
 			// sb.append(XPATH_TESTCASE);
 
 			NodeList nodelist = org.apache.xpath.XPathAPI.selectNodeList(doc, sb.toString());
 			List<TestCase> testCases = new ArrayList<TestCase>();
 
 			int failureTestCases = 0;
 			int errorTestCases = 0;
 
 			for (int i = 0; i < nodelist.getLength(); i++) {
 				Node node = nodelist.item(i);
 				NodeList childNodes = node.getChildNodes();
 				NamedNodeMap nameNodeMap = node.getAttributes();
 				TestCase testCase = new TestCase();
 				
 				//For Error Failure
 				if (childNodes != null && childNodes.getLength() > 0) {
 
 					for (int j = 0; j < childNodes.getLength(); j++) {
 						Node childNode = childNodes.item(j);
 
 						if (ELEMENT_FAILURE.equals(childNode.getNodeName())) {
 							failureTestCases++;
 							TestCaseFailure failure = getFailure(childNode);
 							if (failure != null) {
 								testCase.setTestCaseFailure(failure);
 							}
 						}
 
 						if (ELEMENT_ERROR.equals(childNode.getNodeName())) {
 							errorTestCases++;
 							TestCaseError error = getError(childNode);
 							if (error != null) {
 								testCase.setTestCaseError(error);
 							}
 						}
 					}
 				}
 
 				for (int k = 0; k < nameNodeMap.getLength(); k++) {
 					Node attribute = nameNodeMap.item(k);
 					String attributeName = attribute.getNodeName();
 					String attributeValue = attribute.getNodeValue();
 					if (ATTR_NAME.equals(attributeName)) {
 						testCase.setName(attributeValue);
 					} else if (ATTR_CLASS.equals(attributeName) || ATTR_CLASSNAME.equals(attributeName)) {
 						testCase.setTestClass(attributeValue);
 					} else if (ATTR_FILE.equals(attributeName)) {
 						testCase.setFile(attributeValue);
 					} else if (ATTR_LINE.equals(attributeName)) {
 						testCase.setLine(Float.parseFloat(attributeValue));
 					} else if (ATTR_ASSERTIONS.equals(attributeName)) {
 						testCase.setAssertions(Float.parseFloat(attributeValue));
 					} else if (ATTR_TIME.equals(attributeName)) {
 						testCase.setTime(attributeValue);
 					}
 				}
 				testCases.add(testCase);
 			}
 
 			setNoOfTstSuiteFailures(failureTestCases);
 			setNoOfTstSuiteErrors(errorTestCases);
 			setNoOfTstSuiteTests(nodelist.getLength());
 			return testCases;
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
     
     private TestCaseFailure getFailure(Node failureNode) throws TransformerException {
         TestCaseFailure failure = new TestCaseFailure();
         try {
             failure.setDescription(failureNode.getTextContent());
             failure.setFailureType(REQ_TITLE_EXCEPTION);
             NamedNodeMap nameNodeMap = failureNode.getAttributes();
 
             if (nameNodeMap != null && nameNodeMap.getLength() > 0) {
                 for (int k = 0; k < nameNodeMap.getLength(); k++){
                     Node attribute = nameNodeMap.item(k);
                     String attributeName = attribute.getNodeName();
                     String attributeValue = attribute.getNodeValue();
 
                     if (ATTR_TYPE.equals(attributeName)) {
                         failure.setFailureType(attributeValue);
                     }
                 }
             }
         } catch (Exception e) {
         	e.printStackTrace();
         }
         return failure;
     }
 
     private TestCaseError getError(Node errorNode) throws TransformerException {
         TestCaseError tcError = new TestCaseError();
         try {
             tcError.setDescription(errorNode.getTextContent());
             tcError.setErrorType(REQ_TITLE_ERROR);
             NamedNodeMap nameNodeMap = errorNode.getAttributes();
 
             if (nameNodeMap != null && nameNodeMap.getLength() > 0) {
                 for (int k = 0; k < nameNodeMap.getLength(); k++){
                     Node attribute = nameNodeMap.item(k);
                     String attributeName = attribute.getNodeName();
                     String attributeValue = attribute.getNodeValue();
 
                     if (ATTR_TYPE.equals(attributeName)) {
                         tcError.setErrorType(attributeValue);
                     }
                 }
             }
         } catch (Exception e) {
         	e.printStackTrace();
         }
         return tcError;
     }
     
 	public JmeterReport getPerformanceReport(Document document, String fileName, String deviceId) throws Exception {  // deviceid is the tag name for android
 		JmeterReport jmeterReport = new JmeterReport();
 		List<PerformanceTestResult> performanceTestResultOfFile = new ArrayList<PerformanceTestResult>();
 		String xpath = "/*/*";	// For other technologies
 		String device = "*";
 		if(StringUtils.isNotEmpty(deviceId)) {
 			device = "deviceInfo[@id='" + deviceId + "']";
 		}
 
 		if(showDeviceReport) {
 			xpath = "/*/" + device + "/*";
 		}
 		NodeList nodeList = org.apache.xpath.XPathAPI.selectNodeList(document, xpath);
 		Map<String, PerformanceTestResult> results = new LinkedHashMap<String, PerformanceTestResult>(100);
 		double maxTs = 0;
 		double minTs = 0;
 		int lastTime = 0;
 		int noOfSamples = nodeList.getLength();
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			Node node = nodeList.item(i);
 			NamedNodeMap nameNodeMap = node.getAttributes();
 			Node timeAttr = nameNodeMap.getNamedItem(ATTR_JM_TIME);
 			int time = Integer.parseInt(timeAttr.getNodeValue());
 			Node bytesAttr = nameNodeMap.getNamedItem(ATTR_JM_BYTES);
 			int bytes = Integer.parseInt(bytesAttr.getNodeValue());
 			Node successFlagAttr = nameNodeMap.getNamedItem(ATTR_JM_SUCCESS_FLAG);
 			boolean success = Boolean.parseBoolean(successFlagAttr.getNodeValue()) ? true : false ;
 			Node labelAttr = nameNodeMap.getNamedItem(ATTR_JM_LABEL);
 			String label = labelAttr.getNodeValue();
 			Node timeStampAttr = nameNodeMap.getNamedItem(ATTR_JM_TIMESTAMP);
 			double timeStamp = Long.parseLong(timeStampAttr.getNodeValue());
 			boolean firstEntry = false;
 
 			PerformanceTestResult performanceTestResult = results.get(label);
 			if (performanceTestResult == null) {
 				performanceTestResult = new PerformanceTestResult();
 				firstEntry = true;
 			} else {
 				firstEntry = false;
 			}
 
 			performanceTestResult.setLabel(label.trim());
 			performanceTestResult.setNoOfSamples(performanceTestResult.getNoOfSamples() + 1);
 			performanceTestResult.getTimes().add(time);
 			performanceTestResult.setTotalTime(performanceTestResult.getTotalTime() + time);
 			performanceTestResult.setTotalBytes(performanceTestResult.getTotalBytes() + bytes);
 
 			if (time < performanceTestResult.getMin() || firstEntry) {
 				performanceTestResult.setMin(time);
 			}
 
 			if (time > performanceTestResult.getMax()) {
 				performanceTestResult.setMax(time);
 			}
 
 			// Error calculation
 			if (!success) {
 				performanceTestResult.setErr(performanceTestResult.getErr() + 1);
 			}
 
 			// Throughput calculation
 
 			if (timeStamp >= performanceTestResult.getMaxTs()) {
 				performanceTestResult.setMaxTs(timeStamp);
 				performanceTestResult.setLastTime(time);
 			}
 
 			if(i == 0 || (performanceTestResult.getMaxTs() > maxTs)) {
 				maxTs = performanceTestResult.getMaxTs();
 				lastTime = performanceTestResult.getLastTime();
 			}
 
 			if (timeStamp < performanceTestResult.getMinTs() || firstEntry) {
 				performanceTestResult.setMinTs(timeStamp);
 			}
 
 			if(i == 0 ) {
 				minTs = performanceTestResult.getMinTs();
 			} else if(performanceTestResult.getMinTs() < minTs) {
 				minTs = performanceTestResult.getMinTs();
 			}
 
 			Double calThroughPut = new Double(performanceTestResult.getNoOfSamples());
 			double timeSpan = performanceTestResult.getMaxTs() + performanceTestResult.getLastTime() - performanceTestResult.getMinTs();
 			if (timeSpan > 0) {
 				calThroughPut = calThroughPut / timeSpan;
 			} else {
 				calThroughPut=0.0;
 			}
 			double throughPut = calThroughPut * 1000;
 
 			performanceTestResult.setThroughPut(throughPut);
 
 			results.put(label, performanceTestResult);
 		}
 		
 		// Total Throughput calculation
 		double totalThroughput;
 		double timeSpan = ((maxTs + lastTime) - minTs);
 		if (timeSpan > 0) {
 			totalThroughput = (noOfSamples / timeSpan) * 1000;
 		} else {
 			totalThroughput = 0.0;
 		}
 		double stdDev = setStdDevToResults(results);
 		
 		// Getting all performance result objects
 		// calculating total values
 		int totalValue = results.keySet().size();
 		int NoOfSample = 0; 
 		double avg = 0; 
   		int min = 0;
   		int max = 0;
   		double StdDev = 0;
   		int Err = 0;
   		double KbPerSec = 0;
   		double sumOfBytes = 0;
   		int i = 1;
   		
 		for (String key : results.keySet()) {
 			PerformanceTestResult performanceTestResult = results.get(key);
 			performanceTestResultOfFile.add(performanceTestResult);
 			
 			// calculating min,max, avgbytes, kbsec
         	NoOfSample = NoOfSample + performanceTestResult.getNoOfSamples();
         	avg = avg + performanceTestResult.getAvg();
         	if (i == 1) {
         		min = performanceTestResult.getMin();
         		max = performanceTestResult.getMax();
         	}
         	if (i != 1 && performanceTestResult.getMin() < min) {
         		min = performanceTestResult.getMin();
         	}
         	if (i != 1 && performanceTestResult.getMax() > max) {
         		max = performanceTestResult.getMax();
         	}
         	StdDev = StdDev + performanceTestResult.getStdDev();
         	Err = Err + performanceTestResult.getErr();
         	sumOfBytes = sumOfBytes + performanceTestResult.getAvgBytes();
         	
         	i++;
 		}
 		// Calculation of avg bytes of a file
       	double avgBytes = sumOfBytes / totalValue;
       	KbPerSec = (avgBytes / 1024) * totalThroughput;
 		
 		// setting performance file name and list objects
 		jmeterReport.setFileName(fileName);
 		jmeterReport.setJmeterTestResult(performanceTestResultOfFile);
 		jmeterReport.setTotalThroughput(roundFloat(2,totalThroughput)+"");
 		jmeterReport.setTotalStdDev(roundFloat(2,stdDev)+"");
 		jmeterReport.setTotalNoOfSample(NoOfSample+"");
 		jmeterReport.setTotalAvg((avg/totalValue)+"");
 		jmeterReport.setMin(min+"");
 		jmeterReport.setMax(max+"");
 		jmeterReport.setTotalErr((Err/totalValue)+"");
 		jmeterReport.setTotalAvgBytes(avgBytes+"");
 		jmeterReport.setTotalKbPerSec(roundFloat(2,KbPerSec)+"");
 		
 		return jmeterReport;
 	}
 
 	private static double setStdDevToResults(Map<String, PerformanceTestResult> results) {
 		Set<String> keySet = results.keySet();
 		long xBar = 0;  		//XBar Calculation
 		long sumOfTime = 0;
 		int totalSamples = 0;
 		double sumMean = 0;
 		List<Integer> allTimes = new ArrayList<Integer>();
 		for (String key : keySet) {
 			PerformanceTestResult performanceTestResult = results.get(key);
 			// calculation of average time
 			double avg = performanceTestResult.getTotalTime() / performanceTestResult.getNoOfSamples();
 			sumOfTime = sumOfTime + performanceTestResult.getTotalTime();
 			totalSamples = totalSamples + performanceTestResult.getNoOfSamples();
 			performanceTestResult.setAvg(avg);
 
 			// calculation of average bytes
 			double avgBytes = (double) performanceTestResult.getTotalBytes() / performanceTestResult.getNoOfSamples();
 			performanceTestResult.setAvgBytes(Math.round(avgBytes));
 			// KB/Sec calculation
 			Double calKbPerSec = new Double(performanceTestResult.getThroughPut());
 			calKbPerSec = calKbPerSec * (((double) avgBytes) / 1024)   ;
 			performanceTestResult.setKbPerSec(calKbPerSec);
 
 			// Std.Dev calculation
 			List<Integer> times = performanceTestResult.getTimes();
 			allTimes.addAll(times);
 			Double totalMean = new Double(0);
 			for (Integer time : times) {
 				totalMean += Math.pow(time - avg, 2);
 			}
 			performanceTestResult.setStdDev((float) Math.sqrt(totalMean / performanceTestResult.getNoOfSamples()));
 
 			performanceTestResult.setErr((performanceTestResult.getErr() / performanceTestResult.getNoOfSamples()) * 100);
 		}
 
 		//Total Std.Dev calculation
 		xBar = sumOfTime / totalSamples;
 		for (Integer time : allTimes) {
 			sumMean += Math.pow(time - xBar, 2);
 		}
 		double stdDev = Math.sqrt(sumMean / totalSamples);
 		return stdDev;
 	}
 	
     private List<TestResult> getLoadTestResult(Document doc) throws TransformerException, PhrescoException, ParserConfigurationException, SAXException, IOException {
     	 List<TestResult> testResults = new ArrayList<TestResult>(2);
 	     try {
 	         NodeList nodeList = org.apache.xpath.XPathAPI.selectNodeList(doc, XPATH_TEST_RESULT);
 	         TestResult testResult = null;
 	         for (int i = 0; i < nodeList.getLength(); i++) {
 	             testResult =  new TestResult();
 	             Node node = nodeList.item(i);
 	             //              NodeList childNodes = node.getChildNodes();
 	             NamedNodeMap nameNodeMap = node.getAttributes();
 	
 	             for (int k = 0; k < nameNodeMap.getLength(); k++) {
 	                 Node attribute = nameNodeMap.item(k);
 	                 String attributeName = attribute.getNodeName();
 	                 String attributeValue = attribute.getNodeValue();
 	
 	                 if (ATTR_JM_TIME.equals(attributeName)) {
 	                     testResult.setTime(Integer.parseInt(attributeValue));
 	                 } else if (ATTR_JM_LATENCY_TIME.equals(attributeName)) {
 	                     testResult.setLatencyTime(Integer.parseInt(attributeValue));
 	                 } else if (ATTR_JM_TIMESTAMP.equals(attributeName)) {
 	                     Date date = new Date(Long.parseLong(attributeValue));
 	                     DateFormat format = new SimpleDateFormat(DATE_TIME_FORMAT);
 	                     String strDate = format.format(date);
 	                     testResult.setTimeStamp(strDate);
 	                 } else if (ATTR_JM_SUCCESS_FLAG.equals(attributeName)) {
 	                     testResult.setSuccess(Boolean.parseBoolean(attributeValue));
 	                 } else if (ATTR_JM_LABEL.equals(attributeName)) {
 	                     testResult.setLabel(attributeValue);
 	                 } else if (ATTR_JM_THREAD_NAME.equals(attributeName)) {
 	                     testResult.setThreadName(attributeValue);
 	                 }
 	             }
 	             testResults.add(testResult);
 	         }
 	     } catch (Exception e) {
 	    	 e.printStackTrace();
 	     }
 	     return testResults;
     }
     
 	private Document getDocumentOfFile(File reportFile) {
 		Document doc = null;
 		InputStream fis = null;
 		DocumentBuilder builder = null;
 		try {
 			fis = new FileInputStream(reportFile); // here should be new
 													// File(path + "/" +
 													// selectedTestResultFileName);
 			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
 			domFactory.setNamespaceAware(false);
 			builder = domFactory.newDocumentBuilder();
 			doc = builder.parse(fis);
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (fis != null) {
 				try {
 					fis.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return doc;
 	}
 
     private Document getDocumentOfFile(String path, String fileName) {
     	Document doc = null;
         InputStream fis = null;
         DocumentBuilder builder = null;
         try {
             fis = new FileInputStream(new File(path + "/" + fileName)); // here should be new File(path + "/" + selectedTestResultFileName);
             DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
             domFactory.setNamespaceAware(false);
             builder = domFactory.newDocumentBuilder();
             doc = builder.parse(fis);
         } catch (Exception e) {
         	e.printStackTrace();
 		} finally {
             if(fis != null) {
                 try {
                     fis.close();
                 } catch (IOException e) {
                 	e.printStackTrace();
                 }
             }
         }
 		return doc;
     }
     
     private String getTestManualResultFiles(String path) {
         File testDir = new File(path);
         StringBuilder sb = new StringBuilder(path);
         if(testDir.isDirectory()){
             FilenameFilter filter = new PhrescoFileFilter("", XLSX);
             File[] listFiles = testDir.listFiles(filter);
             for (File file : listFiles) {
                 if (file.isFile()) {
                 	sb.append(File.separator);
                 	sb.append(file.getName());
                 }
             }
         }
         return sb.toString();
     }
    
     private List<String> getTestResultFiles(String path) {
         File testDir = new File(path);
         List<String> testResultFileNames = new ArrayList<String>();
         if(testDir.isDirectory()){
             FilenameFilter filter = new PhrescoFileFilter("", XML);
             File[] listFiles = testDir.listFiles(filter);
             for (File file : listFiles) {
                 if (file.isFile()) {
                     testResultFileNames.add(file.getName());
                 }
             }
         }
         return testResultFileNames;
     }
     
     // to get performance extension tag value from pom
     private List<String> getTestResultFiles(String path, String extension) {
         File testDir = new File(path);
         List<String> testResultFileNames = new ArrayList<String>();
         if(testDir.isDirectory()){
             FilenameFilter filter = new PhrescoFileFilter("", extension);
             File[] listFiles = testDir.listFiles(filter);
             for (File file : listFiles) {
                 if (file.isFile()) {
                     testResultFileNames.add(file.getName());
                 }
             }
         }
         return testResultFileNames;
     }
 
 	private List<File> getTestResultFilesAsList(String path) {
 		File testDir = new File(path);
 		List<File> testResultFileNames = new ArrayList<File>();
 		if (testDir.isDirectory()) {
 			FilenameFilter filter = new PhrescoFileFilter("", XML);
 			File[] listFiles = testDir.listFiles(filter);
 			for (File file : listFiles) {
 				if (file.isFile()) {
 					testResultFileNames.add(file);
 				}
 			}
 		}
 		return testResultFileNames;
 	}
 	
 	// to get load extension tag value from pom
 	private List<File> getResultFileExtension(String path, String extension) {
 		File testDir = new File(path);
 		List<File> testResultFileNames = new ArrayList<File>();
 		if (testDir.isDirectory()) {
 			FilenameFilter filter = new PhrescoFileFilter("", extension);
 			File[] listFiles = testDir.listFiles(filter);
 			for (File file : listFiles) {
 				if (file.isFile()) {
 					testResultFileNames.add(file);
 				}
 			}
 		}
 		return testResultFileNames;
 	}
 
 	public static Map<String, String> getDeviceNames(Document document)  throws Exception {
 		NodeList nodeList = org.apache.xpath.XPathAPI.selectNodeList(document, "/*/deviceInfo");
 		Map<String, String> deviceList = new LinkedHashMap<String, String>(100);
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			Node node = nodeList.item(i);
 			NamedNodeMap nameNodeMap = node.getAttributes();
 			String deviceId = "";
 			String deviceName = "";
 			Node idAttr = nameNodeMap.getNamedItem(ATTR_ID);
 			deviceId = idAttr.getNodeValue();
 			Node nameAttr = nameNodeMap.getNamedItem(ATTR_NAME);
 			deviceName = nameAttr.getNodeValue();
 			deviceList.put(deviceId, deviceName);
 		}
 		return deviceList;
 	}
 	
     public static float roundFloat(int decimal, double value) {
 		BigDecimal roundThroughPut = new BigDecimal(value);
 		return roundThroughPut.setScale(decimal, BigDecimal.ROUND_HALF_EVEN).floatValue();
 	}
     
 	public int getNoOfTstSuiteTests() {
 		return noOfTstSuiteTests;
 	}
 
 	public void setNoOfTstSuiteTests(int noOfTstSuiteTests) {
 		this.noOfTstSuiteTests = noOfTstSuiteTests;
 	}
 
 	public int getNoOfTstSuiteFailures() {
 		return noOfTstSuiteFailures;
 	}
 
 	public void setNoOfTstSuiteFailures(int noOfTstSuiteFailures) {
 		this.noOfTstSuiteFailures = noOfTstSuiteFailures;
 	}
 
 	public int getNoOfTstSuiteErrors() {
 		return noOfTstSuiteErrors;
 	}
 
 	public void setNoOfTstSuiteErrors(int noOfTstSuiteErrors) {
 		this.noOfTstSuiteErrors = noOfTstSuiteErrors;
 	}
 	
 	private ApplicationInfo getApplicationInfo(File projectInfoFile) throws MojoExecutionException {
 		try {
 	        Gson gson = new Gson();
 	        BufferedReader reader = null;
 	        reader = new BufferedReader(new FileReader(projectInfoFile));
 	        ProjectInfo projectInfo = gson.fromJson(reader, ProjectInfo.class);
 	        this.projName = projectInfo.getName();
 	        List<ApplicationInfo> appInfos = projectInfo.getAppInfos();
 	        for (ApplicationInfo appInfo : appInfos) {
 				return appInfo;
 			}
 		} catch (Exception e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 		return null;
 	}
 	
 	private File getPomFile() throws PhrescoException {
 		PluginUtils pu = new PluginUtils();
 		ApplicationInfo appInfo = pu.getAppInfo(baseDir);
 		pomFileName = Utility.getPhrescoPomFromWorkingDirectory(appInfo, baseDir);
 		File pom = new File(baseDir.getPath() + File.separator + pomFileName);
 		return pom;
 	}
 	
 	private void setMavenProject(File rootDir, String moduleName) throws Exception {
 		String pomVersion = mavenProject.getVersion();
         // Multi module handling
         if (StringUtils.isNotEmpty(moduleName)) {
         	baseDir = new File(rootDir, moduleName);
         	pomFile = getPomFile();
 			org.apache.maven.model.Model project = new org.apache.maven.model.Model();
 			PomProcessor pp = new PomProcessor(pomFile);
 			com.phresco.pom.model.Model.Properties modelProperties = pp.getModel().getProperties();
 			List<Element> propElem = modelProperties.getAny();
 			Properties properties = new Properties();
 			for (Element element : propElem) {
 				properties.put(element.getTagName(), element.getTextContent());
 			}
 			project.setProperties(properties);
 			mavenProject = new MavenProject(project);
 			mavenProject.setFile(pomFile);
 			mavenProject.setVersion(pomVersion);
 			this.version = pomVersion;
         }
 	}
 	
 	public void setConfigurationValues(Configuration config) {
 		Map<String, String> configs = MojoUtil.getAllValues(config);
         String reportType = configs.get(REPORT_TYPE);
         String testType = configs.get(TEST_TYPE);
         String sonarUrl = configs.get("sonarUrl");
         String appVersion = mavenProject.getVersion();
         String pdfReportName = configs.get("reportName");
         
         logo = configs.get("logo");
         // Default copyrights
         this.copyRights = DEFAULT_COPYRIGHTS;
         String themeJson = configs.get("theme");
         if (StringUtils.isNotEmpty(themeJson)) {
         	Gson gson = new Gson();
         	Type mapType = new TypeToken<Map<String, String>>() {}.getType();
 	        theme = (Map<String, String>)gson.fromJson(themeJson, mapType);
 	        
 	        if (MapUtils.isNotEmpty(theme) && StringUtils.isNotEmpty(theme.get("copyRightText"))) {
 	        	this.copyRights = theme.get("copyRightText");
 	        }
         }
         
         if (StringUtils.isEmpty(this.copyRights)) {
 			this.copyRights = DEFAULT_COPYRIGHTS;
 		}
         
         this.testType = testType;
         this.rootTestType = testType;
         this.reportType = reportType;
         this.techName = configs.get("technologyName");
         this.version = appVersion;
         this.sonarUrl = sonarUrl;
         this.pdfReportName = pdfReportName;
 	}
 	
 	public void setAppinfoConfigValues(ApplicationInfo appInfo) {
         this.appDir = appInfo.getAppDirName();
         this.projectCode = appInfo.getName();
 	}
 	
 	public void setReportFileName() {
 		 if (StringUtils.isEmpty(pdfReportName)) {
 	        	if (testType.equals("All")) {
 	        		this.fileName = baseDir.getName() + STR_UNDERSCORE + this.reportType + STR_UNDERSCORE + fileName;
 	        	} else {
 	        		this.fileName = testType + STR_UNDERSCORE + this.reportType + STR_UNDERSCORE + fileName; // time
 	        	}
 	        } else {
 	        	this.fileName = pdfReportName + STR_UNDERSCORE + this.reportType;
 	        }
 	}
 	
 	public void isClangReport() {
     	String clangReportPath = mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_VALIDATE_REPORT);
     	if (StringUtils.isNotEmpty(clangReportPath)) {
     		isClangReport = true;
     	}
 	}
 	
 	public void getUnitTestReport(MultiModuleReports appendTestReport) throws Exception {
 		testType = UNIT;
 		SureFireReport unitTestSureFireReports = sureFireReports(null);
 		
 		List<TestSuite> testSuitesUnit = unitTestSureFireReports.getTestSuites();
 		List<AllTestSuite> allTestSuitesUnit = unitTestSureFireReports.getAllTestSuites();
 		if (CollectionUtils.isNotEmpty(allTestSuitesUnit) || CollectionUtils.isNotEmpty(testSuitesUnit)) {
 			appendTestReport.setUnitTestReport(unitTestSureFireReports);
 		}
 	}
 	
 	public void getManualTestReport(MultiModuleReports appendTestReport) throws Exception {
 		testType = MANUAL;
 		SureFireReport manualTestReport = sureFireReports(null);
 		List<AllTestSuite> allTestSuitesManual = manualTestReport.getAllTestSuites();
 		List<TestSuite> testSuitesManual = manualTestReport.getTestSuites();
 		if (CollectionUtils.isNotEmpty(testSuitesManual) || CollectionUtils.isNotEmpty(allTestSuitesManual)) {
 			appendTestReport.setManualTestReport(manualTestReport);
 		}
 	}
 	
 	public void getFunctionalTestReport(MultiModuleReports appendTestReport) throws Exception {
 		testType = FUNCTIONAL;
 		boolean isClassEmpty = true;
 		List<TestSuite> testSuitesFunctional = null;
 		SureFireReport functionalSureFireReports = sureFireReports(null);
 		List<AllTestSuite> allTestSuitesFunctional = functionalSureFireReports.getAllTestSuites();
 		testSuitesFunctional = functionalSureFireReports.getTestSuites();
 		if (CollectionUtils.isNotEmpty(testSuitesFunctional) || CollectionUtils.isNotEmpty(allTestSuitesFunctional)) {
 			for (TestSuite testSuite : testSuitesFunctional) {
 				List<TestCase> testcases = testSuite.getTestCases();
 				for (TestCase testCase : testcases) {
 					if (StringUtils.isNotEmpty(testCase.getTestClass())) {
 						isClassEmpty = false;
 					}
 				}
 			}
 			appendTestReport.setFunctionalTestReport(functionalSureFireReports);
 		}
 		appendTestReport.setIsClangReport(isClangReport);
 	}
 	
 	public void getComponentTestReport(MultiModuleReports appendTestReport) throws Exception {
 		testType = COMPONENT;
 		List<TestSuite> testSuitesComponent = null;
 		SureFireReport componentSureFireReports = sureFireReports(null);
 		List<AllTestSuite> allTestSuitesComponent = null;
 		allTestSuitesComponent = componentSureFireReports.getAllTestSuites();
 		testSuitesComponent = componentSureFireReports.getTestSuites();
 		if (CollectionUtils.isNotEmpty(testSuitesComponent) || CollectionUtils.isNotEmpty(allTestSuitesComponent)) {
 			appendTestReport.setComponentTestReport(componentSureFireReports);
 		}
 	}
 	
 	public void getPerformanceTestReport(MultiModuleReports appendTestReport) throws Exception {
 		testType = PERFORMACE;
 		//performance details
 		boolean deviceReportAvail = isDeviceReportAvail();
 		showDeviceReport = deviceReportAvail;
 		
 		if(showDeviceReport) { //showDeviceReport
 			List<AndroidPerfReport> perforamceTest = getJmeterTestResultsForAndroid();
 			if (CollectionUtils.isNotEmpty(perforamceTest)) {
 				appendTestReport.setIsAndroidSpecialHandling(true);
 				appendTestReport.setAndroidPerformaceTestReport(perforamceTest);
 			}
 		} else {
 			List<JmeterTypeReport> perforamceTest = getJmeterTestResults();
 			if (CollectionUtils.isNotEmpty(perforamceTest)) {
 				appendTestReport.setIsAndroidSpecialHandling(false);
 				appendTestReport.setPerformanceTestReport(perforamceTest);
 			}
 		}
 	}
 	
 	public void getLoadTestReport(MultiModuleReports appendTestReport) throws Exception {
 		testType = LOAD;
 		List<JmeterTypeReport> loadTestReport  = getJmeterTestResults();
 		if (CollectionUtils.isNotEmpty(loadTestReport)) {
 			appendTestReport.setLoadTestReport(loadTestReport);
 		}
 	}
 	
 	public void getCodeValidationReport(MultiModuleReports appendTestReport) throws Exception {
 		if (!isClangReport) {
 			//Sonar details
 			List<SonarReport> sonarReports = new ArrayList<SonarReport>();
 			String pomPath =  baseDir + File.separator + mavenProject.getFile().getName();
 			if (StringUtils.isNotEmpty(sonarUrl)) {
 				List<String> sonarTechReports = getSonarProfiles(pomPath);
 				if (sonarTechReports != null) {
 					if(CollectionUtils.isEmpty(sonarTechReports)) {
 						sonarTechReports.add(SONAR_SOURCE);
 					}
 					sonarTechReports.add(FUNCTIONAL);
 					for (String sonarTechReport : sonarTechReports) {
 						SonarReport srcSonarReport = generateSonarReport(sonarTechReport, null);
 						if(srcSonarReport != null) {
 							sonarReports.add(srcSonarReport);
 						}
 					}
 				}
 				if (CollectionUtils.isNotEmpty(sonarReports)) {
 					appendTestReport.setCodeValidationReports(sonarReports);
 				}
 			}
 		}
 	}
 	
 	public void getTestReport(MultiModuleReports appendTestReport) throws Exception {
 		getUnitTestReport(appendTestReport);
 		getManualTestReport(appendTestReport);
 		getFunctionalTestReport(appendTestReport);
 		getComponentTestReport(appendTestReport);
 		getPerformanceTestReport(appendTestReport);
 		getLoadTestReport(appendTestReport);
 		getCodeValidationReport(appendTestReport);
 	}
 	
 	public void multiModulereport(Configuration config) throws Exception {
     	setReportFileName();
     	
     	List<String> modules = null;
     	
     	String rootPomFile = mavenProject.getProperties().getProperty("source.pom");
 		if (StringUtils.isNotEmpty(rootPomFile)) {
 			modules = getMultiModules(modules, rootPomFile);
 		} else {
 			modules = PluginUtils.getProjectModules(mavenProject);
 		}
 		
 		Map<String, Object> reportParams = new HashMap<String,Object>();
 		reportParams.put(COPY_RIGHTS, copyRights);
 		reportParams.put(REPORTS_TYPE, reportType);
 		reportParams.put(LOGO, logo);
 		
     	List<MultiModuleReports> multiModuleReports = new ArrayList<MultiModuleReports>(modules.size());
     	
     	// Root module data
     	ApplicationInfo rootAppInfo = getApplicationInfo(); // sets appName and ProjectName
         setAppinfoConfigValues(rootAppInfo);
         isClangReport(); // isClangReport check
         
     	MultiModuleReports rootModuleReport = new MultiModuleReports();
     	rootModuleReport.setIsRootModule(true);
     	rootModuleReport.setApplicationLabel("Application Name :");
     	
     	rootModuleReport.setIsClangReport(isClangReport);
     	rootModuleReport.setProjectName(projName);
     	rootModuleReport.setApplicationName(projectCode);
     	rootModuleReport.setTechnologyName(techName);
     	rootModuleReport.setVersion(version);
     	rootModuleReport.setReportType(reportType);
     	rootModuleReport.setLogo(logo);
     	
     	// set Report objects on rootModuleReport
     	getTestReport(rootModuleReport);
     	// add the each application or module object here
     	multiModuleReports.add(rootModuleReport);
     	// multi module object
     	if (CollectionUtils.isNotEmpty(modules)) {
 	    	for (String module : modules) {
 	    		setMavenProject(rootDir, module); // it will set the mavenProject info
 		        ApplicationInfo appInfo = getApplicationInfo(); // sets appName and ProjectName
 		        setAppinfoConfigValues(appInfo);
 		        isClangReport(); // isClangReport check
 		        
 	    		MultiModuleReports multiModuleReport = new MultiModuleReports();
 	    		multiModuleReport.setIsRootModule(false);
 	    		multiModuleReport.setApplicationLabel("Module Name :");
 	    		
 	    		multiModuleReport.setProjectName(projName);
 	    		multiModuleReport.setApplicationName(projectCode);
 	        	multiModuleReport.setIsClangReport(isClangReport);
 	        	multiModuleReport.setTechnologyName(techName);
 	        	multiModuleReport.setVersion(version);
 	        	multiModuleReport.setReportType(reportType);
 	    		// set report objects
 	        	getTestReport(multiModuleReport);
 	        	// add the each application or module object here
 	        	multiModuleReports.add(multiModuleReport);
 			}
     	}
     	// generate report method call here
     	generateMultiModuleReport(multiModuleReports, reportParams);
 	}
 
 	private List<String> getMultiModules(List<String> modules,
 			String rootPomFile) throws PhrescoException {
 		File pomFile = new File(baseDir, rootPomFile);
 		PomProcessor processor;
 		try {
 			processor = new PomProcessor(pomFile);
 			Modules pomModule = processor.getPomModule();
 			if(pomModule != null) {
 				modules = pomModule.getModule();
 			}
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 		return modules;
 	}
 	
 	// Generate the pdf report with all modules
 	public void generateMultiModuleReport(List<MultiModuleReports> multiModuleReports, Map<String, Object> reportParams) throws PhrescoException {
 		String outFileNamePDF = rootDir.getAbsolutePath() + File.separator + DO_NOT_CHECKIN_FOLDER + File.separator + ARCHIVES + File.separator + CUMULATIVE + File.separator + fileName + DOT + PDF;
 		new File(outFileNamePDF).getParentFile().mkdirs();
 		
 		InputStream reportStream = null;
 		BufferedInputStream bufferedInputStream = null;
 		try {
 			String containerJasperFile = "MultiModuleReportRootPage.jasper";
 			reportStream = this.getClass().getClassLoader().getResourceAsStream(REPORTS_JASPER + containerJasperFile);
 			bufferedInputStream = new BufferedInputStream(reportStream);
 			
 			JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(multiModuleReports);
 			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(bufferedInputStream);
 			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, reportParams, dataSource);
 			
 			// applying theme
 			applyTheme(jasperPrint);
 			
 			// move table of contents
 			jasperPrint = moveMultiModuleTableOfContents(jasperPrint);
 			
 			JRExporter exporter = new net.sf.jasperreports.engine.export.JRPdfExporter(); 
 			exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outFileNamePDF);
 			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
 			exporter.exportReport();
 		} catch(Exception e) {
 			log.error("Unit and functional generation error");
 			e.printStackTrace();
 			throw new PhrescoException(e);
 		} finally {
 			if (reportStream != null) {
 				try {
 					reportStream.close();
 				} catch (IOException e) {
 					log.error("Report generation errorr ");
 				}
 			}
 			if (bufferedInputStream != null) {
 				try {
 					bufferedInputStream.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 					log.error("Report generation errorr ");
 				}
 			}
 		}
 	}
 	
 	public void generate(Configuration config, MavenProjectInfo mavenProjectInfo, Log log) throws PhrescoException {
 		try {
 			this.log = log;
 	        baseDir = mavenProjectInfo.getBaseDir();
 	        mavenProject = mavenProjectInfo.getProject();
 	        moduleName = mavenProjectInfo.getModuleName();
 	        rootDir = mavenProjectInfo.getBaseDir();
 	        
 	        // set pdf basic config values
 	        setConfigurationValues(config);
 	        
 	        if (StringUtils.isEmpty(this.reportType)) {
 	        	throw new PhrescoException("Report type is empty ");
 	        }
 	        
 	        if (StringUtils.isEmpty(testType)) {
 	        	throw new PhrescoException("Test Type is empty ");
 	        }
 	        
 	        // Report generation part
 	        if (isMultiModuleProject() && StringUtils.isEmpty(moduleName) && "All".equalsIgnoreCase(testType)) {
 	        	multiModulereport(config);
 	        } else {
 		        setMavenProject(rootDir, moduleName);
 		        ApplicationInfo appInfo = getApplicationInfo();
 		        setAppinfoConfigValues(appInfo);
 		        setReportFileName();
 		        isClangReport();
 		    	
 		    	if ("All".equalsIgnoreCase(testType)) {
 		        	log.info("all report generation started ... "); // all report
 		        	cumalitiveTestReport();
 		        } else {
 		        	log.info("indivudal report generation started ... "); // specified type report
 		        	generatePdfReport();
 		        }
 		        log.info("Report generation completed ... ");
 	        }
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	private ApplicationInfo getApplicationInfo() throws MojoExecutionException, IOException {
 		// get projects plugin info file path
 		File projectInfo = new File(baseDir, DOT_PHRESCO_FOLDER + File.separator + PROJECT_INFO_FILE);
 		if (!projectInfo.exists()) {
 			throw new MojoExecutionException("Project info file is not found in jenkins workspace dir " + baseDir.getCanonicalPath());
 		}
 		ApplicationInfo appInfo = getApplicationInfo(projectInfo);
 		if (appInfo == null) {
 			throw new MojoExecutionException("AppInfo value is Null ");
 		}
 		return appInfo;
 	}
 	
 	private boolean isMultiModuleProject() throws PhrescoException {
 		List<String> modules = null;
 		String rootPomFile = mavenProject.getProperties().getProperty("source.pom");
 		if (StringUtils.isNotEmpty(rootPomFile)) {
 			modules = getMultiModules(modules, rootPomFile);
 		} else {
 			modules = PluginUtils.getProjectModules(mavenProject);
 		}
 		boolean isMultiModuleProject = false;
 		if (CollectionUtils.isNotEmpty(modules)) {
 			isMultiModuleProject = true;
 		}
 		return isMultiModuleProject;
 	}
 	
 	// This method is used by test cases for testing purpose
 	public void generateTest(String baseDirPath, String dotPhrescoFilePath,
 			String testType, String reportType, String sonarUrl,
 			Properties properties, String logoimage64, String themeJson1, String technologyName, String pomPath)
 			throws PhrescoException {
 		try {
 	        baseDir = new File(baseDirPath);
 	        
 			org.apache.maven.model.Model project = new org.apache.maven.model.Model();
 			project.setProperties(properties);
 			mavenProject = new MavenProject(project);
 			mavenProject.setFile(new File(pomPath));
 	        
 	        // get projects plugin info file path
 	        File projectInfo = new File(baseDir, DOT_PHRESCO_FOLDER + File.separator + PROJECT_INFO_FILE);
 			if (!projectInfo.exists()) {
 				throw new MojoExecutionException("Project info file is not found in Dir " + baseDir.getCanonicalPath());
 			}
 			ApplicationInfo appInfo = getApplicationInfo(projectInfo);
 			if (appInfo == null) {
 				throw new MojoExecutionException("AppInfo value is Null ");
 			}
 	        
 	        logo = logoimage64;
 	        if (StringUtils.isNotEmpty(themeJson1)) {
 	        	Gson gson = new Gson();
 	        	Type mapType = new TypeToken<Map<String, String>>() {}.getType();
 		        theme = (Map<String, String>)gson.fromJson(themeJson1, mapType);
 		        if (MapUtils.isNotEmpty(theme) && StringUtils.isNotEmpty(theme.get("CopyRight"))) {
 		        	this.copyRights = theme.get("CopyRight");
 		        }
 	        }
 	        
 			if (StringUtils.isEmpty(this.copyRights)) {
 				this.copyRights = DEFAULT_COPYRIGHTS;
 			}
 	        
 	        this.testType = testType;
 	        this.reportType = reportType;
 	        this.appDir = appInfo.getAppDirName();
 	        this.projectCode = appInfo.getName();
 	        this.techName = technologyName;
 	        this.version = "1.0-testjdhfjdfjhjdf -kdjfhjkdhfj dfj";
 	        this.sonarUrl = sonarUrl;
 	        String pdfReportName = "test";
 	        if (StringUtils.isEmpty(pdfReportName)) {
 	        	if (testType.equals("All")) {
 	        		this.fileName = baseDir.getName() + STR_UNDERSCORE + reportType + STR_UNDERSCORE + fileName;
 	        	} else {
 	        		this.fileName = testType + STR_UNDERSCORE + reportType + STR_UNDERSCORE + fileName; // time
 	        	}
 	        } else {
 	        	this.fileName = pdfReportName + STR_UNDERSCORE + reportType + STR_UNDERSCORE + fileName;
 	        }
 	        
 	        if (StringUtils.isEmpty(reportType)) {
 	        	throw new PhrescoException("Report type is empty ");
 	        }
 	        
 	        if (StringUtils.isEmpty(testType)) {
 	        	throw new PhrescoException("Test Type is empty ");
 	        }
 	        
 	    	String clangReportPath = mavenProject.getProperties().getProperty(Constants.POM_PROP_KEY_VALIDATE_REPORT);
 	    	if (StringUtils.isNotEmpty(clangReportPath)) {
 	    		isClangReport = true;
 	    	}
 	    	
 	        if ("All".equalsIgnoreCase(testType)) {
 	        	System.out.println("all report generation started ... "); // all report
 	        	cumalitiveTestReport();
 	        } else {
 	        	System.out.println("indivudal report generation started ... "); // specified type report
 	        	generatePdfReport();
 	        }
 	        
 	        System.out.println("Report generation completed ... ");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public List<String> getSonarProfiles(String pomPath) throws PhrescoException {
 		List<String> sonarTechReports = new ArrayList<String>(6);
 		try {
 			PomProcessor pomProcessor = new PomProcessor(new File(pomPath));
 			Model model = pomProcessor.getModel();
 			Profiles modelProfiles = model.getProfiles();
 			if (modelProfiles == null) {
 				return sonarTechReports;
 			}
 			List<Profile> profiles = modelProfiles.getProfile();
 			if (profiles == null) {
 				return sonarTechReports;
 			}
 			for (Profile profile : profiles) {
 				if (profile.getProperties() != null) {
 					List<Element> any = profile.getProperties().getAny();
 					int size = any.size();
 					
 					for (int i = 0; i < size; ++i) {
 						boolean tagExist = 	any.get(i).getTagName().equals(SONAR_LANGUAGE);
 						if (tagExist){
 							sonarTechReports.add(profile.getId());
 						}
 					}
 				}
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 		return sonarTechReports;
 	}
 	
 	private void applyTheme(JasperPrint jasperPrint) throws Exception {
 		
 		
 		if (MapUtils.isNotEmpty(theme)) {
 			if(StringUtils.isNotEmpty(theme.get("headerBackGroundcolorTop"))){
 				titleColor = theme.get("headerBackGroundcolorTop");
 				titleLabelColor = theme.get("headerBackGroundcolorTop");
 				headingForeColor = theme.get("headerBackGroundcolorTop");
 //				headingRowBackColor = theme.get("headerBackGroundcolorTop");
 				copyRightBackColor = theme.get("headerBackGroundcolorTop");
 				copyRightPageNumberBackColor = theme.get("headerBackGroundcolorTop");
 			}
 			if(StringUtils.isNotEmpty(theme.get("pageTitleBackGroundTop"))){
 				headingBackColor = theme.get("pageTitleBackGroundTop");
 				headingRowTextBackColor = theme.get("pageTitleBackGroundTop");
 				headingRowBackColor = theme.get("headerBackGroundcolorTop");
 			}
 			if(StringUtils.isNotEmpty(theme.get("editNavigationActiveBackGroundTop"))){
 				headingRowLabelBackColor = theme.get("editNavigationActiveBackGroundTop");
 			}
 			if(StringUtils.isNotEmpty(theme.get("pageTitleBackGroundBottom"))){
 				headingRowLabelForeColor = theme.get("pageTitleBackGroundBottom");
 				headingRowTextForeColor = theme.get("pageTitleBackGroundBottom");
 				copyRightForeColor = theme.get("pageTitleBackGroundBottom");
 				copyRightPageNumberForeColor = theme.get("pageTitleBackGroundBottom");
 			}
 		}
 		
 		java.awt.Color userTitleColor = java.awt.Color.decode(titleColor);
 		java.awt.Color userTitleLabelColor = java.awt.Color.decode(titleLabelColor);
 		
 		java.awt.Color userHeadingForeColor = java.awt.Color.decode(headingForeColor); // heading yellow color
 		java.awt.Color userHeadingBackColor = java.awt.Color.decode(headingBackColor);
 		
 		java.awt.Color userHeadingRowBackColor = java.awt.Color.decode(headingRowBackColor); // HeadingRow - light color
 		java.awt.Color userHeadingRowLabelBackColor = java.awt.Color.decode(headingRowLabelBackColor); // HeadingRow - light color
 		java.awt.Color userHeadingRowLabelForeColor = java.awt.Color.decode(headingRowLabelForeColor); // HeadingRow - light color
 		java.awt.Color userHeadingRowTextBackColor = java.awt.Color.decode(headingRowTextBackColor); // HeadingRow - light color
 		java.awt.Color userHeadingRowTextForeColor = java.awt.Color.decode(headingRowTextForeColor); // HeadingRow - light color
 		
 		java.awt.Color userCopyRightBackColor = java.awt.Color.decode(copyRightBackColor);
 		java.awt.Color userCopyRightForeColor = java.awt.Color.decode(copyRightForeColor);
 		java.awt.Color userCopyRightPageNumberForeColor = java.awt.Color.decode(copyRightPageNumberForeColor);
 		java.awt.Color userCopyRightPageNumberBackColor = java.awt.Color.decode(copyRightPageNumberBackColor);
 		
 		
 		JRStyle[] styleList = jasperPrint.getStyles();
 		for (int j = 0; j < styleList.length; j++) {
 			if (styleList[j].getName().endsWith("TitleRectLogo")) {
 		        styleList[j].setBackcolor(userTitleColor);
 		        jasperPrint.addStyle(styleList[j], true);
 			} else if (styleList[j].getName().endsWith("TitleRectDetail")) {
 				styleList[j].setForecolor(userTitleLabelColor);
 				JRPen linePen = styleList[j].getLinePen();
 				linePen.setLineColor(userTitleColor);
 				linePen.setLineWidth(1);
 		        jasperPrint.addStyle(styleList[j], true);
 		    } else if (styleList[j].getName().endsWith("TitleLabel")) {
 		        styleList[j].setForecolor(userTitleLabelColor);
 		        jasperPrint.addStyle(styleList[j], true);
 		    } else if (styleList[j].getName().endsWith("TitleLabelValue")) {
 		        styleList[j].setForecolor(userTitleLabelColor);
 		        jasperPrint.addStyle(styleList[j], true);
 		    }  else if (styleList[j].getName().endsWith("Heading")) {
 		    	styleList[j].setForecolor(userHeadingForeColor);
 		    	styleList[j].setBackcolor(userHeadingBackColor);
 	        	jasperPrint.addStyle(styleList[j], true);
 		    }  else if (styleList[j].getName().endsWith("CopyRight")) {
 		    	styleList[j].setForecolor(userCopyRightForeColor);
 		    	styleList[j].setBackcolor(userCopyRightBackColor);
 	        	jasperPrint.addStyle(styleList[j], true);
 		    }  else if (styleList[j].getName().endsWith("CopyRightPageNo")) {
 		    	styleList[j].setForecolor(userCopyRightPageNumberForeColor);
 		    	styleList[j].setBackcolor(userCopyRightPageNumberBackColor);
 	        	jasperPrint.addStyle(styleList[j], true);
 		    }  else if (styleList[j].getName().endsWith("HeadingRow")) {
 		    	styleList[j].setBackcolor(userHeadingRowBackColor);
 	        	jasperPrint.addStyle(styleList[j], true);
 		    }  else if (styleList[j].getName().endsWith("HeadingRowLabel")) {
 		    	styleList[j].setForecolor(userHeadingRowLabelForeColor);
 		    	styleList[j].setBackcolor(userHeadingRowLabelBackColor);
 	        	jasperPrint.addStyle(styleList[j], true);
 		    }  else if (styleList[j].getName().endsWith("HeadingRowLabelValue")) {
 		    	styleList[j].setForecolor(userHeadingRowTextForeColor);
 		    	styleList[j].setBackcolor(userHeadingRowTextBackColor);
 	        	jasperPrint.addStyle(styleList[j], true);
 		   	// table related styles
 		    }  else if (styleList[j].getName().endsWith("table_CH")) {
 		    	styleList[j].setBackcolor(userHeadingRowBackColor);
 	        	jasperPrint.addStyle(styleList[j], true);
 		    }  else if (styleList[j].getName().endsWith("table_CH_Label")) {
 		    	styleList[j].setForecolor(userHeadingRowLabelForeColor);
 		    	styleList[j].setBackcolor(userHeadingRowLabelBackColor);
 	        	jasperPrint.addStyle(styleList[j], true);
 		    }
 //		    else if (styleList[j].getName().endsWith("table_TD_Label")) {
 //		    	styleList[j].setForecolor(userHeadingRowLabelForeColor);
 //		    	styleList[j].setBackcolor(userHeadingRowLabelBackColor);
 //	        	jasperPrint.addStyle(styleList[j], true);
 //		    }
 		}
 	}
 }
 
 class PhrescoFileFilter implements FilenameFilter {
 	private String name;
 	private String extension;
 
 	public PhrescoFileFilter(String name, String extension) {
 		this.name = name;
 		this.extension = extension;
 	}
 
 	public boolean accept(File directory, String filename) {
 		boolean fileOK = true;
 
 		if (name != null) {
 			fileOK &= filename.startsWith(name);
 		}
 
 		if (extension != null) {
 			fileOK &= filename.endsWith('.' + extension);
 		}
 		return fileOK;
 	}
 }
 
 class FileExtensionFileFilter implements FilenameFilter {
     private String filter_;
     public FileExtensionFileFilter(String filter) {
         filter_ = filter;
     }
 
     public boolean accept(File dir, String name) {
         return name.endsWith(filter_);
     }
     
 }
