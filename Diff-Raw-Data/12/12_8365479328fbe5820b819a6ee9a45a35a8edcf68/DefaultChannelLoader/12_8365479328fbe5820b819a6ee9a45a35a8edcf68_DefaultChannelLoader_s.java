 package org.lazydevs.veetle.api;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Sascha
  * Date: 01.06.11
  * Time: 23:09
  * To change this template use File | Settings | File Templates.
  */
 public class DefaultChannelLoader implements ChannelLoader {
 
     public static final Logger log = Logger.getLogger(DefaultChannelLoader.class.getSimpleName());
 
    public static final String DEFAULT_VEETLE_CHANNEL_DETAILS_URL = "http://veetle.com/index.php/channel/ajaxInfo/";
 
     private String veetleChannelDetailsUrl = DEFAULT_VEETLE_CHANNEL_DETAILS_URL;
 
     public String getVeetleChannelDetailsUrl() {
         return veetleChannelDetailsUrl;
     }
 
     public void setVeetleChannelDetailsUrl(String veetleChannelDetailsUrl) {
         this.veetleChannelDetailsUrl = veetleChannelDetailsUrl;
     }
 
     public List<String> load(List<String> channelIds) throws LoadChannelException {
 
         List<String> results = new ArrayList<String>();
 
         for(String channelId : channelIds) {
 
             results.add(load(channelId));
         }
 
         return results;
     }
 
     public String load(String channelId) throws LoadChannelException {
 
         if (veetleChannelDetailsUrl == null || veetleChannelDetailsUrl.length() == 0) {
             throw new IllegalArgumentException("URL for loading the channel is null or empty.");
         }
 
         String json;
 
         try {
             log.info("Start loading channel: " + channelId);
 
            URL url = new URL(veetleChannelDetailsUrl + "/" + channelId + "/" + new Date().getTime());
             URLConnection veetleConnection = url.openConnection();
 
             veetleConnection.setConnectTimeout(10000);
             veetleConnection.setReadTimeout(10000);
 
             BufferedReader in = new BufferedReader(new InputStreamReader(veetleConnection.getInputStream()));
 
             json = in.readLine();
 
             in.close();
 
             log.info("Finished loading channel");
 
         } catch (MalformedURLException e) {
             throw new LoadChannelException("Invalid URL provided.", e);
         } catch (IOException e) {
             throw new LoadChannelException("Error loading channel details from URL: " + veetleChannelDetailsUrl, e);
         }
 
         return json;
     }
 
 }
