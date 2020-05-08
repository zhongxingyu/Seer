 package org.knetwork.webapp.service;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.knetwork.webapp.util.SessionMapUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Service;
 
 import com.opentok.api.OpenTokSDK;
 import com.opentok.api.OpenTokSession;
 import com.opentok.api.constants.RoleConstants;
 import com.opentok.exception.OpenTokException;
 
 @Service
 public class TokboxServiceImpl implements TokboxService {
 	
 	private final Logger logger = LoggerFactory.getLogger(getClass());
 	
 	private OpenTokSession oSession;
 	private OpenTokSDK sdk;
 	private int apiKey;
 	private String apiSecret;
 	
 	public int getApiKey() {
 		return apiKey;
 	}
 	
 	public TokboxServiceImpl() {
 		super();
 		this.apiKey = Integer.parseInt("12109012");
 		this.apiSecret = "52d0de2b49af6597a54f8a4f0b3195c01c57f6b5";
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.knetwork.webapp.service.TokboxService#createSession()
 	 */
 	@Override
 	public Map<String, String> createSession(String learningSessionId) throws OpenTokException {
 		this.sdk = new OpenTokSDK(apiKey, apiSecret);
 		
 		String s = sdk.generate_token("session");
 		logger.info("Tokbox token: " + s);
 		String moderatorToken = sdk.generate_token("session",RoleConstants.MODERATOR);
 		logger.info("Token, moderator:  " + moderatorToken);
 	
 		//Generate a basic session
 		oSession = sdk.create_session();
 		logger.info("Creating Tokbox session: " + oSession.session_id);
 		
 		Map<String, String> map = new HashMap<String, String>();
 		map.put("tokboxSessionId", oSession.session_id);
 		map.put("moderatorToken", moderatorToken);
 		
 		SessionMapUtil.addTokboxSessionId(learningSessionId, oSession.session_id);
 		
 		System.out.println("Added tokbox session id:" + SessionMapUtil.getTokboxSessionId(learningSessionId)
 				+ " to learning session:" + learningSessionId);
 		
 		return map;
 	}
 	
 	public Map<String, String> getUserTokens(String tokboxSessionId) throws OpenTokException {
 		this.sdk = new OpenTokSDK(apiKey, apiSecret);
 		String publisherToken = sdk.generate_token(tokboxSessionId,RoleConstants.PUBLISHER);
 		String subscriberToken = sdk.generate_token(tokboxSessionId,RoleConstants.SUBSCRIBER);
 		
 		Map<String, String> map = new HashMap<String, String>();
 		map.put("publisherToken", publisherToken);
 		map.put("subscriberToken", subscriberToken);
		map.put("userToken",  sdk.generate_token(tokboxSessionId));		
 		return map;
 	}
 }
