 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mahn42.anhalter42.building;
 
 import com.mahn42.framework.BuildingDescription;
 import com.mahn42.framework.BuildingDetector;
 import com.mahn42.framework.Framework;
 import com.mahn42.framework.WorldDBList;
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.material.MaterialData;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.Vector;
 
 /**
  *
  * @author andre
  */
 public class BuildingPlugin extends JavaPlugin {
 
     public static BuildingPlugin plugin;
     
     public WorldDBList<SimpleBuildingDB> SimpleDBs;
     public WorldDBList<SendReceiveDB> SendReceiveDBs;
     public WorldDBList<LandmarkDB> LandmarkDBs;
     
     protected DynMapLandmarkRenderer fDynMapTask;
 
     public static void main(String[] args) {
     }
 
     public DynMapLandmarkRenderer getDynmapTask() {
         return fDynMapTask;
     }
     
     @Override
     public void onEnable() {
         plugin = this;
         getServer().getPluginManager().registerEvents(new BuildingListener(), this);
         SimpleDBs = new WorldDBList<SimpleBuildingDB>(SimpleBuildingDB.class, this);
         SendReceiveDBs = new WorldDBList<SendReceiveDB>(SendReceiveDB.class, "SendReceive", this);
         LandmarkDBs = new WorldDBList<LandmarkDB>(LandmarkDB.class, "Landmark", this);
         Framework.plugin.registerSaver(SimpleDBs);
         Framework.plugin.registerSaver(SendReceiveDBs);
         Framework.plugin.registerSaver(LandmarkDBs);
         Framework.plugin.registerMarkerStorage(new MarkerStorage());
 
         getCommand("bd_landmark_list").setExecutor(new CommandLandmarkList());
         
         fDynMapTask = new DynMapLandmarkRenderer();
         getServer().getScheduler().scheduleSyncRepeatingTask(this, fDynMapTask, 100, 20);
         
         //ItemStack lItemStack = new ItemStack(Material.SMOOTH_BRICK, 4);//, (short)0, (byte)3);
         //lItemStack.setData(new MaterialData(Material.SMOOTH_BRICK, (byte)3));
         ItemStack lItemStack = new ItemStack(Material.SMOOTH_BRICK, 4, (short)0, (byte)3);
         ShapedRecipe lShapeRecipe = new ShapedRecipe(lItemStack);
         lShapeRecipe.shape("AA", "AA");
         lShapeRecipe.setIngredient('A', Material.SMOOTH_BRICK);
         getServer().addRecipe(lShapeRecipe);
 
         lItemStack = new ItemStack(Material.SMOOTH_BRICK, 4); //, (short)0, (byte)0);
         ShapedRecipe lChiseledStoneBrick = new ShapedRecipe(lItemStack);
         lChiseledStoneBrick.shape("AA", "AA");
         lChiseledStoneBrick.setIngredient('A', new MaterialData(Material.SMOOTH_BRICK, (byte)3));
         getServer().addRecipe(lChiseledStoneBrick);
 
         //lItemStack = new ItemStack(Material.SNOW_BLOCK, 6); //, (short)0, (byte)0);
         //lItemStack.setData(new MaterialData(Material.SNOW_BLOCK, (byte)3));
         //lItemStack = new ItemStack(Material.SNOW, 6, (short)0, (byte)3);
         //ShapedRecipe lHalfSnow = new ShapedRecipe(lItemStack);
         //lHalfSnow.shape("AAA");
         //lHalfSnow.setIngredient('A', new MaterialData(Material.SNOW_BLOCK, (byte)0));
         //getServer().addRecipe(lHalfSnow);
         
         SimpleBuildingHandler lHandler = new SimpleBuildingHandler(this);
         SendReceiveHandler lSRHandler = new SendReceiveHandler(this);
         BuildingDetector lDetector = Framework.plugin.getBuildingDetector();
         BuildingDescription lDesc;
         BuildingDescription.BlockDescription lBDesc;
         BuildingDescription.RelatedTo lRel;
         
         lDesc = lDetector.newDescription("Building.BuildingEntryDetector");
         lDesc.typeName = "Building Enter/Leave Detector";
         lDesc.handler = lHandler;
         lDesc.circleRadius = 1;
         lDesc.color = 0xC818CB;
         lBDesc = lDesc.newBlockDescription("base");
         lBDesc.materials.add(Material.SMOOTH_BRICK, (byte)3);
         lBDesc.detectSensible = true;
         lRel = lBDesc.newRelatedTo("lever", BuildingDescription.RelatedPosition.Nearby, 1);
         lRel = lBDesc.newRelatedTo("sign", BuildingDescription.RelatedPosition.Nearby, 1);
         lBDesc = lDesc.newBlockDescription("lever");
         lBDesc.materials.add(Material.LEVER);
         lBDesc = lDesc.newBlockDescription("sign");
         lBDesc.materials.add(Material.SIGN);
         lBDesc.materials.add(Material.SIGN_POST);
         lBDesc.materials.add(Material.WALL_SIGN);
         lDesc.activate();
    
         lDesc = lDetector.newDescription("Building.RedStoneReceiver");
         lDesc.typeName = "Building for receiving redstone signals";
         lDesc.handler = lSRHandler;
         lDesc.circleRadius = 1;
         lDesc.color = 0x18CB18;
         lBDesc = lDesc.newBlockDescription("base");
         lBDesc.materials.add(Material.SMOOTH_BRICK, (byte)3);
         lBDesc.detectSensible = true;
         lRel = lBDesc.newRelatedTo(new Vector(0, 1, 0), "antenabase");
         lRel = lBDesc.newRelatedTo("lever", BuildingDescription.RelatedPosition.Nearby, 1);
         lRel = lBDesc.newRelatedTo("sign", BuildingDescription.RelatedPosition.Nearby, 1);
         lBDesc = lDesc.newBlockDescription("lever");
         lBDesc.materials.add(Material.LEVER);
         lBDesc = lDesc.newBlockDescription("sign");
         lBDesc.materials.add(Material.SIGN);
         lBDesc.materials.add(Material.SIGN_POST);
         lBDesc.materials.add(Material.WALL_SIGN);
         lBDesc = lDesc.newBlockDescription("antenabase");
         lBDesc.materials.add(Material.FENCE);
         lRel = lBDesc.newRelatedTo(new Vector(0, 10, 0), "antenatop");
         lRel.materials.add(Material.FENCE);
         lRel.minDistance = 1;
         lBDesc = lDesc.newBlockDescription("antenatop");
         lBDesc.materials.add(Material.FENCE);
         lDesc.activate();
    
         lDesc = lDetector.newDescription("Building.RedStoneReceiver.Lamp");
         lDesc.typeName = "Lamp for receiving redstone signals";
         lDesc.handler = lSRHandler;
         lDesc.circleRadius = 1;
         lDesc.color = 0x88CB18;
         lBDesc = lDesc.newBlockDescription("base");
         lBDesc.materials.add(Material.REDSTONE_LAMP_ON);
         lBDesc.materials.add(Material.REDSTONE_LAMP_OFF);
         lBDesc.detectSensible = true;
         lRel = lBDesc.newRelatedTo("lever", BuildingDescription.RelatedPosition.Nearby, 1);
         lRel = lBDesc.newRelatedTo("sign", BuildingDescription.RelatedPosition.Nearby, 1);
         lBDesc = lDesc.newBlockDescription("lever");
         lBDesc.materials.add(Material.LEVER);
         lBDesc = lDesc.newBlockDescription("sign");
         lBDesc.materials.add(Material.SIGN);
         lBDesc.materials.add(Material.SIGN_POST);
         lBDesc.materials.add(Material.WALL_SIGN);
         lDesc.activate();
    
         lDesc = lDetector.newDescription("Building.RedStoneSender");
         lDesc.typeName = "Building for sending redstone signals";
         lDesc.handler = lSRHandler;
         lDesc.circleRadius = 1;
         lDesc.color = 0xCB6918;
         lBDesc = lDesc.newBlockDescription("base");
         lBDesc.materials.add(Material.SMOOTH_BRICK, (byte)3);
         lBDesc.detectSensible = true;
         lBDesc.redstoneSensible = true;
         lRel = lBDesc.newRelatedTo(new Vector(0, 1, 0), "antenabase");
         lRel = lBDesc.newRelatedTo("sign", BuildingDescription.RelatedPosition.Nearby, 1);
         lBDesc = lDesc.newBlockDescription("sign");
         lBDesc.materials.add(Material.SIGN);
         lBDesc.materials.add(Material.SIGN_POST);
         lBDesc.materials.add(Material.WALL_SIGN);
         lBDesc = lDesc.newBlockDescription("antenabase");
         lBDesc.materials.add(Material.FENCE);
         lRel = lBDesc.newRelatedTo(new Vector(0, 10, 0), "antenatop");
         lRel.materials.add(Material.FENCE);
         lRel.minDistance = 1;
         lBDesc = lDesc.newBlockDescription("antenatop");
         lBDesc.materials.add(Material.FENCE);
         lDesc.activate();
    
         lDesc = lDetector.newDescription("Building.Pyramid.Sandstone");
         lDesc.handler = lHandler;
         lDesc.typeName = "Pyramid";
         lBDesc = lDesc.newBlockDescription("top");
         lBDesc.materials.add(Material.SANDSTONE);
         lBDesc.detectSensible = true;
         lBDesc.nameSensible = true;
         lRel = lBDesc.newRelatedTo(new Vector( 80,-80, 80), "ground1");
         lRel.materials.add(Material.SANDSTONE);
         lRel.minDistance = 1;
         lRel = lBDesc.newRelatedTo(new Vector( 80,-80,-80), "ground2");
         lRel.materials.add(Material.SANDSTONE);
         lRel.minDistance = 1;
         lRel = lBDesc.newRelatedTo(new Vector(-80,-80, 80), "ground3");
         lRel.materials.add(Material.SANDSTONE);
         lRel.minDistance = 1;
         lRel = lBDesc.newRelatedTo(new Vector(-80,-80,-80), "ground4");
         lRel.materials.add(Material.SANDSTONE);
         lRel.minDistance = 1;
         lBDesc = lDesc.newBlockDescription("ground1");
         lBDesc.materials.add(Material.SANDSTONE);
         lBDesc = lDesc.newBlockDescription("ground2");
         lBDesc.materials.add(Material.SANDSTONE);
         lBDesc = lDesc.newBlockDescription("ground3");
         lBDesc.materials.add(Material.SANDSTONE);
         lBDesc = lDesc.newBlockDescription("ground4");
         lBDesc.materials.add(Material.SANDSTONE);
         lDesc.activate();
         
         lDesc = lDetector.newDescription("Building.BoatRailStation");
         lDesc.handler = lHandler;
         lDesc.typeName = "BoatRailStation";
         lDesc.iconName = "anchor";
         lBDesc = lDesc.newBlockDescription("railblock");
         lBDesc.materials.add(Material.COBBLESTONE_STAIRS);
         lBDesc.materials.add(Material.SMOOTH_STAIRS);
         lBDesc.materials.add(Material.WOOD_STAIRS);
         lBDesc.materials.add(Material.BRICK_STAIRS);
         lBDesc.materials.add(Material.NETHER_BRICK_STAIRS);
         lBDesc.materials.add(Material.SANDSTONE_STAIRS);
         lBDesc.materials.add(Material.SPRUCE_WOOD_STAIRS);
         lBDesc.materials.add(Material.BIRCH_WOOD_STAIRS);
         lBDesc.materials.add(Material.JUNGLE_WOOD_STAIRS);
         lBDesc.detectSensible = true;
         lBDesc.nameSensible = true;
         lRel = lBDesc.newRelatedTo(new Vector(0, 0, 1), "rails");
         lRel = lBDesc.newRelatedTo(new Vector(0,-1,-1), "water");
         lBDesc = lDesc.newBlockDescription("rails");
         lBDesc.materials.add(Material.RAILS);
         lBDesc.materials.add(Material.POWERED_RAIL);
         lBDesc.materials.add(Material.DETECTOR_RAIL);
         lBDesc = lDesc.newBlockDescription("water");
         lBDesc.materials.add(Material.STATIONARY_WATER);
         lBDesc.materials.add(Material.WATER);
         lRel = lBDesc.newRelatedTo(new Vector(0, 0,-1), "water2");
         lBDesc = lDesc.newBlockDescription("water2");
         lBDesc.materials.add(Material.STATIONARY_WATER);
         lBDesc.materials.add(Material.WATER);
         lRel = lBDesc.newRelatedTo(new Vector(1, 0, 0), "water3");
         lRel = lBDesc.newRelatedTo(new Vector(-1, 0, 0), "water4");
         lBDesc = lDesc.newBlockDescription("water3");
         lBDesc.materials.add(Material.STATIONARY_WATER);
         lBDesc.materials.add(Material.WATER);
         lBDesc = lDesc.newBlockDescription("water4");
         lBDesc.materials.add(Material.STATIONARY_WATER);
         lBDesc.materials.add(Material.WATER);
         lDesc.createAndActivateXZ(true);
         
         lDesc = lDetector.newDescription("Building.Lodge");
         BuildingDescription.BlockMaterialArray lMats = lDesc.newBlockMaterialArray();
         lMats.add(Material.SMOOTH_BRICK);
         lMats.add(Material.BRICK);
         lMats.add(Material.WOOD);
         lMats.add(Material.STONE);
         lMats.add(Material.COBBLESTONE);
         lMats.add(Material.SANDSTONE);
         lMats.add(Material.BEDROCK);
         lMats.add(Material.OBSIDIAN);
         lMats.add(Material.WOOD_PLATE);
         lMats.add(Material.NETHERRACK);
         lMats.add(Material.NETHER_BRICK);
         lMats.add(Material.IRON_BLOCK);
         lMats.add(Material.GOLD_BLOCK);
         lMats.add(Material.DIAMOND_BLOCK);
         lMats.add(Material.EMERALD_BLOCK);
         lDesc.handler = lHandler;
         lDesc.typeName = "Lodge";
         lBDesc = lDesc.newBlockDescription("ground_e1");
         lBDesc.materials.add(lMats);
         lBDesc.detectSensible = true;
         lRel = lBDesc.newRelatedTo(new Vector( 1, 0, 0), "ground_e1x");
         lRel = lBDesc.newRelatedTo(new Vector( 0, 1, 0), "ground_e1y");
         lRel = lBDesc.newRelatedTo(new Vector( 0, 0, 1), "ground_e1z");
         lRel = lBDesc.newRelatedTo(new Vector(20, 0,20), "ground_e3");
         lRel.minDistance = 3;
         lRel = lBDesc.newRelatedTo(new Vector(20, 0, 0), "ground_e2");
         lRel.minDistance = 3;
         lRel = lBDesc.newRelatedTo(new Vector( 0, 0,20), "ground_e4");
         lRel.minDistance = 3;
         lRel = lBDesc.newRelatedTo(new Vector( 0, 8, 0), "top_e1");
         //lRel = lBDesc.newRelatedTo(new Vector(19, 0, 0), "door");
         lBDesc = lDesc.newBlockDescription("ground_e2");
         lBDesc.materials.add(lMats);
         lRel = lBDesc.newRelatedTo(new Vector(-1, 0, 0), "ground_e2x");
         lRel = lBDesc.newRelatedTo(new Vector( 0, 1, 0), "ground_e2y");
         lRel = lBDesc.newRelatedTo(new Vector( 0, 0, 1), "ground_e2z");
         lRel = lBDesc.newRelatedTo(new Vector( 0, 8, 0), "top_e2");
         lBDesc = lDesc.newBlockDescription("ground_e3");
         lBDesc.materials.add(lMats);
         lRel = lBDesc.newRelatedTo(new Vector(-1, 0, 0), "ground_e3x");
         lRel = lBDesc.newRelatedTo(new Vector( 0, 1, 0), "ground_e3y");
         lRel = lBDesc.newRelatedTo(new Vector( 0, 0,-1), "ground_e3z");
         lRel = lBDesc.newRelatedTo(new Vector( 0, 8, 0), "top_e3");
         lBDesc = lDesc.newBlockDescription("ground_e4");
         lBDesc.materials.add(lMats);
         lRel = lBDesc.newRelatedTo(new Vector( 1, 0, 0), "ground_e4x");
         lRel = lBDesc.newRelatedTo(new Vector( 0, 1, 0), "ground_e4y");
         lRel = lBDesc.newRelatedTo(new Vector( 0, 0,-1), "ground_e4z");
         lRel = lBDesc.newRelatedTo(new Vector( 0, 8, 0), "top_e4");
         lBDesc = lDesc.newBlockDescription("ground_e1x");
         lBDesc.materials.add(lMats);
         lBDesc = lDesc.newBlockDescription("ground_e1y");
         lBDesc.materials.add(lMats);
         lBDesc = lDesc.newBlockDescription("ground_e1z");
         lBDesc.materials.add(lMats);
         lBDesc = lDesc.newBlockDescription("ground_e2x");
         lBDesc.materials.add(lMats);
         lBDesc = lDesc.newBlockDescription("ground_e2y");
         lBDesc.materials.add(lMats);
         lBDesc = lDesc.newBlockDescription("ground_e2z");
         lBDesc.materials.add(lMats);
         lBDesc = lDesc.newBlockDescription("ground_e3x");
         lBDesc.materials.add(lMats);
         lBDesc = lDesc.newBlockDescription("ground_e3y");
         lBDesc.materials.add(lMats);
         lBDesc = lDesc.newBlockDescription("ground_e3z");
         lBDesc.materials.add(lMats);
         lBDesc = lDesc.newBlockDescription("ground_e4x");
         lBDesc.materials.add(lMats);
         lBDesc = lDesc.newBlockDescription("ground_e4y");
         lBDesc.materials.add(lMats);
         lBDesc = lDesc.newBlockDescription("ground_e4z");
         lBDesc.materials.add(lMats);
         lBDesc = lDesc.newBlockDescription("top_e1");
         lBDesc.materials.add(lMats);
         lBDesc = lDesc.newBlockDescription("top_e2");
         lBDesc.materials.add(lMats);
         lBDesc = lDesc.newBlockDescription("top_e3");
         lBDesc.materials.add(lMats);
         lBDesc = lDesc.newBlockDescription("top_e4");
         lBDesc.materials.add(lMats);
         //lBDesc = lDesc.newBlockDescription("door");
         //lBDesc.materials.add(Material.WOODEN_DOOR);
         //lRel = lBDesc.newRelatedTo(new Vector( 0, 1, 0), "door_top");
         //lBDesc = lDesc.newBlockDescription("door_top");
         //lBDesc.materials.add(Material.WOODEN_DOOR);
         //lDesc.createAndActivateXZ();
 
         lDesc = lDetector.newDescription("Building.Lift");
         lDesc.handler = lHandler;
         lDesc.typeName = "Lift";
         lBDesc = lDesc.newBlockDescription("bottomleftfront");
         lBDesc.materials.add(Material.IRON_BLOCK);
         lBDesc.detectSensible = true;
         lRel = lBDesc.newRelatedTo(new Vector( 0, 0,10), "bottomleftback");
         lRel = lBDesc.newRelatedTo(new Vector(10, 0, 0), "bottomrightfront");
         lRel = lBDesc.newRelatedTo(new Vector(10, 0,10), "bottomrightback");
         lRel = lBDesc.newRelatedTo(new Vector( 0,10, 0), "topleftfront");
         lRel = lBDesc.newRelatedTo(new Vector( 0,10,10), "topleftback");
         lRel = lBDesc.newRelatedTo(new Vector(10,10, 0), "toprightfront");
         lRel = lBDesc.newRelatedTo(new Vector(10,10,10), "toprightback");
         lBDesc = lDesc.newBlockDescription("bottomleftback");
         lBDesc.materials.add(Material.IRON_BLOCK);
         lBDesc = lDesc.newBlockDescription("bottomrightfront");
         lBDesc.materials.add(Material.IRON_BLOCK);
         lBDesc = lDesc.newBlockDescription("bottomrightback");
         lBDesc.materials.add(Material.IRON_BLOCK);
         lBDesc = lDesc.newBlockDescription("topleftfront");
         lBDesc.materials.add(Material.IRON_BLOCK);
         lBDesc = lDesc.newBlockDescription("topleftback");
         lBDesc.materials.add(Material.IRON_BLOCK);
         lBDesc = lDesc.newBlockDescription("toprightfront");
         lBDesc.materials.add(Material.IRON_BLOCK);
         lBDesc = lDesc.newBlockDescription("toprightback");
         lBDesc.materials.add(Material.IRON_BLOCK);
         //lDesc.activate();
 
         lDesc = lDetector.newDescription("Building.Landmark");
         lDesc.handler = lHandler;
         lDesc.typeName = "Landmark";
         lDesc.circleRadius = 1;
         lDesc.visibleOnMap = false;
         lBDesc = lDesc.newBlockDescription("bottom");
         lBDesc.materials.add(Material.SMOOTH_BRICK, (byte)3);
         lBDesc.detectSensible = true;
         lRel = lBDesc.newRelatedTo(new Vector( 0, 2, 0), "top");
         lRel.materials.add(Material.SMOOTH_BRICK, (byte)3);
         lBDesc = lDesc.newBlockDescription("top");
         lBDesc.materials.add(Material.SMOOTH_BRICK, (byte)3);
         lBDesc.nameSensible = true;
         lRel = lBDesc.newRelatedTo("sign", BuildingDescription.RelatedPosition.Nearby, 1);
         lBDesc = lDesc.newBlockDescription("sign");
         lBDesc.materials.add(Material.SIGN);
         lBDesc.materials.add(Material.SIGN_POST);
         lBDesc.materials.add(Material.WALL_SIGN);
         lDesc.activate();
         
         lDesc = lDetector.newDescription("Building.Portal");
         lDesc.handler = lHandler;
         lDesc.typeName = "Portal";
         lDesc.iconName = "portal";
         lBDesc = lDesc.newBlockDescription("bottomleft");
         lBDesc.materials.add(Material.OBSIDIAN);
         lBDesc.detectSensible = true;
         lBDesc.nameSensible = true;
         lRel = lBDesc.newRelatedTo("sign", BuildingDescription.RelatedPosition.Nearby, 1);
         lRel = lBDesc.newRelatedTo(new Vector( 3, 0, 0), "bottomright");
         lRel.materials.add(Material.AIR);
         lRel.minDistance = 2;
         lRel = lBDesc.newRelatedTo(new Vector( 0, 4, 0), "topleft");
         lRel.materials.add(Material.OBSIDIAN);
         lRel.minDistance = 2;
         lBDesc = lDesc.newBlockDescription("bottomright");
         lBDesc.materials.add(Material.OBSIDIAN);
         lRel = lBDesc.newRelatedTo(new Vector( 0, 4, 0), "topright");
         lRel.materials.add(Material.OBSIDIAN);
         lRel.minDistance = 2;
         lBDesc = lDesc.newBlockDescription("topleft");
         lBDesc.materials.add(Material.GLOWSTONE);
         lRel = lBDesc.newRelatedTo(new Vector( 3, 0, 0), "topright");
         lRel.materials.add(Material.OBSIDIAN);
         lRel.minDistance = 2;
         lBDesc = lDesc.newBlockDescription("topright");
         lBDesc.materials.add(Material.GLOWSTONE);
         lBDesc = lDesc.newBlockDescription("sign");
         lBDesc.materials.add(Material.SIGN);
         lBDesc.materials.add(Material.SIGN_POST);
         lBDesc.materials.add(Material.WALL_SIGN);
         lDesc.createAndActivateXZ();
         
         lDesc = lDetector.newDescription("Building.Monument.Creeper");
         lDesc.handler = lHandler;
         lDesc.typeName = "Creeper Monument";
         //lDesc.iconName = "tower";
         lDesc.circleRadius = 100;
         lDesc.influenceRadiusFactor = 100.0;
         lDesc.color = 0x80FF80;
         lBDesc = lDesc.newBlockDescription("top");
         lBDesc.materials.add(Material.GOLD_BLOCK);
         lBDesc.detectSensible = true;
         lBDesc.nameSensible = true;
         lRel = lBDesc.newRelatedTo(new Vector( 2,-2, 2), "e11");
         lRel.materials.add(Material.NETHERRACK);
         lRel = lBDesc.newRelatedTo(new Vector(-2,-2, 2), "e12");
         lRel.materials.add(Material.NETHERRACK);
         lRel = lBDesc.newRelatedTo(new Vector(-2,-2,-2), "e13");
         lRel.materials.add(Material.NETHERRACK);
         lRel = lBDesc.newRelatedTo(new Vector( 2,-2,-2), "e14");
         lRel.materials.add(Material.NETHERRACK);
         lBDesc = lDesc.newBlockDescription("e11");
         lBDesc.materials.add(Material.NETHERRACK);
         lBDesc = lDesc.newBlockDescription("e12");
         lBDesc.materials.add(Material.NETHERRACK);
         lBDesc = lDesc.newBlockDescription("e13");
         lBDesc.materials.add(Material.NETHERRACK);
         lBDesc = lDesc.newBlockDescription("e14");
         lBDesc.materials.add(Material.NETHERRACK);
         lDesc.activate();
     }
 
     @Override
     public void onDisable() {
         getServer().getScheduler().cancelTasks(this);
         plugin = null;
     }
 
 }
