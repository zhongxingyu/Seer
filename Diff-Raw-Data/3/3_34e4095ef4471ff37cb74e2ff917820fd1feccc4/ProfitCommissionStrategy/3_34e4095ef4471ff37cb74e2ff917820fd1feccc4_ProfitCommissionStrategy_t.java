 package org.pillarone.riskanalytics.domain.pc.reinsurance.commissions;
 
 import org.pillarone.riskanalytics.domain.pc.claims.Claim;
 import org.pillarone.riskanalytics.domain.pc.underwriting.UnderwritingInfo;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author ben.ginsberg (at) intuitive-collaboration (dot) com
  */
 public class ProfitCommissionStrategy implements ICommissionStrategy {
 
     private double profitCommissionRatio = 0d;
     private double costRatio = 0d;
     private boolean lossCarriedForwardEnabled = true;
     private double initialLossCarriedForward = 0d;
    /**
     * not a parameter but updated during calculateCommission() to avoid side effect for the parameter variable
     */
     private double lossCarriedForward = 0d;
 
     public Object getType() {
         return CommissionStrategyType.PROFITCOMMISSION;
     }
 
     public Map getParameters() {
         Map<String, Object> map = new HashMap<String, Object>(4);
         map.put("profitCommissionRatio", profitCommissionRatio);
         map.put("costRatio", costRatio);
         map.put("lossCarriedForwardEnabled", lossCarriedForwardEnabled);
         map.put("initialLossCarriedForward", initialLossCarriedForward);
         return map;
     }
 
     public void calculateCommission(List<Claim> claims, List<UnderwritingInfo> underwritingInfos, boolean firstPeriod) {
         if (lossCarriedForwardEnabled && firstPeriod) {
             lossCarriedForward = initialLossCarriedForward;
         }
         double incurredClaims = 0d;
         for (Claim claim: claims) {
             incurredClaims += claim.getUltimate();
         }
         double totalPremiumWritten = 0d;
         for (UnderwritingInfo underwritingInfo : underwritingInfos) {
             totalPremiumWritten += underwritingInfo.getPremiumWritten();
         }
         double nextLossCarriedForward = totalPremiumWritten * (1d - costRatio) - incurredClaims;
         double totalCommission =  profitCommissionRatio * Math.max(0d, nextLossCarriedForward - lossCarriedForward);
         for (UnderwritingInfo underwritingInfo : underwritingInfos) {
             underwritingInfo.setCommission(underwritingInfo.getCommission() +
                 totalCommission * underwritingInfo.getPremiumWritten() / totalPremiumWritten);
         }
         lossCarriedForward = lossCarriedForwardEnabled ? Math.min(0d, nextLossCarriedForward) : 0d;
     }
 }
