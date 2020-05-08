 /*
  * AuthenticationCanceledException.java
  *
  * Created on 19. Oktober 2006, 09:19
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package de.cismet.security.exceptions;
 
 /**
  *
  * @author Sebastian
  */
 public class AuthenticationCanceledException extends Exception {
     
     /** Creates a new instance of AuthenticationCanceledException */
     public AuthenticationCanceledException() {
    super("The HTTP authentication was canceled by user");  //NOI18N
     }
     
     public AuthenticationCanceledException(String message) {
     super(message);
     }
     
 }
