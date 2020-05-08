 package com.mns.alphaposition.server.engine.strategy;
 
 import com.google.inject.Inject;
 import com.mns.alphaposition.server.engine.execution.Executor;
 import com.mns.alphaposition.server.engine.model.Fund;
 import com.mns.alphaposition.server.engine.portfolio.Portfolio;
 import com.mns.alphaposition.server.engine.portfolio.PortfolioProvider;
 import com.mns.alphaposition.server.engine.portfolio.Position;
 import com.mns.alphaposition.server.util.TradingDayUtils;
 import com.mns.alphaposition.shared.params.MomentumStrategyParams;
 import com.mns.alphaposition.shared.params.StrategyParams;
 import org.joda.time.LocalDate;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 public class MomentumStrategy implements TradingStrategy {
 
     private static final Logger log = Logger.getLogger(MomentumStrategy.class.getName());
 
     private final RankingStrategy rankingStrategy;
     private final PortfolioProvider portfolioProvider;
     private final Executor executor;
 
     @Inject
     public MomentumStrategy(RankingStrategy rankingStrategy, Executor executor,
                             PortfolioProvider portfolioProvider) {
         this.rankingStrategy = rankingStrategy;
         this.portfolioProvider = portfolioProvider;
         this.executor = executor;
     }
 
     @Override
     public void execute(LocalDate fromDate, LocalDate toDate, List<Fund> funds,
                         StrategyParams strategyParams) throws StrategyException {
 
         if (!supports(strategyParams))
             throw new StrategyException(this.getClass() + " doesn't support " + strategyParams.getClass());
         MomentumStrategyParams params = (MomentumStrategyParams) strategyParams;
 
         List<LocalDate> rebalanceDates = getRebalanceDates(fromDate, toDate, params);
 
         for (LocalDate rebalanceDate : rebalanceDates) {
             List<Fund> ranked = rankingStrategy.rank(rebalanceDate, funds, params.getRankingStrategyParams());
             List<Fund> selection;
             try {
                 selection = getSelection(ranked, params);
             } catch (StrategyException e) {
                 log.info(rebalanceDate + " " + e.getMessage());
                 continue;
             }
             log.info("** " + rebalanceDate + " **");
 
             sellLosers(rebalanceDate, selection);
             buyWinners(params, rebalanceDate, selection);
 
             for (Map.Entry<Fund, Position> e : portfolio().getActivePositions().entrySet()) {
                 log.info(e.getValue().getFund()
                         + " shares: " + e.getValue().shares()
                         + ", marketValue: " + e.getValue().marketValue(rebalanceDate)
                         + ", returnsGain: " + e.getValue().totalReturn(rebalanceDate)
                         + ", gain%: " + e.getValue().gainPercentage(rebalanceDate));
 
             }
 
             log.info("Overall return: " + portfolio().overallReturn(rebalanceDate));
         }
     }
 
     @Override
     public boolean supports(StrategyParams strategyParams) {
         return strategyParams instanceof MomentumStrategyParams;
     }
 
     private Portfolio portfolio() {
         return portfolioProvider.get();
     }
 
     private void sellLosers(LocalDate rebalanceDate, List<Fund> selection) {
         for (Fund fund : portfolio().getActiveHoldings()) {
             if (!selection.contains(fund)) {
                 executor.sellAll(fund, rebalanceDate);
             }
         }
     }
 
     private void buyWinners(MomentumStrategyParams params, LocalDate rebalanceDate, List<Fund> selection) {
 
         BigDecimal numEmpty = new BigDecimal(params.getPortfolioSize() - portfolio().numberOfActivePositions());
         BigDecimal availableCash = portfolio().getCash().
                 subtract(executor.getTransactionCost().
                         multiply(numEmpty));
         log.info("Available cash: " + availableCash);
         for (Fund fund : selection) {
             if (!portfolio().contains(fund)) {
                 BigDecimal allocation = availableCash
                         .divide(numEmpty, RoundingMode.HALF_DOWN);
                 executor.buy(fund, rebalanceDate, allocation);
             }
         }
     }
 
     private List<Fund> getSelection(List<Fund> ranked, MomentumStrategyParams params)
             throws StrategyException {
         if (ranked.size() <= params.getPortfolioSize() * 2)
             throw new StrategyException("Not enough funds in population to make selection");
         return ranked.subList(0, params.getPortfolioSize());
     }
 
     private List<LocalDate> getRebalanceDates(LocalDate fromDate, LocalDate toDate, MomentumStrategyParams params) {
         //TODO: should handle rebalance frequency unit here - strategyParams.getRebalanceFrequency()
        return TradingDayUtils.getMonthlySeries(fromDate, toDate, true);
     }
 
 
 }
