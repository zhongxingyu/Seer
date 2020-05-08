 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mahn42.anhalter42.building;
 
 import com.mahn42.framework.BlockPosition;
import com.mahn42.framework.BlockRect;
 import com.mahn42.framework.BuildingDB;
 import java.io.File;
 import java.util.ArrayList;
import java.util.logging.Logger;
 import org.bukkit.World;
 
 /**
  *
  * @author andre
  */
 public class SendReceiveDB extends BuildingDB<SendReceiveBuilding> {
     
     public SendReceiveDB() {
         super(SendReceiveBuilding.class);
     }
 
     public SendReceiveDB(World aWorld, File aFile) {
         super(SendReceiveBuilding.class, aWorld, aFile);
     }
     
     ArrayList<SendReceiveBuilding> getReceiver(BlockPosition aEdge1, BlockPosition aEdge2, String aFrequencyName) {
         ArrayList<SendReceiveBuilding> lResult = new ArrayList<SendReceiveBuilding>();
        BlockRect lRect1 = new BlockRect(aEdge1, aEdge2);
         for(SendReceiveBuilding lSR : this) {
             if (lSR.mode == SendReceiveBuilding.Mode.receive) {
                BlockRect lRect2 = new BlockRect(lSR.antenaEdge1, lSR.antenaEdge2);
                Logger.getLogger("x").info("1 " + lRect1 + " " + lRect2);
                if (lRect1.isIntersected(lRect2)) {
                Logger.getLogger("x").info("2 " + lSR.frequencyName);
                     if (aFrequencyName == null || aFrequencyName.equalsIgnoreCase(lSR.frequencyName)) {
                Logger.getLogger("x").info("3");
                         lResult.add(lSR);
                     }
                 }
             }
         }
         return lResult;
     }
 }
