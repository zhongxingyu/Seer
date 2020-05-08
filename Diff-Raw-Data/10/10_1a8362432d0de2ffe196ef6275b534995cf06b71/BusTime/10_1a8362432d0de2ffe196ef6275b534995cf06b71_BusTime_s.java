 // Copyright (C) 2013 Christopher "Kasoki" Kaster (@Kasoki)
 // 
 // This file is part of "SWT-Realtime". <http://github.com/Kasoki/SWT-Realtime>
 //
 // Neither this library nor the author are somehow related to SWT (Stadtwerke Trier)!
 // 
 // Permission is hereby granted, free of charge, to any person obtaining a
 // copy of this software and associated documentation files (the "Software"),
 // to deal in the Software without restriction, including without limitation
 // the rights to use, copy, modify, merge, publish, distribute, sublicense,
 // and/or sell copies of the Software, and to permit persons to whom the
 // Software is furnished to do so, subject to the following conditions:
 // 
 // The above copyright notice and this permission notice shall be included
 // in all copies or substantial portions of the Software.
 // 
 // THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 // OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 // FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 // AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 // LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 // OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN 
 // THE SOFTWARE.
 package de.kasoki.swtrealtime;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 /**
  * Get real time information from SWT. Neither this library nor the author are related to SWT.
  * Special Thanks to Daniel Fett (@dfett42) for the sniffing.
  * @author Christopher Kaster (@Kasoki)
  */
 public class BusTime {
 	
 	private int number;
 	private String destination;
 	private Date arrivalTime;
 	private Date expectedArrivalTime;
 	
 	private static SimpleDateFormat dateFormat;
 	
 	private static final int MAJOR_VERSION = 0;
 	private static final int MINOR_VERSION = 1;
	private static final int PATCH_VERSION = 3;
 	
 	static {
		dateFormat = new SimpleDateFormat("HH:MM");
 	}
 	
 	private BusTime(int number, String destination, Date arrivalTime, Date expectedArrivalTime) {
 		this.number = number;
 		this.destination = destination;
 		this.arrivalTime = arrivalTime;
 		this.expectedArrivalTime = expectedArrivalTime;
 	}
 	
 	@Override
 	public String toString() {
 		return getNumber() + ": " + getDestination() + " [Arrival Time: " +
 				dateFormat.format(arrivalTime) + " Delay: " + getDelay() + "m ]";
 	}
 	
 	public int getNumber() {
 		return this.number;
 	}
 	
 	public String getDestination() {
 		return this.destination;
 	}
 	
 	public Date getArrivalTime() {
 		return this.arrivalTime;
 	}
 	
 	public String getArrivalTimeAsString() {
 		return dateFormat.format(arrivalTime);
 	}
 	
 	public Date getExpectedArrivalTime() {
 		return this.expectedArrivalTime;
 	}
 	
 	public String getExpectedArrivalTimeAsString() {
 		return dateFormat.format(expectedArrivalTime);
 	}
 	
 	public int getDelay() {
 		long difference = expectedArrivalTime.getTime() - arrivalTime.getTime();
 		
 		int differenceMinutes = (int)(difference / (60 * 1000));
 		
 		return differenceMinutes;
 	}
 	
 	/**
 	 * Set the date format (Default: HH:MM)
 	 * @param dateFormat
 	 */
 	public static void setDateFormat(String dateFormat) {
 		setDateFormat(new SimpleDateFormat(dateFormat));
 	}
 	
 	/**
 	 * Set the date format (Default: HH:MM)
 	 * @param dateFormat
 	 */
 	public static void setDateFormat(SimpleDateFormat dateFormat) {
 		BusTime.dateFormat = dateFormat;
 	}
 	
 	/**
 	 * Creates a list with the next buses.
 	 * @param busStop The bus stop of interest ;).
 	 * @return
 	 */
 	public static List<BusTime> fromStopCode(BusStop busStop) {
 		return BusTime.fromStopCode(busStop.getStopCode());
 	}
 	
 	/**
 	 * Creates a list with the next buses.
 	 * @param stopCode The stop code of your bus stop.
 	 * @return
 	 */
 	public static List<BusTime> fromStopCode(String stopCode) {
 		String url = "http://212.18.193.124/onlineinfo/onlineinfo/stopData";
 		String charset = "UTF-8";
 		
 		String response = "";
 		
 		List<BusTime> busTimeList = null;
 		
 		try {
 			// setup connection
 			URLConnection connection = new URL(url).openConnection();
 			connection.setDoOutput(true); // triggers POST
 			
 			connection.setRequestProperty("Accept-Charset", charset);
 			connection.setRequestProperty("X-GWT-Module-Base", "http://212.18.193.124/onlineinfo/onlineinfo/");
 			connection.setRequestProperty("X-GWT-Permutation", "D8AB656D349DD625FC1E4BA18B0A253C");
 			connection.setRequestProperty("Content-Type", "text/x-gwt-rpc; charset=" + charset);
 			
 			String body = "5|0|6|http://212.18.193.124/onlineinfo/onlineinfo/|7E201FB9D23B0EA0BDBDC82C554E92FE|com.initka.onlineinfo.client.services.StopDataService|getDepartureInformationForStop|java.lang.String/2004016611|%s|1|2|3|4|1|5|6|";
 			body = String.format(body, stopCode);
 			
 			OutputStream output = connection.getOutputStream();
 			output.write(body.getBytes());
 			output.close();
 			
 			connection.connect();
 			
 			// read connection
 			InputStream input = connection.getInputStream();
 			
 			BufferedReader reader = new BufferedReader(new InputStreamReader(input, charset));
 			
 			for (String line; (line = reader.readLine()) != null;) {
 	            response += line;
 	        }
 			
 			reader.close();
 			
 			busTimeList = BusTime.parseResponse(response);
 			
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return busTimeList;
 	}
 	
 	private static List<BusTime> parseResponse(String response) throws Exception {
 		List<BusTime> busTimeList = new ArrayList<BusTime>();
 		
 		if(response.startsWith("//OK")) {
 			String jsonString = response.substring(4, response.length());
 			
 			try {
 				JSONArray json = new JSONArray(jsonString);
 				
 				JSONArray innerInformations = new JSONArray(json.get(json.length() - 3).toString());
 				
 				for(int i = 0; i < Math.floor(json.length() / 11); i++) {
 					
 					String numberString = getItemFromInnerInformationList(innerInformations, json.getInt(i * 11 + 5));
 					
 					try {
 						int number = Integer.parseInt(numberString);
 						String destination = getItemFromInnerInformationList(innerInformations, json.getInt(i * 11 + 6));
 						Date arrivalTime = new Date(json.getLong(i * 11 + 2) + json.getLong(i * 11 + 3));
 						Date expectedArrivalTime = new Date(json.getLong(i * 11 + 7) + json.getLong(i * 11 + 8));
 						
 						busTimeList.add(new BusTime(number, destination, arrivalTime, expectedArrivalTime));
 					} catch(Exception ex) {
 						// if this happens we've found a bus without destination or number... don't know what they
 						// are but they actually exist! Oo
 						continue;
 					}
 					
 				}
 			} catch(JSONException e) {
 				e.printStackTrace();
 			}
 		} else {
 			throw new Exception("Error: Invalid Response: " + response);
 		}
 		
 		return busTimeList;
 	}
 	
 	private static String getItemFromInnerInformationList(JSONArray innerInformations, int index) {
 		return innerInformations.getString(index - 1);
 	}
 	
 	public static String getVersion() {
 		return MAJOR_VERSION + "." + MINOR_VERSION + "." + PATCH_VERSION;
 	}
 	
 	/**
 	 * This is a test method which prints the next buses coming to HBF.
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		List<BusTime> list = BusTime.fromStopCode(BusStop.HAUPTBAHNHOF);
 		
 		for(BusTime busTime : list) {
 			System.out.println(busTime);
 		}
 	}
 }
