 package com.htb.cnk.data;
 
 import android.util.Log;
 
 import com.htb.cnk.lib.Http;
 import com.htb.constant.Server;
 
 public class UserData {
 	public final static int PWD_NETWORK_ERR = -1;
 	public final static int PWD_INCORRECT = -2;
 
 	static protected String mId;
 	static protected String mName;
 	static protected String mPwd;
 
 	public static void clean() {
 		mName = null;
 		mPwd = null;
 	}
 
 	public static String getUserName() {
 		return mName;
 	}
 
 	public static String getUserPwd() {
 		return mPwd;
 	}
 
 	public static void setUserName(String name) {
 		UserData.mName = name;
 	}
 
 	public static void setUserPwd(String pwd) {
 		UserData.mPwd = pwd;
 	}
 	
 	public static String getUID() {
 		return mId;
 	}
 
 	// TODO need parameter permission
 	public static int compare() {
 		String userPwd = Http.get(Server.GET_PWD, "UNAME=" + UserData.mName);
 		if (userPwd == null) {
 			return PWD_NETWORK_ERR;
 		}
 		Log.d("pwd", "userPwd:" + userPwd);
 		String userPerminssion = Http.get(Server.GET_PERMINSSION, "UNAME="
 				+ UserData.mName);
 		if (userPerminssion == null) {
 			return PWD_NETWORK_ERR;
 		}
 		int start = userPerminssion.indexOf("[");
 		int end = userPerminssion.indexOf("]");
 
 		if ((start < 0) || (end < 0)) {
 			Log.e("userPerminssion", "userPerminssion is " + userPerminssion);
 			return -1;
 		}
 
 		String userPerminssionRet = userPerminssion.subSequence(start + 1, end)
 				.toString();
 		if (userPerminssionRet.length() <= 0) {
 			Log.e("userPermission", "userPermission length < 0");
 			return -1;
 		}
 
 		String pwd[] = userPwd.split(",");
 		
 		mId = pwd[0];
		//TODO verify Permission
		if (pwd[1].equals(UserData.mPwd)) {
 			return 1;
 		} else {
 			Log.e("compare", "userPwd.error:" + userPwd + " userPermission:"
 					+ userPerminssionRet);
 			return PWD_INCORRECT;
 		}
 
 	}
 	
 
 	
 	public static void debugMode() {
 		mId = "1";
 		mName = "admin";
 	}
 }
