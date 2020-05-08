 package ca.cumulonimbus.barometer;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 public class BarometerServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static String logName = "ca.cumulonimbus.barometer.BarometerServlet";
 	private static Logger log = Logger.getLogger(logName);
 	
 	String serverURL = "";  
 	String distributionServerURL = "";
 	
 	private static DatabaseHelper dh;
 	
 	public BarometerServlet() {
 		dh = new DatabaseHelper();
 	}
 	
 	private ArrayList<BarometerReading> bufferToPNDV = new ArrayList<BarometerReading>();
 	private int sendBufferLimit = 100;
 	
 	/**
 	 * Add to the list to send to PNDV. Send if the buffer is large (sendBufferLimit).
 	 * @param br
 	 */
 	public void addToPNDV(BarometerReading br) {
 		bufferToPNDV.add(br);
 		if (bufferToPNDV.size() >= sendBufferLimit) {
 			/*
 			 * To send this data securely and with minimal overhead:
 			 * 1. Send a Request to PNDV telling it we have the data.
 			 * 2. PNDV replies with its own Request.
 			 * 3. We respond with CSVd data in the Response
 			 */
 			
 			// Send the initial notice
 
 			DefaultHttpClient httpClient = new DefaultHttpClient();
 			HttpGet httpGet = new HttpGet(distributionServerURL);
 			try {
 				HttpResponse responseFromPNDV = httpClient.execute(httpGet);
 			} catch (ClientProtocolException cpe) {
 				log.info(cpe.getMessage());
 			} catch(IOException ioe) {
 				log.info(ioe.getMessage());
 			}
 		}
 	}
 	
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doPost(request, response);
 	}
 	
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// Get and process the parameters
 		Map<String, String[]> params = request.getParameterMap();
 		
 		// What type of request is this? Options are:
 		// 1. We're being sent new data
 		// 2. This is a download request.
 		//log.info("do post");
 		if(params.containsKey("download")) {
 			// This is #2.
 			//log.info("download req " + params.get("download")[0]);
 			if(params.get("download")[0].equals("all_data")) {
 				//log.info("all data");
 				// Download all the things!
 				// except don't.
 				//ArrayList<BarometerReading> allReadings = dh.getAllReadings();
 				
 				response.setContentType("text/html");
 				PrintWriter out = response.getWriter();
 				try {
 					//for(BarometerReading br : allReadings) {
 						//out.print(barometerReadingToWeb(br));
 					//}
 				} catch(Exception e) {
 					log(e.getMessage());
 				}
 
 				out.close();
 			} else if (params.get("download")[0].equals("recent_data")) {
 				//log(params.containsKey("days") + "");
 				if(params.containsKey("days")) {
 					String days = params.get("days")[0];
 					int numOfDays = Integer.valueOf(days);
 					
 					ArrayList<BarometerReading> recentReadings = dh.getRecentReadings(numOfDays);
 					
 					response.setContentType("text/html");
 					PrintWriter out = response.getWriter();
 					try {
 						for(BarometerReading br : recentReadings) {
 							out.print(barometerReadingToWeb(br));
 						}
 					} catch(Exception e) {
 						log(e.getMessage());
 					}
 					out.close();
 				}
 			} else if (params.get("download")[0].equals("local_data")) {
 				log("sending local_data");
 				double centerLat = Double.parseDouble(params.get("centerlat")[0]) / 1E6;
 				double centerLon = Double.parseDouble(params.get("centerlon")[0]) / 1E6;
 				double latSpan = Double.parseDouble(params.get("latspan")[0]) / 1E6;
 				double longSpan = Double.parseDouble(params.get("longspan")[0]) / 1E6;
 				//log.info("local data: " + centerLat + ", " + centerLon);
 				
 				ArrayList<Double> regionList = new ArrayList<Double>();
 				double lat1 = centerLat - latSpan / 2;
 				double lat2 = centerLat + latSpan / 2;
 				double lon1 = centerLon - longSpan / 2;
 				double lon2 = centerLon + longSpan / 2;
 				regionList.add(lat1);
 				regionList.add(lat2);
 				regionList.add(lon1);
 				regionList.add(lon2);
 				
 				long day = (1000 * 60 * 60 * 24 * 1);
 				long shortPeriod = (1000 * 60 * 60 * 6); // last six hours
 				long sinceWhen = Calendar.getInstance().getTimeInMillis() - shortPeriod;
 				// Get the visible readings
 				ArrayList<BarometerReading> recentReadings = dh.getReadingsWithinRegion(regionList, sinceWhen);
 				// Get the visible conditions
 				ArrayList<CurrentCondition> recentConditions = dh.getConditionsWithinRegion(regionList, sinceWhen);
 
 				log("sending " + recentReadings.size() + " readings and " + recentConditions.size() + " conditions" );
 				
 				// Send the Recent Readings
 				response.setContentType("text/html");
 				PrintWriter out = response.getWriter();
 				out.print("local_data return;");
 				for(BarometerReading br : recentReadings) {
 					out.print(barometerReadingToWeb(br));
 				}
 			
 				// separation
 				out.print("----------");
 				
 				// Send the Recent Conditions
 				for(CurrentCondition cc : recentConditions) {
 					out.print(currentConditionToWeb(cc));
 				}
 				
 				out.close();
 			} else if (params.get("download")[0].equals("full_delete_request")) {
 				String userID = params.get("userid")[0];
 				log.info("full delete request for: " + userID);
 				boolean success = dh.deleteUserData(userID);
 				response.setContentType("text/html");
 				PrintWriter out = response.getWriter();
 				out.print(success);
 				out.close();
 			} 
 		}  else if(params.containsKey("statistics")) {
 			log.info("statistics " + params.get("statistics")[0]);
 			if(params.get("statistics")[0].equals("by_user")) {
 				if(params.containsKey("user_id")) {
 					long sinceWhen = 0L;
 					String units = "";
 					if(params.containsKey("sincewhen")) {
 						sinceWhen = Long.valueOf(params.get("sincewhen")[0]);
 					}
 					if(params.containsKey("units")) {
 						units = (params.get("units")[0]);
 					}
 					String userId = params.get("user_id")[0];
 					log.info("userId: " + userId + ", units: " + units);
 					String chartData = dh.getChartFromSingleUser(userId, sinceWhen, units);
 					String additionalData = "";
 					String totalSubmissions = "";
 					String lastDaySubmissions = "";
 					String percentile = "";	
 					String exportLink = "";
 					if(params.containsKey("selfstats")) {
 						String selfStats = params.get("selfstats")[0];
 						if(selfStats.equals("yes")) {
 							totalSubmissions = dh.generateStatisticsByUserAndTime(userId, 0);
 							
 							long now = Calendar.getInstance().getTimeInMillis();
 							long day = 1000*60*60*24;
 							lastDaySubmissions = dh.generateStatisticsByUserAndTime(userId, now-day);
 							
 							//percentile = dh.getUserPercentile(userId, totalSubmissions);
 							String url = serverURL + "?export=true&userId=" + userId;
 							exportLink = "<a href='" + url + "' style='color:#33b5e5'>Download Your Recent Data (CSV)</a>";
 							exportLink += " (archive export temporarily disabled, coming back soon)";
 							
 							
 							additionalData = totalSubmissions + "<br/>" + lastDaySubmissions + "<br/>" + exportLink;
 						} else {
 							additionalData = "";
 						}
 					}
 					//log.info(chartData);
 					response.setContentType("text/html");
 					PrintWriter out = response.getWriter();
 					out.print(chartData + additionalData + "</body></html>");
 					
 					out.close();
 				}
 			}
 			
 		} else if(params.containsKey("export")) {
 			String export = params.get("export")[0];
 			if(export.equals("true")) {
 				String id = params.get("userId")[0];
 				// give the user the CSV file
 				String headings = "Time,Longitude,Latitude,Reading";
 				String data = dh.getUserCSV(id);
 				String file = headings + "\n" + data;
 				
 				response.setContentType("text/html");
 				//response.setContentType("application/zip");
 				PrintWriter out = response.getWriter();
 				out.print(file); // entry
 				out.close();
 				
 			}
 		} else if (params.containsKey("pndv")) {
 			String pndv = params.get("pndv")[0];
 			if (pndv.equals("buffer")) {
 				// PNDV is requesting recent buffered data. Send it!
 				
 				// Send the data dump inside the response
 				// Should be sent as CSV or XML probably.
 				// ...HTML for now
 				try {
 					response.setContentType("text/plain");
 					PrintWriter out = response.getWriter();
 					for (BarometerReading br : bufferToPNDV) {
 						out.print(barometerReadingToWebPNDV(br));
 					}
 					out.close();
 					
 					// clear the buffer
 					bufferToPNDV.clear();
 				} catch (Exception e)
 				{
 					// ...
 				}
 			}
 		} else if(params.containsKey("current_condition")) {
 			log.info("receiving current condition");
 			try {
 				CurrentCondition cc = getCurrentConditionFromParams(params);
 				dh.addCurrentConditionToDatabase(cc);
 				response.setContentType("text/html");
 				PrintWriter out = response.getWriter();
 				out.close();
 			} catch(Exception e) {
 				log.info("failed to receive condition: " + e.getMessage());
 			}
 		} else { 
 			try {
 				// This is #1.
 				BarometerReading br = getBarometerReadingFromParams(params);
 				
 				// Store result in database
 				dh.addReadingToDatabase(br);
 				
 				// Response
 				response.setContentType("text/html");
 				PrintWriter out = response.getWriter();
 				
 				out.close();
 				
 				// TO PNDV!
 				// Send the measurement to the distribution servers
 				addToPNDV(br);
 				
 			} catch(Exception e) {
 				log(e.getMessage());
 				response.setContentType("text/html");
 				PrintWriter out = response.getWriter();
 				out.write("There was an error. Please check your request and try again. Error information: " + e.getMessage());
 				out.close();
 				  // log.info(e.getMessage());
 			}
 		}
 	}
 	
 	// Prepare data to send through the web. Decoded by
 	// csvToBarometerReadings in the android app.
 	// Since some of the data has commas, CSV = BSV
 	public String barometerReadingToWeb(BarometerReading br) {
 		return br.getLatitude() + "|" + 
 			   br.getLongitude() + "|" +
 			   br.getReading() + "|" +
 			   br.getTime() + "|" +
 			   br.getTimeZoneOffset() + "|" +
 			   br.getAndroidId() + "|" +
 			   br.getSharingPrivacy() + "|" +
 			   br.getClientKey() + ";";
 	}
 	
 
 	public String currentConditionToWeb(CurrentCondition cc) {
 		return cc.getLatitude() + "|" + 
 			   cc.getLongitude() + "|" +
 			   cc.getGeneral_condition() + "|" +
 			   cc.getTime() + "|" +
 			   cc.getTzoffset() + "|" +
 			   cc.getWindy() + "|" +
			   cc.getFog_thickness() + "|" +
 			   cc.getPrecipitation_type() + "|" +
 			   cc.getPrecipitation_amount() + "|" +
 			   cc.getThunderstorm_intensity() + "|" +
 			   cc.getUser_id() + ";";
 	}
 	
 	
 	// Shave off the milliseconds
 	public String barometerReadingToWebPNDV(BarometerReading br) {
 		return br.getLatitude() + "|" + 
 			   br.getLongitude() + "|" +
 			   br.getReading() + "|" +
 			   (br.getTime()) + "|" +
 			   br.getTimeZoneOffset() + "|" +
 			   br.getAndroidId() + "|" +
 			   br.getSharingPrivacy() + "|" +
 			   br.getClientKey() + ";";
 	}
 	
 	// Create a Barometer Reading object from a list of parameters 
 	public BarometerReading getBarometerReadingFromParams(Map<String, String[]> params) {
 		BarometerReading br = new BarometerReading();
 		br.setLatitude(Double.parseDouble(params.get("latitude")[0]));
 		br.setLongitude(Double.parseDouble(params.get("longitude")[0]));
 		br.setTime(Double.parseDouble(params.get("time")[0]));
 		br.setTimeZoneOffset(Integer.parseInt(params.get("tzoffset")[0]));
 		br.setReading(Double.parseDouble(params.get("reading")[0]));
 		br.setAndroidId((params.get("text")[0]));
 		br.setSharingPrivacy((params.get("share")[0]));
 		br.setClientKey(params.get("client_key")[0]);
 		
 		return br;
 	}
 	
 	// Create a CurrentCondition object from a list of parameters 
 	public CurrentCondition getCurrentConditionFromParams(Map<String, String[]> params) {
 		CurrentCondition cc = new CurrentCondition();
 		cc.setLatitude(Double.parseDouble(params.get("latitude")[0]));
 		cc.setLongitude(Double.parseDouble(params.get("longitude")[0]));
 		cc.setGeneral_condition(params.get("general_condition")[0]);
 		cc.setTime(Double.parseDouble(params.get("time")[0]));
 		cc.setTzoffset(Integer.parseInt(params.get("tzoffset")[0]));
 		cc.setUser_id(params.get("user_id")[0]);
 		cc.setPrecipitation_type(params.get("precipitation_type")[0]);
 		cc.setPrecipitation_amount(Double.parseDouble(params.get("precipitation_amount")[0]));
 		cc.setWindy(params.get("windy")[0]);
 		cc.setThunderstorm_intensity(params.get("thunderstorm_intensity")[0]);
 		/*
 		cc.setLocation_type(params.get("location_type")[0]);
 		cc.setLocation_accuracy(Double.parseDouble(params.get("location_accuracy")[0]));
 		// ...
 		
 		cc.setSharing_policy((params.get("sharing_policy")[0]));
 		*/
 		
 		return cc;
 	}
 	
     public void log(String text) {
     	log.info(text);
     }
 	
 }
 
