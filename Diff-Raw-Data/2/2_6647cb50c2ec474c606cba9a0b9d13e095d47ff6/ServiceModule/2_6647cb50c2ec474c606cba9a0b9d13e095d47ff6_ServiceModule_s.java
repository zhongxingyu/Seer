 package com.ijg.darklightnova.modules;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import com.ijg.darklightnova.core.Issue;
 import com.ijg.darklightnova.core.ScoreModule;
 
 public class ServiceModule extends ScoreModule {
 
 	private Issue telnetEnabled = new Issue("Telnet Disabled", "The Telnet service has been removed or disabled.");
 	private Issue updatesEnabled = new Issue("Windows Update Service started", "The Windows Update service is now online.");
 	
 	public ServiceModule() {
 		issues.add(telnetEnabled);
 	}
 	
 	private boolean isServiceOperational(String service) {
 		try {
 			Process p;
 			p = Runtime.getRuntime().exec("cmd.exe /c sc query " + service);
 			BufferedReader br = new BufferedReader(new InputStreamReader(
 					p.getInputStream()));
 			String line;
 			while ((line = br.readLine()) != null) {
				if (line.contains("1060") || line.contains("1058")) {
 					return false;
 				}
 			}
 			return true;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return true;
 		}
 	}
 	
 	@Override
 	public ArrayList<Issue> check() {
 		if (!isServiceOperational("TlntSvr")) {
 			add(telnetEnabled);
 		} else {
 			remove(telnetEnabled);
 		}
 		
 		if (!isServiceOperational("wuauserv")) {
 			add(updatesEnabled);
 		} else {
 			remove(updatesEnabled);
 		}
 		return issues;
 	}
 
 }
