 package net.jawr.web.resource.bundle.locale.message;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Properties;
 import java.util.ResourceBundle;
 import java.util.StringTokenizer;
 
 import javax.servlet.ServletContext;
 
 import net.jawr.web.exception.BundlingProcessException;
 import net.jawr.web.resource.bundle.IOUtils;
 import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
 import net.jawr.web.resource.bundle.factory.util.RegexUtil;
 import net.jawr.web.resource.bundle.generator.GeneratorContext;
 
import org.apache.log4j.Logger;
 import org.springframework.context.NoSuchMessageException;
 import org.springframework.context.support.ReloadableResourceBundleMessageSource;
 
 /**
  * override the implementation of jawr forcedly, to enable loading properties with charset encoding 
  * 
  * the createScript method is overrided
  *
  */
 public class MessageBundleScriptCreator {
 
    private static final Logger LOGGER = Logger.getLogger(MessageBundleScriptCreator.class.getName());
     public static final String DEFAULT_NAMESPACE = "messages";
     private static final String SCRIPT_TEMPLATE = "/net/jawr/web/resource/bundle/message/messages.js";
     protected static StringBuffer template;
     protected String configParam;
     protected String namespace;
     private String filter;
     protected Locale locale;
     private List filterList;
     protected ServletContext servletContext;
     
     
     public MessageBundleScriptCreator(GeneratorContext context) {
         super();
         this.servletContext = context.getServletContext();
         if(null == template)
             template = loadScriptTemplate();
         
         this.locale = context.getLocale();
         
         
         // Set the namespace
         namespace = context.getParenthesesParam();
         namespace = null == namespace ? DEFAULT_NAMESPACE : namespace;
         
 
         // Set the filter
         filter = context.getBracketsParam();
         if(null != filter) {
             StringTokenizer tk = new StringTokenizer(filter,"\\|");
             filterList = new ArrayList();
             while(tk.hasMoreTokens())
                 filterList.add(tk.nextToken());
         }
         
         this.configParam = context.getPath();
     }
     
     /**
      * Loads a template containing the functions which convert properties into methods. 
      * @return
      */
     private StringBuffer loadScriptTemplate() {
         StringWriter sw = new StringWriter();
         InputStream is = null;
         try {
             is = ClassLoaderResourceUtils.getResourceAsStream(SCRIPT_TEMPLATE,this);
             IOUtils.copy(is, sw);
         } catch (IOException e) {
            LOGGER.fatal("a serious error occurred when initializing MessageBundleScriptCreator");
             throw new BundlingProcessException("Classloading issues prevent loading the message template to be loaded. ",e);
         }finally{
             IOUtils.close(is);
         }
         
         return sw.getBuffer();
     }
 
 
     /**
      * overrided version 
      * 
      * Loads the message resource bundles specified and uses a BundleStringJasonifier to generate the properties. 
      * @return
      */
     public Reader createScript(Charset charset){
         String[] basenames = configParam.split("\\|");
         ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
         messageSource.setDefaultEncoding(charset.name());
         messageSource.setBasenames(basenames);
 
         Properties props = new Properties();
         for (String basename: basenames) {
             ResourceBundle bundle;
             if(null != locale){
                 bundle = ResourceBundle.getBundle(basename,locale);
             }else{
                 bundle = ResourceBundle.getBundle(basename);
             }
             Enumeration<String> keys = bundle.getKeys();
             HashSet<String> allPropertyNames = new HashSet<String>();
             
             while(keys.hasMoreElements()){
                 allPropertyNames.add(keys.nextElement());
             }
             // Pass all messages to a properties file. 
             for (String key : allPropertyNames) {
                 if(matchesFilter(key)){
                     try {
                         String msg = messageSource.getMessage(key, new Object[0], locale);
                         props.put(key, msg);
                     } catch (NoSuchMessageException e) {
                         // This is expected, so it's OK to have an empty catch block. 
                         if(LOGGER.isDebugEnabled()){
                             LOGGER.debug("Message key [" + key + "] not found.");
                         }
                     }
                 }
             }
         }
         return doCreateScript(props);
     }
 
     /**
      * Loads the message resource bundles specified and uses a BundleStringJasonifier to generate the properties. 
      * @return
      */
     public Reader createScript(Charset charset, ResourceBundle bundle){
         
         Properties props = new Properties();
         updateProperties(bundle, props);
         return doCreateScript(props);
     }
     
     /**
      * Loads the message resource bundles specified and uses a BundleStringJasonifier to generate the properties. 
      * @return
      */
     public void updateProperties(ResourceBundle bundle, Properties props){
         
         Enumeration keys = bundle.getKeys();
         
         while(keys.hasMoreElements()) {
             String key = (String) keys.nextElement();
             
             if(matchesFilter(key)) {
                     String value = bundle.getString(key);
                     props.put(key, value);
             }
         }
     }
     
     /**
      * @return
      */
     protected Reader doCreateScript(Properties props) {
         BundleStringJasonifier bsj = new BundleStringJasonifier(props);
         String script = template.toString();
         String messages = bsj.serializeBundles().toString();
         script = script.replaceFirst("@namespace", RegexUtil.adaptReplacementToMatcher(namespace));
         script = script.replaceFirst("@messages", RegexUtil.adaptReplacementToMatcher(messages));
         
         return new StringReader(script);
     }
     
     /**
      * Determines wether a key matches any of the set filters. 
      * @param key
      * @return
      */
     protected boolean matchesFilter(String key) {
         boolean rets = (null == filterList);
         if(!rets) {
             for(Iterator it = filterList.iterator();it.hasNext() && !rets; )
                 rets = key.startsWith((String)it.next());
         }
         return rets;
             
     }
 }
