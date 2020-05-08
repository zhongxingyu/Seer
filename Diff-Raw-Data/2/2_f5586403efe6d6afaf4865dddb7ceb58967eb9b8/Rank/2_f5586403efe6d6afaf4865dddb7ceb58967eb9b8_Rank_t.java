 package info.bytecraft.api;
 
 import org.bukkit.ChatColor;
 
 import static org.bukkit.ChatColor.*;
 
 public enum Rank
 {
     NEWCOMER(WHITE),
     SETTLER(GREEN),
     MEMBER(DARK_GREEN),
     CHILD(AQUA),
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
     
     public boolean canBuild()
     {
         return (this != NEWCOMER);
     }
     
     public boolean canKillAnimals()
     {
         return canBuild();
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
     
     public boolean canCreateZones()
     {
         return (this == ELDER || this == PRINCESS);
     }
     
     public boolean canEditZones()
     {
        return (this == ADMIN || this.canCreateZones());
     }
     
     public boolean canKick()
     {
         return (this == ADMIN || this == PRINCESS || this == ELDER || this == PROTECTOR);
     }
     
     public boolean canBan()
     {
         return canKick();
     }
     
     public boolean canMentor()
     {
         return (this == ADMIN || this == MENTOR || this == ELDER || this == PRINCESS);
     }
     
     public boolean canFly()
     {
         return (this == ADMIN || this == ELDER || this == PRINCESS);
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
     
     public boolean canTeleportToPosition()
     {
         return (this == ADMIN || this == ELDER || this == PRINCESS);
     }
     
     public boolean canCreateWarps()
     {
         return (this == ELDER || this == PRINCESS);
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
     
     public boolean canGoToPlayersHomes()
     {
         return (this == ADMIN || isElder());
     }
     
     public boolean canMute()
     {
         return (this == ADMIN || this == PROTECTOR || isElder());
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
 
 
 }
