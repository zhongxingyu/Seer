 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mahn42.anhalter42.building;
 
 import com.mahn42.framework.BlockPosition;
 import com.mahn42.framework.BuildingDB;
 import java.io.File;
 import java.util.ArrayList;
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
         for(SendReceiveBuilding lSR : this) {
             if (lSR.mode == SendReceiveBuilding.Mode.receive) {
                if (lSR.antenaEdge1.isBetween(aEdge1, aEdge2) || lSR.antenaEdge2.isBetween(aEdge1, aEdge2)) {
                     if (aFrequencyName == null || aFrequencyName.equalsIgnoreCase(lSR.frequencyName)) {
                         lResult.add(lSR);
                     }
                 }
             }
         }
         return lResult;
     }
 }
