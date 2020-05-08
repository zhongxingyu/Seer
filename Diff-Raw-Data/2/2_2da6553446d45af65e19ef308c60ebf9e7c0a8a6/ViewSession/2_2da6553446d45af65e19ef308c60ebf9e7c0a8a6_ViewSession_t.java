 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.wazari.service.exchange;
 
 import java.io.File;
 import net.wazari.dao.entity.Utilisateur;
 import net.wazari.dao.exchange.ServiceSession;
 
 /**
  *
  * @author kevin
  */
 public interface ViewSession extends ServiceSession {
     enum Special {
        TOP5, FULLSCREEN, VISIONNEUSE, CLOUD, PERSONS, PLACES, UPDATE, YEARS, PHOTOALBUM_SIZE,
         SELECT, RANDOM, ABOUT, FASTEDIT, JUST_THEME, MAP, GRAPH, GPX}
 
     enum Action {
         DEFAULT,
         SAVE, SUBMIT, EDIT, IMPORT, 
         DELTAG, MODGEO, MODVIS, MODTAG, NEWTAG, MASSEDIT, LOGIN, DELTHEME, LINKTAG, MODPERS,
         TRUNK, EXPORT, CHECK_DB, CHECK_FS, STATS, PLUGINS, RELOAD_PLUGINS, CREATE_DIRS, SAVE_CONFIG, RELOAD_CONFIG, PRINT_CONFIG,   MODMINOR, SETHOME}
 
     enum Box {
         NONE, MULTIPLE, LIST, MAP, MAP_SCRIPT
     }
 
     enum Mode {
         TAG_USED, TAG_NUSED, TAG_ALL, TAG_NEVER, TAG_NEVER_EVER, TAG_GEO
     }
 
     boolean getCompleteChoix();
 
     Special getSpecial();
 
     Action getAction();
 
     Utilisateur getUser();
 
     boolean isAuthenticated();
 
     File getTempDir();
 
     Configuration getConfiguration();
     
     Integer getThemeId();
 
     boolean isRemoteAccess();
     
     boolean directFileAccess();
     
     void setDirectFileAccess(boolean access);
     
     void setStatic(boolean statik);
     
     boolean getStatic();
 }
