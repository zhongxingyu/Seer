 import client.*;
 import se.lth.cs.fakecamera.Axis211A;
 
 public class Main {
 	public static void main(String[] args) {
         Axis211A camera = new Axis211A();
 
         (new CameraServer(6600, 6601, "localhost", 6602, camera)).start();
         (new CameraServer(6610, 6611, "localhost", 6612, camera)).start();
///*
         String
             serverURL1 = "localhost",
             serverURL2 = "localhost";
         int
             pictureReceivePort1 = 6600,
             stateSendPort1 = 6601,
             stateReceivePort1 = 6602,
             pictureReceivePort2 = 6610,
             stateSendPort2 = 6611,
             stateReceivePort2 = 6612;
 
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
