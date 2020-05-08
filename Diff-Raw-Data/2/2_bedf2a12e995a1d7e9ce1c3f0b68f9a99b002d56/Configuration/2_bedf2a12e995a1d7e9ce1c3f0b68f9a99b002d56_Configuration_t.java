 /**
  * 
  */
 package de.saumya.gwt.session.client.models;
 
 import java.sql.Timestamp;
 
 import com.google.gwt.xml.client.Element;
 
 import de.saumya.gwt.persistence.client.Repository;
 import de.saumya.gwt.persistence.client.Resource;
 import de.saumya.gwt.persistence.client.ResourceCollection;
 
 public class Configuration extends Resource<Configuration> {
 
     private final UserFactory   userFactory;
     private final LocaleFactory localeFactory;
 
     Configuration(final Repository repository,
             final ConfigurationFactory factory, final UserFactory userFactory,
             final LocaleFactory localeFactory) {
         super(repository, factory);
         this.userFactory = userFactory;
         this.localeFactory = localeFactory;
     }
 
     public int                        sessionIdleTimeout;
     public int                        keepAuditLogs;
     public ResourceCollection<Locale> locales;
     public String                     notificationSenderEmail;
     public String                     notificationRecipientEmails;
     public Timestamp                  updatedAt;
     public User                       updatedBy;
 
     @Override
     public String key() {
         return null;
     }
 
     @Override
     protected void appendXml(final StringBuilder buf) {
         appendXml(buf, "session_idle_timeout", this.sessionIdleTimeout);
         appendXml(buf, "keep_audit_logs", this.keepAuditLogs);
         appendXml(buf, "locales", this.locales);
         appendXml(buf,
                  "notification_sender_email",
                   this.notificationSenderEmail);
         appendXml(buf,
                   "notification_recipient_emails",
                   this.notificationRecipientEmails);
         appendXml(buf, "updated_at", this.updatedAt);
         appendXml(buf, "updated_by", this.updatedBy);
     }
 
     @Override
     protected void fromXml(final Element root) {
         this.sessionIdleTimeout = getInt(root, "session_idle_timeout");
         this.keepAuditLogs = getInt(root, "keep_audit_logs");
         this.locales = this.localeFactory.getChildResourceCollection(root,
                                                                      "locales");
         this.notificationRecipientEmails = getString(root,
                                                      "notification_recipient_emails");
         this.notificationSenderEmail = getString(root,
                                                  "notification_sender_email");
         this.updatedAt = getTimestamp(root, "updated_at");
         this.updatedBy = this.userFactory.getChildResource(root, "updated_by");
     }
 
     @Override
     public void toString(final StringBuilder buf) {
         toString(buf, "session_idle_timeout", this.sessionIdleTimeout);
         toString(buf, "keep_audit_logs", this.keepAuditLogs);
         toString(buf, "locales", this.locales);
         toString(buf,
                  "notification_sender_emailn",
                  this.notificationSenderEmail);
         toString(buf,
                  "notification_recipient_emails",
                  this.notificationRecipientEmails);
         toString(buf, "updated_at", this.updatedAt);
         toString(buf, "updated_by", this.updatedBy);
     }
 
     @Override
     public String display() {
         return "Configuration";
     }
 }
