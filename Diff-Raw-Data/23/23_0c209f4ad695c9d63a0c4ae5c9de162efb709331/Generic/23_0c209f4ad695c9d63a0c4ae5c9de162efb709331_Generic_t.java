package tutorial.generic;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler; // used in 1.6.2
 //import cpw.mods.fml.common.Mod.PreInit;    // used in 1.5.2
 //import cpw.mods.fml.common.Mod.Init;       // used in 1.5.2
 //import cpw.mods.fml.common.Mod.PostInit;   // used in 1.5.2
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.MinecraftForge;
 
 @Mod(modid="GenericModID", name="Generic", version="0.0.0")
 @NetworkMod(clientSideRequired=true)
 public class Generic {
 
     public static Block genericDirt;
 
     // The instance of your mod that Forge uses.
     @Instance(value = "GenericModID")
     public static Generic instance;
 
     // Says where the client and server 'proxy' code is loaded.
     @SidedProxy(clientSide="tutorial.generic.client.ClientProxy", serverSide="tutorial.generic.CommonProxy")
     public static CommonProxy proxy;
 
     @EventHandler // used in 1.6.2
     //@PreInit    // used in 1.5.2
     public void preInit(FMLPreInitializationEvent event) {
         genericDirt = new GenericBlock(500, Material.ground)
                 .setUnlocalizedName("genericDirt").setCreativeTab(CreativeTabs.tabBlock);
     }
 
     @EventHandler // used in 1.6.2
     //@Init       // used in 1.5.2
     public void load(FMLInitializationEvent event) {
         proxy.registerRenderers();
 
         ItemStack stoneStack = new ItemStack(Block.stone);
         ItemStack redstoneStack = new ItemStack(Item.redstone);
 
         GameRegistry.registerBlock(genericDirt, "genericDirt");
 
         GameRegistry.addRecipe(new ItemStack(genericDirt), "srs", "srs", "sss",
                 's', stoneStack, 'r', redstoneStack);
 
         LanguageRegistry.addName(genericDirt, "Generic Dirt");
         MinecraftForge.setBlockHarvestLevel(genericDirt, "shovel", 0);
     }
 
     @EventHandler // used in 1.6.2
     //@PostInit   // used in 1.5.2
     public void postInit(FMLPostInitializationEvent event) {
         // Stub Method
     }
 }
