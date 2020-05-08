 package com.example.android.navigationdrawerexample;
 
 
 import android.app.Fragment;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 
 
 public class ContactUsFragment extends Fragment implements OnClickListener
 {
    
 
   @Override
     public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState)
     {
       View rootView = inflator.inflate(R.layout.fragment_contact_us, container, false);
 
        getActivity().setTitle("Contact Us");
 
        
        //This sets up the listeners for the image buttons
        ImageButton ImagebuttonT;
        ImageButton ImagebuttonF;
        ImageButton ImagebuttonM;
              
        ImagebuttonT = (ImageButton) rootView.findViewById(R.id.buttonT);
        ImagebuttonF = (ImageButton) rootView.findViewById(R.id.buttonF);
        ImagebuttonM = (ImageButton) rootView.findViewById(R.id.buttonM);
               
        ImagebuttonT.setOnClickListener(this);
        ImagebuttonF.setOnClickListener(this);
        ImagebuttonM.setOnClickListener(this);
         
        return rootView;
    }
 
 
 
   	//This gives the actions to the buttons when they are clicked on
 	@Override
 	public void onClick(View v) {				
 			
 		switch(v.getId())
 		{
 		case R.id.buttonM:
 			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			String[] recipients = new String[]{"drprepapp@gmail.com", "thorn79@gmail.com","devryscott@gmail.com"};
 			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
 			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "DRPrep Feedback");
 			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Please type your comment or question below: \n\n\n\n");
 			emailIntent.setType("text/plain");
 			startActivity(Intent.createChooser(emailIntent, "Send Email with..."));
 			break;
 		}
 		
 		
 		switch(v.getId())
 		{
 		case R.id.buttonT:
 			Uri uri = Uri.parse("https://twitter.com/DrPrep1");
 			 Intent intent = new Intent(Intent.ACTION_VIEW, uri);
 			 startActivity(intent);
 		}
 		
 		
 		switch(v.getId())
 		{
 		case R.id.buttonF:
 			Uri uri = Uri.parse("https://www.facebook.com/pages/DR-Prep/1383849238519326");
 			 Intent intent = new Intent(Intent.ACTION_VIEW, uri);
 			 startActivity(intent);
 		}
 		
 		
 		
 	}
 	
 	
 	
 }
