 package edu.umd.umiacs.newsstand;
 
 import java.util.BitSet;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class TopStoriesListView extends ListView {
 	private TopStories _ctx;
 	final BitSet _mEnabled = new BitSet();
 
 	private TopStoryAdapter _mAdapter;
 
 	public TopStoriesListView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		_ctx = (TopStories)context;
 	}	
 
 	public void initAdapter(TopStoriesFeed feed){
 		_mAdapter = new TopStoryAdapter(_ctx, feed.getAllInfos());
 		setAdapter(_mAdapter);
 	}
 	
 	public class TopStoryAdapter extends ArrayAdapter<TopStoriesInfo>{
 		private final TopStories _ctx;
 		private final List<TopStoriesInfo> _mValues;
 		private int currentDisplay;
 
 		public TopStoryAdapter(TopStories context, List<TopStoriesInfo> allInfos) {
 			super(context, R.layout.topstorylistview, allInfos);
 			this._ctx = (TopStories)context;
 			this._mValues = allInfos;
 			this.currentDisplay = -1;
 		}
 		
 		public void clickFirstVisible(int pos) {
 			
 			int tmp = pos >= _mValues.size() ? pos - 1 : pos;
 			
 			if (tmp != currentDisplay) {
 				_ctx.updateMapView(_mValues.get(tmp).getCluster_id());
 				_ctx.getRefresh().isClick();
 				currentDisplay = tmp;
 			}
 		}
 		
		//unverified
		public void onItemClick(AdapterView<?> adaptView, View clickedView, int position, long id) {
			clickedView.setSelected(true);
			Log.i("DEBUG", "item selected");
		}
		
 		@Override
 		public View getView(final int position, View convertView, ViewGroup parent) {
 			LayoutInflater inflater = (LayoutInflater) _ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			View rowView = inflater.inflate(R.layout.topstorylistview, parent, false);
 			final TopStoriesInfo cur = _mValues.get(position);
 			ImageView marker = (ImageView) rowView.findViewById(R.id.imageMarker);
 			
 			String topic = cur.getTopic();
 
 			if (topic.equals("General"))
 				marker.setImageResource(R.drawable.marker_general);
 			else if (topic.equals("Business"))
 				marker.setImageResource(R.drawable.marker_business);
 			else if (topic.equals("Entertainment"))
 				marker.setImageResource(R.drawable.marker_entertainment);
 			else if (topic.equals("Health"))
 				marker.setImageResource(R.drawable.marker_health);
 			else if (topic.equals("SciTech"))
 				marker.setImageResource(R.drawable.marker_scitech);
 			else if (topic.equals("Sports"))
 				marker.setImageResource(R.drawable.marker_sports);
 			else {
 				marker.setImageResource(R.drawable.marker_general);
 				Toast.makeText(_ctx, "Bad topic: " + topic, Toast.LENGTH_SHORT).show();
 			}
 			
 			TextView titleView = (TextView) rowView.findViewById(R.id.topstorytext);
 			titleView.setText(cur.getTitle());
 			titleView.setOnClickListener(new OnClickListener(){
 				@Override
 				public void onClick(View v){
 					Uri uri = Uri.parse(cur.getURL());
 					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
 					_ctx.startActivity(intent);
 					
 				}});
 			TextView domainView = (TextView) rowView.findViewById(R.id.topstorydomain);
 			domainView.setText(cur.getDomain());
 			domainView.setOnClickListener(new OnClickListener(){
 				@Override
 				public void onClick(View v){
 					_ctx.updateMapView(cur.getCluster_id());				
 				}});
 			TextView descrView = (TextView) rowView.findViewById(R.id.topstorydescription);
 			descrView.setText(cur.getDescription());
 			descrView.setOnClickListener(new OnClickListener(){
 				@Override
 				public void onClick(View v){
 					_ctx.updateMapView(cur.getCluster_id());
 					_ctx.getRefresh().isClick();
 				}});
 			String temp = cur.getNum_images();
 			ImageView image1 = (ImageView) rowView.findViewById(R.id.topstoryimage);
 			image1.setImageResource(R.drawable.images);
 			if (temp != null && ! temp.equals("\n")) {
 				image1.setOnClickListener(new OnClickListener(){
 		    		@Override
 		    		public void onClick(View v) {
 		    			Intent i = new Intent(_ctx, ImageGridView.class);
 		    			i = i.putExtra("cluster_id", cur.getCluster_id());
 		    			_ctx.startActivity(i);
 		    		}});
 			}
 			else{
 				image1.setAlpha(0);
 			}
 			temp = cur.getNum_videos();
 			ImageView image2 = (ImageView) rowView.findViewById(R.id.topstoryvideo);
 			image2.setImageResource(R.drawable.video);
 			if (temp != null && ! temp.equals("\n") && !temp.equals("0")) {
 				image2.setOnClickListener(new OnClickListener(){
 					@Override
 					public void onClick(View v){
 						Intent j = new Intent(_ctx, VideoListActivity.class);
 						j = j.putExtra("cluster_id", cur.getCluster_id());
 						_ctx.startActivity(j);
 					}});
 			}
 			else{
 				image2.setAlpha(0);
 			}
 			
 			ImageView image3 = (ImageView) rowView.findViewById(R.id.topstoryrelate);
 			image3.setImageResource(R.drawable.related);
 			image3.setOnClickListener(new OnClickListener(){
 				@Override
 				public void onClick(View v){
 		        	Intent k = new Intent(_ctx, NewsStandWebView.class);
 		        	String url = "http://newsstand.umiacs.umd.edu/news/story_light?cluster_id=";
 		        	url += cur.getCluster_id();
 		        	url += "&limit=30&page=1";
 		        	k = k.putExtra("url", url);
 		        	_ctx.startActivity(k);
 				}
 			});
 
 			return rowView;
 		}
 		
 		
 	}
 	
 }
