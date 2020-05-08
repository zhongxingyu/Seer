 package me.DDoS.Quarantine.util;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Blaze;
 import org.bukkit.entity.CaveSpider;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.Enderman;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.MagmaCube;
 import org.bukkit.entity.PigZombie;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Silverfish;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Slime;
 import org.bukkit.entity.Spider;
 import org.bukkit.entity.Wolf;
 import org.bukkit.entity.Zombie;
 
 /**
  *
  * @author DDoS
  */
 public class QUtil {
 
     public static void tell(Player player, String msg) {
 
         player.sendMessage(ChatColor.DARK_RED + "[Quarantine] " + ChatColor.GRAY + msg);
 
     }
 
     public static CreatureType getCreatureType(LivingEntity ent) {
 
         if (ent instanceof CaveSpider) {
 
             return CreatureType.CAVE_SPIDER;
 
         }
 
         if (ent instanceof Creeper) {
 
             return CreatureType.CREEPER;
 
         }
 
         if (ent instanceof Enderman) {
 
            return CreatureType.ENDERMAN;
 
         }
 
         if (ent instanceof PigZombie) {
 
             return CreatureType.PIG_ZOMBIE;
 
         }
 
         if (ent instanceof Silverfish) {
 
             return CreatureType.SILVERFISH;
 
         }
 
         if (ent instanceof Skeleton) {
 
             return CreatureType.SKELETON;
 
         }
 
         if (ent instanceof Slime) {
 
             return CreatureType.SLIME;
 
         }
 
         if (ent instanceof Spider) {
 
             return CreatureType.SPIDER;
 
         }
 
         if (ent instanceof Wolf) {
 
             return CreatureType.WOLF;
 
         }
 
         if (ent instanceof Zombie) {
 
             return CreatureType.ZOMBIE;
 
         }
 
         if (ent instanceof Blaze) {
 
             return CreatureType.BLAZE;
 
         }
 
         if (ent instanceof MagmaCube) {
 
             return CreatureType.MAGMA_CUBE;
 
         }
 
         return null;
 
     }
 
     public static boolean acceptsMobs(Block block) {
 
         Material mat = block.getType();
 
         switch (mat) {
             case HUGE_MUSHROOM_1:
                 return true;
 
             case HUGE_MUSHROOM_2:
                 return true;
 
             case ENDER_PORTAL_FRAME:
                 return true;
 
             case ENDER_STONE:
                 return true;
 
             case PUMPKIN:
                 return true;
 
             case MYCEL:
                 return true;
 
             case NETHER_BRICK:
                 return true;
 
             case NETHER_BRICK_STAIRS:
                 return true;
 
             case BEDROCK:
                 return true;
 
             case BOOKSHELF:
                 return true;
 
             case BRICK:
                 return true;
 
             case BRICK_STAIRS:
                 return true;
 
             case BURNING_FURNACE:
                 return true;
 
             case CHEST:
                 return true;
 
             case CLAY:
                 return true;
 
             case COAL_ORE:
                 return true;
 
             case COBBLESTONE:
                 return true;
 
             case COBBLESTONE_STAIRS:
                 return true;
 
             case DIAMOND_BLOCK:
                 return true;
 
             case DIAMOND_ORE:
                 return true;
 
             case DIRT:
                 return true;
 
             case DISPENSER:
                 return true;
 
             case DOUBLE_STEP:
                 return true;
 
             case FURNACE:
                 return true;
 
             case GLASS:
                 return true;
 
             case GLOWSTONE:
                 return true;
 
             case GOLD_BLOCK:
                 return true;
 
             case GOLD_ORE:
                 return true;
 
             case GRASS:
                 return true;
 
             case GRAVEL:
                 return true;
 
             case ICE:
                 return true;
 
             case IRON_BLOCK:
                 return true;
 
             case IRON_ORE:
                 return true;
 
             case JACK_O_LANTERN:
                 return true;
 
             case JUKEBOX:
                 return true;
 
             case LAPIS_BLOCK:
                 return true;
 
             case LAPIS_ORE:
                 return true;
 
             case LEAVES:
                 return true;
 
             case LOG:
                 return true;
 
             case MELON_BLOCK:
                 return true;
 
             case MOB_SPAWNER:
                 return true;
 
             case MONSTER_EGGS:
                 return true;
 
             case MOSSY_COBBLESTONE:
                 return true;
 
             case NETHERRACK:
                 return true;
 
             case NOTE_BLOCK:
                 return true;
 
             case OBSIDIAN:
                 return true;
 
             case REDSTONE_ORE:
                 return true;
 
             case SAND:
                 return true;
 
             case SANDSTONE:
                 return true;
 
             case SMOOTH_BRICK:
                 return true;
 
             case SMOOTH_STAIRS:
                 return true;
 
             case SNOW_BLOCK:
                 return true;
 
             case SOIL:
                 return true;
 
             case SOUL_SAND:
                 return true;
 
             case SPONGE:
                 return true;
 
             case STEP:
                 return true;
 
             case STONE:
                 return true;
 
             case TNT:
                 return true;
 
             case TRAP_DOOR:
                 return true;
 
             case WOOD:
                 return true;
 
             case WOOD_STAIRS:
                 return true;
 
             case WOOL:
                 return true;
 
             case WORKBENCH:
                 return true;
 
             default:
                 return false;
 
         }
     }
 }
