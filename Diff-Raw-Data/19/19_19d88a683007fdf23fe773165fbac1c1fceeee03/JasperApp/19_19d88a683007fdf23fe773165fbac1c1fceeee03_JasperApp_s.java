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
 import java.util.List;
 import java.util.ArrayList;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.regex.Matcher; 
 import java.util.regex.Pattern;
 
 
 
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
 import mutil.base.Holder;
 import mutil.base.FileUtil;
 
 import org.python.core.PyCode;
 import org.python.core.PyException;
 import org.python.core.PyObject;
 import org.python.util.PythonInterpreter;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.apache.commons.lang3.StringEscapeUtils;
 
 public class JasperApp
 {
     private static final Logger l = LoggerFactory.getLogger(JasperApp.class);
 	
         public static void main(String[] args) throws JRException, IOException {
             switch (args[0]) {
                 case "pdf": pdf(args[1]); 
                     break;
                 default:
                     throw new RuntimeException("unrecognized case");
             }
             System.err.println("exiting "+JasperApp.class.getName()+"#main");
 	}
 
         private static String className         () { return JasperApp.class.getName() ; }
         private static String reportCoreName    () { return "groups"             ; }
         private static String compiledReportName() { return reportCoreName()+".jasper"; }
         private static String pdfReportName     () { return reportCoreName()+".pdf"   ; }
 
 
 	private static Connection getHsqlConnection() throws JRException {
 		Connection conn;
 		try {
 			String driver = "org.hsqldb.jdbcDriver";
 			String connectString = "jdbc:hsqldb:hsql://localhost";
 			String user = "sa";
 			String password = "";
 			Class.forName(driver);
 			conn = DriverManager.getConnection(connectString, user, password);
                         System.out.println("Connection obtained: "+((conn==null)?"null":"not null"));
 		}
 		catch (ClassNotFoundException e) {
 			throw new JRException(e);
 		}
 		catch (SQLException e) {
 			throw new JRException(e);
 		}
 		return conn;
 	}
 
     private static File fill() throws JRException, IOException {
             long start = System.currentTimeMillis();
             String sourceFileLocation = "reports/"+compiledReportName();
             System.err.println(" sourceFileLocation : " + sourceFileLocation);
             JasperReport jasperReport = (JasperReport)JRLoader.loadObjectFromLocation(sourceFileLocation);
             String script = FileUtil.readFileAsSingleString(new File("script.py"), "\n");
             System.out.println("script file read as follows:\n");
             System.out.println(script);
            /*
                "q1 = sql.q('SELECT ORDERID FROM PUBLIC.ORDERS')\n"                           +
                "STR_1 = 'Pi One'\n"                                                          +
                "I_2=3**2\nI_3=2\n"                                                           +
                "I_4=I_2**I_3\n"                                                              +
                "I_5=q1[1]\n"                                                                 +
                "q2 = sql.q('SELECT SHIPCOUNTRY FROM PUBLIC.ORDERS WHERE ORDERID=%d'%I_5)\n"  +
                "STR_b=q2[1]"                                                                 ;*/
             System.out.println("\\---------------- end of script file ------------/");
            Map<String, Object> parameters = processParameters(getHsqlConnection(), script);
             printParameters(parameters);
             JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, getHsqlConnection());
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
 
 
     private static void printParameters(Map<String, Object> params) {
         System.out.println("----------- έλεγχος -----------");
         String s = StringEscapeUtils.unescapeJava("\u00ce\u00a0\u00ce\u00b9 \u00ce\u00ad\u00ce\u00bd\u00ce\u00b1");
         System.out.println(s);
         String test = "\u03a0\u03b9 \u03ad\u03bd\u03b1";
         System.out.println("test = " +test);
         for (String key : params.keySet()) {
             if (!key.startsWith("STR_"))
                 System.out.println(key+"  -->  "+params.get(key));
             else {
                 System.out.println(key+"  -->  "+StringEscapeUtils.unescapeJava((String)params.get(key)));
                 params.put(key, StringEscapeUtils.unescapeJava((String)params.get(key)));
             }
         }
     }
 
     public static Map<String, Object> processParameters(Connection conn, String script) throws JRException {
         Map<String, Object> params = new HashMap<String, Object>();
         {
             PythonInterpreter pi = new PythonInterpreter();
             QueryJythonHolder sql = new QueryJythonHolder(conn);
             pi.set("params", params);
             pi.set("sql", sql);
             PyObject result = null;
             try {
                 PyCode code = pi.compile(adorn(script, "params"));
                 result = pi.eval(code);
                 l.info("evalution result = "+result);
             } catch (PyException pyException) {
                 throw new ExceptionAdapter(pyException);
             }
         }
         return params;
     }
 
     private static List<String> paramNames(String script, String paramRegExp) {
         List<String> retValue = new ArrayList<String>();
         Pattern p0 = Pattern.compile(paramRegExp);
         Matcher m = p0.matcher(script);
         while (m.find()) {
             retValue.add(m.group(0));
         }
         return retValue;
     }
 
     public static void mmain(String args[]) {
         System.out.println(paramNames("STR_1=sdf I_3=I_2+I_3;BD_234\nBD_3; dfasdfsdfasdfasdfasdf", "(STR|BD|I|D)_\\w*"));
     }
 
     private static final String adorn(String script, String paramsVar) {
        List<String> paramNames = paramNames(script, "(STR|BD|I|D)_\\w*");
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (String paramName : paramNames)
            sb.append(String.format("%s.put('%s', %s)\n", paramsVar, paramName, paramName));
        return script+sb.toString();
     }
 
        
     public static void pdf(String whereToProducePDF) throws JRException, IOException {
        File jrprintFile = fill();
        long start = System.currentTimeMillis();
        JasperExportManager.exportReportToPdfFile(jrprintFile.getAbsolutePath(), whereToProducePDF+"/"+pdfReportName());
        System.err.println("PDF creation time : " + (System.currentTimeMillis() - start));
     }
 }
 
