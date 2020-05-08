 package fr.aumgn.dac2.stage;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.Listener;
 
 import fr.aumgn.dac2.DAC;
 import fr.aumgn.dac2.arena.Arena;
 import fr.aumgn.dac2.exceptions.StageAlreadyRunning;
 
 public class Stages {
 
     private final DAC dac;
     private final List<Stage> stages;
 
     public Stages(DAC dac) {
         this.dac = dac;
         this.stages = new ArrayList<Stage>(dac.getArenas().length());
     }
 
     public Stage get(Arena arena) {
         for (Stage stage : stages) {
             if (stage.getArena() == arena) {
                 return stage;
             }
         }
 
         return null;
     }
 
     public Stage get(Player player) {
         for (Stage stage : stages) {
             if (stage.contains(player)) {
                 return stage;
             }
         }
 
         return null;
     }
 
     public void start(Stage stage) {
         if (get(stage.getArena()) != null) {
             throw new StageAlreadyRunning(dac.getMessages());
         }
 
         stages.add(stage);
         registerListeners(stage);
         stage.start();
     }
 
     public void stop(Stage stage) {
         unregisterListeners(stage);
         stage.stop(false);
         stages.remove(stage);
     }
 
     public void forceStop(Stage stage) {
         unregisterListeners(stage);
         stage.stop(true);
         stages.remove(stage);
     }
 
     public void switchTo(Stage stage) {
         Stage oldStage = get(stage.getArena());
         if (oldStage == null) {
             stages.add(stage);
         } else {
             unregisterListeners(oldStage);
             oldStage.stop(false);
             int index = stages.indexOf(oldStage);
            stages.set(index, stage);
         }
 
         registerListeners(stage);
         stage.start();
     }
 
     private void registerListeners(Stage stage) {
         for (Listener listener : stage.getListeners()) {
             Bukkit.getPluginManager()
                     .registerEvents(listener, dac.getPlugin());
         }
     }
 
     private void unregisterListeners(Stage stage) {
         for (Listener listener : stage.getListeners()) {
             HandlerList.unregisterAll(listener);
         }
     }
 }
