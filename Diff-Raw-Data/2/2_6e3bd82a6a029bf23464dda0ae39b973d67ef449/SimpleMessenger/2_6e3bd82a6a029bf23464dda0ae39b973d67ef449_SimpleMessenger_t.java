 package me.limebyte.battlenight.core.util.chat;
 
 import java.util.logging.Level;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.battle.Battle;
 import me.limebyte.battlenight.api.battle.Team;
 import me.limebyte.battlenight.api.util.Message;
 import me.limebyte.battlenight.api.util.Messenger;
 import me.limebyte.battlenight.api.util.Page;
 import me.limebyte.battlenight.api.util.Song;
 import me.limebyte.battlenight.core.BattleNight;
 import me.limebyte.battlenight.core.battle.SimpleArena;
 import me.limebyte.battlenight.core.battle.SimpleTeam;
 import me.limebyte.battlenight.core.battle.SimpleTeamedBattle;
 import me.limebyte.battlenight.core.tosort.ConfigManager;
 import me.limebyte.battlenight.core.tosort.ConfigManager.Config;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Sound;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.ComplexEntityPart;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class SimpleMessenger implements Messenger {
 
     public final String PREFIX = ChatColor.GRAY + "[BattleNight] " + ChatColor.WHITE;
 
     private BattleNightAPI api;
 
     public SimpleMessenger(BattleNightAPI api) {
         this.api = api;
     }
 
     @Override
     public void debug(Level level, String message) {
         if (ConfigManager.get(Config.MAIN).getBoolean("Debug", false)) {
             log(level, message);
         }
     }
 
     @Override
     public void debug(Level level, String message, Object... args) {
         debug(level, format(message, args));
     }
 
     @Override
     public String format(Message message, Object... args) {
         return format(message.getMessage(), args);
     }
 
     @Override
     public String format(String message, Object... args) {
         for (int i = 0; i < args.length; i++) {
             message = message.replace("$" + (i + 1), describeObject(args[i]));
         }
 
         return message;
     }
 
     @Override
     public String getColouredName(Player player) {
         String name = player.getName();
         Battle battle = api.getBattle();
         ChatColor colour = ChatColor.WHITE;
 
         if (battle != null && battle.containsPlayer(player)) {
             if (battle instanceof SimpleTeamedBattle) {
                 Team team = ((SimpleTeamedBattle) battle).getTeam(player);
                 if (team != null) {
                     colour = team.getColour();
                 }
             }
         } else if (!api.getLobby().getPlayers().contains(player.getName())) {
             colour = ChatColor.DARK_GRAY;
         }
 
         return colour + name + ChatColor.RESET;
     }
 
     @Override
     public void log(Level level, String message) {
         BattleNight.instance.getLogger().log(level, message);
     }
 
     @Override
     public void log(Level level, String message, Object... args) {
         log(level, format(message, args));
     }
 
     @Override
     public void playSong(Song battleEnd) {
         if (api.getBattle() == null) return;
         for (String name : api.getBattle().getPlayers()) {
             Player p = Bukkit.getPlayerExact(name);
             if (p != null) {
                 battleEnd.play(p);
             }
         }
     }
 
     @Override
     public void playSound(Sound sound, float pitch) {
         if (api.getBattle() == null) return;
         for (String name : api.getBattle().getPlayers()) {
             Player p = Bukkit.getPlayerExact(name);
             if (p != null) {
                 p.playSound(p.getLocation(), sound, 20f, pitch);
             }
         }
     }
 
     @Override
     public void tell(CommandSender sender, Message message) {
         tell(sender, message.getMessage());
     }
 
     @Override
     public void tell(CommandSender sender, Message message, Object... args) {
         tell(sender, message.getMessage(), args);
     }
 
     @Override
     public void tell(CommandSender sender, Page page) {
         sender.sendMessage(page.getPage());
     }
 
     @Override
     public void tell(CommandSender sender, String message) {
         if (message.isEmpty()) return;
        for (String line : message.split("\\n")) {
             sender.sendMessage(PREFIX + line);
         }
     }
 
     @Override
     public void tell(CommandSender sender, String message, Object... args) {
         tell(sender, format(message, args));
 
     }
 
     @Override
     public void tellBattle(Message message) {
         tellBattle(message.getMessage());
     }
 
     @Override
     public void tellBattle(Message message, Object... args) {
         tellBattle(format(message, args));
     }
 
     @Override
     public void tellBattle(Page page) {
         if (api.getBattle() == null) return;
         for (String name : api.getBattle().getPlayers()) {
             Player p = Bukkit.getPlayerExact(name);
             if (p != null) {
                 tell(p, page);
             }
         }
     }
 
     @Override
     public void tellBattle(String message) {
         if (api.getBattle() == null) return;
         for (String name : api.getBattle().getPlayers()) {
             Player p = Bukkit.getPlayerExact(name);
             if (p != null) {
                 tell(p, message);
             }
         }
     }
 
     @Override
     public void tellBattle(String message, Object... args) {
         tellBattle(format(message, args));
     }
 
     @Override
     public void tellBattleExcept(Player player, Message message) {
         tellBattleExcept(player, message.getMessage());
     }
 
     @Override
     public void tellBattleExcept(Player player, Message message, Object... args) {
         tellBattleExcept(player, message.getMessage(), args);
     }
 
     @Override
     public void tellBattleExcept(Player player, Page page) {
         if (api.getBattle() == null) return;
         for (String name : api.getBattle().getPlayers()) {
             Player p = Bukkit.getPlayerExact(name);
             if (p != null && player != p) {
                 tell(p, page);
             }
         }
     }
 
     @Override
     public void tellBattleExcept(Player player, String message) {
         if (api.getBattle() == null) return;
         for (String name : api.getBattle().getPlayers()) {
             Player p = Bukkit.getPlayerExact(name);
             if (p != null && player != p) {
                 tell(p, message);
             }
         }
     }
 
     @Override
     public void tellBattleExcept(Player player, String message, Object... args) {
         tellBattleExcept(player, format(message, args));
 
     }
 
     @Override
     public void tellLobby(Message message) {
         tellLobby(message.getMessage());
     }
 
     @Override
     public void tellLobby(Message message, Object... args) {
         tellLobby(format(message, args));
     }
 
     @Override
     public void tellLobby(Page page) {
         for (String name : api.getLobby().getPlayers()) {
             Player p = Bukkit.getPlayerExact(name);
             if (p != null) {
                 tell(p, page);
             }
         }
     }
 
     @Override
     public void tellLobby(String message) {
         for (String name : api.getLobby().getPlayers()) {
             Player p = Bukkit.getPlayerExact(name);
             if (p != null) {
                 tell(p, message);
             }
         }
     }
 
     @Override
     public void tellLobby(String message, Object... args) {
         tellLobby(format(message, args));
     }
 
     private String describeEntity(Entity entity) {
         if (entity instanceof Player) return ((Player) entity).getName();
 
         return entity.getType().toString().toLowerCase().replace("_", " ");
     }
 
     private String describeLocation(Location loc) {
         return loc.getX() + ", " + loc.getY() + ", " + loc.getZ();
     }
 
     private String describeMaterial(Material material) {
         if (material == Material.INK_SACK) return "dye";
 
         return material.toString().toLowerCase().replace("_", " ");
     }
 
     private String describeObject(Object obj) {
         if (obj instanceof ComplexEntityPart) return describeObject(((ComplexEntityPart) obj).getParent());
         else if (obj instanceof Item) return describeMaterial(((Item) obj).getItemStack().getType());
         else if (obj instanceof ItemStack) return describeMaterial(((ItemStack) obj).getType());
         else if (obj instanceof Player) return getColouredName((Player) obj);
         else if (obj instanceof Entity) return describeEntity((Entity) obj);
         else if (obj instanceof Block) return describeMaterial(((Block) obj).getType());
         else if (obj instanceof Material) return describeMaterial((Material) obj);
         else if (obj instanceof Location) return describeLocation((Location) obj);
         else if (obj instanceof World) return ((World) obj).getName();
         else if (obj instanceof SimpleTeam) return ((SimpleTeam) obj).getColour() + ((SimpleTeam) obj).getDisplayName();
         else if (obj instanceof SimpleArena) return ((SimpleArena) obj).getDisplayName();
         return obj.toString();
     }
 
     @Override
     public String get(String name) {
         return ChatColor.translateAlternateColorCodes('&', ConfigManager.get(Config.MESSAGES).getString(name, name));
     }
 
 }
