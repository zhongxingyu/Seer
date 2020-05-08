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
 import android.widget.TextView;
 
 
 public class SelectedFrag extends Fragment {
 	
 //building the interface to implement in activity (SecondView)
 private SecondInterface secondlistener;
 	public interface SecondInterface
 	{
 		public void onMakeCall();
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreateView(inflater, container, savedInstanceState);
 		
		//relating to the linear layout
 		LinearLayout view = (LinearLayout) inflater.inflate(R.layout.selectedview, container, false);
 		
 		//Getting id for TextView to display full name
 		TextView fullName = (TextView) getActivity().findViewById(R.id.full_name);
 		
 //Getting id for TextView to display Phone Number
 		final TextView phone = (TextView) getActivity().findViewById(R.id.phone_digits);
 		
 //creating the button
 		Button callButt = (Button)getActivity().findViewById(R.id.call_button);
 		
 //setting first and last name in one text view
 		fullName.setText(getActivity().getIntent().getExtras().getString("list_name")); //passed key is in quotes
 //setting phone number
 		phone.setText(getActivity().getIntent().getExtras().getString("phoneText"));   //passed key is in quotes
 		
 		
 		
 		callButt.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				
 				secondlistener.onMakeCall();
 			}
 		});
 		
 		return view;	
 		
 	}
 	
 	 @Override
 		public void onAttach(Activity activity)
 		{
 			super.onAttach(activity);
 			
 			try{
 				secondlistener = (SecondInterface) activity;
 			}
 			catch(ClassCastException e)
 			{
				throw new ClassCastException(activity.toString() + "must implement SecondInterface");
 			}
 		}
 }
