 package org.macno.puma.util;
 
 import static org.macno.puma.PumaApplication.APP_NAME;
 
 import java.text.ParseException;
 
 import org.json.JSONObject;
 import org.macno.puma.R;
 import org.macno.puma.view.RemoteImageView;
 
 import android.content.Context;
 import android.text.Html;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class ActivityUtil {
 
 	public static JSONObject getActor(JSONObject act) {
 		JSONObject ret = act.optJSONObject("actor");
 		return ret;
 	}
 	
 	public static String getActorBestName(JSONObject actor) {
 		if(actor != null) {
 			if(actor.has("displayName")) {
 				return actor.optString("displayName");
 			} else if(actor.has("preferredUsername")) {
 				return actor.optString("preferredUsername");
 			}
 		}
 		return null;
 	}
 	
 	public static String getImageUrl(JSONObject image) {
 		
 		if(image.has("image")) {
 			return image.optJSONObject("image").optString("url");
 		}
 		
 		return null;
 	}
 	
 	public static String getPublished(JSONObject act) {
 		return act.optString("published",null);
 	}
 	
 	public static String getContent(JSONObject act) {
 		return act.optString("content",null);
 	}
 	
 	public static String getObjectImage(JSONObject obj) {
		return obj.optJSONObject("image").optString("url");
 	}
 	
 	public static LinearLayout getViewActivity(Context mContext,JSONObject act) {
 		return ActivityUtil.getViewActivity(mContext, act, true);
 	}
 	
 	public static LinearLayout getViewActivity(Context mContext,JSONObject act, boolean showCounterBar) {
 		
 		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		LinearLayout view = (LinearLayout)inflater.inflate(R.layout.activity_row, null);
 
 		JSONObject obj = act.optJSONObject("object");
 		JSONObject actor = ActivityUtil.getActor(act);
 		String objectType = obj.optString("objectType");
 		TextView sender = (TextView)view.findViewById(R.id.tv_sender);
 		TextView note = (TextView)view.findViewById(R.id.note);
 		
 		if("post".equals(act.optString("verb"))) {
 			String message="";
 			String what = "";
 			if(objectType.equals("note")) {
 				what = mContext.getString(R.string.objecttype_note);
 			} else if(objectType.equals("comment")) {
 				what = mContext.getString(R.string.objecttype_comment);
 			} else if(objectType.equals("image")) {
 				what = mContext.getString(R.string.objecttype_image);
 			} else {
 				what = "something";
 			}
 			if("note".equals(objectType)) {
 				try {
 					String content = ActivityUtil.getContent(obj);
 					if(content != null)
 						note.setText(Html.fromHtml(content));
 					
 					if(showCounterBar) {
 						ActivityUtil.showCounterBar(view, obj);
 					}
 				} catch(Exception e) {
 					Log.e(APP_NAME,"Setting note html: " + e.getMessage(),e);
 					note.setText(ActivityUtil.getContent(obj));
 				}
 			} else if("comment".equals(objectType)) {
 				String content = ActivityUtil.getContent(obj);
 				if(content != null)
 					note.setText(Html.fromHtml(content));
 			} else if("image".equals(objectType)) {
 				String content = ActivityUtil.getContent(obj);
 				if(content != null)
 					note.setText(Html.fromHtml(content));
 				RemoteImageView noteImage = (RemoteImageView)view.findViewById(R.id.note_image);
 				noteImage.setVisibility(View.VISIBLE);
				noteImage.setRemoteURI(ActivityUtil.getObjectImage(obj));
				noteImage.loadImage();
 				
 				if(showCounterBar) {
 					ActivityUtil.showCounterBar(view, obj);
 				}
 			}
 			message = mContext.getString(R.string.msg_posted,ActivityUtil.getActorBestName(actor), what);
 			sender.setText(message);
 			
 			
 		} else if ("favorite".equals(act.optString("verb"))) {
 			String message="";
 			String what = "";
 			if(objectType.equals("note")) {
 				what = mContext.getString(R.string.objecttype_note);
 			} else if(objectType.equals("comment")) {
 				what = mContext.getString(R.string.objecttype_comment);
 			} else if(objectType.equals("image")) {
 				what = mContext.getString(R.string.objecttype_image);
 			} else {
 				what = "something";
 			}
 			message = mContext.getString(R.string.msg_favorited,ActivityUtil.getActorBestName(actor),  what);
 			sender.setText(message);
 			String content = ActivityUtil.getContent(obj);
 			if(content != null)
 				note.setText(Html.fromHtml(content));
 		} else if ("share".equals(act.optString("verb"))) {
 			String message="";
 			String what = "";
 			if(objectType.equals("note")) {
 				what = mContext.getString(R.string.objecttype_note);
 			} else if(objectType.equals("comment")) {
 				what = mContext.getString(R.string.objecttype_comment);
 			} else if(objectType.equals("image")) {
 				what = mContext.getString(R.string.objecttype_image);
 			} else {
 				what = "something";
 			}
 			JSONObject originalActor = ActivityUtil.getActor(obj);
 			if(originalActor != null) {
 				message = mContext.getString(R.string.msg_shareded_from,ActivityUtil.getActorBestName(actor), what, ActivityUtil.getActorBestName(originalActor));
 			} else {
 				message = mContext.getString(R.string.msg_shareded,ActivityUtil.getActorBestName(actor), what);
 			}
 			sender.setText(message);
 			String content = ActivityUtil.getContent(obj);
 			if(content != null)
 				note.setText(Html.fromHtml(content));
 		}
 
 		
 		RemoteImageView rim = (RemoteImageView)view.findViewById(R.id.riv_sender);
 		String avatar = ActivityUtil.getImageUrl(actor);
 		if(avatar == null) {
 			avatar = "http://macno.org/images/unkown.png";
 		}
 		rim.setRemoteURI(avatar);
 		rim.loadImage();
 		
 		TextView published = (TextView)view.findViewById(R.id.tv_published);
 		String s_published = ActivityUtil.getPublished(act);
 		try {
 			s_published = DateUtils.getRelativeDate(mContext, 
 					DateUtils.parseRFC3339Date(s_published)
 					);
 		} catch (ParseException e) {
 			Log.e(APP_NAME,e.getMessage(),e);
 		}
 		published.setText(s_published);
 		
 		return view;
 		
 	}
 	
 	public static void showCounterBar(LinearLayout view, JSONObject obj) {
 		LinearLayout ll_counter = (LinearLayout)view.findViewById(R.id.ll_counter);
 		TextView cnt_replies = (TextView)view.findViewById(R.id.cnt_replies);
 		TextView cnt_likes = (TextView)view.findViewById(R.id.cnt_likes);
 		TextView cnt_shares = (TextView)view.findViewById(R.id.cnt_shares);
 		ll_counter.setVisibility(View.VISIBLE);
 
 		JSONObject replies = obj.optJSONObject("replies");
 		if(replies != null)
 			cnt_replies.setText(replies.optString("totalItems"));
 		JSONObject likes = obj.optJSONObject("likes");
 		if(likes != null)
 			cnt_likes.setText(likes.optString("totalItems"));
 		JSONObject shares = obj.optJSONObject("shares");
 		if(shares != null)
 			cnt_shares.setText(shares.optString("totalItems"));
 	}
 	
 	public static LinearLayout getViewComment(Context context, LayoutInflater inflater, JSONObject item, boolean even) {
 		if(item == null) {
 			Log.d(APP_NAME,"getViewComment but item is null");
 			return null;
 		}
 		LinearLayout view = (LinearLayout)inflater.inflate(R.layout.comment_row, null);
 		
 		LinearLayout ll_comment = (LinearLayout)view.findViewById(R.id.ll_comment);
 		int color = even ? R.color.bg_comment_even : R.color.bg_comment_odd;
 		ll_comment.setBackgroundColor( context.getResources().getColor(color) );
 		TextView tv_comment = (TextView)view.findViewById(R.id.comment);
 		
 		String content = item.optString("content");
 		if(content == null) {
 			return null;
 		}
 		tv_comment.setText(Html.fromHtml(content));
 		
 		JSONObject actor = item.optJSONObject("author");
 		if(actor != null ) {
 			RemoteImageView rim = (RemoteImageView)view.findViewById(R.id.riv_sender);
 			String avatar = ActivityUtil.getImageUrl(actor);
 			if(avatar == null) {
 				avatar = "http://macno.org/images/unkown.png";
 			}
 			rim.setRemoteURI(avatar);
 			rim.loadImage();
 			
 			TextView sender = (TextView)view.findViewById(R.id.tv_sender);
 			sender.setText(ActivityUtil.getActorBestName(actor));
 		}
 		
 		TextView published = (TextView)view.findViewById(R.id.tv_published);
 		String s_published = item.optString("published");
 		if(s_published != null) {
 //			Log.d(APP_NAME,"published <<< " + s_published);
 			try {
 				s_published = DateUtils.getRelativeDate(context, 
 						DateUtils.parseRFC3339Date(s_published)
 						);
 //				Log.d(APP_NAME,"published >>> " + s_published);
 				published.setText(s_published);
 			} catch (ParseException e) {
 				Log.e(APP_NAME,e.getMessage(),e);
 			}
 			
 		}
 		
 		return view;
 	}
 			
 }
