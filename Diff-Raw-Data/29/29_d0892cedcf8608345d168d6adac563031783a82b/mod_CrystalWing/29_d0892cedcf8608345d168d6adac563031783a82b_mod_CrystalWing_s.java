 package net.minecraft.src;
 
 import net.minecraft.client.Minecraft;
 import bspkrs.crystalwing.CrystalWing;
 import bspkrs.util.ModVersionChecker;
 
 public class mod_CrystalWing extends BaseMod
 {
     @MLProp
     public static int         idCrystalWing = 3100;
     @MLProp
     public static int         idBurningWing = 3101;
     @MLProp
     public static int         idBurnedWing  = 3102;
     @MLProp(info = "Number of Crystal Wing uses. Set to 0 for infinite.")
     public static int         uses          = 8;
     @MLProp(info = "Maximum distance for the Burned Wing random teleportation.\n\n**ONLY EDIT WHAT IS BELOW THIS**")
     public static int         teleDistance  = 500;
    
     public static boolean     allowUpdateCheck;
     private ModVersionChecker versionChecker;
     private String            versionURL    = "http://bspk.rs/Minecraft/1.5.1/crystalWing.version";
     private String            mcfTopic      = "http://www.minecraftforum.net/topic/1009577-";
    
     public mod_CrystalWing()
     {
         allowUpdateCheck = mod_bspkrsCore.allowUpdateCheck;
        
         if (allowUpdateCheck)
            versionChecker = new ModVersionChecker(getName(), getVersion(), versionURL, mcfTopic, ModLoader.getLogger());
     }
    
     @Override
     public String getName()
     {
         return "CrystalWing";
     }
    
     @Override
     public String getVersion()
     {
         return "ML " + CrystalWing.VERSION_NUMBER;
     }
    
     @Override
     public String getPriorities()
     {
         return "required-after:mod_bspkrsCore";
     }
    
     @Override
     public void load()
     {
         if (allowUpdateCheck)
             versionChecker.checkVersionWithLogging();
         ModLoader.setInGameHook(this, true, true);
        
         new CrystalWing(false, idCrystalWing, idBurningWing, idBurnedWing, uses, teleDistance);
     }
    
     @Override
     public boolean onTickInGame(float f, Minecraft mc)
     {
         if (allowUpdateCheck)
         {
             if (!versionChecker.isCurrentVersion())
                 for (String msg : versionChecker.getInGameMessage())
                     mc.thePlayer.addChatMessage(msg);
         }
         return false;
     }
 }
