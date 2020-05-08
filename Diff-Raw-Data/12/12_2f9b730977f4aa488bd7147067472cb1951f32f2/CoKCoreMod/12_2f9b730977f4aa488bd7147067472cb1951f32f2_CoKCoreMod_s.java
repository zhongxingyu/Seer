 package de.minestar.cok.preloader;
 
 import java.io.File;
 import java.util.Map;
 
 import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
 
public class CoKCoreMod implements IFMLLoadingPlugin {
 	
 	public static File	location;
 
 	@Override
 	public String[] getLibraryRequestClass() {
 		return null;
 	}
 
 	@Override
 	public String[] getASMTransformerClass() {
 		String[] res = { "de.minestar.cok.preloader.asm.CoKEventAdder" };
 		return res;
 	}
 
 	@Override
 	public String getModContainerClass() {
 		return "de.minestar.cok.preloader.CoKCoreModContainer";
 	}
 
 	@Override
 	public String getSetupClass() {
 		return "de.minestar.cok.preloader.CoKCoreMod";
 	}
 
 	@Override
 	public void injectData(Map<String, Object> data) {
 		if (data.containsKey("coremodLocation"))
 		{
 			location = (File) data.get("coremodLocation");
 		}
 		
 	}
 
 }
