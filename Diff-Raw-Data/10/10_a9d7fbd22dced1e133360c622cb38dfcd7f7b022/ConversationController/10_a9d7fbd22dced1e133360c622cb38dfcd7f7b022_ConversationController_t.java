 package edu.rit.asksg.web;
 
 import com.google.common.base.Optional;
 import edu.rit.asksg.common.Log;
 import edu.rit.asksg.dataio.ScheduledPocessor;
 import edu.rit.asksg.domain.Conversation;
 import edu.rit.asksg.domain.Email;
 import edu.rit.asksg.domain.Facebook;
 import edu.rit.asksg.domain.Identity;
 import edu.rit.asksg.domain.Message;
 import edu.rit.asksg.domain.Reddit;
 import edu.rit.asksg.domain.Service;
 import edu.rit.asksg.domain.SocialSubscription;
 import edu.rit.asksg.domain.Twilio;
 import edu.rit.asksg.domain.Twitter;
 import edu.rit.asksg.domain.config.EmailConfig;
 import edu.rit.asksg.domain.config.ProviderConfig;
 import edu.rit.asksg.domain.config.SpringSocialConfig;
 import edu.rit.asksg.domain.config.TwilioConfig;
 import edu.rit.asksg.service.IdentityService;
 import edu.rit.asksg.service.ProviderService;
 import org.joda.time.LocalDateTime;
 import org.joda.time.Minutes;
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.context.request.ServletWebRequest;
 import org.springframework.web.context.request.WebRequest;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 @RooWebJson(jsonObject = Conversation.class)
 @Controller
 @RequestMapping("/conversations")
 public class ConversationController {
 
 	public static final Integer DEFAULT_COUNT = 10;
 
 	@Log
 	private Logger logger;
 
 	@Autowired
 	ProviderService providerService;
 
 	@Autowired
 	IdentityService identityService;
 
 	@Autowired
 	ScheduledPocessor scheduledPocessor;
 
 	@RequestMapping(value = "seed")
 	public ResponseEntity<String> seed() {
 
 		bootstrapProviders();
 
 		Conversation c = new Conversation();
 		Message m1 = new Message();
 		Identity identity1 = identityService.findOrCreate("Socrates");
 		m1.setIdentity(identity1);
 		m1.setContent("For the unexamined life is not worth living");
 		m1.setConversation(c);
 		Message m2 = new Message();
 		Identity identity2 = identityService.findOrCreate("Tyrion Lannister");
 		m2.setIdentity(identity2);
 		m2.setContent("Sorcery is the sauce fools spoon over failure to hide the flavor of the their own incompetence");
 		m2.setConversation(c);
 		List<Message> messages = new ArrayList<Message>();
 		messages.add(m1);
 		messages.add(m2);
 
 		c.setMessages(messages);
 		Service twilioprovider = providerService.findServiceByTypeAndIdentifierEquals(Twilio.class, "15852865275");
 		c.setService(twilioprovider);
 		conversationService.saveConversation(c);
 
 		pullContent();
 
 		HttpHeaders headers = new HttpHeaders();
 		headers.add("content_type", "text/plain");
 
 		return new ResponseEntity<String>("your seed has been spread", headers, HttpStatus.OK);
 	}
 
 	//Prevent roo from autogenerating method (hiding listJson with WebRequest)
 	private ResponseEntity<String> listJson() {
 		return listJson(new ServletWebRequest(null));
 	}
 
 	@RequestMapping(headers = "Accept=application/json")
 	@ResponseBody
 	public ResponseEntity<String> listJson(WebRequest params) {
 		String s = params.getParameter("since");
 		String u = params.getParameter("until");
 		String c = params.getParameter("count");
 		String b = params.getParameter("showRead");
 
 		Optional<Integer> since = Optional.absent();
 		Optional<Integer> until = Optional.absent();
 		Optional<Boolean> read = Optional.absent();
 
         //todo:not here
         Integer count = DEFAULT_COUNT;
 
 		if (s != null)
 			since = Optional.of(Integer.parseInt(s));
 
 		if (u != null)
 			until = Optional.of(Integer.parseInt(u));
 
 		if (c != null)
 			count = Integer.parseInt(c);
 
 		if (b != null)
 			read = Optional.of(Boolean.parseBoolean(b));
 
 		String[] excludeServices = params.getParameterValues("excludeServices[]");
 		if (excludeServices == null) {
 			excludeServices = new String[0];
 		}
 
 		String[] includes = params.getParameterValues("includeTags[]");
 		if (includes == null) includes = new String[0];
 
 
 		List<Conversation> conversations = conversationService.findAllConversations(
 				since, until, excludeServices, includes, read, count);
 
 		HttpHeaders headers = new HttpHeaders();
 		headers.add("Content-Type", "application/json; charset=utf-8");
 
 		try {
 			Conversation.toJsonArray(conversations);
 		} catch (Exception e) {
 			logger.error("Conversation failed to serialize!", e);
 		}
 
 		return new ResponseEntity<String>(Conversation.toJsonArray(conversations), headers, HttpStatus.OK);
 	}
 
 	private void bootstrapProviders() {
 
 		LocalDateTime now = LocalDateTime.now();
 
 		final Service twiloprovider = providerService.findServiceByTypeAndIdentifierEquals(Twilio.class, "15852865275");
 		if (twiloprovider == null) {
 			Service twilio = new Twilio();
 			TwilioConfig twilioconfig = new TwilioConfig();
 			twilioconfig.setIdentifier("15852865275");
 			twilioconfig.setPhoneNumber("15852865275");
 			twilioconfig.setUsername("AC932da9adfecf700aba37dba458fc9621");
 			twilioconfig.setAuthenticationToken("9cda6e23aa46651c9759492b625e3f35");
 			twilio.setConfig(twilioconfig);
 			providerService.saveService(twilio);
 		}
 
 		final Service emailProvider = providerService.findServiceByTypeAndIdentifierEquals(Email.class, "ritasksg");
 		if (emailProvider == null) {
 			Service email = new Email();
 			EmailConfig emailConfig = new EmailConfig();
 			emailConfig.setIdentifier("ritasksg");
 			emailConfig.setUsername("ritasksg@gmail.com");
 			emailConfig.setPassword("allHailSpring");
 			emailConfig.setHost("gmail.com");
 			email.setConfig(emailConfig);
 			providerService.saveService(email);
 		}
 
 		final Service twitterProvider = providerService.findServiceByTypeAndIdentifierEquals(Twitter.class, "wY0Aft0Gz410RtOqOHd7Q");
 		if (twitterProvider == null) {
 			Service twitter = new Twitter();
 			SpringSocialConfig twitterConfig = new SpringSocialConfig();
 			twitterConfig.setIdentifier("wY0Aft0Gz410RtOqOHd7Q");
 
 			twitterConfig.setConsumerKey("wY0Aft0Gz410RtOqOHd7Q");
 			twitterConfig.setConsumerSecret("rMxrTP9nqPzwU6UHIQufKR23be4w4NHIqY7VbwfzU");
 			twitterConfig.setAccessToken("15724679-FUz0huThLIpEzm66QySG7exllaV1CWV9VqXxXeTOw");
 			twitterConfig.setAccessTokenSecret("rFTEFz8tNX71V2nCo6pDtF38LhDEfO2f692xxzQxaA");
 
 			twitter.setConfig(twitterConfig);
 
 			Set<SocialSubscription> subscriptions = new HashSet<SocialSubscription>();
 			subscriptions.add(new SocialSubscription("SG", "RIT_SG"));
 			subscriptions.add(new SocialSubscription("rithash", "#RIT"));
 			subscriptions.add(new SocialSubscription("news hash", "#RITNews"));
 			subscriptions.add(new SocialSubscription("cab", "ritcab"));
 			subscriptions.add(new SocialSubscription("SpringFest Hash", "#RITSF"));
 			subscriptions.add(new SocialSubscription("Academic Affairs", "RIT_AcadAffairs"));
 			subscriptions.add(new SocialSubscription("Imagine RIT", "Imagine_RIT"));
 			subscriptions.add(new SocialSubscription("RIT Sports", "RITsports"));
 
 			twitterConfig.setSubscriptions(subscriptions);
 
 			providerService.saveService(twitter);
 		}
 
 		final Service facebookProvider = providerService.findServiceByTypeAndIdentifierEquals(Facebook.class, "asksgfbapp");
 		if (facebookProvider == null) {
 			Service facebook = new Facebook();
 			SpringSocialConfig fbConfig = new SpringSocialConfig();
 			fbConfig.setIdentifier("asksgfbapp");
 
 			facebook.setConfig(fbConfig);
 
 			SocialSubscription ritsg = new SocialSubscription();
 			ritsg.setHandle("ritstudentgov");
 
 			SocialSubscription clubs = new SocialSubscription();
 			clubs.setHandle("RITClubs");
 
 			SocialSubscription rit = new SocialSubscription();
 			rit.setHandle("RITfb");
 
 			SocialSubscription news = new SocialSubscription();
 			news.setHandle("RITNews");
 
 			Set<SocialSubscription> subscriptions = new HashSet<SocialSubscription>();
 			subscriptions.add(ritsg);
 			subscriptions.add(clubs);
 			subscriptions.add(rit);
 			subscriptions.add(news);
 
 			fbConfig.setSubscriptions(subscriptions);
 
 			providerService.saveService(facebook);
 		}
 
 		final Service redditProvider = providerService.findServiceByTypeAndIdentifierEquals(Reddit.class, "rit");
 		if (redditProvider == null) {
 			Service reddit = new Reddit();
 			ProviderConfig config = new ProviderConfig();
 			config.setIdentifier("rit");
 			reddit.setConfig(config);
 
             SocialSubscription java = new SocialSubscription();
             java.setHandle("rit");
             Set<SocialSubscription> subscriptions = new HashSet<SocialSubscription>();
             subscriptions.add(java);
 
 			config.setSubscriptions(subscriptions);
             config.setSubscriptions(subscriptions);
 
 			providerService.saveService(reddit);
 
 		}
 	}
 
 	@RequestMapping(value = "/tweet")
 	public ResponseEntity<String> twats() {
 
 		Service twitter = providerService.findServiceByTypeAndIdentifierEquals(Twitter.class, "wY0Aft0Gz410RtOqOHd7Q");
 		List<Conversation> twats = twitter.getNewContent();
 		for (Conversation c : twats) {
 			//conversationService.saveConversation(c);
 		}
 
 		HttpHeaders headers = new HttpHeaders();
 		headers.add("content_type", "text/plain");
 
 		return new ResponseEntity<String>("Show me your tweets!", headers, HttpStatus.OK);
 	}
 
 	protected void pullContent() {
 		scheduledPocessor.executeRefresh();
 		scheduledPocessor.executeSubscriptions();
 	}
 
 
 	@RequestMapping(value = "/refresh")
 	public ResponseEntity<String> refresh() {
 		pullContent();
 
 		HttpHeaders headers = new HttpHeaders();
 		headers.add("content_type", "text/plain");
 
 		return new ResponseEntity<String>("Refresh Requested", headers, HttpStatus.OK);
 	}
 }
