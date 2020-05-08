package org.jdryad.communicator.messages;
 
 import org.jdryad.commmunicator.Message;
 import org.jdryad.commmunicator.MessageType;
 
 /**
  * A simple implementation of <code>Message</code> that wraps the underlying
  * payload object sent over wire.
  *
  * @author Balraja Subbiah
  * @version $Id:$
  */
 public class SimpleMessage implements Message
 {
     private final MessageType myType;
 
     private final Object myPayload;
 
     /**
      * CTOR
      */
     public SimpleMessage(MessageType type, Object payload)
     {
         myType = type;
         myPayload = payload;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public MessageType getMessageType()
     {
         return myType;
     }
 
     /**
      * Returns the value of payload
      */
     public Object getPayload()
     {
         return myPayload;
     }
 }
