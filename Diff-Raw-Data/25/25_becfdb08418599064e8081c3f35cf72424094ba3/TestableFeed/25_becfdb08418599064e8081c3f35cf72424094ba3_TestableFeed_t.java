 package com.barchart.feed.ddf.client.provider.legacy;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicLong;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.barchart.feed.api.Agent;
 import com.barchart.feed.api.MarketObserver;
 import com.barchart.feed.api.Marketplace;
 import com.barchart.feed.api.connection.Connection;
 import com.barchart.feed.api.connection.TimestampListener;
 import com.barchart.feed.api.model.data.Book;
 import com.barchart.feed.api.model.data.Cuvol;
 import com.barchart.feed.api.model.data.Market;
 import com.barchart.feed.api.model.data.MarketData;
 import com.barchart.feed.api.model.data.Trade;
 import com.barchart.feed.api.model.meta.Exchange;
 import com.barchart.feed.api.model.meta.Instrument;
import com.barchart.feed.api.util.Identifier;
 import com.barchart.feed.base.market.api.MarketRegListener;
 import com.barchart.feed.base.market.api.MarketTaker;
 import com.barchart.feed.base.market.enums.MarketEvent;
 import com.barchart.feed.base.sub.Subscription;
 import com.barchart.feed.ddf.datalink.api.DDF_FeedClientBase;
 import com.barchart.feed.ddf.datalink.api.DDF_MessageListener;
 import com.barchart.feed.ddf.datalink.enums.DDF_Transport;
 import com.barchart.feed.ddf.datalink.provider.DDF_FeedClientFactory;
 import com.barchart.feed.ddf.datalink.provider.DDF_Subscription;
 import com.barchart.feed.ddf.instrument.provider.InstrumentDBProvider;
 import com.barchart.feed.ddf.instrument.provider.InstrumentDatabaseMap;
 import com.barchart.feed.ddf.instrument.provider.DDF_InstrumentProvider;
 import com.barchart.feed.ddf.message.api.DDF_BaseMessage;
 import com.barchart.feed.ddf.message.api.DDF_ControlTimestamp;
 import com.barchart.feed.ddf.message.api.DDF_MarketBase;
 import com.barchart.util.value.api.Factory;
 import com.barchart.util.value.api.FactoryLoader;
 import com.barchart.util.values.api.Value;
 
 public class TestableFeed implements Marketplace {
 
 	private static final Logger log = LoggerFactory
 			.getLogger(TestableFeed.class);
 	
 	/* Value api factory */
 	private static final Factory factory = FactoryLoader.load();
 	
 	/* Used if unable to retrieve system default temp directory */
 	private static final String TEMP_DIR = "C:\\windows\\temp\\";
 	private final File dbFolder;
 	private static final long DB_UPDATE_TIMEOUT = 60; // seconds
 	
 	private final DDF_FeedClientBase connection;
 	private final TestableMarketplace maker;
 	private final ExecutorService executor;
 	
 	private volatile InstrumentDatabaseMap dbMap = null;
 	
 	@SuppressWarnings("unused")
 	private volatile Connection.Monitor stateListener;
 	
 	private final CopyOnWriteArrayList<TimestampListener> timeStampListeners =
 			new CopyOnWriteArrayList<TimestampListener>();
 	
 	public TestableFeed(final String username, final String password) {
 		
 		executor = Executors.newFixedThreadPool(100, 
 				
 				new ThreadFactory() {
 
 			final AtomicLong counter = new AtomicLong(0);
 			
 			@Override
 			public Thread newThread(final Runnable r) {
 				
 				final Thread t = new Thread(r, "Feed thread " + 
 						counter.getAndIncrement()); 
 				
 				t.setDaemon(true);
 				
 				return t;
 			}
 			
 		});
 		
 		dbFolder = getTempFolder();
 		
 		connection = DDF_FeedClientFactory.newConnectionClient(
 				DDF_Transport.TCP, username, password, executor);
 		
 		connection.bindMessageListener(msgListener);
 		
 		maker = TestableMarketplace.newTestableInstance(connection);
 		maker.add(instrumentSubscriptionListener);
 		
 	}
 	
 	private final MarketRegListener instrumentSubscriptionListener = new MarketRegListener() {
 
 		@Override
 		public void onRegistrationChange(final Map<Instrument, Set<MarketEvent>> 
 				instMap) {
 
 			final Set<Subscription> subs = new HashSet<Subscription>();
 			final Set<Subscription> unsubs = new HashSet<Subscription>();
 
 			for(final Entry<Instrument, Set<MarketEvent>> e: instMap.entrySet()) {
 
 				/*
 				 * The market maker denotes 'unsubscribe' with an empty event set
 				 */
 				if (e.getValue().isEmpty()) {
 					log.debug("Unsubscribing to "
 							+ e.getKey().symbol());
 					unsubs.add(new DDF_Subscription(e.getKey(), e.getValue()));
 				} else {
 					log.debug("Subscribing to "
 							+ e.getKey().symbol() + " Events: "
 							+ printEvents(e.getValue()));
 					subs.add(new DDF_Subscription(e.getKey(), e.getValue()));
 				}
 
 			}
 
 			if(!unsubs.isEmpty()) {
 				connection.unsubscribe(unsubs);
 			}
 			connection.subscribe(subs);
 
 		}
 	};
 	
 	private String printEvents(final Set<MarketEvent> events) {
 		final StringBuffer sb = new StringBuffer();
 
 		for (final MarketEvent me : events) {
 			sb.append(me.name() + ", ");
 		}
 
 		return sb.toString();
 	}
 	
 	/*
 	 * This is the default message listener. Users wishing to handle raw
 	 * messages will need to implement their own feed client.
 	 */
 	private final DDF_MessageListener msgListener = new DDF_MessageListener() {
 
 		@Override
 		public void handleMessage(final DDF_BaseMessage message) {
 
 			if (message instanceof DDF_ControlTimestamp) {
 				for (final TimestampListener listener : timeStampListeners) {
 					listener.listen(factory.newTime(((DDF_ControlTimestamp) message)
 							.getStampUTC().asMillisUTC(), ""));
 				}
 			}
 
 			if (message instanceof DDF_MarketBase) {
 				final DDF_MarketBase marketMessage = (DDF_MarketBase) message;
 				maker.make(marketMessage);
 			}
 
 		}
 
 	};
 	
 	private final AtomicBoolean isStartingup = new AtomicBoolean(false);
 	private final AtomicBoolean isShuttingdown = new AtomicBoolean(false);
 	
 	@Override
 	public void startup() {
 		
 		// Consider dummy future?
 		if(isStartingup.get()) {
 			throw new IllegalStateException("Startup called while already starting up");
 		}
 		
 		if(isShuttingdown.get()) {
 			throw new IllegalStateException("Startup called while shutting down");
 		}
 		
 		isStartingup.set(true);
 		
 		executor.execute(new StartupRunnable());
 		
 	}
 	
 	private final class StartupRunnable implements Runnable {
 
 		/*
 		 * Ensures instrument database is installed/updated before
 		 * user is able to send subscriptions to JERQ
 		 */
 		@Override
 		public void run() {
 			
 			try {
 			
 				dbMap = InstrumentDBProvider.getMap(dbFolder);
 				
 				DDF_InstrumentProvider.bindDatabaseMap(dbMap);
 				
 				final Future<Boolean> dbUpdate = executor.submit(
 						InstrumentDBProvider.updateDBMap(dbFolder, dbMap));
 				
 				dbUpdate.get(DB_UPDATE_TIMEOUT, TimeUnit.SECONDS);
 				
 				connection.startup();
 			
 			} catch (final Throwable t) {
 				
 				isStartingup.set(false);
 				
 				return;
 			}
 			
 			isStartingup.set(false);
 			
 		}
 		
 	}
 
 	@Override
 	public void shutdown() {
 		
 		// Consider dummy future?
 		if(isStartingup.get()) {
 			throw new IllegalStateException("Shutdown called while shutting down");
 		}
 		
 		if(isShuttingdown.get()) {
 			throw new IllegalStateException("Shutdown called while already shutting down");
 		}
 		
 		isShuttingdown.set(true);
 		
 		executor.execute(new ShutdownRunnable());
 		
 	}
 	
 	private final class ShutdownRunnable implements Runnable {
 
 		@Override
 		public void run() {
 			
 			try {
 				
 				if(maker != null) {
 					maker.clearAll();
 				}
 				
 				if(dbMap != null) {
 					dbMap.close();
 				}
 	
 				connection.shutdown();
 				
 				log.debug("Barchart Feed shutdown");
 				
 			} catch (final Throwable t) {
 				
 				log.error(t.getMessage());
 				
 				isShuttingdown.set(false);
 				
 				return;
 			}
 			
 			
 			isShuttingdown.set(false);
 			
 			log.debug("Barchart Feed shutdown succeeded");
 			
 		}
 		
 	}
 
 	@Override
 	public void bindConnectionStateListener(Connection.Monitor listener) {
 		
 		stateListener = listener;
 
 		if (connection != null) {
 			connection.bindStateListener(listener);
 		} else {
 			throw new RuntimeException("Connection state listener already bound");
 		}
 		
 	}
 
 	@Override
 	public void bindTimestampListener(TimestampListener listener) {
 		
 		if(listener != null) {
 			timeStampListeners.add(listener);
 		}
 		
 	}
 
 	@Override
 	public <V extends MarketData<V>> Agent newAgent(Class<V> dataType,
 			MarketObserver<V> callback) {
 		return maker.newAgent(dataType, callback);
 	}
 
 	@Override
 	public <V extends MarketData<V>> Agent subscribe(Class<V> clazz,
 			MarketObserver<V> callback, String... symbols) {
 		final Agent agent = newAgent(clazz, callback);
 		
 		agent.include(symbols);
 		
 		return agent;
 	}
 
 	@Override
 	public <V extends MarketData<V>> Agent subscribe(Class<V> clazz,
 			MarketObserver<V> callback, Instrument... instruments) {
 		
 		final Agent agent = newAgent(clazz, callback);
 		
 		agent.include(instruments);
 		
 		return agent;
 	}
 
 	@Override
 	public <V extends MarketData<V>> Agent subscribe(Class<V> clazz,
 			MarketObserver<V> callback, Exchange... exchanges) {
 		final Agent agent = newAgent(clazz, callback);
 		
 		agent.include(exchanges);
 		
 		return agent;
 	}
 
 	@Override
 	public Agent subscribeMarket(MarketObserver<Market> callback,
 			String... symbols) {
 		final Agent agent = newAgent(Market.class, callback);
 		
 		agent.include(symbols);
 		
 		return agent;
 	}
 
 	@Override
 	public Agent subscribeTrade(MarketObserver<Trade> lastTrade,
 			String... symbols) {
 		final Agent agent = newAgent(Trade.class, lastTrade);
 		
 		agent.include(symbols);
 		
 		return agent;
 	}
 
 	@Override
 	public Agent subscribeBook(MarketObserver<Book> book,
 			String... symbols) {
 		final Agent agent = newAgent(Book.class, book);
 		
 		agent.include(symbols);
 		
 		return agent;
 	}
 
 	@Override
 	public Agent subscribeCuvol(MarketObserver<Cuvol> cuvol, String... symbols) {
 		final Agent agent = newAgent(Cuvol.class, cuvol);
 		
 		return agent;
 	}
 
 	/*
 	 * Returns the default temp folder
 	 */
 	private static File getTempFolder() {
 		
 		try {
 			
 			return File.createTempFile("temp", null).getParentFile();
 			
 		} catch (IOException e) {
 			log.warn("Unable to retrieve system temp folder, using default {}", 
 					TEMP_DIR);
 			return new File(TEMP_DIR);
 		}
 		
 	}
 
 	public <V extends Value<V>> boolean addTaker(final MarketTaker<V> taker) {
 		return maker.register(taker);
 	}
 	
 	public <V extends Value<V>> boolean updateTaker(final MarketTaker<V> taker) {
 		return maker.update(taker);
 	}
 	
 	public <V extends Value<V>> boolean removeTaker(final MarketTaker<V> taker) {
 		return maker.unregister(taker);
 	}
 
 	@Override
 	public Market snapshot(Instrument instrument) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Market snapshot(String symbol) {
 		throw new UnsupportedOperationException();
 	}

	@Override
	public Market snapshot(Identifier instID) {
		// TODO Auto-generated method stub
		return null;
	}
 	
 }
