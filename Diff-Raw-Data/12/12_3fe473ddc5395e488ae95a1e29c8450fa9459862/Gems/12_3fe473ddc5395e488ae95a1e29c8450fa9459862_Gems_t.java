 package ictrobot.gems;
 
 //imports
 import ictrobot.gems.armor.lapisgem.LapisGemBoots;
 import ictrobot.gems.armor.lapisgem.LapisGemChestplate;
 import ictrobot.gems.armor.lapisgem.LapisGemHelmet;
 import ictrobot.gems.armor.lapisgem.LapisGemLeggings;
 import ictrobot.gems.items.lapisgem.ChargedLapisGem;
 import ictrobot.gems.items.lapisgem.LapisGem;
 import ictrobot.gems.block.lapisgem.BlockLapisGem;
 import ictrobot.gems.Reference;
 import ictrobot.gems.proxy.CommonProxy;
 import ictrobot.gems.tools.lapisgem.LapisGemTool;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.src.ModLoader;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.EnumHelper;
 import net.minecraftforge.common.MinecraftForge;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION_NUMBER)
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class Gems {
   @Instance(Reference.MOD_ID)
   public static Gems instance;
 
   // Define IDs
   public static int lapisGemID;
   public static int blockLapisGemID;
   public static int chargedLapisGemID;
   public static int lapisGemHelmetID;
   public static int lapisGemChestplateID;
   public static int lapisGemLeggingsID;
   public static int lapisGemBootsID;
   public static int lapisGemToolID;
   // Define Items
   public static Item lapisGem;
   public static Item chargedLapisGem;
   public static Item lapisGemHelmet;
   public static Item lapisGemChestplate;
   public static Item lapisGemLeggings;
   public static Item lapisGemBoots;
   public static Item lapisGemTool;
   
   //Define Blocks
   public static Block blockLapisGem;
 
   // Proxy
   @SidedProxy(clientSide = "ictrobot.gems.proxy.ClientProxy", serverSide = "ictrobot.gems.proxy.CommonProxy")
   public static CommonProxy proxy;
 
   // preInit / Read Config
   @EventHandler
   public void preInit(FMLPreInitializationEvent event) {
     Configuration config = new Configuration(event.getSuggestedConfigurationFile());
     config.load();
     //Items
     lapisGemID = config.get(Configuration.CATEGORY_ITEM, "LapisGem", 5001).getInt();
     chargedLapisGemID = config.get(Configuration.CATEGORY_ITEM, "ChargedLapisGem", 5002).getInt();
     lapisGemHelmetID = config.get(Configuration.CATEGORY_ITEM, "LapisGemHelmet", 5011).getInt();
     lapisGemChestplateID = config.get(Configuration.CATEGORY_ITEM, "LapisChestplate", 5012).getInt();
     lapisGemLeggingsID = config.get(Configuration.CATEGORY_ITEM, "LapisGemLeggings", 5013).getInt();
     lapisGemBootsID = config.get(Configuration.CATEGORY_ITEM, "LapisGemBoots", 5014).getInt();
     lapisGemToolID = config.get(Configuration.CATEGORY_ITEM, "lapisGemTool", 5021).getInt();
     //Blocks
     blockLapisGemID = config.get(Configuration.CATEGORY_BLOCK, "BlockLapisGem", 1001).getInt();
     config.save();
   }
 
   // load
   @EventHandler
   public void load(FMLInitializationEvent event) {
     proxy.registerRenderers();
 
     // Settings for Items, Unlocalized Names, Creative Tabs
     //Items
     lapisGem = (new LapisGem(lapisGemID)).setMaxStackSize(64).setUnlocalizedName("LapisGem").setCreativeTab(CreativeTabs.tabMaterials);
     chargedLapisGem = (new ChargedLapisGem(chargedLapisGemID)).setMaxStackSize(64).setUnlocalizedName("ChargedLapisGem").setCreativeTab(CreativeTabs.tabMaterials);
     
     //Tools
     
    EnumToolMaterial lapisGemToolMaterial = EnumHelper.addToolMaterial("Lapis Gem Tool", 4, 2048, 10F, -4F, 50);
     lapisGemTool = (new LapisGemTool(lapisGemToolID, 10F, lapisGemToolMaterial)).setUnlocalizedName("lapisGemTool").setCreativeTab(CreativeTabs.tabTools);
     
     //Armor
     EnumArmorMaterial lapisGemArmorMaterial = EnumHelper.addArmorMaterial("Lapis Gem Armor", 40, new int[] { 5, 10, 8, 5 }, 50);
 
     lapisGemHelmet = new LapisGemHelmet(lapisGemHelmetID, lapisGemArmorMaterial, ModLoader.addArmor("LapisGem"), 0).setUnlocalizedName("LapisGemHelmet").setCreativeTab(CreativeTabs.tabCombat);
     lapisGemChestplate = new LapisGemChestplate(lapisGemChestplateID, lapisGemArmorMaterial, ModLoader.addArmor("LapisGem"), 1).setUnlocalizedName("LapisGemChestplate").setCreativeTab(CreativeTabs.tabCombat);
     lapisGemLeggings = new LapisGemLeggings(lapisGemLeggingsID, lapisGemArmorMaterial, ModLoader.addArmor("LapisGem"), 2).setUnlocalizedName("LapisGemLeggings").setCreativeTab(CreativeTabs.tabCombat);
     lapisGemBoots = new LapisGemBoots(lapisGemBootsID, lapisGemArmorMaterial, ModLoader.addArmor("LapisGem"), 3).setUnlocalizedName("LapisGemBoots").setCreativeTab(CreativeTabs.tabCombat);
     //Blocks
     blockLapisGem = new BlockLapisGem(blockLapisGemID, Material.rock).setHardness(4.0F).setResistance(7.5F).setStepSound(Block.soundMetalFootstep).setUnlocalizedName("BlockLapisGem").setCreativeTab(CreativeTabs.tabBlock);
     
     // Register Recipes
 
     GameRegistry.addRecipe(new ItemStack(lapisGem, 1), "lll", "ldl", "lll", 'l', new ItemStack(Item.dyePowder, 1, 4), 'd', new ItemStack(Item.diamond));
     GameRegistry.addSmelting(lapisGemID + 256, new ItemStack(chargedLapisGem), 1);
     GameRegistry.addRecipe(new ItemStack(lapisGemHelmet), "lll", "l l", "   ", 'l', new ItemStack(chargedLapisGem));
     GameRegistry.addRecipe(new ItemStack(lapisGemChestplate), "l l", "lll", "lll", 'l', new ItemStack(chargedLapisGem));
     GameRegistry.addRecipe(new ItemStack(lapisGemLeggings), "lll", "l l", "l l", 'l', new ItemStack(chargedLapisGem));
     GameRegistry.addRecipe(new ItemStack(lapisGemBoots), "   ", "l l", "l l", 'l', new ItemStack(chargedLapisGem));
     GameRegistry.addRecipe(new ItemStack(blockLapisGem), "lll", "lll", "lll", 'l', new ItemStack(lapisGem));
     GameRegistry.addRecipe(new ItemStack(blockLapisGem), "lll", "lll", "lll", 'l', new ItemStack(chargedLapisGem));
     GameRegistry.addShapelessRecipe(new ItemStack(lapisGem, 9), new ItemStack(blockLapisGem));
     GameRegistry.addRecipe(new ItemStack(lapisGemTool), "lll", "lsl", "lll", 'l', new ItemStack(chargedLapisGem), 's', new ItemStack(Item.stick));
   }
 
   // postInit / Add Names
   @EventHandler
   public void postInit(FMLPostInitializationEvent event) {
     //Items
     LanguageRegistry.addName(lapisGem, "Lapis Gem");
     GameRegistry.registerItem(lapisGem, "lapisGem");
     LanguageRegistry.addName(chargedLapisGem, "Charged Lapis Gem");
     GameRegistry.registerItem(chargedLapisGem, "chargedLapisGem");
     LanguageRegistry.addName(lapisGemHelmet, "Lapis Gem Helmet");
     GameRegistry.registerItem(lapisGemHelmet, "lapisGemHelmet");
     LanguageRegistry.addName(lapisGemChestplate, "Lapis Gem Chestplate");
     GameRegistry.registerItem(lapisGemChestplate, "lapisGemChestplate");
     LanguageRegistry.addName(lapisGemLeggings, "Lapis Gem Leggings");
     GameRegistry.registerItem(lapisGemLeggings, "lapisGemLeggings");
     LanguageRegistry.addName(lapisGemBoots, "Lapis Gem Boots");
     GameRegistry.registerItem(lapisGemBoots, "lapisGemBoots");
     LanguageRegistry.addName(lapisGemTool, "Lapis Gem Tool");
     GameRegistry.registerItem(lapisGemTool, "lapisGemTool");
     //Blocks
     LanguageRegistry.addName(blockLapisGem, "Block of Lapis Gems");
     GameRegistry.registerBlock(blockLapisGem, "blockLapisGem");
     MinecraftForge.setBlockHarvestLevel(blockLapisGem, "pickaxe", 2);
   }
 }
