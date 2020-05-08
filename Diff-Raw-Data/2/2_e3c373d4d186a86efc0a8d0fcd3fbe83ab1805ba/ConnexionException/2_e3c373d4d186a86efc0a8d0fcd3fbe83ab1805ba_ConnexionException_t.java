 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package packageException;
 
 /**
  *
  * @author Joachim
  */
public class ConnexionException extends Exception {
     
     private String mess;
     
     public ConnexionException(String msg)
     {
         this.mess = msg;
     }
     
     @Override
     public String toString()
     {
         return this.mess;
     }
     
 }
