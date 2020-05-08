 package com.mns.mojoinvest.server.engine.execution;
 
 import com.google.inject.Inject;
 import com.mns.mojoinvest.server.engine.model.Fund;
 import com.mns.mojoinvest.server.engine.model.Quote;
 import com.mns.mojoinvest.server.engine.model.dao.QuoteDao;
 import com.mns.mojoinvest.server.engine.portfolio.Portfolio;
 import com.mns.mojoinvest.server.engine.portfolio.PortfolioException;
 import com.mns.mojoinvest.server.engine.portfolio.Position;
 import com.mns.mojoinvest.server.engine.transaction.BuyTransaction;
 import com.mns.mojoinvest.server.engine.transaction.SellTransaction;
 import org.joda.time.LocalDate;
 
 import java.math.BigDecimal;
 import java.util.logging.Logger;
 
 public class NextTradingDayExecutor implements Executor {
 
     private static final Logger log = Logger.getLogger(NextTradingDayExecutor.class.getName());
 
     private QuoteDao quoteDao;
 
     @Inject
     public NextTradingDayExecutor(QuoteDao quoteDao) {
         this.quoteDao = quoteDao;
     }
 
     @Override
     public void buy(Portfolio portfolio, Fund fund, LocalDate date, BigDecimal allocation)
             throws PortfolioException {
         //TODO: getRanking execution price should be mid between open and close
         Quote executionQuote = quoteDao.get(fund, date);
         BigDecimal shares = allocation.divide(executionQuote.getClose(), 0, BigDecimal.ROUND_DOWN);
//        log.info("Buying " + fund + " amount: " + allocation +
//                ", price: " + executionQuote.getClose() + ", shares: " + shares);
         BuyTransaction tx = new BuyTransaction(fund, date, shares,
                 executionQuote.getClose(), portfolio.getTransactionCost());
         portfolio.add(tx);
     }
 
     @Override
     public void sellAll(Portfolio portfolio, Fund fund, LocalDate date)
             throws PortfolioException {
         //TODO: getRanking execution price should be mid between open and close
         Quote executionQuote = quoteDao.get(fund, date);
         Position position = portfolio.getPosition(fund);
//        log.info("Selling " + fund + " price: " + executionQuote.getClose() +
//                ", gain%: " + position.gainPercentage(date));
         SellTransaction tx = new SellTransaction(fund, date, position.shares(date),
                 executionQuote.getClose(), portfolio.getTransactionCost());
         portfolio.add(tx);
     }
 }
