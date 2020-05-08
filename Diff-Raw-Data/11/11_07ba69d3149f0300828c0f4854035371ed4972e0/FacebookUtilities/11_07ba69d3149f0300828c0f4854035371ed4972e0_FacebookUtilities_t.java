 package com.charlesmadere.android.classygames.utilities;
 
 
 import android.content.Context;
 import android.content.SharedPreferences;
 
 /**
  * Class for constants and methods relating to Facebook. Further documentation
  * on what this class is doing can be found on the Facebook website:
  * https://developers.facebook.com/docs/reference/api/using-pictures/
  */
 public final class FacebookUtilities
 {
 
 
 	private final static String GRAPH_API_URL = "https://graph.facebook.com/";
 	private final static String PICTURE = "/picture";
 	private final static String TYPE = "?type=";
 	private final static String ACCESS_TOKEN = "&access_token=";
 	private final static String RETURN_SSL_RESOURCES_1 = "&return_ssl_resources=1";
 
 	private final static String PICTURE_LARGE = PICTURE + TYPE + "large" + RETURN_SSL_RESOURCES_1;
 	private final static String PICTURE_NORMAL = PICTURE + TYPE + "normal" + RETURN_SSL_RESOURCES_1;
 	private final static String PICTURE_SMALL = PICTURE + TYPE + "small" + RETURN_SSL_RESOURCES_1;
 	private final static String PICTURE_SQUARE = PICTURE + TYPE + "square" + RETURN_SSL_RESOURCES_1;
 
 
 	/**
 	 * The user's Facebook Access Token. Note that it's possible for this
 	 * String to be null. More about access tokens here:
 	 * https://developers.facebook.com/docs/getting-started/graphapi/#login
 	 */
 	private static String accessToken;
 	private static final String KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN";
 
 
 
 
 	/**
 	 * Returns the complete Facebook API access token query string. So that
 	 * would be "&access_token=FC030F". Please only use this method if the
 	 * user's Facebook access token is not null or empty!
 	 *
 	 * @param context
 	 * The context of the Activity that is calling this method.
 	 *
 	 * @return
 	 * Returns the complete Facebook API access token query string, ready to be
 	 * used in a Facebook Graph API request.
 	 */
 	private static String getAccessToken(final Context context)
 	{
 		if (Utilities.verifyValidString(accessToken))
 		{
 			return ACCESS_TOKEN + accessToken;
 		}
 		else
 		{
 			final SharedPreferences sPreferences = Utilities.getPreferences(context);
 			accessToken = sPreferences.getString(KEY_ACCESS_TOKEN, null);
 
 			if (Utilities.verifyValidString(accessToken))
 			{
 				return ACCESS_TOKEN + accessToken;
 			}
 			else
 			{
 				return null;
 			}
 		}
 	}
 
 
 	/**
 	 * Sets the user's Facebook access token. This is needed as some Facebook
 	 * API requests require the user's access token in order to run without
 	 * worriment of hitting an API rate limit. More on the subject here:
 	 * https://developers.facebook.com/docs/reference/api/using-pictures/#ratelimits
 	 *
 	 * @param context
 	 * The context of the Activity that is calling this method.
 	 *
 	 * @param accessToken
 	 * The user's Facebook Access Token.
 	 */
 	public static void setAccessToken(final Context context, final String accessToken)
 	{
 		final SharedPreferences sPreferences = Utilities.getPreferences(context);
 		final SharedPreferences.Editor editor = sPreferences.edit();
 		editor.putString(KEY_ACCESS_TOKEN, accessToken);
 		editor.commit();
 
 		FacebookUtilities.accessToken = accessToken;
 	}
 
 
 	/**
 	 * Returns the large profile picture URL for the given user ID.
 	 *
 	 * @param context
 	 * The context of the Activity that is calling this method.
 	 *
 	 * @param id
 	 * The Facebook user ID of the person that you want a profile picture for.
 	 *
 	 * @return
 	 * Returns the URL as a String. This URL could be typed directly into a
 	 * browser if you wanted to test to make sure that it works.
 	 */
 	public static String getFriendsPictureLarge(final Context context, final long id)
 	{
 		if (Utilities.verifyValidString(getAccessToken(context)))
 		{
			return GRAPH_API_URL + id + PICTURE_LARGE + ACCESS_TOKEN + accessToken;
 		}
 		else
 		{
 			return GRAPH_API_URL + id + PICTURE_LARGE;
 		}
 	}
 
 
 	/**
 	 * Returns the normal profile picture URL for the given user ID.
 	 *
 	 * @param context
 	 * The context of the Activity that is calling this method.
 	 *
 	 * @param id
 	 * The Facebook user ID of the person that you want a profile picture for.
 	 *
 	 * @return
 	 * Returns the URL as a String. This URL could be typed directly into a
 	 * browser if you wanted to test to make sure that it works.
 	 */
 	public static String getFriendsPictureNormal(final Context context, final long id)
 	{
 		if (Utilities.verifyValidString(getAccessToken(context)))
 		{
			return GRAPH_API_URL + id + PICTURE_NORMAL + ACCESS_TOKEN + accessToken;
 		}
 		else
 		{
 			return GRAPH_API_URL + id + PICTURE_NORMAL;
 		}
 	}
 
 
 	/**
 	 * Returns the small profile picture URL for the given user ID.
 	 *
 	 * @param context
 	 * The context of the Activity that is calling this method.
 	 *
 	 * @param id
 	 * The Facebook user ID of the person that you want a profile picture for.
 	 *
 	 * @return
 	 * Returns the URL as a String. This URL could be typed directly into a
 	 * browser if you wanted to test to make sure that it works.
 	 */
 	public static String getFriendsPictureSmall(final Context context, final long id)
 	{
 		if (Utilities.verifyValidString(getAccessToken(context)))
 		{
			return GRAPH_API_URL + id + PICTURE_SMALL + ACCESS_TOKEN + accessToken;
 		}
 		else
 		{
 			return GRAPH_API_URL + id + PICTURE_SMALL;
 		}
 	}
 
 
 	/**
 	 * Returns the square profile picture URL for the given user ID.
 	 *
 	 * @param context
 	 * The context of the Activity that is calling this method.
 	 *
 	 * @param id
 	 * The Facebook user ID of the person that you want a profile picture for.
 	 *
 	 * @return
 	 * Returns the URL as a String. This URL could be typed directly into a
 	 * browser if you wanted to test to make sure that it works.
 	 */
 	public static String getFriendsPictureSquare(final Context context, final long id)
 	{
 		if (Utilities.verifyValidString(getAccessToken(context)))
 		{
			return GRAPH_API_URL + id + PICTURE_SQUARE + ACCESS_TOKEN + accessToken;
 		}
 		else
 		{
 			return GRAPH_API_URL + id + PICTURE_SQUARE;
 		}
 	}
 
 
 }
