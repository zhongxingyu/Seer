 package org.pillarone.riskanalytics.domain.pc.reinsurance.commissions;
 
 import org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter;
 import org.pillarone.riskanalytics.core.parameterization.ConstraintsFactory;
 import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier;
 import org.pillarone.riskanalytics.core.util.GroovyUtils;
 import org.pillarone.riskanalytics.domain.pc.claims.Claim;
 import org.pillarone.riskanalytics.domain.pc.underwriting.UnderwritingInfo;
 import org.pillarone.riskanalytics.domain.utils.constraints.DoubleConstraints;
 
 import java.util.*;
 
 /**
  * @author shartmann (at) munichre (dot) com
  */
 public class SlidingCommissionStrategy implements ICommissionStrategy {
 
     public static final String LOSS_RATIO = "Loss Ratio (from)";
     public static final String COMMISSION = "Commission";
     
     private ConstrainedMultiDimensionalParameter commissionBands = new ConstrainedMultiDimensionalParameter(
             GroovyUtils.convertToListOfList(new Object[]{0d, 0d}),
             Arrays.asList(LOSS_RATIO, COMMISSION),
             ConstraintsFactory.getConstraints(DoubleConstraints.IDENTIFIER));
 
     public IParameterObjectClassifier getType() {
         return CommissionStrategyType.SLIDINGCOMMISSION;
     }
 
     public Map getParameters() {
         Map<String, Object> map = new HashMap<String, Object>(1);
        map.put("commission bands", commissionBands);
         return map;
     }
 
     public void calculateCommission(List<Claim> claims, List<UnderwritingInfo> underwritingInfos, boolean isFirstPeriod, boolean isAdditive) {
         double totalClaims = 0d;
         double totalPremium = 0d;
         for (Claim claim : claims) {
             totalClaims += claim.getUltimate();
         }
         for (UnderwritingInfo uwInfo : underwritingInfos) {
             totalPremium += uwInfo.getPremiumWritten();
         }
         double totalLossRatio = totalClaims / totalPremium;
         LinkedHashMap<Double, Double> commissionRates = getCommissionRates();
         double totalCommission = 0d;
         double previousCommission = 0d;
 
         for (Map.Entry<Double, Double> entry : commissionRates.entrySet()) {
             double lossRatio = entry.getKey();
             double commission = entry.getValue();
             if (lossRatio > totalLossRatio) {
                 totalCommission = previousCommission * totalPremium;
                 break;
             }
             previousCommission = commission;
         }
         if (isAdditive) {
             for (UnderwritingInfo uwInfo : underwritingInfos) {
                 double shareOfTotalPremium = uwInfo.getPremiumWritten() / totalPremium;
                 uwInfo.setCommission(uwInfo.getCommission() + shareOfTotalPremium * totalCommission);
             }
         }
         else {
             for (UnderwritingInfo uwInfo : underwritingInfos) {
                 double shareOfTotalPremium = uwInfo.getPremiumWritten() / totalPremium;
                 uwInfo.setCommission(shareOfTotalPremium * totalCommission);
             }
         }
     }
 
     /**
      * 
      * @return key: loss ratio (from), value: commission
      */
     private LinkedHashMap<Double, Double> getCommissionRates() {
         int numberOfBands = commissionBands.getValueRowCount();
         LinkedHashMap<Double, Double> commissionRates = new LinkedHashMap<Double, Double>(numberOfBands);
         int columnLossRatio = commissionBands.getColumnIndex(LOSS_RATIO);
         int columnCommission = commissionBands.getColumnIndex(COMMISSION);
         double previousLossRatio = -1d;
         for (int row = 1; row <= numberOfBands; row++) {
             double lossRatio = (Double) commissionBands.getValueAt(row, columnLossRatio);
             if (lossRatio <= previousLossRatio) {
                 throw new IllegalArgumentException("Loss ratios must be strictly increasing");
             }
 
             double commission = (Double) commissionBands.getValueAt(row, columnCommission);
             commissionRates.put(lossRatio, commission);
             previousLossRatio = lossRatio;
         }
         return commissionRates;
     } 
         
 }
