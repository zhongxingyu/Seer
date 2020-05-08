 /**
  * 
  */
 package pl.psnc.dl.wf4ever.portal;
 
 import java.net.URI;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.Session;
 import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
 import org.apache.wicket.authroles.authorization.strategies.role.Roles;
 import org.apache.wicket.request.Request;
 import org.apache.wicket.util.cookies.CookieUtils;
 import org.openid4java.discovery.DiscoveryInformation;
 import org.purl.wf4ever.rosrs.client.Creator;
 import org.purl.wf4ever.rosrs.client.ROSRService;
 import org.purl.wf4ever.rosrs.client.users.User;
 import org.purl.wf4ever.rosrs.client.users.UserManagementService;
 import org.scribe.model.Token;
 
 /**
  * Custom app session.
  * 
  * @author piotrhol
  * 
  */
 public class MySession extends AbstractAuthenticatedWebSession {
 
     /** Id. */
     private static final long serialVersionUID = -4113134277706549806L;
 
     /** Logger. */
     private static final Logger LOG = Logger.getLogger(MySession.class);
 
     /** RODL access token. */
     private Token dLibraAccessToken;
 
     /** Should RODL tokens be flushed to cookies. */
     private boolean dirtydLibra = false;
 
     /** myExperiment access token. */
     private Token myExpAccessToken;
 
     /** Should myExperiment tokens be flushed to cookies. */
     private boolean dirtyMyExp = false;
 
     /** Temporary token used for OAuth 1.0 with myExperiment. */
     private Token requestToken;
 
     /** Cookie key. */
     private static final String DLIBRA_KEY = "dlibra";
 
     /** Cookie key. */
     private static final String MYEXP_KEY_TOKEN = "myexp1";
 
     /** Cookie key. */
     private static final String MYEXP_KEY_SECRET = "myexp2";
 
     /** Usernames cache. */
     private final Map<URI, Creator> usernames = new HashMap<>();
 
     /** OpenID discovery information. */
     private DiscoveryInformation discoveryInformation;
 
     /** OpenID request token. */
     private String rodlRequestToken;
 
     /** Callback to the application's OpenID endpoint. */
     private URI openIDCallbackURI;
 
     /** RODL user. */
     private User user;
 
     /** ROSRS client. */
     private ROSRService rosrs;
 
     /** UMS client. */
     private UserManagementService ums;
 
 
     /**
      * Constructor.
      * 
      * @param request
      *            same as for superclass
      */
     public MySession(Request request) {
         super(request);
         PortalApplication app = (PortalApplication) getApplication();
         this.rosrs = new ROSRService(app.getRodlURI().resolve("ROs/"), null);
        this.ums = new UserManagementService(app.getRodlURI(), app.getAdminToken());
         if (new CookieUtils().load(DLIBRA_KEY) != null) {
             signIn(new CookieUtils().load(DLIBRA_KEY));
         }
         if (new CookieUtils().load(MYEXP_KEY_TOKEN) != null && new CookieUtils().load(MYEXP_KEY_SECRET) != null) {
             myExpAccessToken = new Token(new CookieUtils().load(MYEXP_KEY_TOKEN),
                     new CookieUtils().load(MYEXP_KEY_SECRET));
         }
     }
 
 
     /**
      * Singleton.
      * 
      * @return the only instance
      */
     public static MySession get() {
         return (MySession) Session.get();
     }
 
 
     public void signIn(String userToken) {
         try {
             PortalApplication app = (PortalApplication) getApplication();
             this.rosrs = new ROSRService(app.getRodlURI().resolve("ROs/"), userToken);
             this.user = getUms().getWhoAmi(userToken);
         } catch (Exception e) {
             LOG.error("Error when retrieving user data: " + e.getMessage());
         }
         dirtydLibra = true;
     }
 
 
     /**
      * myExperiment access token.
      * 
      * @return the myExpAccessToken
      */
     public Token getMyExpAccessToken() {
         return myExpAccessToken;
     }
 
 
     /**
      * myExperiment access token.
      * 
      * @param myExpAccessToken
      *            the myExpAccessToken to set
      */
     public void setMyExpAccessToken(Token myExpAccessToken) {
         this.myExpAccessToken = myExpAccessToken;
         dirtyMyExp = true;
     }
 
 
     /**
      * myExperiment temporary access token.
      * 
      * @return the requestToken
      */
     public Token getRequestToken() {
         return requestToken;
     }
 
 
     /**
      * myExperiment temporary access token.
      * 
      * @param requestToken
      *            the requestToken to set
      */
     public void setRequestToken(Token requestToken) {
         this.requestToken = requestToken;
     }
 
 
     @Override
     public Roles getRoles() {
         return isSignedIn() ? new Roles(Roles.USER) : null;
     }
 
 
     @Override
     public boolean isSignedIn() {
         return user != null;
     }
 
 
     /**
      * Remove access tokens from memory and cookies.
      */
     public void signOut() {
         dLibraAccessToken = null;
         myExpAccessToken = null;
         user = null;
         rosrs = new ROSRService(rosrs.getRosrsURI(), null);
         new CookieUtils().remove(DLIBRA_KEY);
         new CookieUtils().remove(MYEXP_KEY_TOKEN);
         new CookieUtils().remove(MYEXP_KEY_SECRET);
     }
 
 
     /**
      * Flush access tokens to cookies.
      */
     public void persist() {
         if (dirtydLibra) {
             if (dLibraAccessToken != null) {
                 new CookieUtils().save(DLIBRA_KEY, dLibraAccessToken.getToken());
             }
             dirtydLibra = false;
         }
         if (dirtyMyExp) {
             if (myExpAccessToken != null) {
                 new CookieUtils().save(MYEXP_KEY_TOKEN, myExpAccessToken.getToken());
                 new CookieUtils().save(MYEXP_KEY_SECRET, myExpAccessToken.getSecret());
             }
             dirtyMyExp = false;
         }
     }
 
 
     public User getUser() {
         return user;
     }
 
 
     /**
      * The usernames cache.
      * 
      * @return the usernames
      */
     public Map<URI, Creator> getUsernames() {
         return usernames;
     }
 
 
     public DiscoveryInformation getDiscoveryInformation() {
         return discoveryInformation;
     }
 
 
     public void setDiscoveryInformation(DiscoveryInformation discoveryInformation) {
         this.discoveryInformation = discoveryInformation;
     }
 
 
     public String getRodlRequestToken() {
         return rodlRequestToken;
     }
 
 
     public void setRodlRequestToken(String rodlRequestToken) {
         this.rodlRequestToken = rodlRequestToken;
     }
 
 
     public URI getOpenIDCallbackURI() {
         return openIDCallbackURI;
     }
 
 
     public void setOpenIDCallbackURI(URI openIDCallbackURI) {
         this.openIDCallbackURI = openIDCallbackURI;
     }
 
 
     public ROSRService getRosrs() {
         return rosrs;
     }
 
 
     public UserManagementService getUms() {
         return ums;
     }
 }
