 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.muni.fi.pb138.log4jconverter;
 
 import java.util.Enumeration;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 import cz.muni.fi.pb138.log4jconverter.configuration.Appender;
 import cz.muni.fi.pb138.log4jconverter.configuration.Configuration;
 import cz.muni.fi.pb138.log4jconverter.configuration.Level;
 import cz.muni.fi.pb138.log4jconverter.configuration.Logger;
 import cz.muni.fi.pb138.log4jconverter.configuration.LoggerFactory;
 import cz.muni.fi.pb138.log4jconverter.configuration.Root;
 
 /**
  * 
  * Init Configuration from property external file.
  * 
  * 
  *
  * @author fivekeyem
  */
 public class PropertiesParser implements Parser {
 	
     public static final String               PREFIX    = "log4j";
     public static final String             APPENDER    = "appender";
     public static final String             CATEGORY    = "category";
     public static final String               LOGGER    = "logger";
     public static final String        ROOT_CATEGORY    = "rootCategory";
     public static final String          ROOT_LOGGER    = "rootLogger";
     public static final String                DEBUG    = "debug";
     public static final String            THRESHOLD    = "threshold";
     public static final String       LOGGER_FACTORY    = "loggerFactory";
     public static final String           ADDITIVITY    = "additivity";
     
     
     public static final String      CATEGORY_PREFIX    = "log4j.category.";
     public static final String        LOGGER_PREFIX    = "log4j.logger.";
     public static final String       FACTORY_PREFIX    = "log4j.factory";
     public static final String    ADDITIVITY_PREFIX    = "log4j.additivity.";
     public static final String ROOT_CATEGORY_PREFIX    = "log4j.rootCategory";
     public static final String   ROOT_LOGGER_PREFIX    = "log4j.rootLogger";
     public static final String      APPENDER_PREFIX    = "log4j.appender.";
     public static final String      RENDERER_PREFIX    = "log4j.renderer.";
     public static final String     THRESHOLD_PREFIX    = "log4j.threshold";
     
 	public static final String      THROWABLE_RENDERER_PREFIX = "log4j.throwableRenderer";
     public static final String LOGGER_REF	= "logger-ref";
     public static final String   ROOT_REF	= "root-ref";
     public static final String APPENDER_REF_TAG= "appender-ref";  
 
     /** Key for specifying the {@link org.apache.log4j.spi.LoggerFactory
         LoggerFactory}.  Currently set to "<code>log4j.loggerFactory</code>".  */
     public static final String LOGGER_FACTORY_KEY = "log4j.loggerFactory";
 
     /**
         * If property set to true, then hierarchy will be reset before configuration.
         */
     public static final String RESET_KEY = "log4j.reset";
 
     public static final String INTERNAL_ROOT_NAME = "root";
     
     private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PropertiesParser.class);
     
     private Properties properties;
     private Configuration configuration;
     
     
     public PropertiesParser(Properties properties) {
         this.properties = properties;
         this.configuration = null;
     }
     
     
     public void writeAllProperties() {
         Enumeration<?> e = properties.propertyNames();
 
         while (e.hasMoreElements()) {
             String key = (String) e.nextElement();
             System.out.println(key + " -- " + properties.getProperty(key));
         }
     }
 
 	// goes for all properties, split key into array and parse
     private void parsePropeties(){
     	for(Entry<Object, Object> e : properties.entrySet()){
     		String[] key = ((String) e.getKey()).split("\\.");
     		String value = (String) e.getValue();
     		
     		try {
 				parseProperty(key,value);
 			} catch (ParseException e1) {
 				logger.warn("Unexpected property: " + e.getKey() + "=" + e.getValue(),e1);
 			}
     	}
     	
     }
     
     private void parseProperty(String[] key, String value) throws ParseException {
 		if (logger.isTraceEnabled()) { logger.trace("parsing key: " + concateKeyParts(key, 0)); }
     	if(key.length < 2) throw new ParseException("Key must have at least 2 parts");
     	if(!PREFIX.equals(key[0])) throw new ParseException("Key must have prefix " + PREFIX);
     	
     	String specifier = key[1];
     	
     	if(APPENDER.equals(specifier)){
     		parseAppender(key, value);
     	}else if(LOGGER.equals(specifier) || CATEGORY.equals(specifier)){
     		parseLogger(key, value);
     	}else if(ROOT_LOGGER.equals(specifier) || ROOT_CATEGORY.equals(specifier)){
     		parseRootLogger(key, value);
     	}else if(DEBUG.equals(specifier)){
     		if(value.equals("true")){
     			configuration.setDebug(true);
     		}else if(value.equals("false")){
     			configuration.setDebug(false);
     		}else{
     			throw new ParseException("Unknown value for " + PREFIX + "." + DEBUG);
     		}
     	}else if(LOGGER_FACTORY.equals(specifier)){
     		LoggerFactory lf = new LoggerFactory();
     		lf.setClassName(value);
     		configuration.setLogFactory(lf);
     	}else if(THRESHOLD.equals(specifier)){
     		try{
     			configuration.setThreshold(Configuration.Threshold.valueOf(value.toLowerCase()));
     		}catch (IllegalArgumentException ex){
     			throw new ParseException(ex);
     		}
     	}else if(ADDITIVITY.equals(specifier)){
     		Logger l = configuration.getLogger(concateKeyParts(key, 2));
     		if(value.equals("true")){
     			l.setAdditivity(true);
     		}else if(value.equals("false")){
     			l.setAdditivity(false);
     		}else{
     			throw new ParseException("Unknown value for " + PREFIX + "." + ADDITIVITY);
     		}
     	}else{
     		throw new ParseException("Unknown key");
     	}
     	
 	}
 
 
 	private void parseRootLogger(String[] key, String value) throws ParseException {
 		if (logger.isTraceEnabled()) { logger.trace("parsing root logger: '" + value + "'"); }
 		if(ROOT_CATEGORY.equals(key[1]) && configuration.getRoot() != null ){
 			logger.info("Trying to parse "+ ROOT_CATEGORY+" when root is allready set. Skipping.");
 			return;
 		}
         
         Root rootLogger = new Root();
 
         // ziskej jednotlive appendery
         String[] rootLoggerValue = value.split(",");
 		try{
 			Level level = new Level();
 			level.setValues(Level.Levels.valueOf(rootLoggerValue[0].trim().toUpperCase()));
 			rootLogger.setLevel(level);
 		}catch (IllegalArgumentException ex){
 			throw new ParseException(ex);
 		}
         for (int i = 1; i < rootLoggerValue.length; i++) {
         	// pridej novy appender
         	rootLogger.addAppenderRef(rootLoggerValue[i].trim());
 
         	if (logger.isTraceEnabled()) { logger.trace("new appender ("+ rootLoggerValue[i].trim() +") created"); }
         }
 
         // nakonec uloz vse do configuration
         configuration.setRoot(rootLogger);
         if (logger.isTraceEnabled()) { logger.trace("configuration saved"); }		
 	}
 
 
 	private void parseLogger(String[] key, String value) throws ParseException {
 		if (logger.isTraceEnabled()) { logger.trace("parsing logger: " +concateKeyParts(key, 0)+"=" + value); }
 		
 		String loggerName = concateKeyParts(key, 2);
 		Logger l = configuration.getLogger(loggerName);
 		if(CATEGORY.equals(key[1])){
			l.setCategory(true);
 		}
 		
 		String[] values = value.split(",");
 		if(values.length >= 1){
 			try{
 				Level level = new Level();
 				level.setValues(Level.Levels.valueOf(values[0].trim().toUpperCase()));
 				l.setLevel(level);
 			}catch (IllegalArgumentException ex){
 				throw new ParseException(ex);
 			}
 			for(int i=1; i < values.length; i++){
 				l.addAppenderRef(values[i].trim());
 			}
 		}
 	}
 
 
 	private void parseAppender(String[] key, String value) throws ParseException {
 		if (logger.isTraceEnabled()) { logger.trace("parsing appender: " +concateKeyParts(key, 0)+"=" + value); }
 		if(key.length < 3) throw new ParseException("Appender key must have at least 3 parts");
 		
 		String appenderName = key[2];
 		Appender appender = configuration.getAppender(appenderName);
 		
 		if(key.length == 3){ // log4j.appender.APNAME = org.example.myclass
 			appender.setClassName(value);
 		}else{ // length > 3
 			if("layout".equals(key[3])){
 				parseLayout(key,value,appender);
 			}else if("filter".equals(key[3])){
 				parseFilter(key,value,appender);
 			}else if(key.length == 4){ // this should be miscellaneous parameters
 				appender.addParam(key[3],value);
 			}else{ // if it has length > 4 and isn't parsed yet, it is wrong or unknown
 				new ParseException("Unknown appender key");
 			}
 		}
 		
 	}
 
 	private void parseFilter(String[] key, String value, Appender appender) throws ParseException {
 		if (logger.isTraceEnabled()) { logger.trace("parsing appender filter: " +concateKeyParts(key, 0)+"=" + value); }
 		if(key.length < 5) throw new ParseException("Appender filter key must have at least 5 parts");
 
 		String filterName = key[4];
 		if(key.length == 5){
 			appender.getFilter(filterName).setClassName(value);
 		}else if(key.length == 6){
 			appender.getFilter(filterName).addParam(key[5], value);
 		}else{
 			throw new ParseException("Unknown filter key");
 		}
 	}
 
 
 	private void parseLayout(String[] key, String value, Appender appender) throws ParseException {
 		if (logger.isTraceEnabled()) { logger.trace("parsing appender layout: " +concateKeyParts(key, 0)+"=" + value); }
 		if(key.length < 4) throw new ParseException("Appender layout key must have at least 4 parts");
 		
 		if(key.length == 4){
 			appender.getLayout().setClassName(value);
 		}else if(key.length == 5){ // parameter
 			appender.getLayout().addParam(key[4], value);
 		}else{
 			throw new ParseException("Unknown layout key");
 		}
 	}
 
 	@Override
 	public Configuration parse() {
 		if(configuration == null){
 			configuration = new Configuration();
 			parsePropeties();
 		}
 		return configuration;
 	}
     
 	private static String concateKeyParts(String[] key, int from){
 		if(from >= key.length) return "";
 		StringBuilder sb = new StringBuilder();
 		sb.append(key[from]);
 		for(int i=from+1; i < key.length; i++){
 			sb.append('.');
 			sb.append(key[i]);
 		}
 		
 		return sb.toString();
 	}
     
 }
