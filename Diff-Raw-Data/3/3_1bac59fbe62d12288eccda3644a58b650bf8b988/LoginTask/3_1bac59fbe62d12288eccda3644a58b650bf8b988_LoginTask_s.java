 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package brutes.gui;
 
 import brutes.ScenesContext;
 import brutes.gui.LoginController;
 import brutes.net.Protocol;
 import brutes.net.client.ErrorResponseException;
 import brutes.net.client.InvalidResponseException;
 import brutes.net.client.NetworkClient;
 import brutes.user.Session;
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.beans.property.ReadOnlyBooleanProperty;
 import javafx.beans.property.ReadOnlyBooleanWrapper;
 import javafx.beans.property.ReadOnlyStringProperty;
 import javafx.beans.property.ReadOnlyStringWrapper;
 import javafx.concurrent.Task;
 
 /**
  *
  * @author Karl
  */
 public class LoginTask extends Task{
     private String login;
     private String password;
     private String host;
     private ReadOnlyBooleanWrapper loginError;
     private ReadOnlyBooleanWrapper passwordError;
     private ReadOnlyBooleanWrapper hostError;
 
     public LoginTask(String host, String login, String password) {
         this.login = login;
         this.password = password;
         this.host = host;
     }
 
     public ReadOnlyBooleanProperty getLoginErrorProperty(){
         return this.loginError.getReadOnlyProperty();
     }
     public ReadOnlyBooleanProperty getPasswordErrorProperty(){
         return this.passwordError.getReadOnlyProperty();
     }
     public ReadOnlyBooleanProperty getHostErrorProperty(){
         return this.hostError.getReadOnlyProperty();
     }
     
     @Override
     protected Void call() throws Exception {
         try{
             String token;
             try (NetworkClient connection = new NetworkClient(new Socket(this.host, Protocol.CONNECTION_PORT))) {
                 token = connection.sendLogin(this.login, this.password);
                 ScenesContext.getInstance().setSession(new Session(this.host, token));
             } catch (UnknownHostException ex) {
                 this.hostError.set(true);
                 throw new Exception("Login task failed at server connection");
             } catch (IOException ex) {
                 Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
             }
         } catch(ErrorResponseException ex){
                 this.loginError.set(true);
             throw new Exception("Login task failed at login/password validation");
         } catch(InvalidResponseException ex){
             this.passwordError.set(true);
             throw new Exception("Login task failed at server response");
         }
         return null;
     }
 }
