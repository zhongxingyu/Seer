 package com.niccholaspage.nICs;
 
 import java.util.HashMap;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.event.Event;
 
 import com.niccholaspage.nICs.ics.*;
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class nICs extends JavaPlugin {
 	private final nICsBlockListener blockListener = new nICsBlockListener(this);
 	//Create IC hashmap :)
 	public final HashMap<String, IC> ics = new HashMap<String, IC>();
 	//Perma
 	public PermissionHandler permissions;
     @Override
 	public void onDisable() {
 		//Print "Basic Disabled" on the log.
 		System.out.println("nICs Disabled");
 		
 	}
     @Override
 	public void onEnable() {
 		//Create the pluginmanage pm.
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.REDSTONE_CHANGE, this.blockListener, Event.Priority.Normal, this);
 	    pm.registerEvent(Event.Type.SIGN_CHANGE, this.blockListener, Event.Priority.Normal, this);
        //Get the infomation from the yml file.
         PluginDescriptionFile pdfFile = this.getDescription();
         //Add all the ICs
         setupICs();
         //Perma.
         setupPermissions();
         //Print that the plugin has been enabled!
         System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
 	}
     private void setupICs(){
     	ics.put("MC1235", new MC1235());
     	ics.put("MC1236", new MC1236());
     	ics.put("MC1237", new MC1237());
     	ics.put("MC1238", new MC1238());
     	ics.put("MC1239", new MC1239());
     	ics.put("N1000", new N1000());
     }
 	public int getSignDirection(Sign signBlock){
 		if(signBlock.getRawData() == 0x2){
     		return 3;//EAST
        	}
     	else if(signBlock.getRawData() == 0x3){
     		
     		return 1;//WEST
     	}
     	else if(signBlock.getRawData() == 0x4){
     		return 4;//NORTH
     	}
     	else if(signBlock.getRawData() == 0x5){
     		return 2;//SOUTH    	
     	}   
 		return -1;
 	}
 	public void setLever(Sign signBlock, boolean on)
 	{
 			
 			Location loc = signBlock.getBlock().getLocation();
 			int direction = getSignDirection(signBlock);
 			switch (direction){
 			case 3: loc.setZ(loc.getZ() + 2); break;
 			case 1: loc.setZ(loc.getZ() - 2); break;
 			case 4: loc.setX(loc.getX() + 2); break;
 			case 2: loc.setX(loc.getX() - 2); break;
 			}
 			Block lever = signBlock.getWorld().getBlockAt(loc);
 	    	if(lever.getType().equals(Material.LEVER)){
 		    	int data = lever.getData();
 		    	if(on){          	
 	            	if ((data & 0x8) != 8){
 	            		data |= 8;
 	            	}
 		    	}else if ((data & 0x8) == 8){
 		    		data ^= 8;		    		
 		    	}
 		    	lever.setData((byte)data);
 			}
 	}
 	public Block getBlockFrontOfSign(Sign signBlock){
 		int direction = getSignDirection(signBlock);
 		Location loc = signBlock.getBlock().getLocation();
 		switch (direction){
 		case 3: loc.setZ(loc.getZ() - 1); break;
 		case 1: loc.setZ(loc.getZ() + 1); break;
 		case 4: loc.setX(loc.getX() - 1); break;
 		case 2: loc.setX(loc.getX() + 1); break;
 		}
 		return signBlock.getWorld().getBlockAt(loc);
 	}
 	public Block getBlockBehindOfSign(Sign signBlock){
 		int direction = getSignDirection(signBlock);
 		Location loc = signBlock.getBlock().getLocation();
 		switch (direction){
 		case 3: loc.setZ(loc.getZ() + 1); break;
 		case 1: loc.setZ(loc.getZ() - 1); break;
 		case 4: loc.setX(loc.getX() + 1); break;
 		case 2: loc.setX(loc.getX() - 1); break;
 		}
 		return signBlock.getWorld().getBlockAt(loc);
 	}
     public boolean isInt(String i){
     	try {
     		Integer.parseInt(i);
     		return true;
     	} catch(NumberFormatException nfe){
     		return false;
     	}
     }
     private void setupPermissions() {
         Plugin test = getServer().getPluginManager().getPlugin("Permissions");
 
         if (permissions == null)
           if (test != null) {
             permissions = ((Permissions)test).getHandler();
           } else {
             System.out.println("No permissions system was found. ICs can be built by everyone.");
           }
       }
 }
