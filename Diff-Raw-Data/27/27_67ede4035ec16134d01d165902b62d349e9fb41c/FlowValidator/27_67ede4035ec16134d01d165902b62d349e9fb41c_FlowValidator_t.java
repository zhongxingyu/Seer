 package de.hub.clickwatch.apps.god.validation;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import de.hub.clickwatch.apps.god.Server;
 import de.hub.clickwatch.apps.god.information.ClientInformations;
 import de.hub.clickwatch.apps.god.information.FlowRoute;
 import de.hub.clickwatch.apps.god.information.FlowRouteChildren;
 import de.hub.clickwatch.apps.god.information.FlowStatInformation;
 import de.hub.clickwatch.apps.god.node.FlowStatProcessor;
 import de.hub.clickwatch.apps.god.routing.GlobalLinktable;
 import de.hub.clickwatch.apps.god.routing.GlobalRoutingtable;
 
 public class FlowValidator implements Validator {
 	private Map<String, String[]> validatingFlows = new HashMap<String, String[]>();
 	private Map<String, String> validationDiff= new HashMap<String, String>();
 	private List<String> allParticipatingAPs = new ArrayList<String>();
 	
 	private boolean routeIsAcceptable(String from, String to) {
 		return GlobalRoutingtable.getShortestLength(from, to) != -1;
 	}
 	
 	@Override
 	public void init() {
 		String[][] aps = new String[][] {
 				/*{"06-0F-B5-3F-22-E9", "192.168.3.34"},
 				{"06-0F-B5-3F-21-3C", "192.168.3.24"},
 				{"06-0F-B5-97-33-6C", "192.168.3.31"},
 				{"06-0F-B5-97-34-BC", "192.168.3.73"},
 				{"06-0F-B5-3F-58-49", "192.168.3.45"},
 				{"06-0B-6B-09-ED-73", "192.168.3.110"},
 				{"06-0B-6B-09-F2-94", "192.168.3.111"},
 				{"06-0C-42-0C-74-0D", "192.168.3.112"}
 				*/
 				
 				{"06-0B-6B-09-ED-73", "192.168.3.110"},
 				{"06-0B-6B-09-F2-94", "192.168.3.111"},
 				{"06-0C-42-0C-74-0D", "192.168.3.112"},
 				{"06-0F-B5-3F-42-62", "192.168.3.23"},
 				{"06-0F-B5-3F-21-3C", "192.168.3.24"},
 				{"06-0F-B5-0B-95-29", "192.168.3.25"},
 				{"06-0F-B5-97-33-1D", "192.168.3.28"},
 				{"06-0F-B5-97-36-9A", "192.168.3.29"},
 				{"06-0F-B5-97-33-6C", "192.168.3.31"},
 				{"06-0F-B5-3F-45-57", "192.168.3.32"},
 				{"06-0F-B5-3F-1E-C7", "192.168.3.33"},
 				{"06-0F-B5-3F-22-E9", "192.168.3.34"},
 				{"06-0F-B5-3F-22-EC", "192.168.3.35"},
 				{"06-0F-B5-97-33-D2", "192.168.3.37"},
 				{"06-0F-B5-97-25-7B", "192.168.3.38"},
 				{"06-0F-B5-97-38-5A", "192.168.3.39"},
 				{"06-0F-B5-97-37-FC", "192.168.3.40"},
 				{"06-0F-B5-3F-20-D6", "192.168.3.42"},
 				{"06-0F-B5-3F-21-81", "192.168.3.43"},
 				{"06-0F-B5-3F-56-B1", "192.168.3.44"},
 				{"06-0F-B5-3F-58-49", "192.168.3.45"},
 				{"06-0F-B5-97-36-77", "192.168.3.46"},
 				{"06-0F-B5-97-36-83", "192.168.3.47"},
 				{"06-0F-B5-3F-1F-1C", "192.168.3.49"},
 				{"06-0F-B5-3F-45-72", "192.168.3.51"},
 				{"06-0F-B5-97-34-E9", "192.168.3.52"},
 				{"06-0F-B5-97-36-D9", "192.168.3.53"},
 				{"06-0F-B5-97-35-86", "192.168.3.54"},
 				{"06-0F-B5-97-35-8C", "192.168.3.55"},
 				{"06-0F-B5-3F-1D-3B", "192.168.3.61"},
 				{"06-0F-B5-97-37-5C", "192.168.3.63"},
 				{"06-0F-B5-0D-AE-34", "192.168.3.70"},
 				{"06-0F-B5-97-36-7D", "192.168.3.72"},
 				{"06-0F-B5-97-34-BC", "192.168.3.73"},
 				{"06-0F-B5-97-37-37", "192.168.3.74"},
 				{"06-0F-B5-97-36-D8", "192.168.3.77"},
 				{"06-0F-B5-97-36-54", "192.168.3.78"},
 				{"06-0F-B5-97-35-E1", "192.168.3.80"},
 				{"06-0F-B5-97-25-42", "192.168.3.82"},
 				{"06-0F-B5-97-25-82", "192.168.3.201"},
 				{"00-1B-B1-05-3B-75", "192.168.3.162"},
 				{"00-1B-B1-05-3B-5D", "192.168.3.151"}
 		};
 		
 		this.validatingFlows = new HashMap<String, String[]>();
 		this.validationDiff = new HashMap<String, String>();
 		this.allParticipatingAPs = new ArrayList<String>();
 		Map<String, String> apResetList = new HashMap<String, String>();
 		for (String[] ap : aps) {
 			allParticipatingAPs.add(ap[1]);
 			apResetList.put(ap[0], ap[1]);
 			
 			for (String[] targetAp : aps) {
 				if (!targetAp[0].equals(ap[0])) {
 					validatingFlows.put(ap[0] + " -> " + targetAp[0], new String[] {ap[0], targetAp[0], ap[1], targetAp[1]});
 				}
 			}
 		}
 	}
 
 	@Override
 	public void startValidation() {
 		int flowNum = 1;
 		for (String flow : validatingFlows.keySet()) {
			System.out.print((flowNum++) + ".flow (of " + validatingFlows.size() + "):\t" + flow + " -->");
			/*
 			for (String participant : allParticipatingAPs) {
 				Server.getInstance().handleSetter(participant, 7777, "lt", "fix_linktable", "false");
 			}
			*/
 			try {
 				String[] data = validatingFlows.get(flow);
 				int routeCheckCounter = 0;
 				boolean routeIsUsable = false;
 				while (routeCheckCounter < 20) {
 					routeCheckCounter++;
 					if (routeIsAcceptable(data[0], data[1])) {
 						routeIsUsable = true;
 						break;
 					}
 					Thread.sleep(600);
 				}
 					
 				if (!routeIsUsable) {
 					System.out.println(" not taken");
 					continue;
 				}
 				
 				//1. reset all participant's flowstats and fix their linktables
 				System.out.print("\tresetting, ");
 				for (String participant : allParticipatingAPs) {
					//Server.getInstance().handleSetter(participant, 7777, "lt", "fix_linktable", "true");
 					Server.getInstance().handleSetter(participant, 7777, "routing/dsr_stats", "reset", "");
 				}
 				Thread.sleep(500);
 				
 				//2. add flow
 				System.out.print("adding flow, ");
 				Server.getInstance().handleSetter(data[2], 7777, "sf", "add_flow", data[0] + " " + data[1] + " 300 100 0 100 true");
 				Thread.sleep(8000);
 				
 				//3. stop flow
 				System.out.print("stopping flow, ");
 				Server.getInstance().handleSetter(data[2], 7777, "sf", "add_flow", data[0] + " " + data[1] + " 300 100 0 100 false");
 				Thread.sleep(2000);
 				
 				//4. read stats about the flow
 				System.out.print("processing results ... ");
 				boolean gotInfos = false;
 				int tries = 0;
 				while (!gotInfos && (tries < 50)) {
 					tries++;
 					HashMap<String, ClientInformations> apInfos = Server.getInstance().getStorageManager().getClientInformations(data[0]);
 					if (apInfos.containsKey(FlowStatProcessor.class.getName())) {
 						FlowStatInformation fs = (FlowStatInformation)apInfos.get(FlowStatProcessor.class.getName());
 						for (FlowRoute route : fs.getFlowRoutes()) {
 							if ((route.getSrc().equals(data[0])) && (route.getDst().equals(data[1]))) {
 								gotInfos = true;
 								
 								//found statistics, collect them
 								String stats = "real," + data[0] + "," + data[1] + ",";
 								boolean noControlProblem = false;
 								
 								for (FlowRouteChildren child : route.getChildren()) {
 									StringTokenizer tok = new StringTokenizer(child.getHops(), ",");
 									String hop = ""; 
 									String nextHop = tok.nextToken();
 									int realMetric = 0;
 									while (tok.hasMoreTokens()) {
 										hop = nextHop;
 										nextHop = tok.nextToken();
 										if (GlobalLinktable.getLinkInfos(hop, nextHop) != null) {
 											realMetric += GlobalLinktable.getLinkInfos(hop, nextHop).getMetric();
 										} else {
 											System.err.println("\n\nhave the 'no control over node' problem");
 											noControlProblem = true;
 											break;
 										}
 									}
 									stats += realMetric + "," + child.getHop_count() + "," + child.getHops();
 								}
 								
 								stats += "\ngod," + data[0] + "," + data[1] + "," +
 										GlobalRoutingtable.getShortestLength(data[0], data[1]) + "," +
 										GlobalRoutingtable.getShortestHopCount(data[0], data[1]) + "," +
 										GlobalRoutingtable.getShortestPath(data[0], data[1]);
 								
 								if (!noControlProblem) {
 									System.out.print("[added], ");
 									validationDiff.put(flow, stats);
 								} else {
 									System.out.print("[NOT ADDED], ");
 								}
 								
 								break;
 							}
 						}
						
						if (!validationDiff.containsKey(flow)) {
							System.out.print("[no flow], ");
							String stats = "real," + data[0] + "," + data[1] + ",NOT_FOUND";
							stats += "\ngod," + data[0] + "," + data[1] + "," +
									GlobalRoutingtable.getShortestLength(data[0], data[1]) + "," +
									GlobalRoutingtable.getShortestHopCount(data[0], data[1]) + "," +
									GlobalRoutingtable.getShortestPath(data[0], data[1]);
							validationDiff.put(flow, stats);
						}
						
 					}
 					Thread.sleep(250);
 				}
 				
 				//5. reactivate linktable of all participants
 				System.out.print("finishing flow run ... ");
				/*
 				for (String participant : allParticipatingAPs) {
 					Server.getInstance().handleSetter(participant, 7777, "lt", "fix_linktable", "false");
 				}
				*/
				Thread.sleep(2000);
 				System.out.println("done");
 			} catch (InterruptedException int_exc) {
 				//nothing to do
 			}
 		}
 		
 		FileWriter fw = null;
 		try {
 			fw = new FileWriter("resultsFlowRun.csv");
 			for (String k : validationDiff.keySet()) {
 				//System.out.println(validationDiff.get(k));
 				fw.write(validationDiff.get(k) + "\n");
 			}
 			fw.close();
 		} catch (Exception exc) {
 			//nothing
 		} finally { 
 			if (fw != null) {
 				try {
 					fw.close();
 				} catch (IOException e) {
 					//nothing
 				}
 			}
 		}	
 	}
 
 }
