 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mahn42.anhalter42.circuit;
 
 /**
  *
  * @author andre
  */
 public class CircuitHandler42M2003 extends CircuitHandler {
     
     public CircuitHandler42M2003() {
         super("DFP4", "42M2003");
         description = "3x3 Redstone Lamp Display 4 Input";
         pins.add(CircuitHandler.PinMode.Input);
         pins.add(CircuitHandler.PinMode.Input);
         pins.add(CircuitHandler.PinMode.Input);
         pins.add(CircuitHandler.PinMode.Input);
         pins.addArea(CircuitHandler.PinMode.Output, "out", 3);
     }
     
     @Override
     protected void tick() {
         int lCode = getPinValueInt("pin1")
                 | (getPinValueInt("pin2") << 1)
                 | (getPinValueInt("pin3") << 2)
                 | (getPinValueInt("pin4") << 3);
         CircuitFont.CircuitFontChar lChar;
         if (fContext.circuit.signLine2 != null && !fContext.circuit.signLine2.isEmpty()) {
             if (fContext.circuit.signLine2.length() > lCode) {
                 char lC = fContext.circuit.signLine2.charAt(lCode);
                 lChar = Circuit.plugin.configASCI_3.getChar("" + lC);
             } else {
                 lChar = Circuit.plugin.configASCI_3.getChar(" ");
             }
         } else {
             lChar = Circuit.plugin.configASCI_3.getChar(lCode);
         }
        for(int lX = 1; lX <= 3; lX++) {
            for(int lY = 1; lY <= 3; lY++) {
                setPinInArea("out", lX, lY, lChar.getPixel(lX - 1, 3 - lY));
             }
         }
     }
 
 }
