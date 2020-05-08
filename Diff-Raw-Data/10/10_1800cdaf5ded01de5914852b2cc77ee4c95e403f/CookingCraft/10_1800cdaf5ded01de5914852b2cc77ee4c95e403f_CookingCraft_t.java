 package mods.cc.rock;
 
 import mods.cc.rock.block.ModBlocks;
 import mods.cc.rock.core.proxy.CommonProxy;
 import mods.cc.rock.creativetabs.CreativeTabCC;
 import mods.cc.rock.item.ModItems;
 import mods.cc.rock.lib.Reference;
 
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 
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
 
 @Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION)
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class CookingCraft
 {
         @Instance(Reference.MOD_ID)
         public static CookingCraft instance;
         
     	@SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
     	public static CommonProxy proxy;
     	
     	
     	public static CreativeTabs tabCC = new CreativeTabCC(Reference.MOD_ID);
     	
         
         @SuppressWarnings("unchecked")
         public static void addOreRecipe(ItemStack output, Object[] input) 
     	{
     		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(output, new Object[] { Boolean.valueOf(true), input }));
     	}
     
         
         @PreInit
         public void preInit(FMLPreInitializationEvent event)
         {
             ModBlocks.initBlocks();
             
             ModItems.initItems();
             
         }
         
         
         @Init
         public void load(FMLInitializationEvent event)
         {
        	LanguageRegistry.instance().addStringLocalization("itemGroup.CC", "en_US", "CookingCraft");
             
         }
         
         
         @PostInit
         public void postInit(FMLPostInitializationEvent event)
         {
             
             
         }
 }
