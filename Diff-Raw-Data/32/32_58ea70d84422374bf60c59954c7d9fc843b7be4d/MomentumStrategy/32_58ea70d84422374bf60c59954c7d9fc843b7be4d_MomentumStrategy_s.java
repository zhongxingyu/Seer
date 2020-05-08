 package com.mns.mojoinvest.server.engine.strategy;
 
 import com.google.inject.Inject;
 import com.googlecode.objectify.NotFoundException;
 import com.mns.mojoinvest.server.engine.execution.Executor;
 import com.mns.mojoinvest.server.engine.model.Fund;
 import com.mns.mojoinvest.server.engine.model.Ranking;
 import com.mns.mojoinvest.server.engine.model.RankingParams;
 import com.mns.mojoinvest.server.engine.model.dao.FundDao;
 import com.mns.mojoinvest.server.engine.model.dao.RankingDao;
 import com.mns.mojoinvest.server.engine.portfolio.Portfolio;
 import com.mns.mojoinvest.server.engine.portfolio.PortfolioException;
 import com.mns.mojoinvest.server.engine.portfolio.Position;
 import com.mns.mojoinvest.server.util.TradingDayUtils;
 import com.mns.mojoinvest.shared.params.BacktestParams;
 import com.mns.mojoinvest.shared.params.MomentumStrategyParams;
 import org.joda.time.LocalDate;
 
 import java.math.BigDecimal;
 import java.math.MathContext;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 public class MomentumStrategy {
 
     private static final Logger log = Logger.getLogger(MomentumStrategy.class.getName());
 
     private final Executor executor;
     private final RankingDao rankingDao;
     private final FundDao fundDao;
 
     @Inject
     public MomentumStrategy(Executor executor,
                             RankingDao rankingDao,
                             FundDao fundDao) {
         this.executor = executor;
         this.rankingDao = rankingDao;
         this.fundDao = fundDao;
     }
 
     public void execute(Portfolio portfolio, BacktestParams backtestParams,
                         Set<Fund> acceptableFunds, MomentumStrategyParams strategyParams)
             throws StrategyException {
 
         LocalDate fromDate = new LocalDate(backtestParams.getFromDate());
         LocalDate toDate = new LocalDate(backtestParams.getToDate());
 
         if (fromDate.isAfter(toDate))
             throw new StrategyException("From date cannot be after to date");
 
         List<LocalDate> rebalanceDates = getRebalanceDates(fromDate, toDate, strategyParams);
 
         for (LocalDate rebalanceDate : rebalanceDates) {
             try {
                 Ranking ranking = rankingDao.get(rebalanceDate,
                         new RankingParams(strategyParams.getFormationPeriod()));
                 Collection<Fund> selection = getSelection(ranking.getSymbols(),
                         acceptableFunds, strategyParams);

                 sellLosers(portfolio, rebalanceDate, selection);
                 buyWinners(portfolio, strategyParams, rebalanceDate, selection);
             } catch (NotFoundException e) {
                 //TODO: How should we handle exceptions here - what type of exceptions are they?
                 log.info(rebalanceDate + " " + e.getMessage());
             } catch (StrategyException e) {
                 log.info(rebalanceDate + " " + e.getMessage());
             }
         }
     }
 
     private Collection<Fund> getSelection(List<String> ranked, Set<Fund> acceptableFunds,
                                           MomentumStrategyParams params) throws StrategyException {
 
        //TODO: This check should only take into account the number of funds in the acceptable set
         if (ranked.size() <= params.getPortfolioSize() * 2)
             throw new StrategyException("Not enough funds in population to make selection");
        Collection<Fund> funds = fundDao.get(ranked);
        Collection<Fund> selection = new ArrayList<Fund>(params.getPortfolioSize());
        for (Fund fund : funds) {
            if (acceptableFunds.contains(fund))
                selection.add(fund);
            if (selection.size() == params.getPortfolioSize()) {
                break;
            }
        }
        return selection;
     }
 
 
     private void sellLosers(Portfolio portfolio, LocalDate rebalanceDate, Collection<Fund> selection)
             throws StrategyException {
         for (Fund fund : portfolio.getActiveFunds(rebalanceDate)) {
             if (!selection.contains(fund)) {
                 try {
                     executor.sellAll(portfolio, fund, rebalanceDate);
                 } catch (PortfolioException e) {
                     throw new StrategyException("Unable to sell losers", e);
                 }
             }
         }
     }
 
     private void buyWinners(Portfolio portfolio, MomentumStrategyParams params, LocalDate rebalanceDate,
                             Collection<Fund> selection) throws StrategyException {
 
         BigDecimal numEmpty = new BigDecimal(params.getPortfolioSize() - portfolio.openPositionCount(rebalanceDate));
         BigDecimal availableCash = portfolio.getCash(rebalanceDate).
                 subtract(portfolio.getTransactionCost().
                         multiply(numEmpty));
//        log.info("Available cash: " + availableCash);
         for (Fund fund : selection) {
             if (!portfolio.contains(fund, rebalanceDate)) {
                 BigDecimal allocation = availableCash
                         .divide(numEmpty, MathContext.DECIMAL32);
                 try {
                     executor.buy(portfolio, fund, rebalanceDate, allocation);
                 } catch (PortfolioException e) {
                     throw new StrategyException("Unable to buy winners", e);
                 }
             }
         }
     }
 
     private List<LocalDate> getRebalanceDates(LocalDate fromDate, LocalDate toDate, MomentumStrategyParams params) {
         return TradingDayUtils.getMonthlySeries(fromDate, toDate, params.getHoldingPeriod(), true);
     }
 
     private void logPortfolio(Portfolio portfolio, LocalDate rebalanceDate) {
         for (Position position : portfolio.getOpenPositions(rebalanceDate).values()) {
             log.info(position.getFund()
                     + " shares: " + position.shares(rebalanceDate)
                     + ", marketValue: " + position.marketValue(rebalanceDate)
                     + ", returnsGain: " + position.totalReturn(rebalanceDate)
                     + ", gain%: " + position.gainPercentage(rebalanceDate));
 
         }
     }
 
 
 }
