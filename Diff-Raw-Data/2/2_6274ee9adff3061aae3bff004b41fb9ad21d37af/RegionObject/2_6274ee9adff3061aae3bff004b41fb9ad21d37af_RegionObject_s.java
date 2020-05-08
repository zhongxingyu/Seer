 package ca.ubc.magic.enph479.builder;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import weka.core.Attribute;
 import weka.core.Instances;
 
 /**
  * RegionObject is the object that contains all of the informations about regions of Vancouver
  * @author richardlee@hotmail.ca
  *
  */
 public class RegionObject {
 
 	class regionX {
 		private double lat_min = -1;
 		private double lat_max = -1;
 		private double lng_min = -1;
 		private double lng_max = -1;
 		private eRegion reg;
 		
 		private double tweet_count = 0;
 		private double weather_score = 0;
 		private double sentiment_score = 0;
 		
 		public int region_name = -1;
 		public double weather_ave = 0;
 		public double sentiment_ave = 0;
 		
 		private regionX(double _lat_min, double _lat_max, double _lng_min, double _lng_max, eRegion _r, int _name) {
 			this.lat_min = _lat_min;
 			this.lat_max = _lat_max;
 			this.lng_min = _lng_min;
 			this.lng_max = _lng_max;
 			this.reg = _r;
 			this.region_name = _name;
 		}
 		
 		private boolean isThisRegion(double _lat, double _lng) {
 			if((_lat >= this.lat_min)&&(_lat <= this.lat_max)
 				&&(_lng>=this.lng_min)&&(_lng<=this.lng_max)) {
 				return true;
 			}
 			
 			return false;
 		}
 		
 		private void computeAverages() {
 			if(this.tweet_count == 0){
 				this.weather_ave = 0;
 				this.sentiment_ave = 0;
 			}
 			else {
 				this.weather_ave = this.weather_score / this.tweet_count;
 				this.sentiment_ave = this.sentiment_score / this.tweet_count;
 			}
 		}
 		
 		public String toJSONFormat() {
 			StringBuffer buffer = new StringBuffer("");
 			buffer.append("\"" + this.region_name + "\": {")
 				.append("\"currentWeatherAverage\":" + this.weather_ave + ",")
 				.append("\"currentSentimentAverage\":" + this.sentiment_ave + ",")
 				.append("\"tweetCount\":" + this.tweet_count)
 				.append("}");
 			return buffer.toString();
 		}
 	}
 	
 	private enum eRegion {
 		Vancouver(-1), WestVancouver(0), CentralVancouver(1), EastVancouver(2);
 		
 		int regionIndex;
 		eRegion(int _index) {
 	        this.regionIndex = _index;
 	    }
 	}
 	
 	private double vancouver_lat_min = 49.195;
 	private double vancouver_lat_max = 49.315;
 	private double vancouver_lng_min = -123.270;
 	private double vancouver_lng_max = -123.020;
 	
 	private double region1_lat_min = vancouver_lat_min;
 	private double region1_lat_max = vancouver_lat_max;
 	private double region1_lng_min = vancouver_lng_min;
 	private double region1_lng_max = -123.16772;
 	
 	private double region2_lat_min = vancouver_lat_min;
 	private double region2_lat_max = vancouver_lat_max;
 	private double region2_lng_min = -123.16772;
 	private double region2_lng_max = -123.05717;
 	
 	private double region3_lat_min = vancouver_lat_min;
 	private double region3_lat_max = vancouver_lat_max;
 	private double region3_lng_min = -123.05717;
 	private double region3_lng_max = vancouver_lng_max;
 	
 	private int regionCount = 3;
 	
 	private regionX regionVancouver = new regionX(vancouver_lat_min, vancouver_lat_max, vancouver_lng_min, vancouver_lng_max, eRegion.Vancouver, eRegion.Vancouver.regionIndex);
 	private regionX region1 = new regionX(region1_lat_min, region1_lat_max, region1_lng_min, region1_lng_max, eRegion.WestVancouver, eRegion.WestVancouver.regionIndex);
 	private regionX region2 = new regionX(region2_lat_min, region2_lat_max, region2_lng_min, region2_lng_max, eRegion.CentralVancouver, eRegion.CentralVancouver.regionIndex);
 	private regionX region3 = new regionX(region3_lat_min, region3_lat_max, region3_lng_min, region3_lng_max, eRegion.EastVancouver, eRegion.EastVancouver.regionIndex);
 	
 	public ArrayList<regionX> lRegionObject = new ArrayList<regionX>(regionCount);
 	
 	public int getRegionCount() {
 		return this.regionCount;
 	}
 	
 	public boolean isVancouver(double _lat, double _lng) {
 		if(regionVancouver.isThisRegion(_lat, _lng))
 			return true;
 		
 		return false;
 	}
 	
 	public int classifyIntoRegion(double _lat, double _lng, double _weatherScore, int _sentimentScore) {
 		if(region1.isThisRegion(_lat, _lng)) {
 			region1.weather_score += _weatherScore;
 			region1.sentiment_score += _sentimentScore;
 			region1.tweet_count++;
 			return region1.reg.regionIndex;
 		}
 		else if(region2.isThisRegion(_lat, _lng)) {
 			region2.weather_score += _weatherScore;
 			region2.sentiment_score += _sentimentScore;
 			region2.tweet_count++;
 			return region2.reg.regionIndex;
 		}
 		else if(region3.isThisRegion(_lat, _lng)) {
 			region3.weather_score += _weatherScore;
 			region3.sentiment_score += _sentimentScore;
 			region3.tweet_count++;
 			return region3.reg.regionIndex;
 		}
 		else
 			return -1;
 	}
 	
 	public String getJsonRegionObject() {
 		
 		region1.computeAverages();
 		region2.computeAverages();
 		region3.computeAverages();
 		
 		String json = "{";
 		json += region1.toJSONFormat() + ",";
 		json += region2.toJSONFormat() + ",";
		json += region3.toJSONFormat() + ",";
 		json += "}";
 		
 		return json;
 	}
 	
 }
