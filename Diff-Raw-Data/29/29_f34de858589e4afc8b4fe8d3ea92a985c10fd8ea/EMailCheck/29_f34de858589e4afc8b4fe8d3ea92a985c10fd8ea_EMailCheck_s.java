 package no.ntnu.idi.tdt4237.h2012.g5.lut;
 
 import java.net.IDN;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Hashtable;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.naming.NamingException;
 import javax.naming.directory.Attribute;
 import javax.naming.directory.Attributes;
 import javax.naming.directory.DirContext;
 import javax.naming.directory.InitialDirContext;
 
 public class EMailCheck {
 
	private static EMailCheck instance;
 	
 	protected HashMap<String,List<InetAddress>> validHosts = new HashMap<String,List<InetAddress>>();
 	
 	protected EMailCheck() {
 		// TODO Auto-generated constructor stub
 	}
 
 	public boolean check(String emailAddress) {
 		try {
 			String[] parts = emailAddress.split("@");
 			if (parts.length != 2)
 				return false;
 			if (!"".equals(parts[0].replaceFirst("([A-Za-z0-9!#\\$%&'\\*\\+\\-/=\\?\\^_`\\{|\\}~]+)", "")))
 				return false; // All valid e-mail characters
 			if (emailAddress.charAt(0) == '.' || emailAddress.charAt(emailAddress.length()-1) == '.')
 				return false; // Hostnames cannot start or end with a dot
 			String localPart = parts[0];
 			String host = IDN.toASCII(parts[1]);
 			if (localPart.charAt(localPart.length()-1) == '.')
 				return false; // The addr-spec part cannot start or end with a dot
 			if (!localPart.equals(localPart.replaceFirst("\\.\\.", "")))
 				return false; // The addr-spec part cannot contain two successive dots
 			if (!"".equals(parts[1].replaceFirst("([A-Za-z0-9\\-\\.]+)", "")))
 				return false;  // Hostnames can contain letters, numbers, dashes and dots
 			if (host.charAt(0) == '.')
 				return false; // Hostnames cannot start or end with a dot
 			if (!host.equals(host.replaceFirst("\\.\\.", "")))
 				return false; // Hostnames cannot contain two successive dots
 			if (!localPart.equals(localPart.replaceFirst("\\.([0-9]+)\\.", "")))
 				return false; // Reject direct-to-IPv4 mailing (IPv6 is already rejected because : is not allowed)
 			if (validHosts.containsKey(host))
 				return validHosts.get(host).size() > 0;
 			// No locking because this can make a DoS easier
 			// Doing the check multiple times is not a very big problem
 			return doLookup(host).size() > 0;
 		} catch (Exception e) {
 			// On any failure, refuse
 			return false;
 		}
 	}
 
 	protected List<InetAddress> doLookup(String hostName) throws NamingException {
 		// Code borrowed from http://www.rgagnon.com/javadetails/java-0452.html
 		Hashtable<String,String> env = new Hashtable<String,String>();
 		env.put("java.naming.factory.initial",
 				"com.sun.jndi.dns.DnsContextFactory");
 		DirContext ictx = new InitialDirContext(env);
 		Attributes attrs = 
 				ictx.getAttributes(hostName, new String[]{"MX"});
 		Attribute attr = attrs.get("MX");
 		if( attr == null ) return(Collections.emptyList());
 		ArrayList<InetAddress> result = new ArrayList<InetAddress>(attr.size());
 		for(int i=0;i<attr.size();i++)
 			try {
				result.add(InetAddress.getByName(attr.get(i).toString()));
			} catch (UnknownHostException e) {/* do nothing */}
 		validHosts.put(hostName, result);
 		return(result);
 	}
 	
 	public static EMailCheck getInstance() {
		if (instance != null)
			return instance;
		synchronized(instance) {
			if (instance == null)
				return instance = new EMailCheck();
			return instance;
 		}
 	}
 
 }
