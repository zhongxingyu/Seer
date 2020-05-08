 package com.github.ubiquitousspice.dreamdimension;
 
import com.github.ubiquitousspice.dreamdimension.blocks.BlockDreamDirt;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import net.minecraft.block.Block;
 import net.minecraftforge.common.Configuration;
 
 import java.util.logging.Logger;
 
 @Mod(modid = DreamDimension.MODID, version = DreamDimension.VERSION)
 public class DreamDimension
 {
     public static final String MODID = "dreamdimension";
     public static final String VERSION = "0.1";
 
     @Mod.Instance
     public static DreamDimension instance;
 
    @SidedProxy(modId = MODID, clientSide = "com.github.ubiquitousspice.dreamdimension.client.ProxyClient",
            serverSide = "com.github.ubiquitousspice.dreamdimension.ProxyCommon")
    @SidedProxy(modId=MODID, clientSide = "com.github.ubiquitousspice.dreamdimension.client.ProxyClient", serverSide = "com.github.ubiquitousspice.dreamdimension.ProxyCommon")
     public static ProxyCommon proxy;
 
     public static Logger logger;
 
     // Material
     public static MaterialDream material;
 
     // random configurations
     public static boolean dreamMaterialBreakable = false;
 
     // dimension configs
 
     // IDS
     public static int dimensionID;
    
     private int idDreamDirt;
 
     // blocks
     private Block dreamDirt;
 
     @EventHandler
     public void preInit(FMLPreInitializationEvent event)
     {
         // get logger
         logger = event.getModLog();
 
         // mess with material
         material = (new MaterialDream());
 
         // CONFIGURATION STUFF
         {
             Configuration config = new Configuration(event.getSuggestedConfigurationFile());
 
             // config blockIDs
             idDreamDirt = config.getBlock("DreamDirt", 300).getInt();
 
             // config itemIDs
 
             // config dimension
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        idDreamDirt = config.getBlock("DreamDirt", 300).getInt();
        //dimensionID = config.get("Dimension ID", key, 2).getInt();
 
             // config other
             dreamMaterialBreakable = config.get("Adventure", "dreamMaterialBreakable", false).getBoolean(false);
        if (config.hasChanged())
            config.save();
 
             // save it.
             if (config.hasChanged())
             {
                 config.save();
             }
         }
         // do config stuff
     }
 
     @EventHandler
     public void init(FMLInitializationEvent event)
     {
         // do blocks and stuff here.
        dreamDirt = new BlockDreamDirt(idDreamDirt).setUnlocalizedName(MODID + ".dreamDirt");
        dreamDirt = new BlockDreamDirt(idDreamDirt).setUnlocalizedName(MODID+".dreamDirt");
     }
 }
