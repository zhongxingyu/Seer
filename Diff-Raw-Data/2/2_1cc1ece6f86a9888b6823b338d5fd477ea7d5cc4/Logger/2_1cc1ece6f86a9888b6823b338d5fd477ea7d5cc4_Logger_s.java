 package cz.muni.fi.pb138.log4jconverter.configuration;
 
 import cz.muni.fi.pb138.log4jconverter.PropertiesParser;
 import java.util.*;
 import java.util.Map.Entry;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  *
  * @author Admin
  */
 public class Logger {
     //required
 
     private String name;
     //implies
     private String className;
     //podle dtd je deafulat hodnota true
     private boolean additivity = true;
     //optional
     private HashMap<String, String> params;
     private HashSet<String> appenderRefs;
     private Level level;
     /*
      * Category is deprecated synonym of Logger, this boolean keeps information
      * about actual name of Logger.
      */
     private boolean isCategory = false;
 
     public Logger() {
         this.params = new HashMap<String, String>();
         this.appenderRefs = new LinkedHashSet<String>();
     }
 
     public void isCategory(boolean b) {
         isCategory = b;
     }
 
     public boolean isAdditivity() {
         return additivity;
     }
 
     public void setAdditivity(boolean additivity) {
         this.additivity = additivity;
     }
 
 	public void addAppenderRef(String appenderName) {
 		appenderRefs.add(appenderName);
 	}
 
     public HashSet<String> getAppenderRefs() {
         return appenderRefs;
     }
 
     public void setAppenderRefs(HashSet<String> appenderRefs) {
         this.appenderRefs = appenderRefs;
     }
 
     public String getClassName() {
         return className;
     }
 
     public void setClassName(String className) {
         this.className = className;
     }
 
     public HashMap<String, String> getParams() {
         return params;
     }
 
     public void setParams(HashMap<String, String> params) {
         this.params = params;
     }
     
     public void addParam(String key, String value) {
         params.put(key, value);
     }
 
     public Level getLevel() {
         return level;
     }
 
     public void setLevel(Level level) {
         this.level = level;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Logger other = (Logger) obj;
         if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 3;
         hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
         return hash;
     }
 	
 	
 	public void generateProperties(Properties p) {
 		// log4j.logger.logger_name
 		String prefixKey = ( isCategory ? PropertiesParser.CATEGORY_PREFIX : PropertiesParser.LOGGER_PREFIX ) + name;
 		
 		StringBuilder value = new StringBuilder();
 		
 		// level, appdenderRefs
 		if (level != null) value.append(level.getValues());
 		for (String appenderRef : appenderRefs) {
 			value.append(", ");
 			value.append(appenderRef);
 		}
 		p.setProperty(prefixKey, value.toString());
 		
 		// additivity
 		if (!additivity) p.setProperty(PropertiesParser.ADDITIVITY_PREFIX + name, "false");
 	}
 	
 
     public void generateXML(Document doc, Element config) {
 
         Element logger;
         if (!isCategory) {
             logger = doc.createElement("logger");
         } else {
             logger = doc.createElement("category");
         }
        logger.setAttribute("loggerName", name);
         if (className != null) {
             logger.setAttribute("class", className);
         }
         if (additivity) {
             logger.setAttribute("additivity", "true");
         } else {
             logger.setAttribute("additivity", "false");
         }
 
 
         if (!params.isEmpty()) {
             Iterator<Entry<String, String>> it = params.entrySet().iterator();
             while (it.hasNext()) {
             	Entry<String, String> e = it.next();
                 Element param = doc.createElement("param");
 
                 param.setAttribute("name", e.getKey());
                 param.setAttribute("value", e.getValue());
                 logger.appendChild(param);
 
             }
 
         }
 
         if (level != null) {
             level.generateXML(doc, logger);
         }
 
         for (String ref : appenderRefs) {
             Element apRef = doc.createElement("appender-ref");
             apRef.setAttribute("ref", ref);
             logger.appendChild(apRef);
         }
 
 
         config.appendChild(logger);
 
     }
 
 }
