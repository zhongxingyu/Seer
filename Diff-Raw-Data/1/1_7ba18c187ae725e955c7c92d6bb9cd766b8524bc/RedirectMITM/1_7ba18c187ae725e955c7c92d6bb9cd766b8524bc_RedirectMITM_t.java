 package org.bitducks.spoofing.services;
 
 import java.net.InetAddress;
 import java.util.HashMap;
 
 import jpcap.packet.Packet;
 
 import org.bitducks.spoofing.core.InterfaceInfo;
 import org.bitducks.spoofing.core.Server;
 import org.bitducks.spoofing.core.Service;
 import org.bitducks.spoofing.customrules.IpAndMacFilterRule;
 
 public class RedirectMITM extends Service {
 
 	private InterfaceInfo infoInterface;
 	private HashMap<InetAddress, byte[]> ipToMac;
 
 	public RedirectMITM() {
 		infoInterface = Server.getInstance().getInfo();
 		this.getPolicy().addRule(new IpAndMacFilterRule(infoInterface.getAddress(), infoInterface.getMacAddress()));
 	}
 	
 	@Override
 	public void run() {
 		while(! this.isCloseRequested()){
 			Packet toTransfer = this.getNextBlockingPacket();
 			redirectPacket(toTransfer);
 		}
 	}
 
 	private void redirectPacket(Packet p) {
 	}
 	
 	
 
 }
