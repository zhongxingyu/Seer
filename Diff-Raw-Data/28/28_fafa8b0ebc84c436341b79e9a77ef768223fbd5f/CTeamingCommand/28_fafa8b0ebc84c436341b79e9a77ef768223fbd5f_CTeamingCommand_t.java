 /*
  * @author     ucchy
  * @license    LGPLv3
  * @copyright  Copyright ucchy 2013
  */
 package com.github.ucchyocean.ct.command;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.scoreboard.Team;
 
 import com.github.ucchyocean.ct.ColorTeaming;
 import com.github.ucchyocean.ct.ColorTeamingAPI;
 import com.github.ucchyocean.ct.Utility;
 import com.github.ucchyocean.ct.config.ColorTeamingConfig;
 import com.github.ucchyocean.ct.config.TeamNameSetting;
 import com.github.ucchyocean.ct.scoreboard.PlayerCriteria;
 import com.github.ucchyocean.ct.scoreboard.SidebarCriteria;
 
 /**
  * colorteaming(ct)コマンドの実行クラス
  * @author ucchy
  */
 public class CTeamingCommand implements CommandExecutor {
 
     private static final String PREERR = ChatColor.RED.toString();
     private static final String PREINFO = ChatColor.GRAY.toString();
 
     private ColorTeaming plugin;
 
     public CTeamingCommand(ColorTeaming plugin) {
         this.plugin = plugin;
     }
 
     /**
      * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
      */
     public boolean onCommand(
             CommandSender sender, Command command, String label, String[] args) {
 
         if ( args.length < 1 ) {
             return false;
         }
 
         if ( args[0].equalsIgnoreCase("reload") ) {
 
             return doReload(sender, command, label, args);
 
         } else if ( args[0].equalsIgnoreCase("removeall") ) {
 
             return doRemoveAll(sender, command, label, args);
 
         } else if ( args.length >= 2 && args[0].equalsIgnoreCase("remove") ) {
 
             return doRemove(sender, command, label, args);
 
         } else if ( args.length >= 2 && args[0].equalsIgnoreCase("trophy") ) {
 
             return doTrophy(sender, command, label, args);
 
         } else if ( args.length >= 2 && args[0].equalsIgnoreCase("reachTrophy") ) {
 
             return doReachTrophy(sender, command, label, args);
 
         } else if ( args.length >= 2 && args[0].equalsIgnoreCase("allowJoinAny") ) {
 
             return doAllowJoinAny(sender, command, label, args);
 
         } else if ( args.length >= 2 && args[0].equalsIgnoreCase("allowJoinRandom") ) {
 
             return doAllowJoinRandom(sender, command, label, args);
 
         } else if ( args.length >= 2 && args[0].equalsIgnoreCase("allowLeave") ) {
 
             return doAllowLeave(sender, command, label, args);
 
         } else if ( args.length >= 3 && args[0].equalsIgnoreCase("add") ) {
 
             return doAdd(sender, command, label, args);
 
         } else if ( args.length >= 2 && args[0].equalsIgnoreCase("side") ) {
 
             return doSide(sender, command, label, args);
 
         } else if ( args.length >= 2 && args[0].equalsIgnoreCase("list") ) {
 
             return doList(sender, command, label, args);
 
         } else if ( args.length >= 2 && args[0].equalsIgnoreCase("below") ) {
 
             return doBelow(sender, command, label, args);
 
         }
 
         return false;
     }
     
     /**
      * reloadコマンドの実行
      * @param sender 
      * @param command 
      * @param label 
      * @param args 
      * @return
      */
     private boolean doReload(
             CommandSender sender, Command command, String label, String[] args) {
         
         plugin.getAPI().realod();
         sender.sendMessage("config.ymlの再読み込みを行いました。");
         return true;
     }
 
     /**
      * 全チーム解散コマンドの実行
      * @param sender 
      * @param command 
      * @param label 
      * @param args 
      * @return
      */
     private boolean doRemoveAll(
             CommandSender sender, Command command, String label, String[] args) {
         
         ColorTeamingAPI api = plugin.getAPI();
 
         HashMap<String, ArrayList<Player>> members =
                 api.getAllTeamMembers();
         boolean removed = false;
         for ( String team : members.keySet() ) {
             if ( api.removeTeam(team) ) {
                 removed = true;
             }
         }
         if ( !removed ) {
             return true; // イベントによる実行キャンセル
         }
 
         // サイドバー削除、タブキーリスト更新
         api.removeSidebarScore();
         api.refreshTabkeyListScore();
         api.refreshBelowNameScore();
 
         sender.sendMessage(PREINFO + "全てのチームが解散しました。");
 
         return true;
     }
 
     /**
      * チーム解散コマンドの実行
      * @param sender 
      * @param command 
      * @param label 
      * @param args 
      * @return
      */
     private boolean doRemove(
             CommandSender sender, Command command, String label, String[] args) {
 
         ColorTeamingAPI api = plugin.getAPI();
 
         String target = args[1];
         if ( !api.isExistTeam(target) ) {
             sender.sendMessage(PREERR + "チーム " + target + " は存在しません。");
             return true;
         }
 
         if ( !api.removeTeam(target) ) {
             return true; // イベントによる実行キャンセル
         }
 
         // サイドバー再作成、タブキーリスト更新
         api.makeSidebarScore();
         api.refreshTabkeyListScore();
         api.refreshBelowNameScore();
         
         sender.sendMessage(PREINFO + "チーム " + target + " が解散しました。");
 
         return true;
     }
     
     /**
      * trophyコマンドの実行
      * @param sender 
      * @param command 
      * @param label 
      * @param args 
      * @return
      */
     private boolean doTrophy(
             CommandSender sender, Command command, String label, String[] args) {
 
         if ( !Utility.tryIntParse(args[1]) && !args[1].equalsIgnoreCase("off") ) {
             sender.sendMessage(PREERR + "キル数 " + args[1] + " は、数値として解釈できません。");
             return true;
         }
 
         int amount;
         if ( args[1].equalsIgnoreCase("off") ) {
             amount = 0;
         } else {
             amount = Integer.parseInt(args[1]);
         }
 
         if ( amount < 0 ) {
             sender.sendMessage(PREERR + "ct trophy コマンドには、マイナス値を指定できません。");
             return true;
         }
 
         ColorTeamingConfig config = plugin.getCTConfig();
         config.setKillTrophy(amount);
         config.saveConfig();
 
         if ( amount == 0 ) {
             sender.sendMessage(PREINFO + "キル数達成時の通知機能をオフにしました。");
         } else {
             sender.sendMessage(PREINFO + "キル数達成時の通知機能を、" + amount + "キル数に設定します。");
         }
         return true;
     }
     
     /**
      * reachTrophyコマンドの実行
      * @param sender 
      * @param command 
      * @param label 
      * @param args 
      * @return
      */
     private boolean doReachTrophy(
             CommandSender sender, Command command, String label, String[] args) {
 
         if ( !Utility.tryIntParse(args[1]) && !args[1].equalsIgnoreCase("off") ) {
             sender.sendMessage(PREERR + "キル数 " + args[1] + " は、数値として解釈できません。");
             return true;
         }
 
         int amount;
         if ( args[1].equalsIgnoreCase("off") ) {
             amount = 0;
         } else {
             amount = Integer.parseInt(args[1]);
         }
 
         if ( amount < 0 ) {
             sender.sendMessage(PREERR + "ct reachTrophy コマンドには、マイナス値を指定できません。");
             return true;
         } else if ( plugin.getCTConfig().getKillTrophy() < amount ) {
             sender.sendMessage(PREERR + "killTrophyの設定値より大きな値は指定できません。");
             return true;
         }
 
         ColorTeamingConfig config = plugin.getCTConfig();
         config.setKillReachTrophy(amount);
         config.saveConfig();
 
         if ( amount == 0 ) {
             sender.sendMessage(PREINFO + "キル数リーチ時の通知機能をオフにしました。");
         } else {
             sender.sendMessage(PREINFO + "キル数リーチ時の通知機能を、" + amount + "キル数に設定します。");
         }
         return true;
     }
     
     /**
      * allowJoinAnyコマンドの実行
      * @param sender 
      * @param command 
      * @param label 
      * @param args 
      * @return
      */
     private boolean doAllowJoinAny(
             CommandSender sender, Command command, String label, String[] args) {
 
         if ( args[1].equalsIgnoreCase("on") ) {
             ColorTeamingConfig config = plugin.getCTConfig();
             config.setAllowPlayerJoinAny(true);
             config.saveConfig();
             sender.sendMessage(PREINFO + "一般プレイヤーの /cjoin (group) の使用が可能になりました。");
             return true;
         } else if ( args[1].equalsIgnoreCase("off") ) {
             ColorTeamingConfig config = plugin.getCTConfig();
             config.setAllowPlayerJoinAny(false);
             config.saveConfig();
             sender.sendMessage(PREINFO + "一般プレイヤーの /cjoin (group) の使用が不可になりました。");
             return true;
         }
 
         return false;
     }
     
     /**
      * allowJoinRandomコマンドの実行
      * @param sender 
      * @param command 
      * @param label 
      * @param args 
      * @return
      */
     private boolean doAllowJoinRandom(
             CommandSender sender, Command command, String label, String[] args) {
 
         if ( args[1].equalsIgnoreCase("on") ) {
             ColorTeamingConfig config = plugin.getCTConfig();
             config.setAllowPlayerJoinRandom(true);
             config.saveConfig();
             sender.sendMessage(ChatColor.GRAY + "一般プレイヤーの /cjoin の使用が可能になりました。");
             return true;
         } else if ( args[1].equalsIgnoreCase("off") ) {
             ColorTeamingConfig config = plugin.getCTConfig();
             config.setAllowPlayerJoinRandom(false);
             config.saveConfig();
             sender.sendMessage(ChatColor.GRAY + "一般プレイヤーの /cjoin の使用が不可になりました。");
             return true;
         }
 
         return false;
     }
     
     /**
      * allowLeaveコマンドの実行
      * @param sender 
      * @param command 
      * @param label 
      * @param args 
      * @return
      */
     private boolean doAllowLeave(
             CommandSender sender, Command command, String label, String[] args) {
 
         if ( args[1].equalsIgnoreCase("on") ) {
             ColorTeamingConfig config = plugin.getCTConfig();
             config.setAllowPlayerLeave(true);
             config.saveConfig();
             sender.sendMessage(ChatColor.GRAY + "一般プレイヤーの /cleave の使用が可能になりました。");
             return true;
         } else if ( args[1].equalsIgnoreCase("off") ) {
             ColorTeamingConfig config = plugin.getCTConfig();
             config.setAllowPlayerLeave(false);
             config.saveConfig();
             sender.sendMessage(ChatColor.GRAY + "一般プレイヤーの /cleave の使用が不可になりました。");
             return true;
         }
 
         return false;
     }
     
     /**
      * addコマンドの実行
      * @param sender 
      * @param command 
      * @param label 
      * @param args 
      * @return
      */
     private boolean doAdd(
             CommandSender sender, Command command, String label, String[] args) {
 
         String target = args[1];
         ColorTeamingAPI api = plugin.getAPI();
         if ( !api.getTeamNameConfig().containsID(target) ) {
             sender.sendMessage(PREERR + "チーム " + target + " は設定できないチーム名です。");
             return true;
         }
 
         boolean isNewGroup;
         boolean isAll = args[2].equalsIgnoreCase("all");
        boolean isRest = args[2].equalsIgnoreCase("rest");
         
         if ( isAll ) {
             
            // ゲームモードがクリエイティブの人は除外する
            ArrayList<Player> tempPlayers =
                    api.getAllPlayersOnWorld(plugin.getCTConfig().getWorldNames());
            ArrayList<Player> players = new ArrayList<Player>();
            for ( Player p : tempPlayers ) {
                if ( p.getGameMode() != GameMode.CREATIVE ) {
                    players.add(p);
                }
            }
            if ( players.size() == 0 ) {
                sender.sendMessage(
                        PREERR + "設定されたワールドに、対象プレイヤーがいないようです。");
                return true;
            }

            isNewGroup = !api.isExistTeam(target);
            TeamNameSetting tns = api.getTeamNameFromID(target);
            for ( Player player : players ) {
                api.addPlayerTeam(player, tns);
            }

            sender.sendMessage(PREINFO + "全てのプレイヤーを、チーム " +
                    tns.getName() + " に追加しました。");
        
        } else if ( isRest ) {
            
             // ゲームモードがクリエイティブの人や、既に色が設定されている人は除外する
             ArrayList<Player> tempPlayers =
                     api.getAllPlayersOnWorld(plugin.getCTConfig().getWorldNames());
             ArrayList<Player> players = new ArrayList<Player>();
             for ( Player p : tempPlayers ) {
                 Team team = api.getPlayerTeam(p);
                 if ( p.getGameMode() != GameMode.CREATIVE &&
                         (team == null || team.getName().equals("") )) {
                     players.add(p);
                 }
             }
             if ( players.size() == 0 ) {
                 sender.sendMessage(
                         PREERR + "設定されたワールドに、対象プレイヤーがいないようです。");
                 return true;
             }
 
             isNewGroup = !api.isExistTeam(target);
             TeamNameSetting tns = api.getTeamNameFromID(target);
             for ( Player player : players ) {
                 api.addPlayerTeam(player, tns);
             }
 
             sender.sendMessage(PREINFO + "未所属のプレイヤーを、チーム " +
                     tns.getName() + " に追加しました。");
             
         } else {
             
             Player player = Bukkit.getPlayerExact(args[2]);
             if ( player == null ) {
                 sender.sendMessage(PREERR + "プレイヤー " + args[2] + " は存在しません。");
                 return true;
             }
 
             isNewGroup = !api.isExistTeam(target);
             TeamNameSetting tns = api.getTeamNameFromID(target);
             api.addPlayerTeam(player, tns);
 
             sender.sendMessage(PREINFO + "プレイヤー " + player.getName() + " をチーム " +
                     tns.getName() + " に追加しました。");
         }
 
         // メンバー情報をlastdataに保存する
         api.getCTSaveDataHandler().save("lastdata");
 
         // サイドバーの更新 チームが増える場合は、再生成する
         if ( isNewGroup ) {
             api.makeSidebarScore();
         }
         api.refreshSidebarScore();
         api.refreshTabkeyListScore();
         api.refreshBelowNameScore();
 
         return true;
     }
     
     /**
      * sideコマンドの実行
      * @param sender 
      * @param command 
      * @param label 
      * @param args 
      * @return
      */
     private boolean doSide(
             CommandSender sender, Command command, String label, String[] args) {
         
         ColorTeamingConfig config = plugin.getCTConfig();
 
         if ( args[1].equalsIgnoreCase("kill") ) {
             config.setSideCriteria(SidebarCriteria.KILL_COUNT);
         } else if ( args[1].equalsIgnoreCase("death") ) {
             config.setSideCriteria(SidebarCriteria.DEATH_COUNT);
         } else if ( args[1].equalsIgnoreCase("point") ) {
             config.setSideCriteria(SidebarCriteria.POINT);
         } else if ( args[1].equalsIgnoreCase("rest") ) {
             config.setSideCriteria(SidebarCriteria.REST_PLAYER);
         } else if ( args[1].equalsIgnoreCase("clear") || args[1].equalsIgnoreCase("none") ) {
             config.setSideCriteria(SidebarCriteria.NONE);
         } else {
             return false;
         }
         config.saveConfig();
 
         // サイドバーの更新
         plugin.getAPI().makeSidebarScore();
 
         String criteria = config.getSideCriteria().toString();
         sender.sendMessage(PREINFO + "サイドバーの表示を" + criteria + "にしました。");
 
         return true;
     }
     
     /**
      * listコマンドの実行
      * @param sender 
      * @param command 
      * @param label 
      * @param args 
      * @return
      */
     private boolean doList(
             CommandSender sender, Command command, String label, String[] args) {
         
         ColorTeamingConfig config = plugin.getCTConfig();
 
         if ( args[1].equalsIgnoreCase("kill") ) {
             config.setListCriteria(PlayerCriteria.KILL_COUNT);
         } else if ( args[1].equalsIgnoreCase("death") ) {
             config.setListCriteria(PlayerCriteria.DEATH_COUNT);
         } else if ( args[1].equalsIgnoreCase("point") ) {
             config.setListCriteria(PlayerCriteria.POINT);
         } else if ( args[1].equalsIgnoreCase("health") ) {
             config.setListCriteria(PlayerCriteria.HEALTH);
         } else if ( args[1].equalsIgnoreCase("clear") || args[1].equalsIgnoreCase("none") ) {
             config.setListCriteria(PlayerCriteria.NONE);
         } else {
             return false;
         }
         config.saveConfig();
 
         // スコアボードの更新
         plugin.getAPI().makeTabkeyListScore();
 
         String criteria = config.getListCriteria().toString();
         sender.sendMessage(PREINFO + "リストの表示を" + criteria + "にしました。");
 
         return true;
     }
     
     /**
      * belowコマンドの実行
      * @param sender 
      * @param command 
      * @param label 
      * @param args 
      * @return
      */
     private boolean doBelow(
             CommandSender sender, Command command, String label, String[] args) {
 
         ColorTeamingConfig config = plugin.getCTConfig();
 
         if ( args[1].equalsIgnoreCase("kill") ) {
             config.setBelowCriteria(PlayerCriteria.KILL_COUNT);
         } else if ( args[1].equalsIgnoreCase("death") ) {
             config.setBelowCriteria(PlayerCriteria.DEATH_COUNT);
         } else if ( args[1].equalsIgnoreCase("point") ) {
             config.setBelowCriteria(PlayerCriteria.POINT);
         } else if ( args[1].equalsIgnoreCase("health") ) {
             config.setBelowCriteria(PlayerCriteria.HEALTH);
         } else if ( args[1].equalsIgnoreCase("clear") || args[1].equalsIgnoreCase("none") ) {
             config.setBelowCriteria(PlayerCriteria.NONE);
         } else {
             return false;
         }
         config.saveConfig();
 
         // スコアボードの更新
         plugin.getAPI().makeBelowNameScore();
 
         // 設定の保存
         String criteria = config.getBelowCriteria().toString();
         sender.sendMessage(PREINFO + "名前欄のスコア表示を" + criteria + "にしました。");
 
         return true;
     }
 }
