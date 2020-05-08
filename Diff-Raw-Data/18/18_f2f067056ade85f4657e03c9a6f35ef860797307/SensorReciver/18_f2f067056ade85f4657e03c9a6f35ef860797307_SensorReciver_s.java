 
 
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 
 /**
  * Klasa przykładowa pokazująca, jak odebrać dane z sensora.
  * @author Konrad Tabor
  */
 public class SensorReciver extends Thread
 {
 
     public void run()
     {
     	DatagramSocket socket=null;
         try
         {
             // Convert the argument to ensure that is it valid
             int port = Integer.parseInt(Config.getPort());
 
             // Construct the socket
             socket = new DatagramSocket(port);
             System.out.println("The server is ready...");
         	UpdateMeasurement updateMeasurement=new UpdateMeasurement();
 
             for (;;)
             {
                 // Create a packet
                 DatagramPacket packet = new DatagramPacket(new byte[100], 100);
 
                 // Receive a packet (blocking)
                 socket.receive(packet);
                 String str=new String(packet.getData());
                 MeasurementContainer.parseSensorData(str);
                 // Print the packet
                 System.out.println(packet.getAddress() + " " + packet.getPort() + ": " + str);

                 // Return the packet to the sender
                 socket.send(packet);
             }
         } catch (Exception e)
         {
             System.out.println(e);
             e.printStackTrace();
         }finally
         {
             if (socket != null)
             {
                 socket.close();
             }
         }
     }
 
 	
 }
