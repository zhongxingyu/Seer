 package eecs.berkeley.edu.cs294;
 
 /* Should not have android.sax.element */
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.util.Log;
 
 //TODO : server returns tododetail instead of group as a post response
 
 public class ServerConnection extends Activity {
 	
 	static public final int GET_REQUEST = 0;
 	static public final int CREATE_REQUEST = 1;
 	static public final int UPDATE_REQUEST = 2;
 	static public final int DELETE_REQUEST = 3;
 	static public final int UNSUBSCRIBE_REQUEST = 4;
 	static public final int ACCEPT_REQUEST = 5;
 	static public final int REJECT_REQUEST = 6;
 	
 	static public final int SENT_INV_SERVER_UPDATE = 0;
 	static public final int RECV_INV_SERVER_UPDATE = 1;
 	static public final int GROUP_SERVER_UPDATE = 2;
 	static public final int TODO_SERVER_UPDATE = 3;
 	static public final int USER_SERVER_UPDATE = 4;
 	
	///use 10.0.2.2 for localhost ip
	static final String homeurl = "http://blazing-galaxy-902.heroku.com/";
 	static final String users_link = "users/";
 	static final String groups_link = "/groups/";
 	static final String todolink = "/tododetails/";
 	static final String tmp_groups_link = "groups/";
 
 	
 	static final String my_groups_link = "/groups?format=xml";
 	static final String my_sent_invs_link = "/sent_invitations";
 	static final String my_recv_invs_link = "/recv_invitations";
 	
 	static final String group_todos_link = "/tododetails?format=xml";
 	static final String group_members_link = "/users?format=xml";
 	
 	static final String unsubscribe_link = "/unsubscribe";
 	static final String accept_link = "/accept";
 	static final String reject_link = "/reject";
 	
 	/*
 	 * Check if phone is connected to the internet
 	 */
 	public boolean isConnected() {
 		System.out.println("CONNECTED CALLED");
 		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
 		NetworkInfo netInfo = connManager.getActiveNetworkInfo();
 
 		if(netInfo == null) {
 			Log.d("ServerDEBUG", "--------------- No internet connection --------- ");
 			return false;
 		}
 
 		if (netInfo.isConnected()) {
 			Log.d("ServerDEBUG", "------------- Connected to internet -------------");
 			return true;
 		}
 
 		return false;
 	}
 	
 	/*
 	 * Main function that pulls all of the data in the server and synchronize it with the 
 	 * local database
 	 */
 	public static void pullAllRemote() {
 		/* Retrieving the different components of the system from server */
 		ArrayList<MySentInvitation> sentInvitationList = 
 			PullConnectionHelper.pullSentInvitations();
 		ArrayList<MyRecvInvitation> recvInvitationList = 
 			PullConnectionHelper.pullRecvInvitations();
 		ArrayList<MyGroup> groupList = 
 			PullConnectionHelper.pullGroups();		
 		ArrayList<MyTodo> todoList = 
 			PullConnectionHelper.pullTodos(groupList);
 		ArrayList<MyGroupMember> groupMemberList = 
 			PullConnectionHelper.pullGroupMembers(groupList);
 		
 		/* Synchronize! */
 		SynchDatabase.SynchSentInvitations(sentInvitationList);
 		SynchDatabase.SynchRecvInvitations(recvInvitationList);
 		SynchDatabase.SynchGroups(groupList);
 		SynchDatabase.SynchTodos(todoList);
 		SynchDatabase.SynchGroupMembers(groupMemberList);
 	}
 
 	/*
 	 * Synchronize sent invitations only
 	 */
 	public static void pullSynchSentInvitations() {
 		ArrayList<MySentInvitation> sentInvitationList = 
 			PullConnectionHelper.pullSentInvitations();
 		SynchDatabase.SynchSentInvitations(sentInvitationList);
 	}
 	
 	/*
 	 * Synchronize received invitations only
 	 */
 	public static void pullSynchRecvInvitations() {
 		ArrayList<MyRecvInvitation> recvInvitationList = 
 			PullConnectionHelper.pullRecvInvitations();
 		SynchDatabase.SynchRecvInvitations(recvInvitationList);
 	}
 	
 	/*
 	 * Synchronize the groups only
 	 */
 	public static void pullSynchGroups() {
 		ArrayList<MyGroup> groupList = 
 			PullConnectionHelper.pullGroups();
 		SynchDatabase.SynchGroups(groupList);		
 	}
 	
 	/*
 	 * Synchronize the todos only
 	 */
 	public static void pullSynchTodos() {
 		ArrayList<MyGroup> groupList = 
 			PullConnectionHelper.pullGroups();
 		ArrayList<MyTodo> todoList = 
 			PullConnectionHelper.pullTodos(groupList);
 		SynchDatabase.SynchTodos(todoList);		
 	}
 	
 	/*
 	 * Synchronize the group members only
 	 */
 	public static void pullSynchGroupMembers() {
 		ArrayList<MyGroup> groupList = 
 			PullConnectionHelper.pullGroups();
 		ArrayList<MyGroupMember> groupMemberList = 
 			PullConnectionHelper.pullGroupMembers(groupList);
 		SynchDatabase.SynchGroupMembers(groupMemberList);		
 	}
 	
 	/*
 	 * Get an xml response from the server
 	 */
 	public static String getResponse( HttpEntity entity )
 	{
 		String response = "";
 
 		try
 		{
 			int length = (int) entity.getContentLength();
 			StringBuffer sb = new StringBuffer( length );
 			InputStreamReader isr = new InputStreamReader(entity.getContent(), "UTF-8");
 			char buff[] = new char[length];
 			int cnt;
 			while ( ( cnt = isr.read( buff, 0, length - 1 ) ) > 0 )
 			{
 				sb.append( buff, 0, cnt );
 			}
 
 			response = sb.toString();
 			isr.close();
 		} catch ( IOException ioe ) {
 			ioe.printStackTrace();
 		}
 
 		return response;
 	}
 	
 	/*
 	 * Push local changes to the server
 	 */
 	public static void pushRemote(List<String> entry, int entry_type, int request_type) {
 		
 		int retCode = 0;
 		
 		if(entry == null) {
 			Log.d("ServerDEBUG", "entry: null");
 			return;
 		}
 		else {
 			Log.d("ServerDEBUG", "entry: " + entry.toString() + " with entry type: " + 
 					entry_type + " with request type: " + request_type);
 		}	
 		
 		switch(entry_type) {
 		case SENT_INV_SERVER_UPDATE:
 			switch(request_type) {
 			case CREATE_REQUEST:
 				retCode = PushServerSentInv.create(entry);
 				break;	
 			case DELETE_REQUEST:
 				retCode = PushServerSentInv.delete(entry);
 			
 			default:
 				Log.d("ServerDEBUG", "! Wrong request_type" + request_type);
 			}
 			break;
 		
 		case RECV_INV_SERVER_UPDATE:
 			switch(request_type) {
 			case CREATE_REQUEST:
 				retCode = PushServerRecvInv.accept(entry);
 				break;
 			case UPDATE_REQUEST:
 				retCode = PushServerRecvInv.reject(entry);
 				break;			
 			
 			default:
 				Log.d("ServerDEBUG", "! Wrong request_type" + request_type);				
 			}
 			
 			break;
 			
 		case GROUP_SERVER_UPDATE:
 			switch(request_type) {
 			case CREATE_REQUEST:
 				retCode = PushServerGroup.create(entry);
 				break;
 			case UPDATE_REQUEST:
 				retCode = PushServerGroup.update(entry);
 				break;			
 			case DELETE_REQUEST:
 				retCode = PushServerGroup.delete(entry);
 				break;
 			case UNSUBSCRIBE_REQUEST:
 				retCode = PushServerGroup.unsubscribe(entry);
 				break;
 			
 			default:
 				Log.d("ServerDEBUG", "! Wrong request_type" + request_type);
 			}
 			
 			break;
 			
 		case TODO_SERVER_UPDATE:
 			switch(request_type) {
 			case CREATE_REQUEST:
 				retCode = PushServerTodo.create(entry);
 				break;
 			case UPDATE_REQUEST:
 				retCode = PushServerTodo.update(entry);
 				break;			
 			case DELETE_REQUEST:
 				retCode = PushServerTodo.delete(entry);
 				break;
 				
 			default:
 				Log.d("ServerDEBUG", "! Wrong request_type" + request_type);
 			}
 			
 			break;
 			
 		case USER_SERVER_UPDATE:
 			switch(request_type) {
 			case CREATE_REQUEST:
 				retCode = PushServerUser.create(entry);
 				break;
 			case UPDATE_REQUEST:
 				retCode = PushServerUser.update(entry);
 				break;			
 			case DELETE_REQUEST:
 				retCode = PushServerUser.delete(entry);
 				break;
 				
 			default:
 				Log.d("ServerDEBUG", "! Wrong request_type" + request_type);
 			}
 			
 			break;
 		
 		default:
 			Log.d("ServerDEBUG", "! Wrong entry_type" + entry_type);
 		}
 	}
 	
 	/*
 	 * Send a POST request to the server when a new todo is created
 	 */
 	public static int pushPost(List<String> entry) {
 		Log.d("POSTING DEBUG", "POSTING CALEDEDJLIJHFLIHSHFE");
 		String url = homeurl + todolink; // For localhost use ip 10.0.2.2
 		DefaultHttpClient client = new DefaultHttpClient();
 		
 		HttpPost postRequest = new HttpPost(url);
 		
 		JSONObject posts = new JSONObject();
 		JSONObject details = new JSONObject();
 
 		/* Setting up the packet to be sent to server */
 		try {
 			details.put("title", entry.get(DatabaseHelper.TITLE_INDEX_T));
 			details.put("place", entry.get(DatabaseHelper.PLACE_INDEX_T));
 			details.put("tag", entry.get(DatabaseHelper.TAG_INDEX_T));
 			details.put("note", entry.get(DatabaseHelper.NOTE_INDEX_T));
 			details.put("status", entry.get(DatabaseHelper.STATUS_INDEX_T));
 			details.put("priority", entry.get(DatabaseHelper.PRIORITY_INDEX_T));
 
 			posts.put("tododetail", details);
 
 			Log.d("ServerDEBUG", "Event JSON = "+ posts.toString());
 
 			StringEntity se = new StringEntity(posts.toString());
 
 			postRequest.setEntity(se);
 			postRequest.setHeader("Content-Type","application/json");
 			postRequest.setHeader("Accept", "application/json");
 			
 		} catch (UnsupportedEncodingException e) {
 			Log.e("Error",""+e);
 			e.printStackTrace();	
 			return -1;
 		
 		} catch (JSONException js) {
 			js.printStackTrace();	
 			return -1;
 		}
 
 		/* Sending... */
 		HttpResponse response = null;
 		try {
 			response = client.execute(postRequest);
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			Log.e("ClientProtocol",""+e);
 			return -1;
 		} catch (IOException e) {
 			e.printStackTrace();
 			Log.e("IO",""+e);
 			return -1;
 		}
 
 		/* Parsing the response from the server */
 		HttpEntity entity = response.getEntity();
 		String stringResponse = getResponse(entity);
 		
 		if (entity != null) {
 			try {
 				entity.consumeContent();
 			} catch (IOException e) {
 				Log.e("IO E",""+e);
 				e.printStackTrace();
 				return -1;
 			}
 		}
 
 		Log.d("ServerDEBUG", "response: " + stringResponse);
 		
 		try {
 			JSONObject jObject = new JSONObject(stringResponse);
 /*TODO*/	JSONObject tododetailObject = jObject.getJSONObject("tododetail");
 			String railsID = tododetailObject.getString("id");
 			
 			int pk = Integer.parseInt(entry.get(DatabaseHelper.TD_ID_INDEX_T));
 			ToDo_Replica.dh.update_to_do(pk, null, null, null, null, 0, null, null, null, null, railsID);
 		} catch (Exception e) {
 			Log.e("JSON E", ""+e);
 			e.printStackTrace();
 			return -1;
 		}
 
 		return 0;
 	}
 	
 	/*
 	 * Send a PUT request to the server in the case of a todo being edited
 	 */
 	public static int pushPut(List<String> entry) {
 		
 		String url = homeurl + todolink + entry.get(DatabaseHelper.TO_DO_RAILS_ID_INDEX_T); 
 		
 		Log.d("ServerDEBUG", "PUT to " + url);
 		
 		DefaultHttpClient client = new DefaultHttpClient();
 		HttpPut putRequest = new HttpPut(url);
 		
 		JSONObject tododetail= new JSONObject();
 		JSONObject details = new JSONObject();
 
 		/* Setting up the packet to be sent to server */
 		try {
 			details.put("title", entry.get(DatabaseHelper.TITLE_INDEX_T));
 			details.put("place", entry.get(DatabaseHelper.PLACE_INDEX_T));
 			details.put("tag", entry.get(DatabaseHelper.TAG_INDEX_T));
 			details.put("note", entry.get(DatabaseHelper.NOTE_INDEX_T));
 			details.put("status", entry.get(DatabaseHelper.STATUS_INDEX_T));
 			details.put("priority", entry.get(DatabaseHelper.PRIORITY_INDEX_T));
 
 			tododetail.put("tododetail", details);
 
 			Log.d("ServerDEBUG", "Event JSON = "+ tododetail.toString());
 
 			StringEntity se = new StringEntity(tododetail.toString());
 
 			putRequest.setEntity(se);
 			putRequest.setHeader("Content-Type","application/json");
 			putRequest.setHeader("Accept", "application/json");
 			
 		} catch (UnsupportedEncodingException e) {
 			Log.e("Error",""+e);
 			e.printStackTrace();	
 			return -1;
 		
 		} catch (JSONException js) {
 			js.printStackTrace();	
 			return -1;
 		}
 
 		/* Sending... */
 		HttpResponse response = null;
 		try {
 			response = client.execute(putRequest);
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			Log.e("ClientProtocol",""+e);
 			return -1;
 		} catch (IOException e) {
 			e.printStackTrace();
 			Log.e("IO",""+e);
 			return -1;
 		}
 
 		/* Parsing the response from the server */
 		HttpEntity entity = response.getEntity();
 		String stringResponse = getResponse(entity);
 		if (entity != null) {
 			try {
 				entity.consumeContent();
 			} catch (IOException e) {
 				Log.e("IO E",""+e);
 				e.printStackTrace();
 				return -1;
 			}
 		}
 
 		Log.d("ServerDEBUG", "response: " + stringResponse);
 
 		return 0;
 	}
 	
 	/*
 	 * Send a DELETE request to the server in the case of a todo being deleted
 	 */
 	public static int pushDelete(List<String> entry) {
 		String url = homeurl + todolink + entry.get(DatabaseHelper.TO_DO_RAILS_ID_INDEX_T);
 		DefaultHttpClient client = new DefaultHttpClient();
 		HttpDelete deleteRequest = new HttpDelete(url);
 		HttpResponse response = null;
 
 		Log.d("ServerDEBUG", "DELETE to " + url);
 		
 		/* Sending... */
 		try {
 			response = client.execute(deleteRequest);
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			Log.e("ClientProtocol",""+e);
 			return -1;
 		} catch (IOException e) {
 			e.printStackTrace();
 			Log.e("IO",""+e);
 			return -1;
 		}
 
 		/* Parsing the response from the server */
 		HttpEntity entity = response.getEntity();
 		String stringResponse = getResponse(entity);
 		if (entity != null) {
 			try {
 				entity.consumeContent();
 			} catch (IOException e) {
 				Log.e("IO E",""+e);
 				e.printStackTrace();
 				return -1;
 			}
 		}
 
 		Log.d("ServerDEBUG", "response: " + stringResponse);
 		return 0;
 	}
 }
