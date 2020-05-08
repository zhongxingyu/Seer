 package edu.uw.zookeeper.orchestra.peer.protocol;
 
 import com.fasterxml.jackson.annotation.JsonCreator;
 import com.fasterxml.jackson.annotation.JsonProperty;
 import edu.uw.zookeeper.orchestra.Identifier;
 
 @MessageBodyType(type=MessageType.MESSAGE_TYPE_HANDSHAKE)
 public class MessageHandshake extends IdentifierMessage<Identifier> {
 
     public static MessageHandshake of(Identifier id) {
         return new MessageHandshake(id);
     }
 
     @JsonCreator
    public MessageHandshake(@JsonProperty("id") Identifier id) {
         super(id);
     }
 }
