 /*
  * This file is part of FruitTrees.
  *
  * Copyright © 2013 Visual Illusions Entertainment
  *
  * FruitTrees is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  *
  * FruitTrees is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with FruitTrees.
  * If not, see http://www.gnu.org/licenses/gpl.html.
  */
 package net.visualillusionsent.fruittrees.canary;
 
 import net.canarymod.Canary;
 import net.canarymod.api.GameMode;
 import net.canarymod.api.entity.living.humanoid.Player;
 import net.canarymod.api.inventory.Item;
 import net.canarymod.api.inventory.ItemType;
 import net.canarymod.api.world.blocks.Block;
 import net.canarymod.api.world.blocks.BlockType;
 import net.canarymod.hook.HookHandler;
 import net.canarymod.hook.player.BlockDestroyHook;
 import net.canarymod.hook.player.BlockRightClickHook;
 import net.canarymod.hook.player.CraftHook;
 import net.canarymod.hook.system.LoadWorldHook;
 import net.canarymod.hook.system.UnloadWorldHook;
 import net.canarymod.hook.world.BlockUpdateHook;
 import net.canarymod.hook.world.TreeGrowHook;
 import net.canarymod.plugin.PluginListener;
 import net.canarymod.plugin.Priority;
 import net.visualillusionsent.fruittrees.TreeTracker;
 import net.visualillusionsent.fruittrees.TreeType;
 import net.visualillusionsent.fruittrees.TreeWorld;
 import net.visualillusionsent.fruittrees.trees.AppleTree;
 import net.visualillusionsent.fruittrees.trees.CoalTree;
 import net.visualillusionsent.fruittrees.trees.DiamondTree;
 import net.visualillusionsent.fruittrees.trees.DyeTree;
 import net.visualillusionsent.fruittrees.trees.EmeraldTree;
 import net.visualillusionsent.fruittrees.trees.FruitTree;
 import net.visualillusionsent.fruittrees.trees.GoldTree;
 import net.visualillusionsent.fruittrees.trees.GoldenAppleTree;
 import net.visualillusionsent.fruittrees.trees.IronTree;
 import net.visualillusionsent.fruittrees.trees.RecordTree;
 import net.visualillusionsent.fruittrees.trees.RedstoneTree;
 import net.visualillusionsent.fruittrees.trees.SpongeTree;
 
 public class CanaryFruitTreesListener implements PluginListener {
 
     public CanaryFruitTreesListener(CanaryFruitTrees cft) {
         Canary.hooks().registerListener(this, cft);
     }
 
     @HookHandler(priority = Priority.LOW)
     public final void craftSeeds(CraftHook hook) {
         if (!CanaryFruitTrees.instance().getFruitTreesConfig().requirePermissions()) {
             return;
         }
         if (SeedGen.recipes.contains(hook.getMatchingRecipe())) {
             String type = hook.getRecipeResult().getMetaTag().getCompoundTag("FruitTrees").getString("TreeType");
             if (!hook.getPlayer().hasPermission("fruittrees.craft.".concat(type.toLowerCase().replace("seeds", "")))) {
                 hook.setCanceled();
                 return;
             }
         }
     }
 
     @HookHandler(priority = Priority.LOW)
     public final void plantSeeds(BlockRightClickHook hook) {
         if (hook.getBlockClicked().getType() == BlockType.Soil) {
             Item seeds = hook.getPlayer().getItemHeld();
             Block block = hook.getBlockClicked();
            if (seeds.getMetaTag().containsKey("FruitTrees")) {
                 String type = seeds.getMetaTag().getCompoundTag("FruitTrees").getString("TreeType");
                 if (seeds.getType() == ItemType.MelonSeeds) {
                     if (type.equals("AppleSeeds")) {
                         if (CanaryFruitTrees.instance().getFruitTreesConfig().checkEnabled(TreeType.APPLE)) {
                             block.getWorld().setBlockAt(block.getPosition(), (short)3);
                             block.getWorld().setBlockAt(block.getX(), block.getY() + 1, block.getZ(), (short)6, TreeType.APPLE.getLogData());
                             FruitTree tree = new AppleTree(CanaryFruitTrees.instance(), block.getX(), block.getY() + 1, block.getZ(), CanaryFruitTrees.instance().getWorldForName(block.getWorld().getFqName()));
                             tree.save();
                             decreaseStack(hook.getPlayer());
                             hook.setCanceled();
                         }
                     }
                     else if (type.equals("GoldenAppleSeeds")) {
                         if (CanaryFruitTrees.instance().getFruitTreesConfig().checkEnabled(TreeType.GOLDEN_APPLE)) {
                             block.getWorld().setBlockAt(block.getPosition(), (short)3);
                             block.getWorld().setBlockAt(block.getX(), block.getY() + 1, block.getZ(), (short)6, TreeType.GOLDEN_APPLE.getLogData());
                             FruitTree tree = new GoldenAppleTree(CanaryFruitTrees.instance(), block.getX(), block.getY() + 1, block.getZ(), CanaryFruitTrees.instance().getWorldForName(block.getWorld().getFqName()));
                             tree.save();
                             decreaseStack(hook.getPlayer());
                             hook.setCanceled();
                         }
                     }
                     else if (type.equals("CoalSeeds")) {
                         if (CanaryFruitTrees.instance().getFruitTreesConfig().checkEnabled(TreeType.COAL)) {
                             block.getWorld().setBlockAt(block.getPosition(), (short)3);
                             block.getWorld().setBlockAt(block.getX(), block.getY() + 1, block.getZ(), (short)6, TreeType.COAL.getLogData());
                             FruitTree tree = new CoalTree(CanaryFruitTrees.instance(), block.getX(), block.getY() + 1, block.getZ(), CanaryFruitTrees.instance().getWorldForName(block.getWorld().getFqName()));
                             tree.save();
                             decreaseStack(hook.getPlayer());
                             hook.setCanceled();
                         }
                     }
                 }
                 else if (seeds.getType() == ItemType.PumpkinSeeds) {
                     if (type.equals("RecordSeeds")) {
                         block.getWorld().setBlockAt(block.getPosition(), (short)3);
                         block.getWorld().setBlockAt(block.getX(), block.getY() + 1, block.getZ(), (short)6, TreeType.RECORD.getLogData());
                         FruitTree tree = new RecordTree(CanaryFruitTrees.instance(), block.getX(), block.getY() + 1, block.getZ(), CanaryFruitTrees.instance().getWorldForName(block.getWorld().getFqName()));
                         tree.save();
                         decreaseStack(hook.getPlayer());
                         hook.setCanceled();
                     }
                     else if (type.equals("SpongeSeeds")) {
                         block.getWorld().setBlockAt(block.getPosition(), (short)3);
                         block.getWorld().setBlockAt(block.getX(), block.getY() + 1, block.getZ(), (short)6, TreeType.SPONGE.getLogData());
                         FruitTree tree = new SpongeTree(CanaryFruitTrees.instance(), block.getX(), block.getY() + 1, block.getZ(), CanaryFruitTrees.instance().getWorldForName(block.getWorld().getFqName()));
                         tree.save();
                         decreaseStack(hook.getPlayer());
                         hook.setCanceled();
                     }
                 }
                 else if (seeds.getType() == ItemType.Seeds) {
                     if (type.endsWith("DyeSeeds")) {
                         block.getWorld().setBlockAt(block.getPosition(), (short)3);
                         block.getWorld().setBlockAt(block.getX(), block.getY() + 1, block.getZ(), (short)6, TreeType.DYE_BLACK.getLogData());
                         FruitTree tree = new DyeTree(CanaryFruitTrees.instance(), block.getX(), block.getY() + 1, block.getZ(), CanaryFruitTrees.instance().getWorldForName(block.getWorld().getFqName()), seeds.getMetaTag().getCompoundTag("FruitTrees").getByte("DyeColor"));
                         tree.save();
                         decreaseStack(hook.getPlayer());
                         hook.setCanceled();
                     }
                     else if (type.equals("RedstoneSeeds")) {
                         block.getWorld().setBlockAt(block.getPosition(), (short)3);
                         block.getWorld().setBlockAt(block.getX(), block.getY() + 1, block.getZ(), (short)6, TreeType.REDSTONE.getLogData());
                         FruitTree tree = new RedstoneTree(CanaryFruitTrees.instance(), block.getX(), block.getY() + 1, block.getZ(), CanaryFruitTrees.instance().getWorldForName(block.getWorld().getFqName()));
                         tree.save();
                         decreaseStack(hook.getPlayer());
                         hook.setCanceled();
                     }
                     else if (type.equals("IronSeeds")) {
                         block.getWorld().setBlockAt(block.getPosition(), (short)3);
                         block.getWorld().setBlockAt(block.getX(), block.getY() + 1, block.getZ(), (short)6, TreeType.IRON.getLogData());
                         FruitTree tree = new IronTree(CanaryFruitTrees.instance(), block.getX(), block.getY() + 1, block.getZ(), CanaryFruitTrees.instance().getWorldForName(block.getWorld().getFqName()));
                         tree.save();
                         decreaseStack(hook.getPlayer());
                         hook.setCanceled();
                     }
                     else if (type.equals("GoldSeeds")) {
                         block.getWorld().setBlockAt(block.getPosition(), (short)3);
                         block.getWorld().setBlockAt(block.getX(), block.getY() + 1, block.getZ(), (short)6, TreeType.GOLD.getLogData());
                         FruitTree tree = new GoldTree(CanaryFruitTrees.instance(), block.getX(), block.getY() + 1, block.getZ(), CanaryFruitTrees.instance().getWorldForName(block.getWorld().getFqName()));
                         tree.save();
                         decreaseStack(hook.getPlayer());
                         hook.setCanceled();
                     }
                     else if (type.equals("DiamondSeeds")) {
                         block.getWorld().setBlockAt(block.getPosition(), (short)3);
                         block.getWorld().setBlockAt(block.getX(), block.getY() + 1, block.getZ(), (short)6, TreeType.GOLD.getLogData());
                         FruitTree tree = new DiamondTree(CanaryFruitTrees.instance(), block.getX(), block.getY() + 1, block.getZ(), CanaryFruitTrees.instance().getWorldForName(block.getWorld().getFqName()));
                         tree.save();
                         decreaseStack(hook.getPlayer());
                         hook.setCanceled();
                     }
                     else if (type.equals("EmeraldSeeds")) {
                         block.getWorld().setBlockAt(block.getPosition(), (short)3);
                         block.getWorld().setBlockAt(block.getX(), block.getY() + 1, block.getZ(), (short)6, TreeType.GOLD.getLogData());
                         FruitTree tree = new EmeraldTree(CanaryFruitTrees.instance(), block.getX(), block.getY() + 1, block.getZ(), CanaryFruitTrees.instance().getWorldForName(block.getWorld().getFqName()));
                         tree.save();
                         decreaseStack(hook.getPlayer());
                         hook.setCanceled();
                     }
                     else if (type.equals("CoalSeeds")) {
                         block.getWorld().setBlockAt(block.getPosition(), (short)3);
                         block.getWorld().setBlockAt(block.getX(), block.getY() + 1, block.getZ(), (short)6, TreeType.GOLD.getLogData());
                         FruitTree tree = new CoalTree(CanaryFruitTrees.instance(), block.getX(), block.getY() + 1, block.getZ(), CanaryFruitTrees.instance().getWorldForName(block.getWorld().getFqName()));
                         tree.save();
                         decreaseStack(hook.getPlayer());
                         hook.setCanceled();
                     }
                 }
             }
         }
     }
 
     @HookHandler(priority = Priority.LOW)
     public final void killTree(BlockDestroyHook hook) {
         if (BlockType.fromId(hook.getBlock().getTypeId()) == BlockType.OakSapling) {
             Block block = hook.getBlock();
             FruitTree tree = TreeTracker.getTreeAt(block.getX(), block.getY(), block.getZ(), CanaryFruitTrees.instance().getWorldForName(block.getWorld().getFqName()));
             if (tree != null) {
                 if (hook.getPlayer().getMode() != GameMode.CREATIVE) { //If creative, no need to drop stuff
                     hook.setCanceled();
                     switch (tree.getType()) {
                         case APPLE:
                             block.getWorld().dropItem(block.getPosition(), SeedGen.seeds[0].clone());
                             break;
                         case GOLDEN_APPLE:
                             block.getWorld().dropItem(block.getPosition(), SeedGen.seeds[1].clone());
                             break;
                         case SPONGE:
                             block.getWorld().dropItem(block.getPosition(), SeedGen.seeds[2].clone());
                             break;
                         case RECORD:
                             block.getWorld().dropItem(block.getPosition(), SeedGen.seeds[3].clone());
                             break;
                         case DYE_BLACK:
                             block.getWorld().dropItem(block.getPosition(), SeedGen.seeds[4].clone());
                             break;
                         case DYE_RED:
                             block.getWorld().dropItem(block.getPosition(), SeedGen.seeds[5].clone());
                             break;
                         case DYE_GREEN:
                             block.getWorld().dropItem(block.getPosition(), SeedGen.seeds[6].clone());
                             break;
                         case DYE_BROWN:
                             block.getWorld().dropItem(block.getPosition(), SeedGen.seeds[7].clone());
                             break;
                         case DYE_BLUE:
                             block.getWorld().dropItem(block.getPosition(), SeedGen.seeds[8].clone());
                             break;
                         case DYE_PURPLE:
                             block.getWorld().dropItem(block.getPosition(), SeedGen.seeds[9].clone());
                             break;
                         case DYE_CYAN:
                             block.getWorld().dropItem(block.getPosition(), SeedGen.seeds[10].clone());
                             break;
                         case DYE_LIGHT_GRAY:
                             block.getWorld().dropItem(block.getPosition(), SeedGen.seeds[11].clone());
                             break;
                         case DYE_GRAY:
                             block.getWorld().dropItem(block.getPosition(), SeedGen.seeds[12].clone());
                             break;
                         case DYE_PINK:
                             block.getWorld().dropItem(block.getPosition(), SeedGen.seeds[13].clone());
                             break;
                         case DYE_LIME:
                             block.getWorld().dropItem(block.getPosition(), SeedGen.seeds[14].clone());
                             break;
                         case DYE_YELLOW:
                             block.getWorld().dropItem(block.getPosition(), SeedGen.seeds[15].clone());
                             break;
                         case DYE_LIGHT_BLUE:
                             block.getWorld().dropItem(block.getPosition(), SeedGen.seeds[16].clone());
                             break;
                         case DYE_MAGENTA:
                             block.getWorld().dropItem(block.getPosition(), SeedGen.seeds[17].clone());
                             break;
                         case DYE_ORANGE:
                             block.getWorld().dropItem(block.getPosition(), SeedGen.seeds[18].clone());
                             break;
                         case DYE_WHITE:
                             block.getWorld().dropItem(block.getPosition(), SeedGen.seeds[19].clone());
                             break;
                         case REDSTONE:
                             block.getWorld().dropItem(block.getPosition(), SeedGen.seeds[20].clone());
                             break;
                         case IRON:
                             block.getWorld().dropItem(block.getPosition(), SeedGen.seeds[21].clone());
                             break;
                         case GOLD:
                             block.getWorld().dropItem(block.getPosition(), SeedGen.seeds[22].clone());
                             break;
                         case DIAMOND:
                             block.getWorld().dropItem(block.getPosition(), SeedGen.seeds[23].clone());
                             break;
                         case EMERALD:
                             block.getWorld().dropItem(block.getPosition(), SeedGen.seeds[24].clone());
                             break;
                         case COAL:
                             block.getWorld().dropItem(block.getPosition(), SeedGen.seeds[25].clone());
                             break;
                         default:
                             break;
                     }
                     block.setTypeId((short)0);
                     block.getWorld().setBlock(block);
                 }
                 tree.killTree();
             }
         }
     }
 
     @HookHandler(priority = Priority.LOW)
     public final void killTreeArea(BlockUpdateHook hook) { //BlockUpdate a little more reliable with tracking Tree destruction (Especially if editting out a tree)
         Block block = hook.getBlock();
         if (block.getTypeId() == BlockType.OakLog.getId()) {
             FruitTree tree = TreeTracker.isTreeArea(block.getX(), block.getY(), block.getZ(), block.getTypeId(), block.getData(), CanaryFruitTrees.instance().getWorldForName(block.getWorld().getFqName()));
             if (tree != null) {
                 tree.killTree();
             }
         }
         else if (block.getTypeId() == BlockType.OakLeaves.getId()) {
             FruitTree tree = TreeTracker.isTreeArea(block.getX(), block.getY(), block.getZ(), block.getTypeId(), block.getData(), CanaryFruitTrees.instance().getWorldForName(block.getWorld().getFqName()));
             if (tree != null) {
                 tree.killTree();
             }
         }
         else if (block.getType() == BlockType.Sponge) {
             FruitTree tree = TreeTracker.isTreeArea(block.getX(), block.getY(), block.getZ(), block.getTypeId(), block.getData(), CanaryFruitTrees.instance().getWorldForName(block.getWorld().getFqName()));
             if (tree != null) {
                 tree.killTree();
             }
         }
         else if (block.getTypeId() == BlockType.WoolWhite.getId()) {
             FruitTree tree = TreeTracker.isTreeArea(block.getX(), block.getY(), block.getZ(), block.getTypeId(), block.getData(), CanaryFruitTrees.instance().getWorldForName(block.getWorld().getFqName()));
             if (tree != null) {
                 tree.killTree();
             }
         }
         else if (block.getType() == BlockType.RedstoneBlock) {
             FruitTree tree = TreeTracker.isTreeArea(block.getX(), block.getY(), block.getZ(), block.getTypeId(), block.getData(), CanaryFruitTrees.instance().getWorldForName(block.getWorld().getFqName()));
             if (tree != null) {
                 tree.killTree();
             }
         }
         else if (block.getType() == BlockType.NoteBlock) {
             FruitTree tree = TreeTracker.isTreeArea(block.getX(), block.getY(), block.getZ(), block.getTypeId(), block.getData(), CanaryFruitTrees.instance().getWorldForName(block.getWorld().getFqName()));
             if (tree != null) {
                 tree.killTree();
             }
         }
         else if (block.getType() == BlockType.IronBlock) {
             FruitTree tree = TreeTracker.isTreeArea(block.getX(), block.getY(), block.getZ(), block.getTypeId(), block.getData(), CanaryFruitTrees.instance().getWorldForName(block.getWorld().getFqName()));
             if (tree != null) {
                 tree.killTree();
             }
         }
         else if (block.getType() == BlockType.GoldBlock) {
             FruitTree tree = TreeTracker.isTreeArea(block.getX(), block.getY(), block.getZ(), block.getTypeId(), block.getData(), CanaryFruitTrees.instance().getWorldForName(block.getWorld().getFqName()));
             if (tree != null) {
                 tree.killTree();
             }
         }
         else if (block.getType() == BlockType.DiamondBlock) {
             FruitTree tree = TreeTracker.isTreeArea(block.getX(), block.getY(), block.getZ(), block.getTypeId(), block.getData(), CanaryFruitTrees.instance().getWorldForName(block.getWorld().getFqName()));
             if (tree != null) {
                 tree.killTree();
             }
         }
         else if (block.getType() == BlockType.EmeraldBlock) {
             FruitTree tree = TreeTracker.isTreeArea(block.getX(), block.getY(), block.getZ(), block.getTypeId(), block.getData(), CanaryFruitTrees.instance().getWorldForName(block.getWorld().getFqName()));
             if (tree != null) {
                 tree.killTree();
             }
         }
         else if (block.getType() == BlockType.CoalBlock) {
             FruitTree tree = TreeTracker.isTreeArea(block.getX(), block.getY(), block.getZ(), block.getTypeId(), block.getData(), CanaryFruitTrees.instance().getWorldForName(block.getWorld().getFqName()));
             if (tree != null) {
                 tree.killTree();
             }
         }
     }
 
     @HookHandler(priority = Priority.HIGH)
     public final void treeGrow(TreeGrowHook hook) {
         Block sapling = hook.getSapling();
         FruitTree tree = TreeTracker.getTreeAt(sapling.getX(), sapling.getY(), sapling.getZ(), CanaryFruitTrees.instance().getWorldForName(sapling.getWorld().getFqName()));
         if (tree != null) {
             tree.growTree();
             hook.setCanceled();
         }
     }
 
     @HookHandler
     public final void worldLoad(LoadWorldHook hook) {
         CanaryFruitTrees.instance().addLoadedWorld(hook.getWorld());
     }
 
     @HookHandler
     public final void worldunload(UnloadWorldHook hook) {
         TreeWorld tree_world = CanaryFruitTrees.instance().getWorldForName(hook.getWorld().getFqName());
         if (tree_world != null) {
             CanaryFruitTrees.instance().debug("World Unloaded: " + tree_world);
             tree_world.unloadWorld();
         }
     }
 
     private final void decreaseStack(Player player) {
         if (player.getMode() != GameMode.CREATIVE) {
             Item held = player.getItemHeld();
             held.setAmount(held.getAmount() - 1);
             if (held.getAmount() <= 0) {
                 player.getInventory().setSlot(held.getSlot(), null);
             }
         }
     }
 }
