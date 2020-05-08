 /*
   Copyright (C) 2012 Petr Mrázek
 
   This software is provided 'as-is', without any express or implied
   warranty.  In no event will the authors be held liable for any damages
   arising from the use of this software.
 
   Permission is granted to anyone to use this software for any purpose,
   including commercial applications, and to alter it and redistribute it
   freely, subject to the following restrictions:
 
   1. The origin of this software must not be misrepresented; you must not
      claim that you wrote the original software. If you use this software
      in a product, an acknowledgment in the product documentation would be
      appreciated but is not required.
   2. Altered source versions must be plainly marked as such, and must not be
      misrepresented as being the original software.
   3. This notice may not be removed or altered from any source distribution.
 
   Petr Mrázek <peterix@gmail.com>
  */
 
 package peterix.friendsss;
 
 import java.lang.reflect.Field;
 import java.util.logging.Level;
 
 import net.minecraft.src.Block;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.relauncher.ReflectionHelper;
 
@Mod(modid = "Friendsss", name = "Friendsss", version = "0.0.1")
 @NetworkMod(clientSideRequired = false, serverSideRequired = true)
 public class Friendsss
 {
 
     boolean                   NERF_CREEPERS;
     boolean                   NERF_ENDERMEN;
     boolean                   NERF_GHASTS;
     boolean                   PROTECT_VILLAGERS;
 
     // The instance of your mod that Forge uses.
     @Instance("Friendsss")
     public static Friendsss   instance;
 
     // Says where the client and server 'proxy' code is loaded.
     @SidedProxy(clientSide = "peterix.friendsss.client.ClientProxy", serverSide = "peterix.friendsss.CommonProxy")
     public static CommonProxy proxy;
 
     @PreInit
     public void preInit(FMLPreInitializationEvent event)
     {
 
         Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
         try
         {
             cfg.load();
             NERF_CREEPERS = cfg.get(Configuration.CATEGORY_GENERAL, "nerfCreepers", true).getBoolean(true);
             NERF_ENDERMEN = cfg.get(Configuration.CATEGORY_GENERAL, "nerfEndermen", true).getBoolean(true);
             NERF_GHASTS = cfg.get(Configuration.CATEGORY_GENERAL, "nerfGhasts", true).getBoolean(true);
             PROTECT_VILLAGERS = cfg.get(Configuration.CATEGORY_GENERAL, "protectVillagers", true).getBoolean(true);
         }
         catch (Exception e)
         {
             FMLLog.log(Level.SEVERE, e, "Friendsss failed to load config file.");
         }
         finally
         {
             cfg.save();
         }
 
         if (NERF_ENDERMEN)
         {
             Class endermanClass = net.minecraft.src.EntityEnderman.class;
             try
             {
                 // steal only flowers. flowers are pretty :D
                 boolean[] carriableBlocks = new boolean[4096];
                 carriableBlocks[Block.plantYellow.blockID] = true;
                 carriableBlocks[Block.plantRed.blockID] = true;
                 Field carriableBlocksField = ReflectionHelper.findField(endermanClass, "carriableBlocks");
                 carriableBlocksField.set(null, carriableBlocks);
                 FMLLog.log(Level.INFO, "Sucessfully nerfed endermen.");
 
             }
             catch (Exception e)
             {
                 FMLLog.log(Level.WARNING, "Endermen nerf failed:");
                 e.printStackTrace();
             }
         }
         if (NERF_CREEPERS)
         {
             // nerf them green guys
             try
             {
                 MinecraftForge.EVENT_BUS.register(new CreeperUpdateEvent());
                 FMLLog.log(Level.INFO, "Sucessfully nerfed creepers.");
 
             }
             catch (Exception e)
             {
                 FMLLog.log(Level.WARNING, "Creeper nerf failed:");
                 e.printStackTrace();
             }
         }
         if (NERF_GHASTS)
         {
             // nerf ghasts
             try
             {
                 MinecraftForge.EVENT_BUS.register(new GhastUpdateEvent());
                 FMLLog.log(Level.INFO, "Sucessfully nerfed ghasts.");
             }
             catch (Exception e)
             {
                 FMLLog.log(Level.WARNING, "Ghast nerf failed:");
                 e.printStackTrace();
             }
         }
         if (PROTECT_VILLAGERS)
         {
             try
             {
                 MinecraftForge.EVENT_BUS.register(new VillagerTargettedEvent());
                 FMLLog.log(Level.INFO, "Now protecting villagers from attacks.");
             }
             catch (Exception e)
             {
                 FMLLog.log(Level.WARNING, "Unable to protect villagers...");
                 e.printStackTrace();
             }
         }
     }
 }
