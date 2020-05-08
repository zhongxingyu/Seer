 package mods.cc.rock.block;
 
 import mods.cc.rock.CookingCraft;
 import mods.cc.rock.lib.BlockIDs;
 import mods.cc.rock.lib.Textures;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraftforge.common.MinecraftForge;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class ModBlocks
 {
     
     public static Block machineCoreOn;
     public static Block machineCoreOff;
 	public static Block kitchenTile;
 	public static Block aluminumOre;
 	public static Block fridge;
 	
 	public static void initBlocks()
 	{
 		
 		kitchenTile = new BlockCC(BlockIDs.ID_KITCHEN_TILE, Material.rock, Textures.TEX_KITCHEN_TILE).setHardness(1F).setResistance(2F).setCreativeTab(CookingCraft.tabCC).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("kitchenTile");
		aluminumOre = new BlockCC(BlockIDs.ID_ALUMINUM_ORE, Material.rock, Textures.TEX_ALUMINUM_ORE).setHardness(3F).setResistance(5F).setCreativeTab(CookingCraft.tabCC).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreAluminum");
 		machineCoreOff = new BlockMachineCore(BlockIDs.ID_MACHINE_CORE, false).setCreativeTab(CookingCraft.tabCC).setUnlocalizedName("machineCoreOff");
 		machineCoreOn = new BlockMachineCore(BlockIDs.ID_MACHINE_CORE_ON, true).setUnlocalizedName("machineCoreOn").setLightValue(0.9F);
 		fridge = new BlockRefridgerator(704).setUnlocalizedName("frige").setCreativeTab(CookingCraft.tabCC);
 		
 		harvests();
 		registry();
 	}
 	
 	private static void harvests()
 	{
 	    //Set Block Harvest Levels
 	    MinecraftForge.setBlockHarvestLevel(kitchenTile, "pickaxe", 0);
 	    MinecraftForge.setBlockHarvestLevel(aluminumOre, "pickaxe", 2);
 	    MinecraftForge.setBlockHarvestLevel(machineCoreOff, "pickaxe", 1);
 	    MinecraftForge.setBlockHarvestLevel(machineCoreOff, "pickaxe", 1);
 	    MinecraftForge.setBlockHarvestLevel(fridge, "pickaxe", 1);
 	}
 	
 	private static void registry()
 	{
 		//Register Blocks
 		GameRegistry.registerBlock(kitchenTile, kitchenTile.getUnlocalizedName());
 		GameRegistry.registerBlock(aluminumOre, aluminumOre.getUnlocalizedName());
 		GameRegistry.registerBlock(machineCoreOff, machineCoreOff.getUnlocalizedName());
 		GameRegistry.registerBlock(machineCoreOn, machineCoreOn.getUnlocalizedName());
 		GameRegistry.registerBlock(fridge, fridge.getUnlocalizedName());
 	}
 	
 }
