 package denoflionsx.HDSAC.Mod.Utils;
 
 import denoflionsx.HDSAC.Mod.Config.HDSACTunable;
import denoflionsx.HDSAC.Mod.HDSACMod;
 
 public class SkinUrlParser {
     
     public static String parseURL(String url){
         String newUrl = "";
         String dir = "";
         if (url != null){
            HDSACMod.Proxy.print("Caught image request: " + url + ". Parsing...");
             String[] parse = url.split("/");
             for (String a : parse){
                if (a.equals("MinecraftSkins") || a.equals("MinecraftCloaks") || a.equals("capes")){
                    if (a.equals("capes")){
                        a = "MinecraftCloaks";
                    }
                     dir = "/" + a + "/";
                 }
                 if (a.contains(".png")){
                     newUrl = "http://" + HDSACTunable.Server.serverUrl + ":" + String.valueOf(HDSACTunable.Server.serverPort) + dir + a;
                 }
             }
         }
        HDSACMod.Proxy.print("Redirected URL: " + newUrl + ".");
         return newUrl;
     }
     
 }
