 package denoflionsx.CreeperCollateral.CoreMod;
 
 import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
 import denoflionsx.CreeperCollateral.CoreMod.ASM.BukkitLibRequest;
 import denoflionsx.denLib.Lib.denLib;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Map;
 
 public class CCCore implements IFMLLoadingPlugin {
 
     public static boolean isBukkit = false;
     public static File location;
 
     //@Override
     public String[] getLibraryRequestClass() {
         return null;
     }
 
     @Override
     public String[] getASMTransformerClass() {
         if (denLib.BukkitHelper.isBukkit()) {
             print("Bukkit env detected.");
             isBukkit = true;
         }
         ArrayList<String> trans = new ArrayList();
         if (isBukkit) {
             print("Using bukkit configuration.");
             Mode.BUKKIT.setupMode(trans);
         } else {
             print("Using vanilla configuration.");
             Mode.VANILLA.setupMode(trans);
         }
         return trans.toArray(new String[trans.size()]);
     }
 
     @Override
     public String getModContainerClass() {
         return null;
     }
 
     @Override
     public String getSetupClass() {
         return null;
     }
 
     public static void print(String msg) {
        System.out.println("[@NAME@Core]: " + msg);
     }
 
     @Override
     public void injectData(Map<String, Object> data) {
         location = (File) data.get("coremodLocation");
         CCDummy dummy = new CCDummy();
         dummy.doNothing();
     }
 
     public static enum Mode {
 
         VANILLA,
         BUKKIT;
 
         public void setupMode(ArrayList<String> trans) {
             switch (this.ordinal()) {
                 case 0:
                     break;
                 case 1:
                     trans.add(BukkitLibRequest.class.getName());
                     break;
             }
         }
     }
 }
