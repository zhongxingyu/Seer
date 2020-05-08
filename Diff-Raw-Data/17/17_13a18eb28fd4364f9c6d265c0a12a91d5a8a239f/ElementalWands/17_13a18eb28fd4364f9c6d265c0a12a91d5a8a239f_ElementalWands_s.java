 package hylinn.minecraft.ElementalWands;
 
 import net.minecraft.item.Item;
 import hylinn.minecraft.ElementalWands.item.EnumWandElement;
 import hylinn.minecraft.ElementalWands.item.EnumWandMaterial;
 import hylinn.minecraft.ElementalWands.item.ItemWand;
 import hylinn.minecraft.ElementalWands.proxy.CommonProxy;
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
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid="ElementalWands", name="Elemental Wands", version="0.0.1")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false)
 public class ElementalWands {
 
     private static final Item woodFireWand = (new ItemWand(31000, EnumWandMaterial.WODD, EnumWandElement.FIRE)).setUnlocalizedName("woodFireWand");
 	
     // The instance of your mod that Forge uses.
     @Instance("ElementalWands")
     public static ElementalWands instance;
    
     // Says where the client and server 'proxy' code is loaded.
     @SidedProxy(clientSide="hylinn.minecraft.ElementalWands.proxy.ClientProxy", serverSide="hylinn.minecraft.ElementalWands.proxy.CommonProxy")
     public static CommonProxy proxy;
    
     @PreInit
     public void preInit(FMLPreInitializationEvent event) {
     	event.getModMetadata().version = "0.0.1";
     	event.getModMetadata().name = "Elemental Wands";
	event.getModMetadata().description = "Adds wands of the five elements (Fire, Earth, Water, Air, and Arcane)";
	event.getModMetadata().authorList.addAll(Array.asList(new String[]{"Hylinn Taggart", "Matt Beyer"});
	//event.getModMetadata().logoFile = "";
	//event.getModMetadata().dependencies.addAll(Array.asList(new String[]{"mod_MinecraftForge"});
	//event.getModMetadata().credits = "";
	//event.getModMetadata().updateUrl = "";
	//event.getModMetadata().modId = "";
     }
    
     @Init
     public void load(FMLInitializationEvent event) {
     	LanguageRegistry.addName(woodFireWand, "Wood Fire Wand");
         //proxy.registerRenderers();
     }
    
     @PostInit
     public void postInit(FMLPostInitializationEvent event) {
         // Stub Method
     }
 }
