 package fr.neamar.cinetime.fragments;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import fr.neamar.cinetime.MovieAdapter;
 import fr.neamar.cinetime.R;
 import fr.neamar.cinetime.api.APIHelper;
 import fr.neamar.cinetime.callbacks.TaskMoviesCallbacks;
 import fr.neamar.cinetime.objects.Movie;
 
 public class MoviesFragment extends ListFragment implements TaskMoviesCallbacks {
 
 	private static final String STATE_ACTIVATED_POSITION = "activated_position";
 	static public ArrayList<Movie> movies;
 
 	private Callbacks mCallbacks = sDummyCallbacks;
 	private int mActivatedPosition = ListView.INVALID_POSITION;
 	private LoadMoviesTask mTask;
	private boolean toFinish = false;
	private boolean dialogPending = false;
 	private ProgressDialog dialog;
 
 	public interface Callbacks {
 
 		public void onItemSelected(int position, Fragment source);
 
 		public void setFragment(Fragment fragment);
 		
 		public void setIsLoading(Boolean isLoading);
 	}
 
 	private static Callbacks sDummyCallbacks = new Callbacks() {
 		@Override
 		public void onItemSelected(int position, Fragment source) {
 		}
 
 		@Override
 		public void setFragment(Fragment fragment) {
 		}
 		
 		public void setIsLoading(Boolean isLoading)
 		{
 			
 		}
 	};
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		if (movies == null && mTask == null) {
 			String theaterCode = getActivity().getIntent().getStringExtra("code");
 			mTask = new LoadMoviesTask(this, theaterCode);
 			mTask.execute(theaterCode);
 		}
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
 			setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
 		}
 		return inflater.inflate(R.layout.fragment_movies, container, false);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.setRetainInstance(true);
 		if (movies == null && mTask == null) {
 			String theaterCode = getActivity().getIntent().getStringExtra("code");
 			mTask = new LoadMoviesTask(this, theaterCode);
 			mTask.execute(theaterCode);
 		}
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		if (!(activity instanceof Callbacks)) {
 			throw new IllegalStateException("Activity must implement fragment's callbacks.");
 		}
 		mCallbacks = (Callbacks) activity;
 		mCallbacks.setFragment(this);
 		if (toFinish) {
 			getActivity().finish();
 			toFinish = false;
 		}
 		if (dialogPending) {
 			dialog = new ProgressDialog(activity);
 			dialog.setMessage("Chargement des séances en cours...");
 			dialog.show();
 		}
 	}
 
 	@Override
 	public void onDetach() {
 		super.onDetach();
 		if (dialog != null) {
 			dialog.dismiss();
 			dialog = null;
 		}
 		mCallbacks = sDummyCallbacks;
 	}
 
 	@Override
 	public void onListItemClick(ListView listView, View view, int position, long id) {
 		super.onListItemClick(listView, view, position, id);
 		mCallbacks.onItemSelected(position, this);
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		if (mActivatedPosition != ListView.INVALID_POSITION) {
 			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
 		}
 	}
 
 	public void setActivateOnItemClick(boolean activateOnItemClick) {
 		getListView().setChoiceMode(
 				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
 	}
 
 	public void setActivatedPosition(int position) {
 		if (position == ListView.INVALID_POSITION) {
 			getListView().setItemChecked(mActivatedPosition, false);
 		} else {
 			getListView().setItemChecked(position, true);
 		}
 
 		mActivatedPosition = position;
 	}
 
 	public void finish() {
 		if (getActivity() != null) {
 			getActivity().finish();
 		} else {
 			toFinish = true;
 		}
 	}
 
 	private class LoadMoviesTask extends AsyncTask<String, Void, JSONArray> {
 		private MoviesFragment fragment;
 		private Context ctx;
 		private String theaterCode;
 		private Boolean remoteDataHasChangedFromLocalCache = true;
 
 		public LoadMoviesTask(MoviesFragment fragment, String theaterCode) {
 			super();
 			this.fragment = fragment;
 			this.ctx = fragment.getActivity();
 			this.theaterCode = theaterCode;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			String cache = ctx.getSharedPreferences("theater-cache", Context.MODE_PRIVATE).getString(theaterCode, "");
 			if(!cache.equals(""))
 			{
 				//Display cached values
 				try {
 					Log.e("wtf", "Read display datas from cache for " + theaterCode);
 					mCallbacks.setIsLoading(true);
 					ArrayList<Movie> movies = (new APIHelper(fragment)).formatMoviesList(new JSONArray(cache), theaterCode);
 					fragment.updateListView(movies);
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}
 			else
 			{
 				dialog = new ProgressDialog(ctx);
 				dialog.setMessage("Chargement des séances en cours...");
 				dialog.show();
 				dialogPending = true;
 			}
 		}
 
 		@Override
 		protected JSONArray doInBackground(String... queries) {
 			if(theaterCode != queries[0])
 			{
 				throw new RuntimeException("Fragment misuse: theaterCode differs");
 			}
 			JSONArray jsonResults = (new APIHelper(fragment)).downloadMoviesList(theaterCode);
 			
 			String oldCache = ctx.getSharedPreferences("theater-cache", Context.MODE_PRIVATE).getString(theaterCode, "");
 			String newCache = jsonResults.toString();
 			if(oldCache.equals(newCache))
 			{
 				Log.e("wtf", "Remote datas equals local datas; skipping UI update.");
 				remoteDataHasChangedFromLocalCache = false;
 			}
 			else
 			{
 				Log.e("wtf", "Remote data differs from local datas; updating UI");
 				//Store in cache for future use
 				SharedPreferences.Editor ed = ctx.getSharedPreferences("theater-cache", Context.MODE_PRIVATE).edit();
 				ed.putString(theaterCode, jsonResults.toString());
 				ed.commit();
 				remoteDataHasChangedFromLocalCache = true;
 			}
 			
 			
 			return jsonResults;
 		}
 
 		@Override
 		protected void onPostExecute(JSONArray jsonResults) {
 			mCallbacks.setIsLoading(false);
 			if (dialog != null) {
 				if (dialog.isShowing())
 					dialog.dismiss();
 			}
 			dialogPending = false;
 			
 			//Update only if data changed
 			if(remoteDataHasChangedFromLocalCache)
 			{
 				ArrayList<Movie> movies = (new APIHelper(fragment)).formatMoviesList(jsonResults, theaterCode);
 				fragment.updateListView(movies);
 			}
 		}
 	}
 
 	static public ArrayList<Movie> getMovies() {
 		return movies;
 	}
 
 	public void clear() {
 		movies.clear();
 		movies = null;
 	}
 
 	@Override
 	public void updateListView(ArrayList<Movie> movies) {
 		MoviesFragment.movies = movies;
		setListAdapter(new MovieAdapter(getActivity(), R.layout.listitem_theater, movies));
 		mTask = null;
 	}
 }
