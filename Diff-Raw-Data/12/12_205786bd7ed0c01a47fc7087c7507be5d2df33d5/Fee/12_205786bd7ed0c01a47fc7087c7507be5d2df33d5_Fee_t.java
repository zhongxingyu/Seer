 package org.melonbrew.fee;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.material.Door;
 import org.bukkit.material.TrapDoor;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.melonbrew.fee.commands.YesCommand;
 import org.melonbrew.fee.listeners.*;
 
 public class Fee extends JavaPlugin {
 	private Logger log;
 	
 	private Economy economy;
 	
 	private Permission permission;
 	
 	private Set<Session> sessions;
 	
 	private Set<Material> supportedBlocks;
 	
 	public void onEnable(){
 		log = getServer().getLogger();
 		
 		sessions = new HashSet<Session>();
 		
 		supportedBlocks = new HashSet<Material>();
 		
 		addSupportedBlock(Material.WOODEN_DOOR);
 		addSupportedBlock(Material.TRAP_DOOR);
 		addSupportedBlock(Material.FURNACE);
 		addSupportedBlock(Material.BURNING_FURNACE);
 		addSupportedBlock(Material.CHEST);
 		addSupportedBlock(Material.FENCE_GATE);
 		
 		Phrase.init(this);
 		
 		if (!setupEconomy()){
 			log(Phrase.VAULT_HOOK_FAILED);
 			
 			getServer().getPluginManager().disablePlugin(this);
 			
 			return;
 		}
 		
 		new FeePlayerListener(this);
 		
 		new FeeBlockListener(this);
 		
 		getConfig().options().copyDefaults(true);
 		
 		getConfig().options().header("Fee Config - melonbrew.org\n" +
				"# serveraccount - An account for fees to go to. (Blank for none)\n" +
 				"# closespeed - How many milliseconds (1000 milliseconds is 1 second) before doors, trapdoors and gates auto close.\n" +
 				"# globalcommands - A command followed by it's cost. For all players.\n" +
 				"# groupcommands - Per group commands.");
 		
 		saveConfig();
 		
 		getCommand("fee").setExecutor(new FeeCommand(this));
 		
 		getCommand("yes").setExecutor(new YesCommand(this));
 	}
 	
 	public Sign getSign(Player player, Block block, boolean up){
 		Block signBlock = block.getRelative(up ? BlockFace.UP : BlockFace.DOWN);
 		
 		if (block.getState().getData() instanceof Door && up && signBlock.getType() != Material.SIGN){
 			Door door = (Door) block.getState().getData();
 			
 			signBlock = signBlock.getRelative(door.getFacing().getOppositeFace());
 		}
 		
 		if (signBlock == null || !(signBlock.getState() instanceof Sign)){
 			return null;
 		}
 		
 		if (player.hasPermission("fee.exempt")){
 			return null;
 		}
 		
 		Sign sign = (Sign) signBlock.getState();
 		
 		if (!isSignFee(sign.getLine(0))){
 			return null;
 		}
 		
 		return sign;
 	}
 	
 	public boolean isSignFee(String firstLine){
 		return ChatColor.stripColor(firstLine).equals(ChatColor.stripColor(Phrase.SIGN_START.parse()));
 	}
 	
 	private boolean isDoorClosed(Block block){
 		if (block.getType() == Material.TRAP_DOOR){
 			
 			TrapDoor trapdoor = (TrapDoor)block.getState().getData();
 			
 			return !trapdoor.isOpen();
 		} else{
 			byte data = block.getData();
 			
 			if ((data & 0x8) == 0x8){
 				block = block.getRelative(BlockFace.DOWN);
 				
 				data = block.getData();
 			}
 			
 			return ((data & 0x4) == 0);
 		}
 	}
 
 	public void openDoor(Block block){
 		if (block.getType() == Material.TRAP_DOOR){
 			BlockState state = block.getState();
 
 			TrapDoor trapdoor = (TrapDoor)state.getData();
 
 			trapdoor.setOpen(true);
 
 			state.update();
 		} else {
 			byte data = block.getData();
 
 			if ((data & 0x8) == 0x8){
 				block = block.getRelative(BlockFace.DOWN);
 
 				data = block.getData();
 			}
 			if (isDoorClosed(block)){
 				data = (byte) (data | 0x4);
 
 				block.setData(data, true);
 
 				block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 0);
 			}
 		}
 	}
 
 	public void closeDoor(Block block){
 		if (block.getType() == Material.TRAP_DOOR){
 			BlockState state = block.getState();
 
			TrapDoor trapdoor = (TrapDoor) state.getData();
 
 			trapdoor.setOpen(false);
 
 			state.update();
 		} else {
 			byte data = block.getData();
 
 			if ((data & 0x8) == 0x8){
 				block = block.getRelative(BlockFace.DOWN);
 
 				data = block.getData();
 			}
 			if (!isDoorClosed(block)) {
 				data = (byte) (data & 0xb);
 
 				block.setData(data, true);
 
 				block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 0);
 			}
 		}
 	}
 	
 	public void addSupportedBlock(Material type){
 		supportedBlocks.add(type);
 	}
 	
 	public boolean containsSupportedBlock(Material type){
 		for (Material material : supportedBlocks){
 			if (material == type){
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	public Set<Material> getSupportedBlocks(){
 		return supportedBlocks;
 	}
 	
 	public String getKey(Player player, String message){
 		message = message.toLowerCase();
 		
 		try {
 			String group = permission.getPrimaryGroup(player);
 			
 			ConfigurationSection groupCommands = getConfig().getConfigurationSection("groupcommands." + group);
 			
 			if (groupCommands != null){
 				Set<String> keys = groupCommands.getKeys(false);
 				
 				for (String key : keys){
 					if (message.startsWith(key.toLowerCase())){
 						return key;
 					}
 				}
 			}
 		}catch (UnsupportedOperationException e){
 			
 		}
 		
 		ConfigurationSection globalCommands = getConfig().getConfigurationSection("globalcommands");
 		
 		Set<String> keys = globalCommands.getKeys(false);
 		
 		for (String key : keys){
 			if (message.startsWith(key.toLowerCase())){
 				return key;
 			}
 		}
 		
 		return null;
 	}
 	
 	public double getKeyMoney(String key){
 		return getConfig().getDouble("globalcommands." + key);
 	}
 	
 	public Session getSession(Player player){
 		for (Session session : sessions){
 			if (session.getPlayer().equals(player)){
 				return session;
 			}
 		}
 		
 		return null;
 	}
 	
 	public void addSession(Session session){
 		sessions.add(session);
 	}
 	
 	public void removeSession(Player player){
 		for (Session session : new HashSet<Session>(sessions)){
 			if (session.getPlayer().equals(player)){
 				sessions.remove(session);
 			}
 		}
 	}
 	
 	public Set<Session> getSessions(){
 		return sessions;
 	}
 	
 	public void log(String message){
 		log.info("[Fee] " + message);
 	}
 	
 	public void log(Phrase phrase, String... args){
 		log(phrase.parse(args));
 	}
 
 	private boolean setupEconomy(){
 		economy = null;
 		
 		permission = null;
 		
 		if (getServer().getPluginManager().getPlugin("Vault") != null){
 			RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
 
 			if (economyProvider != null){
 				economy = economyProvider.getProvider();
 			}
 			
 			RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(Permission.class);
 			
 			if (permissionProvider != null){
 				permission = permissionProvider.getProvider();
 			}
 		}
 
 		return economy != null;
 	}
 	
 	public Economy getEconomy(){
 		return economy;
 	}
 	
 	public String getMessagePrefix(){
 		return ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Fee" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;
 	}
 	
 	public String getEqualMessage(String inBetween, int length){
 		return getEqualMessage(inBetween, length, length);
 	}
 	
 	public String getEqualMessage(String inBetween, int length, int length2){
 		String equals = getEndEqualMessage(length);
 		
 		String end = getEndEqualMessage(length2);
 		
 		return equals + ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + inBetween + ChatColor.DARK_GRAY + "]" + end;
 	}
 	
 	public String getEndEqualMessage(int length){
 		String message = ChatColor.GRAY + "";
 		
 		for (int i = 0; i < length; i++){
 			message += "=";
 		}
 		
 		return message;
 	}
 }
