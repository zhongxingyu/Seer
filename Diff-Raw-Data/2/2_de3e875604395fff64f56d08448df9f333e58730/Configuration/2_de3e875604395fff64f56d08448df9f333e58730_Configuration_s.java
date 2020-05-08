 package cz.muni.fi.pb138.log4jconverter.configuration;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import cz.muni.fi.pb138.log4jconverter.PropertiesParser;
 
 
 public class Configuration{
     
     public enum Threshold {
         all,trace,debug,info,warn,error,fatal,off,
     }
     
     
     private Threshold threshold = null;
     private Boolean debug = null;
     private boolean reset = false;
     
     private Root root;
     private HashSet<Renderer> renderers;
     private ThrowableRenderer throwableRenderer;
     private HashMap<String, Appender> appenders;
     private HashSet<Logger> loggers;
     private HashMap<String, Plugin> plugins;
     private LoggerFactory logFactory;
     
     private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Configuration.class);
     
 
     public Configuration() {
         renderers = new HashSet<Renderer>();
         appenders = new HashMap<String, Appender>();
         loggers = new HashSet<Logger>();
         plugins = new HashMap<String, Plugin>();
     }
 
     /* returns Appender by its name, if it does'n exists,
 	 * creates new one.
      */
     public Appender getAppender(String name) { 
         if (!appenders.containsKey(name)) {
             appenders.put(name, new Appender(name));
         }
         return appenders.get(name);
     }
     
     /*
      * It's used in tests
      * default visibility is OK for tests
      */
     boolean isAppender(String name) {
         if (appenders.containsKey(name)) {
             return true;
         }
         return false;
     }
 
     public HashMap<String, Appender> getAppenders() {
         return appenders;
     }
 
     public void setAppenders(HashMap<String, Appender> appenders) {
         this.appenders = appenders;
     }
 
     public Boolean getDebug() {
         return debug;
     }
 
     public void setDebug(Boolean debug) {
         this.debug = debug;
     }
 
     public LoggerFactory getLogFactory() {
         return logFactory;
     }
 
     public void setLogFactory(LoggerFactory logFactory) {
         this.logFactory = logFactory;
     }
     
     public Logger getLogger(String name){
     	for(Logger l : loggers){
     		if(l.getName().equals(name)){
     			return l;
     		}
     	}
     	Logger l = new Logger();
         l.setName(name);
     	loggers.add(l);
     	return l;
     }
 
     public HashSet<Logger> getLoggers() {
         return loggers;
     }
 
     public void setLoggers(HashSet<Logger> loggers) {
         this.loggers = loggers;
     }
     
     public void addLogger(Logger logger) {
         loggers.add(logger);
     }
 
     public HashMap<String, Plugin> getPlugins() {
         return plugins;
     }
 
     public void setPlugins(HashMap<String, Plugin> plugins) {
         this.plugins = plugins;
     }
     
     public void addPlugin(Plugin plugin) {
         plugins.put(plugin.getName(), plugin);
     }
 
     public boolean isReset() {
         return reset;
     }
 
     public void setReset(boolean reset) {
         this.reset = reset;
     }
 
     public Threshold getThreshold() {
         return threshold;
     }
 
     public void setThreshold(Threshold threshold) {
         this.threshold = threshold;
     }
 
     public HashSet<Renderer> getRenderers() {
         return renderers;
     }
 
     public void setRenderers(HashSet<Renderer> renderers) {
         this.renderers = renderers;
     }
 
     public Root getRoot() {
         return root;
     }
 
     public void setRoot(Root root) {
         this.root = root;
     }
 
     public ThrowableRenderer getThrowableRenderer() {
         return throwableRenderer;
     }
 
     public void setThrowableRenderer(ThrowableRenderer throwableRenderer) {
         this.throwableRenderer = throwableRenderer;
     }
     
     public void addRenderer(Renderer r) {
         renderers.add(r);
     }
     
     public void addAppender(Appender a) {
         appenders.put(a.getAppenderName(), a);
     }
 
     public Document generateXML() {
     	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder;
 		try {
 			builder = factory.newDocumentBuilder();
 		} catch (ParserConfigurationException e) {
 			logger.error("Couldn't make DocumentBuilder",e);
 			return null;
 		}
         Document doc = builder.newDocument();
         
         Element config = doc.createElement("log4j:configuration");
         config.setAttribute("xmlns:log4j","http://jakarta.apache.org/log4j/");
         if(threshold !=null){
            config.setAttribute("treshold",threshold.toString());
         }
         if(debug!=null)
         {
             config.setAttribute("debug",debug.toString());
         }
         if(reset){
         config.setAttribute("reset","true");    
         }
         else{
         config.setAttribute("reset","false");        
         }
         
         
         
       
         for(Renderer renderer : renderers)
         {
             renderer.generateXML(doc, config);
         }
         
         if(throwableRenderer!= null){
             throwableRenderer.generateXML(doc, config);
         }
         
         for(Appender appender : appenders.values())
         {
             appender.generateXML(doc, config);
         }
         
        
         
         for (Plugin plugin : plugins.values())
         {
             plugin.generateXML(doc, config);
         }
          for(Logger logger : loggers)
         {
            logger.generateXML(doc, config);
         }
          
           if(root!= null)
         {
            root.generateXML(doc, config);
         }
         
         if(logFactory!= null)
         {
             logFactory.generateXML(doc, config);
         }
         
         doc.appendChild(config);    
         
         return doc;
     }
 
     public Properties generateProperties() {
     	Properties props = new Properties();
     	
 		// log4j.rootLogger
 		if (root != null) root.generateProperties(props);
 			
 		// log4j.appender
 		if (appenders != null) {
 			Iterator<Entry<String, Appender>> i = appenders.entrySet().iterator(); 
 			while(i.hasNext()) { 
 				Entry<String, Appender> pairs = i.next();
 				Appender a = pairs.getValue();
 				a.generateProperties(props);
 			} 
 		}
 		
 		// log4j.logger
 		if (logger != null) {
 			for (Logger logger : loggers) {
 				logger.generateProperties(props);
 			}
 		}
 		
 		// log4j.threshold=[level]
 		if (threshold != null) props.setProperty(PropertiesParser.THRESHOLD_PREFIX, threshold.toString());
 		
 		// log4.loggerFactory
 		if (logFactory != null) logFactory.generateProperties(props);
 		
 		// log4j.debug
 		if ((debug != null) && debug) props.setProperty(PropertiesParser.PREFIX + "." + PropertiesParser.DEBUG, "true");
 		
 		return props;
     }
 
     @Override
     public String toString() {
         return root.toString();
     }
 
 }
