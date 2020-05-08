 /**
  * Copyright 2013 Ognyan Bankov
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.yairkukielka.feedhungry;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.text.Html;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.android.volley.Request.Method;
 import com.android.volley.RequestQueue;
 import com.android.volley.Response;
 import com.android.volley.VolleyError;
 import com.android.volley.toolbox.ImageLoader;
 import com.android.volley.toolbox.NetworkImageView;
 import com.yairkukielka.feedhungry.AppData.MARK_ACTION;
 import com.yairkukielka.feedhungry.app.MyVolley;
 import com.yairkukielka.feedhungry.feedly.ListEntry;
 import com.yairkukielka.feedhungry.network.JsonCustomRequest;
 import com.yairkukielka.feedhungry.settings.PreferencesActivity;
 import com.yairkukielka.feedhungry.toolbox.DateUtils;
 import com.yairkukielka.feedhungry.toolbox.NetworkUtils;
 
 public class ListViewEntryArrayAdapter extends ArrayAdapter<ListEntry> {
 	private static final String TAG = ListViewEntryArrayAdapter.class.getSimpleName();
 	private static final String ACTION = "action";
 	private static final String TYPE = "type";
 	private static final String ENTRIES = "entries";
 	private static final String ENTRIES_IDS = "entryIds";
 	private static final String HTML_OPEN_MARK = "<";
 	private static final String MARK_AS_READ = "markAsRead";
 	private static final String EMPTY_STRING = "";
 	private static final String MARK_AS_UNREAD = "keepUnread";
 	private ImageLoader mImageLoader;
 	private boolean cards;
 	private Activity context;
 	Animation animation;
 	LayoutInflater layoutInflater;
 	int imageHeight, imageWidth;
 
 
 	public ListViewEntryArrayAdapter(Activity context, int textViewResourceId, List<ListEntry> objects,
 			ImageLoader imageLoader) {
 		super(context, textViewResourceId, objects);
 		mImageLoader = imageLoader;
 		this.context = context;
 		animation = AnimationUtils.loadAnimation(context, R.anim.wave_scale);
 		this.cards = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
 				PreferencesActivity.KEY_LIST_WITH_CARDS, false);
 		layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		imageHeight = getContext().getResources().getDimensionPixelSize(R.dimen.item_image_height);
 		imageWidth = getContext().getResources().getDimensionPixelSize(R.dimen.item_image_width);
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View v = convertView;
 		boolean makeAnimation = false;
 		if (v == null) {			
 			makeAnimation = true;
 			int numColumns = getContext().getResources().getInteger(R.integer.num_columns);
 			if (numColumns == 1) {
 				if (cards) {
 					// cards style
 					v = layoutInflater.inflate(R.layout.grid_item_layout, null);
 				} else {
 					v = layoutInflater.inflate(R.layout.list_item_layout, null);
 				}
 			} else {
 				if (cards) {
 					// width > 500dp
 					v = layoutInflater.inflate(R.layout.grid_item_layout, null);
 				} else {
 					v = layoutInflater.inflate(R.layout.list_item_layout, null);
 				}
 			}
 		}
 
 		ViewHolder holder = (ViewHolder) v.getTag(R.id.id_holder);
 
 		if (holder == null) {
 			holder = new ViewHolder(v);
 			holder.image.setDefaultImageResId(R.drawable.placeholder);
 			v.setTag(R.id.id_holder, holder);
 		}
 
 		ListEntry entry = getItem(position);
 		if (entry.getVisual() != null) {
 			// there is an image
 			holder.image.setImageUrl(entry.getVisual(), mImageLoader);
 		} else {
 			// no image found
 //			holder.image.setImageResource(R.drawable.black_pixel);
 			holder.image.setImageDrawable(null);
 		}
 
 		String summary = getSummaryWithoutHTML(entry.getContent());
 		// Spanned summary = Html.fromHtml(entry.getContent());
 		// SpannableStringBuilder spanstr = new
 		// SpannableStringBuilder(entry.getTitle());
 		// spanstr.setSpan(new StyleSpan(Typeface.BOLD),0,
 		// entry.getTitle().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 		// spanstr.append(". ");
 		// spanstr.append(summary);
 		// holder.title.setText(spanstr);
 
 		if (entry.isPopular()) {
 			holder.popular.setVisibility(View.VISIBLE);
 		} else {
 			holder.popular.setVisibility(View.INVISIBLE);
 		}
 
 		holder.title.setText(Html.fromHtml(entry.getTitle()));
 		if (EMPTY_STRING.equals(summary)) {
 			holder.summary.setVisibility(View.GONE);
 		} else {
 			holder.summary.setText(summary);
 		}
 		holder.date.setText(DateUtils.dateToString(entry.getPublished()));
 
 		holder.checkRead.setOnCheckedChangeListener(null);
 		if (!entry.isUnread()) {
 			holder.checkRead.setChecked(true);
 		} else {
 			holder.checkRead.setChecked(false);
 		}
 		holder.checkRead.setOnCheckedChangeListener(new MyCheckReadListener(entry));
 		holder.streamName.setText(entry.getOriginTitle());
 
 		holder.saved.setOnClickListener(null);
 		if (entry.isSaved()) {
 			holder.saved.setImageResource(R.drawable.star_on);
 			holder.saved.setTag(R.drawable.star_on);
 		} else {
 			holder.saved.setImageResource(R.drawable.star_off);
 			holder.saved.setTag(R.drawable.star_off);
 		}
 		holder.saved.setOnClickListener(new MySaveListener(entry));
 		if (v != null && makeAnimation) {
 			animation.setDuration(400);
 			v.startAnimation(animation);
 		}
 		return v;
 	}
 
 	private class ViewHolder {
 		NetworkImageView image;
 		TextView title;
 		TextView summary;
 		TextView date;
 		TextView popular;
 		TextView streamName;
 		CheckBox checkRead;
 		ImageView saved;
 
 		public ViewHolder(View v) {
 			image = (NetworkImageView) v.findViewById(R.id.image_list_thumb);
 			title = (TextView) v.findViewById(R.id.tv_list_title);
 			summary = (TextView) v.findViewById(R.id.tv_list_summary);
 			date = (TextView) v.findViewById(R.id.tv_list_date);
 			popular = (TextView) v.findViewById(R.id.tv_list_popular);
 			streamName = (TextView) v.findViewById(R.id.tv_list_stream_name);
 			checkRead = (CheckBox) v.findViewById(R.id.check_list_read);
 			saved = (ImageView) v.findViewById(R.id.image_list_saved);
 			v.setTag(this);
 		}
 	}
 
 	private String getSummaryWithoutHTML(String s) {
 		if (!(s == null || EMPTY_STRING.equals(s) || s.startsWith(HTML_OPEN_MARK))) {
 			int index = s.indexOf(HTML_OPEN_MARK);
 			if (index != -1) {
 				return s.substring(0, index);
 			}
 		}
 		return EMPTY_STRING;
 	}
 
 	/************ CHECKBOX LISTENER *******************/
 
 	/**
 	 * Listener for the read/unread checkbox
 	 */
 	public class MyCheckReadListener implements OnCheckedChangeListener {
 		ListEntry entry;
 
 		public MyCheckReadListener(ListEntry entry) {
 			this.entry = entry;
 		}
 
 		@Override
 		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 			if (isChecked) {
 				markEntryAs(MARK_AS_READ, entry, context.getResources().getString(R.string.marked_as_read),
 						MARK_ACTION.READ);
 			} else {
 				markEntryAs(MARK_AS_UNREAD, entry, context.getResources().getString(R.string.marked_as_unread),
 						MARK_ACTION.UNREAD);
 			}
 		}
 	}
 
 	private void markEntryAs(String mark, ListEntry entry, String successMessage, MARK_ACTION action) {
 		RequestQueue queue = MyVolley.getRequestQueue();
 		try {
 			String accessToken = getSharedPrefs().getString(MainActivity.SHPREF_KEY_ACCESS_TOKEN, null);
 			JSONObject jsonRequest = new JSONObject();
 			jsonRequest.put(ACTION, mark);
 			jsonRequest.put(TYPE, ENTRIES);
 			JSONArray entries = new JSONArray();
 			entries.put(entry.getId());
 			jsonRequest.put(ENTRIES_IDS, entries);
 			JsonCustomRequest myReq = NetworkUtils.getJsonCustomRequest(Method.POST, MainActivity.ROOT_URL
 					+ MainActivity.MARKERS_PATH, jsonRequest, getActionListenerEntrySuccessListener(successMessage, action, entry),
 					createMyReqErrorListener(), accessToken);
 			queue.add(myReq);
 
 		} catch (JSONException uex) {
 			uex.printStackTrace();
 			Log.e(TAG, "JSONException marking as read/unread");
 		}
 	}
 
 	private SharedPreferences getSharedPrefs() {
 		return PreferencesActivity.getPreferences(context);
 	}
 	private Response.Listener<JSONObject> getActionListenerEntrySuccessListener(final String successMessage, final MARK_ACTION action, final ListEntry entry) {
 			
 		return new Response.Listener<JSONObject>() {
 			@Override
 			public void onResponse(JSONObject response) {
 				markEntry(action, entry);	
 				Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show();
 			}
 		};
 	}
 
 	public void markEntry(MARK_ACTION action, ListEntry entry) {
 		switch (action) {
 		case SAVE:
 			entry.setSaved(true);
 //			getAppData().markSubsriptionAs(MARK_ACTION.SAVE, entry.getId());
 			break;
 		case UNSAVE:
 			entry.setSaved(true);
 			break;
 		case READ:
 			entry.setUnread(false);
 			notifyDataSetChanged();
 			notifySubscriptionsChange(entry, action);
 			break;
 		case UNREAD:
 			entry.setUnread(true);
 			notifyDataSetChanged();
 			notifySubscriptionsChange(entry, action);
 			break;
 		default:
 			break;		
 		}
 	}
 
 	/**
 	 * Notify the subscriptions in the drawer of the change
 	 * @param entry entry read/unread
 	 * @param action action (read or unread)
 	 */
 	private void notifySubscriptionsChange(ListEntry entry, MARK_ACTION action) {
 		MainActivity mainActivity = (MainActivity) this.getContext();
 		mainActivity.notifySubscriptionRead(entry, action);
 	}
 	
 	private Response.ErrorListener createMyReqErrorListener() {
 		return new Response.ErrorListener() {
 			@Override
 			public void onErrorResponse(VolleyError error) {
 				Log.e(TAG, "Error marking entry in stream list" + error.getMessage());
 			}
 		};
 	}
 
 	/************ SAVE LISTENER *******************/
 
 	/**
 	 * Listener for the read/unread checkbox
 	 */
 	public class MySaveListener implements OnClickListener {
 		ListEntry entry;
 
 		public MySaveListener(ListEntry entry) {
 			this.entry = entry;
 		}
 
 		@Override
 		public void onClick(View v) {
			try {
 			ImageView iView = (ImageView) v;
 			int tag = ((Integer) iView.getTag()).intValue();
 			if (tag == R.drawable.star_off) {
 				iView.setImageResource(R.drawable.star_on);
 				iView.setTag(R.drawable.star_on);
 				saveOrUnsaveEntry(Method.PUT, entry, context.getResources().getString(R.string.saved_article),
 						MARK_ACTION.SAVE);
 			} else {
 				iView.setImageResource(R.drawable.star_off);
 				iView.setTag(R.drawable.star_off);
 				saveOrUnsaveEntry(Method.DELETE, entry, context.getResources().getString(R.string.unsaved_article),
 						MARK_ACTION.UNSAVE);
 			}
			} catch (Exception ex) {
				Log.e(TAG, "Error in ListViewEntryArrayAdapter.MySaveListener.Onlick");
			}
 		}
 	}
 	
 	private void saveOrUnsaveEntry(int method, ListEntry entry, String successMessage, MARK_ACTION action) {
 		RequestQueue queue = MyVolley.getRequestQueue();
 		try {
 			JSONObject jsonRequest = new JSONObject();
 			JSONArray entries = new JSONArray();
 			entries.put(entry.getId());			
 			SharedPreferences sprPreferences = getSharedPrefs();
 			String accessToken = sprPreferences.getString(MainActivity.SHPREF_KEY_ACCESS_TOKEN, null);
 			String userId = sprPreferences.getString(MainActivity.SHPREF_KEY_USERID_TOKEN, null);
 			String userIdEncoded = URLEncoder.encode("/" + userId.toString() + "/tag/", MainActivity.UTF_8);
 			StringBuilder url = new StringBuilder();
 			url.append(MainActivity.ROOT_URL).append(MainActivity.TAGS_PATH).append("/user").append(userIdEncoded)
 					.append(MainActivity.GLOBAL_SAVED);
 			if (accessToken != null && userId != null) {
 				if (method == Method.PUT) {
 					jsonRequest.put(ENTRIES_IDS, entries);
 					JsonCustomRequest myReq = NetworkUtils.getJsonCustomRequest(method, url.toString(), jsonRequest,
 							getActionListenerEntrySuccessListener(successMessage, action, entry),
 							createMyReqErrorListener(), accessToken);
 					queue.add(myReq);
 				} else {
 					String entryIdEncoded = URLEncoder.encode(entry.getId(), MainActivity.UTF_8);
 					url.append("/" + entryIdEncoded);
 					JsonCustomRequest myReq = NetworkUtils.getJsonCustomRequest(method, url.toString(), jsonRequest,
 							getActionListenerEntrySuccessListener(successMessage, action, entry),
 							createMyReqErrorListener(), accessToken);
 					queue.add(myReq);
 				}
 			}
 		} catch (JSONException uex) {
 			Log.e(TAG, "JSONException marking as read/unread");
 		} catch (UnsupportedEncodingException uex) {
 			Log.e(TAG, "Error encoding URL when saving/unsaving");
 		}
 	}
 
 }
