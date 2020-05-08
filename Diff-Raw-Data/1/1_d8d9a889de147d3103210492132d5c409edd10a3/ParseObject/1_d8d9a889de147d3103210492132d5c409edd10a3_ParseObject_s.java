 package com.brif.nix.parse;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * The <b>ParseObject</b> is a local representation of data that can be saved
  * and retrieved from the Parse cloud.
  * 
  * <p>
  * The basic workflow for creating new data is to construct a new ParseObject,
  * use put() to fill it with data, and then use save() to persist to the databa
  * 
  * <p>
  * The basic workflow for accessing existing data is to use a ParseQuery to
  * specify which existing data to retrieve.
  * 
  * @author js
  * 
  */
 public class ParseObject {
 	private static final String FIELD_CREATED_AT = "createdAt";
 	private static final String FIELD_UPDATED_AT = "updatedAt";
 
 	/**
 	 * Creates a new ParseObject based upon a class name. If the class name is a
 	 * special type (e.g. for ParseUser), then the appropriate type of
 	 * ParseObject is returned.
 	 * 
 	 * @param className
 	 * 
 	 * @return
 	 */
 	public static ParseObject create(String className) {
 		return new ParseObject(className);
 	}
 
 	private final static Set<String> excludeObjectId = new LinkedHashSet<String>(
 			1);
 
 	public static Set<String> excludeObjectId() {
 		if (excludeObjectId.size() == 0) {
 			excludeObjectId.add("objectId");
 		}
 		return excludeObjectId;
 
 	}
 
 	private String mClassName;
 
 	private Hashtable<String, Object> mData;
 	private String charset;
 
 	/**
 	 * Constructs a new ParseObject with no data in it. A ParseObject
 	 * constructed in this way will not have an objectId and will not persist to
 	 * the database until save() is called.
 	 * 
 	 * Class names must be alphanumerical plus underscore, and start with a
 	 * letter. It is recommended to name classes in CamelCaseLikeThis.
 	 * 
 	 * @param theClassName
 	 *            The className for this ParseObject.
 	 */
 	public ParseObject(String theClassName) {
 		mClassName = theClassName;
 		mData = new Hashtable<String, Object>();
 
 	}
 
 	/**
 	 * Used to support a query that returns an object from Parse encoded with
 	 * JSON. This constructor will create itself from the JSON. This is probably
 	 * a poor method, especially if the JSON is mal-formed. A better approach
 	 * would be move the handling to ParseQuery, where it alone would
 	 * understands query responses from Parse.
 	 * 
 	 * @param theClassName
 	 *            The className for this ParseObject
 	 * @param json
 	 *            JSON encoded response from Parse corresponding to a
 	 *            ParseObject
 	 */
 	ParseObject(String theClassName, JSONObject json) {
 		mClassName = theClassName;
 		mData = new Hashtable<String, Object>();
 
 		for (String name : JSONObject.getNames(json)) {
 			try {
 				/*
 				 * Check for data types here. If the 'value' is JSONObject then
 				 * there's additional data type information.
 				 */
 				if (json.get(name).getClass().getName()
 						.equals("org.json.JSONObject")) {
 					JSONObject oType = (JSONObject) json.get(name);
 
 					if (oType.get("__type").equals("Pointer")) {
 						System.out.println("Found Pointer");
 						put(name,
 								new ParsePointer(oType.getString("className"),
 										oType.getString("objectId")));
 					} else if (oType.get("__type").equals("Date")) {
 						throw new UnsupportedOperationException();
 					}
 				} else {
 					Object value = json.get(name); // the value associated with
 													// key 'name'
 
 					/*
 					 * Check for special fields, createdAt and updatedAt, which
 					 * will be formatted and stored as Dates.
 					 */
 					if (name.equals(FIELD_CREATED_AT)
 							|| name.equals(FIELD_UPDATED_AT)) {
 						value = javax.xml.bind.DatatypeConverter.parseDateTime(
 								(String) value).getTime();
 					}
 
 					put(name, value);
 				}
 			} catch (JSONException e) {
 
 			}
 		}
 
 	}
 
 	/**
 	 * Whether this object has a particular key. Same as 'has'.
 	 * 
 	 * @param key
 	 *            The key to check for
 	 * @return Returns whether this object contains the key
 	 */
 	public boolean containsKey(String key) {
 		return mData.containsKey(key);
 	}
 
 	/**
 	 * A private helper class to facilitate running a ParseObject delete
 	 * operation in the background.
 	 * 
 	 * @author js
 	 * 
 	 */
 	class DeleteInBackgroundThread extends Thread {
 		DeleteCallback mDeleteCallback;
 
 		/**
 		 * 
 		 * @param callback
 		 *            A function object of type DeleteCallback, whose method
 		 *            done will be called upon completion
 		 */
 		DeleteInBackgroundThread(DeleteCallback callback) {
 			mDeleteCallback = callback;
 		}
 
 		public void run() {
 			ParseException exception = null;
 
 			try {
 				delete();
 			} catch (ParseException e) {
 				exception = e;
 			}
 
 			if (mDeleteCallback != null) {
 				mDeleteCallback.done(exception);
 			}
 		}
 	}
 
 	/**
 	 * Deletes this object on the server in a background thread. Does nothing in
 	 * particular when the save completes. Use this when you don't care if the
 	 * delete works.
 	 */
 	public void deleteInBackground() {
 		deleteInBackground(null);
 	}
 
 	/**
 	 * Deletes this object on the server in a background thread. This is
 	 * preferable to using delete(), unless your code is already running from a
 	 * background thread.
 	 * 
 	 * @param callback
 	 *            callback.done(e) is called when the save completes.
 	 */
 	public void deleteInBackground(DeleteCallback callback) {
 		DeleteInBackgroundThread thread = new DeleteInBackgroundThread(callback);
 		thread.run();
 	}
 
 	/**
 	 * Deletes this object on the server. This does not delete or destroy the
 	 * object locally.
 	 * 
 	 * @throws ParseException
 	 *             Throws an error if the object does not exist or if the
 	 *             internet fails.
 	 * 
 	 */
 	public void delete() throws ParseException {
 		try {
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpDelete httpdelete = new HttpDelete(
 					Parse.getParseAPIUrlClasses() + mClassName + "/"
 							+ getObjectId());
 			httpdelete.addHeader("X-Parse-Application-Id",
 					Parse.getApplicationId());
 			httpdelete.addHeader("X-Parse-REST-API-Key", Parse.getRestAPIKey());
 
 			HttpResponse httpresponse = httpclient.execute(httpdelete);
 			HttpEntity entity = httpresponse.getEntity();
 
 			ParseResponse response = new ParseResponse(httpresponse);
 
 			if (!response.isFailed()) {
 				// delete was successful
 			} else {
 				throw response.getException();
 			}
 		} catch (ClientProtocolException e) {
 			throw ParseResponse.getConnectionFailedException(e.getMessage());
 		} catch (IOException e) {
 			throw ParseResponse.getConnectionFailedException(e.getMessage());
 		}
 	}
 
 	/**
 	 * Accessor to the class name.
 	 * 
 	 * @return
 	 */
 	public String getClassName() {
 		return mClassName;
 	}
 
 	/**
 	 * Accessor to the object id. An object id is assigned as soon as an object
 	 * is saved to the server. The combination of a className and an objectId
 	 * uniquely identifies an object in your application.
 	 * 
 	 * @return The object id.
 	 */
 	public String getObjectId() {
 		return (String) mData.get("objectId");
 	}
 
 	/**
 	 * Setter for the object id. In general you do not need to use this.
 	 * However, in some cases this can be convenient. For example, if you are
 	 * serializing a ParseObject yourself and wish to recreate it, you can use
 	 * this to recreate the ParseObject exactly.
 	 * 
 	 * @param objectId
 	 */
 	public void setObjectId(String objectId) {
 		mData.put("objectId", objectId);
 	}
 
 	public void setCharset(String charset) {
 		this.charset = charset;
 	}
 
 	public void setCreatedAt(String createdAt) {
 		mData.put("createdAt", createdAt);
 	}
 
 	public void setUpdatedAt(String updatedAt) {
 		mData.put("updatedAt", updatedAt);
 	}
 
 	/**
 	 * Access a ParsePointer value.
 	 * 
 	 * @param key
 	 *            The key to access the value for.
 	 * @return Returns null if there is no such key or if it is not a
 	 *         ParsePointer.
 	 */
 	public ParsePointer getParsePointer(String key) {
 		Object value = get(key);
 
 		// test for no such key or not a ParsePointer
 
 		if (value == null || value.getClass() != ParsePointer.class)
 			return null;
 
 		return (ParsePointer) value;
 	}
 
 	/**
 	 * Creates and returns a new ParsePointer object that points to the object
 	 * it's called on. Use when other objects need to point to *this* object.
 	 * 
 	 * @return A ParsePointer object set to point to this object.
 	 */
 	public ParsePointer getPointer() {
 		return new ParsePointer(mClassName, getObjectId());
 	}
 
 	/**
 	 * Access a string value.
 	 * 
 	 * @param key
 	 *            The key to access the value for.
 	 * @return Returns null if there is no such key or if it is not a String.
 	 */
 	public String getString(String key) {
 		Object value = get(key);
 
 		// test for no such key or value not a string
 
 		if (value == null || value.getClass() != String.class)
 			return null;
 
 		return (String) value;
 	}
 
 	/**
 	 * Access a boolean value.
 	 * 
 	 * @param key
 	 *            The key to access the value for.
 	 * @return Returns false if there is no such key or if it is not a boolean.
 	 */
 	public Boolean getBoolean(String key) {
 		Object value = get(key);
 
 		// test for no such key or value not a string
 
 		if (value == null || value.getClass() != Boolean.class)
 			return null;
 
 		return (Boolean) value;
 	}
 
 	/**
 	 * Encapsulates access to the HashTable that stores key/value pairs stored
 	 * by this Object
 	 * 
 	 * @param key
 	 *            The key to access the value for
 	 * @return Returns null if there is no such key
 	 */
 	private Object get(String key) {
 		return mData.get(key);
 	}
 
 	/**
 	 * Access a Date value.
 	 * 
 	 * @param key
 	 *            The key to access the value for.
 	 * @return Returns null if there is no such key or if it is not a Date.
 	 */
 	public Date getDate(String key) {
 		Object value = get(key);
 
 		// test for no such key or value not a string
 
 		if (value == null || value.getClass() != Date.class)
 			return null;
 
 		return (Date) value;
 	}
 
 	/**
 	 * Access a long value.
 	 * 
 	 * @param key
 	 *            The key to access the value for.
 	 * @return Returns null if there is no such key or if it is not a long.
 	 */
 	public long getLong(String key, long defaultValue) {
 		Object value = get(key);
 
 		// test for no such key or value not a long
 
 		if (value == null)
 			return defaultValue;
 
 		if (value.getClass() == Long.class)
 			return (Long) value;
 		else if (value.getClass() == Integer.class)
 			return Long.valueOf((Integer) value);
 
 		return defaultValue;
 	}
 
 	/**
 	 * Add a key-value pair to this object. It is recommended to name keys in
 	 * partialCamelCaseLikeThis.
 	 * 
 	 * @param key
 	 *            Keys must be alphanumerical plus underscore, and start with a
 	 *            letter.
 	 * @param value
 	 *            Values may be numerical, String, JSONObject, JSONArray,
 	 *            JSONObject.NULL, or other ParseObjects. value may not be null.
 	 */
 	public void put(String key, Object value) {
 		mData.put(key, value);
 	}
 
 	/**
 	 * Saves this object to the server. Typically, you should use
 	 * saveInBackground(com.parse.SaveCallback) instead of this, unless you are
 	 * managing your own threading.
 	 * 
 	 * @throws ParseException
 	 *             Throws an exception if the server is inaccessible.
 	 */
 	public void save() throws ParseException {
 		try {
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpPost httppost = new HttpPost(Parse.getParseAPIUrlClasses()
 					+ mClassName);
 			httppost.addHeader("X-Parse-Application-Id",
 					Parse.getApplicationId());
 			httppost.addHeader("X-Parse-REST-API-Key", Parse.getRestAPIKey());
 			httppost.addHeader("Content-Type", "application/json");
 
 			StringEntity stringEntity = null;
 			if (this.charset != null) {
 				stringEntity = new StringEntity(toJSONObject().toString(),
 						charset);
 			} else {
 				stringEntity = new StringEntity(toJSONObject().toString());
 			}
 
 			httppost.setEntity(stringEntity);
 			HttpResponse httpresponse = httpclient.execute(httppost);
 
 			ParseResponse response = new ParseResponse(httpresponse);
 
 			if (!response.isFailed()) {
 				JSONObject jsonResponse = response.getJsonObject();
 
 				if (jsonResponse == null) {
 					throw response.getException();
 				}
 
 				try {
 					setObjectId(jsonResponse.getString("objectId"));
 					setCreatedAt(jsonResponse.getString("createdAt"));
 				} catch (JSONException e) {
 					throw new ParseException(
 							ParseException.INVALID_JSON,
 							"Although Parse reports object successfully saved, the response was invalid.",
 							e);
 				}
 
 			} else {
 				throw response.getException();
 			}
 		} catch (ClientProtocolException e) {
 			throw ParseResponse.getConnectionFailedException(e);
 		} catch (IOException e) {
 			throw ParseResponse.getConnectionFailedException(e);
 		}
 	}
 
 	/**
 	 * A private helper class to facilitate running a ParseObject save operation
 	 * in the background.
 	 * 
 	 * @author js
 	 * 
 	 */
 	class SaveInBackgroundThread extends Thread {
 		SaveCallback mSaveCallback;
 
 		/**
 		 * 
 		 * @param callback
 		 *            A function object of type Savecallback, whose method done
 		 *            will be called upon completion
 		 */
 		SaveInBackgroundThread(SaveCallback callback) {
 			mSaveCallback = callback;
 		}
 
 		public void run() {
 			ParseException exception = null;
 
 			try {
 				save();
 			} catch (ParseException e) {
 				exception = e;
 			}
 
 			if (mSaveCallback != null) {
 				mSaveCallback.done(exception);
 			}
 		}
 	}
 
 	/**
 	 * Saves this object to the server in a background thread. This is
 	 * preferable to using save(), unless your code is already running from a
 	 * background thread.
 	 * 
 	 * @param callback
 	 *            callback.done(e) is called when the save completes.
 	 */
 	public void saveInBackground(SaveCallback callback) {
 		SaveInBackgroundThread t = new SaveInBackgroundThread(callback);
 		t.start();
 	}
 
 	/**
 	 * Saves this object to the server in a background thread. Use this when you
 	 * do not have code to run on completion of the push.
 	 */
 	public void saveInBackground() {
 		saveInBackground(null);
 	}
 
 	/**
 	 * Updates this object to the server. Typically, you should use
 	 * updateInBackground(com.parse.UpdateCallback) instead of this, unless you
 	 * are managing your own threading.
 	 * 
 	 * @throws ParseException
 	 *             Throws an exception if the server is inaccessible.
 	 */
 	public void update() throws ParseException {
 		try {
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpPut httpput = new HttpPut(Parse.getParseAPIUrlClasses()
 					+ mClassName + "/" + getObjectId());
 			httpput.addHeader("X-Parse-Application-Id",
 					Parse.getApplicationId());
 			httpput.addHeader("X-Parse-REST-API-Key", Parse.getRestAPIKey());
 			httpput.addHeader("Content-Type", "application/json");
 
 			httpput.setEntity(new StringEntity(toJSONObject(excludeObjectId)
 					.toString()));
 			HttpResponse httpresponse = httpclient.execute(httpput);
 
 			ParseResponse response = new ParseResponse(httpresponse);
 
 			if (!response.isFailed()) {
 				JSONObject jsonResponse = response.getJsonObject();
 
 				if (jsonResponse == null) {
 					throw response.getException();
 				}
 
 				try {
 					setUpdatedAt(jsonResponse.getString("updatedAt"));
 				} catch (JSONException e) {
 					throw new ParseException(
 							ParseException.INVALID_JSON,
 							"Although Parse reports object successfully saved, the response was invalid.",
 							e);
 				}
 
 			} else {
 				throw response.getException();
 			}
 		} catch (ClientProtocolException e) {
 			throw ParseResponse.getConnectionFailedException(e);
 		} catch (IOException e) {
 			throw ParseResponse.getConnectionFailedException(e);
 		}
 	}
 
 	/**
 	 * A private helper class to facilitate running a ParseObject save operation
 	 * in the background.
 	 * 
 	 * @author js
 	 * 
 	 */
 	class UpdateInBackgroundThread extends Thread {
 		UpdateCallback mUpdateCallback;
 
 		/**
 		 * 
 		 * @param callback
 		 *            A function object of type Updatecallback, whose method
 		 *            done will be called upon completion
 		 */
 		UpdateInBackgroundThread(UpdateCallback callback) {
 			mUpdateCallback = callback;
 		}
 
 		public void run() {
 			ParseException exception = null;
 
 			try {
 				update();
 			} catch (ParseException e) {
 				exception = e;
 			}
 
 			if (mUpdateCallback != null) {
 				mUpdateCallback.done(exception);
 			}
 		}
 	}
 
 	/**
 	 * Saves this object to the server in a background thread. This is
 	 * preferable to using save(), unless your code is already running from a
 	 * background thread.
 	 * 
 	 * @param callback
 	 *            callback.done(e) is called when the save completes.
 	 */
 	public void updateInBackground(UpdateCallback callback) {
 		UpdateInBackgroundThread t = new UpdateInBackgroundThread(callback);
 		t.start();
 	}
 
 	/**
 	 * Saves this object to the server in a background thread. Use this when you
 	 * do not have code to run on completion of the push.
 	 */
 	public void updateInBackground() {
 		updateInBackground(null);
 	}
 
 	/**
 	 * Increments this object to the server. Typically, you should use
 	 * incrementInBackground(com.parse.UpdateCallback) instead of this, unless you
 	 * are managing your own threading.
 	 * 
 	 * @throws ParseException
 	 *             Throws an exception if the server is inaccessible.
 	 */
 	public void increment(String key, int amount) throws ParseException {
 		try {
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpPut httpput = new HttpPut(Parse.getParseAPIUrlClasses()
 					+ mClassName + "/" + getObjectId());
 			httpput.addHeader("X-Parse-Application-Id",
 					Parse.getApplicationId());
 			httpput.addHeader("X-Parse-REST-API-Key", Parse.getRestAPIKey());
 			httpput.addHeader("Content-Type", "application/json");
 
 			String json = "{\"" + key
 					+ "\":{\"__op\":\"Increment\",\"amount\":" + amount + "}}";
 
 			httpput.setEntity(new StringEntity(json));
 			HttpResponse httpresponse = httpclient.execute(httpput);
 
 			ParseResponse response = new ParseResponse(httpresponse);
 
 			if (!response.isFailed()) {
 				JSONObject jsonResponse = response.getJsonObject();
 
 				if (jsonResponse == null) {
 					throw response.getException();
 				}
 
 				try {
 					setUpdatedAt(jsonResponse.getString("updatedAt"));
 				} catch (JSONException e) {
 					throw new ParseException(
 							ParseException.INVALID_JSON,
 							"Although Parse reports object successfully saved, the response was invalid.",
 							e);
 				}
 
 			} else {
 				throw response.getException();
 			}
 		} catch (ClientProtocolException e) {
 			throw ParseResponse.getConnectionFailedException(e);
 		} catch (IOException e) {
 			throw ParseResponse.getConnectionFailedException(e);
 		}
 	}
 
 	/**
 	 * A private helper class to facilitate running a ParseObject save operation
 	 * in the background.
 	 * 
 	 * @author js
 	 * 
 	 */
 	class IncrementInBackgroundThread extends Thread {
 		UpdateCallback mIncrementCallback;
 		private int amount;
 		private String key;
 
 		/**
 		 * 
 		 * @param callback
 		 *            A function object of type Updatecallback, whose method
 		 *            done will be called upon completion
 		 */
 		IncrementInBackgroundThread(String key, int amount,
 				UpdateCallback callback) {
 			this.key = key;
 			this.amount = amount;
 			mIncrementCallback = callback;
 		}
 
 		public void run() {
 			ParseException exception = null;
 
 			try {
 				increment(key, amount);
 			} catch (ParseException e) {
 				exception = e;
 			}
 
 			if (mIncrementCallback != null) {
 				mIncrementCallback.done(exception);
 			}
 		}
 	}
 
 	/**
 	 * Increments this object to the server in a background thread. This is
 	 * preferable to using save(), unless your code is already running from a
 	 * background thread.
 	 * 
 	 * @param callback
 	 *            callback.done(e) is called when the save completes.
 	 */
 	public void incrementInBackground(UpdateCallback callback, String key,
 			int amount) {
 		IncrementInBackgroundThread t = new IncrementInBackgroundThread(key,
 				amount, callback);
 		t.start();
 	}
 
 	/**
 	 * Increments this object to the server in a background thread. Use this when you
 	 * do not have code to run on completion of the push.
 	 */
 	public void incremenetInBackground(String key, int amount) {
 		incrementInBackground(null, key, amount);
 	}
 
 	/**
 	 * Increments this object to the server in a background thread. Use this when you
 	 * do not have code to run on completion of the push.
 	 */
 	public void incremenetInBackground(String key) {
 		incremenetInBackground(key, 1);
 	}
 
 	private JSONObject toJSONObject(Set<String> ignoreFields) {
 		JSONObject jo = new JSONObject();
 
 		try {
 			for (String key : mData.keySet()) {
 				if (!ignoreFields.contains(key)) {
 					jo.put(key, get(key));
 				}
 			}
 		} catch (JSONException e) {
 
 		}
 
 		return jo;
 	}
 
 	private JSONObject toJSONObject() {
 		return toJSONObject(Collections.<String> emptySet());
 	}
 
 	/**
 	 * This reports time as the server sees it, so that if you make changes to a
 	 * ParseObject, then wait a while, and then call save(), the updated time
 	 * will be the time of the save() call rather than the time the object was
 	 * changed locally.
 	 * 
 	 * @return The last time this object was updated on the server.
 	 */
 	public Date getUpdatedAt() {
 		return getDate(FIELD_UPDATED_AT);
 	}
 
 	/**
 	 * This reports time as the server sees it, so that if you create a
 	 * ParseObject, then wait a while, and then call save(), the creation time
 	 * will be the time of the first save() call rather than the time the object
 	 * was created locally.
 	 * 
 	 * @return The first time this object was saved on the server.
 	 */
 	public Date getCreatedAt() {
 		return getDate(FIELD_CREATED_AT);
 	}
 
 	/**
 	 * Decides if the calling ParseObject and the parameter ParseObject have the
 	 * same Parse Id.
 	 * 
 	 * @param asThisObject
 	 *            Parse Object to compare with
 	 * @return True if the Ids of the two objects are equal, false otherwise
 	 */
 	public boolean hasSameId(ParseObject asThisObject) {
 		return this.getObjectId().equals(asThisObject.getObjectId());
 	}
 }
