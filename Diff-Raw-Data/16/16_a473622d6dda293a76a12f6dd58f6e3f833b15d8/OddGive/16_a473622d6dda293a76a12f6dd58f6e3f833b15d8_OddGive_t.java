 /* This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package info.somethingodd.bukkit.OddGive;
 
 import info.somethingodd.bukkit.OddItem.OddItem;
 import info.somethingodd.bukkit.OddItem.OddItemGroup;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 /**
  * @author Gordon Pettey (petteyg359@gmail.com)
  */
 public class OddGive extends JavaPlugin {
     protected Map<Player, OddItemGroup> lists = null;
     protected List<OddItemGroup> groups = null;
     protected Map<String, OddItemGroup> kits = null;
     protected String logPrefix = null;
     protected Integer defaultQuantity = null;
     protected Logger log = null;
     protected Boolean blacklist = null;
     private PluginDescriptionFile info = null;
     private OddGiveCommandExecutor OGCE = null;
     private OddGivePlayerListener OGPL = null;
     private String configurationFile = null;
 
     private void configure() {
         File configFile = new File(configurationFile);
         if (!configFile.exists())
             if (!writeConfig()) return;
         Configuration config = new Configuration(configFile);
         config.load();
         blacklist = config.getBoolean("blacklist", true);
         defaultQuantity = config.getInt("defaultQuantity", 64);
         for (String groupName : OddItem.getGroups()) {
             OddItemGroup oddItemGroup = OddItem.getItemGroup(groupName);
             List<String> oddgiveType = oddItemGroup.getData().getStringList("oddgive.type", new ArrayList<String>());
             if (oddgiveType.isEmpty()) continue;
             oddItemGroup.setName(groupName);
             if (oddgiveType.contains("kit")) {
                 if (kits == null) kits = new HashMap<String, OddItemGroup>();
                 kits.put(oddItemGroup.getName(), oddItemGroup);
                 log.info(logPrefix + "Added kit \"" + groupName + "\"");
             }
             if (oddgiveType.contains("blacklist") || oddgiveType.contains("whitelist")) {
                 log.info(logPrefix + "Added " + (oddgiveType.contains("blacklist") ? "black" : "white") + "list \"" + groupName + "\"");
                 list(oddItemGroup);
             }
         }
         config.save();
     }
 
     protected void list(OddItemGroup oddItemGroup) {
         if (groups == null) groups = new ArrayList<OddItemGroup>();
         int i = 0;
         while (i < groups.size()) {
             if (groups.get(i).getData().getInt("oddgive.priority", 0) >= oddItemGroup.getData().getInt("oddgive.priority", 0)) break;
             i++;
         }
         groups.add(i, oddItemGroup);
     }
 
     protected void calculate(Player player) {
         if (lists == null) lists = new HashMap<Player, OddItemGroup>();
         OddItemGroup oddItemGroup = new OddItemGroup();
         for (int i = groups.size() - 1; i >= 0; i--) {
             OddItemGroup currentGroup = groups.get(i);
             List<String> types = currentGroup.getData().getStringList("oddgive.type", new ArrayList<String>());
             boolean blacklist = true;
             if (types.contains("blacklist")) {
                 blacklist = true;
             } else if (types.contains("whitelist")) {
                 blacklist = false;
             }
             for (ItemStack itemStack : currentGroup) {
                 if (oddItemGroup.contains(itemStack) && this.blacklist && !blacklist) {
                     oddItemGroup.remove(itemStack);
                 } else if (!oddItemGroup.contains(itemStack) && this.blacklist && blacklist) {
                     oddItemGroup.add(itemStack);
                 }
             }
         }
         lists.put(player, oddItemGroup);
     }
 
     @Override
     public void onDisable() {
         log.info(logPrefix + "disabled");
         OGCE = null;
         OGPL = null;
         groups = null;
         lists = null;
         blacklist = null;
         info = null;
         log = null;
         logPrefix = null;
         defaultQuantity = null;
         kits = null;
     }
 
     @Override
     public void onEnable() {
         info = getDescription();
         log = getServer().getLogger();
         logPrefix = "[" + info.getName() + "] ";
         log.info(logPrefix + info.getVersion() + " enabled");
         configurationFile = getDataFolder() + System.getProperty("file.separator") + "OddGive.yml";
         configure();
         for (Player player : getServer().getOnlinePlayers()) {
             calculate(player);
         }
         OGCE = new OddGiveCommandExecutor(this);
         OGPL = new OddGivePlayerListener(this);
         getCommand("give").setExecutor(OGCE);
         getCommand("i").setExecutor(OGCE);
         getCommand("i0").setExecutor(OGCE);
         getCommand("oddgive").setExecutor(OGCE);
         getServer().getPluginManager().registerEvent(Event.Type.PLAYER_LOGIN, OGPL, Event.Priority.Normal, this);
         getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, OGPL, Event.Priority.Normal, this);
     }
 
     private boolean writeConfig() {
         FileWriter fw;
         File config = new File(configurationFile);
         if (!config.getParentFile().exists()) config.getParentFile().mkdir();
         try {
             fw = new FileWriter(configurationFile);
         } catch (IOException e) {
             log.severe(logPrefix + "Couldn't write config file: " + e.getMessage());
             getServer().getPluginManager().disablePlugin(this);
             return false;
         }
         BufferedReader i = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/" + info.getName() + ".yml")));
         BufferedWriter o = new BufferedWriter(fw);
         try {
             String line = i.readLine();
             while (line != null) {
                 o.write(line + System.getProperty("line.separator"));
                 line = i.readLine();
             }
             log.info(logPrefix + "Wrote default config");
         } catch (IOException e) {
             log.severe(logPrefix + "Error writing config: " + e.getMessage());
         } finally {
             try {
                 o.close();
                 i.close();
             } catch (IOException e) {
                 log.severe(logPrefix + "Error saving config: " + e.getMessage());
                 getServer().getPluginManager().disablePlugin(getServer().getPluginManager().getPlugin("OddGive"));
             }
         }
         return true;
     }
 }
