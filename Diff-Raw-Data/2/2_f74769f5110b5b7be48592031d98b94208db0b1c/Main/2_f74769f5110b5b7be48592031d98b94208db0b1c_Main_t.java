 import client.CameraClient;
 import client.HTTPServer;
 import se.lth.cs.fakecamera.Axis211A;
 
 public class Main {
 	public static void main(String[] args) {
         Axis211A camera = new Axis211A();
         (new CameraServer(6077, 6078, "localhost", 6079, camera)).start();
         (new CameraServer(6080, 6081, "localhost", 6082, camera)).start();
 
         CameraClient cameraClient = new CameraClient(
                 "localhost", 6077, 6079, 6078,
                 "localhost", 6080, 6082, 6081
         );
         cameraClient.start();
        HTTPServer httpServer = new HTTPServer(1337,cameraClient);
         try{
         	httpServer.handleRequests();
         }catch(Exception e){
         	System.out.println("Errorzzozerzo");
         }
 	}
 }
