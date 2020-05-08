 package me.dalton.capturethepoints.commands;
 
 import me.dalton.capturethepoints.CaptureThePoints;
 import me.dalton.capturethepoints.PlayerData;
 import org.bukkit.ChatColor;
 
 public class StatsCommand extends CTPCommand {
    
     /** Allows players to view their kills, deaths, K/D, and money in-game. */
     public StatsCommand(CaptureThePoints instance) {
         super.ctp = instance;
         super.aliases.add("stats");
         super.aliases.add("stat");
         super.aliases.add("info");
         super.notOpCommand = true;
         super.requiredPermissions = new String[]{"ctp.*", "ctp.play", "ctp.admin", "ctp.stats"};
         super.senderMustBePlayer = true;
         super.minParameters = 2;
         super.maxParameters = 2;
         super.usageTemplate = "/ctp stats";
     }
 
     @Override
     public void perform() {
         if (!ctp.blockListener.isAlreadyInGame(player)) {
             player.sendMessage(ChatColor.RED + "You are not in a CTP game!");
             return;
         }
         PlayerData pdata = ctp.playerData.get(player);
         ChatColor cc = pdata.team.chatcolor, white = ChatColor.WHITE, green = ChatColor.GREEN;
         
         player.sendMessage(cc + "Your Stats: ");
         player.sendMessage(cc + "  Kills: " + white + pdata.kills + " (Streak: " + pdata.killsInARow + ")");
         player.sendMessage(cc + "  Deaths: " + white + pdata.deaths + " (Streak: " + pdata.deathsInARow + ")");
         
        double kd = 0.0; // Avoid divding by 0 and rounding
         if (pdata.deaths == 0) {
             kd = pdata.kills;
         } else {
            kd = ((double)pdata.kills * 10.0D) / ((double)pdata.deaths * 10.0D);
            kd = Math.round(kd);
            kd /= 100.0D;
         }
         
         ChatColor goodKD = ChatColor.WHITE;
         if (kd < 0.5) {
             goodKD = ChatColor.RED;
         } else if (kd < 1.0) {
             goodKD = ChatColor.YELLOW;
         } else if (kd < 1.5) {
             goodKD = ChatColor.DARK_GREEN;
         } else {
             goodKD = ChatColor.GREEN;
         }
         
         player.sendMessage(cc + "  K/D: " + goodKD + String.valueOf(kd));
         player.sendMessage(cc + "  Money: " + green + pdata.money);
         return;
     }
 }
