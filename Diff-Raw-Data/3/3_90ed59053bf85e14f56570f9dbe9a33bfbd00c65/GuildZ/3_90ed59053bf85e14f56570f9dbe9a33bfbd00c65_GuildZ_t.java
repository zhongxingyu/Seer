 package me.bluejelly.main;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 import me.bluejelly.main.commands.CmdGuild;
 import me.bluejelly.main.configs.*;
 import me.bluejelly.main.getters.Guild;
 import me.bluejelly.main.getters.GuildPlayer;
 import me.bluejelly.main.listeners.*;
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 public class GuildZ extends JavaPlugin {
 	
 	public static Inventory inv;
 	
 	private PlayerListener PlayerListener;
 	private MobListener MobListener;
 	private SignListener SignListener;
 	
 	public static GuildConfig gC;
 	public static PlayerConfig pC;
 	public static ChunkConfig cC;
 	public static SignConfig sC;
 	
     public static Economy econ = null;
     
 	public static Map<String, Chunk> playerChunk = new HashMap<String, Chunk>();
 	public static Map<String, String> playerTerritory = new HashMap<String, String>();
 	
 	public static Map<Integer, String> guildName = new HashMap<Integer, String>();
 	
 	public static ConsoleCommandSender console;
 	
 	public void onEnable() {
 
 		gC = new GuildConfig(this);
 		pC = new PlayerConfig(this);
 		cC = new ChunkConfig(this);
 		sC = new SignConfig(this);
 		
 		inv = Bukkit.createInventory(null, 6*9);
 		
 		console = Bukkit.getServer().getConsoleSender();
 		console.sendMessage("Version " + ChatColor.GOLD + getDescription().getVersion() + ChatColor.WHITE + " enabled!");
 		
 		//CONFIG
 		getConfig().options().copyDefaults(true);
 		/*/////*/
 		getConfig().addDefault("plugin.useguildvault", true);
 		getConfig().addDefault("plugin.somethingelse", true);
 		/*/////*/
 		saveConfig();
 		//CONFIG END
 		
 		//COMMANDS START
 		getCommand("g").setExecutor(new CmdGuild(this));
 		getCommand("guild").setExecutor(new CmdGuild(this));
 		//COMMANDS END
 
 		//REGISTER LISTENERS
 		this.PlayerListener = new PlayerListener(this);
 		this.MobListener = new MobListener(this);
 		this.SignListener = new SignListener(this);
 		
 		getServer().getPluginManager().registerEvents(this.PlayerListener, this);
 		getServer().getPluginManager().registerEvents(this.MobListener, this);
 		getServer().getPluginManager().registerEvents(this.SignListener, this);
 		//REGISTER LISTENERS
 		
 	
 		
 		//CUSTOM_CONFIGS
 		GuildConfig.saveConfig();
 		PlayerConfig.saveConfig();
 		ChunkConfig.saveConfig();
 		SignConfig.saveConfig();
 		
 		File PluginDir = getDataFolder();
 		if (!PluginDir.exists())
 	    {
 			PluginDir.mkdir();
 	    }
 		
 		for(Player p: Bukkit.getOnlinePlayers()) {
 			playerChunk.put(p.getName(), p.getLocation().getChunk());
 		}
 		
 		schedules();
 		
 		signUpdate();
 		mountSpeed();
 		
         setupEconomy();
 		
 	}
 
 	public void onDisable() {
 
 		console.sendMessage("Version " + ChatColor.GOLD + getDescription().getVersion() + ChatColor.WHITE + " disabled!");
 		
 	}
 	
 	public void schedules() {
 		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 			@Override
 			public void run() {
 				for(Player p: Bukkit.getOnlinePlayers()) {
 					if(ChunkConfig.config.contains(""+p.getLocation().getChunk())) {
 						if(playerTerritory.containsKey(p.getName())) {
 							if(!playerTerritory.get(p.getName()).equals(ChunkConfig.config.getString(p.getLocation().getChunk()+".owner"))) {
 								playerTerritory.put(p.getName(), ChunkConfig.config.getString(p.getLocation().getChunk()+".owner"));
 								p.sendMessage(ChatColor.RED + ChunkConfig.config.getString(p.getLocation().getChunk()+".owner"));
 							}
 						} else {
 							playerTerritory.put(p.getName(), ChunkConfig.config.getString(p.getLocation().getChunk()+".owner"));
 							p.sendMessage(ChatColor.RED + ChunkConfig.config.getString(p.getLocation().getChunk()+".owner"));
 						}
 					} else {
 						if(playerTerritory.containsKey(p.getName())) {
 							if(!playerTerritory.get(p.getName()).equals("~Unclaimed territory~")) {
 								playerTerritory.put(p.getName(), "~Unclaimed territory~");
 								p.sendMessage(ChatColor.GREEN + "~Unclaimed territory~");
 							}
 						} else {
 							playerTerritory.put(p.getName(), "~Unclaimed territory~");
 							p.sendMessage(ChatColor.GREEN + "~Unclaimed territory~");
 						}
 					}
 				}
 			}
 		}, 20, 10);
 	}
 	
 	private boolean setupEconomy() {
         if (getServer().getPluginManager().getPlugin("Vault") == null) {
             return false;
         }
         RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
         if (rsp == null) {
             return false;
         }
         econ = rsp.getProvider();
         return econ != null;
     }
 	
 	public void signUpdate() {
 		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, 
 		new Runnable() {
 			public void run() {
 				Object[] signs = SignConfig.config.getKeys(false).toArray();
 				
 				for(int i=0; i < signs.length; i++) {
 			        World w = Bukkit.getServer().getWorld("world");
 
 			        int x = SignConfig.config.getInt(signs[i]+".x");
 			        int y = SignConfig.config.getInt(signs[i]+".y");
 			        int z = SignConfig.config.getInt(signs[i]+".z");
 			        Location loc = new Location(w, x, y, z);
 			        
 			        Block b = w.getBlockAt(loc);
 			        
 					if(b.getTypeId() == Material.SIGN_POST.getId() || b.getTypeId() == Material.WALL_SIGN.getId()) {
 						
 	                	Sign sign = (Sign) b.getState();
 	                	sign.setLine(0, ChatColor.DARK_BLUE + "[Guild Bank]");
 			        	
 			            for(Player player : b.getWorld().getPlayers())
 			            {
 			                Block pBlock = player.getLocation().getBlock();
 			 
 			                int xDist = Math.abs(pBlock.getX() - b.getX());
 			         
 			                int zDist = Math.abs(pBlock.getZ() - b.getZ());
 			         
 			                if(xDist <= 3 & zDist <= 3)
 			                {
 			                	if(GuildPlayer.isInGuild(player.getName())) {
 			                		int level = Guild.getLevel(GuildPlayer.getGuildName(player.getName()));
 			                		
 				                	sign.setLine(1, ChatColor.GOLD + GuildPlayer.getGuildName(player.getName()));
 				                	sign.setLine(2, ChatColor.DARK_AQUA + "Level: " + ChatColor.AQUA + level);
 				                	if(level >= 5) {
 				                		int size = ((level-4)*9);
 				                		if(size > (6*9)) {
 				                			size = 6*9;
 				                		}
 				                		sign.setLine(3, "Size: " + size);
 				                	} else {
 				                		sign.setLine(3, ChatColor.DARK_RED + "NO BANK");
 				                	}
 				                	
 			                	} else {
 				                	sign.setLine(1, ChatColor.DARK_RED + "NO");
 				                	sign.setLine(2, ChatColor.DARK_RED + "GUILD");
 				                	sign.setLine(3, ChatColor.DARK_RED + "FOUND");
 			                	}
 			                }
 			                sign.update();
 			            }
 			        } else {
 			        	b.setTypeId(Material.REDSTONE_BLOCK.getId());
 			        	
 			        	SignConfig.config.set(""+signs[i], null);
 						SignConfig.saveConfig();
 			        	
 			        }
 			    }
 			}
 		}, 20, 20);
 	}
 	
 	public void mountSpeed() {
 		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, 
 		new Runnable() {
 			public void run() {
 				for(Player p: Bukkit.getOnlinePlayers()) {
 				    if(p.isInsideVehicle()){
 				    	if(GuildPlayer.isInGuild(p.getName())) {
 				    		int level = Guild.getLevel(GuildPlayer.getGuildName(p.getName()));
 							if(level < 5) {
 								break;
 							}
 							int mountLevel = level/5;
 							if(p.getVehicle().getType() == EntityType.HORSE) {
 								LivingEntity entity = (LivingEntity) p.getVehicle();
 								entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 99, mountLevel));
 								entity.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 99, mountLevel*3));
 								entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 99, 255));
 							}
 						}
 				    }
 				}
 			}
 		}, 0, 100);
 	}
 	
 
 	
 }
