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
 
 package de.cosmocode.palava.ipc.json.rpc;
 
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Function;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.inject.Inject;
 
 import de.cosmocode.palava.core.Registry;
 import de.cosmocode.palava.core.Registry.Key;
 import de.cosmocode.palava.core.lifecycle.Disposable;
 import de.cosmocode.palava.core.lifecycle.Initializable;
 import de.cosmocode.palava.core.lifecycle.LifecycleException;
 import de.cosmocode.palava.ipc.json.Json;
 import de.cosmocode.palava.ipc.protocol.DetachedConnection;
 import de.cosmocode.palava.ipc.protocol.ListProtocol;
 import de.cosmocode.palava.ipc.protocol.Protocol;
 import de.cosmocode.palava.ipc.protocol.ProtocolException;
 
 /**
  * Batch-style implementation of the Json-Rpc 2.0 specification.
  *
  * @since 1.0
  * @author Willi Schoenborn
  */
 final class JsonRpc2BatchProtocol extends ListProtocol implements Initializable, Disposable {
 
     private static final Logger LOG = LoggerFactory.getLogger(JsonRpc2BatchProtocol.class);
     
     private final Registry registry;
 
     private final JsonRpc2Protocol protocol;
     
     private final Predicate<Object> supports = new Predicate<Object>() {
         
         @Override
         public boolean apply(Object input) {
             return protocol.supports(input);
         }
         
     };
     
     @Inject
     public JsonRpc2BatchProtocol(Registry registry, JsonRpc2Protocol protocol) {
         this.registry = Preconditions.checkNotNull(registry, "Registry");
         this.protocol = Preconditions.checkNotNull(protocol, "Protocol");
     }
     
     @Override
     public void initialize() throws LifecycleException {
         registry.register(Key.get(Protocol.class, Json.class), this);
     }
 
     @Override
     public boolean supports(List<?> request) {
         return Iterables.all(request, supports);
     }
 
     @Override
     public Object process(List<?> request, final DetachedConnection connection) throws ProtocolException {
         LOG.trace("Processing json-rpc 2.0 batch call: {}", request);
         return Lists.newArrayList(Lists.transform(request, new Function<Object, Object>() {
             
             @Override
             public Object apply(Object from) {
                 try {
                     return protocol.process(from, connection);
                 /* CHECKSTYLE:OFF */
                 } catch (RuntimeException e) {
                 /* CHECKSTYLE:ON */
                     return protocol.onError(e, from);
                 } catch (ProtocolException e) {
                     return protocol.onError(e, from);
                 }
             }
             
         }));
     }
 
     @Override
     public Object onError(final Throwable t, List<?> request) {
         return ErrorCode.INTERNAL_ERROR.newResponse(null, t);
     }
     
     @Override
     public void dispose() throws LifecycleException {
         registry.remove(this);
     }
 
 }
