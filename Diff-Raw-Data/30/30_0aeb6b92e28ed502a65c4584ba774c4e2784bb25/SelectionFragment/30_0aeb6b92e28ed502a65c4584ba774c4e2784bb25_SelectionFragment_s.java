 package fr.eyal.datalib.sample.netflix.fragment;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 
import fr.eyal.datalib.sample.netflix.data.model.movieimage.MovieImage;
import fr.eyal.datalib.sample.netflix.data.model.top100.ItemTop100;
import fr.eyal.datalib.sample.netflix.data.model.top100.Top100;
import fr.eyal.datalib.sample.netflix.data.service.NetflixDataManager;
import fr.eyal.datalib.sample.netflix.data.service.NetflixService;
import fr.eyal.datalib.sample.netflix.ui.MovieItemHolder;
import fr.eyal.lib.data.model.ResponseBusinessObjectDAO;
import fr.eyal.lib.data.service.DataManager;
import fr.eyal.lib.data.service.model.BusinessResponse;
import fr.eyal.lib.data.service.model.DataLibRequest;
import fr.eyal.datalib.sample.netflix.R;
 import android.content.Context;
 import android.graphics.Point;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
import android.widget.GridLayout;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 public class SelectionFragment extends NetflixFragment {
 
 	public static final String TAG = SelectionFragment.class.getSimpleName(); 
 	
 	NetflixDataManager mDataManager;
 	
 	public static final int DEFAULT_SCREEN_WIDTH = 360;
 	public static final int DEFAULT_WIDTH = 170;
 	public static final int DEFAULT_HEIGHT = 80;
 
 	
 	Point mPoint = new Point();
 	DisplayMetrics mMetrics = new DisplayMetrics();
 
 	ArrayList<MovieItemHolder> mMovies = new ArrayList<MovieItemHolder>();	
 	ImageView mNetflixItem;
 
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		
 		mDataManager = NetflixDataManager.getInstance(getActivity());
 		
 		WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
 		Display display = wm.getDefaultDisplay();
 		display.getSize(mPoint);
 		display.getMetrics(mMetrics);
 		
 		super.onCreate(savedInstanceState);
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		
 		ScrollView contentView = (ScrollView) inflater.inflate(R.layout.fgmt_selection, container, true); //on met true si le fragment est dans un merge du cote du layout main.xml
 		
 		GridLayout selectionLayout = (GridLayout) contentView.findViewById(R.id.selectionLayout);
 		
 		int size = selectionLayout.getChildCount();
 		for (int i = 0; i < size; i++) {
 			View v = selectionLayout.getChildAt(i);
 			if(v instanceof RelativeLayout){
 				MovieItemHolder holder = new MovieItemHolder();
 				RelativeLayout layout = (RelativeLayout) v;
 				holder.image = (ImageView) layout.getChildAt(0);
 				holder.text = (TextView) layout.getChildAt(1);
 				mMovies.add(holder);
 			}
 		}
 		
 		try {
 			int requestId = mDataManager.getTop100(DataManager.TYPE_NETWORK, this, DataLibRequest.OPTION_NO_OPTION);
 			mRequestIds.add(requestId);
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		
 		return contentView;
 	}
 
 	@Override
 	public void onCacheRequestFinished(ResponseBusinessObjectDAO response) {
 		
 	}
 
 	@Override
 	public void onDataFromDatabase(int code, ArrayList<?> data) {
 		
 	}
 
 	@Override
 	public void onRequestFinished(int requestId, boolean suceed, BusinessResponse response) {
 		
 		switch (response.webserviceType) {
 		
 		case NetflixService.WEBSERVICE_TOP100:
 			
 			Top100 top100 = (Top100) response.response;
 			ArrayList<ItemTop100> movies = top100.itemTop100;
 			
 			try {
 				
 			int size = mMovies.size();
 			for (int i = 0; i < size; i++) {
 				MovieItemHolder m = mMovies.get(i);
 				m.text.setText(movies.get(i).title);
 				Log.d(TAG, movies.get(i).getImageUrl());
 
 				//we ask for movie's picture
 				//the movie item holder will handle the bitmap received 
 				mDataManager.getMovieImage(mMovies.get(i), movies.get(i).getImageUrl(), DataLibRequest.OPTION_NO_OPTION);
 				
 				}
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 			
 			break;
 
 			
 //		case NetflixService.WEBSERVICE_MOVIEIMAGE:
 //			
 //			MovieImage img = (MovieImage) response.response;
 //			mMovies.get(0).image.setImageBitmap(img.image);
 //			
 //			break;
 
 		default:
 			break;
 		}
 		
 	}
 }
