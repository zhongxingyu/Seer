 package uk.co.jacekk.bukkit.spawnertest;
 
import uk.co.jacekk.bukkit.baseplugin.BasePlugin;
 
 public class SpawnerTest extends BasePlugin {
     
     @Override
     public void onEnable(){
         super.onEnable(false);
         
         this.commandManager.registerCommandExecutor(new SetSpawnerPropertiesExecutor(this));
         this.pluginManager.registerEvents(new TNTListener(this), this);
     }
     
 }
