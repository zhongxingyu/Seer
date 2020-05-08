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
    public void knowsAboutSpa() throws Exception {
        assertThat(new QueryProcessor().process("SPA2012"), containsString("conference"));
     }
 
    @Test
    public void knowsAboutMe() throws Exception {
        assertThat(new QueryProcessor().process("Robert"), containsString("presenting"));
    }
 }
