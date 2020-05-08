 package com.dropoutdesign.ddf;
 
 import com.dropoutdesign.ddf.config.*;
 import com.dropoutdesign.ddf.module.*;
 
 import java.util.*;
 import java.io.*;
 import java.net.InetAddress;
 import java.awt.Rectangle;
 
 public class LocalFloor extends DanceFloor {
 	
 	private List<Module> modules;
 	private int numConnectedModules;
 	
 	private int width;
 	private int height;
 	
 	private int frameRate;
 	
 	private List<InetAddress> ipWhitelist;
 	
 	public LocalFloor(String confFile) throws IOException {
 		
 		this(DanceFloorConfig.readAll(confFile));
 	}
 	
 	public LocalFloor(DanceFloorConfig config) {
 		
 		frameRate = config.maxfps;
 		ipWhitelist = config.getWhitelistAddresses();
 		
 		modules = new ArrayList<Module>(config.modules.size());
 		for (ModuleConfig mc : config.modules) {
 			Module m = new Module(mc);
 		
 			Rectangle bounds = m.getBounds();
 			int x = bounds.x + bounds.width;
 			if (x > width)
 				width = x;
 			int y = bounds.y + bounds.height;
 			if (y > height)
 				height = y;
 			
 			modules.add(m);
 		}
 		System.out.println("Initialized floor: " + width + "x" + height);
 	}
 	
 	public void connect() throws ModuleIOException {
 		if (isConnected()) {
 			return;
 		}
 		for (Module m : modules) {
 			try {
 				m.connect();
 				numConnectedModules++;
 			} catch (Exception e) {
 				System.out.println("Failed to connect module: " + e);
 			}
 		}
 		if (numConnectedModules == 0) {
 			throw new ModuleIOException("Unable to connect any modules.");
 		}
 	}
 	
 	public void disconnect() {
 		for (Module m : modules) {
 			m.disconnect();
 		}
 		numConnectedModules = 0;
 	}
 	
 	public boolean isConnected() {
 		return (numConnectedModules > 0);
 	}
 
 	public int getWidth() {
 		return width;
 	}
 	
 	public int getHeight() {
 		return height;
 	}
 	
 	public List getModules() {
 		return modules;
 	}
 
 	public int getFramerate() {
 		return frameRate;
 	}
 	
 	public void drawFrame(byte frame[]) throws ModuleIOException {
 		for (Module m : modules) {
			if (m.isConnected()) {
				m.writeFrame(frame);
			}
 		}
 	}
 }
