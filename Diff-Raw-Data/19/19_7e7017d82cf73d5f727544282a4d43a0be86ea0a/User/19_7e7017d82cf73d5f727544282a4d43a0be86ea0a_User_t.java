 package org.aldeon.model;
 
import org.aldeon.crypt.Key;

 /**
  * User - an author of messages.
  *
  * TODO: should there be any username?
  */
 public interface User extends Identifiable {
    Key getPublicKey();
 }
