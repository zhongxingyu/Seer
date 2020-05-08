 package fr.aumgn.diamondrush;
 
 import java.io.IOException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 import fr.aumgn.diamondrush.config.DRConfig;
 import fr.aumgn.diamondrush.exception.NoGameRunning;
 import fr.aumgn.diamondrush.game.Game;
 import fr.aumgn.diamondrush.listeners.GameListener;
 import fr.aumgn.diamondrush.listeners.RegionsListener;
 import fr.aumgn.diamondrush.listeners.SpectatorsListener;
 import fr.aumgn.diamondrush.stage.JoinStage;
 
 public final class DiamondRush {
 
     private static final double GSON_VERSION = 0.0;
 
     private static DiamondRushPlugin plugin;
     private static Game game;
     private static Listener[] listeners;
     private static DRConfig config;
 
     private DiamondRush() {
     }
 
     public static void init(DiamondRushPlugin plugin) {
         if (DiamondRush.plugin != null) {
             throw new UnsupportedOperationException();
         }
         DiamondRush.plugin = plugin;
         DiamondRush.game = null;
         DiamondRush.listeners = new Listener[3];
         DiamondRush.listeners[0] = new GameListener();
         DiamondRush.listeners[1] = new RegionsListener();
         DiamondRush.listeners[2] = new SpectatorsListener();
         reloadConfig();
     }
 
     public static Plugin getPlugin() {
         return plugin;
     }
 
     public static Gson getGson() {
         return new GsonBuilder()
             .setVersion(GSON_VERSION)
             .setPrettyPrinting()
             .create();
     }
 
     public static void reloadConfig() {
         try {
             DiamondRush.config = plugin.loadTBNConfig();
         } catch (IOException exc) {
             plugin.getLogger().warning("Impossible de charger le fichier de configuration.");
             plugin.getLogger().warning("Utilisation des valeurs par défaut.");
             DiamondRush.config = new DRConfig();
         }
     }
 
     public static DRConfig getConfig() {
         return config;
     }
 
     public static boolean isRunning() {
         return game != null;
     }
 
     public static Game getGame() {
         if (!isRunning()) {
             throw new NoGameRunning();
         }
         return game;
     }
 
     public static void initGame(Game game, JoinStage stage) {
         DiamondRush.game = game;
         PluginManager pm = Bukkit.getPluginManager();
         for (Listener listener : listeners) {
             pm.registerEvents(listener, plugin);
         }
         game.nextStage(stage);
     }
 
     public static void forceStop() {
         for (Listener listener : game.getStage().getListeners()) {
             HandlerList.unregisterAll(listener);
         }
         Bukkit.getScheduler().cancelTasks(DiamondRush.getPlugin());
 
         for (Listener listener : listeners) {
             HandlerList.unregisterAll(listener);
         }
         DiamondRush.game = null;
     }
 }
