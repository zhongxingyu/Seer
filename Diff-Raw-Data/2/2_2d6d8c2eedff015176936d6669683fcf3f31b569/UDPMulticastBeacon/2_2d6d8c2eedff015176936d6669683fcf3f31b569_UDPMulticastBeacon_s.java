 package edu.teco.dnd.network;
 
 import io.netty.bootstrap.ChannelFactory;
 import io.netty.channel.Channel;
 import io.netty.channel.ChannelFuture;
 import io.netty.channel.ChannelFutureListener;
 import io.netty.channel.ChannelHandler;
 import io.netty.channel.ChannelHandler.Sharable;
 import io.netty.channel.ChannelHandlerContext;
 import io.netty.channel.ChannelInitializer;
 import io.netty.channel.EventLoopGroup;
 import io.netty.channel.SimpleChannelInboundHandler;
 import io.netty.channel.socket.DatagramChannel;
 import io.netty.handler.codec.string.StringDecoder;
 import io.netty.handler.codec.string.StringEncoder;
 import io.netty.util.AttributeKey;
 import io.netty.util.concurrent.EventExecutorGroup;
 import io.netty.util.concurrent.GenericFutureListener;
 
 import java.net.InetSocketAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.nio.charset.Charset;
 import java.util.AbstractMap;
 import java.util.AbstractMap.SimpleEntry;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicReference;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import edu.teco.dnd.network.codecs.DatagramPacketWrapper;
 import edu.teco.dnd.network.codecs.GsonCodec;
 import edu.teco.dnd.network.codecs.MessageAdapter;
 import edu.teco.dnd.network.messages.BeaconMessage;
 import edu.teco.dnd.network.messages.Message;
 import edu.teco.dnd.util.InetSocketAddressAdapter;
 
 /**
  * This class can be used to send beacons using UDP multicast. The format of the beacons is explained in the
  * <a href="https://github.com/DesignAndDeploy/dnd/wiki/Network-Protocol#udp-multicast">GitHub wiki</a>.
  * 
  * @author Philipp Adolf
  */
 public class UDPMulticastBeacon {
 	/**
 	 * The logger for this class.
 	 */
 	private static final Logger LOGGER = LogManager.getLogger(UDPMulticastBeacon.class);
 	
 	/**
 	 * The default interval to send beacons at.
 	 */
 	public static final long DEFAULT_INTERVAL = 5;
 	
 	/**
 	 * The unit for {@link #DEFAULT_INTERVAL}.
 	 */
 	public static final TimeUnit DEFAULT_INTERVAL_UNIT = TimeUnit.SECONDS;
 	
 	/**
 	 * The Charset that will be used.
 	 */
 	public static final Charset CHARSET = Charset.forName("UTF-8");
 	
 	/**
 	 * Factory for new channels.
 	 */
 	private final UDPMulticastChannelFactory channelFactory;
 	
 	/**
 	 * Stores all channels that are used for sending and receiving multicasts. Maps from interface and multicast
 	 * address to channel.
 	 */
 	private final Map<Entry<NetworkInterface, InetSocketAddress>, DatagramChannel> channels =
 			new HashMap<Map.Entry<NetworkInterface,InetSocketAddress>, DatagramChannel>();
 	
 	/**
 	 * Is set to true if the beacon should shut down.
 	 */
 	private boolean shutdown = false;
 	
 	/**
 	 * Lock used for synchronizing access to {@link #channels} and {@link #shutdown}.
 	 */
 	private final ReadWriteLock channelLock = new ReentrantReadWriteLock();
 	
 	private final Condition shutdownCondition = channelLock.writeLock().newCondition();
 	
 	private final Future<Void> shutdownFuture = new ShutdownFuture();
 	
 	/**
 	 * The listeners that will be informed when a beacon is received.
 	 */
 	private final Set<BeaconListener> listeners = new HashSet<BeaconListener>();
 	
 	/**
 	 * Lock used for synchronizing access to {@link #listeners}.
 	 */
 	private final ReadWriteLock listenersLock = new ReentrantReadWriteLock();
 	
 	/**
 	 * The beacon to send.
 	 */
 	private final AtomicReference<BeaconMessage> beacon;
 	
 	/**
 	 * Creates a new UDPMulticastBeacon.
 	 * 
 	 * @param factory a ChannelFactory
 	 * @param group the EventLoopGroup to use for channels and the timer
 	 * @param executor the executor for application code and a timer for regularly sending the beacon
 	 * @param uuid the UUID to announce
 	 * @param interval the interval at which to send beacons
 	 * @param unit the unit for interval
 	 */
 	public UDPMulticastBeacon(final ChannelFactory<? extends DatagramChannel> factory, final EventLoopGroup group,
 			final EventExecutorGroup executor, final UUID uuid, final long interval, final TimeUnit unit) {
 		beacon = new AtomicReference<BeaconMessage>(
 				new BeaconMessage(uuid, Collections.<InetSocketAddress>emptyList()));
 		
 		executor.scheduleAtFixedRate(new Runnable() {
 			@Override
 			public void run() {
 				sendBeacon();
 			}
 		}, 0, interval, unit);
 		
 		final MessageAdapter messageAdapter = new MessageAdapter();
 		messageAdapter.addMessageType(BeaconMessage.class);
 		final GsonCodec gsonCodec = new GsonCodec(Message.class);
 		gsonCodec.registerTypeAdapter(Message.class, messageAdapter);
 		gsonCodec.registerTypeAdapter(InetSocketAddress.class, new InetSocketAddressAdapter());
 		
 		this.channelFactory = new UDPMulticastChannelFactory(factory, group, new ChannelInitializer<DatagramChannel>() {
 			private final DatagramPacketWrapper datagramPacketWrapper = new DatagramPacketWrapper();
 			private final StringEncoder stringEncoder = new StringEncoder();
 			private final StringDecoder stringDecoder = new StringDecoder();
 			private final ChannelHandler beaconHandler = new BeaconHandler();
 			
 			@Override
 			protected void initChannel(final DatagramChannel channel) {
 				channel.pipeline()
 					.addLast(datagramPacketWrapper)
 					.addLast(stringEncoder)
 					.addLast(stringDecoder)
 					.addLast(gsonCodec)
 					.addLast(executor, beaconHandler);
 				
 				// Move TARGET_ADDRESS from channel context to handler context
 				channel.pipeline().context(DatagramPacketWrapper.class).attr(DatagramPacketWrapper.TARGET_ADDRESS)
 					.set(channel.attr(DatagramPacketWrapper.TARGET_ADDRESS).getAndRemove());
 			}
 		});
 	}
 	
 	/**
 	 * Creates a new UDPMulticastBeacon. Beacons will be send at intervals defined by {@link #DEFAULT_INTERVAL} and
 	 * {@link #DEFAULT_INTERVAL_UNIT}.
 	 * 
 	 * @param factory a ChannelFactory
 	 * @param group the EventLoopGroup to use for channels and the timer
 	 * @param executor the executor for application code and a timer for regularly sending the beacon
 	 * @param uuid the UUID to announce
 	 */
 	public UDPMulticastBeacon(final ChannelFactory<? extends DatagramChannel> factory, final EventLoopGroup group,
 			final EventExecutorGroup executor, final UUID uuid) {
 		this(factory, group, executor, uuid, DEFAULT_INTERVAL, DEFAULT_INTERVAL_UNIT);
 	}
 	
 	/**
 	 * Adds an address to the list of multicast addresses that are used to send multicast packages. The first package
 	 * will be send as soon as the next timer update is done.
 	 * 
 	 * @param inf the interface to send with. Must not be null.
 	 * @param address the multicast address to use for sending. Must not be null, must be resolved and must be a
 	 *		multicast address.
 	 */
 	public void addAddress(final NetworkInterface inf, final InetSocketAddress address) {
 		final Entry<NetworkInterface, InetSocketAddress> key =
 				new AbstractMap.SimpleEntry<NetworkInterface, InetSocketAddress>(inf, address);
 		channelLock.readLock().lock();
 		try {
 			if (shutdown || channels.containsKey(key)) {
 				return;
 			}
 		} finally {
 			channelLock.readLock().unlock();
 		}
 		
 		final Map<AttributeKey<?>, Object> attrs = new HashMap<AttributeKey<?>, Object>();
 		attrs.put(DatagramPacketWrapper.TARGET_ADDRESS, address);
 		ChannelFuture future = null;
 		channelLock.writeLock().lock();
 		try {
 			if (shutdown || channels.containsKey(key)) {
 				return;
 			}
 			future = channelFactory.bind(inf, address, attrs);
 			channels.put(key, (DatagramChannel) future.channel());
 		} finally {
 			channelLock.writeLock().unlock();
 		}
 		
 		future.addListener(new ChannelFutureListener() {
 			@Override
 			public void operationComplete(final ChannelFuture future) throws Exception {
 				if (!future.isSuccess()) {
 					LOGGER.debug("bind {} failed");
 					future.channel().close();
 				}
 			}
 		});
 		future.channel().closeFuture().addListener(new ChannelFutureListener() {
 			@Override
 			public void operationComplete(final ChannelFuture future) throws Exception {
 				channelLock.writeLock().lock();
 				try {
 					if (future.channel().equals(channels.get(key))) {
 						channels.remove(key);
 					}
 				} finally {
 					channelLock.writeLock().unlock();
 				}
 				channelLock.writeLock().lock();
 				try {
 					shutdownCondition.signalAll();
 				} finally {
 					channelLock.writeLock().unlock();
 				}
 			}
 		});
 	}
 	
 	/**
 	 * Adds an address to the list of multicast addresses that are used to send multicast packages. The address will
 	 * be used with all interfaces. The first package will be send as soon as the next timer update is done.
 	 * 
 	 * @param address the multicast address to use for sending. Must not be null, must be resolved and must be a
 	 *		multicast address.
 	 * @throws SocketException if an I/O error occurs
 	 */
 	public void addAddress(final InetSocketAddress address) throws SocketException {
 		LOGGER.entry(address);
 		channelLock.writeLock().lock();
 		try {
 			for (final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
 					interfaces.hasMoreElements(); ) {
 				final NetworkInterface inf = interfaces.nextElement();
 				LOGGER.trace("adding address {} for interface {}", address, inf);
 				addAddress(inf, address);
 			}
 		} finally {
 			channelLock.writeLock().unlock();
 		}
 		LOGGER.exit();
 	}
 	
 	/**
 	 * Removes an address from the list of addresses that should be used. The address will only be removed from the
 	 * given interface. If the interface/address combination is not in use nothing is done.
 	 * 
 	 * @param inf the interface from which the address should be removed
 	 * @param address the address that should be removed
 	 */
 	public void removeAddress(final NetworkInterface inf, final InetSocketAddress address) {
 		LOGGER.entry(inf, address);
 		final Entry<NetworkInterface, InetSocketAddress> key =
 				new SimpleEntry<NetworkInterface, InetSocketAddress>(inf, address);
 		Channel channel = null;
 		channelLock.readLock().lock();
 		try {
 			channel = channels.get(key);
 		} finally {
 			channelLock.readLock().unlock();
 		}
 		if (channel != null) {
 			channel.close();
 		}
 		LOGGER.exit();
 	}
 	
 	/**
 	 * Removes an address from the list of addresses that should be used. The address will be removed from all
 	 * interfaces. If the addresses is missing on some interfaces nothing is done for these interfaces.
 	 * 
 	 * @param address the address that should be removed
 	 * @throws SocketException if an I/O error occurs
 	 */
 	public void removeAddress(final InetSocketAddress address) throws SocketException {
 		LOGGER.entry(address);
 		channelLock.writeLock().lock();
 		try {
 			for (final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
 					interfaces.hasMoreElements(); ) {
 				final NetworkInterface interf = interfaces.nextElement();
 				LOGGER.trace("removing address {} from {}", address, interf);
 				removeAddress(interf, address);
 			}
 		} finally {
 			channelLock.writeLock().unlock();
 		}
 		LOGGER.exit();
 	}
 	
 	/**
 	 * Adds a listener.
 	 * 
 	 * @param listener the listener to add
 	 */
 	public void addListener(final BeaconListener listener) {
 		listenersLock.writeLock().lock();
 		try {
 			listeners.add(listener);
 		} finally {
 			listenersLock.writeLock().unlock();
 		}
 	}
 	
 	/**
 	 * Removes a listener. If the listener wasn't added before, nothing is done.
 	 * 
 	 * @param listener the listener to remove
 	 */
 	public void removeListener(final BeaconListener listener) {
 		listenersLock.writeLock().lock();
 		try {
 			listeners.remove(listener);
 		} finally {
 			listenersLock.writeLock().unlock();
 		}
 	}
 	
 	/**
 	 * Sets the addresses that will be sent with the beacon.
 	 * 
 	 * @param addresses the addresses to send with the beacon
 	 */
 	public void setAnnounceAddresses(final List<InetSocketAddress> addresses) {
 		final BeaconMessage newBeacon = new BeaconMessage(beacon.get().getUUID(), addresses);
 		LOGGER.debug("doing lazy set on beacon to {}", newBeacon);
 		beacon.lazySet(newBeacon);
 	}
 	
 	/**
 	 * Sends a beacon. This will be automatically called at a fixed interval.
 	 */
 	public void sendBeacon() {
 		final BeaconMessage msg = beacon.get();
 		channelLock.readLock().lock();
 		try {
 			for (final DatagramChannel channel : channels.values()) {
 				if (channel.isActive()) {
 					LOGGER.trace("trying to send on {}", channel);
					channel.write(msg).addListener(new GenericFutureListener<io.netty.util.concurrent.Future<Void>>() {
 						@Override
 						public void operationComplete(io.netty.util.concurrent.Future<Void> future) throws Exception {
 							LOGGER.trace(future);
 						}
 					});
 				}
 			}
 		} finally {
 			channelLock.readLock().unlock();
 		}
 	}
 	
 	/**
 	 * Closes all channels and stops this UDPMulticastBeacon from creating new ones.
 	 */
 	public void shutdown() {
 		LOGGER.entry();
 		channelLock.readLock().lock();
 		try {
 			if (shutdown) {
 				LOGGER.exit();
 				return;
 			}
 		} finally {
 			channelLock.readLock().unlock();
 		}
 		
 		channelLock.writeLock().lock();
 		try {
 			shutdown = true;
 			for (final Channel channel : channels.values()) {
 				channel.close();
 			}
 		} finally {
 			channelLock.writeLock().unlock();
 		}
 		LOGGER.exit();
 	}
 	
 	/**
 	 * Returns true if this UDPMulticastBeacon is shutting down or has finished shutting down. This is basically
 	 * whether or not {@link #shutdown()} has been called.
 	 * 
 	 * @return true if this UDPMulticastBeacon is shutting down or has finished shutting down
 	 */
 	public boolean isShuttingDown() {
 		LOGGER.entry();
 		channelLock.readLock().lock();
 		try {
 			LOGGER.exit(shutdown);
 			return shutdown;
 		} finally {
 			channelLock.readLock().unlock();
 		}
 	}
 	
 	/**
 	 * Returns true if this UDPMulticastBeacon has finished shutting down.
 	 * 
 	 * @return true if this UDPMulticastBeacon has finished shutting down
 	 */
 	public boolean isShutDown() {
 		LOGGER.entry();
 		channelLock.readLock().lock();
 		try {
 			if (!shutdown) {
 				LOGGER.exit(false);
 				return false;
 			}
 			for (final Channel channel : channels.values()) {
 				if (!channel.closeFuture().isDone()) {
 					LOGGER.exit(false);
 					return false;
 				}
 			}
 		} finally {
 			channelLock.readLock().unlock();
 		}
 		LOGGER.exit(true);
 		return true;
 	}
 	
 	public Future<Void> getShutdownFuture() {
 		return shutdownFuture;
 	}
 	
 	/**
 	 * Handles incoming beacons.
 	 * 
 	 * @param beacon the beacon that was found
 	 */
 	// TODO: maybe find a way to inform listeners about multiple beacons
 	// maybe queue them and empty the queue at a fixed interval (every second or so)
 	private void handleBeacon(final BeaconMessage beacon) {
 		LOGGER.entry(beacon);
 		if (this.beacon.get().getUUID().equals(beacon.getUUID())) {
 			LOGGER.exit();
 			return;
 		}
 		listenersLock.readLock().lock();
 		try {
 			for (final BeaconListener listener : listeners) {
 				listener.beaconFound(beacon);
 			}
 		} finally {
 			listenersLock.readLock().unlock();
 		}
 		LOGGER.exit();
 	}
 	
 	@Sharable
 	private class BeaconHandler extends SimpleChannelInboundHandler<BeaconMessage> {
 		@Override
 		public void channelRead0(final ChannelHandlerContext ctx, final BeaconMessage msg) {
 			handleBeacon(msg);
 		}
 	};
 	
 	private class ShutdownFuture implements Future<Void> {
 		@Override
 		public boolean cancel(boolean mayInterruptIfRunning) {
 			return false;
 		}
 
 		@Override
 		public Void get() throws InterruptedException {
 			channelLock.writeLock().lock();
 			try {
 				while (!isDone()) {
 					shutdownCondition.await();
 				}
 			} finally {
 				channelLock.writeLock().unlock();
 			}
 			return null;
 		}
 
 		@Override
 		public Void get(final long timeout, final TimeUnit unit) {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public boolean isCancelled() {
 			return false;
 		}
 
 		@Override
 		public boolean isDone() {
 			channelLock.readLock().lock();
 			try {
 				if (!shutdown) {
 					return false;
 				}
 				for (final Channel channel : channels.values()) {
 					if (!channel.closeFuture().isDone()) {
 						return false;
 					}
 				}
 			} finally {
 				channelLock.readLock().unlock();
 			}
 			return true;
 		}
 	}
 }
