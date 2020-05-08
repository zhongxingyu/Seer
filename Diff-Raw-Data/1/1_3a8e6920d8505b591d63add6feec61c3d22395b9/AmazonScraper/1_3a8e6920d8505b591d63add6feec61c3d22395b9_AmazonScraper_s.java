 /**
 (C) Kyle Kamperschroer 2013
  */
 
 package com.kylek.ripe.core;
 
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.io.InputStream;
 
 public class AmazonScraper {
 
    ////////////////////////////////////////
    // Members
    ////////////////////////////////////////
 
    // The search find/replace string
    private static final String SEARCH_FIND_REPLACE = "#{INPUT}";
 
    // Hard coded search URL
    private static final String SEARCH_URL =
       "http://fresh.amazon.com/Search?resultsPerPage=1&comNow=&input=" +
       SEARCH_FIND_REPLACE;
 
    // The product find/replace string
    private static final String PRODUCT_FIND_REPLACE = "#{ASIN}";
 
    // Hard coded product URL
    private static final String PRODUCT_URL =
       "http://fresh.amazon.com/product?asin=" +
       PRODUCT_FIND_REPLACE;
 
    // The pattern we are looking for
    private static final String ASIN_PATTERN =
       "<input type=\"hidden\" name=\"addToCartAsin\" value=\"(.+)\" />";
 
    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////
 
    // None. Everything is static here.
 
    ////////////////////////////////////////
    // Methods
    ////////////////////////////////////////
 
    // A method to get a product URL, given a search string
    public static String getProductUrl(String inputProduct){
       if (inputProduct == null){
          return "";
       }
       
       // First, clean up the input string
       String product = inputProduct.replaceAll(" ", "+")
                                    .replaceAll("<", "&lt;")
                                    .replaceAll("&", "&amp;");
 
       // Now build the search URL
       final String builtSearchUrl = SEARCH_URL.replace(SEARCH_FIND_REPLACE,
                                                        product);
 
       String webPage;
 
       try{
          // Get the contents of that webpage
          URL searchUrl = new URL(builtSearchUrl);
          HttpURLConnection connection =
             (HttpURLConnection) searchUrl.openConnection();
          connection.setRequestMethod("GET");
          connection.connect();
 
          InputStream stream = connection.getInputStream();
          Scanner s = new Scanner(stream).useDelimiter("\\A");
          webPage = s.hasNext() ? s.next() : "";
          s.close();
          stream.close();
       }
       catch (Exception e){
          System.out.println("Exception trying to get a product URL");
          System.out.println(e.toString());
          return "";
       }
 
       // Sweet, got the web page from the search!
       // Now we just need to find the first product on that page.
       Pattern asinPattern = Pattern.compile(ASIN_PATTERN);
       Matcher matcher = asinPattern.matcher(webPage);
 
       // Try to find one
       String asin = "";
       if (matcher.find()){
          asin = matcher.group(1);
       }
       else{
          System.out.println("Error: No pattern matched on the page for \""
                             + inputProduct
                             + "\"");
          return "";
       }
 
       // Nice work, now build the final product URL
       String productUrl = PRODUCT_URL.replace(PRODUCT_FIND_REPLACE,
                                               asin);
       return productUrl;
    }
 }
