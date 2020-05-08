 package com.mymed.controller.core.manager.mailtemplates.v2;
 
 
 import java.io.InputStream;
 import java.util.Map;
 
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.mailtemplates.MailTemplate;
 import com.mymed.controller.core.manager.storage.IStorageManager;
 import com.mymed.controller.core.manager.storage.v2.StorageManager;
 import com.mymed.utils.PubSub;
 
 /** Store mail templates in the backend */
 public class MailTemplateManager extends com.mymed.controller.core.manager.mailtemplates.MailTemplateManager{
     
     // ---------------------------------------------------------------------------
     // Constants
     // ---------------------------------------------------------------------------
     
     /** Format of the resource filenames */
     static private final String MAIL_TEMPLATE_RES_PATH = "mail_templates/%s-%s-%s.ftl.xml"; 
     
     /** Defalut languages */
     static private final String DEFAULT_LANG = "fr";
     
     /** Column family name */
     static private final  String CF_MAILTEMPLATES = "MailTemplates";
     
     /** Tag name for the subject : Used both in XML and as Cassandra column name */
     static public final  String SUBJECT_TAG = "subject";
     static public final  String BODY_TAG = "body";
     
     public MailTemplateManager() {
         super(new StorageManager());
     }
     
     public MailTemplateManager(IStorageManager storageManager) {
         super(storageManager);
     }
     
     /**
      *  Get a template for a specific application, namespace and language.
      *  The manager searches successively for :
     *  * Template in Cassandra, with <applicationID>, <namespace>, <lang>, <version>
      *  * Template in Cassandra, with <applicationID>, <namespace>, <default-lang>
     *  * Template in WAR resources : mail-templates/<applicationID>#<namespace>-<lang>.flt.xml 
     *  * Template in WAR resources : mail-templates/<applicationID>#<namespace>-<default-lang>.flt.xml 
     *  * Template in WAR resources : default-<lang>.flt.xml
     *  * Template in WAR resources : default-<default-lang>.flt.xml 
      */
     public MailTemplate getTemplate(
             String applicationID,
             String namespace,
             String language,
             String version) 
     {
         MailTemplate res = null;
         
         // Search in cassandra with <applicationID>,<namespace>, <lang>        
         if (res == null) res = getTemplateFromCassandra(applicationID, namespace, language);
         
         // Search in cassandra with <applicationID>,<namespace>, DEFAULT_LANG        
         if (res == null) res = getTemplateFromCassandra(applicationID, namespace, DEFAULT_LANG);
         
         // Search for resources with <applicationID>,<namespace>, <lang>
         if (res == null) res = getTemplateFromResources(applicationID, namespace, language, version);
         
         // Search for resources with <applicationID>,<namespace>, DEFAULT_LANG
         if (res == null) res = getTemplateFromResources(applicationID, namespace, DEFAULT_LANG, version);
         
         // Search for resources with "default" <lang>
         if (res == null) res = getTemplateFromResources("default", null, language, version);
         
         // Search for resources with "default" DEFAULT_LANG
         if (res == null) res = getTemplateFromResources("default", null, DEFAULT_LANG, version);
         
         // Still not found ??
         if (res == null) {
             throw new InternalBackEndException(
                    "No template found for appId:%s, ns:%s, lang:%s, default-lang:%s", 
                     applicationID,
                     namespace,
                     language,
                     DEFAULT_LANG);
         }
         
         return res;
         
     }
     
     /** Search for a template in the resource folder.
      * @return null if no resource is found */
     private MailTemplate getTemplateFromResources(
             String applicationID,
             String namespace,
             String language,
             String version) 
     {
         // Build Prefix = application#namespace
         String prefix = PubSub.makePrefix(applicationID, namespace, "#");
         
         // Build filename
         String resPath = String.format(
                 MAIL_TEMPLATE_RES_PATH,
                 prefix,
                 language,
                 version);
         
         // Try to get input stream
         InputStream is = this.getClass().getClassLoader().getResourceAsStream(
                 resPath);
        
         // Not found
         if (is == null) return null;
         
         // Create a mail template from this XML file
         return new MailTemplate(is);
     }
     
     /** Search for a template in cassandra
      * @return null if template not found */
     private MailTemplate getTemplateFromCassandra(
             String applicationID,
             String namespace,
             String language)
     {
         // Build key "<appID>[#<ns>]-<lang>"
         String key = PubSub.makePrefix(applicationID, namespace, "#");
         key += "-" + language; 
         
         LOGGER.info("Searching for mail template in cassandra. key={}", key);
         
         // Try to get it from cassandra
         Map<String, String> map = this.storageManager.selectAllStr(CF_MAILTEMPLATES, key);
         
         // Not found !
         if (map == null) return null;
         if (map.size() == 0) return null;
         
         // Get the column values (subject & body)
         String subject = map.get(SUBJECT_TAG);
         String body = map.get(BODY_TAG);
         
         // Build a template object
         return new MailTemplate(subject, body);
     }
     
 }
