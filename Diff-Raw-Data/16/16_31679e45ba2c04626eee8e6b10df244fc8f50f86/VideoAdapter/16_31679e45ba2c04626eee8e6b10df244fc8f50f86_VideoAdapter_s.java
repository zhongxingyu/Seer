 package com.ricex.rpi.remote.android;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.ricex.rpi.common.video.Video;
 
 
 public class VideoAdapter  extends BaseAdapter {
 
 	/** The context of this adapter */
 	private Context context;
 	
 	/** The list of items in this adapter */
 	private List<Video> items;
 	
 	/** Creates a new Video Adapter with an empty list of videos
 	 * 
 	 */
 	
 	public VideoAdapter(Context context) {
 		this(context, new ArrayList<Video>());
 	}
 	
 	/** Creates a new Video Adapter with a list of the given items
 	 * 
 	 * @param items
 	 */
 	
 	public VideoAdapter(Context context, List<Video> items) {
 		this.context = context;
 		this.items = items;
 	}
 	
 	/** Adds the given video to the item list 
 	 * 
 	 * @param item Video to add
 	 */
 	
 	public void addItem(Video item) {
 		items.add(item);
 	}
 	/**
 	 * {@inheritDoc}
 	 */
 	
 	@Override
 	public int getCount() {
 		return items.size();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	
 	@Override
 	public Video getItem(int position) {
 		return items.get(position);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		if (convertView == null) {
 			LayoutInflater inflater = LayoutInflater.from(getContext());
 			convertView = inflater.inflate(R.layout.movies_list_item, null);
 		}
 		
 		Video video = getItem(position);
 		
 		if (video != null) {
 			ImageView icon = (ImageView) convertView.findViewById(R.id.movies_thumbnail);
 			TextView textView = (TextView) convertView.findViewById(R.id.movies_text);
 			
 			//Set the icon of the video appropriatly 
 			
 			if (video.isDirectory()) {
 				icon.setImageResource(R.drawable.directory);
 			}
 			else {
 				icon.setImageResource(R.drawable.movie);
 			}
 			
 			
 			textView.setText(video.getName());
 			
 		}
 		
		return null;
 	}
 
 	private Context getContext() {
 		return context;
 	}
 
 }
