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
 
 package ac.hw.rfid.server;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Timer;
 
 import javax.swing.JOptionPane;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
import org.societies.api.css.devicemgmt.rfid.IRfidDriver;
 import org.societies.api.schema.servicelifecycle.model.ServiceResourceIdentifier;
 
 import ac.hw.rfid.client.api.remote.IRfidClient;
 import ac.hw.rfid.server.api.IRfidServer;
 
 public class RfidServer implements IRfidServer {
 
 	private Logger logging = LoggerFactory.getLogger(this.getClass());
 	//private ServiceResourceIdentifier myServiceId;
 	//private List<String> myServiceTypes = new ArrayList<String>();
 	
 	private Hashtable<String, String> tagToPasswordTable;
 
 	private Hashtable<String, String> tagtoIdentityTable;
 	
 	private Hashtable<String, String> wUnitToSymlocTable;
 	private ServerGUIFrame frame;
 	
	private IRfidDriver rfidDriver;
	
 	//private Hashtable<String, String> dpiToServiceID;
 	Hashtable<String, Timer> tagToTimerTable = new Hashtable<String, Timer>();
 
 	private IRfidClient rfidClient;
 	
 	public void initialiseRFIDServer(){
 		this.tagtoIdentityTable = new Hashtable<String, String>();
 		this.tagToPasswordTable = new Hashtable<String, String>();
 		//this.dpiToServiceID = new Hashtable<String, String>();
 		RFIDConfig rfidConfig = new RFIDConfig();
 		this.wUnitToSymlocTable = rfidConfig.getUnitToSymloc();
 		if (this.wUnitToSymlocTable==null){
 			this.wUnitToSymlocTable = new Hashtable<String, String>();
 			
 		}
 		frame = new ServerGUIFrame(this);
 	}
 	
 	public RfidServer(){
 		
 
 
 	}
 
 
 	
 /*	public void sendAcknowledgeMessage(String clientDPI, String clientServiceID, Integer rStatus){
 		String intAsXML = XMLConverter.objectToXml(rStatus);
 		ServiceMessage msg = new ServiceMessage(this.myServiceId.toUriString(),
 				clientServiceID,
 				clientDPI,
 				true,
 				"acknowledgeRegistration",
 				false,
 				new String[]{intAsXML},
 				new String[]{Integer.class.getName()});
 		try {
 			this.msgQ = this.getMessageQueue();
 			this.msgQ.addServiceMessage(msg);
 		} catch (ONMException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		logging.debug("Sent message:" +
 				"\nsourceServiceID: "+this.myServiceId.toUriString()+
 				"\ntargetServiceID: "+clientServiceID+
 				"\ndestinationID: "+clientDPI+
 				"\ntargetIsOtherPSS: "+msg.targetIsOtherPSS()+
 				"\ntargetOperation: "+msg.getTargetOperation()+
 				"\noperationIsAsync: "+msg.operationIsAsync()+
 				"\nparameters: "+intAsXML+
 				"\nparameterTypes: "+Integer.class.getName());
 	}
 	public void sendUpdateMessage(String clientDPI, String clientServiceID, String tagNumber, String symLoc) {
 		this.msgQ = this.getMessageQueue();
 		
 		ServiceMessage msg = new ServiceMessage(
 				this.myServiceId.toUriString(),
 				clientServiceID,
 				clientDPI,
 				true,
 				"sendUpdate",
 				true,
 				new String[]{symLoc,tagNumber}, new String[]{String.class.getName(), String.class.getName()});
 		try {
 			this.msgQ.addServiceMessage(msg);
 		} catch (ONMException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			JOptionPane.showMessageDialog(null, "Failed to send RFID update to :"+clientDPI);
 		}
 		logging.debug("Sent message:" +
 				"\nsourceServiceID: "+this.myServiceId.toUriString()+
 				"\ntargetServiceID: "+clientServiceID+
 				"\ndestinationID: "+clientDPI+
 				"\ntargetIsOtherPSS: "+msg.targetIsOtherPSS()+
 				"\ntargetOperation: "+msg.getTargetOperation()+
 				"\noperationIsAsync: "+msg.operationIsAsync()+
 				"\nparameters: "+symLoc+tagNumber+
 				"\nparameterTypes: "+String.class.getName());
 	}*/
 
 
 
 
 	/*
 	 * Method called when an RFID_UPDATE_EVENT is received
 	 */
 	public void sendUpdate(String wUnit, String rfidTagNumber) {
 	
 		String symLoc = "other";
 		if (this.wUnitToSymlocTable.containsKey(wUnit)){
 			symLoc = this.wUnitToSymlocTable.get(wUnit);
 			if (this.tagToTimerTable.containsKey(rfidTagNumber)){
 				this.tagToTimerTable.get(rfidTagNumber).cancel();
 			}
 			
 		}else{
 			logging.debug("wUnit: "+wUnit+" not found, wUnit length: "+wUnit.length());
 			Enumeration<String> e = this.wUnitToSymlocTable.keys();
 			logging.debug("Existing wUnits: ");
 			while (e.hasMoreElements()){
 				String u = e.nextElement();
 				logging.debug(u+" size: "+u.length());
 			}
 			
 		}
 		if (!symLoc.equalsIgnoreCase("other")){
 			Timer timer = new Timer();
 			RFIDUpdateTimerTask task = new RFIDUpdateTimerTask(this, rfidTagNumber);
 			timer.schedule(task, 3000);
 			this.tagToTimerTable.put(rfidTagNumber, timer);
 		}		
 		if (this.tagtoIdentityTable.containsKey(rfidTagNumber)){
 			String dpi = this.tagtoIdentityTable.get(rfidTagNumber);
 			//String clientServiceID = this.dpiToServiceID.get(dpi);
 			//this.sendUpdateMessage(dpi, clientServiceID, rfidTagNumber, symLoc);
 			this.rfidClient.sendUpdate(dpi, symLoc, rfidTagNumber);
 			this.frame.addRow(dpi, rfidTagNumber, wUnit, symLoc);
 
 		}else{
 			//JOptionPane.showMessageDialog(null, "Tag: "+rfidTagNumber+" in location "+symLoc+" not registered to a DPI");
 			logging.debug("Tag: "+rfidTagNumber+" in location "+symLoc+" not registered to a DPI");
 			this.frame.addRow("Unregistered", rfidTagNumber, wUnit, symLoc);
 		}
 		
 		
 	}
 
 	@Override
 	public void registerRFIDTag(String tagNumber, String dpiAsString, String serviceID, String password) {
 		logging.debug("Received request to register RFID tag: "+tagNumber+" from dpi: "+dpiAsString+" and serviceID: "+serviceID);
 			if (this.tagToPasswordTable.containsKey(tagNumber)){
 				String myPass = this.tagToPasswordTable.get(tagNumber);
 				logging.debug("Tag exists");
 				if (myPass.equalsIgnoreCase(password)){
 					
 					this.removeOldRegistration(dpiAsString);
 					
 					this.tagtoIdentityTable.put(tagNumber, dpiAsString);
 					//this.dpiToServiceID.put(dpiAsString, serviceID);
 					//this.sendAcknowledgeMessage(dpiAsString, serviceID, 0);
 					this.frame.setNewDPIRegistered(tagNumber, dpiAsString);
 					Timer timer = new Timer();
 					RFIDUpdateTimerTask task = new RFIDUpdateTimerTask(this,  tagNumber);
 					timer.schedule(task, 3000);
 					this.tagToTimerTable.put(tagNumber, timer);
 					logging.debug("Registration successfull. Sent Acknowledgement 0");
 					
 					
 				}else{
 					//this.sendAcknowledgeMessage(dpiAsString, serviceID, 1);
 					logging.debug("Registration unsuccessfull. Sent Ack 1");
 				}
 			}else{
 				
 				//this.sendAcknowledgeMessage(dpiAsString, serviceID, 2);
 				logging.debug("Registration unsuccessfull. Sent Ack 2");
 			}
 		
 
 		
 	}
 
 	private void removeOldRegistration( String dpiAsString){
 		if (this.tagtoIdentityTable.contains(dpiAsString)){
 			
 			Enumeration<String> tags = this.tagtoIdentityTable.keys();
 			
 			while (tags.hasMoreElements()){
 				String tag = tags.nextElement();
 				String dpi = this.tagtoIdentityTable.get(tag);
 				if (dpi.equalsIgnoreCase(dpiAsString)){
 					this.tagtoIdentityTable.remove(tag);
 					/*if (this.dpiToServiceID.containsKey(dpiAsString)){
 						this.dpiToServiceID.remove(dpiAsString);
 					}*/
 					this.frame.setNewDPIRegistered(tag, "");
 					return;
 				}
 			}
 		}
 		
 		/*if (this.dpiToServiceID.containsKey(dpiAsString)){
 			this.dpiToServiceID.remove(dpiAsString);
 		}*/
 	
 
 	}
 	public String getPassword() {
 		int n = 4;
 		char[] pw = new char[n];
 		int c  = 'A';
 		int  r1 = 0;
 		for (int i=0; i < n; i++)
 		{
 			r1 = (int)(Math.random() * 3);
 			switch(r1) {
 			case 0: c = '0' +  (int)(Math.random() * 10); break;
 			case 1: c = 'a' +  (int)(Math.random() * 26); break;
 			case 2: c = 'A' +  (int)(Math.random() * 26); break;
 			}
 			pw[i] = (char)c;
 		}
 		return new String(pw);
 	}
 
 	public void storePassword(String tagNumber, String password){
 		this.tagToPasswordTable.put(tagNumber, password);
 		this.tagtoIdentityTable.remove(tagNumber);
 	}
 	
 	public static void main(String[] args) throws IOException{
 		RfidServer impl = new RfidServer();
 		System.out.println(impl.getPassword());
 		
 		
 		RFIDConfig config = new RFIDConfig();
 		impl.wUnitToSymlocTable = config.getUnitToSymloc();
 		
 		Hashtable<String, Timer> timerTable = new Hashtable<String, Timer>();
 		RFIDUpdateTimerTask task = new RFIDUpdateTimerTask(impl,"0071");
 		Timer timer = new Timer();
 		timer.schedule(task, 5000);
 		timerTable.put("0071", timer);
 		task.cancel();
 	}
 
 
 
 	/**
 	 * @return the rfidClient
 	 */
 	public IRfidClient getRfidClient() {
 		return rfidClient;
 	}
 
 
 
 	/**
 	 * @param rfidClient the rfidClient to set
 	 */
 	public void setRfidClient(IRfidClient rfidClient) {
 		this.rfidClient = rfidClient;
 	}

	/**
	 * @return the rfidDriver
	 */
	public IRfidDriver getRfidDriver() {
		return rfidDriver;
	}

	/**
	 * @param rfidDriver the rfidDriver to set
	 */
	public void setRfidDriver(IRfidDriver rfidDriver) {
		this.rfidDriver = rfidDriver;
	}
 }
