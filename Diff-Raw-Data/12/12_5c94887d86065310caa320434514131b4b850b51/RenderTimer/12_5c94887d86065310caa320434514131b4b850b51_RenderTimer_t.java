 package com.lemoulinstudio.gfa.core.time;
 
 import com.lemoulinstudio.gfa.core.dma.Dma;
 import com.lemoulinstudio.gfa.core.gfx.Lcd;
 import com.lemoulinstudio.gfa.core.memory.GfaMMU;
 import com.lemoulinstudio.gfa.core.memory.IORegisterSpace_8_16_32;
 
 public class RenderTimer {
 
   private IORegisterSpace_8_16_32 ioMem;
   private Lcd lcd;
   private Dma dma0;
   private Dma dma1;
   private Dma dma2;
   private Dma dma3;
 
   private int xDisplay;
   private int yDisplay;
   private boolean oldIsInHBlank;
   private boolean oldIsInVBlank;
   private boolean oldIsVCountMatch;
   
   public RenderTimer() {
     reset();
   }
 
   public void connectToMemory(GfaMMU memory) {
     ioMem = (IORegisterSpace_8_16_32) memory.getMemoryBank(0x04);
   }
 
   public void connectToLcd(Lcd lcd) {
     this.lcd = lcd;
   }
   
   public void connectToDma0(Dma dma0) {
     this.dma0 = dma0;
   }
   
   public void connectToDma1(Dma dma1) {
     this.dma1 = dma1;
   }
   
   public void connectToDma2(Dma dma2) {
     this.dma2 = dma2;
   }
   
   public void connectToDma3(Dma dma3) {
     this.dma3 = dma3;
   }
   
   public final void reset() {
     xDisplay = 0;
     yDisplay = 0;
     oldIsInHBlank = false;
     oldIsInVBlank = false;
     oldIsVCountMatch = false;
   }
 
   public void addTime(int nbCycles) {
     // Update hValue and vValue.
     xDisplay += nbCycles;
 
     if (xDisplay >= 308 * 4) {
       xDisplay -= 308 * 4;
       
       yDisplay++;
       if (yDisplay >= 228)
         yDisplay = 0;
 
       ioMem.setYScanline(yDisplay);
     }
 
     // Update the bits of the display status.
     boolean isInHBlank = (xDisplay >= 240 * 4);
     boolean isInVBlank = (yDisplay >= 160);
     boolean isVCountMatch = (yDisplay == ioMem.getVCountValue());
     ioMem.setIsHBlank(isInHBlank);
     ioMem.setIsVBlank(isInVBlank);
     ioMem.setIsVCountMatch(isVCountMatch);
 
     // Check if it is time to draw a line.
     if (!oldIsInHBlank && isInHBlank && !isInVBlank)
       lcd.drawLine(yDisplay);
 
     // Handle the trigger of the DMAs and the HBlank interrupt.
    if (!oldIsInHBlank && isInHBlank) {
      if (!isInVBlank) {
        dma0.notifyHBlank();
        dma1.notifyHBlank();
        dma2.notifyHBlank();
        dma3.notifyHBlank();
      }
 
       if (ioMem.isHBlankInterruptEnabled())
         ioMem.genInterrupt(IORegisterSpace_8_16_32.hBlankInterruptBit);
     }
 
     // Handle the trigger of the DMAs and the VBlank interrupt.
     if (!oldIsInVBlank && isInVBlank) {
       dma0.notifyVBlank();
       dma1.notifyVBlank();
       dma2.notifyVBlank();
       dma3.notifyVBlank();
 
       if (ioMem.isVBlankInterruptEnabled())
         ioMem.genInterrupt(IORegisterSpace_8_16_32.vBlankInterruptBit);
     }
 
     // Handle the trigger of the VCount interrupt.
     if (!oldIsVCountMatch && isVCountMatch)
       if (ioMem.isVCountInterruptEnabled())
         ioMem.genInterrupt(IORegisterSpace_8_16_32.vCountInterruptBit);
 
     
     oldIsInHBlank = isInHBlank;
     oldIsInVBlank = isInVBlank;
     oldIsVCountMatch = isVCountMatch;
   }
 
 }
