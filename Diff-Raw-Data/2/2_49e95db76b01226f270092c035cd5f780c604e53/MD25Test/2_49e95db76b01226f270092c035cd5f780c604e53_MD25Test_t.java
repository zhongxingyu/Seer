 package ttree.pipin.i2c;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import com.pi4j.io.i2c.I2CBus;
 import com.pi4j.io.i2c.I2CDevice;
 import com.pi4j.io.i2c.I2CFactory;
 
 /**
  * Test MD 25 device attached to I2C bus
  * 
  * @author michael
  */
 public class MD25Test {
 
 	/**
 	 * Main for Motor Test
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 
 		// RPi external I2C bus
 		final I2CBus piExtBus = I2CFactory.getInstance(1);
 		// Default address for MD25 board
 		final I2CDevice md25 = piExtBus.getDevice(0x58);
 	
 		int revision = md25.read(MD25.REG_REVISION);
 		
 		System.out.print("MD25 revision " + revision + " ");
 		md25.write(MD25.REG_MODE, (byte)1);
 		int mode = md25.read(MD25.REG_MODE);
 		System.out.println("in mode " + mode);
 
 		// open up standard input
 	    final BufferedReader sin = new BufferedReader(new InputStreamReader(System.in));
 		for (;;) {
 			System.out.print("MD25 motor 1 speed (-128..127) : ");
 			final String input = sin.readLine();
 			if (input.isEmpty() == true)
 				break;
 			
 			try {
 				final int value = Integer.parseInt(input);
				if (value < -128 || value > 127) {
 					throw new NumberFormatException();
 				}
 				md25.write(MD25.REG_SPEED1, (byte)value);
 			}
 			catch (NumberFormatException e) {
 				System.err.println("Expecting -128..127\n");
 			}
 		}
 	}
 	
 }
