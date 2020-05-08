 package us.joaogldarkdeagle.hygienic;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import us.joaogldarkdeagle.hygienic.blockitem.BlockPolluted;
 import us.joaogldarkdeagle.hygienic.blockitem.ItemMop;
 import us.joaogldarkdeagle.hygienic.gui.GuiHandler;
 import us.joaogldarkdeagle.hygienic.lib.HygienicTab;
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
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid = ModInfo.MOD_ID, name = ModInfo.MOD_NAME, version = ModInfo.MOD_VERSION)
 @NetworkMod(clientSideRequired = true, serverSideRequired = true)
 public class Hygienic {
 
     @Instance("Hygienic")
     public static Hygienic instance;
 
     public static CreativeTabs hygienicTab = new HygienicTab("Hygienic");
     public static Block pollutedBlock;
     public static Block polluCraft;
     public static Item mop;
 
     @PreInit
     public void preInit(FMLPreInitializationEvent event) {
 
     }
 
     @Init
     public void init(FMLInitializationEvent event) {
         mop = (new ItemMop(900, EnumToolMaterial.IRON));
         LanguageRegistry.addName(mop, "Mop");
        GameRegistry.addRecipe(new ItemStack(mop, 1), new Object[] { " S ", " S ", "WWW", Character.valueOf('S'), Item.stick, Character.valueOf('W'), Item.silk });
 
         pollutedBlock = new BlockPolluted(901, Material.snow);
         GameRegistry.registerBlock(pollutedBlock, "polluted_UN");
         LanguageRegistry.addName(pollutedBlock, "Pollution");        
         
         polluCraft = new BlockPolluCraft(902, Material.wood);
         GameRegistry.registerBlock(polluCraft, "polluCraft_UN");
         LanguageRegistry.addName(polluCraft, "PolluCraft");
         GameRegistry.addRecipe(new ItemStack(polluCraft, 1), new Object[] { "   ", " XX", " XX", Character.valueOf('X'), Item.ingotIron });
         
         
         NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler());
         LanguageRegistry.instance().addStringLocalization("itemGroup.Hygienic", "en_US", "Hygienic");
     }
 
     @PostInit
     public void postInit(FMLPostInitializationEvent event) {
 
     }
 }
