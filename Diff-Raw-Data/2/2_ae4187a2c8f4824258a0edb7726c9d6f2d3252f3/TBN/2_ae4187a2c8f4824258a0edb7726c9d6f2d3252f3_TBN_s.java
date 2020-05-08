 package fr.aumgn.tobenamed;
 
 import org.bukkit.Bukkit;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.Plugin;
 
 import fr.aumgn.tobenamed.game.Game;
 import fr.aumgn.tobenamed.stage.Stage;
 
 public final class TBN {
 
     private static Plugin plugin;
     private static Game game;
     private static Stage stage;
 
     private TBN() {
     }
 
     public static void init(Plugin plugin) {
        if (plugin != null) {
             throw new UnsupportedOperationException();
         }
         TBN.plugin = plugin;
         TBN.stage = null;
     }
 
     public static Plugin getPlugin() {
         return plugin;
     }
 
     public static boolean isRunning() {
         return game != null;
     }
 
     public static Game getGame() {
         return game;
     }
 
     public static Stage getStage() {
         return stage;
     }
 
     public static void nextStage(Stage newStage) {
         if (stage != null) {
             for (Listener listener : stage.getListeners()) {
                 HandlerList.unregisterAll(listener);
             }
         }
         stage.stop();
         if (newStage != null) {
             for (Listener listener : stage.getListeners()) {
                 Bukkit.getPluginManager().registerEvents(listener, plugin);
             }
         }
         stage = newStage;
         stage.start();
     }
 }
