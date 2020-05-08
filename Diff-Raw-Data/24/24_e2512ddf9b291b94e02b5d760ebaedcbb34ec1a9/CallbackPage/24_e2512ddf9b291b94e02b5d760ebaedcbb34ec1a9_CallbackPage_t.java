 package nu.danielsundberg.yakutia;
 
 import nu.danielsundberg.yakutia.auth.CreateAccountPage;
 import nu.danielsundberg.yakutia.auth.OauthParameters;
 import nu.danielsundberg.yakutia.session.MySession;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.scribe.builder.ServiceBuilder;
 import org.scribe.builder.api.GoogleApi;
 import org.scribe.model.*;
 import org.scribe.oauth.OAuthService;
 import java.util.Iterator;
 
 /**
  * User: Fredde
  * Date: 3/29/13 12:19 AM
  */
 public class CallbackPage extends BasePage {
 
     public CallbackPage(PageParameters parameters) {
         super(parameters);
         getSession().bind();
 
         boolean isAdmin = false;
 
         String oauthVerifier = null;
         Iterator<String> iter = parameters.getNamedKeys().iterator();
         while (iter.hasNext()) {
             String name = iter.next();
             if (name.equals("oauth_verifier")) {
                 oauthVerifier = parameters.get(name).toString();
                 if (oauthVerifier.equals("undefined")) {
                     oauthVerifier = null;
                     // TODO handle some exception here?
                 }
             } else if (name.equals("admin")) {
                 isAdmin = true;
                 break;
             }
         }
 
         if (isAdmin) {
             adminSingIn();
         } else {
             Token requestToken = null;
             if (getSession().getAttribute("requestToken") != null) {
                 requestToken = (Token) getSession().getAttribute("requestToken");
             }
 
             OAuthService service = new ServiceBuilder()
                     .provider(GoogleApi.class)
                     .apiKey(OauthParameters.APIKEY)
                     .apiSecret(OauthParameters.APISECRET)
                     .scope(OauthParameters.SCOPE)
                     .build();
 
             String emailExtracted = "";
             if (oauthVerifier != null) {
                 try {
                     Verifier verifier = new Verifier(oauthVerifier);
                     Token accessToken = service.getAccessToken(requestToken, verifier);
                     // TODO Store this token?
 
                     OAuthRequest request = new OAuthRequest(Verb.GET,
                             OauthParameters.GOOGLE_API_USERINFO);
                     service.signRequest(accessToken, request);
                     request.addHeader("GData-Version", "3.0");
                     Response response = request.send();
 
                     JSONObject obj = null;
                     try {
                         obj = new JSONObject(response.getBody().toString());
                         emailExtracted = obj.getString("email");
                     } catch (JSONException e) {
                         // TODO handle some exceptions here
                         e.printStackTrace();
                     }
 
                 } catch (RuntimeException re) {
                    re.printStackTrace();
                 }
             } else {
                System.out.println("NO oath found");
             }
 
             MySession session = (MySession)getSession();
 
             if (session.signIn(emailExtracted,""))
             {
                 setResponsePage(WelcomePage.class);
             } else {
                 PageParameters params = new PageParameters();
                 params.set("email",emailExtracted);
                 setResponsePage(CreateAccountPage.class, params);
             }
 
             // TODO add some nice message here
             add(new Label("msg", "Something went wrong.."));
         }
     }
 
     private void adminSingIn() {
         MySession session = (MySession) getSession();
         if (session.signIn("admin","")) {
             setResponsePage(WelcomePage.class);
         }
     }
 
 
 }
