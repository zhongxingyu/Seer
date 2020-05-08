 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package de.javauni.jarcade.event;
 
 /**
  * interface used to broadcast messages on a channel
 * @param <L> the listener type
  * @author wabu
  */
 public interface Broadcastor<L extends Listener> {
     void apply(L l);
 }
