 /*
  * Crystal McDonald
  * Java II
  * 1309
  * Week 4
  * TideFragment: the graphic interface (left fragment)
  */
 package com.cm.clamdigger4;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class TideFragment extends Fragment {
 	
 	private TideDataListener listener;
 	
 	public interface TideDataListener{
 		public String onClammerTipsRequest();
 		public void onLaunchImplicitIntent();
 		public void onLaunchSecondActivity();
 	}
 	
 	public static final String TAG = "TIDE FRAGMENT";
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
 		
 		//create a view by overriding superclass
 		super.onCreateView(inflater, container, savedInstanceState);
 		//inflate view into current context...goes in tide.xml file and make into usable view
		LinearLayout view = (LinearLayout) inflater.inflate(R.layout.tide, container, false);
 		
 		Log.i(TAG, "onCreateView()");
 		Toast.makeText(view.getContext(), "TIDE FRAGMENT", Toast.LENGTH_SHORT).show();
 		
 		//Add UI from TideActivity
 		Button infoButton, tipsButton, browse;
 		final TextView clammersTip;
 		
 		infoButton = (Button) view.findViewById(R.id.infoButton);
 		tipsButton = (Button) view.findViewById(R.id.tButton);
 		browse = (Button) view.findViewById(R.id.browse);
 		clammersTip = (TextView) view.findViewById(R.id.clamTips);
 		
 		/*
 		 * Intent: Launches Second Activity
 		 */
 		infoButton.setOnClickListener(new OnClickListener(){//setup onclick Listener
 			
 			@Override
 			public void onClick(View v){
 				//when button is clicked it will launch second activity
 				listener.onLaunchSecondActivity();
 			}
 		});
 		
 		/*
 		 * Clamming Tips: Randomly selects tips on clamming.
 		 */
 		tipsButton.setOnClickListener(new OnClickListener(){
 			
 			@Override
 			public void onClick(View arg2){
 			
 				Toast.makeText(getActivity(), TAG, Toast.LENGTH_SHORT).show();
 				String tip = listener.onClammerTipsRequest();
 				
 				//update the label with dynamic answer
 				clammersTip.setText(tip);
 			}
 		});
 		
 		/*
 		 * Implicit Intent: Launches Web browsers to open website.
 		 */
 		browse.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View interwebs) {
 				Log.i(TAG, "browse onClick()");
 				Toast.makeText(getActivity(), TAG, Toast.LENGTH_SHORT).show();
 				listener.onLaunchImplicitIntent();
 			}
 		});
 		return view;
 }
 	/*
 	 * Implement onAttach()
 	 * call the super
 	 * implement try catch block with a ClassCastException
 	 * if the interface is not implemented
 	 */
 	@Override
 	public void onAttach(Activity activity){
 		super.onAttach(activity);
 		try{
 			listener = (TideDataListener) activity;
 		}catch (ClassCastException e){
 			throw new ClassCastException(activity.toString()+ "must implement the Interface TideDataListener");
 		}
 	}
 }//end
