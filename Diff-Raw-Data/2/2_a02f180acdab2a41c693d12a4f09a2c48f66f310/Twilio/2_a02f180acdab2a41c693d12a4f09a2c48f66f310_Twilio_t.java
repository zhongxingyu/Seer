 package edu.rit.asksg.domain;
 
 import com.google.common.base.Optional;
 import com.sun.org.apache.xpath.internal.operations.Bool;
 import com.twilio.sdk.TwilioRestClient;
 import com.twilio.sdk.TwilioRestException;
 import com.twilio.sdk.resource.factory.SmsFactory;
 import com.twilio.sdk.resource.instance.Sms;
 import edu.rit.asksg.dataio.ContentProvider;
 import edu.rit.asksg.domain.config.ProviderConfig;
 import edu.rit.asksg.domain.config.TwilioConfig;
 import edu.rit.asksg.service.ConversationService;
 import edu.rit.asksg.service.IdentityService;
 import flexjson.JSON;
 import org.joda.time.LocalDateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
 import org.springframework.roo.addon.json.RooJson;
 import org.springframework.roo.addon.tostring.RooToString;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 @RooJavaBean
 @RooToString
 @RooJpaEntity
 @RooJson
 public class Twilio extends Service implements ContentProvider {
 
     private transient static final Logger logger = LoggerFactory.getLogger(Twilio.class);
 
     @JSON(include = false)
     @Override
     public List<Conversation> getNewContent() {
         logger.debug("Twilio does not support fetching new content.");
         return new ArrayList<Conversation>();
     }
 
     @JSON(include = false)
     @Override
     public List<Conversation> getContentSince(LocalDateTime datetime) {
         return new ArrayList<Conversation>();
     }
 
     @Override
     public boolean postContent(Message message) {
         final TwilioConfig config = (TwilioConfig) this.getConfig();
         TwilioRestClient twc = new TwilioRestClient(config.getUsername(), config.getAuthenticationToken());
         Map<String, String> vars = new HashMap<String, String>();
         vars.put("Body", message.getContent());
         vars.put("From", config.getPhoneNumber());
         vars.put("To", message.getConversation().getRecipient());
 
         SmsFactory smsFactory = twc.getAccount().getSmsFactory();
         try {
             Sms sms = smsFactory.create(vars);
             //TODO: Twilio can use a callback to POST information to if sending fails
         } catch (TwilioRestException e) {
             logger.error(e.getLocalizedMessage(), e);
             return false;
         }
         return true;
     }
 
     @Override
     public boolean authenticate() {
         return false;
     }
 
     @Override
     public boolean isAuthenticated() {
         return false;
     }
 
     public void handleMessage(String smsSid, String accountSid, String from, String to, String body) {
 
         Message message = new Message();
         message.setContent(body);
 		Identity identity = getIdentityService().findOrCreate(from);
 		message.setIdentity(identity);
        LocalDateTime now = LocalDateTime.now().plusHours(4);
         message.setCreated(now);
         message.setModified(now);
 
 		Conversation conv = getConversationService().findConversationByRecipientSince(from, now.minusWeeks(1));
 
         if (conv == null) {
             logger.debug("Twilio: Creating new conversation for message received from " + from);
             conv = new Conversation(message);
             conv.setExternalId(smsSid);
             conv.setCreated(now);
             conv.setSubject(body);
         }
         conv.getMessages().add(message);
 
         conv.setModified(now);
         conv.setService(this);
 
         message.setConversation(conv);
 
         getConversationService().saveConversation(conv);
     }
 }
