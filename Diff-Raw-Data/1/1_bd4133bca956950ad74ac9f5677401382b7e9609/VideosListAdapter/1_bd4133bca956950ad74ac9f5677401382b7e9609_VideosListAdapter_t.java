 /**
  * 
  */
 package org.vimeoid.adapter.user;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.vimeoid.R;
 import org.vimeoid.adapter.JsonObjectsAdapter;
 import org.vimeoid.dto.advanced.Video;
 import org.vimeoid.util.Utils;
 
 import com.fedorvlasov.lazylist.ImageLoader;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 /**
  * <dl>
  * <dt>Project:</dt> <dd>vimeoid</dd>
  * <dt>Package:</dt> <dd>org.vimeoid.adapter.user</dd>
  * </dl>
  *
  * <code>VideosListAdapter</code>
  *
  * <p>Description</p>
  *
  * @author Ulric Wilfred <shaman.sir@gmail.com>
  * @date Sep 29, 2010 8:31:27 PM 
  *
  */
 public class VideosListAdapter extends JsonObjectsAdapter<Video> {
     
     private final LayoutInflater layoutInflater;
     private final ImageLoader imageLoader;    
     
     public VideosListAdapter(Context context, LayoutInflater inflater) {
         super(Video.FieldsKeys.MULTIPLE_KEY);
         
         this.layoutInflater = inflater;        
         
         this.imageLoader = new ImageLoader(context, R.drawable.thumb_loading_small, R.drawable.video_unknown_item);        
     }
     
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         VideoItemViewHolder itemHolder = null;
         
         final Video video = (Video)getItem(position); 
         
         if (convertView == null) {
             
             convertView = layoutInflater.inflate(R.layout.item_video, parent, false);
             itemHolder = new VideoItemViewHolder();
                 
             itemHolder.ivThumb = (ImageView) convertView.findViewById(R.id.videoItemImage);
             itemHolder.tvTitle = (TextView) convertView.findViewById(R.id.videoItemTitle);
             itemHolder.tvAuthor = (TextView) convertView.findViewById(R.id.videoItemAuthor);
             itemHolder.tvDuration = (TextView) convertView.findViewById(R.id.videoItemDuration);
             itemHolder.llTags = (LinearLayout) convertView.findViewById(R.id.videoItemTags);
             
             itemHolder.tvLikes = (TextView) convertView.findViewById(R.id.videoItemNumOfLikes);
             itemHolder.tvPlays = (TextView) convertView.findViewById(R.id.videoItemNumOfPlays);
             itemHolder.tvComments = (TextView) convertView.findViewById(R.id.videoItemNumOfComments);
                 
             convertView.setTag(itemHolder);
             
         } else {
             
             itemHolder = (VideoItemViewHolder) convertView.getTag();
             
         }
         
         imageLoader.displayImage(video.thumbnails.small.url, itemHolder.ivThumb);
         
         itemHolder.tvTitle.setText(video.title);
         itemHolder.tvAuthor.setText(video.uploaderName);
         itemHolder.tvDuration.setText(Utils.adaptDuration(video.duration));
         injectTags(video.tags, itemHolder.llTags, position);
         
         itemHolder.tvLikes.setText(String.valueOf(video.likesCount));
         itemHolder.tvPlays.setText(String.valueOf(video.playsCount));
         itemHolder.tvComments.setText(String.valueOf(video.commentsCount));
         
         if (video.isLike) convertView.setBackgroundResource(R.drawable.liked_bg_gradient);
         else if (video.isWatchLater) convertView.setBackgroundResource(R.drawable.watchlater_bg_gradient);
        else convertView.setBackgroundResource(0);
         
         return convertView;
     }
     
     protected void injectTags(final String[] tags, final ViewGroup group, final int curPosition) {
         group.removeAllViews();
         if (tags.length == 0) {
             group.addView(layoutInflater.inflate(R.layout.no_tags_for_item, null));
             return;
         }
         
         for (final String tag: tags) {
             final View tagStruct = layoutInflater.inflate(R.layout.tag_for_the_item, null);
             ((TextView)tagStruct.findViewById(R.id.tagItem)).setText(tag);
             group.addView(tagStruct);
         }
     }
     
     @Override
     protected void finalize() throws Throwable {
         super.finalize();
         imageLoader.clearCache();
     }
     
     private class VideoItemViewHolder {
         
         ImageView ivThumb;
         
         TextView tvTitle;
         TextView tvAuthor;
         TextView tvDuration;
         LinearLayout llTags;
         
         TextView tvLikes;
         TextView tvPlays;
         TextView tvComments;
     }    
 
     @Override
     protected Video[] extractItems(JSONObject jsonObject) throws JSONException {
         return Video.collectListFromJson(jsonObject);
     }
 
     public void updateLikes(Set<Long> videosIds) {
         final Set<Long> videosList = new HashSet<Long>();
         videosList.add(Long.valueOf(15166258));
         for (Video video: getItems()) {
             if (videosList.contains(video.getId())) video.isLike = true;
         }
     }
 
     public void updateWatchLaters(Set<Long> videosIds) {
         final Set<Long> videosList = new HashSet<Long>();
         videosList.add(Long.valueOf(14011251));
         for (Video video: getItems()) {
             if (videosList.contains(video.getId())) video.isWatchLater = true;
         }
     }
 
 }
