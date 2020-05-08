 /**
  *  Copyright (C) 2009 Progress Software, Inc. All rights reserved.
  *  http://fusesource.com
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.fusesource.meshkeeper.distribution.jms;
 
 import java.net.URI;
 import java.util.List;
 
 import org.apache.activemq.broker.BrokerFactory;
 import org.apache.activemq.broker.BrokerService;
 import org.apache.activemq.broker.TransportConnector;
 import org.fusesource.meshkeeper.control.ControlService;
 
 /**
  * ActiveMQControlService
  * <p>
  * Description:
  * </p>
  *
  * @author cmacnaug
  * @version 1.0
  */
 public class ActiveMQControlService implements ControlService {
 
     BrokerService controlBroker;
     String serviceUri;
     String directory = "activemq-control-service";
 
     /*
      * (non-Javadoc)
      *
      * @see org.fusesource.meshkeeper.control.ControlService#setDirectory()
      */
     public void setDirectory(String path) {
         this.directory = path;
     }
 
     /*
      * (non-Javadoc)
      *
      * @see org.fusesource.meshkeeper.control.ControlService#start()
      */
     public void start() throws Exception {
         controlBroker.setDataDirectory(directory);
         controlBroker.setUseShutdownHook(false);
         controlBroker.start();
         List<TransportConnector> connectors = controlBroker.getTransportConnectors();
        serviceUri = "activemq:" + connectors.get(0).getConnectUri();
     }
 
     /*
      * (non-Javadoc)
      *
      * @see org.fusesource.meshkeeper.control.ControlService#destroy()
      */
     public void destroy() throws Exception {
         if (controlBroker != null) {
             controlBroker.stop();
             controlBroker.waitUntilStopped();
             controlBroker = null;
         }
     }
 
     /*
      * (non-Javadoc)
      *
      * @see org.fusesource.meshkeeper.control.ControlService#getName()
      */
     public String getName() {
         return "ActiveMQ Control Service at: " + getServiceUri();
     }
 
     /*
      * (non-Javadoc)
      *
      * @see org.fusesource.meshkeeper.control.ControlService#getServiceUri()
      */
     public String getServiceUri() {
         return serviceUri;
     }
 
     static ActiveMQControlService create(URI uri) throws Exception {
         ActiveMQControlService rc = new ActiveMQControlService();
         BrokerService controlBroker = null;
         try {
             controlBroker = BrokerFactory.createBroker(uri);
         } catch (Throwable thrown) {
             controlBroker = new BrokerService();
             controlBroker.setBrokerName("MeshKeeperControlBroker");
             controlBroker.addConnector(uri.toString());
             //controlBroker.setPersistent(false);
             controlBroker.setDeleteAllMessagesOnStartup(true);
             controlBroker.setUseJmx(false);
         }
         rc.controlBroker = controlBroker;
         return rc;
     }
 
 }
