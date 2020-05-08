 package com.glen3b.plugin.invpotions;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 public class InventoryPotionEffects extends JavaPlugin {
 	
 	private int taskid;
 	
 	protected boolean nonstandardHelmet(PlayerInventory pi){
 		ItemStack h = pi.getHelmet();
 		return h != null && h.getType() != Material.LEATHER_HELMET && h.getType() != Material.GOLD_HELMET && h.getType() != Material.CHAINMAIL_HELMET && h.getType() != Material.IRON_HELMET && h.getType() != Material.DIAMOND_HELMET;
 	}
 	
 	@Override
 	public void onEnable(){
 		this.saveDefaultConfig();
 	    taskid = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 	    	
 	        @Override  
 	        public void run() {
 	            for(Player p : getServer().getOnlinePlayers()) {
 	            	Iterator<Entry<String, Object>> nondeepconfig = getConfig().getRoot().getValues(false).entrySet().iterator();
 	            	if(p != null){
 	            		PlayerInventory pi = p.getInventory();
 	            		ItemStack[] parmor = pi.getArmorContents();
 	            		while(nondeepconfig.hasNext()){
 	            			Entry<String, Object> entry = nondeepconfig.next();
 	            			String basekey = entry.getKey()+".";
 	            			String[] configvalues = new String[]{
 	            					getConfig().getString(basekey+"armor.helmet"),
 	            					getConfig().getString(basekey+"armor.chestplate"),
 	            					getConfig().getString(basekey+"armor.leggings"),
 	            					getConfig().getString(basekey+"armor.boots")};
 	            			List<ArmorConfigValueType> armorcfg = ArmorConfigValueType.populateList(configvalues);
 	            			int[] numbers = new int[]{-257,-257,-257,-257};
 	            			Material[] armor = new Material[]{
 	            					Material.getMaterial(configvalues[0]),
 	            					Material.getMaterial(configvalues[1]),
 	            					Material.getMaterial(configvalues[2]),
 	            					Material.getMaterial(configvalues[3])};
 	            			for(int i = 0; i < 4; i++){
 	            				boolean usenum = true;
 	            				try{
 	            					numbers[i] = Integer.parseInt(configvalues[i]);
 	            				}catch(NumberFormatException e){
 	            					usenum = false;
 	            				}
 	            				if(usenum){
 	            					armor[i] = Material.getMaterial(numbers[i]);
 	            				}
 	            			}
 	            			boolean armorvalid = true;
 	            			boolean inventoryvalid = true;
 	            			for(int i = 0; i < 4; i++){
 	            				switch(armorcfg.get(i)){
 		            			case Acknowledge:
 		            				ItemStack checking;
 		            				boolean useItemStack = false;
 		            				if(configvalues[i].split(":").length == 1){
 		            					checking = new ItemStack(Material.getMaterial(configvalues[i]));
 		        					}else{
 		        						useItemStack = true;
 		        						checking = new ItemStack(Material.getMaterial(configvalues[i].split(":")[0]), 1, Short.parseShort(configvalues[i].split(":")[1]));
 		        					}
 		            				if(!(useItemStack ? parmor[3-i].getType() == checking.getType() && parmor[3-i].getDurability() == checking.getDurability() : parmor[3-i].getType() == Material.getMaterial(configvalues[i]))){
 		            					armorvalid = false;
 		            				}
 		            				break;
 		            			case NonStandard:
 		            				if(!nonstandardHelmet(pi)){
 		            					armorvalid = false;
 		            				}
 		            				break;
 		            			case Ignore:
 		            				break;
 		            			}
 		            			}
 	            			List<String> criteria = getConfig().getStringList(basekey+"criteria");
 	            			for(int i = 0; i < criteria.size(); i++){
 	            				ItemStack checking;
 	            				boolean useItemStack = false;
 	            				if(criteria.get(i).split(":").length == 1){
 	            					checking = new ItemStack(Material.getMaterial(criteria.get(i)));
 	        					}else{
 	        						useItemStack = true;
 	        						checking = new ItemStack(Material.getMaterial(criteria.get(i).split(":")[0]), 1, Short.parseShort(criteria.get(i).split(":")[1]));
 	        					}
	            				if(checking != null && !(useItemStack ? pi.containsAtLeast(checking, 1) : pi.contains(Material.getMaterial(criteria.get(i))))){
 		            				inventoryvalid = false;
 		            			}
 		            		}
 	            			
 	            			String handitem = getConfig().getString(basekey+"handitem");
 	            			if(handitem != null){
 	            				ItemStack checking;
 	            				boolean useItemStack = false;
 	            				if(handitem.split(":").length == 1){
 	            					checking = new ItemStack(Material.getMaterial(handitem));
 	        					}else{
 	        						useItemStack = true;
 	        						checking = new ItemStack(Material.getMaterial(handitem.split(":")[0]), 1, Short.parseShort(handitem.split(":")[1]));
 	        					}
 	            				if(!(useItemStack ? pi.getItemInHand().getType() == checking.getType() && pi.getItemInHand().getDurability() == checking.getDurability() : pi.getItemInHand().getType() == Material.getMaterial(handitem))){
 		            				inventoryvalid = false;
 		            			}
 	            			}
 	            			
 	            			if(armorvalid && inventoryvalid && !p.hasPermission("invpotions.bypass") && (p.hasPermission("invpotions.potion."+entry.getKey()) || p.hasPermission("invpotions.potion.*")) ){
 	            				List<String> potioneffects = getConfig().getStringList(basekey+"effects");
 	            				for(int i = 0; i < potioneffects.size(); i++){
 		            				String[] components = potioneffects.get(i).split("::");
 		            				int level;
 		            				try{
 		            				level = Integer.parseInt(components[1])-1;
 		            				}catch(NumberFormatException n){
 		            					getLogger().log(Level.WARNING, "Your potion effect configuration has an invalid number as a level.");
 		            					break;
 		            				}catch(ArrayIndexOutOfBoundsException n){
 		            					getLogger().log(Level.WARNING, "Your potion effect configuration does not have a valid splitting character ('::') in one effect configuration.");
 		            					break;
 		            				}
 		            				PotionEffectType potionefc = PotionEffectType.getByName(components[0].toUpperCase());
 		            				try{
 		            				p.removePotionEffect(potionefc);
 		            				p.addPotionEffect(new PotionEffect(potionefc, 200, level));
 		            				}catch(NullPointerException n){
 		            					getLogger().log(Level.WARNING, "There appears to be an invalid potion effect in your config file.");
 		            				}
 			            		}
 	            			}
 		            		}
 	            		}}}
 	    }, 150L, 90L);
 	}
 	
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
     	if (cmd.getName().equalsIgnoreCase("invpotions")){
     		if(args.length > 1){
     			sender.sendMessage("§cToo many arguments.");
     			return false;
     		}if(args.length < 1){
     			sender.sendMessage("§cToo few arguments.");
     			return false;
     		}
     		if(args[0].equalsIgnoreCase("reload")){
     		    this.reloadConfig();
     		    sender.sendMessage("§aReloaded configuration.");
     			return true;
     		}else{
     			sender.sendMessage("§cUnrecognized subcommand.");
     			return false;
     		}
     	} else if (cmd.getName().equalsIgnoreCase("matchinv")) {
     		boolean getplayer = true;
     		if (!(sender instanceof Player)) {
     			if(args.length < 2){
     				sender.sendMessage("§cToo few arguments (console users must pass player name).");
     				return false;
     			}
     			getplayer = false;
     		}
     		if(args.length == 2){
     			getplayer = false;
     		}
     		if(args.length < 1){
     			sender.sendMessage("§cToo few arguments.");
 				return false;
     		}
     		if(args.length > 2){
 				sender.sendMessage("§cToo many arguments.");
 				return false;
 			}
     			Player target = (Player) sender;
     			if(!getplayer){
     				target = getServer().getPlayer(args[1]);
     			}
 				List<String> items = getConfig().getStringList(args[0]+".criteria");
 				String[] cfgvalues = new String[]{
 						getConfig().getString(args[0]+".armor.helmet"),
 						getConfig().getString(args[0]+".armor.chestplate"),
 						getConfig().getString(args[0]+".armor.leggings"),
 						getConfig().getString(args[0]+".armor.boots")
 						};
 				
 				Material[] armor = new Material[]{
 					Material.getMaterial(cfgvalues[0]),
 					Material.getMaterial(cfgvalues[1]),
 					Material.getMaterial(cfgvalues[2]),
 					Material.getMaterial(cfgvalues[3])
 				};
 				int[] numbers = new int[]{-257,-257,-257,-257};
     			for(int i = 0; i < 4; i++){
     				boolean usenum = true;
     				try{
     					numbers[i] = Integer.parseInt(cfgvalues[i]);
     				}catch(NumberFormatException e){
     					usenum = false;
     				}
     				if(usenum){
     					armor[i] = Material.getMaterial(numbers[i]);
     				}
     			}
     			
 				try{
 				for(String str : items){
 					if(str.split(":").length == 1){
 						target.getInventory().addItem(new ItemStack(Material.getMaterial(str)));
 					}else{
 						target.getInventory().addItem(new ItemStack(Material.getMaterial(str.split(":")[0]), 1, Short.parseShort(str.split(":")[1])));
 					}
 				}}catch(NullPointerException n){
 					sender.sendMessage("§cAn error occured.");
 					if(target == null){
 						sender.sendMessage("§cThe player you targeted couldn't be found.");
 					}
 					return true;
 				}catch(NumberFormatException nf){
 					sender.sendMessage("§cError parsing damage value in configuration.");
 					return true;
 				}
 				try{
 					ItemStack add;
 					if(ArmorConfigValueType.getType(cfgvalues[0]) != ArmorConfigValueType.Ignore){
 	    			if(cfgvalues[0].split(":").length == 1){
 						add = new ItemStack(armor[0]);
 					}else{
 						add = new ItemStack(armor[0], 1, Short.parseShort(cfgvalues[0].split(":")[1]));
 					}
 					target.getInventory().setHelmet(add);
 					}
 					if(ArmorConfigValueType.getType(cfgvalues[1]) != ArmorConfigValueType.Ignore){
 					if(cfgvalues[1].split(":").length == 1){
 						add = new ItemStack(armor[1]);
 					}else{
 						add = new ItemStack(armor[1], 1, Short.parseShort(cfgvalues[1].split(":")[1]));
 					}
 					target.getInventory().setChestplate(add);
 					}
 					if(ArmorConfigValueType.getType(cfgvalues[2]) != ArmorConfigValueType.Ignore){
 					if(cfgvalues[2].split(":").length == 1){
 						add = new ItemStack(armor[2]);
 					}else{
 						add = new ItemStack(armor[2], 1, Short.parseShort(cfgvalues[2].split(":")[1]));
 					}
 					target.getInventory().setLeggings(add);
 					}
 					if(ArmorConfigValueType.getType(cfgvalues[3]) != ArmorConfigValueType.Ignore){
 					if(cfgvalues[3].split(":").length == 1){
 						add = new ItemStack(armor[3]);
 					}else{
 						add = new ItemStack(armor[3], 1, Short.parseShort(cfgvalues[3].split(":")[1]));
 					}
 					target.getInventory().setBoots(add);
 				}}
 				catch(NullPointerException n){
 					sender.sendMessage("§cEither a bad material name or the potion doesn't exist.");
 					return true;
 				}catch(NumberFormatException nf){
 					sender.sendMessage("§cError parsing damage value in configuration.");
 					return true;
 				}
 				sender.sendMessage("§aMatched inventory of "+target.getDisplayName()+" to requirements of "+args[0]+".");
 				return true;
     	}
     	return false;
     }
 
 
 	
 	@Override
 	public void onDisable(){
 		this.getServer().getScheduler().cancelTask(taskid);
 	}
 
 }
