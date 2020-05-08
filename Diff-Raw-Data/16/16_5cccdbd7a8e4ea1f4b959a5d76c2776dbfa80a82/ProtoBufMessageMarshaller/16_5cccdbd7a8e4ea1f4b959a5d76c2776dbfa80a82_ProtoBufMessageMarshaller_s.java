package org.jdryad.com.messages;
 
 import com.google.protobuf.ByteString;
 import com.google.protobuf.InvalidProtocolBufferException;
 
 import org.jdryad.commmunicator.Message;
 import org.jdryad.commmunicator.MessageMarshaller;
 
 /**
  * A simple message marshaller that translates the messages sent to/from
  * using google's protocol buffers.
  *
  * @author Balraja Subbiah
  * @version $Id:$
  */
 public class ProtoBufMessageMarshaller implements MessageMarshaller
 {
     /**
      * {@inheritDoc}
      */
     @Override
     public byte[] marshal(Message message)
     {
         SimpleMessage wireMessage = (SimpleMessage) message;
         JDAGMessageType type = (JDAGMessageType) message.getMessageType();
         ByteString wiredMessagePayload = null;
         switch (type)  {
         case UP_AND_ALIVE_MESSAGE:
            wiredMessagePayload =
                 ((UpAndLiveProtos.UpAndAliveMessage)
                     wireMessage.getPayload()).toByteString();
            break;
 
         case EXECUTE_VERTEX_MESSAGE:
             wiredMessagePayload =
                 ((ExecuteVertexProtos.ExecuteVertexMessage)
                     wireMessage.getPayload()).toByteString();
             break;
         case EXECUTE_VERTEX_STATUS_MESSAGE:
             wiredMessagePayload =
                 ((ExecuteVertexStatusProtos.ExecuteVertexStatusMessage)
                      wireMessage.getPayload()).toByteString();
             break;
         }
         WiredMessageProtos.WiredMessage wiredMessage =
             WiredMessageProtos.WiredMessage
                               .newBuilder()
                               .setMessageType(type.asInteger())
                               .setPayload(wiredMessagePayload)
                               .build();
         return wiredMessage.toByteArray();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Message unmarshal(byte[] binaryMsg)
     {
         try {
             WiredMessageProtos.WiredMessage messageOnWire =
                 WiredMessageProtos.WiredMessage.parseFrom(binaryMsg);
             JDAGMessageType type =
                 JDAGMessageType.fromInteger(messageOnWire.getMessageType());
             Object payload = null;
             switch (type)  {
             case UP_AND_ALIVE_MESSAGE:
                 payload =
                     UpAndLiveProtos.UpAndAliveMessage.parseFrom(
                         messageOnWire.getPayload());
                 break;
             case EXECUTE_VERTEX_MESSAGE:
                 payload =
                     ExecuteVertexProtos.ExecuteVertexMessage.parseFrom(
                         messageOnWire.getPayload());
                 break;
             case EXECUTE_VERTEX_STATUS_MESSAGE:
                 payload =
                     ExecuteVertexStatusProtos.ExecuteVertexStatusMessage
                                              .parseFrom(messageOnWire.getPayload());
                 break;
             }
             return new SimpleMessage(type, payload);
         }
         catch (InvalidProtocolBufferException e) {
              throw new RuntimeException(e);
         }
     }
 }
