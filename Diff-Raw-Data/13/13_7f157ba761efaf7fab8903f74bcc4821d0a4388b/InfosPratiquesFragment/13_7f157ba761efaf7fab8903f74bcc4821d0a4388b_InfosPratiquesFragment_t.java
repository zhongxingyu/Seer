 package com.utt.scd.apropos;
 
 
 
 import java.util.List;
 
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 
 import android.text.SpannableString;
 import android.text.style.StyleSpan;
 import android.text.style.UnderlineSpan;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.utt.scd.R;
 
 public class InfosPratiquesFragment extends SherlockFragment implements OnClickListener
 {
 	private Button questions_reponses;
 	private TextView itineraire;
 
 	
 	public static InfosPratiquesFragment newInstance() 
 	{
 		InfosPratiquesFragment fragment = new InfosPratiquesFragment();
 		
 		return fragment;
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) 
 	{
 		View view = inflater.inflate(R.layout.infos_pratiques, null);
 		
 		
 		SpannableString spanString = new SpannableString("Itinraire");
 		spanString.setSpan(new UnderlineSpan(), 0, spanString.length(), 0);
 		spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
 		spanString.setSpan(new StyleSpan(Typeface.ITALIC), 0, spanString.length(), 0);
 
 		this.itineraire = (TextView) view.findViewById(R.id.textView5);
 		this.itineraire.setOnClickListener(this);
 		this.itineraire.setText(spanString);
 		
 		this.questions_reponses = (Button) view.findViewById(R.id.button1);
 		this.questions_reponses.setOnClickListener(this);
 		
 		return view;
 	}
 	
 	@Override
 	public void onClick(View v) 
 	{
 		if (v.equals(itineraire))
 		{
 			LocationManager locationManager = (LocationManager)getSherlockActivity().getSystemService(Context.LOCATION_SERVICE);
 			String provider = locationManager.getBestProvider(new Criteria(), true);
 
 			double location_longitude = 0;
 			double location_latitude = 0;
 			
 			Location locations = locationManager.getLastKnownLocation(provider);
 			List<String>  providerList = locationManager.getAllProviders();
 			
 			if(null != locations && null != providerList && providerList.size() > 0)
 			{                 
 				location_longitude = locations.getLongitude();
				location_latitude = locations.getLatitude();  			
 			}
 			
 			if(location_latitude != 0 && location_longitude != 0)
 			{
 				double destionation_longitude = 4.0669048;
 				double destionation_latitude = 48.2690833;
 				
 				
 				Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr=" + location_latitude + "," + location_longitude+ 
 																													"&daddr="+ destionation_latitude + "," + destionation_longitude));
 				intent.setComponent(new ComponentName("com.google.android.apps.maps", 
 					    								"com.google.android.maps.MapsActivity"));
 				startActivity(intent);
 			}
			else
			{
				Toast.makeText(getSherlockActivity(), "Votre position actuelle est inconnue", Toast.LENGTH_LONG).show();
			}
 			
 		}
 		else if (v.equals(questions_reponses))
 		{
 			Intent intent = new Intent(getSherlockActivity(),QuestionsReponses.class);
 			startActivity(intent);
 		}
 		
 	}
 	
 
 }
