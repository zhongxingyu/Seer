 package nl.q42.javahueapi.models;
 
 import java.util.Map;
 
 public class Config {
 	public String name;
 	
 	public String mac;
 	
 	public boolean dhcp;
 	
 	public String ipaddress;
 	
 	public String netmask;
 	
 	public String gateway;
 	
 	public String proxyaddress;
 	
 	public int proxyport;
 	
 	public String UTC;
 	
	public Map<String, WhitelistItem> whitelist;
	
 	public String swversion;
 	
 	public SWUpdate swupdate;
 	
 	public boolean linkbutton;
 	
 	public boolean portalservices;
 }
