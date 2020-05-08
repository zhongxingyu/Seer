 package se.goagubbar.twee;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import se.goagubbar.twee.Models.Episode;
 import se.goagubbar.twee.Models.ExtendedSeries;
 import se.goagubbar.twee.Models.Series;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Bitmap;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Adapters {
 
 	public static Map<String, Bitmap> cache;
 
 	public static class SeriesAdapter extends ArrayAdapter<ExtendedSeries> {
 
 		static class viewHolder
 		{
 			ImageView image;
 			TextView information;
 			String seriesId;
 			String season;
 			ProgressBar progress;
 			TextView txtSmallView;
 		}
 
 		private final Context context;
 		private final ArrayList<ExtendedSeries> series;
		private DateHelper dateHelper;
		private final DatabaseHandler db;
 		Object mActionMode;
 		int resource;
 
 		public SeriesAdapter(Context context, int resource, ListView lv, ArrayList<ExtendedSeries> objects)
 		{
 			super(context, resource, objects);
 			this.context = context;
 			this.series = objects;
 			this.resource = resource;
			db = new DatabaseHandler(context);
			dateHelper = new DateHelper();	
 
 			cache = new HashMap<String, Bitmap>();
 		}
 
 
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent)
 		{
 
 			viewHolder holder;
 			ExtendedSeries s = series.get(position);
 
 			if(convertView == null)
 			{
 				convertView = View.inflate(context, resource, null);
 				holder = new viewHolder();
 
 				holder.image = (ImageView)convertView.findViewById(R.id.imgSeriesImage);
 				holder.information = (TextView)convertView.findViewById(R.id.txtUpcomingEpisode);
 				holder.progress = (ProgressBar)convertView.findViewById(R.id.pgrWatched);						
 
 				convertView.setTag(holder);
 			}
 			else
 			{
 				holder = (viewHolder)convertView.getTag();
 			}
 
 
 			if(s != null)
 			{
 				holder.seriesId = s.getImage();
 
 
 				convertView.setTag(R.string.homeactivity_tag_id,s.getID());
 				convertView.setTag(R.string.homeactivity_tag_seriesid,s.getSeriesId());
 
 				holder.progress.setMax(s.getTotalEpisodes());
 				holder.progress.setProgress(s.getWatchedEpisodes());
 				holder.image.setImageBitmap(getBitmapFromCache(s.getImage()));
 				holder.information.setText(s.getNextEpisodeInformation().equals("") ? context.getText(R.string.message_show_ended) : s.getNextEpisodeInformation());		
 
 			}
 
 
 			return convertView;
 
 		}
 
 
 		private Bitmap getBitmapFromCache(String imageName) {  
 			if (cache.containsKey(imageName)) {  
 				return cache.get(imageName);  
 			}  
 			else
 			{
 				Bitmap bm = new ImageService().GetImage(imageName, context); 
 				cache.put(imageName, bm);
 				return bm;
 			}
 
 		}
 	}
 
 	public static class EpisodeAdapter extends ArrayAdapter<Episode> {
 
 		private final Context context;
 		private final ArrayList<Episode> episodes;
 		private DateHelper dateHelper;
 		private ArrayList<Boolean> itemChecked = new ArrayList<Boolean>();
 		private ArrayList<Boolean> visibleItems = new ArrayList<Boolean>();
 		private ArrayList<Boolean> showSeasonBanner = new ArrayList<Boolean>();
 		private String season;
 
 		public EpisodeAdapter(Context context, int resource, ListView lv, ArrayList<Episode> objects)
 		{
 			super(context, R.layout.listitem_allepisodes, objects);
 			this.context = context;
 			this.episodes = objects;
 			this.dateHelper = new DateHelper();
 			this.season = "0";
 
 			for (int i = 0; i < objects.size(); i++) {
 				itemChecked.add(i, objects.get(i).getWatched().equals("1"));
 				visibleItems.add(i, dateHelper.CompareDates(episodes.get(i).getAired(), dateHelper.GetTodaysDate()) >= 0);
 				showSeasonBanner.add(i,objects.get(i).getSeason().equals(this.season));
 
 				this.season = objects.get(i).getSeason();		
 			}	
 
 
 		}
 
 		static class ViewHolder {
 			protected TextView title;
 			protected TextView information;
 			protected CheckBox watched;
 			protected LinearLayout seasonWrapper;
 			protected TextView seasonNumber;
 		}
 
 		@Override
 		public View getView(final int pos, View convertView, ViewGroup parent)
 		{
 
 			if(convertView == null){
 				LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				convertView = inflator.inflate(R.layout.listitem_allepisodes, null);
 			}
 
 			final CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.chkWatched);
 			final TextView seasonCheckBox = (TextView)convertView.findViewById(R.id.txtMarkSeasonAsWatched);
 			final TextView title = (TextView) convertView.findViewById(R.id.txtTitle);
 			final TextView information = (TextView) convertView.findViewById(R.id.txtEpisodeNumber);
 			final TextView seasonNumber = (TextView) convertView.findViewById(R.id.txtSeasonNumber);
 			final RelativeLayout seasonWrapper = (RelativeLayout) convertView.findViewById(R.id.seasonTest);
 
 			checkBox.setOnClickListener(new OnClickListener() {
 
 				public void onClick(View v) {
 					CheckBox cb = (CheckBox) v.findViewById(R.id.chkWatched);
 					new DatabaseHandler(context).ToggleEpisodeWatched("" + episodes.get(pos).getID(), cb.isChecked());
 
 					RefreshOverView();			
 
 					if (cb.isChecked()) {
 						itemChecked.set(pos, true);
 						// do some operations here
 					} else if (!cb.isChecked()) {
 						itemChecked.set(pos, false);
 						// do some operations here
 					}
 
 				}
 			});
 
 
 			seasonCheckBox.setOnClickListener(new OnClickListener() {
 
 				public void onClick(View v) {
 					MarkSeasonAsWatched(episodes.get(pos).getSeriesId(), episodes.get(pos).getSeason());
 
 				}
 			});
 
 			convertView.setOnClickListener(new OnClickListener() {
 
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					new SeriesHelper().displayPlot(episodes.get(pos), getContext());
 				}
 			});
 
 			checkBox.setChecked(itemChecked.get(pos));
 
 			if(visibleItems.get(pos))
 			{
 				checkBox.setVisibility(8);
 			}
 			else
 			{
 				checkBox.setVisibility(View.VISIBLE);
 			}
 
 
 			if(!showSeasonBanner.get(pos))
 			{
 				seasonWrapper.setVisibility(View.VISIBLE);
 			}
 			else
 			{
 				seasonWrapper.setVisibility(View.GONE);
 			}
 
 
 			seasonNumber.setText("Season " + episodes.get(pos).getSeason());
 
 			String _title = episodes.get(pos).getTitle();
 			title.setText(_title.equals("") ? "TBA" : _title);
 			information.setText(dateHelper.Episodenumber(episodes.get(pos)) + " | " + dateHelper.DisplayDate(episodes.get(pos).getAired()));
 
 			return convertView;
 
 
 		}
 
 		private void RefreshOverView()
 		{
 			OverviewActivity overViewFragment = (OverviewActivity)context;
 			overViewFragment.Refresh();
 		}
 
 		private void MarkSeasonAsWatched(String seriesId, String season)
 		{
 			new DatabaseHandler(context).ToggleSeasonWatched("" + seriesId, season, true);
 			Toast.makeText(context, R.string.message_season_watched, Toast.LENGTH_SHORT).show();
 
 			for (int i = 0; i < episodes.size(); i++) {
 
 				if(episodes.get(i).getSeason().equals(season))
 				{
 					itemChecked.set(i, true);
 				}
 			}
 			notifyDataSetChanged();
 			RefreshOverView();
 
 		}
 
 	}
 
 	public static class UpcomingEpisodesAdapter extends ArrayAdapter<Episode> {
 
 		private final Context context;
 		private final ArrayList<Episode> episodes;
 		private DateHelper dateHelper;
 
 		public UpcomingEpisodesAdapter(Context context, int resource, ListView lv, ArrayList<Episode> objects)
 		{
 			super(context, R.layout.listitem_episode, objects);
 			this.context = context;
 			this.episodes = objects;
 			this.dateHelper = new DateHelper();		
 
 		}
 
 		static class ViewHolder {
 			protected TextView title;
 			protected TextView information;
 			protected CheckBox watched;
 		}
 
 		@Override
 		public View getView(final int pos, View convertView, ViewGroup parent)
 		{
 
 			if(convertView == null){
 				LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				convertView = inflator.inflate(R.layout.listitem_episode, null);
 			}
 
 			final TextView title = (TextView) convertView.findViewById(R.id.txtTitle);
 			final TextView information = (TextView) convertView.findViewById(R.id.txtEpisodeNumber);
 			final CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.chkWatched);
 			checkBox.setVisibility(View.GONE);
 			convertView.setOnClickListener(new OnClickListener() {
 
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					new SeriesHelper().displayPlot(episodes.get(pos), getContext());
 				}
 			});
 
 
 			String _title = episodes.get(pos).getTitle(); 
 			title.setText(_title.equals("") ? "TBA" : _title);
 			information.setText(dateHelper.Episodenumber(episodes.get(pos)) + " | " + dateHelper.DisplayDate(episodes.get(pos).getAired()));
 
 			return convertView;
 
 
 		}
 	}
 
 	public static class CalendarAdapter extends ArrayAdapter<Episode> {
 
 		private final Context context;
 		private final ArrayList<Episode> episodes;
 		private DateHelper dateHelper;
 		private ImageService imageService;
 		private ArrayList<Boolean> displayDateWrapper = new ArrayList<Boolean>();
 		//private ArrayList<Boolean> itemChecked = new ArrayList<Boolean>();
 		//private ArrayList<Boolean> visibleItems = new ArrayList<Boolean>();
 
 		public CalendarAdapter(Context context, int resource, ListView lv, ArrayList<Episode> objects)
 		{
 			super(context, R.layout.listitem_calendar, objects);
 			this.context = context;
 			this.episodes = objects;
 			this.dateHelper = new DateHelper();
 			this.imageService = new ImageService();
 
 			String lastDate = "";
 			for (int i = 0; i < objects.size(); i++) {
 				displayDateWrapper.add(objects.get(i).getAired().equals(lastDate));
 				lastDate = objects.get(i).getAired();
 				//itemChecked.add(i, objects.get(i).getWatched().equals("1"));
 			}
 			//
 			//			for (int i = 0; i < objects.size(); i++) {
 			//				visibleItems.add(i, dateHelper.CompareDates(episodes.get(i).getAired(), dateHelper.GetTodaysDate()) > 0);
 			//			}
 
 		}
 
 		static class ViewHolder {
 			protected TextView date;
 			//protected TextView information;
 			//protected CheckBox watched;
 			//protected TextView extra;
 			protected ImageView image;
 			protected TextView dateSeparator;
 			protected TextView episodeInformation;
 			protected RelativeLayout dateSeparatorWrapper;
 			protected RelativeLayout layoutEpisode;
 		}
 
 		@Override
 		public View getView(final int pos, View convertView, ViewGroup parent)
 		{
 
 			if(convertView == null){
 				LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				convertView = inflator.inflate(R.layout.listitem_calendar, null);
 			}
 
 			//final CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.chkWatched);
 			//final TextView title = (TextView) convertView.findViewById(R.id.txtTitle);
 			final ImageView image = (ImageView) convertView.findViewById(R.id.imgSeriesImage);
 			//final TextView information = (TextView) convertView.findViewById(R.id.txtEpisodeNumber);
 			final TextView episodeInformation = (TextView)convertView.findViewById(R.id.txtEpisodeInformation);
 			//final TextView test = (TextView)convertView.findViewById(R.id.txtTest);
 			final TextView dateSeparator = (TextView)convertView.findViewById(R.id.txtDate);
 			final RelativeLayout dateSeparatorWrapper = (RelativeLayout)convertView.findViewById(R.id.daySeparator);
 			final RelativeLayout episodeImage = (RelativeLayout)convertView.findViewById(R.id.layoutEpisode);
 			//checkBox.setOnClickListener(new OnClickListener() {
 
 			//				public void onClick(View v) {
 			//					CheckBox cb = (CheckBox) v.findViewById(R.id.chkWatched);
 			//					new DatabaseHandler(context).ToggleEpisodeWatched("" + episodes.get(pos).getID(), cb.isChecked());
 			//
 			//					if (cb.isChecked()) {
 			//						itemChecked.set(pos, true);
 			//						// do some operations here
 			//					} else if (!cb.isChecked()) {
 			//						itemChecked.set(pos, false);
 			//						// do some operations here
 			//					}
 			//
 			//				}
 			//			});
 
 
 			episodeImage.setOnClickListener(new OnClickListener() {
 
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					new SeriesHelper().displayPlot(episodes.get(pos), getContext());
 				}
 			});
 
 			//checkBox.setChecked(itemChecked.get(pos));
 			//			if(visibleItems.get(pos))
 			//			{
 			//				checkBox.setVisibility(8);
 			//			}
 			//			else
 			//			{
 			//				checkBox.setVisibility(View.VISIBLE);
 			//			}
 			//date.setText(episodes.get(pos).getSeriesId());
 
 			Bitmap bm = imageService.GetImage(episodes.get(pos).getSeriesId(), getContext());
 			if(bm != null){
 				image.setImageBitmap(bm);
 			}
 			else
 			{
 				image.setVisibility(View.INVISIBLE);
 			}
 
 			dateSeparator.setText(dateHelper.DaysTilNextEpisode(episodes.get(pos).getAired()) + " (" + dateHelper.DisplayDate(episodes.get(pos).getAired()).substring(4) + ")");
 			episodeInformation.setText( dateHelper.Episodenumber(episodes.get(pos)) + " | " + episodes.get(pos).getTitle());
 
 			if(displayDateWrapper.get(pos))
 			{
 				dateSeparatorWrapper.setVisibility(View.GONE);
 			}
 			//test.setText(test.getText() + dateHelper.Episodenumber(episodes.get(i)) + " | " + episodes.get(i).getTitle() + " | " +dateHelper.DisplayDate(episodes.get(i).getAired()) + "\n");
 
 
 			//extra.setVisibility(View.VISIBLE);
 			//title.setText(episodes.get(pos).getTitle());
 			//information.setText(dateHelper.Episodenumber(episodes.get(pos)) + " | " +dateHelper.DisplayDate(episodes.get(pos).getAired()));
 
 			return convertView;
 
 
 		}
 	}
 
 	public static class SearchAdapter extends ArrayAdapter<Series>
 	{
 
 		private final Context context;
 		private final ArrayList<Series> series;
 
 
 
 
 		public SearchAdapter(Context context, int resource, ArrayList<Series> objects) {
 			super(context, R.layout.listitem_searchresult, objects);
 			this.context = context;
 			this.series = objects;
 		}
 
 
 
 		@Override
 		public View getView(int position, View ConvertView, ViewGroup parent){
 			LayoutInflater inflater  = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			final View rowView = inflater.inflate(R.layout.listitem_searchresult, parent, false);
 
 			TextView name = (TextView)rowView.findViewById(R.id.txtName);
 			TextView year = (TextView)rowView.findViewById(R.id.txtYear);
 			final TextView btnPlot = (TextView)rowView.findViewById(R.id.txtSeriesPlot);
 
 			rowView.setTag(series.get(position).getID());
 			btnPlot.setTag(series.get(position).getSummary());
 			name.setText(series.get(position).getName());
 			year.setText("First aired: " + series.get(position).getAirs());
 
 			btnPlot.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 
 					AlertDialog.Builder builder = new AlertDialog.Builder(context);
 					builder.setMessage(btnPlot.getTag().toString())
 					.setCancelable(false)
 					.setTitle(R.string.series_summary)
 					.setPositiveButton(R.string.about_close, new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							dialog.cancel();
 						}
 					}).create().show();
 
 				}
 			});
 
 			return rowView;
 		}
 
 	}
 
 }
