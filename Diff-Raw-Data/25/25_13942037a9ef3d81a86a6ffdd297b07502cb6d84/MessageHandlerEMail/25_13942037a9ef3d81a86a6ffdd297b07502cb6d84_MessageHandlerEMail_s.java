 /*
  * Copyright 2011-2013 JAIDE GmbH
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package de.jaide.courier.email;
 
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.mail.DefaultAuthenticator;
 import org.apache.commons.mail.EmailAttachment;
 import org.apache.commons.mail.EmailException;
 import org.apache.commons.mail.HtmlEmail;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import de.jaide.courier.MessageHandler;
 import de.jaide.courier.exception.CourierException;
 import de.jaide.courier.exception.MissingParameterException;
 import freemarker.template.Configuration;
 import freemarker.template.DefaultObjectWrapper;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 
 /**
  * A handler for e-mail messages, based on the Freemarker templating system.
  * 
  * @author Rias A. Sherzad, JAIDE GmbH // http://www.jaide.de
  */
 public class MessageHandlerEMail implements MessageHandler {
   /**
    * The suffixes for the Freemarker-templated files.
    */
   private static final String TEMPLATENAME_SUFFIX_SUBJECT = "subject";
   private static final String TEMPLATENAME_SUFFIX_HEADERS = "headers";
   private static final String TEMPLATENAME_SUFFIX_BODY = "body";
 
   /**
    * Mapping parameters known to this handler.
    */
   public static final String MAPPING_PARAM_CONFIGURATION_NAME = "configurationName";
   public static final String MAPPING_PARAM_TEMPLATE_PATH = "templatePath";
   public static final String MAPPING_PARAM_TEMPLATE_PATH_CLASS = "templatePathClass";
   public static final String MAPPING_PARAM_TEMPLATE_PATH_FILE = "templatePathFile";
   public static final String MAPPING_PARAM_TEMPLATE_NAME = "templateName";
   public static final String MAPPING_PARAM_TEMPLATE_TYPE = "templatetype";
   public static final String MAPPING_PARAM_RECIPIENT_FIRSTNAME = "recipientFirstname";
   public static final String MAPPING_PARAM_RECIPIENT_LASTNAME = "recipientLastname";
   public static final String MAPPING_PARAM_RECIPIENT_EMAIL = "recipientEMail";
   public static final String MAPPING_PARAM_CC_RECIPIENT_FIRSTNAME = "ccRecipientFirstname";
   public static final String MAPPING_PARAM_CC_RECIPIENT_LASTNAME = "ccRecipientLastname";
   public static final String MAPPING_PARAM_CC_RECIPIENT_EMAIL = "ccRecipientEMail";
   public static final String MAPPING_PARAM_SENDER_FIRSTNAME = "senderFirstname";
   public static final String MAPPING_PARAM_SENDER_LASTNAME = "senderLastname";
   public static final String MAPPING_PARAM_SENDER_EMAIL = "senderEMail";
   public static final String MAPPING_PARAM_ATTACHMENTS = "attachments";
 
   /**
    * The obligatory mapping parameters define which parameters HAVE to be provided when calling this handler.
    */
   private static List<String> obligatoryMappingParameters = new ArrayList<String>();
   static {
     obligatoryMappingParameters.add(MAPPING_PARAM_CONFIGURATION_NAME);
     obligatoryMappingParameters.add(MAPPING_PARAM_TEMPLATE_NAME);
     obligatoryMappingParameters.add(MAPPING_PARAM_RECIPIENT_FIRSTNAME);
     obligatoryMappingParameters.add(MAPPING_PARAM_RECIPIENT_LASTNAME);
     obligatoryMappingParameters.add(MAPPING_PARAM_RECIPIENT_EMAIL);
   }
 
   /**
    * The available SMTP configurations.
    */
   private Map<String, SmtpConfiguration> smtpConfigurations = new HashMap<String, SmtpConfiguration>();
 
   /**
    * The Freemarker templating configuration.
    */
   private Configuration templatingConfiguration;
 
   /**
    * Caches Freemarker templates.
    */
   private Map<String, Template> cachedTemplates = new HashMap<String, Template>();
 
   /**
    * Creates an instance of this class, loads the SMTP configuration and sets the Freemarker templating configuration.
    * 
    * @param smtpConfiguration The SMTP configuration to load. Needs to be an absolute URL, e.g. "/configs/smtp.json".
    * @throws IOException Thrown, if the SMTP configuration couldn't be read.
    */
   public MessageHandlerEMail(String smtpConfiguration) throws IOException {
     /*
      * Load the SMTP configurations.
      */
     loadSmtpConfigurations(smtpConfiguration);
 
     /*
      * Instantiate the Freemarker templating engine.
      */
     templatingConfiguration = new Configuration();
     templatingConfiguration.setObjectWrapper(new DefaultObjectWrapper());
   }
 
   /**
    * Loads the SMTP configuration from a JSON file.
    * 
   * @param smtpConfiguration The SMTP configuration to load. Needs to be an absolute URL, e.g. "/configs/smtp.json" that is loaded from the
    *          classpath.
    * @throws IOException Thrown, if the SMTP configuration couldn't be read.
    */
   private void loadSmtpConfigurations(String smtpConfigurationJsonLocation) {
     /*
      * Now load the SMTP configuration JSON file and try to parse it.
      */
     try {
       /*
        * Load the JSON-based SMTP configuration file.
        */
       JSONArray keyArray = (JSONArray) new JSONParser().parse(IOUtils.toString((InputStream) MessageHandlerEMail.class.getResource(
           smtpConfigurationJsonLocation).getContent()));
 
       /*
        * Iterate through the outer array
        */
       for (int i = 0; i < keyArray.size(); i++) {
 
         /*
          * Iterate through the inner array of configuration keys.
          */
         JSONObject keysArray = (JSONObject) keyArray.get(i);
         for (Object keyObject : keysArray.keySet()) {
 
           /*
            * Get the name of the key...
            */
           String key = (String) keyObject;
           JSONObject configArray = (JSONObject) keysArray.get(key);
 
           /*
            * ... and load the associated configuration values.
            */
           String smtpHostname = (String) configArray.get("smtpHostname");
           Long smtpPort = (Long) (configArray.get("smtpPort"));
           Boolean tls = (Boolean) configArray.get("tls");
           Boolean ssl = (Boolean) configArray.get("ssl");
           String username = (String) configArray.get("username");
           String password = (String) configArray.get("password");
           String fromEMail = (String) configArray.get("fromEMail");
           String fromSenderName = (String) configArray.get("fromSenderName");
 
           /*
            * Use the obtained values and create a new SMTP configuration.
            */
           SmtpConfiguration smtpConfiguration = new SmtpConfiguration(key, smtpHostname, smtpPort.intValue(), tls, ssl, username, password,
               fromEMail, fromSenderName);
           smtpConfigurations.put(key, smtpConfiguration);
         }
       }
     } catch (IOException ioe) {
       throw new RuntimeException("SMTP configuration not found at '" + smtpConfigurationJsonLocation + "'", ioe);
     } catch (ParseException pe) {
       throw new RuntimeException("SMTP configuration couldn't be loaded from '" + smtpConfigurationJsonLocation + "'", pe);
     }
   }
 
   /**
    * Returns the filename, excluding the path, of the given template name.<br/>
    * If "activate" is given as the <code>templateName</code> and "TEMPLATENAME_SUFFIX_SUBJECT" as the <code>templatePart</code> then
    * "activate_subject.ftl" is returned.
    * 
    * @param templateName The template name to return the filename for.
    * @param templatePart The template part, e.g. header, subject or body. Should be one of TEMPLATENAME_SUFFIX_SUBJECT,
    *          TEMPLATENAME_SUFFIX_HEADERS or TEMPLATENAME_SUFFIX_BODY.
    * @param isHtml If set to true then the HTML template with the suffix .html is returned. Only makes sense in connection with templatePart
    *          set to TEMPLATENAME_SUFFIX_BODY.
    * @return The filename of the template, excluding the path.
    */
   public String retrieveTemplateFilename(String templateName, String templatePart, boolean isHtml) {
     if (TEMPLATENAME_SUFFIX_BODY.equals(templatePart))
       return templateName + "_" + templatePart + ".ftl" + (isHtml ? ".html" : ".txt");
     else
       return templateName + "_" + templatePart + ".ftl";
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see de.jaide.courier.MessageHandler#handleMessage(java.util.Map)
    */
  @Override
   public void handleMessage(Map<String, Object> parameters) throws CourierException {
     /*
      * Check if the obligatory parameters are there.
      */
     for (String key : obligatoryMappingParameters)
       if (!parameters.containsKey(key))
         throw new CourierException(new MissingParameterException("The parameter '" + key + "' was expected but couldn't be found."));
 
     /*
      * Now retrieve the obligatory parameters.
      */
     String configurationName = (String) parameters.get(MAPPING_PARAM_CONFIGURATION_NAME);
     String templatePath = (String) parameters.get(MAPPING_PARAM_TEMPLATE_PATH);
     if ((templatePath != null) && (!templatePath.endsWith("/")))
       templatePath += "/";
     else if (templatePath == null)
       templatePath = "/";
 
     Class<?> templatePathClass = (Class<?>) parameters.get(MAPPING_PARAM_TEMPLATE_PATH_CLASS);
     File templatePathFile = (File) parameters.get(MAPPING_PARAM_TEMPLATE_PATH_FILE);
 
     String templateName = (String) parameters.get(MAPPING_PARAM_TEMPLATE_NAME);
     TemplateTypeEnum templateTypeEnum = (TemplateTypeEnum) parameters.get(MAPPING_PARAM_TEMPLATE_TYPE);
     String recipientFirstname = (String) parameters.get(MAPPING_PARAM_RECIPIENT_FIRSTNAME);
     String recipientLastname = (String) parameters.get(MAPPING_PARAM_RECIPIENT_LASTNAME);
     String recipientEMail = (String) parameters.get(MAPPING_PARAM_RECIPIENT_EMAIL);
     String ccRecipientFirstname = (String) parameters.get(MAPPING_PARAM_CC_RECIPIENT_FIRSTNAME);
     String ccRecipientLastname = (String) parameters.get(MAPPING_PARAM_CC_RECIPIENT_LASTNAME);
     String ccRecipientEMail = (String) parameters.get(MAPPING_PARAM_CC_RECIPIENT_EMAIL);
 
     /*
      * The next three parameters are optional, as they might also be specified in the SMTP configuration file. If they are specified they
      * tell us to overwrite what was specified in the SMTP configuration file and use those values (firstname, lastname, e-mail) for the
      * sender instead.
      */
     String senderFirstname = null;
     if (parameters.containsKey(MAPPING_PARAM_SENDER_FIRSTNAME))
       senderFirstname = (String) parameters.get(MAPPING_PARAM_SENDER_FIRSTNAME);
 
     String senderLastname = null;
     if (parameters.containsKey(MAPPING_PARAM_SENDER_LASTNAME))
       senderLastname = (String) parameters.get(MAPPING_PARAM_SENDER_LASTNAME);
 
     String senderEMail = null;
     if (parameters.containsKey(MAPPING_PARAM_SENDER_EMAIL))
       senderEMail = (String) parameters.get(MAPPING_PARAM_SENDER_EMAIL);
 
     /*
      * Will be used for parsing the templates.
      */
     StringWriter writer = new StringWriter();
 
     try {
       /*
        * Construct the template that we're about to process. First set the path to load the template(s) from. In case a Directory was given
        * as the base for template loading purposes use that instead.
        */
       if (templatePathFile == null) {
         if (templatePathClass == null)
           templatingConfiguration.setClassForTemplateLoading(getClass(), templatePath);
         else
           templatingConfiguration.setClassForTemplateLoading(templatePathClass, templatePath);
       } else
         templatingConfiguration.setDirectoryForTemplateLoading(templatePathFile);
 
       /*
        * Get the headers and Freemarker-parse them. If there are no headers then ignore the errors. The file has to be one header per line,
        * header name and value separated by a colon (":").
        */
       Template template;
 
       Map<String, String> headers = new HashMap<String, String>();
       String headersFilename = retrieveTemplateFilename(templateName, MessageHandlerEMail.TEMPLATENAME_SUFFIX_HEADERS, true);
       if (headersFilename != null) {
         try {
           template = loadTemplate(headersFilename);
           template.process(parameters, writer);
           BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
           headers = new HashMap<String, String>();
           String str = "";
           while ((str = reader.readLine()) != null) {
             String[] split = str.split(":");
             if (split.length > 1)
               headers.put(split[0].trim(), split[1].trim());
           }
         } catch (IOException ioe) {
           // The header file is optional, hence we don't care if it couldn't be found
         }
       }
 
       /*
        * Get the subject line and Freemarker-parse it.
        */
       String subject = loadAndProcessTemplate(parameters, MessageHandlerEMail.TEMPLATENAME_SUFFIX_SUBJECT, templateName, false);
 
       /*
        * Get the body content and Freemarker-parse it.
        */
       String contentText = null;
       String contentHtml = null;
 
       /*
        * Load all requested versions of the template.
        */
       if (templateTypeEnum == TemplateTypeEnum.TEXT) {
         contentText = loadAndProcessTemplate(parameters, MessageHandlerEMail.TEMPLATENAME_SUFFIX_BODY, templateName, false);
       } else if (templateTypeEnum == TemplateTypeEnum.HTML) {
         contentHtml = loadAndProcessTemplate(parameters, MessageHandlerEMail.TEMPLATENAME_SUFFIX_BODY, templateName, true);
       } else if (templateTypeEnum == TemplateTypeEnum.BOTH) {
         contentText = loadAndProcessTemplate(parameters, MessageHandlerEMail.TEMPLATENAME_SUFFIX_BODY, templateName, false);
         contentHtml = loadAndProcessTemplate(parameters, MessageHandlerEMail.TEMPLATENAME_SUFFIX_BODY, templateName, true);
       } else if (templateTypeEnum == TemplateTypeEnum.ANY) {
         try {
           contentText = loadAndProcessTemplate(parameters, MessageHandlerEMail.TEMPLATENAME_SUFFIX_BODY, templateName, false);
         } catch (IOException ioe) {
         } finally {
           try {
             contentHtml = loadAndProcessTemplate(parameters, MessageHandlerEMail.TEMPLATENAME_SUFFIX_BODY, templateName, true);
           } catch (IOException ioe) {
             throw new RuntimeException("Neither the HTML nor the TEXT-only version of the e-mail template '" + templateName
                 + "' could be found. Are you sure they reside in '" + templatePath + "' as '" + templateName + "_body.ftl.html' or '"
                 + templateName + "_body.ftl.txt'?", ioe);
           }
         }
       }
 
       /*
        * Set the parameters that are identical for that sender, for all recipients.
        * Note: attachments may not be removed once they have been attached, hence the performance-improving caching had to be removed.
        */
       SmtpConfiguration smtpConfiguration = (SmtpConfiguration) smtpConfigurations.get(configurationName);
       HtmlEmail htmlEmail = new HtmlEmail();
       htmlEmail.setHostName(smtpConfiguration.getSmtpHostname());
       htmlEmail.setSmtpPort(smtpConfiguration.getSmtpPort());
       if (smtpConfiguration.isTls()) {
         htmlEmail.setAuthenticator(new DefaultAuthenticator(smtpConfiguration.getUsername(), smtpConfiguration.getPassword()));
         htmlEmail.setStartTLSEnabled(smtpConfiguration.isTls());
       }
       htmlEmail.setSSLOnConnect(smtpConfiguration.isSsl());
 
       /*
        * Changing the sender, to differ from what was specified in the particular SMTP configuration, is optional. As explained above this
        * will only happen if they were specified by the caller.
        */
       if ((senderFirstname != null) || (senderLastname != null) || (senderEMail != null))
         htmlEmail.setFrom(senderEMail, senderFirstname + " " + senderLastname);
       else
         htmlEmail.setFrom(smtpConfiguration.getFromEMail(), smtpConfiguration.getFromSenderName());
 
       /*
        * Set the parameters that differ for each recipient.
        */
       htmlEmail.addTo(recipientEMail, recipientFirstname + " " + recipientLastname);
       if ((ccRecipientFirstname != null) && (ccRecipientLastname != null) && (ccRecipientEMail != null))
         htmlEmail.addCc(ccRecipientEMail, ccRecipientFirstname + " " + ccRecipientLastname);
       htmlEmail.setHeaders(headers);
       htmlEmail.setSubject(subject);
 
       /*
        * Set the HTML and Text version of the e-mail body.
        */
       if (contentHtml != null)
         htmlEmail.setHtmlMsg(contentHtml);
       if (contentText != null)
         htmlEmail.setTextMsg(contentText);
 
       /*
        * Add attachments, if available.
        */
       if (parameters.containsKey(MAPPING_PARAM_ATTACHMENTS)) {
         @SuppressWarnings("unchecked")
         List<EmailAttachment> attachments = (List<EmailAttachment>) parameters.get(MAPPING_PARAM_ATTACHMENTS);
         for (EmailAttachment attachment : attachments) {
           htmlEmail.attach(attachment);
         }
       }
 
       /*
        * Finished - now send the e-mail.
        */
       htmlEmail.send();
     } catch (IOException ioe) {
       throw new CourierException(ioe);
     } catch (TemplateException te) {
       throw new CourierException(te);
     } catch (EmailException ee) {
       throw new CourierException(ee);
     } finally {
       if (writer != null) {
         writer.flush();
 
         try {
           writer.close();
         } catch (IOException ioe) {
           ioe.printStackTrace();
         }
       }
     }
   }
 
   /**
    * Returns the Freemarker-processed String-content of the specified template part and name.
    * 
    * @param parameters The Freemarker-variables/parameters to process.
    * @param templatePart The template part of return. Should be any of TEMPLATENAME_SUFFIX_HEADERS, TEMPLATENAME_SUFFIX_SUBJECT or
    *          TEMPLATENAME_SUFFIX_BODY.
    * @param templateName The name of the template to load.
    * @param isHtml Return the HTML version of the template? Only makes sense for TEMPLATENAME_SUFFIX_BODY.
    * @throws IOException Thrown if the template couldn't be found.
    * @throws TemplateException Thrown if the Freemarker-variables/parameters couldn't be processed.
    */
   private String loadAndProcessTemplate(Map<String, Object> parameters, String templatePart, String templateName, boolean isHtml)
       throws IOException, TemplateException {
     StringWriter writer = new StringWriter();
     Template template = templatingConfiguration.getTemplate(retrieveTemplateFilename(templateName, templatePart, isHtml));
     template.process(parameters, writer);
     return writer.toString();
   }
 
   /**
    * Loads the specified template by filename - does some caching as well.
    * 
    * @param templateName The name of the template to load.
    * @return The Template.
    * @throws IOException Thrown if the template couldn't be found.
    */
   private Template loadTemplate(String templateName) throws IOException {
     /*
      * Try to load the template from the cache.
      */
     Template template = cachedTemplates.get(templateName);
 
     if (template == null) {
       /*
        * It's not there - load it from the filesystem and cache it for future use.
        */
       template = templatingConfiguration.getTemplate(templateName);
       cachedTemplates.put(templateName, template);
     }
 
     return template;
   }
 }
