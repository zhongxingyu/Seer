 /**********************************************************************************************************************
  * Copyright (c) 2010, Institute of Telematics, University of Luebeck                                                 *
  * All rights reserved.                                                                                               *
  *                                                                                                                    *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the   *
  * following conditions are met:                                                                                      *
  *                                                                                                                    *
  * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following *
  *   disclaimer.                                                                                                      *
  * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the        *
  *   following disclaimer in the documentation and/or other materials provided with the distribution.                 *
  * - Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or        *
  *   promote products derived from this software without specific prior written permission.                           *
  *                                                                                                                    *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, *
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE      *
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,         *
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE *
  * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF    *
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY   *
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                                *
  **********************************************************************************************************************/
 
 package de.uniluebeck.itm.wsn.deviceutils.listener;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Splitter;
 import com.google.common.util.concurrent.ThreadFactoryBuilder;
 import com.google.inject.Guice;
 import de.uniluebeck.itm.nettyprotocols.*;
 import de.uniluebeck.itm.util.StringUtils;
 import de.uniluebeck.itm.util.Tuple;
 import de.uniluebeck.itm.util.logging.LogLevel;
 import de.uniluebeck.itm.util.logging.Logging;
 import de.uniluebeck.itm.wsn.drivers.core.Device;
 import de.uniluebeck.itm.wsn.drivers.factories.DeviceFactory;
 import de.uniluebeck.itm.wsn.drivers.factories.DeviceFactoryModule;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.PosixParser;
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.channel.*;
 import org.jboss.netty.channel.iostream.IOStreamAddress;
 import org.jboss.netty.channel.iostream.IOStreamChannelFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.annotation.Nonnull;
 import java.io.*;
 import java.nio.charset.Charset;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import static com.google.common.collect.Lists.newArrayList;
 import static com.google.common.collect.Maps.newHashMap;
 import static de.uniluebeck.itm.wsn.deviceutils.CliUtils.assertParametersPresent;
 import static de.uniluebeck.itm.wsn.deviceutils.CliUtils.printUsageAndExit;
 import static org.jboss.netty.channel.Channels.pipeline;
 
 public class DeviceListenerCLI {
 
 	static {
 		Logging.setLoggingDefaults(LogLevel.WARN);
 	}
 
 	private final static Logger log = LoggerFactory.getLogger(DeviceListenerCLI.class);
 
 	private static final DeviceFactory deviceFactory = Guice
 			.createInjector(new DeviceFactoryModule())
 			.getInstance(DeviceFactory.class);
 
 	public static void main(String[] args) throws InterruptedException, IOException {
 
 		CommandLineParser parser = new PosixParser();
 		Options options = createCommandLineOptions();
 
 		String deviceType = null;
 		String port = null;
 		Map<String, String> configuration = newHashMap();
 
 		OutputStream outStream = System.out;
 		WriterHandler writerHandler = null;
 		@Nonnull List<Tuple<String, ChannelHandler>> handlers = newArrayList();
 
 		try {
 
 			CommandLine line = parser.parse(options, args, true);
 
 			if (line.hasOption('h')) {
 				printUsageAndExit(DeviceListenerCLI.class, options, 0);
 			}
 
 			if (line.hasOption('v')) {
 				Logging.setLogLevel(LogLevel.DEBUG);
 			}
 
 			if (line.hasOption('l')) {
 				Logging.setLogLevel(LogLevel.toLevel(line.getOptionValue('l')));
 			}
 
 			if (line.hasOption('c')) {
 				final String configurationFileString = line.getOptionValue('c');
 				final File configurationFile = new File(configurationFileString);
 				final Properties configurationProperties = new Properties();
 				configurationProperties.load(new FileReader(configurationFile));
 				for (Map.Entry<Object, Object> entry : configurationProperties.entrySet()) {
 					configuration.put((String) entry.getKey(), (String) entry.getValue());
 				}
 			}
 
 			assertParametersPresent(line, 't', 'p');
 
 			deviceType = line.getOptionValue('t');
 			port = line.getOptionValue('p');
 
 			if (line.hasOption('o')) {
 				String filename = line.getOptionValue('o');
 				log.info("Using outfile {}", filename);
 				outStream = new FileOutputStream(filename);
 			}
 
 			if (line.hasOption('e')) {
 
 				final String handlerNamesString = line.getOptionValue('e');
 
 				final Iterable<String> handlerNames = Splitter.on(",")
 						.omitEmptyStrings()
 						.trimResults()
 						.split(handlerNamesString);
 
 				final HandlerFactoryMap handlerFactories = Guice
 						.createInjector(new NettyProtocolsModule())
 						.getInstance(HandlerFactoryMap.class);
 
 				for (String handlerName : handlerNames) {
 
 					final NamedChannelHandlerList channelHandlers = handlerFactories
 							.get(handlerName)
 							.create(new ChannelHandlerConfig(handlerName));
 
 					for (NamedChannelHandler channelHandler : channelHandlers) {
 						handlers.add(
 								new Tuple<String, ChannelHandler>(
 										channelHandler.getInstanceName(),
 										channelHandler.getChannelHandler()
 								)
 						);
 					}
 				}
 			}
 
 			if (line.hasOption('f')) {
 
 				String format = line.getOptionValue('f');
 
 				if ("csv".equals(format)) {
 					writerHandler = new CsvWriter(outStream);
 				} else if ("wiseml".equals(format)) {
 					writerHandler = new WiseMLWriterHandler(outStream, "node at " + line.getOptionValue('p'), true);
 				} else if ("hex".equals(format)) {
 					writerHandler = new HexWriter(outStream);
 				} else if ("human".equals(format)) {
 					writerHandler = new HumanReadableWriter(outStream);
 				} else if ("utf8".equals(format) || "UTF-8".equals(format)) {
 					writerHandler = new StringWriter(outStream, Charset.forName("UTF-8"));
 				} else if ("iso".equals(format) || "ISO-8859-1".equals(format)) {
 					writerHandler = new StringWriter(outStream, Charset.forName("ISO-8859-1"));
 				} else {
 					throw new Exception("Unknown format " + format);
 				}
 
 				log.info("Using format {}", format);
 
 			} else {
 				writerHandler = new StringWriter(outStream, Charset.forName("US-ASCII"));
 			}
 
 		} catch (Exception e) {
			log.error("Invalid command line: {}", e.getMessage());
 			printUsageAndExit(DeviceListenerCLI.class, options, 1);
 		}
 
 		if (writerHandler == null) {
 			throw new RuntimeException("This should not happen!");
 		}
 
 		final ExecutorService executorService = Executors.newCachedThreadPool(
 				new ThreadFactoryBuilder().setNameFormat("DeviceListener-Thread %d").build()
 		);
 
 		final Device device = deviceFactory.create(executorService, deviceType, configuration);
 
 		device.connect(port);
 		if (!device.isConnected()) {
 			throw new RuntimeException("Connection to device at port \"" + args[1] + "\" could not be established!");
 		}
 
 		final InputStream inputStream = device.getInputStream();
 		final OutputStream outputStream = device.getOutputStream();
 
 		final ClientBootstrap bootstrap = new ClientBootstrap(new IOStreamChannelFactory(executorService));
 
 		final WriterHandler finalWriterHandler = writerHandler;
 		final List<Tuple<String, ChannelHandler>> finalHandlers = handlers;
 		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
 			@Override
 			public ChannelPipeline getPipeline() throws Exception {
 				final ChannelPipeline pipeline = pipeline();
 				for (Tuple<String, ChannelHandler> handler : finalHandlers) {
 					pipeline.addLast(handler.getFirst(), handler.getSecond());
 				}
 				pipeline.addLast("finalWriterHandler", finalWriterHandler);
 				return pipeline;
 			}
 		}
 		);
 
 		// Make a new connection.
 		ChannelFuture connectFuture = bootstrap.connect(new IOStreamAddress(inputStream, outputStream));
 
 		// Wait until the connection is made successfully.
 		final Channel channel = connectFuture.awaitUninterruptibly().getChannel();
 
 		Runtime.getRuntime().addShutdownHook(new Thread(DeviceListenerCLI.class.getName() + "-ShutdownThread") {
 			@Override
 			public void run() {
 				try {
 					channel.close();
 				} catch (Exception e) {
 					log.error("Exception while closing channel to device: {}", e, e);
 				}
 			}
 		}
 		);
 
 		while (!Thread.interrupted()) {
 
 			try {
 
 				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 				final byte[] cmdBytes = in.readLine().getBytes();
 				final byte[] bytes = new byte[cmdBytes.length + 1];
 				System.arraycopy(cmdBytes, 0, bytes, 0, cmdBytes.length);
 				bytes[cmdBytes.length] = 0x0a; // LF
 
 				device.getOutputStream().write(bytes);
 				System.out.println("SENT " + bytes.length + " bytes: " + StringUtils.toHexString(bytes));
 				device.getOutputStream().flush();
 				// device.getOutputStream().write(StringUtils.fromStringToByteArray(in.readLine()));
 
 			} catch (IOException e) {
 				log.error("{}", e);
 				System.exit(1);
 			}
 		}
 
 	}
 
 	private static Options createCommandLineOptions() {
 
 		Options options = new Options();
 
 		// add all available options
 		options.addOption("p", "port", true, "Serial port to which the device is attached");
 		options.getOption("p").setRequired(true);
 
 		options.addOption("t", "type", true, "Type of the device");
 		options.getOption("t").setRequired(true);
 
 		options.addOption("c", "configuration", true,
 				"Optional: file name of a configuration file containing key value pairs to configure the device"
 		);
 
 		options.addOption("e", "channelpipeline", true,
 				"Optional: comma-separated list of channel pipeline handler names"
 		);
 		options.addOption("f", "format", true, "Optional: output format, options: csv, wiseml");
 		options.addOption("o", "outfile", true, "Optional: redirect output to file");
 		options.addOption("v", "verbose", false, "Optional: verbose logging output (equal to -l DEBUG)");
 		options.addOption("l", "logging", true,
 				"Optional: set logging level (one of [" + Joiner.on(", ").join(Logging.LOG_LEVELS) + "])"
 		);
 		options.addOption("h", "help", false, "Optional: print help");
 
 		return options;
 	}
 }
