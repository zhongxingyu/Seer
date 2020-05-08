 package denoflionsx.CreeperCollateral.CoreMod.ASM;
 
 import denoflionsx.CreeperCollateral.CoreMod.CCCore;
 import denoflionsx.denLib.CoreMod.ASM.FileRequest;
 import java.io.File;
 import org.apache.commons.io.FileUtils;
 
 public class BukkitLibRequest extends FileRequest {
 
     public BukkitLibRequest() {
        super("commons-io-2.4.jar", "https://dl.dropboxusercontent.com/u/23892866/Downloads/commons-io-2.4.zip");
     }
 
     @Override
     public byte[] transform(String string, String string1, byte[] bytes) {
         if (!string.equals("denoflionsx.CreeperCollateral.CoreMod.CCDummy")){
             return bytes;
         }
         try {
             FileUtils.copyFile(CCCore.location, new File(new File("./plugins"), CCCore.location.getName()));
             CCCore.print("Injected plugin!");
         } catch (Throwable t) {
             t.printStackTrace();
         }
         return bytes;
     }
 }
