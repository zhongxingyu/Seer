 package mods.anotherWorld.common;
 
 import mods.anotherWorld.AnotherWorld;
 import mods.anotherWorld.common.basicItems.BasicItems;
 import mods.anotherWorld.common.basicItems.ItemBlueprint;
 import mods.anotherWorld.common.basicItems.ItemBlueprintPart;
 import mods.anotherWorld.common.basicItems.ItemGlue;
 import mods.anotherWorld.common.basicItems.ItemSalt;
 import mods.anotherWorld.common.machines.BlockEngineeringGlass;
 import mods.anotherWorld.common.machines.BlockSeparator;
 import mods.anotherWorld.common.machines.BlockSpaceChest;
 import mods.anotherWorld.common.machines.BlockTriCSFlow;
 import mods.anotherWorld.common.machines.BlockTriCSStill;
 import net.minecraft.block.material.Material;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.Property;
 import net.minecraftforge.liquids.LiquidDictionary;
 import net.minecraftforge.liquids.LiquidStack;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import static mods.anotherWorld.common.basicBlocks.BasicBlocks.*;
 import static mods.anotherWorld.common.basicItems.BasicItems.*;
 import static mods.anotherWorld.common.machines.Machines.*;
 public class Config {
 
 	public static void doReadWrite(FMLPreInitializationEvent event) {
 
         
         AnotherWorld.configAW.load();
         
         //Render IDs
         GlobalIDs.SpaceChestRenderID = AnotherWorld.configAW.get("RenderIDs DONT CHANGE UNLESS YOU KNOW WHAT YOU'RE DOING", "SpaceChest", GlobalIDs.SpaceChestRenderID).getInt();
         GlobalIDs.SeparatorRenderID = AnotherWorld.configAW.get("RenderIDs DONT CHANGE UNLESS YOU KNOW WHAT YOU'RE DOING", "Separator", GlobalIDs.SeparatorRenderID).getInt(); 
         GlobalIDs.SpaceCactusRenderID = AnotherWorld.configAW.get("RenderIDs DONT CHANGE UNLESS YOU KNOW WHAT YOU'RE DOING", "SpaceCactus", GlobalIDs.SpaceCactusRenderID).getInt();    
         
         
         //Tool Items
         ItemRaygunID =				AnotherWorld.configAW.get("Tools", ItemRaygunName, ItemRaygunID).getInt();
         
         //Player Items
         ItemForkkID =				AnotherWorld.configAW.get("Characters", ItemForkkName, ItemForkkID).getInt();
         ItemCadenDonutID =			AnotherWorld.configAW.get("Characters", ItemCadenDonutName, ItemCadenDonutID).getInt();
         
         //Items
          ItemGlueID = 				AnotherWorld.configAW.get("Items", ItemGlueName, ItemGlueID).getInt();
          ItemBlueprintPartID = 		AnotherWorld.configAW.get("Items", ItemBlueprintPartName, ItemBlueprintPartID).getInt();
          ItemBlueprintID =		 	AnotherWorld.configAW.get("Items", ItemBlueprintName, ItemBlueprintID).getInt();
          ItemSiliconChipID =		AnotherWorld.configAW.get("Items", ItemSiliconChipName, ItemSiliconChipID).getInt();
          ItemLEDRedID =				AnotherWorld.configAW.get("Items", ItemLEDRedName, ItemLEDRedID).getInt();
          ItemLEDGreenID = 			AnotherWorld.configAW.get("Items", ItemLEDGreenName, ItemLEDGreenID).getInt();
          ItemLEDBlueID = 			AnotherWorld.configAW.get("Items", ItemLEDBlueName, ItemLEDBlueID).getInt();
          ItemSiliconSlagID = 		AnotherWorld.configAW.get("Items", ItemSiliconSlagName, ItemSiliconSlagID).getInt();
          ItemMetGradeSiliconID =	AnotherWorld.configAW.get("Items", ItemMetGradeSiliconName, ItemMetGradeSiliconID).getInt();
          ItemQuartzCrucibleID = 	AnotherWorld.configAW.get("Items", ItemQuartzCrucibleName, ItemQuartzCrucibleID).getInt();
          ItemSiliconBouleID =		AnotherWorld.configAW.get("Items", ItemSiliconBouleName, ItemSiliconBouleID).getInt();
          ItemSiliconSliceID =		AnotherWorld.configAW.get("Items", ItemSiliconSliceName, ItemSiliconSliceID).getInt();
          ItemSaltID = 				AnotherWorld.configAW.get("Items", ItemSaltName, ItemSaltID).getInt();
      
    
          //Blocks
          ChromiumID = 				AnotherWorld.configAW.get("Blocks", ChromiumName, ChromiumID).getInt();
          SaltID = 					AnotherWorld.configAW.get("Blocks", SaltName, SaltID).getInt();
          TyteonRockID = 			AnotherWorld.configAW.get("Blocks", TyteonRockName, TyteonRockID).getInt();
          RedDustID =				AnotherWorld.configAW.get("Blocks", RedDustName, RedDustID).getInt();
          SpaceCactusID =			AnotherWorld.configAW.get("Blocks", SpaceCactusName, SpaceCactusID).getInt();
         
  		 BlockTriCSFlowID = 		AnotherWorld.configAW.get("Blocks", BlockTriCSFlowName + " Still version = this + 1", BlockTriCSFlowID).getInt();
  		 SpaceChestID =				AnotherWorld.configAW.get("Blocks", SpaceChestName, SpaceChestID).getInt();;
 		 BlockEngineeringGlassID =	AnotherWorld.configAW.get("Blocks", BlockEngineeringGlassName, BlockEngineeringGlassID).getInt();
         SeparatorID = 				AnotherWorld.configAW.get("Blocks", SeparatorName, SeparatorID).getInt();			
          
          AnotherWorld.configAW.save();
 	}
 	
 	
 	
 }
