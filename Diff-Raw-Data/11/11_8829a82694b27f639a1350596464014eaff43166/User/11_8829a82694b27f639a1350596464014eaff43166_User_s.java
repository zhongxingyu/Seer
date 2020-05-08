 package com.github.remomueller.tasktracker.android;
 
 import android.content.Context;
 import java.util.HashMap;
 import android.util.Log;
 
 import com.github.remomueller.tasktracker.android.util.DatabaseHandler;
 
 // Debug
 import android.util.Log;
 
 public class User {
     private static final String TAG = "TaskTrackerAndroid";
 
     public int id = 0;
     public String first_name = "";
     public String last_name = "";
     public String email = "";
     public String password = "";
     public String cookie = "";
 
     public String site_url = "";
     public String authentication_token = "";
 
     private transient DatabaseHandler db;
 
     // Given a Context, return user from database
     public User(Context context){
         db = new DatabaseHandler(context);
         HashMap<String,String> user;
         user = db.getUserDetails();
         // if(user.get("id") != null && user.get("id") != "") // Put in with migration 3
         //     id = Integer.parseInt(user.get("id"));
 
        if(user.get("cookie") != null && user.get("cookie") != "")
             id = Integer.parseInt(user.get("cookie"));

 
 
         first_name = user.get("first_name");
         last_name = user.get("last_name");
         email = user.get("email");
         password = user.get("password");
 
         site_url = user.get("site_url");
         authentication_token = user.get("authentication_token");
 
 
         // if(first_name != null) Log.d(TAG, "First Name: " + first_name);
         // if(last_name != null) Log.d(TAG, "Last Name: " + last_name);
         // if(email != null) Log.d(TAG, "Email: " + email);
         // if(site_url != null) Log.d(TAG, "Site URL: " + site_url);
     }
 
 
     public void print(){
         Log.d(TAG, "USER id: " + Integer.toString(id));
         if(first_name != null) Log.d(TAG, "USER first_name: " + first_name);
         if(last_name != null) Log.d(TAG, "USER last_name: " + last_name);
         if(email != null) Log.d(TAG, "USER email: " + email);
         if(site_url != null) Log.d(TAG, "USER site_url: " + site_url);
         if(authentication_token != null) Log.d(TAG, "USER authentication_token: " + authentication_token);
     }
 
     /**
      * Function get Login status
      * */
     public boolean isUserLoggedIn(){
         if(db == null) return false;
         int count = db.getRowCount();
         if(count > 0){
             return true;
         }
         return false;
     }
 
     /**
      * Function to logout user
      * Reset Database
      * */
     public boolean logoutUser(){
         if(db != null) db.resetLogin(email, site_url);
         return true;
     }
 
     public String name(){
         String result = "";
         if(first_name != null && first_name != "" && last_name != null && last_name != "")
             result = first_name + " " + last_name;
         else if(first_name != null && first_name != "")
             result = first_name;
         else if(last_name != null && last_name != "")
             result = last_name;
         return result;
     }
 
 }
