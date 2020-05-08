 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mahn42.anhalter42.gates;
 
 import com.mahn42.framework.BlockPosition;
 import com.mahn42.framework.Framework;
 import com.mahn42.framework.SyncBlockList;
 import com.mahn42.framework.WorldLineWalk;
 import java.util.logging.Logger;
 import org.bukkit.Effect;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.inventory.ItemStack;
 
 /**
  *
  * @author andre
  */
 public class GateTask implements Runnable {
 
     public Gates plugin;
     public GateBuilding gate;
     public boolean open = true;
     public boolean emergency = false;
     public int taskId;
     
     protected boolean fInRun = false;
     protected boolean fInit = false;
     protected BlockPosition fLeftBottom;
     protected BlockPosition fLeftTop;
     protected BlockPosition fRightBottom;
     protected BlockPosition fRightTop;
     protected int fHeight = 0;
     protected int fWidth = 0;
     protected int fDepth = 0;
     protected int fCount = 0;
     protected boolean fAlongX;
     protected int fDx = 0;
     protected int fDz = 0;
     protected int fFDx = 0;
     protected int fFDz = 0;
     
     public GateTask(Gates aPlugin) {
         plugin = aPlugin;
     }
     
     public void doEmegency() {
         while (fCount > 0) {
             run();
         }
     }
     
     @Override
     public void run() {
         if (!fInRun) {
             fInRun = true;
             try {
                 if (!fInit) {
                     fLeftBottom = gate.getBlock("DoorHingeLeftBottom").position.clone();
                     fLeftTop = gate.getBlock("DoorHingeLeftTop").position.clone();
                     fRightBottom = gate.getBlock("DoorHingeRightBottom").position.clone();
                     fRightTop = gate.getBlock("DoorHingeRightTop").position.clone();
                     plugin.getServer().getScheduler().runTask(plugin, 
                         new Runnable() {
                             @Override
                             public void run() {
                                 gate.world.playEffect(fLeftBottom.getLocation(gate.world), Effect.DOOR_TOGGLE, 0);
                                 gate.world.playEffect(fRightBottom.getLocation(gate.world), Effect.DOOR_TOGGLE, 0);
                             }
                         }
                     );
                     fHeight = ( fLeftTop.y - fLeftBottom.y ) + 1;
                     if (fLeftTop.x != fRightTop.x) {
                         fWidth = Math.abs( fLeftTop.x - fRightTop.x ) + 1;
                         fAlongX = false;
                         fDx = (fLeftTop.x < fRightTop.x) ? 1 : -1;
                         fDz = 0;
                     } else {
                         fWidth = Math.abs( fLeftTop.z - fRightTop.z ) + 1;
                         fAlongX = true;
                         fDx = 0;
                         fDz = (fLeftTop.z < fRightTop.z) ? 1 : -1;
                     }
                     if (fLeftTop.x != fLeftBottom.x) {
                         fDepth = Math.abs( fLeftTop.x - fLeftBottom.x ) + 1;
                         fFDx = (fLeftTop.x < fLeftBottom.x) ? 1 : -1;
                         fFDz = 0;
                     } else {
                         fDepth = Math.abs( fLeftTop.z - fLeftBottom.z ) + 1;
                         fFDx = 0;
                         fFDz = (fLeftTop.z < fLeftBottom.z) ? 1 : -1;
                     }
                     if (Framework.plugin.isDebugSet("gates")) {
                         Logger.getLogger(getClass().getSimpleName()).info("width=" + fWidth + " heigth=" + fHeight + " depth=" + fDepth);
                         Logger.getLogger(getClass().getSimpleName()).info("dx=" + fDx + " dz=" + fDz + " fdx=" + fFDx + " fdz=" + fFDz);
                         Logger.getLogger(getClass().getSimpleName()).info("lt=" + fLeftTop + " rt=" + fRightTop);
                         Logger.getLogger(getClass().getSimpleName()).info("lb=" + fLeftBottom + " rb=" + fRightBottom);
                     }
                     switch(gate.mode) {
                         case UpDown:
                         case DownUp:
                             if (open) {
                                 fCount = fHeight - 1;
                                 gate.openCount = 0;
                             } else {
                                 fCount = gate.openCount;
                                 fLeftTop.y -= ( fHeight - fCount ) - 1;
                                 fRightTop.y -= ( fHeight - fCount ) - 1;
                                 fLeftBottom.y += ( fHeight - fCount ) - 1;
                                 fRightBottom.y += ( fHeight - fCount ) - 1;
                             }
                             break;
                         case LeftRight:
                             if (open) {
                                 fCount = fWidth - 1;
                                 gate.openCount = 0;
                             } else {
                                 fCount = gate.openCount;
                                 fLeftTop.add(((fWidth - fCount) - 1)*fDx,0,((fWidth - fCount) - 1)*fDz);
                                 fRightTop.add(((fWidth - fCount) - 1)*fDx,0,((fWidth - fCount) - 1)*fDz);
                                 fLeftBottom.add(((fWidth - fCount) - 1)*fDx,0,((fWidth - fCount) - 1)*fDz);
                                 fRightBottom.add(((fWidth - fCount) - 1)*fDx,0,((fWidth - fCount) - 1)*fDz);
                             }
                             break;
                         case RightLeft:
                             if (open) {
                                 fCount = fWidth - 1;
                                 gate.openCount = 0;
                             } else {
                                 fCount = gate.openCount;
                                 fLeftTop.add(-((fWidth - fCount) - 1)*fDx,0,-((fWidth - fCount) - 1)*fDz);
                                 fRightTop.add(-((fWidth - fCount) - 1)*fDx,0,-((fWidth - fCount) - 1)*fDz);
                                 fLeftBottom.add(-((fWidth - fCount) - 1)*fDx,0,-((fWidth - fCount) - 1)*fDz);
                                 fRightBottom.add(-((fWidth - fCount) - 1)*fDx,0,-((fWidth - fCount) - 1)*fDz);
                             }
                             break;
                         case FlatLeftRight:
                             if (open) {
                                 fCount = fWidth - 1;
                                 gate.openCount = 0;
                             } else {
                                 fCount = gate.openCount;
                                 fLeftTop.add(((fWidth - fCount) - 1)*fDx,0,((fWidth - fCount) - 1)*fDz);
                                 fRightTop.add(((fWidth - fCount) - 1)*fDx,0,((fWidth - fCount) - 1)*fDz);
                                 fLeftBottom.add(((fWidth - fCount) - 1)*fDx,0,((fWidth - fCount) - 1)*fDz);
                                 fRightBottom.add(((fWidth - fCount) - 1)*fDx,0,((fWidth - fCount) - 1)*fDz);
                             }
                             break;
                         case FlatRightLeft:
                             if (open) {
                                 fCount = fWidth - 1;
                                 gate.openCount = 0;
                             } else {
                                 fCount = gate.openCount;
                                 fLeftTop.add(-((fWidth - fCount) - 1)*fDx,0,-((fWidth - fCount) - 1)*fDz);
                                 fRightTop.add(-((fWidth - fCount) - 1)*fDx,0,-((fWidth - fCount) - 1)*fDz);
                                 fLeftBottom.add(-((fWidth - fCount) - 1)*fDx,0,-((fWidth - fCount) - 1)*fDz);
                                 fRightBottom.add(-((fWidth - fCount) - 1)*fDx,0,-((fWidth - fCount) - 1)*fDz);
                             }
                             break;
                     }
                     fInit = true;
                 }
                 if (open) {
                     doOpen();
                 } else {
                     doClose();
                 }
             } finally {
                 fInRun = false;
             }
         }
     }
     
     protected void doOpen() {
         switch(gate.mode) {
             case UpDown: doUpDownOpen(); break;
             case DownUp: doDownUpOpen(); break;
             case LeftRight: doLeftRightOpen(); break;
             case RightLeft: doRightLeftOpen(); break;
             case FlatLeftRight: doFlatLeftRightOpen(); break;
             case FlatRightLeft: doFlatRightLeftOpen(); break;
             default: doUpDownOpen(); break;
         }
     }
 
     protected void doClose() {
         switch(gate.mode) {
             case UpDown: doUpDownClose(); break;
             case DownUp: doDownUpClose(); break;
             case LeftRight: doLeftRightClose(); break;
             case RightLeft: doRightLeftClose(); break;
             case FlatLeftRight: doFlatLeftRightClose(); break;
             case FlatRightLeft: doFlatRightLeftClose(); break;
             default: doUpDownClose(); break;
         }
     }
     
     protected boolean isAIRLike(Material aMat) {
         return aMat.equals(Material.AIR) || aMat.equals(Material.WATER) || aMat.equals(Material.STATIONARY_WATER) || aMat.equals(Material.LAVA) || aMat.equals(Material.STATIONARY_LAVA);
     }
 
 /*************************************
  **************** UpDown ************* 
  *************************************/
     
     protected void doUpDownOpen() {
         if (fCount > 0) {
             BlockPosition lLeftTop = fLeftTop.clone();
             BlockPosition lRightTop = fRightTop.clone();
             lLeftTop.add(0,1,0);
             lRightTop.add(0,1,0);
             //check if top line is empty
             boolean lTopLineEmpty = true;
             for(BlockPosition lPos : new WorldLineWalk(lLeftTop, lRightTop)) {
                 if (!lPos.equals(lLeftTop) && !lPos.equals(lRightTop)) {
                     if (!isAIRLike(lPos.getBlockType(gate.world))) {
                         lTopLineEmpty = false;
                         break;
                     }
                 }
             }
             if (lTopLineEmpty) {
                 lLeftTop.cloneFrom(fLeftTop);
                 lRightTop.cloneFrom(fRightTop);
                 SyncBlockList lList = new SyncBlockList(gate.world);
                 for(int lDy = 0; lDy < fHeight; lDy++) {
                     for(BlockPosition lPos : new WorldLineWalk(lLeftTop, lRightTop)) {
                         if (!lPos.equals(lLeftTop) && !lPos.equals(lRightTop)) {
                             Block lFrom = lPos.getBlock(gate.world);
                             BlockPosition lTo = lPos.clone();
                             lTo.add(0,1,0);
                             lList.add(lTo, lFrom.getType(), lFrom.getData(), true);
                         }
                     }
                     lLeftTop.y--;
                     lRightTop.y--;
                 }
                 if (fHeight > 0) {
                     lLeftTop.y++;
                     lRightTop.y++;
                 }
                 for(BlockPosition lPos : new WorldLineWalk(lLeftTop, lRightTop)) {
                     if (!lPos.equals(lLeftTop) && !lPos.equals(lRightTop)) {
                         lList.add(lPos, Material.AIR, (byte)0, true);
                     }
                 }
                 lList.execute();
                 fLeftTop.add(0,1,0);
                 fRightTop.add(0,1,0);
                 fCount--;
                 gate.open = true;
                 gate.openCount++;
             } else {
                 plugin.stopGateTask(this);
             }
         } else {
             plugin.stopGateTask(this);
         }
     }
     
     protected void doUpDownClose() {
         if (fCount > 0) {
             BlockPosition lLeftTop = fLeftTop.clone();
             BlockPosition lRightTop = fRightTop.clone();
             lLeftTop.add(0,-1,0);
             lRightTop.add(0,-1,0);
             //check if bottom line has items to drop
             for(BlockPosition lPos : new WorldLineWalk(lLeftTop, lRightTop)) {
                 if (!lPos.equals(lLeftTop) && !lPos.equals(lRightTop)) {
                     Block lBlock = lPos.getBlock(gate.world);
                     Material lMat = lBlock.getType();
                     if (!lMat.equals(Material.AIR)) {
                         if (!(lMat.equals(Material.WATER)
                                 || lMat.equals(Material.LAVA)
                                 || lMat.equals(Material.STATIONARY_LAVA)
                                 || lMat.equals(Material.STATIONARY_WATER))) {
                             ItemStack lStack = new ItemStack(lMat, 1, (short)0, lBlock.getData());
                             gate.world.dropItemNaturally(lPos.getLocation(gate.world), lStack);
                         }
                     }
                 }
             }
             lLeftTop.cloneFrom(fLeftTop);
             lRightTop.cloneFrom(fRightTop);
             SyncBlockList lList = new SyncBlockList(gate.world);
             for(int lDy = 0; lDy < fHeight; lDy++) {
                 for(BlockPosition lPos : new WorldLineWalk(lLeftTop, lRightTop)) {
                     if (!lPos.equals(lLeftTop) && !lPos.equals(lRightTop)) {
                         Block lFrom = lPos.getBlock(gate.world);
                         BlockPosition lTo = lPos.clone();
                         lTo.add(0,-1,0);
                         lList.add(lTo, lFrom.getType(), lFrom.getData(), true);
                     }
                 }
                 lLeftTop.y++;
                 lRightTop.y++;
             }
             if (fHeight > 0) {
                 lLeftTop.y--;
                 lRightTop.y--;
             }
             for(BlockPosition lPos : new WorldLineWalk(lLeftTop, lRightTop)) {
                 if (!lPos.equals(lLeftTop) && !lPos.equals(lRightTop)) {
                     lList.add(lPos, Material.AIR, (byte)0, true);
                 }
             }
             lList.moveDependantBlocksAtEnd();
             lList.execute();
             fLeftTop.add(0,-1,0);
             fRightTop.add(0,-1,0);
             fCount--;
             gate.open = false;
             gate.openCount = 0;
         } else {
             plugin.stopGateTask(this);
         }
     }
 
 /*************************************
  **************** DownUp ************* 
  *************************************/
 
     protected void doDownUpOpen() {
         if (fCount > 0) {
             BlockPosition lLeftBottom = fLeftBottom.clone();
             BlockPosition lRightBottom = fRightBottom.clone();
             lLeftBottom.add(0,-1,0);
             lRightBottom.add(0,-1,0);
             //check if bottom line is empty
             boolean lBottomLineEmpty = true;
             for(BlockPosition lPos : new WorldLineWalk(lLeftBottom, lRightBottom)) {
                 if (!lPos.equals(lLeftBottom) && !lPos.equals(lRightBottom)) {
                     if (!isAIRLike(lPos.getBlockType(gate.world))) {
                         lBottomLineEmpty = false;
                         break;
                     }
                 }
             }
             if (lBottomLineEmpty) {
                 lLeftBottom.cloneFrom(fLeftBottom);
                 lRightBottom.cloneFrom(fRightBottom);
                 SyncBlockList lList = new SyncBlockList(gate.world);
                 for(int lDy = 0; lDy < fHeight; lDy++) {
                     for(BlockPosition lPos : new WorldLineWalk(lLeftBottom, lRightBottom)) {
                         if (!lPos.equals(lLeftBottom) && !lPos.equals(lRightBottom)) {
                             Block lFrom = lPos.getBlock(gate.world);
                             BlockPosition lTo = lPos.clone();
                             lTo.add(0,-1,0);
                             lList.add(lTo, lFrom.getType(), lFrom.getData(), true);
                         }
                     }
                     lLeftBottom.y++;
                     lRightBottom.y++;
                 }
                 if (fHeight > 0) {
                     lLeftBottom.y--;
                     lRightBottom.y--;
                 }
                 for(BlockPosition lPos : new WorldLineWalk(lLeftBottom, lRightBottom)) {
                     if (!lPos.equals(lLeftBottom) && !lPos.equals(lRightBottom)) {
                         lList.add(lPos, Material.AIR, (byte)0, true);
                     }
                 }
                 lList.execute();
                 fLeftBottom.add(0,-1,0);
                 fRightBottom.add(0,-1,0);
                 fCount--;
                 gate.open = true;
                 gate.openCount++;
             } else {
                 plugin.stopGateTask(this);
             }
         } else {
             plugin.stopGateTask(this);
         }
     }
 
     protected void doDownUpClose() {
         if (fCount > 0) {
             BlockPosition lLeftBottom = fLeftBottom.clone();
             BlockPosition lRightBottom = fRightBottom.clone();
             lLeftBottom.add(0,1,0);
             lRightBottom.add(0,1,0);
             //check if top line has items to drop
             for(BlockPosition lPos : new WorldLineWalk(lLeftBottom, lRightBottom)) {
                 if (!lPos.equals(lLeftBottom) && !lPos.equals(lRightBottom)) {
                     Block lBlock = lPos.getBlock(gate.world);
                     Material lMat = lBlock.getType();
                     if (!lMat.equals(Material.AIR)) {
                         if (!(lMat.equals(Material.WATER)
                                 || lMat.equals(Material.LAVA)
                                 || lMat.equals(Material.STATIONARY_LAVA)
                                 || lMat.equals(Material.STATIONARY_WATER))) {
                             ItemStack lStack = new ItemStack(lMat, 1, (short)0, lBlock.getData());
                             gate.world.dropItemNaturally(lPos.getLocation(gate.world), lStack);
                         }
                     }
                 }
             }
             lLeftBottom.cloneFrom(fLeftBottom);
             lRightBottom.cloneFrom(fRightBottom);
             SyncBlockList lList = new SyncBlockList(gate.world);
             for(int lDy = 0; lDy < fHeight; lDy++) {
                 for(BlockPosition lPos : new WorldLineWalk(lLeftBottom, lRightBottom)) {
                     if (!lPos.equals(lLeftBottom) && !lPos.equals(lRightBottom)) {
                         Block lFrom = lPos.getBlock(gate.world);
                         BlockPosition lTo = lPos.clone();
                         lTo.add(0,1,0);
                         lList.add(lTo, lFrom.getType(), lFrom.getData(), true);
                     }
                 }
                 lLeftBottom.y--;
                 lRightBottom.y--;
             }
             if (fHeight > 0) {
                 lLeftBottom.y++;
                 lRightBottom.y++;
             }
             for(BlockPosition lPos : new WorldLineWalk(lLeftBottom, lRightBottom)) {
                 if (!lPos.equals(lLeftBottom) && !lPos.equals(lRightBottom)) {
                     lList.add(lPos, Material.AIR, (byte)0, true);
                 }
             }
             lList.execute();
             fLeftBottom.add(0,1,0);
             fRightBottom.add(0,1,0);
             fCount--;
             gate.open = false;
             gate.openCount = 0;
         } else {
             plugin.stopGateTask(this);
         }
     }
 
 /*************************************
  **************** LeftRight ************* 
  *************************************/
     
     protected void doLeftRightOpen() {
         if (fCount > 0) {
             BlockPosition lLeftTop = fLeftTop.clone();
             BlockPosition lLeftBottom = fLeftBottom.clone();
             lLeftTop.add(-fDx,0,-fDz);
             lLeftBottom.add(-fDx,0,-fDz);
             //check if left line is empty
             boolean lLeftLineEmpty = true;
             for(BlockPosition lPos : new WorldLineWalk(lLeftTop, lLeftBottom)) {
                 if (!lPos.equals(lLeftTop) && !lPos.equals(lLeftBottom)) {
                     if (!isAIRLike(lPos.getBlockType(gate.world))) {
                         lLeftLineEmpty = false;
                         break;
                     }
                 }
             }
             if (lLeftLineEmpty) {
                 lLeftTop.cloneFrom(fLeftTop);
                 lLeftBottom.cloneFrom(fLeftBottom);
                 SyncBlockList lList = new SyncBlockList(gate.world);
                 for(int lDy = 0; lDy < fWidth; lDy++) {
                     for(BlockPosition lPos : new WorldLineWalk(lLeftTop, lLeftBottom)) {
                         if (!lPos.equals(lLeftTop) && !lPos.equals(lLeftBottom)) {
                             Block lFrom = lPos.getBlock(gate.world);
                             BlockPosition lTo = lPos.clone();
                             lTo.add(-fDx,0,-fDz);
                             lList.add(lTo, lFrom.getType(), lFrom.getData(), true);
                         }
                     }
                     lLeftTop.add(fDx,0,fDz);
                     lLeftBottom.add(fDx,0,fDz);
                 }
                 if (fWidth > 0) {
                     lLeftTop.add(-fDx,0,-fDz);
                     lLeftBottom.add(-fDx,0,-fDz);
                 }
                 for(BlockPosition lPos : new WorldLineWalk(lLeftTop, lLeftBottom)) {
                     if (!lPos.equals(lLeftTop) && !lPos.equals(lLeftBottom)) {
                         lList.add(lPos, Material.AIR, (byte)0, true);
                     }
                 }
                 lList.execute();
                 fLeftTop.add(-fDx,0,-fDz);
                 fLeftBottom.add(-fDx,0,-fDz);
                 fCount--;
                 gate.open = true;
                 gate.openCount++;
             } else {
                 plugin.stopGateTask(this);
             }
         } else {
             plugin.stopGateTask(this);
         }
     }
     
     protected void doLeftRightClose() {
         if (fCount > 0) {
             BlockPosition lLeftTop = fLeftTop.clone();
             BlockPosition lLeftBottom = fLeftBottom.clone();
             lLeftTop.add(fDx,0,fDz);
             lLeftBottom.add(fDx,0,fDz);
             //check if bottom line has items to drop
             for(BlockPosition lPos : new WorldLineWalk(lLeftTop, lLeftBottom)) {
                 if (!lPos.equals(lLeftTop) && !lPos.equals(lLeftBottom)) {
                     Block lBlock = lPos.getBlock(gate.world);
                     Material lMat = lBlock.getType();
                     if (!lMat.equals(Material.AIR)) {
                         if (!(lMat.equals(Material.WATER)
                                 || lMat.equals(Material.LAVA)
                                 || lMat.equals(Material.STATIONARY_LAVA)
                                 || lMat.equals(Material.STATIONARY_WATER))) {
                             ItemStack lStack = new ItemStack(lMat, 1, (short)0, lBlock.getData());
                             gate.world.dropItemNaturally(lPos.getLocation(gate.world), lStack);
                         }
                     }
                 }
             }
             lLeftTop.cloneFrom(fLeftTop);
             lLeftBottom.cloneFrom(fLeftBottom);
             SyncBlockList lList = new SyncBlockList(gate.world);
             for(int lDy = 0; lDy < fWidth; lDy++) {
                 for(BlockPosition lPos : new WorldLineWalk(lLeftTop, lLeftBottom)) {
                     if (!lPos.equals(lLeftTop) && !lPos.equals(lLeftBottom)) {
                         Block lFrom = lPos.getBlock(gate.world);
                         BlockPosition lTo = lPos.clone();
                         lTo.add(fDx,0,fDz);
                         lList.add(lTo, lFrom.getType(), lFrom.getData(), true);
                     }
                 }
                 lLeftTop.add(-fDx,0,-fDz);
                 lLeftBottom.add(-fDx,0,-fDz);
             }
             if (fHeight > 0) {
                 lLeftTop.add(fDx,0,fDz);
                 lLeftBottom.add(fDx,0,fDz);
             }
             for(BlockPosition lPos : new WorldLineWalk(lLeftTop, lLeftBottom)) {
                 if (!lPos.equals(lLeftTop) && !lPos.equals(lLeftBottom)) {
                     lList.add(lPos, Material.AIR, (byte)0, true);
                 }
             }
             lList.execute();
             fLeftTop.add(fDx,0,fDz);
             fLeftBottom.add(fDx,0,fDz);
             fCount--;
             gate.open = false;
             gate.openCount = 0;
         } else {
             plugin.stopGateTask(this);
         }
     }
     
 /*************************************
  **************** RightLeft ********** 
  *************************************/
 
     protected void doRightLeftOpen() {
         if (fCount > 0) {
             BlockPosition lRightTop = fRightTop.clone();
             BlockPosition lRightBottom = fRightBottom.clone();
             lRightTop.add(fDx,0,fDz);
             lRightBottom.add(fDx,0,fDz);
             //check if left line is empty
             boolean lLeftLineEmpty = true;
             for(BlockPosition lPos : new WorldLineWalk(lRightTop, lRightBottom)) {
                 if (!lPos.equals(lRightTop) && !lPos.equals(lRightBottom)) {
                     if (!isAIRLike(lPos.getBlockType(gate.world))) {
                         lLeftLineEmpty = false;
                         break;
                     }
                 }
             }
             if (lLeftLineEmpty) {
                 lRightTop.cloneFrom(fRightTop);
                 lRightBottom.cloneFrom(fRightBottom);
                 SyncBlockList lList = new SyncBlockList(gate.world);
                 for(int lDy = 0; lDy < fWidth; lDy++) {
                     for(BlockPosition lPos : new WorldLineWalk(lRightTop, lRightBottom)) {
                         if (!lPos.equals(lRightTop) && !lPos.equals(lRightBottom)) {
                             Block lFrom = lPos.getBlock(gate.world);
                             BlockPosition lTo = lPos.clone();
                             lTo.add(fDx,0,fDz);
                             lList.add(lTo, lFrom.getType(), lFrom.getData(), true);
                         }
                     }
                     lRightTop.add(-fDx,0,-fDz);
                     lRightBottom.add(-fDx,0,-fDz);
                 }
                 if (fWidth > 0) {
                     lRightTop.add(fDx,0,fDz);
                     lRightBottom.add(fDx,0,fDz);
                 }
                 for(BlockPosition lPos : new WorldLineWalk(lRightTop, lRightBottom)) {
                     if (!lPos.equals(lRightTop) && !lPos.equals(lRightBottom)) {
                         lList.add(lPos, Material.AIR, (byte)0, true);
                     }
                 }
                 lList.execute();
                 fRightTop.add(fDx,0,fDz);
                 fRightBottom.add(fDx,0,fDz);
                 fCount--;
                 gate.open = true;
                 gate.openCount++;
             } else {
                 plugin.stopGateTask(this);
             }
         } else {
             plugin.stopGateTask(this);
         }
     }    
 
     protected void doRightLeftClose() {
         if (fCount > 0) {
             BlockPosition lRightTop = fRightTop.clone();
             BlockPosition lRightBottom = fRightBottom.clone();
             lRightTop.add(-fDx,0,-fDz);
             lRightBottom.add(-fDx,0,-fDz);
             //check if bottom line has items to drop
             for(BlockPosition lPos : new WorldLineWalk(lRightTop, lRightBottom)) {
                 if (!lPos.equals(lRightTop) && !lPos.equals(lRightBottom)) {
                     Block lBlock = lPos.getBlock(gate.world);
                     Material lMat = lBlock.getType();
                     if (!lMat.equals(Material.AIR)) {
                         if (!(lMat.equals(Material.WATER)
                                 || lMat.equals(Material.LAVA)
                                 || lMat.equals(Material.STATIONARY_LAVA)
                                 || lMat.equals(Material.STATIONARY_WATER))) {
                             ItemStack lStack = new ItemStack(lMat, 1, (short)0, lBlock.getData());
                             gate.world.dropItemNaturally(lPos.getLocation(gate.world), lStack);
                         }
                     }
                 }
             }
             lRightTop.cloneFrom(fRightTop);
             lRightBottom.cloneFrom(fRightBottom);
             SyncBlockList lList = new SyncBlockList(gate.world);
             for(int lDy = 0; lDy < fWidth; lDy++) {
                 for(BlockPosition lPos : new WorldLineWalk(lRightTop, lRightBottom)) {
                     if (!lPos.equals(lRightTop) && !lPos.equals(lRightBottom)) {
                         Block lFrom = lPos.getBlock(gate.world);
                         BlockPosition lTo = lPos.clone();
                         lTo.add(-fDx,0,-fDz);
                         lList.add(lTo, lFrom.getType(), lFrom.getData(), true);
                     }
                 }
                 lRightTop.add(fDx,0,fDz);
                 lRightBottom.add(fDx,0,fDz);
             }
             if (fHeight > 0) {
                 lRightTop.add(-fDx,0,-fDz);
                 lRightBottom.add(-fDx,0,-fDz);
             }
             for(BlockPosition lPos : new WorldLineWalk(lRightTop, lRightBottom)) {
                 if (!lPos.equals(lRightTop) && !lPos.equals(lRightBottom)) {
                     lList.add(lPos, Material.AIR, (byte)0, true);
                 }
             }
             lList.execute();
             fRightTop.add(-fDx,0,-fDz);
             fRightBottom.add(-fDx,0,-fDz);
             fCount--;
             gate.open = false;
             gate.openCount = 0;
         } else {
             plugin.stopGateTask(this);
         }
     }    
     
 /*************************************
  **************** FlatRightLeft ****** 
  *************************************/
     
     protected void doFlatRightLeftOpen() {
         if (fCount > 0) {
             BlockPosition lRightTop = fRightTop.clone();
             BlockPosition lRightBottom = fRightBottom.clone();
             lRightTop.add(fDx,0,fDz);
             lRightBottom.add(fDx,0,fDz);
             //check if left line is empty
             boolean lLeftLineEmpty = true;
             for(BlockPosition lPos : new WorldLineWalk(lRightTop, lRightBottom)) {
                 if (!lPos.equals(lRightTop) && !lPos.equals(lRightBottom)) {
                     if (!isAIRLike(lPos.getBlockType(gate.world))) {
                         lLeftLineEmpty = false;
                         break;
                     }
                 }
             }
             if (lLeftLineEmpty) {
                 lRightTop.cloneFrom(fRightTop);
                 lRightBottom.cloneFrom(fRightBottom);
                 SyncBlockList lList = new SyncBlockList(gate.world);
                 for(int lDy = 0; lDy < fWidth; lDy++) {
                     for(BlockPosition lPos : new WorldLineWalk(lRightTop, lRightBottom)) {
                         if (!lPos.equals(lRightTop) && !lPos.equals(lRightBottom)) {
                             Block lFrom = lPos.getBlock(gate.world);
                             BlockPosition lTo = lPos.clone();
                             lTo.add(fDx,0,fDz);
                             lList.add(lTo, lFrom.getType(), lFrom.getData(), true);
                         }
                     }
                     lRightTop.add(-fDx,0,-fDz);
                     lRightBottom.add(-fDx,0,-fDz);
                 }
                 if (fWidth > 0) {
                     lRightTop.add(fDx,0,fDz);
                     lRightBottom.add(fDx,0,fDz);
                 }
                 for(BlockPosition lPos : new WorldLineWalk(lRightTop, lRightBottom)) {
                     if (!lPos.equals(lRightTop) && !lPos.equals(lRightBottom)) {
                         lList.add(lPos, Material.AIR, (byte)0, true);
                     }
                 }
                 lList.execute();
                 fRightTop.add(fDx,0,fDz);
                 fRightBottom.add(fDx,0,fDz);
                 fCount--;
                 gate.open = true;
                 gate.openCount++;
             } else {
                 plugin.stopGateTask(this);
             }
         } else {
             plugin.stopGateTask(this);
         }
     }    
 
     protected void doFlatRightLeftClose() {
         if (fCount > 0) {
             BlockPosition lRightTop = fRightTop.clone();
             BlockPosition lRightBottom = fRightBottom.clone();
             lRightTop.add(-fDx,0,-fDz);
             lRightBottom.add(-fDx,0,-fDz);
             //check if bottom line has items to drop
             for(BlockPosition lPos : new WorldLineWalk(lRightTop, lRightBottom)) {
                 if (!lPos.equals(lRightTop) && !lPos.equals(lRightBottom)) {
                     Block lBlock = lPos.getBlock(gate.world);
                     Material lMat = lBlock.getType();
                     if (!lMat.equals(Material.AIR)) {
                         if (!(lMat.equals(Material.WATER)
                                 || lMat.equals(Material.LAVA)
                                 || lMat.equals(Material.STATIONARY_LAVA)
                                 || lMat.equals(Material.STATIONARY_WATER))) {
                             ItemStack lStack = new ItemStack(lMat, 1, (short)0, lBlock.getData());
                             gate.world.dropItemNaturally(lPos.getLocation(gate.world), lStack);
                         }
                     }
                 }
             }
             lRightTop.cloneFrom(fRightTop);
             lRightBottom.cloneFrom(fRightBottom);
             SyncBlockList lList = new SyncBlockList(gate.world);
             for(int lDy = 0; lDy < fWidth; lDy++) {
                 for(BlockPosition lPos : new WorldLineWalk(lRightTop, lRightBottom)) {
                     if (!lPos.equals(lRightTop) && !lPos.equals(lRightBottom)) {
                         Block lFrom = lPos.getBlock(gate.world);
                         BlockPosition lTo = lPos.clone();
                         lTo.add(-fDx,0,-fDz);
                         lList.add(lTo, lFrom.getType(), lFrom.getData(), true);
                     }
                 }
                 lRightTop.add(fDx,0,fDz);
                 lRightBottom.add(fDx,0,fDz);
             }
             if (fHeight > 0) {
                 lRightTop.add(-fDx,0,-fDz);
                 lRightBottom.add(-fDx,0,-fDz);
             }
             for(BlockPosition lPos : new WorldLineWalk(lRightTop, lRightBottom)) {
                 if (!lPos.equals(lRightTop) && !lPos.equals(lRightBottom)) {
                     lList.add(lPos, Material.AIR, (byte)0, true);
                 }
             }
             lList.execute();
             fRightTop.add(-fDx,0,-fDz);
             fRightBottom.add(-fDx,0,-fDz);
             fCount--;
             gate.open = false;
             gate.openCount = 0;
         } else {
             plugin.stopGateTask(this);
         }
     }    
     
 /*************************************
  **************** FlatLeftRight ****** 
  *************************************/
     
     protected void doFlatLeftRightOpen() {
         if (fCount > 0) {
             BlockPosition lLeftTop = fLeftTop.clone();
             BlockPosition lLeftBottom = fLeftBottom.clone();
             lLeftTop.add(-fDx,0,-fDz);
             lLeftBottom.add(-fDx,0,-fDz);
             //check if left line is empty
             boolean lLeftLineEmpty = true;
             for(BlockPosition lPos : new WorldLineWalk(lLeftTop, lLeftBottom)) {
                 if (!lPos.equals(lLeftTop) && !lPos.equals(lLeftBottom)) {
                     if (!isAIRLike(lPos.getBlockType(gate.world))) {
                         lLeftLineEmpty = false;
                         break;
                     }
                 }
             }
             if (lLeftLineEmpty) {
                 lLeftTop.cloneFrom(fLeftTop);
                 lLeftBottom.cloneFrom(fLeftBottom);
                 SyncBlockList lList = new SyncBlockList(gate.world);
                 for(int lDy = 0; lDy < fWidth; lDy++) {
                     for(BlockPosition lPos : new WorldLineWalk(lLeftTop, lLeftBottom)) {
                         if (!lPos.equals(lLeftTop) && !lPos.equals(lLeftBottom)) {
                             Block lFrom = lPos.getBlock(gate.world);
                             BlockPosition lTo = lPos.clone();
                             lTo.add(-fDx,0,-fDz);
                             lList.add(lTo, lFrom.getType(), lFrom.getData(), true);
                         }
                     }
                     lLeftTop.add(fDx,0,fDz);
                     lLeftBottom.add(fDx,0,fDz);
                 }
                 if (fWidth > 0) {
                     lLeftTop.add(-fDx,0,-fDz);
                     lLeftBottom.add(-fDx,0,-fDz);
                 }
                 for(BlockPosition lPos : new WorldLineWalk(lLeftTop, lLeftBottom)) {
                     if (!lPos.equals(lLeftTop) && !lPos.equals(lLeftBottom)) {
                         lList.add(lPos, Material.AIR, (byte)0, true);
                     }
                 }
                 lList.execute();
                 fLeftTop.add(-fDx,0,-fDz);
                 fLeftBottom.add(-fDx,0,-fDz);
                 fCount--;
                 gate.open = true;
                 gate.openCount++;
             } else {
                 plugin.stopGateTask(this);
             }
         } else {
             plugin.stopGateTask(this);
         }
     }    
 
     protected void doFlatLeftRightClose() {
         if (fCount > 0) {
             BlockPosition lLeftTop = fLeftTop.clone();
             BlockPosition lLeftBottom = fLeftBottom.clone();
             lLeftTop.add(fDx,0,fDz);
             lLeftBottom.add(fDx,0,fDz);
             //check if bottom line has items to drop
             for(BlockPosition lPos : new WorldLineWalk(lLeftTop, lLeftBottom)) {
                 if (!lPos.equals(lLeftTop) && !lPos.equals(lLeftBottom)) {
                     Block lBlock = lPos.getBlock(gate.world);
                     Material lMat = lBlock.getType();
                     if (!lMat.equals(Material.AIR)) {
                         if (!(lMat.equals(Material.WATER)
                                 || lMat.equals(Material.LAVA)
                                 || lMat.equals(Material.STATIONARY_LAVA)
                                 || lMat.equals(Material.STATIONARY_WATER))) {
                             ItemStack lStack = new ItemStack(lMat, 1, (short)0, lBlock.getData());
                             gate.world.dropItemNaturally(lPos.getLocation(gate.world), lStack);
                         }
                     }
                 }
             }
             lLeftTop.cloneFrom(fLeftTop);
             lLeftBottom.cloneFrom(fLeftBottom);
             SyncBlockList lList = new SyncBlockList(gate.world);
             for(int lDy = 0; lDy < fWidth; lDy++) {
                 for(BlockPosition lPos : new WorldLineWalk(lLeftTop, lLeftBottom)) {
                     if (!lPos.equals(lLeftTop) && !lPos.equals(lLeftBottom)) {
                         Block lFrom = lPos.getBlock(gate.world);
                         BlockPosition lTo = lPos.clone();
                         lTo.add(fDx,0,fDz);
                         lList.add(lTo, lFrom.getType(), lFrom.getData(), true);
                     }
                 }
                 lLeftTop.add(-fDx,0,-fDz);
                 lLeftBottom.add(-fDx,0,-fDz);
             }
             if (fHeight > 0) {
                 lLeftTop.add(fDx,0,fDz);
                 lLeftBottom.add(fDx,0,fDz);
             }
             for(BlockPosition lPos : new WorldLineWalk(lLeftTop, lLeftBottom)) {
                 if (!lPos.equals(lLeftTop) && !lPos.equals(lLeftBottom)) {
                     lList.add(lPos, Material.AIR, (byte)0, true);
                 }
             }
             lList.execute();
             fLeftTop.add(fDx,0,fDz);
             fLeftBottom.add(fDx,0,fDz);
             fCount--;
             gate.open = false;
             gate.openCount = 0;
         } else {
             plugin.stopGateTask(this);
         }
     }    
 }
