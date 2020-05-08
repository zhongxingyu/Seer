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
 package de.tuilmenau.ics.fog.topology;
 
 
 import java.io.Serializable;
 import java.util.LinkedList;
 import java.util.Observable;
 import java.util.Random;
 
 import de.tuilmenau.ics.CommonSim.datastream.StreamTime;
 import de.tuilmenau.ics.CommonSim.datastream.numeric.IDoubleWriter;
 import de.tuilmenau.ics.CommonSim.datastream.numeric.SumNode;
 import de.tuilmenau.ics.fog.Config;
 import de.tuilmenau.ics.fog.EventHandler;
 import de.tuilmenau.ics.fog.IEvent;
 import de.tuilmenau.ics.fog.application.Application;
 import de.tuilmenau.ics.fog.authentication.IdentityManagement;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Host;
 import de.tuilmenau.ics.fog.facade.Identity;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.Namespace;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.packets.Packet;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.RoutingService;
 import de.tuilmenau.ics.fog.routing.RoutingServiceMultiplexer;
 import de.tuilmenau.ics.fog.topology.ILowerLayerReceive.Status;
 import de.tuilmenau.ics.fog.transfer.TransferPlane;
 import de.tuilmenau.ics.fog.transfer.TransferPlaneObserver.NamingLevel;
 import de.tuilmenau.ics.fog.transfer.forwardingNodes.Multiplexer;
 import de.tuilmenau.ics.fog.transfer.manager.Controller;
 import de.tuilmenau.ics.fog.transfer.manager.ProcessRegister;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.util.Logger;
 import de.tuilmenau.ics.fog.util.SimpleName;
 import de.tuilmenau.ics.fog.util.ParameterMap;
 
 
 /**
  * A Node represents a host in a network.
  * It provides all functions needed locally on a host, such as a routing
  * and authentication service. Furthermore, it can be attached to lower
  * layers providing connectivity to other nodes.
  */
 public class Node extends Observable implements IElementDecorator
 {
 	public Node(String pName, AutonomousSystem pAS, ParameterMap pParameters)
 	{
 		logger = new Logger(pAS.getLogger());
 		isBroken = false;
 		isShuttingDown = false;
 		name = pName;
 		as = pAS;
 		controlgate = new Controller(this);
 		transferPlane = new TransferPlane(getTimeBase(), logger);
 
 		// TODO move this stuff to hierarchical plug-in
 		Random tRandomGenerator = new Random(System.currentTimeMillis());
 		for(int i = 0; i < mBullyPriority.length; i++)
 		{
 			mBullyPriority[i] = tRandomGenerator.nextFloat() * 10;
 		}
 		Logging.log(this, "This node has priority " + mBullyPriority[0]);
 
 		parameters = pParameters;
 		// Note: Do not create central FN here, because we do not have
 		//       a routing service available.
 		multiplexgate = null;
 		
 		// set capabilities of the node
		String tCap = pParameters.get(Cap, null);
 		if((tCap == null) || "all".equalsIgnoreCase(tCap)) {
 			capabilities = Description.createHostExtended();
 		}
 		else if("half".equalsIgnoreCase(tCap)) {
 			capabilities = Description.createHostBasic();
 		}
 		else if("none".equalsIgnoreCase(tCap)) {
 			capabilities = new Description();
 		}
 
 		
 		
 		// TEST:
 //		routingService = new RoutingServiceMultiplexer();
 //		((RoutingServiceMultiplexer)routingService).add(new RoutingService(pRoutingService));
 		
 		host = new Host(this);
 		authenticationService = IdentityManagement.getInstance(pAS, host);
 		ownIdentity = getAuthenticationService().createIdentity(name.toString());
 	}
 	
 	/**
 	 * @deprecated Since a node does not need to be named. Just the apps need names. Just for GUI use.
 	 */
 	public String getName()
 	{
 		return name;
 	}
 	
 	public AutonomousSystem getAS()
 	{
 		return as;
 	}
 	
 	/**
 	 * Registers a routing service entity at a node.
 	 *  
 	 * @param pRS Local routing service entity
 	 */
 	public void registerRoutingService(RoutingService pRS)
 	{
 		if(routingService == null) {
 			routingService = pRS;
 		} else {
 			// check, if already a multiplexer available
 			if(routingService instanceof RoutingServiceMultiplexer) {
 				((RoutingServiceMultiplexer) routingService).add(pRS); 
 			} else {
 				// ... no -> create one and store old and new rs entities in it
 				RoutingService rs = routingService;
 				
 				RoutingServiceMultiplexer rsMult = new RoutingServiceMultiplexer(); 
 				rsMult.add(rs);
 				rsMult.add(pRS);
 				
 				// activate new RS multiplexer as new RS of node
 				routingService = rsMult;
 			}
 		}
 		
 		// inform transfer service about new routing service
 		transferPlane.setRoutingService(routingService);
 	}
 	
 	/**
 	 * Unregisters a local routing service entity.
 	 * 
 	 * @param pRS Routing service entity to unregister
 	 * @returns true==success; false==RS was not registered
 	 */
 	public boolean unregisterRoutingService(RoutingService pRS)
 	{
 		if(routingService != null) {
 			// check, if already a multiplexer available
 			if(routingService instanceof RoutingServiceMultiplexer) {
 				return ((RoutingServiceMultiplexer) routingService).remove(pRS); 
 			} else {
 				if(routingService == pRS) {
 					routingService = null;
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * @return Reference to routing service of node (!= null)
 	 */
 	public TransferPlane getTransferPlane()
 	{
 		// Debug check: It should not happen, since a node gets at least one
 		//              routing service created by the RoutingServiceFactory.
 		if(transferPlane == null) throw new RuntimeException("Node " +this +" does not have a routing service.");
 			
 		return transferPlane;
 	}
 	
 	public boolean hasRoutingService()
 	{
 		return routingService != null;
 	}
 	
 	public RoutingService getRoutingService()
 	{
 		return routingService;
 	}
 	
 	public Controller getController()
 	{
 		return controlgate;
 	}
 	
 	/**
 	 * The main FN is just an implementation artifact. From the FoG concept, it is
 	 * not needed. It would be possible to use several FNs within a node (e.g. one connecting
 	 * the interfaces and one connecting the services). But a central one make debugging
 	 * much easier and simplifies the attachment question for elements of the transfer
 	 * service.
 	 * 
 	 * @return The main FN of a node, which connects all interfaces and services within a node.
 	 */
 	public Multiplexer getCentralFN()
 	{
 		if(multiplexgate == null) {
 			Name nameObj = null;
 			if(!Config.Routing.REDUCE_NUMBER_FNS) {
 				nameObj = new SimpleName(NAMESPACE_HOST, name);
 			}
 			// Register node in routing services at attaching the first interface.
 			// It is important, that it is registered before the interface is created.
 			// TODO name for multiplexer is not really needed => remove it when code finished
 			multiplexgate = new Multiplexer(this, nameObj, NamingLevel.NAMES, Config.Routing.ENABLE_NODE_RS_HIERARCHY_LEVEL, ownIdentity, null, controlgate);
 			multiplexgate.open();
 		}
 		
 		return multiplexgate;
 	}
 	
 	public Identity getIdentity()
 	{
 		return ownIdentity;
 	}
 	
 	public IdentityManagement getAuthenticationService()
 	{
 		return authenticationService;
 	}
 	
 	public ProcessRegister getProcessRegister()
 	{
 		if(processes == null) {
 			processes = new ProcessRegister();
 		}
 		
 		return processes; 
 	}
 	
 	/**
 	 * @return Host facade of this node (!= null)
 	 */
 	public Host getHost()
 	{
 		return host;
 	}
 	
 	/**
 	 * @return Configuration of the simulation (!= null)
 	 */
 	public Config getConfig()
 	{
 		return as.getSimulation().getConfig();
 	}
 	
 	/**
 	 * @return Parameter set of the node (!= null)
 	 */
 	public ParameterMap getParameter()
 	{
 		// debug check:
 		if(parameters == null) parameters = new ParameterMap(false);
 		
 		return parameters;
 	}
 	
 	/**
 	 * Method for sending test messages in the network.
 	 * 
 	 * @deprecated Please use applications establishing connections to other applications.
 	 * @param target Name of the destination node for the test message.
 	 * @param data Data to be send.
 	 * @throws NetworkException On error
 	 */
 	public void send(String target, Serializable data) throws NetworkException
 	{
 		if(transferPlane == null) {
 			throw new NetworkException("Node " +this +" does not have a routing service.");
 		}
 		Packet newpacket = null;
 		try {
 			Route route = transferPlane.getRoute(getCentralFN(), new SimpleName(NAMESPACE_HOST, target), Description.createBE(false), getIdentity());
 			newpacket = new Packet(route, data);
 			newpacket.setSourceNode(name);
 			newpacket.setTargetNode(target);
 			logger.log(this, "sending packet " +newpacket);
 			multiplexgate.handlePacket(newpacket, null);
 		} catch (NetworkException nExc) {
 			logger.err(this, "No route available", nExc);
 		}
 
 	}
 
 	public NetworkInterface attach(ILowerLayer lowerLayer)
 	{
 		
 		return controlgate.addLink(lowerLayer);
 	}
 	
 	public NetworkInterface detach(ILowerLayer lowerLayer)
 	{
 		if(controlgate != null) {
 			return controlgate.removeLink(lowerLayer);
 		}
 		
 		return null;
 	}
 	
 	public int getNumberLowerLayers()
 	{
 		return controlgate.getNumberLowerLayers();
 	}
 	
 	public boolean isGateway()
 	{
 		return true;
 	}
 	
 	public Status isBroken()
 	{
 		if(isBroken) {
 			if(isErrorTypeVisible) {
 				return Status.BROKEN;
 			} else {
 				return Status.UNKNOWN_ERROR;
 			}
 		} else {
 			return Status.OK;
 		}
 	}
 	
 	
 	public boolean isShuttingDown()
 	{
 		return isShuttingDown;
 	}
 	
 	public void setBroken(boolean broken, boolean errorTypeVisible)
 	{
 		boolean stateChange = isBroken != broken;
 		isBroken = broken;
 		
 		getLogger().info(this, "Node is now "+(broken ? "broken" : "working"));
 		
 		if(isBroken) {
 			isErrorTypeVisible = errorTypeVisible;
 			as.getTimeBase().scheduleIn(10.0d, new IEvent() {
 				@Override
 				public void fire()
 				{
 					if(!repair()) {
 						as.getTimeBase().scheduleIn(10.0d, this);
 					}
 				}
 			});
 		}
 		
 		if(stateChange) notifyObservers(broken);
 	}
 	
 	/**
 	 * Tells a node to shutdown all services running on it.
 	 * It will be done in order to shutdown services before
 	 * the node will be removed from simulation.
 	 */
 	public void shutdown(boolean waitForExit)
 	{
 		isShuttingDown = true;
 		
 		// do not use list directly, because apps will remove themselves
 		// from the list, which invalidates iterators
 		LinkedList<Application> apps = new LinkedList<Application>(host.getApps());
 		
 		for(Application app : apps) {
 			app.exit();
 			
 			if(waitForExit) {
 				app.waitForExit();
 			}
 		}
 	}
 	
 	/**
 	 * Informs node that it was deleted from the scenario.
 	 * Resets node and closes everything.
 	 */
 	public void deleted()
 	{
 		shutdown(true);
 		
 		if(controlgate != null)
 			controlgate.closed();
 		
 		if(multiplexgate != null)
 			multiplexgate.close();
 		
 		if((routingService != null) && (routingService instanceof RoutingServiceMultiplexer)) {
 			((RoutingServiceMultiplexer) routingService).clear();
 		}
 		
 		name = null;
 		routingService = null;
 		transferPlane = null;
 		host = null;
 		authenticationService = null;
 		ownIdentity	= null;
 		controlgate = null;
 		multiplexgate = null;
 	}
 	
 	private boolean repair()
 	{
 		if(isBroken) {
 			// we are broken, no repair
 			return false;
 		} else {
 			if(controlgate != null) {
 				controlgate.repair();
 			}
 			
 			return true;
 		}
 	}
 	
 	/**
 	 * @return Description of capabilities of this node. This includes the
 	 *         types of gates this node is able to create.
 	 */
 	public Description getCapabilities()
 	{
 		return capabilities;
 	}
 	
 	/**
 	 * Sets new capabilities for this node.
 	 * Replaces internal capabilities with the new one.
 	 */
 	public void setCapabilities(Description pCapabilities)
 	{
 		capabilities = pCapabilities;
 		controlgate.updateFNsCapabilties(capabilities);
 	}
 	
 	/**
 	 * @return Get time base for this node
 	 */
 	public EventHandler getTimeBase()
 	{
 		return as.getTimeBase();
 	}
 	
 	/**
 	 * @return Logger for this node
 	 */
 	public Logger getLogger()
 	{
 		return logger;
 	}
 	
 	/**
 	 * @return Prefix for node statistics
 	 */
 	public String getCountNodePrefix()
 	{
 		if(countPrefixCache == null) {
 			countPrefixCache = getClass().getName() +"." +this +".";
 		}
 		
 		return countPrefixCache;
 	}
 	
 	/**
 	 * Statistic function for counting elements on a node.
 	 * 
 	 * @param pPostfix Postfix for statistic
 	 * @param increment Indicates if the counter should be incremented or decremented
 	 */
 	public void count(String pPostfix, boolean increment)
 	{
 		if(Config.Logging.CREATE_NODE_STATISTIC) {
 			StreamTime tNow = getTimeBase().nowStream();
 			String baseName = getCountNodePrefix() +pPostfix;
 			double incr = 1.0d;
 			
 			if(!increment) incr = -1.0d;
 			
 			IDoubleWriter tSum = SumNode.openAsWriter(baseName +".number");
 			tSum.write(incr, tNow);
 			
 			if(increment) {
 				tSum = SumNode.openAsWriter(baseName +".totalSum");
 				tSum.write(1.0d, tNow);
 			}
 		}
 	}
 	
 	@Override
 	public String toString()
 	{
 		if(name == null) return null;
 		else return name.toString();
 	}
 
 	/**
 	 * @deprecated TODO remove it from Node/Host and move it to hierarchical plug-in
 	 */
 	public float getBullyPriority(int pLevel)
     {
             return mBullyPriority[pLevel];
     }
 	
 	public String getDecorationParameter()
 	{
 		return (String) mDecorationParameter;
 	}
 	
 	public void setDecorationParameter(Object pDecorationParameter)
 	{
 		mDecorationParameter = pDecorationParameter;
 		notifyObservers();
 	}
 	
 	@Override
 	public Object getValue()
 	{
 		return mLabel;
 	}
 
 	@Override
 	public void setValue(Object pLabel)
 	{
 		mLabel = pLabel;
 	}
 	
 	@Override
 	public synchronized void notifyObservers(Object pEvent)
 	{
 		setChanged();
 		super.notifyObservers(pEvent);
 	}
 	
 	private boolean isBroken;
 	private boolean isErrorTypeVisible;
 	
 	private String name;
 	private AutonomousSystem as;
 	private Logger logger;
 	private Controller controlgate;
 	private Multiplexer multiplexgate;
 	private TransferPlane transferPlane;
 	private RoutingService routingService;
 	private IdentityManagement authenticationService;
 	private Identity ownIdentity;
 	private Host host;
 	private ProcessRegister processes;
 	private Description capabilities;
 	private boolean isShuttingDown;
     private float [] mBullyPriority = new float[5];
     private Object mDecorationParameter=null;
     private Object mLabel;
     private String countPrefixCache;
 	public static final Namespace NAMESPACE_HOST = new Namespace("host");
     public static final int MAXIMUM_BULLY_PRIORITY = 90;
 	private ParameterMap parameters;
 	private final String Cap = "CAPABILITY";
 }
