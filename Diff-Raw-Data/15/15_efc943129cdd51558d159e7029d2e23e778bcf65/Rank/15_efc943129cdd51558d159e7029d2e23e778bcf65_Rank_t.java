 package com.thepastimers.Rank;
 
 import com.thepastimers.Chat.Chat;
 import com.thepastimers.Chat.ChatData;
 import com.thepastimers.Database.Database;
 import com.thepastimers.Worlds.Worlds;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: derp
  * Date: 10/1/12
  * Time: 7:21 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Rank extends JavaPlugin implements Listener {
     Database database;
     Chat chat;
     Worlds worlds;
 
     @Override
     public void onEnable() {
         getLogger().info("Rank init");
 
         getServer().getPluginManager().registerEvents(this,this);
         database = (Database)getServer().getPluginManager().getPlugin("Database");
 
         if (database == null) {
             getLogger().warning("Unable to load Database plugin. Some functionality will not be available.");
         }
 
         chat = (Chat)getServer().getPluginManager().getPlugin("Chat");
 
         if (chat == null) {
             getLogger().warning("Unable to load Database plugin. Some functionality will not be available.");
         } else {
             chat.register(Rank.class,this,1);
         }
 
         worlds = (Worlds)getServer().getPluginManager().getPlugin("Worlds");
         if (worlds == null) {
             getLogger().warning("Unable to load Worlds plugin. Some functionality may not be available.");
         }
 
         getLogger().info("Table info: ");
         getLogger().info(PlayerRank.getTableInfo());
         PlayerTitle.autoPopulate = true;
         PlayerRank.refreshCache(database,getLogger());
         //PlayerTitle.myClass = PlayerTitle.class;
         database.select(PlayerTitle.class,"1");
         getLogger().info(PlayerTitle.getTableInfo());
         getLogger().info(RankData.getTableInfo());
 
         getLogger().info("Rank init complete");
     }
 
     @Override
     public void onDisable() {
         getLogger().info("Rank disabled");
     }
 
     public String getRank(String player) {
         if (player == null || database == null) {
             return "";
         }
 
         player = player.replace("'","");
 
         PlayerRank rank = PlayerRank.getRankForPlayer(player);
 
         if (rank == null) {
             return "";
         }
 
         return rank.getRank();
     }
 
     public List<String> getRanks(String player) {
         List<String> ranks = new ArrayList<String>();
         if (player == null || database == null) {
             return ranks;
         }
 
         player = player.replace("'","");
 
         String rank = getRank(player);
         if ("".equalsIgnoreCase(rank)) {
             return ranks;
         }
 
         ranks.add(rank);
 
         String newRank = rank;
         while (true) {
             List<RankData> rankDataList = (List<RankData>)database.select(RankData.class,"rank = '" + database.makeSafe(newRank) + "'");
             if (rankDataList.size() == 0) {
                 break;
             }
 
             RankData data = rankDataList.get(0);
             ranks.add(data.getParentRank());
             newRank = data.getParentRank();
         }
 
         return ranks;
     }
 
     public PlayerRank getRankObject(String player) {
         if (player == null || database == null) {
             return null;
         }
 
         player = player.replace("'","");
 
         PlayerRank rank = PlayerRank.getRankForPlayer(player);
 
         return rank;
     }
 
     public boolean setRank(String player, String rank) {
         if (player == null || rank == null || database == null) {
             return false;
         }
 
         player = database.makeSafe(player);
         rank = database.makeSafe(rank);
 
         if (!removeRank(player)) {
             return false;
         }
 
         PlayerRank newRank = new PlayerRank();
         newRank.setRank(rank);
         newRank.setPlayer(player);
 
         if (newRank.save(database)) {
             getServer().broadcastMessage(player + " is now in group " + rank);
             return true;
         }
 
         return false;
     }
 
     public boolean removeRank(String player) {
         if (player == null || database == null) {
             return false;
         }
 
         PlayerRank rankObj = getRankObject(player);
 
         if (rankObj == null) {
             return true;
         }
 
         String rank = rankObj.getRank();
 
         rankObj.delete(database);
 
         rankObj = getRankObject(player);
 
         if (rankObj != null) {
             getLogger().warning("removeRank: Cannot remove rank");
             return false;
         }
 
         getServer().broadcastMessage(player + " is no longer in group " + rank);
         return true;
     }
 
     public boolean hasRank(String player, String rank) {
         if (player == null || rank == null || database == null) {
             return false;
         }
 
         PlayerRank rankObj = PlayerRank.getRankForPlayer(player);
 
         if (rankObj == null) {
             return false;
         }
 
         return rank.equalsIgnoreCase(rankObj.getRank());
     }
 
     private boolean isAuthorized(String player) {
         if (player == null) {
             return false;
         }
 
         if (player.equalsIgnoreCase("CONSOLE")) {
             return true;
         }
 
         String rank = getRank(player);
 
         if (rank.equalsIgnoreCase("admin") || rank.equalsIgnoreCase("owner")) {
             return true;
         }
 
         return false;
     }
 
     public String getTitle(String player) {
         if (player == null || database == null) {
             return "";
         }
 
         player = database.makeSafe(player);
 
         List<PlayerTitle> titles = (List<PlayerTitle>)database.select(PlayerTitle.class,"player = '" + player + "'");
 
         if (titles.size() == 0) {
             return "";
         }
 
         String title = titles.get(0).getTitle();
 
         return title;
     }
 
     public PlayerTitle getTitleObject(String player) {
         if (player == null || database == null) {
             return null;
         }
 
         player = database.makeSafe(player);
 
         List<PlayerTitle> titles = (List<PlayerTitle>)database.select(PlayerTitle.class,"player = '" + player + "'");
 
         if (titles.size() == 0) {
             return null;
         }
 
         return titles.get(0);
     }
 
     public boolean removeTitle(String player) {
         if (player == null || database == null) {
             return false;
         }
 
         player = database.makeSafe(player);
 
         PlayerTitle titleObj = getTitleObject(player);
 
         if (titleObj == null) {
             return true;
         }
 
         String title = getTitle(player);
 
        titleObj.delete(database);
 
         titleObj = getTitleObject(player);
 
         if (titleObj != null) {
             getLogger().warning("removeTitle: Cannot remove title");
             return false;
         }
 
         if (chat != null) {
             title = chat.replaceColor(title);
         }
         getServer().broadcastMessage(player + " no longer has title " + title);
         return true;
     }
 
     public boolean setTitle(String player, String title) {
         if (player == null || database == null || title == null) {
             return false;
         }
 
         player = database.makeSafe(player);
         title = database.makeSafe(title);
 
         if (!removeTitle(player)) {
             return false;
         }
 
         PlayerTitle pt = new PlayerTitle();
         pt.setPlayer(player);
         pt.setTitle(title);
 
         if (pt.save(database,PlayerTitle.class)) {
             title = getTitle(player);
             if (chat != null) {
                 title = chat.replaceColor(title);
             }
             getServer().broadcastMessage(player + " now has title " + title);
             return true;
         }
 
         return false;
     }
 
     public ChatColor getTitleColor(String title) {
         if ("pope".equalsIgnoreCase(title)) {
             return ChatColor.DARK_GREEN;
         } else if (title.toLowerCase().contains("lava")) {
             return ChatColor.GOLD;
         } else if ("Oldtimer".equalsIgnoreCase(title)) {
             return ChatColor.DARK_GRAY;
         } else if (title.toLowerCase().contains("sea")) {
             return ChatColor.BLUE;
         } else if ("Mod".equalsIgnoreCase(title) || "Moderator".equalsIgnoreCase(title)) {
             return ChatColor.BLUE;
         }
 
         return ChatColor.WHITE;
     }
 
     @EventHandler
     public void chatEvent(AsyncPlayerChatEvent event) {
         if (chat != null) return;
         String title = getTitle(event.getPlayer().getName());
 
         title = title.replace(":red:",ChatColor.RED.toString());
         title = title.replace(":blue:",ChatColor.BLUE.toString());
         title = title.replace(":green:",ChatColor.GREEN.toString());
         title = title.replace(":dark green:",ChatColor.DARK_GREEN.toString());
         title = title.replaceAll(":.*?:","");
 
         if (!"".equalsIgnoreCase(title)) {
             event.setFormat("<" + title + ChatColor.WHITE + " %1$s> %2$s");
         }
     }
 
     public void doChat(ChatData cd) {
         String title = getTitle(cd.getPlayer());
 
         //ChatColor textColor = getTitleColor(title);
 
         if (!"".equalsIgnoreCase(title)) {
             cd.setPlayerString(title + " :white:" + cd.getPlayerString());
         }
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         String playerName = "";
 
         if (sender instanceof Player) {
             playerName = ((Player)sender).getName();
         } else {
             playerName = "CONSOLE";
         }
 
         if (worlds != null && worlds.getPlayerWorldType(playerName) == Worlds.VANILLA) {
             return false;
         }
 
         String command = cmd.getName();
 
         if (command.equalsIgnoreCase("rank")) {
             if (args.length > 0) {
                 String secondCommand = args[0];
 
                 if (secondCommand.equalsIgnoreCase("set")) {
                     if (!isAuthorized(playerName)) {
                         getLogger().info(playerName + " attempted unauthorized access of /rank set");
                         return true;
                     }
                     if (args.length > 2) {
                         String player = args[1];
                         String newRank = args[2].toLowerCase();
 
                         if (!setRank(player,newRank)) {
                             sender.sendMessage("Unable to set rank");
                         }
 
                         return true;
                     } else {
                         sender.sendMessage("/rank set <player> <rank>");
                     }
                 } else if (secondCommand.equalsIgnoreCase("remove")) {
                     if (!isAuthorized(playerName)) {
                         getLogger().info(playerName + " attempted unauthorized access of /rank remove");
                         return true;
                     }
                     if (args.length > 1) {
                         String player = args[1];
 
                         if (!removeRank(player)) {
                             sender.sendMessage("Unable to remove rank.");
                         }
 
                         return true;
                     }
                 } else if (secondCommand.equalsIgnoreCase("check")) {
                     if (!isAuthorized(playerName)) {
                         getLogger().info(playerName + " attempted unauthorized access of /rank set");
                         return true;
                     }
                     if (args.length > 1) {
                         String player = args[1];
 
                         String rank = getRank(player);
 
                         if (!"".equals(rank)) {
                             sender.sendMessage(player + " has a rank of " + rank);
                         } else {
                             sender.sendMessage(player + " has no rank");
                         }
                     }
                 }
             } else {
                 sender.sendMessage("/rank <set|remove|check>");
             }
         } else if(command.equalsIgnoreCase("title")) {
             if (args.length > 0) {
                 String secondCommand = args[0];
 
                 if (secondCommand.equalsIgnoreCase("set")) {
                     if (!isAuthorized(playerName)) {
                         getLogger().info(playerName + " attempted unauthorized access of /rank set");
                         return true;
                     }
                     if (args.length > 2) {
                         String player = args[1];
                         String title = "";
                         for (int i=2;i<args.length;i++) {
                             title += args[i] + " ";
                         }
 
                         title = title.substring(0,title.length()-1);
 
                         if (title == "") {
                             if (!removeTitle(player)) {
                                 sender.sendMessage("Unable to remove title for player " + player);
                             }
                         } else {
                             if (!setTitle(player,title)) {
                                 sender.sendMessage("Unable to set title " + title + " for player " + player);
                             }
                         }
                     } else {
                         sender.sendMessage("/title set <player> <title>");
 
                     }
                 } else if (secondCommand.equalsIgnoreCase("check")) {
                     if (args.length > 1) {
                         String player = args[1];
 
                         String title = getTitle(player);
 
                         sender.sendMessage("Title for " + player + ": " + title);
                     } else {
                         sender.sendMessage("/title check <player>");
 
                     }
                 }
             } else {
                 sender.sendMessage("/title <set|check>");
             }
         } else {
             return false;
         }
         return true;
     }
 }
