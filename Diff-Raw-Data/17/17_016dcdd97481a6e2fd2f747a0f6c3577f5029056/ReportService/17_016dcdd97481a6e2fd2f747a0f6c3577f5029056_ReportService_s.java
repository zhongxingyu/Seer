 package it.dariofabbri.accesscontrol.service.local.report;
 
 import java.net.URL;
 import java.sql.Connection;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.sql.DataSource;
 
 import net.sf.jasperreports.engine.JasperExportManager;
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.JasperReport;
 import net.sf.jasperreports.engine.util.JRLoader;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ReportService {
 
 	private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
 
 	public byte[] generateReport(String report) {
 
 		return generateReport(report, new HashMap<String, Object>());
 	}
 	
 	public byte[] generateReport(String report, Map<String, Object> parameters) {
 
 		byte[] result = null;
 		
 		URL url = this.getClass().getClassLoader().getResource(report);
 		try {
 			
 			// Retrieve database connection using configured pool.
 			//
 			Context initContext = new InitialContext();
 			Context envContext  = (Context)initContext.lookup("java:/comp/env");
 			DataSource ds = (DataSource)envContext.lookup("jdbc/actl");
 			Connection connection = ds.getConnection();
 		
 			// Load report.
 			//
 			JasperReport jr = (JasperReport) JRLoader.loadObject(url);
 			
 			// Fill report using provided parameters and acquired
 			// database connection.
 			//
 			JasperPrint jp = JasperFillManager.fillReport(
 					jr, 
 					parameters, 
 					connection);
 			
 			// Generate PDF.
 			//
 			result = JasperExportManager.exportReportToPdf(jp);
 			
 			// Release JDBC connection.
 			//
 			connection.close();
 			
 		} catch (Exception e) {
 			logger.error("Exception caught while generating report.", e);
 			throw new RuntimeException("Exception caught while generating report.", e);
 		}
 
 		return result;
 	}
 }
