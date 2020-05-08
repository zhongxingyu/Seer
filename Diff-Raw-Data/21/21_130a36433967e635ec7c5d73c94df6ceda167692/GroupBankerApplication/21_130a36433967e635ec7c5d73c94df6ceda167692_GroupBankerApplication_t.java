 package me.rohitmishra.groupbanker;
 
 import android.app.Application;
 import android.content.SharedPreferences;
 import android.util.Log;
 
 public class GroupBankerApplication extends Application {
 	
 	private static String TAG = "GroupBankerApplication";
 	private FriendsDbAdapter friendsDbHelper ;
 	private SharedPreferences mPrefs ;
 	private String userFBID ;
 	private String userName ;
 	
 	@Override
 	public void onCreate()	{
 		friendsDbHelper = new FriendsDbAdapter(this);
 		friendsDbHelper.open() ;
		
 		mPrefs = getSharedPreferences(Constants.preferences, 0);
 	}
 
 	public FriendsDbAdapter getFriendsDbAdapter()	{
 		return this.friendsDbHelper ;
 	}
 
	/* We were earlier retrieving UserFBID and UserName from SharedPreferences in onCreate. 
	 * The bug that was coming was because GroupBankerApplication may (or always will) get initialized
	 * before other classes. It's onCreate will then assign these two variables null value.
	 */
	
 	public String getUserFBID() {
		this.userFBID = mPrefs.getString("userfbid", null);
 		return this.userFBID;
 	}
 
 	public String getUserName() {
		this.userName = mPrefs.getString("username", null);
		Log.v(TAG, "Username Getting returned:" + this.userName);
 		return this.userName;
 	}
 	
 }
