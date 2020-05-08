 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package brutes.gui;
 
 import brutes.ScenesContext;
 import brutes.net.Protocol;
 import brutes.net.client.ErrorResponseException;
 import brutes.net.client.InvalidResponseException;
 import brutes.net.client.NetworkClient;
 import java.io.IOException;
 import java.net.Socket;
 import java.net.URL;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.event.ActionEvent;
 import javafx.event.Event;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.Node;
 import javafx.scene.control.TextField;
 import javafx.stage.Stage;
 
 /**
  * FXML Controller class
  *
  * @author Karl
  */
 public class UpdateCharacterController implements Initializable {
     @FXML
     private TextField characterName;
     
     @FXML
     private void handleCancelAction(ActionEvent e){
         this.closeStage(e);
     }
     
     @FXML
     private void handleSubmitAction(ActionEvent e){
         new Thread(){
             @Override
             public void run() {
                 try (NetworkClient connection = new NetworkClient(new Socket(ScenesContext.getInstance().getSession().getServer(), Protocol.CONNECTION_PORT))) {
                     try {
                         connection.sendUpdateCharacter(ScenesContext.getInstance().getSession().getToken(), characterName.getText());
                     } catch (InvalidResponseException | ErrorResponseException ex) {
                         Logger.getLogger(FightController.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 } catch (IOException ex) {
                     Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 ScenesContext.getInstance().getSession().netLoadMyCharacter();
             }
         }.start();
         this.closeStage(e);
     }
     
     private void closeStage(Event e){
         Node source = (Node) e.getSource(); 
         Stage stage = (Stage) source.getScene().getWindow();
         stage.close();
     }
     /**
      * Initializes the controller class.
      */
     @Override
     public void initialize(URL url, ResourceBundle rb) {
        this.characterName.textProperty().bind(ScenesContext.getInstance().getSession().getMyCharacter().getName());
     }    
 }
