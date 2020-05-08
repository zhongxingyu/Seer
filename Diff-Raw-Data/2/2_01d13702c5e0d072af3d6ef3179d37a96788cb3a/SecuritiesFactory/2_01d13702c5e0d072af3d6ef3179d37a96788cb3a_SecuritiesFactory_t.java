 package stockwatch;
 
 import stockwatch.WseMarketTypes.EWseMarketTypes;
 
 public class SecuritiesFactory {
 
    public Security getSecurity(EWseMarketTypes type) throws IllegalArgumentException {
         switch (type) {
             case MainMarket:
                 return getStock();
             case NewConnect:
                 return getStock();
             case Catalyst:
                 return getBond();
             case Futures:
                 return getFutureContract();
             case Options:
                 return getOption();
             case Indexes:
                 return getIndex();
             default:
                 throw new IllegalArgumentException("Wrong market type " + type);
         }
     }
 
     Security getStock() {
         return new Share();
     }
 
     Security getBond() {
         return new Bond();
     }
 
     Security getFutureContract() {
         return new FutureContract();
     }
     
     Security getOption() {
         return new Option();
     }
     
     Security getIndex() {
         return new Index();
     }
 }
