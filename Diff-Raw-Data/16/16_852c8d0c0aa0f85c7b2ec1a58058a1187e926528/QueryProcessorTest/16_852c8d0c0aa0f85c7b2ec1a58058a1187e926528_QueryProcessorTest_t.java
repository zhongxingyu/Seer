 package com.develogical;
 
 import org.junit.Test;
 
 import static junit.framework.Assert.assertNull;
 import static org.junit.Assert.assertThat;
 import static org.junit.matchers.StringContains.containsString;
 
 public class QueryProcessorTest {
 
     @Test
     public void returnsNullIfCannotProcessQuery() throws Exception {
         assertNull(new QueryProcessor().process("test"));
     }
 
     @Test
    public void knowsAboutName() throws Exception {
        assertThat(new QueryProcessor().process("what is your name"), containsString("Falling Sunset"));
     }
 
 }
