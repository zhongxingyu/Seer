 package stockwatch;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 import stockwatch.WseMarketTypes.EWseMarketTypes;
 
 public class InternalMarkets{
 
     private Map<String, Vector<Security>> internalMarkets;
     private Map<String, SessionStatistics> marketsStatistics;
 
     public InternalMarkets() {
         internalMarkets = new HashMap<String, Vector<Security>>();
         marketsStatistics = new HashMap<String, SessionStatistics>();
         
         EWseMarketTypes allMarkets[] = EWseMarketTypes.values();
         for (EWseMarketTypes market : allMarkets) {
             internalMarkets.put(market.name(), new Vector<Security>());
             marketsStatistics.put(market.name(), new SessionStatistics());
         }
     }
     
     public Map<String, Vector<Security>> getQuotes() {
         return internalMarkets;
     }
     
     public Map<String, SessionStatistics> getStatistics() {
         return marketsStatistics;
     }
     
    public void updateMarkets(InternalMarkets updatedMarkets){
        this.internalMarkets = updatedMarkets.getQuotes();
        
         EWseMarketTypes allMarkets[] = EWseMarketTypes.values();
         for (EWseMarketTypes market : allMarkets) {
             marketsStatistics.get(market.name()).makeStatistics(internalMarkets.get(market.name()));
         }
         
     }
 }
