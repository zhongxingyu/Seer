 package de.fhb.polyencoder;
 
 import static org.junit.Assert.*;
 
 import java.util.HashMap;
 
 import org.junit.*;
 
 public class UtilTest {
 
   @Test
   public void testReplaceMarker() {
     String text = "Hi {name}";
     String marker = "name";
     String replace = "User";
 
     assertEquals("The new String must be \"Hi User\"", "Hi User", Util.replaceMarker(text, marker, replace));
   }
 
 
 
   @Test
   public void testReplaceMarkerHashMap() {
     String text = "Hi {name} {nachname}";
     HashMap<String, String> map = new HashMap<String, String>();
     map.put("name", "foo");
     map.put("nachname", "bar");
 
     assertEquals("The new String must be \"Hi foo bar\"", "Hi foo bar", Util.replaceMarker(text, map));
   }
 
 
 
   @Test
   public void testReadExistFile() {
     String content = Util.readFile("templates/html/maps.html");
     assertFalse("Should return no empty content", content.equals(""));
   }
 
 
 
   @Test
   public void testReadNotExistFile() {
     String content = Util.readFile("templates/html/mapssss.html");
     assertTrue("Should return empty content", content.equals(""));
   }
 
 
 
   @Test
   public void testReadFile() {
     String content = Util.readFile("testfiles/readFile.test");
    assertEquals("Should return right content", "hello world\n!\"$%&/\nok!", content);
   }
 
 
 
   @Test
   public void testCreateCenter() {
     assertTrue("Center must be 5", 5 == Util.createCenter(-10, 20));
   }
 
 
 
   @Test
   public void testCreateCenterMaxMinFlipped() {
     assertTrue("Center must be 5", 5 == Util.createCenter(20, -10));
   }
 }
