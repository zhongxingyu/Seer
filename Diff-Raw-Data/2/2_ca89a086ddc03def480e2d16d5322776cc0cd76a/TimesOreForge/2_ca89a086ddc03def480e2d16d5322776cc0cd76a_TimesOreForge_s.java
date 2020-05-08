 package org.yogpstop.tof;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Level;
 
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.StatCollector;
 import net.minecraft.world.World;
 import net.minecraft.world.chunk.IChunkProvider;
 
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.IWorldGenerator;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid = "TimesOreForge", name = "TimesOreForge", version = "@VERSION@")
 @NetworkMod(clientSideRequired = false, serverSideRequired = false)
 public class TimesOreForge implements IWorldGenerator {
 	@SidedProxy(clientSide = "org.yogpstop.tof.ClientProxy", serverSide = "org.yogpstop.tof.CommonProxy")
 	public static CommonProxy proxy;
 
 	public static final List<SettingObject> setting = new ArrayList<SettingObject>();
 	private static File settingF;
 
 	@Mod.EventHandler
 	public void preload(FMLPreInitializationEvent event) {
 		settingF = event.getSuggestedConfigurationFile();
 		setting.clear();
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(settingF));
 			String line;
 			while ((line = br.readLine()) != null) {
 				if (line != "") setting.add(new SettingObject(line));
 			}
 			br.close();
 		} catch (IOException e) {
 			save();
 		}
 		LanguageRegistry.instance().loadLocalization("/org/yogpstop/tof/lang/en_US.lang", "en_US", false);
 		LanguageRegistry.instance().loadLocalization("/org/yogpstop/tof/lang/ja_JP.lang", "ja_JP", false);
 		GameRegistry.registerWorldGenerator(this);
 	}
 
 	@Mod.EventHandler
	public static void postInit(FMLPostInitializationEvent event) {
 		proxy.setKeyHandler();
 	}
 
 	@Override
 	public void generate(Random r, int x, int z, World w, IChunkProvider cg, IChunkProvider cp) {
 		for (SettingObject s : setting)
 			s.generate(w, r, x, z);
 	}
 
 	public static void save() {
 		try {
 			BufferedWriter bw = new BufferedWriter(new FileWriter(settingF));
 			for (int i = 0; i < setting.size(); i++) {
 				setting.get(i).save(bw);
 			}
 			bw.close();
 		} catch (IOException e) {
 			FMLLog.log(Level.SEVERE, "Can't save config");
 		}
 	}
 
 	public static String getname(short blockid, int meta) {
 		StringBuffer sb = new StringBuffer();
 		sb.append(blockid);
 		if (meta != 0) {
 			sb.append(":");
 			sb.append(meta);
 		}
 		sb.append("  ");
 		ItemStack cache = new ItemStack(blockid, 1, meta);
 		if (cache.getItem() == null) {
 			sb.append(StatCollector.translateToLocal("tof.nullblock"));
 		} else if (cache.getDisplayName() == null) {
 			sb.append(StatCollector.translateToLocal("tof.nullname"));
 		} else {
 			sb.append(cache.getDisplayName());
 		}
 		return sb.toString();
 	}
 }
