 package gx.realtime.serialize;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import gx.realtime.ValueChangedEvent;
 import org.junit.Test;
 
 import static junit.framework.Assert.assertEquals;
 
 /**
  *
  */
 public class ValueChangedEventSerializerTest
 {
     @Test
     public void testSerializeValueChangedEvent() throws JsonProcessingException
     {
        String expected = "[4,[0,[8,\"id\",\"property\",[21,\"new\"]]]]";
        ValueChangedEvent event = new ValueChangedEvent("id", "sessid", "userid", false, "property", "new", "old");
         ObjectMapper mapper = new ObjectMapper();
         String output = mapper.writeValueAsString(event);
 
         assertEquals(expected, output);
     }
 
 }
