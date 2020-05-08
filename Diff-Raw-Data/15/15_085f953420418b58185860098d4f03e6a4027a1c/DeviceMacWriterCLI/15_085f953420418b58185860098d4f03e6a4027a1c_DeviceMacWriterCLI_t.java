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
 
 package de.uniluebeck.itm.wsn.deviceutils.macwriter;
 
 import com.google.common.base.Joiner;
 import com.google.common.io.Closeables;
 import com.google.common.util.concurrent.ThreadFactoryBuilder;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import de.uniluebeck.itm.tr.util.ExecutorUtils;
 import de.uniluebeck.itm.tr.util.Logging;
 import de.uniluebeck.itm.tr.util.StringUtils;
 import de.uniluebeck.itm.wsn.deviceutils.DeviceUtilsModule;
 import de.uniluebeck.itm.wsn.drivers.core.Device;
 import de.uniluebeck.itm.wsn.drivers.core.MacAddress;
 import de.uniluebeck.itm.wsn.drivers.core.operation.OperationListener;
 import de.uniluebeck.itm.wsn.drivers.core.operation.StateChangedEvent;
 import de.uniluebeck.itm.wsn.drivers.factories.DeviceFactory;
import de.uniluebeck.itm.wsn.drivers.factories.DeviceFactoryModule;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.PosixParser;
 import org.apache.log4j.Level;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileReader;
 import java.util.Map;
 import java.util.Properties;
 import java.util.concurrent.*;
 
 import static com.google.common.collect.Maps.newHashMap;
 import static de.uniluebeck.itm.wsn.deviceutils.CliUtils.assertParametersPresent;
 import static de.uniluebeck.itm.wsn.deviceutils.CliUtils.printUsageAndExit;
 
 public class DeviceMacWriterCLI {
 
 	private final static Level[] LOG_LEVELS = {Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR};
 
 	private static final Logger log = LoggerFactory.getLogger(DeviceMacWriterCLI.class);
 
 	public static void main(String[] args) throws Exception {
 
 		Logging.setLoggingDefaults(Level.WARN);
 
 		CommandLineParser parser = new PosixParser();
 		Options options = createCommandLineOptions();
 
 		String deviceType = null;
 		String port = null;
 		String macAddressLower16String = null;
 		Map<String,String> configuration = newHashMap();
 		boolean use16BitMode = true;
 
 		try {
 
 			CommandLine line = parser.parse(options, args, true);
 
 			if (line.hasOption('h')) {
 				printUsageAndExit(DeviceMacWriterCLI.class, options, 0);
 			}
 
 			if (line.hasOption('v')) {
 				org.apache.log4j.Logger.getRootLogger().setLevel(Level.DEBUG);
 			}
 
 			if (line.hasOption('l')) {
 				Level level = Level.toLevel(line.getOptionValue('l'));
 				org.apache.log4j.Logger.getRootLogger().setLevel(level);
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
 
 			assertParametersPresent(line, 't', 'p', 'm');
 
 			deviceType = line.getOptionValue('t');
 			port = line.getOptionValue('p');
 			macAddressLower16String = line.getOptionValue('m');
 			use16BitMode = !line.hasOption('x');
 
 		} catch (Exception e) {
 			log.error("Invalid command line: " + e);
 			printUsageAndExit(DeviceMacWriterCLI.class, options, 1);
 		}
 
 		long macAddressLower16 = StringUtils.parseHexOrDecLong(macAddressLower16String);
 		final MacAddress macAddress = new MacAddress(new byte[]{
 				0,
 				0,
 				0,
 				0,
 				0,
 				0,
 				(byte) (0xFF & (macAddressLower16 >> 8)),
 				(byte) (0xFF & (macAddressLower16))
 		}
 		);
 
 		final ExecutorService executorService = Executors.newCachedThreadPool(
 				new ThreadFactoryBuilder().setNameFormat("DeviceMacWriter %d").build()
 		);
 
		final Injector injector = Guice.createInjector(
				new DeviceFactoryModule(),
				new DeviceUtilsModule(executorService, null, use16BitMode)
		);
 		final Device device = injector.getInstance(DeviceFactory.class).create(executorService, deviceType, configuration);
 		
 		device.connect(port);
 		if (!device.isConnected()) {
 			throw new RuntimeException("Connection to device at port \"" + args[1] + "\" could not be established!");
 		}
 
 		final String finalDeviceType = deviceType;
 		final String finalPort = port;
 
 		OperationListener<Void> callback = new OperationListener<Void>() {
 			private int lastProgress = -1;
 
 			@Override
 			public void onProgressChange(float fraction) {
 				int newProgress = (int) Math.floor(fraction * 100);
 				if (lastProgress < newProgress) {
 					lastProgress = newProgress;
 					log.info("Progress: {}%", newProgress);
 				}
 			}
 
 			@Override
 			public void onSuccess(Void result) {
 				log.info("Writing MAC address {} of {} device at port {} done!",
 						new Object[] {macAddress, finalDeviceType, finalPort}
 				);
 			}
 
 			@Override
 			public void onFailure(Throwable throwable) {
 				log.error("Writing MAC address failed with Exception: " + throwable, throwable);
 			}
 
 			@Override
 			public void onExecute() {
 				log.info("Starting to write MAC address...");
 			}
 
 			@Override
 			public void onCancel() {
 				log.info("Writing MAC address was canceled!");
 			}
 
 			@Override
 			public void beforeStateChanged(final StateChangedEvent<Void> stateChangedEvent) {
 				log.info("Operation state about to change: {}", stateChangedEvent);
 			}
 
 			@Override
 			public void afterStateChanged(final StateChangedEvent<Void> stateChangedEvent) {
 				log.info("Operation state changed: {}", stateChangedEvent);
 			}
 		};
 
 		try{
 			device.writeMac(macAddress, 120000, callback).get();
 		} finally {
 			closeConnection(device, executorService);
 		}
 	}
 
 	private static void closeConnection(final Device device, final ExecutorService executorService) {
 		log.debug("Closing Device...");
 		Closeables.closeQuietly(device);
 
 		log.debug("Shutting down executor...");
 		ExecutorUtils.shutdown(executorService, 1, TimeUnit.SECONDS);
 	}
 
 	private static Options createCommandLineOptions() {
 
 		Options options = new Options();
 
 		// add all available options
 		options.addOption("p", "port", true, "Serial port to which the device is attached");
 		options.getOption("p").setRequired(true);
 
 		options.addOption("t", "type", true, "Type of the device");
 		options.getOption("t").setRequired(true);
 
 		options.addOption("m", "mac", true, "MAC address to write to the device");
 		options.getOption("m").setRequired(true);
 
 		options.addOption("x", "use64BitMode", false, "Set if you want to write the MAC in 64 bit mode");
 		options.getOption("x").setRequired(false);
 
 		options.addOption("c", "configuration", true,
 				"Optional: file name of a configuration file containing key value pairs to configure the device"
 		);
 		options.addOption("v", "verbose", false, "Optional: verbose logging output (equal to -l DEBUG)");
 		options.addOption("l", "logging", true,
 				"Optional: set logging level (one of [" + Joiner.on(", ").join(LOG_LEVELS) + "])"
 		);
 		options.addOption("h", "help", false, "Optional: print help");
 
 
 		return options;
 	}
 
 }
