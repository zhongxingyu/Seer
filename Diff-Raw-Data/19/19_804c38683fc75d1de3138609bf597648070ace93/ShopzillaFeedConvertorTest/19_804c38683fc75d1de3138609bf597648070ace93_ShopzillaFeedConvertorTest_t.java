 package com.shopzilla.feeds;
 
 import org.testng.annotations.Test;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.*;
 import java.util.StringTokenizer;
 
 import static org.testng.Assert.assertEquals;
 
 /**
  * Created by IntelliJ IDEA.
  * User: shitalm
  * Date: 1/27/12
  * Time: 9:45 AM
  * To change this template use File | Settings | File Templates.
  */
 @Test
 public class ShopzillaFeedConvertorTest {
 
     @Test(enabled = true)
     public void testOfferConversion() throws Exception {
 //        InputStream in = ClassLoader.getSystemResourceAsStream("shopzilla.xml");
         ShopzillaFeedConvertor convertor = new ShopzillaFeedConvertor();
         StringBuffer buffer = new StringBuffer(10000);
         OutputStream out =  new ByteArrayOutputStream(10000);
 
         convertor.convert("file://offers.xml", out);
         String output = out.toString();
         StringTokenizer tokenizer = new StringTokenizer(output, "\r\n");
         assertEquals(tokenizer.countTokens(), 4);   // header and 3 offers
         StringTokenizer header = new StringTokenizer(tokenizer.nextToken(), ",");
         //System.out.println("header = " + header);
         //System.out.flush();
         assertEquals(header.countTokens(), 6);     // 6 mandatory columns
         // Check few header column names
         for(int i=1; header.hasMoreTokens(); i++) {
             String headerColumn = header.nextToken();
             if(i == 2) assertEquals(headerColumn, "title");
             if(i == 4) assertEquals(headerColumn, "description");
             if(i == 5) assertEquals(headerColumn, "url");
             if(i == 6) assertEquals(headerColumn, "image60x60");
             if(i == 1) assertEquals(headerColumn, "productId");
             if(i == 3) assertEquals(headerColumn, "price");
 
             
         }
         // check few columns of first offer
         String firstOfferStr = tokenizer.nextToken();
         //System.out.println("First offer string = " + firstOfferStr);
         System.out.flush();
         StringTokenizer firstOffer = new StringTokenizer(firstOfferStr, ",");
         for(int i=1; firstOffer.hasMoreTokens(); i++) {
             String element = firstOffer.nextToken();
             //System.out.println("i="+ i + " token=" + element);
             System.out.flush();
             if(i == 1) assertEquals(element, "4003042957");
             if(i == 2) assertEquals(element, "Acer Aspire Ethos AS8951G-9630");
             // price has comma in the value so gets tokenized into two token in our simple parsing
             if(i == 3) assertEquals(element, "\"$1");
             if(i == 4) assertEquals(element, "449.00\"");
             if(i == 5) assertEquals(element, "Acer Aspire Ethos AS8951G-9630");
             if(i == 6) assertEquals(element, "http://www.bizrate.com/oid4003042957/search/retarget/");
 
             // Following URL string is  not exactly same as in input xml
             // input xml has "&amp;" while following URLs have "&"
             // this is because "&" got encoded in xml as "&amp;"
             if(i == 7) assertEquals(element, "http://image11.bizrate-images.com/resize?sq=60&uid=4009717441&mid=24106");
         }
         
     }
 
     @Test(enabled = true)
     public void testProductConversion() throws Exception {
 //        InputStream in = ClassLoader.getSystemResourceAsStream("shopzilla.xml");
         ShopzillaFeedConvertor convertor = new ShopzillaFeedConvertor();
         StringBuffer buffer = new StringBuffer(10000);
         OutputStream out =  new ByteArrayOutputStream(10000);
 
         convertor.convert("file://products.xml", out);
         String output = out.toString();
         StringTokenizer tokenizer = new StringTokenizer(output, "\r\n");
         assertEquals(tokenizer.countTokens(), 3);   // header and 2 products
         StringTokenizer header = new StringTokenizer(tokenizer.nextToken(), ",");
         System.out.println("header = " + header);
         System.out.flush();
         assertEquals(header.countTokens(), 6);     // 6 mandatory columns
         // Check few header column names
         for(int i=1; header.hasMoreTokens(); i++) {
             String headerColumn = header.nextToken();
             if(i == 2) assertEquals(headerColumn, "title");
             if(i == 4) assertEquals(headerColumn, "description");
             if(i == 5) assertEquals(headerColumn, "url");
             if(i == 6) assertEquals(headerColumn, "image60x60");
             if(i == 1) assertEquals(headerColumn, "productId");
             if(i == 3) assertEquals(headerColumn, "price");
 
 
         }
         // check few columns of first offer
         String firstOfferStr = tokenizer.nextToken();
         //System.out.println("First offer string = " + firstOfferStr);
         System.out.flush();
         StringTokenizer firstOffer = new StringTokenizer(firstOfferStr, ",");
         for(int i=1; firstOffer.hasMoreTokens(); i++) {
             String element = firstOffer.nextToken();
             //System.out.println("i="+ i + " token=" + element);
             System.out.flush();
             if(i == 1) assertEquals(element, "1071071015");
             if(i == 2) assertEquals(element, "Campbell's Microwaveable Soup at Hand");
             if(i == 3) assertEquals(element, "30.99");
             if(i == 4) assertEquals(element, "0%10.75 oz137368 / CartonAmount Per Serving (serving size) = 1 container Calories: 140 Total Fat: 0g Sat. Fat: 0g Cholesterol: 0mg Sodium: 890mg Total Carb: 31g Dietary Fiber: 2g Sugars: 18g Protein: 3g Vitamin A: 10% Vitamin C: 60% Calcium: 2% Iron:...");
             if(i == 5) assertEquals(element, "http://www.bizrate.com/oid1071071015/search/retarget/");
 
             // Following URL string is  not exactly same as in input xml
             // input xml has "&amp;" while following URLs have "&"
             // this is because "&" got encoded in xml as "&amp;"
             if(i == 6) assertEquals(element, "http://image10.bizrate-images.com/resize?sq=60&uid=1071071015&mid=26054");
         }
 
     }
 
 
     @Test (enabled = false)
     public void URLTest() throws IOException {
         OutputStream out = null;
         ShopzillaFeedConvertor convertor = new ShopzillaFeedConvertor();
         String url = "http://catalog.bizrate.com/services/catalog/v1/us/product?apiKey=bfc9253adedf4ad6880d24ee17eb59d6&publisherId=6866&&categoryId=10000000&keyword=&productId=&productIdType=&offersOnly=true&biddedOnly=true&start=0&results=300&sort=relevancy_desc&imageOnly=true&format=xml";
 
         //String url = "file://shopzilla.xml";
         try {
             out = new FileOutputStream(new File("feed.csv"));
             convertor.convert(url, out);
         } catch (Exception e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } finally {
             if(out != null) out.close();
         }
 
     }
 
     @Test (enabled = true)
     public void createProductFeedFile() throws IOException {
         OutputStream out = null;
         ShopzillaFeedConvertor convertor = new ShopzillaFeedConvertor();
 
         String url = "file:///Users/shitalm/Documents/work/greenfield/111products.xml";
         try {
            out = new FileOutputStream(new File("temp/110-productfeed.csv"));
             convertor.convert(url, out);
         } catch (Exception e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } finally {
             if(out != null) out.close();
         }
 
     }
 
 
 }
