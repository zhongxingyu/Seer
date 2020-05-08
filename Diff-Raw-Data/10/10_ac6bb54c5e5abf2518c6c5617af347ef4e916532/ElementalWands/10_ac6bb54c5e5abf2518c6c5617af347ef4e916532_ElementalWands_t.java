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
     public static final Item woodEarthWand   = (new ItemWand(31005, EnumWandMaterial.WOOD,   EnumWandElement.EARTH)).setUnlocalizedName("woodEarthWand");
     public static final Item boneEarthWand   = (new ItemWand(31006, EnumWandMaterial.BONE,   EnumWandElement.EARTH)).setUnlocalizedName("boneEarthWand");
     public static final Item ironEarthWand   = (new ItemWand(31007, EnumWandMaterial.IRON,   EnumWandElement.EARTH)).setUnlocalizedName("ironEarthWand");
     public static final Item quartzEarthWand = (new ItemWand(31008, EnumWandMaterial.QUARTZ, EnumWandElement.EARTH)).setUnlocalizedName("quartzEarthWand");
     public static final Item goldEarthWand   = (new ItemWand(31009, EnumWandMaterial.GOLD,   EnumWandElement.EARTH)).setUnlocalizedName("goldEarthWand");
     
     // Create the water wands.
     public static final Item woodWaterWand   = (new ItemWand(31010, EnumWandMaterial.WOOD,   EnumWandElement.WATER)).setUnlocalizedName("woodWaterWand");
     public static final Item boneWaterWand   = (new ItemWand(31011, EnumWandMaterial.BONE,   EnumWandElement.WATER)).setUnlocalizedName("boneWaterWand");
     public static final Item ironWaterWand   = (new ItemWand(31012, EnumWandMaterial.IRON,   EnumWandElement.WATER)).setUnlocalizedName("ironWaterWand");
     public static final Item quartzWaterWand = (new ItemWand(31013, EnumWandMaterial.QUARTZ, EnumWandElement.WATER)).setUnlocalizedName("quartzWaterWand");
     public static final Item goldWaterWand   = (new ItemWand(31014, EnumWandMaterial.GOLD,   EnumWandElement.WATER)).setUnlocalizedName("goldWaterWand");
     
     // Create the air wands.
     public static final Item woodAirWand   = (new ItemWand(31015, EnumWandMaterial.WOOD,   EnumWandElement.AIR)).setUnlocalizedName("woodAirWand");
     public static final Item boneAirWand   = (new ItemWand(31016, EnumWandMaterial.BONE,   EnumWandElement.AIR)).setUnlocalizedName("boneAirWand");
     public static final Item ironAirWand   = (new ItemWand(31017, EnumWandMaterial.IRON,   EnumWandElement.AIR)).setUnlocalizedName("ironAirWand");
     public static final Item quartzAirWand = (new ItemWand(31018, EnumWandMaterial.QUARTZ, EnumWandElement.AIR)).setUnlocalizedName("quartzAirWand");
     public static final Item goldAirWand   = (new ItemWand(31019, EnumWandMaterial.GOLD,   EnumWandElement.AIR)).setUnlocalizedName("goldAirWand");
     
     // Create the arcane wands.
    public static final Item woodArcaneWand   = (new ItemWand(31020, EnumWandMaterial.WOOD,   EnumWandElement.ARCANE)).setUnlocalizedName("woodArcaneWand");
    public static final Item boneArcaneWand   = (new ItemWand(31021, EnumWandMaterial.BONE,   EnumWandElement.ARCANE)).setUnlocalizedName("boneArcaneWand");
    public static final Item ironArcaneWand   = (new ItemWand(31022, EnumWandMaterial.IRON,   EnumWandElement.ARCANE)).setUnlocalizedName("ironArcaneWand");
    public static final Item quartzArcaneWand = (new ItemWand(31023, EnumWandMaterial.QUARTZ, EnumWandElement.ARCANE)).setUnlocalizedName("quartzArcaneWand");
    public static final Item goldArcaneWand   = (new ItemWand(31024, EnumWandMaterial.GOLD,   EnumWandElement.ARCANE)).setUnlocalizedName("goldArcaneWand");
 	
     // Create the enchantment type for wands.
     public static final EnumEnchantmentType enchantmentWand = EnumHelper.addEnchantmentType("wand");
     
     @Instance("ElementalWands")
     public static ElementalWands instance;
    
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
     	
     	// Register the water wands.
     	LanguageRegistry.addName(woodWaterWand,   "Wood Water Wand");
     	LanguageRegistry.addName(boneWaterWand,   "Bone Water Wand");
     	LanguageRegistry.addName(ironWaterWand,   "Iron Water Wand");
     	LanguageRegistry.addName(quartzWaterWand, "Nether Quartz Water Wand");
     	LanguageRegistry.addName(goldWaterWand,   "Gold Water Wand");
     	
     	// Register the air wands.
     	LanguageRegistry.addName(woodAirWand,   "Wood Air Wand");
     	LanguageRegistry.addName(boneAirWand,   "Bone Air Wand");
     	LanguageRegistry.addName(ironAirWand,   "Iron Air Wand");
     	LanguageRegistry.addName(quartzAirWand, "Nether Quartz Air Wand");
     	LanguageRegistry.addName(goldAirWand,   "Gold Air Wand");
     	
     	// Register the arcane wands.
     	LanguageRegistry.addName(woodArcaneWand,   "Wood Arcane Wand");
     	LanguageRegistry.addName(boneArcaneWand,   "Bone Arcane Wand");
     	LanguageRegistry.addName(ironArcaneWand,   "Iron Arcane Wand");
     	LanguageRegistry.addName(quartzArcaneWand, "Nether Quartz Arcane Wand");
     	LanguageRegistry.addName(goldArcaneWand,   "Gold Arcane Wand");
     	
         //proxy.registerRenderers();
     }
    
     @PostInit
     public void modsLoaded(FMLPostInitializationEvent event) {
         // Stub Method
     }
 }
