 /**
  * 
  */
 
 package com.zygon.trade.market.data.interpret;
 
 import com.zygon.trade.market.util.MovingAverage;
 import com.zygon.trade.market.util.Aggregation;
 import com.zygon.trade.market.model.indication.market.MACD;
 import com.zygon.trade.market.model.indication.market.MACDSignalCross;
 import com.zygon.trade.market.model.indication.market.MACDZeroCross;
 import com.zygon.trade.market.data.Ticker;
 import com.zygon.trade.market.data.TickerUtil;
 import com.zygon.trade.market.util.Type;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author zygon
  */
 public class TickerMACD extends TickerInterpreter {
     
     private final MovingAverage leadingMA;
     private final MovingAverage laggingMA;
     private final MovingAverage macdMA;
 
     public TickerMACD(MovingAverage leadingMA, MovingAverage laggingMA, MovingAverage macdMA) {
         super();
         this.leadingMA = leadingMA;
         this.laggingMA = laggingMA;
         this.macdMA = macdMA;
     }
     
     public TickerMACD(Aggregation leading, Aggregation lagging, Aggregation macd) {
         this(new MovingAverage(leading.getDuration(), leading.getUnits(), new MovingAverage.ExponentialValueFn()),
              new MovingAverage(lagging.getDuration(), lagging.getUnits(), new MovingAverage.ExponentialValueFn()),
              new MovingAverage(macd.getDuration(), macd.getUnits(), new MovingAverage.ExponentialValueFn()));
         
         if (leading.getType() != Type.AVG || lagging.getType() != Type.AVG || macd.getType() != Type.AVG) {
             throw new IllegalArgumentException("Aggregations must be based on average");
         }
     }
     
     private boolean firstValue = true;
     private boolean aboveZero = false;
    private boolean macdAboveSignal = false;
     
     @Override
     public MACD[] interpret(Ticker in) {
         
         //TBD: only get the average price every x number of ticks?
         
         this.leadingMA.add(TickerUtil.getMidPrice(in), in.getTimestamp());
         this.laggingMA.add(TickerUtil.getMidPrice(in), in.getTimestamp());
         
         double leadingPrice = this.leadingMA.getMean();
         double laggingPrice = this.laggingMA.getMean();
         
         List<MACD> macds = new ArrayList<>();
         
         if (!Double.isNaN(leadingPrice) && !Double.isNaN(laggingPrice)) {
             
             double macdLine = leadingPrice - laggingPrice;
 
             this.macdMA.add(macdLine, in.getTimestamp());
 
             double signalLine = this.macdMA.getMean();
             
             if (!Double.isNaN(signalLine)) {
                 
                 if (this.firstValue) {
                     // First calc zero cross
                     this.aboveZero = macdLine > 0.0;
 
                     // Now calc signal cross
                    this.macdAboveSignal = macdLine > signalLine;
 
                     this.firstValue = false;
                 } else {
                     if (this.aboveZero) {
                         if (macdLine < 0.0) {
                             this.aboveZero = false;
                             macds.add(new MACDZeroCross(in.getTradableIdentifier(), in.getTimestamp().getTime(), this.aboveZero));
                         }
                     } else {
                         if (macdLine > 0.0) {
                             this.aboveZero = true;
                             macds.add(new MACDZeroCross(in.getTradableIdentifier(), in.getTimestamp().getTime(), this.aboveZero));
                         }
                     }
 
                    if (this.macdAboveSignal) {
                        if (macdLine < signalLine) {
                            this.macdAboveSignal = false;
                            macds.add(new MACDSignalCross(in.getTradableIdentifier(), in.getTimestamp().getTime(), this.macdAboveSignal));
                         }
                     } else {
                        if (macdLine > signalLine) {
                            this.macdAboveSignal = true;
                            macds.add(new MACDSignalCross(in.getTradableIdentifier(), in.getTimestamp().getTime(), this.macdAboveSignal));
                         }
                     }
                 }
             }
         }
         
         return macds.toArray(new MACD[macds.size()]);
     }
 }
