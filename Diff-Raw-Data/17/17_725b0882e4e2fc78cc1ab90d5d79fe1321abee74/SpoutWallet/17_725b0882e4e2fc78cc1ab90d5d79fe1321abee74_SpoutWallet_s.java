 package me.spiceking.plugins.spoutwallet;
 
 import java.util.logging.Logger;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 
 import com.iConomy.*;
 import com.iConomy.system.Account;
 
 import java.util.HashMap;
 import java.util.UUID;
 
 import me.spiceking.plugins.spoutwallet.listeners.registerListener;
 import me.spiceking.plugins.spoutwallet.listeners.SpoutCraftListener;
 import me.spiceking.plugins.spoutwallet.payment.Method;
 import me.spiceking.plugins.spoutwallet.payment.Method.MethodAccount;
 
 import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.util.config.Configuration;
 
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class SpoutWallet extends JavaPlugin {
     
     public Configuration config;
     
     public Boolean showRank;
     public String fundsString;
     public String rankString;
     public Integer updateSpeed;
     public Integer ySetting;
     
    public PluginDescriptionFile info = null;
     public PluginManager pluginManager = null;
     
     public iConomy iConomy = null;
 
     // This is public so we can
     public Method Method = null;
     
     HashMap fundsLabels = new HashMap();
     HashMap rankLabels = new HashMap();
     
     private final registerListener registerListener = new registerListener(this);
     
     public void onDisable() {
         // Clear economy stuff
        info = null;
         Method = null;
         pluginManager = null;
         iConomy = null;
         RemoveScheduledTasks();
         System.out.println(this + " is now disabled!");
     }
 
     public void onEnable() {
        info = getDescription();
         config = getConfiguration();
         showRank = config.getBoolean("ShowRank", true);
         fundsString = config.getString("Funds", "You have %s with you."); //String test = String.format("test goes here %s more text", "Testing");
         rankString = config.getString("Rank", "Your rank is: #%s");
         updateSpeed = config.getInt("UpdateSpeed", 20);
         ySetting = config.getInt("Height", 3);
         
         if (ySetting < 0){
             ySetting = 3;
             config.setProperty("Height", ySetting);
         }
         
         if (updateSpeed < 20){
             updateSpeed = 20;
             config.setProperty("UpdateSpeed", updateSpeed);
         }
         config.save(); //Save the config!
         Logger log = getServer().getLogger();
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(Type.PLUGIN_ENABLE, registerListener, Priority.Monitor, this);
         pm.registerEvent(Type.PLUGIN_DISABLE, registerListener, Priority.Monitor, this);
         pm.registerEvent(Type.CUSTOM_EVENT, new SpoutCraftListener(this), Priority.Low, this);
         SetupScheduledTasks();
         System.out.println(this + " is now enabled!");
     }
     
     public HashMap getFundsLabels(){
         return fundsLabels;
     }
     
     public HashMap getRankLabels(){
         return rankLabels;
     }
     
     public void SetupScheduledTasks() {
         getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
             public void run() {
                 onSecond();
             }
         }, 50, 20);
     }
     
     public void RemoveScheduledTasks(){
         getServer().getScheduler().cancelTasks(this);
     }
     
     private void onSecond() {
         Player[] players = getServer().getOnlinePlayers();
         for (Player player : players){
             updateGUI(player);
             }
     }
     
     private void updateGUI(Player player) {
         
         SpoutPlayer sPlayer = (SpoutPlayer) player;
         
         if (!sPlayer.isSpoutCraftEnabled())
             return; //Don't crash me bro!
         
         UUID fundsLabelId = (UUID) getFundsLabels().get(player.getName());
         GenericLabel fundsLabel = (GenericLabel) sPlayer.getMainScreen().getWidget(fundsLabelId);
         
         GenericLabel rankLabel = null;
         UUID rankLabelId = (UUID) getRankLabels().get(player.getName());
         rankLabel = (GenericLabel) sPlayer.getMainScreen().getWidget(rankLabelId);
         
         String fundsText = null;
         String rankText = null;
         if (Method != null){
             if (!Method.hasAccount(player.getName()))
                 return;
             MethodAccount balance = Method.getAccount(player.getName());
             fundsText = String.format(fundsString, Method.format(balance.balance()));
             fundsLabel.setText(fundsText);
             fundsLabel.setDirty(true);
             
             if (showRank && ("iConomy".equals(Method.getName())) && ("5".equals(Method.getVersion()))){
                 iConomy = (iConomy) Method.getPlugin();
                 Account account = iConomy.getAccount(player.getName());
                 rankText =  String.format(rankString, account.getRank());
                 rankLabel.setText(rankText);
                 rankLabel.setDirty(true);
             }
             return;
         } else {
             
             fundsLabel.setText("Looks like a supported economy system is not installed or not working");
             fundsLabel.setDirty(true);
             if (showRank){
                 rankLabel.setText("");
                 rankLabel.setDirty(true);
             }
             return;
         }
     }
 }
