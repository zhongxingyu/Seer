 package com.github.ubiquitousspice.dreamdimension.sleephandle;
 
 import com.github.ubiquitousspice.dreamdimension.Util;
 import cpw.mods.fml.common.ITickHandler;
 import cpw.mods.fml.common.ObfuscationReflectionHelper;
 import cpw.mods.fml.common.TickType;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.world.World;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
 
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.HashSet;
 
 public class BedHandler implements ITickHandler
 {
     private static final HashSet<String> sleepers = new HashSet<String>();
 
     @Override
     public void tickStart(EnumSet<TickType> type, Object... tickData)
     {
         // efficiency
         if (sleepers.isEmpty())
             return;
 
         // had better by the world tickType.
         World world = (World) tickData[0];
 
         // avoid client stuff alone.
         if (world.isRemote)
             return;
 
         EntityPlayerMP player;
         ArrayList<String> removeList = new ArrayList<String>();
 
         // loop through sleepers
         for (String user : sleepers)
         {
             player = Util.getPlayerFromUsername(user);
             if (player.isPlayerFullyAsleep())
             {
                // add the dreamer.
                DreamManager.addDreamer(player);
 
                 // set sleeping to false. And wakeup stuff.
                 player.wakeUpPlayer(true, true, true);
 
                 // teleport.
                 Util.moveToDream(player);
 
                 // remove them from list
                 removeList.add(user);
             }
         }
 
         sleepers.removeAll(removeList);
     }
 
     @Override
     public void tickEnd(EnumSet<TickType> type, Object... tickData)
     {
         // nothing here
     }
 
     @Override
     public EnumSet<TickType> ticks()
     {
         return EnumSet.of(TickType.WORLD);
     }
 
     @ForgeSubscribe
     public void catchBedEvent(PlayerSleepInBedEvent event)
     {
         sleepers.add(event.entityPlayer.username);
     }
 
     @Override
     public String getLabel()
     {
         return "PlayerSleepHandler";
     }
 }
