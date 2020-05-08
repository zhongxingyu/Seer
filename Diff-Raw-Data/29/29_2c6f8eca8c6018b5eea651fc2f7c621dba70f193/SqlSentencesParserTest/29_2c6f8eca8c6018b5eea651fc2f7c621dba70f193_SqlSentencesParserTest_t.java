 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.tools;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 
 public class SqlSentencesParserTest {
 
   @Test
   public void testReadSentence() {
     SqlSentencesParser parser = new SqlSentencesParser(
         "src/test/resources/com/globant/katari/tools/sqlSentences.sql");
 
     String sentence;
    
     sentence = parser.readSentence();
     assertThat(sentence, is("s1"));
 
     sentence = parser.readSentence();
     assertThat(sentence, is("s1\ns2"));
 
     sentence = parser.readSentence();
     assertThat(sentence, is("s1\ns2; \n\na"));
 
     sentence = parser.readSentence();
     assertThat(sentence, is("a\n\n"));
 
     sentence = parser.readSentence();
     assertThat(sentence, is(nullValue()));
   }
 }
 
