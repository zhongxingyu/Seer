 package org.makumba.parade.init;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import org.apache.log4j.Logger;
 
 public class ParadeProperties {
 	
 	static public String paradeBase = "." + java.io.File.separator;
 	static String fileName = paradeBase + "parade.properties";
 	
 	private static Properties config;
 	
 	static public String paradeBaseRelativeToTomcatWebapps = ".."
         + File.separator + "..";
 	
 	static Logger logger = Logger.getLogger(ParadeProperties.class.getName());
 	
 	static {
 		
         try {
             config = new Properties();
             config.load(new FileInputStream(fileName));
         } catch (Throwable t) {
             logger.error("Error while loading parade.properties",t);
         }
     }
 	
 	public static String getProperty(String configProperty) {
         return config.getProperty(configProperty);
     }
 	
 	public static List getElements(String configProperty) {
 		List l = new LinkedList();
 		
 		String s = getProperty(configProperty);
 		if(s == null)
			s = "noSuchProperty";
 		StringTokenizer st = new StringTokenizer(s,",");
 		while(st.hasMoreElements()) {
 			l.add(((String)st.nextToken()).trim());
 		}
 		return l;
 	}
 	
 	
 	
 }
