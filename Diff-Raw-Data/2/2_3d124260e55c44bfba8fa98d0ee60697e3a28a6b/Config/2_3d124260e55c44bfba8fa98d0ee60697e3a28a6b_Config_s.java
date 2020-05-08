 package util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.math.BigDecimal;
 import java.util.Locale;
 import java.util.Properties;
 
 import org.joda.money.CurrencyUnit;
 
 import com.novell.ldap.LDAPConnection;
 
 import freemarker.template.Configuration;
 import freemarker.template.DefaultObjectWrapper;
 import freemarker.template.TemplateExceptionHandler;
 import freemarker.template.Version;
 
 public class Config {
 
 	public static int defaultPageSize = 30;
 
	private static final String resourcesPath = "../standalone/deployments/Jama.war/WEB-INF/classes/";
 	private static final String configPath = resourcesPath + "config/";
 	
 	public static final String depRatesPath = configPath  + "aliquoteDipartimenti/";
 	public static final String depRatesDefaultDir = "default";
 	private static final String mailTemplateDir = configPath + "mailTemplates";
 	private static final String basicConfigFile = configPath + "basic.properties";
 	
 	public static final Configuration fmconf;
 	public static final String instDeadlineTemplateFileName = "template_scadenzaRata.ftl";
 	public static final String contractCreationTemplateFileName = "template_creazioneContratto.ftl";
 	public static final String contractClosureTemplateFileName = "template_chiusuraContratto.ftl";
 
 	public static final CurrencyUnit currency = CurrencyUnit.EUR;
 	public static final Locale locale = Locale.ITALY;
 
 	public static final int dailyScheduledTaskExecutionHour;
 	public static final int daysBeforeDeadlineExpriration;
 	public static final Percent defaultIva;
 	
 	//LDAP settings
 	private static final String ldapConfigFile = configPath +"ldap.properties";
 	
 	public static final int ldapPort;
 	public static final int searchScope = LDAPConnection.SCOPE_SUB;
 	public static final int ldapVersion = LDAPConnection.LDAP_V3;
 	public static final String ldapHost;
 	public static final String loginDN;
 	public static final String password;
 	public static final String searchBase;
 	public static final String deptsBusinessCategory;
 	public static final int hoursBeforeLdapCacheUpdate;
 
 	
 	
 
 	static {
 		
 
 
 //		try {
 //			System.setErr(new PrintStream(new File(logFile)));
 //			System.err.println(new Date() + "\n=========\n" + "Inizio attività di logging\n" + "==========");
 //		} catch (FileNotFoundException e1) {
 //			e1.printStackTrace();
 //		}
 
 		fmconf = new Configuration();
 		setFreeMarkerConf();
 
 		
 		Properties p = new Properties();
 		InputStream in = null;
 
 		try {
 			in = new FileInputStream(basicConfigFile);
 			p.load(in);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			throw new IllegalStateException("Could not find or open " + basicConfigFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new IllegalStateException("Could not read " + basicConfigFile);
 		}
 
 		defaultIva = Percent.normalizedValueOf(new BigDecimal(p.getProperty("iva", "0").trim()));
 		dailyScheduledTaskExecutionHour = Integer.parseInt(p.getProperty("ora_esecuzione", "3").trim());
 		daysBeforeDeadlineExpriration = Integer.parseInt(p.getProperty("giorni_preavviso", "15").trim());
 		
 		
 		try {
 			in.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		
 		p = new Properties();
 		in = null;
 
 		try {
 			in = new FileInputStream(ldapConfigFile);
 			p.load(in);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			throw new IllegalStateException("Could not find or open " + ldapConfigFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new IllegalStateException("Could not read " + ldapConfigFile);
 		}
 
 		ldapPort = Integer.parseInt(p.getProperty("ldapPort").trim());
 		ldapHost = p.getProperty("ldapHost").trim();
 		loginDN = p.getProperty("loginDN").trim();
 		password = p.getProperty("password").trim();
 		searchBase = p.getProperty("searchBase").trim();
 		deptsBusinessCategory = p.getProperty("deptsBusinessCategory");
 		hoursBeforeLdapCacheUpdate = Integer.parseInt(p.getProperty("hoursBeforeCacheUpdate"));
 
 		
 
 		
 		
 		try {
 			in.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		
 		
 		
 		
 		
 		
 	}
 	
 	private static void setFreeMarkerConf(){
 		// Specify the data source where the template files come from. Here I
 		// set a
 		// plain directory for it, but non-file-system are possible too:
 		try {
 			fmconf.setDirectoryForTemplateLoading(new File(Config.mailTemplateDir));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		// Specify how templates will see the data-model. This is an advanced
 		// topic...
 		// for now just use this:
 		fmconf.setObjectWrapper(new DefaultObjectWrapper());
 
 		// Set your preferred charset template files are stored in. UTF-8 is
 		// a good choice in most applications:
 		fmconf.setDefaultEncoding("UTF-8");
 
 		// Sets how errors will appear. Here we assume we are developing HTML
 		// pages.
 		// For production systems TemplateExceptionHandler.RETHROW_HANDLER is
 		// better.
 		fmconf.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
 
 		// At least in new projects, specify that you want the fixes that aren't
 		// 100% backward compatible too (these are very low-risk changes as far
 		// as the
 		// 1st and 2nd version number remains):
 		fmconf.setIncompatibleImprovements(new Version(2, 3, 20)); // FreeMarker
 																	// 2.3.20
 	}
 	
 
 }
