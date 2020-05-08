 package com.dropoutdesign.ddf;
 
 import com.dropoutdesign.ddf.module.*;
 import com.dropoutdesign.ddf.config.*;
 
 /**
  * Subclass of Module that provides automatic recovery from errors.
  */
 public class ReliableModule extends Module {
 	
 	/**
 	 * Default wait time to before attempting to re-establish the connection.
 	 */
 	public static final int RETRY_INTERVAL_MS = 500;
 	
 	private boolean shouldConnect;
 	private boolean hasError;
 	private long lastError;
 	
 	public ReliableModule(ModuleConfig mc) {
 		super(mc);
 		shouldConnect = false;
 		hasError = false;
 	}
 	
 	/**
 	 * Return the cannonical connection status of this module.
 	 * The value returned is independent of whether the connection is currently
 	 * broken due to an error.
 	 */
 	public boolean isConnected() {
 		return shouldConnect;
 	}
 	
 	/**
 	 * Return the current error status of this module.
 	 */
 	public boolean hasError() {
 		return hasError;
 	}
 	
 	/**
 	 * Connect to this module, retrying until sucessful.
 	 * This method attempts to connect once and returns, but will attempt to reconnect
 	 * upon later calls to writeFrame if necessary.
 	 */
 	public void connect() {
 		shouldConnect = true;
 		try {
 			super.connect();
		} catch (ModuleConnectionException e) {
 			hasError = true;
 			lastError = System.currentTimeMillis();
 		}
 	}
 	
 	/**
 	 * Disconnect from this module, ceasing all reconnection attempts.
 	 */
 	public void disconnect() {
 		super.disconnect();
 		shouldConnect = false;
 		hasError = false;
 	}
 	
 	/**
 	 * Write a frame to this module, attempting to diagnose and repair any failures that arise.
 	 */
 	public void writeFrame(byte[] frame) {
 		ModuleConnection mc = getConnection();
 		
 		if (hasError) {
 			long t = System.currentTimeMillis() - lastError;
 			if (t > RETRY_INTERVAL_MS) {	// Diagnose and re-establish
 				System.out.println("Trying to fix module " + address);
 				boolean shouldReconnect = false;
 				try {
 					byte b = mc.ping();
 					if (b != 0)
 						shouldReconnect = true;
 				} catch (Exception e) {		// Connection died, or never alive in the first place
 					shouldReconnect = true;
 				}
 				
 				if (shouldReconnect) {	// Module unresponsive or highly confused.
 					System.out.println("Ping failed on module " + address 
 								+ ", cycling link...");
 					disconnect();
 					connect();
 					if (hasError) {	// Failed to re-establish link, give up for now
 						return;
 					}
 				
 				} else {	// Module responsive, reset and continue
 					try {
 						byte b = mc.reset();
 						// TODO: Don't know what these responses mean...
 						System.out.println("Module " + address + " got response " 
 													+ b + " on reset.");
 					} catch (Exception e) {	// Apparently not
 						hasError = true;
 						lastError = System.currentTimeMillis();
 					}
 				}
 			}
 		}
 		
 		try {
 			super.writeFrame(frame);
 			byte response = mc.readResponseByte();
 			if (response != 0) {
 				System.out.println("Module " + address + " reponse: " + response);
 				System.out.println("\ti2c: "
 						+ Integer.toString(mc.checkI2C(), 16));
 				hasError = true;
 				lastError = System.currentTimeMillis();
 			} else {
 				hasError = false;
 			}
 		} catch (ModuleConnectionException e) {
 			hasError = true;
 			lastError = System.currentTimeMillis();
 		}
 	}
 }
