 package org.esgf.searchConfig;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.log4j.Logger;
 import org.esgf.metadata.JSONException;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 
 @Controller
 @RequestMapping("/lasconfigurationproxy")
 public class LASConfigurationController {
 
     private final static Logger LOG = Logger.getLogger(LASConfigurationController.class);
 
     //Name and location of the file (In this case, the base package location)
     private final static String WRITEABLE_LASCONFIG_FILE = System.getenv("CATALINA_HOME") + "/webapps/esgf-web-fe/WEB-INF/classes/lasconfig.properties";
     
     private final static String LASCONFIG_PROPERTIES_FILE = "/esg/config/lasconfig.properties";
     
     
     @RequestMapping(method=RequestMethod.GET)
     public @ResponseBody String doGet()  {
     
         List<String> regexes = readRegexes();
         
         //System.out.println(regexes.size());
     
         LASConfiguration las = new LASConfiguration(regexes);
         
         System.out.println("LAS REGEXES:\n\t" + las.toJSON());
         
         return las.toJSON();
     }
     
     public List<String> readRegexes() {
 
         List<String> regexTokens = new ArrayList<String>();
         
         Properties properties = new Properties();
         String propertiesFile = LASCONFIG_PROPERTIES_FILE;
         
         try {
             properties.load(new FileInputStream(propertiesFile));
             
             for(Object key : properties.keySet()) {
                 String value = (String)properties.get(key);
                 
                 //System.out.println("Key: " + key + " Value: " + value);
                 if(key.equals("RestrictedRegexes")) {
                     
                     String [] tokens = value.split(";");
                     for(int i=0;i<tokens.length;i++) {
                         regexTokens.add(tokens[i]);
                     }
                     
                     
                 }
                 
             }
             
             
         } catch (FileNotFoundException e) {
             // TODO Auto-generated catch block
            e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
            e.printStackTrace();
         }
         
         
         
         return regexTokens;
         
     }
     
     public static void main(String [] args) {
         LASConfigurationController controller = new LASConfigurationController();
         
         controller.doGet();
         
     }
     
 }
