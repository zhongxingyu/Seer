 package de.geotweeter;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.scribe.exceptions.OAuthException;
 import org.scribe.model.OAuthRequest;
 import org.scribe.model.Response;
 import org.scribe.model.Verb;
 
 import android.os.Handler;
 import android.util.Log;
 
 import com.alibaba.fastjson.JSONException;
 
 import de.geotweeter.apiconn.TwitterApiAccess;
 import de.geotweeter.exceptions.UnknownJSONObjectException;
 
 public class StreamRequest {
 
 	private transient TwitterApiAccess api;
 
 	private StreamRequestThread thread = new StreamRequestThread();
 	private boolean keepRunning = true;
 	private Handler handler;// = new Handler();
 	private static final String LOG = "StreamRequest";
 	private boolean doRestart = true;
 
 	protected Account account;
 
 	public StreamRequest(Account account, Handler handler) {
 		this.account = account;
 		this.handler = handler;
 		api = new TwitterApiAccess(account.getToken());
 	}
 
 	public void start() {
 		if (doRestart) {
 			keepRunning = true;
 			new Thread(thread, "StreamRequestThread").start();
 		} else {
 			Log.d(LOG, "start() called but doRestart is false.");
 		}
 	}
 
 	public void stop(boolean restart) {
 		keepRunning = false;
 		this.doRestart = restart;
 
 		try {
 			if (thread != null && thread.stream != null) {
 				thread.stream.close();
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (thread.timer != null) {
 			thread.timer.cancel();
 		}
 	}
 
 	private class StreamRequestThread implements Runnable {
 		private static final String LOG = "StreamRequestThread";
 		private final Pattern part_finder_pattern = Pattern.compile(
 				"([0-9]+)([\n\r]+.+)$", Pattern.DOTALL);
 		public InputStream stream;
 		String buffer = "";
 		private long lastNewlineReceivedAt = 0;
 		private long lastDataReceivedAt = 0;
 		private Timer timer = null;
 		private long reconnectDelay = 10000;
 
 		public void run() {
 			if (timer != null) {
 				timer.cancel();
 			}
 			timer = new Timer(true);
 			timer.schedule(new TimerTask() {
 				@Override
 				public void run() {
 					if (Debug.LOG_STREAM_CHECKS) {
 						Log.d("StreamCheckNewlineTimeoutTask",
 								"Running. "
 										+ (System.currentTimeMillis() - lastNewlineReceivedAt));
 					}
 					if (lastNewlineReceivedAt > 0
 							&& (System.currentTimeMillis() - lastNewlineReceivedAt) > 40000) {
 						// We should get a newline every 30 seconds. If that
 						// didn't happen -> reconnect.
 						try {
 							stream.close();
 						} catch (IOException e) {
 						}
 					}
 				}
 			}, 0, 15000);
 
 			timer.schedule(new TimerTask() {
 				@Override
 				public void run() {
 					if (Debug.LOG_STREAM_CHECKS) {
 						Log.d("StreamCheckDataTimeoutTask",
 								"Running. "
 										+ (System.currentTimeMillis() - lastDataReceivedAt));
 					}
 					if (lastDataReceivedAt > 0
 							&& (System.currentTimeMillis() - lastDataReceivedAt) > 600000) {
 						// We didn't get anything for more than 10 minutes ->
 						// reconnect.
 						try {
 							stream.close();
 						} catch (IOException e) {
 						}
 					}
 				}
 			}, 0, 60000);
 
 			startRequest();
 		}
 
 		public void startRequest() {
 			Log.d(LOG, "Starting Stream.");
 			buffer = "";
 			char ch[] = new char[1];
 			OAuthRequest request = new OAuthRequest(Verb.GET,
 					Constants.URI_USER_STREAM);
 			request.addQuerystringParameter("delimited", "length");
 			api.signRequest(request);
 			try {
 				Response response = request.send();
 				stream = response.getStream();
 				if (stream == null) {
 					Log.d(LOG, "stream is null. Delaying.");
 				} else {
 					InputStreamReader reader = new InputStreamReader(stream);
 					Log.d(LOG, "Waiting for first data.");
 					try {
 						while (reader.read(ch) > 0) {
 							reconnectDelay = 10000;
 							if (!keepRunning) {
 								return;
 							}
 							lastNewlineReceivedAt = System.currentTimeMillis();
 							buffer += ch[0];
 							if (ch[0] == '\n' || ch[0] == '\r') {
 								processBuffer();
 							}
 						}
 					} catch (IOException e) {
 						Log.d(LOG, "Corrupt data. Restarting Stream.");
 					}
 					Log.d(LOG, "Stream beendet");
 				}
 			} catch (OAuthException e) {
 				Log.d(LOG,
 						"No API access. Network may be down. Retrying. Message: "
 								+ e.getMessage(), e);
 			}
 			lastDataReceivedAt = 0;
 			lastNewlineReceivedAt = 0;
 			Log.d(LOG, "Delaying stream reconnection by " + reconnectDelay
 					+ "ms...");
 			try {
 				Thread.sleep(reconnectDelay);
 			} catch (InterruptedException e) {
 				/* Shouldn't happen as we don't interrupt this thread */
 			}
 			reconnectDelay *= 1.5;
 			reconnectDelay = Math.min(reconnectDelay, 300000);
 			handler.post(new Runnable() {
 				@Override
 				public void run() {
 					account.start(false);
 				}
 			});
 		}
 
 		/**
 		 * Processes the stream's buffer (as in
 		 * "looks for seperate JSON objects and parses them").
 		 * @throws IOException 
 		 */
 		public void processBuffer() throws IOException {
 			Matcher m;
 			while ((m = part_finder_pattern.matcher(buffer)) != null
 					&& m.find()) {
 				lastDataReceivedAt = System.currentTimeMillis();
 				reconnectDelay = 10000;
 				String text = m.group(2);
 				int bytes = Integer.parseInt(m.group(1));
 				if (text.length() >= bytes) {
 					buffer = text.substring(bytes);
 					try {
 						account.addTweet(Utils.jsonToNativeObject(text
 								.substring(0, bytes)));
 					} catch (UnknownJSONObjectException ex) {
 						/* Nothing interesting for us */
 					} catch (JSONException ex) {
 						throw new IOException();
 					}
 				} else {
 					return;
 				}
 			}
 		}
 
 	}
 }
