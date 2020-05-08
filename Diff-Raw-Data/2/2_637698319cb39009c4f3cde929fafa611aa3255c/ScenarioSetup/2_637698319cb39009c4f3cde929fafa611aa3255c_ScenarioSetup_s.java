 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator
  * Copyright (C) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * This program and the accompanying materials are dual-licensed under either
  * the terms of the Eclipse Public License v1.0 as published by the Eclipse
  * Foundation
  *  
  *   or (per the licensee's choosing)
  *  
  * under the terms of the GNU General Public License version 2 as published
  * by the Free Software Foundation.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.scenario;
 
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.rmi.RemoteException;
 import java.util.Enumeration;
 import java.util.UUID;
 
 import de.tuilmenau.ics.fog.routing.naming.HierarchicalNameMappingService;
 import de.tuilmenau.ics.fog.routing.naming.NameMappingService;
 import de.tuilmenau.ics.fog.topology.Simulation;
 
 
 /**
  * Class provides functions setting up a scenario containing nodes and links.
  */
 public class ScenarioSetup
 {
 	private static final String DEFAULT_AS_NAME = "default";
 	
 	
 	public static boolean selectScenario(String scenarioName, String options, Simulation sim)
 	{
 		if(sim != null) {
 			try {
 				int scenario_number = Integer.parseInt(scenarioName);
 				
 				// creating a row of connected nodes
 				if((scenario_number > 100) && (scenario_number < 200)) {
 					scenarioRow(sim, DEFAULT_AS_NAME, scenario_number -100);
 					return true;
 				}
 				
 				// creating a ring of connected nodes
 				if((scenario_number > 200) && (scenario_number < 300)) {
 					scenarioRing(sim, DEFAULT_AS_NAME, scenario_number -200);
 					return true;
 				}
 				
 				switch(scenario_number)
 				{
 					// default scenarios available even without scripts
 					case 1: scenario1(sim); break;
 					case 2: scenario2(sim); break;
 					case 3: scenario3(sim); break;
 
 					// scenarios requiring access to code instead of commands
 					case 6: rereoutetestdist(sim); break;
 					case 43: scenario43(sim); break;
 					
 					// a bus with 3 connected nodes
 					case 33: scenario33(sim); break;
 
 					// a bus with 4 connected nodes
 					case 34: scenario34(sim); break;
 
 					// a bus with 5 connected nodes
 					case 35: scenario35(sim); break;
 
 					case 88: scenario88(sim); break;
 					case 89: scenario89(sim); break;
 					case 90: scenario90(sim); break;
 					case 91: scenario91(sim); break;
 					
 					// emulator scenario
 					case 99: emulator(sim); break;
 				}
 				
 				return true;
 			}
 			catch(Exception exc) {
 				sim.getLogger().err(sim, "Exception during scenario setup.", exc);
 			}
 		}
 		
 		return false;
 	}
 
 	
 	public static void scenarioRow(Simulation sim, String pAsName, int numberNodes)
 	{
 		scenarioRow(sim, pAsName, "bus", 1, numberNodes, 0);
 	}
 	
 	public static void scenarioRow(Simulation sim, String pAsName, int numberNodes, long pDataRate)
 	{
 		scenarioRow(sim, pAsName, "bus", 1, numberNodes, pDataRate);
 	}
 	
 	public static void scenarioRow(Simulation sim, String pAsName, String pBusDescriptor, int pStartNode, int numberNodes, long pDataRate)
 	{
 		if(pAsName != null){
 			sim.executeCommand("@ - create as " + pAsName);
 			sim.executeCommand("switch " + pAsName);
 		}
 
 		for(int i=pStartNode; i<=(pStartNode + numberNodes - 1); i++) {
 			String nodeName = "node" +i;
 			sim.executeCommand("create node " +nodeName);
 			NameMappingService tNMS = HierarchicalNameMappingService.getGlobalNameMappingService(sim);
 			try {
 				tNMS.setNodeASName(nodeName, pAsName);
 			} catch (RemoteException tExc) {
 				tExc.printStackTrace();
 			}
 			
 			// do not create bus for the last one
 			if(i < (pStartNode + numberNodes - 1)) {
 				String busName = pBusDescriptor + i +"_" +(i+1);
 				if(pDataRate > 0)
 					sim.executeCommand("create bus " +busName + " " + Long.toString(pDataRate));
 				else
 					sim.executeCommand("create bus " +busName);
 				sim.executeCommand("connect " + nodeName + " " + busName);
 			}
 			
 			if(i > 1) {
 				sim.executeCommand("connect " + nodeName + " " + pBusDescriptor + (i-1) + "_" + i);
 			}
 		}
 	}
 	
 	public static void scenario88(Simulation pSim) // Thomas for testing/evaluating HRM
 	{
 		scenario88(pSim, DEFAULT_AS_NAME);
 	}
 	
 	public static void scenario88(Simulation pSim, String pAsName) // Thomas for testing/evaluating HRM
 	{
 		// create bus [name] [data rate in kbit/s] [delay in ms] [packet loss in %]
 		
 		int tNumberOfNodes = 12;
 		long tDataRate = 100 * 1000;
 		
		scenarioRow(pSim, pAsName, tNumberOfNodes, tDataRate);
 		
 		// close row to a ring by connecting last and first node
 		String busName = "link_" +tNumberOfNodes +"_1";
 		if(tDataRate > 0)
 			pSim.executeCommand("create bus " +busName  + " " + Long.toString(tDataRate));
 		else
 			pSim.executeCommand("create bus " +busName);
 		
 		pSim.executeCommand("connect node" +tNumberOfNodes +" " +busName);
 		pSim.executeCommand("connect node1 " +busName);
 	}
 
 	public static void scenario89(Simulation pSim) // Thomas for testing/evaluating HRM
 	{
 		// create bus [name] [data rate in kbit/s] [delay in ms] [packet loss in %]
 		
 		int tNumberOfNodes = 3;
 		long tDataRate = 100 * 1000;
 	
 		scenario88(pSim, DEFAULT_AS_NAME);
 		
 		scenarioRow(pSim, DEFAULT_AS_NAME, "link_", 13, tNumberOfNodes, tDataRate);
 		
 		// close row to a ring by connecting last and first node
 		String tRingEndBusName = "link_13_" + (12 + tNumberOfNodes);
 		if(tDataRate > 0)
 			pSim.executeCommand("create bus " +tRingEndBusName  + " " + Long.toString(tDataRate));
 		else
 			pSim.executeCommand("create bus " +tRingEndBusName);
 		
 		pSim.executeCommand("connect node" + (12 + tNumberOfNodes) + " " + tRingEndBusName);
 		pSim.executeCommand("connect node13 " +tRingEndBusName);
 
 		// connect both networks
 		String tInterNetworkBusName = "link_7_13";
 		if(tDataRate > 0)
 			pSim.executeCommand("create bus " +tInterNetworkBusName  + " " + Long.toString(tDataRate));
 		else
 			pSim.executeCommand("create bus " +tInterNetworkBusName);
 		
 		pSim.executeCommand("connect node7 " +tInterNetworkBusName);
 		pSim.executeCommand("connect node13 " +tInterNetworkBusName);
 	}
 
 	public static void scenario90(Simulation pSim) // Thomas for testing/evaluating HRM
 	{
 		// create bus [name] [data rate in kbit/s] [delay in ms] [packet loss in %]
 		
 		int tNumberOfNodes = 4;
 		long tDataRate = 100 * 1000;
 	
 		// ####### first AS
 
 		scenarioRow(pSim, "firstNet", tNumberOfNodes, tDataRate);
 		
 		// close row to a ring by connecting last and first node
 		String busName = "link_" +tNumberOfNodes +"_1";
 		if(tDataRate > 0)
 			pSim.executeCommand("create bus " +busName  + " " + Long.toString(tDataRate));
 		else
 			pSim.executeCommand("create bus " +busName);
 		
 		pSim.executeCommand("connect node" +tNumberOfNodes +" " +busName);
 		pSim.executeCommand("connect node1 " +busName);
 
 				
 		// connect both networks - part 1
 		String tInterNetworkBusName = "link_" + tNumberOfNodes + "_" + (tNumberOfNodes + 1);
 		if(tDataRate > 0)
 			pSim.executeCommand("create bus " +tInterNetworkBusName  + " " + Long.toString(tDataRate));
 		else
 			pSim.executeCommand("create bus " +tInterNetworkBusName);
 		
 		pSim.executeCommand("connect node" + tNumberOfNodes + " " +tInterNetworkBusName);
 
 		// ####### second AS
 		
 		// create another line of nodes
 		scenarioRow(pSim, "secondNet", "link_", tNumberOfNodes + 1, tNumberOfNodes, tDataRate);
 		
 		// close row to a ring by connecting last and first node
 		String tRingEndBusName = "link_" + tNumberOfNodes + "_" + (2 * tNumberOfNodes);
 		if(tDataRate > 0)
 			pSim.executeCommand("create bus " +tRingEndBusName  + " " + Long.toString(tDataRate));
 		else
 			pSim.executeCommand("create bus " +tRingEndBusName);
 		
 		pSim.executeCommand("connect node" + (2 * tNumberOfNodes) + " " + tRingEndBusName);
 		pSim.executeCommand("connect node" + (tNumberOfNodes + 1) + " " + tRingEndBusName);
 
 		// connect both networks - part 2
 		pSim.executeCommand("connect node" + (tNumberOfNodes + 1) + " " +tInterNetworkBusName);
 	}
 
 	public static void scenario91(Simulation pSim) // Thomas for testing/evaluating HRM
 	{
 		// create bus [name] [data rate in kbit/s] [delay in ms] [packet loss in %]
 		
 		int tNumberOfNodes = 3;
 		long tDataRate = 100 * 1000;
 	
 		// ####### first AS
 
 		scenario88(pSim, "bigNet");
 		
 		// connect both networks - part 1
 		String tInterNetworkBusName = "link_7_13";
 		if(tDataRate > 0)
 			pSim.executeCommand("create bus " +tInterNetworkBusName  + " " + Long.toString(tDataRate));
 		else
 			pSim.executeCommand("create bus " +tInterNetworkBusName);
 		
 		pSim.executeCommand("connect node7 " +tInterNetworkBusName);
 
 		// ####### second AS
 		
 		// create another line of nodes
 		scenarioRow(pSim, "smallNet", "link_", 13, tNumberOfNodes, tDataRate);
 		
 		// close row to a ring by connecting last and first node
 		String tRingEndBusName = "link_13_" + (12 + tNumberOfNodes);
 		if(tDataRate > 0)
 			pSim.executeCommand("create bus " +tRingEndBusName  + " " + Long.toString(tDataRate));
 		else
 			pSim.executeCommand("create bus " +tRingEndBusName);
 		
 		pSim.executeCommand("connect node" + (12 + tNumberOfNodes) + " " + tRingEndBusName);
 		pSim.executeCommand("connect node13 " +tRingEndBusName);
 
 		// connect both networks - part 2
 		pSim.executeCommand("connect node13 " +tInterNetworkBusName);
 	}
 	
 	public static void scenarioRing(Simulation sim, String asName, int numberNodes)
 	{
 		scenarioRow(sim, asName, numberNodes);
 		
 		// close row to a ring by connecting last and first node
 		String busName = "bus" +numberNodes +"_1";
 		sim.executeCommand("create bus " +busName);
 		
 		sim.executeCommand("connect node" +numberNodes +" " +busName);
 		sim.executeCommand("connect node1 " +busName);
 	}
 	
 
 	private static void rereoutetestdist(Simulation pSim)
 	{
 		pSim.executeCommand("@ - create as default");
 //		pSim.executeCommand("@ 131.246.112.7 create as 7");
 //		pSim.executeCommand("@ 131.246.112.9 create as 9");
 		pSim.executeCommand("@ worker1 create as 7");
 		pSim.executeCommand("@ worker2 create as 9");
 		
 		pSim.switchToAS("7");
 		pSim.executeCommand("create node 71");
 		pSim.executeCommand("create node 72");
 		pSim.executeCommand("create node 73");
 		pSim.executeCommand("create node 74");
 		pSim.executeCommand("create bus 71-72");
 		pSim.executeCommand("create bus 72-73");
 		pSim.executeCommand("create bus 73-74");
 		pSim.executeCommand("create bus 74-74");
 		pSim.executeCommand("create bus 74-91");
 		pSim.executeCommand("connect 71 71-72");
 		pSim.executeCommand("connect 72 71-72");
 		pSim.executeCommand("connect 72 72-73");
 		pSim.executeCommand("connect 73 72-73");
 		pSim.executeCommand("connect 73 73-74");
 		pSim.executeCommand("connect 74 73-74");
 		pSim.executeCommand("connect 74 74-91");
 		
 		pSim.switchToAS("9");
 		pSim.executeCommand("create node 91");
 		pSim.executeCommand("create node 92");
 		pSim.executeCommand("create node 93");
 		pSim.executeCommand("create node 94");
 		pSim.executeCommand("create bus 91-92");
 		pSim.executeCommand("create bus 92-93");
 		pSim.executeCommand("create bus 93-94");
 		pSim.executeCommand("create bus 71-94");
 		pSim.executeCommand("connect 91 91-92");
 		pSim.executeCommand("connect 92 91-92");
 		pSim.executeCommand("connect 92 92-93");
 		pSim.executeCommand("connect 93 92-93");
 		pSim.executeCommand("connect 93 93-94");
 		pSim.executeCommand("connect 94 93-94");
 		pSim.executeCommand("connect 94 71-94");
 		pSim.executeCommand("connect 91 74-91");
 		pSim.switchToAS("9");
 		pSim.switchToAS("7");
 		pSim.executeCommand("connect 74 74-91");
 		pSim.executeCommand("connect 71 71-94");
 	
 		try {
 			NameMappingService mNMS = HierarchicalNameMappingService.getGlobalNameMappingService(pSim);
 			mNMS.setNodeASName("71", "7");
 			mNMS.setNodeASName("72", "7");
 			mNMS.setNodeASName("73", "7");
 			mNMS.setNodeASName("74", "7");
 			mNMS.setNodeASName("74", "7");
 			mNMS.setNodeASName("91", "9");
 			mNMS.setNodeASName("92", "9");
 			mNMS.setNodeASName("93", "9");
 			mNMS.setNodeASName("94", "9");
 			mNMS.setNodeASName("defaultnode", "default");
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void emulator(Simulation pSim)
 	{
 		String nodeName = UUID.randomUUID().toString();
 		String asName = nodeName;
 		
 		pSim.executeCommand("@ - create as " +asName);
 		pSim.executeCommand("switch " +asName);
 		pSim.executeCommand("create node " +nodeName);
 		
 		try {
 			Enumeration<NetworkInterface> netInfs = NetworkInterface.getNetworkInterfaces();
 			while(netInfs.hasMoreElements()) {
 				NetworkInterface netInf = netInfs.nextElement();
 				
 				String ethName = netInf.getDisplayName();
 				if(netInf.isUp()) {
 					if(ethName != null) {
 						// no spaces allowed in command
 						ethName = ethName.replace(" ", "_");
 						
 						// create Ethernet element in simulation and link it to node
 						pSim.executeCommand("create ethernet " +ethName +" " +netInf.getName() +" " +netInf.getName());
 						pSim.executeCommand("connect " +nodeName +" " +ethName);
 					} else {
 						pSim.getLogger().err(ScenarioSetup.class, "Can not get display name for " +netInf);
 					}
 				} else {
 					pSim.getLogger().info(ScenarioSetup.class, "Ignoring deactivated network interface '" +ethName +"'");
 				}
 			}
 		}
 		catch(SocketException exc) {
 			pSim.getLogger().err(ScenarioSetup.class, "Can not setup network interfaces in simulation.", exc);
 		}
 	}
 	
 	public static void scenario43(Simulation pSim) //Thomas for GAPI proxy: TODO: need extension point for FoG applications
 	{
 		// setup as1
 		pSim.executeCommand("@ - create as as1 true");
 		pSim.executeCommand("switch as1");
 
 		pSim.executeCommand("create node A half");
 		pSim.executeCommand("create node B all");
 		pSim.executeCommand("create bus bus_ab");
 		
 		pSim.executeCommand("connect A bus_ab");
 		pSim.executeCommand("connect B bus_ab");
 		
 		// setup as2
 		pSim.executeCommand("@ - create as as2 true");
 		pSim.executeCommand("switch as2");
 
 		pSim.executeCommand("create node C all");
 		pSim.executeCommand("create node D half");
 		pSim.executeCommand("create bus bus_cd");
 
 		pSim.executeCommand("connect C bus_cd");
 		pSim.executeCommand("connect D bus_cd");
 		
 		// inter as link
 		pSim.executeCommand("create bus bus_bc 1000 5 10");
 		pSim.executeCommand("connect C bus_bc");
 		
 		pSim.executeCommand("switch as1");
 		pSim.executeCommand("connect B bus_bc");
 		pSim.executeCommand("start App EchoServer D echo");	
 //		pSim.executeCommand("start App GAPIProxyApplication CD echo");	
 		
 		// setup server
 		pSim.executeCommand("switch as2");
 	}
 
 	public static void scenario33(Simulation pSim)
 	{
 		pSim.executeCommand("@ - create as default");
 		pSim.executeCommand("switch default");
 
 		pSim.executeCommand("create node A");
 		pSim.executeCommand("create node B");
 		pSim.executeCommand("create node C");
 		
 		pSim.executeCommand("create bus bus");
 		
 		pSim.executeCommand("connect A bus");
 		pSim.executeCommand("connect B bus");
 		pSim.executeCommand("connect C bus");
 	}
 
 	public static void scenario34(Simulation pSim)
 	{
 		pSim.executeCommand("@ - create as default");
 		pSim.executeCommand("switch default");
 
 		pSim.executeCommand("create node A");
 		pSim.executeCommand("create node B");
 		pSim.executeCommand("create node C");
 		pSim.executeCommand("create node D");
 		
 		pSim.executeCommand("create bus bus");
 		
 		pSim.executeCommand("connect A bus");
 		pSim.executeCommand("connect B bus");
 		pSim.executeCommand("connect C bus");
 		pSim.executeCommand("connect D bus");
 	}
 
 	public static void scenario35(Simulation pSim)
 	{
 		pSim.executeCommand("@ - create as default");
 		pSim.executeCommand("switch default");
 
 		pSim.executeCommand("create node A");
 		pSim.executeCommand("create node B");
 		pSim.executeCommand("create node C");
 		pSim.executeCommand("create node D");
 		pSim.executeCommand("create node E");
 		
 		pSim.executeCommand("create bus bus");
 		
 		pSim.executeCommand("connect A bus");
 		pSim.executeCommand("connect B bus");
 		pSim.executeCommand("connect C bus");
 		pSim.executeCommand("connect D bus");
 		pSim.executeCommand("connect E bus");
 	}
 
 	public static void scenario3(Simulation pSim)
 	{
 		pSim.executeCommand("@ - create as default");
 		pSim.executeCommand("switch default");
 
 		pSim.executeCommand("create node A");
 		pSim.executeCommand("create node B");
 		pSim.executeCommand("create node C");
 		
 		pSim.executeCommand("create bus bus_ab");
 		pSim.executeCommand("create bus bus_bc");
 		
 		pSim.executeCommand("connect A bus_ab");
 		pSim.executeCommand("connect B bus_ab");
 		
 		pSim.executeCommand("connect B bus_bc");
 		pSim.executeCommand("connect C bus_bc");
 	}
 	
 	public static void scenario2(Simulation pSim)
 	{
 		pSim.executeCommand("@ - create as default");
 		pSim.executeCommand("switch default");
 
 		pSim.executeCommand("create node A");
 		pSim.executeCommand("create node B");
 		pSim.executeCommand("create bus bus");
 		
 		pSim.executeCommand("connect A bus");
 		pSim.executeCommand("connect B bus");
 		
 		// having a server for testing
 		pSim.executeCommand("start App EchoServer B echo");
 	}
 	
 	public static void scenario1(Simulation pSim)
 	{
 		pSim.executeCommand("@ - create as default");
 		pSim.executeCommand("switch default");
 
 		pSim.executeCommand("create node A");
 		pSim.executeCommand("create bus bus");
 		
 		pSim.executeCommand("connect A bus");
 	}
 }
 
 
