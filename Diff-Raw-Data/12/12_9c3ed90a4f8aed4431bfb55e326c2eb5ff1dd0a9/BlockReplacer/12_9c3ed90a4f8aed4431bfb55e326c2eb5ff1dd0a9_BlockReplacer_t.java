 package io.github.alekso56;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.Sound;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerItemHeldEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.Vector;
 
 public final class BlockReplacer extends JavaPlugin implements Listener{
 	public static int id;
 	ArrayList<String> Proclist = new ArrayList<String>();
 	public static Map<String, String> ST = new HashMap<String, String>(); // selected tool
 	public void onEnable(){
 		Bukkit.getPluginManager().registerEvents(this, this);
 		getConfig().options().copyDefaults(true);
		//getLogger().info("saved config");
 		saveConfig();
 		Proclist = loadArray(); 
		//getLogger().info("loaded arrayData!");
 		//yay for bukkit scheduling    /s
 	    if (getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
 	    public void run()
 	    {
 	        for(int x = 1; x < Proclist.size(); x = x+1){
 	    	  splitString(Proclist.get(x),x);
 	    	  }
 	        }
 	    },getConfig().getInt("Launch.NextBlockScan") * 20, getConfig().getInt("Launch.NextBlockScan") * 20) > 0) 
 	    {
 	     getLogger().info("Scheduled dbcheck with bukkit scheduler.");
 	    } else {
 	        getLogger().warning("Failed to schedule dbcheck with bukkit scheduler.");
 	       }
 	}
  
 	public void onDisable(){
 		saveArray(Proclist);
 		getLogger().info("Saved array data!");
 	}
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 		if(cmd.getName().equalsIgnoreCase("test")){
 			getLogger().info(sender.getName() + " activated spam!");
 			sender.sendMessage("here's your spam!");
 			return true;
 		}
 		return false; 
 	}
 	 public void saveArray(ArrayList<String> proclist2) {
      try {
         FileOutputStream fos = new FileOutputStream("blockReplacer.db");
         GZIPOutputStream gzos = new GZIPOutputStream(fos);
         ObjectOutputStream out = new ObjectOutputStream(gzos);
         out.writeObject(proclist2);
         out.flush();
         out.close();
      }
      catch (IOException e) {
          System.out.println(e); 
      }
   }
   
   public void splitString(String assetClasses,int index) {
 	  StringTokenizer stringtokenizer = new StringTokenizer(assetClasses, ":");
 	  if (stringtokenizer.hasMoreElements()) {
 			  int x = Integer.parseInt(stringtokenizer.nextToken());
 			  int y = Integer.parseInt(stringtokenizer.nextToken());
 			  int z = Integer.parseInt(stringtokenizer.nextToken());
 			  int Material = Integer.parseInt(stringtokenizer.nextToken());
 			  int timeStamps = Integer.parseInt(stringtokenizer.nextToken());
 	          //if timestamp - currenttime  = bigger than config value then
 			  int timeStamp = Integer.parseInt(cTime());
 			  int result = timeStamp - timeStamps;
			  //getLogger().info("Scanning "+index+" result:"+result+" TimeCheck: "+getConfig().getInt("Launch.CheckTime"));
 			  if(result > getConfig().getInt("Launch.CheckTime")){
 				  Proclist.remove(index);
 				  World world= getServer().getWorld("world");
 				  world.getBlockAt(x, y, z).setTypeId(Material, true);
 				  getLogger().info("set "+x+" "+y+" "+z+" "+Material+" And deleted: "+index);
 			  }
 	  }
 }
   
   private String joinString(int x2, int y2, int z2, int typeId, int i) {
 		String y = x2 + ":" +y2+":" +z2+ ":" +typeId+ ":"+i;
 		return y;
 	}
   
   private void AddToDb(String dbString){
 	getLogger().info(dbString + " is dbstring");
   	getLogger().info(Proclist.size() + " is tbP");
   	Proclist.add(dbString);
   }
 
 @SuppressWarnings("unchecked")
 public ArrayList<String> loadArray() {
       try {
         FileInputStream fis = new FileInputStream("blockReplacer.db");
         GZIPInputStream gzis = new GZIPInputStream(fis);
         ObjectInputStream in = new ObjectInputStream(gzis);
         ArrayList<String> input_array = (ArrayList<String>) in.readObject();
         in.close();
         return input_array;
       }
       catch (Exception e) {
     	  getLogger().info("Database not found, will save db on exit");
     	  saveArray(Proclist);
           return Proclist;
       }
   }
 	@EventHandler
     public void onBlockBreak(BlockBreakEvent event)
     {
         Block b = event.getBlock();
         Material b1 = b.getType();
         int timeStamp = Integer.parseInt(cTime());
         String dbString =  joinString(b.getX(),b.getY(),b.getZ(),b.getTypeId(),timeStamp);
         if (b1 == Material.LOG && ST.get(event.getPlayer().getName()) == "AXE" && !getConfig().getBoolean("Blocks.LOGS"))
         {
         	b.setTypeId(5);
        	short itemDamage = b.getData();
        	event.getPlayer().getInventory().addItem(new ItemStack(Material.LOG, 1, itemDamage));
         	AddToDb(dbString);
         	event.setCancelled(true);
         }
         else if (b1 == Material.LEAVES && !getConfig().getBoolean("Blocks.LEAVES") && ST.get(event.getPlayer().getName()) == "SHEARS")
         {
         	AddToDb(dbString);
         }
         else if (b1 == Material.IRON_ORE ||b1 == Material.GOLD_ORE ||b1 == Material.DIAMOND_ORE ||b1 == Material.LAPIS_ORE ||b1 == Material.COAL_ORE||b1 == Material.STONE||b1 == Material.EMERALD_ORE ||b1 == Material.GLOWING_REDSTONE_ORE || b1 == Material.REDSTONE_ORE && ST.get(event.getPlayer().getName()) == "PICKAXE"){
         	if(b1 == Material.IRON_ORE&& !getConfig().getBoolean("Blocks.IRON")){b.setType(Material.WOOL); b.setData((byte)8); event.getPlayer().getInventory().addItem(new ItemStack(Material.IRON_ORE, 1));}
             else if(b1 == Material.COAL_ORE && !getConfig().getBoolean("Blocks.COAL")){b.setType(Material.WOOL); b.setData((byte)15);event.getPlayer().getInventory().addItem(new ItemStack(Material.COAL, 1)); event.getPlayer().giveExp(1);}
             else if(b1 == Material.GOLD_ORE && !getConfig().getBoolean("Blocks.GOLD")){b.setType(Material.WOOL); b.setData((byte)4);event.getPlayer().getInventory().addItem(new ItemStack(Material.GOLD_ORE, 1));}
             else if(b1 == Material.DIAMOND_ORE && !getConfig().getBoolean("Blocks.DIAMOND")){b.setType(Material.WOOL); b.setData((byte)9);event.getPlayer().getInventory().addItem(new ItemStack(Material.DIAMOND, 1));  event.getPlayer().giveExp(5);}
             else if(b1 == Material.GLOWING_REDSTONE_ORE || b1 == Material.REDSTONE_ORE && !getConfig().getBoolean("Blocks.REDSTONE")){b.setType(Material.WOOL); b.setData((byte)14);event.getPlayer().getInventory().addItem(new ItemStack(Material.REDSTONE, 4)); event.getPlayer().giveExp(3);}
             else if(b1 == Material.EMERALD_ORE&& !getConfig().getBoolean("Blocks.EMERALD")){b.setType(Material.WOOL); b.setData((byte)5);event.getPlayer().getInventory().addItem(new ItemStack(Material.EMERALD, 1)); event.getPlayer().giveExp(6);}
             else if(b1 == Material.LAPIS_ORE&& !getConfig().getBoolean("Blocks.LAPIS")){b.setType(Material.WOOL); b.setData((byte)11);short itemDamage = 4;event.getPlayer().getInventory().addItem(new ItemStack(Material.INK_SACK, 4 , itemDamage)); event.getPlayer().giveExp(3);}
             else if(b1 == Material.STONE&& !getConfig().getBoolean("Blocks.STONE")){b.setType(Material.COBBLESTONE);event.getPlayer().getInventory().addItem(new ItemStack(Material.COBBLESTONE, 1));}
         	event.setCancelled(true);
         	AddToDb(dbString);
         }
         else if (b1 == Material.SAND || b1 == Material.CLAY || b1 == Material.GRAVEL && ST.get(event.getPlayer().getName()) == "SPADE")
         {
         	if(b1 == Material.SAND&& !getConfig().getBoolean("Blocks.SAND")){b.setType(Material.SANDSTONE);event.getPlayer().getInventory().addItem(new ItemStack(Material.SAND, 1));}
         	else if(b1 == Material.CLAY&& !getConfig().getBoolean("Blocks.CLAY")){b.setType(Material.WOOL); b.setData((byte)7);event.getPlayer().getInventory().addItem(new ItemStack(337, 4));}
         	else if(b1 == Material.GRAVEL&& !getConfig().getBoolean("Blocks.GRAVEL")){b.setType(Material.STAINED_CLAY); b.setData((byte)9);event.getPlayer().getInventory().addItem(new ItemStack(Material.GRAVEL, 1));}
         	event.setCancelled(true);
         	AddToDb(dbString);
         }
         else if(event.getPlayer().getGameMode() != GameMode.CREATIVE){event.setCancelled(true);}
      }
 
 	@EventHandler
     public void onPlayerItemHeld(PlayerItemHeldEvent event){
         Player p = event.getPlayer();
         ItemStack i = p.getInventory().getItem(event.getNewSlot());
         if(i == null){id = 0;}
         else{id = i.getTypeId();}
 		//getLogger().info(id + " is the typeID!");
         //getLogger().info(event.getNewSlot() + " is the itemslot!");
         if(id == 269 || id == 284 || id == 273 || id == 277||id == 256){ST.put(p.getName(), "SPADE");}
         else if(id == 257 || id == 274 || id == 270 || id == 285 ||id == 278){ST.put(p.getName(), "PICKAXE");}
         else if(id == 271 || id == 286 || id == 279 || id == 275 ||id == 258){ ST.put(p.getName(), "AXE");}
         else if(id == 268 || id == 267 || id == 272 || id == 276||id == 283){ST.put(p.getName(), "SWORD");}
         else if(id == 359){ST.put(p.getName(), "SHEARS");}
         else{ST.put(p.getName(), "NONE");} //player has no tool
                 }
     
     public static String cTime() {
     	Calendar cal = Calendar.getInstance();
     	SimpleDateFormat sdf = new SimpleDateFormat("hhmmss");
 		String currentTime = sdf.format(cal.getTime());
 		return currentTime;
     }
 	
      @EventHandler
      public void onBlockPlace(BlockPlaceEvent event){
     	 Player send = event.getPlayer();
     	 if (send.getInventory().contains(Material.matchMaterial(getConfig().getString("Launch.Item")))) {
              send.sendMessage("Launching ...");
              send.playSound(send.getLocation(), Sound.BLAZE_DEATH, 1.0F, 1.0F);
              send.setVelocity(new Vector(40, 10, 40));
              Vector dir = send.getLocation().getDirection();
              send.setVelocity(dir.multiply(8));
              send.setFallDistance(-150.0F);
            }
     	 if(event.getPlayer().getGameMode() != GameMode.CREATIVE){event.setCancelled(true);}
      }
     }
