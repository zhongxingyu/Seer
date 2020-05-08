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
 package esg.node.components.registry;
 
 import java.util.Properties;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Collections;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.TreeSet;
 import java.util.Comparator;
 import java.util.Date;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.impl.*;
 
 import esg.common.Utils;
 import esg.common.util.ESGFProperties;
 import esg.common.service.ESGRemoteEvent;
 import esg.node.connection.ESGConnectionManager;
 import esg.node.core.*;
 import esg.common.generated.registration.*;
 
 import static esg.node.components.registry.NodeTypes.*;
 
 /**
    Description:
 
    Core object for providing the Registry service.  The registration
    "form" (xml payload defined by the registration.xsd) that is passed
    among nodes describes the set of services available on a particular
    node.  This object exists in service of registration distribution
    and collection.
 
 */
 public class ESGFRegistry extends AbstractDataNodeComponent {
 
     public static String PROTOCOL_VERSION="v0.0.1";
 
     private static Log log = LogFactory.getLog(ESGFRegistry.class);
     private Properties props = null;
     private boolean isBusy = false;
     private RegistrationGleaner gleaner = null;
     private Map<String,String> processedMap = null;
     private Map<String,Long> removedMap = null;
     private NodeHostnameComparator nodecomp = null;
     private long lastDispatchTime = -1L;
     private PeerNetworkFilter peerFilter = null;
     private ExclusionListReader.ExclusionList exList = null;
 
     public ESGFRegistry(String name) {
         super(name);
         log.debug("Instantiating ESGFRegistry...");
     }
 
     public void init() {
         log.info("Initializing ESGFRegistry...");
         try{
             //props = getDataNodeManager().getMatchingProperties("*"); //TODO: figure the right regex for only what is needed
             props = new ESGFProperties();
             gleaner = new RegistrationGleaner(props);
             nodecomp = new NodeHostnameComparator();
             processedMap = new HashMap<String,String>();
             removedMap = new HashMap<String,Long>();
             peerFilter = new PeerNetworkFilter(props);
             if(ExclusionListReader.getInstance().loadExclusionList()) {
                 exList = ExclusionListReader.getInstance().getExclusionList().useType(PRIVATE_BIT);
             }
         }catch(java.io.IOException e) {
             System.out.println("Damn ESGFRegistry can't fire up... :-(");
             log.error(e);
         }
     }
 
     private void startRegistry() {
 
         //----------------------------------
         log.info("Loading and Initializing...");
         try{
             //gleaner.loadMyRegistration();
             gleaner.createMyRegistration().saveRegistration();
         }catch(ESGFRegistryException e) {
             log.warn(e.getMessage());
             gleaner.createMyRegistration();
         }
         Set<Node> loadedNodes = new TreeSet<Node>(nodecomp);
         loadedNodes.addAll(gleaner.getMyRegistration().getNode());
 
         enqueueESGEvent(new ESGEvent(this,
                                      new RegistryUpdateDigest(gleaner.toString(),
                                                               gleaner.getMyChecksum(),
                                                               loadedNodes),
                                      "Initializing..."));
         lastDispatchTime = (new Date()).getTime();
         //----------------------------------
 
         log.trace("Launching registry timer");
         long delay  = Long.parseLong(props.getProperty("registry.initialDelay","10"));
         final long period = Long.parseLong(props.getProperty("registry.period","300")); //every 5 mins
         log.debug("registry delay:  "+delay+" sec");
         log.debug("registry period: "+period+" sec");
 
         Timer timer = new Timer("Quiescence-Reg-Repost-Timer");
         timer.schedule(new TimerTask() {
                 public final void run() {
                     //If I have not dispatched any information to
                     //another peer in "period" seconds then touch the
                     //registry (give a new timestamp and thus a new
                     //checksum) and send out to share my view with
                     //others. The idea here is to only send out your
                     //state if you have been inactive for more than
                     //"period" time - anecdotal evidence that the
                     //network has reached quiescence.  This avoids the
                     //case where some node has already pushed their
                     //state after quiescense and as such starts the
                     //gossip dominoes, which gets here and you send
                     //out your state, but without this conditional
                     //here, then I would in turn send out my state
                     //after the blind elapsing of the period and the
                     //do the gossip cascade again... it makes for a
                     //noisier network.  So now nodes will deal with
                     //one cascade at a time-ish. ;-)
 
                     //Sidebar: There could potentially cause a race condition on
                     //lastDispatchTime since longs are not required to
                     //be dealt with in an atomic way by the VM, but it
                     //won't hurt a thing.
                     //-gavin
                     Date now = new Date();
                     long delta=(now.getTime() - lastDispatchTime);
                     if ( delta > (period*1000)) {
                         if(!ESGFRegistry.this.isBusy) {
                             ESGFRegistry.this.isBusy = true;
 
                             synchronized(gleaner) {
                                 //"touch" the registration.xml file (update timestamp via call to createMyRegistration, and resave)
                                 log.debug("re-posting registration...");
                                 
                                 gleaner.saveRegistration();
 
                                 enqueueESGEvent(new ESGEvent(this,
                                                              new RegistryUpdateDigest(gleaner.toString(),
                                                                                       gleaner.getMyChecksum(),
                                                                                       new HashSet<Node>()),
                                                              "Re-Posting Registration State"));
                                 lastDispatchTime = (new Date()).getTime();
                             }
                             ESGFRegistry.this.isBusy = false;
                         }
                     }else{
                         log.debug("Won't re-send state - too soon after last dispatch (quiescence period "+period+"secs, was not reached ["+(delta/1000)+"secs] elapsed)");
                     }
                 }
             },delay*1000,period*1000);
     }
 
     //(Indeed this algorithm is not the most parsimoneous on memory,
     //sort of.... thank goodness we are only talking about pointer
     //storage!!!)
 
     /*Notes on the merge algorithm:
 
       This algorithm is pretty much 'merge' from merge-sort.  The
       registrations each contain a list of nodes.  These lists are
       first storted by hostname via the nodecomp comparator.  Then
       they are merged.  When nodes are equal i.e. have the same
       hostname, a secondary test is done on time and the most recent
       time wins.  (pretty straight forward).  The additional wrinkle
       to this is as follows.
 
       If there is something in the "other" list that is not in "my"
       list then I don't just accept it, but I first check to see if it
       is listed in my "removed" list (a list of nodes that I know to
       have disappeared from the system because the connection manager
       signaled this to me in an un-join event that indicated that a
       node is AWOL and can't be accounted for anymore). So, if the
       other registration timestamp is newer than the timestamp of when
       the node was put in "removed" then the "other" node's
       registration is more current than mine and I will incorporate
       that new entry as well as remove it from my "removed" list.
       Further down the process *my* newly updated registration
       information is provided as an update to the connection manager
       wrapped in an event containing the "registry update digest"
       object.  The new updates are then taken by the connection
       manager and added to the peer list that he maintains.
 
       I am really tring to keep the task of updating the registry (the
       "passive" portion of the gossip) separate from the "active"/push
       portion of the gossip which, in this design, is done by the
       connection manager (soon to be renamed the peer
       manager... soon... one day... any day now...).  THIS object is
       all about maintaining the representation of the network.  This
       is the state that is being syncronized.  The connection manager
       should be all about interacting with peers and peforming pushes
       and getting status on connections and attendance.
 
       I am depending pretty heavily on wall clock time here.  I should
       probably implement vector clocks here to liberate me from time
       skew issues.  All should be good.  The hope is that nodes don't
       come up in such a way that there are any horrendous time/timing
       issues.
 
       -gavin
     */
     Set<Node> mergeNodes(Registration myRegistration, Registration otherRegistration) {
         log.trace("merging registrations...");
 
         List<Node> myList = myRegistration.getNode();
         List<Node> otherList = otherRegistration.getNode();
 
         try{
             log.trace("mylist = ["+myList+"] - size ("+myList.size()+") "+myList.get(0).getHostname());
             log.trace("otherList = ["+otherList+"] - size ("+otherList.size()+") "+otherList.get(0).getHostname());
         }catch(Throwable t) {
             log.error(t);
             log.trace("Malformed Registration: hostname field not set!!!!"); 
         }
         Long removedNodeTimeStamp = null;
         String removedNodeHostname = null;
 
         //Sort lists by hostname (natural) ascending order a -> z
         //where a compareTo z is < 0 iff a is before z
         //This algorithm is not in-place, uses terciary list.
         Collections.sort(myList,nodecomp);
         Collections.sort(otherList,nodecomp);
 
         Set<Node> newNodes = new TreeSet<Node>(nodecomp);
         Set<Node> updatedNodes = new HashSet<Node>();
 
         int i=0;
         int j=0;
         while ((i < myList.size()) && (j < otherList.size())) {
             try{
                 if( (nodecomp.compare(myList.get(i),otherList.get(j))) == 0 ) {
                     if((myList.get(i)).getTimeStamp() >= (otherList.get(j)).getTimeStamp()) {
                         newNodes.add(myList.get(i));
                         log.trace("-- Keeping local entry for (=) "+(myList.get(i)).getHostname());
                     }else {
                         if( peerFilter.isInNetwork(otherList.get(j)) && !exList.isExcluded((otherList.get(j)).getHostname()) ) {
                             newNodes.add(otherList.get(j));
                             updatedNodes.add(otherList.get(j));
                             log.trace("-- Updating with remote entry for (=) "+(myList.get(j)).getHostname());
                         }else{
                             log.trace("   Skipping, Not in our peer network (=) ["+(myList.get(j)).getHostname()+"]");
                             //just skip what's in the entry in the
                             //otherList but leave us at the same position
                             //in myList to do the next comparison
                             //(I could have also done j++; continue;)
                             i--;
                         }
                     }
                     i++;
                     j++;
                 }else if ( (nodecomp.compare(myList.get(i),otherList.get(j))) < 0 ) {
                     newNodes.add(myList.get(i));
                     log.trace("-  Keeping local entry for "+(myList.get(i)).getHostname());
                     i++;
                 }else{
                     if( (null == (removedNodeTimeStamp = removedMap.get(removedNodeHostname = otherList.get(j).getHostname()))) ||
                         (removedNodeTimeStamp < otherRegistration.getTimeStamp()) ) {
                         removedMap.remove(removedNodeHostname);
                         if( peerFilter.isInNetwork(otherList.get(j)) && !exList.isExcluded((otherList.get(j)).getHostname()) ) {
                             newNodes.add(otherList.get(j));
                             updatedNodes.add(otherList.get(j));
                             log.trace("-  Accepting new(er) remote entry for (+) "+(otherList.get(j)).getHostname());
                         }else {
                             log.trace("   Skipping "+(otherList.get(j)).getHostname()+", Not in our peer network (+)");
                         }
                     }else {
                         log.debug("   NOT accepting older candidate remote entry, ["+(otherList.get(j)).getHostname()+"], have more recent knowledge of removal by ["+(removedNodeTimeStamp > otherRegistration.getTimeStamp())+"]ms than candidate entry (+)");
                     }
                     j++;
                 }
             }catch(Throwable t) {
                 log.error(t);
                 log.warn("[=+] Skipping MALFORMED Node Entry...(i="+(i)+") (j="+(j)+")"); 
                 j++;
                 continue;
             }
         }
 
         while( i < myList.size() ) {
             newNodes.add(myList.get(i));
             log.trace("   Keeping local entry for "+myList.get(i).getHostname());
             i++;
         }
 
         while( j < otherList.size() ) {
             try{
                 if( (null == (removedNodeTimeStamp = removedMap.get(removedNodeHostname = otherList.get(j).getHostname()))) ||
                     (removedNodeTimeStamp < otherRegistration.getTimeStamp()) ) {
                     removedMap.remove(removedNodeHostname);
                     if( peerFilter.isInNetwork(otherList.get(j)) && !exList.isExcluded((otherList.get(j)).getHostname()) ) {
                         newNodes.add(otherList.get(j));
                         updatedNodes.add(otherList.get(j));
                         log.trace("   Adding new(er) remote entry for (++) "+(otherList.get(j)).getHostname());
                     }else {
                         log.trace("   Skipping "+(otherList.get(j)).getHostname()+", Not in our peer network (++)");
                     }
                 }else {
                     log.debug("   NOT accepting older candidate remote entry, ["+(otherList.get(j)).getHostname()+"], have more recent knowledge of removal by ["+(removedNodeTimeStamp > otherRegistration.getTimeStamp())+"]ms than candidate entry (++)");
                 }
             }catch(Throwable t) {
                 log.error(t);
                 log.warn("Skipping MALFORMED Node Entry... (j="+(j)+")"); 
                 j++;
                 continue;
             }
             j++;
         }
         
         log.trace("updatedNodes: ("+updatedNodes.size()+")");
         for(Node n : updatedNodes) {
             log.debug("updating registry with info on: "+n.getHostname());
         }
 
         myList.clear();
         myList.addAll(newNodes); //because using set they are
         newNodes.clear();
 
         return updatedNodes;
     }
 
     //Dispatch method for dealing with types of queued events...
     //The two we are concerned with are registering and unregistering peers
     public boolean handleESGQueuedEvent(ESGEvent event) {
         log.trace("delegating enqueued event ["+getName()+"]:["+this.getClass().getName()+"]: "+event);
         boolean handled = false;
 
         if(event.hasRemoteEvent()) {
             int eventType = event.getRemoteEvent().getMessageType();
             switch (eventType) {
             case ESGRemoteEvent.REGISTER:
                 //If this event is dispatched and yet still not
                 //handled then we push the unhandled event directly to
                 //the next state, in this case the ConnectionManager,
                 //letting it know the event passed through us by
                 //setting this object as the source.... and call it
                 //good
                 if(!(handled = this.handleRegistrationEvent(event))) {
                     event.setSource(this);
                     enqueueESGEvent(event);
                     handled = true;
                 }
                 break;
             case ESGRemoteEvent.UNREGISTER:
                 if(handled = this.handleUnRegistrationEvent(event)) {
                     event.setSource(this);
                     enqueueESGEvent(event);
                 }
                 break;
             default:
                 log.warn("Unknown Event Type: ["+eventType+"]... blindly forwarding to next state");
                 break;
             }
         }else if(event instanceof ESGCallableEvent) {
                 log.trace("Registry: got Callable event: "+event);
                 ((ESGCallableEvent)event).doCall(this);
                 handled=true;
         }
         if(handled) lastDispatchTime = (new Date()).getTime();
         return handled;
     }
     
     protected boolean handleUnRegistrationEvent(ESGEvent event) {
         log.trace("handling unregister enqueued event ["+getName()+"]:["+this.getClass().getName()+"]: Got An Unregister QueuedEvent!!!!: "+event);
         try{
             getDataNodeManager().removePeer(event.getRemoteEvent().getOrigin());
         }catch (Throwable t) {
             log.error(t);
             t.printStackTrace();
             return false;
         }
         return true;
     }
 
     //When peer registration messages are encountered grab those
     //events and collect the peer's registration information and
     //incorporate it into our own world view.
     protected boolean handleRegistrationEvent(ESGEvent event) {
         log.trace("handling register enqueued event ["+getName()+"]:["+this.getClass().getName()+"]: Got A Register QueuedEvent!!!!: "+event);
 
         synchronized(gleaner) {
 
             String payloadChecksum  = event.getRemoteEvent().getPayloadChecksum();
             String sourceServiceURL = event.getRemoteEvent().getSource();
 
             //TODO: Heck no, I should NOT be using string comparison for
             //this...  I need to revisit the typing of the remote event
             //for type of the checksum.  The thing is I don't want to use
             //BigInteger because I don't know how portable that is and I
             //want the event object as type simple as can be.  Right now
             //using the string representation of the checksum... maybe
             //that's good enough for the type complexity trade off?
 
             String lastChecksum = processedMap.get(sourceServiceURL);
             if( (lastChecksum != null) && (lastChecksum.equals(payloadChecksum)) ) {
                 log.trace("I have seen this payload before, from the same dude... there is nothing new to learn... ["+event+"]");
                 //punt... (see dispatcher above)
                 return false;
             }
 
             //Pull out our registration information and parse the xml string
             //payload from the incoming event into object form, via the gleaner.
             Registration myRegistration = gleaner.getMyRegistration();
             Registration peerRegistration = gleaner.createRegistrationFromString((String)event.getRemoteEvent().getPayload());
             
             //log.trace("myRegistration = ["+myRegistration+"]");
             //log.trace("peerRegistration = ["+peerRegistration+"]");
 
             //Don't even consider registrations that are not within version range!
             Set<Node> updatedNodes = null;
             try {
                 if(Utils.versionCompare(peerRegistration.getVersion(), ESGFRegistry.PROTOCOL_VERSION) >= 0) {
                     updatedNodes = mergeNodes(myRegistration,peerRegistration);
                 }else{
                     log.warn("Peer node registration has unsupported version: ["+myRegistration.getVersion()+"] (not merging)");
                 }
             }catch(esg.common.InvalidVersionStringException e) {
                 log.error("Peer node registration has unsupported version*: ["+myRegistration.getVersion()+"] (not merging)",e);
             }catch(NullPointerException e) {
                 log.warn("Peer node apparently does not even have a version field! (not merging)");
                log.error(e);
             }
 
             log.debug("Recording this interaction with "+sourceServiceURL+" - "+payloadChecksum);
             processedMap.put(sourceServiceURL, payloadChecksum);
 
             if(updatedNodes == null || updatedNodes.isEmpty()) {
                 log.debug("No New Information Learned :-(");
                 return false;
             }
 
             //--------------------------------------------------------------
             //There has been updates made to the registry generate the R.U.D.
             //and send it to the next state (the connection manager)
             //--------------------------------------------------------------
             gleaner.touch(); //timestamp our updated registry...
             gleaner.saveRegistration(); //write the new registry to file... (registration.xml)
             sendOutNewRegistryState(gleaner,updatedNodes); //send off registry state to peer network...
             //--------------------------------------------------------------
 
         }
 
         return true;
     }
 
     //When nodes are removed from the registry there is a new state.
     //This method takes the new (reduced) state of the registry and
     //pushes it out - eventually - to the rest of the peer network.
     private void sendOutNewRegistryState(RegistrationGleaner gleaner) {
         this.sendOutNewRegistryState(gleaner,null);
     }
 
     //Conjure a brand new event (as we are now the source for a new state that is to be propagated).
     //send that event on to the next step - that will propagate this new state ([ending with] connection manager)
     private synchronized void sendOutNewRegistryState(RegistrationGleaner gleaner, Set<Node> updatedNodes) {
         log.trace("Sending off new event with registry update digest data");
         ESGEvent rudEvent = new ESGEvent(this,
                                          new RegistryUpdateDigest(gleaner.toString(),
                                                                   gleaner.getMyChecksum(),
                                                                   updatedNodes),
                                          "Updated / Merged Registration State");
         enqueueESGEvent(rudEvent);
     }
 
     //Listen out for Joins from conn mgr
     public void handleESGEvent(ESGEvent esgEvent) {
         //we only care about join events... err... sort of :-)
 
         //Note: I should not be so myopic in dealing with system
         //events. There may be others that need to be acted upon that
         //I am now ignoring but I am in Brody mode now -gavin
         if((esgEvent instanceof ESGSystemEvent) &&
            (((ESGSystemEvent)esgEvent).getEventType() == ESGSystemEvent.ALL_LOADED) ) {
             //log.trace("I must have missed you in the load sequence CONN_MGR... I got you now");
             //addESGQueueListener(getDataNodeManager().getComponent("CONN_MGR"));
             startRegistry();
         }
 
         if(!(esgEvent instanceof ESGJoinEvent)) return;
         //we only care bout peer joining beyond this point...
 
         ESGJoinEvent event = (ESGJoinEvent)esgEvent;
 
         if(event.getJoiner() instanceof ESGPeer) {
             String peerUrl = null;
             String peerHostname = Utils.asHostname(peerUrl = event.getJoiner().getName());
             if(event.hasLeft()) {
                 log.debug("Detected That A Peer Node Has Left: "+event.getJoiner().getName());
                 synchronized(gleaner) {
                     if(gleaner.removeNode(peerHostname)) {
                         processedMap.remove(peerUrl);
                         removedMap.put(peerHostname,event.getTimeStamp());
                         gleaner.saveRegistration(true); //NOTE: When a peer goes away do full check when constructing registration
                         sendOutNewRegistryState(gleaner);
                     }
                 }
             }else if(event.hasJoined()) {
                 removedMap.remove(peerHostname);
             }
         }
 
         if(event.getJoiner() instanceof ESGConnectionManager) {
             if(event.hasJoined()) {
                 log.trace("Detected That The ESGConnectionManager Has Joined: "+event.getJoiner().getName());
                 addESGQueueListener(event.getJoiner());
             }else {
                 log.trace("Detected That The ESGConnectionManager Has Left: "+event.getJoiner().getName());
                 removeESGQueueListener(event.getJoiner());
             }
         }
 
     }
 
     public boolean saveRegistration() { return this.saveRegistration(false); }
     public boolean saveRegistration(boolean doCheck) {
         log.trace("calling saveRegistration from ESGFRegistry (doCheck = "+doCheck+")");
         return gleaner.saveRegistration(doCheck);
     }
 
     //------------------------------------------------------------
     //Utility inner class... comparator for hostnames
     //------------------------------------------------------------
     private class NodeHostnameComparator implements Comparator<Node> {
         public int compare(Node a, Node b) {
             return a.getHostname().compareTo(b.getHostname());
         }
     }
 
 }
