 package test.wikibase.dataModel;
 
 import org.junit.Test;
 import wikibase.dataModel.EntityId;
 
import static org.testng.Assert.assertEquals;

 public class EntityIdTest {
     @Test
     public void assertCanConstruct() throws Exception {
         String entityType = "item";
         String serialization = "q42";
 
         EntityId id = new EntityId( entityType, serialization );
 
        assertEquals(id.getEntityType(), entityType);
        assertEquals(id.getSerialization(), serialization);
     }
 }
