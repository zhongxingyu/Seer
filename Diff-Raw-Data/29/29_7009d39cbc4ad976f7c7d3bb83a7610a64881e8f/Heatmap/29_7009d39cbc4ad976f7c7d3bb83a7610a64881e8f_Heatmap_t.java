 package com.xenon.greenup.api;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
import org.json.JSONObject;
 
 import android.util.Log;
 
 public class Heatmap {
 	
 	//TODO: Maybe just have this extend ArrayList?
 	
 	private ArrayList<HeatmapPoint> pointList;
 	
 	public Heatmap(String jsonString) {
 		int i;
		JSONObject object;
 		JSONArray points;
 		try {
			object = new JSONObject(jsonString);
			points = object.getJSONArray("grid");
 			this.pointList = new ArrayList<HeatmapPoint>();
 			for (i = 0; i < points.length(); i++){
 				this.pointList.add(new HeatmapPoint(points.getString(i)));
 				Log.i("lat",Double.toString(pointList.get(i).getLatDegree()));
 				Log.i("lon",Double.toString(pointList.get(i).getLonDegree()));
 				Log.i("time",Double.toString(pointList.get(i).getSecondsWorked()));
 			}
 		}
 		catch (JSONException e){
 			e.printStackTrace();
 		}
 	}
 	
 	public Heatmap() {
 		this.pointList = new ArrayList<HeatmapPoint>();
 	}
 	
 	public void add(HeatmapPoint point) {
 		pointList.add(point);
 	}
 	
 	public HeatmapPoint get(int i) {
 		return pointList.get(i);
 	}
 	
 	public JSONArray toJSON() {
 		JSONArray list = new JSONArray();
 		for (int i = 0; i < pointList.size(); i++) {
 			list.put(pointList.get(i).toJSON());
 		}
 		Log.i("pointList",list.toString());
 		return list;
 	}
 }
