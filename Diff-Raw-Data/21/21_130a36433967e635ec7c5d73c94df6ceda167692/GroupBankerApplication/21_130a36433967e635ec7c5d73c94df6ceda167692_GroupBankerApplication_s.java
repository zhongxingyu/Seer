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
	
		try {
 		mPrefs = getSharedPreferences(Constants.preferences, 0);
		this.userFBID = mPrefs.getString("userfbid", null);
		this.userName = mPrefs.getString("username", null);
		} catch (Exception e)	{
			Log.e(TAG, "Problem in mPrefs = " + e.toString());
		}
 	}
 
 	public FriendsDbAdapter getFriendsDbAdapter()	{
 		return this.friendsDbHelper ;
 	}
 
 	public String getUserFBID() {
 		return this.userFBID;
 	}
 
 	public String getUserName() {
		Log.v(TAG, "Username Getting returned:" + userName);
 		return this.userName;
 	}
 	
 }
