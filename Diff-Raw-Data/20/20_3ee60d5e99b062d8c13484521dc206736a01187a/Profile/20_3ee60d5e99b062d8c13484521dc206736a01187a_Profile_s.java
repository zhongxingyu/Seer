 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.stutiguias.profile;
 
 import com.gmail.nossr50.datatypes.PlayerProfile;
 import com.gmail.nossr50.util.Users;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import me.stutiguias.mcmmorankup.Mcmmorankup;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author Daniel
  */
 public class Profile {
     
     Player player;
     Mcmmorankup plugin;
     String HabilityForRank;
     File configplayerfile;
     YamlConfiguration PlayerYML;
     
     public Profile(Mcmmorankup plugin,Player player) {
         configplayerfile = new File("plugins"+ File.separator +"Mcmmorankup"+ File.separator +"userdata"+ File.separator + player.getName() +".yml");
         PlayerYML = new YamlConfiguration();
         boolean havetocreate = false;
         try {
             havetocreate = configplayerfile.createNewFile();
         }catch(IOException ex) {
             Mcmmorankup.log.warning(plugin.logPrefix + " Can't create the user file" + ex.getMessage() );
         }
         initLoadYML();
        if(!havetocreate) {
            Mcmmorankup.log.info( plugin.logPrefix + " Profile of user " + player.getName() + " found!" );
        }else{
             Mcmmorankup.log.info( plugin.logPrefix + " Profile of user " + player.getName() + " not found, create new one!" );
             PlayerYML.set("Gender", "Male");
             PlayerYML.set("HabilityForRank", plugin.DefaultSkill);
            PlayerYML.set("Tag","Default");
             if(setInitRank()) Mcmmorankup.log.info("Player " + player.getName() + " rank line is " + plugin.DefaultSkill);
         }
         this.player = player;
         this.plugin = plugin;
 
     }
     
     private boolean setInitRank() {
        SaveYML();
        return true;
     }
     
     public boolean setHabilityForRank(String HabilityForRank) {
         PlayerProfile _playerprofile;
         try {
             _playerprofile = Users.getProfile(player);
         }catch(Exception ex) {
             Mcmmorankup.log.info(" Can't find profile for player " + player.getName());
             Mcmmorankup.log.info(ex.getMessage());
             player.sendMessage(plugin.logPrefix + " " + plugin.NotHaveProfile);
             return false;
         }
         if(_playerprofile != null) {
                 
                 if(HabilityForRank.equalsIgnoreCase("EXCAVATION")) {
                     SendMessage(player, HabilityForRank);
                 } else if(HabilityForRank.equalsIgnoreCase("FISHING")) {
                     SendMessage(player, HabilityForRank);
                 } else if(HabilityForRank.equalsIgnoreCase("HERBALISM")) {
                     SendMessage(player, HabilityForRank);
                 } else if(HabilityForRank.equalsIgnoreCase("MINING")) {
                     SendMessage(player, HabilityForRank);
                 } else if(HabilityForRank.equalsIgnoreCase("AXES")) {
                     SendMessage(player, HabilityForRank);
                 } else if(HabilityForRank.equalsIgnoreCase("ARCHERY")) {
                     SendMessage(player, HabilityForRank);
                 } else if(HabilityForRank.equalsIgnoreCase("SWORDS")) {
                     SendMessage(player, HabilityForRank);
                 } else if(HabilityForRank.equalsIgnoreCase("TAMING")) {
                     SendMessage(player, HabilityForRank);
                 } else if(HabilityForRank.equalsIgnoreCase("UNARMED")) {
                     SendMessage(player, HabilityForRank);
                 } else if(HabilityForRank.equalsIgnoreCase("ACROBATICS")) {
                     SendMessage(player, HabilityForRank);
                 } else if(HabilityForRank.equalsIgnoreCase("REPAIR")) { 
                     SendMessage(player, HabilityForRank);
                 } else if(HabilityForRank.equalsIgnoreCase("POWERLEVEL")) {
                     SendMessage(player, HabilityForRank);
                 } else{
                     return false;
                 }
         }
                 
         PlayerYML.set("HabilityForRank", HabilityForRank);
         SaveYML();
         return true;
     }
     
     public Boolean setTag(String Tag) {
         PlayerYML.set("Tag",Tag);
         SaveYML();
         return true;
     }
     
     public String getTag() {
         return PlayerYML.getString("Tag");
     }
     
     public String getHabilityForRank(){
         return PlayerYML.getString("HabilityForRank");
     }
     
     public Boolean setGender(String gender) {
         PlayerYML.set("Gender", gender);
         SaveYML();
         return true;
     }
     
     public String getGender(){
         return PlayerYML.getString("Gender");
     }
     
     public void SendMessage(Player player,String Hability) {
              player.sendMessage("-----------------------------------------------------");
              player.sendMessage(plugin.parseColor(plugin.ChooseHability.replace("%hability%", Hability)));
              player.sendMessage("-----------------------------------------------------");
     }
     
     private void initLoadYML() {
         LoadYML();
     }
     
     public void LoadYML() {
         try {
             PlayerYML.load(configplayerfile);
         } catch (FileNotFoundException ex) {
            Mcmmorankup.log.warning(plugin.logPrefix + " File Not Found " + ex.getMessage() );
         } catch (IOException ex) {
            Mcmmorankup.log.warning(plugin.logPrefix + " IO Problem " + ex.getMessage() );
         } catch (InvalidConfigurationException ex) {
            Mcmmorankup.log.warning(plugin.logPrefix + " Invalid Configuration " + ex.getMessage() );
         }
     }
     
     public void SaveYML() {
         try {
             PlayerYML.save(configplayerfile);
         } catch (FileNotFoundException ex) {
            Mcmmorankup.log.warning(plugin.logPrefix + " File Not Found " + ex.getMessage() );
         } catch (IOException ex) {
            Mcmmorankup.log.warning(plugin.logPrefix + " IO Problem " + ex.getMessage() );
         }
     }
 }
