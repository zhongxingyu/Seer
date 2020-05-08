 package com.lemoulinstudio.gfa.core.time;
 
 import com.lemoulinstudio.gfa.core.dma.Dma;
 import com.lemoulinstudio.gfa.core.gfx.Lcd;
 import com.lemoulinstudio.gfa.core.memory.GfaMMU;
 import com.lemoulinstudio.gfa.core.memory.IORegisterSpace_8_16_32;
 
 public class RenderTimer {
 
   private int hValue;
   private int vValue;
   private boolean oldIsHBlank;
   private boolean oldIsVBlank;
   private IORegisterSpace_8_16_32 ioMem;
   private Lcd lcd;
   private Dma dma0;
   private Dma dma1;
   private Dma dma2;
   private Dma dma3;
 
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
     hValue = 0;
     vValue = 0;
     oldIsHBlank = false;
     oldIsVBlank = false;
   }
 
   /*
    public int getTime() {
     return vValue;
    }
    */
 
   public void addTime(int nbCycles) {
     hValue += nbCycles;
 
     boolean isHBlank = (hValue >= 240 * 4);
     boolean isVBlank = (vValue >= 160);
 
     ioMem.setHBlank(isHBlank);
     ioMem.setVBlank(isVBlank);
     
    if (!oldIsHBlank && isHBlank && !isVBlank) {
       lcd.drawLine(vValue);
       dma0.notifyHBlank();
       dma1.notifyHBlank();
       dma2.notifyHBlank();
       dma3.notifyHBlank();
      ioMem.genInterrupt(IORegisterSpace_8_16_32.hBlankInterruptBit);
     }
     
     if (!oldIsVBlank && isVBlank) {
       dma0.notifyVBlank();
       dma1.notifyVBlank();
       dma2.notifyVBlank();
       dma3.notifyVBlank();
      ioMem.genInterrupt(IORegisterSpace_8_16_32.vBlankInterruptBit);
     }
     
     oldIsHBlank = isHBlank;
     oldIsVBlank = isVBlank;
 
     if (hValue >= 308 * 4) {
       hValue -= 308 * 4;
       
       vValue++;
       if (vValue >= 228)
         vValue = 0;
 
       ioMem.setYScanline(vValue);
       if (ioMem.isVCountMatchInterruptEnabled() &&
 	  (vValue == ioMem.getVCountSetting()))
	  ioMem.genInterrupt(IORegisterSpace_8_16_32.vCountInterruptBit);
     }
   }
 
 }
