 package net.mayateck.DataShuttle;
 
import java.io.File;

 import org.bukkit.ChatColor;
 import org.bukkit.plugin.java.JavaPlugin;
 
 //@SuppressWarnings("unused")
 public final class DataShuttle extends JavaPlugin{
 	
	public static File jarFile = new File(DataShuttle.class.getProtectionDomain().getCodeSource().getLocation().getPath());
	
 	public static boolean isReady = false;
 	public static boolean isConnected = false;
 	
 	public static String block = ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "||" + ChatColor.RESET;
 	public static String header = " " + block + ChatColor.AQUA + "DS" + block;
 	public static String hError = header + " " + ChatColor.RED + "ERROR: " + ChatColor.RESET;
 	public static String hSucc = header + " " + ChatColor.GREEN + "SUCCESS! " + ChatColor.RESET;
 	public static String hNote = header + " " + ChatColor.GRAY + "NOTICE: " + ChatColor.RESET;
 	
 	@Override
 	public void onEnable(){
 		getLogger().info("DataShuttle invoked. Attemping database connection...");
		getLogger().info("Checksum for this version: "+FetchNetworkData.checksum);
		int connectAttempt = FetchNetworkData.checkConnectionToServer();
		if (connectAttempt==0){
			isConnected = true;
		} else if(connectAttempt==1){
			getLogger().info("An error occured while contacting the server.");
		} else if(connectAttempt==2){
			getLogger().info("The checksum for this JAR file is invalid. (Is DataShuttle up-to-date?)");
		} else if(connectAttempt==3){
			getLogger().info("The database is currently undergoing maintinence.");
		}
 		if (isConnected == true){
 			getLogger().info("Connection achieved. DataShuttle is now registering remaining data...");
 			getCommand("ds").setExecutor(new CommandHandler(this));
 			getLogger().info("Success! DataShuttle is ready!");
 		} else {
 			getLogger().info("Connection failed. DataShuttle cannot function without a connection.");
 		}
 	}
 	
 	@Override
 	public void onDisable(){
 		getLogger().info("DataShuttle shutting down...");
 	}
 }
