 package to.joe.j2mc.tournament;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import to.joe.j2mc.core.J2MC_Manager;
 import to.joe.j2mc.tournament.command.JoinCommand;
 import to.joe.j2mc.tournament.command.LeaveCommand;
 import to.joe.j2mc.tournament.command.admin.DuelCommand;
 
 public class J2MC_Tournament extends JavaPlugin implements Listener {
 
 	public enum GameStatus {
 		Fighting, //Two players are currently fighting. Listener should pay attention to the two players on the top of roundList
 		Idle, //Nobody is fighting. Listener should ignore all deaths.
 	}
 
 	private Location startPositionA; //The start position of the first player
 	private Location startPositionB; //The start position of the second player
 	private Location respawnLoc;
 	public ArrayList<Player> participants = new ArrayList<Player>(); //List of players who are still in the tournament
 	public boolean registrationOpen = false; //Are new players allowed to enter the tournament?
 	private List<Integer> itemList;
 	public ArrayList<Player> roundList = new ArrayList<Player>(); //Array of players who will fight. Should always have an even number of players
 	public GameStatus status = GameStatus.Idle;
 
 	private boolean isPowerOfTwo(int number) {
 		return (number != 0) && ((number & (number - 1)) == 0);
 	}
 
 	/*
 	 * Sets up the round by filling roundList with an even number of Players who are made to fight
 	 * If the number of players is a power of 2, then players fight in order
 	 * If it is not a power of 2, the playerList is scrambled (because participants isn't seeded) and enough pairs are picked to bring the number down to a power of 2.
 	 */
 	public void setupRound() {
 		Logger l = J2MC_Manager.getCore().getServer().getLogger();
 		if (roundList.isEmpty()) {
 			l.log(Level.INFO, "The roundlist is empty, proceding");
 			if (participants.size() == 1) {
 				l.log(Level.INFO, "Only one player is participating. They must be the winner.");
 				J2MC_Manager.getCore().getServer().broadcastMessage(ChatColor.RED + participants.get(0).getName() + ChatColor.AQUA + " is the last player standing and wins this tournament!");
 				participants.clear();
 			} else if (isPowerOfTwo(participants.size())) {
 				l.log(Level.INFO, "Participants is a power of 2. Adding all to roundlist.");
 				roundList.addAll(participants);
 			} else { //Not power of 2, so must eliminate until power of 2
 				l.log(Level.INFO, "The number of participants is not a power of 2.");
				int numberToEliminate = 0;
				while (isPowerOfTwo(participants.size()-numberToEliminate))
 					numberToEliminate++;
 				l.log(Level.INFO, "Must eliminate " + numberToEliminate + " participants to make power of 2");
 				for(int x = 0; x < numberToEliminate*2; x++) {
 					//select random person
 					Random playerPicker = new Random();
 					while(true) {
 						int playerNumber = playerPicker.nextInt(participants.size());
 						if (!roundList.contains(participants.get(playerNumber))) {
 							roundList.add(participants.get(playerNumber));
 							break;
 						}
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public void onEnable() {
 		//Read configuration
 		this.getConfig().options().copyDefaults(true);
 		this.saveConfig();
 
 		//Setup start positions
 		String world = this.getConfig().getString("startLocation.world");
 		startPositionA = new Location(this.getServer().getWorld(world), this.getConfig().getInt("startLocation.a.x"), this.getConfig().getInt("startLocation.a.y"), this.getConfig().getInt("startLocation.a.z"));
 		startPositionB = new Location(this.getServer().getWorld(world), this.getConfig().getInt("startLocation.b.x"), this.getConfig().getInt("startLocation.b.y"), this.getConfig().getInt("startLocation.b.z"));
 		respawnLoc = new Location(this.getServer().getWorld(world), this.getConfig().getInt("spawnLocation.x"), this.getConfig().getInt("spawnLocation.y"), this.getConfig().getInt("spawnLocation.z"));
 		startPositionA.setYaw(this.getConfig().getInt("startLocation.a.yaw"));
 		startPositionB.setYaw(this.getConfig().getInt("startLocation.b.yaw"));
 		respawnLoc.setYaw(this.getConfig().getInt("spawnLocation.yaw"));
 
 		//Setup inventory
 		itemList = this.getConfig().getIntegerList("inventory");
 
 		this.getServer().getPluginManager().registerEvents(this, this);
 		
 		this.getCommand("join").setExecutor(new JoinCommand(this));
 		this.getCommand("leave").setExecutor(new LeaveCommand(this));
 		this.getCommand("duel").setExecutor(new DuelCommand(this));
 		
 	}
 
 	@EventHandler
 	public void onDeath(PlayerDeathEvent event) {
 		if (status == GameStatus.Fighting) {
 			if (event.getEntity().equals(roundList.get(0))) {
 				J2MC_Manager.getCore().getServer().broadcastMessage(ChatColor.RED + roundList.get(0).getName() + ChatColor.AQUA + " is has been slain.");
 				J2MC_Manager.getCore().getServer().broadcastMessage(ChatColor.RED + roundList.get(1).getName() + ChatColor.AQUA + " wins this duel!");
 				roundList.get(1).teleport(respawnLoc);
 				participants.remove(roundList.get(0));
 				status = GameStatus.Idle;
 				roundList.remove(0);
 				roundList.remove(0);
 				return;
 			} else if (event.getEntity().equals(roundList.get(1))) {
 				J2MC_Manager.getCore().getServer().broadcastMessage(ChatColor.RED + roundList.get(1).getName() + ChatColor.AQUA + " is has been slain.");
 				J2MC_Manager.getCore().getServer().broadcastMessage(ChatColor.RED + roundList.get(0).getName() + ChatColor.AQUA + " wins this duel!");
 				roundList.get(0).teleport(respawnLoc);
 				participants.remove(roundList.get(1));
 				status = GameStatus.Idle;
 				roundList.remove(0);
 				roundList.remove(0);
 				return;
 			}
 		}
 	}
 	/*
 	 * Takes the first two players and first checks if they are both online
 	 * If both are offline, both are eliminated
 	 * If one player is offline, the other automatically wins
 	 */
 	public void fight() {
 		if (roundList.size() >= 2) {
 			//Check for AFK
 			if (!roundList.get(0).isOnline() && !roundList.get(1).isOnline()) {
 				J2MC_Manager.getCore().getServer().broadcastMessage(ChatColor.AQUA + "Both " + ChatColor.RED + roundList.get(0).getName() + ChatColor.AQUA + " and " + ChatColor.RED + roundList.get(1).getName() + ChatColor.AQUA + " are offline.");
 				J2MC_Manager.getCore().getServer().broadcastMessage(ChatColor.AQUA + "Both players are eliminated from the tournament!");
 				return;
 			}
 			if (!roundList.get(0).isOnline()) {
 				J2MC_Manager.getCore().getServer().broadcastMessage(ChatColor.RED + roundList.get(0).getName() + ChatColor.AQUA + " is offline.");
 				J2MC_Manager.getCore().getServer().broadcastMessage(ChatColor.RED + roundList.get(0).getName() + ChatColor.AQUA + " forfeits and " + ChatColor.RED + roundList.get(1).getName() + ChatColor.AQUA + " wins by default!");
 				return;
 			}
 			if (!roundList.get(1).isOnline()) {
 				J2MC_Manager.getCore().getServer().broadcastMessage(ChatColor.RED + roundList.get(1).getName() + ChatColor.AQUA + " is offline.");
 				J2MC_Manager.getCore().getServer().broadcastMessage(ChatColor.RED + roundList.get(1).getName() + ChatColor.AQUA + " forfeits and " + ChatColor.RED + roundList.get(0).getName() + ChatColor.AQUA + " wins by default!");
 				return;
 			}
 			//Both players are online, set status to fighting so the event handler will pay attention
 			status = GameStatus.Fighting;
 			//Give each player a proper inventory, heal them, teleport them to their positions
 			for (int x = 0; x < 2; x++) {
 				Player p = roundList.get(x);
 				Inventory pInventory = p.getInventory();
 				pInventory.clear(36);
 				pInventory.clear(37);
 				pInventory.clear(38);
 				pInventory.clear(39);
 				pInventory.clear(); //Working
 				for (Integer i : itemList) {
					pInventory.addItem(new ItemStack(i));
 				}
 				p.setHealth(p.getMaxHealth()); //Working
 				p.setFoodLevel(7); //Working
 				if (x == 0) {
 					p.teleport(startPositionA);
 				} else {
 					p.teleport(startPositionB);
 				}
 			}
 		}
 	}
 }
