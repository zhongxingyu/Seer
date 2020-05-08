 package ase.insideunit;
 
 import java.util.concurrent.LinkedBlockingQueue;
 import javax.swing.JLabel;
 
 import ase.interfaces.InterruptibleModule;
 import ase.modules.Led;
 import ase.modules.Timer;
 import ase.utils.ADCPin;
 import ase.utils.Logger;
 import ase.utils.Pin;
 
 public class CPU extends Thread implements InterruptibleModule {
 	
 	/* IO */
 	public static final int IRPIN = 0;
 	private Pin irPin;
 	
 	public static final int PIRPIN = 3;
 	private Pin pirPin;
 	
 	private static final int IRFRAMEDELAY = 700;
 	private static final int IRTIMERPIN = 1;
 	private Timer irTimer = new Timer(this, IRTIMERPIN);
 	
 	public static final int RXDPIN = 4;
 	private Pin RXD;
 	public static final int TXDPIN = 5;
 	private Pin TXD;
 	
 	private UART uart;// = new UART();
 	
 	private Led ha, hb, hc, hd, he, hf, hg;
 	private Led la, lb, lc, ld, le, lf, lg;
 	private Led lint, lmov;
 	
 	private Inside inside;
 
 	/* Intelligent Mode */
 	/* Timer / Interrupt / Counter */
 	private static final int CLOCKTIMERDELAY = 1000;
 	private static final int CLOCKTIMERPIN = 6;
 	private Timer clockTimer = new Timer(this, CLOCKTIMERPIN);
 
 		/* Default value is 2pm15 / 14h15 */
 		private int secondsCount = 51300;
 		
 		/* Temperature Pattern Array. This keeps the average
 		 * temperature for a determined hour 00h-23h */
 		/* This temperature will be used by the intelligent mode
 		 * to automatically set depending on the hour
 		 */
 		private int[] temperaturePattern;
 		
 		/* Temperature Update count. This is used to calculate
 		 * the average temperature for each hour.
 		 * Example: @14h User sets 3 times to 20, 1 time to 16,
 		 *               average is 19.   
 		 */
 		private int[] temperatureUpdateCount;
 
 	/* AD channels*/
 	public static final int TEMPSENSORPIN = 2;
 	private ADCPin tempSensorPin;
 	
 	/* Interrupts */
 	private LinkedBlockingQueue<Integer> interrupts = new LinkedBlockingQueue<Integer>();
 	private InterruptHandler intHandler = new InterruptHandler(this);
 	
 	/* Flags */
 	private volatile boolean frameReady = false;
 	private volatile boolean ignoreIrTimerInterrupt = false;
 	private volatile boolean tempChanged = false;
 	private volatile boolean movementDetected = false;
 	private volatile boolean stopMovementDetected = false;
 	private volatile boolean intelligentMode  = true;
 	private volatile boolean timingShutdown = false;
 	private boolean power = false;
 	private boolean standby = false;
 	
 	private static final int TIMINGSHUTDOWNDELAY = 900; // 15 MINS = 900 secs
 	private int timingStartTime = -1;
 	
 	/* DEBUG */
 	private JLabel lblRemoteTemp;
 	private JLabel lblRemoteFrame;
 	private JLabel lblOutsideFrame;
 	
 	/* DATA */
 	private int irTemp = 20;
 	private int envTemp = 20;
 	
 	private int irAddress;
 	private boolean[] irFrame = new boolean[11];
 	private boolean[] frameReceived = new boolean[11];
 	private volatile int irBitCounter = 0;
 	
 	
 	public CPU(Inside i, JLabel frame, JLabel temp, JLabel out, int add)
 	{
 		this.lblRemoteTemp = temp;
 		this.lblRemoteFrame = frame;
 		this.lblOutsideFrame = out;
 		this.irAddress = add;
 		this.inside = i;
 		
 		clockTimer.initiate(CLOCKTIMERDELAY);
 		
 		initializeTemperaturePattern();
 	}
 	
 	/*
 	 * -1 means there isn't a pattern yet
 	 */
 	private void initializeTemperaturePattern()
 	{
 		temperaturePattern = new int[]{ 
 		//	00h	01h	02h	03h	04h	05h	06h	07h	08h	09h	10h	11h
 			18,	19,	19,	-1,	-1,	-1,	-1,	21,	20,	19,	18,	18,
 		//	12h 13h	14h	15h	16h	17h	18h	19h	20h	21h	22h	23h
 			18,	18,	18,	17,	17,	18,	19,	19,	19,	20,	21,	19
 		};
 		
 		temperatureUpdateCount = new int[]{ 
 		//	00h	01h	02h	03h	04h	05h	06h	07h	08h	09h	10h	11h
 			 0,	 0,	 0,	 0,	 0,	 0,	 0,	 0,	 0,	 0,	 0,	 0,
 		//	12h 13h	14h	15h	16h	17h	18h	19h	20h	21h	22h	23h
 			 0,	 0,	 0,	 0,	 0,	 0,	 0,	 0,	 0,	 0,	 0,	 0
 		};
 	}
 	
 	private int checkFrame(){
 		
 		// Get Address
 		String bits = "";
 		for(int i = 0; i < 5; i++)
 			if(frameReceived[i])
 				bits = bits.concat("1");
 			else
 				bits = bits.concat("0");
 				
 		// Check address
 		if(Integer.parseInt(bits, 2) != irAddress){
 			System.err.println("Frame with different address!");
 			return -1;
 		}
 				
 		bits = "";
 		for(int i = 5; i < 11; i++){
 			if(frameReceived[i])
 				bits = bits.concat("1");
 			else
 				bits = bits.concat("0");
 		}
 				
 		return Integer.parseInt(bits, 2);
 	}
 	
 	public void run(){
 		
		intHandler.start();
		
 		float t1;
 		int t2;
 		String frame;
 
 		while(true){
 			
 			try {
 				sleep(100);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			// Ir frame Received
 			if(frameReady){
 				frameReady = false;
 				
 				frame = "111";
 				for(boolean b : frameReceived)
 					if(b) frame = frame.concat("1");
 					else frame = frame.concat("0");
 				lblRemoteFrame.setText("Frame: "+frame);
 				
 				int command = checkFrame();
 				switch(command){
 					case 12:
 						// Power command
 						if(power)
 						{
 							// Power off
 							turnOffLeds();
 							power = false;
 							standby = false;
 							uart.stop = true;
 							inside.PowerOff();
 							uart = null;
 						}else{
 							// Power on
 							power = true;
 							standby = false;
 							inside.PowerOn();
 							uart = new UART();
 						}
 						break;
 					case 16:
 						// Up command
 						if(power && irTemp < 44){
 							updateTemperature(++irTemp);							
 							
 							updateCompressor();
 						}
 
 						// manual override of temperature,
 						// turn off intelligent mode
 						intelligentMode = false;
 						
 						break;
 					case 17:
 						// Down command
 						if(power && irTemp > 0){
 							updateTemperature(--irTemp);
 		
 							updateCompressor();
 						}
 
 						// manual override of temperature,
 						// turn off intelligent mode
 						intelligentMode = false;
 						
 						break;
 				}
 			}
 			
 			if(!power) continue;
 			
 			// Motion detected
 			if (movementDetected)
 			{	
 				movementDetected = false;
 				lmov.setOn();
 				
 				// movement is detected, active intelligent mode
 				// by default.
 				// TODO check if the user has manual entered a temperature
 				// in the last X minutes
 				
 				if(standby){
 					intelligentMode = true;
 					standby = false;
 				}
 				
 				timingShutdown = false;
 			}
 			
 			if(stopMovementDetected)
 			{
 				stopMovementDetected = false;
 				lmov.setOff();
 				
 				timingStartTime = secondsCount;
 				
 				timingShutdown = true;
 			}
 			
 			if(timingShutdown){
 				
 				int timeElapsed = (timingStartTime > secondsCount ? secondsCount + 86400 : secondsCount) - timingStartTime;
 				
 				if(timeElapsed >= TIMINGSHUTDOWNDELAY){
 					
 					TurnOffCompressor();
 					
 					standby = true;
 					timingShutdown = false;
 				}
 			}
 
 			// Intelligent Mode
 			if (intelligentMode)
 			{
 				lint.setOn();
 				
 				// compare current temperature with average of array
 				// if is different setup the temperature!
 				intelligentModeRoutine();
 			}else
 				lint.setOff();
 
 			// Temperature Changed
 			if(tempChanged)
 			{
 				tempChanged = false;
 				
 				// Convert voltage into temperature value
 				t1 = tempSensorPin.readVoltage() * 100f - 50f;
 				t2 = (int) t1;
 				if(t2 == t1){
 					envTemp = t2;
 					setDisplayNumber(envTemp);
 					if(envTemp == irTemp) TurnOffCompressor();
 					else updateCompressor();
 				}else
 					updateCompressor();
 			}
 		}
 	}
 	
 	private void updateTemperature(int newTemperature)
 	{
 		int currentHour = secondsCount / 3600;
 		//int index       = currentHour - 1;
 		int index       = currentHour;
 		
 		//irTemp = newTemperature;
 		
 		lblRemoteTemp.setText("Temp: "+irTemp);
 		
 		// TODO With the remote this wil be triggered several times
 		// only do this after the user defines a fixed temperature
 		
 		
 		// Update the pattern array
 		// Averages the pattern temperature with the new temperature
 
 		int averageTemperature =
 			(temperatureUpdateCount[index] * temperaturePattern[index]
 			 + newTemperature )
 			/ (temperatureUpdateCount[index] + 1);
 
 
 		temperatureUpdateCount[index]++;
 		temperaturePattern[index] = averageTemperature; 
 	}
 	
 	private void intelligentModeRoutine()
 	{
 		int currentHour = secondsCount / 3600;
 		//int assumedTemperature = temperaturePattern[currentHour - 1];
 		int assumedTemperature = temperaturePattern[currentHour];
 		
 		// check if the temperature is defined
 		// not "-1"
 		if (assumedTemperature >= 0)
 		{
 			irTemp = assumedTemperature;
 			lblRemoteTemp.setText("Temp: "+irTemp);
 		} else {
 			// do nothing, wait for the next hour
 			// with a predefined temperature
 		}
 	}
 	
 	private void TurnOffCompressor()
 	{
 		while(uart.busy);
 		uart.shiftReg = 0;
 		uart.busy = true;
 	}
 	
 	private void updateCompressor()
 	{
 		if(standby) return;
 		
 		int speed = 45 - irTemp;
 		while(uart.busy);
 		uart.shiftReg = speed;
 		uart.busy = true;
 	}
 	
 	public void configurePins(Pin ir, ADCPin temp, Pin pir, Pin txd, Pin rxd){
 		this.irPin = ir;
 		this.tempSensorPin = temp;
 		this.pirPin = pir;
 		this.TXD = txd;
 		this.RXD = rxd;
 	}
 	
 	public void configureLeds(Led ha, Led hb, Led hc, Led hd, Led he, Led hf, Led hg,
 			Led la, Led lb, Led lc, Led ld, Led le, Led lf, Led lg, Led lint, Led lmov){
 		this.ha = ha;
 		this.hb = hb;
 		this.hc = hc;
 		this.hd = hd;
 		this.he = he;
 		this.hf = hf;
 		this.hg = hg;
 		this.la = la;
 		this.lb = lb;
 		this.lc = lc;
 		this.ld = ld;
 		this.le = le;
 		this.lf = lf;
 		this.lg = lg;
 		this.lint = lint;
 		this.lmov = lmov;
 	}
 	
 	public int readSeconds()
 	{
 		return secondsCount;
 	}
 
 	public void addSeconds(int seconds)
 	{
 		// 86 400
 		seconds += secondsCount;
 		if(seconds > 86399)
 			secondsCount = seconds - 86400;
 		else
 			secondsCount = seconds;
 	}
 	
 	@Override
 	public void interruptModule(int id) {
 		this.interrupts.offer(id);
 	}
 	
 	public void interrupt(){
 		intHandler.interrupt();
 		super.interrupt();
 	}
 	
 	private class UART extends Thread{
 
 		public volatile int shiftReg;
 		public volatile boolean busy = false;
 		public volatile boolean stop = false;
 		
 		private final static int BITDELAY = 50;
 		
 		public UART(){
 			setDaemon(true);
 			start();
 		}
 		
 		private void delay(){
 			try {
 				sleep(BITDELAY);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		public void run(){
 			
 			TXD.sendSignal(Pin.HIGH, true);
 			
 			String conc, bitRep;
 			
 			while(true){
 				
 				while(!busy && !stop) delay();
 				
 				if(stop) break;
 				
 				if(shiftReg > 255){
 					busy = false;
 					continue;
 				}
 
 				bitRep = Integer.toString(shiftReg, 2);
 				conc = "";
 				for(int i = bitRep.length(); i < 8; i++)
 					conc = conc.concat("0");
 				
 				bitRep = conc.concat(bitRep);
 				
 				lblOutsideFrame.setText("Sent: "+bitRep);
 				
 				// Start bit
 				TXD.sendSignal(Pin.LOW, true);
 				Logger.instance().debug("InsideUnitOUT", "UART Write", "0");
 				delay();
 				for(int i = 0; i < 8; i++){
 					if(bitRep.charAt(i) == '0'){
 						TXD.sendSignal(Pin.LOW, true);
 						Logger.instance().debug("InsideUnitOUT", "UART Write", "0");
 					} else{
 						TXD.sendSignal(Pin.HIGH, true);
 						Logger.instance().debug("InsideUnitOUT", "UART Write", "1");
 					}
 					delay();
 				}
 				// Stop bit
 				TXD.sendSignal(Pin.HIGH, true);
 				Logger.instance().debug("InsideUnitOUT", "UART Write", "1");
 				
 				busy = false;
 			}
 			
 		}
 	}
 	
 	private class InterruptHandler extends Thread
 	{
 		
 		private CPU cpu;
 		
 		public InterruptHandler(CPU cpu){
 			this.cpu = cpu;
 			setDaemon(true);
 		}
 		
 		public void run(){
 			
 			int pin;
 			
 			while(true){
 				
 				try {
 					pin = interrupts.take();
 				} catch (InterruptedException e) {
 					return;
 				}
 				
 				this.cpu.suspend();
 				switch(pin)
 				{
 				
 					case IRPIN:
 					
 						if(irBitCounter == 0)
 						{
 							irFrame[irBitCounter++] = irPin.readSignal();
 							irTimer.initiate(IRFRAMEDELAY);
 						} else {
 							irFrame[irBitCounter++] = irPin.readSignal();
 							if(irBitCounter >= 11){
 								irBitCounter = 0;
 								frameReceived = irFrame.clone();
 								frameReady = true;
 								ignoreIrTimerInterrupt = true;
 							}
 						}
 						break;
 						
 					case IRTIMERPIN:
 						if(!ignoreIrTimerInterrupt)
 							irBitCounter = 0;
 						else
 							ignoreIrTimerInterrupt = false;
 						break;
 						
 					case TEMPSENSORPIN:
 						tempChanged = true;
 						break;
 						
 					case CLOCKTIMERPIN:
 						clockIntRoutine();
 						clockTimer.initiate(CLOCKTIMERDELAY);
 						break;
 						
 					case PIRPIN:
 						// TODO handle
 						if(pirPin.readSignal()) {
 							movementDetected = true;
 						} else {
 							stopMovementDetected = true;
 						}
 						break;
 						
 				}
 				this.cpu.resume();
 			}
 			
 		}
 		
 		protected void clockIntRoutine()
 		{
 			//System.out.println("clock pin");
 			secondsCount++;
 		}
 	}
 	
 	/* Led methods */
 	private void setDisplayNumber(int num){
 		
 		if(num > 99 || num < -9) return;
 		
 		if(num < 0) setNeg(num);
 		else{
 		
 			int high, low;
 		
 			high = num / 10;
 			low = num % 10;
 		
 			setLeds(true, high);
 			setLeds(false, low);
 		}
 	}
 	
 	private void setNeg(int num){
 		
 		this.ha.setOff();
 		this.hb.setOff();
 		this.hc.setOff();
 		this.hd.setOff();
 		this.he.setOff();
 		this.hf.setOff();
 		this.hg.setOn();
 		
 		setLeds(false, num*-1);
 	}
 	
 	private void setLeds(boolean high, int num){
 		
 		switch(num){
 			case 0:
 				set0(high);
 				break;
 			case 1:
 				set1(high);
 				break;
 			case 2:
 				set2(high);
 				break;
 			case 3:
 				set3(high);
 				break;
 			case 4:
 				set4(high);
 				break;
 			case 5:
 				set5(high);
 				break;
 			case 6:
 				set6(high);
 				break;
 			case 7:
 				set7(high);
 				break;
 			case 8:
 				set8(high);
 				break;
 			case 9:
 				set9(high);
 				break;
 		}
 	}
 	
 	private void set0(boolean high){
 		if(high){
 			this.ha.setOn();
 			this.hb.setOn();
 			this.hc.setOn();
 			this.hd.setOn();
 			this.he.setOn();
 			this.hf.setOn();
 			this.hg.setOff();
 		}else{
 			this.la.setOn();
 			this.lb.setOn();
 			this.lc.setOn();
 			this.ld.setOn();
 			this.le.setOn();
 			this.lf.setOn();
 			this.lg.setOff();
 		}
 	}
 	
 	private void set1(boolean high){
 		if(high){
 			this.ha.setOff();
 			this.hb.setOn();
 			this.hc.setOn();
 			this.hd.setOff();
 			this.he.setOff();
 			this.hf.setOff();
 			this.hg.setOff();
 		}else{
 			this.la.setOff();
 			this.lb.setOn();
 			this.lc.setOn();
 			this.ld.setOff();
 			this.le.setOff();
 			this.lf.setOff();
 			this.lg.setOff();
 		}
 	}
 	
 	private void set2(boolean high){
 		if(high){
 			this.ha.setOn();
 			this.hb.setOn();
 			this.hc.setOff();
 			this.hd.setOn();
 			this.he.setOn();
 			this.hf.setOff();
 			this.hg.setOn();
 		}else{
 			this.la.setOn();
 			this.lb.setOn();
 			this.lc.setOff();
 			this.ld.setOn();
 			this.le.setOn();
 			this.lf.setOff();
 			this.lg.setOn();
 		}
 	}
 	
 	private void set3(boolean high){
 		if(high){
 			this.ha.setOn();
 			this.hb.setOn();
 			this.hc.setOn();
 			this.hd.setOn();
 			this.he.setOff();
 			this.hf.setOff();
 			this.hg.setOn();
 		}else{
 			this.la.setOn();
 			this.lb.setOn();
 			this.lc.setOn();
 			this.ld.setOn();
 			this.le.setOff();
 			this.lf.setOff();
 			this.lg.setOn();
 		}
 	}
 	
 	private void set4(boolean high){
 		if(high){
 			this.ha.setOff();
 			this.hb.setOn();
 			this.hc.setOn();
 			this.hd.setOff();
 			this.he.setOff();
 			this.hf.setOn();
 			this.hg.setOn();
 		}else{
 			this.la.setOff();
 			this.lb.setOn();
 			this.lc.setOn();
 			this.ld.setOff();
 			this.le.setOff();
 			this.lf.setOn();
 			this.lg.setOn();
 		}
 	}
 	
 	private void set5(boolean high){
 		if(high){
 			this.ha.setOn();
 			this.hb.setOff();
 			this.hc.setOn();
 			this.hd.setOn();
 			this.he.setOff();
 			this.hf.setOn();
 			this.hg.setOn();
 		}else{
 			this.la.setOn();
 			this.lb.setOff();
 			this.lc.setOn();
 			this.ld.setOn();
 			this.le.setOff();
 			this.lf.setOn();
 			this.lg.setOn();
 		}
 	}
 	
 	private void set6(boolean high){
 		if(high){
 			this.ha.setOn();
 			this.hb.setOff();
 			this.hc.setOn();
 			this.hd.setOn();
 			this.he.setOn();
 			this.hf.setOn();
 			this.hg.setOn();
 		}else{
 			this.la.setOn();
 			this.lb.setOff();
 			this.lc.setOn();
 			this.ld.setOn();
 			this.le.setOn();
 			this.lf.setOn();
 			this.lg.setOn();
 		}
 	}
 	
 	private void set7(boolean high){
 		if(high){
 			this.ha.setOn();
 			this.hb.setOn();
 			this.hc.setOn();
 			this.hd.setOff();
 			this.he.setOff();
 			this.hf.setOff();
 			this.hg.setOff();
 		}else{
 			this.la.setOn();
 			this.lb.setOn();
 			this.lc.setOn();
 			this.ld.setOff();
 			this.le.setOff();
 			this.lf.setOff();
 			this.lg.setOff();
 		}
 	}
 	
 	private void set8(boolean high){
 		if(high){
 			this.ha.setOn();
 			this.hb.setOn();
 			this.hc.setOn();
 			this.hd.setOn();
 			this.he.setOn();
 			this.hf.setOn();
 			this.hg.setOn();
 		}else{
 			this.la.setOn();
 			this.lb.setOn();
 			this.lc.setOn();
 			this.ld.setOn();
 			this.le.setOn();
 			this.lf.setOn();
 			this.lg.setOn();
 		}
 	}
 	
 	private void set9(boolean high){
 		if(high){
 			this.ha.setOn();
 			this.hb.setOn();
 			this.hc.setOn();
 			this.hd.setOn();
 			this.he.setOff();
 			this.hf.setOn();
 			this.hg.setOn();
 		}else{
 			this.la.setOn();
 			this.lb.setOn();
 			this.lc.setOn();
 			this.ld.setOn();
 			this.le.setOff();
 			this.lf.setOn();
 			this.lg.setOn();
 		}
 	}
 	
 	public void turnOffLeds(){
 		this.ha.setOff();
 		this.hb.setOff();
 		this.hc.setOff();
 		this.hd.setOff();
 		this.he.setOff();
 		this.hf.setOff();
 		this.hg.setOff();
 		this.la.setOff();
 		this.lb.setOff();
 		this.lc.setOff();
 		this.ld.setOff();
 		this.le.setOff();
 		this.lf.setOff();
 		this.lg.setOff();
 		this.lint.setOff();
 	}
 }
