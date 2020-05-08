 package fi.helsinki.biblex.domain;
 
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 
 public class BibTexEntryTest {
 
     private BibTexEntry e;
 
     @Before
     public void setUp() {
         e = new BibTexEntry("king", BibTexStyle.ARTICLE);
         e.put("author", "Stephen King");
         e.put("title", "On Fear Distribution");
     }
 
     @Test
     public void testPutGetToArticle() {
         assertEquals(e.get("author"), "Stephen King");
         assertEquals(e.get("title"), "On Fear Distribution");
     }
 
     @Test
     public void testRemoveAndContains() {
         assertTrue(e.containsField("author"));
 
         String removedValue = e.remove("author");
         assertEquals(removedValue, "Stephen King");
         assertFalse(e.containsField("author"));
 
         removedValue = e.remove("author");
         assertNull(removedValue);
     }
 
     @Test
     public void testStyleGetAndNameGet() {
         System.out.println(e.getStyle());
         assertEquals(e.getStyle(), BibTexStyle.ARTICLE);
         assertEquals(e.getName(), "king");
     }
 
     @Test
     public void testToString() {
         System.out.println(e);
         String target = "@article{ king,\n"
                      + "          author = \"Stephen King\",\n"
                      + "          title = \"On Fear Distribution\" }";
         assertEquals(e.toString(), target);
     }
 }
