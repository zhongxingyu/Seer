 package org.social.core.network;
 
import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.json.JSONObject;
 import net.sf.json.JSONSerializer;
 
 import org.scribe.builder.ServiceBuilder;
 import org.scribe.model.OAuthRequest;
 import org.scribe.model.Response;
 import org.scribe.model.Token;
 import org.scribe.model.Verb;
 import org.scribe.oauth.OAuthService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.social.core.constants.Networks;
 import org.social.core.entity.domain.Customers;
 import org.social.core.entity.domain.Messages;
 import org.social.core.network.oauth.YelpV2Api;
 import org.social.core.query.YelpQuery;
 import org.social.core.util.UtilProperties;
 
 public class YelpConnection extends SocialNetworkConnection {
 
 	private Logger logger = LoggerFactory.getLogger(this.getClass());
 
 	private String PROPERTY = "conf/yelp.properties";
 
 	private String CONSUMER_KEY = "consumerKey";
 	private String CONSUMER_SECRET = "consumerSecret";
 	private String ACCESS_TOKEN = "accessToken";
 	private String ACESS_TOEN_SECRET = "accessTokenSecret";
 
 	private String consumerKey = "";
 	private String consumerSecret = "";
 	private String accessToken = "";
 	private String accessTokenSecret = "";
 
 	private OAuthService service = null;
 	private Token token;
 
 	public YelpConnection(Customers customer) {
 		super(customer);
 
 		loadProperties();
 
 		ServiceBuilder builder = new ServiceBuilder();
 		builder.provider(YelpV2Api.class);
 		builder.apiKey(consumerKey);
 		builder.apiSecret(consumerSecret);
 
 		service = builder.build();
 
 		token = new Token(accessToken, accessTokenSecret);
 
 		this.customer = customer;
 
 		getCustomersKeywords(Networks.YELP.getName());
 	}
 
 	@Override
 	public List<Messages> fetchMessages() {
 		if (logger.isDebugEnabled()) {
 			logger.debug("Fetch information from YELP for customer: " + this.customer.getCustomerId());
 		}
 
 		YelpQuery query = buildQueryFromKeywords();
 		String constructedQuery = query.constructQuery();
 
 		OAuthRequest request = new OAuthRequest(Verb.GET, constructedQuery);
 
 		service.signRequest(token, request);
 
 		Response response = request.send();
 
 		JSONObject json = (JSONObject) JSONSerializer.toJSON(response.getBody());
 		List<Messages> resultList = extractMessageData(json);
 
 		return resultList;
 	}
 
 	private List<Messages> extractMessageData(JSONObject json) {
 		List<Messages> extractedData = new ArrayList<Messages>();
 
 		// TODO yelp doesn't offer more than 3 (random) reviews for a request
 
 		return extractedData;
 	}
 
 	private YelpQuery buildQueryFromKeywords() {
 		YelpQuery yelpQuery = new YelpQuery(super.customerNetworkKeywords);
 
		Timestamp sinceTs = this.customer.getLastNetworkdAccess();
		if(sinceTs != null) {
			String since = sinceTs.toString();
			yelpQuery.setSince(since);
		}

 		yelpQuery.setLanguage("en");
 
 		return yelpQuery;
 	}
 
 	private void loadProperties() {
 		this.consumerKey = UtilProperties.getPropertyValue(PROPERTY, CONSUMER_KEY);
 		this.consumerSecret = UtilProperties.getPropertyValue(PROPERTY, CONSUMER_SECRET);
 		this.accessToken = UtilProperties.getPropertyValue(PROPERTY, ACCESS_TOKEN);
 		this.accessTokenSecret = UtilProperties.getPropertyValue(PROPERTY, ACESS_TOEN_SECRET);
 	}
 }
