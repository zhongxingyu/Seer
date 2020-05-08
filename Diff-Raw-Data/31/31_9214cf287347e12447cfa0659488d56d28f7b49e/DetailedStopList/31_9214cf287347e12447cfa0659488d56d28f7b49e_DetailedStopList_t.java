 package com.data;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.json.simple.JSONObject;
 
 import com.REST.actor.DetailedStop;
 import com.REST.client.RestClient;
 
 public class DetailedStopList {
 
 	// stopCode
 	private String SpBern;
 	private ArrayList<DetailedStop> detailedStops;
 
 	public DetailedStopList(String SpBern) {
 		detailedStops = new ArrayList<DetailedStop>();
 		this.SpBern = SpBern;
 	}
 
 	@SuppressWarnings("rawtypes")
 	public void update() {
 		JSONObject job = RestClient.getJSONFromURL("stopareacode/" + SpBern);
 		Set set = job.entrySet();
 		Iterator i = set.iterator();
 		while (i.hasNext()) {
			@SuppressWarnings("unused")
 			Map.Entry me = (Map.Entry) i.next();
 			Iterator iTpc = set.iterator();
 			while (iTpc.hasNext()) {
 				Map.Entry meTpc = (Map.Entry) iTpc.next();
 				JSONObject jobCol = (JSONObject) meTpc.getValue();
 				Set setCol = jobCol.entrySet();
 				Iterator iCol = setCol.iterator();
 				while (iCol.hasNext()) {
 					Map.Entry meCol = (Map.Entry) iCol.next();
 					if (meCol.getValue() instanceof JSONObject) {
 						JSONObject jobMes = (JSONObject) meCol.getValue();
 						Set setMes = jobMes.entrySet();
 						Iterator iMes = setMes.iterator();
 						while (iMes.hasNext()) {
 							Map.Entry meMes = (Map.Entry) iMes.next();
 							if (meMes.getKey().equals("Passes")) {
 								JSONObject jobD = (JSONObject) meMes.getValue();
 								Set setD = jobD.entrySet();
 								Iterator iD = setD.iterator();
 								while (iD.hasNext()) {
 									Map.Entry meD = (Map.Entry) iD.next();
 
 									String stopDetailed = (String) meD.getKey();
 									boolean isTimingStop = false;
 									String destinationName50 = null;
 									long lineDirection = 0;
 									String operationDate = null;
 									String timingPointCode = null;
 									String lineName = null;
 									String linePublicNumber = null;
 									String sideCode = null;
 									String timingPointName = null;
 									String tripStopStatus = null;
 									String userStopCode = null;
 									String journeyStopType = null;
 									String targetArrivalTime = null;
 									String targetDepartureTime = null;
 									String expectedArrivalTime = null;
 
 									JSONObject jobS = (JSONObject) meD.getValue();
 									Set setS = jobS.entrySet();
 									Iterator iS = setS.iterator();
 									while (iS.hasNext()) {
 										Map.Entry meS = (Map.Entry) iS.next();
 										if (((String) meS.getKey()).equalsIgnoreCase("isTimingStop")) {
 											isTimingStop = (Boolean) meS.getValue();
 										} else if (((String) meS.getKey()).equalsIgnoreCase("destinationName50")) {
 											destinationName50 = (String) meS.getValue();
 										} else if (((String) meS.getKey()).equalsIgnoreCase("lineDirection")) {
 											lineDirection = (Long) meS.getValue();
 										} else if (((String) meS.getKey()).equalsIgnoreCase("operationDate")) {
 											operationDate = (String) meS.getValue();
 										} else if (((String) meS.getKey()).equalsIgnoreCase("timingPointCode")) {
 											timingPointCode = (String) meS.getValue();
 										} else if (((String) meS.getKey()).equalsIgnoreCase("lineName")) {
 											lineName = (String) meS.getValue();
 										} else if (((String) meS.getKey()).equalsIgnoreCase("linePublicNumber")) {
 											linePublicNumber = (String) meS.getValue();
 										} else if (((String) meS.getKey()).equalsIgnoreCase("sideCode")) {
 											sideCode = (String) meS.getValue();
 										} else if (((String) meS.getKey()).equalsIgnoreCase("timingPointName")) {
 											timingPointName = (String) meS.getValue();
 										} else if (((String) meS.getKey()).equalsIgnoreCase("tripStopStatus")) {
 											tripStopStatus = (String) meS.getValue();
 										} else if (((String) meS.getKey()).equalsIgnoreCase("userStopCode")) {
 											userStopCode = (String) meS.getValue();
 										} else if (((String) meS.getKey()).equalsIgnoreCase("journeyStopType")) {
 											journeyStopType = (String) meS.getValue();
 										} else if (((String) meS.getKey()).equalsIgnoreCase("targetArrivalTime")) {
 											targetArrivalTime = (String) meS.getValue();
 										} else if (((String) meS.getKey()).equalsIgnoreCase("targetDepartureTime")) {
 											targetDepartureTime = (String) meS.getValue();
 										} else if (((String) meS.getKey()).equalsIgnoreCase("expectedArrivalTime")) {
 											expectedArrivalTime = (String) meS.getValue();
 										}
 
 									}
 
 									detailedStops.add(new DetailedStop(stopDetailed, isTimingStop, destinationName50, lineDirection, operationDate, timingPointCode, lineName, linePublicNumber,
 											sideCode, timingPointName, tripStopStatus, userStopCode, journeyStopType, targetArrivalTime, targetDepartureTime, expectedArrivalTime));
 
 								}
 
 							}
 						}
 					}
 				}
 			}
 
 		}
 	}
 
 	/**
 	 * @return the detailedStops
 	 */
 	public ArrayList<DetailedStop> getDetailedStops() {
 		return detailedStops;
 	}
 }
