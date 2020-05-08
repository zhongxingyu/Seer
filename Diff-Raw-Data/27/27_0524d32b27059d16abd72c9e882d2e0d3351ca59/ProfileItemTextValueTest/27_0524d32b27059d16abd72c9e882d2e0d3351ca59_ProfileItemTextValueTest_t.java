 package com.amee.domain.item.profile;
 
import com.amee.domain.data.ItemValueDefinition;
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
 
 public class ProfileItemTextValueTest {
 
     @Test
     public void getCopy() {
        ItemValueDefinition mockItemDef = mock(ItemValueDefinition.class);
        when(mockItemDef.isDouble()).thenReturn(false);
        when(mockItemDef.isInteger()).thenReturn(false);
        ProfileItemTextValue o = new ProfileItemTextValue(mockItemDef, new NuProfileItem());
         o.setValue("foo");
 
         ProfileItemTextValue c = (ProfileItemTextValue) o.getCopy();
        assertEquals("foo", c.getValue());
     }
 
 }
