 package com.mns.mojoinvest.server.pipeline.fund;
 
 import com.google.appengine.tools.pipeline.FutureValue;
 import com.google.appengine.tools.pipeline.Job0;
 import com.google.appengine.tools.pipeline.Job1;
 import com.google.appengine.tools.pipeline.Value;
 import com.mns.mojoinvest.server.engine.model.Fund;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.core.util.MultivaluedMapImpl;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import javax.ws.rs.core.MultivaluedMap;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 public class FundFetcherJob extends Job0<List<Fund>> {
 
     private static final Logger log = Logger.getLogger(FundFetcherJob.class.getName());
 
     private static final int BATCH_SIZE = 50;
 
     @Override
     public Value<List<Fund>> run() {
         String html = fetchAllFundsHtml();
         List<String> symbols = scrapeSymbols(html);
 
         log.info("Attempting to retrieve details for " + symbols.size() + " funds");
         List<FutureValue<List<Fund>>> fundLists = new ArrayList<FutureValue<List<Fund>>>();
         List<String> batch = new ArrayList<String>(BATCH_SIZE);
 
         for (String symbol : symbols) {
             batch.add(symbol);
             if (batch.size() == BATCH_SIZE) {
                 List<String> clone = new ArrayList<String>(batch);
                 fundLists.add(futureCall(new FundDetailFetcherBatchJob(), immediate(clone)));
                 batch.clear();
             }
         }
         if (batch.size() > 0) {
             fundLists.add(futureCall(new FundDetailFetcherBatchJob(), immediate(batch)));
         }
 
         return futureCall(new MergeFundListJob(), futureList(fundLists));
     }
 
     private String fetchAllFundsHtml() {
         Client c = Client.create();
         c.setReadTimeout(10000);
         c.setConnectTimeout(10000);
 
         WebResource r = c.resource("http://moneycentral.msn.com/investor/partsub/funds/etfperformancetracker.aspx");
         MultivaluedMap<String, String> params = new MultivaluedMapImpl();
         params.add("tab", "mkt");
         params.add("show", "all");
         log.info("Attempting to fetch all funds html");
         String html = r.queryParams(params).get(String.class);
         log.info("Received all funds html");
         return html;
     }
 
     protected List<String> scrapeSymbols(String html) {
         Document doc = Jsoup.parse(html);
         Element table = doc.getElementById("ctl00_ctl00_ctl00_ctl00_HtmlBody_HtmlBody_HtmlBody_Column1_dgETF");
         Element tbody = table.getElementsByTag("tbody").get(0);
         Elements tr = tbody.getElementsByTag("tr");
         List<String> symbols = new ArrayList<String>();
         for (Element element : tr) {
             String symbol = element.child(1).text();
             if ("TICKER".equals(symbol))
                 continue;
             symbols.add(symbol);
         }
         return symbols;
     }
 
     public static class FundFetcherBatchJob extends Job1<List<Fund>, List<String>> {
 
         private static final Logger log = Logger.getLogger(FundFetcherBatchJob.class.getName());
 
         @Override
         public Value<List<Fund>> run(List<String> symbols) {
             List<FutureValue<Fund>> funds = new ArrayList<FutureValue<Fund>>();
             log.info("Attempting to retrieve details for batch: " + symbols);
             for (String symbol : symbols) {
                 funds.add(futureCall(new FundDetailFetcherJob(), immediate(symbol)));
             }
             return futureList(funds);
         }
 
     }
 
     private static class MergeFundListJob extends Job1<List<Fund>, List<List<Fund>>> {
 
         @Override
         public Value<List<Fund>> run(List<List<Fund>> lists) {
             List<Fund> funds = new ArrayList<Fund>();
             for (List<Fund> list : lists) {
                 funds.addAll(list);
             }
             return immediate(funds);
         }
     }
 }
