 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package brutes;
 
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
     private ReadOnlyStringWrapper statusMessage;
 
     public LoginTask(String host, String login, String password) {
         this.statusMessage = new ReadOnlyStringWrapper();
         this.login = login;
         this.password = password;
         this.host = host;
     }
 
     public ReadOnlyStringProperty statusMessageProperty() {
         return this.statusMessage;
     }
     
     @Override
     protected Void call() throws Exception {
         try{
             String token;
             try (NetworkClient connection = new NetworkClient(new Socket(this.host, Protocol.CONNECTION_PORT))) {
                 token = connection.sendLogin(this.login, this.password);
                 ScenesContext.getInstance().setSession(new Session(this.host, token));
             } catch (UnknownHostException ex) {
                 this.statusMessage.set("Serveur invalide");
                throw new Exception("Login task failed at server connection");
             } catch (IOException ex) {
                 Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
             }
        } catch(ErrorResponseException ex){
            this.statusMessage.set(ex.getMessage());
            throw new Exception("Login task failed at login/password validation");
        } catch(InvalidResponseException ex){
             this.statusMessage.set(ex.getMessage());
            throw new Exception("Login task failed at server response");
         }
         return null;
     }
 }
