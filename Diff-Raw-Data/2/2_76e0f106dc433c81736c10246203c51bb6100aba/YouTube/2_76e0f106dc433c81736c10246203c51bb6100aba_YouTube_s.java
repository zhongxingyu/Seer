 /*
  * Copyright (C) 2013 Matias Molinas.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package gdg.youtube;
  
 import org.apache.cordova.CallbackContext;
 import org.apache.cordova.CordovaPlugin;
 
 import org.json.JSONObject;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.app.Activity;
 import android.content.Intent;
 
 import com.google.android.youtube.player.YouTubeInitializationResult;
 import com.google.android.youtube.player.YouTubeStandalonePlayer;
 
 public class YouTube extends CordovaPlugin {
 
 	// API key instructions https://developers.google.com/youtube/android/player/register
 	public static final String YOUTUBE_API_KEY = "YOUR_API_KEY";
 
 	// Checks if the YouTube application installed on the user's device supports the open playlist intent.
 	// https://developers.google.com/youtube/android/player/reference/com/google/android/youtube/player/YouTubeIntents#canResolveOpenPlaylistIntent(android.content.Context)
     public static final String ACTION_CAN_RESOLVE_OPEN_PLAYLIST = "canResolveOpenPlaylist";
 	
 	// Checks if the YouTube application installed on the user's device supports the play playlist intent.
 	// https://developers.google.com/youtube/android/player/reference/com/google/android/youtube/player/YouTubeIntents#canResolvePlayPlaylistIntent(android.content.Context)
     public static final String ACTION_CAN_RESOLVE_PLAY_PLAYLIST = "canResolvePlayPlaylist";
 	
 	// Checks if the YouTube application installed on the user's device supports the play video intent.
 	// https://developers.google.com/youtube/android/player/reference/com/google/android/youtube/player/YouTubeIntents#canResolvePlayVideoIntent(android.content.Context)
     public static final String ACTION_CAN_RESOLVE_PLAY_VIDEO = "canResolvePlayVideo";
 	
 	// Checks if the YouTube application installed on the user's device supports the open search results intent.
 	// https://developers.google.com/youtube/android/player/reference/com/google/android/youtube/player/YouTubeIntents#canResolveSearchIntent(android.content.Context)
     public static final String ACTION_CAN_RESOLVE_SEARCH = "canResolveSearch";
 	
 	// Checks if the YouTube application installed on the user's device supports the upload video intent.
 	// https://developers.google.com/youtube/android/player/reference/com/google/android/youtube/player/YouTubeIntents#canResolveUploadIntent(android.content.Context)
     public static final String ACTION_CAN_RESOLVE_UPLOAD = "canResolveUpload";
 	
 	// Checks if the YouTube application installed on the user's device supports the open user intent.
 	// https://developers.google.com/youtube/android/player/reference/com/google/android/youtube/player/YouTubeIntents#canResolveUserIntent(android.content.Context)
     public static final String ACTION_CAN_RESOLVE_OPEN_PLAYLIST = "canResolveUser";
 	
 	// Creates an Intent that, when resolved, will open the given playlist in the YouTube application.
 	// https://developers.google.com/youtube/android/player/reference/com/google/android/youtube/player/YouTubeIntents#createOpenPlaylistIntent(android.content.Context, java.lang.String)
     public static final String ACTION_OPEN_PLAYLIST = "openPlaylist";
 	
 	// Creates an Intent that, when resolved, will start playing the given playlist in the YouTube application from its first video.
 	// https://developers.google.com/youtube/android/player/reference/com/google/android/youtube/player/YouTubeIntents#createPlayPlaylistIntent(android.content.Context, java.lang.String)
     public static final String ACTION_PLAY_PLAYLIST = "playPlaylist";
 	
 	// Creates an Intent that, when resolved, will start playing the video specified by videoId, within the YouTube application.
 	// https://developers.google.com/youtube/android/player/reference/com/google/android/youtube/player/YouTubeIntents#createPlayVideoIntent(android.content.Context, java.lang.String)
     public static final String ACTION_PLAY_VIDEO = "playVideo";
 	
 	// Creates an Intent that, when resolved, will start playing the video specified by videoId, within the YouTube application.
 	// https://developers.google.com/youtube/android/player/reference/com/google/android/youtube/player/YouTubeIntents#createPlayVideoIntentWithOptions(android.content.Context, java.lang.String, boolean, boolean)
     public static final String ACTION_PLAY_VIDEO = "playVideoWithOptions";
 	
 	// Creates an Intent that, when resolved, will open the search results for the given query in the YouTube application.
 	// https://developers.google.com/youtube/android/player/reference/com/google/android/youtube/player/YouTubeIntents#createSearchIntent(android.content.Context, java.lang.String)
     public static final String ACTION_SEARCH = "search";
 	
 	// Creates an Intent that, when resolved, will open the upload activity in the YouTube application for the video specified by the videoUri.
 	// https://developers.google.com/youtube/android/player/reference/com/google/android/youtube/player/YouTubeIntents#createUploadIntent(android.content.Context, android.net.Uri)
     public static final String ACTION_UPLOAD = "upload";
 	
 	// Creates an Intent that, when resolved, will open the user page for the given user ID in the YouTube application.
 	// https://developers.google.com/youtube/android/player/reference/com/google/android/youtube/player/YouTubeIntents#createUserIntent(android.content.Context, java.lang.String)
     public static final String ACTION_USER = "user";
 	
 	// Retrieves the version code of the YouTube application installed on the user's device.
 	// https://developers.google.com/youtube/android/player/reference/com/google/android/youtube/player/YouTubeIntents#getInstalledYouTubeVersionCode(android.content.Context)
     public static final String ACTION_GET_INSTALLED_YOUTUBE_VERSION_CODE = "getInstalledYouTubeVersionCode";
 	
 	// Retrieves the version code of the YouTube application installed on the user's device.
 	// https://developers.google.com/youtube/android/player/reference/com/google/android/youtube/player/YouTubeIntents#getInstalledYouTubeVersionName(android.content.Context)
     public static final String ACTION_GET_INSTALLED_YOUTUBE_VERSION_NAME = "getInstalledYouTubeVersionName";
 	
 	// Checks if the YouTube application is installed on the user's device.
 	// https://developers.google.com/youtube/android/player/reference/com/google/android/youtube/player/YouTubeIntents#isYouTubeInstalled(android.content.Context)
     public static final String ACTION_IS_YOUTUBE_INSTALLED = "isYouTubeInstalled";
     
     @Override
     public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
 		boolean success = false;
         try {
             if (ACTION_PLAY_VIDEO.equals(action)) { 
                 doPlayVideo(args);
                 callbackContext.success();
                 return true;
             }
             callbackContext.error("Invalid action");
             return false;
         } catch(Exception e) {
             System.err.println("Exception: " + e.getMessage());
             callbackContext.error(e.getMessage());
             return false;
         }
 		return success;
     }
 	
 	private void doPlayVideo(JSONArray args) {
 		JSONObject arg_object = args.getJSONObject(0);
 		String videoid = arg_object.getString("videoid");
		Intent youtubeIntent = YouTubeStandalonePlayer.createVideoIntent(this.cordova.getActivity(), "YOUR_API_KEY", videoid);
 		this.cordova.startActivityForResult(this, youtubeIntent, 0);
 	}
 }
