 package com.xenon.greenup.api;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.util.Log;
 
 public class PinList {
 	
 	private double latDegrees;
 	private double latOffset;
 	private double lonDegrees;
 	private double lonOffset;
 	private ArrayList<Pin> pinList;
 	
 	public PinList(String jsonString) {
 		int i;
 		JSONArray pins;
 		try {
			pins = new JSONArray(jsonString);
 			this.pinList = new ArrayList<Pin>();
 			for (i = 0; i < pins.length(); i++){
 				this.pinList.add(new Pin(pins.getString(i)));
 				Log.i("lat",Double.toString(pinList.get(i).getLatDegrees()));
 				Log.i("lon",Double.toString(pinList.get(i).getLonDegrees()));
 				Log.i("type",pinList.get(i).getType());
 				Log.i("message",pinList.get(i).getMessage());
 			}
 		}
 		catch (JSONException e){
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * @return the latDegrees
 	 */
 	public double getLatDegrees() {
 		return latDegrees;
 	}
 	/**
 	 * @param latDegrees the latDegrees to set
 	 */
 	public void setLatDegrees(double latDegrees) {
 		this.latDegrees = latDegrees;
 	}
 	/**
 	 * @return the latOffset
 	 */
 	public double getLatOffset() {
 		return latOffset;
 	}
 	/**
 	 * @param latOffset the latOffset to set
 	 */
 	public void setLatOffset(double latOffset) {
 		this.latOffset = latOffset;
 	}
 	/**
 	 * @return the lonDegrees
 	 */
 	public double getLonDegrees() {
 		return lonDegrees;
 	}
 	/**
 	 * @param lonDegrees the lonDegrees to set
 	 */
 	public void setLonDegrees(float lonDegrees) {
 		this.lonDegrees = lonDegrees;
 	}
 	/**
 	 * @return the lonOffset
 	 */
 	public double getLonOffset() {
 		return lonOffset;
 	}
 	/**
 	 * @param lonOffset the lonOffset to set
 	 */
 	public void setLonOffset(float lonOffset) {
 		this.lonOffset = lonOffset;
 	}
 	/**
 	 * @return the pinList
 	 */
 	public ArrayList<Pin> getPinList() {
 		return pinList;
 	}
 	/**
 	 * @param pinList the pinList to set
 	 */
 	public void setPinList(ArrayList<Pin> pinList) {
 		this.pinList = pinList;
 	}
 	
 	
 }
