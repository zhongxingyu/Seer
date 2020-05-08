 package edu.teco.dnd.network;
 
 import io.netty.bootstrap.ChannelFactory;
 import io.netty.channel.ChannelFuture;
 import io.netty.channel.ChannelFutureListener;
 import io.netty.channel.ChannelHandler;
 import io.netty.channel.ChannelHandlerContext;
 import io.netty.channel.ChannelInboundMessageHandlerAdapter;
 import io.netty.channel.ChannelInitializer;
 import io.netty.channel.EventLoopGroup;
 import io.netty.channel.nio.NioEventLoopGroup;
 import io.netty.channel.oio.OioEventLoopGroup;
 import io.netty.channel.socket.DatagramChannel;
 import io.netty.channel.socket.oio.OioDatagramChannel;
 import io.netty.handler.codec.string.StringDecoder;
 import io.netty.handler.codec.string.StringEncoder;
 import io.netty.util.AttributeKey;
 
 import java.net.InetSocketAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.nio.charset.Charset;
 import java.util.AbstractMap;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicReference;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
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
 	 * Lock used for synchronizing access to {@link #channels}.
 	 */
 	private final ReadWriteLock channelLock = new ReentrantReadWriteLock();
 	
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
 	
 	
 	
 	// FIXME: only for testing
 	public static void main(final String[] args) throws SocketException {
 		new UDPMulticastBeacon(new ChannelFactory<OioDatagramChannel>() {
 			@Override
 			public OioDatagramChannel newChannel() {
 				return new OioDatagramChannel();
 			}
 		},new OioEventLoopGroup(), new NioEventLoopGroup(), UUID.randomUUID())
 		.addAddress(NetworkInterface.getByName("lo"), new InetSocketAddress("224.0.0.1", 5000));
 	}
 	
 	
 	
 	
 	/**
 	 * Creates a new UDPMulticastBeacon.
 	 * 
 	 * @param factory a ChannelFactory
 	 * @param group the EventLoopGroup to use for channels and the timer
 	 * @param the UUID to announce
 	 * @param interval the interval at which to send beacons
 	 * @param unit the unit for interval
 	 */
 	// TODO: add EventExecutorGroup
 	public UDPMulticastBeacon(final ChannelFactory<? extends DatagramChannel> factory, final EventLoopGroup group,
 			final EventLoopGroup scheduler, final UUID uuid, final long interval, final TimeUnit unit) {
 		beacon = new AtomicReference<BeaconMessage>(
 				new BeaconMessage(uuid, Collections.<InetSocketAddress>emptyList()));
 		
 		scheduler.scheduleAtFixedRate(new Runnable() {
 			@Override
 			public void run() {
 				sendBeacon();
 			}
 		}, 0, interval, unit);
 		
 		final GsonBuilder gsonBuilder = new GsonBuilder();
 		final MessageAdapter messageAdapter = new MessageAdapter();
 		messageAdapter.addMessageType(BeaconMessage.class);
 		gsonBuilder.registerTypeAdapter(Message.class, messageAdapter);
 		gsonBuilder.registerTypeAdapter(InetSocketAddress.class, new InetSocketAddressAdapter());
 		final Gson gson = gsonBuilder.create();
 		
 		this.channelFactory = new UDPMulticastChannelFactory(factory, group, new ChannelInitializer<DatagramChannel>() {
 			private final DatagramPacketWrapper datagramPacketWrapper = new DatagramPacketWrapper();
 			private final StringEncoder stringEncoder = new StringEncoder();
 			private final StringDecoder stringDecoder = new StringDecoder();
 			private final GsonCodec gsonCodec = new GsonCodec(gson, Message.class);
 			private final ChannelHandler beaconHandler = new ChannelInboundMessageHandlerAdapter<BeaconMessage>() {
 				@Override
 				public void messageReceived(final ChannelHandlerContext ctx, final BeaconMessage msg) {
 					handleBeacon(msg);
 				}
 			};
 			
 			@Override
 			protected void initChannel(final DatagramChannel channel) {
 				channel.pipeline()
 					.addLast(datagramPacketWrapper)
 					.addLast(stringEncoder)
 					.addLast(stringDecoder)
 					.addLast(gsonCodec)
					.addLast(beaconHandler); // TODO: add executor group
 				
 				// Move TARGET_ADDRESS from channel context to handler context
 				channel.pipeline().context(DatagramPacketWrapper.class).attr(DatagramPacketWrapper.TARGET_ADDRESS)
 					.set(channel.attr(DatagramPacketWrapper.TARGET_ADDRESS).getAndRemove());
 			}
 		});
 	}
 	
 	/**
 	 * Creates a new UDPMulticastBeacon. Beacons will be send at intervals defined by {@link #DEFAULT_INTERVAL} and {@link #DEFAULT_INTERVAL_UNIT}.
 	 * 
 	 * @param factory a ChannelFactory
 	 * @param group the EventLoopGroup to use for channels and the timer
 	 * @param the UUID to announce
 	 */
 	public UDPMulticastBeacon(final ChannelFactory<? extends DatagramChannel> factory, final EventLoopGroup group,
 			final EventLoopGroup scheduler, final UUID uuid) {
 		this(factory, group, scheduler, uuid, DEFAULT_INTERVAL, DEFAULT_INTERVAL_UNIT);
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
 		final Map<AttributeKey<?>, Object> attrs = new HashMap<AttributeKey<?>, Object>();
 		attrs.put(DatagramPacketWrapper.TARGET_ADDRESS, address);
 		channelFactory.bind(inf, address, attrs).addListener(new ChannelFutureListener() {
 			@Override
 			public void operationComplete(final ChannelFuture future) throws Exception {
 				if (future.isSuccess()) {
 					channelLock.writeLock().lock();
 					channels.put(new AbstractMap.SimpleEntry<NetworkInterface, InetSocketAddress>(inf, address),
 							(DatagramChannel) future.channel());
 					channelLock.writeLock().unlock();
 				} else {
 					LOGGER.debug("bind {} failed");
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
 	 */
 	public void addAddress(final InetSocketAddress address) {
 		// TODO
 	}
 	
 	/**
 	 * Removes an address from the list of addresses that should be used. The address will only be removed from the
 	 * given interface. If the interface/address combination is not in use nothing is done.
 	 * 
 	 * @param inf the interface from which the address should be removed
 	 * @param address the address that should be removed
 	 */
 	public void removeAddress(final NetworkInterface inf, final InetSocketAddress address) {
 		// TODO
 	}
 	
 	/**
 	 * Removes an address from the list of addresses that should be used. The address will be removed from all
 	 * interfaces. If the addresses is missing on some interfaces nothing is done for these interfaces.
 	 * 
 	 * @param address the address that should be removed
 	 */
 	public void removeAddress(final InetSocketAddress address) {
 		// TODO
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
 		final BeaconMessage oldBeacon = beacon.get();
 		beacon.lazySet(new BeaconMessage(oldBeacon.getUUID(), addresses));
 	}
 	
 	/**
 	 * Sends a beacon. This will be automatically called at a fixed interval.
 	 */
 	public void sendBeacon() {
 		final BeaconMessage msg = beacon.get();
 		channelLock.readLock().lock();
 		try {
 			for (final DatagramChannel channel : channels.values()) {
 				channel.write(msg);
 			}
 		} finally {
 			channelLock.readLock().unlock();
 		}
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
 }
