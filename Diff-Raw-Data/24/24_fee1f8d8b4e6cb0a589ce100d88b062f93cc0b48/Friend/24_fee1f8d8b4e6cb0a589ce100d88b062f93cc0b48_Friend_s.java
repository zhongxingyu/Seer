 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package facebin.Profile;
 
 /**
  *
  * @author Jorge
  */
 public class Friend {
     
     String email;
     boolean aceptado;
     String quienInvito;
     boolean amigo;
     
     public Friend( String e, boolean a, String who)
     {
         this.email = e;
         this.aceptado = a;
         this.quienInvito = who;
     }
 
     public boolean isAceptado() {
         return aceptado;
     }
 
     public String getEmail() {
         return email;
     }
     
     public String getQuienInvito()
     {
         return this.quienInvito;
     }
     
     public void esAmigo(String email)
     {
        if(aceptado == true)
         {
            this.amigo = true;
         }
         else
         {
            this.amigo = false;
         }
     }
     
     
     
 }
