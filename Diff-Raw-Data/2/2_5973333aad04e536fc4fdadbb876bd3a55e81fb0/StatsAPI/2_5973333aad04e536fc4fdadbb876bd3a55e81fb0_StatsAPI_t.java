 package btwmods;
 
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Set;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.logging.Level;
 
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.CommandHandler;
 import btwmods.events.EventDispatcher;
 import btwmods.events.EventDispatcherFactory;
 import btwmods.events.IAPIListener;
 import btwmods.io.Settings;
 import btwmods.measure.Measurement;
 import btwmods.measure.Measurements;
 import btwmods.measure.TimeMeasurement;
 import btwmods.stats.IStatsListener;
 import btwmods.stats.CommandStats;
 import btwmods.stats.StatsProcessor;
 import btwmods.stats.data.QueuedTickStats;
 import btwmods.stats.measurements.StatWorld;
 import btwmods.stats.measurements.StatWorldValue;
 
 public class StatsAPI {
 	
 	private static final String PROFILE_DEFAULT = "default";
 	private static final String PROFILE_FULL = "full";
 	private static final String PROFILE_OFF = "off";
 	
 	private StatsAPI() {}
 	
 	private static boolean isInitialized = false;
 	
 	private static MinecraftServer server;
 	
 	/**
 	 * The detailed measurements that have been take this tick. 
 	 */
	private static Measurements measurements = new Measurements();
 	
 	/**
 	 * The stat profile that will be used. Changes do not take effect till the start of the next server tick.
 	 */
 	public static volatile String statProfile = PROFILE_DEFAULT;
 	
 	private static volatile String statProfileCurrent = null;
 	
 	/**
 	 * Named profiles that specify that stats are enabled/disabled. 
 	 */
 	private static Map<String, EnumMap<Stat, Boolean>> statProfiles = new HashMap<String, EnumMap<Stat, Boolean>>();
 	
 	private static EventDispatcher listeners = EventDispatcherFactory.create(new Class[] { IStatsListener.class });
 	
 	/**
 	 * A thread-safe queue where tick stats are stored for the StatsProcessor to pick retrieve.
 	 */
 	private static ConcurrentLinkedQueue<QueuedTickStats> statsQueue = new ConcurrentLinkedQueue<QueuedTickStats>();
 	
 	/**
 	 * A thread-safe queue where recorded measurements from other threads are stored.
 	 */
 	private static ConcurrentLinkedQueue<Measurement> recordMeasurementQueue = new ConcurrentLinkedQueue<Measurement>();
 	
 	/**
 	 * Should only be called by ModLoader.
 	 * 
 	 * @param settings 
 	 * @throws NoSuchFieldException 
 	 * @throws IllegalAccessException 
 	 */
 	static void init(Settings settings) throws NoSuchFieldException, IllegalAccessException {
 		server = MinecraftServer.getServer();
 
 		// Load settings
 		loadProfiles(settings);
 		
 		if (settings.hasKey("StatsAPI", "statProfile")) {
 			statProfile = settings.get("StatsAPI", "statProfile");
 		}
 		
 		((CommandHandler)server.getCommandManager()).registerCommand(new CommandStats());
 		
 		isInitialized = true;
 	}
 
 	private static void loadProfiles(Settings settings) {
 		int count = settings.getInt("StatsAPI", "profiles", 0);
 		for (int i = 1; i <= count; i++) {
 			if (!settings.hasKey("StatsAPI", "profile" + i)) {
 				// TODO: Report error
 			}
 			else {
 				String[] pairs = settings.get("StatsAPI", "profile" + i).split(";", 2);
 				if (pairs.length != 2) {
 					// TODO: Report error
 				}
 				else {
 					String name = pairs[0].toLowerCase();
 					if (name.equals(PROFILE_DEFAULT) || name.equals(PROFILE_FULL) || name.equals(PROFILE_OFF)) {
 						// TODO: Report error
 					}
 					else {
 						pairs = pairs[1].split(";");
 						
 						EnumMap<Stat, Boolean> profile = new EnumMap<Stat, Boolean>(Stat.class);
 						for (String pair : pairs) {
 							String[] splitPair = pair.split(":", 2);
 							if (splitPair.length != 2) {
 								// TODO: Report error
 							}
 							else if (!Settings.isBooleanValue(splitPair[1])) {
 								// TODO: Report error
 							}
 							else {
 								try {
 									profile.put(Stat.valueOf(splitPair[0]), Settings.getBooleanValue(splitPair[1], false));
 								}
 								catch (IllegalStateException e) {
 									// TODO: Report error
 								}
 							}
 						}
 						
 						statProfiles.put(name, profile);
 					}
 				}
 			}
 		}
 		
 		// Empty profile that will use default values.
 		statProfiles.put(PROFILE_DEFAULT, new EnumMap<Stat, Boolean>(Stat.class));
 		
 		// Profile where all stats are enabled.
 		EnumMap<Stat, Boolean> full = new EnumMap<Stat, Boolean>(Stat.class);
 		EnumMap<Stat, Boolean> off = new EnumMap<Stat, Boolean>(Stat.class);
 		for (Stat stat : Stat.values()) {
 			full.put(stat, Boolean.TRUE);
 			off.put(stat, Boolean.FALSE);
 		}
 		statProfiles.put(PROFILE_FULL, full);
 		statProfiles.put(PROFILE_OFF, off);
 	}
 	
 	/**
 	 * Add a listener supported by this API.
 	 * 
 	 * @param listener The listener to add.
 	 */
 	public static void addListener(IAPIListener listener) {
 		if (listener instanceof IStatsListener) {
 			listeners.queuedAddListener(listener, IStatsListener.class);
 
 			if (!StatsProcessor.isRunning()) {
 				Thread thread = new Thread(StatsProcessor.setStatsProcessor(new StatsProcessor(listeners, statsQueue)));
 				thread.setName(StatsAPI.class.getSimpleName());
 				thread.start();
 			}
 		}
 		else {
 			listeners.addListener(listener);
 		}
 	}
 
 	/**
 	 * Remove a listener that has been added to this API.
 	 * 
 	 * @param listener The listener to remove.
 	 */
 	public static void removeListener(IAPIListener listener) {
 		if (listener instanceof IStatsListener) {
 			listeners.queuedRemoveListener(listener, IStatsListener.class);
 		}
 		else {
 			listeners.removeListener(listener);
 		}
 	}
 
 	public static void onStartTick() {
 		if (!isInitialized)
 			return;
 		
 		// Process any failures that may be queued from the last tick.
 		ModLoader.processFailureQueue();
 		
 		// Set the stat profile for this tick, if it has changed.
 		if (!statProfile.equals(statProfileCurrent)) {
 			EnumMap<Stat, Boolean> profile = statProfiles.get(statProfile);
 			if (profile == null) {
 				statProfile = PROFILE_DEFAULT;
 				profile = statProfiles.get(statProfile);
 			}
 			
 			statProfileCurrent = statProfile;
 			Stat.setEnabled(profile);
 		}
 	}
 	
 	public static Set<String> getProfileNames() {
 		return statProfiles.keySet();
 	}
 	
 	private static void setProfileOff() {
 		statProfile = statProfileCurrent = PROFILE_OFF;
 		Stat.setEnabled(statProfiles.get(PROFILE_OFF));
 	}
 
 	public static void onEndTick() {
 		int tickCounter = ServerAPI.getTickCounter();
 		
 		if (!isInitialized)
 			return;
 		
 		if (!StatsProcessor.isRunning()) {
 			statsQueue.clear();
 		}
 		
 		else {
 			QueuedTickStats stats = new QueuedTickStats();
 			
 			stats.statProfile = statProfileCurrent;
 			stats.tickEnd = System.currentTimeMillis();
 			stats.tickCounter = tickCounter;
 			stats.tickTime = server.tickTimeArray[tickCounter % 100];
 			stats.players = server.getConfigurationManager().getAllUsernames();
 			stats.sentPacketCount = server.sentPacketCountArray[tickCounter % 100];
 			stats.sentPacketSize = server.sentPacketSizeArray[tickCounter % 100];
 			stats.receivedPacketCount = server.receivedPacketCountArray[tickCounter % 100];
 			stats.receivedPacketSize = server.receivedPacketSizeArray[tickCounter % 100];
 			
 			stats.bytesReceived = Stat.bytesReceived.get();
 			stats.bytesSent = Stat.bytesSent.get();
 			
 			stats.handlerInvocations = EventDispatcherFactory.getInvocationCount();
 			
 			if (!measurements.completedMeasurements()) {
 				setProfileOff();
 				measurements.startNew();
 				ModLoader.outputError("StatsAPI detected that not all measurements were completed properly and has switched to the 'off' profile.", Level.SEVERE);
 			}
 			
 			Measurement polled = null;
 			while ((polled = recordMeasurementQueue.poll()) != null) {
 				if (!(polled.identifier instanceof Stat) || ((Stat)polled.identifier).enabled)
 					measurements.record(polled);
 			}
 			
 			for (int i = 0, l = server.worldServers.length; i < l; i++) {
 				measurements.record(new StatWorld(Stat.WORLD_TICK, i).record(server.timeOfLastDimensionTick[i][tickCounter % 100]));
 				measurements.record(new StatWorldValue(Stat.WORLD_LOADED_CHUNKS, i, WorldAPI.getLoadedChunks()[i].size()));
 				measurements.record(new StatWorldValue(Stat.WORLD_CACHED_CHUNKS, i, WorldAPI.getCachedChunks()[i].getNumHashElements()));
 				measurements.record(new StatWorldValue(Stat.WORLD_DROPPED_CHUNKS, i, WorldAPI.getDroppedChunks()[i].size()));
 				measurements.record(new StatWorldValue(Stat.WORLD_LOADED_ENTITIES, i, server.worldServers[i].loadedEntityList.size()));
 				measurements.record(new StatWorldValue(Stat.WORLD_LOADED_TILE_ENTITIES, i, server.worldServers[i].loadedTileEntityList.size()));
 				measurements.record(new StatWorldValue(Stat.WORLD_TRACKED_ENTITIES, i, WorldAPI.getTrackedEntities()[i].size()));
 			}
 			
 			// Save measurements and clear it for the next round.
 			stats.measurements = measurements.startNew();
 			
 			statsQueue.add(stats);
 		}
 	}
 	
 	public static void record(Measurement measurement) {
 		if (Thread.currentThread() == ModLoader.getInitThread()) {
 			measurements.record(measurement);
 		}
 		else {
 			recordMeasurementQueue.add(measurement);
 		}
 	}
 	
 	public static void begin(TimeMeasurement measurement) {
 		measurements.begin(measurement);
 	}
 	
 	/**
 	 * End a measurement.
 	 * 
 	 * @param identifier The identifier that matches the last {@link #begin}. 
 	 */
 	public static void end(Object identifier) {
 		try {
 			measurements.end(identifier);
 		}
 		catch (IllegalStateException e) {
 			setProfileOff();
 			measurements.startNew();
 			ModLoader.outputError(e, "StatsAPI#end(" + identifier.toString() + ") switched to the 'off' profile: " + e.getMessage(), Level.SEVERE);
 		}
 		catch (NoSuchElementException e) {
 			setProfileOff();
 			measurements.startNew();
 			ModLoader.outputError(e, "StatsAPI#end(" + identifier.toString() + ") called unexpectedly. Switched to 'off' profile.", Level.SEVERE);
 		}
 	}
 }
