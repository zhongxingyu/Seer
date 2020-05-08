 package com.mns.mojoinvest.server.engine.strategy;
 
 import com.google.inject.Inject;
 import com.mns.mojoinvest.server.engine.model.CalculatedValue;
 import com.mns.mojoinvest.server.engine.model.Fund;
 import com.mns.mojoinvest.server.engine.model.dao.CalculatedValueDao;
 import com.mns.mojoinvest.server.engine.params.Params;
 import org.joda.time.LocalDate;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.*;
 import java.util.logging.Logger;
 
 public class RelativeStrengthCalculator {
 
     private static final Logger log = Logger.getLogger(RelativeStrengthCalculator.class.getName());
 
     private final CalculatedValueDao calculatedValueDao;
 
     @Inject
     public RelativeStrengthCalculator(CalculatedValueDao dao) {
         this.calculatedValueDao = dao;
     }
 
     public SortedMap<LocalDate, Map<String, BigDecimal>> getRelativeStrengthsMA(Collection<Fund> funds, Params params,
                                                                                 List<LocalDate> dates) {
 
         log.info("Starting load of calculated values");
         Collection<CalculatedValue> ma1s = calculatedValueDao.get(dates, funds,
                 "SMA", params.getMa1());
         Collection<CalculatedValue> ma2s = calculatedValueDao.get(dates, funds,
                 "SMA", params.getMa2());
 
         log.info("Building intermediate data structures");
         Map<LocalDate, Map<String, BigDecimal>> ma1Map = buildDateCalcValueMap(ma1s);
         Map<LocalDate, Map<String, BigDecimal>> ma2Map = buildDateCalcValueMap(ma2s);
 
         SortedMap<LocalDate, Map<String, BigDecimal>> allRs = new TreeMap<LocalDate, Map<String, BigDecimal>>();
         for (LocalDate date : dates) {
 
             Map<String, BigDecimal> rs = new HashMap<String, BigDecimal>();
             if (ma1Map.containsKey(date) && ma2Map.containsKey(date)) {
                 Map<String, BigDecimal> ma1vals = ma1Map.get(date);
                 Map<String, BigDecimal> ma2vals = ma2Map.get(date);
 
                 for (Fund fund : funds) {
                     String symbol = fund.getSymbol();
                     if (!ma1vals.containsKey(symbol)) {
                         log.fine(date + " Unable to calculate RS for " + symbol + " on " + date + " no SMA|" + params.getMa1());
                     } else if (!ma2vals.containsKey(symbol)) {
                         log.fine(date + " Unable to calculate RS for " + symbol + " on " + date + " no SMA|" + params.getMa2());
                     } else {
                         rs.put(symbol, ma1vals.get(symbol).divide(ma2vals.get(symbol), RoundingMode.HALF_EVEN));
                     }
 
                 }
             }
             allRs.put(date, rs);
         }
         return allRs;
     }
 
 
     public SortedMap<LocalDate, Map<String, BigDecimal>> getRelativeStrengthsROC(Collection<Fund> funds, Params params, List<LocalDate> dates) {
         Collection<CalculatedValue> rocs = calculatedValueDao.get(dates, funds, "ROC", params.getRoc());
         SortedMap<LocalDate, Map<String, BigDecimal>> strengths = buildDateCalcValueMap(rocs);
         for (LocalDate date : dates) {
             if (strengths.get(date) == null) {
                 log.warning(date + " No ROC values calculated");
                 strengths.put(date, new HashMap<String, BigDecimal>());
             }
         }
         return strengths;
     }
 
     public SortedMap<LocalDate, Map<String, BigDecimal>> getRelativeStrengthAlpha(Collection<Fund> funds, Params params, List<LocalDate> dates) {
         Collection<CalculatedValue> alphas = calculatedValueDao.get(dates, funds, "ALPHA", params.getAlpha());
         SortedMap<LocalDate, Map<String, BigDecimal>> strengths = buildDateCalcValueMap(alphas);
         for (LocalDate date : dates) {
             if (strengths.get(date) == null) {
                 log.warning(date + " No ALPHA values calculated");
                 strengths.put(date, new HashMap<String, BigDecimal>());
             }
         }
         return strengths;
     }
 
     public SortedMap<LocalDate, Map<String, BigDecimal>> adjustRelativeStrengths(SortedMap<LocalDate, Map<String, BigDecimal>> unadjusted,
                                                                                  Collection<Fund> funds, Params params,
                                                                                  List<LocalDate> dates) {
 
         SortedMap<LocalDate, Map<String, BigDecimal>> adjusted = new TreeMap<LocalDate, Map<String, BigDecimal>>();
         Collection<CalculatedValue> stddevs = calculatedValueDao.get(dates, funds, "STDDEV", params.getStdDev());
         Map<LocalDate, Map<String, BigDecimal>> stddevMap = buildDateCalcValueMap(stddevs);
         for (LocalDate date : unadjusted.keySet()) {
             Map<String, BigDecimal> adjustedDate = new HashMap<String, BigDecimal>();
             for (String symbol : unadjusted.get(date).keySet()) {
                 if (stddevMap.containsKey(date) && stddevMap.get(date).containsKey(symbol)) {
                     adjustedDate.put(symbol, unadjusted.get(date).get(symbol)
                             .divide(stddevMap.get(date).get(symbol), RoundingMode.HALF_EVEN));
                 } else {
                     log.warning(date + " Unable to calculate adjusted RS for " + symbol + " on " + date + " no STDDEV|" + params.getStdDev());
                 }
             }
             adjusted.put(date, adjustedDate);
         }
         return adjusted;
     }
 
 
     private SortedMap<LocalDate, Map<String, BigDecimal>> buildDateCalcValueMap(Collection<CalculatedValue> vals) {
         SortedMap<LocalDate, Map<String, BigDecimal>> valMap = new TreeMap<LocalDate, Map<String, BigDecimal>>();
         for (CalculatedValue val : vals) {
             if (!valMap.containsKey(val.getDate())) {
                 valMap.put(val.getDate(), new HashMap<String, BigDecimal>());
             }
             valMap.get(val.getDate()).put(val.getSymbol(), val.getValue());
         }
         return valMap;
     }
 }
