 package com.undeadscythes.udsplugin;
 
 import com.undeadscythes.udsplugin.SaveablePlayer.Rank;
 
 /**
  * Permissions to use within UDSPlugin.
  * @author UndeadScythes
  */
 public enum Perm {
     A(Rank.MOD, true),
     ACCEPTRULES(Rank.DEFAULT, false),
     BACK(Rank.MEMBER, true),
     BAN(Rank.MOD, true),
     BOUNTY(Rank.MEMBER, true),
     BROADCAST(Rank.MOD, true),
     BUTCHER(Rank.MOD, true),
     C(Rank.MEMBER, true),
     CALL(Rank.DEFAULT, true),
     CHALLENGE(Rank.MEMBER, true),
     CHECK(Rank.DEFAULT, true),
     CI(Rank.MEMBER, true),
     CITY(Rank.MEMBER, true),
     CLAN(Rank.MEMBER, true),
     DAY(Rank.WARDEN, true),
     DELWARP(Rank.WARDEN, true),
     DEMOTE(Rank.MOD, true),
     ENCHANT(Rank.MOD, true),
     FACE(Rank.DEFAULT, true),
     GIFT(Rank.MEMBER, true),
     GOD(Rank.MOD, true),
     HEAL(Rank.WARDEN, true),
     HELP(Rank.DEFAULT, true),
     HOME(Rank.MEMBER, true),
     I(Rank.VIP, true),
     I_ADMIN(Rank.MOD, true),
     IGNORE(Rank.DEFAULT, true),
     INVSEE(Rank.WARDEN, true),
     JAIL(Rank.WARDEN, true),
     KICK(Rank.MOD, true),
     KIT(Rank.DEFAULT, true),
     LOCKDOWN(Rank.ADMIN, true),
     MAP(Rank.DEFAULT, true),
     ME(Rank.DEFAULT, true),
     MONEY(Rank.DEFAULT, true),
     MONEY_OTHER(Rank.WARDEN, true),
     MONEY_ADMIN(Rank.MOD, true),
     N(Rank.DEFAULT, true),
     NICK(Rank.MEMBER, true),
     NICK_OTHER(Rank.MOD, true),
     NIGHT(Rank.WARDEN, true),
     P(Rank.DEFAULT, true),
     PAYBAIL(Rank.DEFAULT, true),
     PET(Rank.MEMBER, true),
     POWERTOOL(Rank.MOD, true),
     PRIVATE(Rank.DEFAULT, true),
     PROMOTE(Rank.MOD, true),
     R(Rank.DEFAULT, true),
     RAIN(Rank.WARDEN, true),
     REGION(Rank.MOD, true),
     RULES(Rank.DEFAULT, true),
     SCUBA(Rank.DEFAULT, true),
     SERVER(Rank.ADMIN, true),
     SETSPAWN(Rank.ADMIN, true),
     SETWARP(Rank.WARDEN, true),
     SHOP(Rank.MEMBER, true),
     SIGNS(Rank.MOD, true),
     SIT(Rank.DEFAULT, true),
     SPAWN(Rank.DEFAULT, true),
     SPAWNER(Rank.MOD, true),
     STACK(Rank.DEFAULT, true),
     STATS(Rank.DEFAULT, true),
     STORM(Rank.WARDEN, true),
    SUM(Rank.WARDEN, true),
     TP(Rank.MOD, true),
     TELL(Rank.DEFAULT, true),
     TGM(Rank.MOD, true),
     UNBAN(Rank.MOD, true),
     UNJAIL(Rank.WARDEN, true),
     VIP(Rank.MEMBER, true),
     VIP_BUY(Rank.MEMBER, false),
     WE(Rank.MOD, true),
     WARP(Rank.DEFAULT, true),
     WHERE(Rank.DEFAULT, true),
     WHO(Rank.DEFAULT, true),
     WHOIS(Rank.DEFAULT, true),
     XP(Rank.MOD, true),
     Y(Rank.DEFAULT, true);
 
     private Rank rank;
     private boolean inherits;
 
     private Perm(Rank rank, boolean inherits) {
         this.rank = rank;
         this.inherits = inherits;
     }
 
     public Rank getRank() {
         return rank;
     }
 
     public boolean inherits() {
         return inherits;
     }
 }
