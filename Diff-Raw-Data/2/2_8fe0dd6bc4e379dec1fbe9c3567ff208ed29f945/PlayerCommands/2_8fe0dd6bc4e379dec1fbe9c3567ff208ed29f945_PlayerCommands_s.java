 package fr.noogotte.useful_commands.command;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import fr.aumgn.bukkitutils.command.Command;
 import fr.aumgn.bukkitutils.command.CommandArgs;
 import fr.aumgn.bukkitutils.command.NestedCommands;
 import fr.aumgn.bukkitutils.itemtype.ItemType;
 
 @NestedCommands(name = "useful")
 public class PlayerCommands extends UsefulCommands {
 
     @Command(name = "gamemode", min = 0, max = 1)
     public void gamemode(Player player, CommandArgs args) {
         List<Player> targets = args.getPlayers(0, player);
 
         for (Player target : targets) {
             if (target.getGameMode() == GameMode.CREATIVE) {
                 target.setGameMode(GameMode.SURVIVAL);
             } else {
                 target.setGameMode(GameMode.CREATIVE);
             }
 
             target.sendMessage(ChatColor.GREEN
                     + "Vous êtes maintenant en "
                     + ChatColor.AQUA + target.getGameMode());
             if (!player.equals(target)) {
                 player.sendMessage(ChatColor.GREEN
                     + "Vous avez mis "
                     + ChatColor.AQUA + target.getName()
                     + ChatColor.GREEN + " en "
                     + ChatColor.AQUA +  target.getGameMode());
             }
         }
     }
 
     @Command(name = "heal", flags = "hf", min = 0, max = 1)
     public void heal(Player player, CommandArgs args) {
         List<Player> targets = args.getPlayers(0, player);
         boolean health = !args.hasFlags() || args.hasFlag('h');
         boolean food = !args.hasFlags() || args.hasFlag('f');
 
         for (Player target : targets) {
             if (health) {
                 target.setHealth(20);
             }
             if (food) {
                 target.setFoodLevel(20);
             }
 
             target.sendMessage(ChatColor.YELLOW
                     + "Vous voila soignés et nourris");
             if (!player.equals(target)) {
                 player.sendMessage(ChatColor.GREEN
                         + "Vous vous avez soignés et nourris "
                         + ChatColor.BLUE + target.getName());
             }
         }
     }
 
     @Command(name = "clear", flags = "qra", min = 0, max = 1)
     public void clear(Player player, CommandArgs args) {
         List<Player> targets = args.getPlayers(0, player);
         int from = args.hasFlag('q') ? 10 : 0;
         int to = args.hasFlag('r') ? 9 : 35;
         boolean armor = !args.hasFlag('a');
 
         for (Player target : targets) {
             for (int j = from; j <= to; j++) {
                 target.getInventory().setItem(j, null);
             }
             if (armor) {
                 for (int j = 36; j <= 39; j++) {
                     target.getInventory().setItem(j, null);
                 }
             }
             target.sendMessage(ChatColor.YELLOW
                     + "Inventaire vidé !");
 
             if (!player.equals(target)) {
                 player.sendMessage(ChatColor.GREEN
                         + "Vous avez vidés l'inventaire de "
                         + ChatColor.BLUE + target.getName());
             }
         }
     }
 
     @Command(name = "give", min = 1, max = 3)
     public void give(Player player, CommandArgs args) {
         ItemType itemType = args.getItemType(0);
         List<Player> targets = args.getPlayers(2, player);
 
         ItemStack item;
         if(args.length() == 1) {
             item = itemType.toMaxItemStack();
         } else {
             item = itemType.toItemStack(args.getInteger(1));
         }
 
         for (Player target : targets) {
             target.getInventory().addItem(item);
             target.sendMessage(ChatColor.GREEN
                     + "Vous avez reçu "
                     + ChatColor.AQUA + item.getAmount()
                     + ChatColor.GREEN + " de "
                     + ChatColor.AQUA + itemType.getMaterial());
             if (!player.equals(target)) {
                 player.sendMessage(ChatColor.GREEN
                     + "Vous avez donnez à " + target.getName()
                     + ChatColor.GREEN + ": "
                     + ChatColor.AQUA + item.getAmount()
                     + ChatColor.GREEN + " de "
                     + ChatColor.AQUA + itemType.getMaterial());
             }
         }
     }
 
     @Command(name = "open", min = 0, max = 1)
     public void openInv(Player player, CommandArgs args) {
         Player target = args.getPlayer(0, player);
         if(target.isOp() == true) {
             target.sendMessage(ChatColor.RED
                     + "Votre inventaire a été ouvert par "
                     + ChatColor.GRAY + player.getName());
         }
         Inventory inventory = target.getInventory();
         player.openInventory(inventory);
     }
     
     @Command(name = "playerinfo", min = 1, max = 1)
     public void playerInfo(Player player, CommandArgs args) {
    	Player target = args.getPlayer(0, player);
     	
     	player.sendMessage(ChatColor.UNDERLINE + "Info de " + target.getName());
     	player.sendMessage("Vie: " + target.getHealth());
     	player.sendMessage("Faim: " + target.getFoodLevel());
     	player.sendMessage("IP: " + target.getAddress());
     	Location loc = target.getLocation();
     	player.sendMessage("Cordonnées: " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + " Monde: " + target.getWorld().getName());
     	player.sendMessage("Gamemode: " + target.getGameMode());
     	player.sendMessage("Experience: " + target.getLevel());
     }
 }
