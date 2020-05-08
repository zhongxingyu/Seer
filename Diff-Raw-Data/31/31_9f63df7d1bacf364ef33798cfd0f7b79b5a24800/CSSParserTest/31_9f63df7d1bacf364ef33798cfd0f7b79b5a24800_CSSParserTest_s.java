 package html2windows.css;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 
 import java.util.ArrayList;
 
 import static org.junit.Assert.*;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import html2windows.dom.Document;
 import html2windows.dom.Element;
 import html2windows.dom.NodeList;
 import html2windows.dom.Node;
 import html2windows.dom.UIParser;
 import html2windows.css.CSSParser;
 import html2windows.css.Style;
 
 /**
  * Test CSSParser
  * @author bee040811
  */
 public class CSSParserTest {
 
     @BeforeClass
     public static void setUpBeforeClass() throws Exception {
 
     }
 
     @AfterClass
     public static void tearDownAfterClass() throws Exception {
 
     }
 
     /**
      * Read CSS file and parse css  
      *
      * Expect CSS Parse result equal css file
      */
     @Test   
     public void CSSParserTest() throws Exception{
 
         /**
          * Setup
          * 
          * Create a file 
          * Create a cssString
          */
         String cssString = "";
         try {
             BufferedReader in = new BufferedReader(
                     new FileReader(
                         "./test-file/testFile.txt"));
             String line;
             while ((line = in.readLine()) != null) {
                 cssString += line;
                 cssString += "\n";
             }
         }
         catch (FileNotFoundException e) {
             e.printStackTrace();
         }
         catch (IOException e) {
             e.printStackTrace();
         }
 
         /**
          * Setup
          * 
          * Create a file 
          * Create a cssString
          */
        UIParser uiParserTest = new UIParser();
        String input = "<html><head></head><body>test message</body></html>";

         /**Get doc from UIParser result whether is not null 
          * 
          */
        Document doc = uiParserTest.parse(input);
        assertNotNull(doc);
 
         /**Setting doc Style from CSSParser
          * 
          */
         CSSParser CSSParser = new CSSParser();
         CSSParser.parser(cssString, doc); 

        /**Use doc to get documentElement and test that it is not null.
         * 
         */
        Element html = doc.documentElement();
        assertNotNull(html);
 
         /** Test whether the tag Name is correct
          * 
          */
         assertEquals("tagName of root element should be <html>", "html", html.tagName());
 
         /**Use html getStyle that it is not null
          * 
          */
         Style style = html.getStyle();
         assertNotNull(style);
 
         /** Test whether the background is black
          * 
          */
         assertEquals("background should be black", "black", style.getProperty("background"));
 
         /** Test whether the width is correct
          * 
          */
         assertEquals("width should be 100", "100px", style.getProperty("width"));
 
 
         /**Get the child list of documentElement and test whether the child count correct 
          * 
          */
         NodeList list = html.childNodes();
         assertEquals("list length should be one",list.length(),2);
 
         /**Get the first child and test whether the node name is correct.
          * 
          */
        Node head = list.item(0);
        assertEquals("first child of <html> should be <head>", "head", head.nodeName());
 
         /**Get the first child and test whether the node name is correct.
          * 
          */
         Node body = list.item(1);
         assertEquals("second child of <html> should be <body>", "body", body.nodeName());
 
         /**Get Style from element getStyle whether it is null or not
          * 
          */
         Element bodyElement = (Element) body;
         Style bodyStyle = bodyElement.getStyle();
         assertNotNull(bodyStyle);
 
         /** Test whether the margin-top is correct
          * 
          */
         assertEquals("margin-top should be 10px", "10px", bodyStyle.getProperty("margin-top"));
 
         /** Test whether the text-color is correct
          * 
          */
         assertEquals("text-color should be blue", "blue", bodyStyle.getProperty("text-color"));
     }
 
     /**
      * Test parsing string by UIParser
      * 
      */
     @Test
     public void testParseString() {
         /** Create a UIParser and string to test
          * 
          */
         UIParser uiParserTest = new UIParser();
         String input = "<html><head></head><body>test message</body></html>";
 
         /**Use UIParser to parse string and test test that it is not null.
          * 
          * 
          */
         Document doc = uiParserTest.parse(input);
         assertNotNull(doc);
 
         /**Use doc to get documentElement and test that it is not null.
          * 
          */
         Element html = doc.documentElement();
         assertNotNull(html);
 
         /** Test whether the tag Name is correct
          * 
          */
         assertEquals("tagName of root element should be <html>", "html", html.tagName());
 
         /**Get the child list of documentElement and test whether the child count correct 
          * 
          */
         NodeList list = html.childNodes();
         assertEquals("list length should be one",list.length(),2);
 
         /**Get the first child and test whether the node name is correct.
          * 
          */
         Node head = list.item(0);
         assertEquals("first child of <html> should be <head>", "head", head.nodeName());
 
         /**Get the second child and test whether the node name is correct.
          * 
          */
         Node body = list.item(1);
         assertEquals("second child of <html> should be <body>", "body", body.nodeName()); 
 
         /**Get the childlist of second component and test whether the count of list is correct.
          * 
          */
         NodeList bodyChildList = body.childNodes();
         assertEquals("list length should be one",bodyChildList.length(),1);
 
         /**Get the grandchild of second child and test whether the nodeType and node Value are correct. 
          * 
          */
         Node bodyChild = bodyChildList.item(0);
         assertEquals("type of first child of <body> should be TEXT_NODE", Node.TEXT_NODE, bodyChild.nodeType());
         assertEquals("value of first child of <employ> should be test message", "test message", bodyChild.nodeValue());
 
     }
 
 
 
 }
 
