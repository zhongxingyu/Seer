 package com.w2e.firehose.resources;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 
 import org.json.JSONObject;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 
 public class User {
 
 	private static final int IO_BUFFER_SIZE = 2048;
 	private long id;
 	private String name;
 	private String screenName;
 	private String location;
 	private String profileImageUrl;
 	private String url;
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public long getId() {
 		return id;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setScreenName(String screenName) {
 		this.screenName = screenName;
 	}
 
 	public String getScreenName() {
 		return screenName;
 	}
 
 	public void setLocation(String location) {
 		this.location = location;
 	}
 
 	public String getLocation() {
 		return location;
 	}
 
 	public void setProfileImageUrl(String profileImageUrl) {
 		this.profileImageUrl = profileImageUrl;
 	}
 
 	public String getProfileImageUrl() {
 		return profileImageUrl;
 	}
 
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	public String getUrl() {
 		return url;
 	}
 	
 	public Bitmap getAvatar() {
 		Bitmap bitmap = null;
 		InputStream in = null;
 		OutputStream out = null;
 		
 		try {
 			in = new BufferedInputStream(new URL(profileImageUrl).openStream(), IO_BUFFER_SIZE);
 
 			final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
 			out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
 			byte[] b = new byte[IO_BUFFER_SIZE];
 			int read;
 			while ((read = in.read(b)) != -1) {
 				out.write(b, 0, read);
 			}
 			out.flush();
 
 			final byte[] data = dataStream.toByteArray();
 			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
 		} catch (IOException e) {
 			Log.e(getClass().getName(), "Could not load image.", e);
 		} finally {
 			try {
 				if (in != null) {
 					in.close();
 				}
 				if (out != null) {
 					out.close();
 				}
 			} catch (IOException ioe) {
 				Log.e(getClass().getName(), "Couldn't close stream after trying to load photo.", ioe);
 			}
 		}
 
 		Log.d(getClass().getName(), "Loaded image for " + url);
 		return bitmap;
 	}
 	
 	public static User fromJSON(String json) {
 		try {
 			JSONObject jsonObject = new JSONObject(json);
 			return fromJSON(jsonObject);
 		} catch (Exception e) {
 			Log.e(User.class.getName(), "Error deserializing:", e);
 		}
 		return null;
 	}
 	
 	public static User fromJSON(JSONObject jsonObject) {
 		if (null == jsonObject) {
 			return null;
 		}
 
 		try {
 			User user = new User();
 			user.id = jsonObject.getLong("id");
 			user.name = stringOrNull(jsonObject.getString("name"));
 			user.screenName = stringOrNull(jsonObject.getString("screen_name"));
 			user.location = stringOrNull(jsonObject.getString("location"));
 			user.profileImageUrl = stringOrNull(jsonObject.getString("profile_image_url"));
 			user.url = stringOrNull(jsonObject.getString("url"));
 			return user;
 		} catch (Exception e) {
 			Log.e(User.class.getName(), "Error deserializing:", e);
 		}
 		return null;
 	}
 	
 	public static String stringOrNull(String s) {
		if (null == s) {
 			return null;
 		} else {
 			return s;
 		}
 	}
 
 }
