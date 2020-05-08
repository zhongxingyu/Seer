 package com.tuit.ar.api;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import oauth.signpost.AbstractOAuthConsumer;
 
 import org.apache.http.NameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import com.tuit.ar.api.request.Options;
 import com.tuit.ar.models.Status;
 import com.tuit.ar.models.User;
 
 public class TwitterAccount implements TwitterAccountRequestsObserver {
 	static private final String PERSISTENT_FOLDER = "/data/data/com.tuit.ar/accounts/";
 	private oauth.signpost.AbstractOAuthConsumer consumer;
 	private User user;
 
 	private ArrayList<TwitterAccountObserver> observers = new ArrayList<TwitterAccountObserver>(); 
 	private ArrayList<TwitterAccountRequestsObserver> requestsObservers = new ArrayList<TwitterAccountRequestsObserver>(); 
 	private HashSet<Options> urlOnRequest = new HashSet<Options>();
 
 	public TwitterAccount(oauth.signpost.AbstractOAuthConsumer _consumer) throws Exception {
 		consumer = _consumer;
 		this.addRequestObserver(this);
 		this.requestUrl(Options.LOGIN);
 	}
 
 	public TwitterAccount(oauth.signpost.AbstractOAuthConsumer _consumer, String userId) throws Exception {
 		consumer = _consumer;
 		this.setUser(User.select("id = ? AND belongs_to_user = ?", new String[] { userId, userId }, null, null, null, "1").get(0));
 		this.addRequestObserver(this);
 		this.requestUrl(Options.LOGIN);
 	}
 
 	public void requestUrl(Options login) throws Exception { requestUrl(login, null, TwitterRequest.METHOD_GET); }
 
 	public TwitterRequest requestUrl(Options login, ArrayList<NameValuePair> params, int get) throws Exception {
 		TwitterRequest request = new TwitterRequest(this, login, params, get);
 		startedRequest(request);
 		return request;
 
 	}
 	public void upload(File photo, String message) throws Exception {
 		startedRequest(new TwitterRequestWithPhoto(this, photo, message));
 	}
 
 	public String getUsername() {
 		try {
 			return getUser().getScreenName();
 		} catch (Exception e) {
 		}
 		return null;
 	}
 
 	public void addObserver(TwitterAccountObserver observer) {
 		observers.add(observer);
 	}
 
 	public void removeObserver(TwitterAccountObserver observer) {
 		observers.remove(observer);
 	}
 
 	public void accountChanged() {
 		for (TwitterAccountObserver observer : observers) {
 			observer.accountHasChanged(this);
 		}
 	}
 
 	public void addRequestObserver(TwitterAccountRequestsObserver observer) {
 		requestsObservers.add(observer);
 	}
 
 	public void removeRequestObserver(TwitterAccountRequestsObserver observer) {
 		requestsObservers.remove(observer);
 	}
 
 	@SuppressWarnings("unchecked")
 	public void startedRequest(TwitterRequest twitterRequest) {
 		ArrayList<TwitterAccountRequestsObserver> requestsObservers = (ArrayList<TwitterAccountRequestsObserver>) this.requestsObservers.clone();
 		for (TwitterAccountRequestsObserver observer : requestsObservers) {
 			observer.requestHasStarted(twitterRequest);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public void finishedRequest(TwitterRequest twitterRequest) {
 		ArrayList<TwitterAccountRequestsObserver> requestsObservers = (ArrayList<TwitterAccountRequestsObserver>)this.requestsObservers.clone();
 		for (TwitterAccountRequestsObserver  observer : requestsObservers) {
 			observer.requestHasFinished(twitterRequest);
 		}
 	}
 
 	public void requestHasFinished(TwitterRequest request) {
 		if (request.getUrl() == Options.LOGIN) {
 			// Unauthorized! I'm invalid!!!
			if (request.getStatusCode() >= 400 && request.getStatusCode() < 500) {
 				try {
 					Twitter.getInstance().removeAccount(this);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				return;
 			}
 
 			String response = request.getResponse();
 			if (response == null) {
 				return;
 			}
 
 			try {
 				JSONObject data = new JSONObject(new JSONTokener(response));
 				this.setUser(new User(data));
 				accountChanged();
 
 				// checking this just in case the account was removed before the username was set... unexpected scenario, but possible 
 				if (Twitter.getInstance().getAccounts().contains(this)) {
 					this.serialize();
 				}
 			} catch (JSONException e) {
 			}
 		}
 	}
 
 	private void setUser(User user) {
 		this.user = user;
 		user.setBelongsToUser(user.getId());
 		user.replace();
 	}
 
 	public User getUser() {
 		return user;
 	}
 
 	public void requestHasStarted(TwitterRequest request) {
 	}
 
 	public oauth.signpost.AbstractOAuthConsumer getConsumer() {
 		return consumer;
 	}
 
 	public void serialize() {
 		User user = getUser();
 		if (user == null) return;
 
 		File folder = new File(PERSISTENT_FOLDER);
 		if (folder.exists() == false) folder.mkdirs();
 
 		String filename = PERSISTENT_FOLDER + user.getId();
 		FileOutputStream fos = null;
 		ObjectOutputStream out = null;
 		try {
 			fos = new FileOutputStream(filename);
 			out = new ObjectOutputStream(fos);
 			out.writeObject(consumer);
 			out.close();
 			fos.close();
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	static public TwitterAccount unserialize(String nickname) {
 		FileInputStream fis = null;
 		ObjectInputStream in = null;
 		AbstractOAuthConsumer consumer = null;
 		try {
 			fis = new FileInputStream(PERSISTENT_FOLDER + nickname);
 			in = new ObjectInputStream(fis);
 			consumer = (AbstractOAuthConsumer)in.readObject();
 			in.close();
 			fis.close();
 		} catch(Exception ex) {
 			ex.printStackTrace();
 			(new File(PERSISTENT_FOLDER + nickname)).delete();
 		}
 
 		if (consumer == null) return null;
 		try {
 			return new TwitterAccount(consumer, nickname);
 		} catch (Exception e) {
 			// something went wrong... returning null
 		}
 		return null;
 	}
 
 	static public ArrayList<TwitterAccount> fetchAllFromCache() {
 		File folder = new File(PERSISTENT_FOLDER);
 		String[] nicknames = folder.list(null);
 		ArrayList<TwitterAccount> accounts = new ArrayList<TwitterAccount>();
 		if (nicknames != null) {
 			for (String nickname : nicknames) {
 				TwitterAccount account = unserialize(nickname);
 				if (account != null) {
 					accounts.add(account);
 				}
 			}
 		}
 		return accounts;
 	}
 
 	public boolean delete() {
 		User user = getUser();
 		if (user == null) return false;
 
 		File folder = new File(PERSISTENT_FOLDER);
 		if (folder.exists() == false) folder.mkdirs();
 
 		String id = String.valueOf(user.getId());
 		String filename = PERSISTENT_FOLDER + id;
 		File file = new File(filename);
 		if (file.exists() == false || file.delete() == true) {
 			Status.deleteBelongsToUser(id);
 			User.deleteBelongsToUser(id);
 			return true;
 		}
 		return false;
 	}
 	
 	public void addUrlOnRequest(Options options) {
 		urlOnRequest.add(options);
 	}
 
 	public void removeUrlOnRequest(Options options) {
 		urlOnRequest.remove(options);
 	}
 	
 	public boolean hasUrlOnRequest(Options options) {
 		return urlOnRequest.contains(options);
 	}
 	
 /*	protected ConcreteTweetPhoto getTweetPhotoProfile() {
 		if (tweetPhoto != null) return tweetPhoto;  
 		ConcreteTweetPhoto tweetPhoto = new ConcreteTweetPhoto();
 
 		AbstractOAuthConsumer consumer = getConsumer();
 		String key = consumer.getToken();
 		String secret = consumer.getTokenSecret();
 		tweetPhoto.signIn(TWEET_PHOTO_API_KEY , "Twitter", true, key, secret);
 		return tweetPhoto;
 	}
 
 	public ConcreteTweetPhotoResponse upload(File photo, String text) {
 		ConcreteTweetPhoto tweetPhoto = getTweetPhotoProfile();
 		if (tweetPhoto != null) {
 			String mime = MimeTypeMap.getFileExtensionFromUrl(photo.getAbsolutePath());
 			return tweetPhoto.concreteUploadPhoto(new FileEntity(photo, mime), text, null, 0.0,0.0, "image/" + mime);
 		}
 		return null;
 	}*/
 }
