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
 
 package avrora.sim.radio;
 
 import avrora.sim.FiniteStateMachine;
 import avrora.sim.Simulator;
 import avrora.sim.clock.Clock;
 import avrora.sim.energy.Energy;
 import avrora.sim.mcu.*;
 import avrora.sim.util.SimUtil;
 import avrora.sim.util.TransactionalList;
 import cck.text.StringUtil;
 import cck.util.Arithmetic;
 import cck.util.Util;
 
 /**
  * The <code>CC1000Radio</code> class is a simulation of the CC1000 radio for use with avrora. The CC1000
  * radio is used with the Mica2 platform in the real world. Verbose printers for this class include
  * "sim.cc1000", "sim.cc1000.data", "sim.cc1000.pinconfig".
  *
  * @author Daniel Lee
  */
 public class CC1000Radio implements Radio {
 
     /**
      * LegacyRegister addresses.
      */
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
 
     protected static final String[] allModeNames = RadioEnergy.allModeNames();
     protected static final int[][] ttm = FiniteStateMachine.buildSparseTTM(allModeNames.length, 0);
 
 
     protected RadioRegister[] registers  = new RadioRegister[0x47];
 
     /**
      * Registers
      */
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
 
     protected final SimUtil.SimPrinter radioPrinter;
 
     protected final Receiver receiver = new Receiver();
     protected final Transmitter transmitter = new Transmitter();
 
     protected final ProbeList probes;
     protected final long xoscFrequency;
 
     protected FrequencyRegister currentFrequencyRegister;
 
     /**
      * Connected Microcontroller, Simulator and SimulatorThread should all correspond.
      */
     protected final Microcontroller mcu;
     protected final Simulator sim;
     protected final Clock clock;
     protected final FiniteStateMachine stateMachine;
 
     protected Radio.RadioController controller;
 
     /**
      * Radio environment into which this radio broadcasts.
      */
     protected RadioAir air;
 
     /**
      * The <code>ProbeList</code> class just keeps track of a list of probes.
      */
     class ProbeList extends TransactionalList implements Radio.RadioProbe {
         public void fireAtPowerChange(Radio r, int newPower) {
             beginTransaction();
             for (Link pos = head; pos != null; pos = pos.next)
                 ((RadioProbe)pos.object).fireAtPowerChange(r, newPower);
             endTransaction();
         }
 
         public void fireAtFrequencyChange(Radio r, double freq)  {
             beginTransaction();
             for (Link pos = head; pos != null; pos = pos.next)
                 ((RadioProbe)pos.object).fireAtFrequencyChange(r, freq);
             endTransaction();
         }
 
         public void fireAtBitRateChange(Radio r, int newbitrate)  {
             beginTransaction();
             for (Link pos = head; pos != null; pos = pos.next)
                 ((RadioProbe)pos.object).fireAtBitRateChange(r, newbitrate);
             endTransaction();
         }
 
         public void fireAtTransmit(Radio r, Radio.Transmission p)  {
             beginTransaction();
             for (Link pos = head; pos != null; pos = pos.next)
                 ((RadioProbe)pos.object).fireAtTransmit(r, p);
             endTransaction();
         }
 
         public void fireAtReceive(Radio r, Radio.Transmission p) {
             beginTransaction();
             for (Link pos = head; pos != null; pos = pos.next)
                 ((RadioProbe)pos.object).fireAtReceive(r, p);
             endTransaction();
         }
     }
 
     public CC1000Radio(Microcontroller mcu, long xfreq) {
         xoscFrequency = xfreq;
 
         probes = new ProbeList();
 
         this.mcu = mcu;
         this.sim = mcu.getSimulator();
         this.clock = sim.getClock();
 
         radioPrinter = SimUtil.getPrinter(sim, "radio.cc1000");
 
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
         controller = new ATMegaController();
         controller.install(mcu);
 
         //setup energy recording
         Simulator simulator = mcu.getSimulator();
 
         stateMachine = new FiniteStateMachine(simulator.getClock(), RadioEnergy.startMode, allModeNames, ttm);
 
         new Energy("Radio", RadioEnergy.modeAmpere, stateMachine);
 
     }
 
     /**
      * The <code>getFiniteStateMachine()</code> method gets a reference to the finite state
      * machine that represents this radio's state. For example, there are states corresponding
      * to "on", "off", "transmitting", and "receiving". The state names and numbers will vary
      * by radio implementation. The <code>FiniteStateMachine</code> instance allows the user
      * to instrument the state transitions in order to gather information during simulation.
      * @return a reference to the finite state machine for this radio
      */
     public FiniteStateMachine getFiniteStateMachine() {
         return stateMachine;
     }
 
     /**
      * The <code>insertProbe()</code> method inserts a probe into a radio. The probe is then
      * notified when the radio changes power, frequency, baud rate, or transmits or receives
      * a byte.
      * @param p the probe to insert on this radio
      */
     public void insertProbe(RadioProbe p) {
         probes.add(p);
     }
 
     /**
      * The <code>removeProbe()</code> method removes a probe on this radio.
      * @param p the probe to remove from this radio instance
      */
     public void removeProbe(RadioProbe p) {
         probes.remove(p);
     }
 
     /**
      * The <code>RadioRegister</code> is an abstract register grouping together registers on the CC1000
      * radio.
      */
     protected abstract class RadioRegister {
         protected final String id; // name of this register
         protected final byte def; // default value
 
         protected byte value; // current value of this register
 
         RadioRegister(String id, byte def) {
             this.id = id;
             this.def = def;
             this.value = def;
         }
 
         public void write(byte val) {
             value = val;
             decode(value);
             if (radioPrinter.enabled) {
                 printStatus();
             }
         }
 
         public void writeBit(int bit, boolean val) {
             value = Arithmetic.setBit(value, bit, val);
             decode(value);
             if (radioPrinter.enabled) {
                 printStatus();
             }
         }
 
         protected abstract void decode(byte val);
 
         protected void printStatus() {
             // default: do nothing
         }
 
         protected void reset() {
             write(def);
         }
 
 
     }
 
     /**
      * The <code>DummyRegister</code> is a filler class for registers within the 7-bit address space of the
      * radio registers, but do not actually exist/do anything in the real radio.
      */
     protected class DummyRegister extends RadioRegister {
         DummyRegister(int i) {
             super("Dummy " + Integer.toHexString(i), (byte)0);
         }
 
         protected void decode(byte val) {
         }
     }
 
     /**
      * The main register on the CC1000.
      */
     protected class MainRegister extends RadioRegister {
         public static final int RXTX = 7;      // 0: RX, 1: TX
         public static final int F_REG = 6;     // 0: A, 1: B
         public static final int RX_PD = 5;     // Power down of RX part of interface
         public static final int TX_PD = 4;     // Power down of TX part of interface
         public static final int FS_PD = 3;     // Power down of Frequency Synthesizer
         public static final int CORE_PD = 2;   // Power down of Crystal Oscillator Core
         public static final int BIAS_PD = 1;   // Power down of BIAS and Crystal Oscillator Buffer
         public static final int RESET_N = 0;   // Reset other registers to default value
 
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
             super("MAIN", (byte)0x3e);
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
 
             if (val != oldVal) {
                 int state = computeState();
                 stateMachine.transition(state);
             }
 
             oldVal = val;
 
 
         }
 
         private int computeState() {
             // TODO: reduce this code to compute state more easily
             int state;
             if (corePd)
                 state = 1; //power down state
             else
                 state = 2; // core, e.g. crystal on state
             if (!corePd && !biasPd)
                 state = 3; // crystal and bias on state
             if (!corePd && !biasPd && !fsPd)
                 state = 4; // crystal, bias and synth. on
             if (!corePd && !biasPd && !fsPd && !rxtx && !rxPd)
                 state = 5; // receive state
             if (!corePd && !biasPd && !fsPd && rxtx && !txPd)
                 state = PA_POW_reg.getPower() + 6;
             return state;
         }
 
         protected void printStatus() {
             String rxtxS = rxtx ? "TX" : "RX";
             String fRegS = fReg ? "B" : "A";
             StringBuffer buf = new StringBuffer(100);
 
             buf.append("CC1000[MAIN]: ");
             buf.append(rxtxS);
             buf.append(", freg: ");
             buf.append(fRegS);
             buf.append(", rx pd: ");
             buf.append(StringUtil.toBit(rxPd));
             buf.append(", tx pd: ");
             buf.append(StringUtil.toBit(txPd));
             buf.append(", fs pd: ");
             buf.append(StringUtil.toBit(fsPd));
             buf.append(", core pd: ");
             buf.append(StringUtil.toBit(corePd));
             buf.append(", bias pd: ");
             buf.append(StringUtil.toBit(biasPd));
             buf.append(", reset: ");
             buf.append(StringUtil.toBit(resetN));
             radioPrinter.println(buf.toString());
         }
 
     }
 
     /**
      * A frequency register on the CC1000. It is divided into three 8-bit registers.
      */
     protected class FrequencyRegister {
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
             frequency = 0x00ff0000 & (reg2.value << 16);
             frequency |= 0x0000ff00 & (reg1.value << 8);
             frequency |= 0x000000ff & reg0.value;
         }
 
         protected void setFrequency(int frequency) {
             reg2.write((byte)((0x00ff0000 & frequency) >> 16));
             reg1.write((byte)((0x0000ff00 & frequency) >> 8));
             reg0.write((byte)((0x000000ff & frequency)));
         }
 
         /**
          * One of the three sub-registers in the 24-bit frequency register.
          */
         protected class FrequencySubRegister extends RadioRegister {
 
             FrequencySubRegister(String id) {
                 super(id, (byte)0);
             }
 
             protected void decode(byte val) {
                 updateFrequency();
             }
         }
 
     }
 
     /**
      * The frequency separation register on the CC1000. It is divided into two 8-bit registers.
      */
     protected class FrequencySeparationRegister {
         protected final SubRegister reg1 = new SubRegister("FSEP1");
         protected final SubRegister reg0 = new SubRegister("FSEP0");
 
         FrequencySeparationRegister() {
             setFrequencySeparation(0x59); // default frequency separation is 0b 0000 0000 0101 1001
         }
 
         int frequencySeparation;
 
         protected void updateFrequencySeparation() {
             frequencySeparation = (reg1.value & 0x0f) << 8;
             frequencySeparation |= reg0.value;
         }
 
         protected void setFrequencySeparation(int val) {
             reg1.write((byte)((0x0f00 & val) >> 8));
             reg0.write((byte)(0xff & val));
         }
 
         /**
          * One of the two sub-registers in the 18-bit frequency separation register.
          */
         protected class SubRegister extends RadioRegister {
 
             SubRegister(String id) {
                 super(id, (byte)0);
             }
 
             protected void decode(byte val) {
                 updateFrequencySeparation();
             }
 
         }
     }
 
     static final int[] VCO_CURRENT = {150, 250, 350, 450, 950, 1050, 1150, 1250,
                                    1450, 1550, 1650, 1750, 2250, 2350, 2450, 2550}; // in microamperes
 
     static final double[] LO_DRIVE = {0.5, 1.0, 1.5, 2.0}; // in milliamperes
     static final int[] PA_DRIVE = {1, 2, 3, 4}; // in milliamperes
 
     /**
      * The <code>CurrentRegister</code> controls various currents running through the CC1000 wiring.
      */
     protected class CurrentRegister extends RadioRegister {
 
         int vcoCurrent = 150;
         double loDrive = 0.5;
         int paDrive = 1;
 
         CurrentRegister() {
             super("CURRENT", (byte)0xca);
             // default value 0b 1100 1010
         }
 
         protected void decode(byte val) {
             vcoCurrent = VCO_CURRENT[(val & 0xf0) >> 4];
             loDrive = LO_DRIVE[(val & 0x0c) >> 2];
             paDrive = PA_DRIVE[(val & 0x3)];
         }
 
         protected void printStatus() {
             radioPrinter.println("CC1000[CURRENT]: vco current: " + vcoCurrent + ", LO drive: " + loDrive
                     + ", PA drive: " + paDrive);
         }
     }
 
     static final int[] BUF_CURRENT = {520, 690}; // in microamperes
     static final double[] LNA_CURRENT = {0.8, 1.4, 1.8, 2.2}; // in milliamperes
 
     protected class FrontEndRegister extends RadioRegister {
 
         int bufCurrent = 520;
 
         double lnaCurrent = 0.8;
 
         static final int IF_RSSI_INACTIVE = 0;
         static final int IF_RSSI_ACTIVE = 1;
         static final int IF_RSSI_MIXER = 2;
         int ifRSSI;
 
         boolean xoscBypassExternal;
 
         FrontEndRegister() {
             super("FRONT_END", (byte)0);
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
             super("PA_POW", (byte)0x0f);
             // default value 0b 0000 1111
         }
 
         protected void decode(byte val) {
             paHighPower = (value & 0xf0) >> 4;
             paLowPower = (value & 0x0f);
 
             probes.fireAtPowerChange(CC1000Radio.this, getPower());
 
             //start energy tracking
             //check for transmission mode enabled
             if (!MAIN_reg.corePd && !MAIN_reg.biasPd && !MAIN_reg.fsPd && MAIN_reg.rxtx && !MAIN_reg.txPd)
                 stateMachine.transition(getPower() + 6);
         }
 
         protected int getPower() {
             return value & 0xff;
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
             super("PLL", (byte)0x10);
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
 
     //PLL_LOCK_ACCURACY
     static final int[] SETS_LOCK_THRESHOLD = {127, 31};
     static final int[] RESET_LOCK_THRESHOLD = {111, 15};
 
     protected class LockRegister extends RadioRegister {
 
         static final int LOCK_NORMAL = 0;
         static final int LOCK_CONTINUOUS = 1;
         static final int LOCK_INSTANT = 2;
         static final int ALARM_H = 3;
         static final int ALARM_L = 4;
         static final int CAL_COMPLETE = 5;
         static final int IF_OUT = 6;
         static final int REFERENCE_DIVIDER = 7;
         static final int TX_DPB = 8;
         static final int MANCHESTER_VIOLATION = 9;
         static final int RX_PDB = 10;
         // 11 undefined
         // 12 undefined
         static final int LOCK_AVG_FILTER = 13;
         static final int N_DIVIDER = 14;
         static final int F_COMP = 15;
 
         final String[] LOCK_SELECT = {"LOCK NORMAL", "LOCK CONTINUOUS", "LOCK INSTANT", "ALARM HIGH", "ALARM LOW",
                                       "CAL COMPLETE", "IF OUT", "REFERENCE DIVIDER", "TX DPB", "MANCHESTER VIOLATION",
                                       "RX PDB", "NOT DEFINED (11)", "NOT DEFINED (12)", "LOCK AVG FILTER",
                                       "N DIVIDER", "F COMP"};
 
         int lockSelect;
 
         boolean pllLockLength;
 
         int setsLockThreshold = 127;
         int resetLockThreshold = 111;
 
         boolean lockInstant;
         boolean lockContinuous;
 
         LockRegister() {
             super("LOCK", (byte)0);
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
             StringBuffer buf = new StringBuffer(100);
             buf.append("CC1000[LOCK]: lock select: ");
             buf.append(LOCK_SELECT[lockSelect]);
             buf.append(", set thr: ");
             buf.append(setsLockThreshold);
             buf.append(", reset thr: ");
             buf.append(resetLockThreshold);
             buf.append(", inst: ");
             buf.append(StringUtil.toBit(lockInstant));
             buf.append(", contin: ");
             buf.append(StringUtil.toBit(lockContinuous));
             radioPrinter.println(buf.toString());
         }
 
         public byte read() {
             return (byte)(value & 0x03);
         }
     }
 
     protected class CALRegister extends RadioRegister {
 
         static final int CAL_START = 7;
         static final int CAL_DUAL = 6;
         static final int CAL_WAIT = 5;
         static final int CAL_CURRENT = 4;
         static final int CAL_COMPLETE = 3;
 
         boolean calStart;
         boolean calDual;
         boolean calWait;
         boolean calCurrent;
         boolean calComplete;
 
         static final int CAL_ITERATE_NORMAL = 0x6;
 
         int calIterate;
 
         Calibrate calibrate = new Calibrate();
 
 
         CALRegister() {
             super("CAL", (byte)0x05);
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
                 //OL: calibration time depends on the reference frequency
                 //worst case is 34ms
                 //it is determined with: 34ms * 1MHz / (Fxosc / REFDIV)
                 //with Fxosc is 14.7456 MHz for CC1000 on Mica2
                 //and REFDIV is set in the PLL register 
                 //in the current TinyOS version (1.1.7) REFDIV seems to be 14
                 //resulting in a delay of a little more than 32ms 
                 //Reference: CC1000 datasheet (rev 2.1) pages 20 and 22
                 double calMs = (34.0 * 1000000.0 / 14745600.0) * PLL_reg.refDiv;
                 clock.insertEvent(calibrate, clock.millisToCycles(calMs));
             }
 
         }
 
         protected void printStatus() {
             StringBuffer buf = new StringBuffer(100);
             buf.append("CC1000[CAL]: cal start: ");
             buf.append(StringUtil.toBit(calStart));
             buf.append(", dual: ");
             buf.append(StringUtil.toBit(calDual));
             buf.append(", wait: ");
             buf.append(StringUtil.toBit(calWait));
             buf.append(", current: ");
             buf.append(StringUtil.toBit(calCurrent));
             buf.append(", complete: ");
             buf.append(StringUtil.toBit(calComplete));
             buf.append(", iterate: ");
             buf.append(calIterate);
             radioPrinter.println(buf.toString());
         }
 
         /** */
         protected class Calibrate implements Simulator.Event {
 
             public void fire() {
                 // TODO: finish implementation of calibrate routine
                 writeBit(CAL_START, false);
                 writeBit(CAL_COMPLETE, true);
                 LOCK_reg.write((byte)((LOCK_reg.read() & 0x0f) | 0x50)); // LOCK = CAL_COMPLETE
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
             super("MODEM2", (byte)0x96);
             // default value 0b 1001 0110
         }
 
         protected void decode(byte val) {
             peakDetect = Arithmetic.getBit(val, 7);
             peakLevelOffset = val & 0x7f;
         }
     }
 
     static final int[] SETTLING = {11, 22, 43, 86};
 
     protected class Modem1Register extends RadioRegister {
 
         int mlimit;
 
         boolean lockAvgN;
 
         boolean lockAvgMode;
 
         int settling = 11;
 
         boolean modemResetN;
 
         Modem1Register() {
             super("MODEM1", (byte)0x67);
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
 
     static final int[] BAUDRATE = {600, 1200, 2400, 4800, 9600, 19200, 0, 0};
     static final int[] XOSC_FREQ = {3686400, // 3-4 Mhz
                              7372800, // 6-8 Mhz
                              1105920, // 9-12 Mhz
                              1474560};// 12-16 Mhz
 
     /**
      * The baud rate of the system is determined by values on the MODEM0 register. TinyOS uses a baud rate of
      * 19.2 kBaud with manchester encoding, which translates into 9.6 kbps of data.
      */
     protected class Modem0Register extends RadioRegister {
         int baudrate = 2400;
         int bitrate = 1200;
 
         static final int DATA_FORMAT_NRZ = 0;
         static final int DATA_FORMAT_MANCHESTER = 1;
         static final int DATA_FORMAT_UART = 2;
         int dataFormat = DATA_FORMAT_MANCHESTER;
 
         int xoscFreqRange = XOSC_FREQ[0];
 
         Modem0Register() {
             super("MODEM0", (byte)0x24);
             decode(value);
             // default value 0b 0010 0100
         }
 
         protected void decode(byte val) {
 
             int baudIndex = (val & 0x70) >> 4;
             int xoscIndex = (val & 0x3);
             dataFormat = (val & 0x0c) >> 2;
             xoscFreqRange = XOSC_FREQ[xoscIndex];
             calculateBaudRate(baudIndex, xoscIndex);
             boolean manchester = dataFormat == DATA_FORMAT_MANCHESTER;
             bitrate = baudrate / (manchester ? 2 : 1);
             probes.fireAtBitRateChange(CC1000Radio.this, bitrate);
         }
 
         private void calculateBaudRate(int baudIndex, int xoscIndex) {
             if ( baudIndex == 5 && xoscFrequency > XOSC_FREQ[2]) {
                 if ( xoscIndex == 0 ) baudrate = 76800;
                 else if ( xoscIndex == 1 ) baudrate = 38400;
                 else baudrate = BAUDRATE[baudIndex];
             } else {
                 baudrate = BAUDRATE[baudIndex];
             }
         }
 
         protected void printStatus() {
             radioPrinter.println("CC1000[MODEM0]: "+baudrate+" baud, "+bitrate+" bit rate, manchester: "+
                     (dataFormat == DATA_FORMAT_MANCHESTER));
         }
     }
 
     protected class MatchRegister extends RadioRegister {
         int rxMatch;
         int txMatch;
 
         MatchRegister() {
             super("MATCH", (byte)0);
         }
 
         protected void decode(byte val) {
             rxMatch = (val & 0xf0) >> 4;
             txMatch = (val & 0x0f);
         }
 
     }
 
     protected class FSCTRLRegister extends RadioRegister {
 
         boolean fsResetN;
 
         FSCTRLRegister() {
             super("FSCTRL", (byte)0x01);
             // default value 0b 0000 0001
         }
 
         protected void decode(byte val) {
             fsResetN = Arithmetic.getBit(val, 0);
         }
     }
 
     // TODO: are there integer round off problems with these values?
     static final double[] PRE_SWING = {1.0, 2 / 3, 7 / 3, 5 / 3};
     static final double[] PRE_CURRENT = {1.0, 2 / 3, 1 / 2, 2 / 5};
 
     protected class PrescalerRegister extends RadioRegister {
 
         double preSwing = 1.0;
 
         double preCurrent = 1.0;
 
         boolean ifInput;
         boolean ifFront;
 
         PrescalerRegister() {
             super("PRESCALER", (byte)0);
         }
 
         protected void decode(byte val) {
             preSwing = PRE_SWING[(val & 0xc0) >> 6];
             preCurrent = PRE_CURRENT[(val & 0x30) >> 4];
             ifInput = Arithmetic.getBit(val, 3);
             ifFront = Arithmetic.getBit(val, 4);
         }
 
     }
 
     /**
      * A CC1000 Controller class for the ATMega microcontroller family. Installing an ATMega128 into this
      * class connects the microcontroller to this radio. Data is communicated over the SPI interface, on which
      * the CC1000 is the master. RSSI data from the CC1000 is available to the ATMega128 though the ADC
      * (analog to digital converter).
      */
     public class ATMegaController implements Radio.RadioController,
             ADC.ADCInput,
             SPIDevice {
 
         SerialConfigurationInterface pinReader;
 
         private SPIDevice spiDevice;
         private final TransferTicker ticker;
         private final SimUtil.SimPrinter printer;
 
         ATMegaController() {
             ticker = new TransferTicker();
             printer = SimUtil.getPrinter(sim, "radio.cc1000.data");
         }
 
         public void enable() {
             ticker.activateTicker();
         }
 
         public void disable() {
             if (MAIN_reg.rxPd && MAIN_reg.txPd) ticker.deactivateTicker();
         }
 
         /**
          * The <code>TransferTicker</code> class is responsible for timing/facilitating transfer between the
          * radio and the connected microcontroller. The receiveFrame(), transmitFrame() methods from the
          * SPIDevice interface are used.
          */
         private class TransferTicker implements Simulator.Event {
             private boolean tickerOn;
 
 
             public void activateTicker() {
                 if (!tickerOn) {
                     tickerOn = true;
                     //OL:
                     //used to switch radio to transmit or receive mode
                     //this takes 250us, e.g. 1843.2 cycles
                     //however, a delay is not really needed for TinyOS
                     //as TinyOS itself waits 250us via TOSH_uwait(250) before it
                     //sends or reads data
                     //Based on this, probably Radio.TRANSFER_TIME fits best
                     clock.insertEvent(ticker, Radio.TRANSFER_TIME);
                 }
             }
 
             public void deactivateTicker() {
                 tickerOn = false;
                 clock.removeEvent(this);
             }
 
             public void fire() {
 
                 SPI.Frame frame = spiDevice.transmitFrame();
 
                 if (MAIN_reg.rxtx && !MAIN_reg.txPd) {
                     receiveFrame(frame);
                 }
 
                 spiDevice.receiveFrame(transmitFrame());
 
                 if (tickerOn) {
                     clock.insertEvent(this, Radio.TRANSFER_TIME);
                 }
             }
         }
 
         /**
          * <code>receiveFrame</code> receives an <code>SPIFrame</code> from a connected device. If the radio
          * is in a transmission state, this should be the next frame sent into the air.
          */
         public void receiveFrame(SPI.Frame frame) {
 
             // data, frequency, origination
             if (!MAIN_reg.txPd && MAIN_reg.rxtx) {
                 long currentTime = clock.getCount();
                 new Transmit(new Transmission(frame.data, 0, currentTime));
             } else {
                 if (printer.enabled) {
                     printer.println("CC1000: discarding "+StringUtil.toMultirepString(frame.data, 8)+" from SPI");
                 }
             }
 
         }
 
         /**
          * <code>Transmit</code> is an event that transmits a packet of data after a one bit period delay.
          */
         protected class Transmit implements Simulator.Event {
             final Radio.Transmission packet;
 
             Transmit(Radio.Transmission packet) {
                 this.packet = packet;
                 clock.insertEvent(this, Radio.TRANSFER_TIME / 8);
             }
 
             public void fire() {
                 if (printer.enabled) {
                     printer.println("CC1000: transmitting "+StringUtil.toMultirepString(packet.data, 8));
                 }
                 // send packet into air...
                 if ( air != null )
                     air.transmit(CC1000Radio.this, packet);
                 probes.fireAtTransmit(CC1000Radio.this, packet);
             }
         }
 
         /**
          * Transmits an <code>SPIFrame</code> to be received by the connected device. This frame is either the
          * last byte of data received or a zero byte.
          */
         public SPI.Frame transmitFrame() {
             SPI.Frame frame;
 
             if (MAIN_reg.rxtx && MAIN_reg.txPd) {
                 frame = SPI.ZERO_FRAME;
             } else {
                 byte data = air != null ? air.readChannel(CC1000Radio.this) : 0;
                 frame = SPI.newFrame(data);
                 if (printer.enabled) {
                     printer.println("CC1000: received " + StringUtil.toMultirepString(frame.data, 8));
                 }
             }
 
             return frame;
         }
 
 
         public void connect(SPIDevice d) {
             spiDevice = d;
         }
 
         public int getLevel() {
             // ask the air for the current RSSI value
             if ( air != null )
                 return air.sampleRSSI(CC1000Radio.this);
             else return ADC.VBG_LEVEL; // return a default value of some sort
         }
 
         //////////////////////////
 
         public void install(Microcontroller mcu) {
             pinReader = new SerialConfigurationInterface(mcu);
 
             if (mcu instanceof ATMegaFamily) {
                 ATMegaFamily atm = (ATMegaFamily)mcu;
 
                 // get ADC device and connect
                 ADC adc = (ADC)atm.getDevice("adc");
                 adc.connectADCInput(this, 0);
 
                 // get SPI device and connect
                 SPI spi = (SPI)atm.getDevice("spi");
                 spi.connect(this);
                 connect(spi); // don't forget to connect ourselves to this device
             } else {
                 throw Util.failure("CC1000: only ATMegaFamily is supported");
             }
         }
 
 
     }
 
 
     /**
      * TODO: determine if I will really need this for anything more than debugging. This class is more or less
      * a state machine on the status of the receiver for this radio. It is "activated" when the receiver unit
      * is powered up and it is "receiving" when the radio is in receive mode.
      */
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
 
         public boolean isListening() {
             boolean listening = activated && receiving;
             if (radioPrinter.enabled) {
                 radioPrinter.println("CC1000: Listening? "+listening);
             }
             return listening;
         }
     }
 
     public boolean isListening() {
         return receiver.isListening();
     }
 
     /**
      * This class is a state machine similar to <code>Receiver</code>, but for the transmitter on the radio.
      * So it is "activated" when the transmitter is powered up and "transmitting" when the radio is in
      * transmit mode. It is very likely that moving this functionality into the <code>MainRegister</code>
      * class would be a good design decision.
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
      * Reads the three pins used in the three wire serial configuration interface. Microcontrollers can
      * program this radio by communication over this interfance. Debug output for communication over this
      * interface is available on "sim.cc1000.pinconfig"
      */
     protected class SerialConfigurationInterface {
 
         byte address;
         boolean write;
         byte data;
         boolean value;
 
         byte readData;
         boolean readValue;
 
         int bitsRead;
 
         SimUtil.SimPrinter readerPrinter;
 
         Microcontroller.Pin.Input paleInput;
 
         SerialConfigurationInterface(Microcontroller mcu) {
 
             readerPrinter = SimUtil.getPrinter(sim, "radio.cc1000.pinconfig");
 
             //install outputs
             mcu.getPin(31).connect(new PCLKOutput());
             mcu.getPin(32).connect(new PDATAOutput());
             mcu.getPin(32).connect(new PDATAInput());
             mcu.getPin(29).connect(new PALEOutput());
 
         }
 
         /**
          * Clocking the PCLK pin is what drives the action of the configuration interface. One bit of data on
          * PDATA per clock.
          */
         protected class PCLKOutput extends Microcontroller.OutputPin {
            protected boolean last;
             public void write(boolean level) {
                if ( level == last ) return;
                 if (!level) { // clr
                     action();
                 } else { // set. false
 
                 }
                last = level;
             }
         }
 
         protected class PDATAInput extends Microcontroller.InputPin {
             public boolean read() {
                 return readValue;
             }
         }
 
         protected class PDATAOutput extends Microcontroller.OutputPin {
 
             public void write(boolean level) {
                 value = level;
             }
         }
 
         // PALE is the address latch. It really isn't necessary, if you assume
         // that software will respect the packet formatting.
         // If you don't, then PALE should be implemented.
         protected class PALEOutput extends Microcontroller.OutputPin {
            protected boolean last;
             public void write(boolean level) {
                if ( level == last ) return;
                 if (!level) {
                     bitsRead = 0;
                 } else {
                     bitsRead = 8;
                 }
                last = level;
             }
         }
 
         public void action() {
             if (bitsRead < 7) {
                 address <<= 1;
                 address |= value ? 0x1 : 0x0;
             } else if (bitsRead == 7) {
                 write = value;
                 if (!write) {
                     readData = registers[0x7f & address].value;
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
                 byte printData = write ? data : registers[(0x7f & address)].value;
                 if (readerPrinter.enabled)
                     readerPrinter.println("Address " + Integer.toHexString(0x7f & address) + rw + " data " + Integer.toBinaryString(0xff & printData));
                 bitsRead = 0;
 
                 if (write) {
                     registers[0x7f & address].write(data);
                 }
             }
         }
 
     }
 
 
     /**
      * Get the <code>Simulator</code> on which this radio is running.
      */
     public Simulator getSimulator() {
         return sim;
     }
 
     /**
      * get the transmission power
      *
      * @see Radio#getPower()
      */
     public int getPower() {
         return PA_POW_reg.getPower();
     }
 
     /**
      * get transmission frequency
      *
      * @see Radio#getFrequency()
      */
     public double getFrequency() {
         // according to CC1000 handbook
         // fRef = fXosc / REFDIV
         // frequency = fRef * ( ( FREQ + 8192 ) / 16384 )
         double ret = 14745600.0 / PLL_reg.refDiv;
         int freq;
         if (MAIN_reg.F_REG == 0)
         //register A
             freq = FREQ_A_reg.frequency;
         else
             freq = FREQ_B_reg.frequency;
         ret *= ((freq + 8192) / 16384);
         return ret;
     }
 
     public RadioAir getAir() {
         return air;
     }
 
     public void setAir(RadioAir nair) {
         air = nair;
     }
 
 }
