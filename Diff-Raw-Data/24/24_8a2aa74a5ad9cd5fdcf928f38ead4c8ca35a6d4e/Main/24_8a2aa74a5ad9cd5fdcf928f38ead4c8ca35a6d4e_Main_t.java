 import client.*;
 import se.lth.cs.fakecamera.Axis211A;
 
 public class Main {
 	public static void main(String[] args) {
         String
            clientURL1 = "localhost",
            clientURL2 = "localhost",
             serverURL1 = "localhost",
             serverURL2 = "localhost";
         int
             pictureReceivePort1 = 6600,
             stateSendPort1 = 6601,
             stateReceivePort1 = 6602,
             pictureReceivePort2 = 6610,
             stateSendPort2 = 6611,
             stateReceivePort2 = 6612;
 
         Axis211A camera = new Axis211A();
         (new CameraServer(pictureReceivePort1, stateSendPort1, clientURL1, stateReceivePort1, camera)).start();
         (new CameraServer(pictureReceivePort2, stateSendPort2, clientURL2, stateReceivePort2, camera)).start();
 ///*
         HTTPMonitor httpMonitor = new HTTPMonitor();
         ClientStateMonitor clientStateMonitor = new ClientStateMonitor();
 
         (new CameraClient(
             new GUI(serverURL1, serverURL2, clientStateMonitor),
             clientStateMonitor,
             httpMonitor,
             serverURL1, pictureReceivePort1, stateReceivePort1, stateSendPort1,
             serverURL2, pictureReceivePort2, stateReceivePort2, stateSendPort2
         )).start();
 
         (new HTTPServer(1337, httpMonitor)).start();
 //*/
 	}
 }
