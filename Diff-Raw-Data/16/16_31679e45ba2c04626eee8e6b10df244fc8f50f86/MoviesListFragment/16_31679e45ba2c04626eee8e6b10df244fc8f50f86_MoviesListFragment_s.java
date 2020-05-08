 package com.ricex.rpi.remote.android;
 
 import android.app.Fragment;
 import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 
 import com.ricex.rpi.common.video.Directory;
 import com.ricex.rpi.common.video.Movie;
 
 /** The fragment for displaying the movies list
  * 
  * @author Mitchell
  *
  */
 
 public class MoviesListFragment extends Fragment {
 
 	/** The list view that will display the movies in the current directory */
 	private ListView moviesListView;
 	
 	/** The linear layout that will display the navigation buttons */
 	private LinearLayout moviesDirectoryView;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
 		View view = inflater.inflate(R.layout.movies_layout, container, false);
 		
 		moviesListView = (ListView) view.findViewById(R.id.movies_list);
 		
 		VideoAdapter adapter = generateVideoAdapter(view.getContext());
 
 		Log.i("RPI", "Adapter: " + adapter);
 		
		moviesListView.setAdapter(adapter);	
 		
 		return view;
 		
 	}
 	
 	private VideoAdapter generateVideoAdapter(Context context) {
 		VideoAdapter adapter = new VideoAdapter(context);
 		
 		for (int i=0;i<10;i++) {
 			if (i % 2 == 0) {
 				adapter.addItem(new Movie("Movie: " + i/2, ""));
 			}
 			if (i % 2 == 0) {
 				adapter.addItem(new Directory("Directory: " + (i/2+ 1)));
 			}
 		}
 		
 		return adapter;
 	}
 }
