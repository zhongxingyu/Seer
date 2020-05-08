 package com.globant.katari.registration.domain;
 
 import org.apache.commons.lang.Validate;
 
 /**
  * This class holds all the information to create a new email.
 * This one should be configured by spring to be complaiment with i18l.
 *
 * @author waabox (emiliano[dot]arango[at]globant[dot]com)
  *
  */
 public class EmailConfigurer {
 
  /** The "admin" emails. It's never null.*/
   private final String emailFrom;
 
  /** The email's subject. It's never null.*/
   private final String subject;
 
  /** The email's template (Freemarker). It's never null.*/
   private final String template;
 
  /** The email's plain message (non HTML) It's never null. */
   private final String plainMessage;
 
   /** Builds a new instance of the email configurer.
    *
    * @param theEmailFrom email from. Cannot be null.
    * @param theSubjet the email's subject. Cannot be null.
    * @param theTemplate the email's template. Cannot be null.
    * @param thePlainMessage the email's plain message (non HTML) Cannot be null.
    */
   public EmailConfigurer(final String theEmailFrom, final String theSubjet,
       final String theTemplate, final String thePlainMessage) {
     Validate.notNull(theEmailFrom, "The email from cannot be null");
     Validate.notNull(theSubjet, "The subject from cannot be null");
     Validate.notNull(theTemplate, "The template from cannot be null");
     Validate.notNull(thePlainMessage, "The plain message from cannot be null");
     emailFrom = theEmailFrom;
     subject = theSubjet;
     template = theTemplate;
     plainMessage = thePlainMessage;
   }
 
   /** Returns the email from.
    * @return the emailFrom. Never returns null.
    */
   public String getEmailFrom() {
     return emailFrom;
   }
 
   /** Returns the email's subject.
    * @return the subject. Never returns null.
    */
   public String getSubject() {
     return subject;
   }
 
   /** Returns the template.
    * @return the template. Never returns null.
    */
   public String getTemplate() {
     return template;
   }
 
   /** Returns the plain message.
    * @return the plainMessage. Never returns null.
    */
   public String getPlainMessage() {
     return plainMessage;
   }
 }
