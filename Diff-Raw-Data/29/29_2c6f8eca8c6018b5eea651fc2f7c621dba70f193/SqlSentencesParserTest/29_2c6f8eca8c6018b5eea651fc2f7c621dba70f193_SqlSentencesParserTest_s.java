 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.tools;
 
import java.sql.Connection;
import java.util.Properties;

import org.springframework.beans.DirectFieldAccessor;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.hibernate.cfg.Configuration;

import static org.easymock.classextension.EasyMock.*;

 import org.junit.Test;
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 
import com.globant.katari.tools.database.MySqlDropAllObjects;

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
 
