 /***************************************************************************
 *                                                                          *
 *  Organization: Lawrence Livermore National Lab (LLNL)                    *
 *   Directorate: Computation                                               *
 *    Department: Computing Applications and Research                       *
 *      Division: S&T Global Security                                       *
 *        Matrix: Atmospheric, Earth and Energy Division                    *
 *       Program: PCMDI                                                     *
 *       Project: Earth Systems Grid Federation (ESGF) Data Node Software   *
 *  First Author: Gavin M. Bell (gavin@llnl.gov)                            *
 *                                                                          *
 ****************************************************************************
 *                                                                          *
 *   Copyright (c) 2009, Lawrence Livermore National Security, LLC.         *
 *   Produced at the Lawrence Livermore National Laboratory                 *
 *   Written by: Gavin M. Bell (gavin@llnl.gov)                             *
 *   LLNL-CODE-420962                                                       *
 *                                                                          *
 *   All rights reserved. This file is part of the:                         *
 *   Earth System Grid Federation (ESGF) Data Node Software Stack           *
 *                                                                          *
 *   For details, see http://esgf.org/esg-node/                             *
 *   Please also read this link                                             *
 *    http://esgf.org/LICENSE                                               *
 *                                                                          *
 *   * Redistribution and use in source and binary forms, with or           *
 *   without modification, are permitted provided that the following        *
 *   conditions are met:                                                    *
 *                                                                          *
 *   * Redistributions of source code must retain the above copyright       *
 *   notice, this list of conditions and the disclaimer below.              *
 *                                                                          *
 *   * Redistributions in binary form must reproduce the above copyright    *
 *   notice, this list of conditions and the disclaimer (as noted below)    *
 *   in the documentation and/or other materials provided with the          *
 *   distribution.                                                          *
 *                                                                          *
 *   Neither the name of the LLNS/LLNL nor the names of its contributors    *
 *   may be used to endorse or promote products derived from this           *
 *   software without specific prior written permission.                    *
 *                                                                          *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS    *
 *   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT      *
 *   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS      *
 *   FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL LAWRENCE    *
 *   LIVERMORE NATIONAL SECURITY, LLC, THE U.S. DEPARTMENT OF ENERGY OR     *
 *   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,           *
 *   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT       *
 *   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF       *
 *   USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND    *
 *   ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,     *
 *   OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT     *
 *   OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF     *
 *   SUCH DAMAGE.                                                           *
 *                                                                          *
 ***************************************************************************/
 
 /**
    Description:
 
    This class also MANAGES peer proxy object(s)
    (ex:BasicPeer) that communicate OUT (egress) to the
    peer(s).
 
 **/
 package esg.node.connection;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.impl.*;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Set;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.Properties;
 import java.util.Date;
 import java.util.concurrent.atomic.AtomicLong;
 
 import esg.common.Utils;
 import esg.common.util.ESGFProperties;
 import esg.common.service.ESGRemoteEvent;
 import esg.node.core.ESGPeerListener;
 import esg.node.core.AbstractDataNodeManager;
 import esg.node.core.ESGDataNodeManager;
 import esg.node.core.AbstractDataNodeComponent;
 import esg.node.core.DataNodeComponent;
 import esg.node.core.ESGEvent;
 import esg.node.core.ESGEventHelper;
 import esg.node.core.ESGJoinEvent;
 import esg.node.core.ESGPeerEvent;
 import esg.node.core.ESGPeer;
 import esg.node.core.BasicPeer;
 import esg.node.core.ESGCallableEvent;
 import esg.node.core.ESGCallableFutureEvent;
 
 import esg.common.generated.registration.*;
 import esg.node.components.registry.RegistryUpdateDigest;
 
 public class ESGConnectionManager extends AbstractDataNodeComponent implements ESGPeerListener {
 
     private static final Log log = LogFactory.getLog(ESGConnectionManager.class);
 
     private Properties props = null;
 
     private AtomicLong lastDispatchTime = null;
     private Map<String,ESGPeer> peers = null;
     private Map<String,ESGPeer> unavailablePeers = null;
     private RegistryUpdateDigest lastRud = null;
     private ESGPeer defaultPeer = null;
     private boolean shutdownHookLatch = false;
 
     public ESGConnectionManager(String name) {
         super(name);
         log.info("Instantiating ESGConnectionManager...");
     }
     
     //Bootstrap the rest of the subsystems... (ESGDataNodeServiceImpl really bootstraps)
     public void init() {
         log.info("Initializing ESGFConnectionManager...");
        lastDispatchTime = new AtomicLong(-1L);
 
         //NOTE:
         //Just to make sure we have these guys if we decide to re-register.
         //since we did such a good job cleaning things out with we unregister.
         //Once could imagine wanting to re-establish the connection manager.
         if(peers == null) peers = Collections.synchronizedMap(new HashMap<String,ESGPeer>());
         if(unavailablePeers == null) unavailablePeers = Collections.synchronizedMap(new HashMap<String,ESGPeer>());
         
         try{
             props = new ESGFProperties();
             periodicallyPingToPeers();
             periodicallyRegisterToPeers();
         }catch(java.io.IOException e) {
             System.out.println("Damn, ESGConnectionManager, can't fire up... :-(");
             log.error(e);
         }
 
         Runtime.getRuntime().addShutdownHook(new Thread() {
                 public void run(){
                     if(ESGConnectionManager.this.shutdownHookLatch){
                         System.out.println("Running Connection Manager Shutdown Hook");
                         ESGConnectionManager.this.dispatchUnRegisterToPeers();
                         System.out.println("Bye!");
                     }
                     ESGConnectionManager.this.shutdownHookLatch=true;
                 }
             });
     }
 
     //--------------------------------------------
     // Status Methods
     //--------------------------------------------
     
     public int numAvailablePeers() { return peers.size(); }
     public int numUnavailablePeers() { return unavailablePeers.size(); }
 
     
     
     private void periodicallyPingToPeers() {
         log.trace("Launching ping timer...");
         long delay  = Long.parseLong(props.getProperty("conn.ping.initialDelay","5"));
         long period = Long.parseLong(props.getProperty("conn.ping.period","30"));
         log.trace("connection ping delay:  "+delay+" sec");
         log.trace("connection ping period: "+period+" sec");
        
         Timer timer = new Timer("Peer-Sweep-Timer");
         timer.schedule(new TimerTask() { 
                 public final void run() {
                     ESGConnectionManager.this.pingToPeers();
                 }
             },delay*1000,period*1000);
     }
 
     //TODO: Instead of making this a sync'ed method, turn this into a
     //"future" tracking invocation and 'catch' responses or lack there
     //of without locking things up.
     private synchronized void pingToPeers() {
         java.util.Vector<ESGPeer> peers_ = new java.util.Vector<ESGPeer>();
         peers_.addAll(unavailablePeers.values());
         for(ESGPeer peer: peers_) {
             log.trace("Inspecting ["+peers_.size()+"] marked peers");
             if(peer.equals(defaultPeer)) log.trace("(default peer)");
             //TODO: put in random selection and or heartbeat/leasing here...
             //this is where the relationship maintenance code goes
             //and detecting when folks fall out of the system.
             //maybe ping should be expanded to put in lease negotiation proper.
             peer.ping();
         }
         peers_.clear();
         peers_ = null; //gc niceness...
     }
 
     //Does a brute force check (pings) against all known peers
     //By doing so we are left with a list of peers that are all active (responding positively)
     //return value of true means that some pruning did take place.
     public boolean prune() {
         log.trace("prune() ...");
         int pruneCount = 0;
         java.util.Vector<ESGPeer> peers_ = new java.util.Vector<ESGPeer>();
         peers_.addAll(peers.values());
         log.trace("Inspecting ["+peers_.size()+"] currently known peers");
         for(ESGPeer peer: peers_) {
             if(peer.equals(defaultPeer)) log.trace("(default peer)");
             if(!peer.ping()) {
                 pruneCount++;
                 log.trace("Pruning out unresponsive peer: ("+pruneCount+") "+peer.getServiceURL());
             }
         }
         log.trace("Total number of pruned peers: ["+pruneCount+"] / ["+peers_.size()+"]");
         peers_.clear();
 
         peers_.addAll(unavailablePeers.values());
         log.trace("Inspecting ["+peers_.size()+"] currently dead peers");
         for(ESGPeer peer: peers_) {
             log.trace("Purging dead peer: "+peer);
             peer.unregister();
         }
 
         peers_.clear();
         peers_ = null; //gc niceness...
         log.trace("--> returning "+(pruneCount > 0));
         return (pruneCount > 0);
     }
 
     private void periodicallyRegisterToPeers() {
         log.trace("Launching connection manager's registration push timer...");
         long delay  = Long.parseLong(props.getProperty("conn.mgr.initialDelay","10"));
         final long period = Long.parseLong(props.getProperty("conn.mgr.period","30"));
         log.trace("connection registration delay:  "+delay+" sec");
         log.trace("connection registration period: "+period+" sec");
 	
         Timer timer = new Timer("Reg-Repush-Timer");
         
         //This will transition from active map to inactive map
         timer.schedule(new TimerTask() {
                 public final void run() {
                     log.debug("(Timer) Re-Pushing My Last Registry State (Event)");
                     Date now = new Date();
                     long delta=(now.getTime() - lastDispatchTime.longValue());
                     if ( delta > (period*1000)) {
                     ESGConnectionManager.this.getESGEventQueue().enqueueEvent(
                                      new ESGCallableFutureEvent<Boolean>(ESGConnectionManager.this,
                                                                          Boolean.valueOf(false),
                                                                          "Registration Re-push Event") {
                                          public boolean call(DataNodeComponent contextComponent) {
                                              log.trace("Registration Re-push \"Call\"'ed...");
                                              boolean handled = false;
                                              try{
                                                  //Note: since "Boolean" generic, setData needs to take a value of that type
                                                  //"handled" plays *two* roles. 1) It is the Data that is being retrieved and stored
                                                  //by whatever process is coded (in this case calling sendOutRegistryState()).
                                                  //2) It is also setting the return value for this "call" method being implemented
                                                  //that indicates if this call was successfully handled.  The former has its type
                                                  //dictated by the generic.  The latter is always a boolean.
                                                  setData(handled=((ESGConnectionManager)contextComponent).sendOutRegistryState());
                                              }finally{
                                                  log.info("Registration Re-push completed ["+handled+"]");
                                              }
                                              return handled;
                                          }
                                      });
                     }else {
                         log.debug("NOT performing re-push - last message sent "+delta+"secs ago < "+period+"secs");
                     }
                 }
             },delay*1000,period*1000);
     }
 
 
     //We will consider this communications closed (essentially making
     //this unavailable for ingress communication) if there are no
     //peers to communicate with. That would be because:
     //1) There are no peer proxy objects available for us to use
     //2) If the peer proxy objects we DO have are no longer valid
     //(we just need one to be valid for us to be available) 
 
     //NOTE (TODO): review this policy! *for now* good enough as we are
     //only planning on having a 1:1 between data node and peers... but
     //we could imagine that it would only take just having one peer
     //that is valid holding open the door for us to be DOS-ed by folks
     //maliciously sending us huge events that flood our system.
     public boolean amAvailable() {
         //boolean amAvailable = false;
         //boolean haveValidPeerProxies = false;
         //if (peers.isEmpty()) { amAvailable = false; return false; }
         //Collection<? extends ESGPeer> peers_ = peers.values();
         //for(ESGPeer peer: peers_) {
         //    haveValidPeerProxies |= peer.isAvailable();
         //}
         //amAvailable = (amAvailable || haveValidPeerProxies );
         //return amAvailable;
         return true;
     }
     
     public void unregister() {
         //TODO: Be nice and send all the peers termination events
         //clear out my datastrutures of node proxies
         peers.clear(); peers = null; //gc niceness
         unavailablePeers.clear(); unavailablePeers = null; //gc niceness
         super.unregister();
     }
 
     //-------
     //quick helper method
     //-------
     private boolean checkEvent(ESGEvent event) {
         ESGRemoteEvent rEvent=null;
         if((rEvent = event.getRemoteEvent()) == null) {
             log.warn("The encountered event does not contain a remote event, which is needed for egress routing [event dropped]");
             event = null; //gc hint!
             return false;
         }
         if(!rEvent.isValid()) {
             log.warn("Will NOT send invalid RemoteEvent "+rEvent);
             return false;
         }
         return true;
     }
     //-------
     
     //Cached last registry data and checksum in lastRud.
     //Send out the same info to another random pair of neighbors.
     private synchronized boolean sendOutRegistryState() {
         //Bootstrap condition...
         if(lastRud == null && defaultPeer != null) {
             //Damnit, I didn't want this dependency..!!!
             esg.node.components.registry.RegistrationGleaner ephemeralGleaner = new esg.node.components.registry.RegistrationGleaner();
             String registration = null;
             try{
                 registration = ephemeralGleaner.loadMyRegistration().toString();
             }catch(Throwable t) {
                 System.err.println("CONN MGR: no registration available (this thread may have jumped the gun) no worries...");
                 log.error(t);
             }
 
             if(registration == null) {
                 log.warn("(bootstrapping) Sorry no registration information yet available... check again later");
                 return false;
             }
             defaultPeer.handleESGRemoteEvent(new ESGRemoteEvent(Utils.getMyServiceUrl(),
                                                                 ESGRemoteEvent.REGISTER,
                                                                 registration,
                                                                 ephemeralGleaner.getMyChecksum(),
                                                                 Utils.nextSeq(),
                                                                 5));
             log.info("Bootstrapping... sending out my registration... ");
             log.trace("My Registration is:"+ registration);
             ephemeralGleaner = null; //gc niceness.
             return true;
         }
         //delagate through with no so "new" state :-)
         if(lastRud != null) {
             log.trace("Using cached state...");
             return this.sendOutNewRegistryState(this.lastRud.xmlDocument(),this.lastRud.xmlChecksum());
         }
         return false;
     }
     
     //Helper method containing the details of the Gossip protocol dispatch logic
     //Basically - choose two random peers (that are not me) to send my state to.
     private synchronized boolean sendOutNewRegistryState(String xmlDocument, String xmlChecksum) {
         log.trace("Sending out registry state...");
         
         if((peers.size() < 1) && (defaultPeer == null)) {
             log.info("No one to send to... you have no peers.  Nothing further to do. waiting to be contacted... (I am probably my own default peer)");
             return false;
         }
         ESGRemoteEvent myRegistryState = new ESGRemoteEvent(Utils.getMyServiceUrl(),
                                                             ESGRemoteEvent.REGISTER,
                                                             xmlDocument,
                                                             xmlChecksum,
                                                             Utils.nextSeq(),
                                                             5);
         return dispatchToRandomPeers(myRegistryState);
     }
 
 
     //--------------------------------------------
     //Remote Event Dispatching
     //--------------------------------------------
 
     private boolean dispatchToRandomPeers(ESGEvent event) {
         return dispatchToRandomPeers(event.getRemoteEvent());
     }
     private boolean dispatchToRandomPeers(ESGRemoteEvent remoteEvent) {
         //------------
         //If we have no peers we have to resort to using our defaultPeer...
         if((peers.size() == 0)  && (defaultPeer != null)) {
             log.info("You have no peers - resorting to harassing the default peer ["+defaultPeer.getServiceURL()+"]");
             defaultPeer.handleESGRemoteEvent(remoteEvent);
             return true;
         }
         //------------
 
         if(!remoteEvent.checkTTL()) {
             log.trace("The buck stops here... will not propagate an event with exhausted TTL ["+remoteEvent.getTTL()+"]");
             return true;
         }
 
         int networkSizeLimit = 10000; //Essentially the total number of nodes to randomly choose from is between 0 and networkSizeLimit+1
         int retries = 3; //how many times to try to get this event to [branchFactor] peers
         int numDispatchedPeers = 0; //how many peers have successfully had events sent to them.
         int branchFactor = 2; //how many peers we need to send to on the next hop
         int idx = -1; // index into list of peer (stub) objects
         int lastIdx = -1; //the last index value that you choose.
         int rechooseLimit = 4; //number of times to select a peer that you haven't selected before
 
         List<ESGPeer> peerList = new ArrayList<ESGPeer>(peers.values());
         
         for(int i=0; i < retries; i++)  {
             //It is possible, to randomly keep getting the same index
             //number again and again to prevent that we try up to
             //[rechooseLimit] times to select a different peer If we hit
             //the limit we re-try again up to [retries] times.
             
             //So if you are tremendously unlucky or in a situation
             //where there is less than 1 other peer to send to, you
             //will do this reselection a bounded number of times.
             //Also if you have selected [branchFactor] distinct number
             //of peers to dispatch to but they both were "bad" then
             //you
             int rechooseIndexCount = 0; 
             while((numDispatchedPeers < branchFactor)) {
                 //Randomly select a peer to send our state to...
                 if(peerList.size() == 0) {
                     log.warn("no peers");
                     break;
                 }
 
                 idx = ((int)(Math.random()*networkSizeLimit)) % peerList.size();
 
                 //Notice that the following single step check works
                 //well because our branching factor is 2 otherwise
                 //we'd have to check in a SET of previously selected
                 //values or something
                 if(lastIdx == idx) { 
                     if((++rechooseIndexCount % rechooseLimit) == 0) {
                         log.trace("exhaused attempts ["+rechooseLimit+"] to select a different peer");
                         break;
                     }
                     log.trace("already choose that peer....");
                     continue;
                 }
                 rechooseIndexCount = 0;
 
                 //NOTE: I can't check for "success" of the message
                 //getting to the peer so there could be the case where
                 //my bad luck has choosen two dead beat peers and I
                 //would not know and thus the message propagation
                 //would stop dead in its tracks.  Though, if I did
                 //such a thing the peers would send a signal to purge
                 //themselves from the active list, but still be in the
                 //node managers peer list.... so I thinkI need to
                 //recant the preceding paragraph.  I do want some
                 //reasonable notion that I am not sending messages to
                 //dead machines.... Okay I have convinced myself to
                 //use the local active data structure...  Rule of
                 //thumb, keep things local to this object as much as
                 //you can. And try to stay on the stack not heap (yes,
                 //in Java it's hard)
 
                 ESGPeer chosenPeer = peerList.get(idx);
                 log.debug("Selected Peer: "+chosenPeer.getName());
                 chosenPeer.handleESGRemoteEvent(ESGEventHelper.createRelayedOutboundEvent(remoteEvent));
                 lastIdx = idx;
                 numDispatchedPeers++;
             }
             if(numDispatchedPeers >= branchFactor) break;
         }
         lastDispatchTime.set((new Date()).getTime());
         return (numDispatchedPeers > 1); //I was at least able to get one off!
     }
     
     private boolean dispatchResponseToSource(ESGEvent event) {
         if(!checkEvent(event)) return false;
 
         ESGRemoteEvent remoteEvent = event.getRemoteEvent();
         String targetAddress = null;
         ESGPeer targetPeer = null;
 
         //Responding back to message source...
         if((targetPeer = peers.get(targetAddress=remoteEvent.getSource())) == null) {
             targetPeer = unavailablePeers.get(targetAddress);
             log.error("Specified source peer named by ["+targetAddress+"] is "+
                       ((targetPeer == null) ? "unknown " : "unavailable ")+"[event dropped]");
             return false;
         }
         log.info("Dispatching Event Back To Source: "+targetAddress);
         targetPeer.handleESGRemoteEvent(ESGEventHelper.createResponseOutboundEvent(event));
         return true;
     }
 
     private boolean dispatchResponseToOrigin(ESGEvent event) {
         if(!checkEvent(event)) return false;
 
         ESGRemoteEvent remoteEvent = event.getRemoteEvent();
         String targetAddress = null;
         ESGPeer targetPeer = null;
 
         //Responding back to message origin...
         if((targetPeer = peers.get(targetAddress=remoteEvent.getOrigin())) == null) {
             targetPeer = unavailablePeers.get(targetAddress);
             log.error("Specified origin peer named by ["+targetAddress+"] is "+
                       ((targetPeer == null) ? "unknown " : "unavailable ")+"[event dropped]");
             return false;
         }
         log.info("Dispatching Event Back To Origin: "+targetAddress);
         targetPeer.handleESGRemoteEvent(ESGEventHelper.createResponseOutboundEvent(event));
         return true;
     }
 
     private boolean dispatchUnRegisterToPeers() {
         System.out.println("I am dispatching UnRegister Event To Peers");
         String now = (new java.util.Date()).getTime()+""; //yeah... ugly... :-\
         ESGRemoteEvent unregisterEvent = new ESGRemoteEvent(Utils.getMyServiceUrl(),
                                                             ESGRemoteEvent.UNREGISTER,
                                                             now,
                                                             Utils.hashSum(now),
                                                             Utils.nextSeq());
         System.out.println(unregisterEvent);
         return dispatchToRandomPeers(unregisterEvent);
     }
 
     //--------------------------------------------
     //Event handling...
     //--------------------------------------------
 
     public boolean handleESGQueuedEvent(ESGEvent event) {
         log.trace("["+getName()+"]:["+this.getClass().getName()+"]: Got A QueuedEvent!!!!: "+event);
 
         //--------------------
         //Routing Registration Update Events ONLY...
         //--------------------
         if(event.getData() instanceof RegistryUpdateDigest) {
             log.trace("Getting update information regarding internal representation of the federation");
             RegistryUpdateDigest rud = (RegistryUpdateDigest)event.getData();
             
             //Add all the newly discovered peers that I don't already
             //know first hand are active... but they are not fully "available"
             //yet.
             ESGPeer peer = null;
             String peerServiceUrl = null;
             Set<Node> updatedNodes = null;
             if(null != (updatedNodes = rud.updatedNodes())) {
                 for(Node node : updatedNodes) {
 
                     //Scenario A:
                     //This was the first way... Where we enforced the service url... maybe not a bad idea?
                     //peer = peers.get(peerServiceUrl = Utils.asServiceUrl(node.getHostname()));
 
                     //Scenario B: Get the service endpoint advertised by the peer in their registration...
                     //Check this node to see if it has an entry for a node manager... (required);
 
                     peer = null;
                     peerServiceUrl = null;
 
                     try{
                         peerServiceUrl = node.getNodeManager().getEndpoint();
                     }catch (Throwable t) { 
                         log.warn(node.getHostname()+" does not seem to be running a node manager thus, not qualified to be a peer... dropping'em"); 
                         continue;
                     }
 
                     peer = peers.get(peerServiceUrl);
 
                     //If we don't have you in our peer list then we'll add
                     //you... (indirectly) The act of registering this new
                     //peer fires off a join event which is caught and
                     //handled below in the implementation of
                     //this.handleESGEvent where the peer is then added to
                     //the peers datastructure (map).
                     try{
                         //shall never store myself as a peer.
                         if(Utils.getMyServiceUrl().equals(peerServiceUrl)) {
                             log.warn("I should not be even attempting to store myself as my own peer!");
                             continue;
                         }
                         if (peer == null) {
                             getDataNodeManager().registerPeer(new BasicPeer(peerServiceUrl, ESGPeer.PEER));
                         }
                     }catch(java.net.MalformedURLException e) {
                         log.error(e);
                         log.error("This url was not recognized as a node manager url, no need to go further - Drop it like it's hot...");
                         return false;
                     }
                 }
             }
             lastRud=rud;
             if(rud != null) {
                 return sendOutNewRegistryState(rud.xmlDocument(), rud.xmlChecksum());  //dispatch method
             }else {
                 log.warn("Sorry rud is: ["+rud+"] will not attempt to send out registration");
             }
         }else{
             //--------------------
             //Routing of events...
             //--------------------
             boolean handled = false;
             if(event.hasRemoteEvent()) {
                 int eventType = event.getRemoteEvent().getMessageType();
                 switch (eventType) {
                 case ESGRemoteEvent.REGISTER:
                     if(log.isTraceEnabled() && event.getRemoteEvent().getTTL() > 0) {
                         log.trace("Forwarding REGISTER event to next random peers");
                     }
                     log.trace(event);
                     return dispatchToRandomPeers(event.getRemoteEvent());
                 case ESGRemoteEvent.UNREGISTER:
                     if(log.isTraceEnabled() && event.getRemoteEvent().getTTL() > 0) {
                         log.trace("Forwarding UNREGISTER event to next random peers");
                     }
                     log.trace(event);
                     return dispatchToRandomPeers(event.getRemoteEvent());
                 default:
                     log.warn("UnHandled event type: ["+event.getRemoteEvent().getMessageType()+"] from "+event.getRemoteEvent().getSource());
                     log.trace(event);
                     break;
                 }
             }else if(event instanceof ESGJoinEvent) {
                 log.trace("Handling Queueed Join Event -->> delegating to handling method handlePeerJoinEvent()");
                 if(handled=this.handlePeerJoinEvent((ESGJoinEvent)event));
             }else if(event instanceof ESGCallableEvent) {
                 log.trace("ConnMgr: got Callable event: "+event);
                 ((ESGCallableEvent)event).doCall(this);
             }
         }
         event = null; //gc hint!
         return false;
     }
 
 
     //(for JOIN events that happens in the ESGDataNodeManager via it's superclass AbstractDataNodeManager)
     public void handleESGEvent(ESGEvent esgEvent) {
         //we only care about join events
         if(!(esgEvent instanceof ESGJoinEvent)) return;
         //should not be called but here for completeness...
         handlePeerJoinEvent((ESGJoinEvent)esgEvent);
     }
     
     private boolean handlePeerJoinEvent(ESGJoinEvent event) {
         //we only care bout ESGPeers joining
         if(!(event.getJoiner() instanceof ESGPeer)) return false;
 
         //manage the data structure for peer 'stubs' locally while
         //object is a participating managed component.
         if(event.hasJoined()) {
             log.trace("6)) Detected That A Peer Component Has Joined: "+event.getJoiner().getName());
             ESGPeer peer = (ESGPeer)event.getJoiner();
             String peerUrl = peer.getServiceURL();
             if(Utils.getMyServiceUrl().equals(peerUrl)) { log.warn("I may not be my own peer ;-)"); return true; }
             if(peerUrl != null) {
 
                 //Have the newly joined peer (stub) attempt to contact
                 //it's endpoint to establish notification.  By adding
                 //"this" connection manager, the peer stub can now
                 //send us an event if the notify call to the endpoint
                 //was successful or not.(see handlePeerEvent below)
                 peer.addPeerListener(this);
                 peers.put(peer.getName(),peer);
                 if (peer.getPeerType() == ESGPeer.DEFAULT_PEER) defaultPeer = peer;
 
             }else{
                 log.warn("Dropping "+peer+"... (no null service urls accepted)");
             }
             log.trace("Number of active service managed peers == "+peers.size());
         }else {
             log.trace("Detected That A Peer Component Has Left: "+event.getJoiner().getName());
             peers.remove(event.getJoiner().getName());
             unavailablePeers.remove(event.getJoiner().getName());
             log.trace("Number of active service managed peers = "+peers.size());
         }
         return false;
     }
 
     //--------------------------------------------
     //Special Event handling channel... 
     //(for peer events directly from managed peer stub objects)
     //--------------------------------------------
     public void handlePeerEvent(ESGPeerEvent evt) {
         log.trace("Got Peer Event: "+evt);
 
         //TODO: I know I know... use generics in the event!!! (todo)
         ESGPeer peer = (ESGPeer)evt.getSource();
         switch(evt.getEventType()) {
         case ESGPeerEvent.CONNECTION_BUSY:
             log.trace("Got ESGPeerEVent.CONNECTION_BUSY from: "+peer.getName());
         case ESGPeerEvent.CONNECTION_FAILED:
             log.trace("Got ESGPeerEVent.CONNECTION_FAILED from: "+peer.getName());
             if(peers.remove(peer.getName()) != null) {
                 log.trace("Transfering from active -to-> inactive list");
                 unavailablePeers.put(peer.getName(),peer);
             }else if(unavailablePeers.remove(peer.getName()) != null) {
                 log.trace("Transfering from inactive -to-> outta here! :-)");
                 peer.unregister();
             }
             break;
         case ESGPeerEvent.CONNECTION_AVAILABLE:
             log.trace("Got ESGPeerEVent.CONNECTION_AVAILABLE from: "+peer.getName());
             if(unavailablePeers.remove(peer.getName()) != null) {
                 log.trace("Transfering from inactive -to-> active list");
                 peers.put(peer.getName(),peer);
             }else {
                 log.trace("no status change for "+peer.getName());
             }
             break;
         default:
             break;
         }
         log.trace("Available Peers: ["+peers.size()+"] Unavailable: ["+unavailablePeers.size()+"]");    
     }
     
 }
