 package com.kulkarni.gasmileagecalculator;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Locale;
 
 import com.kulkarni.gasmileagecalculator.data.Fillup;
 import com.kulkarni.gasmileagecalculator.data.FillupVector;
 
 import android.app.Activity;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 public class HistoryAdapter extends BaseAdapter {
 	
 	private Activity activity;
 	private FillupVector fv;
 	private static LayoutInflater inflater = null;
 	
 	public HistoryAdapter (Activity a, FillupVector fillupList) {
 		activity = a;
 		fv 		 = fillupList;
 		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	}
 
 	@Override
 	public int getCount() {
 		// TODO Auto-generated method stub
 		return fv.size();
 	}
 
 	@Override
 	public Object getItem(int position) {
 		// TODO Auto-generated method stub
		return fv.getItem(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		// TODO Auto-generated method stub
 		return position;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View view =  convertView;
 		if (convertView == null) {
 			view = inflater.inflate(R.layout.history_adapter, null);
 		}
 		
 		TextView textDate    = (TextView) view.findViewById(R.id.txt_date);
 		TextView textMPG     = (TextView) view.findViewById(R.id.txt_mileage);
 		TextView textVehicle = (TextView) view.findViewById(R.id.txt_vehicle);
 		TextView textVolume  = (TextView) view.findViewById(R.id.txt_fuel_volume);
 		TextView textRate    = (TextView) view.findViewById(R.id.txt_fuel_rate);
 		TextView textCost    = (TextView) view.findViewById(R.id.txt_fillup_cost);
 		
 		Fillup fillup         = fv.getItem (position);
 		Fillup previousFillup = fv.getItem (position - 1);
 		
 		SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getDateInstance();
 		textDate.setText(sdf.format(fillup._fillup_date));
 		
 		textVehicle.setText(activity.getResources().getString(R.string.vehicle_nickname).toUpperCase(Locale.US));
 		textVolume.setText(String.format("%.2f gal", fillup._fillup_fuel_volume));
 		textRate.setText(String.format("$ %.2f/gal", fillup._fillup_fuel_rate));
 		textCost.setText(String.format("$ %.2f", fillup._fillup_fuel_cost));
 		
 		if (previousFillup != null && fv.size() > 1) {
 			double mileage = Fillup.get_point_mileage(fillup, previousFillup);
 			double average = fv.get_overall_mileage();
 			
 			textMPG.setText(String.format("%.2f mpg", mileage));
 			if (mileage > average)	textMPG.setTextColor(activity.getResources().getColor(R.color.fillup_above_avg));
 			else if (mileage < average)	textMPG.setTextColor(activity.getResources().getColor(R.color.fillup_below_avg));
 			else	textMPG.setTextColor(activity.getResources().getColor(R.color.fillup_avg));
 		}
 		else {
 			textMPG.setText("N/A");
			textMPG.setTextColor(activity.getResources().getColor(android.R.color.darker_gray));
 		}
 		
 		// TODO Auto-generated method stub
 		return view;
 	}
 
 }
