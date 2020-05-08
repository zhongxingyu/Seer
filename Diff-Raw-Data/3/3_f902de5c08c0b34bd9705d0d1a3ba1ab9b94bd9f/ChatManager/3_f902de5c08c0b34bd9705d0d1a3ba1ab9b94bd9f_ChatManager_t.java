 package realtalk.util;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * ChatManager is a helper class that allows the Android side of RealTalk to cleanly
  * communicate with the server while keeping it abstracted.
  * 
  * @author Taylor Williams
  *
  */
 public final class ChatManager {
 	
 	//HUNGARIAN TAGS:
 	//	rrs		RequestResultSet
 	//	pmrs	PullMessageResultSet
 	//	crrs	ChatRoomResultSet
 	
 	public static final String URL_QUALIFIER = CommonUtilities.URL_QUALIFIER;
 	
 	//User servlets
     public static final String URL_ADD_USER = CommonUtilities.URL_ADD_USER;
     public static final String URL_REMOVE_USER = CommonUtilities.URL_REMOVE_USER;
     public static final String URL_AUTHENTICATE = CommonUtilities.URL_AUTHENTICATE;
     public static final String URL_CHANGE_PASSWORD = CommonUtilities.URL_CHANGE_PASSWORD;
     public static final String URL_CHANGE_ID = CommonUtilities.URL_CHANGE_ID;
     //Chat room servlets
     public static final String URL_ADD_ROOM = CommonUtilities.URL_ADD_ROOM;
     public static final String URL_JOIN_ROOM = CommonUtilities.URL_JOIN_ROOM;
     public static final String URL_LEAVE_ROOM = CommonUtilities.URL_LEAVE_ROOM;
     public static final String URL_POST_MESSAGE = CommonUtilities.URL_POST_MESSAGE;
     public static final String URL_GET_RECENT_MESSAGES = CommonUtilities.URL_GET_RECENT_MESSAGES;
     public static final String URL_GET_ALL_MESSAGES = CommonUtilities.URL_GET_ALL_MESSAGES;
     public static final String URL_GET_NEARBY_CHATROOMS = CommonUtilities.URL_GET_NEARBY_CHATROOMS;
     public static final String URL_GET_USERS_ROOMS = CommonUtilities.URL_GET_USERS_ROOMS;
     
     
 	/**
 	 * Private contructor prevents this class from being instantiated.
 	 */
     private ChatManager() {
     	throw new UnsupportedOperationException("ChatManager is a utility class and should not be instantiated.");
     }
     
     /**
      * This method makes a request to the server using the given url and params and parses
      * the expected JSON response as a JSON Object. If the call fails or if the response is
      * not valid json, an empty JSON Object is returned.
      * 
      * @param stUrl    Url to query from.
      * @param rgparams Params to use.
      * @return         JSONObject that describes the response.
      */
     private static JSONObject makeRequest(String stUrl, List<NameValuePair> rgparams) {
     	// Retrieve Stream from URL
     	InputStream inputstreamResponse = null;
     	try {
 			inputstreamResponse = HttpUtility.sendPostRequest(stUrl, rgparams);
 		} catch (UnsupportedOperationException e) {
 			e.printStackTrace();
 			return new JSONObject();
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return new JSONObject();
 		} catch (IOException e) {
 			e.printStackTrace();
 			return new JSONObject();
 		}
     	
     	// Parse Stream to JSON Object
     	JSONObject jsonobjectResponse = null;
     	
     	try {
 			jsonobjectResponse = JSONParser.parseStream(inputstreamResponse);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return new JSONObject();
 		} catch (IOException e) {
 			e.printStackTrace();
 			return new JSONObject();
 		}
     	
     	return jsonobjectResponse;
     }
     
     /**
      * @param messageinfo         Message info object
      * @return the list of parameters as basic name value pairs
      * @throws UnsupportedEncodingException 
      */
     private static List<NameValuePair> rgparamsMessageInfo(MessageInfo messageinfo) throws UnsupportedEncodingException {
         List<NameValuePair> rgparams = new ArrayList<NameValuePair>();
         rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_MESSAGE_TIMESTAMP, Long.valueOf(messageinfo.timestampGet().getTime()).toString()));
         rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_MESSAGE_BODY, URLEncoder.encode(messageinfo.stBody(), "UTF-8")));
         rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_MESSAGE_SENDER, URLEncoder.encode(messageinfo.stSender(), "UTF-8")));
         return rgparams;
     }
     
     /**
      * @param userinfo         User info object
      * @return the list of parameters as basic name value pairs
      * @throws UnsupportedEncodingException 
      */
     private static List<NameValuePair> rgparamsUserBasicInfo(UserInfo userinfo) throws UnsupportedEncodingException {
         List<NameValuePair> rgparams = new ArrayList<NameValuePair>();
         rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_REG_ID, userinfo.stRegistrationId()));
 		rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_USER, URLEncoder.encode(userinfo.stUserName(), "UTF-8")));
         rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_PWORD, userinfo.stPassword()));
         return rgparams;
     }
     
     /**
      * @param chatroominfo         Chat room info object
      * @return the list of parameters as basic name value pairs
      * @throws UnsupportedEncodingException 
      */
     private static List<NameValuePair> rgparamsChatRoomBasicInfo(ChatRoomInfo chatroominfo) throws UnsupportedEncodingException {
         List<NameValuePair> rgparams = new ArrayList<NameValuePair>();
         rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_ROOM_NAME, URLEncoder.encode(chatroominfo.stName(), "UTF_8")));
         rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_ROOM_ID, chatroominfo.stId()));
         rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_ROOM_DESCRIPTION, URLEncoder.encode(chatroominfo.stDescription(), "UTF-8")));
         rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_USER_LATITUDE, Double.valueOf(chatroominfo.getLatitude()).toString()));
         rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_USER_LONGITUDE, Double.valueOf(chatroominfo.getLongitude()).toString()));
         return rgparams;
     }
     
     /**
      * @param latitude
      * @param longitude
      * @param radiusMeters
      * @return the list of parameters as basic name value pairs
      */
     private static List<NameValuePair> rgparamsLocationInfo(double latitude, double longitude, double radiusMeters) {
         List<NameValuePair> rgparams = new ArrayList<NameValuePair>();
         rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_USER_LATITUDE, Double.valueOf(latitude).toString()));
         rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_USER_LONGITUDE, Double.valueOf(longitude).toString()));
         rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_USER_RADIUS, Double.valueOf(radiusMeters).toString()));
         return rgparams;
     }
     
     /**
      * 
      * @param fAnon
      * @return a parameter for anonymous login as a basic name value pair
      */
     private static NameValuePair paramAnonymous(boolean fAnon) {
     	return new BasicNameValuePair(RequestParameters.PARAMETER_ANON, String.valueOf(fAnon));
     }
     
     /**
      * @param rgparam         List of parameters to embed in the request
      * @param stUrl			The url to send the request to
      * @return A RequestResultSet containing the result of the request
      */
     private static RequestResultSet rrsPostRequest(List<NameValuePair> rgparam, String stUrl) {
 
 		JSONObject json = makeRequest(stUrl, rgparam);
         try {
         	boolean fSucceeded = json.getString(RequestParameters.PARAMETER_SUCCESS).equals("true");
         	String stErrorCode = fSucceeded ? "NO ERROR MESSAGE" : json.getString(ResponseParameters.PARAMETER_ERROR_CODE);
         	String stErrorMessage = fSucceeded ? "NO ERROR MESSAGE" : json.getString(ResponseParameters.PARAMETER_ERROR_MSG);
         	return new RequestResultSet(fSucceeded, stErrorCode, stErrorMessage);
         } catch (JSONException e) {
             e.printStackTrace();
             //if all else fails, return generic error code and message
         	return new RequestResultSet(false, "REQUEST FAILED", 
         			"REQUEST FAILED");
         }
         
     }
     
     /**
      * @param rgparam         List of parameters to embed in the request
      * @param stUrl			The url to send the request to
      * @return A RequestResultSet containing the result of the request
      */
     private static ChatRoomResultSet crrsPostRequest(List<NameValuePair> rgparam, String stUrl) {
     	JSONObject json = makeRequest(stUrl, rgparam);
         try {
         	boolean fSucceeded = json.getString(RequestParameters.PARAMETER_SUCCESS).equals("true");
         	if (fSucceeded) {
         		List<ChatRoomInfo> rgchatroominfo = new ArrayList<ChatRoomInfo>();
         		//get list of rooms from response
         		JSONArray rgroom = json.getJSONArray(RequestParameters.PARAMETER_ROOM_ROOMS);
         		for (int i = 0; i < rgroom.length(); i++) {
         			JSONObject jsonobject = rgroom.getJSONObject(i);
         			String stName = URLDecoder.decode(jsonobject.getString(RequestParameters.PARAMETER_ROOM_NAME), "UTF-8");
         			String stId = jsonobject.getString(RequestParameters.PARAMETER_ROOM_ID);
         			String stDescription = URLDecoder.decode(jsonobject.getString(RequestParameters.PARAMETER_ROOM_DESCRIPTION), "UTF-8");
         			double latitude = jsonobject.getDouble(RequestParameters.PARAMETER_ROOM_LATITUDE);
         			double longitude = jsonobject.getDouble(RequestParameters.PARAMETER_ROOM_LONGITUDE);
         			String stCreator = URLDecoder.decode(jsonobject.getString(RequestParameters.PARAMETER_ROOM_CREATOR), "UTF-8");
         			int numUsers = jsonobject.getInt(RequestParameters.PARAMETER_ROOM_NUM_USERS);
         			long ticks = jsonobject.getLong(RequestParameters.PARAMETER_TIMESTAMP);
         			rgchatroominfo.add(new ChatRoomInfo(stName, stId, stDescription, latitude, longitude, stCreator, numUsers, new Timestamp(ticks)));
         		}
         		return new ChatRoomResultSet(true, rgchatroominfo, "NO ERROR CODE", "NO ERROR MESSAGE");
         	}
         	return new ChatRoomResultSet(false, ResponseParameters.RESPONSE_ERROR_CODE_ROOM, 
         			ResponseParameters.RESPONSE_MESSAGE_ERROR);
         } catch (JSONException e) {
             e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
         	e.printStackTrace();
         }
         //if all else fails, return generic error code and message
     	return new ChatRoomResultSet(false, "REQUEST FAILED", "REQUEST FAILED");
     }
     
     /** Sends a message/chatroom specific request.
      * @param rgparam         List of parameters to embed in the request
      * @param stUrl			The url to send the request to
      * @return A PullMessageResultSet containing the result of the request
      */
     private static PullMessageResultSet pmrsPostRequest(List<NameValuePair> rgparam, String stUrl) {
     	JSONObject json = makeRequest(stUrl, rgparam);
         try {
         	boolean fSucceeded = json.getString(RequestParameters.PARAMETER_SUCCESS).equals("true");
         	if (fSucceeded) {
         		List<MessageInfo> rgmessageinfo = new ArrayList<MessageInfo>();
         		JSONArray rgmessage = json.getJSONArray(RequestParameters.PARAMETER_MESSAGE_MESSAGES);
         		for (int i = 0; i < rgmessage.length(); i++) {
         			JSONObject jsonobject = rgmessage.getJSONObject(i);
         			String stBody = URLDecoder.decode(jsonobject.getString(RequestParameters.PARAMETER_MESSAGE_BODY), "UTF-8");
         			long ticks = jsonobject.getLong(RequestParameters.PARAMETER_MESSAGE_TIMESTAMP);
         			String stSender = URLDecoder.decode(jsonobject.getString(RequestParameters.PARAMETER_MESSAGE_SENDER), "UTF-8");
         			rgmessageinfo.add(new MessageInfo(stBody, stSender, ticks));
         		}
         		return new PullMessageResultSet(true, rgmessageinfo, "NO ERROR CODE", "NO ERROR MESSAGE");
         	}
             return new PullMessageResultSet(false, new ArrayList<MessageInfo>(), 
             		json.getString(ResponseParameters.PARAMETER_ERROR_CODE), 
             		json.getString(ResponseParameters.PARAMETER_ERROR_MSG));
         } catch (JSONException e) {
             e.printStackTrace();
         } catch (UnsupportedEncodingException e) {
         	e.printStackTrace();
         }
         //if all else fails, return generic error code and message
     	return new PullMessageResultSet(false, "REQUEST FAILED", "REQUEST FAILED");
     }
 	
     /** Authenticates a user
      * @param userinfo		The user to authenticate
      * @return A resultset containing the result of the authentication
      */
 	public static RequestResultSet rrsAuthenticateUser(UserInfo userinfo) {
         List<NameValuePair> rgparams = null;
 		try {
 			rgparams = rgparamsUserBasicInfo(userinfo);
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
         return rrsPostRequest(rgparams, URL_AUTHENTICATE);
 	}
 	
     /** Adds a user
      * @param userinfo		The user to add
      * @return A resultset containing the result of the addition
      */
 	public static RequestResultSet rrsAddUser(UserInfo userinfo) {
         List<NameValuePair> rgparams = null;
 		try {
 			rgparams = rgparamsUserBasicInfo(userinfo);
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
         return rrsPostRequest(rgparams, URL_ADD_USER);
 	}
 	
     /** Remove a user
      * @param userinfo		The user to remove
      * @return A resultset containing the result of the removal
      */
 	public static RequestResultSet rrsRemoveUser(UserInfo userinfo) {
         List<NameValuePair> rgparams = null;
 		try {
 			rgparams = rgparamsUserBasicInfo(userinfo);
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
         return rrsPostRequest(rgparams, URL_REMOVE_USER);
 	}
 	
     /** Changes a user's password
      * @param userinfo		The user to change
      * @param stPasswordNew		The new password
      * @return A resultset containing the result of the change
      */
 	public static RequestResultSet rrsChangePassword(UserInfo userinfo, String stPasswordNew) {
         List<NameValuePair> rgparams = null;
 		try {
 			rgparams = rgparamsUserBasicInfo(userinfo);
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
         rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_NEW_PWORD, stPasswordNew));
         return rrsPostRequest(rgparams, URL_CHANGE_PASSWORD);
 	}
 	
     /** Changes a user's ID
      * @param userinfo		The user to change
      * @param stIdNew		The new ID
      * @return A resultset containing the result of the change
      */
 	public static RequestResultSet rrsChangeID(UserInfo userinfo, String stIdNew) {
         List<NameValuePair> rgparams = null;
 		try {
 			rgparams = rgparamsUserBasicInfo(userinfo);
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
         rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_NEW_REG_ID, stIdNew));
         return rrsPostRequest(rgparams, URL_CHANGE_ID);
 	}
 	
     /** Adds a new chatroom
      * @param chatroominfo		The chatroom to add
      * @param userinfo		The user to associate with the new room
      * @return A resultset containing the result of the addition
      */
 	public static RequestResultSet rrsAddRoom(ChatRoomInfo chatroominfo, UserInfo userinfo) {
         List<NameValuePair> rgparams = null;
 		try {
 			rgparams = rgparamsChatRoomBasicInfo(chatroominfo);
 	        rgparams.addAll(rgparamsUserBasicInfo(userinfo));
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		return rrsPostRequest(rgparams, URL_ADD_ROOM);
 	}
 	
     /** Joins a user to a chatroom
      * @param chatroominfo		The chatroom to join
      * @param userinfo		The user to join into the room
      * @return A resultset containing the result of the join
      */
 	public static RequestResultSet rrsJoinRoom(UserInfo userinfo, ChatRoomInfo chatroominfo, boolean fAnon) {
         List<NameValuePair> rgparams = null;
 		try {
 			rgparams = rgparamsUserBasicInfo(userinfo);
 	        rgparams.addAll(rgparamsChatRoomBasicInfo(chatroominfo));
 	        rgparams.add(paramAnonymous(fAnon));
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		return rrsPostRequest(rgparams, URL_JOIN_ROOM);
 	}
 	
     /** Leaves a chatroom
      * @param chatroominfo		The chatroom to leave
      * @param userinfo		The user leaving the room
      * @return A resultset containing the result of the leave
      */
 	public static RequestResultSet rrsLeaveRoom(UserInfo userinfo, ChatRoomInfo chatroominfo) {
         List<NameValuePair> rgparams = null;
 		try {
 			rgparams = rgparamsUserBasicInfo(userinfo);
 	        rgparams.addAll(rgparamsChatRoomBasicInfo(chatroominfo));
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		return rrsPostRequest(rgparams, URL_LEAVE_ROOM);
 	}
 	
     /** Posts a message to a chatroom
      * @param chatroominfo		The chatroom to post a message to
      * @param userinfo		The user posting the message
      * @return A resultset containing the result of the post
      */
 	public static RequestResultSet rrsPostMessage(UserInfo userinfo, ChatRoomInfo chatroominfo, MessageInfo message) {
         List<NameValuePair> rgparams = null;
 		try {
 			rgparams = rgparamsUserBasicInfo(userinfo);
 	        rgparams.addAll(rgparamsChatRoomBasicInfo(chatroominfo));
 	        rgparams.addAll(rgparamsMessageInfo(message));
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		return rrsPostRequest(rgparams, URL_POST_MESSAGE);
 	}
 	
     /** Returns the chatlog for a certain chatroom
      * @param chatroominfo		The chatroom to pull the log from
      * @return A resultset containing the result of the pull
      */
 	public static PullMessageResultSet pmrsChatLogGet(ChatRoomInfo chatroominfo) {
         List<NameValuePair> rgparams = null;
 		try {
 			rgparams = rgparamsChatRoomBasicInfo(chatroominfo);
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		return pmrsPostRequest(rgparams, URL_GET_ALL_MESSAGES);
 	}
 	
 	/**
 	 * This method pulls all recent messages to a specific given chatroom after a given time as indicated
 	 * in timestamp
 	 * 
 	 * @param chatroominfo Information about chatroom to pull messages from.
 	 * @param timestamp    Time 
 	 * @return             Result set that contains a boolean that indicates success or failure and 
 	 *                     returns an error code and message if failure was occurred. If success,
 	 *                     it returns a list of MessageInfo that have a timestamp later than the given
 	 *                     timestamp
 	 *                     
 	 */
 	@Deprecated
 	public static PullMessageResultSet pmrsChatRecentChat(ChatRoomInfo chatroominfo, Timestamp timestamp) {
 		long rawtimestamp = timestamp.getTime();
 		String stTimestamp = "";
 		try {
 			stTimestamp = String.valueOf(rawtimestamp);
 		} catch (NumberFormatException e) {
 			e.printStackTrace();
 			return new PullMessageResultSet(false, "ERROR_INVALID_TIMESTAMP", "ERROR_MESSAGE_PARSING_ERROR");
 		}
 		List<NameValuePair> rgparams = null;
 		try {
 			rgparams = rgparamsChatRoomBasicInfo(chatroominfo);
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_TIMESTAMP, stTimestamp));
 		return pmrsPostRequest(rgparams, URL_GET_RECENT_MESSAGES);
 	}
 	
 	/**
 	 * This method pulls all nearby chatrooms, given a latitude, longitude, and a radius.
 	 * 
 	 * @param latitude	users latitude
 	 * @param longitude	users longitude
 	 * @param radiusMeters radius from the user in which to find chatrooms
 	 * @return             Result set that contains a boolean that indicates success or failure and 
 	 *                     returns an error code and message if failure was occurred. If success,
 	 *                     it holds a list of ChatRoomInfo objects describing the nearby rooms.
 	 */
 	public static ChatRoomResultSet crrsNearbyChatrooms(double latitude, double longitude, double radiusMeters) {
 		List<NameValuePair> rgparams = rgparamsLocationInfo(latitude, longitude, radiusMeters);
 		return crrsPostRequest(rgparams, URL_GET_NEARBY_CHATROOMS);
 	}
 	
 	/**
 	 * This method pulls all chatrooms that the given user has joined from the server
 	 * 
 	 * @param userinfo     Information about the user
      * @return             Result set that contains a boolean that indicates success or failure and 
      *                     returns an error code and message if failure was occurred. If success,
      *                     it holds a list of ChatRoomInfo objects describing the user's rooms.
 	 */
 	public static ChatRoomResultSet crrsUsersChatrooms(UserInfo userinfo) {
 	    List<NameValuePair> rgparams = null;
 		try {
 			rgparams = rgparamsUserBasicInfo(userinfo);
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 	    return crrsPostRequest(rgparams, URL_GET_USERS_ROOMS);
 	}
 }
