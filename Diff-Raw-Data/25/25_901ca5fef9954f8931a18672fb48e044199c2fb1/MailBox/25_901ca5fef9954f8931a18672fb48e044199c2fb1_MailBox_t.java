 package org.gethydrated.hydra.actors.mailbox;
 
 public interface Mailbox {
 
     Message poll();
 
    Message pollSystem();

     void offer(Message m);
 
    void offerSystem(Message m);

     boolean hasMessages();

    boolean hasSystemMessages();

    boolean isScheduled();

    void setScheduled(boolean state);

    boolean isSuspended();

    void setSuspended(boolean state);

    void close();

    boolean isClosed();
 }
