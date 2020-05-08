 package de.minestar.therock.commands;
 
 import java.util.StringTokenizer;
 
 import org.bukkit.entity.Player;
 
 import de.minestar.minestarlibrary.commands.AbstractExtendedCommand;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 import de.minestar.therock.TheRockCore;
 
 public class AreaCommand extends AbstractExtendedCommand {
 
     public AreaCommand(String syntax, String arguments, String node) {
         super(TheRockCore.NAME, syntax, arguments, node);
         this.description = "Count the changes in a given area.";
     }
 
     public void execute(String[] args, Player player) {
 
         // wrong syntax : too many arguments
        if (args.length > 4) {
             PlayerUtils.sendError(player, TheRockCore.NAME, "Wrong syntax! Too many arguments.");
             PlayerUtils.sendInfo(player, "Example: /tr area 20 time 2d");
             PlayerUtils.sendInfo(player, "Example: /tr area 20 player GeMoschen 2d");
             return;
         }
 
         // get the radius
         int radius = 20;
         try {
             radius = Integer.valueOf(args[0]);
         } catch (Exception e) {
             PlayerUtils.sendError(player, TheRockCore.NAME, "Wrong syntax! Radius must be an integer.");
             PlayerUtils.sendInfo(player, "Example: /tr area 10 time 1d2h3m4s");
             PlayerUtils.sendInfo(player, "Example: /tr area 15 player GeMoschen");
             PlayerUtils.sendInfo(player, "Example: /tr area 20 player GeMoschen 1d2h3m4s");
             return;
         }
 
         // Command: /tr area <RADIUS> time[1d2h3m4s]
         if (args[1].equalsIgnoreCase("time")) {
             // wrong syntax : too less arguments
             if (args.length < 3) {
                 PlayerUtils.sendError(player, TheRockCore.NAME, "Wrong syntax! Too less arguments.");
                 PlayerUtils.sendInfo(player, "Example: /tr area time 1d2h3m4s");
                 return;
             }
 
             // get the timestamp
             int[] times = this.parseString(args[2], player);
             if (times == null) {
                 PlayerUtils.sendError(player, TheRockCore.NAME, "Wrong syntax!");
                 PlayerUtils.sendInfo(player, "Example: /tr area time 1d2h3m4s");
                 return;
             }
 
             long seconds = times[3] + times[2] * 60 + times[1] * 60 * 60 + times[0] * 60 * 60 * 24;
             long timestamp = System.currentTimeMillis() - seconds * 1000;
 
             PlayerUtils.sendInfo(player, TheRockCore.NAME, "Getting results for radius '" + radius + "'...");
             TheRockCore.mainConsumer.flushWithoutThread();
             TheRockCore.databaseHandler.getAreaTimeChanges(player, radius, timestamp);
             return;
         } else if (args[1].equalsIgnoreCase("player")) {
             // wrong syntax : too less arguments
             if (args.length < 3) {
                 PlayerUtils.sendError(player, TheRockCore.NAME, "Wrong syntax! Too less arguments.");
                 PlayerUtils.sendInfo(player, "Example: /tr area 15 player GeMoschen");
                 PlayerUtils.sendInfo(player, "Example: /tr area 20 player GeMoschen 1d2h3m4s");
                 return;
             }
             String targetName = args[2];
             // Command: /tr area <RADIUS> player <Player>
             if (args.length == 3) {
                 PlayerUtils.sendInfo(player, TheRockCore.NAME, "Getting results for player '" + targetName + "' with radius '" + radius + "'...");
                 TheRockCore.mainConsumer.flushWithoutThread();
                 TheRockCore.databaseHandler.getAreaPlayerChanges(player, radius, targetName);
             } else {
                 // Command: /tr area <RADIUS> player <Player> <1d2h3m4s>
                 int[] times = this.parseString(args[3], player);
                 if (times == null) {
                     PlayerUtils.sendError(player, TheRockCore.NAME, "Wrong syntax!");
                     PlayerUtils.sendInfo(player, "Example: /tr area 15 player GeMoschen");
                     PlayerUtils.sendInfo(player, "Example: /tr area 20 player GeMoschen 1d2h3m4s");
                     return;
                 }
 
                 long seconds = times[3] + times[2] * 60 + times[1] * 60 * 60 + times[0] * 60 * 60 * 24;
                 long timestamp = System.currentTimeMillis() - seconds * 1000;
 
                 PlayerUtils.sendInfo(player, TheRockCore.NAME, "Getting results for player '" + targetName + "', time " + args[2] + ", radius '" + radius + "'...");
                 TheRockCore.mainConsumer.flushWithoutThread();
                 TheRockCore.databaseHandler.getAreaPlayerTimeChanges(player, radius, targetName, timestamp);
             }
         }
     }
 
     private int[] parseString(String date, Player player) {
         try {
             int[] result = new int[4];
             // split the string at 'h' OR 'm' OR 'd' and remaine the delimiter
             StringTokenizer st = new StringTokenizer(date, "[d,h,m,s]", true);
             // parsed integer
             int i = 0;
             // date identifier
             char c = 0;
             // parse string
             while (st.hasMoreTokens()) {
                 i = Integer.parseInt(st.nextToken());
                 c = st.nextToken().charAt(0);
                 // assign date
                 fillDates(result, c, i);
             }
             // when all numbers are zero or negative
             if (result[0] < 1 && result[1] < 1 && result[2] < 1 && result[3] < 1) {
                 return null;
             }
             return result;
         } catch (Exception e) {
             return null;
         }
     }
 
     private void fillDates(int[] result, char c, int i) {
         switch (c) {
             case 'd' :
                 result[0] = i;
                 break;
             case 'h' :
                 result[1] = i;
                 break;
             case 'm' :
                 result[2] = i;
                 break;
             case 's' :
                 result[3] = i;
                 break;
         }
     }
 }
