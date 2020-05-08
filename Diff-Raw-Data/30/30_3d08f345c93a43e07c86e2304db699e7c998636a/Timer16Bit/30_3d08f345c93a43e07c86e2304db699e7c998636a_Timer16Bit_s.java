 /**
  * Copyright (c) 2004-2005, Regents of the University of California
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  *
  * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *
  * Neither the name of the University of California, Los Angeles nor the
  * names of its contributors may be used to endorse or promote products
  * derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package avrora.sim.mcu;
 
 import avrora.sim.State;
 import avrora.sim.Simulator;
 import avrora.sim.clock.Clock;
 import avrora.sim.RWRegister;
 import avrora.util.Arithmetic;
 
 /**
  * The <code>Timer16Bit</code> class emulates the functionality and behavior of a 16-bit timer on the
  * Atmega128. It has several control and data registers and can fire up to six different interrupts
  * depending on the mode that it has been put into. It has three output compare units and one input
  * capture unit. UNIMPLEMENTED: input capture unit.
  *
  * @author Daniel Lee
  */
 public abstract class Timer16Bit extends AtmelInternalDevice {
 
     // Timer/Counter Modes of Operations
     public static final int MODE_NORMAL = 0;
     public static final int MODE_PWM_PHASE_CORRECT_8_BIT = 1;
     public static final int MODE_PWM_PHASE_CORRECT_9_BIT = 2;
     public static final int MODE_PWM_PHASE_CORRECT_10_BIT = 3;
     public static final int MODE_CTC_OCRnA = 4;
     public static final int MODE_FASTPWM_8_BIT = 5;
     public static final int MODE_FASTPWM_9_BIT = 6;
     public static final int MODE_FASTPWM_10_BIT = 7;
     public static final int MODE_PWM_PNF_ICRn = 8;
     public static final int MODE_PWM_PNF_OCRnA = 9;
     public static final int MODE_PWN_PHASE_CORRECT_ICRn = 10;
     public static final int MODE_PWN_PHASE_CORRECT_OCRnA = 11;
     public static final int MODE_CTC_ICRn = 12;
     // 13 is reserved
     public static final int MODE_FASTPWM_ICRn = 14;
     public static final int MODE_FASTPWM_OCRnA = 15;
 
 
     public static final int MAX = 0xffff;
     public static final int BOTTOM = 0x0000;
 
 
     /**
      * The <code>OutputCompareUnit</code> class represents an output compare unit that is
      * connected to the timer. The output compare unit functions by continually comparing the
      * count of the timer to a particular value, and signalling a match when complete.
      */
     class OutputCompareUnit {
         final BufferedRegister OCRnXH_reg;
         final BufferedRegister OCRnXL_reg;
         final OCRnxPairedRegister OCRnX_reg;
         final AtmelMicrocontroller.Pin outputComparePin;
         final RegisterSet.Field mode;
         final RegisterSet.Field force;
         final char unit;
         final int flagBit;
 
         OutputCompareUnit(Microcontroller m, RegisterSet rset, char c, int fb) {
             unit = c;
             OCRnXH_reg = new BufferedRegister();
             OCRnXL_reg = new BufferedRegister();
             OCRnX_reg = new OCRnxPairedRegister(OCRnXH_reg, OCRnXL_reg);
             outputComparePin = (AtmelMicrocontroller.Pin)m.getPin("OC"+n+unit);
             mode = rset.getField("COM"+n+c);
             force = rset.installField("FOC"+n+c, new FOC_Field());
             flagBit = fb;
 
             installIOReg("OCR"+n+unit+"H", new OCRnxTempHighRegister(OCRnXH_reg));
             installIOReg("OCR"+n+unit+"L", OCRnX_reg);
         }
 
         class FOC_Field extends RegisterSet.Field {
             public void update() {
                 if ( value == 1 ) {
                     if ( read16(TCNTnH_reg, TCNTnL_reg) == read() ) {
                         output();
                     }
                 }
                 set(0);
             }
         }
 
         void forceCompare(int count) {
             if ( count == read() ) {
                 output();
                 // note: interrupts are not posted when the compare is forced
             }
         }
 
         void compare(int count) {
             if ( count == read() ) {
                 output();
                 xTIFR_reg.flagBit(flagBit);
             }
         }
 
         void flush() {
             OCRnXH_reg.flush();
             OCRnXL_reg.flush();
         }
 
         private void output() {
             // read the bits in the control register for compare mode
             switch (mode.value) {
                 case 1:
                     outputComparePin.write(!outputComparePin.read()); // clear
                     break;
                 case 2:
                     outputComparePin.write(false);
                     break;
                 case 3:
                     outputComparePin.write(true);
                     break;
             }
         }
 
         int read() {
             return read16(OCRnXH_reg, OCRnXL_reg);
         }
     }
 
     final RWRegister TCNTnH_reg; // timer counter registers
     final TCNTnRegister TCNTnL_reg;
     final PairedRegister TCNTn_reg;
 
     final OutputCompareUnit[] compareUnits;
 
     final RWRegister highTempReg;
 
     final RWRegister ICRnH_reg; // input capture registers
     final RWRegister ICRnL_reg;
     final PairedRegister ICRn_reg;
 
     final Ticker ticker;
 
     final RegisterSet.Field WGMn;
     final RegisterSet.Field CSn;
 
     boolean timerEnabled;
     boolean countUp;
     long period;
 
     boolean blockCompareMatch;
 
     Simulator.Printer timerPrinter;
 
     protected final Clock externalClock;
     Clock timerClock;
 
     // information about registers and flags that specifies
     // which specific registers this 16-bit timer interacts with
 
     final int n; // number of timer. 1 for Timer1, 3 for Timer3
 
     // these are the offsets on registers corresponding to these flags
     int OCIEnA;
     int OCIEnB;
     int OCIEnC;
     int TOIEn;
     int TOVn;
     int OCFnA;
     int OCFnB;
     int OCFnC;
     int ICFn;
 
     protected ATMegaFamily.FlagRegister xTIFR_reg;
     protected ATMegaFamily.MaskRegister xTIMSK_reg;
 
     protected int[] periods;
 
     // This method should be overloaded to initialize the above values.
     protected abstract void initValues();
 
     protected Timer16Bit(int n, int numUnits, AtmelMicrocontroller m) {
         super("timer"+n, m);
         this.n = n;
 
         RegisterSet rset = m.getRegisterSet();
 
         initValues();
 
         WGMn = rset.getField("WGM"+n);
         CSn = rset.installField("CS"+n, new RegisterSet.Field() {
             public void update() {
                 resetPeriod(periods[value]);
             }
         });
 
         ticker = new Ticker();
 
         highTempReg = new RWRegister();
 
         compareUnits = new OutputCompareUnit[numUnits];
         newOCU(0, numUnits, m, rset, 'A', OCFnA);
         newOCU(1, numUnits, m, rset, 'B', OCFnB);
         newOCU(2, numUnits, m, rset, 'C', OCFnC);
 
         TCNTnH_reg = new RWRegister();
         TCNTnL_reg = new TCNTnRegister();
         TCNTn_reg = new PairedRegister(TCNTnH_reg, TCNTnL_reg);
 
         ICRnH_reg = new RWRegister();
         ICRnL_reg = new RWRegister();
         ICRn_reg = new PairedRegister(ICRnL_reg, ICRnH_reg);
 
         externalClock = m.getClock("external");
         timerClock = mainClock;
 
         installIOReg("TCNT"+n+"H", highTempReg);
         installIOReg("TCNT"+n+"L", TCNTn_reg);
 
         installIOReg("ICR"+n+"H", highTempReg);
         installIOReg("ICR"+n+"L", ICRn_reg);
 
         timerPrinter = m.getSimulator().getPrinter("atmega.timer" + n);
     }
 
     void newOCU(int unit, int numUnits, Microcontroller m, RegisterSet rset, char uname, int fb) {
        if ( unit < numUnits-1 ) {
             compareUnits[unit] = new OutputCompareUnit(m, rset, uname, fb);
         }
     }
 
 
     /**
      * Flags the overflow interrupt for this timer.
      */
     protected void overflow() {
         if (timerPrinter.enabled) {
             boolean enabled = xTIMSK_reg.readBit(TOIEn);
             timerPrinter.println("Timer" + n + ".overFlow (enabled: " + enabled + ')' + "  ");
         }
         // set the overflow flag for this timer
         xTIFR_reg.flagBit(TOVn);
     }
 
     /**
      * The <code>PairedRegister</code> class exists to implement the shared temporary register for the
      * high byte of the 16-bit registers corresponding to a 16-bit timer. Accesses to the high byte of
      * a register pair should go through this temporary byte. According to the manual, writes to the
      * high byte are stored in the temporary register. When the low byte is written to, both the low
      * and high byte are updated. On a read, the temporary high byte is updated when a read occurs on
      * the low byte. The PairedRegister should be installed in place of the low register. Reads/writes
      * on this register will act accordingly on the low register, as well as initiate a read/write on
      * the associated high register.
      */
     protected class PairedRegister extends RWRegister {
         RWRegister high;
         RWRegister low;
 
         PairedRegister(RWRegister high, RWRegister low) {
             this.high = high;
             this.low = low;
         }
 
         public void write(byte val) {
             low.write(val);
             high.write(highTempReg.read());
         }
 
         public void writeBit(int bit, boolean val) {
             low.writeBit(bit, val);
             high.write(highTempReg.read());
         }
 
         public byte read() {
             highTempReg.write(high.read());
             return low.read();
         }
 
         public boolean readBit(int bit) {
             byte val = read();
             return Arithmetic.getBit(val, bit);
         }
     }
 
     /**
      * The normal 16-bit read behavior described in the doc for PairedRegister does not apply for the
      * OCRnx registers. Reads on the OCRnxH registers are direct.
      */
     protected class OCRnxPairedRegister extends PairedRegister {
         OCRnxPairedRegister(RWRegister high, RWRegister low) {
             super(high, low);
         }
 
         public byte read() {
             return low.read();
         }
 
         public boolean readBit(int bit) {
             return low.readBit(bit);
         }
     }
 
     /**
      * See doc for OCRnxPairedRegister.
      */
     protected class OCRnxTempHighRegister extends RWRegister {
         RWRegister register;
 
         OCRnxTempHighRegister(RWRegister register) {
             this.register = register;
         }
 
         public void write(byte val) {
             highTempReg.write(val);
         }
 
         public void writeBit(int bit, boolean val) {
             highTempReg.writeBit(bit, val);
         }
 
         public byte read() {
             return register.read();
         }
 
         public boolean readBit(int bit) {
             return register.readBit(bit);
         }
     }
 
     /**
      * Overloads the write behavior of this class of register in order to implement compare match
      * blocking for one timer period.
      */
     protected class TCNTnRegister extends RWRegister {
         /* expr of the blockCompareMatch corresponding to
          * this register in the array of boolean flags.  */
         public void write(byte val) {
             value = val;
             blockCompareMatch = true;
         }
 
         public void writeBit(int bit, boolean val) {
             value = Arithmetic.setBit(value, bit, val);
             blockCompareMatch = true;
         }
     }
 
     private void resetPeriod(int nPeriod) {
         if (nPeriod == 0) {
             if (timerEnabled) {
                 if (timerPrinter.enabled) timerPrinter.println("Timer" + n + " disabled");
                 timerClock.removeEvent(ticker);
                 timerEnabled = false;
             }
             return;
         }
         if (timerEnabled) {
             timerClock.removeEvent(ticker);
         }
         if (timerPrinter.enabled) timerPrinter.println("Timer" + n + " enabled: period = " + nPeriod + " mode = " + WGMn.value);
         period = nPeriod;
         timerEnabled = true;
         timerClock.insertEvent(ticker, period);
 
     }
 
     /**
      * In PWN modes, writes to the OCRnx registers are buffered. Specifically, the actual write is
      * delayed until a certain event (the counter reaching either TOP or BOTTOM) specified by the
      * particular PWN mode. BufferedRegister implements this by writing to a buffer register on a
      * write and reading from the buffered register in a read. When the buffered register is to be
      * updated, the flush() method should be called.
      */
     protected class BufferedRegister extends RWRegister {
         final RWRegister register;
 
         protected BufferedRegister() {
             this.register = new RWRegister();
         }
 
         public void write(byte val) {
             super.write(val);
             int mode = WGMn.value;
             if (mode == MODE_NORMAL || mode == MODE_CTC_OCRnA
                     || mode == MODE_CTC_ICRn) {
                 flush();
             }
         }
 
         public void writeBit(int bit, boolean val) {
             super.writeBit(bit, val);
             int mode = WGMn.value;
             if (mode == MODE_NORMAL || mode == MODE_CTC_OCRnA
                     || mode == MODE_CTC_ICRn) {
                 flush();
             }
         }
 
         public byte readBuffer() {
             return super.read();
         }
 
         public byte read() {
             return register.read();
         }
 
         public boolean readBit(int bit) {
             return register.readBit(bit);
         }
 
         protected void flush() {
             register.write(value);
         }
     }
 
     /**
      * The <code>Ticker</class> implements the periodic behavior of the timer. It emulates the
      * operation of the timer at each clock cycle and uses the global timed event queue to achieve the
      * correct periodic behavior.
      */
     protected class Ticker implements Simulator.Event {
 
         private void flushOCRnx() {
             for ( int cntr = 0; cntr < compareUnits.length; cntr++ )
                 compareUnits[cntr].flush();
            /*
            OCRnAL_reg.flush();
            OCRnAH_reg.flush();
            OCRnBL_reg.flush();
            OCRnBH_reg.flush();
            OCRnCL_reg.flush();
            OCRnCH_reg.flush();
            */
         }
 
         private final int[] TOP = {0xffff, 0x00ff, 0x01ff, 0x03ff, 0, 0x0ff, 0x01ff, 0x03ff};
 
 
         public void fire() {
 
             int count = read16(TCNTnH_reg, TCNTnL_reg);
             int countSave = count;
             int compareA = compareUnits[0].read();
             /*
             int compareB = read16(OCRnBH_reg, OCRnBL_reg);
             int compareC = read16(OCRnCH_reg, OCRnCL_reg);
             */
             int compareI = read16(ICRnH_reg, ICRnL_reg);
 
             if (timerPrinter.enabled) {
                 timerPrinter.println("Timer" + n + " [TCNT" + n + " = " + count + ", OCR" + n + "A = " + compareA + "]");
             }
 
             // What exactly should I do when I phase/frequency current?
             // registers.
             // Make sure these cases are doing what they are supposed to
             // do.
             int mode = WGMn.value;
             switch (mode) {
                 case MODE_NORMAL:
                     count++;
                     countSave = count;
                     if (count == MAX) {
                         overflow();
                         count = 0;
                     }
                     break;
                 case MODE_PWM_PHASE_CORRECT_8_BIT:
                 case MODE_PWM_PHASE_CORRECT_9_BIT:
                 case MODE_PWM_PHASE_CORRECT_10_BIT:
                     if (countUp)
                         count++;
                     else
                         count--;
 
                     countSave = count;
                     if (count >= TOP[mode]) {
                         countUp = false;
                         count = TOP[mode];
                         flushOCRnx();
                     }
                     if (count <= 0) {
                         overflow();
                         countUp = true;
                         count = 0;
                     }
                     break;
 
                 case MODE_CTC_OCRnA:
                     count++;
 
                     countSave = count;
                     if (count == compareA) {
                         count = 0;
                     }
                     if (count == MAX) {
                         overflow();
                     }
                     break;
                 case MODE_FASTPWM_8_BIT:
                 case MODE_FASTPWM_9_BIT:
                 case MODE_FASTPWM_10_BIT:
                     count++;
 
                     countSave = count;
                     if (count == TOP[mode]) {
                         count = 0;
                         overflow();
                         flushOCRnx();
                     }
 
                     break;
                 case MODE_PWM_PNF_ICRn:
                     if (countUp)
                         count++;
                     else
                         count--;
 
                     countSave = count;
                     if (count >= compareI) {
                         countUp = false;
                         count = compareI;
                     }
                     if (count <= 0) {
                         overflow();
                         flushOCRnx();
                         countUp = true;
                         count = 0;
                     }
                     break;
                 case MODE_PWM_PNF_OCRnA:
                     if (countUp)
                         count++;
                     else
                         count--;
 
                     countSave = count;
                     if (count >= compareA) {
                         countUp = false;
                         count = compareA;
                     }
                     if (count <= 0) {
                         overflow();
                         flushOCRnx();
                         countUp = true;
                         count = 0;
                     }
                     break;
                 case MODE_PWN_PHASE_CORRECT_ICRn:
                     if (countUp)
                         count++;
                     else
                         count--;
 
                     countSave = count;
                     if (count >= compareI) {
                         flushOCRnx();
                         countUp = false;
                         count = compareI;
                     }
                     if (count <= 0) {
                         overflow();
                         countUp = true;
                         count = 0;
                     }
                     break;
                 case MODE_PWN_PHASE_CORRECT_OCRnA:
                     if (countUp)
                         count++;
                     else
                         count--;
 
                     countSave = count;
                     if (count >= compareA) {
                         flushOCRnx();
                         countUp = false;
                         count = compareA;
                     }
                     if (count <= 0) {
                         overflow();
                         countUp = true;
                         count = 0;
                     }
                     break;
                 case MODE_CTC_ICRn:
                     count++;
 
                     countSave = count;
                     if (count == compareI) {
                         count = 0;
                     }
                     if (count == MAX) {
                         overflow();
                     }
                     break;
                 case MODE_FASTPWM_ICRn:
                     count++;
 
                     countSave = count;
                     if (count == compareI) {
                         count = 0;
                         overflow();
                         flushOCRnx();
                     }
                     break;
                 case MODE_FASTPWM_OCRnA:
                     count++;
 
                     countSave = count;
                     if (count == compareA) {
                         count = 0;
                         overflow();
                         flushOCRnx();
                     }
                     break;
             }
 
             // the compare match should be performed in any case.
             if (!blockCompareMatch) {
                 for ( int cntr = 0; cntr < compareUnits.length; cntr++ )
                     compareUnits[cntr].compare(countSave);
                /*
                if (countSave == compareA) {
                    compareMatchA();
                }
                if (countSave == compareB) {
                    compareMatchB();
                }
                if (countSave == compareC) {
                    compareMatchC();
                }
                */
             }
             write16(count, TCNTnH_reg, TCNTnL_reg);
             // make sure timings on this are correct
             blockCompareMatch = false;
 
 
             if (period != 0) timerClock.insertEvent(this, period);
         }
     }
 
 }
