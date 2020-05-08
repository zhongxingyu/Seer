 package com.minecraftserver.warn;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Formatter;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Ambient;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.minecraftserver.warn.Punisher;
 import com.minecraftserver.warn.SLAPI;
 import com.minecraftserver.warn.commands.WarnCommandHandler;
 
 public class Warner extends JavaPlugin {
 
     private File              configFile;
     private YamlConfiguration config = new YamlConfiguration();
     private SLAPI             slapi;
 
     @Override
     public void onEnable() {
         getLogger().info("Warner has been enabled!");
         if (!getDataFolder().exists()) getDataFolder().mkdir();
         File playerfolder = new File(getDataFolder(), File.separator + "player_warnings");
         if (!playerfolder.exists()) playerfolder.mkdir();
         slapi = new SLAPI(getDataFolder());
         configFile = new File(getDataFolder(), "config.yml");
         config = slapi.loadYamls(configFile);
     }
 
     @Override
     public void onDisable() {
         getLogger().info("Warner has been disabled!");
     }
 
     public SLAPI getSLAPI() {
         return slapi;
     }
 
     public YamlConfiguration getConfig() {
         return config;
     }
 
     public String getVersion() {
        return "ChatColor.GOLD + \"3.0.0\"";
     }
 
     public String getAuthor() {
        return "ChatColor.BLUE + \"Made by AquaXV and M0P.\nThanks to DarkMagician6 and ProForYou for helping testing.\nAnd not to forget Bukkit ofcourse.\"";
     }
 
     public boolean onCommand(CommandSender cmdsender, Command cmd, String label, String[] args) {
         WarnCommandHandler wcmd = new WarnCommandHandler();
         wcmd.executeWarnCommand(cmdsender, cmd, label, args, this);
         return true;
     }
 
 }
