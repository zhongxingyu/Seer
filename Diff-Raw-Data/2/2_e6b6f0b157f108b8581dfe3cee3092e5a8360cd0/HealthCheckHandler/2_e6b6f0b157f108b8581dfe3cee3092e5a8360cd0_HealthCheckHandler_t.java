 package dk.frv.eavdam.utils;
 
 import java.util.ArrayList;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 
 import dk.frv.eavdam.data.AISDatalinkCheckArea;
 import dk.frv.eavdam.data.AISDatalinkCheckIssue;
 import dk.frv.eavdam.data.AISDatalinkCheckResult;
 import dk.frv.eavdam.data.AISDatalinkCheckRule;
 import dk.frv.eavdam.data.AISDatalinkCheckSeverity;
 import dk.frv.eavdam.data.AISFixedStationCoverage;
 import dk.frv.eavdam.data.AISFixedStationData;
 import dk.frv.eavdam.data.AISFixedStationType;
 import dk.frv.eavdam.data.AISFrequency;
 import dk.frv.eavdam.data.AISSlotMap;
 import dk.frv.eavdam.data.AISStation;
 import dk.frv.eavdam.data.AISTimeslot;
 import dk.frv.eavdam.data.ActiveStation;
 import dk.frv.eavdam.data.EAVDAMData;
 import dk.frv.eavdam.data.EAVDAMUser;
 import dk.frv.eavdam.data.OtherUserStations;
 import dk.frv.eavdam.data.Simulation;
 import dk.frv.eavdam.healthcheck.PointInPolygon;
 import dk.frv.eavdam.io.AISDatalinkCheckListener;
 import dk.frv.eavdam.io.derby.DerbyDBInterface;
 
 
 public class HealthCheckHandler {
 
 	public static final int TRANSMISSION_COVERAGE = 1;
 	public static final int INTERFERENCE_COVERAGE = 2;
 	EAVDAMData data = null;
 	
 	public static String plannedIndicator = " (P)";
 	public static int numberOfFrequencies = 2;
 	public static int numberOfSlotsPerFrequency = 2250;
 	
 	private Map<String, String> areaIssueMap = null; //Store the issues found from the area to this map. This is a pointer to the stationSlotmap
 	private Map<String, AISSlotMap> stationSlotmap = null; //Stores slot map of each station combination.
 	private Map<String, AISFixedStationData> stations = null;
 	boolean log = false;
 	
 	private static int maxNumberOfCells = 75000;
 	
 	public boolean useOptimization = false;
 
 	private boolean cancelled = false;
 	
 	public HealthCheckHandler(EAVDAMData data){
 		if(data == null) System.out.println("Health Check started with null data...");
 		this.data = data;
 	}
 	
 	public AISDatalinkCheckResult startAISDatalinkCheck(AISDatalinkCheckListener listener, boolean checkRule1, boolean checkRule2, boolean checkRule3, 
 			boolean checkRule4, boolean checkRule5, boolean checkRule6, boolean checkRule7, boolean includePlanned,
 			double topLeftLatitude, double topLeftLongitude, double lowerRightLatitude, double lowerRightLongitude, double resolution){
 		
 		listener.progressed(0);
 		this.areaIssueMap = new HashMap<String, String>();
 		this.stationSlotmap = new HashMap<String, AISSlotMap>();
 		this.stations = new HashMap<String, AISFixedStationData>();
 		
 		System.gc();
 		
 		AISDatalinkCheckResult results = new AISDatalinkCheckResult();
 
 		if(!checkRule1 && !checkRule2 && !checkRule3 && !checkRule4 && !checkRule5 && !checkRule6 && !checkRule7){
 			listener.completed(results);
 			listener.progressed(1.0);
 			
 			return results;
 		}
 		
 ////		System.out.println("Resolution for health check: "+resolution);
 //		Map<String,AISFixedStationCoverage> coverages = new HashMap<String, AISFixedStationCoverage>();
 //		for(OtherUserStations o : this.data.getOtherUsersStations()){
 //			for(ActiveStation as : o.getStations()){
 //				for(AISFixedStationData s : as.getStations()){
 //					System.out.println(o.getUser().getOrganizationName()+": "+s.getStationName()+" --> "+s.getTransmissionCoverage()+" | "+s.getTransmissionCoverage().getCoveragePoints());
 //					coverages.put("T:"+o.getUser().getOrganizationName()+": "+s.getStationName(), s.getTransmissionCoverage());
 //					coverages.put("I:"+o.getUser().getOrganizationName()+": "+s.getStationName(), s.getInterferenceCoverage());
 //					coverages.put("R:"+o.getUser().getOrganizationName()+": "+s.getStationName(), s.getReceiveCoverage());
 //					
 //				}
 //			}
 //		}
 		
 		this.data = DBHandler.getData(topLeftLatitude + 2.5, topLeftLongitude - 4.5, lowerRightLatitude -2.5, lowerRightLongitude + 4.5);  
 
 		//Copy the Coverage areas of other user stations
 //		this.data = this.copyCoverageAreasToEAVDAMData(coverages);
 		
 //		if(this.data == null) System.out.println("No DATA!");
 		
 		Map<String, Map<String,AISFixedStationData>> overlappingStations = this.findOverlappingStations(topLeftLatitude + 2.5, topLeftLongitude - 4.5, lowerRightLatitude -2.5, lowerRightLongitude + 4.5, includePlanned);
 		
 		List<AISDatalinkCheckIssue> issues = new ArrayList<AISDatalinkCheckIssue>();
 		Set<String> foundProblems = new HashSet<String>();
 		
 		
 		
 		//AREA FOCUSED HEALTH CHECK
 		double latIncrement = getLatitudeIncrement(resolution, topLeftLatitude, topLeftLongitude, lowerRightLatitude, lowerRightLongitude);
 		if(latIncrement < 0) latIncrement *= -1;
 		
 		double lonIncrement = getLongitudeIncrement(resolution, topLeftLatitude, topLeftLongitude, lowerRightLatitude, lowerRightLongitude);
 		if(lonIncrement < 0) lonIncrement *= -1;
 		
 		double numberOfCells = 1.0*(topLeftLatitude-lowerRightLatitude)/latIncrement * 1.0*(lowerRightLongitude-topLeftLongitude)/lonIncrement;
 		if(numberOfCells > maxNumberOfCells){
 			double oldRes = resolution;
 			resolution = getMinResolution(topLeftLatitude, topLeftLongitude, lowerRightLatitude, lowerRightLongitude);
 			
 			if(resolution > oldRes){
 			
 				latIncrement = getLatitudeIncrement(resolution, topLeftLatitude, topLeftLongitude, lowerRightLatitude, lowerRightLongitude);
 				if(latIncrement < 0) latIncrement *= -1;
 			
 				lonIncrement = getLongitudeIncrement(resolution, topLeftLatitude, topLeftLongitude, lowerRightLatitude, lowerRightLongitude);
 				if(lonIncrement < 0) lonIncrement *= -1;
 			
 				numberOfCells = 1.0*(topLeftLatitude-lowerRightLatitude)/latIncrement * 1.0*(lowerRightLongitude-topLeftLongitude)/lonIncrement;
 			
 				System.out.println("Changing resolution to "+resolution+" to avoid memory issues...");
 			}
 		}
 		
 		System.out.println("Health Check started with resolution "+resolution+"nm. There are "+((int)numberOfCells)+" number of coordinates to be checked!");
 		
 		
 		System.out.println("Starting area focused health check (Rules "+(checkRule1 ? "1" : "")+(checkRule3 ? ", 3" : "")+(checkRule7 ? ", 7" : "")+").");
 		List<AISDatalinkCheckArea> areas = new ArrayList<AISDatalinkCheckArea>();
 		//Loop through the area?
 		
 		int ithLine = 0, ithTotal = 0;
 		double prevLat = 0, prevLon = 0;
 		Set<String> foundAreas = new HashSet<String>();
 		
 		
 		List<AISFixedStationData> latitudeStopPoints = null;
 		if(this.useOptimization) latitudeStopPoints = this.getLatitudeStopPoints();
 		List<AISFixedStationData> endStopPoints = new ArrayList<AISFixedStationData>();
 		
 		if(useOptimization && (latitudeStopPoints == null || latitudeStopPoints.size() <= 0)){
 			System.out.println("Problem with latitude stop points...");
 			
 			return null;
 		}
 		
 		int currentStopPoint = 0;
 		
 		double lon = topLeftLongitude, maxLongitude = lowerRightLongitude;
 		if(useOptimization){
 			lon = lowerRightLatitude;
 			maxLongitude = Double.MIN_VALUE;
 		}
 		
 //		System.out.println("LAT search area: "+topLeftLatitude+" > "+lowerRightLatitude+", increment: "+(-1*latIncrement) +" --> "+((int)(1.0*(topLeftLatitude-lowerRightLatitude)/latIncrement)));
 //		System.out.println("LON search area: "+lon+" < "+maxLongitude+", increment: "+lonIncrement +" --> "+((int)(1.0*(lowerRightLongitude-topLeftLongitude)/lonIncrement)));
 		for(double lat = topLeftLatitude ; lat > lowerRightLatitude; lat = lat - latIncrement){
 
 			if(!checkRule1 && !checkRule3 && !checkRule7) break;
 			
 			if(useOptimization){
 				//Optimization: Skip cells that do not have any information.
 				if(currentStopPoint < latitudeStopPoints.size() && latitudeStopPoints.get(currentStopPoint).getNorthTransmitCoveragePoints()[0] < lat){ //No stop point
 					//We are between the stop points. No need to do anything?
 					
 				}else{ //We passed a stop point.
 					if(currentStopPoint < latitudeStopPoints.size()){
 					
 						System.out.println("Found stop point at "+lat+" ith stop point: "+currentStopPoint);
 						
 						//check if the old lon is smaller than the new one:
 						if(latitudeStopPoints.get(currentStopPoint).getWestTransmitCoveragePoints()[1] < lon)
 							lon = latitudeStopPoints.get(currentStopPoint).getWestTransmitCoveragePoints()[1];
 						
 						if(latitudeStopPoints.get(currentStopPoint).getEastTransmitCoveragePoints()[1] > maxLongitude)
 							maxLongitude = latitudeStopPoints.get(currentStopPoint).getEastTransmitCoveragePoints()[1];
 						
 						
 						
 						if(endStopPoints.size() == 0){ //Add new end point.
 							endStopPoints.add(latitudeStopPoints.get(currentStopPoint));
 						}else{
 							for(int i = 0; i < endStopPoints.size(); ++i){ //Add point to the list to indicate when it should be stopped.
 								if(endStopPoints.get(i).getSouthTransmitCoveragePoints()[0] < latitudeStopPoints.get(currentStopPoint).getSouthTransmitCoveragePoints()[0]){
 									endStopPoints.add(latitudeStopPoints.get(currentStopPoint));
 									break;
 								}
 								
 								if(i == endStopPoints.size() - 1){
 									endStopPoints.add(latitudeStopPoints.get(currentStopPoint));
 									break;
 								}
 							}
 							
 						}
 						
 						
 						++currentStopPoint; //Move stop point to the next point.
 					}
 				}
 				
 				//Check if we have reached an end point
 				if(endStopPoints.size() > 0){
 					if(endStopPoints.get(0).getSouthTransmitCoveragePoints()[0] > lat){
 						//We have passed an end point. 
 						
 						//Find a new longitude
 						lon = lowerRightLongitude;
 						maxLongitude = topLeftLongitude;
 						for(int i = 1; i < endStopPoints.size(); ++i){
 							if(endStopPoints.get(i).getWestTransmitCoveragePoints()[1] < lon) lon = endStopPoints.get(i).getWestTransmitCoveragePoints()[1];
 							if(endStopPoints.get(i).getEastTransmitCoveragePoints()[1] > maxLongitude) maxLongitude = endStopPoints.get(i).getEastTransmitCoveragePoints()[1];
 						}
 						
 						endStopPoints.remove(0); //Delete the top-most end point.
 					}
 					
 					
 				}else if(currentStopPoint >= latitudeStopPoints.size()){
 					break;
 				}
 			}
 			
 			++ithLine;
 			
 			int ithColumn = 0;
 			for(; lon < maxLongitude; lon = lon + lonIncrement){
 				++ithColumn;
 				
 //				System.out.println("ith Column: "+ithColumn+" lat: "+lat+", lon: "+lon);
 				//Get the issues
 				AISSlotMap sm = slotMapAtPoint(lat, lon, includePlanned);
 				
 				if(ithLine > 1 && ithColumn > 1 && sm != null){
 					
 					this.areaIssueMap.put(prevLat+";"+prevLon+"-"+lat+";"+lon, this.areaIssueMap.get(lat+";"+lon));
 					foundAreas.add(prevLat+";"+prevLon+"-"+lat+";"+lon);
 					
 					AISDatalinkCheckArea area = new AISDatalinkCheckArea(prevLat,prevLon,lat,lon);
 					
 					area.setSlotmap(sm);
 					if(area.getSlotmap() != null){
 						area.setIssues(area.getSlotmap().getIssues());
 						area.setBandwithUsageLevel(area.getSlotmap().getBandwidthReservation());
 					}
 					
 					areas.add(area);
 				}
 				
 				prevLon = lon;
 				
 				
 			}
 			
 			if(this.cancelled){
 				listener.completed(null);
 				System.gc();
 				System.out.println("Health Check cancelled...");
 				return null;
 			}
 			
 			
 			System.gc();
 			if(!useOptimization){
 				lon = topLeftLongitude;
 			}
 			
 			ithTotal += 1.0*(lowerRightLongitude-topLeftLongitude)/lonIncrement;
 			
 			if(ithTotal > numberOfCells) ithTotal = (int)numberOfCells;
 			
 //			System.out.println("Total "+ithTotal+"/"+((int)numberOfCells)+"");
 			
 			if(listener != null){
 				double progress = 1.0*ithTotal/((int) numberOfCells);
 				if(progress > 0.9) progress = 0.9;
 				listener.progressed(progress);
 			}
 			
 			
 			prevLat = lat;
 			
 		}
 		
 		
 		System.out.println("Area focused health check completed...");
 		
 		if(checkRule2 || checkRule4 || checkRule5 || checkRule6){
 			System.out.println("Starting station focused health check (Rules "+(checkRule2 ? "2, " : "")+(checkRule4 ? "4, " : "")+(checkRule5 ? "5, " : "")+(checkRule6 ? "6 " : "")+")");
 		}
 		
 		
 		for(String s : overlappingStations.keySet()){
 			if(this.cancelled){
 				listener.completed(null);
 				System.gc();
 				System.out.println("Health Check cancelled...");
 				return null;
 			}
 			
 			AISFixedStationData station = stations.get(s);
 			if(station == null){
 				System.err.println("Station "+s+" was not found when checking the rules!");
 				continue;
 			}
 			
 			if(overlappingStations.get(s) == null){
 				System.err.println("Station "+s+" was not found when getting the overlapping stations!");
 				continue;
 			}
 			
 			if(checkRule2){
 				List<AISTimeslot> problems = this.checkRule2(station,overlappingStations.get(s));
 				if(problems != null && problems.size() > 0){
 					
 					List<AISStation> stations = new ArrayList<AISStation>();
 					AISStation s1 = new AISStation(station.getOperator().getOrganizationName(), station.getStationName(), station.getLat(), station.getLon());
 					s1.setDbId(station.getStationDBID());
 					stations.add(s1);
 					
 					String problemName = "RULE2_"+s1.getOrganizationName()+"_"+s1.getStationName();
 					
 					if(!foundProblems.contains(problemName)){
 						AISDatalinkCheckIssue issue = new AISDatalinkCheckIssue(-1,AISDatalinkCheckRule.RULE2,getRuleSeverity(AISDatalinkCheckRule.RULE2),stations,problems);
 						
 						foundProblems.add(problemName);
 						
 						issues.add(issue);
 					}
 				}
 				
 			}
 			
 			listener.progressed(0.93);
 			
 			if(checkRule4){
 				List<AISTimeslot> problems = this.checkRule4(station);
 				if(problems != null && problems.size() > 0){
 					
 					List<AISStation> stations = new ArrayList<AISStation>();
 					AISStation s1 = new AISStation(station.getOperator().getOrganizationName(), station.getStationName(), station.getLat(), station.getLon());
 					s1.setDbId(station.getStationDBID());
 					stations.add(s1);
 					
 					String problemName = "RULE4_"+s1.getOrganizationName()+"_"+s1.getStationName();
 					if(!foundProblems.contains(problemName)){
 						AISDatalinkCheckIssue issue = new AISDatalinkCheckIssue(-1,AISDatalinkCheckRule.RULE4,getRuleSeverity(AISDatalinkCheckRule.RULE4),stations,problems);
 						
 						foundProblems.add(problemName);
 						
 						issues.add(issue);
 					}
 				}
 			}
 			
 			listener.progressed(0.95);
 			
 			if(checkRule5){
 				
 				if(this.checkRule5(station)){
 					
 					List<AISStation> stations = new ArrayList<AISStation>();
 					AISStation s1 = new AISStation(station.getOperator().getOrganizationName(), station.getStationName(), station.getLat(), station.getLon());
 					s1.setDbId(station.getStationDBID());
 					stations.add(s1);
 					
 					String problemName = "RULE5_"+s1.getOrganizationName()+"_"+s1.getStationName();
 					if(!foundProblems.contains(problemName)){
 						AISDatalinkCheckIssue issue = new AISDatalinkCheckIssue(-1,AISDatalinkCheckRule.RULE5,getRuleSeverity(AISDatalinkCheckRule.RULE4),stations,null);
 						
 						foundProblems.add(problemName);
 						
 						issues.add(issue);
 					}
 				}
 			}
 			listener.progressed(0.97);
 			
 			for(String v : overlappingStations.get(s).keySet()){
 				if(true) break;
 				if(false){ //1. rule
 					//Checking the first rule.
 					List<AISTimeslot> problems = this.checkRule1(station, overlappingStations.get(s).get(v));
 					if(problems != null && problems.size() > 0){
 						
 						AISFixedStationData station2 = overlappingStations.get(s).get(v);
 						
 						List<AISStation> stations = new ArrayList<AISStation>();
 						AISStation s1 = new AISStation(station.getOperator().getOrganizationName(), station.getStationName(), station.getLat(), station.getLon());
 						s1.setDbId(station.getStationDBID());
 						stations.add(s1);
 						
 						AISStation s2 = new AISStation(station2.getOperator().getOrganizationName(), station2.getStationName(), station2.getLat(), station2.getLon());
 						s2.setDbId(station2.getStationDBID());
 						stations.add(s2);
 						
 						String problemName = "RULE1_"+s1.getOrganizationName()+"_"+s1.getStationName()+" "+s2.getOrganizationName()+"_"+s2.getStationName();
 						String problemName2 = "RULE1_"+s2.getOrganizationName()+"_"+s2.getStationName()+" "+s1.getOrganizationName()+"_"+s1.getStationName();
 						if(!foundProblems.contains(problemName) && !foundProblems.contains(problemName2) ){
 							AISDatalinkCheckIssue issue = new AISDatalinkCheckIssue(-1,AISDatalinkCheckRule.RULE1,getRuleSeverity(AISDatalinkCheckRule.RULE1),stations,problems);
 							
 							foundProblems.add(problemName);
 							foundProblems.add(problemName2);
 							
 							issues.add(issue);
 						}
 					}
 				}
 				
 
 				
 
 			}
 		}
 		
 		if(this.cancelled){
 			listener.completed(null);
 			System.gc();
 			System.out.println("Health Check cancelled...");
 			return null;
 		}
 		
 		if(areas.size() == 0){
 			for(String keys : foundAreas){
 				String key = this.areaIssueMap.get(keys);
 				String[] top = keys.split("\\-");
 				
 				AISDatalinkCheckArea area = new AISDatalinkCheckArea(Double.parseDouble(top[0].split(";")[0]),Double.parseDouble(top[0].split(";")[1]),Double.parseDouble(top[1].split(";")[0]),Double.parseDouble(top[1].split(";")[1]));
 				area.setSlotmap(this.stationSlotmap.get(key));
 				if(area.getSlotmap() != null){
 					area.setIssues(area.getSlotmap().getIssues());
 					area.setBandwithUsageLevel(area.getSlotmap().getBandwidthReservation());
 				}
 				
 				areas.add(area);
 			}
 		}
 		
 		List<AISDatalinkCheckIssue> issueList = new ArrayList<AISDatalinkCheckIssue>();
 		if(areas != null){
 			for(AISDatalinkCheckArea a : areas){
 				if(a != null){
 					issueList.addAll(a.getSlotmap().getIssues());
 				}
 			}
 			
 			issueList.addAll(issues);
 			
 //			System.out.println("IssueList: "+issueList.size());
 			issues = this.trimIssueList(issueList, checkRule1, checkRule2, checkRule3, checkRule4, checkRule5, checkRule6, checkRule7);
 			
 			
 		}
 		
 		System.out.println("Health checks completed...");
 		listener.progressed(1.0);
 		
 
		System.out.println("Found "+issues.size()+" station issues and slot maps for "+areas.size()+" points.");
 //		for(AISDatalinkCheckIssue i : issues){
 //			System.out.println("\t"+i.toString());
 //		}
 		
 
 		
 		results.setIssues(issues);
 		results.setAreas(areas);
 		
 //		System.out.println("Found information regarding "+areas.size()+" areas. ");
 //		for(AISDatalinkCheckArea i : areas){
 //			if(Math.random() < 0.001)
 //				System.out.println("\t"+i.toString());
 //		}
 		
 		if(listener != null) listener.completed(results);
 		
 		this.areaIssueMap = null;
 		this.stations = null;
 		this.stationSlotmap = null;
 		
 		System.gc(); //Garbage Collection...
 				
 		
 		return results;
 	}
 	
 	
 	
 	private boolean checkRule5(AISFixedStationData station) {
 		return FATDMAUtils.areReservedBlocksAccordingToFATDMAScheme((float)station.getLat(), (float)station.getLon(), station.getReservedBlocksForChannelA(), station.getReservedBlocksForChannelB());
 	}
 
 	private List<AISFixedStationData> getLatitudeStopPoints() {
 		List<AISFixedStationData> latitudeStopPoints = new ArrayList<AISFixedStationData>();
 		
 		//Check the areas within the area.
 		if(this.data.getActiveStations() != null){
 			for(ActiveStation as : this.data.getActiveStations()){
 				if(as.getStations() != null){
 					for(AISFixedStationData d : as.getStations()){
 						if(d.getNorthTransmitCoveragePoints() == null) continue;
 						if(latitudeStopPoints.size() == 0){
 							latitudeStopPoints.add(d);
 						}else{
 							for(int i = 0; i < latitudeStopPoints.size(); ++i){
 								if(latitudeStopPoints.get(i).getNorthTransmitCoveragePoints()[0] < d.getNorthTransmitCoveragePoints()[0]){
 									latitudeStopPoints.add(i,d);
 									break;
 								}
 								
 								if(i == latitudeStopPoints.size() - 1){
 									latitudeStopPoints.add(d);
 									break;
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		if(this.data.getOtherUsersStations() != null){
 			for(OtherUserStations other : this.data.getOtherUsersStations()){
 				if(other != null && other.getStations() != null){
 					for(ActiveStation ao : other.getStations()){
 						if(ao.getStations() != null){
 							for(AISFixedStationData d : ao.getStations()){
 								if(latitudeStopPoints.size() == 0){
 									latitudeStopPoints.add(d);
 								}else{
 									for(int i = 0; i < latitudeStopPoints.size(); ++i){
 										if(latitudeStopPoints.get(i).getNorthTransmitCoveragePoints()[0] < d.getNorthTransmitCoveragePoints()[0]){
 											latitudeStopPoints.add(i,d);
 											break;
 										}
 										
 										if(i == latitudeStopPoints.size() - 1){
 											latitudeStopPoints.add(d);
 											break;
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		
 		return latitudeStopPoints;
 	}
 
 	/**
 	 * Checks the Rule 1: two stations have the same FATDMA slots reserved (both of them for local and neither of them are AtoN stations).
 	 * 
 	 * @param s1
 	 * @param s2
 	 * @return
 	 */
 	public List<AISTimeslot> checkRule1(AISFixedStationData s1, AISFixedStationData s2){
 		if(s1 == null || s2 == null){
 			System.err.println("Station is null when checking the rule 1...");
 			return null;
 		}			
 		
 		List<AISTimeslot> problems = new ArrayList<AISTimeslot>();
 		if(s1.getReservedBlocksForChannelA() != null){
 			for(Integer rs1 : s1.getReservedBlocksForChannelA()){
 				if(s2.getReservedBlocksForChannelA() == null) break;
 				if(s1.getStationType().equals(AISFixedStationType.ATON) || s2.getStationType().equals(AISFixedStationType.ATON)) break;	
 				
 				for(Integer rs2 : s2.getReservedBlocksForChannelA()){
 					if(rs2.intValue() > rs1.intValue()) break;
 						
 					if(rs1.intValue() == rs2.intValue()){
 						
 						if(s1.getOwnershipInSlot("A", rs1.intValue()) == null){
 							System.out.println("No ownership for slot "+rs1+" in station 1 ("+s1.getStationType()+")");
 							continue;
 						}
 					
 						if(s2.getOwnershipInSlot("A", rs1.intValue()) == null){
 							System.out.println("No ownership for slot "+rs1+" in station 2 ("+s2.getStationType()+")");
 							continue;
 						}
 						
 						//Check if the slots are reserved for the remote use.
 						if(!s1.getOwnershipInSlot("A", rs1.intValue()).equals("R") && !s2.getOwnershipInSlot("A", rs1.intValue()).equals("R")){
 							//Problem found!
 							AISTimeslot slot = new AISTimeslot(AISFrequency.AIS1,rs1.intValue(),new Boolean(false),null,null,null,new Boolean(true));
 							problems.add(slot);
 						}
 						
 						break;
 					}
 				}
 			}
 		}
 		
 		if(s1.getReservedBlocksForChannelB() != null){
 			for(Integer rs1 : s1.getReservedBlocksForChannelB()){
 				if(s2.getReservedBlocksForChannelB() == null) break;
 				if(s1.getStationType().equals(AISFixedStationType.ATON) || s2.getStationType().equals(AISFixedStationType.ATON)) break;	
 				
 				for(Integer rs2 : s2.getReservedBlocksForChannelB()){
 					if(rs2.intValue() > rs1.intValue()) break;
 						
 					if(rs1.intValue() == rs2.intValue()){
 						
 						if(s1.getOwnershipInSlot("B", rs1.intValue()) == null){
 							System.out.println("No ownership for slot "+rs1+" in station 1 ("+s1.getStationType()+")");
 							continue;
 						}
 					
 						if(s2.getOwnershipInSlot("B", rs1.intValue()) == null){
 							System.out.println("No ownership for slot "+rs1+" in station 2 ("+s2.getStationType()+")");
 							continue;
 						}
 						
 						//Check if the slots are reserved for the remote use.
 						if(!s1.getOwnershipInSlot("B", rs1.intValue()).equals("R") && !s2.getOwnershipInSlot("B", rs1.intValue()).equals("R")){
 							//Problem found!
 							AISTimeslot slot = new AISTimeslot(AISFrequency.AIS2,rs1.intValue(),new Boolean(false),null,null,null,new Boolean(true));
 							problems.add(slot);
 						}
 						
 						break;
 					}
 				}
 			}
 		}
 		
 		return problems;
 	}
 	
 	/**
 	 * Checks the rule 2: Remote reservations but no intended use
 	 * 
 	 * @param s1
 	 * @param s2
 	 * @return
 	 */
 	private List<AISTimeslot> checkRule2(AISFixedStationData s1, Map<String,AISFixedStationData> stations) {
 		if(s1 == null || stations == null){
 			System.err.println("Station is null when checking the rule 1...");
 			return null;
 		}			
 		
 		//First station must not be an AtoN station 
 		if(s1.getStationType().equals(AISFixedStationType.ATON)){
 			return null;
 		}
 		
 		List<AISTimeslot> problems = new ArrayList<AISTimeslot>();
 		if(s1.getReservedBlocksForChannelA() != null){
 			for(Integer rs1 : s1.getReservedBlocksForChannelA()){
 				if(s1.getOwnershipInSlot("A", rs1.intValue()).equals("L")) continue;  //We only need the remote slots.
 
 				boolean reservation = false;
 				//Check the all the stations that overlap this one.
 				for(String s : stations.keySet()){
 					AISFixedStationData s2 = stations.get(s);
 					if(s2 == null) continue;
 					
 					double[] s2Loc = {s2.getLat(),s2.getLon()};
 					boolean pip = PointInPolygon.isPointInPolygon(s1.getTransmissionCoverage().getCoveragePoints(), s2Loc);
 					
 					if(!pip) continue; //Not within the station's transmit area
 					
 					if(s2.getOwnershipInSlot("A", rs1) == null || s2.getOwnershipInSlot("A", rs1).equals("R")) continue; //No reservation or a Remote reservation
 					else{
 						reservation = true; //There is a reservation for another station that isn't remote and that is within the area.
 					}
 				}
 				
 				if(!reservation){
 					AISTimeslot slot = new AISTimeslot(AISFrequency.AIS1,rs1.intValue(),new Boolean(false),null,null,null,new Boolean(true));
 					problems.add(slot);
 				}
 			}
 		}
 		
 		if(s1.getReservedBlocksForChannelB() != null){
 			for(Integer rs1 : s1.getReservedBlocksForChannelB()){
 				if(s1.getOwnershipInSlot("B", rs1.intValue()).equals("L")) continue;  //We only need the remote slots.
 
 				boolean reservation = false;
 				//Check the all the stations that overlap this one.
 				for(String s : stations.keySet()){
 					AISFixedStationData s2 = stations.get(s);
 					if(s2 == null) continue;
 					
 					double[] s2Loc = {s2.getLat(),s2.getLon()};
 					boolean pip = PointInPolygon.isPointInPolygon(s1.getTransmissionCoverage().getCoveragePoints(), s2Loc);
 					
 					if(!pip) continue; //Not within the station's transmit area
 					
 					if(s2.getOwnershipInSlot("B", rs1) == null || s2.getOwnershipInSlot("B", rs1).equals("R")) continue; //No reservation or a Remote reservation
 					else{
 						reservation = true; //There is a reservation for another station that isn't remote and that is within the area.
 					}
 				}
 				
 				if(!reservation){
 					AISTimeslot slot = new AISTimeslot(AISFrequency.AIS2,rs1.intValue(),new Boolean(false),null,null,null,new Boolean(true));
 					problems.add(slot);
 				}
 			}
 			
 		}
 		
 		return problems;
 
 	}
 
 	private List<AISTimeslot> checkRule4(AISFixedStationData s1) {
 		if(s1 == null || s1.getReservedBlocksForChannelA() == null || s1.getReservedBlocksForChannelB() == null){
 			
 			return null;
 		}			
 		
 		
 		List<AISTimeslot> problems = new ArrayList<AISTimeslot>();
 		if(s1.getReservedBlocksForChannelA() != null){
 			for(Integer rs1 : s1.getReservedBlocksForChannelA()){
 				for(Integer rs2 : s1.getReservedBlocksForChannelB()){
 					if(rs1.intValue() == rs2.intValue()){
 						//Problem if they are both for local use!
 						
 						if(!s1.getOwnershipInSlot("A", rs1.intValue()).equals("R") && !s1.getOwnershipInSlot("B", rs1.intValue()).equals("R")){
 							AISTimeslot slot = new AISTimeslot(AISFrequency.AIS1,rs1.intValue(),new Boolean(false),null,null,null,new Boolean(true));
 							problems.add(slot);
 						}
 					}
 					
 					if(rs2.intValue() > rs1.intValue()) break;
 				}
 					
 			}
 				
 		}
 		
 		
 		
 		return problems;
 	}
 
 
 	/**
 	 * Finds the stations that have overlapping coverage areas. The areas overlap if transmit area of one cuts the interference area of another 
 	 * 
 	 * NOTE: This retrieves all the overlapping stations even if they are AtoN stations. This check should be done within the rules. 
 	 * 
 	 * @param topLeftLatitude The top left latitude of the area in observation (this will be the exact area, i.e., it will not be expanded within this method).
 	 * @param topLeftLongitude Top left longitude
 	 * @param lowerRightLatitude Low right latitude
 	 * @param lowerRightLongitude Low right longitude
 	 * @return Map that holds stations as the keys and a map with overlapping stations (e.g., Station A -> Station B, Stations C = A overlaps with B and C). 
 	 */
 	private Map<String, Map<String,AISFixedStationData>> findOverlappingStations(double topLeftLatitude,
 			double topLeftLongitude, 
 			double lowerRightLatitude,
 			double lowerRightLongitude, 
 			boolean includePlanned) {
 	
 		if(this.data == null) this.data = DBHandler.getData(topLeftLatitude + 2.5, topLeftLongitude - 4.5, lowerRightLatitude -2.5, lowerRightLongitude + 4.5);
 
 		if(this.stations == null) stations = new HashMap<String, AISFixedStationData>();
 		
 		Map<String, Map<String,AISFixedStationData>> overlappingStations = new HashMap<String, Map<String,AISFixedStationData>>();
 		
 		if(data.getActiveStations() != null){
 			for(ActiveStation as : data.getActiveStations()){
 				if(as.getStations() != null){
 					for(AISFixedStationData s1 : as.getStations()){
 						if(s1.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED && !includePlanned) continue;
 						
 						String s1Name = s1.getOperator().getOrganizationName()+"-"+s1.getStationName();
 						stations.put(s1Name, s1);
 						Map<String,AISFixedStationData> overlaps = overlappingStations.get(s1Name);
 						if(overlaps == null) overlaps = new HashMap<String, AISFixedStationData>();
 								
 						
 						for(AISFixedStationData s2 : as.getStations()){
 							if(s2.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED && !includePlanned) continue;
 							if(s1 == s2 || s1Name.equals(s2.getOperator().getOrganizationName()+"-"+s2.getStationName())) continue;
 							
 //							System.out.println("Comparing: "+s1Name+" vs. "+s2.getStationName());
 							//Compare transmit coverage against interference coverage.
 							try{
 								if(PointInPolygon.isPolygonIntersection(s1.getTransmissionCoverage().getCoveragePoints(), s2.getInterferenceCoverage().getCoveragePoints())){
 									overlaps.put(s2.getOperator().getOrganizationName()+"-"+s2.getStationName(), s2);
 	//								System.out.println("Added "+s2.getStationName()+" to "+s1Name);
 								}
 							}catch (Exception e) {
 								if(log) System.err.println("Problem with the polygons: Stations "+s1.getStationName()+" and "+s2.getStationName());
 								
 								if(log) e.printStackTrace();
 							}
 							
 						}
 
 					
 						for(ActiveStation as1 : data.getActiveStations()){
 							if(as == as1) continue;
 							if(as1.getStations() != null){
 								for(AISFixedStationData s2 : as1.getStations()){
 									if(s2.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED && !includePlanned) continue;
 									if(s1 == s2) continue;
 									
 //									System.out.println("Comparing: "+s1Name+" vs. "+s2.getStationName());
 									try{
 											//Compare transmit coverage against interference coverage.
 										if(PointInPolygon.isPolygonIntersection(s1.getTransmissionCoverage().getCoveragePoints(), s2.getInterferenceCoverage().getCoveragePoints())){
 											overlaps.put(s2.getOperator().getOrganizationName()+"-"+s2.getStationName(), s2);
 	//										System.out.println("Added "+s2.getStationName()+" to "+s1Name);
 										}
 									}catch (Exception e) {
 										if(log) System.err.println("Problem with the polygons: Stations "+s1.getStationName()+" and "+s2.getStationName());
 										
 										if(log) e.printStackTrace();
 									}
 								}
 								
 								
 							}
 						}
 						
 						//Check the other user's stations also...
 						if(data.getOtherUsersStations() != null){
 							for(OtherUserStations other : data.getOtherUsersStations()){
 								if(other.getStations() != null){
 									for(ActiveStation acs : other.getStations()){
 										if(acs.getStations() != null){
 											for(AISFixedStationData o2 : acs.getStations()){
 												if(o2.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED && !includePlanned) continue;
 												
 												if(s1 == o2) continue;
 													
 												try{
 													//Compare transmit coverage against interference coverage.
 													if(PointInPolygon.isPolygonIntersection(s1.getTransmissionCoverage().getCoveragePoints(), o2.getInterferenceCoverage().getCoveragePoints())){
 														overlaps.put(o2.getOperator().getOrganizationName()+"-"+o2.getStationName(), o2);
 													}
 												}catch (Exception e) {
 													if(log) System.err.println("Problem with the polygons: Stations "+s1.getStationName()+" and "+o2.getStationName());
 													
 													if(log) e.printStackTrace();
 												}	
 											}
 											
 											overlappingStations.put(s1Name, overlaps);
 											
 										}
 									}
 								}
 							}
 						}
 						
 						overlappingStations.put(s1Name, overlaps);
 					}
 						
 						
 				}
 			}
 		}
 		
 		
 		if(data.getOtherUsersStations() != null){
 			for(OtherUserStations other : data.getOtherUsersStations()){
 				if(other.getStations() != null){
 					for(ActiveStation as : other.getStations()){
 						if(as.getStations() != null){
 							for(AISFixedStationData o1 : as.getStations()){
 								if(o1.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED && !includePlanned) continue;
 								String o1Name = o1.getOperator().getOrganizationName()+"-"+o1.getStationName();
 								stations.put(o1Name, o1);
 								
 								Map<String,AISFixedStationData> overlaps = overlappingStations.get(o1Name);
 								if(overlaps == null) overlaps = new HashMap<String, AISFixedStationData>();
 								for(AISFixedStationData s2 : as.getStations()){
 									if(s2.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED && !includePlanned) continue;
 									if(o1 == s2) continue;
 									
 									try{
 										//Compare transmit coverage against interference coverage.
 										if(PointInPolygon.isPolygonIntersection(o1.getTransmissionCoverage().getCoveragePoints(), s2.getInterferenceCoverage().getCoveragePoints())){
 											overlaps.put(s2.getOperator().getOrganizationName()+"-"+s2.getStationName(), s2);
 										}
 									}catch (Exception e) {
 										if(log) System.err.println("Problem with the polygons: Stations "+o1.getStationName()+" and "+s2.getStationName());
 										
 										if(log) e.printStackTrace();
 									}
 								}
 								
 								overlappingStations.put(o1Name, overlaps);
 							
 								
 								
 								for(OtherUserStations other2 : data.getOtherUsersStations()){
 									if(other == other2) continue;
 									if(other2.getStations() != null){
 										for(ActiveStation acs : other.getStations()){
 											if(acs.getStations() != null){
 												for(AISFixedStationData o2 : acs.getStations()){
 													if(o2.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED && !includePlanned) continue;
 													if(o1 == o2) continue;
 														
 													try{
 														//Compare transmit coverage against interference coverage.
 														if(PointInPolygon.isPolygonIntersection(o1.getTransmissionCoverage().getCoveragePoints(), o2.getInterferenceCoverage().getCoveragePoints())){
 															overlaps.put(o2.getOperator().getOrganizationName()+"-"+o2.getStationName(), o2);
 														}
 													}catch (Exception e) {
 														if(log)
 															System.err.println("Problem with the polygons: Stations "+o1.getStationName()+" and "+o2.getStationName());
 														
 														if(log) e.printStackTrace();
 													}
 												}
 												
 												overlappingStations.put(o1Name, overlaps);
 												
 											}
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		
 		return overlappingStations;
 	}
 	
 	
 
 	/**
 	 * 
 	 * Gets the stations and checks the bandwith of the given point. The point is the top left corner of the area.
 	 * 
 	 * @param lat TOP LEFT Lat point of the area
 	 * @param lon TOP LEFT Lon point of the area
 	 * @return
 	 */
 	public AISSlotMap slotMapAtPoint(double lat, double lon, boolean includePlanned){
 		if(this.data == null){
 			
 			System.out.println("No data, getting it from the database");
 			
 			this.data = DBHandler.getData(); 
 		}
 		
 		double[] point = {lat, lon};
 		
 		EAVDAMData transmission = getStationsAtPoint(data, point, HealthCheckHandler.TRANSMISSION_COVERAGE);
 		EAVDAMData interference = getStationsAtPoint(data, point, HealthCheckHandler.INTERFERENCE_COVERAGE);
 		
 		if(transmission == null && interference == null) return null;
 
 		Map<String,List<AISFixedStationData>> reservationsA = new HashMap<String, List<AISFixedStationData>>();
 		Map<String,List<AISFixedStationData>> reservationsB = new HashMap<String, List<AISFixedStationData>>();
 		
 		Set<String> handledStations = new HashSet<String>();
 		
 		List<Integer> listOfTransmitStations = new ArrayList<Integer>();
 		List<Integer> listOfInterferenceStations = new ArrayList<Integer>();
 		
 		//Check all active stations
 		
 		if(transmission != null && transmission.getActiveStations() != null){
 			if(transmission.getActiveStations() != null)
 			for(ActiveStation as : transmission.getActiveStations()){
 				if(as.getStations() != null){
 					
 					for(AISFixedStationData s : as.getStations()){
 						if(s.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED && !includePlanned) continue;
 						
 						listOfTransmitStations.add(new Integer(s.getStationDBID()));
 //						if(s.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED) continue; //TODO Include the PLANNED stations to the check also?
 //						
 //						if(handledStations.contains(s.getStationName()+"_"+s.getOperator().getOrganizationName())) continue;
 						
 						if(s.getFATDMAChannelA() != null){
 
 							for(Integer a : s.getReservedBlocksForChannelA()){
 								List<AISFixedStationData> reservations = reservationsA.get(a.intValue()+"");
 								if(reservations == null) reservations = new ArrayList<AISFixedStationData>();
 								
 								reservations.add(s);
 								
 								reservationsA.put(a.intValue()+"", reservations);
 							}
 						}
 						
 						if(s.getFATDMAChannelB() != null){
 							for(Integer a : s.getReservedBlocksForChannelB()){
 								List<AISFixedStationData> reservations = reservationsB.get(a.intValue()+"");
 								if(reservations == null) reservations = new ArrayList<AISFixedStationData>();
 								
 								reservations.add(s);
 								
 								reservationsB.put(a.intValue()+"", reservations);
 							}
 						}
 						
 						handledStations.add(s.getStationName()+"_"+s.getOperator().getOrganizationName());
 					}
 				}
 			}
 		}
 		
 		if(transmission != null && transmission.getOtherUsersStations() != null){
 			for(OtherUserStations other : transmission.getOtherUsersStations()){
 				if(other.getStations() != null){
 					for(ActiveStation as : other.getStations()){
 						if(as.getStations() != null){
 							for(AISFixedStationData s : as.getStations()){
 								if(s.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED && !includePlanned) continue;
 								
 //								System.out.println(s.getStationName()+"-"+s.getOperator().getOrganizationName());
 								
 								listOfTransmitStations.add(new Integer(s.getStationDBID()));
 								
 //								if(s.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED) continue;
 //								if(handledStations.contains(s.getStationName()+"_"+s.getOperator().getOrganizationName())) continue;
 								
 								if(s.getFATDMAChannelA() != null){
 									for(Integer a : s.getReservedBlocksForChannelA()){
 										List<AISFixedStationData> reservations = reservationsA.get(a.intValue()+"");
 										if(reservations == null) reservations = new ArrayList<AISFixedStationData>();
 										
 										reservations.add(s);
 										
 										reservationsA.put(a.intValue()+"", reservations);
 									}
 								}
 								
 								if(s.getFATDMAChannelB() != null){
 									for(Integer a : s.getReservedBlocksForChannelB()){
 										List<AISFixedStationData> reservations = reservationsB.get(a.intValue()+"");
 										if(reservations == null) reservations = new ArrayList<AISFixedStationData>();
 										
 										reservations.add(s);
 										
 										reservationsB.put(a.intValue()+"", reservations);
 									}
 								}
 								
 								handledStations.add(s.getStationName()+"_"+s.getOperator().getOrganizationName());
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		Map<String,List<AISFixedStationData>> interferenceA = new HashMap<String, List<AISFixedStationData>>();
 		Map<String,List<AISFixedStationData>> interferenceB = new HashMap<String, List<AISFixedStationData>>();
 		
 		Set<String> interferenceStations = new HashSet<String>();
 		
 		//Check all active stations
 		if(interference != null && interference.getActiveStations() != null){
 			for(ActiveStation as : interference.getActiveStations()){
 				if(as.getStations() != null){
 					for(AISFixedStationData s : as.getStations()){
 						if(s.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED && !includePlanned) continue;
 						listOfInterferenceStations.add(new Integer(s.getStationDBID()));
 						
 //						if(s.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED) continue; //TODO Include the PLANNED stations to the check also?
 //						if(interferenceStations.contains(s.getStationName()+"_"+s.getOperator().getOrganizationName())) continue;
 						
 						if(s.getFATDMAChannelA() != null){
 							for(Integer a : s.getReservedBlocksForChannelA()){
 								List<AISFixedStationData> reservations = interferenceA.get(a.intValue()+"");
 								if(reservations == null) reservations = new ArrayList<AISFixedStationData>();
 								
 								reservations.add(s);
 								
 								interferenceA.put(a.intValue()+"", reservations);
 							}
 						}
 						
 						if(s.getFATDMAChannelB() != null){
 							for(Integer a : s.getReservedBlocksForChannelB()){
 								List<AISFixedStationData> reservations = interferenceB.get(a.intValue()+"");
 								if(reservations == null) reservations = new ArrayList<AISFixedStationData>();
 								
 								reservations.add(s);
 								
 								interferenceB.put(a.intValue()+"", reservations);
 							}
 						}
 						
 						interferenceStations.add(s.getStationName()+"_"+s.getOperator().getOrganizationName());
 					}
 				}
 				
 				
 			}
 		}
 
 		//TODO Is there a need to check proposals also? What to do to the planned stations?
 		
 		if(interference != null && interference.getOtherUsersStations() != null){
 			for(OtherUserStations other : interference.getOtherUsersStations()){
 				if(other.getStations() != null){
 					for(ActiveStation as : other.getStations()){
 						if(as.getStations() != null){
 							for(AISFixedStationData s : as.getStations()){
 								if(s.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED && !includePlanned) continue;
 								listOfInterferenceStations.add(s.getStationDBID());
 //								if(s.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED) continue;
 //								if(interferenceStations.contains(s.getStationName()+"_"+s.getOperator().getOrganizationName())) continue;
 								
 								if(s.getFATDMAChannelA() != null){
 									for(Integer a : s.getReservedBlocksForChannelA()){
 										List<AISFixedStationData> reservations = interferenceA.get(a.intValue()+"");
 										if(reservations == null) reservations = new ArrayList<AISFixedStationData>();
 										
 										reservations.add(s);
 										
 										interferenceA.put(a.intValue()+"", reservations);
 									}
 								}
 								
 								if(s.getFATDMAChannelB() != null){
 									for(Integer a : s.getReservedBlocksForChannelB()){
 										List<AISFixedStationData> reservations = interferenceB.get(a.intValue()+"");
 										if(reservations == null) reservations = new ArrayList<AISFixedStationData>();
 										
 										reservations.add(s);
 										
 										interferenceB.put(a.intValue()+"", reservations);
 									}
 								}
 								
 								interferenceStations.add(s.getStationName()+"_"+s.getOperator().getOrganizationName());
 							}
 						}
 					}
 				}
 			}
 		}
 
 		Collections.sort(listOfTransmitStations);
 		Collections.sort(listOfInterferenceStations);
 		
 		String smKey = listOfTransmitStations.toString()+"-"+listOfInterferenceStations.toString();
 		
 		if(this.stationSlotmap == null) this.stationSlotmap = new HashMap<String, AISSlotMap>();
 		else if(this.stationSlotmap.get(smKey) != null){
 			this.areaIssueMap.put(lat+";"+lon, smKey);
 			
 			return this.stationSlotmap.get(smKey);
 		}
 		
 //		System.out.println(smKey+" NOT FOUND! Calculating SlotMap");
 		
 		
 		//Create slot map
 		AISSlotMap slotmap = new AISSlotMap();
 		
 		//Get the reservation %
 		
 		slotmap.setLat(lat);
 		slotmap.setLon(lon);
 		
 //		System.out.println("There are "+reservationsA.size()+" and "+reservationsB.size()+" reservations...");
 		AISDatalinkCheckArea area = new AISDatalinkCheckArea(lat, lon); //This point.
 		
 		List<AISTimeslot> slotA = new ArrayList<AISTimeslot>();
 		List<AISTimeslot> slotB = new ArrayList<AISTimeslot>();
 		int notFreeA = 0, notFreeB = 0, usedA = 0, usedB = 0;
 		List<AISDatalinkCheckIssue> issues = new ArrayList<AISDatalinkCheckIssue>();
 		for(int i = 0; i < numberOfSlotsPerFrequency; ++i){
 			AISTimeslot a = this.createAISTimeslot(reservationsA, interferenceA, i, "A", issues);
 			AISTimeslot b = this.createAISTimeslot(reservationsB, interferenceB, i, "B", issues);
 			
 			
 			if(!a.getFree().booleanValue()) ++notFreeA;
 			if(!b.getFree().booleanValue()) ++notFreeB;
 			
 			if(a.getUsedBy() != null && a.getUsedBy().size() > 0) ++usedA;
 			if(b.getUsedBy() != null && b.getUsedBy().size() > 0) ++usedB;
 			
 			slotA.add(a);
 			slotB.add(b);
 		}
 		
 				
 		
 		slotmap.setAIS1Timeslots(slotA);
 		slotmap.setAIS2Timeslots(slotB);
 		
 		slotmap.setBandwidthReservationA(1.0*notFreeA/numberOfSlotsPerFrequency);
 		slotmap.setBandwidthReservationB(1.0*notFreeB/numberOfSlotsPerFrequency);
 		slotmap.setBandwidthUsedByLocalA(1.0*usedA/numberOfSlotsPerFrequency);
 		slotmap.setBandwidthUsedByLocalB(1.0*usedB/numberOfSlotsPerFrequency);
 		slotmap.setBandwidthReservation((1.0*(notFreeA+notFreeB))/(numberOfSlotsPerFrequency*numberOfFrequencies));
 		
 //		this.trimIssueList(issues);
 		slotmap.setIssues(issues);
 
 //		System.out.println("Reservation: "+slotmap.getBandwidthReservation());
 		
 		if(slotmap.getBandwidthReservationA() >= 0.5){
 			AISDatalinkCheckIssue issue = new AISDatalinkCheckIssue(-1,AISDatalinkCheckRule.RULE7,getRuleSeverity(AISDatalinkCheckRule.RULE7),null,null);
 			issues.add(issue);
 		}
 		
 		if(slotmap.getBandwidthReservationB() >= 0.5){
 			AISDatalinkCheckIssue issue = new AISDatalinkCheckIssue(-1,AISDatalinkCheckRule.RULE7,getRuleSeverity(AISDatalinkCheckRule.RULE7),null,null);
 			issues.add(issue);
 		}
 		
 		area.setSlotmap(slotmap);
 		if(this.areaIssueMap == null) this.areaIssueMap = new HashMap<String, String>();
 		
 		this.areaIssueMap.put(lat+";"+lon, listOfTransmitStations.toString()+"-"+listOfInterferenceStations.toString());
 		this.stationSlotmap.put(smKey,slotmap);
 		
 		System.gc();
 		
 		return slotmap;
 	}
 	
 	private List<AISDatalinkCheckIssue> trimIssueList(List<AISDatalinkCheckIssue> issues, boolean checkRule1, boolean checkRule2, boolean checkRule3, boolean checkRule4, boolean checkRule5, boolean checkRule6, boolean checkRule7){
 		if(issues == null || issues.size() == 0){
 			System.out.println("No issues to trim...");
 			return issues;
 		}
 		
 		Map<String, List<AISDatalinkCheckIssue>> rules = new HashMap<String, List<AISDatalinkCheckIssue>>();
 		for(AISDatalinkCheckIssue i : issues){ //Store all issues of the single rule in a list
 			AISDatalinkCheckRule rv = i.getRuleViolated();
 //			System.out.println(rv.toString());
 			
 			if(!checkRule1 && rv.equals(AISDatalinkCheckRule.RULE1)) continue;
 			if(!checkRule2 && rv.equals(AISDatalinkCheckRule.RULE2)) continue;
 			if(!checkRule3 && rv.equals(AISDatalinkCheckRule.RULE3)) continue;
 			if(!checkRule4 && rv.equals(AISDatalinkCheckRule.RULE4)) continue;
 			if(!checkRule5 && rv.equals(AISDatalinkCheckRule.RULE5)) continue;
 			if(!checkRule6 && rv.equals(AISDatalinkCheckRule.RULE6)) continue;
 			if(!checkRule7 && rv.equals(AISDatalinkCheckRule.RULE7)) continue;
 			
 			List<AISDatalinkCheckIssue> ri = rules.get(i.getRuleViolated().toString());
 			if(ri == null) ri = new ArrayList<AISDatalinkCheckIssue>();
 			
 			ri.add(i);
 			
 			rules.put(rv.toString(), ri);
 			
 //			System.out.println(rules.size());
 		}
 		
 //		System.out.println("Found "+rules.size()+" rules..");
 		
 		Map<String,AISDatalinkCheckIssue> stationIssues = new HashMap<String, AISDatalinkCheckIssue>();
 		Map<String,Set<AISTimeslot>> stationTimeslots = new HashMap<String, Set<AISTimeslot>>();
 		
 		
 		List<AISDatalinkCheckIssue> r = new ArrayList<AISDatalinkCheckIssue>();
 		for(String rule : rules.keySet()){
 			Map<String, AISStation> stations = new HashMap<String, AISStation>();
 			
 			for(AISDatalinkCheckIssue i : rules.get(rule)){
 
 				if(i == null || i.getInvolvedStations() == null){
 					if(i.getRuleViolated().equals(AISDatalinkCheckRule.RULE7) && stationIssues.get("RULE7") == null){
 						stationIssues.put("RULE7",i);
 					}
 					
 					continue;
 				}
 				
 				String stationInvolved = "";
 				List<String> invStationNames = new ArrayList<String>();
 				for(AISStation s : i.getInvolvedStations()){
 					stations.put(s.getOrganizationName()+"-"+s.getStationName(), s);
 					
 					invStationNames.add(s.getOrganizationName()+"-"+s.getStationName());
 				}
 				
 				Collections.sort(invStationNames);
 				
 				for(int j = 0; j < invStationNames.size(); ++j){
 					String isn = invStationNames.get(j);
 					
 					stationInvolved += isn;
 					if(j < invStationNames.size() - 1)
 						stationInvolved += ";--;"; 
 				}
 				
 				AISDatalinkCheckIssue iss = stationIssues.get(stationInvolved);
 				if(iss == null){ //Store this only once per rule
 					stationIssues.put(stationInvolved, i);
 					
 					if(i.getInvolvedTimeslots() != null){
 						Set<AISTimeslot> ts = new HashSet<AISTimeslot>();
 						ts.addAll(i.getInvolvedTimeslots());
 					
 						stationTimeslots.put(stationInvolved,ts);
 
 					}
 				}else if(i.getInvolvedTimeslots() != null){
 					Set<AISTimeslot> ts = stationTimeslots.get(stationInvolved);
 					if(ts == null) ts = new HashSet<AISTimeslot>();
 					
 					ts.addAll(i.getInvolvedTimeslots());
 					
 					stationTimeslots.put(stationInvolved,ts);
 				}
 				
 			}
 			
 			for(String si : stationIssues.keySet()){
 //				System.out.println("");
 
 				if(si != null && si.length() > 0 && stationIssues.get(si) != null){
 					AISDatalinkCheckIssue issue = stationIssues.get(si);
 					
 					if(stationTimeslots.get(si) != null){
 						Set<AISTimeslot> ts = stationTimeslots.get(si);
 						List<AISTimeslot> lts = new ArrayList<AISTimeslot>();
 						for(AISTimeslot t : ts){
 							if(lts.size() == 0){
 								lts.add(t);
 							}else{
 								for(int i = 0; i < lts.size(); ++i){
 									if(lts.get(i).getFrequency().equals(t.getFrequency()) && lts.get(i).getSlotNumber() == t.getSlotNumber()){
 										break;
 									}
 									
 									if(lts.get(i).getFrequency().equals(AISFrequency.AIS2) && t.getFrequency().equals(AISFrequency.AIS1)){
 										lts.add(i,t);
 										break;
 									}
 									
 									
 									if(lts.get(i).getFrequency().equals(t.getFrequency()) && lts.get(i).getSlotNumber() > t.getSlotNumber()){
 										lts.add(i,t);
 										
 										break;
 									}
 									
 									if(i == lts.size() - 1){
 										lts.add(t);
 										break;
 									}
 								}
 							}
 						}
 						
 //						lts.addAll(ts);
 						
 						issue.setInvolvedTimeslots(lts);
 					}
 					
 					r.add(issue);
 				}
 			
 			}
 		}
 		
 		
 		System.out.println("Trimmed issue list from "+issues.size()+" to " +r.size());
 		
 		return r;
 	}
 	
 	/**
 	 * Creates the timeslot for the given slot.
 	 * 
 	 * @param reservations List of stations with reservations in the given slots. (Map's keys are the slots).
 	 * @param interference List of interfering stations.
 	 * @param slot Number of the slot in question
 	 * @param channel Either A or B
 	 * @return Timeslot.
 	 */
 	private AISTimeslot createAISTimeslot(Map<String,List<AISFixedStationData>> reservations, 
 			Map<String,List<AISFixedStationData>> interference, int slot, String channel,
 			List<AISDatalinkCheckIssue> issues){
 		AISTimeslot a = new AISTimeslot();
 		a.setSlotNumber(slot);
 		a.setFrequency((channel.equals("B") ? AISFrequency.AIS2 : AISFrequency.AIS1));
 		
 		
 		if(issues == null) issues = new ArrayList<AISDatalinkCheckIssue>();
 		
 		if(reservations.get(slot+"") == null || reservations.get(slot+"").size() <= 0){
 			if(interference.get(slot+"") != null){ //First case: No reservation but there is an interference.
 				List<AISStation> infs = new ArrayList<AISStation>();
 				for(AISFixedStationData station : interference.get(slot+"")){
 					if(station.getStationType().equals(AISFixedStationType.ATON)) continue; //AtoNs do not interfere?
 					
 					if(station.getOwnershipInSlot(channel, new Integer(slot)).equals("L")){ //Interference is for Local Base station
 						AISStation s = new AISStation(station.getOperator().getOrganizationName(), station.getStationName(), station.getLat(), station.getLon());
 						s.setDbId(station.getStationDBID());
 						infs.add(s);
 					}
 				}
 
 				if(infs.size() > 0) a.setInterferedBy(infs); 
 				a.setFree(new Boolean(false));
 			}else{ //No interference
 				a.setFree(new Boolean(true));
 			}
 		}else{
 			int atons = 0, expectedAtoNs = 0;
 			List<AISStation> res = new ArrayList<AISStation>();
 			List<AISStation> used = new ArrayList<AISStation>();
 			
 			Set<String> resNames = new HashSet<String>();
 			Set<String> usedNames = new HashSet<String>();
 			
 			Set<String> resStations = new HashSet<String>();
 			for(AISFixedStationData station : reservations.get(slot+"")){ //Loop through the reservations
 				
 				String planned = "";
 				if(station.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED) planned = plannedIndicator;
 				AISStation s = new AISStation(station.getOperator().getOrganizationName(), station.getStationName()+planned, station.getLat(), station.getLon());
 				s.setDbId(station.getStationDBID());
 				s.setStationType(station.getStationType());
 				
 				if(station.getStationType().equals(AISFixedStationType.ATON)){ //If the station is AtoN, only use it but do not reserve it.
 					if(usedNames.contains(s.getOrganizationName()+"_"+station.getStationName()) && planned.length() <= 0) { //There is a planned station added previously --> need to remove it.
 						for(int i = 0; i < used.size(); ++i){
 							String sta = used.get(i).getOrganizationName()+"_"+used.get(i).getStationName(); 
 							if(sta.equals(s.getOrganizationName()+"_"+station.getStationName()+plannedIndicator)){
 								used.remove(i);
 								break;
 							}
 						}
 						used.add(s);
 					}else if(!usedNames.contains(s.getOrganizationName()+"_"+station.getStationName())){
 						used.add(s);
 						++atons;
 						
 						usedNames.add(s.getOrganizationName()+"_"+station.getStationName());
 					}
 					
 					
 					resStations.add(s.getStationName()+"_"+s.getOrganizationName());
 				}else{ //Not an AtoN stations
 					String o = station.getOwnershipInSlot(channel, new Integer(slot)); //Check if it is a local or remote.
 					if(o == null){
 						System.err.println("No ownership for "+s.getStationName()+" in slot "+slot+" in Channel "+channel+"!");
 					}else if(o.equals("R")){ //Remote --> Reserve for AtoN
 
 						if(resNames.contains(s.getOrganizationName()+"_"+station.getStationName()) && planned.length() <= 0) { //There is a planned station added previously --> need to remove it.
 							for(int i = 0; i < res.size(); ++i){
 								String sta = res.get(i).getOrganizationName()+"_"+res.get(i).getStationName(); 
 								if(sta.equals(s.getOrganizationName()+"_"+station.getStationName()+plannedIndicator)){
 									res.remove(i);
 									break;
 								}
 							}
 							res.add(s);
 						}else if(!resNames.contains(s.getOrganizationName()+"_"+station.getStationName())){
 							res.add(s);
 							++expectedAtoNs;
 							
 							resNames.add(s.getOrganizationName()+"_"+station.getStationName());
 						}
 						
 						resStations.add(s.getStationName()+"_"+s.getOrganizationName());
 					}else{ //Local --> Reserve and use it
 						if(resNames.contains(s.getOrganizationName()+"_"+station.getStationName()) && planned.length() <= 0) { //There is a planned station added previously --> need to remove it.
 							for(int i = 0; i < res.size(); ++i){
 								String sta = res.get(i).getOrganizationName()+"_"+res.get(i).getStationName(); 
 								if(sta.equals(s.getOrganizationName()+"_"+station.getStationName()+plannedIndicator)){
 									res.remove(i);
 									break;
 								}
 							}
 							res.add(s);
 						}else if(!resNames.contains(s.getOrganizationName()+"_"+station.getStationName())){
 							res.add(s);
 							++expectedAtoNs;
 							
 							resNames.add(s.getOrganizationName()+"_"+station.getStationName());
 						}
 						
 						if(usedNames.contains(s.getOrganizationName()+"_"+station.getStationName()) && planned.length() <= 0) { //There is a planned station added previously --> need to remove it.
 							for(int i = 0; i < used.size(); ++i){
 								String sta = used.get(i).getOrganizationName()+"_"+used.get(i).getStationName(); 
 								if(sta.equals(s.getOrganizationName()+"_"+station.getStationName()+plannedIndicator)){
 									used.remove(i);
 									break;
 								}
 							}
 							used.add(s);
 						}else if(!usedNames.contains(s.getOrganizationName()+"_"+station.getStationName())){
 							used.add(s);
 							++atons;
 							
 							usedNames.add(s.getOrganizationName()+"_"+station.getStationName());
 						}
 						
 						resStations.add(s.getStationName()+"_"+s.getOrganizationName());
 					}
 				}
 			}
 		
 			boolean addRule1 = false;
 		
 			if(interference.get(slot+"") != null){ //Check the interferences
 					
 				List<AISStation> infs = new ArrayList<AISStation>();
 				
 				Set<String> infNames = new HashSet<String>();
 				for(AISFixedStationData station : interference.get(slot+"")){
 					String planned = "";
 					if(station.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED) planned = plannedIndicator;
 					
 					AISStation s = new AISStation(station.getOperator().getOrganizationName(), station.getStationName()+planned, station.getLat(), station.getLon());
 					s.setDbId(station.getStationDBID());
 					s.setStationType(station.getStationType());	
 					
 					if(!resStations.contains(s.getStationName()+"_"+s.getOrganizationName())){
 						if(!infNames.contains(s.getOrganizationName()+"_"+station.getStationName())){
 							infs.add(s);
 							infNames.add(s.getOrganizationName()+"_"+station.getStationName());
 						}else if(infNames.contains(s.getOrganizationName()+"_"+station.getStationName()) && planned.length() <= 0){ //Replace planned with active.
 							for(int i = 0; i < infs.size(); ++i){
 								String sta = infs.get(i).getOrganizationName()+"_"+infs.get(i).getStationName(); 
 								if(sta.equals(s.getOrganizationName()+"_"+station.getStationName()+plannedIndicator)){
 									infs.remove(i);
 									break;
 								}
 							}
 							
 							infs.add(s);
 						}
 					}
 					
 					
 					
 					
 				}
 					
 				
 				if(infs.size() > 0){
 					a.setPossibleConflicts(new Boolean(true));
 					a.setInterferedBy(infs); //Store the interfering stations
 					
 					//Create a new issue with Rule 1.
 					List<AISTimeslot> slots = new ArrayList<AISTimeslot>();
 					slots.add(a);
 					
 					List<AISStation> ps = new ArrayList<AISStation>();
 					ps.addAll(used);
 					if(a.getInterferedBy() != null)
 						ps.addAll(a.getInterferedBy());
 					
 					
 					
 					
 					
 					if(ps.size() > 1){
 						AISDatalinkCheckIssue issue = new AISDatalinkCheckIssue(-1,AISDatalinkCheckRule.RULE1,getRuleSeverity(AISDatalinkCheckRule.RULE1),ps,slots);
 					
 						issues.add(issue);
 						addRule1 = true;
 					}
 				}
 				
 			}else if(used.size() > 1){ //Several stations use the slot
 				
 				a.setPossibleConflicts(new Boolean(true));
 				
 				if(!addRule1){
 					//Create a new issue with Rule 1 if it has not been created yet.
 					List<AISTimeslot> slots = new ArrayList<AISTimeslot>();
 					slots.add(a);
 					
 					List<AISStation> ps = new ArrayList<AISStation>();
 					ps.addAll(used);
 					if(a.getInterferedBy() != null)
 						ps.addAll(a.getInterferedBy());
 					
 //					if(ps.size() < 2) System.out.println("Too few stations 2: "+used.size()+" + "+a.getInterferedBy().size());
 					AISDatalinkCheckIssue issue = new AISDatalinkCheckIssue(-1,AISDatalinkCheckRule.RULE1,getRuleSeverity(AISDatalinkCheckRule.RULE1),ps,slots);
 									
 					issues.add(issue);
 				}
 			}else{
 				a.setPossibleConflicts(new Boolean(false));
 			}
 			
 
 			
 			a.setReservedBy(res);
 			a.setUsedBy(used);
 			a.setFree(new Boolean(false));
 			
 			if(atons != expectedAtoNs){
 				AISDatalinkCheckIssue issue = null;
 				
 				if(atons > expectedAtoNs){
 					//AtoN usage with no reservation!
 //					System.out.println("AtoNs and expected AtoN does not match! AtoN usage without reservation!");
 					List<AISStation> atonStations = new ArrayList<AISStation>();
 					for(AISStation as : used){
 						if(as.getStationType().equals(AISFixedStationType.ATON)){
 							atonStations.add(as);
 						}
 					}
 					List<AISTimeslot> slots = new ArrayList<AISTimeslot>();
 					slots.add(a);
 				
 					issue = new AISDatalinkCheckIssue(-1,AISDatalinkCheckRule.RULE3, getRuleSeverity(AISDatalinkCheckRule.RULE3),atonStations,slots);
 					
 					issues.add(issue);
 				}else{
 					//Reservation but no usage!
 //					System.out.println("AtoNs and expected AtoN does not match! Reservation without AtoN usage!");
 						
 				}
 					
 				a.setPossibleConflicts(new Boolean(true));
 			}
 		}
 		
 		return a;
 	}
 	
 	
 	
 	/**
 	 * 
 	 * Gets the stations and checks the bandwith of the given point. The point is the top left corner of the area.
 	 * 
 	 * @param lat TOP LEFT Lat point of the area
 	 * @param lon TOP LEFT Lon point of the area
 	 * @return
 	 */
 	public AISDatalinkCheckResult checkRulesAtPoint(double lat, double lon, double endLat, double endLon, double resolution){
 		if(this.data == null){
 			
 			this.data = DBHandler.getData(); 
 		}
 		
 		double[] point = {lat, lon};
 		
 		EAVDAMData filtered = getStationsAtPoint(data, point);
 		AISDatalinkCheckResult result = new AISDatalinkCheckResult();
 		
 
 		
 		if(filtered == null) return null;
 
 		Map<String,List<AISFixedStationData>> reservationsA = new HashMap<String, List<AISFixedStationData>>();
 		Map<String,List<AISFixedStationData>> reservationsB = new HashMap<String, List<AISFixedStationData>>();
 		
 		List<AISDatalinkCheckIssue> issues = new ArrayList<AISDatalinkCheckIssue>();
 		
 		Set<String> handledStations = new HashSet<String>();
 		
 		
 		//Check all active stations
 		if(filtered.getActiveStations() != null){
 			for(ActiveStation as : filtered.getActiveStations()){
 				if(as.getStations() != null){
 					for(AISFixedStationData s : as.getStations()){
 						if(s.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED) continue; //TODO Include the PLANNED stations to the check also?
 						
 						if(handledStations.contains(s.getStationName()+"_"+s.getOperator().getOrganizationName())) continue;
 						
 						if(s.getFATDMAChannelA() != null){
 							for(Integer a : s.getReservedBlocksForChannelA()){
 								List<AISFixedStationData> reservations = reservationsA.get(a.intValue()+"");
 								if(reservations == null) reservations = new ArrayList<AISFixedStationData>();
 								
 								reservations.add(s);
 								
 //								if(reservations.size() > 1) System.out.println("TOO MANY RESERVATIONS IN A: "+a.intValue());
 								
 								reservationsA.put(a.intValue()+"", reservations);
 							}
 						}
 						
 						if(s.getFATDMAChannelB() != null){
 							for(Integer a : s.getReservedBlocksForChannelB()){
 								List<AISFixedStationData> reservations = reservationsB.get(a.intValue()+"");
 								if(reservations == null) reservations = new ArrayList<AISFixedStationData>();
 								
 								reservations.add(s);
 								
 								reservationsB.put(a.intValue()+"", reservations);
 							}
 						}
 						
 						handledStations.add(s.getStationName()+"_"+s.getOperator().getOrganizationName());
 					}
 				}
 				
 				
 			}
 		}
 //		System.out.println("Checked active stations..");
 		//TODO Is there a need to check proposals also? What to do to the planned stations?
 		
 		if(filtered.getOtherUsersStations() != null){
 			for(OtherUserStations other : filtered.getOtherUsersStations()){
 				if(other.getStations() != null){
 					for(ActiveStation as : other.getStations()){
 						if(as.getStations() != null){
 							for(AISFixedStationData s : as.getStations()){
 								if(s.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED) continue;
 								if(handledStations.contains(s.getStationName()+"_"+s.getOperator().getOrganizationName())) continue;
 								if(s.getFATDMAChannelA() != null){
 									for(Integer a : s.getReservedBlocksForChannelA()){
 										List<AISFixedStationData> reservations = reservationsA.get(a.intValue()+"");
 										if(reservations == null) reservations = new ArrayList<AISFixedStationData>();
 										
 										reservations.add(s);
 										
 										reservationsA.put(a.intValue()+"", reservations);
 									}
 								}
 								
 								if(s.getFATDMAChannelB() != null){
 									for(Integer a : s.getReservedBlocksForChannelB()){
 										List<AISFixedStationData> reservations = reservationsB.get(a.intValue()+"");
 										if(reservations == null) reservations = new ArrayList<AISFixedStationData>();
 										
 										reservations.add(s);
 										
 										reservationsB.put(a.intValue()+"", reservations);
 									}
 								}
 								
 								handledStations.add(s.getStationName()+"_"+s.getOperator().getOrganizationName());
 							}
 						}
 					}
 				}
 			}
 		}
 		
 
 		//Create slot map
 		AISSlotMap slotmap = new AISSlotMap();
 		
 		//Get the reservation %
 		double percentage = 1.0*(reservationsA.size()+reservationsB.size())/(numberOfFrequencies*numberOfSlotsPerFrequency);
 		slotmap.setBandwidthReservation(percentage);
 		slotmap.setLat(lat);
 		slotmap.setLon(lon);
 		
 		List<AISTimeslot> slotA = new ArrayList<AISTimeslot>();
 		List<AISTimeslot> slotB = new ArrayList<AISTimeslot>();
 		for(int i = 0; i < numberOfSlotsPerFrequency; ++i){
 			AISTimeslot a = new AISTimeslot();
 			AISTimeslot b = new AISTimeslot();
 			
 			a.setSlotNumber(i);
 			b.setSlotNumber(i);
 			
 			if(reservationsA.get(i+"") == null || reservationsA.get(i+"").size() <= 0){
 				a.setFree(new Boolean(true));
 			}else{
 				List<AISFixedStationData> stations = reservationsA.get(i+"");
 
 					List<AISStation> problems = new ArrayList<AISStation>();
 					for(AISFixedStationData station : reservationsA.get(i+"")){
 						
 						AISStation s = new AISStation(station.getOperator().getOrganizationName(), station.getStationName(), station.getLat(), station.getLon());
 					
 						problems.add(s);
 					}
 					
 					if(stations.size() > 1){
 
 						a.setPossibleConflicts(new Boolean(true));
 						a.setInterferedBy(problems);
 					}else{
 						a.setPossibleConflicts(new Boolean(false));
 					}
 					
 					a.setReservedBy(problems);
 					a.setUsedBy(problems); //TODO AtoN check
 					a.setFree(new Boolean(false));
 				
 			}
 			
 			if(reservationsB.get(i+"") == null){
 				b.setFree(new Boolean(true));
 			}else{
 				List<AISFixedStationData> stations = reservationsB.get(i+"");
 				
 					List<AISStation> problems = new ArrayList<AISStation>();
 					for(AISFixedStationData station : reservationsB.get(i+"")){
 						
 						AISStation s = new AISStation(station.getOperator().getOrganizationName(), station.getStationName(), station.getLat(), station.getLon());
 					
 						problems.add(s);
 					}
 				
 					if(stations.size() > 1){
 						b.setPossibleConflicts(new Boolean(true));
 						b.setInterferedBy(problems);
 					}else{
 						b.setPossibleConflicts(new Boolean(false));
 					}
 					
 					b.setReservedBy(problems);
 					b.setUsedBy(problems);
 					b.setFree(new Boolean(false));
 				
 			}
 			
 			
 			slotA.add(a);
 			slotB.add(b);
 		}
 		
 		slotmap.setAIS1Timeslots(slotA);
 		slotmap.setAIS2Timeslots(slotB);
 		
 		
 		
 		AISDatalinkCheckArea area = new AISDatalinkCheckArea(lat, lon, endLat, endLon, percentage);
 		List<AISDatalinkCheckArea> areas = new ArrayList<AISDatalinkCheckArea>();
 		areas.add(area);
 		result.setAreas(areas);
 		
 		return result;
 	}
 	
 	
 	/**
 	 * Gets the stations with the coverage at the given point. Both interference coverage and transmission coverage are checked. If only one of those is needed, use getStationsAtPoint(EAVDAMData data, double[] point, int coverageType).
 	 * 
 	 * @param data The EAVDAMData object that holds all the stations and their covarege areas.
 	 * @param point Array of two double values where point[0] = lat and point[1] = lon.
 	 * @return EAVDAMData object that is filtered to only contain the stations which have a coverage area overlapping the point.
 	 */
 	public static EAVDAMData getStationsAtPoint(EAVDAMData data, double[] point){
 		EAVDAMData filtered = new EAVDAMData();
 		
 		if(data == null) return filtered;
 		
 		
 		if(data.getActiveStations() != null){
 			List<ActiveStation> activeStations = new ArrayList<ActiveStation>();
 			for(ActiveStation as : data.getActiveStations()){
 				List<AISFixedStationData> stations = new ArrayList<AISFixedStationData>();
 
 				for(AISFixedStationData s : as.getStations()){
 //					System.out.println("Checking: "+s.getLat()+";"+s.getLon());
 //					if(s.getLat() == 60 && s.getLon() == 24) System.out.println("\t"+s.getInterferenceCoverage().getCoveragePoints()+" | "+s.getTransmissionCoverage().getCoveragePoints());
 					if(PointInPolygon.isPointInPolygon(s.getInterferenceCoverage().getCoveragePoints(), point)){
 //						System.out.println("ADDED: (I)"+s.getLat()+";"+s.getLon());
 						stations.add(s);
 					}else if(PointInPolygon.isPointInPolygon(s.getTransmissionCoverage().getCoveragePoints(), point)){
 //						System.out.println("ADDED (T): "+s.getLat()+";"+s.getLon());
 						stations.add(s);
 					}
 				}
 				
 //				System.out.println("Stations: "+stations.size());
 				ActiveStation a = new ActiveStation();
 				if(stations.size() > 0){
 					a.setStations(stations);
 					activeStations.add(a);
 				}else{
 					a.setStations(null);
 				}
 	
 				if(as.getProposals() != null){
 					boolean addedProposal = false;
 					for(EAVDAMUser u : as.getProposals().keySet()){
 						if(PointInPolygon.isPointInPolygon(as.getProposals().get(u).getInterferenceCoverage().getCoveragePoints(), point)){
 							Map<EAVDAMUser, AISFixedStationData> proposals = new HashMap<EAVDAMUser, AISFixedStationData>();
 							proposals.put(u, as.getProposals().get(u));
 							a.setProposals(proposals);
 							addedProposal = true;
 						}else if(PointInPolygon.isPointInPolygon(as.getProposals().get(u).getTransmissionCoverage().getCoveragePoints(), point)){
 							Map<EAVDAMUser, AISFixedStationData> proposals = new HashMap<EAVDAMUser, AISFixedStationData>();
 							proposals.put(u, as.getProposals().get(u));
 							a.setProposals(proposals);
 							addedProposal = true;
 						}
 					}
 					
 					if(as.getStations() != null && as.getStations().size() == 0 && stations.size() == 0){
 						a.setStations(stations);
 					}
 					
 					if(addedProposal)
 						activeStations.add(a);
 				}
 				
 
 			}
 			
 //			System.out.println("Active stations checked. Result: "+activeStations.size());
 			filtered.setActiveStations(activeStations);
 		}
 		
 		if(data.getOtherUsersStations() != null){
 			List<OtherUserStations> others = new ArrayList<OtherUserStations>();
 			for(OtherUserStations other : data.getOtherUsersStations()){
 				List<ActiveStation> otherActiveStations = new ArrayList<ActiveStation>();
 				for(ActiveStation as : other.getStations()){
 					List<AISFixedStationData> stations = new ArrayList<AISFixedStationData>();
 					
 					for(AISFixedStationData s : as.getStations()){
 						if(PointInPolygon.isPointInPolygon(s.getInterferenceCoverage().getCoveragePoints(), point)){
 //							System.out.println("ADDED OTHER (I): "+s.getLat()+";"+s.getLon());
 							stations.add(s);
 						}else if(PointInPolygon.isPointInPolygon(s.getTransmissionCoverage().getCoveragePoints(), point)){
 //							System.out.println("ADDED OTHER (T): "+s.getLat()+";"+s.getLon());
 							stations.add(s);
 						}
 					}
 					
 					ActiveStation a = new ActiveStation();
 					if(stations.size() > 0){
 						a.setStations(stations);
 					}else if(as.getStations() != null && as.getStations().size() == 0 && stations.size() == 0){
 						a.setStations(stations);
 					}else{
 						a.setStations(null);
 					}
 					
 					otherActiveStations.add(a);
 					
 				}
 				
 				OtherUserStations o = new OtherUserStations();
 				o.setUser(other.getUser());
 				o.setStations(otherActiveStations);
 				others.add(o);
 				
 			}
 			
 			filtered.setOtherUsersStations(others);
 		}
 		
 		
 		if(data.getSimulatedStations() != null){
 			List<Simulation> simulations = new ArrayList<Simulation>();
 			
 			for(Simulation sim : data.getSimulatedStations()){
 				List<AISFixedStationData> stations = new ArrayList<AISFixedStationData>();
 				
 				for(AISFixedStationData s : sim.getStations()){
 					if(PointInPolygon.isPointInPolygon(s.getInterferenceCoverage().getCoveragePoints(), point)){
 						stations.add(s);
 					}else if(PointInPolygon.isPointInPolygon(s.getTransmissionCoverage().getCoveragePoints(), point)){
 						stations.add(s);
 					}
 				}
 				
 				Simulation s = new Simulation();
 				s.setName(sim.getName());
 				
 				if(stations.size() > 0){
 					s.setStations(stations);
 				}else if(sim.getStations() != null && sim.getStations().size() == 0 && stations.size() == 0){
 					s.setStations(stations);
 				}else{
 					s.setStations(null);
 				}
 	
 				simulations.add(s);
 			}
 			
 			filtered.setSimulatedStations(simulations);
 		}
 
 		
 		return filtered;
 	}
 	
 	public static double getMinResolution(double topLeftLatitude, double topLeftLongitude, double lowerRightLatitude, double lowerRightLongitude){
 		
 		for(double resolution = 0.1 ; resolution <= 5.0; resolution = 0.25 + resolution){
 			double latIncrement = getLatitudeIncrement(resolution, topLeftLatitude, topLeftLongitude, lowerRightLatitude, lowerRightLongitude);
 			if(latIncrement < 0) latIncrement *= -1;
 		
 			double lonIncrement = getLongitudeIncrement(resolution, topLeftLatitude, topLeftLongitude, lowerRightLatitude, lowerRightLongitude);
 			if(lonIncrement < 0) lonIncrement *= -1;
 		
 			double numberOfCells = 1.0*(topLeftLatitude-lowerRightLatitude)/latIncrement * 1.0*(lowerRightLongitude-topLeftLongitude)/lonIncrement;
 			
 			if(numberOfCells < maxNumberOfCells) return resolution;
 		}
 		
 		return -1;
 	}
 	
 	/**
 	 * Gets the stations with the coverage at the given point. Both interference coverage and transmission coverage are checked. If only one of those is needed, use getStationsAtPoint(EAVDAMData data, double[] point, int coverageType).
 	 * 
 	 * @param data The EAVDAMData object that holds all the stations and their covarege areas.
 	 * @param point Array of two double values where point[0] = lat and point[1] = lon.
 	 * @param coverageType Type of coverage. Use HealthCheckHandler.TRANSMISSION_COVERAGE or HealthCheckHandler.INTERFERENCE_COVERAGE  
 	 * @return List of stations with coverage at the given point.
 	 */
 	public static EAVDAMData getStationsAtPoint(EAVDAMData data, double[] point, int coverageType){
 		EAVDAMData filtered = new EAVDAMData();
 		
 		if(data == null) return filtered;
 		
 		int addedStations = 0;
 		if(data.getActiveStations() != null){
 			List<ActiveStation> activeStations = new ArrayList<ActiveStation>();
 			for(ActiveStation as : data.getActiveStations()){
 				List<AISFixedStationData> stations = new ArrayList<AISFixedStationData>();
 
 				for(AISFixedStationData s : as.getStations()){
 					
 //					System.out.println("Checking: "+s.getLat()+";"+s.getLon());
 //					if(s.getLat() == 60 && s.getLon() == 24) System.out.println("\t"+s.getInterferenceCoverage().getCoveragePoints()+" | "+s.getTransmissionCoverage().getCoveragePoints());
 					if(s.getInterferenceCoverage() != null && coverageType == INTERFERENCE_COVERAGE && PointInPolygon.isPointInPolygon(s.getInterferenceCoverage().getCoveragePoints(), point)){
 //						System.out.println("ADDED: (I)"+s.getLat()+";"+s.getLon());
 						stations.add(s);
 						++addedStations;
 					}else if(s.getTransmissionCoverage() != null && coverageType == TRANSMISSION_COVERAGE && PointInPolygon.isPointInPolygon(s.getTransmissionCoverage().getCoveragePoints(), point)){
 //						System.out.println("ADDED (T): "+s.getLat()+";"+s.getLon());
 						stations.add(s);
 						++addedStations;
 					}
 				}
 				
 //				System.out.println("Stations: "+stations.size());
 				ActiveStation a = new ActiveStation();
 				if(stations.size() > 0){
 					a.setStations(stations);
 					activeStations.add(a);
 				}else{
 					a.setStations(null);
 				}
 	
 				if(as.getProposals() != null){
 					boolean addedProposal = false;
 					for(EAVDAMUser u : as.getProposals().keySet()){
 						if(as.getProposals().get(u).getInterferenceCoverage() != null && coverageType == INTERFERENCE_COVERAGE && PointInPolygon.isPointInPolygon(as.getProposals().get(u).getInterferenceCoverage().getCoveragePoints(), point)){
 							Map<EAVDAMUser, AISFixedStationData> proposals = new HashMap<EAVDAMUser, AISFixedStationData>();
 							proposals.put(u, as.getProposals().get(u));
 							a.setProposals(proposals);
 							addedProposal = true;
 						}else if(as.getProposals().get(u).getTransmissionCoverage() != null && coverageType == TRANSMISSION_COVERAGE && PointInPolygon.isPointInPolygon(as.getProposals().get(u).getTransmissionCoverage().getCoveragePoints(), point)){
 							Map<EAVDAMUser, AISFixedStationData> proposals = new HashMap<EAVDAMUser, AISFixedStationData>();
 							proposals.put(u, as.getProposals().get(u));
 							a.setProposals(proposals);
 							addedProposal = true;
 						}
 					}
 					
 					if(as.getStations() != null && as.getStations().size() == 0 && stations.size() == 0){
 						a.setStations(stations);
 					}
 					
 					if(addedProposal)
 						activeStations.add(a);
 				}
 				
 
 			}
 			
 //			System.out.println("Active stations checked. Result: "+activeStations.size());
 			filtered.setActiveStations(activeStations);
 		}
 		
 		if(data.getOtherUsersStations() != null){
 			List<OtherUserStations> others = new ArrayList<OtherUserStations>();
 			for(OtherUserStations other : data.getOtherUsersStations()){
 				List<ActiveStation> otherActiveStations = new ArrayList<ActiveStation>();
 				for(ActiveStation as : other.getStations()){
 					List<AISFixedStationData> stations = new ArrayList<AISFixedStationData>();
 					
 					for(AISFixedStationData s : as.getStations()){
 //						System.out.println(s.getStationName()+"-"+s.getOperator().getOrganizationName()+". Coverage: "+s.getTransmissionCoverage().getCoveragePoints().size());
 						
 						
 						
 						if(s.getInterferenceCoverage() != null && coverageType == INTERFERENCE_COVERAGE && PointInPolygon.isPointInPolygon(s.getInterferenceCoverage().getCoveragePoints(), point)){
 //							System.out.println("ADDED OTHER (I): "+s.getLat()+";"+s.getLon());
 							stations.add(s);
 							++addedStations;
 						}else if(s.getTransmissionCoverage() != null && coverageType == TRANSMISSION_COVERAGE && PointInPolygon.isPointInPolygon(s.getTransmissionCoverage().getCoveragePoints(), point)){
 //							System.out.println("ADDED OTHER (T): "+s.getLat()+";"+s.getLon());
 
 							stations.add(s);
 							++addedStations;
 						}
 					}
 					
 					ActiveStation a = new ActiveStation();
 					if(stations.size() > 0){
 						a.setStations(stations);
 					}else if(as.getStations() != null && as.getStations().size() == 0 && stations.size() == 0){
 						a.setStations(stations);
 					}else{
 						a.setStations(null);
 					}
 					
 					otherActiveStations.add(a);
 					
 				}
 				
 				OtherUserStations o = new OtherUserStations();
 				o.setUser(other.getUser());
 				o.setStations(otherActiveStations);
 				others.add(o);
 				
 			}
 			
 			filtered.setOtherUsersStations(others);
 		}
 		
 		
 		if(data.getSimulatedStations() != null){
 			List<Simulation> simulations = new ArrayList<Simulation>();
 			
 			for(Simulation sim : data.getSimulatedStations()){
 				List<AISFixedStationData> stations = new ArrayList<AISFixedStationData>();
 				
 				for(AISFixedStationData s : sim.getStations()){
 					if(s.getInterferenceCoverage() != null && coverageType == INTERFERENCE_COVERAGE && PointInPolygon.isPointInPolygon(s.getInterferenceCoverage().getCoveragePoints(), point)){
 						stations.add(s);
 						++addedStations;
 					}else if(s.getTransmissionCoverage() != null && coverageType == TRANSMISSION_COVERAGE && PointInPolygon.isPointInPolygon(s.getTransmissionCoverage().getCoveragePoints(), point)){
 						stations.add(s);
 						++addedStations;
 					}
 				}
 				
 				Simulation s = new Simulation();
 				s.setName(sim.getName());
 				
 				if(stations.size() > 0){
 					s.setStations(stations);
 				}else if(sim.getStations() != null && sim.getStations().size() == 0 && stations.size() == 0){
 					s.setStations(stations);
 				}else{
 					s.setStations(null);
 				}
 	
 				simulations.add(s);
 			}
 			
 			filtered.setSimulatedStations(simulations);
 		}
 
 		if(addedStations <= 0) return null;
 		
 		return filtered;
 	}
 	
 	private double[] getLatitudeBorders(){
 		if(this.data == null) return null;
 		
 		double maxLatitude = Double.MIN_VALUE;
 		double minLatitude = Double.MAX_VALUE;
 		
 		
 		return null;
 	}
 	
 
 	
 	private static double getLatituteChangeForResolution(double resolution, double lat){
 		return 1.0*resolution/(60.0/Math.cos(lat));
 	}
 	
 	private static double getLongitudeChangeForResolution(double resolution, double lon){
 		return 1.0*resolution/(60.0/Math.cos(lon));
 	}
 	
 	/**
 	 * Distance between two points
 	 * 
 	 * @param lat1
 	 * @param lon1
 	 * @param lat2
 	 * @param lon2
 	 * @param unit
 	 * @return
 	 */
 	private static double distanceBetweenPoints(double lat1, double lon1, double lat2, double lon2) {
 		  double lonDiff = lon1 - lon2;
 		  double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(lonDiff));
 		  dist = Math.acos(dist);
 		  dist = rad2deg(dist);
 		  dist = dist * 60 * 1.1515 * 0.8684; //To Nautical Miles
 		  
 		  return dist;
 		}
 
 	private static double deg2rad(double deg) {
 	  return (deg * Math.PI / 180.0);
 	}
 
 	private static double rad2deg(double rad) {
 	  return (rad * 180.0 / Math.PI);
 	}
 	
 
 	
 	private static double getLatitudeIncrement(double resolution, double startLat, double startLon, double endLat, double endLon){
 		double distanceInLatitude = distanceBetweenPoints(startLat, startLon, endLat, startLon); //Keep the longitude the same
 		
 		double nCells = distanceInLatitude/resolution;
 		
 		return 1.0*(startLat-endLat)/nCells;
 		
 	}
 	
 	private static double getLongitudeIncrement(double resolution, double startLat, double startLon, double endLat, double endLon){
 		double distanceInLatitude = distanceBetweenPoints(startLat, startLon, startLat, endLon); //Keep the latitude the same
 		
 		double nCells = distanceInLatitude/resolution;
 		
 		return 1.0*(startLon-endLon)/nCells;
 	}
 	
 	public static void isPolygonIntersection(){
 		
 	}
 	
 	public static AISDatalinkCheckSeverity getRuleSeverity(AISDatalinkCheckRule rule){
 		//Rule 1:
 		if(rule.equals(AISDatalinkCheckRule.RULE1)){
 			return AISDatalinkCheckSeverity.SEVERE;
 		}else if(rule.equals(AISDatalinkCheckRule.RULE2)){
 			return AISDatalinkCheckSeverity.MAJOR;
 		}else if(rule.equals(AISDatalinkCheckRule.RULE3)){
 			return AISDatalinkCheckSeverity.MAJOR;
 		}else if(rule.equals(AISDatalinkCheckRule.RULE4)){
 			return AISDatalinkCheckSeverity.MAJOR;
 		}else if(rule.equals(AISDatalinkCheckRule.RULE5)){
 			return AISDatalinkCheckSeverity.MINOR;
 		}else if(rule.equals(AISDatalinkCheckRule.RULE6)){
 			return AISDatalinkCheckSeverity.MINOR;
 		}else if(rule.equals(AISDatalinkCheckRule.RULE7)){
 			return AISDatalinkCheckSeverity.MAJOR;
 		}
 		
 		return null;
 	}
 	
 	public static AISDatalinkCheckRule getRule(int ruleNumber){
 		//Rule 1:
 		if(ruleNumber == 1) return AISDatalinkCheckRule.RULE1;
 		else if(ruleNumber == 2) return AISDatalinkCheckRule.RULE2;
 		else if(ruleNumber == 3) return AISDatalinkCheckRule.RULE3;
 		else if(ruleNumber == 4) return AISDatalinkCheckRule.RULE4;
 		else if(ruleNumber == 5) return AISDatalinkCheckRule.RULE5;
 		else if(ruleNumber == 6) return AISDatalinkCheckRule.RULE6;
 		else if(ruleNumber == 7) return AISDatalinkCheckRule.RULE7;
 		
 		return null;
 	}
 
 	public static int getRuleInt(AISDatalinkCheckRule ruleViolated) {
 		//Rule 1:
 		if(ruleViolated.equals(AISDatalinkCheckRule.RULE1)) return 1;
 		else if(ruleViolated.equals(AISDatalinkCheckRule.RULE2)) return 2;
 		else if(ruleViolated.equals(AISDatalinkCheckRule.RULE3)) return 3;
 		else if(ruleViolated.equals(AISDatalinkCheckRule.RULE4)) return 4;
 		else if(ruleViolated.equals(AISDatalinkCheckRule.RULE5)) return 5;
 		else if(ruleViolated.equals(AISDatalinkCheckRule.RULE6)) return 6;
 		else if(ruleViolated.equals(AISDatalinkCheckRule.RULE7)) return 7;
 		
 		
 		return 0;
 	}
 	
 	public void setCancelled(boolean cancelled){
 		this.cancelled = cancelled;
 	}
 	
 	private EAVDAMData copyCoverageAreasToEAVDAMData(Map<String,AISFixedStationCoverage> cov){
 		if(this.data != null && this.data.getOtherUsersStations() != null && this.data.getOtherUsersStations().size() > 0){
 			for(OtherUserStations ous : this.data.getOtherUsersStations()){
 				EAVDAMUser user = ous.getUser();
 				
 				if(ous.getStations() != null){
 					for(ActiveStation as : ous.getStations()){
 						if(as.getStations() != null){
 							for(AISFixedStationData s : as.getStations()){
 								
 								s.setTransmissionCoverage(cov.get("T:"+user.getOrganizationName()+"-"+s.getStationName()));
 								s.setInterferenceCoverage(cov.get("I:"+user.getOrganizationName()+"-"+s.getStationName()));
 								s.setReceiveCoverage(cov.get("R:"+user.getOrganizationName()+"-"+s.getStationName()));
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		return this.data;
 	}
 }
