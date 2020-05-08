 package net.floodlightcontroller.hand;
 
 import java.util.ArrayList;
 
 
 /**
  * 
  * @author wallnerryan
  *
  */
 
 public class HANDGangliaHost implements Comparable<HANDGangliaHost>{
 	
 	public long hostId;
 	
 	public String cluster; //Translates to the cluster name used in RRDFetch 
 	public String hostName; //Translates the host name used for the RRDFetch (Required)
 	public String domain;	//domain the host is in.(Optional)
 	public int ipAddress; //can use IPv4.toIPv4Address(String add) (Required)
 	public long macAddress; //can use MACAddress.valueOf(String MACadd)
 	public ArrayList<Long> firstHops;	//list of Swutch IDs for first hop OF switch.
 	public long timeAdded;
 	
 	public HANDGangliaHost(){
 		
 		this.hostId = 0;
 		this.cluster = null;
 		this.hostName = null;
		this.domain = ""; //change so we can add a host without a domain.
 		this.ipAddress = 0;
 		this.macAddress = 0;
 		this.firstHops = new ArrayList<Long>();
 		this.timeAdded = this.getCurrentTime();
 	}
 	
 	/**
 	 * Generate UID
 	 * @return
 	 */
 	public int genUniqueId() {
 		int uid = this.hashCode();
 		if (uid <= 0 ){
 			uid = Math.abs(uid);
 			uid = uid * 1553;
 		}
 		return uid;
 	}
 	
 	/**
 	 * Get current time in seconds since Epoch.
 	 * @return
 	 */
 	public long getCurrentTime(){
 		long time = System.currentTimeMillis() / 1000l;
 		return time;
 				
 	}
 	
 	/**
 	 * Compare to another Host
 	 * @param host
 	 * @return
 	 */
 	public boolean isSameAs(HANDGangliaHost host) {
 		boolean isSame;
 		if (this.cluster == host.cluster
 				&& this.hostName == host.hostName){
 			isSame = true;
 		}else if( this.ipAddress == host.ipAddress){
 			isSame = true;
 		}else{
 			isSame=false;
 			}
 		
 		return isSame;
 	}
 	
 	/**
 	 * Hash Code
 	 * @return
 	 */
 	@Override
 	public int hashCode(){
 		final int primeNum = 2521;
 		int result = super.hashCode();
 		result = primeNum * result + cluster.hashCode();
 		result = primeNum * result + hostName.hashCode();
 		result = primeNum * result + ipAddress;
 		result = primeNum * result + (int) macAddress;
 		return result;
 		
 	}
 
 	@Override
 	public int compareTo(HANDGangliaHost arg0) {
 		
 		
 		// TODO compare the hosts to another
 		
 		
 		return 0;
 	}
 
 }
