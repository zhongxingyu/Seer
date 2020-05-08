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
 
 	static final String pinDir = "/sys/devices/virtual/misc/gpio/pin/";
 	static final String modeDir= "/sys/devices/virtual/misc/gpio/mode/";
 	
 	static final String analogPinDir = "/proc/";
 	static final String pwmPinDir = "/sys/class/leds/";
 	
 	
 	
 	static final String UNINITIALIZED = "uninitialized";
 	static final String WRITE = "output";
 	static final String READ = "input";
 	static final ArrayList<String> legalModes = new ArrayList<String>();
 	static {
 		legalModes.add(UNINITIALIZED);
 		legalModes.add(WRITE);
 		legalModes.add(READ);
 	}
 	
 	static final String[] avalableAnalogs = {"adc0", "adc1", "adc2", "adc3", "adc4", "adc5" };
	static final String[] availablePWMs = {"pwm0", "pwm1", "pwm2", "pwm3", "pwm4", "pwm5"};
 	static final ArrayList<String> analogPinList = new ArrayList<String>();
 	static final ArrayList<String> pwmPinList = new ArrayList<String>();
 	static {
 		for (String ap : avalableAnalogs)
 		{
 			analogPinList.add( ap );
 		}
 		for (String pp : availablePWMs)
 		{
 			pwmPinList.add( pp );
 		}
 	}
 	
 	static final String[] availablePins = {"gpio0","gpio1","gpio2","gpio3","gpio4","gpio5","gpio6","gpio7",
             "gpio8", "gpio9", "gpio10", "gpio11", "gpio12", "gpio13",
             "gpio14", "gpio15", "gpio16", "gpio17", "gpio18", "gpio19"};
 	static final ArrayList<String> pinList = new ArrayList<String>();
 	static final HashMap<String, String> pinStates = new HashMap<String, String>();
 	static {	
 		for (String p : availablePins)
 		{
 			pinList.add( p );
 			pinStates.put(p, UNINITIALIZED);
 		}	
 	}
 	
 	
 	@Override
 	public void load(PrimitiveManager pm) throws ExtensionException {
 		pm.addPrimitive("test", new TestPrimitive()  );
 		pm.addPrimitive("led-on", new LedOn() );
 		pm.addPrimitive("led-off", new LedOff() );
 		pm.addPrimitive("set-pin-mode", new SetPinMode() );
 		pm.addPrimitive("digital-write", new DigitalWrite() );
 		pm.addPrimitive("digital-read", new DigitalRead() );
 		pm.addPrimitive("pwm-write", new PWMWrite() );
 		pm.addPrimitive("analog-read", new AnalogRead() );
 		pm.addPrimitive("get-pin-info", new GetPinInfo() );
 	}
 
 	public static class GetPinInfo extends DefaultReporter {
 		static final LogoListBuilder lb = new LogoListBuilder();
 		static {
 			for (String p : pinList)
 			{
 				lb.add( p );
 			}
 		}
 		@Override
 		public Object report(Argument[] arg0, Context arg1)
 				throws ExtensionException, LogoException {
 			return lb.toLogoList();
 		}
 	}
 	
 	public static class TestPrimitive extends DefaultReporter {
 
 		@Override
 		public Object report(Argument[] arg0, Context arg1)
 				throws ExtensionException, LogoException {
 			return "Hello";
 		}
 		
 	}
 	
 	public static class LedOn extends DefaultCommand {
 
 		@Override
 		public void perform(Argument[] arg0, Context arg1)
 				throws ExtensionException, LogoException {
 			
 			File f = new File( pinDir + "gpio18" );
 
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
 			File f = new File( pinDir + "gpio18" );
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
 			String pin = arg[0].getString();
 			String mode = arg[1].getString();
 			if ( pinList.contains(pin) )
 			{
 				if (legalModes.contains(mode))
 				{
 					try
 					{
 					File f = new File( modeDir + pin );
 					FileOutputStream fos = new FileOutputStream( f );
 					if (mode.equalsIgnoreCase(WRITE))
 						fos.write( "1".getBytes() );
 					else
 						fos.write("0".getBytes() );
 					fos.close();
 					pinStates.put(pin, mode);
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
 			double toreturn = -1.0;
 			if ( analogPinList.contains(pin) )
 			{
 
 				try
 				{
 					File f = new File( analogPinDir + pin );
 					FileInputStream fis = new FileInputStream( f );
 					int contint;
 					while ((contint = fis.read()) != -1)
 					{
 						contents += (char)contint;
 					}
 					if (contents.contains(":"))
 					{
 						int i = contents.indexOf(":");
 						contents = contents.substring(i + 1);
 					}
 					toreturn = Double.valueOf(contents);
 					fis.close();
 				}
 				catch (Exception e)
 				{
 					e.printStackTrace();
 					throw new ExtensionException( "An exception occurred in trying to read from analog pin " + pin + ":\n" + e.getMessage());
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
 		
 		//THIS is overkill -- i'm reading just one byte (the character for "1" or "0")
 		@Override
 		public Object report(Argument[] arg, Context ctxt)
 				throws ExtensionException, LogoException {
 
 			String pin = arg[0].getString();
 			String contents = "";
 			double toreturn = -1.0;
 			if ( pinList.contains(pin) )
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
 	
 	
 	public static class PWMWrite extends DefaultCommand {
 		public Syntax getSyntax() {
 			return Syntax.commandSyntax(new int[] { Syntax.StringType(),
 					Syntax.StringType() });
 		}
 
 		@Override
 		public void perform(Argument[] arg, Context arg1)
 				throws ExtensionException, LogoException {
 			String pin = arg[0].getString();
 			String value = arg[1].getString();
 			if ( pwmPinList.contains(pin) )
 			{
 				try
 				{
 					File f = new File( pwmPinDir + pin );
 					FileOutputStream fos = new FileOutputStream( f );
 					fos.write( value.getBytes() );
 					fos.close();
 				}
 				catch (Exception e)
 				{
 					e.printStackTrace();
 					throw new ExtensionException( "An exception occurred in trying to set pin " + pin + " to value " + value + ".");
 				}
 			}	
 			else
 			{
 				throw new ExtensionException("PWM Pin " + pin + " is not defined for this interface to pcDuino.");
 			}
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
 			if ( pinList.contains(pin) )
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
 	
 	
 	
 	
 	
 }
