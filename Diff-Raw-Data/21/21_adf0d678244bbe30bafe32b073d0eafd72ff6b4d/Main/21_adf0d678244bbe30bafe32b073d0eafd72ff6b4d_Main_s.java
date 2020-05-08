 import client.CameraClient;
 import client.HTTPServer;
 import se.lth.cs.fakecamera.Axis211A;
 
 public class Main {
 	public static void main(String[] args) {
         Axis211A camera = new Axis211A();
//        (new CameraServer(6077, 6078, "localhost", 6079, camera)).start();
        //(new CameraServer(6080, 6081, "localhost", 6082, camera)).start();        
         CameraClient cameraClient = new CameraClient(
                "130.235.34.189", 6600, 6602, 6601,
                "130.235.34.194", 6610, 6612, 6611
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
