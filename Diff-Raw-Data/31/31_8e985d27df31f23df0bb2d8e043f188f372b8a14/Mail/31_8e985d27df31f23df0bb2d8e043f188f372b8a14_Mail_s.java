 package de.deepamehta.plugins.mail;
 
 import static de.deepamehta.plugins.mail.MailPlugin.*;
 
 import java.io.UnsupportedEncodingException;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 
 import de.deepamehta.core.Association;
 import de.deepamehta.core.RelatedTopic;
 import de.deepamehta.core.Topic;
 import de.deepamehta.core.model.CompositeValue;
 import de.deepamehta.core.model.SimpleValue;
 import de.deepamehta.core.model.TopicModel;
 import de.deepamehta.core.service.ClientState;
 import de.deepamehta.core.service.DeepaMehtaService;
 import de.deepamehta.core.storage.spi.DeepaMehtaTransaction;
 
 /**
  * Model class that wraps the mail composite access.
  */
 public class Mail {
 
     private final DeepaMehtaService dms;
 
     private final Topic topic;
 
     private final ClientState clientState;
 
     public Mail(long topicId, DeepaMehtaService dms, ClientState clientState) {
         this.dms = dms;
         this.clientState = clientState;
         this.topic = dms.getTopic(topicId, true, clientState);
     }
 
     public String getBody() throws Exception {
         String body = topic.getCompositeValue().getTopic(BODY).getSimpleValue().toString();
         if (body.isEmpty()) {
             throw new IllegalArgumentException("Body of mail is empty");
         }
 
         if (topic.getCompositeValue().has(SIGNATURE) == false) {
             throw new IllegalArgumentException("Signature of mail not found");
         } else {
             List<TopicModel> signature = topic.getCompositeValue().getTopics(SIGNATURE);
             CompositeValue value = signature.get(0).getCompositeValue();
             if (value.has(BODY) == false) {
                 throw new IllegalArgumentException("Signature of mail is empty");
             } else {
                 String sigBody = value.getTopic(BODY).getSimpleValue().toString();
                 if (sigBody.isEmpty()) {
                     throw new IllegalArgumentException("Signature of mail is empty");
                 }
                 return body + sigBody;
             }
         }
     }
 
     public RecipientsByType getRecipients() throws InvalidRecipients {
         Set<String> invalid = new HashSet<String>();
         RecipientsByType results = new RecipientsByType();
 
         for (RelatedTopic recipient : topic.getRelatedTopics(RECIPIENT,//
                 WHOLE, PART, null, false, false, 0, null)) {
             String personal = recipient.getSimpleValue().toString();
 
             for (Association association : dms.getAssociations(topic.getId(), recipient.getId())) {
                 if (association.getTypeUri().equals(RECIPIENT) == false) {
                     continue; // sender or something else found
                 }
 
                 // get and validate recipient association
                 CompositeValue value = dms.getAssociation(association.getId(),//
                         true, null).getCompositeValue(); // re-fetch with value
                 if (value.has(RECIPIENT_TYPE) == false) {
                    invalid.add("Recipient type of \"" + personal + "\" is not defined.");
                     continue;
                 }
                 if (value.has(EMAIL_ADDRESS) == false) {
                     invalid.add("Recipient \"" + personal + "\" has no email address");
                     continue;
                 }
 
                 TopicModel type = value.getTopic(RECIPIENT_TYPE);
                 String email = value.getTopic(EMAIL_ADDRESS).getSimpleValue().toString();
                 try {
                     results.add(type.getUri(), email, personal);
                 } catch (Exception e) {
                     invalid.add("Email address \"" + email + "\" of recipient \"" + //
                            personal + "\" is invalid.");
                 }
             }
         }
         if (invalid.isEmpty() == false) {
             throw new InvalidRecipients(invalid);
         }
         return results;
     }
 
     public InternetAddress getSender() throws UnsupportedEncodingException, AddressException {
         RelatedTopic sender = topic.getRelatedTopic(SENDER,//
                 WHOLE, PART, null, false, true, null);
         if (sender == null) {
             throw new IllegalArgumentException("Contact required");
         }
         String personal = sender.getSimpleValue().toString();
         String address;
         try { // throws runtime access
             address = sender.getRelatingAssociation().getCompositeValue()//
                     .getTopic(EMAIL_ADDRESS).getSimpleValue().toString();
         } catch (Exception e) {
             throw new IllegalArgumentException("Contact has no email address");
         }
         InternetAddress internetAddress = new InternetAddress(address, personal);
         internetAddress.validate();
         return internetAddress;
     }
 
     public String getSubject() {
         return topic.getCompositeValue().getTopic(SUBJECT).getSimpleValue().toString();
     }
 
     public Topic getTopic() {
         return topic;
     }
 
     public Topic setMessageId(String messageId) {
         DeepaMehtaTransaction tx = dms.beginTx();
         topic.setChildTopicValue(DATE, new SimpleValue(new Date().toString()));
         topic.setChildTopicValue(MESSAGE_ID, new SimpleValue(messageId));
         tx.success();
         tx.finish();
         return topic;
     }
 
     public Set<Long> getAttachmentIds() {
         Set<Long> attachments = new HashSet<Long>();
         for (RelatedTopic attachment : topic.getRelatedTopics(AGGREGATION,//
                 WHOLE, PART, FILE, false, false, 0, clientState)) {
             attachments.add(attachment.getId());
         }
         return attachments;
     }
 }
