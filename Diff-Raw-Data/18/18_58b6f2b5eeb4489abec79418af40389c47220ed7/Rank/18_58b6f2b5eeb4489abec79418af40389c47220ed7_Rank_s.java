 package info.bytecraft.api;
 
import org.bukkit.ChatColor;
 
import static org.bukkit.ChatColor.*;
 
 public enum Rank
 {
     NEWCOMER(WHITE),
     SETTLER(GREEN),
     MEMBER(DARK_GREEN),
     CHILD(AQUA),
     NOBLE(GOLD),
     LORD(GOLD),
     MENTOR(DARK_PURPLE),
     PROTECTOR(BLUE),
     ARCHITECT(YELLOW),
     ADMIN(RED),
     PRINCESS(LIGHT_PURPLE),
     ELDER(DARK_RED);
     
     private final ChatColor color;
     
     private Rank(ChatColor color)
     {
         this.color = color;
     }
 
     public ChatColor getColor()
     {
         return color;
     }
     
     public static Rank getRank(String name)
     {
         for(Rank rank: values()){
             if(rank.name().equalsIgnoreCase(name)){
                 return rank;
             }
         }
         return Rank.NEWCOMER;
     }
     
     public boolean canSpawnTools()
     {
         return (this == ADMIN || this == ARCHITECT || isElder());
     }
     
     public boolean canBuild()
     {
         return (this != NEWCOMER);
     }
     
     public boolean canKillAnimals()
     {
         return canBuild();
     }
     
     public boolean canNuke()
     {
         return (this == ADMIN || isElder());
     }
     
     public boolean canVanish()
     {
         return (this == ELDER || this == PRINCESS);
     }
     
     public boolean canFill()
     {
         return (this == ARCHITECT
                 || this == ADMIN || this == PRINCESS || this == ELDER);
     }
     
     public boolean canCompassTeleport()
     {
         return (this == NOBLE || this == LORD
                 || this == PROTECTOR || this == MENTOR || this == ADMIN
                 || this == ARCHITECT || isElder());
     }
     
     public boolean canCreateZones()
     {
         return (this == ADMIN || this == ELDER || this == PRINCESS);
     }
     
     public boolean canEditZones()
     {
         return (this == ADMIN || this.canCreateZones());
     }
     
     public boolean canKick()
     {
         return (this == ADMIN || this == MENTOR || isElder() || this == PROTECTOR);
     }
     
     public boolean canBan()
     {
         return (this == ADMIN || isElder() || this == PROTECTOR);
     }
     
     public boolean canMentor()
     {
         return (this == ADMIN || this == MENTOR || this == ELDER || this == PRINCESS || this == PROTECTOR);
     }
     
     public boolean canFly()
     {
         return (this == NOBLE || this == LORD
                 || this == PROTECTOR || this == MENTOR || this == ARCHITECT
                 ||this == ADMIN||isElder());
     }
     
     public boolean canBless()
     {
         return (this == PROTECTOR || this == ADMIN || this == ELDER || this == PRINCESS || this == MENTOR);
     }
     
     public boolean canSeeChestLogs()
     {
         return (this == ADMIN || this == ELDER || this == PRINCESS);
     }
     
     public boolean canSpawnMobs()
     {
         return (this == ADMIN || this == ELDER || this == PRINCESS);
     }
     
     public boolean canSwitchGamemodes()
     {
         return canFill();
     }
     
     public boolean canSpawnItems()
     {
         return canFill();
     }
     
     public boolean canSpawnItemsForPlayers()
     {
         return (this == ADMIN || this == ELDER || this == PRINCESS);
     }
     
     public boolean canViewInventories()
     {
         return (this == ADMIN || this == ELDER || this == PRINCESS);
     }
     
     public boolean canUseGod()
     {
         return (this == ADMIN || this == ELDER || this == PRINCESS);
     }
     
     public boolean canSmite()
     {
         return canUseGod();
     }
     
     public boolean canSummon()
     {
         return (this == ADMIN || this == ELDER || this == PRINCESS);
     }
     
     public boolean canTeleport()
     {
         return (this != Rank.NEWCOMER);
     }
     
     public boolean canTeleportBlock()
     {
         return (this == NOBLE || this == LORD
                 || this == PROTECTOR || this == MENTOR || this == ADMIN
                 || this == ARCHITECT || isElder());
     }
     
     public boolean canChangeTime()
     {
         return (this == NOBLE || this == LORD
                 || this == PROTECTOR || this == MENTOR || this == ADMIN
                 || this == ARCHITECT || isElder());
     }
     
     public boolean canTeleportToPosition()
     {
         return (this == ADMIN || this == ELDER || this == PRINCESS);
     }
     
     public boolean canCreateWarps()
     {
         return (this == ELDER || this == PRINCESS || this == ADMIN);
     }
     
     public boolean canSeePlayerInfo()
     {
         return (this == ADMIN || this == PRINCESS || this == ELDER);
     }
 
     public boolean canOverrideBless()
     {
         return (this == ADMIN || this == PRINCESS || this == ELDER);
     }
     
     public boolean isImmortal()
     {
         return (this == ADMIN || this == PRINCESS || this == ELDER);
     }
     
     public boolean canSaveHomes()
     {
         return (this == SETTLER 
                 || this == MEMBER || this == NOBLE 
                 || this == LORD || this == ADMIN 
                 || this == PROTECTOR || this == ARCHITECT 
                 || this == MENTOR || isElder());
     }
     
     public boolean canGoToPlayersHomes()
     {
         return (this == ADMIN || isElder());
     }
     
     public boolean canMute()
     {
         return (this == ADMIN || this == MENTOR || this == PROTECTOR || isElder());
     }
     
     public boolean canWarn()
     {
         return (this == ADMIN || this == PROTECTOR || isElder());
     }
     
     public boolean canTeleportSilently()
     {
         return (this == ADMIN || isElder());
     }
     
     public boolean canOverrideTeleportBlock()
     {
         return (this == ADMIN || isElder());
     }
     
     private boolean isElder()
     {
         return (this == ELDER || this == PRINCESS);
     }
     
     public boolean canRide()
     {
         return isElder();
     }
     
     @Override
     public String toString()
     {
         return name().toLowerCase();
     }
 
     public boolean canEditBooks()
     {
         return (this == ADMIN || isElder());
     }
 
     public boolean canUseColoredChat()
     {
         return (isElder());
     }
 
     public boolean canFlyFast()
     {
         return (this.isElder());
     }
 
     public boolean canSpawnHeads()
     {
         return (this == ADMIN || isElder());
     }
 
     public boolean canChangeName()
     {
        return isElder();
     }
 
     public boolean canKeepItems()
     {
         return (this == ADMIN || this == ARCHITECT || isElder());
     }
     
     public int getMaxHomes()
     {
         if(isImmortal()) return 10;
         if(this == PROTECTOR || this == ARCHITECT
                 || this == MENTOR)return 5;
         if(this == LORD) return 5;
         if(this == NOBLE)return 3;
         return 1;
     }
     
     public int getMaxTeleportDistance()
     {
         if(isImmortal() || this == Rank.PROTECTOR)return Integer.MAX_VALUE;
         if(this == LORD)return 15000;
         if(this == NOBLE)return 10000;
         
         return 300;
     }
     
     public long getTeleportTimeout()
     {
         if(isImmortal()) return 20 * 0L;
         if(this == Rank.PROTECTOR) return 20 * 1L;
         if(this == Rank.MENTOR) return 20 * 2L;
         if(this == LORD) return 20 * 3L;
         if(this == NOBLE) return 20 * 4L;
         
         return 20 * 5L;
     }
 
     public long getWarpTimeout()
     {
         if(isImmortal()) return 20 * 0L;
         if(this == Rank.PROTECTOR) return 20 * 1L;
         if(this == Rank.MENTOR) return 20 * 2L;
         if(this == LORD) return 20 * 3L;
         if(this == NOBLE) return 20 * 4L;
         return 20 * 5L;
     }
     
     public int getBackCost()
     {
         if(isImmortal())return 0;
         if(this == LORD)return 500;
         if(this == NOBLE)return 700;
         return 1000;
     }
     
     public int getToolCost()
     {
         if(isImmortal())return 0;
         if(this == LORD)return 500;
         if(this == NOBLE)return 700;
         return 1000;
     }
     
     public int getRareDropTimeout()
     {
         if(isImmortal())return 0;//admins dont have to wait
         if(this == LORD)return 2;
         if(this == NOBLE)return 3;
         return 5;
     }
     
     public int getRareDropIncrease()
     {
         if(isImmortal())return 3;
         if(this == LORD)return 2;
         if(this == NOBLE)return 1;
         return 0;
     }
 
     public boolean canBrush()
     {
         return isElder();
     }
 }
