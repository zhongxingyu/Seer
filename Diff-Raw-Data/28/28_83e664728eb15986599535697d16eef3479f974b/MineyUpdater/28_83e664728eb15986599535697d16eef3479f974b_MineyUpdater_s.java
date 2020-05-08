 package net.medsouz.miney.updater;
 
 import java.io.File;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 
import net.medsouz.miney.MineyClientInterface;
 import net.minecraft.client.Minecraft;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 @Mod(modid = "Miney", name = "Miney", version = "0.1")
 public class MineyUpdater {
 	@Instance
 	public static MineyUpdater instance;
 
 	public static UpdaterThread updater;
 	public static boolean updateFailed = false;
 
	public static MineyClientInterface client;

 	@Init
 	public void init(FMLInitializationEvent evt) {
 		TickRegistry.registerTickHandler(new UpdateCheckTick(), Side.CLIENT);
 		TickRegistry.registerTickHandler(new RenderTick(), Side.CLIENT);
 		File mineyDir = new File(Minecraft.getMinecraftDir(), "Miney/");
 		if (!mineyDir.exists()) {
 			mineyDir.mkdir();
 		}
 		String localVer = VersionManager.checkLocalMineyVersion();
 		String remoteVer = VersionManager.checkRemoteMineyVersion();
 		File client = new File(mineyDir, "client.zip");
 		if (!remoteVer.equals("null")) {
 			if (localVer.equals(remoteVer)) {
 				MineyUpdater.loadClient();
 			} else {
 				updater = new UpdaterThread(client, VersionManager.clientURL);
 				Thread updateThread = new Thread(updater);
 				updateThread.setName("Miney Updater");
 				updateThread.start();
 			}
 		} else {
 			updateFailed = true;
 		}
 	}
 
 	public static void loadClient() {
 		try {
			URLClassLoader loader = new URLClassLoader(new URL[] {new File(Minecraft.getMinecraftDir(), "Miney/client.zip").toURI().toURL()}, MineyUpdater.class.getClassLoader());
			Class clazz = Class.forName("net.medsouz.miney.client.MineyClient", true, loader);
			client = (MineyClientInterface) clazz.newInstance();
 			Method method = clazz.getDeclaredMethod("init");
 			method.invoke(client);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
