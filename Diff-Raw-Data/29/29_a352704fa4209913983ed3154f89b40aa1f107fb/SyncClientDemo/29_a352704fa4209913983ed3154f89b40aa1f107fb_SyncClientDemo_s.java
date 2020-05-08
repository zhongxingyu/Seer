 package com.flat502.rox.demo;
 
 import java.net.URL;
 
 import com.flat502.rox.client.XmlRpcClient;
 
 /**
  * A demo synchronous client illustrating the {@link com.flat502.rox.client.XmlRpcClient#execute(String, Object[], Class)}
  * method.
  */
 public class SyncClientDemo {
 	/**
 	 * Call each of the exposed methods on the remote demo server.
 	 * @param args
 	 * 	A list of parameters. Only the first is used and if
 	 * 	present must be the URL of the remote server. This 
 	 * 	defaults to <code>http://localhost:8080/</code> if
 	 * 	not specified.
 	 */
 	public static void main(String[] args) {
 		try {
 			String url = "http://localhost:8080/";
 
 			if (args != null && args.length > 0) {
 				url = args[0];
 			}
 			System.out.println("Connecting to " + url);
 
 			XmlRpcClient client = new XmlRpcClient(new URL(url));
 			TimeInfo rsp = (TimeInfo)client.execute("example.getDate", null, TimeInfo.class);
 			System.out.println("getDate() returned with");
 			System.out.println("   today is "+rsp.today);
 			System.out.println("   info: "+rsp.info);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
