 package uk.co.jacekk.bukkit.spawnertest;
 
import uk.co.jacekk.bukkit.baseplugin.v9_1.BasePlugin;
 
 public class SpawnerTest extends BasePlugin {
     
     @Override
     public void onEnable(){
         super.onEnable(false);
         
         this.commandManager.registerCommandExecutor(new SetSpawnerPropertiesExecutor(this));
         this.pluginManager.registerEvents(new TNTListener(this), this);
     }
     
 }
