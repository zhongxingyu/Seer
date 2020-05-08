 package org.snippr.business.entities;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 public class SnippetCodeTest {
 
     @Test
     public void testToString() {
         String title = "Code test";
         String description = "This is only a test description for the class Snippet";
 
         Snippet testSnippet = new Snippet(title, description);
 
         String code = "10 PRINT \"Hi there\"\n20 GOTO 10\n";
 
         SnippetCode snippetCode = new SnippetCode(code, testSnippet);
         String result = snippetCode.toString();
        assertEquals("Testing SnippetCode instance ...", "SnippetCode [code="
                + code
                 + ", snippet=" + testSnippet.toString() + "]", result);
     }
 
 }
