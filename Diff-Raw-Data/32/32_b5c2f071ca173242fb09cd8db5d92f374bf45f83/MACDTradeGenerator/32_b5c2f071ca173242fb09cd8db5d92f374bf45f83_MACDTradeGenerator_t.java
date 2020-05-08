 
 package com.zygon.trade.agent.trade;
 
 import com.xeiam.xchange.currency.Currencies;
 import com.zygon.trade.market.model.indication.Indication;
 import com.zygon.trade.market.model.indication.market.Direction;
 import com.zygon.trade.market.model.indication.market.Direction.MarketDirection;
 import com.zygon.trade.market.model.indication.market.MACDSignalCross;
 import com.zygon.trade.market.model.indication.market.MACDZeroCross;
 import com.zygon.trade.trade.PriceObjective;
 import com.zygon.trade.trade.Trade;
 import com.zygon.trade.trade.TradeSignal;
import com.zygon.trade.trade.TradeSignal.Decision;
 import com.zygon.trade.trade.TradeUrgency;
 import com.zygon.trade.trade.VolumeObjective;
 import java.util.ArrayList;
 import java.util.Collection;
 
 /**
  *
  * @author zygon
  */
 public class MACDTradeGenerator extends TradeGeneratorImpl {
 
//    private static final double PROFIT_MODIFIER         = 7.5;
//    private static final double STOP_LOSS_MODIFER       = 0.5;
     
    private static final double PROFIT_MODIFIER         = 1600;
    private static final double STOP_LOSS_MODIFER       = 40;
    
    
    private static final double VOLUME_MODIFER          = 2;
     
     public MACDTradeGenerator() {
     }
 
     @Override
     protected void getAdditionalIndications(Collection<Indication> additional) {
         MarketDirection marketDirection = this.getMarketDirection();
         
         additional.add(new Direction(Currencies.BTC, System.currentTimeMillis(), marketDirection));
     }
     
     private Direction getDirection() {
         return (Direction) this.getMarketConditions().getIndication(Direction.ID, Currencies.BTC);
     }
     
     private MACDZeroCross getZeroCross() {
         return (MACDZeroCross) this.getMarketConditions().getIndication(MACDZeroCross.ID, Currencies.BTC);
     }
     
     private MACDSignalCross getSignalCross() {
         return (MACDSignalCross) this.getMarketConditions().getIndication(MACDSignalCross.ID, Currencies.BTC);
     }
     
     // MarketDirection should probably be introduced as a higher level concept as well.
     private MarketDirection getMarketDirection() {
         MACDZeroCross zeroCross = this.getZeroCross();
         MACDSignalCross signalCross = this.getSignalCross();
         
         MarketDirection dir = null;
         
         if (zeroCross != null && signalCross != null) {
             if (zeroCross.crossAboveZero() && signalCross.crossAboveSignal()) {
                 dir = MarketDirection.UP;
             } else if (!zeroCross.crossAboveZero() && !signalCross.crossAboveSignal()) {
                 dir = MarketDirection.DOWN;
             }
         }
 
         if (dir == null) {
             dir = MarketDirection.NEUTRAL; // maybe neutral - maybe unsure.
         }
         
         return dir;
     }
     
     private TradeSignal createTradeSignal() {
         
         MarketDirection marketDirection = this.getDirection().getMarketDirection();
         
        Decision decision = null;
         
         if (marketDirection == MarketDirection.UP) {
            decision = Decision.SELL;
         } else if (marketDirection == MarketDirection.DOWN) {
            decision = Decision.BUY;
         }
         
        if (decision != null) {
            PriceObjective priceObjective = new PriceObjective(decision.getType(), PriceObjective.Modifier.PIP, PROFIT_MODIFIER, STOP_LOSS_MODIFER);
             VolumeObjective volumeObjective = new VolumeObjective(VolumeObjective.Modifier.PERCENT, VOLUME_MODIFER);
            TradeSignal tradeSignal = new TradeSignal(decision, volumeObjective, "BTC", "USD", priceObjective, TradeUrgency.MEDIUM, marketDirection.name());
 
             return tradeSignal;
         }
         
         return null;
     }
     
     @Override
     public Collection<Trade> getTrades() {
          
         Collection<Trade> trades = new ArrayList<>();
         
         TradeSignal tradeSignal = createTradeSignal();
         if (tradeSignal != null) {
             trades.add(new Trade(tradeSignal));
         } 
         // This is just for fun
 //        else {
 //            PriceObjective priceObjective = new PriceObjective(TradeSignal.Decision.BUY.getType(), PriceObjective.Modifier.PERCENT, PROFIT_MODIFIER, STOP_LOSS_MODIFER);
 //            VolumeObjective volumeObjective = new VolumeObjective(VolumeObjective.Modifier.PERCENT, VOLUME_MODIFER);
 //            tradeSignal = new TradeSignal(TradeSignal.Decision.BUY, volumeObjective, "BTC", "USD", priceObjective, TradeUrgency.MEDIUM, "test");
 //            
 //            trades.add(new Trade(tradeSignal));
 //        }
         
         return trades;
     }
 }
