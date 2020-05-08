 package denoflionsx.HDSAC.Mod.Utils;
 
 import denoflionsx.HDSAC.Mod.Config.HDSACTunable;
 
 public class SkinUrlParser {
     
     public static String parseURL(String url){
         String newUrl = "";
         String dir = "";
         if (url != null){
             String[] parse = url.split("/");
             for (String a : parse){
                if (a.equals("MinecraftSkins") || a.equals("MinecraftCloaks")){
                     dir = "/" + a + "/";
                 }
                 if (a.contains(".png")){
                     newUrl = "http://" + HDSACTunable.Server.serverUrl + ":" + String.valueOf(HDSACTunable.Server.serverPort) + dir + a;
                 }
             }
         }
         return newUrl;
     }
     
 }
