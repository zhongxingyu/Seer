 /***************************************************************************
 *                                                                          *
 *  Organization: Lawrence Livermore National Lab (LLNL)                    *
 *   Directorate: Computation                                               *
 *    Department: Computing Applications and Research                       *
 *      Division: S&T Global Security                                       *
 *        Matrix: Atmospheric, Earth and Energy Division                    *
 *       Program: PCMDI                                                     *
 *       Project: Earth Systems Grid (ESG) Data Node Software Stack         *
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
 *   Earth System Grid (ESG) Data Node Software Stack, Version 1.0          *
 *                                                                          *
 *   For details, see http://esg-repo.llnl.gov/esg-node/                    *
 *   Please also read this link                                             *
 *    http://esg-repo.llnl.gov/LICENSE                                      *
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
 
 **/
 package esg.node.core;
 
 import esg.node.connection.ESGConnectionManager;
 import esg.node.components.notification.ESGNotifier;
import esg.node.components.monitoring.ESGMonitor;;
import esg.node.components.metrics.ESGMetrics;;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.impl.*;
 
 public class ESGDataNodeManager extends AbstractDataNodeManager {
 
     private static Log log = LogFactory.getLog(ESGDataNodeManager.class);
     private ESGConnectionManager connMgr = null;
 
     //TODO: the logic of the system, pulling in the components from a
     //config file and managing them.
     
     public ESGDataNodeManager() {
 	log.info("Instantiating ESGDataNodeManager...");
     }
 
     private void initCoreServices() {
 	log.info("Loading core components");
 
 	connMgr = new ESGConnectionManager("CONN_MGR");
 	registerComponent(connMgr);
 	
 	try{
 	    //for gavin's local testing...
 	    //Gateway gateway = new BasicGateway("https://128.115.184.6/esg-node/gateway",Gateway.DEFAULT_GATEWAY);
 	    String gatewaySvcRoot = System.getenv().get("ESG_GATEWAY_SVC_ROOT");
 	    if(null == gatewaySvcRoot) log.error("The Default Gateway is: ["+gatewaySvcRoot+"]: Hint - set ESG_GATEWAY_SVC_ROOT in /etc/esg.env");
 	    Gateway gateway = new BasicGateway("http://"+gatewaySvcRoot+"/gateway", Gateway.DEFAULT_GATEWAY);
 	    registerGateway(gateway);
 	}catch(java.net.MalformedURLException e) {log.error(e); }
 
 	ESGNotifier notifier = new ESGNotifier("NOTIFIER");
 	registerComponent(notifier);
 
 	ESGMonitor monitor = new ESGMonitor("MONITOR");
 	registerComponent(monitor);
 	
 	ESGMetrics metrics = new ESGMetrics("METRICS");
 	registerComponent(metrics);
 	
 	log.info("Connecting core components");
 
 	//Now build the "FSM" connections.
 	//Ingress messages from the network come in through the DNODE_SVC
 	//The DNODE_SVC then routes the message to the appropriate 
 	
 	connect("DNODE_SVC","NOTIFER");
 	connect("DNODE_SVC","MONITOR");
 	connect("DNODE_SVC","METRICS");
 
 	connect("NOTIFIER","CONN_MGR");
 	connect("MONITOR","CONN_MGR");
 	connect("METRICS","CONN_MGR");
     }
 
     public void init() {
 	log.info("Initializing ESG Data Node Manager...");
 	
 	//TODO
 	//Read in configuration file
 	
 	initCoreServices();
 	
 	//TODO
 	//load up other services....
 	
 	sendAllLoadedNotification();
     }
 
 } 
