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
 
 	public class regionX {
 		private double lat_min = -1;
 		private double lat_max = -1;
 		private double lng_min = -1;
 		private double lng_max = -1;
 		
 		private int tweet_count = 0;
 		private double weather_score = 0;
 		private double sentiment_score = 0;
 		
 		public int region_name = -1;
 		public double weather_ave = 0;
 		public double sentiment_ave = 0;
 		
 		private regionX(double _lat_min, double _lat_max, double _lng_min, double _lng_max, int _name) {
 			this.lat_min = _lat_min;
 			this.lat_max = _lat_max;
 			this.lng_min = _lng_min;
 			this.lng_max = _lng_max;
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
 		Vancouver(-1), UBC(0), WestPointGrey(1), Kitsilano(2), Fairview(3), MountPleasant(4),
 		Dunbar(5), Arbutus(6), Shaughnessy(7), SouthCambie(8), RileyPark(9), Kensington(10),
 		Renfrew(11), Kerrisdale(12), OakRidge(13), Marpole(14), Sunset(15), Victoria(16),
 		Killarney(17), StanleyPark(18), WestEnd(19), BusinessDistrict(20),
 		DowntownEastSide(21), Grandview(22), EastHastings(23);
 		
 		int regionIndex;
 		eRegion(int _index) {
 	        this.regionIndex = _index;
 	    }
 	}
 	//vancouver
 	private double vancouver_lat_min = 49.195;
 	private double vancouver_lat_max = 49.315;
 	private double vancouver_lng_min = -123.270;
 	private double vancouver_lng_max = -123.02422;
 	//ubc
 	private double region1_lat_min = 49.23083;
 	private double region1_lat_max = 49.27632;
 	private double region1_lng_min = -123.26660;
 	private double region1_lng_max = -123.21476;
 	//west point grey
 	private double region2_lat_min = 49.25795;
 	private double region2_lat_max = region1_lat_max;
 	private double region2_lng_min = region1_lng_min;
 	private double region2_lng_max = -123.18592;
 	//kitsilano
 	private double region4_lat_min = region2_lat_min;
 	private double region4_lat_max = region2_lat_max;
 	private double region4_lng_min = region2_lng_min;
 	private double region4_lng_max = -123.14610;
 	//fairview
 	private double region5_lat_min = region4_lat_min;
 	private double region5_lat_max = 49.26982;
 	private double region5_lng_min = region4_lng_max;
 	private double region5_lng_max = -123.11520;
 	//mount pleasant
 	private double region6_lat_min = region5_lat_min;
 	private double region6_lat_max = region5_lat_max;
 	private double region6_lng_min = region5_lng_max;
 	private double region6_lng_max = -123.07777;
 	//dunbar
 	private double region7_lat_min = 49.21939;
 	private double region7_lat_max = region2_lat_min;
 	private double region7_lng_min = region1_lng_max;
 	private double region7_lng_max = -123.17081;
 	//arbutus
 	private double region8_lat_min = 49.23441;
 	private double region8_lat_max = region4_lat_min;
 	private double region8_lng_min = region7_lng_max;
 	private double region8_lng_max = -123.15399;
 	//shaughnessy
 	private double region9_lat_min = region8_lat_min;
 	private double region9_lat_max = region5_lat_min;
 	private double region9_lng_min = region8_lng_max;
 	private double region9_lng_max = -123.13923;
 	//south cambie
 	private double region10_lat_min = region9_lat_min;
 	private double region10_lat_max = region9_lat_max;
 	private double region10_lng_min = region9_lng_max;
 	private double region10_lng_max = region5_lng_max;
 	//riley park
 	private double region11_lat_min = region10_lat_min;
 	private double region11_lat_max = region10_lat_max;
 	private double region11_lng_min = region10_lng_max;
 	private double region11_lng_max = -123.09082;
 	//kensington
 	private double region12_lat_min = region11_lat_min;
 	private double region12_lat_max = region11_lat_max;
 	private double region12_lng_min = region11_lng_max;
 	private double region12_lng_max = -123.05683;
 	//renfrew
 	private double region13_lat_min = region12_lat_min;
 	private double region13_lat_max = region12_lat_max;
 	private double region13_lng_min = region12_lng_max;
 	private double region13_lng_max = vancouver_lng_max;
 	//kerrisdale
 	private double region14_lat_min = 49.20324;
 	private double region14_lat_max = region8_lat_min;
 	private double region14_lng_min = region7_lng_max;
 	private double region14_lng_max = -123.14026;
 	//oakRidge
 	private double region15_lat_min = 49.21872;
 	private double region15_lat_max = region14_lat_max;
 	private double region15_lng_min = region14_lng_max;
 	private double region15_lng_max = -123.10215;
 	//marpole
 	private double region16_lat_min = 49.19965;
 	private double region16_lat_max = region15_lat_min;
 	private double region16_lng_min = region14_lng_max;
 	private double region16_lng_max = region15_lng_max;
 	//sunset
 	private double region17_lat_min = region14_lat_min;
 	private double region17_lat_max = region15_lat_max;
 	private double region17_lng_min = region16_lng_max;
 	private double region17_lng_max = region6_lng_max;
 	//victoria
 	private double region18_lat_min = region17_lat_min;
 	private double region18_lat_max = region17_lat_max;
 	private double region18_lng_min = region17_lng_max;
 	private double region18_lng_max = -123.05511;
 	//killarney
 	private double region19_lat_min = region18_lat_min;
 	private double region19_lat_max = region18_lat_max;
 	private double region19_lng_min = region18_lng_max;
 	private double region19_lng_max = vancouver_lng_max;
 	//StanleyPark
 	private double region20_lat_min = 49.29311;
 	private double region20_lat_max = 49.31371;
 	private double region20_lng_min = -123.16017;
 	private double region20_lng_max = -123.11794;
 	//west end
 	private double region21_lat_min = region4_lat_max;
 	private double region21_lat_max = region20_lat_min;
 	private double region21_lng_min = -123.14644;
 	private double region21_lng_max = -123.12378;
 	//business district
 	private double region22_lat_min = region21_lat_min;
 	private double region22_lat_max = region21_lat_max;
 	private double region22_lng_min = region21_lng_max;
 	private double region22_lng_max = -123.10181;
 	//downtown east side
 	private double region23_lat_min = region6_lat_max;
 	private double region23_lat_max = 49.28953;
 	private double region23_lng_min = region22_lng_max;
 	private double region23_lng_max = region6_lng_max;
 	//grandview
 	private double region24_lat_min = region12_lat_max;
 	private double region24_lat_max = 49.29334;
 	private double region24_lng_min = region23_lng_max;
 	private double region24_lng_max = region12_lng_max;
 	//east hastings
 	private double region25_lat_min = region24_lat_min;
 	private double region25_lat_max = region24_lat_max;
 	private double region25_lng_min = region24_lng_max;
 	private double region25_lng_max = vancouver_lng_max;
 	
	private int regionCount = 24;
 	public ArrayList<regionX> lRegionObject = new ArrayList<regionX>(regionCount);
 	
 	private regionX regionVancouver = new regionX(vancouver_lat_min, vancouver_lat_max, vancouver_lng_min, vancouver_lng_max, eRegion.Vancouver.regionIndex);
 	private regionX region1 = new regionX(region1_lat_min, region1_lat_max, region1_lng_min, region1_lng_max, eRegion.UBC.regionIndex);
 	private regionX region2 = new regionX(region2_lat_min, region2_lat_max, region2_lng_min, region2_lng_max, eRegion.WestPointGrey.regionIndex);
 	private regionX region4 = new regionX(region4_lat_min, region4_lat_max, region4_lng_min, region4_lng_max, eRegion.Kitsilano.regionIndex);
 	private regionX region5 = new regionX(region5_lat_min, region5_lat_max, region5_lng_min, region5_lng_max, eRegion.Fairview.regionIndex);
 	private regionX region6 = new regionX(region6_lat_min, region6_lat_max, region6_lng_min, region6_lng_max, eRegion.MountPleasant.regionIndex);
 	private regionX region7 = new regionX(region7_lat_min, region7_lat_max, region7_lng_min, region7_lng_max, eRegion.Dunbar.regionIndex);
 	private regionX region8 = new regionX(region8_lat_min, region8_lat_max, region8_lng_min, region8_lng_max, eRegion.Arbutus.regionIndex);
 	private regionX region9 = new regionX(region9_lat_min, region9_lat_max, region9_lng_min, region9_lng_max, eRegion.Shaughnessy.regionIndex);
 	private regionX region10 = new regionX(region10_lat_min, region10_lat_max, region10_lng_min, region10_lng_max, eRegion.SouthCambie.regionIndex);
 	private regionX region11 = new regionX(region11_lat_min, region11_lat_max, region11_lng_min, region11_lng_max, eRegion.RileyPark.regionIndex);
 	private regionX region12 = new regionX(region12_lat_min, region12_lat_max, region12_lng_min, region12_lng_max, eRegion.Kensington.regionIndex);
 	private regionX region13 = new regionX(region13_lat_min, region13_lat_max, region13_lng_min, region13_lng_max, eRegion.Renfrew.regionIndex);
 	private regionX region14 = new regionX(region14_lat_min, region14_lat_max, region14_lng_min, region14_lng_max, eRegion.Kerrisdale.regionIndex);
 	private regionX region15 = new regionX(region15_lat_min, region15_lat_max, region15_lng_min, region15_lng_max, eRegion.OakRidge.regionIndex);
 	private regionX region16 = new regionX(region16_lat_min, region16_lat_max, region16_lng_min, region16_lng_max, eRegion.Marpole.regionIndex);
 	private regionX region17 = new regionX(region17_lat_min, region17_lat_max, region17_lng_min, region17_lng_max, eRegion.Sunset.regionIndex);
 	private regionX region18 = new regionX(region18_lat_min, region18_lat_max, region18_lng_min, region18_lng_max, eRegion.Victoria.regionIndex);
 	private regionX region19 = new regionX(region19_lat_min, region19_lat_max, region19_lng_min, region19_lng_max, eRegion.Killarney.regionIndex);
 	private regionX region20 = new regionX(region20_lat_min, region20_lat_max, region20_lng_min, region20_lng_max, eRegion.StanleyPark.regionIndex);
 	private regionX region21 = new regionX(region21_lat_min, region21_lat_max, region21_lng_min, region21_lng_max, eRegion.WestEnd.regionIndex);
 	private regionX region22 = new regionX(region22_lat_min, region22_lat_max, region22_lng_min, region22_lng_max, eRegion.BusinessDistrict.regionIndex);
 	private regionX region23 = new regionX(region23_lat_min, region23_lat_max, region23_lng_min, region23_lng_max, eRegion.DowntownEastSide.regionIndex);
 	private regionX region24 = new regionX(region24_lat_min, region23_lat_max, region24_lng_min, region24_lng_max, eRegion.Grandview.regionIndex);
 	private regionX region25 = new regionX(region25_lat_min, region25_lat_max, region25_lng_min, region25_lng_max, eRegion.Grandview.regionIndex);
 	
 	public RegionObject(){
 		lRegionObject.add(region1);
 		lRegionObject.add(region2);
 		lRegionObject.add(region4);
 		lRegionObject.add(region5);
 		lRegionObject.add(region6);
 		lRegionObject.add(region7);
 		lRegionObject.add(region8);
 		lRegionObject.add(region9);
 		lRegionObject.add(region10);
 		lRegionObject.add(region11);
 		lRegionObject.add(region12);
 		lRegionObject.add(region13);
 		lRegionObject.add(region14);
 		lRegionObject.add(region15);
 		lRegionObject.add(region16);
 		lRegionObject.add(region17);
 		lRegionObject.add(region18);
 		lRegionObject.add(region19);
 		lRegionObject.add(region20);
 		lRegionObject.add(region21);
 		lRegionObject.add(region22);
 		lRegionObject.add(region23);
 		lRegionObject.add(region24);
 		lRegionObject.add(region25);
 	}
 	
 	public int getRegionCount() {
 		return this.regionCount;
 	}
 	
 	public boolean isVancouver(double _lat, double _lng) {
 		if(regionVancouver.isThisRegion(_lat, _lng))
 			return true;
 		
 		return false;
 	}
 	
 	public int classifyIntoRegion(double _lat, double _lng, double _weatherScore, int _sentimentScore) {
 		for(int i = 0; i < lRegionObject.size(); i++) {
 			if(lRegionObject.get(i).isThisRegion(_lat, _lng)) {
 				lRegionObject.get(i).weather_score += _weatherScore;
 				lRegionObject.get(i).sentiment_score += _sentimentScore;
 				lRegionObject.get(i).tweet_count++;
 				return lRegionObject.get(i).region_name;
 			}
 		}
 		return -1;
 	}
 	
 	public String getJsonRegionObject() {
 		for(int i = 0; i < lRegionObject.size(); i++) {
 			lRegionObject.get(i).computeAverages();
 		}
 		
 		String json = "{";
 		for(int i = 0; i < lRegionObject.size(); i++) {
 			json += lRegionObject.get(i).toJSONFormat();
 			if(i < lRegionObject.size())
 				 json += ",";
 		}
 		json += "}";
 		
 		return json;
 	}
 	
 	public ArrayList<regionX> getCurrentlRegionObjectForTimePlay() {
 		
 		for(int i = 0; i < lRegionObject.size(); i++) {
 			lRegionObject.get(i).computeAverages();
 		}
 		
 		return lRegionObject;
 	}
 	
 }
