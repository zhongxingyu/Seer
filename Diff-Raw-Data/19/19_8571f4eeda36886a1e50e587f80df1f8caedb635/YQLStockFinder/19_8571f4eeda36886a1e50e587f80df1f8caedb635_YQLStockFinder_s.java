 package mitch30000.stockinfo.control;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import mitch30000.stockinfo.exception.StockException;
 import mitch30000.stockinfo.model.CurrentStock;
 import mitch30000.stockinfo.model.Stock;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.utils.URIBuilder;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.joda.time.Interval;
 
 /**
  *
  * @author Mitch
  */
 public class YQLStockFinder implements StockFinder {
 
     private static final String CURRENT_QUERY = "select * from yahoo.finance.quote where symbol in (%s)";
     private URIBuilder builder;
 
     public YQLStockFinder() throws StockException {
         try {
             builder = new URIBuilder();
             builder.setPath("http://query.yahooapis.com/v1/public/yql")
                     .addParameter("format", "json")
                     .addParameter("env", "store://datatables.org/alltableswithkeys");
         } catch (Exception ex) {
             throw new StockException("Error during YQLStockFinder construction", ex);
         }
     }
 
     public Stock findCurrentStock(String symbol) throws StockException {
         try {
             builder.setParameter("q", String.format(CURRENT_QUERY, "\"" + symbol + "\""));
             JsonNode result = callService(builder);
             return parseToCurrentStock(result);
         } catch (Exception ex) {
             throw new StockException("Error gathering information for " + symbol, ex);
         }
     }
 
     public List<Stock> findCurrentStocks(String... symbols) throws StockException {
         if (symbols.length == 0) {
             throw new StockException("Cannot run findCurrentStocks when no stocks have been provided");
         }
         try {
             List<Stock> stocks = new ArrayList<Stock>();
             String symbolsQuery = "";
             String seperator = "";
             for (String symbol : symbols) {
                symbolsQuery += "\"" + symbol + "\"";
                 seperator = ",";
                symbolsQuery += seperator;
             }
             builder.setParameter("q", String.format(CURRENT_QUERY, symbolsQuery));
             JsonNode results = callService(builder);
             Iterator<JsonNode> iter = results.getElements();
             while (iter.hasNext()) {
                 JsonNode result = iter.next();
                 stocks.add(parseToCurrentStock(result));
             }
             return stocks;
         } catch (Exception ex) {
             throw new StockException("Error gathering information for " + symbols, ex);
         }
     }
 
     public Stock updateCurrentStock(Stock stock) throws StockException {
         try {
             builder.setParameter("q", String.format(CURRENT_QUERY, "\"" + stock.getSymbol() + "\""));
             JsonNode result = callService(builder);
             return parseToCurrentStock(result);
         } catch (Exception ex) {
             throw new StockException("Error gathering information for " + stock.getSymbol(), ex);
         }
     }
 
     public List<Stock> updateCurrentStocks(List<Stock> stocks) throws StockException {
         if (stocks.isEmpty()) {
             throw new StockException("Cannot run updateCurrentStocks when no stocks have been provided");
         }
         try {
             List<Stock> newStocks = new ArrayList<Stock>();
             String symbolsQuery = "";
             String seperator = "";
             for (Stock stock : stocks) {
                symbolsQuery += "\"" + stock.getSymbol() + "\"";
                seperator = ",";
                 symbolsQuery += seperator;
             }
             builder.setParameter("q", String.format(CURRENT_QUERY, symbolsQuery));
             JsonNode results = callService(builder);
             Iterator<JsonNode> iter = results.getElements();
             while (iter.hasNext()) {
                 JsonNode result = iter.next();
                 newStocks.add(parseToCurrentStock(result));
             }
             return newStocks;
         } catch (Exception ex) {
             throw new StockException("Error gathering information for stocks", ex);
         }
     }
 
     public Stock findHistoricStock(Interval interval, String symbol) throws StockException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     public List<Stock> findHistoricStocks(Interval interval, String... symbols) throws StockException {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     private JsonNode callService(URIBuilder builder) throws StockException {
         String json = "";
         try {
             HttpClient client = new DefaultHttpClient();
             HttpGet get = new HttpGet(builder.build());
             HttpResponse response = client.execute(get);
             HttpEntity entity = response.getEntity();
             if (entity != null) {
                 InputStreamReader is = new InputStreamReader(entity.getContent());
                 BufferedReader reader = new BufferedReader(is);
                 json = reader.readLine();
             }
             ObjectMapper mapper = new ObjectMapper();
             return mapper.readTree(json).path("query").path("results").path("quote");
         } catch (Exception ex) {
             throw new StockException("Error during service call", ex);
         }
     }
 
     private CurrentStock parseToCurrentStock(JsonNode quote) throws StockException {
         CurrentStock stock = new CurrentStock();
         try {
             stock.setSymbol(quote.path("Symbol").asText());
             stock.setCompanyName(quote.path("Name").asText());
             stock.setExchange(quote.path("StockExchange").asText());
             stock.setChange(quote.path("Change").asDouble());
             stock.setLow(quote.path("DaysLow").asDouble());
             stock.setHigh(quote.path("DaysHigh").asDouble());
             stock.setPrice(quote.path("LastTradePriceOnly").asDouble());
             stock.setVolume(quote.path("Volume").asLong());
         } catch (Exception ex) {
             throw new StockException("Error during json parsing", ex);
         }
         return stock;
     }
 }
