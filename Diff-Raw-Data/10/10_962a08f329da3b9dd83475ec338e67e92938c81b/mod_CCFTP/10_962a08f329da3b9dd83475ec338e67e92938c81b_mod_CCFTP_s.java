 package net.minecraft.server;
 
 import de.doridian.ccftp.ComputerCraftFTP;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.Socket;
 import java.net.URL;
 import java.net.URLConnection;
 
 public class mod_CCFTP extends BaseModMp {
 
 	@MLProp
 	public static int ftp_port = 2221;
 	@MLProp
 	public static String ftp_ip = "";
     @MLProp
     public static String ftp_passive_ports = "40000-50000";
     @MLProp
     public static String ftp_passive_local_address = "";
     @MLProp
     public static String ftp_max_file_size = "1MB";
 	
 	public static String default_world = "world";
 
 	@Override
	public void ModsLoaded() {
		super.ModsLoaded();
 
 		new Thread() {
 			public void run() {
 				MinecraftServer server = ModLoader.getMinecraftServerInstance();
 				while(server.networkListenThread == null) {
 					try {
 						Thread.sleep(1000);
 					} catch(Exception e) { }
 				}
 
 				if(ftp_ip == null ||ftp_ip.isEmpty()) {
 					ftp_ip = server.propertyManager.properties.getProperty("server-ip");
 				}
                 
                 if(ftp_passive_local_address == null || ftp_passive_local_address.isEmpty()) {
                     try {
                         Socket sock = new Socket("8.8.8.8", 53);
                         String addr = sock.getLocalAddress().getHostAddress();
                         sock.close();
                         URLConnection urlConnection = new URL("http://system.doridian.de/ip.php").openConnection();
                         urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.2; rv:9.0.1) Gecko/20100101 Firefox/9.0.1");
                         BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                         String newAddr = reader.readLine();
                         if(!newAddr.equalsIgnoreCase(addr)) {
                             ftp_passive_local_address = newAddr;
                             System.out.println("Autodetected passive local address to be: " + newAddr);
                         }
                         reader.close();
                     } catch(Exception e) {
                         e.printStackTrace();
                     }
                 }
 
 				System.out.println("Starting CCFTPd on " + ftp_ip + ":" + ftp_port + " (default world \"" + default_world + "\", using passive ports " + ftp_passive_ports + ")");
 
 				try {
 					new ComputerCraftFTP(ftp_port, ftp_ip, ftp_passive_ports, ftp_passive_local_address, ftp_max_file_size);
 				} catch(Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	@Override
 	public String getVersion() {
 		return "1.0";
 	}
 
 	@Override
 	public String getName() {
 		return "mod_CCFTPd";
 	}
 
 	@Override
	public void HandlePacket(Packet230ModLoader packet230ModLoader, EntityPlayer entityPlayer) { }
 
 	@Override
	public void HandleLogin(EntityPlayer entityPlayer) { }
 
 	@Override
 	public boolean hasClientSide() {
 		return false;
 	}
 
 	@Override
 	public void load() {}
 }
