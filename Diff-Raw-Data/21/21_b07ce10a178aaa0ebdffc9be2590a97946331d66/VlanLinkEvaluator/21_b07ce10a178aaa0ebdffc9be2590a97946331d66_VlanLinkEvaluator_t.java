 package net.es.oscars.pce.dijkstra;
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import net.es.oscars.utils.soap.ErrorReport;
 import net.es.oscars.utils.soap.OSCARSServiceException;
 import net.es.oscars.utils.topology.NMWGParserUtil;
 import net.es.oscars.utils.topology.PathTools;
 import net.es.oscars.utils.topology.VlanRange;
 
 import org.apache.log4j.Logger;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneDomainContent;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneNodeContent;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePortContent;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwcapContent;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwitchingCapabilitySpecificInfo;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneTopologyContent;
 
 public class VlanLinkEvaluator extends LinkEvaluator {
     
     final private String L2_SWCAP_TYPE = "l2sc";
     private Logger log = Logger.getLogger(VlanLinkEvaluator.class);
     
     public boolean evaluate(CtrlPlaneLinkContent link,
             ArrayList<CtrlPlaneLinkContent> currentBestPath) {
         
         CtrlPlaneSwcapContent swcap = link.getSwitchingCapabilityDescriptors();
         String linkId = NMWGParserUtil.normalizeURN(link.getId());
         String localDomain = PathTools.getLocalDomainId();
         
         //skip this link if no vlan info to analyze
         if(!this.hasVlanInfo(swcap)){
             return true;
         }
         
         //no worries if this link supports translation since we can use any vlan
         if(swcap.getSwitchingCapabilitySpecificInfo().isVlanTranslation() != null &&
                 swcap.getSwitchingCapabilitySpecificInfo().isVlanTranslation()){
             return true;
         }
        
         //..so we have a layer 2 link that doesn't support translation. Its continued use depends 
         // on whether the previous links in the shortest path have overlapping VLAN ranges.
         VlanRange vlanRange = new VlanRange(swcap.getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability());
         for(int i = (currentBestPath.size() - 1); i >= 0; i--){
             CtrlPlaneLinkContent bestPathLink = currentBestPath.get(i);
             
             //skip links with no VLAN info
             if(!this.hasVlanInfo(bestPathLink.getSwitchingCapabilityDescriptors())){
                 continue;
             }
             
             //Join VLAN values
             CtrlPlaneSwitchingCapabilitySpecificInfo swcapInfo = 
                 bestPathLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo();
             vlanRange = VlanRange.and(vlanRange, new VlanRange(swcapInfo.getVlanRangeAvailability()));
             
             //no VLANS available so this link should NOT be used
             if(vlanRange.isEmpty()){
                 return false;
             }
             
             //this hop is local and we set the vlanRange of local links, so don't need to go any further
             if(localDomain != null && NMWGParserUtil.normalizeURN(bestPathLink.getId()).startsWith(localDomain)){
                 break;
             }
             
             //if has translation then we don't need to look any further
             if(swcapInfo.isVlanTranslation() != null && swcapInfo.isVlanTranslation()){
                 break;
             }
         }
         
         //set the vlan range if its a local link so constrained range is passed to next domain
         if(localDomain != null && linkId.startsWith(localDomain)){
             link.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().setVlanRangeAvailability(vlanRange.toString());
         }
         
         return true;
     }
     
     public void finalizeCreate(CtrlPlanePathContent pathConstraints, CtrlPlaneTopologyContent topology) throws OSCARSServiceException{
         this.chooseVlan(pathConstraints, topology, true);
     }
     
     public void chooseVlan(CtrlPlanePathContent pathConstraints, CtrlPlaneTopologyContent topology, boolean suggest) throws OSCARSServiceException{
 
         HashMap<String, Boolean> visitedLinkMap = new HashMap<String, Boolean>();
         HashMap<String, CtrlPlaneLinkContent> linkMap = new HashMap<String, CtrlPlaneLinkContent>();
         HashMap<String, List<String>> nodeLinkMap = new HashMap<String, List<String>>();
         HashMap<String, List<CtrlPlaneLinkContent>> domainMap = new HashMap<String, List<CtrlPlaneLinkContent>>();
         String localDomain = PathTools.getLocalDomainId();
         if(localDomain == null){
             throw new OSCARSServiceException("No local domain ID defined");
         }
         
         //build map of all links in topology
         for(CtrlPlaneDomainContent domain : topology.getDomain()){
             List<CtrlPlaneLinkContent> domainLinks = new ArrayList<CtrlPlaneLinkContent>();
             domainMap.put(NMWGParserUtil.normalizeURN(domain.getId()), domainLinks);
             for(CtrlPlaneNodeContent node : domain.getNode()){
                 for(CtrlPlanePortContent port :node.getPort()){
                     for(CtrlPlaneLinkContent link : port.getLink()){
                         linkMap.put(NMWGParserUtil.normalizeURN(link.getId()), link);
                         if(!nodeLinkMap.containsKey(NMWGParserUtil.normalizeURN(node.getId()))){
                             nodeLinkMap.put(NMWGParserUtil.normalizeURN(node.getId()), new ArrayList<String>());
                         }
                         domainLinks.add(link);
                         nodeLinkMap.get(NMWGParserUtil.normalizeURN(node.getId())).add(NMWGParserUtil.normalizeURN(link.getId()));
                     }
                 }
             }
         }
         
         //get source
         String currLinkId = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(pathConstraints.getHop().get(0), NMWGParserUtil.LINK_TYPE));
         String localLinkId = null;
         VlanRange prevDomainSuggested = new VlanRange(VlanRange.ANY_RANGE);//default to any
         VlanRange prevDomainVlanAvail = new VlanRange(VlanRange.ANY_RANGE);//default to any
         while(currLinkId != null){
             if(currLinkId.startsWith(localDomain)){
                 localLinkId = currLinkId;
                 break;
             }
             String domainId = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(currLinkId, NMWGParserUtil.DOMAIN_TYPE));
             visitedLinkMap.put(currLinkId, true);
             
             /* keep track of vlan info since need to consider last hop seen before
              * current domain
              */
             if(linkMap.containsKey(currLinkId) && 
                     this.hasVlanInfo(linkMap.get(currLinkId).getSwitchingCapabilityDescriptors())){
                 //get vlanRangeAvailability
                 prevDomainVlanAvail = new VlanRange(linkMap.get(currLinkId).getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability());
                 //get suggested
                 if(linkMap.get(currLinkId).getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange() != null){
                     prevDomainSuggested = new VlanRange(linkMap.get(currLinkId).getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange());
                 }
             }
             
             //remove from nodeLinkMap
             String nodeId = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(currLinkId, NMWGParserUtil.NODE_TYPE));
             if(nodeLinkMap.containsKey(nodeId)){
                 for(int i = 0; i < nodeLinkMap.get(nodeId).size(); i++){
                     if(nodeLinkMap.get(nodeId).get(i).equals(currLinkId)){
                         nodeLinkMap.get(nodeId).remove(i);
                         break;
                     }
                 }
             }
             
             //determine the next link to look at 
             currLinkId = this.getNextLinkInPath(currLinkId, nodeId, visitedLinkMap, linkMap, nodeLinkMap);
             //handle case where previous domain did not provide internal hops
             if(currLinkId == null && domainMap.containsKey(domainId) && domainMap.get(domainId).size() == 2){
                 for(CtrlPlaneLinkContent link : domainMap.get(domainId)){
                     String tmpLinkId = NMWGParserUtil.normalizeURN(link.getId());
                     if(!visitedLinkMap.containsKey(tmpLinkId)){
                         currLinkId = tmpLinkId;
                         break;
                     }
                 }
             }
         }
         if(localLinkId == null){
             throw new OSCARSServiceException("Unable to find a local link in the path", ErrorReport.USER);
         }
         
         //iterate through local links
         VlanRange currSegmentRange = null;
         List<CtrlPlaneLinkContent> currSegment = new ArrayList<CtrlPlaneLinkContent>();
         ArrayList<Integer> translationStack = new ArrayList<Integer>();
         currLinkId = localLinkId;
         String dest = NMWGParserUtil.normalizeURN(
                 NMWGParserUtil.getURN(pathConstraints.getHop().get(pathConstraints.getHop().size() - 1), NMWGParserUtil.LINK_TYPE));
         boolean foundNextDomainHop = false;
         while(currLinkId != null && !foundNextDomainHop){
             if(!currLinkId.startsWith(localDomain)){
                 /* this allows us to compare the vlan of 
                    the next domain ingress then exit */
                 foundNextDomainHop = true;
             }
             visitedLinkMap.put(currLinkId, true);
             
             //remove from nodeLinkMap
             String nodeId = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(currLinkId, NMWGParserUtil.NODE_TYPE));
             if(nodeLinkMap.containsKey(nodeId)){
                 for(int i = 0; i < nodeLinkMap.get(nodeId).size(); i++){
                     if(nodeLinkMap.get(nodeId).get(i).equals(currLinkId)){
                         nodeLinkMap.get(nodeId).remove(i);
                         break;
                     }
                 }
             }
             
             //go to next link if not in map or if not a vlan link
             if(!linkMap.containsKey(currLinkId) || !this.hasVlanInfo(linkMap.get(currLinkId).getSwitchingCapabilityDescriptors())){
                 currLinkId = this.getNextLinkInPath(currLinkId, nodeId, visitedLinkMap, linkMap, nodeLinkMap);
                 continue;
             }
             //look at vlans
             CtrlPlaneSwitchingCapabilitySpecificInfo swcapInfo = linkMap.get(currLinkId).getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo();
             currSegment.add(linkMap.get(currLinkId));
             String vlans = linkMap.get(currLinkId).getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability();
             
            //combine vlans with remoteLink vlans
            String remoteLinkId = null;
            if(linkMap.get(currLinkId).getRemoteLinkId() != null){
                remoteLinkId = NMWGParserUtil.normalizeURN(linkMap.get(currLinkId).getRemoteLinkId());
            }
            if(remoteLinkId != null && linkMap.containsKey(remoteLinkId) &&
                    this.hasVlanInfo(linkMap.get(remoteLinkId).getSwitchingCapabilityDescriptors())){
                String remoteVlanRange = linkMap.get(remoteLinkId).getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability();
                VlanRange combinedRange = VlanRange.and(new VlanRange(vlans), new VlanRange(remoteVlanRange));
                if(combinedRange.isEmpty()){
                    throw new OSCARSServiceException("No overlapping VLANs on link " + 
                            currLinkId + "(" + vlans + ") and its remote link " + 
                            remoteLinkId + "(" + remoteVlanRange  + ")", ErrorReport.USER);
                }
                vlans = combinedRange.toString();
                linkMap.get(remoteLinkId).getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().setVlanRangeAvailability(vlans);
            }
            
             //add to translation stack if can do translation
             if(swcapInfo.isVlanTranslation() != null && swcapInfo.isVlanTranslation()){
                 translationStack.add(currSegment.size() - 1);
             }
             
             //if very first vlan hop no point in anding so just continue
             if(currSegmentRange == null){
                 currSegmentRange = VlanRange.and(new VlanRange(vlans), prevDomainVlanAvail);
                 //throw error if previous domain egress and current domain ingress don't overlap
                 if(currSegmentRange.isEmpty()){
                    throw new OSCARSServiceException("No overlapping VLANs between previous " +
                             "domain egress (" + prevDomainVlanAvail + ") and current domain " +
                             "ingress (" + vlans + ")",  ErrorReport.USER);
                 }
                 currLinkId = this.getNextLinkInPath(currLinkId, nodeId, visitedLinkMap, linkMap, nodeLinkMap);
                 continue;
             }
             //..not first hop in segment
             VlanRange tmpRange = VlanRange.and(currSegmentRange, new VlanRange(vlans));
             
             
             if(tmpRange.isEmpty()){
                 //pick a vlan
                 int vlan = suggest ? this.makeSuggestion(currSegmentRange, prevDomainSuggested) : this.useSuggested(currSegment.get(0), currSegmentRange);
                 
                 /* need 2 points capable of translation to do a translation 
                  * (really translation should be a node property so this is a 
                  * little confusing). also, never allow translation on interdomain link
                  */
                 if(translationStack.size() < 2 || foundNextDomainHop){
                     throw new OSCARSServiceException("Unable to choose vlan " +
                             "for segment from " + currSegment.get(0).getId() + 
                             "(" + currSegmentRange + ") to " + currLinkId + "(" + vlans + ")", ErrorReport.USER);
                 }
                 
                 //set VLAN in current segment
                 List<CtrlPlaneLinkContent> newSegment = currSegment.subList(translationStack.remove(translationStack.size() - 1), currSegment.size());
                 int currSegEndIndex = translationStack.remove(translationStack.size() - 1);
                 if(foundNextDomainHop){
                     //don't set the inter-domain hop
                     currSegEndIndex--;
                 }
                 for(int i = 0; i <= currSegEndIndex; i++){
                     if(suggest){
                         currSegment.get(i).getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().setSuggestedVLANRange(vlan + "");
                     }else{
                         currSegment.get(i).getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().setVlanRangeAvailability(vlan + "");
                     }
                 }
                 
                 //start building new segment
                 currSegment = newSegment;
                 currSegmentRange = new VlanRange(vlans);
                 translationStack = new ArrayList<Integer>();
             }else{
                 currSegmentRange = tmpRange;
             }
             
             //if we reached the destination then we're done
             if(dest.equals(currLinkId)){
                 break;
             }
             currLinkId = this.getNextLinkInPath(currLinkId, nodeId, visitedLinkMap, linkMap, nodeLinkMap);
             
         }
         
         //handle last segment by choosing a vlan
         if(currSegment.size() > 0){
             int vlan = suggest ? this.makeSuggestion(currSegmentRange, prevDomainSuggested) : this.useSuggested(currSegment.get(0), currSegmentRange);
             int currSegSize = currSegment.size();
             if(foundNextDomainHop){
                 //don't set vlan in next domain
                 currSegSize--;
             }
             for(int i = 0; i < currSegSize ; i++){
                 if(suggest){
                     currSegment.get(i).getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().setSuggestedVLANRange(vlan+"");
                 }else{
                     currSegment.get(i).getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().setVlanRangeAvailability(vlan+"");
                 }
             }
         }
     }
     
     private int makeSuggestion(VlanRange currSegmentRange,
             VlanRange prevDomainSuggested) {
         VlanRange combinedRange = VlanRange.and(currSegmentRange, prevDomainSuggested);
         //if prev domain range is not available then return random vlan from current range
         if(combinedRange.isEmpty()){
             return currSegmentRange.getRandom();
         }
         return combinedRange.getRandom();
     }
 
     private int useSuggested(CtrlPlaneLinkContent link,
             VlanRange currSegmentRange) {
         CtrlPlaneSwitchingCapabilitySpecificInfo linkSwcapInfo = link.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo();
         
         //if no suggested range then just return a random vlan from current range
         if(linkSwcapInfo.getSuggestedVLANRange() == null){
             return currSegmentRange.getRandom();
         }
         
         VlanRange suggestedRange = new VlanRange(linkSwcapInfo.getSuggestedVLANRange());
         VlanRange combinedRange = VlanRange.and(currSegmentRange, suggestedRange);
         //if suggested range is not available then return random vlan from current range
         if(combinedRange.isEmpty()){
             return currSegmentRange.getRandom();
         }
         
         //return one of the available suggested vlans
         return combinedRange.getRandom();
     }
 
     public void commit(CtrlPlanePathContent pathConstraints, CtrlPlaneTopologyContent topology) throws OSCARSServiceException {
         this.chooseVlan(pathConstraints, topology, false);
     }
     
     private String getNextLinkInPath(String currLinkId, String nodeId, HashMap<String, Boolean> visitedLinkMap,
             HashMap<String, CtrlPlaneLinkContent> linkMap,
             HashMap<String, List<String>> nodeLinkMap) throws OSCARSServiceException{
         
         //determine if can get remote link id
         String remoteLinkId = null;
         if(linkMap.containsKey(currLinkId) && linkMap.get(currLinkId) != null && 
                 linkMap.get(currLinkId).getRemoteLinkId() != null){
             remoteLinkId = NMWGParserUtil.normalizeURN(linkMap.get(currLinkId).getRemoteLinkId());
         }
         
         //determine whether to grab link on other side of node or remote link
         if(nodeLinkMap.containsKey(nodeId) && nodeLinkMap.get(nodeId).size() > 0){
            return nodeLinkMap.get(nodeId).get(0);
         }else if(remoteLinkId != null && linkMap.containsKey(remoteLinkId) && 
                 !visitedLinkMap.containsKey(remoteLinkId)){
             return NMWGParserUtil.normalizeURN(remoteLinkId);
         }
         
         return null;
     }
 
     private boolean hasVlanInfo(CtrlPlaneSwcapContent swcap){
         //skip link if technology type not specified
         if(swcap == null || swcap.getSwitchingCapabilitySpecificInfo() == null){
             return false;
         }
         
         //skip link if not a layer 2 link
         //use the vlan field to determine if check needed
         /* if(!L2_SWCAP_TYPE.equals(swcap.getSwitchingcapType())){
             return false;
         } */
         
         //if no vlans defined then skip
         if(swcap.getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability() == null){
             return false;
         }
         
         return true;
     }
 }
