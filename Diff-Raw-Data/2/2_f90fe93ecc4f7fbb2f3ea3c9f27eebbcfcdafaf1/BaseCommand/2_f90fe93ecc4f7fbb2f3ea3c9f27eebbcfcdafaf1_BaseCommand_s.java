 /**
  * SakuraCmd - Package: net.syamn.sakuracmd.commands
  * Created: 2012/12/28 13:59:11
  */
 package net.syamn.sakuracmd.commands;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.syamn.sakuracmd.SakuraCmd;
 import net.syamn.sakuracmd.permission.Perms;
 import net.syamn.utils.Util;
 import net.syamn.utils.exception.CommandException;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 /**
  * BaseCommand (BaseCommand.java)
  * @author syam(syamn)
  */
 public abstract class BaseCommand {
     public SakuraCmd plugin;
     
     /* コマンド関係 */
     public CommandSender sender;
     public List<String> args = new ArrayList<String>();
     public Player player;
     public boolean isPlayer = false;
     public String command;
     
     public String name;
     public boolean bePlayer = true;
     public int argLength = 0;
     public String usage;
     public Perms perm = null;
     
     public boolean run(SakuraCmd plugin, CommandSender sender, String cmd, String[] preArgs) {
         if (name == null) {
             Util.message(sender, "&cThis command not loaded properly!");
             return true;
         }
         
         this.plugin = plugin;
         this.sender = sender;
         this.command = cmd;
         
         // 引数をソート
         args.clear();
         for (String arg : preArgs)
             args.add(arg);
         
         // 引数からコマンドの部分を取り除く
         // (コマンド名に含まれる半角スペースをカウント、リストの先頭から順にループで取り除く)
         // for (int i = 0; i < name.split(" ").length && i < args.size(); i++)
         // args.remove(0);
         
         // 引数の長さチェック
         if (argLength > args.size()) {
             sendUsage();
             return true;
         }
         
         // 実行にプレイヤーであることが必要かチェックする
         if (bePlayer && !(sender instanceof Player)) {
             Util.message(sender, "&cThis command cannot run from Console!");
             return true;
         }
         if (sender instanceof Player) {
             player = (Player) sender;
             isPlayer = true;
         }else{
             player = null;
             isPlayer = false;
         }
         
         // 権限チェック
         if ((perm != null && !perm.has(sender)) || !permission(sender)) {
             Util.message(sender, "&cYou don't have permission to use this!");
             return true;
         }
         
         // 実行
         try {
             execute();
         } catch (CommandException ex) {
             Throwable error = ex;
              while (error instanceof Exception){
                  Util.message(sender, error.getMessage());
                  error = error.getCause();
              }
         }
         
         return true;
     }
     
     /**
      * コマンドを実際に実行する
      */
     public abstract void execute() throws CommandException;
     
     protected List<String> tabComplete(SakuraCmd plugin, final CommandSender sender, String cmd, String[] preArgs) {
         return null;
     }
     
     /**
      * コマンド実行に必要な権限を持っているか検証する
      * 
      * @return trueなら権限あり、falseなら権限なし
      */
     public boolean permission(CommandSender sender){
         return true;
     }
     
     /**
      * コマンドの使い方を送信する
      */
     public void sendUsage() {
        Util.message(sender, "&c/" + this.command + " " + name + " " + usage);
     }
 }
