 import client.CameraClient;
 import client.HTTPServer;
 import se.lth.cs.fakecamera.Axis211A;
 
 public class Main {
 	public static void main(String[] args) {
         Axis211A camera = new Axis211A();
        (new CameraServer(6600, 6601, "localhost", 6602, camera)).start();
        (new CameraServer(6610, 6611, "localhost", 6612, camera)).start();
         CameraClient cameraClient = new CameraClient(
                "localhost", 6600, 6602, 6601,
                "localhost", 6610, 6612, 6611
         );
         cameraClient.start();
         /*HTTPServer httpServer = new HTTPServer(1337,cameraClient);
         try{
         	httpServer.handleRequests();
         }catch(Exception e){
         	System.out.println("Errorzzozerzo");
         }*/
 	}
 }
