 package gpio;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.nlogo.api.Argument;
 import org.nlogo.api.Context;
 import org.nlogo.api.DefaultClassManager;
 import org.nlogo.api.DefaultCommand;
 import org.nlogo.api.DefaultReporter;
 import org.nlogo.api.ExtensionException;
 import org.nlogo.api.LogoException;
 import org.nlogo.api.LogoListBuilder;
 import org.nlogo.api.PrimitiveManager;
 import org.nlogo.api.Syntax;
 
 public class GpioExtension extends DefaultClassManager {
 
 	static final String LED_PIN = "gpio18";
 	static final String analogPinDir = "/proc/";
 
 	static final String pinDir = "/sys/devices/virtual/misc/gpio/pin/";
 	static final String modeDir= "/sys/devices/virtual/misc/gpio/mode/";
 	
 	static final String pwmDir = "/sys/devices/virtual/misc/pwmtimer/";
 	static final String pwmEnable = pwmDir + "enable/";
 	static final String pwmFreq = pwmDir + "freq/";
 	static final String pwmLevel = pwmDir + "level/";
 	
 	static final String READ = "0";
 	static final String WRITE = "1";
 	
 	static final String[] availablePins = {"gpio0","gpio1","gpio2","gpio3","gpio4","gpio5","gpio6","gpio7",
         "gpio8", "gpio9", "gpio10", "gpio11", "gpio12", "gpio13",
         "gpio14", "gpio15", "gpio16", "gpio17", "gpio18", "gpio19"};
 	
 	static final String[] availablePWMs = {"gpio5", "gpio6", "gpio3", "gpio9", "gpio10", "gpio11"};
 	
 	static final String[] avalableAnalogs = {"adc0", "adc1", "adc2", "adc3", "adc4", "adc5" };
 	static final ArrayList<String> legalAnalogs = new ArrayList<String>();
 	static final ArrayList<String> legalDigitals = new ArrayList<String>();
 	static final ArrayList<String> legalPWMs = new ArrayList<String>();
 	
 	static final HashMap<String, HashMap<String, String>> legalModes = new HashMap<String, HashMap<String, String>>();
 	static final HashMap<String, String> pinStates = new HashMap<String,String>();
 	
 	static {
 		for (String pinName : availablePins ) {
 			HashMap<String, String> modesHere  = new HashMap<String, String>();
 			modesHere.put("read", READ);
 			modesHere.put("write", WRITE);
 			legalModes.put(pinName, modesHere);
 			pinStates.put(pinName, READ);
 		}
 		for (String pwmName : availablePWMs ) {
 			HashMap<String, String> modesHere  = legalModes.get(pwmName);
 			if (modesHere != null ) {
 				if( pwmName.equalsIgnoreCase("gpio5") || pwmName.equalsIgnoreCase("gpio6") ) {
 					modesHere.put("pwm", "2");
 				} else {
 					modesHere.put("pwm", "1");
 				}
 			}
 			legalModes.put(pwmName, modesHere);
 			legalPWMs.add(pwmName);
 		}
 		for (String digital: availablePins) {
 			legalDigitals.add(digital);
 		}
 		for (String analog: avalableAnalogs) {
 			legalAnalogs.add(analog);
 		}
 	}
 	
 	
 	//adc0 and adc1 -- 0 to 2 volts, readings with 6 bits --> 0 to 63 is reading.
 	//adc2 to adc5 -- 0 to 3.3 volts, readings with 12 bits --> 0 to 4095
 	
 	
 	/*
 	 * PWM filesystem map (assuming sysfs is insalled...)
 	 * 
 /sys/devices/virtual/misc/pwmtimer/freq/pwmX (r/w min_freq to max_freq )
 /sys/devices/virtual/misc/pwmtimer/freq_range/pwmX (read only: freq range)
 /sys/devices/virtual/misc/pwmtimer/level/pwmX (r/w 0 to max_level )
 /sys/devices/virtual/misc/pwmtimer/enable/pwmX (r/w 1: enable, 0:disable)
 /sys/devices/virtual/misc/pwmtimer/max_level/pwmX (read only)
 	...where X should be one of 5/6/3/9/10/11
 	 *
 	 *
 	 *usage:
 	 *
 1) set pin mode function to pwm mode
 PWM5/6
  echo 2 > /sys/devices/virtual/misc/gpio/mode/gpio5
  echo 2 > /sys/devices/virtual/misc/gpio/mode/gpio6
 PWM3/9/10/11
  echo 1 > /sys/devices/virtual/misc/gpio/mode/gpio3
  echo 1 > /sys/devices/virtual/misc/gpio/mode/gpio9
  echo 1 > /sys/devices/virtual/misc/gpio/mode/gpio10
  echo 1 > /sys/devices/virtual/misc/gpio/mode/gpio11
 2) disable pwm before changing freq
  echo 0 > /sys/devices/virtual/misc/pwmtimer/enable/pwmX
 3) set freq ( min_freq <= freq <= max_freq )
  echo FREQ > /sys/devices/virtual/misc/pwmtimer/freq/pwmX
  
 NOTE: you can get freq first: cat /sys/devices/virtual/misc/pwmtimer/freq_range/pwmX
 	 */
 
 	
 	
 	@Override
 	public void load(PrimitiveManager pm) throws ExtensionException {
 		pm.addPrimitive("test1", new TestPWMPrimitive()  );
 		pm.addPrimitive("test2", new TestPWMPrimitive2()  );
 		
 		pm.addPrimitive("led-on", new LedOn() );
 		pm.addPrimitive("led-off", new LedOff() );
 		
 		pm.addPrimitive("set-mode", new SetPinMode() );
 		pm.addPrimitive("get-mode", new GetPinMode() );
 		
 		pm.addPrimitive("digital-read", new DigitalRead() );
 		pm.addPrimitive("digital-write", new DigitalWrite() );
 		
 		pm.addPrimitive("analog-read", new AnalogRead() );
 		pm.addPrimitive("pwm-set-level", new PWMSet() );
 		
 		//pm.addPrimitive("all-pin-info", new GetAllPinInfo());
 	}
 
 	
 	public static class LedOn extends DefaultCommand {
 		@Override
 		public void perform(Argument[] arg0, Context arg1)
 				throws ExtensionException, LogoException {
 			
 			File f = new File( pinDir + LED_PIN );
 			try {
 				FileOutputStream fos = new FileOutputStream( f );
 				fos.write( "0".getBytes() );
 				fos.close();
 			} catch (FileNotFoundException fnfe) {
 				fnfe.printStackTrace();
 			} catch (IOException ioe ) {
 				ioe.printStackTrace();
 			}
 		}
 	}
 	
 	public static class LedOff extends DefaultCommand {
 		@Override
 		public void perform(Argument[] arg0, Context arg1)
 				throws ExtensionException, LogoException {
 			File f = new File( pinDir + LED_PIN );
 			try {
 				FileOutputStream fos = new FileOutputStream( f );
 				fos.write( "1".getBytes() );
 				fos.close();
 			} catch (FileNotFoundException fnfe) {
 				fnfe.printStackTrace();
 			} catch (IOException ioe ) {
 				ioe.printStackTrace();
 			}
 		}
 	}
 	
 	public static class SetPinMode extends DefaultCommand {
 		public Syntax getSyntax() {
 			return Syntax.commandSyntax(new int[] { Syntax.StringType(),
 					Syntax.StringType() });
 		}
 
 		@Override
 		public void perform(Argument[] arg, Context arg1)
 				throws ExtensionException, LogoException {
 			String pin = arg[0].getString().toLowerCase();
 			String mode = arg[1].getString().toLowerCase();
 			if ( legalModes.containsKey(pin) )
 			{
 				HashMap<String, String> pinsModes = legalModes.get(pin);
 				if (pinsModes.containsKey(mode))
 				{
 					String modeValue = pinsModes.get(mode);
 					try
 					{
 						File f = new File( modeDir + pin );
 						FileOutputStream fos = new FileOutputStream( f );
 						fos.write(modeValue.getBytes());
 						fos.close();
 					}
 					catch (Exception e)
 					{
 						e.printStackTrace();
 						throw new ExtensionException( "An exception occurred in trying to set pin " + pin + " to mode " + mode + ".");
 					}
 				}
 				else
 				{
 					throw new ExtensionException("The requested mode " + mode + " is not defined for this interface to pcDuino.");
 				}
 			}
 			else
 			{
 				throw new ExtensionException("Pin " + pin + " is not defined for this interface to pcDuino.");
 			}
 		}
 	}
 	
 	public static class GetPinMode extends DefaultReporter {
 		public Syntax getSyntax() {
 			return Syntax.reporterSyntax(new int[] { Syntax.StringType(),
 					 }, Syntax.NumberType() );
 		}
 		@Override
 		public Object report(Argument[] arg, Context ctxt)
 		throws ExtensionException, LogoException {
 
 			String pin = arg[0].getString();
 			Double toreturn = -1.0;
 			if ( legalModes.containsKey(pin) )
 			{
 				try
 				{
 					String contents = "";
 					File f = new File( modeDir + pin );
 					FileInputStream fis = new FileInputStream( f );
 					byte[] contbytes = new byte[16];
 					int j = fis.read(contbytes);
 					for (int i = 0; i<j; i++) {
 						contents += (char)contbytes[i];
 					}
 					if (contents.contains(":")) {
 						int k = contents.indexOf(":");
 						contents = contents.substring(k + 1);
 					}
 					while ( contents.endsWith("\\n") ) {
 						contents = contents.substring(0,contents.length()-1);
 					}
 					toreturn = Double.valueOf(contents);
 					fis.close();
 				}
 				catch (Exception e)
 				{
 					e.printStackTrace();
 					throw new ExtensionException( "An exception occurred in trying to read mode of pin" + pin + ":\n" + e.getMessage());
 				}
 			}	
 			else
 			{
 				throw new ExtensionException("Pin " + pin + " is not defined for this interface to pcDuino.");
 			}
 			return toreturn;
 		}
 	}
 	
 	
 	public static class AnalogRead extends DefaultReporter {
 		public Syntax getSyntax() {
 			return Syntax.reporterSyntax(new int[] { Syntax.StringType(),
 					 }, Syntax.NumberType() );
 		}
 		
 		@Override
 		public Object report(Argument[] arg, Context ctxt)
 		throws ExtensionException, LogoException {
 
 			String pin = arg[0].getString();
 			String contents = "";
 			Double toreturn = -1.0;
 			if ( legalAnalogs.contains(pin) )
 			{
 				try
 				{
 					File f = new File( analogPinDir + pin );
 					FileInputStream fis = new FileInputStream( f );
 					byte[] contbytes = new byte[16];
 					int j = fis.read(contbytes);
 					for (int i = 0; i<j; i++) {
 						contents += (char)contbytes[i];
 					}
 					if (contents.contains(":")) {
 						int k = contents.indexOf(":");
 						contents = contents.substring(k + 1);
 					}
 					while ( contents.endsWith("\\n") ) {
 						contents = contents.substring(0,contents.length()-1);
 					}
 					toreturn = Double.valueOf(contents);
 					fis.close();
 				}
 				catch (Exception e)
 				{
 					e.printStackTrace();
 					throw new ExtensionException( "An exception occurred in trying to read from analog pin" + pin + ":\n" + e.getMessage());
 				}
 			}	
 			else
 			{
 				throw new ExtensionException("Analog pin " + pin + " is not defined for this interface to pcDuino.");
 			}
 			return toreturn;
 			
 		}
 	}
 	
 	
 	public static class DigitalRead extends DefaultReporter {	
 		public Syntax getSyntax() {
 			return Syntax.reporterSyntax(new int[] { Syntax.StringType(),
 					 }, Syntax.NumberType() );
 		}
 		@Override
 		public Object report(Argument[] arg, Context ctxt)
 				throws ExtensionException, LogoException {
 
 			String pin = arg[0].getString();
 			String contents = "";
 			double toreturn = -1.0;
 			if ( legalDigitals.contains(pin) )
 			{
 				String mode = pinStates.get(pin);
 				if ( mode.equals(READ) )
 				{
 						try
 						{
 							File f = new File( pinDir + pin );
 							FileInputStream fis = new FileInputStream( f );
 							int contint;
 							while ((contint = fis.read()) != -1)
 							{
 								contents += (char)contint;
 							}
 							toreturn = Double.valueOf(contents);
 							fis.close();
 						}
 						catch (Exception e)
 						{
 							e.printStackTrace();
 							throw new ExtensionException( "An exception occurred in trying to read from pin " + pin + ".");
 						}
 					
 				}
 				else
 				{
 					throw new ExtensionException("Pin " + pin + " is not set to READ mode.");
 				}
 			}	
 			else
 			{
 				throw new ExtensionException("Pin " + pin + " is not defined for this interface to pcDuino.");
 			}
 			
 			return toreturn;
 		}
 		
 	}
 	
 	
 
 	
 	
 	public static class DigitalWrite extends DefaultCommand {
 		public Syntax getSyntax() {
 			return Syntax.commandSyntax(new int[] { Syntax.StringType(),
 					Syntax.StringType() });
 		}
 
 		@Override
 		public void perform(Argument[] arg, Context arg1)
 				throws ExtensionException, LogoException {
 			String pin = arg[0].getString();
 			String state = arg[1].getString();
 			if ( legalDigitals.contains(pin) )
 			{
 				String mode = pinStates.get(pin);
 				if ( mode.equals(WRITE) )
 				{
 					if (state.equalsIgnoreCase("HIGH") || state.equalsIgnoreCase("LOW") )
 					{
 						try
 						{
 							File f = new File( pinDir + pin );
 							FileOutputStream fos = new FileOutputStream( f );
 							if (state.equalsIgnoreCase("HIGH"))
 								fos.write( "1".getBytes() );
 							else
 								fos.write("0".getBytes() );
 							fos.close();
 						}
 						catch (Exception e)
 						{
 							e.printStackTrace();
 							throw new ExtensionException( "An exception occurred in trying to set pin " + pin + " to mode " + mode + ".");
 						}
 					}
 					else
 					{
 						throw new ExtensionException("The requested state " + state + " is not available in digital-write.  Use HIGH or LOW.");
 					}
 				}
 				else
 				{
 					throw new ExtensionException("Pin " + pin + " is not set to WRITE mode.");
 				}
 			}	
 			else
 			{
 				throw new ExtensionException("Pin " + pin + " is not defined for this interface to pcDuino.");
 			}
 			
 		}
 	}
 	
 	
 	
 	public static class PWMSet extends DefaultReporter {
 		public Syntax getSyntax() {
			return Syntax.reporterSyntax(new int[] { Syntax.StringType(), Syntax.StringType()
 					 }, Syntax.StringType() );
 		}
 		@Override
 		public Object report(Argument[] arg, Context ctxt)
 				throws ExtensionException, LogoException {
 			
 			String pinNum = arg[0].getString();
 			String pwmLevelValue = arg[1].getString();
 			
 			String gpName = "gpio" + pinNum;
 			String pwmName = "pwm" + pinNum;
 			
 			if (legalPWMs.contains(pwmName))
 			try {
 				String pwmMODE = legalModes.get(pwmName).get("pwm");
 				File fmode = new File( modeDir + gpName );
 				FileOutputStream modefos = new FileOutputStream( fmode );
 				modefos.write( pwmMODE.getBytes() );
 				modefos.close();
 
 				File fenable = new File( pwmEnable + pwmName );
 				FileOutputStream enableFOS = new FileOutputStream( fenable );
 				enableFOS.write( "0".getBytes() );
 				enableFOS.close();
 				
 				File ffreq = new File( pwmFreq + pwmName );
 				FileOutputStream freqfos = new FileOutputStream( ffreq );
 				freqfos.write( "195".getBytes() );
 				freqfos.close();
 				
 				fenable = new File( pwmEnable + pwmName );
 				enableFOS = new FileOutputStream( fenable );
 				enableFOS.write( "1".getBytes() );
 				enableFOS.close();
 				
 				File flevel = new File( pwmLevel + pwmName );
 				FileOutputStream levelfos = new FileOutputStream( flevel );
 				levelfos.write( pwmLevelValue.getBytes() );
 				levelfos.close();
 				
 				
 			} catch (FileNotFoundException fnfe) {
 				fnfe.printStackTrace();
 			} catch (IOException ioe ) {
 				ioe.printStackTrace();
 			}
 			
 			
 			return pwmName + " set to level " + pwmLevelValue ;
 		}
 		
 	}
 	
 ///////////test prims
 	
 	public static class TestPWMPrimitive extends DefaultReporter {
 
 		private static final String TESTPIN = "pwm5";
 		@Override
 		public Object report(Argument[] arg0, Context arg1)
 				throws ExtensionException, LogoException {
 			try {
 				File fmode = new File( modeDir + "gpio5" );
 				FileOutputStream modefos = new FileOutputStream( fmode );
 				modefos.write( "2".getBytes() );
 				modefos.close();
 
 				File fenable = new File( pwmEnable + TESTPIN );
 				FileOutputStream enableFOS = new FileOutputStream( fenable );
 				enableFOS.write( "0".getBytes() );
 				enableFOS.close();
 				
 				File ffreq = new File( pwmFreq + TESTPIN );
 				FileOutputStream freqfos = new FileOutputStream( ffreq );
 				freqfos.write( "195".getBytes() );
 				freqfos.close();
 				
 				fenable = new File( pwmEnable + TESTPIN );
 				enableFOS = new FileOutputStream( fenable );
 				enableFOS.write( "1".getBytes() );
 				enableFOS.close();
 				
 				File flevel = new File( pwmLevel + TESTPIN );
 				FileOutputStream levelfos = new FileOutputStream( flevel );
 				levelfos.write( "1".getBytes() );
 				levelfos.close();
 				
 				
 			} catch (FileNotFoundException fnfe) {
 				fnfe.printStackTrace();
 			} catch (IOException ioe ) {
 				ioe.printStackTrace();
 			}
 			
 			
 			return "PWM5 set to level 1";
 		}
 		
 	}
 	
 
 	
 	public static class TestPWMPrimitive2 extends DefaultReporter {
 
 		private static final String TESTPIN2 = "pwm5";
 
 		@Override
 		public Object report(Argument[] arg0, Context arg1)
 				throws ExtensionException, LogoException {
 			try {
 				File fmode = new File( modeDir + "gpio5" );
 				FileOutputStream modefos = new FileOutputStream( fmode );
 				modefos.write( "2".getBytes() );
 				modefos.close();
 
 				File fenable = new File( pwmEnable + TESTPIN2 );
 				FileOutputStream enableFOS = new FileOutputStream( fenable );
 				enableFOS.write( "0".getBytes() );
 				enableFOS.close();
 				
 				File ffreq = new File( pwmFreq + TESTPIN2 );
 				FileOutputStream freqfos = new FileOutputStream( ffreq );
 				freqfos.write( "195".getBytes() );
 				freqfos.close();
 				
 				fenable = new File( pwmEnable + TESTPIN2 );
 				enableFOS = new FileOutputStream( fenable );
 				enableFOS.write( "1".getBytes() );
 				enableFOS.close();
 				
 				File flevel = new File( pwmLevel + TESTPIN2 );
 				FileOutputStream levelfos = new FileOutputStream( flevel );
 				levelfos.write( "128".getBytes() );
 				levelfos.close();
 				
 
 				
 	
 			} catch (FileNotFoundException fnfe) {
 				fnfe.printStackTrace();
 			} catch (IOException ioe ) {
 				ioe.printStackTrace();
 			}
 			
 			
 			return "PWM5 set to level 128";
 		}
 		
 	}
 	
 	
 }
