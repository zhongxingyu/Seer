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
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableSet;
 import com.google.inject.Inject;
 import com.google.inject.internal.Maps;
 
 import de.cosmocode.palava.core.Registry;
 import de.cosmocode.palava.core.Registry.Key;
 import de.cosmocode.palava.core.Registry.Proxy;
 import de.cosmocode.palava.core.Registry.SilentProxy;
 import de.cosmocode.palava.core.lifecycle.Disposable;
 import de.cosmocode.palava.core.lifecycle.Initializable;
 import de.cosmocode.palava.core.lifecycle.LifecycleException;
 import de.cosmocode.palava.ipc.IpcArguments;
 import de.cosmocode.palava.ipc.IpcCall;
 import de.cosmocode.palava.ipc.IpcCallCreateEvent;
 import de.cosmocode.palava.ipc.IpcCallDestroyEvent;
 import de.cosmocode.palava.ipc.IpcCallScope;
 import de.cosmocode.palava.ipc.IpcCommandExecutionException;
 import de.cosmocode.palava.ipc.IpcCommandExecutor;
 import de.cosmocode.palava.ipc.IpcCommandNotAvailableException;
 import de.cosmocode.palava.ipc.IpcConnection;
 import de.cosmocode.palava.ipc.IpcConnectionDestroyEvent;
 import de.cosmocode.palava.ipc.IpcSession;
 import de.cosmocode.palava.ipc.IpcSessionProvider;
 import de.cosmocode.palava.ipc.MapIpcArguments;
 import de.cosmocode.palava.ipc.json.Json;
 import de.cosmocode.palava.ipc.protocol.DetachedConnection;
 import de.cosmocode.palava.ipc.protocol.MapProtocol;
 import de.cosmocode.palava.ipc.protocol.Protocol;
 import de.cosmocode.palava.ipc.protocol.ProtocolException;
 
 /**
  * Implementation of the {@link JsonIpcConnectorMapHandler} interface which
  * uses the Json-RPC 2.0 protocol as proposed by:
  * <a href="http://groups.google.com/group/json-rpc/web/json-rpc-1-2-proposal">
  *   http://groups.google.com/group/json-rpc/web/json-rpc-1-2-proposal
  * </a>.
  *
  * @since 12.2.2010
  * @author Willi Schoenborn
  */
 final class JsonRpc2Protocol extends MapProtocol implements IpcConnectionDestroyEvent, Initializable, Disposable {
 
     private static final Logger LOG = LoggerFactory.getLogger(JsonRpc2Protocol.class);
 
     private static final ImmutableSet<ImmutableSet<String>> KEYS = ImmutableSet.of(
         // normal request
         ImmutableSet.of(JsonRpc.JSON_RPC, JsonRpc.METHOD, JsonRpc.PARAMS, JsonRpc.ID),
         // normal request, without params
         ImmutableSet.of(JsonRpc.JSON_RPC, JsonRpc.METHOD, JsonRpc.ID),
         // notification
         ImmutableSet.of(JsonRpc.JSON_RPC, JsonRpc.METHOD, JsonRpc.PARAMS),
         // notification, without params
         ImmutableSet.of(JsonRpc.JSON_RPC, JsonRpc.METHOD)
     );
     
     private static final String VERSION = "2.0";
 
     private static final ImmutableSet<Class<?>> VALID_ID_TYPES = ImmutableSet.<Class<?>>of(
         String.class,
         Long.class,
         Integer.class,
         Boolean.class
     );
     
     private static final UUID IDENTIFIER = UUID.randomUUID();
     private static final String IDENTIFIER_VALUE = "Json-RPC 2.0";
     
     private final Registry registry;
     
     private final IpcCallCreateEvent createEvent;
     
     private final IpcCallDestroyEvent destroyEvent;
     
     private final IpcSessionProvider sessionProvider;
     
     private final IpcCommandExecutor commandExecutor;
     
     private final IpcCallScope scope;
     
     @Inject
     public JsonRpc2Protocol(Registry registry,
         @Proxy IpcCallCreateEvent createEvent, @SilentProxy IpcCallDestroyEvent destroyEvent,
         IpcSessionProvider sessionProvider, IpcCommandExecutor commandExecutor, IpcCallScope scope) {
     
         this.registry = Preconditions.checkNotNull(registry, "Registry");
         this.createEvent = Preconditions.checkNotNull(createEvent, "CreateEvent");
         this.destroyEvent = Preconditions.checkNotNull(destroyEvent, "DestroyEvent");
         this.sessionProvider = Preconditions.checkNotNull(sessionProvider, "SessionProvider");
         this.commandExecutor = Preconditions.checkNotNull(commandExecutor, "CommandExecutor");
         this.scope = Preconditions.checkNotNull(scope, "Scope");
     }
     
     @Override
     public void initialize() throws LifecycleException {
         registry.register(Key.get(Protocol.class, Json.class), this);
         registry.register(IpcConnectionDestroyEvent.class, this);
     }
     
     @Override
     public boolean supports(Map<?, ?> request) {
         final int size = request.size();
         final Set<?> keys = request.keySet();
         for (ImmutableSet<String> set : KEYS) {
             if (size == set.size() && keys.containsAll(set)) {
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public Object process(Map<?, ?> request, DetachedConnection connection) throws ProtocolException {
         LOG.trace("Processing json-rpc 2.0 call: {}", request);
         
         final Object id = request.get(JsonRpc.ID);
         LOG.trace("Call id: {}", id);
         
         if (id != null && !VALID_ID_TYPES.contains(id.getClass())) {
             return ErrorCode.INVALID_REQUEST.newResponse(id, "id must be on of [string, number, boolean]");
         }
         
         final Object untypedJsonRpc = request.get(JsonRpc.JSON_RPC);
         
         if (!VERSION.equals(untypedJsonRpc)) {
             return ErrorCode.INVALID_REQUEST.newResponse(id, "jsonrpc must be 2.0");
         }
         
         final Object untypedMethod = request.get(JsonRpc.METHOD);
         
         final String method;
         if (untypedMethod instanceof String) {
             method = String.class.cast(untypedMethod);
             LOG.trace("Requested method: {}", method);
         } else {
             return ErrorCode.INVALID_REQUEST.newResponse(id, "method must be a string");
         }
         
         final Object untypedParams = request.get(JsonRpc.PARAMS);
         
         final IpcArguments arguments;
         
         if (untypedParams == null && !request.containsKey(JsonRpc.PARAMS)) {
             LOG.trace("No params, using empty map");
             arguments = new MapIpcArguments(Maps.<String, Object>newHashMap());
         } else if (untypedParams instanceof List<?>) {
             @SuppressWarnings("unchecked")
             final List<Object> params = List.class.cast(untypedParams);
             LOG.trace("Incoming positional params: {}", untypedParams);
             arguments = new JsonRpcArguments(params);
         } else if (untypedParams instanceof Map<?, ?>) {
             @SuppressWarnings("unchecked")
             final Map<String, Object> params = Map.class.cast(untypedParams);
             LOG.trace("Incoming named params: {}", untypedParams);
             arguments = new MapIpcArguments(params);
         } else {
             return ErrorCode.INVALID_PARAMS.newResponse(id, "params must be either an array or an object");
         }
         
         final IpcSession session = sessionProvider.getSession(connection.getConnectionId(), null);
         connection.attachTo(session);
         connection.set(IDENTIFIER, IDENTIFIER_VALUE);
         final IpcCall call = new JsonRpcCall(arguments, connection);
         
         return execute(id, method, call);
     }
     
     private Object execute(Object id, String method, final IpcCall call) {
         createEvent.eventIpcCallCreate(call);
         scope.enter(call);
 
         try {
             final Map<String, Object> result = commandExecutor.execute(method, call);
                 
             if (id == null) {
                 LOG.trace("Request was notification, returning no result");
                 return Protocol.NO_RESPONSE;
             } else {
                 LOG.trace("Returning {}", result);
                 return newResult(result, id);
             }
         } catch (IpcCommandNotAvailableException e) {
             return ErrorCode.METHOD_NOT_FOUND.newResponse(id, e.getCause());
         } catch (IpcCommandExecutionException e) {
             return ErrorCode.INTERNAL_ERROR.newResponse(id, e.getCause());
         } finally {
             destroyEvent.eventIpcCallDestroy(call);
            scope.exit();
         }
     }
     
     private Map<String, Object> newResult(Object result, Object id) {
         return newHashMap(
             JsonRpc.JSON_RPC, VERSION,
             JsonRpc.RESULT, result,
             JsonRpc.ID, id
         );
     }
     
     private Map<String, Object> newHashMap(String k1, Object v1, String k2, Object v2, String k3, Object v3) {
         final Map<String, Object> returnValue = Maps.newHashMap();
         returnValue.put(k1, v1);
         returnValue.put(k2, v2);
         returnValue.put(k3, v3);
         return returnValue;
     }
     
     @Override
     public Object onError(Throwable t, Map<?, ?> request) {
         return ErrorCode.INTERNAL_ERROR.newResponse(request.get(JsonRpc.ID), t);
     }
     
     @Override
     public void eventIpcConnectionDestroy(IpcConnection connection) {
         final String identifier = connection.get(IDENTIFIER);
         if (identifier == null) return;
         if (identifier.equals(IDENTIFIER_VALUE)) {
             connection.getSession().clear();
         }
     }
     
     @Override
     public void dispose() throws LifecycleException {
         registry.remove(this);
     }
     
 }
