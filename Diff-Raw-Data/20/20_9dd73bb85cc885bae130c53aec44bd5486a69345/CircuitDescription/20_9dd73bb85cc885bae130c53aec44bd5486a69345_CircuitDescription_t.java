 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mahn42.anhalter42.circuit;
 
 import com.mahn42.framework.BuildingDescription;
 import com.mahn42.framework.Framework;
 import com.mahn42.framework.WoolColors;
 import java.util.ArrayList;
 import org.bukkit.Material;
 import org.bukkit.util.Vector;
 
 /**
  *
  * @author andre
  */
 public class CircuitDescription extends BuildingDescription {
 
     public enum Type {
         DIP, // Dual in Line (Rechteck mit Pins aun 2 Seiten)
         QFP  // Quad mit Pins an allen Seiten (Quadrat)
     }
 
     /*
     public enum PinMode {
         Input,
         Output,
         NotConnected
     }
     
     public class Pin {
         public PinMode mode;
         public String name;
         
         public Pin(PinMode aMode) {
             mode = aMode;
         }
 
         public Pin(PinMode aMode, String aName) {
             mode = aMode;
             name = aName;
         }
     }
     
     public class Pins extends ArrayList<Pin> {
         public boolean add(PinMode aMode) {
             return add(new Pin(aMode));
         }
 
         public boolean add(PinMode aMode, String aName) {
             return add(new Pin(aMode, aName));
         }
     }
     */
     public Type type = Type.DIP;
     //public int inputCount = 0;
     //public int outputCount = 0;
     //public int ncCount = 0;
     public int pinCount = 0;
     public String circuitTypeName;
     
     //public Pins pins = new Pins();
 
     public CircuitDescription() {
     }
     
     @Override
     public void activate() {
        if (blocks.size() == 0) {
            switch (type) {
                case DIP: makeDIP(); break;
                case QFP: makeQFP(); break;
            }
         }
         super.activate();
     }
     
     @Override
     protected BuildingDescription create(String aName) {
         CircuitDescription lDesc = new CircuitDescription();
         lDesc.name = aName;
         Framework.plugin.getBuildingDetector().addDescription(lDesc);
         return lDesc;
     }
     
     @Override
     public void cloneFrom(BuildingDescription aDesc) {
         super.cloneFrom(aDesc);
         if (aDesc instanceof CircuitDescription) {
             CircuitDescription aCDesc = (CircuitDescription)aDesc;
             type = aCDesc.type;
             pinCount = aCDesc.pinCount;
             circuitTypeName = aCDesc.circuitTypeName;
         }
     }
     /*
     public CircuitDescription(Type aType, PinMode[] aInPins) {
         switch (aType) {
             case DIP: makeDIP(aInPins); break;
             case QFP: makeQFP(aInPins); break;
         }
     }
     */
 
     private void makeDIP() {
         int lCount = pinCount;
         lCount = ((lCount + 1) >> 1) << 1; 
         int lWidth = lCount >> 1;
         BlockDescription lBlock;
         BlockDescription lLB, lRT, lLast = null;
         RelatedTo lRel;
         lBlock = newBlockDescription("lt");
        lBlock.detectSensible = true;
         lBlock.materials.add(Material.WOOL, WoolColors.black);
         lRel = lBlock.newRelatedTo(new Vector(lWidth, 0, 0), "rt");
         lRel.materials.add(Material.WOOL, WoolColors.black);
         lRel.minDistance = lWidth - 1;
         lBlock.newRelatedTo(new Vector(0, 0, 1), "lb");
         
         lBlock = newBlockDescription("rt"); lRT = lBlock;
         lBlock.materials.add(Material.WOOL, WoolColors.black);
         lBlock.newRelatedTo(new Vector(0, 0, 1), "rb");
         
         lBlock = newBlockDescription("lb"); lLB = lBlock;
         lBlock.materials.add(Material.WOOL, WoolColors.black);
         //lBlock.signSensible = true;
         lRel = lBlock.newRelatedTo(new Vector(lWidth, 0, 0), "rb");
         lRel.materials.add(Material.WOOL, WoolColors.black);
         lRel.minDistance = lWidth - 1;
         lRel = lBlock.newRelatedTo("sign", RelatedPosition.Nearby, 1);
         
         lBlock = newBlockDescription("rb");
         lBlock.materials.add(Material.WOOL, WoolColors.black);
 
         lBlock = newBlockDescription("sign");
         lBlock.materials.add(Material.SIGN_POST);
         
         String lNewPin = null;
         int lDir = 1;
         int lPos = 0;
         for(lPos = 0; lPos < lCount; lPos++) {
             if (lPos == (lCount / 2)) {
                 lDir = -1;
                 lLast = null;
             }
             lNewPin = "pin" + (lPos + 1);
             lBlock = newBlockDescription(lNewPin);
             lBlock.materials.add(Material.WOOL, WoolColors.gray);
             lBlock.redstoneSensible = true;
             if (lLast == null) {
                 if (lDir > 0) {
                     lLB.newRelatedTo(new Vector(0, 0, 1), lNewPin);
                 } else {
                     lRT.newRelatedTo(new Vector(0, 0, -1), lNewPin);
                 }
             } else {
                 lRel = lLast.newRelatedTo(new Vector(2 * lDir,0,0), lBlock.name);
                 lRel.minDistance = 1;
             }
             lLast = lBlock;
         }
     }
     
     // In1, In2 ...
     // Out1, Out2 ...   OutLever1, OutLever2 ...
     // NC1, NC2 ...
     /*
     private void makeDIP() {
         int lCount = pins.size();
         lCount = ((lCount + 1) >> 1) << 1; 
         int lWidth = lCount >> 1;
         BlockDescription lBlock;
         BlockDescription lLB, lRT, lLast = null;
         RelatedTo lRel;
         lBlock = newBlockDescription("lt");
         lBlock.materials.add(Material.WOOL, WoolColors.black);
         lRel = lBlock.newRelatedTo(new Vector(lWidth, 0, 0), "rt");
         lRel.materials.add(Material.WOOL, WoolColors.black);
         lRel.minDistance = lWidth - 1;
         lBlock.newRelatedTo(new Vector(0, 0, 1), "lb");
         
         lBlock = newBlockDescription("rt"); lRT = lBlock;
         lBlock.materials.add(Material.WOOL, WoolColors.black);
         lBlock.newRelatedTo(new Vector(0, 0, 1), "rb");
         
         lBlock = newBlockDescription("lb"); lLB = lBlock;
         lBlock.materials.add(Material.WOOL, WoolColors.black);
         lBlock.signSensible = true;
         lRel = lBlock.newRelatedTo(new Vector(lWidth, 0, 0), "rb");
         lRel.materials.add(Material.WOOL, WoolColors.black);
         lRel.minDistance = lWidth - 1;
         
         lBlock = newBlockDescription("rb");
         lBlock.materials.add(Material.WOOL, WoolColors.black);
         //String lLastPin = null;
         String lNewPin = null;
         int lDir = 1;
         int lPos = 0;
         for(Pin lPin : pins) {
             if (lPos == (lCount / 2)) {
                 lDir = -1;
                 lLast = null;
             }
             lPos++;
             switch (lPin.mode) {
                 case Input:
                     inputCount++;
                     if (lPin.name != null && !lPin.name.isEmpty())
                         lNewPin = lPin.name;
                     else
                         lNewPin = "In" + inputCount;
                     lBlock = newBlockDescription(lNewPin);
                     lBlock.materials.add(Material.WOOL, WoolColors.gray);
                     lBlock.redstoneSensible = true;
                     break;
                 case Output:
                     outputCount++;
                     if (lPin.name != null && !lPin.name.isEmpty())
                         lNewPin = lPin.name;
                     else
                         lNewPin = "Out" + outputCount;
                     lBlock = newBlockDescription(lNewPin + "Lever");
                     lBlock.materials.add(Material.LEVER);
                     lBlock = newBlockDescription(lNewPin);
                     lBlock.materials.add(Material.WOOL, WoolColors.gray);
                     lBlock.newRelatedTo(new Vector(0, 1, 0), lNewPin + "Lever");
                     break;
                 case NotConnected:
                     ncCount++;
                     if (lPin.name != null && !lPin.name.isEmpty())
                         lNewPin = lPin.name;
                     else
                         lNewPin = "NC" + ncCount;
                     lBlock = newBlockDescription(lNewPin);
                     lBlock.materials.add(Material.WOOL, WoolColors.gray);
                     break;
             }
             if (lLast == null) {
                 if (lDir > 0) {
                     lLB.newRelatedTo(new Vector(0, 0, 1), lNewPin);
                 } else {
                     lRT.newRelatedTo(new Vector(0, 0, -1), lNewPin);
                 }
             } else {
                 lRel = lLast.newRelatedTo(new Vector(2 * lDir,0,0), lBlock.name);
                 lRel.minDistance = 1;
             }
             lLast = lBlock;
         }
     }
     */
     private void makeQFP() {
     }
 
 }
