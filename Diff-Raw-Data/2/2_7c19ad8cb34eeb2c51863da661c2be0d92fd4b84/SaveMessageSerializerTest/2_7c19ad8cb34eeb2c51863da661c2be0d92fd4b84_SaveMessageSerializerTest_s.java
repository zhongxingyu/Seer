 package gx.realtime.serialize;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import gx.browserchannel.message.SaveMessage;
 import gx.realtime.ValueChangedEvent;
 import org.junit.Test;
 
 import static junit.framework.Assert.assertEquals;
 
 /**
  *
  */
 public class SaveMessageSerializerTest
 {
     @Test
     public void testSerializeValueChangedEvent() throws JsonProcessingException
     {
         String expected = "{\"revision\":1,\"requestNumber\":2,\"changes\":[[4,[0,[8,\"id\",\"property\",[21,\"new\"]]]]]}";
         ValueChangedEvent event = new ValueChangedEvent("id", "sessid", "userid", false, "property", "new", "old");
 
        SaveMessage m = new SaveMessage(1, 2, event);
 
         ObjectMapper mapper = new ObjectMapper();
         String output = mapper.writeValueAsString(m);
 
         assertEquals(expected, output);
     }
 
 }
