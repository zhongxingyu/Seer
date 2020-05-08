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
 package de.tuilmenau.ics.fog.packets.statistics;
 
 import java.rmi.RemoteException;
 import java.util.LinkedList;
 import java.util.TreeSet;
 
 import de.tuilmenau.ics.fog.packets.ExperimentAgent;
 import de.tuilmenau.ics.fog.packets.Packet;
 import de.tuilmenau.ics.fog.scripts.RerouteScript;
 import de.tuilmenau.ics.fog.transfer.ForwardingElement;
 import de.tuilmenau.ics.fog.transfer.forwardingNodes.Multiplexer;
 import de.tuilmenau.ics.fog.transfer.gates.AbstractGate;
 import de.tuilmenau.ics.fog.transfer.gates.GateID;
 import de.tuilmenau.ics.fog.transfer.manager.Controller.RerouteMethod;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.middleware.JiniHelper;
 
 /**
  * This is a packet in which meta information for a rerouting experiment are put
  */
 public class ReroutingTestAgent extends ExperimentAgent implements IPacketStatistics
 {
 	private static final long serialVersionUID = 6777109421512772858L;
 	
 	private String mBrokenName;
 	
 	private RerouteMethod mRerouteMethod;
 	
 	private int mStep = 0;
 	private int mBrokenType;
 	private int mCountValue;
 	private LinkedList<GateID> mGates;
 	private TreeSet<String> mNodes;
 	
 	public ReroutingTestAgent()
 	{
 		super(ExperimentAgent.FN_NODE | ExperimentAgent.GATE);
 		
 		mGates = new LinkedList<GateID>();
 		mNodes = new TreeSet<String>();
 	}
 	
 	@Override
 	public LinkedList<String> getStats()
 	{
 		LinkedList<String> tStats = new LinkedList<String>();
 		if(mRerouteMethod != null) tStats.add(Integer.toString(mRerouteMethod.ordinal()));
 		
 		int tBrokenType;
 		if (mStep==1) tBrokenType = ReroutingExperiment.BROKEN_TYPE_NOTHING;
 		else tBrokenType = mBrokenType;
 		
 		tStats.add(Integer.toString(tBrokenType));
 		tStats.add(mBrokenName);
 		return tStats;
 	}
 	
 	public boolean setRerouteMethod(RerouteMethod method)
 	{
 		this.mRerouteMethod = method;
 		return true;
 	}
 	
 	public boolean setStep(int step)
 	{
 		this.mStep = step;
 		return true;
 	}
 	
 	public boolean setBrokenName(String broken)
 	{
 		this.mBrokenName = broken;
 		return true;
 	}
 	
 	public boolean setBrokenType(int type)
 	{
 		this.mBrokenType = type;
 		return true;
 	}
 	
 	public boolean setCount(int count)
 	{
 		this.mCountValue = count;
 		return true;
 	}
 	
 	/*
 	 *  output of object
 	 */
 	
 	public RerouteMethod getRerouteMethod()
 	{
 		return this.mRerouteMethod;
 	}
 
 	public int getStep()
 	{
 		return this.mStep;
 	}
 	
 	public int getCount()
 	{
 		return this.mCountValue;
 	}
 	
 	@Override
 	public boolean nextStep(ForwardingElement pElement, Packet pPacket) 
 	{
 		if (pElement instanceof AbstractGate) {
 			mGates.add(((AbstractGate)pElement).getGateID());
 		}
 		else if (pElement instanceof Multiplexer){
 			Multiplexer m = (Multiplexer)pElement;
			mNodes.add(String.valueOf(m.getNode().getCentralFN().getName()));
 			// Forwarding node
 		}
 			
 		return false;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see de.tuilmenau.ics.fog.packets.Signalling#execute(de.tuilmenau.ics.fog.gates.ForwardingElement, de.tuilmenau.ics.fog.packets.Packet)
 	 * Every packet has to provide some kind of execution procedure. If a controller gate receives this packet it calls this method. We get the
 	 * RerouteMaster and tell him which packet we received.
 	 * @param pElement the forwarding element that received this packet
 	 * @param pPacket the packet that was received, maybe this packet itself
 	 */
 	@Override
 	public boolean finish(ForwardingElement pElemet, Packet pPacket)
 	{
         try {
         	if(JiniHelper.isEnabled()) {
         		int step = ((ReroutingTestAgent)pPacket.getData()).getStep();
             	int count = ((ReroutingTestAgent)pPacket.getData()).getCount();
             	((IRerouteMaster)JiniHelper.getService(IRerouteMaster.class, "RerouteMaster_" + count)).tell(pPacket);
         	} else {
         		RerouteScript.getCurrentInstance().getExperiment().tell(pPacket);
         	}
         } catch (RemoteException e) {
         	Logging.err(this, "Error when trying to execute tell method of ReroutingPacket.");
         }
         return true;
 	}
 }
