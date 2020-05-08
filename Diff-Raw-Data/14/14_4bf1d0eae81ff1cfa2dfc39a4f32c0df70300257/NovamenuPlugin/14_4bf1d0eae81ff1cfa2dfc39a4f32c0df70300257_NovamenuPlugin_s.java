 package com.voidzm.novamenu.asm;
 
 import java.io.File;
 import java.util.Map;
 
 import com.voidzm.novamenu.util.NovamenuConfiguration;
 
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.relauncher.IFMLCallHook;
 import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
 import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
 import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
 
 @TransformerExclusions(value = {"com.voidzm.novamenu.asm", "com.voidzm.novamenu.gui"})
 @MCVersion(value = "1.6.4")
 public class NovamenuPlugin implements IFMLLoadingPlugin, IFMLCallHook {
 
 	public static File location;
 	public static boolean isDevEnvironment = true;
 	public static String minecraftVersion = "1.6.4";
	public static String novamenuVersion = "1.1.3";
 
 	private static NovamenuPlugin instance;
 
 	private static NovamenuConfiguration configuration;
 
 	public NovamenuPlugin() {
 		super();
 		instance = this;
 	}
 
 	public static NovamenuConfiguration getConfiguration() {
 		if(configuration == null) {
 			configuration = new NovamenuConfiguration(new File(Loader.instance().getConfigDir(), "Novamenu.cfg"));
 		}
 		return configuration;
 	}
 
 	public static NovamenuPlugin instance() {
 		if(instance != null) {
 			return instance;
 		}
 		else {
 			return new NovamenuPlugin();
 		}
 	}
 
 	@Override
 	public String[] getLibraryRequestClass() {
 		return null;
 	}
 
 	@Override
 	public String[] getASMTransformerClass() {
 		return new String[] {NovamenuTransformer.class.getName()};
 	}
 
 	@Override
 	public String getModContainerClass() {
 		return NovamenuModContainer.class.getName();
 	}
 
 	@Override
 	public String getSetupClass() {
 		return NovamenuPlugin.class.getName();
 	}
 
 	@Override
 	public void injectData(Map<String, Object> data) {
 		if(data.containsKey("coremodLocation")) location = (File)data.get("coremodLocation");
 		if(data.containsKey("runtimeDeobfuscationEnabled")) isDevEnvironment = !(Boolean)data.get("runtimeDeobfuscationEnabled");
 	}
 
 	@Override
 	public Void call() throws Exception {
 		return null;
 	}
 
 }
