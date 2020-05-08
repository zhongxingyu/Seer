 package fr.aumgn.bukkitutils.command.args;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.World;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.potion.PotionEffectType;
 
 import fr.aumgn.bukkitutils.command.exception.CommandUsageError;
 import fr.aumgn.bukkitutils.command.exception.InvalidMaterialAndDataFormat;
 import fr.aumgn.bukkitutils.command.exception.NoSuchColor;
 import fr.aumgn.bukkitutils.command.exception.NoSuchEnchantment;
 import fr.aumgn.bukkitutils.command.exception.NoSuchEntityType;
 import fr.aumgn.bukkitutils.command.exception.NoSuchMaterial;
 import fr.aumgn.bukkitutils.command.exception.NoSuchPlayer;
 import fr.aumgn.bukkitutils.command.exception.NoSuchPotionEffect;
 import fr.aumgn.bukkitutils.command.exception.NoSuchWorld;
 import fr.aumgn.bukkitutils.command.messages.Messages;
 import fr.aumgn.bukkitutils.geom.Vector;
 import fr.aumgn.bukkitutils.geom.Vector2D;
 import fr.aumgn.bukkitutils.itemtype.ItemType;
 import fr.aumgn.bukkitutils.util.Util;
 
 public class CommandArgs extends CommandArgsBase {
 
     public CommandArgs(Messages messages, CommandArgsParser parser) {
         this.messages = messages;
         this.flags = parser.getFlags();
         this.args = parser.getArgs();
     }
 
     public int getInteger(int index) {
         try {
             return Integer.parseInt(get(index));
         } catch (NumberFormatException exc) {
             throw new CommandUsageError(
                     String.format(messages.notAValidNumber(), index + 1));
         }
     }
 
     public int getInteger(int index, int def) {
         if (hasIndex(index)) {
             return getInteger(index);
         }
 
         return def;
     }
 
     public double getDouble(int index) {
         try {
             return Double.parseDouble(get(index));
         } catch (NumberFormatException exc) {
             throw new CommandUsageError(
                     String.format(messages.notAValidNumber(), index + 1));
         }
     }
 
     public double getDouble(int index, double def) {
         if (hasIndex(index)) {
             return getDouble(index);
         }
 
         return def;
     }
 
     public ChatColor getChatColor(int index) {
         try {
             return ChatColor.valueOf(get(index).toUpperCase());
         } catch (IllegalArgumentException exc) {
             throw new NoSuchColor(messages, get(index));
         }
     }
 
     public ChatColor getChatColor(int index, ChatColor def) {
         if (hasIndex(index)) {
             return getChatColor(index);
         }
 
         return def;
     }
 
     public Vector getVector(int i) {
         String arg = get(i);
         String[] splitted = arg.split(",");
 
         if (splitted.length > 3) {
             throw new CommandUsageError();
         }
 
         double x = parseVectorComponent(splitted[0]);
         double y = 0.0;
         double z = 0.0;
         if (splitted.length > 1) {
             y = parseVectorComponent(splitted[1]);
             if (splitted.length > 2) {
                 z = parseVectorComponent(splitted[2]);
             }
         }
 
         return new Vector(x, y, z);
     }
 
     public Vector getVector(int index, Vector def) {
         if (hasIndex(index)) {
             return getVector(index);
         }
 
         return def;
     }
 
     public Vector2D getVector2D(int i) {
         String arg = get(i);
         String[] splitted = arg.split(",");
 
         if (splitted.length > 2) {
             throw new CommandUsageError();
         }
 
         double x = parseVectorComponent(splitted[0]);
         double z = 0.0;
         if (splitted.length > 1) {
             z = parseVectorComponent(splitted[1]);
         }
 
         return new Vector2D(x,z);
     }
 
     public Vector2D getVector2D(int index, Vector2D def) {
         if (hasIndex(index)) {
             return getVector2D(index);
         }
 
         return def;
     }
 
     private double parseVectorComponent(String component) {
         try {
             return Double.parseDouble(component);
         } catch (NumberFormatException exc) {
             throw new CommandUsageError(
                     String.format(messages.notAValidVectorComponent(), component));
         }
     }
 
     public World getWorld(int index) {
         World world = Bukkit.getWorld(get(index));
         if (world == null) {
             throw new NoSuchWorld(messages, get(index));
         }
 
         return world;
     }
 
     public World getWorld(int index, World def) {
         if (hasIndex(index)) {
             return getWorld(index);
         }
 
         return def;
     }
 
     public Player getPlayer(int index) {
         Player player = Bukkit.getPlayer(get(index));
         if (player == null) {
             throw new NoSuchPlayer(messages, get(index));
         }
 
         return player;
     }
 
     public Player getPlayer(int index, Player def) {
         if (hasIndex(index)) {
             return getPlayer(index);
         }
 
         return def;
     }
 
     public List<Player> getPlayers(int index) {
         return getPlayers(index, true);
     }
 
     public List<Player> getPlayers(int index, Player def) {
         return getPlayers(index, def, true);
     }
 
     public List<Player> getPlayers(int index, List<Player> def) {
         return getPlayers(index, def, true);
     }
 
     public List<Player> getPlayers(int index, boolean throwWhenEmpty) {
         String arg = get(index);
 
         if (arg.equals("*")) {
             return Arrays.asList(Bukkit.getOnlinePlayers());
         }
 
         List<Player> players = Util.matchPlayer(arg);
         if (throwWhenEmpty && players.isEmpty()) {
             throw new NoSuchPlayer(messages, get(index));
         }
 
         return players;
     }
 
     public List<Player> getPlayers(int index, Player def, boolean throwWhenEmpty) {
         return getPlayers(index, Collections.<Player>singletonList(def));
     }
 
     public List<Player> getPlayers(int index, List<Player> def, boolean throwWhenEmpty) {
         if (hasIndex(index)) {
             return getPlayers(index, throwWhenEmpty);
         }
 
         return def;
     }
 
     public OfflinePlayer getOfflinePlayer(int index) {
         return Bukkit.getOfflinePlayer(get(index));
     }
 
     public OfflinePlayer getOfflinePlayer(int index, OfflinePlayer def) {
         if (hasIndex(index)) {
             return getOfflinePlayer(index);
         }
 
         return def;
     }
 
     public List<OfflinePlayer> getOfflinePlayers(int index) {
         return getOfflinePlayers(index, true);
     }
 
     public List<OfflinePlayer> getOfflinePlayers(int index, boolean throwWhenEmpty) {
         List<OfflinePlayer> players = Util.matchOfflinePlayer(get(index));
         if (throwWhenEmpty && players.isEmpty()) {
             throw new NoSuchPlayer(messages, get(index));
         }
 
         return players;
     }
 
     private Material getMaterial(String pattern) {
         Material material = Util.matchMaterial(pattern);
         if (material == null) {
             throw new NoSuchMaterial(messages, pattern);
         }
 
         return material;
     }
 
     public Material getMaterial(int index) {
         return getMaterial(get(index));
     }
 
     public Material getMaterial(int index, Material def) {
         if (hasIndex(index)) {
             return getMaterial(index);
         }
 
         return def;
     }
 
     public ItemType getItemType(int index) {
         String[] splitted = get(index).split(":");
         if (splitted.length > 2) {
             throw new InvalidMaterialAndDataFormat(messages, get(index));
         }
 
         Material material = getMaterial(splitted[0]);
         Short data = 0;
         if (splitted.length == 2) {
             data = Util.parseDataFor(material, splitted[1]);
             if (data == null) {
                 throw new InvalidMaterialAndDataFormat(messages, get(index));
             }
         }
 
         return new ItemType(material, data);
     }
 
     public ItemType getItemType(int index, ItemType def) {
         if (hasIndex(index)) {
             return getItemType(index);
         }
 
         return def;
     }
 
     public ItemType getItemType(int index, Material def) {
         if (hasIndex(index)) {
             return getItemType(index);
         }
 
         return new ItemType(def);
     }
 
     public PotionEffectType getPotionEffectType(int index) {
         String name = get(index);
         PotionEffectType effect = PotionEffectType.getByName(name);
         if(effect == null) {
             throw new NoSuchPotionEffect(messages, name);
         }
 
         return effect;
     }
 
     public Enchantment getEnchantment(int index) {
         String name = get(index);
        Enchantment enchant = Enchantment.getByName(name);
         if(enchant == null) {
             throw new NoSuchEnchantment(messages, name);
         }
 
         return enchant;
     }
 
     public EntityType getEntityType(int index) {
         String name = get(index);
         EntityType entityType = EntityType.fromName(name);
         if(entityType == null) {
             throw new NoSuchEntityType(messages, name);
         }
 
         return entityType;
     }
 }
