 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package brutes.net.client;
 
 /**
  *
  * @author Karl
  */
 public class InvalidResponseException extends Exception{
     public InvalidResponseException(){
        super("Erreur du serveur");
     }
 }
