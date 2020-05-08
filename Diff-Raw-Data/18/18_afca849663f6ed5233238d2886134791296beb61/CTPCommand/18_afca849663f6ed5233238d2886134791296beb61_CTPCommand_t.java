 /*
  * Copyright ucchy 2013
  */
 package com.github.ucchyocean.cmt.command;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.BlockCommandSender;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 
 import com.github.ucchyocean.cmt.ColorMeTeaming;
 import com.github.ucchyocean.cmt.ColorMeTeamingConfig;
 
 /**
  * @author ucchy
  * colortp(ctp)コマンドの実行クラス
  */
 public class CTPCommand implements CommandExecutor {
 
     private static final String PREERR = ChatColor.RED.toString();
     private static final String PREINFO = ChatColor.GRAY.toString();
 
     /**
      * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
      */
     public boolean onCommand(
             CommandSender sender, Command command, String label, String[] args) {
 
         if ( args.length < 2 ) {
             return false;
         }
 
         String group;
         String world = ColorMeTeamingConfig.defaultWorldName;
         int x_actual, y_actual, z_actual;
 
         if ( args[1].equalsIgnoreCase("here") ) {
 
            group = args[0];

             if ( sender instanceof Player ) {
                 x_actual = ((Player)sender).getLocation().getBlockX();
                 y_actual = ((Player)sender).getLocation().getBlockY();
                 z_actual = ((Player)sender).getLocation().getBlockZ();
                 world = ((Player)sender).getWorld().getName();
             } else if ( sender instanceof BlockCommandSender ) {
                 x_actual = ((BlockCommandSender)sender).getBlock().getX();
                 y_actual = ((BlockCommandSender)sender).getBlock().getY()+1;
                 z_actual = ((BlockCommandSender)sender).getBlock().getZ();
                 world = ((BlockCommandSender)sender).getBlock().getWorld().getName();
             } else {
                 sender.sendMessage(PREERR + "ctp の here 指定は、コンソールからは実行できません。");
                 return true;
             }
 
        } else if ( args[1].equalsIgnoreCase("spawn") ) {

             group = args[0];
 
            Location respawnPoint = ColorMeTeaming.respawnConfig.get(group);

            if ( respawnPoint == null ) {
                sender.sendMessage(PREERR + "グループ " + group + " にリスポーンポイントが指定されていません。");
                return true;
            }

            x_actual = respawnPoint.getBlockX();
            y_actual = respawnPoint.getBlockY();
            z_actual = respawnPoint.getBlockZ();
            world = respawnPoint.getWorld().getName();

         } else if ( args.length == 4 ) {
 
             // 有効な座標が指定されたか確認する
             if ( !checkXYZ(sender, args[1]) ||
                     !checkXYZ(sender, args[2]) ||
                     !checkXYZ(sender, args[3]) ) {
                 return true;
             }
 
             // 実行者がプレイヤーかコマンドブロックなら、worldを取得して設定する
             if ( sender instanceof BlockCommandSender ) {
                 BlockCommandSender block = (BlockCommandSender)sender;
                 world = block.getBlock().getWorld().getName();
             } else if ( sender instanceof Player ) {
                 Player player = (Player)sender;
                 world = player.getWorld().getName();
             }
 
             group = args[0];
             x_actual = Integer.parseInt(args[1]);
             y_actual = Integer.parseInt(args[2]);
             z_actual = Integer.parseInt(args[3]);
 
         } else if ( args.length >= 5 ) {
 
             // 有効な座標が指定されたか確認する
             if ( !checkXYZ(sender, args[2]) ||
                     !checkXYZ(sender, args[3]) ||
                     !checkXYZ(sender, args[4]) ) {
                 return true;
             }
 
             group = args[0];
             world = args[1];
             x_actual = Integer.parseInt(args[2]);
             y_actual = Integer.parseInt(args[3]);
             z_actual = Integer.parseInt(args[4]);
 
         } else {
             // 引数指定不足
             return false;
         }
 
         double x = (double)x_actual + 0.5;
         double y = (double)y_actual;
         double z = (double)z_actual + 0.5;
 
         Hashtable<String, ArrayList<Player>> members = ColorMeTeaming.getAllColorMembers();
 
         // 有効なグループ名が指定されたか確認する
         if ( !members.containsKey(group) ) {
             sender.sendMessage(PREERR + "グループ " + group + " が存在しません。");
             return true;
         }
 
         // 有効なワールド名が指定されたか確認する
         if ( ColorMeTeaming.getWorld(world) == null ) {
             sender.sendMessage(PREERR + "ワールド " + world + " が存在しません。");
             return true;
         }
 
         // テレポートを実行する
         Location loc = new Location(ColorMeTeaming.getWorld(world), x, y, z);
         for ( Player p : members.get(group) ) {
             p.teleport(loc, TeleportCause.COMMAND);
         }
 
         sender.sendMessage(PREINFO + "グループ " + group + " のプレイヤーを全員テレポートしました。");
 
         return true;
     }
 
     private boolean checkXYZ (CommandSender sender, String value) {
 
         // 数値かどうかをチェックする
         if ( !value.matches("-?[0-9]+") ) {
             sender.sendMessage(PREERR + value + " は数値ではありません。");
             return false;
         }
 
         int v = Integer.parseInt(value);
 
         // 大きすぎる値でないかどうかチェックする
         if ( v < -30000000 || 30000000 < v ) {
             sender.sendMessage(PREERR + value + " は遠すぎて指定できません。");
             return false;
         }
 
         return true;
     }
 }
