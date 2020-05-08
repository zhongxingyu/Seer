 package de.geotweeter;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import android.content.Context;
 import android.util.Pair;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import de.geotweeter.activities.TimelineActivity;
 import de.geotweeter.timelineelements.TLEComparator;
 import de.geotweeter.timelineelements.TimelineElement;
 
 public class TimelineElementAdapter extends ArrayAdapter<TimelineElement>{
 	private ArrayList<TimelineElement> items;
 	private Context context;
 	private boolean useDarkTheme;
 	
 
 	public TimelineElementAdapter(Context context, 
 			int textViewResourceId, ArrayList<TimelineElement> objects) {
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
 	
 	public void addAllAsFirst(ArrayList<TimelineElement> elements) {
 		Collections.sort(elements, new TLEComparator());
 		items.addAll(0, elements);
 		for (TimelineElement t : elements) {
 			TimelineActivity.addToAvailableTLE(t);
 		}
 		this.notifyDataSetChanged();
 	}
 	
 	public View getView(int position, View convertView, ViewGroup parent) {
 		TimelineElement t = (TimelineElement) items.get(position);
 		
 		View v = convertView;
 		if (v == null) {
 			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			v = vi.inflate(R.layout.timeline_element, null);
 		}
 		
 		if (t != null) {
 			FrameLayout mapContainer = (FrameLayout) v.findViewById(R.id.map_fragment_container);
 			mapContainer.removeAllViews();
 			
 			View mapAndControls = v.findViewById(R.id.map_and_controls);
 			mapAndControls.setVisibility(View.GONE);
 			
 			TextView txtView;
 			String text;
 			
 			txtView = (TextView) v.findViewById(R.id.txtSender);
 			if (txtView != null) {
 				txtView.setText(t.getSenderString());
 			}
 			
 			txtView = (TextView) v.findViewById(R.id.txtTimestamp);
 			if (txtView != null) {
 				txtView.setText(t.getDateString());
 			}
 			
 			txtView = (TextView) v.findViewById(R.id.txtText);
 			if (txtView != null) { 
 				txtView.setText(t.getTextForDisplay());
 			}
 			
 			txtView = (TextView) v.findViewById(R.id.txtPlace);
 			if (txtView != null) {
 				text = t.getPlaceString();
 				if (text == null) {
 					txtView.setVisibility(View.GONE);
 				} else {
 					txtView.setText(text);
 					txtView.setVisibility(View.VISIBLE);
 				}
 			}
 			
 			txtView = (TextView) v.findViewById(R.id.txtSource);
 			if (txtView != null) {
 				text = t.getSourceText();
 				if (text == null) {
 					txtView.setVisibility(View.GONE);
 				} else {
 					txtView.setText(text);
 					txtView.setVisibility(View.VISIBLE);
 				}
 			}
 			
 			ImageView img = (ImageView) v.findViewById(R.id.avatar_image);
 			if (img != null) {
 				TimelineActivity.getBackgroundImageLoader(getContext()).displayImage(t.getAvatarSource(), img, true);
 			}
 			
 			ArrayList<Pair<URL, URL>> media_list = t.getMediaList();
 			LinearLayout picPreviews = (LinearLayout) v.findViewById(R.id.picPreviews);
 			if (!media_list.isEmpty()) {
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
 		v.setBackgroundResource(t.getBackgroundDrawableID(useDarkTheme));
 		return v;	
 	}
 	
 	protected void showOverlay(String url) {
 		ImageView img_overlay = (ImageView) TimelineActivity.getInstance().findViewById(R.id.img_overlay);
 		TimelineActivity.getBackgroundImageLoader(context).displayImage(url, img_overlay, false);
 		img_overlay.setVisibility(View.VISIBLE);
 	}
 
 	public ArrayList<TimelineElement> getItems() {
 		return items;
 	}
 
 
 }
