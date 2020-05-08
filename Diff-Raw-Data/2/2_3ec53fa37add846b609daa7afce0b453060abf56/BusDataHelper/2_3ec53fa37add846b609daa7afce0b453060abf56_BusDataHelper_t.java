 /*
  * Copyright 2010 Andrew De Quincey -  adq@lidskialf.net
  * This file is part of rEdBus.
  *
  *  rEdBus is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  rEdBus is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with rEdBus.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.redbus;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.protocol.HTTP;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.os.AsyncTask;
 import android.util.Log;
 import android.util.Xml;
 
 public class BusDataHelper {
 	
 	public static final int BUSSTATUS_HTTPERROR = -1;
 	public static final int BUSSTATUS_BADSTOPCODE = -2;
 	public static final int BUSSTATUS_BADDATA = -3;
 
 	private static Pattern stopDetailsRegex = Pattern.compile("([0-9]+)\\s+([^/]+).*");
 	private static Pattern destinationRegex = Pattern.compile("(\\S+)\\s+(.*)");
 	private static Pattern destinationAndTimeRegex = Pattern.compile("(\\S+)\\s+(.*)\\s+(\\S+)");
 	private static SimpleDateFormat advanceTimeFormat = new SimpleDateFormat("HH:mm");
 
 	private static Integer RequestId = new Integer(0);
 
 	public static int getBusTimesAsync(long stopCode, int daysInAdvance, Date timeInAdvance, BusDataResponseListener callback)
 	{
 		int requestId = RequestId++;
 		
 		new AsyncHttpRequestTask().execute(new BusDataRequest(requestId, 
				buildURL(stopCode, daysInAdvance, timeInAdvance, 2), 
 				BusDataRequest.REQ_BUSTIMES, 
 				callback));
 		
 		return requestId;
 	}
 	
 	public static int getStopNameAsync(long stopCode, BusDataResponseListener callback)
 	{
 		int requestId = RequestId++;
 		
 		new AsyncHttpRequestTask().execute(new BusDataRequest(requestId, 
 				buildURL(stopCode, 1, "09:00", 0), 
 				BusDataRequest.REQ_STOPNAME, 
 				callback));
 		
 		return requestId;
 	}
 	
 	private static URL buildURL(long stopCode, int daysInAdvance, Object timeInAdvance, int departureCount)
 	{
 		String time = "";
 		if (timeInAdvance != null) {
 			if (timeInAdvance instanceof Date)
 				time = advanceTimeFormat.format(timeInAdvance);
 			else
 				time = timeInAdvance.toString();
 		} else {
 			daysInAdvance = 0;
 		}
 
 		StringBuilder result = new StringBuilder("http://www.mybustracker.co.uk/getBusStopDepartures.php");
 		result.append("?refreshCount=0");
 		result.append("&clientType=b");
 		result.append("&busStopDay=").append(daysInAdvance);
 		result.append("&busStopService=0");
 		result.append("&numberOfPassage=").append(departureCount);
 		result.append("&busStopTime=").append(time);
 		result.append("&busStopDestination=0");
 		result.append("&busStopCode=").append(stopCode);
 		
 		try {
 			return new URL(result.toString());
 		} catch (MalformedURLException e) {
 			Log.e("BusDataHelper", "Malformed URL reported: " + result.toString());
 		}
 		
 		return null;
 	}
 	
 	private static void getBusTimesResponse(BusDataRequest request)
 	{
 		if (request.throwable != null) {
 			Log.e("BusDataHelper.GetBusTimesResponse(HTTPERROR)", request.content, request.throwable);
 			request.callback.getBusTimesError(request.requestId, BUSSTATUS_HTTPERROR, "A network problem occurred (" + request.throwable.getMessage() + ")");
 			return;
 		}
 		if (request.responseCode != HttpURLConnection.HTTP_OK) {
 			if (request.responseMessage != null)
 				Log.e("BusDataHelper.GetBusTimesResponse(HTTPRESPONSE)", request.responseMessage);
 			request.callback.getBusTimesError(request.requestId, request.responseCode, "A network problem occurred (" + request.responseMessage + ")");
 			return;
 		}
 		if (request.content.toLowerCase().contains("doesn't exist")) {
 			request.callback.getBusTimesError(request.requestId, BUSSTATUS_BADSTOPCODE, "The BusStop code was invalid");
 			return;
 		}
 		
 		ArrayList<BusTime> busTimes = new ArrayList<BusTime>();
 		try {
 			XmlPullParser parser = Xml.newPullParser();
 			parser.setInput(new StringReader(request.content));
 			
 			while(parser.next() != XmlPullParser.END_DOCUMENT) {
 				switch(parser.getEventType()) {
 				case XmlPullParser.START_TAG:
 					String tagName = parser.getName();
 					if (tagName == "pre")
 						busTimes.add(parseStopTime(parser));
 				}
 			}
 			
 		} catch (Exception ex) {
 			Log.e("BusDataHelper.GetBusTimesResponse", request.content, ex);
 			request.callback.getBusTimesError(request.requestId, BUSSTATUS_BADDATA, "Invalid data received from the bus website (" + ex.getMessage() + ")");
 			return;
 		}
 		
 		request.callback.getBusTimesSuccess(request.requestId, busTimes);
 	}
 	
 	private static BusTime parseStopTime(XmlPullParser parser) 
 		throws XmlPullParserException, IOException
 	{
 		String rawDestination = parser.nextText();
 		String rawTime = null;
 		
 		String service =  null;
 		String destination = null;
 		boolean lowFloorBus =  false;
 		boolean arrivalEstimated = false;
 		boolean arrivalIsDue= false;
 		int arrivalMinutesLeft = -1;
 		String arrivalAbsoluteTime = null;
 		
 		boolean done = false;
 		int tagDepth = 0;
 		while(!done) {
 			switch(parser.next()) {
 			case XmlPullParser.END_TAG:
 				if (tagDepth == 0)
 					done = true;
 				tagDepth--;
 				break;
 			case XmlPullParser.START_TAG:
 				tagDepth++;
 				if (parser.getName().equalsIgnoreCase("span")) {
 					String classAttr = parser.getAttributeValue(null, "class");
 					if (classAttr.equalsIgnoreCase("handicap"))
 						lowFloorBus = true;
 				}
 				break;
 			case XmlPullParser.TEXT:
 				if (tagDepth == 0)
 					rawTime = parser.getText().trim();
 				break;
 			}
 		}
 		
 		// parse the rawDestination
 		if (rawTime == null) {
 			Matcher m = destinationAndTimeRegex.matcher(rawDestination.trim());
 			if (m.matches()) {
 				service = m.group(1).trim();
 				destination = m.group(2).trim();
 				rawTime = m.group(3).trim();
 			} else {
 				throw new RuntimeException("Failed to parse rawTime");
 			}
 		} else {
 			Matcher m = destinationRegex.matcher(rawDestination.trim());
 			if (m.matches()) {
 				service = m.group(1).trim();
 				destination = m.group(2).trim();
 			} else {
 				throw new RuntimeException("Failed to parse destination");
 			}
 		}
 
 		// parse the rawTime
 		if (rawTime.startsWith("*")) {
 			arrivalEstimated = true;
 			rawTime = rawTime.substring(1).trim();
 		}
 		if (rawTime.equalsIgnoreCase("due"))
 			arrivalIsDue = true;
 		else if (rawTime.contains(":"))
 			arrivalAbsoluteTime = rawTime;
 		else
 			arrivalMinutesLeft = Integer.parseInt(rawTime);
 
 		return new BusTime(service, destination, lowFloorBus, arrivalEstimated, arrivalIsDue, arrivalMinutesLeft, arrivalAbsoluteTime);
 	}
 
 	private static void getStopNameResponse(BusDataRequest request)
 	{
 		if (request.throwable != null) {
 			Log.e("BusDataHelper.GetBusTimesResponse(HTTPERROR)", request.content, request.throwable);
 			request.callback.getStopNameError(request.requestId, BUSSTATUS_HTTPERROR, "A network problem occurred (" + request.throwable.getMessage() + ")");
 			return;
 		}
 		if (request.responseCode != HttpURLConnection.HTTP_OK) {
 			if (request.responseMessage != null)
 				Log.e("BusDataHelper.GetBusTimesResponse(HTTPRESPONSE)", request.responseMessage);
 			request.callback.getStopNameError(request.requestId, request.responseCode, "A network problem occurred (" + request.responseMessage + ")");
 			return;
 		}
 		if (request.content.toLowerCase().contains("doesn't exist")) {
 			request.callback.getStopNameError(request.requestId, BUSSTATUS_BADSTOPCODE, "The BusStop code was invalid");
 			return;
 		}
 
 		long stopCode = -1;
 		String stopName = null;
 		try {
 			XmlPullParser parser = Xml.newPullParser();
 			parser.setInput(new StringReader(request.content));
 			
 			boolean done = false;
 			while((!done) && (parser.next() != XmlPullParser.END_DOCUMENT)) {
 				switch(parser.getEventType()) {
 				case XmlPullParser.START_TAG:
 					String tagName = parser.getName();
 					if (tagName == "a") {
 						String tmp = parser.nextText().trim();
 						Matcher m = stopDetailsRegex.matcher(tmp);
 						if (m.matches()) {
 							stopCode = Long.parseLong(m.group(1).trim());
 							stopName = m.group(2).trim();
 						} else {
 							throw new RuntimeException("Failed to parse BusStop details");
 						}
 
 						done = true;
 						break;
 					}
 				}
 			}
 			
 		} catch (Exception ex) {
 			Log.e("BusDataHelper.GetStopNameResponse", request.content, ex);
 			request.callback.getStopNameError(request.requestId, BUSSTATUS_BADDATA, "Invalid data received from the bus website (" + ex.getMessage() + ")");
 			return;
 		}
 		
 		request.callback.getStopNameSuccess(request.requestId, stopCode, stopName);
 	}
 
 	
 	private static class AsyncHttpRequestTask extends AsyncTask<BusDataRequest, Integer, BusDataRequest> {
 		
 		protected BusDataRequest doInBackground(BusDataRequest... params) {
 			BusDataRequest bdr = params[0];
 			
 			InputStreamReader reader = null;
 			try {
 				// make the request and check the response code
 				HttpURLConnection connection = (HttpURLConnection) bdr.url.openConnection();
 				bdr.responseCode = connection.getResponseCode();
 				if (bdr.responseCode != HttpURLConnection.HTTP_OK) {
 					bdr.responseMessage = connection.getResponseMessage();
 					return bdr;
 				}
 				
 				// figure out the content encoding
 				String charset = connection.getContentEncoding();
 				if (charset == null)
 					charset = HTTP.DEFAULT_CONTENT_CHARSET;
 				
 				// read the request data
 				reader = new InputStreamReader(connection.getInputStream(), charset);
 				StringBuilder result = new StringBuilder();
 				char[] buf= new char[1024];
 				while(true) {
 					int len = reader.read(buf);
 					if (len < 0)
 						break;
 					result.append(buf, 0, len);
 				}
 				bdr.content = result.toString();
 			} catch (Throwable t) {
 				bdr.throwable = t;
 			} finally {
 				if (reader != null)
 					try {
 						reader.close();
 					} catch (IOException e) {
 					}
 			}
 			
 			return bdr;
 		}
 
 		protected void onPostExecute(BusDataRequest request) {
 			switch(request.requestType) {
 			case BusDataRequest.REQ_BUSTIMES:
 				BusDataHelper.getBusTimesResponse(request);			
 				break;
 			case BusDataRequest.REQ_STOPNAME:
 				BusDataHelper.getStopNameResponse(request);			
 				break;
 			}
 		}
 	}
 	
 	private static class BusDataRequest {
 		
 		public static final int REQ_BUSTIMES = 0;
 		public static final int REQ_STOPNAME = 1;
 		
 		public BusDataRequest(int requestId, URL url, int requestType, BusDataResponseListener callback)
 		{
 			this.requestId = requestId;
 			this.url = url;
 			this.requestType = requestType;
 			this.callback = callback;
 		}
 		
 		public int requestId;
 		public URL url;
 		public int requestType;
 		public BusDataResponseListener callback;
 		public int responseCode = -1;
 		public String responseMessage = null;
 		public String content = null;
 		public Throwable throwable = null;
 	}
 }
