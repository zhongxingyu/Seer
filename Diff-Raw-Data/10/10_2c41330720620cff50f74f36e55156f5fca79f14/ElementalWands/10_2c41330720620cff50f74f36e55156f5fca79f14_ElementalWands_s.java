 package hylinn.minecraft.ElementalWands;
 
 import java.util.Arrays;
 
 import net.minecraft.enchantment.EnumEnchantmentType;
 import net.minecraft.item.Item;
 import net.minecraftforge.common.EnumHelper;
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
 
 	// Create the fire wands.
     public static final Item woodFireWand   = (new ItemWand(31000, EnumWandMaterial.WOOD,   EnumWandElement.FIRE)).setUnlocalizedName("woodFireWand");
     public static final Item boneFireWand   = (new ItemWand(31001, EnumWandMaterial.BONE,   EnumWandElement.FIRE)).setUnlocalizedName("boneFireWand");
     public static final Item ironFireWand   = (new ItemWand(31002, EnumWandMaterial.IRON,   EnumWandElement.FIRE)).setUnlocalizedName("ironFireWand");
     public static final Item quartzFireWand = (new ItemWand(31003, EnumWandMaterial.QUARTZ, EnumWandElement.FIRE)).setUnlocalizedName("quartzFireWand");
     public static final Item goldFireWand   = (new ItemWand(31004, EnumWandMaterial.GOLD,   EnumWandElement.FIRE)).setUnlocalizedName("goldFireWand");
     
     // Create the earth wands.
    public static final Item woodEarthWand   = (new ItemWand(31000, EnumWandMaterial.WOOD,   EnumWandElement.EARTH)).setUnlocalizedName("woodEarthWand");
    public static final Item boneEarthWand   = (new ItemWand(31001, EnumWandMaterial.BONE,   EnumWandElement.EARTH)).setUnlocalizedName("boneEarthWand");
    public static final Item ironEarthWand   = (new ItemWand(31002, EnumWandMaterial.IRON,   EnumWandElement.EARTH)).setUnlocalizedName("ironEarthWand");
    public static final Item quartzEarthWand = (new ItemWand(31003, EnumWandMaterial.QUARTZ, EnumWandElement.EARTH)).setUnlocalizedName("quartzEarthWand");
    public static final Item goldEarthWand   = (new ItemWand(31004, EnumWandMaterial.GOLD,   EnumWandElement.EARTH)).setUnlocalizedName("goldEarthWand");
 	
     // Create the enchantment type for wands.
     public static final EnumEnchantmentType enchantmentWand = EnumHelper.addEnchantmentType("wand");
     
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
     	event.getModMetadata().authorList.addAll(Arrays.asList(new String[]{"Hylinn Taggart", "Matt Beyer"}));
     	//event.getModMetadata().logoFile = "";
     	//event.getModMetadata().dependencies.addAll(Arrays.asList(new String[]{"mod_MinecraftForge"}));
     	//event.getModMetadata().credits = "";
     	//event.getModMetadata().updateUrl = "";
     	//event.getModMetadata().modId = "";
     }
    
     @Init
     public void load(FMLInitializationEvent event) {
     	// Register the fire wands.
     	LanguageRegistry.addName(woodFireWand,   "Wood Fire Wand");
     	LanguageRegistry.addName(boneFireWand,   "Bone Fire Wand");
     	LanguageRegistry.addName(ironFireWand,   "Iron Fire Wand");
     	LanguageRegistry.addName(quartzFireWand, "Nether Quartz Fire Wand");
     	LanguageRegistry.addName(goldFireWand,   "Gold Fire Wand");
     	
     	// Register the earth wands.
     	LanguageRegistry.addName(woodEarthWand,   "Wood Earth Wand");
     	LanguageRegistry.addName(boneEarthWand,   "Bone Earth Wand");
     	LanguageRegistry.addName(ironEarthWand,   "Iron Earth Wand");
     	LanguageRegistry.addName(quartzEarthWand, "Nether Quartz Earth Wand");
     	LanguageRegistry.addName(goldEarthWand,   "Gold Earth Wand");
         //proxy.registerRenderers();
     }
    
     @PostInit
     public void modsLoaded(FMLPostInitializationEvent event) {
         // Stub Method
     }
 }
