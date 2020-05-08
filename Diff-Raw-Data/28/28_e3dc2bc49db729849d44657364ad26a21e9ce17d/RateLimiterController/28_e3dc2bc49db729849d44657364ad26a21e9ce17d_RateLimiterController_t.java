 package net.floodlightcontroller.ratelimiter;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.floodlightcontroller.core.util.AppCookie;
 import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;
 import net.floodlightcontroller.qos.IQoSService;
 import net.floodlightcontroller.qos.QoSWebRoutable;
 import net.floodlightcontroller.restserver.IRestApiService;
 import net.floodlightcontroller.routing.*;
 
 import org.openflow.protocol.*;
 import org.openflow.protocol.action.OFAction;
 import org.openflow.protocol.action.OFActionEnqueue;
 import org.openflow.protocol.action.OFActionType;
 import org.openflow.util.HexString;
 
 import net.floodlightcontroller.core.FloodlightContext;
 import net.floodlightcontroller.core.IFloodlightProviderService;
 import net.floodlightcontroller.core.IOFSwitch;
 import net.floodlightcontroller.core.module.FloodlightModuleContext;
 import net.floodlightcontroller.core.module.FloodlightModuleException;
 import net.floodlightcontroller.core.module.IFloodlightService;
 import net.floodlightcontroller.counter.ICounterStoreService;
 import net.floodlightcontroller.devicemanager.IDevice;
 import net.floodlightcontroller.devicemanager.IDeviceService;
 import net.floodlightcontroller.devicemanager.SwitchPort;
 import net.floodlightcontroller.forwarding.Forwarding;
 import net.floodlightcontroller.topology.ITopologyService;
 import net.floodlightcontroller.topology.NodePortTuple;
 import net.floodlightcontroller.benchmarkcontroller.IQueueCreaterService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class RateLimiterController extends Forwarding implements RateLimiterService{
 	private Map<Integer, Policy> policyStorage;
 	private Map<Integer, Flow> flowStorage;
     private static Logger log = LoggerFactory.getLogger(RateLimiterController.class);
     private ILinkDiscoveryService linkService;
     protected IQueueCreaterService queueCreaterService;
 	protected IRestApiService restApi;
     private Map<Long, IOFSwitch> switches;
     private Map<Long, Set<Policy>> swPolicyTuples;
 
 	public boolean flowBelongsToRule(OFMatch flow, OFMatch rule){
         log.warn("flow: " + flow.toString() + " rule: " + rule.toString());
 
 		int rulewc = rule.getWildcards();
 		if(!(((rulewc & OFMatch.OFPFW_IN_PORT) == OFMatch.OFPFW_IN_PORT) || 
 				rule.getInputPort() == flow.getInputPort()))
 			return false;
 
 		if(!(((rulewc & OFMatch.OFPFW_DL_VLAN) == OFMatch.OFPFW_DL_VLAN) || 
 				rule.getDataLayerVirtualLan() == flow.getDataLayerVirtualLan()))
 			return false;
 
 		if(!(((rulewc & OFMatch.OFPFW_DL_SRC) == OFMatch.OFPFW_DL_SRC) || 
 				Arrays.equals(rule.getDataLayerSource(), flow.getDataLayerSource())))
 			return false;
 
 		if(!(((rulewc & OFMatch.OFPFW_DL_DST) == OFMatch.OFPFW_DL_DST) || 
 				Arrays.equals(rule.getDataLayerDestination(), flow.getDataLayerDestination())))
 			return false;
 
 		if(!(((rulewc & OFMatch.OFPFW_DL_TYPE) == OFMatch.OFPFW_DL_TYPE) || 
 				rule.getDataLayerType() == flow.getDataLayerType()))
 			return false;
 
 		if(!(((rulewc & OFMatch.OFPFW_NW_PROTO) == OFMatch.OFPFW_NW_PROTO) || 
 				rule.getNetworkProtocol() == flow.getNetworkProtocol()))
 			return false;
 
 		if(!(((rulewc & OFMatch.OFPFW_TP_SRC) == OFMatch.OFPFW_TP_SRC) || 
 				rule.getTransportSource() == flow.getTransportSource()))
 			return false;
 
 		if(!(((rulewc & OFMatch.OFPFW_TP_DST) == OFMatch.OFPFW_TP_DST) || 
 				rule.getTransportDestination() == flow.getTransportDestination()))
 			return false;
 		
 		int ruleSrcMask = rule.getNetworkSourceMaskLen();
 		int matchSrcMask = flow.getNetworkSourceMaskLen();
 		
 		if(!(ruleSrcMask <= matchSrcMask &&
 				(rule.getNetworkSource() & ((ruleSrcMask==0)? 0:0xffffffff << (32-ruleSrcMask))) ==
 				(flow.getNetworkSource() & ((ruleSrcMask==0)? 0:0xffffffff << (32-ruleSrcMask)))))
 			return false;
 		int ruleDstMask = rule.getNetworkDestinationMaskLen();
 		int matchDstMask = flow.getNetworkDestinationMaskLen();
 		if(!(ruleDstMask <= matchDstMask &&
 				(rule.getNetworkDestination() & ((ruleDstMask==0)? 0:0xffffffff << (32-ruleDstMask))) ==
 				(flow.getNetworkDestination() & ((ruleDstMask==0)? 0:0xffffffff << (32-ruleDstMask)))))
 			return false;
 
 		return true;
 	}
 
     private boolean isSwitchAvaiForPolicy(Policy p, Long sw) {
         if (!swPolicyTuples.containsKey(sw)) {
             swPolicyTuples.put(sw, new HashSet<Policy>());
             return true;
         }
         for (Policy ponsw : swPolicyTuples.get(sw)) {
             if (checkIfPolicyCoexist(p, ponsw)) {
                 return false;
             }
         }
         return true;
     }
 	
 	private boolean checkIfPolicyCoexist(Policy p1, Policy p2){
 		for(Flow flowp1:p1.flows){
 			for(OFMatch rule:p2.rules){
 				if(flowBelongsToRule(flowp1.match, rule)) {
 					return false;
 				}
 			}
 		}
 		
 		for(Flow flowp2:p2.flows){
 			for(OFMatch rule:p1.rules){
 				if(flowBelongsToRule(flowp2.match, rule)){
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	private Set<Policy> matchPoliciesFromStorage(OFMatch match){
 		Set<Policy> matchedPolicies= new HashSet<Policy>();
 		Iterator itp = policyStorage.values().iterator();
 		while(itp.hasNext()){
 			Policy policytmp = (Policy) itp.next();
 			if(policytmp.flows.contains(Integer.valueOf(match.hashCode())))
 				continue;
 			Iterator itr = policytmp.rules.iterator();
 			while(itr.hasNext()){
 				if(flowBelongsToRule(match, (OFMatch) itr.next())) {
 					matchedPolicies.add(policytmp);
                 }
 			}
 			
 		}
 		return matchedPolicies;
 	}
 
 	private boolean processPacket(IOFSwitch sw, OFPacketIn pi, FloodlightContext cntx){
         IDevice dstDevice =
                 IDeviceService.fcStore.
                         get(cntx, IDeviceService.CONTEXT_DST_DEVICE);
         IDevice srcDevice =
                 IDeviceService.fcStore.
                         get(cntx, IDeviceService.CONTEXT_SRC_DEVICE);
 
 
         //We can't handle packets with unknown destination
         if (dstDevice == null || srcDevice == null) {
             return false;
         }
 
         SwitchPort[] dstDaps = dstDevice.getAttachmentPoints();
         SwitchPort[] srcDaps = srcDevice.getAttachmentPoints();
         if (dstDaps == null || srcDaps == null) return false;
         SwitchPort dstsw = dstDaps[0];
         SwitchPort srcsw = srcDaps[0];
 
 		OFMatch match = new OFMatch();
 		match.loadFromPacket(pi.getPacketData(), pi.getInPort());
 
         NodePortTuple srctuple = new NodePortTuple(srcsw.getSwitchDPID(), srcsw.getPort());
         NodePortTuple dsttuple = new NodePortTuple(dstsw.getSwitchDPID(), dstsw.getPort());
         OFMatch flowmatch = match.clone();
         flowmatch.setInputPort((short) 0);
         Flow flow = new Flow(flowmatch, srctuple, dsttuple);
 
         Set<Policy> policies = matchPoliciesFromStorage(match);
         log.warn("This flow matches " + policies.size() + " policies ");
         if(policies.isEmpty()) {
        	Integer wildcard_hints = null;
            IRoutingDecision decision = null;
            if (cntx != null) {
                decision = IRoutingDecision.rtStore
                        .get(cntx,
                                IRoutingDecision.CONTEXT_DECISION);
            }
            if (decision != null) {
                wildcard_hints = decision.getWildcards();
            } else {
            	// L2 only wildcard if there is no prior route decision
                wildcard_hints = ((Integer) sw
                        .getAttribute(IOFSwitch.PROP_FASTWILDCARDS))
                        .intValue()
                        & ~OFMatch.OFPFW_IN_PORT
                        & ~OFMatch.OFPFW_DL_VLAN
                        & ~OFMatch.OFPFW_DL_SRC
                        & ~OFMatch.OFPFW_DL_DST
                        & ~OFMatch.OFPFW_NW_SRC_MASK
                        & ~OFMatch.OFPFW_NW_DST_MASK;
            }
            flow.match.setWildcards(wildcard_hints.intValue());
            flowStorage.put(flow.hashCode(), flow);

             return false;
         }
        flowStorage.put(flow.hashCode(), flow);
 
         for (Policy p : policies) {
             SwitchPort oldsw = p.getSwport();
             if (findNewSwitch(p, flow)) {
                 if (!swPolicyTuples.containsKey(p.getDpid())) {
                     swPolicyTuples.put(p.getDpid(), new HashSet<Policy>());
                 }
                 swPolicyTuples.get(p.getDpid()).add(p);
                 if (oldsw != null) {
                     swPolicyTuples.get(oldsw.getSwitchDPID()).remove(p);
                 }
                 synchronized (p.getFLows()) {
                     for (Flow f : p.getFLows()) {
                         if (f.equals(flow)) {
                             continue;
                         }
                         if (oldsw != null) {
                             //Delete enqueue and queue
                             log.warn(f.getMatch().toString());
                             log.warn(f.getQmatches().toString());
                             SwitchPort newsw = p.getSwport();
                             p.setSwport(oldsw);
                             installMatchedFLowToSwitch(f.getQmatch(p), p, OFFlowMod.OFPFC_DELETE_STRICT);
                             p.setSwport(newsw);
                         }
                         //Add new routes and enqueue
                         addRouteAndEnqueue(sw, pi, cntx, p, f);
                     }
                 }
             }
             addRouteAndEnqueue(sw, pi, cntx, p, flow);
         }
 
 		return true;
 	}
 
     private void addRouteAndEnqueue(IOFSwitch sw, OFPacketIn pi, FloodlightContext cntx, Policy p, Flow f) {
         OFMatch match = f.getMatch();
         NodePortTuple src = f.lastSwExceptPolicy(p);
         NodePortTuple qs = new NodePortTuple(p.getDpid(), p.getPort());
         short qsinport;
         /* Add the route before the queue */
         Route r1_temp = routingEngine.getRoute(src.getNodeId(), src.getPortId(), p.getDpid(), p.getPort(), 0);
         if (r1_temp != null) {
             Route r1 = new Route(r1_temp.getId(), r1_temp.getPath());
             int r1len = r1.getPath().size();
             r1.getPath().remove(r1len-1);
             qsinport = r1.getPath().get(r1len-2).getPortId();
             r1.getPath().remove(r1len-2);
             long cookie1 = AppCookie.makeCookie(FORWARDING_APP_ID, 0);
             pushRoute(r1, match, match.getWildcards(), pi, sw.getId(), cookie1,
                     cntx, false, true, OFFlowMod.OFPFC_MODIFY_STRICT);
         } else {
             qsinport = pi.getInPort();
         }
 
         /* Add the route after the queue (if necessary) */
         Set<Link> links = linkService.getPortLinks().get(qs);
         if (links != null) {
             NodePortTuple nexts = null;
             for (Link l : links) {
                 if (l.getSrc() == qs.getNodeId() && l.getSrcPort() == qs.getPortId()) {
                     nexts = new NodePortTuple(l.getDst(), l.getDstPort());
                     break;
                 }
             }
             Route r2 = routingEngine.getRoute(nexts.getNodeId(), nexts.getPortId(), f.getDst().getNodeId(), f.getDst().getPortId(), 0);
             long cookie2 = AppCookie.makeCookie(FORWARDING_APP_ID, 0);
             pushRoute(r2, match, match.getWildcards(), pi, sw.getId(), cookie2,
                     cntx, false, true, OFFlowMod.OFPFC_MODIFY_STRICT);
         }
 
         /* Add the enqueue entry */
         OFMatch s2match = match.clone();
         s2match.setInputPort(qsinport);
         installMatchedFLowToSwitch(s2match, p, OFFlowMod.OFPFC_MODIFY_STRICT);
 
         f.addPolicy(p, s2match);
     }
 	
 	/**
 	 * Divide policies into sets according the switches they are installed
 	 * non-installed policies are in one set
 	 * @param policies
 	 * @param flow
 	 * @return
 	 */
     private List<ArrayList<Policy>> dividePolicyBySwitch(Set<Policy> policies, Flow flow) {
 		// TODO Here we simply order the list by switch ID. A better way is to order the switches
     	// in the sequence that flow could travel through in the shortest distance.
     	boolean inserted = false;
     	int i = 0;
     	List<ArrayList<Policy>> policySet = new LinkedList<ArrayList<Policy>>();
     	Iterator itp = policies.iterator();
     	while(itp.hasNext()){
     		Policy p = (Policy) itp.next();
     		Iterator itl = policySet.iterator();
     		ArrayList<Policy> policySameSwitch;
     		while(itl.hasNext()){
     			policySameSwitch = (ArrayList<Policy>) itl.next();
     			if(policySameSwitch.get(0).getDpid() == p.getDpid()){
     				policySameSwitch.add(p);
     				inserted = true;
     			}
     			if(policySameSwitch.get(0).getDpid() < p.getDpid()){
     				i++;
     				continue;
     			}
     			else{
     				i--;
     				break;
     			}
     		}
     		if(inserted == false){
     			policySet.add(i, new ArrayList<Policy>());
     		}else{
     			inserted = false;
     		}
     	}
 		return policySet;
 	}
 
     /**
      * Process and update the flow and the matching policies.
      * @param flow
      * @return
      */
     /*
 	private Set<Policy> processFlowWithPolicy(Flow flow, Set<Policy> policies) {
 		Set<Policy> policiesToDelete = new HashSet<Policy>();
 		List<ArrayList<Policy>> policySet = dividePolicyBySwitch(policies, flow);
 
 		//List<Integer> switches = getSwitchByPolicy(policySet);
 		
 		// Add sets of policies to the new flow
 		Iterator it = policySet.iterator();
 		while(it.hasNext()){
 			ArrayList<Policy> policySameSwitch = (ArrayList<Policy>) it.next();
 			if(policySameSwitch.get(0).getSwport() != null){
 				Policy p = policySameSwitch.get(0);
 				updatePolicyWithFlow(flow, p);
 				flow.addPolicy(p);
 				int i = 1;
 				int size = policySameSwitch.size();
 				while(i<size){
 					p = policySameSwitch.get(i);
 					updatePolicyWithFlow(flow, p);
 					if(findNewSwitch(p, flow) == false){
 						policiesToDelete.add(p);
 						deleteFlowFromPolicy(p, flow);
 					} else {
 						flow.addPolicy(p);
 					}
 					i++;
 				}
 			} else {
                 Route[] routes = (Route[]) flow.getRoutes().toArray();
 				Route route = routes[0];
 				int i = 0;
 				int size = policySameSwitch.size();
 				while(i<size){
 					Policy p = policySameSwitch.get(i);
 					if(findNewSwitch(p, route) == false){
 						policiesToDelete.add(p);
 					}else{
 						updatePolicyWithFlow(flow, p);
 						flow.addPolicy(p);
 					}
 					i++;
 				}
 			}
 		}
 		if(!flow.getPolicies().isEmpty()) {
 			flowStorage.put(flow.hashCode(), flow);
 		}
 		// we can also implement a optimization here to determine
 		// whether the switches are affecting the route of flow too much
 		return policiesToDelete;
 	}
 	*/
 	
 	private void installMatchedFLowToSwitch(OFMatch flow, Policy p, short flowModCommand){
         IOFSwitch sw = switches.get(p.getDpid());
         if(flowModCommand != OFFlowMod.OFPFC_DELETE_STRICT) {
             queueCreaterService.createQueue(sw, p.getPort(), p.queue, p.speed);
         } else {
             queueCreaterService.deleteQueue(sw, p.getPort(), p.queue);
         }
 
         OFFlowMod fm = new OFFlowMod();
         fm.setType(OFType.FLOW_MOD);
 
         List<OFAction> actions = new ArrayList<OFAction>();
 
         //add the queuing action
         OFActionEnqueue enqueue = new OFActionEnqueue();
         enqueue.setLength((short)OFActionEnqueue.MINIMUM_LENGTH);
         enqueue.setType(OFActionType.OPAQUE_ENQUEUE); // I think this happens anyway in the constructor
         //enqueue.setPort(p.getPort());
         enqueue.setQueueId(p.queue);
 
         if(flow.getInputPort() == p.getPort()){
             fm.setOutPort(OFPort.OFPP_IN_PORT);
             enqueue.setPort(OFPort.OFPP_IN_PORT.getValue());
         } else {
             fm.setOutPort(OFPort.OFPP_NONE);
             enqueue.setPort(p.getPort());
         }
 
         actions.add(enqueue);
         long cookie = AppCookie.makeCookie(FORWARDING_APP_ID, 0);
         fm.setMatch(flow)
                 .setCookie(cookie)
                 .setCommand(flowModCommand)
                 .setActions(actions)
                 .setIdleTimeout((short)5)  // infinite
                 .setHardTimeout((short) 0)  // infinite
                 .setBufferId(OFPacketOut.BUFFER_ID_NONE)
                 .setFlags(OFFlowMod.OFPFF_SEND_FLOW_REM)
                         //.setOutPort(OFPort.OFPP_NONE.getValue())
                 .setPriority(p.priority)
                 .setLengthU(OFFlowMod.MINIMUM_LENGTH+OFActionEnqueue.MINIMUM_LENGTH);
         try {
             sw.write(fm, null);
             sw.flush();
         } catch (IOException e) {
             log.error("Tried to write OFFlowMod to {} but failed: {}",
                     HexString.toHexString(sw.getId()), e.getMessage());
         }
 
 				
 	}
 
     private int canFlowPassSw(Flow flow, NodePortTuple sw, Policy p) {
         NodePortTuple src = flow.lastSwExceptPolicy(p);
         NodePortTuple dst = flow.getDst();
         Route subr1 = routingEngine.getRoute(src.getNodeId(), src.getPortId(), sw.getNodeId(), sw.getPortId(), 0);
         Route subr2 = routingEngine.getRoute(sw.getNodeId(), sw.getPortId(), dst.getNodeId(), dst.getPortId(), 0);
         if (subr1 == null ||
                 (subr2 == null && !sw.equals(dst))) {
             /* If this switch can't reach either a flow's dst or src, it's not available */
             return -1;
         }
 
         /* Check if incoming route and outgoing route shares the same link */
         if (subr2 != null) {
             int len1 = subr1.getPath().size(), len2 = subr2.getPath().size();
             List<NodePortTuple> path1 = new ArrayList<NodePortTuple>(), path2 = new ArrayList<NodePortTuple>();
             for (int i = 0; i < len1; i++) {
                 if (i % 2 == 0) {
                     path1.add(subr1.getPath().get(i));
                 }
             }
             for (int i = 0; i < len2; i++) {
                 if (i % 2 == 0) {
                     path2.add(subr2.getPath().get(i));
                 }
             }
             for (NodePortTuple npt : path1) {
                 if (path1.contains(npt)) {
                     return -1;
                 }
             }
             for (NodePortTuple npt : path2) {
                 if (path2.contains(npt)) {
                     return -1;
                 }
             }
         }
         int routelen = 0;
         routelen += subr1.getPath().size();
         if (subr2 != null) {
             routelen += subr2.getPath().size();
         }
         return routelen;
     }
 
     /* This function checks if the policy needs to change the switch */
 	private boolean findNewSwitch(Policy p, Flow flow) {
         NodePortTuple src, dst, next;
         src = flow.lastSwExceptPolicy(p);
         dst = flow.getDst();
         Route r = routingEngine.getRoute(src.getNodeId(), src.getPortId(), dst.getNodeId(), dst.getPortId(), 0);
         if (r == null) {
             /* TODO There is no route for this flow, should return error */
             return false;
         } else if (p.getFLows().isEmpty()) {
             /* If this is the first flow matched by this policy, we add a queue at the first hop */
             p.addFlow(flow);
             next = r.getPath().get(1);
             if (isSwitchAvaiForPolicy(p, next.getNodeId())) {
                 p.setSwport(new SwitchPort(next.getNodeId(), next.getPortId()));
                 return true;
             }
         } else {
             p.addFlow(flow);
             NodePortTuple qs = new NodePortTuple(p.getDpid(), p.getPort());
             if (r.getPath().contains(qs)) {
                 /* If the new flow's default route contains the queue switch, there is no need to change it */
                 return false;
             } else if (canFlowPassSw(flow, qs, p) > 0) {
                 return false;
             }
         }
 
         /* TODO If there's no overlapped switch, we need to find a new switch that can satisfy all flows (IMPORTANT!!!) */
         NodePortTuple bestsp = null;
         int bestroute = Integer.MAX_VALUE;
         for (IOFSwitch sw : switches.values()) {
             if (!isSwitchAvaiForPolicy(p, sw.getId())) {
                 continue;
             }
             for (OFPhysicalPort po : sw.getPorts()) {
                 NodePortTuple sp = new NodePortTuple(sw.getId(), po.getPortNumber());
                 if (sp.getPortId() < 0) {
                     /* Don't check negative ports (perhaps for controller connection) */
                     continue;
                 }
                 boolean available = true;
                 int routelen = 0, rl;
                 synchronized (p.getFLows()) {
                     for (Flow f : p.getFLows()) {
                         rl = canFlowPassSw(f, sp, p);
                         if (rl < 0) {
                             available = false;
                             break;
                         }
                         else routelen += rl;
                     }
                     if (available && routelen < bestroute) {
                         bestsp = sp;
                         bestroute = routelen;
                     }
                 }
             }
         }
         if (bestsp == null) {
             /* TODO There is no available switch, should return error */
             return false;
         } else {
             p.setSwport(new SwitchPort(bestsp.getNodeId(), bestsp.getPortId()));
             return true;
         }
         /* TODO Need to handle if the target switch already has a policy, and check if the policies conflict */
 	}
 
     @Override
     protected void doForwardFlow(IOFSwitch sw, OFPacketIn pi, FloodlightContext cntx, boolean requestFlowRemovedNotifn) {
         if (processPacket(sw, pi, cntx)) return;
         else super.doForwardFlow(sw, pi, cntx, requestFlowRemovedNotifn);
     }
     
     private Command handleFlowRemoved(IOFSwitch sw, OFFlowRemoved msg, FloodlightContext cntx) {
         OFMatch match = msg.getMatch();
         match.setInputPort((short) 0);
         if (!flowStorage.containsKey(match.hashCode())) {
             return Command.CONTINUE;
         } else {
             Flow flow = flowStorage.get(match.hashCode());
             for (Policy p : flow.getPolicies()) {
                 synchronized (p.getFLows()) {
                     p.getFLows().remove(flow);
                 }
                 /*
                 if (p.getFLows().isEmpty()) {
                     queueCreaterService.deleteQueue(switches.get(p.getDpid()), p.getPort(), p.queue);
                 }
                 */
             }
             log.warn("Remove Flow: " + flow.getMatch().toString());
             flowStorage.remove(flow);
             return Command.CONTINUE;
         }
     }
     
     @Override
     public Command receive(IOFSwitch sw, OFMessage msg,
                            FloodlightContext cntx) {
         switch (msg.getType()) {
             case PACKET_IN:
                 IRoutingDecision decision = null;
                 if (cntx != null)
                      decision =
                              IRoutingDecision.rtStore.get(cntx,
                                                           IRoutingDecision.CONTEXT_DECISION);
 
                 return this.processPacketInMessage(sw,
                                                    (OFPacketIn) msg,
                                                    decision,
                                                    cntx);
             case FLOW_REMOVED:
                 return handleFlowRemoved(sw, (OFFlowRemoved) msg, cntx);
             default:
                 break;
         }
         return Command.CONTINUE;
     }
 
     @Override
     public void startUp(FloodlightModuleContext context) {
     	super.startUp(context);
     	restApi.addRestletRoutable(new RateLimiterWeb());	
         floodlightProvider.addOFMessageListener(OFType.FLOW_REMOVED, this);
     }
     
     @Override
     public Collection<Class<? extends IFloodlightService>> getModuleServices() {
         // We don't export any services
     	Collection<Class<? extends IFloodlightService>> l = 
                 new ArrayList<Class<? extends IFloodlightService>>();
         l.add(RateLimiterService.class);
         return l;
     }
     
     @Override
 	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
 		Map<Class<? extends IFloodlightService>,
         IFloodlightService> m = 
         new HashMap<Class<? extends IFloodlightService>,
         IFloodlightService>();
         // We are the class that implements the service
         m.put(RateLimiterService.class, this);
         return m;
 	}
     
     public void init(FloodlightModuleContext context) throws FloodlightModuleException {
         super.init();
         this.floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
         this.deviceManager = context.getServiceImpl(IDeviceService.class);
         this.routingEngine = context.getServiceImpl(IRoutingService.class);
         this.topology = context.getServiceImpl(ITopologyService.class);
         this.counterStore = context.getServiceImpl(ICounterStoreService.class);
         this.linkService = context.getServiceImpl(ILinkDiscoveryService.class);
         this.queueCreaterService = context.getServiceImpl(IQueueCreaterService.class);
         this.restApi = context.getServiceImpl(IRestApiService.class);
         this.policyStorage = new HashMap<Integer, Policy>();
     	this.flowStorage = new HashMap<Integer, Flow>();
     	
         switches = floodlightProvider.getSwitches();
         swPolicyTuples = new HashMap<Long, Set<Policy>>();
         for (Long s : switches.keySet()) {
             swPolicyTuples.put(s, new HashSet<Policy>());
         }
 /*
         OFMatch temp_match = new OFMatch(), temp2 = new OFMatch();
         temp_match.setWildcards(~(OFMatch.OFPFW_NW_DST_MASK));
         temp_match.setNetworkDestination(167772164);
         temp2.setWildcards(~(OFMatch.OFPFW_NW_SRC_MASK));
         temp2.setNetworkSource(167772163);
 
         Set<OFMatch> temp_policyset = new HashSet<OFMatch>(), temp2_set = new HashSet<OFMatch>();
         temp_policyset.add(temp_match);
         temp2_set.add(temp2);
         Policy temp_policy = new Policy(temp_policyset, (short)1);
         Policy temp2_pol = new Policy(temp2_set, (short) 1);
         temp_policy.setQueue(1);
         temp2_pol.setQueue(1);
         log.info("policyid: " + temp_policy.policyid);
         for(OFMatch match:temp_policy.getRules()){
         	log.info(match.toString());
         }
         policyStorage.put(Integer.valueOf(temp_policy.hashCode()), temp_policy);
         policyStorage.put(Integer.valueOf(temp2_pol.hashCode()), temp2_pol);
 */
         // read our config options
         Map<String, String> configOptions = context.getConfigParams(this);
         try {
             String idleTimeout = configOptions.get("idletimeout");
             if (idleTimeout != null) {
                 FLOWMOD_DEFAULT_IDLE_TIMEOUT = Short.parseShort(idleTimeout);
             }
         } catch (NumberFormatException e) {
             log.warn("Error parsing flow idle timeout, " +
             		 "using default of {} seconds",
                      FLOWMOD_DEFAULT_IDLE_TIMEOUT);
         }
         try {
             String hardTimeout = configOptions.get("hardtimeout");
             if (hardTimeout != null) {
                 FLOWMOD_DEFAULT_HARD_TIMEOUT = Short.parseShort(hardTimeout);
             }
         } catch (NumberFormatException e) {
             log.warn("Error parsing flow hard timeout, " +
             		 "using default of {} seconds",
                      FLOWMOD_DEFAULT_HARD_TIMEOUT);
         }
         log.debug("FlowMod idle timeout set to {} seconds", 
                   FLOWMOD_DEFAULT_IDLE_TIMEOUT);
         log.debug("FlowMod hard timeout set to {} seconds", 
                   FLOWMOD_DEFAULT_HARD_TIMEOUT);
     }
 
 	@Override
 	public synchronized void addPolicy(Policy p) {
 		policyStorage.put(p.policyid, p);
         for (Flow f : flowStorage.values()) {
             boolean matchpolicy = false;
             for (OFMatch m : p.getRules()) {
                 log.warn("Checking new policy: " + m.toString());
                 if (flowBelongsToRule(f.getMatch(), m)) {
                     matchpolicy = true;
                     break;
                 }
             }
             if (matchpolicy) {
                 NodePortTuple src = f.getSrc();
                 OFFlowMod fm =
                         (OFFlowMod) floodlightProvider.getOFMessageFactory()
                                 .getMessage(OFType.FLOW_MOD);
                 long cookie = AppCookie.makeCookie(FORWARDING_APP_ID, 0);
 
                 fm.setIdleTimeout(FLOWMOD_DEFAULT_IDLE_TIMEOUT)
                         .setHardTimeout(FLOWMOD_DEFAULT_HARD_TIMEOUT)
                         .setBufferId(OFPacketOut.BUFFER_ID_NONE)
                         .setCookie(cookie)
                         .setCommand(OFFlowMod.OFPFC_DELETE_STRICT)
                         .setMatch(f.getMatch())
                         .setLengthU(OFFlowMod.MINIMUM_LENGTH);
 
                 fm.getMatch().setInputPort(src.getPortId());
                 IOFSwitch sw = switches.get(src.getNodeId());
                 log.warn("Remove flow on sw" + src.toString());
                 try {
                     sw.write(fm, null);
                     sw.flush();
                 } catch (IOException e) {
                     log.error("Tried to write OFFlowMod to {} but failed: {}",
                             HexString.toHexString(sw.getId()), e.getMessage());
                 }
             }
         }
 	}
 
 	@Override
 	public synchronized void deletePolicy(Policy p) {
 		// TODO Auto-generated method stub
 		policyStorage.remove(p.policyid);
 	}
 
 	@Override
 	public boolean checkIfPolicyExists(Policy policy) {
 		// TODO Auto-generated method stub
 		if(policyStorage.containsKey(policy.policyid)){
 			return true;
 		}
 		log.info("policyid: "+policy.policyid);
 		for(OFMatch match:policy.getRules()){
         	log.info(match.toString());
         }
 		return false;
 	}
 
 	@Override
 	public List<Policy> getPolicies() {
 		// TODO Auto-generated method stub
 		return (List<Policy>) policyStorage.values();
 	}
 }
