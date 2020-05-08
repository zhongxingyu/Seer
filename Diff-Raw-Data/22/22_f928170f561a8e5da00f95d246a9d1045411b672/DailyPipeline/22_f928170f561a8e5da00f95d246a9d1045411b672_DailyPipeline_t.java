 package com.mns.mojoinvest.server.pipeline;
 
 import com.google.appengine.tools.pipeline.FutureValue;
 import com.google.appengine.tools.pipeline.Job1;
 import com.google.appengine.tools.pipeline.Value;
 import com.mns.mojoinvest.server.engine.model.Fund;
 import com.mns.mojoinvest.server.engine.model.Quote;
 import com.mns.mojoinvest.server.pipeline.fund.FundFetcherJob;
 import com.mns.mojoinvest.server.pipeline.fund.FundUpdaterJob;
 import com.mns.mojoinvest.server.pipeline.quote.QuoteUpdaterJob;
 import com.mns.mojoinvest.server.pipeline.quote.QuotesFetcherJob;
 import org.joda.time.LocalDate;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 public class DailyPipeline extends Job1<List<Quote>, LocalDate> {
 
     private static final Logger log = Logger.getLogger(DailyPipeline.class.getName());
 
     @Override
     public Value<List<Quote>> run(LocalDate date) {
 
         FutureValue<List<Fund>> funds = futureCall(new FundFetcherJob());
         futureCall(new FundUpdaterJob(), funds);
         FutureValue<List<Quote>> quotes = futureCall(new QuotesFetcherJob(), funds, immediate(date));
         FutureValue<Boolean> quotesUpdated = futureCall(new QuoteUpdaterJob(), quotes);
 
 //        //for each of the parameter combinations (1M, 2M, 6M etc) call
 //        for (Integer integer : Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 18, 24)) {
 //            RankingParams params = new RankingParams(integer);
 //            futureCall(new PerformanceRankingJob(), immediate(date), immediate(params), waitFor(quotesUpdated));
 //        }
 
        //Send email for confirmation of success or failure

         return null;
     }
 
 
 }
