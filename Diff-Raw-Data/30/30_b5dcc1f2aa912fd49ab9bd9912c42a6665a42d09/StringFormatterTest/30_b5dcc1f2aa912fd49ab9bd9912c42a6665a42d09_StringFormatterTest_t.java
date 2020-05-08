 package com.ctco.louchansky.stringformatter;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 
 public class StringFormatterTest {
 
     @Test
     public void testFormatNewlinesEmptyString() throws Exception {
         StringFormatter s = new StringFormatter("");
        assertEquals("", s.formatNewlines());
     }
 
     @Test
     public void testFormatNewlinesNothingToFormat() throws Exception {
         StringFormatter s = new StringFormatter("abcdef");
         assertEquals("abcdef", s.formatNewlines());
     }
 
     @Test
     public void testFormatNewlinesRemoveTwoNewlines() throws Exception {
         StringFormatter s = new StringFormatter("ab\\\ncd\\\nef");
         assertEquals("abcdef", s.formatNewlines());
     }
 
     @Test
     public void testFormatNewlinesRemoveTwoNearNewlines() throws Exception {
         StringFormatter s = new StringFormatter("abc\\\n\\\ndef");
         assertEquals("abcdef", s.formatNewlines());
     }
 
     @Test
     public void testFormatNewlinesRemoveOneNewline() throws Exception {
         StringFormatter s = new StringFormatter("abc\\\ndef");
         assertEquals("abcdef", s.formatNewlines());
     }
 
     @Test
     public void testFormatNewlinesWrongOrder() throws Exception {
         StringFormatter s = new StringFormatter("abc\\n\\\\def");
         assertEquals("abc\\n\\\\def", s.formatNewlines());
     }
 
     @Test
     public void testFormatNewlinesNoNCharacter() throws Exception {
         StringFormatter s = new StringFormatter("abc\\\\def");
         assertEquals("abc\\\\def", s.formatNewlines());
     }
 
     @Test
     public void testFormatNewlinesWrongBackslashCount() throws Exception {
         StringFormatter s = new StringFormatter("abc\\ndef");
         assertEquals("abc\\ndef", s.formatNewlines());
     }
 
     @Test
     public void testFormatNewlinesRightAndWrongNotNear() throws Exception {
         StringFormatter s = new StringFormatter("abc\\\nd\\nef");
         assertEquals("abcd\\nef", s.formatNewlines());
     }
 
     @Test
     public void testReverseString() throws Exception {
         StringFormatter s = new StringFormatter("abc\\ndef");
         s.reverse();
         assertEquals("fedn\\cba", s.getString());
     }
 
     @Test
     public void testFormatNewlinesRightAndWrongNear() throws Exception {
         StringFormatter s = new StringFormatter("abc\\\n\\\n\\ef");
         assertEquals("abc\\ef", s.formatNewlines());
     }
 
     @Test
     public void testFormatNewlinesTwoNewlinesNear() throws Exception {
         StringFormatter s = new StringFormatter("abc\\\n\nef");
         assertEquals("abc\nef", s.formatNewlines());
     }
 
     @Test
     public void testFormatNewlinesAllBackslashes() throws Exception {
         StringFormatter s = new StringFormatter("\\\\\\");
         assertEquals("\\\\\\", s.formatNewlines());
     }
 
     @Test
     public void testFormatNewlinesAllNewlines() throws Exception {
         StringFormatter s = new StringFormatter("\n\n");
         assertEquals("\n\n", s.formatNewlines());
     }
 
     @Test
     public void testFormatNewlinesOnlyBackslashNewline() throws Exception {
         StringFormatter s = new StringFormatter("\\\n\\\n");
         assertEquals("", s.formatNewlines());
     }
 
 }
