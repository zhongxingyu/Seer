 package ca.ubc.magic.enph479.builder;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import weka.core.Attribute;
 import weka.core.Instances;
 
 /**
  * TwitterObject is the object that contains all of the structured data of tweets fetched from WoTKit
  * id, timestamp, latitude, longitude, sensor id, sensor name, message, value
  * @author richardlee@hotmail.ca
  *
  */
 public class TwitterObject {
 
 	private int id = -1;
 	private String timestamp = "undefined";
 	private double lat = -1;
 	private double lng = -1;
 	private int sensor_id = -1;
 	private String sensor_name = "undefined";
 	private String message = "undefined";
 	private int value = -1;
 	private int region = -1;
 	private int sentiment = -1;
 	private double temperature = -999;
 	private double precipitation = -999;
 	private double weather_score = -999;
 	
 	private double temp_weight = 0.5;
 	private double prec_weight = 0.5;
 	
 	public int getId() {
 		return id;
 	}
 	public void setId(int id) {
 		this.id = id;
 	}
 	public String getTimestamp() {
 		return timestamp;
 	}
 	public void setTimestamp(String timestamp) {
 		this.timestamp = timestamp;
 	}
 	public double toEpochTime() {
 		
 	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm:ss zzz");
 	    Date date = null;
 	    if(timestamp == "undefined")
 	    	return -1;
 	    else{
 	    	try {
 				date = sdf.parse(timestamp + " UTC");
 			} catch (ParseException e) {
 				e.printStackTrace();
 				return -1;
 			}
 		    long epoch = date.getTime();
 		    
 		    return (double) epoch;
 	    }
 	}
 	public double getLatitude() {
 		return lat;
 	}
 	public void setLatitude(double latitude) {
 		this.lat = latitude;
 	}
 	public double getLongitude() {
 		return lng;
 	}
 	public void setLongitude(double longitude) {
 		this.lng = longitude;
 	}
 	public int getSensor_id() {
 		return sensor_id;
 	}
 	public void setSensor_id(int sensor_id) {
 		this.sensor_id = sensor_id;
 	}
 	public String getSensor_name() {
 		return sensor_name;
 	}
 	public void setSensor_name(String sensor_name) {
 		this.sensor_name = sensor_name;
 	}
 	public String getMessage() {
 		return message;
 	}
 	public void setMessage(String message) {
 		this.message = message;
 	}
 	public int getValue() {
 		return value;
 	}
 	public void setValue(int value) {
 		this.value = value;
 	}
 	public String printInfo() {
 		return "Twitter-id: " + this.id +
 				" timestamp: " + this.timestamp +
 				" latitude: " + this.lat + 
 				" longitude: " + this.lng 
 				+ " polarity: " + this.getSentiment();
 	}
 	public int getSentiment() {
 		return sentiment;
 	}
 	public void setSentiment(int sentiment) {
 		this.sentiment = sentiment;
 	}
 	public double getTemperature() {
 		return temperature;
 	}
 	public void setTemperature(double temperature) {
 		this.temperature = temperature;
 	}
 	public double getPrecipitation() {
 		return precipitation;
 	}
 	public void setPrecipitation(double precipitation) {
 		this.precipitation = precipitation;
 	}
 	public double getWeather_score() {
 		return weather_score;
 	}
 	public void calculateWeatherScore() {
 		if((this.getTemperature()==-999)&&(this.getPrecipitation()==-999)){
 			weather_score = -999;
 		}
 		if((this.getTemperature()!=-999)&&(this.getPrecipitation()==-999)) {
 			this.setPrecipitation(0.0);
 		}
 		// happiness function for temperature
 		double temperature_happiness = 10*Math.exp(-Math.pow(this.getTemperature()-21, 2)/150);
 		// happiness function for precipitation
 		double precipitation_happiness = 10*Math.exp(-1/5 * this.getPrecipitation());
 		
 		weather_score = (double) (temp_weight * temperature_happiness + prec_weight * precipitation_happiness);
 	}
 	public int getRegion() {
 		return region;
 	}
 	public void setRegion(int region) {
 		this.region = region;
 	}
 	
 	/**
 	 * Converts TwitterObject to TweetInstance
 	 * 
 	 * @param numAtts number of attributes to be considered. (Latitude, Longitude and time)
 	 * @return A TweetInstance
 	 */
 	public TweetInstance toTweetInstance(int numAtts) {
 		TweetInstance tempTweetInst = new TweetInstance(numAtts, this.getId());
 		//TODO: figure out how to make this work with more than two attributes
 		tempTweetInst.setValue(new Attribute("latitude", 0), this.getLatitude());
 		tempTweetInst.setValue(new Attribute("longitude", 1), this.getLongitude());
 		if (numAtts == 3)
 			tempTweetInst.setValue(new Attribute("time", 2), this.toEpochTime());
 		
 		ArrayList<Attribute> atts = new ArrayList<Attribute>(numAtts);
 		for (int i=0; i< numAtts; i++) {
 			atts.add(new Attribute("att"+i));
 		}
 		
 		tempTweetInst.setDataset(new Instances("Dataset" + tempTweetInst.getId(), atts , 0));
 		
 		return tempTweetInst;
 	}
 }
