 package com.REST.actor;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import com.data.DetailedStopList;
 
 /**
  * This class wil be needed to hold the bus stops
  * @author edopost
  * @version V1.0
  *
  */
 
 public class Stop {
 	//stop code
 	private String SpBern;
 	//latitude
 	private double Latitude;
 	//stop name
 	private String TimingPointName;
 	//longitude
 	private double Longitude;
 	//stop town
 	private String TimingPointTown;
 	//detaild info
 	private DetailedStopList details;
 	//updateTime
	@SuppressWarnings("unused")
 	private String lastUpdate;
 	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 	
 	
 	public Stop(String SpBern, double Latitude, String TimingPointName, double Longitude, String TimingPointTown) {
 		this.SpBern = SpBern;
 		this.Latitude = Latitude;
 		this.TimingPointName = TimingPointName;
 		this.Longitude = Longitude;
 		this.TimingPointTown = TimingPointTown;
 	}
 
 	public DetailedStopList getDetails() {
 	//	if(details == null) {
 			details = new DetailedStopList(SpBern);
 			details.update();
 			lastUpdate = dateFormat.format(new Date()).toString();
 	//	} 
 		//TODO update only afeter 1 minit
 		
 		return details;
 	}
 	
 	
 	/**
 	 * @return the spBern
 	 */
 	public String getSpBern() {
 		return SpBern;
 	}
 
 	/**
 	 * @return the latitude
 	 */
 	public double getLatitude() {
 		return Latitude;
 	}
 
 	/**
 	 * @return the timingPointName
 	 */
 	public String getTimingPointName() {
 		return TimingPointName;
 	}
 
 	/**
 	 * @return the longitude
 	 */
 	public double getLongitude() {
 		return Longitude;
 	}
 
 	/**
 	 * @return the timingPointTown
 	 */
 	public String getTimingPointTown() {
 		return TimingPointTown;
 	}
 	
 	
 }
