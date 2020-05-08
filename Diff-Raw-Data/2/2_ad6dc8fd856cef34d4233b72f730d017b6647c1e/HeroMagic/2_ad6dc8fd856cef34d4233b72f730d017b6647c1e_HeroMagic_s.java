 package com.herocraftonline.heromagic;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.persistence.PersistenceException;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class HeroMagic extends JavaPlugin {
 	private static final Logger logger = Logger.getLogger("Minecraft.HeroMagic");
 	public CastManager castManager;
 	public Spells spells;
     
 	@Override
     public void onEnable() {
     	HeroMagicPlayerListener playerListener = new HeroMagicPlayerListener(this);
     	
 		PluginManager pm = getServer().getPluginManager();
 	    pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
 	    
 	    spells = new Spells(this);
 	    castManager = new CastManager(this);
 	    
 	    setupDatabase();
 	    
 		logger.info(this.getDescription().getFullName() + " enabled.");
     }
     
 	@Override
     public void onDisable() {
     	logger.info(this.getDescription().getFullName() + " disabled.");
     }
 	
 	@Override
     public boolean onCommand(CommandSender sender, Command  command, String label, String[]  args) {
 		String commandName = command.getName().toLowerCase();
 		
 		if (!anonymousCheck(sender)) {
 			Player player = (Player)sender;
 			
 			if (commandName.equals("cast")) {
 				if (args.length > 0) {
 					if (args[0].equalsIgnoreCase("spellbook"))
 					{
 						return setSpellBook(player, args);
 					}
 					else if (args[0].equalsIgnoreCase("cost"))
 					{
 						return showCost(player, args);
 					}
 					else if (args[0].equalsIgnoreCase("mark"))
 					{
 						return castMark(player);
 					}
 					else if (args[0].equalsIgnoreCase("blink"))
 					{
 						return castBlink(player);
 					}
 					else if (args[0].equalsIgnoreCase("recall"))
 					{
 						return castRecall(player);
 					}
 					else if (args[0].equalsIgnoreCase("gate"))
 					{
 						return castGate(player, args);
 		 			}
 				}
 				
 				sender.sendMessage(ChatColor.LIGHT_PURPLE + "Your magical words have no effect. Perhaps you need to pronounce them better...");
 			}
 			return true;
 		}
 		return false;
     }
 	
 	private void setupDatabase() {
 		try {
 			getDatabase().find(PlayerMark.class).findRowCount();
 			getDatabase().find(PlayerSpell.class).findRowCount();
 		} catch (PersistenceException ex) {
 			System.out.println("Installing database for " + getDescription().getName() + " due to first time usage");
 			installDDL();
 		}
 	}
 	
     @Override
     public List<Class<?>> getDatabaseClasses() {
         List<Class<?>> list = new ArrayList<Class<?>>();
         list.add(PlayerMark.class);
         list.add(PlayerSpell.class);
         return list;
     }
 	
 	/**
 	 * Checks if the sender is a player.
 	 * @param sender
 	 * @return
 	 */
 	public static boolean anonymousCheck(CommandSender sender) {
 		if (!(sender instanceof Player)) {
 			sender.sendMessage("Cannot execute that command, I don't know who you are!");
 			return true;
     	} else {
     		return false;
     	}
 	}
 	
 	/**
 	 * Sets a position where a spell can be learned.
 	 * @param player
 	 * @param args
 	 * @return
 	 */
     private boolean setSpellBook(Player player, String[] args) {
     	if(!player.isOp()) return true;
     	
     	if (args.length >= 2) {
 	    	Block book = player.getTargetBlock(null, 20);
 	    	
 	    	if(book.getTypeId() == 47) {
 	        	Spell spell = spells.getSpellByName(args[1]);
 	        	
 	        	if (spell != null) {
 	        		spell.setLocation(book.getLocation());
 	        		spells.save();
 	        		player.sendMessage(ChatColor.GREEN + "SpellBook set.");
 	        	} else {
 	        		player.sendMessage(ChatColor.RED + "The specified spell doesnt exist.");
 	        	}
 	    	} else {
 	    		player.sendMessage(ChatColor.RED + "You dont look at a bookshelf.");
 	    	}
     	} else {
     		player.sendMessage(ChatColor.RED + "You have to specify a spell.");
     	}
     	return true;
     }
 	
     /**
      * Sends the cost of a spell to the player.
      * @param player
      * @param args
      * @return
      */
 	private boolean showCost(Player player, String[] args) {
     	if(args.length >= 2) {
     		Spell spell = spells.getSpellByName(args[1]);
     		
     		if (spell != null) {
 	    		player.sendMessage(ChatColor.BLUE + "The spell " + spell.getName() + " costs " + spell.getReagent1_amount() + " of " + spell.getReagent1_name());
 	    		if (spell.getReagent2() != 0) {
	    			player.sendMessage(ChatColor.BLUE + "And costs " + spell.getReagent1_amount() + " of " + spell.getReagent1_name());
 	    		}
     		} else {
     			player.sendMessage(ChatColor.RED + "The specified spell doesnt exist.");
     		}
     	} else {
     		player.sendMessage(ChatColor.RED + "You have to specify a spell.");
     	}
     	return true;
 	}
 	
 	//TODO Add new spells here!
 	/**
 	 * #################################################
 	 * ############### SPELLS DOWN HERE! ###############
 	 * #################################################
 	 */
     
     public boolean castRecall(Player player) {
     	if(castManager.canCastSpell(player,"Recall")) {
     		if (!castManager.isOnCooldown(player,"Recall")) {
     			if (castManager.removeRegents(player, "Recall")) {
 	    			Location loc = castManager.getPlayerMark(player);
 	    			if (loc.getX() == 0.0 && loc.getY() == 0.0 && loc.getZ() == 0.0) {
 	    				player.sendMessage(ChatColor.RED + "You must first mark a location before you can Recall!");
 	    				return true;
 	    			}
 	    			player.teleport(loc);
 	    			castManager.startCooldown(player,"Recall");
 	    			player.sendMessage(ChatColor.BLUE + "You tear a hole in the fabric of space and time...");
 	    		} else {
 	    			player.sendMessage(ChatColor.RED + "You do not have the reagants to cast Recall");
 	    		}
     		} else {
     			player.sendMessage(ChatColor.LIGHT_PURPLE + "This spell Recall is on cooldown for " + castManager.getCoolDownRemaining(player, "Recall") + " more minutes");
     		}
     	} else {
     		player.sendMessage(ChatColor.RED + "You need to learn this spell first.");
     	}
     	return true;
     }
     
 	public boolean castBlink(Player player) {
     	if(castManager.canCastSpell(player,"Blink")) {
     		Block target = player.getTargetBlock(null, 20);
     		BlockFace face = target.getFace(player.getLocation().getBlock());
     		
     		if (target != null && castManager.getDistance(player, target) <= 20) {
 	    		if (!castManager.isOnCooldown(player,"Blink")) {
 	    			if (castManager.removeRegents(player, "Blink")) {
 			    		if (player.getWorld().getBlockTypeIdAt(target.getX(),target.getY()+1,target.getZ()) == 0 && player.getWorld().getBlockTypeIdAt(target.getX(),target.getY()+2,target.getZ()) == 0) {
 			    			player.sendMessage(ChatColor.BLUE + "You Cast Blink!");
 			    			player.teleport(new Location(player.getWorld(), target.getX()+.5, (double)target.getY()+1, target.getZ()+.5 ,player.getEyeLocation().getYaw(), player.getEyeLocation().getPitch()  ));
 			    			castManager.startCooldown(player,"Blink");
 			    			return true;
 			    		} else if (target.getTypeId() == 0 && player.getWorld().getBlockTypeIdAt(face.getModX(),face.getModY()+1,face.getModZ()) == 0) {
 			    			player.sendMessage(ChatColor.BLUE +"You cast blink");
 			    			player.teleport(new Location(player.getWorld(),face.getModX()+.5,face.getModY(),face.getModZ()+.5,player.getEyeLocation().getYaw(), player.getEyeLocation().getPitch()));
 			    			castManager.startCooldown(player,"Blink");
 			    			return true;
 			    		} else {
 			    			player.sendMessage(ChatColor.LIGHT_PURPLE + "There ss no place to stand at that location!");
 			    		}
 	    			} else {
 	    				player.sendMessage(ChatColor.RED + "You do not have the reagants to cast Blink!");
 	    			}	
 	    		} else {
 	    			player.sendMessage(ChatColor.LIGHT_PURPLE + "This spell Blink is on cooldown for " + castManager.getCoolDownRemaining(player, "Blink") + " more minutes");
 	    		}
 	    	} else {
 	    		player.sendMessage(ChatColor.LIGHT_PURPLE +"Your target is to far!");
 	    	}
     	} else {
     		player.sendMessage(ChatColor.RED + "You need to learn this spell first.");
     	}
     	return true;
     }
 	
 	public boolean castGate(Player player, String[] args) {
 		if(castManager.canCastSpell(player, "Gate")) {
 			if(!castManager.isOnCooldown(player, "Gate")) {
 				if (castManager.removeRegents(player, "Gate")) {
 	    			Location loc = player.getWorld().getSpawnLocation();
 	    			player.teleport(loc);
 	    			castManager.startCooldown(player, "Gate");
 	    			player.sendMessage(ChatColor.BLUE + "You focus your magic to return yourself to the Origin...");
 	    		} else {
 	    			player.sendMessage(ChatColor.RED +"You do not have the reagants to cast gate!");
 	    		}
     		} else {
     			player.sendMessage(ChatColor.LIGHT_PURPLE + "This spell Gate is on cooldown for " + castManager.getCoolDownRemaining(player, "Gate") + " more minutes");
     		}
 		} else {
 			player.sendMessage(ChatColor.RED + "You need to learn this spell first.");
 		}
 		return true;
 	}
 	
 	private boolean castMark(Player player) {
 		if(castManager.canCastSpell(player, "Mark")) {
 			if(!castManager.isOnCooldown(player, "Mark")) {
 				if (castManager.removeRegents(player, "Mark")) {
 					castManager.setPlayerMark(player);
 					player.sendMessage(ChatColor.BLUE + "You have marked a location for further use...");
 	    		} else {
 	    			player.sendMessage(ChatColor.RED + "You do not have the reagants to cast Mark!");
 	    		}
     		} else {
     			player.sendMessage(ChatColor.LIGHT_PURPLE + "This spell Mark is on cooldown for " + castManager.getCoolDownRemaining(player, "Mark") + " more minutes");
     		}
 		} else {
 			player.sendMessage(ChatColor.RED + "You need to learn this spell first.");
 		}
 		return true;
 	}
 }
 
