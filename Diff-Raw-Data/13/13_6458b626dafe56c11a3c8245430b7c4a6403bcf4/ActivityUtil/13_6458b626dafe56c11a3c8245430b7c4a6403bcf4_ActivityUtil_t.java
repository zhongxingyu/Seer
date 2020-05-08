 package org.macno.puma.util;
 
 import static org.macno.puma.PumaApplication.APP_NAME;
 
 import java.text.ParseException;
 
 import org.json.JSONObject;
 import org.macno.puma.R;
 import org.macno.puma.provider.Pumpio;
 import org.macno.puma.view.RemoteImageView;
 
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.text.Html;
 import android.text.method.LinkMovementMethod;
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
 		JSONObject imgo = obj.optJSONObject("image");
		JSONObject pumpIo = imgo.optJSONObject("pump_io");
		if(pumpIo != null && pumpIo.has("proxyURL")) {
			// If there's a proxy I use it
			return pumpIo.optString("proxyURL");
		}
 		return imgo != null ? imgo.optString("url",null) : null;
 	}
 	public static String getObjectFullImage(JSONObject obj) {
 		JSONObject imgo = obj.optJSONObject("fullImage");
		JSONObject pumpIo = imgo.optJSONObject("pump_io");
		if(pumpIo != null && pumpIo.has("proxyURL")) {
			// If there's a proxy I use it
			return pumpIo.optString("proxyURL");
		}
 		return imgo != null ? imgo.optString("url",null) : null;
 	}
 	
 	public static LinearLayout getViewActivity(Pumpio pumpio, JSONObject act) {
 		return ActivityUtil.getViewActivity(pumpio,  act, true, false);
 	}
 	
 	public static LinearLayout getViewActivity(Pumpio pumpio, JSONObject act, boolean showCounterBar, boolean clickableLink) {
 		
 		LayoutInflater inflater = (LayoutInflater) pumpio.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		LinearLayout view = (LinearLayout)inflater.inflate(R.layout.activity_row, null);
 
 		String verb = act.optString("verb");
 		JSONObject obj = act.optJSONObject("object");
 		JSONObject actor = ActivityUtil.getActor(act);
 		String objectType = obj.optString("objectType");
 		TextView sender = (TextView)view.findViewById(R.id.tv_sender);
 		TextView note = (TextView)view.findViewById(R.id.note);
 		
 		Log.d(APP_NAME,"Verb: " + verb + " / what: " + objectType);
 		if("post".equals(verb)) {
 			String message="";
 			String what = "";
 			if(objectType.equals("note")) {
 				what = pumpio.getContext().getString(R.string.objecttype_note);
 			} else if(objectType.equals("comment")) {
 				what = pumpio.getContext().getString(R.string.objecttype_comment);
 			} else if(objectType.equals("image")) {
 				what = pumpio.getContext().getString(R.string.objecttype_image);
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
 				String imageURL = ActivityUtil.getObjectImage(obj);
 				if(imageURL != null) {
 					Log.e(APP_NAME,"Image URL: " + imageURL);
 					noteImage.setRemoteURI(pumpio.getHttpUtil(), imageURL);
 					noteImage.loadImage();
 					if(clickableLink) {
 						final String fullImageURL = ActivityUtil.getObjectFullImage(obj);
 						final Context context = pumpio.getContext();
 						if(fullImageURL != null && !"".equals(fullImageURL)) {
 						noteImage.setOnClickListener(new View.OnClickListener() {
 							
 							@Override
 							public void onClick(View v) {
 								Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fullImageURL));
 								context.startActivity(myIntent);
 							}
 						});
 						}
 					}
 				} else {
 					Log.e(APP_NAME,"Uhm an image activity without the image object");
 				}
 				
 				if(showCounterBar) {
 					ActivityUtil.showCounterBar(view, obj);
 				}
 			} else {
 				
 			}
 			message = pumpio.getContext().getString(R.string.msg_posted,ActivityUtil.getActorBestName(actor), what);
 			sender.setText(message);
 			
 			
 		} else if ("favorite".equals(verb)) {
 			String message="";
 			String what = "";
 			if(objectType.equals("note")) {
 				what = pumpio.getContext().getString(R.string.objecttype_note);
 			} else if(objectType.equals("comment")) {
 				what = pumpio.getContext().getString(R.string.objecttype_comment);
 			} else if(objectType.equals("image")) {
 				what = pumpio.getContext().getString(R.string.objecttype_image);
 			} else {
 				what = "something";
 			}
 			message = pumpio.getContext().getString(R.string.msg_favorited,ActivityUtil.getActorBestName(actor),  what);
 			sender.setText(message);
 			String content = ActivityUtil.getContent(obj);
 			if(content != null)
 				note.setText(Html.fromHtml(content));
 		} else if ("share".equals(verb)) {
 			String message="";
 			String what = "";
 			if(objectType.equals("note")) {
 				what = pumpio.getContext().getString(R.string.objecttype_note);
 			} else if(objectType.equals("comment")) {
 				what = pumpio.getContext().getString(R.string.objecttype_comment);
 			} else if(objectType.equals("image")) {
 				what = pumpio.getContext().getString(R.string.objecttype_image);
 			} else {
 				what = "something";
 			}
 			JSONObject originalActor = ActivityUtil.getActor(obj);
 			if(originalActor != null) {
 				message = pumpio.getContext().getString(R.string.msg_shareded_from,ActivityUtil.getActorBestName(actor), what, ActivityUtil.getActorBestName(originalActor));
 			} else {
 				message = pumpio.getContext().getString(R.string.msg_shareded,ActivityUtil.getActorBestName(actor), what);
 			}
 			sender.setText(message);
 			String content = ActivityUtil.getContent(obj);
 			if(content != null)
 				note.setText(Html.fromHtml(content));
 		} else if ("follow".equals(verb)) {
 			String message="";
 			
 			JSONObject originalActor = ActivityUtil.getActor(obj);
 			if(originalActor != null) {
 				message = pumpio.getContext().getString(R.string.msg_followed,ActivityUtil.getActorBestName(actor), ActivityUtil.getActorBestName(originalActor));
 			} else {
 				message = pumpio.getContext().getString(R.string.msg_followed,ActivityUtil.getActorBestName(actor), "you");
 			}
 			sender.setText(message);
 			String content = ActivityUtil.getContent(obj);
 			if(content != null)
 				note.setText(Html.fromHtml(content));
 		} else if ("stop-following".equals(verb)) {
 				String message="";
 				
 				JSONObject originalActor = ActivityUtil.getActor(obj);
 				if(originalActor != null) {
 					message = pumpio.getContext().getString(R.string.msg_stop_following,ActivityUtil.getActorBestName(actor), ActivityUtil.getActorBestName(originalActor));
 				} else {
 					message = pumpio.getContext().getString(R.string.msg_stop_following,ActivityUtil.getActorBestName(actor), "you");
 				}
 				sender.setText(message);
 				String content = ActivityUtil.getContent(obj);
 				if(content != null)
 					note.setText(Html.fromHtml(content));
 		} else {
 			String what = verb;
 
 			sender.setText(ActivityUtil.getActorBestName(actor) + " " + what);
 			String content = ActivityUtil.getContent(obj);
 			if(content != null)
 				note.setText(Html.fromHtml(content));
 		}
 
 		if(clickableLink) {
 			note.setMovementMethod(LinkMovementMethod.getInstance());
 		}
 		RemoteImageView rim = (RemoteImageView)view.findViewById(R.id.riv_sender);
 		String avatar = ActivityUtil.getImageUrl(actor);
 		if(avatar == null) {
 			avatar = "http://macno.org/images/unkown.png";
 		}
 		
 		rim.setRemoteURI(pumpio.getHttpUtil(), avatar);
 		rim.loadImage();
 		
 		TextView published = (TextView)view.findViewById(R.id.tv_published);
 		String s_published = ActivityUtil.getPublished(act);
 		try {
 			s_published = DateUtils.getRelativeDate(pumpio.getContext(), 
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
 	
 	public static LinearLayout getViewComment(Pumpio pumpio, LayoutInflater inflater, JSONObject item, boolean even) {
 		if(item == null) {
 			Log.d(APP_NAME,"getViewComment but item is null");
 			return null;
 		}
 		LinearLayout view = (LinearLayout)inflater.inflate(R.layout.comment_row, null);
 		
 		LinearLayout ll_comment = (LinearLayout)view.findViewById(R.id.ll_comment);
 		int color = even ? R.color.bg_comment_even : R.color.bg_comment_odd;
 		ll_comment.setBackgroundColor(pumpio.getContext().getResources().getColor(color) );
 		TextView tv_comment = (TextView)view.findViewById(R.id.comment);
 		
 		String content = item.optString("content");
 		if(content == null) {
 			return null;
 		}
 		tv_comment.setText(Html.fromHtml(content));
 		tv_comment.setMovementMethod(LinkMovementMethod.getInstance());
 		
 		JSONObject actor = item.optJSONObject("author");
 		if(actor != null ) {
 			RemoteImageView rim = (RemoteImageView)view.findViewById(R.id.riv_sender);
 			String avatar = ActivityUtil.getImageUrl(actor);
 			if(avatar == null) {
 				avatar = "http://macno.org/images/unkown.png";
 			}
 			rim.setRemoteURI(pumpio.getHttpUtil(), avatar);
 			rim.loadImage();
 			
 			TextView sender = (TextView)view.findViewById(R.id.tv_sender);
 			sender.setText(ActivityUtil.getActorBestName(actor));
 		}
 		
 		TextView published = (TextView)view.findViewById(R.id.tv_published);
 		String s_published = item.optString("published");
 		if(s_published != null) {
 //			Log.d(APP_NAME,"published <<< " + s_published);
 			try {
 				s_published = DateUtils.getRelativeDate(pumpio.getContext(), 
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
