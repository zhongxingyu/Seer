 package dk.frv.eavdam.utils;
 
 import java.util.ArrayList;
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
 import dk.frv.eavdam.io.jaxb.AisFixedStationStatus;
 import dk.frv.eavdam.io.jaxb.AisFixedStationType;
 
 public class HealthCheckHandler {
 
 	public static final int TRANSMISSION_COVERAGE = 1;
 	public static final int INTERFERENCE_COVERAGE = 2;
 	EAVDAMData data = null;
 	
 	public static String plannedIndicator = " (P)";
 	public static int numberOfFrequencies = 2;
 	public static int numberOfSlotsPerFrequency = 2250;
 	private Map<String, AISDatalinkCheckArea> areaIssueMap = null; //Store the issues found from the area to this map.
 	
 	private Map<String, AISFixedStationData> stations = null;
 	
 	public HealthCheckHandler(EAVDAMData data){
 		this.data = data;
 	}
 	
 	public AISDatalinkCheckResult startAISDatalinkCheck(AISDatalinkCheckListener listener, boolean checkRule1, boolean checkRule2, boolean checkRule3, 
 			boolean checkRule4, boolean checkRule5, boolean checkRule6, boolean checkRule7, 
 			double topLeftLatitude, double topLeftLongitude, double lowerRightLatitude, double lowerRightLongitude, double resolution){
 		
 		AISDatalinkCheckResult results = new AISDatalinkCheckResult();
 		
 		this.data = DBHandler.getData(topLeftLatitude + 2.5, topLeftLongitude - 4.5, lowerRightLatitude -2.5, lowerRightLongitude + 4.5);  
 		
 		
 		Map<String, Map<String,AISFixedStationData>> overlappingStations = this.findOverlappingStations(topLeftLatitude + 2.5, topLeftLongitude - 4.5, lowerRightLatitude -2.5, lowerRightLongitude + 4.5);
 		
 		List<AISDatalinkCheckIssue> issues = new ArrayList<AISDatalinkCheckIssue>();
 		Set<String> foundProblems = new HashSet<String>();
 		
 		
 		
 		//AREA FOCUSED HEALTH CHECK
 		double latIncrement = getIncrementedLatitude(resolution, topLeftLatitude)-topLeftLatitude;
 		if(latIncrement < 0) latIncrement *= -1;
 		
 		double lonIncrement = getIncrementedLongitude(resolution, topLeftLongitude) - topLeftLongitude;
 		if(lonIncrement < 0) lonIncrement *= -1;
 		
 		System.out.println("Starting area focused health check (Rules 1, 3, and 7).");
 		List<AISDatalinkCheckArea> areas = new ArrayList<AISDatalinkCheckArea>();
 		//Loop through the area?
 		
 		int ithLine = 0, ithTotal = 0;
 		double prevLat = 0, prevLon = 0;
 		Set<String> foundAreas = new HashSet<String>();
 		double numberOfCells = 1.0*(topLeftLatitude-lowerRightLatitude)/latIncrement * 1.0*(lowerRightLongitude-topLeftLongitude)/lonIncrement;
 		AISSlotMap prevSlotMap = null;
 		for(double lat = topLeftLatitude ; lat > lowerRightLatitude; lat = lat - latIncrement){
 //			if(true) break;
 			++ithLine;
 			
 			int ithColumn = 0;
 			for(double lon = topLeftLongitude; lon < lowerRightLongitude; lon = lon + lonIncrement){
 				++ithColumn;
 				++ithTotal;
 				AISSlotMap sm = slotMapAtPoint(lat, lon);
 				
 				if(ithLine > 1 && ithColumn > 1){
 					//Make the average
 					AISDatalinkCheckArea a1 = this.areaIssueMap.remove(prevLat+";"+prevLon); //Only remove this one
 					if(a1 == null){
 						System.out.println("A1 is null");
 						continue;
 					}
 					
 					AISDatalinkCheckArea a2 = this.areaIssueMap.get(prevLat+";"+lon);
 					if(a2 == null){
 						System.out.println("A2 is null");
 						continue;
 					}
 					
 					AISDatalinkCheckArea a3 = this.areaIssueMap.get(lat+";"+prevLon);
 					if(a3 == null){
 						System.out.println("A3 is null");
 						continue;
 					}
 					
 					AISDatalinkCheckArea a4 = this.areaIssueMap.get(lat+";"+lon);
 					if(a4 == null){
 						System.out.println("A4 is null");
 						continue;
 					}
 					
 					
 					this.areaIssueMap.put(prevLat+";"+prevLon+"-"+lat+";"+lon, this.getAverageArea(a1, a2, a3, a4));
 					foundAreas.add(prevLat+";"+prevLon+"-"+lat+";"+lon);
 				}
 				
 
 				
 				prevLon = lon;
 			}
 			System.out.println("Total "+ithTotal+"/"+((int)numberOfCells)+"");
 			prevLat = lat;
 			
 		}
 		System.out.println("Area focused health check completed...\nStarting station focused health check (Rules 2, 4, 5, and 6)");
 		
 		
 		for(String s : overlappingStations.keySet()){
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
 					stations.add(s1);
 					
 					String problemName = "RULE2_"+s1.getOrganizationName()+"_"+s1.getStationName();
 					
 					if(!foundProblems.contains(problemName)){
 						AISDatalinkCheckIssue issue = new AISDatalinkCheckIssue(-1,AISDatalinkCheckRule.RULE2,AISDatalinkCheckSeverity.MAJOR,stations,problems);
 						
 						foundProblems.add(problemName);
 						
 						issues.add(issue);
 					}
 				}
 				
 			}
 			
 			if(checkRule3){
 				List<AISTimeslot> problems = this.checkRule4(station);
 				if(problems != null && problems.size() > 0){
 					
 					List<AISStation> stations = new ArrayList<AISStation>();
 					AISStation s1 = new AISStation(station.getOperator().getOrganizationName(), station.getStationName(), station.getLat(), station.getLon());
 					stations.add(s1);
 					
 					String problemName = "RULE3_"+s1.getOrganizationName()+"_"+s1.getStationName();
 					if(!foundProblems.contains(problemName)){
 						AISDatalinkCheckIssue issue = new AISDatalinkCheckIssue(-1,AISDatalinkCheckRule.RULE3,AISDatalinkCheckSeverity.MAJOR,stations,problems);
 						
 						foundProblems.add(problemName);
 						
 						issues.add(issue);
 					}
 				}
 			}
 			
 			if(checkRule4){
 				List<AISTimeslot> problems = this.checkRule4(station);
 				if(problems != null && problems.size() > 0){
 					
 					List<AISStation> stations = new ArrayList<AISStation>();
 					AISStation s1 = new AISStation(station.getOperator().getOrganizationName(), station.getStationName(), station.getLat(), station.getLon());
 					stations.add(s1);
 					
 					String problemName = "RULE4_"+s1.getOrganizationName()+"_"+s1.getStationName();
 					if(!foundProblems.contains(problemName)){
 						AISDatalinkCheckIssue issue = new AISDatalinkCheckIssue(-1,AISDatalinkCheckRule.RULE4,AISDatalinkCheckSeverity.MAJOR,stations,problems);
 						
 						foundProblems.add(problemName);
 						
 						issues.add(issue);
 					}
 				}
 				
 				
 			}
 			
 			for(String v : overlappingStations.get(s).keySet()){
 				if(checkRule1){
 					//Checking the first rule.
 					List<AISTimeslot> problems = this.checkRule1(station, overlappingStations.get(s).get(v));
 					if(problems != null && problems.size() > 0){
 						
 						AISFixedStationData station2 = overlappingStations.get(s).get(v);
 						
 						List<AISStation> stations = new ArrayList<AISStation>();
 						AISStation s1 = new AISStation(station.getOperator().getOrganizationName(), station.getStationName(), station.getLat(), station.getLon());
 						stations.add(s1);
 						
 						AISStation s2 = new AISStation(station2.getOperator().getOrganizationName(), station2.getStationName(), station2.getLat(), station2.getLon());
 						stations.add(s2);
 						
 						String problemName = "RULE1_"+s1.getOrganizationName()+"_"+s1.getStationName()+" "+s2.getOrganizationName()+"_"+s2.getStationName();
 						String problemName2 = "RULE1_"+s2.getOrganizationName()+"_"+s2.getStationName()+" "+s1.getOrganizationName()+"_"+s1.getStationName();
 						if(!foundProblems.contains(problemName) && !foundProblems.contains(problemName2) ){
 							AISDatalinkCheckIssue issue = new AISDatalinkCheckIssue(-1,AISDatalinkCheckRule.RULE1,AISDatalinkCheckSeverity.SEVERE,stations,problems);
 							
 							foundProblems.add(problemName);
 							foundProblems.add(problemName2);
 							
 							issues.add(issue);
 						}
 					}
 				}
 				
 
 				
 
 			}
 		}
 		
 		System.out.println("Health checks completed...");
 //		for(AISDatalinkCheckIssue issue : issues){
 //			System.out.println(issue.toString());
 //		}
 		
 		for(String keys : foundAreas){
 			areas.add(this.areaIssueMap.get(keys));
 		}
 		
 		
 		results.setIssues(issues);
 		results.setAreas(areas);
 		
 		return results;
 	}
 	
 	private AISDatalinkCheckArea getAverageArea(AISDatalinkCheckArea a1, AISDatalinkCheckArea a2, AISDatalinkCheckArea a3, AISDatalinkCheckArea a4){
 		AISDatalinkCheckArea area = new AISDatalinkCheckArea(a1.getTopLeftLatitude(), a1.getTopLeftLongitude(),a4.getTopLeftLatitude(),a4.getTopLeftLongitude());
 		
 		List<AISDatalinkCheckIssue> allIssues = new ArrayList<AISDatalinkCheckIssue>();
 		allIssues.addAll(a1.getIssues());
 		allIssues.addAll(a2.getIssues());
 		allIssues.addAll(a3.getIssues());
 		allIssues.addAll(a4.getIssues());
 		
 		this.trimIssueList(allIssues);
 		
 		area.setIssues(allIssues);
 		area.setSlotmap(a4.getSlotmap()); //TODO make an average slotmap!
 		
 		
 		return area;
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
 			double lowerRightLongitude) {
 	
 		if(this.data == null) this.data = DBHandler.getData(topLeftLatitude + 2.5, topLeftLongitude - 4.5, lowerRightLatitude -2.5, lowerRightLongitude + 4.5);
 
 		if(this.stations == null) stations = new HashMap<String, AISFixedStationData>();
 		
 		Map<String, Map<String,AISFixedStationData>> overlappingStations = new HashMap<String, Map<String,AISFixedStationData>>();
 		
 		if(data.getActiveStations() != null){
 			for(ActiveStation as : data.getActiveStations()){
 				if(as.getStations() != null){
 					for(AISFixedStationData s1 : as.getStations()){
 						String s1Name = s1.getOperator().getOrganizationName()+"-"+s1.getStationName();
 						stations.put(s1Name, s1);
 						Map<String,AISFixedStationData> overlaps = overlappingStations.get(s1Name);
 						if(overlaps == null) overlaps = new HashMap<String, AISFixedStationData>();
 								
 						
 						for(AISFixedStationData s2 : as.getStations()){
 							if(s1 == s2 || s1Name.equals(s2.getOperator().getOrganizationName()+"-"+s2.getStationName())) continue;
 							
 //							System.out.println("Comparing: "+s1Name+" vs. "+s2.getStationName());
 							//Compare transmit coverage against interference coverage.
 							if(PointInPolygon.isPolygonIntersection(s1.getTransmissionCoverage().getCoveragePoints(), s2.getInterferenceCoverage().getCoveragePoints())){
 								overlaps.put(s2.getOperator().getOrganizationName()+"-"+s2.getStationName(), s2);
 //								System.out.println("Added "+s2.getStationName()+" to "+s1Name);
 							}
 							
 						}
 
 					
 						for(ActiveStation as1 : data.getActiveStations()){
 							if(as == as1) continue;
 							if(as1.getStations() != null){
 								for(AISFixedStationData s2 : as1.getStations()){
 									if(s1 == s2) continue;
 									
 //									System.out.println("Comparing: "+s1Name+" vs. "+s2.getStationName());
 									//Compare transmit coverage against interference coverage.
 									if(PointInPolygon.isPolygonIntersection(s1.getTransmissionCoverage().getCoveragePoints(), s2.getInterferenceCoverage().getCoveragePoints())){
 										overlaps.put(s2.getOperator().getOrganizationName()+"-"+s2.getStationName(), s2);
 //										System.out.println("Added "+s2.getStationName()+" to "+s1Name);
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
 													if(s1 == o2) continue;
 													
 													//Compare transmit coverage against interference coverage.
 													if(PointInPolygon.isPolygonIntersection(s1.getTransmissionCoverage().getCoveragePoints(), o2.getInterferenceCoverage().getCoveragePoints())){
 														overlaps.put(o2.getOperator().getOrganizationName()+"-"+o2.getStationName(), o2);
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
 								String o1Name = o1.getOperator().getOrganizationName()+"-"+o1.getStationName();
 								stations.put(o1Name, o1);
 								
 								Map<String,AISFixedStationData> overlaps = overlappingStations.get(o1Name);
 								if(overlaps == null) overlaps = new HashMap<String, AISFixedStationData>();
 								for(AISFixedStationData s2 : as.getStations()){
 									if(o1 == s2) continue;
 									
 									//Compare transmit coverage against interference coverage.
 									if(PointInPolygon.isPolygonIntersection(o1.getTransmissionCoverage().getCoveragePoints(), s2.getInterferenceCoverage().getCoveragePoints())){
 										overlaps.put(s2.getOperator().getOrganizationName()+"-"+s2.getStationName(), s2);
 									}
 									
 								}
 								
 								overlappingStations.put(o1Name, overlaps);
 							
 								
 								
 								for(OtherUserStations other2 : data.getOtherUsersStations()){
 									if(other == other2) continue;
 									if(other2.getStations() != null){
 										for(ActiveStation acs : other.getStations()){
 											if(acs.getStations() != null){
 												for(AISFixedStationData o2 : acs.getStations()){
 													if(o1 == o2) continue;
 														
 													//Compare transmit coverage against interference coverage.
 													if(PointInPolygon.isPolygonIntersection(o1.getTransmissionCoverage().getCoveragePoints(), o2.getInterferenceCoverage().getCoveragePoints())){
 														overlaps.put(o2.getOperator().getOrganizationName()+"-"+o2.getStationName(), o2);
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
 	public AISSlotMap slotMapAtPoint(double lat, double lon){
 		if(this.data == null){
 			
 			this.data = DBHandler.getData(); 
 		}
 		
 		double[] point = {lat, lon};
 		
 		EAVDAMData transmission = getStationsAtPoint(data, point, HealthCheckHandler.TRANSMISSION_COVERAGE);
 		EAVDAMData interference = getStationsAtPoint(data, point, HealthCheckHandler.INTERFERENCE_COVERAGE);
 		
 		if(transmission == null) return null;
 
 		Map<String,List<AISFixedStationData>> reservationsA = new HashMap<String, List<AISFixedStationData>>();
 		Map<String,List<AISFixedStationData>> reservationsB = new HashMap<String, List<AISFixedStationData>>();
 		
 		Set<String> handledStations = new HashSet<String>();
 		
 		//Check all active stations
 		if(transmission.getActiveStations() != null){
 			if(transmission.getActiveStations() != null)
 			for(ActiveStation as : transmission.getActiveStations()){
 				if(as.getStations() != null){
 					
 					for(AISFixedStationData s : as.getStations()){
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
 		
 		if(transmission.getOtherUsersStations() != null){
 			for(OtherUserStations other : transmission.getOtherUsersStations()){
 				if(other.getStations() != null){
 					for(ActiveStation as : other.getStations()){
 						if(as.getStations() != null){
 							for(AISFixedStationData s : as.getStations()){
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
 		if(interference.getActiveStations() != null){
 			for(ActiveStation as : interference.getActiveStations()){
 				if(as.getStations() != null){
 					for(AISFixedStationData s : as.getStations()){
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
 		
 		if(interference.getOtherUsersStations() != null){
 			for(OtherUserStations other : interference.getOtherUsersStations()){
 				if(other.getStations() != null){
 					for(ActiveStation as : other.getStations()){
 						if(as.getStations() != null){
 							for(AISFixedStationData s : as.getStations()){
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
 		slotmap.setBandwidthReservation((1.0*notFreeA+notFreeB)/(numberOfSlotsPerFrequency*numberOfFrequencies));
 		
 		this.trimIssueList(issues);
 		area.setIssues(issues);
 
 		if(slotmap.getBandwidthReservationA() > 0.5){
 			AISDatalinkCheckIssue issue = new AISDatalinkCheckIssue(-1,AISDatalinkCheckRule.RULE7,AISDatalinkCheckSeverity.MAJOR,null,null);
 			issues.add(issue);
 		}
 		
 		area.setSlotmap(slotmap);
 		if(this.areaIssueMap == null) this.areaIssueMap = new HashMap<String, AISDatalinkCheckArea>();
 		
 		this.areaIssueMap.put(lat+";"+lon, area);
 		
 		return slotmap;
 	}
 	
 	private void trimIssueList(List<AISDatalinkCheckIssue> issues){
 		Map<String, List<AISDatalinkCheckIssue>> rules = new HashMap<String, List<AISDatalinkCheckIssue>>();
 		for(AISDatalinkCheckIssue i : issues){
 			List<AISDatalinkCheckIssue> ri = rules.get(i.getRuleViolated().toString());
 			if(ri == null) ri = new ArrayList<AISDatalinkCheckIssue>();
 			
 			ri.add(i);
 			
 			rules.put(i.getRuleViolated().toString(), ri);
 		}
 		
 		List<AISDatalinkCheckIssue> r = new ArrayList<AISDatalinkCheckIssue>();
 		for(String rule : rules.keySet()){
 			Map<String, AISStation> stations = new HashMap<String, AISStation>();
 			List<AISTimeslot> timeslots = new ArrayList<AISTimeslot>();
 			AISDatalinkCheckRule rl = null;
 			AISDatalinkCheckSeverity sev = null;
 			for(AISDatalinkCheckIssue i : rules.get(rule)){
 				rl = i.getRuleViolated();
 				sev = i.getSeverity();
 
 				if(i == null || i.getInvolvedStations() == null) continue;
 				
 				for(AISStation s : i.getInvolvedStations()){
 					stations.put(s.getOrganizationName()+"-"+s.getStationName(), s);
 				}
 				
 				for(AISTimeslot t : i.getInvolvedTimeslots()){
 					timeslots.add(t);
 				}
 			}
 			
 			List<AISStation> st = new ArrayList<AISStation>();
 			for(String s : stations.keySet()){
 				st.add(stations.get(s));
 			}
 			
 			AISDatalinkCheckIssue newIssue = new AISDatalinkCheckIssue(-1, rl, sev, st, timeslots);
 			r.add(newIssue);
 		}
 		
 		issues = r;
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
 		
 		if(issues == null) issues = new ArrayList<AISDatalinkCheckIssue>();
 		
 		if(reservations.get(slot+"") == null || reservations.get(slot+"").size() <= 0){
 			if(interference.get(slot+"") != null){ //First case: No reservation but there is an interference.
 				List<AISStation> infs = new ArrayList<AISStation>();
 				for(AISFixedStationData station : interference.get(slot+"")){
 					if(station.getStationType().equals(AISFixedStationType.ATON)) continue; //AtoNs do not interfere?
 					
 					if(station.getOwnershipInSlot(channel, new Integer(slot)).equals("L")){ //Interference is for Local Base station
 						AISStation s = new AISStation(station.getOperator().getOrganizationName(), station.getStationName(), station.getLat(), station.getLon());
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
					String o = station.getOwnershipInSlot("A", new Integer(slot)); //Check if it is a local or remote.
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
 					
 					AISDatalinkCheckIssue issue = new AISDatalinkCheckIssue(-1,AISDatalinkCheckRule.RULE1,AISDatalinkCheckSeverity.MAJOR,ps,slots);
 					
 					issues.add(issue);
 					addRule1 = true;
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
 					
 					AISDatalinkCheckIssue issue = new AISDatalinkCheckIssue(-1,AISDatalinkCheckRule.RULE1,AISDatalinkCheckSeverity.SEVERE,ps,slots);
 									
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
 				
 					issue = new AISDatalinkCheckIssue(-1,AISDatalinkCheckRule.RULE3, AISDatalinkCheckSeverity.MAJOR,atonStations,slots);
 					
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
 	
 	public List<AISDatalinkCheckIssue> checkRulesAtTimeslot(AISTimeslot timeslot){
 		List<AISDatalinkCheckIssue> issues = new ArrayList<AISDatalinkCheckIssue>();
 		
 		List<AISStation> interfered = timeslot.getInterferedBy();
 		
 		
 		
 		return issues;
 	}
 	
 	
 	/**
 	 * 
 	 * Gets the stations and checks the bandwith of the given point. The point is the top left corner of the area.
 	 * 
 	 * @param lat TOP LEFT Lat point of the area
 	 * @param lon TOP LEFT Lon point of the area
 	 * @return
 	 */
 	public AISDatalinkCheckResult checkRulesAtPoint(double lat, double lon, double resolution){
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
 		
 		
 		
 		AISDatalinkCheckArea area = new AISDatalinkCheckArea(lat, lon, getIncrementedLatitude(resolution, lat), getIncrementedLongitude(resolution, lon), percentage);
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
 		
 		
 		if(data.getActiveStations() != null){
 			List<ActiveStation> activeStations = new ArrayList<ActiveStation>();
 			for(ActiveStation as : data.getActiveStations()){
 				List<AISFixedStationData> stations = new ArrayList<AISFixedStationData>();
 
 				for(AISFixedStationData s : as.getStations()){
 //					System.out.println("Checking: "+s.getLat()+";"+s.getLon());
 //					if(s.getLat() == 60 && s.getLon() == 24) System.out.println("\t"+s.getInterferenceCoverage().getCoveragePoints()+" | "+s.getTransmissionCoverage().getCoveragePoints());
 					if(coverageType == INTERFERENCE_COVERAGE && PointInPolygon.isPointInPolygon(s.getInterferenceCoverage().getCoveragePoints(), point)){
 //						System.out.println("ADDED: (I)"+s.getLat()+";"+s.getLon());
 						stations.add(s);
 					}else if(coverageType == TRANSMISSION_COVERAGE && PointInPolygon.isPointInPolygon(s.getTransmissionCoverage().getCoveragePoints(), point)){
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
 						if(coverageType == INTERFERENCE_COVERAGE && PointInPolygon.isPointInPolygon(as.getProposals().get(u).getInterferenceCoverage().getCoveragePoints(), point)){
 							Map<EAVDAMUser, AISFixedStationData> proposals = new HashMap<EAVDAMUser, AISFixedStationData>();
 							proposals.put(u, as.getProposals().get(u));
 							a.setProposals(proposals);
 							addedProposal = true;
 						}else if(coverageType == TRANSMISSION_COVERAGE && PointInPolygon.isPointInPolygon(as.getProposals().get(u).getTransmissionCoverage().getCoveragePoints(), point)){
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
 						if(coverageType == INTERFERENCE_COVERAGE && PointInPolygon.isPointInPolygon(s.getInterferenceCoverage().getCoveragePoints(), point)){
 //							System.out.println("ADDED OTHER (I): "+s.getLat()+";"+s.getLon());
 							stations.add(s);
 						}else if(coverageType == TRANSMISSION_COVERAGE && PointInPolygon.isPointInPolygon(s.getTransmissionCoverage().getCoveragePoints(), point)){
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
 					if(coverageType == INTERFERENCE_COVERAGE && PointInPolygon.isPointInPolygon(s.getInterferenceCoverage().getCoveragePoints(), point)){
 						stations.add(s);
 					}else if(coverageType == TRANSMISSION_COVERAGE && PointInPolygon.isPointInPolygon(s.getTransmissionCoverage().getCoveragePoints(), point)){
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
 	
 	private double[] getLatitudeBorders(){
 		if(this.data == null) return null;
 		
 		double maxLatitude = Double.MIN_VALUE;
 		double minLatitude = Double.MAX_VALUE;
 		
 		
 		return null;
 	}
 	
 	private static double getIncrementedLongitude(double resolution, double startLon){
 // 		 System.out.println(startLon+" | "+resolution+"/(60/"+Math.cos(startLon)+") + "+startLon+" = "+(1.0*resolution/(60.0/Math.cos(startLon)) + startLon));
 		 double dres = 1.0*resolution/(60.0/Math.cos(startLon));
 		 
 		 return startLon + dres;
 	}
 	
 	private static double getLatituteChangeForResolution(double resolution, double lat){
 		return 1.0*resolution/(60.0/Math.cos(lat));
 	}
 	
 	private static double getLongitudeChangeForResolution(double resolution, double lon){
 		return 1.0*resolution/(60.0/Math.cos(lon));
 	}
 	
 	
 	private static double getIncrementedLatitude(double resolution, double startLat){
 //		System.out.println(startLat+" | "+resolution+"/(60/"+Math.cos(startLat)+") + "+startLat+" = "+(1.0*resolution/(60.0/Math.cos(startLat)) + startLat));
 		return 1.0*resolution/(60.0/Math.cos(startLat)) + startLat;
 	}
 	
 	public static void isPolygonIntersection(){
 		
 	}
 }
