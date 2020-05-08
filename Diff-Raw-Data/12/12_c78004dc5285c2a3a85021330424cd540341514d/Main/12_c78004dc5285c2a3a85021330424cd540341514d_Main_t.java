 import java.io.File;
 import java.util.ArrayList;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import com.iConomy.iConomy;
 
 import Base.Card;
 import Base.CardPlayer;
 import Games.BlackJack.*;
 
 /**
  * The main class of the plugin
  * Makes most method calls (needs cleaning/organizing)
  * 
  * @author JorganPubshire
  *
  */
 public class Main extends JavaPlugin{
 	BlackJack blackjack = new BlackJack();
 	CardPlayer turn;
 	CardPlayer better;
 	ArrayList<Player> joinQueue = new ArrayList<Player>();
 	ArrayList<CardPlayer> leaveQueue = new ArrayList<CardPlayer>();
 	ArrayList<Player> joined = new ArrayList<Player>();
 	boolean betting = false;
 	PluginManager pm;
 	public iConomy iconomy = null;
 	boolean usingIconomy = false;
 	File data = new File("plugins/Casino/config.yml");
 	Configuration config = new Configuration(data);
 	int min = 0;
 
 	/**
 	 * Code called before plugin is disabled
 	 */
 	public void onDisable() {
 		System.out.println("Cards is disabled!");
 
 	}
 
 	/**
 	 * Code called after plugin is enabled
 	 */
 	public void onEnable() {
 		System.out.println("Cards is working properly!");
 		//event registry
 		pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_CHAT, new ChatListener(this), Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLUGIN_DISABLE, new PluginListener(this), Priority.Monitor, this);
 		pm.registerEvent(Event.Type.PLUGIN_ENABLE, new PluginListener(this), Priority.Monitor, this);
 		pm.registerEvent(Event.Type.PLAYER_INTERACT, new BlockListener(this), Priority.Normal, this);
 
 		//creates Casino folder and config.yml file
 		new File("plugins/Casino").mkdir();
 		if(!data.exists()){
 			try{
 				data.createNewFile();
 			}
 			catch(Exception e){
 				e.printStackTrace();
 				return;
 			}
 		}
 
 		config.load();
 		// records data to new config file
 		if(config.getKeys("minBet") == null){
 			config.setProperty("minBet",1);
 		}
 		
 		if(config.getKeys("slots") == null){
 			config.setProperty("slots.1.world", getServer().getWorlds().get(0).getName());
 			config.setProperty("slots.1.x", 0);
 			config.setProperty("slots.1.y", 0);
 			config.setProperty("slots.1.z", 0);
 			config.setProperty("slots.1.pitch", 0);
 			config.setProperty("slots.1.yaw", 0);
 			config.setProperty("slots.2.world", getServer().getWorlds().get(0).getName());
 			config.setProperty("slots.2.x", 0);
 			config.setProperty("slots.2.y", 0);
 			config.setProperty("slots.2.z", 0);
 			config.setProperty("slots.2.pitch", 0);
 			config.setProperty("slots.2.yaw", 0);
 			config.setProperty("slots.3.world", getServer().getWorlds().get(0).getName());
 			config.setProperty("slots.3.x", 0);
 			config.setProperty("slots.3.y", 0);
 			config.setProperty("slots.3.z", 0);
 			config.setProperty("slots.3.pitch", 0);
 			config.setProperty("slots.3.yaw", 0);
 			config.setProperty("slots.4.world", getServer().getWorlds().get(0).getName());
 			config.setProperty("slots.4.x", 0);
 			config.setProperty("slots.4.y", 0);
 			config.setProperty("slots.4.z", 0);
 			config.setProperty("slots.4.pitch", 0);
 			config.setProperty("slots.4.yaw", 0);
 			config.setProperty("slots.5.world", getServer().getWorlds().get(0).getName());
 			config.setProperty("slots.5.x", 0);
 			config.setProperty("slots.5.y", 0);
 			config.setProperty("slots.5.z", 0);
 			config.setProperty("slots.5.pitch", 0);
 			config.setProperty("slots.5.yaw", 0);
 			config.save();
 		}
 		//loads config data into plugin
 		refresh();
 
 	}
 
 	/**
 	 * Loads config data into plugin memory
 	 */
 	public void refresh(){
 		//forms the 5 locations and stores them
 		for(int i = 1; i<6;i++){
 			Location loc;
 			double x = config.getDouble("slots."+i+".x", 0);
 			double y = config.getDouble("slots."+i+".y", 0);
 			double z = config.getDouble("slots."+i+".z", 0);
 			float pitch = new Float(config.getString("slots."+i+".pitch"));
 			float yaw = new Float(config.getString("slots."+i+".yaw"));
 			loc = new Location(getServer().getWorlds().get(0),x,y,z,pitch,yaw);
 			blackjack.slots[i-1] = loc;
 		}
 		
 		min = config.getInt("minBet", 0);
 		//reloads the locations into the plugin
 		blackjack.init();
 
 	}
 
 	/**
 	 * Saves the player's current location as a blackjack slot (Admin command)
 	 * 
 	 * @param loc
 	 * @param slot
 	 */
 	public void saveLoc(Location loc, int slot){
 		World world = loc.getWorld();
 		double x = loc.getX();
 		double y = loc.getY();
 		double z = loc.getZ();
 		float pitch = loc.getPitch();
 		float yaw = loc.getYaw();
 
 		//sets config file data
 		config.load();
 		config.setProperty("slots."+slot+".world", world.getName());
 		config.setProperty("slots."+slot+".x", x);
 		config.setProperty("slots."+slot+".y", y);
 		config.setProperty("slots."+slot+".z", z);
 		config.setProperty("slots."+slot+".pitch", pitch);
 		config.setProperty("slots."+slot+".yaw", yaw);
 		config.save();
 
 		//reloads slot data
 		refresh();
 	}
 
 	/**
 	 * Handles player issued commands
 	 */
 	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String [] args){
 		if(sender instanceof Player){
 		}
 		else{
 			return false;
 		}
 		Player player = (Player) sender;
 		//blackjack
 		if(cmdLabel.equalsIgnoreCase("blackjack")){
 			if(args.length == 0){
 				if(blackjack.getPlayers().contains(player)){
 					player.sendMessage(ChatColor.RED + "You are already in that game!");
 					return true;
 				}
 				if(blackjack.getPlayers().size()>0){
 					if(iConomy.getAccount(player.getName()).getHoldings().balance() > 0){
 						if(!joinQueue.contains(player)){
 							joinQueue.add(player);
 						}
 						player.sendMessage("There is a game in progress, you will be added to the game at the end of the next hand.");
 					}
 					else{
 						player.sendMessage(ChatColor.RED + "You don't have any money!");
 					}
 				}
 				else{
 					addPlayer(player);
 				}
 			}
 			else{
 				if(blackjack.getPlayers().size()!=0){
 					player.sendMessage(ChatColor.RED + "That cannot be done while a game is in progress!");
 					return true;
 				}
 				switch(Integer.parseInt(args[0])){
 				case 1: saveLoc(player.getLocation(),1); return true;
 				case 2: saveLoc(player.getLocation(),2); return true;
 				case 3: saveLoc(player.getLocation(),3); return true;
 				case 4: saveLoc(player.getLocation(),4); return true;
 				case 5: saveLoc(player.getLocation(),5); return true;
 				}
 			}
 
 		}
 		//hit
 		else if(cmdLabel.equalsIgnoreCase("hit")){
 			if(turn != null && player.equals(turn.getPlayer())){
 				turn.getPlayer().sendMessage(ChatColor.GOLD + "hit");
 				turn.giveCard(blackjack.hit());
 				playerTurn(turn);
 			}
 		}
 		//stay
 		else if(cmdLabel.equalsIgnoreCase("stay")){
 			if(turn != null && player.equals(turn.getPlayer())){
 				turn.getPlayer().sendMessage(ChatColor.GOLD + "stay");
 				blackjack.stay(turn);
 				game();
 			}
 		}
 		//leave
 		else if(cmdLabel.equalsIgnoreCase("leave")){
 			if(blackjack.containsPlayer(player)){
 				leaveQueue.add(blackjack.match(player));
 				blackjack.tpOutNow(blackjack.match(player));
 				if(better != null && betting && blackjack.match(player).equals(better)){
 					bettingAI();
 				}				
 				if(turn != null && !betting && blackjack.match(player).equals(turn)){
 				playerAI(blackjack.match(player));
 			}
 			//				player.sendMessage("You will be removed from the game at the end of this hand...");
 		}
 	}
 	//double down
 	else if(cmdLabel.equalsIgnoreCase("double")){
 		if(turn != null && player.equals(turn.getPlayer())){
 			int bet = blackjack.bets.get(turn);
 			if(turn.getCash() >= bet){
 				turn.getPlayer().sendMessage(ChatColor.GOLD + "double down");
 				turn.takeCash(bet);
 				blackjack.bets.put(turn,bet*2);
 				turn.giveCard(blackjack.hit());
 				blackjack.stay(turn);
 				game();
 			}
 			else{
 				turn.getPlayer().sendMessage(ChatColor.RED + "You don't have enough money to do that!");
 			}
 		}
 	}
 	//bet
 	else if(cmdLabel.equalsIgnoreCase("bet")){
 		if(better != null && player.equals(better.getPlayer()) && betting){
			if(args.length != 1){
				player.sendMessage("Please use that command properly. (/bet <amount>)");
				return true;
			}
 			int bet = Integer.parseInt(args[0]);
 			if(bet < min){
 				player.sendMessage(ChatColor.RED + "You must bet at least " + min + " dollar(s)");
 				return true;
 			}
 			better.getPlayer().sendMessage(ChatColor.GOLD + "You bet " + bet);
 			boolean betted = bet(better,bet);
 			if(betted){
 				betting();
 			}
 			else{
 				player.sendMessage(ChatColor.RED + "You don't have enough money for that!");
 			}
 		}
 	}
 	//money/cash
 	else if(cmdLabel.equalsIgnoreCase("cash")){
 		if(blackjack.containsPlayer(player)){
 			player.sendMessage("You have " + blackjack.match(player).getCash() + " dollars.");
 		}
 	}
 	return true;
 }
 
 /**
  * Enables iConomy use
  */
 public void useIconomy(){
 	usingIconomy = true;
 	blackjack.usingIconomy = true;
 }
 
 /**
  * Disables iConomy use
  */
 public void unuseIconomy(){
 	usingIconomy = false;
 	blackjack.usingIconomy = false;
 }
 
 /**
  * Adds a player to the game
  * 
  * @param player
  */
 public void addPlayer(Player player){
 	boolean allow;
 	//uses iConomy
 	if(usingIconomy){
 		allow = blackjack.addPlayer(player,iconomy);
 	}
 	else {
 		allow = blackjack.addPlayer(player);
 	}
 	if(allow){
 		player.sendMessage("You have been added to the game.");
 		//begins betting phase
 		prepBets();
 		betting();
 	}
 }
 
 /**
  * Adds multiple players before a hand begins
  */
 public void addPlayers(){
 	for(Player player : joinQueue){
 		boolean allow;
 		//uses iConomy
 		if(usingIconomy){
 			allow = blackjack.addPlayer(player,iconomy);
 		}
 		else{
 			allow = blackjack.addPlayer(player);
 		}
 		if(allow){
 			//records joined players
 			joined.add(player);
 			player.sendMessage("You have been added to the game.");
 		}
 	}
 	//removes joined players from the waiting queue
 	for(Player player : joined){
 		joinQueue.remove(player);
 	}
 	joined.clear();
 }
 
 /**
  * Removes multiple players before a hand begins
  */
 public void removePlayers(){
 	for(CardPlayer player : leaveQueue){
 		blackjack.removePlayer(player);
 	}
 	leaveQueue.clear();
 }
 
 /**
  * Pre-betting prep
  */
 public void prepBets(){
 	blackjack.prepBets();
 }
 
 /**
  * Automates the player's betting
  */
 public void bettingAI(){
 	bet(better,0);
 	betting();
 }
 
 /**
  * Cycles through players and takes bets
  */
 public void betting(){
 	//enables betting
 	betting = true;
 	//progresses the betting to the next player
 	better = blackjack.next();
 	//begins the game if there are no players left to bet
 	if(better == null){
 		//disables betting
 		betting = false;
 		//starts game
 		startGame();
 		return;
 	}
 	blackjack.removeWaiting(better);
 	if(leaveQueue.contains(better)){
 		bettingAI();
 	}
 	else{
 		better.getPlayer().sendMessage(ChatColor.YELLOW + "Please place your bet. (You have " + better.getCash() + " dollars)");
 	}
 }
 
 /**
  * Places the player's bet
  * 
  * @param player
  * @param bet
  * @return
  */
 public boolean bet(CardPlayer player, int bet){
 	if(player.getCash() >= bet){
 		player.takeCash(bet);
 		blackjack.bets.put(player, bet);
 		bet = 0;
 		return true;
 	}
 	else{
 		return false;
 	}
 }
 
 /**
  * Begins the gaming cycle
  */
 public void startGame(){	
 	blackjack.startHand();
 	game();
 }
 
 /**
  * Cycles through the players, takes commands, and performs commands without blocking other plugins
  */
 public void game(){
 	//progresses gameplay to the next player
 	turn = blackjack.next();
 	//if there are no player waiting for their turn, gameplay progresses to the dealer
 	if(turn == null){
 		blackjack.dealerTurn();
 		//the hand ends after the dealer's turn
 		endGame();
 		return;
 	}
 	blackjack.removeWaiting(turn);
 	if(leaveQueue.contains(turn)){
 		playerAI(turn);
 	}
 	else{
 		turn.getPlayer().sendMessage(ChatColor.GREEN + "It is now your turn. Please say \"hit\" or \"stay\".");
 		turn.getPlayer().sendMessage(ChatColor.YELLOW + "The dealer has " + blackjack.dealer.getHand().get(0) + " showing." + " ("+ (blackjack.dealer.getHand().get(0).getValue() + blackjack.aceBonus(blackjack.dealer)) + ")");
 		playerTurn(turn);
 	}
 }
 
 /**
  * Performs post-hand actions
  */
 public void endGame(){
 	//pays winners
 	blackjack.payout();
 	//notify players
 	blackjack.finish();
 	//resets values
 	blackjack.endHand();
 	//restarts the betting/gaming cycle
 	restart();
 }
 
 /**
  * Restarts the betting/gaming cycle
  */
 public void restart(){
 	//removes players with no money
 	removeBroke();
 	//remove players the want to leave
 	removePlayers();
 	//add players that wish to join
 	addPlayers();
 	//begins gameplay if there are players
 	if(blackjack.getPlayers().size()>0){
 		prepBets();
 		betting();
 	}
 	else{
 		System.out.println("NO PLAYERS");
 	}
 }
 
 /**
  * Asynchronously loops for the player to take their turn
  * 
  * @param player
  */
 public void playerTurn(CardPlayer player){
 	//allows the player to execute commands until they bust or stay
 	boolean good = blackjack.validate(player);
 	//advances gameplay after a bust
 	if(!good){
 		game();
 	}
 }
 
 /**
  * Adds players with no money to the leave queue
  */
 public void removeBroke(){
 	for(CardPlayer player : blackjack.getCardPlayers()){
 		if(player.getCash() <= 0){
 			leaveQueue.add(player);
 			player.getPlayer().sendMessage(ChatColor.RED + "You have no money and are being removed from the game!");
 		}
 	}
 }
 
 /**
  * Automates the player's turn in a similar fashion to the dealer
  * 
  * @param player
  */
 public void playerAI(CardPlayer player){
 	int sum = 0;
 	for(Card card : player.getHand()){
 		sum += card.getValue();
 	}
 	sum += blackjack.aceBonus(player);
 
 	blackjack.tellPlayer(player);
 
 	if(sum < 17){
 		player.giveCard(blackjack.changeCard(blackjack.hit()));
 		playerAI(player);
 	}
 	else{
 		blackjack.stay(player);
 		game();
 	}
 }
 
 
 }
