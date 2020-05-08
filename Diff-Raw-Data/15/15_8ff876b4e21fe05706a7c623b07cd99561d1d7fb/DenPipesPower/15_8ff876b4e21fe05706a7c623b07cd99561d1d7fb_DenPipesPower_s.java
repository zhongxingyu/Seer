 package denoflionsx.DenPipes.AddOns.Power;
 
 import buildcraft.BuildCraftSilicon;
 import buildcraft.BuildCraftTransport;
 import buildcraft.api.core.IIconProvider;
 import buildcraft.api.recipes.AssemblyRecipe;
 import buildcraft.transport.ItemPipe;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import denoflionsx.DenPipes.API.DenPipesAPI;
 import denoflionsx.DenPipes.API.Interfaces.IDenPipeAddon;
 import denoflionsx.DenPipes.AddOns.Power.Client.PipeProvider;
 import denoflionsx.DenPipes.AddOns.Power.Pipes.DiamondWoodenKinesisPipe;
 import denoflionsx.denLib.Mod.denLibMod;
 import denoflionsx.denLib.NewConfig.ConfigField;
 import java.io.File;
 import net.minecraft.item.ItemStack;
 
 @Mod(modid = "@NAME@", name = "@NAME@", version = "@VERSION@", dependencies = "@DEPENDS@")
 @NetworkMod(clientSideRequired = true, serverSideRequired = true)
 public class DenPipesPower implements IDenPipeAddon {
 
     @Mod.Instance("@NAME@")
     public static Object instance;
     public static IIconProvider icons = new PipeProvider();
     public static ItemPipe diamondwoodenkinesis;
     @ConfigField(category = "recipes")
     public static boolean recipes_AssemblyTable = true;
 
     @Mod.EventHandler
     public void preLoad(FMLPreInitializationEvent event) {
         DenPipesAPI.addons.add(this);
         denLibMod.configManager.setup(event.getSourceFile(), new File(event.getModConfigurationDirectory().getAbsolutePath() + "/denoflionsx/DenPipes/" + event.getSuggestedConfigurationFile().getName()));
     }
 
     @Override
     public void preinit(FMLPreInitializationEvent event) {
     }
 
     @Override
     public void init(FMLInitializationEvent event) {
         diamondwoodenkinesis = DenPipesAPI.manager.Pipes.registerPipe(DiamondWoodenKinesisPipe.class);
     }
 
     @Override
     public void postinit(FMLPostInitializationEvent evt) {
         if (recipes_AssemblyTable) {
            AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[]{new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3), new ItemStack(BuildCraftTransport.pipePowerWood)}, 1024, new ItemStack(diamondwoodenkinesis)));
         } else {
            GameRegistry.addShapelessRecipe(new ItemStack(diamondwoodenkinesis), new Object[]{new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3), new ItemStack(BuildCraftTransport.pipePowerWood)});
         }
     }
 }
