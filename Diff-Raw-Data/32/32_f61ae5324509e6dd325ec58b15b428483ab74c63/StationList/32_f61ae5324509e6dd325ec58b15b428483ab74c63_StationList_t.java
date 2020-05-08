 /*
  * Copyright 2012 Sergio Garcia Villalonga (yayalose@gmail.com)
  *
  * This file is part of PalmaBici.
  *
  *    PalmaBici is free software: you can redistribute it and/or modify
  *    it under the terms of the Affero GNU General Public License version 3
  *    as published by the Free Software Foundation.
  *
  *    PalmaBici is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    Affero GNU General Public License for more details
  *    (https://www.gnu.org/licenses/agpl-3.0.html).
  *    
  */
 
 package com.poguico.palmabici;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Matrix;
import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.poguico.palmabici.parsers.*;
 import com.poguico.palmabici.syncronizers.OrientationSynchronizer;
 
 public class StationList extends ListView {
 	private StationAdapter adapter;
 	private final Context context;
	private final WindowManager mWindowManager;
	private final Display mDisplay;
 	private final ArrayList<Station> stations;
 		
 	/*
 	 * Ugly solution to keep nice names
 	 */
 	private static final Map<String, String> myMap;
     static {
     	Map<String, String> aMap = new HashMap<String, String>();
         aMap.put("01", "Parc de ses Veles");
         aMap.put("06", "Manacor - Manuel Azaña");
         aMap.put("07", "Aragó - Nuredduna");
         aMap.put("09", "F. Manuel de los Herreros");
         aMap.put("13", "Parc de les Estacions");
         aMap.put("15", "J. Verdaguer - J. Balmes");
         aMap.put("16", "Parc de sa Riera");
         aMap.put("17", "Aragó - J. Balmes");
         aMap.put("21", "Pl. Alexander Flemimg");
         aMap.put("24", "Blanquerna - C. de Sallent");
         aMap.put("25", "Blanquerna - Bartolomé");
         aMap.put("27", "Pl. París");
         aMap.put("29", "Institut Balear");
         aMap.put("31", "Pl. Madrid");
         aMap.put("37", "Av. Argentina");
         aMap.put("41", "Fàbrica");
         aMap.put("45", "Jaume III");
         aMap.put("46", "Pl. Rei Joan Carles I");
         aMap.put("47", "Pl. Porta de Santa Catalina");
         aMap.put("49", "Pl. de la Reina");
         aMap.put("51", "Via Roma");
         aMap.put("52", "Cecili Metel");
         aMap.put("55", "Pl. Santa Eulàlia");
         aMap.put("56", "Pl. del Mercat");
         aMap.put("59", "Mateu Enric Lladó");
         aMap.put("60", "Travessera Ballester");
         aMap.put("63", "Pl. d'Espanya");
         aMap.put("65", "Pl. Alexandre Jaume");
         myMap = Collections.unmodifiableMap(aMap);
     }
 
     private class StationAdapter extends ArrayAdapter<Station> {    	
     	private final Context context;
     	private final ArrayList<Station> stations;
     	
     	public StationAdapter(Context context, ArrayList<Station> stations) {
     		super(context, R.layout.main_list_item_layout, stations);
     		
     		Collections.sort(stations);
     		
     		this.context = context;
     		this.stations = stations;
     	}
     	
     	@Override
     	public View getView(int position, View convertView, ViewGroup parent) {
     		Float dist_f;
     		Matrix rotate_matrix;
     		LayoutInflater inflater = (LayoutInflater) context
     				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     		View rowView   = inflater.inflate(R.layout.main_list_item_layout, parent, false);
     		
     		if (stations.get(position).isFavourite())
     			rowView.setBackgroundColor(context.getResources().getColor(R.color.list_favourite_item));
     				
     		TextView id       = (TextView)  rowView.findViewById(R.id.id);
     		TextView name     = (TextView)  rowView.findViewById(R.id.name);
     		TextView bikes    = (TextView)  rowView.findViewById(R.id.bikes);
     		TextView holes    = (TextView)  rowView.findViewById(R.id.holes);
     		TextView distance = (TextView)  rowView.findViewById(R.id.distance);
     		ImageView compass = (ImageView) rowView.findViewById(R.id.compass);
     				
     		id.setText(stations.get(position).getN_estacio() + " · ");		
     		name.setText(myMap.get(stations.get(position).getN_estacio()));
     		bikes.setText(context.getString(R.string.bikes) + ": " + stations.get(position).getBusy_slots());
     		holes.setText(context.getString(R.string.free_slots) + ": " + stations.get(position).getFree_slots());
     		    		
     		dist_f = stations.get(position).getDistance();
     		if (dist_f >= 0) {
     			
     			float rotation = (stations.get(position).getBearing() -
    							  OrientationSynchronizer.getOrientation()
    							  - (90 * mDisplay.getOrientation())) %
     							  360;
    			    			
     			rotate_matrix = new Matrix();
     			rotate_matrix.setRotate(rotation,
     					compass.getDrawable().getIntrinsicWidth() / (float)2,
     					compass.getDrawable().getIntrinsicHeight() / (float)2);
     			compass.setImageMatrix(rotate_matrix);
     			
 	    		if (dist_f >= 0 && dist_f < 1000)
 	    			distance.setText(String.valueOf(dist_f.intValue()) + "m");
 	    		else if (dist_f >= 0)
 	    			distance.setText(Parser.parseDistance(dist_f/1000, context) + "km");
     		} else {
     			compass.setVisibility(INVISIBLE);
     		}
     			
     		
     		return rowView;
     	}
     }
     
 	public StationList(Context c, ArrayList<Station> s) {
 		super(c);
 		
		this.context   = c;
		mWindowManager =  (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	    mDisplay 	   = mWindowManager.getDefaultDisplay();
				
 		this.stations = s;
 		
 		adapter = new StationAdapter(this.context, this.stations);
 		this.setAdapter(adapter);
 		
 		this.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position,
 					long arg3) {
 				
 				final Station station = adapter.getItem(position);
         		String message;	
         		
         		if (station.isFavourite()) {
         			message = new String(context.getString(R.string.remove_from_favorites));
         		} else {
         			message = new String(context.getString(R.string.add_to_favorites));
         		}
 
         		final CharSequence[] item = {message.subSequence(0, message.length())};
         		
         		AlertDialog.Builder builder = new AlertDialog.Builder(context);
         		builder.setTitle(myMap.get(station.getN_estacio()));
         		builder.setItems(item, new DialogInterface.OnClickListener() {
         		    public void onClick(DialogInterface dialog, int item) {
         		        station.changeFavouriteState();
         		        Collections.sort(stations);
         		        adapter.notifyDataSetChanged();
         		    }
         		});
         		builder.show();
         		return true;
 			}
 			
 		});
 	}
 	
 	public void refresh() {
 		for (Station station : stations)
 			station.updatePosition();
 		Collections.sort(stations);
 		adapter.notifyDataSetChanged();	
 	}
 }
