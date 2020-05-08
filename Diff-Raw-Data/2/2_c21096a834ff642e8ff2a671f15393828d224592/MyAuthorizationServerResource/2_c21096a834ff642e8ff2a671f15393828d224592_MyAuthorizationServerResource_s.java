 package oauthsample.oauth;
 
 import org.restlet.data.Form;
 import org.restlet.ext.oauth.AuthorizationServerResource;
 
 public class MyAuthorizationServerResource extends AuthorizationServerResource {
 
 	// TODO jvw controleren of alles nog gewoon werkt
 	@Override
 	public Form getQuery() {
 
 		org.restlet.data.Form params = new Form();
		params.add("client_id", "1336");
 		params.add("client_secret", "secret1");
 		params.add("grant_type", "access_token");
 		params.add(REDIR_URI, "https://statusnet.surfnetlabs.nl/cs/status.html");
 
 		return params;
 	}
 
 }
