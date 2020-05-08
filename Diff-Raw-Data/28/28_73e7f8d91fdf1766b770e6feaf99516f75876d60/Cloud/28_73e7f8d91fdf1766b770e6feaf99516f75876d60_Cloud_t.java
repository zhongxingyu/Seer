 package com.nbos.phonebook.sync.platform;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
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
 import org.json.JSONObject;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.CommonDataKinds.Photo;
 import android.provider.ContactsContract.Data;
 import android.util.Log;
 
 import com.nbos.phonebook.Db;
 import com.nbos.phonebook.sync.Constants;
 import com.nbos.phonebook.sync.client.Contact;
 import com.nbos.phonebook.sync.client.ContactPicture;
 import com.nbos.phonebook.sync.client.Group;
 import com.nbos.phonebook.sync.client.PhoneContact;
 import com.nbos.phonebook.sync.client.ServerData;
 import com.nbos.phonebook.sync.client.SharingBook;
 import com.nbos.phonebook.util.ImageInfo;
 import com.nbos.phonebook.value.PicData;
 
 public class Cloud {
 	static String tag = "Cloud";
 	Db db;
     Context context;
     String accountName, authToken, lastUpdated;
     HttpClient httpClient;
     Cursor rawContactsCursor;
     SyncManager syncManager;
     List<PicData> serverPicData;
     boolean newOnly;
     public static final int REGISTRATION_TIMEOUT = 20 * 60 * 1000; // ms
 
     public static final String 
     	PARAM_USERNAME = "username",
     	PARAM_PASSWORD = "password",
     	PARAM_PHONE_NUMBER = "ph",
     	PARAM_VALIDATION_CODE = "valid",
     	PARAM_UPDATED = "timestamp",
     	USER_AGENT = "AuthenticationService/1.0",
     	// BASE_URL = "http://phonebook.nbostech.com/phonebook",
     	BASE_URL = "http://10.9.8.29:8080/phonebook",
     	AUTH_URI = BASE_URL + "/mobile/index",
     	REG_URL = BASE_URL + "/mobile/register",
     	VALIDATION_URI = BASE_URL + "/mobile/validate",
     	NEW_VALIDATION_CODE_URI = BASE_URL + "/mobile/newValidationCode",
     	CHECK_VALID_ACCOUNT_URI = BASE_URL + "/mobile/valid",
     	GET_CONTACT_UPDATES_URI = BASE_URL + "/mobile/contacts",
     	GET_SHARED_BOOK_UPDATES_URI = BASE_URL + "/mobile/sharedBooks",
         SEND_CONTACT_UPDATES_URI = BASE_URL + "/mobile/updateContacts",
         SEND_GROUP_UPDATES_URI = BASE_URL + "/mobile/updateGroups",
         TIMESTAMP_URI = BASE_URL + "/mobile/timestamp",
     	SEND_SHARED_BOOK_UPDATES_URI = BASE_URL + "/mobile/updateSharedBooks",
     	UPLOAD_CONTACT_PIC_URI = BASE_URL + "/fileUploader/process",
     	DOWNLOAD_CONTACT_PIC_URI = BASE_URL + "/download/index/",
     	GET_PIC_DATA_URI = BASE_URL + "/mobile/picData";
 
 	public Cloud(Context context, String name, String authtoken) {
 		this.context = context;
 		this.db = new Db(context);
 		accountName = name;
 		authToken = authtoken;
 	}
 	
 	public String sync(String lastUpdated) throws AuthenticationException, ParseException, JSONException, IOException {
 		// sendAllContacts();
 		this.lastUpdated = lastUpdated;		
 		newOnly = lastUpdated != null;
         Object[] update = getContactUpdates();
 		serverPicData = getServerPicData();
         syncManager = new SyncManager(context, accountName, update, serverPicData);
         rawContactsCursor = db.getRawContactsCursor(newOnly);
        sendUpdates();
         getSharedBooks();
         return getTimestamp();
 	}
 
 	/*public void sendAllContacts() throws ClientProtocolException, IOException, JSONException {
 		sendFriendUpdates(false);
 	}*/
 	
     private void getSharedBooks() throws ClientProtocolException, JSONException, IOException {
         List<NameValuePair> params = getAuthParams();
         if(lastUpdated != null)
         	params.add(new BasicNameValuePair(Constants.ACCOUNT_LAST_UPDATED, lastUpdated));
         final JSONArray sharedBooks = new JSONArray(post(GET_SHARED_BOOK_UPDATES_URI, params));
         final List<Group> books = new ArrayList<Group>();
         for (int i = 0; i < sharedBooks.length(); i++)  
             books.add(Group.valueOf(sharedBooks.getJSONObject(i)));
         syncManager.syncGroups(books, true);
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
             contactsList.add(Contact.valueOf(contacts.getJSONObject(i)));
         
         Log.i(tag, "Contacts: "+contactsList);
         for (int i = 0; i < groups.length(); i++) 
             groupsList.add(Group.valueOf(groups.getJSONObject(i)));
         
         return new Object[] {contactsList, groupsList}; 
     }
 
	public void sendUpdates() throws ClientProtocolException, IOException, JSONException {
 		sendContactUpdates(db.getContacts(newOnly), rawContactsCursor);
         sendGroupUpdates(db.getGroups(newOnly));
         sendSharedBookUpdates(db.getSharingBooks(newOnly));
         uploadContactPictures();
         ContactManager.resetDirtyContacts(context);
 	}
 
 	public String getTimestamp() throws ClientProtocolException, JSONException, IOException {
         JSONArray response = new JSONArray(post(TIMESTAMP_URI, getAuthParams()));
         Long timestamp = response.getLong(0);
         return timestamp.toString();
 		
 	}
 
 	private void sendContactUpdates(List<PhoneContact> contacts, Cursor rawContactsCursor) throws ClientProtocolException, IOException, JSONException {
         List<NameValuePair> params = getAuthParams();
         params.add(new BasicNameValuePair("numContacts", new Integer(contacts.size()).toString()));
         if(lastUpdated != null)
         	params.add(new BasicNameValuePair(Constants.ACCOUNT_LAST_UPDATED, lastUpdated));
         for(int i=0; i< contacts.size(); i++)
         {
         	String index = new Integer(i).toString();
         	PhoneContact contact =  contacts.get(i);
         	contact.addParams(params, index);
         }
 		
         JSONArray contactUpdates = new JSONArray(post(SEND_CONTACT_UPDATES_URI, params));
         Cursor serverDataCursor = Db.getContactServerData(context);
         final BatchOperation batchOperation = new BatchOperation(context);
         for (int i = 0; i < contactUpdates.length(); i++)
         {
         	ContactManager.updateContact(contactUpdates.getJSONObject(i), context, serverDataCursor, batchOperation);
         	if(batchOperation.size() > 50)
         		batchOperation.execute();
         }
         batchOperation.execute();
 	}
 	
 	private List<PicData> getServerPicData() throws ClientProtocolException, JSONException, IOException {
 		List<PicData> picData = new ArrayList<PicData>();
 		JSONArray picsJson = new JSONArray(post(GET_PIC_DATA_URI, getAuthParams()));
 		for(int i=0; i< picsJson.length(); i++)
 		{
 			JSONArray picJson = picsJson.getJSONArray(i);
 			picData.add(new PicData(picJson.getLong(0), picJson.getLong(1), picJson.getLong(2), picJson.getString(2)));
 		}
 		return picData;
 	}
 
 	void uploadContactPictures() throws ClientProtocolException, JSONException, IOException {
 		List<PicData> serverPicData = getServerPicData();
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("upload", "avatar");
 		params.put("errorAction", "error");
 		params.put("errorController", "file");
 		params.put("successAction", "success");
 		params.put("successController", "file");
 		Log.i(tag, "There are "+rawContactsCursor.getCount()+" raw contacts entries");
 		if(rawContactsCursor.getCount() == 0) return;
 	    Cursor dataCursor = db.getData(),
 	    	photosDataCursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
 	    		// null,
 	    	    new String[] {
 	    			ContactsContract.Contacts._ID, 
 	    			ContactsContract.Data.CONTACT_ID,
 	    			ContactsContract.Data.RAW_CONTACT_ID,
 	    			ContactsContract.RawContacts._ID,
 	    			ContactsContract.Contacts.DISPLAY_NAME,
 	    			ContactsContract.CommonDataKinds.Photo.PHOTO,
 	    			Data.MIMETYPE, Data.DATA1,
 	    		},
 	    		ContactsContract.CommonDataKinds.Photo.PHOTO +" is not null ",
 	    		null,
 	    		// +"and "+Data.MIMETYPE+" = ? ",
 	    	    // new String[] {ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE}, 
 	    	    ContactsContract.Data.CONTACT_ID);
 	    
 	    Log.i(tag, "There are "+photosDataCursor.getCount()+" photo data entries");
 	    
 	    photosDataCursor.moveToFirst();
 	    rawContactsCursor.moveToFirst();
 	    do {
 	    	
 	    	String rawContactId = rawContactsCursor.getString(rawContactsCursor.getColumnIndex(ContactsContract.RawContacts._ID));
 	    	ServerData data = Db.getServerDataFromContactId(dataCursor, rawContactId);
 	    	if(data == null) {
 	    		Log.i(tag, "Server data is null for contactId: "+rawContactId);
 	    		continue;
 	    	}
 	    	String serverId = data.serverId,
 	    		picId = data.picId, 
 	    		picSize = data.picSize,
 	    		picHash = data.picHash;
 	    	
 	    	ContactPicture pic = null;
 			try {
 				pic = getContactPicture(photosDataCursor, rawContactId);
 				if(pic == null) continue;
 				String hash = ImageInfo.hash(pic.pic);
 		        Log.i(tag, "picId: "+picId+", hash: " + hash);
 
 				if(picId != null) {
 					int pSize = Integer.parseInt(picSize);
 					if(pSize == pic.pic.length && picHash != null && hash.equals(picHash)
 					&& isServerPicData(serverId, picId, serverPicData))
 					{
 						Log.i(tag, "Same image not uploading");
 						continue;
 					}
 				}
 	    		String contentType = pic.mimeType.split("/")[1];
 	    		Log.i(tag, "uploading "+contentType);
 	    		params.remove("id");
 	    		params.put("id", serverId);
 	    		JSONObject response = upload(UPLOAD_CONTACT_PIC_URI, pic.pic, contentType, params);
 	    		if(response != null)
 	    		{
 	    			try {
 						int status = response.getInt("ok");
 						if(status == 1)
 						{
 							String pId = response.getString("id");
 								// pSize = response.getString("size"),
 								// hash = response.getString("hash");
 							updateContactPicData(rawContactId, serverId, pId, new Long(pic.pic.length).toString(), hash);
 						}
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 	    		}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 	    } while(rawContactsCursor.moveToNext());
 	    rawContactsCursor.close();
 	    dataCursor.close();
 	    photosDataCursor.close();
 	}
 
 	private boolean isServerPicData(String serverId, String picId,
 			List<PicData> serverPicData) {
 		for(PicData p : serverPicData)
 		{
 			if(p.serverId.equals(serverId))
 			{
 				if(p.picId.equals(picId))
 				{
 					Log.i(tag, "Pic is same on server");
 					return true;
 				}
 				else return false;
 			}
 		}
 		Log.i(tag, "No pic on server");
 		return false;
 	}
 
 	private void updateContactPicData(String rawContactId, String serverId, String picId, String picSize, String hash) {
 		Uri uri = Data.CONTENT_URI;
 		ContentValues values = new ContentValues();
 		values.put(PhonebookSyncAdapterColumns.PIC_ID, picId);
 		values.put(PhonebookSyncAdapterColumns.PIC_SIZE, picSize);
 		values.put(PhonebookSyncAdapterColumns.PIC_HASH, hash);
 		context.getContentResolver().update(uri, values, 
 				Data.RAW_CONTACT_ID + " = " + rawContactId + " and " +
 				PhonebookSyncAdapterColumns.DATA_PID + " = " + serverId + " and " +
 				Data.MIMETYPE + " = '" + PhonebookSyncAdapterColumns.MIME_PROFILE + "'", null);
 	}
 
 	private ContactPicture getContactPicture(Cursor dataCursor, String rawContactId) throws IOException {
 		if(dataCursor.getCount() == 0) return null;
 		dataCursor.moveToFirst();
 	    do {
 	    	String mimetype = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Data.MIMETYPE));
 	    	if(!mimetype.equals(Photo.CONTENT_ITEM_TYPE)) continue;
 	    	String rawId = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID));
 	    	if(!rawId.equals(rawContactId)) continue;
 	    	String name = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
 	    	byte[] pic = dataCursor.getBlob(dataCursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO));
 	    	String contentType = new ImageInfo(pic).getMimeType(); 
 	    		// dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.MIMETYPE));
 	    	// String serverId
 	    	Log.i(tag, "Contact["+rawContactId+"] "+name+", pic: "+(pic == null ? "null" : pic.length+", content type: "+contentType));
 	    	return new ContactPicture(pic, contentType);
 	    } while(dataCursor.moveToNext());
 	    return null;
 	}
 	
 	private void sendGroupUpdates(List<Group> groups) throws ClientProtocolException, IOException, JSONException {
         List<NameValuePair> params = getAuthParams();
         params.add(new BasicNameValuePair("numBooks", new Integer(groups.size()).toString()));
         for(int i=0; i< groups.size(); i++)
         {
         	String index = new Integer(i).toString();
         	Group group =  groups.get(i);
         	params.add(new BasicNameValuePair("groupId_"+index, group.groupId));
         	params.add(new BasicNameValuePair("serverId_"+index, group.serverId));
         	params.add(new BasicNameValuePair("bookName_"+index, group.name));
         	List<Contact> bookContacts = group.contacts;
         	params.add(new BasicNameValuePair("numContacts_"+index, new Integer(bookContacts.size()).toString()));
         	for(int j=0; j< bookContacts.size(); j++)
         	{
         		Contact bContact = bookContacts.get(j);
         		String cIndex = new Integer(j).toString();
         		params.add(new BasicNameValuePair("serverId_"+index+"_"+cIndex, bContact.serverId));
         	}
         }
         JSONArray groupUpdates = new JSONArray(post(SEND_GROUP_UPDATES_URI, params));
         for (int i = 0; i < groupUpdates.length(); i++)
         	ContactManager.updateGroup(groupUpdates.getJSONObject(i), context);
         ContactManager.resetDirtyGroups(context);
 
 	}
 	
 	private String sendSharedBookUpdates(List<SharingBook> books) throws ClientProtocolException, IOException, JSONException {
         List<NameValuePair> params = getAuthParams();
         params.add(new BasicNameValuePair("numShareBooks", new Integer(books.size()).toString()));
         for(int i=0; i< books.size(); i++)
         {
         	String index = new Integer(i).toString();
         	SharingBook book =  books.get(i);
         	params.add(new BasicNameValuePair("shareBookId_"+index, book.groupId));
         	params.add(new BasicNameValuePair("shareContactId_"+index, book.contactId));
         }
         JSONArray response = new JSONArray(post(SEND_SHARED_BOOK_UPDATES_URI, params));
         JSONArray  bookUpdates = response.getJSONArray(0);
         Long timestamp = response.getLong(1);
         for (int i = 0; i < bookUpdates.length(); i++)
         	ContactManager.updateBook(bookUpdates.getJSONObject(i), context);
         ContactManager.resetDirtySharedBooks(context);
         return timestamp.toString();
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
 	
 	private List<NameValuePair> getAuthParams() {
         List<NameValuePair> params = new ArrayList<NameValuePair>();
         params.add(new BasicNameValuePair(PARAM_USERNAME, accountName));
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
 	
 	JSONObject upload(String uploadUrl, byte[] data, String contentType, Map<String, String> params) {
 		Log.i(tag, "Uploading to "+uploadUrl);
 		HttpURLConnection connection = null;
 		DataOutputStream outputStream = null;
 
 		// String pathToOurFile = "/tmp/avatar/1310118336631/card_errors.gif";
 		// String uploadUrl = "http://10.9.8.29:8080/phonebook/fileUploader/process";
 		String lineEnd = "\r\n";
 		String twoHyphens = "--";
 		String boundary =  "*****";
 
 		try
 		{
 		URL url = new URL(uploadUrl);
 		connection = (HttpURLConnection) url.openConnection();
 
 		// Allow Inputs & Outputs
 		// connection.setDoInput(true);
 		connection.setDoOutput(true);
 		// connection.setUseCaches(false);
 
 		// Enable POST method
 		connection.setRequestMethod("POST");
 
 		connection.setRequestProperty("Connection", "Keep-Alive");
 		connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
 		
 		//connection.setInstanceFollowRedirects(true);
 		// HttpURLConnection.setFollowRedirects(true);
 		Log.i(tag, "Follow redirects: "+connection.getInstanceFollowRedirects());
 
 		outputStream = new DataOutputStream( connection.getOutputStream() );
 		for (Map.Entry<String, String> entry : params.entrySet()) {
 		    String name = entry.getKey();
 		    String value = entry.getValue();
 		    Log.i(tag, "param: "+name+", value: "+value);
 		    if(value == null) continue;
 		    outputStream.writeBytes(twoHyphens + boundary + lineEnd);
 		    outputStream.writeBytes("Content-Disposition: form-data; name=\""+name+"\"" + lineEnd);
 		    outputStream.writeBytes(lineEnd);
 		    outputStream.writeBytes(value);
 		    outputStream.writeBytes(lineEnd);
 		}
 
 		outputStream.writeBytes(twoHyphens + boundary + lineEnd);
 		// outputStream.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + pathToOurFile +"\"" + lineEnd);
 		outputStream.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + "image."+contentType +"\"" + lineEnd);
 		outputStream.writeBytes(lineEnd);
 
 		outputStream.write(data, 0, data.length);
 
 		outputStream.writeBytes(lineEnd);
 		outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
 
 		// Responses from the server (code and message)
 		int serverResponseCode = connection.getResponseCode();
 		String serverResponseMessage = connection.getResponseMessage();
 		String location = connection.getHeaderField("Location");
 		Log.i(tag, "response code: "+serverResponseCode+", message: "+serverResponseMessage+", location: "+location);
 		if(serverResponseCode == 302)
 		{
 			url = new URL(location);
 			connection = (HttpURLConnection) url.openConnection();
 			connection.setRequestMethod("GET");
 			connection.connect();
 		}
 		
 		InputStream in = new BufferedInputStream(connection.getInputStream(), 8*1024);
 		String response = convertStreamToString(in);
 		Log.i(tag, "Response: "+response);
 		outputStream.flush();
 		outputStream.close();
 		return new JSONObject(response);
 		}
 		catch (Exception ex)
 		{
 			ex.printStackTrace();
 		}		
 		return null;
 	}
 	
     String convertStreamToString(InputStream is) throws IOException {
 	/*
 	 * To convert the InputStream to String we use the
 	 * Reader.read(char[] buffer) method. We iterate until the
 	 * Reader return -1 which means there's no more data to
 	 * read. We use the StringWriter class to produce the string.
 	 */
 		if (is != null) {
 		    Writer writer = new StringWriter();
 		
 		    char[] buffer = new char[1024];
 		    try {
 		        Reader reader = new BufferedReader(
 		                new InputStreamReader(is, "UTF-8"));
 		        int n;
 		        while ((n = reader.read(buffer)) != -1) {
 		            writer.write(buffer, 0, n);
 		        }
 		    } finally {
 		        is.close();
 		    }
 		    return writer.toString();
 		} else {        
 		    return "";
 		}
     }
 }
