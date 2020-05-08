 package org.touchirc.db;
 
 import android.provider.BaseColumns;
import android.widget.EditText;
import android.widget.TextView;
 
 public interface DBConstants extends BaseColumns {
 	
 	public static final String SERVER_TABLE_NAME = "servers";
 	public static final String SERVER_ID		 = "id";
 	public static final String SERVER_TITLE 	 = "title";
     public static final String SERVER_HOST 		 = "host";
     public static final String SERVER_PORT 		 = "port";
     public static final String SERVER_PASSWORD 	 = "password";
     public static final String SERVER_USE_SSL 	 = "useSSL";
     public static final String SERVER_CHARSET 	 = "charset";
     
     public static final String[] SERVER_ALL = {
         SERVER_ID,
         SERVER_TITLE,
         SERVER_HOST,
         SERVER_PORT,
         SERVER_PASSWORD,
         SERVER_USE_SSL,
         SERVER_CHARSET
     };
     
     public static final String PROFILE_TABLE_NAME 		= "profiles";
 	public static final String PROFILE_ID				= "id";
 	public static final String PROFILE_NAME				= "name";
    public static final String PROFILE_FIRST_NICKNAME 	= "first nickname";
    public static final String PROFILE_SCD_NICKNAME 	= "second nickname";
    public static final String PROFILE_THIRD_NICKNAME 	= "third nickname";
     public static final String PROFILE_USERNAME 		= "username";
     public static final String PROFILE_REALNAME 		= "realname";
    public static final String PROFILE_SERVER_LIST 		= "server list";
     
     public static final String[] PROFILE_ALL = {
     	PROFILE_ID,
     	PROFILE_FIRST_NICKNAME,
     	PROFILE_SCD_NICKNAME,
     	PROFILE_THIRD_NICKNAME,
     	PROFILE_USERNAME,
     	PROFILE_REALNAME,
     	PROFILE_SERVER_LIST
     };
     
     
 }
