 package stockwatch;
 
 import java.util.Vector;
 
 public class StockExchangeContextBuilder {
     private ConfigParser configParser;
     
     StockExchangeContextBuilder() {
         configParser = new ConfigParser();
     }
 
     public StockExchange buildStockMarket(WarsawStockExchange stockmarket) {
         DataStoreHolder dataStoreHolder = 
                 new DataFileHolder(configParser.getDirectoryPath() + stockmarket.getClass().getSimpleName());
         
         stockmarket.setQuotestWriter(buildQuotesWriter(dataStoreHolder));
         stockmarket.setStatisticsWriter(buildStatisticsWriter(dataStoreHolder));
         
         return stockmarket;
     }
     
    public Vector<QuotesWriter> buildQuotesWriter(DataStoreHolder dataHolder) {
         Vector<QuotesWriter> quotesWriters = new Vector<QuotesWriter>();
         if (configParser.dumpToFile()) 
             quotesWriters.add(new QuotesToFileWriter(dataHolder));
         if (configParser.dumpToDatabase());
             // add quotes to database writer 
         return quotesWriters;
     }
 
    public Vector<StatisticsWriter> buildStatisticsWriter(DataStoreHolder dataHolder) {
         Vector<StatisticsWriter> statsWriters = new Vector<StatisticsWriter>();
         if (configParser.dumpToFile()) 
             statsWriters.add(new StatisticsToFileWriter(dataHolder));
         if (configParser.dumpToDatabase());
             // add stats to database writer
         return statsWriters;
     }
 }
