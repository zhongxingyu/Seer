 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.stutiguias.apimcmmo;
 
 import com.gmail.nossr50.api.ExperienceAPI;
 import com.gmail.nossr50.datatypes.PlayerProfile;
 import com.gmail.nossr50.datatypes.SkillType;
 import com.gmail.nossr50.util.Users;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.logging.Level;
 import me.stutiguias.mcmmorankup.Mcmmorankup;
 import me.stutiguias.profile.Profile;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 /**
  *
  * @author Stutiguias
  */
 public class RankUp {
     
     private static Mcmmorankup plugin;
     
     public RankUp(Mcmmorankup instance)
     {
         plugin = instance;
         Plugin pl = plugin.getServer().getPluginManager().getPlugin("mcMMO");
         if(pl != null) {
             Mcmmorankup.log.log(Level.INFO, plugin.logPrefix + " mcMMO found !!!");
         }
     }
     
     public Boolean tryRankUp(Player player,String skill,String gender) {
       try{
             if(PlayerToIgnore(player)) return false;
             if(GroupToIgnore(player)) return false;
             PlayerProfile _McMMOPlayerProfile =  Users.getProfile(player);
             Integer SkillLevel;
             if(skill.equalsIgnoreCase("POWERLEVEL")) {
                 SkillLevel = getPowerLevel(player);
             }else{
                 SkillLevel = _McMMOPlayerProfile.getSkillLevel(getSkillType(skill));
             }
             String group = "";
             for (Iterator<String> it = plugin.RankUpConfig.get(skill).get(gender).iterator(); it.hasNext();) {
                 String entry = it.next();
                 String[] values = entry.split(",");
                 if(Integer.parseInt(values[0]) < SkillLevel){
                     group = values[1]; 
                 }
             }
             if(!group.equalsIgnoreCase("")) return ChangeGroup(player,group,skill);
       }catch(NullPointerException ex) {
             Mcmmorankup.log.log(Level.WARNING,"Error try to rank up " + ex.getMessage());
             return false;
       }catch(Exception ex) {
             Mcmmorankup.log.log(Level.WARNING,"Error try to rank up " + ex.getMessage());
             ex.printStackTrace();
             return false;
       }
       return false;
     }
     
     public boolean tryRankUpWithoutGroup(Player player,String skill,String gender) {
         try{
             if(PlayerToIgnore(player)) return false;
            if(GroupToIgnore(player)) return false;
             PlayerProfile _McMMOPlayerProfile =  Users.getProfile(player);
             Integer SkillLevel;
             if(skill.equalsIgnoreCase("POWERLEVEL")) {
                 SkillLevel = getPowerLevel(player);
             }else{
                 SkillLevel = _McMMOPlayerProfile.getSkillLevel(getSkillType(skill));
             }
             String Tag = "";
             for (Iterator<String> it = plugin.RankUpConfig.get(skill).get(gender).iterator(); it.hasNext();) {
                 String entry = it.next();
                 String[] values = entry.split(",");
                 if(Integer.parseInt(values[0]) < SkillLevel){
                     Tag = values[1]; 
                 }
             }
             System.out.print(Tag);
             if(!Tag.equalsIgnoreCase("")) return ChangeTag(player,Tag,skill);
       }catch(NullPointerException ex) {
             Mcmmorankup.log.log(Level.WARNING,"Error try to rank up " + ex.getMessage());
             return false;
       }catch(Exception ex) {
             Mcmmorankup.log.log(Level.WARNING,"Error try to rank up " + ex.getMessage());
             ex.printStackTrace();
             return false;
       }
       return false;
     }
     
     private boolean ChangeTag(Player player,String tag,String skill) {
          Profile _profile = new Profile(plugin, player);
          String tagnow = _profile.getTag();
          if(tagnow == null) tagnow = "Default"; 
          if(!tag.equalsIgnoreCase(tagnow))  {
             _profile.setTag(tag);
             plugin.getServer().broadcastMessage("----------------[McMMORANKUP]------------------------");
             plugin.getServer().broadcastMessage(plugin.parseColor(BroadcastMessage(player, tag, skill)));
             plugin.getServer().broadcastMessage("-----------------------------------------------------");
             return true;
         }else{
             return false;
         }
     }
     
     private boolean ChangeGroup(Player player,String group,String skill)
     {
         String groupnow = plugin.permission.getPrimaryGroup(player);
         boolean state = plugin.permission.playerAddGroup(player.getWorld(),player.getName(),group);
         String[] plgr = plugin.permission.getPlayerGroups(player);
         for(String gr:plgr) {
           if(!gr.equals(group))
             state = plugin.permission.playerRemoveGroup(player.getWorld(), player.getName(), gr);
         }
         if(!groupnow.equalsIgnoreCase(group))  {
             plugin.getServer().broadcastMessage("----------------[McMMORANKUP]------------------------");
             plugin.getServer().broadcastMessage(plugin.parseColor(BroadcastMessage(player, group, skill)));
             plugin.getServer().broadcastMessage("-----------------------------------------------------");
             return state;
         }else{
             return false;
         }
             
     }
     
     private static int getPowerLevel(Player player)
     {
         return ExperienceAPI.getPowerLevel(player);
     }
     
     private String BroadcastMessage(Player player,String group,String skill)
     {
         if(plugin.UseAlternativeBroadcast) {
             try {
                HashMap<String,String> _BroadCast = plugin.BroadCast.get(skill);
                String bc = _BroadCast.get(group);
                return plugin.MPromote.replace("%player%", player.getName()).replace("%group%", bc);
             }catch(Exception ex) {
                 Mcmmorankup.log.log(Level.WARNING,"Error try to broadcast Alternative " + ex.getMessage());
                 ex.printStackTrace();
                 return "Error try to broadcast Alternative";
             }
         }else {
             return plugin.MPromote.replace("%player%", player.getName()).replace("%group%", group);
         }
     }
     
     
     public SkillType getSkillType(String skill) {
          if(skill.equalsIgnoreCase("EXCAVATION")) return SkillType.EXCAVATION;
          if(skill.equalsIgnoreCase("FISHING")) return SkillType.FISHING;
          if(skill.equalsIgnoreCase("HERBALISM")) return SkillType.HERBALISM;
          if(skill.equalsIgnoreCase("MINING")) return SkillType.MINING;
          if(skill.equalsIgnoreCase("AXES")) return SkillType.AXES;
          if(skill.equalsIgnoreCase("ARCHERY")) return SkillType.ARCHERY;
          if(skill.equalsIgnoreCase("SWORDS")) return SkillType.SWORDS;
          if(skill.equalsIgnoreCase("TAMING")) return SkillType.TAMING;
          if(skill.equalsIgnoreCase("UNARMED")) return SkillType.UNARMED;
          if(skill.equalsIgnoreCase("ACROBATICS")) return SkillType.ACROBATICS;
          if(skill.equalsIgnoreCase("REPAIR")) return SkillType.REPAIR;
          return null;
     }
     
     public Boolean PlayerToIgnore(Player player) {
         for(String PlayerToIgnore:plugin.PlayerToIgnore) {
             if(player.getName().equalsIgnoreCase(PlayerToIgnore)) {
                 return true;
             }
         }
         return false;
     }
     
     public Boolean GroupToIgnore(Player player) {
         for (String GroupToIgnore  : plugin.GroupToIgnore) {
             if(GroupToIgnore.equalsIgnoreCase(plugin.permission.getPrimaryGroup(player))) {
                 return true;
             }
         }
         return false;
     }
 }
