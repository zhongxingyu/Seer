 package edu.msu.nscl.olog.api;
 
 import static edu.msu.nscl.olog.api.LogBuilder.log;
 import static edu.msu.nscl.olog.api.PropertyBuilder.property;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.URI;
 import java.security.KeyManagementException;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.imageio.ImageIO;
 import javax.net.ssl.HostnameVerifier;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLSession;
 import javax.net.ssl.TrustManager;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.UriBuilder;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.UniformInterfaceException;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
 import com.sun.jersey.client.urlconnection.HTTPSProperties;
 import com.sun.jersey.core.util.MultivaluedMapImpl;
 
 import com.sun.jersey.client.apache.ApacheHttpClient;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import net.coobird.thumbnailator.Thumbnails;
 import org.apache.commons.httpclient.Credentials;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
 import org.apache.commons.httpclient.methods.RequestEntity;
 import org.apache.jackrabbit.webdav.DavConstants;
 import org.apache.jackrabbit.webdav.DavException;
 import org.apache.jackrabbit.webdav.MultiStatus;
 import org.apache.jackrabbit.webdav.MultiStatusResponse;
 import org.apache.jackrabbit.webdav.client.methods.DavMethod;
 import org.apache.jackrabbit.webdav.client.methods.MkColMethod;
 import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
 import org.apache.jackrabbit.webdav.client.methods.PutMethod;
 
 /**
  * 
  * TODO: replace the usage of Xml* types with log,tag,logbooks
  * 
  * @author Eric Berryman taken from shroffk
  * 
  */
 public class OlogClientImpl implements OlogClient {
 	private final WebResource service;
 	private final HttpClient webdav;
 	private final ExecutorService executor;
 	private final URI ologJCRBaseURI;
 
 	/**
 	 * Builder Class to help create a olog client.
 	 * 
 	 * @author shroffk
 	 * 
 	 */
 	public static class OlogClientBuilder {
 		// required
 		private URI ologURI = null;
 
 		private URI ologJCRURI;
 
 		// optional
 		private boolean withHTTPAuthentication = false;
 
 		private ClientConfig clientConfig = null;
 		private TrustManager[] trustManager = new TrustManager[] { new DummyX509TrustManager() };;
 		@SuppressWarnings("unused")
 		private SSLContext sslContext = null;
 
 		private String protocol = null;
 		private String username = null;
 		private String password = null;
 
 		private ExecutorService executor = Executors.newSingleThreadExecutor();
 
 		private OlogProperties properties = new OlogProperties();
 
 		private static final String DEFAULT_OLOG_URL = "http://localhost:8080/Olog/resources"; //$NON-NLS-1$
 		private static final String DEFAULT_OLOG_JCR_URL = "http://localhost:8080/Olog/repository";
 
 		private OlogClientBuilder() {
 			this.ologURI = URI.create(this.properties.getPreferenceValue(
 					"olog_url", DEFAULT_OLOG_URL));
 			this.ologJCRURI = URI.create(this.properties.getPreferenceValue(
 					"olog_jcr_url", DEFAULT_OLOG_JCR_URL));
 			this.protocol = this.ologURI.getScheme();
 		}
 
 		private OlogClientBuilder(URI uri) {
 			this.ologURI = uri;
 			this.protocol = this.ologURI.getScheme();
 		}
 
 		/**
 		 * Creates a {@link OlogClientBuilder} for a CF client to Default URL in
 		 * the channelfinder.properties.
 		 * 
 		 * @return
 		 */
 		public static OlogClientBuilder serviceURL() {
 			return new OlogClientBuilder();
 		}
 
 		/**
 		 * Creates a {@link OlogClientBuilder} for a CF client to URI
 		 * <tt>uri</tt>.
 		 * 
 		 * @param uri
 		 * @return {@link OlogClientBuilder}
 		 */
 		public static OlogClientBuilder serviceURL(String uri) {
 			return new OlogClientBuilder(URI.create(uri));
 		}
 
 		/**
 		 * Creates a {@link OlogClientBuilder} for a CF client to {@link URI}
 		 * <tt>uri</tt>.
 		 * 
 		 * @param uri
 		 * @return {@link OlogClientBuilder}
 		 */
 		public static OlogClientBuilder serviceURL(URI uri) {
 			return new OlogClientBuilder(uri);
 		}
 
 		/**
 		 * Set the jcr url to be used for the attachment repository.
 		 * 
 		 * @param username
 		 * @return {@link OlogClientBuilder}
 		 */
 		public OlogClientBuilder jcrURI(URI jcrURI) {
 			this.ologJCRURI = jcrURI;
 			return this;
 		}
 
 		/**
 		 * Set the jcr url to be used for the attachment repository.
 		 * 
 		 * @param username
 		 * @return {@link OlogClientBuilder}
 		 */
 		public OlogClientBuilder jcrURI(String jcrURI) {
 			this.ologJCRURI = UriBuilder.fromUri(jcrURI).build();
 			return this;
 		}
 
 		/**
 		 * Enable of Disable the HTTP authentication on the client connection.
 		 * 
 		 * @param withHTTPAuthentication
 		 * @return {@link OlogClientBuilder}
 		 */
 		public OlogClientBuilder withHTTPAuthentication(
 				boolean withHTTPAuthentication) {
 			this.withHTTPAuthentication = withHTTPAuthentication;
 			return this;
 		}
 
 		/**
 		 * Set the username to be used for HTTP Authentication.
 		 * 
 		 * @param username
 		 * @return {@link OlogClientBuilder}
 		 */
 		public OlogClientBuilder username(String username) {
 			this.username = username;
 			return this;
 		}
 
 		/**
 		 * Set the password to be used for the HTTP Authentication.
 		 * 
 		 * @param password
 		 * @return {@link OlogClientBuilder}
 		 */
 		public OlogClientBuilder password(String password) {
 			this.password = password;
 			return this;
 		}
 
 		/**
 		 * set the {@link ClientConfig} to be used while creating the
 		 * channelfinder client connection.
 		 * 
 		 * @param clientConfig
 		 * @return {@link OlogClientBuilder}
 		 */
 		public OlogClientBuilder withClientConfig(ClientConfig clientConfig) {
 			this.clientConfig = clientConfig;
 			return this;
 		}
 
 		@SuppressWarnings("unused")
 		private OlogClientBuilder withSSLContext(SSLContext sslContext) {
 			this.sslContext = sslContext;
 			return this;
 		}
 
 		/**
 		 * Set the trustManager that should be used for authentication.
 		 * 
 		 * @param trustManager
 		 * @return {@link OlogClientBuilder}
 		 */
 		public OlogClientBuilder withTrustManager(TrustManager[] trustManager) {
 			this.trustManager = trustManager;
 			return this;
 		}
 
 		/**
 		 * Provide your own executor on which the queries are to be made. <br>
 		 * By default a single threaded executor is used.
 		 * 
 		 * @param executor
 		 * @return {@link OlogClientBuilder}
 		 */
 		public OlogClientBuilder withExecutor(ExecutorService executor) {
 			this.executor = executor;
 			return this;
 		}
 
 		public OlogClientImpl create() {
 			if (this.protocol.equalsIgnoreCase("http")) { //$NON-NLS-1$
 				this.clientConfig = new DefaultClientConfig();
 			} else if (this.protocol.equalsIgnoreCase("https")) { //$NON-NLS-1$
 				if (this.clientConfig == null) {
 					SSLContext sslContext = null;
 					try {
 						sslContext = SSLContext.getInstance("SSL"); //$NON-NLS-1$
 						sslContext.init(null, this.trustManager, null);
 					} catch (NoSuchAlgorithmException e) {
 						throw new OlogException();
 					} catch (KeyManagementException e) {
 						throw new OlogException();
 					}
 					this.clientConfig = new DefaultClientConfig();
 					this.clientConfig.getProperties().put(
 							HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
 							new HTTPSProperties(new HostnameVerifier() {
 								@Override
 								public boolean verify(String hostname,
 										SSLSession session) {
 									return true;
 								}
 							}, sslContext));
 				}
 				this.username = ifNullReturnPreferenceValue(this.username,
 						"username", "username");
 				this.password = ifNullReturnPreferenceValue(this.password,
 						"password", "password");
 			}
 			return new OlogClientImpl(this.ologURI, this.ologJCRURI,
 					this.clientConfig, this.withHTTPAuthentication,
 					this.username, this.password, this.executor);
 		}
 
 		private String ifNullReturnPreferenceValue(String value, String key,
 				String Default) {
 			if (value == null) {
 				return this.properties.getPreferenceValue(key, Default);
 			} else {
 				return value;
 			}
 		}
 
 	}
 
 	private OlogClientImpl(URI ologURI, URI ologJCRURI, ClientConfig config,
 			boolean withHTTPBasicAuthFilter, String username, String password,
 			ExecutorService executor) {
 		this.ologJCRBaseURI = ologJCRURI;
 		this.executor = executor;
 		Client client = Client.create(config);
 		if (withHTTPBasicAuthFilter) {
 			client.addFilter(new HTTPBasicAuthFilter(username, password));
 		}
 		// client.addFilter(new
 		// RawLoggingFilter(Logger.getLogger(RawLoggingFilter.class.getName())));
 		service = client.resource(UriBuilder.fromUri(ologURI).build());
 
 		ApacheHttpClient client2Apache = ApacheHttpClient.create(config);
 		webdav = client2Apache.getClientHandler().getHttpClient();
 		webdav.getHostConfiguration().setHost(getJCRBaseURI().getHost(), 8181);
 		Credentials credentials = new UsernamePasswordCredentials(username,
 				password);
 		webdav.getState().setCredentials(AuthScope.ANY, credentials);
 		webdav.getParams().setAuthenticationPreemptive(true);
 	}
 
 	/**
 	 * Get a list of all the logbooks currently existing
 	 * 
 	 * @return string collection of logbooks
 	 */
 	public Collection<String> getAllLogbooks() {
 		return wrappedSubmit(new GetAllResrouce("logbooks"));
 	}
 
 	/**
 	 * Get a list of all the tags currently existing
 	 * 
 	 * @return string collection of tags
 	 */
 	public Collection<String> getAllTags() {
 		return wrappedSubmit(new GetAllResrouce("tags"));
 	}
 
 	/**
 	 * Get a list of all the levels currently existing
 	 * 
 	 * @return string collection of levels
 	 */
 	@SuppressWarnings("deprecation")
 	public Collection<String> getAllLevels() {
 		return wrappedSubmit(new GetAllResrouce("levels"));
 	}
 
 	private class GetAllResrouce implements Callable<Collection<String>> {
 
 		private final String path;
 
 		public GetAllResrouce(String path) {
 			this.path = path;
 		}
 
 		@Override
 		public Collection<String> call() throws UniformInterfaceException {
 			Collection<String> allResources = new HashSet<String>();
 			if (path.equalsIgnoreCase("logbooks")) {
 				XmlLogbooks allXmlLogbooks = service.path("logbooks")
 						.accept(MediaType.APPLICATION_XML)
 						.get(XmlLogbooks.class);
 				for (XmlLogbook xmlLogbook : allXmlLogbooks.getLogbooks()) {
 					allResources.add(xmlLogbook.getName());
 				}
 			} else if (path.equalsIgnoreCase("tags")) {
 				XmlTags allXmlTags = service.path("tags")
 						.accept(MediaType.APPLICATION_XML).get(XmlTags.class);
 				for (XmlTag xmlTag : allXmlTags.getTags()) {
 					allResources.add(xmlTag.getName());
 				}
 			} else if (path.equalsIgnoreCase("levels")) {
 				XmlLevels allXmlLevels = service.path("levels")
 						.accept(MediaType.APPLICATION_XML).get(XmlLevels.class);
 				for (XmlLevel xmlLevel : allXmlLevels.getLevels()) {
 					allResources.add(xmlLevel.getName());
 				}
 			}
 			return allResources;
 		}
 	}
 
 	private URI getJCRBaseURI() {
 		return this.ologJCRBaseURI;
 	}
 
 	/**
 	 * Returns a collection of attachments that matches the logId <tt>logId</tt>
 	 * 
 	 * @param logId
 	 *            log id
 	 * @return attachments collection object
 	 * @throws OlogException
 	 */
 	public Collection<String> getAttachments(Long logId) throws OlogException,
 			DavException {
 		Collection<String> allFiles = new HashSet<String>();
 		try {
 			URI remote = UriBuilder.fromUri(getJCRBaseURI()).path("{arg1}/")
 					.build(logId);
 			DavMethod pFind = new PropFindMethod(remote.toASCIIString(),
 					DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);
 			webdav.executeMethod(pFind);
 			MultiStatus multiStatus = pFind.getResponseBodyAsMultiStatus();
 			MultiStatusResponse[] responses = multiStatus.getResponses();
 			MultiStatusResponse currResponse;
 
 			for (int i = 0; i < responses.length; i++) {
 				currResponse = responses[i];
 				if (!currResponse.getHref().endsWith("/")) {
 					allFiles.add(currResponse.getHref());
 				}
 			}
 			pFind.releaseConnection();
 			return allFiles;
 		} catch (UniformInterfaceException e) {
 			throw new OlogException(e);
 		} catch (IOException e) {
 			throw new OlogException(e);
 		}
 	}
 
 	/**
 	 * Returns a log that exactly matches the logId <tt>logId</tt>
 	 * 
 	 * @param logId
 	 *            log id
 	 * @return Log object
 	 * @throws OlogException
 	 */
 	public Log getLog(Long logId) throws OlogException {
 		return wrappedSubmit(new GetLog(logId));
 	}
 
 	private class GetLog implements Callable<Log> {
 
 		private final Long logId;
 
 		public GetLog(Long logId) {
 			this.logId = logId;
 		}
 
 		@Override
 		public Log call() throws OlogException {
 			try {
 				return new Log(service
 						.path("logs").path(logId.toString()).accept( //$NON-NLS-1$
 								MediaType.APPLICATION_XML).get(XmlLog.class));
 			} catch (UniformInterfaceException e) {
 				throw new OlogException(e);
 			}
 		}
 
 	}
 
 	/**
 	 * Add a single log <tt>log</tt>
 	 * 
 	 * @param log
 	 *            the log to be added
 	 * @throws OlogException
 	 */
 	public Log add(LogBuilder log) throws OlogException {
 		try {
 			ClientResponse incResponse;
 			XmlLogs xmlLogs = new XmlLogs();
 			xmlLogs.addXmlLog(log.toXml());
 
 			if (log.toXml().getId() != null) {
 				// throw new OlogException();
 				// TODO: Fail? use UpdateLog instead?
 				// service logs/id does not reply with inserted Log
 				// this doesn't seem right to just return the object given
 				service.path("logs").path(log.toXml().getId().toString()).accept( //$NON-NLS-1$
 								MediaType.APPLICATION_XML).type( //$NON-NLS-1$
 								MediaType.APPLICATION_XML)
 						.put(ClientResponse.class, log.toXml());
 				return log.build();
 			} else {
 				incResponse = service.path("logs").accept( //$NON-NLS-1$
 						MediaType.APPLICATION_XML).type( //$NON-NLS-1$
 						MediaType.APPLICATION_XML)
 						.post(ClientResponse.class, xmlLogs);
 			}
 			XmlLogs response = incResponse.getEntity(XmlLogs.class);
 			return new Log(response.getLogs().iterator().next());
 		} catch (UniformInterfaceException e) {
 			throw new OlogException(e);
 		}
 	}
 
 	/**
 	 * Add a set of logs
 	 * 
 	 * @param logs
 	 *            set of logs to be added
 	 * @throws OlogException
 	 */
 	public Collection<Log> add(Collection<LogBuilder> logs)
 			throws OlogException {
 		try {
 			XmlLogs xmlLogs = new XmlLogs();
 			Collection<Log> returnLogs = new HashSet<Log>();
 
 			for (LogBuilder log : logs) {
 				xmlLogs.addXmlLog(log.toXml());
 			}
 			ClientResponse incResponse = service.path("logs").accept( //$NON-NLS-1$
 					MediaType.APPLICATION_XML).type( //$NON-NLS-1$
 					MediaType.APPLICATION_XML)
 					.post(ClientResponse.class, xmlLogs);
 			XmlLogs response = incResponse.getEntity(XmlLogs.class);
 			for (XmlLog xmllog : response.getLogs()) {
 				returnLogs.add(new Log(xmllog));
 			}
 			return Collections.unmodifiableCollection(returnLogs);
 		} catch (UniformInterfaceException e) {
 			throw new OlogException(e);
 		}
 	}
 
 	/**
 	 * Add a Tag <tt>tag</tt> with no associated logs to the database.
 	 * 
 	 * @param tag
 	 */
 	public void add(TagBuilder tag) {
 		try {
 			XmlTag xmlTag = tag.toXml();
 			service.path("tags").path(xmlTag.getName())
 					.accept(MediaType.APPLICATION_XML)
 					.accept(MediaType.APPLICATION_JSON).put(xmlTag);
 		} catch (UniformInterfaceException e) {
 			throw new OlogException(e);
 		}
 	}
 
 	/**
 	 * Add Tag <tt>tag </tt> to Log with name <tt>logName</tt>
 	 * 
 	 * @param tag
 	 *            tag builder
 	 * @param logId
 	 *            log id the tag to be added
 	 */
 	public void add(TagBuilder tag, Long logId) {
 		wrappedSubmit(new AddResource<TagBuilder>(tag, logId));
 	}
 
 	/**
 	 * Add the Tag <tt>tag</tt> to the set of the logs with ids <tt>logIds</tt>
 	 * 
 	 * @param tag
 	 *            tag builder
 	 * @param logIds
 	 *            collection of log ids
 	 */
 	public void add(TagBuilder tag, Collection<Long> logIds) {
 		wrappedSubmit(new AddResource<TagBuilder>(tag, logIds));
 	}
 
 	/**
 	 * Add a new logbook <tt>logbook</tt>
 	 * 
 	 * @param logbookBuilder
 	 */
 	public void add(LogbookBuilder logbookBuilder) {
 		try {
 			XmlLogbook logbook = logbookBuilder.toXml();
 			service.path("logbooks").path(logbook.getName())
 					.accept(MediaType.APPLICATION_XML)
 					.accept(MediaType.APPLICATION_JSON).put(logbook);
 		} catch (UniformInterfaceException e) {
 			throw new OlogException(e);
 		}
 	}
 
 	/**
 	 * Add Logbook <tt>logbook</tt> to the log <tt>logId</tt>
 	 * 
 	 * @param logbook
 	 *            logbook builder
 	 * @param logId
 	 *            log id
 	 */
 	public void add(LogbookBuilder logbook, Long logId) {
 		wrappedSubmit(new AddResource<LogbookBuilder>(logbook, logId));
 	}
 
 	/**
 	 * @param logIds
 	 * @param logbook
 	 */
 	public void add(LogbookBuilder logbook, Collection<Long> logIds) {
 		wrappedSubmit(new AddResource<LogbookBuilder>(logbook, logIds));
 	}
 
 	/**
 	 * Add Property <tt>property</tt> to Log with id <tt>logId</tt>
 	 * 
 	 * @param property
 	 *            property builder
 	 * @param logId
 	 *            log id the property to be added
 	 */
 	public void add(PropertyBuilder property, Long logId) {
 		wrappedSubmit(new AddResource<PropertyBuilder>(property, logId));
 	}
 
 	/**
 	 * Add the Property <tt>property</tt> to the set of the logs with ids
 	 * <tt>logIds</tt>
 	 * 
 	 * @param property
 	 *            property builder
 	 * @param logIds
 	 *            collection of log ids
 	 */
 	public void add(PropertyBuilder property, Collection<Long> logIds) {
 		wrappedSubmit(new AddResource<PropertyBuilder>(property, logIds));
 	}
 
 	private class AddResource<T> implements Runnable {
 		private final T resource;
 		private final Collection<Long> logIds;
 
 		public AddResource(T resource, Long logId) {
 			this.resource = resource;
 			Collection<Long> logIds = new HashSet<Long>();
 			logIds.add(logId);
 			this.logIds = logIds;
 		}
 
 		public AddResource(T resource, Collection<Long> logIds) {
 			this.resource = resource;
 			this.logIds = logIds;
 		}
 
 		@Override
 		public void run() {
 			for (Long logId : logIds) {
 				Log originalLog = new Log(service
 						.path("logs").path(logId.toString()).accept( //$NON-NLS-1$
 								MediaType.APPLICATION_XML).get(XmlLog.class));
 				if (originalLog != null) {
 					LogBuilder newLog = log(originalLog);
 					if (resource instanceof TagBuilder) {
 						newLog.with((TagBuilder) resource);
 					} else if (resource instanceof PropertyBuilder) {
 						newLog.property((PropertyBuilder) resource);
 					} else if (resource instanceof LogbookBuilder) {
 						newLog.in((LogbookBuilder) resource);
 					}
 					service.path("logs").path(newLog.toXml().getId().toString()).type( //$NON-NLS-1$
 									MediaType.APPLICATION_XML)
 							.post(newLog.toXml());
 				}
 			}
 
 		}
 
 	}
 
 	/**
 	 * @param logId
 	 * @param local
 	 */
 	public void add(File local, Long logId) {
 		URI remote = UriBuilder.fromUri(getJCRBaseURI()).path("{arg1}")
 				.path("{arg2}").build(logId, local.getName());
 		URI remoteThumb = UriBuilder.fromUri(getJCRBaseURI())
 				.path("thumbnails").path("{arg1}").path("{arg2}")
 				.build(logId, local.getName());
 		URI remoteDir = UriBuilder.fromUri(getJCRBaseURI()).path("{arg1}")
 				.build(logId);
 		URI remoteThumbDir = UriBuilder.fromUri(getJCRBaseURI())
 				.path("thumbnails").path("{arg1}").build(logId);
 		final int ndx = local.getName().lastIndexOf(".");
 		final String extension = local.getName().substring(ndx + 1);
 		DavMethod mkCol = new MkColMethod(remoteDir.toASCIIString());
 		DavMethod mkColThumb = new MkColMethod(remoteThumbDir.toASCIIString());
 		PutMethod putM = new PutMethod(remote.toASCIIString());
 		PutMethod putMThumb = new PutMethod(remoteThumb.toASCIIString());
 		try {
 			PropFindMethod propM = new PropFindMethod(remoteDir.toASCIIString());
 			webdav.executeMethod(propM);
 			if (!propM.succeeded())
 				webdav.executeMethod(mkCol);
 			propM.releaseConnection();
 			mkCol.releaseConnection();
 		} catch (IOException ex) {
 			throw new OlogException(ex);
 		}
 		try {
 			FileInputStream fis = new FileInputStream(local);
 			RequestEntity requestEntity = new InputStreamRequestEntity(fis);
 			putM.setRequestEntity(requestEntity);
 			webdav.executeMethod(putM);
 			putM.releaseConnection();
 			// If image add thumbnail
 			if ((extension.equals("jpeg") || extension.equals("jpg")
 					|| extension.equals("gif") || extension.equals("png"))) {
 				PropFindMethod propMThumb = new PropFindMethod(
 						remoteThumbDir.toASCIIString());
 				webdav.executeMethod(propMThumb);
 				if (!propMThumb.succeeded())
 					webdav.executeMethod(mkColThumb);
 				propMThumb.releaseConnection();
 				mkColThumb.releaseConnection();
 				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 				Thumbnails.of(local).size(80, 80).outputFormat("jpg")
 						.toOutputStream(outputStream);
 				InputStream fis2 = new ByteArrayInputStream(
 						outputStream.toByteArray());
 				RequestEntity requestEntity2 = new InputStreamRequestEntity(
 						fis2);
 				putMThumb.setRequestEntity(requestEntity2);
 				webdav.executeMethod(putMThumb);
 				putMThumb.releaseConnection();
 
 			}
 		} catch (IOException e) {
 			throw new OlogException(e);
 		}
 	}
 
 	/**
 	 * 
 	 * @param pattern
 	 * @return collection of Log objects
 	 */
 	public Collection<Log> findLogsBySearch(String pattern)
 			throws OlogException {
 		return wrappedSubmit(new FindLogs("logs", pattern));
 	}
 
 	/**
 	 * 
 	 * @param pattern
 	 * @return collection of Log objects
 	 */
 	public Collection<Log> findLogsByTag(String pattern) throws OlogException {
 		return wrappedSubmit(new FindLogs("tag", pattern));
 	}
 
 	/**
 	 * This function is a subset of queryLogs - should it be removed??
 	 * <p>
 	 * TODO: add the usage of patterns and implement on top of the general query
 	 * using the map
 	 * 
 	 * @param logbook
 	 *            logbook name
 	 * @return collection of Log objects
 	 * @throws OlogException
 	 */
 	public Collection<Log> findLogsByLogbook(String logbook)
 			throws OlogException {
 		return wrappedSubmit(new FindLogs("logbook", logbook));
 	}
 
 	/**
 	 * Query for logs based on the criteria specified in the map
 	 * 
 	 * @param map
 	 * @return collection of Log objects
 	 */
 	public Collection<Log> findLogs(Map<String, String> map)
 			throws OlogException {
 		return wrappedSubmit(new FindLogs(map));
 	}
 
 	/**
 	 * Multivalued map used to search for a key with multiple values. e.g.
 	 * logbook a=1 or logbook a=2
 	 * 
 	 * @param map
 	 *            Multivalue map for searching a key with multiple values
 	 * @return collection of Log objects
 	 */
 	public Collection<Log> findLogs(MultivaluedMap<String, String> map)
 			throws OlogException {
 		return wrappedSubmit(new FindLogs(map));
 	}
 
 	private class FindLogs implements Callable<Collection<Log>> {
 
 		private final MultivaluedMap<String, String> map;
 
 		public FindLogs(String queryParameter, String pattern) {
 			MultivaluedMap<String, String> mMap = new MultivaluedMapImpl();
 			mMap.putSingle(queryParameter, pattern);
 			this.map = mMap;
 		}
 
 		public FindLogs(MultivaluedMap<String, String> map) {
 			this.map = map;
 		}
 
 		public FindLogs(Map<String, String> map) {
 			MultivaluedMap<String, String> mMap = new MultivaluedMapImpl();
 			Iterator<Map.Entry<String, String>> itr = map.entrySet().iterator();
 			while (itr.hasNext()) {
 				Map.Entry<String, String> entry = itr.next();
 				mMap.put(entry.getKey(),
 						Arrays.asList(entry.getValue().split(",")));
 			}
 			this.map = mMap;
 		}
 
 		@Override
 		public Collection<Log> call() throws Exception {
 			Collection<Log> logs = new HashSet<Log>();
 			XmlLogs xmlLogs = service.path("logs").queryParams(map)
 					.accept(MediaType.APPLICATION_XML)
 					.accept(MediaType.APPLICATION_JSON).get(XmlLogs.class);
 			for (XmlLog xmllog : xmlLogs.getLogs()) {
 				logs.add(new Log(xmllog));
 			}
 			return Collections.unmodifiableCollection(logs);
 		}
 
 	}
 
 	/**
 	 * Remove {tag} from all logs
 	 * 
 	 * @param tag
 	 */
 	public void deleteTag(String tag) {
 		try {
 			service.path("tags").path(tag).accept(MediaType.APPLICATION_XML) //$NON-NLS-1$
 					.delete();
 		} catch (UniformInterfaceException e) {
 			throw new OlogException(e);
 		}
 	}
 
 	/**
 	 * 
 	 * @param logbook
 	 * @throws LogFinderException
 	 */
 	public void deleteLogbook(String logbook) throws OlogException {
 		try {
 			service.path("logbooks").path(logbook)
 					.accept(MediaType.APPLICATION_XML)
 					.accept(MediaType.APPLICATION_JSON).delete();
 		} catch (UniformInterfaceException e) {
 			throw new OlogException(e);
 		}
 	}
 
 	public Collection<Log> getAllLogs() {
 		try {
 			XmlLogs logs = service.path("logs").accept( //$NON-NLS-1$
 					MediaType.APPLICATION_XML).get(XmlLogs.class);
 			Collection<Log> set = new HashSet<Log>();
 			for (XmlLog log : logs.getLogs()) {
 				set.add(new Log(log));
 			}
 			return set;
 		} catch (UniformInterfaceException e) {
 			throw new OlogException(e);
 		}
 	}
 
 	/**
 	 * Remove the log identified by <tt>log</tt>
 	 * 
 	 * @param log
 	 *            log to be removed
 	 * @throws OlogException
 	 */
 	public void remove(LogBuilder log) throws OlogException {
 		try {
 			service.path("logs").path(log.toXml().getId().toString()).delete(); //$NON-NLS-1$
 		} catch (UniformInterfaceException e) {
 			throw new OlogException(e);
 		}
 	}
 
 	/**
 	 * Remove the log identified by <tt>log</tt>
 	 * 
 	 * @param logId
 	 *            log id log id to be removed
 	 * @throws OlogException
 	 */
 	public void remove(Long logId) throws OlogException {
 		try {
 			service.path("logs").path(logId.toString()).delete(); //$NON-NLS-1$
 		} catch (UniformInterfaceException e) {
 			throw new OlogException(e);
 		}
 	}
 
 	/**
 	 * Remove the log collection identified by <tt>log</tt>
 	 * 
 	 * @param logs
 	 *            logs to be removed
 	 * @throws OlogException
 	 */
 	public void remove(Collection<Log> logs) throws OlogException {
 		try {
 			for (Log log : logs) {
 				service.path("logs").path(log.getId().toString()).delete(); //$NON-NLS-1$
 			}
 		} catch (UniformInterfaceException e) {
 			throw new OlogException(e);
 		}
 	}
 
 	/**
 	 * Remove tag <tt>tag</tt> from the log with the id <tt>logId</tt>
 	 * 
 	 * @param tag
 	 * @param logId
 	 */
 	public void remove(TagBuilder tag, Long logId) throws OlogException {
 		wrappedSubmit(new RemoveResourcefromLog<TagBuilder>(tag, logId));
 	}
 
 	/**
 	 * Remove the tag <tt>tag </tt> from all the logs <tt>logNames</tt>
 	 * 
 	 * @param tag
 	 * @param logIds
 	 * @throws OlogException
 	 */
 	public void remove(TagBuilder tag, Collection<Long> logIds)
 			throws OlogException {
 		wrappedSubmit(new RemoveResourcefromLog<TagBuilder>(tag, logIds));
 	}
 
 	/**
 	 * Remove logbook <tt>logbook</tt> from the log with name <tt>logName</tt>
 	 * 
 	 * @param logbook
 	 *            logbook builder
 	 * @param logId
 	 *            log id
 	 * @throws OlogException
 	 */
 	public void remove(LogbookBuilder logbook, Long logId) throws OlogException {
 		wrappedSubmit(new RemoveResourcefromLog<LogbookBuilder>(logbook, logId));
 	}
 
 	/**
 	 * Remove the logbook <tt>logbook</tt> from the set of logs <tt>logIds</tt>
 	 * 
 	 * @param logbook
 	 * @param logIds
 	 * @throws OlogException
 	 */
 	public void remove(LogbookBuilder logbook, Collection<Long> logIds)
 			throws OlogException {
 		wrappedSubmit(new RemoveResourcefromLog<LogbookBuilder>(logbook, logIds));
 	}
 
 	/**
 	 * Remove property <tt>property</tt> from the log with id <tt>logId</tt>
 	 * TODO: Should this be it's own service?
 	 * 
 	 * @param property
 	 *            property builder
 	 * @param logId
 	 *            log id
 	 * @throws OlogException
 	 */
 	public void remove(PropertyBuilder property, Long logId)
 			throws OlogException {
 		wrappedSubmit(new RemoveResourcefromLog<PropertyBuilder>(property,
 				logId));
 	}
 
 	/**
 	 * Remove the property <tt>property</tt> from the set of logs
 	 * <tt>logIds</tt>
 	 * 
 	 * @param property
 	 * @param logIds
 	 * @throws OlogException
 	 */
 	public void remove(PropertyBuilder property, Collection<Long> logIds)
 			throws OlogException {
 		wrappedSubmit(new RemoveResourcefromLog<PropertyBuilder>(property,
 				logIds));
 	}
 
 	private class RemoveResourcefromLog<T> implements Callable<Void> {
 
 		private final Collection<Long> logIds;
 		private final T resource;
 
 		public RemoveResourcefromLog(T resource, Collection<Long> logIds) {
 			this.logIds = logIds;
 			this.resource = resource;
 		}
 
 		public RemoveResourcefromLog(T resource, Long logId) {
 			Collection<Long> ids = new ArrayList<Long>();
 			ids.add(logId);
 			this.logIds = ids;
 			this.resource = resource;
 		}
 
 		@Override
 		public Void call() throws OlogException {
 			if (resource instanceof TagBuilder) {
 				for (Long logId : logIds) {
 					try {
 						TagBuilder tag = (TagBuilder) resource;
 						service.path("tags").path(tag.toXml().getName()).path(logId.toString()).accept( //$NON-NLS-1$
 										MediaType.APPLICATION_XML).delete();
 					} catch (UniformInterfaceException e) {
 						throw new OlogException(e);
 					}
 				}
 			} else if (resource instanceof LogbookBuilder) {
 				for (Long logId : logIds) {
 					try {
 						LogbookBuilder logbook = (LogbookBuilder) resource;
 						service.path("logbooks")
 								.path(logbook.toXml().getName())
 								.path(logId.toString())
 								.accept(MediaType.APPLICATION_XML)
 								.accept(MediaType.APPLICATION_JSON).delete();
 					} catch (UniformInterfaceException e) {
 						throw new OlogException(e);
 					}
 
 				}
 			} else if (resource instanceof PropertyBuilder) {
 				for (Long logId : logIds) {
 					PropertyBuilder property = (PropertyBuilder) resource;
					Log log = getLog(logId);
 					// TODO consider directly deleting the property.
					XmlLog xmlLog = log(log).toXml();
 					XmlProperties props = new XmlProperties();
					for (Property prop : log.getProperties()) {
 						if (!prop.getName().equals(property.toXml().getName())) {
							props.addXmlProperty(property(prop).toXml());
 						}
 					}
 					xmlLog.setXmlProperties(props);
					if (log != null) {
 						add(log(new Log(xmlLog)));
 					}
 				}
 			}
 			return null;
 		}
 
 	}
 
 	/**
 	 * Remove file attachment from log <tt>logId<tt>
 	 * 
 	 * TODO: sardine delete hangs up, using jersey for delete
 	 * 
 	 * @param String
 	 *            fileName
 	 * @param Long
 	 *            logId
 	 * @throws OlogException
 	 */
 	public void remove(String fileName, Long logId) {
 		try {
 			URI remote = UriBuilder.fromUri(getJCRBaseURI()).path("{arg1}")
 					.path("{arg2}").build(logId, fileName);
 			service.uri(remote).accept(MediaType.APPLICATION_XML)
 					.accept(MediaType.APPLICATION_JSON).delete();
 		} catch (UniformInterfaceException e) {
 			throw new OlogException(e);
 		}
 
 	}
 
 	/**
 	 * Update logbooks and tags of existing log <tt>log</tt>
 	 * 
 	 * @param log
 	 * @throws OlogException
 	 */
 	public void updateLog(LogBuilder log) throws OlogException {
 		wrappedSubmit(new UpdateLog(log));
 	}
 
 	private class UpdateLog implements Runnable {
 
 		private final LogBuilder log;
 
 		public UpdateLog(LogBuilder log) {
 			this.log = log;
 		}
 
 		@Override
 		public void run() throws OlogException {
 			try {
 				service.path("logs").path(log.toXml().getId().toString()).type( //$NON-NLS-1$
 						MediaType.APPLICATION_XML).post(log.toXml());
 			} catch (UniformInterfaceException e) {
 				throw new OlogException(e);
 			}
 		}
 
 	}
 
 	/**
 	 * Add tag <tt>tag</tt> to log <tt>logId</tt> and remove the tag from all
 	 * other logs
 	 * 
 	 * @param tag
 	 * @param logId
 	 * @throws OlogException
 	 */
 	public void set(TagBuilder tag, Long logId) throws OlogException {
 		wrappedSubmit(new SetTag(tag, logId));
 	}
 
 	/**
 	 * Set tag <tt>tag</tt> on the set of logs {logs} and remove it from all
 	 * others
 	 * 
 	 * @param tag
 	 *            tag builder
 	 * @param logIds
 	 *            collection of log ids
 	 */
 	public void set(TagBuilder tag, Collection<Long> logIds) {
 		wrappedSubmit(new SetTag(tag, logIds));
 	}
 
 	private class SetTag implements Runnable {
 
 		private final TagBuilder tag;
 		private final Collection<Long> logIds;
 
 		public SetTag(TagBuilder tag, Long logId) {
 			Collection<Long> logs = new ArrayList<Long>();
 			logs.add(logId);
 			this.tag = tag;
 			this.logIds = logs;
 		}
 
 		public SetTag(TagBuilder tag, Collection<Long> logIds) {
 			this.tag = tag;
 			this.logIds = logIds;
 		}
 
 		@Override
 		public void run() {
 			try {
 				XmlTag xmlTag = tag.toXml();
 				XmlLogs logs = new XmlLogs();
 				LogBuilder log = null;
 				for (Long logId : logIds) {
 					logs.addXmlLog(new XmlLog(logId));
 				}
 				xmlTag.setXmlLogs(logs);
 				service.path("tags").path(tag.toXml().getName())
 						.accept(MediaType.APPLICATION_XML).put(xmlTag);
 			} catch (UniformInterfaceException e) {
 				throw new OlogException(e);
 			}
 		}
 
 	}
 
 	private <T> T wrappedSubmit(Callable<T> callable) {
 		try {
 			return this.executor.submit(callable).get();
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			if (e.getCause() != null
 					&& e.getCause() instanceof UniformInterfaceException) {
 				throw new OlogException(
 						(UniformInterfaceException) e.getCause());
 			}
 			throw new RuntimeException(e);
 		}
 	}
 
 	private void wrappedSubmit(Runnable runnable) {
 		try {
 			this.executor.submit(runnable).get();
 		} catch (ExecutionException e) {
 			if (e.getCause() != null
 					&& e.getCause() instanceof UniformInterfaceException) {
 				throw new OlogException(
 						(UniformInterfaceException) e.getCause());
 			}
 			throw new RuntimeException(e);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 }
