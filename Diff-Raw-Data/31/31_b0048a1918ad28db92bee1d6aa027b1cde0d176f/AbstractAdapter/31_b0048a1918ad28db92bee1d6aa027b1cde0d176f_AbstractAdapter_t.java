 package com.engine9;
 
 import java.io.IOException;
 import java.util.Locale;
 import java.util.Vector;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.location.Geocoder;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class AbstractAdapter extends ArrayAdapter<AbstractInfo> {
 
 	private Vector<AbstractInfo> stops;
 	private Context context;
 	
 	public Boolean highlightSet = false;
 	
 	public AbstractAdapter(Context context, Vector<AbstractInfo> stops) {
 		super(context, R.layout.list_abstract, stops);
 		this.stops = stops;
 		this.context = context;
 		// TODO Auto-generated constructor stub
 	}
 	
 	@Override
 	public int getCount() {
 		// TODO Auto-generated method stub
 		return stops.size();
 	}
 
 
 	@Override
 	public long getItemId(int arg0) {
 		// TODO Auto-generated method stub
 		return arg0;
 	}
 	
 	@Override
 	public View getView(int position, View convert, ViewGroup parent) {
 		LayoutInflater inflater = (LayoutInflater) context
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		
 		View row = inflater.inflate(R.layout.list_abstract, parent, false);
 		
 		TextView time = (TextView) row.findViewById(R.id.abstract_time);
 		TextView area = (TextView) row.findViewById(R.id.abstract_area);
 		
 		AbstractInfo current = stops.get(position);
 		
 		if(System.currentTimeMillis() < current.time){
 			time.setText(String.valueOf((current.time - System.currentTimeMillis())/60000) + " MIN");
 		}
 		else{
 			time.setText("PAST");
 			time.setTextColor(Color.RED);
 		}
 		
		area.setText(current.description);
 		//row.setBackgroundColor(Color.RED);
		/*
 		Geocoder gcd = new Geocoder(getContext(), Locale.getDefault());
 		try {
 			area.setText( gcd.getFromLocation(current.position.latitude, 
 					current.position.longitude, 1).get(0).getLocality());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
		*/
 		
 		return row;
 	}
 	
 
 }
