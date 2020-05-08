 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package info.jeppes.ZoneCore.Users;
 
 import de.bananaco.bpermissions.api.CalculableType;
 import org.bukkit.World;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author jeppe
  */
 public class ZoneCoreUserData extends ZoneUserData implements ZoneCoreUser{
 
     public enum ServerGroup {
         Owner,
         Admin,
         SuperModArcane,
         SuperModPremium,
         SuperModArchitect,
         SuperModElite,
         SuperModSponsor,
         SuperMod,
         Mod,
         Arcane,
         Premium,
         Architect,
         Elite,
         Sponsor,
         Donator,
         VIP,
         Regular,
         Basic;
         
         public boolean isStaff(){
             switch(this){
                 case Owner: return true;
                 case Admin: return true;
                 case SuperModArcane: return true;
                 case SuperModPremium: return true;
                 case SuperModArchitect: return true;
                 case SuperModElite: return true;
                 case SuperModSponsor: return true;
                 case SuperMod: return true;
                 case Mod: return true;
                 default: return false;
             }
         }
     }
     
     private RecommendationsHolder recommendationsHolder;
     
     public ZoneCoreUserData(Player player, ConfigurationSection configurationSection) {
         super(player, configurationSection);
         recommendationsHolder = new RecommendationsHolder(this,getConfig());
     }
     public ZoneCoreUserData(String userName, ConfigurationSection configurationSection) {
         super(userName, configurationSection);
         recommendationsHolder = new RecommendationsHolder(this,getConfig());
     }
     
     
     @Override
     public RecommendationsHolder getRecommendationsHolder() {
         return recommendationsHolder;
     }
     
     @Override
     public ServerGroup getServerGroup(World world){
 //        try{
 //            String[] groups = de.bananaco.bpermissions.api.ApiLayer.getGroups(null, CalculableType.USER, null);
 //            for(String groupName : groups){
 //                de.bananaco.bpermissions.api.ApiLayer.getValue(groupName, CalculableType.USER, groupName, groupName)
 //            }
 //            return ServerGroup.valueOf(group.getName());
 //        } catch(Exception e){}
         return getServerGroup();
     }
     
     @Override
     public ServerGroup getServerGroup(){
         String[] groups = de.bananaco.bpermissions.api.ApiLayer.getGroups(null, CalculableType.USER, this.getName());
         return ServerGroup.valueOf(groups[0]);
     }
     
     
     @Override
     public void updatePlayTime() {
         if(getConfig().contains("playtimecheck")){
             long lastPlayTimeChecked = getConfig().getLong("playtimecheck");
            getConfig().set("playtime", getPlayTime() + (System.currentTimeMillis() - lastPlayTimeChecked));
             getConfig().set("playtimecheck", System.currentTimeMillis());
         }
     }
     
     
     @Override
     public long getPlayTime() {
        updatePlayTime();
         return getConfig().getLong("playtime");
     }
 }
