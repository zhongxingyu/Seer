 /*
  * Claudia Project
  * http://claudia.morfeo-project.org
  *
  * (C) Copyright 2010 Telefonica Investigacion y Desarrollo
  * S.A.Unipersonal (Telefonica I+D)
  *
  * See CREDITS file for info about members and contributors.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the Affero GNU General Public License (AGPL) as 
  * published by the Free Software Foundation; either version 3 of the License, 
  * or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the Affero GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  *
  * If you want to use this software an plan to distribute a
  * proprietary application in any way, and you are not licensing and
  * distributing your source code under AGPL, you probably need to
  * purchase a commercial license of the product. Please contact
  * claudia-support@lists.morfeo-project.org for more information.
  */
 package com.telefonica.claudia.slm.lifecyclemanager;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.PostLoad;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import javax.persistence.Transient;
 
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 import org.dmtf.schemas.ovf.envelope._1.ContentType;
 import org.dmtf.schemas.ovf.envelope._1.ProductSectionType;
 import org.dmtf.schemas.ovf.envelope._1.VirtualSystemCollectionType;
 import org.dmtf.schemas.ovf.envelope._1.VirtualSystemType;
 import org.dmtf.schemas.ovf.envelope._1.ProductSectionType.Property;
 
 import com.abiquo.ovf.OVFEnvelopeUtils;
 import com.abiquo.ovf.exceptions.EmptyEnvelopeException;
 import com.abiquo.ovf.section.OVFProductUtils;
 import com.telefonica.claudia.slm.common.SMConfiguration;
 
 import edu.emory.mathcs.backport.java.util.Arrays;
 
 
 /**
  * Network range for making subnets. It stores a network address with its
  * mask; whenever a new network address is needed, the NetworkRange can 
  * calculate the size of the address based on the maximum number of hosts 
  * needed, and create a subnet in its range that does not conflict with previous 
  * leases (method getNetwork() ).
  * 
  * Aditionally, it offers static methods related to IP to String conversion. The
  * IP format is a 4 byte array (represented as a short array where only the lsb
  * part of the number). IPv4 support only.
  * 
  *
  */
 @Entity
 public class NetworkRange {
 
 	private static Logger logger = Logger.getLogger(NetworkRange.class); 
 
 	private static Logger monitoringLog = Logger.getLogger("Monitoring");
 
 	static {
 		Logger.getLogger("com.telefonica.claudia.slm.NetworkRange").setLevel(Level.INFO); 
 		Logger.getLogger("com.telefonica.claudia.slm.NetworkRange").addAppender(
 				new ConsoleAppender(new PatternLayout("%-5p [%t] %c{2}: %m%n"),
 				"System.out"));   	
 	}
 
 
 
 	@Id
 	@GeneratedValue
 	public long internalId;
 
 	protected short[] IP;
 	protected short[] mask;
 	protected String DNS;
 	protected String gateway;
 
 	/**
 	 * Map containing the actual leased Networks in the range (the key) and the
 	 * corresponding netmask.
 	 */
 	@Transient
 	private Map<String, short[]> subnetLeases = new HashMap<String, short[]>();
 	private String serializedSubnetLeases;
 
 	/**
 	 * Map containing the actual leased IPs for each network.
 	 */
 	@Transient
 	private Map<String, Set<String> > addressLeases = new HashMap<String, Set<String> >();
 	private String serializedAddressLeases;
 
 	protected boolean publicNetwork;
 
 	@PrePersist
 	@PreUpdate
 	public void serialize() {
 
 		StringBuffer sbSubnet =new StringBuffer();
 		StringBuffer sbAddress =new StringBuffer();
 
 		for (String subnet: subnetLeases.keySet()) {
 			sbSubnet.append(subnet).append("=");
 
 			for (short lease: subnetLeases.get(subnet)) {
 				sbSubnet.append(lease).append("/");
 			}
 
 			sbSubnet.append(";");
 		}
 
 		for (String address: addressLeases.keySet()) {
 			sbAddress.append(address).append("=");
 
 			for (String lease: addressLeases.get(address)) {
 				sbAddress.append(lease).append("/");
 			}
 
 			sbAddress.append(";");
 		}
 
 		this.serializedSubnetLeases=sbSubnet.toString();
 		this.serializedAddressLeases=sbAddress.toString();
 	}
 
 	@SuppressWarnings("unchecked")
 	@PostLoad
 	public void deserialize() {
 
 		String[] subnets;
 		String[] addresses;
 
 		if (serializedSubnetLeases.equals(""))
 			subnets = new String[0];
 		else
 			subnets = serializedSubnetLeases.split(";");
 
 		if (serializedSubnetLeases.equals(""))
 			addresses = new String[0];
 		else
 			addresses = serializedAddressLeases.split(";");
 
 		for (String subnet: subnets) {
 			String name = subnet.split("=")[0];
 			String[] contents = subnet.split("=")[1].split("/");
 			short [] address = new short[4];
 			for (int i=0; i< 4; i++)
 				address[i] = Short.parseShort(contents[i]);
 
 			subnetLeases.put(name, address);
 		}
 
 		for (String address: addresses) {
 			String[] components = address.split("=");
 			String name = components[0];
 
 			if (components.length >1) {
 				String[] contents = components[1].split("/");
 				HashSet<String> lease = new HashSet<String>();
 
 				lease.addAll(Arrays.asList(contents));
 				addressLeases.put(name, lease);
 			} else 
 				addressLeases.put(name, new HashSet<String>());
 		}
 	}
 
 	/**
 	 * Transform a string representation of an IPv4 address into an array
 	 * of short (only the lesser byte is used in each short, beware the use
 	 * boolean operators).
 	 * 
 	 * @param ip
 	 * 		A string representing an IP address in the format x.y.z.t being
 	 * 		x, y, z and t in the range [0-255].
 	 * 
 	 * @return
 	 * 		A array of short with the same address in binary representation.
 	 * 
 	 * @throws IllegalArgumentException
 	 * 		If the ip address is not in the IPv4 format.
 	 */
 	public static short[] string2Ip(String ip) {
 
 		if (!ip.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) 
 			throw new IllegalArgumentException("Wrong IPv4 format. Expected example: 192.168.0.0 Got: " + ip);
 
 		String[] ipBytes = ip.split("\\.");
 
 		short[] result = new short[4];
 		for (int i=0; i < 4; i++) {
 			try {
 				result[i] = Short.parseShort(ipBytes[i]);
 				if (result[i]>255) throw new NumberFormatException("Should be in the range [0-255]."); 
 			} catch (NumberFormatException nfe) {
 				throw new IllegalArgumentException("Number out of bounds. Bytes should be on the range 0-255.");
 			}
 		}
 
 		return result;
 	}
 
 	public static String ip2String(short[] ip) {
 		StringBuffer result = new StringBuffer();
 		for (int i=0; i < 4; i++) {
 			result.append(String.valueOf(ip[i]));
 
 			if (i!=3) result.append(".");
 		}
 		return result.toString();	
 	}
 
 	public String getIP() {
 		return ip2String(IP);
 	}
 
 	public void setIP(String ip) {
 		this.IP = string2Ip(ip);
 	}
 
 	public String getMask() {
 		return ip2String(mask);
 	}
 
 	public void setMask(String mask) {
 		this.mask = string2Ip(mask);
 	}	
 
 	public String getDNS() {
 		return DNS;
 	}
 
 	public void setDNS(String dns) {
 		this.DNS = dns;
 	}
 
 	public String getGateway() {
 		return gateway;
 	}
 
 	public void setGateway(String gw) {
 		this.gateway = gw;
 	}
 
 	/**
 	 * Calculate the minimum number of bits needed to encode the given number of hosts. 
 	 * Two addresses are added to the number of hosts, representing the broadcast 
 	 * address and the network one.
 	 * 
 	 * @param size
 	 * @return
 	 */
 	public static int bitNumber(long size) {
 		return (int) Math.ceil(Math.log(size+2)/Math.log(2));
 	} 
 
 	public static short[] numberToByteArray(int number) {
 		short[] result = new short[4];
 		result[0] = (short) ((number >> 24) & 255); 
 		result[1] = (short) (((number & 16711680) >> 16) & 255); 
 		result[2] = (short) (((number & 65280) >> 8) & 255); 
 		result[3] = (short) (((number & 255)) & 255);
 		return result;
 	}
 
 	public static int byteArrayToNumber(short[] array) {
 
 		int result = 0;
 
 		for (short i=0; i < 4; i++)
 			result += array[i] << (3-i)*8;
 
 		return result;
 	}
 
 	/**
 	 * Calculate the appropiate number of bits to assign for the new network to
 	 * meet the size requirements.
 	 * 
 	 * 
 	 * @param size
 	 * @return
 	 * 		The method returns an array containing the IP Address assigned for the
 	 * 		network in its first position and the network mask in the second one.
 	 * 
 	 */
 	public String[] getNetwork (long size) {
 
 		int bitNumber = bitNumber(size);
 		int maskNumber = byteArrayToNumber(mask);
 		int ipNumber = byteArrayToNumber(IP);
 
 		if (ip2String(mask).equals("255.255.255.255")) {
 
 			if (subnetLeases.containsKey(ip2String(IP))) {
 				return null;
 			} else {
 				return new String[] {ip2String(IP), ip2String(mask)};
 			}
 
 		} else {
 			int lastMaskBit = Integer.toBinaryString(maskNumber).indexOf('0');
 			int solutionSize = (int) Math.pow(2, 32 - (bitNumber+lastMaskBit)); 
 			short[] maskAssigned = numberToByteArray(~0 << bitNumber);
 
 			for (int i=1; i < solutionSize; i++) {
 
 				short[] ipSolution = numberToByteArray(((i << bitNumber) & ~maskNumber) ^ ipNumber); 
 
 				if (available(ipSolution, maskAssigned)) {
 					subnetLeases.put(ip2String(ipSolution), maskAssigned);
 					addressLeases.put(ip2String(ipSolution), new HashSet<String>());
 					return new String[] {ip2String(ipSolution), ip2String(maskAssigned)};
 				}
 			}			
 		}
 
 		return null;
 	}
 
 	/**
 	 * Gets an IP from the selected Network Address, if theres is one free. If there
 	 * aren't any IPs left, return null
 	 * 
 	 * @param networkAddress
 	 * 		IP Address of the target network.
 	 *  
 	 * @return
 	 * 		A String representation of the leased address or null if there weren't any.
 	 */
 	public  String getNetworkAddress (String networkAddress) {
 
 
 		int maskNumber = byteArrayToNumber(subnetLeases.get(networkAddress));
 		int ipNumber = byteArrayToNumber(string2Ip(networkAddress));
 
 		if (ip2String(subnetLeases.get(networkAddress)).equals("255.255.255.255")) {
 
 			if (subnetLeases.containsKey(networkAddress)) {
 				return null;
 			} else {
 				return networkAddress;
 			}
 
 		} else {
 			int lastMaskBit = Integer.toBinaryString(maskNumber).indexOf('0');
 			int solutionSize = (int) Math.pow(2, 32 - lastMaskBit);
 
 			Set<String> actualLeases = addressLeases.get(networkAddress);
 
 			for (int i=1; i < solutionSize; i++) {
 
 				String ipSolution = ip2String(numberToByteArray((i  & ~maskNumber) ^ ipNumber)); 
 
 				if (!actualLeases.contains(ipSolution)) {
 					actualLeases.add(ipSolution);
 					return ipSolution;
 				}
 
 			}
 			return null;
 		}
 	}
 
 	public String getNetworkAddress (String networkAddress, String staticIpProp) {
		return null;
 	}
 
 
 	private boolean available(short[] ipAssigned, short[] maskAssigned) {
 
 		leaseLoop: for (String leaseStr: subnetLeases.keySet()) {
 			short[] lease = string2Ip(leaseStr);
 
 			for (short i=0; i < 4; i++) {
 
 				short maskByte = subnetLeases.get(leaseStr)[i];
 
 				// Choose the bigger mask.
 				if (maskByte > maskAssigned[i]) {
 					maskByte = maskAssigned[i];
 				}
 
 				if ((maskByte&lease[i]) != (maskByte&ipAssigned[i])) continue leaseLoop;
 			}
 
 			// If the whole mask have been tested without finding a difference, the proposed network address
 			// is a subnet of the current lease or viceversa, so it mustn't be accepted.
 			return false;
 		}
 
 	return true;
 	}
 
 	/**
 	 * Releases the subnet specified. Releasing a subnet also release all
 	 * the given IP addresses in this subnet.
 	 * 
 	 * @param ip
 	 * 		IP Address of the subnet to be released.
 	 */
 	public void releaseSubnet(String ip) {
 		subnetLeases.remove(ip);
 		addressLeases.remove(ip);
 	}
 
 	/**
 	 * Release the selected ip from the selected subnet.
 	 * 
 	 * @param ip
 	 * 		IP address to release.
 	 * 
 	 * @param subnet
 	 * 		Subnet associated to the address.
 	 * 
 	 */
 	public void releaseAddress(String ip, String subnet) {
 		addressLeases.get(subnet).remove(ip);
 	}
 
 
 	/**
 	 * Marks the network range as a public IP network range, or as a private one.
 	 * All the subnets obtained will be of the same type of the range (it makes sense).
 	 * 
 	 * @param pub
 	 * 
 	 * 		True if the network is on a public IP range; false if it falls into the 
 	 * 		private network ranges.
 	 */
 	public void setPublic(boolean pub) {
 		this.publicNetwork = pub;
 	}
 
 	/**
 	 * Indicates whether the Network Range belongs to the public IP network ranges or to
 	 * the private ones.
 	 * 
 	 * @return
 	 * 		True if the network is on a public IP range; false if it falls into the 
 	 * 		private network ranges.
 	 */
 	public boolean isPublic() {
 		return publicNetwork;
 	}
 
 	/**
 	 * Check whether the network address passed as a parameter is one of the leased
 	 * subnets of the Network Range.
 	 * 
 	 * @param network
 	 * 		Network address to be checked.
 	 * 		
 	 * @return
 	 * 		True if the network address is a subnet lease of this network range. False
 	 * 		otherwise.
 	 */
 	public boolean isNetworkInRange(String network) {
 		return subnetLeases.containsKey(network);
 	}
 
 	public String toString() {
 
 		StringBuffer definition= new StringBuffer();
 
 		definition.append("<Range Address=\"" + this.getIP() + "\" >\n");
 
 		for (String ipSubnet: subnetLeases.keySet()) {
 			definition.append("<Subnet IP=\"" + ipSubnet + "\" mask=\"" + ip2String(subnetLeases.get(ipSubnet)) + "\" />\n");
 		}
 
 		definition.append("</Range>\n");
 
 		return definition.toString();
 	}
 
 	public Set<String> getSubnets() {
 		return subnetLeases.keySet();
 	}
 }
