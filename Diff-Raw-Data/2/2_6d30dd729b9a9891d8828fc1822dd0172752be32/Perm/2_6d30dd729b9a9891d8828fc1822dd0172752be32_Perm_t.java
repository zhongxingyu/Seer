 package com.undeadscythes.udsplugin;
 
 /**
  * Permissions to use within UDSPlugin.
  * @author UndeadScythes
  */
 public enum Perm {
     A(PlayerRank.MOD, true),
     ACCEPTRULES(PlayerRank.DEFAULT, false),
     ADMIN(PlayerRank.ADMIN, true),
     BACK(PlayerRank.MEMBER, true),
     BACK_ON_DEATH(PlayerRank.VIP, true),
     BAN(PlayerRank.MOD, true),
     BOUNTY(PlayerRank.MEMBER, true),
     BROADCAST(PlayerRank.MOD, true),
     BUTCHER(PlayerRank.MOD, true),
     BYPASS(PlayerRank.MOD, true),
     C(PlayerRank.MEMBER, true),
     CALL(PlayerRank.DEFAULT, true),
     CHALLENGE(PlayerRank.MEMBER, true),
     CHAT_HELP(PlayerRank.DEFAULT, true),
     CHECK(PlayerRank.DEFAULT, true),
     CI(PlayerRank.MEMBER, true),
     CITY(PlayerRank.MEMBER, true),
     CLAN(PlayerRank.MEMBER, true),
    COMPASS(PlayerRank.WARDEN, true),
     DAY(PlayerRank.WARDEN, true),
     DELWARP(PlayerRank.WARDEN, true),
     DEMOTE(PlayerRank.MOD, true),
     ENCHANT(PlayerRank.MOD, true),
     FACE(PlayerRank.DEFAULT, true),
     GIFT(PlayerRank.MEMBER, true),
     GOD(PlayerRank.MOD, true),
     HEAL(PlayerRank.WARDEN, true),
     HELP(PlayerRank.DEFAULT, true),
     HOME(PlayerRank.MEMBER, true),
     HOME_OTHER(PlayerRank.MOD, true),
     I(PlayerRank.VIP, true),
     IGNORE(PlayerRank.DEFAULT, true),
     INVSEE(PlayerRank.WARDEN, true),
     ITEM(PlayerRank.DEFAULT, true),
     I_ADMIN(PlayerRank.MOD, true),
     JAIL(PlayerRank.WARDEN, true),
     KICK(PlayerRank.MOD, true),
     KIT(PlayerRank.DEFAULT, true),
     LOCKDOWN(PlayerRank.ADMIN, true),
     MAP(PlayerRank.DEFAULT, true),
     ME(PlayerRank.DEFAULT, true),
     MINECART(PlayerRank.DEFAULT, true),
     MOD(PlayerRank.MOD, true),
     MONEY(PlayerRank.DEFAULT, true),
     MONEY_ADMIN(PlayerRank.MOD, true),
     MONEY_OTHER(PlayerRank.WARDEN, true),
     N(PlayerRank.DEFAULT, true),
     NICK(PlayerRank.MEMBER, true),
     NICK_OTHER(PlayerRank.MOD, true),
     NIGHT(PlayerRank.WARDEN, true),
     P(PlayerRank.DEFAULT, true),
     PAPER_COMPLEX(PlayerRank.MOD, true),
     PAPER_SIMPLE(PlayerRank.DEFAULT, true),
     PAYBAIL(PlayerRank.DEFAULT, true),
     PET(PlayerRank.MEMBER, true),
     POWERTOOL(PlayerRank.MOD, true),
     PRIVATE(PlayerRank.DEFAULT, true),
     PRIZE(PlayerRank.DEFAULT, true),
     PROMOTE(PlayerRank.MOD, true),
     R(PlayerRank.DEFAULT, true),
     RAIN(PlayerRank.WARDEN, true),
     REGION(PlayerRank.MOD, true),
     RULES(PlayerRank.DEFAULT, true),
     SCUBA(PlayerRank.DEFAULT, true),
     SERVER(PlayerRank.ADMIN, true),
     SETSPAWN(PlayerRank.ADMIN, true),
     SETWARP(PlayerRank.WARDEN, true),
     SHOP(PlayerRank.MEMBER, true),
     SHOP_ADMIN(PlayerRank.ADMIN, true),
     SHOP_SERVER(PlayerRank.MOD, true),
     SHOP_SIGN(PlayerRank.MEMBER, true),
     SIGNS(PlayerRank.MOD, true),
     SIGN_CHECKPOINT(PlayerRank.MOD, true),
     SIGN_ITEM(PlayerRank.ADMIN, true),
     SIGN_MINECART(PlayerRank.MOD, true),
     SIGN_PRIZE(PlayerRank.ADMIN, true),
     SIGN_SPLEEF(PlayerRank.MOD, true),
     SIGN_WARP(PlayerRank.MOD, true),
     SIT(PlayerRank.DEFAULT, true),
     SPAWN(PlayerRank.DEFAULT, true),
     SPAWNER(PlayerRank.MOD, true),
     SPLEEF(PlayerRank.DEFAULT, true),
     STACK(PlayerRank.DEFAULT, true),
     STATS(PlayerRank.DEFAULT, true),
     STORM(PlayerRank.WARDEN, true),
     SUN(PlayerRank.WARDEN, true),
     TELL(PlayerRank.DEFAULT, true),
     TGM(PlayerRank.MOD, true),
     TICKET(PlayerRank.DEFAULT, true),
     TP(PlayerRank.MOD, true),
     UNBAN(PlayerRank.MOD, true),
     UNJAIL(PlayerRank.WARDEN, true),
     VIP(PlayerRank.MEMBER, true),
     VIP_BUY(PlayerRank.MEMBER, false),
     VIP_HELP(PlayerRank.VIP, true),
     WAND(PlayerRank.MEMBER, true),
     WARDEN(PlayerRank.WARDEN, true),
     WARP(PlayerRank.DEFAULT, true),
     WE(PlayerRank.WARDEN, true),
     WE_COPY(PlayerRank.MOD, true),
     WE_DRAIN(PlayerRank.MOD, true),
     WE_EXT(PlayerRank.WARDEN, true),
     WE_LOAD(PlayerRank.OWNER, true),
     WE_MOVE(PlayerRank.MOD, true),
     WE_PASTE(PlayerRank.MOD, true),
     WE_REGEN(PlayerRank.MOD, true),
     WE_REPLACE(PlayerRank.MOD, true),
     WE_SAVE(PlayerRank.OWNER, true),
     WE_SET(PlayerRank.MOD, true),
     WE_UNDO(PlayerRank.MOD, true),
     WE_VIP(PlayerRank.VIP, false),
     WHERE(PlayerRank.DEFAULT, true),
     WHO(PlayerRank.DEFAULT, true),
     WHOIS(PlayerRank.DEFAULT, true),
     XP(PlayerRank.MOD, true),
     Y(PlayerRank.DEFAULT, true);
 
     private PlayerRank rank;
     private boolean hereditary;
 
     private Perm(final PlayerRank rank, final boolean hereditary) {
         this.rank = rank;
         this.hereditary = hereditary;
     }
 
     /**
      * Get the rank assigned to the permission.
      * @return A player rank.
      */
     public PlayerRank getRank() {
         return rank;
     }
 
     /**
      * Check if a permission is hereditary.
      * @return
      */
     public boolean isHereditary() {
         return hereditary;
     }
 }
