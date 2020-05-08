 /**
  * SakuraCmd - Package: net.syamn.sakuracmd.commands.other
  * Created: 2013/01/18 15:22:18
  */
 package net.syamn.sakuracmd.commands.other;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.inventory.meta.SkullMeta;
 
 import net.syamn.sakuracmd.commands.BaseCommand;
 import net.syamn.sakuracmd.permission.Perms;
 import net.syamn.utils.StrUtil;
 import net.syamn.utils.Util;
 import net.syamn.utils.exception.CommandException;
 
 /**
  * AdminCommand (AdminCommand.java)
  * @author syam(syamn)
  */
 public class AdminCommand extends BaseCommand {
     public AdminCommand() {
         bePlayer = false;
         name = "admin";
         perm = Perms.ADMIN;
         argLength = 1;
         usage = "<- admin commands";
     }
     
     @Override
     public void execute() throws CommandException {
         final String sub = args.remove(0).trim().toLowerCase(Locale.ENGLISH);
         
         // bcast
         if (sub.equals("bcast") && args.size() > 0) {
             args.remove(0);
             Util.broadcastMessage(StrUtil.join(args, " "));
             return;
         }
         
         // item
         if (sub.equals("item") && isPlayer && args.size() > 0){
             final String action = args.remove(0).trim().toLowerCase(Locale.ENGLISH);
             final String str = (args.size() > 0) ? Util.coloring(StrUtil.join(args, " ")) : null;
             
             ItemStack is = player.getItemInHand();
            if (is == null){
                 throw new CommandException("&c手にアイテムを持っていません！");
             }
             ItemMeta meta = is.getItemMeta();
             
             if (action.equals("name")){
                 if (str == null){
                     throw new CommandException("&cメッセージが入力されていません！");
                 }
                 meta.setDisplayName(str);
             }else if (action.equals("add")){
                 if (str == null){
                     throw new CommandException("&cメッセージが入力されていません！");
                 }
                 List<String> lores = meta.getLore();
                 if (lores == null){
                     lores = new ArrayList<String>();
                 }
                 lores.add(str);
                 meta.setLore(lores);
             }else if (action.equals("clear")){
                 meta.setLore(null);
             }else{
                 throw new CommandException("&c指定したNBT編集アクションは存在しません！");
             }
             is.setItemMeta(meta);
             player.setItemInHand(is);
             Util.message(sender, "&aNBTを編集しました！");
             return;
         }
         
         // head
         if (sub.equals("head") && isPlayer && args.size() > 0){
             final String name = args.get(0).trim();
             if (!StrUtil.isValidName(name)){
                 throw new CommandException("&cプレイヤー名が不正です！");
             }
             
             ItemStack is = player.getItemInHand();
             if(is == null || is.getType() != Material.SKULL_ITEM){
                 throw new CommandException("&cプレイヤーヘッドを持っていません！");
             }
             
             is.setDurability((short) 3);
             SkullMeta meta = (SkullMeta) is.getItemMeta();
             meta.setOwner(name);
             is.setItemMeta(meta);
             player.setItemInHand(is);
             Util.message(sender, "&aプレイヤーヘッドを " + name + " に変更しました！");
             return;
         }
         
         
         Util.message(sender, "&cUnknown sub-command!");
     }
 }
