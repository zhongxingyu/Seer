 package ext.jist.swans.app;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import jist.runtime.JistAPI;
 import jist.swans.Constants;
 import jist.swans.mac.MacAddress;
 import jist.swans.misc.Message;
 import jist.swans.net.NetAddress;
 import ducks.driver.SimParams;
 import ext.util.stats.DucksCompositionStats;
 
 public class AppCiANProvider extends AppCiANBase
 {
     private static final int              STATE_LISTENING = 0;
     private static final int              STATE_OBSERVING = 1;
     private static final int              STATE_EXECUTING = 2;
 
     private int                           state;
     private HashMap<String, CiANWorkflow> pendingWf;
     private HashMap<Character, Integer>   repository;
 
     private List<Message>                 completedMessages;
     private List<String>                  responsesSent;
 
     public AppCiANProvider(int nodeId, DucksCompositionStats stats, String mode, HashMap<Character, Integer> repository) {
         super(nodeId, stats);
         this.state = STATE_LISTENING;
         this.pendingWf = new HashMap<String, CiANWorkflow>();
         this.completedMessages = new ArrayList<Message>();
         this.responsesSent = new ArrayList<String>();
         this.repository = repository;
     }
 
     @Override
     public void run(String[] args) {
     }
 
     @Override
     public void receive(Message msg, NetAddress src, MacAddress lastHop, byte macId, NetAddress dst, byte priority,
             byte ttl) {
 
         // avoid duplicate messages
         if (completedMessages.contains(msg)) {
             return;
         } else {
             completedMessages.add(msg);
         }
 
         // handle message
         if (msg instanceof CiANWorkflowRequest) {
             handleWorkflowRequest((CiANWorkflowRequest) msg, src);
         } else if (msg instanceof CiANToken) {
             handleToken((CiANToken) msg, src);
         } else if (msg instanceof CiANDiscoveryRequest) {
             handleDiscoveryRequest((CiANDiscoveryRequest) msg, src);
         } else {
             return;
         }
     }
 
     protected void handleDiscoveryRequest(CiANDiscoveryRequest msg, NetAddress src) {
         switch (state) {
             case STATE_LISTENING:
                 String id = msg.getId() + "@" + msg.getVersion();
                 if (!responsesSent.contains(id) && !msg.isExpired()) {
                     // First forward it to neighbours
                     responsesSent.add(id);
                     send(msg);
 
                     // And finally send a response to the initiator
                     // To simplify, we announce that we can handle every service
                     // TODO To be closer to CiAN implementation, we should respond to only some (or even one service)
                     //      chosen arbitrarily. That would allow maybe the initiator to remove its AWAITING_TIMER state.
                     CiANDiscoveryResponse response = new CiANDiscoveryResponse(msg.getId(), nodeId, msg.getServices(),
                             msg.getTtl());
                     send(response, msg.getInitiatorAddress());
                 }
                 break;
             default:
                 break;
         }
     }
 
     protected void handleWorkflowRequest(CiANWorkflowRequest req, NetAddress src) {
         CiANWorkflow wf = null;
         String reqId = req.getId();
 
         switch (state) {
             case STATE_LISTENING:
                 wf = new CiANWorkflow(reqId, req.getServicesCopy(), req.getInputCopy(), req.getProvidersCopy());
                 pendingWf.put(wf.getId(), wf);
 
                 // Check if we are the first provider
                 if (nodeId == wf.getProvidersFor(0).get(0).getNodeId()) {
                     state = STATE_EXECUTING;
                     execute(wf, 0);
                 } else {
                     state = STATE_OBSERVING;
                 }
                 break;
             default:
                 break; // commit to one workflow at a time
         }
     }
 
     protected void handleToken(CiANToken t, NetAddress src) {
         String wfId = t.getId();
         CiANWorkflow wf = pendingWf.get(wfId);
         if (wf == null)
             return;
 
         char service = t.getService();
         int index = wf.getIndexForService(service);
         int in = t.getInput();
         wf.updateInputFor(service, in);
 
         // Check that we really are the good provider
         if (nodeId != wf.getProvidersFor(index).get(0).getNodeId())
             return;
 
         switch (state) {
             case STATE_OBSERVING:
                 // TODO Do not execute until we are sure we received ALL tokens for all predecessors of this service
                 execute(wf, index);
                 break;
             default:
                 break;
         }
     }
 
     private void execute(CiANWorkflow wf, int index) {
         String wfId = wf.getId();
 
         char service = wf.getServiceForIndex(index);
         int in = wf.getInputForService(service);
         if (in == -2) {
             cleanup(wfId);
             return;
         }
        compositionStats.registerForwardToExecEndTime(String.valueOf(service), wf.getId(), JistAPI.getTime());
 
         in = serviceImpl(in, repository.get(service));
         compositionStats.incrementInvokeSuccess(String.valueOf(service));
         compositionStats.setLastServiceExecuted(String.valueOf(service));
         char nextService = wf.getSuccessorService(service);
         wf.updateInputFor(nextService, in);
         
         // TODO handoff to every successors and not rely on (index + 1) anymore
         handOff(wfId, nextService, in, wf.getProvidersFor(index + 1).get(0), wf.isLastService(nextService));
 
         // If we must wait for another service to execute
         int size = wf.getServices().length;
         for (int i = index + 1; i < size; ++i) {
             if (nodeId == wf.getProvidersFor(i).get(0).getNodeId()) {
                 state = STATE_OBSERVING;
                 return;
             }
         }
 
         cleanup(wfId);
     }
 
     private void handOff(String wfId, char service, int in, CiANProvider provider, boolean isLast) {
         CiANToken t = new CiANToken(wfId, service, in);
         compositionStats.registerForwardToExecStartTime(
                 isLast ? CiANWorkflow.STRING_DESTINATION : String.valueOf(service), wfId, JistAPI.getTime());
        send(t, provider.getAddress());
     }
 
     private int serviceImpl(int in, int waitTime) {
         int out = 0;
         out = in + 1;
         JistAPI.sleep(waitTime * Constants.MILLI_SECOND);
         return out;
     }
 
     private void cleanup(String wfId) {
         pendingWf.remove(wfId);
         state = STATE_LISTENING;
     }
 
 }
