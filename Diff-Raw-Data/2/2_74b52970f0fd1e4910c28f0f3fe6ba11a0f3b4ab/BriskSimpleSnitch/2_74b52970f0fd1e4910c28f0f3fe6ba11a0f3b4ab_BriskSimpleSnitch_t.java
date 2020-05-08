 /**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.cassandra.locator;
 
 import java.io.IOException;
 import java.net.InetAddress;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.apache.cassandra.config.ConfigurationException;
 import org.apache.cassandra.gms.ApplicationState;
 import org.apache.cassandra.gms.Gossiper;
 import org.apache.cassandra.service.StorageService;
 import org.apache.cassandra.utils.FBUtilities;
 
 /**
  * A snitch that detects if Hadoop trackers are active and put this machine in a separate analytics DC
  */
 public class BriskSimpleSnitch extends SimpleSnitch
 {
    protected static Logger logger = LoggerFactory.getLogger(BriskSimpleSnitch.class);
     static final String BRISK_DC = "BriskHadoopAnalyticsDatacenter";
     protected IEndpointSnitch subsnitch = new SimpleSnitch();
     protected String myDC;
 
     public BriskSimpleSnitch() throws IOException, ConfigurationException
     {
         if(System.getProperty("hadoop-trackers", "false").equalsIgnoreCase("true"))
         {
             myDC = BRISK_DC;
             logger.info("Detected Hadoop trackers are enabled, setting my DC to " + BRISK_DC);
         }
         else
         {
             myDC = subsnitch.getDatacenter(FBUtilities.getLocalAddress());
             logger.info("Hadoop trackers not running, setting my DC to " + myDC);
         }
     }
 
     public String getDatacenter(InetAddress endpoint)
     {
         return Gossiper.instance.getEndpointStateForEndpoint(endpoint).getApplicationState(ApplicationState.DC).value;
     }
 
     @Override
     public void gossiperStarting()
     {
         // Share DC info via gossip.
         Gossiper.instance.addLocalApplicationState(ApplicationState.DC, StorageService.instance.valueFactory.datacenter(myDC));
     }
 }
