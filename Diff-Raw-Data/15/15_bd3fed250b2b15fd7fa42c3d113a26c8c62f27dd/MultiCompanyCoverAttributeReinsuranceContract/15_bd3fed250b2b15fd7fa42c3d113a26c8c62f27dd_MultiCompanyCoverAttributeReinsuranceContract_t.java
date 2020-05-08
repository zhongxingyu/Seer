 package org.pillarone.riskanalytics.domain.pc.reinsurance.contracts;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.pillarone.riskanalytics.core.packets.PacketList;
 import org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter;
 import org.pillarone.riskanalytics.core.parameterization.ConstraintsFactory;
 import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope;
 import org.pillarone.riskanalytics.core.util.GroovyUtils;
 import org.pillarone.riskanalytics.domain.pc.claims.Claim;
 import org.pillarone.riskanalytics.domain.pc.claims.ClaimFilterUtilities;
 import org.pillarone.riskanalytics.domain.pc.claims.ClaimUtilities;
 import org.pillarone.riskanalytics.domain.pc.claims.SortClaimsByFractionOfPeriod;
 import org.pillarone.riskanalytics.domain.pc.company.ICompanyMarker;
 import org.pillarone.riskanalytics.domain.pc.constants.IncludeType;
 import org.pillarone.riskanalytics.domain.pc.constants.LogicArguments;
 import org.pillarone.riskanalytics.domain.pc.constants.ReinsuranceContractBase;
 import org.pillarone.riskanalytics.domain.pc.constraints.CompanyPortion;
 import org.pillarone.riskanalytics.domain.pc.creditrisk.ReinsurerDefault;
 import org.pillarone.riskanalytics.domain.pc.generators.claims.PerilMarker;
 import org.pillarone.riskanalytics.domain.pc.lob.LobMarker;
 import org.pillarone.riskanalytics.domain.pc.reinsurance.ReinsuranceResultWithCommissionPacket;
 import org.pillarone.riskanalytics.domain.pc.reinsurance.contracts.cover.*;
 import org.pillarone.riskanalytics.domain.pc.reserves.IReserveMarker;
 import org.pillarone.riskanalytics.domain.pc.reserves.fasttrack.ClaimDevelopmentLeanPacket;
 import org.pillarone.riskanalytics.domain.pc.underwriting.UnderwritingFilterUtilities;
 import org.pillarone.riskanalytics.domain.pc.underwriting.UnderwritingInfo;
 import org.pillarone.riskanalytics.domain.pc.underwriting.UnderwritingInfoUtilities;
 import org.pillarone.riskanalytics.domain.utils.InputFormatConverter;
 
 import java.util.*;
 
 /**
  * This reinsurance contract allows to specify the portions covered by each reinsurer. Furthermore it considers if
  * a specific reinsurer has gone default and reduces the cover correspondingly.
  *
  * @author stefan.kunz (at) intuitive-collaboration (dot) com
  */
 public class MultiCompanyCoverAttributeReinsuranceContract extends ReinsuranceContract {
 
     private SimulationScope simulationScope;
 
     private ReinsuranceContractBase parmBasedOn = ReinsuranceContractBase.NET;
     private ICoverAttributeStrategy parmCover = CompanyCoverAttributeStrategyType.getStrategy(
             CompanyCoverAttributeStrategyType.ALL, ArrayUtils.toMap(new Object[][]{{"reserves", IncludeType.NOTINCLUDED}}));
 
     private PacketList<Claim> outFilteredClaims = new PacketList<Claim>(Claim.class);
 
     private PacketList<UnderwritingInfo> outFilteredUnderwritingInfo = new PacketList<UnderwritingInfo>(UnderwritingInfo.class);
 
     private PacketList<ReinsurerDefault> inReinsurersDefault = new PacketList<ReinsurerDefault>(ReinsurerDefault.class);
 
     private ConstrainedMultiDimensionalParameter parmReinsurers = new ConstrainedMultiDimensionalParameter(
             GroovyUtils.convertToListOfList(new Object[]{"", 1d}),
            Arrays.asList(REINSURER, PORTION),
             ConstraintsFactory.getConstraints(CompanyPortion.IDENTIFIER));
 
     private static final String REINSURER = "Reinsurer";
     private static final String PORTION = "Covered Portion";
 
     /**
      * Evaluate the received default information and adjust the coveredByReinsurer factor accordingly
      * before applying the contract.
      */
     @Override
     public void doCalculation() {
         if (parmContractStrategy == null) {
             throw new IllegalStateException("MultiCompanyCoverAttributeReinsuranceContract.missingContractStrategy");
         }
         if (parmCover == null) {
             throw new IllegalStateException("MultiCompanyCoverAttributeReinsuranceContract.missingCoverStrategy");
         }
 
         Map<String, Double> reinsurersDefault = new HashMap<String, Double>(inReinsurersDefault.size());
         for (ReinsurerDefault reinsurerDefault : inReinsurersDefault) {
             reinsurersDefault.put(reinsurerDefault.getReinsurer(), reinsurerDefault.isDefaultOccurred() ? 0d : 1d);
         }
         double coveredByReinsurers = 0d;
         for (int row = parmReinsurers.getTitleRowCount(); row < parmReinsurers.getRowCount(); row++) {
            double portion = InputFormatConverter.getDouble(parmReinsurers.getValueAt(row, parmReinsurers.getColumnIndex(PORTION)));
            String reinsurer = InputFormatConverter.getString(parmReinsurers.getValueAt(row, parmReinsurers.getColumnIndex(REINSURER)));
             double reinsurerDefault = reinsurersDefault.get(reinsurer) == null ? 1d : reinsurersDefault.get(reinsurer);
             coveredByReinsurers += portion * reinsurerDefault;
         }
         parmContractStrategy.adjustCovered(coveredByReinsurers);
 
         filterInChannels();
         // initialize contract details
         parmContractStrategy.initBookkeepingFigures(outFilteredClaims, outFilteredUnderwritingInfo);
 
         Collections.sort(outFilteredClaims, SortClaimsByFractionOfPeriod.getInstance());
         if (isSenderWired(getOutUncoveredClaims()) || isSenderWired(getOutClaimsDevelopmentLeanNet())) {
             calculateClaims(outFilteredClaims, outCoveredClaims, outUncoveredClaims, this);
         }
         else {
             calculateCededClaims(outFilteredClaims, outCoveredClaims, this);
         }
 
         if (isSenderWired(outCoverUnderwritingInfo) || isSenderWired(outContractFinancials) || isSenderWired(outNetAfterCoverUnderwritingInfo)) {
             calculateCededUnderwritingInfos(outFilteredUnderwritingInfo, outCoverUnderwritingInfo, outCoveredClaims);
         }
         parmCommissionStrategy.calculateCommission(outCoveredClaims, outCoverUnderwritingInfo, false, false);
         if (isSenderWired(outNetAfterCoverUnderwritingInfo)) {
             calculateNetUnderwritingInfos(UnderwritingFilterUtilities.filterUnderwritingInfoByLobWithoutScaling(
                     inUnderwritingInfo, ClaimFilterUtilities.getLinesOfBusiness(outFilteredClaims)),
                     outCoverUnderwritingInfo, outNetAfterCoverUnderwritingInfo, outCoveredClaims);
         }
         if (inClaims.size() > 0 && inClaims.get(0) instanceof ClaimDevelopmentLeanPacket) {
             for (Claim claim : outFilteredClaims) {
                 getOutClaimsDevelopmentLeanGross().add((ClaimDevelopmentLeanPacket) claim);
             }
         }
         if (outCoveredClaims.size() > 0 && outCoveredClaims.get(0) instanceof ClaimDevelopmentLeanPacket) {
             for (Claim claim : outUncoveredClaims) {
                 getOutClaimsDevelopmentLeanNet().add((ClaimDevelopmentLeanPacket) claim);
             }
             for (Claim claim : outCoveredClaims) {
                 getOutClaimsDevelopmentLeanCeded().add((ClaimDevelopmentLeanPacket) claim);
             }
         }
         if (isSenderWired(outContractFinancials)) {
             ReinsuranceResultWithCommissionPacket result = new ReinsuranceResultWithCommissionPacket();
             UnderwritingInfo underwritingInfo = UnderwritingInfoUtilities.aggregate(outCoverUnderwritingInfo);
             if (underwritingInfo != null) {
                 result.setCededPremium(-underwritingInfo.getPremiumWritten());
                 result.setCededCommission(-underwritingInfo.getCommission());
             }
             if (outCoveredClaims.size() > 0) {
                 result.setCededClaim(ClaimUtilities.aggregateClaims(outCoveredClaims, this).getUltimate());
             }
             if (result.getCededPremium() != 0) {
                 result.setCededLossRatio(result.getCededClaim() / -result.getCededPremium());
             }
             outContractFinancials.add(result);
         }
 
         for (UnderwritingInfo outCoverUnderwritingInfoPacket : outCoverUnderwritingInfo) {
             outCoverUnderwritingInfoPacket.setReinsuranceContract(this);
         }
 
         parmContractStrategy.resetCovered();
     }
 
     protected void filterInChannels() {
         if (parmCover instanceof NoneCompanyCoverAttributeStrategy) {
             // leave outFiltered* lists void
         }
         else if (parmCover instanceof AllCompanyCoverAttributeStrategy) {
             outFilteredClaims.addAll(inClaims);
             outFilteredUnderwritingInfo.addAll(inUnderwritingInfo);
         }
         else if (parmCover instanceof CompaniesCompanyCoverAttributeStrategy) {
             List<ICompanyMarker> coveredCompanies = (List<ICompanyMarker>) (((CompaniesCompanyCoverAttributeStrategy) parmCover).getCompanies().getValuesAsObjects());
             outFilteredClaims.addAll(ClaimFilterUtilities.filterClaimsByCompanies(inClaims, coveredCompanies, false));
             List<LobMarker> coveredLines = ClaimFilterUtilities.getLinesOfBusiness(outFilteredClaims);
             outFilteredUnderwritingInfo.addAll(UnderwritingFilterUtilities.filterUnderwritingInfoByLobWithoutScaling(inUnderwritingInfo, coveredLines));
         }
         else {
             List<LobMarker> coveredLines = parmCover instanceof ILinesOfBusinessCoverAttributeStrategy
                     ? (List<LobMarker>) (((ILinesOfBusinessCoverAttributeStrategy) parmCover).getLines().getValuesAsObjects()) : null;
             List<PerilMarker> coveredPerils = parmCover instanceof IPerilCoverAttributeStrategy
                     ? (List<PerilMarker>) ((IPerilCoverAttributeStrategy) parmCover).getPerils().getValuesAsObjects() : null;
             List<IReserveMarker> coveredReserves = parmCover instanceof IReservesCoverAttributeStrategy
                     ? (List<IReserveMarker>) ((IReservesCoverAttributeStrategy) parmCover).getReserves().getValuesAsObjects() : null;
             LogicArguments connection = parmCover instanceof ICombinedCoverAttributeStrategy
                     ? ((ICombinedCoverAttributeStrategy) parmCover).getConnection() : null;
             outFilteredClaims.addAll(ClaimFilterUtilities.filterClaimsByPerilLobReserve(inClaims, coveredPerils, coveredLines, coveredReserves, connection));
             // extend coveredLines such that they additionally consist of the segments which are associated with the selected perils
             coveredLines = ClaimFilterUtilities.getLinesOfBusiness(outFilteredClaims);
             outFilteredUnderwritingInfo.addAll(UnderwritingFilterUtilities.filterUnderwritingInfoByLobAndScaleByPerilsInLob(inUnderwritingInfo, coveredLines, inClaims, coveredPerils));
         }
     }
 
     public ConstrainedMultiDimensionalParameter getParmReinsurers() {
         return parmReinsurers;
     }
 
     public void setParmReinsurers(ConstrainedMultiDimensionalParameter parmReinsurers) {
         this.parmReinsurers = parmReinsurers;
     }
 
     public PacketList<ReinsurerDefault> getInReinsurersDefault() {
         return inReinsurersDefault;
     }
 
     public void setInReinsurersDefault(PacketList<ReinsurerDefault> inReinsurersDefault) {
         this.inReinsurersDefault = inReinsurersDefault;
     }
 
     public SimulationScope getSimulationScope() {
         return simulationScope;
     }
 
     public void setSimulationScope(SimulationScope simulationScope) {
         this.simulationScope = simulationScope;
     }
 
     public ReinsuranceContractBase getParmBasedOn() {
         return parmBasedOn;
     }
 
     public void setParmBasedOn(ReinsuranceContractBase parmBasedOn) {
         this.parmBasedOn = parmBasedOn;
     }
 
     public ICoverAttributeStrategy getParmCover() {
         return parmCover;
     }
 
     public void setParmCover(ICoverAttributeStrategy parmCover) {
         this.parmCover = parmCover;
     }
 
     /**
      * claims whose source is a covered line
      */
     public PacketList<Claim> getOutFilteredClaims() {
         return outFilteredClaims;
     }
 
     public void setOutFilteredClaims(PacketList<Claim> outFilteredClaims) {
         this.outFilteredClaims = outFilteredClaims;
     }
 
     public PacketList<UnderwritingInfo> getOutFilteredUnderwritingInfo() {
         return outFilteredUnderwritingInfo;
     }
 
     public void setOutFilteredUnderwritingInfo(PacketList<UnderwritingInfo> outFilteredUnderwritingInfo) {
         this.outFilteredUnderwritingInfo = outFilteredUnderwritingInfo;
     }
 
 }
