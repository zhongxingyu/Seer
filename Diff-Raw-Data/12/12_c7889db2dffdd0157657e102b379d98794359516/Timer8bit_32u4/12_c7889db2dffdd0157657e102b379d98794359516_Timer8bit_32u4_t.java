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
 
 import cck.util.Util;
 import avrora.sim.RWRegister;
 import avrora.sim.Simulator;
 import avrora.sim.state.RegisterView;
 import avrora.sim.state.RegisterUtil;
 import avrora.sim.state.RegisterView.RegisterValueSetListener;
 import avrora.sim.clock.Clock;
 
 /**
  * Base class of 8-bit timers. Timer0 and Timer2 are subclasses of this.
  * 
  * @author Daniel Lee
  */
 public abstract class Timer8bit_32u4 extends AtmelInternalDevice {
 	public static final int MODE_NORMAL = 0;
 	public static final int MODE_PWM = 1;
 	public static final int MODE_CTC = 2;
 	public static final int MODE_FASTPWM = 3;
 	public static final int MAX = 0xff;
 	public static final int BOTTOM = 0x00;
 
 	final ControlRegisterState controlRegState;
 
 	final ControlRegisterA TCCRnA_reg;
 	final ControlRegisterB TCCRnB_reg;
 
 	final TCNTnRegister TCNTn_reg;
 
 	final BufferedRegister OCRnA_reg;
 	final BufferedRegister OCRnB_reg;
 
 	protected final int n; // number of timer. 0 for Timer0, 2 for Timer2
 
 	protected Simulator.Event ticker;
	//protected final Clock externalClock;
 	protected Clock timerClock;
 
 	protected int period;
 
 	final AtmelMicrocontroller.Pin outputComparePinA;
 	final AtmelMicrocontroller.Pin outputComparePinB;
 	final Simulator.Event[] tickers;
 
 	/*
 	 * pg. 93 of manual. Block compareMatch for one period after TCNTn is
 	 * written to.
 	 */
 	boolean blockCompareMatch;
 
 	final int OCIEnA;
 	final int OCIEnB;
 	final int TOIEn;
 	final int OCFnA;
 	final int OCFnB;
 	final int TOVn;
 
 	protected ATMegaFamilyNew.FlagRegister TIFRn_reg;
 	protected ATMegaFamilyNew.MaskRegister TIMSKn_reg;
 
 	final int[] periods;
 
 	protected Timer8bit_32u4(AtmelMicrocontroller m, int n, int OCIEnA,
 			int OCIEnB, int TOIEn, int OCFnA, int OCFnB, int TOVn, int[] periods) {
 		super("timer" + n, m);
 
 		TCCRnA_reg = new ControlRegisterA();
 		TCCRnB_reg = new ControlRegisterB();
 		TCNTn_reg = new TCNTnRegister();
 		OCRnA_reg = new BufferedRegister();
 		OCRnB_reg = new BufferedRegister();
 
 		TIFRn_reg = (ATMegaFamilyNew.FlagRegister) m.getIOReg("TIFR" + n);
 		TIMSKn_reg = (ATMegaFamilyNew.MaskRegister) m.getIOReg("TIMSK" + n);
 
		//externalClock = m.getClock("external");
 		timerClock = mainClock;
 
 		outputComparePinA = (AtmelMicrocontroller.Pin) microcontroller
 				.getPin("OC" + n + "A");
 		outputComparePinB = (AtmelMicrocontroller.Pin) microcontroller
 				.getPin("OC" + n + "B");
 
 		this.OCIEnA = OCIEnA;
 		this.OCIEnB = OCIEnB;
 		this.TOIEn = TOIEn;
 		this.OCFnA = OCFnA;
 		this.OCFnB = OCFnB;
 		this.TOVn = TOVn;
 		this.n = n;
 		this.periods = periods;
 
 		controlRegState = new ControlRegisterState();
 
 		TCCRnA_reg.registerValueSetListener(controlRegState);
 		TCCRnB_reg.registerValueSetListener(controlRegState);
 
 		installIOReg("TCCR" + n + "A", TCCRnA_reg);
 		installIOReg("TCCR" + n + "B", TCCRnB_reg);
 		installIOReg("TCNT" + n, TCNTn_reg);
 		installIOReg("OCR" + n + "A", OCRnA_reg);
 		installIOReg("OCR" + n + "B", OCRnB_reg);
 
 		tickers = new Simulator.Event[4];
 		installTickers();
 	}
 
 	private void installTickers() {
 		tickers[MODE_NORMAL] = new Mode_Normal();
 		tickers[MODE_CTC] = new Mode_CTC();
 		tickers[MODE_FASTPWM] = new Mode_FastPWM();
 		tickers[MODE_PWM] = new Mode_PWM();
 	}
 
 	protected void compareMatch(boolean is_A) {
 		int OCIE = is_A ? OCIEnA : OCIEnB;
 		int OCF = is_A ? OCFnA : OCFnB;
 		if (devicePrinter != null) {
 			boolean enabled = TIMSKn_reg.readBit(OCIE);
 			devicePrinter.println("Timer" + n + ".compareMatch (enabled: "
 					+ enabled + ')');
 		}
 		// set the compare flag for this timer
 		TIFRn_reg.flagBit(OCF);
 		// if the mode is correct, modify pin OCn. but if the flag is
 		// already connected to the pin, does this happen automatically
 		// with the last previous call?
 		// compareMatchPin();
 	}
 
 	protected void overflow() {
 		if (devicePrinter != null) {
 			boolean enabled = TIMSKn_reg.readBit(TOIEn);
 			devicePrinter.println("Timer" + n + ".overFlow (enabled: "
 					+ enabled + ')');
 		}
 		// set the overflow flag for this timer
 		TIFRn_reg.flagBit(TOVn);
 	}
 
 	/**
 	 * Overloads the write behavior of this class of register in order to
 	 * implement compare match blocking for one timer period.
 	 */
 	protected class TCNTnRegister extends RWRegister {
 
 		public void write(byte val) {
 			value = val;
 			blockCompareMatch = true;
 		}
 
 	}
 
 	/**
 	 * <code>BufferedRegister</code> implements a register with a write buffer.
 	 * In PWN modes, writes to this register are not performed until flush() is
 	 * called. In non-PWM modes, the writes are immediate.
 	 */
 	protected class BufferedRegister extends RWRegister {
 		final RWRegister register;
 
 		protected BufferedRegister() {
 			this.register = new RWRegister();
 		}
 
 		public void write(byte val) {
 			super.write(val);
 			if (controlRegState.mode == MODE_NORMAL
 					|| controlRegState.mode == MODE_CTC) {
 				flush();
 			}
 		}
 
 		public byte readBuffer() {
 			return super.read();
 		}
 
 		public byte read() {
 			return register.read();
 		}
 
 		protected void flush() {
 			register.write(value);
 		}
 	}
 
 	static enum ETopSource {
 		FF, OCRA
 	};
 
 	protected class ControlRegisterState implements RegisterValueSetListener {
 
 		public static final int FOCn = 7;
 		public static final int WGMn0 = 6;
 		public static final int COMn1 = 5;
 		public static final int COMn0 = 4;
 		public static final int WGMn1 = 3;
 		public static final int CSn2 = 2;
 		public static final int CSn1 = 1;
 		public static final int CSn0 = 0;
 
 		final RegisterView TCCR = RegisterUtil.stackedView(TCCRnA_reg,
 				TCCRnB_reg);
 
 		final RegisterView CSn = RegisterUtil.bitRangeView(TCCRnB_reg, 0, 2);
 		final RegisterView COMnA = RegisterUtil.bitRangeView(TCCRnA_reg, 6, 7);
 		final RegisterView COMnB = RegisterUtil.bitRangeView(TCCRnA_reg, 4, 5);
 		final RegisterView WGMn = RegisterUtil.permutedView(TCCR, new byte[] {
 				0, 1, 3 + 8 });
 
 		int mode = -1;
 		int scale = -1;
 		ETopSource top_source = ETopSource.FF;
 
 		public void onValueSet(RegisterView view, int oldValue, int newValue) {
 			if (TCCRnA_reg == view)
 				write_A(oldValue, newValue);
 			else if (TCCRnB_reg == view)
 				write_B(oldValue, newValue);
 			else
 				throw new InternalError("wrong register");
 
 			// decode modes and update internal state
 			int nmode = WGMn.getValue();
 			int nscale = CSn.getValue();
 			// table 13-8, page 104
 			ETopSource ntop_source = (nmode == 2 || nmode == 5 || nmode == 7) ? ETopSource.OCRA
 					: ETopSource.FF;
 			nmode = nmode & 0x3;
 
 			if (nmode != mode || nscale != scale || ntop_source != top_source) {
 				if (ticker != null)
 					timerClock.removeEvent(ticker);
 				mode = nmode;
 				scale = nscale;
 				ticker = tickers[mode];
 				period = periods[scale];
 
 				if (period != 0) {
 					timerClock.insertEvent(ticker, period);
 				}
 
 				if (devicePrinter != null) {
 					if (period != 0)
 						devicePrinter.println("Timer" + n
 								+ " enabled: period = " + period + " mode = "
 								+ mode);
 					else
 						devicePrinter.println("Timer" + n + " disabled");
 				}
 			}
 		}
 
 		protected void write_A(int oldValue, int newValue) {
 
 		}
 
 		public void write_B(int oldValue, int newValue) {
 			if ((newValue & 0x80) != 0) {
 				forcedOutputCompare(true);
 			}
 
 			if ((newValue & 0x40) != 0) {
 				forcedOutputCompare(false);
 			}
 		}
 
 		private void forcedOutputCompare(boolean is_A) {
 
 			BufferedRegister OCR_reg = is_A ? OCRnA_reg : OCRnB_reg;
 			RegisterView COM = is_A ? COMnA : COMnB;
 			AtmelMicrocontroller.Pin outputComparePin = is_A ? outputComparePinA
 					: outputComparePinB;
 
 			int count = TCNTn_reg.read() & 0xff;
 			int compare = OCR_reg.read() & 0xff;
 
 			// the non-PWM modes are NORMAL and CTC
 			// under NORMAL, there is no pin action for a compare match
 			// under CTC, the action is to clear the pin.
 
 			// TODO: this implementation is probably not correct...
 			if (count == compare) {
 				switch (COM.getValue()) {
 				case 1:
 					int nmode = WGMn.getValue() & 3;
 					if (nmode == MODE_NORMAL || nmode == MODE_CTC)
 						outputComparePin.write(!outputComparePin.read()); // toggle
 					break;
 				case 2:
 					outputComparePin.write(false); // clear
 					break;
 				case 3:
 					outputComparePin.write(true); // set to true
 					break;
 				}
 
 			}
 		}
 	}
 
 	protected class ControlRegisterA extends RWRegister {
 	}
 
 	protected class ControlRegisterB extends RWRegister {
 
 		public void write(byte val) {
 
 			// hardware manual states that FOC0A/FOC0B bit is always read as
 			// zero
 			byte old = value;
 			value = (byte) (val & 0x3f);
 			// we will notify with the value of FOC0A/FOC0B
 			notify(old, val);
 		}
 	}
 
 	class Mode_Normal implements Simulator.Event {
 		public void fire() {
 			int ncount = (int) TCNTn_reg.read() & 0xff;
 			tickerStart(ncount);
 			if (ncount >= MAX) {
 				overflow();
 				ncount = BOTTOM;
 			} else {
 				ncount++;
 			}
 			tickerFinish(this, ncount);
 		}
 	}
 
 	class Mode_PWM implements Simulator.Event {
 		protected byte increment = 1;
 
 		public void fire() {
 
 			throw new UnsupportedOperationException("not implemented");
 
 			// // TODO: OCn handling
 			// int ncount = (int) TCNTn_reg.read() & 0xff;
 			// tickerStart(ncount);
 			// if (ncount >= MAX) {
 			// increment = -1;
 			// ncount = MAX;
 			// OCRn_reg.flush(); // pg. 102. update OCRn at TOP
 			// } else if (ncount <= BOTTOM) {
 			// overflow();
 			// increment = 1;
 			// ncount = BOTTOM;
 			// }
 			// ncount += increment;
 			// tickerFinish(this, ncount);
 		}
 	}
 
 	class Mode_CTC implements Simulator.Event {
 		public void fire() {
 			int ncount = (int) TCNTn_reg.read() & 0xff;
 			// TODO OCRnA can be changed in interrupt. Here we have strict
 			// order. In real processor it can be different.
 			tickerStart(ncount);
 			if (ncount >= MAX) {
 				// OCRn == MAX, then overflow is handled as in normal mode
 				overflow();
 				ncount = BOTTOM;
 			} else if (ncount == ((int) OCRnA_reg.read() & 0xff)) {
 				ncount = BOTTOM;
 			} else {
 				ncount++;
 			}
 			tickerFinish(this, ncount);
 		}
 	}
 
 	class Mode_FastPWM implements Simulator.Event {
 		public void fire() {
 
 			int top = (controlRegState.top_source == ETopSource.FF) ? 0xFF
 					: ((int) OCRnA_reg.read() & 0xff);
 			// TODO: OCn handling
 			int ncount = (int) TCNTn_reg.read() & 0xff;
 			tickerStart(ncount);
 			if (ncount >= top) {
 				ncount = BOTTOM;
 				overflow();
 				OCRnA_reg.flush(); // pg. 102. update OCRn at TOP
 			} else {
 				ncount++;
 			}
 			tickerFinish(this, ncount);
 		}
 	}
 
 	private void tickerStart(int count) {
 		if (!blockCompareMatch && count == ((int) OCRnA_reg.read() & 0xff)) {
 			compareMatch(true);
 		}
 		if (!blockCompareMatch && count == ((int) OCRnB_reg.read() & 0xff)) {
 			compareMatch(false);
 		}
 	}
 
 	private void tickerFinish(Simulator.Event ticker, int ncount) {
 		TCNTn_reg.write((byte) ncount);
 		// previous write sets the compare, so reset it now
 		blockCompareMatch = false;
 
 		timerClock.insertEvent(ticker, period);
 	}
 }
