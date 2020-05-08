 /*
  * DHCPServerApp.java
  */
 
 package dhcpserver;
 
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 
 import java.net.InetAddress;
import java.util.EventObject;
 import org.jdesktop.application.Application;
 import org.jdesktop.application.SingleFrameApplication;
 
 /**
  * The main class of the application.
  */
 public class DHCPServerApp extends SingleFrameApplication {
 
     static class DHCPD extends Thread
     {
         public void run()
         {
             byte buffer[] = new byte[1000];
             DHCPController controller = new DHCPController();
 
             try {
                 DatagramSocket socket = new DatagramSocket(67);
                 System.out.println("waiting for discover packet");
 
                 while (true) { try {
                     // receive
                     DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
                     socket.receive(datagram);
 
                     // read
                     if (controller.readMessage(buffer, datagram.getLength()))
                     {
                         // send
                         DatagramPacket sendPacket = new DatagramPacket(controller.response, controller.index, InetAddress.getByAddress(new byte[]{-1, -1, -1, -1}), 68);
                         socket.send(sendPacket);
                     }
                 } catch (Exception e) { System.out.println( e.getMessage()); }
             }
             } catch (Exception e) { System.out.println( e.getMessage()); }
         }
     }
 
     /**
      * At startup create and show the main frame of the application.
      */
     @Override protected void startup() {

        addExitListener(new ExitListener() {
            public boolean canExit(EventObject e) {

                DHCPDatabase.writeToFile();

                return true;
            }
            public void willExit(EventObject event) {

            }
        });

         show(new DHCPServerView(this));
     }
 
     /**
      * This method is to initialize the specified window by injecting resources.
      * Windows shown in our application come fully initialized from the GUI
      * builder, so this additional configuration is not needed.
      */
     @Override protected void configureWindow(java.awt.Window root) {
     }
 
     /**
      * A convenient static getter for the application instance.
      * @return the instance of DHCPServerApp
      */
     public static DHCPServerApp getApplication() {
         return Application.getInstance(DHCPServerApp.class);
     }
 
     /**
      * Main method launching the application.
      */
     public static void main(String[] args) {
         launch(DHCPServerApp.class, args);
 
         DHCPD server = new DHCPD();
         server.start();
 
         System.out.println("dhcp server launched");
     }
 }
