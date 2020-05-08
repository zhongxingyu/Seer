 package marksomnian.milkcraft.common;
 
 
 import java.io.File;
 
 import cpw.mods.fml.common.Mod;
 import marksomnian.milkcraft.common.CommonProxy;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.event.Event;
 
 @Mod(modid = "Marksomnian_MilkCraft", name = "MilkCraft", version = "0.0.2")
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class MilkCraft {
 
 	
 	@PreInit
 	public void PreInit(FMLPreInitializationEvent FMLPreInitializationEvent)
 	{
 	}
 	 public static CreativeTabs tabCustom = new CreativeTabs("tabCustom") {
          public ItemStack getIconItemStack() {
                  return new ItemStack(Item.glassBottle, 1, 0);
                  
          }
 	 };
 	
 	private final static Item TestItem = new TestItem(1000);
 	public final static Block BlockTestFlowing = new BlockTestFlowing(501);
 	public final static Block BlockTestStill = new BlockTestStill(502);
 	public final static Block TestBlock = new TestBlock(500);
 	private static final int OreCopperstoneID = 503;
 	public static Block OreCopperstone,
 	CopperstoneOre = new BlockOreCopperstone(OreCopperstoneID, Material.iron).setHardness(2.5F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep);
 	
  
  @Init
  public void load(FMLInitializationEvent event) 
  {
 	 
 		GameRegistry.registerBlock(BlockTestFlowing, "BlockTestFlowing");
 		GameRegistry.registerBlock(BlockTestStill, "BlockTestStill");
 		GameRegistry.registerBlock(TestBlock);
 	 
 	 	LanguageRegistry.addName(TestItem, "Test Item");
 		LanguageRegistry.addName(BlockTestFlowing, "Test Flowing Block");
 		LanguageRegistry.addName(BlockTestStill, "Test Still Block");
 		LanguageRegistry.addName(TestBlock, "Test Block");
 		
 		LanguageRegistry.instance().addStringLocalization("itemGroup.tabCustom", "en_US", "MilkCraft");
 
   
  }
}
