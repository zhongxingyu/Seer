 /**
  * Copyright (c) 2012 keyhom.c@gmail.com.
  *
  * This software is provided 'as-is', without any express or implied warranty.
  * In no event will the authors be held liable for any damages arising from
  * the use of this software.
  *
  * Permission is granted to anyone to use this software for any purpose
  * excluding commercial applications, and to alter it and redistribute it
  * freely, subject to the following restrictions:
  *
  *     1. The origin of this software must not be misrepresented; you must not
  *     claim that you wrote the original software. If you use this software
  *     in a product, an acknowledgment in the product documentation would be
  *     appreciated but is not required.
  *
  *     2. Altered source versions must be plainly marked as such, and must not
  *     be misrepresented as being the original software.
  *
  *     3. This notice may not be removed or altered from any source
  *     distribution.
  */
 package org.vimide.core.server;
 
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.util.Collections;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
 import org.apache.mina.core.service.IoHandlerAdapter;
 import org.apache.mina.core.session.IdleStatus;
 import org.apache.mina.core.session.IoSession;
 import org.apache.mina.filter.codec.ProtocolCodecFilter;
 import org.apache.mina.filter.executor.ExecutorFilter;
 import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.vimide.core.server.codec.VimideProtocolCodecFactory;
 import org.vimide.core.server.filter.VimideLoggingFilter;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.Maps;
 
 /**
  * Abstract I/O server for vimide.
  * 
  * @author keyhom (keyhom.c@gmail.com)
  */
 public abstract class VimideServer {
 
     /**
      * Logger
      */
     private static final Logger LOGGER = LoggerFactory
             .getLogger(VimideServer.class);
     private static int INSTANCE_ID = 0;
 
     private int id;
     private NioSocketAcceptor socketAcceptor;
     private VimideSessionFactory sessionFactory;
 
     /**
      * Creates an new VimideServer instance.
      */
     public VimideServer() {
         super();
 
         id = ++INSTANCE_ID;
 
         LOGGER.debug("A new VimideServer instance created, ID: {}", id);
     }
 
     /**
      * Starts the vimide server by listening the specified port.
      * 
      * @param port the listening port.
      * @throws Exception
      */
     public void start(int port) throws Exception {
         start("*", port);
     }
 
     /**
      * Starts the vimide server by binding the specified hostname and port.
      * 
      * @param host the binding hostname.
      * @param port the listening port.
      * @throws Exception
      */
     public void start(String host, int port) throws Exception {
         if (Strings.isNullOrEmpty(host))
             host = "*";
 
         if (!(port > 0 && port < 65536))
             throw new IllegalArgumentException(
                     "Listening port must be (0 - 65535]");
 
         start(new InetSocketAddress(host, port));
     }
 
     /**
      * Start the vimide server by binding the specified address.
      * 
      * @param address the binding address.
      * @throws Exception
      */
     public void start(SocketAddress address) throws Exception {
         if (null == address)
             throw new IllegalArgumentException(
                     "Illegal binding address was submit!");
 
         /* preparing to start the vimide server. */
 
         // construct the socket acceptor.
         socketAcceptor = new NioSocketAcceptor(Runtime.getRuntime()
                 .availableProcessors() + 1); // CPU Core Num + 1
 
         // filter chain building.
         DefaultIoFilterChainBuilder chain = socketAcceptor.getFilterChain();
         chain.addFirst("Buffer-Logging", new VimideLoggingFilter());
         chain.addLast("Protocol-Codec", new ProtocolCodecFilter(
                 new VimideProtocolCodecFactory()));
         chain.addLast("Message-Logging", new VimideLoggingFilter());
         chain.addLast("Executions", new ExecutorFilter(Runtime.getRuntime()
                 .availableProcessors(), Runtime.getRuntime()
                 .availableProcessors() * 4, 10, TimeUnit.MINUTES));
 
         // handler building.
         socketAcceptor.setHandler(getSessionFactory());
         socketAcceptor.setReuseAddress(true);
         socketAcceptor.bind(address);
     }
 
     /**
      * Gets the session factory.
      * 
      * @return session factory
      */
     public VimideSessionFactory getSessionFactory() {
         if (null == sessionFactory)
             sessionFactory = getDefaultSessionFactory();
         return sessionFactory;
     }
 
     /**
      * Sets the session factory.
      * 
     * @param sessionFactory the session factory implemetation.
      * @return reference of this.
      */
     public VimideServer setSessionFactory(VimideSessionFactory sessionFactory) {
         this.sessionFactory = sessionFactory;
         return this;
     }
 
     /**
      * Gets the default session factory.
      * 
      * @return default session factory.
      */
     public VimideSessionFactory getDefaultSessionFactory() {
         return DefaultSessionFactory.instance;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see java.lang.Object#hashCode()
      */
     @Override
     public int hashCode() {
         return new HashCodeBuilder().append(id).toHashCode();
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see java.lang.Object#equals(java.lang.Object)
      */
     @Override
     public boolean equals(Object o) {
         if (!(o instanceof VimideServer))
             return false;
         if (this == o)
             return true;
 
         final VimideServer that = (VimideServer) o;
         return new EqualsBuilder().append(id, that.id).isEquals();
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
         final ToStringBuilder tsb = new ToStringBuilder(this);
         tsb.append(id);
         return tsb.toString();
     }
 
     /**
      * Default implementation of Vimide's server session factory.
      * 
      * @author keyhom (keyhom.c@gmail.com)
      */
     public static class DefaultSessionFactory extends IoHandlerAdapter
             implements VimideSessionFactory {
 
         /**
          * Logger
          */
         static final Logger LOGGER = LoggerFactory
                 .getLogger(DefaultSessionFactory.class);
 
         /**
          * Session container.
          */
         protected final Map<Long, VimideSession> sessionMap = Maps
                 .newIdentityHashMap();
 
         @Override
         public Map<Long, VimideSession> getSessionMap() {
             return Collections.unmodifiableMap(sessionMap);
         }
 
         /**
          * Checks if the session existing.
          * 
          * @param session the session for validating.
          * @return true if the session existing, false otherwise.
          */
         public boolean containsSession(VimideSession session) {
             return sessionMap.containsValue(session);
         }
 
         /**
          * Default singleton provided by Bob's lazy mode.
          */
         static final VimideSessionFactory instance = new DefaultSessionFactory() {
 
             /**
              * {@inheritDoc}
              * 
              * @see org.apache.mina.core.service.IoHandlerAdapter#sessionCreated(org.apache.mina.core.session.IoSession)
              */
             @Override
             public void sessionCreated(IoSession session) throws Exception {
                 super.sessionCreated(session);
 
                 LOGGER.debug("Connected from: {}", session.getRemoteAddress());
                 sessionMap.put(session.getId(), new VimideSession(session));
             }
 
             /**
              * {@inheritDoc}
              * 
              * @see org.apache.mina.core.service.IoHandlerAdapter#sessionOpened(org.apache.mina.core.session.IoSession)
              */
             @Override
             public void sessionOpened(IoSession session) throws Exception {
                 super.sessionOpened(session);
             }
 
             /**
              * {@inheritDoc}
              * 
              * @see org.apache.mina.core.service.IoHandlerAdapter#sessionClosed(org.apache.mina.core.session.IoSession)
              */
             @Override
             public void sessionClosed(IoSession session) throws Exception {
                 super.sessionClosed(session);
 
                 LOGGER.debug("Session closed from: {}",
                         session.getRemoteAddress());
 
                 sessionMap.remove(session);
             }
 
             /**
              * {@inheritDoc}
              * 
              * @see org.apache.mina.core.service.IoHandlerAdapter#sessionIdle(org.apache.mina.core.session.IoSession,
              *      org.apache.mina.core.session.IdleStatus)
              */
             @Override
             public void sessionIdle(IoSession session, IdleStatus status)
                     throws Exception {
                 super.sessionIdle(session, status);
 
                 LOGGER.debug("Session Idle as {} from {}", status,
                         session.getRemoteAddress());
             }
 
             /**
              * {@inheritDoc}
              * 
              * @see org.apache.mina.core.service.IoHandlerAdapter#exceptionCaught(org.apache.mina.core.session.IoSession,
              *      java.lang.Throwable)
              */
             @Override
             public void exceptionCaught(IoSession session, Throwable cause)
                     throws Exception {
                 super.exceptionCaught(session, cause);
 
                 LOGGER.debug("Exception caught by {} for {}",
                         session.getRemoteAddress(), cause.getMessage());
             }
 
             /**
              * {@inheritDoc}
              * 
              * @see org.apache.mina.core.service.IoHandlerAdapter#messageReceived(org.apache.mina.core.session.IoSession,
              *      java.lang.Object)
              */
             @SuppressWarnings("unused")
             @Override
             public void messageReceived(IoSession session, Object message)
                     throws Exception {
                 super.messageReceived(session, message);
 
                 if (!sessionMap.containsKey(session.getId())) {
                     throw new IllegalStateException(
                             "Illegal message received in a noop-session.");
                 }
 
                 final VimideSession vimideSession = sessionMap.get(session
                         .getId());
             }
 
             /**
              * {@inheritDoc}
              * 
              * @see org.apache.mina.core.service.IoHandlerAdapter#messageSent(org.apache.mina.core.session.IoSession,
              *      java.lang.Object)
              */
             @Override
             public void messageSent(IoSession session, Object message)
                     throws Exception {
                 super.messageSent(session, message);
             }
 
         };
     }
 }
