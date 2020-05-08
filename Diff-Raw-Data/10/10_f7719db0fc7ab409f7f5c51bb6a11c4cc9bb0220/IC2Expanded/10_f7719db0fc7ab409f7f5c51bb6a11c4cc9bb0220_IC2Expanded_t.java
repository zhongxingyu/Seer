 package IC2Expanded;
 
import ic2.api.Items;

import java.lang.reflect.Field;
 import java.util.logging.Logger;
 
 import IC2Expanded.item.ItemArmorQuantumSuitMk1;
 
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.client.MinecraftForgeClient;
 import net.minecraftforge.common.Configuration;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
@Mod(modid = "IC2Expanded", name = "IC2 Expanded", version = "0.1.4")
 @NetworkMod(clientSideRequired = true)
 public class IC2Expanded {
 	public static int quantumSuitMk1ID, quantumSuitMk2ID;
 	public static Item quantumSuitMk1, quantumSuitMk2;
 	
 	@PreInit
 	public void preload(FMLPreInitializationEvent iEvent) {
 		MinecraftForgeClient.preloadTexture("/IC2Expanded/images/item.png");
 		Configuration conf = new Configuration(iEvent.getSuggestedConfigurationFile());
 		conf.load();
 		quantumSuitMk1ID = conf.getItem("quantumSuitMk1ID", 7000).getInt();
 		quantumSuitMk2ID = conf.getItem("quantumSuitMk2ID", 7001).getInt();
 		conf.save();
 	}
 	
 	@Init
 	public void load(FMLInitializationEvent iEvent) {
 		quantumSuitMk1 = new ItemArmorQuantumSuitMk1(quantumSuitMk1ID);
		GameRegistry.addShapelessRecipe(new ItemStack(quantumSuitMk1), Items.getItem("quantumBodyarmor"), Items.getItem("lapPack"));
 		LanguageRegistry.addName(quantumSuitMk1, "Quantum Suit Mk1");
 	}
 }
