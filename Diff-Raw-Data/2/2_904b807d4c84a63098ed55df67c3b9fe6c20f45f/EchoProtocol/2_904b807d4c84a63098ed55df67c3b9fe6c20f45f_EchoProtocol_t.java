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
 
 package de.cosmocode.palava.ipc.protocol;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Preconditions;
 import com.google.inject.Inject;
 
 import de.cosmocode.palava.core.Registry;
 import de.cosmocode.palava.core.lifecycle.Disposable;
 import de.cosmocode.palava.core.lifecycle.Initializable;
 import de.cosmocode.palava.core.lifecycle.LifecycleException;
 
 /**
  * Implementation of the ECHO protocol.
  *
  * @since 1.0
  * @author Willi Schoenborn
  */
 public final class EchoProtocol implements Protocol, Initializable, Disposable {
     
     private static final Logger LOG = LoggerFactory.getLogger(EchoProtocol.class); 
 
     private final Registry registry;
     
     @Inject
     public EchoProtocol(Registry registry) {
         this.registry = Preconditions.checkNotNull(registry, "Registry");
     }
     
     @Override
     public void initialize() throws LifecycleException {
         registry.register(Protocol.class, this);
     }
     
     @Override
     public Object process(Object request, DetachedConnection connection) throws ProtocolException {
         LOG.debug("Echoing {}", request);
         return request;
     }
 
     @Override
     public boolean supports(Object request) {
         return true;
     }
 
     @Override
     public Object onError(Throwable t, Object request) {
        LOG.warn("Echoing " + request + " caused exception", t);
         return request;
     }
     
     @Override
     public void dispose() throws LifecycleException {
         registry.remove(this);
     }
 
 }
