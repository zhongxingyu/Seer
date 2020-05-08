 package pl.psnc.dl.wf4ever.portal;
 
 import java.net.URL;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.Session;
 import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
 import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
 import org.apache.wicket.authroles.authorization.strategies.role.annotations.AnnotationsRoleAuthorizationStrategy;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.request.Request;
 import org.apache.wicket.request.Response;
 
 import pl.psnc.dl.wf4ever.portal.pages.AuthenticatePage;
 import pl.psnc.dl.wf4ever.portal.pages.ContactPage;
 import pl.psnc.dl.wf4ever.portal.pages.ErrorPage;
 import pl.psnc.dl.wf4ever.portal.pages.HomePage;
 import pl.psnc.dl.wf4ever.portal.pages.MyExpAuthorizePage;
 import pl.psnc.dl.wf4ever.portal.pages.MyExpImportPage;
 import pl.psnc.dl.wf4ever.portal.pages.MyRosPage;
 import pl.psnc.dl.wf4ever.portal.pages.OAuthPage;
 import pl.psnc.dl.wf4ever.portal.pages.RoPage;
 import pl.psnc.dl.wf4ever.portal.pages.SparqlEndpointPage;
 import pl.psnc.dl.wf4ever.portal.services.DlibraApi;
 
 /**
  * Application object for your web application. If you want to run this application
  * without deploying, run the Start class.
  * 
  * @see pl.psnc.dl.wf4ever.portal.Start#main(String[])
  */
 public class PortalApplication
 	extends AuthenticatedWebApplication
 {
 
 	private static final Logger log = Logger.getLogger(PortalApplication.class);
 
 	private String dLibraClientId;
 
 	private String myExpConsumerKey;
 
 	private String myExpConsumerSecret;
 
 	private String callbackURL;
 
 	private URL sparqlEndpointURL;
 
 
 	/**
 	 * @see org.apache.wicket.Application#getHomePage()
 	 */
 	@Override
 	public Class< ? extends WebPage> getHomePage()
 	{
 		return HomePage.class;
 	}
 
 
 	/**
 	 * @see org.apache.wicket.Application#init()
 	 */
 	@Override
 	public void init()
 	{
 		super.init();
 
 		getSecuritySettings().setAuthorizationStrategy(new AnnotationsRoleAuthorizationStrategy(this));
 
 		mountPage("/wickethome", WicketHomePage.class);
 		mountPage("/home", HomePage.class);
 		mountPage("/myros", MyRosPage.class);
 		mountPage("/ro", RoPage.class);
 		mountPage("/sparql", SparqlEndpointPage.class);
 		mountPage("/myexpimport", MyExpImportPage.class);
 		mountPage("/myexpauthorize", MyExpAuthorizePage.class);
 		mountPage("/oauth", OAuthPage.class);
 		mountPage("/authenticate", AuthenticatePage.class);
 		mountPage("/error", ErrorPage.class);
 		mountPage("/contact", ContactPage.class);
 
 		loadTokens("tokens.properties");
 		loadProperties("portal.properties");
 	}
 
 
 	@Override
 	public final Session newSession(Request request, Response response)
 	{
 		return new MySession(request);
 	}
 
 
 	private void loadProperties(String propertiesFile)
 	{
 		Properties props = new Properties();
 		try {
 			props.load(getClass().getClassLoader().getResourceAsStream(propertiesFile));
 			sparqlEndpointURL = new URL(props.getProperty("sparqlEndpointURL"));
 		}
 		catch (Exception e) {
 			log.error("Failed to load properties: " + e.getMessage());
 		}
 	}
 
 	private void loadTokens(String propertiesFile)
 	{
 		Properties props = new Properties();
 		try {
 			props.load(getClass().getClassLoader().getResourceAsStream(propertiesFile));
 			myExpConsumerKey = props.getProperty("myExpConsumerKey");
 			myExpConsumerSecret = props.getProperty("myExpConsumerSecret");
 			dLibraClientId = props.getProperty("dLibraClientId");
 			callbackURL = props.getProperty("callbackURL");
 
 			AuthenticatePage.setAuthorizationURL(DlibraApi.getOAuthService(getdLibraClientId(), getCallbackURL())
 					.getAuthorizationUrl(null));
 		}
 		catch (Exception e) {
 			log.error("Failed to load tokens: " + e.getMessage());
 		}
 	}
 
 
 	/**
 	 * @return the dLibraClientId
 	 */
 	public String getdLibraClientId()
 	{
 		return dLibraClientId;
 	}
 
 
 	/**
 	 * @return the myExpConsumerKey
 	 */
 	public String getMyExpConsumerKey()
 	{
 		return myExpConsumerKey;
 	}
 
 
 	/**
 	 * @return the myExpConsumerSecret
 	 */
 	public String getMyExpConsumerSecret()
 	{
 		return myExpConsumerSecret;
 	}
 
 
 	/**
 	 * @return the callbackURL
 	 */
 	public String getCallbackURL()
 	{
 		return callbackURL;
 	}
 
 
 	@Override
 	protected Class< ? extends WebPage> getSignInPageClass()
 	{
 		return AuthenticatePage.class;
 	}
 
 
 	@Override
 	protected Class< ? extends AbstractAuthenticatedWebSession> getWebSessionClass()
 	{
 		return MySession.class;
 	}
 
 
 	/**
 	 * @return the sparqlEndpoint
 	 */
 	public URL getSparqlEndpointURL()
 	{
 		return sparqlEndpointURL;
 	}
 
 
 	/**
 	 * @param sparqlEndpoint
 	 *            the sparqlEndpoint to set
 	 */
 	public void setSparqlEndpointURL(URL sparqlEndpoint)
 	{
 		this.sparqlEndpointURL = sparqlEndpoint;
 	}
 
 }
