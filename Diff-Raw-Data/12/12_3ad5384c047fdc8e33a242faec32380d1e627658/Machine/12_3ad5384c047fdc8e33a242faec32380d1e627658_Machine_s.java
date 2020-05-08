 package com.emn.fil.automaticdiscover.dto;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import com.emn.fil.automaticdiscover.dto.enums.OsType;
 
 public class Machine {
 
 	private IP ip;
 	private OsType osType;
 	private String hostname;
 
 	public Machine() {}
 	
 	public Machine(IP ip, OsType os) {
 		this.ip = ip;
 		this.osType = os;
 	}
 	
 	public Machine(IP ip, OsType os, String hostname) {
 		this.ip = ip;
 		this.osType = os;
 		this.hostname = hostname;
 	}
 	
 	public ArrayList<Object> toObject() {
		return (ArrayList<Object>) Arrays.asList((Object) ip.toString(), osType.toString());
 	}
 
 	public IP getIp() {
 		return ip;
 	}
 	public void setIp(IP ip) {
 		this.ip = ip;
 	}
 	public OsType getOsType() {
 		return osType;
 	}
 	public void setOsType(OsType osType) {
 		this.osType = osType;
 	}
 	public String getHostname() {
 		return hostname;
 	}
 	public void setHostname(String hostname) {
 		this.hostname = hostname;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((hostname == null) ? 0 : hostname.hashCode());
 		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
 		result = prime * result + ((osType == null) ? 0 : osType.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Machine other = (Machine) obj;
 		if (hostname == null) {
 			if (other.hostname != null)
 				return false;
 		} else if (!hostname.equals(other.hostname))
 			return false;
 		if (ip == null) {
 			if (other.ip != null)
 				return false;
 		} else if (!ip.equals(other.ip))
 			return false;
 		if (osType != other.osType)
 			return false;
 		return true;
 	}
 	
 	public String toString() {
 		return "Machine [osType=" + osType + ", ip=" + ip + ", hostname =" + hostname + "]\n";
 	}
 }
