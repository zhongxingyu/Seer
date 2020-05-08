 package denoflionsx.DenPipes.Core;
 
 import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
 import java.util.Map;
 
@IFMLLoadingPlugin.MCVersion(value = "1.6.2")
 public class DenPipesCoreMod implements IFMLLoadingPlugin {
 
     @Override
     public String[] getLibraryRequestClass() {
         return null;
     }
 
     @Override
     public String[] getASMTransformerClass() {
         return new String[]{"denoflionsx.DenPipes.Core.ASM.BCHooks"};
     }
 
     @Override
     public String getModContainerClass() {
         return null;
     }
 
     @Override
     public String getSetupClass() {
         return null;
     }
 
     @Override
     public void injectData(Map<String, Object> data) {
     }
 }
