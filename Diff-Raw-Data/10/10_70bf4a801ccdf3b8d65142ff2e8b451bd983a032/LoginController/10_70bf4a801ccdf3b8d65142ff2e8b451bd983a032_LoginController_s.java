 package brutes;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import brutes.user.Session;
 import java.net.URL;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.concurrent.Worker;
 import javafx.concurrent.Worker.State;
 import javafx.event.ActionEvent;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.Button;
 import javafx.scene.control.PasswordField;
 import javafx.scene.control.ProgressIndicator;
 import javafx.scene.control.TextField;
 import javafx.scene.input.KeyCode;
 import javafx.scene.input.KeyEvent;
 import javafx.scene.layout.AnchorPane;
 import javafx.scene.text.Text;
 
 /**
  * FXML Controller class
  *
  * @author Karl
  */
 public class LoginController implements Initializable {
     private Session session;
     
     @FXML
     private AnchorPane root;
     @FXML
     private Button connexion;
     @FXML
     private TextField login;
     @FXML
     private PasswordField password;
     @FXML
     private ProgressIndicator loading;
     @FXML
     private Text logError;
     
     @FXML
     private void handleKeyPressed(KeyEvent e){
         Logger.getLogger(LoginController.class.getName()).log(Level.INFO, "Handle");
         KeyCode key = e.getCode();
         if(key == KeyCode.ENTER){
             this.login();
         }
     }
     @FXML
     private void handleConnexionAction(ActionEvent e){
         Logger.getLogger(LoginController.class.getName()).log(Level.INFO, "Handle");
         this.login();
     }
     @FXML
     private void handleInputClick(){
         logError.setVisible(false);
     }
     
     private synchronized void login(){
         Logger.getLogger(LoginController.class.getName()).log(Level.INFO, "Login");
         
         this.loading.setVisible(true);
         this.login.setDisable(true);
         this.password.setDisable(true);
         this.connexion.setDisable(true);
         
         final LoginTask loginTask = new LoginTask(this.login.getText(), this.password.getText());
         new Thread(loginTask).start();
         loginTask.stateProperty().addListener(new ChangeListener<Worker.State>() {
             @Override
             public void changed(ObservableValue<? extends State> observable, State oldValue, State newState) {
                 if(newState == Worker.State.SUCCEEDED){
                     loginTask.cancel();
                    ScenesContext.getInstance().showFight(loginTask.getSession());
                     this.reactiveLogin();
                 }
                 else if(newState == Worker.State.FAILED){
                     loginTask.cancel();
                     Logger.getLogger(LoginController.class.getName()).log(Level.INFO, "Thread failed");
                     logError.setVisible(true);
                     this.reactiveLogin();
                 }
             }   
 
             private void reactiveLogin() {
                 Logger.getLogger(LoginController.class.getName()).log(Level.INFO, "Reactive login");
                 login.setDisable(false);
                 password.setDisable(false);
                 connexion.setDisable(false);
                 loading.setVisible(false);
             }
         });
     }
    
     /**
      * Initializes the controller class.
      */
     @Override
     public void initialize(URL url, ResourceBundle rb) {
         // TODO
     }    
 }
