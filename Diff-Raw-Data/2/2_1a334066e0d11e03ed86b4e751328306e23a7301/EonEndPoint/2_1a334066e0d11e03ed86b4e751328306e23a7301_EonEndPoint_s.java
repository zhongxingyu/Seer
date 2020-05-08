 package com.robonobo.mina.external.node;
 
 import java.net.InetAddress;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.robonobo.common.util.TextUtil;
 import com.robonobo.core.api.proto.CoreApi.EndPoint;
 
 public abstract class EonEndPoint {
 	static final Pattern EP_PATTERN = Pattern.compile("^(\\w+?):(.*):(\\d+):(\\d+);(.*)$");
 	protected int udpPort;
 	protected int eonPort;
 	protected InetAddress address;
 	protected String url;
 
 	/** Does only a quick check against the protocol, doesn't check the url components */
 	public static boolean isEonUrl(String url) {
 		int colPos = url.indexOf(":");
 		if (colPos < 0)
 			return false;
 		String protocol = url.substring(0, colPos);
 		return (protocol.equals("seon") || protocol.equals("deon"));
 	}
 
 	public static EonEndPoint parse(String url) {
 		Matcher m = EP_PATTERN.matcher(url);
 		if (!m.matches())
 			throw new RuntimeException("Error in url " + url);
 		String protocol = m.group(1);
 		try {
 			InetAddress addr = InetAddress.getByName(m.group(2));
 			int udpPort = Integer.parseInt(m.group(3));
 			int eonPort = Integer.parseInt(m.group(4));
 			String[] opts = m.group(5).split(",");
 			if (protocol.equals("seon")) {
 				for (String opt : opts) {
 					if(opt.equals("nt"))
 						return new SeonNatTraversalEndPoint(addr, udpPort, eonPort);
 					throw new RuntimeException("Unknown eon option "+opt);
 				}
 				return new SeonEndPoint(addr, udpPort, eonPort);
 			} else if (protocol.equals("deon"))
 				return new DeonEndPoint(addr, udpPort, eonPort);
 		} catch (Exception ignore) {
 		}
 		throw new RuntimeException("Invalid eon url " + url);
 	}
 
 	EonEndPoint() {
 	}
 
 	public boolean equals(Object obj) {
 		if (obj instanceof EonEndPoint)
 			return hashCode() == obj.hashCode();
 		return false;
 	}
 
 	public int hashCode() {
 		return getClass().getName().hashCode() ^ url.hashCode();
 	}
 
 	public int getEonPort() {
 		return eonPort;
 	}
 
 	public int getUdpPort() {
 		return udpPort;
 	}
 
 	public InetAddress getAddress() {
 		return address;
 	}
 
 	public String getUrl() {
 		return url;
 	}
 
 	@Override
 	public String toString() {
 		return url;
 	}
 
 	public EndPoint toMsg() {
 		return EndPoint.newBuilder().setUrl(url).build();
 	}
 }
