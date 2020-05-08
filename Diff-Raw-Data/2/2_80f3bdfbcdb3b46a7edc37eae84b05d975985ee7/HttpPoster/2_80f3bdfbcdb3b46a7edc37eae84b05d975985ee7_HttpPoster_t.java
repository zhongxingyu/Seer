 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.ePark.http_json;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import net.sf.json.JSONObject;
 
 //import net.sf.json.JSONException;
 /**
  *
  * @author pantelis
  */
 public class HttpPoster {
 
     private String url = "";
     private String urlParameters;
     private int responseCode = 0;
     private HttpURLConnection con = null;
     private int connectTimeout = Config.connectTimeout;
     private int readTimeout = Config.readTimeout;
 
 
     /*
      public HttpPoster() {
      System.out.println("Creating HttpPoster object");
      System.out.println("Timeout is: " + connectTimeout);
      }
      */
     private void formatUrl(String request_type, JSONObject jsonIn) throws MessageTypeException {
 
         StringBuilder paramsBuild = new StringBuilder();
         if (request_type.equals("event")) {
             switch (jsonIn.get("message_type").toString()) {
                 case "IN":
                 case "INOFFLINE":
                     paramsBuild.append("message_type=" + jsonIn.get("message_type"));
                     paramsBuild.append("&version=" + jsonIn.get("version"));
                     paramsBuild.append("&parking_code=" + jsonIn.get("parking_code"));
                     paramsBuild.append("&tag_identifier=" + jsonIn.get("tag_identifier"));
                     paramsBuild.append("&time_in=" + jsonIn.get("time_in"));
                     paramsBuild.append("&reader_code=" + jsonIn.get("reader_code"));
                     break;
                 case "OUT":
                 case "OUTOFFLINE":
                     paramsBuild.append("message_type=" + jsonIn.get("message_type"));
                     paramsBuild.append("&version=" + jsonIn.get("version"));
                     paramsBuild.append("&ticket_number=" + jsonIn.get("ticket_number"));
                     paramsBuild.append("&parking_code=" + jsonIn.get("parking_code"));
                     paramsBuild.append("&tag_data=" + jsonIn.get("tag_data"));
                     paramsBuild.append("&time_out=" + jsonIn.get("time_out"));
                     paramsBuild.append("&reader_code=" + jsonIn.get("reader_code"));
                     paramsBuild.append("&tag_identifier=" + jsonIn.get("tag_identifier"));
                     break;
                 default:
                     throw new MessageTypeException("Malformed message type");
 
             }
         } else if (request_type.equals("au")) {
             // AvailabilityUpdate ws version from Config.java
             paramsBuild.append("version=" + Config.availabilityUpdateVersion);
             paramsBuild.append("&parking_code=" + jsonIn.getString("parking_code"));
             paramsBuild.append("&add_incoming=" + jsonIn.getString("add_incoming"));
             paramsBuild.append("&add_outgoing=" + jsonIn.getString("add_outgoing"));
             paramsBuild.append("&reader_code=" + jsonIn.getString("reader_code"));
 
         }
         urlParameters = paramsBuild.toString();
     }
 
     private JSONObject postEvent(String request_type, JSONObject jsonIn) throws java.net.SocketTimeoutException, MessageTypeException,
             java.net.MalformedURLException, IOException, ParkingException {
 
         formatUrl(request_type, jsonIn);
         url = Config.url;
         if (request_type.equals("event")) {
             switch (jsonIn.get("message_type").toString()) {
                 case "IN":
                 case "INOFFLINE":
                     url = url + Config.arrivalOperation;
                     break;
                 case "OUT":
                 case "OUTOFFLINE":
                     url = url + Config.departureOperation;
                     break;
                 default:
                     throw new MessageTypeException("Malformed message type");
             }
         } else if (request_type.equals("au")) {
             url = url + Config.availabilityUpdate;
         }
         System.out.println("URL is: " + url);
         
         URL obj = new URL(url);
         con = (HttpURLConnection) obj.openConnection();
         //add reuqest header
         con.setRequestMethod("POST");
         con.setConnectTimeout(connectTimeout);
         con.setReadTimeout(readTimeout);
         //con.setRequestProperty("Content-Type", "application/json");
         con.setDoOutput(true);
         DataOutputStream wr = new DataOutputStream(con.getOutputStream());
         wr.writeBytes(urlParameters);
         wr.flush();
         wr.close();
 
         responseCode = con.getResponseCode();
 
         System.out.println("\nSending 'POST' request to URL : " + url);
         //System.out.println("Post parameters : " + urlParameters);
         System.out.println("Response Code : " + responseCode);
         BufferedReader in;
         if (responseCode == 200) {
             in = new BufferedReader(
                     new InputStreamReader(con.getInputStream()));
         } else {
             in = new BufferedReader(
                     new InputStreamReader(con.getErrorStream()));
         }
         String inputLine;
         StringBuilder response = new StringBuilder();
 
         while ((inputLine = in.readLine()) != null) {
             response.append(inputLine);
         }
         in.close();
 
 
         JSONObject jsonResponse = JSONObject.fromObject(response.toString());
        if (jsonResponse.getInt("response_code") != 1) {
             throw new ParkingException(jsonResponse.getString("response_message"));
         }
         return jsonResponse;
     }
 
     public ArrivalResponse postArrival(String message_type, String version,
             String parking_code, String tag_identifier, String tag_data, String time_in, String reader_code) throws java.net.SocketTimeoutException,
             MessageTypeException, java.net.MalformedURLException, java.io.IOException, ParkingException {
 
         JSONObject jsonArrival = new JSONObject();
         jsonArrival.accumulate("message_type", message_type);
         jsonArrival.accumulate("version", version);
         jsonArrival.accumulate("parking_code", parking_code);
         jsonArrival.accumulate("tag_identifier", tag_identifier);
         jsonArrival.accumulate("tag_data", tag_data);
         jsonArrival.accumulate("time_in", time_in);
         jsonArrival.accumulate("reader_code", reader_code);
 
         JSONObject jsonResponse = postEvent("event", jsonArrival);
         return new ArrivalResponse(jsonResponse);
 
     }
 
     public DepartureResponse postDeparture(String message_type, String version,
             String parking_code, String tag_identifier, String tag_data, String time_out, String reader_code, String ticket_number) throws java.net.SocketTimeoutException,
             MessageTypeException, java.net.MalformedURLException, java.io.IOException, ParkingException {
 
         JSONObject jsonDeparture = new JSONObject();
         jsonDeparture.accumulate("message_type", message_type);
         jsonDeparture.accumulate("version", version);
         jsonDeparture.accumulate("parking_code", parking_code);
         jsonDeparture.accumulate("tag_identifier", tag_identifier);
         jsonDeparture.accumulate("tag_data", tag_data);
         jsonDeparture.accumulate("time_out", time_out);
         jsonDeparture.accumulate("reader_code", reader_code);
         jsonDeparture.accumulate("ticket_number", ticket_number);
         
         JSONObject jsonResponse = postEvent("event", jsonDeparture);
         return new DepartureResponse(jsonResponse);
 
     }
 
     public AUResponse postAvailabilityUpdate(String parking_code, String add_incoming, String add_outgoing, String reader_code) throws java.net.SocketTimeoutException,
             MessageTypeException, java.net.MalformedURLException, java.io.IOException, ParkingException {
         JSONObject jsonAvailabilityUpdate = new JSONObject();
         //jsonAvailabilityUpdate.accumulate("version", Config.availabilityUpdateVersion);
         jsonAvailabilityUpdate.accumulate("parking_code", parking_code);
         jsonAvailabilityUpdate.accumulate("add_incoming", add_incoming);
         jsonAvailabilityUpdate.accumulate("add_outgoing", add_outgoing);
         jsonAvailabilityUpdate.accumulate("reader_code", reader_code);
         
         JSONObject jsonResponse = postEvent("au", jsonAvailabilityUpdate);
         return new AUResponse(jsonResponse);
 
     }
 }
