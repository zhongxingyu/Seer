 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
  */
 
 package net.manniche.xor.types;
 
 /**
  * Defines the content types that can be handled by the object repository servers.
  * The basic content type(s) are implemented in
  * {@link net.manniche.xor.types.BasicContentType}
  * @author stm
  */
 public interface ObjectRepositoryContentType {
 
     /**
      * @return a String representation of the content type
      */
     @Override
     public String toString();
 
 }
