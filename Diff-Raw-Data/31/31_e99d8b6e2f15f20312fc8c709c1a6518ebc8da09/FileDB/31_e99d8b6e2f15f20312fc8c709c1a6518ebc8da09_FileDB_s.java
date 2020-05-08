 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.stutiguias.dao.type;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.logging.Level;
 import me.stutiguias.mcpk.MCPlayer;
 import me.stutiguias.mcpk.Mcpk;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author Daniel
  */
 public class FileDB {
 
     private File PlayerFile;
     private String Path = "plugins"+ File.separator + "Mcpk" + File.separator +"userdata";
     private YamlConfiguration PlayerYML;
     
     public MCPlayer LoadPlayerFile(Player player) {
         try {
             PlayerFile = new File( Path + File.separator + player.getName() +".yml");
             PlayerYML = new YamlConfiguration();
             PlayerYML.load(PlayerFile);
             MCPlayer _MCPlayer = new MCPlayer();
             _MCPlayer.setKills(getKills());
             _MCPlayer.setNewBieProtectUntil(getNewbieProtectUntil());
             return _MCPlayer;
         } catch (FileNotFoundException ex) {
           ex.printStackTrace();
            return null;
         } catch (IOException | InvalidConfigurationException ex) {
            ex.printStackTrace();
            return null;
         }
     }
     
     public void CreatePlayer(Player player,Timestamp newBieProtectUntil) {
         try {
             PlayerFile = new File( Path + File.separator + player.getName() +".yml");
             PlayerFile.createNewFile();
             LoadYML();
             SetupYML(0,newBieProtectUntil);
             SaveYML();
         } catch (IOException ex) {
             Mcpk.logger.log(Level.INFO, "{0} Error Creating new YML player File", Mcpk.logPrefix);
             Mcpk.logger.log(Level.INFO, "{0}", ex.getMessage());
         }
     }
     
     private void SetupYML(int kills,Timestamp newBieProtectUntil) {
         PlayerYML.set("kills", kills);
         PlayerYML.set("NewbieProtectUntil", newBieProtectUntil);
         PlayerYML.set("PKMsg",true);
         PlayerYML.set("AlertMsg",true);
     }
     
     public Boolean getPKMsg() {
         return PlayerYML.getBoolean("PKMsg");
     }
     
     public void setPKMsg(Boolean PKMsg) {
         PlayerYML.set("PKMsg", PKMsg);
         SaveYML();
     }
     
     public Boolean getAlertMsg() {
         return PlayerYML.getBoolean("AlertMsg");
     }
     
     public void setAlertMsg(Boolean AlertMsg) {
         PlayerYML.set("AlertMsg", AlertMsg);
         SaveYML();
     }
     
     public void setKills(int Kills) {
         PlayerYML.set("kills", Kills);
         SaveYML();
     }
     
     public int getKills() {
         return PlayerYML.getInt("kills");
     }
     
     public Date getNewbieProtectUntil() {
         return (Date)PlayerYML.get("NewbieProtectUntil");
     }
     
     public void CheckDiretory() {
         File f = new File(Path);
         if(!f.exists())  {
             Mcpk.logger.log(Level.INFO, "{0} Diretory not exist creating new one", Mcpk.logPrefix);
             f.mkdirs();
         }
     }
     
     private void LoadYML() {
         try {
             PlayerYML = new YamlConfiguration();
             PlayerYML.load(PlayerFile);
         } catch (FileNotFoundException ex) {
            Mcpk.logger.log(Level.WARNING, "{0} File Not Found {1}", new Object[]{Mcpk.logPrefix, ex.getMessage()});
         } catch (IOException ex) {
            Mcpk.logger.log(Level.WARNING, "{0} IO Problem {1}", new Object[]{Mcpk.logPrefix, ex.getMessage()});
         } catch (InvalidConfigurationException ex) {
            Mcpk.logger.log(Level.WARNING, "{0} Invalid Configuration {1}", new Object[]{Mcpk.logPrefix, ex.getMessage()});
         }
     }
     
     public void SaveYML() {
         try {
             PlayerYML.save(PlayerFile);
         } catch (FileNotFoundException ex) {
            Mcpk.logger.log(Level.WARNING, "{0} File Not Found {1}", new Object[]{Mcpk.logPrefix, ex.getMessage()});
         } catch (IOException ex) {
            Mcpk.logger.log(Level.WARNING, "{0} IO Problem {1}", new Object[]{Mcpk.logPrefix, ex.getMessage()});
         }
     }
 }
