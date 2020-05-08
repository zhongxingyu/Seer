 package pl.psnc.dl.wf4ever.integration;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.UUID;
 
 import javax.ws.rs.core.UriBuilder;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.joda.time.DateTime;
 import org.joda.time.format.ISODateTimeFormat;
 
 import pl.psnc.dl.wf4ever.vocabulary.NotificationService;
 
 import com.damnhandy.uri.template.UriTemplate;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.util.FileManager;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.test.framework.JerseyTest;
 import com.sun.jersey.test.framework.WebAppDescriptor;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.io.FeedException;
 import com.sun.syndication.io.SyndFeedInput;
 import com.sun.syndication.io.XmlReader;
 
 public abstract class AbstractIntegrationTest extends JerseyTest {
 
 	protected WebResource webResource;
 
 	/** admin credentials. */
 	// FIXME this shouldn't be hardcoded
 	protected final String adminCreds = StringUtils.trim(Base64
 			.encodeBase64String("wfadmin:wfadmin!!!".getBytes()));
 
 	/** client name used when registering a client. */
 	public static final String CLIENT_NAME = "ROSRS testing app written in Ruby";
 
 	/** redirection URI used when registering the client. */
 	public static final String CLIENT_REDIRECTION_URI = "OOB"; // will not be
 																// used
 
 	/** A sample access token generated before every test. */
 	protected String accessToken;
 
 	/** A sample client ID generated before every test. */
 	protected String clientId;
 
 	/** A sample user ID generated before every test. */
 	protected final String userId = "http://" + UUID.randomUUID().toString();
 
 	protected static final String PROJECT_PATH = System.getProperty("user.dir");
 
 	public AbstractIntegrationTest() {
 		super(new WebAppDescriptor.Builder("pl.psnc.dl.wf4ever").build());
 	}
 
 	@Override
 	public void setUp() throws Exception {
 		super.setUp();
 		client().setFollowRedirects(true);
 		if (resource().getURI().getHost().equals("localhost")) {
 			webResource = resource();
 		} else {
 			webResource = resource().path("rodl/");
 		}
 		ClientResponse response = createClient(CLIENT_NAME);
 		clientId = response.getLocation().resolve(".")
 				.relativize(response.getLocation()).toString();
 		response = createUser(userId, "test user");
 		response = createAccessToken(clientId, userId);
 		accessToken = response.getLocation().resolve(".")
 				.relativize(response.getLocation()).toString();
 	}
 
 	@Override
 	public void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	/**
 	 * Create an OAuth client.
 	 * 
 	 * @param clientName
 	 *            nice name
 	 * @return server response
 	 */
 	protected ClientResponse createClient(String clientName) {
 		return webResource
 				.path("clients/")
 				.header("Authorization", "Bearer " + adminCreds)
 				.post(ClientResponse.class,
 						clientName + "\r\n" + CLIENT_REDIRECTION_URI);
 	}
 
 	/**
 	 * Create an OAuth user.
 	 * 
 	 * @param userId
 	 *            id
 	 * @param username
 	 *            nice name
 	 * @return server response
 	 */
 	protected ClientResponse createUser(String userId, String username) {
 		String userIdEncoded = StringUtils.trim(Base64
 				.encodeBase64URLSafeString(userId.getBytes()));
 		return webResource.path("users/" + userIdEncoded)
 				.header("Authorization", "Bearer " + adminCreds)
 				.put(ClientResponse.class, username);
 	}
 
 	/**
 	 * Create an access token.
 	 * 
 	 * @param clientId
 	 *            client id
 	 * @param userId
 	 *            user id
 	 * @return server response
 	 */
 	protected ClientResponse createAccessToken(String clientId, String userId) {
 		return webResource.path("accesstokens/")
 				.header("Authorization", "Bearer " + adminCreds)
 				.post(ClientResponse.class, clientId + "\r\n" + userId);
 	}
 
 	protected URI createRO() {
 		return createRO(accessToken);
 	}
 
 	protected URI createRO(String token) {
 		String uuid = UUID.randomUUID().toString();
 		ClientResponse response = webResource.path("ROs/")
 				.header("Authorization", "Bearer " + token)
 				.header("Slug", uuid).post(ClientResponse.class);
 		URI ro = response.getLocation();
 		response.close();
 		return ro;
 	}
 
 	protected ClientResponse addFile(URI roURI, String filePath,
 			InputStream is, String mimeType, String token) {
 		return webResource.uri(roURI).header("Slug", filePath)
 				.header("Authorization", "Bearer " + token).type(mimeType)
 				.post(ClientResponse.class, is);
 	}
 
 	protected ClientResponse addFile(URI roURI, String filePath,
 			InputStream is, String mimeType) {
		return addFile(roURI, filePath, is, mimeType, adminCreds);
 	}
 
 	protected ClientResponse addLoremIpsumFile(URI roURI, String filePath) {
 		return addFile(roURI, filePath, IOUtils.toInputStream("lorem ipsum"),
 				"text/plain");
 	}
 
 	/**
 	 * Add an external resource to the RO.
 	 * 
 	 * @param roURI
 	 *            RO URI
 	 * @param resourceUri
 	 *            resource URI
 	 * @return server response
 	 */
 	protected ClientResponse addFile(URI roURI, URI resourceUri) {
 		return webResource
 				.uri(roURI)
 				.header("Authorization", "Bearer " + accessToken)
 				.type("application/vnd.wf4ever.proxy")
 				.post(ClientResponse.class,
 						"<rdf:RDF\n"
 								+ "   xmlns:ore=\"http://www.openarchives.org/ore/terms/\"\n"
 								+ "   xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" >\n"
 								+ "   <ore:Proxy>\n"
 								+ "     <ore:proxyFor rdf:resource=\""
 								+ resourceUri + "\" />\n" + "   </ore:Proxy>\n"
 								+ " </rdf:RDF>");
 	}
 
 	protected ClientResponse updateFile(URI uri, InputStream is, String mimeType) {
 		ClientResponse response = webResource.uri(uri)
 				.header("Authorization", "Bearer " + accessToken)
 				.type(mimeType).put(ClientResponse.class, is);
 		// follow redirects doesn't work for PUT
 		while (response.getStatus() >= 300 && response.getStatus() < 400) {
 			response = webResource.uri(response.getLocation())
 					.header("Authorization", "Bearer " + accessToken)
 					.type(mimeType).put(ClientResponse.class, is);
 		}
 		return response;
 	}
 
 	protected ClientResponse delete(URI uri) {
 		return delete(uri, accessToken);
 	}
 
 	protected ClientResponse delete(URI uri, String creds) {
 		return webResource.uri(uri).header("Authorization", "Bearer " + creds)
 				.delete(ClientResponse.class);
 	}
 
 	/**
 	 * Add an annotation with an annotation body. The annotation will annotate
 	 * the RO.
 	 * 
 	 * @param is
 	 *            annotation body in Turtle format
 	 * @param roURI
 	 *            RO URI
 	 * @param annotationBodyPath
 	 *            path of the annotation body
 	 * @return response from the server
 	 */
 	protected ClientResponse addAnnotation(URI roURI,
 			String annotationBodyPath, InputStream is) {
 		return webResource
 				.uri(roURI)
 				.header("Slug", annotationBodyPath)
 				.header("Link",
 						"<"
 								+ webResource.uri(roURI).getURI().toString()
 								+ ">; rel=\"http://purl.org/ao/annotatesResource\"")
 				.header("Authorization", "Bearer " + accessToken)
 				.type("application/x-turtle").post(ClientResponse.class, is);
 	}
 
 	/**
 	 * Add an annotation without the body.
 	 * 
 	 * @param is
 	 *            annotation description
 	 * @param roURI
 	 *            RO URI
 	 * @return response from the server
 	 */
 	protected ClientResponse addAnnotation(URI roURI, InputStream is) {
 		return webResource.uri(roURI)
 				.header("Authorization", "Bearer " + accessToken)
 				.type("application/vnd.wf4ever.annotation")
 				.post(ClientResponse.class, is);
 	}
 
 	/**
 	 * Add a folder to the RO.
 	 * 
 	 * @param is
 	 *            request entity
 	 * @param roURI
 	 *            RO URI
 	 * @param folderPath
 	 *            folder path
 	 * @return response from server
 	 */
 	protected ClientResponse addFolder(URI roURI, String folderPath,
 			InputStream is) {
 		return webResource.uri(roURI).header("Slug", folderPath)
 				.header("Authorization", "Bearer " + accessToken)
 				.type("application/vnd.wf4ever.folder")
 				.post(ClientResponse.class, is);
 	}
 
 	/**
 	 * Add a folder entry to a folder.
 	 * 
 	 * @param is
 	 *            request entity
 	 * @param folderUri
 	 *            URI of the folder to which the entry will be added
 	 * @return response from server
 	 */
 	protected ClientResponse addFolderEntry(URI folderUri, InputStream is) {
 		return webResource.uri(folderUri)
 				.header("Authorization", "Bearer " + accessToken)
 				.type("application/vnd.wf4ever.folderentry")
 				.post(ClientResponse.class, is);
 	}
 
 	/**
 	 * Get notifications.
 	 * 
 	 * @param testUri
 	 *            RO URI
 	 * @param from
 	 *            optional start point
 	 * @return a feed
 	 * @throws FeedException
 	 *             can't load the feed
 	 * @throws IOException
 	 *             can't load the feed
 	 */
 	protected SyndFeed getNotifications(URI testUri, DateTime from)
 			throws FeedException, IOException {
 		Model model = FileManager.get().loadModel(
 				webResource.getURI().toString());
 		Resource serviceResource = model.getResource(webResource.getURI()
 				.toString());
 		String notificationsUriTemplateString = serviceResource
 				.listProperties(NotificationService.notifications).next()
 				.getObject().asLiteral().getString();
 		UriTemplate uriTemplate = UriTemplate
 				.fromTemplate(notificationsUriTemplateString);
 		uriTemplate = uriTemplate.set("ro", testUri.toString());
 		if (from != null) {
 			uriTemplate = uriTemplate.set("from", ISODateTimeFormat.dateTime()
 					.print(from));
 		}
 		URI notificationsUri = UriBuilder.fromUri(uriTemplate.expand()).build();
 		SyndFeedInput input = new SyndFeedInput();
 		SyndFeed feed = input.build(new XmlReader(notificationsUri.toURL()));
 		return feed;
 	}
 
 }
