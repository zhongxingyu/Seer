 package com.develogical;
 
 import org.junit.Test;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.matchers.StringContains.containsString;
 
 public class QueryProcessorTest {
 
     QueryProcessor queryProcessor = new QueryProcessor();
 
     @Test
     public void returnsEmptyStringIfCannotProcessQuery() throws Exception {
         assertThat(queryProcessor.process("test"), is(""));
     }
 
     @Test
     public void knowsAboutProgramming() throws Exception {
         assertThat(queryProcessor.process("programming"), containsString("computing"));
     }
 
     @Test
     public void knowsAboutCakes() throws Exception {
         assertThat(queryProcessor.process("cakes").toLowerCase(), containsString("battenberg"));
     }
 
     @Test
     public void knowsAboutCheese() throws Exception {
         assertThat(queryProcessor.process("cheese").toLowerCase(), containsString("milk"));
     }
 
     @Test
     public void knowsAboutChocolate() throws Exception {
        assertThat(queryProcessor.process("chocolate").toLowerCase(), containsString("chocolate"));
     }
 }
