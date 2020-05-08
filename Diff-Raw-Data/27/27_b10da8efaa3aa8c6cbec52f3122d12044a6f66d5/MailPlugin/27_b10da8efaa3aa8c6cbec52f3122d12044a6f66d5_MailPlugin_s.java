 package de.deepamehta.plugins.mail;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.mail.internet.InternetAddress;
 import javax.ws.rs.GET;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 
 import org.apache.commons.mail.EmailAttachment;
 import org.apache.commons.mail.EmailException;
 import org.apache.commons.mail.HtmlEmail;
 import org.jsoup.nodes.Document;
 
 import de.deepamehta.core.Association;
 import de.deepamehta.core.CompositeValue;
 import de.deepamehta.core.RelatedTopic;
 import de.deepamehta.core.ResultSet;
 import de.deepamehta.core.Topic;
 import de.deepamehta.core.model.AssociationModel;
 import de.deepamehta.core.model.CompositeValueModel;
 import de.deepamehta.core.model.SimpleValue;
 import de.deepamehta.core.model.TopicModel;
 import de.deepamehta.core.model.TopicRoleModel;
 import de.deepamehta.core.osgi.PluginActivator;
 import de.deepamehta.core.service.ClientState;
 import de.deepamehta.core.service.Directives;
 import de.deepamehta.core.service.PluginService;
 import de.deepamehta.core.service.accesscontrol.ACLEntry;
 import de.deepamehta.core.service.accesscontrol.AccessControlList;
 import de.deepamehta.core.service.accesscontrol.Operation;
 import de.deepamehta.core.service.accesscontrol.UserRole;
 import de.deepamehta.core.service.annotation.ConsumesService;
 import de.deepamehta.core.service.event.PostCreateTopicListener;
 import de.deepamehta.core.storage.spi.DeepaMehtaTransaction;
 import de.deepamehta.plugins.accesscontrol.service.AccessControlService;
 import de.deepamehta.plugins.files.ResourceInfo;
 import de.deepamehta.plugins.files.service.FilesService;
 import de.deepamehta.plugins.mail.service.MailService;
 
 @Path("/mail")
 @Produces(MediaType.APPLICATION_JSON)
 public class MailPlugin extends PluginActivator implements MailService, PostCreateTopicListener {
 
     private static Logger log = Logger.getLogger(MailPlugin.class.getName());
 
     // URI constants
 
     public static final String AGGREGATION = "dm4.core.aggregation";
 
     public static final String COMPOSITION = "dm4.core.composition";
 
     public static final String PART = "dm4.core.part";
 
     public static final String PART_TYPE = "dm4.core.part_type";
 
     public static final String TOPIC_TYPE = "dm4.core.topic_type";
 
     public static final String WHOLE = "dm4.core.whole";
 
     public static final String WHOLE_TYPE = "dm4.core.whole_type";
 
     public static final String FILE = "dm4.files.file";
 
     public static final String ATTACHMENTS = "attachments";
 
     public static final String BODY = "dm4.mail.body";
 
     public static final String EMAIL_ADDRESS = "dm4.contacts.email_address";
 
     public static final String DATE = "dm4.mail.date";
 
     public static final String FROM = "dm4.mail.from";
 
     public static final String MAIL = "dm4.mail";
 
     public static final String MESSAGE_ID = "dm4.mail.id";
 
     public static final String RECIPIENT = "dm4.mail.recipient";
 
     public static final String RECIPIENT_TYPE = "dm4.mail.recipient.type";
 
     public static final String SENDER = "dm4.mail.sender";
 
     public static final String SIGNATURE = "dm4.mail.signature";
 
     public static final String SUBJECT = "dm4.mail.subject";
 
     public static final String USER_ACCOUNT = "dm4.accesscontrol.user_account";
 
     // service references
 
     private AccessControlService acService;
 
     private FilesService fileService = null;
 
     // package internal helpers
 
     MailConfigurationCache config = null;
 
     ImageCidEmbedment cidEmbedment = null;
 
     Autocomplete autocomplete = null;
 
     boolean isInitialized;
 
     /**
      * @see #associateRecipient(long, Topic, String)
      */
     @POST
     @Path("{mail}/recipient/{address}")
     public Association mailRecipient(//
             @PathParam("mail") long mailId,//
             @PathParam("address") long addressId,//
             @QueryParam("type") String type,//
             @HeaderParam("Cookie") ClientState cookie) {
         try {
             return associateRecipient(mailId, addressId,//
                     config.checkRecipientType(type), cookie);
         } catch (Exception e) {
             throw new WebApplicationException(e);
         }
     }
 
     @Override
     public Association associateRecipient(long mailId, long addressId, RecipientType type, ClientState clientState) {
         log.info("associate " + mailId + " with recipient address " + addressId);
 
         // create value of the recipient association
         CompositeValueModel value = new CompositeValueModel()//
                 .putRef(RECIPIENT_TYPE, type.getUri())//
                 .putRef(EMAIL_ADDRESS, addressId);
 
         // get and update or create a new recipient association
         RelatedTopic recipient = getContactOfEmail(addressId, clientState);
         Association association = getRecipientAssociation(mailId, //
                 addressId, recipient.getId(), clientState);
 
         if (association == null) { // create a recipient association
             return associateRecipient(mailId, recipient, value, clientState);
         } else { // update address and type references
             association.setCompositeValue(value, clientState, new Directives());
             return association;
         }
     }
 
     /**
      * @see #associateSender(long, Topic)
      */
     @POST
     @Path("{mail}/sender/{address}")
     public Association mailSender(//
             @PathParam("mail") long mailId,//
             @PathParam("address") long addressId,//
             @HeaderParam("Cookie") ClientState cookie) {
         try {
             return associateSender(mailId, addressId, cookie);
         } catch (Exception e) {
             throw new WebApplicationException(e);
         }
     }
 
     @Override
     public Association associateSender(long mailId, long addressId, ClientState clientState) {
         log.info("associate " + mailId + " with sender " + addressId);
 
         // create value of sender association
         CompositeValueModel value = new CompositeValueModel().putRef(EMAIL_ADDRESS, addressId);
 
         // find existing sender association
         RelatedTopic sender = getContactOfEmail(addressId, clientState);
         RelatedTopic oldSender = getSender(mailId, false, clientState);
 
         if (oldSender == null) { // create the first sender association
             return associateSender(mailId, sender, value, clientState);
         } else { // update or delete the old sender
             Association association = getSenderAssociation(mailId, oldSender.getId(), clientState);
             DeepaMehtaTransaction tx = dms.beginTx();
             try {
                 if (sender.getId() != oldSender.getId()) { // delete the old one
                     dms.deleteAssociation(association.getId(), clientState);
                     association = associateSender(mailId, sender, value, clientState);
                 } else { // update composite
                     association.setCompositeValue(value, clientState, new Directives());
                 }
                 tx.success();
             } catch (Exception e) {
                 throw new WebApplicationException(e);
             } finally {
                 tx.finish();
             }
             return association;
         }
     }
 
     /**
      * Returns the parent of each search type substring match.
      * 
      * @param term
      *            String to search.
      * @param cookie
      *            Actual cookie.
      * @return Parent model of each result topic.
      */
     @GET
     @Path("/autocomplete/{term}")
     public List<TopicModel> search(@PathParam("term") String term, //
             @HeaderParam("Cookie") ClientState cookie) {
         try {
             String query = "*" + term + "*";
             log.info("autocomplete " + query);
             return autocomplete.search(query, cookie);
         } catch (Exception e) {
             throw new WebApplicationException(e);
         }
     }
 
     /**
      * Creates a copy of the mail.
      * 
      * @param mailId
      *            ID of the mail topic to clone.
      * @param includeRecipients
      *            Copy recipients of the origin?
      * @param cookie
      *            Actual cookie.
      * @return Cloned mail topic with associated sender and recipients.
      */
     @POST
     @Path("/{mail}/copy")
     public Topic copyMail(//
             @PathParam("mail") long mailId,//
             @QueryParam("recipients") boolean includeRecipients,//
             @HeaderParam("Cookie") ClientState cookie) {
         log.info("copy mail " + mailId);
         DeepaMehtaTransaction tx = dms.beginTx();
         try {
             Topic mail = dms.getTopic(mailId, true, cookie);
             TopicModel model = new TopicModel(MAIL, mail.getCompositeValue().getModel() // copy
                     .put(DATE, "").put(MESSAGE_ID, "")); // nullify date and ID
             Topic clone = dms.createTopic(model, cookie);
 
            // copy signature
            Topic signature = getSignature(mail, cookie);
            associateSignature(clone.getId(), signature.getId(), cookie);

             // copy sender association
             RelatedTopic sender = getSender(mail, true, cookie);
             associateSender(clone.getId(), sender, sender.getRelatingAssociation()//
                     .getCompositeValue().getModel(), cookie);
 
             // copy recipient associations
             if (includeRecipients) {
                 for (RelatedTopic recipient : mail.getRelatedTopics(RECIPIENT,//
                         WHOLE, PART, null, false, true, 0, null)) {
                     for (Association association : dms.getAssociations(mail.getId(),//
                             recipient.getId())) {
                         if (association.getTypeUri().equals(RECIPIENT) == false) {
                             continue; // sender or something else found
                         }
                         CompositeValue value = dms.getAssociation(association.getId(), //
                                 true, cookie).getCompositeValue();
                         associateRecipient(clone.getId(), recipient, value.getModel(), cookie);
                     }
                 }
             }
             tx.success();
             return clone;
         } catch (Exception e) {
             throw new WebApplicationException(e);
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Creates a new mail to all email addresses of the contact topic.
      * 
      * @param recipientId
      *            ID of a recipient contact topic.
      * @param cookie
      *            Actual cookie.
      * @return Mail topic with associated recipient.
      */
     @POST
     @Path("/write/{recipient}")
     public Topic writeTo(@PathParam("recipient") long recipientId,//
             @HeaderParam("Cookie") ClientState cookie) {
         log.info("write a mail to recipient " + recipientId);
         DeepaMehtaTransaction tx = dms.beginTx();
         try {
             Topic mail = dms.createTopic(new TopicModel(MAIL), cookie);
             Topic recipient = dms.getTopic(recipientId, true, cookie);
             if (recipient.getCompositeValue().has(EMAIL_ADDRESS)) {
                 for (Topic address : recipient.getCompositeValue().getTopics(EMAIL_ADDRESS)) {
                     associateRecipient(mail.getId(), address.getId(),//
                             config.getDefaultRecipientType(), cookie);
                 }
             }
             tx.success();
             return mail;
         } catch (Exception e) {
             throw new WebApplicationException(e);
         } finally {
             tx.finish();
         }
     }
 
     /**
      * @return Default recipient type.
      */
     @GET
     @Path("/recipient/default")
     public String getDefaultRecipientType() {
         try {
             return config.getDefaultRecipientType().getUri();
         } catch (Exception e) {
             throw new WebApplicationException(e);
         }
     }
 
     /**
      * @return Recipient types.
      */
     @GET
     @Path("/recipient/types")
     public ResultSet<RelatedTopic> getRecipientTypes() {
         try {
             return config.getRecipientTypes();
         } catch (Exception e) {
             throw new WebApplicationException(e);
         }
     }
 
     /**
      * @see #getSearchParentTypes
      */
     @GET
     @Path("/search/parents")
     public ResultSet<Topic> listSearchParentTypes() {
         try {
             Collection<Topic> parents = getSearchParentTypes();
             return new ResultSet<Topic>(parents.size(), new HashSet<Topic>(parents));
         } catch (Exception e) {
             throw new WebApplicationException(e);
         }
     }
 
     @Override
     public Collection<Topic> getSearchParentTypes() {
         return config.getSearchParentTypes();
     }
 
     /**
      * Load the configuration.
      * 
      * Useful after type and configuration changes with the web-client.
      */
     @GET
     @Path("/config/load")
     public Topic loadConfiguration() {
         try {
             log.info("load mail configuration");
             config = new MailConfigurationCache(dms);
             autocomplete = new Autocomplete(dms, config);
             return config.getTopic();
         } catch (Exception e) {
             throw new WebApplicationException(e);
         }
     }
 
     /**
      * Sets the default sender and signature of a mail topic after creation.
      */
     @Override
     public void postCreateTopic(Topic topic, ClientState clientState, Directives directives) {
         if (topic.getTypeUri().equals(MAIL)) {
             if (topic.getCompositeValue().has(FROM) == false) { // new mail
                 associateDefaultSender(topic, clientState);
             } else { // copied mail
                 Topic from = topic.getCompositeValue().getTopic(FROM);
                 if (from.getSimpleValue().booleanValue() == false) { // sender?
                     associateDefaultSender(topic, clientState);
                 }
             }
         }
     }
 
     /**
      * @see #send(Mail)
      */
     @POST
     @Path("/{mail}/send")
     public StatusReport send(//
             @PathParam("mail") long mailId,//
             @HeaderParam("Cookie") ClientState cookie) {
         log.info("send mail " + mailId);
         try {
             return send(new Mail(mailId, dms, cookie));
         } catch (Exception e) {
             throw new WebApplicationException(e);
         }
     }
 
     @Override
     public StatusReport send(Mail mail) {
         StatusReport statusReport = new StatusReport(mail.getTopic());
 
         HtmlEmail email = new HtmlEmail();
         email.setDebug(true); // => System.out.println(SMTP communication);
         email.setHostName(config.getSmtpHost());
 
         try {
             InternetAddress sender = mail.getSender();
             email.setFrom(sender.getAddress(), sender.getPersonal());
         } catch (Exception e) {
             reportException(statusReport, Level.INFO, MailError.SENDER, e);
         }
 
         try {
             String subject = mail.getSubject();
             if (subject.isEmpty()) { // caught immediately
                 throw new IllegalArgumentException("Subject of mail is empty");
             }
             email.setSubject(subject);
         } catch (Exception e) {
             reportException(statusReport, Level.INFO, MailError.CONTENT, e);
         }
 
         try {
             Document body = cidEmbedment.embedImages(email, mail.getBody());
             String text = body.text();
             if (text.isEmpty()) { // caught immediately
                 throw new IllegalArgumentException("Text body of mail is empty");
             }
             email.setTextMsg(text);
             email.setHtmlMsg(body.html());
         } catch (Exception e) {
             reportException(statusReport, Level.INFO, MailError.CONTENT, e);
         }
 
         for (Long fileId : mail.getAttachmentIds()) {
             try {
                 String path = fileService.getFile(fileId).getAbsolutePath();
                 EmailAttachment attachment = new EmailAttachment();
                 attachment.setPath(path);
                 log.fine("attach " + path);
                 email.attach(attachment);
             } catch (Exception e) {
                 reportException(statusReport, Level.INFO, MailError.ATTACHMENTS, e);
             }
         }
 
         RecipientsByType recipients = new RecipientsByType();
         try {
             recipients = mail.getRecipients();
             try {
                 mapRecipients(email, recipients);
             } catch (Exception e) {
                 reportException(statusReport, Level.SEVERE, MailError.RECIPIENT_TYPE, e);
             }
         } catch (InvalidRecipients e) {
             for (String recipient : e.getRecipients()) {
                 log.log(Level.INFO, MailError.RECIPIENTS.getMessage() + ": " + recipient);
                 statusReport.addError(MailError.RECIPIENTS, recipient);
             }
         }
 
         if (statusReport.hasErrors()) {
             statusReport.setMessage("Mail can NOT be sent");
         } else { // send, update message ID and return status with attached mail
             try {
                 String messageId = email.send();
                 statusReport.setMessage("Mail was SUCCESSFULLY sent to " + //
                         recipients.getCount() + " mail addresses");
                 mail.setMessageId(messageId);
             } catch (EmailException e) {
                 statusReport.setMessage("Sending mail FAILED");
                 reportException(statusReport, Level.SEVERE, MailError.SEND, e);
             } catch (Exception e) { // error after send
                 reportException(statusReport, Level.SEVERE, MailError.UPDATE, e);
             }
         }
         return statusReport;
     }
 
     /**
      * Initialize.
      */
     @Override
     public void init() {
         isInitialized = true;
         configureIfReady();
     }
 
     /**
      * Reference file service and create the attachment directory if not exists.
      */
     @Override
     @ConsumesService({ "de.deepamehta.plugins.accesscontrol.service.AccessControlService",
             "de.deepamehta.plugins.files.service.FilesService" })
     public void serviceArrived(PluginService service) {
         if (service instanceof AccessControlService) {
             acService = (AccessControlService) service;
         } else if (service instanceof FilesService) {
             fileService = (FilesService) service;
             cidEmbedment = new ImageCidEmbedment(fileService);
         }
         configureIfReady();
     }
 
     private void configureIfReady() {
         if (isInitialized && acService != null && fileService != null) {
             createAttachmentDirectory();
             checkACLsOfMigration();
             loadConfiguration();
         }
     }
 
     @Override
     public void serviceGone(PluginService service) {
         if (service == acService) {
             acService = null;
         } else if (service == fileService) {
             fileService = null;
             cidEmbedment = null;
         }
     }
 
     private void checkACLsOfMigration() {
         Topic config = dms.getTopic("uri", new SimpleValue("dm4.mail.config"), false, null);
         if (acService.getCreator(config.getId()) == null) {
             log.info("initial ACL update of configuration");
             Topic admin = acService.getUsername("admin");
             String adminName = admin.getSimpleValue().toString();
             acService.setCreator(config.getId(), adminName);
             acService.setOwner(config.getId(), adminName);
             acService.createACL(config.getId(), new AccessControlList( //
                     new ACLEntry(Operation.WRITE, UserRole.OWNER)));
         }
     }
 
     private void createAttachmentDirectory() {
         // TODO move the initialization to migration "0"
         try {
             ResourceInfo resourceInfo = fileService.getResourceInfo(ATTACHMENTS);
             String kind = resourceInfo.toJSON().getString("kind");
             if (kind.equals("directory") == false) {
                 String repoPath = System.getProperty("dm4.filerepo.path");
                 String message = "attachment storage directory " + repoPath + File.separator + ATTACHMENTS
                         + " can not be used";
                 throw new IllegalStateException(message);
             }
         } catch (WebApplicationException e) { // !exists
             // catch fileService info request error => create directory
             if (e.getResponse().getStatus() != 404) {
                 throw e;
             } else {
                 log.info("create attachment directory");
                 fileService.createFolder(ATTACHMENTS, "/");
             }
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     private void associateDefaultSender(Topic mail, ClientState clientState) {
         Topic creator = null;
         RelatedTopic sender = null;
 
         Topic creatorName = acService.getUsername(acService.getCreator(mail.getId()));
         if (creatorName != null) {
             creator = creatorName.getRelatedTopic(null,//
                     PART, WHOLE, USER_ACCOUNT, false, false, null);
         }
 
         // get user account specific sender
         if (creator != null) {
             sender = getSender(creator, true, clientState);
         }
 
         // get the configured default sender instead
         if (sender == null) {
             sender = config.getDefaultSender();
         }
 
         if (sender != null) {
             DeepaMehtaTransaction tx = dms.beginTx();
             try {
                 CompositeValueModel value = sender.getRelatingAssociation()//
                         .getCompositeValue().getModel();
                 associateSender(mail.getId(), sender, value, clientState);
                 long addressId = value.getTopic(EMAIL_ADDRESS).getId();
                 RelatedTopic signature = getContactSignature(sender, addressId, clientState);
                 if (signature != null) {
                    associateSignature(mail.getId(), signature.getId(), clientState);
                 }
                 tx.success();
             } finally {
                 tx.finish();
             }
         }
     }
 
     private Association associateRecipient(long topicId, Topic recipient, CompositeValueModel value,
             ClientState clientState) {
         return dms.createAssociation(new AssociationModel(RECIPIENT,//
                 new TopicRoleModel(recipient.getId(), PART),//
                 new TopicRoleModel(topicId, WHOLE), value), clientState);
     }
 
     private Association associateSender(long topicId, Topic sender, CompositeValueModel value, ClientState clientState) {
         return dms.createAssociation(new AssociationModel(SENDER,//
                 new TopicRoleModel(sender.getId(), PART),//
                 new TopicRoleModel(topicId, WHOLE), value), clientState);
     }
 
    private Association associateSignature(long mailId, long signatureId, ClientState clientState) {
        return dms.createAssociation(new AssociationModel(AGGREGATION,//
                new TopicRoleModel(signatureId, PART),//
                new TopicRoleModel(mailId, WHOLE)), clientState);
    }

     private RelatedTopic getContactOfEmail(long addressId, ClientState clientState) {
         Topic address = dms.getTopic(addressId, false, clientState);
         RelatedTopic contact = address.getRelatedTopic(COMPOSITION,//
                 PART, WHOLE, null, false, false, null);
         return contact;
     }
 
     private RelatedTopic getContactSignature(Topic topic, long addressId, ClientState clientState) {
         for (RelatedTopic signature : topic.getRelatedTopics(SENDER,//
                 PART, WHOLE, SIGNATURE, true, true, 0, clientState)) {
             CompositeValue value = signature.getRelatingAssociation().getCompositeValue();
             if (addressId == value.getTopic(EMAIL_ADDRESS).getId()) {
                 return signature;
             }
         }
         return null;
     }
 
     private Association getRecipientAssociation(long topicId, long addressId, long recipientId, ClientState clientState) {
         for (Association recipient : dms.getAssociations(topicId, recipientId)) {
             Association association = dms.getAssociation(recipient.getId(), true, clientState);
             Topic address = association.getCompositeValue().getTopic(EMAIL_ADDRESS);
             if (association.getTypeUri().equals(RECIPIENT) && address.getId() == addressId) {
                 return association;
             }
         }
         return null;
     }
 
     private RelatedTopic getSender(long topicId, boolean fetchRelatingComposite, ClientState clientState) {
         return getSender(dms.getTopic(topicId, false, clientState),//
                 fetchRelatingComposite, clientState);
     }
 
     private RelatedTopic getSender(Topic topic, boolean fetchRelatingComposite, ClientState clientState) {
         return topic.getRelatedTopic(SENDER, WHOLE, PART, null, false,//
                 fetchRelatingComposite, clientState);
     }
 
    private Topic getSignature(Topic mail, ClientState clientState) {
        return mail.getRelatedTopics(AGGREGATION, WHOLE, PART,//
                SIGNATURE, false, false, 1, clientState).iterator().next();
    }

     private Association getSenderAssociation(long topicId, long senderId, ClientState clientState) {
         return dms.getAssociation(SENDER, topicId, senderId, WHOLE, PART, true, clientState);
     }
 
     private void mapRecipients(HtmlEmail email, Map<RecipientType, List<InternetAddress>> recipients)
             throws EmailException {
         for (RecipientType type : recipients.keySet()) {
             switch (type) {
             case BCC:
                 email.setBcc(recipients.get(type));
                 break;
             case CC:
                 email.setCc(recipients.get(type));
                 break;
             case TO:
                 email.setTo(recipients.get(type));
                 break;
             default:
                 throw new IllegalArgumentException(type.toString());
             }
         }
     }
 
     private void reportException(StatusReport report, Level level, MailError error, Exception e) {
         String message = e.getMessage();
         Throwable cause = e.getCause();
         if (cause != null) {
             message += ": " + cause.getMessage();
         }
         String logMessage = error.getMessage() + ": " + message;
         if (level == Level.WARNING || level == Level.SEVERE) {
             log.log(level, logMessage, e); // log the exception trace
         } else {
             log.log(level, logMessage); // log only the message
         }
         report.addError(error, message);
     }
 }
