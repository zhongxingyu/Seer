 package hardware;
 
 import httpserver.Server;
 import java.io.IOException;
 
 import com.tinkerforge.IPConnection;
 import com.tinkerforge.NotConnectedException;
 
 public class RoboControl {
     private static final String robohost = "robo";
     private static final int roboport = 4223;
     private static final IPConnection ipcon = new IPConnection();
     private static TFConfigurator tfConfigurator;
     
     private static final String serverhost = "localhost";
     private static final int serverport = 8080;
 
 	public static void main(String[] args) {
 		// ----------------------------------------------------------------------
 		// start web server
 		// ----------------------------------------------------------------------
 		final Server server = new Server(serverhost, serverport);
 		try {
 			server.start();
 			System.out.println("Web Server started.....");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		
 		// ----------------------------------------------------------------------
 		// initialize TF hardware
 		// ----------------------------------------------------------------------
         while(true) {
             try {
                 // try to connect
             	ipcon.connect(robohost, roboport);
                 break;
             } catch(Exception e) {
             	e.printStackTrace();
             }
             try {
                 Thread.sleep(2000);
             } catch(InterruptedException ei) {
             }
         }
 		
         tfConfigurator = new TFConfigurator(ipcon);
         ipcon.addEnumerateListener(tfConfigurator);
         ipcon.addConnectedListener(tfConfigurator);
         
         
         while(true) {
             try {
             	// try to configure hardware
                 ipcon.enumerate();
                 break;
             } catch(NotConnectedException e) {
             	e.printStackTrace();
             }
             try {
                 Thread.sleep(2000);
             } catch(InterruptedException ei) {
             }
         }
 		
         
        // ---------------------------------------------------------------------
         // On server shutdown
         // ---------------------------------------------------------------------
 		Runtime.getRuntime().addShutdownHook(new Thread(){
 			public void run(){
 				server.stop();
 				try {
 					ipcon.disconnect();
 				} catch (NotConnectedException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 }
