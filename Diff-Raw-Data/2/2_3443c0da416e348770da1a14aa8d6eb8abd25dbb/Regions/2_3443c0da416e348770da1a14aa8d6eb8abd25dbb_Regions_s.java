 /******************************************************************************\
 |                                     ,,                                       |
 |                    db             `7MM                                       |
 |                   ;MM:              MM                                       |
 |                  ,V^MM.    ,pP"Ybd  MMpMMMb.  .gP"Ya `7Mb,od8                |
 |                 ,M  `MM    8I   `"  MM    MM ,M'   Yb  MM' "'                |
 |                 AbmmmqMA   `YMMMa.  MM    MM 8M""""""  MM                    |
 |                A'     VML  L.   I8  MM    MM YM.    ,  MM                    |
 |              .AMA.   .AMMA.M9mmmP'.JMML  JMML.`Mbmmd'.JMML.                  |
 |                                                                              |
 |                                                                              |
 |                                ,,    ,,                                      |
 |                     .g8"""bgd `7MM    db        `7MM                         |
 |                   .dP'     `M   MM                MM                         |
 |                   dM'       `   MM  `7MM  ,p6"bo  MM  ,MP'                   |
 |                   MM            MM    MM 6M'  OO  MM ;Y                      |
 |                   MM.    `7MMF' MM    MM 8M       MM;Mm                      |
 |                   `Mb.     MM   MM    MM YM.    , MM `Mb.                    |
 |                     `"bmmmdPY .JMML..JMML.YMbmd'.JMML. YA.                   |
 |                                                                              |
 \******************************************************************************/
 /******************************************************************************\
 | Copyright (c) 2012, Asher Glick                                              |
 | All rights reserved.                                                         |
 |                                                                              |
 | Redistribution and use in source and binary forms, with or without           |
 | modification, are permitted provided that the following conditions are met:  |
 |                                                                              |
 | * Redistributions of source code must retain the above copyright notice,     |
 |   this list of conditions and the following disclaimer.                      |
 | * Redistributions in binary form must reproduce the above copyright notice,  |
 |   this list of conditions and the following disclaimer in the documentation  |
 |   and/or other materials provided with the distribution.                     |
 |                                                                              |
 | THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  |
 | AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE    |
 | IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE   |
 | ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE    |
 | LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR          |
 | CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF         |
 | SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS     |
 | INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN      |
 | CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)      |
 | ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE   |
 | POSSIBILITY OF SUCH DAMAGE.                                                  |
 \******************************************************************************/
 package iggy.Regions;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import iggy.Economy.Economy;
 
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import org.dynmap.DynmapAPI;
 import org.dynmap.markers.AreaMarker;
 import org.dynmap.markers.MarkerAPI;
 import org.dynmap.markers.MarkerSet;
 
 
 public class Regions extends JavaPlugin{
   //////////////////////////////////////////////////////////////////////////////
  ///////////////////////////// GLOBAL DECLARATIONS ////////////////////////////
 //////////////////////////////////////////////////////////////////////////////
 	Logger logger = Logger.getLogger("Minecraft");
 	
 	String pluginTitle;
 	PluginDescriptionFile pdFile;
 	
 	Plugin dynmap;
 	Plugin economy;
 	DynmapAPI dynmapapi;
 	MarkerAPI markerapi;
 	Economy economyapi;
 	
 	World mainworld;
 	World thenether;
 	
 	DisplayPlotTitles displayPlotTitles = new DisplayPlotTitles(this);
 	
 	
 	public Map<Position,String> chunkNames = new HashMap<Position,String>();
 	public Map<String,Owners> chunkOwners = new HashMap<String,Owners>();
 	
 	BlockMonitor pluginMonitor;
   //////////////////////////////////////////////////////////////////////////////
  ////////////////////////////// ENABLE / DISABLE //////////////////////////////
 //////////////////////////////////////////////////////////////////////////////
 	@Override
 	public void onDisable() {
 		// TODO clear the regions so they don't double, just reset the server for now instead of reloading plugins
 		saveRegions();
 		info(" Version " + pdFile.getVersion() +" is disabled");
 	}
 
 	@Override
 	public void onEnable() {
 		pdFile = this.getDescription();
 		String pluginName = pdFile.getName();
 		pluginTitle = "[\033[2;33m"+pluginName+"\033[0m]";
 
 		pluginMonitor = new BlockMonitor(this);
 		
 		// define worlds
 		mainworld = Bukkit.getWorld("world");
 		thenether = Bukkit.getWorld("world_nether");
 		
 		displayPlotTitles.EnableRegionDisplayNames();
 		
 		loadRegions();
 		
 		// add external plugin links
 		PluginManager pm = getServer().getPluginManager();
 		dynmap = pm.getPlugin("dynmap");
 		economy = pm.getPlugin("Economy");
 		if (dynmap == null){
 			severe("cannot find dynmap");
 			return;
 		}
 		if (economy == null) {
 			severe("cannot find economy");
 			return;
 		}
 		dynmapapi = (DynmapAPI) dynmap;
 		info("Loaded Dynmap");
 		economyapi = (Economy) economy;
 		info ("Loaded Economy");
 		
 		//set up all the block listeners to prevent distruction
 		
 		//economy is required for buying new chunks
 		//dynmap is required for mapfunctions
 		
 		if (!economy.isEnabled() || !dynmap.isEnabled()) {
 			getServer().getPluginManager().registerEvents(new OurServerListener(), this);
 			if (!economy.isEnabled()) {
 				info("Waiting for Economy to be enabled");
 			}
 			if (!dynmap.isEnabled()) {
 				info("Waiting for Dynmap to be enabled");
 			}
 		}
 		if (economy.isEnabled()){
 			activateEconomy();
 		}
 		if (dynmap.isEnabled()){
 			activatedynmap();
 		}
 		info (" Version " + pdFile.getVersion() +" is enabled");
 	}
   //////////////////////////////////////////////////////////////////////////////
  /////////////////////////////// INPUT COMMANDS ///////////////////////////////
 //////////////////////////////////////////////////////////////////////////////
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 		
 		//World world = player.getWorld();
 		/************************************ CLAIM ***********************************\
 		|
 		\******************************************************************************/
 		if (commandLabel.equalsIgnoreCase("claim")){
 			// if economy is enabled
 			if (!economy.isEnabled()) {
 				player.sendMessage("Economy plugin is not enabled, contact admin for help");
 				return false;
 			}
 			// is plot is not already claimed
 			else if (chunkNames.containsKey(new Position(player.getLocation()))){
 				player.sendMessage("This plot has allready been claimed, you cannot claim it");
 				return false;
 			}
 			// is plot in the regular world or the nether
 			else if (player.getWorld() != mainworld && player.getWorld() != thenether){
 				player.sendMessage("You can only claim plots in the nether or the main world");
 			}
 			// is a name given
 			else if (args.length == 0) {
 				player.sendMessage("You need to specify a name for this plot");
 				return false;
 			}
 			// try to claim block
 			else {
 				String plotName = args[0];
 				for (int i = 1; i < args.length; i++) {
 					plotName += " "+args[i];
 				}
 				// check to see if the name has already been taken
 				if (chunkOwners.containsKey(plotName)) {
 					player.sendMessage("This plot name has allready been taken");
 					return false;
 				}
 				
 				if (economyapi.chargeMoney(player, 5000)) {
 					Position plot = new Position(player.getLocation());
 					Owners owner = new Owners();
 					owner.addOwner(player.getName());
 					chunkOwners.put(plotName, owner);
 					chunkNames.put(plot, plotName);
 					
 					// find highest block at the four corners
 					plot.placeTorches();
 					
 					player.sendMessage("You bought the plot "+plotName+" for $5000");
 				}
 				
 				else {
 					player.sendMessage("You dont have enough money to buy this plot ($5000)");
 					return false;
 				}
 			}
 			saveRegions();
 			
 			// dynmap overlay
 			if (dynmap.isEnabled()){
 				refreshRegions ();
 			}
 		}
 		/*********************************** EXPAND ***********************************\
 		|
 		\******************************************************************************/
 		if (commandLabel.equalsIgnoreCase("expand") || commandLabel.equalsIgnoreCase("ex") ||
 				commandLabel.equalsIgnoreCase("dark-expand") || commandLabel.equalsIgnoreCase("dx")) {
 			boolean dark = false;
 			// check to see if the expand should have torches
 			if (commandLabel.equalsIgnoreCase("dark-expand") || commandLabel.equalsIgnoreCase("dx")) {
 				dark = true;
 			}
 			// TODO Find the closest plot to your position instead of the first checked
 			// if economy is enabled
 			if (!economy.isEnabled()) {
 				player.sendMessage("Economy plugin is not enabled, contact admin for help");
 				return false;
 			}
 			// is plot is not already claimed
 			else if (chunkNames.containsKey(new Position(player.getLocation()))){
 				player.sendMessage("This plot has allready been claimed, you cannot claim it");
 				return false;
 			}
 			// is plot in the regular world or the nether
 			else if (player.getWorld() != mainworld && player.getWorld() != thenether){
 				player.sendMessage("You can only claim plots in the nether or the main world");
 				return false;
 			}
 
 			// try to claim block
 			else {
 				// if a plot you own is adjacent
 				Position plot = new Position(player.getLocation());
 				String plotName = "";
 				
 				Position plotN = new Position(player.getLocation().add( 8,0, 0));
 				Position plotS = new Position(player.getLocation().add(-8,0, 0));
 				Position plotE = new Position(player.getLocation().add(0, 0, 8));
 				Position plotW = new Position(player.getLocation().add(0, 0,-8));
 				
 				// Check plot to the north
 				if (chunkNames.containsKey(plotN)){
 					String tempName = chunkNames.get(plotN);
 					Owners plotOwners = chunkOwners.get(tempName);
 					// if the player owns the found chunk
 					if (plotOwners != null) if (plotOwners.hasOwner(player.getName()))plotName = tempName;
 					// display an error if there is no owners list
 					else severe("Error finding plot owners for "+tempName);
 				}
 				if (chunkNames.containsKey(plotS)){
 					String tempName = chunkNames.get(plotS);
 					Owners plotOwners = chunkOwners.get(tempName);
 					// if the player owns the found chunk
 					if (plotOwners != null) if (plotOwners.hasOwner(player.getName()))plotName = tempName;
 					// display an error if there is no owners list
 					else severe("Error finding plot owners for "+tempName);
 				}
 				if (chunkNames.containsKey(plotE)){
 					String tempName = chunkNames.get(plotE);
 					Owners plotOwners = chunkOwners.get(tempName);
 					if (plotOwners != null) if (plotOwners.hasOwner(player.getName()))plotName = tempName;
 					// display an error if there is no owners list
 					else severe("error finding plot owners for "+tempName);
 				}
 				if (chunkNames.containsKey(plotW)){
 					String tempName = chunkNames.get(plotW);
 					Owners plotOwners = chunkOwners.get(tempName);
 					if (plotOwners != null) if (plotOwners.hasOwner(player.getName()))plotName = tempName;
 					// display an error if there is no owners list
 					else severe("error finding plot owners for "+tempName);
 				}
 				if (plotName.equalsIgnoreCase("")) {
 					player.sendMessage("No adjacent plot found, cannot expand");
 					return false;
 				}
 				
 				// check to see if the name has already been taken (this is not nessasary
 				// because only plots you own can be expanded
 				/*
 				if (chunkOwners.containsKey(plotName)) {
 					player.sendMessage("This plot name has allready been taken");
 					return false;
 				}*/
 				
 				if (economyapi.chargeMoney(player, 1000)) {
 					
 					chunkNames.put(plot, plotName);
 					
 					// find highest block at the four corners
 					if (!dark) {
 						plot.placeTorches();
 					}
 					
 					player.sendMessage("You expanded the plot "+plotName+" for $1000");
 				}
 				
 				else {
 					player.sendMessage("You dont have enough money to buy this plot");
 					return false;
 				}
 			}
 			saveRegions();
 			// dynmap overlay
 			if (dynmap.isEnabled()){
 				refreshRegions ();
 			}
 		}
 		/********************************* ADD BUILDER ********************************\
 		|
 		\******************************************************************************/
 		if (commandLabel.equalsIgnoreCase("add-builder") || commandLabel.equalsIgnoreCase("ab")) {
 			String newBuilder;
 			String plotName;
 			
 			// Get the user attempted to be added and the region name
 			if (args.length == 1){
 				if (player == null) {
 					info ("This command needs two arguments to be run by the console <player> <plot>");
 					return false;
 				}
 				newBuilder = args[0];
 				plotName = chunkNames.get(new Position(player.getLocation()));
 				if (plotName == null) {
 					player.sendMessage("You are not standing in a region");
 					return false;
 				}				
 			}
 			// If the player and the region is specified
 			else if (args.length == 2) {
 				newBuilder = args[0];
 				plotName = args[1];
 			}
 			// If any other number of arguments are entered
 			else {
 				if (player == null) info ("This command needs two arguments to be run by the console <player> <plot>");
 				else player.sendMessage("Correct usage /add-builder <player> [<plot>]");
 				return false;
 			}
 			
 			// Make sure the user adding the builder is allowed to do so
 			Owners owners = chunkOwners.get(plotName);
 			if (owners == null) {
 				if (player == null) info ("The plot "+plotName+" does not exist");
 				else player.sendMessage("The plot "+plotName+" does not exist");
 				return false;
 			}
 			if (player == null || owners.hasOwner(player.getName())) {
 				// Add the builder to the plot
				owners.addBuilder(newOwner);
 				// Save the owners list to the global variable
 				chunkOwners.put(plotName, owners);
 				
 				if (player == null) info ("Added "+newBuilder+" to "+plotName);
 				else player.sendMessage("Added "+newBuilder+" to "+plotName);
 			}
 			else { player.sendMessage("You do not have permission to add a Builder to this plot"); }
 			
 						
 		}
 		// TODO Remove builder from plot
 		/******************************* REMOVE BUILDER *******************************\
 		|
 		\******************************************************************************/
 		if (commandLabel.equalsIgnoreCase("remove-builder") || commandLabel.equalsIgnoreCase("rb")) {
 			String oldBuilder;
 			String plotName;
 			
 			// Get the user attempted to be added and the region name
 			if (args.length == 1){
 				if (player == null) {
 					info ("This command needs two arguments to be run by the console <player> <plot>");
 					return false;
 				}
 				oldBuilder = args[0];
 				plotName = chunkNames.get(new Position(player.getLocation()));
 				if (plotName == null) {
 					player.sendMessage("You are not standing in a region");
 					return false;
 				}				
 			}
 			// If the player and the region is specified
 			else if (args.length == 2) {
 				oldBuilder = args[0];
 				plotName = args[1];
 			}
 			// If any other number of arguments are entered
 			else {
 				if (player == null) info ("This command needs two arguments to be run by the console <player> <plot>");
 				else player.sendMessage("Correct usage /add-builder <player> [<plot>]");
 				return false;
 			}
 			
 			// Make sure the user adding the builder is allowed to do so
 			Owners owners = chunkOwners.get(plotName);
 			if (owners == null) {
 				if (player == null) info ("The plot "+plotName+" does not exist");
 				else player.sendMessage("The plot "+plotName+" does not exist");
 				return false;
 			}
 			if (player == null || owners.hasOwner(player.getName())) {
 				// Add the builder to the plot
 				owners.removeBuilder(oldBuilder);
 				// Save the owners list to the global variable
 				chunkOwners.put(plotName, owners);
 				
 				if (player == null) info ("Removed "+oldBuilder+" from "+plotName);
 				else player.sendMessage("Removed "+oldBuilder+" from "+plotName);
 			}
 			else { player.sendMessage("You do not have permission to add a Builder to this plot"); }
 			
 						
 		}
 		// TODO Add owner to plot
 		/********************************** ADD OWNER *********************************\
 		|
 		\******************************************************************************/
 		if (commandLabel.equalsIgnoreCase("add-builder") || commandLabel.equalsIgnoreCase("ab")) {
 			String newOwner;
 			String plotName;
 			
 			// Get the user attempted to be added and the region name
 			if (args.length == 1){
 				if (player == null) {
 					info ("This command needs two arguments to be run by the console <player> <plot>");
 					return false;
 				}
 				newOwner = args[0];
 				plotName = chunkNames.get(new Position(player.getLocation()));
 				if (plotName == null) {
 					player.sendMessage("You are not standing in a region");
 					return false;
 				}				
 			}
 			// If the player and the region is specified
 			else if (args.length == 2) {
 				newOwner = args[0];
 				plotName = args[1];
 			}
 			// If any other number of arguments are entered
 			else {
 				if (player == null) info ("This command needs two arguments to be run by the console <player> <plot>");
 				else player.sendMessage("Correct usage /add-builder <player> [<plot>]");
 				return false;
 			}
 			
 			// Make sure the user adding the builder is allowed to do so
 			Owners owners = chunkOwners.get(plotName);
 			if (owners == null) {
 				if (player == null) info ("The plot "+plotName+" does not exist");
 				else player.sendMessage("The plot "+plotName+" does not exist");
 				return false;
 			}
 			if (player == null || owners.hasOwner(player.getName())) {
 				// Add the builder to the plot
 				owners.addBuilder(newOwner);
 				// Save the owners list to the global variable
 				chunkOwners.put(plotName, owners);
 				
 				if (player == null) info ("Added "+newOwner+" to "+plotName);
 				else player.sendMessage("Added "+newOwner+" to "+plotName);
 			}
 			else { player.sendMessage("You do not have permission to add a Builder to this plot"); }
 			
 						
 		}
 		// TODO Admin Remove Plot
 		/********************************* REMOVE PLOT ********************************\
 		|
 		\******************************************************************************/
 		// TODO List Owners
 		/********************************* LIST OWNERS ********************************\
 		|
 		\******************************************************************************/
 		if (commandLabel.equalsIgnoreCase("list-owners")) {
 			String plotName;
 			if (args.length == 0) {
 				if (player == null) {
 					info ("You need to specify a plot to check");
 					return false;
 				}
 				
 				plotName = chunkNames.get(new Position(player.getLocation()));
 				if (plotName == null) {
 					player.sendMessage("You are not standing in a region");
 					return false;
 				}
 			}
 			else if (args.length == 1) {
 				plotName = args[0];
 			}
 			else {
 				if (player == null) info ("Use list-owner <plotname>");
 				else player.sendMessage("Use /list-owner <plotname>");
 				return false;
 			}
 			
 			Owners owners = chunkOwners.get(plotName);
 			if (owners == null){
 				if (player == null) info ("The plot "+plotName+" was not found");
 				else player.sendMessage("The plot "+plotName+" was not found");
 				return false;
 			}
 			if (player == null) info ("Owners: "+ owners.getOwners().toString()+"\nBuilders:"+owners.getBuilders().toString());
 			else player.sendMessage(owners.getOwners().toString());
 			
 		}
 		// TODO List Builders
 		/******************************** LIST BUILDERS *******************************\
 		|
 		\******************************************************************************/
 		
 		return false;		
 	}
   //////////////////////////////////////////////////////////////////////////////
  /////////////////////////// WAIT FOR OTHER PLUGINS ///////////////////////////
 //////////////////////////////////////////////////////////////////////////////
 	// listener class to wait for the other plugins to enable
 	private class OurServerListener implements Listener {
 		// warnings are suppressed becasue this is called using registerEvents when the 
 		@SuppressWarnings("unused")
 		// this function runs whenever a plugin is enabled
 		@EventHandler (priority = EventPriority.MONITOR)
         public void onPluginEnable(PluginEnableEvent event) {
             Plugin p = event.getPlugin();
             String name = p.getDescription().getName();
             if(name.equals("dynmap")) {
             	activatedynmap();
             }
             if(name.equals("Economy")) {
             	activateEconomy();
             }
         }
     }
 
 	// funtion to finish activating the plugin once the other plugins are enabled
 	public void activateEconomy(){
 		info ("Economy features (claim, expand) enabled");
 	}
 	
 	/******************************* ACTIVATE DYNMAP ******************************\
 	| This function is run when dynmap is activated. It sets up the functions that |
 	| map the regions onto the dynmap display                                      |
 	\******************************************************************************/
 	MarkerSet set;
 	public void activatedynmap() {
 		markerapi =  dynmapapi.getMarkerAPI();
 		if (markerapi == null){
 			severe ("error loading the dynmap marker api");
 			return;
 		}
 		// create the market set
 		/* Now, add marker set for mobs (make it transient) */
         set = markerapi.getMarkerSet("regions.markerset");
         if(set == null)
             set = markerapi.createMarkerSet("regions.markerset", "Regions", null, false);
         else
             set.setMarkerSetLabel("Regions");
         if(set == null) {
             severe("Error creating marker set");
             return;
         }
         int minzoom = 0;
         if(minzoom > 0)
         set.setMinZoom(minzoom);
         set.setLayerPriority(10);
         set.setHideByDefault(true);
 
         // make the plots show up on the map
         refreshRegions();
 		info("dynmap features (view plots on map) enabled");
 	}
 	
   //////////////////////////////////////////////////////////////////////////////
  ////////////////////////// DYNMAP DISPLAY FUNCTIONS //////////////////////////
 //////////////////////////////////////////////////////////////////////////////
 	
 	private Map<String, AreaMarker> resareas = new HashMap<String, AreaMarker>();
 
 	/********************************* POINT CLASS ********************************\
 	| The point class is a simple class made for ease of use with sorting the      |
 	| lists that define the borders of plots. It is used in order to display the   |
 	| plots on dynmap. Each point contains a single X and Z point                  |
 	\******************************************************************************/
 	class Point {
 		public Point(double x, double z){
 			_x = x;
 			_z = z;
 		}
 		double _x;
 		double _z;
 		public String toString () {
 			return "("+_x+","+_z+")";
 		}
 		public boolean equals(Point two) {
 			if (two._z == _z && two._x == _x) return true;
 			return false;
 		}
 	}
 	
 	/********************************* MERGE EDGES ********************************\
 	| The merge edges function tries to match the begining of certian                ---------------------------------------------
 	\******************************************************************************/
 	private List <List<Point>> mergeEdges (List <List<Point>> pointLists) {
 		List <List <Point >> resultingLists = new ArrayList <List <Point>>();
 		
 		while (pointLists.size() > 0) {
 			List<Point> first = pointLists.remove(0);
 			Point first_beginPoint = first.get(0);
 			Point first_finalPoint = first.get(first.size()-1);
 			boolean didMergeLines = false;
 			// Loop through all the other edges to see if there is a match
 			for (int i = 0; i < pointLists.size(); i++){
 				Point second_beginPoint = pointLists.get(i).get(0);
 				Point second_finalPoint = pointLists.get(i).get(pointLists.get(i).size()-1);
 				if (first_beginPoint.equals(second_finalPoint)) {					
 					// Merge the two matching point lists
 					List <Point> second = pointLists.remove(i);
 					first.remove(0); // get rid of the overlapping point
 					second.addAll(first);
 					first = second;
 					didMergeLines = true;
 					// prevent more merges from happening with the outdated point data
 					break;
 				}
 				else if (first_finalPoint.equals(second_beginPoint)) {
 					// Merge the two matching point lists
 					List <Point> second = pointLists.remove(i);
 					second.remove(0); // get rid of the overlapping point
 					first.addAll(second);
 					didMergeLines = true;
 					// prevent more merges from happening with the outdated point data
 					break;
 				}
 			}
 			// if there was a merge, throw it back in to check for another merge
 			if (didMergeLines) { pointLists.add(first); }
 			// if there was no merge add it to the list of finished lists
 			else if (!didMergeLines) { resultingLists.add(first); }
 		}
 		return resultingLists;
 	}
 	/******************************* LINERIZE EDGES *******************************\
 	| Linearize edges takes in a list of lists of points, representing the edges,   |-------------------------------------
 	| and turns them into a single list of edges.
 	\******************************************************************************/
 	private List <Point> linearizeEdges (List<List<Point>> pointLists){
 		List<List<Point>> newPointLists = new ArrayList<List<Point>>();
 		for (List<Point> pointlist : pointLists) {
 			List<Point> newpointlist = new ArrayList<Point>();
 			
 			// if the first point is not a corner do not include it
 			{
 				boolean pre_del_x = pointlist.get(0)._x != pointlist.get(pointlist.size()-2)._x;
 				boolean pre_del_z = pointlist.get(0)._z != pointlist.get(pointlist.size()-2)._z;
 				boolean post_del_x= pointlist.get(0)._x != pointlist.get(1)._x;
 				boolean post_del_z= pointlist.get(0)._z != pointlist.get(1)._z;
 				if (!((!pre_del_x && !post_del_x) || (!pre_del_z && !post_del_z))) {
 					newpointlist.add(pointlist.get(0));
 				}
 			}
 			// if every other point is not a corner do not include it
 			for (int i = 1; i < pointlist.size() -1; i ++) {
 				boolean pre_del_x = pointlist.get(i)._x != pointlist.get(i-1)._x;
 				boolean pre_del_z = pointlist.get(i)._z != pointlist.get(i-1)._z;
 				boolean post_del_x= pointlist.get(i)._x != pointlist.get(i+1)._x;
 				boolean post_del_z= pointlist.get(i)._z != pointlist.get(i+1)._z;
 				if (!((!pre_del_x && !post_del_x) || (!pre_del_z && !post_del_z))) {
 					newpointlist.add(pointlist.get(i));
 				}
 			}
 			// add the begining element to the end for areas with more then one edgeline
 			newpointlist.add(newpointlist.get(0));
 			// add the point list to the new set of lists
 			newPointLists.add(newpointlist);
 		}
 		pointLists = newPointLists;
 		
 		// Create a list of 
 		List <Point> returnPath = new ArrayList <Point>();
 		
 		List <Point> fullList = new ArrayList <Point>();
 		for (List<Point> pointlist : pointLists) {
 			returnPath.add(pointlist.get(0));
 			fullList.addAll(pointlist);
 		}
 		for (int i = returnPath.size()-1; i >= 0; i--){
 			Point point = returnPath.get(i);
 			fullList.add(point);
 		}
 		return fullList;
 	}
 	/****************************** EXTRACT X POINTS ******************************\
 	| This function goes through a list of points (which contains X and Z values)  | 
 	| Then extracts all of the X points and returns them as an array of doubles.   |
 	| It is virtually identical to the extract z points function                   |
 	\******************************************************************************/
 	private double [] extractx (List <Point> pointList) {
 		int length = pointList.size();
 		double [] xvalues = new double[length];
 		for (int i = 0; i < length; i++) {
 			xvalues[i] = pointList.get(i)._x;
 		}
 		return xvalues;
 	}
 	
 	/****************************** EXTRACT Z POINTS ******************************\
 	| This function goes through a list of points (which contains X and Z values)  | 
 	| Then extracts all of the Z points and returns them as an array of doubles.   |
 	| It is virtually identical to the extract x ponts function
 	\******************************************************************************/
 	private double [] extractz (List <Point> pointList) {
 		int length = pointList.size();
 		double [] zvalues = new double[length];
 		for (int i = 0; i < length; i++) {
 			zvalues[i] = pointList.get(i)._z;
 		}
 		return zvalues;
 	}
 
 	/******************************* REFRESH REGIONS ******************************\
 	| The refresh regions function takes all of the region data stored by the      |
 	| regions plugin and turns it into the map display data you see on the dynmap  |
 	| It does this by getting all the edges of each region and then pairing up     |
 	| the edges that share end points, that then makes a continuous line around    |
 	| the region. If, once all the end points are paired, there is more then one   |
 	| line a new line is drawn to connect the disconnected regions                 |
 	\******************************************************************************/
 	public void refreshRegions () {
 		int newCount = 0;
 		int replaceCount = 0;
 		int countID = 0;
 		
 		// Create a map for the new region areas
 		Map<String, AreaMarker> newresareas = new HashMap<String, AreaMarker>();
 		
 		// Create a map for name to region lists
 		Map<String, HashSet <Position> > regions = new HashMap <String, HashSet <Position>>();
 		
 		// Loop through and create a map of plot names mapped to lists of plots
 		for (Entry<Position,String> positionIterator : chunkNames.entrySet()) {
 			Position position = positionIterator.getKey();
 			String name = positionIterator.getValue();
 			
 			HashSet<Position> plotList = new HashSet<Position>();
 			if (regions.containsKey(name)) {
 				plotList = regions.get(name);
 			}
 			plotList.add(position);
 			
 			regions.put(name, plotList);
 		}
 		
 		
 		for (Entry <String, HashSet <Position> > regionIterator : regions.entrySet()){
 
 			String id = "region"+countID;
 			String name = regionIterator.getKey();
 			
 			String worldName = regionIterator.getValue().iterator().next()._world;
 		    
 			List <List <Point>> pointLists = new ArrayList <List <Point>>();
 			HashSet <Position> plotPositions = regionIterator.getValue();
 			// For each plot in the region, create edges on all sides that are not shared with another plot of the region
 			for (Position position : regionIterator.getValue()) {
 				List <List <Point>> thisPlotsPoints = new ArrayList <List <Point>>();
 				String thisWorld = position._world;
 				long thisX = position._x;
 				long thisZ = position._z;
 				// Check Left
 				Position left = new Position(thisWorld,thisX-1,thisZ  );
 				if (!plotPositions.contains(left)) {
 					List <Point> edgePoints = new ArrayList <Point>();
 					edgePoints.add(new Point(position.getMinimumXCorner()  ,position.getMinimumZCorner()  ));
 					edgePoints.add(new Point(position.getMinimumXCorner()  ,position.getMinimumZCorner()+8));
 					thisPlotsPoints.add(edgePoints);
 				}
 				
 				// Check Up
 				Position up = new Position(thisWorld,thisX, thisZ +1);
 				if (!plotPositions.contains(up)) {
 					List <Point> edgePoints = new ArrayList <Point>();
 					edgePoints.add(new Point(position.getMinimumXCorner()  ,position.getMinimumZCorner()+8));
 					edgePoints.add(new Point(position.getMinimumXCorner()+8,position.getMinimumZCorner()+8));
 					thisPlotsPoints.add(edgePoints);
 				}
 				
 				// Check Right
 				Position right = new Position(thisWorld, thisX + 1, thisZ);
 				if (!plotPositions.contains(right)){
 					List <Point> edgePoints = new ArrayList <Point>();
 					edgePoints.add(new Point(position.getMinimumXCorner()+8,position.getMinimumZCorner()+8));
 					edgePoints.add(new Point(position.getMinimumXCorner()+8,position.getMinimumZCorner()));
 					thisPlotsPoints.add(edgePoints);
 				}
 				
 				// Check down
 				Position down = new Position(thisWorld, thisX,thisZ-1);
 				if (!plotPositions.contains(down)){
 					List <Point> edgePoints = new ArrayList <Point>();
 					edgePoints.add(new Point(position.getMinimumXCorner()+8,position.getMinimumZCorner()  ));
 					edgePoints.add(new Point(position.getMinimumXCorner()  ,position.getMinimumZCorner()  ));
 					thisPlotsPoints.add(edgePoints);
 				}
 				// add the sorted and merged edges into the entire plot's point list
 				pointLists.addAll(mergeEdges(thisPlotsPoints));
 			}
 			pointLists = mergeEdges(pointLists);
 			List<Point> edgepoints = linearizeEdges(pointLists);
 			
         	// draw an outline
     		double[] x = extractx(edgepoints);
     		double[] z = extractz(edgepoints);
     		
     		// Attempt to remove the region with the same ID
 			AreaMarker m = resareas.remove(id);
 			
 			// If the region did not exist, create a new one
 		    if(m == null) {
 			    m = set.createAreaMarker(id, name, false, worldName, x, z, false);
 		        if(m == null) {info("null region");continue;}
 		        // setLineStyle(weight,opacity,color)
 		        m.setLineStyle(3, .8, 0xFF0000);
 		        // setFillStyle (opacity, color)
 		        m.setFillStyle(.35, 0xFF0000);
 		        m.setRangeY(60, 70);
 		        newCount++;
 		    }
 		    // If the region did exist just change the data, no need to create a new one
 		    else {
 		        m.setCornerLocations(x, z); // Replace the border points    
 		        m.setLabel(name); // Replace the name of the plot
 		        
 		        replaceCount++; 
 		    }
 		    newresareas.put(id, m);
 		    countID += 1;
 		}
 		resareas = newresareas;
 		info ("NEW COUNT:"+newCount);
 		info ("REPLACE COUNT:"+replaceCount);
 	}
   //////////////////////////////////////////////////////////////////////////////
  /////////////////////////////// REGION STORAGE ///////////////////////////////
 //////////////////////////////////////////////////////////////////////////////
 	/******************************** SAVE REGIONS ********************************\
 	| The save regions function save the three components of each region. The      |
 	| plots contained within the region, the owners of the plot, and the builders  |
 	| assigned to the plot. These pieces of data are saved to the config file      |
 	\******************************************************************************/
 	public void saveRegions() {
 		getConfig().set("regions", "");
 		
 		Map<String,List<String> > plotLists = new HashMap<String,List<String>>();
 		
 		// load all of the plots together in an array with their plotname
 		Iterator<Entry<Position, String>> plotIterator = chunkNames.entrySet().iterator();
 		while (plotIterator.hasNext()){
 			Entry<Position, String> pair = plotIterator.next();
 			List <String> plotsLocations = plotLists.get(pair.getValue());
 			if (plotsLocations == null){
 				plotsLocations = new ArrayList<String>();
 			}
 			plotsLocations.add(pair.getKey().toString());
 			plotLists.put(pair.getValue(), plotsLocations);
 		}
 		
 		// write the plots by their plotnames at regions.plotname.plots
 		Iterator<Entry<String, List<String>>> plotList = plotLists.entrySet().iterator();
 		while (plotList.hasNext()) {
 			Entry<String, List<String>> pair = plotList.next();
 			getConfig().set("regions."+pair.getKey()+".plots", pair.getValue());
 			// draw the regions
 		}
 		
 		// write the owners to the plots as well
 		Iterator<Entry<String, Owners>> ownerIterator = chunkOwners.entrySet().iterator();
 		while (ownerIterator.hasNext()) {
 			Entry<String, Owners> pairs = ownerIterator.next();
 			this.getConfig().set("regions."+pairs.getKey()+".owners",  pairs.getValue().getOwners());
 			this.getConfig().set("regions."+pairs.getKey()+".builders",pairs.getValue().getBuilders());
 		}
 		this.saveConfig();
 		info("Regions Saved");
 	}
 	
 	/******************************** LOAD REGIONS ********************************\
 	| The load regions function does the opposite of the save region function. It  |
 	| loads the plots contained in the region, the owners, and the builders into   |
 	| memory for use during the game                                               |
 	\******************************************************************************/
 	public void loadRegions() {
 		chunkNames.clear();
 		chunkOwners.clear();
 		ConfigurationSection regionSection = getConfig().getConfigurationSection("regions");
 		if (regionSection == null){
 			severe("cannot load regions (region section no found)");
 			return;
 		}
 		Set<String> regions = regionSection.getKeys(false);
 		if (regions == null){
 			severe("cannot load regions (no regions found in regions)");
 			return;
 		}
 		// for each region
 		for (String region : regions){
 			 List<String> plots = getConfig().getStringList("regions."+region+".plots");
 			 List<String> ownerslist= getConfig().getStringList("regions."+region+".owners");
 			 List<String> builderslist = getConfig().getStringList("regions."+region+".builders");
 			 if (plots == null) {
 				 severe("error loading configuration (no plots found for this region)");
 				 continue;
 			 }
 			 if (ownerslist == null) {
 				 severe("error loading configuration (no owners found for this region)");
 				 continue;
 			 }
 			 // add all of the plots for each region to the region list
 			 for (String plot : plots){
 				Position position = new Position();
 				position.setFromString(plot);
 				chunkNames.put(position, region);
 			 }
 			 // add all of the users to the user list
 			 Owners owners = new Owners();
 			 owners.addOwners(ownerslist);
 			 if (builderslist != null) {
 				 owners.addBuilders(builderslist);
 			 }
 			 chunkOwners.put(region, owners);
 		}
 		info ("regions loaded");
 	}
   //////////////////////////////////////////////////////////////////////////////
  /////////////////////////////// DISPLAY HELPERS //////////////////////////////
 //////////////////////////////////////////////////////////////////////////////
 	/********************************** LOG INFO **********************************\
 	| The log info function is a simple function to display info to the console    |
 	| logger. It also prepends the plugin title (with color) to the message so     |
 	| that the plugin that sent the message can easily be identified               |
 	\******************************************************************************/
 	public void info(String input) {
 		this.logger.info(pluginTitle + input);
 	}
 	
 	/********************************* LOG SEVERE *********************************\
 	| The log severe function is very similar to the log info function in that it  |
 	| displays information to the console, but the severe function sends a SEVERE  |
 	| message instead of an INFO. It also turns the message text red               |
 	\******************************************************************************/
 	public void severe (String input) {
 		this.logger.severe(pluginTitle+"\033[31m"+input+"\033[0m");
 	}
 	
 }
