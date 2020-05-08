 /**
  * 
  */
 package net.mysocio.data.accounts.google;
 
 import java.net.ConnectException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import net.mysocio.connection.google.GoogleSource;
 import net.mysocio.connection.readers.Source;
 import net.mysocio.data.SocioTag;
 import net.mysocio.data.UserTags;
 import net.mysocio.data.accounts.Oauth2Account;
 import net.mysocio.data.messages.GeneralMessage;
 
 import org.scribe.model.OAuthRequest;
 import org.scribe.model.Response;
 import org.scribe.model.Verb;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.jmkgreen.morphia.annotations.Entity;
 
 /**
  * @author Aladdin
  *
  */
 @Entity("accounts")
 public class GoogleAccount extends Oauth2Account{
 	public static final String ACCOUNT_TYPE = "google";
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 7707109361608748417L;
 	
 	private static final Logger logger = LoggerFactory.getLogger(GoogleAccount.class);
 	
 	private String refreshToken;
 	
 	public GoogleAccount() {
 		super();
 	}
 
 	public String getRefreshToken() {
 		return refreshToken;
 	}
 	public void setRefreshToken(String refreshToken) {
 		this.refreshToken = refreshToken;
 	}
 	@Override
 	public String getAccountType() {
 		return ACCOUNT_TYPE;
 	}
 
 	@Override
 	public List<Source> getSources() {
 		List<Source> sources = new ArrayList<Source>();
 		GoogleSource source = new GoogleSource();
 		source.setAccount(this);
 		source.setName(getUserName());
		source.setUrl("http://graph.facebook.com/" + getAccountUniqueId());
 		sources.add(source);
 		return sources;
 	}
 	
 	@Override
 	public String getIconUrl() {
 		return "google.icon.account";
 	}
 
 	@Override
 	public SocioTag createAccountTagset(UserTags userTags) {
 		SocioTag accountTypeTag = createAccountTypeTag(userTags);
 		SocioTag sourceTag = userTags.createTag(getAccountUniqueId(), getUserName(), accountTypeTag);
 		return accountTypeTag;
 	}
 
 	public String getGoogleReaderFeeds(String userId) throws Exception{
 		String url = "https://www.google.com/reader/api/0/subscription/list";
 		OAuthRequest request = new OAuthRequest(Verb.GET, url);
 		request.addHeader("Authorization", "OAuth " + getToken());
 		Response response = request.send();
 		if (response.getCode() != 200) {
 			logger.error("Error getting Google Reader data for url: " + url + " token: " + getToken());
 			Set<String> headers = response.getHeaders().keySet();
 			for (String name : headers) {
 				logger.error(response.getHeader(name));
 			}
 			throw new ConnectException("Error getting Google Reader data for url: "
 					+ url);
 		}
 		return response.getBody();
 	}
 
 	@Override
 	public void like(GeneralMessage message) throws Exception {
 		// TODO Auto-generated method stub
 	}
 	
 	@Override
 	public void postToAccount(String message) throws Exception {
 		// TODO Auto-generated method stub
 	}
 }
