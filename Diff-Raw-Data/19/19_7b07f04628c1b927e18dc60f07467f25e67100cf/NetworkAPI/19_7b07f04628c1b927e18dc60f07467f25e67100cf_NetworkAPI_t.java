 package btwmods;
 
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 
 import net.minecraft.src.EntityPlayerMP;
 import net.minecraft.src.NetHandler;
 import net.minecraft.src.NetServerHandler;
 import net.minecraft.src.Packet;
 import btwmods.events.EventDispatcher;
 import btwmods.events.EventDispatcherFactory;
 import btwmods.events.IAPIListener;
 import btwmods.io.Settings;
 import btwmods.network.CustomPacketEvent;
 import btwmods.network.ICustomPacketListener;
 import btwmods.network.IPacketHandlerListener;
 import btwmods.network.IPacketListener;
 import btwmods.network.NetworkType;
 import btwmods.network.PacketEvent;
 import btwmods.network.PacketHandlerEvent;
 
 public class NetworkAPI {
 	
 	private static final String BASE_CHANNEL_NAME = "BM|";
 	
 	private static Field playerEntityField;
 	
 	// Normal channel lookup (by channel extension).
 	private static Map<String, ICustomPacketListener> networkListeners = new HashMap<String, ICustomPacketListener>();
 	
 	// Reverse channel lookup (by ICustomPacketListener).
 	private static Map<ICustomPacketListener, Set<String>> channelListeners = new HashMap<ICustomPacketListener, Set<String>>();
 	
 	private static EventDispatcher listeners = EventDispatcherFactory.create(new Class[] { IPacketListener.class, IPacketHandlerListener.class });
 	
 	private NetworkAPI() { }
 	
 	public static void init(@SuppressWarnings("unused") Settings settings) throws NoSuchFieldException {
 		playerEntityField = ReflectionAPI.getPrivateField(NetServerHandler.class, "playerEntity");
 		if (playerEntityField == null)
 			throw new NoSuchFieldException("playerEntity");
 	}
 	
 	public static void addListener(IAPIListener listener) {
 		listeners.addListener(listener);
 	}
 
 	public static void removeListener(IAPIListener listener) {
 		listeners.removeListener(listener);
 	}
 	
 	/**
 	 * Registered a custom channel to a {@link ICustomPacketListener}.
 	 * 
 	 * @param channelExtension the extension that will be added to the base channel name used by the API.
 	 * @param listener the listener that implements {@link ICustomPacketListener}.
 	 * @return true if the channel was registered; false if the channel is already registered.
 	 * @throws IllegalArgumentException if channelExtension is null or empty, or if listener is null.
 	 */
 	public static boolean registerCustomChannel(String channelExtension, ICustomPacketListener listener) throws IllegalArgumentException {
 		if (channelExtension == null || channelExtension.length() == 0)
 			throw new IllegalArgumentException("channelExtension cannot be null or a zero length string");
 		
 		if (listener == null)
 			throw new IllegalArgumentException("listener cannot be null or a zero length string");
 		
 		if (!networkListeners.containsKey(channelExtension)) {
 			networkListeners.put(BASE_CHANNEL_NAME + channelExtension, listener);
 			
 			// Add the channel to a reversed map.
 			Set channels;
 			if ((channels = channelListeners.get(listener)) == null) {
 				channelListeners.put(listener, channels = new HashSet<String>());
 			}
 			channels.add(channelExtension);
 			
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Unregister all custom channels for a listener.
 	 * 
 	 * @param listener the listener that implements {@link ICustomPacketListener}.
 	 * @throws IllegalArgumentException if listener is null.
 	 */
 	public static void unregisterCustomChannels(IAPIListener listener) throws IllegalArgumentException {
 		if (listener == null)
 			throw new IllegalArgumentException("listener cannot be null or a zero length string");
 		
 		// Remove the listener from the normal lookup.
 		Set<String> channels = channelListeners.get(listener);
 		if (channels != null) {
 			for (String channelExtension : channelListeners.get(listener)) {
 				networkListeners.remove(channelExtension);
 			}
 		}
 		
 		// Remove the listener from the reverse lookup.
 		channelListeners.remove(listener);
 	}
 	
 	/**
 	 * Unregister a channel attached to a listener.
 	 * 
 	 * @param channelExtension the extension that will be added to the base channel name used by the API.
 	 * @param listener the listener that implements {@link ICustomPacketListener}.
 	 * @return true if the channel was unregistered; false if a channel was not found by that name, or it is registered to another listener.
 	 * @throws IllegalArgumentException if channelExtension is null or empty, or if listener is null.
 	 */
 	public static boolean unregisterCustomChannel(String channelExtension, ICustomPacketListener listener) throws IllegalArgumentException {
 		if (channelExtension == null || channelExtension.length() == 0)
 			throw new IllegalArgumentException("channelExtension cannot be null or a zero length string");
 		
 		if (listener == null)
 			throw new IllegalArgumentException("listener cannot be null or a zero length string");
 		
 		if (networkListeners.containsKey(channelExtension) && networkListeners.get(channelExtension) == listener) {
 			networkListeners.remove(channelExtension);
 			return true;
 		}
 		return false;
 	}
 
 	public static boolean onHandleCustomPayload(String channel, byte[] data, int length) {
 		// Process any queued failures before invoking a method on a mod.
 		ModLoader.processFailureQueue();
 		
 		if (networkListeners.containsKey(channel)) {
 			CustomPacketEvent event = new CustomPacketEvent(channel, data, length);
 			ICustomPacketListener listener = networkListeners.get(channel);
 			try {
 				listener.onCustomPacket(event);
 				return event.isHandled();
 			}
 			catch (Throwable e) {
 				unregisterCustomChannels(listener);
 				ModLoader.reportListenerFailure(e, listener);
 			}
 		}
 		return false;
 	}
 
 	public static void onReceivedPacket(Packet packet, NetHandler netHandler) {
 		EntityPlayerMP player = null;
 		
 		if (netHandler instanceof NetServerHandler && playerEntityField != null) {
 			try {
 				player = (EntityPlayerMP)playerEntityField.get(netHandler);
 				
 				if (!listeners.isEmpty(IPacketListener.class)) {
 					PacketEvent event = PacketEvent.ReceivedPlayerPacket(player, packet, (NetServerHandler)netHandler);
 					((IPacketListener)listeners).onPacket(event);
 				}
 			}
 			catch (Exception e) {
 				ModLoader.outputError(e, "NetworkAPI failed to get the playerEntity field from " + netHandler.getClass().getSimpleName() + ": " + e.getMessage(), Level.SEVERE);
 				ModLoader.outputError("NetworkAPI's inspection of packets received from players has been disabled.", Level.SEVERE);
 				playerEntityField = null;
 			}
 		}
 		
		Stat.recordNetworkIO(NetworkType.RECEIVED, packet.getPacketSize(), player);
 	}
 
 	public static void onSentPacket(Packet packet, NetHandler netHandler) {
 		EntityPlayerMP player = null;
 		
 		if (netHandler instanceof NetServerHandler && playerEntityField != null) {
 			try {
 				player = (EntityPlayerMP)playerEntityField.get(netHandler);
 				
 				if (!listeners.isEmpty(IPacketListener.class)) {
 					PacketEvent event = PacketEvent.SentPlayerPacket(player, packet, (NetServerHandler)netHandler);
 					((IPacketListener)listeners).onPacket(event);
 				}
 			}
 			catch (Exception e) {
 				ModLoader.outputError(e, "NetworkAPI failed to get the playerEntity field from " + netHandler.getClass().getSimpleName() + ": " + e.getMessage(), Level.SEVERE);
 				ModLoader.outputError("NetworkAPI's inspection of packets sent to players has been disabled.", Level.SEVERE);
 				playerEntityField = null;
 			}
 		}
 
		Stat.recordNetworkIO(NetworkType.SENT, packet.getPacketSize(), player);
 	}
 	
 	public static Packet onSendPlayerPacket(Packet packet, EntityPlayerMP player) {
 		if (!listeners.isEmpty(IPacketHandlerListener.class)) {
 			PacketHandlerEvent event = PacketHandlerEvent.SendPlayerPacket(player, packet);
 			((IPacketHandlerListener)listeners).onHandlePacket(event);
 			
 			return event.getPacket();
 		}
 		
 		return packet;
 	}
 }
