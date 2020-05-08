 package us.mbilker.minecraftportable;
 
 import java.io.File;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class MinecraftPortable {
 	
 	public static File currentDir = new File(System.getProperty("user.dir", "."));
 	public static File dataDir = new File(currentDir, "mcp_data");
 	public static File serverDir = new File(dataDir, "server");
 	
 	public static File clientDir = new File(dataDir, "mc");
 	
 	public MinecraftPortable(String[] args) {
 		
 		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss aa");
         Date date = new Date();
 		
 		Main.log("Minecraft Portable 2.8");
 		Main.log("by mbilker\n");
 		Main.log("Started at %s", dateFormat.format(date));
 		Main.log("Data directory: %s\n", dataDir.toString());
 		
 		if (!dataDir.exists()) {
 			Main.log("Data folder does not exist, creating. Typical on first start.");
 			dataDir.mkdir();
 		}
 		
 		if (!serverDir.exists()) {
 			Main.log("Server folder does not exist, creating. Typical on first start.");
 			serverDir.mkdir();
 		}
 		
 		if (!clientDir.exists()) {
 			Main.log(".minecraft folder does not exist, creating. Typical on first start.");
 			clientDir.mkdir();
 		}
 		
 		LauncherFrame.main(args);
 	}
 
 }
