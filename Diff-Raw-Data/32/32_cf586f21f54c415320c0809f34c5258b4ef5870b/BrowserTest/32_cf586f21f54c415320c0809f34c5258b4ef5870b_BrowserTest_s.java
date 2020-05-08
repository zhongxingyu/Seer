 package com.slugsource.steam.serverbrowser;
 
 import com.slugsource.steam.servers.KillingFloorServer;
 import com.slugsource.steam.servers.ServerAddress;
 import com.slugsource.steam.servers.query.KillingFloorGameQuery;
 import com.slugsource.steam.servers.query.KillingFloorPlayerQuery;
 import com.slugsource.steam.servers.query.KillingFloorServerQuery;
 import com.slugsource.steam.servers.query.MasterServerQuery;
 import com.slugsource.steam.servers.query.SourceServerQuery;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.Executor;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import org.apache.commons.lang3.StringUtils;
 
 /**
  *
  * @author Nathan Fearnley
  */
 public class BrowserTest
 {
 
     public static void main(String[] args)
     {
         try
         {
             sendMasterRequest(InetAddress.getByName("208.64.200.52"), 27011);
//            sendRequest(InetAddress.getByName("68.232.176.139"), 28852);
//            sendRequest(InetAddress.getByName("localhost"), 28852);
         } catch (Exception ex)
         {
             ex.printStackTrace();
         }
     }
 
     private static void sendMasterRequest(InetAddress address, int port) throws IOException
     {
         String header = "Server " + address.getHostAddress() + ":" + port;
         header += '\n' + StringUtils.repeat('=', header.length());
         System.out.println(header);
 
         List<ServerAddress> serverList = new LinkedList<>();
         
         Executor exec = Executors.newFixedThreadPool(5);
 
         MasterServerQuery mQuery = new MasterServerQuery(address, port, "", serverList);
         exec.execute(mQuery);
 
         System.out.println(serverList);
         System.out.println();
     }
 
     private static void sendRequest(InetAddress address, int port) throws IOException
     {
         String header = "Server " + address.getHostAddress() + ":" + port;
         header += '\n' + StringUtils.repeat('=', header.length());
         System.out.println(header);
 
         KillingFloorServer server = new KillingFloorServer(address, port);
         
         Executor exec = Executors.newFixedThreadPool(5);
         
         exec.execute(new SourceServerQuery(address, port, server));
        exec.execute(new KillingFloorServerQuery(address, server.getGamePort() + 1, server));
        exec.execute(new KillingFloorGameQuery(address, server.getGamePort() + 1, server));
        exec.execute(new KillingFloorPlayerQuery(address, server.getGamePort() + 1, server));
         
         System.out.println();
     }
 }
