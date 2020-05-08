 import gnu.io.CommPortIdentifier;
 
 
 import io.ClientInput;
 import io.ConnectionManager;
 import io.LEDOutput;
 import io.SerialDevice;
 import io.SerialLEDDevice;
 
 import java.io.IOException;
 import java.util.Enumeration;
 
 import modules.led.CursorColor;
 import modules.led.Fade;
 import modules.led.ScreenColor;
 import modules.led.Random;
 
 
 public class Test {
 
 	private static final String PORT_NAMES[] = { 
 		"/dev/ttyUSB0", // Linux
 		"/dev/tty.usbserial-A8008EGb", // Mac OS X
 		"COM3" // Windows
 	};
 	
 	public static void main(String[] args) throws InterruptedException {
 		
 	//	Test.testClientInput();
 		
 		ConnectionManager conManager = new ConnectionManager();
 		conManager.connectSerialDevices();
 		System.out.println("number of connected devices: " + conManager.getLEDOutputList().size());
 		
 		for(LEDOutput ledOut: conManager.getLEDOutputList()){
 			Fade fade = new Fade(ledOut, 1000);
 			new Thread(fade).start();
 			Thread.sleep(7000);
 			fade.stop();
 		}
 		
		Thread.sleep(5000);
 		
 		conManager.closeAll();
		Thread.sleep(5000);
		
 		
 //		//determine the port to use
 //		String port = null;
 //		Enumeration<?> portEnum = CommPortIdentifier.getPortIdentifiers();
 //		while (portEnum.hasMoreElements()) {
 //			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
 //			for (String portName : PORT_NAMES) {
 //				if (currPortId.getName().equals(portName)) {
 //					port = portName;
 //					break;
 //				}
 //			}
 //		}
 //		if (port == null) {
 //			System.out.println("Could not find COM port.");
 //			return;
 //		}
 //
 //		try {
 //			//TODO: automatically determine code version/SerialLEDdevice capabilities
 //			SerialLEDDevice led = new SerialLEDDevice(port, 115200, SerialLEDDevice.Code.D_CODE);
 //
 //			Fade fade = new Fade(led,1000);
 //			System.out.println("run module Fade for 5 seconds");
 //			new Thread(fade).start();
 //			Thread.sleep(5000);
 //			fade.stop();
 //			System.out.println("module Fade done");
 //			
 //			Random random = new Random(led,100);
 //			System.out.println("run module Random for 10 seconds");
 //			new Thread(random).start();
 //			Thread.sleep(10000);
 //			random.stop();
 //			System.out.println("module Random done");
 //
 //			CursorColor cursorColor = new CursorColor(led);
 //			System.out.println("run module CursorColor for 10 seconds, move your cursor arround!");
 //			new Thread(cursorColor).start();
 //			Thread.sleep(10000);
 //			cursorColor.stop();
 //			System.out.println("module CursorColor done");
 //
 //			ScreenColor screenColor = new ScreenColor(led);
 //			System.out.println("run module ScreenColor for 10 seconds, move windows on your screen arround!");
 //			new Thread(screenColor).start();
 //			Thread.sleep(10000);
 //			screenColor.stop();
 //			System.out.println("module ScreenColor done");
 //
 //			Thread.sleep(1000); //wait 1 second to stop modules
 //			led.close();
 //			System.out.println("connection closed, test complete");
 //			
 //		} catch (InterruptedException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		} 
 		
 		
 		
 //		SerialConnection con = new SerialConnection("/dev/tty.usbserial-A8008EGb", 115200);
 //		try {
 //			con.connect();
 //			Thread.sleep(2000);
 //			con.send("G254");
 //			
 //			Thread.sleep(5000);
 //			con.close();
 //			
 //		} catch (InterruptedException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		} catch (IOException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 		
 	}
 
 	
 	public static void testClientInput(){
 		
 		new Thread(new ClientInput()).start();
 	}
 }
