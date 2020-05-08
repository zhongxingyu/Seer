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
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.impl.*;
 
 import esg.common.Utils;
 import esg.common.util.ESGFProperties;
 import esg.common.service.ESGRemoteEvent;
 import esg.node.connection.ESGConnectionManager;
 import esg.node.core.*;
 import esg.common.generated.registration.*;
 
 /**
    Description:
    
    Core object for providing the Registry service.  The registration
    "form" (xml payload defined by the registration.xsd) that is passed
    among nodes describes the set of services available on a particular
    node.  This object exists in service of registration distribution
    and collection.
    
 */
 public class ESGFRegistry extends AbstractDataNodeComponent {
     
     private static Log log = LogFactory.getLog(ESGFRegistry.class);
     private Properties props = null;
     private boolean isBusy = false;
     private RegistrationGleaner gleaner = null;
     private Map<String,String> processedMap = null;
     private Map<String,Long> removedMap = null;
     private NodeHostnameComparator nodecomp = null;
 
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
             processedMap = new HashMap<String,String>(); //TODO: may want to persist this and then bring it back.
             removedMap = new HashMap<String,Long>();
             startRegistry();
         }catch(java.io.IOException e) {
             System.out.println("Damn ESGFRegistry can't fire up... :-(");
             log.error(e);
         }
     }
 
     private void startRegistry() {
         log.trace("launching registry timer");
         long delay  = Long.parseLong(props.getProperty("registry.initialDelay","0"));
         long period = Long.parseLong(props.getProperty("registry.period","86405")); //Daily
         log.trace("registry delay:  "+delay+" sec");
         log.trace("registry period: "+period+" sec");
 	
         Timer timer = new Timer();
         timer.schedule(new TimerTask() {
                 public final void run() {
                     //log.trace("Checking for any new node information... [busy? "+ESGFRegistry.this.isBusy+"]");
                     if(!ESGFRegistry.this.isBusy) {
                         ESGFRegistry.this.isBusy = true;
                         gleaner.createMyRegistration().saveRegistration();
                         ESGFRegistry.this.isBusy = false;
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
     private Set<Node> mergeNodes(Registration myRegistration, Registration otherRegistration) {
         log.trace("merging registrations...");
         
         List<Node> myList = myRegistration.getNode();
         List<Node> otherList = otherRegistration.getNode();
 
         log.trace("mylist = ["+myList+"] - size ("+myList.size()+") "+myList.get(0).getHostname());
         log.trace("otherList = ["+otherList+"] - size ("+otherList.size()+") "+otherList.get(0).getHostname());
 
         Long removedNodeTimeStamp = null;
         String removedNodeHostname = null;
 
         //Sort lists by hostname (natural) ascending order a -> z
         //where a compareTo z is < 0 iff a is before z
         //This algorithm is not in-place, uses terciary list.
         Collections.sort(myList,nodecomp);
         Collections.sort(otherList,nodecomp);
 
         log.trace("Collections sorted...");
 
         Set<Node> newNodes = new TreeSet<Node>(nodecomp);
         Set<Node> updatedNodes = new HashSet<Node>();
         
         int i=0;
         int j=0;
         while ((i < myList.size()) && (j < otherList.size())) {
             log.trace("In while loop");
             if( (nodecomp.compare(myList.get(i),otherList.get(j))) == 0 ) {
                 if((myList.get(i)).getTimeStamp() > (otherList.get(j)).getTimeStamp()) {
                     newNodes.add(myList.get(i));
                 }else {
                     newNodes.add(otherList.get(j));
                     updatedNodes.add(otherList.get(j));
                 }
                 i++;
                 j++;
             }else if ( (nodecomp.compare(myList.get(i),otherList.get(j))) < 0 ) {
                 newNodes.add(myList.get(i));
                 i++;
             }else{
                 if( (null == (removedNodeTimeStamp = removedMap.get(removedNodeHostname = otherList.get(j).getHostname()))) ||
                     (removedNodeTimeStamp < otherRegistration.getTimeStamp()) ) {
                     removedMap.remove(removedNodeHostname);
                     newNodes.add(otherList.get(j));
                     updatedNodes.add(otherList.get(j));
                 }
                 j++;
             }
         }
 
         log.trace("Now let's copy over what's left over...");
         log.trace("myList.size() = "+myList.size());
         log.trace("newNodes.size() = "+newNodes.size());
         log.trace("index i = "+i);
         log.trace("myList's ith object = "+myList.get(i));
         log.trace("myList's ith value  = "+myList.get(i).getHostname());
         while( i < myList.size() ) {
             newNodes.add(myList.get(i));
         }
         log.trace("I am stuck here");
         while( j < otherList.size() ) {
             if( (null == (removedNodeTimeStamp = removedMap.get(removedNodeHostname = otherList.get(j).getHostname()))) ||
                 (removedNodeTimeStamp < otherRegistration.getTimeStamp()) ) {
                 removedMap.remove(removedNodeHostname);
                 newNodes.add(otherList.get(j));
                 updatedNodes.add(otherList.get(j));
             }
         }
 
         log.trace("updatedNodes: ("+updatedNodes.size()+")");
         for(Node n : updatedNodes) {
             log.trace("updated node - "+n.getHostname());
         }
         
         myList.clear();
         myList.addAll(newNodes); //because using set they are 
         newNodes.clear();
         
         return updatedNodes;
     }
 
     //When peer registration messages are encountered grab those
     //events and collect the peer's registration information and
     //incorporate it into our own world view.
     public boolean handleESGQueuedEvent(ESGEvent event) {
         log.trace("handling enqueued event ["+getName()+"]:["+this.getClass().getName()+"]: Got A QueuedEvent!!!!: "+event);
         
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
             log.trace("I have seen this payload before, from the same dude... there is nothing new to learn... dropping event on floor ["+event+"]");
         }
         
         //zoiks: use gleaner to get local registration object to modify.
         //       periodically write modified structure to file (getting a new checksum)
         //       back the list of nodes with a datastructure that can ...
         //       have another datastructure with checksum and source service url
         //       to see if we should even do anything at all.
         
         //Pull out our registration information (parse the xml string
         //payload into object form via the gleaner) and send the event
         //on its way (the connection manager should be downstream this
         //event path
 
         Registration myRegistration = gleaner.getMyRegistration();
         Registration peerRegistration = gleaner.createRegistrationFromString((String)event.getRemoteEvent().getPayload());
 
         log.trace("myRegistration = ["+myRegistration+"]");
         log.trace("peerRegistration = ["+peerRegistration+"]");
         
         Set<Node> updatedNodes = mergeNodes(myRegistration,peerRegistration);
 
         log.trace("Nodes merged");
         
         gleaner.saveRegistration();
 
         log.info("Recording this interaction with "+sourceServiceURL+" - "+payloadChecksum);
         processedMap.put(sourceServiceURL, payloadChecksum);
 
         log.trace("Sending off event with registry update digest data");
         enqueueESGEvent(new ESGEvent(this,
                                      new RegistryUpdateDigest(gleaner.toString(), 
                                                               gleaner.getMyChecksum(),
                                                               updatedNodes),
                                      "Updated / Merged Registration State"));
         }
 
         return true;
     }
     
     //Listen out for Joins from conn mgr  
     public void handleESGEvent(ESGEvent esgEvent) {
         //we only care about join events... err... sort of :-)
         
         //Note: I should not be so myopic in dealing with system
         //events. There may be others that need to be acted upon that
         //I am now ignoring but I am in Brody mode now -gavin
         if((esgEvent instanceof ESGSystemEvent) && 
            (((ESGSystemEvent)esgEvent).getEventType() == ESGSystemEvent.ALL_LOADED) ) {
             log.trace("I must have missed you in the load sequence CONN_MGR... I got you now");
             addESGQueueListener(getDataNodeManager().getComponent("CONN_MGR"));
         }
         
         if(!(esgEvent instanceof ESGJoinEvent)) return;
         //we only care bout peer joining beyond this point...
 
         ESGJoinEvent event = (ESGJoinEvent)esgEvent;
 
         if(event.getJoiner() instanceof ESGPeer) {
             if(event.hasLeft()) {
                 log.trace("Detected That A Peer Node Has Left: "+event.getJoiner().getName());
                 String peerHostname = null;
                 String peerUrl = null;
                 synchronized(gleaner) {
                     if(gleaner.removeNode( peerHostname = Utils.asHostname(peerUrl = event.getJoiner().getName()))) {
                         processedMap.remove(peerUrl);
                         removedMap.put(peerHostname,event.getTimeStamp());
                     }
                 }
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
 
     //------------------------------------------------------------
     //Utility inner class... comparator for hostnames
     //------------------------------------------------------------
     private class NodeHostnameComparator implements Comparator<Node> {
         public int compare(Node a, Node b) {
             return a.getHostname().compareTo(b.getHostname());
         }
     }
     
 }
