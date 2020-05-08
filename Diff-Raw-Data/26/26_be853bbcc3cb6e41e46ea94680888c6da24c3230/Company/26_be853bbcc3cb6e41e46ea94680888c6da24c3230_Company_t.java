 package org.pillarone.riskanalytics.domain.pc.company;
 
import org.pillarone.riskanalytics.core.components.AbstractStore;
 import org.pillarone.riskanalytics.core.components.Component;
 import org.pillarone.riskanalytics.core.components.MultiPhaseComponent;
import org.pillarone.riskanalytics.core.components.PeriodStore;
 import org.pillarone.riskanalytics.core.packets.PacketList;
 import org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter;
 import org.pillarone.riskanalytics.domain.assets.constants.Rating;
 import org.pillarone.riskanalytics.domain.pc.assetLiabilityMismatch.CompanyConfigurableAssetLiabilityMismatchGenerator;
 import org.pillarone.riskanalytics.domain.pc.claims.Claim;
 import org.pillarone.riskanalytics.domain.pc.constraints.CompanyPortion;
 import org.pillarone.riskanalytics.domain.pc.creditrisk.DefaultProbabilities;
 import org.pillarone.riskanalytics.domain.pc.creditrisk.ReinsurerDefault;
 import org.pillarone.riskanalytics.domain.pc.lob.CompanyConfigurableLobWithReserves;
 import org.pillarone.riskanalytics.domain.pc.lob.LobMarker;
 import org.pillarone.riskanalytics.domain.pc.reinsurance.contracts.IReinsuranceContractMarker;
 import org.pillarone.riskanalytics.domain.pc.reinsurance.contracts.MultiCompanyCoverAttributeReinsuranceContract;
 import org.pillarone.riskanalytics.domain.pc.reserves.fasttrack.ClaimDevelopmentLeanPacket;
 import org.pillarone.riskanalytics.domain.pc.underwriting.UnderwritingInfo;
 import org.pillarone.riskanalytics.domain.pc.underwriting.UnderwritingInfoUtilities;
 import org.pillarone.riskanalytics.domain.utils.IRandomNumberGenerator;
 import org.pillarone.riskanalytics.domain.utils.RandomNumberGeneratorFactory;
 import umontreal.iro.lecuyer.probdist.BinomialDist;
 
 import java.util.HashMap;
 import java.util.Map;
 
 
 /**
  * Class company is a component of the MultiCompany-model to obtain aggregated claims and
  * underwriting information associated to the instantiated company.
  * <p/>
  * The incoming information is filtered with respect to engaged companies,
  * matches with the instance company entail a modification of its aggregated portfolio.
  * Attention should be paid to the case where one of the reinsurers of a ceded claim or underwriting information
  * equals the instance company. This situation leads to a (portion-weighted) increase of the aggregated gross
  * claim, respectively underwriting information. The incoming claims (analogously
  * incoming underwriting information) are filtered by means of the parameters parmCompany and parmReinsurers
  * provided by the components lineOfBusiness and reinsuranceContract.
  * <p/>
  * Implementation Note: For the query algorithm to correctly function, it is crucial that the components line of
  * business and reinsurance contract are instances of classes CompanyConfigurableLobWithReserves and
  * MultiCompanyCoverAttributeReinsuranceContract, respectively.
  *
  * @author stefan.kunz (at) intuitive-collaboration (dot) com
  */
 public class Company extends MultiPhaseComponent implements ICompanyMarker {
 
     private PacketList<DefaultProbabilities> inDefaultProbability = new PacketList<DefaultProbabilities>(DefaultProbabilities.class);
 
     private PacketList<Claim> inClaimsGross = new PacketList<Claim>(Claim.class);
     private PacketList<Claim> inClaimsCeded = new PacketList<Claim>(Claim.class);
     private PacketList<UnderwritingInfo> inUnderwritingInfoGross = new PacketList<UnderwritingInfo>(UnderwritingInfo.class);
     private PacketList<UnderwritingInfo> inUnderwritingInfoCeded = new PacketList<UnderwritingInfo>(UnderwritingInfo.class);
 
     private PacketList<ReinsurerDefault> outReinsurersDefault = new PacketList<ReinsurerDefault>(ReinsurerDefault.class);
 
     private PacketList<Claim> outClaimsGross = new PacketList<Claim>(Claim.class);
     private PacketList<Claim> outClaimsGrossPrimaryInsurer = new PacketList<Claim>(Claim.class);
     private PacketList<Claim> outClaimsGrossReinsurer = new PacketList<Claim>(Claim.class);
     private PacketList<Claim> outClaimsCeded = new PacketList<Claim>(Claim.class);
     private PacketList<Claim> outClaimsNet = new PacketList<Claim>(Claim.class);
     private PacketList<Claim> outClaimsNetPrimaryInsurer = new PacketList<Claim>(Claim.class);
 
     // todo(sku): remove the following and related lines as soon as PMO-648 is resolved
     private PacketList<ClaimDevelopmentLeanPacket> outClaimsLeanDevelopmentGross = new PacketList<ClaimDevelopmentLeanPacket>(ClaimDevelopmentLeanPacket.class);
     private PacketList<ClaimDevelopmentLeanPacket> outClaimsLeanDevelopmentGrossPrimaryInsurer = new PacketList<ClaimDevelopmentLeanPacket>(ClaimDevelopmentLeanPacket.class);
     private PacketList<ClaimDevelopmentLeanPacket> outClaimsLeanDevelopmentGrossReinsurer = new PacketList<ClaimDevelopmentLeanPacket>(ClaimDevelopmentLeanPacket.class);
     private PacketList<ClaimDevelopmentLeanPacket> outClaimsLeanDevelopmentCeded = new PacketList<ClaimDevelopmentLeanPacket>(ClaimDevelopmentLeanPacket.class);
     private PacketList<ClaimDevelopmentLeanPacket> outClaimsLeanDevelopmentNet = new PacketList<ClaimDevelopmentLeanPacket>(ClaimDevelopmentLeanPacket.class);
     private PacketList<ClaimDevelopmentLeanPacket> outClaimsLeanDevelopmentNetPrimaryInsurer = new PacketList<ClaimDevelopmentLeanPacket>(ClaimDevelopmentLeanPacket.class);
     private PacketList<UnderwritingInfo> outUnderwritingInfoGross = new PacketList<UnderwritingInfo>(UnderwritingInfo.class);
     private PacketList<UnderwritingInfo> outUnderwritingInfoGrossPrimaryInsurer = new PacketList<UnderwritingInfo>(UnderwritingInfo.class);
     private PacketList<UnderwritingInfo> outUnderwritingInfoGrossReinsurer = new PacketList<UnderwritingInfo>(UnderwritingInfo.class);
     private PacketList<UnderwritingInfo> outUnderwritingInfoCeded = new PacketList<UnderwritingInfo>(UnderwritingInfo.class);
     private PacketList<UnderwritingInfo> outUnderwritingInfoNet = new PacketList<UnderwritingInfo>(UnderwritingInfo.class);
     private PacketList<UnderwritingInfo> outUnderwritingInfoNetPrimaryInsurer = new PacketList<UnderwritingInfo>(UnderwritingInfo.class);
 
     private PacketList<Claim> inFinancialResults = new PacketList<Claim>(Claim.class);
     private PacketList<Claim> outFinancialResults = new PacketList<Claim>(Claim.class);
 
 
     /**
      * This parameter is currently not used for any calculation. It may be used for default modeling as in DCEM.
      * Reason for adding it: Components addable in a DynamicComposedComponent need at least one parameter.
      */
     private Rating parmRating = Rating.NO_DEFAULT;
 
     /**
      * contains the covered portion of <tt>this</tt> company per contract
      */
     private Map<IReinsuranceContractMarker, Double> coveredPortionPerContract = new HashMap<IReinsuranceContractMarker, Double>();
     
     private IRandomNumberGenerator generator = RandomNumberGeneratorFactory.getBinomialGenerator();
     private static final String PHASE_DEFAULT = "Phase Default";
     private static final String PHASE_AGGREGATION = "Phase Aggregation";
 
    private PeriodStore periodStore;
    private static final String IS_DEFAULT = "IS_DEFAULT";

     @Override
     public void doCalculation(String phase) {
         if (phase.equals(PHASE_DEFAULT)) {
             doCalculationDefault();
         }
         else if (phase.equals(PHASE_AGGREGATION)) {
             doCalculationAggregation();
         }
     }
     
     private void doCalculationDefault() {
         Map<Rating, Double> defaultProbabilities = inDefaultProbability.get(0).defaultProbability;
         boolean isReinsurerDefault = defaultOfReinsurer(defaultProbabilities.get(parmRating));
         ReinsurerDefault reinsurerDefault = new ReinsurerDefault(getNormalizedName(), isReinsurerDefault);
         outReinsurersDefault.add(reinsurerDefault);
     }
     
     private boolean defaultOfReinsurer(double probability) {
        Boolean isDefault = (Boolean) periodStore.get(IS_DEFAULT, AbstractStore.LAST_PERIOD);
        if (isDefault == null || !isDefault) {
            ((BinomialDist) generator.getDistribution()).setParams(1, probability);
            isDefault = ((Integer) generator.nextValue()) == 1;
        }
        periodStore.put(IS_DEFAULT, isDefault);
        return isDefault;        
     }
     
     private void doCalculationAggregation() {
         try {
             Claim aggregateClaimGrossPI;
             Claim aggregateClaimCeded;
             Claim aggregateClaimGrossRI;
             if (inClaimsGross.size() > 0) {
                 aggregateClaimGrossPI = inClaimsGross.get(0).getClass().newInstance();
                 aggregateClaimCeded = inClaimsGross.get(0).getClass().newInstance();
                 aggregateClaimGrossRI = inClaimsGross.get(0).getClass().newInstance();
             } else {
                 aggregateClaimGrossPI = (Claim) inClaimsGross.getType().newInstance();
                 aggregateClaimCeded = (Claim) inClaimsGross.getType().newInstance();
                 aggregateClaimGrossRI = (Claim) inClaimsGross.getType().newInstance();
             }
             for (Claim claim : inClaimsGross) {
                 // the line of business belongs to this company and therefore the gross claim is added to this company
                 if (isCompanyClaim(claim)) {
                     aggregateClaimGrossPI.plus(claim);
                 }
             }
 
             for (Claim claim : inClaimsCeded) {
                 IReinsuranceContractMarker contract = claim.getReinsuranceContract();
                 if (isCompanyClaim(claim)) {
                     aggregateClaimCeded.plus(claim);
                 }
                 // check if this company was the reinsurer of a ceded claim, if true add the claim to the gross claim list
                 else if (contract instanceof MultiCompanyCoverAttributeReinsuranceContract) {
                     Claim coveredClaimByThisCompany = claim.copy();
                     coveredClaimByThisCompany.scale(getCoveredPortion(contract));
                     aggregateClaimGrossRI.plus(coveredClaimByThisCompany);
                 }
             }
 
             outClaimsGrossPrimaryInsurer.add(aggregateClaimGrossPI);
             outClaimsCeded.add(aggregateClaimCeded);
             outClaimsGrossReinsurer.add(aggregateClaimGrossRI);
             Claim aggregateClaimGross = aggregateClaimGrossPI.copy();
             aggregateClaimGross.plus(aggregateClaimGrossRI);
             outClaimsGross.add(aggregateClaimGross);
             Claim aggregateClaimNet = aggregateClaimGross.copy();
             aggregateClaimNet.minus(aggregateClaimCeded);
             outClaimsNet.add(aggregateClaimNet);
             Claim aggregateClaimNetPI = aggregateClaimGrossPI.copy();
             aggregateClaimNetPI.minus(aggregateClaimCeded);
             outClaimsNetPrimaryInsurer.add(aggregateClaimNetPI);
 
             if (aggregateClaimGross instanceof ClaimDevelopmentLeanPacket) {
                 outClaimsLeanDevelopmentGrossPrimaryInsurer.add((ClaimDevelopmentLeanPacket) aggregateClaimGrossPI);
                 outClaimsLeanDevelopmentGrossReinsurer.add((ClaimDevelopmentLeanPacket) aggregateClaimGrossRI);
                 outClaimsLeanDevelopmentGross.add((ClaimDevelopmentLeanPacket) aggregateClaimGross);
                 outClaimsLeanDevelopmentCeded.add((ClaimDevelopmentLeanPacket) aggregateClaimCeded);
                 outClaimsLeanDevelopmentNet.add((ClaimDevelopmentLeanPacket) aggregateClaimNet);
                 outClaimsLeanDevelopmentNetPrimaryInsurer.add((ClaimDevelopmentLeanPacket) aggregateClaimNetPI);
             }
 
             // underwriting info calculations:
             UnderwritingInfo aggregatedUnderwritingInfoGrossPI;
             UnderwritingInfo aggregatedUnderwritingInfoGrossRI;
             UnderwritingInfo aggregatedUnderwritingInfoCeded;
             if (inUnderwritingInfoGross.size() > 0) {
                 aggregatedUnderwritingInfoGrossPI = inUnderwritingInfoGross.get(0).getClass().newInstance();
                 aggregatedUnderwritingInfoGrossRI = inUnderwritingInfoGross.get(0).getClass().newInstance();
                 aggregatedUnderwritingInfoCeded = inUnderwritingInfoGross.get(0).getClass().newInstance();
             } else {
                 aggregatedUnderwritingInfoGrossPI = (UnderwritingInfo) inUnderwritingInfoGross.getType().newInstance();
                 aggregatedUnderwritingInfoGrossRI = (UnderwritingInfo) inUnderwritingInfoGross.getType().newInstance();
                 aggregatedUnderwritingInfoCeded = (UnderwritingInfo) inUnderwritingInfoGross.getType().newInstance();
             }
             for (UnderwritingInfo underwritingInfo : inUnderwritingInfoGross) {
                 // the line of business belongs to this company and therefore the gross underwriting info is added to this company
                 if (isCompanyUnderwritingInfo(underwritingInfo)) {
                     aggregatedUnderwritingInfoGrossPI.plus(underwritingInfo);
                 }
             }
 
             for (UnderwritingInfo underwritingInfo : inUnderwritingInfoCeded) {
                 IReinsuranceContractMarker contract = underwritingInfo.getReinsuranceContract();
                 if (isCompanyUnderwritingInfo(underwritingInfo)) {
                     aggregatedUnderwritingInfoCeded.plus(underwritingInfo);
                 }
                 // check if this company was the reinsurer of a ceded underwriting info, if true add up the underwriting info to the aggregate gross
                 else if (contract instanceof MultiCompanyCoverAttributeReinsuranceContract) {
                     UnderwritingInfo underwritingInfoOfThisCompany = (UnderwritingInfo) underwritingInfo.copy();
                     underwritingInfoOfThisCompany.scale(getCoveredPortion(contract));
                     aggregatedUnderwritingInfoGrossRI.plus(underwritingInfoOfThisCompany);
                 }
             }
 
             outUnderwritingInfoGrossPrimaryInsurer.add(aggregatedUnderwritingInfoGrossPI);
             outUnderwritingInfoGrossReinsurer.add(aggregatedUnderwritingInfoGrossRI);
             outUnderwritingInfoCeded.add(aggregatedUnderwritingInfoCeded);
             UnderwritingInfo aggregatedUnderwritingInfoGross = (UnderwritingInfo) aggregatedUnderwritingInfoGrossPI.copy();
             aggregatedUnderwritingInfoGross.plus(aggregatedUnderwritingInfoGrossRI);
             outUnderwritingInfoGross.add(aggregatedUnderwritingInfoGross);
             UnderwritingInfo aggregatedUnderwritingInfoNet = UnderwritingInfoUtilities.calculateNet(aggregatedUnderwritingInfoGross, aggregatedUnderwritingInfoCeded);
             outUnderwritingInfoNet.add(aggregatedUnderwritingInfoNet);
             UnderwritingInfo aggregatedUnderwritingInfoNetPI = UnderwritingInfoUtilities.calculateNet(aggregatedUnderwritingInfoGrossPI, aggregatedUnderwritingInfoCeded);
             outUnderwritingInfoNetPrimaryInsurer.add(aggregatedUnderwritingInfoNetPI);
 
             Claim aggregateFinancialResult;
             if (inFinancialResults.size() > 0) {
                 aggregateFinancialResult = inFinancialResults.get(0).getClass().newInstance();
             } else {
                 aggregateFinancialResult = (Claim) inFinancialResults.getType().newInstance();
             }
             for (Claim financialResult : inFinancialResults) {
                 if (isCompanyFinancialResult(financialResult)) {
                     aggregateFinancialResult.plus(financialResult);
                 }
             }
             outFinancialResults.add(aggregateFinancialResult);
         }
         catch (InstantiationException e) {
             throw new RuntimeException(e.getCause());
         }
         catch (IllegalAccessException e) {
             throw new RuntimeException(e.getCause());
         }
     }
 
     /**
      * @param claim
      * @return if the claim's line of business belongs to this company
      */
     private boolean isCompanyClaim(Claim claim) {
         LobMarker lob = claim.getLineOfBusiness();
         return (lob instanceof CompanyConfigurableLobWithReserves) &&
                 (((CompanyConfigurableLobWithReserves) lob).getParmCompany().getSelectedComponent() == this);
     }
 
     /**
      * @param underwritingInfo
      * @return if the underwriting info's line of business belongs to this company
      */
     private boolean isCompanyUnderwritingInfo(UnderwritingInfo underwritingInfo) {
         LobMarker lob = underwritingInfo.getLineOfBusiness();
         return (lob instanceof CompanyConfigurableLobWithReserves) &&
                 (((CompanyConfigurableLobWithReserves) lob).getParmCompany().getSelectedComponent() == this);
     }
 
     /**
      * @param financialResult
      * @return if the financialResults's almGenerator belongs to this company
      */
     private boolean isCompanyFinancialResult(Claim financialResult) {
         if (financialResult.getOrigin() instanceof CompanyConfigurableAssetLiabilityMismatchGenerator) {
             CompanyConfigurableAssetLiabilityMismatchGenerator almGenerator = (CompanyConfigurableAssetLiabilityMismatchGenerator) financialResult.getOrigin();
             return almGenerator.getParmCompany().getSelectedComponent() == this;
         }
         return false;
     }
 
     /**
      * @param contract
      * @return is only executed if the reinsurance contract is not yet collected by the map.
      *         The algorithm compares each reinsurer in the list with the company. It stops with the first matching, returns
      *         the corresponding portion and saves contract and portion in the map coveredPortionperContract for further
      *         queries. If no matches were found, portion is set to zero.
      */
     private Double getCoveredPortion(IReinsuranceContractMarker contract) {
         Double portion = coveredPortionPerContract.get(contract);
         if (portion == null) {
             if (!(contract instanceof MultiCompanyCoverAttributeReinsuranceContract)) {
                 return 0d;
             }
             ConstrainedMultiDimensionalParameter coverPortions = ((MultiCompanyCoverAttributeReinsuranceContract) contract).getParmReinsurers();
             int numberOfReinsurers = coverPortions.getValueRowCount();
             int firstRowWithReinsurer = coverPortions.getTitleRowCount();
             for (int row = firstRowWithReinsurer; row <= numberOfReinsurers; row++) {
                 String reinsurerName = (String) coverPortions.getValueAt(row, CompanyPortion.COMPANY_COLUMN_INDEX);
                 portion = (Double) coverPortions.getValueAt(row, CompanyPortion.PORTION_COLUMN_INDEX);
                 if (!reinsurerName.equals(this.getNormalizedName())) {
                     // this company is not a reinsurer for the contract
                     portion = 0d;
                 }
                 coveredPortionPerContract.put(contract, portion);
                 if (portion > 0) break;   // stop, if equal names were found
             }
         }
         return portion;
     }
 
     public void allocateChannelsToPhases() {
         setTransmitterPhaseInput(inDefaultProbability, PHASE_DEFAULT);
         setTransmitterPhaseOutput(outReinsurersDefault, PHASE_DEFAULT);
 
         setTransmitterPhaseInput(inClaimsCeded, PHASE_AGGREGATION);
         setTransmitterPhaseInput(inClaimsGross, PHASE_AGGREGATION);
         setTransmitterPhaseInput(inFinancialResults, PHASE_AGGREGATION);
         setTransmitterPhaseInput(inUnderwritingInfoCeded, PHASE_AGGREGATION);
         setTransmitterPhaseInput(inUnderwritingInfoGross, PHASE_AGGREGATION);
 
         setTransmitterPhaseOutput(outClaimsCeded, PHASE_AGGREGATION);
         setTransmitterPhaseOutput(outClaimsGross, PHASE_AGGREGATION);
         setTransmitterPhaseOutput(outClaimsGrossPrimaryInsurer, PHASE_AGGREGATION);
         setTransmitterPhaseOutput(outClaimsGrossReinsurer, PHASE_AGGREGATION);
         setTransmitterPhaseOutput(outClaimsLeanDevelopmentCeded, PHASE_AGGREGATION);
         setTransmitterPhaseOutput(outClaimsLeanDevelopmentGross, PHASE_AGGREGATION);
         setTransmitterPhaseOutput(outClaimsLeanDevelopmentGrossPrimaryInsurer, PHASE_AGGREGATION);
         setTransmitterPhaseOutput(outClaimsLeanDevelopmentGrossReinsurer, PHASE_AGGREGATION);
         setTransmitterPhaseOutput(outClaimsLeanDevelopmentNet, PHASE_AGGREGATION);
         setTransmitterPhaseOutput(outClaimsLeanDevelopmentNetPrimaryInsurer, PHASE_AGGREGATION);
         setTransmitterPhaseOutput(outClaimsNet, PHASE_AGGREGATION);
         setTransmitterPhaseOutput(outClaimsNetPrimaryInsurer, PHASE_AGGREGATION);
         setTransmitterPhaseOutput(outFinancialResults, PHASE_AGGREGATION);
         setTransmitterPhaseOutput(outUnderwritingInfoCeded, PHASE_AGGREGATION);
         setTransmitterPhaseOutput(outUnderwritingInfoGross, PHASE_AGGREGATION);
         setTransmitterPhaseOutput(outUnderwritingInfoGrossPrimaryInsurer, PHASE_AGGREGATION);
         setTransmitterPhaseOutput(outUnderwritingInfoGrossReinsurer, PHASE_AGGREGATION);
         setTransmitterPhaseOutput(outUnderwritingInfoNet, PHASE_AGGREGATION);
         setTransmitterPhaseOutput(outUnderwritingInfoNetPrimaryInsurer, PHASE_AGGREGATION);
     }
 
     @Override
     public String toString() {
         return this.getNormalizedName();
     }
 
     public PacketList<Claim> getInClaimsGross() {
         return inClaimsGross;
     }
 
     public void setInClaimsGross(PacketList<Claim> inClaimsGross) {
         this.inClaimsGross = inClaimsGross;
     }
 
     public PacketList<Claim> getOutClaimsGross() {
         return outClaimsGross;
     }
 
     public void setOutClaimsGross(PacketList<Claim> outClaimsGross) {
         this.outClaimsGross = outClaimsGross;
     }
 
     public PacketList<Claim> getOutClaimsGrossPrimaryInsurer() {
         return outClaimsGrossPrimaryInsurer;
     }
 
     public void setOutClaimsGrossPrimaryInsurer(PacketList<Claim> outClaimsGrossPrimaryInsurer) {
         this.outClaimsGrossPrimaryInsurer = outClaimsGrossPrimaryInsurer;
     }
 
     public PacketList<Claim> getOutClaimsGrossReinsurer() {
         return outClaimsGrossReinsurer;
     }
 
     public void setOutClaimsGrossReinsurer(PacketList<Claim> outClaimsGrossReinsurer) {
         this.outClaimsGrossReinsurer = outClaimsGrossReinsurer;
     }
 
     public PacketList<ClaimDevelopmentLeanPacket> getOutClaimsLeanDevelopmentGross() {
         return outClaimsLeanDevelopmentGross;
     }
 
     public void setOutClaimsLeanDevelopmentGross(PacketList<ClaimDevelopmentLeanPacket> outClaimsLeanDevelopmentGross) {
         this.outClaimsLeanDevelopmentGross = outClaimsLeanDevelopmentGross;
     }
 
     public PacketList<Claim> getInClaimsCeded() {
         return inClaimsCeded;
     }
 
     public void setInClaimsCeded(PacketList<Claim> inClaimsCeded) {
         this.inClaimsCeded = inClaimsCeded;
     }
 
     public PacketList<Claim> getOutClaimsCeded() {
         return outClaimsCeded;
     }
 
     public void setOutClaimsCeded(PacketList<Claim> outClaimsCeded) {
         this.outClaimsCeded = outClaimsCeded;
     }
 
     public PacketList<Claim> getOutClaimsNet() {
         return outClaimsNet;
     }
 
     public void setOutClaimsNet(PacketList<Claim> outClaimsNet) {
         this.outClaimsNet = outClaimsNet;
     }
 
     public PacketList<Claim> getOutClaimsNetPrimaryInsurer() {
         return outClaimsNetPrimaryInsurer;
     }
 
     public void setOutClaimsNetPrimaryInsurer(PacketList<Claim> outClaimsNetPrimaryInsurer) {
         this.outClaimsNetPrimaryInsurer = outClaimsNetPrimaryInsurer;
     }
 
     public PacketList<ClaimDevelopmentLeanPacket> getOutClaimsLeanDevelopmentCeded() {
         return outClaimsLeanDevelopmentCeded;
     }
 
     public void setOutClaimsLeanDevelopmentCeded(PacketList<ClaimDevelopmentLeanPacket> outClaimsLeanDevelopmentCeded) {
         this.outClaimsLeanDevelopmentCeded = outClaimsLeanDevelopmentCeded;
     }
 
     public PacketList<ClaimDevelopmentLeanPacket> getOutClaimsLeanDevelopmentNet() {
         return outClaimsLeanDevelopmentNet;
     }
 
     public void setOutClaimsLeanDevelopmentNet(PacketList<ClaimDevelopmentLeanPacket> outClaimsLeanDevelopmentNet) {
         this.outClaimsLeanDevelopmentNet = outClaimsLeanDevelopmentNet;
     }
 
     public PacketList<UnderwritingInfo> getInUnderwritingInfoGross() {
         return inUnderwritingInfoGross;
     }
 
     public void setInUnderwritingInfoGross(PacketList<UnderwritingInfo> inUnderwritingInfoGross) {
         this.inUnderwritingInfoGross = inUnderwritingInfoGross;
     }
 
     public PacketList<UnderwritingInfo> getInUnderwritingInfoCeded() {
         return inUnderwritingInfoCeded;
     }
 
     public void setInUnderwritingInfoCeded(PacketList<UnderwritingInfo> inUnderwritingInfoCeded) {
         this.inUnderwritingInfoCeded = inUnderwritingInfoCeded;
     }
 
     public PacketList<UnderwritingInfo> getOutUnderwritingInfoGross() {
         return outUnderwritingInfoGross;
     }
 
     public void setOutUnderwritingInfoGross(PacketList<UnderwritingInfo> outUnderwritingInfoGross) {
         this.outUnderwritingInfoGross = outUnderwritingInfoGross;
     }
 
     public PacketList<UnderwritingInfo> getOutUnderwritingInfoCeded() {
         return outUnderwritingInfoCeded;
     }
 
     public void setOutUnderwritingInfoCeded(PacketList<UnderwritingInfo> outUnderwritingInfoCeded) {
         this.outUnderwritingInfoCeded = outUnderwritingInfoCeded;
     }
 
     public PacketList<UnderwritingInfo> getOutUnderwritingInfoNet() {
         return outUnderwritingInfoNet;
     }
 
     public void setOutUnderwritingInfoNet(PacketList<UnderwritingInfo> outUnderwritingInfoNet) {
         this.outUnderwritingInfoNet = outUnderwritingInfoNet;
     }
 
     public Rating getParmRating() {
         return parmRating;
     }
 
     public void setParmRating(Rating parmRating) {
         this.parmRating = parmRating;
     }
 
     public PacketList<UnderwritingInfo> getOutUnderwritingInfoGrossPrimaryInsurer() {
         return outUnderwritingInfoGrossPrimaryInsurer;
     }
 
     public void setOutUnderwritingInfoGrossPrimaryInsurer(PacketList<UnderwritingInfo> outUnderwritingInfoGrossPrimaryInsurer) {
         this.outUnderwritingInfoGrossPrimaryInsurer = outUnderwritingInfoGrossPrimaryInsurer;
     }
 
     public PacketList<UnderwritingInfo> getOutUnderwritingInfoGrossReinsurer() {
         return outUnderwritingInfoGrossReinsurer;
     }
 
     public void setOutUnderwritingInfoGrossReinsurer(PacketList<UnderwritingInfo> outUnderwritingInfoGrossReinsurer) {
         this.outUnderwritingInfoGrossReinsurer = outUnderwritingInfoGrossReinsurer;
     }
 
     public PacketList<UnderwritingInfo> getOutUnderwritingInfoNetPrimaryInsurer() {
         return outUnderwritingInfoNetPrimaryInsurer;
     }
 
     public void setOutUnderwritingInfoNetPrimaryInsurer(PacketList<UnderwritingInfo> outUnderwritingInfoNetPrimaryInsurer) {
         this.outUnderwritingInfoNetPrimaryInsurer = outUnderwritingInfoNetPrimaryInsurer;
     }
 
     public PacketList<ClaimDevelopmentLeanPacket> getOutClaimsLeanDevelopmentGrossPrimaryInsurer() {
         return outClaimsLeanDevelopmentGrossPrimaryInsurer;
     }
 
     public void setOutClaimsLeanDevelopmentGrossPrimaryInsurer(PacketList<ClaimDevelopmentLeanPacket> outClaimsLeanDevelopmentGrossPrimaryInsurer) {
         this.outClaimsLeanDevelopmentGrossPrimaryInsurer = outClaimsLeanDevelopmentGrossPrimaryInsurer;
     }
 
     public PacketList<ClaimDevelopmentLeanPacket> getOutClaimsLeanDevelopmentGrossReinsurer() {
         return outClaimsLeanDevelopmentGrossReinsurer;
     }
 
     public void setOutClaimsLeanDevelopmentGrossReinsurer(PacketList<ClaimDevelopmentLeanPacket> outClaimsLeanDevelopmentGrossReinsurer) {
         this.outClaimsLeanDevelopmentGrossReinsurer = outClaimsLeanDevelopmentGrossReinsurer;
     }
 
     public PacketList<ClaimDevelopmentLeanPacket> getOutClaimsLeanDevelopmentNetPrimaryInsurer() {
         return outClaimsLeanDevelopmentNetPrimaryInsurer;
     }
 
     public void setOutClaimsLeanDevelopmentNetPrimaryInsurer(PacketList<ClaimDevelopmentLeanPacket> outClaimsLeanDevelopmentNetPrimaryInsurer) {
         this.outClaimsLeanDevelopmentNetPrimaryInsurer = outClaimsLeanDevelopmentNetPrimaryInsurer;
     }
 
     public PacketList<Claim> getInFinancialResults() {
         return inFinancialResults;
     }
 
     public void setInFinancialResults(PacketList<Claim> inFinancialResults) {
         this.inFinancialResults = inFinancialResults;
     }
 
     public PacketList<Claim> getOutFinancialResults() {
         return outFinancialResults;
     }
 
     public void setOutFinancialResults(PacketList<Claim> outFinancialResults) {
         this.outFinancialResults = outFinancialResults;
     }
 
     public PacketList<DefaultProbabilities> getInDefaultProbability() {
         return inDefaultProbability;
     }
 
     public void setInDefaultProbability(PacketList<DefaultProbabilities> inDefaultProbability) {
         this.inDefaultProbability = inDefaultProbability;
     }
 
     public PacketList<ReinsurerDefault> getOutReinsurersDefault() {
         return outReinsurersDefault;
     }
 
     public void setOutReinsurersDefault(PacketList<ReinsurerDefault> outReinsurersDefault) {
         this.outReinsurersDefault = outReinsurersDefault;
     }

    public PeriodStore getPeriodStore() {
        return periodStore;
    }

    public void setPeriodStore(PeriodStore periodStore) {
        this.periodStore = periodStore;
    }
 }
