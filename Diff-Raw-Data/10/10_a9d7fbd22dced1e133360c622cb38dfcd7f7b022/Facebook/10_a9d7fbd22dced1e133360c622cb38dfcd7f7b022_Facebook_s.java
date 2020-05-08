 package edu.rit.asksg.domain;
 
 import edu.rit.asksg.dataio.ContentProvider;
 import edu.rit.asksg.dataio.SubscriptionProvider;
 import edu.rit.asksg.domain.config.SpringSocialConfig;
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
 import org.springframework.social.connect.Connection;
 import org.springframework.social.facebook.api.Comment;
 import org.springframework.social.facebook.api.Post;
 import org.springframework.social.facebook.api.impl.FacebookTemplate;
 import org.springframework.social.facebook.connect.FacebookConnectionFactory;
 import org.springframework.social.oauth2.AccessGrant;
 import org.springframework.social.oauth2.GrantType;
 import org.springframework.social.oauth2.OAuth2Operations;
 import org.springframework.social.oauth2.OAuth2Parameters;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 @RooJavaBean
 @RooToString
 @RooJpaEntity
 @RooJson
 public class Facebook extends Service implements ContentProvider, SubscriptionProvider {
 
     private static final transient Logger logger = LoggerFactory.getLogger(Facebook.class);
 
     @JSON(include = false)
     public List<Conversation> getNewContent() {
         return new ArrayList<Conversation>();
     }
 
     @JSON(include = false)
     public List<Conversation> getContentSince(LocalDateTime datetime) {
         return new ArrayList<Conversation>();
     }
 
     public boolean postContent(Message message) {
         final org.springframework.social.facebook.api.Facebook facebookApi = getFacebookApi();
         try {
             if (message.getConversation() == null) {
                 facebookApi.feedOperations().post(facebookApi.userOperations().getUserProfile().getId(), message.getContent());
             } else {
                 // If the message we got passed is attached to a conversation, comment on the thread instead of posting a new message
                 facebookApi.commentOperations().addComment(message.getConversation().getExternalId(), message.getContent());
             }
         } catch (Exception e) {
             logger.error("Exception in Facebook trying to post content.", e);
             return false; //TODO: for the love of god handle this exception
         }
         return true;
     }
 
     public boolean authenticate() {
         return super.authenticate();
     }
 
     public boolean isAuthenticated() {
         return super.isAuthenticated();
     }
 
     @JSON(include = false)
     private org.springframework.social.facebook.api.Facebook getFacebookApi() {
         final SpringSocialConfig config = (SpringSocialConfig) this.getConfig();
         return new FacebookTemplate(config.getAccessTokenSecret());
     }
 
     @JSON(include = false)
     public Collection<Conversation> getContentFor(SocialSubscription socialSubscription) {
         final org.springframework.social.facebook.api.Facebook facebookApi = getFacebookApi();
         return parseFacebookFeed(facebookApi.feedOperations().getFeed(socialSubscription.getHandle()), facebookApi);
     }
 
 
     protected List<Conversation> parseFacebookFeed(List<Post> posts, org.springframework.social.facebook.api.Facebook facebookApi) {
         final List<Conversation> conversations = new ArrayList<Conversation>();
         logger.debug("Facebook: parsing feed. Posts has size " + posts.size() + ". Thread name: " + Thread.currentThread().getName());
         for (Post post : posts) {
             logger.debug("Facebook: parsing feed: post message is: " + post.getMessage() + ", author is: " + post.getFrom().getName());
             if (post.getMessage() == null) {
                 continue; // skip posts with no text content to save
             }
             Message message = new Message();
             Conversation conversation = new Conversation(message);
             conversation.setService(this);
             message.setConversation(conversation);
            String postMessage = post.getMessageas();
             message.setContent(postMessage);
             // Subject snippet will be the first 40 characters plus a "...", unless it's shorter than that
             conversation.setSubject(postMessage.length() > 40 ? postMessage.substring(0, 40) + "..." : postMessage);
 			Identity identity = getIdentityService().findOrCreate(facebookApi.userOperations().getUserProfile(post.getFrom().getId()).getName());
             message.setIdentity(identity);
             message.setCreated(new LocalDateTime(post.getCreatedTime()));
             if (post.getComments() != null) {
                 for (Comment comment : post.getComments()) {
                     Message commentMsg = new Message();
                     commentMsg.setConversation(conversation);
                     commentMsg.setContent(comment.getMessage());
 					Identity commentIdentity = getIdentityService().findOrCreate(facebookApi.userOperations().getUserProfile(comment.getFrom().getId()).getName());
                     commentMsg.setIdentity(commentIdentity);
                     commentMsg.setCreated(new LocalDateTime(comment.getCreatedTime()));
                     conversation.getMessages().add(commentMsg);
                 }
             }
             conversation.setExternalId(post.getId());
 
             if (conversation.getMessages() != null && !conversation.getMessages().isEmpty())
                 conversation.setCreated(conversation.getMessages().get(0).getCreated());
 
             conversations.add(conversation);
         }
         return conversations;
 
     }
 
 }
