 package com.github.idragonfire.dragonskills;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Set;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class DPlayer {
     private static final SimpleDateFormat FORMAT_OUTPUT = new SimpleDateFormat(
             "yyyy.MM.dd@HH:mm:ss");
     private HashMap<Material, String> bindList;
     private HashMap<String, Date> cooldowns;
     private Player bukkitPlayer;
     private File file;
 
     public DPlayer(PlayerStorage storage, Player bukkitPlayer) {
         this(storage.getPlayerFile(bukkitPlayer), bukkitPlayer);
     }
 
     public DPlayer(File storageFile, Player bukkitPlayer) {
         this.bukkitPlayer = bukkitPlayer;
         file = storageFile;
         bindList = new HashMap<Material, String>();
         cooldowns = new HashMap<String, Date>();
     }
 
     public void addCooldown(String skillName, int seconds) {
         Calendar cal = Calendar.getInstance();
         cal.add(Calendar.SECOND, seconds);
         addCooldown(skillName, cal.getTime());
         save();
     }
 
     public void addCooldown(String skillName, Date time) {
         cooldowns.put(skillName, time);
         save();
     }
 
    public boolean hasCooldown(String skillName) {
        if (!cooldowns.containsKey(skillName)) {
            return false;
        }
        return cooldowns.get(skillName).compareTo(
                Calendar.getInstance().getTime()) > 0;
    }

     public boolean hasBind(Material material) {
         return bindList.containsKey(material);
     }
 
     public void addBind(Material material, String skillName) {
         bindList.put(material, skillName);
         save();
     }
 
     public void removeBind(Material material) {
         bindList.remove(material);
         save();
     }
 
     public void onPlayerInteractEvent(Skills skills, Material mat,
             PlayerInteractEvent event) {
         if (!hasBind(mat)) {
             return;
         }
         event.setCancelled(true);
         skills.useSkill(bindList.get(mat), this);
     }
 
     public Player getBukkitPlayer() {
         return bukkitPlayer;
     }
 
     public void save() {
         FileConfiguration player = new YamlConfiguration();
         // save binds
         ConfigurationSection section = player.createSection("bind");
         for (Material material : bindList.keySet()) {
             section.set(material.toString(), bindList.get(material));
         }
         // save cooldowns
         Date current_time = Calendar.getInstance().getTime();
         section = player.createSection("cooldown");
         for (String skillName : cooldowns.keySet()) {
             if (cooldowns.get(skillName).compareTo(current_time) > 0) {
                 section.set(skillName, FORMAT_OUTPUT.format(cooldowns
                         .get(skillName)));
             }
         }
 
         try {
             player.save(file);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public static DPlayer load(File file, Player bukkitPlayer)
             throws FileNotFoundException, IOException,
             InvalidConfigurationException {
         DPlayer player = new DPlayer(file, bukkitPlayer);
        if (!file.exists()) {
            player.save();
            return player;
        }
         FileConfiguration playerData = new YamlConfiguration();
 
         playerData.load(file);
 
         ConfigurationSection section = playerData
                 .getConfigurationSection("bind");
         Set<String> keys = section.getKeys(false);
         if ((keys != null) && (keys.size() > 0)) {
             for (String materialString : keys) {
                 player.addBind(Material.valueOf(materialString), section
                         .getString(materialString));
             }
         }
 
         section = playerData.getConfigurationSection("cooldown");
         keys = section.getKeys(false);
         if ((keys != null) && (keys.size() > 0)) {
             for (String skillName : keys) {
                 try {
                     player.addCooldown(skillName, FORMAT_OUTPUT.parse(section
                             .getString(skillName)));
                 } catch (ParseException e) {
                     e.printStackTrace();
                 }
             }
         }
         return player;
     }
 }
