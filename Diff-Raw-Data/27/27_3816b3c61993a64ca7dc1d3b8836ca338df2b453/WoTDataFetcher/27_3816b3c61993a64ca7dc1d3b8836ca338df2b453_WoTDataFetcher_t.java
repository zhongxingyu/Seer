 package ca.ubc.magic.enph479;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.TimeZone;
 
 import ca.ubc.magic.enph479.DataManipulationProcessor.web_type;
 import ca.ubc.magic.enph479.DataManipulationProcessor.wot_type;
 import ca.ubc.magic.enph479.builder.TweetInstance;
 import ca.ubc.magic.enph479.builder.TwitterObject;
 import ca.ubc.magic.enph479.builder.WeatherObject;
 import ca.ubc.magic.enph479.builder.RegionObject.regionX;
 
 /**
  * WoTDataFetcher is a controlling manager that calls DataManipulationProcessor to gets data from WoTkit
  * User directly call functions in the class
  * @author richardlee@hotmail.ca
  *
  */
 public class WoTDataFetcher {
 
 	private DataManipulationProcessor dmp = null;
 	private DB_Handler dbh = null;
 	private int fetch_interval = 0; //in seconds
 	private int job_fetch_interval = 0; //in seconds
 	private String ref_datetime = null; //reference time in system for fetching
 	private int fetch_count = -1;
 	private int fetch_count_max = 0;
 	
 	public HashMap<Integer, TwitterObject> getAllTweetsData() throws Exception {
 		return dmp.gettweets_all();
 	}
 	
 	public int getTweetRegionCount() {
 		return dmp.getRegion_count();
 	}
 
 	public boolean prepareForFetching(int _fetch_interval, int _job_fetch_interval, String _start_time) throws Exception  {
 		dmp = new DataManipulationProcessor();
 		this.fetch_interval = _fetch_interval;
 		this.job_fetch_interval = _job_fetch_interval;
 		this.ref_datetime = _start_time;
 		
 		this.fetch_count_max = (int)(double)(10.0*60.0/(((double)this.fetch_interval+(double)this.job_fetch_interval)/2.0));
 		if(this.fetch_count_max > 150)
 			this.fetch_count_max = 150;
 		else if(this.fetch_count_max < 40)
 			this.fetch_count_max = 40;
 		
 		dbh = new DB_Handler();
 		if(dbh.prepareDB()) {
 			//retrieve existing tweet data from DB before fetching any new data
 			ArrayList<TwitterObject> ltweets = dbh.retrieveDBTweet();
 			dmp.addToTweets_all(ltweets);
 			dmp.updateRegionScoreForDBData();
 			return true;
 		}
 		else {
 			System.err.println("Error initializing connecting to database!");
 			return false;
 		}
 	}
 	
 	public void prepareForFetchingLess() throws Exception  {
 		dmp = new DataManipulationProcessor();
 	}
 	
 	public void fetchDataExample() throws Exception {
 		String start_time = dmp.toEpochTime("2013 Sep 29 23:11:04 UTC");
 		String end_time = dmp.toEpochTime("2013 Sep 29 23:11:06 UTC");
 		dmp.getJsonFromWoT(wot_type.twitter, start_time, end_time);
 		dmp.toListFromJsonParser(wot_type.twitter);
 	}
 	
 	public ArrayList<TweetInstance> fetchData() throws Exception {
 		//calculate start time and end time from ref_datetime
 		Date date_start_time = new Date(ref_datetime);
 		Date date_end_time = new Date(ref_datetime);
 		//set start time fetch interval earlier
 		date_start_time.setTime(date_start_time.getTime() - fetch_interval * 1000);
 		//format data to string with UTC
 		DateFormat date_format = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");
 		String start_time = date_format.format(date_start_time);
 		String end_time = date_format.format(date_end_time);
 		start_time += " PDT";
 		end_time += " PDT";
 		//update ref_date
 		Date date_ref_time = new Date(ref_datetime);
 		date_ref_time.setTime(date_ref_time.getTime() + fetch_interval * 1000);
 		//check if start time is greater than current time
 		//if it is, set current time to start time
 		Date date_current_time = new Date();
 		
 		if(date_ref_time.after(date_current_time))
 			date_ref_time = date_current_time;
 		String ref_time = date_format.format(date_ref_time);
 		ref_datetime = ref_time;
 		//get epoch time
 		String epoch_start_time = dmp.toEpochTime(start_time);
 		String epoch_end_time = dmp.toEpochTime(end_time);
 		date_format.setTimeZone(TimeZone.getTimeZone("UTC"));
 		Date date_UTC = new Date(ref_datetime);
 		String time_UTC = date_format.format(date_UTC);
 		System.out.println("Fetching data @ " + ref_datetime + "(UTC: " + time_UTC + ")");
 		//retrieve from WoT
 		dmp.getJsonFromWoT(wot_type.twitter, epoch_start_time, epoch_end_time);
 		dmp.toListFromJsonParser(wot_type.twitter);
 		//remove duplicates
 		//dmp.removeDuplicates(wot_type.twitter);
 		//convert twitterObject to TweetInstance for clustering
 		ArrayList<TweetInstance> linstance = dmp.toWekaInstanceFromTwitterObj();
 		//update current all list
 		dmp.updateAllList(wot_type.twitter);
 		//write to DB
 		dbh.writeToDBTweet(dmp.gettweets_incoming());
 		
 		return linstance;
 	}
 	
 	//public ArrayList<TwitterObject> fetchNewData() throws Exception {
 	public String fetchNewData() throws Exception {
 		
 		//calculate start time and end time from ref_datetime
 		Date date_start_time = new Date(ref_datetime);
 		Date date_end_time = new Date(ref_datetime);
 		//set start time fetch interval earlier
 		date_start_time.setTime(date_start_time.getTime() - fetch_interval * 1000);
 		//format data to string with UTC
 		DateFormat date_format = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");
 		String start_time = date_format.format(date_start_time);
 		String end_time = date_format.format(date_end_time);
 		start_time += " PDT";
 		end_time += " PDT";
 		//update ref_date
 		Date date_ref_time = new Date(ref_datetime);
 		date_ref_time.setTime(date_ref_time.getTime() + fetch_interval * 1000);
 		//check if start time is greater than current time
 		//if it is, set current time to start time
 		Date date_current_time = new Date();
 		
 		if(date_ref_time.after(date_current_time))
 			date_ref_time = date_current_time;
 		String ref_time = date_format.format(date_ref_time);
 		ref_datetime = ref_time;
 		//get epoch time
 		String epoch_start_time = dmp.toEpochTime(start_time);
 		String epoch_end_time = dmp.toEpochTime(end_time);
 		date_format.setTimeZone(TimeZone.getTimeZone("UTC"));
 		Date date_UTC = new Date(ref_datetime);
 		String time_UTC = date_format.format(date_UTC);
 		System.out.println("Fetching data @ " + ref_datetime + "(UTC: " + time_UTC + ")");
 		//retrieve from WoT
 		dmp.getJsonFromWoT(wot_type.twitter, epoch_start_time, epoch_end_time);
 		dmp.toListFromJsonParser(wot_type.twitter);
 		//remove duplicates
 		//dmp.removeDuplicates(wot_type.twitter);
 		
 		System.out.println("fetch count: " + fetch_count);
 		//write average scores to database every n fetches for timeplay feature
 		fetch_count++;
 		if(fetch_count > fetch_count_max) {
 			fetch_count=0;
 			ArrayList<regionX> lRegions = dmp.getCurrentlRegionObjectsForTimePlay();
 			dbh.writeToDBScores(lRegions);
 		}
 		
 		//get all new incoming tweets
 		ArrayList<TwitterObject> ltweets_new = dmp.gettweets_incoming();
 		if(ltweets_new.size() == 0) {
 			System.out.println("No new tweets detected in this loop, returning empty...");
 			return "";
 		}
 		//update current all list
		//dmp.updateAllList(wot_type.twitter);
 		//write to DB
 		dbh.writeToDBTweet(dmp.gettweets_incoming());
		//clear new incoming tweets
		dmp.clear_tweet_incoming();
 		//return ltweets_new;
 		return dmp.getlJsonRegionObject();
 	}
 	
 	public WeatherObject getWeatherFromLatLng(double _lat, double _lng) throws Exception {
 		//retrive weather json from web
 		dmp.getJsonFromWeb(web_type.weather, _lat, _lng);
 		WeatherObject weather_condition = dmp.toWeatherFromJsonParser(web_type.weather);
 		//System.out.println("Weather data @ cluster centre: " + weather_condition.printInfo());
 		return weather_condition;
 	}
 	
 	
 	
 }
