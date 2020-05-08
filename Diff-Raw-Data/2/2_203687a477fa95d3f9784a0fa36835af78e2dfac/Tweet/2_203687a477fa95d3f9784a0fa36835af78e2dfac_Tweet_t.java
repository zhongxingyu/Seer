 package de.geotweeter.timelineelements;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 
 import android.graphics.drawable.Drawable;
 import android.util.Pair;
 import android.view.View;
 import de.geotweeter.Constants;
 import de.geotweeter.R;
 import de.geotweeter.User;
 import de.geotweeter.Utils;
 import de.geotweeter.Utils.PictureService;
 import de.geotweeter.activities.TimelineActivity;
 import de.geotweeter.apiconn.ImglyApiAccess;
 import de.geotweeter.apiconn.ImgurApiAccess;
 import de.geotweeter.apiconn.InstagramApiAccess;
 import de.geotweeter.apiconn.LockerzApiAccess;
 import de.geotweeter.apiconn.MobytoApiAccess;
 import de.geotweeter.apiconn.PlixiApiAccess;
 import de.geotweeter.apiconn.TwitpicApiAccess;
 import de.geotweeter.apiconn.YfrogApiAccess;
 import de.geotweeter.apiconn.YoutubeApiAccess;
 
 public class Tweet extends TimelineElement {
 	private static final long serialVersionUID = -6610449879010917836L;
 	@SuppressWarnings("unused")
 	private static final String LOG = "Tweet";
 	public Coordinates coordinates;
 	public String text;
 	public String text_for_display = null;
 	public long id;
 	public User user;
 	public View view;
 	public String source;
 	public Entities entities;
 	public long in_reply_to_status_id;
 	public long in_reply_to_user_id;
 	private Place place;
 	
 	public long getID() {
 		return id;
 	}
 	
 	public String getTextForDisplay() {
 		if (text_for_display == null) {
 			text_for_display = new String(text);
 			if (entities != null) {
 				if (entities.urls != null) {
 					for (Url url : entities.urls) {
						text_for_display = text_for_display.replace(url.url, url.display_url);
 					}
 				}
 				if (entities.media != null) {
 					for (Media media : entities.media) {
 						text_for_display = text_for_display.replace(media.url, media.display_url);
 					}
 				}
 
 			}
 			text_for_display = text_for_display.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&");
 		}
 		return text_for_display;
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
 	
 	public Drawable getAvatarDrawable() { 
 		return user.avatar; 
 	}
 
 	public String getSourceText() {
 		return "via " + source; 
 	}
 	
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
 	public int getBackgroundDrawableID(boolean getDarkVersion) {
 		User current_user = TimelineActivity.current_account.getUser();
 		if (user.id == current_user.id) {
 			return getDarkVersion ? R.drawable.listelement_background_dark_my : R.drawable.listelement_background_light_my;
 		} else if(this.mentionsUser(current_user)) {
 			return getDarkVersion ? R.drawable.listelement_background_dark_mention : R.drawable.listelement_background_light_mention;
 		} else if(this.id > TimelineActivity.current_account.getMaxReadTweetID()) {
 			return getDarkVersion ? R.drawable.listelement_background_dark_unread : R.drawable.listelement_background_light_unread;
 		} else {
 			return getDarkVersion ? R.drawable.listelement_background_dark_normal : R.drawable.listelement_background_light_normal;
 		}
 	}
 
 	public boolean mentionsUser(User user) {
 		if (entities != null) {
 			for(int i = 0; i < entities.user_mentions.size(); i++ ) {
 				if (entities.user_mentions.get(i).id == user.id) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public String getSenderString() {
 		return user.getScreenName();
 	}
 	
 	@Override
 	public String getPlaceString() {
 		if (place == null) {
 			return null;
 		}
 		return place.getFullName();
 	}
 	
 	public List<Pair<URL, URL>> getMediaList() {
 		List<Pair<URL, URL>> result = new ArrayList<Pair<URL, URL>>();
 		for (Media media : entities.media) {
 			try {
 				Pair<URL, URL> urls = new Pair<URL, URL>(new URL(media.media_url + ":thumb"), new URL(media.media_url));
 				result.add(urls);
 			} catch (MalformedURLException e) {
 				continue;
 			}
 		}
 		for (Url url : entities.urls) {
 			PictureService hoster = Utils.getPictureService(url);
 			switch (hoster) {
 			case NONE: 
 				break;
 			case TWITPIC: 
 				try {
 					result.add(TwitpicApiAccess.getUrlPair(url));
 				} catch (MalformedURLException e) {
 					break;
 				}
 				break;
 			case YFROG:
 				try {
 					result.add(YfrogApiAccess.getUrlPair(url));
 				} catch (MalformedURLException e) {
 					break;
 				}
 				break;
 			case YOUTUBE:
 				try {
 					result.add(YoutubeApiAccess.getUrlPair(url));
 				} catch (MalformedURLException e) {
 					break;
 				}
 				break;
 			case IMGUR:
 				try {
 					result.add(ImgurApiAccess.getUrlPair(url));
 				} catch (MalformedURLException e) {
 					break;
 				}
 				break;
 			case IMGLY:
 				try {
 					result.add(ImglyApiAccess.getUrlPair(url));
 				} catch (MalformedURLException e) {
 					break;
 				}
 				break;
 			case INSTAGRAM:
 				try {
 					result.add(InstagramApiAccess.getUrlPair(url));
 				} catch (MalformedURLException e) {
 					break;
 				}
 				break;
 			case PLIXI:
 				try {
 					result.add(PlixiApiAccess.getUrlPair(url));
 				} catch (MalformedURLException e) {
 					break;
 				}
 				break;
 			case LOCKERZ:
 				try {
 					result.add(LockerzApiAccess.getUrlPair(url));
 				} catch (MalformedURLException e) {
 					break;
 				}
 				break;
 			case MOBYTO:
 				try {
 					result.add(MobytoApiAccess.getUrlPair(url));
 				} catch (MalformedURLException e) {
 					break;
 				}
 				break;
 			}
 		}
 		return result;
 	}
 	
 }
