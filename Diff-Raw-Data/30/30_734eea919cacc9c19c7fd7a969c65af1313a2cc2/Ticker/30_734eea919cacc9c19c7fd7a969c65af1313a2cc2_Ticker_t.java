 package to.sparks.mtgox.model;
 
 import com.fasterxml.jackson.annotation.JsonAutoDetect;
 import com.fasterxml.jackson.annotation.JsonProperty;
 
 /**
  *
  * @author SparksG
  */
 @JsonAutoDetect
 public class Ticker extends DtoBase implements CurrencyKludge {
 
     private TickerPrice high;
     private TickerPrice low;
     private TickerPrice avg;
     private TickerPrice vwap;
     private TickerPrice vol;
     private TickerPrice last_local;
     private TickerPrice last;
     private TickerPrice last_orig;
     private TickerPrice last_all;
     private TickerPrice buy;
     private TickerPrice sell;
     private String currencyCode;
    private long now;
 
    public Ticker(@JsonProperty("high") TickerPrice high,
             @JsonProperty("low") TickerPrice low,
             @JsonProperty("avg") TickerPrice avg,
             @JsonProperty("vwap") TickerPrice vwap,
             @JsonProperty("vol") TickerPrice vol,
             @JsonProperty("last_local") TickerPrice last_local,
             @JsonProperty("last") TickerPrice last,
             @JsonProperty("last_orig") TickerPrice last_orig,
             @JsonProperty("last_all") TickerPrice last_all,
             @JsonProperty("buy") TickerPrice buy,
            @JsonProperty("sell") TickerPrice sell,
            @JsonProperty("now") long now) {
         this.high = high;
         this.low = low;
         this.avg = avg;
         this.vwap = vwap;
         this.vol = vol;
         this.last_local = last_local;
         this.last = last;
         this.last_orig = last_orig;
         this.last_all = last_all;
         this.buy = buy;
         this.sell = sell;
        this.now = now;

         if (this.vol != null) {
             this.vol.setCurrencyInfo(CurrencyInfo.BitcoinCurrencyInfo);
         }
 
         this.currencyCode = avg.getCurrencyCode();
 
     }
 
     public String getCurrencyCode() {
         return currencyCode;
     }
 
     /*
      * Cast the result to either MtGoxBitcoin or MtGoxFiatCurrency depending on
      * which is correct for the situation.
      */
     private static MtGoxUnitOfCredit getMtGoxUnits(long intValue, CurrencyInfo currencyInfo) {
         MtGoxUnitOfCredit result = new MtGoxBitcoin(intValue);
         if (currencyInfo != null && !currencyInfo.isVirtual()) {
             result = new MtGoxFiatCurrency(intValue, currencyInfo);
         }
         return result;
     }
 
     public MtGoxUnitOfCredit getHigh() {
         return getMtGoxUnits(high.getPriceValueInt(), high.getCurrencyInfo());
     }
 
     public void setHigh(TickerPrice high) {
         this.high = high;
     }
 
     public MtGoxUnitOfCredit getLow() {
         return getMtGoxUnits(low.getPriceValueInt(), low.getCurrencyInfo());
     }
 
     public void setLow(TickerPrice low) {
         this.low = low;
     }
 
     public MtGoxUnitOfCredit getAvg() {
         return getMtGoxUnits(avg.getPriceValueInt(), avg.getCurrencyInfo());
     }
 
     public void setAvg(TickerPrice avg) {
         this.avg = avg;
     }
 
     public MtGoxUnitOfCredit getVwap() {
         return getMtGoxUnits(vwap.getPriceValueInt(), vwap.getCurrencyInfo());
     }
 
     public void setVwap(TickerPrice vwap) {
         this.vwap = vwap;
     }
 
     public MtGoxUnitOfCredit getVol() {
         return getMtGoxUnits(vol.getPriceValueInt(), vol.getCurrencyInfo());
     }
 
     public void setVol(TickerPrice vol) {
         this.vol = vol;
     }
 
     public MtGoxUnitOfCredit getLast_local() {
         return getMtGoxUnits(last_local.getPriceValueInt(), last_local.getCurrencyInfo());
 
     }
 
     public void setLast_local(TickerPrice last_local) {
         this.last_local = last_local;
     }
 
     public MtGoxUnitOfCredit getLast() {
         return getMtGoxUnits(last.getPriceValueInt(), last.getCurrencyInfo());
     }
 
     public void setLast(TickerPrice last) {
         this.last = last;
     }
 
     public TickerPrice getLast_orig() {
         return last_orig;
     }
 
     public void setLast_orig(TickerPrice last_orig) {
         this.last_orig = last_orig;
     }
 
     public TickerPrice getLast_all() {
         return last_all;
     }
 
     public void setLast_all(TickerPrice last_all) {
         this.last_all = last_all;
     }
 
     public TickerPrice getBuy() {
         return buy;
     }
 
     public void setBuy(TickerPrice buy) {
         this.buy = buy;
     }
 
     public TickerPrice getSell() {
         return sell;
     }
 
     public void setSell(TickerPrice sell) {
         this.sell = sell;
     }
 
     @Override
     public void setCurrencyInfo(CurrencyInfo currencyInfo) {
         high.setCurrencyInfo(currencyInfo);
         low.setCurrencyInfo(currencyInfo);
         avg.setCurrencyInfo(currencyInfo);
         vwap.setCurrencyInfo(currencyInfo);
         last_local.setCurrencyInfo(currencyInfo);
         last.setCurrencyInfo(currencyInfo);
         last_orig.setCurrencyInfo(currencyInfo);
         last_all.setCurrencyInfo(currencyInfo);
         buy.setCurrencyInfo(currencyInfo);
         sell.setCurrencyInfo(currencyInfo);
     }

    public long getNow() {
        return now;
    }
}
