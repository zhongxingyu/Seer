 package com.tuit.ar.api;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import oauth.signpost.AbstractOAuthConsumer;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.entity.FileEntity;
 
 import android.webkit.MimeTypeMap;
 
 import com.tuit.ar.api.request.Options;
 import com.tweetphoto.api.ConcreteTweetPhoto;
 import com.tweetphoto.api.ConcreteTweetPhotoResponse;
 
 public class TwitterRequestWithPhoto extends TwitterRequest {
 	static private final String TWEET_PHOTO_API_KEY = "d21221d4-bbdd-47d7-831e-6e3eab2cc86b";
 	protected AbstractOAuthConsumer consumer;
 
 	private ConcreteTweetPhoto tweetPhoto;
 	private File photo;
 	private String message;
 
 	public TwitterRequestWithPhoto(TwitterAccount account, File photo, String message) throws Exception {
 		super(account, null, null, Method.POST);
 		consumer = account.getConsumer();
 		this.photo = photo;
 		this.message = message;
 		this.run(null, null, null);
 	}
 
 	protected void run(final Options url, final ArrayList <NameValuePair> nvps, final Method method) {
 		if (consumer == null) return;
 		(new Thread() {
 			public void run() {
 				upload(photo, message);
 				handler.post(runnable);
 			}
 		}).start();
 	}
 
 
 	protected ConcreteTweetPhoto getTweetPhotoProfile() {
 		if (tweetPhoto != null) return tweetPhoto;  
 		ConcreteTweetPhoto tweetPhoto = new ConcreteTweetPhoto();
 
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
 	}
 }
