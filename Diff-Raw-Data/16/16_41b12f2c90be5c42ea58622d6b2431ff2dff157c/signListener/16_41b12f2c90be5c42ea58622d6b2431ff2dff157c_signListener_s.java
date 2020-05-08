 package com.egodroid.bukkit.carmod.listeners;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import com.egodroid.bukkit.carmod.CarMod;
 import com.egodroid.bukkit.carmod.util.FuelManager;
 
 public class signListener implements Listener {
 	
 	
 	private CarMod mPlugin;
 	private boolean useEconomy = false;
 	private boolean useLicense = true;
 	private int licenseCost = 0;
	@SuppressWarnings("unused")
 	private minecartListener mML;
 	private FuelManager mFM;
 	private File inputFile, tempFile;
 	private ChatColor dGreen = ChatColor.DARK_GREEN;
 	private ChatColor green = ChatColor.GREEN;
 	private ChatColor white = ChatColor.WHITE;
 	
 	
 	
 	
 	
 public signListener(CarMod pPlugin, minecartListener pML, FuelManager pFM) throws IOException {
 	this.mPlugin = pPlugin;
 	this.mML = pML;
 	this.mFM = pFM;
 	this.inputFile = new File("plugins/MineCars/signs.txt");
 	this.tempFile = new File("plugins/MineCars/signs_temp.txt");
 	if(!this.inputFile.exists())
 		this.inputFile.createNewFile();
 	if(!this.tempFile.exists())
 		this.tempFile.createNewFile();
 	
 	this.loadSigns();
 	
 }
 
 	public void setLicenseCost(int cost){licenseCost = cost;}
 	
 	public void setUseLicense(boolean use){useLicense = use;}
 
 @EventHandler
 public void onSignChange(SignChangeEvent event) throws IOException {
 	if (this.mPlugin.getConfig().getBoolean("UseFuelSystem")) {
 		String[] templines = event.getLines();
 		if(templines[0].equalsIgnoreCase("Fuel Station")) {
 			if (CarMod.permission.has(event.getPlayer(), "minecars.fuelstation.create")) {
 				String temploc = Double.toString(event.getBlock().getLocation().getX()) + ";" + Double.toString(event.getBlock().getLocation().getY()) + ";" + Double.toString(event.getBlock().getLocation().getZ()) + ";" + event.getBlock().getWorld().getName(); 
 				this.addSign(event.getBlock().getLocation(), event.getBlock().getWorld().getName());
 		  
 		  templines[0] = green + "Fuel Station";
 		  templines[1] = ChatColor.WHITE+" Costs: ";
 		  
 		  if (this.useEconomy) {
 			  templines[2] = ChatColor.WHITE+new Integer(this.mPlugin.getConfig().getInt("priceperfuel")).toString() + " " + CarMod.economy.currencyNamePlural();
 		  } else {
 			  templines[2] = ChatColor.WHITE+new Integer(this.mPlugin.getConfig().getInt("itemsperfuel")).toString() + " " + Material.getMaterial(this.mPlugin.getConfig().getInt("fuelitemid")).name();
 		  }
 		  
 		  event.getPlayer().sendMessage(String.format(dGreen+"[%s]"+white+" Fuel Station sucessfully registered!", this.mPlugin.getDescription().getName()));
 			} else {
 				event.getPlayer().sendMessage(String.format(dGreen+"[%s]"+white+" You don't have the permission to create a Fuel Station!", this.mPlugin.getDescription().getName()));
 				event.getBlock().breakNaturally();
 			}
 			return;
 		}
 		if(templines[0].equalsIgnoreCase("License")){
 			if(CarMod.permission.has(event.getPlayer(), "minecars.license.create")){
 				templines[0] = green+"License";
 				templines[1] = green+"~~~~~~~~~~";
 				templines[2] = white+"Price:";
 				templines[3] = white+""+licenseCost;
 				event.getPlayer().sendMessage(String.format(dGreen+"[%s]"+white+" Driver's License Sign sucessfully registered!", this.mPlugin.getDescription().getName()));
 			}else{
 				event.getPlayer().sendMessage(dGreen+"[MineCars]"+white+ " You don't have the permission to create a Driver's License Sign.");
 				event.getBlock().breakNaturally();
 			}
 
 		}
 	}
 	}
 
 @EventHandler
 public void onBlockBreak(BlockBreakEvent event) throws IOException {
 	if (this.mPlugin.getConfig().getBoolean("UseFuelSystem")) {
 		if(event.getBlock().getType() == Material.WALL_SIGN || event.getBlock().getType() == Material.SIGN_POST || event.getBlock().getType() == Material.SIGN) {
 			Sign sign = (Sign) event.getBlock().getState();
 			if (sign.getLine(0).equalsIgnoreCase(ChatColor.GREEN + "Fuel Station")) {
 				if (CarMod.permission.has(event.getPlayer(), "minecars.fuelstation.destroy")) {
 					String temploc = Double.toString(event.getBlock().getLocation().getX()) + ";" + Double.toString(event.getBlock().getLocation().getY()) + ";" + Double.toString(event.getBlock().getLocation().getZ()) + ";" + event.getBlock().getWorld().getName(); 
 					this.destroySign(temploc);
 					event.getPlayer().sendMessage(String.format(dGreen+"[%s]"+white+" Fuel Station sucessfully unregistered!", this.mPlugin.getDescription().getName()));
 				} else {
 					event.getPlayer().sendMessage(String.format(dGreen+"[%s]"+white+" You don't have Permission to destroy a Fuel Station!", this.mPlugin.getDescription().getName()));
 					event.setCancelled(true);
 					sign.update();
 				}
 			}
 			else if(sign.getLine(0).equalsIgnoreCase(ChatColor.GREEN + "License")){
 				if(CarMod.permission.has(event.getPlayer(),"minecars.license.destroy")){
 					event.getPlayer().sendMessage(String.format(dGreen+"[%s]"+white+" Driver's License Sign sucessfully unregistered!", this.mPlugin.getDescription().getName()));
 				}else{
 					event.getPlayer().sendMessage(String.format(dGreen+"[%s]"+white+" You don't have Permission to destroy a Driver's License Sign!", this.mPlugin.getDescription().getName()));
 					event.setCancelled(true);
 					sign.update();
 				}
 			}
 		}
 	}
 }
 
 @EventHandler
 public void onBlockPlace(BlockPlaceEvent event) throws IOException {
 	//Shouldnt ever need this for sign interaction
 }
 
 
 
 
 @EventHandler
 public void onPlayerInteract (PlayerInteractEvent event) {
 	if (event.hasBlock()) {
 		
 		Action action = event.getAction();
 		Block b = event.getClickedBlock();
 		Player p = event.getPlayer();
 		Sign s;
 				
         if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK){
         	return;
         }
         
         if(b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST){
         	s = (Sign)b.getState();
         }else{
         	return;
         }
         
         if(action==Action.LEFT_CLICK_BLOCK){
         	return;
         }
         
 		if (s.getLine(0).equalsIgnoreCase(ChatColor.GREEN + "Fuel Station")) {
			if (CarMod.permission.has(p, "minecars.fuelstation.use"))
 				this.mFM.buyFuel(event.getPlayer());
 			else 
 				event.getPlayer().sendMessage(String.format(dGreen+"[%s]"+white+" You don't have Permission to buy Fuel at this Station!", this.mPlugin.getDescription().getName()));
 			return;
 		} 
 
 		if (s.getLine(0).equalsIgnoreCase(ChatColor.GREEN + "License")&&useLicense&&CarMod.permission.has(p, "minecars.license.use")) {
 			
 		}else{
 			return;
 		}
 		
 		if(CarMod.permission.has(p, "minecars.move")){
 			p.sendMessage(dGreen+ "[MineCars] "+ white+ "You already have a Driver's License!");
 			return;
 		}
 		if(CarMod.economy.getBalance(p.getName())<licenseCost){
 			p.sendMessage(dGreen+ "[MineCars License] "+ white+ "You can't afford a Driver's License.");
 			return;
 		}
 			
 		CarMod.economy.withdrawPlayer(p.getName(),(double)licenseCost);
 		
 		if(!CarMod.permission.has(p, "minecars.useCommands")){
 			CarMod.permission.playerAdd(p, "minecars.useCommands");
 		}
 		
 		if(!CarMod.permission.has(p, "minecars.move")){
 			CarMod.permission.playerAdd(p, "minecars.move");
 		}
 		
 		if(!CarMod.permission.has(p, "minecars.fuelstation.use")){
 			CarMod.permission.playerAdd(p, "minecars.fuelstation.use");
 		}
 		
 		if(!CarMod.permission.has(p, "minecars.create")){
 			CarMod.permission.playerAdd(p, "minecars.create");
 		}
 		
 		p.sendMessage(dGreen+ "MineCars License: "+ white+ "Congratulations, You now have a Driver's License!");
 			
 		mPlugin.setupPermissions();
 	}
 }
 
 
 
 public void loadSigns() throws IOException {
 	 
 	  BufferedReader br = new BufferedReader(new FileReader(this.inputFile));	  
 	  String strLine;
 	  while ((strLine = br.readLine()) != null)   {
 		  
 		  String[] tempcord = strLine.split(";");
 		  Sign tempsign = (Sign) this.mPlugin.getServer().getWorld(tempcord[3]).getBlockAt(new Location(this.mPlugin.getServer().getWorld(tempcord[3]), new Double(tempcord[0]), new Double(tempcord[1]), new Double(tempcord[2]))).getState();
 		 
 		  String[] lines = tempsign.getLines();
 		  lines[0] = ChatColor.GREEN + "Fuel Station";
 		
 		  lines[1] = white+" Costs: ";
 		  if (this.useEconomy) {
 			  lines[2] = white+new Integer(this.mPlugin.getConfig().getInt("priceperfuel")).toString() + " " + CarMod.economy.currencyNamePlural();
 		  } else {
 			  lines[2] = white+new Integer(this.mPlugin.getConfig().getInt("itemsperfuel")).toString() + " " + Material.getMaterial(this.mPlugin.getConfig().getInt("fuelitemid")).name();
 		  }
 		
 		  tempsign.update();
 		  
 		  
 		  
 	  }
 	  
 	  br.close();
 
 	  
 	
 	  
 }
 
 private boolean destroySign(String pLoctoRemove) throws IOException {
 	 /* FileInputStream fstream = new FileInputStream("signs.txt");
 	  FileWriter fwrt = new FileWriter("signs_temp.txt", true);
 	  BufferedWriter out = new BufferedWriter(fwrt);
 	  DataInputStream in = new DataInputStream(fstream);
 	  BufferedReader br = new BufferedReader(new InputStreamReader(in));
 	  String strLine;
 	  while ((strLine = br.readLine()) != null)   {
 		  if(strLine.equalsIgnoreCase(pLoctoRemove)) {
 			  continue;
 		  } else {
 			  out.write(strLine);
 			  out.newLine();
 		  }	  
 	  }
 	  in.close(); */
 	  
 	  
 
 	  
 
 	  BufferedReader reader = new BufferedReader(new FileReader(this.inputFile));
 	  BufferedWriter writer = new BufferedWriter(new FileWriter(this.tempFile, true));
 
 	  String lineToRemove = pLoctoRemove;
 	  String currentLine;
 
 	  while((currentLine = reader.readLine()) != null) {
 	      // trim newline when comparing with lineToRemove
 	      String trimmedLine = currentLine.trim();
 
 	      if(trimmedLine.equalsIgnoreCase(lineToRemove)) {
 
 	    	  continue;
 	      }
 	      
 	      writer.write(currentLine);
 	      writer.flush();
 	      writer.newLine();
 	  }
 	  writer.close();
 	  reader.close();
 
 	  this.inputFile.delete();
 	  boolean successful;
 	 return successful = this.tempFile.renameTo(this.inputFile);
 
 }
 
 private void addSign(Location pLoc, String pWorldName) throws IOException {
 	  BufferedWriter out = new BufferedWriter(new FileWriter(this.inputFile, true));
 	  
 	  out.write(Double.toString(pLoc.getX()) + ";" +Double.toString(pLoc.getY()) + ";" + Double.toString(pLoc.getZ()) + ";" + pWorldName);
 	  out.newLine();
 	  out.close();
 	  
 }
 
 public void useEconomy(boolean pValue) {
 	this.useEconomy = pValue;
 }
 	
 }
