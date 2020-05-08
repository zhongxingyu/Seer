 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package brutes;
 
 import brutes.net.server.NetworkLocalTestServer;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.application.Application;
 import javafx.application.Platform;
 import javafx.event.Event;
 import javafx.event.EventHandler;
 import javafx.stage.Stage;
 
 /**
  *
  * @author Karl
  */
 public class Brutes extends Application {
     private Thread server;
     
     @Override
     public void start(Stage stage) throws Exception {
         stage.setResizable(false);
         stage.setTitle("Les brutes (TP Réseaux 2012/2013 - Karl Woditsch)");
         stage.setOnCloseRequest(new EventHandler(){
             @Override
             public void handle(Event t) {
                 server.interrupt();
                 Platform.exit();
             }
         });
         
         this.server = new Thread(){
             @Override
             public void run(){
                 try {
                     ServerSocket sockserv = new ServerSocket (42666);
                    sockserv.setSoTimeout(10000);
                     System.out.println("Server up");
                     while(!this.isInterrupted()){
                         final Socket sockcli = sockserv.accept();
                         sockcli.setSoTimeout(1000);
                         new Thread(){
                             @Override
                             public void run(){
                                 try (NetworkLocalTestServer n = new NetworkLocalTestServer(sockcli);){
                                     n.read();
                                 } catch (IOException ex) {
                                     Logger.getLogger(Brutes.class.getName()).log(Level.SEVERE, null, ex);
                                 }
                             }
                         }.start();
                     }
                 } catch (IOException ex) {
                     Logger.getLogger(Brutes.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         server.start();
         
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
