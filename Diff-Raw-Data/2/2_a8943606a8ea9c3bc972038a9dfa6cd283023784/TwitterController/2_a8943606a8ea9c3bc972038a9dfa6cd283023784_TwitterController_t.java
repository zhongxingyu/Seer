 /**
  * Licensed to Jasig under one or more contributor license
  * agreements. See the NOTICE file distributed with this work for
  * additional information regarding copyright ownership.
  *
  * Jasig licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at:
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.esupportail.twitter.web;
 
 import java.util.List;
 
 import javax.portlet.ActionRequest;
 import javax.portlet.ActionResponse;
 import javax.portlet.PortletPreferences;
 import javax.portlet.RenderRequest;
 import javax.portlet.RenderResponse;
 
 import org.apache.log4j.Logger;
 import org.esupportail.twitter.beans.OAuthTwitterConfig;
 import org.esupportail.twitter.services.OAuthTwitterApplicationOnlyService;
 import org.scribe.builder.ServiceBuilder;
 import org.scribe.builder.api.TwitterApi;
 import org.scribe.model.Token;
 import org.scribe.model.Verifier;
 import org.scribe.oauth.OAuthService;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.social.twitter.api.Tweet;
 import org.springframework.social.twitter.api.Twitter;
 import org.springframework.social.twitter.api.TwitterProfile;
 import org.springframework.social.twitter.api.impl.TwitterTemplate;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.util.StringUtils;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.portlet.ModelAndView;
 
 @Controller
 public class TwitterController implements InitializingBean {
 
 	private static Logger log = Logger.getLogger(TwitterController.class);
 	
 	private static final String PREF_TWITTER_USERNAME = "twitterUsername";
 	//private static final String PREF_TWITTER_PASSWORD = "twitterPassword";
     private static final String DEFAULT_TWITTER_USERNAME = "lavillederouen";
     
     private static final String PREF_TWITTER_USER_TOKEN = "twitterUserToken";
     private static final String PREF_TWITTER_USER_SECRET = "twitterUserSecret";
     
     private static final String PREF_TWITTER_TWEETS_NUMBER = "twitterTweetsNumber";
     
     
     @Autowired
     protected OAuthTwitterConfig oAuthTwitterConfig;
     
     @Autowired
     OAuthTwitterApplicationOnlyService oAuthTwitterApplicationOnlyService;
  
     
     protected OAuthService service;
     
 	public void afterPropertiesSet() throws Exception {		
 		if(oAuthTwitterConfig != null && !oAuthTwitterConfig.getConsumerKey().isEmpty() && !oAuthTwitterConfig.getConsumerSecret().isEmpty()) {
			service = new ServiceBuilder().provider(TwitterApi.SSL.class)
 			.apiKey(oAuthTwitterConfig.getConsumerKey())
 			.apiSecret(oAuthTwitterConfig.getConsumerSecret())
 			// .callback(oAuthTwitterConfig.getCallbackUrl())
 			.build();
 		} else {
 			new Exception("You have to setup twitterConfig.xml and register your EsupTwitter on https://dev.twitter.com/apps");
 		}
 	}
 	
 	public boolean isOAuthEnabled() {
 		return service != null;
 	}
 	
 	
 
     @RequestMapping("VIEW")
     protected ModelAndView renderView(RenderRequest request, RenderResponse response) throws Exception {
         
     	ModelMap model = new ModelMap();
     	
     	final PortletPreferences prefs = request.getPreferences();
     	String twitterUsername = prefs.getValue(PREF_TWITTER_USERNAME, DEFAULT_TWITTER_USERNAME);	
     	
     	String applicationOnlyBearerToken = oAuthTwitterApplicationOnlyService.getApplicationOnlyBearerToken();
     	
     	String twitterUserToken = prefs.getValue(PREF_TWITTER_USER_TOKEN, null);
     	String twitterUserSecret = prefs.getValue(PREF_TWITTER_USER_SECRET, null);	
     	
     	int tweetsNumber = (new Integer(prefs.getValue(PREF_TWITTER_TWEETS_NUMBER, "-1"))).intValue();	
 
     	// get username timeline with oAuth authentication
     	log.debug("twitterUserToken:" + twitterUserToken);
     	log.debug("twitterUserSecret:" + twitterUserSecret);
     	
     	Twitter twitter;
     	TwitterProfile twitterProfile;
     	List<Tweet> tweetList;
     	
     	if(twitterUserToken != null && twitterUserSecret != null) {
     		twitter = new TwitterTemplate(oAuthTwitterConfig.getConsumerKey(), oAuthTwitterConfig.getConsumerSecret(), twitterUserToken, twitterUserSecret);
         	twitterProfile = twitter.userOperations().getUserProfile();   			
         	tweetList = twitter.timelineOperations().getHomeTimeline(tweetsNumber);
     	} else {
     		twitter = new TwitterTemplate(applicationOnlyBearerToken);
         	twitterProfile = twitter.userOperations().getUserProfile(twitterUsername);   			
         	tweetList = twitter.timelineOperations().getUserTimeline(twitterUsername, tweetsNumber);
     	}
 
     	model.put("tweetList", tweetList);
     	model.put("twitterProfile", twitterProfile);
 
     	response.setTitle("Twitter " + twitterProfile.getScreenName());
 
     		// return new ModelAndView("error", model);
     	 
     	return new ModelAndView("view", model);
     }
     
     @RequestMapping("EDIT")
 	public ModelAndView renderEditView(RenderRequest request, RenderResponse response) throws Exception {
         final PortletPreferences prefs = request.getPreferences();
         ModelMap model = new ModelMap();
         if(this.isOAuthEnabled()) {
 			Token requestToken = service.getRequestToken();
 			String twitterAccessToken = requestToken.getToken();
 			String twitterAccessTokenSecret = requestToken.getSecret();
 			model.put("twitterAccessToken", twitterAccessToken);
 			model.put("twitterAccessTokenSecret", twitterAccessTokenSecret);
 		}
         
         String currentTwitterUsername = prefs.getValue(PREF_TWITTER_USERNAME, DEFAULT_TWITTER_USERNAME);
     	boolean connected = false;
  
     	String twitterUserToken = prefs.getValue(PREF_TWITTER_USER_TOKEN, null);
     	String twitterUserSecret = prefs.getValue(PREF_TWITTER_USER_SECRET, null);
     	
     	String twitterTweetsNumber = prefs.getValue(PREF_TWITTER_TWEETS_NUMBER, "-1");
     	
     	if(twitterUserToken!=null) {
     		Twitter twitter = new TwitterTemplate(oAuthTwitterConfig.getConsumerKey(), oAuthTwitterConfig.getConsumerSecret(), twitterUserToken, twitterUserSecret);
 			TwitterProfile twitterProfile = twitter.userOperations().getUserProfile();   			
     		currentTwitterUsername = twitterProfile.getName();
     		connected = true;
     	}
     	
 		model.put("isOAuthEnabled", this.isOAuthEnabled());
 		model.put("currentTwitterUsername", currentTwitterUsername);
 		model.put("connectedMode", connected);
 		model.put("twitterTweetsNumber", twitterTweetsNumber);
 		
 		return new ModelAndView("edit", model);
 	}
     
 
     @RequestMapping(value = {"EDIT"}, params = {"action=setTwitterUsername"})
     public void setTwitterUsername(
             @RequestParam(value = "twitterUsername", required = true) String twitterUsername,
             ActionRequest request, ActionResponse response) throws Exception {
     	
         // validate the submitted data
         if (StringUtils.hasText(twitterUsername) && StringUtils.hasLength(twitterUsername)) {
         	PortletPreferences prefs = request.getPreferences();
         	prefs.setValue(PREF_TWITTER_USER_TOKEN, null);
         	prefs.setValue(PREF_TWITTER_USER_SECRET, null);
         	prefs.setValue(PREF_TWITTER_USERNAME, twitterUsername);
             prefs.store();
         }
 
         return;
 	}
     
     @RequestMapping(value = {"EDIT"}, params = {"action=setTwitterPin"})
     public void setTwitterPin (
             @RequestParam(value = "twitterAccessToken", required = true) String twitterAccessToken,
             @RequestParam(value = "twitterAccessTokenSecret", required = true) String twitterAccessTokenSecret,
             @RequestParam(value = "twitterPin", required = true) String twitterPin,
             ActionRequest request, ActionResponse response) throws Exception {
 
     	Verifier verifier = new Verifier(twitterPin);
     	Token requestToken = new Token(twitterAccessToken, twitterAccessTokenSecret);
     	
     	Token accessToken = null;
     	try {
     		accessToken = service.getAccessToken(requestToken, verifier);
     	} catch (Exception ex) {
     		log.info("pb retrieving accessToken : maybe the user put a wrong pin code");
     		log.debug("exception when retrieving accessToken : " + ex.getMessage(), ex);
     	}
     	
     	if(accessToken != null) {
             PortletPreferences prefs = request.getPreferences();
     		prefs.setValue(PREF_TWITTER_USERNAME, "");
     		prefs.setValue(PREF_TWITTER_USER_TOKEN, accessToken.getToken());
     		prefs.setValue(PREF_TWITTER_USER_SECRET, accessToken.getSecret());
     		prefs.store();
     	}
     	
     	return;
 	}
     
     @RequestMapping(value = {"EDIT"}, params = {"action=setTwitterTweetsNumber"})
     public void setTwitterTweetsNumber(
             @RequestParam(value = "twitterTweetsNumber", required = true) String twitterTweetsNumber,
             ActionRequest request, ActionResponse response) throws Exception {
 
         // validate the submitted data
         if (StringUtils.hasText(twitterTweetsNumber) && StringUtils.hasLength(twitterTweetsNumber)) {
         	int twitterTweetsNumberInt = -1;
         	try {
         		twitterTweetsNumberInt = (new Integer(twitterTweetsNumber)).intValue();
         	} catch (Exception ex) {
         	}
         	if(twitterTweetsNumberInt > 0) {
         		PortletPreferences prefs = request.getPreferences();
         		prefs.setValue(PREF_TWITTER_TWEETS_NUMBER, twitterTweetsNumber);
         		prefs.store();
         	}
         }
 
         return;
 	}
     
     
     @RequestMapping("ABOUT")
 	public ModelAndView renderAboutView(RenderRequest request, RenderResponse response) throws Exception {
 		ModelMap model = new ModelMap();
 		return new ModelAndView("about", model);
 	}
     
     @RequestMapping("HELP")
 	public ModelAndView renderHelpView(RenderRequest request, RenderResponse response) throws Exception {
 		ModelMap model = new ModelMap();
 		return new ModelAndView("help", model);
 	}
 
     
 }
