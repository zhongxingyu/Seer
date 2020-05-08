 package de.geotweeter;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Stack;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import javax.net.ssl.SSLPeerUnverifiedException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 import org.scribe.exceptions.OAuthException;
 import org.scribe.model.OAuthRequest;
 import org.scribe.model.Token;
 
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Handler;
 import android.util.Log;
 import android.util.Pair;
 import android.widget.Toast;
 import de.geotweeter.activities.TimelineActivity;
 import de.geotweeter.apiconn.TwitterApiAccess;
 import de.geotweeter.timelineelements.DirectMessage;
 import de.geotweeter.timelineelements.TLEComparator;
 import de.geotweeter.timelineelements.TimelineElement;
 import de.geotweeter.timelineelements.Tweet;
 import de.geotweeter.widgets.AccountSwitcherRadioButton;
 
 public class Account extends Observable implements Serializable {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -3681363869066996199L;
 	
 	protected final static Object lock_object = new Object();
 	protected final String LOG = "Account";
 	
 	public static List<Account> all_accounts = new ArrayList<Account>();
 	private transient int tasksRunning = 0;
 	
 	protected transient ArrayList<TimelineElement> mainTimeline;
 	protected transient List<ArrayList<TimelineElement>> apiResponses;
 
 	private Token token;
 	private transient Handler handler;
 	private transient StreamRequest stream_request;
 	private long max_read_tweet_id = 0;
 	private long max_read_dm_id = 0;
 	private long max_known_tweet_id = 0;
 	private long min_known_tweet_id = Long.MAX_VALUE;
 	private long max_known_dm_id = 0;
 	private long min_known_dm_id = Long.MAX_VALUE;
 	private User user;
 	private transient TimelineElementAdapter elements;
 	private long max_read_mention_id = 0;
 	private transient Context appContext;
 	private transient TwitterApiAccess api;
 	private transient Stack<TimelineElementAdapter> timeline_stack;
 	private MessageHashMap dm_conversations;
 	private Map<AccessType, Boolean> accessSuccessful = new HashMap<AccessType, Boolean>();
 
 	private transient ThreadPoolExecutor exec = new ThreadPoolExecutor(4, 8, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(4));
 	
 	private enum AccessType {
 		TIMELINE, MENTIONS, DM_RCVD, DM_SENT
 	}
 	
 	/**
 	 * Creates an account object
 	 * 
 	 * @param elements The target container for the account's timeline elements
 	 * @param token The user's access token
 	 * @param user The user object
 	 * @param applicationContext The application context
 	 * @param fetchTimeLine If the account's timeline should be fetched
 	 */
 	public Account(TimelineElementAdapter elements, Token token, User user, Context applicationContext, boolean fetchTimeLine) {
 		mainTimeline = new ArrayList<TimelineElement>();
 		apiResponses = new ArrayList<ArrayList<TimelineElement>>(4);
 		for (AccessType type : AccessType.values()) {
 			accessSuccessful.put(type, true);
 		}
 		api = new TwitterApiAccess(token);
 		this.token = token;
 		this.user = user;
 		handler = new Handler();
 		this.elements = elements;
 		this.appContext = applicationContext;
 		stream_request = new StreamRequest(this);
 		dm_conversations = new MessageHashMap(user.id);
 		
 		all_accounts.add(this);
 		
 		timeline_stack = new Stack<TimelineElementAdapter>();
 		timeline_stack.push(elements);
 		
 		if (fetchTimeLine) {
 			if (Debug.LOG_ACCOUNT) {
 				Log.d(LOG, "Fetch timelines from API for " + user.screen_name);
 			}
 			start(true);
 		}
 	}
 	
 	/**
 	 * Provides access to all available direct message conversations
 	 * 
 	 * @return All direct message conversations
 	 */
 	public MessageHashMap getDMConversations() {
 		return dm_conversations;
 	}
 
 	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
 		in.defaultReadObject();
 		mainTimeline = new ArrayList<TimelineElement>();
 		apiResponses = new ArrayList<ArrayList<TimelineElement>>(4);
 		api = new TwitterApiAccess(token);
 		handler = new Handler();
 		stream_request = new StreamRequest(this);
 	}
 		
 	/**
 	 * Setter for the application context
 	 * 
 	 * @param appContext
 	 */
 	public void setAppContext(Context appContext) {
 		this.appContext = appContext;
 	}
 
 	@SuppressWarnings("unused")
 	/**
 	 * Fetches the timeline
 	 * 
 	 * @param loadPersistedTweets true if the timeline should be pre filled with stored timeline elements
 	 */
 	public void start(boolean loadPersistedTweets) {
 		Log.d(LOG, "In start()");
 		if (stream_request != null) {
 			stream_request.stop(true);
 		}
 		if (loadPersistedTweets) {
 			loadPersistedTweets(appContext);
 		}
 		if (Debug.ENABLED && Debug.SKIP_FILL_TIMELINE) {
 			Log.d(LOG, "TimelineRefreshThread skipped. (Debug.SKIP_FILL_TIMELINE)");
 		} else {
 			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 				refreshTimeline();
 			} else {
 				refreshTimelinePreAPI11();
 			}
 		}
 		getMaxReadIDs();
 	}
 		
 	/**
 	 * Stops the stream access
 	 */
 	public void stopStream() {
 		stream_request.stop(false);
 	}
 
 	/**
 	 * Gets timeline updates from the twitter API from Android 3.0 onward
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void refreshTimeline() {
 		if (tasksRunning == 0) {
 			setChanged();
 			notifyObservers(AccountSwitcherRadioButton.Message.REFRESH_START);
 		}
 		for (AccessType type : AccessType.values()) {
 			accessSuccessful.put(type, true);
 		}
 		tasksRunning = 4;
 		new TimelineRefreshTask().executeOnExecutor(exec, AccessType.TIMELINE);
 		new TimelineRefreshTask().executeOnExecutor(exec, AccessType.MENTIONS);
 		new TimelineRefreshTask().executeOnExecutor(exec, AccessType.DM_RCVD);
 		new TimelineRefreshTask().executeOnExecutor(exec, AccessType.DM_SENT);
 	}
 
 	/**
 	 * Gets timeline updates from the twitter API below Android 3.0
 	 */
 	private void refreshTimelinePreAPI11() {
 		if (tasksRunning == 0) {
 			setChanged();
 			notifyObservers(AccountSwitcherRadioButton.Message.REFRESH_START);
 		}
 		for (AccessType type : AccessType.values()) {
 			accessSuccessful.put(type, true);
 		}
 		tasksRunning = 4;
 		new TimelineRefreshTask().execute(AccessType.TIMELINE);
 		new TimelineRefreshTask().execute(AccessType.MENTIONS);
 		new TimelineRefreshTask().execute(AccessType.DM_RCVD);
 		new TimelineRefreshTask().execute(AccessType.DM_SENT);
 	}
 
 	/**
 	 * Keeps the API access result or the thrown exception
 	 */
 	private class TimelineRefreshResult {
 		public Exception e;
 		public ArrayList<TimelineElement> elements;
 	}
 	
 	/**
 	 * AsyncTask which handles any timeline API access tasks
 	 */
 	private class TimelineRefreshTask extends AsyncTask<AccessType, Void, TimelineRefreshResult> {
 	
 		private AccessType accessType;
 		private long startTime;
 		
 		@Override
 		protected TimelineRefreshResult doInBackground(AccessType... params) {
 			accessType = params[0];
 			startTime = System.currentTimeMillis();
 			TimelineRefreshResult result = new TimelineRefreshResult();
 			try {
 				switch (accessType) {
 				case TIMELINE: 
 					Log.d(LOG, "Get home timeline");
 					result.elements = api.getHomeTimeline(max_known_tweet_id, 0);
 					return result;
 				case MENTIONS:
 					Log.d(LOG, "Get mentions");
 					result.elements = api.getMentions(max_known_tweet_id, 0);
 					return result;
 				case DM_RCVD:
 					Log.d(LOG, "Get received dm");
 					result.elements = api.getReceivedDMs(max_known_dm_id, 0);
 					return result;
 				case DM_SENT:
 					Log.d(LOG, "Get sent dm");
 					result.elements = api.getSentDMs(max_known_dm_id, 0);
 					return result;
 				}
 			} catch (OAuthException e) {
 				result.e = e;
 				return result;
 			}
 			return null;
 		}
 		
 		@SuppressWarnings("unused")
 		/**
 		 * Handles the API responses
 		 * 
 		 * @param result API response
 		 */
 		protected void onPostExecute(TimelineRefreshResult result) {
 			tasksRunning--;
 			
 			if (result.e != null) {
 				if (accessSuccessful.get(accessType)) {
 					Log.d(LOG, "Get " + accessType.toString() + " failed. Retrying in 10 seconds");
 					Toast.makeText(appContext, R.string.error_api_access_retry, Toast.LENGTH_SHORT).show();
 					handler.postDelayed(new Runnable() {
 						
 						@Override
 						public void run() {
 							accessSuccessful.put(accessType, false);
 							tasksRunning++;
 							new TimelineRefreshTask().execute(accessType);
 						}
 					}, 10000);
 				return;
 				} else {
 					Log.d(LOG, "Recurring error in " + accessType.toString() + " access. Not Retrying");
 					Toast.makeText(appContext, R.string.error_api_access_recurring, Toast.LENGTH_SHORT).show();
 					return;
 				}
 			}
 			
 			accessSuccessful.put(accessType, true);
 			
 			Log.d(LOG, "Get " + accessType.toString() + " finished. Runtime: " + String.valueOf(System.currentTimeMillis() - startTime) + "ms");
 			if (accessType == AccessType.TIMELINE) {
 				mainTimeline.addAll(result.elements);
 			} else {
 				apiResponses.add(result.elements);
 			}
 			
 			if (accessType == AccessType.DM_RCVD || accessType == AccessType.DM_SENT) {
 				dm_conversations.addMessages(result.elements);
 			}
 			
 			if (tasksRunning == 0) { 
 				if (mainTimeline == null) {
 					mainTimeline = new ArrayList<TimelineElement>();
 				}
 				if (!mainTimeline.isEmpty()) {
 					apiResponses.add(0, mainTimeline);
 				}
 				parseData(apiResponses, false);
 				
 				if (Debug.ENABLED && Debug.SKIP_START_STREAM) {
 					Log.d(LOG, "Not starting stream - Debug.SKIP_START_STREAM is true.");
 				} else {
 					stream_request.start();
 				}
 				
 				setChanged();
 				notifyObservers(AccountSwitcherRadioButton.Message.REFRESH_FINISHED);
 			}
 
 		}
 		
 	}
 		
 	/**
 	 * Puts the API responses in the visible timeline and removes duplicates
 	 * 
 	 * @param apiResponses2 The API responses
 	 * @param do_clip
 	 */
 	protected void parseData(List<ArrayList<TimelineElement>> apiResponses2, boolean do_clip) {
 		final long old_max_known_dm_id = max_known_dm_id;
 		Log.d(LOG, "parseData started.");
 		final List<TimelineElement> all_elements = new ArrayList<TimelineElement>();
 
 		for (ArrayList<TimelineElement> list : apiResponses2) {
 			if (list == null) {
 				continue;
 			}
 			if (list.size() == 0) {
 				continue;
 			}
 			TimelineElement element = list.get(0);
 
 			if (element instanceof DirectMessage) {
 				max_known_dm_id = Math.max(max_known_dm_id, ((DirectMessage) element).id);
 			} else if (element instanceof Tweet) {
 				max_known_tweet_id = Math.max(max_known_tweet_id, ((Tweet) element).id);
 			}
 			element = list.get(list.size() - 1);
 			if (element instanceof DirectMessage) {
 				min_known_dm_id = Math.min(min_known_dm_id, ((DirectMessage) element).id);
 			} else if (element instanceof Tweet) {
 				min_known_tweet_id = Math.min(min_known_tweet_id, ((Tweet) element).id);
 			}
 			for (TimelineElement tle : list) {
 				if (TimelineActivity.availableTweets != null) {
 					if (!TimelineActivity.availableTweets.containsKey(tle.getID())) {
 						if (tle.getClass() == DirectMessage.class) {
 							if (tle.getID() > old_max_known_dm_id) {
 								all_elements.add(tle);
 							}
 						} else {
 							all_elements.add(tle);
 						}
 					}
 				} else { 
 					/* Shouldn't actually happen... */
 					if (tle.getClass() == DirectMessage.class) {
 						if (tle.getID() > old_max_known_dm_id) {
 							all_elements.add(tle);
 						}
 					} else {
 						all_elements.add(tle);
 					}					
 				}
 			}
 		}
 		Collections.sort(all_elements, new TLEComparator());
 				
 		Log.d(LOG, "parseData is almost done. " + all_elements.size() + " elements.");
 		handler.post(new Runnable() {
 			@Override
 			public void run() {
 				elements.addAllAsFirst(all_elements);
 				setChanged();
 				notifyObservers(AccountSwitcherRadioButton.Message.UNREAD);
 			}
 		});
 	}
 
 	/**
 	 * Adds a timeline element to the timeline
 	 * 
 	 * @param elm Timeline element to be added
 	 */
 	public void addTweet(final TimelineElement elm) {
 		Log.d(LOG, "Adding Tweet.");
 		if (elm instanceof DirectMessage) {
 			dm_conversations.addMessage((DirectMessage) elm);
 			if (elm.getID() > max_known_dm_id) {
 				max_known_dm_id = elm.getID();
 			}
 		} else if (elm instanceof Tweet) {
 			if (elm.getID() > max_known_tweet_id) {
 				max_known_tweet_id = elm.getID();
 			}
 		}
 		//elements.add(tweet);
 		handler.post(new Runnable() {
 			public void run() {
 				elements.addAsFirst(elm);
 				setChanged();
 				notifyObservers(AccountSwitcherRadioButton.Message.UNREAD);
 			}
 		});
 	}
 
 	/**
 	 * Registers with push service
 	 */
 	public void registerForGCMMessages() {
 		Log.d(LOG, "Registering...");
 		new Thread(new Runnable() {
 			public void run() {
 				HttpClient http_client = new CacertHttpClient(appContext);
 				HttpPost http_post = new HttpPost(Utils.getProperty("google.gcm.server.url") + "/register");
 				try {
 					List<NameValuePair> name_value_pair = new ArrayList<NameValuePair>(5);
 					name_value_pair.add(new BasicNameValuePair("reg_id", TimelineActivity.reg_id));
 					name_value_pair.add(new BasicNameValuePair("token", getToken().getToken()));
 					name_value_pair.add(new BasicNameValuePair("secret", getToken().getSecret()));
 					name_value_pair.add(new BasicNameValuePair("screen_name", getUser().getScreenName()));
 					name_value_pair.add(new BasicNameValuePair("protocol_version", "1"));
 					http_post.setEntity(new UrlEncodedFormEntity(name_value_pair));
 					http_client.execute(http_post);
 				} catch(ClientProtocolException e) {
 					e.printStackTrace();
 				} catch(SSLPeerUnverifiedException e) {
 					Log.e(LOG, "Couldn't register account at GCM-server. Maybe you forgot to install CAcert's certificate?");
 					e.printStackTrace();
 				} catch(IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}, "register").start();
 	}
 	
 	/**
 	 * Gets the last read tweet ID from tweetmarker
 	 */
 	public void getMaxReadIDs() {
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				OAuthRequest oauth_request = api.getVerifiedCredentials();
 								
 				try {
 					HttpClient http_client = new DefaultHttpClient();
 					HttpGet http_get = new HttpGet(Constants.URI_TWEETMARKER_LASTREAD + user.getScreenName() + "&api_key=" + Utils.getProperty("tweetmarker.key"));
 					http_get.addHeader("X-Auth-Service-Provider", Constants.URI_VERIFY_CREDENTIALS);
 					http_get.addHeader("X-Verify-Credentials-Authorization", oauth_request.getHeaders().get("Authorization"));
 					HttpResponse response = http_client.execute(http_get);
 					if (response.getEntity() == null) {
 						return;
 					}
 					String[] parts = EntityUtils.toString(response.getEntity()).split(",");
 					try {
 						max_read_tweet_id = Long.parseLong(parts[0]);
 					} catch (NumberFormatException ex) {}
 					try {
 						max_read_mention_id = Long.parseLong(parts[1]);
 					} catch (NumberFormatException ex) {}
 					try {
 						max_read_dm_id = Long.parseLong(parts[2]);
 						if (max_known_dm_id == 0) {
 							max_known_dm_id = max_read_dm_id;
 						}
 					} catch (NumberFormatException ex) {}
 					handler.post(new Runnable() {
 						@Override
 						public void run() {
 							elements.notifyDataSetChanged();
 							setChanged();
 							notifyObservers(AccountSwitcherRadioButton.Message.UNREAD);
 							
 							LinkedList<Pair<TimelineElement, String>> elementsToDelete = new LinkedList<Pair<TimelineElement, String>>();
 							for (Pair<TimelineElement, String> pair : ((Geotweeter)appContext).notifiedElements) {
 								if (pair.first instanceof DirectMessage) {
 									if (pair.first.getID() <= max_read_dm_id) elementsToDelete.add(pair);
 								} else if (pair.first instanceof Tweet) {
 									if (pair.first.getID() <= max_read_tweet_id) elementsToDelete.add(pair);
 								} else {
 									elementsToDelete.add(pair);
 								}
 							}
 							
 							for (Pair<TimelineElement, String> pair : elementsToDelete) {
 								((Geotweeter)appContext).notifiedElements.remove(pair);
 							}
 							elementsToDelete.clear();
 							((Geotweeter)appContext).updateNotification(false);
 						}
 					});
 				} catch (UnsupportedEncodingException e) {
 					e.printStackTrace();
 				} catch (ClientProtocolException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}, "GetMaxReadIDs").start();
 	}
 	
 	/**
 	 * Pushes the actual read tweet ids to tweet marker
 	 * 
 	 * @param tweet_id
 	 * @param mention_id
 	 * @param dm_id
 	 */
 	public void setMaxReadIDs(long tweet_id, long mention_id, long dm_id) {
 		if (tweet_id > this.max_read_tweet_id) {
 			this.max_read_tweet_id = tweet_id;
 		}
 		if (mention_id > this.max_read_mention_id ) {
 			this.max_read_mention_id = mention_id;
 		}
 		if (dm_id > this.max_read_dm_id) {
 			this.max_read_dm_id = dm_id;
 		}
 		
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				OAuthRequest oauth_request = api.getVerifiedCredentials();
 								
 				try {
 					HttpClient http_client = new DefaultHttpClient();
 					HttpPost http_post = new HttpPost(Constants.URI_TWEETMARKER_LASTREAD + user.getScreenName() + "&api_key=" + Utils.getProperty("tweetmarker.key"));
 					http_post.addHeader("X-Auth-Service-Provider", Constants.URI_VERIFY_CREDENTIALS);
 					http_post.addHeader("X-Verify-Credentials-Authorization", oauth_request.getHeaders().get("Authorization"));
 					http_post.setEntity(new StringEntity(""+max_read_tweet_id+","+max_read_mention_id+","+max_read_dm_id));
 					http_client.execute(http_post);
 				} catch (UnsupportedEncodingException e) {
 					e.printStackTrace();
 				} catch (ClientProtocolException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}, "SetMaxReadIDs").start();
 		
 		LinkedList<Pair<TimelineElement, String>> elementsToDelete = new LinkedList<Pair<TimelineElement, String>>();
 		for (Pair<TimelineElement, String> pair : ((Geotweeter)appContext).notifiedElements) {
 			if (pair.first instanceof DirectMessage) {
 				if (pair.first.getID() <= max_read_dm_id) elementsToDelete.add(pair);
 			} else if (pair.first instanceof Tweet) {
 				if (pair.first.getID() <= max_read_tweet_id) elementsToDelete.add(pair);
 			} else {
 				elementsToDelete.add(pair);
 			}
 		}
 		
 		for (Pair<TimelineElement, String> pair : elementsToDelete) {
 			((Geotweeter)appContext).notifiedElements.remove(pair);
 		}
 		elementsToDelete.clear();
 		((Geotweeter)appContext).updateNotification(false);
 		
 		elements.notifyDataSetChanged();
 		setChanged();
 		notifyObservers(AccountSwitcherRadioButton.Message.UNREAD);
 	}
 	
 	/**
 	 * Returns the newest read tweet id
 	 * 
 	 * @return
 	 */
 	public long getMaxReadTweetID() {
 		return max_read_tweet_id;
 	}
 	
 	/**
 	 * Returns the account's TimelineElementAdapter
 	 * 
 	 * @return
 	 */
 	public TimelineElementAdapter getElements() {
 		return elements;
 	}
 
 	/**
 	 * Returns the account's user access token
 	 * 
 	 * @return
 	 */
 	public Token getToken() {
 		return token;
 	}
 
 	
 	/**
 	 * Sets the account's user element
 	 * 
 	 * @param user
 	 */
 	public void setUser(User user) {
 		this.user = user;
 	}
 	
 	/**
 	 * Returns the account's user element
 	 * 
 	 * @return
 	 */
 	public User getUser() {
 		return user;
 	}
 
 	/**
 	 * Stores the recent 50 timeline element to persistent memory
 	 * 
 	 * @param context
 	 */
 	public void persistTweets(Context context) {
 		File dir = context.getExternalFilesDir(null);
 		if (!dir.exists()) {
 			dir = context.getCacheDir();
 		}
 		
 		dir = new File(dir, Constants.PATH_TIMELINE_DATA);
 		
 		if (!dir.exists()) {
 			dir.mkdirs();
 		}
 		
 		List<TimelineElement> last_tweets = new ArrayList<TimelineElement>(50);
 		for (int i=0; i<elements.getCount(); i++) {
 			if (i >= 100) {
 				break;
 			}
 			last_tweets.add(elements.getItem(i));
 		}
 		
 		try {
 			FileOutputStream fout = new FileOutputStream(dir.getPath() + File.separator + String.valueOf(getUser().id));
 			ObjectOutputStream oos = new ObjectOutputStream(fout);
 			oos.writeObject(last_tweets);
 			oos.close();
 		} catch(Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	/**
 	 * Gets the last stored timeline elements from persistent memory
 	 * 
 	 * @param context
 	 */
 	public void loadPersistedTweets(Context context) {
 		String fileToLoad = null;
 		File dir = context.getExternalFilesDir(null);
 		if (!dir.exists()) {
 			dir = context.getCacheDir();
 		}
 		
 		dir = new File(dir, Constants.PATH_TIMELINE_DATA);
 		
 		fileToLoad = dir.getPath() + File.separator + String.valueOf(getUser().id);
 		
 		if (!(new File(fileToLoad).exists())) {
 			return;
 		}
 		
 		List<TimelineElement> tweets;
 		try {
 			FileInputStream fin = new FileInputStream(fileToLoad);
 			ObjectInputStream ois = new ObjectInputStream(fin);
 			tweets = (List<TimelineElement>) ois.readObject();
 			ois.close();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			return;
 		}
 		
 		for (TimelineElement elm : tweets) {
 			if (elm instanceof DirectMessage) {
 				if (elm.getID() > max_known_dm_id) {
 					max_known_dm_id = elm.getID();
 				}
 			} else if (elm instanceof Tweet) {
 				if (elm.getID() > max_known_tweet_id) {
 					max_known_tweet_id = elm.getID();
 				}
 			}
 		}
 		elements.addAllAsFirst(tweets);
 		setChanged();
 		notifyObservers(AccountSwitcherRadioButton.Message.UNREAD);
 	}
 	
 	/**
 	 * Returns the TwitterApiAccess object
 	 * 
 	 * @return
 	 */
 	public TwitterApiAccess getApi() {
 		return api;
 	}
 	
 	/**
 	 * Pushes the given timeline to the last visible timelines stack
 	 * 
 	 * @param tea
 	 */
 	public void pushTimeline(TimelineElementAdapter tea) {
 		timeline_stack.push(tea);
 	}
 	
 	/**
 	 * Pops the currently shown timeline from the stack and returns the previous shown. 
 	 * 
 	 * @return
 	 */
 	public TimelineElementAdapter getPrevTimeline() {
 		timeline_stack.pop();
 		return timeline_stack.peek();
 	}
 		
 	/**
 	 * Returns the currently shown timeline from the stack
 	 * 
 	 * @return
 	 */
 	public TimelineElementAdapter activeTimeline() {
 		return timeline_stack.peek();
 	}
 	
 	public int getUnreadTweetsSize() {
 		List<TimelineElement> tweets = elements.getItems();
 		int size = 0;
 		for (; size < tweets.size() && tweets.get(size).getID() > max_read_tweet_id; size++) {
 		}
 		return size;
 	}
 	
 	/**
 	 * Returns the account object for a given user object
 	 * 
 	 * @param u The according user object
 	 * @return The according account object if available, null otherwise
 	 */
 	public static Account getAccount(User u) {
 		for (Account a : all_accounts) {
 			if (a.getUser().id == u.id) {
 				return a;
 			}
 		}
 		return null;
 	}
 	
 }
