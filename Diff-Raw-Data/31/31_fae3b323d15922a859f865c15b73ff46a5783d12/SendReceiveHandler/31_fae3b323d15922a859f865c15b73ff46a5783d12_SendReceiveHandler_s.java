 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mahn42.anhalter42.building;
 
 import com.mahn42.framework.BlockPosition;
 import com.mahn42.framework.Building;
 import com.mahn42.framework.BuildingDB;
 import com.mahn42.framework.BuildingHandlerBase;
 import java.util.ArrayList;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author andre
  */
 public class SendReceiveHandler extends BuildingHandlerBase {
     
     public BuildingPlugin plugin;
     
     public SendReceiveHandler(BuildingPlugin aPlugin) {
         plugin = aPlugin;
     }
             
     @Override
     public JavaPlugin getPlugin() {
         return plugin;
     }
     
     @Override
     public Building insert(Building aBuilding) {
         SendReceiveDB lDB = (SendReceiveDB) getDB(aBuilding.world);
         SendReceiveBuilding lSR = new SendReceiveBuilding();
         lSR.cloneFrom(aBuilding);
         Sign lSign = (Sign)lSR.getBlock("sign").position.getBlock(lSR.world).getState();
        lSR.frequencyName = lSign.getLine(0);
         BlockPosition lbase = lSR.getBlock("antenabase").position;
         BlockPosition ltop = lSR.getBlock("antenatop").position;
         int lHigh = ltop.y - lbase.y + 1;
         int lRange = (int) (8 + Math.pow(2, lHigh));
         lSR.antenaEdge1.cloneFrom(lbase);
         lSR.antenaEdge2.cloneFrom(lbase);
         lSR.antenaEdge1.add(-lRange, -lRange, -lRange);
         lSR.antenaEdge2.add(lRange, lRange, lRange);
         plugin.getLogger().info("strength " + lRange);
         if (lSR.description.name.contains("Receiver")) {
             lSR.mode = SendReceiveBuilding.Mode.receive;
         }
         lDB.addRecord(lSR);
         return lSR;
     }
 
     @Override
     public BuildingDB getDB(World aWorld) {
         return plugin.SendReceiveDBs.getDB(aWorld);
     }
 
     @Override
     public boolean redstoneChanged(BlockRedstoneEvent aEvent, Building aBuilding) {
         SendReceiveBuilding lSR = (SendReceiveBuilding)aBuilding;
         boolean lSigOn = aEvent.getNewCurrent() > 0;
         SendReceiveDB lDB = (SendReceiveDB) getDB(lSR.world);
         ArrayList<SendReceiveBuilding> lReceivers = lDB.getReceiver(lSR.antenaEdge1, lSR.antenaEdge2, lSR.frequencyName);
        plugin.getLogger().info("redstone signal " + lSigOn + " found " + lReceivers.size());
         for(SendReceiveBuilding lReceiver : lReceivers) {
             Block lBlock = lReceiver.getBlock("lever").position.getBlock(lSR.world);
             byte lData = lBlock.getData();
             if (lSigOn) {
                 lData |= 0x08;
             } else {
                 lData &= 0xF7;
             }
             lBlock.setData(lData);
         }
         return true;
     }
 }
