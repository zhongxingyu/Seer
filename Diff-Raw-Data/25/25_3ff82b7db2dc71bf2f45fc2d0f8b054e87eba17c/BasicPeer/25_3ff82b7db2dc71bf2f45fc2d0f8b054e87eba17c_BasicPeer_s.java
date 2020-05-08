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
 
    This is essentially the wrapped stubs to the peer(s).
    This (node) side of the rpc call where calls
    ORIGINATE. (from ESGPeer -to-> ESGPeer's Node Manger Service "node").
 
    ----------------------------------------------------
    THIS CLASS REPRESNTS *EGRESS* CALLS TO THE ESGPeer!!
    ----------------------------------------------------
    (I don't think I can make this any clearer)
 
 **/
 package esg.node.core;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.impl.*;
 
 import java.net.MalformedURLException;
 import java.net.InetAddress;
 import java.util.List;
 import java.util.ArrayList;
 
 import esg.node.core.ESGPeerListener;
 import esg.node.service.ESGDataNodeService;
 import esg.common.service.ESGRemoteEvent;
 import esg.common.Utils;
 
 public class BasicPeer extends HessianPeer {
 
     private static final Log log = LogFactory.getLog(BasicPeer.class);
     private ESGDataNodeService datanodeServiceStub = null;
     private List<ESGPeerListener> peerEventListeners = null;
     
     private boolean pingState = false;
 
     public BasicPeer(String serviceURL, int type) throws java.net.MalformedURLException { 
         super(serviceURL,type); 
         peerEventListeners = new ArrayList<ESGPeerListener>();
     }
     
     //From the super-dooper-class the default type is set to "ESGPeer.PEER" ;-)
     public BasicPeer(String serviceURL) throws java.net.MalformedURLException { 
         super(serviceURL); 
         peerEventListeners = new ArrayList<ESGPeerListener>();
     }
     
     /**
        The idea here is to establish the proper proxy object to the
        rpc endpoint. This class is an rpc stub wrapper. "init()" may
        be called multiple times until valid i.e. until an endpoint can
        be created with the given serviceURL. "isValid()" indicates
        that an rpc stub was able to be created referencing the
        endpoint at the serviceURL, not whether or not that endpoint is
        available or even present for communication.
     */
     public void init() {
         log.trace("4)) Peer init attempting to create handle to endpoint: ["+getName()+"]");
         if(isValid) {
             log.info("I am already valid... :-)");
             return;
         }
 
         //I am not a valid object if I don't have a name
         if(getName() == null || (getName().equals(DataNodeComponent.ANONYMOUS)) ) {
             log.warn("Cannot initialize Peer DataNode with name/serviceURL ["+getName()+"]");
             isValid = false;
             return;
         }
 
         try {
             //The call to our superclass to create the proper stub object to remote service.
             datanodeServiceStub = (ESGDataNodeService)factoryCreate(ESGDataNodeService.class, getServiceURL());
             log.trace("Peer DataNode Service handle is ["+datanodeServiceStub+"]");
         }catch(Exception ex) {
             log.warn("Could not connect to peer @ serviceURL ["+getServiceURL()+"]",ex);
             isAvailable = false;
         }
     
         if(datanodeServiceStub != null) {
             log.trace(getName()+" Peer Proxy properly initialized");
             isValid = true;
         }else {
             log.warn(getName()+" Proxy NOT properly initialized");
             isValid = false;
         }
     }
 
     //The way to talk back to the connection manager, pretty much who this listener at the very least is
     public void addPeerListener(ESGPeerListener listener) {
         if(peerEventListeners.contains(listener)) return;
         peerEventListeners.add(listener);
         log.trace("Added Peer Listener: "+listener);
     }
     
     
     //-----------------------------------------------
     //Delgated remote methods...
     //-----------------------------------------------
 
     //The simplest of communications to make sure folks are alive and
     //that the RPC works at a basic level... If and only if I can ping
     //you and you respond (return true) that I can consider you a
     //VALID end point, and therefore, my proxy ;-), *I* am valid to
     //use for communicating to you, mr. peer. gyot it? :-) 
 
     //A return true of false indicate that you are able to be
     //communicated with, however a return of false is you responding
     //that you are not open to being talked to (busy etc.) and thus
     //this object, by proxy, am not going to perform calls on your
     //behalf, and therefore not valid.
 
     //This semantically expected to be the START of the communication
     //between data node and the peer this object represents.  (called by the bootstrapping
     //service: ESGDataNodeService)
     public boolean ping() { return this.ping(false); }
     public boolean ping(boolean force) {
         log.trace("ping -->> ["+getName()+"] (force = "+force+")");
         boolean response = false;
         try {
             //TODO see about changing the timeout so don't have to wait forever to fail!        
             response = datanodeServiceStub.ping();
             pingState = (response  && isValid);
             log.trace( (response ? "[OK]" : "[BUSY]") );
         }catch (RuntimeException ex) {
             log.info("Could not call \"ping\" on ["+getServiceURL()+"] "+ex.getMessage());
             log.trace(ex);
             isAvailable=false; //I know now necessary but doesn't hurt - communicates more clearly meaning of isAvailable IMHO
             fireConnectionFailed(ex);
         }
    
         //----
         //NOTE: Here I am basically saying that IF, because of a ping,
         //there is a change in the availability state of the peer in
         //question then only upon a CHANGE in state will there be
         //events fired through the rest of the system and the new
         //state recorded and dispatched to the rest of the system.
         //This saves us from sending out events that doesn't contain
         //any *new* information.  This can be forced to fire an event
         //under all conditions if "force" is set to true.
         //----
 
         log.trace("(ia)["+isAvailable+"] -> (ps)["+pingState+"]");
         if((isAvailable != pingState) || force ) {
             log.trace("Peer's state/availability changed... from ["+isAvailable+"] -> ["+pingState+"] (force = "+force+")");
             if(pingState) fireConnectionAvailable(); else fireConnectionBusy();
         }
        isAvailable = pingState; //I know now necessary but doesn't hurt - communicates more clearly meaning of isAvailable IMHO
 
         log.trace("isValid = "+isValid);
         log.trace("response = "+response);
         log.trace("isAvailable = "+isAvailable);
         return isAvailable;
         //FYI: the isAvailable() method is defined in super-superclass)
     }
         
     public void handleESGRemoteEvent(ESGRemoteEvent evt) {
         try {
             log.trace("Making Remote Call to "+getServiceURL()+"'s remote \"handleESGRemoteEvent\" method, sending: "+evt);
             datanodeServiceStub.handleESGRemoteEvent(evt);
         }catch (RuntimeException ex) {
             log.error("Could not make call \"handleESGRemoteEvent\" on ["+getServiceURL()+"] "+ex.getMessage());
             isAvailable=false; //I know now necessary but doesn't hurt - communicates more clearly meaning of isAvailable IMHO
             fireConnectionFailed(ex);
         }
     }
 
     protected void fireConnectionAvailable() {
         log.trace("Firing Connection Available to "+getServiceURL());
         isAvailable=true;
         fireESGPeerEvent(new ESGPeerEvent(this,ESGPeerEvent.CONNECTION_AVAILABLE));
     }
     protected void fireConnectionFailed(Throwable t) {
         log.trace("Firing Connection Failed to "+getServiceURL());
         isAvailable=false;
         fireESGPeerEvent(new ESGPeerEvent(this,t.getMessage(),ESGPeerEvent.CONNECTION_FAILED));
     }
     protected void fireConnectionBusy() {
         log.trace("Firing Connection Busy to "+getServiceURL());
         isAvailable=false;
         fireESGPeerEvent(new ESGPeerEvent(this,ESGPeerEvent.CONNECTION_BUSY));
     }
     //--------------------------------------------
     //Event dispatching to all registered ESGPeerListeners
     //calling their handlePeerEvent method
     //--------------------------------------------
     protected void fireESGPeerEvent(ESGPeerEvent esgEvent) {
         log.trace("Firing Event: "+esgEvent);
         for(ESGPeerListener listener: peerEventListeners) {
             listener.handlePeerEvent(esgEvent);
         }
     }
 
 
     
 }
