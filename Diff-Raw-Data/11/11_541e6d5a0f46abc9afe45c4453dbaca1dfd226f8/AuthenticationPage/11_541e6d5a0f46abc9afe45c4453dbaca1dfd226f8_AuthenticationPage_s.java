 package pl.psnc.dl.wf4ever.portal.pages.users;
 
 import java.net.URI;
 
 import org.apache.wicket.markup.html.form.Button;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.RequiredTextField;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.request.IRequestHandler;
 import org.apache.wicket.request.Url;
 import org.apache.wicket.request.cycle.RequestCycle;
 import org.apache.wicket.request.http.handler.RedirectRequestHandler;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.openid4java.discovery.DiscoveryInformation;
 import org.openid4java.message.AuthRequest;
 
 import pl.psnc.dl.wf4ever.portal.MySession;
 import pl.psnc.dl.wf4ever.portal.model.users.OpenIdUser;
 import pl.psnc.dl.wf4ever.portal.pages.TemplatePage;
 import pl.psnc.dl.wf4ever.portal.services.OpenIdService;
 
 /**
  * The page where the user inputs his OpenID to log in. The page saves the callback URL in the session and redirects the
  * user to his OpenID provider.
  * 
  * @author Piotr Hołubowicz
  * 
  */
 public class AuthenticationPage extends TemplatePage {
 
     /** id. */
     private static final long serialVersionUID = -8975579933617712699L;
 
     /** Google OP. */
     private static final String GOOGLE_URL = "https://www.google.com/accounts/o8/id";
 
    /** Yahoo OP. */
    private static final String YAHOO_URL = "http://yahoo.com";

 
     /**
      * Constructor.
      * 
      * @param pageParameters
      *            page parameters
      */
     @SuppressWarnings("serial")
     public AuthenticationPage(PageParameters pageParameters) {
         super(pageParameters);
 
         final URI callbackUrl = createCallbackUrl();
         ((MySession) getSession()).setOpenIDCallbackURI(callbackUrl);
 
         final OpenIdUser tempUser = new OpenIdUser();
 
         Form<OpenIdUser> form = new Form<OpenIdUser>("openIdForm", new CompoundPropertyModel<OpenIdUser>(tempUser)) {
 
             @Override
             protected void onSubmit() {
                 super.onSubmit();
                 applyForAuthentication(tempUser.getOpenId(), callbackUrl);
             }
         };
         add(form);
         TextField<String> openId = new RequiredTextField<String>("openId");
         openId.setLabel(new Model<String>("Your Open ID"));
         form.add(openId);
         form.add(new Button("confirmOpenIdButton"));
 
         Form<?> form2 = new Form<Void>("form");
         add(form2);
         form2.add(new Link<String>("logInWithGoogle") {
 
             @Override
             public void onClick() {
                 applyForAuthentication(GOOGLE_URL, callbackUrl);
             }
         });
        form2.add(new Link<String>("logInWithYahoo") {

            @Override
            public void onClick() {
                applyForAuthentication(YAHOO_URL, callbackUrl);
            }
        });
 
     }
 
 
     /**
      * Prepare the request to the OP.
      * 
      * @param userSuppliedIdentifier
      *            user openID
      * @param callbackUrl
      *            the callback in this application
      */
     public void applyForAuthentication(String userSuppliedIdentifier, URI callbackUrl) {
         DiscoveryInformation discoveryInformation = OpenIdService
                 .performDiscoveryOnUserSuppliedIdentifier(userSuppliedIdentifier);
         // Store the discovery results in session.
         MySession session = (MySession) getSession();
         session.setDiscoveryInformation(discoveryInformation);
         // Create the AuthRequest
         AuthRequest authRequest = OpenIdService.createOpenIdAuthRequest(discoveryInformation, callbackUrl.toString());
         // Now take the AuthRequest and forward it on to the OP
         IRequestHandler reqHandler = new RedirectRequestHandler(authRequest.getDestinationUrl(true));
         getRequestCycle().scheduleRequestHandlerAfterCurrent(reqHandler);
     }
 
 
     /**
      * Generates the returnToUrl parameter that is passed to the OP. The User Agent (i.e., the browser) will be directed
      * to this page following authentication.
      * 
      * @return the absolute URI pointing to this application's OpenID endpoint
      */
     private URI createCallbackUrl() {
         // FIXME replaceAll because "../" gets inserted, don't know why
         return URI.create(RequestCycle.get().getUrlRenderer()
                 .renderFullUrl(Url.parse(urlFor(OpenIDEndpoint.class, null).toString())).replaceAll("\\.\\./", ""));
     }
 
 }
