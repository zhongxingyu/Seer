 package edu.selu.android.classygames.utilities;
 
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.util.Log;
 
 
 public class ServerUtilities
 {
 
 
 	public final static String MIMETYPE_JSON = "application/json";
 
 	public final static String POST_DATA = "json";
 	public final static String POST_DATA_BOARD = "board";
 	public final static String POST_DATA_ERROR = "error";
 	public final static String POST_DATA_FINISHED = "finished";
 	public final static String POST_DATA_GAME_ID = "game_id";
 	public final static String POST_DATA_ID = "id";
 	public final static String POST_DATA_LAST_MOVE = "last_move";
 	public final static String POST_DATA_NAME = "name";
 	public final static String POST_DATA_REG_ID = "reg_id";
 	public final static String POST_DATA_RESULT = "result";
 	public final static String POST_DATA_TURN = "turn";
 	public final static String POST_DATA_TURN_THEIRS = "turn_theirs";
 	public final static String POST_DATA_TURN_YOURS = "turn_yours";
 	public final static String POST_DATA_TYPE = "type";
 	public final static byte POST_DATA_TYPE_NEW_GAME = 1;
 	public final static byte POST_DATA_TYPE_NEW_MOVE = 2;
 	public final static byte POST_DATA_TYPE_GAME_OVER_LOSE = 7;
 	public final static byte POST_DATA_TYPE_GAME_OVER_WIN = 15;
 	public final static String POST_DATA_SUCCESS = "success";
 	public final static String POST_DATA_USER_CHALLENGED = "user_challenged";
 	public final static String POST_DATA_USER_CREATOR = "user_creator";
 
	public final static String SERVER_ADDRESS = "http://classygames.elasticbeanstalk.com/";
 	public final static String SERVER_GET_GAME = "GetGame";
 	public final static String SERVER_GET_GAME_ADDRESS = SERVER_ADDRESS + SERVER_GET_GAME;
 	public final static String SERVER_GET_GAMES = "GetGames";
 	public final static String SERVER_GET_GAMES_ADDRESS = SERVER_ADDRESS + SERVER_GET_GAMES;
 	public final static String SERVER_NEW_GAME = "NewGame";
 	public final static String SERVER_NEW_GAME_ADDRESS = SERVER_ADDRESS + SERVER_NEW_GAME;
 	public final static String SERVER_NEW_MOVE = "NewMove";
 	public final static String SERVER_NEW_MOVE_ADDRESS = SERVER_ADDRESS + SERVER_NEW_MOVE;
 	public final static String SERVER_NEW_REG_ID = "NewRegId";
 	public final static String SERVER_NEW_REG_ID_ADDRESS = SERVER_ADDRESS + SERVER_NEW_REG_ID;
 	public final static String SERVER_REMOVE_REG_ID = "RemoveRegId";
 	public final static String SERVER_REMOVE_REG_ID_ADDRESS = SERVER_ADDRESS + SERVER_REMOVE_REG_ID;
 
 
 	private static boolean GCMParseServerResults(final String jsonString)
 	{
 		boolean returnValue = false;
 
 		try
 		{
 			Log.d(Utilities.LOG_TAG, "Parsing JSON data: " + jsonString);
 			final JSONObject jsonData = new JSONObject(jsonString);
 			final JSONObject jsonResult = jsonData.getJSONObject(POST_DATA_RESULT);
 
 			try
 			{
 				final String successMessage = jsonResult.getString(POST_DATA_SUCCESS);
 				Log.d(Utilities.LOG_TAG, "Server returned success with message: " + successMessage);
 
 				returnValue = true;
 			}
 			catch (final JSONException e)
 			{
 				try
 				{
 					final String errorMessage = jsonResult.getString(POST_DATA_ERROR);
 					Log.d(Utilities.LOG_TAG, "Server returned error with message: " + errorMessage);
 				}
 				catch (final JSONException e1)
 				{
 					Log.e(Utilities.LOG_TAG, "Data returned from server contained no error message.", e1);
 				}
 			}
 		}
 		catch (final JSONException e)
 		{
 			Log.e(Utilities.LOG_TAG, "Server returned message that was unable to be properly parsed.", e);
 		}
 
 		return returnValue;
 	}
 
 
 	public static void GCMRegister(final String reg_id, final Context context)
 	{
 		Log.d(Utilities.LOG_TAG, "Registering device with reg_id of \"" + reg_id + "\" from GCM server.");
 
 		// build the data to be sent to the server
 		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 		nameValuePairs.add(new BasicNameValuePair(POST_DATA_ID, Long.valueOf(Utilities.getWhoAmI(context).getId()).toString()));
 		nameValuePairs.add(new BasicNameValuePair(POST_DATA_NAME, Utilities.getWhoAmI(context).getName()));
 		nameValuePairs.add(new BasicNameValuePair(POST_DATA_REG_ID, reg_id));
 
 		try
 		{
 			if (GCMParseServerResults(postToServer(SERVER_NEW_REG_ID_ADDRESS, nameValuePairs)))
 			{
 				Log.d(Utilities.LOG_TAG, "Server successfully completed all the reg_id stuff.");
 			}
 		}
 		catch (final IOException e)
 		{
 
 		}
 	}
 
 
 	public static void GCMUnregister(final String reg_id, final Context context)
 	{
 		Log.d(Utilities.LOG_TAG, "Unregistering device with reg_id of \"" + reg_id + "\" from GCM server.");
 
 		// build the data to be sent to the server
 		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 		nameValuePairs.add(new BasicNameValuePair(POST_DATA_ID, Long.valueOf(Utilities.getWhoAmI(context).getId()).toString()));
 		nameValuePairs.add(new BasicNameValuePair(POST_DATA_REG_ID, reg_id));
 
 		try
 		{
 			postToServer(SERVER_REMOVE_REG_ID_ADDRESS, nameValuePairs);
 		}
 		catch (final IOException e)
 		{
 
 		}
 	}
 
 
 	/**
 	 * Use this method to send data to and then receive a response from the server.
 	 * 
 	 * <p><strong>Examples</strong><br />
 	 * ServerUtilities.postToServer(ServerUtilities.SERVER_NEW_MOVE_ADDRESS, postData);<br />
 	 * ServerUtilities.postToServer(ServerUtilities.SERVER_GET_GAMES_ADDRESS, postData);</p>
 	 * 
 	 * @param url
 	 * The URL that you want to send your data to. This should be formulated using the URLs found
 	 * in this class.
 	 * 
 	 * @param data
 	 * Data to be sent to the server using HTTP POST. This ArrayList will need to be constructed
 	 * outside of this method.
 	 * <p><strong>Example of data creation</strong><br />
 	 * ArrayList&#60;NameValuePair&#62; postData = new ArrayList&#60;NameValuePair&#62;();<br />
 	 * postData.add(new BasicNameValuePair(ServerUtilities.POST_DATA_ID, Long.valueOf(id).toString());<br />
 	 * postData.add(new BasicNameValuePair(ServerUtilities.POST_DATA_REG_ID, reg_id);<br />
 	 * Note that both values in the BasicNameValuePair <strong>must</strong> be a String.</p>
 	 * 
 	 * @return
 	 * The server's response as a String. This will need to be parsed as it is JSON data.
 	 * <strong>There is a slight possibility that the data String returned from this method will be
 	 * null.</strong> Please check for that <strong>as well as</strong> if the String is empty.
 	 */
 	public static String postToServer(final String url, final ArrayList<NameValuePair> data) throws IOException
 	{
 		Log.d(Utilities.LOG_TAG, "Posting data to server at " + url);
 		String jsonString = null;
 
 		try
 		{
 			HttpPost httpPost = new HttpPost(url);
 			httpPost.setEntity(new UrlEncodedFormEntity(data));
 
 			HttpClient httpClient = new DefaultHttpClient();
 			HttpResponse httpResponse = httpClient.execute(httpPost);
 
 			InputStream inputStream = httpResponse.getEntity().getContent();
 
 			if (inputStream != null)
 			{
 				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, HTTP.UTF_8));
 				StringBuilder stringBuilder = new StringBuilder();
 
 				for (String line = new String(); line != null; line = bufferedReader.readLine())
 				{
 					stringBuilder.append(line);
 				}
 
 				jsonString = new String(stringBuilder.toString());
 				Log.d(Utilities.LOG_TAG, "Parsed result from server: " + jsonString);
 			}
 		}
 		catch (final IllegalArgumentException e)
 		{
 			Log.e(Utilities.LOG_TAG, "Error in HTTP connection.", e);
 		}
 
 		return jsonString;
 	}
 
 
 	public static boolean validGameTypeValue(final byte gameType)
 	{
 		switch (gameType)
 		{
 			case POST_DATA_TYPE_NEW_GAME:
 			case POST_DATA_TYPE_NEW_MOVE:
 			case POST_DATA_TYPE_GAME_OVER_LOSE:
 			case POST_DATA_TYPE_GAME_OVER_WIN:
 				return true;
 
 			default:
 				return false;
 		}
 	}
 
 
 	public static boolean validWinOrLoseValue(final byte winOrLose)
 	{
 		switch (winOrLose)
 		{
 			case POST_DATA_TYPE_GAME_OVER_LOSE:
 			case POST_DATA_TYPE_GAME_OVER_WIN:
 				return true;
 
 			default:
 				return false;
 		}
 	}
 
 
 }
