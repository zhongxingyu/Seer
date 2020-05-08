 package test.wikibase.dataModel;
 
 import org.junit.Test;
import org.testng.Assert;
 import wikibase.dataModel.EntityId;
 
 public class EntityIdTest {
     @Test
     public void assertCanConstruct() throws Exception {
         String entityType = "item";
         String serialization = "q42";
 
         EntityId id = new EntityId( entityType, serialization );
 
        Assert.assertEquals(id.getEntityType(), entityType);
        Assert.assertEquals(id.getSerialization(), serialization);
     }




 }
