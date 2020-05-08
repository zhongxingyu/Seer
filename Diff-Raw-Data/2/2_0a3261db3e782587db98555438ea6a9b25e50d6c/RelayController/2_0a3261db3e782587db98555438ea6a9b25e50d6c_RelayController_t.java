 package ch.compass.gonzoproxy.controller;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.ResourceBundle;
 
 import ch.compass.gonzoproxy.listener.StateListener;
 import ch.compass.gonzoproxy.model.SessionModel;
 import ch.compass.gonzoproxy.relay.GonzoRelayService;
 import ch.compass.gonzoproxy.relay.RelayService;
 import ch.compass.gonzoproxy.relay.modifier.FieldRule;
 import ch.compass.gonzoproxy.relay.modifier.PacketRegex;
 import ch.compass.gonzoproxy.relay.modifier.PacketRule;
 
 public class RelayController {
 
 	private boolean sessionIsActive = false;
 	
 	private String[] relayModes;
 
 	private Thread relayServiceThread;
 	private RelayService relayService = new GonzoRelayService();
 
 	public RelayController() {
 		loadPossibleRelayModes();
 	}
 
 	public void newSession(String portListen, String remoteHost,
 			String remotePort, String mode) {
 		stopRunningSession();
 		relayService.generateNewSessionParameters(portListen, remoteHost,
 				remotePort, mode);
 		relayServiceThread = new Thread(relayService);
 		relayServiceThread.start();
 		sessionIsActive = true;
 	}
 
 	public void stopRunningSession() {
 		if (sessionIsActive) {
 			relayService.killSession();
 			sessionIsActive = false;
 		}
 	}
 
 	public SessionModel getSessionModel() {
 		return relayService.getSessionModel();
 	}
 
 	public String[] getPossibleRelayModes() {
 		return relayModes;
 	}
 
 	public ArrayList<PacketRule> getPacketRules() {
 		return relayService.getPacketRules();
 	}
 
 	public ArrayList<PacketRegex> getPacketRegex() {
 		return relayService.getPacketRegex();
 	}
 
 	public void addModifierRule(String packetName, String fieldName,
 			String originalValue, String replacedValue, Boolean updateLength) {
 		FieldRule fieldRule = new FieldRule(fieldName, originalValue,
 				replacedValue);
 		relayService.addRule(packetName, fieldRule, updateLength);
 		persistRules();
 	}
 
 	public void addRegex(String regex, String replaceWith, boolean isActive) {
 		PacketRegex packetRegex = new PacketRegex(regex, replaceWith);
 		relayService.addRegex(packetRegex, isActive);
 		persistRegex();
 	}
 
 	public void commandTrapChanged() {
 		relayService.commandTrapChanged();
 	}
 
 	public void responseTrapChanged() {
 		relayService.responseTrapChanged();
 	}
 
 	public void sendOneCmd() {
 		relayService.sendOneCmd();
 	}
 
 	public void sendOneRes() {
 		relayService.sendOneRes();
 	}
 
 	public int getCurrentListenPort() {
 		return relayService.getCurrentListenPort();
 	}
 
 	public String getCurrentRemoteHost() {
 		return relayService.getCurrentRemoteHost();
 	}
 
 	public int getCurrentRemotePort() {
 		return relayService.getCurrentRemotePort();
 	}
 
 	public void addSessionStateListener(StateListener stateListener) {
 		relayService.addSessionStateListener(stateListener);
 	}
 
 	public void reparsePackets() {
 		relayService.reParse();
 	}
 
 	public void persistSessionData(File file) {
 		try {
 			relayService.persistSessionData(file);
 		} catch (IOException e) {
 			// notify user
 			e.printStackTrace();
 		}
 	}
 
 	public void loadPacketsFromFile(File file) {
 		try {
 			relayService.loadPacketsFromFile(file);
 		} catch (ClassNotFoundException | IOException e) {
 			// notify user
 		}
 	}
 
 	public void persistRules() {
 		try {
 			relayService.persistRules();
 		} catch (IOException e) {
 			// TODO: save failed notification
 		}
 
 	}
 
 	public void persistRegex() {
 		try {
 			relayService.persistRegex();
 		} catch (IOException e) {
 			// TODO: PERSISTNG FAIL
 		}
 	}
 
 	private void loadPossibleRelayModes() {
 		ArrayList<String> inputModes = new ArrayList<>();
 
 		ResourceBundle bundle = ResourceBundle.getBundle("plugin");
 
 		Enumeration<String> keys = bundle.getKeys();
 		while (keys.hasMoreElements()) {
 			String element = keys.nextElement();
 			if (element.contains("name")) {
 				inputModes.add(bundle.getString(element));
 			}
 		}
 
		this.relayModes = inputModes.toArray(new String[inputModes.size()]);
 	}
 
 }
