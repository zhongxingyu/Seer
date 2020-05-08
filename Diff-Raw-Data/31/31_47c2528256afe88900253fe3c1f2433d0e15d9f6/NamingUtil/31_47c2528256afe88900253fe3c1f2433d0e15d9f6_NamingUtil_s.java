 package gov.nih.nci.gss.util;
 
 import gov.nih.nci.gss.support.SimpleName;
 
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import org.apache.log4j.Logger;
 import org.hibernate.SessionFactory;
 import org.hibernate.classic.Session;
 
 /**
  * Utilities for translating and generating names.
  * 
  * @author <a href="mailto:rokickik@mail.nih.gov">Konrad Rokicki</a>
  */
 public class NamingUtil {
 
     private static Logger log = Logger.getLogger(NamingUtil.class);
     
     private Map<String,String> simpleServiceNameMap = new LinkedHashMap<String,String>();
     private Map<String,String> simpleHostNameMap = new LinkedHashMap<String,String>();
     
     public NamingUtil(SessionFactory sessionFactory) {
 
         Session s = sessionFactory.openSession();
         try {
             log.info("Configuring naming map...");
             List<SimpleName> names = s.createCriteria(SimpleName.class).list();
             
             for(SimpleName simpleName : names) {
                 String pattern = simpleName.getPattern();
                 
                 try {
                     Pattern.compile(pattern);
                     String type = simpleName.getType();
                     if ("ServiceName".equals(type)) {
                         log.info("Service pattern: /"+pattern+"/"+simpleName.getSimpleName()+"/");
                         simpleServiceNameMap.put(
                             simpleName.getPattern(), simpleName.getSimpleName());
                     }
                     else if ("HostName".equals(type)) {
                         log.info("Host pattern: /"+pattern+"/"+simpleName.getSimpleName()+"/");
                         simpleHostNameMap.put(
                             simpleName.getPattern(), simpleName.getSimpleName());
                     }
                     else {
                         log.info("Unknown SimpleName type: "+
                             type+" (id="+simpleName.getId()+")");
                     }
                 }
                 catch (PatternSyntaxException e) {
                     log.warn("Invalid SimpleName pattern: "+pattern+" " +
                     		"("+e.getMessage()+")");
                 }
             }
             log.info("Naming map configuration complete.");
         }
         finally {
             s.close();
         }
     }
     
     /**
      * Attempt to generate a simple name for the input name. If no simple name
      * pattern matches then the input is returned unchanged.
      * @param originalName a name to simplify
      * @return simple name
      */
     private String getSimpleName(String originalName, 
             Map<String,String> simpleNameMap) {
 
         for (String pattern : simpleNameMap.keySet()) {
             String replacement = simpleNameMap.get(pattern);
            
            Matcher m = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(originalName);
            String finalName = m.replaceAll(replacement);
            
            return finalName;
         }
         
        return originalName;
     } 
 
     public String getSimpleServiceName(String originalName) {
         String simpleName = originalName.
             replaceAll("(Grid|Data|Analytical)?(Service|Svc)$", "").
             replaceAll("^Ca", "ca");
         return getSimpleName(simpleName, simpleServiceNameMap);
     }
     
     public String getSimpleHostName(String originalName) {
         return getSimpleName(originalName, simpleHostNameMap);
     }
     
    
 }
