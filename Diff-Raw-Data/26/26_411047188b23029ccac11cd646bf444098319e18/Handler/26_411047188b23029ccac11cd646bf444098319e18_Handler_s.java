 package com.virtualdisk.coordinator.handler;
 
 import com.virtualdisk.coordinator.*;
 import com.virtualdisk.network.request.*;
 import com.virtualdisk.network.request.base.*;
 import com.virtualdisk.network.util.*;
 
 /**
  * The Handler class is used to handle all kinds of network/coordinator requests. It must be subclassed to be used.
  */
 public abstract class Handler extends Thread
 {
     protected int requestId;
     protected int volumeId;
     protected long logicalOffset;
     protected Coordinator coordinator;
     protected Sendable requestResult;
 
     /**
      * Setter for the request's id. This must be done.
      */
     public void setRequestId(int id)
     {
         requestId = id;
     }
 
     /**
      * Getter for the request's id.
      */
     public int getRequestId()
     {
         return requestId;
     }
 
     /**
      * Sets off the action, then passes along the result to the client.
      */
    public void run()
     {
         action();
 
         // TODO : change this line... it's kind of gross.
         SingletonCoordinator.sendToClient(requestId, requestResult);
         /*
 
         change to something like this:
             coordinator.getServer().set
 
         */
     }
 
     public abstract void action();
 }
