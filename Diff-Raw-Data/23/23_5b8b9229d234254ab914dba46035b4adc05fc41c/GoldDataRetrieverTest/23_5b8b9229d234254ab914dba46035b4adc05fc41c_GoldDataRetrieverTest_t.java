 package com.nkhoang.gae.test.gold;
 
 import com.nkhoang.gae.model.Currency;
 import com.nkhoang.gae.model.GoldPrice;
 import com.nkhoang.gae.utils.DateConverter;
 import net.htmlparser.jericho.Element;
 import net.htmlparser.jericho.Source;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.io.*;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.text.ParseException;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration({"/applicationContext-service.xml"})
 public class GoldDataRetrieverTest {
     public static final Logger LOGGER = LoggerFactory.getLogger(GoldDataRetrieverTest.class);
 
     @Test
     public void testRun() {}
 
     public void testGetGoldPrice() {
         Source source = retrieveWebContent("http://giavang.net");
     }
 
 
    @Test
     public void testGetExchangeRate() {
         getExchangeRate();
     }
 

    @Test
     public void testGetInternationalGoldPrice() {
         GoldPrice price = getInternationalGoldPrice();
 
         LOGGER.info(price.toString());
     }
 
     // http://giavang.net/resources/pricetable2a.asp
     private GoldPrice getInternationalGoldPrice() {
         GoldPrice price = new GoldPrice();
         Source source = retrieveWebContent("http://giavang.net/resources/pricetable2a.asp");
         List<Element> trElements = source.getAllElementsByClass("row1");
         Element trElement = trElements.get(0);
 
         List<Element> tdElements = trElement.getChildElements();
         // get the 2nd td
         Element priceElement = tdElements.get(1);
 
         String priceString = priceElement.getTextExtractor().toString();
         LOGGER.info("International Gold Price: " + priceString);
         // process string
         Calendar calendar = GregorianCalendar.getInstance();
         Date currentDate = calendar.getTime();
         price.setTime(currentDate);
         price.setCurrency("USD");
         // Using regex to get price value.
         Pattern p = Pattern.compile("\\d+.\\d+");
         Matcher m = p.matcher(priceString.split("/")[0]);
         if (m.find()) {
             price.setPriceSell(Float.parseFloat(m.group(0)));
         }
         m = p.matcher(priceString.split("/")[1]);
         if (m.find()) {
             price.setPriceBuy(Float.parseFloat(m.group(0)));
         }
         return price;
     }
 
     private List<Currency> getExchangeRate() {
         List<Currency> listExchangeRate = new ArrayList<Currency>(0);
         Source source = retrieveWebContent("http://www.sjc.com.vn/ajax_currency.php");
 
         List<Element> timeElements = source.getAllElementsByClass("text_tgvang_mota");
         String timeString = timeElements.get(0).getContent().toString();
         // create a new Currency object.
         Currency currency = new Currency();
         // process time        
         int index = timeString.indexOf(":");
         String currencyTime = timeString.substring(index + 1, index + 19).trim();
         Date currencyDate = null;
 
         // set to currency
         try {
             currencyDate = DateConverter.convertFromStringToken(currencyTime, DateConverter.defaultCurrencyDateFormat);
         } catch (ParseException parseEx) {
             LOGGER.error("Could not parse time for Currency.", parseEx);
             currency = null;
         }
         if (currency != null) {
             // process more information
             List<Element> otherElements = source.getAllElementsByClass("text_tgvang_chitiet");
             Iterator<Element> otherElementIter = otherElements.iterator();
             int counter = 0;
             while (otherElementIter.hasNext()) {
                 if (counter != 0 && counter % 4 == 0) {
                     currency = new Currency();
                 }
                 currency.setTime(currencyDate);
                 Element element = otherElementIter.next();
                 currency.setCurrency(element.getTextExtractor().toString());
                 counter++;
                 element = otherElementIter.next();
                 currency.setPriceBuy(Float.parseFloat(element.getContent().toString()));
                 counter++;
                 element = otherElementIter.next();
                 currency.setPriceSell(Float.parseFloat(element.getContent().toString()));
                 counter++;
                 otherElementIter.next();
                 LOGGER.info(currency.toString());
                 listExchangeRate.add(currency);
             }
         }
         return listExchangeRate;
     }
 
     private Source retrieveWebContent(String websiteURL) {
         Source source = null;
         try {
             URL url = new URL(websiteURL);
             HttpURLConnection connection = (HttpURLConnection) url.openConnection();
             connection.setRequestMethod("GET");
             // get inputStream
             InputStream is = connection.getInputStream();
             // create source HTML
             source = new Source(is);
         } catch (IOException ioe) {
             LOGGER.error("Cannot parse URL " + websiteURL, ioe);
         }
 
         return source;
     }
 
 
               @Test
     public void testGetVnGoldPrice() {
         getVNGoldData("http://www.sjc.com.vn/chart/data.csv");
     }
 
     private GoldPrice createGoldObject(String tokenString) {
         GoldPrice price = null;
         // parse String token
         String[] info = tokenString.split(";");
         if (info.length != 3) {
             LOGGER.error("Not enough information to create gold price object.");
         } else {
             price = new GoldPrice();
             Date priceDate = null;
             try {
                 priceDate = DateConverter.convertFromStringToken(info[0], DateConverter.defaultGoldDateFormat);
             } catch (ParseException parseEx) {
                 LOGGER.error("Could not parse date.", parseEx);
                 price = null;
             }
             if (price != null) {
                 price.setCurrency("VND");
                 price.setTime(priceDate);
                 price.setPriceBuy(Float.parseFloat(info[1]));
                 price.setPriceSell(Float.parseFloat(info[2]));
             }
         }
         return price;
     }
 
     // http://www.sjc.com.vn/chart/data.csv
     private void getVNGoldData(String websiteURL) {
         BufferedInputStream bis = null;
         try {
             // open connection
             URL url = new URL(websiteURL);
             URLConnection connection = url.openConnection();
             bis = new BufferedInputStream(connection.getInputStream());
 
 
             BufferedReader br = new BufferedReader(new InputStreamReader(bis));
             String strLine;
 
             StringTokenizer st;
 
             int lineNumber = 0, tokenNumber = 0;
             //read comma separated file line by line
             while ((strLine = br.readLine()) != null) {
                 lineNumber++;
                 //break comma separated line using ","
                 st = new StringTokenizer(strLine, ",");
                 while (st.hasMoreTokens()) {
                     //display csv values
                     tokenNumber++;
                     String goldToken = st.nextToken();
                     // starting to create gold object
                     GoldPrice goldUnit = createGoldObject(goldToken);
                     LOGGER.info(goldUnit.toString());
                 }
                 //reset token number
                 tokenNumber = 0;
             }
         } catch (Exception e) {
             LOGGER.error("Failed to get VN gold data from URL: [" + websiteURL + "].", e);
         } finally {
             if (bis != null) {
                 try {
                     bis.close();
                 } catch (IOException ioe) {
                     LOGGER.error("Could not close stream", ioe);
                 }
             }
         }
 
     }
 }
