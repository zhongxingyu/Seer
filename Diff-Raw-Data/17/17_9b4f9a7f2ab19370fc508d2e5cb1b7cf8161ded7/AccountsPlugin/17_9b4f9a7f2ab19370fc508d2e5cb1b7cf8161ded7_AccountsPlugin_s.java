 package fr.ethilvan.bukkit.accounts;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.comphenix.protocol.ProtocolLibrary;
 import com.comphenix.protocol.ProtocolManager;
 
 import fr.aumgn.bukkitutils.command.CommandsRegistration;
 import fr.ethilvan.bukkit.accounts.listeners.AccountsListener;
 import fr.ethilvan.bukkit.accounts.listeners.NamePlatePacketListener;
 import fr.ethilvan.bukkit.accounts.listeners.VisitorsListener;
 import fr.ethilvan.bukkit.api.EthilVan;
 import fr.ethilvan.bukkit.api.accounts.Account;
 import fr.ethilvan.bukkit.api.accounts.Accounts;
 import fr.ethilvan.bukkit.impl.accounts.EVAccount;
 import fr.ethilvan.bukkit.impl.accounts.EVAccounts;
 import fr.ethilvan.bukkit.impl.accounts.MinecraftStats;
 import fr.ethilvan.bukkit.impl.accounts.Visitor;
 
 public class AccountsPlugin extends JavaPlugin implements Runnable {
 
     @Override
     public List<Class<?>> getDatabaseClasses() {
         List<Class<?>> list = new ArrayList<Class<?>>();
         list.add(MinecraftStats.class);
         list.add(EVAccount.class);
         list.add(Visitor.class);
         return list;
     }
 
     @Override
     public void onEnable() {
         PluginManager pm = Bukkit.getPluginManager();
 
         Accounts accounts = new EVAccounts(this);
         //EVAccount.initConfig(this);
         EthilVan.registerAccounts(accounts);
 
         Listener accountsListener = new AccountsListener(this);
         Listener visitorListener = new VisitorsListener();
         pm.registerEvents(accountsListener, this);
         pm.registerEvents(visitorListener, this);
 
         ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
         protocolManager.addPacketListener(new NamePlatePacketListener(this));
 
         CommandsRegistration registration =
                 new CommandsRegistration(this, Locale.FRANCE);
         registration.register(new AccountsCommands());
 
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(
                this, this, 6000, 6000);
     }
 
     @Override
     public void run() {
         for (Player player : Bukkit.getOnlinePlayers()) {
             updateStats(player);
         }
     }
 
     private Timestamp currentTime() {
         return new Timestamp(System.currentTimeMillis());
     }
 
     public void updateStats(Player player) {
         updateStats(player, false);
     }
 
     public void updateStats(Player player, boolean death) {
         Account account = EthilVan.getAccounts().get(player);
         account.setLastMinecraftVisit(currentTime());
         if (account instanceof EVAccount) {
             MinecraftStats stats = ((EVAccount)account).getStats();
             stats.updateMaxLevel(player.getLevel());
             if (death) {
                 stats.updateDeath();
             }
             getDatabase().save(stats);
         }
     }
 }
