 package de.geotweeter;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
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
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.ByteArrayBody;
 import org.apache.http.entity.mime.content.ContentBody;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 import org.scribe.builder.ServiceBuilder;
 import org.scribe.builder.api.TwitterApi;
 import org.scribe.model.OAuthRequest;
 import org.scribe.model.Response;
 import org.scribe.model.Token;
 import org.scribe.model.Verb;
 import org.scribe.oauth.OAuthService;
 
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Handler;
 import android.util.Log;
 
 import com.alibaba.fastjson.JSON;
 
 import de.geotweeter.activities.TimelineActivity;
 import de.geotweeter.apiconn.TwitterApiAccess;
 import de.geotweeter.exceptions.TweetSendException;
 import de.geotweeter.timelineelements.DirectMessage;
 import de.geotweeter.timelineelements.TimelineElement;
 import de.geotweeter.timelineelements.Tweet;
 
 public class Account implements Serializable {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -3681363869066996199L;
 	private static final long PIC_SIZE_TWITTER = 3145728;
 	
 	protected final static Object lock_object = new Object();
 	protected final String LOG = "Account";
 	
 	public static ArrayList<Account> all_accounts = new ArrayList<Account>();
 	private static transient OAuthService service;
 	private transient int tasksRunning = 0;
 	
 	protected transient ArrayList<TimelineElement> mainTimeline;
 	protected transient ArrayList<ArrayList<TimelineElement>> apiResponses;
 
 	private Token token;
 	private transient Handler handler;
 	private transient StreamRequest stream_request;
 	private long max_read_tweet_id = 0;
 	private long max_read_dm_id = 0;
 	private long max_known_tweet_id = 0;
 	private long min_known_tweet_id = -1;
 	private long max_known_dm_id = 0;
 	private long min_known_dm_id = -1;
 	private User user;
 	private transient TimelineElementAdapter elements;
 	private long max_read_mention_id = 0;
 	private transient Context appContext;
 	private transient TwitterApiAccess api;
 	
 	private enum AccessType {
 		TIMELINE, MENTIONS, DM_RCVD, DM_SENT
 	}
 	
 	public Account(TimelineElementAdapter elements, Token token, User user, Context applicationContext, boolean fetchTimeLine) {
 		if (service == null) {
 			ServiceBuilder builder = new ServiceBuilder()
 			                             .provider(TwitterApi.class)
 			                             .apiKey(Utils.getProperty("twitter.consumer.key"))
 			                             .apiSecret(Utils.getProperty("twitter.consumer.secret"));
 			if (Debug.LOG_OAUTH_STUFF) {
 				builder = builder.debug();
 			}
 			service = builder.build();
 		}
 		mainTimeline = new ArrayList<TimelineElement>();
 		apiResponses = new ArrayList<ArrayList<TimelineElement>>(4);
 		api = new TwitterApiAccess(token);
 		this.token = token;
 		this.user = user;
 		handler = new Handler();
 		this.elements = elements;
 		this.appContext = applicationContext;
 		stream_request = new StreamRequest(this);
 		
 		all_accounts.add(this);
 		if (fetchTimeLine) {
 			start(true);
 		}
 	}
 	
 	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
 		in.defaultReadObject();
 		if (service == null) {
 			ServiceBuilder builder = new ServiceBuilder()
 			                             .provider(TwitterApi.class)
 			                             .apiKey(Utils.getProperty("twitter.consumer.key"))
 			                             .apiSecret(Utils.getProperty("twitter.consumer.secret"));
 			if (Debug.LOG_OAUTH_STUFF) {
 				builder = builder.debug();
 			}
 			service = builder.build();
 		}
 		mainTimeline = new ArrayList<TimelineElement>();
 		apiResponses = new ArrayList<ArrayList<TimelineElement>>(4);
 		api = new TwitterApiAccess(token);
 		handler = new Handler();
 		stream_request = new StreamRequest(this);
 	}
 		
 	public void setAppContext(Context appContext) {
 		this.appContext = appContext;
 	}
 
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
 			if (Build.VERSION.SDK_INT >= 11) {
 				refreshTimeline();
 			} else {
 				refreshTimelinePreAPI11();
 			}
 		}
 		getMaxReadIDs();
 	}
 	
 	public void signRequest(OAuthRequest request) {
 		service.signRequest(getToken(), request);
 	}
 	
 	public void stopStream() {
 		stream_request.stop(false);
 	}
 	
 	@TargetApi(11)
 	private void refreshTimeline() {
 //		if (overallTasksRunning == 0) {
 //			Utils.showMainSpinner();
 //		}
 		tasksRunning = 4;
 		ThreadPoolExecutor exec = new ThreadPoolExecutor(4, 4, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(4));
 		new TimelineRefreshTask().executeOnExecutor(exec, AccessType.TIMELINE);
 		new TimelineRefreshTask().executeOnExecutor(exec, AccessType.MENTIONS);
 		new TimelineRefreshTask().executeOnExecutor(exec, AccessType.DM_RCVD);
 		new TimelineRefreshTask().executeOnExecutor(exec, AccessType.DM_SENT);
 	}
 
 	private void refreshTimelinePreAPI11() {
 //		if (overallTasksRunning == 0) {
 //			Utils.showMainSpinner();
 //		}
 		tasksRunning = 4;
 		new TimelineRefreshTask().execute(AccessType.TIMELINE);
 		new TimelineRefreshTask().execute(AccessType.MENTIONS);
 		new TimelineRefreshTask().execute(AccessType.DM_RCVD);
 		new TimelineRefreshTask().execute(AccessType.DM_SENT);
 	}
 
 	
 	private class TimelineRefreshTask extends AsyncTask<AccessType, Void, ArrayList<TimelineElement>> {
 
 		private AccessType accessType;
 		private long startTime;
 		
 		@Override
 		protected ArrayList<TimelineElement> doInBackground(AccessType... params) {
 			accessType = params[0];
 			startTime = System.currentTimeMillis();
 			switch (accessType) {
 			case TIMELINE: 
 				Log.d(LOG, "Get home timeline");
 				return api.getHomeTimeline(0, 0);
 			case MENTIONS:
 				Log.d(LOG, "Get mentions");
 				return api.getMentions(0, 0);
 			case DM_RCVD:
 				Log.d(LOG, "Get received dm");
 				return api.getReceivedDMs(0, 0);
 			case DM_SENT:
 				Log.d(LOG, "Get sent dm");
 				return api.getSentDMs(0, 0);
 			}
 			return null;
 		}
 		
 		protected void onPostExecute(ArrayList<TimelineElement> result) {
 			tasksRunning--;
 			Log.d(LOG, "Get " + accessType.toString() + " finished. Runtime: " + String.valueOf(System.currentTimeMillis() - startTime) + "ms");
 			if (accessType == AccessType.TIMELINE) {
 				mainTimeline = result;
 			} else {
 				apiResponses.add(result);
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
 			}
 			
 //			if (overallTasksRunning == 0) {
 //				Utils.hideMainSpinner();
 //			}
 
 		}
 		
 	}
 		
 	protected void parseData(ArrayList<ArrayList<TimelineElement>> responses, boolean do_clip) {
 		final long old_max_known_dm_id = max_known_dm_id;
 		Log.d(LOG, "parseData started.");
 		final ArrayList<TimelineElement> all_elements = new ArrayList<TimelineElement>();
 		long last_id = 0;
 		// remove empty arrays
 		for (int i = responses.size() - 1; i >= 0; i--) {
 			if (responses.get(i).size()==0) {
 				responses.remove(i);
 			}
 		}
 		
 		while (responses.size() > 0) {
 			Date newest_date = null;
 			int newest_index = -1;
 			for (int i = 0; i < responses.size(); i++) {
 				TimelineElement element = responses.get(i).get(0);
 				if (newest_date == null || element.getDate().after(newest_date)) {
 					newest_date = element.getDate();
 					newest_index = i;
 				}
 			}
 			TimelineElement element = responses.get(newest_index).remove(0);
 			if (responses.get(newest_index).size() == 0) {
 				/* Das primäre Element ist leer. Also brechen wir ab.
 				 * Allerdings muss vorher noch ein bisschen gearbeitet werden... */
 				responses.remove(newest_index);
 
 				if (newest_index == 0) {
 					if (max_known_tweet_id == 0) {
 						for(ArrayList<TimelineElement> array : responses) {
 							TimelineElement first_element = array.get(0);
 							if (first_element instanceof Tweet && ((Tweet) first_element).id > max_known_tweet_id) {
 								max_known_tweet_id = ((Tweet)first_element).id;
 							}
 						}
 					}
 					if (max_known_dm_id==0) {
 						for(ArrayList<TimelineElement> array : responses) {
 							TimelineElement first_element = array.get(0);
 							if (first_element instanceof DirectMessage && first_element.getID() > max_known_dm_id) {
 								max_known_dm_id = first_element.getID();
 							}
 						}
 					}
 					Log.d(LOG, "Breaking!");
 					break;
 				}
 			}
 			
 			long element_id = element.getID();
 			
 			if (element_id != last_id) {
 				if (!(element instanceof DirectMessage) || element_id>old_max_known_dm_id) {
 					all_elements.add(element);
 				}
 			}
 			last_id = element_id;
 			
 			if (element instanceof Tweet) {
 				if (element_id > max_known_tweet_id) {
 					max_known_tweet_id = element_id;
 				}
 				if (min_known_tweet_id == -1 || element_id < min_known_tweet_id) {
 					min_known_tweet_id = element_id;
 				}
 			} else if (element instanceof DirectMessage) {
 				if (element_id > max_known_dm_id) {
 					max_known_dm_id = element_id;
 				}
 				if (min_known_dm_id == -1 || element_id < min_known_dm_id) {
 					min_known_dm_id = element_id;
 				}
 			}
 		}
 		
 		Log.d(LOG, "parseData is almost done. " + all_elements.size() + " elements.");
 		handler.post(new Runnable() {
 			@Override
 			public void run() {
 				elements.addAllAsFirst(all_elements);
 			}
 		});
 	}
 
 	public void addTweet(final TimelineElement elm) {
 		Log.d(LOG, "Adding Tweet.");
 		if (elm instanceof DirectMessage) {
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
 			}
 		});
 	}
 
 	public void sendTweet(SendableTweet tweet) throws TweetSendException {
 		api.sendTweet(tweet);
 	}
 	
 
 	public void sendTweetWithPic(SendableTweet tweet) throws TweetSendException, IOException {
 		String imageHoster = appContext.getSharedPreferences(Constants.PREFS_APP, 0).getString("pref_image_hoster", "twitter");
 		if (imageHoster.equals("twitter")) {
 			
 			ContentBody picture = null;
 			File f = new File(tweet.imagePath);
 			if (f.length() <= PIC_SIZE_TWITTER) {
 				picture = new FileBody(f);
 			} else {
 				picture = new ByteArrayBody(resizeImage(f), f.getName());
 			}
 
 			api.sendTweetWithPicture(tweet, picture);
 
 		} else if(imageHoster.equals("twitpic")) {
 			// Upload pic to Twitpic
 			OAuthRequest request = new OAuthRequest(Verb.POST, Constants.TWITPIC_URI);
 			
 			MultipartEntity entity = new MultipartEntity();
 			entity.addPart("key", new StringBody(Utils.getProperty("twitpic.key")));
 			entity.addPart("message", new StringBody(tweet.text));
 			
 			File f = new File(tweet.imagePath);
 			addImageToMultipartEntity(entity, f, "media");
 //			entity.addPart("media", new FileBody(new File(picture)));
 			
 			Log.d(LOG, "Start output Stream");
 			ByteArrayOutputStream out = new ByteArrayOutputStream();
 			entity.writeTo(out);
 			Log.d(LOG, "Finish output Stream");
 			request.addPayload(out.toByteArray());
 			request.addHeader(entity.getContentType().getName(), entity.getContentType().getValue());
 			
 			OAuthRequest oauth_request = new OAuthRequest(Verb.GET, "https://api.twitter.com/1/account/verify_credentials.json");
 			signRequest(oauth_request);
 			request.addHeader("X-Auth-Service-Provider", "https://api.twitter.com/1/account/verify_credentials.json");
 			request.addHeader("X-Verify-Credentials-Authorization", oauth_request.getHeaders().get("Authorization"));
 			
 			Log.d(LOG, "Send Twitpic");
 			Response response = request.send();
 			Log.d(LOG, "Finished Send Twitpic");
 			
 			// Handle response
 			// sendTweet with pic-URL
 			if (response.isSuccessful()) {
 				String twitpicURL = JSON.parseObject(response.getBody()).getString("url");
 				Log.d(LOG, "Send Tweet with Twitpic");
 				tweet.imagePath = null;
 				tweet.text += " " + twitpicURL;
 				sendTweet(tweet);
 				Log.d(LOG, "Finished Send Tweet with Twitpic");
 			}
 		} else {
 			//TODO: Exception?
 		}
 	}
 	
 	private void addImageToMultipartEntity(MultipartEntity entity, File imageFile, String key) throws IOException {
 		if (imageFile.length() <= PIC_SIZE_TWITTER) {
 			entity.addPart(key, new FileBody(imageFile));
 		} else {
 			entity.addPart(key, new ByteArrayBody(resizeImage(imageFile), imageFile.getName()));
 		}
 	}
 	
 	private byte[] resizeImage(File file) throws IOException {
 		Log.d(LOG, "Before resizeFile: " + file.length());
 		int scale = (int) (file.length() / PIC_SIZE_TWITTER);
 //		if(Integer.bitCount(scale) > 1) {
 			scale = 2 * Integer.highestOneBit(scale);
 //		}
 		Log.d(LOG, "scale: " + scale);
 		BitmapFactory.Options opt = new BitmapFactory.Options();
 		opt.inSampleSize = scale;
 		Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, opt);
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		if(file.getName().endsWith(".png")) {
 			bitmap.compress(Bitmap.CompressFormat.PNG, 0, out);
 		} else {
 			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
 		}
 		byte[] bytes = out.toByteArray();
 		Log.d(LOG, "After resizeFile: " + bytes.length);
 		return bytes;
 	}
 
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
 	
 	public void getMaxReadIDs() {
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				OAuthRequest oauth_request = new OAuthRequest(Verb.GET, Constants.URI_VERIFY_CREDENTIALS);
 				signRequest(oauth_request);
 				
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
 		
 		elements.notifyDataSetChanged();
 	}
 	
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
 				OAuthRequest oauth_request = new OAuthRequest(Verb.GET, Constants.URI_VERIFY_CREDENTIALS);
 				signRequest(oauth_request);
 				
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
 		
 		elements.notifyDataSetChanged();
 	}
 	
 	public long getMaxReadTweetID() {
 		return max_read_tweet_id;
 	}
 	
 	public TimelineElementAdapter getElements() {
 		return elements;
 	}
 
 	public Token getToken() {
 		return token;
 	}
 
 	public void setUser(User user) {
 		this.user = user;
 	}
 	
 	public User getUser() {
 		return user;
 	}
 
 	public void persistTweets(Context context) {
 		File dir;
 		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
 			dir = android.os.Environment.getExternalStorageDirectory();
 		} else {
 			dir = context.getCacheDir();
 		}
 		
 		dir = new File(dir.getPath() + File.separator + "Geotweeter" + File.separator + "timelines");
 		
 		if (!dir.exists()) {
 			dir.mkdirs();
 		}
 		
 		ArrayList<TimelineElement> last_tweets = new ArrayList<TimelineElement>(50);
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
 	
 	public void loadPersistedTweets(Context context) {
 		String fileToLoad = null;
 		String suffix = File.separator + "Geotweeter" + File.separator + "timelines" + File.separator + String.valueOf(getUser().id);
 		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
 			String file = android.os.Environment.getExternalStorageDirectory().getPath() + suffix;
 			if (new File(file).exists()) {
 				fileToLoad = file;
 			}
 		}
 		
 		if (fileToLoad == null) {
 			String file = context.getCacheDir().getPath() + suffix;
 			if (new File(file).exists()) {
 				fileToLoad = file;
 			}
 		}
 		
 		if (fileToLoad == null) return;
 		
 		ArrayList<TimelineElement> tweets;
 		try {
 			FileInputStream fin = new FileInputStream(fileToLoad);
 			ObjectInputStream ois = new ObjectInputStream(fin);
 			tweets = (ArrayList<TimelineElement>) ois.readObject();
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
 	}
 }
