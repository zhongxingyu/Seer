 package com.evernote.android.sample.tageditor.service;
 
 import java.util.List;
 
import org.apache.thrift.transport.TTransportException;
 
 import android.app.IntentService;
 import android.content.Intent;
 import android.content.SharedPreferences.Editor;
 import android.preference.PreferenceManager;
 import android.text.format.Time;
 import android.util.Log;
 
 import com.evernote.android.sample.tageditor.data.TagsDb;
 import com.evernote.android.sample.tageditor.utils.TagEditorUtil;
 import com.evernote.client.oauth.android.EvernoteSession;
 import com.evernote.edam.notestore.NoteStore.Client;
 import com.evernote.edam.type.Tag;
 
 /**
  * Sync service that handles all the communication with the Evernote API
  * <p> We extend an IntentService to implement our sync service, because it 
  * implements a "work queue processor" pattern which fits our exact usage scenario.</p>
  * <p>The fact that it can be started as needed, handles each Intent using 
  * a worker thread, and stops itself when it runs out of work, makes it the perfect choice</p>
  * 
  * @author Juan Gomez
  * @version 1.0.0
  * @since December 2, 2012
  * 
  */
 public class TagSyncService extends IntentService {
 
 	// Name of this application, for logging
 	private static final String TAG = "TagSyncService";
 
 	// Used to interact with the Evernote web service
 	private EvernoteSession mEvernoteSession;
 
 	// Reference to our DB 
 	private TagsDb datasource;
 
 	// Constants to coordinate the work
 	public static final String EXTRA_CURRENT_TASK = "currentTask";
 
 	public static final String EXTRA_TAG_PARENT = "parentGuid";
 	public static final String EXTRA_TAG_GUID = "guid";
 	public static final String EXTRA_TAG_NAME = "name";
 
 	public static final String ACTION_COMPLETED = "com.evernote.android.sample.tageditor.service.action.ACTION_COMPLETED";
 	public static final String ACTION_FAILED = "com.evernote.android.sample.tageditor.service.action.ACTION_FAILED";
 
 	// Type of task being perform by our service
 	public static enum Task {
 		SYNC, DELETE, CREATE, UPDATE
 	};
 
 	@Override
 	public void onCreate() {
 		// We open the database and start a session with the Evernote service
 		datasource = TagsDb.INSTANCE;
 		datasource.open(getApplicationContext());
 		setupSession();
 		super.onCreate();
 	}
 
 	public TagSyncService() {
 		super("TagSyncService");
 	}
 
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		Task currentTask = null;
 		// If the intent is empty or has no "current task"set, we ignore this call
 		if (intent != null && intent.getExtras() != null) {
 
 			if (intent.hasExtra(EXTRA_CURRENT_TASK)) {
 				try {
 					currentTask = (Task) intent.getExtras().get(EXTRA_CURRENT_TASK);
 				} catch (Exception e) {
 					return;
 				}
 			}
 
 			if (currentTask == null)
 				return;
 
 			Client noteStore = null;
 			String action = "";
 			Intent responseIntent = new Intent();
 
 
 			try {
 				// Create an Evernote Note Store to perform actions on the Tags
 				noteStore = mEvernoteSession.createNoteStore();
 			} catch (TTransportException e) {
 				Log.e(TAG, "Can't get noteStore", e);
 				responseIntent.putExtra(EXTRA_CURRENT_TASK, currentTask);
 				action = ACTION_FAILED;
 				responseIntent.setAction(action);
 				sendBroadcast(responseIntent);
 			}
 			switch(currentTask) {
 			case SYNC:
 				try {
 					if (mEvernoteSession.isLoggedIn() && noteStore != null) {
 						List<Tag> tags = noteStore.listTags(mEvernoteSession.getAuthToken());
 						// If the operation was successful with the service, 
 						// we perform it on our local database
 						datasource.storeRemoteTags(tags);
 						action = ACTION_COMPLETED;
 					}
 				} catch(Exception e) {
 					Log.e(TAG, "Can't get list of tags", e);
 					action = ACTION_FAILED;
 				}
 				break;
 			case CREATE:
 				if (intent.hasExtra(EXTRA_TAG_NAME)) {
 					try {
 						if (mEvernoteSession.isLoggedIn() && noteStore != null) {
 							Tag temp_tag = new Tag();
 							String name = (String) intent.getExtras().get(EXTRA_TAG_NAME);
 							temp_tag.setName(name);
 							String parentGuid = "";
 							// If a parent guid is included we set it on the tag
 							if (intent.hasExtra(EXTRA_TAG_PARENT)) {
 								parentGuid = (String) intent.getExtras().get(EXTRA_TAG_PARENT);
 								temp_tag.setParentGuid(parentGuid);
 							}
 							Tag tag = noteStore.createTag(mEvernoteSession.getAuthToken(), temp_tag);
 							// If the operation was successful with the service, 
 							// we perform it on our local database
 							datasource.insertTag(tag);
 							action = ACTION_COMPLETED;
 						}
 					} catch(Exception e) {
 						Log.e(TAG, "Can't create new tag", e);
 						action = ACTION_FAILED;
 					}
 				}
 				break;
 			case DELETE:
 				if (intent.hasExtra(EXTRA_TAG_GUID)) {
 					try {
 						if (mEvernoteSession.isLoggedIn() && noteStore != null) {
 							String guid = (String) intent.getExtras().get(EXTRA_TAG_GUID);
 							noteStore.untagAll(mEvernoteSession.getAuthToken(), guid);
 							noteStore.expungeTag(mEvernoteSession.getAuthToken(), guid);
 							// If the operation was successful with the service, 
 							// we perform it on our local database
 							datasource.deleteTag(guid);
 							action = ACTION_COMPLETED;
 						}
 					} catch(Exception e) {
 						Log.e(TAG, "Can't delete tag", e);
 						action = ACTION_FAILED;
 					}
 				}
 				break;
 			case UPDATE:
 				if (intent.hasExtra(EXTRA_TAG_GUID)) {
 					try {
 						if (mEvernoteSession.isLoggedIn() && noteStore != null) {
 							Tag temp_tag = new Tag();
 							String name = (String) intent.getExtras().get(EXTRA_TAG_NAME);
 							String guid = (String) intent.getExtras().get(EXTRA_TAG_GUID);
 							temp_tag.setName(name);
 							temp_tag.setGuid(guid);
 							String parentGuid = "";
 							if (intent.hasExtra(EXTRA_TAG_PARENT)) {
 								parentGuid = (String) intent.getExtras().get(EXTRA_TAG_PARENT);
 								// if the parent tag is the empty tag, we set the parent guid to null
 								parentGuid = parentGuid.equalsIgnoreCase(TagsDb.EMPTY_TAG_LIST_ITEM) ? null : parentGuid;
 								temp_tag.setParentGuid(parentGuid);
 							}
 							// if the update succeeds we get an updateSequenceNum 
 							// so we set it to the temp tag
 							int updateSequenceNum = noteStore.updateTag(mEvernoteSession.getAuthToken(), temp_tag);
 							temp_tag.setUpdateSequenceNum(updateSequenceNum);
 							temp_tag.setUpdateSequenceNumIsSet(true);
 							// If the operation was successful with the service, 
 							// we perform it on our local database
 							datasource.updateTag(temp_tag);
 							action = ACTION_COMPLETED;
 						}
 					} catch(Exception e) {
 						Log.e(TAG, "Can't update new tag", e);
 						action = ACTION_FAILED;
 					}
 				}
 				break;
 			default:
 				break;
 			}
 			// if there's no action to report we don't send a broadcast
 			if(!action.equalsIgnoreCase("")) {
 				// If there's an action, we send a broadcast that gets captured by our main activity
 				responseIntent.setAction(action);
 				responseIntent.putExtra(EXTRA_CURRENT_TASK, currentTask);
 				sendBroadcast(responseIntent);
 			}
 		}
 	}
 
 	/**
 	 * Setup the EvernoteSession used to access the Evernote API.
 	 */
 	private void setupSession() {
 		// Retrieve persisted authentication information
 		mEvernoteSession = EvernoteSession.init(getApplicationContext(), TagEditorUtil.CONSUMER_KEY, TagEditorUtil.CONSUMER_SECRET, TagEditorUtil.EVERNOTE_HOST, null);
 	}
 }
