 package com.amee.domain.item.profile;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 
 public class ProfileItemTextValueTest {
 
     @Test
     public void getCopy() {
        ProfileItemTextValue o = new ProfileItemTextValue();
         o.setValue("foo");
 
         ProfileItemTextValue c = (ProfileItemTextValue) o.getCopy();
        assertEquals("foo", c.getValue());        
     }
 
 }
