 package se.goagubbar.twee.fragments;
 
 import java.util.ArrayList;
 
 import se.goagubbar.twee.R;
 import se.goagubbar.twee.adapters.EpisodeAdapter;
 import se.goagubbar.twee.models.Episode;
 import se.goagubbar.twee.utils.DatabaseHandler;
 import se.goagubbar.twee.utils.DateHelper;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class Episodes extends Fragment{
 	String showId;
 	DateHelper dateHelper;
 	TextView markSeasonAsWatched;
 	ListView episodes;
 	static EpisodeAdapter allEpisodes;
 	
 	
 	
 	public Episodes(String showId, int totalEpisodes, int watchedEpisodes){
 		this.showId = showId;
 		this.dateHelper = new DateHelper();
 	}
 	
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
     	View v = inflater.inflate(R.layout.view_episodes, container, false);
     	 episodes = (ListView)v.findViewById(R.id.lstAllEpisodes);        		
     	
     	new GetEpisodesTask().execute(getActivity());
     	
     	TextView t = (TextView) episodes.findViewById(R.id.txtMarkSeasonAsWatched);      	
         return v;
     }
 
 	@Override
 	public void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 	}
     
     
     public class GetEpisodesTask extends AsyncTask<Context, Void, ArrayList<Episode>>{
 
 		@Override
 		protected void onPostExecute(ArrayList<Episode> result) {
 			super.onPostExecute(result);
 			
 			allEpisodes = new EpisodeAdapter(getActivity(), R.layout.listitem_allepisodes, episodes, result);
 			episodes.setAdapter(allEpisodes);
 		}
 
 		@Override
 		protected ArrayList<Episode> doInBackground(Context... params) {
 			// TODO Auto-generated method stub
 			ArrayList<Episode> episodes = new DatabaseHandler(getActivity()).GetEpisodes(showId);
 			return episodes;
 		}
 
     }
 	
 	
     public static void MarkAllEpisodes()
     {
     	EpisodeAdapter.MarkShowAsWatched();
     	allEpisodes.notifyDataSetChanged();
     }
     
    public static void MarkLastAiredEpisodesAsWatched()
    {
    	allEpisodes.notifyDataSetChanged();
    }
    
 }
