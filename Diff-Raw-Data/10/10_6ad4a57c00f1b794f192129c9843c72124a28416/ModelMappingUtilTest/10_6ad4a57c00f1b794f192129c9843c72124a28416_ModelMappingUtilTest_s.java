 import static org.junit.Assert.assertEquals;
 
 import java.io.IOException;
 import java.util.List;
 
 import com.memtag.model.*;
 import org.junit.Test;
 
 import com.fasterxml.jackson.core.type.TypeReference;
 import com.memtag.model.util.ModelMappingUtil;
 
 /**
  * Unit test mode mapping. Json <-> Object
  */
 public class ModelMappingUtilTest {
 
     @Test
     public void convertMemoryObjToJson(){
 
         Memory memory = new Memory();
         memory.setId(1L);
         memory.setText("text goes here");
         memory.setType(MemoryType.PERSON);
         String json = null;
         try{
             json = ModelMappingUtil.asJSON(memory);
 
         } catch(Exception e){
             e.printStackTrace();
         }
         String expectedJson = "{\"id\":1,\"type\":\"PERSON\",\"text\":\"text goes here\",\"fragments\":[]}";
         assertEquals(expectedJson,json);
     }
 
     @Test
     public void convertMemtagUserToJson(){
         MemtagUser adminUser = new MemtagUser("admin");
         adminUser.setPassword("admin");
         MemtagRole memtagRole = new MemtagRole();
         memtagRole.setAuthority("ROLE_ADMIN");
         adminUser.getAuthorities().add(memtagRole);
         try {
             String jsonString = ModelMappingUtil.asJSON(adminUser);
            System.out.println(jsonString);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     @Test
     public void convertJsonToMemoryObj(){
         String json = "{\"id\":1,\"type\":\"PERSON\",\"text\":\"text goes here\",\"fragments\":[]}";
         Memory memory = null;
         try{
             memory = (Memory)ModelMappingUtil.asObject(json, Memory.class);
         } catch(Exception e ){
             e.printStackTrace();
         }
         assertEquals(memory.getId().longValue(), 1L);
         assertEquals(memory.getText(), "text goes here");
         assertEquals(memory.getType(), MemoryType.PERSON);
     }
 
     @Test
     public void convertJsonToMemtagUserObj(){
        String json = "{id:18, enabled:true, username:admin, friends:[], subscribedMemories:[], authorities:[], login:admin, accountNonExpired:true, credentialsNonExpired:true, ownedMemories:[], accountNonLocked:true}";
         MemtagUser memtagUser = null;
         try{
             memtagUser = (MemtagUser)ModelMappingUtil.asObject(json, MemtagUser.class);
         } catch(Exception e ){
             e.printStackTrace();
         }
        assertEquals(memtagUser.getId().longValue(), 18L);
     }
 
 
     @Test
     public void convertJsonToJsonMessage(){
         String json = "{\"status\":\"success\",\"payload\":{\"id\":18,\"type\":\"PERSON\",\"text\":\"testtext\",\"fragments\":[]}}";
         JSONMessage<Memory> jsonMessage = null;
         TypeReference<JSONMessage<Memory>> typeReference =  new TypeReference<JSONMessage<Memory>>() {};
         try{
             jsonMessage = (JSONMessage<Memory>)ModelMappingUtil.asJsonMessage(json, typeReference);
         } catch(Exception e){
             e.printStackTrace();
         }
 
         assertEquals(jsonMessage.getStatus(), "success");
         assertEquals(18L, jsonMessage.getPayload().getId().longValue());
         Memory memory = jsonMessage.getPayload();
         assertEquals(18L, memory.getId().longValue());
         assertEquals("testtext", memory.getText());
 
     }
 
     @Test
     public void convertJsonToJsonMessageWithMemtagUser(){
         String json = "{\"status\":\"success\",\"payload\":{\"id\":\"18\", \"enabled\":\"true\", \"username\":\"admin\",\"friends\":[], \"authorities\":[], \"subscribedMemories\":[], \"login\":\"admin\", \"accountNonExpired\":\"true\", \"credentialsNonExpired\":\"true\", \"ownedMemories\":[], \"accountNonLocked\":\"true\"}}";
 
         JSONMessage<MemtagUser> jsonMessage = null;
         TypeReference<JSONMessage<MemtagUser>> typeReference =  new TypeReference<JSONMessage<MemtagUser>>() {};
         try{
             jsonMessage = (JSONMessage<MemtagUser>)ModelMappingUtil.asJsonMessage(json, typeReference);
         } catch(Exception e){
             e.printStackTrace();
         }
 
         assertEquals(jsonMessage.getStatus(), "success");
        System.out.println(jsonMessage.getPayload().getId());
        System.out.println(jsonMessage.getPayload().getLogin());
         assertEquals(18L, jsonMessage.getPayload().getId().longValue());
         MemtagUser user = jsonMessage.getPayload();
         assertEquals(18L, user.getId().longValue());
 
     }
 
     @Test
     public void convertJsonToJsonMessageWithList(){
         String json = "{\"status\":\"success\",\"payload\":[{\"id\":18,\"type\":\"PERSON\",\"text\":\"testtext\",\"fragments\":[]},{\"id\":2,\"type\":\"PERSON\",\"text\":\"text to find\",\"fragments\":[]},{\"id\":12,\"type\":\"PERSON\",\"text\":\"text to find\",\"fragments\":[]}]}";
         JSONMessage<List<Memory>> jsonMessage = null;
         TypeReference<JSONMessage<List<Memory>>> typeReference = new TypeReference<JSONMessage<List<Memory>>>() {};
         try{
             jsonMessage = (JSONMessage<List<Memory>>)ModelMappingUtil.asJsonMessage(json, typeReference);
         } catch(Exception e){
             e.printStackTrace();
         }
 
         assertEquals(jsonMessage.getStatus(), "success");
         assertEquals(jsonMessage.getPayload().get(0).getId().longValue(), 18L);
 
     }
 }
