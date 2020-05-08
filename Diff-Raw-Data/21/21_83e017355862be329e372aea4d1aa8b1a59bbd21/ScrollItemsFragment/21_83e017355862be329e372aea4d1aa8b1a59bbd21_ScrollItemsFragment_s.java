 package com.skybot.activities;
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.achartengine.GraphicalView;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.LinearLayout;
 
 import com.skybot.charts.CheckPageCoordinates;
 
 /**
  * Dediacted for scrolling Chart viewes from one to another
  * 
  * @author gor, armenabrahamyan
  * 
  */
 public class ScrollItemsFragment extends Fragment {
 	int mCurrentPage;
 	public ArrayList<HashMap<String, String>> data;
 	GraphicalView mChartView;
 	CheckPageCoordinates cpCoord= new CheckPageCoordinates();
 	FragmentActivity context;
 	
 	
 	
 	public ScrollItemsFragment() {
 		
 	}
 
 //public static ScrollItemsFragment newInstance(String content) {
 //	ScrollItemsFragment fragment = new ScrollItemsFragment();
 //
 //    return fragment;
 //}
 //	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		/** Getting the arguments to the Bundle object */
 		Bundle data = getArguments();
 
 		/** Getting integer data of the key current_page from the bundle */
 		mCurrentPage = data.getInt("current_page", 0);
 
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		
 		View v = inflater.inflate(R.layout.scrolling_fragment_layout,
 				container, false);
 		
 		LinearLayout ll = (LinearLayout) v.findViewById(R.id.chartLayout);
 		context = getActivity();
 		mChartView = (GraphicalView) cpCoord.getChart(context, mCurrentPage,data);
 		ll.addView(mChartView, new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
 				
 		return ll;
 
 	}
 	
 	@Override
 	public void onResume(){ 
 		
 		Log.i("notification", "onResume()");
 		
 		super.onResume();
 		context = getActivity();
 		LinearLayout ll = (LinearLayout) context.findViewById(R.id.chartLayout);
 		if(mChartView == null) {
 			
 			mChartView = (GraphicalView) cpCoord.getChart(context,mCurrentPage,data);
 			ll.addView(mChartView, new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
 			Log.i("ChartView null Event", "Creating view");
 		}
 		else {
 			mChartView.repaint();
 			Log.i("else", "repaint()");
 		}
 		
 	}
 	
 	
 	
 }
