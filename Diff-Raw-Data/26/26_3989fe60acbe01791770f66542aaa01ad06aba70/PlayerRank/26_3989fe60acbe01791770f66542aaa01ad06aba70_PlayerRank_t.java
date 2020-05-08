 package com.undeadscythes.udsplugin;
 
 import org.bukkit.*;
 
 /**
  * A player rank granting permission.
  * 
  * @author UndeadScythes
  */
 public enum PlayerRank {
     NEWBIE(ChatColor.WHITE, 0),
     MEMBER(ChatColor.GREEN, 1),
     VIP(ChatColor.DARK_PURPLE, 1),
     WARDEN(ChatColor.AQUA, 2),
     MOD(ChatColor.DARK_AQUA, 3),
     ADMIN(ChatColor.YELLOW, 4),
    OWNER(ChatColor.GOLD, 5);
 
     private final ChatColor color;
     private final int ranking;
 
     PlayerRank(final ChatColor color, final int rank) {
         this.color = color;
         this.ranking = rank;
     }
 
     public final ChatColor getColor() {
         return color;
     }
 
     public static PlayerRank getByRanking(final int ranking) {
         for(PlayerRank rank : values()) {
             if(rank.ranking == ranking) {
                 return rank;
             }
         }
         return null;
     }
 
     public static PlayerRank getAbove(final PlayerRank rank) {
         return getByRanking(rank.ranking + 1);
     }
 
     public static PlayerRank getBelow(final PlayerRank rank) {
         return getByRanking(rank.ranking - 1);
     }
 
     public static PlayerRank getByName(final String string) {
         for(PlayerRank rank : values()) {
             if(rank.name().equals(string.toUpperCase())) {
                 return rank;
             }
         }
         return null;
     }
 
     @Override
     public final String toString() {
         return name().toLowerCase();
     }
 }
