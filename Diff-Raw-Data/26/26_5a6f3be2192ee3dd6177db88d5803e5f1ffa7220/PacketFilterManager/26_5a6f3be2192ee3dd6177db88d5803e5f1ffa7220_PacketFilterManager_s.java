 package com.comphenix.protocol.injector;
 
 import java.io.DataInputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArraySet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.minecraft.server.Packet;
 import net.sf.cglib.proxy.Enhancer;
 import net.sf.cglib.proxy.MethodInterceptor;
 import net.sf.cglib.proxy.MethodProxy;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 
 import com.comphenix.protocol.ProtocolManager;
 import com.comphenix.protocol.events.ConnectionSide;
 import com.comphenix.protocol.events.PacketAdapter;
 import com.comphenix.protocol.events.PacketContainer;
 import com.comphenix.protocol.events.PacketEvent;
 import com.comphenix.protocol.events.PacketListener;
 import com.comphenix.protocol.reflect.FuzzyReflection;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Sets;
 
 public final class PacketFilterManager implements ProtocolManager {
 
 	private Set<PacketListener> packetListeners = new CopyOnWriteArraySet<PacketListener>();
 	
 	// Player injection
 	private Map<DataInputStream, Player> connectionLookup = new HashMap<DataInputStream, Player>();
 	private Map<Player, PlayerInjector> playerInjection = new HashMap<Player, PlayerInjector>();
 	
 	// Packet injection
 	private PacketInjector packetInjector;
 	
 	// Enabled packet filters
 	private Set<Integer> packetFilters = new HashSet<Integer>();
 	
 	// Whether or not this class has been closed
 	private boolean hasClosed;
 	
 	// The default class loader
 	private ClassLoader classLoader;
 	
 	// Error logger
 	private Logger logger;
 	
 	/**
 	 * Only create instances of this class if protocol lib is disabled.
 	 */
 	public PacketFilterManager(ClassLoader classLoader, Logger logger) {
 		if (logger == null)
 			throw new IllegalArgumentException("logger cannot be NULL.");
 		if (classLoader == null)
 			throw new IllegalArgumentException("classLoader cannot be NULL.");
 		
 		try {
 			// Initialize values
 			this.classLoader = classLoader;
 			this.logger = logger;
 			this.packetInjector = new PacketInjector(classLoader, this, connectionLookup);
 		} catch (IllegalAccessException e) {
 			logger.log(Level.SEVERE, "Unable to initialize packet injector.", e);
 		}
 	}
 	
 	public Logger getLogger() {
 		return logger;
 	}
 	
 	@Override
 	public ImmutableSet<PacketListener> getPacketListeners() {
 		return ImmutableSet.copyOf(packetListeners);
 	}
 
 	@Override
 	public void addPacketListener(PacketListener listener) {
 		if (listener == null)
 			throw new IllegalArgumentException("listener cannot be NULL.");
 		
 		packetListeners.add(listener);
 		enablePacketFilters(listener.getConnectionSide(), 
 							listener.getPacketsID());
 	}
 	
 	@Override
 	public void removePacketListener(PacketListener listener) {
 		if (listener == null)
 			throw new IllegalArgumentException("listener cannot be NULL");
 		
 		packetListeners.remove(listener);
 		disablePacketFilters(listener.getConnectionSide(),
 						 	 listener.getPacketsID());
 	}
 	
 	@Override
 	public void removePacketAdapters(Plugin plugin) {
 		
 		// Iterate through every packet listener
 		for (Object listener : packetListeners.toArray()) {
 			if (listener instanceof PacketAdapter) {
 				PacketAdapter adapter = (PacketAdapter) listener;
 				
 				// Remove the listener
 				if (adapter.getPlugin().equals(plugin)) {
 					packetListeners.remove(listener);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Invokes the given packet event for every registered listener.
 	 * @param event - the packet event to invoke.
 	 */
 	public void invokePacketRecieving(PacketEvent event) {
 		for (PacketListener listener : packetListeners) {
 			try {
 				if (canHandlePacket(listener, event))
 					listener.onPacketReceiving(event);
 			} catch (Exception e) {
 				// Minecraft doesn't want your Exception.
 				logger.log(Level.SEVERE, "Exception occured in onPacketReceiving() for " + listener.toString(), e);
 			}
 		}	
 	}
 	
 	/**
 	 * Invokes the given packet event for every registered listener.
 	 * @param event - the packet event to invoke.
 	 */
 	public void invokePacketSending(PacketEvent event) {
 		for (PacketListener listener : packetListeners) {
 			try {
 				if (canHandlePacket(listener, event))
 					listener.onPacketSending(event);
 			} catch (Exception e) {
 				logger.log(Level.SEVERE, "Exception occured in onPacketReceiving() for " + listener.toString(), e);
 			}
 		}	
 	}
 	
 	private boolean canHandlePacket(PacketListener listener, PacketEvent event) {
 		// Make sure the listener is looking for this packet
 		if (!listener.getPacketsID().contains(event.getPacket().getID()))
 			return false;
 		
 		// And this type of packet
 		if (event.isServerPacket())
 			return listener.getConnectionSide().isForServer();
 		else
 			return listener.getConnectionSide().isForClient();
 	}
 	
 	/**
 	 * Enables packet events for a given packet ID.
 	 * <p>
 	 * Note that all packets are disabled by default.
 	 * 
 	 * @param side - which side the event will arrive from.
 	 * @param packets - the packet id(s).
 	 */
 	private void enablePacketFilters(ConnectionSide side, Set<Integer> packets) {
 		if (side == null)
 			throw new IllegalArgumentException("side cannot be NULL.");
 		
 		for (int packetID : packets) {
 			if (side.isForServer())
 				packetFilters.add(packetID);
 			if (side.isForClient() && packetInjector != null)
 				packetInjector.addPacketHandler(packetID);
 		}
 	}
 
 	/**
 	 * Disables packet events from a given packet ID.
 	 * @param packets - the packet id(s).
 	 * @param side - which side the event no longer should arrive from.
 	 */
 	private void disablePacketFilters(ConnectionSide side, Set<Integer> packets) {
 		if (side == null)
 			throw new IllegalArgumentException("side cannot be NULL.");
 		
 		for (int packetID : packets) {
 			if (side.isForServer())
 				packetFilters.remove(packetID);
 			if (side.isForClient() && packetInjector != null) 
 				packetInjector.removePacketHandler(packetID);
 		}
 	}
 	
 	@Override
 	public void sendServerPacket(Player reciever, PacketContainer packet) throws InvocationTargetException {
 		sendServerPacket(reciever, packet, true);
 	}
 	
 	@Override
 	public void sendServerPacket(Player reciever, PacketContainer packet, boolean filters) throws InvocationTargetException {
 		if (reciever == null)
 			throw new IllegalArgumentException("reciever cannot be NULL.");
 		if (packet == null)
 			throw new IllegalArgumentException("packet cannot be NULL.");
 		
 		getInjector(reciever).sendServerPacket(packet.getHandle(), filters);
 	}
 
 	@Override
 	public void recieveClientPacket(Player sender, PacketContainer packet) throws IllegalAccessException, InvocationTargetException {
 		recieveClientPacket(sender, packet, true);
 	}
 	
 	@Override
 	public void recieveClientPacket(Player sender, PacketContainer packet, boolean filters) throws IllegalAccessException, InvocationTargetException {
 		
 		if (sender == null)
 			throw new IllegalArgumentException("sender cannot be NULL.");
 		if (packet == null)
 			throw new IllegalArgumentException("packet cannot be NULL.");
 		
 		PlayerInjector injector = getInjector(sender);
 		Packet mcPacket = packet.getHandle();
 		
 		if (filters) {
 			mcPacket = injector.handlePacketRecieved(mcPacket);
 		}
 		
 		injector.processPacket(mcPacket);
 	}
 	
 	@Override
 	public PacketContainer createPacket(int id) {
 		return createPacket(id, false);
 	}
 	
 	@Override
 	public PacketContainer createPacket(int id, boolean skipDefaults) {
 		PacketContainer packet = new PacketContainer(id);
 		
 		// Use any default values if possible
 		if (!skipDefaults) {
 			try {
 				packet.getModifier().writeDefaults();
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Security exception.", e);
 			}
 		}
 		
 		return packet;
 	}
 	
 	@Override
 	public Set<Integer> getPacketFilters() {
 		if (packetInjector != null)
 			return Sets.union(packetFilters, packetInjector.getPacketHandlers());
 		else
 			return packetFilters;
 	}
 	
 	/**
 	 * Initialize the packet injection for every player.
 	 * @param players - list of players to inject. 
 	 */
 	public void initializePlayers(Player[] players) {
 		for (Player player : players)
 			injectPlayer(player);
 	}
 	
 	private void injectPlayer(Player player) {
 		// Don't inject if the class has closed
 		if (!hasClosed && player != null && !playerInjection.containsKey(player)) {
 			try {
 				PlayerInjector injector = new PlayerInjector(player, this, packetFilters);
 				
 				injector.injectManager();
 				playerInjection.put(player, injector);
 				connectionLookup.put(injector.getInputStream(false), player);
 				
 			} catch (IllegalAccessException e) {
 				// Mark this injection attempt as a failure
 				playerInjection.put(player, null);
 				logger.log(Level.SEVERE, "Unable to access fields.", e);
 			}
 		}
 	}
 	
 	/**
 	 * Register this protocol manager on Bukkit.
 	 * @param manager - Bukkit plugin manager that provides player join/leave events.
 	 * @param plugin - the parent plugin.
 	 */
 	public void registerEvents(PluginManager manager, Plugin plugin) {
 		
 		try {
 			manager.registerEvents(new Listener() {
 				
 				@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 			    public void onPlayerJoin(PlayerJoinEvent event) {
 					injectPlayer(event.getPlayer());
 			    }
 				
 				@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 			    public void onPlayerQuit(PlayerQuitEvent event) {
 					uninjectPlayer(event.getPlayer());
 			    }
 			}, plugin);
 		
 		} catch (NoSuchMethodError e) {
 			// Oh wow! We're running on 1.0.0 or older.
 			registerOld(manager, plugin);
 		}
 	}
 	
 	// Yes, this is crazy.
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private void registerOld(PluginManager manager, Plugin plugin) {
 		
 		try {
 			ClassLoader loader = manager.getClass().getClassLoader();
 			
 			// The different enums we are going to need
 			Class eventTypes = loader.loadClass("org.bukkit.event.Event$Type");
 			Class eventPriority = loader.loadClass("org.bukkit.event.Event$Priority");
 			
 			// Get the priority
 			Object priorityNormal = Enum.valueOf(eventPriority, "Normal");
 			
 			// Get event types
 			Object playerJoinType = Enum.valueOf(eventTypes, "PLAYER_JOIN");
 			Object playerQuitType = Enum.valueOf(eventTypes, "PLAYER_QUIT");
 			
 			// The player listener! Good times.
 			Class<?> playerListener = loader.loadClass("org.bukkit.event.player.PlayerListener");
 			
 			// Find the register event method
 			Method registerEvent = FuzzyReflection.fromObject(manager).getMethodByParameters("registerEvent", 
 					eventTypes, Listener.class, eventPriority, Plugin.class);
 			
 			Enhancer ex = new Enhancer();
 			ex.setSuperclass(playerListener);
 			ex.setClassLoader(classLoader);
 			ex.setCallback(new MethodInterceptor() {
 				@Override
 				public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
 					
 					// Must have a parameter
 					if (args.length == 1) {
 						Object event = args[0];
 						
 						// Check for the correct event
 						if (event instanceof PlayerJoinEvent)
 							injectPlayer(((PlayerJoinEvent) event).getPlayer());
 						else if (event instanceof PlayerQuitEvent)
 							injectPlayer(((PlayerQuitEvent) event).getPlayer());
 					}
 					
 					return null;
 				}
 			});
 			
 			// Create our listener
 			Object proxy = ex.create();
 			
 			registerEvent.invoke(manager, playerJoinType, proxy, priorityNormal, plugin);
 			registerEvent.invoke(manager, playerQuitType, proxy, priorityNormal, plugin);
 			
 			// A lot can go wrong
 		} catch (ClassNotFoundException e1) {
 			e1.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void uninjectPlayer(Player player) {
 		if (!hasClosed && player != null) {
 			
 			PlayerInjector injector = playerInjection.get(player);
 			DataInputStream input = injector.getInputStream(true);
 			
 			if (injector != null) {
 				injector.cleanupAll();
 				
 				playerInjection.remove(injector);
 				connectionLookup.remove(input);
 			}
 		}	
 	}
 	
 	private PlayerInjector getInjector(Player player) {
 		if (!playerInjection.containsKey(player)) {
 			// What? Try to inject again.
 			injectPlayer(player);
 		}
 		
 		PlayerInjector injector = playerInjection.get(player);
 		
 		// Check that the injector was sucessfully added
 		if (injector != null)
 			return injector;
 		else
 			throw new IllegalArgumentException("Player has no injected handler.");
 	}
 	
 	@Override
 	public boolean isClosed() {
 		return hasClosed;
 	}
 	
 	public void close() {
 		// Guard
 		if (hasClosed)
 			return;
 		
 		// Remove everything
 		for (PlayerInjector injection : playerInjection.values()) {
 			injection.cleanupAll();
 		}
 		
 		// Remove packet handlers
 		if (packetInjector != null)
 			packetInjector.cleanupAll();
 		
 		// Remove listeners
 		packetListeners.clear();
 		playerInjection.clear();
 		connectionLookup.clear();
 		hasClosed = true;
 	}
 	
 	@Override
 	protected void finalize() throws Throwable {
 		close();
 	}
 }
