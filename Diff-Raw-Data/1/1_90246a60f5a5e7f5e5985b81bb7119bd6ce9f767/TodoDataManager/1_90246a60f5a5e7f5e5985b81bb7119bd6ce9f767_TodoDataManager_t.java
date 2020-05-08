 /*
  * Copyright (C) 2011 Kazuya Yokoyama <kazuya.yokoyama@gmail.com>
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
 
 package com.kazuyayokoyama.android.todobento.io;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import mobisocial.socialkit.Obj;
 import mobisocial.socialkit.musubi.MemObj;
 import mobisocial.socialkit.musubi.Musubi;
 import mobisocial.socialkit.musubi.multiplayer.FeedRenderable;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.util.Base64;
 import android.util.Log;
 
 import com.kazuyayokoyama.android.todobento.ui.list.ParentTodoListItem;
 import com.kazuyayokoyama.android.todobento.ui.list.TodoListItem;
 import com.kazuyayokoyama.android.todobento.util.BitmapHelper;
 
 public class TodoDataManager {
 	public static final String TYPE_APP_STATE = "appstate";
 	public static final String STATE = "state";
 	public static final String B64JPGTHUMB = "b64jpgthumb";
 
 	public static final String PARENT_ROOT = "parent";
 	public static final String PARENT_UUID = "parent_uuid";
 	public static final String PARENT_TITLE = "parent_title";
 	public static final String PARENT_TODO = "parent_todo";
 	public static final String CHILD_TITLE = "child_title";
 	public static final String CHILD_DESC = "child_description";
 	public static final String CHILD_WIMG = "child_withimg";
 	public static final String CHILD_DONE = "child_done";
 	public static final String CHILD_UUID = "child_uuid";
 	public static final String CHILD_CRE_DATE = "child_cre_date";
 	public static final String CHILD_MOD_DATE = "child_mod_date";
 	public static final String CHILD_CRE_CONTACT_ID = "child_cre_contact_id";
 	public static final String CHILD_MOD_CONTACT_ID = "child_mod_contact_id";
 
 	public static final String DIFF = "diff";
 	public static final String DIFF_ADDED_UUID = "diff_added_uuid";
 	
 	private static final String TAG = "TodoDataManager";
 	private static TodoDataManager sInstance = null;
 	private static JSONObject sJSONObj = null;
 	private Musubi mMusubi = null;
 	private Uri mBaseUri = null;
 	private String mLocalContactId = null;
 
 	// ----------------------------------------------------------
 	// Instance
 	// ----------------------------------------------------------
 	private TodoDataManager() {
 		// nothing to do
 	}
 
 	public static TodoDataManager getInstance() {
 		if (sInstance == null) {
 			sInstance = new TodoDataManager();
 		}
 
 		return sInstance;
 	}
 
 	public void init(Musubi musubi, Uri baseUri) {
 		mMusubi = musubi;
 		mBaseUri = baseUri;
 		mLocalContactId = mMusubi.userForLocalDevice(mBaseUri).getId();
 
		sJSONObj = null;
 		Obj obj = mMusubi.getFeed().getLatestObj();
 		if (obj != null && obj.getJson() != null && obj.getJson().has(TodoDataManager.STATE)) {
 			sJSONObj = obj.getJson().optJSONObject(TodoDataManager.STATE);
 		}
 
 		// TODO temporary
 		if (sJSONObj == null) {
 			sJSONObj = new JSONObject();
 			JSONArray parentArray = new JSONArray();
 			try {
 				JSONObject parent = new JSONObject();
 				// TODO fix uuid
 				// parent.put(PARENT_UUID, UUID.randomUUID().toString());
 				parent.put(PARENT_UUID, 0);
 				parent.put(PARENT_TITLE, "Shared");
 				
 				JSONArray todoArray = new JSONArray();
 				parent.put(PARENT_TODO, todoArray);
 				parentArray.put(0, parent);
 				
 				sJSONObj.put(PARENT_ROOT, parentArray);
 			} catch (JSONException e) {
 				Log.e(TAG, "Failed to craete JSON", e);
 			}
 		}
 	}
 
 	public void fin() {
 		if (sJSONObj != null) {
 			sJSONObj = null;
 		}
 
 		if (sInstance != null) {
 			sInstance = null;
 		}
 	}
 
 	// ----------------------------------------------------------
 	// Get / Retrieve
 	// ----------------------------------------------------------
 	synchronized public List<ParentTodoListItem> getParentTodoList() {
 		List<ParentTodoListItem> list = new ArrayList<ParentTodoListItem>();
 
 		return list;
 	}
 
 	synchronized public List<TodoListItem> getTodoList(String parentUUID) {
 		List<TodoListItem> list = new ArrayList<TodoListItem>();
 
 		JSONArray parentArray = sJSONObj.optJSONArray(PARENT_ROOT);
 		try {
 			// TODO find parentUUID (now ignore UUID=0)
 			JSONObject parent = parentArray.getJSONObject(0);
 			JSONArray todoArray = parent.optJSONArray(PARENT_TODO);
 			for (int i = 0; i < todoArray.length(); i++) {
 				JSONObject todo = todoArray.getJSONObject(i);
 				TodoListItem item = new TodoListItem();
 				convObjectToTodoListItem(todo, item);
 				list.add(item);
 			}
 		} catch (JSONException e) {
 			Log.e(TAG, "Failed to get JSON", e);
 		}
 		return list;
 	}
 
 	synchronized public TodoListItem getItem(String parentUUID, int position) {
 		TodoListItem item = new TodoListItem();
 
 		JSONArray parentArray = sJSONObj.optJSONArray(PARENT_ROOT);
 		try {
 			// TODO find parentUUID (now ignore UUID=0)
 			JSONObject parent = parentArray.getJSONObject(0);
 			JSONArray todoArray = parent.optJSONArray(PARENT_TODO);
 
 			JSONObject todo = todoArray.getJSONObject(position);
 			convObjectToTodoListItem(todo, item);
 
 		} catch (JSONException e) {
 			Log.e(TAG, "Failed to get JSON", e);
 		}
 		return item;
 	}
 
 	synchronized public TodoListItem getItem(String todoUUID) {
 		Log.d(TAG, "getItem : " + todoUUID);
 
 		if (sJSONObj == null) {
 			return null;
 		} else {
 			try {
 				// search parent
 				JSONArray parentArray = sJSONObj.optJSONArray(PARENT_ROOT);
 				for (int i = 0; i < parentArray.length(); i++) {
 					// search child
 					JSONObject parent = parentArray.getJSONObject(i);
 					JSONArray todoArray = parent.optJSONArray(PARENT_TODO);
 					for (int j = 0; j < todoArray.length(); j++) {
 						JSONObject todo = todoArray.getJSONObject(j);
 						if (todo.optString(CHILD_UUID).equalsIgnoreCase(
 								todoUUID)) {
 							TodoListItem item = new TodoListItem();
 							convObjectToTodoListItem(todo, item);
 							return item;
 						}
 					}
 					Log.e(TAG, "Didn't find todo to update");
 				}
 			} catch (JSONException e) {
 				Log.e(TAG, "Failed to update JSON", e);
 			}
 			return null;
 		}
 	}
 
 	synchronized public Bitmap getBitmap(String todoUUID, int targetWidth,
 			int targetHeight, float degrees) {
 		Bitmap bitmap = null;
 
 		Cursor c = mMusubi.getFeed().query();
 		c.moveToFirst();
 		for (int i = 0; i < c.getCount(); i++) {
 			Obj object = mMusubi.objForCursor(c);
 			if (object != null && object.getJson() != null && object.getJson().has(TodoDataManager.DIFF)) {
 				JSONObject diff = object.getJson().optJSONObject(TodoDataManager.DIFF);
 				if (todoUUID.equals(diff.optString(TodoDataManager.DIFF_ADDED_UUID))) {
 					byte[] byteArray = Base64.decode(object.getJson().optString(B64JPGTHUMB), Base64.DEFAULT);
 					bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
 					break;
 				}
 			}
 			c.moveToNext();
 		}
 		c.close();
 
 		// dummy
 		if (bitmap == null) {
 			bitmap = BitmapHelper.getDummyBitmap(targetWidth, targetHeight);
 		} else {
 			bitmap = BitmapHelper.getResizedBitmap(bitmap, targetWidth, targetHeight, degrees);
 		}
 
 		return bitmap;
 	}
 
 	synchronized public JSONObject getObject(String parentUUID, int position) {
 		JSONObject object = null;
 
 		JSONArray parentArray = sJSONObj.optJSONArray(PARENT_ROOT);
 		try {
 			// TODO find parentUUID (now ignore UUID=0)
 			JSONObject parent = parentArray.getJSONObject(0);
 			JSONArray todoArray = parent.optJSONArray(PARENT_TODO);
 
 			object = todoArray.getJSONObject(position);
 
 		} catch (JSONException e) {
 			Log.e(TAG, "Failed to get JSON", e);
 		}
 		return object;
 	}
 
 	synchronized public int getParentTodoListCount() {
 		if (sJSONObj == null) {
 			return 0;
 		} else {
 			return sJSONObj.optJSONArray(PARENT_ROOT).length();
 		}
 	}
 
 	synchronized public int getTodoListCount(String parentUUID) {
 		if (sJSONObj == null) {
 			return 0;
 		} else {
 			JSONArray parentArray = sJSONObj.optJSONArray(PARENT_ROOT);
 			// Log.d(TAG, "-----------" + sJSONObj.toString());
 			for (int i = 0; i < parentArray.length(); i++) {
 				try {
 					JSONObject parent = parentArray.getJSONObject(i);
 					if (parent.optString(PARENT_UUID).equalsIgnoreCase(parentUUID)) {
 						return parent.optJSONArray(PARENT_TODO).length();
 					}
 				} catch (JSONException e) {
 					Log.e(TAG, "Failed to get JSON", e);
 				}
 			}
 			return 0;
 		}
 	}
 
 	synchronized public JSONObject getJSONObject() {
 		return sJSONObj;
 	}
 
 	synchronized public void setJSONObject(JSONObject obj) {
 		sJSONObj = obj;
 	}
 	
 	public String getLocalContactId() {
 		return mLocalContactId;
 	}
 
 	// ----------------------------------------------------------
 	// Update
 	// ----------------------------------------------------------
 	synchronized public void addTodo(String parentUUID, TodoListItem item,
 			Bitmap image, String msg) {
 		Log.d(TAG, "addTodo");
 
 		JSONArray parentArray = sJSONObj.optJSONArray(PARENT_ROOT);
 		try {
 			// TODO find parentUUID (now ignore UUID=0)
 			JSONObject parent = parentArray.getJSONObject(0);
 			JSONArray todoArray = parent.optJSONArray(PARENT_TODO);
 			// Log.d(TAG, "before : " + todoArray.length());
 
 			// Add top of array
 			JSONObject todo = new JSONObject();
 			convTodoListItemToObject(item, todo);
 			todoArray.put(todoArray.length(), todo);
 			// Log.d(TAG, "after : " + todoArray.length());
 
 			// Musubi
 			if (image == null) {
 				pushUpdate(msg);
 			} else {
 				pushUpdate(msg, item.uuid, Base64.encodeToString(BitmapHelper.bitmapToBytes(image), Base64.DEFAULT));
 			}
 
 		} catch (JSONException e) {
 			Log.e(TAG, "Failed to add JSON", e);
 		}
 	}
 
 	synchronized public void removeTodo(String parentUUID, TodoListItem item) {
 		// TODO no use-case
 	}
 
 	synchronized public void updateTodo(String todoUUID, TodoListItem item,
 			String msg) {
 		// Log.d(TAG, "updateTodo : " + todoUUID);
 
 		if (sJSONObj == null) {
 			return;
 		} else {
 			try {
 				// search parent
 				JSONArray parentArray = sJSONObj.optJSONArray(PARENT_ROOT);
 				for (int i = 0; i < parentArray.length(); i++) {
 					// search child
 					JSONObject parent = parentArray.getJSONObject(i);
 					JSONArray todoArray = parent.optJSONArray(PARENT_TODO);
 					for (int j = 0; j < todoArray.length(); j++) {
 						JSONObject todo = todoArray.getJSONObject(j);
 						if (todo.optString(CHILD_UUID).equalsIgnoreCase(todoUUID)) {
 							// found and update
 							convTodoListItemToObject(item, todo);
 
 							// Musubi
 							pushUpdate(msg);
 
 							return;
 						}
 					}
 					Log.w(TAG, "Didn't find todo to update");
 				}
 			} catch (JSONException e) {
 				Log.e(TAG, "Failed to update JSON", e);
 			}
 			return;
 		}
 	}
 
 	synchronized public void sort(String parentUUID, int positionFrom,
 			int positionTo) {
 		// Log.d(TAG, "sort from " + positionFrom + " to " + positionTo);
 
 		JSONArray parentArray = sJSONObj.optJSONArray(PARENT_ROOT);
 		try {
 			// TODO find parentUUID (now ignore UUID=0)
 			JSONObject parent = parentArray.getJSONObject(0);
 			JSONArray todoArray = parent.optJSONArray(PARENT_TODO);
 
 			int i;
 			if (positionFrom < positionTo) {
 				final int min = positionFrom;
 				final int max = positionTo;
 				final JSONObject dataMin = todoArray.getJSONObject(min);
 				i = min;
 				while (i < max) {
 					todoArray.put(i, todoArray.getJSONObject(++i));
 				}
 				todoArray.put(max, dataMin);
 			} else if (positionFrom > positionTo) {
 				final int min = positionTo;
 				final int max = positionFrom;
 				final JSONObject dataMax = todoArray.getJSONObject(max);
 				i = max;
 				while (i > min) {
 					todoArray.put(i, todoArray.getJSONObject(--i));
 				}
 				todoArray.put(min, dataMax);
 			}
 
 		} catch (JSONException e) {
 			Log.e(TAG, "Failed to get JSON", e);
 		}
 
 		// Log.d(TAG, sJSONObj.toString());
 	}
 
 	synchronized public void clearTodoDone(String parentUUID, String msg) {
 		// Log.d(TAG, "clearTodoDone : " + parentUUID);
 
 		// prepare new objects after clearing
 		JSONArray newTodoArray = new JSONArray();
 
 		JSONArray parentArray = sJSONObj.optJSONArray(PARENT_ROOT);
 		try {
 			// TODO find parentUUID (now ignore UUID=0)
 			JSONObject parent = parentArray.getJSONObject(0);
 			JSONArray todoArray = parent.optJSONArray(PARENT_TODO);
 
 			for (int i = 0; i < todoArray.length(); i++) {
 				JSONObject todo = todoArray.getJSONObject(i);
 
 				if (!todo.optBoolean(CHILD_DONE)) {
 					// leave undone item in new array
 					newTodoArray.put(newTodoArray.length(), todo);
 				}
 			}
 
 			parent.put(PARENT_TODO, newTodoArray);
 
 			// if updated
 			if (todoArray.length() != newTodoArray.length()) {
 				// Musubi
 				pushUpdate(msg);
 			}
 
 		} catch (JSONException e) {
 			Log.e(TAG, "Failed to edit JSON", e);
 		}
 
 		// Log.d(TAG, sJSONObj.toString());
 	}
 
 	// ----------------------------------------------------------
 	// Musubi
 	// ----------------------------------------------------------
 	public void pushUpdate(String htmlMsg) {
 		try {
 			Log.d(TAG, "pushUpdate");
 
 			JSONObject b = new JSONObject(getStateObj().toString());
 			FeedRenderable renderable = FeedRenderable.fromHtml(htmlMsg);
 			renderable.withJson(b);
 			mMusubi.getFeed().postObj(new MemObj(TYPE_APP_STATE, b));
 		} catch (JSONException e) {
 			Log.e(TAG, "Failed to post JSON", e);
 		}
 	}
 
 	public void pushUpdate(String htmlMsg, String addedUUID, String data) {
 		try {
 			Log.d(TAG, "pushUpdate w/ data");
 
 			JSONObject b = new JSONObject(getStateObj().toString());
 
 			JSONObject diff = new JSONObject();
 			diff.put(DIFF_ADDED_UUID, addedUUID);
 			b.put(DIFF, diff);
 			b.put(B64JPGTHUMB, data);
 
 			FeedRenderable renderable = FeedRenderable.fromHtml(htmlMsg);
 			renderable.withJson(b);
 			mMusubi.getFeed().postObj(new MemObj(TYPE_APP_STATE, b));
 		} catch (JSONException e) {
 			Log.e(TAG, "Failed to post JSON", e);
 		}
 	}
 
 	private JSONObject getStateObj() {
 		JSONObject out = new JSONObject();
 		try {
 			out.put(STATE, getJSONObject());
 		} catch (JSONException e) {
 			Log.e(TAG, "Failed to put JSON", e);
 		}
 		return out;
 	}
 
 	// ----------------------------------------------------------
 	// Utility
 	// ----------------------------------------------------------
 	private void convTodoListItemToObject(TodoListItem item, JSONObject obj) {
 		try {
 			obj.put(CHILD_TITLE, item.title);
 			obj.put(CHILD_DONE, item.bDone);
 			obj.put(CHILD_UUID, item.uuid);
 			obj.put(CHILD_DESC, item.description);
 			obj.put(CHILD_WIMG, item.withImg);
 			obj.put(CHILD_CRE_DATE, item.creDateMillis);
 			obj.put(CHILD_MOD_DATE, item.modDateMillis);
 			obj.put(CHILD_CRE_CONTACT_ID, item.creContactId);
 			obj.put(CHILD_MOD_CONTACT_ID, item.modContactId);
 		} catch (JSONException e) {
 			Log.e(TAG, "Failed to convert TodoListItem to JSON", e);
 		}
 	}
 
 	private void convObjectToTodoListItem(JSONObject obj, TodoListItem item) {
 		item.uuid = obj.optString(CHILD_UUID);
 		item.bDone = obj.optBoolean(CHILD_DONE);
 		item.title = obj.optString(CHILD_TITLE);
 		item.description = obj.optString(CHILD_DESC);
 		item.withImg = obj.optBoolean(CHILD_WIMG);
 		item.creDateMillis = obj.optLong(CHILD_CRE_DATE);
 		item.modDateMillis = obj.optLong(CHILD_MOD_DATE);
 		item.creContactId = obj.optString(CHILD_CRE_CONTACT_ID);
 		item.modContactId = obj.optString(CHILD_MOD_CONTACT_ID);
 	}
 }
