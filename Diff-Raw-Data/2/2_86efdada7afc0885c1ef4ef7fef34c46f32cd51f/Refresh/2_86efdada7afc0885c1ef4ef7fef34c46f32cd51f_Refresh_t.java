 package edu.umd.umiacs.newsstand;
 
 import java.net.URL;
 import java.util.List;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.InputSource;
 import org.xml.sax.XMLReader;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.ColorFilter;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.graphics.Typeface;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.SeekBar;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.Overlay;
 
 import edu.umd.umiacs.newsstand.MarkerOverlay.MarkerOverlayItem;
 
 public class Refresh implements Runnable {
 
 	// references to other class member variables
 	private final NewsStand _ctx;
 	private final NewsStandMapView _mapView;
 	private final SeekBar _slider;
 	private final Resources _resources;
 	private final SharedPreferences _settingPrefs;
 	private final SharedPreferences _sourcePrefs;
 
 	// member variables
 	private int mNumExecuting = 0;
 	private int mShowIdx = 0;
 	private int mAjaxIdx = 0;
 
 	private int mLatL = 0;
 	private int mLatH = 0;
 	private int mLonL = 0;
 	private int mLonH = 0;
 
 	public Refresh(Context ctx) {
 		_ctx = (NewsStand)ctx;
 		_mapView = _ctx.getMapView();
 		_slider = _ctx.getSlider();
 		_resources = _ctx.getResources();
 		_settingPrefs = _ctx.getPrefsSetting();
 		_sourcePrefs = _ctx.getPrefsSource();
 	}
 
 	@Override
 	public void run() {
 		executeForce();
 	}
 
 	public void execute() {
 		if (mNumExecuting < 3) {
 			if (curBoundsDiffer()) {
 				updateBounds();
 				new RefreshTask().execute("");
 			}
 		}
 	}
 
 	public void executeForce() {
 		updateBounds();
 		new RefreshTask().execute("");
 	}
 
 	public void clearSavedLocation() {
 		mLatL = 0;
 		mLatH = 0;
 		mLonL = 0;
 		mLonH = 0;
 	}
 
 	private boolean curBoundsDiffer() {
 		GeoPoint centerpoint = _mapView.getMapCenter();
 		int lat_span = _mapView.getLatitudeSpan();
 		int lon_span = _mapView.getLongitudeSpan();
 
 		int lat_l = centerpoint.getLatitudeE6() - (lat_span / 2);
 		int lat_h = lat_l + lat_span;
 		int lon_l = centerpoint.getLongitudeE6() - (lon_span / 2);
 		int lon_h = lon_l + lon_span;
 
 		return (lat_l != mLatL || lat_h != mLatH || lon_l != mLonL || lon_h != mLonH);
 	}
 
 	private void updateBounds() {
 		GeoPoint centerpoint = _mapView.getMapCenter();
 		int lat_span = _mapView.getLatitudeSpan();
 		int lon_span = _mapView.getLongitudeSpan();
 
 		mLatL = centerpoint.getLatitudeE6() - (lat_span / 2);
 		mLatH = mLatL + lat_span;
 		mLonL = centerpoint.getLongitudeE6() - (lon_span / 2);
 		mLonH = mLonL + lon_span;
 	}
 
 	private MarkerFeed getMarkers() {
 		// get map coordinates
 
 		_ctx.updateHome();
 	//	_ctx.updateOneHand();
 		
 		String marker_url = "http://newsstand.umiacs.umd.edu/news/xml_map?lat_low=%f&lat_high=%f&lon_low=%f&lon_high=%f";
 		marker_url = String.format(marker_url,
 							mLatL / 1E6, mLatH / 1E6,
 							mLonL / 1E6, mLonH / 1E6);
 
 		//THIS IS HOW SEARCH IS USED
 		if (_ctx.mSearchQuery != null && _ctx.mSearchQuery != "") {
 			marker_url += String.format("&search=%s", _ctx.mSearchQuery);
 		}
 
 		//sourcesParam, rankParam, layerParam, topicParam, imagesParam, videosParam, boundParam, countryParam;
 		// sourcesParam - 
 		marker_url += sourceQuery();
 		
 		// rankParam - 
 		marker_url += rankQuery();
 
 		// layerParam - total four layers
 		marker_url += layerQuery();
 
 		// topicParam - topics
 		marker_url += topicQuery();
 
 		// imagesParam - num of images
 		marker_url += imageQuery();
 		
 		// videosParam - num of videos
 		marker_url += videoQuery();
 		
 		// boundParam - need to find out what is this . TODO2
 
 		
 		// countryParam - need to find out what is this . TODO2
 		marker_url += countryQuery();
 		
 		Log.i("feed url", marker_url);
 		
 		return getFeed(marker_url);
 	}
 
 	private String topicQuery() {
 		if (_settingPrefs.getString("all_topics", "false").equals("true")) {
 			// add nothing to query string if showing all topics
 		} else {
 			String topics = ""; 
 			if (_settingPrefs.getString("general_topics", "false").equals("true")) {
 				topics += "'General',";
 			}
 			if (_settingPrefs.getString("business_topics", "false").equals("true")) {
 				topics += "'Business',";
 			}
 			if (_settingPrefs.getString("scitech_topics", "false").equals("true")) {
 				topics += "'SciTech',";
 			}
 			if (_settingPrefs.getString("entertainment_topics", "false").equals("true")) {
 				topics += "'Entertainment',";
 			}
 			if (_settingPrefs.getString("health_topics", "false").equals("true")) {
 				topics += "'Health',";
 			}
 			if (_settingPrefs.getString("sports_topics", "false").equals("true")) {
 				topics += "'Sports',";
 			}
 			if (topics.length() > 0) {
 				return String.format("&cat=(%s)", topics.substring(0, topics.length()-1));
 			}
 		}
		return "";
 	}
 
 	private String layerQuery(){
 		String layer = _settingPrefs.getString("layer", "3");
 		int layerInt = Integer.valueOf(layer);
 		if (layerInt < 3)
 			return String.format("&layer=%s", layer);
 		return "";
 	}
 
 	private String videoQuery(){
 		String video = _settingPrefs.getString("videos", "0");
 		int videoInt = Integer.valueOf(video);
 		if(videoInt != 0)
 			return String.format("&num_videos=%s", String.valueOf(videoInt-1));
 		return "";
 	}
 
 	private String imageQuery(){
 		String image = _settingPrefs.getString("images", "0");
 		int imageInt = Integer.valueOf(image);
 		if(imageInt != 0)
 			return String.format("&num_images=%s", String.valueOf(imageInt-1));
 		return "";
 	}
 	
 	private String sourceQuery(){
 		//TODO 
 		String result = "";
 /*		boolean defaultSource = true;
 		
 		if(_sourcePrefs.getBoolean("new_york_time", false))
 			result += "";
 		else
 			defaultSource = false;
 
 		if(_sourcePrefs.getBoolean("washington_post", false))
 			result += "";
 		else
 			defaultSource = false;
 		if(_sourcePrefs.getBoolean("usa_today", false))
 			result += "";
 		else
 			defaultSource = false;
 		if(_sourcePrefs.getBoolean("wall_street_journal", false))
 			result += "";
 		else
 			defaultSource = false;
 		if(_sourcePrefs.getBoolean("la_times", false))
 			result += "";
 		else
 			defaultSource = false;
 		if(_sourcePrefs.getBoolean("boston_globe", false))
 			result += "";
 		else
 			defaultSource = false;
 		if(_sourcePrefs.getBoolean("new_york_time", false))
 			result += "";
 		else
 			defaultSource = false;
 		if(_sourcePrefs.getBoolean("miami_herald", false))
 			result += "";
 		else
 			defaultSource = false;
 		if(_sourcePrefs.getBoolean("atlanta", false))
 			result += "";
 		else
 			defaultSource = false;
 		if(_sourcePrefs.getBoolean("guardian", false))
 			result += "";
 		else
 			defaultSource = false;
 		if(_sourcePrefs.getBoolean("times_online", false))
 			result += "";
 		else
 			defaultSource = false;
 		if(_sourcePrefs.getBoolean("haaretz", false))
 			result += "";
 		else
 			defaultSource = false;
 		if(_sourcePrefs.getBoolean("irish_independent", false))
 			result += "";
 		else
 			defaultSource = false;
 		if(_sourcePrefs.getBoolean("uk_independent", false))
 			result += "";
 		else
 			defaultSource = false;
 		if(_sourcePrefs.getBoolean("iht", false))
 			result += "";
 		else
 			defaultSource = false;
 		if(_sourcePrefs.getBoolean("reuters", false))
 			result += "";
 		else
 			defaultSource = false;
 		if(_sourcePrefs.getBoolean("associated_press", false))
 			result += "";
 		else
 			defaultSource = false;
 		if(_sourcePrefs.getBoolean("times_of_india", false))
 			result += "";
 		else
 			defaultSource = false;
 		*/
 		return result;
 	}
 	
 	private String rankQuery(){
 		String rank = _sourcePrefs.getString("rank", "2");
 		int rankInt = Integer.valueOf(rank);
 		if(rankInt == 0)
 			return "&rank=time";
 		else if(rankInt == 1)
 			return "&rank=reputable";
 		else if(rankInt == 2)
 			return "&rank=newest";
 		else if(rankInt == 3)
 			return "&rank=twitter";
 		
 		return "";
 	}
 	
 	private String countryQuery(){
 		return "";
 /*		//TODO - need to verify this 
 		boolean defaultCountry = true;
 		String result = "";
 		if (_sourcePrefs.getBoolean("australia", false))
 		{
 			result += "Australia,";
 			defaultCountry = false;
 		}
 		if (_sourcePrefs.getBoolean("canada", false))
 			result += "Canada,";
 			defaultCountry = false;
 		if (_sourcePrefs.getBoolean("india", false))
 			result += "India,";
 			defaultCountry = false;
 		if (_sourcePrefs.getBoolean("iran", false))
 			result += "Iran,";
 			defaultCountry = false;
 		if (_sourcePrefs.getBoolean("ireland", false))
 			result += "Ireland,";
 			defaultCountry = false;
 		if (_sourcePrefs.getBoolean("israel", false))
 			result += "Israel,";
 			defaultCountry = false;
 		if (_sourcePrefs.getBoolean("japan", false))
 			result += "Japan,";
 			defaultCountry = false;
 		if (_sourcePrefs.getBoolean("mexico", false))
 			result += "Mexico,";
 			defaultCountry = false;
 		if (_sourcePrefs.getBoolean("new_zealand", false))
 			result += "New Zealand,";
 			defaultCountry = false;
 		if (_sourcePrefs.getBoolean("russia", false))
 			result += "Russia,";
 			defaultCountry = false;
 		if (_sourcePrefs.getBoolean("united_kingdom", false))
 			result += "United Kingdom,";
 			defaultCountry = false;
 		if (_sourcePrefs.getBoolean("united_states", false))
 			result += "United States,";
 		else
 			defaultCountry = false;
 				
 		if (defaultCountry)
 			return "";			
 		return String.format("&cname=(%s)", result.substring(0, result.length()-1));
 		*/
 	}
 
 	private void setMarkers(MarkerFeed feed) {
 		try {
 		List<Overlay> mapOverlays = _mapView.getOverlays();
 		
 		MarkerOverlay itemizedoverlay = new MarkerOverlay( 
 				_resources.getDrawable(R.drawable.marker_general), _ctx);
 
 		String layer = _settingPrefs.getString("layer", "3");
 		int layerInt = Integer.valueOf(layer);
 
 		if(layerInt == 3) {	// icon layer
 			for (int i = 0; i < feed.getMarkerCount(); i++) {
 				MarkerInfo cur_marker = feed.getMarker(i);
 				float lat = 0.0f;
 				if(! cur_marker.getLatitude().contains("-"));
 					lat = Float.valueOf(cur_marker.getLatitude()).floatValue();
 				float lon = 0.0f;
 				if( ! cur_marker.getLongitude().contains("-"));
 					lon = Float.valueOf(cur_marker.getLongitude()).floatValue();
 				GeoPoint point = new GeoPoint(
 						(int) ( lat* 1E6),
 						(int) ( lon* 1E6));
 				//description but no snippet
 				MarkerOverlayItem overlayitem = new MarkerOverlayItem(point,
 						cur_marker.getTitle(), cur_marker.getSnippet(),
 						cur_marker.getGazID(), cur_marker.getName());
 
 				String cur_topic = cur_marker.getTopic();
 
 				int my_marker = 0;
 
 				if (cur_topic.equals("General"))
 					my_marker = R.drawable.marker_general;
 				else if (cur_topic.equals("Business"))
 					my_marker = R.drawable.marker_business;
 				else if (cur_topic.equals("Entertainment"))
 					my_marker = R.drawable.marker_entertainment;
 				else if (cur_topic.equals("Health"))
 					my_marker = R.drawable.marker_health;
 				else if (cur_topic.equals("SciTech"))
 					my_marker = R.drawable.marker_scitech;
 				else if (cur_topic.equals("Sports"))
 					my_marker = R.drawable.marker_sports;
 				else {
 					my_marker = R.drawable.marker_general;
 					//not sure what it means, but it gets annoying
 					//Toast.makeText(_ctx, "Bad topic: " + cur_topic, Toast.LENGTH_SHORT).show();
 				}
 				itemizedoverlay.addOverlay(overlayitem, _resources.getDrawable(my_marker));
 			}
 		} else {
 			for (int i = 0; i < feed.getMarkerCount(); i++) {
 				MarkerInfo cur_marker = feed.getMarker(i);
 				GeoPoint point = new GeoPoint(
 						(int) (Float.valueOf(cur_marker.getLatitude()).floatValue() * 1E6),
 						(int) (Float.valueOf(cur_marker.getLongitude()).floatValue() * 1E6));
 				MarkerOverlayItem overlayitem = new MarkerOverlayItem(point,
 						cur_marker.getTitle(), cur_marker.getSnippet(), cur_marker.getGazID(),
 						cur_marker.getName());
 				
 				String marker_text = "";
 				if (layerInt == 4){	//location layer
 					marker_text = cur_marker.getName();
 				}
 				else {	// disease or people layer
 					marker_text = cur_marker.getKeyword();
 			 	}
 				MarkerOverlayText textOverlay = new MarkerOverlayText(marker_text);
 				itemizedoverlay.addOverlay(overlayitem, textOverlay);
 			}
 
 		} 
 
 		if (feed.getMarkerCount() > 0) {
 			itemizedoverlay.setPctShown(_slider.getProgress(), _ctx);
 			if (mapOverlays.size() > 0) {
 				mapOverlays.set(0,itemizedoverlay);
 			}
 			else {
 				mapOverlays.add(itemizedoverlay);
 			}
 			_mapView.invalidate();
 		}
 		} catch(NumberFormatException e) {
 			//I believe this happens when repeatedly panning, too fast and long
 		}
 	}
 
 	private MarkerFeed getFeed(String urlToRssFeed) {
 		try {
 			// set up the url
 			URL url = new URL(urlToRssFeed);
 
 			// create the factory
 			SAXParserFactory factory = SAXParserFactory.newInstance();
 			// create a parser
 			SAXParser parser = factory.newSAXParser();
 
 			// create the reader (scanner)
 			XMLReader xmlreader = parser.getXMLReader();
 			// instantiate our handler
 			MarkerFeedHandler theMarkerFeedHandler = new MarkerFeedHandler();
 			// assign our handler
 			xmlreader.setContentHandler(theMarkerFeedHandler);
 			// get our data via the url class
 			InputSource is = new InputSource(url.openStream());
 			// perform the synchronous parse
 			xmlreader.parse(is);
 			// get the results - should be a fully populated RSSFeed instance,
 			// or null on error
 			return theMarkerFeedHandler.getFeed();
 		} catch (Exception ee) {
 			
 			//GENERIC EXCEPTION CATCH
 			ee.printStackTrace();
 			
 			Log.i("Refresh.getFeed()", "No Internet");
 			return null;
 			
 			////////////TEMP TEST CODE ////////////////
 /*
 			MarkerFeed tempFeed = new MarkerFeed();
 			MarkerInfo tempInfo = new MarkerInfo();
 			tempInfo.setClusterID("1");
 		    tempInfo.setLatitude("60.736893");
 		    tempInfo.setLongitude("-63.967462");
 		    
 		    tempInfo.setName("name");
 		    tempInfo.setDescription("description");
 		    tempInfo.setTitle("title");
 		    
 		    tempInfo.setGazID("2");
 		    tempInfo.setTopic("general_topics");
 		    tempInfo.setMarkup("markup");
 		    tempInfo.setSnippet("snippet");
 		    tempInfo.setKeyword("keyword");
 		    tempFeed.addItem(tempInfo);
 		    
 		    ////////////TEMP TEST CODE ////////////////
 			return tempFeed;
 */
 		}
 	}
 
 	public class RefreshTask extends AsyncTask<String, Integer, MarkerFeed> {
 
 		private int refresh_idx;
 
 		@Override
 		protected MarkerFeed doInBackground(String... string) {
 			mNumExecuting++;
 			mAjaxIdx++;
 			refresh_idx = mAjaxIdx;
 			return getMarkers();
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... progress) {
 			// setProgressPercent(progress[0]);
 		}
 
 		@Override
 		protected void onPostExecute(MarkerFeed feed) {
 			if (feed != null) {
 				if (refresh_idx > mShowIdx) {
 					mShowIdx = refresh_idx;
 					setMarkers(feed);
 				}
 				mNumExecuting--;
 			} else {
 				String errmsg = "Null marker feed...";
 				Toast.makeText(_ctx, "Unable to access internet", Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 
 	public class MarkerOverlayText extends Drawable{
 		private String _text;
 		private Paint _paint;
 
 		public MarkerOverlayText(String text){
 			_text = text;
 			_paint = new Paint();
 			_paint.setAlpha(255);
 			_paint.setTextSize(18);
 			_paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
 			_paint.setColor(Color.RED);
 			_paint.setTextAlign(Align.CENTER);
 			
 			this.setVisible(true, false);
 		}
 		
 		public int getColor(){
 			return _paint.getColor();
 		}
 		
 		public void setColor(int color){
 			_paint.setColor(color);
 		}
 
 		@Override
 		public void draw(Canvas canvas) {
 			canvas.drawText(_text, 0, 0, _paint); 
 		}
 
 		@Override
 		public int getOpacity() {
 			return _paint.getAlpha();
 		}
 
 		@Override
 		public void setAlpha(int alpha) {
 			_paint.setAlpha(alpha);
 		}
 
 		@Override
 		public void setColorFilter(ColorFilter cf) {
 			_paint.setColorFilter(cf);
 		}
 		
 		@Override
 		public boolean setVisible(boolean visible, boolean restart){
 			if(visible)
 				_paint.setAlpha(255);
 			else
 				_paint.setAlpha(0);
 			
 			if(restart)
 				this.invalidateSelf();
 			return true;
 		}
 	}
 }
