 package de.fabianonline.geotweeter.timelineelements;
 
 import java.text.SimpleDateFormat;
 import java.util.regex.Matcher;
 
 import android.graphics.drawable.Drawable;
 import android.view.View;
 
 import com.alibaba.fastjson.JSONArray;
 import com.alibaba.fastjson.JSONObject;
 
 import de.fabianonline.geotweeter.Constants;
 import de.fabianonline.geotweeter.R;
 import de.fabianonline.geotweeter.User;
 import de.fabianonline.geotweeter.activities.TimelineActivity;
 
 public class Tweet extends TimelineElement{
 	private static final long serialVersionUID = -6610449879010917836L;
 	private static final String LOG = "Tweet";
 	public String text;
 	public String text_for_display = null;
 	public long id;
 	public User user;
 	public View view;
 	public String source;
 	public JSONObject entities;
 	
 	public long getID() {
 		return id;
 	}
 	
 	public String getTextForDisplay() {
 		if (text_for_display == null) {
 			if (entities == null) {
 				// TODO why?
 				text_for_display = new String(text);
 			} else {
 				StringBuilder temp_text = new StringBuilder();
 				JSONArray urls = entities.getJSONArray("urls");
 				int start_index = 0;
 				if (urls!=null) {
 					for (int i=0; i<urls.size(); i++) {
 						JSONObject url = urls.getJSONObject(i);
 						JSONArray indices = url.getJSONArray("indices");
 						temp_text.append(text.substring(start_index, indices.getIntValue(0)));
 						temp_text.append(url.getString("display_url"));
 						start_index = indices.getIntValue(1);
 					}
 					if(start_index < text.length()) {
						temp_text.append(text.substring(start_index, text.length()-1));
 					}
 					text_for_display = temp_text.toString();
 				} else {
 					text_for_display = new String(text);
 				}
 			}
 		}
 		return "<strong>" + user.getScreenName() + "</strong> " + text_for_display;
 	}
 	
 	public void setUser(User u) {
 		if(User.all_users.containsKey(u.id)) {
 			user = User.all_users.get(u.id);
 		} else {
 			User.all_users.put(u.id, u);
 			user = u;
 		}
 	}
 	
 	public String getAvatarSource() {
 		return user.getAvatarSource();
 	}
 	
 	public void setSource(String str) {
 		Matcher m = Constants.REGEXP_FIND_SOURCE.matcher(str);
 		if (m.find()) {
 			source = m.group(1);
 		} else {
 			source = "web";
 		}
 	}
 	
 	public Drawable getAvatarDrawable() { return user.avatar; }
 
 	public CharSequence getSourceText() { return new SimpleDateFormat("dd.MM. HH:mm").format(created_at) + " from " + source; }
 	public String getSenderScreenName() {
 		return user.getScreenName();
 	}
 	
 	@Override
 	public boolean isReplyable() {
 		return true;
 	}
 	
 	@Override
 	public boolean showNotification() {
 		return true;
 	}
 	
 	@Override
 	public String getNotificationText(String type) {
 		if (type.equals("mention")) {
 			return "Mention von " + user.screen_name + ": " + text;
 		} else if (type.equals("retweet")) {
 			return user.screen_name + " retweetete: " + text;
 		}
 		return "";
 	}
 	
 	@Override
 	public String getNotificationContentTitle(String type) {
 		if (type.equals("mention")) {
 			return "Mention von " + user.screen_name;
 		} else if(type.equals("retweet")) {
 			return "Retweet von " + user.screen_name;
 		}
 		return "";
 	}
 	
 	@Override
 	public String getNotificationContentText(String type) {
 		return text;
 	}
 	
 	@Override
 	public int getBackgroundDrawableID() {
 		User current_user = TimelineActivity.current_account.getUser();
 		if (user.id == current_user.id) {
 			return R.drawable.listelement_background_my;
 		} else if(this.mentionsUser(current_user)) {
 			return R.drawable.listelement_background_mention;
 		} else if(this.id > TimelineActivity.current_account.getMaxReadTweetID()) {
 			return R.drawable.listelement_background_unread;
 		} else {
 			return R.drawable.listelement_background_normal;
 		}
 	}
 
 	public boolean mentionsUser(User user) {
 		if (entities!=null) {
 			JSONArray mentions = entities.getJSONArray("user_mentions");
 			for(int i=0; i<mentions.size(); i++) {
 				if (mentions.getJSONObject(i).getLong("id").equals(user.id)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 }
