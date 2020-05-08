 package compactMobs;
 
 import java.io.File;
 import java.util.logging.Logger;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.common.Property;
 import buildcraft.core.network.PacketHandler;
 
 import cpw.mods.fml.common.Mod.PostInit;
 
 import compactMobs.Blocks.BlockBreeder;
 import compactMobs.Blocks.BlockCatalyst;
 import compactMobs.Blocks.BlockCompactor;
 import compactMobs.Blocks.BlockDecompactor;
 import compactMobs.Blocks.BlockIncubator;
 import compactMobs.Items.CompactMobsItems;
 import compactMobs.TileEntity.TileEntityBreeder;
 import compactMobs.TileEntity.TileEntityCatalyst;
 import compactMobs.TileEntity.TileEntityCompactor;
 import compactMobs.TileEntity.TileEntityDecompactor;
 import compactMobs.TileEntity.TileEntityIncubator;
 
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @NetworkMod(clientSideRequired = true, serverSideRequired = true, channels = { "CMC" }, packetHandler = PacketHandler.class)
@Mod(modid = "CM", name = "CompactMobs", version = "1.2.4", dependencies = "required-after:BuildCraft|Transport;required-after:BuildCraft|Builders;required-after:BuildCraft|Silicon;required-after:BuildCraft|Core;")
 public class CompactMobsCore {
 
     @Instance
     public static CompactMobsCore instance;
     //hardcoded id of block
     public static int BlockID = 3391;
     public boolean useFullTagCompound = true;
     public static Block blocks;
     public static Block blockCompactor;
     public static Block blockDecompactor;
     public static Block blockBreeder;
     public static Block blockIncubator;
     public static Block blockCatalyst;
     public boolean tick;
     public static Configuration mainConfig;
     public static Logger cmLog = Logger.getLogger("CompactMobs");
     public Property compatorId;// = CompactMobsCore.mainConfig.getOrCreateBlockIdProperty("CompactorId", 3391);
     public Property decompatorId;// = CompactMobsCore.mainConfig.getOrCreateBlockIdProperty("DecompactorId", 3392);
     public Property breederId;// = CompactMobsCore.mainConfig.getOrCreateBlockIdProperty("BreederId", 3393);
     public Property incubatorId;// = CompactMobsCore.mainConfig.getOrCreateBlockIdProperty("IncubatorId", 3394);
     public Property emptyMobHolderId;// = CompactMobsCore.mainConfig.getOrCreateBlockIdProperty("EmptyMobHolderId", 3395);
     public Property fullMobHolderId;
     public Property handCompactorId;
     public Property handDecompactorId;
     public Property catalystId;
     public Property catalystCoreId;
     @SidedProxy(clientSide = "compactMobs.ClientProxyCompactMobs", serverSide = "compactMobs.CommonProxyCompactMobs")
     public static CommonProxyCompactMobs proxy;
 
     @PreInit
     public void loadConfiguration(FMLPreInitializationEvent evt) {
         cmLog.setParent(FMLLog.getLogger());
        cmLog.info("Starting CompactMobs v1.2.4");
         tick = false;
         
         mainConfig = new Configuration(new File(evt.getModConfigurationDirectory(), "CompactMobs.cfg"));
         try {
             mainConfig.load();
             compatorId = CompactMobsCore.mainConfig.get("block", "CompactorId", 3391);
             decompatorId = CompactMobsCore.mainConfig.get("block", "DecompactorId", 3392);
             breederId = CompactMobsCore.mainConfig.get("block", "BreederId", 3393);
             incubatorId = CompactMobsCore.mainConfig.get("block", "IncubatorId", 3394);
             catalystId = CompactMobsCore.mainConfig.get("block", "CatalystBlockId", 3399);
             emptyMobHolderId = CompactMobsCore.mainConfig.get("item", "EmptyMobHolderId", 3395);
             fullMobHolderId = CompactMobsCore.mainConfig.get("item", "FullMobHolderId", 3396);
             handCompactorId = CompactMobsCore.mainConfig.get("item", "HandheldCompactorId", 3397);
             handDecompactorId = CompactMobsCore.mainConfig.get("item", "HandheldDecompactorId", 3398);
             catalystCoreId = CompactMobsCore.mainConfig.get("item", "CatalystCoreId", 3400);
 
         } finally {
             mainConfig.save();
         }
     }
 
     @Init
     public void load(FMLInitializationEvent event) {
 
         CompactMobsItems.createInstance();
         NetworkRegistry.instance().registerGuiHandler(this, this.proxy);
 
         blockCompactor = new BlockCompactor(compatorId.getInt(), Material.iron).setStepSound(Block.soundMetalFootstep).setHardness(3F).setResistance(1.0F).setBlockName("blockCompactor");
         blockDecompactor = new BlockDecompactor(decompatorId.getInt(), Material.iron).setStepSound(Block.soundMetalFootstep).setHardness(3F).setResistance(1.0F).setBlockName("blockDecompactor");
         blockBreeder = new BlockBreeder(breederId.getInt(), Material.iron).setStepSound(Block.soundMetalFootstep).setHardness(3F).setResistance(1.0F).setBlockName("blockBreeder");
         blockIncubator = new BlockIncubator(incubatorId.getInt(), Material.iron).setStepSound(Block.soundMetalFootstep).setHardness(3F).setResistance(1.0F).setBlockName("blockIncubator");
         blockCatalyst = new BlockCatalyst(catalystId.getInt(), Material.iron).setStepSound(Block.soundMetalFootstep).setHardness(3F).setResistance(1.0F).setBlockName("blockCatalyst");
         
         GameRegistry.registerBlock(blockCompactor, "Compactor");
         LanguageRegistry.addName(blockCompactor, "Compactor");
         
         
         GameRegistry.registerBlock(blockDecompactor, "Decompactor");
         LanguageRegistry.addName(blockDecompactor, "Decompactor");
         
         GameRegistry.registerBlock(blockBreeder, "Breeder");
         LanguageRegistry.addName(blockBreeder, "Breeder");
         
         GameRegistry.registerBlock(blockIncubator, "Incubator");
         LanguageRegistry.addName(blockIncubator, "Incubator");
         
         GameRegistry.registerBlock(blockCatalyst, "Catalyst");
         LanguageRegistry.addName(blockCatalyst, "Catalyst");
 
         GameRegistry.registerTileEntity(TileEntityCompactor.class, "tileEntityCompactor");
         GameRegistry.registerTileEntity(TileEntityDecompactor.class, "tileEntityDecompactor");
         GameRegistry.registerTileEntity(TileEntityBreeder.class, "tileEntityBreeder");
         GameRegistry.registerTileEntity(TileEntityIncubator.class, "tileEntityIncubator");
         GameRegistry.registerTileEntity(TileEntityCatalyst.class, "tileEntityCatalyst");
 
         CompactMobsItems.getInstance().instantiateItems();
         CompactMobsItems.getInstance().nameItems();
         
         MinecraftForge.EVENT_BUS.register(new CompactMobsEventHandler());
 
 
 
 
     }
 
     @PostInit
     //public void initialize(FMLInitializationEvent evt) {
     public void PostLoad(FMLPostInitializationEvent event) {
     	Item ironGear = Item.ingotIron;
         Item goldGear = Item.ingotGold;
         Item diamondGear = Item.diamond;
 
         try {
             cmLog.info("Adding Compactmob Recipes");
             ironGear = (Item) Class.forName("buildcraft.BuildCraftCore").getField("ironGearItem").get(null);
             goldGear = (Item) Class.forName("buildcraft.BuildCraftCore").getField("goldGearItem").get(null);
             diamondGear = (Item) Class.forName("buildcraft.BuildCraftCore").getField("diamondGearItem").get(null);
             GameRegistry.addRecipe(new ItemStack(blockCompactor, 1), new Object[]{"ipi", "lol", "gpg", 'i', ironGear, 'p', Block.pistonBase, 'l', new ItemStack(Item.dyePowder, 1, 4), 'o', Block.obsidian, 'g', goldGear});
             GameRegistry.addRecipe(new ItemStack(blockDecompactor, 1), new Object[]{"oro", "ild", "grg", 'o', ironGear, 'r', Item.redstone, 'i', Item.ingotIron, 'l', Block.blockLapis, 'd', Block.dispenser, 'g', goldGear});
             GameRegistry.addRecipe(new ItemStack(CompactMobsItems.mobHolder, 2), new Object[]{"hhh", "ibi", "sss", 'h', new ItemStack(Block.stoneSingleSlab, 1, 0), 'i', Item.ingotIron, 'b', Block.fenceIron, 's', Block.stone});
             GameRegistry.addRecipe(new ItemStack(blockBreeder, 1), new Object[]{"oho", "iwi", "gag", 'o', ironGear, 'h', CompactMobsItems.mobHolder, 'i', Item.ingotIron, 'w', Item.wheat, 'g', goldGear, 'a', new ItemStack(Item.appleGold, 1, 0)});
             GameRegistry.addRecipe(new ItemStack(blockIncubator, 1), new Object[]{"oco", "ifi", "gbg", 'o', ironGear, 'c', Block.chest, 'i', Item.ingotIron, 'f', Block.stoneOvenIdle, 'g', goldGear, 'b', Item.blazePowder});
             GameRegistry.addRecipe(new ItemStack(CompactMobsItems.handCompactor,1), new Object[]{"d t", "rch", "dgr", 'd', new ItemStack(Item.dyePowder, 1, 1), 't', Block.torchRedstoneActive, 'r', Item.redstone, 'c', blockCompactor, 'h', CompactMobsItems.mobHolder, 'g', diamondGear});
             GameRegistry.addRecipe(new ItemStack(CompactMobsItems.handDecompactor,1), new Object[]{"p t", "ldr", " gr", 'p', Block.thinGlass, 't', Block.torchRedstoneActive, 'l', new ItemStack(Item.dyePowder, 1, 4), 'd', blockDecompactor, 'r', Item.redstone, 'g', diamondGear});
             GameRegistry.addRecipe(new ItemStack(blockCatalyst,1), new Object[]{"rdr", "ixi", "gsg", 'r', Item.redstone, 'd', diamondGear, 'i', Item.ingotIron, 'x', blockBreeder.dispenser, 'g', goldGear, 's', new ItemStack(Item.swordSteel, 1, 0)});
             GameRegistry.addRecipe(new ItemStack(CompactMobsItems.catalystCore,1), new Object[]{"idr", "geg", "rdi", 'i', Block.fenceIron, 'd', Item.diamond, 'g', Item.gunpowder, 'e', Item.eyeOfEnder, 'r', Item.redstone});
         
        } catch (Exception ex) {
             	cmLog.info("Generic Buildcraft Item Check Failed, Attempting Direct");
             	/*try {
             		 ironGear = BuildCraftCore.ironGearItem;
                      goldGear = BuildCraftCore.goldGearItem;
                      diamondGear = BuildCraftCore.diamondGearItem;
                      
 	            } catch (Exception exx) {
 	            	cmLog.info("No buildcraft detected at all. Using generic recipes, Warning, Compact Mobs may not work");
 	            }*/
         }
         
 
     }
 
     public String getPriorities() {
         return "after:mod_IC2;after:mod_BuildCraftCore;after:mod_BuildCraftEnergy;after:mod_BuildCraftFactory;after:mod_BuildCraftSilicon;after:mod_BuildCraftTransport;after:mod_RedPowerWorld";
     }
 }
