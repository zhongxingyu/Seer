 package com.minecraftserver.castlebreach;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.minecraftserver.castlebreach.commands.CBCommandHandler;
 
 public class CastleBreach extends JavaPlugin {
 
     Logger           log = Logger.getLogger("MineCraft");
     CBSQLManager     sqlman;
     CBCommandHandler cbcmd;
 
     public void onEnable() {
         if (!getDataFolder().exists()) getDataFolder().mkdir();
         log.info("[CastleBreach] Enabled system, version: " + getVersion() + "!");
         sqlman = new CBSQLManager(this);
         cbcmd = new CBCommandHandler();
     }
 
     public void onDisable() {
         sqlman.stop();
         log.info("[CastleBreach] Disabled system!");
     }
 
     public String getVersion() {
         return this.getDescription().getVersion();
     }
 
     public String getAuthor() {
         List<String> CBAuthor = this.getDescription().getAuthors();
         String CBAuthor_String = CBAuthor.toString();
         return CBAuthor_String;
     }
 
     public boolean onCommand(CommandSender cmdsender, Command cmd, String label, String[] args) {
         if (!cmd.getName().equalsIgnoreCase("cb")) {
             return false;
         }
         //testing
         if(args.length>=0){
            Bukkit.broadcastMessage("Player Jim got"+sqlman.getPoints("Jim")+" Points!");
            Bukkit.broadcastMessage("Player Jim earned 20 Points!");
            sqlman.addPoints("Jim", 20);
            Bukkit.broadcastMessage("Player Jim got"+sqlman.getPoints("Jim")+" Points!");
         }
         cbcmd.executeCommand(cmdsender, cmd, label, args, this);
         return true;
     }
 
 }
