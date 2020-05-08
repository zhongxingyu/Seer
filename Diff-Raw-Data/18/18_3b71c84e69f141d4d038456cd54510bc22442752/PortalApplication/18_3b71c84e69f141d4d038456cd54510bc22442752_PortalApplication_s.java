 package pl.psnc.dl.wf4ever.portal;
 
 import java.net.URI;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.Session;
 import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
 import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
 import org.apache.wicket.authroles.authorization.strategies.role.annotations.AnnotationsRoleAuthorizationStrategy;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.request.Request;
 import org.apache.wicket.request.Response;
 
 import pl.psnc.dl.wf4ever.portal.model.ResourceGroup;
 import pl.psnc.dl.wf4ever.portal.pages.AllRosPage;
 import pl.psnc.dl.wf4ever.portal.pages.AuthenticatePage;
 import pl.psnc.dl.wf4ever.portal.pages.ContactPage;
 import pl.psnc.dl.wf4ever.portal.pages.ErrorPage;
 import pl.psnc.dl.wf4ever.portal.pages.HelpPage;
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
 
 	private URI rodlURI;
 
 	private URL sparqlEndpointURL;
 
 	private URL searchEndpointURL;
 
 	private URL recommenderEndpointURL;
 
 	private URL stabilityEndpointURL;
 
 	private URL userAccessTokenEndpointURL;
 
 	private URL userAuthorizationEndpointURL;
 
 	private final Set<ResourceGroup> resourceGroups = new HashSet<>();
 
 
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
 		mountPage("/allros", AllRosPage.class);
 		mountPage("/ro", RoPage.class);
 		mountPage("/sparql", SparqlEndpointPage.class);
 		mountPage("/myexpimport", MyExpImportPage.class);
 		mountPage("/myexpauthorize", MyExpAuthorizePage.class);
 		mountPage("/oauth", OAuthPage.class);
 		mountPage("/authenticate", AuthenticatePage.class);
 		mountPage("/error", ErrorPage.class);
 		mountPage("/contact", ContactPage.class);
 		mountPage("/help", HelpPage.class);
 
		loadTokens("tokens.properties");
 		loadProperties("portal.properties");
 		loadResourceGroups("resourceGroups.properties");
 
 		Locale.setDefault(Locale.ENGLISH);
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
 			rodlURI = new URI(props.getProperty("rodlURL"));
 			sparqlEndpointURL = new URL(props.getProperty("sparqlEndpointURL"));
 			searchEndpointURL = new URL(props.getProperty("searchEndpointURL"));
 			recommenderEndpointURL = new URL(props.getProperty("recommenderEndpointURL"));
 			stabilityEndpointURL = new URL(props.getProperty("stabilityEndpointURL"));
 			userAccessTokenEndpointURL = new URL(props.getProperty("userAccessTokenEndpointURL"));
 			userAuthorizationEndpointURL = new URL(props.getProperty("userAuthorizationEndpointURL"));
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
 
 
 	private void loadResourceGroups(String propertiesFile)
 	{
 		Properties props = new Properties();
 		try {
 			props.load(getClass().getClassLoader().getResourceAsStream(propertiesFile));
 			Set<String> entries = props.stringPropertyNames();
 			Map<String, ResourceGroup> groups = new HashMap<>();
 			for (String entry : entries) {
 				if (entry.endsWith(".classes")) {
 					String[] classes = props.getProperty(entry, "").split(",");
 					for (String clazz : classes) {
 						if (!clazz.trim().isEmpty()) {
 							URI classURI = URI.create(clazz.trim());
 							if (classURI != null) {
 								String key = entry.substring(0, entry.length() - ".classes".length());
 								if (!groups.containsKey(key)) {
 									groups.put(key, new ResourceGroup(key));
 								}
 								groups.get(key).getRdfClasses().add(classURI);
 							}
 						}
 					}
 				}
 				else if (entry.endsWith(".description")) {
 					String desc = props.getProperty(entry, "");
 					String key = entry.substring(0, entry.length() - ".description".length());
 					if (!groups.containsKey(key)) {
 						groups.put(key, new ResourceGroup(key));
 					}
 					groups.get(key).setDescription(desc);
 				}
 			}
 			resourceGroups.addAll(groups.values());
 		}
 		catch (Exception e) {
 			log.error("Failed to load resourceGroups: " + e.getMessage());
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
 
 
 	public URI getRodlURI()
 	{
 		return rodlURI;
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
 
 
 	/**
 	 * @return the searchEndpointURL
 	 */
 	public URL getSearchEndpointURL()
 	{
 		return searchEndpointURL;
 	}
 
 
 	/**
 	 * @param searchEndpointURL
 	 *            the searchEndpointURL to set
 	 */
 	public void setSearchEndpointURL(URL searchEndpointURL)
 	{
 		this.searchEndpointURL = searchEndpointURL;
 	}
 
 
 	/**
 	 * @return the recommenderEndpointURL
 	 */
 	public URL getRecommenderEndpointURL()
 	{
 		return recommenderEndpointURL;
 	}
 
 
 	/**
 	 * @param recommenderEndpointURL
 	 *            the recommenderEndpointURL to set
 	 */
 	public void setRecommenderEndpointURL(URL recommenderEndpointURL)
 	{
 		this.recommenderEndpointURL = recommenderEndpointURL;
 	}
 
 
 	/**
 	 * @return the resourceGroups
 	 */
 	public Set<ResourceGroup> getResourceGroups()
 	{
 		return resourceGroups;
 	}
 
 
 	/**
 	 * @return the stabilityEndpointURL
 	 */
 	public URL getStabilityEndpointURL()
 	{
 		return stabilityEndpointURL;
 	}
 
 
 	/**
 	 * @param stabilityEndpointURL
 	 *            the stabilityEndpointURL to set
 	 */
 	public void setStabilityEndpointURL(URL stabilityEndpointURL)
 	{
 		this.stabilityEndpointURL = stabilityEndpointURL;
 	}
 
 
 	public URL getUserAccessTokenEndpointURL()
 	{
 		return userAccessTokenEndpointURL;
 	}
 
 
 	public void setUserAccessTokenEndpointURL(URL userAccessTokenEndpointURL)
 	{
 		this.userAccessTokenEndpointURL = userAccessTokenEndpointURL;
 	}
 
 
 	public URL getUserAuthorizationEndpointURL()
 	{
 		return userAuthorizationEndpointURL;
 	}
 
 
 	public void setUserAuthorizationEndpointURL(URL userAuthorizationEndpointURL)
 	{
 		this.userAuthorizationEndpointURL = userAuthorizationEndpointURL;
 	}
 
 }
