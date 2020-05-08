 package com.objects;
 
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.Date;
 
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.JsonGenerator;
 import org.codehaus.jackson.JsonParser;
 
 import android.content.SharedPreferences;
 
 /**
  * This object is used to store user preferences and such, passed between activities inside a bundle.  
  * Remember to generate getters and setters
  * */
 public class ServerCalls {
 
 	public static final JsonFactory JSON_FACTORY = new JsonFactory();
 	private static final String SERVER_HOSTNAME = "http://75.101.132.219:8080";
 
 	/*returns if authentication passed.*/
 	public static UserObject getUser(String email, String hash){
 		try {
 			//open connection
 			final HttpURLConnection conn =
 					(HttpURLConnection)
 					new URL(SERVER_HOSTNAME + "/parkservice.auth")
 			.openConnection();
 			conn.setRequestProperty("Accept", "application/json");
 			conn.setRequestProperty("Content-Type", "application/json");
 			conn.setDoOutput(true);
 			//write data
 			final JsonGenerator jg = JSON_FACTORY.createJsonGenerator(conn.getOutputStream());
 			jg.writeStartObject();
 			jg.writeFieldName("email");
 			jg.writeString(email);
 			jg.writeFieldName("password");
 			jg.writeString(hash);
 			jg.writeEndObject();
 			jg.flush();
 			jg.close();
 			if (conn.getResponseCode() == 200) {
 				final JsonParser jp = JSON_FACTORY.createJsonParser(conn
 						.getInputStream());
 				final UserObject user = Parsers.parseUser(jp);
 				jp.close();
 				return user;
 			}
 		} catch (IOException e){
 			e.printStackTrace();
 		}
 		return null;
 
 	}
 
 	public static boolean registerNewUser(String email, String password, String name,
 			String ccNumber, String cscNumber, int expMonth, int expYear, String zip, String address) {
 		try {	
 			final HttpURLConnection conn =
 					(HttpURLConnection)
 					new URL(SERVER_HOSTNAME + "/parkservice.user/register")
 			.openConnection();
 			conn.setRequestProperty("Accept", "application/json");
 			conn.setRequestProperty("Content-Type", "application/json");
 			conn.setDoOutput(true);
 			final JsonGenerator jg = JSON_FACTORY.createJsonGenerator(conn.getOutputStream());
 			jg.writeStartObject();
 			jg.writeFieldName("creditCard");
 			jg.writeString(ccNumber);
 			jg.writeFieldName("holderName");
 			jg.writeString(name);
 			jg.writeFieldName("cscNumber");
 			jg.writeString(cscNumber);
 			jg.writeFieldName("expMonth");
 			jg.writeNumber(expMonth);
 			jg.writeFieldName("expYear");
 			jg.writeNumber(expYear);
 			jg.writeFieldName("email");
 			jg.writeString(email);
 			jg.writeFieldName("password");
 			jg.writeString(password);
 			jg.writeFieldName("zipcode");
 			jg.writeString(zip);
 			jg.writeFieldName("address");
 			jg.writeString(address);
 			jg.writeEndObject();
 			jg.flush();
 			jg.close();
 			if (conn.getResponseCode() == 200) {
 				final JsonParser jp = JSON_FACTORY.createJsonParser(conn
 						.getInputStream());
 				final boolean response = Parsers.parseResponseCode(jp);
 				jp.close();
 				return response;
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public static boolean unPark(long spotid, String parkingReferenceNumber, SharedPreferences prefs){
 		try {
 			final HttpURLConnection conn =
 					(HttpURLConnection)
 					new URL(SERVER_HOSTNAME + "/parkservice.park/unpark")
 			.openConnection();
 			conn.setRequestProperty("Accept", "application/json");
 			conn.setRequestProperty("Content-Type", "application/json");
 			conn.setDoOutput(true);
 
 			final JsonGenerator jg = JSON_FACTORY.createJsonGenerator(conn.getOutputStream());
 			jg.writeStartObject();
 			jg.writeFieldName("spotId");
 			jg.writeNumber(spotid);
 			jg.writeFieldName("parkingReferenceNumber");
 			jg.writeString(parkingReferenceNumber);
 			writeUserDetails(jg, prefs);
 			jg.writeEndObject();
 			jg.flush();
 			jg.close();
 			if (conn.getResponseCode() == 200) {
 				final JsonParser jp = JSON_FACTORY.createJsonParser(conn
 						.getInputStream());
 				final boolean response = Parsers.parseResponseCode(jp);
 				jp.close();
 				return response;
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public static RateResponse getRateGps(String spotNum, double lat, double lon, SharedPreferences prefs) {
 		try {
 			final HttpURLConnection conn =
 					(HttpURLConnection)
 					new URL(SERVER_HOSTNAME + "/parkservice.rate/gps")
 			.openConnection();
 			conn.setRequestProperty("Accept", "application/json");
 			conn.setRequestProperty("Content-Type", "application/json");
 			conn.setDoOutput(true);
 			final JsonGenerator jg = JSON_FACTORY.createJsonGenerator(conn.getOutputStream());
 			jg.writeStartObject();
 			jg.writeFieldName("lat");
 			jg.writeNumber(lat);
 			jg.writeFieldName("lon");
 			jg.writeNumber(lon);
 			jg.writeFieldName("spot");
 			jg.writeString(spotNum);
 			writeUserDetails(jg, prefs);
 			jg.writeEndObject();
 			jg.flush();
 			jg.close();
 			if (conn.getResponseCode() == 200) {
 				final JsonParser jp = JSON_FACTORY.createJsonParser(conn
 						.getInputStream());
 				final RateResponse rate = Parsers.parseRateResponse(jp);
 				jp.close();
 				return rate;
 			}
 		} catch (IOException e){
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public static RateResponse getRateQr(String qrcode, SharedPreferences prefs){
		///final String[] tokens = qrcode.split("/");
		final String[] tokens = qrcode.split("http://|/");
		//0 is empty, 1 is parqme.com, 2 is main_lot, 3 is 1412
		// qr code is url, http://parqme.com/main_lot/1412
		if (tokens.length != 4) {
 			return null;
 		}
 		try {
 			final HttpURLConnection conn =
 					(HttpURLConnection)
 					new URL(SERVER_HOSTNAME + "/parkservice.rate/qrcode")
 			.openConnection();
 			conn.setRequestProperty("Accept", "application/json");
 			conn.setRequestProperty("Content-Type", "application/json");
 			conn.setDoOutput(true);
 			final JsonGenerator jg = JSON_FACTORY.createJsonGenerator(conn.getOutputStream());
 			jg.writeStartObject();
 			jg.writeFieldName("lot");
 			jg.writeString(tokens[2]);
			jg.writeFieldName("spot");
			jg.writeString(tokens[3]);
 			writeUserDetails(jg, prefs);
 			jg.writeEndObject();
 			jg.flush();
 			jg.close();
 			if (conn.getResponseCode() == 200) {
 				final JsonParser jp = JSON_FACTORY.createJsonParser(conn
 						.getInputStream());
 				final RateResponse rate = Parsers.parseRateResponse(jp);
 				jp.close();
 				return rate;
 			}
 		} catch (IOException e){
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public static ParkInstanceObject park(int duration, RateObject spot, SharedPreferences prefs){
 		try {
 			final HttpURLConnection conn =
 					(HttpURLConnection)
 					new URL(SERVER_HOSTNAME + "/parkservice.park/park")
 			.openConnection();
 			conn.setRequestProperty("Accept", "application/json");
 			conn.setRequestProperty("Content-Type", "application/json");
 			conn.setDoOutput(true);
 			final JsonGenerator jg = JSON_FACTORY.createJsonGenerator(conn.getOutputStream());
 			jg.writeStartObject();
 			jg.writeFieldName("spotId");
 			jg.writeNumber(spot.getSpot());
 			jg.writeFieldName("durationMinutes");
 			jg.writeNumber(duration);
 			jg.writeFieldName("chargeAmount");
 			jg.writeNumber(duration / spot.getMinIncrement() * spot.getDefaultRate());
 			jg.writeFieldName("paymentType");
 			jg.writeNumber(0);
 			writeUserDetails(jg, prefs);
 			jg.writeEndObject();
 			jg.flush();
 			jg.close();
 			if (conn.getResponseCode() == 200) {
 				final JsonParser jp = JSON_FACTORY.createJsonParser(conn
 						.getInputStream());
 				final ParkInstanceObject response = Parsers.parseParkInstance(jp);
 				jp.close();
 				return response;
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
     public static ParkInstanceObject refill(int duration, RateObject spot, String parkId, SharedPreferences prefs){
         try {
             final HttpURLConnection conn =
                     (HttpURLConnection)
                     new URL(SERVER_HOSTNAME + "/parkservice.park/refill")
             .openConnection();
             conn.setRequestProperty("Accept", "application/json");
             conn.setRequestProperty("Content-Type", "application/json");
             conn.setDoOutput(true);
             final JsonGenerator jg = JSON_FACTORY.createJsonGenerator(conn.getOutputStream());
             jg.writeStartObject();
             jg.writeFieldName("parkingReferenceNumber");
             jg.writeString(parkId);
             jg.writeFieldName("spotId");
             jg.writeNumber(spot.getSpot());
             jg.writeFieldName("durationMinutes");
             jg.writeNumber(duration);
             jg.writeFieldName("chargeAmount");
             jg.writeNumber(duration / spot.getMinIncrement() * spot.getDefaultRate());
             jg.writeFieldName("paymentType");
             jg.writeNumber(0);
             writeUserDetails(jg, prefs);
             jg.writeEndObject();
             jg.flush();
             jg.close();
             if (conn.getResponseCode() == 200) {
                 final JsonParser jp = JSON_FACTORY.createJsonParser(conn
                         .getInputStream());
                 final ParkInstanceObject response = Parsers.parseParkInstance(jp);
                 jp.close();
                 return response;
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
         return null;
     }
 	public static int addHistory(String date, String starttime, String endtime, String location, String email, int cost){
 		try {
 			String data = URLEncoder.encode("date", "UTF-8") + "=" + URLEncoder.encode(date, "UTF-8");
 			data+= "&"+URLEncoder.encode("location", "UTF-8") + "=" + URLEncoder.encode(location, "UTF-8");
 			data+= "&"+URLEncoder.encode("starttime", "UTF-8") + "=" + URLEncoder.encode(starttime, "UTF-8");
 			data+= "&"+URLEncoder.encode("endtime", "UTF-8") + "=" + URLEncoder.encode(endtime, "UTF-8");
 			data+= "&"+URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8");
 			data+= "&"+URLEncoder.encode("cost", "UTF-8") + "=" + URLEncoder.encode(cost+"", "UTF-8");
 			// Send data
 			URL url = new URL("http://parqme.com/add_history.php");
 			URLConnection conn = url.openConnection();
 
 			conn.setDoOutput(true);
 			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
 			wr.write(data);
 			wr.flush();
 			//write data
 			int bytesRead = 1;
 			byte[] buffer = new byte[1];
 			//prepare buffers
 			BufferedInputStream bufferedInput = new BufferedInputStream(conn.getInputStream());
 			bufferedInput.read(buffer);
 			//read into buffer
 			String x =(new String(buffer, 0,bytesRead));
 			return Integer.parseInt(x);
 		}catch (Exception e){
 			e.printStackTrace();
 		}
 		return -1;
 	}
 	public static String test(String qrcode, String email, String endtime){
 		try {
 			String data = URLEncoder.encode("code", "UTF-8") + "=" + URLEncoder.encode(qrcode, "UTF-8");
 			data+= "&"+URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8");
 			data+= "&"+URLEncoder.encode("endtime", "UTF-8") + "=" + URLEncoder.encode(endtime, "UTF-8");
 			// Send data
 			URL url = new URL("http://localhost:8080/UserBounce/UpdateDatabase");
 			URLConnection conn = url.openConnection();
 
 			conn.setRequestProperty("fromApp", "yes");
 
 			conn.setDoOutput(true);
 			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
 			wr.write(data);
 			wr.flush();
 			//write data
 			int bytesRead = 1024;
 			byte[] buffer = new byte[1];
 			//prepare buffers
 			BufferedInputStream bufferedInput = new BufferedInputStream(conn.getInputStream());
 			bufferedInput.read(buffer);
 			//read into buffer
 			String x =(new String(buffer, 0,bytesRead));
 			return x;
 		}catch (Exception e){
 			e.printStackTrace();
 		}
 		return "blah";
 	}
 	private static void writeUserDetails(JsonGenerator jg, SharedPreferences prefs) throws JsonGenerationException, IOException {
 		final long uid = prefs.getLong("uid", -1);
 		final String email = prefs.getString("email", "");
 		final String password = prefs.getString("password", "");
 		jg.writeFieldName("uid");
 		jg.writeNumber(uid);
 		jg.writeFieldName("userInfo");
 		jg.writeStartObject();
 		jg.writeFieldName("email");
 		jg.writeString(email);
 		jg.writeFieldName("password");
 		jg.writeString(password);
 		jg.writeEndObject();
 	}
 }
