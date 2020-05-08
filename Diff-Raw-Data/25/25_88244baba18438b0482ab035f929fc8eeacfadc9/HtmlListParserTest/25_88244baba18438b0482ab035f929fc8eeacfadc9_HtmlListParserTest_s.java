 package com.abudko.reseller.huuto.query.html.list;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.abudko.reseller.huuto.query.html.HtmlParserTestUtils;
 
 /**
  * Tests for {@link HtmlListParser}.
  * 
  * @author Alexei
  * 
  */
 public class HtmlListParserTest {
 
     private static HtmlListParser htmlParser = new HtmlListParser();
     private static String html;
     private static Collection<ListResponse> responses;
 
     static {
         try {
             html = HtmlParserTestUtils.readHtmlFromFile("./src/test/resources/html/test-resultlist.html");
             responses = htmlParser.parse(html);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private ListResponse queryResponse;
 
     @Before
     public void setup() throws IOException {
         queryResponse = responses.iterator().next();
     }
 
     @Test
     public void testNoSearchResults() {
         String htmlNoSearchResults = null;
         try {
             htmlNoSearchResults = HtmlParserTestUtils
                     .readHtmlFromFile("./src/test/resources/html/no-search-results.html");
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         Collection<ListResponse> responseList = htmlParser.parse(htmlNoSearchResults);
 
         assertTrue(responseList.isEmpty());
     }
 
     @Test
     public void testResponseSize() {
         assertEquals(33, responses.size());
     }
 
     @Test
     public void testParseDescription() {
         assertTrue(queryResponse.getDescription().length() > 0);
     }
 
     @Test
     public void testParseItemUrl() {
         assertFalse("Url: " + queryResponse.getItemUrl(),
                 queryResponse.getItemUrl().contains("http://www.huuto.net/kohteet"));
         assertTrue("Url: " + queryResponse.getItemUrl(), queryResponse.getItemUrl().contains("/"));
     }
 
     @Test
     public void testParseBids() {
        assertTrue("Bids: " + queryResponse.getBids(), queryResponse.getBids().length() > 0);
     }
 
     @Test
     public void testParseLast() {
         assertTrue("Last: " + queryResponse.getLast(), queryResponse.getLast().length() > 0);
     }
 
     @Test
     public void testParseCurrentPrice() {
         String currentPrice = queryResponse.getCurrentPrice();
         try {
             Double.parseDouble(currentPrice);
         } catch (Exception e) {
             fail("Unable to parse current price " + currentPrice);
         }
     }
 
     @Test
     public void testParseFullPrice() {
         ListResponse fullPriceQueryResponse = new ArrayList<ListResponse>(responses).get(5);
         String fullPrice = fullPriceQueryResponse.getFullPrice();
         try {
             Double.parseDouble(fullPrice);
         } catch (Exception e) {
             fail("Unable to parse full price " + fullPrice);
         }
     }
 
     @Test
     public void testParseFullPriceBiggerThanCurrentPrice() {
         ListResponse fullPriceQueryResponse = new ArrayList<ListResponse>(responses).get(5);
         String currentPrice = fullPriceQueryResponse.getCurrentPrice();
         String fullPrice = fullPriceQueryResponse.getFullPrice();
 
         double current = 0;
         try {
             current = Double.parseDouble(currentPrice);
         } catch (Exception e) {
             fail("Unable to parse current price " + fullPrice);
         }
 
         double full = 0;
         try {
             full = Double.parseDouble(fullPrice);
         } catch (Exception e) {
             fail("Unable to parse full price " + fullPrice);
         }
 
         assertTrue(full > current);
     }
 
     @Test
     public void testParseFullPriceNoComma() {
         ListResponse fullPriceQueryResponse = new ArrayList<ListResponse>(responses).get(5);
         String fullPrice = fullPriceQueryResponse.getFullPrice();
 
         assertFalse(fullPrice.contains(","));
     }
 
     @Test
     public void testParseImgSrc() {
         ListResponse imgSrcResponse = new ArrayList<ListResponse>(responses).get(5);
         assertTrue("Src: " + imgSrcResponse.getImgBaseSrc(),
                 imgSrcResponse.getImgBaseSrc().contains("http://kuvat2.huuto.net/"));
     }
 
     @Test
     public void testParseSize() {
         String size = queryResponse.getSize();
         try {
             Double.parseDouble(size);
         } catch (Exception e) {
             fail("Unable to parse size " + size);
         }
     }
 
     @Test
     public void testParseBrand() {
         assertNotNull("Brand: " + queryResponse.getBrand(), queryResponse.getBrand());
     }
 }
