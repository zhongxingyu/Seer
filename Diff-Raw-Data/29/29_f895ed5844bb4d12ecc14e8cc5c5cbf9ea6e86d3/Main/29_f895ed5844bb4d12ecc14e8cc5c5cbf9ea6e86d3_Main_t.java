 package main;
 
import net.frontlinesms.test.serial.hayes.HayesPortHandler;
 
 import org.apache.camel.CamelContext;
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.camel.impl.DefaultCamelContext;
 
 import serial.mock.CommPortIdentifier;
 import serial.mock.MockSerial;
 import serial.mock.SerialPortHandler;
 
 /**
  * File for running manual scripts.
  * @author aga
  */
 public class Main {
 	private CamelContext context = new DefaultCamelContext();
 
 	/**
 	 * @param args
 	 * @throws Exception 
 	 */
 	public static void main(String[] args) throws Exception {
 		MockSerial.init();
 		
 		SerialPortHandler portHandler = new HayesPortHandler("ERROR: 999",
 				"AT", "OK",
 				"AT+CREG?", "ERROR", // gsm network reg
 				"AT+CMGF=0", "OK",
 				"+++", "", // switch 2 command mode
 				"AT+CPMS?", "+CPMS:\r\"ME\",1,15,\"SM\",0,100\rOK", // get storage locations
 				"AT+CPMS=\"ME\"", "OK",
 				"AT+CMGL=0", "+CMGL: 1,0,,51\r0791947101670000040485080039004010411171334029D737DB7C0EBBCF2E69D8BD6603C865D739DD22975DE3771B747DB3CDE7B0FB0CA296E774\r+CMGL: 2,1,,43\r0791947101670000040485080039004010313131604020D737DB7C0EBBCF2E69D8BD6603C865D739DD22975DE3771B442DCFE9\rOK"
 				);
 		MockSerial.setIdentifier("COM1", new CommPortIdentifier("COM1", portHandler));
 		
 		Main t = new Main();
 		t.createRoute();
 		t.context.start();
 	}
 
     private void createRoute() throws Exception {
 		RouteBuilder createRouteBuilder = createRouteBuilder();
 		context.addRoutes(createRouteBuilder);
 	}
 
 	protected RouteBuilder createRouteBuilder() throws Exception {
         return new RouteBuilder() {
             @Override
             public void configure() throws Exception {
 //                from("stream:in").to("smslib://COM1?hahahaha");
                 from("smslib://COM1?baud=9600")
                     .to("stream:out");
             }
         };
     }
 }
