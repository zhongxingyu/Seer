 package com.github.idragonfire.dragonskills.skills;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 
 import com.github.idragonfire.dragonskills.DragonSkillsPlugin;
 import com.github.idragonfire.dragonskills.api.DSystem;
 import com.github.idragonfire.dragonskills.api.SkillResult;
 import com.github.idragonfire.dragonskills.api.TargetBlockSkill;
 import com.github.idragonfire.dragonskills.api.TimeEffect;
 import com.github.idragonfire.dragonskills.utils.DUtils;
 import com.github.idragonfire.dragonskills.utils.SkillConfig;
 
 public class Wallhack extends TargetBlockSkill {
 
     @SkillConfig
     private int thickness = 2;
     @SkillConfig
     private int widthLeftAndRight = 2;
     @SkillConfig
     private int duration = 3;
 
     public Wallhack(DragonSkillsPlugin plugin) {
         super(plugin);
     }
 
     @Override
     public SkillResult use(Player player, Block targetBlock) {
         BlockFace[] faces = DUtils.getDirections(player);
         Block[] blocks = new Block[3 * (widthLeftAndRight * 2 + 1)];
         blocks[0] = targetBlock;
         blocks[1] = targetBlock.getRelative(BlockFace.DOWN);
         blocks[2] = targetBlock.getRelative(BlockFace.UP);
         Block left = targetBlock;
         Block right = targetBlock;
         int i = 3;
         while (i < blocks.length) {
             left = left.getRelative(faces[DUtils.LEFT]);
             blocks[i] = left;
             i++;
             right = right.getRelative(faces[DUtils.RIGHT]);
             blocks[i] = right;
             i++;
             blocks[i] = left.getRelative(BlockFace.DOWN);
             i++;
             blocks[i] = right.getRelative(BlockFace.DOWN);
             i++;
             blocks[i] = left.getRelative(BlockFace.UP);
             i++;
             blocks[i] = right.getRelative(BlockFace.UP);
             i++;
         }
         WallHackEffect effect = new WallHackEffect(getPlugin(), duration
                 * DUtils.TICKS, blocks, player, faces);
         effect.startEffect(DUtils.TICK_DELAY);
         DSystem.log("You can look over the wall for $1 seconds", duration);
         return SkillResult.SUCESSFULL;
     }
 
     @Override
     public String getDescription() {
        return "Make a wall invisible and give you a look behind";
     }
 
     public class WallHackEffect extends TimeEffect {
         private Block[] blocks;
         private Player player;
         private BlockFace[] faces;
 
         public WallHackEffect(DragonSkillsPlugin plugin, long duration,
                 Block[] blocks, Player player, BlockFace[] faces) {
             super(plugin, duration);
             this.blocks = blocks;
             this.player = player;
             this.faces = faces;
         }
 
         @Override
         public void initTimeEffect() {
             for (int i = 0; i < thickness; i++) {
                 for (int j = 0; j < blocks.length; j++) {
                     player.sendBlockChange(blocks[j].getLocation(),
                             Material.AIR.getId(), (byte) 0);
                     if (i + 1 < thickness) {
                         blocks[j] = blocks[j].getRelative(faces[DUtils.FRONT]);
                     }
                 }
             }
         }
 
         @Override
         public void endTimeEffect() {
             for (int i = 0; i < thickness; i++) {
                 for (int j = 0; j < blocks.length; j++) {
                     player.sendBlockChange(blocks[j].getLocation(), blocks[j]
                             .getTypeId(), blocks[j].getData());
                     blocks[j] = blocks[j].getRelative(faces[DUtils.BACK]);
                 }
             }
 
         }
 
     }
 
 }
