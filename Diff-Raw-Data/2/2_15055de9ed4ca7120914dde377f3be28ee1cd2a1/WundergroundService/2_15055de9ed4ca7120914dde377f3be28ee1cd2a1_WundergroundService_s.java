 package pk.ip.weather.api.wunderground;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Scanner;
 import pk.ip.weather.api.wunderground.model.forecast.ForecastResponse;
 import pk.ip.weather.api.wunderground.model.forecast.SimpleForecast;
 import pk.ip.weather.api.wunderground.model.history.History;
 import pk.ip.weather.api.wunderground.model.history.HistoryResponse;
 import pk.ip.weather.core.Format;
 import pk.ip.weather.core.MessageParser;
 
 public class WundergroundService
 {
     public final static String API_URL = "http://api.wunderground.com/api";
     String urlPattern = API_URL+"/%s/%s/q/%s.%s";
     String apiKey;
     Format format;
     MessageParser messageParser;
        
     public WundergroundService(MessageParser messageParser, String apiKey, Format format)
     {
         this.apiKey = apiKey;
         this.format = format;
         this.messageParser = messageParser;        
     }
     
     public WundergroundService(MessageParser messageParser, String apiKey)
     {
         this(messageParser, apiKey, Format.JSON);
     }
     
     String formatApiUrl(String feature, String query)
     {
         try
         {
             return String.format(urlPattern, apiKey, feature, URLEncoder.encode(query, "utf-8"), format);
         }
         catch(UnsupportedEncodingException e)
         {
             throw new RuntimeException(e);
         }
     }
     
     public History findHistory(Date date, String location)
     {
        DateFormat formatter = new SimpleDateFormat("yyyyMd");
         String feature = "history_"+formatter.format(date);
         
         String url = formatApiUrl(feature, location);
         
         String response = doRequest(url);
         
         HistoryResponse historyResponse = messageParser.parseMessage(response, HistoryResponse.class);
         
         return historyResponse.history;
     }
 
     private String doRequest(String urlAddress)
     {
         Scanner in = null;
         try
         {
             URL url = new URL(urlAddress);
             in = new Scanner(new BufferedInputStream(url.openStream()));
             
             
             StringBuilder builder = new StringBuilder();
             
             while(in.hasNextLine())
             {
                 builder.append(in.nextLine());
             }
             
             return builder.toString();
         }
         catch (IOException ex)
         {            
             throw new RuntimeException(ex);
         }
         finally
         {
             if(in != null)
             {
                 in.close();
             }
         }
     }
     
     public SimpleForecast findForecast(String location)
     {
         String url = formatApiUrl("forecast", location);
         
         String response = doRequest(url);
         
         ForecastResponse forecastResponse = messageParser.parseMessage(response, ForecastResponse.class);
         
         return forecastResponse.forecast.simpleforecast;
     }
 }
