 package ua.maker.gbible.fragment;
 
 import ua.maker.gbible.R;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 public class PlanFragment extends SherlockFragment {
 	
 	private static final String TAG = "PlanFragment";
 	
 	private View view = null;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		Log.d(TAG, "onCreateView()");
 		view = inflater.inflate(R.layout.activity_plan_layout, null);
 		
 		return view;
 	}

 }
