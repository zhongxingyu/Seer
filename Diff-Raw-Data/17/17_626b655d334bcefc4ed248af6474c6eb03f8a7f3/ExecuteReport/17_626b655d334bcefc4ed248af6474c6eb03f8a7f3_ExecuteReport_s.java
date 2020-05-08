 package it.bisi.report.jasper;
 
 import it.bisi.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.AccessControlException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.sax.SAXSource;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import net.sf.saxon.lib.NamespaceConstant;
 import net.sf.saxon.om.NodeInfo;
 import net.sf.saxon.xpath.XPathEvaluator;
 
 
 import net.sf.jasperreports.engine.JREmptyDataSource;
 import net.sf.jasperreports.engine.JRParameter;
 import net.sf.jasperreports.engine.JasperCompileManager;
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.JasperReport;
 import net.sf.jasperreports.view.JasperViewer;
 import net.sf.jasperreports.engine.JasperExportManager;
 import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
 
 
 import net.sf.jasperreports.engine.JRExporterParameter;
 import net.sf.jasperreports.engine.export.JExcelApiExporter;
 import net.sf.jasperreports.engine.export.JRPdfExporter;
 //import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
 import net.sf.jasperreports.engine.export.JRRtfExporter;
 import net.sf.jasperreports.engine.export.JRXlsExporter;
 import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
 import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
 import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
 
 //import it.bisi.report.GetData;
 
 
 /**
  * @author jaapandre
  *
  */
 public class ExecuteReport {
 	static String generatedFileNames="";
 
 	/**
 	 * @param args
 	 *   
 	 * webenq 4.3 args:
 	 * 0 databaseName
 	 * 1 userName, 
 	 * 2 password,
 	 * 3 reportId
 	 * 4 output dir  
 	 */
 	public static void main(String[] args) {
 		
 		runReport(args[0], args[1]);
 	}
 
 	public static void runReport(String configFileName, String apiKey)	{
 		if (apiKey.equals("")){
 			apiKey="5dcf19e85860d0e219c400ce8b5ea643";
 		}
 		try{
 			String tempDir="../tmp/";//storage of datafiles (xform data, def, reportdefinition)
 			checkCreateDir(tempDir);
 			
 			//get report config information (location, language, customer, etc)
 			Map<String,String> reportConfig=getReportControlFile(configFileName, apiKey);
 			//System.out.println(reportConfig);
 
 			String xformName=reportConfig.get("xformName");
 			String outputDir=reportConfig.get("outputDir");
 			if (outputDir==null){
 				outputDir=".";
 			}
 			outputDir="../public/reports/"+outputDir;
 			
 			String splitQuestionId=reportConfig.get("splitQuestionId");
 			String outputFileName=reportConfig.get("outputFileName");
 			if (outputFileName==null){
 				outputFileName="emptyFileName";
 			}
 			String outputFormat=reportConfig.get("outputFormat");
 			if (outputFormat==null) {
 				outputFormat="pdf";
 			}
 			String language=reportConfig.get("language");
 			if (language == null){
 				language="en";
 			}
 			String customer=reportConfig.get("customer");
 			if (customer == null){
 				customer="default";
 			}
 			
 			//if no local file retrieve data form url
 			//@todo files from a jar are not readable, so not able to use 
 			
 			String reportDefinitionLocation;
 			if (new File(reportConfig.get("reportDefinitionLocation")).canRead()){
 				reportDefinitionLocation=reportConfig.get("reportDefinitionLocation");
 			} else {
 				reportDefinitionLocation=it.bisi.report.GetData.getData( reportConfig.get("reportDefinitionLocation"), tempDir, (long) 3600, apiKey);
 			}
 			//System.out.println(reportDefinitionLocation);
 
 			String xformLocation;
 			if (new File(reportConfig.get("xformLocation")).canRead()){
 				xformLocation=reportConfig.get("xformLocation");
 			} else {
 				xformLocation=it.bisi.report.GetData.getData( reportConfig.get("xformLocation"), tempDir, (long) 3600, apiKey);
 			}
 			//System.out.println(xformLocation);
 
 			if (reportConfig.get("dataLocation")==null){
 				System.out.println("No data location defined, possible you don't have the right permission to get the file");
 				System.err.println("No data location defined");
 			}
 			String dataLocation;
 			if (new File(reportConfig.get("dataLocation")).canRead()){
 				dataLocation=reportConfig.get("dataLocation");
 			} else {
 				dataLocation=it.bisi.report.GetData.getData( reportConfig.get("dataLocation"), tempDir, (long) 3600000, apiKey);
 			}
 			//System.out.println(dataLocation);
 			
 			// create parameter map to send to jasper
 			Map<String,Object> prms = new HashMap<String,Object>();
 			prms.put("OUTPUT_DIR", outputDir);
 			prms.put("DATA_LOCATION", dataLocation);
 			prms.put("XFORM_LOCATION", xformLocation);
 			prms.put("FORM_NAME", xformName);
 			prms.put("CUSTOMER", customer); //resource bundle and needed for hacks in jrxml
 			prms.put("SPLIT_QUESTION_ID", splitQuestionId ); //not yet implemented #5395
 
 			//get map of colors of mean values
 			
 			HashMap<String,Map<String,Double>> color_range_map=it.bisi.Utils.getColorRangeMaps(customer);
 			prms.put("COLOR_RANGE_MAP",color_range_map);
 
 			HashMap<String, Map<String, Map<String, Object>>> recode_color_map=it.bisi.Utils.recodeColorMap(customer);
 			prms.put("RECODE_COLOR_MAP", recode_color_map);
 
 			
 			//localization
 			Locale locale = new Locale("nl", "NL", customer);
 			if (language.equals("en")){
 				locale = new Locale("en", "US", customer);
 			}
 			Locale.setDefault(locale);
 			prms.put(JRParameter.REPORT_LOCALE, locale);
 
 			//Get localized resource bundles with general texts
 			ResourceBundle myresources = ResourceBundle.getBundle("org.webenq.resources.webenq4",locale);
 			prms.put(JRParameter.REPORT_RESOURCE_BUNDLE, myresources);
 
 
 			//looping through available split by values (seperate reports for subsets of respondents)
 			if (splitQuestionId !=null && splitQuestionId.length()>0 ) {
				//get distinct split_values, using xpath2 (saxon)
				String searchSplitValues="distinct-values(//respondenten/respondent/*/"+splitQuestionId+")";
 
 				XPathFactory factory = XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON);
 				XPath xpath = factory.newXPath();
 				InputSource is = new InputSource(dataLocation);
 				SAXSource ss = new SAXSource(is);
 				NodeInfo doc = ((XPathEvaluator)xpath).setSource(ss);
 				XPathExpression expr = xpath.compile(searchSplitValues);
 				List splitValuesList=(List) expr.evaluate(doc, XPathConstants.NODESET);
 
 				// iterate through the split_question_values, and create report for each of them
 				String splitQuestionValue;
 				for (Iterator iter = splitValuesList.iterator(); iter.hasNext();) {
 					splitQuestionValue = (String) iter.next();
 
 					//needed for displaying content
 					String splitQuestionLabel=it.bisi.Utils.getXformLabel(xformLocation, xformName, splitQuestionId, splitQuestionValue);
 					//System.out.println(splitQuestionLabel);
 					prms.put("SPLIT_QUESTION_VALUE", splitQuestionValue);
 					prms.put("SPLIT_QUESTION_LABEL", splitQuestionLabel);
 					generateReport(reportDefinitionLocation, prms, splitQuestionLabel, outputDir, outputFileName, outputFormat );
 
 				}
 			}else{
 				//no split value
 				prms.put("SPLIT_QUESTION_VALUE", "");
 				prms.put("SPLIT_QUESTION_LABEL", "");
 				generateReport(reportDefinitionLocation, prms, "", outputDir, outputFileName, outputFormat );
 			}
 			System.out.println(generatedFileNames);
 		}
 
 		catch(Exception ex) {
 			ex.printStackTrace();
 			System.out.println("error generating report(s): \n");
 			System.out.print(ex);
 			System.exit(2);
 		}
 	
 	}
 
 	static String generateReport(String reportDefinitionLocation, Map<String,Object> prms, String splitQuestionValue, String outputDir, String outputFileName, String outputFormat ) throws Exception{
 		//clean fileName
 		//pre condition: need a writable outputdir
 		checkCreateDir(outputDir);
 		
 		if (splitQuestionValue.length()>0){
 			// no slash in split part
 			outputFileName=cleanFileName(outputDir,true)+"/"+cleanFileName(outputFileName,false)+"-"+cleanFileName(splitQuestionValue,false);
 		}else{
 			//output_file_name=fileName(output_file_name, true);
 			outputFileName=cleanFileName(outputDir,true)+"/"+cleanFileName(outputFileName,false);
 		}
 		generatedFileNames+=outputFileName+"."+outputFormat+",";
 		
 		//System.out.println(reportDefinitionLocation);
 		//File inputFile=new File(reportDefinitionLocation);
 		File inputFile=new File(reportDefinitionLocation);
 						
 		URL url;
 		url =new URL(inputFile.toURI().toURL().toString());
 		InputStream inputStream =url.openStream();
 		
 		JasperPrint print;
 		if (!reportDefinitionLocation.endsWith("jasper")){
 			System.setProperty("jasper.reports.compile.temp","/tmp");
 			//need to compile the report
 			JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);
 			// we need an empty datasource to display the report...
 			//we can extend this to encapsulated subreports into reports (i think)
 			print = JasperFillManager.fillReport(jasperReport, prms, new JREmptyDataSource());
 		}else{
 			//report is already compiled
 			print = JasperFillManager.fillReport(inputStream, prms, new JREmptyDataSource());
 		}
 		// Create output in directory public/reports  
 		if(outputFormat.equals("pdf")) {
 		
 			JRPdfExporter exporter = new JRPdfExporter(); 
 			exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outputFileName + ".pdf");
 			exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
 			exporter.exportReport();
 		} else if(outputFormat.equals("odt")) {
 			JROdtExporter exporter = new JROdtExporter();
 			exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outputFileName + ".odt");
 			exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
 			exporter.exportReport();
 		}else if(outputFormat.equals("rtf")) {
 			//untested
 			JRRtfExporter exporter = new JRRtfExporter(); 
 			exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outputFileName + ".rtf");
 			exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
 			exporter.exportReport();
 		}else if (outputFormat.equals("docx")){
 			//untested from jasper report sample
 			JRDocxExporter exporter = new JRDocxExporter();
 			exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outputFileName + ".docx");
 			exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
 			exporter.exportReport();
 		} else if(outputFormat.equals("html")) {
 			JasperExportManager.exportReportToHtmlFile(print, outputFileName +"-"+splitQuestionValue+ ".html");
 		} else if(outputFormat.equals("xml")) {
 			JasperExportManager.exportReportToXmlFile(print, outputFileName +"_"+splitQuestionValue+ ".xml", false);
 		} else if(outputFormat.equals("xls")) {
 			JRXlsExporter exporter = new JRXlsExporter();
 			exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outputFileName +".xls");
 			exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
 			exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
 			exporter.exportReport();
 		}else if (outputFormat.equals("jxl")) {
 			//untested other xls creator? form jasper-report sample
 			JExcelApiExporter exporter = new JExcelApiExporter();
 			exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
 			exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outputFileName +".xls");
 			exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
 			exporter.exportReport();
 		}else if (outputFormat.equals("xlsx")){
 			//untested form jasper-report sample
 			JRXlsxExporter exporter = new JRXlsxExporter();
 			exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
 			exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
 			exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outputFileName +".xlsx");
 			exporter.exportReport();
 		} else { 
 			JasperViewer.viewReport(print);
 		}	
 		return generatedFileNames;
 	}
 	/*
 	 * Clean file name, only keep certain characters (A-Z a-z 0-9 _=/-+.)
 	 * if keepPath=false / will also be replaced by _
 	 * @todo move to utils
 	 */
 	static String cleanFileName(String fileName, Boolean keepPath){
 		//replace whitespaces with underscore
 		fileName=fileName.replaceAll("\\p{javaWhitespace}","_");
 		//replace other character to underscore (except A-Z a-z 0-9 _=/-+.
 		fileName=fileName.replaceAll("[^A-Za-z0-9_=\\/\\-\\+\\.]", "_");
 		if (!keepPath){
 			fileName=fileName.replaceAll("/", "_");
 		}
 		//remove duplicate underscores
 		fileName=fileName.replaceAll("_+", "_");
 		return fileName;
 
 	}
 	static Boolean checkCreateDir(String outputDir) throws IOException{
 		outputDir=cleanFileName(outputDir, true);
 		File output=new File(outputDir);
 		if (output.exists()){
 			//output is no directory?
 			if (!output.isDirectory()){
 				throw new IOException("Output dir "+outputDir+" is not a directory");
 			}
 			//output is not writable
 			if (!output.canWrite()){
 				throw new IOException("Output dir "+outputDir+" is not writable");
 			}
 			//output exist, it is a dir and it is writable
 			return true;
 		}else{ 
 			//outputdir doesn't exist
 			if (output.mkdirs()){
 				output.setWritable(true);
 				// directory is created, is it writable
 				if (!output.canWrite()){
 					throw new IOException("Output dir "+outputDir+" is created, but is not writable");
 				}else{
 					//directory is created and is writable
 					return true;
 				}
 			} else {
 				//output dir cannot be created
 				throw new IOException("Output dir "+outputDir+" cannot be created");
 			}
 		}
 	}
 		/**
 	 * @param configFilename
 	 * @return	file with the reportconfiguration (one at this moment)
 	 *
 	 */
 	public static Map<String,String> getReportControlFile(String configFileLocation, String apiKey){
 		//define return variable
 		Map<String,String> reportConfig = new HashMap<String,String>();
 		try {
 				//begin new method (get info from xml location?)
 				//create dom doc object
 				URL url;
 				File inputFile=new File(configFileLocation);
 				if (inputFile.canRead()){
 					//we can read the configFile, let us parse it as file
 					url =new URL(inputFile.toURI().toURL().toString());
 				}else {
 					//we cannot read the configfile, let us assume it is a valid URI
 					//TODO add some test to determin if it is a valid uri.
 					url = new URL(configFileLocation+"/api_key/"+apiKey);
 				}
 				InputStream is=url.openStream();
 				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 				//don't validate dtd at w3.org (if for some reason we would)
 				dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
 				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 				Document doc = dBuilder.parse(is);
 										
 				//read config using xpath
 				String searchConfig="/reportConfig/*";
 				XPathFactory factory = XPathFactory.newInstance();
 				XPath xpath = factory.newXPath();
 				XPathExpression expr = null;
 				expr = xpath.compile(searchConfig);
 				Object reportConfigResult = expr.evaluate(doc, XPathConstants.NODESET);
 				// end new method?
 				NodeList resultNodes = (NodeList) reportConfigResult;
 							
 				if (resultNodes.getLength()>0) {
 					for (int i = 0; i < resultNodes.getLength(); i++) {
 						// put the next control parameter in the return variable
 						Node resultNode=resultNodes.item(i);
 						reportConfig.put(Utils.removeEol(resultNode.getNodeName()), Utils.removeEol(resultNode.getTextContent()));
 					}
 				} else {
 					// not able to read config
 					String errorMessage="Cannot access config information";
 					throw new AccessControlException(errorMessage);
 				}
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (AccessControlException e) {
 				e.printStackTrace();
 			} catch (XPathExpressionException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (ParserConfigurationException e) {
 				e.printStackTrace();
 			} catch (SAXException e) {
 				e.printStackTrace();
 			}
 		return reportConfig;
 	}
 }
