 package com.utt.scd.apropos;
 
 
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
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
 		
 		this.itineraire = (TextView) view.findViewById(R.id.textView5);
 		this.itineraire.setOnClickListener(this);
 		
 		this.questions_reponses = (Button) view.findViewById(R.id.button1);
 		this.questions_reponses.setOnClickListener(this);
 		
 		return view;
 	}
 	
 	@Override
 	public void onClick(View v) 
 	{
 		if (v.equals(itineraire))
 		{
			LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
			String provider = locationManager.getBestProvider(new Criteria(), true);
			
			double location_longitude = 0;
			double location_latitude = 0;
			Location locations = locationManager.getLastKnownLocation(provider);
			List<String> providerList = locationManager.getAllProviders();
			if(null!=locations && null!=providerList && providerList.size()>0)
			{
				location_longitude = locations.getLongitude();
				location_latitude = locations.getLatitude();
			}
			
			System.out.println(location_longitude + " " + location_latitude);
			
			double destionation_longitude = 4.0669048;
			double destionation_latitude = 48.2690833;
			
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr="+
													location_latitude + "," + location_longitude) + "&daddr="
												 	destionation_latitude + ","+ destionation_longitude)

			//Intent intent = new Intent(getSherlockActivity(),Itineraire.class);
 			startActivity(intent);
 		}
 		else if (v.equals(questions_reponses))
 		{
 			Intent intent = new Intent(getSherlockActivity(),QuestionsReponses.class);
 			startActivity(intent);
 		}
 		
 	}
 	
 
 }
