 package com.patmahoneyJR.csu.vikings;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class Parking extends ListActivity {
 	
 	private String[] lots = {"Lot 10", "Lot 11", "Lot 20", "Lot 21",
 			"Lot 22", "Lot 40", "Lot 50", "Lot 51", "Lot 54",
 			"Lot 57", "Lot 61", "Lot 62", "Lot 80", "Lot 90",
 			"Lot CG", "Lot EG", "Lot MG", "Lot PG", "Lot SG",
 			"Lot RG", "Lot UG", "Lot WG"};
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setListAdapter(new ArrayAdapter<String>(this,
         		R.layout.simple_list_item_1,
         		lots));
 	}
 	
 	public void onListItemClick(ListView parent, View v, 
 			int position, long id) {
 		Toast.makeText(this, lots[position], Toast.LENGTH_SHORT).show();
 		
 		Intent mapIntent = new Intent(this, ParkingMap.class);
 		
 		if(lots[position].equals("Lot 10")) {
 			mapIntent.putExtra("lat", 41.50542);
 			mapIntent.putExtra("lon", -81.676141);
 		}
		else if(lots[position].equals("Lot 11")) {
 			mapIntent.putExtra("lat", 41.504847);
 			mapIntent.putExtra("lon", -81.675926);
 		}
 		else if(lots[position].equals("Lot 20")) {
 			mapIntent.putExtra("lat", 41.503138);
 			mapIntent.putExtra("lon", -81.678335);
 		}
 		else if(lots[position].equals("Lot 21")) {
 			mapIntent.putExtra("lat", 41.501844);
 			mapIntent.putExtra("lon", -81.676202);
 		}
 		else if(lots[position].equals("Lot 22")) {
 			mapIntent.putExtra("lat", 41.502682); 
 			mapIntent.putExtra("lon", -81.674745); 
 		}
 		else if(lots[position].equals("Lot 40")) {
 			mapIntent.putExtra("lat", 41.499976);
 			mapIntent.putExtra("lon", -81.677041);
 		}
 		else if(lots[position].equals("Lot 50")) {
 			mapIntent.putExtra("lat", 41.505775);
 			mapIntent.putExtra("lon", -81.67481);
 		}
 		else if(lots[position].equals("Lot 51")) {
 			mapIntent.putExtra("lat", 41.505363);
 			mapIntent.putExtra("lon", -81.674552);
 		}
 		else if(lots[position].equals("Lot 54")) {
 			mapIntent.putExtra("lat", 41.506209);
 			mapIntent.putExtra("lon", -81.674029);
 		}
 		else if(lots[position].equals("Lot 57")) {
 			mapIntent.putExtra("lat", 41.505773);
 			mapIntent.putExtra("lon", -81.672584);
 		}
 		else if(lots[position].equals("Lot 61")) {
 			mapIntent.putExtra("lat", 41.503138);
 			mapIntent.putExtra("lon", -81.673973);
 		}
 		else if(lots[position].equals("Lot 62")) {
 			mapIntent.putExtra("lat", 41.50374);
 			mapIntent.putExtra("lon", -81.67297);
 		}
 		else if(lots[position].equals("Lot 80")) {
 			mapIntent.putExtra("lat", 41.499671);
 			mapIntent.putExtra("lon", -81.673196);
 		}
 		else if(lots[position].equals("Lot 90")) {
 			mapIntent.putExtra("lat", 41.503957);
 			mapIntent.putExtra("lon", -81.666769);
 		}
 		else if(lots[position].equals("Lot CG")) {
 			mapIntent.putExtra("lat", 41.502897);
 			mapIntent.putExtra("lon", -81.676935);
 		}
 		else if(lots[position].equals("Lot EG")) {
 			mapIntent.putExtra("lat", 41.503825);
 			mapIntent.putExtra("lon", -81.670551);
 		}
 		else if(lots[position].equals("Lot MG")) {
 			mapIntent.putExtra("lat", 41.502346);
 			mapIntent.putExtra("lon", -81.674327);
 		}
 		else if(lots[position].equals("Lot PG")) {
 			mapIntent.putExtra("lat", 41.500822);
 			mapIntent.putExtra("lon", -81.672637);
 		}
 		else if(lots[position].equals("Lot SG")) {
 			mapIntent.putExtra("lat", 41.499803);
 			mapIntent.putExtra("lon", -81.674907);
 		}
 		else if(lots[position].equals("Lot RG")) {
 			mapIntent.putExtra("lat", 41.503841);
 			mapIntent.putExtra("lon", -81.671264);
 		}
 		else if(lots[position].equals("Lot UG")) {
 			mapIntent.putExtra("lat", 41.502624);
 			mapIntent.putExtra("lon", -81.673013);
 		}
 		else if(lots[position].equals("Lot WG")) {
 			mapIntent.putExtra("lat", 41.502889);
 			mapIntent.putExtra("lon", -81.679982);
 		}
 		
 		startActivity(mapIntent);
 	}
 }
