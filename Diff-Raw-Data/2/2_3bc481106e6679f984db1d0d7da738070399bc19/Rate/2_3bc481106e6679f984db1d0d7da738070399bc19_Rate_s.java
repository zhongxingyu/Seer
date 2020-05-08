 /**
 This file is part of Like.
 
 Like is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 Like is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with Like.  If not, see http://www.gnu.org/licenses/.
 **/
 
 package me.endorphin8er.Rater;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.getspout.spoutapi.event.screen.ButtonClickEvent;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class Rate extends JavaPlugin implements Listener {
 	
 	public static Rate Plugin;
 	public final Logger logger = Logger.getLogger("Minecraft");
 	
 	// Define the GUI object
 	GUI gui;
 	
 	// Set up likes
 	HashMap<String, Integer> likes;// = new HashMap<Player, Integer>();
 	// Set up dislikes
 	HashMap<String, Integer> dislikes;// = new HashMap<Player, Integer>();
 	// Set up agrees
 	HashMap<String, Integer> agrees;// = new HashMap<Player, Integer>();
 	// Set up disagrees
 	HashMap<String, Integer> disagrees;// = new HashMap<Player, Integer>();
 	// Set up facepalms
 	HashMap<String, Integer> facepalms;// = new HashMap<Player, Integer>();
 	
 	// Set up GUI object
 	HashMap<Player, GUI> player_gui = new HashMap<Player, GUI>();
 	
 	String path = "plugins" + File.separator + "Like" + File.separator + "HashMaps" + File.separator;
 	
 	@Override
 	public void onDisable() {
 		try {
 			SLAPI.save(likes, new File(path + "likes.yml"));
 			SLAPI.save(dislikes, new File(path + "dislikes.yml"));
 			SLAPI.save(agrees, new File(path + "agrees.yml"));
 			SLAPI.save(disagrees, new File(path + "disagrees.yml"));
 			SLAPI.save(facepalms, new File(path + "facepalms.yml"));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		PluginDescriptionFile pdfFile = this.getDescription();
 		this.logger.info(pdfFile.getName() + " Has been Disabled!");
 	}
 	 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void onEnable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		this.logger.info(pdfFile.getName() + " Version " + pdfFile.getVersion() +  " Has been Enabled!");
 		
 		// Register event listeners
 		getServer().getPluginManager().registerEvents(this, this);
 		
 		try {
 			likes = (HashMap<String, Integer>) SLAPI.load(path + "likes.yml");
 			dislikes = (HashMap<String, Integer>) SLAPI.load(path + "dislikes.yml");
 			agrees = (HashMap<String, Integer>) SLAPI.load(path + "agrees.yml");
 			disagrees = (HashMap<String, Integer>) SLAPI.load(path + "disagrees.yml");
 			facepalms = (HashMap<String, Integer>) SLAPI.load(path + "facepalms.yml");
 		} catch (Exception e) {
 			//e.printStackTrace();
 			logger.warning("The file doesn't exsist! Will create it on server shut down!");
 		}
 		
 		if (likes == null) {
 			likes = new HashMap<String, Integer>();
 		}
 		if (dislikes == null) {
 			dislikes = new HashMap<String, Integer>();
 		}
 		if (agrees == null) {
 			agrees = new HashMap<String, Integer>();
 		}
 		if (disagrees == null) {
 			disagrees = new HashMap<String, Integer>();
 		}
 		if (facepalms == null) {
 			facepalms = new HashMap<String, Integer>();
 		}
 	}
 	
 	// The rank test method
 	public String rankcompare(HashMap<String, Integer> type, Player player, String s) {
 		
 		String top = player.getName();
 		
 		Player players[] = player.getWorld().getPlayers().toArray(new Player[0]);
 		
 		//System.out.println("Likes size = " + likes.size());
 	
 		
 		for (int i = 0; i < players.length; i++) {
 			//System.out.println("Player is: " + players[i].getName());
 				
 			if (i + 1 >= players.length) {
 				break;
 			}
 				
 			if (type.containsKey(players[i + 1].getName())) {
 				if (type.containsKey(players[i].getName())) {
 					if (type.get(players[i].getName()) > type.get(players[i + 1].getName())) {
 						top = players[i].getName();
 						// Increment the counter to skip the next person
 						i = i+1;
 					}
 					else {
 						// If the next player has a higher vote use him
 						top = players[i + 1].getName();
 					}
 				}
 				else {
 					// If the current player doesn't exist use next
 					top = players[i + 1].getName();
 				}
 			}
 			else {
 				// If the next player isn't in the HashMap use the current one
 				top = players[i].getName();
 			}
 		}
 		
 		return ChatColor.AQUA + "The Top Online Player is: "+ ChatColor.GREEN + top + ChatColor.AQUA + " with: " + ChatColor.GREEN + type.get(top) + ChatColor.AQUA + " " + s +"!";
 	}
 	
 	// This in theory frees up memory
 	@EventHandler
 	public void onDisconnect(PlayerQuitEvent event) {
 		if (player_gui.containsKey(event.getPlayer())) {
 			player_gui.remove(event.getPlayer());
 		}
 	}
 	
 	@EventHandler
 	public void buttonPress(ButtonClickEvent event){
 		
 		Player targetPlayer = null;
 		SpoutPlayer sp_target = null;
 		
 		if (player_gui.containsKey(event.getPlayer())) {
 			targetPlayer = player_gui.get(event.getPlayer()).isTargetPlayer();
 			sp_target = (SpoutPlayer) targetPlayer;
 		
 			// Like
 			if (player_gui.get(event.getPlayer()).isLikeButton(event.getButton())) {
 				event.getPlayer().getMainScreen().getActivePopup().close();
 				
 				// safety check
 				if (!likes.containsKey(targetPlayer.getName())) {
 					likes.put(targetPlayer.getName(), 0);
 				}
 				
 				int likes_count = likes.get(targetPlayer.getName()) + 1;
 				
 				likes.put(targetPlayer.getName(), likes_count);
 				
 				if (sp_target.isSpoutCraftEnabled()) {
 					// These notifications may not be longer than 26 characters
 					sp_target.sendNotification("Like", "You recieved a Like!", Material.GOLDEN_APPLE);
 				}
 			}
 			// DisLike
 			else if (player_gui.get(event.getPlayer()).isDisLikeButton(event.getButton())) {
 				event.getPlayer().getMainScreen().getActivePopup().close();
 			
 				// safety check
				if (!dislikes.containsKey(targetPlayer)) {
 					dislikes.put(targetPlayer.getName(), 0);
 				}
 				
 				int dislikes_count = dislikes.get(targetPlayer.getName()) + 1;
 				
 				dislikes.put(targetPlayer.getName(), dislikes_count);
 				
 				if (sp_target.isSpoutCraftEnabled()) {
 					// These notifications may not be longer than 26 characters
 					sp_target.sendNotification("Like", "You recieved a dis-Like!", Material.GOLDEN_APPLE);
 				}
 			}
 			// Agree
 			else if (player_gui.get(event.getPlayer()).isAgree(event.getButton())) {
 				event.getPlayer().getMainScreen().getActivePopup().close();
 				
 				// safety check
 				if (!agrees.containsKey(targetPlayer.getName())) {
 					agrees.put(targetPlayer.getName(), 0);
 				}
 				
 				int agrees_count = agrees.get(targetPlayer.getName()) + 1;
 				
 				agrees.put(targetPlayer.getName(), agrees_count);
 				
 				if (sp_target.isSpoutCraftEnabled()) {
 					// These notifications may not be longer than 26 characters
 					sp_target.sendNotification("Like", "You were Agreed!", Material.GOLDEN_APPLE);
 				}
 			}
 			// DisAgree
 			else if (player_gui.get(event.getPlayer()).isDisAgree(event.getButton())) {
 				event.getPlayer().getMainScreen().getActivePopup().close();
 				
 				// safety check
 				if (!disagrees.containsKey(targetPlayer.getName())) {
 					disagrees.put(targetPlayer.getName(), 0);
 				}
 				
 				int disagrees_count = disagrees.get(targetPlayer.getName()) + 1;
 				
 				disagrees.put(targetPlayer.getName(), disagrees_count);
 				
 				if (sp_target.isSpoutCraftEnabled()) {
 					// These notifications may not be longer than 26 characters
 					sp_target.sendNotification("Like", "You were Disagreed!", Material.GOLDEN_APPLE);
 				}
 			}
 			// Facepalm
 			else if (player_gui.get(event.getPlayer()).isFacePalm(event.getButton())) {
 				event.getPlayer().getMainScreen().getActivePopup().close();
 			
 				// safety check
 				if (!facepalms.containsKey(targetPlayer.getName())) {
 					facepalms.put(targetPlayer.getName(), 0);
 				}
 				
 				int facepalms_count = facepalms.get(targetPlayer.getName()) + 1;
 				
 				facepalms.put(targetPlayer.getName(), facepalms_count);
 				
 				if (sp_target.isSpoutCraftEnabled()) {
 					// These notifications may not be longer than 26 characters
 					sp_target.sendNotification("Like", "You recieved a FacePalm!", Material.GOLDEN_APPLE);
 				}
 			}
 		}
 	}
 	
 	@EventHandler
 	public void playerInteract(PlayerInteractEntityEvent event) {
 		if (event.getRightClicked() instanceof Player) {
 			gui = new GUI((SpoutPlayer) event.getPlayer(), (Player) event.getRightClicked(), this, this);
 			player_gui.put(event.getPlayer(), gui);
 		}
 	}
 			
 	// Public like return
 	public int Likes(Player player) {
 		// Safety check
 		if (!likes.containsKey(player.getName())) {
 			likes.put(player.getName(), 0);
 		}
 		return likes.get(player.getName());
 	}
 	
 	// Public dislike return
 	public int DisLikes(Player player) {
 		// Safety check
 		if (!dislikes.containsKey(player.getName())) {
 			dislikes.put(player.getName(), 0);
 		}
 		return dislikes.get(player.getName());
 	}
 	
 	// Public agree return
 	public int Agrees(Player player) {
 		// Safety check
 		if (!agrees.containsKey(player.getName())) {
 			agrees.put(player.getName(), 0);
 		}
 		return agrees.get(player.getName());
 	}
 	
 	// Public disagrees return
 	public int DisAgrees(Player player) {
 		// Safety check
 		if (!disagrees.containsKey(player.getName())) {
 			disagrees.put(player.getName(), 0);
 		}
 		return disagrees.get(player.getName());
 	}
 	
 	// Public facepalm return
 	public int Facepalms(Player player) {
 		// Safety check
 		if (!facepalms.containsKey(player.getName())) {
 			facepalms.put(player.getName(), 0);
 		}
 		return facepalms.get(player.getName());
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		Player player = (Player) sender;
 		
 		/*
 		 * On Like Command
 		 */
 		
 		if(commandLabel.equalsIgnoreCase("like")){
 			if(args.length == 0){
 				
 				player.sendMessage("----" + ChatColor.GREEN + "Like Help" + ChatColor.WHITE + "----");
 				player.sendMessage(ChatColor.AQUA + "/like" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows This Screen!");
 				player.sendMessage(ChatColor.AQUA + "like rank" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows The Player With The Most Likes");
 				player.sendMessage(ChatColor.AQUA + "/like <player's name>" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Sends A Message To The Player That You Like Them");
 				player.sendMessage(ChatColor.AQUA + "/like amount" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows The Amount Of Likes You Have!");
 				player.sendMessage(ChatColor.AQUA + "/facepalm" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows Facepalm Commands!");
 				player.sendMessage(ChatColor.AQUA + "/dislike" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows Dislike Commands!");
 				player.sendMessage(ChatColor.AQUA + "/agree" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows 'Agree' Commands!");
 				player.sendMessage(ChatColor.AQUA + "/disagree" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows 'Disagree' Commands!");
 			
 				return true;
 			}
 			if(args.length == 1){
 				if(player.getServer().getPlayer(args[0]) !=null){
 					Player targetPlayer = player.getServer().getPlayer(args[0]);
 					if(targetPlayer.isOp()){
 						// Safety check for null pointers
 						if (!likes.containsKey(targetPlayer.getName())) {
 							likes.put(targetPlayer.getName(), 0);
 						}
 						
 						int likes_count = likes.get(targetPlayer.getName()) + 1;
 						
 						// Increment the target player's like by 1
 						likes.put(targetPlayer.getName(), likes_count);
 						
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Have A Like From " + ChatColor.DARK_AQUA + player.getName());
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Now Have " + likes.get(targetPlayer.getName()) + " Likes!");
 					}
 					else{
 						// Safety check for null pointers
 						if (!likes.containsKey(player.getName())) {
 							likes.put(player.getName(), 0);
 						}
 						
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Have A Like From " + ChatColor.DARK_AQUA + player.getName());
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Now Have " + likes.get(targetPlayer.getName()) + " Likes!");
 					}
 					
 					return true;
 				}
 				if(player.isOp()){
 					// Amount parameter (Shows the amount of likes you have)
 					if(args[0].equalsIgnoreCase("amount")){
 						
 						// Safety check for null pointers
 						if (!likes.containsKey(player.getName())) {
 							likes.put(player.getName(), 0);
 						}
 						// If likes are 0
 						if (likes.get(player.getName()) == 0) {
 							player.sendMessage(ChatColor.GREEN + "You have no Likes At The Moment");
 						}
 						// If likes are more than 0
 						else {
 							player.sendMessage(ChatColor.AQUA + "You Have " + likes.get(player.getName()) + " Likes!");
 						}
 						
 						return true;
 						
 					}
 					// Undo a like
 					else if(args[0].equalsIgnoreCase("done")){
 						// Safety check
 						if (!likes.containsKey(player.getName())) {
 							likes.put(player.getName(), 0);
 						}
 						
 						if (likes.get(player.getName()) == 0) {
 							player.sendMessage(ChatColor.DARK_RED + "You have No Likes!");
 						}
 						else {
 							int likes_count = likes.get(player.getName()) -1;
 							
 							likes.put(player.getName(), likes_count);
 							player.sendMessage(ChatColor.BLUE + "You Have " + likes.get(player.getName()) + " Likes Left!");
 						}
 						
 						return true;
 					}
 					
 					else if (args[0].equalsIgnoreCase("rank")) {					
 						player.sendMessage(rankcompare(likes, player, "Likes"));
 						return true;
 					}
 				}
 			}
 		}
 		/*
 		 * on Facepalm command		
 		 */
 		if(commandLabel.equalsIgnoreCase("facepalm")){
 			if(args.length == 0){
 					player.sendMessage("----" + ChatColor.GREEN + "Facepalm Help" + ChatColor.WHITE + "----");
 					player.sendMessage(ChatColor.AQUA + "/facepalm" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows This Screen!");
 					player.sendMessage(ChatColor.AQUA + "/facepalm <player's name>" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Sends A Message To The Player That You facepalmed Them");
 					player.sendMessage(ChatColor.AQUA + "/facepalm rank" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows The Player With The Most Facepalms");
 					player.sendMessage(ChatColor.AQUA + "/facepalm amount" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows The Amount Of Facepalms You Have!");
 			
 					return true;
 			}
 			if(args.length == 1){
 				if(player.getServer().getPlayer(args[0]) !=null){
 					Player targetPlayer = player.getServer().getPlayer(args[0]);
 					if(targetPlayer.isOp()){
 						
 						// Safety check
 						if (!facepalms.containsKey(targetPlayer.getName())) {
 							facepalms.put(targetPlayer.getName(), 0);
 						}
 						
 						int facepalms_count = facepalms.get(targetPlayer.getName()) + 1;
 						
 						facepalms.put(targetPlayer.getName(), facepalms_count);
 									
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Have A Facepalm From " + ChatColor.DARK_AQUA + player.getName());
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Now Have " + facepalms.get(targetPlayer.getName()) + " Facepalms");
 					}
 					else {
 						// Safety check
 						if (!facepalms.containsKey(targetPlayer.getName())) {
 							facepalms.put(targetPlayer.getName(), 0);
 						}
 						
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Have A Facepalm From " + ChatColor.DARK_AQUA + player.getName() + ChatColor.GREEN + "He is not an OP");
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Now Have " + facepalms.get(targetPlayer.getName()) + " Facepalms!");
 					}
 					
 					return true;
 				}
 				if(player.isOp()){
 					if(args[0].equalsIgnoreCase("done")){
 						
 						// Safety check
 						if (!facepalms.containsKey(player.getName())) {
 							facepalms.put(player.getName(), 0);
 						}
 						
 						if (facepalms.get(player.getName()) == 0) {
 							player.sendMessage(ChatColor.DARK_RED + "You have no Facepalms!");
 						}
 						else {
 							
 							int facepalms_count = facepalms.get(player.getName()) -1;
 							
 							facepalms.put(player.getName(), facepalms_count);
 							player.sendMessage(ChatColor.BLUE + "You have " + facepalms.get(player.getName()) + " Facepalms!");
 						}
 						
 						return true;
 									
 					}
 					// Amount
 					else if (args[0].equalsIgnoreCase("amount")){
 						// Safety check
 						if (!facepalms.containsKey(player.getName())) {
 							facepalms.put(player.getName(), 0);
 						}
 						
 						if (facepalms.get(player.getName()) == 0) {
 							player.sendMessage(ChatColor.BLUE + "You have no Facepalms!");
 						}
 						else {
 							player.sendMessage(ChatColor.BLUE + "You have " + facepalms.get(player.getName()) + " Facepalms!");
 						}
 						
 						return true;
 					}
 					else if (args[0].equalsIgnoreCase("rank")) {					
 						player.sendMessage(rankcompare(facepalms, player, "Facepalms"));
 						return true;
 					}
 				}
 			}
 		}
 		/*
 		 * On Dislike Command
 		 */
 		if(commandLabel.equalsIgnoreCase("dislike")) {
 			if(args.length == 0){
 				player.sendMessage("----" + ChatColor.GREEN + "Dislike Help" + ChatColor.WHITE + "----");
 				player.sendMessage(ChatColor.AQUA + "/dislike" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows This Screen!");
 				player.sendMessage(ChatColor.AQUA + "/dislike amount" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows The Amount Of Dislikes You Have");
 				player.sendMessage(ChatColor.AQUA + "/dislike rank" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows The Player With The Most Dislikes");
 				player.sendMessage(ChatColor.AQUA + "/dislike <player's name>" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Sends A Message To The Player That You Disliked Them");
 			
 				return true;
 			}
 			if(args.length == 1){
 				if(player.getServer().getPlayer(args[0]) !=null){
 					// Set the target player to the argument
 					Player targetPlayer = player.getServer().getPlayer(args[0]);
 					if(targetPlayer.isOp()){
 						
 						if (!dislikes.containsKey(targetPlayer.getName())) {
 							dislikes.put(targetPlayer.getName(), 0);
 						}
 						
 						int dislikes_count = dislikes.get(targetPlayer.getName()) +1;
 						
 						dislikes.put(targetPlayer.getName(), dislikes_count);
 						
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Have A Dislike From " + ChatColor.DARK_AQUA + player.getName());
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Now Have " + dislikes.get(targetPlayer.getName()) + " Dislikes");
 					}
 					else {
 						// Safety check
 						if (!dislikes.containsKey(targetPlayer.getName())) {
 							dislikes.put(targetPlayer.getName(), 0);
 						}
 						
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Have A Dislike From " + ChatColor.DARK_AQUA + player.getName() + ChatColor.GREEN + " He is not an OP");
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Now Have " + dislikes.get(targetPlayer.getName()) + " Dislikes!");
 					}
 					
 					return true;
 				}
 				if(player.isOp()){
 					if(args[0].equalsIgnoreCase("amount")){
 						
 						// Safety check
 						if (!dislikes.containsKey(player.getName())) {
 							dislikes.put(player.getName(), 0);
 						}
 						
 						if (dislikes.get(player.getName()) == 0) {
 							player.sendMessage(ChatColor.GREEN + "You have No Dislikes At The Moment");
 						}
 						else {
 							player.sendMessage(ChatColor.AQUA + "You Have " + dislikes.get(player.getName()) + " Dislikes!");
 						}
 						
 						return true;
 					}
 					else if(args[0].equalsIgnoreCase("done")){
 						
 						// Safety check
 						if (!dislikes.containsKey(player.getName())) {
 							dislikes.put(player.getName(), 0);
 						}
 						
 						if (dislikes.get(player.getName()) == 0) {
 							player.sendMessage(ChatColor.GREEN + "You have No Dislikes At The Moment");
 						}
 						else {
 							// Subtract a dislike
 							int dilikes_count = dislikes.get(player.getName()) -1;
 							
 							dislikes.put(player.getName(), dilikes_count);
 							player.sendMessage(ChatColor.AQUA + "You Have " + dislikes.get(player.getName()) + " Dislikes!");
 						}
 						
 						return true;
 
 					}
 					else if (args[0].equalsIgnoreCase("rank")) {					
 						player.sendMessage(rankcompare(dislikes, player, "Dislikes"));
 						return true;
 					}
 				}
 			}
 		}
 		/*
 		 * On agree command
 		 */
 		if(commandLabel.equalsIgnoreCase("agree")){
 			if(args.length == 0){
 				player.sendMessage("----" + ChatColor.GREEN + "Agree Help" + ChatColor.WHITE + "----");
 				player.sendMessage(ChatColor.AQUA + "/agree" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows This Screen!");
 				player.sendMessage(ChatColor.AQUA + "/agree amount" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows The Amount Of Agrees You Have");
 				player.sendMessage(ChatColor.AQUA + "/agree rank" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows The Player With The Most Agrees");
 				player.sendMessage(ChatColor.AQUA + "/agree <player's name>" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Sends A Message To The Player That You Agree With Them");
 			
 				return true;
 			}
 			if(args.length == 1){
 				if(player.getServer().getPlayer(args[0]) !=null){
 					Player targetPlayer = player.getServer().getPlayer(args[0]);
 					if(targetPlayer.isOp()){
 						
 						if (!agrees.containsKey(targetPlayer.getName())) {
 							agrees.put(targetPlayer.getName(), 0);
 						}
 						
 						int agrees_count = agrees.get(targetPlayer.getName()) +1;
 						
 						agrees.put(targetPlayer.getName(), agrees_count);
 						
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Have A 'Agree' From " + ChatColor.DARK_AQUA + player.getName());
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Now Have " + agrees.get(targetPlayer.getName()) + " People That Agree With You");
 					}
 					else{
 						
 						// Safety check
 						if (!agrees.containsKey(targetPlayer.getName())) {
 							agrees.put(targetPlayer.getName(), 0);
 						}
 						
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Have A 'Agree' From " + ChatColor.DARK_AQUA + player.getName() + ChatColor.GREEN + " He is not an OP");
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Now Have " + agrees.get(targetPlayer.getName()) + " People That Agree With You!");
 					}
 					
 					return true;
 				}
 				if(player.isOp()){
 					if(args[0].equalsIgnoreCase("amount")){
 						
 						// Safety check
 						if (!agrees.containsKey(player.getName())) {
 							agrees.put(player.getName(), 0);
 						}
 						
 						if(agrees.get(player.getName()) == 0){
 							player.sendMessage(ChatColor.GREEN + "No One Agrees With You At The Moment");
 						}
 						else{
 							player.sendMessage(ChatColor.AQUA + "You Have " + agrees.get(player.getName()) + " People That Agree!");
 						}
 						
 						return true;
 					}	
 					else if(args[0].equalsIgnoreCase("done")){
 						
 						// Safety check
 						if (!agrees.containsKey(player.getName())) {
 							agrees.put(player.getName(), 0);
 						}
 						
 						if(agrees.get(player) == 0){
 							player.sendMessage(ChatColor.DARK_RED + "You have no Agrees!");
 						}
 						else {
 							// Subtract 1 from your agrees
 							int agrees_count = agrees.get(player.getName()) -1;
 							
 							agrees.put(player.getName(), agrees_count);
 							player.sendMessage(ChatColor.AQUA + "You Have " + agrees.get(player.getName()) + " People That Agree!");
 						}
 						
 						return true;
 					}
 					else if (args[0].equalsIgnoreCase("rank")) {					
 						player.sendMessage(rankcompare(agrees, player, "Agrees"));
 						return true;
 					}
 				}
 			}
 		}
 		/*
 		 * On disagree command
 		 */
 		if(commandLabel.equalsIgnoreCase("disagree")){
 			if(args.length == 0){
 				player.sendMessage("----" + ChatColor.GREEN + "Disagree Help" + ChatColor.WHITE + "----");
 				player.sendMessage(ChatColor.AQUA + "/disagree" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows This Screen!");
 				player.sendMessage(ChatColor.AQUA + "/disagree amount" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows The Amount Of Disagrees You Have");
 				player.sendMessage(ChatColor.AQUA + "/disagree rank" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows The Player With The Most Disagrees");
 				player.sendMessage(ChatColor.AQUA + "/disagree <player's name>" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Sends A Message To The Player That You Disagree With Them");
 				
 				return true;
 			}
 			if(args.length == 1) {
 				if(player.getServer().getPlayer(args[0]) !=null){
 					Player targetPlayer = player.getServer().getPlayer(args[0]);
 					if(targetPlayer.isOp()){
 						
 						// Safety check
 						if (!disagrees.containsKey(targetPlayer.getName())) {
 							disagrees.put(targetPlayer.getName(), 0);
 						}
 						
 						int disagrees_count = disagrees.get(targetPlayer.getName()) +1;
 						
 						disagrees.put(targetPlayer.getName(), disagrees_count);
 						
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Have A 'Disagree' From " + ChatColor.DARK_AQUA + player.getName());
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Now Have " + disagrees.get(targetPlayer.getName()) + " People That Disagree With You");
 					}
 					else{
 						// Safety check
 						if (!disagrees.containsKey(targetPlayer.getName())) {
 							disagrees.put(targetPlayer.getName(), 0);
 						}
 						
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Have A 'Disagree' From " + ChatColor.DARK_AQUA + player.getName() + " He is not an OP");
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Now Have " + disagrees.get(targetPlayer.getName()) + " People That Disagree With You!");
 					}
 					
 					return true;
 				}
 				if(player.isOp()){
 					if(args[0].equalsIgnoreCase("amount")){
 						
 						// Safety check
 						if (!disagrees.containsKey(player.getName())) {
 							disagrees.put(player.getName(), 0);
 						}
 						
 						if (disagrees.get(player.getName()) == 0) {
 							player.sendMessage(ChatColor.GREEN + "There Are No People That Disagree With You At The Moment");
 						}
 						else {
 							player.sendMessage(ChatColor.AQUA + "You Have " + disagrees.get(player.getName()) + " People That Disagree!");
 						}
 						
 						return true;
 						
 					}
 					else if(args[0].equalsIgnoreCase("done")){
 						
 						// Safety check
 						if (!disagrees.containsKey(player.getName())) {
 							disagrees.put(player.getName(), 0);
 						}
 						
 						if (disagrees.get(player.getName()) == 0) {
 							player.sendMessage(ChatColor.BLUE + "There Are No People That Disagree With You left!");
 						}
 						else {
 							int disagrees_count = disagrees.get(player.getName()) -1;
 							
 							disagrees.put(player.getName(), disagrees_count);
 							player.sendMessage(ChatColor.BLUE + "You Have " + disagrees.get(player.getName()) + " People That Disagree!");
 						}
 						
 						return true;
 
 					}
 					else if (args[0].equalsIgnoreCase("rank")) {					
 						player.sendMessage(rankcompare(disagrees, player, "Disagrees"));
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 }
