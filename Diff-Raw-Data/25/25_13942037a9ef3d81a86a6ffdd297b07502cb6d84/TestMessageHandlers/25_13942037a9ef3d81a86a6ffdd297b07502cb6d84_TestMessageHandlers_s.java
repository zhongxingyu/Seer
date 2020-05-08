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
 package de.jaide.courier;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.mail.EmailAttachment;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import de.jaide.courier.email.MessageHandlerEMail;
 import de.jaide.courier.email.TemplateTypeEnum;
 
 /**
  * Tests the implemented message handlers.
  * 
  * @author Rias A. Sherzad, JAIDE GmbH // http://www.jaide.de
  */
 public class TestMessageHandlers {
 
   @BeforeClass
   public void init() {
   }
 
   /**
    * Test the message handler for e-mails.
    * 
    * @throws IOException
    */
   @Test
   public void testMessageHandlerForEMails_Signup() throws IOException {
     Map<String, Object> mappedParameters = new HashMap<String, Object>();
 
     /*
      * Configuration parameters
      */
 
     // This is the name of the SMTP-configuration, found in the smtp.json, to use for sending the e-mail.
     mappedParameters.put(MessageHandlerEMail.MAPPING_PARAM_CONFIGURATION_NAME, "info");
 
     // Load the templates using a regular Java classloader, from the folder /email_templates/en/ found in the root of the classpath.
     mappedParameters.put(MessageHandlerEMail.MAPPING_PARAM_TEMPLATE_PATH, "/email_templates/en/");
     mappedParameters.put(MessageHandlerEMail.MAPPING_PARAM_TEMPLATE_PATH_CLASS, this.getClass());
 
     // Or, alternatively, give it a directory path to load the templates from. If possible always stick with the TEMPLATE_PATH_CLASS option.
     // mappedParameters.put(MessageHandlerEMail.MAPPING_PARAM_TEMPLATE_PATH_FILE, new File("target/classes/email_templates/en/"));
 
     // This parameter tells the framework which e-mail templates to use. Here: contact_accept_headers.ftl, contact_accept_subject.ftl
     // contact_accept_body.ftl.html (if found) and contact_accept_body.ftl.txt (if found).
     mappedParameters.put(MessageHandlerEMail.MAPPING_PARAM_TEMPLATE_NAME, "contact_accept");
     // Send the HTML and the TEXT-version of the e-mail.
     mappedParameters.put(MessageHandlerEMail.MAPPING_PARAM_TEMPLATE_TYPE, TemplateTypeEnum.BOTH);
     // The recipient's first name? Used in the e-mail's TO: field. This variable may be accessed in the templates as "recipientFirstname".
     mappedParameters.put(MessageHandlerEMail.MAPPING_PARAM_RECIPIENT_FIRSTNAME, "Peter");
     // The recipient's last name? Used in the e-mail's TO: field. This variable may be accessed in the templates as "recipientLastname".
     mappedParameters.put(MessageHandlerEMail.MAPPING_PARAM_RECIPIENT_LASTNAME, "Recipientname");
     // Who to send the e-mail to? This variable may be accessed in the templates as "recipientEMail".
     // For testing purposes you should put the actual SMTP-configured sender e-mail address in here
    mappedParameters.put(MessageHandlerEMail.MAPPING_PARAM_RECIPIENT_EMAIL, "peter.sendername@mydomain.com");
 
     /*
      * Set the Freemarker-variables for the actual template file. They key names are what they may be referenced as within the Freemarker
      * templates.
      */
     mappedParameters.put("memberFirstname", "Peter");
     mappedParameters.put("memberLastname", "Smith");
     mappedParameters.put("memberTitle", "Developer");
     mappedParameters.put("memberCompany", "JAIDE GmbH");
     mappedParameters.put("memberProfileLink", "http://www.salambc.com/members?id=12345");
     mappedParameters.put("memberCompanyLink", "http://www.salambc.com/companies?id=4711");
     mappedParameters.put("recipientProfileLink", "http://www.salambc.com/companies?id=54321");
     mappedParameters.put("unsubscribeLink", "http://www.salambc.com/preferences/notifications/unsubscribe?id=54321");
 
     /*
      * Optional step: we want to overwrite the sender's firstname, lastname and e-mail that was configured in the smtp.json.
      * This is an absolute optional step - you can just go with the sender configured in the smtp.json if it doesn't change on each e-mail
      * sent out.
      */
     // mappedParameters.put(MessageHandlerEMail.MAPPING_PARAM_SENDER_FIRSTNAME, "Peter Different");
     // mappedParameters.put(MessageHandlerEMail.MAPPING_PARAM_SENDER_LASTNAME, "Sendername");
     // mappedParameters.put(MessageHandlerEMail.MAPPING_PARAM_SENDER_EMAIL, "peter-different.sendername@mydomain.com");
 
     /*
      * Send the e-mail
      */
     CourierService.getInstance().getMessageHandlerEMail("/smtp.json").handleMessage(mappedParameters);
 
     /*
      * Send a second e-mail, this time with an attachment.
      */
     List<EmailAttachment> attachments = new ArrayList<EmailAttachment>();
     EmailAttachment attachment = new EmailAttachment();
     attachment.setPath("target/test-classes/BabyOngBak.jpg");
     attachment.setDisposition(EmailAttachment.ATTACHMENT);
     attachment.setDescription("Picture of Baby Ong Bak");
     attachment.setName("BabyOngBak.jpg");
     attachments.add(attachment);
     mappedParameters.put(MessageHandlerEMail.MAPPING_PARAM_ATTACHMENTS, attachments);
 
     /*
      * And now send this e-mail as well.
      */
     CourierService.getInstance().getMessageHandlerEMail("/smtp.json").handleMessage(mappedParameters);
   }
 }
