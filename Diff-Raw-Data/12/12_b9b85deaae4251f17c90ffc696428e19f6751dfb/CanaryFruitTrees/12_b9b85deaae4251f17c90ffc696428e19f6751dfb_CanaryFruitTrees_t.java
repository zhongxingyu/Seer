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
 package net.visualillusionsent.minecraft.server.mod.canary.plugin.fruittrees;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.jar.Attributes;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 import java.util.logging.Level;
 import net.canarymod.Canary;
 import net.canarymod.api.inventory.Item;
 import net.canarymod.api.inventory.ItemType;
 import net.canarymod.api.inventory.recipes.CraftingRecipe;
 import net.canarymod.api.inventory.recipes.RecipeRow;
 import net.canarymod.api.nbt.CompoundTag;
 import net.canarymod.api.world.World;
 import net.canarymod.config.Configuration;
 import net.canarymod.logger.CanaryLevel;
 import net.canarymod.plugin.Plugin;
import net.visualillusionsent.minecraft.server.mod.canary.plugin.fruittrees.commands.FruitTreesCommands;
 import net.visualillusionsent.minecraft.server.mod.fruittrees.FruitTree;
 import net.visualillusionsent.minecraft.server.mod.fruittrees.FruitTrees;
 import net.visualillusionsent.minecraft.server.mod.fruittrees.FruitTreesConfigurations;
 import net.visualillusionsent.minecraft.server.mod.fruittrees.TreeTracker;
 import net.visualillusionsent.minecraft.server.mod.fruittrees.TreeType;
 import net.visualillusionsent.minecraft.server.mod.fruittrees.TreeWorld;
 import net.visualillusionsent.minecraft.server.mod.fruittrees.data.TreeStorage;
 import net.visualillusionsent.minecraft.server.mod.fruittrees.data.XMLStorage;
 import net.visualillusionsent.utils.ProgramStatus;
 import net.visualillusionsent.utils.PropertiesFile;
 import net.visualillusionsent.utils.VersionChecker;
 
 public class CanaryFruitTrees extends Plugin implements FruitTrees{
 
     private final CanaryTreeWorldCache world_cache = new CanaryTreeWorldCache(this);
     private TreeStorage storage;
     private FruitTreesConfigurations ft_cfg;
     private final VersionChecker vc;
     private float version;
     private short build;
     private String buildTime;
     private ProgramStatus status;
     private static CanaryFruitTrees $;
     final static Item[] seeds = new Item[128]; //This is set to 128 for quick expansion durring development
     private final static String[] dyes = new String[] { "Black", "Red", "Green", "Brown", "Blue", "Purple", "Cyan", "Light_Gray", "Gray", "Pink", "Lime", "Yellow", "Light_Blue", "Magenta", "Orange", "White" };
 
     static {
         genAppleSeeds();
         genGoldAppleSeeds();
         genSpongeSeeds();
         genRecordSeeds();
         genDyeSeeds();
         genRedstoneSeeds();
     }
 
     public CanaryFruitTrees(){
         readManifest();
         vc = new VersionChecker(getName(), String.valueOf(version), String.valueOf(build), "http://visualillusionsent.net/minecraft/plugins/", status, false);
     }
 
     @Override
     public boolean enable(){
         $ = this;
         checkStatus();
         checkVersion();
         ft_cfg = new FruitTreesConfigurations(this);
         storage = new XMLStorage(this);
         for (World world : Canary.getServer().getWorldManager().getAllWorlds()) {
             world_cache.setExistingWorlds(new CanaryTreeWorld(this, world, world.getFqName()));
             storage.loadTreesForWorld(world_cache.getTreeWorld(world.getFqName()));
         }
         new CanaryFruitTreesListener(this);
        new FruitTreesCommands(this);
         addAppleSeedsRecipe();
         addGoldenAppleSeedsRecipe0();
         addGoldenAppleSeedsRecipe1();
         addSpongeSeedRecipe0();
         addSpongeSeedRecipe1();
         addRecordSeedRecipe();
         addDyeSeedsRecipe();
         addRedstoneSeedsRecipe();
         return true;
     }
 
     @Override
     public void disable(){
         $ = null;
         Arrays.fill(seeds, null);
     }
 
     public TreeWorld getWorldForName(String name){
         return world_cache.getTreeWorld(name);
     }
 
     protected void addLoadedWorld(World world){
         TreeWorld tree_world = world_cache.getTreeWorld(world.getFqName());
         if (tree_world == null) {
             world_cache.setExistingWorlds(new CanaryTreeWorld(this, world, world.getFqName()));
             storage.loadTreesForWorld(world_cache.getTreeWorld(world.getFqName()));
         }
         else {
             tree_world.isLoaded();
         }
     }
 
     protected final void worldUnload(TreeWorld tree_world){
         for (FruitTree tree : TreeTracker.getTreesInWorld(tree_world)) {
             storage.storeTree(tree);
         }
     }
 
     private final void addAppleSeedsRecipe(){
         if (ft_cfg.checkEnabled(TreeType.APPLE)) {
             Item apple = Canary.factory().getItemFactory().newItem(ItemType.Apple, 0, 1);
             Item aplSeeds = seeds[0].clone();
             aplSeeds.setAmount(3);
             Canary.getServer().addRecipe(new CraftingRecipe(aplSeeds, apple));
         }
     }
 
     private final void addGoldenAppleSeedsRecipe0(){
         if (ft_cfg.checkEnabled(TreeType.GOLDEN_APPLE)) {
             Item golden_apple = Canary.factory().getItemFactory().newItem(ItemType.GoldenApple, 0, 1);
             Item gld_apl_seeds = seeds[1].clone();
             gld_apl_seeds.setAmount(3);
             Canary.getServer().addRecipe(new CraftingRecipe(gld_apl_seeds, golden_apple));
         }
     }
 
     private final void addGoldenAppleSeedsRecipe1(){
         if (ft_cfg.checkEnabled(TreeType.GOLDEN_APPLE)) {
             Item gld_apl_seeds = seeds[1].clone();
             Item gold_nuggets = Canary.factory().getItemFactory().newItem(ItemType.GoldNugget, 0, 1);
             RecipeRow[] rows = new RecipeRow[] { new RecipeRow("NNN", new Item[] { gold_nuggets }),
                 new RecipeRow("NSN", new Item[] { gold_nuggets, seeds[0].clone() }),
                 new RecipeRow("NNN", new Item[] { gold_nuggets })
             };
             Canary.getServer().addRecipe(new CraftingRecipe(gld_apl_seeds, rows));
         }
     }
 
     private final void addSpongeSeedRecipe0(){
         if (ft_cfg.checkEnabled(TreeType.SPONGE)) {
             Item sponge_seeds = seeds[2].clone();
             sponge_seeds.setAmount(2);
             Item yellow_wool = Canary.factory().getItemFactory().newItem(ItemType.Cloth, 4, 1);
             Item clay_ball = Canary.factory().getItemFactory().newItem(ItemType.ClayBall, 0, 1);
             Canary.getServer().addRecipe(new CraftingRecipe(sponge_seeds, yellow_wool, clay_ball));
         }
     }
 
     private final void addSpongeSeedRecipe1(){
         if (ft_cfg.checkEnabled(TreeType.SPONGE)) {
             Item sponge_seeds = seeds[2].clone();
             sponge_seeds.setAmount(3);
             Item sponge = Canary.factory().getItemFactory().newItem(ItemType.Sponge, 0, 1);
             Canary.getServer().addRecipe(new CraftingRecipe(sponge_seeds, sponge));
         }
     }
 
     private final void addRecordSeedRecipe(){
         if (ft_cfg.checkEnabled(TreeType.RECORD)) {
             Item record_seeds = seeds[3].clone();
             record_seeds.setAmount(2);
             Item record = Canary.factory().getItemFactory().newItem(ItemType.GoldRecord, 0, 1);
             Canary.getServer().addRecipe(new CraftingRecipe(record_seeds.clone(), record));
             record = Canary.factory().getItemFactory().newItem(ItemType.GreenRecord, 0, 1);
             Canary.getServer().addRecipe(new CraftingRecipe(record_seeds.clone(), record));
             record = Canary.factory().getItemFactory().newItem(ItemType.BlocksRecord, 0, 1);
             Canary.getServer().addRecipe(new CraftingRecipe(record_seeds.clone(), record));
             record = Canary.factory().getItemFactory().newItem(ItemType.ChirpRecord, 0, 1);
             Canary.getServer().addRecipe(new CraftingRecipe(record_seeds.clone(), record));
             record = Canary.factory().getItemFactory().newItem(ItemType.FarRecord, 0, 1);
             Canary.getServer().addRecipe(new CraftingRecipe(record_seeds.clone(), record));
             record = Canary.factory().getItemFactory().newItem(ItemType.MallRecord, 0, 1);
             Canary.getServer().addRecipe(new CraftingRecipe(record_seeds.clone(), record));
             record = Canary.factory().getItemFactory().newItem(ItemType.MellohiRecord, 0, 1);
             Canary.getServer().addRecipe(new CraftingRecipe(record_seeds.clone(), record));
             record = Canary.factory().getItemFactory().newItem(ItemType.StalRecord, 0, 1);
             Canary.getServer().addRecipe(new CraftingRecipe(record_seeds.clone(), record));
             record = Canary.factory().getItemFactory().newItem(ItemType.StradRecord, 0, 1);
             Canary.getServer().addRecipe(new CraftingRecipe(record_seeds.clone(), record));
             record = Canary.factory().getItemFactory().newItem(ItemType.WardRecord, 0, 1);
             Canary.getServer().addRecipe(new CraftingRecipe(record_seeds.clone(), record));
             record = Canary.factory().getItemFactory().newItem(ItemType.ElevenRecord, 0, 1);
             Canary.getServer().addRecipe(new CraftingRecipe(record_seeds.clone(), record));
             record = Canary.factory().getItemFactory().newItem(ItemType.WaitRecord, 0, 1);
             Canary.getServer().addRecipe(new CraftingRecipe(record_seeds.clone(), record));
         }
     }
 
     private final void addRedstoneSeedsRecipe(){
         if (ft_cfg.checkEnabled(TreeType.REDSTONE)) {
             Item redstone_seeds = seeds[20].clone();
             redstone_seeds.setAmount(2);
             Item redstone_dust = Canary.factory().getItemFactory().newItem(ItemType.RedStone, 0, 1);
             Item normal_seeds = Canary.factory().getItemFactory().newItem(ItemType.Seeds, 0, 1);
             Canary.getServer().addRecipe(new CraftingRecipe(redstone_seeds, redstone_dust, normal_seeds));
         }
     }
 
     private final void addDyeSeedsRecipe(){
         for (int index = 0; index <= 15; index++) {
             if (ft_cfg.checkEnabled(TreeType.valueOf("DYE_".concat(dyes[index].toUpperCase())))) {
                 Item dye_seeds = seeds[index + 4].clone();
                 dye_seeds.setAmount(2);
                 Item seeds = Canary.factory().getItemFactory().newItem(ItemType.Seeds, 0, 1);
                 Item dye = Canary.factory().getItemFactory().newItem(ItemType.InkSack, index, 1);
                 Canary.getServer().addRecipe(new CraftingRecipe(dye_seeds.clone(), seeds, dye));
             }
         }
     }
 
     private static final void genAppleSeeds(){
         Item appleSeeds = Canary.factory().getItemFactory().newItem(ItemType.MelonSeeds, 0, 1);
         appleSeeds.setDisplayName("Apple Seeds");
         appleSeeds.setLore("Plant to grow an Apple Tree");
         CompoundTag fruit_trees_tag = Canary.factory().getNBTFactory().newCompoundTag("FruitTrees");
         fruit_trees_tag.put("TreeType", Canary.factory().getNBTFactory().newStringTag("TreeType", "AppleSeeds"));
         appleSeeds.getMetaTag().put("FruitTrees", fruit_trees_tag);
         seeds[0] = appleSeeds;
     }
 
     private static final void genGoldAppleSeeds(){
         Item gold_apple_seeds = Canary.factory().getItemFactory().newItem(ItemType.MelonSeeds, 0, 1);
         gold_apple_seeds.setDisplayName("Golden Apple Seeds");
         gold_apple_seeds.setLore("Plant to grow a Golden Apple Tree");
         CompoundTag fruit_trees_tag = Canary.factory().getNBTFactory().newCompoundTag("FruitTrees");
         fruit_trees_tag.put("TreeType", Canary.factory().getNBTFactory().newStringTag("TreeType", "GoldenAppleSeeds"));
         gold_apple_seeds.getMetaTag().put("FruitTrees", fruit_trees_tag);
         seeds[1] = gold_apple_seeds;
     }
 
     private static final void genSpongeSeeds(){
         Item sponge_seeds = Canary.factory().getItemFactory().newItem(ItemType.PumpkinSeeds, 0, 1);
         sponge_seeds.setDisplayName("Sponge Seeds");
         sponge_seeds.setLore("Plant to grow a Sponge Tree");
         CompoundTag fruit_trees_tag = Canary.factory().getNBTFactory().newCompoundTag("FruitTrees");
         fruit_trees_tag.put("TreeType", Canary.factory().getNBTFactory().newStringTag("TreeType", "SpongeSeeds"));
         sponge_seeds.getMetaTag().put("FruitTrees", fruit_trees_tag);
         seeds[2] = sponge_seeds;
     }
 
     private static final void genRecordSeeds(){
         Item record_seeds = Canary.factory().getItemFactory().newItem(ItemType.PumpkinSeeds, 0, 1);
         record_seeds.setDisplayName("Record Seeds");
         record_seeds.setLore("Plant to grow a Record Tree");
         CompoundTag fruit_trees_tag = Canary.factory().getNBTFactory().newCompoundTag("FruitTrees");
         fruit_trees_tag.put("TreeType", Canary.factory().getNBTFactory().newStringTag("TreeType", "RecordSeeds"));
         record_seeds.getMetaTag().put("FruitTrees", fruit_trees_tag);
         seeds[3] = record_seeds;
     }
 
     private static final void genDyeSeeds(){
         Item dye_seeds = Canary.factory().getItemFactory().newItem(ItemType.Seeds, 0, 1);
         for (int index = 0; index <= 15; index++) {
             dye_seeds.setDisplayName(String.format("%s Dye Seeds", dyes[index]));
             dye_seeds.setLore(String.format("Plant to grow a %s Dye Tree", dyes[index]));
             CompoundTag fruit_trees_tag = Canary.factory().getNBTFactory().newCompoundTag("FruitTrees");
             fruit_trees_tag.put("TreeType", Canary.factory().getNBTFactory().newStringTag("TreeType", "DyeSeeds"));
             fruit_trees_tag.put("DyeColor", Canary.factory().getNBTFactory().newByteTag("DyeColor", (byte) index));
             dye_seeds.getMetaTag().put("FruitTrees", fruit_trees_tag);
             seeds[index + 4] = dye_seeds.clone();
         }
     }
 
     private static final void genRedstoneSeeds(){
         Item redstone_seeds = Canary.factory().getItemFactory().newItem(ItemType.Seeds, 0, 1);
         redstone_seeds.setDisplayName("Redstone Seeds");
         redstone_seeds.setLore("Plant to grow a Redstone Tree");
         CompoundTag fruit_trees_tag = Canary.factory().getNBTFactory().newCompoundTag("FruitTrees");
         fruit_trees_tag.put("TreeType", Canary.factory().getNBTFactory().newStringTag("TreeType", "RedstoneSeeds"));
         redstone_seeds.getMetaTag().put("FruitTrees", fruit_trees_tag);
         seeds[20] = redstone_seeds;
     }
 
     //21 is the next seeds index
 
     public static final CanaryFruitTrees instance(){
         return $;
     }
 
     public final void debug(String msg){
         if (ft_cfg.debug()) {
             getLogman().log(CanaryLevel.PLUGIN_DEBUG, msg);
         }
     }
 
     public final void info(String msg){
         getLogman().info(msg);
     }
 
     public final void warning(String msg){
         getLogman().warning(msg);
     }
 
     public final void severe(String msg){
         getLogman().severe(msg);
     }
 
     public final void severe(String msg, Throwable thrown){
         getLogman().log(Level.SEVERE, msg, thrown);
     }
 
     public final PropertiesFile getConfig(){
         return Configuration.getPluginConfig(this.getName());
     }
 
     public final FruitTreesConfigurations getFruitTreesConfig(){
         return this.ft_cfg;
     }
 
     public final TreeStorage getStorage(){
         return storage;
     }
 
     private final Manifest getManifest() throws Exception{
         Manifest toRet = null;
         Exception ex = null;
         JarFile jar = null;
         try {
             jar = new JarFile(getJarPath());
             toRet = jar.getManifest();
         }
         catch (Exception e) {
             ex = e;
         }
         finally {
             if (jar != null) {
                 try {
                     jar.close();
                 }
                 catch (IOException e) {}
             }
             if (ex != null) {
                 throw ex;
             }
         }
         return toRet;
     }
 
     private final void readManifest(){
         try {
             Manifest manifest = getManifest();
             Attributes mainAttribs = manifest.getMainAttributes();
             version = Float.parseFloat(mainAttribs.getValue("Version").replace("-SNAPSHOT", ""));
             build = Short.parseShort(mainAttribs.getValue("Build"));
             buildTime = mainAttribs.getValue("Build-Time");
             try {
                 status = ProgramStatus.valueOf(mainAttribs.getValue("ProgramStatus"));
             }
             catch (IllegalArgumentException iaex) {
                 status = ProgramStatus.UNKNOWN;
             }
         }
         catch (Exception ex) {
             version = -1.0F;
             build = -1;
             buildTime = "19700101-0000";
         }
     }
 
     private final void checkStatus(){
         if (status == ProgramStatus.UNKNOWN) {
             getLogman().severe(String.format("%s has declared itself as an 'UNKNOWN STATUS' build. Use is not advised and could cause damage to your system!", getName()));
         }
         else if (status == ProgramStatus.ALPHA) {
             getLogman().warning(String.format("%s has declared itself as a 'ALPHA' build. Production use is not advised!", getName()));
         }
         else if (status == ProgramStatus.BETA) {
             getLogman().warning(String.format("%s has declared itself as a 'BETA' build. Production use is not advised!", getName()));
         }
         else if (status == ProgramStatus.RELEASE_CANDIDATE) {
             getLogman().info(String.format("%s has declared itself as a 'Release Candidate' build. Expect some bugs.", getName()));
         }
     }
 
     private final void checkVersion(){
         Boolean islatest = vc.isLatest();
         if (islatest == null) {
             getLogman().warning("VersionCheckerError: " + vc.getErrorMessage());
         }
         else if (!vc.isLatest()) {
             getLogman().warning(vc.getUpdateAvailibleMessage());
             getLogman().warning(String.format("You can view update info @ http://wiki.visualillusionsent.net/%s#ChangeLog", getName()));
         }
     }
 
     public final float getRawVersion(){
         return version;
     }
 
     public final short getBuildNumber(){
         return build;
     }
 
     public final String getBuildTime(){
         return buildTime;
     }
 
     public final VersionChecker getVersionChecker(){
         return vc;
     }
 }
