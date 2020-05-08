 package net.erbros.Lottery;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Random;
 
 import net.erbros.Lottery.register.payment.Method;
 import net.erbros.Lottery.register.payment.Method.MethodAccount;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 
 public class Etc {
     public Lottery plugin;
     public FileConfiguration config = null;
     
     public Etc(Lottery plugin) {
         this.plugin = plugin;
         config = plugin.getConfig();
     }
     
     
     
 
 
     public void configReplacePath (String pathFrom, String pathReplace, boolean deleteFrom) {
         if(config.contains(pathFrom)) {
             // Let's check for booleans etc so we can save the objects correctly.
             if(config.isBoolean(pathReplace) && config.isBoolean(pathFrom)) {
                 config.set(pathReplace, config.getBoolean(pathFrom));
                 debugMsg("Got value from old path: " + pathFrom + ", to new: " + pathReplace);
             } else {
                 debugMsg("isBoolean: " + pathReplace + "(" + config.isBoolean(pathReplace) + ") :: isBoolean: " + pathFrom + "(" + config.isBoolean(pathFrom) + ")");
             }
             // Int?
             if(config.isInt(pathReplace) && config.isInt(pathFrom)) {
                 config.set(pathReplace, config.getInt(pathFrom));
                 debugMsg("Got value from old path: " + pathFrom + ", to new: " + pathReplace);
             } else {
                 debugMsg("isBoolean: " + pathReplace + "(" + config.isBoolean(pathReplace) + ") :: isBoolean: " + pathFrom + "(" + config.isBoolean(pathFrom) + ")");
             }
             if(config.isLong(pathReplace) && config.isLong(pathFrom)) {
                 config.set(pathReplace, config.getLong(pathFrom));
                 debugMsg("Got value from old path: " + pathFrom + ", to new: " + pathReplace);
             } else {
                 debugMsg("isBoolean: " + pathReplace + "(" + config.isBoolean(pathReplace) + ") :: isBoolean: " + pathFrom + "(" + config.isBoolean(pathFrom) + ")");
             }
             if(config.isBoolean(pathReplace) && config.isBoolean(pathFrom)) {
                 config.set(pathReplace, config.getBoolean(pathFrom));
                 debugMsg("Got value from old path: " + pathFrom + ", to new: " + pathReplace);
             } else {
                 debugMsg("isBoolean: " + pathReplace + "(" + config.isBoolean(pathReplace) + ") :: isBoolean: " + pathFrom + "(" + config.isBoolean(pathFrom) + ")");
             }
             if(config.isString(pathReplace) && config.isString(pathFrom)) {
                 config.set(pathReplace, config.getString(pathFrom));
                 debugMsg("Got value from old path: " + pathFrom + ", to new: " + pathReplace);
             } else {
                 debugMsg("isBoolean: " + pathReplace + "(" + config.isBoolean(pathReplace) + ") :: isBoolean: " + pathFrom + "(" + config.isBoolean(pathFrom) + ")");
             }
             
             // Should we remove the old path?
             if(deleteFrom) config.set(pathFrom, null);
         }
     }
 
     
     public void loadConfig() {
         /*
         // lets check if there exist old config that needs to be loaded, then removed.
         String oldConfigs[] = new String[] {"cost", 
                 "hours", 
                 "useiConomy", 
                 "material", 
                 "broadcastBuying", 
                 "welcomeMessage", 
                 "extraInPot", 
                 "clearExtraInPot", 
                 "netPayout", 
                 "maxTicketsEachUser", 
                 "numberOfTicketsEachUser",
                 "jackpot",
                 "nextexec",
                 "debug"};
         
         List<String> oldConfigList = Arrays.asList(oldConfigs);
         
         for (String current : oldConfigList) {
             configReplacePath(current, "config." + current, true);
         }
         */
         
         Lottery.cost = config.getInt("config.cost",5);
 
         debugMsg("2");
         plugin.hours = config.getInt("config.hours", 24);
         Lottery.useiConomy = config.getBoolean("config.useiConomy", true);
         Lottery.material = config.getInt("config.material", 266);
         plugin.broadcastBuying = config.getBoolean("config.broadcastBuying", true);
         plugin.welcomeMessage = config.getBoolean("config.welcomeMessage", true);
         plugin.extraInPot = config.getInt("config.extraInPot", 0);
         plugin.clearExtraInPot = config.getBoolean("config.clearExtraInPot", true);
         plugin.netPayout = config.getInt("config.netPayout", 100);
         plugin.maxTicketsEachUser = config.getInt("config.maxTicketsEachUser", 1);
         plugin.numberOfTicketsAvailable = config.getInt("config.numberOfTicketsAvailable", 0);
         plugin.jackpot = config.getInt("config.jackpot", 0);
         Lottery.nextexec = config.getLong("config.nextexec");
 
         // Load messages?
         loadCustomMessages();
         // Then lets save this stuff :)
         plugin.saveConfig();
     }
     
     
     public void loadCustomMessages() {
 
         plugin.msgWelcome = formatCustomMessage("message.welcome", "&6[LOTTERY] &fDraw in: &c%drawLong%");
         
     }
     
     public ArrayList<String> formatCustomMessage (String node, String def) {
         ArrayList<String> fList = new ArrayList<String>();
         // Lets find a msg.
         String msg = config.getString(node, def);
         config.set(node, msg);
         
         // Lets put this in a arrayList in case we want more than one line.
         Collections.addAll(fList, msg.split("%newline%"));
         
         return fList;
     }
     
     public String formatCustomMessageLive (String msg, Player player) {
         //Lets give timeLeft back if user provie %draw%
         msg = msg.replaceAll("%draw%", timeUntil(Lottery.nextexec, true));
         //Lets give timeLeft with full words back if user provie %drawLong%
         msg = msg.replaceAll("%drawLong%", timeUntil(Lottery.nextexec, false));
         // If %player% = Player name
         msg = msg.replaceAll("%player%", player.getDisplayName());
         // %cost% = cost
         msg = msg.replaceAll("%cost%", Lottery.cost.toString());
         // %pot%
         msg = msg.replaceAll("%pot%", Integer.toString(winningAmount()));
         // Lets get some colors on this, shall we?
         msg = msg.replaceAll("(&([a-f0-9]))", "\u00A7$2");
         return msg;
     }
 
 
     public ArrayList<String> playersInFile(String file) {
         ArrayList<String> players = new ArrayList<String>();
         try {
             BufferedReader in = new BufferedReader(new FileReader(
                     plugin.getDataFolder() + File.separator + file));
             String str;
             while ((str = in.readLine()) != null) {
                 // add players to array.
                 players.add(str.toString());
             }
             in.close();
         } catch (IOException e) {
         }
         return players;
     }
 
     public boolean addPlayer(Player player,
             Integer maxAmountOfTickets, Integer numberOfTickets) {
 
         if (playerInList(player) + numberOfTickets > maxAmountOfTickets) {
             return false;
         }
 
         // Do the ticket cost money or item?
         if (Lottery.useiConomy == false) {
             // Do the user have the item?
             if (player.getInventory().contains(Lottery.material,
                     Lottery.cost * numberOfTickets)) {
                 // Remove items.
                 player.getInventory().removeItem(
                         new ItemStack(Lottery.material, Lottery.cost
                                 * numberOfTickets));
             } else {
                 return false;
             }
         } else {
             // Do the player have money?
             // First checking if the player got an account, if not let's create
             // it.
             plugin.Method.hasAccount(player.getName());
                         Method.
 
             MethodAccount account = plugin.Method.getAccount(player.getName());
 
             // And lets withdraw some money
             if (account.hasOver(Lottery.cost * numberOfTickets)) {
                 // Removing coins from players account.
                 account.subtract(Lottery.cost * numberOfTickets);
             } else {
                 return false;
             }
 
         }
         // If the user paid, continue. Else we would already have sent return
         // false
         try {
             BufferedWriter out = new BufferedWriter(new FileWriter(
                     plugin.getDataFolder() + File.separator + "lotteryPlayers.txt",
                     true));
             for (Integer i = 0; i < numberOfTickets; i++) {
                 out.write(player.getName());
                 out.newLine();
             }
             out.close();
 
         } catch (IOException e) {
         }
 
         return true;
     }
 
     public Integer playerInList(Player player) {
         int numberOfTickets = 0;
         try {
             BufferedReader in = new BufferedReader(new FileReader(
                     plugin.getDataFolder() + File.separator + "lotteryPlayers.txt"));
             String str;
             while ((str = in.readLine()) != null) {
 
                 if (str.equalsIgnoreCase(player.getName())) {
                     numberOfTickets = numberOfTickets + 1;
                 }
             }
             in.close();
         } catch (IOException e) {
         }
 
         return numberOfTickets;
     }
 
     public int winningAmount() {
         int amount = 0;
         ArrayList<String> players = playersInFile("lotteryPlayers.txt");
         amount = players.size() * Lottery.cost;
         // Set the net payout as configured in the config.
         if (plugin.netPayout > 0) {
             amount = amount * plugin.netPayout / 100;
         }
         // Add extra money added by admins and mods?
         amount += plugin.extraInPot;
         // Any money in jackpot?
         amount += config.getInt("config.jackpot");
 
         return amount;
     }
     
     public int ticketsSold() {
         int sold = 0;
         ArrayList<String> players = playersInFile("lotteryPlayers.txt");
         sold = players.size();
         return sold;
     }
 
     public String formatMaterialName(int materialId) {
         String returnMaterialName = "";
         String rawMaterialName = Material.getMaterial(materialId).toString();
         rawMaterialName = rawMaterialName.toLowerCase();
         // Large first letter.
         String firstLetterCapital = rawMaterialName.substring(0, 1)
                 .toUpperCase();
         rawMaterialName = firstLetterCapital
                 + rawMaterialName.substring(1, rawMaterialName.length());
         returnMaterialName = rawMaterialName.replace("_", " ");
 
         return returnMaterialName;
     }
 
     public boolean removeFromClaimList(Player player) {
         // Do the player have something to claim?
         ArrayList<String> otherPlayersClaims = new ArrayList<String>();
         ArrayList<String> claimArray = new ArrayList<String>();
         try {
             BufferedReader in = new BufferedReader(new FileReader(
                     plugin.getDataFolder() + File.separator + "lotteryClaim.txt"));
             String str;
             while ((str = in.readLine()) != null) {
                 String[] split = str.split(":");
                 if (split[0].equals(player.getName())) {
                     // Adding this to player claim.
                     claimArray.add(str);
                 } else {
                     otherPlayersClaims.add(str);
                 }
             }
             in.close();
         } catch (IOException e) {
         }
 
         // Did the user have any claims?
         if (claimArray.isEmpty()) {
             player.sendMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE
                     + "You did not have anything unclaimed.");
             return false;
         }
         // Do a bit payout.
         for (int i = 0; i < claimArray.size(); i++) {
             String[] split = claimArray.get(i).split(":");
             int claimAmount = Integer.parseInt(split[1]);
             int claimMaterial = Integer.parseInt(split[2]);
             player.getInventory().addItem(
                     new ItemStack(claimMaterial, claimAmount));
             player.sendMessage("You just claimed " + claimAmount + " "
                     + formatMaterialName(claimMaterial) + ".");
         }
 
         // Add the other players claims to the file again.
         try {
             BufferedWriter out = new BufferedWriter(new FileWriter(
                     plugin.getDataFolder() + File.separator + "lotteryClaim.txt"));
             for (int i = 0; i < otherPlayersClaims.size(); i++) {
                 out.write(otherPlayersClaims.get(i));
                 out.newLine();
             }
 
             out.close();
 
         } catch (IOException e) {
         }
         return true;
     }
 
     public boolean addToClaimList(String playerName, int winningAmount,
             int winningMaterial) {
         // Then first add new winner, and after that the old winners.
         try {
             BufferedWriter out = new BufferedWriter(
                     new FileWriter(plugin.getDataFolder() + File.separator
                             + "lotteryClaim.txt", true));
             out.write(playerName + ":" + winningAmount + ":" + winningMaterial);
             out.newLine();
             out.close();
         } catch (IOException e) {
         }
         return true;
     }
 
     public boolean addToWinnerList(String playerName, int winningAmount,
             int winningMaterial) {
         // This list should be 10 players long.
         ArrayList<String> winnerArray = new ArrayList<String>();
         try {
             BufferedReader in = new BufferedReader(new FileReader(
                     plugin.getDataFolder() + File.separator + "lotteryWinners.txt"));
             String str;
             while ((str = in.readLine()) != null) {
                 winnerArray.add(str);
             }
             in.close();
         } catch (IOException e) {
         }
         // Then first add new winner, and after that the old winners.
         try {
             BufferedWriter out = new BufferedWriter(new FileWriter(
                     plugin.getDataFolder() + File.separator + "lotteryWinners.txt"));
             out.write(playerName + ":" + winningAmount + ":" + winningMaterial);
             out.newLine();
             // How long is the array? We just want the top 9. Removing index 9
             // since its starting at 0.
             if (winnerArray.size() > 0) {
                 if (winnerArray.size() > 9) {
                     winnerArray.remove(9);
                 }
                 // Go trough list and output lines.
                 for (int i = 0; i < winnerArray.size(); i++) {
                     out.write(winnerArray.get(i));
                     out.newLine();
                 }
             }
             out.close();
 
         } catch (IOException e) {
         }
         return true;
     }
 
     public String timeUntil(long time, boolean mini) {
 
         double timeLeft = Double.parseDouble(Long.toString(((time - System
                 .currentTimeMillis()) / 1000)));
         // If negative number, just tell them its DRAW TIME!
         if (timeLeft < 0) {
                     // Lets make it draw at once.. ;)
                     plugin.StartTimerSchedule(true);
                     // And return some string to let the user know we are doing our best ;)
                     if(mini) {
                         return "Soon";
                     }
                     return "Draw will occur soon!";
 
         }
 
         // How many days left?
         String stringTimeLeft = "";
         
         if (timeLeft >= 60 * 60 * 24) {
             int days = (int) Math.floor(timeLeft / (60 * 60 * 24));
             timeLeft -= 60 * 60 * 24 * days;
             if (!mini) {
                 stringTimeLeft += Integer.toString(days) + " " + pluralWording("day", days) + ", ";
             } else {
                 stringTimeLeft += Integer.toString(days) + "d ";
             }
         }
         if (timeLeft >= 60 * 60) {
             int hours = (int) Math.floor(timeLeft / (60 * 60));
             timeLeft -= 60 * 60 * hours;
             if (!mini) {
                 stringTimeLeft += Integer.toString(hours) + " " + pluralWording("hour", hours) + ", ";
             } else {
                 stringTimeLeft += Integer.toString(hours) + "h ";
             }
         }
         if (timeLeft >= 60) {
             int minutes = (int) Math.floor(timeLeft / (60));
             timeLeft -= 60 * minutes;
             if (!mini) {
                 stringTimeLeft += Integer.toString(minutes) + " " + pluralWording("minute", minutes) + ", ";
             } else {
                 stringTimeLeft += Integer.toString(minutes) + "m ";
             }
         } else {
             // Lets remove the last comma, since it will look bad with 2 days, 3
             // hours, and 14 seconds.
             if (stringTimeLeft.equalsIgnoreCase("") == false && !mini) {
                 stringTimeLeft = stringTimeLeft.substring(0,
                         stringTimeLeft.length() - 1);
             }
         }
         int secs = (int) timeLeft;
         if (stringTimeLeft.equalsIgnoreCase("") == false && !mini) {
             stringTimeLeft += "and ";
         }
         if (!mini) {
                     stringTimeLeft += Integer.toString(secs) + " " + pluralWording("second", secs);
                 } else {
                     stringTimeLeft += secs + "s";
         }
 
         return stringTimeLeft;
     }
 
     /*
     // Stolen from ltguide! Thank you so much :)
     public Boolean hasPermission(CommandSender sender, String node,
             Boolean needOp) {
         if (!(sender instanceof Player))
             return true;
 
         Player player = (Player) sender;
         if (Permissions != null)
             return Permissions.has(player, node);
         else {
             Plugin test = getServer().getPluginManager().getPlugin(
                     "Permissions");
             if (test != null) {
                 Permissions = ((Permissions) test).getHandler();
                 return Permissions.has(player, node);
             }
         }
         
         
         
         if (needOp) {
             return player.isOp();
         }
         return true;
     }
     */
 
     public static String pluralWording(String word, Integer number) {
         // Start
         if (word.equalsIgnoreCase("ticket")) {
             if (number == 1) {
                 return "ticket";
             } else {
                 return "tickets";
             }
         }
         // Next
         if (word.equalsIgnoreCase("player")) {
             if (number == 1) {
                 return "player";
             } else {
                 return "players";
             }
         }
         // Next
         if (word.equalsIgnoreCase("day")) {
             if (number == 1) {
                 return "day";
             } else {
                 return "days";
             }
         }
         // Next
         if (word.equalsIgnoreCase("hour")) {
             if (number == 1) {
                 return "hour";
             } else {
                 return "hours";
             }
         }
         // Next
         if (word.equalsIgnoreCase("minute")) {
             if (number == 1) {
                 return "minute";
             } else {
                 return "minutes";
             }
         }
         // Next
         if (word.equalsIgnoreCase("second")) {
             if (number == 1) {
                 return "second";
             } else {
                 return "seconds";
             }
         }
         // Next
         return "i don't know that word";
     }
     
     // Enable some debugging?
     public void debugMsg(String msg) {
         if(config.getBoolean("config.debug") == true) {
             if(msg != null) {
                Lottery.log.info(msg);
                 plugin.getServer().broadcastMessage(msg);
             }
         }
     }
 
     public Hashtable<String, Integer> realPlayersFromList(
             ArrayList<String> ticketList) {
         Hashtable<String, Integer> playerList = new Hashtable<String, Integer>();
         int value = 0;
         for (String check : ticketList) {
             if (playerList.containsKey(check)) {
                 value = Integer.parseInt(playerList.get(check).toString()) + 1;
             } else {
                 value = 1;
             }
             playerList.put(check, value);
         }
         return playerList;
     }
     
 
     public boolean getWinner() {
         ArrayList<String> players = playersInFile("lotteryPlayers.txt");
 
         if (players.isEmpty() == true) {
             Bukkit.broadcastMessage(ChatColor.GOLD + "[LOTTERY] "
                 + ChatColor.WHITE
                 + "No tickets sold this round. Thats a shame.");
             return false;
         } else {
             // Find rand. Do minus 1 since its a zero based array.
             int rand = 0;
 
             // is max number of tickets 0? If not, include empty tickets not sold.
             if(plugin.numberOfTicketsAvailable > 0 && ticketsSold() < plugin.numberOfTicketsAvailable) {
                 rand = new Random().nextInt(plugin.numberOfTicketsAvailable);
                 // If it wasn't a player winning, then do some stuff. If it was a player, just continue below.
                 if(rand > players.size()-1) {
                     // No winner this time, pot goes on to jackpot!
                     Integer jackpot = winningAmount();
                     config.set("config.jackpot", jackpot);
                     addToWinnerList("Jackpot", jackpot, Lottery.useiConomy ? 0 : Lottery.material);
                     config.set("config.lastwinner", "Jackpot");
                     config.set("config.lastwinneramount", jackpot);
                     Bukkit.broadcastMessage(ChatColor.GOLD + "[LOTTERY] "
                         + ChatColor.WHITE
                         + "No winner, we have a rollover! "
                         + ChatColor.GREEN
                         + ((Lottery.useiConomy)? plugin.Method.format(jackpot) :  + jackpot + " " + formatMaterialName(Lottery.material))
                         + ChatColor.WHITE
                         + " went to jackpot!");
                     clearAfterGettingWinner();
                     return true;
                 }
             } else {
                 // Else just continue
                 rand = new Random().nextInt(players.size());
             }
             
 
             debugMsg("Rand: " + Integer.toString(rand));
             int amount = winningAmount();
             if (Lottery.useiConomy == true) {
                 plugin.Method.hasAccount(players.get(rand));
                 MethodAccount account = plugin.Method.getAccount(players.get(rand));
 
                 // Just make sure the account exists, or make it with default
                 // value.
                 // Add money to account.
                 account.add(amount);
                 // Announce the winner:
                 Bukkit.broadcastMessage(ChatColor.GOLD + "[LOTTERY] "
                         + ChatColor.WHITE + "Congratulations to "
                         + players.get(rand) + " for winning " + ChatColor.RED
                         + plugin.Method.format(amount) + ".");
                 addToWinnerList(players.get(rand), amount, 0);
             } else {
                 Bukkit.broadcastMessage(ChatColor.GOLD + "[LOTTERY] "
                         + ChatColor.WHITE + "Congratulations to "
                         + players.get(rand) + " for winning " + ChatColor.RED
                         + amount + " " + formatMaterialName(Lottery.material) + ".");
                 Bukkit.broadcastMessage(ChatColor.GOLD + "[LOTTERY] "
                         + ChatColor.WHITE + "Use " + ChatColor.RED
                         + "/lottery claim" + ChatColor.WHITE
                         + " to claim the winnings.");
                 addToWinnerList(players.get(rand), amount, Lottery.material);
                 addToClaimList(players.get(rand), amount, Lottery.material.intValue());
             }
             Bukkit.broadcastMessage(ChatColor.GOLD
                     + "[LOTTERY] "
                     + ChatColor.WHITE
                     + "There was in total "
                     + realPlayersFromList(players).size()
                     + " "
                     + pluralWording("player", realPlayersFromList(players)
                             .size()) + " buying " + players.size() + " "
                     + pluralWording("ticket", players.size()));
 
             // Add last winner to config.
             config.set("config.lastwinner", players.get(rand));
             config.set("config.lastwinneramount", amount);
             config.set("config.jackpot", 0);
 
             clearAfterGettingWinner();
         }
         return true;
     }
     
     public void clearAfterGettingWinner() {
 
         // extra money in pot added by admins and mods?
         // Should this be removed?
         if (plugin.clearExtraInPot == true) {
             config.set("extraInPot", 0);
             plugin.extraInPot = 0;
         }
         // Clear file.
                     try {
                         BufferedWriter out = new BufferedWriter(
                                 new FileWriter(plugin.getDataFolder() + File.separator
                                         + "lotteryPlayers.txt", false));
                         out.write("");
                         out.close();
 
                     } catch (IOException e) {
                     }
     }
     
 }
