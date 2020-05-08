 /*
  * Copyright ucchy 2013
  */
 package com.github.ucchyocean.ct.command;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.github.ucchyocean.ct.ColorTeaming;
 import com.github.ucchyocean.ct.ColorTeamingAPI;
 import com.github.ucchyocean.ct.ColorTeamingConfig;
 import com.github.ucchyocean.ct.Utility;
 import com.github.ucchyocean.ct.event.ColorTeamingPlayerLeaveEvent.Reason;
 import com.github.ucchyocean.ct.scoreboard.CustomScoreInterface;
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
 
             plugin.reloadCTConfig();
             sender.sendMessage("config.ymlの再読み込みを行いました。");
             return true;
 
         } else if ( args[0].equalsIgnoreCase("removeall") ) {
 
             ColorTeamingAPI api = plugin.getAPI();
 
             HashMap<String, ArrayList<Player>> members =
                     api.getAllTeamMembers();
             for ( String group : members.keySet() ) {
                 for ( Player p : members.get(group) ) {
                     api.leavePlayerTeam(p, Reason.TEAM_REMOVED);
                 }
                 api.removeTeam(group);
             }
 
             // サイドバー削除、タブキーリスト更新
             api.removeSidebarScore();
             api.refreshTabkeyListScore();
             api.refreshBelowNameScore();
 
             sender.sendMessage(PREINFO + "全てのグループが解散しました。");
 
             return true;
 
         } else if ( args.length >= 2 && args[0].equalsIgnoreCase("remove") ) {
 
             ColorTeamingAPI api = plugin.getAPI();
 
             HashMap<String, ArrayList<Player>> members = api.getAllTeamMembers();
             String group = args[1];
             if ( !members.containsKey(group) ) {
                 sender.sendMessage(PREERR + "グループ " + group + " は存在しません。");
                 return true;
             }
 
             for ( Player p : members.get(group) ) {
                 api.leavePlayerTeam(p, Reason.TEAM_REMOVED);
                 p.sendMessage(PREINFO + "グループ " + group + " が解散しました。");
             }
             api.removeTeam(group);
 
             // サイドバー再作成、タブキーリスト更新
             api.makeSidebarScore();
             api.refreshTabkeyListScore();
             api.refreshBelowNameScore();
 
             sender.sendMessage(PREINFO + "グループ " + group + " が解散しました。");
 
             return true;
 
         } else if ( args.length >= 2 && args[0].equalsIgnoreCase("trophy") ) {
 
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
 
         } else if ( args.length >= 2 && args[0].equalsIgnoreCase("reachTrophy") ) {
 
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
 
         } else if ( args.length >= 2 && args[0].equalsIgnoreCase("allowJoinAny") ) {
 
             if ( args[1].equalsIgnoreCase("on") ) {
                 ColorTeamingConfig config = plugin.getCTConfig();
                 config.setAllowPlayerJoinAny(true);
                 config.saveConfig();
                 sender.sendMessage(ChatColor.GRAY + "一般プレイヤーの /cjoin (group) の使用が可能になりました。");
                 return true;
             } else if ( args[1].equalsIgnoreCase("off") ) {
                 ColorTeamingConfig config = plugin.getCTConfig();
                 config.setAllowPlayerJoinAny(false);
                 config.saveConfig();
                 sender.sendMessage(ChatColor.GRAY + "一般プレイヤーの /cjoin (group) の使用が不可になりました。");
                 return true;
             }
 
             return false;
 
         } else if ( args.length >= 2 && args[0].equalsIgnoreCase("allowJoinRandom") ) {
 
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
 
         } else if ( args.length >= 3 && args[0].equalsIgnoreCase("add") ) {
 
             String group = args[1];
             if ( !Utility.isValidColor(group) ) {
                 sender.sendMessage(PREERR + "グループ " + group + " は設定できないグループ名です。");
                 return true;
             }
 
             Player player = Bukkit.getPlayerExact(args[2]);
             if ( player == null ) {
                 sender.sendMessage(PREERR + "プレイヤー " + args[2] + " は存在しません。");
                 return true;
             }
 
             ColorTeamingAPI api = plugin.getAPI();
             boolean isNewGroup = !api.getAllTeamMembers().containsKey(group);
             api.addPlayerTeam(player, group);
 
             // メンバー情報をlastdataに保存する
             api.getCTSaveDataHandler().save("lastdata");
 
             // サイドバーの更新 グループが増える場合は、再生成する
             if ( isNewGroup ) {
                 api.makeSidebarScore();
             }
             api.refreshSidebarScore();
             api.refreshTabkeyListScore();
             api.refreshBelowNameScore();
 
             sender.sendMessage(PREINFO + "プレイヤー " + player.getName() + " をグループ " +
                     group + " に追加しました。");
 
             return true;
 
         } else if ( args.length >= 2 && args[0].equalsIgnoreCase("side") ) {
 
             ColorTeamingConfig config = plugin.getCTConfig();
 
             // 前回がカスタムだった場合、表示を終了する必要があるので、
             // あらかじめカスタムを取得しておく
             CustomScoreInterface oldCustom = null;
             if ( config.getSideCriteria() == SidebarCriteria.CUSTOM ) {
                 String slot = config.getSideCustomSlot();
                 oldCustom = plugin.getAPI().getCustomScoreCriteria(slot);
             }
 
             // カスタム指定の場合
             if ( args[1].toLowerCase().startsWith("custom-") ) {
                 int index = 7; // args[1].indexOf("-") + 1;
                 String slot = args[1].toLowerCase().substring(index);
 
                 CustomScoreInterface custom =
                         plugin.getAPI().getCustomScoreCriteria(slot);
                 if ( custom == null ) {
                     sender.sendMessage(PREERR + "カスタムスコア" + slot + "が存在しません。");
                     return true;
                 }
 
                config.setSideCriteria(SidebarCriteria.CUSTOM);
                config.setSideCustomSlot(slot);
                 config.saveConfig();
 
                 // 開始処理と終了処理、スコアボードの更新
                 if ( oldCustom != null ) {
                     oldCustom.displayEnd();
                 }
                 plugin.getAPI().makeTabkeyListScore();
 
                 sender.sendMessage(PREINFO +
                         "リストの表示をカスタムスコア" + slot + "にしました。");
 
                 return true;
             }
 
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
             if ( oldCustom != null ) {
                 oldCustom.displayEnd();
             }
             plugin.getAPI().makeSidebarScore();
 
             String criteria = config.getSideCriteria().toString();
             sender.sendMessage(PREINFO + "サイドバーの表示を" + criteria + "にしました。");
 
             return true;
 
         } else if ( args.length >= 2 && args[0].equalsIgnoreCase("list") ) {
 
             ColorTeamingConfig config = plugin.getCTConfig();
 
             // 前回がカスタムだった場合、表示を終了する必要があるので、
             // あらかじめカスタムを取得しておく
             CustomScoreInterface oldCustom = null;
             if ( config.getListCriteria() == PlayerCriteria.CUSTOM ) {
                 String slot = config.getSideCustomSlot();
                 oldCustom = plugin.getAPI().getCustomScoreCriteria(slot);
             }
 
             // カスタム指定の場合
             if ( args[1].toLowerCase().startsWith("custom-") ) {
                 int index = 7; // args[1].indexOf("-") + 1;
                 String slot = args[1].toLowerCase().substring(index);
 
                 CustomScoreInterface custom =
                         plugin.getAPI().getCustomScoreCriteria(slot);
                 if ( custom == null ) {
                     sender.sendMessage(PREERR + "カスタムスコア" + slot + "が存在しません。");
                     return true;
                 }
 
                 config.setListCriteria(PlayerCriteria.CUSTOM);
                 config.setListCustomSlot(slot);
                 config.saveConfig();
 
                 // 開始処理と終了処理、スコアボードの更新
                 if ( oldCustom != null ) {
                     oldCustom.displayEnd();
                 }
                 plugin.getAPI().makeTabkeyListScore();
 
                 sender.sendMessage(PREINFO +
                         "リストの表示をカスタムスコア" + slot + "にしました。");
 
                 return true;
             }
 
             // その他の指定の場合
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
             if ( oldCustom != null ) {
                 oldCustom.displayEnd();
             }
             plugin.getAPI().makeTabkeyListScore();
 
             String criteria = config.getListCriteria().toString();
             sender.sendMessage(PREINFO + "リストの表示を" + criteria + "にしました。");
 
             return true;
 
         } else if ( args.length >= 2 && args[0].equalsIgnoreCase("below") ) {
 
             ColorTeamingConfig config = plugin.getCTConfig();
 
             // カスタム指定の場合
             if ( args[1].toLowerCase().startsWith("custom-") ) {
                 int index = 7; // args[1].indexOf("-") + 1;
                 String slot = args[1].toLowerCase().substring(index);
 
                 CustomScoreInterface custom =
                         plugin.getAPI().getCustomScoreCriteria(slot);
                 if ( custom == null ) {
                     sender.sendMessage(PREERR + "カスタムスコア" + slot + "が存在しません。");
                     return true;
                 }
 
                 config.setBelowCriteria(PlayerCriteria.CUSTOM);
                 config.setBelowCustomSlot(slot);
                 config.saveConfig();
 
                 // スコアボードの更新
                 plugin.getAPI().makeBelowNameScore();
 
                 sender.sendMessage(PREINFO +
                         "リストの表示をカスタムスコア" + slot + "にしました。");
 
                 return true;
             }
 
             // その他の指定の場合
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
 
         return false;
     }
 
 }
