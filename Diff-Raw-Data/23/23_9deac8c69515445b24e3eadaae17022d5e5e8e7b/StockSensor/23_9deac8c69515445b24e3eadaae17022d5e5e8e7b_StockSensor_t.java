 /**
  * User: pizuricv
  * Date: 6/4/13
  */
 
 package com.ai.myplugin;
 
 import com.ai.bayes.plugins.BNSensorPlugin;
 import com.ai.bayes.scenario.TestResult;
 import com.ai.util.resource.TestSessionContext;
 import net.xeoh.plugins.base.annotations.PluginImplementation;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 
 @PluginImplementation
 public class StockSensor implements BNSensorPlugin{
 
     private static final String STOCK = "stock";
     private static final String THRESHOLD = "threshold";
    private static final String VALUE_TAG = "PRICE, VOLUME, HIGH, LOW, MOVING_AVERAGE, PERCENT";
     static final String server = "http://finance.yahoo.com/d/quotes.csv?s=";
     Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();
     String [] states = {"Below", "Above"};
     String [] tags = {"PRICE","VOLUME", "HIGH", "LOW", "MOVING_AVERAGE", "PERCENT"};
     private static final String FORMAT_QUERY = "&f=l1vhgm4p2d1t1";
 
     @Override
     public String[] getRequiredProperties() {
         return new String[]{VALUE_TAG, STOCK, THRESHOLD};
     }
 
     @Override
     public void setProperty(String string, Object obj) {
         if(string.equals(VALUE_TAG) || string.equals(STOCK)
                 || string.equals(THRESHOLD) ) {
             if(string.equals(VALUE_TAG)){
                 Set<String> mySet = new HashSet<String>(Arrays.asList(tags));
                if(!mySet.contains(obj.toString())){
                     throw new RuntimeException("Property "+ string + " is not in correct format "+Arrays.toString(tags));
                 }
             }
            propertiesMap.put(string, obj.toString());
         } else {
             throw new RuntimeException("Property "+ string + " not in the required settings");
         }
     }
 
     @Override
     public Object getProperty(String string) {
         return propertiesMap.get(string);
     }
     @Override
     public String getDescription() {
        return "Stock exchange sensor";
     }
 
     @Override
     public TestResult execute(TestSessionContext testSessionContext) {
         URL url;
         boolean testSuccess = true;
        final Double threshold = Double.parseDouble((String) getProperty(THRESHOLD));
         final String tag = (String) getProperty(VALUE_TAG);
         System.out.println("Properties are " + getProperty(STOCK) + ", " + tag + ", "+threshold);
 
         try {
             url = new URL(server+ getProperty(STOCK) + FORMAT_QUERY);
         } catch (MalformedURLException e) {
             System.err.println(e.getLocalizedMessage());
             throw new RuntimeException(e);
         }
         HttpURLConnection conn = null;
         try {
             conn = (HttpURLConnection) url.openConnection();
         } catch (IOException e) {
             e.printStackTrace();
             testSuccess = false;
         }
         assert conn != null;
         try {
             conn.setRequestMethod("GET");
         } catch (ProtocolException e) {
             e.printStackTrace();
             testSuccess = false;
         }
 
         BufferedReader rd = null;
         try {
             rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
         } catch (IOException e) {
             e.printStackTrace();
             testSuccess = false;
         }
         String inputLine;
         StringBuffer stringBuffer = new StringBuffer();
 
         assert rd != null;
         try {
             while ((inputLine = rd.readLine()) != null){
                 stringBuffer.append(inputLine);
             }
         } catch (IOException e) {
             e.printStackTrace();
             testSuccess = false;
         }
         conn.disconnect();
         try {
             rd.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         final String stringToParse = stringBuffer.toString();
         System.out.println("Response for " + getProperty(STOCK) + " >>" + stringToParse);
         StringTokenizer stringTokenizer = new StringTokenizer(stringToParse,",");
         final ConcurrentHashMap<String, Double> hashMap = new ConcurrentHashMap<String, Double>();
 
         parseOutput("PRICE", hashMap, stringTokenizer);
         parseOutput("VOLUME", hashMap, stringTokenizer);
         parseOutput("HIGH", hashMap, stringTokenizer);
         parseOutput("LOW", hashMap, stringTokenizer);
         parseOutput("MOVING_AVERAGE", hashMap, stringTokenizer);
         parseOutput("PERCENT", hashMap, stringTokenizer);
 
         //date:time
         SimpleDateFormat format =
                 new SimpleDateFormat("\"MM/dd/yyyy\" \"HH:mma\"");
         String dateString = stringTokenizer.nextToken() + " " + stringTokenizer.nextToken();
         try {
             Date parsed = format.parse(dateString);
             System.out.println("Date is " + parsed.toString());
         } catch (ParseException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         final boolean finalTestSuccess = testSuccess;
         return new TestResult() {
             @Override
             public boolean isSuccess() {
                 return finalTestSuccess;
             }
 
             @Override
             public String getName() {
                 return "Stock result";
             }
 
             @Override
             public String getObserverState() {
                 if("PRICE".equalsIgnoreCase(tag))  {
                     if(hashMap.get("PRICE") < threshold)
                         return "Below";
                     return "Above";
                 } else if("HIGH".equalsIgnoreCase(tag))  {
                     if(hashMap.get("HIGH") < threshold)
                         return "Below";
                     return "Above";
                 }  else if("LOW".equalsIgnoreCase(tag))  {
                     if(hashMap.get("LOW") < threshold)
                         return "Below";
                     return "Above";
                 }  else if("VOLUME".equalsIgnoreCase(tag))  {
                     if(hashMap.get("VOLUME") < threshold)
                         return "Below";
                     return "Above";
                 }  else if("MOVING_AVERAGE".equalsIgnoreCase(tag))  {
                     if(hashMap.get("MOVING_AVERAGE") < threshold)
                         return "Below";
                     return "Above";
                 } else if("PERCENT".equalsIgnoreCase(tag))  {
                     if(hashMap.get("PERCENT") < threshold)
                         return "Below";
                     return "Above";
                 } else {
                     throw new RuntimeException("Error getting Stock result");
                 }
             }
         } ;
     }
 
     private void parseOutput(String tag, Map<String, Double> parsing, StringTokenizer stringTokenizer) {
         try{
             String string = stringTokenizer.nextToken();
             parsing.put(tag, Double.parseDouble(string.replaceAll("%", "").replaceAll("\"", "")));
         } catch (Exception e){
             System.err.println("Error parsing [" + tag + "] " + e.getMessage());
         }
     }
 
     @Override
     public String getName() {
         return "Stock exchange sensor result";
     }
 
     @Override
     public String[] getSupportedStates() {
         return states;
     }
 
     public static void main(String[] args){
         StockSensor stockSensor = new StockSensor();
         stockSensor.setProperty(STOCK, "MSFT");
         stockSensor.setProperty(VALUE_TAG, "PRICE");
        stockSensor.setProperty(THRESHOLD, "36");
         System.out.println(Arrays.toString(stockSensor.getSupportedStates()));
         System.out.println(stockSensor.execute(null).getObserverState());
 
 
         stockSensor.setProperty(STOCK, "GOOG");
         stockSensor.setProperty(VALUE_TAG, "PRICE");
        stockSensor.setProperty(THRESHOLD, "800.0");
         System.out.println(stockSensor.execute(null).getObserverState());
 
         stockSensor.setProperty(STOCK, "BAR.BR");
         stockSensor.setProperty(VALUE_TAG, "PERCENT");
        stockSensor.setProperty(THRESHOLD, "-1.0");
         System.out.println(stockSensor.execute(null).getObserverState());
     }
 }
