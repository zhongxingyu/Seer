 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package brutes;
 
 import brutes.db.DatasManager;
 import brutes.net.Protocol;
 import brutes.net.client.NetworkClient;
 import brutes.net.server.NetworkLocalTestServer;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.sql.Connection;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.application.Application;
 import javafx.stage.Stage;
 
 /**
  *
  * @author Karl
  */
 public class Brutes extends Application {
     
     @Override
     public void start(Stage stage) throws Exception {
         
         // DEBUG
         //(new File("bdd.db")).delete();
         
        Connection instance = DatasManager.getInstance("sqlite", "bdd.db");
         System.out.println(instance);
         
         new Thread(){
             @Override
             public void run(){
                 try {
                     ServerSocket sockserv = new ServerSocket (42666);
                     System.out.println("Server up");
                     while(true){
                         Socket sockcli = sockserv.accept();
                         NetworkLocalTestServer n = new NetworkLocalTestServer(sockcli);
                         try {
                             n.read();
                         } catch (Exception ex) {
                             Logger.getLogger(Brutes.class.getName()).log(Level.SEVERE, null, ex);
                         }
                     }
                 } catch (IOException ex) {
                     Logger.getLogger(Brutes.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }.start();
         
         ScenesContext.getInstance().setStage(stage);
         ScenesContext.getInstance().showLogin();
     }
 
     /**
      * The main() method is ignored in correctly deployed JavaFX application.
      * main() serves only as fallback in case the application can not be
      * launched through deployment artifacts, e.g., in IDEs with limited FX
      * support. NetBeans ignores main().
      *
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         launch(args);
     }
 }
