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
 import dk.frv.eavdam.data.AISFixedStationData;
 import dk.frv.eavdam.data.AISFixedStationType;
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
 
 public class HealthCheckHandler {
 
 	public static final int TRANSMISSION_COVERAGE = 1;
 	public static final int INTERFERENCE_COVERAGE = 2;
 	EAVDAMData data = null;
 	
 	public static String plannedIndicator = " (P)";
 	public static int numberOfFrequencies = 2;
 	public static int numberOfSlotsPerFrequency = 2250;
 	
 	public HealthCheckHandler(EAVDAMData data){
 		this.data = data;
 	}
 	
 	public AISDatalinkCheckResult startAISDatalinkCheck(AISDatalinkCheckListener listener, boolean checkRule1, boolean checkRule2, boolean checkRule3, boolean checkRule4, boolean checkRule5, boolean checkRule6, boolean checkRule7, double topLeftLatitude, double topLeftLongitude, double lowerRightLatitude, double lowerRightLongitude, double resolution){
 		
 		
 		
 		return null;
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
 		
 		List<AISDatalinkCheckIssue> issues = new ArrayList<AISDatalinkCheckIssue>();
 		
 		AISDatalinkCheckResult result = new AISDatalinkCheckResult();
 		
 		if(transmission == null) return null;
 
 		Map<String,List<AISFixedStationData>> reservationsA = new HashMap<String, List<AISFixedStationData>>();
 		Map<String,List<AISFixedStationData>> reservationsB = new HashMap<String, List<AISFixedStationData>>();
 		
 		Set<String> handledStations = new HashSet<String>();
 		
 		//Check all active stations
 		if(transmission.getActiveStations() != null){
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
 
 		//TODO Is there a need to check proposals also? What to do to the planned stations?
 		
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
 		
 		List<AISTimeslot> slotA = new ArrayList<AISTimeslot>();
 		List<AISTimeslot> slotB = new ArrayList<AISTimeslot>();
 		int notFreeA = 0, notFreeB = 0;
 		for(int i = 0; i < numberOfSlotsPerFrequency; ++i){
 			AISTimeslot a = this.createAISTimeslot(reservationsA, interferenceA, i, "A");
 			AISTimeslot b = this.createAISTimeslot(reservationsB, interferenceB, i, "B");
 			
 			if(!a.getFree().booleanValue()) ++notFreeA;
 			if(!b.getFree().booleanValue()) ++notFreeB;
 			
 			slotA.add(a);
 			slotB.add(b);
 		}
 		
 		slotmap.setAIS1Timeslots(slotA);
 		slotmap.setAIS2Timeslots(slotB);
 		
 		slotmap.setBandwidthReservationA(1.0*notFreeA/numberOfSlotsPerFrequency);
 		slotmap.setBandwidthReservationB(1.0*notFreeB/numberOfSlotsPerFrequency);
 		
 		return slotmap;
 	}
 	
 	/**
 	 * Creates the timeslot for the given slot.
 	 * @param reservations List of stations with reservations.
 	 * @param interference List of interfering stations.
 	 * @param slot Number of the slot in question
 	 * @param channel Either A or B
 	 * @return Timeslot.
 	 */
 	private AISTimeslot createAISTimeslot(Map<String,List<AISFixedStationData>> reservations, Map<String,List<AISFixedStationData>> interference, int slot, String channel){
 		AISTimeslot a = new AISTimeslot();
 		a.setSlotNumber(slot);
 		
 		if(reservations.get(slot+"") == null || reservations.get(slot+"").size() <= 0){
 			if(interference.get(slot+"") != null){ //First case: No reservation but there is an interference.
 				List<AISStation> infs = new ArrayList<AISStation>();
 				for(AISFixedStationData station : interference.get(slot+"")){
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
 			
 		
 			if(interference.get(slot+"") != null){ //Check the interferences
 					
 				List<AISStation> infs = new ArrayList<AISStation>();
 				
 				Set<String> infNames = new HashSet<String>();
 				for(AISFixedStationData station : interference.get(slot+"")){
 					String planned = "";
 					if(station.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED) planned = plannedIndicator;
 					
 					AISStation s = new AISStation(station.getOperator().getOrganizationName(), station.getStationName()+planned, station.getLat(), station.getLon());
 						
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
 				}
 				
 			}else if(used.size() > 1){ //Several stations use the slot
 				
 				a.setPossibleConflicts(new Boolean(true));
 			}else{
 				a.setPossibleConflicts(new Boolean(false));
 			}
 			
 
 			
 			a.setReservedBy(res);
 			a.setUsedBy(used);
 			a.setFree(new Boolean(false));
 			
 			if(atons != expectedAtoNs){
 				if(atons > expectedAtoNs){
 					//AtoN usage with no reservation!
 					System.out.println("AtoNs and expected AtoN does not match! AtoN usage without reservation!");
 						
 				}else{
 					//Reservation but no usage!
 					System.out.println("AtoNs and expected AtoN does not match! Reservation without AtoN usage!");
 						
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
 	
 	
 	private static double getIncrementedLongitude(double resolution, double startLon){
 // 		 System.out.println(startLon+" | "+resolution+"/(60/"+Math.cos(startLon)+") + "+startLon+" = "+(1.0*resolution/(60.0/Math.cos(startLon)) + startLon));
 		 double dres = 1.0*resolution/(60.0/Math.cos(startLon));
 		 
 		 return startLon + dres;
 	}
 	
 	private static double getIncrementedLatitude(double resolution, double startLat){
 //		System.out.println(startLat+" | "+resolution+"/(60/"+Math.cos(startLat)+") + "+startLat+" = "+(1.0*resolution/(60.0/Math.cos(startLat)) + startLat));
 		return 1.0*resolution/(60.0/Math.cos(startLat)) + startLat;
 	}
 	
 	public static void isPolygonIntersection(){
 		
 	}
 }
