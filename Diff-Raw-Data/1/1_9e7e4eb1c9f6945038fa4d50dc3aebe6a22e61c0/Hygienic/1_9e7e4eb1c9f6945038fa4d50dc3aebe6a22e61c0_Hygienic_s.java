 package us.joaogldarkdeagle.hygienic;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import us.joaogldarkdeagle.hygienic.lib.ModInfo;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid = ModInfo.MOD_ID, name = ModInfo.MOD_NAME, version = ModInfo.MOD_VERSION)
 @NetworkMod(clientSideRequired = true, serverSideRequired = true)
 public class Hygienic {
     
     @Instance("Hygienic")
     public Hygienic instance;
 
     public static CreativeTabs hygienicTab = new HygienicTab("Hygienic");
     public static Block mopBukket;
     public static Item mop;
     public static Item food;
     public static Item apple;
     public static Item glassBow;
     static String mopBukket_Tex = "Hygienic:Blocks";
     static String mop_Tex = "Hygienic:Items";
     static String modFood_Tex = "Hygienic:Food";
     static String modApple_Tex = "Hygienic:Apple";
     static String modBow_Tex = "Hygienic:Bow";
 
     @PreInit
     public void preInit(FMLPreInitializationEvent event) {
         
     }
 
     @Init
     public void init(FMLInitializationEvent event) {
         mopBukket = new BlockMopBukket(900, Material.grass, mopBukket_Tex).setUnlocalizedName("mopBukket_UN");
         mopBukket.setStepSound(Block.soundMetalFootstep);
         
         mop = (new ItemMop(901, mop_Tex)).setUnlocalizedName("Mop").setCreativeTab(hygienicTab);
         food = (new ItemModFood(902, 18, true, modFood_Tex)).setUnlocalizedName("My Food");
         apple = (new ItemModApple(903, 18, false, modFood_Tex)).setUnlocalizedName("My Apple").setPotionEffect("potion.blindness");
         glassBow = (new GlassBow(904, modBow_Tex)).setUnlocalizedName("Glass Bow");
         
         GameRegistry.registerBlock(mopBukket, "mopBukket_UN");
         
         LanguageRegistry.addName(mopBukket, "Mop Bukket");
         LanguageRegistry.addName(mop, "Mop");   
         LanguageRegistry.addName(food, "My Food");   
         LanguageRegistry.addName(apple, "My Apple");  
         LanguageRegistry.addName(glassBow, "Glass Bow");   
         
         GameRegistry.addRecipe(new ItemStack(mopBukket, 1), new Object[] {"   "," X ","X X", Character.valueOf('X'), Item.ingotIron});
         GameRegistry.addRecipe(new ItemStack(mop, 4), new Object[] {"XXX","XXX","XXX", Character.valueOf('X'), mopBukket});
         GameRegistry.addRecipe(new ItemStack(food, 4), new Object[] {"   "," X "," X ", Character.valueOf('X'), Item.ingotIron});
         GameRegistry.addRecipe(new ItemStack(apple, 4), new Object[] {" X "," X "," X ", Character.valueOf('X'), Item.ingotIron});
         
         GameRegistry.addRecipe(new ItemStack(glassBow, 1), new Object[] {" XS","X S"," XS", Character.valueOf('X'), Block.glass, Character.valueOf('S'), Item.silk});
     }
 
     @PostInit
     public void postInit(FMLPostInitializationEvent event) {
 
     }
 }
