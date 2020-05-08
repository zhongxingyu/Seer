 package mods.anotherWorld.common.machines;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraftforge.liquids.LiquidDictionary;
 import net.minecraftforge.liquids.LiquidStack;
 
 public class Machines {
 	public static LiquidStack triCSLiquid;
 	
 	
 	public static Block TriCSFlow;
 	public static String BlockTriCSFlowName = "Trichlorosilane Flowing";
 	public static int BlockTriCSFlowID = 3001;
 	
 	public static Block TriCSStill;
 	public static String BlockTriCSStillName = "Trichlorosilane Still";
 	public static int BlockTriCSStillID = BlockTriCSFlowID + 1;
 	
 	public static Block EngineeringGlass;
 	public static String BlockEngineeringGlassName = "Engineering Glass";
 	public static int BlockEngineeringGlassID = 3000;
 	
 	public static Block Separator;
	public static String SeparatorName = "Engineering Glass";
 	public static int SeparatorID = 3005;
 	
 	public static Block SpaceChest;
 	public static String SpaceChestName = "Space Chest";
 	public static int SpaceChestID = 3006;
 	
 	public static void addItems() {
 		EngineeringGlass = new BlockEngineeringGlass(BlockEngineeringGlassID, Material.glass);
 		Separator = new BlockSeparator(SeparatorID);
 		TriCSStill = new BlockTriCSStill(BlockTriCSStillID);
 		TriCSFlow = new BlockTriCSFlow(BlockTriCSFlowID);
 		triCSLiquid = LiquidDictionary.getOrCreateLiquid("Trichlorosilane", new LiquidStack(TriCSStill, 1));
 		SpaceChest = new BlockSpaceChest(SpaceChestID, 0);
 
 	}
 	
 }
