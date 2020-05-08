 package net.es.oscars.pce.dijkstra;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import net.es.oscars.utils.soap.ErrorReport;
 import org.apache.log4j.Logger;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneDomainContent;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneNodeContent;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePortContent;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneTopologyContent;
 
 import net.es.oscars.logging.OSCARSNetLogger;
 import net.es.oscars.pce.PCEMessage;
 import net.es.oscars.pce.soap.gen.v06.PCEDataContent;
 import net.es.oscars.utils.soap.OSCARSServiceException;
 import net.es.oscars.utils.topology.NMWGParserUtil;
 import net.es.oscars.utils.topology.NMWGTopoBuilder;
 import net.es.oscars.utils.topology.PathTools;
 
 
 /**
  * Path computation engine (PCE) module that runs Dijkstra's algorithm between 
  * hops given in the reservedConstraint or userConstraint. Preference is given 
  * to the reservedConstraint if both are provided. It calculates the paths 
  * against the topology graph provided. It returns the constraints as they 
  * were before and return a topology of elements that represent a 
  * point-to-point path.
  * 
  * There are fundamental issues with stitching Dijkstra's algorithm 
  * results together when looking at multiple hops. These issues may 
  * lead to false negatives if the path contains more than 2 hops AND
  * the non-endpoints are not on the same node or the remote link of their
  * adjacent hops in the given path from the constraint.
  * 
  * @author Andy Lake <andy@es.net>
  * 
  */
 public class DijkstraPCE {
     final private int DEFAULT_LINK_COST = 10;
     final private int DEST_FOUND = 0;
     final private int DEST_NOT_EVAL = 1;
     final private int DEST_NOT_FOUND = 2;
     
     private List<LinkEvaluator> linkEvaluators = new ArrayList<LinkEvaluator>();
     private Logger log = Logger.getLogger(DijkstraPCE.class);
     
     /**
      * Main method that takes a PCEQuery and builds a topology containing 
      * a point-to-point path.
      * 
      * @param query the PCEQuery to process
      * @return PCEDataContent with the input constraints and a topology 
      *  representing a point-to-point path
      * @throws OSCARSServiceException
      */
     public PCEDataContent calculatePath(PCEMessage query) throws OSCARSServiceException {
         NMWGTopoBuilder topoBuilder = new NMWGTopoBuilder();
         PCEDataContent pceData = query.getPCEDataContent();
         CtrlPlaneTopologyContent inputTopo = pceData.getTopology();
         if(inputTopo == null){
             throw new OSCARSServiceException("No topology graph provided [1]");
         }
         
         //get path
         CtrlPlanePathContent path = null;
         if(pceData.getReservedConstraint() != null && 
                 pceData.getReservedConstraint().getPathInfo() != null){
             path = pceData.getReservedConstraint().getPathInfo().getPath();
         }else if(pceData.getUserRequestConstraint() != null && 
                 pceData.getUserRequestConstraint().getPathInfo() != null){
             path = pceData.getUserRequestConstraint().getPathInfo().getPath();
         }else{
             throw new OSCARSServiceException("Received a request with no " +
                     "reservedConstraint or userRequestConstraint containing " +
                     "a PathInfo element");
         }
         
         //make sure path is not null
         if(path == null){
             throw new OSCARSServiceException("Received a null path in request");
         }
         
         //build a map of the nodes in the provided topology
         HashMap<String, CtrlPlaneNodeContent> nodeMap = new HashMap<String, CtrlPlaneNodeContent>();
         HashMap<String, Boolean> linkExistsMap = new HashMap<String, Boolean>();
         for(CtrlPlaneDomainContent domain : inputTopo.getDomain()){
             for(CtrlPlaneNodeContent node : domain.getNode()){
                 nodeMap.put(NMWGParserUtil.normalizeURN(node.getId()), node);
                 for(CtrlPlanePortContent port : node.getPort()){
                     for(CtrlPlaneLinkContent link : port.getLink()){
                         if(link.getId() != null){
                             linkExistsMap.put(NMWGParserUtil.normalizeURN(link.getId()), true);
                         }
                     }
                 }
             }
         }
         
         //main loop to iterate through hops from constraints
         HashMap<String, Boolean> reqElemMap = new HashMap<String, Boolean>();
         ArrayList<CtrlPlaneLinkContent> bestPath = new  ArrayList<CtrlPlaneLinkContent>();
         String src = null;
         String lastDest = null;
         for(CtrlPlaneHopContent hop : path.getHop()){
             //handle first hop - NOTE: first hop must be a link
             if(src == null){
                 src = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hop, NMWGParserUtil.LINK_TYPE));
                 reqElemMap.put(src, true);
                 continue ;
             }
             //get dest. may be domain, node, port, or link urn.
             String dest = NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hop));
             reqElemMap.put(dest, true);
             lastDest = dest;
             //if they are the same because both sides of link specified then continue
             if(src.equals(dest)){
                 continue;
             }
             //run dijkstra's algorithm
             bestPath.addAll(this.dijkstra(src, dest, nodeMap, linkExistsMap, bestPath));
             
             //set source to the other end of the last link in the path
             src = NMWGParserUtil.normalizeURN(bestPath.get(bestPath.size() - 1).getRemoteLinkId());
         }
         
         //Build the topology, 
         //All the foundLocal stuff is to make sure the path before the local domain is not modified
         String localDomain = PathTools.getLocalDomainId();
         String enpointAdded = this.addEndpoint(path.getHop().get(0), bestPath.get(0).getId(),
                 nodeMap, topoBuilder);
         boolean foundLocal = this.isLinkLocal(enpointAdded, localDomain);
         for(CtrlPlaneLinkContent link : bestPath){
             if(!foundLocal){
                 foundLocal = this.isLinkLocal(link.getId(), localDomain);
             }
             if(foundLocal || reqElemMap.containsKey(NMWGParserUtil.normalizeURN(link.getId()))){
                 topoBuilder.addLink(link);
             }
             
             //will not need to add the remote link ID in case where dest is an edge port
             if(lastDest.equals(NMWGParserUtil.normalizeURN(link.getId()))){
                 break;
             }
             
             try{
                 if(!foundLocal){
                     foundLocal = this.isLinkLocal(link.getRemoteLinkId(), localDomain);
                 }
                 if(foundLocal || reqElemMap.containsKey(
                         NMWGParserUtil.normalizeURN(link.getRemoteLinkId()))){
                     topoBuilder.addLink(this.getLink(link.getRemoteLinkId(), nodeMap));
                 }
             }catch(Exception e){
                 /* Catch exception about link not existing. This prevents
                  * non-existent remote link-ids from causing problems. At this
                  * point in the code, any problematic remoteLinkIds would have been
                  * dealt with earlier in the code
                  */
             }
         }
         
         //Let the link evaluators make any final settings
         for(LinkEvaluator evaluator : this.linkEvaluators){
             evaluator.finalizeCreate(path, topoBuilder.getTopology());
         }
         
         pceData.setTopology(topoBuilder.getTopology());
         return pceData;
     }
 
     public PCEDataContent commitPath(PCEMessage query) throws OSCARSServiceException{
         OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
         String event = "commitPath";
         PCEDataContent pceData = query.getPCEDataContent();
         CtrlPlaneTopologyContent inputTopo = pceData.getTopology();
         if(inputTopo == null){
             throw new OSCARSServiceException("No topology graph provided [2]");
         }
         
         //get path - during commit MUST have reservedConstraint
         CtrlPlanePathContent path = null;
         if(pceData.getReservedConstraint() != null && 
                 pceData.getReservedConstraint().getPathInfo() != null){
             path = pceData.getReservedConstraint().getPathInfo().getPath();
         }else{
             throw new OSCARSServiceException("Received a request with no " +
                     "reservedConstraint containing a PathInfo element");
         }
         
         //make sure path is not null
         if(path == null){
             throw new OSCARSServiceException("Received a null path in request");
         }
         
         //pass input to each of the evaluators
         for(LinkEvaluator evaluator : this.linkEvaluators){
             evaluator.commit(path, inputTopo);
         }
         
         this.log.info(netLogger.end(event, 
                 "topoLink="+ pceData.getTopology().getDomain().get(0).getNode().get(0).getPort().get(0).getLink().get(0).getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability()));
         
         return pceData;
     }
     /**
      * Calculates a Dijkstra's Shortest Path between the given source and destination
      * @param src the link ID of the path segments's source
      * @param dest the domain, node, port or link ID of the path segment's destination
      * @param nodeMap HashMap containing all the nodes in the graph indexed by their node ID
      * @param linkExistsMap 
      * @param excludedLinks links (edges) to exclude from the calculation
      * @return a list of links representing the path from source to destination. 
      *    the links will be only the side outgoing from nodes.
      * @throws OSCARSServiceException
      */
     private List<CtrlPlaneLinkContent> dijkstra(String src, String dest, HashMap<String, CtrlPlaneNodeContent> nodeMap, HashMap<String, Boolean> linkExistsMap, ArrayList<CtrlPlaneLinkContent> excludedLinks) throws OSCARSServiceException{
         String srcNode = NMWGParserUtil.getURN(src, NMWGParserUtil.NODE_TYPE);
         CtrlPlaneLinkContent srcLink = this.getLink(src, nodeMap);
         HashMap<String, Integer> nodeCostMap = new HashMap<String, Integer>();
         HashMap<String, List<CtrlPlaneLinkContent>> shortestPathMap = new HashMap<String, List<CtrlPlaneLinkContent>>();
         HashMap<String, Boolean> visitedNodeMap = new HashMap<String, Boolean>();
         HashMap<String, Boolean> excludedLinkMap = new HashMap<String, Boolean>();
         
         //initialization
         srcNode = NMWGParserUtil.normalizeURN(srcNode);
         nodeCostMap.put(srcNode, 0);
         src = NMWGParserUtil.normalizeURN(src);
         dest = NMWGParserUtil.normalizeURN(dest);
         int destType = NMWGParserUtil.getURNType(dest);
         
         //exclude nodes in previous segments in path to avoid unintentional loops
         for(CtrlPlaneLinkContent excludedLink : excludedLinks){
             excludedLinkMap.put(NMWGParserUtil.normalizeURN(excludedLink.getId()), true);
         }
         
         //check if src and dest are at the same node or on the other side of same link
         ArrayList<CtrlPlaneLinkContent> oneNodePath = new ArrayList<CtrlPlaneLinkContent>();
         int oneNodeTestResult = this.isDestFound(dest, destType, null, srcLink, srcNode, oneNodePath, nodeMap);
         if(oneNodeTestResult == DEST_FOUND){
             /* only return the last hop. the source will be added by 
              * other code and this prevents a source's remote link that is 
              * irrelevant from getting added to the final path
              */
             ArrayList<CtrlPlaneLinkContent> tmpPath = new ArrayList<CtrlPlaneLinkContent>();
             tmpPath.add(oneNodePath.get(oneNodePath.size()-1));
             return tmpPath;
         }else if(oneNodeTestResult == DEST_NOT_EVAL){
             /* if found destination, but some factor that prohibits node 
              * from going from source to dest (e.g. source port does not 
              * have overlapping vlan range with dest port)
              */
             throw new OSCARSServiceException("Unable to calculate path from " 
                     + src + " to " + dest + ". Although on same nodes " +
                     "a link-level property prevents these links from " +
                     "being in the same path.", ErrorReport.USER);
         }
         
         //get neighbors
         while(!nodeCostMap.isEmpty()){
             //get the minimum cost node and set as visited
             String currentNode = this.findMinCostNode(nodeCostMap);
             visitedNodeMap.put(currentNode, true);
             
             //look at neighbors
             for(CtrlPlanePortContent port : nodeMap.get(currentNode).getPort()){
                 for(CtrlPlaneLinkContent link : port.getLink()){
                     //check if this link is in our exclusion list
                     if(excludedLinkMap.containsKey(NMWGParserUtil.normalizeURN(link.getId())) ||
                             excludedLinkMap.containsKey(NMWGParserUtil.normalizeURN(link.getRemoteLinkId()))){
                         continue;
                     }
                     
                     //Check that the remote node is valid
                     String remoteNode = NMWGParserUtil.getURN(link.getRemoteLinkId(), NMWGParserUtil.NODE_TYPE);
                     remoteNode = NMWGParserUtil.normalizeURN(remoteNode);
                     if(!nodeMap.containsKey(remoteNode)){
                         continue;
                     }
                     
                     //Check that remoteLink is valid
                     if(!linkExistsMap.containsKey(NMWGParserUtil.normalizeURN(link.getRemoteLinkId()))){
                         continue;
                     }
                     
                     //continue if visited
                     if(visitedNodeMap.containsKey(remoteNode)){
                         continue;
                     }
                     
                     //build current path
                     ArrayList<CtrlPlaneLinkContent> currentPath = new ArrayList<CtrlPlaneLinkContent>();
                     if(shortestPathMap.containsKey(currentNode)){
                         currentPath.addAll(shortestPathMap.get(currentNode));
                     }
                     
                     //check custom parameters to determine whether current link should be considered
                     if(!this.runEvaluators(link, srcLink, currentPath, null)){
                         continue;
                     }
                     
                     int destTestResult = this.isDestFound(dest, destType, srcLink, link, remoteNode, currentPath, nodeMap);
                     if(destTestResult == DEST_FOUND){
                       //if found destination
                         return currentPath;
                     }else if(destTestResult == DEST_NOT_EVAL){
                         /* destination found but the link properties (e.g. VLANs)
                          * invalidate this solution */
                         excludedLinkMap.put(NMWGParserUtil.normalizeURN(link.getId()), true);
                         continue;
                     }else{
                         currentPath.add(link);
                     }
                     
                     //determine cost
                     int linkCost = DEFAULT_LINK_COST;
                     try{
                         linkCost = Integer.parseInt(link.getTrafficEngineeringMetric());
                     }catch(Exception e){}
                     
                     int totalCost = (nodeCostMap.get(currentNode) + linkCost);
                     if(!nodeCostMap.containsKey(remoteNode) || totalCost < nodeCostMap.get(remoteNode)){
                         nodeCostMap.put(remoteNode, totalCost);
                         shortestPathMap.put(remoteNode, currentPath);
                     }
                 }
             }
             //remove the current node since we don't want to visit it again
             nodeCostMap.remove(currentNode);
         }
 
         throw new OSCARSServiceException("No path found between " + src + " and " + dest);
     }
     
     /**
      * Finds the least cost node in the given map
      * 
      * @param nodeCostMap a HashMap of node costs indexed by node ID
      * @return the node ID of the least cost node
      */
     private String findMinCostNode(HashMap<String, Integer> nodeCostMap) {
         int minCost = 0;
         String minNode = null;
         
         for(String nodeId : nodeCostMap.keySet()){
             if(minNode == null || nodeCostMap.get(nodeId) < minCost){
                 minNode = nodeId;
                 minCost = nodeCostMap.get(nodeId);
             }
         }
         
         return minNode;
     }
     
     /**
      * Returns a link from the given node map or throws exception if 
      * can't be found.
      * 
      * @param linkId String of the link ID to find
      * @param nodeMap a map of all the nodes in the graph from 
      *         which to extract the link
      * @return the matching link
      * @throws OSCARSServiceException
      */
     private CtrlPlaneLinkContent getLink(String linkId,
             HashMap<String, CtrlPlaneNodeContent> nodeMap) throws OSCARSServiceException {
         
         linkId = NMWGParserUtil.normalizeURN(linkId);
         String nodeId = NMWGParserUtil.getURN(linkId, NMWGParserUtil.NODE_TYPE);
         nodeId = NMWGParserUtil.normalizeURN(nodeId);
         if(nodeMap.containsKey(nodeId)){
             for(CtrlPlanePortContent port: nodeMap.get(nodeId).getPort()){
                 for(CtrlPlaneLinkContent link : port.getLink()){
                     if(linkId.equals(NMWGParserUtil.normalizeURN(link.getId()))){
                         return link;
                     }
                 }
             }
         }
         
         throw new OSCARSServiceException("Unable to find link with id " + linkId);
     }
     
     /**
      * Returns a port from the given node map or throws exception if 
      * can't be found.
      * 
      * @param portId String of the port ID to find
      * @param nodeMap a map of all the nodes in the graph from 
      *         which to extract the port
      * @return the matching port
      * @throws OSCARSServiceException
      */
     private CtrlPlanePortContent getPort(String portId,
             HashMap<String, CtrlPlaneNodeContent> nodeMap) throws OSCARSServiceException {
         
         portId = NMWGParserUtil.normalizeURN(portId);
         String nodeId = NMWGParserUtil.getURN(portId, NMWGParserUtil.NODE_TYPE);
         nodeId = NMWGParserUtil.normalizeURN(nodeId);
         if(nodeMap.containsKey(nodeId)){
             for(CtrlPlanePortContent port: nodeMap.get(nodeId).getPort()){
                 if(portId.equals(NMWGParserUtil.normalizeURN(port.getId()))){
                         return port;
                 }
             }
         }
         
         throw new OSCARSServiceException("Unable to find port with id " + portId);
     }
     
     /**
      * Adds a link to the topology if its not already in the path
      * 
      * @param endpoint the endpoint to add as a hop
      * @param nextLinkId the next link in the path
      * @param nodeMap map of nodes in graph
      * @param topoBuilder the object used to store the working topology
      * @throws OSCARSServiceException
      */
     private String addEndpoint(CtrlPlaneHopContent endpoint,
             String nextLinkId, HashMap<String, CtrlPlaneNodeContent> nodeMap,
             NMWGTopoBuilder topoBuilder) throws OSCARSServiceException {
         String endpointId = NMWGParserUtil.getURN(endpoint, NMWGParserUtil.LINK_TYPE);
         nextLinkId = NMWGParserUtil.getURN(nextLinkId, NMWGParserUtil.LINK_TYPE);
         if(!endpointId.equals(nextLinkId)){
             topoBuilder.addLink(this.getLink(endpointId, nodeMap));
             return endpointId;
         }
         return null;
     }
     
     /**
      * Returns true if the destination has been found and false if need o keep looking
      * 
      * @param dest the destination that the calculation is trying to find
      * @param destType whether the destination is a domain, node, port or link. 
      * @param srcLink  the srcLink. can be null in one node situation
      * @param currLink the link to compare to the destination
      * @param currNode the node to compare the destination
      * @param path the current best path
      * @param nodeMap a HashMap of all the nodes in the graph
      * @return true if the destination is found, false otherwise
      * @throws OSCARSServiceException
      */
     private int isDestFound(String dest, int destType, CtrlPlaneLinkContent srcLink,
             CtrlPlaneLinkContent currLink, 
             String currNode, List<CtrlPlaneLinkContent> path, 
             HashMap<String, CtrlPlaneNodeContent> nodeMap) throws OSCARSServiceException{
         if(destType == NMWGParserUtil.DOMAIN_TYPE || destType == NMWGParserUtil.NODE_TYPE){
            if(currNode.startsWith(dest)){
                 path.add(currLink);
                 return DEST_FOUND;
             }
         }else if(destType == NMWGParserUtil.PORT_TYPE){
             String remoteLinkId = NMWGParserUtil.normalizeURN(currLink.getRemoteLinkId());
            if(remoteLinkId.startsWith(dest)){
                 path.add(currLink);
                 return DEST_FOUND;
            }else if(dest.startsWith(currNode)){
                 CtrlPlanePortContent port = this.getPort(dest, nodeMap);
                 if(port.getLink().size() == 1){
                     /* when we first ran link evaluators we did not have 
                      * the destination link. We need to make sure that the 
                      * destination does not have any issues before we can declare success
                      */
                     if(!this.runEvaluators(port.getLink().get(0), srcLink, path, currLink)){
                         return DEST_NOT_EVAL;
                     }
                     path.add(currLink);
                     path.add(port.getLink().get(0));
                     
                     return DEST_FOUND;
                 }else{
                     /* probably a better way to handle this but it is an 
                        extreme edge case currently (if you're reading this 
                        comment though its possible its not as extreme as 
                        I thought). */
                     throw new OSCARSServiceException(dest + " is ambiguous " +
                         "because it has multiple links. Please specify which " +
                         "link should be in the path.", ErrorReport.USER);
                 }
             }
         }else if(destType == NMWGParserUtil.LINK_TYPE){
             String remoteLinkId = NMWGParserUtil.normalizeURN(currLink.getRemoteLinkId());
             if(remoteLinkId.equals(dest)){
                 path.add(currLink);
                 return DEST_FOUND;
            }else if(dest.startsWith(currNode)){
                 CtrlPlaneLinkContent destLink = this.getLink(dest, nodeMap);
                 /* when we first ran link evaluators we did not have 
                  * the destination link. We need to make sure that the 
                  * destination does not have any issues before we can declare success
                  */
                 if(!this.runEvaluators(destLink, srcLink, path, currLink)){
                     return DEST_NOT_EVAL;
                 }
                 
                 path.add(currLink);
                 path.add(destLink);
                 
                 return DEST_FOUND;
             }
         }
         
         return DEST_NOT_FOUND;
     }
     
     /**
      * Method that calls LinkEvaluators to determine if it should be considered
      * 
      * @param link the link to evaluate
      * @param srcLink the source link in this request
      * @param currentPath the current path (does not contain source)
      * @param endLink link to append to end of path
      * @return true if link should be considered, false otherwise
      */
     private boolean runEvaluators(CtrlPlaneLinkContent link,
             CtrlPlaneLinkContent srcLink,
             List<CtrlPlaneLinkContent> currentPath,
             CtrlPlaneLinkContent endLink) {
         ArrayList<CtrlPlaneLinkContent> tmpPath = new ArrayList<CtrlPlaneLinkContent>();
         
         //build path that contains source for evaluator
         if(srcLink != null){
             tmpPath.add(srcLink);
         }
         tmpPath.addAll(currentPath);
         if(endLink != null){
             tmpPath.add(endLink);
         }
         
         for(LinkEvaluator evaluator : this.linkEvaluators){
             if(!evaluator.evaluate(link, tmpPath)){
                 return false;
             }
         }
         
         return true;
     }
     
     /**
      * Returns the set of custom classes that can be used to analyze 
      * additional properties beyond cost to determine if a link should be used.
      * 
      * @return the list of LinkEvaluators for this PCE
      */
     public List<LinkEvaluator> getLinkEvaluators() {
         return this.linkEvaluators;
     }
     
     /**
      * Checks if a given link belongs to the given domain
      * 
      * @param linkId the ID of the link to check
      * @param localDomain the local domain ID
      * @return true if link belongs to given domain, false otherwise
      */
     private boolean isLinkLocal(String linkId, String localDomain) {
         if(linkId == null || localDomain == null){
             return false;
         }
         
         try {
             return localDomain.equals(NMWGParserUtil.normalizeURN(
                     NMWGParserUtil.getURN(linkId, NMWGParserUtil.DOMAIN_TYPE)));
         } catch (OSCARSServiceException e) {
             return false;
         }
     }
 }
