 package org.nebulostore.communication.dht;
 
 import java.io.Serializable;
 
 /**
  * Interface for arbitrary merging operations in DHT.
  * Please implement the method to support desired
  * semantics of updates to existing keys.
  *
  * @author Marcin Walas
  *
  */
 public interface Mergeable extends Serializable {
 
   /**
    * This returns an object that will be stored in
    * DHT iff there is already a value written
    * to the key.
    *
    * Please note that in Kademlia impl. there
    * is no guarantee that this will be triggered
    * if a new peer has local put operation on local map
    * and the local map is empty.
    *
    * @param other the other Mergeable object
   * @return
    */
   Mergeable merge(Mergeable other);
 }
