 /**
  * Copyright 2011 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 package com.google.bitcoin.core;
 
 import com.google.bitcoin.core.Peer.PeerHandler;
 import com.google.bitcoin.discovery.PeerDiscovery;
 import com.google.bitcoin.discovery.PeerDiscoveryException;
 import com.google.bitcoin.utils.EventListenerInvoker;
 import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
 import com.google.common.util.concurrent.*;
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.channel.*;
 import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.util.*;
 import java.util.concurrent.*;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 
 /**
  * <p>Maintain a number of connections to peers.</p>
  * 
  * <p>PeerGroup tries to maintain a constant number of connections to a set of distinct peers.
  * Each peer runs a network listener in its own thread.  When a connection is lost, a new peer
  * will be tried after a delay as long as the number of connections less than the maximum.</p>
  * 
  * <p>Connections are made to addresses from a provided list.  When that list is exhausted,
  * we start again from the head of the list.</p>
  * 
  * <p>The PeerGroup can broadcast a transaction to the currently connected set of peers.  It can
  * also handle download of the blockchain from peers, restarting the process when peers die.</p>
  *
  * <p>PeerGroup implements the {@link Service} interface. This means before it will do anything,
  * you must call the {@link com.google.common.util.concurrent.Service#start()} method (which returns
  * a future) or {@link com.google.common.util.concurrent.Service#startAndWait()} method, which will block
  * until peer discovery is completed and some outbound connections have been initiated (it will return
  * before handshaking is done, however). You should call {@link com.google.common.util.concurrent.Service#stop()}
  * when finished. Note that not all methods of PeerGroup are safe to call from a UI thread as some may do
  * network IO, but starting and stopping the service should be fine.</p>
  */
 public class PeerGroup extends AbstractIdleService {
     private static final int DEFAULT_CONNECTIONS = 4;
 
     private static final Logger log = LoggerFactory.getLogger(PeerGroup.class);
 
     // These lists are all thread-safe so do not have to be accessed under the PeerGroup lock.
     // Addresses to try to connect to, excluding active peers.
     private List<PeerAddress> inactives;
     // Currently active peers. This is an ordered list rather than a set to make unit tests predictable.
     private List<Peer> peers;
     // Currently connecting peers
     private List<Peer> pendingPeers;
     private Map<Peer, ChannelFuture> channelFutures;
 
     // The peer we are currently downloading the chain from
     private Peer downloadPeer;
     // Callback for events related to chain download
     private PeerEventListener downloadListener;
     // Callbacks for events related to peer connection/disconnection
     private List<PeerEventListener> peerEventListeners;
     // Peer discovery sources, will be polled occasionally if there aren't enough inactives.
     private Set<PeerDiscovery> peerDiscoverers;
     // The version message to use for new connections.
     private VersionMessage versionMessage;
     // A class that tracks recent transactions that have been broadcast across the network, counts how many
     // peers announced them and updates the transaction confidence data. It is passed to each Peer.
     private final MemoryPool memoryPool;
     // How many connections we want to have open at the current time. If we lose connections, we'll try opening more
     // until we reach this count.
     private int maxConnections;
 
     // Runs a background thread that we use for scheduling pings to our peers, so we can measure their performance
     // and network latency. We ping peers every pingIntervalMsec milliseconds.
     private Timer pingTimer;
     /** How many milliseconds to wait after receiving a pong before sending another ping. */
     public static final long DEFAULT_PING_INTERVAL_MSEC = 5000;
     private long pingIntervalMsec = DEFAULT_PING_INTERVAL_MSEC;
 
     private final NetworkParameters params;
     private final AbstractBlockChain chain;
     private long fastCatchupTimeSecs;
     private ArrayList<Wallet> wallets;
 
     private AbstractPeerEventListener getDataListener;
     private ClientBootstrap bootstrap;
     private int minBroadcastConnections = 0;
 
     private class PeerStartupListener implements Peer.PeerLifecycleListener {
         public void onPeerConnected(Peer peer) {
             pendingPeers.remove(peer);
             peers.add(peer);
             handleNewPeer(peer);
         }
 
         public void onPeerDisconnected(Peer peer) {
             pendingPeers.remove(peer);
             peers.remove(peer);
             channelFutures.remove(peer);
             handlePeerDeath(peer);
         }
     }
 
     // Visible for testing
     Peer.PeerLifecycleListener startupListener = new PeerStartupListener();
 
     /**
      * Creates a PeerGroup with the given parameters. No chain is provided so this node will report its chain height
      * as zero to other peers. This constructor is useful if you just want to explore the network but aren't interested
      * in downloading block data.
      *
      * @param params Network parameters
      */
     public PeerGroup(NetworkParameters params) {
         this(params, null);
     }
 
     /**
      * Creates a PeerGroup for the given network and chain. Blocks will be passed to the chain as they are broadcast
      * and downloaded. This is probably the constructor you want to use.
      */
     public PeerGroup(NetworkParameters params, AbstractBlockChain chain) {
         this(params, chain, null);
     }
 
 
     /**
      * <p>Creates a PeerGroup for the given network and chain, using the provided Netty {@link ClientBootstrap} object.
      * </p>
      *
      * <p>A ClientBootstrap creates raw (TCP) connections to other nodes on the network. Normally you won't need to
      * provide one - use the other constructors. Providing your own bootstrap is useful if you want to control
      * details like how many network threads are used, the connection timeout value and so on. To do this, you can
      * use {@link PeerGroup#createClientBootstrap()} method and then customize the resulting object. Example:</p>
      *
      * <pre>
      *   ClientBootstrap bootstrap = PeerGroup.createClientBootstrap();
      *   bootstrap.setOption("connectionTimeoutMillis", 3000);
      *   PeerGroup peerGroup = new PeerGroup(params, chain, bootstrap);
      * </pre>
      *
      * <p>The ClientBootstrap provided does not need a channel pipeline factory set. If one wasn't set, the provided
      * bootstrap will be modified to have one that sets up the pipelines correctly.</p>
      */
     public PeerGroup(NetworkParameters params, AbstractBlockChain chain, ClientBootstrap bootstrap) {
         this.params = params;
         this.chain = chain;  // Can be null.
         this.fastCatchupTimeSecs = params.genesisBlock.getTimeSeconds();
         this.wallets = new ArrayList<Wallet>(1);
 
         // This default sentinel value will be overridden by one of two actions:
         //   - adding a peer discovery source sets it to the default
         //   - using connectTo() will increment it by one
         this.maxConnections = 0;
 
         int height = chain == null ? 0 : chain.getBestChainHeight();
         this.versionMessage = new VersionMessage(params, height);
 
         memoryPool = new MemoryPool();
 
         // Configure Netty. The "ClientBootstrap" creates connections to other nodes. It can be configured in various
         // ways to control the network.
         if (bootstrap == null) {
             this.bootstrap = createClientBootstrap();
             this.bootstrap.setPipelineFactory(makePipelineFactory(params, chain));
         } else {
             this.bootstrap = bootstrap;
         }
 
         inactives = Collections.synchronizedList(new ArrayList<PeerAddress>());
         peers = Collections.synchronizedList(new ArrayList<Peer>());
         pendingPeers = Collections.synchronizedList(new ArrayList<Peer>());
         channelFutures = Collections.synchronizedMap(new HashMap<Peer, ChannelFuture>());
         peerDiscoverers = new CopyOnWriteArraySet<PeerDiscovery>(); 
         peerEventListeners = new ArrayList<PeerEventListener>();
         // This event listener is added to every peer. It's here so when we announce transactions via an "inv", every
         // peer can fetch them.
         getDataListener = new AbstractPeerEventListener() {
             @Override
             public List<Message> getData(Peer peer, GetDataMessage m) {
                 return handleGetData(m);
             }
         };
     }
 
     /**
      * Helper method that just sets up a normal Netty ClientBootstrap using the default options, except for a custom
      * thread factory that gives worker threads useful names and lowers their priority (to avoid competing with UI
      * threads). You don't normally need to call this - if you aren't sure what it does, just use the regular
      * constructors for {@link PeerGroup} that don't take a ClientBootstrap object.
      */
     public static ClientBootstrap createClientBootstrap() {
         ExecutorService bossExecutor = Executors.newCachedThreadPool(new PeerGroupThreadFactory());
         ExecutorService workerExecutor = Executors.newCachedThreadPool(new PeerGroupThreadFactory());
         NioClientSocketChannelFactory channelFactory = new NioClientSocketChannelFactory(bossExecutor, workerExecutor);
         ClientBootstrap bs = new ClientBootstrap(channelFactory);
         bs.setOption("connectionTimeoutMillis", 2000);
         return bs;
     }
 
     // Create a Netty pipeline factory.  The pipeline factory will create a network processing
     // pipeline with the bitcoin serializer ({@code TCPNetworkConnection}) downstream
     // of the higher level {@code Peer}.  Received packets will first be decoded, then passed
     // {@code Peer}.  Sent packets will be created by the {@code Peer}, then encoded and sent.
     private ChannelPipelineFactory makePipelineFactory(final NetworkParameters params, final AbstractBlockChain chain) {
         return new ChannelPipelineFactory() {
             public ChannelPipeline getPipeline() throws Exception {
                 VersionMessage ver = getVersionMessage().duplicate();
                 ver.bestHeight = chain == null ? 0 : chain.getBestChainHeight();
                 ver.time = Utils.now().getTime() / 1000;
 
                 ChannelPipeline p = Channels.pipeline();
 
                 Peer peer = new Peer(params, chain, ver);
                 peer.addLifecycleListener(startupListener);
                 pendingPeers.add(peer);
                 TCPNetworkConnection codec = new TCPNetworkConnection(params, peer.getVersionMessage());
                 p.addLast("codec", codec.getHandler());
                 p.addLast("peer", peer.getHandler());
                 return p;
             }
         };
     }
 
     /**
      * Adjusts the desired number of connections that we will create to peers. Note that if there are already peers
      * open and the new value is lower than the current number of peers, those connections will be terminated. Likewise
      * if there aren't enough current connections to meet the new requested max size, some will be added.
      */
     public void setMaxConnections(int maxConnections) {
         int adjustment;
         synchronized (this) {
             this.maxConnections = maxConnections;
             if (!isRunning()) return;
         }
         // We may now have too many or too few open connections. Adding the sizes together here is a race condition.
         adjustment = maxConnections - (peers.size() + pendingPeers.size());
         while (adjustment > 0) {
             try {
                 connectToAnyPeer();
             } catch (PeerDiscoveryException e) {
                 throw new RuntimeException(e);
             }
             adjustment--;
         }
         while (adjustment < 0) {
             Channel channel;
             synchronized (peers) {
                 channel = channelFutures.get(peers.remove(peers.size() - 1)).getChannel();
             }
             channel.close();
             adjustment++;
         }
     }
 
     /** The maximum number of connections that we will create to peers. */
     public synchronized int getMaxConnections() {
         return maxConnections;
     }
 
     private synchronized List<Message> handleGetData(GetDataMessage m) {
         // Scans the wallets and memory pool for transactions in the getdata message and returns them.
         // Runs on peer threads.
         LinkedList<Message> transactions = new LinkedList<Message>();
         LinkedList<InventoryItem> items = new LinkedList<InventoryItem>(m.getItems());
         Iterator<InventoryItem> it = items.iterator();
         while (it.hasNext()) {
             InventoryItem item = it.next();
             // Check the mempool first.
             Transaction tx = memoryPool.get(item.hash);
             if (tx != null) {
                 transactions.add(tx);
                 it.remove();
             } else {
                 // Check the wallets.
                 for (Wallet w : wallets) {
                     synchronized (w) {
                         tx = w.getTransaction(item.hash);
                         if (tx == null) continue;
                         transactions.add(tx);
                         it.remove();
                         break;
                     }
                 }
             }
         }
         return transactions;
     }
 
     /**
      * Sets the {@link VersionMessage} that will be announced on newly created connections. A version message is
      * primarily interesting because it lets you customize the "subVer" field which is used a bit like the User-Agent
      * field from HTTP. It means your client tells the other side what it is, see
      * <a href="https://en.bitcoin.it/wiki/BIP_0014">BIP 14</a>.
      *
      * The VersionMessage you provide is copied and the best chain height/time filled in for each new connection,
      * therefore you don't have to worry about setting that. The provided object is really more of a template.
      */
     public synchronized void setVersionMessage(VersionMessage ver) {
         versionMessage = ver;
     }
 
     /**
      * Returns the version message provided by setVersionMessage or a default if none was given.
      */
     public synchronized VersionMessage getVersionMessage() {
         return versionMessage;
     }
 
     /**
      * Sets information that identifies this software to remote nodes. This is a convenience wrapper for creating 
      * a new {@link VersionMessage}, calling {@link VersionMessage#appendToSubVer(String, String, String)} on it,
      * and then calling {@link PeerGroup#setVersionMessage(VersionMessage)} on the result of that. See the docs for
      * {@link VersionMessage#appendToSubVer(String, String, String)} for information on what the fields should contain.
      *
      * @param name
      * @param version
      */
     public void setUserAgent(String name, String version, String comments) {
         VersionMessage ver = new VersionMessage(params, 0);
         ver.appendToSubVer(name, version, comments);
         setVersionMessage(ver);
     }
 
     /**
      * Sets information that identifies this software to remote nodes. This is a convenience wrapper for creating
      * a new {@link VersionMessage}, calling {@link VersionMessage#appendToSubVer(String, String, String)} on it,
      * and then calling {@link PeerGroup#setVersionMessage(VersionMessage)} on the result of that. See the docs for
      * {@link VersionMessage#appendToSubVer(String, String, String)} for information on what the fields should contain.
      *
      * @param name
      * @param version
      */
     public void setUserAgent(String name, String version) {
         setUserAgent(name, version, null);
     }
 
 
     /**
      * <p>Adds a listener that will be notified on a library controlled thread when:</p>
      * <ol>
      *     <li>New peers are connected to.</li>
      *     <li>Peers are disconnected from.</li>
      *     <li>A message is received by the download peer (there is always one peer which is elected as a peer which
      *     will be used to retrieve data).
      *     <li>Blocks are downloaded by the download peer.</li>
      *     </li>
      * </ol>
      * <p>The listener will be locked during callback execution, which in turn will cause network message processing
      * to stop until the listener returns.</p>
      */
     public synchronized void addEventListener(PeerEventListener listener) {
         peerEventListeners.add(checkNotNull(listener));
     }
 
     /** The given event listener will no longer be called with events. */
     public synchronized boolean removeEventListener(PeerEventListener listener) {
         return peerEventListeners.remove(checkNotNull(listener));
     }
 
     /**
      * Returns a newly allocated list containing the currently connected peers. If all you care about is the count,
      * use numConnectedPeers().
      */
     public List<Peer> getConnectedPeers() {
         synchronized (peers) {
             ArrayList<Peer> result = new ArrayList<Peer>(peers.size());
             result.addAll(peers);
             return result;
         }
     }
 
     /**
      * Add an address to the list of potential peers to connect to. It won't necessarily be used unless there's a need
      * to build new connections to reach the max connection count.
      *
      * @param peerAddress IP/port to use.
      */
     public void addAddress(PeerAddress peerAddress) {
         inactives.add(peerAddress);
     }
 
     /**
      * Add addresses from a discovery source to the list of potential peers to connect to. If max connections has not
      * been configured, or set to zero, then it's set to the default at this point.
      */
     public synchronized void addPeerDiscovery(PeerDiscovery peerDiscovery) {
         if (getMaxConnections() == 0)
             setMaxConnections(DEFAULT_CONNECTIONS);
         peerDiscoverers.add(peerDiscovery);
     }
 
     protected void discoverPeers() throws PeerDiscoveryException {
         long start = System.currentTimeMillis();
        Set<PeerAddress> addressSet = Sets.newHashSet();
         for (PeerDiscovery peerDiscovery : peerDiscoverers) {
             InetSocketAddress[] addresses;
             addresses = peerDiscovery.getPeers(10, TimeUnit.SECONDS);
            for (int i = 0; i < addresses.length; i++)
                addressSet.add(new PeerAddress(addresses[i]));
            if (addressSet.size() > 0) break;
        }
        synchronized (inactives) {
            inactives.addAll(addressSet);
         }
         log.info("Peer discovery took {}msec", System.currentTimeMillis() - start);
     }
 
     /** Picks a peer from discovery and connects to it. If connection fails, picks another and tries again. */
     protected void connectToAnyPeer() throws PeerDiscoveryException {
         // Do not call this method whilst synchronized on the PeerGroup lock.
         final PeerAddress addr;
         if (!(state() == State.STARTING || state() == State.RUNNING)) return;
         synchronized (inactives) {
             if (inactives.size() == 0) {
                 discoverPeers();
             }
             if (inactives.size() == 0) {
                 log.debug("Peer discovery didn't provide us any more peers, not trying to build new connection.");
                 return;
             }
             addr = inactives.remove(inactives.size() - 1);
         }
         // Don't do connectTo whilst holding the PeerGroup lock because this can trigger some amazingly deep stacks
         // and potentially circular deadlock in the case of immediate failure (eg, attempt to access IPv6 node from
         // a non-v6 capable machine). It doesn't relay control immediately to the netty boss thread as you may expect.
         //
         // This method eventually constructs a Peer and puts it into pendingPeers. If the connection fails to establish,
         // handlePeerDeath will be called, which will potentially call this method again to replace the dead or failed
         // connection.
         connectTo(addr.toSocketAddress(), false);
     }
 
     @Override
     protected void startUp() throws Exception {
         // This is run in a background thread by the AbstractIdleService implementation.
         synchronized (this) {
             pingTimer = new Timer("Peer pinging thread", true);
         }
         // Bring up the requested number of connections. If a connect attempt fails,
         // new peers will be tried until there is a success, so just calling connectToAnyPeer for the wanted number
         // of peers is sufficient.
         for (int i = 0; i < getMaxConnections(); i++) {
             try {
                 connectToAnyPeer();
             } catch (PeerDiscoveryException e) {
                 if (e.getCause() instanceof InterruptedException) return;
                 log.error(e.getMessage());
             }
         }
     }
 
     @Override
     protected void shutDown() throws Exception {
         // This is run on a separate thread by the AbstractIdleService implementation.
         synchronized (this) {
             pingTimer.cancel();
         }
         // TODO: Make this shutdown process use a ChannelGroup.
         LinkedList<ChannelFuture> futures;
         synchronized (channelFutures) {
             // Copy the list here because the act of closing the channel modifies the channelFutures map.
             futures = new LinkedList<ChannelFuture>(channelFutures.values());
         }
         for (ChannelFuture future : futures) {
             future.getChannel().close();
         }
         bootstrap.releaseExternalResources();
         for (PeerDiscovery peerDiscovery : peerDiscoverers) {
             peerDiscovery.shutdown();
         }
     }
 
     /**
      * <p>Link the given wallet to this PeerGroup. This is used for three purposes:</p>
      * <ol>
      *   <li>So the wallet receives broadcast transactions.</li>
      *   <li>Announcing pending transactions that didn't get into the chain yet to our peers.</li>
      *   <li>Set the fast catchup time using {@link PeerGroup#setFastCatchupTimeSecs(long)}, to optimize chain
      *       download.</li>
      * </ol>
      * <p>Note that this should be done before chain download commences because if you add a wallet with keys earlier
      * than the current chain head, the relevant parts of the chain won't be redownloaded for you.</p>
      */
     public synchronized void addWallet(Wallet wallet) {
         Preconditions.checkNotNull(wallet);
         wallets.add(wallet);
         addEventListener(wallet.getPeerEventListener());
         announcePendingWalletTransactions(Collections.singletonList(wallet), peers);
 
         // Don't bother downloading block bodies before the oldest keys in all our wallets. Make sure we recalculate
         // if a key is added. Of course, by then we may have downloaded the chain already. Ideally adding keys would
         // automatically rewind the block chain and redownload the blocks to find transactions relevant to those keys,
         // all transparently and in the background. But we are a long way from that yet.
         wallet.addEventListener(new AbstractWalletEventListener() {
             @Override
             public void onKeyAdded(ECKey key) {
                 recalculateFastCatchupTime();
             }
         });
         recalculateFastCatchupTime();
     }
 
     private synchronized void recalculateFastCatchupTime() {
         // Fully verifying mode doesn't use this optimization (it can't as it needs to see all transactions).
         if (chain.shouldVerifyTransactions()) return;
         long earliestKeyTime = Long.MAX_VALUE;
         for (Wallet w : wallets) {
             earliestKeyTime = Math.min(earliestKeyTime, w.getEarliestKeyCreationTime());
         }
         setFastCatchupTimeSecs(earliestKeyTime);
     }
 
     /**
      * Unlinks the given wallet so it no longer receives broadcast transactions or has its transactions announced.
      */
     public void removeWallet(Wallet wallet) {
         if (wallet == null)
             throw new IllegalArgumentException("wallet is null");
         wallets.remove(wallet);
         removeEventListener(wallet.getPeerEventListener());
     }
 
     /**
      * Returns the number of currently connected peers. To be informed when this count changes, register a 
      * {@link PeerEventListener} and use the onPeerConnected/onPeerDisconnected methods.
      */
     public int numConnectedPeers() {
         return peers.size();
     }
 
     /**
      * Connect to a peer by creating a Netty channel to the destination address.
      * 
      * @param address destination IP and port.
      * @return a ChannelFuture that can be used to wait for the socket to connect.  A socket
      *           connection does not mean that protocol handshake has occured.
      */
     public ChannelFuture connectTo(SocketAddress address) {
         return connectTo(address, true);
     }
 
     // Internal version. Do not call whilst holding the PeerGroup lock.
     protected ChannelFuture connectTo(SocketAddress address, boolean incrementMaxConnections) {
         ChannelFuture future = bootstrap.connect(address);
         // When the channel has connected and version negotiated successfully, handleNewPeer will end up being called on
         // a worker thread.
 
         // Set up the address on the TCPNetworkConnection handler object.
         // TODO: This is stupid and racy, get rid of it.
         TCPNetworkConnection.NetworkHandler networkHandler =
                 (TCPNetworkConnection.NetworkHandler) future.getChannel().getPipeline().get("codec");
         if (networkHandler != null) {
             // This can be null in unit tests or apps that don't use TCP connections.
             networkHandler.getOwnerObject().setRemoteAddress(address);
         }
         synchronized (this) {
             Peer peer = peerFromChannelFuture(future);
             channelFutures.put(peer, future);
             if (incrementMaxConnections) {
                 // We don't use setMaxConnections here as that would trigger a recursive attempt to establish a new
                 // outbound connection.
                 maxConnections++;
             }
         }
         return future;
     }
 
     static public Peer peerFromChannelFuture(ChannelFuture future) {
         return peerFromChannel(future.getChannel());
     }
 
     static public Peer peerFromChannel(Channel channel) {
         return ((PeerHandler)channel.getPipeline().get("peer")).getPeer();
     }
 
     /**
      * Start downloading the blockchain from the first available peer.
      * <p/>
      * <p>If no peers are currently connected, the download will be started
      * once a peer starts.  If the peer dies, the download will resume with another peer.
      *
      * @param listener a listener for chain download events, may not be null
      */
     public synchronized void startBlockChainDownload(PeerEventListener listener) {
         this.downloadListener = listener;
         // TODO: be more nuanced about which peer to download from.  We can also try
         // downloading from multiple peers and handle the case when a new peer comes along
         // with a longer chain after we thought we were done.
         synchronized (peers) {
             if (!peers.isEmpty()) {
                 startBlockChainDownloadFromPeer(peers.iterator().next());
             }
         }
     }
 
     /**
      * Download the blockchain from peers. Convenience that uses a {@link DownloadListener} for you.<p>
      * 
      * This method waits until the download is complete.  "Complete" is defined as downloading
      * from at least one peer all the blocks that are in that peer's inventory.
      */
     public void downloadBlockChain() {
         DownloadListener listener = new DownloadListener();
         startBlockChainDownload(listener);
         try {
             listener.await();
         } catch (InterruptedException e) {
             throw new RuntimeException(e);
         }
     }
 
     protected synchronized void handleNewPeer(final Peer peer) {
         // Runs on a netty worker thread for every peer that is newly connected.
         log.info("{}: New peer", peer);
         // Link the peer to the memory pool so broadcast transactions have their confidence levels updated.
         peer.setMemoryPool(memoryPool);
         // If we want to download the chain, and we aren't currently doing so, do so now.
         // TODO: Change this so we automatically switch the download peer based on ping times.
         if (downloadListener != null && downloadPeer == null) {
             log.info("  starting block chain download");
             startBlockChainDownloadFromPeer(peer);
         } else if (downloadPeer == null) {
             setDownloadPeer(peer);
         } else {
             peer.setDownloadData(false);
         }
         // Make sure the peer knows how to upload transactions that are requested from us.
         peer.addEventListener(getDataListener);
         // Now tell the peers about any transactions we have which didn't appear in the chain yet. These are not
         // necessarily spends we created. They may also be transactions broadcast across the network that we saw,
         // which are relevant to us, and which we therefore wish to help propagate (ie they send us coins).
         //
         // Note that this can cause a DoS attack against us if a malicious remote peer knows what keys we own, and
         // then sends us fake relevant transactions. We'll attempt to relay the bad transactions, our badness score
         // in the Satoshi client will increase and we'll get disconnected.
         //
         // TODO: Find a way to balance the desire to propagate useful transactions against obscure DoS attacks.
         announcePendingWalletTransactions(wallets, Collections.singletonList(peer));
         // And set up event listeners for clients. This will allow them to find out about new transactions and blocks.
         for (PeerEventListener listener : peerEventListeners) {
             peer.addEventListener(listener);
         }
         setupPingingForNewPeer(peer);
         EventListenerInvoker.invoke(peerEventListeners, new EventListenerInvoker<PeerEventListener>() {
             @Override
             public void invoke(PeerEventListener listener) {
                 listener.onPeerConnected(peer, peers.size());
             }
         });
     }
 
     private void setupPingingForNewPeer(final Peer peer) {
         if (peer.getPeerVersionMessage().clientVersion < Pong.MIN_PROTOCOL_VERSION)
             return;
         if (getPingIntervalMsec() <= 0)
             return;  // Disabled.
         // Start the process of pinging the peer. Do a ping right now and then ensure there's a fixed delay between
         // each ping. If the peer is taken out of the peers list then the cycle will stop.
         new Runnable() {
             private boolean firstRun = true;
             public void run() {
                 // Ensure that the first ping happens immediately and later pings after the requested delay.
                 if (firstRun) {
                     firstRun = false;
                     try {
                         peer.ping().addListener(this, MoreExecutors.sameThreadExecutor());
                     } catch (Exception e) {
                         log.warn("{}: Exception whilst trying to ping peer: {}", peer, e.toString());
                         return;
                     }
                     return;
                 }
 
                 final long interval = getPingIntervalMsec();
                 if (interval <= 0)
                     return;  // Disabled.
                 pingTimer.schedule(new TimerTask() {
                     @Override
                     public void run() {
                         try {
                             if (!peers.contains(peer))
                                 return;  // Peer was removed/shut down.
                             peer.ping().addListener(this, MoreExecutors.sameThreadExecutor());
                         } catch (Exception e) {
                             log.warn("{}: Exception whilst trying to ping peer: {}", peer, e.toString());
                         }
                     }
                 }, interval);
             }
         }.run();
     }
 
     /** Returns true if at least one peer received an inv. */
     private synchronized boolean announcePendingWalletTransactions(List<Wallet> announceWallets,
                                                                    List<Peer> announceToPeers) {
         // Build up an inv announcing the hashes of all pending transactions in all our wallets.
         InventoryMessage inv = new InventoryMessage(params);
         for (Wallet w : announceWallets) {
             for (Transaction tx : w.getPendingTransactions()) {
                 inv.addTransaction(tx);
             }
         }
         // Don't send empty inv messages.
         if (inv.getItems().size() == 0) {
             return true;
         }
         boolean success = false;
         for (Peer p : announceToPeers) {
             try {
                 log.info("{}: Announcing {} pending wallet transactions", p.getAddress(), inv.getItems().size());
                 p.sendMessage(inv);
                 success = true;
             } catch (IOException e) {
                 log.warn("Failed to announce 'inv' to peer: {}", p);
             }
         }
         return success;
     }
 
     private synchronized void setDownloadPeer(Peer peer) {
         if (chain == null)
             return;
         if (downloadPeer != null) {
             log.info("Unsetting download peer: {}", downloadPeer);
             downloadPeer.setDownloadData(false);
         }
         downloadPeer = peer;
         if (downloadPeer != null) {
             log.info("Setting download peer: {}", downloadPeer);
             downloadPeer.setDownloadData(true);
             if (chain != null)
                 downloadPeer.setFastCatchupTime(fastCatchupTimeSecs);
         }
     }
 
     /**
      * Returns the {@link MemoryPool} created by this peer group to synchronize its peers. The pool tracks advertised
      * and downloaded transactions so their confidence can be measured as a proportion of how many peers announced it.
      * With an un-tampered with internet connection, the more peers announce a transaction the more confidence you can
      * have that it's really valid.
      */
     public MemoryPool getMemoryPool() {
         // Locking unneeded as memoryPool is final.
         return memoryPool;
     }
 
     /**
      * Tells the PeerGroup to download only block headers before a certain time and bodies after that. See
      * {@link Peer#setFastCatchupTime(long)} for further explanation. Call this before starting block chain download.
      */
     public synchronized void setFastCatchupTimeSecs(long secondsSinceEpoch) {
         Preconditions.checkState(!chain.shouldVerifyTransactions(), "Fast catchup is incompatible with fully verifying");
         fastCatchupTimeSecs = secondsSinceEpoch;
         if (downloadPeer != null) {
             downloadPeer.setFastCatchupTime(secondsSinceEpoch);
         }
     }
 
     /**
      * Returns the current fast catchup time. The contents of blocks before this time won't be downloaded as they
      * cannot contain any interesting transactions. If you use {@link PeerGroup#addWallet(Wallet)} this just returns
      * the min of the wallets earliest key times.
      * @return a time in seconds since the epoch
      */
     public synchronized long getFastCatchupTimeSecs() {
         return fastCatchupTimeSecs;
     }
 
     protected void handlePeerDeath(final Peer peer) {
         // This can run on any Netty worker thread. Because connectToAnyPeer() must run unlocked to avoid circular
         // deadlock, this method must run largely unlocked too. Some members are thread-safe and others aren't, so
         // we synchronize only the parts that need it.
         if (!isRunning()) return;
         checkArgument(!peers.contains(peer));
         final Peer downloadPeer;
         final PeerEventListener downloadListener;
         synchronized (this) {
             downloadPeer = this.downloadPeer;
             downloadListener = this.downloadListener;
         }
         if (peer == downloadPeer) {
             log.info("Download peer died. Picking a new one.");
             setDownloadPeer(null);
             // Pick a new one and possibly tell it to download the chain.
             synchronized (peers) {
                 if (!peers.isEmpty()) {
                     Peer next = peers.get(0);
                     setDownloadPeer(next);
                     if (downloadListener != null) {
                         startBlockChainDownloadFromPeer(next);
                     }
                 }
             }
         }
         // Replace this peer with a new one to keep our connection count up, if necessary.
         // The calculation is a little racy.
         if (peers.size() + pendingPeers.size() < getMaxConnections()) {
             try {
                 connectToAnyPeer();
             } catch (PeerDiscoveryException e) {
                 log.error(e.getMessage());
             }
         }
         // TODO: Remove peerEventListeners from the Peer here.
         peer.removeEventListener(getDataListener);
         EventListenerInvoker.invoke(peerEventListeners, new EventListenerInvoker<PeerEventListener>() {
             @Override
             public void invoke(PeerEventListener listener) {
                 listener.onPeerDisconnected(peer, peers.size());
             }
         });
     }
 
     private synchronized void startBlockChainDownloadFromPeer(Peer peer) {
         try {
             peer.addEventListener(downloadListener);
             setDownloadPeer(peer);
             // startBlockChainDownload will setDownloadData(true) on itself automatically.
             peer.startBlockChainDownload();
         } catch (IOException e) {
             log.error("failed to start block chain download from " + peer, e);
             return;
         }
     }
 
     /**
      * Returns a future that is triggered when the number of connected peers is equal to the given number of connected
      * peers. By using this with {@link com.google.bitcoin.core.PeerGroup#getMaxConnections()} you can wait until the
      * network is fully online. To block immediately, just call get() on the result.
      *
      * @param numPeers How many peers to wait for.
      * @return a future that will be triggered when the number of connected peers >= numPeers
      */
     public ListenableFuture<PeerGroup> waitForPeers(final int numPeers) {
         if (peers.size() >= numPeers) {
             return Futures.immediateFuture(this);
         }
         final SettableFuture<PeerGroup> future = SettableFuture.create();
         addEventListener(new AbstractPeerEventListener() {
             @Override public void onPeerConnected(Peer peer, int peerCount) {
                 if (peerCount >= numPeers) {
                     future.set(PeerGroup.this);
                     removeEventListener(this);
                 }
             }
         });
         return future;
     }
 
     /**
      * Returns the number of connections that are required before transactions will be broadcast. If there aren't
      * enough, {@link PeerGroup#broadcastTransaction(Transaction)} will wait until the minimum number is reached so
      * propagation across the network can be observed. If no value has been set using
      * {@link PeerGroup#setMinBroadcastConnections(int)} a default of half of whatever
      * {@link com.google.bitcoin.core.PeerGroup#getMaxConnections()} returns is used.
      * @return
      */
     public int getMinBroadcastConnections() {
         if (minBroadcastConnections == 0) {
             int max = getMaxConnections();
             if (max <= 1)
                 return max;
             else
                 return (int)Math.round(getMaxConnections() / 2.0);
         }
         return minBroadcastConnections;
     }
 
     /**
      * See {@link com.google.bitcoin.core.PeerGroup#getMinBroadcastConnections()}.
      */
     public void setMinBroadcastConnections(int value) {
         minBroadcastConnections = value;
     }
 
     /**
      * Calls {@link PeerGroup#broadcastTransaction(Transaction,int)} with getMinBroadcastConnections() as the number
      * of connections to wait for before commencing broadcast.
      */
     public ListenableFuture<Transaction> broadcastTransaction(final Transaction tx) {
         return broadcastTransaction(tx, getMinBroadcastConnections());
     }
 
     /**
      * <p>Given a transaction, sends it un-announced to one peer and then waits for it to be received back from other
      * peers. Once all connected peers have announced the transaction, the future will be completed. If anything goes
      * wrong the exception will be thrown when get() is called, or you can receive it via a callback on the
      * {@link ListenableFuture}. This method returns immediately, so if you want it to block just call get() on the
      * result.</p>
      *
      * <p>Note that if the PeerGroup is limited to only one connection (discovery is not activated) then the future
      * will complete as soon as the transaction was successfully written to that peer.</p>
      *
      * <p>Other than for sending your own transactions, this method is useful if you have received a transaction from
      * someone and want to know that it's valid. It's a bit of a weird hack because the current version of the Bitcoin
      * protocol does not inform you if you send an invalid transaction. Because sending bad transactions counts towards
      * your DoS limit, be careful with relaying lots of unknown transactions. Otherwise you might get kicked off the
      * network.</p>
      *
      * <p>The transaction won't be sent until there are at least minConnections active connections available.
      * A good choice for proportion would be between 0.5 and 0.8 but if you want faster transmission during initial
      * bringup of the peer group you can lower it.</p>
      */
     public ListenableFuture<Transaction> broadcastTransaction(final Transaction tx, final int minConnections) {
         final SettableFuture<Transaction> future = SettableFuture.create();
         log.info("Waiting for {} peers required for broadcast ...", minConnections);
         ListenableFuture<PeerGroup> peerAvailabilityFuture = waitForPeers(minConnections);
         peerAvailabilityFuture.addListener(new Runnable() {
             public void run() {
                 // This can be called immediately if we already have enough peers. Otherwise it'll be called from a
                 // peer thread.
                 final Peer somePeer = peers.get(0);
                 log.info("broadcastTransaction: Enough peers, adding {} to the memory pool and sending to {}",
                         tx.getHashAsString(), somePeer);
                 final Transaction pinnedTx = memoryPool.seen(tx, somePeer.getAddress());
                 try {
                     // Satoshis code sends an inv in this case and then lets the peer request the tx data. We just
                     // blast out the TX here for a couple of reasons. Firstly it's simpler: in the case where we have
                     // just a single connection we don't have to wait for getdata to be received and handled before
                     // completing the future in the code immediately below. Secondly, it's faster. The reason the
                     // Satoshi client sends an inv is privacy - it means you can't tell if the peer originated the
                     // transaction or not. However, we are not a fully validating node and this is advertised in
                     // our version message, as SPV nodes cannot relay it doesn't give away any additional information
                     // to skip the inv here - we wouldn't send invs anyway.
                     somePeer.sendMessage(pinnedTx);
                 } catch (IOException e) {
                     future.setException(e);
                     return;
                 }
 
                 // If we've been limited to talk to only one peer, we can't wait to hear back because the remote peer
                 // won't tell us about transactions we just announced to it for obvious reasons. So we just have to
                 // assume we're done, at that point. This happens when we're not given any peer discovery source and
                 // the user just calls connectTo() once.
                 if (minConnections == 1) {
                     synchronized (PeerGroup.this) {
                         for (Wallet wallet : wallets) {
                             try {
                                 wallet.receivePending(pinnedTx);
                             } catch (Throwable t) {
                                 future.setException(t);
                                 return;
                             }
                         }
                     }
                     future.set(pinnedTx);
                     return;
                 }
 
                 tx.getConfidence().addEventListener(new TransactionConfidence.Listener() {
                     public void onConfidenceChanged(Transaction tx) {
                         // This will run in a peer thread.
                         final int numSeenPeers = tx.getConfidence().getBroadcastBy().size();
                         boolean done = false;
                         log.info("broadcastTransaction: TX {} seen by {} peers", pinnedTx.getHashAsString(),
                                 numSeenPeers);
                         synchronized (PeerGroup.this) {
                             if (numSeenPeers >= minConnections) {
                                 // We've seen the min required number of peers announce the transaction. Note that we
                                 // can't wait for the current number of connected peers right now because we could have
                                 // added more peers after the broadcast took place, which means they won't have seen
                                 // the transaction. In future when peers sync up their memory pools after they connect
                                 // we could come back and change this.
                                 //
                                 // Now tell the wallet about the transaction. If the wallet created the transaction then
                                 // it already knows and will ignore this. If it's a transaction we received from
                                 // somebody else via a side channel and are now broadcasting, this will put it into the
                                 // wallet now we know it's valid.
                                 for (Wallet wallet : wallets) {
                                     try {
                                         wallet.receivePending(pinnedTx);
                                     } catch (Throwable t) {
                                         future.setException(t);
                                         return;
                                     }
                                 }
                                 done = true;
                             }
                         }
                         if (done) {
                             // We're done! Run this outside of the peer group lock as setting the future may immediately
                             // invoke any listeners associated with it and it's simpler if the PeerGroup isn't locked.
                             log.info("broadcastTransaction: {} complete", pinnedTx.getHashAsString());
                             tx.getConfidence().removeEventListener(this);
                             future.set(pinnedTx);
                         }
                     }
                 });
             }
         }, MoreExecutors.sameThreadExecutor());
         return future;
     }
 
     /**
      * Returns the period between pings for an individual peer. Setting this lower means more accurate and timely ping
      * times are available via {@link com.google.bitcoin.core.Peer#getLastPingTime()} but it increases load on the
      * remote node. It defaults to 5000.
      */
     public synchronized long getPingIntervalMsec() {
         return pingIntervalMsec;
     }
 
     /**
      * Sets the period between pings for an individual peer. Setting this lower means more accurate and timely ping
      * times are available via {@link com.google.bitcoin.core.Peer#getLastPingTime()} but it increases load on the
      * remote node. It defaults to {@link PeerGroup#DEFAULT_PING_INTERVAL_MSEC}.
      * Setting the value to be <= 0 disables pinging entirely, although you can still request one yourself
      * using {@link com.google.bitcoin.core.Peer#ping()}.
      */
     public synchronized void setPingIntervalMsec(long pingIntervalMsec) {
         this.pingIntervalMsec = pingIntervalMsec;
     }
 
     private static class PeerGroupThreadFactory implements ThreadFactory {
         static final AtomicInteger poolNumber = new AtomicInteger(1);
         final ThreadGroup group;
         final AtomicInteger threadNumber = new AtomicInteger(1);
         final String namePrefix;
 
         PeerGroupThreadFactory() {
             group = Thread.currentThread().getThreadGroup();
             namePrefix = "PeerGroup-" + poolNumber.getAndIncrement() + "-thread-";
         }
 
         public Thread newThread(Runnable r) {
             Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
             // Lower the priority of the peer threads. This is to avoid competing with UI threads created by the API
             // user when doing lots of work, like downloading the block chain. We select a priority level one lower
             // than the parent thread, or the minimum.
             t.setPriority(Math.max(Thread.MIN_PRIORITY, Thread.currentThread().getPriority() - 1));
             t.setDaemon(true);
             return t;
         }
     }
 }
