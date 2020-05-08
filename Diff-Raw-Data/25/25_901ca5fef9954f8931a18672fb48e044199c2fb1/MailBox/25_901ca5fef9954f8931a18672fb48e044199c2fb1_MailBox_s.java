 package org.gethydrated.hydra.actors.mailbox;
 

 public interface Mailbox {
 
    Message take() throws InterruptedException;

     Message poll();
 
     void offer(Message m);
 
     boolean hasMessages();
 }
