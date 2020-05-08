 /**
  * Copyright (c) 2004, Regents of the University of California
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
 
 package avrora.sim.radio;
 
 import avrora.sim.State;
 import avrora.sim.Simulator;
 import avrora.sim.SimulatorThread;
 import avrora.sim.mcu.Microcontroller;
 import avrora.sim.mcu.ATMega128L;
 import avrora.util.Verbose;
 import avrora.util.Arithmetic;
 
 import java.util.LinkedList;
 
 /**
  * The <code>CC1000Radio</code> class is a simulation of the CC1000 radio for use with
  * avrora. The CC1000 radio is used with the Mica2 platform in the real world.
  * Verbose printers for this class include "sim.cc1000", "sim.cc1000.data",
  * "sim.cc1000.pinconfig".
  *
  * @author Daniel Lee
  */
 public class CC1000Radio implements Radio {
 
     Radio.RadioController controller;
 
     protected RadioRegister registers [] = new RadioRegister[0x47];
 
     /** Register addresses. */
     public static final int MAIN = 0x00;      //1
     public static final int FREQ_2A = 0x01;   //2
     public static final int FREQ_1A = 0x02;
     public static final int FREQ_0A = 0x03;
     public static final int FREQ_2B = 0x04;   //3
     public static final int FREQ_1B = 0x05;
     public static final int FREQ_0B = 0x06;
     public static final int FSEP1 = 0x07;     //4
     public static final int FSEP0 = 0x08;
     public static final int CURRENT = 0x09;   //5
     public static final int FRONT_END = 0x0a; //6
     public static final int PA_POW = 0x0b;    //7
     public static final int PLL = 0x0c;       //8
     public static final int LOCK = 0x0d;      //9
     public static final int CAL = 0x0e;       //10
     public static final int MODEM2 = 0x0f;    //12
     public static final int MODEM1 = 0x10;
     public static final int MODEM0 = 0x11;
     public static final int MATCH = 0x12;     //13
     public static final int FSCTRL = 0x13;    //14
     public static final int PRESCALER = 0x1c; //15
     public static final int TEST6 = 0x40;
     public static final int TEST5 = 0x41;
     public static final int TEST4 = 0x42;
     public static final int TEST3 = 0x43;
     public static final int TEST2 = 0x44;
     public static final int TEST1 = 0x45;
     public static final int TEST0 = 0x46;
 
     /** Registers */
     protected final MainRegister MAIN_reg;
     protected final FrequencyRegister FREQ_A_reg;
     protected final FrequencyRegister FREQ_B_reg;
     protected final FrequencySeparationRegister FSEP_reg;
     protected final CurrentRegister CURRENT_reg;
     protected final FrontEndRegister FRONT_END_reg;
     protected final PA_POWRegister PA_POW_reg;
     protected final PLLRegister PLL_reg;
     protected final LockRegister LOCK_reg;
     protected final CALRegister CAL_reg;
     protected final Modem2Register MODEM_2_reg;
     protected final Modem1Register MODEM_1_reg;
     protected final Modem0Register MODEM_0_reg;
     protected final MatchRegister MATCH_reg;
     protected final FSCTRLRegister FSCTRL_reg;
     protected final PrescalerRegister PRESCALER_reg;
 
     protected final Verbose.Printer radioPrinter = Verbose.getVerbosePrinter("sim.cc1000");
 
     protected final Receiver receiver = new Receiver();
     protected final Transmitter transmitter = new Transmitter();
 
     /** Connected Microcontroller, Simulator and SimulatorThread should all correspond. */
     protected final Microcontroller mcu;
     protected final Simulator sim;
     protected SimulatorThread simThread;
 
     /** Radio environment into which this radio broadcasts. */
     protected final RadioAir air = SimpleAir.simpleAir;
 
     FrequencyRegister currentFrequencyRegister;
 
     private final LinkedList receivedBuffer = new LinkedList();
 
     /** Sets the <code>SimulatorThread</code> of this radio. Should be done BEFORE
      * adding this radio to a <code>RadioAir</code> environment. */
     public void setSimulatorThread(SimulatorThread thread) {
         simThread = thread;
     }
 
     /** Gets the <code>SimulatorThread</code> of this radio. */
     public SimulatorThread getSimulatorThread() {
         return simThread;
     }
 
     /** Part of the <code>Radio</code> interface. It should be called by the <code>RadioAir</code>
      * that this radio is transmitting over when data is to be received. */
     public void receive(Radio.RadioPacket packet) {
         receivedBuffer.addLast(packet);
 
     }
 
     /** Transmit a packet of data into the <code>RadioAir</code>.*/
     public void transmit(Radio.RadioPacket packet) {
         // send packet into air..
         air.transmit(this, packet);
     }
 
     public CC1000Radio(Microcontroller mcu) {
         mcu.setRadio(this);
 
 
         this.mcu = mcu;
         this.sim = mcu.getSimulator();
 
         MAIN_reg = new MainRegister();
 
         for (int i = 0x14; i < registers.length; i++) {
             registers[i] = new DummyRegister(i);
         }
 
         registers[MAIN] = MAIN_reg;
 
         FREQ_A_reg = new FrequencyRegister("A");
         registers[FREQ_2A] = FREQ_A_reg.reg2;
         registers[FREQ_1A] = FREQ_A_reg.reg1;
         registers[FREQ_0A] = FREQ_A_reg.reg0;
 
         FREQ_B_reg = new FrequencyRegister("B");
         registers[FREQ_2B] = FREQ_B_reg.reg2;
         registers[FREQ_1B] = FREQ_B_reg.reg1;
         registers[FREQ_0B] = FREQ_B_reg.reg0;
 
         FSEP_reg = new FrequencySeparationRegister();
         registers[FSEP1] = FSEP_reg.reg1;
         registers[FSEP0] = FSEP_reg.reg0;
 
         CURRENT_reg = new CurrentRegister();
         registers[CURRENT] = CURRENT_reg;
 
         FRONT_END_reg = new FrontEndRegister();
         registers[FRONT_END] = FRONT_END_reg;
 
         PA_POW_reg = new PA_POWRegister();
         registers[PA_POW] = PA_POW_reg;
 
         PLL_reg = new PLLRegister();
         registers[PLL] = PLL_reg;
 
         LOCK_reg = new LockRegister();
         registers[LOCK] = LOCK_reg;
 
         CAL_reg = new CALRegister();
         registers[CAL] = CAL_reg;
 
         MODEM_2_reg = new Modem2Register();
         registers[MODEM2] = MODEM_2_reg;
 
         MODEM_1_reg = new Modem1Register();
         registers[MODEM1] = MODEM_1_reg;
 
         MODEM_0_reg = new Modem0Register();
         registers[MODEM0] = MODEM_0_reg;
 
         MATCH_reg = new MatchRegister();
         registers[MATCH] = MATCH_reg;
 
         FSCTRL_reg = new FSCTRLRegister();
         registers[FSCTRL] = FSCTRL_reg;
 
         PRESCALER_reg = new PrescalerRegister();
         registers[PRESCALER] = PRESCALER_reg;
 
         // If there are other microcontroller implementations in the future,
         // this code should be adjusted to account for that.
         controller = new ATMega128LController();
         controller.install(mcu);
 
     }
 
     /**
      * The <code>RadioRegister</code> is an abstract register grouping together
      * registers on the CC1000 radio.
      */
     protected abstract class RadioRegister extends State.RWIOReg {
         public void write(byte val) {
             super.write(val);
             decode(val);
             if (radioPrinter.enabled) {
                 printStatus();
             }
 
         }
 
         public void writeBit(int bit, boolean val) {
             super.writeBit(bit, val);
             decode(value);
             if (radioPrinter.enabled) {
                 printStatus();
             }
         }
 
         RadioRegister(String id, byte def) {
             this.id = id;
             this.def = def;
             this.value = def;
         }
 
         protected final String id;
 
         protected final byte def; // default value
 
         protected abstract void decode(byte val);
 
         protected void printStatus() {
             radioPrinter.println("CC1000[" + id + "]: ...");
         }
 
         protected void reset() {
             write(def);
         }
 
 
     }
 
     /** The <code>DummyRegister</code> is a filler class for registers within the
      * 7-bit address space of the radio registers, but do not actually exist/do
      * anything in the real radio.
      */
     protected class DummyRegister extends RadioRegister {
         DummyRegister(int i) {
             super("Dummy " + Integer.toHexString(i), (byte) 0);
         }
 
         protected void decode(byte val) {
         }
     }
 
     /**
      * The main register on the CC1000.
      */
     protected class MainRegister extends RadioRegister {
         public final int RXTX = 7;      // 0: RX, 1: TX
         public final int F_REG = 6;     // 0: A, 1: B
         public final int RX_PD = 5;     // Power down of RX part of interface
         public final int TX_PD = 4;     // Power down of TX part of interface
         public final int FS_PD = 3;     // Power down of Frequency Synthesizer
         public final int CORE_PD = 2;   // Power down of Crystal Oscillator Core
         public final int BIAS_PD = 1;   // Power down of BIAS and Crystal Oscillator Buffer
         public final int RESET_N = 0;   // Reset other registers to default value
 
         boolean rxtx;
         boolean fReg;
         boolean rxPd;
         boolean txPd;
         boolean fsPd;
         boolean corePd;
         boolean biasPd;
         boolean resetN;
 
         byte oldVal;
         boolean oldRn;
 
         MainRegister() {
             super("MAIN", (byte) 0x3e);
         }
 
         protected void decode(byte val) {
 
             oldRn = resetN;
 
             rxtx = Arithmetic.getBit(val, RXTX);
             fReg = Arithmetic.getBit(val, F_REG);
             rxPd = Arithmetic.getBit(val, RX_PD);
             txPd = Arithmetic.getBit(val, TX_PD);
             fsPd = Arithmetic.getBit(val, FS_PD);
             corePd = Arithmetic.getBit(val, CORE_PD);
             biasPd = Arithmetic.getBit(val, BIAS_PD);
             resetN = Arithmetic.getBit(val, RESET_N);
 
             if (rxPd) {
                 receiver.deactivate();
             } else {
                 receiver.activate();
             }
 
             if (txPd) {
                 transmitter.deactivate();
             } else {
                 transmitter.activate();
             }
 
             if (rxtx && !Arithmetic.getBit(oldVal, RXTX)) {
                 // switch from receive to transmit
                 receiver.endReceive();
                 transmitter.transmit();
 
             } else if (!rxtx && Arithmetic.getBit(oldVal, RXTX)) {
                 // switch from transmit to receive
                 transmitter.endTransmit();
                 receiver.receive();
             }
 
             currentFrequencyRegister = fReg ? FREQ_B_reg : FREQ_A_reg;
 
             // TODO: Figure out how radio really resets..
 
             if (resetN && !Arithmetic.getBit(oldVal, RESET_N)) {
                 oldVal = val;
                 //resetRadio();
                 return;
             }
             oldVal = val;
 
         }
 
         protected void printStatus() {
             String rxtxS = rxtx ? "TX" : "RX";
             String fRegS = fReg ? "B" : "A";
 
             radioPrinter.println("CC1000[MAIN]: " + rxtxS + ", frequency register: " + fRegS + ", rx powerdown: "
                     + rxPd + ", tx powerdown: " + txPd + ", fs powerdown: " + fsPd + ", core powerdown: "
                     + corePd + ", bias powerdown: " + biasPd + ", reset: " + resetN);
         }
 
     }
 
     /**
      * A frequency register on the CC1000. It is divided into three 8-bit registers.
      */
     protected class FrequencyRegister extends State.RWIOReg {
         protected final FrequencySubRegister reg2;
         protected final FrequencySubRegister reg1;
         protected final FrequencySubRegister reg0;
 
         int frequency;
 
         // subId should be either A or b
         FrequencyRegister(String subId) {
 
             reg2 = new FrequencySubRegister("FREQ2" + subId);
             reg1 = new FrequencySubRegister("FREQ1" + subId);
             reg0 = new FrequencySubRegister("FREQ0" + subId);
 
             setFrequency(0x75a0cb); // default frequency is 0b 01111 0101 1010 0000 1100 1011,
         }
 
         protected void updateFrequency() {
             frequency = reg2.read() << 16;
             frequency |= reg1.read() << 8;
             frequency |= reg0.read();
         }
 
         protected void setFrequency(int frequency) {
             reg2.write((byte) ((0x00ff0000 & frequency) >> 16));
             reg1.write((byte) ((0x0000ff00 & frequency) >> 8));
             reg0.write((byte) ((0x000000ff & frequency)));
         }
 
         /** One of the three sub-registers in the 24-bit frequency register. */
         protected class FrequencySubRegister extends RadioRegister {
 
             FrequencySubRegister(String id) {
                 super(id, (byte) 0);
             }
 
             protected void decode(byte val) {
                 updateFrequency();
             }
         }
 
     }
 
     /**
      * The frequency separation register on the CC1000. It is divided into two
      * 8-bit registers.
      */
     protected class FrequencySeparationRegister extends State.RWIOReg {
         protected final SubRegister reg1 = new SubRegister("FSEP1");
         protected final SubRegister reg0 = new SubRegister("FSEP0");
 
         FrequencySeparationRegister() {
             setFrequencySeparation(0x59); // default frequency separation is 0b 0000 0000 0101 1001
         }
 
         int frequencySeparation;
 
         protected void updateFrequencySeparation() {
             frequencySeparation = (reg1.read() & 0x0f) << 8;
             frequencySeparation |= reg0.read();
         }
 
         protected void setFrequencySeparation(int val) {
             reg1.write((byte) ((0x0f00 & val) >> 8));
             reg0.write((byte) (0xff & val));
         }
 
         /** One of the two sub-registers in the 18-bit frequency separation register. */
         protected class SubRegister extends RadioRegister {
 
             SubRegister(String id) {
                 super(id, (byte) 0);
             }
 
             protected void decode(byte val) {
                 updateFrequencySeparation();
             }
 
         }
     }
 
     /**
      * The <code>CurrentRegister</code> controls various currents running through the
      * CC1000 wiring.
      */
     protected class CurrentRegister extends RadioRegister {
 
         final int[] VCO_CURRENT = {150, 250, 350, 450, 950, 1050, 1150, 1250,
                                    1450, 1550, 1650, 1750, 2250, 2350, 2450, 2550}; // in microamperes
 
         int vcoCurrent = 150;
 
         final double[] LO_DRIVE = {0.5, 1.0, 1.5, 2.0}; // in milliamperes
         double loDrive = 0.5;
 
         final int[] PA_DRIVE = {1, 2, 3, 4}; // in milliamperes
         int paDrive = 1;
 
         CurrentRegister() {
             super("CURRENT", (byte) 0xca);
             // default value 0b 1100 1010
         }
 
         protected void decode(byte val) {
             vcoCurrent = VCO_CURRENT[(val & 0xf0) >> 4];
             loDrive = LO_DRIVE[(val & 0x0c) >> 2];
             paDrive = PA_DRIVE[(val & 0x3)];
         }
 
         protected void printStatus() {
             radioPrinter.println("CC1000[CURRENT]: vco current: " + vcoCurrent + ", LO drive: " + loDrive
                     + ", PA drive:" + paDrive);
         }
     }
 
     protected class FrontEndRegister extends RadioRegister {
 
         final int[] BUF_CURRENT = {520, 690}; // in microamperes
         int bufCurrent = 520;
 
         final double[] LNA_CURRENT = {0.8, 1.4, 1.8, 2.2}; // in milliamperes
         double lnaCurrent = 0.8;
 
         final int IF_RSSI_INACTIVE = 0;
         final int IF_RSSI_ACTIVE = 1;
         final int IF_RSSI_MIXER = 2;
         int ifRSSI;
 
         boolean xoscBypassExternal;
 
         FrontEndRegister() {
             super("FRONT_END", (byte) 0);
         }
 
         protected void decode(byte val) {
             bufCurrent = BUF_CURRENT[(val & 0x20) >> 5];
             lnaCurrent = LNA_CURRENT[(val & 0x18) >> 3];
             ifRSSI = (val & 0x06) >> 1;
 
             xoscBypassExternal = Arithmetic.getBit(val, 0);
 
         }
     }
 
     protected class PA_POWRegister extends RadioRegister {
 
         int paHighPower;
         int paLowPower;
 
         PA_POWRegister() {
             super("PA_POW", (byte) 0x0f);
             // default value 0b 0000 1111
         }
 
         protected void decode(byte val) {
             paHighPower = (value & 0xf0) >> 4;
             paLowPower = (value & 0x0f);
         }
 
         protected void printStatus() {
             radioPrinter.println("CC1000[PA_POW]: PA high power: " + paHighPower + ", PA low power: " + paLowPower);
         }
     }
 
     protected class PLLRegister extends RadioRegister {
         boolean extFilter;
         int refDiv;
         boolean alarmDisable;
         boolean alarmHigh;
         boolean alarmLow;
 
         PLLRegister() {
             super("PLL", (byte) 0x10);
             // default value 0b 00010000
         }
 
         protected void decode(byte val) {
             extFilter = Arithmetic.getBit(val, 7);
             refDiv = (value & 0x78) >> 3;
             alarmDisable = Arithmetic.getBit(val, 2);
             alarmHigh = Arithmetic.getBit(val, 1);
             alarmLow = Arithmetic.getBit(val, 0);
         }
     }
 
     protected class LockRegister extends RadioRegister {
 
         final int LOCK_NORMAL = 0;
         final int LOCK_CONTINUOUS = 1;
         final int LOCK_INSTANT = 2;
         final int ALARM_H = 3;
         final int ALARM_L = 4;
         final int CAL_COMPLETE = 5;
         final int IF_OUT = 6;
         final int REFERENCE_DIVIDER = 7;
         final int TX_DPB = 8;
         final int MANCHESTER_VIOLATION = 9;
         final int RX_PDB = 10;
         // 11 undefined
         // 12 undefined
         final int LOCK_AVG_FILTER = 13;
         final int N_DIVIDER = 14;
         final int F_COMP = 15;
 
         final String[] LOCK_SELECT = {"LOCK NORMAL", "LOCK CONTINUOUS", "LOCK INSTANT", "ALARM HIGH", "ALARM LOW",
                                       "CAL COMPLETE", "IF OUT", "REFERENCE DIVIDER", "TX DPB", "MANCHESTER VIOLATION",
                                       "RX PDB", "NOT DEFINED (11)", "NOT DEFINED (12)", "LOCK AVG FILTER",
                                       "N DIVIDER", "F COMP"};
 
         int lockSelect;
 
         boolean pllLockLength;
 
         //PLL_LOCK_ACCURACY
         final int[] SETS_LOCK_THRESHOLD = {127, 31};
 
         int setsLockThreshold = 127;
 
         final int[] RESET_LOCK_THRESHOLD = {111, 15};
         int resetLockThreshold = 111;
 
         boolean lockInstant;
         boolean lockContinuous;
 
         LockRegister() {
             super("LOCK", (byte) 0);
         }
 
         protected void decode(byte val) {
             lockSelect = (val & 0xf0) >> 4;
             int pllLockAccuracy = (val & 0x0c) >> 2;
             setsLockThreshold = SETS_LOCK_THRESHOLD[pllLockAccuracy];
             resetLockThreshold = RESET_LOCK_THRESHOLD[pllLockAccuracy];
 
             pllLockLength = Arithmetic.getBit(val, 2);
             lockInstant = Arithmetic.getBit(val, 1);
             lockContinuous = Arithmetic.getBit(val, 0);
         }
 
 
         protected void printStatus() {
             radioPrinter.println("CC1000[LOCK]: lock select: " + LOCK_SELECT[lockSelect] + ", sets lock threshold: "
                     + setsLockThreshold + ", reset lock threshold: " + resetLockThreshold +
                     ", lock instant: " + lockInstant + ", lockContinuous: " + lockContinuous);
         }
 
         public byte read() {
             return (byte) (super.read() & 0x03);
         }
     }
 
     protected class CALRegister extends RadioRegister {
 
         final int CAL_START = 7;
         final int CAL_DUAL = 6;
         final int CAL_WAIT = 5;
         final int CAL_CURRENT = 4;
         final int CAL_COMPLETE = 3;
 
         boolean calStart;
         boolean calDual;
         boolean calWait;
         boolean calCurrent;
         boolean calComplete;
 
         final int CAL_ITERATE_NORMAL = 0x6;
 
         int calIterate;
 
         Calibrate calibrate = new Calibrate();
 
 
         CALRegister() {
             super("CAL", (byte) 0x05);
             // default value 0b 00000101
         }
 
         boolean calibrating;
 
         protected void decode(byte val) {
             boolean oldCalStart = calStart;
             calStart = Arithmetic.getBit(val, 7);
             calDual = Arithmetic.getBit(val, 6);
             calWait = Arithmetic.getBit(val, 5);
             calCurrent = Arithmetic.getBit(val, 4);
             calComplete = Arithmetic.getBit(val, 3);
 
             calIterate = (value & 0x7);
 
             if (!oldCalStart && calStart && !calibrating) {
                 calibrating = true;
                 sim.insertEvent(calibrate, mcu.millisToCycles(1));  // This interval is probably bogus.
                 // TODO: determine real value for the delay.
             }
 
         }
 
         protected void printStatus() {
             radioPrinter.println("CC1000[CAL]: cal start: " + calStart + ", cal dual: " + calDual +
                     ", cal wait: " + calWait + ", cal current: " + calCurrent + ", calComplete: " + calComplete +
                     ", cal iterate: " + calIterate + " ... " + sim.getState().getCycles());
         }
 
         /** */
         protected class Calibrate implements Simulator.Event {
 
             public void fire() {
                 // TODO: finish implementation of calibrate routine
                 writeBit(CAL_START, false);
                 writeBit(CAL_COMPLETE, true);
                 LOCK_reg.write((byte) ((LOCK_reg.read() & 0x0f) | 0x50)); // LOCK = CAL_COMPLETE
                 if (radioPrinter.enabled) {
                     radioPrinter.println("CC1000: Calibration complete ");
                 }
                 calibrating = false;
             }
         }
 
     }
 
     protected class Modem2Register extends RadioRegister {
 
         boolean peakDetect;
         int peakLevelOffset;
 
         Modem2Register() {
             super("MODEM2", (byte) 0x96);
             // default value 0b 1001 0110
         }
 
         protected void decode(byte val) {
             peakDetect = Arithmetic.getBit(val, 7);
             peakLevelOffset = val & 0x7f;
         }
     }
 
     protected class Modem1Register extends RadioRegister {
 
         int mlimit;
 
         boolean lockAvgN;
 
         boolean lockAvgMode;
 
         final int[] SETTLING = {11, 22, 43, 86};
         int settling = 11;
 
         boolean modemResetN;
 
         Modem1Register() {
             super("MODEM1", (byte) 0x67);
             // default value 0b 0110 0111
         }
 
         protected void decode(byte val) {
             mlimit = (val & 0xe0) >> 5;
             lockAvgN = Arithmetic.getBit(val, 4);
             lockAvgMode = Arithmetic.getBit(val, 3);
             settling = SETTLING[(val & 0x06) >> 1];
             modemResetN = Arithmetic.getBit(val, 0);
         }
     }
 
 
     /**
      * The baud rate of the system is determined by values on the MODEM0 register.
      * TinyOS uses a baud rate of 19.2 kBaud with manchester encoding, which translates
      * into 9.6 kbps of data.
      */
     protected class Modem0Register extends RadioRegister {
         final int[] BAUDRATE = {600, 1200, 2400, 4800, 9600, 19200, 0, 0};
         int baudrate = 2400;
 
         final int DATA_FORMAT_NRZ = 0;
         final int DATA_FORMAT_MANCHESTER = 1;
         final int DATA_FORMAT_UART = 2;
         int dataFormat = DATA_FORMAT_MANCHESTER;
 
         final int[] XOSC_FREQ = {3686400, // 3-4 Mhz
                                  7372800, // 6-8 Mhz
                                  1105920, // 9-12 Mhz
                                  1474560};// 12-16 Mhz
 
         int xoscFreq = 3686400;
 
         long byteTimeCycles;
 
         Modem0Register() {
             super("MODEM0", (byte) 0x24);
             decode(value);
             // default value 0b 0010 0100
         }
 
         protected void decode(byte val) {
 
             baudrate = BAUDRATE[(val & 0x70) >> 4];
             dataFormat = (val & 0x0c) >> 2;
             xoscFreq = XOSC_FREQ[(val & 0x2)];
 
             boolean manchester = dataFormat == DATA_FORMAT_MANCHESTER;
             int bitsPerSecond = baudrate / (manchester ? 2 : 1);
             int bytesPerSecond = bitsPerSecond / 8;
             double byteTimeSeconds = 1.0 / bytesPerSecond;
             double byteTimeMillis = byteTimeSeconds * 1000.0;
 
             byteTimeCycles = mcu.millisToCycles(byteTimeMillis);
         }
 
         protected void printStatus() {
             radioPrinter.println("CC1000[MODEM0]: ...");
         }
     }
 
     protected class MatchRegister extends RadioRegister {
         int rxMatch;
         int txMatch;
 
         MatchRegister() {
             super("MATCH", (byte) 0);
         }
 
         protected void decode(byte val) {
             rxMatch = (val & 0xf0) >> 4;
             txMatch = (val & 0x0f);
         }
 
     }
 
     protected class FSCTRLRegister extends RadioRegister {
 
         boolean fsResetN;
 
         FSCTRLRegister() {
             super("FSCTRL", (byte) 0x01);
             // default value 0b 0000 0001
         }
 
         protected void decode(byte val) {
             fsResetN = Arithmetic.getBit(val, 0);
         }
     }
 
     protected class PrescalerRegister extends RadioRegister {
 
         final double[] PRE_SWING = {1.0, 2 / 3, 7 / 3, 5 / 3};
         double preSwing = 1.0;
 
         final double[] PRE_CURRENT = {1.0, 2 / 3, 1 / 2, 2 / 5};
         double preCurrent = 1.0;
 
         boolean ifInput;
         boolean ifFront;
 
         PrescalerRegister() {
             super("PRESCALER", (byte) 0);
         }
 
         protected void decode(byte val) {
             preSwing = PRE_SWING[(val & 0xc0) >> 6];
             preCurrent = PRE_CURRENT[(val & 0x30) >> 4];
             ifInput = Arithmetic.getBit(val, 3);
             ifFront = Arithmetic.getBit(val, 4);
         }
 
     }
 
     /**
      * A CC1000 Controller class for the ATMega128L microcontroller cpu. Installing
      * an ATMega128L into this class connects the microcontroller to this radio.
      * Data is communicated over the SPI interface, on which the CC1000 is the master.
      * RSSI data from the CC1000 is available to the ATMega128L though the ADC (analog to
      * digital converter).
      */
     public class ATMega128LController implements Radio.RadioController, ATMega128L.SPIDevice, ATMega128L.ADCInput {
 
         SerialConfigurationInterface pinReader;
 
         public ATMega128L.SPIDevice connectedDevice;
         private final TransferTicker ticker;
 
         private Verbose.Printer printer = Verbose.getVerbosePrinter("sim.cc1000.data");
 
         ATMega128LController() {
             ticker = new TransferTicker();
         }
 
         public void enable() {
             ticker.activateTicker();
         }
 
         public void disable() {
             if (MAIN_reg.rxPd && MAIN_reg.txPd) ticker.deactivateTicker();
         }
 
         /**
          * The <code>TransferTicker</code> class is responsible for timing/facilitating
          * transfer between the radio and the connected microcontroller. The receiveFrame(),
          * transmitFrame() methods from the SPIDevice interface are used.
          */
         private class TransferTicker implements Simulator.Event {
             private boolean tickerOn;
 
 
             public void activateTicker() {
                 if (!tickerOn) {
                     tickerOn = true;
                     sim.insertEvent(ticker, 40000); // TODO: the 40000 is just guessed off the top
                                                     // of my head. Determine what the real delay is.
                 }
             }
 
             public void deactivateTicker() {
                 tickerOn = false;
                 sim.removeEvent(this);
             }
 
             public void fire() {
 
                 SPIFrame frame = connectedDevice.transmitFrame();
 
                 if (MAIN_reg.rxtx && !MAIN_reg.txPd) {
                     receiveFrame(frame);
                 }
 
                 connectedDevice.receiveFrame(transmitFrame());
 
                 if (tickerOn) {
                     sim.insertEvent(this, Radio.TRANSFER_TIME);
 
                 }
             }
         }
 
         byte oldData;
 
         /**
          * <code>receiveFrame</code> receives an <code>SPIFrame</code> from a connected device.
          * If the radio is in a transmission state, this should be the next frame sent into the
          * air.
          */
         public void receiveFrame(SPIFrame frame) {
             if (printer.enabled) {
                 printer.println("CC1000: sending " + (char) frame.data + ", " + Integer.toHexString(0xff & frame.data));
                 if (oldData == (byte) 0x03) {
                     printer.println("Int Data : " + hex(frame.data));
                 }
             }
             oldData = frame.data;
             long currentTime = sim.getState().getCycles();
 
             // data, frequency, origination
             if (!MAIN_reg.txPd && MAIN_reg.rxtx) {
                 new Transmit(new RadioPacket(frame.data, 0, currentTime));
             }
 
         }
 
         /**
          * <code>Transmit</code> is an event that transmits a packet of data after
          * a one bit period delay.
          */
         protected class Transmit implements Simulator.Event {
             final Radio.RadioPacket packet;
 
             Transmit(Radio.RadioPacket packet) {
                 this.packet = packet;
                 sim.insertEvent(this, Radio.TRANSFER_TIME / 8);
             }
 
             public void fire() {
                 transmit(packet);
             }
         }
 
         /**
          * Transmits an <code>SPIFrame</code> to be received by the connected device.
          * This frame is either the last byte of data received or a zero byte.
          */
         public SPIFrame transmitFrame() {
             SPIFrame frame;
 
             if (MAIN_reg.rxtx && MAIN_reg.txPd) {
                 frame = new SPIFrame((byte)0x00);
             } else if (!receivedBuffer.isEmpty()) {
                 Radio.RadioPacket receivedPacket = (Radio.RadioPacket) receivedBuffer.removeFirst();
 
                 // Apparently TinyOS expects received data to be inverted
                 byte data = MAIN_reg.rxtx ? receivedPacket.data : (byte) ~receivedPacket.data;
                 frame = new SPIFrame(data);
                 receivedPacket = null;
                 if (printer.enabled) {
                     printer.println("CC1000: received " + hex(frame.data));
                 }
             } else {
                 frame = new SPIFrame((byte)0x00);
             }
 
 
             return frame;
         }
 
 
         public void connect(ATMega128L.SPIDevice d) {
             connectedDevice = d;
         }
 
         public int getLevel() {
             // ask the air for the current RSSI value
             return air.sampleRSSI(CC1000Radio.this);
         }
 
         //////////////////////////
 
         public void install(Microcontroller mcu) {
             pinReader = new SerialConfigurationInterface(mcu);
 
             if (mcu instanceof ATMega128L) {
                 ATMega128L atm = (ATMega128L) mcu;
                 atm.connectADCInput(this, 0);
                 atm.connectSPIDevice(this);
             }
         }
 
 
     }
 
 
     /** TODO: determine if I will really need this for anything more than debugging.
      * This class is more or less a state machine on the status of the receiver for
      * this radio. It is "activated" when the receiver unit is powered up and it is
      * "receiving" when the radio is in receive mode. */
     protected class Receiver {
 
         boolean activated;
         boolean receiving;
 
         protected void activate() {
             if (activated) {
                 return;
             }
 
             activated = true;
 
             if (radioPrinter.enabled) {
                 radioPrinter.println("CC1000: RX activated");
             }
 
             controller.enable();
         }
 
         protected void deactivate() {
             if (!activated) {
                 return;
             }
 
             activated = false;
 
             if (radioPrinter.enabled) {
                 radioPrinter.println("CC1000: RX de-activated");
             }
 
             controller.disable();
         }
 
         protected void receive() {
             receiving = true;
             if (radioPrinter.enabled) {
                 radioPrinter.println("CC1000: RX receiving");
             }
         }
 
         protected void endReceive() {
             receiving = false;
             if (radioPrinter.enabled) {
                 radioPrinter.println("CC1000: RX end receiving");
             }
 
         }
     }
 
     /**
      * This class is a state machine similar to <code>Receiver</code>, but for the
      * transmitter on the radio. So it is "activated" when the transmitter is powered up
      * and "transmitting" when the radio is in transmit mode. It is very likely that
      * moving this functionality into the <code>MainRegister</code> class would be a good
      * design decision.
      */
     protected class Transmitter {
 
         boolean activated;
         boolean transmitting;
 
         protected void activate() {
             if (activated) {
                 return;
             }
 
             activated = true;
 
             if (radioPrinter.enabled) {
                 radioPrinter.println("CC1000: TX activated");
             }
 
             controller.enable();
         }
 
         protected void deactivate() {
             if (!activated) {
                 return;
             }
 
             activated = false;
 
             if (radioPrinter.enabled) {
                 radioPrinter.println("CC1000: TX de-activated");
             }
 
             controller.disable();
 
         }
 
         protected void transmit() {
             transmitting = true;
             if (radioPrinter.enabled) {
                 radioPrinter.println("CC1000: TX transmitting");
             }
         }
 
         protected void endTransmit() {
             transmitting = false;
             if (radioPrinter.enabled) {
                 radioPrinter.println("CC1000: TX end transmitting");
             }
         }
     }
 
     /**
      * Reads the three pins used in the three wire serial configuration interface.
      * Microcontrollers can program this radio by communication over this interfance.
      * Debug output for communication over this interface is available on
      * "sim.cc1000.pinconfig"
      */
     protected class SerialConfigurationInterface {
 
         byte address;
         boolean write;
         byte data;
         boolean value;
 
         byte readData;
         boolean readValue;
 
         int bitsRead;
 
         Verbose.Printer readerPrinter = Verbose.getVerbosePrinter("sim.cc1000.pinconfig");
 
         Microcontroller.Pin.Input paleInput;
 
         SerialConfigurationInterface(Microcontroller mcu) {
 
             //install outputs
             mcu.getPin(31).connect(new PCLKOutput());
             mcu.getPin(32).connect(new PDATAOutput());
             mcu.getPin(32).connect(new PDATAInput());

         }
 
         /**
          * Clocking the PCLK pin is what drives the action of the configuration interface.
          * One bit of data on PDATA per clock.
          */
         protected class PCLKOutput implements Microcontroller.Pin.Output {
 
             public void write(boolean level) {
                 if (!level) { // clr
                     action();
                 } else { // set. false
 
                 }
             }
 
             public void enableOutput() {
             }
 
             public void disableOutput() {
             }
 
         }
 
         protected class PDATAInput implements Microcontroller.Pin.Input {
             public boolean read() {
                 return readValue;
             }
 
             public void enableInput() {
             }
 
             public void disableInput() {
             }
         }
 
         protected class PDATAOutput implements Microcontroller.Pin.Output {
 
             public void write(boolean level) {
                 value = level;
             }
 
             public void enableOutput() {
             }
 
             public void disableOutput() {
             }
         }
 
        // TODO install PALEOutput
         // PALE is the address latch. It really isn't necessary, if you assume
         // that software will respect the packet formatting.
         // If you don't, then PALE should be implemented.
        /*
         protected class PALEOutput implements Microcontroller.Pin.Output {
             public void write(boolean level) {
                if(level) {
                     bitsRead = 0;
                 } else {
                     bitsRead = 8;
                 }
             }
 
             public void enableOutput() {}
 
             public void disableOutput() {}
         }
        */
 
         public void action() {
             if (bitsRead < 7) {
                 address <<= 1;
                 address |= value ? 0x1 : 0x0;
             } else if (bitsRead == 7) {
                 write = value;
                 if (!write) {
                     readData = registers[0x7f & address].read();
                     readValue = Arithmetic.getBit(readData, 7);
 
                 }
             } else {
                 if (write) {
                     data <<= 1;
                     data |= value ? 0x1 : 0x0;
                 } else { // read
                     readValue = Arithmetic.getBit(readData, 7);
                     readData <<= 1;
                 }
             }
             bitsRead++;
             if (bitsRead == 16) {
                 String rw = write ? " write " : " read ";
                 byte printData = write ? data : registers[(0x7f & address)].read();
                 readerPrinter.println("Address " + Integer.toHexString(0x7f & address) + rw + " data " + Integer.toBinaryString(0xff & printData));
                 bitsRead = 0;
 
                 if (write) {
 
                     registers[0x7f & address].write(data);
 
                 }
             }
         }
 
     }
 
     /** Get the <code>Simulator</code> on which this radio is running. */
     public Simulator getSimulator() {
         return sim;
     }
 
     /** Output helper method. */
     private static String hex(byte val) {
         return Integer.toHexString(0xff & val);
     }
 
 
 }
