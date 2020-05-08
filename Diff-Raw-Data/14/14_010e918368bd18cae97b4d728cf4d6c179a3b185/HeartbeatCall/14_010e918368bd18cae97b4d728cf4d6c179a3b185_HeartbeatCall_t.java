 ///////////////////////////////////////////////////////////////////////////
 //
 // This program is part of Zenoss Core, an open source monitoring platform.
 // Copyright (C) 2007, Zenoss Inc.
 //
 // This program is free software; you can redistribute it and/or modify it
 // under the terms of the GNU General Public License version 2 as published by
 // the Free Software Foundation.
 //
 // For complete information please visit: http://www.zenoss.com/oss/
 //
 ///////////////////////////////////////////////////////////////////////////
 package com.zenoss.zenpacks.zenjmx.call;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import com.zenoss.zenpacks.zenjmx.Configuration;
 
 import com.zenoss.xmlrpc.XmlRpcClient;
 
 import org.apache.log4j.Logger;
 
 import static com.zenoss.zenpacks.zenjmx.OptionsFactory.*;
 
 
 /**
  * <p></p>
  *
  * <p>$Author: chris $</p>
  *
  * @author Christopher Blunck
  * @version $Revision: 1.6 $
  */
 public class HeartbeatCall
   implements Runnable {
 
   // the canonical hostname of the device the heartbeat is associated with
   private String _device;
 
   // the component the heartbeat is associated wtih
   private String _component;
 
   // logger
   private static final Logger _logger = Logger.getLogger(HeartbeatCall.class);
 
 
   /**
    * Creates a HeartbeatCall
    */
   public HeartbeatCall() {
    Configuration config = Configuration.instance();
 
	// use the confName as the device portion of the heartbeat
	_device = config.getProperty(CONF_NAME);
 
     // use the component name from the configuration
     _component = config.getProperty(COMPONENT_NAME);
   }
 
 
   /**
    * @see Runnable#run
    */
   public void run() {
     try {
       Configuration config = Configuration.instance();
 
       String hubUrl = config.getProperty(HUB_URL);
       String confName = config.getProperty(CONF_NAME);
       String username = config.getProperty(HUB_USERNAME);
       String password = config.getProperty(HUB_PASSWORD);
       int timeout = Integer.valueOf(config.getProperty(CYCLE_TIME)) * 3;
 
       _logger.debug("sending heartbeat...");
 
       XmlRpcClient client = new XmlRpcClient(hubUrl, confName);
       try {
         client.setCredentials(username, password);
         client.sendHeartbeat(_device, _component, timeout);
       } finally {
         client.close();
         client = null;
       }
       _logger.debug("heartbeat sent.");
     } catch (Exception e) {
       _logger.error("failed to send heartbeat server", e);
     } catch (Throwable e) {
       _logger.error("unexpected error sending heartbeat", e);
     }
   }
 
 }
