 package com.chuckanutbay.reportgeneration;
 
 import java.io.File;
 import java.io.InputStream;
 
 import net.sf.jasperreports.engine.JRException;
 import net.sf.jasperreports.engine.JasperCompileManager;
 import net.sf.jasperreports.engine.JasperExportManager;
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.JasperReport;
 
 import org.joda.time.DateTime;
 import org.slf4j.LoggerFactory;
 
 import com.chuckanutbay.businessobjects.util.HibernateUtil;
 import com.chuckanutbay.webapp.common.server.Timer;
 import com.chuckanutbay.webapp.common.shared.ReportDto;
 import com.google.common.io.Files;
 
 public class ReportGenerator {
 	
 	private static final File generatedReportsDir = Files.createTempDir();
 	
 	public static String generateReport(ReportDto report) {
 		try {
 			//Setup Timer
 			Timer timer = new Timer(LoggerFactory.getLogger(ReportGenerator.class.getName())).start("Generating Report:");
 			
 			//Compile report
 			String reportPath = report.getUncompiledFilePath();
 			JasperReport compiledReport = JasperCompileManager.compileReport(asInputStream(reportPath));
 			timer.logTime("\tCopiled report:");
 			
 			//Compile subreport
 			if (report.getParameters().containsKey("SUBREPORT")) {
 				Object subreport = report.getParameters().get("SUBREPORT");
 				if (subreport instanceof ReportDto) {
 					ReportDto subreportAsReportDto = (ReportDto) subreport;
 					JasperReport compiledSubreport = JasperCompileManager.compileReport(asInputStream(subreportAsReportDto.getUncompiledFilePath()));
 					report.addParameter("SUBREPORT", compiledSubreport);
 				}
 			}
 			
 			//Fill the report
 			HibernateUtil.beginTransaction();
 			JasperPrint jasperPrint = JasperFillManager.fillReport(compiledReport, report.getParameters(), HibernateUtil.getSession().connection());
 			timer.logTime("Pages: " + jasperPrint.getPages().size());
 			timer.logTime("\tFilled report:");
 			
 			//Export filled report as a PDF
 			String pdfFilePath = new File(generatedReportsDir, "generatedReport" + new DateTime().getMillis() + ".pdf").getAbsolutePath();
 			JasperExportManager.exportReportToPdfFile(jasperPrint, pdfFilePath);
 			timer.logTime("\tGenerated PDF:");
 			return pdfFilePath;
 		} catch (JRException e) {
 			throw new RuntimeException(e);
 		} finally {
			//Steve suggested this originally, but it causes org.hibernate.TransactionException: Transaction not successfully started
			//HibernateUtil.closeSession();
 		}
 	}
 	
 	private static InputStream asInputStream(String filePath) {
 		InputStream reportStream = ReportGenerator.class.getClassLoader().getResourceAsStream(filePath);
 		if (reportStream == null) {
 			throw new IllegalStateException("No report could be found at " + filePath + ".  Did you run " + CompileReports.class.getName() + "?");
 		}
 		return reportStream;
 	}
 }
