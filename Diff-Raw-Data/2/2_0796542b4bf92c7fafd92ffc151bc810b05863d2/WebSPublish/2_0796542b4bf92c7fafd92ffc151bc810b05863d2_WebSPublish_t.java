 package export;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.security.AlgorithmParameters;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.crypto.Cipher;
 import javax.crypto.spec.IvParameterSpec;
 import javax.crypto.spec.SecretKeySpec;
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 
 import net.sf.jasperreports.engine.JRExporterParameter;
 import net.sf.jasperreports.engine.JasperCompileManager;
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.JasperReport;
 import net.sf.jasperreports.engine.data.JRXmlDataSource;
 import net.sf.jasperreports.engine.export.JRXlsExporter;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 import org.w3c.dom.Document;
 
 import com.sforce.soap.partner.Connector;
 import com.sforce.soap.partner.PartnerConnection;
 import com.sforce.soap.partner.QueryResult;
 import com.sforce.soap.partner.sobject.SObject;
 import com.sforce.ws.ConnectorConfig;
 
 import export.Base64;
 
 public class WebSPublish extends HttpServlet {
 
     List<String> filterProducts = new ArrayList<String>();
     
 	@Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp)
             throws ServletException, IOException { 		
 		
 	System.out.println(req.getParameter("user") + "\n\n " + req.getParameter("pass"));
 	/*String USERNAME = req.getParameter("user");
 	String PASSWORD = req.getParameter("pass");
 	String type = req.getParameter("type");
 	String reportID = req.getParameter("reportID");*/
 	
 	// Get the parameters
 	String EncrypetedUSERNAME = req.getParameter("user");
 	String EncryptedPASSWORDTOKENKEY = req.getParameter("pass");	
 	String type = req.getParameter("type");
 	String reportID = req.getParameter("reportID");
 	
 	// Get Token, Password and cryptoKey
 	Integer tokenlength = Integer.valueOf(EncryptedPASSWORDTOKENKEY.substring(EncryptedPASSWORDTOKENKEY.length() - 2));
 	Integer passlength = Integer.valueOf(EncryptedPASSWORDTOKENKEY.substring(EncryptedPASSWORDTOKENKEY.length() - 4,EncryptedPASSWORDTOKENKEY.length() - 2));
 	String key = EncryptedPASSWORDTOKENKEY.substring(passlength + tokenlength , EncryptedPASSWORDTOKENKEY.length() - 4);
 	String EncryptedPASSWORD = EncryptedPASSWORDTOKENKEY.substring(0 , passlength);
 	String EncryptedTOKEN = EncryptedPASSWORDTOKENKEY.substring(passlength , passlength + tokenlength);
 	
 	System.out.println("######################## Encrypted CREDENTIALS ########################");
 	System.out.println("EncryptedPASSWORDTOKENKEY ---->" + EncryptedPASSWORDTOKENKEY);
 	System.out.println("KEY ---->" + key);
 	System.out.println("EncryptedUSERNAME ---->" + EncrypetedUSERNAME);
 	System.out.println("EcryptedPASSWORD ---->" + EncryptedPASSWORD);
 	System.out.println("EncryptedTOKEN ---->" + EncryptedTOKEN);
 	
 	PartnerConnection connection;
 	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
 	
 	
 	try { 		
 		SecretKeySpec secretkey = new SecretKeySpec(Util.hexStringToByteArray(key), "AES");
 		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
 		cipher.init(Cipher.DECRYPT_MODE, secretkey, new IvParameterSpec(Util.hexStringToByteArray(key)));
 		
 		System.out.println("######################## Decrypting CREDENTIALS ########################");			
 				
 		byte[] decodedUsername = Util.hexStringToByteArray(EncrypetedUSERNAME);
 		String USERNAME = new String(cipher.doFinal(decodedUsername));
 		System.out.println("USERNAME ---->" + USERNAME);
 		
 		String PASSWORD = new String(cipher.doFinal(Util.hexStringToByteArray(EncryptedPASSWORD)));
 		System.out.println("PASSWORD ---->" + PASSWORD);
 		
 		String TOKEN = new String(cipher.doFinal(Util.hexStringToByteArray(EncryptedTOKEN)));
 		System.out.println("TOKEN ---->" + TOKEN);
 		
 		ConnectorConfig config = new ConnectorConfig();
 	    config.setUsername(USERNAME);
 	    //config.setPassword(PASSWORD);
 	    config.setPassword(PASSWORD + TOKEN);
 	    
 	    connection = Connector.newConnection(config);   
 	    QueryResult queryResults =  connection.query("SELECT o.Row_HTML__c, o.Object_Export_Excel__c FROM Object_Row__c o WHERE o.Object_Export_Excel__c = '" + reportID + "' ORDER BY o.name");	
 	    
 	    System.out.println("######################## START ########################");
 	    
 	    String xml = "<root>";	
 	    for (SObject s : queryResults.getRecords()) { 
 	    	
 	    	System.out.println("######################## Looping ########################");
 	    	if(s.getField("Row_HTML__c") != null){
 	    		xml += s.getField("Row_HTML__c").toString();	
 	    	}	    	
 		}
 	    
 	    xml += "</root>";	    
 	    
 	    System.out.println("######################## XML ########################");
 	    
 	    Document xmlOutput = Util.xmlFormat(xml.replaceAll("&", " "));
 	    
 	    System.out.println("XML --------------------------------------->" + xmlOutput); 
 	    /* JasperPrint is the object contains
 		report after result filling process */
 		JasperPrint jasperPrint = null;
 	    
 	    if (type.equals("NonPerformer")) {
 		    // Create Data source
 			JRXmlDataSource xmlDataSource = new JRXmlDataSource(xmlOutput, "root/NonPerformingAccounts/Row");	
 			
 			// Complie Template to .jasper
 			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
 			JasperReport jasperReport = JasperCompileManager.compileReport(classLoader.getResourceAsStream("NonPerformingAccounts.jrxml"));
 			
 			// Compilamos el sub reporte
 			JasperReport jasperSubReport = JasperCompileManager.compileReport(classLoader.getResourceAsStream("NonPerformingAccounts_subreport1.jrxml"));				 	
 			
 			Map<String, Object> param = new HashMap<String, Object>();
 			param.put("SubReportParam", jasperSubReport);
 			
 			// filling report with data from data source
 			jasperPrint = JasperFillManager.fillReport(jasperReport,param,xmlDataSource);
 	    } else if (type.equals("productionBySalesperson")){
 	    	JRXmlDataSource xmlDataSource = new JRXmlDataSource(xmlOutput, "root/ProductionReportSalespeople/Row");	
 			
 			// Complie Template to .jasper
 			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
 			JasperReport jasperReport = JasperCompileManager.compileReport(classLoader.getResourceAsStream("productionBySalesperson.jrxml"));
 			
 			// Compilamos el sub reporte
 			JasperReport jasperSubReport = JasperCompileManager.compileReport(classLoader.getResourceAsStream("productionBySalesperson_subreport1.jrxml"));
 			JasperReport jasperSubReport2 = JasperCompileManager.compileReport(classLoader.getResourceAsStream("productionBySalesperson_subreport2.jrxml"));
 			
 			Map<String, Object> param = new HashMap<String, Object>();
 			param.put("SubReportParam", jasperSubReport);
 			param.put("SubReportParam2", jasperSubReport2);
 			
 			// filling report with data from data source
 			jasperPrint = JasperFillManager.fillReport(jasperReport,param,xmlDataSource);
 	    	
 	    } else if (type.equals("productionByBook")){
 	    	JRXmlDataSource xmlDataSource = new JRXmlDataSource(xmlOutput, "root/ProductionReportBook/Row");	
 	    	System.out.println("###################Book########################");
 			// Complie Template to .jasper
 			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
 			JasperReport jasperReport = JasperCompileManager.compileReport(classLoader.getResourceAsStream("productionByBook.jrxml"));
 			
 			// Compilamos el sub reporte
 			JasperReport jasperSubReport = JasperCompileManager.compileReport(classLoader.getResourceAsStream("productionByBook_subreport1.jrxml"));
 			
 			Map<String, Object> param = new HashMap<String, Object>();
 			param.put("SubReportParam", jasperSubReport);
 			
 			// filling report with data from data source
 			jasperPrint = JasperFillManager.fillReport(jasperReport,param,xmlDataSource);
 	    	
 	    } else if (type.equals("productionByProduct")){
 	    	JRXmlDataSource xmlDataSource = new JRXmlDataSource(xmlOutput, "root/ProductionReportProductType/Row");
 	    	JRXmlDataSource SubDataSource = new JRXmlDataSource(xmlOutput, "root/ProductionReportProductType/Row/Product");
 			
 			// Complie Template to .jasper
 			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
 			JasperReport jasperReport = JasperCompileManager.compileReport(classLoader.getResourceAsStream("productionByProduct.jrxml"));
 			
 			// Compilamos el sub reporte
 			JasperReport jasperSubReport = JasperCompileManager.compileReport(classLoader.getResourceAsStream("productionByProduct_subreport1.jrxml"));
 			
 			Map<String, Object> param = new HashMap<String, Object>();
 			param.put("SubReportParam", jasperSubReport);
 			param.put("SubDataSource", SubDataSource);
 			
 			// filling report with data from data source
 			jasperPrint = JasperFillManager.fillReport(jasperReport,param,xmlDataSource);
 	    	
 	    } else if (type.equals("coverage")){
 	    	JRXmlDataSource xmlDataSource = new JRXmlDataSource(xmlOutput, "root/conflicts/conflict");	
 			
 			// Complie Template to .jasper
 			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
 			JasperReport jasperReport = JasperCompileManager.compileReport(classLoader.getResourceAsStream("coverage.jrxml"));
 						
 			// filling report with data from data source
 			jasperPrint = JasperFillManager.fillReport(jasperReport,null,xmlDataSource);
 	    	
 	    }			
 		
 		//resp.setContentType("application/vnd.ms-excel");
 	    resp.setHeader("content-type","application/vnd.ms-excel#report.xls");
 		resp.setContentType("application/x-msdownload");
 		resp.setHeader("Content-Disposition",
 				 "attachment; filename=report.xls"); 
 		resp.setDateHeader ("Expires", 0);
 		
 		// exports to xls file
 		JRXlsExporter exporterXls = new JRXlsExporter ();
 		exporterXls.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
 		exporterXls.setParameter(JRExporterParameter.OUTPUT_STREAM, byteArrayOutputStream); 
 		exporterXls.exportReport();		
 		
 		System.out.println(Base64.encodeBytes(byteArrayOutputStream.toByteArray()).getBytes());		
 		resp.getOutputStream().write(Base64.encodeBytes(byteArrayOutputStream.toByteArray()).getBytes());
 		System.out.println("######################## Finish ########################");
 		
 		} catch (Exception e) {			
 			e.printStackTrace();			
 		}		
     }
 	
 	@Override	
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		System.out.println(req.getParameter("user") + "\n\n " + req.getParameter("pass"));
 		String USERNAME = req.getParameter("user");
 		String PASSWORD = req.getParameter("pass");
 		String type = req.getParameter("type");
 		String reportID = req.getParameter("reportID");		
 		
 		PartnerConnection connection;
 		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
 		
 		
 		try {			
 			ConnectorConfig config = new ConnectorConfig();
 		    config.setUsername(USERNAME);
 		    config.setPassword(PASSWORD);
 		    
 		    
 		    connection = Connector.newConnection(config);   
 		    QueryResult queryResults =  connection.query("SELECT o.Row_HTML__c, o.Object_Export_Excel__c FROM Object_Row__c o WHERE o.Object_Export_Excel__c = '" + reportID + "' ORDER BY o.name");	
 		    		    
 		    System.out.println("######################## START ########################");
 		    
 		    String xml = "<root>";	
 		    for (SObject s : queryResults.getRecords()) { 
 		    	if(s.getField("Row_HTML__c") != null){
 		    		xml += s.getField("Row_HTML__c").toString();
 		    	}	    	
 			}
 		    		    
 	    	System.out.println("######################## Finish Looping ########################");
 		    xml += "</root>";	    
 		    
 		    Document xmlOutput = Util.xmlFormat(xml.replaceAll("&", " "));
 		    
 		    /* JasperPrint is the object contains
 			report after result filling process */
 			JasperPrint jasperPrint = null;
 		    
 		    if (type.equals("NonPerformer")) {
 			    // Create Data source
 				JRXmlDataSource xmlDataSource = new JRXmlDataSource(xmlOutput, "root/NonPerformingAccounts/Row");	
 				
 				// Complie Template to .jasper
 				ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
 				JasperReport jasperReport = JasperCompileManager.compileReport(classLoader.getResourceAsStream("NonPerformingAccounts.jrxml"));
 				
 				// Compilamos el sub reporte
 				JasperReport jasperSubReport = JasperCompileManager.compileReport(classLoader.getResourceAsStream("NonPerformingAccounts_subreport1.jrxml"));				 	
 				
 				Map<String, Object> param = new HashMap<String, Object>();
 				param.put("SubReportParam", jasperSubReport);
 				
 				// filling report with data from data source
 				jasperPrint = JasperFillManager.fillReport(jasperReport,param,xmlDataSource);
 		    } else if (type.equals("productionBySalesperson")){
 		    	JRXmlDataSource xmlDataSource = new JRXmlDataSource(xmlOutput, "root/ProductionReportSalespeople/Row");	
 				
 				// Complie Template to .jasper
 				ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
 				JasperReport jasperReport = JasperCompileManager.compileReport(classLoader.getResourceAsStream("productionBySalesperson.jrxml"));
 				
 				// Compilamos el sub reporte
 				JasperReport jasperSubReport = JasperCompileManager.compileReport(classLoader.getResourceAsStream("productionBySalesperson_subreport1.jrxml"));
 				JasperReport jasperSubReport2 = JasperCompileManager.compileReport(classLoader.getResourceAsStream("productionBySalesperson_subreport2.jrxml"));
 				
 				Map<String, Object> param = new HashMap<String, Object>();
 				param.put("SubReportParam", jasperSubReport);
 				param.put("SubReportParam2", jasperSubReport2);
 				
 				// filling report with data from data source
 				jasperPrint = JasperFillManager.fillReport(jasperReport,param,xmlDataSource);
 		    	
 		    } else if (type.equals("productionByBook")){
 		    	JRXmlDataSource xmlDataSource = new JRXmlDataSource(xmlOutput, "root/ProductionReportBook/Row");	
 		    	System.out.println("###################Book########################");
 				// Complie Template to .jasper
 				ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
 				JasperReport jasperReport = JasperCompileManager.compileReport(classLoader.getResourceAsStream("productionByBook.jrxml"));
 				
 				// Compilamos el sub reporte
 				JasperReport jasperSubReport = JasperCompileManager.compileReport(classLoader.getResourceAsStream("productionByBook_subreport1.jrxml"));
 				
 				Map<String, Object> param = new HashMap<String, Object>();
 				param.put("SubReportParam", jasperSubReport);
 				
 				// filling report with data from data source
 				jasperPrint = JasperFillManager.fillReport(jasperReport,param,xmlDataSource);
 		    	
 		    } else if (type.equals("productionByProduct")){
 		    	JRXmlDataSource xmlDataSource = new JRXmlDataSource(xmlOutput, "root/ProductionReportProductType/Row");
 		    	JRXmlDataSource SubDataSource = new JRXmlDataSource(xmlOutput, "root/ProductionReportProductType/Row/Product");
 				
 				// Complie Template to .jasper
 				ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
 				JasperReport jasperReport = JasperCompileManager.compileReport(classLoader.getResourceAsStream("productionByProduct.jrxml"));
 				
 				// Compilamos el sub reporte
 				JasperReport jasperSubReport = JasperCompileManager.compileReport(classLoader.getResourceAsStream("productionByProduct_subreport1.jrxml"));
 				
 				Map<String, Object> param = new HashMap<String, Object>();
 				param.put("SubReportParam", jasperSubReport);
 				param.put("SubDataSource", SubDataSource);
 				
 				// filling report with data from data source
 				jasperPrint = JasperFillManager.fillReport(jasperReport,param,xmlDataSource);
 		    	
 		    } else if (type.equals("coverage")){
 		    	JRXmlDataSource xmlDataSource = new JRXmlDataSource(xmlOutput, "root/conflicts/conflict");	
 				
 				// Complie Template to .jasper
 				ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
 				JasperReport jasperReport = JasperCompileManager.compileReport(classLoader.getResourceAsStream("coverage.jrxml"));
 							
 				// filling report with data from data source
 				jasperPrint = JasperFillManager.fillReport(jasperReport,null,xmlDataSource);
 		    	
 		    }			
 			
 			//resp.setContentType("application/vnd.ms-excel");
 			resp.setHeader("content-type","application/vnd.ms-excel#report.xls");
 			resp.setContentType("application/x-msdownload");
 			resp.setHeader("Content-Disposition",
 					 "attachment; filename=report.xls"); 
 			resp.setDateHeader ("Expires", 0);
 			
 			// exports to xls file
 			JRXlsExporter exporterXls = new JRXlsExporter ();
 			exporterXls.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
 			exporterXls.setParameter(JRExporterParameter.OUTPUT_STREAM, resp.getOutputStream());//byteArrayOutputStream); 
 			exporterXls.exportReport();		
 			
 			//System.out.println(Base64.encodeBytes(byteArrayOutputStream.toByteArray()).getBytes());		
 			//resp.getOutputStream().write(byteArrayOutputStream.toByteArray());
 			System.out.println("######################## Finish ########################");
 			
 			} catch (Exception e) {			
 				e.printStackTrace();			
 		}		
 		
 	}	
 	
 	public static void main(String[] args) throws Exception {
 		
 		Server server = new Server(Integer.valueOf(System.getenv("PORT")));		
 		
 		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
         context.setContextPath("/");
        System.out.println("Max inactive interval ------------------------------------->" + context.getSessionHandler().getSessionManager().getMaxInactiveInterval());
        context.getSessionHandler().getSessionManager().setMaxInactiveInterval(300);
         server.setHandler(context);
         
         context.addServlet(new ServletHolder(new WebSPublish()),"/toxls");
         
         server.start();
         server.join(); 		
 	}
 	
 	
 }
