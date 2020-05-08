 package com.upopple.android.seethatmovie;
 
 import java.util.ArrayList;
 
 import org.json.JSONException;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.upopple.andoid.seethatmovie.R;
 import com.upopple.android.seethatmovie.web.RTMovieResult;
 import com.upopple.android.seethatmovie.web.RottenTomatoesAPI;
 
 public class MovieSearchResults extends ListActivity {
 	SearchAdapter searchAdapter;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		setContentView(R.layout.movie_search_results);
 		super.onCreate(savedInstanceState);
 		
 		Intent i = getIntent();
 		String search = i.getStringExtra("search");
 		searchAdapter = new SearchAdapter(this, search);
 		this.setListAdapter(searchAdapter);	
 	}
 	
 	private class SearchAdapter extends BaseAdapter{
 		private LayoutInflater li;
 		private ArrayList<RTMovieResult> movieResults;
 		
 		public SearchAdapter(Context context, String search){
 			li = LayoutInflater.from(context);
 			try {
 				movieResults = RottenTomatoesAPI.getMovieTitles(search);
 			} catch (JSONException e) {
 				Log.v("Unable to get search results", e.getMessage());
 			}
 		}
 		
 
 		public int getCount() {return movieResults.size();}
 		public RTMovieResult getItem(int i){return movieResults.get(i);}
 		public long getItemId(int i){return i;}
 		public View getView(int position, View convertView, ViewGroup parent) {
 			final ViewHolder holder;
 			View v = convertView;
 			if((v==null) || v.getTag() == null){
 				v = li.inflate(R.layout.movie_search_result, null);
 				holder = new ViewHolder();
 				holder.mTitle = (TextView)v.findViewById(R.id.movieSearchResult);
 				v.setTag(holder);
 			} else {
 				holder = (ViewHolder) v.getTag();
 			}
 			
 			holder.movie = getItem(position);
 			holder.mTitle.setText(holder.movie.getTitleYear());
 			
 			v.setTag(holder);
 			
 			return v;
 		}
 		
 		public class ViewHolder {
 			RTMovieResult movie;
 			TextView mTitle;
 		}
 	}
 	
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		
 		Intent i = new Intent(MovieSearchResults.this, AddMovie.class);
		i.putExtra("movieTitle", l.getItemAtPosition(position).toString());
 		startActivity(i);
 	}
 
 }
