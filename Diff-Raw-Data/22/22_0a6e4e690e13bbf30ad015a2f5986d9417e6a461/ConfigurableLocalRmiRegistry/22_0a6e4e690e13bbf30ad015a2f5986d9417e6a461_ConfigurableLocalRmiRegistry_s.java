 /**
  * Copyright 2010 CosmoCode GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.cosmocode.palava.rmi;
 
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Implementation of the {@link RmiRegistry} interface which uses a
  * local registry by calling {@link LocateRegistry#createRegistry(int)}.
  * 
  * @author Tobias Sarnowski
  */
 final class ConfigurableLocalRmiRegistry extends AbstractConfigurableRmiRegistry {
     
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurableRmiRemoteRegistry.class);
 
     @Override
     protected Registry initializeRegistry(String host, int port) throws RemoteException {
         LOG.info("Starting local registry on port {}", port);
         return LocateRegistry.createRegistry(port);
     }
 
     @Override
     public boolean isLocal() {
         return true;
     }
     
 }
