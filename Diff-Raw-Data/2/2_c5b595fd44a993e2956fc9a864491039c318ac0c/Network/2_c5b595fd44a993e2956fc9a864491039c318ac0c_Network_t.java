 package de.lankenau.rubenwrite;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 
 public class Network {
 	public static void start() {		
 		Process p;
 		try {
 			p = Runtime.getRuntime().exec("/sbin/iptables -D OUTPUT 1");			
 			readStream(p);
 			p.waitFor();
 			System.out.println("Exit valud of iptables: "+ p.exitValue());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}	    		
 	}
 
 	private static void readStream(Process p) throws IOException {
 		InputStreamReader reader = new InputStreamReader(p.getErrorStream());
 		int c;
 		while ((c = reader.read()) != -1) {
 			System.out.print((char)c);
 		}
 	}
 
 	public static void stop() {		
 		Process p;
 		try {
			p = Runtime.getRuntime().exec("/sbin/iptables -A OUTPUT -j REJECT");
 			readStream(p);
 			p.waitFor();
 			System.out.println("Exit valud of iptables: "+ p.exitValue());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}	    		
 	}
 
 }
