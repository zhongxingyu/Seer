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
    private String login;
    private String password;
     private Session session;

    public LoginTask(String login, String password) {
        this.login = login;
        this.password = password;
    }
     
     public Session getSession(){
         return this.session;
     }
     
     @Override
     protected Void call() throws Exception {
        if(this.login.isEmpty() || this.password.isEmpty()){
            throw new Exception("Bad login");
        }
         this.session = new Session(this.toString());
        
        //TODO : True login
        
         return null;
     }
 }
