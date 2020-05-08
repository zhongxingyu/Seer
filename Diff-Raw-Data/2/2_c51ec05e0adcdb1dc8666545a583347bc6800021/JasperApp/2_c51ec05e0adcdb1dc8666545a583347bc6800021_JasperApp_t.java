 import java.awt.Image;
 import java.awt.MediaTracker;
 import java.awt.Panel;
 import java.awt.Toolkit;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 
 import net.sf.jasperreports.engine.JRException;
 import net.sf.jasperreports.engine.JRExporterParameter;
 import net.sf.jasperreports.engine.JasperExportManager;
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.JasperPrintManager;
 import net.sf.jasperreports.engine.JasperRunManager;
 import net.sf.jasperreports.engine.JasperReport;
 import net.sf.jasperreports.engine.JRDataSource;
 import net.sf.jasperreports.engine.export.JExcelApiExporter;
 import net.sf.jasperreports.engine.export.JExcelApiMetadataExporter;
 import net.sf.jasperreports.engine.export.JRCsvExporter;
 import net.sf.jasperreports.engine.export.JRCsvMetadataExporter;
 import net.sf.jasperreports.engine.export.JRPdfExporter;
 import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
 import net.sf.jasperreports.engine.export.JRRtfExporter;
 import net.sf.jasperreports.engine.export.JRXhtmlExporter;
 import net.sf.jasperreports.engine.export.JRXlsExporter;
 import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
 import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
 import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
 import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
 import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter;
 import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
 import net.sf.jasperreports.engine.util.AbstractSampleApp;
 import net.sf.jasperreports.engine.util.JRLoader;
 import net.sf.jasperreports.engine.util.AbstractSampleApp;
 import net.sf.jasperreports.engine.util.JRLoader;
 import net.sf.jasperreports.engine.util.JRSaver;
 
 import mutil.base.ExceptionAdapter;
 
 
 public class JasperApp
 {
 	
 	public static void main(String[] args) throws JRException {
             switch (args[0]) {
                 case "pdf": pdf(args[1]); 
                     break;
                 default:
                     throw new RuntimeException("unrecognized case");
             }
             System.err.println("exiting "+JasperApp.class.getName()+"#main");
 	}
 
         private static String className         () { return JasperApp.class.getName() ; }
         private static String reportCoreName    () { return "report2"             ; }
         private static String compiledReportName() { return reportCoreName()+".jasper"; }
         private static String pdfReportName     () { return reportCoreName()+".pdf"   ; }
 
 
 	private static Connection getConnection() throws JRException {
 		Connection conn;
 		try {
 			String driver        = "org.postgresql.Driver";
 			String connectString = "jdbc:postgresql://localhost:5432/cashflow";
 			String user          = "gaia-user";
 			String password      = "gaia-user-pwd";
 			Class.forName(driver); // I suppose that's some kind of early failure
 			conn = DriverManager.getConnection(connectString, user, password);
 		}
 		catch (ClassNotFoundException e) {
 			throw new JRException(e);
 		}
 		catch (SQLException e) {
 			throw new JRException(e);
 		}
 		return conn;
 	}
 
         private static File fill() throws JRException {
 		long start = System.currentTimeMillis();
                 String sourceFileLocation = "reports/"+compiledReportName();
 		System.err.println(" sourceFileLocation : " + sourceFileLocation);
 		JasperReport jasperReport = (JasperReport)JRLoader.loadObjectFromLocation(sourceFileLocation);
 
 
                 Map parameters = null;
                 {
 		Image image = 
 			Toolkit.getDefaultToolkit().createImage(
 				JRLoader.loadBytesFromResource("dukesign.jpg")
 				);
 		MediaTracker traker = new MediaTracker(new Panel());
 		traker.addImage(image, 0);
 		try
 		{
 			traker.waitForID(0);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		parameters = new HashMap();
                 /*		parameters.put("ReportTitle", "The First Jasper Report Ever");
 		parameters.put("MaxOrderID", new Integer(10500));
 		parameters.put("SummaryImage", image); */
                parameters.put("p_cafl_id", 5);
                 //parameters.put("p_tg_type", 0);
                 }
 
 		
 		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, getConnection());
                 File destTempFile = null;
                 try {
                     destTempFile = File.createTempFile("jasper-"+className(), ".jrprint");
                 } catch (IOException e) {
                     throw new ExceptionAdapter(e);
                 }
 		JRSaver.saveObject(jasperPrint, destTempFile);
 		System.err.println("Filling time : " + (System.currentTimeMillis() - start));
                 return destTempFile;
 	}
 
        
         // the below method actually prints in a printer (I should test it at home - but is now broken cause the .jrprint
         // file is located in a temporary folder - to fix it I'll have to change its signature like the pdf() method
 	public static void print() throws JRException {
 		long start = System.currentTimeMillis();
 		JasperPrintManager.printReport("build/reports/"+className()+"Report.jrprint", true);
 		System.err.println("Printing time : " + (System.currentTimeMillis() - start));
 	}
 
 	
 	public static void pdf(String whereToProducePDF) throws JRException {
                 File jrprintFile = fill();
 		long start = System.currentTimeMillis();
 		JasperExportManager.exportReportToPdfFile(jrprintFile.getAbsolutePath(), whereToProducePDF+"/"+pdfReportName());
 		System.err.println("PDF creation time : " + (System.currentTimeMillis() - start));
 	}
 }
 
 /*            
 public class JasperApp extends AbstractSampleApp
 {
 
 
 	public static void main(String[] args) 
 	{
 		main(new JasperApp(), args);
 	}
 	
 	
 
 	public void test() throws JRException
 	{
 		fill();
 		pdf();
 		xmlEmbed();
 		xml();
 		html();
 		rtf();
 		xls();
 		jxl();
 		csv();
 		csvMetadata();
 		jxlMetadata();
 		odt();
 		ods();
 		docx();
 		xlsx();
 		pptx();
 		xhtml();
 	}
 	
 	
 	public void fill() throws JRException
 	{
 		long start = System.currentTimeMillis();
 		//Preparing parameters
 		Image image = 
 			Toolkit.getDefaultToolkit().createImage(
 				JRLoader.loadBytesFromResource("dukesign.jpg")
 				);
 		MediaTracker traker = new MediaTracker(new Panel());
 		traker.addImage(image, 0);
 		try
 		{
 			traker.waitForID(0);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		Map parameters = new HashMap();
 		parameters.put("ReportTitle", "The First Jasper Report Ever");
 		parameters.put("MaxOrderID", new Integer(10500));
 		parameters.put("SummaryImage", image);
 		
 		JasperFillManager.fillReportToFile("build/reports/FirstJasper.jasper", parameters, getDemoHsqldbConnection());
 		System.err.println("Filling time : " + (System.currentTimeMillis() - start));
 	}
 	
 	
 	public void print() throws JRException
 	{
 		long start = System.currentTimeMillis();
 		JasperPrintManager.printReport("build/reports/FirstJasper.jrprint", true);
 		System.err.println("Printing time : " + (System.currentTimeMillis() - start));
 	}
 	
 	
 	public void pdf() throws JRException
 	{
 		long start = System.currentTimeMillis();
 		JasperExportManager.exportReportToPdfFile("build/reports/FirstJasper.jrprint");
 		System.err.println("PDF creation time : " + (System.currentTimeMillis() - start));
 	}
 	
 	
 	public void pdfa1() throws JRException
 	{
 		long start = System.currentTimeMillis();
 
 		try{
 			ByteArrayOutputStream os = new ByteArrayOutputStream();
 
 			JRPdfExporter exporter = new JRPdfExporter();
 			exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, os);
 			
 			JasperPrint jp = (JasperPrint)JRLoader.loadObject(new File("build/reports/FirstJasper.jrprint"));
 			
 			// Exclude transparent images when exporting to PDF; elements marked with the key 'TransparentImage'
 			// will be excluded from the exported PDF
 			jp.setProperty("net.sf.jasperreports.export.pdf.exclude.key.TransparentImage", null);
 			
 			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jp);
 			
 			// Include structure tags for PDF/A-1a compliance; unnecessary for PDF/A-1b
 			exporter.setParameter(JRPdfExporterParameter.IS_TAGGED, Boolean.TRUE);
 			
 			exporter.setParameter(JRPdfExporterParameter.PDFA_CONFORMANCE, JRPdfExporterParameter.PDFA_CONFORMANCE_1A);
 			
 			// Uncomment the following line and specify a valid path for the ICC profile
 //			exporter.setParameter(JRPdfExporterParameter.PDFA_ICC_PROFILE_PATH, "path/to/ICC/profile");
 			
 			exporter.exportReport();
 
 			FileOutputStream fos = new FileOutputStream("build/reports/FirstJasper_pdfa.pdf");
 			os.writeTo(fos);
 			fos.close();
 		}catch(Exception e){
 			 e.printStackTrace();
 		}
 				
 		System.err.println("PDF/A-1a creation time : " + (System.currentTimeMillis() - start));
 	}
 	
 	
 	public void xml() throws JRException
 	{
 		long start = System.currentTimeMillis();
 		JasperExportManager.exportReportToXmlFile("build/reports/FirstJasper.jrprint", false);
 		System.err.println("XML creation time : " + (System.currentTimeMillis() - start));
 	}
 	
 	
 	public void xmlEmbed() throws JRException
 	{
 		long start = System.currentTimeMillis();
 		JasperExportManager.exportReportToXmlFile("build/reports/FirstJasper.jrprint", true);
 		System.err.println("XML creation time : " + (System.currentTimeMillis() - start));
 	}
 	
 	
 	public void html() throws JRException
 	{
 		long start = System.currentTimeMillis();
 		JasperExportManager.exportReportToHtmlFile("build/reports/FirstJasper.jrprint");
 		System.err.println("HTML creation time : " + (System.currentTimeMillis() - start));
 	}
 	
 	
 	public void rtf() throws JRException
 	{
 		long start = System.currentTimeMillis();
 		File sourceFile = new File("build/reports/FirstJasper.jrprint");
 
 		JasperPrint jasperPrint = (JasperPrint)JRLoader.loadObject(sourceFile);
 
 		File destFile = new File(sourceFile.getParent(), jasperPrint.getName() + ".rtf");
 		
 		JRRtfExporter exporter = new JRRtfExporter();
 		
 		exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
 		exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, destFile.toString());
 		
 		exporter.exportReport();
 
 		System.err.println("RTF creation time : " + (System.currentTimeMillis() - start));
 	}
 	
 	
 	public void xls() throws JRException
 	{
 		long start = System.currentTimeMillis();
 		File sourceFile = new File("build/reports/FirstJasper.jrprint");
 
 		JasperPrint jasperPrint = (JasperPrint)JRLoader.loadObject(sourceFile);
 		
 		File destFile = new File(sourceFile.getParent(), jasperPrint.getName() + ".xls");
 		
 		Map dateFormats = new HashMap();
 		dateFormats.put("EEE, MMM d, yyyy", "ddd, mmm d, yyyy");
 
 		JRXlsExporter exporter = new JRXlsExporter();
 		
 		exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
 		exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, destFile.toString());
 		exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.TRUE);
 		exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
 		exporter.setParameter(JRXlsExporterParameter.FORMAT_PATTERNS_MAP, dateFormats);
 		
 		exporter.exportReport();
 
 		System.err.println("XLS creation time : " + (System.currentTimeMillis() - start));
 	}
 	
 	
 	public void jxl() throws JRException
 	{
 		long start = System.currentTimeMillis();
 		File sourceFile = new File("build/reports/FirstJasper.jrprint");
 
 		JasperPrint jasperPrint = (JasperPrint)JRLoader.loadObject(sourceFile);
 
 		File destFile = new File(sourceFile.getParent(), jasperPrint.getName() + ".jxl.xls");
 
 		Map dateFormats = new HashMap();
 		dateFormats.put("EEE, MMM d, yyyy", "ddd, mmm d, yyyy");
 
 		JExcelApiExporter exporter = new JExcelApiExporter();
 
 		exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
 		exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, destFile.toString());
 		exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.TRUE);
 		exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
 		exporter.setParameter(JRXlsExporterParameter.FORMAT_PATTERNS_MAP, dateFormats);
 
 		exporter.exportReport();
 
 		System.err.println("XLS creation time : " + (System.currentTimeMillis() - start));
 	}
 	
 	
 	public void jxlMetadata() throws JRException
 	{
 		long start = System.currentTimeMillis();
 		File sourceFile = new File("build/reports/FirstJasper.jrprint");
 
 		JasperPrint jasperPrint = (JasperPrint)JRLoader.loadObject(sourceFile);
 
 		File destFile = new File(sourceFile.getParent(), jasperPrint.getName() + ".jxl.metadata.xls");
 
 		Map dateFormats = new HashMap();
 		dateFormats.put("EEE, MMM d, yyyy", "ddd, mmm d, yyyy");
 
 		JExcelApiMetadataExporter exporter = new JExcelApiMetadataExporter();
 
 		exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
 		exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, destFile.toString());
 		exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.TRUE);
 		exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
 		exporter.setParameter(JRXlsExporterParameter.FORMAT_PATTERNS_MAP, dateFormats);
 
 		exporter.exportReport();
 
 		System.err.println("XLS creation time : " + (System.currentTimeMillis() - start));
 	}
 	
 	
 	public void csv() throws JRException
 	{
 		long start = System.currentTimeMillis();
 		File sourceFile = new File("build/reports/FirstJasper.jrprint");
 
 		JasperPrint jasperPrint = (JasperPrint)JRLoader.loadObject(sourceFile);
 
 		File destFile = new File(sourceFile.getParent(), jasperPrint.getName() + ".csv");
 		
 		JRCsvExporter exporter = new JRCsvExporter();
 		
 		exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
 		exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, destFile.toString());
 		
 		exporter.exportReport();
 
 		System.err.println("CSV creation time : " + (System.currentTimeMillis() - start));
 	}
 	
 	
 	public void csvMetadata() throws JRException
 	{
 		long start = System.currentTimeMillis();
 		File sourceFile = new File("build/reports/FirstJasper.jrprint");
 
 		JasperPrint jasperPrint = (JasperPrint)JRLoader.loadObject(sourceFile);
 
 		File destFile = new File(sourceFile.getParent(), jasperPrint.getName() + ".metadata.csv");
 		
 		JRCsvMetadataExporter exporter = new JRCsvMetadataExporter();
 		
 		exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
 		exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, destFile.toString());
 		
 		exporter.exportReport();
 
 		System.err.println("CSV creation time : " + (System.currentTimeMillis() - start));
 	}
 	
 	
 	public void odt() throws JRException
 	{
 		long start = System.currentTimeMillis();
 		File sourceFile = new File("build/reports/FirstJasper.jrprint");
 
 		JasperPrint jasperPrint = (JasperPrint)JRLoader.loadObject(sourceFile);
 
 		File destFile = new File(sourceFile.getParent(), jasperPrint.getName() + ".odt");
 
 		JROdtExporter exporter = new JROdtExporter();
 
 		exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
 		exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, destFile.toString());
 
 		exporter.exportReport();
 
 		System.err.println("ODT creation time : " + (System.currentTimeMillis() - start));
 	}
 	
 	
 	public void ods() throws JRException
 	{
 		long start = System.currentTimeMillis();
 		File sourceFile = new File("build/reports/FirstJasper.jrprint");
 
 		JasperPrint jasperPrint = (JasperPrint)JRLoader.loadObject(sourceFile);
 
 		File destFile = new File(sourceFile.getParent(), jasperPrint.getName() + ".ods");
 
 		JROdsExporter exporter = new JROdsExporter();
 
 		exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
 		exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, destFile.toString());
 
 		exporter.exportReport();
 
 		System.err.println("ODS creation time : " + (System.currentTimeMillis() - start));
 	}
 	
 	
 	public void docx() throws JRException
 	{
 		long start = System.currentTimeMillis();
 		File sourceFile = new File("build/reports/FirstJasper.jrprint");
 
 		JasperPrint jasperPrint = (JasperPrint)JRLoader.loadObject(sourceFile);
 
 		File destFile = new File(sourceFile.getParent(), jasperPrint.getName() + ".docx");
 
 		JRDocxExporter exporter = new JRDocxExporter();
 
 		exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
 		exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, destFile.toString());
 
 		exporter.exportReport();
 
 		System.err.println("DOCX creation time : " + (System.currentTimeMillis() - start));
 	}
 	
 	
 	public void xlsx() throws JRException
 	{
 		long start = System.currentTimeMillis();
 		File sourceFile = new File("build/reports/FirstJasper.jrprint");
 
 		JasperPrint jasperPrint = (JasperPrint)JRLoader.loadObject(sourceFile);
 
 		File destFile = new File(sourceFile.getParent(), jasperPrint.getName() + ".xlsx");
 
 		Map dateFormats = new HashMap();
 		dateFormats.put("EEE, MMM d, yyyy", "ddd, mmm d, yyyy");
 
 		JRXlsxExporter exporter = new JRXlsxExporter();
 
 		exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
 		exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, destFile.toString());
 		exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
 		exporter.setParameter(JRXlsExporterParameter.FORMAT_PATTERNS_MAP, dateFormats);
 
 		exporter.exportReport();
 
 		System.err.println("XLSX creation time : " + (System.currentTimeMillis() - start));
 	}
 	
 	
 	public void pptx() throws JRException
 	{
 		long start = System.currentTimeMillis();
 		File sourceFile = new File("build/reports/FirstJasper.jrprint");
 
 		JasperPrint jasperPrint = (JasperPrint)JRLoader.loadObject(sourceFile);
 
 		File destFile = new File(sourceFile.getParent(), jasperPrint.getName() + ".pptx");
 		
 		JRPptxExporter exporter = new JRPptxExporter();
 		
 		exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
 		exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, destFile.toString());
 
 		exporter.exportReport();
 
 		System.err.println("PPTX creation time : " + (System.currentTimeMillis() - start));
 	}
 	
 	
 	public void xhtml() throws JRException
 	{
 		long start = System.currentTimeMillis();
 		File sourceFile = new File("build/reports/FirstJasper.jrprint");
 
 		JasperPrint jasperPrint = (JasperPrint)JRLoader.loadObject(sourceFile);
 
 		File destFile = new File(sourceFile.getParent(), jasperPrint.getName() + ".x.html");
 
 		JRXhtmlExporter exporter = new JRXhtmlExporter();
 
 		exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
 		exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, destFile.toString());
 
 		exporter.exportReport();
 
 		System.err.println("XHTML creation time : " + (System.currentTimeMillis() - start));
 	}
 	
 	
 	public void run() throws JRException
 	{
 		long start = System.currentTimeMillis();
 		//Preparing parameters
 		Image image = Toolkit.getDefaultToolkit().createImage("dukesign.jpg");
 		MediaTracker traker = new MediaTracker(new Panel());
 		traker.addImage(image, 0);
 		try
 		{
 			traker.waitForID(0);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		Map parameters = new HashMap();
 		parameters.put("ReportTitle", "The First Jasper Report Ever");
 		parameters.put("MaxOrderID", new Integer(10500));
 		parameters.put("SummaryImage", image);
 		
 		JasperRunManager.runReportToPdfFile("build/reports/FirstJasper.jasper", parameters, getDemoHsqldbConnection());
 		System.err.println("PDF running time : " + (System.currentTimeMillis() - start));
 	}
 }
 */
