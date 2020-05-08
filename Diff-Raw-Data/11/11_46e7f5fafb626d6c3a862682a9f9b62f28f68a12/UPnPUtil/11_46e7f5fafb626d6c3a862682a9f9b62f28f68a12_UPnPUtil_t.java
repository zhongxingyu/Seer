 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
 * limitations under the License.
  */
 package net.rptools.maptool.util;
 
 import java.io.IOException;
 import java.net.InetAddress;
 
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.ui.io.ResolveLocalHostname;
 import net.sbbi.upnp.impls.InternetGatewayDevice;
 import net.sbbi.upnp.messages.ActionResponse;
 import net.sbbi.upnp.messages.UPNPResponseException;
 
 /**
  * @author Phil Wright
  */
 public class UPnPUtil {
 	private static int discoveryTimeout = 5000; // Should be made a preference setting
 	private static InternetGatewayDevice[] IGDs;

 	public static boolean findIGDs() {
 		try {
 			IGDs = InternetGatewayDevice.getDevices(discoveryTimeout);
 		} catch (IOException ex) {
 			// some IO Exception occurred during communication with device
			MapTool.showError("While searching for gateway devices", ex);
 		}

 		if (IGDs != null) {
 			System.out.println("UPnP:  Found device: "	+ IGDs[0].getIGDRootDevice().getModelName());
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public static boolean openPort(int port) {
 		boolean mapped = false;
 		String localHostIP; 
 		InternetGatewayDevice ourIGD;
 		
 		if (IGDs == null) {
 			findIGDs();
 		}
 		
 		if (IGDs != null) {
 			// TODO Allow the option of selecting one IGD from a list
 			ourIGD = IGDs[0];
 		} else {
 			MapTool.showError("UPnP Error - No Internet Gateway Devices found.<br><br>UPnP port mapping will not be available.");
 			return false;
 		}
 		
 		try {
 			InetAddress rptools = InetAddress.getByName("www.rptools.net");
 			InetAddress localAddy = ResolveLocalHostname.getLocalHost(rptools);
 			localHostIP = localAddy.getHostAddress();
 		
 			mapped = ourIGD.addPortMapping(
 						"MapTool", null,
 						port, port, localHostIP, 0, "TCP");
 				
 		} catch (UPNPResponseException respEx) {
 			// oops the IGD did not like something !!
 			MapTool.showError("While configuring UPnP (1)", respEx);
 		} catch (Exception e) {
 			MapTool.showError("While configuring UPnP (2)", e);
 		}
 		
 		if (mapped) {
 			System.out.println("UPnP: Port " + port + " mapped");
 			return true;
 		} else {
 			MapTool.showError("UPnP: Port Mapping Failed");
 			return false;
 		}
 	}
 	
 	public static boolean closePort(int port) {
 		try{
 	
 			if (IGDs != null) {
 				// TODO If openPort() is modified to allow selection of one IGD from a list,
 				// this code will need to be updated as well.
 
 				// using the first device found
 				InternetGatewayDevice testIGD = IGDs[0];
 				
 				// See if there is a mapping before we try to get rid of it
 				ActionResponse actResp = testIGD.getSpecificPortMappingEntry(null, port, "TCP");
 				
 				if (actResp != null) {
 					// Gonna just assume the mapping belongs to us. as I can't make 
 					// One of these days should figure out the  action response to verify it
 					boolean unmapped = testIGD.deletePortMapping(null, port, "TCP");
 					if (unmapped) {
 						System.out.println("UPnP: Port unmapped");
 					} else {
 						System.out.println("UPnP: Failed to unmap port.");
 					}
 				}
 			}
 			
 		} catch (Exception e) {
 			MapTool.showError("While closing UPnP port", e);
 			return false;
 		}
 		return true;
 	}
 
 	public static int getDiscoveryTimeout() {
 		return discoveryTimeout;
 	}
 
 	public static void setDiscoveryTimeout(int discoveryTimeout) {
 		UPnPUtil.discoveryTimeout = discoveryTimeout;
 	}
 }
