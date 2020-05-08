 package mod.culegooner;
 
 import java.io.File;
 import java.util.Map;
 
 import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
 
 @MCVersion(value = "1.6.1")
 public class CBFMLLoadingPlugin implements cpw.mods.fml.relauncher.IFMLLoadingPlugin {
 
 	
 	public static File location;
 	
 	@Override
 	public String[] getLibraryRequestClass() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String[] getASMTransformerClass() {
		//return new String[]{CBClassTransformer.class.getName()};
		System.out.println("***************WOOOOOOOHOOOOOOOOOOO Inside getASMTransformerClass" );
		return new String[] { "mod.culegooner.CBClassTransformer" };
 	}
 
 	@Override
 	public String getModContainerClass() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String getSetupClass() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void injectData(Map<String, Object> data) {
 		location = (File) data.get("coremodLocation");
System.out.println("***************WOOOOOOOHOOOOOOOOOOO : location.getName: " +location.getName());
 	}
 
 }
