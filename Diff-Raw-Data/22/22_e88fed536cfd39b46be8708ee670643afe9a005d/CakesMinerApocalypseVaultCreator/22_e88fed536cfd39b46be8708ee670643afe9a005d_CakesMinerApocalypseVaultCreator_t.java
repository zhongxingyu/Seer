 package me.cakenggt.CakesMinerApocalypse;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Date;
 import java.util.regex.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Furnace;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.world.ChunkLoadEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 
 
 
 public class CakesMinerApocalypseVaultCreator implements Listener {
 	CakesMinerApocalypse p;
 
 	final ItemStack lightBlock;
 	final List<ArrayList<ItemStack>> lootGroups;
 	final List<ItemStack> potionLoot;
 
 	final Random random;
 
 	@SuppressWarnings("unchecked")
 	public CakesMinerApocalypseVaultCreator(CakesMinerApocalypse plugin) {
 		p = plugin;
 
 		random = new Random();
 
 		lightBlock = codeName2ItemStack(plugin.getConfig().getString("shelter.lightBlock"));
 
 		List<String> enabledLootGroups = plugin.getConfig().getStringList("shelter.enabledLootGroups");
 		lootGroups = new ArrayList<ArrayList<ItemStack>>();
 		for (String groupName: enabledLootGroups) {
 			ArrayList<ItemStack> group = new ArrayList<ItemStack>();
 			for (String itemString: plugin.getConfig().getStringList("shelter.loot." + groupName)) {
 				group.add(codeName2ItemStack(itemString));
 			}
 			lootGroups.add(group);
 		}
 
 		potionLoot = new ArrayList<ItemStack>();
 		for (String itemString: plugin.getConfig().getStringList("shelter.potionLoot")) {
 			potionLoot.add(codeName2ItemStack(itemString));
 		}
 
 	}
 
     // Parse a material code string with optional damage value (ex: 35;11)
     public static ItemStack codeName2ItemStack(String codeName) {
         Pattern p = Pattern.compile("^(\\d+x)?([0-9a-z_]+)[;:/]?([\\d-]*)([+]?.*)$");
         Matcher m = p.matcher(codeName);
         int typeCode;
         short dmgCode;
 
         if (!m.find()) {
             throw new IllegalArgumentException("Invalid item code format: " + codeName);
         }
 
 		int count = 64;
 		if (m.group(1) != null) {
 			try {
 				count = Integer.parseInt(m.group(1).replace("x", ""));
 			} catch (NumberFormatException e) {
 				throw new IllegalArgumentException("Invalid item count: " + m.group(1) + " in " + codeName);
 			}
 		}
 
         // typeid
 		try {
 			typeCode = Integer.parseInt(m.group(2));
 		} catch (NumberFormatException e) {
 			Material material = Material.matchMaterial(m.group(2));
 			if (material == null) {
 				throw new IllegalArgumentException("Invalid item type: " + m.group(2) + " in " + codeName);
 			}
 			typeCode = material.getId();
 		}
 
         // ;damagevalue 
         if (m.group(3) != null && !m.group(3).equals("")) {
             dmgCode = Short.parseShort(m.group(3));
         } else {
             dmgCode = 0;
         }
 
         ItemStack item = new ItemStack(typeCode, count, dmgCode);
 
         // +enchantcode@enchantlevel...
         if (m.group(4) != null && !m.group(4).equals("")) {
             String[] parts = m.group(4).split("[+]");
 
             for (String part: parts) {
                 if (part.length() == 0) {
                     continue;
                 }
 
                 String[] idAndLevel = part.split("@");
                 if (idAndLevel.length != 2) {
                     throw new IllegalArgumentException("Invalid item code: " + codeName + ", enchantment spec: " + part);
                 }
                 int id, level;
                 try {
                     id = Integer.parseInt(idAndLevel[0]);
                     level = Integer.parseInt(idAndLevel[1]);
                 } catch (Exception e) {
                     throw new IllegalArgumentException("Invalid item code: " + codeName + ", enchantment id/level: " + part);
                 }
 
                 Enchantment ench = Enchantment.getById(id);
 
                 // Add unsafe, since plugins might want to (ab)use enchantments (compatibility with EnchantMore)
                 item.addUnsafeEnchantment(ench, level);
             }
         }
 
         return item;
     }
 
 	public static void setBlock(Block block, ItemStack itemStack) {
 		block.setTypeIdAndData(itemStack.getTypeId(), (byte)itemStack.getDurability(), false); 
 	}
 
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void vaultCreator(ChunkLoadEvent event) throws IOException {
 		//System.out.println("chunk load event");
 		if(!event.isNewChunk())
 			return;
 		//System.out.println("new chunk");
 		double randomA = Math.random() * (1 / this.p.getShelterChance());
 
 		Chunk chunk = event.getChunk();
 		int x = chunk.getX() * 16;
 		int z = chunk.getZ() * 16;
 		int y = chunk.getWorld().getHighestBlockYAt(x + 8, z + 2);
 		
 		double randomB = Math.random() * (1 / this.p.getCraterChance());
 		if (randomB <= 1) {
 			Location start = new Location(event.getWorld(), x + 8, y, z + 8);
 			System.out.println("Crater generated at " + start.getX() + " " + start.getZ());
 			start.getWorld().createExplosion(start, 32F, false);
 			craterWrite(start);
 			craterTimeWrite();
 			this.p.loadCraters();
 			this.p.loadCraterTimes();
 		}
 		
 		if (randomA <= 1) {
 			Location start = new Location(event.getWorld(), x + 8, y, z + 2);
 			placeShelter(start);
 		}
 	}
 
 	public void placeShelter(Location start) {
 		System.out.println("Fallout shelter generated at " + start.getX() + " " + start.getZ());


 		//start.getBlock().setType(Material.GLOWSTONE);
 		start = start.subtract(0, 1, 0);
 		start.getBlock().setType(Material.TRAP_DOOR);
 		double y = start.getY() - 2;
 		while (start.getBlock().getY() > y / 2) {
 			start.subtract(0, 1, 0);
 			//start = start.subtract(0, 1, 0);
 			start.getBlock().setType(Material.LADDER);
 			start.getBlock().setData((byte) 3);
 			//2, 4, 3
 		}
 		start.getBlock().getRelative(0, 0, 1).setType(Material.AIR);
 		start.getBlock().getRelative(0, 1, 1).setType(Material.AIR);
 		start.getBlock().getRelative(0, 0, 2).setType(Material.AIR);
 		start.getBlock().getRelative(0, 1, 2).setType(Material.AIR);
 		start.subtract(2, 0, -3);
 		start.subtract(2, 2, 2);
 		Location end = new Location(start.getWorld(), start.getX() + 8,
 				start.getY() + 7, start.getZ() + 11);
 		int xIteration = 0;
 		int yIteration = 0;
 		Block startBlock = start.getBlock();
 		while (startBlock.getY() <= end.getY()) {
 			while (startBlock.getX() <= end.getX()) {
 				while (startBlock.getZ() <= end.getZ()) {
 					startBlock.setTypeId(this.p.getConfig().getInt("shelter.buildingBlock", Material.BEDROCK.getId()));
 					startBlock = startBlock.getRelative(0, 0, 1);
 				}
 				xIteration++;
 				startBlock = start.getBlock().getRelative(xIteration,
 						yIteration, 0);
 			}
 			yIteration++;
 			xIteration = 0;
 			startBlock = start.getBlock().getRelative(0, yIteration, 0);
 		}
 		start.add(2, 2, 2);
 		start.getBlock().getRelative(2, 0, -1).setType(Material.AIR);
 		start.getBlock().getRelative(2, 1, -1).setType(Material.AIR);
 		start.getBlock().getRelative(2, 0, -2).setType(Material.AIR);
 		start.getBlock().getRelative(2, 1, -2).setType(Material.AIR);
 		startBlock = start.getBlock();
 		end = new Location(start.getWorld(), start.getX() + 4,
 				start.getY() + 3, start.getZ() + 7);
 		//System.out.println("start " + start.getX() + " " + start.getY() + " " + start.getZ());
 		//System.out.println("end " + end.getX() + " " + end.getY() + " " + end.getZ());
 		xIteration = 0;
 		yIteration = 0;
 		while (startBlock.getY() <= end.getY()) {
 			while (startBlock.getX() <= end.getX()) {
 				while (startBlock.getZ() <= end.getZ()) {
 					startBlock.setType(Material.AIR);
 					startBlock = startBlock.getRelative(0, 0, 1);
 				}
 				xIteration++;
 				startBlock = start.getBlock().getRelative(xIteration,
 						yIteration, 0);
 			}
 			yIteration++;
 			xIteration = 0;
 			startBlock = start.getBlock().getRelative(0, yIteration, 0);
 		}
 		setBlock(start.getBlock().getRelative(2, 4, 2), lightBlock);
 		setBlock(start.getBlock().getRelative(2, 4, 5), lightBlock);
 		setBlock(start.getBlock().getRelative(-1, 1, 1), lightBlock);
 		setBlock(start.getBlock().getRelative(-1, 1, 2), lightBlock);
 		setBlock(start.getBlock().getRelative(-1, 3, 1), lightBlock);
 		setBlock(start.getBlock().getRelative(-1, 3, 2), lightBlock);
 		setBlock(start.getBlock().getRelative(-1, 1, 5), lightBlock);
 		setBlock(start.getBlock().getRelative(-1, 1, 6), lightBlock);
 		setBlock(start.getBlock().getRelative(-1, 3, 5), lightBlock);
 		setBlock(start.getBlock().getRelative(-1, 3, 6), lightBlock);
 		setBlock(start.getBlock().getRelative(5, 1, 1), lightBlock);
 		setBlock(start.getBlock().getRelative(5, 1, 2), lightBlock);
 		setBlock(start.getBlock().getRelative(5, 3, 1), lightBlock);
 		setBlock(start.getBlock().getRelative(5, 3, 2), lightBlock);
 		setBlock(start.getBlock().getRelative(5, 1, 5), lightBlock);
 		setBlock(start.getBlock().getRelative(5, 1, 6), lightBlock);
 		setBlock(start.getBlock().getRelative(5, 3, 5), lightBlock);
 		setBlock(start.getBlock().getRelative(5, 3, 6), lightBlock);
 
 		start.getBlock().getRelative(-1, 0, 1).setType(Material.CHEST);
 		start.getBlock().getRelative(-1, 0, 2).setType(Material.CHEST);
 		start.getBlock().getRelative(-1, 2, 1).setType(Material.CHEST);
 		start.getBlock().getRelative(-1, 2, 2).setType(Material.CHEST);
 		start.getBlock().getRelative(-1, 0, 5).setType(Material.CHEST);
 		start.getBlock().getRelative(-1, 0, 6).setType(Material.CHEST);
 		start.getBlock().getRelative(-1, 2, 5).setType(Material.CHEST);
 		start.getBlock().getRelative(-1, 2, 6).setType(Material.CHEST);
 		start.getBlock().getRelative(5, 0, 1).setType(Material.CHEST);
 		start.getBlock().getRelative(5, 0, 2).setType(Material.CHEST);
 		start.getBlock().getRelative(5, 2, 1).setType(Material.CHEST);
 		start.getBlock().getRelative(5, 2, 2).setType(Material.CHEST);
 		start.getBlock().getRelative(5, 0, 5).setType(Material.CHEST);
 		start.getBlock().getRelative(5, 0, 6).setType(Material.CHEST);
 		start.getBlock().getRelative(5, 2, 5).setType(Material.CHEST);
 		start.getBlock().getRelative(5, 2, 6).setType(Material.CHEST);
 		Block[] chestArray = { start.getBlock().getRelative(-1, 0, 1),
 				start.getBlock().getRelative(-1, 2, 1),
 				start.getBlock().getRelative(-1, 0, 5),
 				start.getBlock().getRelative(-1, 2, 5),
 				start.getBlock().getRelative(5, 0, 1),
 				start.getBlock().getRelative(5, 2, 1),
 				start.getBlock().getRelative(5, 0, 5),
 				start.getBlock().getRelative(5, 2, 5), };
 		int i = 0;
 		int g = random.nextInt(lootGroups.size());
 		for (Block block : chestArray) {
 			// Each double chest gets a random group of loot
 			if ((i & 1) == 0) {
 				g = random.nextInt(lootGroups.size());
 			}
 			List<ItemStack> loot = lootGroups.get(g);
 
 			i += 1;
 
 			Chest chest = (Chest) block.getState();
 			Inventory chestInventory = chest.getInventory();
 			//ItemStack[] chestStack = new ItemStack[27];
 
 
 			for (int t = 0; t < chestInventory.getSize(); t++) {
 				int a = (int) (Math.random() * 64);
 				int b = (int) (Math.random() * 64);
 				if (a >= 54 && a <= 63)
 					a = a - 19;
 
 				if (a < loot.size()) {
 					ItemStack item = loot.get(a);
 					item = item.clone();
 					// note: if 0, will be infinite! must be 1+ - see https://github.com/mushroomhostage/exphc/issues/12
                     int count = (b % item.getAmount()) + 1;
 					item.setAmount(count);  // configured amount is maximum to allow
 
 					chestInventory.setItem(t, item);
 				}
 			}
 			chest.update(true);
 		}
 		Block bottom = start.getBlock().getRelative(0, 0, 4);
 		bottom.setTypeIdAndData(26, (byte) 8, false);
 		bottom.getRelative(0, 0, -1).setTypeIdAndData(26, (byte) 0, true);
 		bottom = start.getBlock().getRelative(2, 0, -2);
 		bottom.setTypeIdAndData(this.p.getConfig().getInt("shelter.doorBlockUpper", 71), (byte)this.p.getConfig().getInt("shelter.doorBlockUpperData", 0), false);
 		bottom.getRelative(0, 1, 0).setTypeIdAndData(this.p.getConfig().getInt("shelter.doorBlockLower", 71), (byte)this.p.getConfig().getInt("shelter.doorBlockLowerData", 8), true);
 		bottom = start.getBlock().getRelative(2, 1, -1);
 		bottom.setTypeIdAndData(this.p.getConfig().getInt("shelter.leverBlock", 69), (byte)1, false);
 		start.getBlock().getRelative(4, 0, 3).setType(Material.CHEST);
 		Chest chest = (Chest) start.getBlock().getRelative(4, 0, 3)
 				.getState();
 		Inventory chestInventory = chest.getInventory();
 		chestInventory.setItem(0, new ItemStack(Material.SPONGE, 1));
 		for (int t = 1; t < 27; t++) {
 			int a = (int) (Math.random() * 48);
 			int b = (int) (Math.random() * 64);
 
 			if (a < potionLoot.size()) {
 				ItemStack item = potionLoot.get(a).clone();
                 int count = (b % item.getAmount()) + 1;
 
 				item.setAmount(count);
 
 				chestInventory.setItem(t, item);
 			}
 		}
 		chest.update(true);
 		start.getBlock().getRelative(4, -1, 4).setType(Material.WATER);
 		start.getBlock().getRelative(0, 0, 7).setType(
 				Material.ENCHANTMENT_TABLE);
 		start.getBlock().getRelative(1, 0, 7).setType(
 				Material.BREWING_STAND);
 		start.getBlock().getRelative(2, 0, 7).setType(Material.WORKBENCH);
 		start.getBlock().getRelative(3, 0, 7).setType(Material.FURNACE);
 		Furnace furnace = (Furnace) start.getBlock().getRelative(3, 0, 7)
 				.getState();
 		Inventory furnaceInventory = furnace.getInventory();
 		furnaceInventory.setItem(1, new ItemStack(Material.COAL, 64));
 		furnace.update(true);
 		start.getBlock().getRelative(4, 0, 7).setType(Material.CAULDRON);
 		start.getBlock().getRelative(1, 0, 8).setType(Material.OBSIDIAN);
 		start.getBlock().getRelative(2, 0, 8).setType(Material.OBSIDIAN);
 		start.getBlock().getRelative(0, 1, 8).setType(Material.OBSIDIAN);
 		start.getBlock().getRelative(0, 2, 8).setType(Material.OBSIDIAN);
 		start.getBlock().getRelative(0, 3, 8).setType(Material.OBSIDIAN);
 		start.getBlock().getRelative(1, 4, 8).setType(Material.OBSIDIAN);
 		start.getBlock().getRelative(2, 4, 8).setType(Material.OBSIDIAN);
 		start.getBlock().getRelative(3, 1, 8).setType(Material.OBSIDIAN);
 		start.getBlock().getRelative(3, 2, 8).setType(Material.OBSIDIAN);
 		start.getBlock().getRelative(3, 3, 8).setType(Material.OBSIDIAN);
 		start.getBlock().getRelative(1, 1, 8).setType(Material.AIR);
 		start.getBlock().getRelative(1, 2, 8).setType(Material.AIR);
 		start.getBlock().getRelative(1, 3, 8).setType(Material.AIR);
 		start.getBlock().getRelative(2, 1, 8).setType(Material.AIR);
 		start.getBlock().getRelative(2, 2, 8).setType(Material.AIR);
 		start.getBlock().getRelative(2, 3, 8).setType(Material.AIR);
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
 	public static void craterTimeWrite () throws IOException{
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
 		outputFile.println(now.getTime() - 1234800000);
 		outputFile.close();
 	}
 }
