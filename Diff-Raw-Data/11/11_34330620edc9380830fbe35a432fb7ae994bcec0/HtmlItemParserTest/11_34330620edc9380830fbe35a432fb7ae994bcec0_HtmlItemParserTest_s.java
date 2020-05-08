 package com.abudko.reseller.huuto.query.html.item;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.abudko.reseller.huuto.query.html.HtmlParserTestUtils;
 
 /**
  * Tests for {@link HtmlItemParser}.
  * 
  * @author Alexei
  * 
  */
 public class HtmlItemParserTest {
 
     static {
         try {
             html = HtmlParserTestUtils.readHtmlFromFile("./src/test/resources/html/test-item.html");
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private static String html;
 
     private ItemResponse response;
 
     @Before
     public void setup() throws IOException {
         HtmlItemParser htmlParser = new HtmlItemParser();
         response = htmlParser.parse(html);
     }
 
     @Test
     public void testParseHv() {
         assertFalse(response.isHv());
     }
 
     @Test
     public void testParseCondition() {
        assertEquals("Uusi", response.getCondition());
     }
 
     @Test
     public void testParseLocation() {
        assertEquals("00940 Helsinki", response.getLocation());
     }
 
     @Test
     public void testParsePrice() {
        assertEquals("110.00", response.getPrice());
     }
 
     @Test
     public void testParseImgBaseSrc() {
         assertTrue("Src: " + response.getImgBaseSrc(), response.getImgBaseSrc().contains("http://"));
     }
 
     @Test
     public void testParseImgBaseSrcWithoutSuffix() {
         assertFalse("Src: " + response.getImgBaseSrc(), response.getImgBaseSrc().contains(".jpg"));
     }
     
     @Test
     public void testParseSeller() {
        assertEquals("marsuxx", response.getSeller());
     }
 }
