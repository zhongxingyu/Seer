 package org.cvut.wa2.projectcontrol;
 
 import java.io.IOException;
 
 import javax.jdo.PersistenceManager;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.cvut.wa2.projectcontrol.DAO.PMF;
 import org.cvut.wa2.projectcontrol.entities.DocumentsToken;
 
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
 import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
 import com.google.gdata.client.authn.oauth.OAuthException;
 import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
 import com.google.gdata.client.authn.oauth.OAuthSigner;
 
 public class DocumentsCallBackServlet extends HttpServlet{
 
 	private static final long serialVersionUID = -6800367517255095306L;
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		UserService userService = UserServiceFactory.getUserService();
     	String tokenSecret = (String) req.getSession().getAttribute("tokenSecret");
     	
     	if(userService.isUserLoggedIn() && tokenSecret != null) {
 			User user = userService.getCurrentUser();
 			String email = user.getEmail();
 			String consumerKey = "anonymous";
 			String consumerSecret = "anonymous";
 
 			GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
 			oauthParameters.setOAuthConsumerKey(consumerKey);
 			oauthParameters.setOAuthConsumerSecret(consumerSecret);
 
 			OAuthSigner signer = new OAuthHmacSha1Signer();
 			GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(signer);
 
 			// Remember the token secret that we stashed? Let's get it back
 			// now. We need to add it to oauthParameters
 			oauthParameters.setOAuthTokenSecret(tokenSecret);
 
 			// The query string should contain the oauth token, so we can just
 			// pass the query string to our helper object to correctly
 			// parse and add the parameters to our instance of oauthParameters
 			oauthHelper.getOAuthParametersFromCallback(req.getQueryString(), oauthParameters);
 
 			PersistenceManager pm = PMF.get();
 			try {
 				// Now that we have all the OAuth parameters we need, we can
 				// generate an access token and access token secret. These
 				// are the values we want to keep around, as they are
 				// valid for all API calls in the future until a user revokes
 				// our access.
 				String accessToken = oauthHelper.getAccessToken(oauthParameters);
 				String accessTokenSecret = oauthParameters.getOAuthTokenSecret();
 				
 				if(accessToken != null && !accessToken.isEmpty()) {
 					// Store token for the user in datastore
 					DocumentsToken token = new DocumentsToken();
 					token.setEmail(email);
 					token.setToken(accessToken);
 					token.setTokenSecret(accessTokenSecret);
 					pm.makePersistent(token);
 				}
 
 			} catch (OAuthException e) {
 				e.printStackTrace();
			} finally {
				pm.close();
 			}
     	}
         resp.sendRedirect("/tasks");
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		doGet(req, resp);
 	}
 
 }
