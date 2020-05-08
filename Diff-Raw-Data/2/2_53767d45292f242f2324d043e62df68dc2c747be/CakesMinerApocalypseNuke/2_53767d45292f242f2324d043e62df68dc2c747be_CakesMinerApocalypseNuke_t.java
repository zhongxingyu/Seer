 package me.cakenggt.CakesMinerApocalypse;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Date;
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Furnace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.inventory.FurnaceSmeltEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 
 public class CakesMinerApocalypseNuke implements Listener {
 	CakesMinerApocalypse p;
 	public CakesMinerApocalypseNuke(CakesMinerApocalypse plugin) {
 		p = plugin;
 	}
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void smeltEvent(FurnaceSmeltEvent event) {
 		//System.out.println("furnace smelt event");
 		if (event.getSource().getType() != Material.SNOW_BLOCK){
 			//System.out.println("not snow");
 			//System.out.println(event.getResult().getTypeId());
 			return;
 		}
 		double random = Math.random() * 10000.0;
 		//System.out.println(random);
 		if (random < 1){
 			//System.out.println("smelting succeed");
 			Furnace furnace = (Furnace) event.getBlock().getState();
 			Inventory furnaceInventory = furnace.getInventory();
 			furnaceInventory.setItem(2, new ItemStack(Material.SNOW, 1));
 			furnace.update(true);
 		}
 	}
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void nukeAugment(EntityExplodeEvent event) throws IOException{
 		//System.out.println("explode event");
 		List<Block> exploded = event.blockList();
 		int amount = 0;
 		for (Block b : exploded){
			if(b != null && b.getState() instanceof Chest){
 				//System.out.println("was a containerblock");
 			    Chest container = (Chest)b.getState();
 			    Inventory bInventory = container.getInventory();
 			    ItemStack[] contents = bInventory.getContents();
 			    for (ItemStack a : contents){
 			    	if (a != null){
 			    		if (a.getType() == Material.SNOW){
 			    			amount += a.getAmount();
 			    			//System.out.println(a.getAmount() + " snow");
 			    		}
 			    	}
 			    }
 			    if (bInventory.contains(Material.SNOW))
 			    	b.setType(Material.AIR);
 			}
 		}
 		if (amount == 0)
 			return;
 		int stacks = (int)(amount/64);
 		int leftOver = amount%64;
 		event.getLocation().getWorld().createExplosion(event.getLocation(), 32F * stacks);
 		for (int i = 0; i < stacks; i++){
 			craterWrite(event.getLocation());
 			craterTimeWrite(0L);
 		}
 		if (leftOver > 0){
 			craterWrite(event.getLocation());
 			craterTimeWrite(64L - leftOver);
 		}
 		System.out.println("Nuclear detonation of yield " + stacks + "." + leftOver + " " + "Mt! at " + event.getLocation().getX() + " " + event.getLocation().getZ());
 		List <Player> players = event.getLocation().getWorld().getPlayers();
 		String message = "";
 		for (Player p : players){
 			if (p.getLocation().getZ() > event.getLocation().getZ())
 				message = "You hear an explosion to the North";
 			if (p.getLocation().getZ() < event.getLocation().getZ())
 				message = "You hear an explosion to the South";
 			if (p.getLocation().getX() > event.getLocation().getX())
 				message = message.concat("West");
 			if (p.getLocation().getX() < event.getLocation().getX())
 				message = message.concat("East");
 			p.sendMessage(message);
 		}
 		this.p.loadCraters();
 		this.p.loadCraterTimes();
 	}
 	public static void craterWrite (Location location) throws IOException{
 		if (new File("plugins/CakesMinerApocalypse/").mkdirs())
 			System.out.println("crater file created");
 		File myFile = new File("plugins/CakesMinerApocalypse/craters.txt");
 		if (!myFile.exists()){
 			PrintWriter outputFile = new PrintWriter("plugins/CakesMinerApocalypse/craters.txt");
 			System.out.println("crater file created");
 			outputFile.close();
 		}
 		FileWriter fWriter = new FileWriter("plugins/CakesMinerApocalypse/craters.txt", true);
 		PrintWriter outputFile = new PrintWriter(fWriter);
 		outputFile.println(location.getWorld().getName() + " " + location.getX() + " " + location.getY() + " " + location.getZ());
 		outputFile.close();
 	}
 	public static void craterTimeWrite (Long offset) throws IOException{
 		if (new File("plugins/CakesMinerApocalypse/").mkdirs())
 			System.out.println("crater time file created");
 		File myFile = new File("plugins/CakesMinerApocalypse/craterTimes.txt");
 		if (!myFile.exists()){
 			PrintWriter outputFile = new PrintWriter("plugins/CakesMinerApocalypse/craterTimes.txt");
 			System.out.println("crater time file created");
 			outputFile.close();
 		}
 		FileWriter fWriter = new FileWriter("plugins/CakesMinerApocalypse/craterTimes.txt", true);
 		PrintWriter outputFile = new PrintWriter(fWriter);
 		java.util.Date now = new Date();
 		outputFile.println(now.getTime() - (offset*19293750L));
 		outputFile.close();
 	}
 }
