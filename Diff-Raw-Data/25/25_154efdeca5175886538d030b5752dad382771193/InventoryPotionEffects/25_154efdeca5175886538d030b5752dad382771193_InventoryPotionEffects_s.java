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
	            	Iterator<Entry<String, Object>> nondeepconfig = getConfig().getDefaultSection().getValues(false).entrySet().iterator();
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
 		            				if(!(parmor[3-i].getType() == armor[i])){
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
 	            				Material checking = Material.getMaterial(criteria.get(i));
 		            			if(checking != null && !pi.contains(checking)){
 		            				inventoryvalid = false;
 		            			}
 		            		}
 	            			if(armorvalid && inventoryvalid && !p.hasPermission("invpotions.bypass")){
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
 		            				p.addPotionEffect(new PotionEffect(potionefc, 375, level));
 		            				}catch(NullPointerException n){
 		            					getLogger().log(Level.WARNING, "There appears to be an invalid potion effect in your config file.");
 		            				}
 			            		}
 	            			}
 		            		}
 	            		}}}
 	    }, 250L, 100L);
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
 				Material[] armor = new Material[]{
 					Material.getMaterial(getConfig().getString(args[0]+".armor.helmet")),
 					Material.getMaterial(getConfig().getString(args[0]+".armor.chestplate")),
 					Material.getMaterial(getConfig().getString(args[0]+".armor.leggings")),
 					Material.getMaterial(getConfig().getString(args[0]+".armor.boots"))
 				};
 				
 				try{
 				for(String str : items){
 					target.getInventory().addItem(new ItemStack(Material.getMaterial(str)));
 				}}catch(NullPointerException n){
 					sender.sendMessage("§cAn error occured.");
 					if(target == null){
 						sender.sendMessage("§cThe player you targeted couldn't be found.");
 					}
 					return false;
 				}
 				try{
 				target.getInventory().setHelmet(new ItemStack(armor[0]));
 				}catch(NullPointerException n){
 					
 				}
 				try{
 				target.getInventory().setChestplate(new ItemStack(armor[1]));
 				}catch(NullPointerException n){
 					
 				}
 				try{
 				target.getInventory().setLeggings(new ItemStack(armor[2]));
 				}catch(NullPointerException n){
 					
 				}
 				try{
 				target.getInventory().setBoots(new ItemStack(armor[3]));
 				}catch(NullPointerException n){
 					
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
