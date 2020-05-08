 package com.isaacjg.darklight.issues;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import com.ijg.darklight.sdk.core.Issue;
 
 /*
  * FirewallIssue - An Issue for Darklight Nova Core
  * Copyright  2013 Isaac Grant
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * FirewallIssue is an Issue for Darklight Nova Core that checks if
  * the Windows Firewall has been enabled
  * 
  * @author Isaac Grant
  */
 
 public class FirewallIssue extends Issue {
 
	public FirewallIssue() {
 		super("Firewall", "The Windows Firewall has been enabled");
 	}
 
 	@Override
 	public boolean isFixed() {
 		try {
 			Process p = Runtime.getRuntime().exec("cmd.exe /c netsh advfirewall show publicprofile state");
 			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
 			String line;
 			while ((line = br.readLine()) != null) {
 				if (line.contains("ON")) {
 					br.close();
 					return true;
 				}
 			}
 			br.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 }
