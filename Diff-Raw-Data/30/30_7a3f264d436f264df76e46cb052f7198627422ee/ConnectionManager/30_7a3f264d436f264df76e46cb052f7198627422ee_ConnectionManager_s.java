 //
 // $Id$
 //
 // Narya library - tools for developing networked games
 // Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
 // http://www.threerings.net/code/narya/
 //
 // This library is free software; you can redistribute it and/or modify it
 // under the terms of the GNU Lesser General Public License as published
 // by the Free Software Foundation; either version 2.1 of the License, or
 // (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
 package com.threerings.presents.server.net;
 
 import java.net.InetSocketAddress;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import java.nio.ByteBuffer;
 import java.nio.channels.DatagramChannel;
 import java.nio.channels.SelectableChannel;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.nio.channels.spi.SelectorProvider;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 import com.samskivert.util.IntMap;
 import com.samskivert.util.IntMaps;
 import com.samskivert.util.Invoker;
 import com.samskivert.util.LoopingThread;
 import com.samskivert.util.Queue;
 import com.samskivert.util.ResultListener;
 import com.samskivert.util.Tuple;
 
 import com.threerings.io.FramingOutputStream;
 import com.threerings.io.ObjectOutputStream;
 
 import com.threerings.presents.annotation.AuthInvoker;
 import com.threerings.presents.client.Client;
 import com.threerings.presents.data.ConMgrStats;
 import com.threerings.presents.net.AuthRequest;
 import com.threerings.presents.net.AuthResponse;
 import com.threerings.presents.net.DownstreamMessage;
 import com.threerings.presents.server.Authenticator;
 import com.threerings.presents.server.ChainedAuthenticator;
 import com.threerings.presents.server.DummyAuthenticator;
 import com.threerings.presents.server.PresentsDObjectMgr;
 import com.threerings.presents.server.ReportManager;
 import com.threerings.presents.server.ShutdownManager;
 import com.threerings.presents.util.DatagramSequencer;
 
 import static com.threerings.presents.Log.log;
 
 /**
  * The connection manager manages the socket on which connections are received. It creates
  * connection objects to manage each individual connection, but those connection objects interact
  * closely with the connection manager because network I/O is done via a poll()-like mechanism
  * rather than via threads.
  */
 @Singleton
 public class ConnectionManager extends LoopingThread
     implements ShutdownManager.Shutdowner, ReportManager.Reporter
 {
     /**
      * Creates a connection manager instance. Don't call this, Guice will do it for you.
      */
     @Inject public ConnectionManager (ShutdownManager shutmgr, ReportManager repmgr)
     {
         super("ConnectionManager");
         shutmgr.registerShutdowner(this);
         repmgr.registerReporter(this);
     }
 
     /**
      * Constructs and initialized a connection manager (binding socket on which it will listen for
      * client connections to each of the specified ports).
      */
     public void init (int[] ports)
         throws IOException
     {
         init(ports, new int[0]);
     }
 
     /**
      * Constructs and initialized a connection manager (binding socket on which it will listen for
      * client connections to each of the specified ports).
      */
     public void init (int[] ports, int[] datagramPorts)
         throws IOException
     {
         _ports = ports;
         _datagramPorts = datagramPorts;
         _selector = SelectorProvider.provider().openSelector();
 
         // create our stats record
         _stats = new ConMgrStats();
         _lastStats = new ConMgrStats();
     }
 
     /**
      * Configures the connection manager with an entity that will be informed of the success or
      * failure of the connection manager initialization process. <em>Note:</em> the callback
      * methods will be called on the connection manager thread, so be careful not to do anything on
      * those methods that will conflict with activities on the dobjmgr thread, etc.
      */
     public void setStartupListener (ResultListener<Object> rl)
     {
         _startlist = rl;
     }
 
     /**
      * Adds an authenticator to the authentication chain. This authenticator will be offered a
      * chance to authenticate incoming connections in lieu of the main autuenticator.
      */
     public void addChainedAuthenticator (ChainedAuthenticator author)
     {
         author.setChainedAuthenticator(_author);
         _author = author;
     }
 
     /**
      * Instructs us to execute the specified runnable when the connection manager thread exits.
      * <em>Note:</em> this will be executed on the connection manager thread, so don't do anything
      * dangerous. Only one action may be specified and it may be cleared by calling this method
      * with null.
      */
     public void setShutdownAction (Runnable onExit)
     {
         _onExit = onExit;
     }
 
     /**
      * Returns our current runtime statistics. <em>Note:</em> don't call this method <em>too</em>
      * frequently as it is synchronized and will contend with the network I/O thread.
      */
     public synchronized ConMgrStats getStats ()
     {
         // fill in our snapshot values
         _stats.authQueueSize = _authq.size();
         _stats.deathQueueSize = _deathq.size();
         _stats.outQueueSize = _outq.size();
         if (_oflowqs.size() > 0) {
             _stats.overQueueSize = 0;
             for (OverflowQueue oq : _oflowqs.values()) {
                 _stats.overQueueSize += oq.size();
             }
         }
         return (ConMgrStats)_stats.clone();
     }
 
     /**
      * Adds the specified connection observer to the observers list.  Connection observers will be
      * notified of connection-related events. An observer will not be added to the list twice.
      *
      * @see ConnectionObserver
      */
     public void addConnectionObserver (ConnectionObserver observer)
     {
         synchronized (_observers) {
             _observers.add(observer);
         }
     }
 
     /**
      * Removes the specified connection observer from the observers list.
      */
     public void removeConnectionObserver (ConnectionObserver observer)
     {
         synchronized (_observers) {
             _observers.remove(observer);
         }
     }
 
     /**
      * Queues a connection up to be closed on the conmgr thread.
      */
     public void closeConnection (Connection conn)
     {
         _deathq.append(conn);
     }
 
     /**
      * Performs the authentication process on the specified connection. This is called by {@link
      * AuthingConnection} itself once it receives its auth request.
      */
     public void authenticateConnection (AuthingConnection conn)
     {
         _author.authenticateConnection(_authInvoker, conn, new ResultListener<AuthingConnection>() {
             public void requestCompleted (AuthingConnection conn) {
                 _authq.append(conn);
             }
             public void requestFailed (Exception cause) {
                 // this never happens
             }
         });
     }
 
     // documentation inherited from interface ReportManager.Reporter
     public void appendReport (
         StringBuilder report, long now, long sinceLast, boolean reset)
     {
         ConMgrStats stats = getStats();
         int connects = stats.connects - _lastStats.connects;
         int disconnects = stats.disconnects - _lastStats.disconnects;
         long bytesIn = stats.bytesIn - _lastStats.bytesIn;
         long bytesOut = stats.bytesOut - _lastStats.bytesOut;
         long msgsIn = stats.msgsIn - _lastStats.msgsIn;
         long msgsOut = stats.msgsOut - _lastStats.msgsOut;
         if (reset) {
             _lastStats = stats;
         }
 
         // make sure we don't div0 if this method somehow gets called twice in
         // the same millisecond
         sinceLast = Math.max(sinceLast, 1L);
 
         report.append("* presents.net.ConnectionManager:\n");
         report.append("- Network connections: ");
         report.append(connects).append(" connects, ");
         report.append(disconnects).append(" disconnects\n");
         report.append("- Network input: ");
         report.append(bytesIn).append(" bytes, ");
         report.append(msgsIn).append(" msgs, ");
         report.append(msgsIn*1000/sinceLast).append(" mps, ");
         long avgIn = (msgsIn == 0) ? 0 : (bytesIn/msgsIn);
         report.append(avgIn).append(" avg size, ");
         report.append(bytesIn*1000/sinceLast).append(" bps\n");
         report.append("- Network output: ");
         report.append(bytesOut).append(" bytes, ");
         report.append(msgsOut).append(" msgs, ");
         report.append(msgsOut*1000/sinceLast).append(" mps, ");
         long avgOut = (msgsOut == 0) ? 0 : (bytesOut/msgsOut);
         report.append(avgOut).append(" avg size, ");
         report.append(bytesOut*1000/sinceLast).append(" bps\n");
     }
 
     @Override // from LoopingThread
     public boolean isRunning ()
     {
         // Prevent exiting our thread until the object manager is done.
         return super.isRunning() || _omgr.isRunning();
     }
 
     /**
      * Notifies the connection observers of a connection event. Used internally.
      */
     protected void notifyObservers (
         int code, Connection conn, Object arg1, Object arg2)
     {
         synchronized (_observers) {
             for (ConnectionObserver obs : _observers) {
                 switch (code) {
                 case CONNECTION_ESTABLISHED:
                     obs.connectionEstablished(conn, (AuthRequest)arg1, (AuthResponse)arg2);
                     break;
                 case CONNECTION_FAILED:
                     obs.connectionFailed(conn, (IOException)arg1);
                     break;
                 case CONNECTION_CLOSED:
                     obs.connectionClosed(conn);
                     break;
                 default:
                     throw new RuntimeException("Invalid code supplied to notifyObservers: " + code);
                 }
             }
         }
     }
 
     @Override
     protected void willStart ()
     {
         int successes = 0;
         IOException failure = null;
         for (int port : _ports) {
             try {
                 // create a listening socket and add it to the select set
                 _ssocket = ServerSocketChannel.open();
                 _ssocket.configureBlocking(false);
 
                 InetSocketAddress isa = new InetSocketAddress(port);
                 _ssocket.socket().bind(isa);
                 registerChannel(_ssocket);
                 successes++;
                 log.info("Server listening on " + isa + ".");
 
             } catch (IOException ioe) {
                 log.warning("Failure listening to socket on port '" + port + "'.", ioe);
                 failure = ioe;
             }
         }
 
         // NOTE: this is not currently working; it works but for whatever inscrutable reason the
         // inherited channel claims to be readable immediately every time through the select() loop
         // which causes the server to consume 100% of the CPU repeatedly ignoring the inherited
         // channel (except when an actual connection comes in in which case it does the right
         // thing)
 
 //         // now look to see if we were passed a socket inetd style by a
 //         // privileged parent process
 //         try {
 //             Channel inherited = System.inheritedChannel();
 //             if (inherited instanceof ServerSocketChannel) {
 //                 _ssocket = (ServerSocketChannel)inherited;
 //                 _ssocket.configureBlocking(false);
 //                 registerChannel(_ssocket);
 //                 successes++;
 //                 log.info("Server listening on " +
 //                          _ssocket.socket().getInetAddress() + ":" +
 //                          _ssocket.socket().getLocalPort() + ".");
 
 //             } else if (inherited != null) {
 //                 log.warning("Inherited non-server-socket channel " + inherited + ".");
 //             }
 //         } catch (IOException ioe) {
 //             log.warning("Failed to check for inherited channel.");
 //         }
 
         // if we failed to listen on at least one port, give up the ghost
         if (successes == 0) {
             if (_startlist != null) {
                 _startlist.requestFailed(failure);
             }
             return;
         }
 
         // open up the datagram ports as well
         for (int port : _datagramPorts) {
             try {
                 // create a channel and add it to the select set
                 _datagramChannel = DatagramChannel.open();
                 _datagramChannel.configureBlocking(false);
 
                 InetSocketAddress isa = new InetSocketAddress(port);
                 _datagramChannel.socket().bind(isa);
                 registerChannel(_datagramChannel);
                 log.info("Server accepting datagrams on " + isa + ".");
 
             } catch (IOException ioe) {
                 log.warning("Failure opening datagram channel on port '" +
                     port + "'.", ioe);
             }
         }
 
         // we'll use these for sending messages to clients
         _framer = new FramingOutputStream();
         _flattener = new ByteArrayOutputStream();
 
         // notify our startup listener, if we have one
         if (_startlist != null) {
             _startlist.requestCompleted(null);
         }
     }
 
     /** Helper function for {@link #willStart}. */
     protected void registerChannel (final ServerSocketChannel listener)
         throws IOException
     {
         // register this listening socket and map its select key to a net event handler that will
         // accept new connections
         SelectionKey sk = listener.register(_selector, SelectionKey.OP_ACCEPT);
         _handlers.put(sk, new NetEventHandler() {
             public int handleEvent (long when) {
                 acceptConnection(listener);
                 // there's no easy way to measure bytes read when accepting a connection, so we
                 // claim nothing
                 return 0;
             }
             public boolean checkIdle (long now) {
                 return false; // we're never idle
             }
         });
     }
 
     /** Helper function for {@link #willStart}. */
     protected void registerChannel (final DatagramChannel listener)
         throws IOException
     {
         SelectionKey sk = listener.register(_selector, SelectionKey.OP_READ);
         _handlers.put(sk, new NetEventHandler() {
             public int handleEvent (long when) {
                 return readDatagram(listener, when);
             }
             public boolean checkIdle (long now) {
                 return false; // we're never idle
             }
         });
     }
 
     /**
      * Returns a reference to the output stream used to flatten messages into byte arrays.  Should
      * only be called by {@link Connection}.
      */
     protected ByteArrayOutputStream getFlattener ()
     {
         return _flattener;
     }
 
     /**
      * Performs the select loop. This is the body of the conmgr thread.
      */
     @Override
     protected void iterate ()
     {
         long iterStamp = System.currentTimeMillis();
 
         // close any connections that have been queued up to die
         Connection dconn;
         while ((dconn = _deathq.getNonBlocking()) != null) {
             // it's possible that we caught an EOF trying to read from this connection even after
             // it was queued up for death, so let's avoid trying to close it twice
             if (!dconn.isClosed()) {
                 dconn.close();
             }
         }
 
         // close connections that have had no network traffic for too long
         for (NetEventHandler handler : _handlers.values()) {
             if (handler.checkIdle(iterStamp)) {
                 // this will queue the connection for closure on our next tick
                 closeConnection((Connection)handler);
             }
         }
 
         // send any messages that are waiting on the outgoing overflow and message queues
         sendOutgoingMessages(iterStamp);
 
         // if we have been shutdown, but we're still around because the DObjectManager is still
         // running (and we want to deliver any outgoing events queued up during shutdown), then we
         // stop here, because we've delivered outgoing events on this tick and all that remains
         // below is accepting new connections and receiving incoming messages, neither of which we
         // want to do during the shutdown process
         if (!super.isRunning()) {
             return;
         }
 
         // check for connections that have completed authentication
         AuthingConnection conn;
         while ((conn = _authq.getNonBlocking()) != null) {
             try {
                 // construct a new running connection to handle this connections network traffic
                 // from here on out
                 SelectionKey selkey = conn.getSelectionKey();
                 RunningConnection rconn = new RunningConnection(
                     this, selkey, conn.getChannel(), iterStamp);
 
                 // we need to keep using the same object input and output streams from the
                 // beginning of the session because they have context that needs to be preserved
                 rconn.inheritStreams(conn);
 
                 // replace the mapping in the handlers table from the old conn with the new one
                 _handlers.put(selkey, rconn);
 
                 // add a mapping for the connection id and set the datagram secret
                 _connections.put(rconn.getConnectionId(), rconn);
                 rconn.setDatagramSecret(
                     conn.getAuthRequest().getCredentials().getDatagramSecret());
 
                 // transfer any overflow queue for that connection
                 OverflowQueue oflowHandler = _oflowqs.remove(conn);
                 if (oflowHandler != null) {
                     _oflowqs.put(rconn, oflowHandler);
                 }
 
                 // and let our observers know about our new connection
                 notifyObservers(CONNECTION_ESTABLISHED, rconn,
                                 conn.getAuthRequest(), conn.getAuthResponse());
 
             } catch (IOException ioe) {
                 log.warning("Failure upgrading authing connection to running.", ioe);
             }
         }
 
         Set<SelectionKey> ready = null;
         try {
             // check for incoming network events
 //             log.debug("Selecting from " + StringUtil.toString(_selector.keys()) + " (" +
 //                       SELECT_LOOP_TIME + ").");
             int ecount = _selector.select(SELECT_LOOP_TIME);
             ready = _selector.selectedKeys();
             if (ecount == 0) {
                 if (ready.size() == 0) {
                     return;
                 } else {
                     log.warning("select() returned no selected sockets, but there are " +
                                 ready.size() + " in the ready set.");
                 }
             }
 
         } catch (IOException ioe) {
             if ("Invalid argument".equals(ioe.getMessage())) {
                 // what is this, anyway?
                 log.warning("Failure select()ing.", ioe);
             } else {
                 log.warning("Failure select()ing [ioe=" + ioe + "].");
             }
             return;
 
         } catch (RuntimeException re) {
             // this block of code deals with a bug in the _selector that we observed on 2005-05-02,
             // instead of looping indefinitely after things go pear-shaped, shut us down in an
             // orderly fashion
             log.warning("Failure select()ing.", re);
             if (_runtimeExceptionCount++ >= 20) {
                 log.warning("Too many errors, bailing.");
                 shutdown();
             }
             return;
         }
         // clear the runtime error count
         _runtimeExceptionCount = 0;
 
         // process those events
 //         log.info("Ready set " + StringUtil.toString(ready) + ".");
         for (SelectionKey selkey : ready) {
             NetEventHandler handler = null;
             try {
                 handler = _handlers.get(selkey);
                 if (handler == null) {
                     log.warning("Received network event but have no registered handler " +
                                 "[selkey=" + selkey + "].");
                     // request that this key be removed from our selection set, which normally
                     // happens automatically but for some reason didn't
                     selkey.cancel();
                     continue;
                 }
 
 //                 log.info("Got event [selkey=" + selkey + ", handler=" + handler + "].");
 
                 int got = handler.handleEvent(iterStamp);
                 if (got != 0) {
                     synchronized (this) {
                         _bytesIn += got;
                         _stats.bytesIn += got;
                         // we know that the handlers only report having read bytes when they have a
                         // whole message, so we can count thusly
                         _msgsIn++;
                         _stats.msgsIn++;
                     }
                 }
 
             } catch (Exception e) {
                 log.warning("Error processing network data: " + handler + ".", e);
 
                 // if you freak out here, you go straight in the can
                 if (handler != null && handler instanceof Connection) {
                     closeConnection((Connection)handler);
                 }
             }
         }
         ready.clear();
     }
 
     /**
      * Writes all queued overflow and normal messages to their respective sockets. Connections that
      * already have established overflow queues will have their messages appended to their overflow
      * queue instead so that they are delivered in the proper order.
      */
     protected void sendOutgoingMessages (long iterStamp)
     {
         // first attempt to send any messages waiting on the overflow queues
         if (_oflowqs.size() > 0) {
             Iterator<OverflowQueue> oqiter = _oflowqs.values().iterator();
             while (oqiter.hasNext()) {
                 OverflowQueue oq = oqiter.next();
                 try {
                     // try writing the messages in this overflow queue
                     if (oq.writeOverflowMessages(iterStamp)) {
                         // if they were all written, we can remove it
                         oqiter.remove();
                     }
 
                 } catch (IOException ioe) {
                     oq.conn.handleFailure(ioe);
                 }
             }
         }
 
         // then send any new messages
         Tuple<Connection, byte[]> tup;
         while ((tup = _outq.getNonBlocking()) != null) {
             Connection conn = tup.left;
 
             // if an overflow queue exists for this client, go ahead and slap the message on there
             // because we can't send it until all other messages in their queue have gone out
             OverflowQueue oqueue = _oflowqs.get(conn);
             if (oqueue != null) {
                 int size = oqueue.size();
                 if ((size > 500) && (size % 50 == 0)) {
                     log.warning("Aiya, big overflow queue for " + conn + " [size=" + size +
                                 ", adding=" + tup.right + "].");
                 }
                 oqueue.add(tup.right);
                 continue;
             }
 
             // otherwise write the message out to the client directly
             writeMessage(conn, tup.right, _oflowHandler);
         }
 
         // send any datagrams
         while ((tup = _dataq.getNonBlocking()) != null) {
             writeDatagram(tup.left, tup.right);
         }
     }
 
     /**
      * Writes a message out to a connection, passing the buck to the partial write handler if the
      * entire message could not be written.
      *
      * @return true if the message was fully written, false if it was partially written (in which
      * case the partial message handler will have been invoked).
      */
     protected boolean writeMessage (Connection conn, byte[] data, PartialWriteHandler pwh)
     {
         // if the connection to which this message is destined is closed, drop the message and move
         // along quietly; this is perfectly legal, a user can logoff whenever they like, even if we
         // still have things to tell them; such is life in a fully asynchronous distributed system
         if (conn.isClosed()) {
             return true;
         }
 
         // sanity check the message size
         if (data.length > 1024 * 1024) {
             log.warning("Refusing to write absurdly large message [conn=" + conn +
                         ", size=" + data.length + "].");
             return true;
         }
 
         // expand our output buffer if needed to accomodate this message
         if (data.length > _outbuf.capacity()) {
             // increase the buffer size in large increments
             int ncapacity = Math.max(_outbuf.capacity() << 1, data.length);
             log.info("Expanding output buffer size [nsize=" + ncapacity + "].");
             _outbuf = ByteBuffer.allocateDirect(ncapacity);
         }
 
         boolean fully = true;
         try {
 //             log.info("Writing " + data.length + " byte message to " + conn + ".");
 
             // first copy the data into our "direct" output buffer
             _outbuf.put(data);
             _outbuf.flip();
 
             // then write the data to the socket
             int wrote = conn.getChannel().write(_outbuf);
             noteWrite(1, wrote);
 
             if (_outbuf.remaining() > 0) {
                 fully = false;
 //                 log.info("Partial write [conn=" + conn +
 //                          ", msg=" + StringUtil.shortClassName(outmsg) + ", wrote=" + wrote +
 //                          ", size=" + buffer.limit() + "].");
                 pwh.handlePartialWrite(conn, _outbuf);
 
 //                 } else if (wrote > 10000) {
 //                     log.info("Big write [conn=" + conn +
 //                              ", msg=" + StringUtil.shortClassName(outmsg) +
 //                              ", wrote=" + wrote + "].");
             }
 
         } catch (IOException ioe) {
             // instruct the connection to deal with its failure
             conn.handleFailure(ioe);
 
         } finally {
             _outbuf.clear();
         }
 
         return fully;
     }
 
     /**
      * Sends a datagram to the specified connection.
      *
      * @return true if the datagram was sent, false if we failed to send for any reason.
      */
     protected boolean writeDatagram (Connection conn, byte[] data)
     {
         InetSocketAddress target = conn.getDatagramAddress();
         if (target == null) {
             log.warning("No address to send datagram [conn=" + conn + "].");
             return false;
         }
 
         _databuf.clear();
         _databuf.put(data).flip();
         try {
             return _datagramChannel.send(_databuf, target) > 0;
         } catch (IOException ioe) {
             log.warning("Failed to send datagram.", ioe);
             return false;
         }
     }
 
     /** Called by {@link #writeMessage} and friends when they write data over the network. */
     protected final synchronized void noteWrite (int msgs, int bytes)
     {
         _msgsOut += msgs;
         _bytesOut += bytes;
         _stats.msgsOut += msgs;
         _stats.bytesOut += bytes;
     }
 
     @Override
     protected void handleIterateFailure (Exception e)
     {
         // log the exception
         log.warning("ConnectionManager.iterate() uncaught exception.", e);
     }
 
     @Override
     protected void didShutdown ()
     {
         // take one last crack at the outgoing message queue
         sendOutgoingMessages(System.currentTimeMillis());
 
         // unbind our listening socket
         // Note: because we wait for the object manager to exit before we do, we will still be
         // accepting connections as long as there are events pending.
         // TODO: consider shutting down the listen socker earlier, like in the shutdown method
         try {
             _ssocket.socket().close();
         } catch (IOException ioe) {
             log.warning("Failed to close listening socket.", ioe);
         }
 
         // and the datagram socket, if any
         if (_datagramChannel != null) {
             _datagramChannel.socket().close();
         }
 
         // report if there's anything left on the outgoing message queue
         if (_outq.size() > 0) {
             log.warning("Connection Manager failed to deliver " + _outq.size() + " message(s).");
         }
 
         // run our on-exit handler if we have one
         Runnable onExit = _onExit;
         if (onExit != null) {
             log.info("Connection Manager thread exited (running onExit).");
             onExit.run();
         } else {
             log.info("Connection Manager thread exited.");
         }
     }
 
     /**
      * Called by our net event handler when a new connection is ready to be accepted on our
      * listening socket.
      */
     protected void acceptConnection (ServerSocketChannel listener)
     {
         SocketChannel channel = null;
 
         try {
             channel = listener.accept();
             if (channel == null) {
                 // in theory this shouldn't happen because we got an ACCEPT_READY event, but better
                 // safe than sorry
                 log.info("Psych! Got ACCEPT_READY, but no connection.");
                 return;
             }
 
 //             log.debug("Accepted connection " + channel + ".");
 
             // create a new authing connection object to manage the authentication of this client
             // connection and register it with our selection set
             SelectableChannel selchan = channel;
             selchan.configureBlocking(false);
             SelectionKey selkey = selchan.register(_selector, SelectionKey.OP_READ);
             _handlers.put(selkey, new AuthingConnection(this, selkey, channel));
             synchronized (this) {
                 _stats.connects++;
             }
             return;
 
         } catch (IOException ioe) {
             // no need to complain this happens in the normal course of events
 //             log.warning("Failure accepting new connection.", ioe);
         }
 
         // make sure we don't leak a socket if something went awry
         if (channel != null) {
             try {
                 channel.socket().close();
             } catch (IOException ioe) {
                 log.warning("Failed closing aborted connection: " + ioe);
             }
         }
     }
 
     /**
      * Called by our net event handler when a datagram is ready to be read from the channel.
      *
      * @return the size of the datagram.
      */
     protected int readDatagram (DatagramChannel listener, long when)
     {
         InetSocketAddress source;
         _databuf.clear();
         try {
             source = (InetSocketAddress)listener.receive(_databuf);
         } catch (IOException ioe) {
             log.warning("Failure receiving datagram.", ioe);
             return 0;
         }
 
         // make sure we actually read a packet
         if (source == null) {
             log.info("Psych! Got READ_READY, but no datagram.");
             return 0;
         }
 
         // flip the buffer and record the size (which must be at least 14 to contain the connection
         // id, authentication hash, and a class reference)
         int size = _databuf.flip().remaining();
         if (size < 14) {
             log.warning("Received undersized datagram [source=" + source +
                 ", size=" + size + "].");
             return 0;
         }
 
         // the first four bytes are the connection id
         int connectionId = _databuf.getInt();
         Connection conn = _connections.get(connectionId);
         if (conn != null) {
             conn.handleDatagram(source, _databuf, when);
         } else {
             log.warning("Received datagram for unknown connection [id=" + connectionId +
                 ", source=" + source + "].");
         }
 
         // return the size of the datagram
         return size;
     }
 
     /**
      * Called by a connection when it has a downstream message that needs to be delivered.
      * <em>Note:</em> this method is called as a result of a call to {@link Connection#postMessage}
      * which happens when forwarding an event to a client and at the completion of authentication,
      * both of which <em>must</em> happen only on the distributed object thread.
      */
     void postMessage (Connection conn, DownstreamMessage msg)
     {
         if (!isRunning()) {
            log.warning("Posting message to inactive connection manager", new Exception());
         }
 
         // sanity check
         if (conn == null || msg == null) {
             log.warning("postMessage() bogosity", "conn", conn, "msg", msg, new Exception());
             return;
         }
 
         // more sanity check; messages must only be posted from the dobjmgr thread
         if (!_omgr.isDispatchThread()) {
             log.warning("Message posted on non-distributed object thread", "conn", conn,
                         "msg", msg, "thread", Thread.currentThread(), new Exception());
             // let it through though as we don't want to break things unnecessarily
         }
 
         try {
             // send it as a datagram if hinted and possible
             if (!msg.getTransport().isReliable() && conn.getDatagramAddress() != null) {
                 postDatagram(conn, msg);
                 return;
             }
 
             _framer.resetFrame();
 
             // flatten this message using the connection's output stream
             ObjectOutputStream oout = conn.getObjectOutputStream(_framer);
             oout.writeObject(msg);
             oout.flush();
 
             // now extract that data into a byte array
             ByteBuffer buffer = _framer.frameAndReturnBuffer();
             byte[] data = new byte[buffer.limit()];
             buffer.get(data);
 
 //             log.info("Flattened " + msg + " into " + data.length + " bytes.");
 
             // and slap both on the queue
             _outq.append(new Tuple<Connection, byte[]>(conn, data));
 
         } catch (Exception e) {
             log.warning("Failure flattening message [conn=" + conn +
                     ", msg=" + msg + "].", e);
         }
     }
 
     /**
      * Helper function for {@link #postMessage}; handles posting the message as a datagram.
      */
     void postDatagram (Connection conn, DownstreamMessage msg)
         throws Exception
     {
         _flattener.reset();
 
         // flatten the message using the connection's sequencer
         DatagramSequencer sequencer = conn.getDatagramSequencer();
         sequencer.writeDatagram(msg);
 
         // extract as a byte array
         byte[] data = _flattener.toByteArray();
 
         // slap it on the queue
         _dataq.append(new Tuple<Connection, byte[]>(conn, data));
     }
 
     /**
      * Called by a connection if it experiences a network failure.
      */
     void connectionFailed (Connection conn, IOException ioe)
     {
         // remove this connection from our mappings (it is automatically removed from the Selector
         // when the socket is closed)
         _handlers.remove(conn.getSelectionKey());
         _connections.remove(conn.getConnectionId());
         _oflowqs.remove(conn);
         synchronized (this) {
             _stats.disconnects++;
         }
 
         // let our observers know what's up
         notifyObservers(CONNECTION_FAILED, conn, ioe, null);
     }
 
     /**
      * Called by a connection when it discovers that it's closed.
      */
     void connectionClosed (Connection conn)
     {
         // remove this connection from our mappings (it is automatically removed from the Selector
         // when the socket is closed)
         _handlers.remove(conn.getSelectionKey());
         _connections.remove(conn.getConnectionId());
         _oflowqs.remove(conn);
 
         // let our observers know what's up
         notifyObservers(CONNECTION_CLOSED, conn, null, null);
     }
 
     /** Used to handle partial writes in {@link ConnectionManager#writeMessage}. */
     protected static interface PartialWriteHandler
     {
         void handlePartialWrite (Connection conn, ByteBuffer buffer);
     }
 
     /**
      * Used to handle messages for a client whose network buffer has filled up because their
      * outgoing network buffer has filled up. This can happen if the client receives many messages
      * in rapid succession or if they receive very large messages or if they become unresponsive
      * and stop acknowledging network packets sent by the server. We want to accomodate the first
      * to circumstances and recognize the third as quickly as possible so that we can disconnect
      * the client and propagate that information up to the higher levels so that further messages
      * are not queued up for the unresponsive client.
      */
     protected class OverflowQueue extends ArrayList<byte[]>
         implements PartialWriteHandler
     {
         /** The connection for which we're managing overflow. */
         public Connection conn;
 
         /**
          * Creates a new overflow queue for the supplied connection and with the supplied initial
          * partial message.
          */
         public OverflowQueue (Connection conn, ByteBuffer message)
         {
             this.conn = conn;
             // set up our initial _partial buffer
             handlePartialWrite(conn, message);
         }
 
         /**
          * Called each time through the {@link ConnectionManager#iterate} loop, this attempts to
          * send any remaining partial message and all subsequent messages in the overflow queue.
          *
          * @return true if all messages in this queue were successfully sent, false if there
          * remains data to be sent on the next loop.
          *
          * @throws IOException if an error occurs writing data to the connection or if we have been
          * unable to write any data to the connection for ten seconds.
          */
         public boolean writeOverflowMessages (long iterStamp)
             throws IOException
         {
             // write any partial message if we have one
             if (_partial != null) {
                 // write all we can of our partial buffer
                 int wrote = conn.getChannel().write(_partial);
                 noteWrite(0, wrote);
 
                 if (_partial.remaining() == 0) {
                     _partial = null;
                     _partials++;
                 } else {
 //                     log.info("Still going [conn=" + conn + ", wrote=" + wrote +
 //                              ", remain=" + _partial.remaining() + "].");
                     return false;
                 }
             }
 
             while (size() > 0) {
                 byte[] data = remove(0);
                 // if any of these messages are partially written, we have to stop and wait for the
                 // next tick
                 _msgs++;
                 if (!writeMessage(conn, data, this)) {
                     return false;
                 }
             }
 
             return true;
         }
 
         // documentation inherited
         public void handlePartialWrite (Connection wconn, ByteBuffer buffer)
         {
             // set up our _partial buffer
             _partial = ByteBuffer.allocateDirect(buffer.remaining());
             _partial.put(buffer);
             _partial.flip();
         }
 
         @Override
         public String toString ()
         {
             return "[conn=" + conn + ", partials=" + _partials + ", msgs=" + _msgs + "]";
         }
 
         /** The remains of a message that was only partially written on its first attempt. */
         protected ByteBuffer _partial;
 
         /** A couple of counters. */
         protected int _msgs, _partials;
     }
 
     /** Used to create an overflow queue on the first partial write. */
     protected PartialWriteHandler _oflowHandler = new PartialWriteHandler() {
         public void handlePartialWrite (Connection conn, ByteBuffer msgbuf) {
             // if we couldn't write all the data for this message, we'll need to establish an
             // overflow queue
             _oflowqs.put(conn, new OverflowQueue(conn, msgbuf));
         }
     };
 
     /** Handles client authentication. The base authenticator is injected but optional services
      * like the PeerManager may replace this authenticator with one that intercepts certain types
      * of authentication and then passes normal authentications through. */
     @Inject(optional=true) protected Authenticator _author = new DummyAuthenticator();
 
     protected int[] _ports, _datagramPorts;
     protected Selector _selector;
     protected ServerSocketChannel _ssocket;
     protected DatagramChannel _datagramChannel;
     protected ResultListener<Object> _startlist;
 
     /** Counts consecutive runtime errors in select(). */
     protected int _runtimeExceptionCount;
 
     /** Maps selection keys to network event handlers. */
     protected Map<SelectionKey, NetEventHandler> _handlers = Maps.newHashMap();
 
     /** Connections mapped by identifier. */
     protected IntMap<Connection> _connections = IntMaps.newHashIntMap();
 
     protected Queue<Connection> _deathq = new Queue<Connection>();
     protected Queue<AuthingConnection> _authq = new Queue<AuthingConnection>();
 
     protected Queue<Tuple<Connection, byte[]>> _outq = new Queue<Tuple<Connection, byte[]>>();
     protected Queue<Tuple<Connection, byte[]>> _dataq = new Queue<Tuple<Connection, byte[]>>();
     protected FramingOutputStream _framer;
     protected ByteArrayOutputStream _flattener;
     protected ByteBuffer _outbuf = ByteBuffer.allocateDirect(64 * 1024);
     protected ByteBuffer _databuf = ByteBuffer.allocateDirect(Client.MAX_DATAGRAM_SIZE);
 
     protected Map<Connection, OverflowQueue> _oflowqs = Maps.newHashMap();
 
     protected List<ConnectionObserver> _observers = Lists.newArrayList();
 
     /** Bytes in and out in the last reporting period. */
     protected long _bytesIn, _bytesOut;
 
     /** Messages read and written in the last reporting period. */
     protected int _msgsIn, _msgsOut;
 
     /** Our current runtime stats. */
     protected ConMgrStats _stats;
 
     /** A snapshot of our runtime stats as of our last report. */
     protected ConMgrStats _lastStats;
 
     /** A runnable to execute when the connection manager thread exits. */
     protected volatile Runnable _onExit;
 
     /** The invoker on which we do our authenticating. */
     @Inject @AuthInvoker protected Invoker _authInvoker;
 
     /** The distributed object manager with which we operate. */
     @Inject protected PresentsDObjectMgr _omgr;
 
     /** How long we wait for network events before checking our running flag to see if we should
      * still be running. We don't want to loop too tightly, but we need to make sure we don't sit
      * around listening for incoming network events too long when there are outgoing messages in
      * the queue. */
     protected static final int SELECT_LOOP_TIME = 100;
 
     // codes for notifyObservers()
     protected static final int CONNECTION_ESTABLISHED = 0;
     protected static final int CONNECTION_FAILED = 1;
     protected static final int CONNECTION_CLOSED = 2;
 }
