 package denoflionsx.HDSAC.CoreMod;
 
 import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
 import denoflionsx.HDSAC.CoreMod.ASM.ASMAbstractClientPlayer;
 import denoflionsx.HDSAC.CoreMod.ASM.ASMImageBufferDownload;
 import denoflionsx.HDSAC.CoreMod.ASM.DBRequest;
 import java.io.File;
 import java.util.Map;
 
@IFMLLoadingPlugin.MCVersion(value = "1.6.4")
 public class HDSACCore implements IFMLLoadingPlugin {
 
     public static File location;
 
     @Override
     public String[] getASMTransformerClass() {
         return new String[]{DBRequest.class.getName(), ASMAbstractClientPlayer.class.getName(), ASMImageBufferDownload.class.getName()};
     }
 
     @Override
     public String[] getLibraryRequestClass() {
         return null;
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
         location = (File) data.get("coremodLocation");
     }
 }
