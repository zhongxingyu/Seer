 package controllers;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import net.sf.jasperreports.engine.JRParameter;
 import net.sf.jasperreports.engine.JasperCompileManager;
 import net.sf.jasperreports.engine.JasperExportManager;
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
 import play.mvc.Result;
 import play.mvc.Results;
 
 public class ReportController {
 
 	static String REPORT_DEFINITION_PATH = "./app/reports/";
 
 	public static Result jasperDocument(String reportFile, Map reportParams, JRBeanCollectionDataSource ds) {
 		OutputStream os = new ByteArrayOutputStream();
 		try {
 			// Sets the default report locale to pt_BR
 			Locale localeBR = new Locale("pt", "BR");
 			if(reportParams == null) {
 				reportParams = new HashMap<String, Object>();
 			}
 			reportParams.put(JRParameter.REPORT_LOCALE, localeBR);
			System.out.println(			java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.FULL, java.text.DateFormat.LONG, localeBR).format(new java.util.Date()) );
 			
 			String compiledFile = REPORT_DEFINITION_PATH + reportFile + ".jasper";
 			JasperCompileManager.compileReportToFile(REPORT_DEFINITION_PATH + reportFile + ".jrxml", compiledFile);
 			JasperPrint jasperPrint = JasperFillManager.fillReport(compiledFile, reportParams, ds);
 			JasperExportManager.exportReportToPdfStream(jasperPrint, os);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return Results.ok(new ByteArrayInputStream(((ByteArrayOutputStream) os).toByteArray())).as("application/pdf");
 	}
 
 }
