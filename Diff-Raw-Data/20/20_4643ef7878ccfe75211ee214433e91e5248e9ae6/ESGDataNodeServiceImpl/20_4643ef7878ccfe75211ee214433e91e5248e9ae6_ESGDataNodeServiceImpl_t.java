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
 
    ----------------------------------------------------
    THIS CLASS RECEIVES ALL *INGRESS* CALLS *FROM* PEERS(s)!!
    ----------------------------------------------------
    (I don't think I can make this any clearer)
 
 
 **/
 package esg.node.service;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.impl.*;
 
 import esg.node.core.ESGDataNodeManager;
 import esg.node.core.AbstractDataNodeComponent;
 import esg.node.core.ESGEvent;
 import esg.node.core.ESGJoinEvent;
 import esg.node.core.ESGPeer;
 import esg.node.core.BasicPeer;
 import esg.node.core.ESGSystemEvent;
 import esg.node.connection.ESGConnectionManager;
 import esg.common.service.ESGRemoteEvent;
 import esg.common.Utils;
 
 public class ESGDataNodeServiceImpl extends AbstractDataNodeComponent 
     implements ESGDataNodeService {
 
     private static final Log log = LogFactory.getLog(ESGDataNodeServiceImpl.class);
     private ESGDataNodeManager datanodeMgr = null;
     private ESGConnectionManager connMgr = null;
     private String myServiceUrl = null;
     
 
     public ESGDataNodeServiceImpl() {
         log.info("ESGDataNodeServiceImpl instantiated...");
         setMyName("DNODE_SVC");
         myServiceUrl = Utils.getMyServiceUrl();
         boot();
     }
 
     //******************************
     //Bootstrap the entire system...
     //******************************
     public void boot() {
         log.trace("Bootstrapping System...");
         datanodeMgr = new ESGDataNodeManager();
         datanodeMgr.registerComponent(this);
         datanodeMgr.init();
     }
     //******************************
     
     public void init() { log.trace("no-op initialization"); }
 
     //------------------------------------------------------------
     //We will consider this object not valid if there are no peers
     //to communicate with. That would be because:
     //1) There are no peer proxy objects available for us to use
     //2) If the peer proxy objects we DO have are no longer valid
     //(we just need one to be valid for us to be available) 
 
     //NOTE: review this policy! *for now* good enough as we are only
     //planning on having a 1:1 among peer nodes... but we could
     //imagine having just one node that is valid holding open the
     //door for us to be DOS-ed by folks maliciously sending us huge
     //events that flood our system.
     private boolean amAvailable() { 
         boolean ret = false;
         ret = (connMgr == null) ? false : connMgr.amAvailable(); 
         log.trace("amAvailable() -> "+ret);
         return ret;
     }
     
     public void unregister() {
         //Intentionally a NO-OP
         log.warn("Balking... What does it mean to unregister the service itself?... exactly... no can do ;-)");
     }
     //------------------------------------------------------------
 
 
     
     //------------------------------------------------------------
     //Remote service interface implementation ping & handleESGRemoteEvent
     //------------------------------------------------------------
     //Ingress calls to check RPC working method...
     public boolean ping() { 
         log.trace("DataNode service got \"ping\"");
         return amAvailable();
     }
 
     //Ingress event handling from remote 'client'
     public void handleESGRemoteEvent(ESGRemoteEvent evt_) {
        if(!evt_.checkTTL()) {
            log.warn("Received event has invalid TTL: ["+evt_.getTTL()+"] from "+evt_.getSource()+" dropping on floor...");
            return;
        }else {
            evt_.decTTL();
        }
         
         //NOTE: This would potentially get called a lot!
         //      May want to look at maybe a faster but equiv comparison
         if(myServiceUrl.equalsIgnoreCase(evt_.getSource())) {
             log.warn("I seem to be getting an incoming message from myself ["+myServiceUrl+"]: Dropping potential spoof");
             return;
         }
         
         log.trace("DataNode service got \"handleESGRemoteEvent\" call with event: ["+evt_+"]");
         if(!amAvailable()) {
             log.warn("Dropping ingress notification event on the floor, I am NOT available. ["+evt_+"]");
         }
     
         //Being a nice guy and rerouting you to right method
         //I may be being too nice... consider taking this out if abused.
         ESGEvent evt = null;
         if(evt_.getMessageType() == ESGRemoteEvent.NOOP) { 
             log.trace("GOT NOOP REMOTE EVENT"); 
         }else if(evt_.getMessageType() == ESGRemoteEvent.REGISTER) {
             log.trace("GOT REGISTER REMOTE EVENT");
             if(evt_.getPayload() == null) {
                 log.warn("Violation: Dropping null payload on floor (payload required)");
                 return;
             }
             log.debug("["+(new java.util.Date())+"] Receiving Register Event from: "+evt_.getSource()); 
             evt = new ESGEvent(this);
             evt.setRemoteEvent(evt_);
             enqueueESGEvent("REGISTRY",evt);
         }else if(evt_.getMessageType() == ESGRemoteEvent.UNREGISTER) { 
             log.trace("GOT UNREGISTER REMOTE EVENT"); 
             evt = new ESGEvent(this);
             evt.setRemoteEvent(evt_);
             enqueueESGEvent("REGISTRY",evt);
         }else if(evt_.getMessageType() == ESGRemoteEvent.HEALTH) { 
             log.trace("GOT HEALTH REMOTE EVENT"); 
             evt = new ESGEvent(this);
             evt.setRemoteEvent(evt_);
             enqueueESGEvent("MONITOR",evt);
         }else if(evt_.getMessageType() == ESGRemoteEvent.METRICS) { 
             log.trace("GOT METRICS REMOTE EVENT");
             evt = new ESGEvent(this);
             evt.setRemoteEvent(evt_);
             enqueueESGEvent("METRICS",evt);
         }else if(evt_.getMessageType() == ESGRemoteEvent.APPLICATION) { 
             log.trace("GOT APPLICATION REMOTE EVENT");
         }else {
             log.trace("DO NOT RECOGNIZE THIS MESSAGE TYPE: "+evt_);
         }
     
 
         //TODO: Grab this remote event, inspect the payload and stuff
         //it into an esg event and fire it off to listeners to handle.
         //Example... if the payload was the Catalog xml then JAXB it
         //and send the object form as the payload for others to use
         //(for example)
     }
 
 
     //--------------------------------------------
     //Internal 'Control' Event handling... (for join events) This service wants handle to connection manager
     //--------------------------------------------
     public void handleESGEvent(ESGEvent esgEvent) {
         //we only care about join events... err... sort of :-)
 
         if((esgEvent instanceof ESGSystemEvent) && 
            (((ESGSystemEvent)esgEvent).getEventType() == ESGSystemEvent.ALL_LOADED) &&
            (connMgr == null) ) {
             log.trace("I must have missed you in the load sequence CONN_MGR... got you now");
             connMgr = (ESGConnectionManager)getDataNodeManager().getComponent("CONN_MGR");
         }
 
         if(!(esgEvent instanceof ESGJoinEvent)) return;
 
         ESGJoinEvent event = (ESGJoinEvent)esgEvent;
     
         //we only care bout peer joining
         if(!(event.getJoiner() instanceof ESGConnectionManager)) return;
 
         if(event.hasJoined()) {
             log.trace("Detected That The ESGConnectionManager Has Joined: "+event.getJoiner().getName());
             connMgr = (ESGConnectionManager)event.getJoiner();
             addESGQueueListener(connMgr);
         }else {
             log.trace("Detected That The ESGConnectionManager Has Left: "+event.getJoiner().getName());
             removeESGQueueListener(connMgr);
             connMgr = null;     
         }
         log.trace("connMgr = "+connMgr);
     }
 
 
 
 }
