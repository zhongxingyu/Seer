 package edu.rit.asksg.domain;
 
 import edu.rit.asksg.dataio.ContentProvider;
 import edu.rit.asksg.domain.config.ProviderConfig;
 import edu.rit.asksg.domain.config.TwilioConfig;
 import edu.rit.asksg.domain.config.EmailConfig;
 import edu.rit.asksg.domain.config.RedditConfig;
 import edu.rit.asksg.domain.config.SpringSocialConfig;
 import flexjson.JSON;
 import flexjson.JSONDeserializer;
 import org.joda.time.LocalDateTime;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
 import org.springframework.roo.addon.json.RooJson;
 import org.springframework.roo.addon.tostring.RooToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 import javax.persistence.CascadeType;
 import javax.persistence.FetchType;
 import javax.persistence.OneToOne;
 import java.util.List;
 
 @RooJavaBean
 @RooToString
 @RooJpaEntity
 @RooJson(deepSerialize = true)
 public class Service implements ContentProvider {
 
	private static final transient Logger logger = LoggerFactory.getLogger(Service.class);

 	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
 	private ProviderConfig config;
 
 	public String getName() {
 		return this.getClass().getSimpleName();
 	}
 
 	@JSON(include = false)
 	public List<edu.rit.asksg.domain.Conversation> getNewContent() {
 		return null;
 	}
 
 	@JSON(include = false)
 	public List<edu.rit.asksg.domain.Conversation> getContentSince(LocalDateTime datetime) {
 		return null;
 	}
 
 	public boolean postContent(Message message) {
 		return true;
 	}
 
 	public boolean authenticate() {
 		return false;
 	}
 
 	public boolean isAuthenticated() {
 		return false;
 	}
 
 	public static Service fromJsonToService(String json) {
 		if (json.contains("\"name\":\"Twilio\"")) {
			logger.error(json);
			Service s = new JSONDeserializer<Service>().use(null, Twilio.class).use("config", TwilioConfig.class).deserialize(json);
			logger.error(s.toString());
			return s;
 		} else if (json.contains("\"name\":\"Email\"")) {
 			return new JSONDeserializer<Service>().use(null, Email.class).use("config", EmailConfig.class).deserialize(json);
 		} else if (json.contains("\"name\":\"Facebook\"")) {
			logger.error("WENT TO FACEBOOK");
			logger.error("input: " + json);
			Service s = new JSONDeserializer<Service>().use(null, Facebook.class).use("config", SpringSocialConfig.class).deserialize(json);
			logger.error(s.toString() + " " + s.getConfig().toString());
			return s;
 		} else if (json.contains("\"name\":\"Twitter\"")) {
 			return new JSONDeserializer<Service>().use(null, Twitter.class).use("config", SpringSocialConfig.class).deserialize(json);
 		} else if (json.contains("\"name\":\"Reddit\"")) {
 			return new JSONDeserializer<Service>().use(null, Reddit.class).use("config", RedditConfig.class).deserialize(json);
 		}
         return new JSONDeserializer<Service>().use(null, Service.class).deserialize(json);
     }
 }
