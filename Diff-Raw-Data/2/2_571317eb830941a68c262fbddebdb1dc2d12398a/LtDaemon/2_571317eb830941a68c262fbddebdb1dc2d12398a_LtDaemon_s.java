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
 package com.mgmtp.perfload.core.daemon;
 
 import static com.google.common.base.Joiner.on;
 import static com.google.common.collect.Lists.newArrayList;
 import static com.mgmtp.perfload.core.common.clientserver.ChannelPredicates.isConsoleChannel;
 import static com.mgmtp.perfload.core.common.clientserver.ChannelPredicates.isTestprocChannel;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ConcurrentSkipListSet;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.MessageEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.ParameterException;
 import com.google.common.base.Predicates;
 import com.mgmtp.perfload.core.clientserver.client.Client;
 import com.mgmtp.perfload.core.clientserver.client.ClientMessageListener;
 import com.mgmtp.perfload.core.clientserver.client.DefaultClient;
 import com.mgmtp.perfload.core.clientserver.server.DefaultServer;
 import com.mgmtp.perfload.core.clientserver.server.Server;
 import com.mgmtp.perfload.core.clientserver.server.ServerMessageListener;
 import com.mgmtp.perfload.core.clientserver.util.ChannelContainer;
 import com.mgmtp.perfload.core.common.clientserver.Payload;
 import com.mgmtp.perfload.core.common.clientserver.PayloadType;
 import com.mgmtp.perfload.core.common.config.ProcessConfig;
 import com.mgmtp.perfload.core.common.config.TestConfig;
 import com.mgmtp.perfload.core.common.config.TestJar;
 import com.mgmtp.perfload.core.common.util.StreamGobbler;
 import com.mgmtp.perfload.core.daemon.util.AbstractClientRunner;
 import com.mgmtp.perfload.core.daemon.util.ForkedProcessClientRunner;
 
 /**
  * Represents a perfLoad daemon process. A perfLoad daemon is responsible for spawning test
  * processes. It acts as a server that communicates with its clients (i. e. test processes and
  * management console).
  * 
  * @author rnaegele
  */
 public class LtDaemon {
 	private final CountDownLatch doneLatch = new CountDownLatch(1);
 	private final Server server;
 
 	/**
 	 * Creates a new instance.
 	 * 
 	 * @param clientDir
 	 *            the client's installation and working directory
 	 * @param abstractClientRunner
 	 *            the {@link AbstractClientRunner} implementation used to run client processes
 	 * @param server
 	 *            the {@link Server} implementation for the daemon
 	 */
 	public LtDaemon(final File clientDir, final AbstractClientRunner abstractClientRunner, final Server server) {
 		this.server = server;
 		this.server.addServerMessageListener(new DaemonMessageListener(clientDir, abstractClientRunner));
 	}
 
 	/**
 	 * Starts the daemon creating the server. Further actions are triggered by requests from
 	 * management console and test processes.
 	 */
 	public void execute() {
 		log().info("Initializing...");
 
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 			@Override
 			public void run() {
 				// Count down the latch, so the daemon can exit.
 				// Also executed when the user hits Ctrl-C
 				if (doneLatch != null) {
 					doneLatch.countDown();
 					try {
 						// TODO timeouts configurable?
 						Thread.sleep(2000L);
 					} catch (InterruptedException ex) {
 						//
 					}
 				}
 				server.shutdown();
 				log().info("Good bye.");
 			}
 		});
 
 		server.bind();
 
 		try {
 			// Keep the daemon alive
 			doneLatch.await();
 			log().info("Exiting...");
 		} catch (InterruptedException ex) {
 			// cannot normally happen
 			log().error(ex.getMessage(), ex);
 		}
 	}
 
 	public static void main(final String... args) {
 		JCommander jCmd = null;
 		try {
 			LtDaemonArgs cliArgs = new LtDaemonArgs();
 			jCmd = new JCommander(cliArgs);
 			jCmd.parse(args);
 
			log().info("Starting perfLoad Console...");
 			log().info(cliArgs.toString());
 
 			if (cliArgs.shutdown) {
 				shutdownDaemon(cliArgs.port);
 				System.exit(0);
 			}
 
 			// Client is expected next to the daemon
 			File clientDir = new File(new File(System.getProperty("user.dir")).getParentFile(), "client");
 			ExecutorService execService = Executors.newCachedThreadPool();
 			StreamGobbler gobbler = new StreamGobbler(execService);
 			Server server = new DefaultServer(cliArgs.port);
 			LtDaemon daemon = new LtDaemon(clientDir, new ForkedProcessClientRunner(execService, gobbler), server);
 			daemon.execute();
 			execService.shutdown();
 			execService.awaitTermination(10L, TimeUnit.SECONDS);
 			System.exit(0);
 		} catch (ParameterException ex) {
 			jCmd.usage();
 			System.exit(1);
 		} catch (Exception ex) {
 			log().error(ex.getMessage(), ex);
 			System.exit(-1);
 		}
 	}
 
 	private static Logger log() {
 		return LoggerFactory.getLogger(LtDaemon.class);
 	}
 
 	public static void shutdownDaemon(final int port) {
 		final CountDownLatch latch = new CountDownLatch(1);
 
 		Client client = new DefaultClient("shutdownClient", "localhost", port);
 		client.addClientMessageListener(new ClientMessageListener() {
 			@Override
 			public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) {
 				final Payload payload = (Payload) e.getMessage();
 				if (payload.getPayloadType() == PayloadType.SHUTDOWN_DAEMON) {
 					log().info("Successfully requested shutdown.");
 					latch.countDown();
 				}
 			}
 		});
 
 		log().info("Connecting to daemon at port {}...", port);
 		client.connect();
 		if (client.isConnected()) {
 			client.sendMessage(new Payload(PayloadType.SHUTDOWN_DAEMON));
 			client.disconnect();
 		} else {
 			log().info("Could not connect to daemon. Daemon probably not running.");
 		}
 		log().info("Good bye.");
 	}
 
 	private final class DaemonMessageListener implements ServerMessageListener {
 		private final ExecutorService execService = Executors.newCachedThreadPool();
 		private final Set<String> testJarNames = new ConcurrentSkipListSet<>();
 		private final File clientDir;
 		private final AbstractClientRunner abstractClientRunner;
 
 		private volatile File testLibDir;
 
 		public DaemonMessageListener(final File clientDir, final AbstractClientRunner abstractClientRunner) {
 			this.clientDir = clientDir;
 			this.abstractClientRunner = abstractClientRunner;
 
 		}
 
 		private void deleteTestLibDir() {
 			if (testLibDir != null) {
 				synchronized (this) {
 					if (testLibDir != null) {
 						log().debug("Deleting test lib directory: {}", testLibDir);
 						try {
 							FileUtils.forceDelete(testLibDir);
 						} catch (IOException ex) {
 							// ignored
 						}
 						testLibDir = null;
 					}
 				}
 			}
 		}
 
 		private void createTestLibDir() throws IOException {
 			if (testLibDir == null) {
 				synchronized (this) {
 					if (testLibDir == null) {
 						testLibDir = new File(clientDir, "lib_daemon_" + System.nanoTime());
 						log().debug("Creating test lib directory: {}", testLibDir);
 					}
 				}
 				FileUtils.forceMkdir(testLibDir);
 			}
 		}
 
 		@Override
 		public void messageReceived(final ChannelHandlerContext ctx, final ChannelContainer channelContainer,
 				final MessageEvent e) {
 
 			try {
 				final Payload payload = (Payload) e.getMessage();
 				switch (payload.getPayloadType()) {
 					case CONSOLE_DISCONNECTING:
 						deleteTestLibDir();
 						break;
 					case SHUTDOWN_DAEMON:
 						log().info("Shutdown was requested.");
 						e.getChannel().write(payload);
 						doneLatch.countDown();
 						break;
 					case ERROR:
 						deleteTestLibDir();
 						channelContainer.getChannel(isConsoleChannel()).write(payload);
 						break;
 					case CREATE_TEST_PROC:
 						execService.submit(new Runnable() {
 							@Override
 							public void run() {
 								Channel channel = e.getChannel();
 								try {
 									ProcessConfig pc = payload.getContent();
 
 									List<String> arguments = newArrayList();
 									arguments.add("-processId");
 									arguments.add(String.valueOf(pc.getProcessId()));
 									arguments.add("-daemonId");
 									arguments.add(String.valueOf(pc.getDaemonId()));
 									arguments.add("-daemonPort");
 									arguments.add(String.valueOf(server.getPort()));
 									if (!testJarNames.isEmpty()) {
 										arguments.add("-testLibDir");
 										arguments.add(testLibDir.getAbsolutePath());
 										arguments.add("-testJars");
 										arguments.add(on(';').join(testJarNames));
 									}
 
 									Future<Integer> runResult = abstractClientRunner.runClient(clientDir, pc, arguments);
 
 									channel.write(new Payload(PayloadType.TEST_PROC_STARTED, pc));
 
 									int exitCode = runResult.get();
 									if (exitCode != 0) {
 										// Logging is enough here
 										log().error("Client process terminated with an error.");
 									}
 
 									channel.write(new Payload(PayloadType.TEST_PROC_TERMINATED, pc));
 								} catch (Throwable th) {
 									log().error(th.getMessage(), th);
 									channel.write(new Payload(PayloadType.ERROR));
 								}
 							}
 						});
 						break;
 					case TEST_PROC_DISCONNECTED:
 						log().info("Client process disconnected: {}", payload.getContent());
 						e.getChannel().write(payload);
 						channelContainer.getChannel(isConsoleChannel()).write(payload);
 						break;
 					case STATUS:
 					case TEST_PROC_CONNECTED:
 					case TEST_PROC_READY:
 					case TEST_PROC_STARTED:
 					case TEST_PROC_TERMINATED:
 						channelContainer.getChannel(isConsoleChannel()).write(payload);
 						break;
 					case START:
 						for (Channel channel : channelContainer.getChannels(isTestprocChannel())) {
 							if (channel.isConnected()) {
 								channel.write(payload);
 							}
 						}
 						break;
 					case ABORT:
 						deleteTestLibDir();
 						for (Channel channel : channelContainer.getChannels(isTestprocChannel())) {
 							if (channel.isConnected()) {
 								channel.write(payload);
 							}
 						}
 						break;
 					case JAR:
 						TestJar jar = payload.getContent();
 						OutputStream os = null;
 						String jarName = jar.getName();
 						log().debug("Received jar file: {}", jarName);
 						try {
 							createTestLibDir();
 							os = new FileOutputStream(new File(testLibDir, jarName));
 							IOUtils.write(jar.getContent(), os);
 							log().debug("Received jar file to: {}", testLibDir);
 							testJarNames.add(jarName);
 
 							// Write an empty jar response back to the console, so it knows
 							// the jar was correctly received and stored.
 							e.getChannel().write(new Payload(PayloadType.JAR));
 						} catch (IOException ex) {
 							log().error("Error saving jar files: {}", jarName, ex);
 							e.getChannel().write(new Payload(PayloadType.ERROR));
 						} finally {
 							IOUtils.closeQuietly(os);
 						}
 						break;
 					case CLIENT_COUNT:
 						e.getChannel().write(new Payload(PayloadType.CLIENT_COUNT, channelContainer.getChannels().size()));
 						break;
 					case CONFIG:
 						TestConfig config = payload.getContent();
 						log().debug("Received test configuration");
 
 						String channelId = "testproc" + config.getProcessId();
 						Channel channel = channelContainer.getChannel(Predicates.equalTo(channelId));
 						log().debug("Forwarding test configuration to client '{}", channelId);
 						channel.write(new Payload(PayloadType.CONFIG, config));
 						break;
 					default:
 						//
 				}
 			} catch (Exception ex) {
 				log().error(ex.getMessage(), ex);
 				channelContainer.getChannel(isConsoleChannel()).write(new Payload(PayloadType.ERROR));
 			}
 		}
 	}
 }
