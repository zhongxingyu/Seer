 package com.themosec;
 
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 
 import com.activeandroid.Model;
 import com.activeandroid.annotation.Column;
 import com.activeandroid.annotation.Table;
 import com.activeandroid.query.Select;
 import com.loopj.android.http.JsonHttpResponseHandler;
 import com.loopj.android.http.RequestParams;
 
 @Table(name = "Users")
 public class User extends Model {
 	private static final String sIdJsonKey = "id";
 	private static final String sFullNameJsonKey = "full_name";
 	private static final String sEmailAddressJsonKey = "email_address";
 	
 	private static final String SIGN_IN_ROUTE = "/user_session.json";
 	
 	private static final String sUserSessionEmailAddressParameterName = "user_session[email_address]";
 	private static final String sUserSessionPasswordParameterName = "user_session[password]";
 	
 	@Column(name = "BackendId")
 	private long mBackendId;
 	
 	@Column(name = "FullName")
 	private String mFullName;
 	
 	@Column(name = "EmailAddress")
 	private String mEmailAddress;
 	
 	private String mPassword;
 	
 	public User() {
 		this(-1, null, null, null);
 	}
 	
 	public User(long backendId) {
 		this(backendId, null, null, null);
 	}
 	
 	public User(long backendId, String fullName) {
 		this(backendId, fullName, null, null);
 	}
 	
 	public User(long backendId, String fullName, String emailAddress) {
 		this(backendId, fullName, emailAddress, null);
 	}
 	
 	public User(long backendId, String fullName, String emailAddress, String password) {
 		mBackendId = backendId;
 		mFullName = fullName;
 		mEmailAddress = emailAddress;
 		mPassword = password;
 	}
 	
 	public long getBackendId() {
 		return mBackendId;
 	}
 	
 	public void setBackendId(long backendId) {
 		mBackendId = backendId;
 	}
 	
 	public String getFullName() {
 		return mFullName;
 	}
 	
 	public void setFullName(String fullName) {
 		mFullName = fullName;
 	}
 	
 	public String getEmailAddress() {
 		return mEmailAddress;
 	}
 	
 	public void setEmailAddress(String emailAddress) {
 		mEmailAddress = emailAddress;
 	}
 	
 	public String getPassword() {
 		return mPassword;
 	}
 	
 	public void setPassword(String password) {
 		mPassword = password;
 	}
 	
 	public void signInInBackground(Context context, final MosecJsonResponseHandler responseHandler) {
 		RequestParams parameters = new RequestParams();
 		
 		parameters.put(sUserSessionEmailAddressParameterName, mEmailAddress);
 		parameters.put(sUserSessionPasswordParameterName, mPassword);
 
 		parameters.put(Phone.UID_PARAMETER_NAME, Phone.getInstance(context).getUid());
		parameters.put(Phone.OPERATING_SYSTEM_PARAMETER_NAME, Phone.OPERATING_SYSTEM);
 		
 		MosecClient.post(SIGN_IN_ROUTE, parameters, new JsonHttpResponseHandler() {
 			@Override
 			public void onSuccess(JSONObject userJson) {
 				try {
 					long backendId = userJson.getLong(sIdJsonKey);
 					String fullName = userJson.getString(sFullNameJsonKey);
 					String emailAddress = userJson.getString(sEmailAddressJsonKey);
 
 					User user = new User();
 
 					user.setBackendId(backendId);
 					user.setFullName(fullName);
 					user.setEmailAddress(emailAddress);
 					
 					user.save();
 				} catch(JSONException exception) {
 					
 				}
 				
 				responseHandler.onSuccess(userJson);
 			}
 			
 			@Override
 			public void onFailure(Throwable throwable, JSONArray errorsJson) {
 				responseHandler.onFailure(throwable, errorsJson);
 			}
 		});
 	}
 	
 	public void logout() {
 		MosecClient.clearCookies();
 
 		deleteAll();
 	}
 	
 	public static List<User> getAll() {
 		List<User> users = new Select().from(User.class).execute();
 		
 	    return users;
 	}
 	
 	public static void deleteAll() {
 		delete(User.class);
 	}
 	
 	public static User getCurrentUser() {
 		List<User> users = getAll();
 		
 		if(users.size() > 0) {
 			User user = users.get(0);
 			
 			return user;
 		} else {
 			return null;
 		}
 	}
 }
