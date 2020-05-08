 package com.dropoutdesign.ddf;
 
 import com.dropoutdesign.ddf.config.*;
 import com.dropoutdesign.ddf.module.*;
 import java.util.*;
 import java.awt.Rectangle;
 
 public class Module {
 
 	public static final int RETRY_INTERVAL_MS = 500;
 
 	private String address;
 	private Rectangle bounds;
 	
 	private ModuleConnection currentConnection = null;
 	private long lastFailureTime = 0;
 	private boolean badAddress = false;
 	
 	public Module(ModuleConfig config) {
 		address = config.getAddress();
 		bounds = config.getBounds();
 		if (bounds.width != 16 || bounds.height != 4) {
 			throw new IllegalArgumentException("Invalid module size: " + bounds.width 
 										+ "x" + bounds.height);
 		}
 	}
 	
 	public String getAddress() {
 		return address;
 	}
 	
 	public Rectangle getBounds() {
 		return bounds;
 	}
 	
 	public boolean isConnected() {
 		return (currentConnection == null);
 	}
 	
 	public ModuleConnection getConnection() {
 		return currentConnection;
 	}
 	
 	public void connect() throws UnknownConnectionTypeException, ModuleIOException {
 		currentConnection = ModuleConnection.open(address);
 		
 		System.out.println("Connected to module at " + address);
 		/* System.out.println("\tfirmware: "
 				+ Integer.toString(currentConnection.firmwareVersion(), 16));
 		System.out.println("\ti2c: "
 				+ Integer.toString(currentConnection.checkI2C(), 16)); */
 		
 		currentConnection.reset(); // send soft reset command to module
 	}
 
 	public void disconnect() {
 		currentConnection.close();
 		currentConnection = null;
 	}
 	
 	public void writeFrame(byte[] frame) throws ModuleIOException {
		System.out.println("[Module " + address + "]: Writing " + frame.length 
							+ " bytes across connection " + currentConnection);
		
 		byte[] cmd = new byte[97];
 		cmd[0] = 0x10;
 		int curNib = 2;
 
 		int xm = bounds.x + bounds.width;
 		int ym = bounds.y + bounds.height;
 		for (int x = bounds.x; x < xm; x++) {
 			for (int y = bounds.y; y < ym; y++) {
 				for (int c = 0; c < 3; c++) {	// Component
 					
 					// FIXME: KLUDGE KLUDGE KLUDGE
 					int whichByte = (y * 16 + x) * 3 + c;
 					int nib = ((frame[whichByte] >> 4) & 0xF) ^ 0xF; 
 						// flip because 0xF is off					
 					
 					if ((curNib % 2) == 0) {
 						cmd[(int)curNib/2] = (byte)(nib);
 					} else {
 						cmd[(int)curNib/2] |= (byte)(nib << 4);
 					}
 					curNib++;
 				}
 			}
 		}
 		currentConnection.sendCommand(cmd);
 	}
 }
