 package ru.exorg.miner;
 
 import java.util.TreeMap;
 
 import org.apache.commons.httpclient.cookie.CookiePolicy;
 import org.apache.log4j.Logger;
 
 import org.webharvest.definition.ScraperConfiguration;
 import org.webharvest.exception.HttpException;
 import org.webharvest.runtime.Scraper;
 import org.webharvest.runtime.ScraperContext;
 
 import ru.exorg.core.service.*;
 
 // ================================================================================
 
 public abstract class Miner {
     private Logger log;
     private String[] configFiles;
     private int httpTimeout;
     private int maxRetries;
 
     private String proxyHost;
     private int proxyPort;
 
     protected DataProvider dataProvider;
 
     protected static class Vars extends TreeMap<String, String> { }
 
     protected static String beautify(final String str) {
         return str
             .trim()
             .replaceAll("^[\r\n \t]*", "")
             .replaceAll("[\r\n \t]*$", "")
             .replaceAll("[ \t]+", " ")
             .replaceAll("[\r\n]+[\r\n ]*", "\n");
     }
 
     protected static Vars parseMinedItem(final String info) {
         Vars v = new Vars();
 
         String[] fields = info.split("[\\[\\]]");
 
         for (int i = 1; i + 1 < fields.length; i += 2) {
             if (fields[i + 1].length() > 1) {
                 v.put(fields[i], beautify(fields[i + 1]));
             }
         }
 
         return v;
     }
 
 
     public Miner() {
         this.log = Logger.getLogger(Main.class);
         this.configFiles = new String[0];
 
         this.httpTimeout = 10000;
         this.maxRetries = 5;        
     }
 
     final public void setDataProvider(final DataProvider p) {
         this.dataProvider = p;
     }
 
     final public void setConfig(final String config) {
         this.configFiles = config.split(";");
     }
 
     final public void setProxy(final String proxy) {
        String[] proxyCfg = proxy.split(":");
 
        this.proxyHost = proxyCfg[0];
        this.proxyPort = Integer.parseInt(proxyCfg[1]);
     }
 
     final public void setHttpTimeout(int timeout) {
         this.httpTimeout = timeout;
     }
 
     final public void setMaxRetries(int retries) {
         this.maxRetries = retries;
     }
 
     final public void run() {
         try {
             for (String configFile : this.configFiles) {
                 this.log.warn("Working on " + configFile);
 
                 int cTry = this.maxRetries;
                 boolean success = false;
                 
                 Scraper scraper = null;
                 while (!success && cTry > 0) {                
                     ScraperConfiguration config = 
                         new ScraperConfiguration(configFile);
                     scraper = new Scraper(config, ".");
 
                     if (this.proxyHost != null) {
                         scraper.getHttpClientManager().setHttpProxy(proxyHost, proxyPort);
                     }
                 
                     scraper.getHttpClientManager().getHttpClient().getParams().setSoTimeout(this.httpTimeout);
                     scraper.getHttpClientManager().getHttpClient().getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
                     scraper.getHttpClientManager().getHttpClient().getHttpConnectionManager().getParams().setConnectionTimeout(this.httpTimeout);
                     scraper.setDebug(false);
 
                     try {
                         scraper.execute();
                         success = true;
                     } catch (HttpException e) {
                         this.log.warn("HTTP error occured. Retries left " + String.valueOf(cTry));
                         System.out.println("HTTP error occured. Retries left " + String.valueOf(cTry));
                         cTry = cTry - 1;
                     } finally {
                     }
                 }
 
                 if (success) {
                     handle(scraper.getContext());
                 } else {
                     this.log.warn("Not handling " + configFile + ". Retry count exceeded.");
                     System.out.println("Not handling " + configFile + ". Retry count exceeded.");
                 }
             }
         } catch (Exception e) {
             System.out.println("Exception was caught. See debug.log for details");
             this.log.warn(e.getMessage());
             this.log.warn(ru.exorg.core.util.Log.getCallStack(e));
         }
     }
 
     abstract protected void handle(final ScraperContext sc) throws Exception;
 }
