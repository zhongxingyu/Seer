 package de.geotweeter;
 
 import java.net.URL;
 import java.util.Collections;
 import java.util.List;
 
 import android.content.Context;
 import android.util.Pair;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
 import android.widget.ArrayAdapter;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import de.geotweeter.activities.TimelineActivity;
 import de.geotweeter.timelineelements.TLEComparator;
 import de.geotweeter.timelineelements.TimelineElement;
 import de.geotweeter.timelineelements.Tweet;
 
 public class TimelineElementAdapter extends ArrayAdapter<TimelineElement>{
 	private List<TimelineElement> items;
 	private Context context;
 	private boolean useDarkTheme;
 	
 
 	public TimelineElementAdapter(Context context, 
 			int textViewResourceId, List<TimelineElement> objects) {
 		super(context, textViewResourceId, objects);
 		this.items = objects;
 		this.context = context;
 		useDarkTheme = context.getSharedPreferences(Constants.PREFS_APP, 0).getBoolean("pref_dark_theme", false);
 	}
 	
 	public void addAsFirst(TimelineElement t) {
 		items.add(0, t);
 		TimelineActivity.addToAvailableTLE(t);
 		this.notifyDataSetChanged();
 	}
 	
 	public void addAllAsFirst(List<TimelineElement> elements) {
 		Collections.sort(elements, new TLEComparator());
 		items.addAll(0, elements);
 		for (TimelineElement t : elements) {
 			TimelineActivity.addToAvailableTLE(t);
 		}
 		this.notifyDataSetChanged();
 	}
 	
 	public View getView(int position, View convertView, ViewGroup parent) {
 		
 		TimelineElement tle = (TimelineElement) items.get(position);
 		boolean is_retweet = false;
 		String retweeter = "";
 		
 		View v = convertView;
 		if (v == null) {
 			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			v = vi.inflate(R.layout.timeline_element, null);
 		}
 		
 		if (tle.getClass() == Tweet.class) {
 			Tweet t = (Tweet) tle;
 			if (t.retweeted_status != null) {
 				tle = t.retweeted_status;
 				is_retweet = true;
 				retweeter = t.getSenderScreenName();
 			}
 		}
 		
 		if (tle != null) {
 			FrameLayout mapContainer = (FrameLayout) v.findViewById(R.id.map_fragment_container);
 			mapContainer.removeAllViews();
 			
 			View mapAndControls = v.findViewById(R.id.map_and_controls);
 			mapAndControls.setVisibility(View.GONE);
 			
 			TextView txtView;
 			String text;
 			
 			txtView = (TextView) v.findViewById(R.id.txtSender);
 			if (txtView != null) {
 				txtView.setText(tle.getSenderString());
 			}
 			
 			txtView = (TextView) v.findViewById(R.id.txtTimestamp);
 			if (txtView != null) {
 				txtView.setText(tle.getDateString());
 			}
 			
 			txtView = (TextView) v.findViewById(R.id.txtText);
 			if (txtView != null) { 
 				txtView.setText(tle.getTextForDisplay());
 			}
 			
 			txtView = (TextView) v.findViewById(R.id.txtPlace);
 			if (txtView != null) {
 				text = tle.getPlaceString();
 				if (text == null) {
 					txtView.setVisibility(View.GONE);
 				} else {
 					txtView.setText(text);
 					txtView.setVisibility(View.VISIBLE);
 				}
 			}
 			
 			txtView = (TextView) v.findViewById(R.id.txtSource);
 			if (txtView != null) {
 				if (is_retweet) {
 					text = "RT by " + retweeter;
 				} else {
 					text = tle.getSourceText();
 				}
 				if (text == null) {
 					txtView.setVisibility(View.GONE);
 				} else {
 					txtView.setText(text);
 					txtView.setVisibility(View.VISIBLE);
 				}
 			}
 			
 			ImageView img = (ImageView) v.findViewById(R.id.avatar_image);
 			if (img != null) {
 				if (tle.getAvatarSource() == null) {
 					img.setImageResource(R.drawable.loading_image);
 				} else {
 					TimelineActivity.getBackgroundImageLoader(getContext()).displayImage(tle.getAvatarSource(), img, true);
 				}
 			}
 			
 			List<Pair<URL, URL>> media_list = tle.getMediaList();
 			LinearLayout picPreviews = (LinearLayout) v.findViewById(R.id.picPreviews);
 			if (media_list != null && !media_list.isEmpty()) {
 				picPreviews.removeAllViews();
 				picPreviews.setVisibility(View.VISIBLE);
 				for (final Pair<URL, URL> url_pair : media_list) {
 					ImageView thumbnail = new ImageView(context);
 					picPreviews.addView(thumbnail);
 					thumbnail.setFocusable(false);
 					thumbnail.getLayoutParams().width = 75;
 					thumbnail.getLayoutParams().height = 75;
 					thumbnail.setScaleType(ScaleType.CENTER_CROP);
 					thumbnail.setImageResource(R.drawable.ic_launcher);
 					TimelineActivity.getBackgroundImageLoader(context).displayImage(url_pair.first.toString(), thumbnail, false);
 					thumbnail.setOnClickListener(new OnClickListener() {
 						
 						@Override
 						public void onClick(View v) {
 							showOverlay(url_pair.second.toString());
 						}
 					});
 				}
 			} else {
 				picPreviews.setVisibility(View.GONE);
 			}
 		}
 //		v.findViewById(R.id.tweet_element).setOnClickListener(new OnClickListener());
 		v.setBackgroundResource(tle.getBackgroundDrawableID(useDarkTheme));
 		return v;	
 	}
 	
 	protected void showOverlay(String url) {
		ImageView img_overlay = null;
		try {
			if (!TimelineActivity.getInstance().isVisible()) {
				return;
			}
			img_overlay = (ImageView) TimelineActivity.getInstance().findViewById(R.id.img_overlay);
		} catch (NullPointerException e) {
			return;
		}
 		TimelineActivity.getBackgroundImageLoader(context).displayImage(url, img_overlay, false);
 		img_overlay.setVisibility(View.VISIBLE);
 	}
 
 	public List<TimelineElement> getItems() {
 		return items;
 	}
 
 
 }
