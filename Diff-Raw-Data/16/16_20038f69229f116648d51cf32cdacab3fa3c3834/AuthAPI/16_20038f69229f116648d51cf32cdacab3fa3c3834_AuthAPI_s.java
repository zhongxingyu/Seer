 package ru.redcraft.pinterest4j.core.api;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.ws.rs.core.Cookie;
 import javax.ws.rs.core.NewCookie;
 
 import org.apache.log4j.Logger;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 import ru.redcraft.pinterest4j.exceptions.PinterestAuthException;
 
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.ClientResponse.Status;
 import com.sun.jersey.api.representation.Form;
 
 public final class AuthAPI extends CoreAPI {
 
 	private static AuthAPI authAPI = new AuthAPI();
 	
 	private static final String CSRF_TOKEN_COOKIE = "csrftoken";
 	private static final String SESSION_COOKIE = "_pinterest_sess";
 	
 	private static final String LOGIN_FORM_EMAIL_FIELD = "email";
 	private static final String LOGIN_FORM_PASSWORD_FIELD = "password";
 	private static final String LOGIN_FORM_CSRF_FIELD = "csrfmiddlewaretoken";
 	
 	private static final Logger LOG = Logger.getLogger(AuthAPI.class);
 	
 	private AuthAPI() {
 		super(null, null);
 	}
 	
 	public static PinterestAccessToken authenticate(String email, String password) throws PinterestAuthException {
 		return authAPI.getAccessToken(email, password);
 	}
 	
 	public PinterestAccessToken getAccessToken(String email, String password) throws PinterestAuthException {
 		LOG.debug("Creating AccessToken for login = " + email);
 		
 		ClientResponse responClient = getWR(Protocol.HTTPS, "login/").get(ClientResponse.class);
 		Cookie csrfToken = getCookieMap(responClient).get(CSRF_TOKEN_COOKIE);
 		LOG.debug("CSRF cookie value = " + csrfToken.getValue());
 		
 		String htmlPage = responClient.getEntity(String.class);
		LOG.debug(htmlPage);
 		Document loginPage = Jsoup.parse(htmlPage);
 		String chToken = loginPage.select("input[name=_ch]").get(0).attr(VALUE_TAG_ATTR);
 		LOG.debug("CH token value = " + chToken);
 		
 		Form loginForm = getLoginForm(email, password, csrfToken, chToken);
 		responClient = getWR(Protocol.HTTPS, "login/").cookie(csrfToken).post(ClientResponse.class, loginForm);
 		if(responClient.getStatus() != Status.FOUND.getStatusCode()) {
 			throw new PinterestAuthException();
 		}
 		Cookie sessionToken = getCookieMap(responClient).get(SESSION_COOKIE);
 		LOG.debug("SESSION cookie value = " + sessionToken.getValue());
 		
 		PinterestAccessToken accessToken = new PinterestAccessToken(email, password, csrfToken, sessionToken);
 		
 		LOG.debug("ACCESS TOKEN: " + accessToken);
 		
 		return accessToken;
 	}
 	
 	private static Map<String, Cookie> getCookieMap(ClientResponse responClient) {
 		Map<String, Cookie> pinterestCookies = new HashMap<String, Cookie>();
 		for(NewCookie cookie: responClient.getCookies()) {
 			pinterestCookies.put(cookie.getName(), cookie);
 		}
 		return pinterestCookies;
 	}
 	
 	private static Form getLoginForm(String email, String password, Cookie csrfCookie, String chToken) {
 		Form form = new Form();
 		form.add(LOGIN_FORM_EMAIL_FIELD, email);
 		form.add(LOGIN_FORM_PASSWORD_FIELD, password);
 		form.add(LOGIN_FORM_CSRF_FIELD, csrfCookie.getValue());
 		form.add("_ch", chToken);
 		return form;
 	}
 
 }
