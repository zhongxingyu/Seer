 /*
  * Copyright (c) 2013 mgm technology partners GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.mgmtp.perfload.core.console;
 
 import static com.google.common.collect.Lists.newArrayListWithCapacity;
 import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
 import static org.apache.commons.io.IOUtils.closeQuietly;
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.net.ConnectException;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.MessageEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.ParameterException;
 import com.google.common.collect.ImmutableList;
 import com.mgmtp.perfload.core.clientserver.client.Client;
 import com.mgmtp.perfload.core.clientserver.client.ClientMessageListener;
 import com.mgmtp.perfload.core.clientserver.client.DefaultClient;
 import com.mgmtp.perfload.core.common.clientserver.Payload;
 import com.mgmtp.perfload.core.common.clientserver.PayloadType;
 import com.mgmtp.perfload.core.common.config.ProcessConfig;
 import com.mgmtp.perfload.core.common.config.ProcessKey;
 import com.mgmtp.perfload.core.common.config.TestConfig;
 import com.mgmtp.perfload.core.common.config.TestJar;
 import com.mgmtp.perfload.core.common.config.TestplanConfig;
 import com.mgmtp.perfload.core.common.config.XmlConfigReader;
 import com.mgmtp.perfload.core.common.util.MemoryInfo;
 import com.mgmtp.perfload.core.common.util.MemoryInfo.Unit;
 import com.mgmtp.perfload.core.common.util.StatusInfo;
 import com.mgmtp.perfload.core.console.meta.LtMetaInfo;
 import com.mgmtp.perfload.core.console.meta.LtMetaInfoHandler;
 import com.mgmtp.perfload.core.console.model.Daemon;
 import com.mgmtp.perfload.core.console.status.FileStatusTransformer;
 import com.mgmtp.perfload.core.console.status.StatusHandler;
 import com.mgmtp.perfload.core.console.status.StatusTransformer;
 
 /**
  * Represents perfLoad's management console. This class is responsible for running and controlling
  * load tests.
  * 
  * @author rnaegele
  */
 public final class LtConsole {
 
 	private static final Logger LOG = LoggerFactory.getLogger(LtConsole.class);
 
 	private final Map<Integer, Client> clients = newHashMapWithExpectedSize(4);
 
 	private volatile CountDownLatch connectLatch;
 	private volatile CountDownLatch jarLatch;
 	private volatile CountDownLatch readyLatch;
 	private volatile CountDownLatch doneLatch;
 	private volatile CountDownLatch clientCountLatch;
 
 	private final TestplanConfig config;
 	private final ExecutorService execService;
 	private final StatusHandler statusHandler = new StatusHandler();
 	private final StatusTransformer statusTransformer;
 	private final LtMetaInfoHandler metaInfoHandler;
 	private final List<Daemon> daemons;
 	private final boolean shutdownDaemons;
 	private final boolean abortTest;
 	private final long loadProfileTestTimeout;
 
 	private volatile int clientCount;
 	private volatile boolean testSuccessful = true;
 
 	private long startTimestamp;
 	private long finishTimestamp;
 
 	/**
 	 * Creates a new instance.
 	 * 
 	 * @param config
 	 *            the test configuration
 	 * @param execService
 	 *            {@link ExecutorService} handling asynchronous tasks such as status queue polling
 	 * @param metaInfoHandler
 	 *            creates and writes out meta information about the test for reporting use
 	 * @param daemons
 	 *            daemon hosts/ports for the test
 	 * @param shutdownDaemons
 	 *            if {@code true}, daemons are shut down after the test
 	 * @param loadProfileTestTimeout
 	 *            timeout in milliseconds for aborting a load profile test, calculated by adding
 	 *            this value to the start time of the last load profile event (zero for no timeout)
 	 */
 	public LtConsole(final TestplanConfig config, final ExecutorService execService, final StatusTransformer statusTransformer,
 			final LtMetaInfoHandler metaInfoHandler, final List<Daemon> daemons, final boolean shutdownDaemons,
 			final boolean abortTest, final long loadProfileTestTimeout) {
 		this.config = config;
 		this.execService = execService;
 		this.statusTransformer = statusTransformer;
 		this.metaInfoHandler = metaInfoHandler;
 		this.daemons = daemons;
 		this.shutdownDaemons = shutdownDaemons;
 		this.abortTest = abortTest;
 		this.loadProfileTestTimeout = loadProfileTestTimeout;
 
 		int totalProcessCount = config.getTotalProcessCount();
 		connectLatch = new CountDownLatch(totalProcessCount);
 		jarLatch = new CountDownLatch(daemons.size() * config.getTestJars().size());
 		readyLatch = new CountDownLatch(totalProcessCount);
 		doneLatch = new CountDownLatch(totalProcessCount);
 
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 			@Override
 			public void run() {
 				if (doneLatch != null && doneLatch.getCount() > 0) {
 					LOG.warn("Aborting test...");
 					abortTest();
 					try {
 						while (connectLatch.getCount() > 0) {
 							// Need to count down connectLatch because test terminated abnormally
 							connectLatch.countDown();
 						}
 						while (readyLatch.getCount() > 0) {
 							// Need to count down readyLatch because test terminated abnormally
 							readyLatch.countDown();
 						}
 						doneLatch.countDown();
 					} catch (Exception ex) {
 						LOG.error(ex.getMessage(), ex);
 					}
 					for (Client client : clients.values()) {
 						LOG.info("Disconnecting from daemon {}", client.getClientId());
 						client.disconnect();
 					}
 					LOG.info("Done.");
 				}
 			}
 		});
 	}
 
 	/**
 	 * Executes the load test carrying out the following steps:
 	 * <ol>
 	 * <li>load test configuration</li>
 	 * <li>set up daemon clients</li>
 	 * <li>connect to daemons</li>
 	 * <li>send test jars to daemons</li>
 	 * <li>create test processes</li>
 	 * <li>send configuration to test processes</li>
 	 * <li>run test</li>
 	 * <li>disconnect from daemons</li>
 	 * </ol>
 	 */
 	public void execute() throws Exception {
 		LOG.info("Console started.");
 
 		setUpDaemonClients();
 		connectToDaemons();
 
 		if (abortTest) {
 			abortTest();
 			while (clientCount != clients.size()) {
 				LOG.info("Waiting for test to be aborted...");
 				fetchClientCount();
 				Thread.sleep(2000L);
 			}
 			while (doneLatch.getCount() > 0) {
 				doneLatch.countDown();
 			}
 			LOG.info("Test aborted!");
 		} else {
 			sendJars();
 			createTestProcesses();
 			sendConfiguration();
 			runTest();
 			runStatusTransformer();
 
 			if (loadProfileTestTimeout > 0) {
 				addAbortionTimeoutWatcher();
 			}
 		}
 
 		disconnectFromDaemons();
 		if (!abortTest && metaInfoHandler != null) {
 			LOG.info("Creating meta information...");
 			LtMetaInfo metaInfo = metaInfoHandler.createMetaInformation(startTimestamp, finishTimestamp, config, daemons);
 			PrintWriter pr = null;
 			try {
 				File metaFile = new File("perfload.meta.utf8.props");
 				LOG.info("Dumping meta information to '{}'...", metaFile);
 				pr = new PrintWriter(metaFile, "UTF-8");
 				metaInfoHandler.dumpMetaInfo(metaInfo, pr);
 			} finally {
 				closeQuietly(pr);
 			}
 		}
 
 		LOG.info("Exiting...");
 	}
 
 	private void setUpDaemonClients() {
 		LOG.info("Setting up daemon clients.");
 
 		ClientMessageListener listener = new ClientMessageListener() {
 			@Override
 			public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) {
 				Payload payload = (Payload) e.getMessage();
 				LOG.debug("Received payload: " + payload);
 
 				switch (payload.getPayloadType()) {
 					case ERROR:
 						LOG.warn("Aborting test...");
 						testSuccessful = false;
 						abortTest();
 						break;
 					case JAR:
 						jarLatch.countDown();
 						break;
 					case TEST_PROC_CONNECTED:
 						connectLatch.countDown();
 						break;
 					case TEST_PROC_DISCONNECTED:
 						LOG.info("Client process disconnected: {}", payload.getContent());
 						break;
 					case TEST_PROC_READY:
 						readyLatch.countDown();
 						break;
 					case TEST_PROC_STARTED: {
 						ProcessConfig procConf = payload.getContent();
 						LOG.info("Test process created: {}", procConf);
 						break;
 					}
 					case TEST_PROC_TERMINATED: {
 						ProcessConfig procConf = payload.getContent();
 						LOG.info("Test process terminated: {}", procConf);
 						while (connectLatch.getCount() > 0) {
 							// Need to count down connectLatch because test terminated abnormally
 							connectLatch.countDown();
 						}
 						while (readyLatch.getCount() > 0) {
 							// Need to count down readyLatch because test terminated abnormally
 							readyLatch.countDown();
 						}
 						doneLatch.countDown();
 						break;
 					}
 					case CLIENT_COUNT:
 						int count = payload.getContent();
 						clientCount += count;
 						clientCountLatch.countDown();
 						break;
 					case STATUS:
 						StatusInfo si = payload.getContent();
 						LOG.debug("Received status info: {}", si);
 						statusHandler.addStatusInfo(si);
 						break;
 					default:
 						//
 				}
 
 				LOG.debug(MemoryInfo.getMemoryInfo(Unit.KIBIBYTES));
 			}
 		};
 
 		for (Daemon daemon : daemons) {
 			int daemonId = daemon.getId();
 			String clientId = "console" + (abortTest ? "Aborter" : "") + daemonId;
 			Client client = new DefaultClient(clientId, daemon.getHost(), daemon.getPort());
 			client.addClientMessageListener(listener);
 			clients.put(daemonId, client);
 		}
 	}
 
 	private void connectToDaemons() throws ConnectException {
 		LOG.info("Connecting to daemons...");
 
 		boolean connected = true;
 		for (Client client : clients.values()) {
 			LOG.info("Connecting to daemon {}", client.getClientId());
 			client.connect();
 			if (!client.isConnected()) {
 				connected = false;
 				LOG.error("Could not connect to daemon: {}", client.getClientId());
 			}
 		}
 
 		if (!connected) {
 			// set to null so the shutdown hook will not try to abort and disconnect
 			doneLatch = null;
 			throw new ConnectException("Could not connect to at least one daemon.");
 		}
 	}
 
 	private void sendJars() throws InterruptedException, TimeoutException {
 		List<TestJar> testJars = config.getTestJars();
 		if (testJars.isEmpty()) {
 			while (jarLatch.getCount() > 0) {
 				jarLatch.countDown();
 			}
 		}
 
 		for (Daemon daemon : daemons) {
 			int daemonId = daemon.getId();
 			LOG.info("Transferring jars to daemon {}", daemonId);
 			Client client = clients.get(daemonId);
 
 			// Send jars separately in order to save memory
 			for (TestJar jar : testJars) {
 				LOG.debug("Transferring jar file: {}", jar.getName());
 				// Await sending of the message. Otherwise jars might pile up in memory causing an OOME.
 				if (!client.sendMessage(new Payload(PayloadType.JAR, jar)).await(30L, TimeUnit.SECONDS)) {
 					throw new TimeoutException("Timeout waiting for jar to be sent.");
 				}
 			}
 		}
 	}
 
 	private void createTestProcesses() throws InterruptedException, TimeoutException {
 		awaitLatch(jarLatch, "Timeout waiting for jars to be transferred to daemons.");
 
 		LOG.info("Starting test processes...");

		for (Daemon daemon : daemons) {
			int daemonId = daemon.getId();
			LOG.debug("Starting test processes on daemon {}", daemonId);
			for (ProcessConfig pc : config.getProcessConfigs()) {
				LOG.debug("Starting test process: {}", pc);
				clients.get(pc.getDaemonId()).sendMessage(new Payload(PayloadType.CREATE_TEST_PROC, pc));
			}
 		}
 	}
 
 	private void sendConfiguration() throws InterruptedException, TimeoutException {
 		awaitLatch(connectLatch, "Timeout waiting for configuration to be sent to clients.");
 
 		LOG.info("Transferring test configuration");
 
 		for (Daemon daemon : daemons) {
 			int daemonId = daemon.getId();
 			// Send test config
 			LOG.debug("Transferring test configuration to daemon {}", daemonId);
 			Client client = clients.get(daemonId);
 
 			for (Entry<ProcessKey, TestConfig> entry : config.getTestConfigs().entrySet()) {
 				if (entry.getKey().getDaemonId() == daemonId) {
 					client.sendMessage(new Payload(PayloadType.CONFIG, entry.getValue()));
 				}
 			}
 		}
 	}
 
 	private void runTest() throws InterruptedException, TimeoutException {
 		awaitLatch(readyLatch, "Timeout waiting until test is ready to be started.");
 
 		startTimestamp = System.currentTimeMillis();
 		LOG.info("Running test...");
 
 		Payload payload = new Payload(PayloadType.START);
 		for (Client client : clients.values()) {
 			LOG.debug("Sending START signal to daemon {}", client.getClientId());
 			client.sendMessage(payload);
 		}
 	}
 
 	private void runStatusTransformer() {
 		if (statusTransformer != null) {
 			execService.submit(new Runnable() {
 				@Override
 				public void run() {
 					while (!Thread.currentThread().isInterrupted()) {
 						try {
 							statusTransformer.execute(statusHandler.getStatusInfoMap(), statusHandler.getThreadActivitiesMap());
 							Thread.sleep(1000L);
 						} catch (InterruptedException ex) {
 							Thread.currentThread().interrupt();
 						}
 					}
 				}
 			});
 		}
 	}
 
 	private void disconnectFromDaemons() throws InterruptedException {
 		doneLatch.await();
 		finishTimestamp = System.currentTimeMillis();
 
 		LOG.info("Waiting for status polling to complete...");
 
 		Thread.sleep(5000L);
 		execService.shutdownNow();
 
 		LOG.info("Disconnecting from daemons...");
 		for (Client client : clients.values()) {
 			LOG.info("Disconnecting from daemon {}", client.getClientId());
 			client.sendMessage(new Payload(PayloadType.CONSOLE_DISCONNECTING));
 
 			if (shutdownDaemons) {
 				LOG.info("Shutting down daemon {}", client.getClientId());
 				client.sendMessage(new Payload(PayloadType.SHUTDOWN_DAEMON));
 			}
 
 			client.disconnect();
 		}
 	}
 
 	private void abortTest() {
 		LOG.info("Aborting test...");
 
 		Payload payload = new Payload(PayloadType.ABORT);
 		for (Client client : clients.values()) {
 			LOG.debug("Sending ABORT signal to daemon {}", client.getClientId());
 			client.sendMessage(payload);
 		}
 	}
 
 	private void fetchClientCount() throws InterruptedException {
 		clientCountLatch = new CountDownLatch(clients.size());
 		clientCount = 0;
 
 		for (Client client : clients.values()) {
 			client.sendMessage(new Payload(PayloadType.CLIENT_COUNT));
 		}
 		clientCountLatch.await(1L, TimeUnit.MINUTES);
 	}
 
 	private void awaitLatch(final CountDownLatch latch, final String message) throws InterruptedException, TimeoutException {
 		if (!latch.await(2L, TimeUnit.MINUTES)) {
 			throw new TimeoutException(message);
 		}
 	}
 
 	public boolean isTestSuccessful() {
 		return testSuccessful;
 	}
 
 	private void addAbortionTimeoutWatcher() {
 		Runnable abortionWatcher = new Runnable() {
 			@Override
 			public void run() {
 				try {
 					Thread.sleep(config.getStartTimeOfLastEvent() + loadProfileTestTimeout);
 					if (doneLatch.getCount() > 0) { // if zero, test is already done
 						LOG.warn("Load profile test timed out!");
 						abortTest();
 					}
 				} catch (InterruptedException e) {
 					// this happens when the sleep is interrupted and an abortion is not necessary
 				}
 			}
 		};
 		execService.submit(abortionWatcher);
 	}
 
 	public static void main(final String[] args) {
 		JCommander jCmd = null;
 		try {
 			LtConsoleArgs cliArgs = new LtConsoleArgs();
 			jCmd = new JCommander(cliArgs);
 			jCmd.parse(args);
 
 			LOG.info("Starting perfLoad Console...");
 			LOG.info(cliArgs.toString());
 
 			int daemonCount = cliArgs.daemons.size();
 			List<Daemon> daemons = newArrayListWithCapacity(daemonCount);
 			for (int i = 0; i < daemonCount; i++) {
 				// daemon id is 1-based
 				int daemonId = i + 1;
 				daemons.add(Daemon.fromHostAndPort(daemonId, cliArgs.daemons.get(i)));
 			}
 			daemons = ImmutableList.copyOf(daemons);
 
 			XmlConfigReader configReader = new XmlConfigReader(new File("."), cliArgs.testplan);
 			TestplanConfig config = configReader.readConfig();
 			int totalThreadCount = config.getTotalThreadCount();
 			StatusTransformer transformer = new FileStatusTransformer(totalThreadCount, new File("ltStatus.txt"), new File(
 					"ltThreads.txt"), "UTF-8");
 
 			LtMetaInfoHandler metaInfoHandler = new LtMetaInfoHandler();
 			LtConsole console = new LtConsole(config, Executors.newCachedThreadPool(), transformer, metaInfoHandler,
 					daemons, cliArgs.shutdownDaemons, cliArgs.abort, TimeUnit.MINUTES.toMillis(cliArgs.timeout));
 			console.execute();
 		} catch (ParameterException ex) {
 			jCmd.usage();
 			System.exit(1);
 		} catch (Exception ex) {
 			LOG.error(ex.getMessage(), ex);
 			System.exit(-1);
 		}
 	}
 }
