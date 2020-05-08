 package de.tenaciousnetwork.airmod;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.Item;
 import net.minecraftforge.common.Configuration;
 
 @Mod(modid = "airmod", version = "0.0.1", name="AirMod")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false)
 public class AirMod 
 {
 	@Instance("airmod")
 	public static AirMod instance;
 	
     @SidedProxy(clientSide="de.tenaciousnetwork.airmod.client.ClientProxy", serverSide="de.tenaciousnetwork.airmod.CommonProxy")
     public static CommonProxy proxy;
     
     public static Block blockCompressor;
     public static Item fluidAir;
     private int compressorID, airID;
    
     @PreInit
     public void preInit(FMLPreInitializationEvent event) 
     {
     	Configuration config = new Configuration(event.getSuggestedConfigurationFile());
     	config.load();
     	
     	compressorID = config.getBlock("compID", 3750, "Block ID for Compressor").getInt();
     	airID		 = config.getItem("airID", 22350, "Item ID for AIR!").getInt();
     	
     	config.save();    	
     }
    
     @Init
     public void load(FMLInitializationEvent event) 
     {
             proxy.registerRenderers();
            blockCompressor = new BlockCompressor(compressorID, Material.rock).setStepSound(Block.soundMetalFootstep).setUnlocalizedName("airmod:compf_unlit");
             fluidAir 		= new FluidAir(airID).setUnlocalizedName("airmod:air");
             GameRegistry.registerItem(fluidAir, "air");
             GameRegistry.registerTileEntity(TileEntityCompressor.class, "airmod.TEcompressor");
             GameRegistry.addRecipe(new ItemStack(blockCompressor, 1),new Object[]{"#", Character.valueOf('#'), Item.stick});
             GameRegistry.addRecipe(new ItemStack(fluidAir, 1),new Object[]{"#", Character.valueOf('#'), Block.dirt});
 
     }
    
     @PostInit
     public void postInit(FMLPostInitializationEvent event) 
     {
             // Stub Method
     }
 
 }
