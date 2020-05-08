 package pl.psnc.dl.wf4ever.myexpimport.pages;
 
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.markup.html.form.Button;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.request.http.handler.RedirectRequestHandler;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.scribe.model.Token;
 import org.scribe.model.Verifier;
 import org.scribe.oauth.OAuthService;
 
 import pl.psnc.dl.wf4ever.myexpimport.services.DlibraApi;
 import pl.psnc.dl.wf4ever.myexpimport.services.MyExpApi;
 import pl.psnc.dl.wf4ever.myexpimport.utils.Constants;
 import pl.psnc.dl.wf4ever.myexpimport.utils.WicketUtils;
 
 /**
  * 
  * @author Piotr Hołubowicz
  *
  */
 public class HomePage
 	extends TemplatePage
 {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private static final Logger log = Logger.getLogger(HomePage.class);
 
 
 	/**
 	 * Default Constructor
 	 */
 	public HomePage()
 	{
 		this(new PageParameters());
 	}
 
 
 	@SuppressWarnings("serial")
 	public HomePage(PageParameters pageParameters)
 	{
 		super(pageParameters);
 
 		processOAuth(pageParameters);
 
 		Form< ? > form = new Form<Void>("form");
 		content.add(form);
 
 		final Button authMyExpButton = new Button("authMyExp") {
 
 			@Override
 			public void onSubmit()
 			{
 				Token at = tryLoadDlibraTestToken();
 				if (at == null) {
					startDlibraAuthorization();
 				}
 			}
 		};
 		authMyExpButton.setEnabled(getMyExpAccessToken() == null);
 		form.add(authMyExpButton).setOutputMarkupId(true);
 
 		final Button authDlibraButton = new Button("authDlibra") {
 
 			@Override
 			public void onSubmit()
 			{
 				Token at = tryLoadMyExpTestToken();
 				if (at == null) {
					startMyExpAuthorization();
 				}
 			}
 		};
 		authDlibraButton.setEnabled(getMyExpAccessToken() != null
 				&& getDlibraAccessToken() == null);
 		form.add(authDlibraButton).setOutputMarkupId(true);
 
 		final Button importButton = new Button("myExpImportButton") {
 
 			@Override
 			public void onSubmit()
 			{
 				goToPage(MyExpImportPage.class, null);
 			}
 		};
 		importButton.setEnabled(getMyExpAccessToken() != null
 				&& getDlibraAccessToken() != null);
 		form.add(importButton).setOutputMarkupId(true);
 	}
 
 
 	private void processOAuth(PageParameters pageParameters)
 	{
 		if (getMyExpAccessToken() == null) {
 			OAuthService service = MyExpApi.getOAuthService(WicketUtils
 					.getCompleteUrl(this, HomePage.class, true));
 			Token token = retrieveMyExpAccessToken(pageParameters, service);
 			setMyExpAccessToken(token);
 		}
 		else if (getDlibraAccessToken() == null) {
 			OAuthService service = DlibraApi.getOAuthService(WicketUtils
 					.getCompleteUrl(this, HomePage.class, true));
 			Token token = retrieveDlibraAccessToken(pageParameters, service);
 			setDlibraAccessToken(token);
 		}
 	}
 
 
 	protected void startDlibraAuthorization()
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 
 	protected Token tryLoadDlibraTestToken()
 	{
 		Properties props = new Properties();
 		try {
 			props.load(getClass().getClassLoader().getResourceAsStream(
 				"testToken.properties"));
 			String token = props.getProperty("dLibraToken");
 			if (token != null) {
 				return new Token(token, null);
 			}
 		}
 		catch (Exception e) {
 			log.debug("Failed to load properties: " + e.getMessage());
 		}
 		return null;
 	}
 
 
 	protected Token tryLoadMyExpTestToken()
 	{
 		Properties props = new Properties();
 		try {
 			props.load(getClass().getClassLoader().getResourceAsStream(
 				"testToken.properties"));
 			String token = props.getProperty("token");
 			String secret = props.getProperty("secret");
 			if (token != null && secret != null) {
 				return new Token(token, secret);
 			}
 		}
 		catch (Exception e) {
 			log.debug("Failed to load properties: " + e.getMessage());
 		}
 		return null;
 	}
 
 
 	private void startMyExpAuthorization()
 	{
 		String oauthCallbackURL = WicketUtils.getCompleteUrl(this,
 			HomePage.class, false);
 
 		OAuthService service = MyExpApi.getOAuthService(oauthCallbackURL);
 		Token requestToken = service.getRequestToken();
 		getSession()
 				.setAttribute(Constants.SESSION_REQUEST_TOKEN, requestToken);
 		String authorizationUrl = service.getAuthorizationUrl(requestToken);
 		log.debug("Request token: " + requestToken.toString() + " service: "
 				+ service.getAuthorizationUrl(requestToken));
 		getRequestCycle().scheduleRequestHandlerAfterCurrent(
 			new RedirectRequestHandler(authorizationUrl));
 	}
 
 
 	/**
 	 * @param pageParameters
 	 * @param service
 	 * @return
 	 */
 	private Token retrieveMyExpAccessToken(PageParameters pageParameters,
 			OAuthService service)
 	{
 		Token accessToken = null;
 		if (!pageParameters.get(MyExpApi.OAUTH_VERIFIER).isEmpty()) {
 			Verifier verifier = new Verifier(pageParameters.get(
 				MyExpApi.OAUTH_VERIFIER).toString());
 			Token requestToken = (Token) getSession().getAttribute(
 				Constants.SESSION_REQUEST_TOKEN);
 			log.debug("Request token: " + requestToken.toString()
 					+ " verifier: " + verifier.getValue() + " service: "
 					+ service.getAuthorizationUrl(requestToken));
 			accessToken = service.getAccessToken(requestToken, verifier);
 		}
 		return accessToken;
 	}
 
 
 	/**
 	 * @param pageParameters
 	 * @param service
 	 * @return
 	 */
 	private Token retrieveDlibraAccessToken(PageParameters pageParameters,
 			OAuthService service)
 	{
 		Token accessToken = null;
 		return accessToken;
 	}
 
 }
