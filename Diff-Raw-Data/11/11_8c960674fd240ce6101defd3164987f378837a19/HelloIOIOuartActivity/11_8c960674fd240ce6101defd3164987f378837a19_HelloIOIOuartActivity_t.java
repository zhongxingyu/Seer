 package my.subject.HelloIOIOuart;
 
 //import ioio.examples.hello.MainActivity.IOIOThread;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import my.subject.HelloIOIOuart.R;
//import ioio.lib.api.DigitalInput;
 import ioio.lib.api.IOIO;
 import ioio.lib.api.DigitalOutput;
 import ioio.lib.api.Uart;
 //import ioio.lib.api.Uart.Parity;
 //import ioio.lib.api.Uart.StopBits;
 import ioio.lib.api.exception.ConnectionLostException;
 import ioio.lib.util.AbstractIOIOActivity;
 import android.os.Bundle;
 //import android.util.Log;
 import android.widget.ToggleButton;
 
 /**
  * This is the main activity of the HelloIOIO example application.
  * 
  * It displays a toggle button on the screen, which enables control of the
  * on-board LED. This example shows a very simple usage of the IOIO, by using
  * the {@link AbstractIOIOActivity} class. For a more advanced use case, see the
  * HelloIOIOPower example.
  */
 public class HelloIOIOuartActivity extends AbstractIOIOActivity {
 	private ToggleButton button_;
	private final String Str = "Hello, I'm VIFAM...OK VIFAM, Your number is 7.\n";
	private final String Str2 = "\nWipe away all your tears. Together we will conquer fear!\n";
 
 	/**
 	 * Called when the activity is first created. Here we normally initialize
 	 * our GUI.
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		button_ = (ToggleButton) findViewById(R.id.button);
 	}
 
 	/**
 	 * This is the thread on which all the IOIO activity happens. It will be run
 	 * every time the application is resumed and aborted when it is paused. The
 	 * method setup() will be called right after a connection with the IOIO has
 	 * been established (which might happen several times!). Then, loop() will
 	 * be called repetitively until the IOIO gets disconnected.
 	 */
 	class IOIOThread extends AbstractIOIOActivity.IOIOThread {
 		private static final int PIN_TX = 14;		// it should be 5V tolerant
 		private static final int PIN_SERCON_TX = 39;
 		/** The on-board LED. */
 		private DigitalOutput led_;
 		private Uart uart_1, uart_2;
 		private OutputStream out, scon;
 
 		/**
 		 * Called every time a connection with IOIO has been established.
 		 * Typically used to open pins.
 		 * 
 		 * @throws ConnectionLostException
 		 *             When IOIO connection is lost.
 		 * 
 		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
 		 */
 		@Override
 		protected void setup() throws ConnectionLostException {
 			led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
    		uart_1 = ioio_.openUart(null, new DigitalOutput.Spec(PIN_TX, DigitalOutput.Spec.Mode.OPEN_DRAIN), 31250, Uart.Parity.NONE, Uart.StopBits.ONE);
     		out = uart_1.getOutputStream();
     		uart_2 = ioio_.openUart(IOIO.INVALID_PIN, PIN_SERCON_TX, 9600, Uart.Parity.NONE, Uart.StopBits.ONE);
     		scon = uart_2.getOutputStream();
     		
 //    		try {
 //				scon.write( new String("Hello, I'm VIFAM...OK VIFAM, Your number is 7.").getBytes() );
 //			} catch (IOException e) {
 //				// TODO Auto-generated catch block
 //				//e.printStackTrace();
 //			}
 //			sendMIDImsg(0x90, 90, 127);	// note on
 //			//sendMIDImsg(0x80, 90, 127);	// note off
 		}
 
 		/**
 		 * Called repetitively while the IOIO is connected.
 		 * 
 		 * @throws ConnectionLostException
 		 *             When IOIO connection is lost.
 		 * 
 		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
 		 */
 		@Override
 		protected void loop() throws ConnectionLostException {
 			led_.write(!button_.isChecked());
 			try {
 				sleep(10);
 			} catch (InterruptedException e) {
 			}
 			
     		try {
 				scon.write( Str.getBytes() );
				out.write( Str2.getBytes() );
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				//e.printStackTrace();
 			}
 			sendMIDImsg(0x90, 90, 127);	// note on
 			delay(500);
 			sendMIDImsg(0x80, 90, 127);	// note off
 			delay(500);
 		}
 		
 		private void serOut(int i) {
 			if ( out != null ) {
 				try {
 					out.write(i);
 				} catch (IOException e) {
 					//Log.d(TAG, "IOException in serOut()");
 				} catch (Exception e) {
 					//Log.d(TAG, "Something weird occurred in serOut()");
 				}
 			}
 		}
 		
 		private void sendMIDImsg(int stat, int data1, int data2) {
 			serOut(stat);
 			serOut(data1);
 			serOut(data2);
 		}
 		
 		private void delay(int wait) {
 			try {
 				Thread.sleep(wait);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				//e.printStackTrace();
 				//Log.d(TAG, "InterruptedException in delay()");
 			}			
 		}
 
 				
 	}
 
 	/**
 	 * A method to create our IOIO thread.
 	 * 
 	 * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
 	 */
 	@Override
 	protected AbstractIOIOActivity.IOIOThread createIOIOThread() {
 		return new IOIOThread();
 	}
 }
