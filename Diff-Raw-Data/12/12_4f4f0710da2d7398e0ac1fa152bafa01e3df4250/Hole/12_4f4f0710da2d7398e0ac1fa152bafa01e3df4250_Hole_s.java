 package com.github.idragonfire.dragonskills.skills;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.entity.Player;
 
 import com.github.idragonfire.dragonskills.DragonSkillsPlugin;
 import com.github.idragonfire.dragonskills.api.DSystem;
 import com.github.idragonfire.dragonskills.api.SkillResult;
 import com.github.idragonfire.dragonskills.api.TargetBlockSkill;
 import com.github.idragonfire.dragonskills.api.TimeEffect;
 import com.github.idragonfire.dragonskills.utils.DUtils;
 import com.github.idragonfire.dragonskills.utils.SkillConfig;
 
 public class Hole extends TargetBlockSkill {
 
     private final int[] LEFT = new int[] { -1, 0, 0 };
     private final int[] RIGHT = new int[] { 1, 0, 0 };
     private final int[] BACK = new int[] { 0, 0, 1 };
     private final int[] FRONT = new int[] { 0, 0, -1 };
     private final int[][] MOVMENT = new int[][] { { 0, 0, 0 }, RIGHT, BACK,
             LEFT, LEFT, LEFT, FRONT, RIGHT, FRONT, RIGHT, FRONT, RIGHT, BACK,
             RIGHT, BACK, RIGHT, BACK, LEFT, BACK, LEFT, BACK, LEFT, FRONT, LEFT };
     private final int[][] STAIRS = new int[][] { { 3, -1, 2 }, { 0, 1, 1 },
             { -1, 1, 0 }, { 0, 1, 1 }, { -1, 1, 0 }, { -1, 1, 0 }, { -1, 1, 0 } };
     private final int[][] RING = new int[][] { FRONT, { -1, 0, -1 },
             { -1, 0, -1 }, FRONT, { 1, 0, -1 }, { 1, 0, -1 }, { 1, 0, -1 },
             RIGHT, { 1, 0, 1 }, { 1, 0, 1 }, { 1, 0, 1 }, BACK, BACK, BACK,
             { -1, 0, 1 }, { -1, 0, 1 }, LEFT, LEFT };
     @SkillConfig
     private final HashSet<Material> allowedMaterial = new HashSet<Material>(
             Arrays.asList(new Material[] { Material.AIR, Material.WOOD, /* Material.BEDROCK, */
             Material.LEAVES, Material.BROWN_MUSHROOM, Material.CLAY,
                     Material.COAL_ORE, Material.COBBLESTONE,
                     Material.COBBLESTONE_STAIRS, Material.ICE,
                     Material.IRON_ORE, Material.DIRT, Material.FENCE,
                     Material.RED_MUSHROOM, Material.GLOWSTONE, Material.GRASS,
                     Material.GRAVEL, Material.LAPIS_BLOCK, Material.LAPIS_ORE,
                     Material.LOG, Material.MOSSY_COBBLESTONE,
                     Material.NETHERRACK, Material.OBSIDIAN,
                     Material.REDSTONE_ORE, Material.RED_ROSE,
                     Material.YELLOW_FLOWER, Material.SAND, Material.SNOW,
                     Material.SOUL_SAND, Material.STONE, Material.WATER,
                     Material.SUGAR_CANE, Material.STATIONARY_WATER,
                     Material.STATIONARY_LAVA, Material.LONG_GRASS,
                     Material.VINE, Material.SANDSTONE, Material.PUMPKIN,
                     Material.LAVA, Material.DEAD_BUSH }));
     @SkillConfig
     private int holeTime = 10;
     @SkillConfig
     private int holeDelay = 3;
 
     public Hole(DragonSkillsPlugin plugin) {
         super(plugin);
     }
 
     @Override
     public SkillResult use(Player player, Block startBlock) {
         if (!canPlaceHole(player, startBlock)) {
             DSystem.log("Cannot place mine here");
             return SkillResult.FAIL;
         }
         new HoleEffect(getPlugin(), holeTime * DUtils.TICKS, player, startBlock)
                 .startEffect(holeDelay * DUtils.TICKS);
         DSystem.log("The Mine call to you in $1 seconds", holeDelay);
         return SkillResult.SUCESSFULL;
     }
 
     public boolean canPlaceHole(Player player, Block start) {
         Block layer = start;
         Block tmp;
         for (int j = 0; j < 6; j++) {
             tmp = layer;
             for (int i = 0; i < MOVMENT.length; i++) {
                 tmp = tmp.getRelative(MOVMENT[i][0], MOVMENT[i][1],
                         MOVMENT[i][2]);
                 if (invalidBlock(player, tmp)) {
                     return false;
                 }
             }
             layer = layer.getRelative(0, -1, 0);
         }
         tmp = layer.getRelative(0, 1, 0);
         for (int i = 0; i < STAIRS.length; i++) {
             tmp = tmp.getRelative(STAIRS[i][0], STAIRS[i][1], STAIRS[i][2]);
 
             Block tmp2 = tmp;
             for (int k = 0; k < 7 - i; k++) {
                 tmp2 = tmp2.getRelative(0, 1, 0);
                 if (invalidBlock(player, tmp2)) {
                     return false;
                 }
             }
             if (invalidBlock(player, tmp)) {
                 return false;
             }
         }
         for (int i = 0; i < RING.length; i++) {
             tmp = tmp.getRelative(RING[i][0], RING[i][1], RING[i][2]);
             if (invalidBlock(player, tmp)) {
                 return false;
             }
             if (invalidBlock(player, tmp.getRelative(0, 1, 0))) {
                 return false;
             }
 
         }
         tmp = start.getRelative(0, 1, 0);
         for (int i = 0; i < MOVMENT.length; i++) {
             tmp = tmp.getRelative(MOVMENT[i][0], MOVMENT[i][1], MOVMENT[i][2]);
             if (invalidBlock(player, tmp)) {
                 return false;
             }
         }
         return true;
     }
 
     public boolean invalidBlock(Player player, Block block) {
         if (!allowedMaterial.contains(block.getType())) {
            return false;
         }
        return DUtils.canBreak(player, block);
     }
 
     public class HoleEffect extends TimeEffect {
         private Block block = null;
         private ArrayList<BlockState> store;
         private Player player;
 
         public HoleEffect(DragonSkillsPlugin plugin, long duration,
                 Player player, Block startBlock) {
             super(plugin, duration);
             block = startBlock;
             store = new ArrayList<BlockState>();
             this.player = player;
         }
 
         @Override
         public void initTimeEffect() {
             DSystem.log("The Mine disapear in $1 seconds",
                     getDurationInSeconds());
             Block tmp;
             for (int j = 0; j < 6; j++) {
                 tmp = block;
                 for (int i = 0; i < MOVMENT.length; i++) {
                     tmp = tmp.getRelative(MOVMENT[i][0], MOVMENT[i][1],
                             MOVMENT[i][2]);
                     store.add(tmp.getState());
                     if (tmp.getType() != Material.DIAMOND_ORE) {
                         DUtils.transformBlock(player, tmp, Material.AIR);
                     }
                 }
                 block = block.getRelative(0, -1, 0);
             }
             tmp = block.getRelative(0, 1, 0);
             for (int i = 0; i < STAIRS.length; i++) {
                 tmp = tmp.getRelative(STAIRS[i][0], STAIRS[i][1], STAIRS[i][2]);
                 Block tmp2 = tmp;
                 for (int k = 0; k < 6 - i; k++) {
                     tmp2 = tmp2.getRelative(0, 1, 0);
                     store.add(tmp2.getState());
                     DUtils.transformBlock(player, tmp2, Material.AIR);
 
                 }
                 store.add(tmp.getState());
                 DUtils.transformBlock(player, tmp, Material.BEDROCK);
             }
             for (int i = 0; i < RING.length; i++) {
                 tmp = tmp.getRelative(RING[i][0], RING[i][1], RING[i][2]);
                 store.add(tmp.getState());
                 DUtils.transformBlock(player, tmp, Material.BEDROCK);
             }
         }
 
         @Override
         public void endTimeEffect() {
             DSystem.log("Mine has gone");
             restore();
         }
 
         public void restore() {
             for (int j = store.size() - 1; j >= 0; j--) {
                 store.get(j).update(true);
             }
             store.clear();
         }
     }
 
     @Override
     public String getDescription() {
         return DSystem.paramString(
                 "spawn a hole after $1 seconds, that is only $2 seconds open",
                 holeDelay, holeTime);
     }
 }
