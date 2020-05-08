 /*
  * Copyright (C) 2011 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.partytv.server;
 
 import android.app.ActionBar.Tab;
 import android.app.ActionBar.TabListener;
 import android.app.Activity;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.widget.Toast;
 
 import com.dropbox.client2.DropboxAPI;
 import com.dropbox.client2.android.AndroidAuthSession;
 import com.dropbox.client2.session.AccessTokenPair;
 import com.dropbox.client2.session.AppKeyPair;
 import com.dropbox.client2.session.Session.AccessType;
 import com.example.google.tv.leftnavbar.LeftNavBar;
 import com.example.google.tv.leftnavbar.LeftNavBarService;
 
 /**
  * This class helps with setting the left-side navigation bar in an Activity's layout.
  */
 public class PanoramioLeftNavService {
 
     private static Context mContext;
 
     ///////////////////////////////////////////////////////////////////////////
     //                      Your app-specific settings.                      //
     ///////////////////////////////////////////////////////////////////////////
 
     // Replace this with your app key and secret assigned by Dropbox.
     // Note that this is a really insecure way to do this, and you shouldn't
     // ship code which contains your key & secret in such an obvious way.
     // Obfuscation is good.
     final static private String APP_KEY = "ex7ktzbeatxkxfv";
     final static private String APP_SECRET = "t4mzka6bcya5xbj";
 
     // If you'd like to change the access type to the full Dropbox instead of
     // an app folder, change this value.
     final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
     
     // You don't need to change these, leave them alone.
     final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
 
     
     public static LeftNavBar getLeftNavBar(Context context) {
         LeftNavBar bar = (LeftNavBarService.instance()).getLeftNavBar((Activity) context);
         mContext = context;
         bar.setDisplayShowTitleEnabled(false);
         bar.setDisplayUseLogoEnabled(false);
         bar.removeAllTabs();
 
         /*
         bar.addTab(bar.newTab().setText(R.string.search).setIcon(R.drawable.search)
                 .setTabListener(searchTabListener), false);
         bar.setNavigationMode(LeftNavBar.NAVIGATION_MODE_TABS);
         // bar.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.red_gradient));
         bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#222222")));
         bar.setDisplayShowHomeEnabled(true);
 	    */
         
 //        .setIcon(R.drawable.places)
         bar.addTab(bar.newTab().setText(R.string.slide_show)
                 .setTabListener(placesTabListener), false);
         bar.setNavigationMode(LeftNavBar.NAVIGATION_MODE_TABS);
        // bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#222222")));
         bar.showOptionsMenu(false);
         bar.setDisplayUseLogoEnabled(true);
         // bar.setDisplayOptions(LeftNavBar.DISPLAY_AUTO_EXPAND);
         bar.setShowHideAnimationEnabled(true);
         
         bar.addTab(bar.newTab().setText(R.string.whos_here)
                 .setTabListener(placesTabListener), false);
         bar.showOptionsMenu(false);
         bar.setDisplayUseLogoEnabled(true);
         // bar.setDisplayOptions(LeftNavBar.DISPLAY_AUTO_EXPAND);
         bar.setShowHideAnimationEnabled(true);
         
         bar.addTab(bar.newTab().setText(R.string.join_party)
                 .setTabListener(placesTabListener), false);
         bar.showOptionsMenu(false);
         bar.setDisplayUseLogoEnabled(true);
         // bar.setDisplayOptions(LeftNavBar.DISPLAY_AUTO_EXPAND);
         bar.setShowHideAnimationEnabled(true);
         
         bar.addTab(bar.newTab().setText(R.string.prev_party)
                 .setTabListener(placesTabListener), false);
         bar.showOptionsMenu(false);
         bar.setDisplayUseLogoEnabled(true);
         // bar.setDisplayOptions(LeftNavBar.DISPLAY_AUTO_EXPAND);
         bar.setShowHideAnimationEnabled(true);
         
         bar.addTab(bar.newTab().setText(R.string.settings)
                 .setTabListener(placesTabListener), false);
         bar.showOptionsMenu(false);
         bar.setDisplayUseLogoEnabled(true);
         // bar.setDisplayOptions(LeftNavBar.DISPLAY_AUTO_EXPAND);
         bar.setShowHideAnimationEnabled(true);
         
         bar.addTab(bar.newTab().setText(R.string.account)
                 .setTabListener(new TabListener() {
 					
 					public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 						// nothing
 					}
 					
 					public void onTabSelected(Tab tab, FragmentTransaction ft) {
 		                // This logs you out if you're logged in, or vice versa
 						logOut();
 					}
 					
 				    private void logOut() {
 				    	Toast.makeText(mContext, "log ou", Toast.LENGTH_SHORT).show();
 			    		// We create a new AuthSession so that we can use the Dropbox API.
 				    	AndroidAuthSession session = buildSession();
 				    	DropboxAPI<AndroidAuthSession> api = new DropboxAPI<AndroidAuthSession>(session);
         
 				        // Remove credentials from the session
 				        api.getSession().unlink();
 				
 				        // Clear our stored keys
 				        clearKeys();
 				    }
 
 				    private void clearKeys() {
 				    	if(mContext!=null) {
 					        SharedPreferences prefs = mContext.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
 					        Editor edit = prefs.edit();
 					        edit.clear();
 					        edit.commit();
 				    	}
 				    }				    
 				    private AndroidAuthSession buildSession() {
 				        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
 				        AndroidAuthSession session;
 				
 				        String[] stored = getKeys();
 				        if (stored != null) {
 				            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
 				            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
 				        } else {
 				            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
 				        }
 				
 				        return session;
				    }
				    
				    private String[] getKeys() {
				    	if(mContext!=null) {
					    	
					        SharedPreferences prefs = mContext.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
					        String key = prefs.getString(ACCESS_KEY_NAME, null);
					        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
					        if (key != null && secret != null) {
					        	String[] ret = new String[2];
					        	ret[0] = key;
					        	ret[1] = secret;
					        	return ret;
					        } else {
					        	return null;
					        }
				    	}
				    	
				    	return null;
 				    }				    
 					public void onTabReselected(Tab tab, FragmentTransaction ft) {
 						this.onTabSelected(tab, ft);
 					}
 				}), false);
         bar.showOptionsMenu(false);
         bar.setDisplayUseLogoEnabled(true);
         // bar.setDisplayOptions(LeftNavBar.DISPLAY_AUTO_EXPAND);
         bar.setShowHideAnimationEnabled(true);
         
         
         
         return bar;
     }
     
 
 
     private static LeftNavBar.TabListener searchTabListener = new LeftNavBar.TabListener() {
         public void onTabSelected(Tab tab, FragmentTransaction ft) {
             ((Activity) mContext).onSearchRequested();
         }
 
         public void onTabUnselected(Tab tab, FragmentTransaction ft) {
         }
 
         public void onTabReselected(Tab tab, FragmentTransaction ft) {
         }
     };
 
     private static LeftNavBar.TabListener placesTabListener = new LeftNavBar.TabListener() {
         public void onTabSelected(Tab tab, FragmentTransaction ft) {
 
             Intent intent = new Intent();
             intent.setClass(mContext, SlideShowActivity.class);
             intent.putExtra("index", 0);
             mContext.startActivity(intent);
         }
 
         public void onTabUnselected(Tab tab, FragmentTransaction ft) {
         }
 
         public void onTabReselected(Tab tab, FragmentTransaction ft) {
         }
     };
 }
