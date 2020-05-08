 package com.vonhof.babelshark.language;
 
 import com.vonhof.babelshark.*;
 import com.vonhof.babelshark.node.ArrayNode;
 import com.vonhof.babelshark.node.ObjectNode;
 import com.vonhof.babelshark.node.SharkNode;
 import com.vonhof.babelshark.node.ValueNode;
 import java.io.IOException;
 import java.util.Iterator;
 import org.codehaus.jackson.*;
 import org.codehaus.jackson.map.ObjectMapper;
 import sun.misc.IOUtils;
 
 /**
  *
  * @author Henrik Hofmeister <@vonhofdk>
  */
 public class JsonLanguage extends SharkLanguageBase {
     private final JsonFactory jsonFactory = new JsonFactory(); 
     private final ObjectMapper om = new ObjectMapper(jsonFactory);
     private final Reader reader = new Reader();
     private final Writer writer = new Writer();
 
     public JsonLanguage() {
        super("json", new String[]{"application/json","text/json"});
     }
     
     public ObjectReader getObjectReader() {
         return reader;
     }
 
     public ObjectWriter getObjectWriter() {
         return writer;
     }
     
     public class Reader implements ObjectReader {
 
         public String[] getContentTypes() {
             return JsonLanguage.this.getContentTypes();
         }
 
         public SharkNode read(Input input) throws IOException {
             JsonParser parser = jsonFactory.createJsonParser(input.getStream());
             parser.setCodec(om);
             try {
                 JsonNode node = parser.readValueAsTree();
                 return jsonToShark(node);
             } catch (Throwable ex) {
                 byte[] body = IOUtils.readFully(input.getStream(),0, true);
                 throw new IOException(new String(body),ex);
             }
         }
         private SharkNode jsonToShark(JsonNode node) {
             if (node == null || node.isNull())
                 return new ValueNode<Object>(null);
             
             if (node.isBigDecimal())
                 return new ValueNode(node.getDecimalValue());
             if (node.isBigInteger())
                 return new ValueNode(node.getBigIntegerValue());
             if (node.isBoolean())
                 return new ValueNode(node.getBooleanValue());
             if (node.isDouble())
                 return new ValueNode(node.getDoubleValue());
             if (node.isInt())
                 return new ValueNode(node.getIntValue());
             if (node.isLong())
                 return new ValueNode(node.getLongValue());
             
             if (node.isTextual())
                 return new ValueNode(node.getTextValue());
             
             if (node.isArray()) {
                 ArrayNode out = new ArrayNode();
                 for(JsonNode child:node) {
                     out.add(jsonToShark(child));
                 }
                 return out;
             }
             
             if (node.isObject()) {
                 ObjectNode out = new ObjectNode();
                 Iterator<String> fields = node.getFieldNames();
                 while(fields.hasNext()) {
                     String name = fields.next();
                     JsonNode entry = node.get(name);
                     out.put(name,jsonToShark(entry));
                 }
                 return out;
             }
             throw new UnknownError(String.format("Unkown node type: %s",node));
         }
         
     }
     
     public class Writer implements ObjectWriter {
 
         public String getContentType() {
             return JsonLanguage.this.getContentTypes()[0];
         }
 
         public void write(Output output, SharkNode node) throws IOException {
             JsonGenerator g = jsonFactory.createJsonGenerator(output.getStream(),JsonEncoding.UTF8);
             writeNode(g, node);
             g.close();
         }
         private void writeNode(JsonGenerator g,SharkNode node) throws IOException {
             if (node == null) {
                 g.writeNull();
                 return;
             }
         
             if (node instanceof ValueNode) {
                 Object value = ((ValueNode)node).getValue();
                 if (value instanceof Enum)
                     value = ((Enum)value).name();
                 g.writeObject(value);
                 return;
             }
             if (node instanceof ArrayNode) {
                 ArrayNode array = (ArrayNode) node;
                 g.writeStartArray();
                 for(SharkNode child:array) {
                     writeNode(g, child);
                 }
                 g.writeEndArray();
                 return;
             }
             if (node instanceof ObjectNode) {
                 ObjectNode object = (ObjectNode) node;
                 g.writeStartObject();
                 for(String field:object.getFields()) {
                     g.writeFieldName(field);
                     writeNode(g, object.get(field));
                 }
                 g.writeEndObject();
                 return;
             }
             
             throw new UnknownError(String.format("Unkown node type: %s",node));
         }
     }
 
 }
