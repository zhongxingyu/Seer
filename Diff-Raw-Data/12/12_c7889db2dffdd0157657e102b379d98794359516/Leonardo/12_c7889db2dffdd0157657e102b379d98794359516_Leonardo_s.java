 /**
  * 
  */
 package arduino.platform;
 
 import cck.text.Terminal;
 import avrora.core.Program;
 import avrora.sim.Simulation;
 import avrora.sim.Simulator;
 import avrora.sim.clock.ClockDomain;
 import avrora.sim.mcu.ATMega32;
 import avrora.sim.mcu.ATMega32u4;
 import avrora.sim.mcu.Microcontroller;
 import avrora.sim.platform.LED;
 import avrora.sim.platform.Platform;
 import avrora.sim.platform.PlatformFactory;
 import avrora.sim.platform.LED.LEDGroup;
 
 /**
  * @author pavel
  *
  */
 public class Leonardo extends Platform {
 	
	protected static final int MAIN_HZ = 8000000;
 	protected static final int EXT_HZ = 32768;
 	
 	public static class Factory implements PlatformFactory {
 
 		@Override
 		public Platform newPlatform(int id, Simulation sim, Program p) {
 			ClockDomain cd = new ClockDomain(MAIN_HZ);
 			cd.newClock("external", EXT_HZ);
 			
 			return new Leonardo(new ATMega32u4(id, sim, cd, p));
 //			return new Leonardo(new ATMega32(id, sim, cd, p));
 		}
 		
 	}
 
 	protected final Simulator sim;
 	protected LED.LEDGroup legGroup;
 	
 	protected Leonardo(Microcontroller m) {
 		super(m);
 		sim = m.getSimulator();
 		addDevices();
 	}
 
 	private void addDevices() {
 		LED yellow = new LED(sim, Terminal.COLOR_YELLOW, "Yellow(13)");
 		
 		legGroup = new LEDGroup(sim, new LED[] {yellow});
 		addDevice("leds", legGroup);
 		
 		mcu.getPin("PC7").connectOutput(yellow);
 		
 		
 	}
 
 
 }
