 package riking.horses;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Horse;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class PluginMain extends JavaPlugin implements Listener {
 
     @Override
     public void onEnable() {
         getCommand("horse").setExecutor(this);
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (command.getName().equalsIgnoreCase("horse")) {
             Player player;
             if (args.length != 0) {
                 player = Bukkit.getPlayer(args[0]);
                 if (player == null) {
                     sender.sendMessage(String.format("%sPlayer '%s' could not be found. Try tab-completing?", ChatColor.RED, args[0]));
                     return true;
                 }
             } else {
                 if (!(sender instanceof Player)) {
                     sender.sendMessage(ChatColor.RED + "You must specify a player to search near.");
                     return true;
                 }
                 player = (Player) sender;
             }
 
             Horse horse = getTargetHorse(player);
             if (horse == null) {
                 sender.sendMessage(ChatColor.RED + "No nearby horses!");
                 return true;
             }
 
             Horse.Variant var = horse.getVariant();
             Horse.Color col = horse.getColor();
             Horse.Style sty = horse.getStyle();
 
             if (var != Horse.Variant.HORSE) {
                 if (var == Horse.Variant.MULE || var == Horse.Variant.DONKEY) {
                     String chestStr = horse.isCarryingChest() ? (ChatColor.GREEN + "a") : (ChatColor.RED + "no");
                    sender.sendMessage(String.format("That is a %s with %s chest%s.", getVariantString(horse), ChatColor.RESET, chestStr, ChatColor.RESET));
                 } else {
                     sender.sendMessage(String.format("That is a %s.", getVariantString(horse)));
                 }
             } else {
                 sender.sendMessage(String.format("That is a %s %s %s.", getStyleString(sty), getColorString(col), getVariantString(horse)));
             }
 
             if (horse.getOwner() == null) {
                 sender.sendMessage(String.format("It is %suntamed%s (%d).", ChatColor.DARK_RED, ChatColor.RESET, horse.getDomestication()));
             } else {
                 sender.sendMessage(String.format("It is %stamed%s, originally by %s.", ChatColor.DARK_GREEN, ChatColor.RESET, ChatColor.YELLOW + horse.getOwner().getName() + ChatColor.RESET));
             }
 
             double jump = (horse.getJumpStrength() - 0.4D) * 10.0D + 0.24D;
             double health = horse.getMaxHealth();
             double speed = 0;
             try {
                 speed = Unsafe.getHorseSpeed(horse) * 30D;
             } catch (Throwable t) {
             }
             // TODO add another try block using Attributes api when released, remove it 2 weeks after RB
             if (speed != 0) {
                 sender.sendMessage(String.format("Health: %s%.1f%s Jump: %s%.3f%s Speed: %s%.3f%s", ChatColor.RED, health, ChatColor.RESET, ChatColor.YELLOW, jump, ChatColor.RESET, ChatColor.GREEN, speed, ChatColor.RESET));
             } else {
                 sender.sendMessage(String.format("Health: %s%.1f%s Jump: %s%.3f%s", ChatColor.RED, health, ChatColor.RESET, ChatColor.YELLOW, jump, ChatColor.RESET));
             }
 
             return true;
         }
         return false;
     }
 
     public String getStyleString(Horse.Style style) {
         String name = style.toString().toLowerCase();
         switch (style) {
         case NONE:
             name = "clean";
         case BLACK_DOTS:
             name = "sooty";
         case WHITE:
             name = "socked";
         case WHITE_DOTS:
             name = "spotted";
         case WHITEFIELD:
             name = "striped";
         }
         return ChatColor.GOLD + StringUtils.capitalize(name) + ChatColor.RESET;
     }
 
     public String getColorString(Horse.Color color) {
         String name = color.toString().toLowerCase();
         if (color == Horse.Color.DARK_BROWN) {
             name = "Dark Brown";
         }
         return ChatColor.BLUE + StringUtils.capitalize(name) + ChatColor.RESET;
     }
 
     public String getVariantString(Horse horse) {
         Horse.Variant variant = horse.getVariant();
         String baby = (horse.isAdult()) ? ("") : (ChatColor.LIGHT_PURPLE + "baby ");
         String name = variant.toString().toLowerCase();
         if (variant == Horse.Variant.SKELETON_HORSE) {
             name = "Skeleton horse";
         } else if (variant == Horse.Variant.UNDEAD_HORSE) {
             name = "Undead horse";
         }
         return baby + ChatColor.AQUA + StringUtils.capitalize(name) + ChatColor.RESET;
     }
 
     private Horse getTargetHorse(Player player) {
         List<Entity> entities = player.getNearbyEntities(10, 10, 10);
         List<Horse> horses = new ArrayList<Horse>();
         for (Entity ent : entities) {
             if (ent instanceof Horse) {
                 horses.add((Horse) ent);
             }
         }
         if (horses.isEmpty()) {
             return null;
         }
         Collections.sort(horses, new HorseComparator(player));
         return horses.get(0);
     }
 
     /**
      * Sort nearby horses. The 'best' horse goes to the "bottom", position 0.
      */
     class HorseComparator implements Comparator<Horse> {
         private Player player;
 
         public HorseComparator(Player player) {
             this.player = player;
         }
 
         @Override
         public int compare(Horse horse1, Horse horse2) {
             if (horse1 == horse2) {
                 return 0;
             }
             int offset = 0;
             if (riddenByPlayer(horse1)) {
                 offset += -20;
             } else if (riddenByPlayer(horse2)) {
                 offset += 20;
             }
             if (leashedByPlayer(horse1)) {
                 if (!leashedByPlayer(horse2)) {
                     offset += -10;
                 }
             } else if (leashedByPlayer(horse2)) {
                 offset += 10;
             }
             Location plLoc = player.getLocation();
             double dist1 = horse1.getLocation().distanceSquared(plLoc);
             double dist2 = horse2.getLocation().distanceSquared(plLoc);
             if (dist1 < dist2) {
                 return offset - 1; // negative if 1/A is 'better' -> be closer to pos 0
             } else {
                 return offset + 1;
             }
         }
 
         private boolean leashedByPlayer(Horse horse) {
             if (horse.isLeashed()) {
                 Entity leasher1 = horse.getLeashHolder();
                 if (leasher1.equals(player)) {
                     return true;
                 }
             }
             return false;
         }
 
         private boolean riddenByPlayer(Horse horse) {
             return player.equals(horse.getPassenger());
         }
     }
 }
