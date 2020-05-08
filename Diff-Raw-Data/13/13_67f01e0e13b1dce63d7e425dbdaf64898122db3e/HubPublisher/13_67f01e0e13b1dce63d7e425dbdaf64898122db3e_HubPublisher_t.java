 package org.ow2.chameleon.rose.pubsubhubbub.publisher.internal;
 
 import static org.ow2.chameleon.rose.pubsubhubbub.constants.PubsubhubbubConstants.HTTP_POST_HEADER_TYPE;
 import static org.ow2.chameleon.rose.pubsubhubbub.constants.PubsubhubbubConstants.HTTP_POST_PARAMETER_HUB_MODE;
 import static org.ow2.chameleon.rose.pubsubhubbub.constants.PubsubhubbubConstants.HTTP_POST_PARAMETER_RSS_TOPIC_URL;
 import static org.ow2.chameleon.rose.pubsubhubbub.constants.PubsubhubbubConstants.DEFAULT_HTTP_PORT;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.http.HttpService;
 import org.osgi.service.log.LogService;
 import org.ow2.chameleon.rose.RoseMachine;
 import org.ow2.chameleon.rose.pubsubhubbub.constants.PubsubhubbubConstants.HubMode;
 
 /**
  * Connect and register as a publisher to Rose Hub, sends an update and
  * unregister command.
  * 
  * @author Bartek
  * 
  */
 public class HubPublisher {
 
 	private final String urlHub;
 	private final String rssUrl;
 	private HttpPost postMethod;
 	private final HttpClient client;
 	private final String host;
 	private final LogService logger;
 
 	/**
 	 * Register a topic in hub.
 	 * 
 	 * @param pUrlHub
 	 *            Pubsubhubub url
 	 * @param pRssUrl
 	 *            RSS topic url
 	 * @param context
 	 *            BundleContext
 	 * @param rose
 	 *            Rose service
 	 * @param pLogger
 	 *            Log service
 	 * @throws IOException
 	 *             exception
 	 */
 	public HubPublisher(final String pUrlHub, final String pRssUrl,
 			final BundleContext context, final RoseMachine rose,
 			final LogService pLogger) throws IOException {
 		String port = null;
 
 		this.urlHub = pUrlHub;
 		this.logger = pLogger;
 		final ServiceReference httpServiceRef = context
 				.getServiceReference(HttpService.class.getName());
 		if (httpServiceRef != null) {
 			port = (String) httpServiceRef
 					.getProperty("org.osgi.service.http.port");
 			
 		}
 		if (port == null) {
 			port = context.getProperty("org.osgi.service.http.port");
 		}
 
 		// set default port number
 		if (port == null) {
 			port = DEFAULT_HTTP_PORT;
 		}
 
 		this.host = rose.getHost();
 
 		this.rssUrl = "http://" + this.host + ":" + port + pRssUrl + "/";
 		client = new DefaultHttpClient();
 
 		// prepare post method
 		postMethod = new HttpPost(this.urlHub);
 		postMethod.setHeader("Content-Type", HTTP_POST_HEADER_TYPE);
 
 		final List<NameValuePair> nvps = new ArrayList<NameValuePair>();
 		nvps.add(new BasicNameValuePair(HTTP_POST_PARAMETER_HUB_MODE,
 				HubMode.publish.toString()));
 		nvps.add(new BasicNameValuePair(HTTP_POST_PARAMETER_RSS_TOPIC_URL,
 				this.rssUrl));
 
 		postMethod.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
 		final HttpResponse response = client.execute(postMethod);
 		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
 			response.getEntity().getContent().close();
 			throw new ClientProtocolException(
 					"Server didn register a topic, received status from hub: "
 							+ response.getStatusLine().getStatusCode());
 		}
 		// read an empty entity and close a connection
 		response.getEntity().getContent().close();
 	}
 
 	/**
 	 * Send an update to hub.
 	 * 
 	 */
 	public final void update() {
 		// prepare post method
 		postMethod = new HttpPost(this.urlHub);
 		postMethod.setHeader("Content-Type",
 				"application/x-www-form-urlencoded");
 		try {
 			final List<NameValuePair> nvps = new ArrayList<NameValuePair>();
 			nvps.add(new BasicNameValuePair(HTTP_POST_PARAMETER_HUB_MODE,
 					HubMode.update.toString()));
 			nvps.add(new BasicNameValuePair(HTTP_POST_PARAMETER_RSS_TOPIC_URL,
 					this.rssUrl));
 			postMethod.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
 			final HttpResponse response = client.execute(postMethod);
 			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_ACCEPTED) {
 				response.getEntity().getContent().close();
 				throw new ClientProtocolException(
 						"Server didnt update, received status from hub: "
 								+ response.getStatusLine().getStatusCode());
 			}
 			// read an empty entity and close a connection
 			response.getEntity().getContent().close();
 		} catch (Exception e) {
 			logger.log(LogService.LOG_ERROR, "Error in update", e);
 		}
 
 	}
 
 	/**
 	 * Send an unregister to hub.
 	 */
 	public final void unregister() {
 		// prepare post method
 		postMethod = new HttpPost(this.urlHub);
 		postMethod.setHeader("Content-Type",
 				"application/x-www-form-urlencoded");
 		try {
 			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
 			nvps.add(new BasicNameValuePair(HTTP_POST_PARAMETER_HUB_MODE,
 					HubMode.unpublish.toString()));
 			nvps.add(new BasicNameValuePair(HTTP_POST_PARAMETER_RSS_TOPIC_URL,
 					this.rssUrl));
 			postMethod.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
 			HttpResponse response = client.execute(postMethod);
 			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_ACCEPTED) {
 				response.getEntity().getContent().close();
 				throw new ClientProtocolException(
 						"Server didnt unregister, received status from hub: "
 								+ response.getStatusLine().getStatusCode());
 			}
 			// read an empty entity and close a connection
 			response.getEntity().getContent().close();
 		} catch (Exception e) {
 			logger.log(LogService.LOG_ERROR, "Error in unregister", e);
 		}
 
 	}
 }
