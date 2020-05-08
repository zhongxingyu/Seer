 /*
  * Copyright (C) 2011 - Jingle Nodes - Yuilop - Neppo
  *
  *   This file is part of Switji (http://jinglenodes.org)
  *
  *   Switji is free software; you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation; either version 2 of the License, or
  *   (at your option) any later version.
  *
  *   Switji is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with MjSip; if not, write to the Free Software
  *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  *   Author(s):
  *   Benhur Langoni (bhlangonijr@gmail.com)
  *   Thiago Camargo (barata7@gmail.com)
  */
 
 package org.jinglenodes.component;
 
 import org.apache.log4j.Logger;
 import org.jinglenodes.jingle.processor.JingleProcessor;
 import org.jinglenodes.sip.processor.SipProcessor;
 import org.jivesoftware.whack.ExternalComponentManager;
 import org.xmpp.component.ComponentException;
 
 public class SIPGatewayApplication {
 
     private static final Logger log = Logger.getLogger(SIPGatewayApplication.class);
     private static final String DESCRIPTION = "SIP Gateway";
     private String subdomain = "sip";
     private SIPGatewayComponent sipGatewayComponent;
     private ExternalComponentManager manager;
     private JingleProcessor jingleProcessor;
     private SipProcessor sipProcessor;
     private String password;
 
     public SIPGatewayComponent getSipGatewayComponent() {
         return sipGatewayComponent;
     }
 
     public ExternalComponentManager getManager() {
         return manager;
     }
 
     public void setManager(ExternalComponentManager manager) {
         this.manager = manager;
     }
 
     public void setSipGatewayComponent(SIPGatewayComponent sipGatewayComponent) {
         this.sipGatewayComponent = sipGatewayComponent;
     }
 
     public String getSubdomain() {
         return subdomain;
     }
 
     public void setSubdomain(String subdomain) {
         this.subdomain = subdomain;
     }
 
     public JingleProcessor getJingleProcessor() {
         return jingleProcessor;
     }
 
     public void setJingleProcessor(JingleProcessor jingleProcessor) {
         this.jingleProcessor = jingleProcessor;
     }
 
     public SipProcessor getSipProcessor() {
         return sipProcessor;
     }
 
     public void setSipProcessor(SipProcessor sipProcessor) {
         this.sipProcessor = sipProcessor;
     }
 
     public void destroy() {
         try {
             manager.removeComponent(subdomain);
         } catch (ComponentException e) {
             log.error("Could Not Remove Component.", e);
         }
         if (sipGatewayComponent.getGatewaySipRouter() != null) {
             sipGatewayComponent.getGatewaySipRouter().shutdown();
             sipGatewayComponent.shutdown();
         }
     }
 
     public void init() {
         log.info("SIP Provider Info: " + sipGatewayComponent);
         int t = 2;
         while (true) {
             try {
                 manager.setSecretKey(subdomain, password);
                 manager.setMultipleAllowed(subdomain, false);
                 manager.addComponent(subdomain, sipGatewayComponent);
                sipGatewayComponent.init();
                 break;
             } catch (ComponentException e) {
                 log.error("Connection Error... ", e);
             }
             log.info("Retrying Connection in " + (t * 1000) + "s");
             try {
                 Thread.sleep(t * 1000);
             } catch (InterruptedException e) {
                 // Do Nothing
             }
             t = t * 2;
             if (t > 120) {
                 t = 2;
             }
         }
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 }
