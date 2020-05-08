 package ltg.heliotablet_android.view.theory;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import ltg.heliotablet_android.ActivityCommunicator;
 import ltg.heliotablet_android.FragmentCommunicator;
 import ltg.heliotablet_android.MainActivity;
 import ltg.heliotablet_android.R;
 import ltg.heliotablet_android.data.ReasonDBOpenHelper;
 import ltg.heliotablet_android.view.controller.TheoryReasonController;
 import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.FrameLayout;
 
 public class TheoryFragmentWithSQLiteLoaderNestFragments extends Fragment implements FragmentCommunicator {
 
 	private TheoryReasonController theoryController;
 	private ViewGroup theoriesView;
 	private ReasonDBOpenHelper db = null;
 	private ViewGroup planetColorsView = null;
 	private Map<String, Fragment> fragments = new HashMap<String, Fragment>(); 
 	private FragmentCommunicator activityCommunicator;
 	private Map<String, View> unUsedPlanetColors = new HashMap<String, View>();
 	private Map<String, View> usedPlanetColors = new HashMap<String, View>();
 	
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		theoriesView = (ViewGroup) inflater.inflate(
 				R.layout.theories_activity_nested, container, false);
 		planetColorsView = (ViewGroup) theoriesView.findViewById(R.id.colors_include);
 		
 		
 		
 		 db= ReasonDBOpenHelper.getInstance(this.getActivity());
 //		  StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll()
 //                  .penaltyLog()
 //                  .build());
 //		  StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects()
 //          .detectLeakedClosableObjects()
 //          .penaltyLog()
 //          .penaltyDeath()
 //          .build());
 		
 		theoryController = TheoryReasonController.getInstance(getActivity());
 
 		setupListeners();
 		
 		//getData(ReasonContentProvider.CONTENT_URI);
 		
 		return theoriesView;
 	}
 
 	/**
 	 * Setups the listeners
 	 */
 	private void setupListeners() {
 		// add listeners to all the draggable planetViews
 		// get all the colors and the
 
 		ViewGroup planetColorsView = (FrameLayout) theoriesView
 				.findViewById(R.id.colors_include);
 		// get all the colors and the
 		int childCount = planetColorsView.getChildCount();
 		for (int i = 0; i < childCount; i++) {
 			CircleView color = (CircleView) planetColorsView.getChildAt(i);
 			color.setOnTouchListener(new CircleViewDefaultTouchListener());
 			unUsedPlanetColors.put(color.getFlag(), color);
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 	}
 	
 	
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		db.close();
 	}
 	
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		activityCommunicator =(FragmentCommunicator)activity;
 		((MainActivity)activity).fragmentCommunicator = this;
 	}
 
 	@Override
 	public void addUsedPlanetColors(CircleView someView) {
 		usedPlanetColors.put(someView.getFlag(), someView);
 		
 	}
 
 	@Override
 	public void showPlanetColor(String color) {
 		
 		View view = usedPlanetColors.get(color);
 		if( view != null ) {
 			planetColorsView.addView(view);
 			planetColorsView.invalidate();
 		}
 		
 	}
 
 	public void addUsedPlanetColor(String color) {
 		if( usedPlanetColors.containsValue(color) == false ) {
 			View view = unUsedPlanetColors.get(color);
 			planetColorsView.removeView(view);
 			planetColorsView.invalidate();
 			usedPlanetColors.put(color, view);
 		}
 		
 	}
 }
