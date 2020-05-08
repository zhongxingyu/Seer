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
 import org.vimeoid.dto.advanced.User;
 import org.vimeoid.dto.advanced.SubscriptionData.SubscriptionType;
 import org.vimeoid.util.Utils;
 
 import com.fedorvlasov.lazylist.ImageLoader;
 
 import android.content.Context;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
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
 public class UsersListAdapter extends JsonObjectsAdapter<User> implements UsersDataReceiver {
 	
 	public static final String TAG = "UsersListAdapter"; 
     
     private final LayoutInflater layoutInflater;
     private final ImageLoader imageLoader;
     private final UsersDataProvider provider;
     
     private Set<Long> requests = new HashSet<Long>();
     
     public UsersListAdapter(String dataKey, Context context, LayoutInflater inflater, UsersDataProvider provider) {
         super(dataKey);
         
         this.layoutInflater = inflater;        
         this.imageLoader = new ImageLoader(context, R.drawable.thumb_loading_square_small, R.drawable.unknown_status);
         this.provider = provider;
     }
     
     @Override
     public View getView(final int position, View convertView, ViewGroup parent) {
         UserItemViewHolder itemHolder = null;
         
         final User user = (User)getItem(position);
         
         if (convertView == null) {
             
             convertView = layoutInflater.inflate(R.layout.item_user, parent, false);
             itemHolder = new UserItemViewHolder();
                 
             itemHolder.ivPortrait = (ImageView) convertView.findViewById(R.id.userItemPortrait);
             itemHolder.tvName = (TextView) convertView.findViewById(R.id.userItemDisplayName);
             itemHolder.tvLocation = (TextView) convertView.findViewById(R.id.userItemLocation);
             itemHolder.llTags = (LinearLayout) convertView.findViewById(R.id.userItemTags);
             
             itemHolder.tvVideos = (TextView) convertView.findViewById(R.id.userItemNumOfVideos);
             itemHolder.tvAlbums = (TextView) convertView.findViewById(R.id.userItemNumOfAlbums);
             itemHolder.tvChannels = (TextView) convertView.findViewById(R.id.userItemNumOfChannels);
             itemHolder.tvContacts = (TextView) convertView.findViewById(R.id.userItemNumOfContacts);
             
             itemHolder.vgMarkers = (ViewGroup) convertView.findViewById(R.id.markersArea);
                 
             convertView.setTag(itemHolder);
             
         } else {
             
             itemHolder = (UserItemViewHolder) convertView.getTag();
             
         }
         
         Log.d(TAG, "(" + position + ") portrait for " + user.displayName + " " + user.portraits.small.url);
         imageLoader.displayImage(user.portraits.small.url, itemHolder.ivPortrait);
               
         itemHolder.tvName.setText(user.displayName + " : " + user.username);
         itemHolder.tvLocation.setText((user.location != null) ? user.location : "");
         injectInfo(itemHolder.llTags, position, user.fromStaff, user.isPlusMember, user.isMutual);
         
         itemHolder.tvVideos.setText((user.uploadsCount >= 0) ? String.valueOf(user.uploadsCount) : "-");
         itemHolder.tvAlbums.setText((user.albumsCount >= 0) ? String.valueOf(user.albumsCount) : "-");
         itemHolder.tvChannels.setText((user.channelsCount >= 0) ? String.valueOf(user.channelsCount) : "-");
         itemHolder.tvContacts.setText((user.contactsCount >= 0) ? String.valueOf(user.contactsCount) : "-");
         
         MarkersSupport.injectMarkers(layoutInflater, itemHolder.vgMarkers, getRequiredMarkers(user));
         
         //Log.d(TAG, "getView: checking is requested for " + user.id + " / " + user.displayName);
         if (!requests.contains(user.id)) {
             Log.d(TAG, "getView: yep, I haven't requested for user " + user.displayName);
             // location, videos count, albums count, channels count, contacts count, subscriptions status      
             provider.requestData(position, user.id, this);
             requests.add(user.id);
         }
                 
        Log.d(TAG, "returning convertView for " + position + " / " + user.displayName);
         return convertView;
     }
     
     protected int[] getRequiredMarkers(User user) {
     	final Set<Integer> markers = new HashSet<Integer>();
     	if ((user.isContact != null) && user.isContact) markers.add(R.drawable.contact_marker);
     	if (user.subscriptonsStatus != null) {
     		if (user.subscriptonsStatus.contains(SubscriptionType.LIKES)) markers.add(R.drawable.like_marker);
     		if (user.subscriptonsStatus.contains(SubscriptionType.UPLOADS)) markers.add(R.drawable.upload_marker);
     		if (user.subscriptonsStatus.contains(SubscriptionType.APPEARS)) markers.add(R.drawable.appear_marker);
     	}
     	final int[] result = new int[markers.size()];
     	int i = 0; for (Integer value: markers) { result[i] = value; i++; } 
         return result;
     }
     
     protected void injectInfo(final ViewGroup holder, final int curPosition, final Boolean isStaff,
     		                                                                 final Boolean isPlus,
     		                                                                 final Boolean isMutual) {
         holder.removeAllViews();
         
         int infoCount = 0;
         if ((isStaff != null) && isStaff) {
         	final View tagStruct = layoutInflater.inflate(R.layout.tag_for_the_item, null);
             ((TextView)tagStruct.findViewById(R.id.tagItem)).setText(R.string.staff_caps);
             ((TextView)tagStruct.findViewById(R.id.tagItem)).setBackgroundResource(R.drawable.small_orange_tag_shape);
             holder.addView(tagStruct);
             infoCount++;
         }
         if ((isPlus != null) && isPlus) {
         	final View tagStruct = layoutInflater.inflate(R.layout.tag_for_the_item, null);
             ((TextView)tagStruct.findViewById(R.id.tagItem)).setText(R.string.plus_caps);
             ((TextView)tagStruct.findViewById(R.id.tagItem)).setBackgroundResource(R.drawable.small_blue_tag_shape);
             holder.addView(tagStruct);
             infoCount++;
         }
         if ((isMutual != null) && isMutual) {
         	final View tagStruct = layoutInflater.inflate(R.layout.tag_for_the_item, null);
             ((TextView)tagStruct.findViewById(R.id.tagItem)).setText(R.string.mutual_caps);
             ((TextView)tagStruct.findViewById(R.id.tagItem)).setBackgroundResource(R.drawable.small_red_tag_shape);
             holder.addView(tagStruct);
             infoCount++;
         }        
         
         if (infoCount == 0) {
             holder.addView(layoutInflater.inflate(R.layout.no_tags_for_item, null));
         }                
     }
     
     @Override
     protected void finalize() throws Throwable {
         super.finalize();
         imageLoader.clearCache();
     }
     
     @Override
     protected User[] extractItems(JSONObject jsonObject) throws JSONException {
         return User.collectListFromJson(jsonObject);
     }
 
     /*
     public void updateLikes(ListView holder, Set<Long> videosIds) {
         final Set<Long> videosList = new HashSet<Long>();
         videosList.add(Long.valueOf(15166258));
         for (Video video: getItems()) {
             video.isLike = videosList.contains(video.getId());
         }
         holder.invalidateViews();        
     }
 
     public void updateWatchLaters(ListView holder, Set<Long> videosIds) {
         final Set<Long> videosList = new HashSet<Long>();
         videosList.add(Long.valueOf(14011251));
         for (Video video: getItems()) {
             video.isWatchLater = videosList.contains(video.getId());
         }
         holder.invalidateViews();
     }
     
     public Video switchWatchLater(AdapterView<?> holder, int position) {
         final Video subject = (Video)getItem(position);
         subject.isWatchLater = !subject.isWatchLater;
         holder.getChildAt(position).invalidate();
         return subject;
     } */
     
     @Override
     public void gotPersonalInfo(AdapterView<?> holder, int position, String location, 
                                 long uploadsCount, long contactsCount) {
         final User user = (User)getItem(position);
         user.location = location;
         user.uploadsCount = uploadsCount;
         user.contactsCount = contactsCount;
         Log.d(TAG, "gotPersonalInfo: user " + user.id + " / " + user.displayName + " " +
                    user.location + " " + user.uploadsCount + " " + user.contactsCount + 
                    " child at position " + position);
         final View itemView = Utils.getItemViewIfVisible(holder, position);
         if (itemView != null) {
             ((TextView)itemView.findViewById(R.id.userItemLocation)).setText((user.location != null) ? user.location : "");
             ((TextView)itemView.findViewById(R.id.userItemNumOfVideos)).setText((user.uploadsCount >= 0) ? String.valueOf(user.uploadsCount) : "-");
             ((TextView)itemView.findViewById(R.id.userItemNumOfContacts)).setText((user.contactsCount >= 0) ? String.valueOf(user.contactsCount) : "-");
         }
     }
     
 
     @Override
     public void gotAlbumsCount(AdapterView<?> holder, int position, long albumsCount) {
         final User user = (User)getItem(position);
         user.albumsCount = albumsCount;
         Log.d(TAG, "gotAlbumsCount: user " + user.id + " / " + user.displayName);
         final View itemView = Utils.getItemViewIfVisible(holder, position);
         if (itemView != null) {
             ((TextView)itemView.findViewById(R.id.userItemNumOfAlbums)).setText((user.albumsCount >= 0) ? String.valueOf(user.albumsCount) : "-");
         }
     }
 
     @Override
     public void gotChannelsCount(AdapterView<?> holder, int position, long channelsCount) {
         final User user = (User)getItem(position);
         user.channelsCount = channelsCount;
         Log.d(TAG, "gotChannelsCount: user " + user.id + " / " + user.displayName);
         final View itemView = Utils.getItemViewIfVisible(holder, position);
         if (itemView != null) {
             ((TextView)itemView.findViewById(R.id.userItemNumOfChannels)).setText((user.channelsCount >= 0) ? String.valueOf(user.channelsCount) : "-");
         }
     }
     
     @Override
     public void gotSubsrcriptions(AdapterView<?> holder, int position, Set<SubscriptionType> types) {
         // TODO Auto-generated method stub
         
     }    
     
     private class UserItemViewHolder {
         
         ImageView ivPortrait;
         
         TextView tvName;
         TextView tvLocation;
         LinearLayout llTags;
         
         TextView tvVideos;
         TextView tvAlbums;
         TextView tvChannels;
         TextView tvContacts;
         
         ViewGroup vgMarkers;
     }
 
 
 }
