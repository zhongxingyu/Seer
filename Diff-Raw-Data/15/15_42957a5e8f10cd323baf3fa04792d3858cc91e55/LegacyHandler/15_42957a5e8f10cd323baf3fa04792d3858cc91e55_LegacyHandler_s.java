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
 
 package de.cosmocode.palava.ipc.legacy;
 
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Map;
 import java.util.concurrent.ConcurrentMap;
 
 import javax.annotation.concurrent.ThreadSafe;
 
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFutureListener;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelHandler;
 import org.jboss.netty.channel.ChannelHandler.Sharable;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.MapMaker;
 import com.google.common.collect.Maps;
 import com.google.gag.annotation.disclaimer.LegacySucks;
 import com.google.inject.Inject;
 
 import de.cosmocode.palava.bridge.Content;
 import de.cosmocode.palava.bridge.call.Arguments;
 import de.cosmocode.palava.bridge.call.Call;
 import de.cosmocode.palava.bridge.call.CallType;
 import de.cosmocode.palava.bridge.content.JsonContent;
 import de.cosmocode.palava.bridge.request.HttpRequest;
 import de.cosmocode.palava.bridge.scope.Scopes;
 import de.cosmocode.palava.bridge.session.HttpSession;
 import de.cosmocode.palava.core.Registry.Proxy;
 import de.cosmocode.palava.core.Registry.SilentProxy;
 import de.cosmocode.palava.ipc.IpcCallCreateEvent;
 import de.cosmocode.palava.ipc.IpcCallDestroyEvent;
 import de.cosmocode.palava.ipc.IpcCallScope;
 import de.cosmocode.palava.ipc.IpcConnectionCreateEvent;
 import de.cosmocode.palava.ipc.IpcConnectionDestroyEvent;
 import de.cosmocode.palava.ipc.IpcSession;
 import de.cosmocode.palava.ipc.IpcSessionProvider;
 import de.cosmocode.palava.scope.AbstractScopeContext;
 
 /**
  * Handler which process the legacy palava php protocol.
  *
  * @since 1.0
  * @author Willi Schoenborn
  */
 @LegacySucks
 @Sharable
 @ThreadSafe
 final class LegacyHandler extends SimpleChannelHandler {
     
     static final String REQUEST_URI = "REQUEST_URI";
     static final String HTTP_REFERER = "HTTP_REFERER";
     static final String REMOTE_ADDR = "REMOTE_ADDR";
     static final String HTTP_USER_AGENT = "HTTP_USER_AGENT";
 
     private static final Logger LOG = LoggerFactory.getLogger(LegacyHandler.class);
     
     private final ConcurrentMap<Channel, InternalHttpRequest> requests = new MapMaker().makeMap();
     
     private final IpcConnectionCreateEvent connectionCreateEvent;
     
     private final IpcConnectionDestroyEvent connectionDestroyEvent;
     
     private final IpcCallCreateEvent callCreateEvent;
     
     private final IpcCallDestroyEvent callDestroyEvent;
     
     private final IpcSessionProvider sessionProvider;
 
     private final IpcCallScope scope;
     
     private final Executor executor;
     
     @Inject
     public LegacyHandler(
         @Proxy IpcConnectionCreateEvent connectionCreateEvent, 
         @SilentProxy IpcConnectionDestroyEvent connectionDestroyEvent,
         @Proxy IpcCallCreateEvent callCreateEvent, @SilentProxy IpcCallDestroyEvent callDestroyEvent,
         IpcSessionProvider sessionProvider, IpcCallScope scope, Executor executor) {
         this.connectionCreateEvent = Preconditions.checkNotNull(connectionCreateEvent, "CreateEvent");
         this.connectionDestroyEvent = Preconditions.checkNotNull(connectionDestroyEvent, "DestroyEvent");
         this.callCreateEvent = Preconditions.checkNotNull(callCreateEvent, "CreateEvent");
         this.callDestroyEvent = Preconditions.checkNotNull(callDestroyEvent, "DestroyEvent");
         this.sessionProvider = Preconditions.checkNotNull(sessionProvider, "SessionProvider");
         this.scope = Preconditions.checkNotNull(scope, "Scope");
         this.executor = Preconditions.checkNotNull(executor, "Executor");
     }
 
     @Override
     public void channelConnected(ChannelHandlerContext context, ChannelStateEvent event) throws Exception {
         final InternalHttpRequest request = new InternalHttpRequest();
         requests.put(event.getChannel(), request);
     }
     
     @Override
     public void messageReceived(ChannelHandlerContext context, MessageEvent event) throws Exception {
         final Object message = event.getMessage();
         if (message instanceof Call) {
             final Call call = Call.class.cast(message);
             final CallType type = call.getHeader().getCallType();
             final Channel channel = event.getChannel();
             
             if (type == CallType.OPEN) {
                 open(call, channel);
             } else if (type == CallType.CLOSE) {
                 close(channel);
             } else if (call instanceof DetachedCall) {
                 final DetachedCall detachedCall = DetachedCall.class.cast(call);
                 final HttpRequest request = requests.get(channel);
                 detachedCall.attachTo(request);
                 final Content content = call(detachedCall);
                 channel.write(content);
             } else {
                 throw new IllegalStateException(String.format("%s is of unknown type", call));
             }
         }
     }
     
     private void open(Call call, Channel channel) {
         final InternalHttpRequest request = requests.get(channel);
         final Arguments arguments = call.getArguments();
         request.setReferer(arguments.getString(HTTP_REFERER, null));
         final String remoteAddress = arguments.getString(REMOTE_ADDR, null);
         request.setRemoteAddress(remoteAddress);
         request.setRequestUri(arguments.getString(REQUEST_URI, null));
         request.setUserAgent(arguments.getString(HTTP_USER_AGENT, null));
         
         if (!request.isAttached()) {
             final String sessionId = call.getHeader().getSessionId();
             final IpcSession session = sessionProvider.getSession(sessionId, remoteAddress);
             final HttpSession httpSession = new LegacyHttpSessionAdapter(session);
             request.attachTo(httpSession);
         }
         
         connectionCreateEvent.eventIpcConnectionCreate(request);
         channel.write(new JsonContent(ImmutableMap.of("sessionId", request.getSession().getSessionId())));
     }
     
     private Content call(final Call call) {
         try {
             callCreateEvent.eventIpcCallCreate(call);
             scope.enter(call);
             Scopes.setCurrentCall(call);
             return executor.execute(call);
         } finally {
             Scopes.clean();
             callDestroyEvent.eventIpcCallDestroy(call);
             scope.exit();
         }
     }
     
     private void close(final Channel channel) {
         channel.write(JsonContent.EMPTY).addListener(ChannelFutureListener.CLOSE);
     }
     
     @Override
     public void channelClosed(ChannelHandlerContext context, ChannelStateEvent event) throws Exception {
         final Channel channel = event.getChannel();
         final HttpRequest request = requests.remove(channel);
         LOG.trace("Closing connection {}", request);
         connectionDestroyEvent.eventIpcConnectionDestroy(request);
         request.clear();
     }
     
     @Override
     public void exceptionCaught(ChannelHandlerContext context, ExceptionEvent event) throws Exception {
         final Channel channel = event.getChannel();
         LOG.error("Uncaught exception", event.getCause());
         channel.close();
     }
     
     /**
      * Internal implementation of the {@link DetachedHttpRequest} interface.
      *
      * @since 1.0
      * @author Willi Schoenborn
      */
     private static final class InternalHttpRequest extends AbstractScopeContext implements DetachedHttpRequest {
 
         private Map<Object, Object> context;
         
         private HttpSession session;
         
         private String referer;
         
         private String remoteAddress;
         
         private String requestUri;
         
         private String userAgent;
         
         @Override
         protected Map<Object, Object> context() {
             if (context == null) {
                 context = Maps.newHashMap();
             }
             return context;
         }
 
         @Override
         public HttpSession getHttpSession() {
             Preconditions.checkState(session != null, "Not yet attached to a session");
             return session;
         }
         
         @Override
         public IpcSession getSession() {
            return getHttpSession();
         }
         
         @Override
         public void attachTo(HttpSession s) {
             this.session = Preconditions.checkNotNull(s, "Session");
         }
         
         @Override
         public boolean isAttached() {
             return session != null;
         }
 
         @Override
         public URL getReferer() {
             try {
                 return referer == null ? null : new URL(referer);
             } catch (MalformedURLException e) {
                 throw new IllegalArgumentException(referer);
             }
         }
         
         void setReferer(String referer) {
             this.referer = referer;
         }
 
         @Override
         public String getRemoteAddress() {
             return remoteAddress;
         }
         
         void setRemoteAddress(String remoteAddress) {
             this.remoteAddress = remoteAddress;
         }
 
         @Override
         public URI getRequestUri() {
             try {
                 return requestUri == null ? null : new URI(requestUri);
             } catch (URISyntaxException e) {
                 throw new IllegalArgumentException(requestUri);
             }
         }
 
         void setRequestUri(String requestUri) {
             this.requestUri = requestUri;
         }
         
         @Override
         public String getUserAgent() {
             return userAgent;
         }
 
         void setUserAgent(String userAgent) {
             this.userAgent = userAgent;
         }
         
     }
     
 }
