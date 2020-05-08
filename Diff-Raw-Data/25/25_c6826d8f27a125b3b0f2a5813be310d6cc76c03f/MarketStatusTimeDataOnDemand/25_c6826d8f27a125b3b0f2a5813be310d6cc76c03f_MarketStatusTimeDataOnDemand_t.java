 package solubris.marketmon.domain;
 
 import org.springframework.roo.addon.dod.RooDataOnDemand;
 
 @RooDataOnDemand(entity = MarketStatusTime.class)
 public class MarketStatusTimeDataOnDemand {
    public MarketStatusTime getNewTransientMarketStatusTime(int index) {
        MarketStatusTime obj = new MarketStatusTime();
        setCreatedAt(obj, index);
        setInplay(obj, index);
        setStatus(obj, index);
        setMarket(obj, index);
        return obj;
    }

    public void setMarket(MarketStatusTime obj, int index) {
    	Market market=new Market();
    	market.setId((long)index+1);
    	market.persist();
        obj.setMarket(market);
    }
 }
