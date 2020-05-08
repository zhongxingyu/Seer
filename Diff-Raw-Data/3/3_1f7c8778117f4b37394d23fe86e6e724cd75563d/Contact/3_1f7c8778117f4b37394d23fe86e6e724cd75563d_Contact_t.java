 package com.codingtest.wdc.model;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 public class Contact {
 	
 	private static final String TAG = Contact.class.getSimpleName();
 	
 	private final JSONObject holder;
 	
 	public Contact(JSONObject holder) {
 		this.holder = holder;
 	}
 	
 	public String getName() {
 		return getStringPropertySafe("Name", "No Name");
 	}
 	
 	public String getId() {
 		return getStringPropertySafe("Id", "No Id");
 	}
 	
 	public String getAccount() {
 		String value = "No Account name";
 		try {
 			value = holder.getJSONObject("Account").getString("Name");
 		} catch (JSONException e) {
 			Log.e(TAG, "Error getting contact name", e);
 		}
 		
 		return value;
 	}
 	
 	public String getTitle() {
 		return getStringPropertySafe("Title", "No Title");
 	}
 	
 	public String getEmail() {
 		return getStringPropertySafe("Email", "No Email");
 	}
 	
 	public String getPhone() {
 		return getStringPropertySafe("Phone", "No Phone");
 	}
 	public String getQuestion1() {
 		return getStringPropertySafe("Question_1__c", "No Question1");
 	}
 	public String getQuestion2() {
 		return getStringPropertySafe("Question_2__c", "No Question2");
 	}
 	public String getQuestion3() {
 		return getStringPropertySafe("Question_3__c", "No Question3");
 	}
 
 	private String getStringPropertySafe(String prop, String defValue) {
 		String value = defValue;
 		try {
 			value = holder.getString(prop);
 		} catch (JSONException e) {
 			Log.e(TAG, "Error getting contact name", e);
 		}
		if (value.equals("null"))
			value = "";
 		return value;
 	}
 }
