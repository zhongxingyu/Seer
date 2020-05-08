 package org.mozeq.Trac;
 
 import java.net.MalformedURLException;
 
 import org.apache.xmlrpc.XmlRpcException;
 
 public class jTracTest {
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		String PROJECT = "someproject";
 		String USERNAME = "username";
 		String PASSWORD = "password";
		String TRAC_URL = "http://tracurl";
 
 		TracProxy trac = new TracProxy(TRAC_URL, PROJECT);
 		try {
 			trac.connect(USERNAME, PASSWORD);
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (XmlRpcException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		Ticket t = null;
 		try {
 			t = trac.getTicket(1);
 		} catch (XmlRpcException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		//System.out.println(t);
 		System.out.println("[" + t.getComponent() +"] " + t.getSummary());
 	}
 }
