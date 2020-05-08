 package com.pjbollinger.guilds;
 
 import com.pjbollinger.guilds.commands.GuildsCommandExecutor;
 import com.pjbollinger.guilds.database.Guild;
 import org.bukkit.plugin.java.JavaPlugin;
 
import java.util.ArrayList;
 import java.util.List;
 
 public class Guilds extends JavaPlugin {
 
     public List<Guild> listOfGuilds;
 
     @Override
     public void onEnable(){
         getLogger().info("Guilds has started.");
         getCommand("gs").setExecutor(new GuildsCommandExecutor(this));
 
        listOfGuilds = new ArrayList<Guild>();

        Guild RSE;
        RSE = new Guild();
         RSE.setName("RSE");
         RSE.setLeader("magicpat2010");
 
         listOfGuilds.add(RSE);
 
         getLogger().info(RSE.getLeader());
         getLogger().info(RSE.getName());
 
     }
 
     @Override
     public void onDisable(){
         getLogger().info("Guilds has stopped.");
     }
 
 }
