 /**
  * 
  */
 package com.manning.sbia.ch13.batch;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.sql.SQLException;
 
 import org.h2.tools.Server;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 
 /**
  * Launching the monitoring.
  * @author templth
  *
  */
 public class LaunchMonitoring {
 
 	private static Server startH2Server() throws SQLException {
 		// start the TCP Server
 		Server server = Server.createTcpServer(new String[0]).start();
 		
 		return server;
 	}
 	
 	private static void stopH2Server(Server server) {
 		// stop the TCP Server
 		server.stop();
 	}
 	
 	public static void main(String[] args) {
 		Server server = null;
 		ClassPathXmlApplicationContext context = null;
 		try {
 			server = startH2Server();
 			context = new ClassPathXmlApplicationContext(new String[] {
 					"/com/manning/sbia/ch13/root-database-context.xml",
 					"/com/manning/sbia/ch13/batch-infrastructure-context.xml",
 					"/com/manning/sbia/ch13/batch-monitoring-context.xml"
 			});
 			
 			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
 			while (true) {
 				String input = reader.readLine();
 				if( "exit".equals(input)) {
 					break;
 				}
 			}
 		} catch(Exception ex) {
 			ex.printStackTrace();
 		} finally {
 			if (server!=null) {
 				stopH2Server(server);
 			}
 		}
 	}
 	
 }
