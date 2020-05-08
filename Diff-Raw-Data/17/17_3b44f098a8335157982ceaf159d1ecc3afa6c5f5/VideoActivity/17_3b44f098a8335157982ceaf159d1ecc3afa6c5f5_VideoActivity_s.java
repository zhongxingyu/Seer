 /**
  * 
  */
 package org.vimeoid.activity.user.item;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import org.vimeoid.R;
 import org.vimeoid.activity.user.SingleItemActivity;
 import org.vimeoid.adapter.LActionItem;
 import org.vimeoid.adapter.SectionedActionsAdapter;
 import org.vimeoid.dto.advanced.Video;
 import org.vimeoid.util.Invoke;
 import org.vimeoid.util.PlayerWebView;
 import org.vimeoid.util.Utils;
 
 import android.os.Bundle;
 import android.text.Html;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebView;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 /**
  * <dl>
  * <dt>Project:</dt> <dd>vimeoid</dd>
  * <dt>Package:</dt> <dd>org.vimeoid.activity.user.item</dd>
  * </dl>
  *
  * <code>UserActivity</code>
  *
  * <p>Description</p>
  *
  * @author Ulric Wilfred <shaman.sir@gmail.com>
  * @date Sep 24, 2010 11:05:50 PM 
  *
  */
 public class VideoActivity extends SingleItemActivity<Video> {
     
     public static final String TAG = "VideoActivity";
     
     private final OnClickListener clickDisabler;
     
     public VideoActivity() {
         super(R.layout.view_single_video);
         setLoadManually(true);
         
         this.clickDisabler = new OnClickListener() {
             @Override public void onClick(View v) { Log.d(TAG, "clicking is disabled"); }
         };
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         
         final long videoId = Long.valueOf(getIntent().getExtras().getLong(Invoke.Extras.VIDEO_ID));
         
         final WebView playerView = 
             PlayerWebView.projectPlayer(videoId, this);
         playerView.setOnClickListener(clickDisabler);
          
         findViewById(R.id.playOverlay).setOnClickListener(clickDisabler);
         
         queryItem();
     }
 
     @Override
     protected Video extractFromJson(JSONObject jsonObj) throws JSONException {
         return Video.collectFromJson(jsonObj);
     }
 
     @Override
     protected SectionedActionsAdapter fillWithActions(SectionedActionsAdapter actionsAdapter, final Video video) {
         
         // TODO: video tags
         // TODO: video likers
         // TODO: video comments
         
         // TODO: add "watch later" and "like" if it is not owner video
         // Statistics section
         int statsSection = actionsAdapter.addSection(getString(R.string.statistics));
         // tags
         if (video.tags.length > 0) actionsAdapter.addAction(statsSection, R.drawable.tag, 
                                  Utils.format(getString(R.string.tags_are), "list",
                                              Utils.adaptTags(video.tags, getString(R.string.none_of_tags))));
         // number of plays
         actionsAdapter.addAction(statsSection, R.drawable.play, 
                                  Utils.format(getString(R.string.num_of_plays), "num", String.valueOf(video.playsCount)));
         // number of likes
         actionsAdapter.addAction(statsSection, R.drawable.like, 
                                  Utils.format(getString(R.string.num_of_likes), "num", String.valueOf(video.likesCount)));        
         // number of comments
         actionsAdapter.addAction(statsSection, R.drawable.comment_video, 
                                  Utils.format(getString(R.string.num_of_comments), "num", String.valueOf(video.commentsCount)));
         
         
         // Information section
         int infoSection = actionsAdapter.addSection(getString(R.string.information));
         // duration
         actionsAdapter.addAction(infoSection, R.drawable.duration,
                                  Utils.format(getString(R.string.duration_is), "time", Utils.adaptDuration(video.duration)));       
         // uploader
         final LActionItem userAction = actionsAdapter.addAction(infoSection, R.drawable.contact, 
                                  Utils.format(getString(R.string.uploaded_by), "name", video.uploaderName));
         userAction.onClick =  new OnClickListener() {
             @Override public void onClick(View v) { Invoke.User_.selectUploader(VideoActivity.this, video); };
         };
         // uploaded on
         actionsAdapter.addAction(infoSection, R.drawable.upload,
                                  Utils.format(getString(R.string.uploaded_on), "time", video.uploadedOn));
         // dimensions
         actionsAdapter.addAction(infoSection, R.drawable.dimensions,
                                  Utils.format(getString(R.string.dimensions_are), "width", String.valueOf(video.width),
                                                                                   "height", String.valueOf(video.height)));
         
         return actionsAdapter;
     }
     
     @Override
     protected void onItemReceived(final Video video) {
         
         // enabling overlay
         findViewById(R.id.playOverlay).setOnClickListener(new OnClickListener() {
             @Override public void onClick(View v) {
                 Invoke.User_.playVideo(VideoActivity.this, video);
             }
         });
         
         
         // description
         ((TextView)findViewById(R.id.videoDescription)).setText((video.description.length() > 0) 
                                                                  ? Html.fromHtml(video.description)
                                                                  : getString(R.string.no_description_supplied));        
         
         // uploader portrait
         final ImageView uploaderPortrait = (ImageView)findViewById(R.id.uploaderPortrait);
         uploaderPortrait.setOnClickListener(new OnClickListener() {
             @Override public void onClick(View v) { Invoke.User_.selectUploader(VideoActivity.this, video); };
         });
         imageLoader.displayImage(video.uploaderPortraits.medium.url, uploaderPortrait);
         
         super.onItemReceived(video);
     }
     
     /* @Override
     public void onSecondaryTaskPerfomed(int taskId, JSONObject result) throws JSONException {
         switch (taskId) {
             case LOAD_PORTRAITS_TASK: {
                 final ImageView uploaderPortrait = (ImageView)findViewById(R.id.userPortrait);
                 final String mediumPortraitUrl = result.getJSONObject(PortraitsData.FieldsKeys.MULTIPLE_KEY)
                                                        .getJSONArray(PortraitsData.FieldsKeys.SINGLE_KEY)
                                                        .getJSONObject(2).getString(PortraitsData.FieldsKeys.URL);
                 imageLoader.displayImage(mediumPortraitUrl, uploaderPortrait);
             }; break;
             case LOAD_ALBUMS_TASK: {
                 final int albumsCount = result.getJSONObject("albums").getInt("total");                
                 Log.d(TAG, "got albums count, its " + albumsCount);
                 albumAction.title = Utils.format(getString(R.string.num_of_albums), "num", String.valueOf(albumsCount));
                 if (albumsCount == 0) albumAction.onClick = null;
                 getActionsList().invalidateViews();
             }; break;
             case LOAD_CHANNELS_TASK: {
                 final int channelsCount = result.getJSONObject("channels").getInt("total");                
                 Log.d(TAG, "got channels count, its " + channelsCount);
                 channelAction.title = Utils.format(getString(R.string.num_of_channels), "num", String.valueOf(channelsCount));
                 if (channelsCount == 0) channelAction.onClick = null;
                 getActionsList().invalidateViews();
             }; break;            
         }
     } */
 
 }
