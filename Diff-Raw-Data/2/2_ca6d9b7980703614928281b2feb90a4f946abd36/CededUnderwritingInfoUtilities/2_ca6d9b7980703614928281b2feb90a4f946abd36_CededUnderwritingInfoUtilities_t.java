 package org.pillarone.riskanalytics.domain.pc.underwriting;
 
 import org.pillarone.riskanalytics.domain.utils.marker.IReinsuranceContractMarker;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author jessika.walter (at) intuitive-collaboration (dot) com
  */
public class CededUnderwritingInfoUtilities {
 
     public static boolean sameContent(CededUnderwritingInfo uwInfo1, CededUnderwritingInfo uwInfo2) {
         return (UnderwritingInfoUtilities.sameContent(uwInfo1, uwInfo2)
                 && uwInfo1.getFixedPremium() == uwInfo2.getFixedPremium()
                 && uwInfo1.getVariablePremium() == uwInfo2.getVariablePremium()
                 && uwInfo1.getFixedCommission() == uwInfo2.getFixedCommission()
                 && uwInfo1.getVariableCommission() == uwInfo2.getVariableCommission());
     }
 
     public static void setZero(CededUnderwritingInfo underwritingInfo) {
         UnderwritingInfoUtilities.setZero(underwritingInfo);
         underwritingInfo.setFixedPremium(0);
         underwritingInfo.setVariablePremium(0);
         underwritingInfo.setFixedCommission(0);
         underwritingInfo.setVariableCommission(0);
     }
 
     static public CededUnderwritingInfo aggregate(List<CededUnderwritingInfo> underwritingInfos) {
         return (CededUnderwritingInfo) UnderwritingInfoUtilities.aggregate(new ArrayList<UnderwritingInfo>(underwritingInfos));
     }
 
     static public CededUnderwritingInfo findUnderwritingInfo(List<CededUnderwritingInfo> underwritingInfos, UnderwritingInfo refUwInfo) {
         return (CededUnderwritingInfo) UnderwritingInfoUtilities.findUnderwritingInfo(new ArrayList<UnderwritingInfo>(underwritingInfos), refUwInfo);
     }
 
     /**
      * @param underwritingInfos        the list of underwritingInfo packets to filter
      * @param contracts                the contract markers to filter by, if any; null means no filtering (all are accepted)
      * @param acceptedUnderwritingInfo the list of underwritingInfo packets whose contract is listed in contracts
      * @param rejectedUnderwritingInfo (if not null) the remaining underwritingInfo packets that were filtered out
      */
     public static void segregateUnderwritingInfoByContract(List<CededUnderwritingInfo> underwritingInfos, List<IReinsuranceContractMarker> contracts,
                                                            List<CededUnderwritingInfo> acceptedUnderwritingInfo,
                                                            List<CededUnderwritingInfo> rejectedUnderwritingInfo) {
         if (contracts == null || contracts.size() == 0) {
             acceptedUnderwritingInfo.addAll(underwritingInfos);
         }
         else {
             for (CededUnderwritingInfo underwritingInfo : underwritingInfos) {
                 if (contracts.contains(underwritingInfo.getReinsuranceContract())) {
                     acceptedUnderwritingInfo.add(underwritingInfo);
                 }
                 else if (rejectedUnderwritingInfo != null) {
                     rejectedUnderwritingInfo.add(underwritingInfo);
                 }
             }
         }
     }
 
     static public void difference(List<UnderwritingInfo> minuendUwInfo, List<CededUnderwritingInfo> subtrahendUwInfo, List<UnderwritingInfo> difference) {
         UnderwritingInfoUtilities.difference(minuendUwInfo, new ArrayList<UnderwritingInfo>(subtrahendUwInfo), difference);
     }
 
     static public void differenceOfCededLists(List<CededUnderwritingInfo> minuendUwInfo, List<CededUnderwritingInfo> subtrahendUwInfo, List<CededUnderwritingInfo> difference) {
         assert minuendUwInfo.size() == subtrahendUwInfo.size();
         assert difference != null;
         assert difference.size() == 0;
         for (int i = 0; i < minuendUwInfo.size(); i++) {
             minuendUwInfo.get(i).minus(subtrahendUwInfo.get(i));
             difference.add(minuendUwInfo.get(i));
         }
     }
 
     static public List<CededUnderwritingInfo> differenceOfCededLists(List<CededUnderwritingInfo> minuendUwInfo, List<CededUnderwritingInfo> subtrahendUwInfo) {
         assert minuendUwInfo.size() == subtrahendUwInfo.size();
         List<CededUnderwritingInfo> difference = new ArrayList<CededUnderwritingInfo>(minuendUwInfo.size());
         differenceOfCededLists(minuendUwInfo, subtrahendUwInfo, difference);
         return difference;
     }
 
 }
 
 
