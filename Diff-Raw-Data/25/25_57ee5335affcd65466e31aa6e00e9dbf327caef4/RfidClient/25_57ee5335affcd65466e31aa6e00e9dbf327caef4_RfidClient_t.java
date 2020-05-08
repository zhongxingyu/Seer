 /**
  * Copyright (c) 2011, SOCIETIES Consortium (WATERFORD INSTITUTE OF TECHNOLOGY (TSSG), HERIOT-WATT UNIVERSITY (HWU), SOLUTA.NET 
  * (SN), GERMAN AEROSPACE CENTRE (Deutsches Zentrum fuer Luft- und Raumfahrt e.V.) (DLR), Zavod za varnostne tehnologije
  * informacijske družbe in elektronsko poslovanje (SETCCE), INSTITUTE OF COMMUNICATION AND COMPUTER SYSTEMS (ICCS), LAKE
  * COMMUNICATIONS (LAKE), INTEL PERFORMANCE LEARNING SOLUTIONS LTD (INTEL), PORTUGAL TELECOM INOVAÇÃO, SA (PTIN), IBM Corp., 
  * INSTITUT TELECOM (ITSUD), AMITEC DIACHYTI EFYIA PLIROFORIKI KAI EPIKINONIES ETERIA PERIORISMENIS EFTHINIS (AMITEC), TELECOM 
  * ITALIA S.p.a.(TI),  TRIALOG (TRIALOG), Stiftelsen SINTEF (SINTEF), NEC EUROPE LTD (NEC))
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
  * conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *    disclaimer in the documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package ac.hw.rfid.client;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 import javax.swing.JOptionPane;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.societies.api.comm.xmpp.interfaces.ICommManager;
 import org.societies.api.context.model.CtxAttributeTypes;
 import org.societies.api.context.source.ICtxSourceMgr;
 import org.societies.api.identity.IIdentity;
 import org.societies.api.identity.IIdentityManager;
 import org.societies.api.identity.InvalidFormatException;
 import org.societies.api.schema.servicelifecycle.model.ServiceResourceIdentifier;
 
 import ac.hw.rfid.client.api.IRfidClient;
 import ac.hw.rfid.server.api.remote.IRfidServer;
 
 
 
 
 /**
  * @author  Eliza Papadopoulou
  * @created December, 2010
  */
 
 public class RfidClient implements IRfidClient {
 
 	private ICommManager commManager;
 	private Logger logging = LoggerFactory.getLogger(this.getClass());
 	private ICtxSourceMgr ctxSourceMgr;
 	private IIdentityManager idm;
 	/* 
 	 * serviceID of RfidClient - Maria to explain how to get this
 	 */
 	private ServiceResourceIdentifier	clientID;
 	/*
 	 * serviceID of RfidServer
 	 */
 	private ServiceResourceIdentifier serverID;
 
 
 	/*
 	 * my LOCAL DPI
 	 */
 	private IIdentity userIdentity;
 
 	private ClientGUIFrame clientGUI;
 
 	private IIdentity serverIdentity;
 	private IRfidServer rfidServerRemote;
 	private String myCtxSourceId;
 
 
 	
 
 
 	public RfidClient() {
 		
 	}
 
 
 
 
 	public void close() {
 		this.clientGUI.setVisible(false);
 		this.clientGUI.dispose();
 	}
 
 
 	public void initialiseRFIDClient() {
 		this.idm = this.commManager.getIdManager();
 		this.userIdentity = this.idm.getThisNetworkNode();
 		String id = (String) JOptionPane.showInputDialog(null, "Please enter the JID of the CSS that runs the RFIDServer service", "Configuration needed", JOptionPane.QUESTION_MESSAGE, null, null, null);
 		boolean haveId = false;
 		
 		while(!haveId){
 			try {
 				this.serverIdentity = this.idm.fromJid(id);
 				haveId = true;
 			} catch (InvalidFormatException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				JOptionPane.showMessageDialog(null, "Invalid JID entered", "Error", JOptionPane.ERROR_MESSAGE, null);
 				id = (String) JOptionPane.showInputDialog(null, "Please enter the JID of the CSS that runs the RFIDServer service", "Configuration needed", JOptionPane.QUESTION_MESSAGE, null, null, null);
 			}
 		}
 		
 		boolean registered = this.register();
 		if (registered){
 			clientGUI = new ClientGUIFrame(this.getRfidServer(), this.userIdentity, this.serverIdentity, this.serverID, clientID);
 		}else{
 			JOptionPane.showMessageDialog(null, "RfidClient is unable to register as a context source with the ICtxSourceMgr at this point. " +
 					"Please contact the SOCIETIES development team to resolve this issue. RFIDClient application is now exiting");
 		}
 
 		
 	}
 
 
 	private boolean register(){
 		try {
 			Future<String> fID = this.ctxSourceMgr.register("RFID", CtxAttributeTypes.LOCATION_SYMBOLIC);
 			myCtxSourceId = fID.get();
 			return true;
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	@Override
 	public void sendUpdate(String symLoc, String tagNumber) {
 		this.clientGUI.sendSymLocUpdate(tagNumber, symLoc);
 		this.clientGUI.tfTagNumber.setText(tagNumber);
 		
 		if (this.myCtxSourceId==null){
 			boolean registered = this.register();
 			if (registered){
 				this.ctxSourceMgr.sendUpdate(this.myCtxSourceId, symLoc);
 			}else{
 				JOptionPane.showMessageDialog(null, "RfidClient is unable to register as a context source with the ICtxSourceMgr at this point. " +
 						"Please contact the SOCIETIES development team to resolve this issue");
 			}
 		}else{
 			this.ctxSourceMgr.sendUpdate(this.myCtxSourceId, symLoc);		}
 		
 	}
 
 	@Override
 	public void acknowledgeRegistration(Integer rStatus) {
 		this.clientGUI.acknowledgeRegistration(rStatus);
 		
 	}
 
 	/**
 	 * @return the commManager
 	 */
 	public ICommManager getCommManager() {
 		return commManager;
 	}
 
 	/**
 	 * @param commManager the commManager to set
 	 */
 	public void setCommManager(ICommManager commManager) {
 		this.commManager = commManager;
 	}
 
 
 
 
 	/**
 	 * @return the ctxSourceMgr
 	 */
 	public ICtxSourceMgr getCtxSourceMgr() {
 		return ctxSourceMgr;
 	}
 
 
 
 
 	/**
 	 * @param ctxSourceMgr the ctxSourceMgr to set
 	 */
 	public void setCtxSourceMgr(ICtxSourceMgr ctxSourceMgr) {
 		this.ctxSourceMgr = ctxSourceMgr;
 	}
 
 
 
 
 	/**
 	 * @return the rfidServer
 	 */
 	public IRfidServer getRfidServer() {
 		return rfidServerRemote;
 	}
 
 
 
 
 	/**
 	 * @param rfidServer the rfidServer to set
 	 */
 	public void setRfidServer(IRfidServer rfidServer) {
 		this.rfidServerRemote = rfidServer;
 	}
 
 
 
 
 
 
 } 
 
 
 
