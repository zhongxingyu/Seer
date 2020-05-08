 package com.lpuente.java2_w3;
 
 
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 
 
 public class FirstFrag extends Fragment {
 	
 	private FirstInterface listener;
 	
 	
 	//Building the interface to implement in MainActivity
 		public interface FirstInterface
 		{
 			public void onAdd();
 		}
 		
 
 		 @Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			// TODO Auto-generated method stub
 			super.onCreateView(inflater, container, savedInstanceState);
 			
 		//relating to the main.xml which is a relative layout. hopefully no issues.
 			LinearLayout view = (LinearLayout) inflater.inflate(R.layout.activity_main, container, false);
 			
 
 			
 			//creating button using reference from xml id
 		        Button addButt = (Button) view.findViewById(R.id.addButt);
 		        
 		    //on click listener for button
 		        addButt.setOnClickListener(new OnClickListener() {
 					
 					@Override
 					public void onClick(View v) {
 						
 					     listener.onAdd();
 					    
 					}
 				});
 		        return view;
 			};
 			@Override
 			public void onAttach(Activity activity)
 			{
 				super.onAttach(activity);
 				
 				try{
 					listener = (FirstInterface) activity;
 				}
 				catch(ClassCastException e)
 				{
 					throw new ClassCastException(activity.toString() + "must implement FirstInterface");
 				}
 			}
 		 
 }
