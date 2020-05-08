 package com.nbos.phonebook.sync.platform;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.ParseException;
 import org.apache.http.auth.AuthenticationException;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.params.ConnManagerParams;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.content.ContentResolver;
 import android.content.Context;
 import android.database.Cursor;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
 import android.provider.ContactsContract.Data;
 import android.provider.ContactsContract.Groups;
 import android.provider.ContactsContract.RawContacts;
 import android.util.Log;
 
 import com.nbos.phonebook.Db;
 import com.nbos.phonebook.sync.Constants;
 import com.nbos.phonebook.sync.client.Contact;
 import com.nbos.phonebook.sync.client.Group;
 import com.nbos.phonebook.sync.client.SharingBook;
 import com.nbos.phonebook.util.Notify;
 import com.nbos.phonebook.value.PicData;
 
 public class Cloud {
 	static String tag = "Cloud";
 	Db db;
     Context context;
     ContentResolver cr;
     String account, authToken, lastUpdated;
     HttpClient httpClient;
     // List<PicData> serverPicData;
     Set<String> syncedContactServerIds = new HashSet<String>(),
     	syncedGroupServerIds = new HashSet<String>();
     // SyncManager syncManager;
     SyncPics syncPics;
     Cursor serverDataCursor;
     Map<String, String> serverContactIdsMap = new HashMap<String, String>(), 
     	contactServerIdsMap = new HashMap<String, String>(); // serverPicIds, 
     Map<String, Integer> serverDataIndex = new HashMap<String, Integer>();
     Set<String> serverIds;
     boolean newOnly;
     public static final int REGISTRATION_TIMEOUT = 20 * 60 * 1000; // ms
 
     public static final String 
     	PARAM_USERNAME = "username",
     	PARAM_PASSWORD = "password",
     	PARAM_PHONE_NUMBER = "ph",
     	PARAM_VALIDATION_CODE = "valid",
     	PARAM_UPDATED = "timestamp",
     	USER_AGENT = "AuthenticationService/1.0",
    	BASE_URL = "http://phonebook.nbostech.com/phonebook",
    	// BASE_URL = "http://10.9.8.172:8080/phonebook",
     	AUTH_URI = BASE_URL + "/mobile/index",
     	REG_URL = BASE_URL + "/mobile/register",
     	FACEBOOK_LOGIN_URL = BASE_URL + "/login/facebookMobileLogin",
     	VALIDATION_URI = BASE_URL + "/mobile/validate",
     	NEW_VALIDATION_CODE_URI = BASE_URL + "/mobile/newValidationCode",
     	CHECK_VALID_ACCOUNT_URI = BASE_URL + "/mobile/valid",
     	GET_CONTACT_UPDATES_URI = BASE_URL + "/mobile/contacts",
     	GET_SHARED_BOOK_UPDATES_URI = BASE_URL + "/mobile/sharedBooks",
     	GET_SHARED_BOOK_ID_UPDATES_URI = BASE_URL + "/mobile/sharedBookIds",
         SEND_CONTACT_UPDATES_URI = BASE_URL + "/mobile/updateContacts",
         SEND_GROUP_UPDATES_URI = BASE_URL + "/mobile/updateGroups",
         TIMESTAMP_URI = BASE_URL + "/mobile/timestamp",
     	SEND_SHARED_BOOK_UPDATES_URI = BASE_URL + "/mobile/updateSharedBooks",
     	SEND_LINK_UPDATES_URI = BASE_URL + "/mobile/updateLinks",
     	SEND_CHANGED_LINK_UPDATES_URI = BASE_URL + "/mobile/updateChangedLinks",
     	UPLOAD_CONTACT_PIC_URI = BASE_URL + "/fileUploader/process",
     	DOWNLOAD_CONTACT_PIC_URI = BASE_URL + "/download/index/",
     	GET_PIC_DATA_URI = BASE_URL + "/mobile/picData";
 
 	public Cloud(Context context, String name, String authtoken) {
 		this.context = context;
 		this.cr = context.getContentResolver();
 		this.db = new Db(context);
 		account = name;
 		authToken = authtoken;
 	}
 	
 	public String sync(String lastUpdated) throws AuthenticationException, ParseException, JSONException, IOException {
 		// sendAllContacts();
 		this.lastUpdated = lastUpdated;		
 		newOnly = lastUpdated != null;
         Object[] update = getContactUpdates();
         List<Contact> contacts =  (List<Contact>) update[0];
         List<Group> groups = (List<Group>) update[1];
         List<Group> sharedBooks = getSharedBooks();
         notify(contacts, groups, sharedBooks);
         getProfileData();
         getServerDataIds();
         syncPics = new SyncPics(context, getServerPicData(), this);
         if(contacts.size() > 0 || groups.size() > 0 || sharedBooks.size() > 0)
         {
         	
         	new SyncManager(context, account, 
         			contacts, groups, sharedBooks, 
         			// serverPicData, unchangedPicsRawContactIds, 
         			syncedContactServerIds, syncedGroupServerIds,
         			syncPics, this);
         			// syncedPictureIds, serverPicIds);
         			
         }
         getSharedBookIds();
         sendUpdates();
         syncPics.closeCursors();
         return getTimestamp();
 	}
 
 	void getProfileData() {
         final String[] PROJECTION =
             new String[] {
         		Data._ID,
         		Data.RAW_CONTACT_ID, Data.CONTACT_ID, Data.MIMETYPE, 
         		PhonebookSyncAdapterColumns.DATA_PID,
         		PhonebookSyncAdapterColumns.ACCOUNT,
         		PhonebookSyncAdapterColumns.PIC_ID,
         		PhonebookSyncAdapterColumns.PIC_SIZE,
         		PhonebookSyncAdapterColumns.PIC_HASH,
         };
 
         serverDataCursor = cr.query(Data.CONTENT_URI, PROJECTION, 
     			Data.MIMETYPE + " = '"+PhonebookSyncAdapterColumns.MIME_PROFILE+"' and "
     			+PhonebookSyncAdapterColumns.ACCOUNT + " = '" + account + "'", 
     			null, Data.CONTACT_ID);
 	}
 	
 	private void getSharedBookIds() throws ClientProtocolException, JSONException, IOException {
         List<NameValuePair> params = getAuthParams();
         if(lastUpdated != null)
         	params.add(new BasicNameValuePair(Constants.ACCOUNT_LAST_UPDATED, lastUpdated));
         final JSONArray sharedBookUpdateIds = new JSONArray(post(GET_SHARED_BOOK_ID_UPDATES_URI, params));
         Set<String> sharedBookIds = new HashSet<String>();
         for (int i = 0; i < sharedBookUpdateIds.length(); i++)  
         	sharedBookIds.add(new Long(sharedBookUpdateIds.getLong(i)).toString());
         deleteSharedBooksNotIn(sharedBookIds);
 	}
 
 	private void deleteSharedBooksNotIn(Set<String> sharedBookSourceIds) {
 	    Cursor sharedGroupsCursor = context.getContentResolver()
 	    	.query(Groups.CONTENT_URI, null,
 	    		Groups.SYNC1+" is not null"
 	    		+" and "+Groups.ACCOUNT_NAME+" = ? "
 	    		+" and "+Groups.ACCOUNT_TYPE+" = ? ", 
 	    		new String[]{account, Constants.ACCOUNT_TYPE}, null),
 	    		
 	    	groupItemsCursor = context.getContentResolver()
 	    		.query(Data.CONTENT_URI, 
 	    				new String[] {
 	    					GroupMembership.GROUP_ROW_ID,
 	    					GroupMembership.RAW_CONTACT_ID
 	    				}, 
 	    				GroupMembership.MIMETYPE+" = ? ", 
 	    				new String[] {GroupMembership.CONTENT_ITEM_TYPE}, 
 	    				GroupMembership.RAW_CONTACT_ID);
 	    
 	    Log.i(tag, "There are "+sharedGroupsCursor.getCount()+" shared groups");
 	    Log.i(tag, "There are "+groupItemsCursor.getCount()+" grouped contacts");
 	    
 	    Set<String> sharedBookIds = new HashSet<String>();
 	    sharedGroupsCursor.moveToFirst();
 	    if(sharedGroupsCursor.getCount() > 0)
 	    do {
 	    	String groupId = sharedGroupsCursor.getString(sharedGroupsCursor.getColumnIndex(Groups._ID)),
 	    		sourceId = sharedGroupsCursor.getString(sharedGroupsCursor.getColumnIndex(Groups.SOURCE_ID));
 	    	if(sharedBookSourceIds.contains(sourceId))
 	    		sharedBookIds.add(groupId);
 	    } while(sharedGroupsCursor.moveToNext());
 
 	    sharedGroupsCursor.moveToFirst();
 	    if(sharedGroupsCursor.getCount() > 0)
 	    do {
 	    	String groupId = sharedGroupsCursor.getString(sharedGroupsCursor.getColumnIndex(Groups._ID)),
 	    		sourceId = sharedGroupsCursor.getString(sharedGroupsCursor.getColumnIndex(Groups.SOURCE_ID));
 	    	if(!sharedBookSourceIds.contains(sourceId))
 	    		deleteGroup(groupId, sharedBookIds, groupItemsCursor);
 	    } while(sharedGroupsCursor.moveToNext());
 	    sharedGroupsCursor.close();
 	    groupItemsCursor.close();
 	}
 
 	private void deleteGroup(String groupId, Set<String> sharedBookIds, Cursor groupItemsCursor) {
 		// delete contacts in group
 		Cursor groupCursor = Db.getContactsInGroup(groupId, context.getContentResolver());
 		groupCursor.moveToFirst();
 	    if(groupCursor.getCount() > 0)
 	    do {
 	    	String rawContactId = groupCursor.getString(groupCursor.getColumnIndex(Data.RAW_CONTACT_ID));
 	    	Set<String> groupIds = getGroupIds(rawContactId, groupItemsCursor);
 	    	Log.i(tag, "Raw contact: "+rawContactId+" is in "+groupIds.size()+" groups");
 	    	boolean isInOtherSharedBook = false;
 	    	for(String gId : groupIds)
 	    	{
 	    		if(!gId.equals(groupId)
 	    		&& sharedBookIds.contains(gId))
 	    		{
 	    			Log.i(tag, "Contact is another shared book");
 	    			isInOtherSharedBook = true;
 	    			break;
 	    		}
 	    	}
 	    	if(isInOtherSharedBook) continue;
 	    	Log.i(tag, "Delete the contact");
 	    	int numDelete = context.getContentResolver()
 	    		.delete(SyncManager.addCallerIsSyncAdapterParameter(RawContacts.CONTENT_URI), 
 	    				RawContacts._ID + " = ? ", 
 	    				new String[] { rawContactId });
 	    	Log.i(tag, "deleted "+numDelete+" contact");
 	    			
 
     		// check if contact is in other shared books	
 		    /*values.put(GroupMembership.RAW_CONTACT_ID, rawContactId);
 		    values.put(GroupMembership.GROUP_ROW_ID, groupId);
 		    values.put(GroupMembership.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
 		    */
 	    } while(groupCursor.moveToNext());
 	    groupCursor.close();
 	    
 	    int numDelete = context.getContentResolver().delete(
 	    	SyncManager.addCallerIsSyncAdapterParameter(ContactsContract.Groups.CONTENT_URI), 
 	    	Groups._ID + " = ? ", 
 			new String[] { groupId} );
 	    Log.i(tag, "Deleted "+numDelete+" group for id: "+groupId);
 
 	}
 
 	private Set<String> getGroupIds(String rawContactId, Cursor groupItemsCursor) {
 		Set<String> groupIds = new HashSet<String>();
 		
 		groupItemsCursor.moveToFirst();
 	    if(groupItemsCursor.getCount() > 0)
 	    do {
 	    	String rawCId = groupItemsCursor.getString(groupItemsCursor.getColumnIndex(GroupMembership.RAW_CONTACT_ID));
 	    	if(Long.parseLong(rawCId) < Long.parseLong(rawContactId))
 	    		continue;
 	    	if(Long.parseLong(rawCId) > Long.parseLong(rawContactId))
 	    		break;
 	    	String gId = groupItemsCursor.getString(groupItemsCursor.getColumnIndex(GroupMembership.GROUP_ROW_ID));	    	
 	    	groupIds.add(gId);
 	    	// check if contact is in other shared books	
 		    /*values.put(GroupMembership.RAW_CONTACT_ID, rawContactId);
 		    values.put(GroupMembership.GROUP_ROW_ID, groupId);
 		    values.put(GroupMembership.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
 		    */
 	    } while(groupItemsCursor.moveToNext());
 		
 		return groupIds;
 	}
 
 	private void notify(List<Contact> contacts, List<Group> groups,
 			List<Group> sharedBooks) {
 		if(contacts.size() == 0 
 		&& groups.size() == 0 
 		&& sharedBooks.size() == 0)
 			return;
 		
 		StringBuffer note = new StringBuffer("");
 		if(newOnly)
 			note.append("Updated ");
 		else
 			note.append("Synced ");
 		if(contacts.size() > 0)
 			note.append(contacts.size()+" contacts");
 		if(groups.size() > 0)
 		{
 			if(contacts.size() > 0)
 				note.append(", ");
 			note.append(groups.size()+" groups");
 		}
 		if(sharedBooks.size() > 0)
 		{
 			if(contacts.size() > 0 || groups.size() > 0)
 				note.append(", ");
 			note.append(sharedBooks.size()+" shared books");
 		}
 		Notify.show("Phonebook: "+account, note.toString(), "Phonebook update", context);
 	}
 
 	private List<Group> getSharedBooks() throws ClientProtocolException, JSONException, IOException {
         List<NameValuePair> params = getAuthParams();
         if(lastUpdated != null)
         	params.add(new BasicNameValuePair(Constants.ACCOUNT_LAST_UPDATED, lastUpdated));
         final JSONArray sharedBooks = new JSONArray(post(GET_SHARED_BOOK_UPDATES_URI, params));
         final List<Group> books = new ArrayList<Group>();
         for (int i = 0; i < sharedBooks.length(); i++)  
             books.add(Group.valueOf(sharedBooks.getJSONObject(i)));
         return books;
 	}
 
 	Object[] getContactUpdates() throws JSONException, ParseException, IOException, AuthenticationException 
     {
     	final List<Contact> contactsList = new ArrayList<Contact>();
         final List<Group> groupsList = new ArrayList<Group>();
         List<NameValuePair> params = getAuthParams();
         if(lastUpdated != null)
         	params.add(new BasicNameValuePair(Constants.ACCOUNT_LAST_UPDATED, lastUpdated));
         final JSONArray update = new JSONArray(post(GET_CONTACT_UPDATES_URI, params)),
         	contacts = update.getJSONArray(0),
         	groups = update.getJSONArray(1);
         	
         for (int i = 0; i < contacts.length(); i++)
         {
         	Contact contact = Contact.valueOf(contacts.getJSONObject(i));
         	if(contact != null)
         		contactsList.add(contact);
         }
         
         // Log.i(tag, "Contacts: "+contactsList);
         for (int i = 0; i < groups.length(); i++) 
             groupsList.add(Group.valueOf(groups.getJSONObject(i)));
         
         return new Object[] {contactsList, groupsList}; 
     }
 
 	void sendUpdates() throws ClientProtocolException, IOException, JSONException {
 		getServerDataIds();
 		// List<PhoneContact> contacts = db.getContacts(newOnly, syncedContactServerIds, contactServerIdsMap);
 		UpdateContacts updateContacts = new UpdateContacts(this);
 		// sendContactUpdates(contacts);
         sendGroupUpdates(db.getGroups(newOnly, syncedGroupServerIds, contactServerIdsMap));
         sendSharedBookUpdates(db.getSharingBooks(newOnly, contactServerIdsMap));
         new UpdateLinks(this);
         syncPics.send(newOnly);
         serverDataCursor.close();
         // uploadContactPictures();
         if(updateContacts.numContacts > 0)
         	updateContacts.resetDirtyContacts();
 	}
 
 	
 	String getServerId(String rawContactId) {
 		return contactServerIdsMap.get(rawContactId);
 	}
 
 	public String getTimestamp() throws ClientProtocolException, JSONException, IOException {
         JSONArray response = new JSONArray(post(TIMESTAMP_URI, getAuthParams()));
         Long timestamp = response.getLong(0);
         return timestamp.toString();
 	}
 
 
 	Map<String, String[]> getContactServerIds() {
 		Log.i(tag, "getContactServerIds, server data cursor has "+serverDataCursor.getCount()+" rows");
 		Map<String, String[]> contactServerIds = new HashMap<String, String[]>();
 		if(serverDataCursor.getCount() == 0)
 			return contactServerIds;
 		serverDataCursor.moveToFirst();
 		do {
 			String id = serverDataCursor.getString(serverDataCursor.getColumnIndex(Data._ID)), 
 				rawId = serverDataCursor.getString(serverDataCursor.getColumnIndex(Data.RAW_CONTACT_ID)),
 				serverId = serverDataCursor.getString(serverDataCursor.getColumnIndex(PhonebookSyncAdapterColumns.DATA_PID));
 			
 			if(rawId != null && serverId != null)
 				contactServerIds.put(rawId, new String [] {id, serverId});
 
 		} while(serverDataCursor.moveToNext());
 		return contactServerIds;
 	}
 
 	private Map<String, PicData> getServerPicData() throws ClientProtocolException, JSONException, IOException {
 		Map<String, PicData> picData = new HashMap<String, PicData>();
 		JSONArray picsJson = new JSONArray(post(GET_PIC_DATA_URI, getAuthParams()));
 		for(int i=0; i< picsJson.length(); i++)
 		{
 			JSONArray picJson = picsJson.getJSONArray(i);
 			String serverId = new Long(picJson.getLong(0)).toString();
 			picData.put(serverId, new PicData(picJson.getLong(1), picJson.getLong(2)));
 		}
 		return picData;
 	}
 
 
 
 	
 	private void sendGroupUpdates(List<Group> groups) throws ClientProtocolException, IOException, JSONException {
         List<NameValuePair> params = getAuthParams();
         int numGroups = 0;
         Log.i(tag,"groupsSize: "+groups.size());
         for(int i=0; i< groups.size(); i++)
         {
         	Group group =  groups.get(i);
         	if(group.serverId != null 
         	&& syncedGroupServerIds.contains(group.serverId))
         	{
         		Log.i(tag, "Already synced group "+group.name+"["+group.serverId+"]");
         		continue;
         	}
         	String index = new Integer(numGroups).toString();
         	
         	params.add(new BasicNameValuePair("groupId_"+index, group.groupId));
         	params.add(new BasicNameValuePair("serverId_"+index, group.serverId));
         	params.add(new BasicNameValuePair("bookName_"+index, group.name));
         	List<Contact> bookContacts = group.contacts;
         	Log.i(tag, "numcontacts: "+bookContacts.size());
         	params.add(new BasicNameValuePair("numContacts_"+index, new Integer(bookContacts.size()).toString()));
         	for(int j=0; j< bookContacts.size(); j++)
         	{
         		Contact bContact = bookContacts.get(j);
         		String cIndex = new Integer(j).toString();
         		params.add(new BasicNameValuePair("serverId_"+index+"_"+cIndex, bContact.serverId));
         	}
         	numGroups++;
         }
         params.add(new BasicNameValuePair("numBooks", new Integer(numGroups).toString()));
         if(numGroups == 0) return;
         JSONArray groupUpdates = new JSONArray(post(SEND_GROUP_UPDATES_URI, params));
         for (int i = 0; i < groupUpdates.length(); i++)
         	ContactManager.updateGroup(groupUpdates.getJSONObject(i), context);
         if(groups.size() > 0)
         	ContactManager.resetDirtyGroups(context);
 
 	}
 	
 	private void sendSharedBookUpdates(List<SharingBook> books) throws ClientProtocolException, IOException, JSONException {
 		if(books.size() == 0) return;
         List<NameValuePair> params = getAuthParams();
         params.add(new BasicNameValuePair("numShareBooks", new Integer(books.size()).toString()));
         for(int i=0; i< books.size(); i++)
         {
         	String index = new Integer(i).toString();
         	SharingBook book =  books.get(i);
         	params.add(new BasicNameValuePair("shareBookId_"+index, book.groupId));
         	params.add(new BasicNameValuePair("shareContactId_"+index, book.contactId));
         	if(book.deleted)
         		params.add(new BasicNameValuePair("shareContactDeleted_"+index, "true"));
         	Log.i(tag, "Shared book["+book.groupId+"] deleted: "+book.deleted);
         }
         JSONArray bookUpdates = new JSONArray(post(SEND_SHARED_BOOK_UPDATES_URI, params));
         if(books.size() > 0)
         	ContactManager.resetDirtySharedBooks(context);
 	}
 	
 	public String post(String url, List<NameValuePair> params) throws ClientProtocolException, IOException, JSONException {
         HttpEntity entity = new UrlEncodedFormEntity(params);
         final HttpPost post = new HttpPost(url);
         Log.i(tag, "Sending to: "+url);
         post.addHeader(entity.getContentType());
         post.setEntity(entity);
         maybeCreateHttpClient();
         final HttpResponse resp = httpClient.execute(post);
         final String response = EntityUtils.toString(resp.getEntity());
         Log.i(tag, "Response is: "+response);
         return response;
 	}
 	
     /**
      * Configures the httpClient to connect to the URL provided.
      */
     void maybeCreateHttpClient() {
         if (httpClient == null) {
             httpClient = new DefaultHttpClient();
             final HttpParams params = httpClient.getParams();
             HttpConnectionParams.setConnectionTimeout(params, REGISTRATION_TIMEOUT);
             HttpConnectionParams.setSoTimeout(params, REGISTRATION_TIMEOUT);
             ConnManagerParams.setTimeout(params, REGISTRATION_TIMEOUT);
         }
     }
 	
 	List<NameValuePair> getAuthParams() {
         List<NameValuePair> params = new ArrayList<NameValuePair>();
         params.add(new BasicNameValuePair(PARAM_USERNAME, account));
         params.add(new BasicNameValuePair(PARAM_PASSWORD, authToken));
         /*if (lastUpdated != null) {
         final SimpleDateFormat formatter =
             new SimpleDateFormat("yyyy/MM/dd HH:mm");
         formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
         params.add(new BasicNameValuePair(PARAM_UPDATED, formatter
             .format(lastUpdated)));
     }
     Log.i(TAG, params.toString());*/
         
         return params;
 	}
 
 	
 	public void loginWithFacebook(String phone) throws ClientProtocolException, JSONException, IOException {
         final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
         params.add(new BasicNameValuePair(PARAM_USERNAME, account));
         params.add(new BasicNameValuePair(PARAM_PHONE_NUMBER, phone));
         final JSONArray response = new JSONArray(post(FACEBOOK_LOGIN_URL, params));
         Log.i(tag, "response is: "+response);
 	}
 	
 	public Map<String, String> getServerDataIds() {
 		serverDataCursor.requery();
         Log.i(tag, "Server data cursor has "+serverDataCursor.getCount()+" rows");
 		serverContactIdsMap = new HashMap<String, String>();
 		contactServerIdsMap = new HashMap<String, String>();
 		serverIds = serverContactIdsMap.keySet();
 		if(serverDataCursor.getCount() == 0)
 			return serverContactIdsMap;
 		serverDataCursor.moveToFirst();
 		do {
 			String serverId = serverDataCursor.getString(serverDataCursor.getColumnIndex(PhonebookSyncAdapterColumns.DATA_PID)),
 				rawContactId = serverDataCursor.getString(serverDataCursor.getColumnIndex(Data.RAW_CONTACT_ID));
 			if(serverId != null && rawContactId != null)
 			{
 				serverContactIdsMap.put(serverId, rawContactId);
 				contactServerIdsMap.put(rawContactId, serverId);
 				serverDataIndex.put(serverId, new Integer(serverDataCursor.getPosition()));
 			}
 		} while(serverDataCursor.moveToNext());
 		 serverIds = serverContactIdsMap.keySet();
 		 return serverContactIdsMap;
 	}
 
 }
