 /*
  * Copyright (C) 2008 The Android Open Source Project
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
 
 package com.android.launcher.catalogue;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.util.Log;
 import android.widget.TextView;
 import com.android.launcher.R;
 
 public class AppGrpUtils {
 	private final static String GRP_INIT_TAG = "GRP_INIT_TAG";
 	private final static boolean DBG = false;
 	private final static String TAG = "AppGrpUtils";
 	// We have fixed APP_GROUP_SIZE slots for group, whether they get enabled
 	// depend on appGrpIndex.
 	//
 	public static final int APP_GROUP_SIZE = 4; //make sure it is less than 9. should more than enough.
 	
 	//  schema:  
 	//  GRP_INIT_TAG, "0/3/1/4/" grp 0,3,1,4 is enabled. sequence is 0,3,1,4 
 	//  "GrpName"+i,"grp name".     name for group i. 
 	private static SharedPreferences appGrpIndex;
 	private static String grpIndex=null;
 	public  static String[] grpValue=null;
 	public  static boolean grpMask[] = new boolean[APP_GROUP_SIZE];
 
 	// predefined groups.
 	private final static String[] multimedia_apps = {
 			"com.android.browser/com.android.browser.BrowserActivity",
 			"com.cooliris.media/com.cooliris.media.Gallery",
 			"com.android.music/com.android.music.MusicBrowserActivity" };
 
 	static final String[] getAppForDefault(int i) {
 		String[] ret = null;
 		switch (i) {
 		case 0:
 			ret = multimedia_apps;
 			break;
 		}
 		return ret;
 	}
 
 	static int currentGrp = -1;
 	public static SharedPreferences curAppGrp = null;
 	private static final String APP_GROUP_PREFS_PREFIX = "APP_GROUP_PREF_";
 
 	private static final SharedPreferences appGrpSettings[] = new SharedPreferences[APP_GROUP_SIZE];
 	
 	//do not optimize the code, but I believe it is not necessary.
 	static private void loadGrpArray() {
 		if (null == grpIndex) forceLoadGrpArray();
 	}
 	
 	static private void forceLoadGrpArray() {
 		grpIndex = appGrpIndex.getString(GRP_INIT_TAG, "");
 		grpValue = grpIndex.split("/");
 		for (int i=0;i<APP_GROUP_SIZE;i++){
 			grpMask[i] = false; 
 		}
 		for (String g: grpValue) {
 			try {
 			    int i = new Integer(g).intValue() % 10; //make sure it <10
 			    grpMask[i] = true;
 			} catch (Exception e) {}
 		}
 	}
 	
 	public static int getGrpNumber(int i) {
 		//translate the group index from Logic to Storage. 
 		// like 4/2/1/3/0, all is -1 
 		//  0->4,1->2 2->1 
 		if ((i < 0) || (i >= APP_GROUP_SIZE)) return -1;
 		loadGrpArray();
 		if (i>=grpValue.length) return -1;
 		String s = grpValue[i];
 		try {
 			return new Integer(s).intValue();
 		} catch (Exception e) {}
 		return -1;
 	}
 	
 	static public String getGrpTextFromDB(int i) {
 		String ret=null;
 		int index = AppGrpUtils.getGrpNumber(i);
 		if ((index >= 0) && (index < APP_GROUP_SIZE)) {
 			ret = appGrpIndex.getString("GrpName"+index, null);
 		}
 		return ret;
 	}
 
 	static public final boolean checkGrp(int cGrp) // check whether appGrp is enabled.
 	{
 		if ((cGrp < 0) || (cGrp >= APP_GROUP_SIZE))
 			return false;	
 		loadGrpArray();		
 		return grpMask[cGrp];
 	}
 	
 	static private final int getFirstValidGrp() {
 		int ret = -1;
 		loadGrpArray();
 		
 		for (int i=0;i<APP_GROUP_SIZE;i++){
 			if (!grpMask[i]) {
 				ret = i;
 				break;
 			}
 		}
 		return ret;
 	}
 	
 	static public final boolean hasValidGrp() {
 		loadGrpArray();
 		
 		for (int i=0;i<APP_GROUP_SIZE;i++){
 			if (!grpMask[i]) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	static public final void checkAndDisableGrp() //disable app Grp
 	{
 		int cGrp = AppGrpUtils.currentGrp;
 		if ((cGrp < 0) || (cGrp >= APP_GROUP_SIZE))
 			return; // invalid index, do nothing.
 		
 		loadGrpArray();
 		String sGrp = ""+cGrp;
 		
 		StringBuilder sb = new StringBuilder();
 		
 		for (String g : grpValue) {
 			if (!g.equals(sGrp)) {
 				sb.append(g);
 				sb.append("/");
 			}
 		}
 		SharedPreferences.Editor editor = appGrpIndex.edit();
 		editor.putString(GRP_INIT_TAG, sb.toString());
 		editor.commit();
 		forceLoadGrpArray();
 	}
 	
 	static public final void checkAndInitGrp(String grpName) {
 		loadGrpArray();
 		int grp = getFirstValidGrp();
		Log.v("----","  "+grp);
 		if (!(grp < 0) || (grp >= APP_GROUP_SIZE)) {
 			SharedPreferences.Editor editor = appGrpIndex.edit();
 			editor.putString(GRP_INIT_TAG, grpIndex + grp +"/");
 			editor.putString("GrpName"+grp, grpName);			
 			editor.commit();
 			forceLoadGrpArray();
 		}
 	}
 
 	private static SharedPreferences getAppGrp(int i) {
 		if ((i < 0) || (i >= APP_GROUP_SIZE))
 			return null;
 		return appGrpSettings[i];
 
 	}
 
 	static public void setCurGrp(int i) {
 		currentGrp = getGrpNumber(i);
 		curAppGrp = getAppGrp(currentGrp);
 	}
 
 	public static SharedPreferences getCurAppGrp() {
 		return curAppGrp;
 	}
 
 	static public final void init(Activity context) {
 		for (int i = 0; i < APP_GROUP_SIZE; i++) {
 			appGrpSettings[i] = context.getSharedPreferences(
 					APP_GROUP_PREFS_PREFIX + i, 0);
 		}
 		appGrpIndex = context.getSharedPreferences(APP_GROUP_PREFS_PREFIX
 				+ "Index", 0);
		//test code
 		SharedPreferences.Editor editor = appGrpIndex.edit();
 		//editor.putString(GRP_INIT_TAG, "1/3/2/");
 		editor.putString(GRP_INIT_TAG, "2/");
 		editor.putString("GrpName3","MultiMedia");			
 		editor.putString("GrpName2","Tools");			
 		editor.putString("GrpName1","Game");			
 		editor.putString("GrpName0","text+0");	
 		editor.commit();
 		//test code;
		loadGrpArray();
 		setCurGrp(-1);
 	}
 
 	public static boolean checkAppInGroup(String className) {
 		boolean ret = true;
 		if (curAppGrp != null)
 			ret = curAppGrp.getBoolean(className, false);
 		return ret;
 	}
 	
 	/**
 	 * Displays the shortcut creation dialog and launches, if necessary, the
 	 * appropriate activity.
 	 */
 	static private int getDefaultGrpText(int i) {
 
 		int ret = R.string.AppGroupAll;
 
 		switch (i) {
 		case 0:
 			ret = R.string.AppGroup0;
 			break;
 		case 1:
 			ret = R.string.AppGroup1;
 			break;
 		case 2:
 			ret = R.string.AppGroup2;
 			break;
 		case 3:
 			ret = R.string.AppGroup3;
 			break;
 		}
 		return ret;
 	}
 
 	static public void setTitleView(TextView v) {
 		String grpTitle = appGrpIndex.getString("GrpName"+currentGrp, null);//getGrpTextFromDB(currentGrp);
 		if (null != grpTitle) {
 			v.setText(grpTitle);
 		} else {
 			v.setText(getDefaultGrpText(currentGrp));
 		}
 	}
 
 }
