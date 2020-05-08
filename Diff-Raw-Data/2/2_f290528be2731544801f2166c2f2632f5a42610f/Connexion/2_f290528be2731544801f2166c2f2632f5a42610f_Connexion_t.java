 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package flickr;
 
 import org.apache.http.client.methods.HttpGet;
 
 /**
  *
  * @author peixoton
  */
 public class Connexion {
     
     private String login;
     private String password;
     
     public static final String KEY = "5ba9bb9bbac0804efaccd0f9d5b4b756";
     public static final String SECRET_KEY = "d44f09102f60a452";
     
     public Connexion(String login, String password){
         this.login = login;
         this.password = password;
     }
 
     public String getLogin() {
         return login;
     }
 
     public void setLogin(String login) {
         if(this.login.length() > 0)
         this.login = login;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         if(this.password.length() > 0)
             this.password = password;
     }
     
     @Override
     public String toString(){
         return "Login/Mot de passe : " + this.login + "/" + this.password;
     }
     
    public void login(){
         //OAuthConsumer consumer = new CommonsHttpOAuthConsumer(Connexion.KEY, Connexion.SECRET_KEY);
         //consumer.setTokenWithSecret(accessToekn, accessSecret);
         //HttpGet request = new HttpGet();
         //consumer.sign(request);
     }
     
 }
