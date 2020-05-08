 package org.jboss.pressgang.ccms.rest.v1.entities.join;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 
 import org.junit.Test;
 
 public class RESTAssignedPropertyTagV1Test
 {
     @Test
     public void testEqualsSame()
     {
         final RESTAssignedPropertyTagV1 item1 = createEntity(1, null, "Test", true);
         final RESTAssignedPropertyTagV1 item3 = createEntity(1, 1, "Test", true);        
        final RESTAssignedPropertyTagV1 item5 = createEntity(1, 1, "Different", true);
        final RESTAssignedPropertyTagV1 item6 = createEntity(2, null, "Test", true);
         
         // Duplicate Values
         final RESTAssignedPropertyTagV1 item2 = createEntity(1, null, "Test", true);
         final RESTAssignedPropertyTagV1 item4 = createEntity(1, 1, "Test", true);
         
         // Test Item 1
         assertEquals(item1, item2);
         assertFalse(item1.equals(item3));
         assertFalse(item1.equals(item5));
        assertFalse(item1.equals(item6));
         
         // Test Item 3 (Different Relationship ID)
         assertEquals(item3, item4);
         assertFalse(item3.equals(item1));
         assertFalse(item3.equals(item5));
        assertFalse(item3.equals(item6));
         
        // Test Item 5 (Different Value)
         assertFalse(item5.equals(item1));
         assertFalse(item5.equals(item3));
        assertFalse(item5.equals(item6));
        
        // Test Item 6 (Different Id)
        assertFalse(item6.equals(item1));
        assertFalse(item6.equals(item3));
        assertFalse(item6.equals(item5));
     }
     
     private RESTAssignedPropertyTagV1 createEntity(final Integer id, final Integer relationshipId, final String value, final boolean isUnique)
     {
         final RESTAssignedPropertyTagV1 entity = new RESTAssignedPropertyTagV1();
         entity.setId(id);
         entity.setValue(value);
         entity.setRelationshipId(relationshipId);
         entity.setIsUnique(isUnique);
         
         return entity;
     }
 }
