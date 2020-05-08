 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Importer
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
 package de.tuilmenau.ics.fog.importer.parser;
 
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.IWorker;
 import de.tuilmenau.ics.fog.importer.ITopologyParser;
 import de.tuilmenau.ics.fog.routing.naming.HierarchicalNameMappingService;
 import de.tuilmenau.ics.fog.routing.naming.NameMappingService;
 import de.tuilmenau.ics.fog.topology.Simulation;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.util.Logger;
 import de.tuilmenau.ics.middleware.JiniHelper;
 
 
 public class TopologyDistributor
 {
 	protected static final String DEFAULT_AS_NAME = "default";
 	
 	private NameMappingService mNMS = null;
 	private ITopologyParser topoHandler;
 	private int gauge;
 	
 	protected LinkedList<Object> workerByJini;
 	
 	private Simulation sim;
 	
 	private String node,
 		as,
 		nodeA,
 		nodeB;
 	
 	private Logger logger = Logging.getInstance();
 	private boolean oneAS;
 	
 	
 	public TopologyDistributor(ITopologyParser parser, Simulation sim, boolean flat) throws IOException
 	{
 		this.sim = sim;
 		topoHandler = parser;
 		oneAS = flat;
 		
 		checkWorkerNumberJini();
 		
 		mNMS = HierarchicalNameMappingService.getGlobalNameMappingService();
 		
 		createAS(DEFAULT_AS_NAME);
 		switchAS(DEFAULT_AS_NAME);
 	}
 	
 	protected void checkWorkerNumberJini()
 	{
 		int numberWorkers = topoHandler.getNumberWorkers();
 
 		// do we know how many worker are required?
 		if(numberWorkers >= 0) {
 			workerByJini = JiniHelper.getServices(IWorker.class, null);
 				
 			if(workerByJini == null) {
 				String errMsg = "Jini has no workers available.";
 				logger.err(this, errMsg);
 				throw new RuntimeException(errMsg);
 			}
 			
 			if(workerByJini.size() != numberWorkers) {
 				logger.err(this, "Jini reports another amount of total available workers.");
 			}
 		}
 	}
 
 	protected boolean switchAS(String toName)
 	{
 		if((toName == null) || ("".equals(toName))) {
 			toName = DEFAULT_AS_NAME;
 		}
 		
 		if(!sim.switchToAS(toName)) {
 			createAS(toName);
 		
 			if(!sim.switchToAS(toName)) {
 				logger.err(this, "Chosen worker did not create AS '" +toName +"' or was unable to switch to it!");
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	protected boolean createAS(String asName)
 	{
		return createAS(asName, sim.getConfig().Scenario.ENABLE_PARTIAL_RS_IN_IMPORT, null);
 	}
 	
 	protected boolean createAS(String asName, boolean partialRouting, String routingServiceName)
 	{
 		try {
 			StringBuffer cmd = new StringBuffer();
 			cmd.append("@ ");
 			cmd.append(getNextWorker());
 			cmd.append(" create as ");
 			cmd.append(asName);
 			cmd.append(" ");
 			cmd.append(partialRouting);
 			
 			if(routingServiceName != null) {
 				cmd.append(" ");
 				cmd.append(routingServiceName);
 			}
 			
 			return sim.executeCommand(cmd.toString());
 
 		} catch (RemoteException exc) {
 			logger.err(this, "Can not switch to AS '" +asName +"'", exc);
 			return false;
 		}
 	}
 	
  	public boolean createNodes()
  	{
  		if(topoHandler.requiresASMode() || oneAS) {
  			if(oneAS) {
  				logger.info(this, "Create nodes in one single AS " +DEFAULT_AS_NAME +".");
  			} else {
  				logger.info(this, "Create each node in own AS.");
  			}
  				
  			return createNodes(true);
  		}
  		else {
 			logger.info(this, "Create nodes in AS as specified in scenario file.");
 			return createNodes(false);
 		}
  	}
 
 	private boolean createNodes(boolean asMode)
  	{
  		while(topoHandler.readNextNodeEntry()) {
 			// We get the node and assign it to the given autonomous system
  			if(asMode) {
 				node = topoHandler.getAS();
 				
 				if(oneAS) {
 					as = DEFAULT_AS_NAME;
 				} else {
 					as = node;
 				}
  			} else {
  				node = topoHandler.getNode();
  				as = topoHandler.getAS();
  			}
  			String tParameter = topoHandler.getParameter();
 
 			if(switchAS(as)) {
 				if(createNode(node, tParameter)) {
 					logger.log(this, "Created node " + node + " in AS " + as + " with parameter " + tParameter);
 			 		try {
 						mNMS.setNodeASName(node, as);
 					} catch (RemoteException tExc) {
 						Logging.err(this, "Error when trying to set AS where node was supposed to be located", tExc);
 					}
 				} else {
 					logger.warn(this, "Failed to created node " + node + " in AS " + as);
 				}
 			} else {
 				return false;
 			}
 		}
  		return true;
  	}
 	
 	protected boolean createNode(String name, String pParameter)
 	{
 		String tCommand = "create node " +name;
 		if(pParameter != null) {
 			tCommand = tCommand + " " + pParameter;
 		}
 
 		return sim.executeCommand(tCommand);
 	}
 
 	private String getNextWorker() throws RemoteException
  	{
 		int numberWorkers = Math.max(1, topoHandler.getNumberWorkers());
 		
 		gauge++;
 		gauge %= numberWorkers;
 		
 		if(workerByJini != null) {
 			if(workerByJini.size() <= numberWorkers) {
 				return ((IWorker) workerByJini.get(gauge)).getName();
 			} else {
 				return "-";
 			}
  		} else {
  			// We return the current worker otherwise
  			return "-";
  		}
  	}
 
  	public boolean createEdges()
  	{
  		boolean res = true;
  		
 		while ( topoHandler.readNextEdgeEntry() ) {
 			nodeA = topoHandler.getEdgeNodeOne();
 			nodeB = topoHandler.getEdgeNodeTwo();
 			
 			try {
 				if(!oneAS) {
 					String nodeOneAS = mNMS.getASNameByNode(nodeA);
 					if(!switchAS(nodeOneAS)) {
 						logger.err(this, "Unable to switch to AS '" +nodeOneAS +"'. Skip edge from " +nodeA +"->" +nodeB +".");
 						continue; //We skip the creation of that edge
 					}
 				}
 				
 				String busName = nodeA + "-" + nodeB;
 				if(createBus(busName)) {
 					String nodeBASname = null;
 					
 					if(!oneAS || topoHandler.getInterAS()) {
 						nodeBASname = mNMS.getASNameByNode(nodeB);
 					}
 					
 					if(link(nodeA, nodeB, nodeBASname)) {
 						logger.log(this, "Created link between " +nodeA +" and " +nodeB);
 					} else {
 						logger.err(this, "Can not connect " + nodeA + " and " +nodeB);
 						res = false;
 					}
 				} else {
 					logger.err(this, "Can not create bus '" +busName +"'");
 					res = false;
 				}
 			} catch (RemoteException tExc) {
 				Logging.err(this, "Unable to find AS where node was put inside", tExc);
 			}
 		}
 		
  		return res;
  	}
  	
 	protected boolean createBus(String name)
 	{
 		return sim.executeCommand("create bus " + name);
 	}
 
 	protected boolean link(String nodeName1, String nodeName2, String nodeName2ASname)
 	{
 		boolean connectRes = true;
 		String busName = nodeName1 +"-" +nodeName2;
 		
 		connectRes &= connect(nodeName1, busName);
 		
 		// OK we are doing inter AS routing, so we have to find the other node inside another autonomous system
 		if(nodeName2ASname != null) {
 			if(!switchAS(nodeName2ASname)) {
 				logger.err(this, "Can not switch to AS '" +nodeName2ASname +"'");
 				sim.executeCommand("disconnect " +nodeName1 + " " +busName);
 				sim.executeCommand("remove bus " +busName);
 			}
 			
 		}
 		connectRes &= connect(nodeName2, busName);
 		
 		return connectRes;
 	}
 
 	protected boolean connect(String nodeName, String busName)
 	{
 		return sim.executeCommand("connect "+ nodeName + " " + busName);
 	}
 
 	public void close()
 	{
 		topoHandler.close();
 	}
 	
 	protected Simulation getSim()
 	{
 		return sim;
 	}
 }
