 package burrito.services;
 
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import com.google.appengine.api.taskqueue.Queue;
 import com.google.appengine.api.taskqueue.QueueFactory;
 import com.google.appengine.api.taskqueue.TaskOptions;
 import com.google.appengine.repackaged.com.google.common.base.CharEscapers;
 import static com.google.appengine.api.taskqueue.TaskOptions.Builder.*;
 
 /**
  * 
  * Utility class for broadcasting a message to multiple subscribers
  * 
  * 
  * &copy; Henric Persson 2011
  * 
  * @author henric.persson@gmail.com
  * 
  */
 public class Broadcaster {
 
 	private String broadcastServer; // defaults to localhost
 
 	/**
 	 * Creates a broadcaster for use within the same host.
 	 */
 	public Broadcaster() {
 		// default constructor;
 	}
 
 	/**
 	 * Creates a broadcaster configured on another host, also running burrito.
 	 * 
 	 * @param broadcastServer
 	 */
 	public Broadcaster(String broadcastServer) {
 		this.broadcastServer = broadcastServer;
 	}
 
 	/**
 	 * Broadcasts a message to a feed.
 	 * 
 	 * @param message
 	 *            the message to broadcast
 	 * @param feedId
 	 *            the feed to broadcast to
 	 * @param skipSubscriptionId
 	 *            (optional) a subscriptionId that you don't want the message to
 	 *            reach
 	 */
 	public void broadcast(String message, String feedId, Long skipSubscriptionId) {
 		if (broadcastServer == null) {
 			broadcastInternally(message, feedId, skipSubscriptionId);
 		} else {
 			broadcastExternally(message, feedId, skipSubscriptionId);
 		}
 	}
 
 	private void broadcastInternally(String message, String feedId,
 			Long skipSubscriptionId) {
 		Queue queue = QueueFactory.getDefaultQueue();
 		TaskOptions opts = withUrl("/burrito/feeds/"
 				+ CharEscapers.uriEscaper(false).escape(feedId)
 				+ "/broadcast").param("message", message).param("feedId", feedId);
 		if (skipSubscriptionId != null) {
			opts.param("excludeSubscriptionId", String.valueOf(skipSubscriptionId));
 		}
 		queue.add(opts);
 	}
 	
 
 	/**
 	 * Broadcasts the message through an external host
 	 * 
 	 * @param message
 	 * @param feedId
 	 * @param skipSubscriptionId
 	 */
 	private void broadcastExternally(String message, String feedId,
 			Long skipSubscriptionId) {
 		try {
 			URL broadcastUrl = new URL(broadcastServer
 					+ "/burrito/feeds/"
 					+ CharEscapers.uriEscaper(false).escape(feedId)
 					+ "/broadcast");
 			HttpURLConnection connection = (HttpURLConnection) broadcastUrl
 					.openConnection();
 			connection.setDoOutput(true);
 			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
 			connection.setRequestMethod("POST");
 			OutputStreamWriter writer = new OutputStreamWriter(
 					connection.getOutputStream(), "UTF-8");
 			writer.write("message="
 					+ CharEscapers.uriEscaper(false).escape(message));
 			if (skipSubscriptionId != null) {
 				writer.write("&excludeSubscriptionId=" + skipSubscriptionId);
 			}
 			writer.close();
 			if (connection.getResponseCode() != 200) {
 				throw new RuntimeException("Failed to push. Got response "
 						+ connection.getResponseCode());
 			}
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException("Failed to broadcast message", e);
 		} catch (MalformedURLException e) {
 			throw new RuntimeException("Failed to broadcast message", e);
 		} catch (IOException e) {
 			throw new RuntimeException("Failed to broadcast message", e);
 		}
 	}
 
 }
