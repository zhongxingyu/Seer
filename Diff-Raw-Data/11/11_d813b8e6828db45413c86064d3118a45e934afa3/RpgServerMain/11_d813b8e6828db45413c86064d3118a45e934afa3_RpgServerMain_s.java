 package me.petterroea.rpgserver;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class RpgServerMain extends JavaPlugin {
 	private final RpgPlayerListener playerListener = new RpgPlayerListener(this);
 	static HashMap<String, DeadGuy> deathPeople = new HashMap<String, DeadGuy>();
 	static HashMap<String, Integer> bank = new HashMap<String, Integer>();
 	@Override
 	public void onDisable() {
 		save(bank, "bankContents.bnk");
 	}
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 	{
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 //		if(command.getName().equalsIgnoreCase("addnpc"))
 //		{
 //			if(sender instanceof Player)
 //			{
 //				Player player = (Player) sender;
 //				if(player.hasPermission("rpg.npc") || player.hasPermission("rpg.*"))
 //				{
 //					player.getWorld().spawnCreature(player.getLocation(), CreatureType.CAVE_SPIDER);
 //				}
 //				else
 //				{
 //					player.sendMessage(ChatColor.RED + "You don't have the permissions!");
 //				}
 //			}
 //			else
 //			{
 //				sender.sendMessage("Only logged in players can use this command");
 //			}
 //			return true;
 //		}
 		if(command.getName().equalsIgnoreCase("back"))
 		{
 			if(player == null)
 			{
 				sender.sendMessage("This command is not for the comsole!");
 			}
 			else
 			{
 				if(deathPeople.containsKey(player.getName()))
 				{
 					if(deathPeople.get(player.getName()).deathtime < (System.currentTimeMillis() - 60000))
 					{
 						player.sendMessage(ChatColor.RED + "You can't do that!");
 						deathPeople.remove(player.getName());
 					}
 					else
 					{
 						player.teleport(deathPeople.get(player.getName()).loc);
 						deathPeople.remove(player.getName());
 					}
 				}
 				else
 				{
 					player.sendMessage(ChatColor.RED + "You can't do that!");
 				}
 				return true;
 			}
 		}
 		else if(command.getName().equalsIgnoreCase("bank"))
 		{
 			if(player == null)
 			{
 				sender.sendMessage("This command is not for the comsole!");
 			}
 			else
 			{
 				if(args[0].equalsIgnoreCase("sell"))
 				{
 					if(args[1].equalsIgnoreCase("all"))
 					{
 						PlayerInventory inv = player.getInventory();
 						int gold = 0;
 						ItemStack[] contents = inv.getContents();
 						for(int i = 0; i < inv.getContents().length; i++)
 						{
 							if(contents[i] == null)
 							{
 								//Nothing
 							}
 							else if(inv.getContents()[i].getTypeId() == 266)//Gold
 							{
 								gold = gold + contents[i].getAmount();
 								contents[i] = null;
 							}
 						}
 						player.getInventory().setContents(contents);
 						player.sendMessage(ChatColor.GREEN + "You have " + gold + " gold ingots");
 						player.sendMessage(ChatColor.GREEN + "" + gold + " was banked");
 					}
 				}
 				else if(args[0].equalsIgnoreCase("info"))
 				{
					if(bank.containsKey(player.getName()))
 					{
 						player.sendMessage(ChatColor.YELLOW + "You have " + bank.get(player.getName()) + " coins!");
 					}
 					else
 					{
 						player.sendMessage(ChatColor.RED + "You don't have any bank info yet!");
 					}
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 	public void addToBank(String user, int amount)
 	{
 		if(bank.containsKey(user))
 		{
 			int money = bank.get(user);
 			bank.remove(user);
 			bank.put(user, money + amount);
 		}
 	}
     public void save(HashMap<String, Integer> pluginEnabled, String path)
     {
     	try{
     		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
     		oos.writeObject(pluginEnabled);
     		oos.flush();
     		oos.close();
     		//Handle I/O exceptions
     	}catch(Exception e){
     		e.printStackTrace();
     	}
     }
 
     @SuppressWarnings("unchecked")
 	public HashMap<String, Integer> load(String path) {
     	try{
     		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
     		Object result = ois.readObject();
     		//you can feel free to cast result to HashMap<Player,Boolean> if you know there's that HashMap in the file
     		return (HashMap<String, Integer>)result;
     	}catch(Exception e){
     		e.printStackTrace();
     	}
     	return null;
     }
 
 
 	@Override
 	public void onEnable() {
 		System.out.println("RPG Server Initalizing...");
 		PluginManager pm = this.getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
 		bank = load("bankContents.bnk");
 		pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Event.Priority.Normal, this);
 		System.out.println("Done!");
 		
 	}
 
 }
