 package com.abudko.reseller.huuto.query.service.item.html.lekmer;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.IOException;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.abudko.reseller.huuto.query.service.item.ItemResponse;
 import com.abudko.reseller.huuto.query.service.item.ItemStatus;
 import com.abudko.reseller.huuto.query.service.item.html.HtmlItemParser;
 import com.abudko.reseller.huuto.query.service.list.HtmlParserTestUtils;
 
 public class LekmerHtmlItemParserTest {
     
     static {
         try {
             html = HtmlParserTestUtils.readHtmlFromFile("./src/test/resources/html/lekmer/lekmer-item.html");
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private static String html;
 
     private ItemResponse response;
 
     @Before
     public void setup() throws IOException {
         HtmlItemParser htmlParser = new LekmerHtmlItemParser();
         response = htmlParser.parse(html);
     }
 
     @Test
     public void testId() {
         assertEquals("510097-6982", response.getId());
     }
 
     @Test
     public void testPrice() {
        assertEquals("39.50", response.getPrice());
     }
     
     @Test
     public void testSizes() {
         assertEquals("80", response.getSizes().get(1));
     }
     
     @Test
     public void testImgSrc() {
         assertEquals("http://lekmer.fi/mediaarchive/1083385/productmanMeasurement465x500/reima-talvihaalari-tatum-raidallinen-tummansininen.jpg", response.getImgBaseSrc());
     }
 }
