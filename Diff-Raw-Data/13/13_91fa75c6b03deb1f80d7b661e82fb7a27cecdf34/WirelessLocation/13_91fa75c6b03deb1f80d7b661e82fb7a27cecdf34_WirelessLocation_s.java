 package com.example.simplevrcontroller.networking.location;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.TreeMap;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import android.net.wifi.ScanResult;
 
 import com.example.simplevrcontroller.cave.Cave;
 import com.example.simplevrcontroller.networking.NetworkAverager.AveragedNetworkInfo;
 import com.example.simplevrcontroller.networking.NetworkManager;
 
 public class WirelessLocation {
 	
 	public static final char COLON_PLACEHOLDER = '_';
 	
 	public enum AccuracyThreshold {
 		STRONG(.75, 300),
 		AVERAGE(.60, 500),
 		WEAK(.50, 1000);
 		
 		public final double amount_present;
 		public final int max_deviation;
 		
 		AccuracyThreshold(double amount_present, int max_dev){
 			this.amount_present = amount_present;
 			max_deviation = max_dev;
 		}
 		
 		
 	}
 
 	private String name;
 	private TreeMap<String, Integer> bssids;
 	private String strength;
 	private Cave cave;
 	private AccuracyThreshold lastLocate;
 
 	public WirelessLocation(String name){
 		this.name = name;
 		
 		bssids = new TreeMap<String, Integer>();
 	}
 	
 	public WirelessLocation(Element n) {
 		
 		bssids = new TreeMap<String, Integer>();
 		
 		NodeList nets = n.getElementsByTagName("network");
 		
 		for (int y = 0; y < nets.getLength(); y++){
 			Element net = (Element) nets.item(y);
 			try {
 				bssids.put(net.getAttribute("bssid"), Integer.parseInt(net.getAttribute("strength")));
 			} catch (NumberFormatException e){
 				e.printStackTrace();
 			}
 		}
 		
 	}
 
 	public void setLocation(NetworkManager networker){
 		bssids.clear();
 		
 		for(int y = 0; y < 20; y++){
 			ArrayList<ScanResult> nets = networker.getFreshOrderedNetworks(WirelessLocator.WIRELESS_THRESHOLD);
 			
 			for(ScanResult res : nets){
 				if(!bssids.containsKey(res.BSSID))
 					bssids.put(res.BSSID, res.level);
 				else
 					bssids.put(res.BSSID, (res.level + bssids.get(res.BSSID))/2);
 			}
 			
 		}
 		
 	}
 	
 	/**
 	 * Returns true if the current location matches this WirelessLocation
 	 * @return
 	 */
 	public AccuracyThreshold checkLocation(NetworkManager networker){
 		
 		if(bssids.size() <= 0)
 			return null;
 		
 		List<AveragedNetworkInfo> nets = networker.getNetworkAverages(WirelessLocator.WIRELESS_THRESHOLD);
 		
 		if(nets.size() <= 0)
 			return null;
 		
 		int dev = 0, cnt = 0;
 		for(AveragedNetworkInfo net : nets){
 			Integer i = bssids.get(net.bssid);
 			int av = net.getAveragedLevel();
 			if(i != null){
 				dev += Math.abs(i * i - av * av);
				
 			} else {
 				//dev -= WirelessLocator.WIRELESS_THRESHOLD; 
 			}
 			
			cnt++;
 			
 		}
 		
 		//TODO Test
 		//dev = (int) Math.sqrt(dev / cnt);
 		dev = (dev / cnt);
 		
 		for(AccuracyThreshold acc : AccuracyThreshold.values()){
 			if(dev <= acc.max_deviation){
 				strength = acc.name() + " " + dev + "  " + acc.max_deviation;
 				lastLocate = acc;
 				return acc;
 			}
 		}
 		
 		lastLocate = null;
 		
 		return null;
 	}
 	
 	public AccuracyThreshold getLastLocateThreashold(){
 		return lastLocate;
 	}
 	
 	/**
 	 * Checks if the given BSSID is recognized by this WirelessLocation
 	 * @param bssid The BSSID to check
 	 * @return True if it is recognized, false otherwise
 	 */
 	public boolean checkBSSID(String bssid) {
 		return bssids.containsKey(bssid);
 	}
 	
 	public void writeToElement(Element e) {
 		
 		for(String s : bssids.keySet()){
 			Element a = e.getOwnerDocument().createElement("network");
 			String mod = s.replace(':', COLON_PLACEHOLDER);
 			a.setAttribute("bssid", s);
 			a.setAttribute("strength", bssids.get(s).toString());
 			e.appendChild(a);
 		}
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	/**
 	 * Gets the strength of the last successful locate
 	 * @return A String or null if there is no recent successful locate
 	 */
 	public String getStrength(){
 		return strength;
 	}
 	
 	/**
 	 * Sets the Cave this location refers to.
 	 * @param cave The cave
 	 */
 	public void setCave(Cave cave) {
 		this.cave = cave;
 	}
 	
 	/**
 	 * Gets the Cave this WirelessLocation refers to.
 	 * @return
 	 */
 	public Cave getCave(){
 		return cave;
 	}
 
 }
