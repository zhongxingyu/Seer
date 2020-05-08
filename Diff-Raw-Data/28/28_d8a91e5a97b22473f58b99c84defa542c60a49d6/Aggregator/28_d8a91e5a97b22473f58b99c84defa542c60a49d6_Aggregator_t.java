 /*
  * Aggregator for the Owl Platform
  * Copyright (C) 2012 Robert Moore
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *  
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *  
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 package com.owlplatform.aggregator;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.HashMap;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.apache.mina.core.session.IdleStatus;
 import org.apache.mina.core.session.IoSession;
 import org.apache.mina.filter.codec.ProtocolCodecFilter;
 import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.owlplatform.common.SampleMessage;
 import com.owlplatform.sensor.SensorIoAdapter;
 import com.owlplatform.sensor.SensorIoHandler;
 import com.owlplatform.sensor.protocol.codecs.AggregatorSensorProtocolCodecFactory;
 import com.owlplatform.sensor.protocol.messages.HandshakeMessage;
 import com.owlplatform.solver.SolverIoAdapter;
 import com.owlplatform.solver.SolverIoHandler;
 import com.owlplatform.solver.protocol.codec.AggregatorSolverProtocolCodecFactory;
 import com.owlplatform.solver.protocol.messages.SubscriptionMessage;
 import com.owlplatform.solver.rules.SubscriptionRequestRule;
 
 /**
  * Implementation of a very simple GRAIL Aggregator.
  * 
  * @author Robert Moore
  * 
  */
 public final class Aggregator implements SensorIoAdapter, SolverIoAdapter {
 
 	/**
 	 * Default port for incoming sensor connections.
 	 */
 	public static final int SENSOR_LISTEN_PORT = 7007;
 
 	/**
 	 * Default port for incoming solver connections.
 	 */
 	public static final int SOLVER_LISTEN_PORT = 7008;
 
 	/**
 	 * Statistics formatting string.
 	 */
 	private static final String STATS_FORMAT_STRING = "Processed %d samples since last report.\n"
 			+ "Statistics\n"
 			+ "\tAvg. Process Time: %,1.3f ns\n"
 			+ "\tAvg. Sample Lifetime: %1.3f ms";
 
 	/**
 	 * How frequently to print out statistics to the log.
 	 */
 	private static final long STATS_REPORTING_DELAY = 10000L;
 
 	/**
 	 * How long to wait before declaring a sensor conection "dead". Because
 	 * there is no heartbeat back to the sensors, idle status is the only way to
 	 * know that they have disconnected. The value is in seconds.
 	 */
 	private static final int SENSOR_TIMEOUT = 600;
 
 	/**
 	 * Map of IoSessions to the solvers.
 	 */
 	private final ConcurrentHashMap<IoSession, CachingFilteringSolverInterface> solvers = new ConcurrentHashMap<IoSession, CachingFilteringSolverInterface>();
 
 	/**
 	 * Map of IoSessions to the sensors.
 	 */
 	private final ConcurrentHashMap<IoSession, SensorInterface> sensors = new ConcurrentHashMap<IoSession, SensorInterface>();
 
 	/**
 	 * Worker threads to process samples that have arrived from sensors.
 	 */
 	private final ExecutorService handlerPool = Executors
 			.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
 
 	/**
 	 * Global variable to track average processing time for samples.
 	 */
 	volatile long processingTime = 0;
 
 	/**
 	 * Global variable to track how long samples are queued before being sent to
 	 * solver buffers.
 	 */
 	volatile long sampleDelay = 0;
 
 	/**
 	 * Global variable to track how many samples were processed.
 	 */
 	volatile long numSamples = 0l;
 
 	/**
 	 * Timer for printing statistics information to the log.
 	 */
 	private final Timer statsTimer = new Timer();
 
 	/**
 	 * Acceptor for sensor connections.
 	 */
 	private final NioSocketAcceptor sensorAcceptor;
 
 	/**
 	 * Acceptor for solver connections.
 	 */
 	private final NioSocketAcceptor solverAcceptor;
 
 	/**
 	 * Configuration values for this aggregator.
 	 */
 	private AggregatorConfiguration configuration;
 
 	/**
 	 * Logging for this class.
 	 */
 	private static final Logger log = LoggerFactory.getLogger(Aggregator.class);
 
 	private static final String CONFIG_INFO = "name: Aggregator" + "\n"
 			+ "arguments: sensor_port solver_port";
 
 	public static final void printConfigInfo() {
 		System.out.println(CONFIG_INFO);
 	}
 
 	/**
 	 * Parses the command-line arguments and starts the aggregator.
 	 * 
 	 * @param args
 	 *            sensor port, solver port. If not specified, default values are
 	 *            used.
 	 */
 	public static void main(String[] args) {
 		int sensorPort = SENSOR_LISTEN_PORT;
 		int solverPort = SOLVER_LISTEN_PORT;
 
 		if (args.length > 0) {
 			if ("-?".equalsIgnoreCase(args[0])) {
 				printConfigInfo();
 				return;
 			}
 			sensorPort = Integer.parseInt(args[0]);
 			if (args.length > 1) {
 				solverPort = Integer.parseInt(args[1]);
 			}
 		}
 
 		// TODO: Remove this configuration set-up and load from disk
 		AggregatorConfiguration config = new AggregatorConfiguration();
 		config.setSensorListenPort(sensorPort);
 		config.setSolverListenPort(solverPort);
 
 		Aggregator agg = new Aggregator();
 		agg.setConfig(config);
 		agg.init();
 
 	}
 
 	/**
 	 * Prints a simple usage string to standard output (System.out).
 	 */
 	public static void printUsageInfo() {
 		System.out
 				.println("Accepts 4 optional parameters: <Sensor Port> <Solver Port> <Next Aggregator Host> <Next Aggregator Port>");
 	}
 
 	/**
 	 * Constructs a new Aggregator object and adds a shutdown hook to it.
 	 */
 	public Aggregator() {
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 			@Override
 			public void run() {
 				Aggregator.this.shutdown();
 			}
 		});
 
 		this.sensorAcceptor = new NioSocketAcceptor();
 		this.sensorAcceptor.setReuseAddress(true);
 		this.solverAcceptor = new NioSocketAcceptor();
 		this.solverAcceptor.setReuseAddress(true);
 	}
 
 	/**
 	 * Sets the configuration values for this aggregator.
 	 * 
 	 * @param serverConfig
 	 *            the new configuration values for this aggregator. These are
 	 *            not used if {@link #init()} has already been invoked.
 	 */
 	public void setConfig(final AggregatorConfiguration serverConfig) {
 		this.configuration = serverConfig;
 	}
 
 	/**
 	 * Starts this aggregator.
 	 */
 	public void init() {
 		SolverIoHandler solverIoHandler = new SolverIoHandler(this);
 		// SensorIoHandler sensorIoHandler = new ThreadedSensorIoHandler(this);
 		SensorIoHandler sensorIoHandler = new SensorIoHandler(this);
 
 		this.statsTimer.scheduleAtFixedRate(new TimerTask() {
 
 			private Logger timeLog = LoggerFactory
 					.getLogger("Aggregator.statstimer");
 
 			@Override
 			public void run() {
 				float avgProcTime = (float) Aggregator.this.processingTime
 						/ Aggregator.this.numSamples;
 				float avgSampleTime = (float) Aggregator.this.sampleDelay
 						/ Aggregator.this.numSamples;
 				HashMap<IoSession, Integer> lostSamples = new HashMap<IoSession, Integer>();
				for (IoSession sess : Aggregator.this.solvers.keySet()) {
					CachingFilteringSolverInterface solver = Aggregator.this.solvers
							.get(sess);
					if (solver != null) {
						lostSamples.put(sess, Integer.valueOf(solver
								.getAndClearDroppedPackets()));
 					}
 				}

				StringBuilder sb = new StringBuilder(String.format(
						Aggregator.STATS_FORMAT_STRING,
 						Long.valueOf(Aggregator.this.numSamples),
 						Float.valueOf(avgProcTime),
 						Float.valueOf(avgSampleTime)));
				if (!lostSamples.isEmpty()) {
 					sb.append("\nSolver Loss Rate:");
					for (IoSession sess : lostSamples.keySet()) {
						sb.append("\n\t").append(sess.toString()).append(": ")
								.append(String.format("%,d",lostSamples.get(sess)));
 					}
 				}

 				this.timeLog.info(sb.toString());
 				Aggregator.this.numSamples = 0L;
 				Aggregator.this.processingTime = 0L;
 				Aggregator.this.sampleDelay = 0L;
 
 			}
 		}, STATS_REPORTING_DELAY, STATS_REPORTING_DELAY);
 		this.sensorAcceptor.getFilterChain().addLast(
 				"sensor codec",
 				new ProtocolCodecFilter(
 						new AggregatorSensorProtocolCodecFactory(true)));
 
 		this.solverAcceptor.getFilterChain().addLast(
 				"solver codec",
 				new ProtocolCodecFilter(
 						new AggregatorSolverProtocolCodecFactory(true)));
 
 		try {
 			this.sensorAcceptor.setHandler(sensorIoHandler);
 			this.sensorAcceptor.getSessionConfig().setTcpNoDelay(true);
 			this.sensorAcceptor.getSessionConfig().setIdleTime(
 					IdleStatus.READER_IDLE, SENSOR_TIMEOUT);
 			this.sensorAcceptor.bind(new InetSocketAddress(this.configuration
 					.getSensorListenPort()));
 		} catch (IOException e) {
 			log.error("Unable to bind to port {}.",
 					Integer.valueOf(this.configuration.getSensorListenPort()));
 			System.exit(1);
 		}
 
 		try {
 			this.solverAcceptor.setHandler(solverIoHandler);
 			this.solverAcceptor.bind(new InetSocketAddress(this.configuration
 					.getSolverListenPort()));
 		} catch (IOException ioe) {
 			log.error("Unable to bind to port {}.",
 					Integer.valueOf(this.configuration.getSolverListenPort()));
 			System.exit(1);
 		}
 		log.info("GRAIL Aggregator is listening for sensors on on port {}.",
 				Integer.valueOf(this.configuration.getSensorListenPort()));
 		log.info("GRAIL Aggregator is listening for solvers on on port {}.",
 				Integer.valueOf(this.configuration.getSolverListenPort()));
 	}
 
 	@Override
 	public void handshakeMessageReceived(final IoSession session,
 			final HandshakeMessage handshakeMessage) {
 		SensorInterface sensor = this.sensors.get(session);
 		if (sensor == null) {
 			log.error("Unable to retrieve sensor for {}", session);
 			return;
 		}
 
 		// this.sensorSampleReceived.put(session,
 		// Long.valueOf(System.currentTimeMillis()));
 
 		log.info("Received handshake message from sensor {}.", session);
 		sensor.setReceivedHandshake(handshakeMessage);
 
 		this.checkHandshakeMessages(sensor);
 
 	}
 
 	private boolean checkHandshakeMessages(final SensorInterface sensor) {
 		if (sensor.getSentHandshake() != null
 				&& sensor.getReceivedHandshake() != null) {
 			if (!sensor.getSentHandshake()
 					.equals(sensor.getReceivedHandshake())) {
 				StringBuffer sb = new StringBuffer();
 				sb.append(
 						"Handshake mis-match, closing connection.\nLocal:\n\t")
 						.append(sensor.getSentHandshake())
 						.append("\nRemote:\n\t")
 						.append(sensor.getReceivedHandshake());
 				IoSession session = sensor.getSession();
 				if (session == null) {
 					log.error("Sensor session was null for {}.", sensor);
 					return false;
 				}
 				this.sensors.remove(session);
 				session.close(true);
 				return false;
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public void sensorSampleReceived(final IoSession session,
 			final SampleMessage sampleMessage) {
 		++this.numSamples;
 		this.handlerPool.execute(new Runnable() {
 
 			@Override
 			public void run() {
 				Aggregator.this.handleSampleMessage(session, sampleMessage);
 			}
 		});
 
 	}
 
 	/**
 	 * @param session
 	 * @param sampleMessage
 	 */
 	protected void handleSampleMessage(final IoSession session,
 			final SampleMessage sampleMessage) {
 		long start = System.nanoTime();
 		this.sendSample(sampleMessage);
 		long nowNano = System.nanoTime();
 		long nowMilli = System.currentTimeMillis();
 		this.processingTime += nowNano - start;
 		this.sampleDelay += nowMilli - sampleMessage.getCreationTimestamp();
 	}
 
 	@Override
 	public void sensorConnected(final IoSession session) {
 		SensorInterface sensor = new SensorInterface();
 		sensor.setSession(session);
 
 		this.sensors.put(session, sensor);
 		log.info("{} connected.", sensor);
 
 		// this.sensorSampleReceived.put(session,
 		// Long.valueOf(System.currentTimeMillis()));
 
 		HandshakeMessage handshakeMsg = HandshakeMessage.getDefaultMessage();
 		session.write(handshakeMsg);
 
 	}
 
 	@Override
 	public void sensorDisconnected(final IoSession session) {
 		SensorInterface sensor = this.sensors.get(session);
 		if (sensor == null) {
 			log.error("Unable to retrieve disconnecting sensor for {}.",
 					session);
 		}
 		log.info("{} disconnected.", sensor);
 		this.sensors.remove(session);
 		// this.sensorSampleReceived.remove(session);
 
 	}
 
 	@Override
 	public void handshakeReceived(
 			final IoSession session,
 			final com.owlplatform.solver.protocol.messages.HandshakeMessage handshakeMessage) {
 		log.info("Received {} from {}.", handshakeMessage, session);
 	}
 
 	@Override
 	public void connectionOpened(final IoSession session) {
 		CachingFilteringSolverInterface solver = new CachingFilteringSolverInterface();
 		solver.setSession(session);
 		this.solvers.put(session, solver);
 		com.owlplatform.solver.protocol.messages.HandshakeMessage handshake = com.owlplatform.solver.protocol.messages.HandshakeMessage
 				.getDefaultMessage();
 		session.write(handshake);
 		log.info("Sent {} to {}.", handshake, solver);
 	}
 
 	@Override
 	public void connectionClosed(final IoSession session) {
 		this.solvers.remove(session);
 	}
 
 	@Override
 	public void subscriptionRequestReceived(final IoSession session,
 			final SubscriptionMessage subscriptionRequestMessage) {
 		CachingFilteringSolverInterface solver = this.solvers.get(session);
 		subscriptionRequestMessage
 				.setMessageType(SubscriptionMessage.RESPONSE_MESSAGE_ID);
 		session.write(subscriptionRequestMessage);
 		log.info("(Solver {}) Responded to subscription request with {}.",
 				solver, subscriptionRequestMessage);
 		solver.setSentSubscriptionResponse(true);
 
 		if (subscriptionRequestMessage.getRules() != null) {
 			for (SubscriptionRequestRule rule : subscriptionRequestMessage
 					.getRules()) {
 				solver.addEffectiveRule(rule);
 				log.info("Added {} to {}.", rule, solver);
 			}
 		}
 	}
 
 	public void sendSample(
 			final com.owlplatform.common.SampleMessage solverSample) {
 		for (CachingFilteringSolverInterface solver : this.solvers.values()) {
 			solver.sendSample(solverSample);
 		}
 	}
 
 	@Override
 	public void handshakeSent(
 			final IoSession session,
 			final com.owlplatform.solver.protocol.messages.HandshakeMessage handshakeMessage) {
 		// No-Op
 	}
 
 	@Override
 	public void sensorSampleSent(final IoSession session,
 			final SampleMessage sampleMessage) {
 		// No-Op
 	}
 
 	@Override
 	public void subscriptionResponseSent(final IoSession session,
 			final SubscriptionMessage subscriptionRequestMessage) {
 		// No-Op
 	}
 
 	@Override
 	public void sessionIdle(final IoSession session, final IdleStatus idleStatus) {
 		// Check sensor sessions
 		SensorInterface sensor = this.sensors.get(session);
 		if (sensor == null) {
 			return;
 		}
 		log.warn("{} is idle for {} seconds. Disconnecting.", sensor,
 				Integer.valueOf(SENSOR_TIMEOUT));
 		this.sensors.remove(sensor);
 		sensor.session.close(true);
 
 	}
 
 	@Override
 	public void handshakeMessageSent(final IoSession session,
 			final HandshakeMessage handshakeMessage) {
 		SensorInterface sensor = this.sensors.get(session);
 		if (sensor == null) {
 			log.error("No sensor available for {}.", session);
 			return;
 		}
 		log.info("Sent {} to {}.", handshakeMessage, sensor);
 
 		this.checkHandshakeMessages(sensor);
 	}
 
 	@Override
 	public void solverSampleSent(final IoSession session,
 			final com.owlplatform.common.SampleMessage sampleMessage) {
 		// No-Op
 	}
 
 	@Override
 	public void solverSampleReceived(final IoSession session,
 			final com.owlplatform.common.SampleMessage sampleMessage) {
 		// No-Op
 
 	}
 
 	@Override
 	public void subscriptionRequestSent(final IoSession session,
 			final SubscriptionMessage subsriptionMessage) {
 		// No-Op
 	}
 
 	@Override
 	public void subscriptionResponseReceived(final IoSession session,
 			final SubscriptionMessage subscriptionMessage) {
 		// No-Op
 	}
 
 	@Override
 	public void exceptionCaught(final IoSession session,
 			final Throwable exception) {
 		// No-Op
 	}
 
 	public void shutdown() {
 		if (!this.sensorAcceptor.isDisposed()) {
 			for (IoSession session : this.sensorAcceptor.getManagedSessions()
 					.values()) {
 				try {
 					session.close(false).await();
 				} catch (InterruptedException ie) {
 					// Ignored
 				}
 			}
 
 			this.sensorAcceptor.unbind();
 			this.sensorAcceptor.dispose();
 			log.info("{} disposed of sensor acceptor.", this);
 		}
 
 		if (!this.solverAcceptor.isDisposed()) {
 			for (IoSession session : this.solverAcceptor.getManagedSessions()
 					.values()) {
 				try {
 					session.close(false).await();
 				} catch (InterruptedException ie) {
 					// Ignored
 				}
 			}
 
 			this.solverAcceptor.unbind();
 			this.solverAcceptor.dispose();
 			log.info("{} disposed of solver acceptor.", this);
 		}
 		if (!this.handlerPool.isShutdown()) {
 			this.handlerPool.shutdownNow();
 			log.info("{} shut down workers.", this);
 		}
 		this.statsTimer.cancel();
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("Aggregator (");
 		sb.append(this.configuration.getSensorListenPort());
 		sb.append("/");
 		sb.append(this.configuration.getSolverListenPort());
 		sb.append(')');
 
 		return sb.toString();
 	}
 }
