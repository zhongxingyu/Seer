 /**
  * 
  */
 package org.vimeoid.util;
 
 import org.vimeoid.R;
 import org.vimeoid.activity.Player;
 import org.vimeoid.activity.user.item.UserActivity;
import org.vimeoid.activity.user.list.VideosActivity;
 import org.vimeoid.connection.advanced.Methods;
 import org.vimeoid.connection.simple.VimeoProvider;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.util.Log;
 
 /**
  * <dl>
  * <dt>Project:</dt> <dd>vimeoid</dd>
  * <dt>Package:</dt> <dd>org.vimeoid.util</dd>
  * </dl>
  *
  * <code>Invoke</code>
  *
  * <p>Description</p>
  *
  * @author Ulric Wilfred <shaman.sir@gmail.com>
  * @date Sep 16, 2010 10:48:03 PM 
  *
  */
 public final class Invoke {
     
     public static final String TAG = "Invoke";
     
     public static class Extras {
         
         public static final String API_METHOD = "v_apimethod";
         public static final String API_PARAMS = "v_apiparams";
     
         public static final String SUBJ_ICON = "v_subjicon";
         public static final String SUBJ_TITLE = "v_subjtitle";
         public static final String RES_ICON = "v_resicon";        
         
         public static final String VIDEO_ID = "v_video_id";
         public static final String USERNAME = "v_username";
         public static final String USER_ID = "v_user_id";
         
     }    
 
     public static class Guest {
     
         // Video
         
         protected static Uri getUploaderPageUri(org.vimeoid.dto.simple.Video video) {
             return Uri.withAppendedPath(VimeoProvider.BASE_URI, "/user/" + getUploaderId(video) + "/info");
         }
         
         protected static String getUploaderId(org.vimeoid.dto.simple.Video video) {
             return Utils.authorIdFromProfileUrl(video.uploaderProfileUrl);
         }        
         
         protected static Uri getVideoPageUri(org.vimeoid.dto.simple.Video video) {
             return Uri.withAppendedPath(VimeoProvider.BASE_URI, "/video/" + video.id);
         }
         
         public static void playVideo(Activity parent, org.vimeoid.dto.simple.Video video) {
         	Log.d(TAG, "Trying to play video " + video.id);
         	parent.startActivity(new Intent(parent, Player.class)
         	                     .putExtra(Extras.VIDEO_ID, video.id));        	
         }
         
         public static void pickVideo(Activity parent, org.vimeoid.dto.simple.Video video) {
 	
             Log.d(TAG, "Video with id " + video.id + " requested");
             parent.setResult(Activity.RESULT_OK, new Intent().setData(getVideoPageUri(video))
                                                              .putExtra(Extras.SUBJ_TITLE, video.title 
                                                               + " (" + video.uploaderName + ")"));
         }
         
         public static void selectVideo(Activity parent, org.vimeoid.dto.simple.Video video) {
             Log.d(TAG, "Video with id " + video.id + " selected");      
             parent.startActivity(new Intent(Intent.ACTION_VIEW, getVideoPageUri(video))
                                     .putExtra(Extras.SUBJ_TITLE, video.title
                                      + " (" + video.uploaderName + ")"));            
         }
         
         public static void selectVideosBy(Activity parent, org.vimeoid.dto.simple.User user) {
             parent.startActivity(new Intent(Intent.ACTION_VIEW, 
                                     Uri.withAppendedPath(VimeoProvider.BASE_URI, "/user/" + user.id + "/videos"))
                                     .putExtra(Extras.SUBJ_TITLE, user.displayName)
                                     .putExtra(Extras.RES_ICON, R.drawable.video));
         }
         
         public static void selectVideosByUploader(Activity parent, org.vimeoid.dto.simple.Video video) {
             parent.startActivity(new Intent(Intent.ACTION_VIEW, 
                                     Uri.withAppendedPath(VimeoProvider.BASE_URI, "/user/" + getUploaderId(video) + "/videos"))
                                     .putExtra(Extras.SUBJ_TITLE, video.uploaderName)
                                     .putExtra(Extras.RES_ICON, R.drawable.video));
         }        
         
         public static void selectUploader(Activity parent, org.vimeoid.dto.simple.Video video) {
             parent.startActivity(new Intent(Intent.ACTION_VIEW, getUploaderPageUri(video))
                                     .putExtra(Extras.SUBJ_TITLE, video.uploaderName));
         }
         
         // User
 
 		public static void selectApperancesOf(Activity parent, org.vimeoid.dto.simple.User user) {
             parent.startActivity(new Intent(Intent.ACTION_VIEW, 
 					   				 Uri.withAppendedPath(VimeoProvider.BASE_URI, "/user/" + user.id + "/appears"))
 					   				 .putExtra(Extras.SUBJ_TITLE, user.displayName)
 					   				 .putExtra(Extras.RES_ICON, R.drawable.appearance));
 		}
 
 		public static void selectLikesOf(Activity parent, org.vimeoid.dto.simple.User user) {
             parent.startActivity(new Intent(Intent.ACTION_VIEW, 
 	                				 Uri.withAppendedPath(VimeoProvider.BASE_URI, "/user/" + user.id + "/likes"))
                     				 .putExtra(Extras.SUBJ_TITLE, user.displayName)
                     				 .putExtra(Extras.RES_ICON, R.drawable.like));
 		}
 
 		public static void selectSubsriptionsOf(Activity parent, org.vimeoid.dto.simple.User user) {
             parent.startActivity(new Intent(Intent.ACTION_VIEW, 
 					   				 Uri.withAppendedPath(VimeoProvider.BASE_URI, "/user/" + user.id + "/subscr"))
 					   				 .putExtra(Extras.SUBJ_TITLE, user.displayName)
 					   				 .putExtra(Extras.RES_ICON, R.drawable.subscribe));			
 		}
 		
         public static void selectAlbumsOf(Activity parent, org.vimeoid.dto.simple.User user) {
             parent.startActivity(new Intent(Intent.ACTION_VIEW, 
                                      Uri.withAppendedPath(VimeoProvider.BASE_URI, "/user/" + user.id + "/albums"))
                                      .putExtra(Extras.SUBJ_TITLE, user.displayName)
                                      .putExtra(Extras.RES_ICON, R.drawable.album));          
         }
         
         public static void selectChannelsOf(Activity parent, org.vimeoid.dto.simple.User user) {
             parent.startActivity(new Intent(Intent.ACTION_VIEW, 
                                      Uri.withAppendedPath(VimeoProvider.BASE_URI, "/user/" + user.id + "/channels"))
                                      .putExtra(Extras.SUBJ_TITLE, user.displayName)
                                      .putExtra(Extras.RES_ICON, R.drawable.channel));          
         }        
         
         // Channel
         
         public static void pickChannel(Activity parent, org.vimeoid.dto.simple.Channel channel) {
             parent.setResult(Activity.RESULT_OK, new Intent()
                                      .setData(Uri.withAppendedPath(VimeoProvider.BASE_URI, "/channel/" + channel.id + "/info"))
                                      .putExtra(Extras.SUBJ_TITLE, channel.name));
         }
         
         public static void selectChannel(Activity parent, org.vimeoid.dto.simple.Channel channel) {
             parent.startActivity(new Intent(Intent.ACTION_VIEW, 
                                      Uri.withAppendedPath(VimeoProvider.BASE_URI, "/channel/" + channel.id + "/info"))
                                      .putExtra(Extras.SUBJ_TITLE, channel.name));          
         }
         
         public static void selectCreator(Activity parent, org.vimeoid.dto.simple.Channel channel) {
             parent.startActivity(new Intent(Intent.ACTION_VIEW, 
                                      Uri.withAppendedPath(VimeoProvider.BASE_URI, "/user/" + channel.creatorId + "/info"))
                                      .putExtra(Extras.SUBJ_TITLE, channel.creatorDisplayName));
         }        
 
         public static void selectChannelContent(Activity parent, org.vimeoid.dto.simple.Channel channel) {
             parent.startActivity(new Intent(Intent.ACTION_VIEW, 
                                      Uri.withAppendedPath(VimeoProvider.BASE_URI, "/channel/" + channel.id + "/videos"))
                                      .putExtra(Extras.SUBJ_TITLE, channel.name)
                                      .putExtra(Extras.RES_ICON, R.drawable.video));            
         }
         
         public static void selectChannelContent(Activity parent, String channelName) {
             parent.startActivity(new Intent(Intent.ACTION_VIEW, 
                                      Uri.withAppendedPath(VimeoProvider.BASE_URI, "/channel/" + channelName + "/videos"))
                                      .putExtra(Extras.SUBJ_TITLE, channelName)
                                      .putExtra(Extras.RES_ICON, R.drawable.video));
         }
         
         // Album
         
         public static void pickAlbum(Activity parent, org.vimeoid.dto.simple.Album album) {
             parent.setResult(Activity.RESULT_OK, new Intent()
                                      .setData(Uri.withAppendedPath(VimeoProvider.BASE_URI, "/album/" + album.id + "/info"))
                                      .putExtra(Extras.SUBJ_TITLE, album.title));
         }
         
         public static void selectAlbum(Activity parent, org.vimeoid.dto.simple.Album album) {
             parent.startActivity(new Intent(Intent.ACTION_VIEW, 
                                      Uri.withAppendedPath(VimeoProvider.BASE_URI, "/album/" + album.id + "/info"))
                                      .putExtra(Extras.SUBJ_TITLE, album.title));          
         }
         
         public static void selectCreator(Activity parent, org.vimeoid.dto.simple.Album album) {
             parent.startActivity(new Intent(Intent.ACTION_VIEW, 
                                      Uri.withAppendedPath(VimeoProvider.BASE_URI, "/user/" + album.creatorId + "/info"))
                                      .putExtra(Extras.SUBJ_TITLE, album.creatorDisplayName));
         }        
 
         public static void selectAlbumContent(Activity parent, org.vimeoid.dto.simple.Album album) {
             parent.startActivity(new Intent(Intent.ACTION_VIEW, 
                                      Uri.withAppendedPath(VimeoProvider.BASE_URI, "/album/" + album.id + "/videos"))
                                      .putExtra(Extras.SUBJ_TITLE, album.title)
                                      .putExtra(Extras.RES_ICON, R.drawable.video));            
         }
         
     }
     
     public static class User_ {
         
         public static void authenticate(Activity parent, Uri authUri) {
             // TODO: ensure Uri matches http://vimeo.com/...
             parent.startActivity(new Intent(Intent.ACTION_VIEW, authUri));
         }
         
         public static void showUserPage(Activity parent, long userId, String username) {
             parent.startActivity(new Intent(parent, UserActivity.class)
                                      .putExtra(Extras.API_METHOD, Methods.people.getInfo)
                                      .putExtra(Extras.API_PARAMS, new ApiParams().add("user_id", String.valueOf(userId))
                                                                                  .toBundle())
                                      .putExtra(Extras.USER_ID, userId)
                                      //.putExtra(Extras.USERNAME, username)
                                      .putExtra(Extras.SUBJ_ICON, R.drawable.contact)
                                      .putExtra(Extras.SUBJ_TITLE, username)
                                      .putExtra(Extras.RES_ICON, R.drawable.info));
         }
 
         public static void showPersonalPage(Activity parent, long userId, String username) {
             showUserPage(parent, userId, username);
         }
 
         public static void selectVideosBy(Activity parent, org.vimeoid.dto.advanced.User user) {
            parent.startActivity(new Intent(parent, VideosActivity.class)
                                      .putExtra(Extras.API_METHOD, Methods.videos.getAll)
                                      .putExtra(Extras.API_PARAMS, new ApiParams().add("user_id", String.valueOf(user.id))
                                                                                  .add("full_response", "1")
                                                                                  .toBundle())                                     
                                      //.putExtra(Extras.USER_ID, user.id)
                                      //.putExtra(Extras.USERNAME, user.username)
                                      .putExtra(Extras.SUBJ_ICON, R.drawable.contact)
                                      .putExtra(Extras.SUBJ_TITLE, user.username)
                                      .putExtra(Extras.RES_ICON, R.drawable.video));
         }
 
         public static void selectAlbumsOf(Activity parent, org.vimeoid.dto.advanced.User user) {
             // TODO Auto-generated method stub
             
         }
 
         public static void selectChannelsOf(Activity parent, org.vimeoid.dto.advanced.User user) {
             // TODO Auto-generated method stub
             
         }
 
         public static void selectContactsOf(Activity parent, org.vimeoid.dto.advanced.User user) {
             // TODO Auto-generated method stub
             
         }
 
         public static void selectApperancesOf(Activity parent, org.vimeoid.dto.advanced.User user) {
             // TODO Auto-generated method stub
             
         }
 
         public static void selectLikesOf(Activity parent, org.vimeoid.dto.advanced.User user) {
             // TODO Auto-generated method stub
             
         }
 
         public static void selectSubsriptionsOf(Activity parent, org.vimeoid.dto.advanced.User user) {
             // TODO Auto-generated method stub
             
         }
         
     }
     
 }
