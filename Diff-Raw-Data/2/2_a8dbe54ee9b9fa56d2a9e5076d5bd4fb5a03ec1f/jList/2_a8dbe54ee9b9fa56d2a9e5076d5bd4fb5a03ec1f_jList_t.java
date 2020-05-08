 package name.richardson.james.jlist;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import name.richardson.james.jlist.commands.ListCommand;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class jList extends JavaPlugin {
 
   private final static Logger logger = Logger.getLogger("Minecraft");
   private final int lineLength = 64;
   
   private PluginDescriptionFile desc;
   
   public void onDisable() {
     log(Level.INFO, String.format("%s is disabled", desc.getName()));
   }
 
   public void onEnable() {
     desc = getDescription();
     
     // register commands
     getCommand("list").setExecutor(new ListCommand(this));
     
     log(Level.INFO, String.format("%s is enabled!", desc.getFullName())); 
   }
   
   public static void log(final Level level, final String msg) {
     logger.log(level, "[jList] " + msg);
   }
   
   public List<Player> getOnlinePlayersInWorld(World world) {
     List<Player> players = new ArrayList<Player>();
     
     for (Player player : world.getPlayers()) {
       if (player.isOnline()) {
          players.add(player);
       }
     }
     
     return players;
   }
   
   public List<Player> getOnlinePlayers() {
     return Arrays.asList(getServer().getOnlinePlayers());
   }
   
   /* 
    * This appears to be the easiest way to handle text wrapping in Minecraft.
    * We keep adding to the string until adding a word would put us over the 64
    * char limit (the width of the text area) where we add a new line instead.
    * 
    * We then later split the string into lines using the /n as a separator and
    * send them as separate messages.
    * 
    * One important thing to note is that the char limit of 64 does NOT include
    * non printable characters which include colour codes and control characters.
    * 
    * If anyone knows of a better way to implement this please let me know!
    */
   
   public String createPlayerList(String header, List<Player> players) {
     StringBuilder message = new StringBuilder();
     final String seperator = "§f, ";
     int currentLine = 1;
     int hiddenChars = 0;
     
     message.append(header);
     
     if (players.size() != 0) {    
       for (Player player : players) {
         int messageLength = ChatColor.stripColor(message.toString()).length() - hiddenChars;
         int availableChars = (lineLength * currentLine) - messageLength;
         int lengthToAppend = player.getName().length() + ChatColor.stripColor(seperator).length();
         
        if (lengthToAppend >= availableChars) {
           message.append("/n");
           hiddenChars = hiddenChars + 2;
           currentLine++;
         }
         
         message.append(player.getDisplayName());
         message.append(seperator);
       }
       message.delete(message.length() - seperator.length(), message.length());
     } else {
       return ChatColor.DARK_GRAY + "There is nobody online!";
     }
     
     return message.toString();
   }
 
   public void sendWrappedMessage(CommandSender sender, String message) {
     for (String line : message.split("/n"))
       sender.sendMessage(line);
     }
   }
