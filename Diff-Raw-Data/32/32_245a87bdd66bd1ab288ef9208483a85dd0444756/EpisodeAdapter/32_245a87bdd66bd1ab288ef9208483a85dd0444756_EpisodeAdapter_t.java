 package se.goagubbar.twee.adapters;
 
 import java.util.ArrayList;
 
 import se.goagubbar.twee.OverviewActivity;
 import se.goagubbar.twee.R;
 import se.goagubbar.twee.SeriesHelper;
 import se.goagubbar.twee.fragments.Overview;
 import se.goagubbar.twee.models.Episode;
 import se.goagubbar.twee.utils.DatabaseHandler;
 import se.goagubbar.twee.utils.DateHelper;
 import android.R.bool;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class EpisodeAdapter extends ArrayAdapter<Episode> {
 
 	private final Context context;
 	private static ArrayList<Episode> episodes;
 	private DateHelper dateHelper;
 	private static ArrayList<Boolean> itemChecked = new ArrayList<Boolean>();
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
 				OverviewActivity.Refresh();
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
 	
 	
 	public static void MarkShowAsWatched()
 	{
 		for (int i = 0; i < episodes.size(); i++) {
 				itemChecked.set(i, true);
 		}
 	}
 	
	public static void MarkLastAiredEpisodesAsWatched(Episode lastEpisode, boolean isChecked)
	{
		for (int i = 0; i < episodes.size(); i++) {
			Episode current = episodes.get(i);
			if(current.getSeason().equals(lastEpisode.getSeason()) && current.getEpisode().equals(lastEpisode.getEpisode()))
			{
				itemChecked.set(i, isChecked);
				break;
			}
			
		}
	}
	
 }
