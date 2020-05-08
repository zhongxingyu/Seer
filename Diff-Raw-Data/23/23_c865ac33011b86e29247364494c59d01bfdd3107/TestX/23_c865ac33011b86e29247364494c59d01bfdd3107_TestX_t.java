 // Distributed under Apache License 2.0 http://www.apache.org/licenses/LICENSE-2.0
 // Copyright Gerard Davison 2011
 package com.kingsfleet.simple.x;
 
 import java.io.StringReader;
 import java.io.StringWriter;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class TestX {
     @Test
     public void simpleRead() {
 
         X x = getSimpleTestData();
 
         String elementValue = x.get("n:elementValue");
         assertEquals("ElementValue", elementValue);
         String attributeValue = x.get("n:attributeValue/@attr");
         assertEquals("AttributeValue", attributeValue);
 
         x.set("n:elementValue", "Fish");
         x.set("n:attributeValue/@attr", "Fish");
 
         elementValue = x.get("n:elementValue");
         assertEquals("Fish", elementValue);
         attributeValue = x.get("n:attributeValue/@attr");
         assertEquals("Fish", attributeValue);
 
     }
 
     @Test
     public void simpleAddChildElement() {
         X x = getSimpleTestData();
         X child = x.children().create("textst");
 
         assertEquals(
             "Added path",
             "textst<-simple",
             child.toString());
         
         assertTrue(
             "dumped xml should contain the new element",
             dumpToString(x).contains("textst"));
         
         assertNotNull(
             "Should be able to find new node in the children",
             x.select("n:textst"));
     }
 
 
     @Test
     public void setAttributeThatIsntThere() {
         X x = getSimpleTestData();
 
         x.set("@newAttribute", "NewAttribute");
         
 
         assertEquals(
             "Added path",
             "NewAttribute",
             x.get("@newAttribute"));
         
         assertTrue(
             "dumped xml should contain the new element",
             dumpToString(x).contains("newAttribute=\""));
     }
 
 
     @Test
     public void testWritingOut() {
     
         X x = getSimpleTestData();
 
         String dumpToString = dumpToString(x);
         assertTrue(
             "dumped xml should contain the root element", 
             dumpToString.contains("simple"));
 
     }
 
 
    @Test
    public void testCreateNew() {
    
        X x = X.in("http://www.example.com", "localname");
        x.attributes().create("attribute").set("value");

        String dumpToString = dumpToString(x);
        assertTrue(
            "dumped xml should contain the root element", 
            dumpToString.contains("localname"));
        assertTrue(
            "dumped xml should contain the namespace", 
            dumpToString.contains("http://www.example.com"));

        assertTrue(
            "dumped xml should contain the attribute", 
            dumpToString.contains("attribute"));
        assertTrue(
            "dumped xml should contain the value", 
            dumpToString.contains("value"));

    }

 
 
 
 
 
 
     /**
      * @return dump x to a string
      */
     private String dumpToString(X x) {
         StringWriter sw = new StringWriter();
         x.out(sw);
         return sw.toString();
     }
 
     /**
      * @return An X with very simple test data in it
      */
     private X getSimpleTestData() {
         
         StringReader sr = new StringReader("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" + 
         "<simple xmlns=\"http://example.com\">\n" + 
         "\n" + 
         "  <elementValue>ElementValue</elementValue>\n" + 
         "  <attributeValue attr=\"AttributeValue\" />\n" + 
         "\n" + 
         "</simple>");
         
         X x = X.in(sr);
         return x;
     }
 }
