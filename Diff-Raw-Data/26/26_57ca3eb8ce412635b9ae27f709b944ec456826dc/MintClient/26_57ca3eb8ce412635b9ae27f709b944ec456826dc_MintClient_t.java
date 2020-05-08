 package eu.dm2e.ws.services.mint;
 
 import java.net.URI;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.ws.rs.core.Cookie;
 import javax.ws.rs.core.NewCookie;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource.Builder;
 
 /**
  * A wrapper for Jersey Client that
  * a) can store cookies
  * b) does *not* follow redirects
  * 
  * @author Konstantin Baierer
  *
  */
 public class MintClient {
 	
 	Logger log = LoggerFactory.getLogger(getClass().getName());
 	
 	private com.sun.jersey.api.client.Client jerseyClient = null;
 	public com.sun.jersey.api.client.Client getJerseyClient() {
     	if (null == this.jerseyClient) { 
     		Client jClient = new com.sun.jersey.api.client.Client();
     		jClient.setFollowRedirects(false);
     		this.jerseyClient = jClient;
     	}
 		return this.jerseyClient;
 	}
 	
	protected final Set<Cookie> cookies = new HashSet<>();
 	
 	public void addCookie(Cookie cookie) { this.cookies.add(cookie); }
 	public void addCookies(List<NewCookie> theseCookies) {
 		log.trace("Adding cookies: " + theseCookies);
 		for (Cookie thisCookie : theseCookies)
 			this.addCookie(thisCookie);
 	}
 	public void clearCookies() {
		log.debug("Clear cookies.");
 		this.cookies.clear(); 
 	}
 	
 	public Builder resource(URI uri) {
 		return this.resource(uri.toString());
 	}
 
 	public Builder resource(String uri) {
 		Builder reqB = getJerseyClient().resource(uri).getRequestBuilder();
 		for (Cookie cookie : cookies) {
			log.trace("Adding cookie to web resource: " + cookie);
 			reqB.cookie(cookie);
 		}
 		reqB.header("User-Agent", "omnom/mint-client");
		log.info("Built WebResource Builder for <{}>", uri);
 		return reqB;
 	}
 	
 }
