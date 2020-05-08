 package net.minecraft.src;
 
 import net.awairo.tooltipplus.TooltipPlus;
 import net.awairo.tooltipplus.Version;
 import net.minecraft.client.Minecraft;
 
 /**
  * Tooltip Plus loader.
  *
  * @author alalwww
  */
 public class mod_TooltipPlus extends BaseMod
 {
     private static final boolean FMLInstalled;
     static
     {
         boolean b = false;
        for (Class<?> ifClazz : mod_TooltipPlus.class.getInterfaces())
         {
             if (ifClazz.getSimpleName().equals("BaseModProxy"))
             {
                 b = true;
                 break;
             }
         }
 
         FMLInstalled = b;
     }
 
     private TooltipPlus mod;
 
     @Override
     public String getVersion()
     {
         Version.initializeFromModLoader();
         return Version.getVersionString();
     }
 
     @Override
     public void load()
     {
        if (!FMLInstalled)
         {
            mod = new TooltipPlus();
            ModLoader.setInGameHook(this, true, false);
         }
     }
 
     @Override
     public boolean onTickInGame(float time, Minecraft minecraftInstance)
     {
         if (FMLInstalled)
         {
             return false;
         }
         mod.update();
         return true;
     }
 }
