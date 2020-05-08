 package com.engine9;
 
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 
 public class AbstractInfo
 {
 	public LatLng position;
 	public Long time;
 	
	public AbstractInfo(LatLng position, Long time){
 		this.position = position;
 		this.time = time;
 	}
 }
