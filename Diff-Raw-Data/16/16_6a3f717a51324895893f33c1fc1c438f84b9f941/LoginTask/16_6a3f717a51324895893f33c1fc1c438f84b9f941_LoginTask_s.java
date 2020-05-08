 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package brutes;
 
 import brutes.user.Session;
 import javafx.concurrent.Task;
 
 /**
  *
  * @author Karl
  */
 public class LoginTask extends Task{
     private Session session;
     
     public Session getSession(){
         return this.session;
     }
     
     @Override
     protected Void call() throws Exception {
         this.session = new Session(this.toString());
         return null;
     }
 }
