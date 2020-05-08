 package com.ksj.bamft.mbta;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 
 import com.ksj.bamft.R;
 import com.ksj.bamft.csv.CSVReader;
 import com.ksj.bamft.maps.MapHelpers;
 import com.ksj.bamft.model.HubwayStation;
 import com.ksj.bamft.model.MbtaStation;
 
 public class MbtaHelpers {
 
 	public static ArrayList<MbtaStation> getAllMbtaStations(Context context) {
 
 		ArrayList<MbtaStation> list = new ArrayList<MbtaStation>();
 
 		InputStream mbtaRawResource = context.getResources().openRawResource(
 				R.raw.mbtastations);
 
 		CSVReader reader = new CSVReader(new InputStreamReader(mbtaRawResource));
 		String[] nextLine;
 
 		try {
 			while ((nextLine = reader.readNext()) != null) {
 
 				/*
 				 * try { Log.d("HELLOMBTA", nextLine[0] + " " + nextLine[1] +
 				 * " " + nextLine[11] + " " + Double.parseDouble(nextLine[13]) +
 				 * " " + Double.parseDouble(nextLine[14]) + " "); } catch
 				 * (NumberFormatException e) { e.printStackTrace(); }
 				 */
 
 				MbtaStation mbtaStation = new MbtaStation();
 				try {
 					mbtaStation.setLineColor(nextLine[0]);
 					mbtaStation.setPlatformKey(nextLine[1]);
 					mbtaStation.setStopName(nextLine[11]);
 					mbtaStation.setLatitude(Double.parseDouble(nextLine[13]));
 					mbtaStation.setLongitude(Double.parseDouble(nextLine[14]));
 					list.add(mbtaStation);
 				} catch (NumberFormatException e) {
 					// the only time this happens is probably when we read the
 					// first line of the CSV, where they have the header labels.
 					e.printStackTrace();
 				}
 			}
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return list;
 
 	}
 	
 	public static MbtaStation getNearestMbtaStation(List<MbtaStation> mbtaStations, double sourceLat, double sourceLon) {
 		MbtaStation nearestMbtaStation = null;
 		
 		double smallestDistanceSeen = Double.MAX_VALUE;
 		
 		for (MbtaStation mbtaStation : mbtaStations) {
 			
 			
 			
 			double distance = MapHelpers.calculateDistance(
 					sourceLat, mbtaStation.getLatitude(),
 					sourceLon, mbtaStation.getLongitude());
 			
 			if(distance < smallestDistanceSeen) {
 				nearestMbtaStation = mbtaStation;
 				smallestDistanceSeen = distance;
 			}
 		}
		
 		return nearestMbtaStation;
 	}
 	
 
 }
