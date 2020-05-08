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
 package esg.node.core;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.impl.*;
 
 
 /**
    <pre>
    Description:
 
    Specific event to encapsulate the logic for the mechanics behind
    PRUNING.  We wish to have this event traverse the components such
    that it goes to the Connection Manager "CONN_MGR" and does node
    level (node-manager level) pruning of the stubs that are no longer
    viable i.e. pointing to dead or unresponsive node-manager
    endpoints.  Then this node jumps to the Registry and tells the
    registry to rebuild itself but instruct it to do so with port
    "poking" turned on such that the resulting registry does not
    reflect services that are no longer present.
 
    Ex:
    We detectd that search fails.
    1) If the node is not up at all (no node manager) then we release the stubs to that endpoint
       By doing this we also release the entry in the registry. OK.
    2) If the node is indeed up (node manager running) but the solr service sitting on the port
       is down, we need to keep the stub to that endpoint but we need to purge the registration
       from having this entry present.
 
    So this event is meant to follow the route CONN_MGR -> REGISTRY, hence why this
    event is a Routable event.
    </pre>
 
 */
 public class ESGFPruneEvent extends ESGCallableRoutableFutureEvent<Boolean> {
 
     private static final Log log = LogFactory.getLog(ESGCallableFutureEvent.class);
 
     public ESGFPruneEvent(Object source) { this(source,null,null); }
     public ESGFPruneEvent(Object source, String message) { this(source,null,message); }
     public ESGFPruneEvent(Object source, Object data, String message) {
         super(source, data, message);
         log.info("Instantiating Prune Event...");
         init();
     }
 
     public void init() {
         log.info("Initializing Prune Event...");
         associateCallable("CONN_MGR",new ESGCallableEvent(source, "Pruning Event for Peer Connection Stubs") {
                 public boolean call(DataNodeComponent contextComponent) {
                     try{
                         log.trace("inside \"call\"... CONN_MGR");
                         if(contextComponent.getName().equals("CONN_MGR")) {
                            log.info("calling \"prune\" on contextComponent CONN_MGR");
                             boolean ret = false;
                            setData( ret=((esg.node.connection.ESGConnectionManager)contextComponent).prune() );
                             log.info("value returned from prune: "+ret);
                             return ret;
                         }else{
                             log.warn("I am a callable event and found myself in an unexpected place!! : "+contextComponent.getName());
                         }
                     }finally {
                         log.info("Prune Callable Event's call method called and completed...(ConnectionManager)");
                     }
                     return false;
                 }
             });
 
         associateCallable("REGISTRY",new ESGCallableEvent(source, "Pruning Event for Registration") {
                 public boolean call(DataNodeComponent contextComponent) {
                     try{
                         log.trace("inside \"call\"... for REGISTRY");
                         if(contextComponent.getName().equals("REGISTRY")) {
                            log.info("calling \"prune\" on contextComponent REGISTRY");
                            boolean ret = (Boolean)getData(); //expected value from previous stage (CONN_MGR) above
                             ret &= ((esg.node.components.registry.ESGFRegistry)contextComponent).saveRegistration(true);
                             setData(ret);
                            log.info("value returned from pruning: "+ret);
                             return ret;
                         }else{
                             log.warn("I am a callable event and found myself in an unexpected place!! : "+contextComponent.getName());
                         }
                     }finally {
                         log.info("Prune Callable Event's call method called and completed... (REGISTRY)");
                     }
                     return false;
                 }
             });
 
         //This determines the route (order)
         setRoute("CONN_MGR","REGISTRY");
         log.info("Route: "+getRouteAsString()+" ("+getNumHops()+" ~ "+getAssociatedComponentNames().size()+")");
     }
 }
