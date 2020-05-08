 package com.charlesmadere.android.classygames.utilities;
 
 
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
 
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 import com.charlesmadere.android.classygames.models.Person;
 
 
 /**
  * Class for tons of stuff relating to communication with the Classy Games
  * server.
  */
 public final class ServerUtilities
 {
 
 
 	public final static String LOG_TAG = Utilities.LOG_TAG + " - ServerUtilities";
 	public final static String MIMETYPE_JSON = "application/json";
 
 
 	public final static String ADDRESS_MAIN = "http://classygames.elasticbeanstalk.com/";
 	public final static String ADDRESS_FORFEIT_GAME = ADDRESS_MAIN + "ForfeitGame";
 	public final static String ADDRESS_GET_GAME = ADDRESS_MAIN + "GetGame";
 	public final static String ADDRESS_GET_GAMES = ADDRESS_MAIN + "GetGames";
 	public final static String ADDRESS_NEW_GAME = ADDRESS_MAIN + "NewGame";
 	public final static String ADDRESS_NEW_MOVE = ADDRESS_MAIN + "NewMove";
 	public final static String ADDRESS_NEW_REG_ID = ADDRESS_MAIN + "NewRegId";
 	public final static String ADDRESS_REMOVE_REG_ID = ADDRESS_MAIN + "RemoveRegId";
 	public final static String ADDRESS_SKIP_MOVE = ADDRESS_MAIN + "SkipMove";
 
 
 	public final static String POST_DATA = "json";
 	public final static String POST_DATA_BOARD = "board";
 	public final static String POST_DATA_ERROR = "error";
 	public final static String POST_DATA_FINISHED = "finished";
 	public final static String POST_DATA_GAME_ID = "game_id";
 	public final static String POST_DATA_GAME_TYPE = "game_type";
 	public final static byte POST_DATA_GAME_TYPE_CHECKERS = 1;
 	public final static byte POST_DATA_GAME_TYPE_CHESS = 2;
 	public final static String POST_DATA_ID = "id";
 	public final static String POST_DATA_LAST_MOVE = "last_move";
 	public final static String POST_DATA_MESSAGE_TYPE = "message_type";
 	public final static byte POST_DATA_MESSAGE_TYPE_NEW_GAME = 1;
 	public final static byte POST_DATA_MESSAGE_TYPE_NEW_MOVE = 2;
 	public final static byte POST_DATA_MESSAGE_TYPE_GAME_OVER_LOSE = 7;
 	public final static byte POST_DATA_MESSAGE_TYPE_GAME_OVER_WIN = 15;
 	public final static String POST_DATA_NAME = "name";
 	public final static String POST_DATA_REG_ID = "reg_id";
 	public final static String POST_DATA_RESULT = "result";
 	public final static String POST_DATA_TURN = "turn";
 	public final static String POST_DATA_TURN_THEIRS = "turn_theirs";
 	public final static String POST_DATA_TURN_YOURS = "turn_yours";
 	public final static String POST_DATA_SUCCESS = "success";
 	public final static String POST_DATA_USER_CHALLENGED = "user_challenged";
 	public final static String POST_DATA_USER_CREATOR = "user_creator";
 
 
 	public final static String REG_ID = "REG_ID";
 
 
 
 
 	/**
 	 * Attempts to parse out the meaningful portion of the server's HTTP JSON
 	 * response.
 	 * 
 	 * @param serverResponse
 	 * The response as received from the server (a String).
 	 * 
 	 * @return
 	 * Returns true if a successful message was able to be parsed out of the
 	 * given server response.
 	 */
 	private static boolean gcmParseServerResults(final String serverResponse)
 	{
 		boolean wasSuccessMessageFound = false;
 
 		try
 		{
 			Log.d(LOG_TAG, "Parsing JSON data: " + serverResponse);
 			final JSONObject jsonData = new JSONObject(serverResponse);
 			final JSONObject jsonResult = jsonData.getJSONObject(POST_DATA_RESULT);
 
 			try
 			{
 				final String successMessage = jsonResult.getString(POST_DATA_SUCCESS);
 				Log.d(LOG_TAG, "Server returned success message: " + successMessage);
 
 				wasSuccessMessageFound = true;
 			}
 			catch (final JSONException e)
 			{
 				final String errorMessage = jsonResult.getString(POST_DATA_ERROR);
 				Log.d(LOG_TAG, "Server returned error message: " + errorMessage);
 			}
 		}
 		catch (final JSONException e)
 		{
 			Log.e(Utilities.LOG_TAG, "Server returned message that was unable to be properly parsed.", e);
 		}
 
 		return wasSuccessMessageFound;
 	}
 
 
 	/**
 	 * Attempts to register the current Android user's registration ID with the
 	 * server for GCM notifications.
 	 * 
 	 * @param regId
 	 * The Android regId to be sent to the server and used for GCM
 	 * notifications.
 	 * 
 	 * @param context
 	 * The Context of the class you're calling this method from.
 	 * 
 	 * @return
 	 * Returns true if the GCM registration attempt was successful.
 	 * 
 	 * @throws IOException
 	 * If something weird happens when trying to post the given data to the
 	 * server then this exception will be thrown.
 	 */
 	public static boolean gcmRegister(final String regId, final Context context) throws IOException
 	{
 		Log.d(LOG_TAG, "Registering device with regId of \"" + regId + "\" with GCM server.");
 
 		final Person whoAmI = Utilities.getWhoAmI(context);
 
 		// build the data to be sent to the server
 		final ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 		nameValuePairs.add(new BasicNameValuePair(POST_DATA_ID, whoAmI.getIdAsString()));
 		nameValuePairs.add(new BasicNameValuePair(POST_DATA_NAME, whoAmI.getName()));
 		nameValuePairs.add(new BasicNameValuePair(POST_DATA_REG_ID, regId));
 
 		if (gcmParseServerResults(postToServer(ADDRESS_NEW_REG_ID, nameValuePairs)))
 		{
 			Log.d(LOG_TAG, "Server successfully completed all the regId stuff.");
 
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 
 	/**
 	 * Attempts to register the current Android user's registration ID with the
 	 * server for GCM notifications.
 	 * 
 	 * @param context
 	 * The Context of the class that you're calling this method from.
 	 * 
 	 * @return
 	 * Returns true if the GCM registration attempt was successful.
 	 * 
 	 * @throws IOException
 	 * If something weird happens when trying to post the given data to the
 	 * server then this exception will be thrown.
 	 */
 	public static boolean gcmRegister(final Context context) throws IOException
 	{
		final String regId = Utilities.getRegId(context);
 
		if (Utilities.verifyValidString(regId))
 		{
			return gcmRegister(regId, context);
 		}
 		else
 		{
 			gcmPerformRegister(context);
 
 			return false;
 		}
 	}
 
 
 	/**
 	 * Performs the GCM registration process.
 	 * 
 	 * @param context
 	 * The Context of the class that you're calling this method from.
 	 */
 	public static void gcmPerformRegister(final Context context)
 	{
 		final Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
 		registrationIntent.putExtra("app", PendingIntent.getBroadcast(context, 0, new Intent(), 0));
 		registrationIntent.putExtra("sender", KeysAndConstants.GOOGLE_PROJECT_ID);
 		context.startService(registrationIntent);
 	}
 
 
 	/**
 	 * Attempts to unregister the current Android user's registration ID from
 	 * the server for GCM notifications.
 	 * 
 	 * @param context
 	 * The Context of the class you're calling this method from.
 	 * 
 	 * @throws IOException
 	 * If something weird happens when trying to post the given data to the
 	 * server then this exception will be thrown.
 	 */
 	public static void gcmUnregister(final Context context) throws IOException
 	{
 		Log.d(LOG_TAG, "Unregistering device with from GCM server.");
 
 		// build the data to be sent to the server
 		final ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 		nameValuePairs.add(new BasicNameValuePair(POST_DATA_ID, Utilities.getWhoAmI(context).getIdAsString()));
 
 		postToServer(ADDRESS_REMOVE_REG_ID, nameValuePairs);
 	}
 
 
 	/**
 	 * Use this method to send data to and then receive a response from the
 	 * server. The String that this method returns is the server's response.
 	 * 
 	 * <p><strong>Examples</strong><br />
 	 * ServerUtilities.postToServer(ServerUtilities.SERVER_NEW_MOVE_ADDRESS, postData);<br />
 	 * ServerUtilities.postToServer(ServerUtilities.SERVER_GET_GAMES_ADDRESS, postData);</p>
 	 * 
 	 * @param url
 	 * The URL that you want to send your data to. This should be formulated
 	 * using the URLs found in this class.
 	 * 
 	 * @param data
 	 * Data to be sent to the server using HTTP POST. This ArrayList will need
 	 * to be constructed outside of this method.
 	 * 
 	 * <p><strong>Example of data creation</strong><br />
 	 * ArrayList&#60;NameValuePair&#62; postData = new ArrayList&#60;NameValuePair&#62;();<br />
 	 * postData.add(new BasicNameValuePair(ServerUtilities.POST_DATA_ID, Long.valueOf(id).toString());<br />
 	 * postData.add(new BasicNameValuePair(ServerUtilities.POST_DATA_REG_ID, reg_id);<br />
 	 * Note that both values in the BasicNameValuePair <strong>must</strong>
 	 * be a String.</p>
 	 * 
 	 * @return
 	 * The server's response as a String. This will need to be parsed as it is
 	 * JSON data. <strong>There is a slight possibility that the data String
 	 * returned from this method will be null.</strong> Please check for that
 	 * <strong>as well as</strong> if the String is empty.
 	 * 
 	 * @throws IOException
 	 * If something weird happens when trying to post the given data to the
 	 * server then this exception will be thrown.
 	 */
 	public static String postToServer(final String url, final ArrayList<NameValuePair> data) throws IOException
 	{
 		String serverResponse = null;
 
 		Log.d(LOG_TAG, "Posting data to server at " + url);
 
 		final HttpPost httpPost = new HttpPost(url);
 		httpPost.setEntity(new UrlEncodedFormEntity(data));
 
 		final HttpClient httpClient = new DefaultHttpClient();
 		final HttpResponse httpResponse = httpClient.execute(httpPost);
 		final InputStream inputStream = httpResponse.getEntity().getContent();
 
 		if (inputStream != null)
 		{
 			final InputStreamReader inputStreamReader = new InputStreamReader(inputStream, HTTP.UTF_8);
 			final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
 			final StringBuilder stringBuilder = new StringBuilder();
 
 			for (String line = new String(); line != null; line = bufferedReader.readLine())
 			{
 				stringBuilder.append(line);
 			}
 
 			serverResponse = stringBuilder.toString();
 		}
 
 		return serverResponse;
 	}
 
 
 	/**
 	 * Ensures that a gameType byte received from the server is a valid
 	 * gameType byte.
 	 * 
 	 * @param gameType
 	 * The gameType byte to check for validity.
 	 * 
 	 * @return
 	 * Returns true if the given gameType byte is valid.
 	 */
 	public static boolean validGameTypeValue(final byte gameType)
 	{
 		switch (gameType)
 		{
 			case POST_DATA_GAME_TYPE_CHECKERS:
 			case POST_DATA_GAME_TYPE_CHESS:
 				return true;
 
 			default:
 				return false;
 		}
 	}
 
 
 	/**
 	 * Ensures that a messageType byte received from the server is a valid
 	 * messageType byte.
 	 * 
 	 * @param messageType
 	 * The messageType byte to check for validity.
 	 * 
 	 * @return
 	 * Returns true if the given messageType byte is valid.
 	 */
 	public static boolean validMessageTypeValue(final byte messageType)
 	{
 		switch (messageType)
 		{
 			case POST_DATA_MESSAGE_TYPE_NEW_GAME:
 			case POST_DATA_MESSAGE_TYPE_NEW_MOVE:
 			case POST_DATA_MESSAGE_TYPE_GAME_OVER_LOSE:
 			case POST_DATA_MESSAGE_TYPE_GAME_OVER_WIN:
 				return true;
 
 			default:
 				return false;
 		}
 	}
 
 
 	/**
 	 * Ensures that a winOrLose byte received from the server is a valid
 	 * winOrLose byte.
 	 * 
 	 * @param winOrLose
 	 * The winOrLose byte to check for validity.
 	 * 
 	 * @return
 	 * Returns true if the given winOrLose byte is valid.
 	 */
 	public static boolean validWinOrLoseValue(final byte winOrLose)
 	{
 		switch (winOrLose)
 		{
 			case POST_DATA_MESSAGE_TYPE_GAME_OVER_LOSE:
 			case POST_DATA_MESSAGE_TYPE_GAME_OVER_WIN:
 				return true;
 
 			default:
 				return false;
 		}
 	}
 
 
 }
