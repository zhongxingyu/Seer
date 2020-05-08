 package ca.ubc.magic.enph479;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.TimeZone;
 
 import ca.ubc.magic.enph479.builder.TweetInstance;
 import ca.ubc.magic.enph479.builder.TwitterObject;
 import ca.ubc.magic.enph479.builder.RegionObject.regionX;
 
 /**
  * DB_Handler is the database handler to talk to the mySQL database
  * @author richardlee@hotmail.ca
  *
  */
 public class DB_Handler {
 	
 	private String url = "jdbc:mysql://localhost:3306/ENPH479";
 	private String user = "root";
 	private String password = "";
 	private Boolean _skipDB = false;
     
     Connection con = null;
 
 	public boolean prepareDB() throws Exception  {
 		
 		if(_skipDB)
 			return true;
 		
 		try {
 			con = DriverManager.getConnection(url, user, password);
 			if(con == null)
 				return false;
 			else
 				return true;
 		} catch (SQLException ex) {
         	System.err.println(ex.getMessage());
         	return false;
 		}
 	}
 	
 	/*public ArrayList<TwitterObject> retrieveDBTweet() throws Exception {
 		
 		if(_skipDB)
 			return new ArrayList<TwitterObject>();
 		
 		try {
 			Statement st = con.createStatement();
 			ArrayList<TwitterObject> ltweets = new ArrayList<TwitterObject>();
 			
             String mysql_select_command = "SELECT * FROM tweet_data";
             ResultSet rs = st.executeQuery(mysql_select_command);
 
             while (rs.next()) {
 				int id = rs.getInt("id");
 				String timestamp = rs.getString("timestamp");
 				double lat = rs.getDouble("lat");
 				double lng = rs.getDouble("lng");
 				int sensor_id = rs.getInt("sensor_id");
 				String sensor_name = rs.getString("sensor_name");
 				String message = rs.getString("message");
 				int value = rs.getInt("value");
 				int sentimentPolarity = rs.getInt("sentimentPolarity");
 				double weatherScore = rs.getDouble("weatherScore");
 				int region = rs.getInt("region");
 				
 				TwitterObject tweet = new TwitterObject();
 				tweet.setId(id);
 				tweet.setTimestamp(timestamp);
 				tweet.setLatitude(lat);
 				tweet.setLongitude(lng);
 				tweet.setSensor_id(sensor_id);
 				tweet.setSensor_name(sensor_name);
 				tweet.setMessage(message);
 				tweet.setValue(value);
 				tweet.setSentiment(sentimentPolarity);
 				tweet .setWeatherScore(weatherScore);
 				tweet.setRegion(region);
 				
 				ltweets.add(tweet);
 			}
             
             return ltweets;
             
         } catch (SQLException ex) {
         	System.err.println("DB Retrieving Error: " + ex.getMessage());
         	return null;
         }
 	}*/
 	
 	public Boolean writeToDBTweet(ArrayList<TwitterObject> _ltweets_incoming) throws Exception {
 		
 		if(_skipDB)
 			return true;
 		
 		try {
 			
 			if(_ltweets_incoming.size() == 0)
 				return true;
 			
 			Statement st = con.createStatement();
 			
 			//retrieve max id to be used
 			String mysql_maxID_command = "SELECT MAX(db_id) AS db_id FROM tweet_data";
 			ResultSet rs = st.executeQuery(mysql_maxID_command);
 
 			int max_id = 0;
             if (rs.next()) {
 				max_id = rs.getInt("db_id");
             }
             else {
             	max_id = 0;
             }
             int cur_id = max_id + 1;
             
 			//String mysql_insert_command = "INSERT INTO tweet_data " + "VALUES (1, 1234, 'Jan 01 1800 23:59:59', 111, 111, 999, 'sensor_test', 'test test test', 999, -1)";
 			String mysql_insert_command = "INSERT INTO tweet_data VALUES";
 			
 			for(int i = 0; i < _ltweets_incoming.size(); i++) {
 				
 				//get rid of commas
 				String tmp = _ltweets_incoming.get(i).getMessage();
 				tmp = tmp.replace(",", ".");
 				tmp = tmp.replace("'", " ");
 				tmp = tmp.replace("\"", "\\\"");
 				tmp = "\"" + tmp + "\"";
 				_ltweets_incoming.get(i).setMessage(tmp);
 				
 				String tmp1 = _ltweets_incoming.get(i).getTimestamp();
 				tmp1 = tmp1.replace(",", ".");
 				tmp1 = tmp1.replace("\"", "");
 				tmp1 = tmp1.replace("'", " ");
 				tmp1 = tmp1.replace("\"", "\\\"");
 				tmp1 = "\"" + tmp1 + "\"";
 				_ltweets_incoming.get(i).setTimestamp(tmp1);
 				
 				String tmp2 = _ltweets_incoming.get(i).getSensor_name();
 				tmp2 = tmp2.replace(",", ".");
 				tmp2 = tmp2.replace("'", " ");
 				tmp2 = tmp2.replace("\"", "\\\"");
 				tmp2 = "\"" + tmp2 + "\"";
 				_ltweets_incoming.get(i).setSensor_name(tmp2);
 				
 				
 				mysql_insert_command += " ("
 						+ (cur_id + i) + ","
 						+ _ltweets_incoming.get(i).getId() + ", "
 						+ _ltweets_incoming.get(i).getTimestamp() + ", "
 						+ _ltweets_incoming.get(i).getLatitude() + ", "
 						+ _ltweets_incoming.get(i).getLongitude() + ", "
 						+ _ltweets_incoming.get(i).getSensor_id() + ", "
 						+ _ltweets_incoming.get(i).getSensor_name() + ", "
 						+ _ltweets_incoming.get(i).getMessage() + ", "
 						+ _ltweets_incoming.get(i).getValue() + ", "
 						+ _ltweets_incoming.get(i).getSentiment() + ", "
 						+ _ltweets_incoming.get(i).getWeather_score() + ", "
 						+ _ltweets_incoming.get(i).getRegion() + ")";
 						
 				if(i < _ltweets_incoming.size()-1)
 					mysql_insert_command += ",";
 			}
             
 			System.out.println("DB statement: " + mysql_insert_command);
 			st.executeUpdate(mysql_insert_command);
             return true;
             
         } catch (SQLException ex) {
         	System.err.println("DB Writting Error: " + ex.getMessage());
         	return false;
         }
 	}
 	
public Boolean writeToDBScores(ArrayList<regionX> lRegions, String currentTime) throws Exception {
 		
 		if(_skipDB)
 			return true;
 		
 		try {
 			
 			if(lRegions.size() == 0)
 				return true;
 			
 			Statement st = con.createStatement();
 			
 			//retrieve max id to be used
 			String mysql_maxID_command = "SELECT MAX(db_id) AS db_id FROM timeplay_data";
 			ResultSet rs = st.executeQuery(mysql_maxID_command);
 
 			int max_id = 0;
             if (rs.next()) {
 				max_id = rs.getInt("db_id");
             }
             else {
             	max_id = 0;
             }
             int cur_id = max_id + 1;
             
 			//String mysql_insert_command = "INSERT INTO timeplay_data " + "VALUES (1, 1, 'Jan 01 1800 23:59:59', 2, 4)";
 			String mysql_insert_command = "INSERT INTO timeplay_data VALUES";
 			
			/*
 			Date date_now = new Date();
 			DateFormat date_format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSS'Z'");
 			date_format.setTimeZone(TimeZone.getTimeZone("UTC"));
 			String start_datetime = date_format.format(date_now);
			*/
 			
 			for(int i = 0; i < lRegions.size(); i++) {
 				
 				mysql_insert_command += " ("
						+ (cur_id + i) + "," + i + "," + "\"" + currentTime + "\"" + ","
 						+ lRegions.get(i).sentiment_ave + ", " + lRegions.get(i).weather_ave + ")";
 						
 				if(i < lRegions.size()-1)
 					mysql_insert_command += ",";
 			}
             
 			st.executeUpdate(mysql_insert_command);
             return true;
             
         } catch (SQLException ex) {
         	System.err.println("DB Writting Error: " + ex.getMessage());
         	return false;
         }
 	}
 	
 	
 }
