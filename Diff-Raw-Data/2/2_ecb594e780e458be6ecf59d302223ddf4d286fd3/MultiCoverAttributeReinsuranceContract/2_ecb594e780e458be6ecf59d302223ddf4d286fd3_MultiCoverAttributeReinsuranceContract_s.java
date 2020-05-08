 package org.pillarone.riskanalytics.domain.pc.reinsurance.contracts;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.pillarone.riskanalytics.core.components.PeriodStore;
 import org.pillarone.riskanalytics.core.packets.Packet;
 import org.pillarone.riskanalytics.core.packets.PacketList;
 import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope;
 import org.pillarone.riskanalytics.domain.pc.claims.Claim;
 import org.pillarone.riskanalytics.domain.pc.claims.ClaimFilterUtilities;
 import org.pillarone.riskanalytics.domain.pc.claims.ClaimUtilities;
 import org.pillarone.riskanalytics.domain.pc.claims.SortClaimsByFractionOfPeriod;
 import org.pillarone.riskanalytics.domain.pc.constants.IncludeType;
 import org.pillarone.riskanalytics.domain.pc.constants.LogicArguments;
 import org.pillarone.riskanalytics.domain.pc.constants.ReinsuranceContractBase;
 import org.pillarone.riskanalytics.domain.pc.constants.ReinsuranceContractPremiumBase;
 import org.pillarone.riskanalytics.domain.pc.filter.FilterUtils;
 import org.pillarone.riskanalytics.domain.pc.generators.claims.PerilMarker;
 import org.pillarone.riskanalytics.domain.pc.lob.LobMarker;
 import org.pillarone.riskanalytics.domain.pc.reinsurance.ReinsuranceResultWithCommissionPacket;
 import org.pillarone.riskanalytics.domain.pc.reinsurance.contracts.cover.*;
 import org.pillarone.riskanalytics.domain.pc.reserves.IReserveMarker;
 import org.pillarone.riskanalytics.domain.pc.reserves.fasttrack.ClaimDevelopmentLeanPacket;
 import org.pillarone.riskanalytics.domain.pc.underwriting.*;
 
 import java.util.Collections;
 import java.util.List;
 
 /**
  * This component filters from the incoming claims and underwriting information
  * the packets whose line is listed in parameter parmCoveredLines and provides
  * them in the corresponding out Packetlists.
  * If the parameter contains no line at all, all packets are sent as is to the
  * next component. Packets are not modified.
  *
  * @author stefan.kunz (at) intuitive-collaboration (dot) com
  */
 public class MultiCoverAttributeReinsuranceContract extends ReinsuranceContract implements IReinsuranceContractMarker {
 
     private SimulationScope simulationScope;
     private PeriodStore periodStore;
 
     private ReinsuranceContractBase parmBasedOn = ReinsuranceContractBase.NET;
     private ReinsuranceContractPremiumBase parmPremiumBase = ReinsuranceContractPremiumBase.COMPLETESEGMENT;
     private ICoverAttributeStrategy parmCover = CoverAttributeStrategyType.getStrategy(
             CoverAttributeStrategyType.ALL, ArrayUtils.toMap(new Object[][]{{"reserves", IncludeType.NOTINCLUDED}}));
 
     /** required for correct allocation of ceded premium */
     private List<Claim> allInClaims = new PacketList<Claim>(Claim.class);
     /** required for correct allocation of ceded premium */
     private List<UnderwritingInfo> allInUnderwritingInfos = new PacketList<UnderwritingInfo>(UnderwritingInfo.class);
 
 
     public void doCalculation() {
         if (parmContractStrategy == null) {
             throw new IllegalStateException("MultiCoverAttributeReinsuranceContract.missingContractStrategy");
         }
         if (parmCover == null) {
             throw new IllegalStateException("MultiCoverAttributeReinsuranceContract.missingCoverStrategy");
         }
 
         // initialize contract details
         parmContractStrategy.initBookkeepingFigures(inClaims, inUnderwritingInfo);
 
         initCoveredByReinsurer();
         Collections.sort(inClaims, SortClaimsByFractionOfPeriod.getInstance());
         if (isSenderWired(getOutUncoveredClaims()) || isSenderWired(getOutClaimsDevelopmentLeanNet())) {
             calculateClaims(inClaims, outCoveredClaims, outUncoveredClaims, this);
         }
         else {
             calculateCededClaims(inClaims, outCoveredClaims, this);
         }
 
         if (isSenderWired(outCoverUnderwritingInfo) || isSenderWired(outContractFinancials) || isSenderWired(outNetAfterCoverUnderwritingInfo)) {
             calculateCededUnderwritingInfos(inUnderwritingInfo, outCoverUnderwritingInfo, outCoveredClaims);
         }
         parmCommissionStrategy.calculateCommission(outCoveredClaims, outCoverUnderwritingInfo, false, false);
         if (isSenderWired(outNetAfterCoverUnderwritingInfo)) {
             if (parmPremiumBase.equals(ReinsuranceContractPremiumBase.COMPLETESEGMENT)) {
                 calculateNetUnderwritingInfos(inUnderwritingInfo, outCoverUnderwritingInfo,
                         outNetAfterCoverUnderwritingInfo, outCoveredClaims);
             }
             else if (parmPremiumBase.equals(ReinsuranceContractPremiumBase.PROPORTIONALTOCOVEREDCLAIMS)) {
                 calculateNetUnderwritingInfos(UnderwritingFilterUtilities.filterUnderwritingInfoByLobWithoutScaling(
                         allInUnderwritingInfos, ClaimFilterUtilities.getLinesOfBusiness(inClaims)),
                         outCoverUnderwritingInfo, outNetAfterCoverUnderwritingInfo, outCoveredClaims);
             }
         }
         if (inClaims.size() > 0 && inClaims.get(0) instanceof ClaimDevelopmentLeanPacket) {
             for (Claim claim : inClaims) {
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
             CededUnderwritingInfo underwritingInfo = CededUnderwritingInfoUtilities.aggregate(outCoverUnderwritingInfo);
             if (underwritingInfo != null) {
                 result.setCededPremium(-underwritingInfo.getPremium());
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
     }
 
    protected void filterInChannels(PacketList inChannel, PacketList source) {
         if (inChannel == inClaims) {
             allInClaims.addAll(source);
         }
         if (inChannel == inUnderwritingInfo) {
             allInUnderwritingInfos.addAll(source);
         }
         if (parmCover instanceof NoneCoverAttributeStrategy) {
             // leave outFiltered* lists void
         } else if (parmCover instanceof AllCoverAttributeStrategy) {
             if (parmCover.getParameters().get("reserves").equals(IncludeType.NOTINCLUDED) && inChannel == inClaims) {
                 for (Object claim : source) {
                     if (((Claim) claim).getPeril() instanceof PerilMarker) {
                         inChannel.add((Packet) claim);
                     }
                 }
             } else if (parmCover.getParameters().get("reserves").equals(IncludeType.ONLY) && inChannel == inClaims) {
                 for (Object claim : source) {
                     if (((Claim) claim).getPeril() instanceof IReserveMarker) {
                         inChannel.add((Packet) claim);
                     }
                 }
             }
             else {
                 super.filterInChannel(inChannel, source);
             }
         }
         else if (inChannel == inClaims) {
             List<LobMarker> coveredLines = FilterUtils.getCoveredLines(parmCover, periodStore);
             List<PerilMarker> coveredPerils = FilterUtils.getCoveredPerils(parmCover, periodStore);
             List<IReserveMarker> coveredReserves = FilterUtils.getCoveredReserves(parmCover, periodStore);
             LogicArguments connection = parmCover instanceof ICombinedCoverAttributeStrategy
                     ? ((ICombinedCoverAttributeStrategy) parmCover).getConnection() : null;
             inChannel.addAll(ClaimFilterUtilities.filterClaimsByPerilLobReserve(source, coveredPerils, coveredLines, coveredReserves, connection));
         }
         else if (inChannel == inUnderwritingInfo) {
             List<LobMarker> coveredLines = FilterUtils.getCoveredLines(parmCover, periodStore);
             if (parmPremiumBase.equals(ReinsuranceContractPremiumBase.COMPLETESEGMENT)) {
                 if (coveredLines == null || coveredLines.size() == 0) {
                     coveredLines = ClaimFilterUtilities.getLinesOfBusiness(inClaims);
                 }
                 inUnderwritingInfo.addAll(UnderwritingFilterUtilities.filterUnderwritingInfoByLob(source, coveredLines));
             }
             else if (parmPremiumBase == ReinsuranceContractPremiumBase.PROPORTIONALTOCOVEREDCLAIMS) {
                 // extend coveredLines such that they additionally consist of the segments which are associated with the selected perils
                 coveredLines = ClaimFilterUtilities.getLinesOfBusiness(inClaims);
                 List<PerilMarker> coveredPerils = FilterUtils.getCoveredPerils(parmCover, periodStore);
                 inUnderwritingInfo.addAll(UnderwritingFilterUtilities.filterUnderwritingInfoByLobAndScaleByPerilsInLob(source, coveredLines, allInClaims, coveredPerils));
             }
         }
         else {
             super.filterInChannel(inChannel, source);
         }
     }
 
     public SimulationScope getSimulationScope() {
         return simulationScope;
     }
 
     public void setSimulationScope(SimulationScope simulationScope) {
         this.simulationScope = simulationScope;
     }
 
     public PacketList<ReinsuranceResultWithCommissionPacket> getOutContractFinancials() {
         return outContractFinancials;
     }
 
     public void setOutContractFinancials(PacketList<ReinsuranceResultWithCommissionPacket> outContractFinancials) {
         this.outContractFinancials = outContractFinancials;
     }
 
     public ICoverAttributeStrategy getParmCover() {
         return parmCover;
     }
 
     public void setParmCover(ICoverAttributeStrategy parmCover) {
         this.parmCover = parmCover;
     }
 
     public ReinsuranceContractBase getParmBasedOn() {
         return parmBasedOn;
     }
 
     public void setParmBasedOn(ReinsuranceContractBase parmBasedOn) {
         this.parmBasedOn = parmBasedOn;
     }
 
     public ReinsuranceContractPremiumBase getParmPremiumBase() {
         return parmPremiumBase;
     }
 
     public void setParmPremiumBase(ReinsuranceContractPremiumBase parmPremiumBase) {
         this.parmPremiumBase = parmPremiumBase;
     }
 
     public PeriodStore getPeriodStore() {
         return periodStore;
     }
 
     public void setPeriodStore(PeriodStore periodStore) {
         this.periodStore = periodStore;
     }
 }
