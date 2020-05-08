 /**
  * 
  */
 package hk.idv.kenson.jrconsole;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Properties;
 import java.util.PropertyResourceBundle;
 import java.util.ResourceBundle;
 
 import net.sf.jasperreports.engine.DefaultJasperReportsContext;
 import net.sf.jasperreports.engine.JRDataSource;
 import net.sf.jasperreports.engine.JRException;
 import net.sf.jasperreports.engine.JRExporterParameter;
 import net.sf.jasperreports.engine.JasperCompileManager;
 import net.sf.jasperreports.engine.JasperExportManager;
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.JasperReport;
 import net.sf.jasperreports.engine.data.JRCsvDataSource;
 import net.sf.jasperreports.engine.data.JRXmlDataSource;
 import net.sf.jasperreports.engine.data.JsonDataSource;
 import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
 import net.sf.jasperreports.engine.util.JRLoader;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Generating report by JasperReport with minimal dependency.
  * @author kenson
  */
 public class Console {
 	public static final String ARG_TEXT="_arg_text_";
 	public static final String DEFAULT_DATE_FORMAT="yyyy-MM-dd";
 	public static final String DEFAULT_DECIMAL_FORMAT="#,##0.0";
 	public static final String VERSION="v1.1";
 	public static final String PARAM_LOCALE="REPORT_LOCALE";
 	public static final String PARAM_BUNDLE="REPORT_RESOURCE_BUNDLE";
 	private static DateFormat dateFormat;
 	private static DecimalFormat decimalFormat;
 	private static Log log=LogFactory.getLog(Console.class);
 	
 	/**
 	 * Checking the param is ready to generate the report or not
 	 * @param params Parameters to be checked
 	 * @throws IllegalArgumentException The exception talking about the which argument is invalid
 	 */
 	private static void checkParam(Map<String, Object> params)throws IllegalArgumentException{
 		log.debug("Checking runtime parameters...");
 		//Checking the data-source
 		if(params.get("source")==null)throw new IllegalArgumentException("Please specify the data-source");
 		if(params.get("type")==null)params.put("type", "jdbc");
 		String type=params.get("type").toString();
 		if(!(type.equals("jdbc") || type.equals("json") || type.equals("xml") || type.equals("csv")))throw new IllegalArgumentException("type \""+type+"\" is not supported");
 		if(!("jdbc".equals(type) || !"pipe".equals(params.get("source")))){
 			File file=new File(params.get("source").toString());
 			if(!(file.exists() && file.isFile()))throw new IllegalArgumentException("The source file is not exists: "+params.get("source"));
 			if(!file.canRead())throw new IllegalArgumentException("The source file is not readable: "+params.get("source"));
 		}
 		
 		//Checking the report template
 		if(params.get("jrxml")==null && params.get("jasper")==null)throw new IllegalArgumentException("Please specify -jrxml or -jasper for loading report template");
 		if(params.get("jrxml")!=null){
 			File jrxml=new File(params.get("jrxml").toString());
 			if(!jrxml.exists())throw new IllegalArgumentException("jrxml \""+params.get("jrxml")+"\" is not exists");
 			if(!jrxml.canRead())throw new IllegalArgumentException("jrxml \""+params.get("jrxml")+"\" is not readable");
 		}
 		if(params.get("jasper")!=null){
 			File jasper=new File(params.get("jasper").toString());
 			if(!jasper.exists())throw new IllegalArgumentException("jasper \""+params.get("jasper")+"\" is not exists");
 			if(!jasper.canRead())throw new IllegalArgumentException("jasper \""+params.get("jasper")+"\" is not readable");
 		}
 		
 		//Checking output
 		if(params.get("outputtype")==null)
 			params.put("outputtype", "pdf");
 		if(params.get("output")==null)
 			params.put("output", System.getProperty("user.dir")+"/output.pdf");
 		File output=new File(params.get("output").toString());
 		if(output.exists() && !output.canWrite())throw new IllegalArgumentException("output \""+params.get("output")+"\" cannot be overwrited");
 		
 		
 		//Checking the locale and bundle
 		try{
 			if(params.get("locale")!=null)
				params.put(PARAM_LOCALE, parseVal(params.remove("locale").toString(), null));
 			if(params.get(PARAM_LOCALE)==null)
 				params.put(PARAM_LOCALE, Locale.getDefault());
 			if(params.get("bundle")!=null)
				params.put(PARAM_BUNDLE, parseVal(params.get("bundle").toString(), params));
		}catch(Exception ex){throw new IllegalArgumentException("Error when reading properties/locale/resource-bundle");}
 	}
 	
 	/**
 	 * Copy the data from ips into ops.
 	 * @param ips
 	 * @param ops
 	 * @throws IOException
 	 */
 	private static void copy(InputStream ips, OutputStream ops)throws IOException{
 		byte[] b = new byte[1024];
         int noOfBytes = 0;
         
         while( (noOfBytes = ips.read(b)) != -1 )
         	ops.write(b, 0, noOfBytes);
 	}
 
 	/**
 	 * Loading the resource bundle from file-system or classpath.
 	 * <div>Loading resource-bundle from class: classpath:/org/apache/resourceBundle</div>
 	 * <div>Loading resource-bundle file file-system: /path/to/resourceBundle.properties</div>
 	 * @param path
 	 * @param locale
 	 * @return
 	 * @throws IOException
 	 */
 	private static ResourceBundle getBundle(String path, Locale locale)throws IOException{
 		if(path==null)throw new NullPointerException("Cannot loading the resource-bundle from empty path");
 		if(path.toLowerCase().startsWith("classpath:"))
 			return ResourceBundle.getBundle(path.substring(10), locale);
 		else
 			return new PropertyResourceBundle(new FileInputStream(path));
 	}
 	
 	/**
 	 * @return the dateFormat
 	 */
 	public static DateFormat getDateFormat() {
 		if(dateFormat==null){
 			String format=System.getProperty("dateFormat");
 			if(format==null)format=DEFAULT_DATE_FORMAT;
 			dateFormat=new SimpleDateFormat(format);
 		}
 		return dateFormat;
 	}
 
 	/**
 	 * @return the decimalFormat
 	 */
 	public static DecimalFormat getDecimalFormat() {
 		if(decimalFormat==null){
 			String format=System.getProperty("decimalFormat");
 			if(format==null)format=DEFAULT_DECIMAL_FORMAT;
 			decimalFormat=new DecimalFormat(format);
 		}
 		return decimalFormat;
 	}
 	
 	/**
 	 * 
 	 * @param localeString
 	 * @return
 	 */
 	private static Locale getLocale(String localeString){
 		if("default".equals(localeString))return Locale.getDefault();
 		if("canada".equals(localeString))return Locale.CANADA;
 		if("canada_french".equals(localeString))return Locale.CANADA_FRENCH;
 		if("china".equals(localeString))return Locale.CHINA;
 		if("chinese".equals(localeString))return Locale.CHINESE;
 		if("english".equals(localeString))return Locale.ENGLISH;
 		if("franch".equals(localeString))return Locale.FRANCE;
 		if("german".equals(localeString))return Locale.GERMAN;
 		if("germany".equals(localeString))return Locale.GERMANY;
 		if("italian".equals(localeString))return Locale.ITALIAN;
 		if("italy".equals(localeString))return Locale.ITALY;
 		if("japan".equals(localeString))return Locale.JAPAN;
 		if("japanese".equals(localeString))return Locale.JAPANESE;
 		if("korea".equals(localeString))return Locale.KOREA;
 		if("korean".equals(localeString))return Locale.KOREAN;
 		if("prc".equals(localeString))return Locale.PRC;
 		if("simplified_chinese".equals(localeString))return Locale.SIMPLIFIED_CHINESE;
 		if("taiwan".equals(localeString))return Locale.TAIWAN;
 		if("traditional_chinese".equals(localeString))return Locale.TRADITIONAL_CHINESE;
 		if("uk".equals(localeString))return Locale.UK;
 		if("us".equals(localeString))return Locale.US;
 		
 		String parts[] = localeString.split("_", -1);
 	    if (parts.length == 1) return new Locale(parts[0]);
 	    else if (parts.length == 2) return new Locale(parts[0], parts[1]);
 	    else return new Locale(parts[0], parts[1], parts[2]);
 	}
 	
 	/**
 	 * Loading the properties from file-system
 	 * @param path
 	 * @return
 	 * @throws IOException
 	 */
 	private static Properties getProperties(String path)throws IOException{
 		if(path==null)throw new NullPointerException("Cannot loading the properties from empty path");
 		Properties result=new Properties();
 		if(path.toLowerCase().endsWith(".xml"))
 			result.loadFromXML(new FileInputStream(path));
 		else
 			result.load(new FileInputStream(path));
 		return result;
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args){
 		try{
 			Map<String, Object> params=Console.parseArgs(args);
 			if(params.containsKey("help")){
 				printUsage();
 				return;
 			}
 			if(params.containsKey("version")){
 				System.err.println("Version: "+VERSION);
 				return;
 			}
 			
 			checkParam(params);
 			stepCompile(params);
 			JasperReport jasper=stepLoadReport(params);
 			JasperPrint print=stepFill(jasper, params);
 			InputStream stream=stepExport(print, params);
 
 			File output=new File(params.get("output").toString());
 			FileOutputStream fos=new FileOutputStream(output);
 			copy(stream, fos);
 			
 			fos.close();
 			stream.close();
 			System.out.println(output.getAbsolutePath());
 		}catch(IllegalArgumentException ex){
 			printUsage();
 			System.err.println("Error: "+ex.getMessage());
 		}catch(RuntimeException ex){
 			throw ex;
 		}catch(Exception ex){
 			throw new RuntimeException("Unexpected exception", ex);
 		}
 	}
 	/**
 	 * Parsing the string array into the Map<String, Object> format
 	 * @param args The string array to be pased
 	 * @return The Map<String, Object>
 	 * @throws ParseException
 	 */
 	public static Map<String, Object> parseArgs(String[] args) throws ParseException{
 		log.debug("Parsing arguments...");
 		Map<String, Object> result=new HashMap<String, Object>();
 		String name=null;
 		if(args!=null)for(String arg:args)
 			if(arg.startsWith("-")){
 				if(name!=null)
 					result.put(arg, true);
 				name=arg.substring(1);
 			}else{
 				if(name==null)
 					name=ARG_TEXT;
 				if(name.startsWith("D"))
 					result.put(name.substring(1), parseVal(arg, result));
 				else
 					result.put(name, arg);
 				name=null;
 			}
 		if(name!=null)result.put(name, true);
 		return result;
 	}
 	
 	/**
 	 * Parsing the string into the specified value (with right data-type)
 	 * @param arg
 	 * @return
 	 * @throws java.text.ParseException
 	 */
 	public static Object parseVal(String arg, Map<String, Object> params)throws java.text.ParseException{
 			try {
 				if(arg==null)return null;
 				if(arg.startsWith("boolean:"))return Boolean.valueOf(arg.substring(8));
 				if(arg.startsWith("int:"))return Integer.valueOf(arg.substring(4));
 				if(arg.startsWith("long:"))return Long.valueOf(arg.substring(5));
 				if(arg.startsWith("double:"))return Double.valueOf(arg.substring(7));
 				if(arg.startsWith("url:"))return new URL(arg.substring(4));
 				if(arg.startsWith("date:"))return getDateFormat().parse(arg.substring(5));
 				if(arg.startsWith("decimal:"))return getDecimalFormat().parse(arg.substring(8));
 				if(arg.startsWith("stream:"))return new URL(arg.substring(7)).openStream();
 				if(arg.startsWith("string:"))return arg.substring(7);
 				if(arg.startsWith("locale:"))return getLocale(arg.substring(7));
 				if(arg.startsWith("properties:"))return getProperties(arg.substring(11));
 				if(arg.startsWith("bundle:")){
 					Object locale=params.get(PARAM_LOCALE);
 					if(locale==null)locale=Locale.getDefault();
					if(locale instanceof Locale)throw new IllegalArgumentException("parameter["+PARAM_LOCALE+"] is reserved for the locale specification. ");
 					return getBundle(arg.substring(7), (Locale)locale);
 				}
 				return arg;
 			} catch (MalformedURLException e) {
 				throw new ParseException("Cannot create the url: "+arg.substring(4), 0);
 			} catch (IOException e) {
 				throw new ParseException("Cannot open the stream: "+arg.substring(7), 0);
 			}
 	}
 
 	/**
 	 * Print the usage of the application
 	 */
 	private static void printUsage(){
 		System.err.printf("Usage:\n");
 		System.err.printf("   java -cp <jar-file> -jar jrconsole.jar <options>\n");
 		System.err.printf("   java -jar jrconsole.jar -version\n");
 		System.err.printf("   java -jar jrconsole.jar -help\n");
 		System.err.printf("Options:\n");
 		System.err.printf("   %-20s: %s\n", "-driver", "The jdbc driver class name");
 		System.err.printf("   %-20s: %s\n", "-source", "The datasource specification. It can be the jdbc-url or file path");
 		System.err.printf("   %-20s: %s\n", "-type", "The datasource type. It can be 'jdbc', 'csv', 'json' or 'xml'. Default is 'jdbc'");
 		System.err.printf("   %-20s: %s\n", "-username", "The username of the data-source");
 		System.err.printf("   %-20s: %s\n", "-password", "The password of the data-source");
 		System.err.printf("   %-20s: %s\n", "-jrxml", "The path of the jrxml file. I\'ll compile it automatically");
 		System.err.printf("   %-20s: %s\n", "-jasper", "The compiled jasper file");
 		System.err.printf("   %-20s: %s\n", "-outputtype", "The report file type. Default is PDF");
 		System.err.printf("   %-20s: %s\n", "-output", "The output path of the report");
 		System.err.printf("   %-20s: %s\n", "-locale", "The locale of the report");
 		System.err.printf("   %-20s: %s\n", "-bundle", "The resource-bundle of the report");
 		System.err.printf("   %-20s: %s\n", "-Dparam", "The parameter that passed into JasperReport");
 		System.err.printf("Supported Parameter Types:\n");
 		System.err.printf("   boolean, int, long, double, url, date, decimal, stream, string, properties\n");
 		System.err.printf("     * Loading properties from the path. If loading classpath properties, use \"classpath:\" prefix.");
 		System.err.printf("     * Date Format can be specified by system property \"dateFormat\". Default is: %s\n", DEFAULT_DATE_FORMAT);
 		System.err.printf("     * Decimal Format can be specified by system property \"decimalFormat\". Default is: %s\n", DEFAULT_DECIMAL_FORMAT);
 		System.err.printf("\nExamples:\n");
 		System.err.printf("   java -cp jasperreport.jar:postgresql.jar:itext.jar -DdateFormat yyyy-MM-dd -jar jrconsole.jar \\\n");
 		System.err.printf("        -type jdbc \\\n");
 		System.err.printf("        -driver org.postgresql.Driver \\\n");
 		System.err.printf("        -source jdbc:postgresql://localhost:5432/dbname \\\n");
 		System.err.printf("        -username test -password abc123 \\\n");
 		System.err.printf("        -jrxml /home/test/report.jrxml \\\n");
 		System.err.printf("        -output /home/test/report.pdf -outputtype pdf \\\n");
 		System.err.printf("        -Dabc boolean:true -DreportDate date:2013-01-28\n");
 		System.err.printf("        -Dproperties properties:/path/to/file.properties \\\n");
 		System.err.printf("        -DREPORT_RESOURCE_BUNDLE bundle:/path/to/file.properties \\\n");
 	}
 	
 	/**
 	 * Compile the jrxml to jasper if necessary
 	 * @param params Runtime parameters
 	 */
 	private static void stepCompile(Map<String, Object> params){
 		if(params.containsKey("jasper"))return;//Don't need to compile
 		log.info("Compiling jrxml into jasper...");
 		
 		try{
 			File jasper=File.createTempFile("jrconsole-", ".jasper");
 			File jrxml=new File(params.get("jrxml").toString());
 			
 			JasperCompileManager.compileReportToStream(new FileInputStream(jrxml), new FileOutputStream(jasper));
 			
 			params.put("jasper", jasper.getAbsolutePath());
 			log.debug("Compiled report is ready on \""+jasper.getAbsolutePath()+"\".");
 		}catch(Exception ex){
 			throw new RuntimeException("Unexpected exception", ex);
 		}
 	}
 	
 	/**
 	 * Export the JasperPrint into output stream according to runtime parameters
 	 * @param report The filled report
 	 * @param params Runtime parameters
 	 * @return The InputStream stream for reading the report
 	 */
 	private static InputStream stepExport(JasperPrint report, Map<String, Object> params){
 		log.info("Exporting report in "+params.get("outputtype")+" format...");
 		
 		byte[] result=null;
 		try {
 			String type=params.get("outputtype").toString();
 			if("pdf".equals(type))
 				result = JasperExportManager.exportReportToPdf(report);
 			else if("odt".equals(type)){
 				ByteArrayOutputStream baos = new ByteArrayOutputStream();
 				JROdtExporter exporter = new JROdtExporter(DefaultJasperReportsContext.getInstance());
 				exporter.setParameter(JRExporterParameter.JASPER_PRINT, report);
 				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos);
 				exporter.exportReport();
 				return new ByteArrayInputStream(baos.toByteArray());
 			}
 			
 			if(result==null)throw new UnsupportedOperationException("output-type \""+type+"\" is not supported");
 		} catch (JRException ex) {
 			throw new RuntimeException("Unexpected exception", ex);
 		}
 
 		return new ByteArrayInputStream(result);
 	}
 	
 	/**
 	 * Filling the report with data-source
 	 * @param report The compiled jasper-report
 	 * @param params Runtime parameters
 	 * @return The filled report
 	 */
 	private static JasperPrint stepFill(JasperReport report, Map<String, Object> params){
 		log.info("Filling report (with locale: "+params.get(PARAM_LOCALE)+")...");
 		
 		try {
 			if(params.get("driver")!=null)
 					Class.forName(params.get("driver").toString());
 
 			JasperPrint result=null;
 			if("jdbc".equals(params.get("type"))){
 				log.debug("Filling report from jdbc datasource...");
 				Connection conn=DriverManager.getConnection(params.get("source").toString(), params.get("username").toString(), params.get("password").toString());
 				result=JasperFillManager.fillReport(report, params, conn);
 			}else{
 				InputStream source=null;
 				if("pipe".equals(params.get("source"))){}else source=new FileInputStream(params.get("source").toString());
 				JRDataSource datasource=null;
 
 				if("csv".equals(params.get("source"))){
 					log.debug("Filling report from csv datasource["+params.get("source")+"]...");
 					datasource=new JRCsvDataSource(source);
 					((JRCsvDataSource)datasource).setUseFirstRowAsHeader(true);
 				}else if("json".equals(params.get("type"))){
 					log.debug("Filling report from json datasource...");
 					datasource=new JsonDataSource(source);
 				}else if("xml".equals(params.get("source"))){
 					log.debug("Filling report from xml datasource...");
 					datasource=new JRXmlDataSource(source);
 				}
 				
 				result=JasperFillManager.fillReport(report, params, datasource);
 			}
 			return result;
 		} catch (Exception ex) {
 			throw new RuntimeException("Unexpected Exception", ex);
 		}
 	}
 
 	/**
 	 * Loading the report from jasper
 	 * @param params Runtime parameters
 	 * @return The compiled jasper-report
 	 */
 	private static JasperReport stepLoadReport(Map<String, Object> params){
 		log.info("Loading report template...");
 		
 		try {
 			JasperReport result=(JasperReport)JRLoader.loadObjectFromFile(params.get("jasper").toString());
 			return result;
 		} catch (JRException e) {
 			throw new RuntimeException("Cannot loading the jasper-report: "+params.get("jasper"));
 		}
 	}
 }
