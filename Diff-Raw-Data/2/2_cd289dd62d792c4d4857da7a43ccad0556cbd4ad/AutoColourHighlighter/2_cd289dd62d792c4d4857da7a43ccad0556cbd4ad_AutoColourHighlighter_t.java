 package me.eccentric_nz.plugins.autocolour;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 public class AutoColourHighlighter implements Listener {
 
     private AutoColour plugin;
     AutoColourDatabase service = AutoColourDatabase.getInstance();
 
     public AutoColourHighlighter(AutoColour plugin) {
         this.plugin = plugin;
     }
 
     public String[][] buildSubstitutions() {
         List<String[]> preList = new ArrayList<String[]>();
         String[][] subs = null;
         try {
             Connection connection = service.getConnection();
             Statement statement = connection.createStatement();
             String querySubs = "SELECT * FROM autocolour";
             ResultSet rsSubs = statement.executeQuery(querySubs);
             if (rsSubs.isBeforeFirst()) {
                 while (rsSubs.next()) {
                     String find = "\\b" + rsSubs.getString("find") + "\\b";
                     String change = "" + rsSubs.getString("colour") + rsSubs.getString("find") + "r";
                     preList.add(new String[]{find, change});
                 }
                 subs = preList.toArray(new String[preList.size()][2]);
                 rsSubs.close();
             }
         } catch (SQLException e) {
            System.out.println(AutoColourConstants.MY_PLUGIN_NAME + "cCouldn't get substitutions:r " + e);
         }
         return subs;
     }
 
     @EventHandler(priority = EventPriority.HIGH)
     public void onChat(AsyncPlayerChatEvent event) {
         Player p = event.getPlayer();
         boolean enabled = this.plugin.getConfig().getBoolean("enabled");
         if (enabled && p.hasPermission("autocolour.use")) {
             event.setMessage(autocolour(event.getMessage()));
         }
     }
 
     public String autocolour(String text) {
         for (String[] words : plugin.replace) {
             //text = Pattern.compile(words[0], Pattern.CASE_INSENSITIVE).matcher(text).replaceAll(words[1]);
             text = Pattern.compile(words[0]).matcher(text).replaceAll(words[1]);
         }
         return text;
     }
 }
