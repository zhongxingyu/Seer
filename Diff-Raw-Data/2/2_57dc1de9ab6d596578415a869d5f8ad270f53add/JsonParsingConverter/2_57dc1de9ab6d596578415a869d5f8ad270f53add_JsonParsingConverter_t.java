 package de.innoaccel.wamp.server.converter;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.JsonNodeType;
 import de.innoaccel.wamp.server.Websocket;
 import de.innoaccel.wamp.server.message.EventMessage;
 import de.innoaccel.wamp.server.message.Message;
 
 import java.io.IOException;
 
 abstract public class JsonParsingConverter<T extends Message> implements Converter<T>
 {
     protected final ObjectMapper objectMapper;
 
     public JsonParsingConverter()
     {
         this(new ObjectMapper());
     }
 
     public JsonParsingConverter(ObjectMapper objectMapper)
     {
         this.objectMapper = objectMapper;
     }
 
     abstract protected T deserialize(JsonNode message, Websocket socket) throws MessageParseException, InvalidMessageCodeException;
 
     public T deserialize(String message, Websocket socket) throws MessageParseException, InvalidMessageCodeException
     {
         JsonNode rootNode;
         try {
             rootNode = this.objectMapper.readTree(message);
         } catch (IOException parseException) {
             // TODO: check when this actually can happen, build a test-case for this
             throw new MessageParseException(parseException.getMessage());
         }
 
         if (JsonNodeType.ARRAY != rootNode.getNodeType()) {
             throw new MessageParseException("Message is no JSON-Array");
         }
 
         return this.deserialize(rootNode, socket);
     }
 
     protected void assertMessageCode(JsonNode rootNode, int messageCode) throws MessageParseException, InvalidMessageCodeException
     {
         JsonNode messageCodeNode = rootNode.get(0);
         if (null == messageCodeNode || JsonNodeType.NUMBER != messageCodeNode.getNodeType() || !messageCodeNode.canConvertToInt()) {
             throw new MessageParseException("Message does not start with message code");
         }
 
         int actualMessageCode = messageCodeNode.asInt();
         if (actualMessageCode != messageCode) {
             throw new InvalidMessageCodeException(actualMessageCode);
         }
     }
 
     protected String readStringAt(JsonNode rootNode, int position) throws MessageParseException
     {
        JsonNode stringNode = rootNode.get(position);
         if (null == stringNode || JsonNodeType.STRING != stringNode.getNodeType()) {
             throw new MessageParseException("Expected string-type at position " + position);
         }
         return stringNode.asText();
     }
 
     protected Object readAnyTypeAt(JsonNode rootNode, int position) throws MessageParseException
     {
         JsonNode objectNode = rootNode.get(position);
         if (null == objectNode) {
             throw new MessageParseException("Expected object at position " + position);
         }
         return objectNode;
     }
 }
